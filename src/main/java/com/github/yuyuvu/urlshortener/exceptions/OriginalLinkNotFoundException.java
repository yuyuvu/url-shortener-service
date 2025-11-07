package com.github.yuyuvu.urlshortener.exceptions;

/** Ошибка, выбрасываемая при отсутствии оригинального URL по запрашиваемому короткому URL. */
public class OriginalLinkNotFoundException extends Exception {
  public OriginalLinkNotFoundException(String message) {
    super(message);
  }
}
