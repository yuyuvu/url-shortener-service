package com.github.yuyuvu.urlshortener.cli.commands;

import com.github.yuyuvu.urlshortener.cli.viewmodels.ViewModel;
import java.util.UUID;

public interface CommandHandler {
  ViewModel handle(String[] commandArgs, UUID currentUserUUID);
}
