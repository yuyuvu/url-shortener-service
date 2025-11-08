package com.github.yuyuvu.urlshortener.domain.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.yuyuvu.urlshortener.exceptions.UsagesLimitReachedException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

public class ShortLink {
  private final UUID ownerOfShortURL;

  private String originalURLAddress;
  private String shortId;

  private final LocalDateTime creationDateTime;
  private LocalDateTime expirationDateTime;

  private int usageCounter;
  private int usageLimitAmount;
  private boolean isLimitNotified;

  @JsonCreator
  public ShortLink(
      @JsonProperty("originalURLAddress") String originalURLAddress,
      @JsonProperty("shortId") String shortId,
      @JsonProperty("creationDateTime") LocalDateTime creationDateTime,
      @JsonProperty("expirationDateTime") LocalDateTime expirationDateTime,
      @JsonProperty("usageCounter") int usageCounter,
      @JsonProperty("usageLimitAmount") int usageLimitAmount,
      @JsonProperty("ownerOfShortURL") UUID ownerOfShortURL,
      @JsonProperty("limitNotified") boolean isLimitNotified) {
    this.originalURLAddress = originalURLAddress;
    this.shortId = shortId;
    this.creationDateTime = creationDateTime;
    this.expirationDateTime = expirationDateTime;
    this.usageCounter = usageCounter;
    this.usageLimitAmount = usageLimitAmount;
    this.ownerOfShortURL = ownerOfShortURL;
    this.isLimitNotified = isLimitNotified;
  }

  public boolean isExpired() {
    return this.expirationDateTime.isBefore(LocalDateTime.now());
  }

  public boolean isLimitReached() {
    return usageCounter >= usageLimitAmount;
  }

  public boolean isLimitNotified() {
    return isLimitNotified;
  }

  public void setLimitNotified(boolean isLimitNotified) {
    this.isLimitNotified = isLimitNotified;
  }

  public void incrementUsageCounter() throws UsagesLimitReachedException {
    if (isLimitReached()) {
      throw new UsagesLimitReachedException(
          "Лимит переходов по данной ссылке исчерпан. Ей больше нельзя воспользоваться."
              + "\nОна будет удалена в "
              + this.getExpirationDateTime()
                  .format(
                      DateTimeFormatter.ofPattern("E dd.MM.uuuu HH:mm")
                          .withLocale(Locale.forLanguageTag("ru-RU")))
              + ".");
    }
    usageCounter++;
  }

  // Геттеры и сеттеры

  public String getShortId() {
    return shortId;
  }

  public void setShortId(String shortId) {
    this.shortId = shortId;
  }

  public String getOriginalURLAddress() {
    return originalURLAddress;
  }

  public void setOriginalURLAddress(String originalURLAddress) {
    this.originalURLAddress = originalURLAddress;
  }

  public LocalDateTime getCreationDateTime() {
    return creationDateTime;
  }

  public LocalDateTime getExpirationDateTime() {
    return expirationDateTime;
  }

  public void setExpirationDateTime(LocalDateTime expirationDateTime) {
    this.expirationDateTime = expirationDateTime;
  }

  public int getUsageLimitAmount() {
    return usageLimitAmount;
  }

  public void setUsageLimitAmount(int usageLimitAmount) {
    this.usageLimitAmount = usageLimitAmount;
  }

  public int getUsageCounter() {
    return usageCounter;
  }

  public UUID getOwnerOfShortURL() {
    return ownerOfShortURL;
  }
}
