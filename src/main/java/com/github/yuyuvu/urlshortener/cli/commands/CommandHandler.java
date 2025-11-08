package com.github.yuyuvu.urlshortener.cli.commands;

import com.github.yuyuvu.urlshortener.cli.viewmodels.ViewModel;
import java.util.UUID;

/**
 * Интерфейс CommandHandler представляет объекты, которые могут обрабатывать запрос пользователя с
 * определённым UUID на выполнение некоторой команды сервиса сокращения ссылок.
 */
public interface CommandHandler {
  /**
   * Метод handle принимает аргументы для команды и UUID пользователя, который её вызвал, и далее
   * выполняет определённое действие, вызывает методы сервисного слоя приложения для выполнения
   * определённой логики или возвращает ошибку в случае предоставления некорректных аргументов или
   * недостаточных прав доступа для выполнения команды от данного UUID.
   */
  ViewModel handle(String[] commandArgs, UUID currentUserUUID);
}
