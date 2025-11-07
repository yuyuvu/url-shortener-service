package com.github.yuyuvu.urlshortener.cli.commands.impl;

import com.github.yuyuvu.urlshortener.cli.commands.CommandHandler;
import com.github.yuyuvu.urlshortener.cli.viewmodels.ViewModel;

import java.util.UUID;

public class HelpCommandHandler implements CommandHandler {

  @Override
  public ViewModel handle(String[] commandArgs, UUID currentUserUUID) {
    return null;
  }
}
