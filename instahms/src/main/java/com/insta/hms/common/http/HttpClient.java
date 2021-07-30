package com.insta.hms.common.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * The Class HttpClient.
 */
public class HttpClient {

  /** The handler. */
  private HttpResponseHandler handler = null;

  /** The connect timeout. */
  private int connectTimeout = -1;

  /** The read timeout. */
  private int readTimeout = -1;

  /** The Constant log. */
  static final Logger log = LoggerFactory.getLogger(HttpClient.class);

  /** The Constant DEFAULT_CONNECT_TIMEOUT. */
  private static final int DEFAULT_CONNECT_TIMEOUT = 30000; // milliseconds

  /** The Constant DEFAULT_READ_TIMEOUT. */
  private static final int DEFAULT_READ_TIMEOUT = 30000; // milliseconds

  /**
   * Instantiates a new http client.
   *
   * @param handler
   *          the handler
   */
  public HttpClient(HttpResponseHandler handler) {
    this(handler, DEFAULT_CONNECT_TIMEOUT, DEFAULT_READ_TIMEOUT);
  }

  /**
   * Instantiates a new http client.
   *
   * @param handler
   *          the handler
   * @param connectTimeout
   *          the connect timeout
   * @param readTimeout
   *          the read timeout
   */
  public HttpClient(HttpResponseHandler handler, int connectTimeout, int readTimeout) {
    this.handler = handler;
    this.connectTimeout = connectTimeout;
    this.readTimeout = readTimeout;
  }

  /**
   * Gets the.
   *
   * @param serverUrl
   *          the server url
   * @param urlParameters
   *          the url parameters
   * @return the http response
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public HttpResponse get(String serverUrl, String urlParameters) throws IOException {
    return get(serverUrl, urlParameters, null);
  }

  /**
   * Gets the.
   *
   * @param serverUrl
   *          the server url
   * @param urlParameters
   *          the url parameters
   * @param headerMap
   *          the header map
   * @return the http response
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public HttpResponse get(String serverUrl, String urlParameters, Map<String, String> headerMap)
      throws IOException {
    log.debug(
        "Sending GET Request to + " + serverUrl + " with query parameters : " + urlParameters);
    String url = serverUrl + ((null != urlParameters && !urlParameters.trim().equals(""))
        ? ("?" + urlParameters) : (""));
    HttpURLConnection connection = getConnection(url);
    if (null == headerMap) {
      headerMap = new HashMap<String,String>();
    }
    headerMap.put("User-Agent", "Insta HMS HttpClient");
    if (!headerMap.containsKey("accept") && !headerMap.containsKey("Accept")) {
      headerMap.put("Accept", "*/*");
    }
    for (Entry<String, String> entry : headerMap.entrySet()) {
      connection.setRequestProperty(entry.getKey(), entry.getValue());
    }
    return handler.handle(connection);
  }

  /**
   * Gets the.
   *
   * @param serverUrl
   *          the server url
   * @param queryParamMap
   *          the query param map
   * @param headerMap
   *          the header map
   * @return the http response
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public HttpResponse get(String serverUrl, Map<String, String> queryParamMap,
      Map<String, String> headerMap) throws IOException {
    StringBuilder sb = new StringBuilder();
    for (Entry<String, String> e : queryParamMap.entrySet()) {
      if (sb.length() > 0) {
        sb.append('&');
      }
      sb.append(encodeQueryParam(e.getKey())).append('=').append(encodeQueryParam(e.getValue()));
    }
    return get(serverUrl, sb.toString(), headerMap);
  }

  /**
   * Post.
   *
   * @param serverUrl
   *          the server url
   * @param postData
   *          the post data
   * @return the http response
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public HttpResponse post(String serverUrl, String postData) throws IOException {
    return post(serverUrl, postData, null);
  }

  /**
   * Post.
   *
   * @param serverUrl
   *          the server url
   * @param postData
   *          the post data
   * @param headerMap
   *          the header map
   * @return the http response
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public HttpResponse post(String serverUrl, String postData, Map<String, String> headerMap)
      throws IOException {
    log.debug("Posting POST Request to + " + serverUrl + " with query parameters : " + postData);
    OutputStream outputStream = null;
    HttpURLConnection connection = null;
    if (null == serverUrl) {
      log.error("Invalid URL specified : " + serverUrl);
      return HttpResponse.INVALID_URL;
    }
    try {
      connection = getConnection(serverUrl);
      connection.setRequestMethod("POST");
      if (null == headerMap) {
        headerMap = new HashMap<String,String>();
      }
      headerMap.put("User-Agent", "Insta HMS HttpClient");
      if (!headerMap.containsKey("accept") && !headerMap.containsKey("Accept")) {
        headerMap.put("Accept", "*/*");
      }
      for (Entry<String, String> entry : headerMap.entrySet()) {
        connection.setRequestProperty(entry.getKey(), entry.getValue());
      }
      connection.setDoOutput(true);
      outputStream = connection.getOutputStream();
      outputStream.write(postData.getBytes("UTF-8"));
      outputStream.close();
      return handler.handle(connection);
    } catch (SocketTimeoutException ste) {
      log.error("Timed out waiting for the server to respond, " + ste.getMessage());
      return HttpResponse.CONNECTION_TIMEOUT;
    } catch (IOException ioe) {
      log.error("Error connecting to server : " + ioe.getMessage());
      return HttpResponse.HTTP_SERVER_ERROR;
    }
  }

  /**
   * Gets the connection.
   *
   * @param serverUrl
   *          the server url
   * @return the connection
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  private HttpURLConnection getConnection(String serverUrl) throws IOException {

    URL url = new URL(serverUrl);
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setConnectTimeout(connectTimeout < 0 ? DEFAULT_CONNECT_TIMEOUT : connectTimeout);
    connection.setReadTimeout(readTimeout < 0 ? DEFAULT_READ_TIMEOUT : readTimeout);
    connection.setUseCaches(false);
    connection.setDoInput(true);
    return connection;
  }

  /**
   * Encode query param.
   *
   * @param param
   *          the param
   * @return the string
   * @throws UnsupportedEncodingException
   *           the unsupported encoding exception
   */
  private String encodeQueryParam(String param) throws UnsupportedEncodingException {
    // encoding 'space' with percent encoding than with '+'
    // @{see} https://stackoverflow.com/questions/2678551/when-to-encode-space-to-plus-or-20
    return URLEncoder.encode(param, "UTF-8").replaceAll("\\+", "%20");
  }

}
