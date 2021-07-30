package com.insta.hms.integration.practo.doctorprofile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.http.HttpResponse;
import com.insta.hms.integration.practo.api.util.HttpClientUtil;
import com.insta.hms.integration.practo.api.util.URLConstants;
import com.insta.hms.integration.practo.centerprofile.CenterProfileMappingService;
import com.insta.hms.integration.practo.pojos.Abt_timings;
import com.insta.hms.integration.practo.pojos.Added;
import com.insta.hms.integration.practo.pojos.Doctor;
import com.insta.hms.integration.practo.pojos.DoctorMapping;
import com.insta.hms.integration.practo.pojos.DoctorPractice;
import com.insta.hms.integration.practo.pojos.DoctorSpecializations;
import com.insta.hms.integration.practo.pojos.Friday;
import com.insta.hms.integration.practo.pojos.Monday;
import com.insta.hms.integration.practo.pojos.Saturday;
import com.insta.hms.integration.practo.pojos.Specializations;
import com.insta.hms.integration.practo.pojos.SpecializationsWrapper;
import com.insta.hms.integration.practo.pojos.Sub_specialities;
import com.insta.hms.integration.practo.pojos.Sunday;
import com.insta.hms.integration.practo.pojos.Thursday;
import com.insta.hms.integration.practo.pojos.Tuesday;
import com.insta.hms.integration.practo.pojos.Visit_timings;
import com.insta.hms.integration.practo.pojos.Wednesday;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.BasicDynaClass;
import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaClass;
import org.apache.commons.beanutils.DynaProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class DoctorProfileMappingService.
 *
 * @author insta
 */
public class DoctorProfileMappingService {

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(DoctorProfileMappingService.class);

  /** The mapping dao. */
  private DoctorProfileMappingDao mappingDao = null;
  
  /** The center profile mapping service. */
  private CenterProfileMappingService centerProfileMappingService = null;

  /**
   * Gets the doc listing.
   *
   * @param requestParams the request params
   * @return the doc listing
   * @throws ParseException the parse exception
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public PagedList getDocListing(Map requestParams)
      throws ParseException, SQLException, IOException {

    if (mappingDao == null) {
      mappingDao = new DoctorProfileMappingDao();
    }
    PagedList list = mappingDao.getDocListing(requestParams,
        ConversionUtils.getListingParameter(requestParams));

    if (centerProfileMappingService == null) {
      centerProfileMappingService = new CenterProfileMappingService();
    }
    String hospitalGrpId = centerProfileMappingService.getHospitalGrpId();
    List<BasicDynaBean> dtoList = list.getDtoList();
    for (BasicDynaBean object : dtoList) {
      String mappedPracticeId = String.format("%s.%s", hospitalGrpId,
          (Integer) object.get("cen_id"));
      String mappedDoctorId = String.format("%s.%s", hospitalGrpId,
          (String) object.get("doctor_id"));
      if (checkIfDoctorCenterMapped(mappedPracticeId, mappedDoctorId)) {
        object.set("status", "Y");
      } else {
        object.set("status", "N");
      }
    }
    return list;
  }

  /**
   * Gets the center publish status.
   *
   * @param list the list
   * @return the center publish status
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public Map<Integer, String> getCenterPublishStatus(PagedList list)
      throws SQLException, IOException {
    Map<Integer, String> map = new HashMap<Integer, String>();
    List<BasicDynaBean> dtoList = list.getDtoList();
    if (centerProfileMappingService == null) {
      centerProfileMappingService = new CenterProfileMappingService();
    }
    String hospGrpId = centerProfileMappingService.getHospitalGrpId();
    for (BasicDynaBean object : dtoList) {
      Integer centerId = (Integer) object.get("cen_id");
      String practiceId = String.format("%s.%s", hospGrpId, centerId);
      String status = "";
      if (map.get(centerId) == null) {
        if (centerProfileMappingService.checkIfCenterMapped(practiceId)) {
          status = "Y";
        } else {
          status = "N";
        }
        map.put(centerId, status);
      }
    }
    return map;
  }

  /**
   * Gets the doctor specializations.
   *
   * @return the doctor specializations
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public List<DynaBean> getDoctorSpecializations() throws IOException {

    Map<String, String> headerMap = new HashMap<String, String>();
    headerMap.put("Accept", "application/json");
    HttpResponse response = HttpClientUtil.sendHttpGetRequest(
        URLConstants.PRACTO_FABRIC_API_DOCTOR_SPECIALIZATIONS, headerMap,
        "with_subspecialities=true");
    List<DynaBean> outerDynaBeans = null;
    if (response.getCode() == 200) {
      String jsonStr = response.getMessage();
      ObjectMapper mapper = new ObjectMapper();
      SpecializationsWrapper specializations = mapper.readValue(jsonStr,
          SpecializationsWrapper.class);
      outerDynaBeans = new ArrayList<DynaBean>();
      for (Specializations specialization : specializations.getSpecializations()) {
        if (specialization.getSub_specialities() != null
            && specialization.getSub_specialities().length != 0) {
          DynaClass innerDynaClass = new BasicDynaClass("json", null,
              new DynaProperty[] { new DynaProperty("id"), new DynaProperty("subspecialization") });

          List<DynaBean> innerDynaBeans = new ArrayList<DynaBean>();
          DynaClass outerDynaClass = new BasicDynaClass("json1", null, new DynaProperty[] {
              new DynaProperty("speciality"), new DynaProperty("sub_specialities") });
          BasicDynaBean outerDynaBean = new BasicDynaBean(outerDynaClass);
          for (Sub_specialities subSpecialities : specialization.getSub_specialities()) {
            BasicDynaBean innerDynaBean = new BasicDynaBean(innerDynaClass);
            innerDynaBean.set("id", subSpecialities.getId());
            innerDynaBean.set("subspecialization", subSpecialities.getSubspecialization());
            innerDynaBeans.add(innerDynaBean);
          }
          outerDynaBean.set("speciality", specialization.getSpeciality());
          outerDynaBean.set("sub_specialities", innerDynaBeans);
          outerDynaBeans.add(outerDynaBean);
        }
      }

    }
    return outerDynaBeans;
  }

  /**
   * Publish doctors.
   *
   * @param requestParams the request params
   * @param list the list
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public void publishDoctors(Map requestParams, PagedList list) throws SQLException, IOException {

    List<String> docsToPublish = Arrays.asList((String[]) requestParams.get("_selectDoctor"));
    for (String doctor : docsToPublish) {

      if (centerProfileMappingService == null) {
        centerProfileMappingService = new CenterProfileMappingService();
      }
      String hospGrpId = centerProfileMappingService.getHospitalGrpId();
      String doctorId = doctor.split("_")[0];
      String centerName = doctor.split("_")[1];
      logger.debug("DoctorId = " + doctorId);
      String mappedDocId = String.format("%s.%s", hospGrpId, doctorId);

      if (!checkIfDoctorMapped(mappedDocId)) {
        HttpResponse response = mapDoctorToFabric(requestParams, list, doctor, doctorId,
            mappedDocId);
        if (response.getCode() == 200) {
          mapPracticeDoctorRelationToFabric(list, doctorId, mappedDocId, hospGrpId, centerName);
        } else {
          logger.error("Error in doctor mapping");
          logger.error("Http Status Code: " + response.getCode());
          logger.error("Message: " + response.getMessage());
        }
      } else {
        mapPracticeDoctorRelationToFabric(list, doctorId, mappedDocId, hospGrpId, centerName);
      }
    }

  }

  /**
   * Map practice doctor relation to fabric.
   *
   * @param list the list
   * @param doctorId the doctor id
   * @param mappedDocId the mapped doc id
   * @param hospGrpId the hosp grp id
   * @param centerName the center name
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws JsonProcessingException the json processing exception
   */
  private void mapPracticeDoctorRelationToFabric(PagedList list, String doctorId,
      String mappedDocId, String hospGrpId, String centerName)
      throws IOException, JsonProcessingException {
    HttpResponse response;
    String centerId = null;
    List<BasicDynaBean> dtoList = list.getDtoList();
    for (BasicDynaBean bean : dtoList) {
      if (bean.get("doctor_id").toString().equals(doctorId)
          && bean.get("center_name").toString().equals(centerName)) {
        centerId = String.valueOf((Integer) bean.get("cen_id"));
        break;
      }
    }

    String mappedPracticeId = String.format("%s.%s", hospGrpId, centerId);

    String mappedPractoPracticeId = centerProfileMappingService
        .getMappedPractoPracticeId(mappedPracticeId);
    String mappedPractoDocId = getMappedPractoDoctorId(mappedDocId);

    if (mappedPractoPracticeId != null && mappedPractoDocId != null) {
      DoctorPractice doctorPractice = new DoctorPractice();

      doctorPractice.setMapped_doctor_id(mappedDocId);
      doctorPractice.setMapped_practice_id(mappedPracticeId);
      doctorPractice.setMapped_service(URLConstants.PRACTO_INSTA_MAPPED_SERVICE);
      doctorPractice.setDoctor_id(mappedPractoDocId);
      doctorPractice.setPractice_id(mappedPractoPracticeId);

      // the below five fields are mandatory so setting some default values
      doctorPractice.setProfile_published("false");
      doctorPractice.setConsultation_fee("0");
      doctorPractice.setResident_doctor("false");
      doctorPractice.setOn_call("false");
      doctorPractice.setStatus("ABS");

      Monday monday = new Monday();
      monday.setSession1_start_time("09:00");
      monday.setSession1_end_time("21:00");
      monday.setSession2_start_time("");
      monday.setSession2_end_time("");

      Tuesday tuesday = new Tuesday();
      tuesday.setSession1_start_time("09:00");
      tuesday.setSession1_end_time("21:00");
      tuesday.setSession2_start_time("");
      tuesday.setSession2_end_time("");

      Wednesday wednesday = new Wednesday();
      wednesday.setSession1_start_time("09:00");
      wednesday.setSession1_end_time("21:00");
      wednesday.setSession2_start_time("");
      wednesday.setSession2_end_time("");

      Thursday thursday = new Thursday();
      thursday.setSession1_start_time("09:00");
      thursday.setSession1_end_time("21:00");
      thursday.setSession2_start_time("");
      thursday.setSession2_end_time("");

      Friday friday = new Friday();
      friday.setSession1_start_time("09:00");
      friday.setSession1_end_time("21:00");
      friday.setSession2_start_time("");
      friday.setSession2_end_time("");

      Saturday saturday = new Saturday();
      saturday.setSession1_start_time("09:00");
      saturday.setSession1_end_time("21:00");
      saturday.setSession2_start_time("");
      saturday.setSession2_end_time("");

      Sunday sunday = new Sunday();
      sunday.setSession1_start_time("09:00");
      sunday.setSession1_end_time("21:00");
      sunday.setSession2_start_time("");
      sunday.setSession2_end_time("");

      Visit_timings visitTimings = new Visit_timings();
      visitTimings.setMonday(monday);
      visitTimings.setTuesday(tuesday);
      visitTimings.setWednesday(wednesday);
      visitTimings.setThursday(thursday);
      visitTimings.setFriday(friday);
      visitTimings.setSaturday(saturday);
      visitTimings.setSunday(sunday);

      Abt_timings abtTimings = new Abt_timings();
      abtTimings.setMonday(monday);
      abtTimings.setTuesday(tuesday);
      abtTimings.setWednesday(wednesday);
      abtTimings.setThursday(thursday);
      abtTimings.setFriday(friday);
      abtTimings.setSaturday(saturday);
      abtTimings.setSunday(sunday);

      doctorPractice.setVisit_timings(visitTimings);
      doctorPractice.setAbt_timings(abtTimings);

      ObjectMapper mapper = new ObjectMapper();
      String postBody = mapper.writeValueAsString(doctorPractice);

      logger.debug(postBody);

      Map<String, String> headerMap = new HashMap<String, String>();
      headerMap.put("Accept", "application/json");
      headerMap.put("Content-Type", "application/json");
      headerMap.put(URLConstants.PRACTO_FABRIC_API_AUTH_TOKEN_NAME,
          URLConstants.PRACTO_FABRIC_API_AUTH_TOKEN_VALUE);

      response = HttpClientUtil.sendHttpPostRequest(
          URLConstants.PRACTO_PRACTICE_DOCTOR_FABRIC_PUT_ENDPOINT, headerMap, postBody);

      logger.debug("Response Code: " + response.getCode());
    }
  }

  /**
   * Map doctor to fabric.
   *
   * @param requestParams the request params
   * @param list the list
   * @param doctor the doctor
   * @param doctorId the doctor id
   * @param mappedDocId the mapped doc id
   * @return the http response
   * @throws JsonProcessingException the json processing exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private HttpResponse mapDoctorToFabric(Map requestParams, PagedList list, String doctor,
      String doctorId, String mappedDocId) throws JsonProcessingException, IOException {

    String specializationId = ((String[]) requestParams.get("_specialization_" + doctor))[0];
    logger.debug("specialization Id= " + specializationId);

    String doctorName = "";
    String doctorAddr = "";

    // String primaryContact = "";
    String primaryEmailId = "";

    List<BasicDynaBean> dtoList = list.getDtoList();
    for (BasicDynaBean bean : dtoList) {
      if (bean.get("doctor_id").toString().equals(doctorId)) {
        doctorName = (String) bean.get("doctor_name");
        doctorAddr = (String) bean.get("doctor_address");
        // primaryContact = (String) bean.get("doctor_mobile");
        primaryEmailId = (String) bean.get("doctor_mail_id");
        break;
      }
    }
    Doctor practoDoctor = new Doctor();
    practoDoctor.setName(doctorName);

    // Not setting the Primary contact number for now, because it should be a valid phone number
    // with country code, and
    // If we set the Primary Contact, then we need to set the city Id
    // practoDoctor.setPrimary_contact_number(primaryContact);

    practoDoctor.setPrimary_email_address(primaryEmailId);
    practoDoctor.setStreet_address(doctorAddr);
    // TODO check whether the value for source is appropriate or not
    practoDoctor.setSource("Insta");
    practoDoctor.setSource_description("");
    practoDoctor.setMapped_service(URLConstants.PRACTO_INSTA_MAPPED_SERVICE);
    practoDoctor.setMapped_doctor_id(mappedDocId);

    // Specializations
    Added[] addedArr = new Added[1];
    Added added = new Added();
    added.setSubspecialization_id(specializationId);

    addedArr[0] = added;
    DoctorSpecializations specializations = new DoctorSpecializations();
    specializations.setAdded(addedArr);

    practoDoctor.setSpecializations(specializations);

    // TODO Need to check wht value to be set whether UNVERIFIED or VERIFIED
    practoDoctor.setVerification_status("UNVERIFIED");

    ObjectMapper mapper = new ObjectMapper();
    String postBody = mapper.writeValueAsString(practoDoctor);

    logger.debug(postBody);

    Map<String, String> headerMap = new HashMap<String, String>();
    headerMap.put("Accept", "application/json");
    headerMap.put("Content-Type", "application/json");
    headerMap.put(URLConstants.PRACTO_FABRIC_API_AUTH_TOKEN_NAME,
        URLConstants.PRACTO_FABRIC_API_AUTH_TOKEN_VALUE);

    HttpResponse response = HttpClientUtil
        .sendHttpPostRequest(URLConstants.PRACTO_DOCTOR_FABRIC_PUT_ENDPOINT, headerMap, postBody);

    logger.debug("Response Code: " + response.getCode());

    return response;

  }

  /**
   * Check if doctor mapped.
   *
   * @param mappedDoctorId the mapped doctor id
   * @return the boolean
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public Boolean checkIfDoctorMapped(String mappedDoctorId) throws IOException {

    Boolean isDoctorMapped = false;
    Map<String, String> headerMap = new HashMap<String, String>();
    headerMap.put("Accept", "application/json");
    String urlParams = String.format("mapped_service=%s&mapped_doctor_id=%s",
        URLConstants.PRACTO_INSTA_MAPPED_SERVICE, mappedDoctorId);
    HttpResponse response = HttpClientUtil
        .sendHttpGetRequest(URLConstants.PRACTO_DOCTOR_MAPPING_GET_ENDPOINT, headerMap, urlParams);
    if (response.getCode() == 404 && response.getMessage().equals("Not Found")) {
      isDoctorMapped = false;
    } else if (response.getCode() == 200) {
      String responseJson = response.getMessage();
      ObjectMapper mapper = new ObjectMapper();
      DoctorMapping mapping = mapper.readValue(responseJson, DoctorMapping.class);
      if (mapping.getMapped_doctor_id().equals(mappedDoctorId)
          && mapping.getMapped_service().equals(URLConstants.PRACTO_INSTA_MAPPED_SERVICE)) {
        isDoctorMapped = true;
      }
    }

    return isDoctorMapped;

  }

  /**
   * Gets the mapped practo doctor id.
   *
   * @param mappedDoctorId the mapped doctor id
   * @return the mapped practo doctor id
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public String getMappedPractoDoctorId(String mappedDoctorId) throws IOException {

    String mappedPractoDocId = null;

    Map<String, String> headerMap = new HashMap<String, String>();
    headerMap.put("Accept", "application/json");
    String urlParams = String.format("mapped_service=%s&mapped_doctor_id=%s",
        URLConstants.PRACTO_INSTA_MAPPED_SERVICE, mappedDoctorId);
    HttpResponse response = HttpClientUtil
        .sendHttpGetRequest(URLConstants.PRACTO_DOCTOR_MAPPING_GET_ENDPOINT, headerMap, urlParams);
    if (response.getCode() == 404 && response.getMessage().equals("Not Found")) {
      logger.debug("Resource Not Found: " + mappedDoctorId);
    } else if (response.getCode() == 200) {
      String responseJson = response.getMessage();
      ObjectMapper mapper = new ObjectMapper();
      DoctorMapping mapping = mapper.readValue(responseJson, DoctorMapping.class);
      if (mapping.getMapped_doctor_id().equals(mappedDoctorId)
          && mapping.getMapped_service().equals(URLConstants.PRACTO_INSTA_MAPPED_SERVICE)) {
        mappedPractoDocId = mapping.getPracto_doctor_id();
      }
    }

    return mappedPractoDocId;

  }

  /**
   * Check if doctor center mapped.
   *
   * @param mappedPracticeId the mapped practice id
   * @param mappedDoctorId the mapped doctor id
   * @return the boolean
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public Boolean checkIfDoctorCenterMapped(String mappedPracticeId, String mappedDoctorId)
      throws IOException {

    Boolean isDoctorCenterMapped = false;
    Map<String, String> headerMap = new HashMap<String, String>();
    headerMap.put("Accept", "application/json");
    String urlParams = String.format("mapped_service=%s&mapped_practice_id=%s&mapped_doctor_id=%s",
        URLConstants.PRACTO_INSTA_MAPPED_SERVICE, mappedPracticeId, mappedDoctorId);
    HttpResponse response = HttpClientUtil.sendHttpGetRequest(
        URLConstants.PRACTO_PRACTICE_DOCTOR_MAPPING_GET_ENDPOINT, headerMap, urlParams);
    if (response.getCode() == 404 && response.getMessage().equals("Not Found")) {
      isDoctorCenterMapped = false;
    } else if (response.getCode() == 200) {
      isDoctorCenterMapped = true;
    }
    return isDoctorCenterMapped;
  }

}
