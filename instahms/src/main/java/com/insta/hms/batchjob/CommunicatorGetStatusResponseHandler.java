package com.insta.hms.batchjob;

import com.insta.hms.common.http.HttpResponse;
import com.insta.hms.common.http.HttpResponseHandler;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

public class CommunicatorGetStatusResponseHandler extends HttpResponseHandler {

  static Logger logger = LoggerFactory.getLogger(CommunicatorGetStatusResponseHandler.class);

  @Override
  public HttpResponse handleResponse(HttpURLConnection connection) throws IOException {
    InputStream is = connection.getInputStream();
    String resBody = IOUtils.toString(is);
    try {
      return new HttpResponse(200, resBody);
    } catch (Exception exception) {
      logger.error("Unable to parse the response : " + exception);
    } finally {
      is.close();
    }
    return new HttpResponse(-100, "Dispatcher response invalid format");
  }

}
