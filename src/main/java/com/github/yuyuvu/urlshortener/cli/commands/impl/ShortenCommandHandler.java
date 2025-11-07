package com.github.yuyuvu.urlshortener.cli.commands.impl;

import com.github.yuyuvu.urlshortener.application.LinkService;
import com.github.yuyuvu.urlshortener.application.UserService;
import com.github.yuyuvu.urlshortener.cli.commands.CommandHandler;
import com.github.yuyuvu.urlshortener.cli.viewmodels.ViewModel;
import com.github.yuyuvu.urlshortener.cli.viewmodels.impl.CreatedLinkViewModel;
import com.github.yuyuvu.urlshortener.cli.viewmodels.impl.ErrorViewModel;
import com.github.yuyuvu.urlshortener.domain.model.ShortLink;
import com.github.yuyuvu.urlshortener.domain.model.User;
import com.github.yuyuvu.urlshortener.exceptions.InvalidOriginalLinkException;
import com.github.yuyuvu.urlshortener.infrastructure.config.ConfigManager;

import java.util.UUID;
import java.util.function.Consumer;

public class ShortenCommandHandler implements CommandHandler {
  LinkService linkService;
  UserService userService;
  ConfigManager configManager;
  Consumer<UUID> onNewUserCreationDo;

  public ShortenCommandHandler(LinkService linkService, UserService userService, ConfigManager configManager, Consumer<UUID> onNewUserCreationDo) {
    this.linkService = linkService;
    this.userService = userService;
    this.configManager = configManager;
    this.onNewUserCreationDo = onNewUserCreationDo;
  }

  @Override
  public ViewModel handle(String[] commandArgs, UUID currentUserUUID) {
    String originalURL = commandArgs[0];
    if (commandArgs.length != 1 || originalURL.isBlank()) {
      return new ErrorViewModel(
          "Правильное использование команды: shorten URL_для_сокращения (указывайте протокол в URL). "
              + "\nВы указали недостаточное или избыточное количество аргументов.");
    } else {
      try {
        linkService.validateURLFormat(originalURL);

        User user;
        boolean isNewUser = false;
        if (currentUserUUID != null && userService.getUserByUUID(currentUserUUID).isPresent()) {
          user = userService.getUserByUUID(currentUserUUID).get();
        } else {
          user = userService.makeNewUUIDAndUser();
          isNewUser = true;
        }
        ShortLink shortLink = linkService.makeNewShortLink(originalURL, user.getUUID());

        int linksPerUserLimit = configManager.getDefaultShortLinkMaxAmountPerUserProperty();
        if (user.getAmountOfMadeShortLinks() >= linksPerUserLimit) {
          return new ErrorViewModel("Вы достигли максимального количества созданных коротких ссылок на одного пользователя. Удалите или измените старые ссылки.");
        } else {
          user.incrementAmountOfMadeShortLinks();

          if (isNewUser) {
            userService.saveNewUser(user);
            this.onNewUserCreationDo.accept(user.getUUID());
          }

          linkService.saveNewShortLink(shortLink);
          String serviceBaseURL = configManager.getDefaultServiceBaseURLProperty();
          return new CreatedLinkViewModel(
              user.getUUID(), serviceBaseURL+shortLink.getShortId(),
              isNewUser, originalURL);
        }
      } catch (InvalidOriginalLinkException e) {
        return new ErrorViewModel(e.getMessage());
      }
    }
  }
}

