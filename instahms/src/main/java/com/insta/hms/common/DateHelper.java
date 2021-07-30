package com.insta.hms.common;

import com.insta.hms.common.preferences.genericpreferences.GenericPreferencesService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;

// TODO: Port complete DateUtil and port to LocalDate
/**
 * The Class DateHelper.
 * 
 * @author tanmay.k
 */
public class DateHelper {

  /** The generic preferences service. */
  @Autowired
  private static GenericPreferencesService genericPreferencesService;

  /** The Constant UTC_YEAR_MONTH_DATE_HOUR_MINUTE. */
  public static final String UTC_YEAR_MONTH_DATE_HOUR_MINUTE = "yyyy-MM-dd'T'HH:mm'Z'";

  /**
   * Parses the dates.
   *
   * @param date the date
   * @return the local date
   */
  public static LocalDate parseDate(String date) {
    return parseDate(date, "dd-MM-yyyy");
  }

  /**
   * Parses the dates.
   *
   * @param date    the date
   * @param pattern the pattern
   * @return the local date
   */
  public static LocalDate parseDate(String date, String pattern) {
    if (null == date || date.isEmpty()) {
      return null;
    }

    if ("${td}".equalsIgnoreCase(date) || "today".equalsIgnoreCase(date)) {
      return new LocalDate();
    } else if ("${pd}".equalsIgnoreCase(date) || "yesterday".equalsIgnoreCase(date)) {
      return new LocalDate().minusDays(1);
    }

    DateTimeFormatter formatter = DateTimeFormat.forPattern(pattern);
    return formatter.parseLocalDate(date);
  }

  /**
   * Parses the time.
   *
   * @param time the time to be parsed
   * @return the parsed String
   */
  public static String parseTime(String time) {
    return parseTime(time, "HH:mm:ss");
  }

  /**
   * Parses the time.
   *
   * @param time    the time to be parsed
   * @param pattern the pattern
   * @return the parsed String
   */
  public static String parseTime(String time, String pattern) {
    if (null == time || time.isEmpty() || pattern == null || pattern.isEmpty()) {
      return null;
    }
    DateTimeFormatter formatter = DateTimeFormat.forPattern(pattern);
    LocalTime parsedTime = formatter.parseLocalTime(time);
    return formatter.print(parsedTime);
  }

  /**
   * Parses the time stamp.
   *
   * @param timestamp the timestamp
   * @return the string
   */
  public static String parseTimeStamp(String timestamp) {
    return parseTimeStamp(timestamp, "dd-MM-yyyy HH:mm:ss");
  }

  /**
   * Parses the time stamp.
   *
   * @param timestamp the timestamp
   * @param pattern   the pattern
   * @return the parsed timestamp
   */
  public static String parseTimeStamp(String timestamp, String pattern) {
    if (null == timestamp || timestamp.isEmpty()) {
      return null;
    }

    if (timestamp.matches("^(today|td).*")) {
      timestamp = timestamp.replaceAll("(today|td)",
          new LocalDate().toString(DateTimeFormat.forPattern("dd-MM-YYYY")));
    } else if (timestamp.matches("^(yesterday|pd).*")) {
      timestamp = timestamp.replaceAll("(yesterday|pd)",
          new LocalDate().minusDays(1).toString(DateTimeFormat.forPattern("dd-MM-YYYY")));
    }
    DateTimeFormatter formatter = DateTimeFormat.forPattern(pattern);
    LocalDateTime parsedTimestamp = formatter.parseLocalDateTime(timestamp);
    return formatter.print(parsedTimestamp);
  }

  /**
   * Gets the time stamp.
   *
   * @param timestamp the timestamp String
   * @param pattern   the pattern
   * @return the time stamp
   */
  public static Timestamp getTimeStamp(String timestamp, String pattern) {
    if (null == timestamp || timestamp.isEmpty()) {
      return null;
    }
    DateTimeFormatter formatter = DateTimeFormat.forPattern(pattern);
    return new Timestamp(formatter.parseDateTime(timestamp).getMillis());
  }

  /**
   * Gets the date range.
   *
   * @param type the type
   * @return the date range
   */
  public static DateTime[] getDateRange(String type) {
    if (type.equals("td")) {
      return getTodaysDateRange();
    } else if (type.equals("pd")) {
      return getYesterdaysDateRange();
    } else if (type.equals("tw")) {
      return getThisWeeksDateRange();
    } else if (type.equals("pw")) {
      return getPreviousWeeksDateRange();
    } else if (type.equals("tm")) {
      return getThisMonthsDateRange();
    } else if (type.equals("pm")) {
      return getPreviousMonthsDateRange();
    } else if (type.equals("ty")) {
      return getCurrentYearsDateRange();
    } else if (type.equals("py")) {
      return getPreviousYearsDateRange();
    } else if (type.equals("tf")) {
      return getCurrentFinancialYearsDateRange();
    } else if (type.equals("pf")) {
      return getPreviousFinancialYearsDateRange();
    } else {
      return getTodaysDateRange();
    }
  }

  /**
   * Gets the current financial years date range.
   *
   * @return the current financial years date range
   */
  public static DateTime[] getCurrentFinancialYearsDateRange() {
    DateTime startDate;
    DateTime endDate;
    DateTime currentDate = new DateTime();
    BasicDynaBean preferencesBean = genericPreferencesService.getAllPreferences();
    Integer startMonth = (Integer) preferencesBean.get("fin_year_start_month");
    Integer endMonth = (Integer) preferencesBean.get("fin_year_end_month");

    if (startMonth > 0) {
      if (currentDate.getMonthOfYear() > endMonth) {
        startDate = new DateTime().withMonthOfYear(startMonth).withDayOfMonth(1);
        endDate = startDate.plusYears(1).minusMonths(1);
        endDate = endDate.dayOfMonth().withMaximumValue();
      } else {
        endDate = new DateTime().withMonthOfYear(endMonth);
        endDate = endDate.dayOfMonth().withMaximumValue();
        startDate = endDate.withMonthOfYear(startMonth).withDayOfMonth(1);
        startDate = startDate.minusYears(1);
      }
    } else {
      startDate = new DateTime().withMonthOfYear(startMonth).withDayOfMonth(1);
      endDate = new DateTime().withMonthOfYear(endMonth).withDayOfMonth(31);
    }

    return new DateTime[] { startDate, endDate };
  }

  /**
   * Gets the previous financial years date range.
   *
   * @return the previous financial years date range
   */
  public static DateTime[] getPreviousFinancialYearsDateRange() {
    DateTime startDate;
    DateTime endDate;
    DateTime currentDate = new DateTime();
    BasicDynaBean preferencesBean = genericPreferencesService.getAllPreferences();
    Integer startMonth = (Integer) preferencesBean.get("fin_year_start_month");
    Integer endMonth = (Integer) preferencesBean.get("fin_year_end_month");

    if (startMonth > 0) {
      if (currentDate.getMonthOfYear() > endMonth) {
        endDate = new DateTime().withMonthOfYear(endMonth);
        endDate = endDate.dayOfMonth().withMaximumValue();
        startDate = endDate.withMonthOfYear(startMonth).withDayOfMonth(1);
        startDate = startDate.minusYears(1);
      } else {
        endDate = new DateTime().minusYears(1).withMonthOfYear(endMonth);
        endDate = endDate.dayOfMonth().withMaximumValue();
        startDate = endDate.withMonthOfYear(startMonth).withDayOfMonth(1);
        startDate = startDate.minusYears(1);
      }
    } else {
      startDate = new DateTime().withMonthOfYear(startMonth).withDayOfMonth(1);
      startDate = startDate.minusYears(1);
      endDate = new DateTime().withMonthOfYear(endMonth).withDayOfMonth(31);
      endDate = endDate.minusYears(1);
    }

    return new DateTime[] { startDate, endDate };
  }

  /**
   * Gets the todays date range.
   *
   * @return the todays date range
   */
  public static DateTime[] getTodaysDateRange() {
    DateTime currentDate = getCurrentDate();
    return new DateTime[] { currentDate, currentDate };
  }

  /**
   * Gets the current date.
   *
   * @return the current date
   */
  public static DateTime getCurrentDate() {
    return new DateTime();
  }

  /**
   * Gets the yesterdays date range.
   *
   * @return the yesterdays date range
   */
  public static DateTime[] getYesterdaysDateRange() {
    DateTime yesterdayDate = getYesterdaysDate();
    return new DateTime[] { yesterdayDate, yesterdayDate };
  }

  /**
   * Gets the past date range.
   *
   * @param daysPast the days past
   * @return the past date range
   */
  public static DateTime[] getPastDateRange(int daysPast) {
    DateTime currentDate = getCurrentDate();
    DateTime pastDate = currentDate.minusDays(daysPast);
    return new DateTime[] { pastDate, currentDate };
  }

  /**
   * Gets the yesterdays date.
   *
   * @return the yesterdays date
   */
  public static DateTime getYesterdaysDate() {
    return new DateTime().minusDays(1);
  }

  /**
   * Gets the this weeks date range.
   *
   * @return the this weeks date range
   */
  public static DateTime[] getThisWeeksDateRange() {
    return getWeeklyDateRange(getCurrentDate());
  }

  /**
   * Gets the previous weeks date range.
   *
   * @return the previous weeks date range
   */
  public static DateTime[] getPreviousWeeksDateRange() {
    DateTime startDate = getCurrentDate().minusWeeks(1);
    return getWeeklyDateRange(startDate);
  }

  /**
   * Gets the weekly date range.
   *
   * @param date the date
   * @return the weekly date range
   */
  public static DateTime[] getWeeklyDateRange(DateTime date) {
    DateTime startDate = new DateTime().withWeekOfWeekyear(date.getWeekOfWeekyear()).dayOfWeek()
        .withMinimumValue();
    DateTime endDate = new DateTime().withWeekOfWeekyear(date.getWeekOfWeekyear()).dayOfWeek()
        .withMaximumValue();
    return new DateTime[] { startDate, endDate };
  }

  /**
   * Gets the this months date range.
   *
   * @return the this months date range
   */
  public static DateTime[] getThisMonthsDateRange() {
    return getMonthlyDateRange(getCurrentDate());
  }

  /**
   * Gets the previous months date range.
   *
   * @return the previous months date range
   */
  public static DateTime[] getPreviousMonthsDateRange() {
    DateTime startDate = getCurrentDate().minusMonths(1);
    return getMonthlyDateRange(startDate);
  }

  /**
   * Gets the monthly date range.
   *
   * @param date the date
   * @return the monthly date range
   */
  public static DateTime[] getMonthlyDateRange(DateTime date) {
    DateTime startDate = new DateTime().withMonthOfYear(date.getMonthOfYear()).dayOfMonth()
        .withMinimumValue();
    DateTime endDate = new DateTime().withMonthOfYear(date.getMonthOfYear()).dayOfMonth()
        .withMaximumValue();
    return new DateTime[] { startDate, endDate };
  }

  /**
   * Gets the current years date range.
   *
   * @return the current years date range
   */
  public static DateTime[] getCurrentYearsDateRange() {
    return getYearlyDateRange(getCurrentDate());
  }

  /**
   * Gets the previous years date range.
   *
   * @return the previous years date range
   */
  public static DateTime[] getPreviousYearsDateRange() {
    DateTime startDate = new DateTime().minusYears(1);
    return getYearlyDateRange(startDate);
  }

  /**
   * Gets the yearly date range.
   *
   * @param date the date
   * @return the yearly date range
   */
  public static DateTime[] getYearlyDateRange(DateTime date) {
    DateTime startDate = new DateTime().withYear(date.getYear()).dayOfYear().withMinimumValue();
    DateTime endDate = new DateTime().withYear(date.getYear()).dayOfYear().withMaximumValue();
    return new DateTime[] { startDate, endDate };
  }

  /**
   * Gets the UTC time formatted according to given pattern.
   *
   * @param millis  - milliseconds from 1970-01-01T00:00:00Z (Unix Epoch)
   * @param pattern - The pattern to format the date to
   * @return the UTC time
   */
  public static String getUTCTime(long millis, String pattern) {
    DateTimeFormatter formatter = DateTimeFormat.forPattern(pattern);
    DateTime dateTime = new DateTime(millis, DateTimeZone.UTC);
    return dateTime.toString(formatter);
  }

  /** The date formatter. */
  public static SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy");

  /**
   * Parses the dateto sql date.
   *
   * @param dateStr the date str
   * @return the java.sql. date
   */
  public static java.sql.Date parseDatetoSqlDate(String dateStr) {
    if ((dateStr != null) && !dateStr.equals("")) {
      java.util.Date dt = null;
      try {
        dt = dateFormatter.parse(dateStr);
      } catch (ParseException exception) {
        // TODO Auto-generated catch block
        exception.printStackTrace();
      }
      return new java.sql.Date(dt.getTime());
    } else {
      return null;
    }
  }
}