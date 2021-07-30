package com.insta.hms.billing;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.http.HttpResponse;
import com.insta.hms.common.http.HttpResponseHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

public class PaytmResponseHandler extends HttpResponseHandler{
	static Logger logger = LoggerFactory.getLogger(PaytmResponseHandler.class);
	
	
	@Override
	public HttpResponse handle(HttpURLConnection connection) throws IOException {

		int httpStatus = connection.getResponseCode();
		String httpStatusMessage = null ;

		if (httpStatus < 200 || httpStatus > 299) {
		        InputStream es = connection.getErrorStream();		    
			if (null != es) {
				httpStatusMessage = new String(DataBaseUtil.readInputStream(es));
				httpStatusMessage = (httpStatusMessage == null || httpStatusMessage.isEmpty()) ? 
						connection.getResponseMessage() : httpStatusMessage;
			    logger.error("Recieved HTTP Error  : \n" +
			    		httpStatusMessage);
			}
			return new HttpResponse(httpStatus, httpStatusMessage);
		}
		// HTTP 2xx response
		return handleResponse(connection);
		
   }
	
	
}


