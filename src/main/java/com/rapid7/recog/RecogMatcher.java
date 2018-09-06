package com.rapid7.recog;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static com.google.common.base.MoreObjects.toStringHelper;
import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableSet;
import static java.util.Objects.requireNonNull;

/**
 * Performs matching of input against a regular expression that supports grouped parameter
 * extraction. Matching of the patterns specified is performed using a sub-sequence or "partial"
 * match. See {@link Matcher#find()} vs {@link Matcher#matches()}.
 */
public class RecogMatcher implements Serializable {

  /** The regular expression pattern to match. */
  private Pattern pattern;

  /** "Constant" values always matched as parameters. Key is the name, value is the value. */
  private Map<String, String> values;

  /**
   * Positional parameters which are associated to a matching group in the expression. Key is the
   * parameter name, the value is the group position (one-based).
   */
  private Map<String, Integer> positionalParameters;

  /**
   * The named parameters which are associated to a named matching group. The name represents both
   * the parameter name and the name of the matching group in the regular expression.
   */
  private Set<String> namedParameters;

  /** An optional human-readable description of the matcher. */
  private String description;

  /** Optional examples that illustrate the matcher (or that can be used to test the matcher). */
  private Set<String> examples;

  public RecogMatcher(Pattern pattern) {
    this.pattern = requireNonNull(pattern);
    values = new HashMap<>();
    positionalParameters = new HashMap<>();
    namedParameters = new HashSet<>();
    examples = new HashSet<>();
  }

  /**
   * Sets an optional description for the matcher. This usually contains the name, product, purpose
   * or similar categorization of the pattern being matched.
   *
   * @param description The description to set. May be {@code null}.
   * @return A reference to this matcher to allow for method chaining.
   */
  public RecogMatcher setDescription(String description) {
    this.description = description;
    return this;
  }

  /**
   * Returns the description of the matcher.
   *
   * @return The description of the matcher. May be {@code null}.
   */
  public String getDescription() {
    return description;
  }

  /**
   * Adds an example to this matcher.
   *
   * @param example The example to match. May be {@code null}, but will be ignored if the value is
   *        {@code null}.
   * @return A reference to this matcher to allow for method chaining.
   */
  public RecogMatcher addExample(String example) {
    if (example != null)
      examples.add(example);

    return this;
  }

  /**
   * Returns all examples for the matcher. These examples can be used to test the pattern, or
   * document examples for the patterns matching usage.
   *
   * @return A non-null, immutable {@link Set} of examples. May be empty.
   */
  public Set<String> getExamples() {
    return examples == null ? emptySet() : unmodifiableSet(examples);
  }

  /**
   * Returns whether this matcher matches the specified input fingerprint value. If this method
   * returns {@code true} {@link #matches(String)} is guaranteed to return a non-{@code null}
   * result.
   *
   * @param input The fingerprint to test this matcher against. May be {@code null}.
   * @return {@code true} if the input is non-{@code null} and matches the fingerprint matcher
   *         pattern.
   */
  public boolean matches(String input) {
    if (input == null)
      return false;
    else
      return pattern.matcher(input).find();
  }

  /**
   * Returns the matched parameter names and values for the fingerprint input. If the matcher
   * matches, all values, matching positional groups, and matching named groups will be returned in
   * the result. The result is a {@link Map} of parameter name to parameter value.
   * If this returns a non-{@code null} value, the {@link #matches(String)} is guaranted to return
   * {@code true}.
   *
   * @param input The fingerprint to match. May be {@code null}.
   * @return {@code null} if the input does not match the pattern, otherwise a non-{@code null}
   *         {@link Map} of parameter name and values (may be empty).
   */
  public Map<String, String> match(String input) {
    if (input == null)
      return null;

    Matcher matcher = pattern.matcher(input);
    if (matcher.find()) {
      Map<String, String> values = new HashMap<>();
      values.putAll(this.values);

      // parse positional parameters for the groups specified
      for (Entry<String, Integer> parameter : positionalParameters.entrySet())
        if (parameter.getValue() <= matcher.groupCount())
          values.put(parameter.getKey(), matcher.group(parameter.getValue()));

      for (String parameter : namedParameters) {
        try {
          values.put(parameter, matcher.group(parameter));
        } catch (IllegalArgumentException exception) {
          // the group with the name doesn't exist, ignore it
        }
      }

      return values;
    } else
      return null;
  }

  /**
   * Adds a constant parameter value with the given name. If the matcher matches, this key-value
   * pair is guaranteed to be returned in the result of {@link #match(String)}.
   *
   * @param name The name of the value to add to the match. Must not be {@code null}.
   * @param value The value to add. May be {@code null}.
   * @return A reference to this object, for method chaining.
   */
  public RecogMatcher addValue(String name, String value) {
    values.put(requireNonNull(name), value);
    return this;
  }

  /**
   * Adds a parameter to the matcher that will capture the positional group with the specified
   * index. If the group with the specified index is not declared in the regular expression, this
   * parameter will be ignored.
   *
   * @param group The group index, one-based, of the group to match the parameter for. Must be
   *        greater than zero.
   * @param name The name of the parameter to output the match result to. Must not be {@code null}.
   * @return A reference to this object, for method chaining.
   */
  public RecogMatcher addParam(int group, String name) {
    // groups are 1-based so they must be positive integers
    if (group <= 0)
      throw new IllegalArgumentException("The value '" + group + "' must be a positive integer.");
    
    positionalParameters.put(requireNonNull(name), group);
    return this;
  }

  /**
   * Adds a parameter to the matcher that will capture the named grouped with the specified name. If
   * the group with the specified name is not declared in the regular expression, this parameter
   * will be ignored. The name of the parameter is the key returned in {@link #match(String)}
   * invocation.
   *
   * @param name The name of the matching group to match as a parameter. Must not be {@code null}.
   * @return A reference to this object, for method chaining.
   */
  public RecogMatcher addParam(String name) {
    namedParameters.add(requireNonNull(name));
    return this;
  }

  public String getPattern() {
    return pattern.pattern();
  }

  /**
   * Utility method to build a {@link Pattern} with compilation flags that is suitable for use in
   * invocation of the constructor {@link #RecogMatcher(Pattern)}.
   *
   * @param regex The regular expression. Must not be {@code null}.
   * @param flags The pattern flags (see {@link Pattern}). May be {@code null} or empty.
   * @return A {@link Pattern} with the compiled flags provided. Will not be {@code null}.
   */
  public static Pattern pattern(String regex, int... flags) {
    int patternFlags = 0;
    for (int flag : flags)
      patternFlags |= flag;

    return Pattern.compile(regex, patternFlags);
  }

  @Override
  public String toString() {
    return toStringHelper(this)
        .add("Pattern", pattern.pattern())
        .add("Description", description)
        .add("Flags", pattern.flags())
        .add("Positional Parameters", positionalParameters)
        .add("Named Parameters", namedParameters)
        .add("Values", values)
        .add("Examples", examples)
        .toString();
  }

  @Override
  public int hashCode() {
    return Objects.hash(pattern, values, positionalParameters);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this)
      return true;
    else if (!(obj instanceof RecogMatcher))
      return false;
    else {
      RecogMatcher other = (RecogMatcher) obj;
      return Objects.equals(pattern.flags(), other.pattern.flags())
          && Objects.equals(pattern.pattern(), other.pattern.pattern())
          && Objects.equals(values, other.values)
          && Objects.equals(positionalParameters, other.positionalParameters)
          && Objects.equals(namedParameters, other.namedParameters);
    }
  }
}
