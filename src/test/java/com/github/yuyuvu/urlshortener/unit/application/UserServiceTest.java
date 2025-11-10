package com.github.yuyuvu.urlshortener.unit.application;

import com.github.yuyuvu.urlshortener.application.UserService;
import com.github.yuyuvu.urlshortener.domain.model.User;
import com.github.yuyuvu.urlshortener.domain.repository.UserRepository;
import com.github.yuyuvu.urlshortener.infrastructure.persistence.InMemoryUserRepository;
import java.util.ArrayList;
import java.util.HashMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/** Класс для тестов методов из UserService. */
public class UserServiceTest {

  /**
   * Проверка генерации случайных UUID, создания новых пользователей и их сохранения в репозитории.
   */
  @Test
  void makeNewUUIDAndUserAndSaveNewUserTest() {
    UserRepository userRepository = new InMemoryUserRepository(new HashMap<>());
    UserService userService = new UserService(userRepository);

    // Создаём 100 пользователей
    for (int i = 1; i <= 100; i++) {
      // Генерируем UUID и пользователя
      User user = userService.makeNewUUIDAndUser();
      Assertions.assertNotNull(user);

      // Проверяем методы из model User
      user.incrementAmountOfMadeShortLinks();
      user.incrementAmountOfMadeShortLinks();
      user.incrementAmountOfMadeShortLinks();
      user.decrementAmountOfMadeShortLinks();

      // Сохраняем созданного пользователя
      userService.saveNewUser(user);
    }

    // Проверяем, что в репозитории сохранилось 100 пользователей
    Assertions.assertEquals(100, userRepository.getAllUsers().size());

    // Проверяем, что у всех пользователей разные UUID
    Assertions.assertEquals(
        100, userRepository.getAllUsers().stream().map(User::getUUID).distinct().count());

    // Проверяем, что у всех пользователей счётчик созданных ссылок выставлен в 2
    Assertions.assertTrue(
        userRepository.getAllUsers().stream()
            .allMatch(user -> user.getAmountOfMadeShortLinks() == 2));
  }

  /**
   * Проверка двух очень похожих методов по получению пользователя по его UUID и по проверке
   * существования пользователя по UUID.
   */
  @Test
  void getUserByUUIDAndCheckUserExistenceByUUIDTest() {
    UserRepository userRepository = new InMemoryUserRepository(new HashMap<>());
    UserService userService = new UserService(userRepository);

    ArrayList<User> newUsers = new ArrayList<>();

    // Создаём и сохраняем в репозиторий 50 пользователей,
    // параллельно добавляем их в наш собственный список
    for (int i = 1; i <= 50; i++) {
      User user = userService.makeNewUUIDAndUser();
      Assertions.assertNotNull(user);
      userService.saveNewUser(user);

      newUsers.add(user);
    }

    // Проходимся по созданному вручную списку и пытаемся найти уже непосредственно
    // в сервисе соответствующего пользователя по UUID
    for (User userToFind : newUsers) {

      // Проверка возврата какого-то значения
      Assertions.assertTrue(userService.getUserByUUID(userToFind.getUUID()).isPresent());

      // Проверка повторного возврата, причём пользователь должен быть точно равен userToFind
      User foundUser = userService.getUserByUUID(userToFind.getUUID()).get();
      Assertions.assertEquals(userToFind.getUUID(), foundUser.getUUID());
      Assertions.assertEquals(
          userToFind.getAmountOfMadeShortLinks(), foundUser.getAmountOfMadeShortLinks());

      // Одновременно проверяем, что случайного пользователя getUserByUUID не находит
      Assertions.assertFalse(
          userService.getUserByUUID(userService.makeNewUUIDAndUser().getUUID()).isPresent());

      // Проверка существования пользователя по UUID через CheckUserExistenceByUUID
      Assertions.assertTrue(userService.checkUserExistenceByUUID(userToFind.getUUID()));

      // Одновременно проверяем, что случайного пользователя CheckUserExistenceByUUID не находит
      Assertions.assertFalse(
          userService.checkUserExistenceByUUID(userService.makeNewUUIDAndUser().getUUID()));
    }
  }
}
