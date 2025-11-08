package com.github.yuyuvu.urlshortener.application;

import com.github.yuyuvu.urlshortener.domain.model.User;
import com.github.yuyuvu.urlshortener.domain.repository.UserRepository;
import java.util.Optional;
import java.util.UUID;

/**
 * Сервис для совершения различных операций с пользователями: создания, сохранения, получения,
 * проверки существования.
 */
public class UserService {
  UserRepository userRepository;

  /** Сервис зависит от UserRepository. */
  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  /**
   * Метод для генерации нового незанятого UUID, продолжает генерацию UUID, пока не найдёт свободный
   * UUID. Возвращает объект типа User, который может хранить дополнительное нужное сервису
   * сокращения ссылок состояние.
   */
  public User makeNewUUIDAndUser() {
    while (true) {
      UUID uuid = UUID.randomUUID();
      if (userRepository.getUserByUUID(uuid).isEmpty()) {
        return new User(uuid, 0);
      }
    }
  }

  /** Метод для сохранения нового пользователя в репозиторий. */
  public User saveNewUser(User user) {
    return userRepository.saveUser(user);
  }

  /** Метод для получения пользователя по UUID. */
  public Optional<User> getUserByUUID(UUID uuid) {
    return userRepository.getUserByUUID(uuid);
  }

  /** Метод для проверки существования пользователя по UUID. */
  public boolean checkUserExistenceByUUID(UUID uuid) {
    return userRepository.getUserByUUID(uuid).isPresent();
  }
}
