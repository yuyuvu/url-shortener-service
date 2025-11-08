package com.github.yuyuvu.urlshortener.exceptions;

/**
 * Ошибка выбрасывается в том случае, если в файле конфигурации было задано некорректное значение
 * для какой-либо настройки.
 */
public class InvalidConfigPropertyException extends Exception {

  /**
   * Ошибка выбрасывается в том случае, если в файле конфигурации было задано некорректное значение
   * для какой-либо настройки.
   */
  public InvalidConfigPropertyException(String message) {
    super(message);
  }
}
