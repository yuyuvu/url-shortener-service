package com.github.yuyuvu.urlshortener.cli.commands.impl;

import com.github.yuyuvu.urlshortener.cli.commands.CommandHandler;
import com.github.yuyuvu.urlshortener.cli.viewmodels.ViewModel;
import com.github.yuyuvu.urlshortener.cli.viewmodels.impl.ErrorViewModel;
import com.github.yuyuvu.urlshortener.cli.viewmodels.impl.SuccessViewModel;

import java.util.UUID;

public class HelpCommandHandler implements CommandHandler {

  @Override
  public ViewModel handle(String[] commandArgs, UUID currentUserUUID) {
    if (commandArgs.length > 1) {
      return new ErrorViewModel(
          "Правильное использование команды: help [опционально_имя_команды_сервиса]."
              + "\nВы указали избыточное количество аргументов.");
    }
    if (commandArgs.length == 1) {
      String commandName =  commandArgs[0].toLowerCase();
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
        case "shorten"  -> {
          return new SuccessViewModel("Команда shorten");
        }
        case "exit"  -> {
          return new SuccessViewModel("Команда exit");
        }
        case "login" -> {
          return new SuccessViewModel("Команда login");
        }
        case "logout" -> {
          return new SuccessViewModel("Команда logout");
        }
        default -> {
          return new ErrorViewModel("Запрошенная команда " +  commandName + " не поддерживается сервисом.");
        }
      }
    } else {
      return new SuccessViewModel("""
          Помощь по сервису сокращения ссылок:
          ...
          """);
    }
  }
}
