package com.github.yuyuvu.urlshortener.domain.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public class User {
  private UUID uuid;

  private int amountOfMadeShortLinks;

  @JsonCreator
  public User(@JsonProperty("uuid") UUID uuid,
              @JsonProperty("amountOfMadeShortLinks") int amountOfMadeShortLinks) {
    this.uuid = uuid;
    this.amountOfMadeShortLinks = amountOfMadeShortLinks;
  }

  public UUID getUUID() {
    return uuid;
  }

  public void setUUID(UUID uuid) {
    this.uuid = uuid;
  }

  public int getAmountOfMadeShortLinks() {
    return amountOfMadeShortLinks;
  }

  public void incrementAmountOfMadeShortLinks() {
    this.amountOfMadeShortLinks = this.amountOfMadeShortLinks + 1;
  }

  public void decrementAmountOfMadeShortLinks() {
    this.amountOfMadeShortLinks = this.amountOfMadeShortLinks - 1;
  }
}
