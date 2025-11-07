package com.github.yuyuvu.urlshortener.infrastructure.persistence;

import com.github.yuyuvu.urlshortener.domain.model.User;
import com.github.yuyuvu.urlshortener.domain.repository.UserRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryUserRepository implements UserRepository {
  Map<UUID, User> existingUsers = new ConcurrentHashMap<>();

  public InMemoryUserRepository(Map<UUID, User> existingUsers) {
    this.existingUsers.putAll(existingUsers);
  }

  @Override
  public User saveUser(User user) {
    return existingUsers.put(user.getUUID(), user);
  }

  @Override
  public List<User> getAllUsers() {
    return existingUsers.values().stream().toList();
  }

  @Override
  public Optional<User> getUserByUUID(UUID uuid) {
    return Optional.ofNullable(existingUsers.get(uuid));
  }

  @Override
  public boolean deleteUser(UUID uuid) {
    return existingUsers.remove(uuid) != null;
  }

  @Override
  public Map<UUID, User> getRepositoryAsMap() {
    return existingUsers;
  }
}
