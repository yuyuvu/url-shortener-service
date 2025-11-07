package com.github.yuyuvu.urlshortener.cli.commands.impl;

import com.github.yuyuvu.urlshortener.application.LinkService;
import com.github.yuyuvu.urlshortener.application.UserService;
import com.github.yuyuvu.urlshortener.cli.commands.CommandHandler;
import com.github.yuyuvu.urlshortener.cli.viewmodels.ViewModel;
import com.github.yuyuvu.urlshortener.cli.viewmodels.impl.ErrorViewModel;
import com.github.yuyuvu.urlshortener.cli.viewmodels.impl.SuccessViewModel;
import com.github.yuyuvu.urlshortener.exceptions.InvalidShortLinkException;
import com.github.yuyuvu.urlshortener.exceptions.NotEnoughPermissionsException;
import com.github.yuyuvu.urlshortener.exceptions.OriginalLinkNotFoundException;

import java.util.UUID;

public class DeleteCommandHandler implements CommandHandler {
  LinkService linkService;
  UserService userService;

  public DeleteCommandHandler(LinkService linkService, UserService userService) {
    this.linkService = linkService;
    this.userService = userService;
  }

  @Override
  public ViewModel handle(String[] commandArgs, UUID currentUserUUID) {
    if (commandArgs.length != 1) {
      return new ErrorViewModel(
          "Правильное использование команды: delete короткий_URL (указывайте протокол в URL). "
              + "\nВы указали недостаточное или избыточное количество аргументов.");
    }
    if (currentUserUUID == null) {
      return new ErrorViewModel("Нельзя использовать удаление ссылки без предварительной идентификации по UUID. "
          + "Используйте login ваш_UUID");
    }
    try {
      if (linkService.deleteShortLink(commandArgs[0], currentUserUUID)) {
        if (userService.getUserByUUID(currentUserUUID).isPresent()) {
          userService.getUserByUUID(currentUserUUID).get().decrementAmountOfMadeShortLinks();
        }
        return new SuccessViewModel("Вы успешно удалили короткую ссылку: " + commandArgs[0]);
      } else {
        return new ErrorViewModel("Возникла непредвиденная ошибка при попытке удаления ссылки: " + commandArgs[0]);
      }
    } catch (OriginalLinkNotFoundException | InvalidShortLinkException | NotEnoughPermissionsException e) {
      return new ErrorViewModel(e.getMessage());
    }
  }
}
