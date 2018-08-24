package com.rapid7.recog.parser;

/**
 * Thrown when an error parsing recog input occurs.
 */
public class ParseException extends Exception {

  public ParseException(String message) {
    super(message);
  }

  public ParseException(String message, Throwable cause) {
    super(message, cause);
  }
}
