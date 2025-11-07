package com.github.yuyuvu.urlshortener.domain.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.yuyuvu.urlshortener.exceptions.UsagesLimitReachedException;
import java.time.LocalDateTime;
import java.util.UUID;

public class ShortLink {
  UUID ownerOfShortURL;

  private String originalURLAddress;
  private String shortId;

  private LocalDateTime creationDateTime;
  private LocalDateTime expirationDateTime;

  private int usageCounter;
  private int usageLimitAmount;

  @JsonCreator
  public ShortLink(
      @JsonProperty("originalURLAddress") String originalURLAddress,
      @JsonProperty("shortId") String shortId,
      @JsonProperty("creationDateTime") LocalDateTime creationDateTime,
      @JsonProperty("expirationDateTime") LocalDateTime expirationDateTime,
      @JsonProperty("usageCounter") int usageCounter,
      @JsonProperty("usageLimitAmount") int usageLimitAmount,
      @JsonProperty("ownerOfShortURL") UUID ownerOfShortURL) {
    this.originalURLAddress = originalURLAddress;
    this.shortId = shortId;
    this.creationDateTime = creationDateTime;
    this.expirationDateTime = expirationDateTime;
    this.usageCounter = usageCounter;
    this.usageLimitAmount = usageLimitAmount;
    this.ownerOfShortURL = ownerOfShortURL;
  }

  public boolean isLimitReached() {
    return usageCounter >= usageLimitAmount;
  }

  public void incrementUsageCounter() throws UsagesLimitReachedException {
    if (isLimitReached()) {
      throw new UsagesLimitReachedException("Лимит переходов по данной ссылке исчерпан.");
    }
    usageCounter++;
  }

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

  public void setCreationDateTime(LocalDateTime creationDateTime) {
    this.creationDateTime = creationDateTime;
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

  public void setUsageCounter(int usageCounter) {
    this.usageCounter = usageCounter;
  }

  public UUID getOwnerOfShortURL() {
    return ownerOfShortURL;
  }

  public void setOwnerOfShortURL(UUID ownerOfShortURL) {
    this.ownerOfShortURL = ownerOfShortURL;
  }
}
