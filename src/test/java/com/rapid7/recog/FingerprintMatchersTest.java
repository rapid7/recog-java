package com.rapid7.recog;

import java.util.HashMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import org.junit.jupiter.api.Test;
import static com.rapid7.recog.RecogMatcher.pattern;
import static com.rapid7.recog.TestGenerators.anyString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.hasSize;

public class FingerprintMatchersTest {

  @Test
  public void matchesNullInput() {
    // given
    RecogMatchers matchers = new RecogMatchers();

    // when
    List<RecogMatch> matches = matchers.getMatches(null);

    // then
    assertThat(matches, is(empty()));
  }

  @Test
  public void matchesNoMatchers() {
    // given
    String fingerprint = anyString();
    RecogMatchers matchers = new RecogMatchers();

    // when
    List<RecogMatch> matches = matchers.getMatches(fingerprint);

    // then
    assertThat(matches, is(empty()));
  }

  @Test
  public void matchesNoMatches() {
    // given
    String fingerprint = anyString();
    RecogMatchers matchers = new RecogMatchers();
    matchers.add(new RecogMatcher(pattern("foo")));
    matchers.add(new RecogMatcher(pattern("bar")));
    matchers.add(new RecogMatcher(pattern("car")));

    // when
    List<RecogMatch> matches = matchers.getMatches(fingerprint);

    // then
    assertThat(matches, is(empty()));
  }

  @Test
  public void matchesSingleMatchNoParameters() {
    // given
    String fingerprint = "Apache Tomcat";
    RecogMatchers matchers = new RecogMatchers();
    matchers.add(new RecogMatcher(pattern("Apache HTTPD")));
    matchers.add(new RecogMatcher(pattern("Apache Tomcat")));
    matchers.add(new RecogMatcher(pattern("Microsoft IIS")));

    // when
    List<RecogMatch> matches = matchers.getMatches(fingerprint);

    // then
    assertThat(matches, hasSize(1));
    assertThat(matches.get(0).getParameters().keySet(), is(empty()));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void matchesSingleMatchParameters() {
    // given
    String fingerprint = "Apache Tomcat 8.3";
    RecogMatchers matchers = new RecogMatchers();
    matchers.add(new RecogMatcher(pattern("Apache HTTPD (.*)")));
    matchers.add(new RecogMatcher(pattern("Apache Tomcat (.*)")).addParam(1, "version"));
    matchers.add(new RecogMatcher(pattern("Microsoft IIS (.*)")));

    // when
    List<RecogMatch> matches = matchers.getMatches(fingerprint);

    // then
    assertThat(matches, hasSize(1));
    assertThat(matches.get(0).getParameters().entrySet(), containsInAnyOrder(new SimpleEntry<>("version", "8.3")));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void matchesMultipleMatchesParameters() {
    // given
    String fingerprint = "Apache Tomcat 8.3";
    RecogMatchers matchers = new RecogMatchers();
    matchers.add(new RecogMatcher(pattern("Apache HTTPD (.*)")));
    matchers.add(new RecogMatcher(pattern("Apache Tomcat (.*)")).addParam(1, "version"));
    matchers.add(new RecogMatcher(pattern("Apache Tomcat (.*)\\.(.*)")).addParam(1, "major.version").addParam(2, "minor.version"));

    // when
    List<RecogMatch> matches = matchers.getMatches(fingerprint);

    // then
    assertThat(matches, hasSize(2));
    assertThat(matches.get(0).getParameters().entrySet(), containsInAnyOrder(new SimpleEntry<>("version", "8.3")));
    assertThat(matches.get(1).getParameters().entrySet(), containsInAnyOrder(new SimpleEntry<>("major.version", "8"), new SimpleEntry<>("minor.version", "3")));
  }

  @Test
  public void matchesReturnsValidCPE() {
    // given
    String fingerprint = "Apache/2.2.3";
    RecogMatchers matchers = new RecogMatchers();
    matchers.add(new RecogMatcher(pattern("^Apache$")));
    matchers.add(new RecogMatcher(pattern("^Apache(?:-AdvancedExtranetServer)?(?:/([012][\\d.]*)\\s*(.*))?$")).addParam(1, "service.version").addValue("service.cpe23", "cpe:/a:apache:http_server:{service.version}"));

    // when
    List<RecogMatch> matches = matchers.getMatches(fingerprint);

    // then
    assertThat(matches, hasSize(1));
    assertThat(matches.get(0).getParameters().get("service.cpe23"), is("cpe:/a:apache:http_server:2.2.3"));
  }

  @Test
  public void firstMatchMatches() {
    // given
    String fingerprint = "Apache/2.2.3";
    RecogMatchers matchers = new RecogMatchers();
    matchers.add(new RecogMatcher(pattern("^Apache$")));
    matchers.add(new RecogMatcher(pattern("^Apache(?:-AdvancedExtranetServer)?(?:/([012][\\d.]*)\\s*(.*))?$")));

    // when
    RecogMatch match = matchers.getFirstMatch(fingerprint);

    // then
    assertThat(match, is(notNullValue()));
  }

  @Test
  public void firstMatchDoesNotMatch() {
    // given
    String fingerprint = "foo";
    RecogMatchers matchers = new RecogMatchers();
    matchers.add(new RecogMatcher(pattern("^Apache$")));
    matchers.add(new RecogMatcher(pattern("^Apache(?:-AdvancedExtranetServer)?(?:/([012][\\d.]*)\\s*(.*))?$")));

    // when
    RecogMatch match = matchers.getFirstMatch(fingerprint);

    // then
    assertThat(match, is(nullValue()));
  }

  @Test
  public void firstMatchReturnsValidCPE() {
    // given
    String fingerprint = "Apache/2.2.3";
    RecogMatchers matchers = new RecogMatchers();
    matchers.add(new RecogMatcher(pattern("^Apache$")));
    matchers.add(new RecogMatcher(pattern("^Apache(?:-AdvancedExtranetServer)?(?:/([012][\\d.]*)\\s*(.*))?$")).addParam(1, "service.version").addValue("service.cpe23", "cpe:/a:apache:http_server:{service.version}"));

    // when
    RecogMatch match = matchers.getFirstMatch(fingerprint);

    // then
    assertThat(match.getParameters().get("service.cpe23"), is("cpe:/a:apache:http_server:2.2.3"));
  }

  @Test
  public void multipleInterpolatedValuesCPE() {
    // given
    String fingerprint = "asdfbar";
    RecogMatchers matchers = new RecogMatchers();
    matchers.add(new RecogMatcher(pattern("(asdf)(bar)")).addParam(1, "first").addParam(2, "second").addValue("cpe.cpe23", "cpe:/a:test:{first}:{second}"));

    // when
    RecogMatch match = matchers.getFirstMatch(fingerprint);

    // then
    assertThat(match.getParameters().get("cpe.cpe23"), is("cpe:/a:test:asdf:bar"));
  }

  @Test
  public void interpolationMissingFieldCausesDash() {
    // given
    String fingerprint = "Apache";
    RecogMatchers matchers = new RecogMatchers();
    matchers.add(new RecogMatcher(pattern("^Apache(?:-AdvancedExtranetServer)?(?:/([012][\\d.]*)\\s*(.*))?$")).addParam(1, "service.version").addValue("service.cpe23", "cpe:/a:apache:http_server:{service.version}"));

    // when
    RecogMatch match = matchers.getFirstMatch(fingerprint);

    // then
    assertThat(match.getParameters().get("service.cpe23"), is("cpe:/a:apache:http_server:-"));
  }

  @Test
  public void multipleOfTheSameInterpolationProperty() {
    // given
    String fingerprint = "Apache/2.2.3";
    RecogMatchers matchers = new RecogMatchers();
    matchers.add(new RecogMatcher(pattern("^Apache(?:-AdvancedExtranetServer)?(?:/([012][\\d.]*)\\s*(.*))?$")).addParam(1, "service.version").addValue("service.cpe23", "cpe:/a:apache:http_server:{service.version}:{service.version}"));

    // when
    RecogMatch match = matchers.getFirstMatch(fingerprint);

    // then
    assertThat(match.getParameters().get("service.cpe23"), is("cpe:/a:apache:http_server:2.2.3:2.2.3"));
  }

  @Test
  public void interpolateWithNullSuffix() {
    // given
    RecogMatchers matchers = new RecogMatchers();
    HashMap<String, String> map = new HashMap<>();
    map.put("foo", "test");
    map.put("bar", "{foo}");

    // when
    matchers.interpolate(null, map);

    // then
    assertThat(map.get("bar"), is("test"));
    assertThat(map.get("foo"), is("test"));
  }

  @Test
  public void interpolateWithNonNullSuffix() {
    // given
    RecogMatchers matchers = new RecogMatchers();
    HashMap<String, String> map = new HashMap<>();
    map.put("foo", "test");
    map.put("bar", "{foo}");

    // when
    matchers.interpolate("bar", map);

    // then
    assertThat(map.get("bar"), is("test"));
    assertThat(map.get("foo"), is("test"));
  }
}
