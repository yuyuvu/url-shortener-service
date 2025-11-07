package com.github.yuyuvu.urlshortener.application;

import com.github.yuyuvu.urlshortener.domain.model.Notification;
import com.github.yuyuvu.urlshortener.domain.model.ShortLink;
import com.github.yuyuvu.urlshortener.domain.repository.NotificationRepository;
import com.github.yuyuvu.urlshortener.infrastructure.config.ConfigManager;
import java.util.List;
import java.util.UUID;

public class NotificationService {
  NotificationRepository notificationRepository;
  ConfigManager configManager;

  public NotificationService(
      NotificationRepository notificationRepository, ConfigManager configManager) {
    this.notificationRepository = notificationRepository;
    this.configManager = configManager;
  }

  public Notification makeNewShortLinkExpiredNotification(ShortLink shortLink) {
    return new Notification(
        shortLink, shortLink.getOwnerOfShortURL(), Notification.NotificationType.EXPIRED);
  }

  public Notification makeNewShortLinkLimitReachedNotification(ShortLink shortLink) {
    return new Notification(
        shortLink, shortLink.getOwnerOfShortURL(), Notification.NotificationType.LIMIT_REACHED);
  }

  public void saveNewNotification(Notification notification) {
    notificationRepository.saveNotification(notification);
  }

  public List<Notification> getUnreadNotificationsByUUID(UUID userUUID) {
    return notificationRepository.getUnreadNotificationsByUserUUID(userUUID);
  }

  public void markUnreadNotificationsAsRead(List<Notification> notifications) {
    notificationRepository.markUnreadNotificationsAsRead(notifications);
  }

  public List<Notification> listAllNotifications() {
    return notificationRepository.getRepositoryAsList();
  }

  public boolean deleteNotification(Notification notification) {
    return notificationRepository.deleteNotification(notification);
  }
}
