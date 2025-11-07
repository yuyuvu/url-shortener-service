package com.github.yuyuvu.urlshortener.exceptions;

public class InvalidShortLinkException extends Exception {
  public InvalidShortLinkException(String message) {
    super(message);
  }
}
