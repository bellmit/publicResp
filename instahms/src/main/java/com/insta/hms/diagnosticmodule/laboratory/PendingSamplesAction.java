package com.insta.hms.diagnosticmodule.laboratory;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.bob.hms.common.Preferences;
import com.bob.hms.common.RequestContext;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.diagnosticmodule.common.OutHouseSampleDetails;
import com.insta.hms.diagnosticmodule.common.SampleCollection;
import com.insta.hms.diagnosticmodule.internallab.AutomaticSampleRegistration;
import com.insta.hms.diagnosticmodule.internallab.InternalLab;
import com.insta.hms.diagnosticmodule.prescribetest.DiagnoDAOImpl;
import com.insta.hms.diagnosticsmasters.addtest.AddTestDAOImpl;
import com.insta.hms.diagnosticsmasters.outhousemaster.OutHouseMasterDAO;
import com.insta.hms.ipservices.BedDTO;
import com.insta.hms.ipservices.IPBedDAO;
import com.insta.hms.master.DiagnosticDepartmentMaster.DiagnosticDepartmentMasterDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDTO;
import com.insta.hms.master.InComingHospitals.InComingHospitalDAO;
import com.insta.hms.master.SampleType.SampleTypeDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
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

public class PendingSamplesAction extends BaseAction {

	static Logger log = LoggerFactory.getLogger(PendingSamplesAction.class);
	static final JSONSerializer js = new JSONSerializer().exclude("class");
	
	private static final GenericDAO sampleCollectionCentersDAO = new GenericDAO("sample_collection_centers");
	private static final GenericDAO patientRegistrationDAO = new GenericDAO("patient_registration");
	private static final GenericDAO hospitalDirectBillPrefsDAO = new GenericDAO("hosp_direct_bill_prefs");
	private static final GenericDAO testsPrescribedDAO = new GenericDAO("tests_prescribed");

	@IgnoreConfidentialFilters
	public ActionForward pendingSamplesList(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException,
			ParseException {
		String category = mapping.getProperty("category");
		BasicDynaBean diagGenericPref = GenericPreferencesDAO.getdiagGenericPref();
		HttpSession session = request.getSession(false);
		String userId = (String) session.getAttribute("userid");
		String userDept = new DiagnosticDepartmentMasterDAO().getUserDepartment(userId);
		Map params = new HashMap(request.getParameterMap());
		 String department = request.getParameter("ddept_id");

		if (department == null ) {
			if (userDept != null && !userDept.equals(""))
				department = userDept;
		}

		params.put("ddept_id", new String[]{department});
		params.put("category", new String[]{category});
		params.remove("collectionCenterId");
		int centerId = (Integer) request.getSession(false).getAttribute("centerId");
		if (centerId != 0) {
			params.put("center_id", new String[]{centerId+""});
			params.put("center_id@type", new String[]{"integer"});
		}
		String collectionCenterId = request.getParameter("collectionCenterId");
		int userSampleCollectionCenterId = (Integer) request.getSession(false).getAttribute("sampleCollectionCenterId");
		if(null != collectionCenterId && !"".equals(collectionCenterId)) {
			params.put("collection_center_id", new String[]{collectionCenterId+""});
			params.put("collection_center_id@type", new String[]{"integer"});
		} else {
			if(userSampleCollectionCenterId != -1){
				params.put("collection_center_id", new String[]{userSampleCollectionCenterId+""});
				params.put("collection_center_id@type", new String[]{"integer"});
			}
		}
		List<BasicDynaBean> collectionCenters = sampleCollectionCentersDAO.findAllByKey("center_id", centerId);
		request.setAttribute("collectionCenters", collectionCenters);
		BasicDynaBean colcenterBean = sampleCollectionCentersDAO.findByKey("collection_center_id", -1);
		request.setAttribute("defautlCollectionCenter", colcenterBean.get("collection_center"));

		String date_range = request.getParameter("date_range");
		String week_start_date = null;
		if (date_range != null && date_range.equals("week")) {
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
	        Calendar cal = Calendar.getInstance();
	        cal.add(Calendar.DATE, -7);
	        Date openDt = cal.getTime();
	        week_start_date = dateFormat.format(openDt);

	        params.put("pres_date", new String[]{week_start_date, ""});
	        params.put("pres_date@op", new String[]{"ge,le"});
	        params.put("pres_date@cast", new String[]{"y"});
	        params.remove("date_range");
		}
		params.remove("screenId");
		Map<LISTING, Object> listingParams = ConversionUtils.getListingParameter(params);
		PagedList list = PendingSamplesBO.pendingSamplesList(params, listingParams, diagGenericPref);
		List<BasicDynaBean> incomingHospitals = new InComingHospitalDAO().listAll(
				 Arrays.asList(new String[] {"hospital_id","hospital_name"}), "status", "A", "hospital_name");
		List outHouses = OutHouseMasterDAO.getAllOutSources();

		request.setAttribute("pagedList", list);
		request.setAttribute("diagGenericPref", diagGenericPref);
		request.setAttribute("inHouses", js.serialize(ConversionUtils.copyListDynaBeansToMap(incomingHospitals)));
		request.setAttribute("outHouses", js.serialize(outHouses));
		request.setAttribute("category", category);
		request.setAttribute("module", category);
		request.setAttribute("userDept", department);
		request.setAttribute("genPrefs", GenericPreferencesDAO.getGenericPreferences());
        request.setAttribute("directBillingPrefs",
            ConversionUtils.listBeanToMapBean(hospitalDirectBillPrefsDAO.listAll(), "item_type"));
		request.setAttribute("test_timestamp", new DiagnoDAOImpl().getCountFromDiagTimeStamp());

		ActionForward forward = new ActionForward(mapping.findForward("list").getPath());
		// when ever user uses a pagination pres_date should not append again.
		if (date_range != null && date_range.equals("week") && request.getParameter("pres_date") == null) {
			addParameter("pres_date", week_start_date, forward);
	    }

		return forward;
	}
	public ActionForward searchPendingSamplesList(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException,
			ParseException {
		String category = mapping.getProperty("category");
		BasicDynaBean diagGenericPref = GenericPreferencesDAO.getdiagGenericPref();
		HttpSession session = request.getSession(false);
		String userId = (String) session.getAttribute("userid");
		String userDept = new DiagnosticDepartmentMasterDAO().getUserDepartment(userId);
		Map params = new HashMap(request.getParameterMap());
		 String department = request.getParameter("ddept_id");

		if (department == null ) {
			if (userDept != null && !userDept.equals(""))
				department = userDept;
		}

		params.put("ddept_id", new String[]{department});
		params.put("category", new String[]{category});
		params.remove("collectionCenterId");
		int centerId = (Integer) request.getSession(false).getAttribute("centerId");
		if (centerId != 0) {
			params.put("center_id", new String[]{centerId+""});
			params.put("center_id@type", new String[]{"integer"});
		}
		String collectionCenterId = request.getParameter("collectionCenterId");
		int userSampleCollectionCenterId = (Integer) request.getSession(false).getAttribute("sampleCollectionCenterId");
		if(null != collectionCenterId && collectionCenterId != "") {
			params.put("collection_center_id", new String[]{collectionCenterId+""});
			params.put("collection_center_id@type", new String[]{"integer"});
		} else {
			if(userSampleCollectionCenterId != -1){
				params.put("collection_center_id", new String[]{userSampleCollectionCenterId+""});
				params.put("collection_center_id@type", new String[]{"integer"});
			}
		}
		List<BasicDynaBean> collectionCenters = sampleCollectionCentersDAO.findAllByKey("center_id", centerId);
		request.setAttribute("collectionCenters", collectionCenters);
		BasicDynaBean colcenterBean = sampleCollectionCentersDAO.findByKey("collection_center_id", -1);
		request.setAttribute("defautlCollectionCenter", colcenterBean.get("collection_center"));
		List<BasicDynaBean> incomingHospitals = new InComingHospitalDAO().listAll(
				 Arrays.asList(new String[] {"hospital_id","hospital_name"}), "status", "A", "hospital_name");
		List outHouses = OutHouseMasterDAO.getAllOutSources();
		request.setAttribute("diagGenericPref", diagGenericPref);
		request.setAttribute("inHouses", js.serialize(ConversionUtils.copyListDynaBeansToMap(incomingHospitals)));
		request.setAttribute("outHouses", js.serialize(outHouses));
		request.setAttribute("category", category);
		request.setAttribute("module", category);
		request.setAttribute("screenType", mapping.getProperty("screen_type"));
		request.setAttribute("userDept", department);
		request.setAttribute("genPrefs", GenericPreferencesDAO.getGenericPreferences());
        request.setAttribute("directBillingPrefs",
            ConversionUtils.listBeanToMapBean(hospitalDirectBillPrefsDAO.listAll(), "item_type"));
		request.setAttribute("test_timestamp", new DiagnoDAOImpl().getCountFromDiagTimeStamp());

		ActionForward forward = new ActionForward(mapping.findForward("list").getPath());
		return forward;
	}
	
	public ActionForward getSampleCollectionScreen(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException, Exception{

		String visitId = request.getParameter("visitid");
	    String testId = request.getParameter("testId");
	    String category = mapping.getProperty("category");

	    List<BasicDynaBean> l=null;
	    PendingSamplesDAO samplesDao = new PendingSamplesDAO();
	    BasicDynaBean diagGenericPref = GenericPreferencesDAO.getdiagGenericPref();
	    
		if(visitId!=null){
			BasicDynaBean bean = getPatientDetails(visitId);

			if (bean == null) {
				FlashScope flash = FlashScope.getScope(request);
				flash.put("error", "No Patient with Id:"+visitId);
				ActionRedirect redirect = new ActionRedirect(mapping.findForward("saveRedirect"));
				redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
				return redirect;
			}

			VisitDetailsDAO visitDetDao = new VisitDetailsDAO();
			BasicDynaBean visitBean = visitDetDao.findByKey("patient_id", visitId);
    		request.setAttribute("visitBean", visitBean.getMap());
		    request.setAttribute("patientvisitdetails", bean);

			l = PendingSamplesDAO.getSampleCollectionList(visitId,testId);
			 Map sampleDetails = null;
			 List samplesList = new ArrayList();
			 Map<String, String> outSourceDestMap = new HashMap<String, String>();
			 
			 for(int i =0;i<l.size();i++){
				 sampleDetails = new HashMap(((BasicDynaBean)l.get(i)).getMap());
				 sampleDetails.put("collectible", samplesDao.isSampleCollectible(
						 (String)sampleDetails.get("dependent_test_id"),
						 (Integer)sampleDetails.get("common_order_id")));

				 int centerId = (Integer)sampleDetails.get("center_id");
				 boolean isOutsourceTest = samplesDao.isOutsourceTest((String)sampleDetails.get("test_id"),centerId);
				 sampleDetails.put("house_status", isOutsourceTest?"O":"I");
				 samplesList.add(sampleDetails);
				 ((BasicDynaBean)l.get(i)).set("house_status", isOutsourceTest?"O":"I");

			 }

			 //this is for getting sample rejection details.
			 List<BasicDynaBean> rejectionList = PendingSamplesDAO.getSampleRejectionDetails((String)bean.getMap().get("mr_no"));

			 Map rejectionDetails = null;
			 List rejection_det = new ArrayList();
			 for(int i =0;i<rejectionList.size();i++){
				 rejectionDetails = new HashMap(((BasicDynaBean)rejectionList.get(i)).getMap());
				 rejection_det.add(rejectionDetails);
			 }
			 request.setAttribute("rejection_det", rejection_det);

			 if(l.size() > 0){
				 request.setAttribute("distinctSamples", PendingSamplesDAO.getDistinctSampleNos(l));
				 request.setAttribute("sampleContainers", new JSONSerializer().serialize(PendingSamplesDAO.getSampleContainers()));
				 request.setAttribute("saplesList", samplesList);
				 request.setAttribute("rejectionList", js.serialize(ConversionUtils.listBeanToListMap(rejectionList)));
				 request.setAttribute("orderDatesList", PendingSamplesDAO.getOrderNos(visitId, testId, "orderdate", null));
				 request.setAttribute("outsourceNamesJSON",
						 new JSONSerializer().serialize(OutHouseMasterDAO.getAllOutSourceName((Integer) visitBean.get("center_id"), visitId)));
				 request.setAttribute("outsourcesAgainstTests", PendingSamplesDAO.getOuthousesAgainstTestId((Integer) visitBean.get("center_id"), false, visitId));
				 int userSampleColectionCenter = (Integer)new GenericDAO("u_user").findByKey("emp_username",
						 (request.getSession().getAttribute("userid"))).get("sample_collection_center");
				 Object savedSampleCollectionCenter = ((BasicDynaBean)l.get(0)).get("collection_center_id");
				 if(savedSampleCollectionCenter != null)
					 userSampleColectionCenter = (Integer)savedSampleCollectionCenter;

				 BasicDynaBean collectionCenterbean = sampleCollectionCentersDAO.findByKey("collection_center_id", userSampleColectionCenter);
				 request.setAttribute("collectionCenter",collectionCenterbean);
			 }

		}

		Map modulesActivatedMap = ((Preferences) RequestContext.getSession().getAttribute("preferences")).getModulesActivatedMap();
		VisitDetailsDAO visitDetDao = new VisitDetailsDAO();
		BasicDynaBean visitBean = visitDetDao.findByKey("patient_id", visitId);
		request.setAttribute("mod_central_lab", (String) (modulesActivatedMap.get("mod_central_lab")));
		request.setAttribute("visitBean", visitBean.getMap());
		request.setAttribute("genPrefs", GenericPreferencesDAO.getGenericPreferences());
		request.setAttribute("title", request.getParameter("title"));
		request.setAttribute("template_name", request.getParameter("template_name"));
		request.setAttribute("autoGenerate", (String)diagGenericPref.get("autogenerate_sampleid"));
		request.setAttribute("category", category);
		request.setAttribute("userDept",new DiagnosticDepartmentMasterDAO().getUserDepartment(
				(String) request.getSession(false).getAttribute("userid")));
			return mapping.findForward("getSampleCollectionScreen");
		}
	
	private BasicDynaBean getPatientDetails(String visitID)throws SQLException {
		List<String> columns = new ArrayList<String>();
		columns.add("patient_id");
		columns.add("mr_no");
		columns.add("center_id");
		Map<String, Object> identifiers = new HashMap<String, Object>();
		identifiers.put("patient_id", visitID);
		return patientRegistrationDAO.findByKey(columns, identifiers);
	}
	
	public ActionForward saveSamples(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException, Exception{

			 ArrayList<SampleCollection> scList = new ArrayList<SampleCollection>();
			 ArrayList<BasicDynaBean > tpList = new ArrayList<BasicDynaBean>();
			 SampleCollection sc = null;
			 Map requestMap = request.getParameterMap();

			 LaboratoryForm rf = (LaboratoryForm)form;
			 String prescribedid[] = (String[])requestMap.get("prescribed_id");
			 String sampleNo[] = (String[])requestMap.get("sampleId");
			 String outSourceDestId[] = (String[])requestMap.get("outSourceId");
			 String mrNo = rf.getMrno();
			 String visitId = rf.getVisitid();
			 String testid[] = (String[])requestMap.get("test_id");
			 String sampleTypeId[]=(String[])requestMap.get("sample_type_id");
			 String houseType[]=(String[])requestMap.get("house_status");
			 String needPrint=null;
			 String sampleNos="", outSourceNames = "", outSourceSampleNos = "", prescribedIds = "",
			 	sampleDates = "",sampleTypes="", outSourceSampleStatuses = "", outSourceDestTypes = "";
			 String sampleDate[] = (String[])requestMap.get("sample_date");
			 String typeofSubmit=rf.getNeedPrint();
			 String sampleStatus[] = (String[])requestMap.get("sample_status");
			 String newSample[] = (String[])requestMap.get("new");
			 String sampleSource[] = (String[])requestMap.get("sample_source_id");
			 String[] sampleCollectionID = (String[])requestMap.get("sample_collection_id");
			 needPrint  = typeofSubmit.equals("Save") ? "N" : "Y";
			 String qty[] = (String[])requestMap.get("qty");
			 String[] existingStatus = (String[])requestMap.get("existingStatus");
			 String userName = (String)request.getSession(false).getAttribute("userid");
			 ActionRedirect redirect = new ActionRedirect(mapping.findForward("saveRedirect"));
			 FlashScope flash = FlashScope.getScope(request);
			 GenericDAO sampleDAO = new GenericDAO("sample_collection");
			 BasicDynaBean diagGenericPref = GenericPreferencesDAO.getdiagGenericPref();
			 boolean autogenerate = "Y".equals((String)diagGenericPref.get("autogenerate_sampleid"));
			 String noGeneration = (String) diagGenericPref.get("sample_no_generation");
			 SampleTypeDAO sampleTypeDao = new SampleTypeDAO();
			 BasicDynaBean sampleTypeBean = null;
			 Map<String, String> sampleCollectionIdMap = new HashMap<String, String>();
			 int iD = -1;
			 boolean status = true;
			 int centerId = VisitDetailsDAO.getCenterId(visitId);
			 GenericDAO lDao = new GenericDAO("diag_outsource_master");
			 int userRegCenter = (Integer)patientRegistrationDAO.findByKey("patient_id", visitId).get("center_id");
			 StringBuilder errorMessage = new StringBuilder();
			 boolean allSuccess = false;

			 String[] sgSampleTypeIds = request.getParameterValues("sg_sample_type_id");
			 String[] sgSelectedStatus = request.getParameterValues("sg_selected_status");
			 String[] sgSampleStatus = request.getParameterValues("sg_sample_status");
			 String[] sgSampleTypeId = request.getParameterValues("sg_sample_type_id");
			 String[] sgOuthouseId = request.getParameterValues("sg_outsource_dest_id");
			 String[] sgSampleNo = request.getParameterValues("sg_sample_sno");
			 String[] sgSampleDate = request.getParameterValues("sg_sample_date");
			 String[] sgSampleSourceId = request.getParameterValues("sg_sample_source_id");
			 String[] sgSampleQty = request.getParameterValues("sg_qty");
			 String[] sgSampleOhId = request.getParameterValues("sg_oh_id");
			 String[] sgDate = request.getParameterValues("sg_sampleDate");

			 List<Object> samplesList = new ArrayList<Object>();

			 for (int i=0; i<sgSampleTypeIds.length-1; i++) {
				 if (!sgSelectedStatus[i].equals("") && !sgSampleStatus[i].equals("C") && !sgSampleStatus[i].equals("A")) {
					 for (int j=0; j<prescribedid.length; j++) {
						 if (sgSampleTypeId[i].equals(sampleTypeId[j]) && sgOuthouseId[i].equals(outSourceDestId[j]) && !sampleStatus[j].equals("")
								 && ((!autogenerate && existingStatus[j].equals("")) ? true : sgSampleNo[i].equals(sampleNo[j]))
								 && !existingStatus[j].equals("C") && !existingStatus[j].equals("A") && ("O".equals(houseType[j]) ? !outSourceDestId[j].equals("") : true)) {

								sampleDate[j] = sgSampleDate[i];
								sampleSource[j] = sgSampleSourceId[i];
								qty[j] = sgSampleQty[i];
								sampleStatus[j] = sgSelectedStatus[i];
						 }
					 }
				 }
			 }

			 Connection con = null;

			 try {
				 con = DataBaseUtil.getConnection();
				 con.setAutoCommit(false);
				 HashMap<String, String> sampleTypeIdMap = new HashMap<String, String>();
				 if (autogenerate && noGeneration.equals("P")) {
					String sampleID = null;
				 	if( prescribedid != null ){
						 for(int i=0; i<prescribedid.length; i++) {
							 if ("Y".equals(newSample[i]) && !sampleStatus[i].equals("") && !sampleStatus[i].equals("setInAction")
									 && !sampleTypeIdMap.containsKey(sampleTypeId[i]+outSourceDestId[i])) {
								 if (sampleTypeId[i] != null && !sampleTypeId[i].equals(""))
									 iD = Integer.parseInt(sampleTypeId[i]);
								 sampleTypeBean = sampleTypeDao.findByKey("sample_type_id", iD);
								 sampleID = SampleTypeDAO.getNextSampleNumber(iD, userRegCenter);
								 sampleNo[i] = sampleID;
								 sampleTypeIdMap.put(sampleTypeId[i]+outSourceDestId[i], sampleID);
							 } else if ("Y".equals(newSample[i]) && !sampleStatus[i].equals("") && !sampleStatus[i].equals("setInAction")) {
								 sampleNo[i] = sampleTypeIdMap.get(sampleTypeId[i]+outSourceDestId[i]);
							 }
						 }
				 	 }
				 } else if (autogenerate && noGeneration.equals("B")) {
					 if (prescribedid != null) {
						 boolean isNogenerated = false;
						 String sampleID = null;
						 for (int i=0; i<prescribedid.length; i++) {
							 if ("Y".equals(newSample[i]) && !sampleStatus[i].equals("") && !sampleStatus[i].equals("setInAction")
									 && !sampleTypeIdMap.containsKey(outSourceDestId[i])) {
								 sampleID = sampleTypeDao.getBatchBasedSampleNo(con);
								 sampleNo[i] = sampleID;
								 sampleTypeIdMap.put(outSourceDestId[i], sampleID);
							 } else if ("Y".equals(newSample[i]) && !sampleStatus[i].equals("") && !sampleStatus[i].equals("setInAction")) {
								 sampleNo[i] = sampleTypeIdMap.get(outSourceDestId[i]);
							 }
						 }
					 }
				 }

				 if(prescribedid != null && testid!=null){
				   	 for(int i=0; i<prescribedid.length;i++){

				   		 BasicDynaBean sampleBean = sampleDAO.findByKey("sample_sno",sampleNo[i]);
				   		 if(newSample[i].equals("Y") && sampleNo[i] != null && !sampleNo[i].equals("") &&
				   				 sampleBean != null && (autogenerate ? !noGeneration.equals("B") : true)) {
				   			return errorResponse(request, response, "Duplicate Sample Id "+sampleNo[i]);
				   		 }

				   		 // checking for cancel tests
	                     if(!sampleStatus[i].equals("") && !sampleStatus[i].equals("setInAction")
	                    		 && !isCommonSample(samplesList , sampleNo[i], sampleTypeId[i])){

	                    	 sampleTypeBean = sampleTypeDao.findByKey("sample_type_id", Integer.parseInt(sampleTypeId[i]));
	                    	 if (autogenerate && noGeneration.equals("B")) {
	                    		 samplesList.add(sampleNo[i]+""+sampleTypeId[i]);
	                    	 } else {
	                    		 samplesList.add(sampleNo[i]);
	                    	 }
							 sc  = new SampleCollection();
							 if ("Y".equals(newSample[i])) {
								 Integer sampleSequence = sampleDAO.getNextSequence();
								 sc.setSampleSequence(sampleSequence);
								 sampleCollectionID[i] = sampleSequence.toString();
								 sampleCollectionIdMap.put(sampleNo[i]+""+sampleTypeId[i], sampleCollectionID[i]);
							 } else {
								 sc.setSampleSequence(Integer.parseInt(sampleCollectionID[i]));
								 sampleCollectionIdMap.put(sampleNo[i]+""+sampleTypeId[i], sampleCollectionID[i]);
							 }

							 sc.setMrNo(mrNo);
							 sc.setVisitId(visitId);
							 sc.setTestId(testid[i]);
							 sc.setSampleNo(sampleNo[i]);
							 sc.setPrescribedId(Integer.parseInt(prescribedid[i]));
							 if(sampleDate[i] == null || sampleDate[i].trim().isEmpty())
								 sc.setSampleDate(DateUtil.getCurrentTimestamp());
							 else
								 sc.setSampleDate(DateUtil.parseTimestamp(sampleDate[i]));
							 sc.setUserName(userName);
							 sc.setSampleTypeId(Integer.parseInt(sampleTypeId[i]));
							 sc.setReqAutoGenId(rf.getReqAutoGenId());
							 sc.setSampleStatus(sampleStatus[i]);
							 sc.setSampleSourceId(sampleSource[i] != null && !sampleSource[i].isEmpty() ? Integer.parseInt(sampleSource[i]) : 0 );
							 int qtyValue = (qty[i] != null && !qty[i].equals("")) ? Integer.parseInt(qty[i]): Integer.parseInt("1") ;
							 sc.setSampleQty(qtyValue);
							 if(null != outSourceDestId[i] && !outSourceDestId[i].equals(""))
								 sc.setoutSourceId(outSourceDestId[i]);
							 sc.setSampleTransferStatus("P");
							 BedDTO beddetails = IPBedDAO.getCurrentBeddetails(visitId);
							 if(beddetails != null){
								 sc.setBedId(beddetails.getBed_id());
								 sc.setWardId(beddetails.getWardNo());
							 }
							 
							 if(sampleBean == null || !sampleBean.get("sample_status").equals(sampleStatus[i])){
	                    		 sampleNos = sampleNos+"'"+sampleNo[i]+"'"+',';
	                    		 sampleDates = sampleDates+"'"+DataBaseUtil.timeStampFormatter.format(sc.getSampleDate())+"'"+",";
	    						 sampleTypes = sampleTypes+"'"+sampleTypeBean.get("sample_type")+"'"+",";
	                    	 }
							 scList.add(sc);
	                     }
				   	 }// outer for

				 }

				 BasicDynaBean testPresBean = null;
				 String sampleCollId4Presc = "";

				 if( prescribedid != null ){
				   	 for(int i=0; i<prescribedid.length;i++){
	                	 testPresBean  = testsPrescribedDAO.findByKey("prescribed_id", Integer.parseInt(prescribedid[i]));
	                	 testPresBean.set("sample_no",(!sampleStatus[i].equals("") && !sampleStatus[i].equals("setInAction")) ? sampleNo[i] : null);
	                	 testPresBean.set("sflag", (sampleStatus[i].equals("C") || sampleStatus[i].equals("A")) ? "1" : "0");
	                	 testPresBean.set("user_name", userName);
	                	 if(null != outSourceDestId[i] && !outSourceDestId[i].equals("")) {
	                		 testPresBean.set("outsource_dest_id", Integer.parseInt(outSourceDestId[i]));
	                		 BasicDynaBean bean = lDao.findByKey("outsource_dest_id", Integer.parseInt(outSourceDestId[i]));
							 String outSourceDestType = (String)bean.get("outsource_dest_type");
							 if (!outSourceDestType.equals("C"))
								 testPresBean.set("outsource_dest_prescribed_id", null);//User may change internal lab center

	                	 }
	                	 sampleCollId4Presc = sampleCollectionIdMap.get(sampleNo[i]+""+sampleTypeId[i]);
	                	 if (sampleCollId4Presc != null && !sampleCollId4Presc.equals(""))
	                	 	testPresBean.set("sample_collection_id", Integer.parseInt(sampleCollId4Presc));

	                	 tpList.add(testPresBean);
		             }
				 }

				 if(!sampleNos.isEmpty()){
					 sampleNos=sampleNos.substring(0, sampleNos.length()-1);
					 sampleDates = sampleDates.substring(0,sampleDates.length()-1);
					 sampleTypes = sampleTypes.substring(0,sampleTypes.length()-1);
				 }

				 ArrayList<OutHouseSampleDetails> ohlist = null;
				 if(outSourceDestId != null){
					 ohlist = new ArrayList<OutHouseSampleDetails>();

					 if(prescribedid != null && testid!=null){
						 for(int i=0;i<prescribedid.length;i++){

							 // checking for cancell tests
		                     if(!sampleStatus[i].equals("") && !sampleStatus[i].equals("setInAction")){
						    	 if("O".equals(houseType[i])){

                                   testPresBean = testsPrescribedDAO.findByKey("prescribed_id",
                                       Integer.parseInt(prescribedid[i]));
						    		 if(Integer.parseInt((String)testPresBean.get("sflag")) != 1){
						    			 outSourceNames = outSourceNames+outSourceDestId[i]+',';
						    			 BasicDynaBean diagOutSourceBean = lDao.findByKey("outsource_dest_id", Integer.parseInt(outSourceDestId[i]));
						    			 outSourceDestTypes = outSourceDestTypes+diagOutSourceBean.get("outsource_dest_type")+',';
						    			 outSourceSampleNos = outSourceSampleNos+"'"+sampleNo[i]+"'"+',';
										 prescribedIds = prescribedIds+"'"+prescribedid[i]+"'"+',';
										 outSourceSampleStatuses = outSourceSampleStatuses+sampleStatus[i]+",";
										 OutHouseSampleDetails ohDetails = new OutHouseSampleDetails();
										 ohDetails.setVisitId(visitId);
										 ohDetails.setPrescribedId(Integer.parseInt(prescribedid[i]));
										 ohDetails.setSampleNo(sampleNo[i]);
										 ohDetails.setTestId(testid[i]);
										 ohDetails.setoutSourceId(outSourceDestId[i]);
										 ohDetails.setReqAutoGenId(rf.getReqAutoGenId());
										 ohDetails.setSampleStatus(sampleStatus[i]);
										 ohlist.add(ohDetails);
						    		 }
								}
							}
						}
					 }
				 }

				 if(!outSourceNames.isEmpty()){
					 outSourceNames = outSourceNames.substring(0, outSourceNames.length()-1);
					 outSourceDestTypes = outSourceDestTypes.substring(0, outSourceDestTypes.length()-1);
					 outSourceSampleNos = outSourceSampleNos.substring(0, outSourceSampleNos.length()-1);
					 prescribedIds = prescribedIds.substring(0, prescribedIds.length()-1);
					 outSourceSampleStatuses = outSourceSampleStatuses.substring(0, outSourceSampleStatuses.length()-1);
				 }

				 status = avoidDifferentOutsourcesForILabTests(tpList, errorMessage);

				 if(!scList.isEmpty()) {
					 status &= PendingSamplesBO.saveSamples(scList, ohlist, tpList, con,centerId);

					if(status) {
						 AutomaticSampleRegistration sampleReg = new AutomaticSampleRegistration();
						 List<Object> internalLabList = new ArrayList<Object>();

						 for(int i=0; i<outSourceDestId.length; i++) {
							 int labTestsidx = 0;
							 BasicDynaBean prescBean = testsPrescribedDAO.findByKey("prescribed_id",
									 Integer.parseInt(prescribedid[i]));
							 if(Integer.parseInt((String)prescBean.get("sflag")) != 1 && !outSourceDestId[i].isEmpty() && sampleStatus[i].equals("C"))  {
								 InternalLab labDto = new InternalLab();
								 BasicDynaBean bean = lDao.findByKey("outsource_dest_id", Integer.parseInt(outSourceDestId[i]));
								 String outSourceDestType = (String)bean.get("outsource_dest_type");

								 if(outSourceDestType.equals("C")) {
									 int internalLabTestsCount = getInternalLabTestsCount(outSourceDestId[i],outSourceDestId,testid,prescribedid, sampleStatus);
									 String[] internalLabTestIds = new String[internalLabTestsCount];
									 String[] internalLabprescIds = new String[internalLabTestsCount];
									 String[] internalLabsampleNos = new String[internalLabTestsCount];
									 String[] internalLabsampleTypeIds = new String[internalLabTestsCount];
									 String outsourceDest = (String)bean.get("outsource_dest");
									 if(!internalLabList.contains(outsourceDest)){
										 internalLabList.add(outsourceDest);
										 internalLabTestIds[labTestsidx] = testid[i];
										 internalLabprescIds[labTestsidx] = prescribedid[i];
										 internalLabsampleNos[labTestsidx] = sampleNo[i];
										 internalLabsampleTypeIds[labTestsidx] = sampleTypeId[i];
										 labTestsidx++;
										 labDto.setInternalLabCenterId(outsourceDest);
										 for(int t=i+1; t<testid.length; t++) {
											 prescBean = testsPrescribedDAO.findByKey("prescribed_id",
													 Integer.parseInt(prescribedid[t]));
											 if(!outSourceDestId[t].isEmpty() && outSourceDestId[i].equals(outSourceDestId[t]) &&
													 Integer.parseInt((String)prescBean.get("sflag")) != 1 && sampleStatus[t].equals("C")){
												 internalLabTestIds[labTestsidx] = testid[t];
												 internalLabprescIds[labTestsidx] = prescribedid[t];
												 internalLabsampleNos[labTestsidx] = sampleNo[t];
												 internalLabsampleTypeIds[labTestsidx] = sampleTypeId[t];
												 labTestsidx++;
											 }
										 }
										 labDto.setInternalLabTestIds(internalLabTestIds);
										 labDto.setInternalLabPrescIds(internalLabprescIds);
										 labDto.setInternalLabSampleNos(internalLabsampleNos);
										 labDto.setInternalLabSampleTypeIds(internalLabsampleTypeIds);
										 status &= sampleReg.sampleRegistrationInInternalLab(con,labDto,visitId,diagGenericPref);
									 }
								 }
							 }
						 }
					 }
				 }
				 
				 GenericPreferencesDTO dto = GenericPreferencesDAO.getGenericPreferences();
				 if (!status)
					flash.error("Failed to collect Sample details.."+(errorMessage.toString().equals("") ? "" : errorMessage));

				 redirect.addParameter("visitid", visitId);
				 redirect.addParameter("category", "DEP_LAB");
				 redirect.addParameter("pageNum", request.getParameter("pageNum"));
				 redirect.addParameter("from", "internal");
				 redirect.addParameter("printType", dto.getSampleCollectionPrintType());
				 redirect.addParameter("title", request.getParameter("title"));
				 redirect.addParameter("needPrint", needPrint);
				 redirect.addParameter("sampleNo", sampleNos);
				 redirect.addParameter("outSourceSampleNos", outSourceSampleNos);
				 redirect.addParameter("outSourceName", outSourceNames);
				 redirect.addParameter("prescribedIds", prescribedIds);
				 redirect.addParameter("outSourceSampleStatuses", outSourceSampleStatuses);
				 redirect.addParameter("template_name", ((String[])requestMap.get("sampleBardCodeTemplate"))[0]);
				 redirect.addParameter("sampleDates", sampleDates);
				 redirect.addParameter("sampleTypes", sampleTypes);
				 redirect.addParameter("outsourceDestType", outSourceDestTypes);
				 redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

				 allSuccess = true;
				 allSuccess &= status;

				 return redirect;

			 } finally {
				 DataBaseUtil.commitClose(con, allSuccess);
			 }

	}

	public int getInternalLabTestsCount(String outSourceDestId, String[] outSourceDestIds,
			String[] testIds,String[] prescIds, String[] sampleStatus)throws Exception{
		int internalLabTestsCount = 0;
		for(int i=0; i<testIds.length; i++) {
			BasicDynaBean prescBean = testsPrescribedDAO.findByKey("prescribed_id", Integer.parseInt(prescIds[i]));
			if(!outSourceDestIds[i].isEmpty() && outSourceDestId.equals(outSourceDestIds[i]) &&
					 Integer.parseInt((String)prescBean.get("sflag")) != 1 && sampleStatus[i].equals("C")) {
				internalLabTestsCount++;
			}
		}
		return internalLabTestsCount;
	}

	public boolean isCommonSample(List sampleNosList, String sampleNo, String sampleTypeID)throws SQLException{

		if( sampleNosList.isEmpty() )
			return false;
		BasicDynaBean diagGenericPref = GenericPreferencesDAO.getdiagGenericPref();
		boolean autogenerate = "Y".equals((String)diagGenericPref.get("autogenerate_sampleid"));
		String noGeneration = (String) diagGenericPref.get("sample_no_generation");
		boolean dupSample = false;
		if (autogenerate && noGeneration.equals("B")) {
			dupSample = sampleNosList.contains(sampleNo+""+sampleTypeID);
		} else {
			dupSample = sampleNosList.contains(sampleNo);
		}
		return dupSample;
	}

	public ActionForward getOrdernosCorrespondingTotheDate(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)throws SQLException, IOException, ParseException {

		String visitID = request.getParameter("visitID");
		String orderDate = request.getParameter("date");
		String testId = request.getParameter("testId");
		JSONSerializer json = new JSONSerializer().exclude("class");

		List<BasicDynaBean> ordernosList = PendingSamplesDAO.getOrderNos(visitID, testId, "orderno", orderDate);
		response.setContentType("text/plain");
		response.setHeader("Cache_Control", "no_cache");
		response.getWriter().write(json.serialize(ConversionUtils.listBeanToListMap(ordernosList)));


		return null;
	}

	//Avoid to map different outsource for tests in case of internal lab tests.
	private boolean avoidDifferentOutsourcesForILabTests(List<BasicDynaBean> tpList, StringBuilder msg)throws SQLException {
		boolean status = true;
		BasicDynaBean tpBean = null;
		BasicDynaBean tpRefBean = null;

		for (int i=0; i<tpList.size(); i++) {
			tpBean = tpList.get(i);
			if (((Boolean)tpBean.get("re_conduction")) && null != tpBean.get("outsource_dest_id")
					&& !tpBean.get("outsource_dest_id").equals("")
					&& testsPrescribedDAO.findByKey("prescribed_id", tpBean.get("prescribed_id")).get("sflag").toString().equals("0")) {
				tpRefBean = testsPrescribedDAO.findByKey("prescribed_id", tpBean.get("reference_pres"));
				if (null != tpRefBean.get("outsource_dest_prescribed_id")
						&& !tpRefBean.get("outsource_dest_prescribed_id").equals("")
						&& !(tpRefBean.get("outsource_dest_id").equals(tpBean.get("outsource_dest_id")))) {
					msg.append("\n Outsources can not be different for the reconducted test "+AddTestDAOImpl.getTestName(tpBean.get("test_id").toString()).toUpperCase());
					status = false;
					break;
				}

			}
		}

		return status;
	}

}
