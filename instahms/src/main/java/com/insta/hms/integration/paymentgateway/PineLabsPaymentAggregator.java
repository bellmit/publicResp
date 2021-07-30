package com.insta.hms.integration.paymentgateway;

import com.bob.hms.common.RequestContext;
import com.chargebee.org.json.JSONArray;
import com.chargebee.org.json.JSONException;
import com.chargebee.org.json.JSONObject;
import com.insta.hms.common.PushService;
import com.insta.hms.common.annotations.EdcMachine;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.exception.HMSException;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@EdcMachine(value = "PineLabs", name = "PineLabs")
public class PineLabsPaymentAggregator implements GenericPaymentsAggregator {

  static Logger log = LoggerFactory.getLogger(PineLabsPaymentAggregator.class);

  private String userName;
  private int sequenceNumber;
  private String allowedPaymentMode;
  private int merchantId;
  private String securityToken;
  private String imei;
  private String merchantPOScode;
  private int plutusTransactionReferenceID;
  private int autoCancelDurationInMinutes;

  private String uploadTxnUrl;
  private String cancelTxnUrl;
  private String checkTxnStatusUrl;

  @LazyAutowired
  public PushService pushService;

  @LazyAutowired
  public PaymentTransactionService paymentTransactionService;

  @LazyAutowired
  public RedisTemplate<String, Object> redisTemplate;

  @LazyAutowired
  private SessionService sessionService;

  private static final String WEBSOCKET_PUSH_CHANNEL = "/topic/txnresult";

  @Override
  public List<String> getSupportedServices() {
    return null;
  }

  @Override
  public Boolean requiresConfiguration() {
    return true;
  }

  @Override
  public Map<String, String> getConfigurationSchema() {
    Map<String, String> configSchemaMap = new HashMap<>();
    configSchemaMap.put("userName", "String");
    configSchemaMap.put("sequenceNumber", "int");
    configSchemaMap.put("allowedPaymentMode", "String");
    configSchemaMap.put("autoCancelDurationInMinutes", "int");
    configSchemaMap.put("merchantId", "int");
    configSchemaMap.put("securityToken", "String");
    configSchemaMap.put("merchantPOScode", "String");
    configSchemaMap.put("IMEI", "String");
    configSchemaMap.put("plutusTransactionReferenceID", "int");
    return configSchemaMap;
  }

  @Override
  public Map<String, String> getConfiguration() {
    Map<String, String> configMap = new HashMap<>();
    configMap.put("userName", this.userName);
    configMap.put("allowedPaymentMode", this.allowedPaymentMode);
    configMap.put("autoCancelDurationInMinutes", String.valueOf(this.autoCancelDurationInMinutes));
    configMap.put("securityToken", this.securityToken);
    configMap.put("merchantPOScode", this.merchantPOScode);
    configMap.put("IMEI", this.imei);
    configMap.put("sequenceNumber", String.valueOf(this.sequenceNumber));
    configMap.put("merchantId", String.valueOf(this.merchantId));
    configMap.put("plutusTransactionReferenceID",
        String.valueOf(this.plutusTransactionReferenceID));

    return configMap;
  }

  @Override
  public void setConfiguration(Map<String, String> configMap) {
    this.userName = configMap.get("userName");
    this.allowedPaymentMode = configMap.get("allowedPaymentMode");
    if (null != configMap.get("autoCancelDurationInMinutes")) {
      this.autoCancelDurationInMinutes = Integer
          .parseInt(configMap.get("autoCancelDurationInMinutes"));
    }
    this.securityToken = configMap.get("securityToken");
    this.merchantPOScode = configMap.get("merchantPOScode");
    this.imei = configMap.get("IMEI");
    if (null != configMap.get("sequenceNumber")) {
      this.sequenceNumber = Integer.parseInt(configMap.get("sequenceNumber"));
    }
    if (null != configMap.get("merchantId")) {
      this.merchantId = Integer.parseInt(configMap.get("merchantId"));
    }
    if (null != configMap.get("plutusTransactionReferenceID")) {
      this.plutusTransactionReferenceID = Integer
          .parseInt(configMap.get("plutusTransactionReferenceID"));
    }
  }

  @Override
  public Map<String, String> getEndpoints() {
    Map<String, String> endpointMap = new HashMap<>();
    endpointMap.put("uploadTxnUrl", this.uploadTxnUrl);
    endpointMap.put("cancelTxnUrl", this.cancelTxnUrl);
    endpointMap.put("checkTxnStatusUrl", this.checkTxnStatusUrl);
    return endpointMap;
  }

  @Override
  public void setEndpoints(Map<String, String> endpointMap) {
    this.uploadTxnUrl = endpointMap.get("uploadTxnUrl");
    this.cancelTxnUrl = endpointMap.get("cancelTxnUrl");
    this.checkTxnStatusUrl = endpointMap.get("checkTxnStatusUrl");
  }

  @Override
  public Map<String, String> doTransaction(TransactionRequirements transactionReq) {

    // String url =
    // "https://www.plutuscloudserviceuat.in:8201/API/CloudBasedIntegration/V1/UploadBilledTransaction";
    String url = getEndpoints().get("uploadTxnUrl").toString();
    Map<String, String> result = new HashMap<>();

    try {
      JSONObject json = new JSONObject();
      json.put("TransactionNumber", transactionReq.getBillNumber());
      json.put("SequenceNumber", sequenceNumber);
      json.put("AllowedPaymentMode", allowedPaymentMode);
      json.put("AutoCancelDurationInMinutes", autoCancelDurationInMinutes);
      json.put("UserID", userName);
      json.put("MerchantID", merchantId);
      json.put("SecurityToken", securityToken);
      json.put("IMEI", imei);
      json.put("MerchantStorePosCode", merchantPOScode);
      // json.put("TotalInvoiceAmount", totalInvoiceAmount); //optional
      // Converting amount to paise.
      json.put("Amount", (int) Math.floor(transactionReq.getAmount() * 100.00f)); 

      HttpClient httpClient = HttpClientBuilder.create().build();
      HttpPost request = new HttpPost(url);
      request.setEntity(new StringEntity(json.toString(), "UTF8"));
      request.setHeader("Content-type", "application/json");

      HttpResponse response = httpClient.execute(request);

      if (response.getStatusLine().getStatusCode() == 200) {
        String responseString = "";
        for (int i = 0; i < response.getEntity().getContentLength(); i++) {
          responseString += Character.toString((char) response.getEntity().getContent().read());
        }
        JSONObject responseJson = new JSONObject(responseString);
        result.put("ResponseCode", responseJson.getString("ResponseCode"));
        result.put("ResponseMessage", responseJson.getString("ResponseMessage"));
        result.put("PlutusTxId", responseJson.getString("PlutusTransactionReferenceID"));

        if (result.get("ResponseMessage").equals("APPROVED")) {
          BasicDynaBean bean = paymentTransactionService.getBean();
          bean.set("response_code", 0);
          bean.set("plutus_txn_id", Integer.parseInt(result.get("PlutusTxId")));
          bean.set("bill_no", transactionReq.getBillNumber());
          bean.set("transaction_type", "PineLabs");
          bean.set("mode_id", -4);
          bean.set("amount", new BigDecimal(Float.toString(transactionReq.getAmount())));
          bean.set("status", true);
          bean.set("status_message", "TXN UPLOADED");
          bean.set("edc_imei", imei);
          bean.set("initiated_by", sessionService.getSessionAttributes().get("userId").toString());
          paymentTransactionService.insert(bean);
          this.redisPineLabsConfig(result.get("PlutusTxId"));
        }
        return result;
      } else {
        throw new HMSException(HttpStatus.BAD_REQUEST, "exception.bad.request", null);
      }
    } catch (IOException | JSONException ex) {
      log.error("", ex);
    }
    return null;
  }

  @Override
  public Map<String, String> cancelTransaction(TransactionRequirements transactionReq) {

    // String url =
    // "https://www.plutuscloudserviceuat.in:8201/API/CloudBasedIntegration/V1/CancelTransaction";
    String url = getEndpoints().get("cancelTxnUrl").toString();
    Map<String, String> result = new HashMap<>();

    try {
      JSONObject json = new JSONObject();
      json.put("PlutusTransactionReferenceID", plutusTransactionReferenceID);
      json.put("MerchantID", merchantId);
      json.put("SecurityToken", securityToken);
      json.put("IMEI", imei);
      json.put("MerchantStorePosCode", merchantPOScode);
      // converting amount to paise.
      json.put("Amount", (int) Math.floor(transactionReq.getAmount() * 100.00f));

      HttpClient httpClient = HttpClientBuilder.create().build();
      HttpPost request = new HttpPost(url);
      request.setEntity(new StringEntity(json.toString(), "UTF8"));
      request.setHeader("Content-type", "application/json");

      HttpResponse response = httpClient.execute(request);

      if (response.getStatusLine().getStatusCode() == 200) {
        String responseString = "";
        for (int i = 0; i < response.getEntity().getContentLength(); i++) {
          responseString += Character.toString((char) response.getEntity().getContent().read());
        }
        JSONObject responseJson = new JSONObject(responseString);
        result.put("ResponseCode", responseJson.getString("ResponseCode"));
        result.put("ResponseMessage", responseJson.getString("ResponseMessage"));

        if (result.get("ResponseMessage").equals("APPROVED")
            || result.get("ResponseMessage").equals("TRANSACTION NOT FOUND")) {
          BasicDynaBean bean = paymentTransactionService.getBean();
          bean.set("status_message", "TXN CANCELLED");
          Map<String, Object> whereMap = new HashMap<String, Object>();
          whereMap.put("plutus_txn_id", plutusTransactionReferenceID);
          paymentTransactionService.update(bean, whereMap);
        }
        return result;
      } else {
        throw new HMSException(HttpStatus.BAD_REQUEST, "exception.bad.request", null);
      }
    } catch (IOException | JSONException ex) {
      log.error("", ex);
    }
    return null;
  }

  @Override
  public void processResponse(String response) {

    // Sample Response from Pine Labs
    // ResponseCode=0,ResponseMessage=APPROVED,PlutusTransactionReferenceID=10942,
    // TransactionNumber=BC17000056,BankTID=26814045,BankMID=0,PaymenMode=CARD,Amount=100,
    // ApprovalCode=00,RRN=000020,Invoice=7,BatchNumber=5,CardNumber=512967******4165,
    // ExpiryDate=XXXX,AcquirerCode=1,AcquirerName=HDFC,TransactionDate=28022018,
    // TransactionTime=132122,CardType=MASTERCARD

    Map<String, String> txnResult = new HashMap<>();
    String[] data = response.split(",");
    String responseCode = data[0];
    txnResult.put("ResponseCode", responseCode);
    txnResult.put("ResponseMessage", data[1].substring(data[1].indexOf("=") + 1));
    txnResult.put("PlutusTransactionReferenceID", data[2].substring(data[2].indexOf("=") + 1));

    if (responseCode.equals("0")) {
      txnResult.put("TID", data[4].substring(data[4].indexOf("=") + 1));
      txnResult.put("MID", data[5].substring(data[5].indexOf("=") + 1));
      txnResult.put("PaymenMode", data[6].substring(data[6].indexOf("=") + 1));
      String amountInPaise = data[7].substring(data[7].indexOf("=") + 1);
      // converting amount to Rs.
      String amount = amountInPaise.substring(0, amountInPaise.length() - 2) + "."
          + amountInPaise.substring(amountInPaise.length() - 2);
      txnResult.put("Amount", amount);
      txnResult.put("ApprovalCode", data[8].substring(data[8].indexOf("=") + 1));
      txnResult.put("RRN", data[9].substring(data[9].indexOf("=") + 1));
      txnResult.put("Invoice", data[10].substring(data[10].indexOf("=") + 1));
      txnResult.put("BatchNumber", data[11].substring(data[11].indexOf("=") + 1));
      txnResult.put("CardNumber", data[12].substring(data[12].indexOf("=") + 1));
      txnResult.put("ExpiryDate", data[13].substring(data[13].indexOf("=") + 1));
      txnResult.put("AcquirerCode", data[14].substring(data[14].indexOf("=") + 1));
      txnResult.put("AcquirerName", data[15].substring(data[15].indexOf("=") + 1));
      txnResult.put("TransactionDate", data[16].substring(data[16].indexOf("=") + 1));
      txnResult.put("TransactionTime", data[17].substring(data[17].indexOf("=") + 1));
      txnResult.put("CardType", data[18].substring(data[18].indexOf("=") + 1));
    }
    this.pushService.pushToUser(RequestContext.getUserName(), WEBSOCKET_PUSH_CHANNEL, txnResult);
    updateTransactionInDb(txnResult);
  }
  
  private void updateTransactionInDb(Map<String,String> txnResult) {
    String redisKey = String.format("plutusTxnId:%s",
        txnResult.get("PlutusTransactionReferenceID"));
    String redisValue = (String) redisTemplate.opsForValue().get(redisKey); // "schema:%s;user:%s"
    String schema = redisValue.split(";")[0].split(":")[1];
    String userName = redisValue.split(";")[1].split(":")[1];
    RequestContext.setConnectionDetails(new String[] { null, null, schema, userName, null });

    // Adding the response to database
    BasicDynaBean bean = paymentTransactionService.getBean();
    bean.set("response_code", Integer.parseInt(txnResult.get("ResponseCode")));
    bean.set("plutus_txn_id", Integer.parseInt(txnResult.get("PlutusTransactionReferenceID")));
    bean.set("mid", txnResult.get("MID"));
    bean.set("approval_code", txnResult.get("ApprovalCode"));
    bean.set("rrn", txnResult.get("RRN"));
    bean.set("invoice", txnResult.get("Invoice"));
    bean.set("card_number", txnResult.get("CardNumber"));
    bean.set("transaction_id", txnResult.get("TID"));
    bean.set("status_message", txnResult.get("ResponseMessage"));
    if (null != txnResult.get("Amount")) {
      bean.set("amount", new BigDecimal(txnResult.get("Amount")));
    } else {
      bean.set("amount", new BigDecimal("0.00"));
    }

    Map<String, Object> whereMap = new HashMap<String, Object>();
    whereMap.put("plutus_txn_id", bean.get("plutus_txn_id"));
    paymentTransactionService.update(bean, whereMap);

  }

  @Override
  public HashMap<String, String> checkTransactionStatus() {
    // String url =
    // "https://www.plutuscloudserviceuat.in:8201/API/CloudBasedIntegration/V1/GetCloudBasedTxnStatus";
    String url = getEndpoints().get("checkTxnStatusUrl").toString();
    HashMap<String, String> result = new HashMap<>();

    try {
      JSONObject json = new JSONObject();
      json.put("PlutusTransactionReferenceID", plutusTransactionReferenceID);
      json.put("MerchantID", merchantId);
      json.put("SecurityToken", securityToken);
      json.put("IMEI", imei);
      json.put("MerchantStorePosCode", merchantPOScode);

      HttpClient httpClient = HttpClientBuilder.create().build();
      HttpPost request = new HttpPost(url);
      request.setEntity(new StringEntity(json.toString(), "UTF8"));
      request.setHeader("Content-type", "application/json");

      HttpResponse response = httpClient.execute(request);

      if (response.getStatusLine().getStatusCode() == 200) {
        String responseString = "";
        for (int i = 0; i < response.getEntity().getContentLength(); i++) {
          responseString += Character.toString((char) response.getEntity().getContent().read());
        }
        JSONObject responseJson = new JSONObject(responseString);

        result.put("ResponseCode", responseJson.getString("ResponseCode"));
        result.put("ResponseMessage", responseJson.getString("ResponseMessage"));

        if (!responseJson.getString("ResponseMessage").equals("TXN UPLOADED")
            && !responseJson.getString("ResponseMessage").equals("INVALID PLUTUS TXN REF ID")) {
          result.put("PlutusTransactionReferenceID", 
              responseJson.getString("PlutusTransactionReferenceID"));
          if (responseJson.getString("ResponseMessage").equals("TXN APPROVED")) {
            JSONArray txnDataJson = responseJson.getJSONArray("TransactionData");
            Map<String, String> txnData = new HashMap<>();
            for (int i = 0; i < txnDataJson.length(); i++) {
              String tag = txnDataJson.getJSONObject(i).getString("Tag");
              String value = txnDataJson.getJSONObject(i).getString("Value");
              txnData.put(tag, value);
            }
            result.put("TID", txnData.get("TID"));
            result.put("MID", txnData.get("MID"));
            result.put("PaymenMode", txnData.get("PaymentMode"));
            String amountInPaise = txnData.get("AmountInPaisa");
            // converting amount to Rs.
            String amount = amountInPaise.substring(0, amountInPaise.length() - 2) + "."
                + amountInPaise.substring(amountInPaise.length() - 2);
            result.put("Amount", amount);
            result.put("ApprovalCode", txnData.get("ApprovalCode"));
            result.put("RRN", txnData.get("RRN"));
            result.put("Invoice", txnData.get("Invoice") != null
                ? txnData.get("Invoice") : txnData.get("Invoice Number"));
            result.put("BatchNumber", txnData.get("BatchNumber"));
            result.put("CardNumber", txnData.get("CardNumber") != null
                ? txnData.get("CardNumber") : txnData.get("Card Number"));
            result.put("ExpiryDate", txnData.get("ExpiryDate") != null
                ? txnData.get("ExpiryDate") : txnData.get("Expiry Date"));
            result.put("AcquirerCode", txnData.get("AcquirerCode") != null
                ? txnData.get("AcquirerCode") : txnData.get("Acquirer Code"));
            result.put("AcquirerName", txnData.get("AcquirerName") != null
                ? txnData.get("AcquirerName") : txnData.get("Acquirer Name"));
            result.put("TransactionDate", txnData.get("TransactionDate") != null
                ? txnData.get("TransactionDate") : txnData.get("Transaction Date"));
            result.put("TransactionTime", txnData.get("TransactionTime") != null 
                ? txnData.get("TransactionTime") : txnData.get("Transaction Time"));
            result.put("CardType", txnData.get("CardType") != null
                ? txnData.get("CardType") : txnData.get("Card Type"));
          }
          this.updateTransactionInDb(result);
        }
        return result;
      } else {
        throw new HMSException(HttpStatus.BAD_REQUEST, "exception.bad.request", null);
      }
    } catch (IOException | JSONException ex) {
      log.error("", ex);
    }
    return null;
  }

  /**
   * This method sets a key-value in redis. key: plutusTxnId:%s value: schema:%s;user:%s This value
   * is used by the Open end-point used by PineLabs to post transaction result.
   */
  public void redisPineLabsConfig(String plutusTxnId) {

    String schema = RequestContext.getSchema();
    String userName = (String) sessionService.getSessionAttributes().get("userId");
    String redisKey = String.format("plutusTxnId:%s", plutusTxnId);
    String redisValue = String.format("schema:%s;user:%s", schema, userName);
    redisTemplate.opsForValue().set(redisKey, redisValue);
    redisTemplate.expire(redisKey, 3600, TimeUnit.SECONDS); // setting expiry time to 1 hour

  }

}