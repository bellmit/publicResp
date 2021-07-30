/**
 * mithun.saha
 */
package com.insta.hms.OTServices;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.otmasters.opemaster.OperationMasterDAO;
import com.insta.hms.Registration.PatientInsurancePlanDAO;
import com.insta.hms.billing.ChargeDTO;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.master.DoctorMaster.DoctorMasterDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.orders.OrderBO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author mithun.saha
 *
 */
public class OperationDetailsBO {
	OperationDetailsDAO opDetDAO = new OperationDetailsDAO();
	
	GenericDAO opeBillableResourcesDAO = new GenericDAO("operation_billable_resources");
	GenericDAO doctorsDAO = new GenericDAO("doctor_consultation");
	GenericDAO procDAO = new GenericDAO("operation_procedures");
	GenericDAO procResourceDAO = new GenericDAO("operation_team");
	
	private static final GenericDAO bedOpeSecondaryDAO = new GenericDAO("bed_operation_secondary");

	public boolean updateOperationDetails(List<ProcedureDetails> procedureUpdateList,List<ProcedureDetails> procedureInsertList,
			List<ProcedureDetails> procedureDeleteList,Connection con) throws Exception {
		boolean success = true;
		if(!procedureDeleteList.isEmpty()) {
			success = opDetDAO.deleteProcedures(procedureDeleteList, con);
		}

		if (!procedureUpdateList.isEmpty()) {
			success = opDetDAO.updateProcedures(procedureUpdateList, con);
		}

		if (!procedureInsertList.isEmpty()) {
			success = opDetDAO.insertProcedures(procedureInsertList, con);
		}

		return success;
	}

	public boolean updateProcedureTeamDetails(List<OperationResources> preocedureResourcesUpdateList,List<OperationResources> preocedureResourcesInsertList,
			List<OperationResources> preocedureResourcesDeleteList,Connection con) throws Exception {
		boolean success = true;
		if(!preocedureResourcesDeleteList.isEmpty()) {
			success = opDetDAO.deleteProceduerResources(preocedureResourcesDeleteList, con);
		}

		if (!preocedureResourcesUpdateList.isEmpty()) {
			success = opDetDAO.updateProcedureResources(preocedureResourcesUpdateList, con);
		}

		if (!preocedureResourcesInsertList.isEmpty()) {
			success = opDetDAO.insertProcedureResources(preocedureResourcesInsertList, con);
		}

		return success;
	}

	public boolean updateProcedureAnaesthesiaDetails(List<BasicDynaBean> procedureAnaesthesiaTypesUpdateList,List<BasicDynaBean> procedureAnaesthesiaTypesInsertList,
			List<BasicDynaBean> procedureAnaesthesiaTypesDeleteList,Connection con) throws Exception {
		boolean success = true;
		if(!procedureAnaesthesiaTypesDeleteList.isEmpty()) {
			success = opDetDAO.deleteProcedureAnaesthesiaTypes(procedureAnaesthesiaTypesDeleteList, con);
		}

		if (!procedureAnaesthesiaTypesUpdateList.isEmpty()) {
			success = opDetDAO.updateProcedureAnaesthesiaTypes(procedureAnaesthesiaTypesUpdateList, con);
		}

		if (!procedureAnaesthesiaTypesInsertList.isEmpty()) {
			success = opDetDAO.insertProcedureAnaesthesiaTypes(procedureAnaesthesiaTypesInsertList, con);
		}

		return success;
	}

	public boolean saveOperationBillableResources(Connection con,Map<String,List<BasicDynaBean>> opProcResourceMap) throws Exception{
		boolean success = false;
		Iterator it = opProcResourceMap.entrySet().iterator();
		while (it.hasNext()) {
	        Map.Entry pairs = (Map.Entry)it.next();
	        String key = (String)pairs.getKey();
	        List<BasicDynaBean> valueBean = (List<BasicDynaBean>)pairs.getValue();
	        for(BasicDynaBean bean : valueBean) {
	        	bean.set("operation_billable_resources_id", opeBillableResourcesDAO.getNextSequence());
	        	success = opeBillableResourcesDAO.insert(con, bean);
	        }
	    }
		return success;
	}

	public BasicDynaBean getOrderBean(BasicDynaBean operationBean,BasicDynaBean primarySurgeryDetails,String resourceId,BasicDynaBean operationTeam,
			String userName,Timestamp prescribedDate, Integer operationRef) throws Exception{
		BasicDynaBean orderBean = doctorsDAO.getBean();
		String opSpeciality = (String)operationTeam.get("operation_speciality");

		if(opSpeciality != null && opSpeciality.equals("SU")) {
			opSpeciality = "SUOPE";
		} else if (opSpeciality != null && opSpeciality.equals("AN")) {
			opSpeciality = "ANAOPE";
		} else if (opSpeciality != null && opSpeciality.equals("ASU")) {
			opSpeciality = "ASUOPE";
		} else if (opSpeciality != null && opSpeciality.equals("ASAN")) {
			opSpeciality = "AANOPE";
		} else if (opSpeciality != null && opSpeciality.equals("COSOPE")) {
			opSpeciality = "COSOPE";
		} else if(opSpeciality != null && opSpeciality.equals("PAED")) {
			opSpeciality = "IPDOC";
		}
		orderBean.set("mr_no", operationBean.get("mr_no"));
		orderBean.set("patient_id", operationBean.get("patient_id"));
		orderBean.set("doctor_name", resourceId);
		orderBean.set("visited_date", prescribedDate);
		orderBean.set("remarks", null);
		orderBean.set("cancel_status", null);
		orderBean.set("operation_ref", operationRef);
		orderBean.set("ot_doc_role",opSpeciality);
		orderBean.set("head",opSpeciality);
		orderBean.set("status","U");
		orderBean.set("appointment_id",0);
		orderBean.set("username",userName);
		orderBean.set("presc_date",prescribedDate);
		orderBean.set("common_order_id", operationBean.get("common_order_id"));
		orderBean.set("presc_doctor_id",primarySurgeryDetails.get("prescribing_doctor"));
		orderBean.set("remarks", primarySurgeryDetails.get("order_remarks"));
		return orderBean;
	}

	public BasicDynaBean getOperationDetailsBean(BasicDynaBean operationBean,BasicDynaBean primarySurgeryDetails,
			BasicDynaBean primarySurgeonBean,BasicDynaBean primaryAnestiatistBean,String userName,
			boolean condApplicable,Timestamp prescribedDate) throws Exception{
		operationBean.set("mr_no", primarySurgeryDetails.get("mr_no"));
		operationBean.set("patient_id", primarySurgeryDetails.get("patient_id"));
		operationBean.set("operation_name", primarySurgeryDetails.get("operation_id"));
		operationBean.set("theatre_name", primarySurgeryDetails.get("theatre_id"));
		operationBean.set("department", "");
		operationBean.set("status", "N");
		operationBean.set("status", condApplicable ? "N" : "U");
		operationBean.set("start_datetime", primarySurgeryDetails.get("surgery_start"));
		operationBean.set("end_datetime", primarySurgeryDetails.get("surgery_end"));
		operationBean.set("prescribed_id", primarySurgeryDetails.get("prescribed_id"));
		operationBean.set("hrly", primarySurgeryDetails.get("charge_type") != null ?
					primarySurgeryDetails.get("charge_type").equals("H") ? "checked" : null : null);
		operationBean.set("common_order_id", DataBaseUtil.getNextSequence("common_order_seq"));
		operationBean.set("surgeon",primarySurgeonBean != null ? primarySurgeonBean.get("resource_id") : null);
		operationBean.set("anaesthetist",primaryAnestiatistBean != null ? primaryAnestiatistBean.get("resource_id") : null);
	//	operationBean.set("anesthesia_type", primarySurgeryDetails.get("anaesthesia_type"));
		operationBean.set("user_name", userName);
		operationBean.set("appointment_id", primarySurgeryDetails.get("appointment_id"));
		operationBean.set("remarks", null);
		operationBean.set("prescribed_date", prescribedDate);
		operationBean.set("finalization_status", "Y");

		return operationBean;
	}

	public BasicDynaBean getBedSecondaryBean(BasicDynaBean bedOpeSecondaryBean,BasicDynaBean secondaryProcBean,BasicDynaBean orderOperationBean) throws Exception{
		bedOpeSecondaryBean.set("sec_prescribed_id", secondaryProcBean.get("main_prescribed_id"));
		bedOpeSecondaryBean.set("prescribed_id", orderOperationBean.get("prescribed_id"));
		bedOpeSecondaryBean.set("operation_id", secondaryProcBean.get("operation_id"));
		bedOpeSecondaryBean.set("remarks", orderOperationBean.get("remarks"));

		return bedOpeSecondaryBean;
	}

	public void orderDoctor(Connection con, BasicDynaBean orderBean, boolean chargeable, Boolean firstOfCategory,
				OrderBO order,String patientId,String operationId) throws Exception {
		orderBean.set("consultation_id", DataBaseUtil.getNextSequence("doctor_consultation_sequence"));
		BasicDynaBean opMasterBean = null;

		if (null != orderBean.get("operation_ref") ) {
			// the doctor charges belong to an operation: we need operation master info also
			// to be able to get the doctor charges.
			opMasterBean = new OperationMasterDAO().getOperationChargeBean(operationId,order.getBedType(),order.getBillRatePlanId());
		}

		String consType = (String) orderBean.get("head");
		BasicDynaBean consTypeBean = null;
		order.setPlanIds(new PatientInsurancePlanDAO().getPlanIds(con,patientId));

		// for ot related doctor consultation insertions, set status to U to indicate that
		// conduction is unnecessary.
		String otDocRole = (String) orderBean.get("ot_doc_role");
		if (otDocRole != null && !otDocRole.equals("")) {
			orderBean.set("status", "U");
		}
		doctorsDAO.insert(con, orderBean);

		if (chargeable) {
			BasicDynaBean doctor = DoctorMasterDAO.getDoctorCharges((String) orderBean.get("doctor_name"),
					order.getBillRatePlanId(), order.getBedType());

			List<ChargeDTO> charges = null;
			if (opMasterBean != null) {
				// This is an operation related doctor order.
				charges = OrderBO.getOtDoctorCharges(doctor, otDocRole,order.getVisitType(),
					opMasterBean, BigDecimal.ONE,
					order.isInsurance(), order.getPlanIds(), order.getBedType(), patientId, firstOfCategory);
			}

			order.insertOrderCharges(con, charges,
					"DOC", (Integer) orderBean.get("consultation_id"),
					(String) orderBean.get("remarks"), (String) orderBean.get("presc_doctor_id"),
					(Timestamp) orderBean.get("presc_date"), "Y",
					(Timestamp) orderBean.get("visited_date"),
					(Integer) orderBean.get("common_order_id"),null,null);

		}
	}

	public static boolean consumeReagents(Connection con,String modConsumableActive,Integer opDetailsId,
			String userName,Integer prescribedId,StringBuilder flashmsg) throws Exception {
		boolean status = true;
		if(modConsumableActive.equals("Y")) {
			BasicDynaBean primaryProcedure = OperationDetailsDAO.getPrimaryOperationDetails(opDetailsId);
			List<BasicDynaBean> secondaryProcedures = OperationDetailsDAO.getSecondaryOperationDetails(con,prescribedId);
			String operationName = (String)primaryProcedure.get("operation_id");
			BasicDynaBean opPrescribedBean = new GenericDAO("bed_operation_schedule").findByKey(con, "prescribed_id",prescribedId);
			if(opPrescribedBean != null) {
				Boolean reagentStockReduced = (Boolean)opPrescribedBean.get("stock_reduced");

				if(!reagentStockReduced)
					status &= OTServicesBO.consumeReagents(con, opPrescribedBean, userName, operationName,true,flashmsg);

				for(int i=0; i<secondaryProcedures.size();i++) {
					BasicDynaBean secondaryProcedureBean = secondaryProcedures.get(i);
					String opName = (String) secondaryProcedureBean.get("operation_id");
					Boolean stockReduced = (Boolean)secondaryProcedureBean.get("stock_reduced");

					if(!stockReduced) {
						BasicDynaBean secondaryOperationBean = bedOpeSecondaryDAO.getBean();
						Integer sec_prescribed_id = (Integer)secondaryProcedureBean.get("sec_prescribed_id");
						opPrescribedBean.set("prescribed_id", sec_prescribed_id);
						status &= OTServicesBO.consumeReagents(con, opPrescribedBean, userName, opName,false,flashmsg);
						secondaryOperationBean.set("stock_reduced", new Boolean(true));
						bedOpeSecondaryDAO.update(con, secondaryOperationBean.getMap(), "sec_prescribed_id",sec_prescribed_id);
					}
				}
			}
		}
		
		return status;
	}

	public String saveSurgeryAppointmnetToOpertionDetails(Connection con,Integer appointmentId,String orgId) throws Exception {
		String msg = null;
		BasicDynaBean scheduledSurgeryBean = OperationDetailsDAO.getAppointmentProcedureDetails(con,appointmentId);
		List<BasicDynaBean> schduledSurgeons = OperationDetailsDAO.getAppointmentResourceDetails(con,appointmentId,new String[]{"SUDOC","ASUDOC"});
		List<BasicDynaBean> schduledAnaestiatists = OperationDetailsDAO.getAppointmentResourceDetails(con,appointmentId,new String[]{"ANEDOC"});
		List<BasicDynaBean> schduledPaediatricians = OperationDetailsDAO.getAppointmentResourceDetails(con,appointmentId, new String[]{"PAEDDOC"});
		BasicDynaBean opDetBean = null;
		ProcedureDetails pdt = null;
		OperationResources or = null;
		List<ProcedureDetails> proceduresInsertList = new ArrayList<ProcedureDetails>();
		List<OperationResources> procedureSurgeonsInsertList = new ArrayList<OperationResources>();
		List<OperationResources> procedureAnaesthetistsInsertList = new ArrayList<OperationResources>();
		List<OperationResources> procedurePaediatricianInsertList = new ArrayList<OperationResources>();

		String fixedOtCharges = GenericPreferencesDAO.getGenericPreferences().getFixedOtCharges();
		fixedOtCharges = (fixedOtCharges == null || fixedOtCharges.equals("")) ? "N" : fixedOtCharges;

		if(scheduledSurgeryBean != null && (scheduledSurgeryBean.get("operation_id") == null ||
				scheduledSurgeryBean.get("operation_id").toString().equals(""))){
			msg = "registration.patient.action.message.error.patient.does.not.have.operations.associated.with.appointment";
		}

		String operationId = (String)scheduledSurgeryBean.get("operation_id");
		String theatreId = (String)scheduledSurgeryBean.get("theatre_id");

		if(operationId != null && !operationId.isEmpty()) {
			BasicDynaBean ratePlanApplicableOperation = OperationDetailsDAO.getRatePlanApplicableOperations(orgId,operationId);
			if(null == ratePlanApplicableOperation) {
				msg = "registration.patient.action.message.error.scheduled.operation.is.not.applicable.for.patient.rate.plan";
			}
		}

		if(theatreId != null && !theatreId.isEmpty() && fixedOtCharges.equals("Y")) {
			BasicDynaBean ratePlanApplicableTheatres = OperationDetailsDAO.getRatePlanApplicableTheatres(orgId, theatreId);
			if(null == ratePlanApplicableTheatres) {
				msg = "registration.patient.action.message.error.scheduled.theatre.is.not.applicable.for.patient.rateplan";
			}
		}

		if(scheduledSurgeryBean != null) {
			opDetBean = opDetDAO.getBean();
			opDetBean.set("mr_no", scheduledSurgeryBean.get("mr_no"));
			opDetBean.set("patient_id", scheduledSurgeryBean.get("visit_id"));
			opDetBean.set("operation_details_id", opDetDAO.getNextSequence());
			opDetBean.set("theatre_id", fixedOtCharges.equals("Y") ? null : (String)scheduledSurgeryBean.get("theatre_id"));
			opDetBean.set("surgery_start", fixedOtCharges.equals("Y") ? null : (Timestamp)scheduledSurgeryBean.get("appointment_time"));
			opDetBean.set("surgery_end", fixedOtCharges.equals("Y") ? null :(Timestamp)scheduledSurgeryBean.get("end_appointment_time"));
			opDetBean.set("appointment_id", appointmentId);
			opDetBean.set("operation_status", "P");
			opDetBean.set("prescribing_doctor", scheduledSurgeryBean.get("presc_doc_id"));

		}

		if(scheduledSurgeryBean != null) {
			pdt = new ProcedureDetails();
			pdt.setProcedureId(procDAO.getNextSequence());
			pdt.setOpDetailsId((Integer)opDetBean.get("operation_details_id"));
			pdt.setOperationId((String)scheduledSurgeryBean.get("operation_id"));
			pdt.setOperationPriority("P");
			pdt.setPriorAuthId((String)scheduledSurgeryBean.get("scheduler_prior_auth_no"));
			pdt.setPriorAuthModeId((Integer)scheduledSurgeryBean.get("scheduler_prior_auth_mode_id"));
			pdt.setPrescribedId(DataBaseUtil.getNextSequence("ip_operation_sequence"));
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

		opDetDAO.insert(con, opDetBean);
		updateOperationDetails(new ArrayList<ProcedureDetails>(), proceduresInsertList, new ArrayList<ProcedureDetails>(), con);
		updateProcedureTeamDetails(new ArrayList<OperationResources>(), procedureSurgeonsInsertList, new ArrayList<OperationResources>(), con);
		updateProcedureTeamDetails(new ArrayList<OperationResources>(), procedureAnaesthetistsInsertList, new ArrayList<OperationResources>(), con);
		updateProcedureTeamDetails(new ArrayList<OperationResources>(), procedurePaediatricianInsertList, new ArrayList<OperationResources>(), con);

		return msg;
	}
}
