package com.github.yuyuvu.urlshortener.domain.repository;

import com.github.yuyuvu.urlshortener.domain.model.Notification;
import java.util.List;
import java.util.UUID;

public interface NotificationRepository {
  List<Notification> getRepositoryAsList();

  Notification saveNotification(Notification notification);

  List<Notification> getUnreadNotificationsByUserUUID(UUID userUUID);

  void markUnreadNotificationsAsRead(List<Notification> notifications);

  boolean deleteNotification(Notification notification);
}
