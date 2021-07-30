package com.insta.hms.common;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberType;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;

/**
 * The Class PhoneNumberUtil.
 */
public class PhoneNumberUtil {

  /** The logger. */
  private static Logger logger = LoggerFactory.getLogger(PhoneNumberUtil.class);

  /** The phone util. */
  private static com.google.i18n.phonenumbers.PhoneNumberUtil phoneUtil = 
      com.google.i18n.phonenumbers.PhoneNumberUtil
      .getInstance();

  static {
    java.util.logging.Logger.getLogger(
        com.google.i18n.phonenumbers.PhoneNumberUtil.class.getName())
        .setLevel(Level.OFF);
  }

  /**
   * Returns whether the provided mobile number is valid.
   *
   * @param number - The mobile number to validate
   * @return true, if is valid number mobile
   */
  public static boolean isValidNumberMobile(String number) {
    try {
      PhoneNumber phoneNumberProto = phoneUtil.parse(number, null);
      PhoneNumberType isMobile = phoneUtil.getNumberType(phoneNumberProto);
      return phoneUtil.isValidNumber(phoneNumberProto)
          && (isMobile == PhoneNumberType.FIXED_LINE_OR_MOBILE
              || isMobile == PhoneNumberType.MOBILE);

    } catch (NumberParseException exception) {
      logger.debug("Not a valid mobile number: " + number);
    }
    return false;

  }

  /**
   * Get the country code part of phone number.
   *
   * @param number the number
   * @return the country code
   */
  public static String getCountryCode(String number) {
    try {
      PhoneNumber phoneNumberProto = phoneUtil.parse(number, null);
      return String.valueOf(phoneNumberProto.getCountryCode());

    } catch (NumberParseException exception) {
      logger.debug("Unable to get country code of number: " + number);
      return null;
    }
  }

  /**
   * Get the country code from ISO-2 letter regionCode EX: for IN returns 91.
   *
   * @param regionCode the region code
   * @return the country code for region
   */
  public static String getCountryCodeForRegion(String regionCode) {
    return String.valueOf(phoneUtil.getCountryCodeForRegion(regionCode));
  }

  /**
   * Get the national number part. Eg : if number is +91-9999999999 , returns 9999999999
   *
   * @param number the number
   * @return the national number
   */
  public static String getNationalNumber(String number) {
    try {
      PhoneNumber phoneNumberProto = phoneUtil.parse(number, null);
      return String.valueOf(phoneNumberProto.getNationalNumber());

    } catch (NumberParseException exception) {
      logger.debug("Unable to get national part of  number: " + number);
      return null;
    }

  }

  /**
   * Gets example number for countryRegion Ex: If country code is 91 , returns India's example
   * mobile number.
   *
   * @param countryCode the country code
   * @return the example number
   */
  public static String getExampleNumber(int countryCode) {
    return String.valueOf(
        phoneUtil.getExampleNumberForType(phoneUtil.getRegionCodeForCountryCode(countryCode),
            PhoneNumberType.MOBILE).getNationalNumber());

  }

  /**
   * Get all the country code and region.
   *
   * @return the all countries
   */
  public static List<List<String>> getAllCountries() {
    List<List<String>> regionsList = new ArrayList<List<String>>();

    for (String region : phoneUtil.getSupportedRegions()) {
      List<String> regionList = new ArrayList<String>();
      regionList.add(String.valueOf(phoneUtil.getCountryCodeForRegion(region)));
      regionList.add(region);
      regionsList.add(regionList);

    }
    return regionsList;
  }

  /**
   * Whether the number matches with the given pattern.
   *
   * @param number              the number
   * @param mobileStartPattern  the mobile start pattern
   * @param mobileLengthPattern the mobile length pattern
   * @return true, if is matches
   */
  public static boolean isMatches(String number, String mobileStartPattern,
      String mobileLengthPattern) {
    if (mobileLengthPattern == null || mobileStartPattern == null) {
      return false;
    }
    String[] possibleLength = mobileLengthPattern.split(",");
    boolean isValid = false;
    for (int i = 0; i < possibleLength.length; i++) {
      try {
        if (number.length() == Integer.valueOf(possibleLength[i])) {
          isValid = true;
          break;
        }
      } catch (NumberFormatException ex) {
        // Do nothing
      }
    }
    if (!isValid) {
      return false;
    }
    // Now match for start prefix
    isValid = false;
    String[] possibleStart = mobileStartPattern.split(",");
    for (int i = 0; i < possibleStart.length; i++) {
      if (number.startsWith(possibleStart[i])) {
        isValid = true;
        break;

      }
    }
    return isValid;
  }

  /**
   * Get all countries with name. This list is fetched from Locale package of Java 6
   *
   * @return the all countries with name
   */
  public static List<List<String>> getAllCountriesWithName() {
    List<List<String>> countriesList = new ArrayList<List<String>>();
    String[] isoCountries = Locale.getISOCountries();
    for (String country : isoCountries) {
      Locale locale = new Locale("en", country);
      String name = locale.getDisplayCountry();
      if (!getCountryCodeForRegion(country).equals(String.valueOf(0))) {
        List<String> countryList = new ArrayList<String>();
        countryList.add(country);
        countryList.add(name);
        countriesList.add(countryList);
      }
      Collections.sort(countriesList, new ListComparator());
    }

    // Collections.sort(countries, new CountryComparator());
    return countriesList;
  }

  /**
   * Returns Display name for given ISO 2-letter code Ex: Returns India for IN Ex: Returns 0 for
   * invalid regionCode.
   *
   * @param regionCode - ISO 2-letter code
   * @return the display country
   */
  public static String getDisplayCountry(String regionCode) {
    Locale locale = new Locale("en", regionCode);
    return locale.getDisplayCountry();
  }

  /**
   * Use this method if both country code and national part of mobile number are required, Instead
   * of {@link #getCountryCode(String)} and {@link #getNationalNumber(String)}.
   *
   * @param number      - Mobile number to be parsed
   * @param countryCode - The country code the number is part of. This coutryCode will not be used
   *                    for splitting if the number is in i18n format
   * @return the list having CountryCode as 1st element, NationalPart as 2nd element . returns null
   *         if number cannot be parsed
   */
  public static List<String> getCountryCodeAndNationalPart(String number, String countryCode) {
    List<String> list = null;
    try {
      // ISO-2 letter regionCode obtained from countryCode
      String regionCode = (countryCode != null)
          ? phoneUtil.getRegionCodeForCountryCode(Integer.valueOf(countryCode))
          : null;
      PhoneNumber phoneNumberProto = phoneUtil.parse(number, regionCode);
      // list having 1st element - CountryCode,2nd element - NationalPart
      list = new ArrayList<String>();
      list.add(String.valueOf(phoneNumberProto.getCountryCode()));
      list.add(String.valueOf(phoneNumberProto.getNationalNumber()));
    } catch (Exception exception) {
      logger.debug("Unable to split number: " + number + " , Exception: " + exception);
    }
    return list;
  }

  /**
   * The Class ListComparator.
   */
  /*
   * Sort country names By Ascending order
   */
  private static class ListComparator implements Comparator<List<String>> {

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare(List<String> country1, List<String> country2) {
      if (country1 == null && country2 == null) {
        return 0;
      }

      if (country1 != null && country2 != null && country1.get(1) == null
          && country2.get(1) == null) {
        return 0;
      }

      if (country1 == null || country1.get(1) == null) {
        return -1;
      }

      if (country2 == null || country2.get(1) == null) {
        return 1;
      }

      return country1.get(1).compareTo(country2.get(1));
    }
  }
}
