package com.github.yuyuvu.urlshortener.domain.repository;

import com.github.yuyuvu.urlshortener.domain.model.Notification;
import java.util.List;
import java.util.UUID;

/** Интерфейс хранилища уведомлений. */
public interface NotificationRepository {
  /**
   * Метод для получения всех уведомления списком, используется для сохранения данных во внешнее
   * постоянное хранилище (например, базу данных или файл) и непосредственно для поиска уже
   * отправленных уведомлений и их удаления.
   */
  List<Notification> getRepositoryAsList();

  /** Метод для сохранения нового уведомления в хранилище. */
  @SuppressWarnings("UnusedReturnValue")
  Notification saveNotification(Notification notification);

  /**
   * Метод для получения всех непрочитанных уведомлений перед их отправкой текущему пользователю.
   */
  List<Notification> getUnreadNotificationsByUserUUID(UUID userUUID);

  /** Метод для установления отметки, что непрочитанные уведомления были прочитаны. */
  void markUnreadNotificationsAsRead(List<Notification> notifications);

  /** Метод для удаления прочитанных уведомлений. */
  boolean deleteNotification(Notification notification);
}
