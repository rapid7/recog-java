package com.rapid7.recog.verify;

public class VerifierOptions {
  private boolean color;
  private boolean detail;
  private boolean quiet;
  private boolean warnings;

  public VerifierOptions() {
    color = false;
    detail = false;
    quiet = false;
    warnings = true;
  }

  public boolean isColor() {
    return color;
  }

  public void setColor(boolean color) {
    this.color = color;
  }

  public boolean isDetail() {
    return detail;
  }

  public void setDetail(boolean detail) {
    this.detail = detail;
  }

  public boolean isQuiet() {
    return quiet;
  }

  public void setQuiet(boolean quiet) {
    this.quiet = quiet;
  }

  public boolean isWarnings() {
    return warnings;
  }

  public void setWarnings(boolean warnings) {
    this.warnings = warnings;
  }
}
