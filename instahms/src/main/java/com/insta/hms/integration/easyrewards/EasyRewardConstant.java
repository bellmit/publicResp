package com.insta.hms.integration.easyrewards;

public class EasyRewardConstant {

  public static final String STORECODE = "StoreCode";
  public static final String BILLGUID = "BillGUID";
  public static final String MEMBERID = "MemberId";
  public static final String SECURITYTOKEN = "SecurityToken";
  public static final String EASY_REWARDS_GENERATE_TOKEN_API = "easy_rewards_generate_token";
  public static final String EASY_REWARDS_WIDGET_API = "easy_rewards_widget";
  // Exceptions messages
  public static final String EXCEPTION_EASYREWARDZ_CONFIGURATION =
              "exception.easyrewardz.configuration";
  public static final String EXCEPTION_EASYREWARDZ_API_RESPONSE_EMPTY =
              "exception.easyrewardz.api.response.empty";
  public static final String EXCEPTION_EASYREWARDZ_LOGIN_FAILED =
              "exception.easyrewardz.login.failed";
  public static final String EXCEPTION_EASYREWARDZ_INVALID_PHONE_NUMBER =
              "exception.easyrewardz.invalid.phone.number";
  public static final String EXCEPTION_EASYREWARDZ_INVALID_STORE_CODE =
              "exception.easyrewardz.invalid.store.code";
  // API Error Response
  public static final String API_ERROR_RESPONSE_MISSING_PHONE_NUMBER =
              "Input parameters are incorrect. missing memberid";
  public static final String API_ERROR_RESPONSE_STORE_CODE_NOT_EXIST =
              "Store code does not exists.";
}