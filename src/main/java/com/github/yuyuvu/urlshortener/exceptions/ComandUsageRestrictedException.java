package com.github.yuyuvu.urlshortener.exceptions;

public class ComandUsageRestrictedException extends Exception {
  public ComandUsageRestrictedException(String message) {
    super(message);
  }
}
