package com.github.yuyuvu.urlshortener.domain.model;

import java.util.UUID;

public class Notification {
  private final ShortLink shortLink;
  private final UUID userUUID;
  private final NotificationType type;

  private boolean isRead;

  public enum NotificationType {
    EXPIRED,
    LIMIT_REACHED
  }

  public Notification(ShortLink shortLink, UUID userUUID, NotificationType type) {
    this.shortLink = shortLink;
    this.userUUID = userUUID;
    this.type = type;
    this.isRead = false;
  }

  public ShortLink getShortLink() {
    return shortLink;
  }

  public UUID getUserUUID() {
    return userUUID;
  }

  public NotificationType getType() {
    return type;
  }

  public boolean isRead() {
    return isRead;
  }

  public void setRead(boolean read) {
    isRead = read;
  }
}
