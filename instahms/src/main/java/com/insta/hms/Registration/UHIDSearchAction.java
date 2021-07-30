package com.insta.hms.Registration;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.common.http.HttpClient;
import com.insta.hms.common.http.HttpResponse;
import com.insta.hms.common.http.HttpResponseHandler;
import com.insta.hms.common.utils.JsonUtility;
import com.insta.hms.messaging.InstaIntegrationDao;

public class UHIDSearchAction extends DispatchAction{
	
	private static final String login = "uhid_login";
	
	private static final String getProfile = "uhid_getprofile";
	
	private static final String search = "uhid_hieSearch";
	
	private static final HttpClient prismClient = new HttpClient(new HttpResponseHandler(),10000,10000);
	
	private static final InstaIntegrationDao integrationDao = new InstaIntegrationDao();
	
	private static final UHIDSearchDAO searchDao = new UHIDSearchDAO();
	
	@IgnoreConfidentialFilters
	public ActionForward getUHID(ActionMapping mapping, ActionForm form,HttpServletRequest request,
            HttpServletResponse response) {
				
				return mapping.findForward("UHIDSearchScreen");
	}
	
	public ActionForward getDetails(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse resp) throws SQLException, ParseException {
		
		List<BasicDynaBean> patientDetailsList = new ArrayList<BasicDynaBean>();
		String error="";
		List<String> searchColumns = Arrays.asList("mr_no","patient_name","patient_gender","patient_phone","custom_field1","custom_field2","custom_field3","dateofbirth","middle_name","last_name");
		HashMap<String, String> filterMap = new HashMap<String, String>();
		if(req.getParameter("uhid") != null && !req.getParameter("uhid").equals("")) {
			filterMap.put("mr_no", req.getParameter("uhid"));
			filterMap.put("oldmrno", req.getParameter("uhid"));
		}
		if(req.getParameter("phone") != null && !req.getParameter("phone").equals(""))
			filterMap.put("patient_phone", req.getParameter("phone"));
		if((req.getParameter("phone") != null && !req.getParameter("phone").equals("")) || 
				(req.getParameter("uhid") != null && !req.getParameter("uhid").equals("")))
			patientDetailsList = searchDao.getPatientList(searchColumns,filterMap,"mr_no");
		
		for(BasicDynaBean patBean : patientDetailsList){
			patBean.set("custom_field1", "local");
			patBean.set("custom_field2", "Insta Local System");
		}
		req.setAttribute("systemType","l");
		
		if(req.getParameter("all_systems") != null ) {
			try {
				req.setAttribute("systemType","a");
					
				String remoteSystemDetails = getRemoteSystemDetails(req.getParameter("uhid"), req.getParameter("phone"),null);
//				String remoteSystemDetails = "{\"errorCode\":0,\"errorMsg\":null,\"errorType\":null,\"response\":[{\"userId\":\"1\",\"uhid\":\"MR000017\",\"hasLabResult\":\"false\",\"firstName\":\"Test\",\"middleName\":\"\",\"lastName\":\"\",\"fullName\":null,\"siteName\":\"Insta health\",\"activationStatus\":null,\"lastLogOn\":null,\"registrationDate\":\"14-11-2014 12:00 AM\",\"mobileNumber\":\"\",\"activationDate\":null,\"status\":\"NOT_ACTIVATED\",\"tat\":\"\",\"loginId\":\"mr000017 46529b2dfd241d89ef374bac8e6ae55\",\"importedDate\":\"01-04-2015 05:50 PM\",\"gender\":\"male\",\"age\":45,\"relationship\":null},{\"userId\":\"2\",\"uhid\":\"MR000017\",\"hasLabResult\":\"false\",\"firstName\":\"Test\",\"middleName\":\"\",\"lastName\":\"\",\"fullName\":null,\"siteName\":\"Instahealth Hyderabad\",\"activationStatus\":null,\"lastLogOn\":null,\"registrationDate\":\"14-11-2014 12:00 AM\",\"mobileNumber\":\"\",\"activationDate\":null,\"status\":\"NOT_ACTIVATED\",\"tat\":\"\",\"loginId\":\"mr000017 3f84f22f8a90412c9a4d1b8431d72b0e\",\"importedDate\":\"02-04-2015 01:23 PM\",\"gender\":\"male\",\"age\":45,\"relationship\":null}]}";
//				String remoteSystemDetails = "{\"errorCode\":-1010,\"errorMsg\":null,\"errorType\":null,\"response\":null}";
				HashMap<String, Object> map = new HashMap<String, Object>();
		        ObjectMapper mapper = new ObjectMapper();
		        map = mapper.readValue(remoteSystemDetails, new TypeReference<Map<String, Object>>(){});
		        List<HashMap<String, Object>> patdetails = (List<HashMap<String, Object>>) map.get("response");
				Boolean localPatientFlag = false;
				if(patientDetailsList.size() != 0) {
					for (int i = 0; i < patientDetailsList.size(); i++) {
						if(patientDetailsList.get(i).get("mr_no").equals(req.getParameter("uhid"))) {
							localPatientFlag = true;
						}
					}
				}
		        if(patdetails != null) {
			        for(int i=0; i<patdetails.size();i++) {
						if (!localPatientFlag || (localPatientFlag && !patdetails.get(i).get("uhid").equals(req.getParameter("uhid")))) {
							BasicDynaBean temp = searchDao.getBean();
							temp.set("mr_no", patdetails.get(i).get("uhid"));
							temp.set("patient_phone", patdetails.get(i).get("mobileNumber"));
							temp.set("patient_name", patdetails.get(i).get("firstName"));
							if (null != patdetails.get(i).get("lastName"))
								temp.set("last_name", patdetails.get(i).get("lastName"));
							else
								temp.set("last_name", "");
							if (null != patdetails.get(i).get("middleName"))
								temp.set("middle_name", patdetails.get(i).get("middleName"));
							else
								temp.set("middle_name", "");
							if (null != patdetails.get(i).get("gender"))
								temp.set("patient_gender",
										patdetails.get(i).get("gender").toString().equals("male") ? "M" : "F");
							temp.set("custom_field1", "remote");
							temp.set("custom_field2", patdetails.get(i).get("siteName"));
							if (null != patdetails.get(i).get("age"))
								temp.set("custom_field3", patdetails.get(i).get("age").toString());

							patientDetailsList.add(temp);
						}
			        } 
		        }
		        else{
		        	error="Could Not Read Details From Remote System";
		        }
			} catch (Exception e) {
				error=error+e.getMessage();
				e.printStackTrace();
			} 
		}
		
		
		req.setAttribute("patientDetailsList", ConversionUtils.listBeanToListMap(patientDetailsList));
		req.setAttribute("errorMessage", error);
		
		return mapping.findForward("UHIDSearchScreen");
	}
	
	public static String getRemoteSystemDetails(String uhid, String phoneNumber,String name) throws Exception{
		BasicDynaBean remoteSystemLoginDetails = integrationDao.getActiveBean(login);
		if(remoteSystemLoginDetails == null){ 
			throw new Exception("Remote System Login Details/URL not Configured");
		}
		String url = (String) remoteSystemLoginDetails.get("url");
		String creds = "userId=" + ((String) remoteSystemLoginDetails.get("userid"))
					   + "&password=" + ((String) remoteSystemLoginDetails.get("password"));
		
		Map<String, String> headerMap = new HashMap<>();
		headerMap.put("Content-Type", "application/x-www-form-urlencoded");
		
		HttpResponse loginResponse = prismClient.post(url, creds, headerMap);
		String token="";
		try{
			if(null != loginResponse.getMessage()){
				Map<String, Object> response = JsonUtility.toObjectMap(loginResponse.getMessage());
				token = (String) response.get("response");
			}
		}catch(Exception e){
			throw new Exception("Login to remote system failed.");
		}
		Map<String,String> queryParams = new HashMap<>();
		queryParams.put("authToken", token);
						
		BasicDynaBean remoteSystemSearchDetails = integrationDao.getActiveBean(search);
		if(remoteSystemSearchDetails == null){ 
			throw new Exception("Remote System Search Details/URL not Configured");
		}
		String getSearchUrl = (String) remoteSystemSearchDetails.get("url");
		if (phoneNumber != null && !phoneNumber.trim().isEmpty()) {	
			queryParams.put("mobile", phoneNumber);
		}
		if (uhid != null && !uhid.trim().isEmpty()) {
			queryParams.put("uhid", uhid);
		}
		headerMap.put("Content-Type", "application/json");
		HttpResponse httpResponseForSearch = prismClient.get(getSearchUrl, queryParams, headerMap);
		
		return httpResponseForSearch.getMessage() != null ? httpResponseForSearch.getMessage() : "";
	}
}
