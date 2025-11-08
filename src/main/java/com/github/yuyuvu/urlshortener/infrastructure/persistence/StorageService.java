package com.github.yuyuvu.urlshortener.infrastructure.persistence;

import com.github.yuyuvu.urlshortener.domain.repository.NotificationRepository;
import com.github.yuyuvu.urlshortener.domain.repository.ShortLinkRepository;
import com.github.yuyuvu.urlshortener.domain.repository.UserRepository;
import com.github.yuyuvu.urlshortener.exceptions.StorageStatePersistenceException;

import java.util.Optional;

/**
 * Интерфейс, определяющий методы для чтения и записи StorageState
 * в любом внешнем хранилище данных (базе данных, файле, сокете и т.д.).
 */
public interface StorageService {
  /**
   * Метод собирает состояние всех репозиториев и сохраняет во внешнее постоянное хранилище
   * согласно структуре объекта StorageState при выключении сервиса.
   */
  void saveStorageState(
      UserRepository userRepository,
      ShortLinkRepository shortLinkRepository,
      NotificationRepository notificationRepository) throws StorageStatePersistenceException;

  /**
   * Метод считывает состояние данных сервиса в виде StorageState из внешнего постоянного хранилища
   * и возвращает объект, из которого репозитории потом могут загрузить нужные им данные
   * при включении сервиса.
   */
  Optional<StorageState> loadState() throws StorageStatePersistenceException;
}
