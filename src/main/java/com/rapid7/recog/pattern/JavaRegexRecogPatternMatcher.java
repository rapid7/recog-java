package com.rapid7.recog.pattern;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static java.util.Objects.requireNonNull;

/**
 * An implementation of {@link RecogPatternMatcher} that uses java.util.regex.*
 * packages to match fingerprint values against fingerprint patterns.
 * Matching of the patterns specified is performed using a sub-sequence or "partial"
 * match. See {@link Matcher#find()} vs {@link Matcher#matches()}.
 */
public class JavaRegexRecogPatternMatcher implements RecogPatternMatcher {

  private static class JavaRegexRecogPatternMatchResult implements RecogPatternMatchResult {
    private final Matcher matcher;

    JavaRegexRecogPatternMatchResult(Matcher matcher) {
      this.matcher = matcher;
    }

    @Override
    public int groupCount() {
      return matcher.groupCount();
    }

    @Override
    public String group(int group) {
      return matcher.group(group);
    }

    @Override
    public String group(String group) {
      return matcher.group(group);
    }
  }

  /**
   * The regular expression pattern to match.
   */
  private final Pattern pattern;

  public JavaRegexRecogPatternMatcher(Pattern pattern) {
    this.pattern = requireNonNull(pattern);
  }

  @Override
  public String getPattern() {
    return pattern.pattern();
  }

  @Override
  public int getFlags() {
    return pattern.flags();
  }

  @Override
  public boolean matches(String input) {
    return input != null && pattern.matcher(input).find();
  }

  @Override
  public RecogPatternMatchResult match(String input) {
    if (input == null) {
      return null;
    }
    Matcher matcher = pattern.matcher(input);
    return matcher.find() ? new JavaRegexRecogPatternMatchResult(matcher) : null;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    } else if (!(other instanceof JavaRegexRecogPatternMatcher)) {
      return false;
    } else {
      JavaRegexRecogPatternMatcher that = (JavaRegexRecogPatternMatcher) other;
      return Objects.equals(getPattern(), that.getPattern())
          && Objects.equals(getFlags(), that.getFlags());
    }
  }

  @Override
  public int hashCode() {
    return Objects.hash(pattern);
  }
}
