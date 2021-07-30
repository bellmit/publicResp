package com.insta.hms.diagnosticmodule.laboratory;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.bob.hms.diag.ohsampleregistration.OhSampleRegistrationDAO;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.billing.Bill;
import com.insta.hms.billing.BillBO;
import com.insta.hms.billing.BillPrintHelper;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.diagnosticmodule.common.DiagReportGenerator;
import com.insta.hms.diagnosticmodule.common.DiagnosticsDAO;
import com.insta.hms.diagnosticmodule.prescribetest.DiagnoDAOImpl;
import com.insta.hms.diagnosticsmasters.outhousemaster.OutHouseMasterDAO;
import com.insta.hms.master.DiagnosticDepartmentMaster.DiagnosticDepartmentMasterDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.Histopathology.HistoImpressionMasterDao;
import com.insta.hms.master.InComingHospitals.InComingHospitalDAO;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;
import com.insta.hms.master.ReferalDoctor.ReferalDoctorDAO;
import com.insta.hms.messaging.MessageManager;
import com.insta.hms.messaging.MessageUtil;

import java.io.StringWriter;
import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.http.HttpHeaders;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import flexjson.JSONSerializer;

public class SignedOffReportsAction extends BaseAction {
	static Logger logger = LoggerFactory.getLogger(SignedOffReportsAction.class);
	
	JSONSerializer js = new JSONSerializer().exclude("class");
	
    private static final GenericDAO sampleCollectionCentersDAO =
        new GenericDAO("sample_collection_centers");
    
    private static final GenericDAO testVisitReportsDAO = new GenericDAO("test_visit_reports");

		private static final GenericDAO patientDetailsDao = new GenericDAO("patient_details");
	  private static GenericDAO messageTypesDao = new GenericDAO("message_types");

	public ActionForward getReportListScreen(ActionMapping mapping, ActionForm form,HttpServletRequest request,
			 HttpServletResponse response) throws IOException, SQLException {

		String category = null;
		category = mapping.getProperty("category");
		String userId =(String)request.getSession(false).getAttribute("userid");
		int centerId = (Integer) request.getSession(false).getAttribute("centerId");

		JSONSerializer json = new JSONSerializer();

		List<BasicDynaBean> incomingHospitals = new InComingHospitalDAO().listAll(
				 Arrays.asList(new String[] {"hospital_id","hospital_name"}), "status", "A", "hospital_name");
		request.setAttribute("inHouses", json.serialize(ConversionUtils.copyListDynaBeansToMap(incomingHospitals)));

		List outHouses = OutHouseMasterDAO.getAllOutSources();
		List HistoCytoNames = HistoImpressionMasterDao.getAllActiveHistoImpressions();
		request.setAttribute("outHouses", json.serialize(outHouses));
		request.setAttribute("HistoCytoNames", json.serialize(HistoCytoNames));
		request.setAttribute("module", category);
		request.setAttribute("test_timestamp", new DiagnoDAOImpl().getCountFromDiagTimeStamp());
		request.setAttribute("userDept", new DiagnosticDepartmentMasterDAO().getUserDepartment(userId));
		request.setAttribute("DiagArraylist", DiagnosticDepartmentMasterDAO.getDepts(category));
		List<BasicDynaBean> collectionCenters = sampleCollectionCentersDAO.findAllByKey("center_id", centerId);
		request.setAttribute("collectionCenters", collectionCenters);
		BasicDynaBean colcenterBean = sampleCollectionCentersDAO.findByKey("collection_center_id", -1);
		request.setAttribute("defautlCollectionCenter", colcenterBean.get("collection_center"));
		request.setAttribute("prescribedDoctors", js.serialize(ReferalDoctorDAO.getPrescribedDoctors()));
		
		String date_range = request.getParameter("date_range");
		String week_start_date = null;
		if (date_range != null && date_range.equals("week")) {
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
	        Calendar cal = Calendar.getInstance();
	        cal.add(Calendar.DATE, -7);
	        Date openDt = cal.getTime();
	        week_start_date = dateFormat.format(openDt);       
		}
		
		ActionForward forward = new ActionForward(mapping.findForward("ReportList").getPath());
		// when ever user uses a pagination rfdate should not append again.
		if (date_range != null && date_range.equals("week") && request.getParameter("rfdate") == null) {
			addParameter("rfdate", week_start_date, forward);
	    }
		return forward;
		
	}
	
  @IgnoreConfidentialFilters
  public ActionForward searchReferralDoctors(ActionMapping m, ActionForm f, HttpServletRequest req,
      HttpServletResponse res) throws SQLException, IOException {
    res.setContentType("text/plain");
    res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    String query = req.getParameter("query");
    List<BasicDynaBean> referalDocs = ReferalDoctorDAO.getReferenceDoctors(query);
    Map result = new HashMap();
    result.put("result", ConversionUtils.copyListDynaBeansToMap(referalDocs));
    js.deepSerialize(result, res.getWriter());
    res.flushBuffer();
    return null;
  }

	@IgnoreConfidentialFilters
	public ActionForward getReportList(ActionMapping mapping, ActionForm form,HttpServletRequest request,
			 HttpServletResponse response)	throws IOException, SQLException,   Exception{

		LaboratoryForm dForm = (LaboratoryForm)form;
		HttpSession session = request.getSession(false);
		String mrno = null;
		String firstName = null;
		String lastName = null;
		String department = null;
		String testName = null;
		String sampelNo = null;
		String labno = null;
		String phoneNo=null;
		String inHouse = null;
		String outHouse = null;
		String userDept = null;
		String userId =(String)session.getAttribute("userid");
		String shortImpression =null;
		String collectionCenterId = null;
		String reference_docto_id = null;
		String pres_doctor = null;

		if ( (dForm.getMrno() != null) && !dForm.getMrno().equals("") ) {
			mrno = dForm.getMrno();
		}

		if ( (dForm.getShortImpression() != null && !dForm.getShortImpression().equals(""))) {
			shortImpression = dForm.getShortImpression();
		}

		phoneNo = request.getParameter("phoneNo");
		if (phoneNo != null && phoneNo.equals("")) phoneNo = null;

		labno = request.getParameter("labno");
		if (labno != null && labno.equals("")) labno = null;

		if ( (dForm.getFirstName() != null) && !dForm.getFirstName().equals("") ) {
			firstName = dForm.getFirstName();
		}

		if ( (dForm.getLastName() != null) && !dForm.getLastName().equals("") ) {
			lastName = dForm.getLastName();
		}

		String patientName = request.getParameter("patientName");
  	 	if (patientName != null){request.setAttribute("patientName", patientName);}

  	 	if (dForm.getDiagname() != null && !dForm.getDiagname().equals("")){testName = dForm.getDiagname();}

  	 	if (dForm.getSampleNo() != null && !dForm.getSampleNo().equals("")){
  	 		sampelNo = dForm.getSampleNo();
 		}

		userDept = new DiagnosticDepartmentMasterDAO().getUserDepartment(userId);
		department = request.getParameter("department");

		if (department == null ) {
			if (userDept != null && !userDept.equals(""))
				department = userDept;
		}

		boolean showOnlyInhouseVisits = dForm.getShowOnlyInhouseTests();

		ArrayList patient = null;
		if (!dForm.getPatientAll()) {
			patient = new ArrayList();
			if (dForm.getPatientIp()) patient.add(Bill.BILL_VISIT_TYPE_IP);
			if (dForm.getPatientOp()) patient.add(Bill.BILL_VISIT_TYPE_OP);
			if (dForm.getPatientIn()) patient.add(Bill.BILL_VISIT_TYPE_INCOMING);
		}

		ArrayList houses = null;
		if (dForm.getShowOnlyInhouseTests() || dForm.getShowOnlyouthouseTests()) {
			houses = new ArrayList();
			if (dForm.getShowOnlyInhouseTests()) houses.add("i");
			if (dForm.getShowOnlyouthouseTests()) houses.add("o");
		}

		if (dForm.getInhouse() != null && !dForm.getInhouse().equals("")){
			inHouse = dForm.getInhouse();
		}

		if (dForm.getOuthouse() != null && !dForm.getOuthouse().equals("")){
			outHouse = dForm.getOuthouse();
		}

		java.sql.Date fromReportDate = DataBaseUtil.parseDate(dForm.getRfdate());
		java.sql.Date toReportDate = DataBaseUtil.parseDate(dForm.getRtdate());

		ArrayList testType = null;
		if (!dForm.isTestAll()) {
			testType = new ArrayList();
			if (dForm.isTestIncoming()) testType.add(LaboratoryBO.INCOMING_TEST);
			if (dForm.isTestOutgoing()) testType.add(LaboratoryBO.OUTHOUST_TEST);
		}

		ArrayList status = null;
		status = new ArrayList();
		status.add("A");
		status.add("I");

		List<String> reportSeveritySearchList = null;
		String[] selectedReportSeverities = request.getParameterValues("report_results_severity_status");
	
		if (null != selectedReportSeverities && selectedReportSeverities.length > 0 && !selectedReportSeverities[0].equals("")) {
			reportSeveritySearchList = Arrays.asList(request.getParameterValues("report_results_severity_status"));
		}

		java.sql.Date fromDate = DataBaseUtil.parseDate(dForm.getFdate());
		java.sql.Date toDate = DataBaseUtil.parseDate(dForm.getTdate());

		int pageNum = 1;
		if (dForm.getPageNum()!=null && !dForm.getPageNum().equals("")) {
			pageNum = Integer.parseInt(dForm.getPageNum());
		}

		String formSort = dForm.getSortOrder();
		String formSortRep = null;
		if(formSort != null){
			if(formSort.equals("mrno")){
				formSort ="tp.mr_no";
				formSortRep ="mr_no";
			}
			if(formSort.equals("reportdate")){
				formSort = "tv.report_date";
			}
		}
		String category = null;
		category = mapping.getProperty("category");
		collectionCenterId = request.getParameter("collectionCenterId");
		int userSampleCollectionCenterId = (Integer) request.getSession(false).getAttribute("sampleCollectionCenterId");
		int centerId = (Integer) request.getSession(false).getAttribute("centerId");
		if(dForm.getReference_docto_id() != null && !dForm.getReference_docto_id().equals("")){
			reference_docto_id = dForm.getReference_docto_id();
		}
		if(dForm.getPres_doctor()!=null && !dForm.getPres_doctor().equals("")){
			pres_doctor = dForm.getPres_doctor();
		}
		PagedList pagedList = new DiagnosticsDAO().getAllReports(mrno, labno, firstName, patientName,
				department, testName, pageNum, patient, status, testType, fromDate, toDate,
				fromReportDate, toReportDate, formSort, dForm.getSortReverse(), category,
				showOnlyInhouseVisits, houses, inHouse, outHouse,
				true, phoneNo,request.getParameterValues("ready_for_handover"),request.getParameter("handed_over"),sampelNo,shortImpression,
				collectionCenterId,userSampleCollectionCenterId,request.getParameterValues("priority"),
				dForm.getPatient_sponsor_type(),reference_docto_id, (List<String>)reportSeveritySearchList,pres_doctor);

		request.setAttribute("pagedList",pagedList);
		List<BasicDynaBean> collectionCenters = sampleCollectionCentersDAO.findAllByKey("center_id", centerId);
		request.setAttribute("collectionCenters", collectionCenters);
		BasicDynaBean colcenterBean = sampleCollectionCentersDAO.findByKey("collection_center_id", -1);
		request.setAttribute("defautlCollectionCenter", colcenterBean.get("collection_center"));

		BasicDynaBean printerBean = null;
		if(category.equals("DEP_LAB"))
			printerBean = PrintConfigurationsDAO.getPrintMode(PrintConfigurationsDAO.PRINT_TYPE_DIAG);
		else
			printerBean = PrintConfigurationsDAO.getPrintMode(PrintConfigurationsDAO.PRINT_TYPE_DIAG_RAD);

		request.setAttribute("printerBean", printerBean);

		List rIdList = new ArrayList();
		for (BasicDynaBean map: (List<BasicDynaBean>) pagedList.getDtoList() ){
			rIdList.add(map.get("report_id"));
		}
		List<BasicDynaBean> testsOfReports = DiagnosticsDAO.getTestNamesOfReports(rIdList,request.getParameterValues("priority"),pres_doctor);
		Map reportsMap = new HashMap();
		if (testsOfReports != null)
		reportsMap = ConversionUtils.listBeanToMapListBean(testsOfReports, "report_id");


		request.setAttribute("reportsMap", reportsMap);
		request.setAttribute("userDept", department);

		JSONSerializer json = new JSONSerializer();
		List<BasicDynaBean> incomingHospitals = new InComingHospitalDAO().listAll(
				 Arrays.asList(new String[] {"hospital_id","hospital_name"}), "status", "A", "hospital_name");
		request.setAttribute("inHouses", json.serialize(ConversionUtils.copyListDynaBeansToMap(incomingHospitals)));

		List outHouses = OutHouseMasterDAO.getAllOutSources();
		request.setAttribute("outHouses", json.serialize(outHouses));
		List HistoCytoNames = HistoImpressionMasterDao.getAllActiveHistoImpressions();
		request.setAttribute("HistoCytoNames", json.serialize(HistoCytoNames));
		request.setAttribute("module", category);
		request.setAttribute("test_timestamp", new DiagnoDAOImpl().getCountFromDiagTimeStamp());
		request.setAttribute("DiagArraylist", DiagnosticDepartmentMasterDAO.getDepts(category));
		request.setAttribute("outsourceRecForUsercenter", new GenericDAO("diag_outsource_master").findByKey("outsource_dest", centerId+""));
		request.setAttribute("hospCentersList", ConversionUtils.listBeanToMapBean(new GenericDAO("hospital_center_master").listAll(), "center_id"));
		request.setAttribute("referalDetails",js.serialize(ReferalDoctorDAO.getReferencedoctors()));
		request.setAttribute("prescribedDoctors", js.serialize(ReferalDoctorDAO.getPrescribedDoctors()));
		BasicDynaBean printPrefs =
			PrintConfigurationsDAO.getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_DIAG);

		String mode = (String) printPrefs.get("print_mode");
		if (mode.equals("T")) {
			request.setAttribute("rawMode", "&rawprint=y&col="+printPrefs.get("text_mode_column"));
		} else {
			request.setAttribute("rawMode","");
		}

		BasicDynaBean diagGenericPref = GenericPreferencesDAO.getdiagGenericPref();
    request.setAttribute("diagGenericPref", diagGenericPref);

		return mapping.findForward("ReportList");
	}
	public ActionForward getHandOver(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws Exception{
		String reportId = request.getParameter("reportId");
		request.setAttribute("reportDetails",
		    testVisitReportsDAO.findByKey("report_id", Integer.parseInt(reportId)));
		Map patientDetails = VisitDetailsDAO.getPatientVisitDetailsMap(request.getParameter("visitid"));
		request.setAttribute("patient", patientDetails);
		if(patientDetails == null)
			patientDetails = OhSampleRegistrationDAO.getIncomingCustomer(request.getParameter("visitid")).getMap();

		request.setAttribute("inpatient", patientDetails);

		return mapping.findForward("gethandoverreport");
	}

	public ActionForward handOverReport(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws Exception{

			Map requestMap = request.getParameterMap();
			FlashScope flash = FlashScope.getScope(request);
			String[] handoverDt = (String[])requestMap.get("hand_over_time_dt");
			String[] handoverTm = (String[])requestMap.get("hand_over_time_tm");

			String reportId = ((String[])requestMap.get("reportId"))[0];
            BasicDynaBean visitReportBean =
                testVisitReportsDAO.findByKey("report_id", Integer.parseInt(reportId));
			List errors = new ArrayList();
			ActionForward errForward = copyToDynaBean(request, response, visitReportBean);
			if (errForward != null) return errForward;

			visitReportBean.set("handed_over", "Y");
			visitReportBean.set("hand_over_time", DateUtil.parseTimestamp(handoverDt[0], handoverTm[0]));

			Connection con = null;
			boolean success = true;
			try{
				con = DataBaseUtil.getConnection();
				con.setAutoCommit(false);
				if(errors.isEmpty())
					success = testVisitReportsDAO.update(
							con, visitReportBean.getMap(), "report_id",Integer.parseInt(reportId)) > 0;
				else
					flash.error("Incorrectly formated values..");
			}finally{
				DataBaseUtil.commitClose(con, success);
			}

			String referrer = ((HttpServletRequest) request).getHeader("referer");
		    ((HttpServletResponse) response).sendRedirect(referrer);

		    return null;
	}

	public ActionForward getSignedOffReportContent(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws Exception {

		String reportId = request.getParameter("reportId");
		BasicDynaBean testVisitBean = testVisitReportsDAO.findByKey("report_id", Integer.parseInt(reportId));

		String reportData = DiagReportGenerator.getReport(Integer.parseInt(reportId),
				(String)request.getSession(false).getAttribute("userid"), request.getContextPath(),false,false) ;
		Integer centerID = DiagReportGenerator.getReportPrintCenter(Integer.parseInt(reportId), null);

		request.setAttribute("templateContent",reportData);

		request.setAttribute("reportid", reportId);

		BasicDynaBean printPrefs = PrintConfigurationsDAO.getCenterPageOptions(PrintConfigurationsDAO.PRINT_TYPE_DIAG, centerID);
		request.setAttribute("prefs",printPrefs);

		request.setAttribute("save", "disabled");

		return mapping.findForward("templateEditor");
	}

	public ActionForward signOffAddendum(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws Exception{
		String reportId = request.getParameter("reportId");
		boolean success = true;

		BasicDynaBean visitreportBean = testVisitReportsDAO.findByKey("report_id", Integer.parseInt(reportId));
		visitreportBean.set("addendum_signed_off", "Y");
		FlashScope flash = FlashScope.getScope(request);

		Connection con = null;
		try{
			con = DataBaseUtil.getConnection();
			success = testVisitReportsDAO.update(con, visitreportBean.getMap(), "report_id",Integer.parseInt(reportId)) > 0 ;
			if(success)
				flash.error("Failed to save Addendum to the report");
		}finally{
			DataBaseUtil.closeConnections(con, null);
		}
		
		ActionRedirect redirect = new ActionRedirect(request.getHeader("Referer").
				replaceAll("&" + FlashScope.FLASH_KEY + "=[0-9A-Fa-f]+", ""));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

		return redirect;
	}

	public ActionForward revertSignOffReports(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
	throws Exception{
		Map requestMap = request.getParameterMap();
		String[] revertSignOffReports = (String[])requestMap.get("revert_signoff");

		GenericDAO testsPrescribed = new GenericDAO("tests_prescribed");
		GenericDAO testsDetails = new GenericDAO("test_details");
		GenericDAO testsConducted = new GenericDAO("tests_conducted");
		Map keys= new HashMap();
		Connection con = null;
		boolean status = true;
		try{
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			if(revertSignOffReports != null){
				for(int i =0;i<revertSignOffReports.length;i++){
					keys= new HashMap();
					keys.put("signed_off", "N");
					keys.put("report_state", "A");
					keys.put("signedoff_by", "");
					status &= testVisitReportsDAO.update(con, keys,"report_id",Integer.parseInt(revertSignOffReports[i])) > 0;

					BasicDynaBean bean = testVisitReportsDAO.findByKey("report_id", Integer.parseInt(revertSignOffReports[i]));
					Map<String,Object> tp_keys = new HashMap<String, Object>();
					tp_keys.put("report_id", bean.get("report_id"));
					tp_keys.put("pat_id", bean.get("patient_id"));

					//Make conducted status back to N and reportId is NUll for
					//the Collection center tests.(Only for Internal Lab Tests Only)
					status &= DiagnosticsDAO.makeConductedStatusBackToN(con, Integer.parseInt(revertSignOffReports[i]));

					List<BasicDynaBean> tpList = testsPrescribed.listAll(null, tp_keys, null);

					for(int j=0; j<tpList.size(); j++) {
						BasicDynaBean tpBean = tpList.get(j);
						if ("X".equals(tpBean.get("conducted"))) {
						  continue;
						}
						tp_keys.put("prescribed_id", tpBean.get("prescribed_id"));

						status &= TestVisitReportSignaturesDAO.deleteSignatures(con, Integer.parseInt(revertSignOffReports[i]),
								(Integer) tpBean.get("prescribed_id"));
						boolean isAmendedTest = DiagnosticsDAO.isAmendedTest(con,(String)tpBean.get("test_id"),(Integer)tpBean.get("prescribed_id"),(String)tpBean.get("pat_id"));
						if(isAmendedTest)
							tpBean.set("conducted", "RC");
						else
							tpBean.set("conducted", "C");

						tpBean.set("user_name", (String)request.getSession(false).getAttribute("userid"));
						tpBean.set("signoff_reverted", true);
						status &= testsPrescribed.update(con, tpBean.getMap(), tp_keys) > 0;
						
						Map tc_keys= new HashMap();
						tc_keys.put("validated_by", "");
						tc_keys.put("validated_date", null);
						status &= testsConducted.update(con, tc_keys,"prescribed_id",(Integer) tpBean.get("prescribed_id")) > 0;
						
						Map<String,Object> td_keys = new HashMap<String, Object>();
						List<String> cols = new ArrayList<String>();
						cols.add("test_id");
						cols.add("prescribed_id");
						cols.add("patient_id");
						cols.add("test_detail_status");
						cols.add("original_test_details_id");
						cols.add("test_details_id");
						td_keys.put("test_id", (String)tpBean.get("test_id"));
						td_keys.put("prescribed_id", (Integer)tpBean.get("prescribed_id"));
						td_keys.put("patient_id", (String)tpBean.get("pat_id"));
						List<BasicDynaBean> testDetailslist = testsDetails.listAll(cols,td_keys,null);
						for(int k=0; k<testDetailslist.size();k++) {
							BasicDynaBean tdBean = testDetailslist.get(k);
							if(!tdBean.get("test_detail_status").equals("A")) {
								if(tdBean.get("test_detail_status").equals("S")) {
									if(null != tdBean.get("original_test_details_id") && (Integer)tdBean.get("original_test_details_id")!=0 )
										tdBean.set("test_detail_status", "RC");
									else
										tdBean.set("test_detail_status", "C");

									status &= testsDetails.update(con, tdBean.getMap(), "test_details_id",(Integer)tdBean.get("test_details_id")) > 0;
								}
							}
						}
					}
				}
			}
		}finally{
			DataBaseUtil.commitClose(con, status);
			if(status && MessageUtil.allowMessageNotification(request,"general_message_send")) {
			    MessageManager mgr = new MessageManager();
				Map reportData = new HashMap();
				String[] revertSignOff = revertSignOffReports;
			    reportData.put("report_id",revertSignOff);
			    reportData.put("status", status);
			    mgr.processEvent("diag_report_revert", reportData);
			}
		}

		String referrer = ((HttpServletRequest) request).getHeader("referer");
	    ((HttpServletResponse) response).sendRedirect(referrer);

	    return null;
	}

	public ActionForward updateAndSendEmail(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws SQLException, Exception {
		Boolean isSuccess = false;
		String newEmail = request.getParameter("newEmail");
		String mr = request.getParameter("patientMrno");
		String[] reportId = {request.getParameter("reportId")};
		boolean status = false;
		Connection con = DataBaseUtil.getConnection();
		con.setAutoCommit(false);
		try {
			Map<String, String> newcol = new HashMap<String, String>();
			newcol.put("email_id", newEmail);
			Map<String, String> refcol = new HashMap<String, String>();
			refcol.put("mr_no", mr);
			status = patientDetailsDao.update(con, newcol, refcol) > 0;
		} finally {
			DataBaseUtil.commitClose(con, status);
			BasicDynaBean messageTypeBean= messageTypesDao.findByKey("message_type_id", "email_diag_report");
			if (status && messageTypeBean != null && messageTypeBean.get("status").equals("A")
					&& MessageUtil.allowMessageNotification(request, "general_message_send")) {
				Map<String, Object> reportData = new HashMap<>();
				reportData.put("report_id",reportId);
				reportData.put("forceResend","Send");
				MessageManager mgr = new MessageManager();
				isSuccess = mgr.processEvent("email_diag_report_share", reportData);
			}
		}
		response.setContentType("application/json");
		response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
		String json ="{\"result\": \"" + isSuccess + "\",\"isUpdated\" : \"" + status +"\" }";
		response.getWriter().write(json);
		response.flushBuffer();
		return null;

	}

}
