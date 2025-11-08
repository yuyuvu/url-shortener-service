package com.github.yuyuvu.urlshortener.exceptions;

/**
 * Ошибка выбрасывается там, где команда сервиса вызывается с недопустимыми, запрещёнными параметрами,
 * которые противоречат настройкам или логике.
 * */
public class IllegalCommandParameterException extends Exception {
  public IllegalCommandParameterException(String message) {
    super(message);
  }
}
