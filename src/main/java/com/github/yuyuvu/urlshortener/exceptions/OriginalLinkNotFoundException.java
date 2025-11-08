package com.github.yuyuvu.urlshortener.exceptions;

/** Ошибка, выбрасываемая при отсутствии оригинального URL по запрашиваемому короткому URL. */
public class OriginalLinkNotFoundException extends Exception {

  /** Ошибка, выбрасываемая при отсутствии оригинального URL по запрашиваемому короткому URL. */
  public OriginalLinkNotFoundException(String message) {
    super(message);
  }
}
