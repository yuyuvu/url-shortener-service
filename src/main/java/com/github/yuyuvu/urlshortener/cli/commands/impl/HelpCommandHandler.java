package com.github.yuyuvu.urlshortener.cli.commands.impl;

import com.github.yuyuvu.urlshortener.cli.commands.CommandHandler;
import com.github.yuyuvu.urlshortener.cli.viewmodels.ViewModel;
import com.github.yuyuvu.urlshortener.cli.viewmodels.impl.ErrorViewModel;
import com.github.yuyuvu.urlshortener.cli.viewmodels.impl.SuccessViewModel;
import java.util.UUID;

/**
 * Обработчик команды help, отвечающей за вывод помощи по всему сервису или только по одной
 * отдельной команде.
 */
public class HelpCommandHandler implements CommandHandler {

  /**
   * Метод handle принимает аргументы для команды и UUID пользователя, который её вызвал, и выводит
   * помощь по сервису.
   */
  @Override
  public ViewModel handle(String[] commandArgs, UUID currentUserUUID) {
    // Проверка передачи требуемого количества аргументов, иначе отправка сообщения с помощью по
    // команде
    if (commandArgs.length > 1) {
      return new ErrorViewModel(
          "Правильное использование команды: help или help [и_опционально_имя_команды_сервиса]."
              + "\nВы указали избыточное количество аргументов.");
    }

    // Проверка запроса помощи по одной конкретной команде
    if (commandArgs.length == 1) {
      String commandName = commandArgs[0].toLowerCase();
      switch (commandName) {
        case "list" -> {
          return new SuccessViewModel("Команда list");
        }
        case "stats" -> {
          return new SuccessViewModel("Команда stats");
        }
        case "manage" -> {
          return new SuccessViewModel("Команда manage");
        }
        case "help" -> {
          return new SuccessViewModel("Команда help");
        }
        case "delete" -> {
          return new SuccessViewModel("Команда delete");
        }
        case "exit" -> {
          return new SuccessViewModel("Команда exit");
        }
        case "login" -> {
          return new SuccessViewModel("Команда login");
        }
        case "logout" -> {
          return new SuccessViewModel("Команда logout");
        }
        case "config" -> {
          return new SuccessViewModel("Команда config");
        }
        default -> {
          return new ErrorViewModel(
              "Запрошенная команда " + commandName + " не поддерживается сервисом.");
        }
      }
    } else {
      // Иначе вывод помощи по всему сервису
      return new SuccessViewModel(
          """
          Помощь по сервису сокращения ссылок:
          ...
          """);
    }
  }
}
