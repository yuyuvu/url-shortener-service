package com.github.yuyuvu.urlshortener.cli.viewmodels.impl;

import com.github.yuyuvu.urlshortener.cli.viewmodels.ViewModel;
import com.github.yuyuvu.urlshortener.domain.model.ShortLink;
import java.util.List;

/**
 * Реализация ViewModel, содержащая нужные данные для визуального представления результата обращения
 * к сервису для получения статистики использования всех или отдельных ссылок некоторого
 * пользователя.
 */
public class StatsViewModel implements ViewModel {
  public final List<ShortLink> shortLinks;
  public final boolean isSingle;

  /**
   * Конструктор реализации ViewModel, содержащей нужные данные для визуального представления
   * результата обращения к сервису для получения статистики использования всех или отдельных ссылок
   * некоторого пользователя.
   */
  public StatsViewModel(List<ShortLink> shortLinks, boolean isSingle) {
    this.shortLinks = shortLinks;
    this.isSingle = isSingle;
  }
}
