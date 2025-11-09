package com.github.yuyuvu.urlshortener.cli.commands.impl;

import com.github.yuyuvu.urlshortener.cli.commands.CommandHandler;
import com.github.yuyuvu.urlshortener.cli.viewmodels.ViewModel;
import com.github.yuyuvu.urlshortener.cli.viewmodels.impl.ErrorViewModel;
import com.github.yuyuvu.urlshortener.cli.viewmodels.impl.SuccessViewModel;
import com.github.yuyuvu.urlshortener.infrastructure.config.ConfigManager;
import java.util.UUID;

/** Обработчик команды config, отвечающей за перезагрузку настроек из файла. */
public class ConfigCommandHandler implements CommandHandler {
  private final ConfigManager configManager;

  /** Конструктор обработчика команды config, отвечающего за перезагрузку настроек из файла. */
  public ConfigCommandHandler(ConfigManager configManager) {
    this.configManager = configManager;
  }

  @Override
  public ViewModel handle(String[] commandArgs, UUID currentUserUUID) {
    if (commandArgs.length > 0 && commandArgs[0].equalsIgnoreCase("reload")) {
      configManager.reloadConfig();
      return new SuccessViewModel(
          """
          Конфигурация успешно перезагружена!
          Обратите внимание: изменённые настройки параметров коротких ссылок \
          начнут применяться только к новым ссылкам.
          Однако прочие параметры, \
          такие как путь до файла хранилища или изменённые URL сервиса, применяются сразу.""");
    } else {
      return new ErrorViewModel(
          "Правильное использование команды: config reload."
              + "\nВы указали недостаточное количество аргументов.");
    }
  }
}
