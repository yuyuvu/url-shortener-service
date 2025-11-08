package com.github.yuyuvu.urlshortener.application;

import com.github.yuyuvu.urlshortener.domain.model.Notification;
import com.github.yuyuvu.urlshortener.domain.model.ShortLink;
import com.github.yuyuvu.urlshortener.domain.repository.NotificationRepository;
import java.util.List;
import java.util.UUID;

/**
 * Сервис для совершения различных операций с уведомлениями: создания, сохранения, получения,
 * удаления.
 */
public class NotificationService {
  private final NotificationRepository notificationRepository;

  /** Сервис зависит от NotificationRepository. */
  public NotificationService(NotificationRepository notificationRepository) {
    this.notificationRepository = notificationRepository;
  }

  /** Метод для создания уведомления об истечении срока действия ссылки. */
  public Notification makeNewShortLinkExpiredNotification(ShortLink shortLink) {
    return new Notification(
        shortLink, shortLink.getOwnerOfShortURL(), Notification.NotificationType.EXPIRED, false);
  }

  /** Метод для создания уведомления об израсходовании лимита использований ссылки. */
  public Notification makeNewShortLinkLimitReachedNotification(ShortLink shortLink) {
    return new Notification(
        shortLink,
        shortLink.getOwnerOfShortURL(),
        Notification.NotificationType.LIMIT_REACHED,
        false);
  }

  /** Метод для сохранения нового уведомления любого типа в репозиторий. */
  public void saveNewNotification(Notification notification) {
    notificationRepository.saveNotification(notification);
  }

  /**
   * Метод для получения всех непрочитанных уведомлений перед их отправкой текущему пользователю.
   */
  public List<Notification> getUnreadNotificationsByUUID(UUID userUUID) {
    return notificationRepository.getUnreadNotificationsByUserUUID(userUUID);
  }

  /** Метод для установления отметки, что непрочитанные уведомления были прочитаны. */
  public void markUnreadNotificationsAsRead(List<Notification> notifications) {
    notificationRepository.markUnreadNotificationsAsRead(notifications);
  }

  /**
   * Метод для получения всех уведомления списком, используется для поиска уже отправленных
   * уведомлений и их удаления.
   */
  public List<Notification> listAllNotifications() {
    return notificationRepository.getRepositoryAsList();
  }

  /** Метод для удаления прочитанных уведомлений. */
  @SuppressWarnings("UnusedReturnValue")
  public boolean deleteNotification(Notification notification) {
    return notificationRepository.deleteNotification(notification);
  }
}
