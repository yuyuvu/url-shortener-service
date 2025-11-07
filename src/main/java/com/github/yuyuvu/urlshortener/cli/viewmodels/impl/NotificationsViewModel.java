package com.github.yuyuvu.urlshortener.cli.viewmodels.impl;

import com.github.yuyuvu.urlshortener.cli.viewmodels.ViewModel;
import com.github.yuyuvu.urlshortener.domain.model.Notification;
import java.util.List;

public class NotificationsViewModel implements ViewModel {
  public List<Notification> notifications;

  public NotificationsViewModel(List<Notification> notifications) {
    this.notifications = notifications;
  }
}
