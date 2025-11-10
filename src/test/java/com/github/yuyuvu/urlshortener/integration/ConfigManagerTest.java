package com.github.yuyuvu.urlshortener.integration;

import com.github.yuyuvu.urlshortener.infrastructure.config.ConfigManager;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Properties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

/** Класс для тестов процесса загрузки настроек из файла. */
@ExtendWith(MockitoExtension.class)
public class ConfigManagerTest {

  /**
   * Проверяем механизм сохранения настроек в файл, чтения их из файла и выставления настроек
   * по-умолчанию, если в файле настроек есть проблемы.
   */
  @Test
  void checkDefaultValuesRecovery() throws IOException {
    // Указываем другие пути до файла настроек
    Path testAppdata = Path.of("test_appdata1");
    Path pathToConfigFile = testAppdata.resolve("url_shortener_test_config.properties");

    // Заменяем System.out
    PrintStream out = System.out;
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    System.setOut(new PrintStream(bos));

    // При первом запуске файл настроек должен записаться без проблем, в ByteArrayOutputStream
    // не должно быть сообщений об ошибках и попытках их исправить
    ConfigManager configManager = new ConfigManager(testAppdata, pathToConfigFile);
    Assertions.assertNotNull(configManager);

    // Проверяем содержимое ByteArrayOutputStream
    Assertions.assertEquals("", bos.toString());

    // Теперь перезаписываем файл настроек на файл с такими настройками, где все ошибочные
    Properties incorrectProperties = makeIncorrectProperties();

    Files.writeString(
        pathToConfigFile,
        "#"
            + "\n"
            + (incorrectProperties
                .toString()
                .replace("{", "")
                .replace("}", "")
                .replace("\\", "\\\\")
                .replaceAll(", ", "\n")),
        StandardCharsets.UTF_8,
        StandardOpenOption.CREATE,
        StandardOpenOption.TRUNCATE_EXISTING,
        StandardOpenOption.WRITE);

    // Снова загружаем данные из файла. Теперь в ByteArrayOutputStream
    // должно быть много сообщений об ошибках и попытках их исправить
    configManager = new ConfigManager(testAppdata, pathToConfigFile);

    // Проверяем содержимое ByteArrayOutputStream
    Assertions.assertNotEquals("", bos.toString());
    Assertions.assertTrue(
        bos.toString()
            .contains(
                "В файле конфигурации обнаружено некорректное время "
                    + "действия ссылки по-умолчанию"));

    // Дальше очищаем содержимое ByteArrayOutputStream
    bos.reset();
    // Применяем метод checkConfigValidity() и проверяем, что во время загрузки в последнем вызове
    // конструктора все ошибочные настройки были выставлены по умолчанию
    // и сообщений об ошибках снова больше нет
    configManager.checkConfigValidity();
    Assertions.assertEquals("", bos.toString());

    // В конце удаляем файл после теста
    Files.deleteIfExists(pathToConfigFile);
    Files.deleteIfExists(testAppdata);
  }

  private static Properties makeIncorrectProperties() {
    Properties incorrectProperties = new Properties();

    // Время жизни короткой ссылки в единицах измерения времени (стандартно в часах, также возможны
    // дни, минуты, секунды)
    incorrectProperties.setProperty(
        ConfigManager.ConfigProperty.DEFAULT_LINK_TTL_UNITS.key(), "-20");

    // Максимальное время жизни ссылки, на которое пользователь может изменить стандартное значение
    incorrectProperties.setProperty(
        ConfigManager.ConfigProperty.USER_SET_LINK_MAX_TTL_UNITS.key(), "-20");

    // Единица времени для установки TTL
    incorrectProperties.setProperty(
        ConfigManager.ConfigProperty.DEFAULT_LINK_TTL_TIME_UNIT.key(), "ча");

    // Максимум использований одной короткой ссылки - 10
    incorrectProperties.setProperty(
        ConfigManager.ConfigProperty.DEFAULT_LINK_USAGE_LIMIT.key(), "-20");

    // Максимум использований одной короткой ссылки, устанавливаемый пользователем - 50
    incorrectProperties.setProperty(
        ConfigManager.ConfigProperty.USER_SET_LINK_USAGE_LIMIT.key(), "-20");

    // Длина короткой ссылки - 6 символов
    incorrectProperties.setProperty(
        ConfigManager.ConfigProperty.DEFAULT_SHORT_LINK_LENGTH.key(), "-20");

    // Используем символы base58, они не содержат символов, которые не различимы сразу: l I 0 O
    incorrectProperties.setProperty(
        ConfigManager.ConfigProperty.DEFAULT_SHORT_LINK_ALLOWED_CHARACTERS.key(), "");

    // Максимальное количество действующих коротких ссылок на одного пользователя
    incorrectProperties.setProperty(
        ConfigManager.ConfigProperty.DEFAULT_SHORT_LINK_MAX_AMOUNT_PER_USER.key(), "-20");

    // Путь до файла, где хранится состояние сервиса во время выключений или перезагрузок
    incorrectProperties.setProperty(
        ConfigManager.ConfigProperty.DEFAULT_FILE_STORAGE_PATH.key(), " вв \"'фв");

    // Текущий URL нашего сервиса сокращения ссылок
    incorrectProperties.setProperty(
        ConfigManager.ConfigProperty.DEFAULT_SERVICE_BASE_URL.key(), "");

    // Устаревшие URL нашего сервиса сокращения ссылок, которые мы ещё распознаём для редиректов
    incorrectProperties.setProperty(
        ConfigManager.ConfigProperty.LEGACY_SERVICE_BASE_URLS.key(), ",");
    return incorrectProperties;
  }
}
