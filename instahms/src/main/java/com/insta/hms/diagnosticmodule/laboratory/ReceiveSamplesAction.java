package com.insta.hms.diagnosticmodule.laboratory;

import com.bob.hms.common.CenterHelper;
import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.diagnosticmodule.common.SampleCollectionDAO;
import com.insta.hms.diagnosticmodule.prescribetest.DiagnoDAOImpl;
import com.insta.hms.master.DiagnosticDepartmentMaster.DiagnosticDepartmentMasterDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import flexjson.JSONSerializer;

public class ReceiveSamplesAction extends BaseAction {

	static Logger logger = LoggerFactory.getLogger(ReceiveSamplesAction.class);
	JSONSerializer js = new JSONSerializer().exclude("class");

	public ReceiveSamplesDAO dao = new ReceiveSamplesDAO();
	private static final GenericDAO sampleCollectionDAO = new GenericDAO("sample_collection");

	@IgnoreConfidentialFilters
	public ActionForward searchScreen(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws IOException, ServletException, Exception {
		List collectionCenters = ReceiveSamplesDAO.getAllCollectionCenter();
		String date_range = request.getParameter("date_range");
		String week_start_date = null;
		if (date_range != null && date_range.equals("week")) {
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
	        Calendar cal = Calendar.getInstance();
	        cal.add(Calendar.DATE, -7);
	        Date openDt = cal.getTime();
	        week_start_date = dateFormat.format(openDt);
		}
		HttpSession session = request.getSession(false);
		String userId =(String)session.getAttribute("userid");
		String userDept = new DiagnosticDepartmentMasterDAO().getUserDepartment(userId);
  	    String department = request.getParameter("ddept_id");

		if (department == null ) {
			if (userDept != null && !userDept.equals(""))
				department = userDept;
		}

		request.setAttribute("userDept", department);
		request.setAttribute("collectionCenters", js.serialize(collectionCenters));
		request.setAttribute("module", mapping.getProperty("category"));
		request.setAttribute("test_timestamp", new DiagnoDAOImpl().getCountFromDiagTimeStamp());

		ActionForward forward = new ActionForward(mapping.findForward("searchlist").getPath());
		// when ever user uses a pagination sample_date should not append again.
		if (date_range != null && date_range.equals("week") && request.getParameter("sample_date") == null) {
			addParameter("sample_date", week_start_date, forward);
	    }
		Integer userCenterId = RequestContext.getCenterId();
		String errorMsg = CenterHelper.centerUserApplicability(userCenterId);
		if (errorMsg != null) {
			request.setAttribute("error", errorMsg);
		}
		return forward;
	}

	@IgnoreConfidentialFilters
	public ActionForward searchlist(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException, Exception,
			ParseException {
		Map map= getParameterMap(request);
		HttpSession session = request.getSession(false);
		String userId =(String)session.getAttribute("userid");
		String[] receiptTimePart = (String[])map.get("_receipt_time");
		String[] transferTimePart = (String[])map.get("_transfer_time");
		int centerId = (Integer) request.getSession(false).getAttribute("centerId");
		if (centerId != 0) {
			map.put("center_id", new String[]{centerId+""});
			map.put("center_id@type", new String[]{"integer"});
		}

		String date_range = request.getParameter("date_range");
		String week_start_date = null;
		if (date_range != null && date_range.equals("week")) {
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
	        Calendar cal = Calendar.getInstance();
	        cal.add(Calendar.DATE, -7);
	        Date openDt = cal.getTime();
	        week_start_date = dateFormat.format(openDt);

			map.put("sample_date", new String[]{week_start_date, ""});
			map.put("sample_date@op", new String[]{"ge,le"});
			map.put("sample_date@cast", new String[]{"y"});
			map.remove("date_range");
		}

		String department = request.getParameter("ddept_id");
		String userDept = new DiagnosticDepartmentMasterDAO().getUserDepartment(userId);
		if (department == null ) {
			if (userDept != null && !userDept.equals(""))
				department = userDept;
		}
		List collectionCenters = ReceiveSamplesDAO.getAllCollectionCenter();

		request.setAttribute("userDept", department);
		request.setAttribute("collectionCenters", js.serialize(collectionCenters));
		request.setAttribute("actionId", mapping.getProperty("action_id"));
		request.setAttribute("module", mapping.getProperty("category"));
		request.setAttribute("test_timestamp", new DiagnoDAOImpl().getCountFromDiagTimeStamp());

		ActionForward forward = new ActionForward(mapping.findForward("searchlist").getPath());
		// when ever user uses a pagination sample_date should not append again.
		if (date_range != null && date_range.equals("week") && request.getParameter("sample_date") == null) {
			addParameter("sample_date", week_start_date, forward);
	    }

		String errorMsg = CenterHelper.centerUserApplicability(centerId);
		if (errorMsg != null) {
			request.setAttribute("error", errorMsg);
			return mapping.findForward("searchlist");
		}

		String[] receiptDateTime = (String[])map.get("_receipt_date");
		String[] receipt_time = new String[2];
		if(receiptDateTime != null) {
			receipt_time[0] = receiptDateTime[0]+" "+receiptTimePart[0];
			receipt_time[1] = receiptDateTime[1]+" "+receiptTimePart[1];
			map.put("receipt_time", receipt_time);
		}
		String[] transferDateTime = (String[])map.get("_transfer_date");
		String[] transfer_time = new String[2];
		if(receiptDateTime != null) {
			transfer_time[0] = transferDateTime[0]+" "+transferTimePart[0];
			transfer_time[1] = transferDateTime[1]+" "+transferTimePart[1];
			map.put("transfer_time", transfer_time);
		}
		map.put("ddept_id", new String[]{department});
		Object pageSize = request.getParameter("pageSize");
		if (pageSize != null) {
			request.setAttribute(LISTING.PAGESIZE.toString(), request.getParameter("pageSize"));
		}
		PagedList  pagedList = dao.getReceiveSamplesList(map,ConversionUtils.getListingParameter(request.getParameterMap()));
		request.setAttribute("pagedList", pagedList);
		
		ArrayList<String> testIDList = new ArrayList<String>();
		ArrayList<String> currentSampleNoList = new ArrayList<String>();
		List<BasicDynaBean> pList = pagedList.getDtoList();
		for (BasicDynaBean item : pList) {
			String testIDs = item.get("test_id").toString();
			String[] testIDsList = testIDs.split(",");
			for (String id : testIDsList) {
				id = id.trim();
				if (!testIDList.contains(id)) {
					testIDList.add(id);
				}
			}
			currentSampleNoList.add(item.get("current_sample_no").toString());
		}
		
		// send an array of current_sample_no retrieved and fetch child sample numbers.
		List childSampleDetails = ReceiveSamplesDAO.getChildSamples(currentSampleNoList);
		List testCenterAssociation = null;
		if (childSampleDetails != null) {
			for (Object samples : childSampleDetails) {
				String testIDs = ((BasicDynaBean) samples).get("test_id").toString();
				String[] testIDsList = testIDs.split(",");
				for (String id : testIDsList) {
					id = id.trim();
					if (!testIDList.contains(id)) {
						testIDList.add(id);
					}
				}
			}
			request.setAttribute("child_samples", childSampleDetails);
		}
		testCenterAssociation = ReceiveSamplesDAO.getTestCenterAssociation(centerId, testIDList);
		request.setAttribute("test_center_association", testCenterAssociation);
		request.setAttribute("genPrefs", GenericPreferencesDAO.getGenericPreferences());
		return forward;
	}
	
	private Boolean isSampleTransferredFromCollectionCenter(String sampleCollectionNumber) throws SQLException {
		BasicDynaBean sampleCollectionBean = sampleCollectionDAO.findByKey("sample_sno", sampleCollectionNumber);
		if (sampleCollectionBean != null) {
			return ((String) sampleCollectionBean.get("sample_transfer_status")).equals("P");
		}
		
		return false;
	}

	@IgnoreConfidentialFilters
	public ActionForward splitSample(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException,
			ParseException, Exception {
		int centerID = (Integer) request.getSession(false).getAttribute("centerId");
		String[] splitTestPrescribedIDs = request.getParameterValues("splitSamplePrescID");
		String parentSampleNo = request.getParameter("parent_sample_no");
		int sampleCollectionID = Integer.parseInt(SampleCollectionDAO.getSampleAtCenter(parentSampleNo, centerID).get("sample_collection_id").toString());
		Boolean isAllTestSelected = Boolean.parseBoolean(request.getParameter("deduct_total_destinations"));
		int totalDestinations = Integer.parseInt(request.getParameter("total_destinations"));
		Map<String, Integer> splitSampleParameters = new HashMap<String, Integer>();
		Boolean isSplitDone = false;
		if (isAllTestSelected) {
			totalDestinations = totalDestinations - 1;
			if (totalDestinations == 1) {
				String parentOutsourceDestinationIDObject = request.getParameter("parent_outsource_destination_id");
				if (parentOutsourceDestinationIDObject != null) {
					Integer parentOutsourceDestinationID = Integer.parseInt(parentOutsourceDestinationIDObject);
					splitSampleParameters.put("parent_outsource_dest_id", parentOutsourceDestinationID);
				}
				isSplitDone = true;
			}
		}
		int outsourceDestinationID = Integer.parseInt(request.getParameter("outsource_dest_id")); // equals -1 if Inhouse test, -2 for more than one destination
		splitSampleParameters.put("sample_collection_id", sampleCollectionID);
		splitSampleParameters.put("center_id", centerID);
		splitSampleParameters.put("child_outsource_dest_id", outsourceDestinationID);
		
		if (splitTestPrescribedIDs != null && splitTestPrescribedIDs.length > 0) {
			SplitSampleDAO splitSampleDAO = new SplitSampleDAO();
			splitSampleDAO.performSplitSample(splitSampleParameters, splitTestPrescribedIDs, isSplitDone);
		}
		ActionRedirect redirect = new ActionRedirect(mapping.findForwardConfig("saveRedirect"));
		redirect.addParameter("_sample_no", parentSampleNo);
		return redirect;
	}
	
	public ActionForward saveMarkedReceivedSamples(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException,
			ParseException, Exception {
		Date parsedDate = new Date();
		Timestamp receivingTime = new Timestamp(parsedDate.getTime());
		HttpSession session = request.getSession();
		FlashScope flash = FlashScope.getScope(request);
		String flashMessage = "";
		String barcodeReceiveStatusMessage = "";
		Boolean isRequestFromCheckBox = Boolean.parseBoolean(request.getParameter("isRequestFromCheckBox"));
		String[] receivedchecks = null;
		String sampleNos = null;
		if(isRequestFromCheckBox) {
			receivedchecks = request.getParameterValues("receiveCheck");
			sampleNos = "";
			for (int i = 0; i < receivedchecks.length; i++) {
				String[] sampleDetail = receivedchecks[i].split("-");
				sampleNos += sampleDetail[1];
				if (i + 1 != receivedchecks.length) {
					sampleNos += ",";
				}
				receivedchecks[i] = sampleDetail[0];
			}
		} else {
			sampleNos = request.getParameter("_receive_sample_no");
			receivedchecks = sampleNos.split(",");
			for (int i = 0; i < receivedchecks.length; i++) {
				receivedchecks[i] = receivedchecks[i].trim();
			}
		}
		String receivingUser = (String)request.getSession(false).getAttribute("userid");		
		Connection con = null;
		Boolean isSplitNeeded = false;
		Boolean success = true;
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			for(int i= 0;i < receivedchecks.length; i++) {
				BasicDynaBean sampleCollectionBean = null;
				int sampleCollectionId = -1;
				Boolean isSampleAlreadyReceived = false;
				Boolean isPendingToBeTransferred = false;
				if(isRequestFromCheckBox) {
					sampleCollectionId = Integer.parseInt(receivedchecks[i]);
					sampleCollectionBean = sampleCollectionDAO.findByKey("sample_collection_id",sampleCollectionId);
				} else {
					BasicDynaBean sampleDetailAtCenterBean = SampleCollectionDAO.getSampleAtCenter(receivedchecks[i], RequestContext.getCenterId());
					if (sampleDetailAtCenterBean != null) {
						Object collSampleNo = sampleDetailAtCenterBean.get("coll_sample_no");
						if (collSampleNo != null) {
							isPendingToBeTransferred = isSampleTransferredFromCollectionCenter(collSampleNo.toString());
							if (isPendingToBeTransferred) {
								if (flashMessage.length() > 0) {
									flashMessage += " , ";
								} else {
									flashMessage = "Samples below are yet to be transferred from the collection center:<br>";
								}
								flashMessage += receivedchecks[i];
							} else {
								sampleCollectionId = (Integer) sampleDetailAtCenterBean.get("sample_collection_id");
								isSampleAlreadyReceived = ((String)sampleDetailAtCenterBean.get("sample_receive_status")).equals("R");
								sampleCollectionBean = sampleCollectionDAO.getBean();
								if (isSampleAlreadyReceived) {
									barcodeReceiveStatusMessage = "Sample No: " + receivedchecks[0] + " is already received";
								} else {
									isSplitNeeded = sampleDetailAtCenterBean.get("sample_split_status").toString().equals("P");
								}
							}
						} else {
							barcodeReceiveStatusMessage = "Sample No: " + receivedchecks[0] + " can't be received";
						}
					} else {
						barcodeReceiveStatusMessage = "Sample No: " + receivedchecks[0] + " does not exist";
					}
				}
				if (sampleCollectionBean != null && !isSampleAlreadyReceived && !isPendingToBeTransferred) {
					sampleCollectionBean.set("sample_receive_status", "R");
					sampleCollectionBean.set("receipt_user", receivingUser);
					sampleCollectionBean.set("receipt_time", receivingTime);
					Map<String,Object> keys = new HashMap<String, Object>();
					keys.put("sample_collection_id", sampleCollectionId);
					int j = sampleCollectionDAO.update(con, sampleCollectionBean.getMap(), keys);
					if(j == 0) {
						success = false;
						barcodeReceiveStatusMessage = "Sample No: " + receivedchecks[0] + " Receive Failed";
						break;
					}
				}
			}
		} finally{
			DataBaseUtil.commitClose(con, success);
		}
		
		ActionRedirect redirect = new ActionRedirect(mapping.findForwardConfig("saveRedirect"));
		if (flashMessage.length() > 0) {
			flashMessage += "<br>Please contact the collection center.<br>";
			flash.info(flashMessage);
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		} else {
			if (!isRequestFromCheckBox) {
				if (success && (barcodeReceiveStatusMessage.length() == 0)) {
					barcodeReceiveStatusMessage = "Sample No: " + receivedchecks[0] + " Received";
				}
				redirect.addParameter("_barcode_action_response", barcodeReceiveStatusMessage);
				redirect.addParameter("_split_needed", isSplitNeeded);
			}
			redirect.addParameter("_sample_no", sampleNos);
		}
		return redirect;
	}
}
