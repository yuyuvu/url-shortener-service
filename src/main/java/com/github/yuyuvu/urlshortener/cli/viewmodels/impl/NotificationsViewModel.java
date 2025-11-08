package com.github.yuyuvu.urlshortener.cli.viewmodels.impl;

import com.github.yuyuvu.urlshortener.cli.viewmodels.ViewModel;
import com.github.yuyuvu.urlshortener.domain.model.Notification;
import java.util.List;

/**
 * Реализация ViewModel, содержащая нужные данные для визуального представления
 * результата обращения к сервису за уведомлениями, сформированными в сервисе для
 * некоторого пользователя
 */
public class NotificationsViewModel implements ViewModel {
  public List<Notification> notifications;

  public NotificationsViewModel(List<Notification> notifications) {
    this.notifications = notifications;
  }
}
