package com.rapid7.recog;

import java.util.Map;
import java.util.Set;
import static com.google.common.base.MoreObjects.toStringHelper;
import static java.util.Objects.requireNonNull;

public class RecogMatchResult {

  private String key;
  private String type;
  private String protocol;
  private float preference;
  private String description;
  private String pattern;
  private Set<String> examples;
  private Map<String, String> matches;

  public RecogMatchResult(String key, String type, String protocol, float preference, String description, String pattern, Set<String> examples, Map<String, String> matches) {
    this.key = requireNonNull(key);
    this.type = requireNonNull(type);
    this.protocol = protocol;
    this.preference = preference;
    this.description = requireNonNull(description);
    this.pattern = requireNonNull(pattern);
    this.examples = examples;
    this.matches = requireNonNull(matches);
  }

  public String getKey() {
    return key;
  }

  public String getType() {
    return type;
  }

  public String getProtocol() {
    return protocol;
  }

  public float getPreference() {
    return preference;
  }

  public String getDescription() {
    return description;
  }

  public String getPattern() {
    return pattern;
  }

  public Set<String> getExamples() {
    return examples;
  }

  public Map<String, String> getMatches() {
    return matches;
  }

  @Override
  public String toString() {
    return toStringHelper(this)
      .add("Key", key)
      .add("Type", type)
      .add("Protocol", protocol)
      .add("Preference", preference)
      .add("Description", description)
      .add("Pattern", pattern)
      .add("Examples", examples)
      .add("Matches", matches)
      .toString();
  }
}
