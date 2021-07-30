package com.insta.hms.common;

import com.bob.hms.common.RequestContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;

import org.apache.http.HttpHeaders;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.owasp.esapi.ESAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * The Class PhoneNumberUtilAction.
 */
public class PhoneNumberUtilAction extends BaseAction {

  /** The center master dao. */
  private static CenterMasterDAO centerMasterDao = new CenterMasterDAO();

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(PhoneNumberUtilAction.class);

  /**
   * Validates the given phone number is valid Mobile number.
   *
   * @param mapping  the mapping
   * @param form     the form
   * @param request  the request
   * @param response the response
   * @return the action forward
   * @throws IOException  Signals that an I/O exception has occurred.
   * @throws SQLException the SQL exception
   */
  @IgnoreConfidentialFilters
  public ActionForward isValidNumber(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {
    response.setContentType("application/json");
    response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    String number = request.getParameter("number");
    boolean isValid = isValidMobileNumber(number);
    response.getWriter().write("{\"result\": \"" + isValid + "\"}");
    response.flushBuffer();
    return null;
  }

  /**
   * Returns a sample valid Mobile number of a given country.
   *
   * @param mapping  the mapping
   * @param form     the form
   * @param request  the request
   * @param response the response
   * @return the example number
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @IgnoreConfidentialFilters
  public ActionForward getExampleNumber(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/json");
    response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    String exampleNum = PhoneNumberUtil
        .getExampleNumber(Integer.valueOf(request.getParameter("countryCode")));
    response.getWriter().write("{\"result\": \"" + exampleNum + "\"}");
    response.flushBuffer();
    return null;

  }

  /**
   * returns the country code and national number from given Phone number.
   *
   * @param mapping  the mapping
   * @param form     the form
   * @param request  the request
   * @param response the response
   * @return the national and country code
   * @throws IOException  Signals that an I/O exception has occurred.
   * @throws SQLException the SQL exception
   */
  @IgnoreConfidentialFilters
  public ActionForward getNationalAndCountryCode(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {
    response.setContentType("application/json");
    response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    String number = request.getParameter("number");
    String json = new ObjectMapper().writeValueAsString(getNationalAndCountryCodeUtil(number));
    response.getWriter().write(json);
    response.flushBuffer();
    return null;

  }

  /**
   * Returns the country code corresponding to ISO-2 letter country code EX: returns 91 for IN.
   *
   * @param mapping  the mapping
   * @param form     the form
   * @param request  the request
   * @param response the response
   * @return the country code from region
   * @throws IOException  Signals that an I/O exception has occurred.
   * @throws SQLException the SQL exception
   */
  @IgnoreConfidentialFilters
  public ActionForward getCountryCodeFromRegion(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {
    response.setContentType("application/json");
    response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    String regionCode = request.getParameter("region_code");
    response.getWriter()
        .write("{\"result\": \"" + PhoneNumberUtil.getCountryCodeForRegion(regionCode) + "\"}");
    response.flushBuffer();
    return null;

  }

  /**
   * Util function - If the number doesnt not have '+' then the country code is the country of
   * current center.
   *
   * @param number the number
   * @return JSONify String
   * @throws SQLException the SQL exception
   */
  public Map<String, String> getNationalAndCountryCodeUtil(String number) throws SQLException {
    char startingChar = StringUtil.isNullOrEmpty(number) ? '\0' : number.charAt(0);
    if (startingChar == '+') {
      number = startingChar + ESAPI.encoder().encodeForJavaScript(number.substring(1));
    } else {
      number = ESAPI.encoder().encodeForJavaScript(number);
    }
    Map<String, String> data = new HashMap<String, String>();
    String countryCode = centerMasterDao.getCountryCode(RequestContext.getCenterId());
    if (StringUtil.isNullOrEmpty(countryCode)) {
      countryCode = centerMasterDao.getCountryCode(0);
    }
    List<String> numberWrapper = PhoneNumberUtil.getCountryCodeAndNationalPart(number, countryCode);
    String national = null;
    if (numberWrapper != null) {
      countryCode = numberWrapper.get(0);
      national = numberWrapper.get(1);
    } else { // Number is not parsed properly
      countryCode = (countryCode == null) ? "" : countryCode;
      number = (number == null) ? "" : number.trim();
      if (number.isEmpty()) {
        national = "";
      } else if (number.charAt(0) == '+') {
        national = number.substring(1); // remove the '+' part
        if (national.startsWith(countryCode)) {
          national = national.substring(countryCode.length());
        }
      } else {
        national = number;
      }
    }
    data.put("national", national);
    data.put("country_code", countryCode);
    return data;
  }

  /**
   * Checks if is valid mobile number. Central Logic for validating mobile number
   *
   * @param number the number
   * @return true, if is valid mobile number
   * @throws SQLException the SQL exception
   */
  @IgnoreConfidentialFilters
  public static boolean isValidMobileNumber(String number) throws SQLException {
    boolean isValid = true;
    if (GenericPreferencesDAO.getGenericPreferences().getIsMobileValidate().equals("Y")) {
      // If Validation enabled
      String countryCode = PhoneNumberUtil.getCountryCode(number);
      String national = PhoneNumberUtil.getNationalNumber(number);
      String centerCountryCode = centerMasterDao.getCountryCode(RequestContext.getCenterId());
      if (StringUtil.isNullOrEmpty(centerCountryCode)) {
        centerCountryCode = centerMasterDao.getCountryCode(0);
      }

      if (countryCode == null || national == null) {
        isValid = false;
      } else if (countryCode.equals(centerCountryCode)) {
        isValid = PhoneNumberUtil.isMatches(national,
            GenericPreferencesDAO.getGenericPreferences().getMobileStartPattern(),
            GenericPreferencesDAO.getGenericPreferences().getMobileLengthPattern())
            || PhoneNumberUtil.isValidNumberMobile(number);
      } else { // for International number i.e other the Hospital country
        isValid = PhoneNumberUtil.isValidNumberMobile(number);
      }
    }
    return isValid;
  }

}
