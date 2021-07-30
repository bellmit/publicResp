package com.insta.hms.integration.practo.api.util;

import com.insta.hms.common.http.HttpClient;
import com.insta.hms.common.http.HttpResponse;
import com.insta.hms.common.http.HttpResponseHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

/**
 * The Class HttpClientUtil.
 */
public class HttpClientUtil {
  
  /** The Constant log. */
  static final Logger log = LoggerFactory.getLogger(HttpClientUtil.class);

  /**
   * Send http get request.
   *
   * @param endpoint the endpoint
   * @param headerMap the header map
   * @param urlParams the url params
   * @return the http response
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static HttpResponse sendHttpGetRequest(String endpoint, Map<String, String> headerMap,
      String urlParams) throws IOException {

    String apiContextPath = URLConstants.PRACTO_FABRIC_API_CONTEXTPATH;
    String url = String.format("%s%s", apiContextPath, endpoint);

    log.debug("Sending Get Request to + " + url + " with query parameters : " + urlParams);
    HttpResponse httpResponse = new HttpClient(new HttpResponseHandler(), 60000, 60000).get(url,
        urlParams, headerMap);
    return httpResponse;
  }

  /**
   * Send http post request.
   *
   * @param endpoint the endpoint
   * @param headerMap the header map
   * @param postData the post data
   * @return the http response
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static HttpResponse sendHttpPostRequest(String endpoint, Map<String, String> headerMap,
      String postData) throws IOException {

    String apiContextPath = URLConstants.PRACTO_FABRIC_API_CONTEXTPATH;
    String url = String.format("%s%s", apiContextPath, endpoint);

    log.debug("Sending Post Request to + " + url + " with data : " + postData);
    HttpResponse httpResponse = new HttpClient(new HttpResponseHandler(), 60000, 60000).post(url,
        postData, headerMap);
    return httpResponse;
  }

}
