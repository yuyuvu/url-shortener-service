package com.github.yuyuvu.urlshortener.cli.commands.impl;

import com.github.yuyuvu.urlshortener.application.LinkService;
import com.github.yuyuvu.urlshortener.cli.commands.CommandHandler;
import com.github.yuyuvu.urlshortener.cli.viewmodels.ViewModel;
import com.github.yuyuvu.urlshortener.cli.viewmodels.impl.ErrorViewModel;
import com.github.yuyuvu.urlshortener.cli.viewmodels.impl.ListViewModel;
import java.util.UUID;

public class ListCommandHandler implements CommandHandler {
  LinkService linkService;

  public ListCommandHandler(LinkService linkService) {
    this.linkService = linkService;
  }

  @Override
  public ViewModel handle(String[] commandArgs, UUID currentUserUUID) {
    if (currentUserUUID == null) {
      return new ErrorViewModel(
          "Нельзя просматривать список созданных ссылок без предварительной идентификации по UUID. "
              + "Используйте команду: login ваш_UUID");
    }
    return new ListViewModel(linkService.listShortLinksByUUID(currentUserUUID));
  }
}
