package com.github.yuyuvu.urlshortener;

import com.github.yuyuvu.urlshortener.application.LinkService;
import com.github.yuyuvu.urlshortener.application.NotificationService;
import com.github.yuyuvu.urlshortener.application.UserService;
import com.github.yuyuvu.urlshortener.cli.ConsoleController;
import com.github.yuyuvu.urlshortener.domain.repository.NotificationRepository;
import com.github.yuyuvu.urlshortener.domain.repository.ShortLinkRepository;
import com.github.yuyuvu.urlshortener.domain.repository.UserRepository;
import com.github.yuyuvu.urlshortener.infrastructure.config.ConfigManager;
import com.github.yuyuvu.urlshortener.infrastructure.persistence.*;
import com.github.yuyuvu.urlshortener.infrastructure.scheduler.LinkCleanupTask;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class UrlShortenerApp {

  /** Точка входа в приложение, запускается из класса Main. */
  public void start() {
    // Загружаем настройки приложения из файла или при их отсутствии устанавливаем по умолчанию
    ConfigManager configManager;
    configManager = new ConfigManager();

    // Загружаем состояние всех репозиториев из файла
    FileStorageService fileStorageService = new FileStorageService(configManager);
    StorageState loadedStorageState;
    loadedStorageState = fileStorageService.loadState().orElse(new StorageState());

    // Возвращаем все репозитории в состояние до выключения сервиса
    ShortLinkRepository linkRepository =
        new InMemoryShortLinkRepository(loadedStorageState.getExistingShortLinks());
    UserRepository userRepository =
        new InMemoryUserRepository(loadedStorageState.getExistingUsers());
    NotificationRepository notificationRepository =
        new InMemoryNotificationRepository(loadedStorageState.getUnreadNotifications());

    // Инициализируем сервисы
    UserService userService = new UserService(userRepository, configManager);
    LinkService linkService = new LinkService(linkRepository, configManager);
    NotificationService notificationService =
        new NotificationService(notificationRepository, configManager);

    // Создаём обработчик ввода из консоли
    ConsoleController consoleController =
        new ConsoleController(userService, linkService, notificationService, configManager);

    // TODO: Задание для Scheduler
    // ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    // scheduledExecutorService.scheduleWithFixedDelay(new LinkCleanupTask(linkService), 20, 20, TimeUnit.SECONDS);

    // Добавляем автосохранение всех данных при выключении сервиса
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      fileStorageService.saveStorageState(userRepository, linkRepository, notificationRepository);
      // scheduledExecutorService.shutdown();
    }));

    consoleController.startListening();
  }
}
