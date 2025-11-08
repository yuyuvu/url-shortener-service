package com.github.yuyuvu.urlshortener.infrastructure.persistence;

import com.github.yuyuvu.urlshortener.domain.repository.NotificationRepository;
import com.github.yuyuvu.urlshortener.domain.repository.ShortLinkRepository;
import com.github.yuyuvu.urlshortener.domain.repository.UserRepository;
import com.github.yuyuvu.urlshortener.exceptions.StorageStatePersistenceException;
import com.github.yuyuvu.urlshortener.infrastructure.config.ConfigManager;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

/** Класс реализующий методы для чтения и записи StorageState в формате файла. */
public class FileStorageService implements StorageService {
  private final ObjectMapper objectMapper = new ObjectMapper();
  private Path storagePath;
  private final ConfigManager configManager;

  /** Конструктор объекта для чтения и записи StorageState в формате файла. */
  public FileStorageService(ConfigManager configManager) {
    this.configManager = configManager;
  }

  /**
   * Метод собирает состояние всех репозиториев и сохраняет в JSON согласно структуре объекта
   * StorageState при выключении сервиса.
   */
  @Override
  public void saveStorageState(
      UserRepository userRepository,
      ShortLinkRepository shortLinkRepository,
      NotificationRepository notificationRepository)
      throws StorageStatePersistenceException {
    // Получаем путь до файла внешнего постоянного хранилища из настроек
    storagePath = configManager.getFileStoragePathProperty();
    StorageState storageState = new StorageState();

    // Получаем все данные для сохранения во внешнее постоянное хранилище
    storageState.setExistingUsers(userRepository.getRepositoryAsMap());
    storageState.setExistingShortLinks(shortLinkRepository.getRepositoryAsMap());
    storageState.setUnreadNotifications(notificationRepository.getRepositoryAsList());

    try {
      // Создаём директории для файла, если их ещё нет
      if (!Files.exists(storagePath)) {
        Files.createDirectories(storagePath.getParent());
      }

      // Сериализуем StorageState в формате JSON и записываем результат в файл
      objectMapper.writeValue(storagePath.toFile(), storageState);
    } catch (JacksonException | IOException e) {
      throw new StorageStatePersistenceException(
          "Проблемы с сериализацией и сохранением файла данных сервиса: "
              + storagePath
              + " "
              + e.getMessage()
              + "\nДанные не были сохранены."
              + "\n"
              + e.getCause()
              + "\n"
              + Arrays.toString(e.getStackTrace()));
    }
  }

  /**
   * Метод считывает состояние данных сервиса в виде StorageState из файла и возвращает объект, из
   * которого репозитории потом могут загрузить нужные им данные при включении сервиса.
   */
  @Override
  public Optional<StorageState> loadState() throws StorageStatePersistenceException {
    // Получаем путь до файла внешнего постоянного хранилища из настроек
    storagePath = configManager.getFileStoragePathProperty();

    // Если это первый запуск и файла ещё нет, возвращаем пустое состояние
    if (!Files.exists(storagePath)) {
      return Optional.empty();
    }

    // Иначе десериализуем StorageState из файла
    try {
      return Optional.of(objectMapper.readValue(storagePath.toFile(), StorageState.class));
    } catch (JacksonException e) {
      throw new StorageStatePersistenceException(
          "Проблемы с десериализацией и загрузкой файла данных сервиса: "
              + storagePath
              + " "
              + e.getMessage()
              + "\nСервис не будет запущен при наличии файла данных "
              + "и одновременной невозможности считывания / загрузки ранее сохранённых данных."
              + "\nПри штатном выключении это может привести к перезаписи сохранённых ранее данных."
              + "\nПроверьте формат данных файла хранилища, который указан "
              + "в конфигурационном файле."
              + "\n"
              + e.getCause()
              + "\n"
              + Arrays.toString(e.getStackTrace()));
    }
  }
}
