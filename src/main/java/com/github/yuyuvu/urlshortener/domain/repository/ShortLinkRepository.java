package com.github.yuyuvu.urlshortener.domain.repository;

import com.github.yuyuvu.urlshortener.domain.model.ShortLink;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** Интерфейс хранилища созданных в сервисе коротких ссылок. */
public interface ShortLinkRepository {

  /** Метод для сохранения новой короткой ссылки в хранилище. */
  ShortLink saveShortLink(ShortLink shortLink);

  /** Метод для получения всех коротких ссылок из хранилища. */
  List<ShortLink> getAllShortLinks();

  /**
   * Метод для получения короткой ссылки из хранилища по shortID (код ссылки без URL сервиса).
   * Может также использоваться для проверки существования короткой ссылки.
   * */
  Optional<ShortLink> getShortLinkByShortID(String shortId);

  /**
   * Метод для получения всех коротких ссылок, принадлежащих некоторому UUID.
   * Несмотря на необходимость перебора всего хранилища, позволяет избежать
   * дублирования данных о ссылках, которыми владеет пользователь, в его объекте.
   * */
  List<ShortLink> getShortLinksByOwnerUUID(UUID uuid);

  /** Метод для удаления короткой ссылки из хранилища по shortID. */
  boolean deleteShortLink(String shortId);

  /**
   * Метод для получения всех коротких ссылок в формате ключ-значение (shortID - объект ссылки),
   * используется для сохранения данных во внешнее постоянное хранилище
   * (например, базу данных или файл).
   */
  Map<String, ShortLink> getRepositoryAsMap();
}
