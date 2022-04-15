package com.rapid7.recog.parser;

import org.xml.sax.SAXException;

/**
 * Thrown when an error parsing recog input occurs.
 */
public class ParseException extends SAXException {

  public ParseException(String message) {
    super(message);
  }

  public ParseException(String message, Exception cause) {
    super(message, cause);
  }
}
