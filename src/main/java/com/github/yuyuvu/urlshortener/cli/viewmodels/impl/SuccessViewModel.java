package com.github.yuyuvu.urlshortener.cli.viewmodels.impl;

import com.github.yuyuvu.urlshortener.cli.viewmodels.ViewModel;

public class SuccessViewModel implements ViewModel {
  public final String message;

  public SuccessViewModel(String message) {
    this.message = message;
  }
}
