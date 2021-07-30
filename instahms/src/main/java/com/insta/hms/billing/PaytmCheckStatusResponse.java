package com.insta.hms.billing;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/*
 * POJO representation of response received from Paytm check status API
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaytmCheckStatusResponse {
	public static  final int SUCCESS = 1;
	CheckStatusResponse response;
	
	public CheckStatusResponse getResponse() {
		return response;
	}
	public void setResponse(CheckStatusResponse response) {
		this.response = response;
	}
	
	@JsonIgnoreProperties(ignoreUnknown = true)
	public  static class CheckStatusResponse{
		List<TransactionStatus> txnList;

		public List<TransactionStatus> getTxnList() {
			return txnList;
		}

		public void setTxnList(List<TransactionStatus> txnList) {
			this.txnList = txnList;
		}		
	}
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class TransactionStatus{
		String txnGuid;
		int status;
		String merchantOrderId;
		String message;
		public String getMessage() {
			return message;
		}
		public void setMessage(String message) {
			this.message = message;
		}
		public String getMerchantOrderId() {
			return merchantOrderId;
		}
		public void setMerchantOrderId(String merchantOrderId) {
			this.merchantOrderId = merchantOrderId;
		}
		public String getTxnGuid() {
			return txnGuid;
		}
		public void setTxnGuid(String txnGuid) {
			this.txnGuid = txnGuid;
		}
		public int getStatus() {
			return status;
		}
		public void setStatus(int status) {
			this.status = status;
		}
		
	}
}


