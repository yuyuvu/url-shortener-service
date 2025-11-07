package com.github.yuyuvu.urlshortener.exceptions;

public class InvalidOriginalLinkException extends Exception {
  public InvalidOriginalLinkException(String message) {
    super(message);
  }
}
