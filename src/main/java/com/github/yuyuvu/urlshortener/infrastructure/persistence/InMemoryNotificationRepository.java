package com.github.yuyuvu.urlshortener.infrastructure.persistence;

import com.github.yuyuvu.urlshortener.domain.model.Notification;
import com.github.yuyuvu.urlshortener.domain.repository.NotificationRepository;
import java.util.ArrayList;
import java.util.List;

public class InMemoryNotificationRepository implements NotificationRepository {
  List<Notification> unreadNotifications = new ArrayList<>();

  public InMemoryNotificationRepository(List<Notification> unreadNotifications) {
    this.unreadNotifications.addAll(unreadNotifications);
  }

  @Override
  public List<Notification> getRepositoryAsList() {
    return List.of();
  }
}
