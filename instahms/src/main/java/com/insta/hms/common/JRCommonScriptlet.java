package com.insta.hms.common;

import com.bob.hms.common.NumberToWordFormat;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDTO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Contains a bunch of functions that can be called from within JRXMLs as
 * utility functions, using $P{REPORT_SCRIPTLET}.<methodName>(), for example,
 * $P{REPORT_SCRIPTLET}.toWords(100.00)
 *
 * In order for iReport to work correctly with this class, the best way is to
 * include WEB-INF/classes in your classpath before starting iReport, or just add
 * the following to iReport.sh in the beginning, eg:
 *   CLASSPATH=/home/vasan/workspace/instahms/WEB-INF/classes
 *
 */

import java.math.BigDecimal;
import java.sql.SQLException;

/**
 * The Class JRCommonScriptlet.
 */
public class JRCommonScriptlet extends net.sf.jasperreports.engine.JRDefaultScriptlet {

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(JRCommonScriptlet.class);

  /**
   * To words. Convert an amount to words
   *
   * @param number the number
   * @return the string
   * @throws SQLException the SQL exception
   */
  public String toWords(long number) throws SQLException {
    NumberToWordFormat numberconvert = NumberToWordFormat.wordFormat();
    return numberconvert.toWord(number);
  }

  /**
   * To words.
   *
   * @param amount the amount
   * @return the string
   * @throws SQLException the SQL exception
   */
  public String toWords(BigDecimal amount) throws SQLException {
    NumberToWordFormat numberconvert = NumberToWordFormat.wordFormat();
    GenericPreferencesDTO dto = GenericPreferencesDAO.getGenericPreferences();
    int numOfDecimals = dto.getDecimalDigits();
    int paiseDividedBy = new Double(Math.pow(10, numOfDecimals)).intValue();
    if (amount == null) {
      return "";
    }
    amount = amount.abs();
    long rupeeValue = amount.intValue();
    long paiseValue = amount.multiply(new BigDecimal(paiseDividedBy)).intValue() % paiseDividedBy;

    String strRupees = dto.getWhole() + " " + numberconvert.toWord(rupeeValue);
    String strPaise = "";
    if (paiseValue > 0) {
      strPaise = " and " + dto.getDecimal() + " " + numberconvert.toWord(paiseValue);
    }

    return strRupees + strPaise;
  }

  /**
   * Null blank. Return Blank string if null, else, the original string.
   *
   * @param string the s
   * @return the string
   */
  public String nullBlank(String string) {
    return (string == null) ? "" : string;
  }

  /**
   * Returns number of decimal places used in amount fields May 6, 2011 - part of 7.4 enhancements.
   * Used in jrxml reports to get number of decimals to pass as a paramter to subreports
   *
   * @return the num decimal places
   * @throws SQLException the SQL exception
   */
  public int getNumDecimalPlaces() throws SQLException {
    GenericPreferencesDTO dto = GenericPreferencesDAO.getGenericPreferences();
    return dto.getDecimalDigits();
  }

  /**
   * Dec format. Returns decimal format based on Generic Preference after_decimal_digita: May 4,
   * 2011 - As part of 7.4 enhancements
   *
   * @param bigDecimal the s
   * @return the string
   * @throws SQLException the SQL exception
   */
  public String decFormat(BigDecimal bigDecimal) throws SQLException {
    String formattedStr = "";
    int decimalDigits = getNumDecimalPlaces();

    formattedStr = decFormat(bigDecimal, decimalDigits);
    return formattedStr;
  }

  /**
   * Similar to above, except that it does not query the generic preferences but rather accepts the
   * no. of decimal places as a parameter Called from subreports
   *
   * @param num   the num
   * @param scale the scale
   * @return the string
   */
  public String decFormat(BigDecimal num, int scale) {
    BigDecimal value = num.setScale(scale, BigDecimal.ROUND_HALF_UP);
    return value.toString();
  }

  /**
   * Dec format.
   *
   * @param num   the num
   * @param scale the scale
   * @return the string
   */
  public String decFormat(BigDecimal num, Integer scale) {
    BigDecimal value = num.setScale(scale, BigDecimal.ROUND_HALF_UP);
    return value.toString();
  }

  /**
   * Dec format.
   *
   * @param num   the num
   * @param scale the scale
   * @return the string
   */
  public String decFormat(BigDecimal num, String scale) {
    BigDecimal value = num.setScale(Integer.parseInt(scale), BigDecimal.ROUND_HALF_UP);
    return value.toString();
  }
}
