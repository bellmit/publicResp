package com.insta.hms.billing;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/*
 * POJO representation of response received from Paytm. This includes only the fields that we require
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaytmWithdrawResponse {
	String status;
	String statusCode;
	String statusMessage;
	Response response;
	
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getStatusCode() {
		return statusCode;
	}
	public void setStatusCode(String statusCode) {
		this.statusCode = statusCode;
	}
	public String getStatusMessage() {
		return statusMessage;
	}
	public void setStatusMessage(String statusMessage) {
		this.statusMessage = statusMessage;
	}
	public Response getResponse() {
		return response;
	}
	public void setResponse(Response response) {
		this.response = response;
	}
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Response{
		String walletSystemTxnId;

		public String getWalletSystemTxnId() {
			return walletSystemTxnId;
		}

		public void setWalletSystemTxnId(String walletSystemTxnId) {
			this.walletSystemTxnId = walletSystemTxnId;
		}
	}
	
}