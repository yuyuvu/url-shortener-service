package com.github.yuyuvu.urlshortener.cli.commands.impl;

import com.github.yuyuvu.urlshortener.application.LinkService;
import com.github.yuyuvu.urlshortener.cli.commands.CommandHandler;
import com.github.yuyuvu.urlshortener.cli.viewmodels.ViewModel;
import com.github.yuyuvu.urlshortener.cli.viewmodels.impl.ErrorViewModel;
import com.github.yuyuvu.urlshortener.cli.viewmodels.impl.StatsViewModel;
import com.github.yuyuvu.urlshortener.cli.viewmodels.impl.SuccessViewModel;
import com.github.yuyuvu.urlshortener.domain.model.ShortLink;
import com.github.yuyuvu.urlshortener.exceptions.*;

import java.util.UUID;

public class ManageCommandHandler implements CommandHandler {
  LinkService linkService;

  public ManageCommandHandler(LinkService linkService) {
    this.linkService = linkService;
  }

  @Override
  public ViewModel handle(String[] commandArgs, UUID currentUserUUID) {
    if (commandArgs.length != 4) {
      return new ErrorViewModel(
          "Правильное использование команды: manage короткий_URL set параметр значение (указывайте протокол в URL)."
              + "\nВ качестве параметров можно установить:"
              + "\nlimit (изменение количества возможных переходов по ссылке);"
              + "\noriginal_url (изменение длинной ссылки, на которую ведёт короткая ссылка);"
              + "\nВ качестве значений можно установить:"
              + "\nдля limit - число (новое максимальное количество возможных переходов по ссылке);"
              + "\nдля original_url - URL (новый URL);"
              + "\nНапример: manage https://yush.ru/RXXbKU set limit 2 - изменит лимит переходов на 2 для указанной ссылки."
              + "\nВы указали недостаточное или избыточное количество аргументов.");
    }
    if (currentUserUUID == null) {
      return new ErrorViewModel(
          "Нельзя использовать управление ссылками без предварительной идентификации по UUID. "
              + "Используйте команду: login ваш_UUID");
    }
    if (!commandArgs[1].equalsIgnoreCase("set")) {
      return new ErrorViewModel(
          "Указан неизвестный параметр после URL. "
              + "Поддерживаемые параметры: set.");
    }
    try {
      String shortLinkFullURL = commandArgs[0];
      ShortLink shortLink = linkService.validateShortLinkExistence(shortLinkFullURL);
      switch (commandArgs[2].toLowerCase()) {
        case "limit" -> {
          int newLimit = Integer.parseInt(commandArgs[3]);
          linkService.changeShortLinkUsageLimit(shortLinkFullURL, currentUserUUID, newLimit);
          return new SuccessViewModel("Для ссылки " + shortLinkFullURL + " успешно задан новый лимит использований в " + newLimit + "!");
        }
        case "original_url" -> {
          linkService.validateURLFormat(commandArgs[3]);
          linkService.changeShortLinkOriginalURL(shortLinkFullURL, currentUserUUID, commandArgs[3]);
          return new SuccessViewModel("Для ссылки " + shortLinkFullURL + " успешно задан новый длинный URL для перехода: " + commandArgs[3] + "!");
        }
        default -> {
            return new ErrorViewModel(
                "Указан неизвестный аргумент к параметру set команды manage. "
                    + "Поддерживаемые аргументы: limit, original_url.");
        }
      }
    } catch (OriginalLinkNotFoundException | InvalidShortLinkException | InvalidOriginalLinkException | NotEnoughPermissionsException | IllegalCommandParameterException e) {
      return new ErrorViewModel(e.getMessage());
    } catch (NumberFormatException e) {
      return new ErrorViewModel("В качестве значения для параметра set limit передано не число.");
    }
  }
}
