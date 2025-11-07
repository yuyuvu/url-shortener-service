package com.github.yuyuvu.urlshortener.cli.commands.impl;

import com.github.yuyuvu.urlshortener.cli.commands.CommandHandler;
import com.github.yuyuvu.urlshortener.cli.presenters.Presenter;
import com.github.yuyuvu.urlshortener.cli.viewmodels.ViewModel;

import java.util.UUID;

public class ExitCommandHandler implements CommandHandler {
  Presenter presenter;

  public ExitCommandHandler(Presenter presenter) {
    this.presenter = presenter;
  }

  @Override
  public ViewModel handle(String[] commandArgs, UUID currentUserUUID) {
    presenter.sendMessage("Выключаем сервис...");
    System.exit(0);
    return null;
  }
}
