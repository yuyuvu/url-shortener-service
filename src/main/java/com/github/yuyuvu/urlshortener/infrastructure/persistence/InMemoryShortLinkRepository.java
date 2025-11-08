package com.github.yuyuvu.urlshortener.infrastructure.persistence;

import com.github.yuyuvu.urlshortener.domain.model.ShortLink;
import com.github.yuyuvu.urlshortener.domain.repository.ShortLinkRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Реализация ShortLinkRepository на ConcurrentHashMap для хранения всех данных о коротких ссылках
 * внутри оперативной памяти.
 */
public class InMemoryShortLinkRepository implements ShortLinkRepository {

  ConcurrentHashMap<String, ShortLink> existingShortLinks = new ConcurrentHashMap<>();

  /** Конструктор для загрузки всех данных после перезапуска из StorageState. */
  public InMemoryShortLinkRepository(Map<String, ShortLink> existingShortLinks) {
    this.existingShortLinks.putAll(existingShortLinks);
  }

  /** Метод для сохранения новой короткой ссылки в хранилище. */
  @Override
  public ShortLink saveShortLink(ShortLink shortLink) {
    existingShortLinks.put(shortLink.getShortId(), shortLink);
    return shortLink;
  }

  /** Метод для получения всех коротких ссылок из хранилища. */
  @Override
  public List<ShortLink> getAllShortLinks() {
    return existingShortLinks.values().stream().toList();
  }

  /**
   * Метод для получения короткой ссылки из хранилища по shortID (код ссылки без URL сервиса).
   * Может также использоваться для проверки существования короткой ссылки.
   * */
  @Override
  public Optional<ShortLink> getShortLinkByShortID(String shortId) {
    return Optional.ofNullable(existingShortLinks.get(shortId));
  }

  /**
   * Метод для получения всех коротких ссылок, принадлежащих некоторому UUID.
   * Несмотря на необходимость перебора всего хранилища, позволяет избежать
   * дублирования данных о ссылках, которыми владеет пользователь, в его объекте.
   * */
  @Override
  public List<ShortLink> getShortLinksByOwnerUUID(UUID uuid) {
    return existingShortLinks.values().stream()
        .filter(shortLink -> shortLink.getOwnerOfShortURL().equals(uuid))
        .toList();
  }

  /** Метод для удаления короткой ссылки из хранилища по shortID. */
  @Override
  public boolean deleteShortLink(String shortId) {
    return existingShortLinks.remove(shortId) != null;
  }

  /**
   * Метод для получения всех коротких ссылок в формате ключ-значение (shortID - объект ссылки),
   * используется для сохранения данных во внешнее постоянное хранилище
   * (например, базу данных или файл).
   */
  @Override
  public Map<String, ShortLink> getRepositoryAsMap() {
    return existingShortLinks;
  }
}
