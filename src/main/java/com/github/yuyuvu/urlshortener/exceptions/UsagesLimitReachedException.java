package com.github.yuyuvu.urlshortener.exceptions;

/**
 * Ошибка выбрасывается при попытке увеличить счётчик использований для короткой ссылки,
 * у которой лимит использований уже исчерпан.
 * */
public class UsagesLimitReachedException extends Exception {
  public UsagesLimitReachedException(String message) {
    super(message);
  }
}
