package com.rapid7.recog.verify;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

public class Formatter {

  private final VerifierOptions options;
  private final PrintWriter writer;

  public Formatter(VerifierOptions options, java.io.OutputStream output) {
    this.options = options;
    this.writer = new PrintWriter(new OutputStreamWriter(output, StandardCharsets.UTF_8), true);
  }

  public void statusMessage(String text) {
    writer.println(color(text, Color.White));
  }

  public void successMessage(String text) {
    writer.println(color(text, Color.Green));
  }

  public void warningMessage(String text) {
    writer.println(color(text, Color.Yellow));
  }

  public void failureMessage(String text) {
    writer.println(color(text, Color.Red));
  }

  private String color(String text, Color color) {
    return options.isColor() ? colorize(text, color) : text;
  }

  private String colorize(String text, Color color) {
    return String.format("\u001B[%dm%s\u001B[%dm", color.getCode(), text, Color.Reset.getCode());
  }
}
