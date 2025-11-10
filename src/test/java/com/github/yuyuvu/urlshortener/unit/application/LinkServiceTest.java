package com.github.yuyuvu.urlshortener.unit.application;

import static org.mockito.Mockito.when;

import com.github.yuyuvu.urlshortener.application.LinkService;
import com.github.yuyuvu.urlshortener.application.UserService;
import com.github.yuyuvu.urlshortener.domain.model.ShortLink;
import com.github.yuyuvu.urlshortener.domain.model.User;
import com.github.yuyuvu.urlshortener.domain.repository.ShortLinkRepository;
import com.github.yuyuvu.urlshortener.domain.repository.UserRepository;
import com.github.yuyuvu.urlshortener.exceptions.IllegalCommandParameterException;
import com.github.yuyuvu.urlshortener.exceptions.InvalidOriginalLinkException;
import com.github.yuyuvu.urlshortener.exceptions.InvalidShortLinkException;
import com.github.yuyuvu.urlshortener.exceptions.NotEnoughPermissionsException;
import com.github.yuyuvu.urlshortener.exceptions.OriginalLinkNotFoundException;
import com.github.yuyuvu.urlshortener.exceptions.UsagesLimitReachedException;
import com.github.yuyuvu.urlshortener.infrastructure.config.ConfigManager;
import com.github.yuyuvu.urlshortener.infrastructure.persistence.InMemoryShortLinkRepository;
import com.github.yuyuvu.urlshortener.infrastructure.persistence.InMemoryUserRepository;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

/** Класс для тестов методов из LinkService. */
@ExtendWith(MockitoExtension.class)
public class LinkServiceTest {
  @SuppressWarnings("FieldCanBeLocal")
  private UserRepository userRepository;

  private ShortLinkRepository shortLinkRepository;
  private UserService userService;
  private LinkService linkService;

  @Mock private ConfigManager configManager;

  /** Подготавливаем чистые репозитории и сервисы перед каждым тестом. */
  @BeforeEach
  public void setUpCleanState() {
    userRepository = new InMemoryUserRepository(new HashMap<>());
    userService = new UserService(userRepository);
    shortLinkRepository = new InMemoryShortLinkRepository(new HashMap<>());
    linkService = new LinkService(shortLinkRepository, configManager);
    Mockito.reset(configManager);
  }

  /** Проверяем метод для выяснения, является ли пользователь с некоторым UUID владельцем ссылки. */
  @Test
  void isUUIDOwnerOfShortLinkTest() {
    // Создаём и сохраняем предполагаемого владельца и другого случайного пользователя
    User ownerOfShortLink = userService.makeNewUUIDAndUser();
    userService.saveNewUser(ownerOfShortLink);

    User randomUser = userService.makeNewUUIDAndUser();
    userService.saveNewUser(randomUser);

    // Делаем и сохраняем 1 ссылку для владельца
    ShortLink shortLink =
        new ShortLink(
            "test",
            "12345",
            LocalDateTime.of(2025, 1, 1, 1, 1),
            LocalDateTime.of(2025, 2, 1, 1, 1),
            0,
            10,
            ownerOfShortLink.getUUID(),
            false);

    linkService.saveNewShortLink(shortLink);

    // Ожидаемый нами UUID является владельцем ссылки
    Assertions.assertTrue(
        linkService.isUUIDOwnerOfShortLink(shortLink, ownerOfShortLink.getUUID()));

    // Другой UUID не является владельцем ссылки
    Assertions.assertFalse(linkService.isUUIDOwnerOfShortLink(shortLink, randomUser.getUUID()));
  }

  /**
   * Проверяем метод для выяснения, является ли переданное значение URL, причём не пустым и, скорее
   * всего, ведущим на удалённый сайт.
   */
  @Test
  void validateURLFormatTest() {
    // Проверяем подходящие для сервиса URL
    Assertions.assertDoesNotThrow(() -> linkService.validateURLFormat("https://google.com"));
    Assertions.assertDoesNotThrow(
        () -> linkService.validateURLFormat("https://blog.blog.example.com"));
    Assertions.assertDoesNotThrow(() -> linkService.validateURLFormat("https://github.com/yuyuvu"));
    Assertions.assertDoesNotThrow(() -> linkService.validateURLFormat("https://www.example.com"));
    Assertions.assertDoesNotThrow(() -> linkService.validateURLFormat("http://www.example.com"));
    Assertions.assertDoesNotThrow(() -> linkService.validateURLFormat("http://192.168.1.1/admin"));
    Assertions.assertDoesNotThrow(
        () -> linkService.validateURLFormat("https://example.com:8443/secure"));
    Assertions.assertDoesNotThrow(() -> linkService.validateURLFormat("ftp://example.com"));
    Assertions.assertDoesNotThrow(() -> linkService.validateURLFormat("https://сайт.рф"));
    Assertions.assertDoesNotThrow(
        () -> linkService.validateURLFormat("http://sub.domain.example.com/path"));
    Assertions.assertDoesNotThrow(
        () -> linkService.validateURLFormat("https://example.com:8080/api/v1/users"));
    Assertions.assertDoesNotThrow(
        () -> linkService.validateURLFormat("http://example.com/search?q=test&sort=desc"));
    Assertions.assertDoesNotThrow(
        () -> linkService.validateURLFormat("https://example.com/page#section1"));
    Assertions.assertDoesNotThrow(
        () -> linkService.validateURLFormat("ftp://files.example.com/documents/"));
    Assertions.assertDoesNotThrow(
        () -> linkService.validateURLFormat("https://user:password@example.com"));
    Assertions.assertDoesNotThrow(
        () -> linkService.validateURLFormat("https://api-key:secret@api.example.com"));
    Assertions.assertDoesNotThrow(() -> linkService.validateURLFormat("mailto:test@example.com"));
    Assertions.assertDoesNotThrow(
        () -> linkService.validateURLFormat("https://yulink.tech/gHx68X"));

    // Проверяем невалидные и неподходящие для сервиса URL
    Assertions.assertThrows(
        InvalidOriginalLinkException.class, () -> linkService.validateURLFormat("google.com"));
    Assertions.assertThrows(
        InvalidOriginalLinkException.class, () -> linkService.validateURLFormat("https://com"));
    Assertions.assertThrows(
        InvalidOriginalLinkException.class, () -> linkService.validateURLFormat("https://.com"));
    Assertions.assertThrows(
        InvalidOriginalLinkException.class, () -> linkService.validateURLFormat("https://"));
    Assertions.assertThrows(
        InvalidOriginalLinkException.class, () -> linkService.validateURLFormat("htt"));
    Assertions.assertThrows(
        InvalidOriginalLinkException.class, () -> linkService.validateURLFormat(""));
    Assertions.assertThrows(
        InvalidOriginalLinkException.class,
        () -> linkService.validateURLFormat("http://localhost:3000"));
    Assertions.assertThrows(
        InvalidOriginalLinkException.class,
        () -> linkService.validateURLFormat("http://example..com"));
    Assertions.assertThrows(
        InvalidOriginalLinkException.class,
        () -> linkService.validateURLFormat("http://...example.com"));
    Assertions.assertThrows(
        InvalidOriginalLinkException.class,
        () -> linkService.validateURLFormat("http://e x a mp le.co m"));
    Assertions.assertThrows(
        InvalidOriginalLinkException.class,
        () -> linkService.validateURLFormat("httpx://example.com"));
    Assertions.assertThrows(
        InvalidOriginalLinkException.class, () -> linkService.validateURLFormat("tel:+1234567890"));
    Assertions.assertThrows(
        InvalidOriginalLinkException.class,
        () -> linkService.validateURLFormat("sms:+1234567890?body=Hello"));
    Assertions.assertThrows(
        InvalidOriginalLinkException.class,
        () -> linkService.validateURLFormat("whatsapp://send?text=Hi"));
    Assertions.assertThrows(
        InvalidOriginalLinkException.class, () -> linkService.validateURLFormat("://example.com"));
    Assertions.assertThrows(
        InvalidOriginalLinkException.class,
        () -> linkService.validateURLFormat("http:example.com"));
    Assertions.assertThrows(
        InvalidOriginalLinkException.class,
        () -> linkService.validateURLFormat("https://-example.com"));
    Assertions.assertThrows(
        InvalidOriginalLinkException.class,
        () -> linkService.validateURLFormat("http://example-.com"));
  }

  /**
   * Проверяем метод для выяснения, существует и активна ли короткая ссылка сервиса по указанному
   * полному URL потенциальной короткой ссылки.
   */
  @Test
  void validateShortLinkExistenceTest() {
    // Делаем и сохраняем 1 ссылку для владельца
    ShortLink shortLink =
        new ShortLink(
            "test",
            "EXISTING",
            LocalDateTime.of(2025, 1, 1, 1, 1),
            LocalDateTime.of(2025, 2, 1, 1, 1),
            0,
            10,
            userService.makeNewUUIDAndUser().getUUID(),
            false);

    linkService.saveNewShortLink(shortLink);

    // Подставляем нужные настройки
    when(configManager.getDefaultServiceBaseURLProperty()).thenReturn("https://yulink.tech/");
    when(configManager.getLegacyServiceBaseURLProperty()).thenReturn(new String[] {""});

    // Проверяем существование созданной ссылки и равенство основных параметров
    Assertions.assertDoesNotThrow(
        () -> {
          linkService.validateShortLinkExistence("https://yulink.tech/EXISTING");
          Assertions.assertEquals(
              "EXISTING",
              linkService.validateShortLinkExistence("https://yulink.tech/EXISTING").getShortId());
          Assertions.assertEquals(
              "test",
              linkService
                  .validateShortLinkExistence("https://yulink.tech/EXISTING")
                  .getOriginalURLAddress());
          Assertions.assertEquals(
              0,
              linkService
                  .validateShortLinkExistence("https://yulink.tech/EXISTING")
                  .getUsageCounter());
        });

    // Проверяем что случайная другая ссылка не существует
    Assertions.assertThrows(
        OriginalLinkNotFoundException.class,
        () -> linkService.validateShortLinkExistence("https://yulink.tech/NONEXISTENT"));

    // Удаляем созданную ссылку и проверяем снова
    linkService.uncheckedDeleteShortLinkByShortId(shortLink.getShortId());
    Assertions.assertThrows(
        OriginalLinkNotFoundException.class,
        () -> linkService.validateShortLinkExistence("https://yulink.tech/EXISTING"));
  }

  /**
   * Проверяем метод для выяснения, что короткий URL НЕ начинается с одного из распознаваемых нами
   * URL нашего сервиса.
   */
  @Test
  void checkShortLinkDoesNotStartWithServiceBaseURLTest() {
    // Подставляем нужные настройки service URL
    when(configManager.getDefaultServiceBaseURLProperty()).thenReturn("https://yulink.tech/");
    when(configManager.getLegacyServiceBaseURLProperty())
        .thenReturn(new String[] {"https://legacy-url.tech/", "https://veryoldurl.tech/"});

    // Проверяем, что метод правильно распознает потенциальные ссылки нашего сервиса
    Assertions.assertFalse(
        linkService.checkShortLinkDoesNotStartWithServiceBaseURL("https://yulink.tech/"));
    Assertions.assertFalse(
        linkService.checkShortLinkDoesNotStartWithServiceBaseURL("https://legacy-url.tech/"));
    Assertions.assertFalse(
        linkService.checkShortLinkDoesNotStartWithServiceBaseURL("https://veryoldurl.tech/"));

    Assertions.assertFalse(
        linkService.checkShortLinkDoesNotStartWithServiceBaseURL("https://yulink.tech/ABC123"));
    Assertions.assertFalse(
        linkService.checkShortLinkDoesNotStartWithServiceBaseURL(
            "https://legacy-url.tech/XYZ2456"));
    Assertions.assertFalse(
        linkService.checkShortLinkDoesNotStartWithServiceBaseURL("https://veryoldurl.tech/QWERTY"));

    // Проверяем, что метод не распознаёт другие ссылки или строки как ссылки нашего сервиса
    Assertions.assertTrue(linkService.checkShortLinkDoesNotStartWithServiceBaseURL(""));
    Assertions.assertTrue(linkService.checkShortLinkDoesNotStartWithServiceBaseURL("QWERTY"));
    Assertions.assertTrue(
        linkService.checkShortLinkDoesNotStartWithServiceBaseURL("https://google.com/"));
    Assertions.assertTrue(
        linkService.checkShortLinkDoesNotStartWithServiceBaseURL("https://google.com/123456"));
    Assertions.assertTrue(
        linkService.checkShortLinkDoesNotStartWithServiceBaseURL("https://github.com/ABC123"));

    // Теперь убираем legacy service url
    when(configManager.getDefaultServiceBaseURLProperty()).thenReturn("https://yulink.tech/");
    when(configManager.getLegacyServiceBaseURLProperty()).thenReturn(new String[] {""});

    // Проверяем, что метод правильно распознает только активный service URL
    Assertions.assertFalse(
        linkService.checkShortLinkDoesNotStartWithServiceBaseURL("https://yulink.tech/"));
    Assertions.assertTrue(
        linkService.checkShortLinkDoesNotStartWithServiceBaseURL("https://legacy-url.tech/"));
    Assertions.assertTrue(
        linkService.checkShortLinkDoesNotStartWithServiceBaseURL("https://veryoldurl.tech/"));

    Assertions.assertFalse(
        linkService.checkShortLinkDoesNotStartWithServiceBaseURL("https://yulink.tech/ABC123"));
    Assertions.assertTrue(
        linkService.checkShortLinkDoesNotStartWithServiceBaseURL(
            "https://legacy-url.tech/XYZ2456"));
    Assertions.assertTrue(
        linkService.checkShortLinkDoesNotStartWithServiceBaseURL("https://veryoldurl.tech/QWERTY"));
  }

  /**
   * Проверяем метод, который делит полный адрес короткого URL на URL сервиса сокращения ссылок и на
   * ID самого короткого URL. Перед использованием метода должна проводиться проверка из метода
   * checkShortLinkDoesNotStartWithServiceBaseURL, иначе он выдаст случайный результат или
   * RuntimeException.
   */
  @Test
  void splitShortLinkAndServiceBaseURLTest() {
    // Подставляем нужные настройки service URL
    when(configManager.getDefaultServiceBaseURLProperty()).thenReturn("https://yulink.tech/");
    when(configManager.getLegacyServiceBaseURLProperty())
        .thenReturn(new String[] {"https://legacy-url.tech/", "https://veryoldurl.tech/"});

    // Проверяем, что метод правильно делит распознанные ранее ссылки нашего сервиса
    // Пустой ID
    Assertions.assertFalse(
        linkService.checkShortLinkDoesNotStartWithServiceBaseURL("https://yulink.tech/"));
    Assertions.assertEquals(
        "https://yulink.tech/",
        linkService.splitShortLinkAndServiceBaseURL("https://yulink.tech/").getServiceURL());
    Assertions.assertEquals(
        "", linkService.splitShortLinkAndServiceBaseURL("https://yulink.tech/").getShortID());

    // Вместе с ID
    Assertions.assertFalse(
        linkService.checkShortLinkDoesNotStartWithServiceBaseURL("https://yulink.tech/ABC123"));
    Assertions.assertEquals(
        "https://yulink.tech/",
        linkService.splitShortLinkAndServiceBaseURL("https://yulink.tech/ABC123").getServiceURL());
    Assertions.assertEquals(
        "ABC123",
        linkService.splitShortLinkAndServiceBaseURL("https://yulink.tech/ABC123").getShortID());

    // Пустой ID
    Assertions.assertFalse(
        linkService.checkShortLinkDoesNotStartWithServiceBaseURL("https://legacy-url.tech/"));
    Assertions.assertEquals(
        "https://legacy-url.tech/",
        linkService.splitShortLinkAndServiceBaseURL("https://legacy-url.tech/").getServiceURL());
    Assertions.assertEquals(
        "", linkService.splitShortLinkAndServiceBaseURL("https://legacy-url.tech/").getShortID());

    // Вместе с ID
    Assertions.assertFalse(
        linkService.checkShortLinkDoesNotStartWithServiceBaseURL("https://legacy-url.tech/"));
    Assertions.assertEquals(
        "https://legacy-url.tech/",
        linkService
            .splitShortLinkAndServiceBaseURL("https://legacy-url.tech/ABC123")
            .getServiceURL());
    Assertions.assertEquals(
        "ABC123",
        linkService.splitShortLinkAndServiceBaseURL("https://legacy-url.tech/ABC123").getShortID());

    // Пустой ID
    Assertions.assertFalse(
        linkService.checkShortLinkDoesNotStartWithServiceBaseURL("https://veryoldurl.tech/"));
    Assertions.assertEquals(
        "https://veryoldurl.tech/",
        linkService.splitShortLinkAndServiceBaseURL("https://veryoldurl.tech/").getServiceURL());
    Assertions.assertEquals(
        "", linkService.splitShortLinkAndServiceBaseURL("https://veryoldurl.tech/").getShortID());

    // Вместе с ID
    Assertions.assertFalse(
        linkService.checkShortLinkDoesNotStartWithServiceBaseURL("https://veryoldurl.tech/ABC123"));
    Assertions.assertEquals(
        "https://veryoldurl.tech/",
        linkService
            .splitShortLinkAndServiceBaseURL("https://veryoldurl.tech/ABC123")
            .getServiceURL());
    Assertions.assertEquals(
        "ABC123",
        linkService.splitShortLinkAndServiceBaseURL("https://veryoldurl.tech/ABC123").getShortID());

    // Ошибки работы с индексами при вызове без проверки
    Assertions.assertThrows(
        RuntimeException.class,
        () -> linkService.splitShortLinkAndServiceBaseURL("https://google.com/"));
  }

  /**
   * Проверяем метод, который создаёт новый объект ShortLink. Все значения берутся из конфигурации:
   * единица измерения TTL, стандартное значение TTL, стандартное значение лимита использований.
   * Внутренний короткий ID ссылки генерируется в generateShortLinkID.
   */
  @Test
  void makeNewShortLinkTest() {
    // Подставляем нужные настройки для service URL
    when(configManager.getDefaultServiceBaseURLProperty()).thenReturn("https://yulink.tech/");
    when(configManager.getLegacyServiceBaseURLProperty())
        .thenReturn(new String[] {"https://legacy-url.tech/", "https://veryoldurl.tech/"});
    when(configManager.getDefaultShortLinkTTLTimeUnitProperty())
        .thenReturn(ConfigManager.TimeUnit.MINUTES);
    when(configManager.getDefaultShortLinkTTLInUnitsProperty()).thenReturn(15);
    when(configManager.getDefaultShortLinkUsageLimitProperty()).thenReturn(5);
    char[] allowedCharacters = new char[] {'a', 'b', 'c'};
    when(configManager.getShortLinkAllowedCharactersProperty()).thenReturn(allowedCharacters);
    when(configManager.getDefaultShortLinkIdLengthProperty()).thenReturn(10);

    // Создаём новую ссылку и проверяем, что она получила все подставляемые и вычисляемые
    // параметры в ожидаемом виде
    UUID ownerOfShortLink = UUID.randomUUID();
    final ShortLink[] shortLink = new ShortLink[1];

    Assertions.assertDoesNotThrow(
        () -> shortLink[0] = linkService.makeNewShortLink("https://github.com", ownerOfShortLink));

    // Проверяем все параметры
    Assertions.assertEquals("https://github.com", shortLink[0].getOriginalURLAddress());
    Assertions.assertEquals(ownerOfShortLink, shortLink[0].getOwnerOfShortURL());

    Assertions.assertEquals(0, shortLink[0].getUsageCounter());
    Assertions.assertEquals(5, shortLink[0].getUsageLimitAmount());

    Assertions.assertEquals(
        shortLink[0].getCreationDateTime().plusMinutes(15), shortLink[0].getExpirationDateTime());
    Assertions.assertTrue(
        shortLink[0].getCreationDateTime().isBefore(LocalDateTime.now().plusMinutes(5)));

    Assertions.assertEquals(10, shortLink[0].getShortId().length());
    Assertions.assertTrue(
        shortLink[0]
            .getShortId()
            .chars()
            .mapToObj(c -> (char) c)
            .allMatch(
                ch ->
                    allowedCharacters[0] == ch
                        || allowedCharacters[1] == ch
                        || allowedCharacters[2] == ch));

    Assertions.assertFalse(shortLink[0].isLimitNotified());
  }

  /**
   * Проверяем метод, который отвечает за редирект по короткому URL. Проводит валидацию введённого
   * URL на принадлежность к нашему сервису.
   */
  @Test
  void redirectByShortLinkTest()
      throws InvalidOriginalLinkException,
          UsagesLimitReachedException,
          OriginalLinkNotFoundException,
          IOException,
          InvalidShortLinkException {
    // Подставляем нужные настройки
    when(configManager.getDefaultServiceBaseURLProperty()).thenReturn("https://yulink.tech/");
    when(configManager.getLegacyServiceBaseURLProperty())
        .thenReturn(new String[] {"https://legacy-url.tech/", "https://veryoldurl.tech/"});
    when(configManager.getDefaultShortLinkTTLTimeUnitProperty())
        .thenReturn(ConfigManager.TimeUnit.MINUTES);
    when(configManager.getDefaultShortLinkTTLInUnitsProperty()).thenReturn(15);
    when(configManager.getDefaultShortLinkUsageLimitProperty()).thenReturn(5);
    char[] allowedCharacters = new char[] {'a', 'b', 'c'};
    when(configManager.getShortLinkAllowedCharactersProperty()).thenReturn(allowedCharacters);
    when(configManager.getDefaultShortLinkIdLengthProperty()).thenReturn(10);

    // Создаём новую ссылку и сохраняем её в репозиторий
    UUID ownerOfShortLink = UUID.randomUUID();
    final ShortLink[] shortLink = new ShortLink[1];
    shortLink[0] =
        linkService.saveNewShortLink(
            linkService.makeNewShortLink("https://github.com", ownerOfShortLink));
    String shortLinkId = shortLink[0].getShortId();

    // Проверяем редирект по данной созданной ссылке: ошибок быть не должно,
    // так как короткая ссылка действующая
    Assertions.assertDoesNotThrow(
        () -> linkService.redirectByShortLink("https://yulink.tech/" + shortLinkId, false));

    // Должен возвращаться оригинальный URL для которого создавалась ссылка
    Assertions.assertEquals(
        "https://github.com",
        linkService.redirectByShortLink("https://yulink.tech/" + shortLinkId, false));

    // Проверяем увеличение счётчика на 2 и отсутствие израсходования лимита использований
    Assertions.assertEquals(2, shortLink[0].getUsageCounter());
    Assertions.assertFalse(shortLink[0].isLimitReached());

    // Проверяем ошибки, выбрасываемые при попытке перейти по несуществующей короткой ссылке
    // Не ссылка сервиса
    Assertions.assertThrows(
        InvalidShortLinkException.class,
        () -> linkService.redirectByShortLink("https://google.com/" + shortLinkId, false));

    // Неактивная ссылка сервиса
    Assertions.assertThrows(
        OriginalLinkNotFoundException.class,
        () -> linkService.redirectByShortLink("https://yulink.tech/" + "NONEXISTENT", false));

    // Редирект не работает при израсходовании лимита
    linkService.redirectByShortLink("https://yulink.tech/" + shortLinkId, false);
    linkService.redirectByShortLink("https://yulink.tech/" + shortLinkId, false);
    linkService.redirectByShortLink("https://yulink.tech/" + shortLinkId, false);
    Assertions.assertThrows(
        UsagesLimitReachedException.class,
        () -> linkService.redirectByShortLink("https://yulink.tech/" + shortLinkId, false));

    // Редирект не работает при истечении срока действия ссылки
    shortLink[0].setExpirationDateTime(LocalDateTime.now().minusMinutes(5));
    Assertions.assertThrows(
        InvalidShortLinkException.class,
        () -> linkService.redirectByShortLink("https://yulink.tech/" + shortLinkId, false));
  }

  /**
   * Проверяем метод для управления созданной короткой ссылкой: позволяет вручную изменить лимит
   * использований.
   */
  @Test
  void changeShortLinkUsageLimitTest()
      throws InvalidOriginalLinkException, UsagesLimitReachedException {
    // Подставляем нужные настройки
    when(configManager.getDefaultServiceBaseURLProperty()).thenReturn("https://yulink.tech/");
    when(configManager.getLegacyServiceBaseURLProperty())
        .thenReturn(new String[] {"https://legacy-url.tech/", "https://veryoldurl.tech/"});
    when(configManager.getDefaultShortLinkTTLTimeUnitProperty())
        .thenReturn(ConfigManager.TimeUnit.MINUTES);
    when(configManager.getDefaultShortLinkTTLInUnitsProperty()).thenReturn(15);
    when(configManager.getDefaultShortLinkUsageLimitProperty()).thenReturn(5);
    char[] allowedCharacters = new char[] {'a', 'b', 'c'};
    when(configManager.getShortLinkAllowedCharactersProperty()).thenReturn(allowedCharacters);
    when(configManager.getDefaultShortLinkIdLengthProperty()).thenReturn(10);

    // Создаём новую ссылку и сохраняем её в репозиторий
    UUID ownerOfShortLink = UUID.randomUUID();
    final ShortLink[] shortLink = new ShortLink[1];
    shortLink[0] =
        linkService.saveNewShortLink(
            linkService.makeNewShortLink("https://github.com", ownerOfShortLink));
    String shortLinkId = shortLink[0].getShortId();

    // Устанавливаем недостающие настройки
    when(configManager.getUserSetShortLinkUsageLimitProperty()).thenReturn(15);

    // Проверяем, что лимит нельзя изменить у несуществующей ссылки
    Assertions.assertThrows(
        InvalidShortLinkException.class,
        () ->
            linkService.changeShortLinkUsageLimit(
                "https://google.com/" + shortLinkId, ownerOfShortLink, 5));

    // Проверяем, что лимит нельзя изменить у чужой ссылки
    Assertions.assertThrows(
        NotEnoughPermissionsException.class,
        () ->
            linkService.changeShortLinkUsageLimit(
                "https://yulink.tech/" + shortLinkId, UUID.randomUUID(), 5));

    // Проверяем, что лимит нельзя выставить в нулевое или отрицательное значение
    Assertions.assertThrows(
        IllegalCommandParameterException.class,
        () ->
            linkService.changeShortLinkUsageLimit(
                "https://yulink.tech/" + shortLinkId, ownerOfShortLink, -15));
    Assertions.assertThrows(
        IllegalCommandParameterException.class,
        () ->
            linkService.changeShortLinkUsageLimit(
                "https://yulink.tech/" + shortLinkId, ownerOfShortLink, 0));

    // Проверяем, что лимит нельзя выставить в запрещённое настройками значение
    // (выставили ранее не более 15), попросим 16
    Assertions.assertThrows(
        IllegalCommandParameterException.class,
        () ->
            linkService.changeShortLinkUsageLimit(
                "https://yulink.tech/" + shortLinkId, ownerOfShortLink, 16));

    // Проверяем, что лимит можно выставить без проблем с нормальными параметрами
    Assertions.assertDoesNotThrow(
        () ->
            linkService.changeShortLinkUsageLimit(
                "https://yulink.tech/" + shortLinkId, ownerOfShortLink, 15));

    // Проверяем, что лимит нельзя выставить на значение меньшее или равное
    // текущему счётчику использований
    for (int i = 1; i <= 5; i++) {
      shortLink[0].incrementUsageCounter();
    }
    Assertions.assertThrows(
        IllegalCommandParameterException.class,
        () ->
            linkService.changeShortLinkUsageLimit(
                "https://yulink.tech/" + shortLinkId, ownerOfShortLink, 4));

    // Проверяем, что лимит нельзя выставить по ссылке с уже израсходованным лимитом
    // для обеспечения надёжной блокировки ссылки без обходов
    for (int i = 1; i <= 10; i++) {
      shortLink[0].incrementUsageCounter();
    }
    Assertions.assertTrue(shortLink[0].isLimitReached());
    Assertions.assertThrows(
        IllegalCommandParameterException.class,
        () ->
            linkService.changeShortLinkUsageLimit(
                "https://yulink.tech/" + shortLinkId, ownerOfShortLink, 5));
  }

  /**
   * Проверяем метод для управления созданной короткой ссылкой: позволяет вручную изменить
   * оригинальный URL внутри короткого URL.
   */
  @Test
  void changeShortLinkOriginalURLTest() throws InvalidOriginalLinkException {
    // Подставляем нужные настройки
    when(configManager.getDefaultServiceBaseURLProperty()).thenReturn("https://yulink.tech/");
    when(configManager.getLegacyServiceBaseURLProperty())
        .thenReturn(new String[] {"https://legacy-url.tech/", "https://veryoldurl.tech/"});
    when(configManager.getDefaultShortLinkTTLTimeUnitProperty())
        .thenReturn(ConfigManager.TimeUnit.MINUTES);
    when(configManager.getDefaultShortLinkTTLInUnitsProperty()).thenReturn(15);
    when(configManager.getDefaultShortLinkUsageLimitProperty()).thenReturn(5);
    char[] allowedCharacters = new char[] {'a', 'b', 'c'};
    when(configManager.getShortLinkAllowedCharactersProperty()).thenReturn(allowedCharacters);
    when(configManager.getDefaultShortLinkIdLengthProperty()).thenReturn(10);

    // Создаём новую ссылку и сохраняем её в репозиторий
    UUID ownerOfShortLink = UUID.randomUUID();
    final ShortLink[] shortLink = new ShortLink[1];
    shortLink[0] =
        linkService.saveNewShortLink(
            linkService.makeNewShortLink("https://github.com", ownerOfShortLink));
    String shortLinkId = shortLink[0].getShortId();

    // Логика нового URL.
    // Сохраняем старый оригинальный URL в переменную
    String oldShortLinkOriginalURL = shortLink[0].getOriginalURLAddress();
    // Проверяем, что URL нельзя изменить у несуществующей ссылки
    Assertions.assertThrows(
        InvalidShortLinkException.class,
        () ->
            linkService.changeShortLinkOriginalURL(
                "https://google.com/" + shortLinkId, ownerOfShortLink, "https://java.com"));

    // Проверяем, что URL нельзя изменить у чужой ссылки
    Assertions.assertThrows(
        NotEnoughPermissionsException.class,
        () ->
            linkService.changeShortLinkOriginalURL(
                "https://yulink.tech/" + shortLinkId, UUID.randomUUID(), "https://java.com"));

    // Проверяем, что новый URL валидный
    Assertions.assertThrows(
        InvalidOriginalLinkException.class,
        () ->
            linkService.changeShortLinkOriginalURL(
                "https://yulink.tech/" + shortLinkId, ownerOfShortLink, "https://-ja..va-.com"));
    Assertions.assertThrows(
        IllegalCommandParameterException.class,
        () ->
            linkService.changeShortLinkOriginalURL(
                "https://yulink.tech/" + shortLinkId, ownerOfShortLink, ""));

    // Проверяем, что URL меняется при нормальных параметрах
    Assertions.assertDoesNotThrow(
        () ->
            linkService.changeShortLinkOriginalURL(
                "https://yulink.tech/" + shortLinkId, ownerOfShortLink, "https://java.com"));

    // Проверка неравенства старого и нового URL после смены
    Assertions.assertNotEquals(oldShortLinkOriginalURL, shortLink[0].getOriginalURLAddress());
    Assertions.assertEquals("https://java.com", shortLink[0].getOriginalURLAddress());
  }

  /**
   * Проверяем метод для управления созданной короткой ссылкой: позволяет вручную изменить TTL
   * ссылки после создания.
   */
  @Test
  void changeShortLinkTTLExpirationLimitTest() throws InvalidOriginalLinkException {
    // Подставляем нужные настройки
    when(configManager.getDefaultServiceBaseURLProperty()).thenReturn("https://yulink.tech/");
    when(configManager.getLegacyServiceBaseURLProperty())
        .thenReturn(new String[] {"https://legacy-url.tech/", "https://veryoldurl.tech/"});
    when(configManager.getDefaultShortLinkTTLTimeUnitProperty())
        .thenReturn(ConfigManager.TimeUnit.MINUTES);
    when(configManager.getDefaultShortLinkTTLInUnitsProperty()).thenReturn(15);
    when(configManager.getDefaultShortLinkUsageLimitProperty()).thenReturn(5);
    char[] allowedCharacters = new char[] {'a', 'b', 'c'};
    when(configManager.getShortLinkAllowedCharactersProperty()).thenReturn(allowedCharacters);
    when(configManager.getDefaultShortLinkIdLengthProperty()).thenReturn(10);

    // Создаём новую ссылку и сохраняем её в репозиторий
    UUID ownerOfShortLink = UUID.randomUUID();
    final ShortLink[] shortLink = new ShortLink[1];
    shortLink[0] =
        linkService.saveNewShortLink(
            linkService.makeNewShortLink("https://github.com", ownerOfShortLink));
    String shortLinkId = shortLink[0].getShortId();

    // Логика нового TTL.
    // Подставляем в конфиге максимальный TTL, на который
    // пользователь может изменить текущий TTL (30)
    // Ранее стандартный выставили в 15
    when(configManager.getUserSetShortLinkMaxTTLInUnitsProperty()).thenReturn(30);

    // Проверяем, что TTL нельзя изменить у несуществующей ссылки
    Assertions.assertThrows(
        InvalidShortLinkException.class,
        () ->
            linkService.changeShortLinkTTLExpirationLimit(
                "https://google.com/" + shortLinkId, ownerOfShortLink, 5));

    // Проверяем, что TTL нельзя изменить у чужой ссылки
    Assertions.assertThrows(
        NotEnoughPermissionsException.class,
        () ->
            linkService.changeShortLinkTTLExpirationLimit(
                "https://yulink.tech/" + shortLinkId, UUID.randomUUID(), 5));

    // Проверяем, что TTL нельзя выставить в нулевое или отрицательное значение
    Assertions.assertThrows(
        IllegalCommandParameterException.class,
        () ->
            linkService.changeShortLinkTTLExpirationLimit(
                "https://yulink.tech/" + shortLinkId, ownerOfShortLink, -15));
    Assertions.assertThrows(
        IllegalCommandParameterException.class,
        () ->
            linkService.changeShortLinkTTLExpirationLimit(
                "https://yulink.tech/" + shortLinkId, ownerOfShortLink, 0));

    // Проверяем, что TTL нельзя выставить в запрещённое настройками значение
    // (выставили ранее не более 30), попросим 35
    Assertions.assertThrows(
        IllegalCommandParameterException.class,
        () ->
            linkService.changeShortLinkTTLExpirationLimit(
                "https://yulink.tech/" + shortLinkId, ownerOfShortLink, 35));

    // Проверяем, что TTL можно выставить без проблем с нормальными параметрами
    Assertions.assertDoesNotThrow(
        () ->
            linkService.changeShortLinkTTLExpirationLimit(
                "https://yulink.tech/" + shortLinkId, ownerOfShortLink, 29));
    Assertions.assertEquals(
        shortLink[0].getCreationDateTime().plusMinutes(29), shortLink[0].getExpirationDateTime());

    // Запрещаем выставлять новый TTL таким, что он приведёт к автоудалению и автоблокировке
    // ссылки моментально (ссылка с новым TTL будет уже истёкшей)
    // Выставили лимит в 29 минут, считаем, что прошло уже 20, а просим задать новый лимит в 3.
    shortLink[0].setCreationDateTime(LocalDateTime.now().minusMinutes(20), true);
    Assertions.assertThrows(
        IllegalCommandParameterException.class,
        () ->
            linkService.changeShortLinkTTLExpirationLimit(
                "https://yulink.tech/" + shortLinkId, ownerOfShortLink, 3));
  }

  /**
   * Проверяем метод calculateNewExpirationDateTimeForShortLinkToChange для подсчётов даты истечения
   * короткой ссылки при её изменении.
   */
  @Test
  void calculateNewExpirationDateTimeForShortLinkToChangeTest()
      throws InvalidOriginalLinkException {
    // Подставляем нужные настройки
    when(configManager.getDefaultServiceBaseURLProperty()).thenReturn("https://yulink.tech/");
    when(configManager.getLegacyServiceBaseURLProperty())
        .thenReturn(new String[] {"https://legacy-url.tech/", "https://veryoldurl.tech/"});
    when(configManager.getDefaultShortLinkTTLTimeUnitProperty())
        .thenReturn(ConfigManager.TimeUnit.MINUTES);
    when(configManager.getDefaultShortLinkTTLInUnitsProperty()).thenReturn(15);
    when(configManager.getDefaultShortLinkUsageLimitProperty()).thenReturn(5);
    char[] allowedCharacters = new char[] {'a', 'b', 'c'};
    when(configManager.getShortLinkAllowedCharactersProperty()).thenReturn(allowedCharacters);
    when(configManager.getDefaultShortLinkIdLengthProperty()).thenReturn(10);

    // Создаём новую ссылку и сохраняем её в репозиторий
    UUID ownerOfShortLink = UUID.randomUUID();
    final ShortLink[] shortLink = new ShortLink[1];
    shortLink[0] =
        linkService.saveNewShortLink(
            linkService.makeNewShortLink("https://github.com", ownerOfShortLink));

    // Проверяем метод calculateNewExpirationDateTimeForShortLinkToChange
    // Для расчётов нового expirationDateTime при смене TTL.
    // Выставили ранее минуты, проверяем со всеми значениями
    Assertions.assertEquals(
        shortLink[0].getCreationDateTime().plusMinutes(20),
        linkService.calculateNewExpirationDateTimeForShortLinkToChange(20, shortLink[0]));

    when(configManager.getDefaultShortLinkTTLTimeUnitProperty())
        .thenReturn(ConfigManager.TimeUnit.HOURS);
    Assertions.assertEquals(
        shortLink[0].getCreationDateTime().plusHours(20),
        linkService.calculateNewExpirationDateTimeForShortLinkToChange(20, shortLink[0]));

    when(configManager.getDefaultShortLinkTTLTimeUnitProperty())
        .thenReturn(ConfigManager.TimeUnit.SECONDS);
    Assertions.assertEquals(
        shortLink[0].getCreationDateTime().plusSeconds(20),
        linkService.calculateNewExpirationDateTimeForShortLinkToChange(20, shortLink[0]));

    when(configManager.getDefaultShortLinkTTLTimeUnitProperty())
        .thenReturn(ConfigManager.TimeUnit.DAYS);
    Assertions.assertEquals(
        shortLink[0].getCreationDateTime().plusDays(20),
        linkService.calculateNewExpirationDateTimeForShortLinkToChange(20, shortLink[0]));
  }

  /**
   * Проверяем методы для удаления созданной короткой ссылки. deleteShortLink с валидацией и
   * uncheckedDeleteShortLinkByShortId без неё.
   */
  @Test
  void deleteShortLinkTest() throws InvalidOriginalLinkException {
    // Подставляем нужные настройки
    when(configManager.getDefaultServiceBaseURLProperty()).thenReturn("https://yulink.tech/");
    when(configManager.getLegacyServiceBaseURLProperty())
        .thenReturn(new String[] {"https://legacy-url.tech/", "https://veryoldurl.tech/"});
    when(configManager.getDefaultShortLinkTTLTimeUnitProperty())
        .thenReturn(ConfigManager.TimeUnit.MINUTES);
    when(configManager.getDefaultShortLinkTTLInUnitsProperty()).thenReturn(15);
    when(configManager.getDefaultShortLinkUsageLimitProperty()).thenReturn(5);
    char[] allowedCharacters = new char[] {'a', 'b', 'c'};
    when(configManager.getShortLinkAllowedCharactersProperty()).thenReturn(allowedCharacters);
    when(configManager.getDefaultShortLinkIdLengthProperty()).thenReturn(10);

    // Создаём новую ссылку и сохраняем её в репозиторий
    UUID ownerOfShortLink = UUID.randomUUID();
    final ShortLink[] shortLink = new ShortLink[1];
    shortLink[0] =
        linkService.saveNewShortLink(
            linkService.makeNewShortLink("https://github.com", ownerOfShortLink));
    String shortLinkId = shortLink[0].getShortId();

    // Проверяем, что несуществующую ссылку нельзя удалить
    Assertions.assertThrows(
        InvalidShortLinkException.class,
        () -> linkService.deleteShortLink("https://google.com/" + shortLinkId, ownerOfShortLink));

    // Проверяем, что чужую ссылку нельзя удалить
    Assertions.assertThrows(
        NotEnoughPermissionsException.class,
        () -> linkService.deleteShortLink("https://yulink.tech/" + shortLinkId, UUID.randomUUID()));

    // Проверяем, что перед удалением ссылка ещё есть в репозитории
    Assertions.assertTrue(shortLinkRepository.getShortLinkByShortID(shortLinkId).isPresent());
    Assertions.assertDoesNotThrow(
        () -> linkService.validateShortLinkExistence("https://yulink.tech/" + shortLinkId));

    // Удаляем
    Assertions.assertDoesNotThrow(
        () -> linkService.deleteShortLink("https://yulink.tech/" + shortLinkId, ownerOfShortLink));

    // Проверяем, что после удаления ссылка пропала из репозитория
    Assertions.assertFalse(shortLinkRepository.getShortLinkByShortID(shortLinkId).isPresent());
    Assertions.assertThrows(
        OriginalLinkNotFoundException.class,
        () -> linkService.validateShortLinkExistence("https://yulink.tech/" + shortLinkId));

    // Проверяем метод uncheckedDeleteShortLinkByShortId для удаления без валидации.
    // Создаём новую ссылку и сохраняем её в репозиторий
    UUID ownerOfShortLinkSecond = UUID.randomUUID();
    final ShortLink[] shortLinkSecond = new ShortLink[1];
    shortLinkSecond[0] =
        linkService.saveNewShortLink(
            linkService.makeNewShortLink("https://github.com", ownerOfShortLinkSecond));
    String shortLinkSecondId = shortLinkSecond[0].getShortId();

    // Проверяем, что перед удалением ссылка ещё есть в репозитории
    Assertions.assertTrue(shortLinkRepository.getShortLinkByShortID(shortLinkSecondId).isPresent());
    Assertions.assertDoesNotThrow(
        () -> linkService.validateShortLinkExistence("https://yulink.tech/" + shortLinkSecondId));

    // Удаляем
    Assertions.assertTrue(linkService.uncheckedDeleteShortLinkByShortId(shortLinkSecondId));

    // Проверяем, что после удаления ссылка пропала из репозитория
    Assertions.assertFalse(
        shortLinkRepository.getShortLinkByShortID(shortLinkSecondId).isPresent());
    Assertions.assertThrows(
        OriginalLinkNotFoundException.class,
        () -> linkService.validateShortLinkExistence("https://yulink.tech/" + shortLinkSecondId));
  }

  /**
   * Проверяем метод для получения списка созданных коротких ссылок пользователя. Например, для
   * команд list и stats. А также метод для получения списка всех коротких ссылок из репозитория.
   * Используется для автопроверок на израсходование лимита использования или на окончание срока
   * действия ссылки.
   */
  @Test
  void listShortLinksTest() throws InvalidOriginalLinkException {
    // Подставляем нужные настройки
    when(configManager.getDefaultServiceBaseURLProperty()).thenReturn("https://yulink.tech/");
    when(configManager.getLegacyServiceBaseURLProperty())
        .thenReturn(new String[] {"https://legacy-url.tech/", "https://veryoldurl.tech/"});
    when(configManager.getDefaultShortLinkTTLTimeUnitProperty())
        .thenReturn(ConfigManager.TimeUnit.MINUTES);
    when(configManager.getDefaultShortLinkTTLInUnitsProperty()).thenReturn(15);
    when(configManager.getDefaultShortLinkUsageLimitProperty()).thenReturn(5);
    char[] allowedCharacters = new char[] {'a', 'b', 'c'};
    when(configManager.getShortLinkAllowedCharactersProperty()).thenReturn(allowedCharacters);
    when(configManager.getDefaultShortLinkIdLengthProperty()).thenReturn(10);

    // Добавляем три новых ссылки одного пользователя и сохраняем их в репозиторий
    UUID ownerOfShortLink = UUID.randomUUID();
    final ShortLink[] shortLink = new ShortLink[3];
    shortLink[0] =
        linkService.saveNewShortLink(
            linkService.makeNewShortLink("https://github.com", ownerOfShortLink));
    shortLink[1] =
        linkService.saveNewShortLink(
            linkService.makeNewShortLink("https://github.com", ownerOfShortLink));
    shortLink[2] =
        linkService.saveNewShortLink(
            linkService.makeNewShortLink("https://github.com", ownerOfShortLink));

    // Добавляем одну новую ссылку другого пользователя и сохраняем её в репозиторий
    UUID secondOwnerOfShortLink = UUID.randomUUID();
    final ShortLink[] secondShortLink = new ShortLink[3];
    secondShortLink[0] =
        linkService.saveNewShortLink(
            linkService.makeNewShortLink("https://github.com", secondOwnerOfShortLink));

    // Проверяем, что сервис отдаёт нужное количество ссылок с правильными ID
    List<ShortLink> firstUserList = linkService.listShortLinksByUUID(ownerOfShortLink);
    Assertions.assertEquals(3, firstUserList.size());
    Assertions.assertTrue(firstUserList.contains(shortLink[0]));
    Assertions.assertTrue(firstUserList.contains(shortLink[1]));
    Assertions.assertTrue(firstUserList.contains(shortLink[2]));

    List<ShortLink> secondUserList = linkService.listShortLinksByUUID(secondOwnerOfShortLink);
    Assertions.assertEquals(1, secondUserList.size());
    Assertions.assertTrue(secondUserList.contains(secondShortLink[0]));

    // Проверяем, что сервис отдаёт нужное количество всех ссылок сервиса с правильными ID
    List<ShortLink> totalList = linkService.listAllShortLinks();
    Assertions.assertEquals(4, totalList.size());
    Assertions.assertTrue(totalList.contains(shortLink[0]));
    Assertions.assertTrue(totalList.contains(shortLink[1]));
    Assertions.assertTrue(totalList.contains(shortLink[2]));
    Assertions.assertTrue(totalList.contains(secondShortLink[0]));
  }
}
