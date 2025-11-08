package com.github.yuyuvu.urlshortener.exceptions;

/**
 * Ошибка чтения или записи всех данных сервиса из внешнего постоянного хранилища при его
 * перезапуске или выключении.
 */
public class StorageStatePersistenceException extends Exception {

  /**
   * Ошибка чтения или записи всех данных сервиса из внешнего постоянного хранилища при его
   * перезапуске или выключении. Должна содержать сообщение.
   */
  public StorageStatePersistenceException(String message) {
    super(message);
  }
}
