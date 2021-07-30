package com.bob.hms.common;

/**
 * The Class Logger.
 */
/*
 * Wrapper over log4j, deprecated. Callers should be using log4j directly.
 */
public class Logger {
  static org.slf4j.Logger log4jLogger = org.slf4j.LoggerFactory.getLogger(Logger.class);

  /**
   * Log.
   *
   * @param message the message
   */
  public static void log(String message) {
    log4jLogger.debug(message);
  }

  /**
   * Log.
   *
   * @param message the message
   */
  public static void log(Object message) {
    log(message.toString());
  }

  /**
   * Log exception.
   *
   * @param message   the message
   * @param exception the e
   */
  public static void logException(String message, Exception exception) {
    log4jLogger.error(message, exception);
  }

}
