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

/** Обработчик команды delete, отвечающей за удаление имеющейся короткой ссылки сервиса. */
public class DeleteCommandHandler implements CommandHandler {
  private final LinkService linkService;
  private final UserService userService;

  /**
   * Конструктор обработчика команды delete, отвечающего за удаление имеющейся короткой ссылки
   * сервиса.
   */
  public DeleteCommandHandler(LinkService linkService, UserService userService) {
    this.linkService = linkService;
    this.userService = userService;
  }

  /**
   * Метод handle принимает аргументы для команды и UUID пользователя, который её вызвал, и пытается
   * исполнить логику команды delete.
   */
  @Override
  public ViewModel handle(String[] commandArgs, UUID currentUserUUID) {
    // Проверка передачи требуемого количества аргументов, иначе отправка сообщения с помощью по
    // команде
    if (commandArgs.length != 1) {
      return new ErrorViewModel(
          "Правильное использование команды: delete короткий_URL (указывайте протокол в URL). "
              + "\nВы указали недостаточное или избыточное количество аргументов.");
    }

    // Проверка выполнения команды от имени идентифицировавшегося пользователя
    if (currentUserUUID == null) {
      return new ErrorViewModel(
          "Нельзя использовать удаление ссылки без предварительной идентификации по UUID. "
              + "Используйте команду: login ваш_UUID");
    }

    try {
      // Попытка удаления, отправление сообщений с ошибками, если передана несуществующая
      // или удалённая короткая ссылка, или команда вызывается не владельцем короткой ссылки
      if (linkService.deleteShortLink(commandArgs[0], currentUserUUID)) {
        if (userService.getUserByUUID(currentUserUUID).isPresent()) {
          userService.getUserByUUID(currentUserUUID).get().decrementAmountOfMadeShortLinks();
        }
        return new SuccessViewModel("Вы успешно удалили короткую ссылку: " + commandArgs[0]);
      } else {
        return new ErrorViewModel(
            "Возникла непредвиденная ошибка при попытке удаления ссылки: " + commandArgs[0]);
      }
    } catch (OriginalLinkNotFoundException
        | InvalidShortLinkException
        | NotEnoughPermissionsException e) {
      return new ErrorViewModel(e.getMessage());
    }
  }
}
