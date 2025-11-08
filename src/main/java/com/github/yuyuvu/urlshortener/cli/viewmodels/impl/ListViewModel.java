package com.github.yuyuvu.urlshortener.cli.viewmodels.impl;

import com.github.yuyuvu.urlshortener.cli.viewmodels.ViewModel;
import com.github.yuyuvu.urlshortener.domain.model.ShortLink;
import java.util.List;

/**
 * Реализация ViewModel, содержащая нужные данные для визуального представления
 * результата обращения к сервису для получения всех ссылок некоторого пользователя
 */
public class ListViewModel implements ViewModel {
  public List<ShortLink> shortLinks;

  public ListViewModel(List<ShortLink> shortLinks) {
    this.shortLinks = shortLinks;
  }
}
