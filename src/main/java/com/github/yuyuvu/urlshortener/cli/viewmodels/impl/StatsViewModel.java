package com.github.yuyuvu.urlshortener.cli.viewmodels.impl;

import com.github.yuyuvu.urlshortener.cli.viewmodels.ViewModel;
import com.github.yuyuvu.urlshortener.domain.model.ShortLink;
import java.util.List;

public class StatsViewModel implements ViewModel {
  public List<ShortLink> shortLinks;
  public boolean isSingle;

  public StatsViewModel(List<ShortLink> shortLinks, boolean isSingle) {
    this.shortLinks = shortLinks;
    this.isSingle = isSingle;
  }
}
