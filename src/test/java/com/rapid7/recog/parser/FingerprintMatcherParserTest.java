package com.rapid7.recog.parser;

import com.rapid7.recog.RecogMatcher;
import com.rapid7.recog.RecogMatchers;
import java.io.StringReader;
import org.junit.jupiter.api.Test;
import static com.rapid7.recog.RecogMatcher.pattern;
import static com.rapid7.recog.TestGenerators.anyString;
import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static java.util.regex.Pattern.DOTALL;
import static java.util.regex.Pattern.MULTILINE;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
        new RecogMatcher(pattern("^Apache$", MULTILINE)).addValue("service.vendor", "Apache").addValue("service.product", "HTTPD").addValue("service.family", "Apache")));
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
}
