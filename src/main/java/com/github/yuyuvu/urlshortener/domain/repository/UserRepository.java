package com.github.yuyuvu.urlshortener.domain.repository;

import com.github.yuyuvu.urlshortener.domain.model.User;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** Интерфейс хранилища пользователей, связанных с определённым UUID. */
public interface UserRepository {

  /** Метод для сохранения нового пользователя в хранилище. */
  User saveUser(User user);

  /** Метод для получения всех пользователей из хранилища. */
  List<User> getAllUsers();

  /**
   * Метод для получения пользователя из хранилища по UUID.
   * Может также использоваться для проверки существования пользователя.
   * */
  Optional<User> getUserByUUID(UUID uuid);

  /** Метод для удаления пользователя из хранилища. */
  boolean deleteUser(UUID uuid);

  /**
   * Метод для получения всех пользователей в формате ключ-значение (UUID - объект пользователя),
   * используется для сохранения данных во внешнее постоянное хранилище
   * (например, базу данных или файл).
   */
  Map<UUID, User> getRepositoryAsMap();
}
