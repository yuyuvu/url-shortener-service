package com.github.yuyuvu.urlshortener.cli.presenters.impl;

import static com.github.yuyuvu.urlshortener.cli.presenters.ColorPrinter.*;

import com.github.yuyuvu.urlshortener.cli.presenters.Presenter;
import com.github.yuyuvu.urlshortener.cli.viewmodels.ViewModel;
import com.github.yuyuvu.urlshortener.cli.viewmodels.impl.CreatedLinkViewModel;
import com.github.yuyuvu.urlshortener.cli.viewmodels.impl.ErrorViewModel;
import com.github.yuyuvu.urlshortener.cli.viewmodels.impl.ListViewModel;
import com.github.yuyuvu.urlshortener.cli.viewmodels.impl.SuccessViewModel;
import com.github.yuyuvu.urlshortener.domain.model.ShortLink;
import com.github.yuyuvu.urlshortener.infrastructure.config.ConfigManager;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

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
      printlnCyan("Для вас создан новый UUID для последующего управления созданными ссылками: " + model.creatorUUID);
      printlnCyan("На время данной сессии вы автоматически идентифицированы под данным UUID.");
      printlnCyan("Обязательно надёжно зафиксируйте данный UUID на будущее.");
      printlnCyan("При повторном подключении к сервису используйте команду login ваш_UUID для идентификации.");
    }
  }

  private void presentListViewModel(ListViewModel model) {
    if(model.shortLinks.isEmpty()) {
      printlnYellow("На текущий момент нет созданных вами активных коротких ссылок.");
    } else {
      printlnGreen("Принадлежащие вам активные короткие ссылки:");
      String serviceBaseURL = configManager.getDefaultServiceBaseURLProperty();
      for (ShortLink shortLink : model.shortLinks) {
        printlnCyan("\t - " + serviceBaseURL+shortLink.getShortId()
            + ", ведёт на: " + shortLink.getOriginalURLAddress()
            + ", время создания: " + shortLink.getCreationDateTime().format(
            DateTimeFormatter.ofPattern("E dd.MM.uuuu HH:mm")
                .withLocale(Locale.forLanguageTag("ru-RU")))
            + ", истекает в " + shortLink.getExpirationDateTime().format(
            DateTimeFormatter.ofPattern("E dd.MM.uuuu HH:mm")
                .withLocale(Locale.forLanguageTag("ru-RU")))
            + ", лимит использований " + shortLink.getUsageLimitAmount() + "."
        );
      }
    }
  }

  @Override
  public void sendMessage(String message) {
    printlnYellow(message);
  }
}
