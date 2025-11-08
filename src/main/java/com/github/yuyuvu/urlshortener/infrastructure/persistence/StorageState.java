package com.github.yuyuvu.urlshortener.infrastructure.persistence;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.github.yuyuvu.urlshortener.domain.model.Notification;
import com.github.yuyuvu.urlshortener.domain.model.ShortLink;
import com.github.yuyuvu.urlshortener.domain.model.User;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Промежуточный объект состояния, в который собираются или из которого получаются все данные
 * сервиса перед сохранением из внешнего постоянного хранилища или перед записью в него.
 */
public class StorageState {
  private Map<String, ShortLink> existingShortLinks;
  private Map<UUID, User> existingUsers;
  private List<Notification> unreadNotifications;

  /** Конструктор для создания объекта, в который всё будет сохраняться при выключении сервиса. */
  @JsonCreator
  public StorageState() {
    this.existingShortLinks = new HashMap<>();
    this.existingUsers = new HashMap<>();
    this.unreadNotifications = new ArrayList<>();
  }

  /** Метод для загрузки данных для репозитория ShortLinkRepository. */
  public Map<String, ShortLink> getExistingShortLinks() {
    return existingShortLinks;
  }

  /** Метод для сохранения данных репозитория ShortLinkRepository. */
  public void setExistingShortLinks(Map<String, ShortLink> existingShortLinks) {
    this.existingShortLinks = existingShortLinks;
  }

  /** Метод для загрузки данных для репозитория UserRepository. */
  public Map<UUID, User> getExistingUsers() {
    return existingUsers;
  }

  /** Метод для сохранения данных репозитория UserRepository. */
  public void setExistingUsers(Map<UUID, User> existingUsers) {
    this.existingUsers = existingUsers;
  }

  /** Метод для загрузки данных для репозитория NotificationRepository. */
  public List<Notification> getUnreadNotifications() {
    return unreadNotifications;
  }

  /** Метод для сохранения данных репозитория NotificationRepository. */
  public void setUnreadNotifications(List<Notification> unreadNotifications) {
    this.unreadNotifications = unreadNotifications;
  }
}
