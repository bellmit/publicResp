package com.insta.hms.billing;
import com.bob.hms.common.DataBaseUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.http.HttpClient;
import com.insta.hms.common.http.HttpResponse;
import com.insta.hms.messaging.InstaIntegrationDao;
import com.paytm.pg.merchant.CheckSumServiceHelper;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * The Class PaytmUtil.
 */
public class PaytmUtil {
	
	/** The logger. */
	static Logger logger = LoggerFactory.getLogger(PaytmUtil.class);
	
	/** The check sum service helper. */
	private static CheckSumServiceHelper checkSumServiceHelper = CheckSumServiceHelper.getCheckSumServiceHelper();
	
	private static final Map<String,String> statusCodesMap = new HashMap<>();	
		
	private static final String WITHDRAW_INTEGRATION_NAME = "paytm_withdraw_money";
	
	private static final String CHECK_TRANSACTION_STATUS_INTEGRATION_NAME = "paytm_check_transaction_status";
	
	private static final GenericDAO paymentTransactionsDAO = new GenericDAO("payment_transactions");
	
	static{
		statusCodesMap.put("DEFAULT","Transaction Failed. Please re-try.");
		statusCodesMap.put("SUCCESS","Transaction is successfull.");
		statusCodesMap.put("TIME_OUT","Request Timed-out. Please re-try.");
		statusCodesMap.put("408","OTP Timed out. Please re-try with a new OTP.");
		statusCodesMap.put("GE_1027","Payer does not have a paytm account.");
		statusCodesMap.put("GE_1018","Invalid Currency Code. Please contact your system administrator and request him to set currency code as INR.");
		statusCodesMap.put("WM_1003","Merchant does not exist. Please contact your system administrator.");
		statusCodesMap.put("GE_1035","Merchant is not active. Please contact your system administrator.");
		statusCodesMap.put("OTP_0001","Please enter OTP.");
		statusCodesMap.put("WM_1006","Insufficient Balance in payer's account. Please ask him/her to refill paytm account and then retry.");
		statusCodesMap.put("WM_1010","Merchant does not exist. Please contact your system administrator.");
		statusCodesMap.put("CB_1000","Invalid Coupon Code. Please re-try with another code.");
		statusCodesMap.put("CB_1001","Invalid Coupon Code. Please re-try with another code.");
		statusCodesMap.put("PAYTM_CONFIG_NOT_SET","Paytm configuration is not set. Please contact your system administrator.");
		statusCodesMap.put("MERCHANT_ID_NOT_SET","Merchant Id is missing. Please contact your system administrator.");
		statusCodesMap.put("MERCHANT_KEY_NOT_SET","Merchant Key is missing. Please contact your system administrator.");
		statusCodesMap.put("CHECKSUM_ERROR","Merchant key is Invalid. Please contact your system administrator.");
		
	}
	
	/** The Paytm mode id. */
	private static final int MODE_ID  = -2;
	
	/**
	 * Request for money / Withdraw money.
	 *
	 * @param mobileNo -  Phone number from which amount has to be deducted
	 * @param otp - OTP of patient
	 * @param amount -  the  amount to be deducted from patient's wallet
	 * @param billNo - Reference against Paytm transaction. We don't send to this Paytm. We just store it for our reference
	 * @param isProduction - Whether production or staging paytm configuration is to be used
	 * @return - Json string to be sent to client
	 */
	public static  String  requestForMoney(String mobileNo, String otp,String amount, String billNo) throws Exception{
		BasicDynaBean paytmDetails = new InstaIntegrationDao().getActiveBean(WITHDRAW_INTEGRATION_NAME);
		boolean  status = false;
		int merchantOrderId = paymentTransactionsDAO.getNextSequence(); // Next sequence of Transaction log table		
		if(paytmDetails == null){ //paytm configuration is not set
			return  handleWithdrawResponse(merchantOrderId,status, "PAYTM_CONFIG_NOT_SET", otp,mobileNo,amount,billNo,null);
		}
		int integrationId=(Integer) paytmDetails.get("integration_id");
		BasicDynaBean integrationDetails=new InstaIntegrationDao().getIntegrationDetailForCenter(integrationId);
		
		if(integrationDetails == null){ //merchant key or aeskey is not set
			return  handleWithdrawResponse(merchantOrderId,status, "PAYTM_CONFIG_NOT_SET", otp,mobileNo,amount,billNo,null);
		}
		
		String merchantId = (String) integrationDetails.get("merchant_id");  // Merchant ID passed in header and body
		if(merchantId == null || merchantId.isEmpty()){
			 return  handleWithdrawResponse(merchantOrderId,status, "MERCHANT_ID_NOT_SET", otp,mobileNo,amount,billNo,null);
		}
		String merchantKey = (String) integrationDetails.get("aeskey");  // Merchant key(AES Key) Used only for checksum
		if(merchantKey == null || merchantKey.isEmpty()){ 
			 return  handleWithdrawResponse(merchantOrderId,status, "MERCHANT_KEY_NOT_SET", otp,mobileNo,amount,billNo,null);
		}
		
		String api = (String) paytmDetails.get("url");
		//Prepare parameters required by Paytm
		Map<String, String> headerMap = new HashMap<>(); // headers to be passed
		headerMap.put("Content-Type", "application/json");
		headerMap.put("phone", mobileNo);
		headerMap.put("otp", otp);
		headerMap.put("mid",merchantId);
		
		Map<String,String> req= new HashMap<>();
		req.put("totalAmount", amount);
		req.put("currencyCode", "INR");
		req.put("merchantGuid", merchantId); 		
		req.put("merchantOrderId", String.valueOf(merchantOrderId));
		
		req.put("posId","pos");
		req.put("comment","TotpRest");
		req.put("industryType","Retail");
		
		Map<String ,Object > postData = new HashMap<>(); // Post data to be sent
		postData.put("request", req);
		postData.put("operationType", "WITHDRAW_MONEY");
		postData.put("channel", "POS");
		postData.put("version", "1.0");
		PaytmWithdrawResponse response =  null;
		try {
			String postDataStr = new ObjectMapper().writeValueAsString(postData);
		
			//Generate checksum hash
			String checkSum = checkSumServiceHelper.genrateCheckSum(merchantKey, postDataStr);	
			headerMap.put("checksumhash", checkSum);
			//send HTTP request to paytm
			HttpResponse httpResponse = new HttpClient(
					new PaytmResponseHandler(),10000,10000).post(api, postDataStr, headerMap);
			logger.info("Sent POST request: " + postData +"\n and got response :  " + httpResponse.getMessage());
			//Parse response received
			if(httpResponse.getCode() < 0){ 
				return handleWithdrawResponse(merchantOrderId,status, "TIME_OUT", otp,mobileNo,amount,billNo,response);
			}
			ObjectMapper mapper = new ObjectMapper();
			response = mapper.readValue(httpResponse.getMessage(), PaytmWithdrawResponse.class);
			if(response != null && response.getStatus() != null && response.getStatus().equals("SUCCESS")){
				status = true;
			}
			return  handleWithdrawResponse(merchantOrderId,status,(response == null)? null : response.getStatusCode(), otp,mobileNo,amount,billNo,response);
		}
		catch (JsonProcessingException e) {
			logger.error("Unable to parse request to JSON:" + e);
			
		}
		catch (IOException e) {
			logger.error("Unable to send HTTP request to  paytm" + e);
		}
		catch(Exception e){
			logger.error("Unable to generate checksum. Merchant key is not valid " + e);
			return  handleWithdrawResponse(merchantOrderId,status, "CHECKSUM_ERROR", otp,mobileNo,amount,billNo,response);
		}
		return  handleWithdrawResponse(merchantOrderId,status, "DEFAULT", otp,mobileNo,amount,billNo,response);		
	}
	
	/**
	 *  Check Transaction Status
	 *  We use this API when we did not receive response from Paytm for WithDraw API in-time
	 */
	public static String checkTransaction(String merchantOrderId) throws Exception{

		BasicDynaBean paytmDetails = new InstaIntegrationDao().getActiveBean(CHECK_TRANSACTION_STATUS_INTEGRATION_NAME);		
		if(paytmDetails == null){ //paytm configuration is not set
			return  handleCheckStatusResponse(merchantOrderId,null);
		}
		int integrationId=(Integer) paytmDetails.get("integration_id");
		BasicDynaBean integrationDetails=new InstaIntegrationDao().getIntegrationDetailForCenter(integrationId);
		
		if(integrationDetails == null){ //key configuration is not set
			return  handleCheckStatusResponse(merchantOrderId,null);
		}
		
		String merchantId = (String) integrationDetails.get("merchant_id");  // Merchant ID passed in header and body
		if(merchantId == null || merchantId.isEmpty()){
			 return  handleCheckStatusResponse(merchantOrderId,null);
		}
		String merchantKey = (String) integrationDetails.get("aeskey");  // Merchant key(AES Key) Used only for checksum
		if(merchantKey == null || merchantKey.isEmpty()){ 
			 return  handleCheckStatusResponse(merchantOrderId,null);
		}
		
		String api = (String) paytmDetails.get("url");
		
		//Prepare parameters required by Paytm
		Map<String, String> headerMap = new HashMap<>(); // headers to be passed
		headerMap.put("Content-Type", "application/json");
		headerMap.put("mid",merchantId);
		
		Map<String,String> req= new HashMap<>();
		req.put("txnType", "withdraw");
		req.put("requestType", "merchanttxnid");
		req.put("txnId", merchantOrderId); 
		req.put("merchantGuid", merchantId); 
		
		Map<String ,Object > postData = new HashMap<>(); // Post data to be sent
		postData.put("request", req);
		postData.put("operationType", "CHECK_TXN_STATUS");
		postData.put("platformName", "PayTM");
		PaytmCheckStatusResponse response =  null;
		try {
			String postDataStr = new ObjectMapper().writeValueAsString(postData);
		
			//Generate checksum hash
			String checkSum = checkSumServiceHelper.genrateCheckSum(merchantKey, postDataStr);	
			headerMap.put("checksumhash", checkSum);
			//send HTTP request to paytm
			HttpResponse httpResponse = new HttpClient(
					new PaytmResponseHandler(),10000,10000).post(api, postDataStr, headerMap);
			logger.info("Sent POST request: " + postData +"\n and got response :  " + httpResponse.getMessage());
			//Parse response received
			if(httpResponse.getCode() < 0){ 
				return handleCheckStatusResponse(merchantOrderId,null);
			}
			ObjectMapper mapper = new ObjectMapper();
			response = mapper.readValue(httpResponse.getMessage(), PaytmCheckStatusResponse.class);
			return  handleCheckStatusResponse(merchantOrderId,response);
		}
		catch (JsonProcessingException e) {
			logger.info("Unable to parse respose json. This occurs if the transaction for  order : " + merchantOrderId + ", did not initiate at Paytm." );
			
		}
		catch (IOException e) {
			logger.error("Unable to send HTTP request to  paytm" + e);
		}
		catch(Exception e){
			logger.error("Unable to generate checksum. Merchant key is not valid " + e);
			return  handleCheckStatusResponse(merchantOrderId,null);
		}
		return handleCheckStatusResponse(merchantOrderId,null);
	}
	/**
	 * Util method for PaytmWithdram / RequestForMoney
	 * @param merchantOrderId - The merchantOrderId that we sent to Paytm for reference
	 * @param status - Whether transaction is successful or not
	 * @param statusCode - The statusCode that we receive in response from Paytm or custom defined like TIME_OUT
	 * @param totp  - The Patient's otp 
	 * @param mobileNo - Mobile number of patient
	 * @param amount  - The amount to be deducted from patient's wallet
	 * @param billNo - The reference against transaction . We do not send this to Paytm
	 * @param response - response we recieved from paytm
	 * Note : We shall log only SUCCESS and TIME_OUT transactions
	 */
	private static String handleWithdrawResponse(int merchantOrderId,boolean status,String statusCode ,String totp,String mobileNo,
			String amount,String billNo,PaytmWithdrawResponse response) throws Exception{
		String transactionId =  (response == null ||  response.getResponse() == null) ? null : response.getResponse().getWalletSystemTxnId();
		String statusMessage = (response == null) ? null : response.getStatusMessage();
        String displayStatusMessage =	(statusCode != null && (statusCode.equals("403") || statusCode.equals("402")) )?
        		statusMessage :statusCodesMap.get(statusCode); // status message we return to client	
        
		if(displayStatusMessage == null){
			displayStatusMessage = statusCodesMap.get("DEFAULT");
		}
		if(status || (statusCode != null && statusCode.equals("TIME_OUT"))){
			insertTransaction(merchantOrderId,"WITHDRAW_MONEY",status,statusCode,statusMessage,totp,mobileNo,amount,billNo,transactionId);
		}
		Map<String,Object> data= new HashMap<>();
		data.put("result", status);
		data.put("message",displayStatusMessage);
		data.put("transaction_id",merchantOrderId);
		data.put("reference_no",transactionId);
		data.put("timed_out",(statusCode != null && statusCode.equals("TIME_OUT")));
		return  new ObjectMapper().writeValueAsString(data);
	}
	
	private static String handleCheckStatusResponse(String merchantOrderId ,PaytmCheckStatusResponse response) throws Exception{
		
		boolean status = response != null && response.getResponse() != null &&
				response.getResponse().getTxnList() != null && response.getResponse().getTxnList().get(0)!= null &&
						response.getResponse().getTxnList().get(0).getStatus() == PaytmCheckStatusResponse.SUCCESS;
		
		String transactionId = null;
		if(status){ // Update table with new status information
			transactionId = response.getResponse().getTxnList().get(0).getTxnGuid();
			BasicDynaBean transactionBean = paymentTransactionsDAO.getBean();
			transactionBean.set("status",status);
			transactionBean.set("transaction_id",transactionId);
			transactionBean.set("status_message",response.getResponse().getTxnList().get(0).getMessage());
			Map<String, String> keys = new HashMap<>();
			keys.put("payment_transaction_id",merchantOrderId);
			Connection con = DataBaseUtil.getConnection();
			paymentTransactionsDAO.update(con, transactionBean.getMap(), keys);			
		}
		Map<String,Object> data= new HashMap<>();
		data.put("result", status);
		data.put("reference_no",transactionId);
		return  new ObjectMapper().writeValueAsString(data);
	}
	
	/**
	 * Inserts transaction data to database
	 * @throws SQLException 
	 * @throws IOException 
	 */
	public static void insertTransaction(int logId,String txnType,boolean status,String statusCode,String statusMessage,String otp,String mobileNo,String amount,
			String billNo,String transactionId) throws SQLException, IOException {
		Connection con = null;
		boolean success = true;	
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			BasicDynaBean transactionBean = paymentTransactionsDAO.getBean();
			transactionBean.set("payment_transaction_id",logId);
			transactionBean.set("transaction_type",txnType);
			transactionBean.set("mode_id",MODE_ID);
			transactionBean.set("totp",otp);
			transactionBean.set("mobile_no",mobileNo);
			transactionBean.set("amount",new BigDecimal(amount)); 
			transactionBean.set("bill_no",billNo);
			transactionBean.set("transaction_id",transactionId);
			transactionBean.set("status",status);
			transactionBean.set("status_code",statusCode);		
			transactionBean.set("status_message",statusMessage);
			success = paymentTransactionsDAO.insert(con, transactionBean);
		} 
		finally{
			DataBaseUtil.commitClose(con, success);
		}		
	}
}
