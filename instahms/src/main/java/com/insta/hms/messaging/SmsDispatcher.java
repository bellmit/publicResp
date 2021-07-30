package com.insta.hms.messaging;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.insta.hms.common.PhoneNumberUtil;
import com.insta.hms.common.PhoneNumberUtilAction;
import com.insta.hms.common.http.HttpClient;
import com.insta.hms.common.http.HttpMessage;
import com.insta.hms.common.http.HttpResponse;
import com.insta.hms.common.http.HttpResponseHandler;
import com.insta.hms.common.utils.EnvironmentUtil;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.messaging.providers.TokensSupported;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;


import javax.mail.MessagingException;

// TODO: Auto-generated Javadoc
/**
 * The Class SMSDispatcher.
 */
public class SmsDispatcher extends GenericDispatcher implements MessageDispatcher {

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(SmsDispatcher.class);
  
  /** CenterMaster Dao. */
  CenterMasterDAO centerMasterDao = new CenterMasterDAO();

  /**
   * Instantiates a new SMS dispatcher.
   */
  public SmsDispatcher() {
    super("SMS");
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.messaging.MessageDispatcher#dispatch(com.insta.hms.messaging.Message)
   */
  @Override
  public boolean dispatch(Message msg) throws SQLException, MessagingException {
    Map<String, String> config = getDispatcherConfig();
    List<String> phoneNumbers = msg.getReceipients();
    boolean result = false;
    String protocol = config.get("protocol");
    if (!protocol.equals("http")) {
      String[] recipients = formatRecipientAddress(phoneNumbers, config);
      if (recipients.length > 0) {
        msg.clearRecipients();
        msg.addRecipients(recipients);
        msg.setBody(formatBodyText(msg.getBody()));
        logger.debug("Sending message " + msg.toString());
        if ((null != msg.getBody() && !msg.getBody().isEmpty())
            || !msg.getAllAttachments().isEmpty()) {
          result = send(msg);
        } else {
          result = false;
          logger.info("Message Body / Attachment not available, skipping the sms ");
        }
      } else {
        throw new MessagingException("No recipient address");
      }
    } else {
      HttpResponse httpResponse = null;
      HttpMessage[] httpMessages;
      try {
        if ((null == msg.getBody() || msg.getBody().isEmpty())
            && msg.getAllAttachments().isEmpty()) {
          logger.info("Message Body / Attachment not available, skipping the sms ");
          return false;
        }
        httpMessages = createHttpMessage(msg, config);
        for (HttpMessage httpMessage : httpMessages) {
          if (httpMessage.getHttpMethodType().equalsIgnoreCase("POST")) {
            httpResponse = new HttpClient(httpMessage.getResponseHandler(),
                httpMessage.getTimeout(), httpMessage.getTimeout()).post(httpMessage.getApiURL(),
                    httpMessage.getParamData(), httpMessage.getHeaderMap());
          } else if (httpMessage.getHttpMethodType().equalsIgnoreCase("GET")) {
            httpResponse = new HttpClient(httpMessage.getResponseHandler(),
                httpMessage.getTimeout(), httpMessage.getTimeout()).get(httpMessage.getApiURL(),
                    httpMessage.getParamData(), httpMessage.getHeaderMap());
          }
          logger.debug("sent request: {0}", httpMessage.getApiURL());
          String errorDescription = "";
          if (httpResponse != null) {
            if (httpResponse.getCode() == HttpResponse.SUCCESS_STATUS_CODE) {
              String rawResponse = httpResponse.getMessage();
              if (rawResponse.contains((String) httpMessage.getSuccessResponse())) {
                logger.debug("SMS sent succesfully");
                result = true;
              } else {
                errorDescription = "Failed: " + rawResponse; 
              }            
            } else if (httpResponse.getCode() == -100) {
              errorDescription = httpResponse.getMessage();
            } else {
              errorDescription = "Invalid response from webservice:" + httpResponse.getMessage();
            }
          } else {
            errorDescription = "Unable to send SMS.";
          }
          if (!errorDescription.isEmpty()) {
            logger.error(errorDescription);
            throw new MessagingException(errorDescription);
          }
        }
      } catch (IOException ioExp) {
        throw new MessagingException(
            "IO Exception, unable to reach to server, " + ioExp.getMessage());
      }
    }

    return result;
  }

  /**
   * Format body text.
   *
   * @param body
   *          the body
   * @return the string
   */
  private String formatBodyText(String body) {
    return body;
  }

  /**
   * Format recipient address.
   *
   * @param phoneNumbers
   *          the phone numbers
   * @param config
   *          the config
   * @return the string[]
   */
  private String[] formatRecipientAddress(List<String> phoneNumbers, Map config) {
    List<String> recipients = new ArrayList<>();

    String providerDomain = (String) config.get("custom_param_1");

    if (null != phoneNumbers && !phoneNumbers.isEmpty()) {
      for (String phoneNumber : phoneNumbers) {
        if (null != phoneNumber && phoneNumber.trim().length() > 0) {
          if (phoneNumber.indexOf(providerDomain) < 0) {
            // append only if the domain is not already appended
            phoneNumber = formatMobileNumber(phoneNumber,
                (String) config.get("country_code_prefix"));
            recipients.add(new StringBuilder(phoneNumber).append(providerDomain).toString());
          } else {
            recipients.add(phoneNumber);
          }
        }
      }
    }

    return recipients.toArray(new String[recipients.size()]);
  }

  /**
   * Format mobile number.
   *
   * @param phoneNumber
   *          the phone number
   * @param countryCodePrefix
   *          the country code prefix
   * @return the string
   */
  /*
   * Format the phone number depending on countryCodePrefix
   */
  private String formatMobileNumber(String phoneNumber, String countryCodePrefix) {
    String number = null;
    if (countryCodePrefix.equals("WP")) { // Without Plus sign - remove plus if exists
      number = phoneNumber.replaceAll("[+\\-()\\s]", "");
    } else if (countryCodePrefix.equals("P")) { // With Plus sign - allow plus if exists
      number = phoneNumber.replaceAll("[\\-()\\s]", "");
    } else { // No prefix(NP) - remove country_code prefix if exists
      number = phoneNumber.replaceAll("[\\-()\\s]", "");
      if (number.charAt(0) == '+') {
        number = PhoneNumberUtil.getNationalNumber(number);
      }
    }
    return number;
  }

  /**
   * Creates the http message.
   *
   * @param msg the msg
   * @param configuration the configuration
   * @return the http message[]
   * @throws MessagingException the messaging exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public HttpMessage[] createHttpMessage(Message msg, Map configuration)
      throws MessagingException, IOException {
    List<String> recipients = msg.getReceipients();
    HttpMessage[] httpmessages = new HttpMessage[recipients.size()];
    if (!validateConfigTokens(msg, configuration)) {
      throw new MessagingException("Message Dispatch failed: "
          + "One of the configured token's value is missing");
    }
    for (int i = 0; i < recipients.size(); i++) {
      String recipient = formatRecipientWithCountryCode(recipients.get(i));
      String url = configuration.get("http_url").toString();
      url = url.replace("{{message_body}}", URLEncoder.encode(msg.getBody(), "UTF-8"));
      url = url.replace("{{mobile_no}}", URLEncoder.encode(recipient, "UTF-8"));
      url = url.replace("{{message_subject}}", URLEncoder.encode(msg.getSubject(), "UTF-8"));

      HttpMessage httpMessage = new HttpMessage();
      httpMessage.setApiURL(url);
      String httpBody = (String) configuration.get("http_body");
      if (httpBody != null && !httpBody.isEmpty()) {
        httpBody = httpBody.replace("{{message_body}}", msg.getBody().replaceAll("\"", "\\\""))
            .replace("{{mobile_no}}", recipient)
            .replace("{{message_subject}}", msg.getSubject().replaceAll("\"", "\\\""));
      }
      httpMessage.setHttpMethodType(configuration.get("http_method").toString());
      httpMessage.setParamData(httpBody);
      String httpHeader = (String) configuration.get("http_header");
      if (httpHeader != null && !httpHeader.isEmpty()) {
        httpHeader = httpHeader.replace("{{message_body}}", msg.getBody().replaceAll("\"", "\\\""))
            .replace("{{mobile_no}}", recipient)
            .replace("{{message_subject}}", msg.getSubject().replaceAll("\"", "\\\""));
        httpMessage.setHeaderMap((Map<String, String>)
            new ObjectMapper().readValue(httpHeader, new TypeReference<Map<String, String>>(){}));
      }
      httpMessage.setSuccessResponse((String) configuration.get("http_success_response"));
      httpMessage.setResponseHandler(new HttpResponseHandler());
      httpMessage.setTimeout(EnvironmentUtil.getMessageDispatcherTimeout() * 1000);
      httpmessages[i] = httpMessage;
    }

    return httpmessages;
  }

  /**
   * validates for non-null able configured message token values.
   * @param msg message object
   * @param configuration configuration object
   * @return returns true or false
   */
  private boolean validateConfigTokens(Message msg, Map configuration) {
    String joinedTemplate = (new StringBuilder())
        .append(StringUtils.defaultString((String)configuration.get("http_url")))
        .append(StringUtils.defaultString((String) configuration.get("http_body")))
        .append(StringUtils.defaultString((String) configuration.get("http_header")))
        .toString();
    return !((joinedTemplate.contains("{{message_body}}") && StringUtils.isEmpty(msg.getBody()))
        || (joinedTemplate.contains("{{message_subject}}") && StringUtils.isEmpty(msg.getSubject()))
        || (joinedTemplate.contains("{{mobile_no}}") 
            && (msg.getReceipients() == null || msg.getReceipients().isEmpty())));
  }

  /**
   * Formats the number into (countryCode)(number).
   *
   * @param number the number
   * @return the string
   * @throws MessagingException the messaging exception
   */
  private String formatRecipientWithCountryCode(String number)
      throws MessagingException {

    PhoneNumberUtilAction phoneNumberUtil = new PhoneNumberUtilAction();
    Map<String, String> phoneNumber;
    String returnNumber = "";
    try {
      phoneNumber = phoneNumberUtil.getNationalAndCountryCodeUtil(number);
      if (phoneNumber == null) {
        throw new MessagingException("Phone number is Empty.");
      }
      String countryCode = phoneNumber.get("country_code");
      String national = phoneNumber.get("national");
      returnNumber = countryCode + national;
    } catch (SQLException exp) {
      throw new MessagingException("SqlException: " + exp.getMessage());
    }
    return returnNumber;
  }
}
