package com.github.yuyuvu.urlshortener;

import com.github.yuyuvu.urlshortener.application.LinkService;
import com.github.yuyuvu.urlshortener.application.NotificationService;
import com.github.yuyuvu.urlshortener.application.UserService;
import com.github.yuyuvu.urlshortener.cli.ConsoleController;
import com.github.yuyuvu.urlshortener.domain.repository.NotificationRepository;
import com.github.yuyuvu.urlshortener.domain.repository.ShortLinkRepository;
import com.github.yuyuvu.urlshortener.domain.repository.UserRepository;
import com.github.yuyuvu.urlshortener.exceptions.StorageStatePersistenceException;
import com.github.yuyuvu.urlshortener.infrastructure.config.ConfigManager;
import com.github.yuyuvu.urlshortener.infrastructure.persistence.FileStorageService;
import com.github.yuyuvu.urlshortener.infrastructure.persistence.StorageState;
import com.github.yuyuvu.urlshortener.infrastructure.persistence.StorageService;
import com.github.yuyuvu.urlshortener.infrastructure.persistence.InMemoryUserRepository;
import com.github.yuyuvu.urlshortener.infrastructure.persistence.InMemoryNotificationRepository;
import com.github.yuyuvu.urlshortener.infrastructure.persistence.InMemoryShortLinkRepository;
import com.github.yuyuvu.urlshortener.infrastructure.scheduler.LinkCheckStateTask;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/** Класс, собирающий все объекты приложения и передающий зависимости по цепочке дальше.
 * В конце обращается к ConsoleController для запуска цикла чтения команд или коротких URL. */
public class UrlShortenerApp {

  /** Точка входа в приложение, запускается из класса Main. */
  public void start() throws StorageStatePersistenceException {
    // Загружаем настройки приложения из файла или при их отсутствии устанавливаем по умолчанию
    ConfigManager configManager;
    configManager = new ConfigManager();

    // Загружаем состояние всех репозиториев из файла
    StorageService storageService = new FileStorageService(configManager);
    StorageState loadedStorageState;
    try {
      loadedStorageState = storageService.loadState().orElse(new StorageState());
    } catch (StorageStatePersistenceException e) {
      // Возможная критическая проблема загрузки данных после перезапуска сервиса,
      // которая может привести к их перезаписи впоследствии
      throw new StorageStatePersistenceException(e.getMessage());
    }

    // Возвращаем все репозитории в состояние до выключения сервиса
    ShortLinkRepository linkRepository =
        new InMemoryShortLinkRepository(loadedStorageState.getExistingShortLinks());
    UserRepository userRepository =
        new InMemoryUserRepository(loadedStorageState.getExistingUsers());
    NotificationRepository notificationRepository =
        new InMemoryNotificationRepository(loadedStorageState.getUnreadNotifications());

    // Инициализируем сервисы
    UserService userService = new UserService(userRepository);
    LinkService linkService = new LinkService(linkRepository, configManager);
    NotificationService notificationService = new NotificationService(notificationRepository);

    // Создаём обработчик ввода из консоли
    ConsoleController consoleController =
        new ConsoleController(userService, linkService, notificationService, configManager);

    /*
    * Создаём задание, которое в параллельном режиме будет проверять истечение
    * срока действия коротких ссылок и удалять их, отправлять уведомления,
    * если был израсходован лимит использований или срок действия ссылки истёк,
    * а также очищать список уже прочитанных уведомлений.
    * */
    ScheduledExecutorService scheduledExecutorService =
        Executors.newSingleThreadScheduledExecutor();
    scheduledExecutorService.scheduleWithFixedDelay(
        new LinkCheckStateTask(
            linkService,
            notificationService,
            userService,
            consoleController::sendUnreadNotifications),
        5,
        15,
        TimeUnit.SECONDS);

    // Добавляем автосохранение всех данных при выключении сервиса
    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(
                () -> {
                  try {
                    storageService.saveStorageState(
                        userRepository, linkRepository, notificationRepository);
                  } catch (StorageStatePersistenceException e) {
                    // Обработка критической проблемы сохранения данных при выключении сервиса
                    System.err.println(e.getMessage());
                  }
                  scheduledExecutorService.shutdown();
                }));

    // Запускаем цикл чтения команд или перенаправления по коротким URL
    consoleController.startListening();
  }
}
