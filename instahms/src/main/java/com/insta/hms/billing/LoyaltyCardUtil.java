package com.insta.hms.billing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.GenericRepository;
import com.insta.hms.common.http.HttpClient;
import com.insta.hms.common.http.HttpResponse;
import com.insta.hms.messaging.InstaIntegrationDao;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class LoyaltyCardUtil {
	
static Logger logger = LoggerFactory.getLogger(LoyaltyCardUtil.class);
	
  protected static final String TRANSACTION_LOG_TABLE = "payment_transactions";
	private static final int LOYALTY_CARD_MODE_ID  = -3;
	private static final String INTEGRATION_NAME = "loyalty_card";
	private static final Map<String,String> statusCodesMap = new HashMap<>();	
	static{
		statusCodesMap.put("DEFAULT","Transaction Failed. Please re-try.");
		statusCodesMap.put("SUCCESS","Transaction is successfull.");
		statusCodesMap.put("TIME_OUT","Request Timed-out. Please re-try.");
		statusCodesMap.put("CONFIG_NOT_SET","Configuration is not set. Please contact your system administrator.");
		statusCodesMap.put("USERNAME_NOT_SET","Username is missing. Please contact your system administrator.");
	}
	
	public static  String  requestForMoney(String mobileNo, String otp,String amount, String billNo, String apiType) throws SQLException, JsonProcessingException {
		BasicDynaBean loyaltyCardDetails = new InstaIntegrationDao().getActiveBean(INTEGRATION_NAME);
		boolean  status = false;
		
		if(loyaltyCardDetails == null){ //integration config not set
			return  handleWalletBalanceResponse(status, "CONFIG_NOT_SET","");
		}
		String userid = (String) loyaltyCardDetails.get("userid");
		if(userid == null || userid.isEmpty()){
			return  handleWalletBalanceResponse(status, "CONFIG_NOT_SET", "");
		}
		String password = (String) loyaltyCardDetails.get("password");
		if(password == null || password.isEmpty()){
			return  handleWalletBalanceResponse(status, "CONFIG_NOT_SET","");
		}
		
		
		int integrationId=(Integer) loyaltyCardDetails.get("integration_id");
		BasicDynaBean integrationDetails=new InstaIntegrationDao().getIntegrationDetailForCenter(integrationId);
		
		if(integrationDetails == null){ //center_integration_details not set
			return  handleWalletBalanceResponse(status, "CONFIG_NOT_SET", "");
		}
		
		String storeId = (String) integrationDetails.get("store_code");
		if(storeId == null || storeId.isEmpty()){
			 return  handleWalletBalanceResponse(status, "STORE_CODE_NOT_SET", "");
		}
			
		String api = (String) loyaltyCardDetails.get("url");
		api=api+"/"+apiType;
		Map<String, String> params = new HashMap<>();
		params.put("customer_mobile", mobileNo);
		params.put("userid", userid);
		params.put("password", password); 
		
		if(apiType.equalsIgnoreCase("GET_WALLET_POINTS")){
			params.put("customer_points", amount);
		}
		if(apiType.equalsIgnoreCase("GET_WALLET_REDEMPTION")){
			params.put("customer_points", amount);
			params.put("store_code", storeId);
			params.put("passcode", otp);
			params.put("ref_bill_no", billNo);
		}
		
		Map<String, String> headerMap = new HashMap<>();
		headerMap.put("Content-Type", "application/json");
		try {
			String postDataStr = new ObjectMapper().writeValueAsString(params);
			//send HTTP request 
			HttpResponse httpResponse = new HttpClient(new PaytmResponseHandler(),10000,10000).post(api, postDataStr, headerMap);
			logger.info("Sent POST request: " + params +"\n and got response :  " + httpResponse.getMessage());
			//Parse response received
			if(httpResponse.getCode() < 0){ 
				return handleWithdrawResponse(status, "TIME_OUT", otp,mobileNo,amount,billNo);
			}
			if(httpResponse.getCode() == 404){ 
				return handleWithdrawResponse(status, "DEFAULT", otp,mobileNo,amount,billNo);
			}
			ObjectMapper mapper = new ObjectMapper();
			Map<String, Map<String, Object>> map = mapper.readValue(httpResponse.getMessage(),new TypeReference<Map<String, Object>>() {});
			String apiSuccess = (String) map.get(apiType+"Result").get("success");
			if(apiType.equalsIgnoreCase("GET_WALLET_REDEMPTION")){
				apiSuccess = (String) map.get(apiType+"Result").get("Success");
			}
			String apiMsg = (String) map.get(apiType+"Result").get("message");
			if(apiSuccess != null && apiSuccess.equals("true")){
				status = true;
			}
			if(apiType.equalsIgnoreCase("GET_CUSTOMER_WALLET_BALANCE")){
				Map<String,String> res=(Map<String, String>) map.get(apiType+"Result").get("result");
				String walletBalance = res.get("wallet_balance");
				return handleWalletBalanceResponse(status,apiMsg,walletBalance);
			}else if(apiType.equalsIgnoreCase("GET_WALLET_POINTS")){
				Map<String,String> res=(Map<String, String>) map.get(apiType+"Result").get("result");
				String pointsValue = res.get("points_value");
				return handleSendOTPResponse(status,apiMsg,pointsValue);
			}
			return  handleWithdrawResponse(status,apiMsg, otp,mobileNo,amount,billNo);
		}
		catch (JsonProcessingException e){
			logger.error("Exception occured", e);
		} catch (IOException e) {
			logger.error("Exception occured", e);
		}
		return  handleWithdrawResponse(status, "DEFAULT", otp,mobileNo,amount,billNo);
	}
	
	public static String handleWalletBalanceResponse(boolean status,String statusCodeMsg ,String walletBalance) throws JsonProcessingException {
		String displayStatusMessage;
		if(statusCodeMsg != null && statusCodesMap.get(statusCodeMsg)!=null)
			displayStatusMessage = statusCodesMap.get(statusCodeMsg);
		else
			displayStatusMessage=statusCodeMsg;
		Map<String,Object> data= new HashMap<>();
		data.put("result", status);
		data.put("message",displayStatusMessage);
		data.put("wallet_balance",walletBalance);
		return  new ObjectMapper().writeValueAsString(data);
	}
	
	public static String handleSendOTPResponse(boolean status,String statusCodeMsg ,String pointsValue) throws JsonProcessingException {
		String displayStatusMessage;
		if(statusCodeMsg != null && statusCodesMap.get(statusCodeMsg)!=null)
			displayStatusMessage = statusCodesMap.get(statusCodeMsg);
		else
			displayStatusMessage=statusCodeMsg;
		Map<String,Object> data= new HashMap<>();
		data.put("result", status);
		data.put("message",displayStatusMessage);
		data.put("points_value",pointsValue);
		return  new ObjectMapper().writeValueAsString(data);
	}
	protected static String handleWithdrawResponse(boolean status,String statusCodeMsg ,String totp,String mobileNo,
			String amount,String billNo) throws SQLException, JsonProcessingException {
		int logId = new GenericDAO(TRANSACTION_LOG_TABLE).getNextSequence(); // Next sequence of Transaction log table
		String displayStatusMessage ;
		if(statusCodeMsg != null && statusCodesMap.get(statusCodeMsg)!=null)
			displayStatusMessage = statusCodesMap.get(statusCodeMsg);
		else
			displayStatusMessage=statusCodeMsg;	
		insertTransaction(logId,status,statusCodeMsg,totp,mobileNo,amount,billNo);
		Map<String,Object> data= new HashMap<>();
		data.put("result", status);
		data.put("message",displayStatusMessage);
		data.put("transaction_id",logId);
		return  new ObjectMapper().writeValueAsString(data);
	}
	
	
	/**
	 * Inserts transaction data to database
	 */
	public static void insertTransaction(int logId,boolean status,String statusMessage,String otp,String mobileNo,String amount,
			String billNo) {
		GenericRepository repository = new GenericRepository(TRANSACTION_LOG_TABLE);
		BasicDynaBean transactionBean = repository.getBean();
		transactionBean.set("payment_transaction_id",logId);
		transactionBean.set("transaction_type","Loyalty_Withdraw");
		transactionBean.set("mode_id",LOYALTY_CARD_MODE_ID);
		transactionBean.set("totp",otp);
		transactionBean.set("mobile_no",mobileNo);
		if(amount==null)
			amount="0";
		transactionBean.set("amount",new BigDecimal(amount)); 
		transactionBean.set("bill_no",billNo);
		transactionBean.set("status",status);
		transactionBean.set("status_message",statusMessage);
		repository.insert(transactionBean);
	}

}
