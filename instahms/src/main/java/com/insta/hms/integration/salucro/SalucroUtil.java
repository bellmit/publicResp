package com.insta.hms.integration.salucro;


import com.bob.hms.common.DataBaseUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.insta.hms.billing.PaytmResponseHandler;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.http.HttpClient;
import com.insta.hms.common.http.HttpResponse;
import com.insta.hms.exception.NestableValidationException;
import com.insta.hms.exception.ValidationErrorMap;
import com.insta.hms.exception.ValidationException;
import com.insta.hms.messaging.InstaIntegrationDao;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * The Class SalucroUtil.
 */
@SuppressWarnings("deprecation")
public class SalucroUtil {

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(SalucroUtil.class);

  /** The Constant paymentTransactionsDAO. */
  private static final GenericDAO paymentTransactionsDAO = new GenericDAO("payment_transactions");

  /**
   * Gets the salucro information.
   *
   * @return the salucro information
   * @throws Exception the exception
   */
  public static  Map<String,String>  getSalucroInformation() throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    SalucroUrlResponse response =  null;
    Map<String, String> headerMap = new HashMap<>(); // headers to be passed
    try {
      BasicDynaBean salucroDetails = new InstaIntegrationDao()
              .getActiveBean(SalucroConstants.SALUCRO_INFORMATION);
      if (salucroDetails == null) {
        ValidationErrorMap errorMap = new ValidationErrorMap();
        errorMap.addError("error", "ui.exception.invalid.data");
        ValidationException ex = new ValidationException(errorMap);
        Map<String, Object> nestedException = new HashMap<String, Object>();
        nestedException.put("error", ex.getErrors());
        throw new NestableValidationException(nestedException);
      }
      int  integrationId = (Integer) salucroDetails.get("integration_id");
      BasicDynaBean integrationDetails = new InstaIntegrationDao()
            .getIntegrationDetailForCenter(integrationId);
      if ( integrationDetails == null ) {
        ValidationErrorMap errorMap = new ValidationErrorMap();
        errorMap.addError("error", "ui.exception.invalid.data");
        ValidationException ex = new ValidationException(errorMap);
        Map<String, Object> nestedException = new HashMap<String, Object>();
        nestedException.put("error", ex.getErrors());
        throw new NestableValidationException(nestedException);
      }
      String httpHeader = (String) integrationDetails.get("http_header"); 
      headerMap =  mapper.readValue(httpHeader,
              new TypeReference<Map<String, String>>() {});
      headerMap.put("Content-Type", "application/json");

      String api = (String) salucroDetails.get("url");
      if ( httpHeader == null || api == null ) {
        ValidationErrorMap errorMap = new ValidationErrorMap();
        errorMap.addError("error", "ui.exception.unavailable.data");
        ValidationException ex = new ValidationException(errorMap);
        Map<String, Object> nestedException = new HashMap<String, Object>();
        nestedException.put("error", ex.getErrors());
        throw new NestableValidationException(nestedException);
      }
      HttpResponse httpResponse = new HttpClient(new PaytmResponseHandler(),10000,10000)
                       .get(api, new HashMap<String,String>(), headerMap);
      logger.info("Sent GET request: " + headerMap
            + "\n and got response :  " + httpResponse.getMessage());

      response = mapper.readValue(httpResponse.getMessage(), 
             SalucroUrlResponse.class);

    } catch (JsonProcessingException ex) {
      logger.error("Unable to parse request to JSON: " , ex);
    }
    return  handleApiResponse(response);
  }

  /**
   *  Payment API
   *  We use this API when user does payment on Salucro.
   *
   * @param params the params
   * @return the map
   * @throws Exception the exception
   */
  public static Map<String,String> doSalucroPayment(Map<String,Object> params) throws Exception {
    BasicDynaBean salucroDetails = new InstaIntegrationDao()
            .getActiveBean(SalucroConstants.SALUCRO_PAYMENT);
    if (salucroDetails == null) {
      ValidationErrorMap errorMap = new ValidationErrorMap();
      errorMap.addError("error", "ui.exception.invalid.data");
      ValidationException ex = new ValidationException(errorMap);
      Map<String, Object> nestedException = new HashMap<String, Object>();
      nestedException.put("error", ex.getErrors());
      throw new NestableValidationException(nestedException);
    }
    int  integrationId = (Integer) salucroDetails.get("integration_id");
 
    BasicDynaBean integrationDetails = new InstaIntegrationDao()
            .getIntegrationDetailForCenter(integrationId);
    if ( integrationDetails == null ) {
      ValidationErrorMap errorMap = new ValidationErrorMap();
      errorMap.addError("error", "ui.exception.invalid.data");
      ValidationException ex = new ValidationException(errorMap);
      Map<String, Object> nestedException = new HashMap<String, Object>();
      nestedException.put("error", ex.getErrors());
      throw new NestableValidationException(nestedException);
    }
    String httpHeader = (String) integrationDetails.get("http_header"); 
    String httpBody = (String) integrationDetails.get("http_body");

    String api = (String) salucroDetails.get("url");
    if ( httpHeader == null || api == null || httpBody == null ) {
      ValidationErrorMap errorMap = new ValidationErrorMap();
      errorMap.addError("error", "ui.exception.unavailable.data");
      ValidationException ex = new ValidationException(errorMap);
      Map<String, Object> nestedException = new HashMap<String, Object>();
      nestedException.put("error", ex.getErrors());
      throw new NestableValidationException(nestedException);
    }
    ObjectMapper mapper = new ObjectMapper();
    //Prepare parameters required by Salucro
    Map<String, String> headerMap = new HashMap<>(); // headers to be passed
    headerMap =  mapper.readValue(httpHeader,
            new TypeReference<Map<String, String>>() {});
    headerMap.put("Content-Type", "application/json");

    Map<String, Object> reqMap =  new HashMap<>();
    reqMap = mapper.readValue(httpBody,
         new TypeReference<Map<String, Object>>() {});

    if (reqMap != null && !reqMap.isEmpty() ) {
      Map<String,Object> userMap = (Map<String, Object>) reqMap.get("user");
      Map<String, Object> optionsMap = (Map<String,Object>) reqMap.get("options");
      List<Map<String, Object>>  accountsList = (List<Map<String, Object>>) reqMap.get("accounts");
      Map<String, Object> accountsMap = accountsList.get(0);
      if ( userMap != null && !userMap.isEmpty() ) {
        if ( params.get(SalucroConstants.USERNAME) != null ) {
          userMap.put(SalucroConstants.USERNAME,params.get(SalucroConstants.USERNAME));
        }
        if ( params.get(SalucroConstants.USERID) != null ) {
          userMap.put(SalucroConstants.USERID,params.get(SalucroConstants.USERID));
        }
        if ( params.get(SalucroConstants.ROLE) != null ) {
          userMap.put(SalucroConstants.ROLE, params.get(SalucroConstants.ROLE));
        }
        if ( params.get(SalucroConstants.ROLES) != null ) {
          userMap.put(SalucroConstants.ROLES, params.get(SalucroConstants.ROLES));
        }
        if ( params.get(SalucroConstants.LOCATION_IDS) != null ) {
          userMap.put(SalucroConstants.LOCATION_IDS,params.get(SalucroConstants.LOCATION_IDS));
        }
        if ( params.get(SalucroConstants.LOCATION_NAMES) != null ) {
          userMap.put(SalucroConstants.LOCATION_NAMES, params.get(SalucroConstants.LOCATION_NAMES));
        }
      }
      reqMap.put("user", userMap);
      if (optionsMap != null && !optionsMap.isEmpty() ) {
        Map<String, Object> paymentPlanRulesMap = (Map<String, Object>) 
              optionsMap.get("payment_plan_rules");
        if ( params.get(SalucroConstants.ALLOW_ACCOUNT_EDIT) != null ) {
          userMap.put(SalucroConstants.ALLOW_ACCOUNT_EDIT, 
                params.get(SalucroConstants.ALLOW_ACCOUNT_EDIT));
        }
        if ( params.get(SalucroConstants.PARENT_WINDOW_NOTIFICATION) != null ) {
          userMap.put(SalucroConstants.PARENT_WINDOW_NOTIFICATION,
                params.get(SalucroConstants.PARENT_WINDOW_NOTIFICATION));
        }
        if ( params.get(SalucroConstants.LANDING_PAGE) != null ) {
          userMap.put(SalucroConstants.LANDING_PAGE, 
                params.get(SalucroConstants.LANDING_PAGE));
        }
        if ( params.get(SalucroConstants.ALLOW_PAYMENT_METHODS) != null ) {
          userMap.put(SalucroConstants.ALLOW_PAYMENT_METHODS, 
                params.get(SalucroConstants.ALLOW_PAYMENT_METHODS));
        }

        if (params.get("payment_plan_rules") != null) {
          Map<String, Object> paramsPaymentMap = (Map<String, Object>) 
                params.get("payment_plan_rules");

          if (params.get(SalucroConstants.START_DAY_LIMIT_MAX) != null) {
            paymentPlanRulesMap.put(SalucroConstants.START_DAY_LIMIT_MAX,
                paramsPaymentMap.get(SalucroConstants.START_DAY_LIMIT_MAX));
          }

          if (params.get(SalucroConstants.START_DATE_MAX) != null) {
            paymentPlanRulesMap.put(SalucroConstants.START_DATE_MAX,
                paramsPaymentMap.get(SalucroConstants.START_DATE_MAX));
          }

          if (params.get(SalucroConstants.PAYMENT_AMOUNT_MIN) != null) {
            paymentPlanRulesMap.put(SalucroConstants.PAYMENT_AMOUNT_MIN,
                paramsPaymentMap.get(SalucroConstants.PAYMENT_AMOUNT_MIN));
          }
          if (params.get(SalucroConstants.MONTHS_MAX) != null) {
            paymentPlanRulesMap.put(SalucroConstants.MONTHS_MAX,
                paramsPaymentMap.get(SalucroConstants.MONTHS_MAX));
          }
          if (params.get(SalucroConstants.INTERVALS) != null) {
            paymentPlanRulesMap.put(SalucroConstants.INTERVALS,
                paramsPaymentMap.get(SalucroConstants.INTERVALS));
          }
          optionsMap.put("payment_plan_rules", paymentPlanRulesMap);
        }
        reqMap.put("options", optionsMap);
      }
      List<Map<String, Object>>  accountsParamsList = 
              (List<Map<String, Object>>) params.get("accounts");
      if ( accountsParamsList != null && !accountsParamsList.isEmpty() ) {
        for (Map<String, Object> accMap : accountsParamsList) {
          if ( !accMap.containsKey(SalucroConstants.LOCATION_ID) ) {
            accMap.put(SalucroConstants.LOCATION_ID, accountsMap.get(SalucroConstants.LOCATION_ID));
          }
          if ( !accMap.containsKey(SalucroConstants.LOCATION_NAME) ) {
            accMap.put(SalucroConstants.LOCATION_NAME, 
                  accountsMap.get(SalucroConstants.LOCATION_NAME));
          }
          if ( !accMap.containsKey(SalucroConstants.FIRST_NAME) ) {
            accMap.put(SalucroConstants.FIRST_NAME, accountsMap.get(SalucroConstants.FIRST_NAME));
          }
          if ( !accMap.containsKey(SalucroConstants.LAST_NAME) ) {
            accMap.put(SalucroConstants.LAST_NAME, accountsMap.get(SalucroConstants.LAST_NAME));
          }
          if ( !accMap.containsKey(SalucroConstants.ACCOUNT_NUMBER) ) {
            accMap.put(SalucroConstants.ACCOUNT_NUMBER, 
                  accountsMap.get(SalucroConstants.ACCOUNT_NUMBER));
          }
          if ( !accMap.containsKey(SalucroConstants.SECONDARY_ACCOUNT_NUMBER) ) {
            accMap.put(SalucroConstants.SECONDARY_ACCOUNT_NUMBER,
                  accountsMap.get(SalucroConstants.SECONDARY_ACCOUNT_NUMBER));
          }
          if ( !accMap.containsKey(SalucroConstants.AMOUNT) ) {
            accMap.put(SalucroConstants.AMOUNT, accountsMap.get(SalucroConstants.AMOUNT));
          }
          if ( !accMap.containsKey(SalucroConstants.EMAIL) ) {
            accMap.put(SalucroConstants.EMAIL, accountsMap.get(SalucroConstants.EMAIL));
          }
          if ( !accMap.containsKey(SalucroConstants.PHONE) ) {
            accMap.put(SalucroConstants.PHONE, accountsMap.get(SalucroConstants.PHONE));
          }
          if ( !accMap.containsKey(SalucroConstants.SOURCE) ) {
            accMap.put(SalucroConstants.SOURCE, accountsMap.get(SalucroConstants.SOURCE));
          }
          if ( !accMap.containsKey(SalucroConstants.SOURCE_ID) ) {
            accMap.put(SalucroConstants.SOURCE_ID, accountsMap.get(SalucroConstants.SOURCE_ID));
          }
        }

        reqMap.put("accounts", accountsParamsList);
      }
    }

    SalucroUrlResponse response =  null;
    try {
      String postDataStr = mapper.writeValueAsString(reqMap);
      HttpResponse httpResponse = new HttpClient(
                      new PaytmResponseHandler(),10000,10000).post(api, postDataStr, headerMap);
      logger.info("Sent POST request: " + reqMap + "\n and "
            + " got response :  " + httpResponse.getMessage());
      response = mapper.readValue(httpResponse.getMessage(), SalucroUrlResponse.class);
    } catch (JsonProcessingException ex) {
      logger.info("Unable to parse respose json: ", ex);
    }
    return  handleApiResponse(response);
  }

  /**
  *  Refund API
  *  We use this API when user does refund on Salucro.
  *
  * @param params the params
  * @return the map
  * @throws Exception the exception
  */
  public static Map<String,String> doSalucroRefund(Map<String,Object> params) throws Exception {
    BasicDynaBean salucroDetails = new InstaIntegrationDao()
          .getActiveBean(SalucroConstants.SALUCRO_REFUND);
    if (salucroDetails == null) {
      ValidationErrorMap errorMap = new ValidationErrorMap();
      errorMap.addError("error", "ui.exception.invalid.data");
      ValidationException ex = new ValidationException(errorMap);
      Map<String, Object> nestedException = new HashMap<String, Object>();
      nestedException.put("error", ex.getErrors());
      throw new NestableValidationException(nestedException);
    }
    int  integrationId = (Integer) salucroDetails.get("integration_id");

    BasicDynaBean integrationDetails = new InstaIntegrationDao()
          .getIntegrationDetailForCenter(integrationId);
    if ( integrationDetails == null ) {
      ValidationErrorMap errorMap = new ValidationErrorMap();
      errorMap.addError("error", "ui.exception.invalid.data");
      ValidationException ex = new ValidationException(errorMap);
      Map<String, Object> nestedException = new HashMap<String, Object>();
      nestedException.put("error", ex.getErrors());
      throw new NestableValidationException(nestedException);
    }
    String httpHeader = (String) integrationDetails.get("http_header"); 
    String httpBody = (String) integrationDetails.get("http_body");
    String api = (String) salucroDetails.get("url");
    if ( httpHeader == null || api == null || httpBody == null ) {
      ValidationErrorMap errorMap = new ValidationErrorMap();
      errorMap.addError("error", "ui.exception.unavailable.data");
      ValidationException ex = new ValidationException(errorMap);
      Map<String, Object> nestedException = new HashMap<String, Object>();
      nestedException.put("error", ex.getErrors());
      throw new NestableValidationException(nestedException);
    }
    //Prepare parameters required by Salucro
    ObjectMapper mapper = new ObjectMapper();
    Map<String, String> headerMap = new HashMap<>(); // headers to be passed
    headerMap =  mapper.readValue(httpHeader,
          new TypeReference<Map<String, String>>() {});
    headerMap.put("Content-Type", "application/json");
    Map<String, Object> reqMap = mapper.readValue(httpBody,
          new TypeReference<Map<String, Object>>() {});

    if (reqMap != null && !reqMap.isEmpty()) {
      Map<String,Object> userMap = (Map<String, Object>) reqMap.get("user");
      if ( userMap != null && !userMap.isEmpty() ) {
        if (params.get(SalucroConstants.USERNAME) != null ) {
          userMap.put(SalucroConstants.USERNAME,params.get(SalucroConstants.USERNAME));
        }
        if (params.get(SalucroConstants.USERID) != null ) {
          userMap.put(SalucroConstants.USERID,params.get(SalucroConstants.USERID));
        }
        if (params.get(SalucroConstants.ROLE) != null ) {
          userMap.put(SalucroConstants.ROLE, params.get(SalucroConstants.ROLE));
        }
        if (params.get(SalucroConstants.ROLES) != null) {
          userMap.put(SalucroConstants.ROLES, params.get(SalucroConstants.ROLES));
        }
        if (params.get(SalucroConstants.LOCATION_IDS) != null) {
          userMap.put(SalucroConstants.LOCATION_IDS,
                params.get(SalucroConstants.LOCATION_IDS));
        }
        if (params.get(SalucroConstants.LOCATION_NAMES) != null) {
          userMap.put(SalucroConstants.LOCATION_NAMES, 
                params.get(SalucroConstants.LOCATION_NAMES));
        }
      }
      reqMap.put("user", userMap);
      if (params.get(SalucroConstants.TRANSACTION_ID) != null ) {
        reqMap.put(SalucroConstants.TRANSACTION_ID,params.get(SalucroConstants.TRANSACTION_ID));
      }
      if (params.get(SalucroConstants.PAYMENT_ID) != null ) {
        reqMap.put(SalucroConstants.PAYMENT_ID,params.get(SalucroConstants.PAYMENT_ID)); 
      }
      if (params.get(SalucroConstants.LINE_ITEM_ID) != null ) {
        reqMap.put(SalucroConstants.LINE_ITEM_ID,params.get(SalucroConstants.LINE_ITEM_ID));
      }
    }

    SalucroUrlResponse response =  null;
    try {
      String postDataStr = mapper.writeValueAsString(reqMap);
      HttpResponse httpResponse = new HttpClient(
          new PaytmResponseHandler(),10000,10000).post(api, postDataStr, headerMap);
      logger.info("Sent POST request: " + reqMap 
          + "\n and got response :  " + httpResponse.getMessage());
      response = mapper.readValue(httpResponse.getMessage(), SalucroUrlResponse.class);
    } catch (JsonProcessingException ex) {
      logger.error("Unable to parse respose json: ", ex);
    }
    return  handleApiResponse(response);
  }

  /**
   *  Report API
   *  We use this API when user wants Salucro report.
   *
   * @param params the params
   * @return the salucro report
   * @throws Exception the exception
   */
  public static Map<String,String> getSalucroReport(Map<String,Object> params) throws Exception {
    BasicDynaBean salucroDetails = new InstaIntegrationDao()
        .getActiveBean(SalucroConstants.SALUCRO_REPORT);
    if (salucroDetails == null) {
      ValidationErrorMap errorMap = new ValidationErrorMap();
      errorMap.addError("error", "ui.exception.invalid.data");
      ValidationException ex = new ValidationException(errorMap);
      Map<String, Object> nestedException = new HashMap<String, Object>();
      nestedException.put("error", ex.getErrors());
      throw new NestableValidationException(nestedException);
    }
    int  integrationId = (Integer) salucroDetails.get("integration_id");
    BasicDynaBean integrationDetails = new InstaIntegrationDao()
        .getIntegrationDetailForCenter(integrationId);
    if ( integrationDetails == null ) {
      ValidationErrorMap errorMap = new ValidationErrorMap();
      errorMap.addError("error", "ui.exception.invalid.data");
      ValidationException ex = new ValidationException(errorMap);
      Map<String, Object> nestedException = new HashMap<String, Object>();
      nestedException.put("error", ex.getErrors());
      throw new NestableValidationException(nestedException);
    }
    String httpHeader = (String) integrationDetails.get("http_header"); 
    String httpBody = (String) integrationDetails.get("http_body");

    String api = (String) salucroDetails.get("url");
    if ( httpHeader == null || api == null || httpBody == null) {
      ValidationErrorMap errorMap = new ValidationErrorMap();
      errorMap.addError("error", "ui.exception.unavailable.data");
      ValidationException ex = new ValidationException(errorMap);
      Map<String, Object> nestedException = new HashMap<String, Object>();
      nestedException.put("error", ex.getErrors());
      throw new NestableValidationException(nestedException);
    }
    //Prepare parameters required by Salucro
    ObjectMapper mapper = new ObjectMapper();
    Map<String, String> headerMap = new HashMap<>(); // headers to be passed
    headerMap =  mapper.readValue(httpHeader,
         new TypeReference<Map<String, Object>>() {});
    headerMap.put("Content-Type", "application/json");

    Map<String, Object> reqMap = new HashMap<>();
    reqMap = mapper.readValue(httpBody,
         new TypeReference<Map<String, Object>>() {});

    if (reqMap != null && !reqMap.isEmpty()) {
      Map<String,Object> userMap = (Map<String, Object>) reqMap.get("user");
      if ( userMap != null && !userMap.isEmpty() ) {
        if (params.get(SalucroConstants.USERNAME) != null ) {
          userMap.put(SalucroConstants.USERNAME,params.get(SalucroConstants.USERNAME));
        }
        if (params.get(SalucroConstants.USERID) != null ) {
          userMap.put(SalucroConstants.USERID,params.get(SalucroConstants.USERID));
        }
        if (params.get(SalucroConstants.ROLE) != null ) {
          userMap.put(SalucroConstants.ROLE, params.get(SalucroConstants.ROLE));
        }
        if (params.get(SalucroConstants.ROLES) != null ) {
          userMap.put(SalucroConstants.ROLES, params.get(SalucroConstants.ROLES));
        }
        if (params.get(SalucroConstants.LOCATION_IDS) != null ) {
          userMap.put(SalucroConstants.LOCATION_IDS,params.get(SalucroConstants.LOCATION_IDS));
        }
        if (params.get(SalucroConstants.LOCATION_NAMES) != null ) {
          userMap.put(SalucroConstants.LOCATION_NAMES, params.get(SalucroConstants.LOCATION_NAMES));
        }
      }
      reqMap.put("user", userMap);
    }

    SalucroUrlResponse response =  null;
    try {
      String postDataStr = mapper.writeValueAsString(reqMap);

      HttpResponse httpResponse = new HttpClient(
          new PaytmResponseHandler(),10000,10000).post(api, postDataStr, headerMap);
      logger.info("Sent POST request: " + reqMap 
          + "\n and got response :  " + httpResponse.getMessage());
      response = mapper.readValue(httpResponse.getMessage(), SalucroUrlResponse.class);
    } catch (JsonProcessingException ex) {
      logger.info("Unable to parse respose json: ", ex);
    }
    return  handleApiResponse(response);
  }

  /**
  *  Transaction API
  *  We use this API when user needs to check transaction on Salucro.
  *
  * @param params the params
  * @return the salucro transactions
  * @throws Exception the exception
  */
  public static Map<String,String> getSalucroTransactions(
        Map<String,Object> params) throws Exception {
    BasicDynaBean salucroDetails = new InstaIntegrationDao()
          .getActiveBean(SalucroConstants.SALUCRO_TRANSACTION);
    if (salucroDetails == null) {
      ValidationErrorMap errorMap = new ValidationErrorMap();
      errorMap.addError("error", "ui.exception.invalid.data");
      ValidationException ex = new ValidationException(errorMap);
      Map<String, Object> nestedException = new HashMap<String, Object>();
      nestedException.put("error", ex.getErrors());
      throw new NestableValidationException(nestedException);
    }
    int  integrationId = (Integer) salucroDetails.get("integration_id");
    BasicDynaBean integrationDetails = new InstaIntegrationDao()
          .getIntegrationDetailForCenter(integrationId);
    if ( integrationDetails == null ) {
      ValidationErrorMap errorMap = new ValidationErrorMap();
      errorMap.addError("error", "ui.exception.invalid.data");
      ValidationException ex = new ValidationException(errorMap);
      Map<String, Object> nestedException = new HashMap<String, Object>();
      nestedException.put("error", ex.getErrors());
      throw new NestableValidationException(nestedException);
    }
    String httpHeader = (String) integrationDetails.get("http_header"); 
    String httpBody = (String) integrationDetails.get("http_body");

    String api = (String) salucroDetails.get("url");
    if ( httpHeader == null || api == null || httpBody == null ) {
      ValidationErrorMap errorMap = new ValidationErrorMap();
      errorMap.addError("error", "ui.exception.unavailable.data");
      ValidationException ex = new ValidationException(errorMap);
      Map<String, Object> nestedException = new HashMap<String, Object>();
      nestedException.put("error", ex.getErrors());
      throw new NestableValidationException(nestedException);
    }
    //Prepare parameters required by Salucro
    ObjectMapper mapper = new ObjectMapper();
    Map<String, String> headerMap = new HashMap<>(); // headers to be passed
    headerMap =  mapper.readValue(httpHeader,
        new TypeReference<Map<String, Object>>() {});
    headerMap.put("Content-Type", "application/json");

    Map<String, Object> reqMap = new HashMap<>();
    reqMap = mapper.readValue(httpBody,
        new TypeReference<Map<String, Object>>() {});

    if (reqMap != null && !reqMap.isEmpty()) {
      Map<String,Object> userMap = (Map<String, Object>) reqMap.get("user");
      if ( userMap != null && !userMap.isEmpty() ) {
        if (params.get(SalucroConstants.USERNAME) != null ) {
          userMap.put(SalucroConstants.USERNAME,params.get(SalucroConstants.USERNAME));
        }
        if (params.get(SalucroConstants.USERID) != null ) {
          userMap.put(SalucroConstants.USERID,params.get(SalucroConstants.USERID));
        }
        if (params.get(SalucroConstants.ROLE) != null ) {
          userMap.put(SalucroConstants.ROLE, params.get(SalucroConstants.ROLE));
        }
        if (params.get(SalucroConstants.ROLES) != null ) {
          userMap.put(SalucroConstants.ROLES, params.get(SalucroConstants.ROLES));
        }
        if (params.get(SalucroConstants.LOCATION_IDS) != null ) {
          userMap.put(SalucroConstants.LOCATION_IDS,
               params.get(SalucroConstants.LOCATION_IDS));
        }
        if (params.get(SalucroConstants.LOCATION_NAMES) != null) {
          userMap.put(SalucroConstants.LOCATION_NAMES,
               params.get(SalucroConstants.LOCATION_NAMES));
        }
      }
      reqMap.put("user", userMap);
      if (params.get(SalucroConstants.TRANSACTION_ID) != null ) {
        reqMap.put(SalucroConstants.TRANSACTION_ID,params.get(SalucroConstants.TRANSACTION_ID));
      }
      if (params.get(SalucroConstants.PAYMENT_ID) != null ) {
        reqMap.put(SalucroConstants.PAYMENT_ID,params.get(SalucroConstants.PAYMENT_ID));
      }
      if (params.get(SalucroConstants.LINE_ITEM_ID) != null ) {
        reqMap.put(SalucroConstants.LINE_ITEM_ID,params.get(SalucroConstants.LINE_ITEM_ID));
      }
    }
    SalucroUrlResponse response =  null;
    try {
      String postDataStr = mapper.writeValueAsString(reqMap);

      HttpResponse httpResponse = new HttpClient(
                 new PaytmResponseHandler(),10000,10000).post(api, postDataStr, headerMap);
      logger.info("Sent POST request: "
                   + reqMap + "\n and got response :  " + httpResponse.getMessage());
      response = mapper.readValue(httpResponse.getMessage(), SalucroUrlResponse.class);
    } catch (JsonProcessingException ex) {
      logger.info("Unable to parse respose json: ", ex);
    }
    return  handleApiResponse(response);
  }

  /**
  * Handle api response.
  *
  * @param response the response
  * @return the map
  * @throws Exception the exception
  */
  private static Map<String,String> handleApiResponse(
       SalucroUrlResponse response) throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    if ( response == null) {
      ValidationErrorMap errorMap = new ValidationErrorMap();
      errorMap.addError("error", "ui.exception.forbidden");
      ValidationException ex = new ValidationException(errorMap);
      Map<String, Object> nestedException = new HashMap<String, Object>();
      nestedException.put("error", ex.getErrors());
      throw new NestableValidationException(nestedException);
    }
    Map<String, Object> result = response.getResult() != null
        ? response.getResult() : null;
    Map<String, Object> payload = response.getPayload() != null
        ? response.getPayload() : null;
    Map<String,Object> error = response.getError() != null
        ? response.getError() : null;
    
    Map<String,String> data = new HashMap<>();
    if ( result != null && !result.isEmpty()) {
      String resultCode =  (String) result.get("status");
      String resultData = mapper.writeValueAsString(result);
      data.put("result_status", resultCode);
      data.put("result", resultData);
      if ( resultCode != null && resultCode.equalsIgnoreCase("success")) {
        String payloadData = mapper.writeValueAsString(payload);
        data.put("payload", payloadData);
      } else {
        String errorData = mapper.writeValueAsString(error);
        data.put("error", errorData);
      }
    }
    return  data;
  }


  /**
  * Inserts transaction data to database.
  *
  * @param params the params
  * @throws SQLException the SQL exception
  * @throws IOException Signals that an I/O exception has occurred.
  */
  public static void insertTransaction(Map<String,Object> params) throws SQLException, IOException {
    Connection con = null;
    boolean success = true;
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      BasicDynaBean transactionBean = paymentTransactionsDAO.getBean();
      transactionBean.set("payment_transaction_id",params.get("payment_transaction_id"));
      transactionBean.set("mode_id",SalucroConstants.MODE_ID);
      transactionBean.set("bill_no",params.get("bill_no"));
      transactionBean.set("transaction_id",params.get("transaction_id"));
      transactionBean.set("response",params.get("response"));
      success = paymentTransactionsDAO.insert(con, transactionBean);
    } finally {
      DataBaseUtil.commitClose(con, success);
    }
  }

}
