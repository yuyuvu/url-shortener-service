package com.github.yuyuvu.urlshortener.infrastructure.scheduler;

import com.github.yuyuvu.urlshortener.application.LinkService;
import com.github.yuyuvu.urlshortener.application.NotificationService;
import com.github.yuyuvu.urlshortener.application.UserService;
import com.github.yuyuvu.urlshortener.domain.model.Notification;
import com.github.yuyuvu.urlshortener.domain.model.ShortLink;

import java.util.List;

public class LinkCleanupAndMakeNotificationsTask implements Runnable {
  LinkService linkService;
  NotificationService notificationService;

  public LinkCleanupAndMakeNotificationsTask(LinkService linkService, NotificationService notificationService) {
    this.linkService = linkService;
    this.notificationService = notificationService;
  }

  @Override
  public void run() {
    List<ShortLink> allServiceLinks = linkService.listAllShortLinks();
    for (ShortLink shortLink : allServiceLinks) {
      if (shortLink.isExpired()) {
        Notification notification = notificationService.makeNewShortLinkExpiredNotification(shortLink);
        notificationService.saveNewNotification(notification);
        linkService.uncheckedDeleteShortLinkByShortId(shortLink.getShortId());
      }
      if (shortLink.isLimitReached() && !shortLink.isLimitNotified()) {
        Notification notification = notificationService.makeNewShortLinkLimitReachedNotification(shortLink);
        notificationService.saveNewNotification(notification);
        shortLink.setLimitNotified(true);
      }
    }
    for (Notification notification : notificationService.listAllNotifications()) {
      if (notification.isRead()) {
        notificationService.deleteNotification(notification);
      }
    }
  }
}
