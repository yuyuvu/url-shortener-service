package com.github.yuyuvu.urlshortener.exceptions;

/**
 * Ошибка выбрасывается там, где ожидается синтаксически корректный URL с правильными протоколами, а
 * передано иное значение.
 */
public class InvalidOriginalLinkException extends Exception {
  /**
   * Ошибка выбрасывается там, где ожидается синтаксически корректный URL с правильными протоколами,
   * а передано иное значение. Должна содержать сообщение.
   */
  public InvalidOriginalLinkException(String message) {
    super(message);
  }
}
