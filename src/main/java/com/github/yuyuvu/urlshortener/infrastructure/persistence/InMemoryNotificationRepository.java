package com.github.yuyuvu.urlshortener.infrastructure.persistence;

import com.github.yuyuvu.urlshortener.domain.model.Notification;
import com.github.yuyuvu.urlshortener.domain.repository.NotificationRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class InMemoryNotificationRepository implements NotificationRepository {
  List<Notification> unreadNotifications = new ArrayList<>();

  public InMemoryNotificationRepository(List<Notification> unreadNotifications) {
    this.unreadNotifications.addAll(unreadNotifications);
  }

  @Override
  public List<Notification> getRepositoryAsList() {
    return unreadNotifications;
  }

  @Override
  public Notification saveNotification(Notification notification) {
    unreadNotifications.add(notification);
    return notification;
  }

  @Override
  public List<Notification> getUnreadNotificationsByUserUUID(UUID userUUID) {
    return unreadNotifications.stream()
        .filter(notification -> notification.getUserUUID().equals(userUUID))
        .filter(notification -> !notification.isRead()).toList();
  }

  @Override
  public void markUnreadNotificationsAsRead(List<Notification> notifications) {
    for (Notification notification : notifications) {
      notification.setRead(true);
    }
  }

  @Override
  public boolean deleteNotification(Notification notification) {
    return unreadNotifications.remove(notification);
  }
}
