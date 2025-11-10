package com.github.yuyuvu.urlshortener.integration;

import static org.mockito.Mockito.when;

import com.github.yuyuvu.urlshortener.application.LinkService;
import com.github.yuyuvu.urlshortener.application.NotificationService;
import com.github.yuyuvu.urlshortener.application.UserService;
import com.github.yuyuvu.urlshortener.domain.model.Notification;
import com.github.yuyuvu.urlshortener.domain.model.ShortLink;
import com.github.yuyuvu.urlshortener.domain.model.User;
import com.github.yuyuvu.urlshortener.domain.repository.NotificationRepository;
import com.github.yuyuvu.urlshortener.domain.repository.ShortLinkRepository;
import com.github.yuyuvu.urlshortener.domain.repository.UserRepository;
import com.github.yuyuvu.urlshortener.exceptions.InvalidOriginalLinkException;
import com.github.yuyuvu.urlshortener.exceptions.StorageStatePersistenceException;
import com.github.yuyuvu.urlshortener.infrastructure.config.ConfigManager;
import com.github.yuyuvu.urlshortener.infrastructure.persistence.FileStorageService;
import com.github.yuyuvu.urlshortener.infrastructure.persistence.InMemoryNotificationRepository;
import com.github.yuyuvu.urlshortener.infrastructure.persistence.InMemoryShortLinkRepository;
import com.github.yuyuvu.urlshortener.infrastructure.persistence.InMemoryUserRepository;
import com.github.yuyuvu.urlshortener.infrastructure.persistence.StorageState;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Класс для тестов процесса сохранения состояния сервиса в файл и его загрузки из файла. */
@ExtendWith(MockitoExtension.class)
public class FileStorageSaveLoadTest {

  @Mock private ConfigManager configManager;

  /** Проверяем весь процесс загрузки и сохранения. */
  @Test
  void saveAndLoadFileStorage()
      throws InvalidOriginalLinkException, StorageStatePersistenceException, IOException {
    // Подставляем нужные настройки для создания новых ссылок
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

    // Создаём 3 новых пользователей и сохраняем их в репозиторий
    UserRepository userRepository = new InMemoryUserRepository(new HashMap<>());
    UserService userService = new UserService(userRepository);

    User ownerOfShortLink = userService.makeNewUUIDAndUser();
    userService.saveNewUser(ownerOfShortLink);
    User user2 = userService.makeNewUUIDAndUser();
    userService.saveNewUser(user2);
    User user3 = userService.makeNewUUIDAndUser();
    userService.saveNewUser(user3);

    // Создаём 2 новых ссылки и сохраняем их в репозиторий
    ShortLinkRepository shortLinkRepository = new InMemoryShortLinkRepository(new HashMap<>());
    LinkService linkService = new LinkService(shortLinkRepository, configManager);

    final ShortLink[] shortLink = new ShortLink[2];
    shortLink[0] =
        linkService.saveNewShortLink(
            linkService.makeNewShortLink("https://github.com", ownerOfShortLink.getUUID()));
    shortLink[1] =
        linkService.saveNewShortLink(
            linkService.makeNewShortLink("https://google.com", ownerOfShortLink.getUUID()));

    // Создаём два новых разных уведомления и сохраняем их в репозиторий
    NotificationRepository notificationRepository =
        new InMemoryNotificationRepository(new ArrayList<>());
    NotificationService notificationService = new NotificationService(notificationRepository);

    Notification notification1 =
        notificationService.makeNewShortLinkExpiredNotification(shortLink[0]);
    notificationService.saveNewNotification(notification1);
    Notification notification2 =
        notificationService.makeNewShortLinkLimitReachedNotification(shortLink[0]);
    notificationService.saveNewNotification(notification2);

    // Сохраняем состояние приложения
    Path testAppdata = Path.of("test_appdata");
    Path testStoragePath = testAppdata.resolve("test_storage.json");
    when(configManager.getFileStoragePathProperty()).thenReturn(testStoragePath);

    FileStorageService fileStorageService = new FileStorageService(configManager);

    // Проверяем отсутствие ошибок при сохранении
    UserRepository finalUserRepository = userRepository;
    ShortLinkRepository finalShortLinkRepository = shortLinkRepository;
    NotificationRepository finalNotificationRepository = notificationRepository;
    Assertions.assertDoesNotThrow(
        () ->
            fileStorageService.saveStorageState(
                finalUserRepository, finalShortLinkRepository, finalNotificationRepository));

    // Проверяем отсутствие ошибок при загрузке
    Optional<StorageState> optionalStorageState;
    StorageState storageState = null;
    Assertions.assertDoesNotThrow(fileStorageService::loadState);
    if ((optionalStorageState = fileStorageService.loadState()).isPresent()) {
      storageState = optionalStorageState.get();
    }
    Assertions.assertNotNull(storageState);

    // Создаём новые объекты и заполняем данными из StorageState
    userRepository = new InMemoryUserRepository(storageState.getExistingUsers());
    userService = new UserService(userRepository);
    shortLinkRepository = new InMemoryShortLinkRepository(storageState.getExistingShortLinks());
    linkService = new LinkService(shortLinkRepository, configManager);
    notificationRepository =
        new InMemoryNotificationRepository(storageState.getUnreadNotifications());
    notificationService = new NotificationService(notificationRepository);

    // Проверяем что сервисы возвращают те же данные, что мы и записали
    Assertions.assertTrue(userService.getUserByUUID(ownerOfShortLink.getUUID()).isPresent());
    Assertions.assertTrue(userService.getUserByUUID(user2.getUUID()).isPresent());
    Assertions.assertTrue(userService.getUserByUUID(user3.getUUID()).isPresent());

    LinkService finalLinkService = linkService;
    Assertions.assertDoesNotThrow(
        () ->
            finalLinkService.validateShortLinkExistence(
                "https://yulink.tech/" + shortLink[0].getShortId()));
    Assertions.assertDoesNotThrow(
        () ->
            finalLinkService.validateShortLinkExistence(
                "https://yulink.tech/" + shortLink[1].getShortId()));

    Assertions.assertEquals(
        2, notificationService.getUnreadNotificationsByUUID(ownerOfShortLink.getUUID()).size());

    Files.deleteIfExists(testStoragePath);
    Files.deleteIfExists(testAppdata);
  }
}
