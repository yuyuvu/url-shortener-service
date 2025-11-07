package com.github.yuyuvu.urlshortener.domain.repository;

import com.github.yuyuvu.urlshortener.domain.model.Notification;
import java.util.List;

public interface NotificationRepository {
  List<Notification> getRepositoryAsList();
}
