package com.github.yuyuvu.urlshortener.infrastructure.persistence;

import static com.github.yuyuvu.urlshortener.cli.presenters.ColorPrinter.printlnRed;

import com.github.yuyuvu.urlshortener.domain.repository.NotificationRepository;
import com.github.yuyuvu.urlshortener.domain.repository.ShortLinkRepository;
import com.github.yuyuvu.urlshortener.domain.repository.UserRepository;
import com.github.yuyuvu.urlshortener.infrastructure.config.ConfigManager;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

public class FileStorageService {
  private final ObjectMapper objectMapper = new ObjectMapper();
  private Path storagePath;
  private final ConfigManager configManager;

  public FileStorageService(ConfigManager configManager) {
    this.configManager = configManager;
  }

  /**
   * Метод собирает состояние всех репозиториев и сохраняет в JSON согласно структуре объекта
   * StorageState
   */
  public boolean saveStorageState(
      UserRepository userRepository,
      ShortLinkRepository shortLinkRepository,
      NotificationRepository notificationRepository) {
    storagePath = configManager.getFileStoragePathProperty();
    StorageState storageState = new StorageState();

    storageState.setExistingUsers(userRepository.getRepositoryAsMap());
    storageState.setExistingShortLinks(shortLinkRepository.getRepositoryAsMap());
    storageState.setUnreadNotifications(notificationRepository.getRepositoryAsList());

    try {
      objectMapper.writeValue(storagePath.toFile(), storageState);
      return true;
    } catch (JacksonException e) {
      printlnRed(
          "Проблемы с сериализацией и сохранением файла данных сервиса: "
              + storagePath
              + " "
              + e.getMessage()
              + "\nДанные не были сохранены.");
      return false;
    }
  }

  public Optional<StorageState> loadState() {
    storagePath = configManager.getFileStoragePathProperty();
    if (!Files.exists(storagePath)) {
      return Optional.empty();
    }
    try {
      return Optional.of(objectMapper.readValue(storagePath.toFile(), StorageState.class));
    } catch (JacksonException e) {
      printlnRed(
          "Проблемы с десериализацией и загрузкой файла данных сервиса: "
              + storagePath
              + " "
              + e.getMessage()
              + "\nПриложение будет запущено без ранее сохранённых данных.");
      return Optional.empty();
    }
  }
}
