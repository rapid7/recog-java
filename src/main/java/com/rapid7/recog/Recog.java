package com.rapid7.recog;

import java.util.List;

public interface Recog {

  public List<RecogMatchResult> fingerprint(String input);

  public RecogVersion refreshContent();
}
