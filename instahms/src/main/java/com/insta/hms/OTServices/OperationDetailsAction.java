/**
 * mithun.saha
 */
package com.insta.hms.OTServices;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.bob.hms.common.Preferences;
import com.insta.hms.billing.BillDAO;
import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.StringUtil;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.core.billing.AllocationService;
import com.insta.hms.core.scheduler.AppointmentRepository;
import com.insta.hms.insurance.SponsorBO;
import com.insta.hms.integration.configuration.InterfaceEventMappingService;
import com.insta.hms.master.DoctorMaster.DoctorMasterDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDTO;
import com.insta.hms.orders.OrderBO;
import com.insta.hms.stores.StockFIFODAO;

import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.http.HttpHeaders;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @author mithun.saha
 *
 */
public class OperationDetailsAction extends DispatchAction{
	final OTServicesDAO otServicesDAO = new OTServicesDAO();
	final GenericDAO opDAO = new GenericDAO("operation_master");
	final OperationDetailsDAO opDetDAO = new OperationDetailsDAO();
	final GenericDAO procDAO = new GenericDAO("operation_procedures");
	final GenericDAO procResourceDAO = new GenericDAO("operation_team");
	final OperationDetailsBO opDetBO = new OperationDetailsBO();
	final GenericDAO opeBillableResourcesDAO = new GenericDAO("operation_billable_resources");
	final GenericDAO operationDao = new GenericDAO("bed_operation_schedule");
	final GenericDAO bedOpeSecDAO = new GenericDAO("bed_operation_secondary");
	final GenericDAO doctorDAO = new GenericDAO("doctor_consultation");
	final GenericDAO surgandetailsDAO = new GenericDAO("operation_anaesthesia_details");
	private static final GenericDAO patientRegistrationDAO = new GenericDAO("patient_registration");
	AppointmentRepository appointmentRepository = new AppointmentRepository();
	
	private final AllocationService allocationService = (AllocationService) ApplicationContextProvider
	      .getApplicationContext().getBean("allocationService");
	
    private final InterfaceEventMappingService interfaceEventMappingService =
        (InterfaceEventMappingService) ApplicationContextProvider.getApplicationContext()
            .getBean("interfaceEventMappingService");
	
	@IgnoreConfidentialFilters
	public ActionForward getOperationDetailedScreen(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, SQLException, IOException,Exception {
		String visitId = request.getParameter("visitId");
		String orgId = null;
		String mrNo = request.getParameter("mr_no");
		HttpSession session = request.getSession(false);
		java.util.HashMap urlRightsMap = (java.util.HashMap) session.getAttribute("urlRightsMap");
		java.util.HashMap actionUrlMap = (java.util.HashMap) session.getServletContext().getAttribute("actionUrlMap");
		
		Boolean isNewOperation  = new Boolean(request.getParameter("is_new_operation"));
		String opDetailsId = request.getParameter("operation_details_id");
		Integer centerId = (Integer)session.getAttribute("centerId");
		BasicDynaBean patientDetails = null;
		JSONSerializer js = new JSONSerializer().exclude("class");
		if(visitId != null && !visitId.isEmpty())  {
			patientDetails = patientRegistrationDAO.findByKey("patient_id", visitId);
		}
		if(patientDetails != null) {
			centerId = (Integer)patientDetails.get("center_id");
			orgId = (String)patientDetails.get("org_id");
		}
		orgId = (orgId == null || orgId.isEmpty()) ? "ORG0001" : orgId;
		List<BasicDynaBean> scheduledSurgerieAppointments = opDetDAO.getSurgeryAppointments(centerId, mrNo);

		if(opDetailsId != null && !opDetailsId.isEmpty()) {
			BasicDynaBean operationDetails = OperationDetailsDAO.getSurgeryDetails(Integer.parseInt(opDetailsId));
			List<BasicDynaBean> opertaions = null;
			List<BasicDynaBean> sugeons = null;
			List<BasicDynaBean> anaesthetists = null;
			Map<String,List<BasicDynaBean>> procedureResouresMap = new HashMap<String, List<BasicDynaBean>>();
			opertaions = opDetDAO.getRatePlanApplicableOperations(orgId);
			sugeons = opDetDAO.getSurgeons(centerId);
			anaesthetists = opDetDAO.getAnaesthetists(centerId);
			request.setAttribute("operationsJson", js.serialize(ConversionUtils.listBeanToListMap(opertaions)));
			request.setAttribute("surgeonsJosn", js.serialize(ConversionUtils.listBeanToListMap(sugeons)));
			request.setAttribute("anaesthetistsJosn", js.serialize(ConversionUtils.listBeanToListMap(anaesthetists)));
			request.setAttribute("operationDetails", operationDetails);
			if(operationDetails != null) {
				request.setAttribute("patientOperations", opDetDAO.getProcedures(Integer.parseInt(opDetailsId)));
				procedureResouresMap.put("surgeons", opDetDAO.getProcedureResources("surgeon",Integer.parseInt(opDetailsId)));
				procedureResouresMap.put("anestiatists", opDetDAO.getProcedureResources("anestiatist",Integer.parseInt(opDetailsId)));
				procedureResouresMap.put("paediatricians", opDetDAO.getProcedureResources("paediatrician",Integer.parseInt(opDetailsId)));
				procedureResouresMap.put("anaesthesiaTypes", OperationDetailsDAO.getProcedureAnaesthesiaTypes(Integer.parseInt(opDetailsId)));
				request.setAttribute("operationResources", procedureResouresMap);
			}
			request.setAttribute("anaeTypes",opDetDAO.getRatePlanApplicableAnesthesiaTypes(orgId));

			//in multi center scheema theatre must belong to visit center.

			request.setAttribute("userCenterOTLists",opDetDAO.getRatePlanApplicableTheatres(orgId, centerId));
			if (null != patientDetails) {
			  request.setAttribute("regDate", DataBaseUtil.dateFormatter.format(patientDetails.get("reg_date")));
			  request.setAttribute("regTime", patientDetails.get("reg_time"));
			}
			
		}
		request.setAttribute("surAppDetails", scheduledSurgerieAppointments);
		List doctorsList = DoctorMasterDAO.getDoctorDepartmentsDynaList(centerId);
		request.setAttribute("doctorsJSON", js.serialize(ConversionUtils.listBeanToListMap(doctorsList)));
		request.setAttribute("genPrefs", GenericPreferencesDAO.getGenericPreferences());
		return mapping.findForward("getOperationDetailedScreen");
	}

	public ActionForward saveOperationDetails(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, SQLException, IOException,Exception {
		String opDetailsId = request.getParameter("operation_details_id");
		String patientId = request.getParameter("patient_id");
		String orgId = null;
		String appointmentId = request.getParameter("appointment_id");
		BasicDynaBean patientDetails = null;
		HttpSession session = request.getSession(false);
		String userName = (String) session.getAttribute("userId");
		Preferences pref = (Preferences)request.getSession(false).getAttribute("preferences");
		String modConsumableActive = "Y";
		StringBuilder flashmsg = new StringBuilder();

		if ( (pref!=null) && (pref.getModulesActivatedMap() != null) ) {
			modConsumableActive = (String) pref.getModulesActivatedMap().get("mod_consumables_flow");
	        if (modConsumableActive == null || modConsumableActive.equals("")) {
	        	modConsumableActive = "N";
	        }
	    }

		String fixedOtCharges = GenericPreferencesDAO.getGenericPreferences().getFixedOtCharges();
		fixedOtCharges = (fixedOtCharges == null || fixedOtCharges.equals("")) ? "N" : fixedOtCharges;

		if(patientId != null && !patientId.isEmpty())  {
			patientDetails = patientRegistrationDAO.findByKey("patient_id", patientId);
		}

		if(patientDetails != null) {
			orgId = (String)patientDetails.get("org_id");
		}
		orgId = (orgId == null || orgId.isEmpty()) ? "ORG0001" : orgId;
		ActionRedirect redirect  = new ActionRedirect(mapping.findForward("opDetailsRedirect"));
		BasicDynaBean surgeryBean = null;
		boolean success = true;
		FlashScope flash = FlashScope.getScope(request);
		if(appointmentId != null && !appointmentId.isEmpty()) {
			surgeryBean = opDetDAO.findByKey("appointment_id", Integer.parseInt(appointmentId));
		}
		if(surgeryBean != null) {
			flash.error("this appointment is already associated with a patient operation.");
			redirect.addParameter("visitId", patientId);
			redirect.addParameter("prescribed_id", request.getParameter("prescribed_id"));
			redirect.addParameter("mr_no", request.getParameter("mr_no"));
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
			return redirect;
		}
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy k:mm");
		String wheelInDateStr = request.getParameter("wheel_in_date");
		String wheelOutDateStr = request.getParameter("wheel_out_date");
		String wheelInTimeStr = request.getParameter("wheel_in_time");
		String wheelOutTimeStr = request.getParameter("wheel_out_time");
		String surgeryStartDateStr = request.getParameter("surgery_start_date");
		String surgeryEndDateStr = request.getParameter("surgery_end_date");
		String surgeryStartTimeStr = request.getParameter("surgery_start_time");
		String surgeryEndTimeStr = request.getParameter("surgery_end_time");
		Timestamp wheelInDateTime = null;
		Timestamp wheelOutDateTime = null;
		Timestamp surgeryStartDateTime = null;
		Timestamp surgeryEndDateTime = null;
		String[] prescribedIds = request.getParameterValues("prescribed_id");
		String[] opProcedureId = request.getParameterValues("operation_proc_id");
		String[] opProceduresDeleted = request.getParameterValues("op_row_deleted");
		String[] suTeamId = request.getParameterValues("su_operation_team_id");
		String[] anTeamId = request.getParameterValues("an_operation_team_id");
		String[] paedTeamId = request.getParameterValues("paed_operation_team_id");
		String[] anTypeSurgAnDetailId = request.getParameterValues("an_type_surgery_anesthesia_details_id");
		String[] suResourceDeleted = request.getParameterValues("su_row_deleted");
		String[] anResourceDeleted = request.getParameterValues("an_row_deleted");
		String[] paedResourceDeleted = request.getParameterValues("paed_row_deleted");
		String[] anTypeDeleted = request.getParameterValues("an_type_row_deleted");
		String[] operationIds = request.getParameterValues("operation_id");
		String[] suResourceIds = request.getParameterValues("su_resource_id");
		String[] anResourceIds = request.getParameterValues("an_resource_id");
		String[] paedResourceIds = request.getParameterValues("paed_resource_id");
		String[] operationPriority = request.getParameterValues("oper_priority");
		String[] procedureModifier = request.getParameterValues("modifier");
		String[] suOperationSpeciality = request.getParameterValues("su_operation_speciality");
		String[] anOperationSpeciality = request.getParameterValues("an_operation_speciality");
		String[] paedOperationSpeciality = request.getParameterValues("paed_operation_speciality");
		String[] anTypeIds = request.getParameterValues("an_type_anaesthesia_type_id");
		String[] from = request.getParameterValues("an_type_anaesthesia_type_from");
		String[] to = request.getParameterValues("an_type_anaesthesia_type_to");
		List<ProcedureDetails> proceduresInsertList = new ArrayList<ProcedureDetails>();
		List<ProcedureDetails> proceduresUpdateList = new ArrayList<ProcedureDetails>();
		List<ProcedureDetails> proceduresDeleteList = new ArrayList<ProcedureDetails>();
		List<OperationResources> procedureSurgeonsInsertList = new ArrayList<OperationResources>();
		List<OperationResources> procedureSurgeonsUpdateList = new ArrayList<OperationResources>();
		List<OperationResources> procedureSurgeonsDeleteList = new ArrayList<OperationResources>();
		List<OperationResources> procedureAnaesthetistsInsertList = new ArrayList<OperationResources>();
		List<OperationResources> procedureAnaesthetistsUpdateList = new ArrayList<OperationResources>();
		List<OperationResources> procedureAnaesthetistsDeleteList = new ArrayList<OperationResources>();
		List<OperationResources> procedurePaediatricianInsertList = new ArrayList<OperationResources>();
		List<OperationResources> procedurePaediatricianUpdateList = new ArrayList<OperationResources>();
		List<OperationResources> procedurePaediatricianDeleteList = new ArrayList<OperationResources>();
		List<BasicDynaBean> procedureAnaesthesiaTypesInsertList = new ArrayList<BasicDynaBean>();
		List<BasicDynaBean> procedureAnaesthesiaTypesUpdateList = new ArrayList<BasicDynaBean>();
		List<BasicDynaBean> procedureAnaesthesiaTypesDeleteList = new ArrayList<BasicDynaBean>();

		BasicDynaBean opDetBean = null;
		Map<String,Object> keys = new HashMap<String, Object>();
		Connection con = null;

		if(wheelInDateStr != null && !wheelInDateStr.isEmpty()
				&& wheelInTimeStr != null && !wheelInTimeStr.isEmpty()) {
			wheelInDateTime = new Timestamp(((dateFormat.parse(wheelInDateStr+" "+wheelInTimeStr).getTime())));
		}
		if(wheelInDateStr != null && !wheelInDateStr.isEmpty()
				&& wheelOutTimeStr != null && !wheelOutTimeStr.isEmpty()) {
			wheelOutDateTime = new Timestamp((dateFormat.parse(wheelOutDateStr+" "+wheelOutTimeStr).getTime()));
		}
		if(surgeryStartDateStr != null && !surgeryStartDateStr.isEmpty()
				&& surgeryStartTimeStr != null && !surgeryStartTimeStr.isEmpty()) {
			surgeryStartDateTime = new Timestamp((dateFormat.parse(surgeryStartDateStr+" "+surgeryStartTimeStr).getTime()));
		}
		if(surgeryEndDateStr != null && !surgeryEndDateStr.isEmpty()
				&& surgeryEndTimeStr != null && !surgeryEndTimeStr.isEmpty()) {
			surgeryEndDateTime = new Timestamp((dateFormat.parse(surgeryEndDateStr+" "+surgeryEndTimeStr).getTime()));
		}

		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			opDetBean = opDetDAO.getBean();
			ConversionUtils.copyToDynaBean(request.getParameterMap(), opDetBean);
				opDetBean.set("wheel_in_time", wheelInDateTime);
				opDetBean.set("wheel_out_time", wheelOutDateTime);
			if(surgeryStartDateStr != null)
				opDetBean.set("surgery_start", surgeryStartDateTime);
			if(surgeryEndDateStr != null)
				opDetBean.set("surgery_end", surgeryEndDateTime);
			opDetBean.set("prescribing_doctor", request.getParameter("prescribing_doctorId"));
			if(opDetailsId != null && !opDetailsId.isEmpty()) {
				keys.put("operation_details_id", Integer.parseInt(opDetailsId));
				opDetDAO.update(con,opDetBean.getMap(),keys);
			//	prescId = Integer.parseInt(request.getParameter("prescribed_id"));

				if(opProcedureId != null) {
					for(int i=0;i<opProcedureId.length;i++) {
						ProcedureDetails pdt = new ProcedureDetails();
						pdt.setOpDetailsId(Integer.parseInt(opDetailsId));
						pdt.setModifier(procedureModifier[i]);
						pdt.setOperationId(operationIds[i]);
						pdt.setOperationPriority(operationPriority[i]);

						if(opProcedureId[i] != null && !opProcedureId[i].isEmpty()) {
							pdt.setPrescribedId(Integer.parseInt(prescribedIds[i]));
							pdt.setProcedureId(Integer.parseInt(opProcedureId[i]));
							if(opProceduresDeleted[i].equals("N")) {
								pdt.setProcedureId(Integer.parseInt(opProcedureId[i]));
								proceduresUpdateList.add(pdt);
							} else if (opProceduresDeleted[i].equals("Y")) {
								proceduresDeleteList.add(pdt);
							}
						} else {
							if(operationIds != null && operationIds[i] != null && !operationIds[i].isEmpty()) {
								pdt.setProcedureId(procDAO.getNextSequence());
								pdt.setPrescribedId(operationPriority[i].equals("P") ? DataBaseUtil.getNextSequence("ip_operation_sequence") : bedOpeSecDAO.getNextSequence());
								proceduresInsertList.add(pdt);
							}
						}
					}
				}

				opDetBO.updateOperationDetails(proceduresUpdateList, proceduresInsertList, proceduresDeleteList, con);

				if(suTeamId != null) {
					for (int i=0;i<suTeamId.length;i++) {
						OperationResources or = new OperationResources();
						or.setOpDetailsId(Integer.parseInt(opDetailsId));
						or.setOperationSpeciality(suOperationSpeciality[i]);
						or.setResourceId(suResourceIds[i]);
						if(suTeamId[i] != null && !suTeamId[i].isEmpty()) {
							or.setOpTeamId(Integer.parseInt(suTeamId[i]));
							if(suResourceDeleted[i].equals("N")) {
								procedureSurgeonsUpdateList.add(or);
							} else if (suResourceDeleted[i].equals("Y")) {
								procedureSurgeonsDeleteList.add(or);
							}
						} else {
							if(suResourceIds != null && suResourceIds[i] != null && !suResourceIds[i].isEmpty()) {
								or.setOpTeamId(procResourceDAO.getNextSequence());
								procedureSurgeonsInsertList.add(or);
							}
						}
					}
				}

				opDetBO.updateProcedureTeamDetails(procedureSurgeonsUpdateList, procedureSurgeonsInsertList,
						procedureSurgeonsDeleteList, con);

				if(anTeamId != null) {
					for (int i=0;i<anTeamId.length;i++) {
						OperationResources or = new OperationResources();
						or.setOpDetailsId(Integer.parseInt(opDetailsId));
						or.setOperationSpeciality(anOperationSpeciality[i]);
						or.setResourceId(anResourceIds[i]);
						if(anTeamId[i] != null && !anTeamId[i].isEmpty()) {
							or.setOpTeamId(Integer.parseInt(anTeamId[i]));
							if(anResourceDeleted[i].equals("N")) {
								procedureAnaesthetistsUpdateList.add(or);
							} else if (anResourceDeleted[i].equals("Y")) {
								procedureAnaesthetistsDeleteList.add(or);
							}
						} else {
							if(anResourceIds != null && anResourceIds[i] != null && !anResourceIds[i].isEmpty()) {
								or.setOpTeamId(procResourceDAO.getNextSequence());
								procedureAnaesthetistsInsertList.add(or);
							}
						}
					}
				}

				opDetBO.updateProcedureTeamDetails(procedureAnaesthetistsUpdateList, procedureAnaesthetistsInsertList,
						procedureAnaesthetistsDeleteList, con);

				if (anTypeSurgAnDetailId != null ) {
					BasicDynaBean bean = null;
					for (int i=0;i<anTypeSurgAnDetailId.length;i++) {
						bean = surgandetailsDAO.getBean();
						bean.set("operation_details_id", Integer.parseInt(opDetailsId));
						bean.set("anesthesia_type", anTypeIds[i]);
						if(anTypeSurgAnDetailId[i] != null && !anTypeSurgAnDetailId[i].isEmpty()) {
							bean.set("operation_anae_detail_id", Integer.parseInt(anTypeSurgAnDetailId[i]));
							if(anTypeDeleted[i].equals("N")) {
								bean.set("anaes_start_datetime", DateUtil.parseTimestamp(from[i]));
								bean.set("anaes_end_datetime", DateUtil.parseTimestamp(to[i]));
								procedureAnaesthesiaTypesUpdateList.add(bean);
							} else if (anTypeDeleted[i].equals("Y")) {
								procedureAnaesthesiaTypesDeleteList.add(bean);
							}
						} else {
							if(anTypeIds != null && anTypeIds[i] != null && !anTypeIds[i].isEmpty()) {
								bean.set("anaes_start_datetime", DateUtil.parseTimestamp(from[i]));
								bean.set("anaes_end_datetime", DateUtil.parseTimestamp(to[i]));
								bean.set("operation_anae_detail_id", surgandetailsDAO.getNextSequence());
								procedureAnaesthesiaTypesInsertList.add(bean);
							}
						}
					}
				}

				opDetBO.updateProcedureAnaesthesiaDetails(procedureAnaesthesiaTypesUpdateList, procedureAnaesthesiaTypesInsertList,
						procedureAnaesthesiaTypesDeleteList, con);

				if(paedTeamId != null) {
					for (int i=0;i<paedTeamId.length;i++) {
						OperationResources or = new OperationResources();
						or.setOpDetailsId(Integer.parseInt(opDetailsId));
						or.setOperationSpeciality(paedOperationSpeciality[i]);
						or.setResourceId(paedResourceIds[i]);
						if(paedTeamId[i] != null && !paedTeamId[i].isEmpty()) {
							or.setOpTeamId(Integer.parseInt(paedTeamId[i]));
							if(paedResourceDeleted[i].equals("N")) {
								procedurePaediatricianUpdateList.add(or);
							} else if (paedResourceDeleted[i].equals("Y")) {
								procedurePaediatricianDeleteList.add(or);
							}
						} else {
							if(paedResourceIds != null && paedResourceIds[i] != null && !paedResourceIds[i].isEmpty()) {
								or.setOpTeamId(procResourceDAO.getNextSequence());
								procedurePaediatricianInsertList.add(or);
							}
						}
					}
				}

				opDetBO.updateProcedureTeamDetails(procedurePaediatricianUpdateList, procedurePaediatricianInsertList,
						procedurePaediatricianDeleteList, con);

				if(request.getParameter("operation_status") != null && request.getParameter("operation_status").equals("C")) {
					BasicDynaBean opDetailsBean = opDetDAO.getPrimaryOperationDetails(Integer.parseInt(opDetailsId));
					BasicDynaBean opPrescribedBean = operationDao.findByKey(con, "prescribed_id",(Integer)opDetailsBean.get("prescribed_id"));
					if (opPrescribedBean != null) {
						Map opColumndata = new HashMap();
						Map docColumndata = new HashMap();
						opColumndata.put("status", "C");
						docColumndata.put("status", "C");
						operationDao.update(con, opColumndata, "prescribed_id", opDetailsBean != null ? (Integer)opDetailsBean.get("prescribed_id") : null);
						BasicDynaBean bedOpeBean = operationDao.findByKey(con, "prescribed_id", opDetailsBean != null ? (Integer)opDetailsBean.get("prescribed_id") : null);
						doctorDAO.update(con, docColumndata, "common_order_id", bedOpeBean != null ? (Integer)bedOpeBean.get("common_order_id") : null);
						if(opDetailsBean != null && opDetailsBean.get("prescribed_id") != null &&
								!opDetailsBean.get("prescribed_id").toString().isEmpty()) {
							Integer prescribedId = (Integer)opDetailsBean.get("prescribed_id");

							success = OperationDetailsBO.consumeReagents(con, modConsumableActive, Integer.parseInt(opDetailsId), userName, prescribedId,flashmsg);
						}
					}else{
						GenericPreferencesDTO genericPref = GenericPreferencesDAO.getGenericPreferences();
						for(int j = 0;j < prescribedIds.length-1;j++){
						 String prescribed_id = prescribedIds[j];
						 String operation_id = operationIds[j];
						 String operation_type = operationPriority[j];
						     
						 Integer  operation_details_id= (opDetailsId != null && !opDetailsId.isEmpty()) ? Integer.parseInt(opDetailsId) : (Integer)opDetBean.get("operation_details_id");
						 String theatre_id = request.getParameter("theatre_id");
						 List theaterDetails =  new GenericDAO("theatre_master").findAllByKey("theatre_id", theatre_id);
						 Map quantitymap = null;
						 if(theaterDetails.size() > 0){
							 Integer store_id = (Integer) ((BasicDynaBean)theaterDetails.get(0)).get("store_id");
							 quantitymap = OperationDetailsDAO.checkAvailableQuantity(con,operation_details_id,store_id);
						 }
						List required_qty_list = null;
						
						String query ="select medicine_name from store_item_details where medicine_id=?";
						List modify_qty = null;
						    
					  if ( prescribed_id != null && !prescribed_id.isEmpty() ) {//if any operation is added and completed directly with out creating consumable entries
					    modify_qty =  OperationDetailsDAO
					        .getModifyQuantity(con, Integer.parseInt(prescribed_id), operation_id,operation_type);
					  } 
					  
						if(modify_qty != null && modify_qty.size() > 0){//found some consumables saved before completed
							required_qty_list = modify_qty;
						}else{// master defined quantities
							required_qty_list = quantitymap == null || quantitymap.get("required_qty") == null ? new ArrayList() : (List) quantitymap.get("required_qty");
						}
					
						for(int i=0;i<required_qty_list.size();i++){
							BasicDynaBean bean =(BasicDynaBean)required_qty_list.get(i);
							if(bean != null && bean.get("medicine_id") != null && bean.get("qty_needed") != null){
							Integer medicine_id = (Integer)bean.get("medicine_id");
							if(quantitymap.containsKey(medicine_id)){
								if(genericPref.getConsumableStockNegative().equals("N")){
									if(((BigDecimal)quantitymap.get(medicine_id)).doubleValue() < ((BigDecimal)bean.get("qty_needed")).doubleValue() && ((BigDecimal)bean.get("qty_needed")).doubleValue() != 0){
										flashmsg.append(DataBaseUtil.getStringValueFromDb(query, medicine_id)+"<br>");
										success = false;
									}
								}
							}else{
								if(((BigDecimal)bean.get("qty_needed")).doubleValue() != 0){
									flashmsg.append(DataBaseUtil.getStringValueFromDb(query, medicine_id)+"<br>");
									success = false;
								}
							}
						}
						}
						if (!success)
              break;
					}
					}
				}

			} else {
				ProcedureDetails pdt = null;
				OperationResources or = null;
				if(appointmentId != null && !appointmentId.isEmpty()) {
					BasicDynaBean scheduledSurgeryBean = OperationDetailsDAO.getAppointmentProcedureDetails(Integer.parseInt(appointmentId));
					List<BasicDynaBean> schduledSurgeons = OperationDetailsDAO.getAppointmentResourceDetails(Integer.parseInt(appointmentId),new String[]{"SUDOC","ASUDOC"});
					List<BasicDynaBean> schduledAnaestiatists = OperationDetailsDAO.getAppointmentResourceDetails(Integer.parseInt(appointmentId),new String[]{"ANEDOC"});
					List<BasicDynaBean> schduledPaediatricians = OperationDetailsDAO.getAppointmentResourceDetails(Integer.parseInt(appointmentId),new String[]{"PAEDDOC"});

					if(scheduledSurgeryBean != null && (scheduledSurgeryBean.get("operation_id") == null ||
							scheduledSurgeryBean.get("operation_id").equals(""))){
						flash.error("patient does not have operations associated with appointment.");
						redirect.addParameter("visitId", patientId);
						redirect.addParameter("prescribed_id", request.getParameter("prescribed_id"));
						redirect.addParameter("mr_no", request.getParameter("mr_no"));
						redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
						return redirect;
					}

					String operationId = (String)scheduledSurgeryBean.get("operation_id");
					String theatreId = (String)scheduledSurgeryBean.get("theatre_id");

					if(operationId != null && !operationId.isEmpty()) {
						BasicDynaBean ratePlanApplicableOperation = opDetDAO.getRatePlanApplicableOperations(orgId,operationId);
						if(null == ratePlanApplicableOperation) {
							flash.error("scheduled operation is not applicable for patient rateplan.");
							redirect.addParameter("visitId", patientId);
							redirect.addParameter("prescribed_id", request.getParameter("prescribed_id"));
							redirect.addParameter("mr_no", request.getParameter("mr_no"));
							redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
							return redirect;
						}
					}

					if(theatreId != null && !theatreId.isEmpty() && fixedOtCharges.equals("Y")) {
						BasicDynaBean ratePlanApplicableTheatres = opDetDAO.getRatePlanApplicableTheatres(orgId, theatreId);
						if(null == ratePlanApplicableTheatres) {
							flash.error("scheduled theatre is not applicable for patient rateplan.");
							redirect.addParameter("visitId", patientId);
							redirect.addParameter("prescribed_id", request.getParameter("prescribed_id"));
							redirect.addParameter("mr_no", request.getParameter("mr_no"));
							redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
							return redirect;
						}
					}

					opDetBean.set("operation_details_id", opDetDAO.getNextSequence());
				//	opDetBean.set("prescribed_id", DataBaseUtil.getNextSequence("ip_operation_sequence"));
					opDetBean.set("operation_status", "P");
					opDetBean.set("theatre_id", fixedOtCharges.equals("Y") ? null : (String)scheduledSurgeryBean.get("theatre_id"));
					opDetBean.set("surgery_start", fixedOtCharges.equals("Y") ? null : (Timestamp)scheduledSurgeryBean.get("appointment_time"));
					opDetBean.set("surgery_end", fixedOtCharges.equals("Y") ? null :(Timestamp)scheduledSurgeryBean.get("end_appointment_time"));
					opDetBean.set("prescribing_doctor",(String)scheduledSurgeryBean.get("presc_doc_id"));

					Integer prescId = DataBaseUtil.getNextSequence("ip_operation_sequence");
					if(scheduledSurgeryBean != null) {
						pdt = new ProcedureDetails();
						pdt.setProcedureId(procDAO.getNextSequence());
						pdt.setOpDetailsId((Integer)opDetBean.get("operation_details_id"));
						pdt.setOperationId((String)scheduledSurgeryBean.get("operation_id"));
						pdt.setOperationPriority("P");
						pdt.setPrescribedId(prescId);
						proceduresInsertList.add(pdt);
					}

					if(!schduledSurgeons.isEmpty() && schduledSurgeons.size() > 0) {
						for(int i=0;i<schduledSurgeons.size();i++) {
							or = new OperationResources();
							or.setOpTeamId(procResourceDAO.getNextSequence());
							or.setOpDetailsId((Integer)opDetBean.get("operation_details_id"));
							or.setOperationSpeciality(schduledSurgeons.get(i).get("resource_type").equals("SUDOC") ? "SU" : "ASU");
							or.setResourceId((String)schduledSurgeons.get(i).get("resource_id"));
							procedureSurgeonsInsertList.add(or);
						}
					}

					if(!schduledAnaestiatists.isEmpty() && schduledAnaestiatists.size() > 0) {
						for(int i=0;i<schduledAnaestiatists.size();i++) {
							or = new OperationResources();
							or.setOpTeamId(procResourceDAO.getNextSequence());
							or.setOpDetailsId((Integer)opDetBean.get("operation_details_id"));
							or.setOperationSpeciality("AN");
							or.setResourceId((String)schduledAnaestiatists.get(i).get("resource_id"));
							procedureAnaesthetistsInsertList.add(or);
						}
					}

					if(!schduledPaediatricians.isEmpty() && schduledPaediatricians.size() > 0) {
						for(int i=0;i<schduledPaediatricians.size();i++) {
							or = new OperationResources();
							or.setOpTeamId(procResourceDAO.getNextSequence());
							or.setOpDetailsId((Integer)opDetBean.get("operation_details_id"));
							or.setOperationSpeciality("PAED");
							or.setResourceId((String)schduledPaediatricians.get(i).get("resource_id"));
							procedurePaediatricianInsertList.add(or);
						}
					}

				}
				opDetDAO.insert(con, opDetBean);
				opDetBO.updateOperationDetails(proceduresUpdateList, proceduresInsertList, proceduresDeleteList, con);
				opDetBO.updateProcedureTeamDetails(procedureSurgeonsUpdateList, procedureSurgeonsInsertList, procedureSurgeonsDeleteList, con);
				opDetBO.updateProcedureTeamDetails(procedureAnaesthetistsUpdateList, procedureAnaesthetistsInsertList, procedureAnaesthetistsDeleteList, con);
				opDetBO.updateProcedureTeamDetails(procedurePaediatricianUpdateList, procedurePaediatricianInsertList, procedurePaediatricianDeleteList, con);
				BasicDynaBean appointmentBean = appointmentRepository.findByKey("appointment_id", Integer.valueOf(appointmentId));
				Map<String,Object> keyColumn = new HashMap();
				keyColumn.put("appointment_id", Integer.valueOf(appointmentId));
				appointmentBean.set("visit_id", patientId);
				appointmentBean.set("appointment_status", "Arrived");
				appointmentBean.set("arrival_time", DateUtil.getCurrentTimestamp());
				appointmentBean.set("changed_by", userName);
				appointmentBean.set("changed_time", DateUtil.getCurrentTimestamp());
				appointmentRepository.update(appointmentBean, keyColumn);
			}
			
		} finally {
			DataBaseUtil.commitClose(con, success);

			//update stock timestamp
			StockFIFODAO stockFIFODAO = new StockFIFODAO();
			stockFIFODAO.updateStockTimeStamp();
			
            if (success
                && request.getParameter("operation_status") != null
                && "C".equals(request.getParameter("operation_status"))
                && !StringUtil.isNullOrEmpty(request.getParameter("operation_details_id"))) {
              interfaceEventMappingService.surgeryEvent(patientId, Integer.parseInt(request.getParameter("operation_details_id")));
            }
		}
		
		if(!"".equals(flashmsg.toString())) {
			flash.error("Insufficient stock items <br><br>"+flashmsg.toString());
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		}
		
		redirect.addParameter("operation_details_id", (opDetailsId != null && !opDetailsId.isEmpty()) ? opDetailsId : opDetBean.get("operation_details_id"));
		redirect.addParameter("visitId", patientId);
		redirect.addParameter("mr_no", request.getParameter("mr_no"));
		
		return redirect;
	}

	@IgnoreConfidentialFilters
	public ActionForward getAppointmentOperationDetails(ActionMapping am, ActionForm af,
			HttpServletRequest req, HttpServletResponse res)
        throws IOException, ServletException, SQLException,Exception {
		String appointmentId = req.getParameter("appointment_id");
		JSONSerializer js = new JSONSerializer().exclude("class");
	    BasicDynaBean appOperationDetails = null;
        List<BasicDynaBean> appSurgeonDetails =null;
        List<BasicDynaBean> appAnestiatistDetails = null;
        List<BasicDynaBean> appPaediatricianDetails = null;

        if(appointmentId != null && !appointmentId.isEmpty()) {
	        appOperationDetails = OperationDetailsDAO.getAppointmentProcedureDetails(Integer.parseInt(appointmentId));
	        appSurgeonDetails = OperationDetailsDAO.getAppointmentResourceDetails(Integer.parseInt(appointmentId),new String[]{"SUDOC","ASUDOC"});
	        appAnestiatistDetails = OperationDetailsDAO.getAppointmentResourceDetails(Integer.parseInt(appointmentId),new String[]{"ANEDOC"});
	        appPaediatricianDetails = OperationDetailsDAO.getAppointmentResourceDetails(Integer.parseInt(appointmentId),new String[]{"PAEDDOC"});
        }

	    Map<String,Object> appSurgeryDetails = new HashMap<String, Object>();
	    if(null != appOperationDetails) {
        appSurgeryDetails.put("procedureDetails", appOperationDetails.getMap());
	    }
        appSurgeryDetails.put("surgeonDetails", ConversionUtils.listBeanToListMap(appSurgeonDetails));
        appSurgeryDetails.put("anestiatistDetails", ConversionUtils.listBeanToListMap(appAnestiatistDetails));
        appSurgeryDetails.put("paediatricianDetails", ConversionUtils.listBeanToListMap(appPaediatricianDetails));

		res.setContentType("text/plain");
        res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
        res.getWriter().write(js.deepSerialize(appSurgeryDetails));
        res.flushBuffer();
        return null;
	}

	@IgnoreConfidentialFilters
	public ActionForward getAddToBillScreen(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws SQLException,Exception{
		String operationDetailsId = request.getParameter("operation_details_id");
		String visitId = request.getParameter("visit_id");
		BasicDynaBean opeartionBean = null;
		if(operationDetailsId != null && !operationDetailsId.isEmpty()) {
			opeartionBean = opDetDAO.getOperationDetailsBean(Integer.parseInt(operationDetailsId));
			List<BasicDynaBean> billItems = opDetDAO.getOperationBillItems(Integer.parseInt(operationDetailsId));

			request.setAttribute("billItems", ConversionUtils.listBeanToListMap(billItems));
			request.setAttribute("operations", ConversionUtils.listBeanToListMap(OperationDetailsDAO.getProcedures
						(Integer.parseInt(operationDetailsId))));
			request.setAttribute("operationStatus", opeartionBean.get("operation_status"));
		}
		request.setAttribute("visit_id", visitId);
		request.setAttribute("operation_details_id", operationDetailsId);
		request.setAttribute("operationDetails", opeartionBean);
		request.setAttribute("genPrefs", GenericPreferencesDAO.getGenericPreferences());

		return mapping.findForward("addToBillScreen");
	}

	public ActionForward saveOperationBillableResources(ActionMapping am,
			ActionForm af, HttpServletRequest req, HttpServletResponse res) throws Exception{

		BasicDynaBean opDetBean = null;
		Connection con = null;
		boolean success = false;
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			saveBillableItems(req, con);
			opDetBean = opDetDAO.getPrimaryOperationDetails(Integer.parseInt(req.getParameter("operation_details_id")));
			success = true;
		}finally{
			DataBaseUtil.commitClose(con, success);
		}
		ActionRedirect redirect = new ActionRedirect(am.findForward("addtoBillRedirect"));
		redirect.addParameter("operation_details_id", req.getParameter("operation_details_id"));
		redirect.addParameter("visit_id", req.getParameter("visit_id"));
		redirect.addParameter("operation_details_id", req.getParameter("operation_details_id"));
		redirect.addParameter("prescribed_id", opDetBean.get("prescribed_id"));
		return redirect;
	}

	public boolean saveBillableItems(HttpServletRequest req,Connection con) throws Exception{
		Map<String,List<BasicDynaBean>> opProcResourceMap= new HashMap<String,List<BasicDynaBean>>();
		String[] operationProcAndResourceIds = req.getParameterValues("billable");
		String[] preAuthNo = req.getParameterValues("prior_auth_id");
		String[] preAuthMode = req.getParameterValues("prior_auth_mode_id");
		String[] preAuthReq = req.getParameterValues("prior_auth_required");
		String[] opeProcIds = req.getParameterValues("opeProcId");
		String opeDetId = req.getParameter("operation_details_id");
		boolean success = false;

		if(opeProcIds != null && opeProcIds.length > 0){
			for(int j=0; j<opeProcIds.length; j++)
				opeBillableResourcesDAO.delete(con, "operation_proc_id", Integer.parseInt(opeProcIds[j]));
		}

		if(operationProcAndResourceIds != null && operationProcAndResourceIds.length > 0) {
			for(int i=0; i<operationProcAndResourceIds.length;i++) {
				String[] splitValues  = operationProcAndResourceIds[i].split("-");
				List<BasicDynaBean> resourcesList = opProcResourceMap.get(splitValues[0]);
				BasicDynaBean bean = opeBillableResourcesDAO.getBean();
				bean.set("resource_id", splitValues[1]);
				bean.set("operation_proc_id", Integer.parseInt(splitValues[0]));
				bean.set("billable", "Y");
				bean.set("resource_type", splitValues[2]);
				if(resourcesList == null) {
					resourcesList = new ArrayList<BasicDynaBean>();
				}
				resourcesList.add(bean);
				opProcResourceMap.put(splitValues[0], resourcesList);
			}
			opDetBO.saveOperationBillableResources(con, opProcResourceMap);

		}

		boolean modAdvIns = (Boolean) req.getSession(false).getAttribute("mod_adv_ins");
		if(modAdvIns && opeProcIds != null) {
			for(int j=0; j<opeProcIds.length; j++) {
				if(!preAuthReq[j].equals("N")) {
					BasicDynaBean bean = procDAO.findByKey(con, "operation_proc_id",Integer.parseInt(opeProcIds[j]));
					bean.set("prior_auth_id", preAuthNo[j]);
					if(null != preAuthMode[j] && !"".equals(preAuthMode[j]))
						bean.set("prior_auth_mode_id", Integer.parseInt(preAuthMode[j]));
					else
						bean.set("prior_auth_mode_id", null);
					procDAO.update(con, bean.getMap(), "operation_proc_id", Integer.parseInt(opeProcIds[j]));
				}
			}
		}
		BasicDynaBean opeDetBean = opDetDAO.findByKey(con, "operation_details_id", Integer.parseInt(opeDetId));
		String orderRemarks = req.getParameter("order_remarks");
		opeDetBean.set("order_remarks", orderRemarks);
		opDetDAO.update(con, opeDetBean.getMap(), "operation_details_id", Integer.parseInt(opeDetId));
		success = true;

		return success;

	}

	public ActionForward AddBillableItemsToBill(ActionMapping am,
			ActionForm af, HttpServletRequest req, HttpServletResponse res) throws Exception{
		Connection con = null;
		StringBuilder flashmsg = new StringBuilder();
		String msg = null;
		String[] operationProcAndResourceIds = req.getParameterValues("billable");
		BasicDynaBean opDetBean = null;
		ActionRedirect redirect = new ActionRedirect(am.findForward("addtoBillRedirect"));
		FlashScope flash = FlashScope.getScope(req);
		String opDetailsId = req.getParameter("operation_details_id");
		boolean success = false;
		OrderBO order = new OrderBO();
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			saveBillableItems(req, con);

			if(operationProcAndResourceIds != null && operationProcAndResourceIds.length > 0)
				success = addOperationToBill(order,req, con, Integer.parseInt(opDetailsId), false, true,flashmsg);

			if(!"".equals(flashmsg.toString())){
				flash.error("Insufficient stock items <br><br>"+flashmsg.toString());
				redirect.addParameter("operation_details_id", opDetailsId);
				redirect.addParameter("visit_id", req.getParameter("visit_id"));
				redirect.addParameter("prescribed_id", req.getParameter("prescribed_id"));
				redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
				success = false;
				return redirect;
			}
			opDetBean = opDetDAO.getPrimaryOperationDetails(Integer.parseInt(opDetailsId));
			
		} finally {
			
			DataBaseUtil.commitClose(con, success);
			if(success){
				new SponsorBO().recalculateSponsorAmount(req.getParameter("visit_id"));
				String billNo = (String)order.getBill().get("bill_no");
				BillDAO.resetSponsorTotals(billNo);
				// Update the bill total amount.
		        allocationService.updateBillTotal(billNo);
		        int centerId = (Integer) req.getSession().getAttribute("centerId");
		        // Call the Allocation method.
		        allocationService.allocate(billNo, centerId);
			}
			
		}

		redirect.addParameter("operation_details_id", opDetailsId);
		redirect.addParameter("visit_id", req.getParameter("visit_id"));
		redirect.addParameter("prescribed_id", opDetBean.get("prescribed_id"));
		return redirect;

	}

	public boolean addOperationToBill(OrderBO order,HttpServletRequest request,
			Connection con, Integer opDetailsId, Boolean allowClosedBills,
			boolean chargable,StringBuilder flashmsg) throws Exception {
		HttpSession session = request.getSession(false);
		String userName = (String) session.getAttribute("userId");
		Preferences pref = (Preferences)request.getSession(false).getAttribute("preferences");
		String modConsumableActive = "Y";
		boolean status = true;
		if ( (pref!=null) && (pref.getModulesActivatedMap() != null) ) {
			modConsumableActive = (String) pref.getModulesActivatedMap().get("mod_consumables_flow");
	        if (modConsumableActive == null || "".equals(modConsumableActive)) {
	        	modConsumableActive = "N";
	        }
	    }
		String msg = order.orderOperation(order,con, chargable, null, opDetailsId, userName, allowClosedBills);
		if(msg == null) {
			BasicDynaBean opDetailsBean = opDetDAO.getPrimaryOperationDetails(opDetailsId);
			BasicDynaBean bedOpeBean = operationDao.findByKey(con, "prescribed_id", opDetailsBean != null ? (Integer)opDetailsBean.get("prescribed_id") : null);
			Map opColumndata = new HashMap();
			Map docColumndata = new HashMap();
			opColumndata.put("remarks", opDetailsBean != null ? opDetailsBean.get("order_remarks") : null);
			opColumndata.put("status", "C");
			docColumndata.put("remarks", opDetailsBean != null ? opDetailsBean.get("order_remarks") : null);
			operationDao.update(con, opColumndata, "prescribed_id", opDetailsBean != null ? (Integer)opDetailsBean.get("prescribed_id") : null);
			doctorDAO.update(con, docColumndata, "common_order_id", bedOpeBean != null ? (Integer)bedOpeBean.get("common_order_id") : null);

			if(opDetailsBean != null && opDetailsBean.get("operation_status").equals("C")) {
				if(opDetailsBean.get("prescribed_id") != null && !opDetailsBean.get("prescribed_id").toString().isEmpty())
					status &= OperationDetailsBO.consumeReagents(con, modConsumableActive, opDetailsId, userName, (Integer)opDetailsBean.get("prescribed_id"),flashmsg);
			}
			Map opdetailsColumndata = new HashMap();
			opdetailsColumndata.put("added_to_bill", "Y");
			status &= (opDetDAO.update(con, opdetailsColumndata, "operation_details_id", opDetailsId) > 0);
		}

		return status;
	}
}
