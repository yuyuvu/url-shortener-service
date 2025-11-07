package com.github.yuyuvu.urlshortener.cli.commands.impl;

import com.github.yuyuvu.urlshortener.application.UserService;
import com.github.yuyuvu.urlshortener.cli.commands.CommandHandler;
import com.github.yuyuvu.urlshortener.cli.viewmodels.ViewModel;
import com.github.yuyuvu.urlshortener.cli.viewmodels.impl.ErrorViewModel;
import com.github.yuyuvu.urlshortener.cli.viewmodels.impl.SuccessViewModel;
import java.util.UUID;
import java.util.function.Consumer;

public class LoginCommandHandler implements CommandHandler {
  private final UserService userService;
  private final Consumer<UUID> onSuccessfulLoginDo;

  public LoginCommandHandler(UserService userService, Consumer<UUID> onSuccessfulLoginDo) {
    this.userService = userService;
    this.onSuccessfulLoginDo = onSuccessfulLoginDo;
  }

  @Override
  public ViewModel handle(String[] commandArgs, UUID currentUserUUID) {
    if (commandArgs.length != 1) {
      return new ErrorViewModel(
          "Правильное использование команды: login ваш_UUID. Вы указали недостаточное или избыточное количество аргументов.");
    }
    UUID uuid;
    try {
      uuid = UUID.fromString(commandArgs[0]);
    } catch (IllegalArgumentException e) {
      return new ErrorViewModel("Параметр UUID передан в некорректном формате.");
    }
    if (uuid.equals(currentUserUUID)) {
      return new ErrorViewModel("Вы уже идентифицированы с данным UUID");
    }
    if (userService.checkUserExistenceByUUID(uuid)) {
      onSuccessfulLoginDo.accept(uuid);
      return new SuccessViewModel("Вы успешно идентифицировались как " + uuid);
    } else {
      return new ErrorViewModel("Пользователь с данным UUID не найден. Укажите существующий UUID.");
    }
  }
}
