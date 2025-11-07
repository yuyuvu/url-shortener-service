package com.github.yuyuvu.urlshortener.exceptions;

public class UsagesLimitReachedException extends Exception {
  public UsagesLimitReachedException(String message) {
    super(message);
  }
}
