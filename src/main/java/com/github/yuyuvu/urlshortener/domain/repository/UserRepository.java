package com.github.yuyuvu.urlshortener.domain.repository;

import com.github.yuyuvu.urlshortener.domain.model.User;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {

  User saveUser(User user);

  List<User> getAllUsers();

  Optional<User> getUserByUUID(UUID uuid);

  boolean deleteUser(UUID uuid);

  Map<UUID, User> getRepositoryAsMap();
}
