package com.rapid7.recog;

import java.util.AbstractMap.SimpleEntry;
import java.util.Map;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import static com.rapid7.recog.RecogMatcher.pattern;
import static com.rapid7.recog.TestGenerators.anyString;
import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang3.RandomUtils.nextInt;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FingerprintMatcherTest {

  @Test
  public void matcherWithNullPattern() {
    assertThrows(NullPointerException.class, () -> new RecogMatcher((Pattern) null));
  }

  @Test
  public void valueNameMustNotBeNull() {
    assertThrows(NullPointerException.class, () -> new RecogMatcher(anyPattern()).addValue(null, anyString()));
  }

  @Test
  public void parameterPositionMustBeGreaterThanZero() {
    assertThrows(IllegalArgumentException.class, () -> new RecogMatcher(anyPattern()).addParam(nextInt(Integer.MIN_VALUE, 0), anyString()));
  }

  @Test
  public void matchesSucceeds() {
    // given
    RecogMatcher matcher = new RecogMatcher(pattern("^Apache HTTPD$"));

    // when
    boolean matches = matcher.matches("Apache HTTPD");

    // then
    assertThat(matches, is(true));
  }

  @Test
  public void matchSuceedsCaseInsensitiveFlag() {
    // given
    RecogMatcher matcher = new RecogMatcher(pattern("^Apache HTTPD$", CASE_INSENSITIVE));

    // when
    boolean matches = matcher.matches("apache httpd");

    // then
    assertThat(matches, is(true));
  }

  @Test
  public void matchesFails() {
    // given
    RecogMatcher matcher = new RecogMatcher(pattern("^Apache HTTPD$"));

    // when
    boolean matches = matcher.matches("Apache Web Server");

    // then
    assertThat(matches, is(false));
  }

  @Test
  public void matchesFailsNull() {
    // given
    RecogMatcher matcher = new RecogMatcher(pattern("^Apache HTTPD$"));

    // when
    boolean matches = matcher.matches(null);

    // then
    assertThat(matches, is(false));
  }

  @Test
  public void matchFails() {
    // given
    RecogMatcher matcher = new RecogMatcher(pattern("^Apache HTTPD$"));

    // when
    Map<String, String> parameters = matcher.match("Apache Web Server");

    // then
    assertThat(parameters, is(nullValue()));
  }

  @Test
  public void matchFailsNull() {
    // given
    RecogMatcher matcher = new RecogMatcher(pattern("^Apache HTTPD$"));

    // when
    Map<String, String> parameters = matcher.match(null);

    // then
    assertThat(parameters, is(nullValue()));
  }

  @Test
  public void matchSuceedsNoParameters() {
    // given
    RecogMatcher matcher = new RecogMatcher(pattern("^Apache HTTPD$"));

    // when
    Map<String, String> parameters = matcher.match("Apache HTTPD");

    // then
    assertThat(parameters.keySet(), is(empty()));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void matchSucceedsStaticValueParameters() {
    // given
    String name = "service.family";
    String value = "Apache";
    RecogMatcher matcher = new RecogMatcher(pattern("^Apache HTTPD$")).addValue(name, value);

    // when
    Map<String, String> parameters = matcher.match("Apache HTTPD");

    // then
    assertThat(parameters.entrySet(), containsInAnyOrder(new SimpleEntry<>(name, value)));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void matchSucceedsStaticAndGroupValueParameters() {
    // given
    String valueName = "service.family";
    String value = "Apache";
    String paramName = "service.version";
    RecogMatcher matcher = new RecogMatcher(pattern("^Apache HTTPD (.*)$")).addValue(valueName, value).addParam(1, paramName);

    // when
    Map<String, String> parameters = matcher.match("Apache HTTPD 6.5");

    // then
    assertThat(parameters.entrySet(), containsInAnyOrder(new SimpleEntry<>(valueName, value), new SimpleEntry<>(paramName, "6.5")));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void matchSucceedsGroupParameterDoesNotExistIgnored() {
    // given
    String valueName = "service.family";
    String value = "Apache";
    String paramName = "service.version";
    RecogMatcher matcher = new RecogMatcher(pattern("^Apache HTTPD (.*)$")).addValue(valueName, value).addParam(2, paramName);

    // when
    Map<String, String> parameters = matcher.match("Apache HTTPD 6.5");

    // then
    assertThat(parameters.entrySet(), containsInAnyOrder(new SimpleEntry<>(valueName, value)));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void matchSucceedsNamedGroupParameter() {
    // given
    String valueName = "service.family";
    String value = "Apache";
    String paramName = "version";
    RecogMatcher matcher = new RecogMatcher(pattern("^Apache HTTPD (?<version>.*)$")).addValue(valueName, value).addParam(paramName);

    // when
    Map<String, String> parameters = matcher.match("Apache HTTPD 6.5");

    // then
    assertThat(parameters.entrySet(), containsInAnyOrder(new SimpleEntry<>(valueName, value), new SimpleEntry<>(paramName, "6.5")));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void matchSucceedsNamedGroupDoesNotExistIgnored() {
    // given
    String valueName = "service.family";
    String value = "Apache";
    String paramName = "version";
    RecogMatcher matcher = new RecogMatcher(pattern("^Apache HTTPD (.*)$")).addValue(valueName, value).addParam(paramName);

    // when
    Map<String, String> parameters = matcher.match("Apache HTTPD 6.5");

    // then
    assertThat(parameters.entrySet(), containsInAnyOrder(new SimpleEntry<>(valueName, value)));
  }

  /////////////////////////////////////////////////////////////////////////
  // Non-public methods
  /////////////////////////////////////////////////////////////////////////

  private Pattern anyPattern() {
    return Pattern.compile(randomAlphabetic(16));
  }
}
