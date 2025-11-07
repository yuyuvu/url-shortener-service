package com.github.yuyuvu.urlshortener.cli.commands.impl;

import com.github.yuyuvu.urlshortener.application.LinkService;
import com.github.yuyuvu.urlshortener.cli.commands.CommandHandler;
import com.github.yuyuvu.urlshortener.cli.viewmodels.ViewModel;
import com.github.yuyuvu.urlshortener.cli.viewmodels.impl.ErrorViewModel;
import com.github.yuyuvu.urlshortener.cli.viewmodels.impl.StatsViewModel;
import com.github.yuyuvu.urlshortener.domain.model.ShortLink;
import com.github.yuyuvu.urlshortener.exceptions.InvalidShortLinkException;
import com.github.yuyuvu.urlshortener.exceptions.OriginalLinkNotFoundException;
import java.util.UUID;

public class StatsCommandHandler implements CommandHandler {
  LinkService linkService;

  public StatsCommandHandler(LinkService linkService) {
    this.linkService = linkService;
  }

  @Override
  public ViewModel handle(String[] commandArgs, UUID currentUserUUID) {
    if (currentUserUUID == null) {
      return new ErrorViewModel(
          "Нельзя просматривать статистику ссылок без предварительной идентификации по UUID. "
              + "Используйте команду: login ваш_UUID");
    }
    if (commandArgs.length == 1) {
      try {
        ShortLink shortLink = linkService.validateShortLinkExistence(commandArgs[0]);
        return new StatsViewModel(
            linkService.listShortLinksByUUID(currentUserUUID).stream()
                .filter(sl -> sl.getShortId().equals(shortLink.getShortId()))
                .toList(),
            true);
      } catch (OriginalLinkNotFoundException | InvalidShortLinkException e) {
        return new ErrorViewModel(e.getMessage());
      }
    } else {
      return new StatsViewModel(linkService.listShortLinksByUUID(currentUserUUID), false);
    }
  }
}
