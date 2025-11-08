package com.github.yuyuvu.urlshortener.domain.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;

/**
 * Уведомление содержит в себе короткую ссылку, с событием по которой связано уведомление,
 * получателя уведомления и тип уведомления (истечение срока действия или израсходование лимита
 * использований ссылки).
 */
public class Notification {
  /** Короткая ссылка, с событием по которой связано уведомление. */
  private final ShortLink shortLink;

  /** Получатель уведомления. */
  private final UUID userUUID;

  /** Тип уведомления (истечение срока действия или израсходование лимита использований ссылки). */
  private final NotificationType type;

  /** Флаг, отражающий статус прочтения уведомления получателем. */
  private boolean isRead;

  /** Тип уведомления (истечение срока действия или израсходование лимита использований ссылки). */
  public enum NotificationType {
    EXPIRED,
    LIMIT_REACHED
  }

  /**
   * Объект уведомления содержит в себе короткую ссылку, с событием по которой связано уведомление,
   * получателя уведомления и тип уведомления (истечение срока действия или израсходование лимита
   * использований ссылки).
   */
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

  /*
   * Геттеры и сеттеры
   * */

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
