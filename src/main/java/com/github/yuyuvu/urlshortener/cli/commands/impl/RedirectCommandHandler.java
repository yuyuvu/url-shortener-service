package com.github.yuyuvu.urlshortener.cli.commands.impl;

import com.github.yuyuvu.urlshortener.application.LinkService;
import com.github.yuyuvu.urlshortener.cli.commands.CommandHandler;
import com.github.yuyuvu.urlshortener.cli.viewmodels.ViewModel;
import com.github.yuyuvu.urlshortener.cli.viewmodels.impl.ErrorViewModel;
import com.github.yuyuvu.urlshortener.cli.viewmodels.impl.SuccessViewModel;
import com.github.yuyuvu.urlshortener.exceptions.InvalidOriginalLinkException;
import com.github.yuyuvu.urlshortener.exceptions.InvalidShortLinkException;
import com.github.yuyuvu.urlshortener.exceptions.OriginalLinkNotFoundException;
import com.github.yuyuvu.urlshortener.exceptions.UsagesLimitReachedException;
import java.io.IOException;
import java.util.UUID;

public class RedirectCommandHandler implements CommandHandler {
  LinkService linkService;

  public RedirectCommandHandler(LinkService linkService) {
    this.linkService = linkService;
  }

  @Override
  public ViewModel handle(String[] commandArgs, UUID currentUserUUID) {
    String shortLinkURL = commandArgs[0];
    if (commandArgs.length != 1 || shortLinkURL.isBlank()) {
      return new ErrorViewModel(
          "Вы не ввели команду или URL для сокращения. Предоставленный ввод не распознан. Введите help для помощи по сервису.");
    } else {
      try {
        linkService.validateURLFormat(shortLinkURL);
      } catch (InvalidOriginalLinkException e) {
        return new ErrorViewModel(
            """
            Предоставленный ввод не распознан в качестве команды или URL.
            Введите help для получения помощи по сервису.
            Если вы желаете ввести URL для сокращения, \
            то указывайте его вместе с протоколом (http://, https://).""");
      }
      try {
        String originalURLAddress = linkService.redirectByShortLink(shortLinkURL);
        return new SuccessViewModel("Перенаправление на " + originalURLAddress + " ...");
      } catch (OriginalLinkNotFoundException
          | IOException
          | InvalidShortLinkException
          | InvalidOriginalLinkException
          | UsagesLimitReachedException e) {
        return new ErrorViewModel(e.getMessage());
      }
    }
  }
}
