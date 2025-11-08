package com.github.yuyuvu.urlshortener.infrastructure.persistence;

import com.github.yuyuvu.urlshortener.domain.model.Notification;
import com.github.yuyuvu.urlshortener.domain.repository.NotificationRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Реализация NotificationRepository на ArrayList для хранения уведомлений внутри оперативной
 * памяти.
 */
public class InMemoryNotificationRepository implements NotificationRepository {

  private final List<Notification> unreadNotifications = new ArrayList<>();

  /** Конструктор для загрузки всех данных после перезапуска из StorageState. */
  public InMemoryNotificationRepository(List<Notification> unreadNotifications) {
    this.unreadNotifications.addAll(unreadNotifications);
  }

  /**
   * Метод для получения всех уведомления списком, используется для сохранения данных в файл и
   * непосредственно для поиска уже отправленных уведомлений и их удаления.
   */
  @Override
  public List<Notification> getRepositoryAsList() {
    return new ArrayList<>(unreadNotifications);
  }

  /** Метод для сохранения нового уведомления в хранилище. */
  @Override
  public Notification saveNotification(Notification notification) {
    unreadNotifications.add(notification);
    return notification;
  }

  /**
   * Метод для получения всех непрочитанных уведомлений перед их отправкой текущему пользователю.
   */
  @Override
  public List<Notification> getUnreadNotificationsByUserUUID(UUID userUUID) {
    return unreadNotifications.stream()
        .filter(notification -> notification.getUserUUID().equals(userUUID))
        .filter(notification -> !notification.isRead())
        .toList();
  }

  /** Метод для установления отметки, что непрочитанные уведомления были прочитаны. */
  @Override
  public void markUnreadNotificationsAsRead(List<Notification> notifications) {
    for (Notification notification : notifications) {
      notification.setRead(true);
    }
  }

  /** Метод для удаления прочитанных уведомлений. */
  @Override
  public boolean deleteNotification(Notification notification) {
    return unreadNotifications.remove(notification);
  }
}
