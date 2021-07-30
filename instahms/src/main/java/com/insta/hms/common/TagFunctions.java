package com.insta.hms.common;

import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;

import flexjson.JSON;
import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.owasp.esapi.errors.EncodingException;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The Class TagFunctions. A set of static functions for convenient use in JSP pages. For example:
 * ${ifn:afmt(value)} will be replaced with the value formatted according to amount (currency)
 * rules.
 */
public class TagFunctions {

  /**
   * Format a BigDecimal according to preference based number of decimals.
   *
   * @param value the value
   * @return the string
   * @throws SQLException the SQL exception
   */
  public static String afmt(BigDecimal value) throws SQLException {
    return afmt(value, false);
  }

  /**
   * Afmt.
   *
   * @param value            the value
   * @param suppressDecimals the suppress decimals
   * @return the string
   * @throws SQLException the SQL exception
   */
  public static String afmt(BigDecimal value, boolean suppressDecimals) throws SQLException {

    if (value == null) {
      return "0";
    }

    int numDecimals = GenericPreferencesDAO.getGenericPreferences().getDecimalDigits();

    // round up the value, just in case we were passed a
    // number with a different scale
    value = value.setScale(numDecimals, BigDecimal.ROUND_HALF_UP);

    DecimalFormat dec;

    // if the remainder for dividing by one is non-zero, then we show 2 decimal places.
    if (value.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 0 && suppressDecimals) {
      dec = new DecimalFormat("#");
    } else {
      String decimalFormat = "";
      if (numDecimals == 2) {
        decimalFormat = "0.00";
      } else if (numDecimals == 3) {
        decimalFormat = "0.000";
      } else if (numDecimals == 1) {
        decimalFormat = "0.0";
      } else if (numDecimals == 4) {
        decimalFormat = "0.0000";
      }
      dec = new DecimalFormat(decimalFormat);
    }
    return dec.format(value);
  }

  /**
   * Round.
   *
   * @param value         the value
   * @param type          the type
   * @param numOfDecimals the num of decimals
   * @return the string
   * @throws SQLException the SQL exception
   */
  public static String round(BigDecimal value, String type, String numOfDecimals)
      throws SQLException {
    if (value == null) {
      return "0";
    }
    if (type == null || type.equals("")) {
      type = "ROUND_HALF_UP";
    }
    int numDecimals = numOfDecimals == null || numOfDecimals.equals("")
        ? GenericPreferencesDAO.getGenericPreferences().getDecimalDigits()
        : Integer.parseInt(numOfDecimals);
    DecimalFormat dec;
    String decimalFormat = "#";
    if (numDecimals == 2) {
      decimalFormat = "0.00";
    } else if (numDecimals == 3) {
      decimalFormat = "0.000";
    } else if (numDecimals == 1) {
      decimalFormat = "0.0";
    } else if (numDecimals == 4) {
      decimalFormat = "0.0000";
    }
    value = value.setScale(4, BigDecimal.ROUND_HALF_UP);
    dec = new DecimalFormat(decimalFormat);
    if (type.equalsIgnoreCase("NO_ROUND")) {
      return dec.format(value);
    } else if (type.equalsIgnoreCase("ROUND_HALF_UP")) {
      return dec.format(value.setScale(numDecimals, BigDecimal.ROUND_HALF_UP));
    } else if (type.equalsIgnoreCase("ROUND_HALF_DOWN")) {
      return dec.format(value.setScale(numDecimals, BigDecimal.ROUND_HALF_DOWN));
    } else if (type.equalsIgnoreCase("ROUND_CEILING")) {
      return dec.format(value.setScale(numDecimals, BigDecimal.ROUND_CEILING));
    } else if (type.equalsIgnoreCase("ROUND_FLOOR")) {
      return dec.format(value.setScale(numDecimals, BigDecimal.ROUND_FLOOR));
    } else if (type.equalsIgnoreCase("ROUND_DOWN")) {
      return dec.format(value.setScale(numDecimals, BigDecimal.ROUND_DOWN));
    } else if (type.equalsIgnoreCase("ROUND_UP")) {
      return dec.format(value.setScale(numDecimals, BigDecimal.ROUND_UP));
    } else if (type.equalsIgnoreCase("ROUND_HALF_EVEN")) {
      return dec.format(value.setScale(numDecimals, BigDecimal.ROUND_HALF_EVEN));
    } else {
      return dec.format(value.toBigInteger());
    }
  }

  /**
   * Format a BigDecimal number so that: - If paise is non-zero, display the decmial places - If
   * paise is zero (usual case) display no decimals.
   *
   * @param value the value
   * @return the string
   * @throws SQLException the SQL exception
   */
  public static String afmts(BigDecimal value) throws SQLException {
    return afmt(value, true);
  }

  /**
   * Get the absolute value.
   *
   * @param value the value
   * @return the big decimal
   */
  public static BigDecimal abs(BigDecimal value) {
    if (value == null) {
      return new BigDecimal(0);
    }
    double doub = value.doubleValue();
    doub = Math.abs(doub);
    return new BigDecimal(doub);
  }

  // break the content from DB with new lines to show in separate lines

  /**
   * Break content.
   *
   * @param refRange the ref range
   * @return the string
   */
  public static String breakContent(String refRange) {
    String[] strs = refRange.split("\n");
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < strs.length; i++) {
      sb.append(strs[i]);
      sb.append("<br/>");
    }
    return sb.toString();
  }

  /**
   * Break content.
   *
   * @param text          the text
   * @param afterNumChars the after num chars
   * @return the string
   */
  public static String breakContent(String text, Integer afterNumChars) {
    StringBuilder sb = new StringBuilder(text);
    int vari = afterNumChars;
    for (int index = 0; index < sb.length(); index++) {
      if (index == vari) {
        if (sb.charAt(index) == ' ') {
          sb.insert(index + 1, "<br/>");
          vari = index + afterNumChars;
        } else {
          int varj = index;
          index = sb.lastIndexOf(" ", index);
          if (index < varj) {
            index = varj;
          }
          sb.insert(index + 1, "<br/>");
          vari = index + afterNumChars;
        }
      }
    }
    return sb.toString();
  }

  /**
   * Search for existence of a string in a list of strings (convenient for setting checkbox/list
   * value), and return its index.
   *
   * @param list   the l
   * @param toFind the to find
   * @return the int
   */
  public static int listFind(List list, String toFind) {
    if (list == null || toFind == null) {
      return -1;
    }

    int length = list.size();
    for (int i = 0; i < length; i++) {
      if (toFind.equals((String) list.get(i))) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Array find.
   *
   * @param arr    the a
   * @param toFind the to find
   * @return the int
   */
  public static int arrayFind(String[] arr, String toFind) {
    if (arr != null) {
      for (int i = 0; i < arr.length; i++) {
        if (toFind.trim().equals(arr[i].trim())) {
          return i;
        }
      }
    }
    return -1;
  }

  /**
   * Next date.
   *
   * @param startDate the start date
   * @param days      the days
   * @return the java.util. date
   */
  public static java.util.Date nextDate(java.util.Date startDate, int days) {
    if (startDate == null) {
      return null;
    }
    Calendar cal = Calendar.getInstance();
    cal.setTime(startDate);
    cal.add(Calendar.DATE, days);
    startDate = cal.getTime();
    return startDate;
  }

  /**
   * Next date string.
   *
   * @param startDate the start date
   * @param seperator the seperator
   * @return the string
   */
  public static String nextDateString(java.util.Date startDate, String seperator) {
    if (startDate == null) {
      return null;
    }
    SimpleDateFormat sdf = new SimpleDateFormat("MM" + seperator + "dd" + seperator + "yyyy");
    String nextDt = sdf.format(startDate);
    return nextDt;
  }

  /**
   * Pretty print.
   *
   * @param input the input
   * @return the string
   */
  public static String prettyPrint(String input) {
    if (null == input) {
      return null;
    }

    StringBuilder sb = new StringBuilder();
    String[] words = input.split("_");
    // Look for all _ and replace it with space and capatalize the following character.
    for (int i = 0; i < words.length; i++) {
      if (i != 0) {
        sb.append(" ");
      }
      sb.append(Character.toUpperCase(words[i].charAt(0))).append(words[i].substring(1));
    }
    return sb.toString();
  }

  /**
   * Encode uri component.
   *
   * @param str the str
   * @return the string
   */
  public static String encodeUriComponent(String str) {
    try {
      return URLEncoder.encode(str, "UTF-8");
    } catch (java.io.UnsupportedEncodingException exception) {
      // ignore, return null
    }
    return null;
  }

  /**
   * Contains.
   *
   * @param coll   the coll
   * @param object the o
   * @return true, if successful
   */
  public static boolean contains(Collection<?> coll, Object object) {
    return coll.contains(object);
  }

  /**
   * Checks if is numeric.
   *
   * @param text the text
   * @return true, if is numeric
   */
  public static boolean isNumeric(String text) {
    boolean result = false;
    // String to be scanned to find the pattern.

    String pattern = "^[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?$";
    // Create a Pattern object
    Pattern regex = Pattern.compile(pattern);

    // Now create matcher object.
    Matcher mmatcher = regex.matcher(text);
    result = mmatcher.find();
    return result;
  }

  /**
   * To string.
   *
   * @param num the num
   * @return the string
   */
  public static String toString(Integer num) {
    return num + "";
  }

  /**
   * List all.
   *
   * @param tableName the table name
   * @param orderBy   the order by
   * @return the list
   * @throws SQLException the SQL exception
   */
  public static List listAll(String tableName, String orderBy) throws SQLException {
    return new GenericDAO(tableName).listAll(orderBy);
  }

  /**
   * JSO nlist.
   *
   * @param tableName the table name
   * @return the string
   * @throws SQLException the SQL exception
   */
  public static String jsonList(String tableName) throws SQLException {
    JSONSerializer js = new JSONSerializer().exclude("class");
    return js
        .serialize(ConversionUtils.copyListDynaBeansToMap(new GenericDAO(tableName).listAll()));
  }

  /*
   * public static String cleanHtmlAttribute(String value) { return
   * ESAPI.encoder().encodeForHTMLAttribute(value); }
   * 
   * public static String cleanHtml(String value) { return ESAPI.encoder().encodeForHTML(value); }
   * 
   * public static Object cleanJavaScript(Object value) { if (value != null) return
   * ESAPI.encoder().encodeForJavaScript(value.toString());
   * 
   * return value; } public static String cleanURL(String value) throws EncodingException { return
   * ESAPI.encoder().encodeForURL(value); }
   */

  /**
   * Clean html attribute.
   *
   * @param value the value
   * @return the string
   */
  public static String cleanHtmlAttribute(String value) {
    return org.owasp.encoder.Encode.forHtmlAttribute(value);
  }

  /**
   * Clean html.
   *
   * @param value the value
   * @return the string
   */
  public static String cleanHtml(String value) {
    return org.owasp.encoder.Encode.forHtml(value);
  }

  /**
   * Clean java script.
   *
   * @param value the value
   * @return the object
   */
  public static Object cleanJavaScript(Object value) {
    if (value != null) {
      return org.owasp.encoder.Encode.forJavaScript(value.toString());
    }
    return value;
  }

  /**
   * Clean URL.
   *
   * @param value the value
   * @return the string
   * @throws EncodingException the encoding exception
   */
  public static String cleanURL(String value) throws EncodingException {
    return org.owasp.encoder.Encode.forUriComponent(value);
  }

  /**
   * This method will use list object and convert into json serializer formate and will return json
   * string.
   *
   * @param list the list
   * @return the string
   */
  public static String convertListToJson(List<?> list) {
    JSONSerializer js = new JSONSerializer().exclude("class");
    if (list != null && !list.isEmpty() && list.get(0) instanceof BasicDynaBean) {
      return js.serialize(ConversionUtils.listBeanToListMap(list));
    }
    return js.serialize(list);
  }

  /**
   * This method will use Map object and convert into json serializer formate and will return json
   * string.
   *
   * @param map the map
   * @return the string
   */
  public static String convertMapToJson(Map<?, ?> map) {
    JSONSerializer js = new JSONSerializer().exclude("class");
    return js.serialize(map);
  }

  public static String convertToDeepSerializedJSON(Object object) {
    JSONSerializer js = new JSONSerializer().exclude("class");
    return js.deepSerialize(object);
  }

  /**
   * This method willl use to convert List of BasicDynaBean to MapMap.
   *
   * @param dynaBeans the dyna beans
   * @param col       the col
   * @return the map
   */
  public static Map listBeantoMapMap(List<Map> dynaBeans, String col) {
    return ConversionUtils.listMapToMapMap(dynaBeans, col);
  }

  /**
   * This method willl use to convert List of Map to MapMapBean.
   *
   * @param dynaBeans the dyna beans
   * @param col1      the col 1
   * @param col2      the col 2
   * @return the map
   */
  public static Map listMapToMapMapBean(List<Map> dynaBeans, String col1, String col2) {
    return ConversionUtils.listMapToMapMapBean(dynaBeans, col1, col2);
  }

}
