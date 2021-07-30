package com.insta.hms.integration;

import com.bob.hms.common.DateUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.StringUtil;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.preferences.genericpreferences.GenericPreferencesService;
import com.insta.hms.common.security.SecurityService;
import com.insta.hms.core.patient.PatientDetailsService;
import com.insta.hms.core.patient.registration.PrepopulateVisitInfoRepository;
import com.insta.hms.core.patient.registration.PrepopulateVisitInfoService;
import com.insta.hms.core.patient.registration.RegistrationService;
import com.insta.hms.core.patient.registration.RegistrationValidator;
import com.insta.hms.exception.ValidationErrorMap;
import com.insta.hms.exception.ValidationException;
import com.insta.hms.integration.patientsync.PatientRecord;
import com.insta.hms.integration.patientsyncresponse.Errors;
import com.insta.hms.integration.patientsyncresponse.PatientRecordResponse;
import com.insta.hms.mdm.cities.CityService;
import com.insta.hms.mdm.countries.CountryService;
import com.insta.hms.mdm.govtidentifiers.GovtIdentifierRepository;
import com.insta.hms.mdm.salutations.SalutationService;
import com.insta.hms.security.usermanager.UserService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.httpclient.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The Class InstaIntegrationService.
 */
@Service
public class InstaIntegrationService {

  /** The logger. */
  Logger logger = LoggerFactory.getLogger(InstaIntegrationService.class);

  /** The insta integration repository. */
  @LazyAutowired
  private InstaIntegrationRepository instaIntegrationRepository;

  /** The center integration repository. */
  @LazyAutowired
  private CenterIntegrationRepository centerIntegrationRepository;

  /** The security service. */
  @LazyAutowired
  private SecurityService securityService;

  /** The patient details service. */
  @LazyAutowired
  private PatientDetailsService patientDetailsService;

  /** The registration service. */
  @LazyAutowired
  private RegistrationService registrationService;

  /** The registration validator. */
  @LazyAutowired
  private RegistrationValidator registrationValidator;

  /** The country service. */
  @LazyAutowired
  private CountryService countryService;
  
  @LazyAutowired
  GovtIdentifierRepository govtIdentifierRepository;

  /** The date util. */
  @LazyAutowired
  private DateUtil dateUtil;

  /** The salutation service. */
  @LazyAutowired
  private SalutationService salutationService;

  /** The user service. */
  @LazyAutowired
  private UserService userService;

  /** The prepopulate visit info repository. */
  @LazyAutowired
  private PrepopulateVisitInfoRepository prepopulateVisitInfoRepository;

  /** The prepopulate visit info service. */
  @LazyAutowired
  private PrepopulateVisitInfoService prepopulateVisitInfoService;

  /** The city service. */
  @LazyAutowired
  private CityService cityService;

  /** The gen pref service. */
  @LazyAutowired
  private GenericPreferencesService genPrefService;

  /**
   * Gets the active record.
   *
   * @param integrationName the integration name
   * @return the active record
   */
  public BasicDynaBean getActiveRecord(String integrationName) {
    List<BasicDynaBean> integrationBeanList = getRecords(integrationName);
    BasicDynaBean activeBean = null;
    for (BasicDynaBean integrationBean : integrationBeanList) {
      if (integrationBean.get("status") != null && integrationBean.get("status").equals("A")) {
        activeBean = integrationBean;
      }
    }
    return activeBean;
  }

  /**
   * Gets the records.
   *
   * @param integrationName the integration name
   * @return the records
   */
  public List<BasicDynaBean> getRecords(String integrationName) {
    return instaIntegrationRepository.listAll(null, "integration_name", integrationName);
  }

  /**
   * Gets the integration details.
   *
   * @param filterMap the filter map
   * @return the integration details
   */
  public BasicDynaBean getIntegrationDetails(Map<String, Object> filterMap) {
    List<BasicDynaBean> list = instaIntegrationRepository.listAll(null, filterMap, null);

    return (list != null && !list.isEmpty()) ? list.get(0) : null;

  }

  /**
   * Get the Details of App Integration details including the center specific details.
   *
   * @param integrationName          - Name of the integration for which details are required
   * @param centerId          - Center Id for which integration details are required
   * @return the center integration details
   */
  public BasicDynaBean getCenterIntegrationDetails(String integrationName, int centerId) {
    return getCenterIntegrationDetails(integrationName, centerId, null);

  }

  /**
   * Gets the center integration details.
   *
   * @param integrationName the integration name
   * @param centerId the center id
   * @param accountGroupId the account group id
   * @return the center integration details
   */
  public BasicDynaBean getCenterIntegrationDetails(String integrationName, int centerId,
      Integer accountGroupId) {
    return instaIntegrationRepository.getCenterIntegrationDetails(integrationName, centerId,
        accountGroupId);

  }

  /**
   * Inserts into Center Integration Repository If the record does not exist for the filterMap. Else
   * updates the existing record
   * 
   * @param filterMap
   *          - The (Key,Value) pairs used in WHERE clause for fetching the Record NOTE: filterMap
   *          should return 1 record. Make sure that either UNIQUE or PKs are used in Map
   * @param valuesMap
   *          - The (Key, Value) pairs containing the new values for record
   * 
   */
  public void insertOrUpdateCenterDetials(Map<String, Object> filterMap,
      Map<String, Object> valuesMap) {
    List<BasicDynaBean> list = centerIntegrationRepository.listAll(null, filterMap, null);
    if (list != null && !list.isEmpty()) {
      centerIntegrationRepository.update(list.get(0), filterMap, valuesMap);
    } else {
      centerIntegrationRepository.insert(valuesMap);
    }

  }

  /**
   * Center details lookup.
   *
   * @param filterMap the filter map
   * @return the list
   */
  public List<BasicDynaBean> centerDetailsLookup(Map<String, Object> filterMap) {
    return centerIntegrationRepository.listAll(null, filterMap, null);

  }

  /** The status success. */
  private static final String STATUS_SUCCESS = "success";
  
  /** The status fail. */
  private static final String STATUS_FAIL = "fail";

  /**
   * Sync external patient.
   *
   * @param patientRecord the patient record
   * @param response the response
   * @return the patient record response
   */
  @SuppressWarnings("static-access")
  public PatientRecordResponse syncExternalPatient(PatientRecord patientRecord,
      HttpServletResponse response) {
    ServletRequest servletRequest = ((ServletRequestAttributes) RequestContextHolder
        .getRequestAttributes()).getRequest();
    String ip = servletRequest.getRemoteAddr();
    String xxRealIp = ((HttpServletRequest) servletRequest).getHeader("X-Real-IP");
    logger.info("Sync external patient ::: request ip address " + ip);
    logger.info("Sync external patient ::: request get Header X-Real-IP " + xxRealIp);
    // if tomcat webapp is running behind nginx proxy, then consider X-Real-IP,
    // otherwise IP address is sufficient.
    if (xxRealIp != null) {
      ip = xxRealIp;
    }
    PatientRecordResponse patientResponse = new PatientRecordResponse();
    Errors patientResponseErrors = new Errors();
    // Check schema
    String schema = "";
    if (!StringUtil.isNullOrEmpty(patientRecord.getSchema())) {
      schema = patientRecord.getSchema();
    } else {
      response.setStatus(HttpStatus.SC_BAD_REQUEST);
      patientResponse.setStatus(STATUS_FAIL);
      patientResponseErrors.setSchema("Schema is null or empty");
    }
    Boolean foundSchema = false;
    List<BasicDynaBean> schemaList = DatabaseHelper.getAllSchemas();
    for (BasicDynaBean schemaBean : schemaList) {
      if (schema.equals(schemaBean.get("schema"))) {
        foundSchema = true;
      }
    }
    if (!foundSchema) {
      response.setStatus(HttpStatus.SC_BAD_REQUEST);
      patientResponse.setStatus(STATUS_FAIL);
      patientResponseErrors.setSchema("Schema is not valid");
      patientResponse.setErrors(patientResponseErrors);
      return patientResponse;
    }
    Map<String, Object> patientDetails = new HashMap<String, Object>();
    Map<String, Object> patientDemography = new HashMap<String, Object>();
    RequestContext.getConnectionDetails();
    RequestContext.setConnectionDetails(new String[] { "", "", schema, "InstaAPI" });
    if (!isIpAllowed(ip,"sync_ext")) {
      response.setStatus(HttpStatus.SC_FORBIDDEN);
      patientResponse.setStatus(STATUS_FAIL);
      return patientResponse;
    }
    Map genPrefs = genPrefService.getPreferences().getMap();
    if (genPrefs.get("max_centers_inc_default") != null
        && (Integer) genPrefs.get("max_centers_inc_default") > 1) {
      response.setStatus(HttpStatus.SC_BAD_REQUEST);
      patientResponse.setStatus(STATUS_FAIL);
      patientResponseErrors.setRemark("Can't create patient for multiceneter schema.");
      patientResponse.setErrors(patientResponseErrors);
      return patientResponse;
    }
    if (securityService.getActivatedModules().contains("mod_sync_external_patient")) {
      patientResponse.setStatus(STATUS_SUCCESS);
      // check old_mr_no is present in our patient_details.
      if (patientRecord.getPatientDetails() != null) {
        // old_mr_no is emp_id
        String oldMrNo = "";
        if (!StringUtil.isNullOrEmpty(patientRecord.getPatientDetails().getOldMrNo())) {
          oldMrNo = patientRecord.getPatientDetails().getOldMrNo().toUpperCase();
          patientDetails.put("oldmrno", oldMrNo);
          if (oldMrNo.length() > 20) {
            patientResponse.setStatus(STATUS_FAIL);
            patientResponseErrors.setRemark("Old_mr_no: employee id is more than 20 characters.");
            response.setStatus(HttpStatus.SC_BAD_REQUEST);
          }
        } else {
          patientResponse.setStatus(STATUS_FAIL);
          patientResponseErrors.setRemark("Old_mr_no: employee id can't be empty.");
          response.setStatus(HttpStatus.SC_BAD_REQUEST);
        }
        // First Name
        if (!StringUtil.isNullOrEmpty(patientRecord.getPatientDetails().getFirstName())) {
          patientDetails.put("patient_name", patientRecord.getPatientDetails().getFirstName());
        } else {
          patientResponse.setStatus(STATUS_FAIL);
          patientResponseErrors.setFirstName("First name tag is missing or empty.");;
          response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }
        // Middle Name
        patientDetails.put("middle_name",
            (patientRecord.getPatientDetails().getMiddleName() != null)
                ? patientRecord.getPatientDetails().getMiddleName()
                : "");
        // Last Name
        patientDetails.put("last_name",
            (patientRecord.getPatientDetails().getLastName() != null)
                ? patientRecord.getPatientDetails().getLastName()
                : "");
        // Gender
        String gender = "";
        String salutation = "";
        Map<String, Object> salutationParams = new HashMap<String, Object>();
        if (patientRecord.getPatientDetails().getGender() != null) {
          if (!StringUtil.isNullOrEmpty(
              (String) patientRecord.getPatientDetails().getGender().getDescription())) {
            gender = ((String) patientRecord.getPatientDetails().getGender().getDescription())
                .substring(0, 1);
          }
          if (!StringUtil.isNullOrEmpty(gender)) {
            if (gender.equalsIgnoreCase("F")) {
              salutation = "Miss"; // Miss
            } else if (gender.equalsIgnoreCase("M")) {
              salutation = "Mr."; // Mr.
            }
            salutationParams.put("gender", gender.toUpperCase());
            salutationParams.put("salutation", salutation);
            BasicDynaBean salutationBean = salutationService.getSalutationId(salutationParams);
            if (salutationBean != null) {
              patientDetails.put("salutation", (String) salutationBean.get("salutation_id"));
            }
          }
        } else {
          patientResponse.setStatus(STATUS_FAIL);
          patientResponseErrors.setGender("Gender is missing.");
          response.setStatus(HttpStatus.SC_BAD_REQUEST);
        }

        patientDetails.put("patient_gender", gender.toUpperCase());
        // Dateofbirth
        if (patientRecord.getPatientDetails().getDateOfBirth() != null
            && patientRecord.getPatientDetails().getDateOfBirth().isValid()) {
          Date dateOfBirth = null;
          try {
            dateOfBirth = dateUtil
                .parseIso8601Date(patientRecord.getPatientDetails().getDateOfBirth().toString());
          } catch (ParseException ex) {
            logger.error("", ex);
          }
          try {
            // dateOfBirth should be past date.
            if (dateUtil.compareDateDayDifference(dateOfBirth,
                dateUtil.parseIso8601Date(dateUtil.currentDate("yyyy-MM-dd")), 0) < 0) {
              if (dateUtil.getAgeComponents(new java.sql.Date(dateOfBirth.getTime()))[0] < 120) {
                patientDetails.put("dateofbirth",
                    new SimpleDateFormat("dd-MM-yyyy").format(dateOfBirth));
              } else {
                patientResponse.setStatus(STATUS_FAIL);
                patientResponseErrors.setDateOfBirth("Date of birth can't be older than 120 years");
                response.setStatus(HttpStatus.SC_BAD_REQUEST);
              }
            } else {
              patientResponse.setStatus(STATUS_FAIL);
              patientResponseErrors
                  .setDateOfBirth("Date of birth can't be future date or same day");
              response.setStatus(HttpStatus.SC_BAD_REQUEST);
            }
          } catch (ParseException ex) {
            logger.error("", ex);
          }
        } else {
          patientResponse.setStatus(STATUS_FAIL);
          patientResponseErrors
              .setDateOfBirth("Date of birth can't be null or empty and must be in YYYY-mm-dd.");
          response.setStatus(HttpStatus.SC_BAD_REQUEST);
        }
        patientDetails.put("agein", "N");
        patientDetails.put("is_unidentified_patient", (Boolean) false);

        // Nationality_id
        String nationalityId = "";
        String countryAlpha2Code = "";
        if (patientRecord.getPatientDetails().getNationalityId() != null
            && !StringUtil.isNullOrEmpty(
                (String) patientRecord.getPatientDetails().getNationalityId().getDescription())) {
          countryAlpha2Code = (String) patientRecord.getPatientDetails().getNationalityId()
              .getDescription().toUpperCase();
          String countryCodeColumn = "alpha2_code";
          if (countryAlpha2Code.length() == 3) {
            countryCodeColumn = "icao_alpha3_code";
          }
          BasicDynaBean countryObject = countryService.findByUniqueName(countryAlpha2Code,
              countryCodeColumn);
          if (countryObject != null) {
            nationalityId = (String) countryObject.get("country_id");
          } else {
            patientResponse.setStatus(STATUS_FAIL);
            patientResponseErrors
                .setNationalityId("Nationality id is not matching with any country/national.");
            response.setStatus(HttpStatus.SC_BAD_REQUEST);
          }
          patientDetails.put("nationality_id", nationalityId);
        }
        
        String governmentIdentifier = patientRecord.getPatientDetails().getGovernmentIdentifier();
        // government_identifier
        if (!StringUtil.isNullOrEmpty(governmentIdentifier)) {
          patientDetails.put("government_identifier", governmentIdentifier);
          Map<String, Object> filterMap = new HashMap<>();
          filterMap.put("identifier_type", governmentIdentifier);
          BasicDynaBean bean = govtIdentifierRepository.findByKey(filterMap);
          if (bean != null) {
            patientDetails.put("identifier_id", (Integer) bean.get("identifier_id"));
          }
        } else if (governmentIdentifier != null && governmentIdentifier.trim().isEmpty()) {
          patientDetails.put("government_identifier", "");
        }

        // mobile_number, is always a free text, incorrect format, so, storing in a patient_phone2
        if (!StringUtil.isNullOrEmpty(patientRecord.getPatientDetails().getMobileNumber())) {
          if (patientRecord.getPatientDetails().getMobileNumber().length() > 20) {
            patientResponse.setStatus(STATUS_FAIL);
            patientResponseErrors
                .setRemark("mobile_number: mobile number is more than 20 characters.");
            response.setStatus(HttpStatus.SC_BAD_REQUEST);
          } else {
            patientDetails.put("patient_phone2",
                patientRecord.getPatientDetails().getMobileNumber());
          }
        }
        // Email
        if (!StringUtil.isNullOrEmpty(patientRecord.getPatientDetails().getEmail())) {
          String expression = "^[A-Z0-9._%+-/']+@[A-Z0-9.-]+\\.[A-Z]{2,6}$";
          Pattern pt = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
          Matcher mt = pt.matcher(patientRecord.getPatientDetails().getEmail());
          if (mt.matches()) {
            patientDetails.put("email_id", patientRecord.getPatientDetails().getEmail());
          } else {
            patientResponse.setStatus(STATUS_FAIL);
            patientResponseErrors.setEmail("Email id is not valid.");
            response.setStatus(HttpStatus.SC_BAD_REQUEST);
          }
        }
        // custom_list2 - Employee status
        String employeeStatus = "";
        if (patientRecord.getPatientDetails().getCustomList2Value() != null
            && !StringUtil.isNullOrEmpty(
                patientRecord.getPatientDetails().getCustomList2Value().getDescription())) {
          employeeStatus = (String) patientRecord.getPatientDetails().getCustomList2Value()
              .getDescription();
          patientDetails.put("custom_list2_value", employeeStatus);
        }

        if (patientRecord.getPatientDetails().getCustomList3Value() != null
            && !StringUtil.isNullOrEmpty((String) patientRecord.getPatientDetails()
                .getCustomList3Value().getDescription())) {
          String salutationTemp = (String) patientRecord.getPatientDetails().getCustomList3Value()
              .getDescription();
          patientDetails.put("custom_list3_value", salutationTemp);
        }

        // custom_field15 - Separation date
        if (patientRecord.getPatientDetails().getCustomField15() != null) {
          Date separationDate = null;
          try {
            separationDate = dateUtil
                .parseIso8601Date(patientRecord.getPatientDetails().getCustomField15().toString());
            // separationDate can be past or present or future.
            patientDetails.put("custom_field15",
                new SimpleDateFormat("dd-MM-yyyy").format(separationDate));
          } catch (ParseException ex) {
            logger.error("", ex);
          }
        }
        // passport_number - Separation email
        patientDetails.put("passport_no",
            (!StringUtil.isNullOrEmpty(patientRecord.getPatientDetails().getPassportNumber()))
                ? patientRecord.getPatientDetails().getPassportNumber()
                : "");

        patientDetails.put("mobile_access", "true");
        patientDetails.put("portal_access", "false");
        patientDetails.put("sms_for_vaccination", "N");
        patientDetails.put("resource_captured_from", "extpsync");
        patientDetails.put("old_reg_auto_generate", "Y");
        // user_name : set default user as InstaApi
        patientDetails.put("user_name", "InstaAPI");
        // City hardcoded for Etihad as "Abu Dhabi"
        Map<String, String> mapCityCountry = cityService.getCityAndCountryId("Abu Dhabi");
        patientDetails.put("patient_city", mapCityCountry.get("cityId"));
        patientDetails.put("country", mapCityCountry.get("countryId"));
        patientDetails.put("country_name", mapCityCountry.get("countryName"));

        String mrNo = "";
        BasicDynaBean checkMrnoBean = patientDetailsService.getMrNoWithOldMrNoBean(oldMrNo);
        if (checkMrnoBean != null) {
          mrNo = (String) checkMrnoBean.get("mr_no");
          patientDetails.put("mr_no", mrNo);
        }
        JSONObject visitDetailsJson = new JSONObject();

        try {
          // check company is matching with master visit_custom_list1
          if (patientRecord.getPrepopulateVisitInfo().getVisitCustomList1() != null) {
            visitDetailsJson.put("visit_custom_list1",
                patientRecord.getPrepopulateVisitInfo().getVisitCustomList1().getDescription());
          } else {
            response.setStatus(HttpStatus.SC_BAD_REQUEST);
            patientResponse.setStatus(STATUS_FAIL);
            patientResponseErrors.setVisitCustomList1(
                "visit_custom_list1 is required and description tag can't be empty");
          }
          // check location is matching with master visit_custom_list2
          if (patientRecord.getPrepopulateVisitInfo().getVisitCustomList2() != null) {
            visitDetailsJson.put("visit_custom_list2",
                patientRecord.getPrepopulateVisitInfo().getVisitCustomList2().getDescription());
          } else {
            response.setStatus(HttpStatus.SC_BAD_REQUEST);
            patientResponse.setStatus(STATUS_FAIL);
            patientResponseErrors.setVisitCustomList2("visit_custom_list2 is required");
          }
          // visitCustomField1 : Cost Center
          if (patientRecord.getPrepopulateVisitInfo().getVisitCustomField1() != null) {
            visitDetailsJson.put("visit_custom_field1",
                patientRecord.getPrepopulateVisitInfo().getVisitCustomField1());
          } else {
            response.setStatus(HttpStatus.SC_BAD_REQUEST);
            patientResponse.setStatus(STATUS_FAIL);
            patientResponseErrors.setVisitCustomField1("visit_custom_field1 is required");
          }
          // visitCustomField2 : Department
          if (patientRecord.getPrepopulateVisitInfo().getVisitCustomField2() != null) {
            visitDetailsJson.put("visit_custom_field2",
                patientRecord.getPrepopulateVisitInfo().getVisitCustomField2());
          } else {
            response.setStatus(HttpStatus.SC_BAD_REQUEST);
            patientResponse.setStatus(STATUS_FAIL);
            patientResponseErrors.setVisitCustomField2("visit_custom_field2 is required");
          }
          // visitCustomField3 : Job Title
          if (patientRecord.getPrepopulateVisitInfo().getVisitCustomField3() != null) {
            visitDetailsJson.put("visit_custom_field3",
                patientRecord.getPrepopulateVisitInfo().getVisitCustomField3());
          } else {
            response.setStatus(HttpStatus.SC_BAD_REQUEST);
            patientResponse.setStatus(STATUS_FAIL);
            patientResponseErrors.setVisitCustomField3("visit_custom_field3 is required");
          }
        } catch (JSONException ex) {
          logger.error("", ex);
        }

        patientDemography.put("patient", patientDetails);
        if (patientResponse.getStatus().equals(STATUS_SUCCESS)) {
          BasicDynaBean patientBean = patientDetailsService.getBean();

          registrationService.copyToPatientDemographyBean(patientDemography, patientBean);
          if (!mrNo.isEmpty()) {
            patientResponse.setSyncOperation("update");
            try {
              registrationService.updatePatientDemography(patientDemography);
            } catch (NoSuchAlgorithmException ex) {
              logger.error("", ex);
            } catch (IOException ex) {
              logger.error("", ex);
            } catch (ParseException ex) {
              logger.error("", ex);
            } catch (ValidationException returnValidationErrors) {
              if (returnValidationErrors != null) {
                response.setStatus(HttpStatus.SC_BAD_REQUEST);
                patientResponse.setStatus(STATUS_FAIL);
                patientResponseErrors
                    .setRemark(returnValidationErrors.getFormattedErrors().replace("<br/>", "\n"));
                patientResponse.setErrors(patientResponseErrors);
                return patientResponse;
              }
            }

          } else {
            patientResponse.setSyncOperation("create");
            ValidationErrorMap validationErrors = new ValidationErrorMap();
            if (registrationValidator.validatePatientDemographyNewVisit(patientBean,
                validationErrors)) {
              registrationService.insertPatientDemography(patientDemography, patientBean);
            } else if (validationErrors != null) {
              ValidationException returnValidationErrors = new ValidationException(
                  validationErrors);
              response.setStatus(HttpStatus.SC_BAD_REQUEST);
              patientResponse.setStatus(STATUS_FAIL);
              patientResponseErrors
                  .setRemark(returnValidationErrors.getFormattedErrors().replace("<br/>", "\n"));
              patientResponse.setErrors(patientResponseErrors);
              return patientResponse;

            }
          }
          patientResponse.setOldMrNo(oldMrNo);
          BasicDynaBean getMrnoBean = patientDetailsService.getMrNoWithOldMrNoBean(oldMrNo);
          if (getMrnoBean != null) {
            String newMrNo = (String) getMrnoBean.get("mr_no");
            patientResponse.setMrNo(newMrNo);
            BasicDynaBean prepopulateVisitBean = prepopulateVisitInfoRepository.findByKey("mr_no",
                newMrNo);
            if (prepopulateVisitBean != null) {
              prepopulateVisitBean.set("visit_values", visitDetailsJson.toString());
              Map<String, Object> updateKeys = new HashMap<String, Object>();
              updateKeys.put("mr_no", newMrNo);
              prepopulateVisitInfoService.update(prepopulateVisitBean, updateKeys);
            } else {
              BasicDynaBean prepopulateVisitInsertBean = prepopulateVisitInfoRepository.getBean();
              prepopulateVisitInsertBean.set("mr_no", newMrNo);
              prepopulateVisitInsertBean.set("visit_values", visitDetailsJson.toString());
              prepopulateVisitInfoService.insert(prepopulateVisitInsertBean);
            }
          }
        }
      } else {
        patientResponse.setStatus(STATUS_FAIL);
        patientResponseErrors.setRemark("Patient Details are missing");
        response.setStatus(HttpStatus.SC_BAD_REQUEST);

      }
    } else {
      patientResponse.setStatus(STATUS_FAIL);
      response.setStatus(HttpStatus.SC_NOT_FOUND);
    }

    if (patientResponse.getStatus() == null || patientResponse.getStatus().equals(STATUS_FAIL)) {
      patientResponse.setErrors(patientResponseErrors);
    } else {
      response.setStatus(HttpStatus.SC_OK);
      patientResponse.setStatus(STATUS_SUCCESS);
    }
    return patientResponse;

  }

  /**
   * Checks if is ip allowed.
   *
   * @param ip the ip
   * @return true, if is ip allowed
   */
  public boolean isIpAllowed(String ip,String usedFor) {
    List<BasicDynaBean> whitelistedIps = instaIntegrationRepository.getWhitelistedIps(usedFor);
    if (whitelistedIps != null && !whitelistedIps.isEmpty() && !StringUtil.isNullOrEmpty(ip)) {
      for (BasicDynaBean whitelistedIp : whitelistedIps) {
        String ipStart = (String) whitelistedIp.get("ip_start");
        String ipEnd = (String) whitelistedIp.get("ip_end");

        if (StringUtil.isNullOrEmpty(ipEnd)) {
          if (!StringUtil.isNullOrEmpty(ipStart) && ip.equals(ipStart)) {
            return true;
          }
        } else {
          if (!StringUtil.isNullOrEmpty(ipStart)) {
            if (isValidRange(ipStart, ipEnd, ip)) {
              logger.info("IP " + ip + " is valid as per range " + ipStart + " - " + ipEnd);
              return true;
            }
          }
        }
      }

      return false;
    } else {
      return false;
    }

  }

  /**
   * ipToLong.
   *
   * @param ip the ip
   * @return the long
   */
  public static long ipToLong(InetAddress ip) {
    byte[] octets = ip.getAddress();
    long result = 0;
    for (byte octet : octets) {
      result <<= 8;
      result |= octet & 0xff;
    }
    return result;
  }

  /**
   * isValidRange.
   *
   * @param ipStart the ip start
   * @param ipEnd the ip end
   * @param ipToCheck the ip to check
   * @return true, if is valid range
   */
  public static boolean isValidRange(String ipStart, String ipEnd, String ipToCheck) {
    try {
      long ipLo = ipToLong(InetAddress.getByName(ipStart));
      long ipHi = ipToLong(InetAddress.getByName(ipEnd));
      long ipToTest = ipToLong(InetAddress.getByName(ipToCheck));
      return (ipToTest >= ipLo && ipToTest <= ipHi);
    } catch (UnknownHostException ex) {
      ex.printStackTrace();
      return false;
    }
  }
}