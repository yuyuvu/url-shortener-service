package com.github.yuyuvu.urlshortener.cli.commands.impl;

import com.github.yuyuvu.urlshortener.cli.commands.CommandHandler;
import com.github.yuyuvu.urlshortener.cli.viewmodels.ViewModel;
import com.github.yuyuvu.urlshortener.cli.viewmodels.impl.ErrorViewModel;
import com.github.yuyuvu.urlshortener.cli.viewmodels.impl.SuccessViewModel;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Обработчик команды logout, отвечающей за снятие идентификации пользователя по некоторому UUID.
 */
public class LogoutCommandHandler implements CommandHandler {
  private final Consumer<UUID> onSuccessfulLogoutDo;

  /**
   * Конструктор обработчика команды logout, отвечающего за снятие идентификации пользователя по
   * некоторому UUID.
   */
  public LogoutCommandHandler(Consumer<UUID> onSuccessfulLogoutDo) {
    this.onSuccessfulLogoutDo = onSuccessfulLogoutDo;
  }

  /**
   * Метод handle принимает аргументы для команды и UUID пользователя, который её вызвал, и пытается
   * снять идентификацию пользователя по некоторому UUID.
   */
  @Override
  public ViewModel handle(String[] commandArgs, UUID currentUserUUID) {
    // Проверка выполнения команды от имени идентифицировавшегося пользователя
    if (currentUserUUID == null) {
      return new ErrorViewModel(
          "Вы не можете использовать данную команду, так как ещё не идентифицировались в системе.");
    }

    // Вызываем коллбэк из ConsoleController, хранящего данные о текущей сессии и UUID, для logout
    onSuccessfulLogoutDo.accept(null);
    return new SuccessViewModel("Вы перестали идентифицироваться с UUID " + currentUserUUID);
  }
}
