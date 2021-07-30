package com.insta.hms.messaging;

import com.insta.hms.common.http.HttpClient;
import com.insta.hms.common.http.HttpMessage;
import com.insta.hms.common.http.HttpResponse;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import javax.mail.MessagingException;

/**
 * The Dispatcher which contains common methods for SMS,EMAIL.
 */
public abstract class HttpDispatcher {

  static Logger logger = LoggerFactory.getLogger(HttpDispatcher.class);

  protected boolean send(Message message, BasicDynaBean instaIntegrationBean, String hospitalName,
      String defaultCountryCode) throws IOException, MessagingException {
    HttpMessage[] httpMessages = createHttpMessage(message, instaIntegrationBean, hospitalName,
        defaultCountryCode);
    HttpResponse httpResponse = null;
    for (HttpMessage httpMessage : httpMessages) {
      if (httpMessage.getHttpMethodType().equalsIgnoreCase("POST")) {
        httpResponse = new HttpClient(httpMessage.getResponseHandler(), httpMessage.getTimeout(),
            httpMessage.getTimeout()).post(httpMessage.getApiURL(), httpMessage.getParamData(),
                httpMessage.getHeaderMap());
      } else if (httpMessage.getHttpMethodType().equalsIgnoreCase("GET")) {
        httpResponse = new HttpClient(httpMessage.getResponseHandler(), httpMessage.getTimeout(),
            httpMessage.getTimeout()).get(httpMessage.getApiURL(), httpMessage.getParamData());
      }
      logger.info("sent GET request: \n " + httpMessage.getParamData() + "\n and got response : \n "
          + httpResponse.getMessage());

      if (httpResponse.getCode() == HttpResponse.SUCCESS_STATUS_CODE) {
        logger.debug("SMS sent succesfully");
      } else if (httpResponse.getCode() == -100) {
        logger.error(httpResponse.getMessage());
        throw new MessagingException(httpResponse.getMessage());
      } else {
        logger.error("Invalid response from communicator: " + httpResponse.getMessage());
        throw new MessagingException("SMS provider error");
      }

    }
    return true;
  }

  public abstract HttpMessage[] createHttpMessage(Message message, BasicDynaBean integrationBean,
      String hospitalName, String defaultCountryCode) throws MessagingException, IOException;
}
