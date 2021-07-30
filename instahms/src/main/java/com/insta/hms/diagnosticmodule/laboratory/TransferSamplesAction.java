package com.insta.hms.diagnosticmodule.laboratory;

import com.bob.hms.common.CenterHelper;
import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.billing.ChargeBO;
import com.insta.hms.billing.ChargeDTO;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.diagnosticmodule.common.OutHouseSampleDetails;
import com.insta.hms.diagnosticmodule.internallab.AutomaticSampleRegistration;
import com.insta.hms.diagnosticmodule.internallab.InternalLab;
import com.insta.hms.diagnosticmodule.prescribetest.DiagnoDAOImpl;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.DiagnosticDepartmentMaster.DiagnosticDepartmentMasterDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;

import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class TransferSamplesAction extends BaseAction {

	static Logger logger = LoggerFactory.getLogger(TransferSamplesAction.class);
	JSONSerializer js = new JSONSerializer().exclude("class");

	public TransferSamplesDAO dao = new TransferSamplesDAO();
	private static final GenericDAO sampleCollectionDAO = new GenericDAO("sample_collection");
	private static final GenericDAO testsPrescribedDAO = new GenericDAO("tests_prescribed");

	@IgnoreConfidentialFilters
	public ActionForward searchScreen(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws IOException, ServletException, Exception {

		List outSources = dao.getAllOutSource();
		List collectionCenters = dao.getAllCollectionCenter();
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
		request.setAttribute("outSources", js.serialize(outSources));
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
	public ActionForward searchBySample(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException, Exception {

		Integer userCenterId = RequestContext.getCenterId();
		String errorMsg = CenterHelper.centerUserApplicability(userCenterId);
		if (errorMsg != null) {
			request.setAttribute("error", errorMsg);
			return mapping.findForward("list");
		}

		Map map= getParameterMap(request);
		List outSources = dao.getAllOutSource();
		List collectionCenters = dao.getAllCollectionCenter();
		List  list = dao.getTransferSamplesBySampleNo(map,ConversionUtils.getListingParameter(request.getParameterMap()));
		request.setAttribute("samplesList", list);
		request.setAttribute("outSourceList", outSources);	
		request.setAttribute("collectionCenters", js.serialize(collectionCenters));
		request.setAttribute("module", mapping.getProperty("category"));
		request.setAttribute("test_timestamp", new DiagnoDAOImpl().getCountFromDiagTimeStamp());
		BasicDynaBean diagGenericPref = GenericPreferencesDAO.getdiagGenericPref();
		request.setAttribute("diagGenericPref",diagGenericPref);

		return mapping.findForward("list");
	}

	@IgnoreConfidentialFilters
	public ActionForward list(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException, Exception,
			ParseException {

		Map map= getParameterMap(request);
		HttpSession session = request.getSession(false);
		String userId =(String)session.getAttribute("userid");
		String[] transferTimePart = (String[])map.get("_transfer_time");
		int centerId = (Integer) request.getSession(false).getAttribute("centerId");

		String department = request.getParameter("ddept_id");
		String userDept = new DiagnosticDepartmentMasterDAO().getUserDepartment(userId);
		if (department == null ) {
			if (userDept != null && !userDept.equals(""))
				department = userDept;
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

		List outSources = dao.getAllOutSource();
		List collectionCenters = dao.getAllCollectionCenter();
		
		request.setAttribute("userDept", department);
		request.setAttribute("outSources", js.serialize(outSources));
		request.setAttribute("outSourceList", outSources);	
		request.setAttribute("collectionCenters", js.serialize(collectionCenters));
		request.setAttribute("module", mapping.getProperty("category"));
		request.setAttribute("test_timestamp", new DiagnoDAOImpl().getCountFromDiagTimeStamp());
		request.setAttribute("actionId", mapping.getProperty("action_id"));

		ActionForward forward = new ActionForward(mapping.findForward("searchlist").getPath());
		// when ever user uses a pagination sample_date should not append again.
		if (date_range != null && date_range.equals("week") && request.getParameter("sample_date") == null) {
			addParameter("sample_date", week_start_date, forward);
	    }

		String errorMsg = CenterHelper.centerUserApplicability(centerId);
		// in center schema only for the center users allow the user to see the results.
		if (errorMsg != null) {
			request.setAttribute("error", errorMsg);
			return forward;
		}

		if (centerId != 0) {
			map.put("center_id", new String[]{centerId+""});
			map.put("center_id@type", new String[]{"integer"});
		}

		String[] transferDateTime = (String[])map.get("_transfer_date");
		String[] transfer_time = new String[2];
		if(transferDateTime != null) {
			transfer_time[0] = transferDateTime[0]+" "+transferTimePart[0];
			transfer_time[1] = transferDateTime[1]+" "+transferTimePart[1];
			map.put("transfer_time", transfer_time);
		}

		map.put("ddept_id", new String[]{department});
		PagedList  pagedList = dao.getTransferSamplesList(map,ConversionUtils.getListingParameter(request.getParameterMap()));
		BasicDynaBean diagGenericPref = GenericPreferencesDAO.getdiagGenericPref();
		request.setAttribute("diagGenericPref",diagGenericPref);
		request.setAttribute("pagedList", pagedList);

		return forward;
	}

	public ActionForward saveMarkedTransferredSamples(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException,
			ParseException, Exception {

		java.util.Date parsedDate = new java.util.Date();
		Timestamp transferredTime = new Timestamp(parsedDate.getTime());
		HttpSession session = request.getSession();
		String[] transferredchecks = request.getParameterValues("transferCheck");
		String transferUser = (String)request.getSession(false).getAttribute("userid");
		String transferDate = request.getParameter("transferDate");
		String transferTime = request.getParameter("transferTime");
		String transferOtherDetails = request.getParameter("transferOtherDetails");
		String isPrint = request.getParameter("isPrint");
		String[] sampleCollIds = request.getParameterValues("sampleCollectionId");
		String[] outsourceDestIds = request.getParameterValues("outsource_dest_id");
		String[] patientIds = request.getParameterValues("patient_id");		
		String[] sgTestIds = request.getParameterValues("sg_test_id");		
		String[] sgPrescIds = request.getParameterValues("sg_prescribed_id");		
		String[] sgSampleNos = request.getParameterValues("sg_sample_no");		
		String[] sgSampleTypeIds = request.getParameterValues("sg_sample_type_id");
		BasicDynaBean diagGenericPref = GenericPreferencesDAO.getdiagGenericPref();
		
		Connection con = null;
		Boolean success = false;
		Boolean allSuccess = false;

		int centerId = RequestContext.getCenterId();
		String centerCode = (String) new CenterMasterDAO().findByKey("center_id", centerId).get("center_code");
		String batchId = centerCode + "-" + DateUtil.currentDate("yyMMddHHmmss");
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			Map<String,InternalLab> incomingSamplesRegMap = new HashMap<String, InternalLab>();
			ChargeBO chargeBo = new ChargeBO();
			OutHouseSampleDetails ohDetails = new OutHouseSampleDetails();
			for (int i=0; i<transferredchecks.length; i++) {
				int sampleCollectionId = Integer.parseInt(transferredchecks[i]);
				Integer outSourceDestId = -1;
				String[] testIdArray = null;
				String[] prescIdArray = null;
				String[] sampleNoArray = null;
				String[] sampleTypeIdArray = null;
				String patientId = null;
				for (int k=0; k<sampleCollIds.length; k++) {
					if (sampleCollectionId == Integer.parseInt(sampleCollIds[k])) {
						outSourceDestId = Integer.parseInt(outsourceDestIds[k]);
						patientId = patientIds[k];
						testIdArray = sgTestIds[k].split(",");
						dao.removeSpaceFromArray(testIdArray);
						prescIdArray = sgPrescIds[k].split(",");
						dao.removeSpaceFromArray(prescIdArray);
						sampleNoArray = sgSampleNos[k].split(",");
						dao.removeSpaceFromArray(sampleNoArray);
						sampleTypeIdArray = sgSampleTypeIds[k].split(",");	
						dao.removeSpaceFromArray(sampleTypeIdArray);
						break;
					}	
				}
				BasicDynaBean sampleCollectionBean = sampleCollectionDAO.findByKey("sample_collection_id",sampleCollectionId);
				Integer isOutSourceDestIdExist = (Integer) sampleCollectionBean.get("outsource_dest_id");
				BasicDynaBean visitCenterBean = VisitDetailsDAO.gettVisitCenterRelatedFields(con, patientId);
				int visitCenterId = (Integer)visitCenterBean.get("center_id");
				
				sampleCollectionBean.set("sample_transfer_status", "T");
				sampleCollectionBean.set("transfer_user", transferUser);
				if (transferDate != null && !transferDate.equals("") && transferTime != null && !transferTime.equals(""))
					transferredTime = DateUtil.parseTimestamp(transferDate+" "+transferTime);
				sampleCollectionBean.set("transfer_time", transferredTime);
				sampleCollectionBean.set("transfer_other_details", transferOtherDetails);
				sampleCollectionBean.set("transfer_batch_id", batchId);
				sampleCollectionBean.set("outsource_dest_id", outSourceDestId);

				BasicDynaBean outSourceBean = dao.getDiagOutSourceDetails(outSourceDestId);
				String outSourceDestType = (String) outSourceBean.get("outsource_dest_type");
				Map<String,Object> keys = new HashMap<String, Object>();
				keys.put("sample_collection_id", sampleCollectionId);
				int j= sampleCollectionDAO.update(con, sampleCollectionBean.getMap(), keys);
				if(j>0) success = true;
				if (success) {
					BasicDynaBean testPrescBean = testsPrescribedDAO.getBean();
					Map<String,Object> keys1 = new HashMap<String, Object>();
					keys1.put("sample_collection_id", sampleCollectionId);
					testPrescBean.set("outsource_dest_id", outSourceDestId);
					testPrescBean.set("is_outhouse_selected", "Y");
					testPrescBean.set("prescription_type", "o");
					if (null != outSourceBean.get("protocol") && outSourceBean.get("protocol").equals("hl7"))
						testPrescBean.set("conduction_type", "i");
					success = testsPrescribedDAO.update(con, testPrescBean.getMap(), keys1) > 0;
				}

				if (success) {
					//Automatic sample registration if outsource is internal lab
					if (outSourceDestType.equals("C") && isOutSourceDestIdExist == null) {
   						 Integer conductionCenterId = (Integer) outSourceBean.get("center_id");						 						 
						 if (incomingSamplesRegMap.containsKey(patientId+","+outSourceDestId)) {
							 InternalLab labDto = incomingSamplesRegMap.get(patientId+","+outSourceDestId);
							 
							 String[] testIDs = labDto.getInternalLabTestIds();
							 String[] prescIds = labDto.getInternalLabPrescIds();
							 String[] sampleNos = labDto.getInternalLabSampleNos();
							 String[] sampleTypeIds = labDto.getInternalLabSampleTypeIds();
							 //String internalLabCenterId = labDto.getInternalLabCenterid();
							 
							 labDto.setInternalLabTestIds((String[])addToArray(testIDs, testIdArray));
							 labDto.setInternalLabPrescIds((String[])addToArray(prescIds, prescIdArray));
							 labDto.setInternalLabSampleNos((String[])addToArray(sampleNos,sampleNoArray));
							 labDto.setInternalLabSampleTypeIds((String[])addToArray(sampleTypeIds, sampleTypeIdArray));
						 } else {
							 InternalLab labDto = new InternalLab();
							 labDto.setInternalLabTestIds(testIdArray);
							 labDto.setInternalLabPrescIds(prescIdArray);
							 labDto.setInternalLabSampleNos(sampleNoArray);
							 labDto.setInternalLabSampleTypeIds(sampleTypeIdArray);
							 labDto.setInternalLabCenterId(conductionCenterId.toString());
							 incomingSamplesRegMap.put(patientId+","+outSourceDestId, labDto);
						 }
											
					} else if ((outSourceDestType.equals("O") || outSourceDestType.equals("IO")) && isOutSourceDestIdExist == null) {						
						//insert out house details and update payment	
						 for(int m=0;m<prescIdArray.length;m++) {							 
							 ohDetails.setVisitId(patientId);
							 ohDetails.setPrescribedId(Integer.parseInt(prescIdArray[m]));
							 ohDetails.setSampleNo(sampleNoArray[m]);
							 ohDetails.setTestId(testIdArray[m]);
							 ohDetails.setoutSourceId(outSourceDestId.toString());
							 success &= LaboratoryDAO.setSamplesToOuthouse(ohDetails,con);
							 if (success) {								
								 String chargeId = LaboratoryDAO.getOhTestChargeId(con, ohDetails.getPrescribedId(),ChargeDTO.CH_DIAG_LAB);
								 success &= chargeBo.updateOhPayment(con,chargeId, visitCenterId);
							 }
						 }
					}					 					 
				}
			}
			
			AutomaticSampleRegistration incomingSampleReg = new AutomaticSampleRegistration(); 
			if (incomingSamplesRegMap != null) {
				Set<String> keys = incomingSamplesRegMap.keySet();
				Iterator it = keys.iterator();
				while (it.hasNext()) {
					String key = (String)it.next();
					String[] splitParts = key.split(",");
					success &= incomingSampleReg.sampleRegistrationInInternalLab(con, incomingSamplesRegMap.get(key),splitParts[0], diagGenericPref);			
					if (!success) {
						break;
					}					
				}
			}

			if (isPrint.equals("P")) {
				List<String> printURLs = new ArrayList<String>();
				String sampleCollectionIds = "";
				for(int i=0;i<transferredchecks.length; i++){
					sampleCollectionIds = sampleCollectionIds + transferredchecks[i];
					if(i != transferredchecks.length-1)
						sampleCollectionIds = sampleCollectionIds + ",";
				}
				printURLs.add(request.getContextPath() + "/Laboratory/SampleWorkSheetPrint.do?_method=printSampleWorkSheet&sampleCollectionIds="+sampleCollectionIds+"&bulkWorkSheetPrint=Y");
				session.setAttribute("printURLs", printURLs);
			}
			allSuccess = true;
			allSuccess &= success;
		} finally {
			DataBaseUtil.commitClose(con, allSuccess);
		}
		return mapping.findForward("saveRedirect");
	}
	
	public Object[] addToArray(Object[] array1, Object[] array2) {
		Object[] newArray = null;
		if (array1 != null && array2 != null) {
			newArray = new String[(array1.length + array2.length)];
			int index = 0;
				for (int i=0; i<array1.length; i++) {
					newArray[i] = array1[i];
					index = i;
				}				
				for (int j=0; j<array2.length; j++) {
					index++;
					newArray[index] = array2[j];
				}
		} else if (array1 != null && array2 == null) {
			return array1;
		}			
		return newArray;
	}
		
}
