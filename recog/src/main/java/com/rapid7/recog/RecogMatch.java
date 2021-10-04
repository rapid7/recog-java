package com.rapid7.recog;

import java.util.Map;
import static java.util.Objects.requireNonNull;

public class RecogMatch {

  private RecogMatcher matcher;
  private Map<String, String> parameters;

  public RecogMatch(RecogMatcher matcher, Map<String, String> parameters) {
    this.matcher = requireNonNull(matcher);
    this.parameters = requireNonNull(parameters);
  }

  public RecogMatcher getMatcher() {
    return matcher;
  }

  public Map<String, String> getParameters() {
    return parameters;
  }
}
