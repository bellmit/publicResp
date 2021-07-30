/**
 *
 */
package com.insta.hms.pbmauthorization;

import com.bob.hms.common.CenterHelper;
import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.erxprescription.generated.ERxValidateTransaction;
import com.insta.hms.erxprescription.generated.ERxValidateTransactionSoap;
import com.insta.hms.master.InsuranceCompMaster.InsuCompMasterDAO;
import com.insta.hms.pbmauthorization.generated.Webservices;
import com.insta.hms.pbmauthorization.generated.WebservicesSoap;
import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.http.HttpHeaders;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.OutputStream;
import java.net.ConnectException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.ws.Holder;
import javax.xml.ws.WebServiceClient;

/**
 * @author lakshmi
 *
 */
public class PBMRequestsAction extends BaseAction {

	static Logger log = LoggerFactory.getLogger(PBMRequestsAction.class);

	private static PBMPrescriptionsDAO pbmdao;
	private static PriorAuthorizationHelper priorAuthHelper;
    private static final GenericDAO pbmRequestApprovalDetailsDAO =
        new GenericDAO("pbm_request_approval_details");

	public PBMRequestsAction() {
		pbmdao = new PBMPrescriptionsDAO();
		priorAuthHelper = new PriorAuthorizationHelper();
	}

	@SuppressWarnings("unchecked")
	@IgnoreConfidentialFilters
	public  ActionForward getRequests(ActionMapping mapping,ActionForm fm,
			HttpServletRequest request,HttpServletResponse response) throws SQLException,Exception{
		Integer userCenterId = RequestContext.getCenterId();
		String errorMsg = CenterHelper.authenticateCenterUser(userCenterId);
		if(errorMsg != null) {
			request.setAttribute("error", errorMsg);
			return mapping.findForward("list");
		}

		Map<Object,Object> map= getParameterMap(request);
		JSONSerializer js = new JSONSerializer().exclude("class");
		PagedList list = PBMRequestsDAO.searchPBMRequestList(map, ConversionUtils.getListingParameter(map));
		request.setAttribute("pagedList", list);
		
		List<String> columns = new ArrayList<>();
		columns.add("insurance_co_id");
		columns.add("insurance_co_name");
		request.setAttribute("insCompList", js.serialize(ConversionUtils.listBeanToListMap(
				new InsuCompMasterDAO().listAll(columns, "status", "A", "insurance_co_name"))));

		request.setAttribute("insCategoryList", js.serialize(ConversionUtils.listBeanToListMap(
				new GenericDAO("insurance_category_master").listAll(null, "status", "A", "category_name"))));
    
		columns.clear();
		columns.add("tpa_name");
		columns.add("tpa_id");
		request.setAttribute("tpaList",js.serialize(ConversionUtils.listBeanToListMap(
				new GenericDAO("tpa_master").listAll(columns, "status", "A", "tpa_name"))));

		columns.clear();
		columns.add("plan_id");
		columns.add("plan_name");
		columns.add("category_id");
		request.setAttribute("planList",js.serialize(ConversionUtils.listBeanToListMap(
				new GenericDAO("insurance_plan_main").listAll(columns, "status", "A", "plan_name"))));
		return mapping.findForward("list");
	}

	public ActionForward viewPBMRequestXML(ActionMapping mapping,ActionForm fm,
			HttpServletRequest request,HttpServletResponse response) throws SQLException,Exception{

		String pbmPrescId = request.getParameter("pbm_presc_id");
		String testing = request.getParameter("testing");
		String requestType = "Authorization";
		HttpSession session = request.getSession(false);
		String userid = (String)session.getAttribute("userid");
		String healthAuthority = (String)session.getAttribute("loginCenterHealthAuthority");

		String err = null;
		// For PBM convert Issue UOM qty to Package UOM qty.
		if (healthAuthority.equals("HAAD")) {
			err = pbmdao.convertQtyToPackageUOM(pbmPrescId);
		}

		String activeMode = (String)session.getAttribute("shafafiya_pbm_active");
		if (healthAuthority.equals("DHA"))
			activeMode = "Y";

		String requestXML = new PBMRequestGenerator().generatePBMRequestXML(pbmPrescId,
							userid, requestType, testing, activeMode, false);
		String errStr = (requestXML.startsWith("Error")) ? requestXML : err;

		if (errStr != null) {

			request.setAttribute("error", errStr.toString());
			request.setAttribute("referer", request.getHeader("Referer").
					replaceAll("&" + FlashScope.FLASH_KEY + "=[0-9A-Fa-f]+", ""));
			return mapping.findForward("reportErrors");
		}else {

			response.setContentType("text/xml");
			OutputStream outputStream = response.getOutputStream();

			//byte[] decodedRequestXML = Base64.decodeBase64(requestXML.getBytes());
			// System.out.println("Base 64 Decoded RequestXML : " + new String(decodedRequestXML));
			outputStream.write(requestXML.getBytes());
			outputStream.flush();
			outputStream.close();
		}
		return null;
	}

	public ActionForward sendPBMRequest(ActionMapping mapping,ActionForm fm,
			HttpServletRequest request,HttpServletResponse response) throws SQLException,Exception{

		FlashScope flash = FlashScope.getScope(request);
		ActionRedirect redirect = new ActionRedirect(mapping.findForward("listRedirect"));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

		String pbmPrescId = request.getParameter("pbm_presc_id");
		String patientId = request.getParameter("patient_id");
		String testing = request.getParameter("testing");
		String requestType = "Authorization";
		HttpSession session = request.getSession(false);
		String userid = (String)session.getAttribute("userid");
		String healthAuthority = (String)session.getAttribute("loginCenterHealthAuthority");

		String err = null;
		// For PBM convert Issue UOM qty to Package UOM qty.
		if (healthAuthority.equals("HAAD")) {
			err = pbmdao.convertQtyToPackageUOM(pbmPrescId);
		}

		// TODO: If mod_eclaim_erx is enabled, then.. is testing member id required?
		String activeMode = (String)session.getAttribute("shafafiya_pbm_active");
		if (healthAuthority.equals("DHA"))
			activeMode = "Y";

		String requestXML = new PBMRequestGenerator().generatePBMRequestXML(pbmPrescId,
						userid, requestType, testing, activeMode, true);
		String errStr = (requestXML.startsWith("Error")) ? requestXML : err;

		if (errStr != null) {

			request.setAttribute("error", errStr.toString());
			request.setAttribute("referer", request.getHeader("Referer").
					replaceAll("&" + FlashScope.FLASH_KEY + "=[0-9A-Fa-f]+", ""));
			return mapping.findForward("reportErrors");
		}else {

			String serviceUser = null;
			String servicePwd = null;

			if (healthAuthority.equals("DHA")) {
				serviceUser = (String)session.getAttribute("dhpo_facility_user");
				servicePwd = (String)session.getAttribute("dhpo_facility_password");

			}else if (healthAuthority.equals("HAAD")) {
				serviceUser = (String)session.getAttribute("shafafiya_user");
				servicePwd = (String)session.getAttribute("shafafiya_password");
			}

			if (healthAuthority.equals("DHA")) {
				String connectErr = priorAuthHelper.isDHPOConnected();

				if (connectErr != null) {
					flash.error(PriorAuthorizationHelper.DHPOSystemDownTimeError);
					log.error(connectErr);
					return redirect;
				}
			}else if (healthAuthority.equals("HAAD")) {
				/*String connectErr = priorAuthHelper.isShafafiyaConnected();

				if (connectErr != null) {
					flash.error(PriorAuthorizationHelper.SystemDownTimeError);
					log.error(connectErr);
					return redirect;
				}*/
			}

			log.debug("PBM Request XML Content (Encoded) for PBM Presc Id: "+pbmPrescId+" is ... : " +requestXML);

			int pbm_presc_id = Integer.parseInt(pbmPrescId);
			List<String> columns = new ArrayList<String>();
			columns.add("pbm_presc_id");
			columns.add("pbm_request_id");
			columns.add("status");

			Map<String, Object> key = new HashMap<String, Object>();
			key.put("pbm_presc_id", pbm_presc_id);
			BasicDynaBean pbmPresBean = new PBMPrescriptionsDAO().findByKey(columns, key);

			columns = new ArrayList<String>();
			columns.add("pbm_request_id");
			columns.add("file_name");

			key = new HashMap<String, Object>();
			key.put("pbm_request_id", pbmPresBean.get("pbm_request_id"));
			BasicDynaBean pbmReqBean = pbmRequestApprovalDetailsDAO.findByKey(columns, key);

			try {

				// PBM Todo : Call PBMRequestSender().sendPriorRequest() instead

				//Call PBM API to upload transaction.
				Webservices pbmws = null;
				WebservicesSoap pbmwsoap = null;

				ERxValidateTransaction erxws = null;
				ERxValidateTransactionSoap erxwsoap = null;

				if (healthAuthority.equals("DHA")) {
					erxws = new ERxValidateTransaction();
					erxwsoap = erxws.getERxValidateTransactionSoap();

				}else if (healthAuthority.equals("HAAD")) {
					pbmws = new Webservices();
					pbmwsoap = pbmws.getWebservicesSoap();
				}

				byte[] fileContent = requestXML.getBytes();
				String fileName = (String)pbmReqBean.get("file_name");
				Holder<Integer> uploadTxnResult = new Holder<>();
				Holder<String> errorMessage = new Holder<>();
				Holder<byte[]> errorReport = new Holder<>();

				if (healthAuthority.equals("DHA") && erxwsoap != null) {
					// TODO: payer login and payer pwd??
					erxwsoap.uploadERxAuthorization(serviceUser, servicePwd, fileContent,
							fileName, uploadTxnResult, errorMessage, errorReport);

				}else if (healthAuthority.equals("HAAD") && pbmwsoap != null) {
					pbmwsoap.uploadTransaction(serviceUser, servicePwd, fileContent,
							fileName, uploadTxnResult, errorMessage, errorReport);
				}

				// Check uploadTransactionResult
				int txnResult = uploadTxnResult.value;
				PriorAuthorizationHelper.TransactionResults txn = PriorAuthorizationHelper.TransactionResults.getTxnResultMessage(txnResult);


				// PBM Todo : Call PBMResponseProcessor().processAuthorizationResponse
				// p = new PBMResponseProcessor();
				// String msg = p.processAuthorizationResponse(response); // response is return value from the call to PBMRequestSender().sendPriorRequest
				// if (response.getResponseCode() >= 0) {
						// Mark presc as sent
				// } else {
						// request.setAttribute("errorReport", msg);
				// }

				if (txnResult >= 0) {
					String msg = txn.getResultMsg();

					log.debug(" PBM prescription transaction upload successful. "+msg);

					// Mark PBM Prescription as Sent request.
					boolean success = pbmdao.markPBMPrescSent(pbm_presc_id, patientId);
					if (!success) {
						flash.error("Error while marking PBM prescription Request as Sent.");
			    		redirect.addParameter("pbm_presc_id", pbmPrescId);
			        	return redirect;
					}

				}else {
					String errMsg = txn.getResultMsg();
					byte[] errorReportBytes = errorReport.value;

					String errorReportStr = priorAuthHelper.getErrorReportbase64String(errorReportBytes); // Encoded string

					log.error("PBM Request Error Report (encoded) for PBM Presc Id: "+pbmPrescId+" is ... : " +errorReportStr);

					request.setAttribute("error", errMsg +" <br/> "+errorMessage.value);
					request.setAttribute("errorReport", errorReportStr);
					request.setAttribute("fileName", fileName);

					BasicDynaBean pbmPrescBean = pbmdao.getPBMPresc(pbm_presc_id);
					request.setAttribute("pbmPrescBean", pbmPrescBean);

					// Forward to PBM errors page for further correction/processing by user.
					return mapping.findForward("pbmServiceErrors");
				}

				log.debug("PBM Prescription "+ pbmPrescId+" Authorization XML is generated and uploaded.");

			}catch (ConnectException e) {
	      if (healthAuthority.equals("DHA")) {
	        flash.error(PriorAuthorizationHelper.DHPOSystemDownTimeError);
	        log.error("System Downtime... Cannot connect to " + ERxValidateTransaction.class.getAnnotation(WebServiceClient.class).targetNamespace());

	      }else if (healthAuthority.equals("HAAD")) {
	        flash.error(PriorAuthorizationHelper.SystemDownTimeError);
	        log.error("System Downtime(Provider, Daman, Shafafiya)... Cannot connect to " + Webservices.class.getAnnotation(WebServiceClient.class).targetNamespace());
	      }
      	redirect.addParameter("pbm_presc_id", pbmPrescId);
    		redirect.addParameter("pbm_presc_id@type", "integer");
    		return redirect;
			}
		}
		flash.info("PBM prescription Request is Sent.");
		redirect.addParameter("pbm_presc_id", pbmPrescId);
		redirect.addParameter("pbm_presc_id@type", "integer");
		return redirect;
	}

	@IgnoreConfidentialFilters
	public ActionForward getPBMErrorReport(ActionMapping mapping,ActionForm fm,
			HttpServletRequest request,HttpServletResponse response) throws Exception {

		HttpSession session = request.getSession(false);
		String userid = (String)session.getAttribute("userid");
		String pbmPrescId = request.getParameter("pbm_presc_id");
		String fileName = request.getParameter("fileName");
		String errorReportStr = request.getParameter("errorReport");

		String errorFileName = fileName + "_" + DataBaseUtil.getCurrentDate();
		File decodedDataFile = File.createTempFile("tempPBMErrorReportFile", "");

		// Decode the error report into a file (this is a zip file).
		String err = priorAuthHelper.decodeErrorReportbase64ToFile(errorReportStr, decodedDataFile);
		if (err == null) {

			response.setContentType("application/vnd.ms-excel");
			response.setHeader("Content-disposition", "attachment; filename=\""+errorFileName+".xls"+"\"");

			OutputStream outputStream = response.getOutputStream();

			// Read the zip file as write to output stream. The file zipped content has an excel sheet.
			err = priorAuthHelper.unzipErrorReportFile(decodedDataFile, outputStream);

			if (err != null) {
				request.setAttribute("error", err);

				// Forward to errors page.
				return mapping.findForward("reportErrors");
			}

			// The error report (zip file) can be downloaded directly.
			// To encounter an error if any other file(s) or a folder in the zipped file,
			// read the Excel sheet from the file for user to download.

			/*	response.setContentType("application/zip");
				response.setHeader("Content-disposition", "attachment; filename=\""+errorFileName+"\"");

				OutputStream outputStream = response.getOutputStream();
				FileInputStream fis = new FileInputStream(decodedDataFile);
				outputStream.write(DataBaseUtil.readInputStream(fis));

				outputStream.flush();
				outputStream.close();
				return null;
			*/
		}

		return null;
	}

	public  ActionForward cancelPBMRequest(ActionMapping mapping,ActionForm fm,
			HttpServletRequest request,HttpServletResponse response) throws SQLException,Exception{

		FlashScope flash = FlashScope.getScope(request);
		ActionRedirect redirect = new ActionRedirect(mapping.findForward("listRedirect"));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

		String pbmPrescId = request.getParameter("pbm_presc_id");
		String testing = request.getParameter("testing");
		String requestType = "Cancellation";
		HttpSession session = request.getSession(false);
		String userid = (String)session.getAttribute("userid");
		String healthAuthority = (String)session.getAttribute("loginCenterHealthAuthority");
		String patientId = request.getParameter("patient_id");

		String activeMode = (String)session.getAttribute("shafafiya_pbm_active");
		if (healthAuthority.equals("DHA"))
			activeMode = "Y";

		String requestXML = new PBMRequestGenerator().generatePBMRequestXML(pbmPrescId,
						userid, requestType, testing, activeMode, false);
		String errStr = (requestXML.startsWith("Error")) ? requestXML : null;

		if (errStr != null) {

			request.setAttribute("error", errStr.toString());
			request.setAttribute("referer", request.getHeader("Referer").
					replaceAll("&" + FlashScope.FLASH_KEY + "=[0-9A-Fa-f]+", ""));
			return mapping.findForward("reportErrors");
		}else {

			String serviceUser = null;
			String servicePwd = null;

			if (healthAuthority.equals("DHA")) {
				serviceUser = (String)session.getAttribute("dhpo_facility_user");
				servicePwd = (String)session.getAttribute("dhpo_facility_password");

			}else if (healthAuthority.equals("HAAD")) {
				serviceUser = (String)session.getAttribute("shafafiya_user");
				servicePwd = (String)session.getAttribute("shafafiya_password");
			}

			if (healthAuthority.equals("DHA")) {
				String connectErr = priorAuthHelper.isDHPOConnected();

				if (connectErr != null) {
					flash.error(PriorAuthorizationHelper.DHPOSystemDownTimeError);
					log.error(connectErr);
					return redirect;
				}
			}else if (healthAuthority.equals("HAAD")) {
				/*String connectErr = priorAuthHelper.isShafafiyaConnected();

				if (connectErr != null) {
					flash.error(PriorAuthorizationHelper.SystemDownTimeError);
					log.error(connectErr);
					return redirect;
				}*/
			}

			int pbm_presc_id = Integer.parseInt(pbmPrescId);
			List<String> columns = new ArrayList<String>();
			columns.add("pbm_presc_id");
			columns.add("pbm_request_id");
			columns.add("status");

			Map<String, Object> key = new HashMap<String, Object>();
			key.put("pbm_presc_id", pbm_presc_id);
			BasicDynaBean pbmPresBean = new PBMPrescriptionsDAO().findByKey(columns, key);

			columns = new ArrayList<String>();
			columns.add("pbm_request_id");
			columns.add("file_name");

			key = new HashMap<String, Object>();
			key.put("pbm_request_id", pbmPresBean.get("pbm_request_id"));
			BasicDynaBean pbmReqBean = pbmRequestApprovalDetailsDAO.findByKey(columns, key);

			try {
				// PBM Todo : This should go into PBMRequestSender.sendCancellation again

				//Call PBM API to upload transaction for cancellation.
				Webservices pbmws = null;
				WebservicesSoap pbmwsoap = null;

				ERxValidateTransaction erxws = null;
				ERxValidateTransactionSoap erxwsoap = null;

				if (healthAuthority.equals("DHA")) {
					erxws = new ERxValidateTransaction();
					erxwsoap = erxws.getERxValidateTransactionSoap();

				} else if (healthAuthority.equals("HAAD")) {
					pbmws = new Webservices();
					pbmwsoap = pbmws.getWebservicesSoap();
				}

				byte[] fileContent = requestXML.getBytes();
				String fileName = (String)pbmReqBean.get("file_name");
				Holder<Integer> uploadTxnResult = new Holder<Integer>();
				Holder<String> errorMessage = new Holder<String>();
				Holder<byte[]> errorReport = new Holder<byte[]>();

				if (healthAuthority.equals("DHA") && erxwsoap != null) {
					// TODO: payer login and payer pwd??
					erxwsoap.uploadERxAuthorization(serviceUser, servicePwd, fileContent,
							fileName, uploadTxnResult, errorMessage, errorReport);

				}else if (healthAuthority.equals("HAAD") && pbmwsoap != null) {
					pbmwsoap.uploadTransaction(serviceUser, servicePwd, fileContent,
							fileName, uploadTxnResult, errorMessage, errorReport);
				}

				// Check uploadTransactionResult
				int txnResult = uploadTxnResult.value;
				PriorAuthorizationHelper.TransactionResults txn = PriorAuthorizationHelper.TransactionResults.getTxnResultMessage(txnResult);

				if (txnResult >= 0) {
					String msg = txn.getResultMsg();

					log.debug(" PBM prescription cancel transaction upload successful. "+msg);

					// Mark PBM Prescription as Closed & set sent_for_approval flags to false
					boolean success = pbmdao.markPBMPrescClosed(pbm_presc_id, patientId);
					if (!success) {
						flash.error("Error while marking PBM prescription Request as Closed.");
			    		redirect.addParameter("pbm_presc_id", pbmPrescId);
			        	return redirect;
					}

				}else {
					String errMsg = txn.getResultMsg();
					byte[] errorReportBytes = errorReport.value;

					String errorReportStr = priorAuthHelper.getErrorReportbase64String(errorReportBytes); // Encoded string

					request.setAttribute("error", errMsg +" <br/> "+errorMessage.value);
					request.setAttribute("errorReport", errorReportStr);
					request.setAttribute("fileName", fileName);

					BasicDynaBean pbmPrescBean = pbmdao.getPBMPresc(pbm_presc_id);
					request.setAttribute("pbmPrescBean", pbmPrescBean);

					// Forward to PBM errors page for further correction/processing by user.
					return mapping.findForward("pbmServiceErrors");
				}

			}catch (ConnectException e) {
        if (healthAuthority.equals("DHA")) {
          flash.error(PriorAuthorizationHelper.DHPOSystemDownTimeError);
          log.error("System Downtime... Cannot connect to " + ERxValidateTransaction.class.getAnnotation(WebServiceClient.class).targetNamespace());

        }else if (healthAuthority.equals("HAAD")) {
          flash.error(PriorAuthorizationHelper.SystemDownTimeError);
          log.error("System Downtime(Provider, Daman, Shafafiya)... Cannot connect to " + Webservices.class.getAnnotation(WebServiceClient.class).targetNamespace());
        }
	    		redirect.addParameter("pbm_presc_id", pbmPrescId);
	    		redirect.addParameter("pbm_presc_id@type", "integer");
	        	return redirect;
			}
		}
		flash.info("PBM prescription Request is Cancelled.");
		redirect.addParameter("pbm_presc_id", pbmPrescId);
		redirect.addParameter("pbm_presc_id@type", "integer");
		return redirect;
	}

	@IgnoreConfidentialFilters
	public ActionForward getNotReturnedMedicineNames(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws SQLException, Exception{

		String prescID = request.getParameter("pbm_presc_id");
		List<BasicDynaBean> medicineList = PBMRequestsDAO.getNotReturnedMedicineNames(prescID);
		String medicineNames = "";
		for(int i=0; i<medicineList.size(); i++){
			medicineNames = medicineNames.concat((String)medicineList.get(i).get("medicine_name")).concat(", ");
		}

		JSONSerializer js = new JSONSerializer().exclude("class");
		response.setContentType("application/x-json");
		response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
		response.getWriter().write(js.serialize(medicineNames));

		return null;
	}

}
