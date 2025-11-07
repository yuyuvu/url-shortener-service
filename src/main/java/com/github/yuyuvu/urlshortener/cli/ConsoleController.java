package com.github.yuyuvu.urlshortener.cli;

import com.github.yuyuvu.urlshortener.application.LinkService;
import com.github.yuyuvu.urlshortener.application.NotificationService;
import com.github.yuyuvu.urlshortener.application.UserService;
import com.github.yuyuvu.urlshortener.cli.commands.CommandHandler;
import com.github.yuyuvu.urlshortener.cli.commands.impl.*;
import com.github.yuyuvu.urlshortener.cli.presenters.Presenter;
import com.github.yuyuvu.urlshortener.cli.presenters.impl.ConsolePresenter;
import com.github.yuyuvu.urlshortener.cli.viewmodels.ViewModel;
import com.github.yuyuvu.urlshortener.cli.viewmodels.impl.NotificationsViewModel;
import com.github.yuyuvu.urlshortener.domain.model.Notification;
import com.github.yuyuvu.urlshortener.infrastructure.config.ConfigManager;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.*;

/**
 * Класс, который принимает ввод от пользователя. Парсит команды и направляет на нужный обработчик
 * команды. Принимает от обработчика реализацию ViewModel и направляет её на вывод в одну из
 * реализаций интерфейса Presenter.
 */
public class ConsoleController {
  /* Получаем текущую системную кодировку и передаём её в InputStreamReader,
   * из которого будет читать Scanner. Это нужно для правильного считывания кириллицы из консоли */
  private Scanner userInput =
      new Scanner(
          new InputStreamReader(
              System.in, Charset.forName((String) System.getProperties().get("stdout.encoding"))));

  private final Map<String, CommandHandler> appCommands = new HashMap<>();
  private final CommandHandler defaultHandler;

  private final UserService userService;
  private final LinkService linkService;
  private final NotificationService notificationService;

  private final ConfigManager configManager;
  private final Presenter presenter;

  /** UUID текущего пользователя. Если не идентифицировался, то null. */
  private UUID currentUserUUID = null;

  public ConsoleController(
      UserService userService,
      LinkService linkService,
      NotificationService notificationService,
      ConfigManager configManager) {
    this.userService = userService;
    this.linkService = linkService;
    this.notificationService = notificationService;
    this.configManager = configManager;
    this.presenter = new ConsolePresenter(configManager);
    this.defaultHandler = new RedirectCommandHandler(linkService);

    registerCommand("login", new LoginCommandHandler(userService, this::loginUser));
    registerCommand("logout", new LogoutCommandHandler(this::logoutUser));
    registerCommand(
        "shorten",
        new ShortenCommandHandler(linkService, userService, configManager, this::loginUser));
    registerCommand("list", new ListCommandHandler(linkService));
    registerCommand("stats", new StatsCommandHandler(linkService));
    registerCommand("manage", new ManageCommandHandler(linkService, configManager));
    registerCommand("help", new HelpCommandHandler());
    registerCommand("exit", new ExitCommandHandler(presenter));
    registerCommand("delete", new DeleteCommandHandler(linkService, userService));
  }

  /**
   * Регистрирует имя для конкретного обработчика команды, которое будет парситься. По этому имени
   * будет осуществляться дальнейшая обработка.
   */
  private void registerCommand(String commandName, CommandHandler handler) {
    appCommands.put(commandName, handler);
  }

  /** Коллбэк для идентификации по UUID для LoginCommandHandler. */
  private void loginUser(UUID userUUID) {
    this.currentUserUUID = userUUID;
  }

  /** Коллбэк для окончания сессии по UUID для LogoutCommandHandler. */
  private void logoutUser(UUID userUUID) {
    this.currentUserUUID = null;
  }

  /** Основной цикл обработки ввода. */
  public void startListening() {
    presenter.sendMessage("Проект выполнил Мордашев Юрий Вячеславович.");
    presenter.sendMessage("Сервис сокращения ссылок запущен!");
    presenter.sendMessage("Для получения помощи по сервису введите help.");
    presenter.sendMessage(
        "Без явного указания какой-либо команды сервис воспринимает ввод как короткий URL для перехода.");

    while (true) {
      String currentInput = userInput.nextLine();
      route(currentInput);
    }
  }

  /** Метод для парсинга команд из ввода и перенаправления на обработчик команды. */
  private void route(String input) {
    String[] commandParts = input.strip().split("\\s+");
    String commandName = commandParts[0];
    String[] commandArgs = Arrays.copyOfRange(commandParts, 1, commandParts.length);

    CommandHandler commandHandler = appCommands.get(commandName.toLowerCase());
    ViewModel result;

    if (commandHandler == null) {
      result = defaultHandler.handle(new String[] {input.strip()}, this.currentUserUUID);
    } else {
      result = commandHandler.handle(commandArgs, this.currentUserUUID);
    }

    if (result != null) {
      presenter.present(result);
    }
  }

  /**
   * Коллбэк, вызываемый в параллельном режиме внутри LinkCheckStateTask. Проверяет, есть ли у
   * пользователя непрочитанные уведомления, и если да, то выводит их для него. Метод полностью
   * гарантирует сохранность уведомлений даже после выключения приложения. При авторизации
   * пользователь сразу или в течение нескольких секунд увидит все непрочитанные уведомления.
   */
  public void sendUnreadNotifications() {
    if (currentUserUUID != null) {
      List<Notification> unreadNotifications =
          notificationService.getUnreadNotificationsByUUID(currentUserUUID);
      if (!unreadNotifications.isEmpty()) {
        presenter.present(new NotificationsViewModel(unreadNotifications));
        notificationService.markUnreadNotificationsAsRead(unreadNotifications);
      }
    }
  }
}
