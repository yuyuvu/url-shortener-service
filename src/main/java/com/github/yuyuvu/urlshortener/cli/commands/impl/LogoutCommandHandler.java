package com.github.yuyuvu.urlshortener.cli.commands.impl;

import com.github.yuyuvu.urlshortener.cli.commands.CommandHandler;
import com.github.yuyuvu.urlshortener.cli.viewmodels.ViewModel;
import com.github.yuyuvu.urlshortener.cli.viewmodels.impl.ErrorViewModel;
import com.github.yuyuvu.urlshortener.cli.viewmodels.impl.SuccessViewModel;
import java.util.UUID;
import java.util.function.Consumer;

public class LogoutCommandHandler implements CommandHandler {
  Consumer<UUID> onSuccessfulLogoutDo;

  public LogoutCommandHandler(Consumer<UUID> onSuccessfulLogoutDo) {
    this.onSuccessfulLogoutDo = onSuccessfulLogoutDo;
  }

  @Override
  public ViewModel handle(String[] commandArgs, UUID currentUserUUID) {
    if (currentUserUUID == null) {
      return new ErrorViewModel(
          "Вы не можете использовать данную команду, так как ещё не идентифицировались в системе.");
    }
    onSuccessfulLogoutDo.accept(null);
    return new SuccessViewModel("Вы перестали идентифицироваться с UUID " + currentUserUUID);
  }
}
