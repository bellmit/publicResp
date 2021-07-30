package com.insta.hms.integration.practo.centerprofile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.http.HttpResponse;
import com.insta.hms.integration.practo.api.util.HttpClientUtil;
import com.insta.hms.integration.practo.api.util.URLConstants;
import com.insta.hms.integration.practo.pojos.Localities;
import com.insta.hms.integration.practo.pojos.LocalitiesWrapper;
import com.insta.hms.integration.practo.pojos.Monday;
import com.insta.hms.integration.practo.pojos.Practice;
import com.insta.hms.integration.practo.pojos.PracticeMapping;
import com.insta.hms.integration.practo.pojos.Timings;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * The Class CenterProfileMappingService.
 */
public class CenterProfileMappingService {

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(CenterProfileMappingService.class);

  /** The mapping dao. */
  CenterProfileMappingDao mappingDao = null;

  /**
   * Gets the centers paged list.
   *
   * @param request the request
   * @return the centers paged list
   * @throws SQLException the SQL exception
   * @throws ParseException the parse exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public PagedList getCentersPagedList(HttpServletRequest request)
      throws SQLException, ParseException, IOException {

    if (mappingDao == null) {
      mappingDao = new CenterProfileMappingDao();
    }

    Map params = new HashMap(request.getParameterMap());
    Map<LISTING, Object> listingParams = ConversionUtils.getListingParameter(params);

    PagedList pagedList = mappingDao.searchCenters(params, listingParams);

    List<BasicDynaBean> dtoList = pagedList.getDtoList();
    for (BasicDynaBean object : dtoList) {
      if (checkIfCenterMapped(
          String.format("%s.%s", getHospitalGrpId(), (Integer) object.get("center_id")))) {
        object.set("status", "Y");
      } else {
        object.set("status", "N");
      }
    }
    return pagedList;
  }

  /**
   * Gets the cities master data.
   *
   * @return the cities master data
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public String getCitiesMasterData() throws IOException {

    Map<String, String> headerMap = new HashMap<String, String>();
    headerMap.put("Accept", "application/json");
    HttpResponse response = HttpClientUtil.sendHttpGetRequest(URLConstants.PRACTO_FABRIC_API_CITIES,
        headerMap, "");
    return response.getMessage();
  }

  /**
   * Publish centers.
   *
   * @param paramsMap the params map
   * @param pagedList the paged list
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public void publishCenters(Map paramsMap, PagedList pagedList) throws SQLException, IOException {

    List<String> selectedCenters = Arrays.asList((String[]) paramsMap.get("_selectCenter"));

    Practice practice = null;
    for (String center : selectedCenters) {

      logger.debug("City Name= " + ((String[]) paramsMap.get("_city_name_" + center))[0]);
      logger.debug("City Id= " + ((String[]) paramsMap.get("_city_id_" + center))[0]);
      logger.debug("Locality Id= " + ((String[]) paramsMap.get("_locality_id_" + center))[0]);

      practice = new Practice();
      String centerName = "";
      String centerAddr = "";
      logger.debug("CenterId = " + center);
      List<BasicDynaBean> dtoList = pagedList.getDtoList();
      for (BasicDynaBean bean : dtoList) {
        if (bean.get("center_id").toString().equals(center)) {
          centerName = (String) bean.get("center_name");
          centerAddr = (String) bean.get("center_address");
          break;
        }

      }
      String urlParams = "city_id=" + ((String[]) paramsMap.get("_city_id_" + center))[0];
      String localityId = ((String[]) paramsMap.get("_locality_id_" + center))[0];

      Map<String, String> headerMap = new HashMap<String, String>();
      headerMap.put("Accept", "application/json");

      HttpResponse httpResponse = HttpClientUtil
          .sendHttpGetRequest(URLConstants.PRACTO_FABRIC_API_LOCALITIES, headerMap, urlParams);
      String json = httpResponse.getMessage();
      ObjectMapper mapper = new ObjectMapper();
      LocalitiesWrapper localities = mapper.readValue(json, LocalitiesWrapper.class);
      String cityId = null;
      for (Localities fromLocality : localities.getLocalities()) {
        if (fromLocality.getId().equals(localityId)) {
          cityId = fromLocality.getCity().getId();
          break;
        }

      }
      practice.setLocality_id(localityId);
      practice.setCity_id(cityId);
      practice.setName(centerName);
      practice.setStreet_address(centerAddr);
      practice.setMapped_service(URLConstants.PRACTO_INSTA_MAPPED_SERVICE);
      practice.setMapped_practice_id(String.format("%s.%s", getHospitalGrpId(), center));
      practice.setLatitude("");
      practice.setLongitude("");
      practice.setWebsite("");
      practice.setTagline("");
      practice.setSummary("");

      Monday monday = new Monday();
      monday.setSession1_start_time("09:00");
      monday.setSession1_end_time("21:00");
      monday.setSession2_start_time("");
      monday.setSession2_end_time("");

      Timings timings = new Timings();
      timings.setMonday(monday);

      practice.setTimings(timings);
      practice.setVn_active("false");

      headerMap = new HashMap<String, String>();
      headerMap.put("Accept", "application/json");
      headerMap.put("Content-Type", "application/json");
      headerMap.put(URLConstants.PRACTO_FABRIC_API_AUTH_TOKEN_NAME,
          URLConstants.PRACTO_FABRIC_API_AUTH_TOKEN_VALUE);
      String postBody = mapper.writeValueAsString(practice);

      HttpResponse response = HttpClientUtil.sendHttpPostRequest(
          URLConstants.PRACTO_PRACTICE_FABRIC_PUT_ENDPOINT, headerMap, postBody);
      logger.debug("Response Code: " + response.getCode());

    }
  }

  /**
   * Gets the hospital grp id.
   *
   * @return the hospital grp id
   * @throws SQLException the SQL exception
   */
  public String getHospitalGrpId() throws SQLException {
    if (mappingDao == null) {
      mappingDao = new CenterProfileMappingDao();
    }
    return mappingDao.getHospitalGroupId();
  }

  /**
   * Check if center mapped.
   *
   * @param mappedPracticeId the mapped practice id
   * @return the boolean
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public Boolean checkIfCenterMapped(String mappedPracticeId) throws IOException {

    Boolean isCenterMapped = false;
    Map<String, String> headerMap = new HashMap<String, String>();
    headerMap.put("Accept", "application/json");
    String urlParams = String.format("mapped_service=%s&mapped_practice_id=%s",
        URLConstants.PRACTO_INSTA_MAPPED_SERVICE, mappedPracticeId);
    HttpResponse response = HttpClientUtil.sendHttpGetRequest(
        URLConstants.PRACTO_PRACTICE_MAPPING_GET_ENDPOINT, headerMap, urlParams);
    if (response.getCode() == 404 && response.getMessage().equals("Not Found")) {
      isCenterMapped = false;
    } else if (response.getCode() == 200) {
      String responseJson = response.getMessage();
      ObjectMapper mapper = new ObjectMapper();
      PracticeMapping mapping = mapper.readValue(responseJson, PracticeMapping.class);
      if (mapping.getMapped_practice_id().equals(mappedPracticeId)
          && mapping.getMapped_service().equals(URLConstants.PRACTO_INSTA_MAPPED_SERVICE)) {
        isCenterMapped = true;
      }
    }

    return isCenterMapped;

  }

  /**
   * Gets the mapped practo practice id.
   *
   * @param mappedPracticeId the mapped practice id
   * @return the mapped practo practice id
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public String getMappedPractoPracticeId(String mappedPracticeId) throws IOException {

    String practoPracticeId = null;
    Map<String, String> headerMap = new HashMap<String, String>();
    headerMap.put("Accept", "application/json");
    String urlParams = String.format("mapped_service=%s&mapped_practice_id=%s",
        URLConstants.PRACTO_INSTA_MAPPED_SERVICE, mappedPracticeId);
    HttpResponse response = HttpClientUtil.sendHttpGetRequest(
        URLConstants.PRACTO_PRACTICE_MAPPING_GET_ENDPOINT, headerMap, urlParams);
    if (response.getCode() == 404 && response.getMessage().equals("Not Found")) {
      System.out.println("Resource Not Found: " + mappedPracticeId);
    } else if (response.getCode() == 200) {
      String responseJson = response.getMessage();
      ObjectMapper mapper = new ObjectMapper();
      PracticeMapping mapping = mapper.readValue(responseJson, PracticeMapping.class);
      if (mapping.getMapped_practice_id().equals(mappedPracticeId)
          && mapping.getMapped_service().equals(URLConstants.PRACTO_INSTA_MAPPED_SERVICE)) {
        practoPracticeId = mapping.getPracto_practice_id();
      }
    }

    return practoPracticeId;

  }

}
