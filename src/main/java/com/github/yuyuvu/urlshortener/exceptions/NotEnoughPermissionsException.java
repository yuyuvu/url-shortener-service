package com.github.yuyuvu.urlshortener.exceptions;

/** Ошибка, выбрасываемая при попытках менять параметры чужих ссылок. */
public class NotEnoughPermissionsException extends Exception {

  /** Ошибка, выбрасываемая при попытках менять параметры чужих ссылок. */
  public NotEnoughPermissionsException() {
    super(
        "Вы не являетесь владельцем данной ссылки, "
            + "поэтому не можете менять её параметры. Идентифицируйтесь с UUID её владельца.");
  }
}
