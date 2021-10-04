package com.rapid7.recog;

import java.util.Map;

// Represents a fingerprint example and associated data.
public class FingerprintExample {
  private final String text;
  private final Map<String, String> attributeMap;

  public FingerprintExample(String text, Map<String, String> attributeMap) {
    this.text = text;
    this.attributeMap = attributeMap;
  }

  public String getText() {
    return text;
  }

  public Map<String, String> getAttributeMap() {
    return attributeMap;
  }
}
