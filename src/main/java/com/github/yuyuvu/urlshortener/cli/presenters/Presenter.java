package com.github.yuyuvu.urlshortener.cli.presenters;

import com.github.yuyuvu.urlshortener.cli.viewmodels.ViewModel;

public interface Presenter {
  void present(ViewModel result);
  void sendMessage(String message);
}
