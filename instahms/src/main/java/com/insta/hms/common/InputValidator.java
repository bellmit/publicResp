package com.insta.hms.common;

import org.owasp.esapi.ESAPI;
import org.owasp.esapi.errors.IntrusionException;
import org.owasp.esapi.errors.ValidationException;

/*
 * Wrapper around the ESAPI validator
 */
public class InputValidator {

  // returns a safe string (alphanumeric only)
  public static String getSafeString(String name, String input, int maxLength, boolean allowNull)
      throws ValidationException, IntrusionException {
    return ESAPI.validator().getValidInput(name, input, "SafeString", maxLength, allowNull);
  }

  /*
   * returns a special string (alphanumeric, hyphen, underscore allowed) The rule is defined in
   * validation.properties
   */
  public static String getSafeSpecialString(String name, String input, int maxLength,
      boolean allowNull) throws ValidationException, IntrusionException {
    return ESAPI.validator().getValidInput(name, input, "SafeStringSpecial", maxLength, allowNull);
  }

  /*
   * returns a special string (alphanumeric, hyphen, underscore allowed) The rule is defined in
   * validation.properties
   */
  public static String getSafeSchemaString(String name, String input, int maxLength,
      boolean allowNull) throws ValidationException, IntrusionException {
    return ESAPI.validator().getValidInput(name, input, "SafeSchema", maxLength, allowNull);
  }

}
