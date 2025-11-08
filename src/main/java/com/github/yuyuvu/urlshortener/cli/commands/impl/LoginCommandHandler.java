package com.github.yuyuvu.urlshortener.cli.commands.impl;

import com.github.yuyuvu.urlshortener.application.UserService;
import com.github.yuyuvu.urlshortener.cli.commands.CommandHandler;
import com.github.yuyuvu.urlshortener.cli.viewmodels.ViewModel;
import com.github.yuyuvu.urlshortener.cli.viewmodels.impl.ErrorViewModel;
import com.github.yuyuvu.urlshortener.cli.viewmodels.impl.SuccessViewModel;
import java.util.UUID;
import java.util.function.Consumer;

/** Обработчик команды login, отвечающей за идентификацию пользователя по некоторому UUID. */
public class LoginCommandHandler implements CommandHandler {
  private final UserService userService;
  private final Consumer<UUID> onSuccessfulLoginDo;

  /**
   * Конструктор обработчика команды login, отвечающего за идентификацию пользователя по некоторому
   * UUID.
   */
  public LoginCommandHandler(UserService userService, Consumer<UUID> onSuccessfulLoginDo) {
    this.userService = userService;
    this.onSuccessfulLoginDo = onSuccessfulLoginDo;
  }

  /**
   * Метод handle принимает аргументы для команды и UUID пользователя, который её вызвал, и пытается
   * идентифицировать пользователя по некоторому UUID.
   */
  @Override
  public ViewModel handle(String[] commandArgs, UUID currentUserUUID) {
    // Проверка передачи требуемого количества аргументов, иначе отправка сообщения с помощью по
    // команде
    if (commandArgs.length != 1) {
      return new ErrorViewModel(
          "Правильное использование команды: login ваш_UUID. "
              + "Вы указали недостаточное или избыточное количество аргументов.");
    }

    UUID uuid;
    // Проверка передачи валидного по формату UUID
    try {
      uuid = UUID.fromString(commandArgs[0]);
    } catch (IllegalArgumentException e) {
      return new ErrorViewModel("Параметр UUID передан в некорректном формате.");
    }

    // Проверка, нет ли уже имеющейся идентификации данного пользователя по данному UUID
    if (uuid.equals(currentUserUUID)) {
      return new ErrorViewModel("Вы уже идентифицированы с данным UUID");
    }

    // Проверка, существует ли переданный UUID в сервисе
    // Далее вызов коллбэка из ConsoleController, хранящего данные о текущей сессии и UUID, для
    // login
    if (userService.checkUserExistenceByUUID(uuid)) {
      onSuccessfulLoginDo.accept(uuid);
      return new SuccessViewModel("Вы успешно идентифицировались как " + uuid);
    } else {
      return new ErrorViewModel("Пользователь с данным UUID не найден. Укажите существующий UUID.");
    }
  }
}
