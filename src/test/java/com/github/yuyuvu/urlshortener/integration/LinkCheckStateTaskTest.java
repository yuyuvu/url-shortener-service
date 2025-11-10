package com.github.yuyuvu.urlshortener.integration;

import static com.github.yuyuvu.urlshortener.cli.presenters.ColorPrinter.deleteColorsFromString;
import static org.mockito.Mockito.when;

import com.github.yuyuvu.urlshortener.application.LinkService;
import com.github.yuyuvu.urlshortener.application.NotificationService;
import com.github.yuyuvu.urlshortener.application.UserService;
import com.github.yuyuvu.urlshortener.cli.ConsoleController;
import com.github.yuyuvu.urlshortener.domain.model.Notification;
import com.github.yuyuvu.urlshortener.domain.model.ShortLink;
import com.github.yuyuvu.urlshortener.domain.model.User;
import com.github.yuyuvu.urlshortener.domain.repository.NotificationRepository;
import com.github.yuyuvu.urlshortener.domain.repository.ShortLinkRepository;
import com.github.yuyuvu.urlshortener.domain.repository.UserRepository;
import com.github.yuyuvu.urlshortener.exceptions.InvalidOriginalLinkException;
import com.github.yuyuvu.urlshortener.exceptions.OriginalLinkNotFoundException;
import com.github.yuyuvu.urlshortener.exceptions.UsagesLimitReachedException;
import com.github.yuyuvu.urlshortener.infrastructure.config.ConfigManager;
import com.github.yuyuvu.urlshortener.infrastructure.persistence.InMemoryNotificationRepository;
import com.github.yuyuvu.urlshortener.infrastructure.persistence.InMemoryShortLinkRepository;
import com.github.yuyuvu.urlshortener.infrastructure.persistence.InMemoryUserRepository;
import com.github.yuyuvu.urlshortener.infrastructure.scheduler.LinkCheckStateTask;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Класс для тестов задания, которое периодически выполняется в параллельном режиме и включает в
 * себя: проверку истечения срока действия коротких ссылок и их удаление, отправку уведомлений, если
 * был израсходован лимит использований или срок действия ссылки истёк, а также очищение списка уже
 * прочитанных уведомлений.
 */
@ExtendWith(MockitoExtension.class)
public class LinkCheckStateTaskTest {
  private UserRepository userRepository;
  private ShortLinkRepository shortLinkRepository;
  private NotificationRepository notificationRepository;
  private LinkService linkService;
  private UserService userService;
  private NotificationService notificationService;
  private User user1;
  private User user2;
  private ShortLink[] shortLink;

  @Mock private ConfigManager configManager;

  /** Подготавливаем контекст для двух тестов. */
  @BeforeEach
  public void setUpState() throws InvalidOriginalLinkException, UsagesLimitReachedException {
    // Подставляем нужные настройки для создания новых ссылок
    when(configManager.getDefaultServiceBaseURLProperty()).thenReturn("https://yulink.tech/");
    when(configManager.getLegacyServiceBaseURLProperty())
        .thenReturn(new String[] {"https://legacy-url.tech/", "https://veryoldurl.tech/"});
    char[] allowedCharacters = new char[] {'a', 'b', 'c'};
    when(configManager.getShortLinkAllowedCharactersProperty()).thenReturn(allowedCharacters);
    when(configManager.getDefaultShortLinkIdLengthProperty()).thenReturn(10);

    // Срок действия будет равен 24 часа
    when(configManager.getDefaultShortLinkTTLTimeUnitProperty())
        .thenReturn(ConfigManager.TimeUnit.HOURS);
    when(configManager.getDefaultShortLinkTTLInUnitsProperty()).thenReturn(24);
    // Лимит использований будет равен 2
    when(configManager.getDefaultShortLinkUsageLimitProperty()).thenReturn(2);

    // Создаём 2 новых пользователей и сохраняем их в репозиторий
    userRepository = new InMemoryUserRepository(new HashMap<>());
    userService = new UserService(userRepository);

    user1 = userService.makeNewUUIDAndUser();
    userService.saveNewUser(user1);
    user2 = userService.makeNewUUIDAndUser();
    userService.saveNewUser(user2);

    // Создаём 4 новые ссылки (2 для первого пользователя и 2 для второго)
    // и сохраняем их в репозиторий
    shortLinkRepository = new InMemoryShortLinkRepository(new HashMap<>());
    linkService = new LinkService(shortLinkRepository, configManager);

    notificationRepository = new InMemoryNotificationRepository(new ArrayList<>());
    notificationService = new NotificationService(notificationRepository);

    shortLink = new ShortLink[4];
    shortLink[0] =
        linkService.saveNewShortLink(
            linkService.makeNewShortLink("https://github.com", user1.getUUID()));
    shortLink[1] =
        linkService.saveNewShortLink(
            linkService.makeNewShortLink("https://google.com", user1.getUUID()));
    shortLink[2] =
        linkService.saveNewShortLink(
            linkService.makeNewShortLink("https://google.com", user2.getUUID()));
    shortLink[3] =
        linkService.saveNewShortLink(
            linkService.makeNewShortLink("https://google.com", user2.getUUID()));

    // Устанавливаем для первой и третьей коротких ссылок истёкший срок действия,
    // для второй - израсходованный лимит
    shortLink[0].setCreationDateTime(LocalDateTime.of(2025, 1, 1, 11, 11), false);
    shortLink[0].setExpirationDateTime(LocalDateTime.of(2025, 2, 1, 11, 11));

    shortLink[1].incrementUsageCounter();
    shortLink[1].incrementUsageCounter();

    shortLink[2].setCreationDateTime(LocalDateTime.of(2025, 3, 1, 11, 11), false);
    shortLink[2].setExpirationDateTime(LocalDateTime.of(2025, 4, 1, 11, 11));
  }

  /**
   * Проверяем подзадачи удаления ссылок с истёкшим сроком действия, а также создания уведомлений.
   */
  @Test
  void removeExpiredLinksAndMakeNotificationsTest()
      throws InvalidOriginalLinkException, UsagesLimitReachedException {
    // Имитируем разовый запуск scheduler (но без непосредственно отображения уведомлений,
    // допустим, что пользователь уже вышел до устаревания ссылки или израсходования лимита,
    // то есть текущий UUID равен null)
    LinkCheckStateTask linkCheckStateTask =
        new LinkCheckStateTask(linkService, notificationService, userService, () -> {});

    // В репозиториях сейчас 4 ссылки и 0 уведомлений
    Assertions.assertEquals(4, linkService.listAllShortLinks().size());
    Assertions.assertEquals(0, notificationService.listAllNotifications().size());

    // Запускаем задачу
    linkCheckStateTask.run();

    // Проверяем, что 1 и 3 ссылки были удалены из репозитория, а остальные две остались
    Assertions.assertEquals(2, linkService.listAllShortLinks().size());
    Assertions.assertThrows(
        OriginalLinkNotFoundException.class,
        () ->
            linkService.validateShortLinkExistence(
                "https://yulink.tech/" + shortLink[0].getShortId()));
    Assertions.assertThrows(
        OriginalLinkNotFoundException.class,
        () ->
            linkService.validateShortLinkExistence(
                "https://yulink.tech/" + shortLink[2].getShortId()));

    Assertions.assertDoesNotThrow(
        () ->
            linkService.validateShortLinkExistence(
                "https://yulink.tech/" + shortLink[1].getShortId()));
    Assertions.assertDoesNotThrow(
        () ->
            linkService.validateShortLinkExistence(
                "https://yulink.tech/" + shortLink[3].getShortId()));

    // Проверяем, что было создано 3 уведомления - 2 об удалении, 1 об израсходовании лимита
    Assertions.assertEquals(3, notificationService.listAllNotifications().size());
    // Проверяем типы, shortID и UUID уведомлений, а также непрочитанный статус
    Assertions.assertEquals(
        2, notificationService.getUnreadNotificationsByUUID(user1.getUUID()).size());
    Assertions.assertEquals(
        1, notificationService.getUnreadNotificationsByUUID(user2.getUUID()).size());

    for (Notification notification :
        notificationService.getUnreadNotificationsByUUID(user1.getUUID())) {
      Assertions.assertEquals(user1.getUUID(), notification.getUserUUID());
      Assertions.assertFalse(notification.isRead());
      Assertions.assertTrue(
          notification.getShortLink().getShortId().equals(shortLink[0].getShortId())
              || notification.getShortLink().getShortId().equals(shortLink[1].getShortId()));
      Assertions.assertTrue(
          notification.getType().equals(Notification.NotificationType.EXPIRED)
              || notification.getType().equals(Notification.NotificationType.LIMIT_REACHED));
    }

    for (Notification notification :
        notificationService.getUnreadNotificationsByUUID(user2.getUUID())) {
      Assertions.assertEquals(user2.getUUID(), notification.getUserUUID());
      Assertions.assertFalse(notification.isRead());
      Assertions.assertEquals(shortLink[2].getShortId(), notification.getShortLink().getShortId());
      Assertions.assertEquals(Notification.NotificationType.EXPIRED, notification.getType());
    }
  }

  /**
   * Проверяем подзадачи отправки непрочитанных уведомлений в консоль, а затем очистки прочитанных
   * уведомлений.
   */
  @Test
  void sendNotificationsTest() throws InvalidOriginalLinkException, UsagesLimitReachedException {
    // Заменяем System.out
    PrintStream out = System.out;
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    System.setOut(new PrintStream(bos));

    // В репозиториях сейчас 4 ссылки и 0 уведомлений
    Assertions.assertEquals(4, linkService.listAllShortLinks().size());
    Assertions.assertEquals(0, notificationService.listAllNotifications().size());

    // Имитируем разовый запуск scheduler (теперь c отображением уведомлений,
    // UUID-получатель в сети)
    ConsoleController consoleController =
        new ConsoleController(userService, linkService, notificationService, configManager);

    LinkCheckStateTask linkCheckStateTask =
        new LinkCheckStateTask(
            linkService,
            notificationService,
            userService,
            consoleController::sendUnreadNotifications);

    // Идентифицируемся под UUID user1
    consoleController.loginUser(user1.getUUID());

    // Проверяем содержимое ByteArrayOutputStream
    Assertions.assertEquals("", bos.toString());

    // Запускаем задачу
    linkCheckStateTask.run();

    // Проверяем, что было созданные уведомления на этот раз были сразу выведены для пользователя
    // Они помечены как прочитанные и будут удалены в следующем вызове linkCheckStateTask.run()
    Assertions.assertEquals(3, notificationService.listAllNotifications().size());

    // Ключевое отличие - в статусе прочтения, ByteArrayOutputStream также должен был наполниться
    // У user1 все прочитанные
    Assertions.assertTrue(
        notificationService.getUnreadNotificationsByUUID(user1.getUUID()).stream()
            .allMatch(Notification::isRead));
    // У user2 никакие
    Assertions.assertTrue(
        notificationService.getUnreadNotificationsByUUID(user2.getUUID()).stream()
            .noneMatch(Notification::isRead));

    // Снова проверяем содержимое ByteArrayOutputStream
    Assertions.assertTrue(
        deleteColorsFromString(bos.toString())
            .contains("Внимание! У вас есть непрочитанные уведомления:"));
    Assertions.assertTrue(
        deleteColorsFromString(bos.toString()).contains("Срок действия вашей короткой ссылки"));
    Assertions.assertTrue(
        deleteColorsFromString(bos.toString())
            .contains("Лимит использований вашей короткой ссылки"));

    bos.reset();

    // Снова запускаем задачу
    linkCheckStateTask.run();

    // Прочитанные уведомления должны были очиститься
    Assertions.assertEquals(1, notificationService.listAllNotifications().size());
    Assertions.assertEquals(
        0, notificationService.getUnreadNotificationsByUUID(user1.getUUID()).size());
    // Непрочитанные уведомления должны были остаться
    Assertions.assertEquals(
        1, notificationService.getUnreadNotificationsByUUID(user2.getUUID()).size());

    // Проверяем содержимое ByteArrayOutputStream, должен быть снова пустым,
    // так как непрочитанных уведомлений для user1 не было
    Assertions.assertEquals("", bos.toString());
  }
}
