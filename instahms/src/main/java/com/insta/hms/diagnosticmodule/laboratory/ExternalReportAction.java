package com.insta.hms.diagnosticmodule.laboratory;

import com.insta.hms.messaging.InstaIntegrationDao;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.http.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.sql.SQLException;

import javax.servlet.http.HttpServletResponse;

public class ExternalReportAction{

  static Logger log = LoggerFactory.getLogger(ExternalReportAction.class);
  
	public String getExternalReportData(HttpServletResponse response,String visitNo, String error) throws SQLException, MalformedURLException {
		HttpURLConnection connection = null;
		BasicDynaBean itdoseExtReport = null;
		itdoseExtReport = new InstaIntegrationDao().getActiveBean("itdose_external_report");
		if(itdoseExtReport == null){
			error = "API Communicator is not configured";
		}else {
		
		URL url = new URL((String) itdoseExtReport.get("url"));
		try{
		    connection = (HttpURLConnection) url.openConnection();
	        String urlParameters = "Username="+(String)itdoseExtReport.get("userid")+"&Password="+(String)itdoseExtReport.get("password")
	        					  +"&VisitNo="+visitNo;
		    connection.setRequestMethod("POST");
		    connection.setConnectTimeout(15000);
		    connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			connection.setRequestProperty("Content-Length", Integer.toString(urlParameters.getBytes().length));
		    connection.setRequestProperty("Content-Language", "en-US");  
			connection.setUseCaches(false);
			connection.setDoOutput(true);
		
			//Send request
			DataOutputStream wr = new DataOutputStream (
	        connection.getOutputStream());
		    wr.writeBytes(urlParameters);
		    wr.close();
		
			 //Get Response              
		    InputStream in = connection.getInputStream();
		    String raw = connection.getHeaderField("Content-Disposition");
		    if(raw != null && raw.indexOf("=") != -1) {  // checking for attachment in response
		    	response.setHeader("Expires", "0");
		        response.setHeader(HttpHeaders.CACHE_CONTROL, "must-revalidate, post-check=0, pre-check=0");
		        response.setHeader("Pragma", "public");
	    	    response.setContentType("application/pdf");
			    OutputStream out = response.getOutputStream();
	
		     // Copy the bits from instream to outstream
			    byte[] buf = new byte[1024];
			    int len;
			    while ((len = in.read(buf)) > 0) {
			       out.write(buf, 0, len);
			      }
			    in.close();
			    out.flush();
			    out.close();
		    } else {
			    BufferedReader reader =  new BufferedReader(new InputStreamReader(in));
			    StringBuilder builder = new StringBuilder();
		   	    String line = null;
		   	    while ((line = reader.readLine()) != null) {
				    builder.append(line);
		   	    }
				String result = builder.toString();			
				error = result.replaceAll("\\<.*?>","") ;				
		    }		      
		} catch(SocketTimeoutException e) {
			error = "IT Dose Server did not respond in 15s";
			e.printStackTrace();          
		} catch(Exception e) {
			error = "Error while creating HTTP connection";
			e.printStackTrace();          
		}
	}	
	return error;	
	}
}
