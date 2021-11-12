package com.rapid7.recog.parser;

import com.rapid7.recog.RecogMatcher;
import com.rapid7.recog.RecogMatchers;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static com.rapid7.recog.RecogMatcher.pattern;
import static com.rapid7.recog.TestGenerators.anyString;
import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static java.util.regex.Pattern.DOTALL;
import static java.util.regex.Pattern.MULTILINE;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;

public class FingerprintMatcherParserTest {

  @Test
  public void noFingerprintsAreReadWhenInputIsEmpty() throws ParseException {
    // given
    String xml = "<?xml version=\"1.0\"?><fingerprints/>";

    // when
    RecogMatchers patterns = new RecogParser().parse(new StringReader(xml), anyString());

    // then
    assertThat(patterns.size(), is(0));
  }

  @Test
  public void invalidXmlInputCausesExceptionToBeThrown() throws ParseException {
    // given
    String xml = "<?xml version=\"1.0\"?>foo";

    // when
    assertThrows(ParseException.class, () -> new RecogParser().parse(new StringReader(xml), anyString()));

    // then - throws exception
  }

  @Test
  public void validFingerprint() throws ParseException {
    // given
    String xml = "<?xml version=\"1.0\"?>\n"
            + "<fingerprints matches=\"http_header.server\">"
            + "   <fingerprint pattern=\"^Apache (\\d)$\" flags=\"REG_DOT_NEWLINE,REG_MULTILINE\">\n"
            + "       <description>Apache returning only its major version number</description>\n"
            + "       <example>Apache 1</example>\n"
            + "       <example>Apache 2</example>\n"
            + "       <param pos=\"0\" name=\"service.vendor\" value=\"Apache\"/>\n"
            + "       <param pos=\"0\" name=\"service.product\" value=\"HTTPD\"/>\n"
            + "       <param pos=\"0\" name=\"service.family\" value=\"Apache\"/>\n"
            + "       <param pos=\"1\" name=\"service.version\"/>\n"
            + "    </fingerprint>\n"
            + "</fingerprints>";

    // when
    RecogMatchers patterns = new RecogParser().parse(new StringReader(xml), anyString());

    // then
    assertThat(patterns.size(), is(1));
    assertThat(patterns, hasItems(new RecogMatcher(pattern("^Apache (\\d)$", DOTALL, MULTILINE)).addValue("service.vendor", "Apache").addValue("service.product", "HTTPD").addValue("service.family", "Apache").addParam(1, "service.version")));
  }

  @Test
  public void twoValidFingerprints() throws ParseException {
    // given
    String xml = "<?xml version=\"1.0\"?>\n"
            + "<fingerprints matches=\"http_header.server\">"
            + "   <fingerprint pattern=\"^Apache/\\d$\" flags=\"REG_ICASE\">\n"
            + "       <description>Apache returning only its major version number</description>\n"
            + "       <example>Apache/1</example>\n"
            + "       <example>Apache/2</example>\n"
            + "       <param pos=\"0\" name=\"service.vendor\" value=\"Apache\"/>\n"
            + "       <param pos=\"0\" name=\"service.product\" value=\"HTTPD\"/>\n"
            + "       <param pos=\"0\" name=\"service.family\" value=\"Apache\"/>\n"
            + "    </fingerprint>\n"
            + "    <fingerprint pattern=\"^Apache$\" flags=\"REG_MULTILINE\">\n"
            + "        <description>Apache returning no version information</description>\n"
            + "        <example>Apache</example>\n"
            + "        <example>apache</example>\n"
            + "        <param pos=\"0\" name=\"service.vendor\" value=\"Apache\"/>\n"
            + "        <param pos=\"0\" name=\"service.product\" value=\"HTTPD\"/>\n"
            + "        <param pos=\"0\" name=\"service.family\" value=\"Apache\"/>\n"
            + "    </fingerprint>"
            + "</fingerprints>";

    // when
    RecogMatchers patterns = new RecogParser().parse(new StringReader(xml), anyString());

    // then
    assertThat(patterns.size(), is(2));
    assertThat(patterns, hasItems(
            new RecogMatcher(pattern("^Apache/\\d$", CASE_INSENSITIVE)).addValue("service.vendor", "Apache").addValue("service.product", "HTTPD").addValue("service.family", "Apache"),
            new RecogMatcher(pattern("^Apache$", DOTALL, MULTILINE)).addValue("service.vendor", "Apache").addValue("service.product", "HTTPD").addValue("service.family", "Apache")));
  }

  @Test
  public void validFingerprintExternalExampleFile() throws ParseException {
    // given
    int exServiceVersion = 1;
    String exText = String.format("Apache %d", exServiceVersion);
    String exFilename = anyString();
    String xml = String.format("<?xml version=\"1.0\"?>\n"
        + "<fingerprints matches=\"http_header.server\">"
        + "    <fingerprint pattern=\"^Apache (\\d)$\">\n"
        + "        <description>Apache returning only its major version number</description>\n"
        + "        <example _filename=\"%s\" service.version=\"%d\"></example>\n"
        + "        <param pos=\"0\" name=\"service.vendor\" value=\"Apache\"/>\n"
        + "        <param pos=\"0\" name=\"service.product\" value=\"HTTPD\"/>\n"
        + "        <param pos=\"0\" name=\"service.family\" value=\"Apache\"/>\n"
        + "        <param pos=\"1\" name=\"service.version\"/>\n"
        + "    </fingerprint>\n"
        + "</fingerprints>", exFilename, exServiceVersion);

    try (MockedStatic<Files> mockFiles = Mockito.mockStatic(Files.class)) {
      mockFiles.when(() -> Files.readAllBytes(any(Path.class)))
          .thenReturn(exText.getBytes(StandardCharsets.US_ASCII));

      // when
      RecogMatchers patterns = new RecogParser().parse(new StringReader(xml), anyString());

      // then
      assertThat(patterns.size(), is(1));
      assertThat(patterns, hasItems(new RecogMatcher(pattern("^Apache (\\d)$")).addValue("service.vendor", "Apache").addValue("service.product", "HTTPD").addValue("service.family", "Apache").addParam(1, "service.version")));
      assertThat(patterns.get(0).getExamples().size(), is(1));
      mockFiles.verify(() -> Files.readAllBytes(any(Path.class)));
      assertThat(patterns.get(0).getExamples(), contains(hasProperty("text", is(exText))));
    }
  }

  @Test
  public void invalidFingerprintsIgnored() throws ParseException {
    // given
    String xml = "<?xml version=\"1.0\"?>\n"
            + "<fingerprints matches=\"http_header.server\">"
            + "   <fingerprint pattern=\"^Apache/\\d$\" flags=\"REG_ICASE\">\n"
            + "        <description>Apache returning only its major version number</description>\n"
            + "        <example>Apache/1</example>\n"
            + "        <example>Apache/2</example>\n"
            + "        <param pos=\"0\" name=\"service.vendor\" value=\"Apache\"/>\n"
            + "        <param pos=\"0\" name=\"service.product\" value=\"HTTPD\"/>\n"
            + "        <param pos=\"0\" name=\"service.family\" value=\"Apache\"/>\n"
            + "   </fingerprint>\n"
            + "   <broken_fingerprint pattern=\"^Apache/\\d$\" flags=\"REG_ICASE\">\n"
            + "      <description>Apache returning only its major version number</description>\n"
            + "      <example>Apache/1</example>\n"
            + "      <param pos=\"0\" name=\"service.vendor\" value=\"Apache\"/>\n"
            + "   </broken_fingerprint>\n"
            + "   <fingerprint pattern=\"^Apache$\" flags=\"REG_ICASE\">\n"
            + "       <description>Apache returning no version information</description>\n"
            + "       <example>Apache</example>\n"
            + "       <example>apache</example>\n"
            + "       <param pos=\"0\" name=\"service.vendor\" value=\"Apache\"/>\n"
            + "       <param pos=\"0\" name=\"service.product\" value=\"HTTPD\"/>\n"
            + "       <param pos=\"0\" name=\"service.family\" value=\"Apache\"/>\n"
            + "    </fingerprint>"
            + "</fingerprints>";

    // when
    RecogMatchers patterns = new RecogParser().parse(new StringReader(xml), anyString());

    // then
    assertThat(patterns.size(), is(2));
  }

  @Test
  public void patternIsRequired() throws ParseException {
    // given
    String xml = "<?xml version=\"1.0\"?>\n"
            + "<fingerprints matches=\"http_header.server\">"
            + "   <fingerprint flags=\"REG_DOT_NEWLINE\">\n"
            + "       <description>Apache returning only its major version number</description>\n"
            + "       <example>Apache 1</example>\n"
            + "       <example>Apache 2</example>\n"
            + "       <param pos=\"0\" name=\"service.vendor\" value=\"Apache\"/>\n"
            + "       <param pos=\"0\" name=\"service.product\" value=\"HTTPD\"/>\n"
            + "       <param pos=\"0\" name=\"service.family\" value=\"Apache\"/>\n"
            + "       <param pos=\"1\" name=\"service.version\"/>\n"
            + "    </fingerprint>\n"
            + "</fingerprints>";

    // when
    RecogMatchers patterns = new RecogParser().parse(new StringReader(xml), anyString());

    // then
    assertThat(patterns.size(), is(0));
  }

  @Test
  public void patternMustBeValid() throws ParseException {
    // given
    String xml = "<?xml version=\"1.0\"?>\n"
            + "<fingerprints matches=\"http_header.server\">"
            + "   <fingerprint pattern=\"^Apache(/\\d$\" flags=\"REG_DOT_NEWLINE\">\n"
            + "       <description>Apache returning only its major version number</description>\n"
            + "       <example>Apache 1</example>\n"
            + "       <example>Apache 2</example>\n"
            + "       <param pos=\"0\" name=\"service.vendor\" value=\"Apache\"/>\n"
            + "       <param pos=\"0\" name=\"service.product\" value=\"HTTPD\"/>\n"
            + "       <param pos=\"0\" name=\"service.family\" value=\"Apache\"/>\n"
            + "       <param pos=\"1\" name=\"service.version\"/>\n"
            + "    </fingerprint>\n"
            + "</fingerprints>";

    // when
    RecogMatchers patterns = new RecogParser().parse(new StringReader(xml), anyString());

    // then
    assertThat(patterns.size(), is(0));
  }

  @Test
  public void emptyFlagsIgnored() throws ParseException {
    // given
    String xml = "<?xml version=\"1.0\"?>\n"
            + "<fingerprints matches=\"http_header.server\">"
            + "   <fingerprint pattern=\"^Apache (\\d)$\" flags=\"\">\n"
            + "       <description>Apache returning only its major version number</description>\n"
            + "       <example>Apache 1</example>\n"
            + "       <example>Apache 2</example>\n"
            + "       <param pos=\"0\" name=\"service.vendor\" value=\"Apache\"/>\n"
            + "       <param pos=\"0\" name=\"service.product\" value=\"HTTPD\"/>\n"
            + "       <param pos=\"0\" name=\"service.family\" value=\"Apache\"/>\n"
            + "       <param pos=\"1\" name=\"service.version\"/>\n"
            + "    </fingerprint>\n"
            + "</fingerprints>";

    // when
    RecogMatchers patterns = new RecogParser().parse(new StringReader(xml), anyString());

    // then
    assertThat(patterns.size(), is(1));
    assertThat(patterns, hasItems(new RecogMatcher(pattern("^Apache (\\d)$")).addValue("service.vendor", "Apache").addValue("service.product", "HTTPD").addValue("service.family", "Apache").addParam(1, "service.version")));
  }

  @Test
  public void unknownFlagIgnored() throws ParseException {
    // given
    String xml = "<?xml version=\"1.0\"?>\n"
            + "<fingerprints matches=\"http_header.server\">"
            + "   <fingerprint pattern=\"^Apache (\\d)$\" flags=\"foo\">\n"
            + "       <description>Apache returning only its major version number</description>\n"
            + "       <example>Apache 1</example>\n"
            + "       <example>Apache 2</example>\n"
            + "       <param pos=\"0\" name=\"service.vendor\" value=\"Apache\"/>\n"
            + "       <param pos=\"0\" name=\"service.product\" value=\"HTTPD\"/>\n"
            + "       <param pos=\"0\" name=\"service.family\" value=\"Apache\"/>\n"
            + "       <param pos=\"1\" name=\"service.version\"/>\n"
            + "    </fingerprint>\n"
            + "</fingerprints>";

    // when
    RecogMatchers patterns = new RecogParser().parse(new StringReader(xml), anyString());

    // then
    assertThat(patterns.size(), is(1));
    assertThat(patterns, hasItems(new RecogMatcher(pattern("^Apache (\\d)$")).addValue("service.vendor", "Apache").addValue("service.product", "HTTPD").addValue("service.family", "Apache").addParam(1, "service.version")));
  }

  @Test
  public void paramZeroPositionWithNoValueFailsWhenStrict() {
    // given
    String xml = "<?xml version=\"1.0\"?>\n"
            + "<fingerprints matches=\"http_header.server\">"
            + "    <fingerprint pattern=\"^Apache (\\d)$\" flags=\"foo\">\n"
            + "        <description>Apache returning only its major version number</description>\n"
            + "        <example>Apache 1</example>\n"
            + "        <example>Apache 2</example>\n"
            + "        <param pos=\"0\" name=\"service.vendor\" value=\"\"/>\n"
            + "        <param pos=\"0\" name=\"service.product\" value=\"HTTPD\"/>\n"
            + "        <param pos=\"0\" name=\"service.family\" value=\"Apache\"/>\n"
            + "        <param pos=\"1\" name=\"service.version\"/>\n"
            + "    </fingerprint>\n"
            + "</fingerprints>";
    String expectedMessage = "Attribute \"value\" does not exist.";

    // when
    Exception exception = assertThrows(ParseException.class, () -> {
      new RecogParser(true).parse(new StringReader(xml), anyString());
    }, expectedMessage);

    // then
    assertEquals(expectedMessage, exception.getMessage());
  }

  @Test
  public void paramNonZeroPositionWithValueFailsWhenStrict() {
    // given
    String paramName = "service.version";
    String paramValue = "1";
    String xml = String.format("<?xml version=\"1.0\"?>\n"
            + "<fingerprints matches=\"http_header.server\">"
            + "    <fingerprint pattern=\"^Apache (\\d)$\" flags=\"foo\">\n"
            + "        <description>Apache returning only its major version number</description>\n"
            + "        <example>Apache 1</example>\n"
            + "        <example>Apache 2</example>\n"
            + "        <param pos=\"0\" name=\"service.vendor\" value=\"Apache\"/>\n"
            + "        <param pos=\"0\" name=\"service.product\" value=\"HTTPD\"/>\n"
            + "        <param pos=\"0\" name=\"service.family\" value=\"Apache\"/>\n"
            + "        <param pos=\"1\" name=\"%s\" value=\"%s\"/>\n"
            + "    </fingerprint>\n"
            + "</fingerprints>", paramName, paramValue);
    String expectedMessage = String.format("Attribute \"%s\" has a non-zero position but specifies a value of \"%s\"", paramName, paramValue);

    // when
    Exception exception = assertThrows(ParseException.class, () -> {
      new RecogParser(true).parse(new StringReader(xml), anyString());
    }, expectedMessage);

    // then
    assertEquals(expectedMessage, exception.getMessage());
  }

  @Test
  public void missingExternalExampleFileFailsWhenStrict() {
    // given
    String exFilename = anyString();
    String xml = String.format("<?xml version=\"1.0\"?>\n"
        + "<fingerprints matches=\"http_header.server\">"
        + "    <fingerprint pattern=\"^Apache (\\d)$\">\n"
        + "        <description>Apache returning only its major version number</description>\n"
        + "        <example _filename=\"%s\"></example>\n"
        + "        <param pos=\"0\" name=\"service.vendor\" value=\"Apache\"/>\n"
        + "        <param pos=\"0\" name=\"service.product\" value=\"HTTPD\"/>\n"
        + "        <param pos=\"0\" name=\"service.family\" value=\"Apache\"/>\n"
        + "        <param pos=\"1\" name=\"service.version\"/>\n"
        + "    </fingerprint>\n"
        + "</fingerprints>", exFilename);
    String name = anyString();
    String expectedMessage = String.format("Unable to process fingerprint example file '%s'", Paths.get(name, exFilename));

    // when
    Exception exception = assertThrows(ParseException.class, () -> {
      new RecogParser(true).parse(new StringReader(xml), name);
    }, expectedMessage);

    // then
    assertEquals(expectedMessage, exception.getMessage());
  }
}
