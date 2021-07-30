package com.insta.hms.OTServices;

import com.bob.hms.common.Preferences;
import com.bob.hms.otmasters.theamaster.TheatreMasterDAO;
import com.insta.hms.OTServices.OtRecord.OtRecordDAO;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.integration.configuration.InterfaceEventMappingService;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.DoctorMaster.DoctorMasterDAO;
import com.insta.hms.master.GenericDocumentTemplate.GenericDocumentTemplateDAO;
import com.insta.hms.master.PatientHeaderTemplate.PatientHeaderTemplateDAO;
import com.insta.hms.master.Sections.SectionsDAO;

import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class OTServicesDashboard extends BaseAction {

	static Logger log = LoggerFactory.getLogger(OTServicesDashboard.class);
	static GenericDocumentTemplateDAO templateDao = new GenericDocumentTemplateDAO("doc_rich_templates");
	static OTReportsDAO reportsDao = new OTReportsDAO();
	static OTServicesBO otServicesBO = new OTServicesBO();
	static OTServicesDAO otServicesDAO = new OTServicesDAO();
	static JSONSerializer js = new JSONSerializer().exclude("class");
	static PatientHeaderTemplateDAO phTemplateDao = new PatientHeaderTemplateDAO();
	static OperationDetailsDAO opDetDAO = new OperationDetailsDAO();
	static CenterMasterDAO centerDao = new CenterMasterDAO();
	
	private static final GenericDAO operationMasterDAO = new GenericDAO("operation_master");
	private static final GenericDAO hospDirectBillPrefs = new GenericDAO("hosp_direct_bill_prefs");
	
	private InterfaceEventMappingService interfaceEventMappingService = ApplicationContextProvider
	      .getBean(InterfaceEventMappingService.class);
	
	/*
	 * which will lists the pending services for the patients.
	 */
	@IgnoreConfidentialFilters
	public ActionForward pendingList(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException, ParseException, SQLException {
		Map params = request.getParameterMap();
		JSONSerializer js = new JSONSerializer().exclude("class");
		List<String> columnsList = new ArrayList<String>();
		columnsList.add("op_id");
		columnsList.add("operation_name");
		Map<String,Object> filterMap = new HashMap<String, Object>();
		filterMap.put("status", "A");

		request.setAttribute("pagedList", OTServicesDAO.searchPendingOperations(params,
				ConversionUtils.getListingParameter(params), true));
		request.setAttribute("operations", js.serialize(ConversionUtils.listBeanToListMap(operationMasterDAO.listAll(columnsList, filterMap, "operation_name"))));
        request.setAttribute("directBillingPrefs",
            ConversionUtils.listBeanToMapBean(hospDirectBillPrefs.listAll(), "item_type"));
		return mapping.findForward("pendingoperationslist");
	}
	/*
	 * returns partially conducted and coundction completed services details list.
	 */
	@IgnoreConfidentialFilters
	public ActionForward conductedList(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException, ParseException, SQLException {
		Map params = request.getParameterMap();
		JSONSerializer js = new JSONSerializer().exclude("class");
		request.setAttribute("pagedList", OTServicesDAO.searchPendingOperations(params,
				ConversionUtils.getListingParameter(params), false));
		request.setAttribute("operations", js.serialize(OTServicesDAO.getOperations()));
		return mapping.findForward("conductedoperationslist");
	}

	/**
	 * Brings up Operation Conduction screen with all selected operation details
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@IgnoreConfidentialFilters
	public ActionForward getOperationsConductionScreen(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)throws Exception{

		HttpSession session = request.getSession(false);
		java.util.HashMap urlRightsMap = (java.util.HashMap) session.getAttribute("urlRightsMap");
		java.util.HashMap actionUrlMap = (java.util.HashMap) session.getServletContext().getAttribute("actionUrlMap");
		Integer roleId = (Integer)session.getAttribute("roleId");
		
		String visitId = request.getParameter("visitId");
		if (visitId == null || visitId.equals(""))
			visitId = request.getParameter("visitid");

		String prescriptionId = request.getParameter("prescription_id");
		BasicDynaBean bean = null;
		if (prescriptionId != null && !prescriptionId.equals("")) {
			 bean = otServicesDAO.getPrescribedOperation(Integer.parseInt(prescriptionId));
			request.setAttribute("operation", bean);
		}
		request.setAttribute("patientvisitdetails", VisitDetailsDAO.getPatientVisitDetailsMap(visitId));
		request.setAttribute("has_consumables", bean == null ? null : OTServicesDAO.getOperationsWithConsumables((String) bean.get("op_id")));
		request.setAttribute("doctors_list",DoctorMasterDAO.getDoctorsandCharges());

		return mapping.findForward("conductionscreen");
	}

	/**
	 * Saves the operation details from operation conduction screen,
	 * handles actions like rescheduling an operation and completing an operation
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward conductOperation(ActionMapping mapping,ActionForm form,
			HttpServletRequest request,HttpServletResponse response)throws Exception{
		Map requestMap = request.getParameterMap();
		List errors = new ArrayList();
		Preferences pref = (Preferences)request.getSession(false).getAttribute("preferences");
		String modConsumableActive = "Y";

		if ( (pref!=null) && (pref.getModulesActivatedMap() != null) ) {
			modConsumableActive = (String) pref.getModulesActivatedMap().get("mod_consumables_flow");
            if (modConsumableActive == null || "".equals(modConsumableActive)) {
            	modConsumableActive = "N";
            }
        }

		boolean result = true;

		BasicDynaBean bed_operation_schedule_bean = new GenericDAO("bed_operation_schedule").getBean();
		ConversionUtils.copyToDynaBean(requestMap, bed_operation_schedule_bean, errors);

		if (!request.getParameter("hidden_completed").equals("C")) {
			if ((request.getParameter("completed") == null))
				bed_operation_schedule_bean.set("status", "P");
			else
				bed_operation_schedule_bean.set("status", "C");
		}
		String userName = (String)request.getSession(false).getAttribute("userid");
		StringBuilder flashmsg = new StringBuilder();
		result &= otServicesBO.scheduleOrCompleteOperation(bed_operation_schedule_bean, userName, modConsumableActive,
				request.getParameter("startdate")+" "+request.getParameter("operation_time")+":00",
				request.getParameter("enddate")+" "+request.getParameter("expected_end_time")+":00", flashmsg);

		FlashScope flash = FlashScope.getScope(request);


		Boolean addReport = new Boolean(request.getParameter("addReport"));
		ActionRedirect redirect = null;
		String patientId = request.getParameter("patient_id");
		int prescriptionId = Integer.parseInt(request.getParameter("prescribed_id"));
		if (result && addReport) {
			redirect = new ActionRedirect(mapping.findForward("documentslist"));
		} else {
			redirect = new ActionRedirect(mapping.findForward("conductionscreenredirect"));
			redirect.addParameter("visitId", patientId);
			if(!"".equals(flashmsg.toString())) {
				flash.error("Insufficient stock items <br><br>"+flashmsg.toString());
				redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
			}
		}

		redirect.addParameter("prescription_id", prescriptionId);
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
        if (result && bed_operation_schedule_bean.get("status") != null
            && bed_operation_schedule_bean.get("status").equals("C")) {
          interfaceEventMappingService.surgeryEvent(patientId, (int) bed_operation_schedule_bean.get("prescribed_id"));
        }
		return redirect;
	}


	/**
	 * Brings up Modify Ot Consumable screen
	 * with the consumable mapped with the selected operation
	 * @param mapping
	 * @param form
	 * @param request
	 * @param responce
	 * @return
	 * @throws Exception
	 */
	@IgnoreConfidentialFilters
	public ActionForward getPendingrConductScreen(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, SQLException, IOException {
		String mrNo = request.getParameter("mrno");
		ActionRedirect redirect = null;
		List list = otServicesDAO.operationsList(mrNo, false);
		if (list.size() == 1) {
			redirect = new ActionRedirect(mapping.findForward("conductRedirect"));
			redirect.addParameter("prescription_id", ((BasicDynaBean) list.get(0)).get("prescribed_id"));
			redirect.addParameter("visitId", ((BasicDynaBean) list.get(0)).get("patient_id"));
		} else {
			redirect = new ActionRedirect(mapping.findForward("pendingListRedirect"));
			redirect.addParameter("mr_no", mrNo);
			redirect.addParameter("sortOrder", "mr_no");
		}
		return redirect;
	}

	@IgnoreConfidentialFilters
	public ActionForward getOtManagementScreen(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, SQLException, IOException,Exception {
		SectionsDAO formDescDAO = new SectionsDAO();
		String prescribedId = request.getParameter("prescription_id");
		String visitId = request.getParameter("visit_id");
		String opDetailsId = request.getParameter("operation_details_id");
		HttpSession session = request.getSession(false);
		java.util.HashMap urlRightsMap = (java.util.HashMap) session.getAttribute("urlRightsMap");
		java.util.HashMap actionUrlMap = (java.util.HashMap) session.getServletContext().getAttribute("actionUrlMap");
		String billingStatus = "Not Billed";

//		String otRecordPageNum = request.getParameter("otRecordPageNum");
//		otRecordPageNum = (otRecordPageNum == null || otRecordPageNum.isEmpty()) ? null : otRecordPageNum;

		if(urlRightsMap != null && urlRightsMap.get("ot_record").equals("A")){
			request.setAttribute("otrecordrights", "yes");
		}

		boolean operationDetailedScreenRights = urlRightsMap != null
				&& urlRightsMap.get("operation_detailed_screen").equals("A");

		boolean patientIssueRights = urlRightsMap != null
				&& urlRightsMap.get("patient_inventory_issue").equals("A");

		boolean equipmentOrderRights = urlRightsMap != null
				&& urlRightsMap.get("equipment_order").equals("A");

		String issuePageNum = request.getParameter("issueDetailsPageNum");
		PagedList patientIssueDetails = otServicesDAO.getPatientIssueDetails(visitId,issuePageNum,"4");

		String equipPageNum = request.getParameter("equipDetailsPageNum");
		PagedList equipDetails = otServicesDAO.getEquipDetails(visitId,equipPageNum,"4");

		request.setAttribute("operationdetailedscreen", operationDetailedScreenRights);
		request.setAttribute("patientIssueRights", patientIssueRights);
		request.setAttribute("equipOrderRights", equipmentOrderRights);
		request.setAttribute("patientIssueDetails", patientIssueDetails);
		request.setAttribute("equipDetails", equipDetails);

		if(opDetailsId != null && !opDetailsId.isEmpty()) {
			BasicDynaBean operationDetails = null;
			operationDetails = opDetDAO.getOTMiniWindowSurgeryDetails(Integer.parseInt(opDetailsId));
			BasicDynaBean surgeonDetails = opDetDAO.getOtMiniWindowSurgeonDetails(Integer.parseInt(opDetailsId));
			BasicDynaBean anesteatistDetails = opDetDAO.getOtMiniWindowAnesteatistDetails(Integer.parseInt(opDetailsId));
			request.setAttribute("operationDetails", operationDetails);
			request.setAttribute("surgeonDetails", surgeonDetails);
			request.setAttribute("anesteatistDetails", anesteatistDetails);
			request.setAttribute("otRecordForms", OtRecordDAO.getOtRecordForms(visitId, opDetailsId, ConversionUtils.getListingParameter(request.getParameterMap())));
			List<BasicDynaBean> physicianForm = formDescDAO.listAll();
			request.setAttribute("physician_forms", physicianForm);
			request.setAttribute("secondaryOpeartions", OperationDetailsDAO.getCommaSepartedSecondaryOpeartions(Integer.parseInt(opDetailsId)));
			request.setAttribute("anathesia_type_details",
						getAnaethesiaTypeDetailsString(OperationDetailsDAO.getCommaSepartedAnaethesiaTypeDetails(Integer.parseInt(opDetailsId))));
			if(prescribedId == null || prescribedId.isEmpty()) {
				prescribedId = (operationDetails.get("prescribed_id") != null) ? operationDetails.get("prescribed_id").toString() : null;
			}
			if(operationDetails != null) {
				String addedToBill = (String)operationDetails.get("added_to_bill");
				if(addedToBill != null && addedToBill.equals("Y")) {
					billingStatus = "Billed";
				}
			}
			request.setAttribute("billingStatus", billingStatus);
		}
		request.setAttribute("prescribed_id", prescribedId);
		return mapping.findForward("getotmanagementscreen");
	}

	public String getAnaethesiaTypeDetailsString(BasicDynaBean bean) {
		StringBuilder anTypeDetails = new StringBuilder("");
		if (bean != null) {
			String anTypes = (String)bean.get("anaesthesia_types");
			String anTypesFroms= (String)bean.get("anaesthesia_start_datetime");
			String anTypesTos = (String)bean.get("anaesthesia_end_datetime");
			String[] anTypeArr = anTypes.split(",");
			String[] anTypeFromsArr = anTypesFroms.split(",");
			String[] anTypeTosArr = anTypesTos.split(",");
			if (anTypeArr != null && anTypeArr.length > 0) {
				for (int i=0;i<anTypeFromsArr.length;i++) {
					anTypeDetails.append("Anaesthesia Type: ");
					anTypeDetails.append(anTypeArr[i]);
					anTypeDetails.append(",Start: "+anTypeFromsArr[i]);
					anTypeDetails.append(",End: "+anTypeTosArr[i]);
					if(i != anTypeArr.length-1) anTypeDetails.append("/");
				}
			} else {
				anTypeDetails.append("Anaesthesia Type: ");
				anTypeDetails.append(anTypes);
				anTypeDetails.append(",Start: "+anTypesFroms);
				anTypeDetails.append(",End: "+anTypesTos);
			}
		}
		return anTypeDetails.toString();
	}

	@IgnoreConfidentialFilters
	public ActionForward getPatientOperations(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, SQLException, IOException,Exception {
		String patientId = request.getParameter("visit_id");
		String mrNo = request.getParameter("mr_no");
		Map<String,Object> filterMap = new HashMap<String, Object>();
		filterMap.put("patient_id", patientId);

		ActionRedirect redirect = null;
		List<BasicDynaBean> patientOperations = opDetDAO.getPatinetOperations(patientId);
		if(patientOperations != null && patientOperations.size() == 1) {
			redirect = new ActionRedirect(mapping.findForward("otManagementScreenRedirect"));
			redirect.addParameter("visit_id", patientId);
			redirect.addParameter("operation_details_id", patientOperations.get(0).get("operation_details_id"));
			redirect.addParameter("prescribed_id", patientOperations.get(0).get("prescribed_id"));

		} else {
			redirect = new ActionRedirect(mapping.findForward("plannedOperationsListredirect"));
			redirect.addParameter("mr_no", mrNo );
			redirect.addParameter("operation_status", "P");
		}
		return redirect;
	}

	@IgnoreConfidentialFilters
	public ActionForward getPlannedOperationsList(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, SQLException, IOException,Exception {
		Map params = request.getParameterMap();
		JSONSerializer js = new JSONSerializer().exclude("class");
		List<String> columnsList = new ArrayList<String>();
		columnsList.add("op_id");
		columnsList.add("operation_name");
		Map<String,Object> filterMap = new HashMap<String, Object>();
		filterMap.put("status", "A");
    HttpSession session = request.getSession();
    String userName = (String) session.getAttribute("userid");
    int roleId = (Integer) request.getSession().getAttribute("roleId");

		List<BasicDynaBean> userTheatres = TheatreMasterDAO.getUserTheatresList(userName,roleId);
		request.setAttribute("userTheatres",userTheatres);
		request.setAttribute("pagedList", OTServicesDAO.searchPatientPlannedOperations(params,
				ConversionUtils.getListingParameter(params),userName,roleId));
		request.setAttribute("operations", js.serialize(ConversionUtils.listBeanToListMap(operationMasterDAO.listAll(columnsList, filterMap, "operation_name"))));
        request.setAttribute("directBillingPrefs",
            ConversionUtils.listBeanToMapBean(hospDirectBillPrefs.listAll(), "item_type"));
		request.setAttribute("centers", centerDao.getAllCentersExceptSuper());

		return mapping.findForward("plannedoperations");
	}
}
