package com.github.yuyuvu.urlshortener.cli.commands.impl;

import com.github.yuyuvu.urlshortener.application.LinkService;
import com.github.yuyuvu.urlshortener.cli.commands.CommandHandler;
import com.github.yuyuvu.urlshortener.cli.viewmodels.ViewModel;
import com.github.yuyuvu.urlshortener.cli.viewmodels.impl.ErrorViewModel;
import com.github.yuyuvu.urlshortener.cli.viewmodels.impl.SuccessViewModel;
import com.github.yuyuvu.urlshortener.domain.model.ShortLink;
import com.github.yuyuvu.urlshortener.exceptions.*;
import com.github.yuyuvu.urlshortener.infrastructure.config.ConfigManager;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

public class ManageCommandHandler implements CommandHandler {
  LinkService linkService;
  ConfigManager configManager;

  public ManageCommandHandler(LinkService linkService, ConfigManager configManager) {
    this.linkService = linkService;
    this.configManager = configManager;
  }

  @Override
  public ViewModel handle(String[] commandArgs, UUID currentUserUUID) {
    if (commandArgs.length < 4) {
      return new ErrorViewModel(
          "Правильное использование команды: manage короткий_URL set параметр значение (указывайте протокол в URL)."
              + "\n\nВ качестве параметров можно установить:"
              + "\nlimit (изменение количества возможных переходов по ссылке);"
              + "\noriginal_url (изменение длинной ссылки, на которую ведёт короткая ссылка);"
              + "\nttl (изменение срока жизни ссылки, после которого она перестанет действовать и удалится);"
              + "\n\nВ качестве значений можно установить:"
              + "\nдля limit - число (новое максимальное количество возможных переходов по ссылке);"
              + "\nдля original_url - URL (новый URL);"
              + String.format(
                  "\nдля ttl - число (единица измерения: %s), которое нужно прибавить к времени создания ссылки,",
                  configManager.getDefaultShortLinkTTLTimeUnitProperty().key())
              + "\nчтобы получилось новое время удаления ссылки, смотрите дату и время создания ссылки в команде list;"
              + "\n\nПример использования 1: manage https://yush.ru/RXXbKU set limit 2 - изменит лимит переходов на 2 для указанной ссылки."
              + "\nПример использования 2: manage https://yush.ru/RXXbKU set ttl 50 - изменит время удаления ссылки на"
              + String.format(
                  "\n(время создания + 50 единиц измерения времени), единица измерения: %s.",
                  configManager.getDefaultShortLinkTTLTimeUnitProperty().key())
              + "\nОбратите внимание: нельзя задать новое время удаления до текущего момента времени или лимит "
              + "\nиспользований меньше текущего количества использований."
              + "\nСейчас вы указали недостаточное или избыточное количество аргументов.");
    }
    if (currentUserUUID == null) {
      return new ErrorViewModel(
          "Нельзя использовать управление ссылками без предварительной идентификации по UUID. "
              + "Используйте команду: login ваш_UUID");
    }
    if (!commandArgs[1].equalsIgnoreCase("set")) {
      return new ErrorViewModel(
          "Указан неизвестный параметр после URL. " + "Поддерживаемые параметры: set.");
    }
    try {
      String shortLinkFullURL = commandArgs[0];
      ShortLink shortLink = linkService.validateShortLinkExistence(shortLinkFullURL);
      switch (commandArgs[2].toLowerCase()) {
        case "limit" -> {
          int newLimit = Integer.parseInt(commandArgs[3]);
          linkService.changeShortLinkUsageLimit(shortLinkFullURL, currentUserUUID, newLimit);
          return new SuccessViewModel(
              "Для ссылки "
                  + shortLinkFullURL
                  + " успешно задан новый лимит использований в "
                  + newLimit
                  + "!");
        }
        case "original_url" -> {
          linkService.validateURLFormat(commandArgs[3]);
          linkService.changeShortLinkOriginalURL(shortLinkFullURL, currentUserUUID, commandArgs[3]);
          return new SuccessViewModel(
              "Для ссылки "
                  + shortLinkFullURL
                  + " успешно задан новый длинный URL для перехода: "
                  + commandArgs[3]
                  + "!");
        }
        case "ttl" -> {
          // TODO: реализовать соответствующий метод в linkService
          int addTTLInUnitsToCreationTime = Integer.parseInt(commandArgs[3]);
          LocalDateTime newTTL =
              linkService.changeShortLinkTTLExpirationLimit(
                  shortLinkFullURL, currentUserUUID, addTTLInUnitsToCreationTime);
          return new SuccessViewModel(
              "Для ссылки "
                  + shortLinkFullURL
                  + " успешно задан новый срок действия, теперь ссылка перестанет действовать и будет удалена в "
                  + newTTL.format(
                      DateTimeFormatter.ofPattern("E dd.MM.uuuu HH:mm")
                          .withLocale(Locale.forLanguageTag("ru-RU")))
                  + "!");
        }
        default -> {
          return new ErrorViewModel(
              "Указан неизвестный аргумент к параметру set команды manage. "
                  + "Поддерживаемые аргументы: limit, original_url, ttl.");
        }
      }
    } catch (OriginalLinkNotFoundException
        | InvalidShortLinkException
        | InvalidOriginalLinkException
        | NotEnoughPermissionsException
        | IllegalCommandParameterException e) {
      return new ErrorViewModel(e.getMessage());
    } catch (NumberFormatException e) {
      return new ErrorViewModel(
          "В качестве значения для параметра set limit или set ttl передано не число.");
    }
  }
}
