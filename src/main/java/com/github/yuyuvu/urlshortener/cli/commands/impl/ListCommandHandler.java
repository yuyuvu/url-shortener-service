package com.github.yuyuvu.urlshortener.cli.commands.impl;

import com.github.yuyuvu.urlshortener.application.LinkService;
import com.github.yuyuvu.urlshortener.cli.commands.CommandHandler;
import com.github.yuyuvu.urlshortener.cli.viewmodels.ViewModel;
import com.github.yuyuvu.urlshortener.cli.viewmodels.impl.ErrorViewModel;
import com.github.yuyuvu.urlshortener.cli.viewmodels.impl.ListViewModel;
import java.util.UUID;

/** Обработчик команды list, отвечающей за вывод всего списка ссылок некоторого пользователя. */
public class ListCommandHandler implements CommandHandler {
  private final LinkService linkService;

  /**
   * Конструктор обработчика команды list, отвечающего за вывод всего списка ссылок некоторого
   * пользователя.
   */
  public ListCommandHandler(LinkService linkService) {
    this.linkService = linkService;
  }

  /**
   * Метод handle принимает аргументы для команды и UUID пользователя, который её вызвал, и пытается
   * вывести весь список ссылок некоторого пользователя.
   */
  @Override
  public ViewModel handle(String[] commandArgs, UUID currentUserUUID) {
    // Проверка выполнения команды от имени идентифицировавшегося пользователя
    if (currentUserUUID == null) {
      return new ErrorViewModel(
          "Нельзя просматривать список созданных ссылок без предварительной идентификации по UUID. "
              + "Используйте команду: login ваш_UUID");
    }

    // Передача списка со ссылками пользователя на вывод
    return new ListViewModel(linkService.listShortLinksByUUID(currentUserUUID));
  }
}
