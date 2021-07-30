package com.insta.hms.pbmauthorization;

import com.insta.hms.pbmauthorization.generated.Webservices;
import com.insta.hms.pbmauthorization.generated.WebservicesSoap;

import javax.xml.ws.Holder;

public class PBMRequestSender {

	private String serviceUser = null;
	private String servicePassword = null;

	public PBMRequestSender(String serviceUser, String servicePassword) {
		super();
		this.serviceUser = serviceUser;
		this.servicePassword = servicePassword;
	}

	public PBMResponse sendPriorRequest(String requestXML, String fileName) {

		byte[] fileContent = requestXML.getBytes();
		Holder<Integer> uploadTxnResult = new Holder<Integer>();
		Holder<String> errorMessage = new Holder<String>();
		Holder<byte[]> errorReport = new Holder<byte[]>();

		String user = getWebServiceUser();
		String password = getWebServicePassword();
		WebservicesSoap pbmwsoap = getWebService();

		pbmwsoap.uploadTransaction(user, password, fileContent,
				fileName, uploadTxnResult, errorMessage, errorReport);

		return new PBMResponse(uploadTxnResult.value, errorMessage.value, errorReport.value);
	}

	public WebservicesSoap getWebService() {
		Webservices pbmws = new Webservices();
		WebservicesSoap pbmwsoap = pbmws.getWebservicesSoap();
		return pbmwsoap;
	}

	public PBMResponse sendCancelRequest() {
		return null;
	}

	public PBMResponse sendResubmitRequest() {
		return null;
	}

	public String getWebServiceUser() {
		return serviceUser;
	}

	public String getWebServicePassword() {
		return servicePassword;
	}
}
