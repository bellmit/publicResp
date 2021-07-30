package com.insta.hms.billing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.insta.hms.common.UrlUtil;
import com.insta.hms.common.http.HttpClient;
import com.insta.hms.common.http.HttpResponse;
import com.insta.hms.messaging.InstaIntegrationDao;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class OneApolloUtil extends LoyaltyCardUtil {

  static Logger logger = LoggerFactory
      .getLogger(OneApolloUtil.class);

  private static final int loyaltyCardModeId = -5;
  private static final String integrationName = "one_apollo_loyalty_card";
  private static final Map<String, String> statusCodesMap = new HashMap<String, String>();

  public static String requestForMoney(String mobileNo, String otp,
      String amount, String billNo, String apiType, String requestNumber)
      throws SQLException, JsonProcessingException {
    BasicDynaBean loyaltyCardDetails = new InstaIntegrationDao()
        .getActiveBean(integrationName);
    boolean status = false;

    if (loyaltyCardDetails == null) { // integration config not set
      return handleWalletBalanceResponse(status, "CONFIG_NOT_SET", "");
    }
    // In userid column we are expecting APIKey

    String userid = (String) loyaltyCardDetails.get("userid");
    if (userid == null || userid.isEmpty()) {
      return handleWalletBalanceResponse(status, "CONFIG_NOT_SET", "");
    }
    String aeskey = (String) loyaltyCardDetails.get("aeskey");
    if (aeskey == null || aeskey.isEmpty()) {
      return handleWalletBalanceResponse(status, "CONFIG_NOT_SET", "");
    }
    int integrationId = (Integer) loyaltyCardDetails.get("integration_id");
    BasicDynaBean integrationDetails = new InstaIntegrationDao()
        .getIntegrationDetailForCenter(integrationId);

    if (integrationDetails == null) { // center_integration_details not set
      return handleWalletBalanceResponse(status, "CONFIG_NOT_SET", "");
    }
    // Get businessUnit from center_integration_details table. storing businessUnit in
    // merchant_id column
    String businessUnit = (String) integrationDetails.get("merchant_id");
    if (businessUnit == null || businessUnit.isEmpty()) {
      return handleWalletBalanceResponse(status, "CONFIG_NOT_SET", "");
    }



    String storeId = (String) integrationDetails.get("store_code");
    if (storeId == null || storeId.isEmpty()) {
      return handleWalletBalanceResponse(status, "STORE_CODE_NOT_SET", "");
    }

    LinkedHashMap<String, Object> params = new LinkedHashMap<String, Object>();
    params.put("BusinessUnit", businessUnit);
    params.put("mobilenumber", mobileNo);
    String api = (String) loyaltyCardDetails.get("url");
    if (apiType.equalsIgnoreCase("GetByMobile")) {
      api = api + "/Customer/" + apiType;
      params.put("GetTierBenefits", "false");
    }
    if (apiType.equalsIgnoreCase("sendOTP")) {
      api = api + "/redemption/" + apiType;
      params.put("StoreCode", storeId);
      params.put("CreditsRedeemed", amount);
    }
    if (apiType.equalsIgnoreCase("validateOTP")) {
      api = api + "/redemption/" + apiType;
      params.put("StoreCode", storeId);
      params.put("RequestNumber", requestNumber);
      params.put("OTP", otp);
    }

    Map<String, String> headerMap = new HashMap<String, String>();
    headerMap.put("Content-Type", "application/json");
    headerMap.put("APIKey", userid);
    headerMap.put("AccessToken", aeskey);
    try {
      HttpResponse httpResponse = new HttpResponse(0, null);
      String postDataStr = null;
      PaytmResponseHandler paytmObj = new PaytmResponseHandler();
      // send HTTP request
      if (apiType.equalsIgnoreCase("GetByMobile")) {
        postDataStr = UrlUtil.buildQueryString(params);
        httpResponse = new HttpClient(paytmObj).get(api, postDataStr,
            headerMap);
      } else if (apiType.equalsIgnoreCase("sendOTP")
          || apiType.equalsIgnoreCase("validateOTP")) {
        postDataStr = new ObjectMapper().writeValueAsString(params);
        httpResponse = new HttpClient(paytmObj).post(api, postDataStr,
            headerMap);
      }
      logger.info("Sent POST request: " + params + "\n and got response :  "
          + httpResponse.getMessage());
      // Parse response received
      if (httpResponse.getCode() < 0) {
        return handleWithdrawResponse(status, "TIME_OUT", otp, mobileNo, amount,
            billNo);
      }
      if (httpResponse.getCode() == 404) {
        return handleWithdrawResponse(status,
            "Some thing went wrong, try again.", otp, mobileNo, amount, billNo);
      }
      ObjectMapper mapper = new ObjectMapper();
      Map<String, Object> map = mapper.readValue(httpResponse.getMessage(),
          new TypeReference<Map<String, Object>>() {
          });
      Boolean apiSuccess = (Boolean) map.get("Success");
      String apiMsg = "Success";
      if (apiSuccess) {
        status = true;
        if (apiType.equalsIgnoreCase("GetByMobile")) {
          Map<String, Object> res = (Map<String, Object>) map
              .get("CustomerData");
          String walletBalance = String.valueOf(res.get("AvailableCredits"));
          return handleWalletBalanceResponse(status, apiMsg, walletBalance);
        } else if (apiType.equalsIgnoreCase("sendOTP")
            || apiType.equalsIgnoreCase("validateOTP")) {
          String pointsValue = String.valueOf(map.get("PointsValue"));
          String requestNumberResponse = String
              .valueOf(map.get("RequestNumber"));
          return handleSendOTPResponse(status, apiMsg, pointsValue,
              requestNumberResponse);
        }
      } else {
        apiMsg = (String) map.get("Message");
        return handleWalletBalanceResponse(status, apiMsg, null);
      }
    } catch (JsonProcessingException e) {
      logger.error("Exception occured", e);
    } catch (IOException e) {
      logger.error("Exception occured", e);
    }
    return handleWithdrawResponse(status, "DEFAULT", otp, mobileNo, amount,
        billNo);

  }

  public static String handleSendOTPResponse(boolean status,
      String statusCodeMsg, String pointsValue, String requestNumber)
      throws JsonProcessingException {
    String displayStatusMessage = null;
    if (statusCodeMsg != null && statusCodesMap.get(statusCodeMsg) != null)
      displayStatusMessage = statusCodesMap.get(statusCodeMsg);
    else
      displayStatusMessage = statusCodeMsg;
    Map<String, Object> data = new HashMap<String, Object>();
    data.put("result", status);
    data.put("message", displayStatusMessage);
    data.put("points_value", pointsValue);
    data.put("request_number", requestNumber);
    return new ObjectMapper().writeValueAsString(data);
  }

}