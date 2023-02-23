package com.rapid7.recog.parser;

import com.rapid7.recog.RecogMatcher;
import com.rapid7.recog.RecogMatchers;
import com.rapid7.recog.pattern.JavaRegexRecogPatternMatcher;
import com.rapid7.recog.pattern.RecogPatternMatcher;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.regex.Pattern;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


/**
 * Parses {@link RecogMatcher} objects from XML input. This parser has support for strict or lenient
 * parsing mode. In lenient mode parsing is tolerant and imposes the minimal constraints on input
 * required to produce valid objects as outputs. Errors in input are logged and as many objects are
 * parsed as possible. In strict mode, any errors during validation immediately throw exceptions and
 * halt processing.
 */
public class RecogParser {

  private static final String FILENAME_KEY = "_filename";

  /**
   * Factory used to create the underlying {@link RecogPatternMatcher} used
   * when matching inputs against regular expressions.
   */
  public interface PatternMatcherFactory {
    RecogPatternMatcher create(String pattern, int flags);
  }

  /**
   * The default {@link PatternMatcherFactory} uses java.regex.* packages to evaluate
   * regular expressions.
   */
  public static final PatternMatcherFactory DEFAULT_PATTERN_MATCHER_FACTORY =
      (pattern, flags) -> new JavaRegexRecogPatternMatcher(Pattern.compile(pattern, flags));

  private static final Logger LOGGER = LoggerFactory.getLogger(RecogParser.class);
  private final boolean strictMode;
  private final PatternMatcherFactory patternMatcherFactory;

  /**
   * Constructs a parser to parser with non-strict (lenient) parsing mode.
   */
  public RecogParser() {
    this(false);
  }

  /**
   * Constructs a parser to parser with the specified strictness mode.
   *
   * @param strictMode {@code true} if the parser should throw exceptions when any error is
   *        encountered, {@code false} otherwise.
   */
  public RecogParser(boolean strictMode) {
    this(strictMode, DEFAULT_PATTERN_MATCHER_FACTORY);
  }

  /**
   * Constructs a parser with the specified strictness mode and {@link PatternMatcherFactory}.
   *
   * @param strictMode {@code true} if the parser should throw exceptions when any error is
   *        encountered, {@code false} otherwise.
   * @param patternMatcherFactory The {@link PatternMatcherFactory} to be used during parsing.
   */
  public RecogParser(boolean strictMode, PatternMatcherFactory patternMatcherFactory) {
    this.strictMode = strictMode;
    this.patternMatcherFactory = patternMatcherFactory;
  }

  /**
   * Parses {@link RecogMatchers} from the XML content in the specified {@link File}.
   *
   * @param file The content to read from. Must not be {@code null}.
   * @return {@link RecogMatchers} parsed from the file. Will not be {@code null} but may be empty
   *         if no matchers are defined, or all matchers are invalid and strict mode is disabled.
   * @throws ParseException If an error is encountered and strict-mode is enabled.
   */
  public RecogMatchers parse(File file)
      throws ParseException {
    try (Reader reader = new FileReader(file)) {
      return parse(reader, file.getPath(), file.getName().replaceAll(".xml", ""));
    } catch (Exception exception) {
      throw new ParseException("Failed to parse recog fingerprints from file " + file.getAbsolutePath(), exception);
    }
  }

  /**
   * Parses {@link RecogMatchers} from the XML content in the specified {@link Reader}.
   *
   * @param reader The content to read from. Must not be {@code null}.
   * @param name Value used for {@link RecogMatchers} key if parsed value is null or empty.
   * @return {@link RecogMatchers} parsed from the reader. Will not be {@code null} but may be empty
   *         if no matchers are defined, or all matchers are invalid and strict mode is disabled.
   * @throws ParseException If an error is encountered and strict-mode is enabled.
   */
  public RecogMatchers parse(Reader reader, String name)
      throws ParseException {
    RecogMatchers matchers = parse(reader, null, name);
    if (matchers == null) {
      throw new ParseException("Failed to parse file: " + name);
    }
    return matchers;
  }

  /**
   * Parses {@link RecogMatchers} from the XML content in the specified {@link Reader}.
   *
   * @param reader The content to read from. Must not be {@code null}.
   * @param path Optional XML content file path.
   * @param name Value used for {@link RecogMatchers} key if parsed value is null or empty.
   * @return {@link RecogMatchers} parsed from the reader. Will not be {@code null} but may be empty
   *         if no matchers are defined, or all matchers are invalid and strict mode is disabled.
   * @throws ParseException If an error is encountered and strict-mode is enabled.
   */
  public RecogMatchers parse(Reader reader, String path, String name) throws ParseException {
    RecogMatchers matchers = null;
    try {
      SAXParserFactory factory = SAXParserFactory.newInstance();
      factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
      factory.setFeature("http://xml.org/sax/features/validation", false);
      factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
      factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
      SAXParser saxParser = factory.newSAXParser();
      FingerprintsHandler handler = new FingerprintsHandler(this.patternMatcherFactory, this.strictMode, path, name);
      saxParser.parse(new InputSource(reader), handler);
      matchers = handler.getMatchers();
    } catch (ParseException exception) {
      // re-throw ParseException
      throw exception;
    } catch (SAXException | IOException | ParserConfigurationException exception) {
      System.out.printf("parse(): exception.getMessage(): %s\n", exception.getMessage());

      throw new ParseException("Unable to parse fingerprints from Document", exception);
    }
    return matchers;
  }
}
