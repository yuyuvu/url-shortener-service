package com.github.yuyuvu.urlshortener.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.github.yuyuvu.urlshortener.application.LinkService;
import com.github.yuyuvu.urlshortener.application.NotificationService;
import com.github.yuyuvu.urlshortener.application.UserService;
import com.github.yuyuvu.urlshortener.cli.ConsoleController;
import com.github.yuyuvu.urlshortener.cli.viewmodels.ViewModel;
import com.github.yuyuvu.urlshortener.cli.viewmodels.impl.CreatedLinkViewModel;
import com.github.yuyuvu.urlshortener.cli.viewmodels.impl.ErrorViewModel;
import com.github.yuyuvu.urlshortener.cli.viewmodels.impl.ListViewModel;
import com.github.yuyuvu.urlshortener.cli.viewmodels.impl.StatsViewModel;
import com.github.yuyuvu.urlshortener.cli.viewmodels.impl.SuccessViewModel;
import com.github.yuyuvu.urlshortener.domain.repository.UserRepository;
import com.github.yuyuvu.urlshortener.infrastructure.config.ConfigManager;
import com.github.yuyuvu.urlshortener.infrastructure.persistence.InMemoryNotificationRepository;
import com.github.yuyuvu.urlshortener.infrastructure.persistence.InMemoryShortLinkRepository;
import com.github.yuyuvu.urlshortener.infrastructure.persistence.InMemoryUserRepository;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Класс для тестов всего процесса прохождения через обработчики команд и влияния выполнения их
 * методов handle на всё состояние сервиса. Обращаемся к основному методу, через который проходит
 * весь ввод (ConsoleController.route()) и смотрим, как на это реагирует сервис. Фактически с нуля и
 * до самого конца воспроизводим взаимодействие пользователя и сервиса.
 */
@ExtendWith(MockitoExtension.class)
public class ControllerAndCommandHandlersTest {
  ConsoleController consoleController;
  UserService userService;
  UserRepository inMemoryUserRepository;
  LinkService linkService;
  NotificationService notificationService;

  @Mock ConfigManager configManager;

  /** Подготавливаем зависимости consoleController. */
  @BeforeEach
  public void setUpController() {
    // when(configManager.getUserSetShortLinkUsageLimitProperty()).thenReturn(50);
    // when(configManager.getUserSetShortLinkMaxTTLInUnitsProperty()).thenReturn(72);

    inMemoryUserRepository = new InMemoryUserRepository(new HashMap<>());
    userService = new UserService(inMemoryUserRepository);
    linkService = new LinkService(new InMemoryShortLinkRepository(new HashMap<>()), configManager);
    notificationService =
        new NotificationService(new InMemoryNotificationRepository(new ArrayList<>()));
    consoleController =
        new ConsoleController(userService, linkService, notificationService, configManager);
  }

  /**
   * Проверяем процесс сокращения URL от ввода ссылки в консоль до изменения состояния репозитория.
   */
  @Test
  void shortenNewLinksTest() {
    // Подготавливаем настройки
    when(configManager.getDefaultServiceBaseURLProperty()).thenReturn("https://yulink.tech/");

    when(configManager.getLegacyServiceBaseURLProperty()).thenReturn(new String[] {""});

    when(configManager.getDefaultShortLinkTTLTimeUnitProperty())
        .thenReturn(ConfigManager.TimeUnit.HOURS);

    when(configManager.getDefaultShortLinkTTLInUnitsProperty()).thenReturn(24);

    when(configManager.getDefaultShortLinkUsageLimitProperty()).thenReturn(8);

    char[] allowedCharacters =
        new char[] {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', '1', '2', '3'};

    when(configManager.getShortLinkAllowedCharactersProperty()).thenReturn(allowedCharacters);

    when(configManager.getDefaultShortLinkIdLengthProperty()).thenReturn(6);

    when(configManager.getDefaultShortLinkMaxAmountPerUserProperty()).thenReturn(100);

    // Имитируем сокращение нескольких новых валидных URL, результатом должно быть успешное
    // сокращение, то есть должен вернуться CreatedLinkViewModel, количество ссылок в сервисе
    // должно увеличиваться
    assertInstanceOf(CreatedLinkViewModel.class, consoleController.route("https://google.com"));
    assertInstanceOf(CreatedLinkViewModel.class, consoleController.route("https://google.com"));
    assertInstanceOf(CreatedLinkViewModel.class, consoleController.route("https://google.com"));
    assertEquals(3, linkService.listAllShortLinks().size());

    assertInstanceOf(
        CreatedLinkViewModel.class, consoleController.route("https://blog.blog.example.com"));
    assertInstanceOf(
        CreatedLinkViewModel.class, consoleController.route("https://github.com/yuyuvu"));
    assertInstanceOf(
        CreatedLinkViewModel.class, consoleController.route("https://www.example.com"));
    assertInstanceOf(CreatedLinkViewModel.class, consoleController.route("http://www.example.com"));
    assertInstanceOf(
        CreatedLinkViewModel.class, consoleController.route("http://192.168.1.1/admin"));
    assertInstanceOf(
        CreatedLinkViewModel.class, consoleController.route("https://example.com:8443/secure"));
    assertInstanceOf(CreatedLinkViewModel.class, consoleController.route("ftp://example.com"));
    assertInstanceOf(CreatedLinkViewModel.class, consoleController.route("https://сайт.рф"));
    assertInstanceOf(
        CreatedLinkViewModel.class, consoleController.route("http://sub.domain.example.com/path"));
    assertInstanceOf(
        CreatedLinkViewModel.class,
        consoleController.route("https://example.com:8080/api/v1/users"));
    assertInstanceOf(
        CreatedLinkViewModel.class,
        consoleController.route("http://example.com/search?q=test&sort=desc"));
    assertInstanceOf(
        CreatedLinkViewModel.class, consoleController.route("https://example.com/page#section1"));
    assertInstanceOf(
        CreatedLinkViewModel.class, consoleController.route("ftp://files.example.com/documents/"));
    assertInstanceOf(
        CreatedLinkViewModel.class, consoleController.route("https://user:password@example.com"));
    assertInstanceOf(
        CreatedLinkViewModel.class,
        consoleController.route("https://api-key:secret@api.example.com"));
    assertInstanceOf(
        CreatedLinkViewModel.class, consoleController.route("mailto:test@example.com"));
    assertEquals(19, linkService.listAllShortLinks().size());

    // Имитируем попытки сокращения невалидных и неподходящих для сервиса URL должен вернуться
    // ErrorViewModel, количество ссылок в сервисе не должно увеличиваться
    assertInstanceOf(ErrorViewModel.class, consoleController.route("google.com"));
    assertInstanceOf(ErrorViewModel.class, consoleController.route("https://com"));
    assertInstanceOf(ErrorViewModel.class, consoleController.route("https://.com"));
    assertInstanceOf(ErrorViewModel.class, consoleController.route("https://"));
    assertInstanceOf(ErrorViewModel.class, consoleController.route("htt"));
    assertInstanceOf(ErrorViewModel.class, consoleController.route(""));
    assertInstanceOf(ErrorViewModel.class, consoleController.route("http://localhost:3000"));
    assertInstanceOf(ErrorViewModel.class, consoleController.route("http://example..com"));
    assertInstanceOf(ErrorViewModel.class, consoleController.route("http://...example.com"));
    assertInstanceOf(ErrorViewModel.class, consoleController.route("http://e x a mp le.co m"));
    assertInstanceOf(ErrorViewModel.class, consoleController.route("httpx://example.com"));
    assertInstanceOf(ErrorViewModel.class, consoleController.route("tel:+1234567890"));
    assertInstanceOf(ErrorViewModel.class, consoleController.route("sms:+1234567890?body=Hello"));
    assertInstanceOf(ErrorViewModel.class, consoleController.route("whatsapp://send?text=Hi"));
    assertInstanceOf(ErrorViewModel.class, consoleController.route("://example.com"));
    assertInstanceOf(ErrorViewModel.class, consoleController.route("http:example.com"));
    assertInstanceOf(ErrorViewModel.class, consoleController.route("https://-example.com"));
    assertInstanceOf(ErrorViewModel.class, consoleController.route("http://example-.com"));
    // количество коротких ссылок осталось тем же
    assertEquals(19, linkService.listAllShortLinks().size());
  }

  /**
   * Имитируем случайный ввод символов в консоль или некорректные вызовы команд. Проверяем, что
   * везде возвращается ошибка с описанием проблемы, сервис нигде не падает.
   */
  @Test
  void consoleGotStrangeInputTest() {
    when(configManager.getDefaultShortLinkTTLTimeUnitProperty())
        .thenReturn(ConfigManager.TimeUnit.HOURS);

    // случайный ввод
    assertInstanceOf(ErrorViewModel.class, consoleController.route("в ыва ываваы"));
    assertInstanceOf(ErrorViewModel.class, consoleController.route(""));
    assertInstanceOf(ErrorViewModel.class, consoleController.route("     "));
    assertInstanceOf(ErrorViewModel.class, consoleController.route(".com"));
    assertInstanceOf(ErrorViewModel.class, consoleController.route("ввввввввввввв"));
    assertInstanceOf(ErrorViewModel.class, consoleController.route(" console"));
    assertInstanceOf(ErrorViewModel.class, consoleController.route("-2=05-0df0og[2-"));
    assertInstanceOf(ErrorViewModel.class, consoleController.route("   fdgdg  fd sdfsdf sd"));
    assertInstanceOf(ErrorViewModel.class, consoleController.route("test"));
    assertInstanceOf(ErrorViewModel.class, consoleController.route("command"));
    assertInstanceOf(ErrorViewModel.class, consoleController.route("http"));
    assertInstanceOf(ErrorViewModel.class, consoleController.route("//example"));
    assertInstanceOf(ErrorViewModel.class, consoleController.route(" :+1234567890"));
    assertInstanceOf(ErrorViewModel.class, consoleController.route(" sms: + 123       4567890 "));
    assertInstanceOf(ErrorViewModel.class, consoleController.route(" ??? "));
    assertInstanceOf(ErrorViewModel.class, consoleController.route(":"));
    assertInstanceOf(ErrorViewModel.class, consoleController.route("ddddddddddddddddddddd"));
    assertInstanceOf(ErrorViewModel.class, consoleController.route("h"));
    assertInstanceOf(ErrorViewModel.class, consoleController.route("bpvcy98yr1fds9fug1uofsod"));

    // некорректные вызовы команд
    assertInstanceOf(ErrorViewModel.class, consoleController.route("manage get URL"));
    assertInstanceOf(ErrorViewModel.class, consoleController.route("delete delete"));
    assertInstanceOf(ErrorViewModel.class, consoleController.route("help NOT_OK"));
    assertInstanceOf(ErrorViewModel.class, consoleController.route("stats stats"));
    assertInstanceOf(ErrorViewModel.class, consoleController.route("list list list list"));
    assertInstanceOf(ErrorViewModel.class, consoleController.route("manage"));
    assertInstanceOf(ErrorViewModel.class, consoleController.route("config delete"));
    assertInstanceOf(ErrorViewModel.class, consoleController.route("stats help list delete"));
    // выход без UUID
    assertInstanceOf(ErrorViewModel.class, consoleController.route("logout"));

    // количество коротких ссылок равно нулю
    assertEquals(0, linkService.listAllShortLinks().size());
    // количество пользователей равно нулю
    assertEquals(0, inMemoryUserRepository.getAllUsers().size());
    // количество уведомлений равно нулю
    assertEquals(0, notificationService.listAllNotifications().size());
  }

  /**
   * Проверяем процесс перехода по сокращённым ссылкам. Проверяем, что везде короткий ID ссылки
   * разный. Что оригинальный URL тот же, что и задавался. Что ссылки не блокируются случайно. А
   * также то, что UUID пользователя сохраняется в течение всего процесса.
   */
  @Test
  void redirectTest() throws IOException {
    // Подготавливаем настройки
    String serviceURL = "https://yulink.tech/";

    when(configManager.getDefaultServiceBaseURLProperty()).thenReturn(serviceURL);
    when(configManager.getLegacyServiceBaseURLProperty()).thenReturn(new String[] {""});
    when(configManager.getDefaultShortLinkTTLTimeUnitProperty())
        .thenReturn(ConfigManager.TimeUnit.HOURS);
    when(configManager.getDefaultShortLinkTTLInUnitsProperty()).thenReturn(24);
    when(configManager.getDefaultShortLinkUsageLimitProperty()).thenReturn(8);
    char[] allowedCharacters =
        new char[] {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', '1', '2', '3'};
    when(configManager.getShortLinkAllowedCharactersProperty()).thenReturn(allowedCharacters);
    when(configManager.getDefaultShortLinkIdLengthProperty()).thenReturn(6);
    when(configManager.getDefaultShortLinkMaxAmountPerUserProperty()).thenReturn(100);

    // Предотвращаем реальное открытие URL в тестах путём использования Mockito
    try (MockedStatic<Desktop> desktopMock = mockStatic(Desktop.class)) {
      // Создаём поддельный Desktop
      Desktop mockDesktopInstance = mock(Desktop.class);
      // Заставляем вызов статического метода Desktop.getDesktop()
      // возвращать наш ненастоящий Desktop
      desktopMock.when(Desktop::getDesktop).thenReturn(mockDesktopInstance);
      // Убеждаемся, что функция isDesktopSupported() возвращает значение true
      desktopMock.when(Desktop::isDesktopSupported).thenReturn(true);
      // Настраиваем browse в поддельном Desktop так, чтобы он ничего не делал
      doNothing().when(mockDesktopInstance).browse(any(URI.class));

      // Имитируем сокращение нескольких новых валидных URL,
      // берём короткие ссылки и вставляем их в консоль
      CreatedLinkViewModel link1 =
          (CreatedLinkViewModel) consoleController.route("https://google.com");
      SuccessViewModel result1 = (SuccessViewModel) consoleController.route(link1.shortURL);
      assertEquals("Перенаправление на " + "https://google.com" + " ...", result1.message);
      // Переходим по ней ещё раз
      SuccessViewModel result2 = (SuccessViewModel) consoleController.route(link1.shortURL);
      assertEquals("Перенаправление на " + "https://google.com" + " ...", result2.message);

      // Проверяем другие короткие ссылки на те же или другие URL
      CreatedLinkViewModel link2 =
          (CreatedLinkViewModel) consoleController.route("https://google.com");
      SuccessViewModel result3 = (SuccessViewModel) consoleController.route(link2.shortURL);
      assertEquals("Перенаправление на " + "https://google.com" + " ...", result3.message);
      // Переходим по ней ещё раз
      SuccessViewModel result4 = (SuccessViewModel) consoleController.route(link2.shortURL);
      assertEquals("Перенаправление на " + "https://google.com" + " ...", result4.message);

      // Пока всё ещё 2 ссылки в сервисе
      assertEquals(2, linkService.listAllShortLinks().size());

      // Теперь другие ссылки
      CreatedLinkViewModel link3 =
          (CreatedLinkViewModel) consoleController.route("https://blog.blog.example.com");
      SuccessViewModel result5 = (SuccessViewModel) consoleController.route(link3.shortURL);
      assertEquals(
          "Перенаправление на " + "https://blog.blog.example.com" + " ...", result5.message);
      // Переходим по ней ещё раз
      SuccessViewModel result6 = (SuccessViewModel) consoleController.route(link3.shortURL);
      assertEquals(
          "Перенаправление на " + "https://blog.blog.example.com" + " ...", result6.message);

      CreatedLinkViewModel link4 =
          (CreatedLinkViewModel) consoleController.route("https://github.com/yuyuvu");
      SuccessViewModel result7 = (SuccessViewModel) consoleController.route(link4.shortURL);
      assertEquals("Перенаправление на " + "https://github.com/yuyuvu" + " ...", result7.message);
      // Переходим по ней ещё раз
      SuccessViewModel result8 = (SuccessViewModel) consoleController.route(link4.shortURL);
      assertEquals("Перенаправление на " + "https://github.com/yuyuvu" + " ...", result8.message);

      CreatedLinkViewModel link5 =
          (CreatedLinkViewModel) consoleController.route("https://www.example.com");
      SuccessViewModel result9 = (SuccessViewModel) consoleController.route(link5.shortURL);
      assertEquals("Перенаправление на " + "https://www.example.com" + " ...", result9.message);
      // Переходим по ней ещё раз
      SuccessViewModel result10 = (SuccessViewModel) consoleController.route(link5.shortURL);
      assertEquals("Перенаправление на " + "https://www.example.com" + " ...", result10.message);

      CreatedLinkViewModel link6 =
          (CreatedLinkViewModel) consoleController.route("http://www.example.com");
      SuccessViewModel result11 = (SuccessViewModel) consoleController.route(link6.shortURL);
      assertEquals("Перенаправление на " + "http://www.example.com" + " ...", result11.message);
      // Переходим по ней ещё раз
      SuccessViewModel result12 = (SuccessViewModel) consoleController.route(link6.shortURL);
      assertEquals("Перенаправление на " + "http://www.example.com" + " ...", result12.message);

      CreatedLinkViewModel link7 =
          (CreatedLinkViewModel) consoleController.route("https://new.test.com/page#section1");
      SuccessViewModel result13 = (SuccessViewModel) consoleController.route(link7.shortURL);
      assertEquals(
          "Перенаправление на " + "https://new.test.com/page#section1" + " ...", result13.message);
      // Переходим по ней ещё раз
      SuccessViewModel result14 = (SuccessViewModel) consoleController.route(link7.shortURL);
      assertEquals(
          "Перенаправление на " + "https://new.test.com/page#section1" + " ...", result14.message);

      // все короткие ссылки должны быть разными
      ArrayList<CreatedLinkViewModel> links =
          new ArrayList<>(Arrays.asList(link1, link2, link3, link4, link5, link6, link7));
      assertEquals(
          7,
          links.stream()
              .map(createdLinkViewModel -> createdLinkViewModel.shortURL)
              .distinct()
              .count());

      // у всех ссылок один владелец
      UUID user1 = link1.creatorUUID;
      assertTrue(
          links.stream()
              .map(createdLinkViewModel -> createdLinkViewModel.creatorUUID)
              .allMatch(user1::equals));

      // В конце у нас должно быть 7 ссылок
      assertEquals(7, linkService.listAllShortLinks().size());
    }
  }

  /**
   * Проверяем многопользовательский режим. Переходы по чужим ссылками. Попытки поменять параметры
   * чужих ссылок. Также проверяем команду logout.
   */
  @Test
  void manyUsersRedirectsAndParametersChangeTest() throws IOException {
    // Подготавливаем настройки
    String serviceURL = "https://yulink.tech/";

    when(configManager.getDefaultServiceBaseURLProperty()).thenReturn(serviceURL);
    when(configManager.getLegacyServiceBaseURLProperty()).thenReturn(new String[] {""});
    when(configManager.getDefaultShortLinkTTLTimeUnitProperty())
        .thenReturn(ConfigManager.TimeUnit.HOURS);
    when(configManager.getDefaultShortLinkTTLInUnitsProperty()).thenReturn(24);
    when(configManager.getDefaultShortLinkUsageLimitProperty()).thenReturn(8);
    char[] allowedCharacters =
        new char[] {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', '1', '2', '3'};
    when(configManager.getShortLinkAllowedCharactersProperty()).thenReturn(allowedCharacters);
    when(configManager.getDefaultShortLinkIdLengthProperty()).thenReturn(6);
    when(configManager.getDefaultShortLinkMaxAmountPerUserProperty()).thenReturn(100);
    when(configManager.getUserSetShortLinkUsageLimitProperty()).thenReturn(50);
    when(configManager.getUserSetShortLinkMaxTTLInUnitsProperty()).thenReturn(72);

    // Предотвращаем реальное открытие URL в тестах путём использования Mockito
    try (MockedStatic<Desktop> desktopMock = mockStatic(Desktop.class)) {
      Desktop mockDesktopInstance = mock(Desktop.class);
      // Заставляем вызов статического метода Desktop.getDesktop()
      // возвращать наш ненастоящий Desktop
      desktopMock.when(Desktop::getDesktop).thenReturn(mockDesktopInstance);
      // Убеждаемся, что функция isDesktopSupported() возвращает значение true
      desktopMock.when(Desktop::isDesktopSupported).thenReturn(true);
      // Настраиваем browse в поддельном Desktop так, чтобы он ничего не делал
      doNothing().when(mockDesktopInstance).browse(any(URI.class));

      // Создаём новые ссылки 1 пользователя, пытаемся переходить по ним и менять параметры другим
      // пользователем.
      CreatedLinkViewModel link1 =
          (CreatedLinkViewModel) consoleController.route("https://google.com");

      // Теперь выходим, создаём второго пользователя
      consoleController.route("logout");
      CreatedLinkViewModel link2 =
          (CreatedLinkViewModel) consoleController.route("https://google.com");

      // Проверяем, что они разные
      assertNotEquals(link1.creatorUUID, link2.creatorUUID);

      // Второй пользователь идентифицирован. Переходим через него по ссылке первого пользователя
      SuccessViewModel result1 = (SuccessViewModel) consoleController.route(link1.shortURL);
      assertEquals("Перенаправление на " + "https://google.com" + " ...", result1.message);

      // Теперь пытаемся менять параметры первой ссылки от второго UUID, должны отображаться ошибки.
      // То же самое со статистикой.
      // Удаление
      assertInstanceOf(ErrorViewModel.class, consoleController.route("delete " + link1.shortURL));

      // Смена параметров
      assertInstanceOf(
          ErrorViewModel.class,
          consoleController.route("manage " + link1.shortURL + " set limit 25"));
      assertInstanceOf(
          ErrorViewModel.class,
          consoleController.route(
              "manage " + link1.shortURL + " set original_url https://github.com"));
      assertInstanceOf(
          ErrorViewModel.class,
          consoleController.route("manage " + link1.shortURL + " set ttl 40"));

      // Статистика
      StatsViewModel st = (StatsViewModel) consoleController.route("stats " + link1.shortURL);
      assertInstanceOf(StatsViewModel.class, st);
      // stats пустой
      assertTrue(st.shortLinks.isEmpty());

      // Но первый пользователь всё ещё может сделать всё то же самое.
      // Заходим от первого UUID и проверяем.
      consoleController.route("logout");
      consoleController.route("login " + link1.creatorUUID.toString());

      // Везде должно быть SuccessViewModel
      assertInstanceOf(
          SuccessViewModel.class,
          consoleController.route("manage " + link1.shortURL + " set limit 25"));
      assertInstanceOf(
          SuccessViewModel.class,
          consoleController.route(
              "manage " + link1.shortURL + " set original_url https://github.com"));
      assertInstanceOf(
          SuccessViewModel.class,
          consoleController.route("manage " + link1.shortURL + " set ttl 40"));

      StatsViewModel st2 = (StatsViewModel) consoleController.route("stats " + link1.shortURL);
      assertInstanceOf(StatsViewModel.class, st2);
      // stats не пустой
      assertFalse(st2.shortLinks.isEmpty());

      // в конце удаление
      assertInstanceOf(SuccessViewModel.class, consoleController.route("delete " + link1.shortURL));
    }
  }

  /**
   * Проверяем создание множества ссылок на один и тот же URL от разных пользователей. У всех
   * коротких ссылок должен быть разный адрес.
   */
  @Test
  void manyShortLinksOnOneURL() {
    // Подготавливаем настройки
    String serviceURL = "https://yulink.tech/";

    when(configManager.getDefaultServiceBaseURLProperty()).thenReturn(serviceURL);
    when(configManager.getLegacyServiceBaseURLProperty()).thenReturn(new String[] {""});
    when(configManager.getDefaultShortLinkTTLTimeUnitProperty())
        .thenReturn(ConfigManager.TimeUnit.HOURS);
    when(configManager.getDefaultShortLinkTTLInUnitsProperty()).thenReturn(24);
    when(configManager.getDefaultShortLinkUsageLimitProperty()).thenReturn(8);
    char[] allowedCharacters =
        new char[] {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', '1', '2', '3'};
    when(configManager.getShortLinkAllowedCharactersProperty()).thenReturn(allowedCharacters);
    when(configManager.getDefaultShortLinkIdLengthProperty()).thenReturn(6);
    when(configManager.getDefaultShortLinkMaxAmountPerUserProperty()).thenReturn(100);

    // Создаём 200 коротких ссылок на один и тот же URL от имени 200 разных UUID
    ArrayList<CreatedLinkViewModel> links = new ArrayList<>();
    for (int i = 0; i < 200; i++) {
      links.add((CreatedLinkViewModel) consoleController.route("https://google.com"));
      consoleController.route("logout");
    }

    // В итоге у всех коротких ссылок разный короткий URL
    assertEquals(
        200,
        links.stream()
            .map(createdLinkViewModel -> createdLinkViewModel.shortURL)
            .distinct()
            .count());
    // Но одинаковый оригинальный URL
    assertEquals(
        1,
        links.stream()
            .map(createdLinkViewModel -> createdLinkViewModel.originalURL)
            .distinct()
            .count());
  }

  /**
   * Проверяем блокировку ссылок после израсходования лимита редиректов. Как для создателя, так и
   * для других пользователей. Для создателя проверяем невозможность смены параметров после
   * блокировки. Но создатель должен мочь посмотреть статистику и удалить ссылку.
   */
  @Test
  void usageLimitExceedingTest() throws IOException {
    // Подготавливаем настройки
    String serviceURL = "https://yulink.tech/";

    when(configManager.getDefaultServiceBaseURLProperty()).thenReturn(serviceURL);
    when(configManager.getLegacyServiceBaseURLProperty()).thenReturn(new String[] {""});
    when(configManager.getDefaultShortLinkTTLTimeUnitProperty())
        .thenReturn(ConfigManager.TimeUnit.HOURS);
    when(configManager.getDefaultShortLinkTTLInUnitsProperty()).thenReturn(24);
    char[] allowedCharacters =
        new char[] {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', '1', '2', '3'};
    when(configManager.getShortLinkAllowedCharactersProperty()).thenReturn(allowedCharacters);
    when(configManager.getDefaultShortLinkIdLengthProperty()).thenReturn(6);
    when(configManager.getDefaultShortLinkMaxAmountPerUserProperty()).thenReturn(100);

    // Лимит равен 8
    when(configManager.getDefaultShortLinkUsageLimitProperty()).thenReturn(8);
    when(configManager.getUserSetShortLinkUsageLimitProperty()).thenReturn(50);
    when(configManager.getUserSetShortLinkMaxTTLInUnitsProperty()).thenReturn(72);

    // Предотвращаем реальное открытие URL в тестах путём использования Mockito
    try (MockedStatic<Desktop> desktopMock = mockStatic(Desktop.class)) {
      // Создаём поддельный Desktop
      Desktop mockDesktopInstance = mock(Desktop.class);
      // Заставляем вызов статического метода Desktop.getDesktop()
      // возвращать наш ненастоящий Desktop
      desktopMock.when(Desktop::getDesktop).thenReturn(mockDesktopInstance);
      // Убеждаемся, что функция isDesktopSupported() возвращает значение true
      desktopMock.when(Desktop::isDesktopSupported).thenReturn(true);
      // Настраиваем browse в поддельном Desktop так, чтобы он ничего не делал
      doNothing().when(mockDesktopInstance).browse(any(URI.class));

      // Создаём короткую ссылку и переходим 8 раз (должен быть SuccessViewModel),
      // на 9 должна быть ошибка
      CreatedLinkViewModel link1 =
          (CreatedLinkViewModel) consoleController.route("https://google.com");

      for (int i = 1; i <= 8; i++) {
        assertInstanceOf(SuccessViewModel.class, consoleController.route(link1.shortURL));
      }
      // Ошибка!
      assertInstanceOf(ErrorViewModel.class, consoleController.route(link1.shortURL));

      // Смена параметров - тоже ошибка
      assertInstanceOf(
          ErrorViewModel.class,
          consoleController.route("manage " + link1.shortURL + " set limit 25"));
      assertInstanceOf(
          ErrorViewModel.class,
          consoleController.route(
              "manage " + link1.shortURL + " set original_url https://github.com"));
      assertInstanceOf(
          ErrorViewModel.class,
          consoleController.route("manage " + link1.shortURL + " set ttl 40"));

      // Статистика - ошибки быть не должно
      StatsViewModel st = (StatsViewModel) consoleController.route("stats " + link1.shortURL);
      assertInstanceOf(StatsViewModel.class, st);
      // stats не пустой
      assertFalse(st.shortLinks.isEmpty());

      // заходим под другим UUID
      consoleController.route("logout");
      consoleController.route("https://google.com");

      // пытаемся перейти по чужой заблокированной ссылке, должна быть ошибка
      assertInstanceOf(ErrorViewModel.class, consoleController.route(link1.shortURL));

      // заходим под UUID создателя и удаляем
      consoleController.route("login " + link1.creatorUUID);
      assertInstanceOf(SuccessViewModel.class, consoleController.route("delete " + link1.shortURL));
    }
  }

  /** Проверяем прохождение через сервис и успешный вызов всех команд. */
  @Test
  void fullServiceUsePathTest() throws IOException {
    // Подготавливаем настройки
    String serviceURL = "https://yulink.tech/";

    when(configManager.getDefaultServiceBaseURLProperty()).thenReturn(serviceURL);
    when(configManager.getLegacyServiceBaseURLProperty()).thenReturn(new String[] {""});
    when(configManager.getDefaultShortLinkTTLTimeUnitProperty())
        .thenReturn(ConfigManager.TimeUnit.HOURS);
    when(configManager.getDefaultShortLinkTTLInUnitsProperty()).thenReturn(24);
    char[] allowedCharacters =
        new char[] {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', '1', '2', '3'};
    when(configManager.getShortLinkAllowedCharactersProperty()).thenReturn(allowedCharacters);
    when(configManager.getDefaultShortLinkIdLengthProperty()).thenReturn(6);
    when(configManager.getDefaultShortLinkMaxAmountPerUserProperty()).thenReturn(100);

    // Лимит равен 8
    when(configManager.getDefaultShortLinkUsageLimitProperty()).thenReturn(8);

    when(configManager.getUserSetShortLinkUsageLimitProperty()).thenReturn(50);
    when(configManager.getUserSetShortLinkMaxTTLInUnitsProperty()).thenReturn(72);

    // Предотвращаем реальное открытие URL в тестах путём использования Mockito
    try (MockedStatic<Desktop> desktopMock = mockStatic(Desktop.class)) {
      // Создаём поддельный Desktop
      Desktop mockDesktopInstance = mock(Desktop.class);
      // Заставляем вызов статического метода Desktop.getDesktop()
      // возвращать наш ненастоящий Desktop
      desktopMock.when(Desktop::getDesktop).thenReturn(mockDesktopInstance);
      // Убеждаемся, что функция isDesktopSupported() возвращает значение true
      desktopMock.when(Desktop::isDesktopSupported).thenReturn(true);
      // Настраиваем browse в поддельном Desktop так, чтобы он ничего не делал
      doNothing().when(mockDesktopInstance).browse(any(URI.class));

      // Тест
      // help - посмотреть помощь по всему сервису
      assertInstanceOf(SuccessViewModel.class, consoleController.route("help"));

      // help имя_команды - посмотреть помощь по отдельной команде
      assertInstanceOf(SuccessViewModel.class, consoleController.route("help list"));

      // Создать URL
      ViewModel link1 = consoleController.route("https://google.com");
      assertInstanceOf(CreatedLinkViewModel.class, link1);

      // Создать URL с имеющимся UUID
      ViewModel link2 = consoleController.route("https://google.com");
      assertInstanceOf(CreatedLinkViewModel.class, link2);

      // logout - снятие идентификации по UUID
      assertInstanceOf(SuccessViewModel.class, consoleController.route("logout"));

      // login
      assertInstanceOf(
          SuccessViewModel.class,
          consoleController.route("login " + ((CreatedLinkViewModel) link1).creatorUUID));

      // Перейти по URL
      assertInstanceOf(
          SuccessViewModel.class, consoleController.route(((CreatedLinkViewModel) link1).shortURL));

      // list - посмотреть список всех созданных от текущего UUID ссылок, которые сейчас активны
      assertInstanceOf(ListViewModel.class, consoleController.route("list"));

      // stats - просмотреть статистику использований по всем активным ссылкам текущего UUID
      assertInstanceOf(StatsViewModel.class, consoleController.route("stats"));

      // stats URL_ссылки - просмотреть статистику по отдельной действующей короткой ссылке
      assertInstanceOf(
          StatsViewModel.class,
          consoleController.route("stats " + ((CreatedLinkViewModel) link1).shortURL));

      // manage URL_ссылки set limit значение - изменить лимит использований короткой ссылки
      String linkURL = ((CreatedLinkViewModel) link1).shortURL;
      assertInstanceOf(
          SuccessViewModel.class, consoleController.route("manage " + linkURL + " set limit 25"));

      // manage URL_ссылки set original_url значение - изменить URL, на который ведёт короткая
      // ссылка
      assertInstanceOf(
          SuccessViewModel.class,
          consoleController.route("manage " + linkURL + " set original_url https://github.com"));

      // manage URL_ссылки set ttl значение - изменить срок действия
      assertInstanceOf(
          SuccessViewModel.class, consoleController.route("manage " + linkURL + " set ttl 40"));

      // delete URL_ссылки - удалить созданную ранее короткую ссылку
      assertInstanceOf(SuccessViewModel.class, consoleController.route("delete " + linkURL));
    }
  }
}
