package com.github.yuyuvu.urlshortener.infrastructure.persistence;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.github.yuyuvu.urlshortener.domain.model.Notification;
import com.github.yuyuvu.urlshortener.domain.model.ShortLink;
import com.github.yuyuvu.urlshortener.domain.model.User;
import java.util.*;

public class StorageState {
  private Map<String, ShortLink> existingShortLinks;
  private Map<UUID, User> existingUsers;
  private List<Notification> unreadNotifications;

  @JsonCreator
  public StorageState() {
    this.existingShortLinks = new HashMap<>();
    this.existingUsers = new HashMap<>();
    this.unreadNotifications = new ArrayList<>();
  }

  public Map<String, ShortLink> getExistingShortLinks() {
    return existingShortLinks;
  }

  public void setExistingShortLinks(Map<String, ShortLink> existingShortLinks) {
    this.existingShortLinks = existingShortLinks;
  }

  public Map<UUID, User> getExistingUsers() {
    return existingUsers;
  }

  public void setExistingUsers(Map<UUID, User> existingUsers) {
    this.existingUsers = existingUsers;
  }

  public List<Notification> getUnreadNotifications() {
    return unreadNotifications;
  }

  public void setUnreadNotifications(List<Notification> unreadNotifications) {
    this.unreadNotifications = unreadNotifications;
  }
}
