package com.github.yuyuvu.urlshortener.application;

import com.github.yuyuvu.urlshortener.domain.model.ShortLink;
import com.github.yuyuvu.urlshortener.domain.repository.ShortLinkRepository;
import com.github.yuyuvu.urlshortener.exceptions.IllegalCommandParameterException;
import com.github.yuyuvu.urlshortener.exceptions.InvalidOriginalLinkException;
import com.github.yuyuvu.urlshortener.exceptions.InvalidShortLinkException;
import com.github.yuyuvu.urlshortener.exceptions.NotEnoughPermissionsException;
import com.github.yuyuvu.urlshortener.exceptions.OriginalLinkNotFoundException;
import com.github.yuyuvu.urlshortener.exceptions.UsagesLimitReachedException;
import com.github.yuyuvu.urlshortener.infrastructure.config.ConfigManager;
import java.awt.Desktop;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

/**
 * LinkService отвечает за операции, совершаемые со ссылками и URL (прежде всего с объектом
 * ShortLink). Получение, валидация, удаление, выполнение редактирующих операций. Инкапсулирует
 * доступ к ShortLinkRepository.
 */
public class LinkService {
  private final ShortLinkRepository shortLinkRepository;
  private final ConfigManager configManager;

  /** Сервис зависит от NotificationRepository и ConfigManager. */
  public LinkService(ShortLinkRepository shortLinkRepository, ConfigManager configManager) {
    this.shortLinkRepository = shortLinkRepository;
    this.configManager = configManager;
  }

  /**
   * Утилитарный класс для более наглядного разделения ссылки на её ID и service base URL (без
   * магических индексов в массивах).
   */
  private static class SplitShortURL {
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private final String serviceURL;

    private final String shortID;

    SplitShortURL(String serviceURL, String shortID) {
      this.serviceURL = serviceURL;
      this.shortID = shortID;
    }
  }

  /*
   * Методы для валидации передаваемых параметров.
   * */

  /** Метод для проверки, что пользователь с некоторым UUID является владельцем ссылки. */
  public boolean isUUIDOwnerOfShortLink(ShortLink shortLink, UUID userUUID) {
    return shortLink.getOwnerOfShortURL().equals(userUUID);
  }

  /**
   * Метод validateURLFormat проверяет, что переданный длинный URL содержит корректные схемы URL
   * (http, https, ftp), а также, что URL соответствует всем синтаксическим правилам стандарта
   * RFC2396.
   */
  @SuppressWarnings("ResultOfMethodCallIgnored")
  public void validateURLFormat(String originalURL) throws InvalidOriginalLinkException {
    // Наиболее корректная валидация на URL встроенными методами Java
    // Можно было также заменить на проверку через Regex по более новому RFC 3987, например,
    // используя regex из класса android.util.Patterns из Android API, но это кажется лишним
    // усложнением
    // в данном случае
    try {
      // Следующие два вызова выбросят Exception, если переданное значение не является URI и URL
      URI uri = new URI(originalURL);
      uri.toURL();
    } catch (URISyntaxException | IllegalArgumentException | MalformedURLException e) {
      throw new InvalidOriginalLinkException(
          "Переданный URL ("
              + originalURL
              + ") представлен в некорректном формате или не является ссылкой."
              + "\nПередайте корректный URL с указанием протокола.");
    }
  }

  /**
   * Метод для проверки наличия пары короткий URL - длинный URL в базе данных приложения. Если такой
   * пары нет, значит, и самой короткой ссылки тоже не существует.
   */
  public ShortLink validateShortLinkExistence(String shortLinkFullURL)
      throws OriginalLinkNotFoundException, InvalidShortLinkException {
    shortLinkFullURL = shortLinkFullURL.strip();
    // Проверка на то, что короткая ссылка начинается с serviceBaseURL
    if (checkShortLinkDoesNotStartWithServiceBaseURL(shortLinkFullURL)) {
      throw new InvalidShortLinkException(
          "Переданная ссылка не была создана в данном сервисе сокращения ссылок "
              + "(переданное значение не распознаётся как короткая ссылка сервиса).");
    }

    // Проверка на то, что короткая ссылка существует, активна и ведёт на какой-то длинный URL
    SplitShortURL splitShortURL = splitShortLinkAndServiceBaseURL(shortLinkFullURL);
    Optional<ShortLink> originalUrl =
        shortLinkRepository.getShortLinkByShortID(splitShortURL.shortID);
    if (originalUrl.isEmpty()) {
      throw new OriginalLinkNotFoundException(
          "Короткая ссылка "
              + shortLinkFullURL
              + " не ведёт ни на какую длинную ссылку. Данной короткой ссылки не существует,"
              + " или её срок жизни истёк.");
    } else {
      return originalUrl.get();
    }
  }

  /**
   * Метод проверяет, что короткий URL НЕ начинается с одного из распознаваемых нами URL нашего
   * сервиса.
   */
  public boolean checkShortLinkDoesNotStartWithServiceBaseURL(String shortLinkFullURL) {
    String activeServiceURL = configManager.getDefaultServiceBaseURLProperty();
    String[] legacyServiceURLs = configManager.getLegacyServiceBaseURLProperty();

    // Начало с текущего serviceBaseURL
    if (shortLinkFullURL.startsWith(activeServiceURL)) {
      return false;
    }

    // Начало с одного из старых serviceBaseURL нашего сервиса (полезно при переезде на новый домен)
    if (Arrays.stream(legacyServiceURLs).noneMatch(String::isBlank)) {
      for (String legacyServiceURL : legacyServiceURLs) {
        if (shortLinkFullURL.startsWith(legacyServiceURL)) {
          return false;
        }
      }
    }
    return true;
  }

  /*
   * Вспомогательные методы.
   * */

  /**
   * Метод делит полный адрес короткого URL на URL сервиса сокращения ссылок и на ID самого
   * короткого URL. Перед использованием метода нужно провести проверку из метода
   * checkShortLinkDoesNotStartWithServiceBaseURL.
   */
  private SplitShortURL splitShortLinkAndServiceBaseURL(String shortLinkFullURL) {
    String activeServiceURL = configManager.getDefaultServiceBaseURLProperty();
    String[] legacyServiceURLs = configManager.getLegacyServiceBaseURLProperty();

    // Случай legacy service base URL
    if (Arrays.stream(legacyServiceURLs).noneMatch(String::isBlank)) {
      for (String legacyServiceURL : legacyServiceURLs) {
        if (shortLinkFullURL.startsWith(legacyServiceURL)) {
          String shortLinkId = shortLinkFullURL.substring(legacyServiceURL.length());
          return new SplitShortURL(legacyServiceURL, shortLinkId);
        }
      }
    }

    // Случай active service base URL
    String shortLinkId = shortLinkFullURL.substring(activeServiceURL.length());
    return new SplitShortURL(activeServiceURL, shortLinkId);
  }

  /*
   * Методы, выполняющие определённую сервисную логику.
   * */

  /**
   * Метод makeNewShortLink создаёт новый объект ShortLink. Все значения берутся из конфигурации:
   * единица измерения TTL, стандартное значение TTL, стандартное значение лимита использований.
   * Внутренний короткий ID ссылки генерируется в generateShortLinkID.
   */
  public ShortLink makeNewShortLink(String originalURL, UUID ownerOfShortLink)
      throws InvalidOriginalLinkException {
    try {
      // Проверяем, что оригинальная ссылка действительно является URL
      validateURLFormat(originalURL);

      // Получаем дату создания ссылки
      LocalDateTime creationDateTime = LocalDateTime.now();

      // Добавляем к дате создания ссылки стандартное значение TTL и получаем срок действия новой
      // ссылки
      LocalDateTime expirationDateTime =
          calculateExpirationDateTimeOfNewShortLink(creationDateTime);

      // Создаём новый объект короткой ссылки
      return new ShortLink(
          originalURL,
          generateShortLinkID(),
          creationDateTime,
          expirationDateTime,
          0,
          configManager.getDefaultShortLinkUsageLimitProperty(),
          ownerOfShortLink,
          false);
    } catch (InvalidOriginalLinkException e) {
      throw new InvalidOriginalLinkException(e.getMessage());
    }
  }

  /**
   * Метод calculateExpirationDateTimeOfNewShortLink вычисляет дату и время истечения срока действия
   * создаваемой новой ссылки.
   */
  private LocalDateTime calculateExpirationDateTimeOfNewShortLink(LocalDateTime creationDateTime) {
    // Получаем единицу измерения TTL и стандартный TTL
    ConfigManager.TimeUnit defaultTTLTimeUnit =
        configManager.getDefaultShortLinkTTLTimeUnitProperty();
    int ttlLimitInTimeUnits = configManager.getDefaultShortLinkTTLInUnitsProperty();

    LocalDateTime expirationDateTime;

    // Добавляем к дате создания ссылки стандартное значение TTL
    switch (defaultTTLTimeUnit) {
      case DAYS -> expirationDateTime = creationDateTime.plusDays(ttlLimitInTimeUnits);
      case MINUTES -> expirationDateTime = creationDateTime.plusMinutes(ttlLimitInTimeUnits);
      case SECONDS -> expirationDateTime = creationDateTime.plusSeconds(ttlLimitInTimeUnits);
      default -> expirationDateTime = creationDateTime.plusHours(ttlLimitInTimeUnits);
    }
    return expirationDateTime;
  }

  /** Метод saveNewShortLink помещает новый объект ShortLink в shortLinkRepository. */
  @SuppressWarnings("UnusedReturnValue")
  public ShortLink saveNewShortLink(ShortLink shortLink) {
    return shortLinkRepository.saveShortLink(shortLink);
  }

  /**
   * Метод generateShortLinkID отвечает за логику генерации уникальных кодов для короткого URL,
   * ShortLinkRepository построен таким образом, что по одному коду в нём может храниться только
   * одна любая ссылка, соответственно, обеспечивается глобальная уникальность коротких ссылок.
   * Никакие короткие коды URL не могут быть сгенерированы повторно, пока они ещё есть в качестве
   * ключа в ShortLinkRepository. Таким образом, какая-либо короткая ссылка вне зависимости от
   * владельца не может вести ни на один и тот же сайт, ни на разные сайты. Это нужно, чтобы другие
   * пользователи могли создавать и использовать (!) ссылки друг друга без коллизий. Так как мы
   * используем реализацию ShortLinkRepository на основе HashMap (InMemoryShortLinkRepository),
   * поиск по shortID будет очень быстрым.
   */
  public String generateShortLinkID() {
    Random random = new Random();

    // Получаем массив разрешённых символов для генерации ID из конфига
    final char[] allowedCharacters = configManager.getShortLinkAllowedCharactersProperty();

    // Получаем разрешённую длину короткого кода ссылки для генерации из конфига
    final int shortIdLength = configManager.getDefaultShortLinkIdLengthProperty();
    char[] shortIdGeneratedChars = new char[shortIdLength];

    // В цикле проверяем, что сгенерированный код не является имеющимся ключом и ещё не отвечает
    // за какую-либо ссылку в ShortLinkRepository
    while (true) {
      // Генерируем shortID
      for (int i = 0; i < shortIdLength; i++) {
        int randomCharIndex = random.nextInt(allowedCharacters.length);
        shortIdGeneratedChars[i] = allowedCharacters[randomCharIndex];
      }

      // Проверяем наличие такого shortID в ShortLinkRepository, если есть - генерируем снова
      String shortIdString = new String(shortIdGeneratedChars);
      if (shortLinkRepository.getShortLinkByShortID(shortIdString).isPresent()) {
        continue;
      }
      return shortIdString;
    }
  }

  /**
   * Метод отвечает за редирект по короткому URL. Проводит валидацию введённого URL на
   * принадлежность к нашему сервису.
   */
  public String redirectByShortLink(String shortLinkFullURL)
      throws OriginalLinkNotFoundException,
          IOException,
          InvalidShortLinkException,
          InvalidOriginalLinkException,
          UsagesLimitReachedException {
    shortLinkFullURL = shortLinkFullURL.strip();
    // Проверка, что переданное значение - это возможная короткая ссылка нашего сервиса
    if (checkShortLinkDoesNotStartWithServiceBaseURL(shortLinkFullURL)) {
      throw new InvalidShortLinkException(
          "Переданный URL не был распознан в качестве короткой ссылки данного сервиса"
              + " сокращения ссылок.");
    }

    // Проверка на то, что короткая ссылка существует, активна и ведёт на какой-то длинный URL
    SplitShortURL splitShortURL = splitShortLinkAndServiceBaseURL(shortLinkFullURL);
    Optional<ShortLink> shortLinkData =
        shortLinkRepository.getShortLinkByShortID(splitShortURL.shortID);
    if (shortLinkData.isEmpty()) {
      throw new OriginalLinkNotFoundException(
          "Короткая ссылка "
              + shortLinkFullURL
              + " не ведёт ни на какую длинную ссылку. Данной короткой ссылки не существует, "
              + "или её срок жизни истёк.");
    } else {
      try {
        // Проверка, что срок действия ссылки истёк, но она пока ещё не была удалена
        if (shortLinkData.get().isExpired()) {
          throw new InvalidShortLinkException(
              "Срок действия данной короткой ссылки истёк. По ней больше нельзя перейти. "
                  + "Она будет удалена в течение 15 секунд.");
        }

        // На всякий случай дополнительно перепроверяем логику создания коротких ссылок для
        // выявления транзитивных ошибок
        String originalURLAddress = shortLinkData.get().getOriginalURLAddress();
        validateURLFormat(originalURLAddress);

        // Увеличиваем счётчик использования коротких ссылок и переходим по оригинальному URL
        // (метод счётчика выбрасывает исключение, если лимит использований уже израсходован,
        // поэтому он должен стоять перед фактическим редиректом)
        shortLinkData.get().incrementUsageCounter();
        Desktop.getDesktop().browse(new URI(originalURLAddress));

        return originalURLAddress;
      } catch (URISyntaxException e) {
        throw new InvalidOriginalLinkException(
            "Адрес оригинальной ссылки имеет некорректный формат. Невозможно перейти.");
      } catch (IOException e) {
        throw new IOException(
            "На вашем устройстве не обнаружено средство для открытия ссылок, "
                + "например браузер. Установите его.");
      }
    }
  }

  /**
   * Метод для управления созданной короткой ссылкой: позволяет вручную изменить лимит
   * использований.
   */
  @SuppressWarnings("UnusedReturnValue")
  public ShortLink changeShortLinkUsageLimit(
      String shortLinkFullURL, UUID userUUID, int newUsageLimit)
      throws OriginalLinkNotFoundException,
          InvalidShortLinkException,
          NotEnoughPermissionsException,
          IllegalCommandParameterException {
    // Проверяем, что пользователь собирается управлять активной короткой ссылкой
    ShortLink shortLinkToManage = validateShortLinkExistence(shortLinkFullURL);

    // Получаем из конфига максимальный лимит, который может выставить пользователь
    int userMaxManualSetLinkUsagesLimitAmount =
        configManager.getUserSetShortLinkUsageLimitProperty();

    // Проверяем, что пользователь собирается управлять своей короткой ссылкой, а не чужой
    if (isUUIDOwnerOfShortLink(shortLinkToManage, userUUID)) {

      // Запрещаем отрицательный или нулевой лимит
      if (newUsageLimit <= 0) {
        throw new IllegalCommandParameterException(
            "Нельзя задать нулевой " + "или отрицательный лимит использований ссылки.");
      }

      // Запрещаем слишком большой лимит
      if (newUsageLimit <= userMaxManualSetLinkUsagesLimitAmount) {

        // Запрещаем менять лимит по ссылке с уже израсходованным лимитом для обеспечения надёжной
        // блокировки ссылки без обходов
        if (shortLinkToManage.isLimitReached()) {
          throw new IllegalCommandParameterException(
              "Нельзя менять лимит использований ссылки, если лимит уже был израсходован. "
                  + "Ссылка будет удалена в скором времени либо можете удалить её вручную.");
        }

        // Запрещаем странное поведение пользователя, приводящее к моментальной автоблокировке
        // ссылки
        if (shortLinkToManage.getUsageCounter() >= newUsageLimit) {
          throw new IllegalCommandParameterException(
              "Нельзя установить новый лимит использований ссылки в значение, меньшее "
                  + "или равное текущему количеству фактических использований ссылки. "
                  + "Используйте команду удаления ссылок.");
        }

        shortLinkToManage.setUsageLimitAmount(newUsageLimit);
        return shortLinkToManage;
      } else {
        throw new IllegalCommandParameterException(
            "Максимальное количество использований ссылки, "
                + "которое может задать пользователь вручную, равно: "
                + userMaxManualSetLinkUsagesLimitAmount
                + "\nВыберите меньшее число максимальных использований.");
      }
    } else {
      throw new NotEnoughPermissionsException();
    }
  }

  /**
   * Метод для управления созданной короткой ссылкой: позволяет вручную изменить оригинальный URL
   * внутри короткого URL.
   */
  @SuppressWarnings("UnusedReturnValue")
  public ShortLink changeShortLinkOriginalURL(
      String shortLinkFullURL, UUID userUUID, String originalURL)
      throws OriginalLinkNotFoundException,
          InvalidShortLinkException,
          NotEnoughPermissionsException,
          IllegalCommandParameterException,
          InvalidOriginalLinkException {
    // Проверяем, что пользователь собирается управлять активной короткой ссылкой
    ShortLink shortLinkToManage = validateShortLinkExistence(shortLinkFullURL);

    // Проверяем, что пользователь собирается управлять своей короткой ссылкой, а не чужой
    if (isUUIDOwnerOfShortLink(shortLinkToManage, userUUID)) {

      // Проверяем, что имеющийся URL собираются заменять на валидный URL
      if (originalURL.isBlank()) {
        throw new IllegalCommandParameterException("Нельзя заменить имеющийся URL на пустой.");
      }
      validateURLFormat(originalURL);

      shortLinkToManage.setOriginalURLAddress(originalURL);
      return shortLinkToManage;
    } else {
      throw new NotEnoughPermissionsException();
    }
  }

  /**
   * Метод для управления созданной короткой ссылкой: позволяет вручную изменить TTL ссылки после
   * создания.
   */
  public LocalDateTime changeShortLinkTTLExpirationLimit(
      String shortLinkFullURL, UUID userUUID, int addTTLInUnitsToCreationTime)
      throws OriginalLinkNotFoundException,
          InvalidShortLinkException,
          NotEnoughPermissionsException,
          IllegalCommandParameterException {
    // Проверяем, что пользователь собирается управлять активной короткой ссылкой
    ShortLink shortLinkToManage = validateShortLinkExistence(shortLinkFullURL);

    // Получаем из конфига максимальный TTL, который может выставить пользователь
    int userSetShortLinkMaxTTLInUnits = configManager.getUserSetShortLinkMaxTTLInUnitsProperty();

    // Проверяем, что пользователь собирается управлять своей короткой ссылкой, а не чужой
    if (isUUIDOwnerOfShortLink(shortLinkToManage, userUUID)) {

      // Запрещаем отрицательный или нулевой TTL
      if (userSetShortLinkMaxTTLInUnits <= 0) {
        throw new IllegalCommandParameterException(
            "Нельзя использовать отрицательное или нулевое значение для единиц измерения времени"
                + " при установке нового TTL.");
      }

      // Запрещаем задавать слишком большой TTL
      if (addTTLInUnitsToCreationTime <= userSetShortLinkMaxTTLInUnits) {

        // Получаем новый срок действия ссылки путём прибавления переданного TTL к дате и времени её
        // создания
        LocalDateTime newExpirationDateTime =
            calculateNewExpirationDateTimeForShortLinkToChange(
                addTTLInUnitsToCreationTime, shortLinkToManage);

        // Запрещаем странное поведение пользователя, приводящее к моментальному автоудалению ссылки
        if (newExpirationDateTime.isBefore(LocalDateTime.now())) {
          throw new IllegalCommandParameterException(
              """
                  Нельзя задать новый TTL для ссылки, при котором новый срок истечения действия ссылки уже будет истёкшим.
                  Срок действия ссылки должен оканчиваться в будущем относительно текущего момента.
                  Ориентируйтесь на дату создания, указанную в команде list.
                  Для удаления ссылки используйте команду delete url_вашей_короткой_ссылки.""");
        }

        shortLinkToManage.setExpirationDateTime(newExpirationDateTime);
        return newExpirationDateTime;
      } else {
        throw new IllegalCommandParameterException(
            String.format(
                    "Максимальное отдаление от даты создания (в единицах измерения времени: %s), ",
                    configManager.getDefaultShortLinkTTLTimeUnitProperty().key())
                + "которое может задавать пользователь равно: "
                + userSetShortLinkMaxTTLInUnits
                + "."
                + "\nВыберите меньшее отдаление от даты создания в единицах измерения времени.");
      }
    } else {
      throw new NotEnoughPermissionsException();
    }
  }

  /**
   * Метод calculateNewExpirationDateTimeForShortLinkToChange вычисляет дату и время истечения срока
   * действия уже существующей короткой ссылки, для которой запрошено изменение TTL.
   */
  private LocalDateTime calculateNewExpirationDateTimeForShortLinkToChange(
      int addTTLInUnitsToCreationTime, ShortLink shortLinkToManage) {
    // Выясняем единицу измерения TTL
    ConfigManager.TimeUnit defaultTTLTimeUnit =
        configManager.getDefaultShortLinkTTLTimeUnitProperty();
    LocalDateTime newExpirationDateTime;

    // Получаем новый срок действия ссылки
    switch (defaultTTLTimeUnit) {
      case DAYS ->
          newExpirationDateTime =
              shortLinkToManage.getCreationDateTime().plusDays(addTTLInUnitsToCreationTime);
      case MINUTES ->
          newExpirationDateTime =
              shortLinkToManage.getCreationDateTime().plusMinutes(addTTLInUnitsToCreationTime);
      case SECONDS ->
          newExpirationDateTime =
              shortLinkToManage.getCreationDateTime().plusSeconds(addTTLInUnitsToCreationTime);
      default ->
          newExpirationDateTime =
              shortLinkToManage.getCreationDateTime().plusHours(addTTLInUnitsToCreationTime);
    }
    return newExpirationDateTime;
  }

  /** Метод для удаления созданной короткой ссылки. */
  public boolean deleteShortLink(String shortLinkFullURL, UUID userUUID)
      throws OriginalLinkNotFoundException,
          InvalidShortLinkException,
          NotEnoughPermissionsException {
    // Проверяем, что пользователь собирается управлять активной короткой ссылкой
    ShortLink shortLinkToManage = validateShortLinkExistence(shortLinkFullURL);

    // Проверяем, что пользователь собирается управлять своей короткой ссылкой, а не чужой
    if (isUUIDOwnerOfShortLink(shortLinkToManage, userUUID)) {
      return shortLinkRepository.deleteShortLink(shortLinkToManage.getShortId());
    } else {
      throw new NotEnoughPermissionsException();
    }
  }

  /**
   * Метод для получения списка созданных коротких ссылок пользователя. Например, для команд list и
   * stats.
   */
  public List<ShortLink> listShortLinksByUUID(UUID userUUID) {
    return shortLinkRepository.getShortLinksByOwnerUUID(userUUID);
  }

  /*
   * Методы, используемые только во внутренней логике работы сервиса
   * */

  /**
   * Метод для удаления созданной короткой ссылкой по ID во внутренней логике приложения.
   * Используется для удаления по истечении TTL.
   */
  @SuppressWarnings("UnusedReturnValue")
  public boolean uncheckedDeleteShortLinkByShortId(String shortLinkId) {
    return shortLinkRepository.deleteShortLink(shortLinkId);
  }

  /**
   * Метод для получения списка всех коротких ссылок из репозитория. Используется для автопроверок
   * на израсходование лимита использования или на окончание срока действия ссылки.
   */
  public List<ShortLink> listAllShortLinks() {
    return shortLinkRepository.getAllShortLinks();
  }
}
