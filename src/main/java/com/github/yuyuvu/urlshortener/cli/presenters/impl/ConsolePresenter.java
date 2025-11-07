package com.github.yuyuvu.urlshortener.cli.presenters.impl;

import static com.github.yuyuvu.urlshortener.cli.presenters.ColorPrinter.*;

import com.github.yuyuvu.urlshortener.cli.presenters.Presenter;
import com.github.yuyuvu.urlshortener.cli.viewmodels.ViewModel;
import com.github.yuyuvu.urlshortener.cli.viewmodels.impl.*;
import com.github.yuyuvu.urlshortener.domain.model.Notification;
import com.github.yuyuvu.urlshortener.domain.model.ShortLink;
import com.github.yuyuvu.urlshortener.infrastructure.config.ConfigManager;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Locale;
import java.util.stream.Collectors;

public class ConsolePresenter implements Presenter {
  ConfigManager configManager;

  public ConsolePresenter(ConfigManager configManager) {
    this.configManager = configManager;
  }

  public void present(ViewModel result) {
    if (result instanceof CreatedLinkViewModel model) {
      presentCreatedLink(model);
    } else if (result instanceof ErrorViewModel model) {
      presentErrorViewModel(model);
    } else if (result instanceof SuccessViewModel model) {
      presentSuccessViewModel(model);
    } else if (result instanceof ListViewModel model) {
      presentListViewModel(model);
    } else if (result instanceof StatsViewModel model) {
      presentStatsViewModel(model);
    } else if (result instanceof NotificationsViewModel model) {
      presentNotificationsViewModel(model);
    }
  }

  private void presentErrorViewModel(ErrorViewModel model) {
    printlnRed("Ошибка: " + model.errorMessage);
  }

  private void presentSuccessViewModel(SuccessViewModel model) {
    printlnYellow(model.message);
  }

  private void presentCreatedLink(CreatedLinkViewModel model) {
    if (!model.isNewUser) {
      printlnGreen("Вы успешно создали новую короткую ссылку на " + model.originalURL + "!");
      printlnGreen("URL вашей короткой ссылки: " + model.shortURL);
    } else {
      printlnGreen("Вы успешно создали новую короткую ссылку на " + model.originalURL + "!");
      printlnGreen("URL вашей короткой ссылки: " + model.shortURL);
      printlnYellow("Это ваша первая созданная ссылка!");
      printlnCyan(
          "Для вас создан новый UUID для последующего управления созданными ссылками: "
              + model.creatorUUID);
      printlnCyan("На время данной сессии вы автоматически идентифицированы под данным UUID.");
      printlnCyan("Обязательно надёжно зафиксируйте данный UUID на будущее.");
      printlnCyan(
          "При повторном подключении к сервису используйте команду (login ваш_UUID) для идентификации.");
    }
  }

  private void presentListViewModel(ListViewModel model) {
    if (model.shortLinks.isEmpty()) {
      printlnYellow("На текущий момент нет созданных вами активных коротких ссылок.");
    } else {
      printlnGreen("Принадлежащие вам активные короткие ссылки:");
      String serviceBaseURL = configManager.getDefaultServiceBaseURLProperty();
      for (ShortLink shortLink : model.shortLinks) {
        printlnCyan(
            "\t - "
                + serviceBaseURL
                + shortLink.getShortId()
                + " - ведёт на: "
                + shortLink.getOriginalURLAddress()
                + ", время создания: "
                + shortLink
                    .getCreationDateTime()
                    .format(
                        DateTimeFormatter.ofPattern("E dd.MM.uuuu HH:mm")
                            .withLocale(Locale.forLanguageTag("ru-RU")))
                + ", истекает в: "
                + shortLink
                    .getExpirationDateTime()
                    .format(
                        DateTimeFormatter.ofPattern("E dd.MM.uuuu HH:mm")
                            .withLocale(Locale.forLanguageTag("ru-RU")))
                + ", лимит использований: "
                + shortLink.getUsageLimitAmount()
                + ".");
      }
    }
  }

  private void presentStatsViewModel(StatsViewModel model) {
    String serviceBaseURL = configManager.getDefaultServiceBaseURLProperty();
    if (model.isSingle) {
      if (model.shortLinks.isEmpty()) {
        printlnRed("Ошибка: Данной короткой ссылки не существует или она вам не принадлежит.");
      } else {
        printlnYellow(
            "Статистика по ссылке: " + serviceBaseURL + model.shortLinks.get(0).getShortId());
        printlnYellow("--------------------------------------------------------");
        printlnYellow("Лимит использований: " + model.shortLinks.get(0).getUsageLimitAmount());
        printlnYellow(
            "Всего фактических использований: "
                + model.shortLinks.get(0).getUsageCounter()
                + "/"
                + model.shortLinks.get(0).getUsageLimitAmount());
        printlnYellow(
            "Истекает в: "
                + model
                    .shortLinks
                    .get(0)
                    .getExpirationDateTime()
                    .format(
                        DateTimeFormatter.ofPattern("E dd.MM.uuuu HH:mm")
                            .withLocale(Locale.forLanguageTag("ru-RU"))));
        printlnYellow("Ведёт на ULR: " + model.shortLinks.get(0).getOriginalURLAddress());
      }
    } else {
      if (model.shortLinks.isEmpty()) {
        printlnYellow("На текущий момент нет созданных вами активных коротких ссылок.");
      } else {
        printlnGreen("Статистика использований по принадлежащим вам активным коротким ссылкам:");
        ArrayList<ShortLink> sortedLinks =
            model.shortLinks.stream()
                .sorted(Comparator.comparingInt(ShortLink::getUsageCounter).reversed())
                .collect(Collectors.toCollection(ArrayList::new));
        for (ShortLink shortLink : sortedLinks) {
          printlnCyan(
              "\t - "
                  + serviceBaseURL
                  + shortLink.getShortId()
                  + " - всего "
                  + shortLink.getUsageCounter()
                  + " использований из лимита в "
                  + shortLink.getUsageLimitAmount()
                  + ".");
        }
      }
    }
  }

  private void presentNotificationsViewModel(NotificationsViewModel model) {
    String serviceBaseURL = configManager.getDefaultServiceBaseURLProperty();
    printlnGreen("Внимание! У вас есть непрочитанные уведомления:");
    for (Notification notification : model.notifications) {
      ShortLink shortLink = notification.getShortLink();
      if (notification.getType() == Notification.NotificationType.LIMIT_REACHED) {
        printlnCyan(
            "Лимит использований вашей короткой ссылки "
                + serviceBaseURL
                + shortLink.getShortId()
                + " на URL "
                + shortLink.getOriginalURLAddress()
                + " был израсходован.\nДанной короткой ссылкой воспользовались "
                + shortLink.getUsageCounter()
                + " раз "
                + "(при лимите в "
                + shortLink.getUsageLimitAmount()
                + " использований). \nСсылка будет удалена в "
                + shortLink
                    .getExpirationDateTime()
                    .format(
                        DateTimeFormatter.ofPattern("E dd.MM.uuuu HH:mm")
                            .withLocale(Locale.forLanguageTag("ru-RU")))
                + ".");
      }
      if (notification.getType() == Notification.NotificationType.EXPIRED) {
        printlnCyan(
            "Срок действия вашей короткой ссылки "
                + serviceBaseURL
                + shortLink.getShortId()
                + " на URL "
                + shortLink.getOriginalURLAddress()
                + " истёк в "
                + shortLink
                    .getExpirationDateTime()
                    .format(
                        DateTimeFormatter.ofPattern("E dd.MM.uuuu HH:mm")
                            .withLocale(Locale.forLanguageTag("ru-RU")))
                + ".\nДанной короткой ссылкой воспользовались "
                + shortLink.getUsageCounter()
                + " раз. \nСсылка была удалена из базы данных сервиса.");
      }
    }
  }

  @Override
  public void sendMessage(String message) {
    printlnYellow(message);
  }
}
