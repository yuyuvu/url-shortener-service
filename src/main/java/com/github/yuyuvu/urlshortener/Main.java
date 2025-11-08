package com.github.yuyuvu.urlshortener;

import com.github.yuyuvu.urlshortener.exceptions.StorageStatePersistenceException;

import java.util.Locale;

/** Класс Main отвечает только за запуск приложения. Делает точку входа видимой сразу. */
public class Main {

  // Установка глобальной локали для сохранения вывода приложения ожидаемым
  static {
    Locale.setDefault(Locale.US);
  }

  /** Метод main отвечает только за запуск приложения. Делает точку входа видимой сразу. */
  public static void main(String[] args){
    try {
      new UrlShortenerApp().start();
    } catch (StorageStatePersistenceException e) {
      System.err.println(e.getMessage());
    }
  }
}
