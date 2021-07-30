package com.insta.hms.common.validation;

import com.bob.hms.common.Preferences;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.PhoneNumberUtil;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.preferences.genericpreferences.GenericPreferencesService;
import com.insta.hms.common.security.SecurityService;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.exception.ValidationErrorMap;
import com.insta.hms.mdm.centers.CenterService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
public class PhoneNumberRule extends ValidationRule {

  @Autowired
  GenericPreferencesService genericPreferencesService;

  @Autowired
  CenterService centerService;

  @LazyAutowired
  private SessionService sessionService;

  @LazyAutowired
  private SecurityService securityService;

  @Override
  public boolean apply(BasicDynaBean bean, String[] fields, ValidationErrorMap errorMap) {
    boolean ok = false;
    if (null != bean) {
      ok = true;
      for (String field : fields) {
        BasicDynaBean genericPreferences = genericPreferencesService.getAllPreferences();
        String phoneNumber = (String) bean.get(field);
        phoneNumber = phoneNumber.replaceFirst("^0+(?!$)", "");
        if (phoneNumber == null || phoneNumber.isEmpty()) {
          continue;
        }
        boolean isValid = false;
        List<String> phone = PhoneNumberUtil.getCountryCodeAndNationalPart(phoneNumber, null);
        String countryCode = null;
        String national = null;
        int centerId = 0;
        boolean smsModActive = false;
        if (RequestContext.getHttpRequest() != null) {
          centerId = RequestContext.getCenterId();
          Map<String, Object> sessionMap = sessionService
              .getSessionAttributes(new String[] { "preferences" });
          if (sessionMap != null) {
            Preferences prefs = (Preferences) sessionMap.get("preferences");
            Map modules = prefs.getModulesActivatedMap();
            smsModActive = modules.containsKey("mod_messaging")
                && "Y".equals(modules.get("mod_messaging"));
          }
        }
        String defaultCode = centerService.getCountryCode(centerId);
        if (defaultCode == null) {
          defaultCode = centerService.getCountryCode(0);
        }
        if ((phone == null || phone.get(0) == null) && defaultCode == null && smsModActive) {
          errorMap.addError(field, "exception.registration.patient.invalid.mobileno.coderequired");
          return false;
        }
        if (phone == null) {
          String appendedNo = (defaultCode != null ? ("+" + defaultCode) : "") + phoneNumber;
          // phone is not in E.164 format, So check whether number is valid using appendedNo which
          // is in E.164
          isValid = PhoneNumberUtil.isValidNumberMobile(appendedNo) || PhoneNumberUtil.isMatches(
              phoneNumber, (String) genericPreferences.get("mobile_starting_pattern"),
              (String) genericPreferences.get("mobile_length_pattern"));
          if (!isValid && defaultCode != null && phoneNumber.startsWith(defaultCode)) {
            // If the phoneNumber is in E.164 without "+" prefix, check if it is valid by prepending
            // "+"
            appendedNo = "+" + phoneNumber;
            isValid = PhoneNumberUtil.isValidNumberMobile(appendedNo)
                || PhoneNumberUtil.isMatches(phoneNumber.substring(defaultCode.length()),
                    (String) genericPreferences.get("mobile_starting_pattern"),
                    (String) genericPreferences.get("mobile_length_pattern"));
          }
          if (isValid) {
            bean.set(field, appendedNo);
          }
        } else {
          countryCode = phone.get(0);
          national = phone.get(1);
          if (countryCode.equals(defaultCode)) {
            // is the country code equal to country of DEFAULT CENTER
            isValid = PhoneNumberUtil.isMatches(national,
                (String) genericPreferences.get("mobile_starting_pattern"),
                (String) genericPreferences.get("mobile_length_pattern"))
                || PhoneNumberUtil.isValidNumberMobile(phoneNumber);
          } else { // for International number i.e other the Hospital country
            isValid = PhoneNumberUtil.isValidNumberMobile(phoneNumber);
          }
          if (isValid && countryCode != null) {
            bean.set(field, "+" + countryCode + national);
          }
        }
        if (!isValid) {
          countryCode = countryCode == null ? defaultCode : countryCode;
          if (countryCode != null) {
            try {
              errorMap.addError(field, "exception.registration.patient.invalid.mobile.no",
                  Arrays.asList("+" + countryCode
                      + PhoneNumberUtil.getExampleNumber(Integer.parseInt(countryCode))));
            } catch (Exception exception) {
              errorMap.addError(field, "exception.registration.patient.invalid.mobile.no",
                  Arrays.asList("+" + defaultCode
                      + PhoneNumberUtil.getExampleNumber(Integer.parseInt(defaultCode))));
            }

          } else {
            errorMap.addError(field, "exception.registration.patient.invalid.mobileno.noexample",
                Arrays.asList((String) genericPreferences.get("mobile_length_pattern"),
                    (String) genericPreferences.get("mobile_starting_pattern")));

          }
          ok = isValid;
        }
      }
    }
    return ok;
  }
}
