package com.github.yuyuvu.urlshortener.cli.viewmodels.impl;

import com.github.yuyuvu.urlshortener.cli.viewmodels.ViewModel;
import java.util.UUID;

/**
 * Реализация ViewModel, содержащая нужные данные для визуального представления результата успешного
 * создания новой короткой ссылки.
 */
public class CreatedLinkViewModel implements ViewModel {
  public final UUID creatorUUID;
  public final String shortURL;
  public final boolean isNewUser;
  public final String originalURL;

  /**
   * Конструктор реализации ViewModel, содержащей нужные данные для визуального представления
   * результата успешного создания новой короткой ссылки.
   */
  public CreatedLinkViewModel(
      UUID creatorUUID, String shortURL, boolean isNewUser, String originalURL) {
    this.creatorUUID = creatorUUID;
    this.shortURL = shortURL;
    this.isNewUser = isNewUser;
    this.originalURL = originalURL;
  }
}
