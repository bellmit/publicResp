package com.insta.hms.messaging;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.insta.hms.common.http.HttpResponse;
import com.insta.hms.common.http.HttpResponseHandler;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;

/**
 * The Class CommunicatorResponseHandler.
 */
public class CommunicatorSmsResponseHandler extends HttpResponseHandler {

  /** The logger. */
  static Logger logger = LoggerFactory
      .getLogger(CommunicatorSmsResponseHandler.class);

  /** The Constant SUCCESS_MESSAGE. */
  private static final String SUCCESS_MESSAGE = "Message sent successfully";

  @Override
  public HttpResponse handleResponse(HttpURLConnection connection) throws IOException {
    InputStream is = connection.getInputStream();
    String resBody = IOUtils.toString(is);
    ObjectMapper mapper = new ObjectMapper();
    try {
      Map<String, Map<String, List<Object>>> map = mapper.readValue(resBody,
          new TypeReference<Map<String, Object>>() {
          });
      List<Object> invalidJobs = map.get("result").get("invalid_jobs");

      if (invalidJobs != null && invalidJobs.isEmpty()) {
        return new HttpResponse(HttpResponse.SUCCESS_STATUS_CODE, SUCCESS_MESSAGE);
      }

    } catch (JsonMappingException jme) {
      logger.error("Unable to parse the response :" + resBody + "\n " + jme);

    } catch (IOException ioe) {
      logger.error("Unable to parse the response :" + resBody + "\n " + ioe);
    } finally {
      is.close();
    }
    return new HttpResponse(HttpResponse.ERROR_STATUS_CODE, resBody);
  }

}
