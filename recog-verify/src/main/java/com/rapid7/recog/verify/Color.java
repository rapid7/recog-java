package com.rapid7.recog.verify;

// ANSI color escape codes
enum Color {
  Reset(0),
  Red(31),
  Yellow(33),
  Green(32),
  White(15);

  private final int code;

  Color(int code) {
    this.code = code;
  }

  public int getCode() {
    return code;
  }
}
