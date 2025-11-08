package com.github.yuyuvu.urlshortener.cli.presenters;

import com.github.yuyuvu.urlshortener.cli.viewmodels.ViewModel;

/**
 * Интерфейс Presenter представляет объекты, которые могут выводить содержимое ViewModel куда-либо
 * (в консоль, лог, файл, отправлять в другой сервис и т.д.).
 */
public interface Presenter {

  /**
   * Метод present выявляет тип полученного ViewModel и в зависимости от типа направляет поток
   * выполнения на нужный метод вывода содержимого ViewModel.
   */
  void present(ViewModel result);

  /** Метод для отправки отдельных служебных сообщений сервиса пользователю. */
  void sendMessage(String message);
}
