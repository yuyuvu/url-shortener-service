package com.github.yuyuvu.urlshortener.cli.viewmodels.impl;

import com.github.yuyuvu.urlshortener.cli.viewmodels.ViewModel;

public class ErrorViewModel implements ViewModel {
  public final String errorMessage;

  public ErrorViewModel(String errorMessage) {
    this.errorMessage = errorMessage;
  }
}
