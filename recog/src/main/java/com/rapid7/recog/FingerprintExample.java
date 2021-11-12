package com.rapid7.recog;

import java.util.Base64;
import java.util.Map;
import static java.util.Objects.requireNonNull;

// Represents a fingerprint example and associated data.
public class FingerprintExample {
  private static final String ENCODING_KEY = "_encoding";

  private final String text;
  private final Map<String, String> attributeMap;

  public FingerprintExample(String text, Map<String, String> attributeMap) {
    String tmpText = requireNonNull(text);
    this.attributeMap = requireNonNull(attributeMap);
    if (attributeMap.containsKey(ENCODING_KEY) && attributeMap.get(ENCODING_KEY).equals("base64")) {
      byte[] exampleContentBytes = Base64.getDecoder().decode(tmpText.replaceAll("\\s+", ""));
      this.text = new String(exampleContentBytes);
    } else {
      this.text = text;
    }
  }

  public String getText() {
    return text;
  }

  public Map<String, String> getAttributeMap() {
    return attributeMap;
  }
}
