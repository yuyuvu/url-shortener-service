package com.github.yuyuvu.urlshortener.cli.viewmodels.impl;

import com.github.yuyuvu.urlshortener.cli.viewmodels.ViewModel;
import com.github.yuyuvu.urlshortener.domain.model.ShortLink;
import java.util.UUID;

public class CreatedLinkViewModel implements ViewModel {
  public final UUID creatorUUID;
  public final String shortURL;
  public final boolean isNewUser;
  public final String originalURL;

  public CreatedLinkViewModel(UUID creatorUUID, String shortURL, boolean isNewUser, String originalURL) {
    this.creatorUUID = creatorUUID;
    this.shortURL = shortURL;
    this.isNewUser = isNewUser;
    this.originalURL = originalURL;
  }
}
