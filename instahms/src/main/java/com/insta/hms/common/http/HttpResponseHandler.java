package com.insta.hms.common.http;

import com.bob.hms.common.DataBaseUtil;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

/**
 * This class handles HTTP response. Override the methods in this class for custom response handling
 */
public class HttpResponseHandler {

  /** The Constant log. */
  static final Logger log = LoggerFactory.getLogger(HttpResponseHandler.class);

  /**
   * Handle.
   *
   * @param connection
   *          the connection
   * @return the http response
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public HttpResponse handle(HttpURLConnection connection) throws IOException {

    int httpStatus = connection.getResponseCode();
    String httpStatusMessage = connection.getResponseMessage();

    if (httpStatus < 200 || httpStatus > 299) {
      InputStream es = connection.getErrorStream();
      if (null != es) {
        log.error("Recieved HTTP Error  : \n" + new String(DataBaseUtil.readInputStream(es)));
        // es.close();
      }
      return new HttpResponse(httpStatus, httpStatusMessage);
    }
    // HTTP 2xx - we parse the result
    return handleResponse(connection);

  }

  /**
   * Handle response.
   *
   * @param connection
   *          the connection
   * @return the http response
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  // logic for handling 2xx HTTP status
  public HttpResponse handleResponse(HttpURLConnection connection) throws IOException {
    InputStream is = connection.getInputStream();
    String resBody = IOUtils.toString(is);
    try {
      return new HttpResponse(connection.getResponseCode(), resBody);
    } finally {
      is.close();
    }
  }
}
