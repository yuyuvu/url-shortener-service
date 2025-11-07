package com.github.yuyuvu.urlshortener.application;

import com.github.yuyuvu.urlshortener.domain.model.User;
import com.github.yuyuvu.urlshortener.domain.repository.UserRepository;
import com.github.yuyuvu.urlshortener.infrastructure.config.ConfigManager;
import java.util.Optional;
import java.util.UUID;

public class UserService {
  UserRepository userRepository;
  ConfigManager configManager;

  public UserService(UserRepository userRepository, ConfigManager configManager) {
    this.userRepository = userRepository;
  }

  public User makeNewUUIDAndUser() {
    while (true) {
      UUID uuid = UUID.randomUUID();
      if (userRepository.getUserByUUID(uuid).isEmpty()) {
        return new User(uuid, 0);
      }
    }
  }

  public User saveNewUser(User user) {
    return userRepository.saveUser(user);
  }

  public Optional<User> getUserByUUID(UUID uuid) {
    return userRepository.getUserByUUID(uuid);
  }

  public boolean checkUserExistenceByUUID(UUID uuid) {
    return userRepository.getUserByUUID(uuid).isPresent();
  }
}
