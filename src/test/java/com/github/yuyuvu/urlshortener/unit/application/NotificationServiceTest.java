package com.github.yuyuvu.urlshortener.unit.application;

import static org.mockito.Mockito.when;

import com.github.yuyuvu.urlshortener.application.LinkService;
import com.github.yuyuvu.urlshortener.application.NotificationService;
import com.github.yuyuvu.urlshortener.application.UserService;
import com.github.yuyuvu.urlshortener.domain.model.Notification;
import com.github.yuyuvu.urlshortener.domain.model.ShortLink;
import com.github.yuyuvu.urlshortener.domain.repository.NotificationRepository;
import com.github.yuyuvu.urlshortener.domain.repository.ShortLinkRepository;
import com.github.yuyuvu.urlshortener.domain.repository.UserRepository;
import com.github.yuyuvu.urlshortener.exceptions.InvalidOriginalLinkException;
import com.github.yuyuvu.urlshortener.infrastructure.config.ConfigManager;
import com.github.yuyuvu.urlshortener.infrastructure.persistence.InMemoryNotificationRepository;
import com.github.yuyuvu.urlshortener.infrastructure.persistence.InMemoryShortLinkRepository;
import com.github.yuyuvu.urlshortener.infrastructure.persistence.InMemoryUserRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

/** Класс для тестов методов из NotificationService. */
@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {
  private UserRepository userRepository;
  private ShortLinkRepository shortLinkRepository;
  private NotificationRepository notificationRepository;
  private UserService userService;
  private LinkService linkService;
  private NotificationService notificationService;

  @Mock private ConfigManager configManager;

  /** Подготавливаем чисты репозитории и сервисы перед каждым тестом. */
  @BeforeEach
  public void setUpCleanState() {
    userRepository = new InMemoryUserRepository(new HashMap<>());
    userService = new UserService(userRepository);
    shortLinkRepository = new InMemoryShortLinkRepository(new HashMap<>());
    linkService = new LinkService(shortLinkRepository, configManager);
    notificationRepository = new InMemoryNotificationRepository(new ArrayList<>());
    notificationService = new NotificationService(notificationRepository);
    Mockito.reset(configManager);

    // Подставляем нужные настройки
    when(configManager.getDefaultServiceBaseURLProperty()).thenReturn("https://yulink.tech/");
    when(configManager.getLegacyServiceBaseURLProperty())
        .thenReturn(new String[] {"https://legacy-url.tech/", "https://veryoldurl.tech/"});
    when(configManager.getDefaultShortLinkTTLTimeUnitProperty())
        .thenReturn(ConfigManager.TimeUnit.MINUTES);
    when(configManager.getDefaultShortLinkTTLInUnitsProperty()).thenReturn(15);
    when(configManager.getDefaultShortLinkUsageLimitProperty()).thenReturn(5);
    char[] allowedCharacters = new char[] {'a', 'b', 'c'};
    when(configManager.getShortLinkAllowedCharactersProperty()).thenReturn(allowedCharacters);
    when(configManager.getDefaultShortLinkIdLengthProperty()).thenReturn(10);
  }

  /** Проверяем метод для создания уведомления об истечении срока действия ссылки. */
  @Test
  void makeNewShortLinkExpiredNotificationTest() throws InvalidOriginalLinkException {

    // Создаём новую ссылку и сохраняем её в репозиторий
    UUID ownerOfShortLink = UUID.randomUUID();
    final ShortLink[] shortLink = new ShortLink[1];
    shortLink[0] =
        linkService.saveNewShortLink(
            linkService.makeNewShortLink("https://github.com", ownerOfShortLink));

    // Создаём новое уведомление и сохраняем его в репозиторий
    Notification notification =
        notificationService.makeNewShortLinkExpiredNotification(shortLink[0]);
    notificationService.saveNewNotification(notification);

    // Проверяем, что данные об уведомлении, тип, получатель
    // и статус прочтения выставились корректно
    Assertions.assertNotNull(notification);
    Assertions.assertEquals(ownerOfShortLink, notification.getUserUUID());
    Assertions.assertEquals(shortLink[0], notification.getShortLink());
    Assertions.assertEquals(Notification.NotificationType.EXPIRED, notification.getType());
    Assertions.assertFalse(notification.isRead());

    // Проверяем, что уведомление сохранилось в репозитории и его можно получить
    Assertions.assertTrue(notificationService.listAllNotifications().contains(notification));
  }

  /** Проверяем метод для создания уведомления об израсходовании лимита использований ссылки. */
  @Test
  void makeNewShortLinkLimitReachedNotificationTest() throws InvalidOriginalLinkException {

    // Создаём новую ссылку и сохраняем её в репозиторий
    UUID ownerOfShortLink = UUID.randomUUID();
    final ShortLink[] shortLink = new ShortLink[1];
    shortLink[0] =
        linkService.saveNewShortLink(
            linkService.makeNewShortLink("https://github.com", ownerOfShortLink));

    // Создаём новое уведомление и сохраняем его в репозиторий
    Notification notification =
        notificationService.makeNewShortLinkLimitReachedNotification(shortLink[0]);
    notificationService.saveNewNotification(notification);

    // Проверяем, что данные об уведомлении, тип, получатель
    // и статус прочтения выставились корректно
    Assertions.assertNotNull(notification);
    Assertions.assertEquals(ownerOfShortLink, notification.getUserUUID());
    Assertions.assertEquals(shortLink[0], notification.getShortLink());
    Assertions.assertEquals(Notification.NotificationType.LIMIT_REACHED, notification.getType());
    Assertions.assertFalse(notification.isRead());

    // Проверяем, что уведомление сохранилось в репозитории и его можно получить
    Assertions.assertTrue(notificationService.listAllNotifications().contains(notification));
  }

  /**
   * Проверяем метод для получения всех непрочитанных уведомлений перед их отправкой текущему
   * пользователю.
   */
  @Test
  void getUnreadNotificationsByUUIDAndMarkUnreadNotificationsAsReadTest()
      throws InvalidOriginalLinkException {
    // Создаём новую ссылку и сохраняем её в репозиторий
    UUID ownerOfShortLink = UUID.randomUUID();
    final ShortLink[] shortLink = new ShortLink[1];
    shortLink[0] =
        linkService.saveNewShortLink(
            linkService.makeNewShortLink("https://github.com", ownerOfShortLink));

    // Создаём два новых разных уведомления и сохраняем их в репозиторий
    Notification notification1 =
        notificationService.makeNewShortLinkExpiredNotification(shortLink[0]);
    notificationService.saveNewNotification(notification1);
    Notification notification2 =
        notificationService.makeNewShortLinkLimitReachedNotification(shortLink[0]);
    notificationService.saveNewNotification(notification2);

    // Проверяем, что новые уведомления можно получить из списка непрочитанных
    List<Notification> notifications =
        notificationService.getUnreadNotificationsByUUID(ownerOfShortLink);
    Assertions.assertEquals(2, notifications.size());

    UUID secondUser = UUID.randomUUID();
    // Проверяем, что для другого пользователя они же не приходят
    List<Notification> notificationsForAnotherUser =
        notificationService.getUnreadNotificationsByUUID(secondUser);
    Assertions.assertEquals(0, notificationsForAnotherUser.size());

    // Проверка, что уведомления можно пометить как прочитанные
    notificationService.markUnreadNotificationsAsRead(notifications);

    // Проверяем, что теперь ранее добавленные уведомления нельзя получить из списка непрочитанных
    List<Notification> newUnreadNotifications =
        notificationService.getUnreadNotificationsByUUID(ownerOfShortLink);
    Assertions.assertEquals(0, newUnreadNotifications.size());
  }

  /** Проверяем метод для удаления (прочитанных) уведомлений. */
  @Test
  void deleteNotificationTest() throws InvalidOriginalLinkException {
    // Создаём новую ссылку и сохраняем её в репозиторий
    UUID ownerOfShortLink = UUID.randomUUID();
    final ShortLink[] shortLink = new ShortLink[1];
    shortLink[0] =
        linkService.saveNewShortLink(
            linkService.makeNewShortLink("https://github.com", ownerOfShortLink));

    // Создаём два новых разных уведомления и сохраняем их в репозиторий
    Notification notification1 =
        notificationService.makeNewShortLinkExpiredNotification(shortLink[0]);
    notificationService.saveNewNotification(notification1);
    Notification notification2 =
        notificationService.makeNewShortLinkLimitReachedNotification(shortLink[0]);
    notificationService.saveNewNotification(notification2);

    // Проверяем, что новые уведомления можно получить из репозитория
    List<Notification> notifications = notificationService.listAllNotifications();
    Assertions.assertEquals(2, notifications.size());

    // Удаляем уведомления
    notificationService.deleteNotification(notification1);
    Assertions.assertEquals(1, notificationService.listAllNotifications().size());
    notificationService.deleteNotification(notification2);

    // Проверяем, что теперь ранее добавленные уведомления нельзя получить из репозитория
    List<Notification> newUnreadNotifications =
        notificationService.getUnreadNotificationsByUUID(ownerOfShortLink);
    Assertions.assertEquals(0, newUnreadNotifications.size());
  }
}
