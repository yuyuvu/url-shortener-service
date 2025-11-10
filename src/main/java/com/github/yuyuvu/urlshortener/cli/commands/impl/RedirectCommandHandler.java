package com.github.yuyuvu.urlshortener.cli.commands.impl;

import com.github.yuyuvu.urlshortener.application.LinkService;
import com.github.yuyuvu.urlshortener.cli.commands.CommandHandler;
import com.github.yuyuvu.urlshortener.cli.viewmodels.ViewModel;
import com.github.yuyuvu.urlshortener.cli.viewmodels.impl.ErrorViewModel;
import com.github.yuyuvu.urlshortener.cli.viewmodels.impl.SuccessViewModel;
import com.github.yuyuvu.urlshortener.exceptions.InvalidOriginalLinkException;
import com.github.yuyuvu.urlshortener.exceptions.InvalidShortLinkException;
import com.github.yuyuvu.urlshortener.exceptions.OriginalLinkNotFoundException;
import com.github.yuyuvu.urlshortener.exceptions.UsagesLimitReachedException;
import java.io.IOException;
import java.util.UUID;

/**
 * Обработчик редиректа пытается перенаправить пользователя по URL, на который ведёт короткая
 * ссылка. Если передан не URL сервиса, то пытается создать новую короткую ссылку через
 * ShortenCommandHandler.
 */
public class RedirectCommandHandler implements CommandHandler {
  private final LinkService linkService;
  private final ShortenCommandHandler shortenCommandHandler;

  /**
   * Конструктор обработчика редиректа, перенаправляющего пользователя по URL, на который ведёт
   * короткая ссылка. Если передан не URL сервиса, то обработчик пытается создать новую короткую
   * ссылку через ShortenCommandHandler.
   */
  public RedirectCommandHandler(
      LinkService linkService, ShortenCommandHandler shortenCommandHandler) {
    this.linkService = linkService;
    this.shortenCommandHandler = shortenCommandHandler;
  }

  /**
   * Метод handle принимает URL для редиректа или сокращения и UUID пользователя, сделавшего запрос,
   * и пытается перенаправить пользователя по URL, на который ведёт короткая ссылка. Если передан не
   * URL сервиса, то пытается создать новую короткую ссылку через ShortenCommandHandler.
   */
  @Override
  public ViewModel handle(String[] commandArgs, UUID currentUserUUID) {
    String shortLinkURL = commandArgs[0];
    // Проверка передачи только URL для редиректа, иначе пользователь ввёл
    // случайные символы через пробел без порядка
    if (commandArgs.length != 1 || shortLinkURL.isBlank()) {
      return new ErrorViewModel(
          "Вы не ввели команду или URL для сокращения или перехода. "
              + "Предоставленный ввод не распознан. "
              + "Введите help для помощи по сервису.");
    } else {
      try {
        // Проверяем, что в запросе есть какой-то URL (передано значение с корректными схемами URL,
        // и оно соответствует правилам стандарта RFC2396)
        linkService.validateURLFormat(shortLinkURL);
      } catch (InvalidOriginalLinkException e) {
        return new ErrorViewModel(
            """
            Предоставленный ввод не распознан в качестве команды или возможного URL для сокращения.
            Введите help для получения помощи по сервису.
            Если вы желаете ввести URL для сокращения, \
            то указывайте его вместе с протоколом (http://, https://).
            Хост должен быть непустым и содержать название доменной зоны. Вместо этого также можно указать ip с протоколом.""");
      }
      try {
        // Проверяем, что запрошена активная короткая ссылка нашего сервиса.
        // Если нет, то пытаемся создать новую короткую ссылку.
        // Если же передана короткая ссылка, то пытаемся перенаправить по ней.
        if (linkService.checkShortLinkDoesNotStartWithServiceBaseURL(shortLinkURL)) {
          return shortenCommandHandler.handle(commandArgs, currentUserUUID);
        }
        String originalURLAddress = linkService.redirectByShortLink(shortLinkURL, true);
        return new SuccessViewModel("Перенаправление на " + originalURLAddress + " ...");
      } catch (OriginalLinkNotFoundException
          | IOException
          | InvalidShortLinkException
          | InvalidOriginalLinkException
          | UsagesLimitReachedException e) {
        return new ErrorViewModel(e.getMessage());
      }
    }
  }
}
