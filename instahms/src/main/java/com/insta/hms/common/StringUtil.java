package com.insta.hms.common;

/**
 * The Class StringUtil.
 */
public class StringUtil {

  /** The Constant URL_PATH_SEPARATOR. */
  private static final String URL_PATH_SEPARATOR = "/";

  /**
   * Pretty name.
   *
   * @param input the input
   * @return the string
   */
  public static String prettyName(String input) {

    if (null == input) {
      return null;
    }

    StringBuilder sb = new StringBuilder();
    String[] words = input.split("_");
    // Look for all _ and replace it with space and capitalize the following character.
    for (int i = 0; i < words.length; i++) {
      if (i != 0) {
        sb.append(" ");
      }
      sb.append(Character.toUpperCase(words[i].charAt(0))).append(words[i].substring(1));
    }
    return sb.toString();
  }

  /**
   * Make URL path.
   *
   * @param parts the parts
   * @return the string
   */
  public static String makeURLPath(String[] parts) {
    String urlPath = join(parts, URL_PATH_SEPARATOR);
    return (null != urlPath && urlPath.startsWith(URL_PATH_SEPARATOR)) ? urlPath
        : URL_PATH_SEPARATOR + urlPath;
  }

  /**
   * Join.
   *
   * @param parts     the parts
   * @param separator the separator
   * @return the string
   */
  public static String join(String[] parts, String separator) {
    StringBuilder pathBuilder = new StringBuilder();

    if (null == parts) {
      return null;
    }

    if (parts.length <= 0) {
      return "";
    }

    for (int i = 0; i < parts.length; i++) {
      String pathElement = trim(parts[i], separator);
      if (i != 0) {
        pathBuilder.append(separator);
      }
      pathBuilder.append(pathElement);
    }
    String ret = pathBuilder.toString();
    return ret;
  }

  /**
   * Trim.
   *
   * @param input       the input
   * @param trimPattern the trim pattern
   * @return the string
   */
  private static String trim(String input, String trimPattern) {
    return trimRight(trimLeft(input, trimPattern), trimPattern);
  }

  /**
   * Trim left.
   *
   * @param input       the input
   * @param trimPattern the trim pattern
   * @return the string
   */
  private static String trimLeft(String input, String trimPattern) {
    if (null == input) {
      return null;
    }
    if (null == trimPattern) {
      return input;
    }
    String trimmed = input.trim();
    String pattern = trimPattern.trim();
    String output = trimmed.startsWith(pattern) ? trimmed.substring(pattern.length()) : trimmed;
    return output;
  }

  /**
   * Trim right.
   *
   * @param input       the input
   * @param trimPattern the trim pattern
   * @return the string
   */
  private static String trimRight(String input, String trimPattern) {
    if (null == input) {
      return null;
    }
    if (null == trimPattern) {
      return input;
    }
    String trimmed = input.trim();
    String pattern = trimPattern.trim();
    String output = trimmed.endsWith(pattern)
        ? trimmed.substring(0, trimmed.length() - pattern.length())
        : trimmed;
    return output;
  }

  /**
   * Checks if is null or empty.
   *
   * @param string the string
   * @return true, if is null or empty
   */
  public static boolean isNullOrEmpty(String string) {
    if (string == null || string.trim().isEmpty()) {
      return true;
    }
    return false;
  }
}
