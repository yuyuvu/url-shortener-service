package com.github.yuyuvu.urlshortener.application;

import com.github.yuyuvu.urlshortener.domain.repository.NotificationRepository;
import com.github.yuyuvu.urlshortener.infrastructure.config.ConfigManager;

public class NotificationService {
  NotificationRepository notificationRepository;
  ConfigManager configManager;

  public NotificationService(
      NotificationRepository notificationRepository, ConfigManager configManager) {
    this.notificationRepository = notificationRepository;
    this.configManager = configManager;
  }
}
