package com.github.yuyuvu.urlshortener.domain.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;

/**
 * User представляет собой UUID с некоторыми дополнительными связанными с ним свойствами (например,
 * количество созданных им ссылок). Введение User позволяет в дальнейшем расширять логику работы с
 * UUID.
 */
public class User {
  /** UUID пользователя. */
  private final UUID uuid;

  /** Количество созданных пользователем с определённым UUID ссылок. */
  private int amountOfMadeShortLinks;

  /**
   * Объект User представляет собой UUID с некоторыми дополнительными связанными с ним свойствами
   * (например, количество созданных им ссылок). Введение User позволяет в дальнейшем расширять
   * логику работы с UUID.
   */
  @JsonCreator
  public User(
      @JsonProperty("uuid") UUID uuid,
      @JsonProperty("amountOfMadeShortLinks") int amountOfMadeShortLinks) {
    this.uuid = uuid;
    this.amountOfMadeShortLinks = amountOfMadeShortLinks;
  }

  /** Метод для инкремента счётчика созданных данным UUID ссылок. */
  public void incrementAmountOfMadeShortLinks() {
    this.amountOfMadeShortLinks = this.amountOfMadeShortLinks + 1;
  }

  /** Метод для декремента счётчика созданных данным UUID ссылок. */
  public void decrementAmountOfMadeShortLinks() {
    this.amountOfMadeShortLinks = this.amountOfMadeShortLinks - 1;
  }

  /*
   * Геттеры и сеттеры
   * */

  public UUID getUUID() {
    return uuid;
  }

  public int getAmountOfMadeShortLinks() {
    return amountOfMadeShortLinks;
  }
}
