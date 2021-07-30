package com.insta.hms.messaging;

import com.bob.hms.common.RequestContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.insta.hms.common.PhoneNumberUtil;
import com.insta.hms.common.http.HttpClient;
import com.insta.hms.common.http.HttpResponse;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.MessagingException;

public class CommunicatorSmsDispatcher extends CommunicatorDispatcher implements MessageDispatcher {

  static Logger logger = LoggerFactory
      .getLogger(CommunicatorSmsDispatcher.class);
  private static final int TIMEOUT = 10000;

  @Override
  public boolean dispatch(Message msg) throws SQLException, MessagingException {
    try {
      // fetch center's country code
      String defaultCountryCode = (String) new CenterMasterDAO().getCountryCode(0);
      // fetch hospital name
      String hospitalName = RequestContext.getSchema();
      // fetch the endpoint
      String api = (String) new InstaIntegrationDao().findByKey("integration_name", "comm_send")
          .get("url");
      if (api == null) {
        logger.error("Communicator api is not configured");
        throw new MessagingException("Dispatcher is not configured");
      }
      if (msg.getReceipients() == null || msg.getReceipients().isEmpty()) {
        throw new MessagingException("No Recipients");
      }
      return sendMessage(msg, api, hospitalName, defaultCountryCode);
    } catch (Exception ex) {
      logger.error("", ex);
      throw new MessagingException(ex.getMessage());
    }
  }

  private boolean sendMessage(Message msg, String api, String hospitalName,
      String defaultCountryCode) throws IOException, MessagingException {

    Map<String, String> headerMap = new HashMap<String, String>();
    headerMap.put("Content-Type", "application/json");

    String messageType = "INSTA_" + msg.getMessageType().toUpperCase();
    List<String> recipients = msg.getReceipients();

    Map<String, Object> data = new HashMap<String, Object>();
    int index = 0;
    boolean result = false;
    for (String recipient : recipients) {
      recipient = formatRecipient(recipient, defaultCountryCode);

      // prepare the message
      Map<String, String> msgObj = new HashMap<String, String>();
      msgObj.put("app_name", "INSTA");
      msgObj.put("reference_id", String.valueOf(msg.getMessageLogId()));
      msgObj.put("reference_type", hospitalName);
      msgObj.put("message_type", messageType);
      msgObj.put("message_text", msg.getBody());
      msgObj.put("mobile", recipient);
      msgObj.put("name", "Insta");
      msgObj.put("template_name", msg.getSubject());
      data.put(String.valueOf(index++), msgObj);
    }
    String postDataStr = new ObjectMapper().writeValueAsString(data);

    HttpResponse httpResponse = new HttpClient(new CommunicatorSmsResponseHandler(), TIMEOUT,
        TIMEOUT).post(api, postDataStr, headerMap);
    logger.debug(
        "sent POST request: \n " + data + "\n and got response : \n " + httpResponse.getMessage());

    if (httpResponse.getCode() == HttpResponse.SUCCESS_STATUS_CODE) {
      logger.debug("SMS sent succesfully");
      result = true;
    } else if (httpResponse.getCode() == -100) {
      logger.error(httpResponse.getMessage());
      throw new MessagingException(httpResponse.getMessage());
    } else {
      logger.error("Invalid response from communicator: " + httpResponse.getMessage());
      throw new MessagingException("SMS provider error");
    }
    return result;
  }

  /**
   * Formats the number into (+)(countryCode)(number).
   */
  private String formatRecipient(String number, String defaultCountryCode)
      throws MessagingException {
    if (number == null || number.length() == 0) {
      throw new MessagingException("Phone number cannot be empty");
    }

    if (number.charAt(0) != '+') {
      if (defaultCountryCode == null) {
        throw new MessagingException("Country code is not set");
      }

      number = "+" + defaultCountryCode + number;
    }
    List<String> phoneNumber = PhoneNumberUtil.getCountryCodeAndNationalPart(number,
        defaultCountryCode);

    if (phoneNumber == null) {
      throw new MessagingException("Phone number is Invalid");
    }
    String countryCode = phoneNumber.get(0);
    String national = phoneNumber.get(1);
    return "+" + countryCode + national;
  }
}
