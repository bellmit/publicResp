package com.insta.hms.diagnosticmodule.laboratory;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.bob.hms.common.Preferences;
import com.bob.hms.common.RequestContext;
import com.bob.hms.diag.ohsampleregistration.OhSampleRegistrationDAO;
import com.insta.hms.Registration.PatientDetailsDAO;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.DynaBeanBuilder;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.PrintPageOptions;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.core.clinical.order.testvisitreports.TestVisitReportsService;
import com.insta.hms.diagnosticmodule.common.DiagnosticsDAO;
import com.insta.hms.diagnosticmodule.common.SampleCollection;
import com.insta.hms.diagnosticmodule.common.TestConducted;
import com.insta.hms.diagnosticmodule.common.TestDetails;
import com.insta.hms.diagnosticmodule.common.TestPrescribed;
import com.insta.hms.diagnosticmodule.common.TestVisitReports;
import com.insta.hms.diagnosticmodule.prescribetest.DiagnoDAOImpl;
import com.insta.hms.diagnosticsmasters.ResultRangesDAO;
import com.insta.hms.diagnosticsmasters.Test;
import com.insta.hms.diagnosticsmasters.TestandResults;
import com.insta.hms.diagnosticsmasters.addtest.TestResultsDAO;
import com.insta.hms.diagnosticsmasters.outhousemaster.OutHouseMasterDAO;
import com.insta.hms.genericdocuments.PatientGeneralImageDAO;
import com.insta.hms.integration.configuration.InterfaceEventMappingService;
import com.insta.hms.master.DiagnosticDepartmentMaster.DiagnosticDepartmentMasterDAO;
import com.insta.hms.master.DiagnosticReagentMaster.DiagnosticReagentMasterDAO;
import com.insta.hms.master.GenericImageMaster.GenericImageDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.Histopathology.HistoImpressionMasterDao;
import com.insta.hms.master.InComingHospitals.InComingHospitalDAO;
import com.insta.hms.master.Microbiology.MicroAbstPanelMasterDao;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;
import com.insta.hms.master.PrintTemplates.PrintTemplate;
import com.insta.hms.master.PrintTemplates.PrintTemplatesDAO;
import com.insta.hms.master.ReferalDoctor.ReferalDoctorDAO;
import com.insta.hms.messaging.MessageManager;
import com.insta.hms.messaging.MessageUtil;
import com.insta.hms.resourcescheduler.ResourceDAO;
import com.insta.hms.stores.StockFIFODAO;
import com.insta.hms.stores.StoreItemStock;
import com.insta.hms.usermanager.UserDAO;
import flexjson.JSONSerializer;
import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.DynaBean;
import org.apache.http.HttpHeaders;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class LaboratoryAction extends BaseAction {

	public static final Logger log = LoggerFactory.getLogger(LaboratoryAction.class);

	public enum return_type {PDF, PDF_BYTES, TEXT_BYTES};
	
	private static final String DEP_LAB = "DEP_LAB";

	private static PrintTemplatesDAO printTemplateDAO = new PrintTemplatesDAO();
	private static LaboratoryDAO lDao = new LaboratoryDAO();
	private static DiagnosticDepartmentMasterDAO diagnosticDepartmentMasterDao = new DiagnosticDepartmentMasterDAO();
	private static InComingHospitalDAO inComingHospitalDao = new InComingHospitalDAO();
	private static GenericDAO sampleCollectionCentersDao = new GenericDAO("sample_collection_centers");
	private static DiagnoDAOImpl diagDaoImpl = new DiagnoDAOImpl();
	private static GenericDAO hospDirectBillPrefsDao = new GenericDAO("hosp_direct_bill_prefs");
	private static GenericDAO testVisitReportsDao = new GenericDAO("test_visit_reports");
	private static GenericDAO diagDao = new GenericDAO("diagnostics");
	private static GenericDAO testsConductedDao = new GenericDAO("tests_conducted");
	private static GenericDAO testPrescDao = new GenericDAO("tests_prescribed");
	private static DiagnosticsDAO diagnosticsDao = new DiagnosticsDAO();
	private static final GenericDAO incomingSampleRegistrationDAO = new GenericDAO("incoming_sample_registration");
	
	private final InterfaceEventMappingService interfaceEventMappingService = (InterfaceEventMappingService) ApplicationContextProvider
	      .getApplicationContext().getBean("interfaceEventMappingService");
	private final TestVisitReportsService testVisitReportsService = (TestVisitReportsService) ApplicationContextProvider
        .getApplicationContext().getBean("testVisitReportsService");
	
	@IgnoreConfidentialFilters
	public ActionForward searchScreen(ActionMapping m, ActionForm f, HttpServletRequest req,
			HttpServletResponse res) throws IOException, ServletException, Exception {
		String category = m.getProperty("category");

		BasicDynaBean diagGenericPref = GenericPreferencesDAO.getdiagGenericPref();
		req.setAttribute("islabNoReq", (String)diagGenericPref.get("autogenerate_labno"));
		req.setAttribute("diagGenericPref", diagGenericPref);
		JSONSerializer js = new JSONSerializer().exclude("class");
		List HistoCytoNames = HistoImpressionMasterDao.getAllActiveHistoImpressions();

		/*
		 * User's department for defaulting the list
		 */
		HttpSession session = req.getSession(false);
		String userId =(String)session.getAttribute("userid");
		int centerId = (Integer)session.getAttribute("centerId");
		String userDept = diagnosticDepartmentMasterDao.getUserDepartment(userId);
  	    String department = req.getParameter("ddept_id");

		if (department == null ) {
			if (userDept != null && !userDept.equals(""))
				department = userDept;
		}

		req.setAttribute("userDept", department);
		req.setAttribute("HistoCytoNames", js.serialize(HistoCytoNames));

		List<BasicDynaBean> incomingHospitals = inComingHospitalDao.listAll(
				Arrays.asList(new String[] {"hospital_id","hospital_name"}), "status", "A", "hospital_name");
		req.setAttribute("inHouses", js.serialize(ConversionUtils.copyListDynaBeansToMap(incomingHospitals)));

		List outHouses = OutHouseMasterDAO.getAllOutSources();
		req.setAttribute("outHouses", js.serialize(outHouses));

		req.setAttribute("module", category);
		req.setAttribute("category", category);
		req.setAttribute("screenType", m.getProperty("screen_type"));
		req.setAttribute("initialScreen", "true");
		req.setAttribute("test_timestamp", diagDaoImpl.getCountFromDiagTimeStamp());

		List<BasicDynaBean> collectionCenters = sampleCollectionCentersDao.findAllByKey("center_id", centerId);
		req.setAttribute("collectionCenters", collectionCenters);
		BasicDynaBean colcenterBean = sampleCollectionCentersDao.findByKey("collection_center_id", -1);
		req.setAttribute("defautlCollectionCenter", colcenterBean.get("collection_center"));
		req.setAttribute("referalDetails",js.serialize(ReferalDoctorDAO.getReferencedoctors()));
		req.setAttribute("prescribedDoctors", js.serialize(ReferalDoctorDAO.getPrescribedDoctors()));

		String date_range = req.getParameter("date_range");
		String week_start_date = null;
		if (date_range != null && date_range.equals("week")) {
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
	        Calendar cal = Calendar.getInstance();
	        cal.add(Calendar.DATE, -7);
	        Date openDt = cal.getTime();
	        week_start_date = dateFormat.format(openDt);
		}

		ActionForward forward = new ActionForward(m.findForward("schedules").getPath());
		// when ever user uses a pagination pres_date should not append again.
		if (date_range != null && date_range.equals("week") && req.getParameter("pres_date") == null) {
			addParameter("pres_date", week_start_date, forward);
	    }

		return forward;
	}

	public void addParameter(String key, String value, ActionForward forward) {
        StringBuffer sb = new StringBuffer(forward.getPath());
        if (key == null || key.length() < 1)
            return ;
        if (forward.getPath().indexOf('?') == -1)
            sb.append('?');
        else
            sb.append('&');
        sb.append(key + "=" + value);
        forward.setPath(sb.toString());
    }


	@IgnoreConfidentialFilters
	public ActionForward getScheduleList(ActionMapping m, ActionForm f, HttpServletRequest req,
			HttpServletResponse res) throws IOException, ServletException,Exception {

		String category = m.getProperty("category");
		GenericDAO  sampleColCenterDAO = sampleCollectionCentersDao;
		LaboratoryBO bo = new LaboratoryBO();
		BasicDynaBean diagGenericPref = GenericPreferencesDAO.getdiagGenericPref();
		req.setAttribute("islabNoReq", (String)diagGenericPref.get("autogenerate_labno"));
		req.setAttribute("diagGenericPref", diagGenericPref);
		JSONSerializer js = new JSONSerializer().exclude("class");

		/*
		 * User's department for defaulting the list
		 */
		HttpSession session = req.getSession(false);
		String userId =(String)session.getAttribute("userid");
  	    String department = req.getParameter("ddept_id");
  	    
  	    
		if (department == null ) {
			String userDept = diagnosticDepartmentMasterDao.getUserDepartment(userId);
			if (userDept != null && !userDept.equals(""))
				department = userDept;
		}

		req.setAttribute("userDept", department);
		List<BasicDynaBean> incomingHospitals = inComingHospitalDao.listAll(
				Arrays.asList(new String[] {"hospital_id","hospital_name"}), "status", "A", "hospital_name");
		req.setAttribute("inHouses", js.serialize(ConversionUtils.copyListDynaBeansToMap(incomingHospitals)));

		List outHouses = OutHouseMasterDAO.getAllOutSources();
		req.setAttribute("outHouses", js.serialize(outHouses));


		req.setAttribute("module", category);
		req.setAttribute("category", category);
		Map paramMap = new HashMap(req.getParameterMap());
		
		String expRepReadyDate = req.getParameter("_exp_rep_ready_date");
		String expRepReadyTime =req.getParameter("_exp_rep_ready_time");
		if(expRepReadyTime==null || "".equals(expRepReadyTime))
			expRepReadyTime="23:59:59";	
		String exp_rep_ready_time = new String();
		if(expRepReadyDate != null  && !"".equals(expRepReadyDate)) {
			exp_rep_ready_time = expRepReadyDate+" "+expRepReadyTime;
		}
		paramMap.remove("_exp_rep_ready_date");
		paramMap.remove("_exp_rep_ready_time");
		paramMap.put("exp_rep_ready_time", new String[]{exp_rep_ready_time});
		paramMap.remove("collectionCenterId");
		paramMap.put("ddept_id", new String[]{department});
		Map listing = ConversionUtils.getListingParameter(paramMap);
		listing.put(LISTING.PAGESIZE, 10);

		int centerId = (Integer) req.getSession(false).getAttribute("centerId");
		if (centerId != 0) {
			paramMap.put("center_id", new String[]{centerId+""});
			paramMap.put("center_id@type", new String[]{"integer"});
		}
		String collectionCenterId = req.getParameter("collectionCenterId");
		int userSampleCollectionCenterId = (Integer) req.getSession(false).getAttribute("sampleCollectionCenterId");
		if(null != collectionCenterId &&  !"".equals(collectionCenterId)) {
			paramMap.put("collection_center_id", new String[]{collectionCenterId+""});
			paramMap.put("collection_center_id@type", new String[]{"integer"});
		} else {
			if(userSampleCollectionCenterId != -1){
				paramMap.put("collection_center_id", new String[]{userSampleCollectionCenterId+""});
				paramMap.put("collection_center_id@type", new String[]{"integer"});
			}
		}
		List<BasicDynaBean> collectionCenters = sampleColCenterDAO.findAllByKey("center_id", centerId);
		if(req.getParameter("pres_doctor")!=null && !req.getParameter("pres_doctor").equals(""))
			paramMap.put("pres_doctor",new String[]{req.getParameter("pres_doctor")});

		req.setAttribute("collectionCenters", collectionCenters);
		BasicDynaBean colcenterBean = sampleColCenterDAO.findByKey("collection_center_id", -1);
		req.setAttribute("defautlCollectionCenter", colcenterBean.get("collection_center"));

		String date_range = req.getParameter("date_range");
		String week_start_date = null;
		if (date_range != null && date_range.equals("week")) {
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
	        Calendar cal = Calendar.getInstance();
	        cal.add(Calendar.DATE, -7);
	        Date openDt = cal.getTime();
	        week_start_date = dateFormat.format(openDt);

	        paramMap.put("pres_date", new String[]{week_start_date, ""});
			paramMap.put("pres_date@op", new String[]{"ge,le"});
			paramMap.remove("date_range");
		}
		String origsampleSno = req.getParameter("orig_sample_no");
		String origsampleSnos = null;
		String[] origsampleSnosArray = null;
		if(origsampleSno != null && !origsampleSno.equals("")) {
			origsampleSnos = origsampleSno;
			origsampleSnosArray = origsampleSnos.split(",");
		}
		paramMap.remove("orig_sample_no");
		// list of all visits in a paged manner.
		Map filterParamMap = removeEmptyParams(paramMap);
		PagedList pagedList = LaboratoryDAO
		    .getDiagSchedulesVisits(category, filterParamMap, listing, origsampleSnosArray);
		req.setAttribute("pagedList",pagedList);

		if (pagedList.getDtoList().size() > 0) {
			// details (reports/tests) contained in each visit
			List<String> visitIds = new ArrayList<String>();
			for (Map obj : (List<Map>) pagedList.getDtoList()) {
			  visitIds.add((String) obj.get("pat_id"));
			}

			List<BasicDynaBean> details = LaboratoryDAO.getDiagSchedulesDetails(category, paramMap, visitIds);
			Map visitDetails = ConversionUtils.listBeanToMapListListBean(details, "patient_id", "report_id");
			req.setAttribute("visitDetails", visitDetails);
		}
		req.setAttribute("directBillingPrefs", ConversionUtils.listBeanToMapBean(
				hospDirectBillPrefsDao.listAll(),"item_type"));
		req.setAttribute("test_timestamp", diagDaoImpl.getCountFromDiagTimeStamp());
		List l2 = bo.getLabtechnicions(category, centerId);
		req.setAttribute("doctors", l2);
		req.getAttribute("screenType");
		req.setAttribute("prescribedDoctors", js.serialize(ReferalDoctorDAO.getPrescribedDoctors()));

		ActionForward forward = new ActionForward(m.findForward("schedules").getPath());
		// when ever user uses a pagination pres_date should not append again.
		if (date_range != null && date_range.equals("week") && req.getParameter("pres_date") == null) {
			addParameter("pres_date", week_start_date, forward);
	    }

		return forward;

	}
	
  private Map removeEmptyParams(Map paramMap) {
    Iterator it = paramMap.entrySet().iterator();
    Map filterParams = new HashMap();
    filterParams.putAll(paramMap);
    while (it.hasNext()) {
      Map.Entry e = (Map.Entry) it.next();
      String[] paramValues = ((String[]) e.getValue());
      if (isParamStringArrayEmpty(paramValues)) {
        filterParams.remove(e.getKey());
      }
    }
    return filterParams;
  }
  
  private boolean isParamStringArrayEmpty(String[] values) {
    for (int i = 0; i < values.length; i++) {
      if (!StringUtils.isEmpty(values[i])) {
        return false;
      }
    }
    return true;
  }
  
  @IgnoreConfidentialFilters
  public ActionForward searchReferralDoctors(ActionMapping m, ActionForm f, HttpServletRequest req,
      HttpServletResponse res) throws SQLException, IOException {
    res.setContentType("text/plain");
    res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    JSONSerializer js = new JSONSerializer().exclude("class");
    String query = req.getParameter("query");
    List<BasicDynaBean> referalDocs = ReferalDoctorDAO.getReferenceDoctors(query);
    Map result = new HashMap();
    result.put("result", ConversionUtils.copyListDynaBeansToMap(referalDocs));
    js.deepSerialize(result, res.getWriter());
    res.flushBuffer();
    return null;
  }

	public ActionForward getBatchConductionScreen(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)throws Exception{
		String reportId = request.getParameter("reportId");
		String category = request.getParameter("category");
		String mod_cons_modify = "Y";
		BasicDynaBean pref=null;

		String fromScreen = request.getParameter("fromScreen");
		if (fromScreen !=null){
			if (fromScreen.equals("manageReport")){
				reportId = request.getParameter("reportIdForEditResults");
			}
		}
		Preferences module_pref = (Preferences)request.getSession(false).getAttribute("preferences");
		if ( (module_pref!=null) && (module_pref.getModulesActivatedMap() != null) ) {
			mod_cons_modify = (String)module_pref.getModulesActivatedMap().get("mod_consumables_flow");
            if (mod_cons_modify == null || "".equals(mod_cons_modify)){
            	mod_cons_modify = "N";
            }
        }
	    request.setAttribute("mod_activation_status", mod_cons_modify);

		if (reportId!=null && !reportId.equals("") && !reportId.equals("0")){
			ArrayList<String> presList = LaboratoryDAO.getTestsListsForReportId(reportId,category);
			String presArray[] = null;
			presArray    = populateListValuesTOArray(presList);
			request.setAttribute("printreportId",reportId);
			request.setAttribute("selectedPresList",presArray);
		}
		
		BasicDynaBean pd = getPatientDetails(request.getParameter("visitid"));
	    getVisitTestDetails(request, pd);
	    if (pd !=null){
	    	request.setAttribute("patientvisitdetails", pd);
	    } else{
	    	request.setAttribute("custmer",
	    			OhSampleRegistrationDAO.getIncomingCustomer(request.getParameter("visitid")));
	    }
	    if (category.equals("DEP_LAB"))
		    pref = PrintConfigurationsDAO.getDiagDefaultPrintPrefs();
		 else
			pref = PrintConfigurationsDAO.getDiagRadDefaultPrintPrefs();

		if(reportId != null && !reportId.isEmpty())
			request.setAttribute("reportForSignoff",
					LaboratoryDAO.getReport(Integer.parseInt(reportId)));

		request.setAttribute("pref", pref);
		request.setAttribute("category", category);
		String cytoConduction = request.getParameter("conductionCyto");
		 if (cytoConduction != null && cytoConduction.equals("Y")) {
			 request.setAttribute("cytoConduction", cytoConduction);
		 }
		 request.setAttribute("amendresult", request.getParameter("amendresult"));
		 request.setAttribute("jsonHtmlColorCodes", GenericPreferencesDAO.getAllPrefs());
		return mapping.findForward("getbacthconductionscreen");
	}


	private static String[]  populateListValuesTOArray(ArrayList<String>al){
		Iterator<String>  it = al.iterator();
		String[] strArray = new String[al.size()];

		int i=0;
		while(it.hasNext()){
		  strArray[i++]= it.next();
		}

		return strArray;
	}
	
	private BasicDynaBean getPatientDetails(String visitID)throws SQLException {
		List<String> columns = new ArrayList<String>();
		columns.add("patient_id");
		columns.add("mr_no");
		columns.add("center_id");
		Map<String, Object> identifiers = new HashMap<String, Object>();
		identifiers.put("patient_id", visitID);
		return new GenericDAO("patient_registration").findByKey(columns, identifiers);
	}

	public ActionForward saveTestDetails(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)throws Exception{

			log.debug("saveTestDetails Laboratory===>");
			 LaboratoryForm rf = (LaboratoryForm)form;
			 Map requestMap = request.getParameterMap();
			 HttpSession session = request.getSession();

			 String testid[] = rf.getTestid();
			 String prescribedid[] = rf.getPrescribedid();
			 String completed[] = rf.getCompleted();
			 String resultlabel[] = rf.getResultlabel();
			 String resultlabel_id[] = (String[])requestMap.get("resultlabel_id");
			 String referenceRanges[] = rf.getReferenceRanges();
			 String rtestId[] = rf.getRtestId();
			 String conductedinreportformat[] = rf.getConductedinreportformat();
			 String formatid[] = rf.getFormatid();
			 String withinNormal[] = (String[])requestMap.get("seviarity");
			 String units[] = rf.getUnits();
			 String rprescribedId[] = rf.getRprescribedId();
			 String doctor[] = rf.getDoctor();
			 String resultvalue[] = rf.getResultvalue();
			 String remarks[] = rf.getRemarks();
			 String mrNo = rf.getMrno();
			 if(mrNo!=null && mrNo.equals("")) mrNo = null;
			 String visitId = rf.getVisitid();
			 String dateOfInvestigation[] = rf.getDateOfInvestigation();
			 String ddeptId[] = rf.getDdeptid();
			 String dateOfSample[] = rf.getDateOfSample();
			 String testRemarks[] = rf.getTestRemarks();
			 String saveandprint = rf.getSaveandprint();
			 String printreportId = rf.getPrintreportId();
			 String category = rf.getCategory();
			 java.sql.Time currentTime = new java.sql.Time(new java.util.Date().getTime());
			 String time =  DataBaseUtil.timeFormatterSecs.format(currentTime);
			 String userName = (String)request.getSession(false).getAttribute("userid");
			 Preferences pref = (Preferences)request.getSession(false).getAttribute("preferences");
			 String timeOfInvestigation[] = (String[])requestMap.get("timeOfInvestigation");
			 String specimenCondition[] = (String[])requestMap.get("specimen_condition");
			 String sampleNumber[] = (String[])requestMap.get("sampleno");//sample_sno in sample_collection table
			 String resultDisclaimer[] = (String[]) requestMap.get("resultDisclaimer");
			 String impression_id[] = (String[])requestMap.get("impression_id");
			 String histoPrescribedId[] = (String[])requestMap.get("hprescribed_id");
			 String nogrowthTemplateId[] = (String[])requestMap.get("nogrowth_template_id");
			 String[] growthExists = (String[])requestMap.get("growth_exists");
			 String microPrescribedId[] = (String[])requestMap.get("mprescribed_id");
			 String result_expr_hidden[]  = (String[])requestMap.get("calc_res_expr");
			 String[] cPrecribedId = (String[])requestMap.get("cprescribed_id");//related to cytology test
			 String[] cyto_microscopic_details = (String[])requestMap.get("cyto_microscopic_details");
			 String[] cytoImpressionId = (String[])requestMap.get("cyto_impression_id");
			 String[] histoImpressionId = (String[])requestMap.get("histo_impression_id");

			 String[] testDetailsid = (String[])requestMap.get("test_details_id");
			 String[] deletedNewTestDetailsId  = (String[])requestMap.get("deleted_new_test_details_id");
			 String[] revisedTestDetailsId = (String[])requestMap.get("revised_test_details_id");
			 String[] originalTestDetailsId = (String[])requestMap.get("original_test_details_id");
			 String[] amenndmentreason = (String[])requestMap.get("amendment_reason");
			 String[] testDetailsStatus = (String[])requestMap.get("test_detail_status");
			 String[] deleted = (String[])requestMap.get("deleted");
			 String[] labNo = (String[])requestMap.get("labNo");
			 String[] technician = (String[]) requestMap.get("h_technician");
			 String[] newResultAfterAmendment = (String[]) requestMap.get("newResultAfterAmendment");
			 String[] methodIds = (String[])requestMap.get("method_id");

			 BasicDynaBean patientDetails = VisitDetailsDAO.getPatientAndVisitInfoBean(visitId);
			 boolean isInpatient = ( patientDetails == null );
			 if(patientDetails == null)
				 patientDetails = PatientDetailsDAO.getIncomingPatientResultDetails(visitId);

			 BasicDynaBean diagGenericPref = GenericPreferencesDAO.getdiagGenericPref();
			 String sampleFlowStr =    (String)diagGenericPref.get("sampleflow_required");
			 boolean sampleFlow =false;
			 if(sampleFlowStr.equals("Y"))
				 sampleFlow = true;

			 ArrayList<TestPrescribed> tpList = new ArrayList<TestPrescribed>();
			 TestPrescribed tp = null;

			 ArrayList<TestConducted> tcList = new ArrayList<TestConducted>();
			 TestConducted tc = null;
			 FlashScope flash = FlashScope.getScope(request);
			 request.setAttribute("amendresult", ((String[])requestMap.get("amendresult"))[0]);


			 ArrayList<SampleCollection> scList = new ArrayList<SampleCollection>();
			 SampleCollection sc = null;
			 int reportIdforGeneration = 0;

			 ArrayList<TestDetails> trList = new ArrayList<TestDetails>();
			 ArrayList<TestDetails> deletedTdList = new ArrayList<TestDetails>();
			 TestDetails td = null;
			 boolean isCalculatedValue = false;
			 BasicDynaBean resultMaster = null;
			 BasicDynaBean referenceResultRangeBean = null;
			 boolean dischardReport = false;

			 /* After amending a result if we delete a newly added result then we are deleting a newly
			   added report from test_visit_reports table and storing old report id against prescribed tests. */
			 boolean deleteReport = false;
			 int newResultscount = null!=newResultAfterAmendment?newResultAfterAmendment.length:0;
			 int oldReportId = 0;
			 int deletedCount = 0;
			 for(int j=0; j<deleted.length; j++) {
					 if(deleted[j].equals("Y"))
						 deletedCount++;
			 }

			deleteReport = newResultscount != 0 && newResultscount == deletedCount;
			if(deleteReport) {
				BasicDynaBean oldReportBean = testVisitReportsDao.findByKey("revised_report_id", Integer.parseInt(printreportId));
				oldReportId = Integer.parseInt(oldReportBean.get("report_id").toString());
			}

			 if(printreportId != null && !printreportId.equals("NEW")){
				 for(int i =0;i<completed.length;i++){
					 	if(prescribedid[i].isEmpty())
					 		continue;
						 dischardReport  = completed[i].equals("RP") || completed[i].equals("RC")
						 						|| completed[i].equals("RV");
						 dischardReport &= !DiagnoDAOImpl.isAmended(Integer.parseInt(prescribedid[i]));

						if(dischardReport)
							break;
				 }
			 }

			 if(rtestId != null ){
				 for(int i=0;i<rtestId.length;i++){
					 if(conductedinreportformat[i].equals("N") && resultlabel[i].isEmpty())
						 continue;
					 if(rprescribedId[i].isEmpty())//possible incase of main row of reportformat tests,which doesnt need an entry in test_details table
						 continue;
					 BasicDynaBean testMasterBean = diagDao.findByKey("test_id", rtestId[i]);
					 HashMap resMap = new HashMap();
					 resMap.put("resultlabel_id", resultlabel_id[i]);
					 resMap.put("sample_date", dateOfSample[i]);
					 if(conductedinreportformat[i].equals("N") && resultlabel_id != null && !resultlabel_id[i].isEmpty()){
						 resultMaster  = new GenericDAO("test_results_master").
							findByKey("resultlabel_id", Integer.parseInt(resultlabel_id[i]));
						 referenceResultRangeBean =
							 ResultRangesDAO.getResultRange(resMap,patientDetails.getMap());
						 isCalculatedValue = DiagnosticsDAO.isCalculateResult(new Integer(resultlabel_id[i]));
					 }

					List thisResults = new ArrayList();
					List thisValues = new ArrayList();

					 filterResults(rf,rprescribedId[i],thisResults,thisValues,rtestId[i],methodIds);

					 td = new TestDetails();
					 td.setMrNo(mrNo);
					 td.setVisitId(visitId);
					 td.setTestId(rtestId[i]);
					 td.setPrescribedId(Integer.parseInt(rprescribedId[i]));
					 td.setConductedInFormat(conductedinreportformat[i]);
					 td.setFormatId(formatid[i]);
					 //td.setReport(reporttemplate[i]);
					 td.setResultLabel(resultlabel[i]);
					 td.setResultlabelId(resultlabel_id[i].equals("") ? 0 : Integer.parseInt(resultlabel_id[i]));
					 td.setMethodId((null != methodIds[i] && !methodIds[i].equals("")) ? Integer.parseInt(methodIds[i]) : null);
					 if(isCalculatedValue && resultMaster != null
						&& ( testDetailsStatus[i] == null || !testDetailsStatus[i].equals("A")) && 
							result_expr_hidden[i] != null && !result_expr_hidden[i].equals("") && result_expr_hidden[i].equals("Y")){
								String resultValue =
									ResultExpressionProcessor.processResultExpressionForLAB(
									thisResults,thisValues,
									resultMaster.get("expr_4_calc_result").toString());
							 	td.setResultValue(resultValue);
							 	td.setResultExpressionCheck(result_expr_hidden[i]);
							 	resultvalue[i] = resultValue;
							 	thisResults = new ArrayList();
							 	thisValues = new ArrayList();
							 	filterResults(rf,rprescribedId[i],thisResults,thisValues,rtestId[i],methodIds);
						
				 	 } else {
				 		td.setResultValue(resultvalue[i]);
				 		td.setResultExpressionCheck(result_expr_hidden[i]);
				 	 }
					
					 td.setUnits(units[i]);
					 td.setReferenceRange(referenceRanges[i]);
					 td.setRemarks(remarks[i]);
					 if(referenceResultRangeBean != null && !resultvalue[i].equals("") && isCalculatedValue && !testDetailsStatus[i].equals("A")){
						 td.setWithInNormal(lDao.checkSeviarity(referenceResultRangeBean,
								 new BigDecimal(td.getResultValue())));
					 }else{
						 td.setWithInNormal(withinNormal[i]);
					 }
					 td.setUserName(userName);
					 if(resultMaster != null){
						 td.setCodeType((String)resultMaster.get("code_type"));
						 td.setResultCode((String)resultMaster.get("result_code"));
					 }

					 //disclaimer

					 td.setResultDisclaimer(resultDisclaimer[i]);
					 String validationExp = (String)testMasterBean.get("results_validation");
					 String validResults =  null;
					 if(validationExp != null && !validationExp.isEmpty()) {
						 validResults =  ResultExpressionProcessor.processResultExpressionForLAB(
								 thisResults,thisValues,
								 ((String)testMasterBean.get("results_validation"))).trim();
					 }
					 if(validationExp != null && validResults != null && !validResults.isEmpty()){
						 flash.put("error",validResults);
						 for(int j=0;j<testid.length;j++){
							 if(testid[j].equals(rtestId[i])) {
								 completed[j] = "N";
							 	 break;
							 }
						 }
					 }else{
						 //which may be set by result calculator not by resultvalidator
						 flash.remove("error");
					 }

					 td.setTestDetailsId(!testDetailsid[i].isEmpty() ?Integer.parseInt(testDetailsid[i]) : 0);
					 td.setRevisedTestDetailsId(!revisedTestDetailsId[i].isEmpty()?Integer.parseInt(revisedTestDetailsId[i]):0);
					 td.setOrginalTestDetailsId(!originalTestDetailsId[i].isEmpty()?Integer.parseInt(originalTestDetailsId[i]):0);
					 td.setAmendReason(amenndmentreason[i]);
					 td.setTestDetailStatus(testDetailsStatus[i]);

					 if(deleted[i].equals("Y"))
						 deletedTdList.add(td);


					 trList.add(td);
				 }
			 }

			 TestVisitReports tvr = new TestVisitReports();
			 Integer pheader_template_id = printTemplateDAO.getPatientHeaderTemplateId(
					 category.equals("DEP_LAB") ? PrintTemplate.Lab : PrintTemplate.Rad);
			 if(!deleteReport && (printreportId.equals("NEW") || dischardReport)){
				 reportIdforGeneration = DiagnosticsDAO.getNextReportId();
				 tvr.setCategory(category);
				 tvr.setReportId(reportIdforGeneration);
				 tvr.setReportName("NEW");
				 tvr.setVisitId(visitId);
				 tvr.setUserName(userName);
				 tvr.setReportMode("P");
				 tvr.setPheader_template_id(pheader_template_id);
				 tvr.setReportState("A");

			 }else{
				 tvr.setReportId(Integer.parseInt(printreportId));//used to update report_results_severity_status
				 tvr.setReportName("OLD");
				 reportIdforGeneration = Integer.parseInt(printreportId);
			 }

			 int dischardedReport = 0;
			 if(dischardReport)
				 dischardedReport =  Integer.parseInt(printreportId);


			 if(testid !=null){
				 for(int i=0;i<testid.length;i++){
					 if(prescribedid[i].equals(""))
						 continue;
					 BasicDynaBean testMasterBean = diagDao.findByKey("test_id", testid[i]);

					 if(testMasterBean.get("sample_needed").equals("y")){
						 sc  = new SampleCollection();
						 sc.setVisitId(visitId);
						 sc.setMrNo(mrNo);
						 if( specimenCondition != null &&  specimenCondition[i] != null )
							 sc.setSpecimenCondition(specimenCondition[i]);
						 else
							 sc.setSpecimenCondition(null);
						 sc.setSampleNo(sampleNumber[i]);
						 sc.setTestId(testid[i]);
						 sc.setSampleStatus("C");
						 scList.add(sc);
					 }

					 //tests_prescribed
					 tp = new TestPrescribed();
					 tp.setMrNo(mrNo);
					 tp.setVisitId(visitId);
					 tp.setUserName(userName);
					 tp.setTestconductedFlag(completed[i]);
					 tp.setPrescribedId(Integer.parseInt(prescribedid[i]));
					 tp.setTestId(testid[i]);
					 tp.setReportId(reportIdforGeneration);
					 tp.setLabno(labNo[i]);
					 tpList.add(tp);


					 //tests_conducted
					 tc = new TestConducted();
					 tc.setMrNo(mrNo);
					 tc.setVisitId(visitId);
					 tc.setTestId(testid[i]);
					 tc.setConductedDate(DateUtil.parseTimestamp(dateOfInvestigation[i], timeOfInvestigation[i]));
					 tc.setConductedBy(doctor[i]);
					 tc.setIssuedBy(doctor[i]);
					 tc.setDesignation(null);
					 tc.setStatisFactoryFlag("Y");
					 tc.setPrescribedId(Integer.parseInt(prescribedid[i]));
					 tc.setRemarks(testRemarks[i]);
					 tc.setUserName(userName);
					 tc.setTestConducted(completed[i]);
					 tc.setReportId(reportIdforGeneration);
					 tc.setTechnician(technician[i]);
					 if(completed[i].equals("C") || completed[i].equals("RC"))
						 tc.setCompletedBy(userName);
					 if (completed[i].equals("V") || completed[i].equals("RV")) {
						 tc.setValidatedBy(userName);
						 tc.setValidatedDate(new java.sql.Date(new java.util.Date().getTime()));
						 String technicianStr = technician[i].equals("") ? userName : technician[i];
						 tc.setTechnician(technicianStr);
						 
						 BasicDynaBean testcondBean = testsConductedDao.findByKey("prescribed_id",Integer.parseInt(prescribedid[i]));
						 if(null == testcondBean || null == testcondBean.get("completed_by") || testcondBean.get("completed_by").equals(""))
							 tc.setCompletedBy(userName);
						 else
							 tc.setCompletedBy((String)testcondBean.get("completed_by"));
					 }

					 tcList.add(tc);
				 }
			}

			 //Histopathology tests conduction
			 List<BasicDynaBean> impressionsList = new ArrayList<BasicDynaBean>();
			 if(histoPrescribedId != null){
				 BasicDynaBean impressionBean = null;
				 GenericDAO impressionDAO = new GenericDAO("test_histopathology_results");
				 List errors = new ArrayList();
				 for(int i =0 ;i<histoPrescribedId.length;i++){
					 impressionBean = impressionDAO.getBean();
					 ConversionUtils.copyIndexToDynaBean(requestMap, i, impressionBean, errors);
					 if (histoImpressionId != null && !histoImpressionId[i].equals("")) {
						 impressionBean.set("impression_id", Integer.parseInt(histoImpressionId[i]));
					 } else {
						 impressionBean.set("impression_id", null);
					 }

					 impressionBean.set("prescribed_id", Integer.parseInt(histoPrescribedId[i]));
					 impressionsList.add(impressionBean);
				 }
			 }

			 //Microbiology test conduction
			 List<BasicDynaBean> microList = new ArrayList<BasicDynaBean>();
			 GenericDAO microDAO = new GenericDAO("test_microbiology_results");
			 BasicDynaBean microBean = null;
			 if(nogrowthTemplateId != null){

				 List errors = new ArrayList();
				 for(int i =0 ;i<nogrowthTemplateId.length;i++){
					 microBean = microDAO.getBean();
					 if(growthExists != null && growthExists[i] != null)
						 microBean.set("growth_exists", !growthExists[i].equals("N"));
					 ConversionUtils.copyIndexToDynaBean(requestMap, i, microBean, errors);
					 microBean.set("prescribed_id", Integer.parseInt(microPrescribedId[i]));

					 if(microDAO.findByKey("prescribed_id", Integer.parseInt(microPrescribedId[i])) == null)
						 microBean.set("test_micro_id",microDAO.getNextSequence());

					 microList.add(microBean);

				 }
			 }

			 GenericDAO  microOrgGrpDAO = new GenericDAO("test_micro_org_group_details");
			 String[] org_prescribed_id = (String[])requestMap.get("org_prescribed_id");
			 String[] antibioicsItem= (String[])requestMap.get("antibioticRow");
			 BasicDynaBean microOrgGrpBean = null;

			 List errors = new ArrayList();
			 GenericDAO antiBioticDAO = new GenericDAO("test_micro_antibiotic_details");
			 BasicDynaBean antiBioticBean = null;

			 List<BasicDynaBean> microOrgGrpList = new ArrayList<BasicDynaBean>();
			 List<BasicDynaBean> microAbstAntibioticList = new ArrayList<BasicDynaBean>();

			 if(org_prescribed_id != null){
				 int testOrgGrpId = 0;
				 for(int i =0;i<org_prescribed_id.length-1;i++){
					 microOrgGrpBean = microOrgGrpDAO.getBean();
					 antiBioticBean = antiBioticDAO.getBean();
					 if(antibioicsItem[i].equals("N")){
						 ConversionUtils.copyIndexToDynaBean(requestMap, i, microOrgGrpBean, errors);
						 testOrgGrpId = 0;
					 }

					 microBean = findInList(microList,"prescribed_id", Integer.parseInt(org_prescribed_id[i]));

					 if(microOrgGrpBean.get("test_org_group_id") == null && antibioicsItem[i].equals("N")){
						 testOrgGrpId = microOrgGrpDAO.getNextSequence();
						 microOrgGrpBean.set("test_org_group_id", testOrgGrpId);
						 microOrgGrpBean.set("test_micro_id", (Integer)microBean.get("test_micro_id"));
					 }

					 if(antibioicsItem[i].equals("N"))
						 microOrgGrpList.add(microOrgGrpBean);

					 ConversionUtils.copyIndexToDynaBean(requestMap, i, antiBioticBean, errors);

					 if( testOrgGrpId != 0 )
						 antiBioticBean.set("test_org_group_id", testOrgGrpId);

					 microAbstAntibioticList.add(antiBioticBean);
				 }
			 }


			 //cytologyTestConduction
			 List<BasicDynaBean> cytoList = new ArrayList<BasicDynaBean>();
			 if(cPrecribedId != null){
				 BasicDynaBean cytoBean = null;
				 GenericDAO cytoDAO = new GenericDAO("test_cytology_results");
				 errors = new ArrayList();
				 for(int i =0 ;i<cPrecribedId.length;i++){
					 cytoBean = cytoDAO.getBean();
					 ConversionUtils.copyIndexToDynaBean(requestMap, i, cytoBean, errors);
					 if (cytoImpressionId != null && !cytoImpressionId[i].equals("")) {
						 cytoBean.set("impression_id", Integer.parseInt(cytoImpressionId[i]));
					 } else {
						 cytoBean.set("impression_id", null);
					 }
					 cytoBean.set("microscopic_details", cyto_microscopic_details[i]);
					 cytoBean.set("prescribed_id", Integer.parseInt(cPrecribedId[i]));
					 cytoList.add(cytoBean);

				 }
			 }
			 if(deleteReport && oldReportId!=0) reportIdforGeneration = oldReportId;
			
			 ActionRedirect redirect = new ActionRedirect("editresults.do?_method=getBatchConductionScreen");
			 redirect.addParameter("reportId", reportIdforGeneration);
			 redirect.addParameter("category", category);
			 redirect.addParameter("visitid", visitId);
			 redirect.addParameter("prescId", requestMap.get("prescId"));
			 request.setAttribute("selectedPresList",request.getAttribute("selectedPresList"));
			 redirect.addParameter("amendresult", ((String[])requestMap.get("amendresult"))[0]);
			 redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

			 int centerId = (Integer)request.getSession().getAttribute("centerId");
			 boolean status = false;
			 Connection con = null;
			 try {
				 con = DataBaseUtil.getConnection();
				 
				 status = LaboratoryBO.insertLaboratoryTestDetails(con, tpList, tcList,
						 trList, scList,  category,tvr,userName,pref,
						 impressionsList,microList,cytoList,
						 microOrgGrpList,microAbstAntibioticList,dischardedReport,deletedTdList,centerId,
						 deletedNewTestDetailsId,deleteReport,flash);
				 
				 	StringBuilder errorValues = LaboratoryBO.getImprobableResultValues(con, reportIdforGeneration);
				 	if (errorValues.length() > 0) {
						 flash.info(errorValues.toString());
				 	}
				 if (isUserOperatingOnMultiTab(rf)) {
					 //incase of central lab we dont get mrno as parameter, so get it from the incoming sample registration table
					 status = false;
					 String MRNO = mrNo;
					 ActionRedirect SearchRedirect = null;
					 if (null == MRNO || MRNO.equals("")) {
						 BasicDynaBean incomingRegBean = incomingSampleRegistrationDAO.findByKey("incoming_visit_id", visitId);
						 MRNO = (incomingRegBean != null && incomingRegBean.get("mr_no") != null) ? incomingRegBean.get("mr_no").toString() : null;
					 }
					 if (category.equals("DEP_LAB"))
						 SearchRedirect = new ActionRedirect("/Laboratory/schedules.do?_method=getScheduleList");
					 else 
						 SearchRedirect = new ActionRedirect("/Radiology/schedules.do?_method=getScheduleList");
					 FlashScope flashScope = FlashScope.getScope(request);
					 SearchRedirect.addParameter(FlashScope.FLASH_KEY, flashScope.key());
					 flashScope.info("Test Details already exist.");
					 
					 SearchRedirect.addParameter("category", category);
					 SearchRedirect.addParameter("mr_no", MRNO);		 
					 
					 return SearchRedirect;
				 }
				 
			 } finally {
				 DataBaseUtil.commitClose(con, status);		 
			 }

			 if(status ){
				status = DiagnosticsDAO.setReportContent(reportIdforGeneration, visitId, userName,
						false, pheader_template_id);
				if(saveandprint.equals("Y")){

					 String printURL = request.getContextPath();
					 printURL = printURL+"/pages/DiagnosticModule/DiagReportPrint.do?_method=printReport";
					 printURL = printURL+"&reportId="+reportIdforGeneration;
					 printURL = printURL+"&printerId="+request.getParameter("printerId");

					 List<String> printURLs = new ArrayList<String>();
					 printURLs.add(printURL);

					 session.setAttribute("printURLs", printURLs);
				}
			 }else{
				 flash.put("error","Failed to save test details.");
			 }

			 return redirect;
	}

	public ActionForward saveTemplateReport(ActionMapping m, ActionForm form,
			HttpServletRequest req, HttpServletResponse res) throws Exception {

		String presId = req.getParameter("prescribedid");
		String reportContent = req.getParameter("templateContent");
		String formatId = req.getParameter("formatid");
		String testDetailsId = req.getParameter("testDetailsId");
		String testId = req.getParameter("testId");
		
		LaboratoryForm lForm = new LaboratoryForm();
		lForm.setPrescribedid(new String[] {presId});
		lForm.setRevisionNumber(new String[] {req.getParameter("revisionNumber")});
				
		if(testDetailsId.isEmpty())
			testDetailsId = "0";
		List newTestDetailsId = new ArrayList();
		Connection con = null;
		boolean success = false;
		
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			success = LaboratoryBO.saveTemplateReport(con, Integer.parseInt(presId), formatId,
					reportContent,Integer.parseInt(testDetailsId),newTestDetailsId,testId);
			
			if (isUserOperatingOnMultiTab(lForm)) {
				success = false;
				FlashScope flash = FlashScope.getScope(req);
				ActionRedirect redirect = new ActionRedirect(m.findForward("templateEditorRedirect"));
				redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
				redirect.addParameter("prescribedid", presId);
				redirect.addParameter("formatid", formatId);
				redirect.addParameter("status", true);
				return redirect;
			}
		} finally {
			DataBaseUtil.commitClose(con, success);
		}

		FlashScope flash = FlashScope.getScope(req);
		flash.put("msg", "Report updated successfully.");
		ActionRedirect redirect = new ActionRedirect(m.findForward("templateEditorRedirect"));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		redirect.addParameter("prescribedid", presId);
		redirect.addParameter("formatid", formatId);
		redirect.addParameter("newtestDetailsId",newTestDetailsId.size() > 0 ? newTestDetailsId.get(0):null);
		redirect.addParameter("status", success);

		return redirect;
	}

	private void getVisitTestDetails(HttpServletRequest request, BasicDynaBean pd) throws Exception{
		String visitId = request.getParameter("visitid");
		Map map = request.getParameterMap();
		String presId[] = (String[])map.get("prescId");
		String presList = "";
		int centerId = (Integer) request.getSession(false).getAttribute("centerId");
		JSONSerializer js = new JSONSerializer().exclude("class");
		String selectedPresList[] = (String[])request.getAttribute("selectedPresList");

		if(presId !=null && selectedPresList ==null ){
			for(int i=0;i<presId.length;i++){
				presList += presId[i] + ",";
			}
			presList = presList.substring(0, presList.length()-1);
			request.setAttribute("printreportId",
					request.getParameter("signedOffReport") == null
						? "NEW": request.getParameter("signedOffReport"));
		}else{
			if(selectedPresList!=null){
				for(int i=0;i<selectedPresList.length;i++){
					presList += selectedPresList[i] + ",";
				}
			}
			if(presList.length()!=0)presList = presList.substring(0, presList.length()-1);
		}

		LaboratoryBO bo = new LaboratoryBO();
		String category = request.getParameter("category");

		if(presList.length()!=0 ){
			List<TestandResults> testsList = bo.getTestsandResults(visitId, category,presList);
			List<TestandResults> valuesTests = new ArrayList<TestandResults>();
			List<TestandResults> templateTests = new ArrayList<TestandResults>();
			List<TestandResults> histoTests = new ArrayList<TestandResults>();
			List<TestandResults> microTests = new ArrayList<TestandResults>();
			List<TestandResults> cytoTests = new ArrayList<TestandResults>();
			Map deptUsersMap = new HashMap();
			List<Map<String, Object>> testsListMap = new ArrayList<>();
			for(int i=0;i<testsList.size();i++){
				List<BasicDynaBean> roleUsers = null;
				Test test = ((TestandResults)testsList.get(i)).getTest();
				testsListMap.add(testsList.get(i).toMap());
				String[] conductingRoleIds = test.getConductingRoleIds();
				if(conductingRoleIds != null)
					 roleUsers = UserDAO.getAllHospitalSpecificUsers(conductingRoleIds,test.getDdeptId());
				else
					roleUsers = UserDAO.getLabTechnician(centerId, test.getDdeptId());
					//roleUsers = new GenericDAO("u_user").findAllByKey("lab_dept_id", test.getDdeptId());
				deptUsersMap.put(test.getDdeptId(), ConversionUtils.copyListDynaBeansToMap(roleUsers));
				if(test.getReportGroup().equals("V")){
					valuesTests.add(testsList.get(i));
				}
				else if(test.getReportGroup().equals("T"))
					templateTests.add(testsList.get(i));
				else if(test.getReportGroup().equals("H"))
					histoTests.add(testsList.get(i));
				else if(test.getReportGroup().equals("M"))
					microTests.add(testsList.get(i));
				else if(test.getReportGroup().equals("C"))
					cytoTests.add(testsList.get(i));
			}

			request.setAttribute("deptUsersMap", js.deepSerialize(deptUsersMap));
			request.setAttribute("testandresults",testsList );
			request.setAttribute("testandresultsJSON", js.serialize(testsListMap));
			request.setAttribute("valueTests", valuesTests);
			request.setAttribute("templateTests", templateTests);
			request.setAttribute("histoTests", histoTests);
			request.setAttribute("microTests", microTests);
			request.setAttribute("cytoTests", cytoTests);
			DiagnosticReagentMasterDAO dao = new DiagnosticReagentMasterDAO();
			List<String>  storeExist = new ArrayList<String>();
			List<String> ReagentExist = new ArrayList<String>();
			Map<String,List<BasicDynaBean>> statesMap = new HashMap<String, List<BasicDynaBean>>();
            boolean allCompleteChk = true;
            boolean allvalidateChk = true;

			for(int i=0; i<testsList.size(); i++){

				boolean completeChk = false;
				boolean validChk = false;
				TestandResults tr =(TestandResults) testsList.get(i);
				Test td = tr.getTest();
				String testId = td.getTestId();
				String prescribeID = tr.getPrescribedId();
				ArrayList bean = (ArrayList)dao.findTestBykey(testId);
				request.setAttribute("bean", bean);

				if(bean.size()==0) {
					tr.setreagentsexist(false);
					ReagentExist.add("false");
					storeExist.add("false");
				} else {
					tr.setreagentsexist(true);
					ReagentExist.add("true");
					Boolean exist = diagnosticDepartmentMasterDao.isDiagDeptStoreExist(td.getDdeptId(), centerId);
					storeExist.add(exist == true?"true":"false");
				}
				if(category.equals("DEP_RAD")){
					List<BasicDynaBean> conductionStates = null;
					String state_category = "N";
	                String condStatus = tr.getCondctionStatus();
	                String amendresult = request.getParameter("amendresult");
	                BasicDynaBean conductRow = new GenericDAO("diag_states_master").findByKey("value" , condStatus);
	                String condCategory = String.valueOf(conductRow.get("category"));
		                if(condStatus.equals("S"))
		                	state_category = "O";
		                if(null != amendresult && !amendresult.equals("")){
			                if(amendresult.equals("Y"))
			                	state_category = "R";
		                	}
		                else if(condCategory.equals("R")){
		                	state_category = "R";
		                }
	                String levelobj =  String.valueOf(conductRow.get("level"));
	                int level = Integer.parseInt(levelobj);
	                int maxlevel = LaboratoryDAO.getMaxLevel();
	                int stateOrder = Integer.parseInt(String.valueOf(conductRow.get("state_order")));
	                int maxstateOrder = LaboratoryDAO.getMaxStateOrder(level);
	                int nextLevel = level + 1;
	                if (nextLevel >= maxlevel)
	                	nextLevel = maxlevel;

	                if(stateOrder == maxstateOrder){
	            	    conductionStates = LaboratoryDAO.getconductionStatesNext(level, nextLevel, state_category);
	                    }
	                else{
	                    conductionStates = LaboratoryDAO.getconductionStates(level, state_category);
	                    }
	                checkValidateRights(request,conductionStates);
	                statesMap.put(prescribeID, conductionStates);
	                for(BasicDynaBean states : conductionStates){
	                    if(states.get("value").equals("C") || states.get("value").equals("RC")){
	                    	completeChk = true;
	                    }
	                }
	                for(BasicDynaBean states : conductionStates){
	                    if(states.get("value").equals("V") || states.get("value").equals("RV")){
	                    	validChk = true;
	                    }
	                }
	            request.setAttribute("conductionstates",ConversionUtils.copyListDynaBeansToMap(conductionStates));
				}
                allvalidateChk = allvalidateChk && validChk;
				allCompleteChk = allCompleteChk && completeChk ;
				request.setAttribute("storeExist", storeExist);
				request.setAttribute("ReagentExist", ReagentExist);
				request.setAttribute("reagents", js.serialize(ConversionUtils.copyListDynaBeansToMap(bean)));
				request.setAttribute("impressionsJSON",js.serialize(
						ConversionUtils.copyListDynaBeansToMap(
								new GenericDAO("histo_impression_master").listAll(null,"status","A"))));
				request.setAttribute("noGrowthTemplatesJSON",js.serialize(
						ConversionUtils.copyListDynaBeansToMap(
								new GenericDAO("micro_nogrowth_template_master").listAll(null,"status","A"))));
				request.setAttribute("growthTemplatesJSON",js.serialize(
						ConversionUtils.copyListDynaBeansToMap(
								new GenericDAO("micro_growth_template_master").listAll(null,"status","A"))));
				request.setAttribute("microAbstPanelJSON",js.serialize(
						ConversionUtils.copyListDynaBeansToMap(
								MicroAbstPanelMasterDao.getAllABSTpanels())));
				request.setAttribute("microAbstAntiBioticJSON",js.serialize(
						ConversionUtils.copyListDynaBeansToMap(
								MicroAbstPanelMasterDao.getAllABSTAndAntibiotics())));
				List<BasicDynaBean > microOrganismsList =
						new GenericDAO("micro_organism_master").listAll(null,"status","A");
				request.setAttribute("microOrganism", microOrganismsList);
				request.setAttribute("microOrganismJSON",js.serialize(
						ConversionUtils.copyListDynaBeansToMap(microOrganismsList)));

			}
			request.setAttribute("stateMap", statesMap);
			request.setAttribute("allCompleteChk", allCompleteChk);
			request.setAttribute("allvalidateChk", allvalidateChk);
		}
		int patCenterId;
		if (pd !=null){
			patCenterId = (Integer) pd.get("center_id");
	    } else{
	    	BasicDynaBean incominCustBean =  OhSampleRegistrationDAO.getIncomingCustomer(request.getParameter("visitid"));
	    	patCenterId = (Integer) incominCustBean.get("center_id");
	    }
		List l2 = bo.getLabtechnicions(category,patCenterId);
		request.setAttribute("doctors", l2);

		List reportList = DiagnosticsDAO.getReportList(visitId, category, true);
		request.setAttribute("reportList", reportList);

		List reportListInprescription = DiagnosticsDAO.reportListInprescription(visitId, category);
		request.setAttribute("reportListInprescription", reportListInprescription);
		BasicDynaBean diagGenericPref = GenericPreferencesDAO.getdiagGenericPref();
		request.setAttribute("diagGenericPref",diagGenericPref);
	}


	private void checkValidateRights(HttpServletRequest request, List<BasicDynaBean> conductionStates) {
		String validateRights = null;
		List<BasicDynaBean> deleteCandidates = new ArrayList<BasicDynaBean>();
		int roleId = (Integer)request.getSession().getAttribute("roleId");
        if((roleId != 1) && (roleId != 2)){
        	validateRights = (String)((Map)request.getSession(false).getAttribute("actionRightsMap")).get("validate_test_results");
        }else{
        	validateRights = "A";
        }
        for (BasicDynaBean validaterights : conductionStates) {
           if(validaterights.get("value").equals("V") || validaterights.get("value").equals("RV")){
        	   deleteCandidates.add(validaterights);

           }
         }
        if(!validateRights.equals("A")){
        	for (BasicDynaBean removeFromList : deleteCandidates) {
           conductionStates.remove(removeFromList);
        	}
        }

	}

	public ActionForward getTemplateEditor(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws Exception {

		String formatid = request.getParameter("formatid");
		String prescribedid = request.getParameter("prescribedid");
		String testDetailsId = request.getParameter("testDetailsId");
		String testId = request.getParameter("testId");
		JSONSerializer js = new JSONSerializer().exclude("class");
		BasicDynaBean testPrescBean = testPrescDao.
				findByKey("prescribed_id",Integer.parseInt(prescribedid));

		LaboratoryBO bo = new LaboratoryBO();
		String templ = bo.getTemplateContent(formatid, prescribedid,testDetailsId);
		request.setAttribute("templateContent", templ);

		request.setAttribute("prescribedid", prescribedid);
		request.setAttribute("testDetailsId", testDetailsId);
		request.setAttribute("testId", testId);
		request.setAttribute("testPrescDetails",testPrescBean );
		List getImagesListForReport = LaboratoryDAO.getImageDetails( Integer.parseInt(prescribedid) );
		request.setAttribute("imagesList", getImagesListForReport);
		request.setAttribute("imagesListjson", js.deepSerialize(
				ConversionUtils.copyListDynaBeansToMap(
						LaboratoryDAO.getImageDetails( Integer.parseInt(prescribedid)))) );


		BasicDynaBean printPrefs = PrintConfigurationsDAO.getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_DIAG);
		request.setAttribute("prefs",printPrefs);
		request.setAttribute("isAddendum", false);
				
		return mapping.findForward("templateEditor");
	}

	public ActionForward getDiagConfirmList(ActionMapping mapping, ActionForm form,HttpServletRequest request,
			HttpServletResponse response) throws IOException, ServletException, Exception{

		LaboratoryBO bo = new LaboratoryBO();
		String mrno = request.getParameter("mrno");
		String department = request.getParameter("department");
		String testid = request.getParameter("testname");
		String sortReverse = request.getParameter("sortReverse");
		String sortOrder = request.getParameter("sortOrder");
		ArrayList list = (ArrayList) bo.getTestConfirmList( mrno, department, testid,sortOrder,sortReverse);
		setPagenationAttributes(request, list, "DEP_LAB");
		return mapping.findForward("getConfirmScreen");
	}

	public void setPagenationAttributes(HttpServletRequest request, List list, String category) throws Exception{

		int offset = 0;
		int index = 0;
		String sIndex = request.getParameter("index");

		if(sIndex != null){
			index = Integer.parseInt(sIndex);
			index--;
			offset = index * 15;
		}
		int noOfTests = list.size();
		ArrayList recList2 = new ArrayList();
		if (noOfTests > 0){
			for (int i = offset, j = 0; i < (offset + 15) && i < noOfTests; i++, j++){
				recList2.add(j, list.get(i));
			}
		}
		int pages = noOfTests / 15;
		int rem = noOfTests % 15;
		if(rem != 0) pages++;

		LaboratoryBO bo = new LaboratoryBO();

		request.setAttribute("mrno", request.getParameter("mrno"));
		request.setAttribute("department", request.getParameter("department"));
		request.setAttribute("testname", request.getParameter("testname"));
		request.setAttribute("DepartmentList",DiagnosticDepartmentMasterDAO.getDepts(category));
		request.setAttribute("allTests",bo.getTestNames(category));

		request.setAttribute("testList", recList2);
		request.setAttribute("noofpages", pages);
		request.setAttribute("index", index+1);
	}


	@IgnoreConfidentialFilters
	public ActionForward backToDashboard(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws Exception {
			String category = request.getParameter("category");
			if(category.equals("DEP_LAB")){
				return mapping.findForward("getLaboratoryDashboard");
			}else if(category.equals("DEP_RAD")){
				return mapping.findForward("getRadiologyDashboard");
			}
		return null;

	}

	public ActionForward getEditReport(ActionMapping mapping, ActionForm form,HttpServletRequest request,
			HttpServletResponse response)
	 throws IOException, ServletException,  Exception {

	 	String reportId = request.getParameter("reportId");
	 	String visitId = request.getParameter("visitid");
	 	setEditReportAttributes(request,Integer.parseInt(reportId));
	 	BasicDynaBean pd =  VisitDetailsDAO.getPatientVisitDetailsBean(visitId);

		if(pd != null)
			request.setAttribute("patientvisitdetails",pd);

	 	request.setAttribute("print",
	 			request.getParameter("print") == null ? false : request.getParameter("print") );

		return mapping.findForward("editReport");
	}


	 private void setEditReportAttributes(HttpServletRequest request,int reportId)throws Exception{

		 	ArrayList<Hashtable<String,String>> list = diagnosticsDao.getReport(reportId);

		 	Iterator<Hashtable<String,String>> it = list.iterator();
		 	String reportContent = null;
		 	String reportName = null;
		 	String reportDate = null;
		 	while(it.hasNext()){
		 		Hashtable<String,String> ht = it.next();
		 		reportContent = ht.get("REPORT_DATA");
		 		reportName = ht.get("REPORT_NAME");
		 		reportDate = ht.get("REPORT_DATE");
		 	}

		 	//Need to move to another functiont
			int printerId = 0;
			String printerIdStr = request.getParameter("printerId");
			if ( (printerIdStr != null) && !printerIdStr.equals("") ) {
				printerId = Integer.parseInt(printerIdStr);
			}
		    BasicDynaBean printPrefs =
				PrintConfigurationsDAO.getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_DIAG,printerId);
		 	request.setAttribute("prefs",printPrefs);

		 	request.setAttribute("reportContent",reportContent);
		 	request.setAttribute("reportName", reportName);
		 	request.setAttribute("reportId", reportId);
		 	request.setAttribute("reportDate", reportDate);
			request.setAttribute("printerId", printerId);

	 }


	 public ActionForward editReport(ActionMapping mapping, ActionForm form,HttpServletRequest request,
				HttpServletResponse response) throws IOException, ServletException, Exception {

		 	LaboratoryForm dForm = (LaboratoryForm)form;
		 	TestVisitReports tvr = new TestVisitReports();
		 	tvr.setReportName(dForm.getReportName());
		 	tvr.setReportData(dForm.getReportContent());
		 	tvr.setReportId(Integer.parseInt(dForm.getReportid()));
		 	tvr.setReportData(dForm.getReportContent());

		 	boolean status = LaboratoryBO.editReport(tvr);

		 	String reportId =  dForm.getReportid();
		 	request.setAttribute("print", "true");
		 	setEditReportAttributes(request,Integer.parseInt(reportId));

		 return mapping.findForward("editReport");
		}

	 public ActionForward getReport(ActionMapping mapping, ActionForm form,HttpServletRequest request,
				HttpServletResponse response)throws IOException, ServletException,Exception{

		 	 String reportId = request.getParameter("reportId");
		 	 ArrayList<Hashtable<String,String>> list = diagnosticsDao.getReport(Integer.parseInt(reportId));
		 	 Iterator<Hashtable<String,String>> it = list.iterator();
			 String reportContent = null;
			 String reportName = null;
			 while(it.hasNext()){
			 		Hashtable<String,String> ht = it.next();
			 		reportContent = ht.get("REPORT_DATA");
			 		reportName = ht.get("REPORT_NAME");
			 }


		 	if(reportContent ==null){
		 		reportContent = "Report is Not Availble";
		 	}

			response.setContentType("text/xml");
			response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
			response.getWriter().write(reportContent);
			response.flushBuffer();


		   return null;
	 }


	public ActionForward signOffSelectedReports(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
		throws IOException, ServletException, Exception {
		
		Map map = request.getParameterMap();
		String[] SignOffIds = (String[]) map.get("signOff");
		boolean status = true;
		Preferences pref = (Preferences) request.getSession(false).getAttribute("preferences");
		String userName = (String) request.getSession(false).getAttribute("userid");
		int centerId = (Integer) request.getSession().getAttribute("centerId");
		ArrayList reportIdsList = new ArrayList();
		ArrayList reportList = new ArrayList();
		ArrayList<TestVisitReports> tvrList = new ArrayList<TestVisitReports>();
		TestVisitReports tvr = null;
		HashMap<Integer, String> reportsMap = new HashMap<Integer, String>();
		if (SignOffIds !=null) {
			for (int j = 0; j < SignOffIds.length; j++) {
				tvr = new TestVisitReports();
				tvr.setReportId(Integer.parseInt(SignOffIds[j]));
				tvrList.add(tvr);
			}
			status=LaboratoryBO.signofReports(tvrList, pref, userName, centerId, reportsMap);
		}
		for (Map.Entry<Integer, String> reportMap :reportsMap.entrySet()) {
			reportIdsList.add(reportMap.getKey());
			reportList.add(reportMap.getValue());
		}

		// go back to the schedules list with the same old filter.
		FlashScope flash = FlashScope.getScope(request);
		ActionRedirect redirect = new ActionRedirect(request.getHeader("Referer").
				                replaceAll("&" + FlashScope.FLASH_KEY + "=[0-9A-Fa-f]+", ""));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		
		if (reportList.size() > 0) {
			flash.info("The following Reports are having Improbable High/Low values.These values are not allowed.<br/>"+reportList.toString());
		}

		if (!status) {
			flash.put("error", "Failed to Sign-Off Reports");
		} else {
			if (null != SignOffIds && SignOffIds.length > 0 &&
					MessageUtil.allowMessageNotification(request,"general_message_send")) {
				List<String> signOffList = new ArrayList<>();
				String contextPath = RequestContext.getRequest()
						.getServletContext().getRealPath("");
				
				for (int i = 0; i < SignOffIds.length; i++) {					
					if(!LaboratoryDAO.isHandoverToSponsor(Integer.parseInt(SignOffIds[i])))
						signOffList.add(SignOffIds[i]);
				}	
				signOffList.removeAll(reportList);
				if(!signOffList.isEmpty())
				{				
					String[] signOff = signOffList.toArray(new String[signOffList.size()]);
					scheduleOPIPDiagReportPHR(signOff, contextPath);
				}
				for (int i = 0; i < SignOffIds.length; i++) {
				  sendReportNotification(Integer.parseInt(SignOffIds[i]));
					List<BasicDynaBean> criticalTestDetails  = LaboratoryDAO.getCriticalTestDetails(Integer.parseInt(SignOffIds[i]));
					Map dataMap = ConversionUtils.listBeanToMapListMap(criticalTestDetails, "test_id");
					notificationforCriticalTests(dataMap, Integer.parseInt(SignOffIds[i]));
				}
			}
		}
		// triggers signoff event
        List<BasicDynaBean> testDetailsList =
              testVisitReportsService.getTestDetails(SignOffIds);
        for (BasicDynaBean testDetail : testDetailsList) {
          interfaceEventMappingService.investigationSignOff((String) testDetail.get("visit_id"),
        		  (int) testDetail.get("presc_id"), DEP_LAB.equals(testDetail.get("category")));
        }
		return redirect;
	}
	
	private void notificationforCriticalTests(Map dataMap, int reportId) throws Exception {
		boolean sentStatus = false;
		int count = 0;
		MessageManager msgManager =  new MessageManager();
		Iterator<Map.Entry<String, Object>> entries = dataMap.entrySet().iterator();
		if (dataMap.isEmpty()) 
			LaboratoryDAO.setNotificationStatus(reportId, "");
		while (entries.hasNext()) {
			Map.Entry<String, Object> entry = entries.next();
			Map<String,Object> eventData = LaboratoryBO.buildCriticalLabTestEventData(entry);
			try {
				eventData.put("recipient_mobile", eventData.get("doctor_mobile"));
				eventData.put("recipient_email", eventData.get("doctor_mail"));
				sentStatus = msgManager.processEvent("lab_critical_val", eventData, true);
				if (sentStatus)  
					count++;
				else
					LaboratoryDAO.setNotificationStatus(reportId, "N");
				if (count == 1)
					LaboratoryDAO.setNotificationStatus(reportId, "Y");
				} catch (Exception e) {
					log.error("Exception during Sending SMS/Email critical Lab test data");
				}
			}
	}
	

	/*
	 * returns a proper js script file (not JSON). This is what TinyMCE editor
	 * requires for a list of URLs that have images.
	 */
	public ActionForward getImageListJS(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
		throws IOException, ServletException, Exception {

		String prescribedIdStr = request.getParameter("prescribedId");
		if ( (prescribedIdStr == null) || prescribedIdStr.equals("") )
			return null;
		BasicDynaBean diagGenericPref = GenericPreferencesDAO.getdiagGenericPref();
		String diaImgPrf = (String)diagGenericPref.get("diag_images");
		List<String> diagImages = new ArrayList<String>(Arrays.asList(diaImgPrf.split("\\s*,\\s*")));
		int prescribedId = Integer.parseInt(prescribedIdStr);
		List imagesList = null;
		if(diagImages.contains("T"))
		imagesList = LaboratoryDAO.getImageDetails(prescribedId);

		/*
		 * construct the javascript as a string. It should look like:
		 * var tinyMCEImageList = new Array(
		 * 		// Name, URL
		 * 		["Logo 1", "/path/logo.jpg"],
		 * 		["Logo 2 Over", "/path/logo_2_over.jpg"]
		 * );
		 */
		StringBuffer buf = new StringBuffer();
		buf.append( "var tinyMCEImageList = new Array(\n");

		boolean first = true;
		if(null != imagesList){
		for (BasicDynaBean image : (List<BasicDynaBean>) imagesList) {
			String title = (String) image.get("title");
			ActionRedirect a = new ActionRedirect(mapping.findForward("viewImage"));
			a.addParameter("titleName", title);
			a.addParameter("prescribedId", prescribedId);

			if (!first)
				buf.append(",\n");

			buf.append("  ['").append(title).append("', ")
				.append("'").append(request.getContextPath()).append(a.getPath()).append("']\n");
			first = false;
		}
	}
		BasicDynaBean bean = testPrescDao.findByKey("prescribed_id", prescribedId);
		if (bean != null) {
			String mrNo = (String) bean.get("mr_no");
			if (mrNo != null && !mrNo.equals("")) {
				// add generic images (per patient as well as common) to the list of images
				PatientGeneralImageDAO patientimagedao = new PatientGeneralImageDAO();
				List<Map> genericImagesList = patientimagedao.getPatientAndGenericImages(mrNo, diagImages);
				for (Map image : genericImagesList) {
					String title = (String) image.get("image_name");
					String viewUrl = (String) image.get("viewUrl");

					if (!first)
						buf.append(",\n");

					buf.append("  ['").append(title).append("', ")
						.append("'").append(request.getContextPath()).append(viewUrl).append("']\n");
					first = false;
				}

				// add patient photo
				InputStream patientPhoto = PatientDetailsDAO.getPatientPhoto(mrNo);
				if (patientPhoto != null && patientPhoto.available() > 0) {
					StringBuilder path = new StringBuilder(request.getContextPath());;
					path.append("/Registration/GeneralRegistrationPatientPhoto.do?_method=viewPatientPhoto");
					path.append("&mrno="+mrNo);

					if (!first)
						buf.append(",\n");

					buf.append(" ['").append("patientPhoto").append("', ")
						.append("'").append(path).append("']\n");
				}
			}
		} else {
			// incoming sample: no mr_no. Make generic images alone available
			GenericImageDAO gImageDAO = new GenericImageDAO();
			List<Map> genericImagesList = gImageDAO.getGenericImageUrls();
			for (Map image : genericImagesList) {
				String title = (String) image.get("image_name");
				String viewUrl = (String) image.get("viewUrl");

				if (!first)
					buf.append(",\n");

				buf.append("  ['").append(title).append("', ")
					.append("'").append(request.getContextPath()).append(viewUrl).append("']\n");
				first = false;
			}
		}
		buf.append(");\n");

		response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
	    response.setContentType("text/javascript");
		response.getWriter().write(buf.toString());
		return null;
	}


	public ActionForward viewImage(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
		throws IOException, ServletException, Exception {

		log.debug("View Image action");
		int prescribedId = Integer.parseInt(request.getParameter("prescribedId"));
		String imageTitle = request.getParameter("titleName");

		response.setContentType("image/gif");
		response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");

		InputStream image = LaboratoryDAO.getImageStream(prescribedId, imageTitle);
		ServletOutputStream os = response.getOutputStream();

		byte[] bytes = new byte[4096];
		int len = 0;
		while ( (len = image.read(bytes)) > 0) {
			os.write(bytes, 0, len);
		}

		os.flush();
		image.close();
		return null;
	}

	private PrintPageOptions pageOptions(String printType) throws SQLException {
		BasicDynaBean  b = PrintConfigurationsDAO.getPageOptions(printType);
        return new PrintPageOptions(
			((Integer)b.get("top_margin")).intValue(),
			((Integer)b.get("bottom_margin")).intValue(),
			((Integer)b.get("right_margin")).intValue(),
			((Integer)b.get("left_margin")).intValue(),
			((Integer)b.get("page_width")).intValue(),
			(String)b.get("font_name"),
			((Integer)b.get("font_size")).intValue()
		);
	}


	public ActionForward modifyReagents(ActionMapping mapping,
			ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws IOException, ServletException,
			Exception {
		request.setAttribute("testName", request.getParameter("testName"));
		String testName = request.getParameter("testName");
		String testId = request.getParameter("testId");
		String visitId = request.getParameter("visitId");
		String category = request.getParameter("category");
		JSONSerializer js = new JSONSerializer().exclude("class");
		if (category != null) {
			request.setAttribute("category", category);
		}
		DiagnosticReagentMasterDAO dao = new DiagnosticReagentMasterDAO();
		List test_list = dao.getTestToMapReagents();
		Hashtable<String, String> table = new Hashtable<String, String>();
		table.put("TEST_ID", testId);
		table.put("TEST_NAME",testName);
		test_list.add(table);

		List diagReagentsModified  = LaboratoryDAO.getDiagReagentsUsed(Integer.parseInt(request.getParameter("prescribedId")));
		if (diagReagentsModified.size() == 0) {
			diagReagentsModified = dao.getDiagnosticReagents(testId);
		}

		BasicDynaBean pd = getPatientDetails(visitId);
		if (pd !=null) {
			request.setAttribute("patientvisitdetails", pd);
		} else {
			request.setAttribute("custmer",
					OhSampleRegistrationDAO.getIncomingCustomer(visitId));
		}

		request.setAttribute("testPrescDetails",testPrescDao.
				findByKey("prescribed_id",Integer.parseInt(request.getParameter("prescribedId"))) );
		request.setAttribute("bean", diagReagentsModified);
		request.setAttribute("reagents", js.serialize(ConversionUtils.copyListDynaBeansToMap(diagReagentsModified)));

		request.setAttribute("test_list", test_list);
		return mapping.findForward("ModifyReagents");
	}

	public ActionForward updateReagents(ActionMapping mapping,
			ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws IOException, ServletException,
			Exception {

		Connection con = null;
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			Map params = request.getParameterMap();
			String testId = ((String[])params.get("testId"))[0];
			String testName = request.getParameter("testName");
            String visitId = request.getParameter("visitId");
    		String[] prescribed_id = (String[])params.get("prescribed_id");
    		String[] reagent_usage_seq = (String[])params.get("reagent_usage_seq");
    		String[] qty = (String[]) params.get("qty");
    		String[] old_qty = (String[]) params.get("old_qty");
    		String conducted = request.getParameter("conducted");
            String category = request.getParameter("category");
            Preferences pref = (Preferences)request.getSession(false).getAttribute("preferences");
            boolean result = false;

			/*Map<String, String> columnData_service = new HashMap<String, String>();

			columnData_service.put("conducted", "P");
			if(conducted.equals("N") && reagent_usage_seq!=null) {
				result = testPrescDao.update(con,columnData_service, "prescribed_id", Integer.parseInt(prescribed_id[0])) > 0;
			}*/

			List<DynaBean> reagentsRegired = new ArrayList<DynaBean>();
			List errors = new ArrayList();

			DynaBeanBuilder builder = new DynaBeanBuilder();
			builder.add("item_id", Integer.class)
			.add("qty",BigDecimal.class)
			.add("redusing_qty",BigDecimal.class);
			DynaBean reagentsbean = builder.build();

			FlashScope flash = FlashScope.getScope(request);
            if(reagent_usage_seq!=null) {
			for(int i =0 ;i<reagent_usage_seq.length;i++){
					reagentsbean = builder.build();
					ConversionUtils.copyIndexToDynaBean(params, i, reagentsbean, errors);
					reagentsbean.set("redusing_qty", (new BigDecimal(qty[i])).subtract(new BigDecimal(old_qty[i])));
					reagentsRegired.add(reagentsbean);
			}
			result = new LaboratoryBO().saveDiagReagentUsage(con, testId, Integer.parseInt(prescribed_id[0]),
					reagent_usage_seq, reagentsRegired, pref);
            }
			if (result) {
				con.commit();
				flash.success("DiagnosticReagent details updated successfully..");
			} else {
				con.rollback();
				flash.error("Failed to update DiagnosticReagent details..");
			}
			/*Transaction Flow : Changed the sendRedirect to an ActionRedirect */
			String url = "editresults.do";
			ActionRedirect redirect = new ActionRedirect(url);
			redirect.addParameter("_method", "modifyReagents");
			redirect.addParameter("visitId", visitId);
			redirect.addParameter("testName", testName);
			redirect.addParameter("testId", testId);
			redirect.addParameter("category", category);
			redirect.addParameter("prescribedId", prescribed_id[0]);
			redirect.addParameter("conducted", conducted);
			redirect.addParameter(FlashScope.FLASH_KEY,flash.key());
			return redirect;
		} finally {
			if (con !=null) con.close();
	    }
	}

	@IgnoreConfidentialFilters
	public ActionForward unfinishedTestsList(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException,
			ParseException, Exception {
		String category = mapping.getProperty("category");
		BasicDynaBean diagGenericPref = GenericPreferencesDAO.getdiagGenericPref();
		HttpSession session = request.getSession(false);
		String userId = (String) session.getAttribute("userid");
		String userDept = diagnosticDepartmentMasterDao.getUserDepartment(userId);
		Map params = new HashMap(request.getParameterMap());
		Object[] department = (Object[]) params.get("ddept_id");
		LaboratoryBO bo = new LaboratoryBO();
		JSONSerializer js = new JSONSerializer().exclude("class");
		String deptId = "";
		if (department == null || department[0] == null ) {
			if (userDept != null && !userDept.equals(""))
				deptId = userDept;
		} else {
			deptId = (String) department[0];
		}
		params.put("ddept_id", new String[]{deptId});
		params.put("category", new String[]{category});
		params.remove("collectionCenterId");
		int centerId = (Integer) request.getSession(false).getAttribute("centerId");
		if (centerId != 0) {
			params.put("center_id", new String[]{centerId+""});
			params.put("center_id@type", new String[]{"integer"});
		}
		String expRepReadyDate = request.getParameter("_exp_rep_ready_date");
		String expRepReadyTime =request.getParameter("_exp_rep_ready_time");
		if(expRepReadyTime==null || "".equals(expRepReadyTime))
			expRepReadyTime="23:59:59";	
		String exp_rep_ready_time = new String();
		if(expRepReadyDate != null && !"".equals(expRepReadyDate)) {
			exp_rep_ready_time = expRepReadyDate+" "+expRepReadyTime;
		}
		params.remove("_exp_rep_ready_date");
		params.remove("_exp_rep_ready_time");
		params.put("exp_rep_ready_time", new String[]{exp_rep_ready_time});
		String collectionCenterId = request.getParameter("collectionCenterId");
		int userSampleCollectionCenterId = (Integer) request.getSession(false).getAttribute("sampleCollectionCenterId");
		if(null != collectionCenterId &&  !"".equals(collectionCenterId)) {
			params.put("collection_center_id", new String[]{collectionCenterId+""});
			params.put("collection_center_id@type", new String[]{"integer"});
		} else {
			if(userSampleCollectionCenterId != -1){
				params.put("collection_center_id", new String[]{userSampleCollectionCenterId+""});
				params.put("collection_center_id@type", new String[]{"integer"});
			}
		}
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

		List<BasicDynaBean> collectionCenters = sampleCollectionCentersDao.findAllByKey("center_id", centerId);
		request.setAttribute("collectionCenters", collectionCenters);
		BasicDynaBean colcenterBean = sampleCollectionCentersDao.findByKey("collection_center_id", -1);
		request.setAttribute("defautlCollectionCenter", colcenterBean.get("collection_center"));
		String origsampleSno = request.getParameter("orig_sample_no");
		String origsampleSnos = null;
		String[] origsampleSnosArray = null;
		if(origsampleSno != null && !origsampleSno.equals("")) {
			origsampleSnos = origsampleSno;
			origsampleSnosArray = origsampleSnos.split(",");
		}
		params.remove("orig_sample_no");
		Map<LISTING, Object> listingParams = ConversionUtils.getListingParameter(params);
		params.remove("screenId");
		PagedList list = LaboratoryBO.unfinishedTestsList(params, listingParams, diagGenericPref,centerId, origsampleSnosArray);
		List<BasicDynaBean> incomingHospitals = inComingHospitalDao.listAll(
				 Arrays.asList(new String[] {"hospital_id","hospital_name"}), "status", "A", "hospital_name");
		List outHouses = OutHouseMasterDAO.getAllOutSources();

		request.setAttribute("islabNoReq", (String)diagGenericPref.get("autogenerate_labno"));
		request.setAttribute("pagedList", list);
		request.setAttribute("diagGenericPref", diagGenericPref);
		request.setAttribute("inHouses", js.serialize(ConversionUtils.copyListDynaBeansToMap(incomingHospitals)));
		request.setAttribute("prescribedDoctors", js.serialize(ReferalDoctorDAO.getPrescribedDoctors()));
		request.setAttribute("outHouses", js.serialize(outHouses));
		request.setAttribute("category", category);
		request.setAttribute("module", category);
		request.setAttribute("userDept", userDept);
		request.setAttribute("genPrefs", GenericPreferencesDAO.getGenericPreferences());
		request.setAttribute("directBillingPrefs", ConversionUtils.listBeanToMapBean(
				hospDirectBillPrefsDao.listAll(),"item_type"));
		request.setAttribute("test_timestamp", diagDaoImpl.getCountFromDiagTimeStamp());
		List l2 = bo.getLabtechnicions(category, centerId);
		request.setAttribute("doctors", l2);

		ActionForward forward = new ActionForward(mapping.findForward("unfinishedtestslist").getPath());
		// when ever user uses a pagination pres_date should not append again.
		if (date_range != null && date_range.equals("week") && request.getParameter("pres_date") == null) {
			addParameter("pres_date", week_start_date, forward);
	    }
		return forward;
	}
	public ActionForward searchUnfinishedTestsList(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException,
			ParseException, Exception {
		String category = mapping.getProperty("category");
		BasicDynaBean diagGenericPref = GenericPreferencesDAO.getdiagGenericPref();
		HttpSession session = request.getSession(false);
		String userId = (String) session.getAttribute("userid");
		String userDept = diagnosticDepartmentMasterDao.getUserDepartment(userId);
		Map params = new HashMap(request.getParameterMap());
		Object[] department = (Object[]) params.get("ddept_id");
		LaboratoryBO bo = new LaboratoryBO();
		JSONSerializer js = new JSONSerializer().exclude("class");
		String deptId = "";
		if (department == null || department[0] == null ) {
			if (userDept != null && !userDept.equals(""))
				deptId = userDept;
		} else {
			deptId = (String) department[0];
		}
		params.put("ddept_id", new String[]{deptId});
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
		/*String date_range = request.getParameter("date_range");
		String week_start_date = null;
		if (date_range != null && date_range.equals("week")) {
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
	        Calendar cal = Calendar.getInstance();
	        cal.add(Calendar.DATE, -7);
	        Date openDt = cal.getTime();
	        week_start_date = dateFormat.format(openDt);
		}*/

		List<BasicDynaBean> collectionCenters = sampleCollectionCentersDao.findAllByKey("center_id", centerId);
		request.setAttribute("collectionCenters", collectionCenters);
		BasicDynaBean colcenterBean = sampleCollectionCentersDao.findByKey("collection_center_id", -1);
		request.setAttribute("defautlCollectionCenter", colcenterBean.get("collection_center"));
		String origsampleSno = request.getParameter("orig_sample_no");
		String origsampleSnos = null;
		String[] origsampleSnosArray = null;
		if(origsampleSno != null && !origsampleSno.equals("")) {
			origsampleSnos = origsampleSno;
			origsampleSnosArray = origsampleSnos.split(",");
		}
		params.remove("orig_sample_no");
		List<BasicDynaBean> incomingHospitals = inComingHospitalDao.listAll(
				 Arrays.asList(new String[] {"hospital_id","hospital_name"}), "status", "A", "hospital_name");
		List outHouses = OutHouseMasterDAO.getAllOutSources();

		request.setAttribute("islabNoReq", (String)diagGenericPref.get("autogenerate_labno"));
		request.setAttribute("diagGenericPref", diagGenericPref);
		request.setAttribute("inHouses", js.serialize(ConversionUtils.copyListDynaBeansToMap(incomingHospitals)));
		request.setAttribute("outHouses", js.serialize(outHouses));
		request.setAttribute("prescribedDoctors", js.serialize(ReferalDoctorDAO.getPrescribedDoctors()));
		request.setAttribute("category", category);
		request.setAttribute("module", category);
		request.setAttribute("screenType", mapping.getProperty("screen_type"));
		request.setAttribute("userDept", userDept);
		request.setAttribute("genPrefs", GenericPreferencesDAO.getGenericPreferences());
		request.setAttribute("directBillingPrefs", ConversionUtils.listBeanToMapBean(
				hospDirectBillPrefsDao.listAll(),"item_type"));
		request.setAttribute("test_timestamp", diagDaoImpl.getCountFromDiagTimeStamp());
		List l2 = bo.getLabtechnicions(category, centerId);
		request.setAttribute("doctors", l2);

		ActionForward forward = new ActionForward(mapping.findForward("unfinishedtestslist").getPath());
		/*// when ever user uses a pagination pres_date should not append again.
		if (date_range != null && date_range.equals("week") && request.getParameter("pres_date") == null) {
			addParameter("pres_date", week_start_date, forward);
	    }*/
		return forward;
	}
	
	@IgnoreConfidentialFilters
	public ActionForward getPreviousResults(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws SQLException, IOException, ParseException {
		String resultlabel = request.getParameter("resultlabel");
		String mrNo = request.getParameter("mr_no");
		String testDateTime = request.getParameter("testDateTime");
		String methodId = request.getParameter("method_id");
		String resultLblId = request.getParameter("resultLblId");
		JSONSerializer js = new JSONSerializer().exclude("class");
		Map<LISTING, Object> listingParams = ConversionUtils.getListingParameter(request.getParameterMap());
		PagedList pagedList = lDao.searchPreviousResults(mrNo, testDateTime, resultlabel, methodId, resultLblId, listingParams);

		response.setContentType("text/plain");
		response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");

		js.deepSerialize(pagedList, response.getWriter());
		return null;
	}

	private void filterResults(LaboratoryForm params,String prescribedId,List resultsLabels,
			List values,String testId, String[] methodIds)throws SQLException,Exception{

		String prescribedid[] = params.getRprescribedId();
		String resultlabel[] = params.getResultlabel();
		String resultValues[] = params.getResultvalue();
		String testDetailStatus[] = params.getTest_detail_status();
		String deleted[] = params.getDeleted();
		for (int i = 0;i< resultlabel.length;i++){
			if ( prescribedId.equals(prescribedid[i])
					&& ( !testDetailStatus[i].equals("A") ||
					( testDetailStatus[i].equals("A") && deleted[i].equals("Y"))) ){
				String resLabel = null;
				if(null != methodIds[i] && !methodIds[i].equals("")) {
					BasicDynaBean bean = new TestResultsDAO().getResultBean(testId,resultlabel[i],methodIds[i]);
					resLabel = (String) bean.get("resultlabel");
				}
				if(null != resLabel)
					resultsLabels.add(resLabel);
				else
					resultsLabels.add(resultlabel[i]);
				values.add(resultValues[i]);
			}
		}
	}

	public ActionForward signOffReports(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
	throws Exception {
		Map requestMap = request.getParameterMap();
		LaboratoryForm rf = (LaboratoryForm)form;
		String[] signoffReports = (String[])requestMap.get("signoff");
		String category = rf.getCategory();
		String visitId = rf.getVisitid();
		String mrNo = rf.getMrno();
		String[] sampleDate = (String[])requestMap.get("sample_date");
		String signoffandprint = (String)request.getParameter("printNeeded");
		String[] isConfidential =  (String[])requestMap.get("confidentialInfo");
		Map keys= new HashMap();
		Connection con = null;
		boolean status = true;
		String userName = (String) request.getSession(false).getAttribute("userid");
		int centerId = (Integer) request.getSession(false).getAttribute("centerId");
		TestDetails td = new TestDetails();
		List<Integer> prescIdList = new ArrayList<>();
		List<BasicDynaBean> testDetailsList = null;
		try{
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			if(signoffReports != null){
				for(int i =0;i<signoffReports.length;i++){

					//inserting signature row for a test.
					TestVisitReportSignaturesDAO sigDAO = new TestVisitReportSignaturesDAO();
					List<BasicDynaBean> testlist = LaboratoryDAO.getTestList(Integer.parseInt(signoffReports[i]));
					for(int j = 0; j<testlist.size(); j++) {
						BasicDynaBean test = testlist.get(j);
						int prescribedId = (Integer) test.get("prescribed_id");
						prescIdList.add(prescribedId);
						int reportId = Integer.parseInt(signoffReports[i]);

						status &= sigDAO.insertSignatureRecord(con, reportId, prescribedId, userName, "D",
								(String) test.get("conducted_by"));

						String technician = (String) test.get("technician");
						if (technician == null || technician.equals("")) {
							status &= LaboratoryDAO.updateTechnician(userName, prescribedId);
							technician = userName;
						}

						status &= sigDAO.insertSignatureRecord(con, reportId, prescribedId, userName, "T",
								technician);
					}
					
					keys= new HashMap();
					keys.put("signed_off", "Y");
					keys.put("report_state", "S");
					keys.put("signoff_center", centerId);
					keys.put("report_date", DateUtil.getCurrentTimestamp());
					keys.put("user_name", userName);
					keys.put("signedoff_by", userName);
					status &= testVisitReportsDao.update(con, keys,"report_id",Integer.parseInt(signoffReports[i])) > 0;

					status &= diagnosticsDao.upDateSignofTests(con, Integer.parseInt(signoffReports[i]), userName);

					//update test result status
					status &= diagnosticsDao.updateTestResultStatus(con, Integer.parseInt(signoffReports[i]), "S");
					List<BasicDynaBean> transferToPatientList =
							new OhSampleRegistrationDAO().getSourcePrescribedIDS(con, Integer.parseInt(signoffReports[i]), visitId);
					Map<String, Object> columnData = new HashMap<String, Object>();
					columnData.put("conducted", "S");
					columnData.put("report_id", Integer.parseInt(signoffReports[i]));
					
					status &= LaboratoryBO.copyDataToMultipleChains(con, transferToPatientList, columnData, "source_test_prescribed_id", null);

				}
				
				if (isUserOperatingOnMultiTab(rf)) {
					status = false;
					String MRNO = mrNo;
					ActionRedirect redirect = null;
					if (null == MRNO || MRNO.equals("")) {
						 BasicDynaBean incomingRegBean = incomingSampleRegistrationDAO.findByKey("incoming_visit_id", visitId);
						 MRNO = (incomingRegBean != null && incomingRegBean.get("mr_no") != null) ? incomingRegBean.get("mr_no").toString() : null;
					 }		 
					 if (category.equals("DEP_LAB"))
						 redirect = new ActionRedirect("/Laboratory/schedules.do?_method=getScheduleList");
					 else 
						 redirect = new ActionRedirect("/Radiology/schedules.do?_method=getScheduleList");			 
					 FlashScope flash = FlashScope.getScope(request);
					 redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
					 flash.info("Test Details already exist.");
					 redirect.addParameter("category", category);
					 redirect.addParameter("mr_no", MRNO);
					 
					 return redirect;
				 }

				HttpSession session = request.getSession();
				if(signoffandprint.equals("Y") && status){
					 String printURL = request.getContextPath();
					 printURL = printURL+"/pages/DiagnosticModule/DiagReportPrint.do?_method=printSelectedReports";
					 printURL = printURL+"&reportId="+signoffReports[0];
					 printURL = printURL+"&using=reportId&category="+category;
					 printURL = printURL+"&printerId="+request.getParameter("printerId");

					 List<String> printURLs = new ArrayList<String>();
					 printURLs.add(printURL);

					 session.setAttribute("printURLs", printURLs);
				}

			}
            // triggers signoff event
            if (status) {
              for (int prescId : prescIdList) {
            	interfaceEventMappingService.investigationSignOff(visitId, prescId, DEP_LAB.equals(category));
              }
            }
		} finally {
			DataBaseUtil.commitClose(con, status);
		}

		if (status) {
			if (null != signoffReports && signoffReports.length > 0 &&
					MessageUtil.allowMessageNotification(request,"general_message_send")) {
				List<String> signOffList = new ArrayList<>();
				String contextPath = RequestContext.getRequest()
						.getServletContext().getRealPath("");
				for (int i = 0; i < signoffReports.length; i++) {					
					if(!LaboratoryDAO.isHandoverToSponsor(Integer.parseInt(signoffReports[i])))
						signOffList.add(signoffReports[i]);
				}							
				if(!signOffList.isEmpty())
				{				
					String[] signOff = signOffList.toArray(new String[signOffList.size()]);
					 scheduleOPIPDiagReportPHR(signOff, contextPath);
				}
				for (int j = 0; j < signoffReports.length; j++) {
				  sendReportNotification(Integer.parseInt(signoffReports[j]));
					List criticalLabResults = LaboratoryDAO.getCriticalTestDetails(Integer.parseInt(signoffReports[j]));
					Map dataMap = ConversionUtils.listBeanToMapListMap(criticalLabResults, "test_id");
					notificationforCriticalTests(dataMap, Integer.parseInt(signoffReports[j]));
				}
			}
		}

		FlashScope flash = FlashScope.getScope(request);
		ActionRedirect redirect = new ActionRedirect("editresults.do?_method=getBatchConductionScreen");
		redirect.addParameter("category", category);
		redirect.addParameter("visitid", visitId);
		request.setAttribute("selectedPresList",request.getAttribute("selectedPresList"));
		redirect.addParameter("prescId", requestMap.get("prescId"));
		redirect.addParameter("reportId", request.getParameter("reportId"));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

		return redirect;
	}

	public BasicDynaBean findInList(List<BasicDynaBean> list,String column,int precribedId){
		BasicDynaBean resultBean = null;
		for(BasicDynaBean bean:list){
			if((Integer)bean.get("prescribed_id") == precribedId){
				resultBean = bean;
				break;
			}
		}
		return resultBean;
	}
	@IgnoreConfidentialFilters
	public ActionForward findImpressionJson(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res) throws Exception {

		res.setContentType("text/plain");
		res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
		JSONSerializer js = new JSONSerializer().exclude("class");
		String query = req.getParameter("query");
		String status = req.getParameter("status");
		String searchType = req.getParameter("searchType");

		if (searchType == null)
			searchType = "";
		if (status == null)
			status = "";

		List l = null;
		if (searchType.equals("impressionId")) {
			if (status.equals("active")) {
				l = LaboratoryDAO.findImpressionsActive(query, 0, status);
			}
		}
		HashMap retVal = new HashMap();
		retVal.put("result", ConversionUtils.copyListDynaBeansToMap(l));
		js.deepSerialize(retVal, res.getWriter());
		res.flushBuffer();

		return null;
	}

	@IgnoreConfidentialFilters
	public ActionForward checkRecords(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res) throws Exception {

		res.setContentType("text/plain");
		res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
		String responseContent = null;

		String impression = req.getParameter("impression");

		responseContent = LaboratoryDAO.findImpressionsDeatils(impression);
		res.setContentType("application/json");
		res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
		res.getWriter().write(responseContent == null ? "" : responseContent);
		res.flushBuffer();

		return null;
	}

	public static boolean isReportInAmendingPhase( String[] completed ) {
		boolean amendingPhase = false;

		for ( String completedStatus : completed ) {
			amendingPhase = completedStatus.equals("RP") || completedStatus.equals("RC") || completedStatus.equals("RV");
			if ( amendingPhase )
				break;
		}

		return amendingPhase;
	}

	public ActionForward saveResultsNotApplicableTests(ActionMapping mapping,ActionForm form,HttpServletRequest request,
			HttpServletResponse response) throws SQLException,IOException,Exception {
		String[] completedchecks = request.getParameterValues("completeCheck");
		int centerId = (Integer) request.getSession(false).getAttribute("centerId");
		Preferences pref = (Preferences)request.getSession(false).getAttribute("preferences");
		String userName = (String)request.getSession(false).getAttribute("userid");
		Connection con = null;
		Boolean success = false;
		int storeId = 0;
		Map<String, Object> identifiers = new HashMap<String, Object>();
		BasicDynaBean incRegDetailsBean = null;
		Map<String, Object> parentUpdationKeys = new HashMap<String, Object>();
		Map<String, String> columnData = new HashMap<String, String>();
		StringBuilder flashmsg = new StringBuilder();
		FlashScope flash = FlashScope.getScope(request);

		try{
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			for(int i= 0;i<completedchecks.length; i++) {
				int presID = Integer.parseInt(completedchecks[i]);
				BasicDynaBean testPrescribedBean = testPrescDao.findByKey("prescribed_id",presID);
				testPrescribedBean.set("conducted", "CRN");
				Map<String,Object> keys = new HashMap<String, Object>();
				keys.put("prescribed_id", presID);
				int j= testPrescDao.update(con, testPrescribedBean.getMap(), keys);
				if(j>0) success = true;
				//Marking parent center prescription conduction status to CRN
				identifiers.put("incoming_visit_id", testPrescribedBean.get("pat_id"));
				identifiers.put("prescribed_id", testPrescribedBean.get("prescribed_id"));
				identifiers.put("test_id", testPrescribedBean.get("test_id"));

				incRegDetailsBean = new GenericDAO("incoming_sample_registration_details").findByKey(identifiers);
				if (incRegDetailsBean != null && incRegDetailsBean.get("source_test_prescribed") != null
						&& !incRegDetailsBean.get("source_test_prescribed").equals("")) {

					columnData.put("conducted", "CRN");
					parentUpdationKeys.put("prescribed_id", incRegDetailsBean.get("source_test_prescribed"));
					testPrescDao.update(con, columnData, parentUpdationKeys);
				}

				String testID = testPrescribedBean.get("test_id").toString();
				BasicDynaBean condBean = testsConductedDao.getBean();
				condBean.set("mr_no", testPrescribedBean.get("mr_no"));
				condBean.set("patient_id", testPrescribedBean.get("pat_id"));
				condBean.set("test_id", testPrescribedBean.get("test_id"));
				condBean.set("conducted_date", new java.sql.Timestamp(new java.util.Date().getTime()));
				String condDoctor = LaboratoryDAO.getConductingDoctor(con,presID,testPrescribedBean.get("pat_id").toString());
				condBean.set("conducted_by", condDoctor);
				condBean.set("prescribed_id", presID);
				condBean.set("user_name", testPrescribedBean.get("user_name"));
				success = testsConductedDao.insert(con, condBean);

				String modConsumableActive = "Y";
				if ( (pref!=null) && (pref.getModulesActivatedMap() != null) ) {
					modConsumableActive = (String)pref.getModulesActivatedMap().get("mod_consumables_flow");
					if(modConsumableActive == null || "".equals(modConsumableActive)){
						modConsumableActive = "N";
					}
				}
				storeId = DiagnosticDepartmentMasterDAO.getStoreOfDiagnosticDepartment(testID,centerId);
				if (modConsumableActive.equals("Y")) {
					if(!(Boolean)testPrescribedBean.get("stock_reduced")){
						if(new GenericDAO("diagnostic_reagent_usage").findByKey("prescription_id", presID) == null){
							success =  StoreItemStock.updateReagents(con, testID, presID, userName, storeId,null,0, "diagnostics",flashmsg);
							if(!success) { 
								if(!"".equals(flashmsg.toString()))
									flash.info("Insufficient stock items <br><br>"+flashmsg.toString());
								break;
								}
							if(success){
								BasicDynaBean testBean = testPrescDao.getBean();
								testBean.set("stock_reduced", true);
								success &= testPrescDao.update(con, testBean.getMap(), "prescribed_id", presID) > 0 ;
							}
						}else{
							List reagents = LaboratoryDAO.getDiagReagentsUsed(presID);
							List<DynaBean> reagentsRequired = new ArrayList<DynaBean>();
							DynaBeanBuilder builder = new DynaBeanBuilder();
							builder.add("item_id", Integer.class)
								.add("qty",BigDecimal.class)
								.add("redusing_qty",BigDecimal.class);
							DynaBean reagentsbean = builder.build();
							for(int k = 0;k<reagents.size();k++){
								BasicDynaBean reagentsUsed = (BasicDynaBean)reagents.get(k);
								reagentsbean = builder.build();
								reagentsbean.set("item_id",reagentsUsed.get("reagent_id"));
								reagentsbean.set("redusing_qty", (BigDecimal)reagentsUsed.get("qty"));
								reagentsbean.set("qty", (BigDecimal)reagentsUsed.get("qty"));
								reagentsRequired.add(reagentsbean);
							}
							success =  StoreItemStock.updateReagents(con, testID, presID, userName, storeId,reagentsRequired,0, "diagnostics",flashmsg);
							if(success){
								BasicDynaBean testBean = testPrescDao.getBean();
								testBean.set("stock_reduced", true);
								success &= testPrescDao.update(con, testBean.getMap(), "prescribed_id", presID) > 0 ;
							}else{
								if(!"".equals(flashmsg.toString()))
									flash.info("Insufficient stock items <br><br>"+flashmsg.toString());
								break;
							}
						}
					}
				}

				success = (ResourceDAO.updateAppointments(con, presID,"DIA"));

			}
		}finally {
			DataBaseUtil.commitClose(con, success);
			//update stock timestamp
			StockFIFODAO stockFIFODAO = new StockFIFODAO();
			stockFIFODAO.updateStockTimeStamp();
			stockFIFODAO.updateStoresStockTimeStamp(storeId);
		}
		ActionRedirect redirect = new ActionRedirect(request.getHeader("Referer").
								replaceAll("&" + FlashScope.FLASH_KEY + "=[0-9A-Fa-f]+", ""));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;
	}

	public ActionForward setConductingDoctorForSelectedTests(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)throws SQLException {
		String category = mapping.getProperty("category");
		String screen = mapping.getProperty("action_id");
		String chkBoxName = "forEdit";
		if (screen.equalsIgnoreCase("lab_unfinished_tests") || screen.equalsIgnoreCase("rad_unfinished_tests") || 
				screen.equalsIgnoreCase("lab_unfinished_tests_search"))
			chkBoxName = "completeCheck";
		String[] prescribedIDS = request.getParameterValues(chkBoxName);
		String doctorID = request.getParameter("commonConductingDoctor");
		if (doctorID != null && doctorID.equals(""))
			doctorID = null;
		Connection con = null;
		boolean success = true;
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			if (prescribedIDS != null) {
				for (int i=0; i<prescribedIDS.length; i++) {
					LaboratoryDAO.updateActivityDetails(con, "DIA", prescribedIDS[i], doctorID);
				}
			}
			success = true;
		} finally {
			DataBaseUtil.commitClose(con, success);
		}


		FlashScope flash = FlashScope.getScope(request);
		ActionRedirect redirect = new ActionRedirect(request.getHeader("Referer").
				                replaceAll("&" + FlashScope.FLASH_KEY + "=[0-9A-Fa-f]+", ""));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

		return redirect;

	}
	
	private boolean isUserOperatingOnMultiTab(LaboratoryForm labForm)throws SQLException {
		
		String[] rPrescribedIDs = labForm.getRprescribedId();
		String[] rRevisionNumbers = labForm.getRrevisionNumber();
		String[] prescribedIDs = labForm.getPrescribedid();
		String[] revisionNumbers = labForm.getRevisionNumber();
		
		if (null != rPrescribedIDs && rPrescribedIDs.length > 0) {
			for (int i=0; i<rPrescribedIDs.length; i++) {
				if (null != rPrescribedIDs[i] && !rPrescribedIDs[i].equals("") && null != rRevisionNumbers[i] && !rRevisionNumbers[i].equals("")) {		
					if (!rRevisionNumbers[i].equals(testPrescDao.findByKey("prescribed_id", Integer.parseInt(rPrescribedIDs[i]))
							.get("revision_number").toString()))
						return true;
				}
			}
		}
		
		if (null != prescribedIDs && prescribedIDs.length > 0) {
			for (int j=0; j<prescribedIDs.length; j++) {
				if (null != prescribedIDs[j] && !prescribedIDs[j].equals("") && null != revisionNumbers[j] && !revisionNumbers[j].equals("")) {		
					if (!revisionNumbers[j].equals(testPrescDao.findByKey("prescribed_id", Integer.parseInt(prescribedIDs[j]))
							.get("revision_number").toString()))
						return true;
				}
			}
		}
		
		return false;
	}
	private void scheduleOPIPDiagReportPHR(String[] signOff, String contextPath) throws SQLException, ParseException, IOException {
		
		Map<String, Object> jobData = new HashMap<>();
		jobData.put("report_id", signOff);
		jobData.put("forceResend", "Send");
		jobData.put("path", contextPath);
		MessageManager mgr = new MessageManager();
		/* Build OP JOb*/
		mgr.processEvent("op_phr_diag_share", jobData);
		/*Build Report Signoff Job*/
		mgr.processEvent("diag_report_signoff", jobData);
		/*Build IP job*/
		mgr.processEvent("ip_phr_diag_share", jobData);
	}
	
  private void sendReportNotification(Integer reportId)
      throws SQLException, ParseException, IOException {
    List<BasicDynaBean> beans = lDao.getNotificationUser(reportId, false);
    for (BasicDynaBean bean : beans) {
      if (bean != null) {
        Map reportData = new HashMap();
        reportData.put("receipient_id__", bean.get("emp_username"));
        reportData.put("entity_id", reportId);
        GenericDAO dao1 = new GenericDAO("message_log_batch_id");
        int batchId = dao1.getNextSequence();
        reportData.put("batch_id", Integer.toString(batchId));
        MessageManager mgr = new MessageManager();
        mgr.processEvent("diag_report_signedoff_notification", reportData);
      }
    }
  }
}
