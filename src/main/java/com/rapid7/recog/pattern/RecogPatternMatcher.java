package com.rapid7.recog.pattern;

/**
 * Performs matching of input values against a regular expression that supports grouped parameter
 * extraction.
 */
public interface RecogPatternMatcher {

  /** The regex pattern this matcher matches. */
  String getPattern();

  int getFlags();

  /**
   * Returns whether this matcher matches the specified input fingerprint value.
   *
   * @param input The fingerprint to test this matcher against. May be {@code null}.
   * @return {@code true} if the input is non-{@code null} and matches the fingerprint matcher
   *     pattern.
   */
  boolean matches(String input);

  /**
   * Matches the regular expression against the specified input.
   *
   * @param input The fingerprint to match. May be {@code null}.
   * @return {@code null} if the input does not match the pattern, otherwise a non-{@code null}
   *     {@link RecogPatternMatchResult}
   */
  RecogPatternMatchResult match(String input);

}
