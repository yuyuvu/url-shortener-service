package com.github.yuyuvu.urlshortener.exceptions;

/** Ошибка, выбрасываемая при попытках менять параметры чужих ссылок. */
public class NotEnoughPermissionsException extends Exception {
  public NotEnoughPermissionsException() {
    super(
        "Ошибка: вы не являетесь владельцем данной ссылки, "
            + "поэтому не можете менять её параметры. Идентифицируйтесь с UUID её владельца.");
  }
}
