package com.github.yuyuvu.urlshortener.cli.viewmodels.impl;

import com.github.yuyuvu.urlshortener.cli.viewmodels.ViewModel;
import com.github.yuyuvu.urlshortener.domain.model.ShortLink;
import java.util.List;

/**
 * Реализация ViewModel, содержащая нужные данные для визуального представления результата обращения
 * к сервису для получения всех ссылок некоторого пользователя.
 */
public class ListViewModel implements ViewModel {
  public final List<ShortLink> shortLinks;

  /**
   * Конструктор реализации ViewModel, содержащей нужные данные для визуального представления
   * результата обращения к сервису для получения всех ссылок некоторого пользователя.
   */
  public ListViewModel(List<ShortLink> shortLinks) {
    this.shortLinks = shortLinks;
  }
}
