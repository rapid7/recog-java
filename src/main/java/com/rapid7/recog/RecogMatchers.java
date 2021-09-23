package com.rapid7.recog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

/**
 * Manages a group of related {@link RecogMatcher} instances that can be matched together against a
 * single input. This object typically corresponds to one file of XML input from recog.
 */
public class RecogMatchers extends ArrayList<RecogMatcher> {

  private String key;
  private String protocol;
  private String type;
  private float preference;

  public RecogMatchers() {
    // no name
  }

  public RecogMatchers(String key, String protocol, String type, float preference) {
    this.key = requireNonNull(key);
    this.protocol = protocol;
    this.type = type;
    this.preference = preference;
  }

  public String getKey() {
    return key;
  }

  public String getProtocol() {
    return protocol;
  }

  public String getType() {
    return type;
  }

  public float getPreference() {
    return preference;
  }

  /**
   * Finds matches for a string input against all matchers.
   *
   * @param input Input to check against a set of recog fingerprints. May be {@code null}.
   * @return List of {@link RecogMatch}es containing matches parameters for any matches.
   */
  public List<RecogMatch> getMatches(String input) {
    if (input == null)
      return Collections.emptyList();
    else
      return stream().map(matcher -> {
        Map<String,String> match = matcher.match(input);
        return match != null ? new RecogMatch(matcher, match) : null;
      }).filter(Objects::nonNull).collect(toList());
  }

  /**
   * Finds the first match for a string input. As soon as the match is discovered,
   * this method will return. If no match is discovered, this method will return {@code null}
   *
   * @param input Input to check against. Must not be {@code null}
   * @return A match, or {@code null} if none is found.
   */
  public RecogMatch getFirstMatch(String input) {
    requireNonNull(input);

    for (RecogMatcher matcher : this) {
      Map<String, String> match = matcher.match(input);
      if (match != null)
        return new RecogMatch(matcher, match);
    }

    return null;
  }
}
