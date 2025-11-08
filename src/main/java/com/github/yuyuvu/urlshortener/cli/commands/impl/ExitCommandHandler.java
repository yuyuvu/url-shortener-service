package com.github.yuyuvu.urlshortener.cli.commands.impl;

import com.github.yuyuvu.urlshortener.cli.commands.CommandHandler;
import com.github.yuyuvu.urlshortener.cli.presenters.Presenter;
import com.github.yuyuvu.urlshortener.cli.viewmodels.ViewModel;
import java.util.UUID;

/** Обработчик команды exit, отвечающей за выключение приложения. Добавлена для удобства. */
public class ExitCommandHandler implements CommandHandler {
  private final Presenter presenter;

  /** Конструктор обработчика команды exit, отвечающего за выключение приложения. */
  public ExitCommandHandler(Presenter presenter) {
    this.presenter = presenter;
  }

  /**
   * Метод handle в данном случае только выводит сообщение о выключении и вызывает запрос на
   * выключение JVM.
   */
  @Override
  public ViewModel handle(String[] commandArgs, UUID currentUserUUID) {
    presenter.sendMessage("Выключаем сервис...");
    System.exit(0);
    return null;
  }
}
