package com.github.yuyuvu.urlshortener.cli.presenters;

/** Утилитарный класс ColorPrinter содержит методы для окрашивания текста, выводимого в консоль. */
public class ColorPrinter {
  private static final String RESET = "\033[0m"; // Text Reset

  private static final String YELLOW = "\033[0;33m"; // YELLOW
  private static final String GREEN = "\033[0;32m"; // GREEN
  private static final String RED = "\033[0;31m"; // RED
  private static final String CYAN = "\033[0;36m"; // CYAN
  private static final String PURPLE = "\033[0;35m"; // PURPLE

  // вывод с новой строкой

  /** Печать в консоль с новой строкой. Цвет: жёлтый. */
  public static void printlnYellow(String message) {
    System.out.println(YELLOW + message + RESET);
  }

  /** Печать в консоль с новой строкой. Цвет: зелёный. */
  public static void printlnGreen(String message) {
    System.out.println(GREEN + message + RESET);
  }

  /** Печать в консоль с новой строкой. Цвет: красный. */
  public static void printlnRed(String message) {
    System.out.println(RED + message + RESET);
  }

  /** Печать в консоль с новой строкой. Цвет: бирюзовый. */
  public static void printlnCyan(String message) {
    System.out.println(CYAN + message + RESET);
  }

  /** Очищение строки от символов окрашивания. */
  public static String deleteColorsFromString(String content) {
    return content
        .replace(YELLOW, "")
        .replace(GREEN, "")
        .replace(RED, "")
        .replace(CYAN, "")
        .replace(PURPLE, "")
        .replace(RESET, "");
  }
}
