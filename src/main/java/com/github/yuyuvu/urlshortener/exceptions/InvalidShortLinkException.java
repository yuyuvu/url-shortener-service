package com.github.yuyuvu.urlshortener.exceptions;

/**
 * Ошибка выбрасывается там, где ожидается активная короткая ссылка сервиса, а передано иное значение.
 * */
public class InvalidShortLinkException extends Exception {
  public InvalidShortLinkException(String message) {
    super(message);
  }
}
