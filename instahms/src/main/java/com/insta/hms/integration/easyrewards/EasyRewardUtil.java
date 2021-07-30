package com.insta.hms.integration.easyrewards;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.insta.hms.billing.PaytmResponseHandler;
import com.insta.hms.common.http.HttpClient;
import com.insta.hms.common.http.HttpResponse;
import com.insta.hms.exception.ValidationException;
import com.insta.hms.messaging.InstaIntegrationDao;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("deprecation")
@Component
public class EasyRewardUtil {

  /** The logger. */
  private static Logger logger = LoggerFactory.getLogger(EasyRewardUtil.class);

  /**
   * To get the coupon redemption.
   *
   * @param easyRewardRequest is the input object
   * @return is the EasyRewardResponse
   * @throws Exception the exception
   */
  public EasyRewardResponse getCouponRedemption(EasyRewardRequest easyRewardRequest)
              throws Exception {

    // 1. First we need to generate the security token
    EasyRewardResponse tokenResponse = handler(EasyRewardConstant.EASY_REWARDS_GENERATE_TOKEN_API,
                easyRewardRequest);

    if (tokenResponse != null && StringUtils.isEmpty(tokenResponse.getToken())) {
      // If we didnt get the token in the response we need to throw error
      String returnMessage = "";
      if (StringUtils.isNotEmpty(tokenResponse.getReturnMessage())) {
        returnMessage = "\n" + tokenResponse.getReturnMessage();
      }
      throw new ValidationException(EasyRewardConstant.EXCEPTION_EASYREWARDZ_LOGIN_FAILED,
                  new String[] { returnMessage });
    }

    easyRewardRequest.setToken(tokenResponse.getToken());

    // 2. Generate the Widget URL
    EasyRewardResponse widgetResponse = handler(EasyRewardConstant.EASY_REWARDS_WIDGET_API,
                easyRewardRequest);

    // If we didn't get the widget url in the response we need to throw error
    if (widgetResponse != null && StringUtils.isEmpty(widgetResponse.getWidgetUrl())) {
      String returnMessage = "";
      if (StringUtils.isNotEmpty(widgetResponse.getReturnMessage())) {
        returnMessage = widgetResponse.getReturnMessage();

        if (EasyRewardConstant.API_ERROR_RESPONSE_MISSING_PHONE_NUMBER
                    .equalsIgnoreCase(returnMessage)) {
          throw new ValidationException(
                      EasyRewardConstant.EXCEPTION_EASYREWARDZ_INVALID_PHONE_NUMBER);
        }

        if (EasyRewardConstant.API_ERROR_RESPONSE_STORE_CODE_NOT_EXIST
                    .equalsIgnoreCase(returnMessage)) {
          throw new ValidationException(
                      EasyRewardConstant.EXCEPTION_EASYREWARDZ_INVALID_STORE_CODE);
        }
        returnMessage = "\n" + widgetResponse.getReturnMessage();
      }
      throw new ValidationException(EasyRewardConstant.EXCEPTION_EASYREWARDZ_LOGIN_FAILED,
                  new String[] { returnMessage });
    }
    return widgetResponse;
  }

  private EasyRewardResponse handler(String integrationName, EasyRewardRequest easyRewardRequest)
              throws Exception {

    if (StringUtils.isNotEmpty(integrationName) && easyRewardRequest != null) {

      BasicDynaBean instaIntegration = getIntegration(integrationName);

      if (instaIntegration == null) {
        throw new ValidationException(EasyRewardConstant.EXCEPTION_EASYREWARDZ_CONFIGURATION);
      }

      Integer integrationId = (Integer) instaIntegration.get("integration_id");
      String url = (String) instaIntegration.get("url");

      if (integrationId == null || StringUtils.isEmpty(url)) {
        throw new ValidationException(EasyRewardConstant.EXCEPTION_EASYREWARDZ_CONFIGURATION);
      }

      BasicDynaBean centerIntegrationDetails = getCenterIntegrationDetails(integrationId);

      if (centerIntegrationDetails == null) {
        throw new ValidationException(EasyRewardConstant.EXCEPTION_EASYREWARDZ_CONFIGURATION);
      }

      String httpBody = (String) centerIntegrationDetails.get("http_body");
      if (StringUtils.isEmpty(httpBody)) {
        throw new ValidationException(EasyRewardConstant.EXCEPTION_EASYREWARDZ_CONFIGURATION);
      }

      EasyRewardResponse response = callApi(integrationName, url, httpBody, easyRewardRequest);
      if (response == null) {
        throw new ValidationException(EasyRewardConstant.EXCEPTION_EASYREWARDZ_API_RESPONSE_EMPTY);
      }

      return response;
    }
    return null;
  }

  /**
   * To get the Insta Integration.
   *
   * @param integrationName is the integration name
   * @return the DB BasicDynaBean object
   * @throws Exception the SQL exception
   */
  private BasicDynaBean getIntegration(String integrationName) throws Exception {

    if (StringUtils.isNotEmpty(integrationName)) {

      BasicDynaBean instaIntegration = new InstaIntegrationDao().getActiveBean(integrationName);
      if (instaIntegration != null) {
        return instaIntegration;
      }
    }
    return null;
  }

  /**
   * To get the Center Integration Details.
   *
   * @param integrationId is the integration id
   * @return the DB BasicDynaBean object
   * @throws Exception the SQL exception
   */
  private BasicDynaBean getCenterIntegrationDetails(Integer integrationId) throws Exception {

    if (integrationId != null && integrationId > 0) {

      BasicDynaBean centerIntegrationDetails = new InstaIntegrationDao()
                  .getIntegrationDetailForCenter(integrationId);
      if (centerIntegrationDetails != null) {
        return centerIntegrationDetails;
      }
    }
    return null;
  }

  /**
   * It will populate the params and call the URL.
   *
   * @param integrationName the integration name
   * @param url             the url
   * @param httpBody        the http body content
   * @param request         the EasyRewardRequest object
   * @return the EasyRewardResponse
   * @throws Exception the IOException, JSONMappingException
   */
  private EasyRewardResponse callApi(String integrationName, String url, String httpBody,
              EasyRewardRequest request) throws Exception {

    if (StringUtils.isNotEmpty(integrationName) && StringUtils.isNotEmpty(url)
                && StringUtils.isNotEmpty(httpBody) && request != null) {

      ObjectMapper mapper = new ObjectMapper();
      mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
      Map<String, String> headerMap = new HashMap<>();
      headerMap.put("Content-Type", "application/json");
      Map<String, Object> reqMap = new HashMap<>();
      reqMap = mapper.readValue(httpBody, new TypeReference<Map<String, Object>>() {
      });

      // We need to set the parameters only for the Widget API
      if (EasyRewardConstant.EASY_REWARDS_WIDGET_API.equalsIgnoreCase(integrationName)) {

        if (reqMap != null && !reqMap.isEmpty()) {

          if (StringUtils.isNotEmpty(request.getToken())) {
            reqMap.put(EasyRewardConstant.SECURITYTOKEN, request.getToken());
          }

          if (StringUtils.isNotEmpty(request.getMobileNumber())) {
            reqMap.put(EasyRewardConstant.MEMBERID, request.getMobileNumber());
          }

          if (StringUtils.isNotEmpty(request.getCenterId())) {
            reqMap.put(EasyRewardConstant.STORECODE, request.getCenterId());
          }

          if (StringUtils.isNotEmpty(request.getBillNumber())) {
            reqMap.put(EasyRewardConstant.BILLGUID, request.getBillNumber());
          }
        }
      }

      EasyRewardResponse response = null;
      try {
        String postDataStr = mapper.writeValueAsString(reqMap);
        HttpResponse httpResponse = new HttpClient(new PaytmResponseHandler(), 10000, 10000)
                    .post(url, postDataStr, headerMap);
        logger.debug("Sent POST request: " + reqMap + "\n and " + " got response :  "
                    + httpResponse.getMessage());
        response = mapper.readValue(httpResponse.getMessage(), EasyRewardResponse.class);
      } catch (JsonProcessingException ex) {
        logger.error("Unable to parse respose json: = " + ex.getMessage(), ex);
      }

      return response;
    }
    return null;
  }
}