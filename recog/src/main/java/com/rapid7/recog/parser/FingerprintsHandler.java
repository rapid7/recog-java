package com.rapid7.recog.parser;

import com.rapid7.recog.FingerprintExample;
import com.rapid7.recog.RecogMatcher;
import com.rapid7.recog.RecogMatchers;
import com.rapid7.recog.parser.RecogParser.PatternMatcherFactory;
import com.rapid7.recog.pattern.RecogPatternMatcher;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * SAX2 event handler used to parse {@link RecogMatchers} from the XML content.
 */
public class FingerprintsHandler extends DefaultHandler {
  private static final String FINGERPRINTS = "fingerprints";
  private static final String FINGERPRINT = "fingerprint";
  private static final String DESCRIPTION = "description";
  private static final String EXAMPLE = "example";
  private static final String PARAM = "param";
  private static final String FILENAME_KEY = "_filename";
  private static final Logger LOGGER = LoggerFactory.getLogger(FingerprintsHandler.class);

  private final PatternMatcherFactory patternMatcherFactory;
  private final boolean strictMode;
  private final String path;
  private final String name;

  private Locator locator;
  private RecogMatchers matchers;
  private RecogMatcher fingerprintPattern;
  private final StringBuilder elementValue;
  private HashMap<String, String> exampleAttributeMap;

  /**
   * Constructs a FingerprintsHandler.
   *
   * @param patternMatcherFactory Factory used to create the underlying {@link RecogPatternMatcher}.
   * @param strictMode {@code true} if the parser should throw exceptions when any error is
   *        encountered, {@code false} otherwise.
   * @param path Optional XML content file path.
   * @param name Value used for {@link RecogMatchers} key if parsed value is null or empty.
   */
  public FingerprintsHandler(PatternMatcherFactory patternMatcherFactory, boolean strictMode, String path, String name) {
    super();
    this.patternMatcherFactory = patternMatcherFactory;
    this.strictMode = strictMode;
    this.path = path;
    this.name = name;
    this.elementValue = new StringBuilder();
  }

  /**
   * Gets parsed {@link RecogMatchers}.
   * @return parsed {@link RecogMatchers}.
   */
  public RecogMatchers getMatchers() {
    return matchers;
  }

  @Override
  public void setDocumentLocator(Locator locator) {
    this.locator = locator; //Save the locator, so that it can be used later for line tracking when traversing nodes.
  }

  @Override
  public void characters(char[] ch, int start, int length) throws SAXException {
    elementValue.append(ch, start, length);
  }

  @Override
  public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
    elementValue.setLength(0);
    try {
      switch (qName.toLowerCase()) {
        case FINGERPRINTS:
          String preferenceValue = getAttribute(attributes, "preference");
          float preference = 0;
          try {
            preference = preferenceValue == null || preferenceValue.isEmpty() ? 0 : Float.parseFloat(preferenceValue);
          } catch (NumberFormatException exception) {
            // ignore - use default
          }

          String recogKey = getAttribute(attributes, "matches");

          if (recogKey == null || recogKey.isEmpty()) {
            LOGGER.debug("Recog Matcher Key is Empty or Null. File Name: " + name);
            recogKey = name;
          }

          matchers = new RecogMatchers(path, recogKey, getAttribute(attributes, "protocol"), getAttribute(attributes, "database_type"), preference);
          break;
        case FINGERPRINT:
          // the pattern is required
          String pattern = getRequiredAttribute(attributes, "pattern");

          // parse the flags for the regular expression
          int regexFlags = parseFlags(getAttribute(attributes,"flags"));

          // construct a pattern
          fingerprintPattern = new RecogMatcher(patternMatcherFactory.create(pattern, regexFlags));
          fingerprintPattern.setLine(locator.getLineNumber());
          break;
        case DESCRIPTION:
          // NOP
          break;
        case EXAMPLE:
          // example (optional)
          exampleAttributeMap = new HashMap<>();
          for (int i = 0; i < attributes.getLength(); i++) {
            String attrName = attributes.getQName(i);
            String attrValue = getAttribute(attributes, i);
            exampleAttributeMap.put(attrName, attrValue);
          }
          break;
        case PARAM:
          if (fingerprintPattern == null) {
            break;
          }

          int position = Integer.parseInt(getRequiredAttribute(attributes, "pos"));
          String paramName = getRequiredAttribute(attributes, "name");

          // zero position indicates a "constant" value
          if (position == 0) {
            String paramValue = getRequiredAttribute(attributes, "value");
            fingerprintPattern.addValue(paramName, paramValue);
          } else {
            // otherwise the position indicates a group match result
            String value = getAttribute(attributes, "value");
            if (!value.isEmpty()) {
              throw new ParseException(String.format("Attribute \"%s\" has a non-zero position but specifies a value of \"%s\"", paramName, value));
            }
            fingerprintPattern.addParam(position, paramName);
          }
          break;
        default:
          LOGGER.info("Unknown qualified name '{}'", qName);
      }
    } catch (ParseException | IllegalArgumentException exception) {
      LOGGER.warn("Failed to parse fingerprint.", exception);
      if (strictMode) {
        throw exception;
      }
    }
  }

  @Override
  public void endElement(String uri, String localName, String qName) throws SAXException {
    try {
      switch (qName.toLowerCase()) {
        case FINGERPRINTS:
        case PARAM:
          // NOP
          break;
        case FINGERPRINT:
          if (fingerprintPattern == null) {
            break;
          }
          matchers.add(fingerprintPattern);
          break;
        case DESCRIPTION:
          // description (optional)
          if (fingerprintPattern != null && elementValue.length() > 0) {
            fingerprintPattern.setDescription(elementValue.toString().replaceAll("\\s+", " ").trim());
          }
          break;
        case EXAMPLE:
          if (fingerprintPattern == null) {
            break;
          }

          String exampleText;
          if (exampleAttributeMap != null && exampleAttributeMap.containsKey(FILENAME_KEY)) {
            // process external example file
            String filename = exampleAttributeMap.get(FILENAME_KEY);
            exampleText = getExternalExampleText(path, name, filename);
          } else {
            exampleText = elementValue.toString();
          }
          fingerprintPattern.addExample(new FingerprintExample(exampleText, exampleAttributeMap));
          break;
        default:
          LOGGER.info("Unknown qualified name '{}'", qName);
      }
    } catch (ParseException | IllegalArgumentException exception) {
      LOGGER.warn("Failed to parse fingerprint.", exception);
      if (strictMode) {
        throw exception;
      }
    }
  }

  /////////////////////////////////////////////////////////////////////////
  // Non-public methods
  /////////////////////////////////////////////////////////////////////////

  private int parseFlags(String flags) {
    int cflags = Pattern.UNIX_LINES;
    if (flags != null && flags.length() != 0) {
      StringTokenizer tok = new StringTokenizer(flags, "|,; \t");
      while (tok.hasMoreTokens()) {
        switch (tok.nextToken()) {
          case "REG_ICASE":
          case "IGNORECASE":
            cflags |= Pattern.CASE_INSENSITIVE;
            break;
          case "REG_DOT_NEWLINE":
          case "REG_MULTILINE":
            cflags |= Pattern.DOTALL;
            cflags |= Pattern.MULTILINE;
            break;
          default:
            // ignore any other flags
            break;
        }
      }
    }

    return cflags;
  }

  public String getAttribute(Attributes attr, String name) {
    String value = attr.getValue(name);
    return (value == null) ? "" : value;
  }

  public String getAttribute(Attributes attr, int index) {
    String value = attr.getValue(index);
    return (value == null) ? "" : value;
  }

  private String getRequiredAttribute(Attributes attr, String name) throws ParseException {
    String value = getAttribute(attr, name);
    if (value.length() == 0)
      throw new ParseException(String.format("Attribute \"%s\" does not exist.", name));

    return value;
  }

  private String getExternalExampleText(String path, String name, String filename) throws ParseException {
    Path examplePath;
    if (path != null) {
      examplePath = Paths.get(Paths.get(path).getParent().toString(), name, filename);
    } else {
      examplePath = Paths.get(name, filename);
    }

    byte[] exampleBytes;
    try {
      exampleBytes = Files.readAllBytes(examplePath);
    } catch (IOException exception) {
      throw new ParseException(String.format("Unable to process fingerprint example file '%s'", examplePath), exception);
    }
    return new String(exampleBytes, StandardCharsets.US_ASCII);
  }
}
