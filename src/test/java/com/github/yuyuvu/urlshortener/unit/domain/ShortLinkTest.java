package com.github.yuyuvu.urlshortener.unit.domain;

import static org.mockito.Mockito.when;

import com.github.yuyuvu.urlshortener.application.LinkService;
import com.github.yuyuvu.urlshortener.domain.model.ShortLink;
import com.github.yuyuvu.urlshortener.exceptions.InvalidOriginalLinkException;
import com.github.yuyuvu.urlshortener.exceptions.InvalidShortLinkException;
import com.github.yuyuvu.urlshortener.exceptions.OriginalLinkNotFoundException;
import com.github.yuyuvu.urlshortener.exceptions.UsagesLimitReachedException;
import com.github.yuyuvu.urlshortener.infrastructure.config.ConfigManager;
import com.github.yuyuvu.urlshortener.infrastructure.persistence.InMemoryShortLinkRepository;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Класс для тестов методов из ShortLink. */
@ExtendWith(MockitoExtension.class)
public class ShortLinkTest {

  @Mock private ConfigManager configManager;

  /**
   * Проверяем метод для выяснения, что срок действия ссылки истёк. Здесь ссылка создаётся с
   * заведомо истекшим сроком. Должна быть истекшей.
   */
  @Test
  void isExpiredTest() {
    ShortLink shortLink =
        new ShortLink(
            "test",
            "12345",
            LocalDateTime.of(2025, 1, 1, 1, 1),
            LocalDateTime.of(2025, 2, 1, 1, 1),
            0,
            10,
            UUID.randomUUID(),
            false);

    Assertions.assertTrue(shortLink.isExpired());
  }

  /**
   * Проверяем корректную работу увеличения счётчика использований ссылки при редиректе. Здесь
   * счётчик должен быть равен 10 после 10 редиректов, а дальше выбрасывать Exception.
   */
  @Test
  void isLimitReachedTest()
      throws UsagesLimitReachedException,
          InvalidOriginalLinkException,
          OriginalLinkNotFoundException,
          IOException,
          InvalidShortLinkException {
    // Действующая до 2100 года ссылка с лимитом в 10 использований
    ShortLink shortLink =
        new ShortLink(
            "https://google.com",
            "12345",
            LocalDateTime.of(2025, 1, 1, 1, 1),
            LocalDateTime.of(2100, 2, 1, 1, 1),
            0,
            10,
            UUID.randomUUID(),
            false);

    LinkService linkService =
        new LinkService(new InMemoryShortLinkRepository(new HashMap<>()), configManager);
    linkService.saveNewShortLink(shortLink);

    when(configManager.getDefaultServiceBaseURLProperty()).thenReturn("https://yulink.tech/");
    when(configManager.getLegacyServiceBaseURLProperty()).thenReturn(new String[] {""});

    for (int i = 1; i <= 10; i++) {
      linkService.redirectByShortLink("https://yulink.tech/12345", false);
      // На 9 итерации проверяем, что лимит ещё не израсходован
      if (i == 9) {
        Assertions.assertFalse(shortLink.isLimitReached());
      }
    }

    // Проверяем, что счётчик равен 10, и лимит израсходовался точно после 10 использований
    Assertions.assertEquals(10, shortLink.getUsageCounter());
    Assertions.assertTrue(shortLink.isLimitReached());

    // Ошибка выбрасывается корректно
    Assertions.assertThrows(
        UsagesLimitReachedException.class,
        () -> linkService.redirectByShortLink("https://yulink.tech/12345", false));

    // Меняем лимит на неизрасходованный и снова проверяем
    shortLink.setUsageLimitAmount(15);
    Assertions.assertFalse(shortLink.isLimitReached());
  }

  /**
   * Повторно проверяем корректную работу увеличения счётчика, но теперь напрямую при помощи
   * incrementUsageCounter из ShortLink. Здесь счётчик должен быть равен 10 после 10 редиректов, а
   * дальше выбрасывать Exception.
   */
  @Test
  void incrementUsageCounterTest() throws UsagesLimitReachedException {
    // Действующая до 2100 года ссылка с лимитом в 10 использований
    ShortLink shortLink =
        new ShortLink(
            "https://google.com",
            "12345",
            LocalDateTime.of(2025, 1, 1, 1, 1),
            LocalDateTime.of(2100, 2, 1, 1, 1),
            0,
            10,
            UUID.randomUUID(),
            false);

    LinkService linkService =
        new LinkService(new InMemoryShortLinkRepository(new HashMap<>()), configManager);
    linkService.saveNewShortLink(shortLink);

    when(configManager.getDefaultServiceBaseURLProperty()).thenReturn("https://yulink.tech/");
    when(configManager.getLegacyServiceBaseURLProperty()).thenReturn(new String[] {""});

    for (int i = 1; i <= 10; i++) {
      // Проверка инкремента напрямую
      shortLink.incrementUsageCounter();

      // На 9 итерации проверяем, что лимит ещё не израсходован
      if (i == 9) {
        Assertions.assertFalse(shortLink.isLimitReached());
      }
    }

    // Проверяем, что счётчик равен 10, и лимит израсходовался точно после 10 использований
    Assertions.assertEquals(10, shortLink.getUsageCounter());
    Assertions.assertTrue(shortLink.isLimitReached());

    // Ошибка всё ещё выбрасывается корректно
    Assertions.assertThrows(
        UsagesLimitReachedException.class,
        () -> linkService.redirectByShortLink("https://yulink.tech/12345", false));

    // Меняем лимит на неизрасходованный и снова проверяем
    shortLink.setUsageLimitAmount(15);
    Assertions.assertFalse(shortLink.isLimitReached());
  }
}
