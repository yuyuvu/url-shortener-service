package com.github.yuyuvu.urlshortener.infrastructure.scheduler;

import com.github.yuyuvu.urlshortener.application.LinkService;
import com.github.yuyuvu.urlshortener.application.NotificationService;
import com.github.yuyuvu.urlshortener.application.UserService;
import com.github.yuyuvu.urlshortener.domain.model.Notification;
import com.github.yuyuvu.urlshortener.domain.model.ShortLink;
import java.util.List;

/**
 * Класс представляет задание, которое периодически выполняется в параллельном режиме и включает в
 * себя: проверку истечения срока действия коротких ссылок и их удаление, отправку уведомлений, если
 * был израсходован лимит использований или срок действия ссылки истёк, а также очищение списка уже
 * прочитанных уведомлений.
 */
public class LinkCheckStateTask implements Runnable {
  private final LinkService linkService;
  private final NotificationService notificationService;
  private final UserService userService;
  private final Runnable showNotificationsTask;

  /**
   * Конструктор для создания задания, которое периодически выполняется в параллельном режиме и
   * включает в себя: проверку истечения срока действия коротких ссылок и их удаление, отправку
   * уведомлений, если был израсходован лимит использований или срок действия ссылки истёк, а также
   * очищение списка уже прочитанных уведомлений.
   */
  public LinkCheckStateTask(
      LinkService linkService,
      NotificationService notificationService,
      UserService userService,
      Runnable showNotificationsTask) {
    this.linkService = linkService;
    this.notificationService = notificationService;
    this.userService = userService;
    this.showNotificationsTask = showNotificationsTask;
  }

  /**
   * Метод, который отвечает за проверку истечения срока действия коротких ссылок и их удаление,
   * отправку уведомлений, если был израсходован лимит использований или срок действия ссылки истёк,
   * а также очищение списка уже прочитанных уведомлений.
   */
  @Override
  @SuppressWarnings("CallToPrintStackTrace")
  public void run() {
    try {
      // Получаем все короткие ссылки сервиса
      List<ShortLink> allServiceLinks = linkService.listAllShortLinks();

      // Проходимся по каждой короткой ссылке
      for (ShortLink shortLink : allServiceLinks) {

        // Случай ссылки с истёкшим сроком действия
        if (shortLink.isExpired()) {
          // Создаём уведомление нужного типа, сохраняем его в репозиторий
          Notification notification =
              notificationService.makeNewShortLinkExpiredNotification(shortLink);
          notificationService.saveNewNotification(notification);

          // Удаляем короткую ссылку в оригинальном ShortLinkRepository через обращение в сервис
          linkService.uncheckedDeleteShortLinkByShortId(shortLink.getShortId());

          // Уменьшаем счётчик созданных коротких ссылок пользователя
          if (userService.getUserByUUID(shortLink.getOwnerOfShortURL()).isPresent()) {
            userService
                .getUserByUUID(shortLink.getOwnerOfShortURL())
                .get()
                .decrementAmountOfMadeShortLinks();
          }
        }

        // Случай ссылки с израсходованным лимитом использований
        if (shortLink.isLimitReached() && !shortLink.isLimitNotified()) {
          // Создаём уведомление нужного типа, сохраняем его в репозиторий, делаем отметку,
          // что по данной ссылке уже есть уведомление
          Notification notification =
              notificationService.makeNewShortLinkLimitReachedNotification(shortLink);
          notificationService.saveNewNotification(notification);
          shortLink.setLimitNotified(true);
        }
      }

      // Удаление уже прочитанных уведомлений всех пользователей
      for (Notification notification : notificationService.listAllNotifications()) {
        if (notification.isRead()) {
          notificationService.deleteNotification(notification);
        }
      }

      // Отправка непрочитанных уведомлений текущему пользователю в коллбэке к ConsoleController
      this.showNotificationsTask.run();

    } catch (Exception e) {
      e.printStackTrace(); // Отладка проблем итерирования и параллельного удаления
    }
  }
}
