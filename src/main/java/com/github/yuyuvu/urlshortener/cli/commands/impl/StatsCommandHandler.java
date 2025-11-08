package com.github.yuyuvu.urlshortener.cli.commands.impl;

import com.github.yuyuvu.urlshortener.application.LinkService;
import com.github.yuyuvu.urlshortener.cli.commands.CommandHandler;
import com.github.yuyuvu.urlshortener.cli.viewmodels.ViewModel;
import com.github.yuyuvu.urlshortener.cli.viewmodels.impl.ErrorViewModel;
import com.github.yuyuvu.urlshortener.cli.viewmodels.impl.StatsViewModel;
import com.github.yuyuvu.urlshortener.domain.model.ShortLink;
import com.github.yuyuvu.urlshortener.exceptions.InvalidShortLinkException;
import com.github.yuyuvu.urlshortener.exceptions.OriginalLinkNotFoundException;
import java.util.UUID;

/**
 * Обработчик команды stats, отвечающей за вывод статистики использования всех созданных под
 * некоторым UUID коротких ссылок или одной определённой (с дополнительной информацией).
 */
public class StatsCommandHandler implements CommandHandler {
  private final LinkService linkService;

  /**
   * Конструктор обработчика команды stats, отвечающего за вывод статистики использования всех
   * созданных под некоторым UUID коротких ссылок или одной определённой (с дополнительной
   * информацией).
   */
  public StatsCommandHandler(LinkService linkService) {
    this.linkService = linkService;
  }

  /**
   * Метод handle принимает аргументы для команды и UUID пользователя, вызвавшего её, и пытается
   * вывести статистику использования всех созданных под данным UUID коротких ссылок или одной
   * определённой (с дополнительной информацией).
   */
  @Override
  public ViewModel handle(String[] commandArgs, UUID currentUserUUID) {
    // Проверка выполнения команды от имени идентифицировавшегося пользователя
    if (currentUserUUID == null) {
      return new ErrorViewModel(
          "Нельзя просматривать статистику ссылок без предварительной идентификации по UUID. "
              + "Используйте команду: login ваш_UUID");
    }

    // Проверка желания вывести статистику только по одной ссылке или по всем сразу
    if (commandArgs.length == 1) {
      try {
        // Проверяем, что запрошена статистика по активной короткой ссылке нашего сервиса
        ShortLink shortLink = linkService.validateShortLinkExistence(commandArgs[0]);

        // Отдаём все ссылки пользователя, выведем только одну
        return new StatsViewModel(
            linkService.listShortLinksByUUID(currentUserUUID).stream()
                .filter(sl -> sl.getShortId().equals(shortLink.getShortId()))
                .toList(),
            true);
      } catch (OriginalLinkNotFoundException | InvalidShortLinkException e) {
        // Отправление сообщений с ошибками, если передана несуществующая или удалённая короткая
        // ссылка
        return new ErrorViewModel(e.getMessage());
      }
    } else {
      // Отдаём все ссылки пользователя, выведем все
      return new StatsViewModel(linkService.listShortLinksByUUID(currentUserUUID), false);
    }
  }
}
