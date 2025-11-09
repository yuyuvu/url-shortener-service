package com.github.yuyuvu.urlshortener.cli.commands.impl;

import com.github.yuyuvu.urlshortener.application.LinkService;
import com.github.yuyuvu.urlshortener.application.UserService;
import com.github.yuyuvu.urlshortener.cli.commands.CommandHandler;
import com.github.yuyuvu.urlshortener.cli.viewmodels.ViewModel;
import com.github.yuyuvu.urlshortener.cli.viewmodels.impl.CreatedLinkViewModel;
import com.github.yuyuvu.urlshortener.cli.viewmodels.impl.ErrorViewModel;
import com.github.yuyuvu.urlshortener.domain.model.ShortLink;
import com.github.yuyuvu.urlshortener.domain.model.User;
import com.github.yuyuvu.urlshortener.exceptions.InvalidOriginalLinkException;
import com.github.yuyuvu.urlshortener.infrastructure.config.ConfigManager;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Обработчик команды shorten, отвечающей за создание новой уникальной короткой ссылки в сервисе для
 * определённого длинного URL. Возможность явного вызова shorten через написание команды в консоли
 * была убрана с учётом того, что ссылку можно просто ввести в консоль без каких-либо команд. Сейчас
 * вызывается из RedirectCommandHandler.
 */
public class ShortenCommandHandler implements CommandHandler {
  private final LinkService linkService;
  private final UserService userService;
  private final ConfigManager configManager;
  private final Consumer<UUID> onNewUserCreationDo;

  /**
   * Конструктор обработчика команды shorten, отвечающего за создание новой уникальной короткой
   * ссылки в сервисе для определённого длинного URL. Возможность явного вызова shorten через
   * написание команды в консоли была убрана с учётом того, что ссылку можно просто ввести в консоль
   * без каких-либо команд. Сейчас handle обработчика вызывается из RedirectCommandHandler.
   */
  public ShortenCommandHandler(
      LinkService linkService,
      UserService userService,
      ConfigManager configManager,
      Consumer<UUID> onNewUserCreationDo) {
    this.linkService = linkService;
    this.userService = userService;
    this.configManager = configManager;
    this.onNewUserCreationDo = onNewUserCreationDo;
  }

  /**
   * Метод handle принимает аргументы для команды и UUID пользователя, вызвавшего её, и пытается
   * создать новую уникальную короткую ссылку в сервисе для определённого длинного URL.
   */
  @Override
  public ViewModel handle(String[] commandArgs, UUID currentUserUUID) {
    String originalURL = commandArgs[0];
    // Проверка передачи требуемого количества аргументов, иначе отправка сообщения с помощью по
    // команде. Нужна только в случае возвращения возможности явного вызова команды shorten.
    if (commandArgs.length != 1 || originalURL.isBlank()) {
      return new ErrorViewModel(
          "Правильное использование команды: shorten URL_для_сокращения "
              + "(указывайте протокол в URL). "
              + "\nВы указали недостаточное или избыточное количество аргументов.");
    } else {
      try {
        // Проверяем что переданный длинный URL содержит корректные схемы URL
        // и соответствует правилам стандарта RFC2396
        linkService.validateURLFormat(originalURL);

        User user;
        boolean isNewUser = false;

        // Получаем текущего пользователя или создаём UUID для нового
        if (currentUserUUID != null && userService.getUserByUUID(currentUserUUID).isPresent()) {
          user = userService.getUserByUUID(currentUserUUID).get();
        } else {
          user = userService.makeNewUUIDAndUser();
          isNewUser = true;
        }

        // Создаём новый объект короткой ссылки, не сохраняя его
        ShortLink shortLink = linkService.makeNewShortLink(originalURL, user.getUUID());

        // Проверяем, сколько активных в одном моменте ссылок может иметь один пользователь
        int linksPerUserLimit = configManager.getDefaultShortLinkMaxAmountPerUserProperty();
        if (user.getAmountOfMadeShortLinks() >= linksPerUserLimit) {
          return new ErrorViewModel(
              "Вы достигли максимального количества созданных коротких ссылок на "
                  + " одного пользователя. Удалите или измените старые ссылки.");
        } else {
          // Если лимит не превышен, увеличиваем счётчик созданных ссылок
          user.incrementAmountOfMadeShortLinks();

          // Сохраняем UUID и объект нового пользователя в репозиторий
          // и автоматически идентифицируемся в сервисе
          if (isNewUser) {
            userService.saveNewUser(user);
            this.onNewUserCreationDo.accept(user.getUUID());
          }

          // Сохраняем объект короткой ссылки в репозиторий
          linkService.saveNewShortLink(shortLink);

          // Получаем из настроек service URL и сообщаем пользователю, что за ссылка для него была
          // создана
          // и, опционально, какой UUID ему присвоен
          String serviceBaseURL = configManager.getDefaultServiceBaseURLProperty();
          return new CreatedLinkViewModel(
              user.getUUID(), serviceBaseURL + shortLink.getShortId(), isNewUser, originalURL);
        }
      } catch (InvalidOriginalLinkException e) {
        return new ErrorViewModel(e.getMessage());
      }
    }
  }
}
