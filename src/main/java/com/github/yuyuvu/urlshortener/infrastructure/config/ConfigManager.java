package com.github.yuyuvu.urlshortener.infrastructure.config;

import static com.github.yuyuvu.urlshortener.cli.presenters.ColorPrinter.printlnRed;

import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Класс для загрузки конфигурации приложения из текстового файла и получения значений настраиваемых
 * параметров.
 */
public class ConfigManager {
  /** Основная папка с данными приложения и файлом настройки, не изменяется. */
  private final Path PATH_TO_APPDATA_DIRECTORY = Path.of("url_shortener_appdata");

  /** Файл настройки приложения, не изменяется. */
  private final Path PATH_TO_CONFIG_FILE =
      PATH_TO_APPDATA_DIRECTORY.resolve("url_shortener_config.properties");

  /** Поле с загруженными настройками приложения. */
  private Properties appProperties;

  /** Поле с настройками по-умолчанию. */
  private final Properties defaultProperties = makeDefaultProperties();

  /** Перечисление возможных настроек приложения с мэппингом на ключи в файле конфигурации. */
  enum ConfigProperty {
    DEFAULT_LINK_TTL_HOURS("default.link.ttl.hours"),
    DEFAULT_LINK_USAGE_LIMIT("default.link.usage.limit"),
    USER_LINK_USAGE_LIMIT("user.link.usage.limit"),
    DEFAULT_SHORT_LINK_LENGTH("default.short.link.length"),
    DEFAULT_SHORT_LINK_ALLOWED_CHARACTERS("default.short.link.allowed.characters"),
    DEFAULT_SHORT_LINK_MAX_AMOUNT_PER_USER("default.short.link.amount.per.user"),
    DEFAULT_FILE_STORAGE_PATH("default.file.storage.path"),
    DEFAULT_SERVICE_BASE_URL("default.service.base.url"),
    LEGACY_SERVICE_BASE_URLS("legacy.service.base.urls");

    private final String key;

    ConfigProperty(String key) {
      this.key = key;
    }

    public String key() {
      return key;
    }
  }

  private Properties makeDefaultProperties() {
    Properties defaultProperties = new Properties();
    // Значения по умолчанию, которые записываются в файл конфигурации в первый раз
    // Время жизни короткой ссылки в часах
    defaultProperties.setProperty(ConfigProperty.DEFAULT_LINK_TTL_HOURS.key(), "24");

    // Максимум использований одной короткой ссылки - 10
    defaultProperties.setProperty(ConfigProperty.DEFAULT_LINK_USAGE_LIMIT.key(), "10");

    // Максимум использований одной короткой ссылки, устанавливаемый пользователем - 50
    defaultProperties.setProperty(ConfigProperty.USER_LINK_USAGE_LIMIT.key(), "50");

    // Длина короткой ссылки - 6 символов
    defaultProperties.setProperty(ConfigProperty.DEFAULT_SHORT_LINK_LENGTH.key(), "6");

    // Используем символы base58, они не содержат символов, которые не различимы сразу: l I 0 O
    defaultProperties.setProperty(
        ConfigProperty.DEFAULT_SHORT_LINK_ALLOWED_CHARACTERS.key(),
        "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz");

    // Максимальное количество коротких ссылок на одного пользователя
    defaultProperties.setProperty(
        ConfigProperty.DEFAULT_SHORT_LINK_MAX_AMOUNT_PER_USER.key(), "100");

    // Путь до файла, где хранится состояние сервиса во время выключений или перезагрузок
    defaultProperties.setProperty(
        ConfigProperty.DEFAULT_FILE_STORAGE_PATH.key(),
        String.valueOf(getAppdataDirectoryPathProperty().resolve("data_storage.json")));

    // Текущий URL нашего сервиса сокращения ссылок
    defaultProperties.setProperty(
        ConfigProperty.DEFAULT_SERVICE_BASE_URL.key(), "https://yush.ru/");

    // Устаревшие URL нашего сервиса сокращения ссылок, которые мы ещё распознаём для редиректов
    defaultProperties.setProperty(ConfigProperty.LEGACY_SERVICE_BASE_URLS.key(), "");

    if (defaultProperties.size() != ConfigProperty.values().length) {
      throw new RuntimeException(
          "Критическая ошибка: не задана одна из настроек по-умолчанию, ожидалось "
              + ConfigProperty.values().length
              + ", задано только "
              + defaultProperties.size()
              + ".");
    }
    return defaultProperties;
  }

  public ConfigManager() {
    // Создание директории для файлов данных сервиса
    try {
      Files.createDirectories(PATH_TO_APPDATA_DIRECTORY);
    } catch (IOException e) {
      appProperties = makeDefaultProperties();
      printlnRed(
          "Проблемы с созданием директории для данных приложения: "
              + PATH_TO_APPDATA_DIRECTORY
              + e.getMessage()
              + ".\n"
              + "Будут использованы стандартные значения настроек.");
    }

    // Создание файла конфигурации или чтение существующего
    String loadedProperties = null;
    try {
      if (Files.notExists(PATH_TO_CONFIG_FILE)) {
        Files.createFile(PATH_TO_CONFIG_FILE);
        defaultProperties.store(new FileWriter(PATH_TO_CONFIG_FILE.toFile()), "");
      }
      loadedProperties = Files.readString(PATH_TO_CONFIG_FILE);
    } catch (IOException e) {
      appProperties = makeDefaultProperties();
      printlnRed(
          "Проблемы с созданием или чтением файла конфигурации: "
              + PATH_TO_CONFIG_FILE
              + " "
              + e.getMessage()
              + ".\n"
              + "Будут использованы стандартные значения настроек.");
    }

    // Загрузка настроек из прочитанного файла конфигурации
    appProperties = makeDefaultProperties();
    try {
      if (loadedProperties != null) {
        appProperties.load(new StringReader(loadedProperties));
      } else {
        throw new Exception();
      }
    } catch (Exception e) {
      appProperties = makeDefaultProperties();
      printlnRed(
          "Проблемы с загрузкой настроек сервиса из файла конфигурации: "
              + PATH_TO_CONFIG_FILE
              + " "
              + e.getMessage()
              + ".\n"
              + "Будут использованы стандартные значения настроек.");
    }
  }

  public Path getAppdataDirectoryPathProperty() {
    return PATH_TO_APPDATA_DIRECTORY;
  }

  public Path getFileStoragePathProperty() {
    String configKey = ConfigProperty.DEFAULT_FILE_STORAGE_PATH.key();
    String defaultValue = defaultProperties.getProperty(configKey);
    String configValue = appProperties.getProperty(configKey);
    try {
      return Path.of(configValue).toAbsolutePath();
    } catch (Exception e) {
      printlnRed(
          "В файле конфигурации обнаружен некорректный путь до директории с файловой базой данных сервиса: "
              + configValue
              + ".\n"
              + "Будет установлено стандартное значение: "
              + defaultValue);
      appProperties.setProperty(configKey, defaultValue);
      return Path.of(appProperties.getProperty(configKey));
    }
  }

  public int getDefaultShortLinkTTLInHoursProperty() {
    String configKey = ConfigProperty.DEFAULT_LINK_TTL_HOURS.key();
    String defaultValue = defaultProperties.getProperty(configKey);
    String configValue = appProperties.getProperty(configKey);
    try {
      return Integer.parseInt(configValue);
    } catch (NumberFormatException e) {
      printlnRed(
          "В файле конфигурации обнаружено некорректное время действия ссылки по-умолчанию: "
              + configValue
              + ".\n"
              + "Укажите значение в часах в формате 24, 5, 1 и т.д.\n"
              + "На время текущего запуска сервиса будет установлено стандартное значение: "
              + defaultValue);
      appProperties.setProperty(configKey, defaultValue);
      return Integer.parseInt(appProperties.getProperty(configKey));
    }
  }

  public int getDefaultShortLinkUsageLimitProperty() {
    String configKey = ConfigProperty.DEFAULT_LINK_USAGE_LIMIT.key();
    String defaultValue = defaultProperties.getProperty(configKey);
    String configValue = appProperties.getProperty(configKey);
    try {
      return Integer.parseInt(configValue);
    } catch (NumberFormatException e) {
      printlnRed(
          "В файле конфигурации обнаружено некорректное количество максимальных использований ссылки: "
              + configValue
              + ".\n"
              + "Укажите количество одним числом, например, 5, 10, 244 и т.д.\n"
              + "На время текущего запуска сервиса будет установлено стандартное значение: "
              + defaultValue);
      appProperties.setProperty(configKey, defaultValue);
      return Integer.parseInt(appProperties.getProperty(configKey));
    }
  }

  public int getUserShortLinkUsageLimitProperty() {
    String configKey = ConfigProperty.USER_LINK_USAGE_LIMIT.key();
    String defaultValue = defaultProperties.getProperty(configKey);
    String configValue = appProperties.getProperty(configKey);
    try {
      return Integer.parseInt(configValue);
    } catch (NumberFormatException e) {
      printlnRed(
          "В файле конфигурации обнаружен некорректный лимит использований ссылки,"
              + " на который пользователь может поменять лимит по умолчанию: "
              + configValue
              + ".\n"
              + "Укажите количество одним числом, например, 5, 10, 244 и т.д.\n"
              + "На время текущего запуска сервиса будет установлено стандартное значение: "
              + defaultValue);
      appProperties.setProperty(configKey, defaultValue);
      return Integer.parseInt(appProperties.getProperty(configKey));
    }
  }

  public int getDefaultShortLinkIdLengthProperty() {
    String configKey = ConfigProperty.DEFAULT_SHORT_LINK_LENGTH.key();
    String defaultValue = defaultProperties.getProperty(configKey);
    String configValue = appProperties.getProperty(configKey);
    try {
      return Integer.parseInt(configValue);
    } catch (NumberFormatException e) {
      printlnRed(
          "В файле конфигурации обнаружена некорректная максимальная длина ID короткой ссылки: "
              + configValue
              + ".\n"
              + "Укажите количество одним числом, например, 5, 10, 244 и т.д.\n"
              + "На время текущего запуска сервиса будет установлено стандартное значение: "
              + defaultValue);
      appProperties.setProperty(configKey, defaultValue);
      return Integer.parseInt(appProperties.getProperty(configKey));
    }
  }

  public int getDefaultShortLinkMaxAmountPerUserProperty() {
    String configKey = ConfigProperty.DEFAULT_SHORT_LINK_MAX_AMOUNT_PER_USER.key();
    String defaultValue = defaultProperties.getProperty(configKey);
    String configValue = appProperties.getProperty(configKey);
    try {
      return Integer.parseInt(configValue);
    } catch (NumberFormatException e) {
      printlnRed(
          "В файле конфигурации обнаружен некорректный лимит количества коротких ссылок на одного владельца: "
              + configValue
              + ".\n"
              + "Укажите количество одним числом, например, 5, 10, 244 и т.д.\n"
              + "На время текущего запуска сервиса будет установлено стандартное значение: "
              + defaultValue);
      appProperties.setProperty(configKey, defaultValue);
      return Integer.parseInt(appProperties.getProperty(configKey));
    }
  }

  public String getDefaultServiceBaseURLProperty() {
    String configKey = ConfigProperty.DEFAULT_SERVICE_BASE_URL.key();
    String defaultValue = defaultProperties.getProperty(configKey);
    String configValue = appProperties.getProperty(configKey);
    try {
      return configValue.strip();
    } catch (Exception e) {
      printlnRed(
          "В файле конфигурации задан некорректный базовый URL сервиса сокращения ссылок: "
              + configValue
              + ".\n"
              + "Укажите URL со схемой http:// или https://, в формате https://ya.ru/ (со слэшем в конце).\n"
              + "На время текущего запуска сервиса будет установлено стандартное значение: "
              + defaultValue);
      appProperties.setProperty(configKey, defaultValue);
      return appProperties.getProperty(configKey);
    }
  }

  public String[] getLegacyServiceBaseURLProperty() {
    String configKey = ConfigProperty.LEGACY_SERVICE_BASE_URLS.key();
    String defaultValue = defaultProperties.getProperty(configKey);
    String configValue = appProperties.getProperty(configKey);
    try {
      return configValue.strip().split(",");
    } catch (Exception e) {
      printlnRed(
          "В файле конфигурации задан некорректный список старых доменов сервиса: "
              + configValue
              + ".\n"
              + "Укажите список URL со схемой http:// или https://, в формате https://ya.ru/ (со слэшем в конце). Указывайте эти URL через запятую.\n"
              + "На время текущего запуска сервиса будет работать перенаправление только по новому домену: "
              + getDefaultServiceBaseURLProperty());
      appProperties.setProperty(configKey, defaultValue);
      return appProperties.getProperty(configKey).split(",");
    }
  }

  public char[] getShortLinkAllowedCharactersProperty() {
    String configKey = ConfigProperty.DEFAULT_SHORT_LINK_ALLOWED_CHARACTERS.key();
    String defaultValue = defaultProperties.getProperty(configKey);
    String configValue = appProperties.getProperty(configKey);
    try {
      if (configValue.isBlank()) {
        throw new Exception();
      }
      return configValue.strip().replaceAll("\\s", "").toCharArray();
    } catch (Exception e) {
      printlnRed(
          "В файле конфигурации не задан список разрешённых символов для короткой ссылки: "
              + configValue
              + ".\n"
              + "Укажите список разрешённых символов одной строкой без пробелов.\n"
              + "На время текущего запуска сервиса ID коротких ссылок будут кодироваться через base58 символы: "
              + getDefaultServiceBaseURLProperty());
      appProperties.setProperty(configKey, defaultValue);
      return appProperties.getProperty(configKey).toCharArray();
    }
  }
}
