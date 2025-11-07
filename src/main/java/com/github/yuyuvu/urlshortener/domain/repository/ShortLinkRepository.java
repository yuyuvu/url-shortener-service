package com.github.yuyuvu.urlshortener.domain.repository;

import com.github.yuyuvu.urlshortener.domain.model.ShortLink;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface ShortLinkRepository {
  ShortLink saveShortLink(ShortLink shortLink);

  List<ShortLink> getAllShortLinks();

  Optional<ShortLink> getShortLinkByShortID(String shortId);

  List<ShortLink> getShortLinksByOwnerUUID(UUID uuid);

  boolean deleteShortLink(String shortId);

  Map<String, ShortLink> getRepositoryAsMap();
}
