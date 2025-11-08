package com.github.yuyuvu.urlshortener.cli.viewmodels.impl;

import com.github.yuyuvu.urlshortener.cli.viewmodels.ViewModel;

/**
 * Реализация ViewModel, содержащая нужные данные для визуального представления успешного выполнения
 * некоторой команды.
 */
public class SuccessViewModel implements ViewModel {
  public final String message;

  /**
   * Конструктор реализации ViewModel, содержащей нужные данные для визуального представления
   * успешного выполнения некоторой команды.
   */
  public SuccessViewModel(String message) {
    this.message = message;
  }
}
