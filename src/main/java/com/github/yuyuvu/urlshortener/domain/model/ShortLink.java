package com.github.yuyuvu.urlshortener.domain.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.yuyuvu.urlshortener.exceptions.UsagesLimitReachedException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

/**
 * Основной класс, с которым работает приложение. Объект ShortLink представляет собой созданную
 * короткую ссылку со всем контекстом, в котором она создана, и со всеми данными о том, до какого
 * момента она должна быть активной. <br>
 * В объекте ShortLink хранятся: UUID создателя-владельца ссылки, строка с URL, на который короткая
 * ссылка ведёт, короткий ID из символов, идентифицирующий данную короткую ссылку в сервисе
 * (показывается после URL сервиса), дата и время создания короткой ссылки, дата и время окончания
 * срока действия короткой ссылки (по истечении него она будет удалена), счётчик использований,
 * максимальное количество использований короткой ссылки и флаг, свидетельствующий о том, что
 * уведомление об израсходовании лимита уже было создано. <br>
 * Также содержит методы для проверки израсходования лимита использований ссылки, истечения её срока
 * действия, наличия уже созданных уведомлений по ней и увеличения счётчика использования.
 */
public class ShortLink {
  /** UUID создателя-владельца ссылки. */
  private final UUID ownerOfShortURL;

  /** Строка с URL, на который короткая ссылка ведёт. */
  private String originalURLAddress;

  /**
   * Короткий ID из символов, идентифицирующий данную короткую ссылку в сервисе (показывается после
   * URL сервиса).
   */
  private final String shortId;

  /** Дата и время создания короткой ссылки. Для тестов не финальная, но не должна изменяться. */
  private LocalDateTime creationDateTime;

  /**
   * Дата и время окончания срока действия короткой ссылки (по истечении него она будет удалена).
   */
  private LocalDateTime expirationDateTime;

  /** Счётчик использований короткой ссылки. */
  private int usageCounter;

  /** Максимальное количество использований короткой ссылки. */
  private int usageLimitAmount;

  /** Флаг, свидетельствующий о том, что уведомление об израсходовании лимита уже было создано. */
  private boolean isLimitNotified;

  /**
   * Данный конструктор используется единожды при создании короткой ссылки через команду shorten.
   * <br>
   * Объект ShortLink представляет собой созданную короткую ссылку со всем контекстом, в котором она
   * создана, и со всеми данными о том, до какого момента она должна быть активной. <br>
   * В объекте ShortLink хранятся: UUID создателя-владельца ссылки, строка с URL, на который
   * короткая ссылка ведёт, короткий ID из символов, идентифицирующий данную короткую ссылку в
   * сервисе (показывается после URL сервиса), дата и время создания короткой ссылки, дата и время
   * окончания срока действия короткой ссылки (по истечении него она будет удалена), счётчик
   * использований, максимальное количество использований короткой ссылки и флаг, свидетельствующий
   * о том, что уведомление об израсходовании лимита уже было создано.
   */
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

  /** Метод для проверки того, что срок действия ссылки истёк. Проверяется из LinkCheckStateTask. */
  public boolean isExpired() {
    return this.expirationDateTime.isBefore(LocalDateTime.now());
  }

  /** Метод для проверки того, что лимит использований ссылки был израсходован. */
  public boolean isLimitReached() {
    return usageCounter >= usageLimitAmount;
  }

  /**
   * Метод для проверки того, что уведомление об израсходовании лимита использований уже было
   * создано.
   */
  public boolean isLimitNotified() {
    return isLimitNotified;
  }

  /**
   * Метод для установки отметки о том, что уведомление об израсходовании лимита использований было
   * создано и больше его создавать не надо.
   */
  public void setLimitNotified(boolean isLimitNotified) {
    this.isLimitNotified = isLimitNotified;
  }

  /**
   * Метод для увеличения счётчика использований короткой ссылки. Бросает
   * UsagesLimitReachedException при израсходованном лимите. Используется при редиректах.
   */
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

  /*
   * Геттеры и сеттеры
   * */

  public String getShortId() {
    return shortId;
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

  /** Метод для подмены даты создания ссылки в тестах и проверки логики изменения TTL. */
  public void setCreationDateTime(LocalDateTime creationDateTime, boolean usedForTests) {
    if (usedForTests) {
      this.creationDateTime = creationDateTime;
    }
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
