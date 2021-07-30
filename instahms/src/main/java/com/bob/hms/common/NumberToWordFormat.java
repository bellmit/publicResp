package com.bob.hms.common;

import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDTO;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Locale;

/**
 * The Class NumberToWordFormat.
 */
public abstract class NumberToWordFormat {

  /**
   * To rupees paise.
   *
   * @param amount the amount
   * @return the string
   * @throws SQLException the SQL exception
   */
  public abstract String toRupeesPaise(BigDecimal amount) throws SQLException;

  /**
   * To word.
   *
   * @param amount the amount
   * @return the string
   * @throws SQLException the SQL exception
   */
  public abstract String toWord(BigDecimal amount) throws SQLException;

  /**
   * To word.
   *
   * @param amount the amount
   * @return the string
   */
  public abstract String toWord(long amount);

  String[] ones = null;
  String[] tens = null;
  String[] elevens = null;
  String[] hundreds = null;
  String[] decimalsInWords = null;
  String decimalSeparator = null;
  String sep = null;

  /**
   * Word format.
   *
   * @return the number to word format
   * @throws SQLException the SQL exception
   */
  public static NumberToWordFormat wordFormat() throws SQLException {
    Locale userLocale = RequestContext.getLocale();
    java.util.ResourceBundle resourceBundle = java.util.ResourceBundle
        .getBundle("java.resources.application", userLocale);

    NumberToWordFormat impl = null;
    GenericPreferencesDTO gdto = GenericPreferencesDAO.getGenericPreferences();
    String currencyFormat = "Millions";
    if (currencyFormat.equalsIgnoreCase(gdto.getCurrencyFormat())) {
      impl = new NumberToStringConversionMillion();
    } else {
      impl = new NumberToStringConversionLakh();
    }
    if (resourceBundle != null) {
      // Value for key errors.notempty
      impl.ones = resourceBundle.getString("numbersystem.ones")
          .split(resourceBundle.getString("numbersystem.comma"));
      impl.tens = resourceBundle.getString("numbersystem.tens")
          .split(resourceBundle.getString("numbersystem.comma"));
      impl.elevens = resourceBundle.getString("numbersystem.elevens")
          .split(resourceBundle.getString("numbersystem.comma"));
      impl.hundreds = resourceBundle.getString("numbersystem.hundreds")
          .split(resourceBundle.getString("numbersystem.comma"));
      impl.sep = resourceBundle.getString("numbersystem.separator");
      impl.decimalsInWords = resourceBundle.getString("numbersystem.decimal.inwords")
          .split(resourceBundle.getString("numbersystem.comma"));
      impl.decimalSeparator = resourceBundle.getString("numbersystem.decimal.word.separator");
    }
    return impl;
  }
}
