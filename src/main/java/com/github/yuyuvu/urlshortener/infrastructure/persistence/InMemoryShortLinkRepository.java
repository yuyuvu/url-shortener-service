package com.github.yuyuvu.urlshortener.infrastructure.persistence;

import com.github.yuyuvu.urlshortener.domain.model.ShortLink;
import com.github.yuyuvu.urlshortener.domain.repository.ShortLinkRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryShortLinkRepository implements ShortLinkRepository {

  ConcurrentHashMap<String, ShortLink> existingShortLinks = new ConcurrentHashMap<>();

  public InMemoryShortLinkRepository(Map<String, ShortLink> existingShortLinks) {
    this.existingShortLinks.putAll(existingShortLinks);
  }

  @Override
  public ShortLink saveShortLink(ShortLink shortLink) {
    existingShortLinks.put(shortLink.getShortId(), shortLink);
    return shortLink;
  }

  @Override
  public List<ShortLink> getAllShortLinks() {
    return existingShortLinks.values().stream().toList();
  }

  @Override
  public Optional<ShortLink> getShortLinkByShortID(String shortId) {
    return Optional.ofNullable(existingShortLinks.get(shortId));
  }

  @Override
  public List<ShortLink> getShortLinksByOwnerUUID(UUID uuid) {
    return existingShortLinks.values().stream()
        .filter(shortLink -> shortLink.getOwnerOfShortURL().equals(uuid))
        .toList();
  }

  @Override
  public boolean deleteShortLink(String shortId) {
    return existingShortLinks.remove(shortId) != null;
  }

  @Override
  public Map<String, ShortLink> getRepositoryAsMap() {
    return existingShortLinks;
  }
}
