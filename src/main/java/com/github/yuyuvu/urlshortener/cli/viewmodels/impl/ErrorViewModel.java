package com.github.yuyuvu.urlshortener.cli.viewmodels.impl;

import com.github.yuyuvu.urlshortener.cli.viewmodels.ViewModel;

/**
 * Реализация ViewModel, содержащая нужные данные для визуального представления выполнения некоторой
 * команды с ошибкой.
 */
public class ErrorViewModel implements ViewModel {
  public final String errorMessage;

  /**
   * Конструктор реализации ViewModel, содержащая нужные данные для визуального представления
   * выполнения некоторой команды с ошибкой.
   */
  public ErrorViewModel(String errorMessage) {
    this.errorMessage = errorMessage;
  }
}
