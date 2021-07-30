/**
 *
 */
package com.insta.hms.pbmauthorization;


import com.bob.hms.common.CenterHelper;
import com.bob.hms.common.Constants;
import com.bob.hms.common.MimeTypeDetector;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.erxprescription.generated.ERxValidateTransaction;
import com.insta.hms.erxprescription.generated.ERxValidateTransactionSoap;
import com.insta.hms.eservice.EResponse;
import com.insta.hms.master.InsuranceCompMaster.InsuCompMasterDAO;
import com.insta.hms.pbmauthorization.generated.Webservices;

import eu.medsea.mimeutil.MimeUtil2;
import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.xml.sax.SAXParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.ConnectException;
import java.sql.Date;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
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
public class PBMApprovalsAction extends BaseAction {

	static Logger log = LoggerFactory.getLogger(PBMApprovalsAction.class);

	private static PBMPrescriptionsDAO pbmdao;
	private static GenericDAO pbmReqDAO;
	private static PriorAuthorizationHelper priorAuthHelper;
	private static PriorAuthorizationXmlProvider priorXMLProvider;
	private static PriorAuthXmlFileFormatProvider xmlfileprovider;

	public PBMApprovalsAction() {
		pbmdao = new PBMPrescriptionsDAO();
		pbmReqDAO = new GenericDAO("pbm_request_approval_details");
		priorAuthHelper = new PriorAuthorizationHelper();
		priorXMLProvider = new PriorAuthorizationXmlProvider();
		xmlfileprovider = new PriorAuthXmlFileFormatProvider();
	}

	private static int timeIntervalInHrs = -4;
	private final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy");
	private static final String STATUS = "status";
	private static final String LIST_REDIRECT = "listRedirect";
	private static final String PBM_FINALIZED = "pbm_finalized";
	private static final String PBM_PRESC_STATUS = "pbm_presc_status";
	private static final String PBM_REQUEST_ID = "pbm_request_id";
	private static final String PBM_PRESCRIPTION_ID = "pbm_presc_id";

	@SuppressWarnings("unchecked")
	@IgnoreConfidentialFilters
	public  ActionForward getApprovals(ActionMapping mapping,ActionForm fm,
			HttpServletRequest request,HttpServletResponse response) throws Exception{

		Integer userCenterId = RequestContext.getCenterId();
		String errorMsg = CenterHelper.authenticateCenterUser(userCenterId);
		if(errorMsg != null) {
			request.setAttribute(Constants.ERROR, errorMsg);
			return mapping.findForward("list");
		}

		Map<Object,Object> map= getParameterMap(request);
		JSONSerializer js = new JSONSerializer().exclude("class");
		PagedList list = PBMApprovalsDAO.searchPBMApprovalList(map, ConversionUtils.getListingParameter(map));
		request.setAttribute("pagedList", list);
		
		List<String> columns = new ArrayList<>();
		columns.add("insurance_co_id");
		columns.add("insurance_co_name");
		request.setAttribute("insCompList", js.serialize(ConversionUtils.listBeanToListMap(
				new InsuCompMasterDAO().listAll(columns, STATUS, "A", "insurance_co_name"))));

		request.setAttribute("insCategoryList", js.serialize(ConversionUtils.listBeanToListMap(
				new GenericDAO("insurance_category_master").listAll(null, STATUS, "A", "category_name"))));

		columns.clear();
		columns.add("tpa_name");
		columns.add("tpa_id");
		request.setAttribute("tpaList",js.serialize(ConversionUtils.listBeanToListMap(
				new GenericDAO("tpa_master").listAll(columns, STATUS, "A", "tpa_name"))));

		columns.clear();
		columns.add("plan_id");
		columns.add("plan_name");
		columns.add("category_id");
		request.setAttribute("planList",js.serialize(ConversionUtils.listBeanToListMap(
				new GenericDAO("insurance_plan_main").listAll(columns, STATUS, "A", "plan_name"))));
		return mapping.findForward("list");
	}

	@IgnoreConfidentialFilters
	public ActionForward getPriorAuthResponse(ActionMapping mapping,ActionForm fm,
			HttpServletRequest request,HttpServletResponse response) throws Exception {
		log.debug("getPriorAuthResponse start");
		FlashScope flash = FlashScope.getScope(request);
		ActionRedirect redirect = new ActionRedirect(mapping.findForward(LIST_REDIRECT));
		redirect.addParameter(PBM_FINALIZED, "Y");
		redirect.addParameter(PBM_PRESC_STATUS, "S");
		redirect.addParameter(PBM_PRESC_STATUS, "D");
		redirect.addParameter(PBM_PRESC_STATUS, "C");
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

		HttpSession session = request.getSession(false);
		String healthAuthority = (String)session.getAttribute("loginCenterHealthAuthority");
		
		String fromDateParam = request.getParameter("_from_date");
		String toDateParam = request.getParameter("_to_date");
				
		redirect.addParameter("_from_date", fromDateParam);
		redirect.addParameter("_to_date", toDateParam);

		String serviceUser = null;
		String servicePwd = null;

		if (healthAuthority.equals("DHA")) {
			serviceUser = (String)session.getAttribute("dhpo_facility_user");
			servicePwd = (String)session.getAttribute("dhpo_facility_password");

			String connectErr = priorAuthHelper.isDHPOConnected();

			if (connectErr != null) {
				flash.error(PriorAuthorizationHelper.DHPOSystemDownTimeError);
				log.error(connectErr);
				return redirect;
			}
		}
		else if (healthAuthority.equals("HAAD")) {
			serviceUser = (String)session.getAttribute("shafafiya_user");
			servicePwd = (String)session.getAttribute("shafafiya_password");
		}

		PBMApprovalsRetriever retriever = new PBMApprovalsRetriever(serviceUser, servicePwd);
		boolean txnsDownloaded = false;
		StringBuilder msg = new StringBuilder("Downloaded and updated PBM approval details for "); // Success message

		try { // try block 1
			ERxValidateTransaction erxws = null;
			ERxValidateTransactionSoap erxwsoap = null;

			if (healthAuthority.equals("DHA")) {
				//TODO: Use ERxApprovalsRetriever
				erxws = new ERxValidateTransaction();
				erxwsoap = erxws.getERxValidateTransactionSoap();

			}
			else if (healthAuthority.equals("HAAD")) {
				retriever.getWebService();
			}

			int txnResult = 0;
			PriorAuthorizationHelper.TransactionResults txn ;
			PBMAuthResponse pbmAuthResponse;

			// Call PBM API to check for new transactions.
			if (healthAuthority.equals("HAAD")) {

				// Check if there are new prior auth transactions for download
				pbmAuthResponse = (PBMAuthResponse) retriever.checkNewTransactions();
				if (pbmAuthResponse.isError() || pbmAuthResponse.isEmptyTransactions()) {
					String errMsg = pbmAuthResponse.getErrorMessage();
					if (errMsg != null && !errMsg.equals("")) {
						flash.error(errMsg);
						return redirect;
					}
				}
			}

			// If no errors then get Auth XMLs
			String foundTxnsXMLStr= null;

			if (healthAuthority.equals("DHA")) {
				//TODO: Use ERxApprovalsRetriever methods instead of calling web service methods directly
				Holder<String> errorMessage = new Holder<>();
				Holder<Integer> newTransactionsResult = new Holder<>();
				Holder<String> foundTxns = new Holder<>(); // Prior Auth Response XML Encoded String

				// Call PBM API to check for new transactions.
				erxwsoap.getNewTransactions(serviceUser,
						servicePwd, newTransactionsResult, foundTxns, errorMessage);

				txnResult = newTransactionsResult.value;
				txn = PriorAuthorizationHelper.TransactionResults.getTxnResultMessage(txnResult);

				if (txnResult < 0) { // If there is an error
					String errMsg = txn.getResultMsg();
					errMsg = "Error while getting New Prior Authorizations... "+errMsg+" <br/> "+errorMessage.value;
					flash.error(errMsg);
		    		log.error(errMsg);
					return redirect;

				}  else if (txnResult == 2) {
					// No new transactions available.
					String errMsg = txn.getResultMsg();
					flash.info(errMsg);
					return redirect;

				}
				foundTxnsXMLStr = foundTxns.value;
				StringBuilder xml = new StringBuilder();
				xml.append("<?xml version=\'1.0\' encoding=\'UTF-8\'?>").append("\n");
				xml.append(foundTxnsXMLStr);
				foundTxnsXMLStr = xml.toString();

			}
			else if (healthAuthority.equals("HAAD")) {
				// Call PBM API to get new transactions.
				Calendar calender = Calendar.getInstance();
				if (toDateParam != null && !toDateParam.isEmpty()){
					java.util.Date tdt = dateFormatter.parse(toDateParam);
					calender.setTime(tdt);
					calender.add(Calendar.HOUR_OF_DAY, 23);
				}else{
					calender.setTime(new java.util.Date(calender.getTimeInMillis()));
				}
				Date toDate = new Date(calender.getTimeInMillis());

				Calendar cto = Calendar.getInstance();
				if (fromDateParam != null && !fromDateParam.isEmpty()){
					java.util.Date fdt = dateFormatter.parse(fromDateParam);
					cto.setTime(fdt);
				}else{
					cto.setTime(new java.util.Date(cto.getTimeInMillis()));
					cto.add(Calendar.HOUR_OF_DAY, timeIntervalInHrs);
				}
				cto.add(Calendar.HOUR_OF_DAY, timeIntervalInHrs);
				Date fromDate = new Date(cto.getTimeInMillis());

				// Get approvals for the last 4 hrs
				pbmAuthResponse = (PBMAuthResponse)retriever.getApprovalFileList(fromDate, toDate);
				if (pbmAuthResponse.isError()){
					String errMsg = pbmAuthResponse.getErrorMessage();
				    if (errMsg != null && !errMsg.equals("")) {
				    	flash.error(errMsg);
				       	return redirect;
				    }
				} else if (pbmAuthResponse.isEmptyTransactions()){
					String errMsg = "No transactions found to download";
					flash.error(errMsg);
					return redirect;
				}

				// Get the file list xml
				StringBuilder builder = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
				builder.append("\n");
				StringWriter writer = new StringWriter();
				IOUtils.copy(pbmAuthResponse.getInputStream(), writer, ("UTF-8"));
				builder.append(writer.toString());
				log.debug("xml received from web service = " + builder.toString());
				foundTxnsXMLStr = builder.toString();
			}

			XMLFiles xmlfiles = xmlfileprovider.getPriorAuthXmlFileFormatMetaData(foundTxnsXMLStr);

			ArrayList<XMLFile> xmlfilesList = xmlfiles.getFiles();
			boolean isZipFile;

			for (XMLFile xmlfile : xmlfilesList) { // for loop 1

				String fileId = xmlfile.getFileId();
				String xmlfileName = xmlfile.getFileName();
				String senderId = xmlfile.getSenderId();
				String receiverId = xmlfile.getReceiverId();
				String recordCount = xmlfile.getRecordCount();
				String isDownloaded = xmlfile.getIsDownloaded();
				
				if (isDownloaded.equalsIgnoreCase("true")){
					continue;
				}

				isZipFile = false;

				//PF1506-PR000115-20140227112844-66.From.A001.To.PF1506.File.1.xml.zip
				log.debug("XML file name in received XML File List: "+xmlfileName);
				// Received file has found to be either .zip file (in case of Daman) or .xml file (in case of others)
				if (xmlfileName.endsWith(".zip"))
					isZipFile = true;

				log.debug("value of isZipFile = " + isZipFile);

				/*
				 * STEP 1: Compare file name to check if there is any matching record
				 */
				// Search PBM prescription by comparing file name and other parameters.
				BasicDynaBean pbmRequestBean = pbmdao.searchPBMPrescBean(xmlfileName, senderId, receiverId, recordCount);

				String pbmRequestId = (pbmRequestBean != null && pbmRequestBean.get(PBM_REQUEST_ID) != null) ? (String)pbmRequestBean.get(PBM_REQUEST_ID) : null;
				String pbmRequestFileId = (pbmRequestBean != null && pbmRequestBean.get(Constants.FILE_ID) != null) ? (String)pbmRequestBean.get(Constants.FILE_ID) : null;

				// STEP 1 (a): Matching record found, therefore update file id
				if (pbmRequestId != null && !pbmRequestId.trim().equals("")){ // record found
					log.debug("Found a matching record for pbmRequestId = " + pbmRequestId);

					// if fileId is null or fileId is different
					if (pbmRequestFileId == null || !pbmRequestFileId.trim().equals(fileId.trim())){
						// update the record with the fileId
						boolean fileUpdate = pbmdao.updatePBMRequestFileId(pbmRequestId, fileId);
						if (!fileUpdate) {
							String errMsg = "Error while updating PBM Request File Id: "+fileId;
							flash.error(flash.get(Constants.ERROR) == null ? errMsg : (String)flash.get(Constants.ERROR)+", <br/> "+errMsg);
					    	log.error(errMsg);
							continue;
						} else {
							log.debug("Updated PBM request id: " + pbmRequestId + " with file id " + fileId);
						}
					} else { // file id is already updated and is the same as in the db
						log.debug("file id is already updated, so moving to next record");
						continue;
					}
				} else { // STEP 1 (b) : No matching record found
					log.debug("No record found for xmlfileName: " + xmlfileName);
				}

				/*
				 * STEP 2: Download the file for the fileId in any case.
				 * For matching files from STEP 1, the approval details need to be updated
				 * For non-matching files from STEP 2, the file is to be downloaded and the ID compared
				 * with the Request ID
				 */
				// Use the fileId to download the data.
				pbmAuthResponse = (PBMAuthResponse) downloadFile(fileId, healthAuthority, serviceUser, servicePwd);
				if (pbmAuthResponse != null && pbmAuthResponse.isError()){
					String errMsg = pbmAuthResponse.getErrorMessage();
				    if (errMsg != null && !errMsg.equals("")) {
				    	flash.error(errMsg);
				       	continue;
				    }
				}

				// No error in downloading file
				byte[] fileBytes = null;
				String strFileName = null ;
				if(pbmAuthResponse != null) {
					fileBytes = IOUtils.toByteArray(pbmAuthResponse.getInputStream());
					strFileName = pbmAuthResponse.getResultParams()[0].toString();
					log.error("After Downloading Transaction file with PBM Request Id :" + pbmRequestId + "... with File Id: " + fileId + " <br/>" +
							"  with Error message: " + pbmAuthResponse.getErrorMessage());
				}
				String errorMsg = null;

				FileOutputStream dataOutPutStream = null;
				try { // try block 2
					InputStream is = null;
					File dataFile = File.createTempFile("tempPBMXMLFile", "");
					dataOutPutStream = new FileOutputStream(dataFile);
					dataOutPutStream.write(fileBytes);

					File xmlDataFile = File.createTempFile("tempPBMXMLDataFile", "");
					FileOutputStream outputStream = new FileOutputStream(xmlDataFile);

					// If it is a zip file, read the zip file and write to output file.
					if (isZipFile) {
						String err = priorAuthHelper.unzipErrorReportFile(dataFile, outputStream);

						if (err != null) {
							flash.error(flash.get(Constants.ERROR) == null ? err : (String)flash.get(Constants.ERROR)+", <br/> "+err);
							log.error(err);
							continue;
						}
					} else { // not zip file
						IOUtils.write(fileBytes, outputStream);
					}

					if (log.isDebugEnabled()) {
						String authXmlStr = FileUtils.readFileToString(xmlDataFile);
						log.debug("Processing PBM Prior Auth XML for PBM Request Id: "+pbmRequestId+", fileID = " + fileId + ", fileName = " + strFileName + " is ... : " +authXmlStr);
					}

					is = new FileInputStream(xmlDataFile);

					// Digest the Prior Auth XML content.
					PriorAuthorization desc = priorXMLProvider.getPriorAuthorizationMetaDataDescription(is);

					/*
					 * STEP 3: From the downloaded xml, read the ID.
					 * Check whether the ID exists in our application
					 */
					// If fileName did not match any record, pbmRequestId is not set.
					// Therefore get the pbmRequestId from the xml.
					if (pbmRequestId == null || pbmRequestId.trim().equals("")) {
						pbmRequestId = desc.getAuthorization().getAuthorizationID();
						log.debug("pbmRequestId from the xml = " + pbmRequestId);

						// Does a record exist in our application with this ID?
						List<String> cols = new ArrayList<>();
						cols.add(PBM_REQUEST_ID);

						Map<String, Object> fld = new HashMap<>();
						fld.put(PBM_REQUEST_ID, pbmRequestId);
						BasicDynaBean pbmReqAppDtlsBean = pbmReqDAO.findByKey(cols, fld);

						// STEP 3(a). No matching record for the ID, move to the next
						if (pbmReqAppDtlsBean == null){ // No record exists for the ID
							log.debug("No record found for request: " + pbmRequestId);
							continue;
						}

						// STEP 3 (b). Matching record exists. Update the file id in the table.
						// If record exists, update the file Id
						// file id is updated here separately, since it is not updated when approval details are updated
						boolean fileUpdate = pbmdao.updatePBMRequestFileId(pbmRequestId, fileId);
						if (!fileUpdate) {
							String errMsg = "Error while updating PBM Request File Id: "+fileId;
							flash.error(flash.get(Constants.ERROR) == null ? errMsg : (String)flash.get(Constants.ERROR)+", <br/> "+errMsg);
							log.error(errMsg);
							continue;
						} else {
							log.debug("Updated PBM request id: " + pbmRequestId + " with file id " + fileId);
						}
					}

					// Using fileId's PBMRequest get the PBM prescription details.
					Integer pbmPrescId;

					List<String> columns = new ArrayList<>();
					columns.add(PBM_PRESCRIPTION_ID);
					columns.add(PBM_REQUEST_ID);

					Map<String, Object> field = new HashMap<>();
					field.put(PBM_REQUEST_ID, pbmRequestId);
					BasicDynaBean pbmPresBean = pbmdao.findByKey(columns, field);

					pbmPrescId = pbmPresBean.get(PBM_PRESCRIPTION_ID) != null ? (Integer)pbmPresBean.get(PBM_PRESCRIPTION_ID) : null;

					BasicDynaBean pbmPriorAuthBean = pbmdao.getPBMPresc(pbmPrescId);
					if (pbmPriorAuthBean == null) {
						String errMsg = "Invalid Auth. ID (or) No Request exists with ID: "+desc.getAuthorization().getAuthorizationID();
						flash.error(flash.get(Constants.ERROR) == null ? errMsg : (String)flash.get(Constants.ERROR)+", <br/> "+errMsg);
			    		log.error(errMsg);
			    		continue;
					}

					// if Prescription status is already "C" or "D", skip processing.
					String pbmPresStatus = (String)pbmPriorAuthBean.get(PBM_PRESC_STATUS);
					if (pbmPresStatus != null && (pbmPresStatus.equals("C") || pbmPresStatus.equals("D"))){
						log.debug("PBM Prescription "+pbmPrescId+" is already marked " + pbmPresStatus);
						continue;
					}

					/*
					 * STEP 4: Validate the downloaded XML
					 */
					// Need to check if PBM Auth of Prescription is valid or not and then save details.
					errorMsg = desc == null ? "Prior Auth XML parsing failed: Incorrectly formatted values supplied"
											: priorXMLProvider.validatePriorAuthorizationXml(desc, pbmPriorAuthBean, null);

					// STEP 4 (a) If validation fails, log error and continue
					if (!errorMsg.equals("")) { // If error in xml file, log error and continue

						flash.error(flash.get(Constants.ERROR) == null ? errorMsg : (String)flash.get(Constants.ERROR)+", <br/> "+errorMsg);
			    		log.error(errorMsg);
			    		continue;

					}else { // update table // STEP 4 (b). If validation succeeds, update approval details

						log.debug("updating approval details for request id = " + pbmRequestId);
						PBMApprovalsDAO appDao = new PBMApprovalsDAO();
						boolean success = appDao.updatePBMApprovalDetails(desc);
						if (!success) {
							errorMsg = "Error while updating PBM Authorization approval details.";
							flash.error(flash.get(Constants.ERROR) == null ? errorMsg : (String)flash.get(Constants.ERROR)+", <br/> "+errorMsg);
				    		log.error(errorMsg);
				    		continue;
						}else {
							msg.append("<br/> PBM Request Id: ").append(pbmRequestId);
							log.debug("msg = " + msg.toString());
						}
					}

					// After downloading the file using file id set as downloaded.
					/*
					 * STEP 5: Set transaction as downloaded
					 */
					if (healthAuthority.equals("DHA")) {
						Holder<Integer> txnDownloadedResult = new Holder<>();
						Holder<String> errorMessage = new Holder<>();
						erxwsoap.setTransactionDownloaded(serviceUser, servicePwd,
								fileId, txnDownloadedResult, errorMessage);

						txnResult = txnDownloadedResult.value;
						txn = PriorAuthorizationHelper.TransactionResults.getTxnResultMessage(txnResult);

						if (txnResult < 0) {

							String errMsg = txn.getResultMsg();
							errMsg = "Error while setting transaction downloaded with fileId : "+fileId+"... "+errMsg+" <br/> "+errorMessage.value;
							flash.error(flash.get(Constants.ERROR) == null ? errMsg : (String)flash.get(Constants.ERROR)+", <br/> "+errMsg);
					   		log.error(errMsg);
					   		continue;
						}

					}else if (healthAuthority.equals("HAAD")) {
							pbmAuthResponse = (PBMAuthResponse)retriever.setTransactionDownloaded(fileId);

						if (pbmAuthResponse.isError()){
							String errMsg = pbmAuthResponse.getErrorMessage();
						    if (errMsg != null && !errMsg.equals("")) {
						    	flash.error(errMsg);
						       	continue;
						    }
						}
					}
					txnsDownloaded = true;
					log.debug("msg = " + msg.toString());
					continue;
				} // end of try block 2
				catch (SAXParseException se) {

					String err = "Error while reading/parsing the Prior Authorization file/data: <br/>" +
						  			" "+se.getMessage()+" "+errorMsg;
					flash.error(flash.get(Constants.ERROR) == null ? err : (String)flash.get(Constants.ERROR)+", <br/> "+err);
					log.error(err);
				} finally {
					dataOutPutStream.close();
				}
			} // end of for block 1
		} // end of try block 1
		catch (ConnectException e) {
			if (healthAuthority.equals("DHA")) {
				flash.error(PriorAuthorizationHelper.DHPOSystemDownTimeError);
				log.error("System Downtime... Cannot connect to " + ERxValidateTransaction.class.getAnnotation(WebServiceClient.class).targetNamespace());

			}else if (healthAuthority.equals("HAAD")) {
				flash.error(PriorAuthorizationHelper.SystemDownTimeError);
				log.error("System Downtime(Provider, Daman, Shafafiya)... Cannot connect to " + Webservices.class.getAnnotation(WebServiceClient.class).targetNamespace());
			}
	    	return redirect;
		}
		if (txnsDownloaded){
			msg.append("<br/> Marked file(s) as downloaded. ");
			flash.info(msg.toString());
		} else {
			flash.info("No Transactions found to download.");
		}
		return redirect;
	}

	public ActionForward setTransactionDownloaded(ActionMapping mapping,ActionForm fm,
			HttpServletRequest request, HttpServletResponse response) throws Exception {

		String pbmRequestId = request.getParameter(PBM_REQUEST_ID);

		FlashScope flash = FlashScope.getScope(request);
		ActionRedirect redirect = new ActionRedirect(mapping.findForward(LIST_REDIRECT));
		redirect.addParameter(PBM_FINALIZED, "Y");
		redirect.addParameter(PBM_PRESC_STATUS, "S");
		redirect.addParameter(PBM_PRESC_STATUS, "D");
		redirect.addParameter(PBM_PRESC_STATUS, "C");
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

		HttpSession session = request.getSession(false);
		String healthAuthority = (String)session.getAttribute("loginCenterHealthAuthority");

		String serviceUser = null;
		String servicePwd = null;

		log.debug("setTransactionDownloaded- start");
		if (healthAuthority.equals("DHA")) {
			serviceUser = (String)session.getAttribute("dhpo_facility_user");
			servicePwd = (String)session.getAttribute("dhpo_facility_password");

			String connectErr = priorAuthHelper.isDHPOConnected();

			if (connectErr != null) {
				flash.error(PriorAuthorizationHelper.DHPOSystemDownTimeError);
				log.error(connectErr);
				return redirect;

			}
		}else if (healthAuthority.equals("HAAD")) {
			serviceUser = (String)session.getAttribute("shafafiya_user");
			servicePwd = (String)session.getAttribute("shafafiya_password");
		}

		PBMApprovalsRetriever retriever = new PBMApprovalsRetriever(serviceUser, servicePwd);
		PBMAuthResponse pbmAuthResponse;

		int txnResult;
		PriorAuthorizationHelper.TransactionResults txn;
		try {

			ERxValidateTransaction erxws;
			ERxValidateTransactionSoap erxwsoap = null;

			if (healthAuthority.equals("DHA")) {
				erxws = new ERxValidateTransaction();
				erxwsoap = erxws.getERxValidateTransactionSoap();

			}else if (healthAuthority.equals("HAAD")) {

				retriever.getWebService();
			}

			BasicDynaBean pbmReqBean = pbmReqDAO.findByKey(PBM_REQUEST_ID, pbmRequestId);

			String fileId = null;

			if (pbmReqBean != null)
				fileId = pbmReqBean.get(Constants.FILE_ID) != null ? (String)pbmReqBean.get(Constants.FILE_ID) : null;

			if (fileId == null || fileId.trim().equals("")) {
				String errMsg = "No File ID to download Prior Authorization with PBM Request Id: "+pbmRequestId;
				flash.error(errMsg);
				return redirect;
			}

			// check approval status to determine whether the approval has already been downloaded
			String approvalStatus = pbmReqBean.get("approval_status") != null ? (String)pbmReqBean.get("approval_status") : null;

			if (approvalStatus != null && !approvalStatus.trim().equals("")){
				String errMsg = "Approval for PBM Request Id: " + pbmRequestId + " is already downloaded";
				flash.info(errMsg);
				return redirect;
			}

			pbmAuthResponse = (PBMAuthResponse) downloadFile(fileId, healthAuthority, serviceUser, servicePwd);
			if (pbmAuthResponse != null && pbmAuthResponse.isError()){
				String errMsg = pbmAuthResponse.getErrorMessage();
				log.debug("setTransactionDownloaded method: Error on downloading file. fileId: " + fileId +", healthAuthority: " + healthAuthority + "errMsg: " + errMsg);
			    if (errMsg != null && !errMsg.equals("")) {
			    	flash.error(errMsg);
			       	return redirect;
			    }
			}
			log.debug("setTransactionDownloaded method: successfully downloaded file");
			byte[] fileBytes = null;
			String strFileName = "";

			if(pbmAuthResponse != null) {
				fileBytes = IOUtils.toByteArray(pbmAuthResponse.getInputStream());
				strFileName = pbmAuthResponse.getResultParams()[0].toString();
				log.error("After Downloading Transaction file with PBM Request Id :"+pbmRequestId+"... with File Id: "+fileId+" <br/>" +
						"  with Error message: " + pbmAuthResponse.getErrorMessage());

			}

			// No error after call to download the file

			String errorMsg = null;
			StringBuilder msg = new StringBuilder("Downloaded and updated Prior Auth. Approval details for ");

			FileOutputStream dataOutPutStream = null;
			try {
				InputStream is = null;
				File dataFile = File.createTempFile("tempPBMXMLFile", "");
				dataOutPutStream = new FileOutputStream(dataFile);
				dataOutPutStream.write(fileBytes);

				File xmlDataFile = File.createTempFile("tempPBMXMLDataFile", "");
				FileOutputStream outputStream = new FileOutputStream(xmlDataFile);

				boolean isZipFile = false;

					// Received file has found to be either .zip file (in case of Daman) or .xml file (in case of others)
				if (strFileName.endsWith(".zip"))
					isZipFile = true;

				log.debug("value of isZipFile = " + isZipFile);

				// If it is a zip file, read the zip file and write to output file.
				if (isZipFile) {
					String err = priorAuthHelper.unzipErrorReportFile(dataFile, outputStream);

					if (err != null) {
						flash.error(flash.get(Constants.ERROR) == null ? err : (String)flash.get(Constants.ERROR)+", <br/> "+err);
						log.error(err);
						return redirect;
					}
				} else { // not zip file
					IOUtils.write(fileBytes, outputStream);
				}

				if (log.isDebugEnabled()) {
					String authXmlStr = FileUtils.readFileToString(xmlDataFile);
					log.debug("Downloaded PBM Response XML for fileID = " + fileId + ", fileName = " + strFileName + " is ... : " +authXmlStr);
				}

				is = new FileInputStream(xmlDataFile);

				// Digest the Prior Auth XML content.
				PriorAuthorization desc = priorXMLProvider.getPriorAuthorizationMetaDataDescription(is);

				// Using fileId's PBMRequest get the PBM prescription details.
				Integer pbmPrescId;

				List<String> columns = new ArrayList<>();
				columns.add(PBM_PRESCRIPTION_ID);
				columns.add(PBM_REQUEST_ID);

				Map<String, Object> field = new HashMap<>();
				field.put(PBM_REQUEST_ID, pbmRequestId);
				BasicDynaBean pbmPresBean = pbmdao.findByKey(columns, field);

				pbmPrescId = pbmPresBean.get(PBM_PRESCRIPTION_ID) != null ? (Integer)pbmPresBean.get(PBM_PRESCRIPTION_ID) : null;

				BasicDynaBean pbmPriorAuthBean = pbmdao.getPBMPresc(pbmPrescId);
				if (pbmPriorAuthBean == null) {
					String errMsg = "Invalid Auth. ID (or) No Request exists with PBM Presc ID: "+pbmPrescId;
					flash.error(errMsg);
			    	log.error(errMsg);
					return redirect;
				}

				// Need to check if PBM Auth of Prescription is valid or not and then save details.
				errorMsg = desc == null ? "Prior Auth XML parsing failed: Incorrectly formatted values supplied"
											: priorXMLProvider.validatePriorAuthorizationXml(desc, pbmPriorAuthBean, null);

				if (!errorMsg.equals("")) {
					flash.error(errorMsg);
			    	log.error(errorMsg);
					return redirect;

				}else {
					PBMApprovalsDAO appDao = new PBMApprovalsDAO();
					boolean success = appDao.updatePBMApprovalDetails(desc);
					if (!success) {
						errorMsg = "Error while updating PBM Authorization approval details.";
						flash.error(errorMsg);
				   		log.error(errorMsg);
						return redirect;
					}else {
						log.debug("successfully updated record for request Id = " + pbmRequestId);
						msg.append("<br/> PBM Request Id: ").append(pbmRequestId);
					}
				}

				// After downloading the file using file id (errors) set as downloaded.

				if (healthAuthority.equals("DHA")) {
					Holder<Integer> txnDownloadedResult = new Holder<>();
					Holder<String>errorMessage = new Holder<>();

					erxwsoap.setTransactionDownloaded(serviceUser, servicePwd,
							fileId, txnDownloadedResult, errorMessage);

					txnResult = txnDownloadedResult.value;
					txn = PriorAuthorizationHelper.TransactionResults.getTxnResultMessage(txnResult);
					log.error("After Marking as downloaded PBM Request Id :"+pbmRequestId+"... with File Id: "+fileId+" <br/>" +
							"  with Error message: "+errorMessage.value);

					if (txnResult < 0) {
						String errMsg = txn.getResultMsg();
						errMsg = new StringBuilder("Error while setting transaction downloaded with fileId : ")
								.append(fileId).append("... ").append(errMsg).append(" <br/> ")
								.append(errorMessage.value).toString();
						flash.error(errMsg);
			    		log.error(errMsg);
						return redirect;

					}else if (txnResult == 2) {
						msg.append(txn.getResultMsg());
						log.debug(">>>>>>>>>>>>>>>> setting txn as downloaded error: " + msg.toString());
						// No new transactions available.
						flash.info(msg.toString());
						return redirect;
					}
				} else if (healthAuthority.equals("HAAD")) {
					pbmAuthResponse = (PBMAuthResponse)retriever.setTransactionDownloaded(fileId);
					log.error("After Marking as downloaded PBM Request Id :"+pbmRequestId+"... with File Id: "+fileId+" <br/>" +
							"  with Error message: "+pbmAuthResponse.getErrorMessage());

					if (pbmAuthResponse.isError()){
						String errMsg = pbmAuthResponse.getErrorMessage();
					    if (errMsg != null && !errMsg.equals("")) {
							log.debug(">>>>>>>>>>>>>>>> setting txn as downloaded error: " + msg.toString());
					    	flash.error(errMsg);
					       	return redirect;
					    }
					}
				}

				msg.append("<br/> Marked file as downloaded. ");
				flash.info(msg.toString());
			}catch (SAXParseException se) {
				  	StringBuilder err = new StringBuilder("Error while reading/parsing the Prior Authorization file/data: <br/> ")
				  			.append(se.getMessage()).append(" ").append(errorMsg);

					flash.error(err.toString());
					return redirect;
			} finally {
				dataOutPutStream.close();
			}
		}catch (ConnectException e) {
			if (healthAuthority.equals("DHA")) {
				flash.error(PriorAuthorizationHelper.DHPOSystemDownTimeError);
				log.error("System Downtime... Cannot connect to " + ERxValidateTransaction.class.getAnnotation(WebServiceClient.class).targetNamespace());

			}else if (healthAuthority.equals("HAAD")) {
				flash.error(PriorAuthorizationHelper.SystemDownTimeError);
				log.error("System Downtime(Provider, Daman, Shafafiya)... Cannot connect to " + Webservices.class.getAnnotation(WebServiceClient.class).targetNamespace());
			}
		}
		return redirect;
	}

	private EResponse downloadFile(String fileId, String healthAuthority, String serviceUser, String servicePwd) {

		PBMAuthResponse pbmAuthResponse = null;
		if (healthAuthority.equals("DHA")) {
			// TODO: Use ERxApprovalRetriever method instead
			ERxValidateTransaction erxws = new ERxValidateTransaction();
			ERxValidateTransactionSoap erxwsoap = erxws.getERxValidateTransactionSoap();

			Holder<Integer> downloadTxnResult = new Holder<>();
			Holder<String> fileName = new Holder<>();
			Holder<byte[]> file = new Holder<>();
			Holder<String> errorMessage = new Holder<>();

			// Call PBM API to download transaction.
			erxwsoap.downloadTransactionFile(serviceUser, servicePwd,
					fileId, downloadTxnResult, fileName, file, errorMessage);

			log.error("After Downloading Transaction file with File Id: "+fileId+" <br/>" +
					"  with Error message: "+errorMessage.value);

			pbmAuthResponse = new PBMAuthResponse(downloadTxnResult.value, errorMessage.value, file.value, new Object[]{fileName.value});
		} else if (healthAuthority.equals("HAAD")) {
			PBMApprovalsRetriever retriever = new PBMApprovalsRetriever(serviceUser, servicePwd);

			// Call PBM API to download transaction.
			pbmAuthResponse = (PBMAuthResponse)retriever.getApprovalFile(fileId);

		}
		return pbmAuthResponse;
	}

	@IgnoreConfidentialFilters
	public ActionForward uploadTestPriorAuth(ActionMapping mapping,ActionForm fm,
			HttpServletRequest request,HttpServletResponse response) throws Exception {

		String pbmPrescIdStr = request.getParameter(PBM_PRESCRIPTION_ID);
		String pbmRequestId = request.getParameter(PBM_REQUEST_ID);

		FlashScope flash = FlashScope.getScope(request);
		ActionRedirect redirect = new ActionRedirect(mapping.findForward(LIST_REDIRECT));
		redirect.addParameter(PBM_FINALIZED, "Y");
		redirect.addParameter(PBM_PRESC_STATUS, "S");
		redirect.addParameter(PBM_PRESC_STATUS, "D");
		redirect.addParameter(PBM_PRESC_STATUS, "C");
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

		HttpSession session = request.getSession(false);

		String fileName = null;
		String fileContentType = null;
		InputStream is = null;

		DiskFileItemFactory factory = new DiskFileItemFactory();
        // Create a new file upload handler
        ServletFileUpload upload = new ServletFileUpload(factory);
        // Parse the request
        try {
            List<FileItem> items = upload.parseRequest(request);
            for (FileItem item : items) {
                if (!item.isFormField()) {
                	fileName = item.getName();
                	is = item.getInputStream();
                	MimeUtil2 mimeUtil = MimeTypeDetector.getMimeUtil();
            		Collection cln = mimeUtil.getMimeTypes(is);
            		Object[] arr = cln.toArray();
            		fileContentType = arr[0].toString();
                }
            }
        } catch (FileUploadException e) {
        	throw new Exception("File processing failed", e);
        }

		if (StringUtils.isBlank(fileContentType) ||
				!fileContentType.equalsIgnoreCase("application/octet-stream")) {
			flash.error("Upload file : " + fileName + " is an invalid file format. Please upload " +
					"valid XML file.");
			return redirect;
		}

		String errorMsg = null;

		try {

			// Digest the Prior Auth XML content.
			PriorAuthorization desc = priorXMLProvider.getPriorAuthorizationMetaDataDescription(is);

			// Using fileId's PBMRequest get the PBM prescription details.
			Integer pbmPrescId = new Integer(pbmPrescIdStr);

			BasicDynaBean pbmPriorAuthBean = pbmdao.getPBMPresc(pbmPrescId);
			if (pbmPriorAuthBean == null) {
				String errMsg = "Invalid Auth. ID (or) No Request exists for PBM Presc ID: "+pbmPrescId;
				flash.error(errMsg);
	    		log.error(errMsg);
				return redirect;
			}

			// Need to check if PBM Auth of Prescription is valid or not and then save details.
			errorMsg = desc == null ? "Prior Auth XML parsing failed: Incorrectly formatted values supplied"
									: priorXMLProvider.validatePriorAuthorizationXml(desc, pbmPriorAuthBean, pbmRequestId);

			if (!errorMsg.equals("")) {

				flash.error(errorMsg);
	    		log.error(errorMsg);
				return redirect;

			}else {

				PBMApprovalsDAO appDao = new PBMApprovalsDAO();
				boolean success = appDao.updatePBMApprovalDetails(desc);
				if (!success) {
					errorMsg = "Error while updating PBM Authorization approval details.";
					flash.error(errorMsg);
		    		log.error(errorMsg);
					return redirect;
				}else {
					flash.info("Prior-Auth details saved for PBM Request Id: "+pbmRequestId);
					redirect.addParameter(PBM_REQUEST_ID, pbmRequestId);
					return redirect;
				}
			}
		} catch (SAXParseException se) {

		  	StringBuilder err = new StringBuilder("Error while reading/parsing the Prior ")
		  			.append("Authorization file/data: <br/> ").append(se.getMessage()).append(" ")
					.append(errorMsg);

			flash.error(err.toString());
			return redirect;
		}
	}

	public ActionForward getSalesScreen(ActionMapping mapping,ActionForm fm,
			HttpServletRequest request,HttpServletResponse response) {

		String visitId = request.getParameter("visit_id");
		String patstatus = request.getParameter("patstatus");
		String pbmPrescId = request.getParameter(PBM_PRESCRIPTION_ID);
		ActionRedirect redirect = new ActionRedirect(mapping.findForwardConfig("showSalesScreen"));
		redirect.addParameter("phStore", "0");
		redirect.addParameter("ps_status", "active");
		redirect.addParameter("patstatus",patstatus);
		redirect.addParameter("visit_id", visitId);
		redirect.addParameter(PBM_PRESCRIPTION_ID, pbmPrescId);
		return redirect;
	}

}
