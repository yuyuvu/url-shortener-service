package com.github.yuyuvu.urlshortener.infrastructure.persistence;

import com.github.yuyuvu.urlshortener.domain.model.User;
import com.github.yuyuvu.urlshortener.domain.repository.UserRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Реализация UserRepository на ConcurrentHashMap для хранения всех данных о созданных UUID и
 * связанных с ними данных внутри оперативной памяти.
 */
public class InMemoryUserRepository implements UserRepository {

  Map<UUID, User> existingUsers = new ConcurrentHashMap<>();

  /** Конструктор для загрузки всех данных после перезапуска из StorageState. */
  public InMemoryUserRepository(Map<UUID, User> existingUsers) {
    this.existingUsers.putAll(existingUsers);
  }

  /** Метод для сохранения нового пользователя в хранилище. */
  @Override
  public User saveUser(User user) {
    return existingUsers.put(user.getUUID(), user);
  }

  /** Метод для получения всех пользователей из хранилища. */
  @Override
  public List<User> getAllUsers() {
    return existingUsers.values().stream().toList();
  }

  /**
   * Метод для получения пользователя из хранилища по UUID.
   * Может также использоваться для проверки существования пользователя.
   * */
  @Override
  public Optional<User> getUserByUUID(UUID uuid) {
    return Optional.ofNullable(existingUsers.get(uuid));
  }

  /** Метод для удаления пользователя из хранилища. */
  @Override
  public boolean deleteUser(UUID uuid) {
    return existingUsers.remove(uuid) != null;
  }

  /**
   * Метод для получения всех пользователей в формате ключ-значение (UUID - объект пользователя),
   * используется для сохранения данных во внешнее постоянное хранилище
   * (например, базу данных или файл).
   */
  @Override
  public Map<UUID, User> getRepositoryAsMap() {
    return existingUsers;
  }
}
