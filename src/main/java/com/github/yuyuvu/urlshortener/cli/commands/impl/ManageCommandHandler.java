package com.github.yuyuvu.urlshortener.cli.commands.impl;

import com.github.yuyuvu.urlshortener.application.LinkService;
import com.github.yuyuvu.urlshortener.cli.commands.CommandHandler;
import com.github.yuyuvu.urlshortener.cli.viewmodels.ViewModel;
import com.github.yuyuvu.urlshortener.cli.viewmodels.impl.ErrorViewModel;
import com.github.yuyuvu.urlshortener.cli.viewmodels.impl.SuccessViewModel;
import com.github.yuyuvu.urlshortener.exceptions.IllegalCommandParameterException;
import com.github.yuyuvu.urlshortener.exceptions.InvalidOriginalLinkException;
import com.github.yuyuvu.urlshortener.exceptions.InvalidShortLinkException;
import com.github.yuyuvu.urlshortener.exceptions.NotEnoughPermissionsException;
import com.github.yuyuvu.urlshortener.exceptions.OriginalLinkNotFoundException;
import com.github.yuyuvu.urlshortener.infrastructure.config.ConfigManager;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

/**
 * Обработчик команды manage, отвечающей за изменение параметров короткой ссылки, согласно запросам
 * пользователя.
 */
public class ManageCommandHandler implements CommandHandler {
  private final LinkService linkService;
  private final ConfigManager configManager;

  /**
   * Конструктор обработчика команды manage, отвечающего за изменение параметров короткой ссылки,
   * согласно запросам пользователя.
   */
  public ManageCommandHandler(LinkService linkService, ConfigManager configManager) {
    this.linkService = linkService;
    this.configManager = configManager;
  }

  /**
   * Метод handle принимает аргументы для команды и UUID пользователя, который её вызвал, и пытается
   * изменить параметры короткой ссылки, согласно запросам пользователя.
   */
  @Override
  public ViewModel handle(String[] commandArgs, UUID currentUserUUID) {
    // Проверка передачи требуемого количества аргументов, иначе отправка сообщения с помощью по
    // команде
    if (commandArgs.length < 4) {
      return new ErrorViewModel(
          "Правильное использование команды: manage короткий_URL set параметр значение "
              + "(указывайте протокол в URL)."
              + "\n\nВ качестве параметров можно установить:"
              + "\nlimit (изменение количества возможных переходов по ссылке);"
              + "\noriginal_url (изменение длинной ссылки, на которую ведёт короткая ссылка);"
              + "\nttl (изменение срока жизни ссылки, после которого она перестанет действовать "
              + "и удалится);"
              + "\n\nВ качестве значений можно установить:"
              + "\nдля limit - число (новое максимальное количество возможных переходов по ссылке);"
              + "\nдля original_url - URL (новый URL);"
              + String.format(
                  "\nдля ttl - число (единица измерения: %s), которое нужно прибавить "
                      + "к времени создания ссылки,",
                  configManager.getDefaultShortLinkTTLTimeUnitProperty().key())
              + "\nчтобы получилось новое время удаления ссылки, смотрите дату и время создания "
              + "ссылки в команде list;"
              + "\n\nПример использования 1: manage https://yulink.tech/RXXbKU set limit 2 - изменит лимит переходов на 2 для указанной ссылки."
              + "\nПример использования 2: manage https://yulink.tech/RXXbKU set ttl 50 - изменит время удаления ссылки на"
              + String.format(
                  "\n(время создания + 50 единиц измерения времени), единица измерения: %s.",
                  configManager.getDefaultShortLinkTTLTimeUnitProperty().key())
              + "\nОбратите внимание: нельзя задать новое время удаления "
              + "до текущего момента времени или лимит "
              + "\nиспользований меньше текущего количества использований."
              + "\nСейчас вы указали недостаточное или избыточное количество аргументов.");
    }

    // Проверка выполнения команды от имени идентифицировавшегося пользователя
    if (currentUserUUID == null) {
      return new ErrorViewModel(
          "Нельзя использовать управление ссылками без предварительной идентификации по UUID. "
              + "Используйте команду: login ваш_UUID");
    }

    // Проверяем наличие ожидаемого аргумента (set) в нужной позиции
    if (!commandArgs[1].equalsIgnoreCase("set")) {
      return new ErrorViewModel(
          "Указан неизвестный параметр после URL. " + "Поддерживаемые параметры: set.");
    }
    try {
      String shortLinkFullURL = commandArgs[0];
      // Проверяем, что запрошено управление активной короткой ссылкой нашего сервиса
      linkService.validateShortLinkExistence(shortLinkFullURL);

      // Проверяем наличие одного из ожидаемых аргументов (limit, original_url, ttl) в нужной
      // позиции
      switch (commandArgs[2].toLowerCase()) {
        case "limit" -> {
          int newLimit = Integer.parseInt(commandArgs[3]);
          // Вызываем соответствующий метод в LinkService
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
          // Вызываем соответствующий метод в LinkService
          linkService.changeShortLinkOriginalURL(shortLinkFullURL, currentUserUUID, commandArgs[3]);
          return new SuccessViewModel(
              "Для ссылки "
                  + shortLinkFullURL
                  + " успешно задан новый длинный URL для перехода: "
                  + commandArgs[3]
                  + "!");
        }
        case "ttl" -> {
          int addTTLInUnitsToCreationTime = Integer.parseInt(commandArgs[3]);
          // Вызываем соответствующий метод в LinkService
          LocalDateTime newTTL =
              linkService.changeShortLinkTTLExpirationLimit(
                  shortLinkFullURL, currentUserUUID, addTTLInUnitsToCreationTime);
          return new SuccessViewModel(
              "Для ссылки "
                  + shortLinkFullURL
                  + " успешно задан новый срок действия, теперь ссылка "
                  + "перестанет действовать и будет удалена в "
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
