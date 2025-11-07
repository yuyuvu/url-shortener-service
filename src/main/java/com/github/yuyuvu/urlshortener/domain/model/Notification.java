package com.github.yuyuvu.urlshortener.domain.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

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

  @JsonCreator
  public Notification(
      @JsonProperty("shortLink") ShortLink shortLink,
      @JsonProperty("userUUID") UUID userUUID,
      @JsonProperty("type") NotificationType type,
      @JsonProperty("read") boolean isRead) {
    this.shortLink = shortLink;
    this.userUUID = userUUID;
    this.type = type;
    this.isRead = isRead;
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
