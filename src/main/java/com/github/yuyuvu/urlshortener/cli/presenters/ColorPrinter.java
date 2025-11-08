package com.github.yuyuvu.urlshortener.cli.presenters;

/** Утилитарный класс ColorPrinter содержит методы для окрашивания текста, выводимого в консоль. */
public class ColorPrinter {
  private static final String RESET = "\033[0m"; // Text Reset

  private static final String YELLOW = "\033[0;33m"; // YELLOW
  private static final String GREEN = "\033[0;32m"; // GREEN
  private static final String RED = "\033[0;31m"; // RED
  private static final String CYAN = "\033[0;36m"; // CYAN
  private static final String PURPLE = "\033[0;35m"; // PURPLE

  // вывод без новой строки
  /** Печать в консоль без новой строки. Цвет: жёлтый. */
  public static void printYellow(String message) {
    System.out.print(YELLOW + message + RESET);
  }

  /** Печать в консоль без новой строки. Цвет: зелёный. */
  public static void printGreen(String message) {
    System.out.print(GREEN + message + RESET);
  }

  /** Печать в консоль без новой строки. Цвет: красный. */
  public static void printRed(String message) {
    System.out.print(RED + message + RESET);
  }

  /** Печать в консоль без новой строки. Цвет: бирюзовый. */
  public static void printCyan(String message) {
    System.out.print(CYAN + message + RESET);
  }

  /** Печать в консоль без новой строки. Цвет: сиреневый. */
  public static void printPurple(String message) {
    System.out.print(PURPLE + message + RESET);
  }

  /** Печать в консоль без новой строки. Цвет: белый. */
  public static void print(String message) {
    System.out.print(message);
  }

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

  /** Печать в консоль с новой строкой. Цвет: сиреневый. */
  public static void printlnPurple(String message) {
    System.out.println(PURPLE + message + RESET);
  }

  /** Печать в консоль с новой строкой. Цвет: белый. */
  public static void println(String message) {
    System.out.println(message);
  }

  // возврат окрашенной строки
  /** Окрашивание и возврат строки. Цвет: жёлтый. */
  public static String paintYellow(String message) {
    return YELLOW + message + RESET;
  }

  /** Окрашивание и возврат строки. Цвет: зелёный. */
  public static String paintGreen(String message) {
    return GREEN + message + RESET;
  }

  /** Окрашивание и возврат строки. Цвет: красный. */
  public static String paintRed(String message) {
    return RED + message + RESET;
  }

  /** Окрашивание и возврат строки. Цвет: бирюзовый. */
  public static String paintCyan(String message) {
    return CYAN + message + RESET;
  }

  /** Окрашивание и возврат строки. Цвет: сиреневый. */
  public static String paintPurple(String message) {
    return PURPLE + message + RESET;
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

  /** Пропуск строки. */
  public static void skipLine() {
    System.out.println();
  }

  /** Символ для прекращения окрашивания последующих символов. */
  public static String resetColor() {
    return RESET;
  }
}
