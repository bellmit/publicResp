/**
 *
 */

package com.bob.hms.common;

import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;


import org.apache.commons.beanutils.BasicDynaBean;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Months;
import org.joda.time.Years;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.DatatypeConverter;

// TODO: Auto-generated Javadoc
/**
 * The Class DateUtil.
 *
 * @author krishna.t
 */
public class DateUtil {

  private static final Logger logger = LoggerFactory.getLogger(DateUtil.class);

  public static final String DURATION_DAYS = "days";
  public static final String DURATION_MONTHS = "months";
  public static final String DURATION_YEARS = "years";
  
  /**
   * Gets the age for date.
   *
   * @param date the date
   * @return the age for date
   */
  public static Map getAgeForDate(java.util.Date date) {
    int daysInAMonth = 31;
    int daysInTwoYears = 730;

    long age = getDifferenceDays(date, DURATION_DAYS);
    String ageUnit = "";
    if (age >= 0) {
      if (age < daysInAMonth) {
        ageUnit = "D";
      } else if (age < daysInTwoYears) {
        age = getDifferenceDays(date, DURATION_MONTHS);
        ageUnit = "M";
      } else {
        age = getDifferenceDays(date, DURATION_YEARS);
        ageUnit = "Y";
      }
    }

    Map map = new HashMap();
    map.put("age", age);
    map.put("ageIn", ageUnit);
    return map;
  }

  /**
   * returns the current age in days / years / months for the requested date string.
   *
   * @param strDate    the str date
   * @param strPattern the str pattern
   * @return the age for date
   * @throws ParseException the parse exception
   */
  public static Map getAgeForDate(String strDate, String strPattern) throws ParseException {

    SimpleDateFormat sdf = new SimpleDateFormat(strPattern);
    Date date = sdf.parse(strDate);
    return getAgeForDate(date);
  }

  /**
   * Gets the age text for date.
   *
   * @param strDate    the str date
   * @param strPattern the str pattern
   * @return the age text for date
   * @throws ParseException the parse exception
   */
  public static String getAgeTextForDate(String strDate, String strPattern) throws ParseException {
    SimpleDateFormat sdf = new SimpleDateFormat(strPattern);
    Date date = sdf.parse(strDate);
    Map map = getAgeForDate(date);
    if (map != null && map.get("age") != null && map.get("ageIn") != null) {
      return map.get("age").toString() + map.get("ageIn").toString();
    }
    return "";
  }

  /**
   * Gets the difference days.
   *
   * @param startDate the start date
   * @param unit      the unit
   * @return the difference days
   */
  public static long getDifferenceDays(Date startDate, String unit) {
    return getDifferenceDays(startDate, new Date(), unit);
  }

  /**
   * Gets the difference days.
   *
   * @param startDate
   *          the start date
   * @param endDate
   *          the end date
   * @param unit
   *          the unit {@value #DURATION_DAYS}, 
   *          {@value #DURATION_MONTHS}, {@value #DURATION_YEARS}
   * @return the difference days
   */
  public static long getDifferenceDays(Date startDate, Date endDate, String unit) {
    if (unit.equals(DURATION_DAYS)) {
      return Days
          .daysBetween(new DateTime(startDate).toLocalDate(), new DateTime(endDate).toLocalDate())
          .getDays();
    } else if (unit.equals(DURATION_MONTHS)) {
      return Months
          .monthsBetween(new DateTime(startDate).toLocalDate(), new DateTime(endDate).toLocalDate())
          .getMonths();
    } else if (unit.equals(DURATION_YEARS)) {
      return Years
          .yearsBetween(new DateTime(startDate).toLocalDate(), new DateTime(endDate).toLocalDate())
          .getYears();
    }
    return 0L;
  }

  /**
   * Gets the age text.
   *
   * @param dateofbirth the dateofbirth
   * @param precise     the precise
   * @return the age text
   */
  public static String getAgeText(java.sql.Date dateofbirth, boolean precise) {
    int[] age = getAgeComponents(dateofbirth);
    int years = age[0];
    int months = age[1];
    int days = age[2];
    String ageInText;

    if (precise) {
      // this is from a date-of-birth, so we can be accurate.
      if (years > 0) {
        // 1 year or more: show years, and also months if < 20 years.
        ageInText = years + "y";
        if (years < 20) {
          ageInText = ageInText + " " + months + "m";
        }

      } else {
        // Less than a year: show months + days or only days if < 2 months.
        if (months < 1) {
          ageInText = days + "d";
        } else {
          ageInText = months + "m " + days + "d";
        }
      }
    } else {
      // this is from expected_dob, so, can cannot accurately say how many months/days etc.
      if (years > 0) {
        ageInText = years + "y";
      } else if (months > 0) {
        ageInText = months + "m";
      } else {
        ageInText = days + "d";
      }
    }
    return ageInText;
  }

  /**
   * Gets the age components.
   *
   * @param dob the dob
   * @return the age components
   */
  public static int[] getAgeComponents(java.util.Date dob) {
    return getAgeComponents(new java.sql.Date(dob.getTime()));
  }

  /**
   * Returns the number of years, months, days given a date of birth.
   *
   * @param dob the dob
   * @return the age components
   */
  public static int[] getAgeComponents(java.sql.Date dob) {
    Calendar calDob = Calendar.getInstance();
    calDob.setTime(dob);

    Calendar calToday = Calendar.getInstance();

    int years = calToday.get(Calendar.YEAR) - calDob.get(Calendar.YEAR);
    int months = calToday.get(Calendar.MONTH) - calDob.get(Calendar.MONTH);
    int days = calToday.get(Calendar.DATE) - calDob.get(Calendar.DATE);
    calToday.add(Calendar.MONTH, -1);
    int maxPrvMonthDays = calToday.getActualMaximum(Calendar.DATE);

    if (days < 0) {
      months = months - 1;
      days = maxPrvMonthDays + days;
    }
    if (months < 0) {
      years = years - 1;
      months = 12 + months;
    }

    // special case: < 2 months: show only total days
    if (years == 0 && months == 1) {
      days += maxPrvMonthDays;
      months = 0;
    }

    return new int[] { years, months, days };
  }

  /**
   * returns expected date(as string) for the requested numberOf days / Months / Years Past or
   * Feature.
   *
   * @param numberOf            the number of
   * @param daysOrMonthsOrYears the days or months or years
   * @param past                the past
   * @param date                the date
   * @return the expected date
   */
  public static Object getExpectedDate(int numberOf, String daysOrMonthsOrYears, boolean past,
      boolean date) {
    Calendar calendar = Calendar.getInstance();
    if (daysOrMonthsOrYears.equals("Y")) {
      if (past) {
        calendar.add(Calendar.YEAR, -numberOf);
        calendar.add(Calendar.MONTH, -6); // If patient says age is 7, assume 7 and half.
      } else {
        calendar.add(Calendar.YEAR, numberOf);
      }
    } else if (daysOrMonthsOrYears.equals("M")) {
      if (past) {
        calendar.add(Calendar.MONTH, -numberOf);
        calendar.add(Calendar.DATE, -15); // if patient says age is 3 months, assume 3 and half
      } else {
        calendar.add(Calendar.MONTH, numberOf);
      }

    } else if (daysOrMonthsOrYears.equals("D")) {
      if (past) {
        calendar.add(Calendar.DATE, -numberOf);
      } else {
        calendar.add(Calendar.DATE, numberOf);
      }
    }
    if (date) {
      return calendar.getTime();
    }
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
    return simpleDateFormat.format(calendar.getTime());
  }

  /**
   * returns expected date(as string) for the requested numberOf days / Months / Years Past or
   * Feature.
   *
   * @param timetsampStr        the timetsamp str
   * @param numberOf            the number of
   * @param daysOrMonthsOrYears the days or months or years
   * @param past                the past
   * @param date                the date
   * @return the expected date time
   */
  public static Object getExpectedDateTime(String timetsampStr, int numberOf,
      String daysOrMonthsOrYears, boolean past, boolean date) {
    Calendar calendar = getCalInstance(timetsampStr);
    if (daysOrMonthsOrYears.equals("Y")) {
      if (past) {
        calendar.add(Calendar.YEAR, -numberOf);
      } else {
        calendar.add(Calendar.YEAR, numberOf);
      }
    } else if (daysOrMonthsOrYears.equals("M")) {
      if (past) {
        calendar.add(Calendar.MONTH, -numberOf);
      } else {
        calendar.add(Calendar.MONTH, numberOf);
      }

    } else if (daysOrMonthsOrYears.equals("D")) {
      if (past) {
        calendar.add(Calendar.DATE, -numberOf);
      } else {
        calendar.add(Calendar.DATE, numberOf);
      }
    } else if (daysOrMonthsOrYears.equals("H")) {
      if (past) {
        calendar.add(Calendar.HOUR, -numberOf);
      } else {
        calendar.add(Calendar.HOUR, numberOf);
      }
    }
    if (date) {
      return calendar.getTime();
    }
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
    return simpleDateFormat.format(calendar.getTime());
  }

  /**
   * Adds the subtract months.
   *
   * @param from  the from
   * @param month the month
   * @return the java.sql. timestamp
   */
  public static java.sql.Timestamp addSubtractMonths(java.sql.Timestamp from, int month) {
    Calendar cal = Calendar.getInstance();
    cal.setTime(from);
    cal.add(Calendar.MONTH, month);
    Date newDate = cal.getTime();
    return new java.sql.Timestamp(newDate.getTime());
  }

  /**
   * Adds the subtract months.
   *
   * @param from  the from
   * @param month the month
   * @return the java.sql. timestamp
   */
  public static java.sql.Timestamp addSubtractMonths(java.sql.Date from, int month) {
    Calendar cal = Calendar.getInstance();
    cal.setTime(from);
    cal.add(Calendar.MONTH, month);
    Date newDate = cal.getTime();
    return new java.sql.Timestamp(newDate.getTime());
  }

  /**
   * Adds the days.
   *
   * @param from      the from
   * @param daysToAdd the days to add
   * @return the java.sql. timestamp
   */
  public static java.sql.Timestamp addDays(java.sql.Timestamp from, int daysToAdd) {
    return new java.sql.Timestamp(from.getTime() + (long) daysToAdd * 24 * 60 * 60 * 1000);
  }

  /**
   * Adds the hours.
   *
   * @param from      the from
   * @param hours hours to add, provide negative value to subtract
   * @return the java.sql. timestamp
   */
  public static java.sql.Timestamp addHours(java.sql.Timestamp from, int hours) {
    Calendar cal = Calendar.getInstance();
    cal.setTime(from);
    cal.add(Calendar.HOUR, hours);

    return new java.sql.Timestamp(cal.getTimeInMillis());
  }

  /**
   * returns the current date in requested pattern.
   *
   * @param strPattern the str pattern
   * @return the string
   */
  public static String currentDate(String strPattern) {
    SimpleDateFormat sdf = new SimpleDateFormat(strPattern);
    return sdf.format(new java.util.Date());
  }

  /**
   * Truncate the date part.
   *
   * @param cal the cal
   */
  public static void dateTrunc(Calendar cal) {
    cal.set(Calendar.HOUR_OF_DAY, 0);
    cal.set(Calendar.MINUTE, 0);
    cal.set(Calendar.SECOND, 0);
    cal.set(Calendar.MILLISECOND, 0);
  }

  /**
   * returns the first day(date) for a requsted week number of year. ex: if u pass week number of
   * year is 16. in 16th week first day(date) will be returned.\
   * 
   * @param weekNumber of a year
   * @return util Date
   */
  public static Date getFirstDayInWeek(int weekNumber) {
    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.WEEK_OF_YEAR, weekNumber);
    calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
    dateTrunc(calendar);
    return calendar.getTime();
  }

  /**
   * returns the last day(date) for a requsted week number of year. ex: if u pass week number of
   * year is 16. in 16th week last day(date) will be returned.
   * 
   * @param weekNumber of a year
   * @return util Date
   */
  public static Date getLastDayInWeek(int weekNumber) {
    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.WEEK_OF_YEAR, weekNumber);
    calendar.setFirstDayOfWeek(Calendar.MONDAY);
    calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
    dateTrunc(calendar);
    return calendar.getTime();
  }

  /**
   * Returns first day of the current month and calendar year as date object.  
   * 
   * @return util Date
   */
  public static Date getFirstDayInMonth() {
    Calendar calendar = Calendar.getInstance();
    return getFirstDayInMonth(calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR));
  }

  /**
   * Returns first day of the given month in current calendar year as date object.  
   * 
   * @param month the month
   * @return util Date
   */
  public static Date getFirstDayInMonth(int month) {
    Calendar calendar = Calendar.getInstance();
    return getFirstDayInMonth(month, calendar.get(Calendar.YEAR));    
  }

  /**
   * Returns first day of the given month and calendar year as date object.  
   * 
   * @param month the month
   * @param year the year
   * @return util Date
   */
  public static Date getFirstDayInMonth(int month, int year) {
    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.YEAR, year);
    calendar.set(Calendar.MONTH, month);
    calendar.set(Calendar.DAY_OF_MONTH, 1);
    dateTrunc(calendar);
    return calendar.getTime();
  }

  /**
   * Returns last day of the current month and calendar year as date object.  
   * 
   * @return util Date
   */
  public static Date getLastDayInMonth() {
    Calendar calendar = Calendar.getInstance();
    return getLastDayInMonth(calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.YEAR));    
  }

  /**
   * Returns last day of the given month in current calendar year as date object.  
   * 
   * @param month the month
   * @return util Date
   */
  public static Date getLastDayInMonth(int month) {
    Calendar calendar = Calendar.getInstance();
    return getLastDayInMonth(month, calendar.get(Calendar.YEAR));    
  }

  /**
   * Returns last day of the given month and calendar year as date object.  
   * 
   * @param month the month
   * @param year the year
   * @return util Date
   */
  public static Date getLastDayInMonth(int month, int year) {
    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.YEAR, year);
    calendar.set(Calendar.DATE, 1);
    calendar.add(Calendar.DATE, -1);
    dateTrunc(calendar);
    return calendar.getTime();
  }

  /**
   * returns the yester day(date).
   * 
   * @return util Date
   */
  public static Date getYesterDay() {
    Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.DATE, -1);
    dateTrunc(calendar);
    return calendar.getTime();
  }

  /**
   * Gets the first day of year.
   * 
   * @param date the date
   * @return the first day of year
   */
  public static Date getFirstDayOfYear(Date date) {

    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    calendar.set(Calendar.DAY_OF_YEAR, 1);
    return calendar.getTime();
  }

  /**
   * Gets the last day of year.
   * 
   * @param date the date
   * @return the last day of year
   */
  public static Date getLastDayOfYear(Date date) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    calendar.add(Calendar.YEAR, 1);
    calendar.set(Calendar.DAY_OF_YEAR, 1);
    calendar.add(Calendar.DAY_OF_YEAR, -1);
    return calendar.getTime();
  }

  /**
   * Gets the first financial day of year.
   * 
   * @param date the date
   * @return the first financial day of year
   */
  public static Date getFirstFinancialDayOfYear(Date date) {
    Calendar cal = Calendar.getInstance();
    cal.setTime(date);
    if (cal.get(cal.MONTH) > 2) {
      cal.set(cal.get(cal.YEAR), 3, 1);
      return cal.getTime();
    } else {
      cal.set(cal.get(cal.YEAR), 2, 31);
      cal.add(Calendar.YEAR, -1);
      cal.set(cal.get(cal.YEAR), 3, 1);
      return cal.getTime();
    }

  }

  /**
   * Gets the last financial day of year.
   * 
   * @param date the date
   * @return the last financial day of year
   */
  public static Date getLastFinancialDayOfYear(Date date) {
    Calendar cal = Calendar.getInstance();
    cal.setTime(date);
    if (cal.get(cal.MONTH) > 2) {
      cal.set(cal.get(cal.YEAR), 3, 1);
      cal.add(Calendar.YEAR, 1);
      cal.set(cal.get(cal.YEAR), 2, 31);
      return cal.getTime();
    } else {
      cal.set(cal.get(cal.YEAR), 2, 31);
      return cal.getTime();
    }
  }

  private static SimpleDateFormat shortDateFormatterWithSlash = new SimpleDateFormat("MM/yy");
  private static SimpleDateFormat DateFormatterWithSlash = new SimpleDateFormat("dd/MM/yyyy");
  private static SimpleDateFormat shortDateFormatterWithHyphen = new SimpleDateFormat("MM-yy");
  private static SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy");
  private static SimpleDateFormat dateFormatterYearAhead = new SimpleDateFormat("yyyy-MM-dd");
  private static SimpleDateFormat dateFormatterWeekDay = new SimpleDateFormat("EEEE d MMMM yyyy");
  private static SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm");
  private static SimpleDateFormat timeFormatterMeridiem = new SimpleDateFormat("hh:mm a");
  private static SimpleDateFormat timeFormatterSecs = new SimpleDateFormat("HH:mm:ss");
  private static SimpleDateFormat timeStampFormatter = new SimpleDateFormat("dd-MM-yyyy HH:mm");
  private static final SimpleDateFormat yearFormatter = new SimpleDateFormat("yyyy");

  /**
   * Gets the iso 8601 timestamp formatter.
   * 
   * @return the iso 8601 timestamp formatter
   */
  public static SimpleDateFormat getIso8601TimestampFormatter() {
    return iso8601TimestampFormatter;
  }

  /**
   * Sets the iso 8601 timestamp formatter.
   * 
   * @param iso8601TimestampFormatter the new iso 8601 timestamp formatter
   */
  public static void setIso8601TimestampFormatter(SimpleDateFormat iso8601TimestampFormatter) {
    DateUtil.iso8601TimestampFormatter = iso8601TimestampFormatter;
  }

  /**
   * Gets the iso 8601 date formatter.
   * 
   * @return the iso 8601 date formatter
   */
  public static SimpleDateFormat getIso8601DateFormatter() {
    return iso8601DateFormatter;
  }

  /**
   * Sets the iso 8601 date formatter.
   * 
   * @param iso8601DateFormatter the new iso 8601 date formatter
   */
  public static void setIso8601DateFormatter(SimpleDateFormat iso8601DateFormatter) {
    DateUtil.iso8601DateFormatter = iso8601DateFormatter;
  }

  /**
   * Gets the iso 8601 time formatter.
   * 
   * @return the iso 8601 time formatter
   */
  public static SimpleDateFormat getIso8601TimeFormatter() {
    return iso8601TimeFormatter;
  }

  /**
   * Sets the iso 8601 time formatter.
   * 
   * @param iso8601TimeFormatter the new iso 8601 time formatter
   */
  public static void setIso8601TimeFormatter(SimpleDateFormat iso8601TimeFormatter) {
    DateUtil.iso8601TimeFormatter = iso8601TimeFormatter;
  }

  private static SimpleDateFormat timeStampFormatterSecs = new SimpleDateFormat(
      "dd-MM-yyyy HH:mm:ss");

  private static SimpleDateFormat sqlDateFormatter = new SimpleDateFormat("yyyy-MM-dd");
  private static SimpleDateFormat sqlTimeFormatter = new SimpleDateFormat("HH:mm");
  private static SimpleDateFormat sqlTimeStampFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");

  private static SimpleDateFormat formatWithoutSpace = new SimpleDateFormat("yyyyMMddHHmm");

  private static SimpleDateFormat iso8601TimestampNoSecFormatter = new SimpleDateFormat(
      "yyyy-MM-dd'T'HH:mmz");
  private static SimpleDateFormat iso8601TimestampFormatter = new SimpleDateFormat(
      "yyyy-MM-dd'T'HH:mm:ssz");
  private static SimpleDateFormat iso8601TimestampWith3FractionsFormatter = new SimpleDateFormat(
      "yyyy-MM-dd'T'HH:mm:ss.SSSz");
  // private static SimpleDateFormat iso8601TimestampWith7FractionsFormatter = new SimpleDateFormat(
  // "yyyy-MM-dd'T'HH:mm:ss.SSSSSSSz" );
  private static SimpleDateFormat iso8601DateFormatter = new SimpleDateFormat("yyyy-MM-dd");
  private static SimpleDateFormat iso8601TimeNoSecFormatter = new SimpleDateFormat("HH:mmz");
  private static SimpleDateFormat iso8601TimeFormatter = new SimpleDateFormat("HH:mm:ssz");
  private static SimpleDateFormat iso8601TimeWith3FractionsFormatter = new SimpleDateFormat(
      "HH:mm:ss.SSSz");
  // private static SimpleDateFormat iso8601TimeWith7FractionsFormatter = new SimpleDateFormat(
  // "HH:mm:ss.SSSSSSSz" );

  /**
   * Gets the short date formatter with slash.
   * 
   * @return the short date formatter with slash
   */
  public SimpleDateFormat getShortDateFormatterWithSlash() {
    return new SimpleDateFormat("MM/yy");
  }

  /**
   * Gets the short date formatter with hyphen.
   * 
   * @return the short date formatter with hyphen
   */
  public SimpleDateFormat getShortDateFormatterWithHyphen() {
    return new SimpleDateFormat("MM-yy");
  }

  /**
   * Gets the date formatter without days.
   *
   * @return the date formatter without days.
   */
  public SimpleDateFormat getDateFormatterWithoutDays() {
    return new SimpleDateFormat("MM-yyyy");
  }

  /**
   * Gets the date formatter.
   *
   * @return the date formatter
   */
  public SimpleDateFormat getDateFormatter() {
    return new SimpleDateFormat("dd-MM-yyyy");
  }

  /**
   * Gets the time formatter.
   *
   * @return the time formatter
   */
  public SimpleDateFormat getTimeFormatter() {
    return new SimpleDateFormat("HH:mm");
  }

  /**
   * Gets the time formatter meridiem.
   *
   * @return the time formatter meridiem
   */
  public SimpleDateFormat getTimeFormatterMeridiem() {
    return new SimpleDateFormat("hh:mm a");
  }

  /**
   * Gets the time formatter secs.
   *
   * @return the time formatter secs
   */
  public SimpleDateFormat getTimeFormatterSecs() {
    return new SimpleDateFormat("HH:mm:ss");
  }

  /**
   * Gets the sql date formatter.
   *
   * @return the sql date formatter
   */
  public SimpleDateFormat getSqlDateFormatter() {
    return new SimpleDateFormat("yyyy-MM-dd");
  }

  /**
   * Gets the sql time formatter.
   *
   * @return the sql time formatter
   */
  public SimpleDateFormat getSqlTimeFormatter() {
    return new SimpleDateFormat("HH:mm");
  }

  /**
   * Gets the sql time stamp formatter.
   *
   * @return the sql time stamp formatter
   */
  public SimpleDateFormat getSqlTimeStampFormatter() {
    return new SimpleDateFormat("yyyy-MM-dd HH:mm");
  }

  /**
   * Gets the sql time stamp formatter with seconds.
   *
   * @return the sql time stamp formatter
   */
  public SimpleDateFormat getSqlTimeStampFormatterSecs() {
    return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
  }

  /**
   * Gets the format without space.
   *
   * @return the format without space
   */
  public SimpleDateFormat getFormatWithoutSpace() {
    return new SimpleDateFormat("yyyyMMddHHmm");
  }

  /**
   * Gets the time stamp formatter secs.
   *
   * @return the time stamp formatter secs
   */
  public SimpleDateFormat getTimeStampFormatterSecs() {
    return new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
  }

  /**
   * Gets the time stamp formatter.
   *
   * @return the time stamp formatter
   */
  public SimpleDateFormat getTimeStampFormatter() {
    return new SimpleDateFormat("dd-MM-yyyy HH:mm");
  }

  /**
   * Parses the ISO 8601 timestamp.
   *
   * @param ts timestamp representated as string
   * @return java.sql.timestamp object
   * @throws ParseException the parse exception
   */
  public static java.sql.Timestamp parseTimestamp8601(String ts) {
    return new java.sql.Timestamp(DatatypeConverter.parseDateTime(ts).getTime().getTime());
  }

  /**
   * Parses the ISO 8601 date.
   *
   * @param ts timestamp representated as string
   * @return java.sql.date object
   * @throws ParseException the parse exception
   */
  public static java.sql.Date parseDate8601(String ts) {
    return new java.sql.Date(DatatypeConverter.parseDateTime(ts).getTime().getTime());
  }

  /**
   * Parses the ISO 8601 timestamp.
   *
   * @param timeStamp the ts
   * @return the java.sql. timestamp
   * @throws ParseException the parse exception
   */
  public static Timestamp parseIso8601Timestamp(String timeStamp) throws java.text.ParseException {
    if (timeStamp == null) {
      return null;
    }
    boolean withSeconds = false;
    if (timeStamp.endsWith("Z")) {
      timeStamp = timeStamp.substring(0, timeStamp.length() - 1);
      if (timeStamp.length() > 16) {
        withSeconds = true;
      } else if (timeStamp.length() == 16) {
        withSeconds = false;
      } else {
        return null;
      }
    } else {
      return null;
    }
    if (withSeconds) {
      // milliseconds formatting - ignore the milliseconds
      if (timeStamp.contains(".")) {
        int dotIndex = timeStamp.indexOf(".");
        timeStamp = timeStamp.substring(0, dotIndex);
      }
      timeStamp = timeStamp + "GMT-00:00";
      iso8601TimestampFormatter.setLenient(false);
      return new Timestamp(iso8601TimestampFormatter.parse(timeStamp).getTime());
    } else {
      timeStamp = timeStamp + "GMT-00:00";
      iso8601TimestampNoSecFormatter.setLenient(false);
      return new Timestamp(iso8601TimestampNoSecFormatter.parse(timeStamp).getTime());
    }
  }

  /**
   * Parses the ISO 8601 date.
   *
   * @param date the date
   * @return the java.sql. date
   * @throws ParseException the parse exception
   */
  public static java.sql.Date parseIso8601Date(String date) throws java.text.ParseException {
    if (date == null) {
      return null;
    }
    iso8601DateFormatter.setLenient(false);
    return new java.sql.Date(iso8601DateFormatter.parse(date).getTime());
  }

  /**
   * Parses the ISO 8601 time.
   *
   * @param time the time
   * @return the java.sql. time
   * @throws ParseException the parse exception
   */
  public static java.sql.Time parseIso8601Time(String time) throws java.text.ParseException {
    if (time == null) {
      return null;
    }
    boolean withSeconds = false;
    if (time.endsWith("Z")) {
      time = time.substring(0, time.length() - 1);
      if (time.length() > 5) {
        withSeconds = true;
      } else if (time.length() == 5) {
        withSeconds = false;
      } else {
        return null;
      }

    } else { // we do not support it currently
      /*
       * int inset = 6; String s0 = time.substring( 0, time.length() - inset ); String s1 =
       * time.substring( time.length() - inset, time.length() ); time = s0 + "GMT" + s1;
       */
      return null;
    }
    if (withSeconds) {
      // milliseconds formatting - ignore the milliseconds
      if (time.contains(".")) {
        int dotIndex = time.indexOf(".");
        time = time.substring(0, dotIndex);
        /*
         * String[] fractions = time.split(Pattern.quote(".")); fractions =
         * fractions[1].split("GMT"); if(fractions[0].length() == 3) {
         * iso8601TimeWith3FractionsFormatter.setLenient(false); return new
         * java.sql.Time(iso8601TimeWith3FractionsFormatter.parse(time).getTime()); } else
         * if(fractions[0].length() == 7){ return new
         * java.sql.Time(iso8601TimeWith7FractionsFormatter.parse(time).getTime()); } else return
         * null;
         */
      }
      time = time + "GMT-00:00";
      iso8601TimeFormatter.setLenient(false);
      return new java.sql.Time(iso8601TimeFormatter.parse(time).getTime());
    } else {
      time = time + "GMT-00:00";
      iso8601TimeNoSecFormatter.setLenient(false);
      return new java.sql.Time(iso8601TimeNoSecFormatter.parse(time).getTime());
    }
  }

  /**
   * Gets the current ISO 8601 timestamp.
   *
   * @return the current ISO 8601 timestamp
   */
  public static String getCurrentIso8601Timestamp() {
    java.sql.Timestamp ts = new java.sql.Timestamp(System.currentTimeMillis());
    TimeZone tz = TimeZone.getTimeZone("UTC");
    iso8601TimestampFormatter.setTimeZone(tz);
    String output = iso8601TimestampFormatter.format(ts);
    output = output.replaceAll("UTC", "Z");
    return output;
  }

  /**
   * Gets the current ISO 8601 timestamp.
   *
   * @return the current ISO 8601 timestamp
   */
  public static String getCurrentISO8601TimestampMillis() {
    java.sql.Timestamp ts = new java.sql.Timestamp(System.currentTimeMillis());
    TimeZone tz = TimeZone.getTimeZone("UTC");
    iso8601TimestampWith3FractionsFormatter.setLenient(Boolean.FALSE);
    iso8601TimestampWith3FractionsFormatter.setTimeZone(tz);
    String output = iso8601TimestampWith3FractionsFormatter.format(ts);
    output = output.replaceAll("UTC", "Z");
    return output;
  }

  /**
   * Format ISO 8601 timestamp.
   *
   * @param date the date
   * @return the string
   */
  public static String formatIso8601Timestamp(java.util.Date date) {
    if (date == null) {
      return null;
    }
    TimeZone tz = TimeZone.getTimeZone("UTC");
    iso8601TimestampFormatter.setTimeZone(tz);
    String output = iso8601TimestampFormatter.format(date);
    output = output.replaceAll("UTC", "Z");
    return output;
  }

  /**
   * Format ISO 8601 timestamp no sec.
   *
   * @param date the date
   * @return the string
   */
  public static String formatIso8601TimestampNoSec(java.util.Date date) {
    if (date == null) {
      return null;
    }
    TimeZone tz = TimeZone.getTimeZone("UTC");
    iso8601TimestampNoSecFormatter.setTimeZone(tz);
    String output = iso8601TimestampNoSecFormatter.format(date);
    output = output.replaceAll("UTC", "Z");
    return output;
  }

  /**
   * Format ISO 8601 date.
   *
   * @param date the date
   * @return the string
   */
  public static String formatIso8601Date(java.sql.Date date) {
    if (date == null) {
      return null;
    }
    return iso8601DateFormatter.format(date);
  }

  /**
   * Format ISO 8601 time.
   *
   * @param time the time
   * @return the string
   */
  public static String formatIso8601Time(java.sql.Time time) {
    if (time == null) {
      return null;
    }
    TimeZone tz = TimeZone.getTimeZone("UTC");
    iso8601TimeFormatter.setTimeZone(tz);
    String output = iso8601TimeFormatter.format(time);
    output = output.replaceAll("UTC", "Z");
    return output;
  }

  /**
   * Parse a string representing a date to return a java.sql.Date object (only the date part). Also
   * does intelligent parsing for special strings "today", "td", "yesterday" and "pd"
   *
   * @param dateStr the date str
   * @return the java.sql. date
   * @throws ParseException the parse exception
   */
  public static java.sql.Date parseDate(String dateStr) throws java.text.ParseException {
    if ((dateStr != null) && !dateStr.equals("")) {
      if (dateStr.equalsIgnoreCase("${td}") || dateStr.equalsIgnoreCase("today")) {
        return getCurrentDate();
      } else if (dateStr.equalsIgnoreCase("${pd}") || dateStr.equalsIgnoreCase("yesterday")) {
        return new java.sql.Date(getYesterDay().getTime());
      }
      java.util.Date dt = dateFormatter.parse(dateStr);
      return new java.sql.Date(dt.getTime());
    } else {
      return null;
    }
  }

  /**
   * Parse a string representing a date to return a java.sql.Date object (only the date part). For
   * credit card, or dates which have month-year format, use type as "short"
   *
   * @param dateStr       the date str
   * @param dateSeparator the date separator
   * @param type          the type
   * @return the java.sql. date
   * @throws ParseException the parse exception
   */
  public static java.sql.Date parseDate(String dateStr, String dateSeparator, String type)
      throws java.text.ParseException {
    if (type != null && type.equals("short")) {
      java.util.Date dt = null;
      if (dateSeparator != null) {
        if (dateSeparator.equals("/")) {
          dt = shortDateFormatterWithSlash.parse(dateStr);
        } else if (dateSeparator.equals("-")) {
          dt = shortDateFormatterWithHyphen.parse(dateStr);
        } else {
          dt = dateFormatter.parse(dateStr);
        }
      } else {
        dt = dateFormatter.parse(dateStr);
      }
      return new java.sql.Date(dt.getTime());
    } else {
      return parseDate(dateStr);
    }
  }

  /**
   * Parse a string representing a time to return a java.sql.Time object (only the time part). The
   * time can optionally contain seconds. this method is deprecated use parseTheTime() as
   * replacement
   *
   * @param timeStr the time str
   * @return the java.sql. time
   * @throws ParseException the parse exception
   */
  @Deprecated
  public static java.sql.Time parseTime(String timeStr) throws java.text.ParseException {
    if ((timeStr == null) || timeStr.equals("")) {
      return null;
    }

    SimpleDateFormat fmt;
    if (timeStr.length() > 5) {
      fmt = timeFormatterSecs;
    } else {
      fmt = timeFormatter;
    }

    java.util.Date dateTime = fmt.parse(timeStr);
    return new java.sql.Time(dateTime.getTime());
  }

  /**
   * Parse a string representing a time to return a java.sql.Time object (only the time part). The
   * time can optionally contain seconds.
   *
   * @param timeStr the time str
   * @return the java.sql. time
   * @throws ParseException the parse exception
   */
  public java.sql.Time parseTheTime(String timeStr) throws java.text.ParseException {
    if ((timeStr == null) || timeStr.equals("")) {
      return null;
    }

    SimpleDateFormat fmt = null;
    if (timeStr.length() > 5) {
      fmt = new SimpleDateFormat("HH:mm:ss");
    } else {
      fmt = new SimpleDateFormat("HH:mm");
    }
    java.util.Date dateTime = fmt.parse(timeStr);
    return new java.sql.Time(dateTime.getTime());
  }

  public static String formatSqlTimestamp(java.util.Date dt) {
    return timeStampFormatterSecs.format(dt);
  }

  /**
   * Parse a string representing a time to return a java.sql.Timestamp object The time part is
   * optional, and can optionally contain seconds. We also intelligently parse "today" etc. as the
   * date part, eg, "today 00:00" this method is deprecated use parseTheTimestamp() as replacement
   * 
   * @param timestampStr the timestamp str
   * @return the java.sql. timestamp
   * @throws ParseException the parse exception
   */
  @Deprecated
  public static java.sql.Timestamp parseTimestamp(String timestampStr)
      throws java.text.ParseException {

    if ((timestampStr == null) || timestampStr.equals("") || timestampStr.equals(" ")) {
      return null;
    }

    if (timestampStr.matches("^(today|td).*")) {
      logger.debug("Doing today replacement.");
      String todayStr = dateFormatter.format(new java.util.Date());
      timestampStr = timestampStr.replaceAll("(today|td)", todayStr);
      logger.debug("New timestamp str: " + timestampStr);

    } else if (timestampStr.matches("^(yesterday|pd).*")) {
      String yesterdayStr = dateFormatter.format(getYesterDay());
      timestampStr = timestampStr.replaceAll("(yesterday|pd)", yesterdayStr);
      logger.debug("New timestamp str: " + timestampStr);
    }

    SimpleDateFormat fmt;
    if (timestampStr.length() <= 11) {
      logger.debug("Using date formatter for timestamp");
      fmt = dateFormatter;
    } else if (timestampStr.length() > 16) {
      logger.debug("Using seconds formatter for timestamp");
      fmt = timeStampFormatterSecs;
    } else {
      logger.debug("Using minutes formatter for timestamp");
      fmt = timeStampFormatter;
    }

    java.util.Date dateTime = fmt.parse(timestampStr);
    return new java.sql.Timestamp(dateTime.getTime());
  }

  /**
   * Parses the timestamp.
   *
   * @param dateStr the date str
   * @param timeStr the time str
   * @return the java.sql. timestamp
   * @throws ParseException the parse exception
   */
  public static java.sql.Timestamp parseTimestamp(String dateStr, String timeStr)
      throws java.text.ParseException {

    if ((dateStr == null) || dateStr.equals("")) {
      return null;
    }

    if (timeStr == null) {
      timeStr = "";
    }

    return parseTimestamp(dateStr + " " + timeStr);
  }

  /**
   * Gets the current timestamp.
   *
   * @return the current timestamp
   */
  public static java.sql.Timestamp getCurrentTimestamp() {
    return new java.sql.Timestamp(new Date().getTime());
  }

  /**
   * Gets the current date.
   *
   * @return the current date
   */
  public static java.sql.Date getCurrentDate() {
    Calendar calendar = Calendar.getInstance();
    dateTrunc(calendar);
    return new java.sql.Date(calendar.getTime().getTime());
  }

  /**
   * Gets the current time.
   *
   * @return the current time
   */
  public static java.sql.Time getCurrentTime() {
    return new java.sql.Time(new Date().getTime());
  }

  /**
   * Format date.
   *
   * @param dt the dt
   * @return the string
   */
  public static String formatDate(java.util.Date dt) {
    if (dt == null) {
      return null;
    }
    return dateFormatter.format(dt);
  }

  /**
   * Format date to year ahead.
   *
   * @param dt the dt
   * @return the string
   */
  public static String formatDateToYearAhead(java.util.Date dt) {
    if (dt == null) {
      return null;
    }
    return dateFormatterYearAhead.format(dt);
  }

  /**
   * Format date to week day.
   *
   * @param dt the dt
   * @return the string
   */
  public static String formatDateToWeekDay(java.util.Date dt) {
    if (dt == null) {
      return null;
    }
    return dateFormatterWeekDay.format(dt);
  }

  /**
   * Format time meridiem.
   *
   * @param dt the dt
   * @return the string
   */
  public static String formatTimeMeridiem(java.sql.Time dt) {
    if (dt == null) {
      return null;
    }
    return timeFormatterMeridiem.format(dt);
  }

  /**
   * Format time.
   *
   * @param dt the dt
   * @return the string
   */
  public static String formatTime(java.sql.Timestamp dt) {
    if (dt == null) {
      return null;
    }
    return timeFormatter.format(dt);
  }

  /**
   * Format S ql time.
   *
   * @param dt the dt
   * @return the string
   */
  public static String formatSQlTime(java.sql.Time dt) {
    if (dt == null) {
      return null;
    }
    return sqlTimeFormatter.format(dt);
  }

  /**
   * Format timestamp.
   *
   * @param dt the dt
   * @return the string
   */
  public static String formatTimestamp(java.util.Date dt) {
    if (dt == null) {
      return null;
    }
    return timeStampFormatter.format(dt);
  }

  /**
   * Format timestamp with slash.
   *
   * @param dt the dt
   * @return the string
   */
  public static String formatTimestampWithSlash(java.sql.Timestamp dt) {
    if (dt == null) {
      return null;
    }
    return DateFormatterWithSlash.format(dt);
  }

  /**
   * Gets a list of date strings within the given date range, and the type (month/week/day). Used by
   * the trend report builder to predict what values we'll get.
   *
   * @param from the from
   * @param to   the to
   * @param type the type
   * @return the dates in range
   */
  public static List<String> getDatesInRange(java.util.Date from, java.util.Date to, String type) {
    Calendar cal = Calendar.getInstance();
    cal.setTime(from);
    Calendar last = Calendar.getInstance();
    last.setTime(to);

    List<String> dates = new ArrayList<String>();

    if (type.equals("month")) {
      int firstDay = cal.getActualMinimum(cal.DAY_OF_MONTH);
      cal.set(cal.DAY_OF_MONTH, firstDay);

      while (!cal.after(last)) {
        SimpleDateFormat formatter = new SimpleDateFormat("MMM yyyy");
        dates.add(formatter.format(cal.getTime()));
        cal.add(cal.MONTH, 1);
      }
    } else if (type.equals("week")) {
      if (cal.get(cal.DAY_OF_WEEK) == cal.SUNDAY) {
        cal.add(cal.DATE, -6);
      } else {
        cal.add(cal.DATE, cal.MONDAY - cal.get(cal.DAY_OF_WEEK));
      }
      while (!cal.after(last)) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        dates.add(formatter.format(cal.getTime()));
        cal.add(cal.DATE, 7);
      }
    } else if (type.equals("day")) {
      while (!cal.after(last)) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        dates.add(formatter.format(cal.getTime()));
        cal.add(cal.DATE, 1);
      }
    }
    return dates;
  }

  /**
   * Gets a date range (array of two dates) given the type, based on today's date.
   *
   * @param type the type
   * @return the date range
   * @throws ParseException the parse exception
   * @throws SQLException   the SQL exception
   */
  public static java.sql.Date[] getDateRange(String type)
      throws java.text.ParseException, SQLException {
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
      return getPrevMonthsDateRange();
    } else if (type.equals("ty")) {
      return getThisYearsDateRange();
    } else if (type.equals("py")) {
      return getPrevYearsDateRange();
    } else if (type.equals("tf")) {
      return getThisFinYearsDateRange();
    } else if (type.equals("pf")) {
      return getPrevFinYearsDateRange();
    } else {
      return getTodaysDateRange();
    }
  }

  /**
   * Gets the todays date range.
   *
   * @return the todays date range
   * @throws ParseException the parse exception
   */
  public static java.sql.Date[] getTodaysDateRange() throws java.text.ParseException {
    java.sql.Date td = getCurrentDate();
    java.sql.Date fd = getCurrentDate();
    java.sql.Date[] dateArray = { fd, td };
    return dateArray;

  }

  /**
   * Gets the yesterdays date range.
   *
   * @return the yesterdays date range
   * @throws ParseException the parse exception
   */
  public static java.sql.Date[] getYesterdaysDateRange() throws java.text.ParseException {
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.DATE, -1);
    java.sql.Date td = new java.sql.Date(cal.getTime().getTime());
    java.sql.Date fd = new java.sql.Date(cal.getTime().getTime());
    java.sql.Date[] dateArray = { fd, td };
    return dateArray;
  }

  /**
   * Gets the this weeks date range.
   *
   * @return the this weeks date range
   * @throws ParseException the parse exception
   */
  public static java.sql.Date[] getThisWeeksDateRange() throws java.text.ParseException {
    Calendar cal = Calendar.getInstance();
    java.sql.Date fd = new java.sql.Date((cal.getTime()).getTime());
    int dayOfWeek = cal.DAY_OF_WEEK;
    dayOfWeek = dayOfWeek == 1 ? 7 : dayOfWeek;
    cal.add(Calendar.DATE, +dayOfWeek);
    java.sql.Date td = new java.sql.Date((cal.getTime()).getTime());
    java.sql.Date[] dateArray = { fd, td };
    return dateArray;

  }

  /**
   * Gets the previous weeks date range.
   *
   * @return the previous weeks date range
   * @throws ParseException the parse exception
   */
  public static java.sql.Date[] getPreviousWeeksDateRange() throws java.text.ParseException {
    Calendar cal = Calendar.getInstance();
    java.sql.Date td = new java.sql.Date((cal.getTime()).getTime());
    int dayOfWeek = cal.DAY_OF_WEEK;
    dayOfWeek = dayOfWeek == 1 ? 7 : dayOfWeek;
    cal.add(Calendar.DATE, -dayOfWeek);
    java.sql.Date fd = new java.sql.Date((cal.getTime()).getTime());
    java.sql.Date[] dateArray = { fd, td };
    return dateArray;

  }

  /**
   * Gets the this months date range.
   *
   * @return the this months date range
   * @throws ParseException the parse exception
   */
  public static java.sql.Date[] getThisMonthsDateRange() throws java.text.ParseException {
    Calendar cal = Calendar.getInstance();
    int maxday = cal.getActualMaximum(cal.DAY_OF_MONTH);
    int minday = cal.getActualMinimum(cal.DAY_OF_MONTH);
    cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), minday);
    java.sql.Date fd = new java.sql.Date((cal.getTime()).getTime());
    cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), maxday);
    java.sql.Date td = new java.sql.Date((cal.getTime()).getTime());
    java.sql.Date[] dateArray = { fd, td };
    return dateArray;

  }

  /**
   * Gets the prev months date range.
   *
   * @return the prev months date range
   * @throws ParseException the parse exception
   */
  public static java.sql.Date[] getPrevMonthsDateRange() throws java.text.ParseException {
    Calendar calz = Calendar.getInstance();
    calz.add(Calendar.MONTH, -1);
    int maxday = calz.getActualMaximum(calz.DAY_OF_MONTH);
    int minday = calz.getActualMinimum(calz.DAY_OF_MONTH);
    calz.set(calz.get(calz.YEAR), calz.get(Calendar.MONTH), minday);
    java.sql.Date fd = new java.sql.Date((calz.getTime()).getTime());
    calz.set(calz.get(calz.YEAR), calz.get(Calendar.MONTH), maxday);
    java.sql.Date td = new java.sql.Date((calz.getTime()).getTime());
    java.sql.Date[] dateArray = { fd, td };
    return dateArray;

  }

  /**
   * Gets the this years date range.
   *
   * @return the this years date range
   * @throws ParseException the parse exception
   */
  public static java.sql.Date[] getThisYearsDateRange() throws java.text.ParseException {
    Calendar cal = Calendar.getInstance();
    cal.set(cal.get(Calendar.YEAR), 0, 1);
    java.sql.Date fd = new java.sql.Date((cal.getTime()).getTime());
    cal.set(cal.get(Calendar.YEAR), 11, 1);
    int maxday = cal.getActualMaximum((cal.DAY_OF_MONTH));
    cal.set(cal.get(Calendar.YEAR), 11, maxday);
    java.sql.Date td = new java.sql.Date((cal.getTime()).getTime());
    java.sql.Date[] dateArray = { fd, td };
    return dateArray;

  }

  /**
   * Gets the prev years date range.
   *
   * @return the prev years date range
   * @throws ParseException the parse exception
   */
  public static java.sql.Date[] getPrevYearsDateRange() throws java.text.ParseException {
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.YEAR, -1);
    cal.set(cal.get(cal.YEAR), 0, 1);
    java.sql.Date fd = new java.sql.Date((cal.getTime()).getTime());
    cal.set(cal.get(cal.YEAR), 11, 1);
    int maxday = cal.getActualMaximum((cal.DAY_OF_MONTH));
    cal.set(cal.get(cal.YEAR), 11, maxday);
    java.sql.Date td = new java.sql.Date((cal.getTime()).getTime());
    java.sql.Date[] dateArray = { fd, td };
    return dateArray;

  }

  /**
   * Gets the this fin years date range.
   *
   * @return the this fin years date range
   * @throws ParseException the parse exception
   * @throws SQLException   the SQL exception
   */
  public static java.sql.Date[] getThisFinYearsDateRange()
      throws java.text.ParseException, SQLException {

    Calendar cal = Calendar.getInstance();
    int startMonth = (Integer) GenericPreferencesDAO.getAllPrefs().get("fin_year_start_month") - 1;
    int endMonth = (Integer) GenericPreferencesDAO.getAllPrefs().get("fin_year_end_month") - 1;
    java.sql.Date fromDate;
    java.sql.Date toDate;
    if (startMonth > 0) {
      if (cal.get(cal.MONTH) > endMonth) {
        cal.set(cal.get(cal.YEAR), startMonth, 1);
        fromDate = new java.sql.Date((cal.getTime()).getTime());
        cal.add(Calendar.YEAR, 1);
        cal.add(Calendar.MONTH, -1);
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        // cal.set(cal.get(cal.YEAR), en_month, 31);
        toDate = new java.sql.Date((cal.getTime()).getTime());
      } else {
        // cal.set(cal.get(cal.YEAR), en_month, 31);
        cal.set(Calendar.MONTH, endMonth);
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        toDate = new java.sql.Date((cal.getTime()).getTime());
        cal.add(Calendar.YEAR, -1);
        cal.set(cal.get(cal.YEAR), startMonth, 1);
        fromDate = new java.sql.Date((cal.getTime()).getTime());
      }
    } else {
      cal.set(cal.get(cal.YEAR), startMonth, 1);
      fromDate = new java.sql.Date((cal.getTime()).getTime());

      cal.set(cal.get(cal.YEAR), endMonth, 31);
      toDate = new java.sql.Date((cal.getTime()).getTime());
    }
    java.sql.Date[] dateArray = { fromDate, toDate };
    return dateArray;
  }

  /**
   * Gets the prev fin years date range.
   *
   * @return the prev fin years date range
   * @throws ParseException the parse exception
   * @throws SQLException   the SQL exception
   */
  public static java.sql.Date[] getPrevFinYearsDateRange()
      throws java.text.ParseException, SQLException {

    Calendar cal = Calendar.getInstance();
    int startMonth = (Integer) GenericPreferencesDAO.getAllPrefs().get("fin_year_start_month") - 1;
    int endMonth = (Integer) GenericPreferencesDAO.getAllPrefs().get("fin_year_end_month") - 1;
    java.sql.Date fromDate;
    java.sql.Date toDate;
    if (startMonth > 0) {
      if (cal.get(cal.MONTH) > endMonth) {
        // cal.set(cal.get(cal.YEAR), en_month, 31);
        cal.set(Calendar.MONTH, endMonth);
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        toDate = new java.sql.Date((cal.getTime()).getTime());
        cal.add(Calendar.YEAR, -1);
        cal.set(cal.get(cal.YEAR), startMonth, 1);
        fromDate = new java.sql.Date((cal.getTime()).getTime());
      } else {
        cal.add(Calendar.YEAR, -1);
        // cal.set(cal.get(cal.YEAR), en_month, 31);
        cal.set(Calendar.MONTH, endMonth);
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        toDate = new java.sql.Date((cal.getTime()).getTime());
        cal.add(Calendar.YEAR, -1);
        cal.set(cal.get(cal.YEAR), startMonth, 1);
        fromDate = new java.sql.Date((cal.getTime()).getTime());
      }
    } else {
      cal.add(Calendar.YEAR, -1);
      cal.set(cal.get(cal.YEAR), startMonth, 1);
      fromDate = new java.sql.Date((cal.getTime()).getTime());

      cal.set(cal.get(cal.YEAR), endMonth, 31);
      toDate = new java.sql.Date((cal.getTime()).getTime());
    }
    java.sql.Date[] dateArray = { fromDate, toDate };
    return dateArray;
  }

  /**
   * Gets the custom date fin years date range.
   *
   * @return the custom date fin years date range
   * @throws ParseException the parse exception
   * @throws SQLException   the SQL exception
   */
  public static java.sql.Date[] getCustomDateFinYearsDateRange(java.util.Date customDate)
      throws SQLException {

    Calendar cal = Calendar.getInstance();
    cal.setTime(customDate);

    int startMonth = (Integer) GenericPreferencesDAO.getAllPrefs().get("fin_year_start_month") - 1;
    int endMonth = (Integer) GenericPreferencesDAO.getAllPrefs().get("fin_year_end_month") - 1;
    java.sql.Date fromDate;
    java.sql.Date toDate;
    if (startMonth > 0) {
      if (cal.get(cal.MONTH) > endMonth) {
        cal.set(cal.get(cal.YEAR), startMonth, 1);
        fromDate = new java.sql.Date((cal.getTime()).getTime());
        cal.add(Calendar.YEAR, 1);
        cal.add(Calendar.MONTH, -1);
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        toDate = new java.sql.Date((cal.getTime()).getTime());
      } else {
        cal.set(Calendar.MONTH, endMonth);
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        toDate = new java.sql.Date((cal.getTime()).getTime());
        cal.add(Calendar.YEAR, -1);
        cal.set(cal.get(cal.YEAR), startMonth, 1);
        fromDate = new java.sql.Date((cal.getTime()).getTime());
      }
    } else {
      cal.set(cal.get(cal.YEAR), startMonth, 1);
      fromDate = new java.sql.Date((cal.getTime()).getTime());

      cal.set(cal.get(cal.YEAR), endMonth, 31);
      toDate = new java.sql.Date((cal.getTime()).getTime());
    }
    java.sql.Date[] dateArray = {fromDate, toDate};
    return dateArray;
  }

  /**
   * The following returns number of days and number of hours given from and to times. Hours are
   * rounded (half-up) instead of any-part-thereof calculation. Thus, 61 minutes is 1 hour, 89
   * minutes = 1 hour, 90 minuts = 2 hours, 23:30 is 1 day.
   *
   * @param from the from
   * @param to   the to
   * @return the days hours
   */
  public static int[] getDaysHours(java.sql.Timestamp from, java.sql.Timestamp to) {
    long timeDiff = to.getTime() - from.getTime(); // milliseconds
    int minutes = (int) (timeDiff / 60 / 1000);

    int totalHours = minutes / 60 + ((minutes % 60 > 29) ? 1 : 0);
    int days = totalHours / 24;
    int hours = totalHours % 24;
    return new int[] { days, hours };
  }

  /**
   * The original method will do a round off for 29 mins But this will do it only roundOff is true.
   *
   * @param from     the from
   * @param to       the to
   * @param roundOff the round off
   * @return the days hours
   */
  public static int[] getDaysHours(java.sql.Timestamp from, java.sql.Timestamp to,
      boolean roundOff) {
    long timeDiff = to.getTime() - from.getTime(); // milliseconds
    int minutes = (int) (timeDiff / 60 / 1000);

    int totalHours = minutes / 60 + ((minutes % 60 > (roundOff ? 29 : 0)) ? 1 : 0);
    int days = totalHours / 24;
    int hours = totalHours % 24;
    return new int[] { days, hours };
  }

  /**
   * The following returns number of days and number of hours given days including fraction of day
   * as decimal. Ex: for 2.5 days the days are 2 and hours are 12
   *
   * @param daysFraction the days fraction
   * @return the days hours
   */
  public static int[] getDaysHours(BigDecimal daysFraction) {

    int days = (int) (daysFraction.intValue());
    float hrs = daysFraction.floatValue() % 1;
    int hours = new BigDecimal(hrs * 24).intValue();
    return new int[] { days, hours };
  }

  /**
   * Gets the hours.
   *
   * @param from the from
   * @param to   the to
   * @return the hours
   */
  public static int getHours(java.sql.Timestamp from, java.sql.Timestamp to) {
    long timeDiff = to.getTime() - from.getTime(); // milliseconds
    int minutes = (int) (timeDiff / 60 / 1000);

    return minutes / 60 + ((minutes % 60 > 29) ? 1 : 0);
  }

  /**
   * The original method will do a round off for 29 mins But this will do it only roundOff is true.
   *
   * @param from     the from
   * @param to       the to
   * @param roundOff the round off
   * @return the hours
   */
  public static int getHours(java.sql.Timestamp from, java.sql.Timestamp to, boolean roundOff) {
    long timeDiff = to.getTime() - from.getTime(); // milliseconds
    int minutes = (int) (timeDiff / 60 / 1000);

    return minutes / 60 + ((minutes % 60 > (roundOff ? 29 : 0)) ? 1 : 0);
  }

  /**
   * Gets the date part.
   *
   * @param from the from
   * @return the date part
   */
  public static java.sql.Date getDatePart(java.sql.Timestamp from) {
    return new java.sql.Date(from.getTime());
  }

  /**
   * Gets the time part.
   *
   * @param from the from
   * @return the time part
   */
  public static java.sql.Time getTimePart(java.sql.Timestamp from) {
    return new java.sql.Time(from.getTime());
  }

  /**
   * Gets the cal instance.
   *
   * @param timestamp the timestamp
   * @return the cal instance
   */
  public static Calendar getCalInstance(String timestamp) {
    Calendar currentDate = GregorianCalendar.getInstance();
    String date = timestamp.split(" ")[0];
    String time = timestamp.split(" ")[1];
    currentDate.set(Integer.parseInt(date.split("-")[0]), // year
        Integer.parseInt(date.split("-")[1]) - 1, // month
        Integer.parseInt(date.split("-")[2]), // day
        Integer.parseInt(time.split(":")[0]), // hr
        Integer.parseInt(time.split(":")[1]), // min
        0);// sec
    return currentDate;

  }

  /**
   * Convert two digit year.
   *
   * @param year  the year
   * @param valid the valid
   * @return the int
   */
  public static int convertTwoDigitYear(int year, String valid) {
    Calendar cal = Calendar.getInstance();
    if (year > 99) {
      return year;
    }

    int thisYear = cal.get(Calendar.YEAR); // say 2008
    int century = (int) Math.floor(thisYear / 100) * 100; // say 2000

    if (valid.equals("past")) {
      if (century + year > thisYear) { // 2000+10 > 2008
        year += century - 100; // 10 + 2000 - 100 = 1910
      } else { // 2000+7 < 2008
        year += century; // 7 + 2000 = 2007
      }
    } else if (valid.equals("future")) {
      if (century + year < thisYear) { // 2000+7 < 2008
        year += century + 100; // 2107
      } else { // 2000+10 > 2008
        year += century; // 2010
      }
    } else {
      if (year >= 50) { // say 75
        year += century - 100; // 1975
      } else { // say 25
        year += century; // 2025
      }
    }
    return year;
  }

  /**
   * Parses the the timestamp; Same as parseTimestamp() but non-static.
   *
   * @param timestampStr the timestamp str
   * @return the java.sql. timestamp
   * @throws ParseException the parse exception
   */
  public java.sql.Timestamp parseTheTimestamp(String timestampStr) throws java.text.ParseException {

    if ((timestampStr == null) || timestampStr.equals("") || timestampStr.equals(" ")) {
      return null;
    }

    if (timestampStr.matches("^(today|td).*")) {
      logger.debug("Doing today replacement.");
      String todayStr = getDateFormatter().format(new java.util.Date());
      timestampStr = timestampStr.replaceAll("(today|td)", todayStr);
      logger.debug("New timestamp str: " + timestampStr);

    } else if (timestampStr.matches("^(yesterday|pd).*")) {
      String yesterdayStr = dateFormatter.format(getYesterDay());
      timestampStr = timestampStr.replaceAll("(yesterday|pd)", yesterdayStr);
      logger.debug("New timestamp str: " + timestampStr);
    }

    SimpleDateFormat fmt;
    if (timestampStr.length() <= 11) {
      logger.debug("Using date formatter for timestamp");
      fmt = getDateFormatter();
    } else if (timestampStr.length() > 16) {
      logger.debug("Using seconds formatter for timestamp");
      fmt = getTimeStampFormatterSecs();
    } else {
      logger.debug("Using minutes formatter for timestamp");
      fmt = getTimeStampFormatter();
    }

    java.util.Date dateTime = fmt.parse(timestampStr);
    return new java.sql.Timestamp(dateTime.getTime());
  }

  /**
   * Date diff.
   *
   * @param cal1 the cal 1
   * @param ts   the ts
   * @return the int
   */
  public static int dateDiff(Calendar cal1, java.sql.Timestamp ts) {
    Calendar cal2 = Calendar.getInstance();
    cal2.setTime(ts);
    dateTrunc(cal2);
    return cal1.compareTo(cal2);
  }

  /**
   * Gets the age between dates.
   *
   * @param fromDate the from date
   * @param toDate   the to date
   * @return the age between dates
   */
  public static Map getAgeBetweenDates(java.util.Date fromDate, java.util.Date toDate) {

    int daysInAMonth = 31;
    int daysInTwoYears = 730;

    long age = getDifferenceDays(fromDate, toDate, DURATION_DAYS);
    String ageUnit = "";
    if (age >= 0) {
      if (age < daysInAMonth) {
        ageUnit = "D";
      } else if (age < daysInTwoYears) {
        age = getDifferenceDays(fromDate, toDate, DURATION_MONTHS);
        ageUnit = "M";
      } else {
        age = getDifferenceDays(fromDate, toDate, DURATION_YEARS);
        ageUnit = "Y";
      }
    }

    Map map = new HashMap();
    map.put("age", new BigDecimal(age));
    map.put("ageIn", ageUnit);
    return map;
  }

  /**
   * Gets the date formatter with slash.
   *
   * @return the date formatter with slash
   */
  public static SimpleDateFormat getDateFormatterWithSlash() {
    return DateFormatterWithSlash;
  }

  /**
   * Sets the date formatter with slash.
   *
   * @param dateFormatterWithSlash the new date formatter with slash
   */
  public static void setDateFormatterWithSlash(SimpleDateFormat dateFormatterWithSlash) {
    DateFormatterWithSlash = dateFormatterWithSlash;
  }

  /**
   * Timestamp from date time.
   *
   * @param inputDate the d
   * @param inputTime the t
   * @return the java.sql. timestamp
   * @throws ParseException the parse exception
   */
  public static java.sql.Timestamp timestampFromDateTime(java.util.Date inputDate,
      java.sql.Time inputTime) throws ParseException {

    String date = dateFormatter.format(inputDate);
    String time = timeFormatterSecs.format(inputTime);
    SimpleDateFormat fmt = timeStampFormatter;
    String dateTime = date + " " + time;
    java.util.Date tt = fmt.parse(dateTime);
    return new java.sql.Timestamp(tt.getTime());
  }

  /**
   * Removes the time from date. Sets it to 00:00
   *
   * @param date the date
   * @return the date
   */
  public static Date removeTimeFromDate(Date date) {

    if (date == null) {
      return null;
    }
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    calendar.set(Calendar.HOUR_OF_DAY, 0);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
    return calendar.getTime();
  }

  /**
   * Compares date difference. Returns the value 0 if this Date object and the given object are
   * equal; a value less than 0 if this Date object is before the given argument; and a value
   * greater than 0 if this Date object is after the given argument.
   *
   * @param fromDate     the from date
   * @param toDate       the to date
   * @param numberOfDays the number of days
   * @return the integer
   */
  public static Integer compareDateDayDifference(Date fromDate, Date toDate, Integer numberOfDays) {
    java.sql.Timestamp toDateNew = addDays(new java.sql.Timestamp(fromDate.getTime()),
        numberOfDays);
    return toDateNew.compareTo(toDate);
  }

  /**
   * Takes a date String of format dd/MM/YYYY HH:mm and returns a java.sql.Timestamp timestamp
   *
   * @param dateString the date string in dd/MM/YYYY HH:mm format
   * @return the timestamp
   * @throws ParseException the parse exception
   */
  public static Timestamp stringToTimestamp(String dateString) throws ParseException {
    // date string null check
    if ("".equals(dateString) || dateString == null) {
      return null;
    }
    DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    Date date = formatter.parse(dateString);
    return new Timestamp(date.getTime());
  }

  /**
   * Takes a date String of format YYYY-MM-DD HH:mm and returns a java.sql.Timestamp timestamp
   *
   * @param dateString the date string in YYYY-MM-DD HH:mm format
   * @return the timestamp
   * @throws ParseException the parse exception
   */
  public static Timestamp stringTosqlTimeStampFormatter(String dateString) throws ParseException {
    // date string null check
    if ("".equals(dateString) || dateString == null) {
      return null;
    }
    Date date = sqlTimeStampFormatter.parse(dateString);
    return new Timestamp(date.getTime());
  }

  /**
   * Takes a date String of format dd/MM/YYYY and returns a java.sql.Date Date
   *
   * @param dateString the date string in the form dd/MM/yyyy
   * @return the java.sql.Date date
   * @throws ParseException the parse exception
   */
  public static Date stringToDate(String dateString) throws ParseException {
    // date string null check
    if ("".equals(dateString) || dateString == null) {
      return null;
    }
    DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
    Date date = formatter.parse(dateString);
    return new java.sql.Date(date.getTime());
  }

  /**
   * Check and set age components.
   *
   * @param patientBean the patient bean
   */
  public static void checkAndSetAgeComponents(BasicDynaBean patientBean) {
    if (patientBean.get("dateofbirth") != null) {
      Map ageMap = DateUtil.getAgeForDate((java.sql.Date) patientBean.get("dateofbirth"));
      patientBean.set("age", ((Long) ageMap.get("age")).intValue());
      patientBean.set("agein", ageMap.get("ageIn"));
      patientBean.set("age_text", "" + ageMap.get("age") + ageMap.get("ageIn"));
    } else if (patientBean.get("expected_dob") != null) {
      boolean precise = (patientBean.get("dateofbirth") != null);
      String ageText = DateUtil.getAgeText((java.sql.Date) patientBean.get("expected_dob"),
          precise);
      patientBean.set("age_text", ageText);
      if (!precise) {
        patientBean.set("age", Integer.parseInt(ageText.substring(0, ageText.length() - 1)));
        patientBean.set("agein", ageText.substring(ageText.length() - 1).toUpperCase());
      }
    }
  }
  
  public static int getYear(Date date) {
    return Integer.valueOf(yearFormatter.format(date));
  }


  /**
   * Gets the age and agein from age Text.
   *
   * @param ageText an ageText
   * @return the age and agein
   */
  public static Map getAgeAndAgeIn(String ageText) {

    Matcher matcher = Pattern.compile("^(\\d+)(\\w+)").matcher(ageText);
    String age = "";
    String ageUnit = "";
    if (matcher.find()) {
      age = matcher.group(1);
      ageUnit = matcher.group(2);
    }
    Map map = new HashMap();
    map.put("age", age);
    map.put("ageIn", ageUnit);
    return map;
  }


  /**
   * Convert time Stamp to ISO8601 format.
   *
   * @param timestampStr the timestamp str
   * @return the String
   * @throws ParseException the parse exception
   */
  public static String convertTimeStampToIso8601(String timestampStr)
          throws java.text.ParseException {
    Timestamp timestamp = Timestamp.valueOf(timestampStr);
    String date = dateFormatter.format(timestamp.getTime());
    String time = timeFormatterSecs.format(timestamp.getTime());
    date = dateFormatterYearAhead.format(dateFormatter.parse(date));
    return formatIso8601Date(java.sql.Date.valueOf(date))
            + "T" + DateUtil.formatIso8601Time(Time.valueOf(time));
  }
}
