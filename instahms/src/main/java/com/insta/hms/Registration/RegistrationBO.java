package com.insta.hms.Registration;

import com.bob.hms.common.Constants;
import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.orders.OrderBO;
import com.insta.hms.billing.Bill;
import com.insta.hms.billing.BillBO;
import com.insta.hms.billing.BillChargeClaimDAO;
import com.insta.hms.billing.BillDAO;
import com.insta.hms.billing.BillPrintHelper;
import com.insta.hms.billing.ChargeDAO;
import com.insta.hms.billing.ChargeDTO;
import com.insta.hms.billing.DepositsDAO;
import com.insta.hms.billing.DiscountPlanBO;
import com.insta.hms.billing.Receipt;
import com.insta.hms.billing.paymentdetails.AbstractPaymentDetails;
import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.MailService;
import com.insta.hms.documentpersitence.AbstractDocumentPersistence;
import com.insta.hms.insurance.InsuranceDAO;
import com.insta.hms.integration.hl7.message.v23.ADTService;
import com.insta.hms.master.AreaMaster.AreaMasterDAO;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDTO;
import com.insta.hms.master.HealthAuthorityPreferences.HealthAuthorityPreferencesDAO;
import com.insta.hms.master.PatientCategory.PatientCategoryDAO;
import com.insta.hms.master.RegistrationPreferences.RegistrationPreferencesDAO;
import com.insta.hms.messaging.MessageManager;
import com.insta.hms.outpatient.PatientPrescriptionDAO;
import com.insta.hms.outpatient.PrescribeDAO;
import com.insta.hms.outpatient.PrescriptionBO;
import com.insta.hms.resourcescheduler.ResourceBO;
import com.insta.hms.resourcescheduler.ResourceDAO;
import com.insta.hms.usermanager.PasswordEncoder;

import eu.medsea.mimeutil.MimeUtil2;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.upload.FormFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import jlibs.core.util.regex.TemplateMatcher;

public class RegistrationBO {

  private static final RegistrationPreferencesDAO regPerfDAO = new RegistrationPreferencesDAO();
  private static final GenericDAO mrdCaseFileAttributesDAO =
      new GenericDAO("mrd_casefile_attributes");
  private static final GenericDAO planDocsDetailsDAO = new GenericDAO("plan_docs_details");
  private static final GenericDAO patientDocumentsDAO = new GenericDAO("patient_documents");
  private static final GenericDAO patientInsurancePlansDAO =
      new GenericDAO("patient_insurance_plans");
  
	/**
	 * The following methods define various transactions required for registering a patient.
	 */

	// Validate Mrno
	public boolean validateMrno(String mrno, String visitId) throws Exception {
		if (mrno == null || mrno.trim().equals(""))
			return false;
		if (visitId == null || visitId.trim().equals(""))
			return true;
		BasicDynaBean patientDetailsBean = PatientDetailsDAO.getPatientGeneralDetailsBean(mrno);
		BasicDynaBean visitDetailsBean = new VisitDetailsDAO().findByKey("patient_id", visitId);
		if (patientDetailsBean != null && visitDetailsBean != null
				&& (patientDetailsBean.get("mr_no")).equals(visitDetailsBean.get("mr_no"))) {
			return true;
		}
		return false;
	}

	// Generate Mrno
	public String generateMrno(BasicDynaBean patientDetailsBean, String regType, String paramMrno)
										throws SQLException, ParseException {
		String mrNo = null;
		if (regType.equals("new")) {
    		patientDetailsBean.set("first_visit_reg_date", new java.sql.Date((new java.util.Date()).getTime()));
    		if (!patientDetailsBean.get("patient_category_id").toString().equals("1")) {
    			BasicDynaBean categoryBean =
    				new GenericDAO("patient_category_master").findByKey("category_id",
    							new BigDecimal(patientDetailsBean.get("patient_category_id").toString()));

    			String categoryCode = null;
    			if (categoryBean.get("code")!=null)
    				categoryCode = categoryBean.get("code").toString();
    			String isSeperateSeqRequire = categoryBean.get("seperate_num_seq").toString();

    			if (categoryCode!=null && "Y".equals(isSeperateSeqRequire)) {
    				mrNo = DataBaseUtil.getNextPatternId(categoryCode);
    			}else {
        			mrNo = DataBaseUtil.getNextPatternId("MRNO");
        		}
    		} else {
    			mrNo = DataBaseUtil.getNextPatternId("MRNO");
    		}
    	} else {
    		mrNo = paramMrno;
    	}
		return mrNo;
	}


	// Registration Validity
	public boolean isRegValidityExpired(BasicDynaBean visitDetailsBean, String mrno, String regType)
															throws SQLException {
		boolean isRenewal = false;
    	if (regType.equalsIgnoreCase("regd")) {
			BasicDynaBean regDates = VisitDetailsDAO.getPreviousRegDateChargeAccepted(mrno);
			if (regDates == null)
				return isRenewal;

			Date previousRegDate = (Date) regDates.get("visit_reg_date");
			DateUtil dateUtil = new DateUtil();
			if (previousRegDate!=null) {
				BasicDynaBean bean = regPerfDAO.getRecord();
				String regValidityPeriod = bean.get("reg_validity_period").toString();
				String dateDiff = DataBaseUtil.dateDiff(dateUtil.getDateFormatter().format(DateUtil.getCurrentDate()),
						dateUtil.getDateFormatter().format(previousRegDate));
				if (Integer.parseInt(dateDiff) >= Integer.parseInt(regValidityPeriod)) {
					visitDetailsBean.set("reg_charge_accepted", "Y");
					isRenewal = true;
				} else {
					visitDetailsBean.set("reg_charge_accepted", "N");
				}
			} else {
				// there is no previous visit: this could be migrated data. Check based on patient reg_date
				Date regDate = (Date) regDates.get("patient_reg_date");
				Date goLiveDate = GenericPreferencesDAO.getGenericPreferences().getGoLiveDate();
				if (regDate != null && (regDate.compareTo(goLiveDate) < 0)) {
					// for uploaded patients: first_visit_reg_date is optional, if the user fills the
					// first_visit_reg_date take that or insert the uploaded date as the first_visit_reg_date
					// patient registered before the system went live: belongs to previous software.
					// Assume that registration is still valid, so don't charge again.
					visitDetailsBean.set("reg_charge_accepted", "N");
				} else {
					// first visit registration date will be null for the patients who has done using pre-registration.
					// No previous visit, so it is a pre-reg patient. Charge normal (not renewal)
					visitDetailsBean.set("reg_charge_accepted", "Y");
				}
			}
		} else {
			// new registration, add the reg charge
			visitDetailsBean.set("reg_charge_accepted", "Y");
		}
    	return isRenewal;
	}

	// Base doctor
	public BasicDynaBean regBaseDoctor(BasicDynaBean visitDetailsBean, String doctorId, String doctorCharge,
			String consDate, String consTime, String consRemarks, int appointmentId) throws SQLException, ParseException {
		BasicDynaBean baseDocBean = null;
		java.util.Date parsedDate = new java.util.Date();
		java.sql.Timestamp datetime = new java.sql.Timestamp(parsedDate.getTime());

    	if (doctorId != null && !doctorId.equals("")
    			&& doctorCharge != null && !doctorCharge.equals("")) {

	    	GenericDAO docDao = new GenericDAO("doctor_consultation");
			if (doctorId != null && !doctorId.equals("")) {
				baseDocBean = docDao.getBean();

				// Prescribed date is same as registration date.
				// Visit date is the consultation date choosen while registration.
				// If Visit date time is less than prescribed date then visit date time = prescribed date time
				Timestamp visitdtTime = new Timestamp(parsedDate.getTime());
				if (consDate != null && !consDate.equals("") && consTime != null && !consTime.equals(""))
					visitdtTime = DateUtil.parseTimestamp(consDate+" "+consTime);

				if (visitdtTime.getTime() < parsedDate.getTime())
					visitdtTime = datetime;

				baseDocBean.set("doctor_name", doctorId);
				baseDocBean.set("presc_date", datetime);
				baseDocBean.set("visited_date", visitdtTime);
				baseDocBean.set("remarks", consRemarks);
				baseDocBean.set("head", doctorCharge);
				baseDocBean.set("status", "A");

				if (appointmentId > 0) {
					BasicDynaBean apptBean = new GenericDAO("scheduler_appointments").findByKey("appointment_id", appointmentId);
					if (baseDocBean.get("doctor_name").equals(apptBean.get("res_sch_name"))) {
						baseDocBean.set("appointment_id", appointmentId);
						baseDocBean.set("presc_doctor_id", apptBean.get("presc_doc_id"));
						//commenting because of Bug#30503
						// update the schedule date as the visited date for the doctor consultation when it is done through scheduler
					//	baseDocBean.set("visited_date", apptBean.get("appointment_time"));
					}
				}

				visitDetailsBean.set("doctor", doctorId);
			}
    	}
    	return baseDocBean;
	}

	public boolean regPolicyDetails(Connection con, BasicDynaBean visitDetailsBean, String regType,
					int planId, Map policyDetailsMap, BasicDynaBean sponsorTypeBean) throws SQLException, IOException, ParseException {
		boolean success = true;
		String mrNo = (String)visitDetailsBean.get("mr_no");
		String policyNo = (String)policyDetailsMap.get("policy_no");
		String policyNumber = (String)policyDetailsMap.get("policy_number");
		String policyHolder = (String)policyDetailsMap.get("policy_holder_name");
		String relation = (String)policyDetailsMap.get("patient_relationship");
		Date startValidity = (Date)policyDetailsMap.get("policy_validity_start");
		Date endValidity = (Date)policyDetailsMap.get("policy_validity_end");

		int nextPatientPolicyDetId = getnextPatientPolicyDetailsId();
		boolean insertPatientPolicy = false;

		GenericDAO patPolicyDao= new GenericDAO("patient_policy_details");
		if(planId > 0) {
			BasicDynaBean patientPolicyBean = patPolicyDao.getBean();
			patientPolicyBean.set("mr_no", mrNo);
			patientPolicyBean.set("member_id", policyNo);
			patientPolicyBean.set("policy_number", policyNumber);
			patientPolicyBean.set("policy_holder_name", policyHolder);
			patientPolicyBean.set("policy_validity_start", startValidity);
			patientPolicyBean.set("policy_validity_end", endValidity);
			patientPolicyBean.set("plan_id", planId);
			patientPolicyBean.set("patient_relationship", relation);

			int patientPolicyId=-1;
			if (regType.equals("new")) {
				insertPatientPolicy = true;

			}else if (regType.equals("regd")) {
				List<BasicDynaBean> existingPatPolicyDetailsList = patPolicyDao.findAllByKey("mr_no", mrNo);

				// Get the patient policy id if the membership has validity.
				patientPolicyId = memberShipValidityExists(existingPatPolicyDetailsList, policyNo, planId, endValidity,sponsorTypeBean);
				// patientPolicyId = membershipValidityExists(existingPatPolicyDetailsList, policyNo, planId, endValidity,sponsorTypeBean);

				if (patientPolicyId == 0)
					insertPatientPolicy = true;
				else
					visitDetailsBean.set("patient_policy_id", patientPolicyId);
			}

			if (insertPatientPolicy) {

				// Update the expired policies as Inactive.
				Map keys = new HashMap();
				Map fields = new HashMap();

				fields.put("status", "I");

				keys.put("mr_no", mrNo);
				keys.put("member_id", policyNo);

				patPolicyDao.update(con, fields, keys);

				// Insert the policy with new policy Validity end date.
				visitDetailsBean.set("patient_policy_id", nextPatientPolicyDetId);
				patientPolicyBean.set("patient_policy_id", nextPatientPolicyDetId);
				success = patPolicyDao.insert(con, patientPolicyBean);
			}else {
				Map keys = new HashMap();
				keys.put("mr_no", mrNo);
				keys.put("plan_id", planId);
				keys.put("patient_policy_id",patientPolicyId);
				patPolicyDao.update(con, patientPolicyBean.getMap(), keys);
			}
		}
		return success;
	}

	public Integer getnextPatientPolicyDetailsId() throws SQLException{
		String query = "SELECT nextval('patient_policy_details_patient_policy_id_seq')";
		Connection con = DataBaseUtil.getReadOnlyConnection();
		PreparedStatement ps=null;
		try {
			ps = con.prepareStatement(query);
			return DataBaseUtil.getIntValueFromDb(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public int membershipValidityExists(List<BasicDynaBean> existingPatPolicyDetailsList,
			String policyNo, int planId, Date endValidity, BasicDynaBean sponsorType) {
		boolean result = true;
		for(BasicDynaBean b: existingPatPolicyDetailsList) {
			if (checkMemberIdMatch(policyNo, b, sponsorType)) {
				result = b.get("member_id").equals(policyNo);
				if (!result) continue;
			}
			if (checkPolicyValidityMatch(endValidity, b, sponsorType)) {
				result = ((Date)b.get("policy_validity_end")).getTime() == endValidity.getTime();
				if (!result) continue;
			}
			if (checkPlanIdMatch(b, sponsorType)) {
				result = ((Integer)b.get("plan_id")).equals(planId);
				if (!result) continue;
			}

			if (result) return (Integer)b.get("patient_policy_id");
		}
		return 0;

	}

	private boolean checkMemberIdMatch(String policyNo, BasicDynaBean patientPolicy, BasicDynaBean sponsorType) {
		if (null != sponsorType) {
			if (null == sponsorType.get("member_id_show") || sponsorType.get("member_id_show").equals("N")) {
				return false;
			}
			if (null != sponsorType.get("member_id_mandatory") && sponsorType.get("member_id_mandatory").equals("Y")) {
				return true; // check always if member id is mandatory
			}
			if (null != patientPolicy && null != patientPolicy.get("member_id")) {
				if (!((String)patientPolicy.get("member_id")).trim().equals("") &&
					(null != policyNo && !policyNo.trim().equals(""))) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean checkPolicyValidityMatch(Date endValidity, BasicDynaBean patientPolicy, BasicDynaBean sponsorType) {
		if (null != sponsorType) {
			if (null == sponsorType.get("validity_period_show") || sponsorType.get("validity_period_show").equals("N")) {
				return false;
			}
			if (null != sponsorType.get("validity_period_mandatory") && sponsorType.get("validity_period_mandatory").equals("Y")) {
				return true; // check always if member id is mandatory
			}
			if (null != patientPolicy && null != patientPolicy.get("policy_validity_end") && null != endValidity) {
				return true;
			}
		}
		return false;
	}

	private boolean checkPlanIdMatch(BasicDynaBean patientPolicy, BasicDynaBean sponsorType) {
		return true;
	}

	public int memberShipValidityExists(List<BasicDynaBean> existingPatPolicyDetailsList,
				String policyNo, int planId, Date endValidity, BasicDynaBean sponsorTypeBean) {
		if (existingPatPolicyDetailsList == null || existingPatPolicyDetailsList.size() == 0)
			return 0;
		if(null != sponsorTypeBean){
			if(sponsorTypeBean.get("member_id_show").equals("Y") && sponsorTypeBean.get("validity_period_show").equals("Y")){
				for(BasicDynaBean b: existingPatPolicyDetailsList) {
					if(b.get("status").equals("A")){
						if(null != b.get("member_id") && null != b.get("policy_validity_end") && null!=policyNo && null!=endValidity){
							if(((String)b.get("member_id")).equals(policyNo)
								&& ((Integer)b.get("plan_id")).equals(planId)
								&& ((Date)b.get("policy_validity_end")).getTime() == endValidity.getTime()) {
								return (Integer)b.get("patient_policy_id");
							}
						}else if(null == b.get("member_id") && (null == policyNo || policyNo.equals("")) && null ==b.get("policy_validity_end")
								&& (null == endValidity || endValidity.equals("")) && ((Integer)b.get("plan_id")).equals(planId)){
							return (Integer)b.get("patient_policy_id");
						}else if ((null == b.get("member_id") && null !=policyNo && !policyNo.equals("")) ||
									(null == b.get("policy_validity_end"))&& null != endValidity && !endValidity.equals("")){
								return 0;
						}
					}
				}
			}
			else if(sponsorTypeBean.get("member_id_show").equals("Y") && !sponsorTypeBean.get("validity_period_show").equals("Y")){
				for(BasicDynaBean b: existingPatPolicyDetailsList) {
					if(b.get("status").equals("A")){
						if(null != b.get("member_id") && null!=policyNo){
							if(((String)b.get("member_id")).equals(policyNo)
								&& ((Integer)b.get("plan_id")).equals(planId)){
								return (Integer)b.get("patient_policy_id");
							}
						}else if(null == b.get("member_id") && (null==policyNo || policyNo.equals("")) && ((Integer)b.get("plan_id")).equals(planId)){
							return (Integer)b.get("patient_policy_id");
						}else if(null == b.get("member_id") && null != policyNo && !(policyNo.equals(""))){
							return 0;
						}
					}
				}
			}
			else if(!sponsorTypeBean.get("member_id_show").equals("Y") && sponsorTypeBean.get("validity_period_show").equals("Y")){
				for(BasicDynaBean b: existingPatPolicyDetailsList) {
					if(b.get("status").equals("A")){
					  if(null != b.get("policy_validity_end") && null!=endValidity){
							if(((Integer)b.get("plan_id")).equals(planId)
								&& ((Date)b.get("policy_validity_end")).getTime() == endValidity.getTime()) {
								return (Integer)b.get("patient_policy_id");
							}
						}else if(null == b.get("policy_validity_end") && (null==endValidity || endValidity.equals("")) && ((Integer)b.get("plan_id")).equals(planId)){
							return (Integer)b.get("patient_policy_id");
						}else if(null == b.get("policy_validity_end") && null != endValidity && !(endValidity.equals(""))){
							return 0;
						}
					}
				}
			}
			else if(!sponsorTypeBean.get("member_id_show").equals("Y") && !sponsorTypeBean.get("validity_period_show").equals("Y")){
				for(BasicDynaBean b: existingPatPolicyDetailsList) {
					if(((Integer)b.get("plan_id")).equals(planId)) {
						return (Integer)b.get("patient_policy_id");
					}
				}
			}
			return 0;
		}
		return 0;
	}
	// Insurance
	public boolean regInsurance(Connection con, int insuranceId, String newTpaSelected,
			String mrNo, String tpaId, Map policyDetailsMap) throws SQLException,ParseException {
		boolean success = true;

    	if (insuranceId > 0) {
			if ("Y".equals(newTpaSelected)) {
				success = new InsuranceDAO().addToDashBoard(con, mrNo, tpaId, insuranceId, policyDetailsMap);
			}
    	}
    	return success;
	}

	// Scheduler Operation Order
	public BasicDynaBean regOperationOrder(Connection con, BasicDynaBean visitDetailsBean, BasicDynaBean apptBean,
				String visitType, String category, int appointmentId) throws Exception{
		BasicDynaBean OperationBean = null;
		boolean conduction = false;
		String visit_type = (String)visitDetailsBean.get("visit_type");
		BasicDynaBean genPrefs =  GenericPreferencesDAO.getAllPrefs();
		String opApplicableFor =  (String)genPrefs.get("operation_apllicable_for");

		opApplicableFor = opApplicableFor.equals("b") ? visit_type :  opApplicableFor;

		if (category.equals("OPE") && appointmentId > 0 && visit_type.equals(opApplicableFor)) {
    		GenericDAO opDao = new GenericDAO("bed_operation_schedule");
    		conduction = ResourceDAO.getConductionForTestOrServiceOrOperation(category, (String)apptBean.get("res_sch_name"));
    		OperationBean = opDao.getBean();
    		Timestamp endDateTime = null;
    		List<BasicDynaBean> baseOpBean = ResourceDAO.getOperationDetails(con,appointmentId);
    		for (int i=0 ;i<baseOpBean.size();i++) {
    			if (baseOpBean.get(i).get("resource_type").equals("Operation Theatre")) {
    				String OpId = (String)baseOpBean.get(i).get("res_sch_name");
    				String prescDocId = (String)baseOpBean.get(i).get("presc_doc_id");
    				Timestamp appointDateTime = (Timestamp)baseOpBean.get(i).get("appointment_time");
					int duration = (Integer)baseOpBean.get(i).get("duration");
					endDateTime = ResourceBO.getAppointmntEndTime(appointDateTime, duration);
    				if ((visitType.equals("ipreg")||visitType.equals("opreg")) && OpId != null && !OpId.equals("")) {
	    				OperationBean.set("operation_name", OpId);
						OperationBean.set("mr_no", (String)visitDetailsBean.get("mr_no"));
						OperationBean.set("patient_id", (String)visitDetailsBean.get("patient_id"));
						OperationBean.set("consultant_doctor", prescDocId);
						OperationBean.set("theatre_name", (String)baseOpBean.get(i).get("booked_resource_id"));
						if (!conduction) {
							OperationBean.set("status", "C");
						} else {
							OperationBean.set("status", "A");
						}
						OperationBean.set("appointment_id", appointmentId);
						OperationBean.set("prescribed_id", -1);
						OperationBean.set("hrly", "checked");
						OperationBean.set("start_datetime", (Timestamp)baseOpBean.get(i).get("appointment_time"));
						OperationBean.set("end_datetime", endDateTime);
						OperationBean.set("prescribed_date", (Timestamp)baseOpBean.get(i).get("booked_time"));
						OperationBean.set("remarks","Scheduler Operation");
						OperationBean.set("finalization_status","N");
    				}
    			} else if (baseOpBean.get(i).get("resource_type").equals("Surgeon")) {
    				OperationBean.set("surgeon", (String)baseOpBean.get(i).get("booked_resource_id"));
    			} else if (baseOpBean.get(i).get("resource_type").equals("Anesthetist")) {
    				OperationBean.set("anaesthetist", (String)baseOpBean.get(i).get("booked_resource_id"));
    			}
    		}
    	}
		return OperationBean;
	}

	// Case file department
	public boolean regCaseFile(Connection con, BasicDynaBean patientDetailsBean,
					String deptId, String userName, String raiseIndent) throws SQLException, IOException {

		boolean success = true;
		String mrNo = patientDetailsBean.get("mr_no").toString();
		boolean mrnoExists = mrdCaseFileAttributesDAO.exist("mr_no", mrNo, false);

    	if(!mrnoExists) {
    		BasicDynaBean mrdfile = mrdCaseFileAttributesDAO.getBean();
    		mrdfile.set("mr_no", mrNo);
    		mrdfile.set("file_status", "A");
    		mrdfile.set("case_status", "A");
    		mrdfile.set("created_date",  DateUtil.getCurrentDate());

    		BasicDynaBean regPrefBean = regPerfDAO.getRecord();
    		int issueseq = 0;
    		if (regPrefBean.get("issue_to_mrd_on_registration").toString().equals("Y")) {

    			issueseq = DataBaseUtil.getIntValueFromDb("SELECT nextval('mrd_casefile_issue_id_seq')");

	    		mrdfile.set("requesting_dept", deptId);
	    		mrdfile.set("request_date",  DateUtil.getCurrentTimestamp());
	    		mrdfile.set("indented", "N");
	    		mrdfile.set("requested_by", userName);
	    		mrdfile.set("remarks", "Issued while registration");
	    		mrdfile.set("issued_to_dept", deptId);
	    		mrdfile.set("issued_on", DateUtil.getCurrentTimestamp());
	    		mrdfile.set("issued_id", issueseq);
	    		mrdfile.set("file_status", "U");
    		}

    		success = mrdCaseFileAttributesDAO.insert(con, mrdfile);

    		if (!success)
	    		return success;

    		if (regPrefBean.get("issue_to_mrd_on_registration").toString().equals("Y")) {

	    		GenericDAO issuedao = new GenericDAO("mrd_casefile_issuelog");
	    		BasicDynaBean issuefile = issuedao.getBean();
	    		issuefile.set("issue_id", issueseq);
	    		issuefile.set("mr_no",  mrNo);
	    		issuefile.set("issued_on", DateUtil.getCurrentTimestamp());
	    		issuefile.set("issued_to_dept", deptId);
	    		issuefile.set("issue_user", userName);

	    		success = issuedao.insert(con, issuefile);
    		}
    	}else if (raiseIndent != null && !raiseIndent.equals("") && "Y".equals(raiseIndent)) {
	    		BasicDynaBean mrdfile = mrdCaseFileAttributesDAO.getBean();
	    		mrdfile.set("requesting_dept", deptId);
	    		mrdfile.set("request_date",  DateUtil.getCurrentTimestamp());
	    		mrdfile.set("indented", "Y");
	    		mrdfile.set("requested_by", userName);
	    		mrdfile.set("remarks", "Raised Indent while registration");

	    		Map fields = mrdfile.getMap();
	    		Map key = new HashMap();
	    		key.put("mr_no", mrNo);

	    		success = mrdCaseFileAttributesDAO.update(con, fields, key) > 0;
	    	}
    	return success;
	}

	// TODO : Review -> Removed a regClaimId method which was using BILL_DEFAULT_ACCOUNT_GROUP.

	// OutSide patient Diagnosis
	public boolean regOutSidePatientDiagnosis(Connection con, Map requestParams,
						String userName, String patientId) throws SQLException, IOException, ParseException {
		PrescriptionBO pbo = new PrescriptionBO();
		// update the secondary diagnosis details.
		String[] diagnosis_ids = (String[])requestParams.get("diagnosis_id");
		String[] diagnosis_code = (String[])requestParams.get("diagnosis_code");
		String[] diagnosis_description = (String[])requestParams.get("diagnosis_description");
		String[] diagnosis_year_of_onset = (String[]) requestParams.get("diagnosis_year_of_onset");
		String[] diagnosis_status_id = (String[])requestParams.get("diagnosis_status_id");
		String[] diagnosis_remarks = (String[])requestParams.get("diagnosis_remarks");
		String[] diag_type = (String[])requestParams.get("diagnosis_type");
		String[] diag_delete = (String[])requestParams.get("diagnosis_deleted");
		String[] diag_edited = (String[])requestParams.get("diagnosis_edited");
		String[] diag_doctor = (String[])requestParams.get("diagnosis_doctor_id");
		String[] diag_datetime = (String[])requestParams.get("diagnosis_datetime");

		if (diagnosis_ids != null) {
			BasicDynaBean regBean = new GenericDAO("patient_registration").findByKey(con,"patient_id", patientId);
			String healthAuthority = CenterMasterDAO.getHealthAuthorityForCenter((Integer) regBean.get("center_id"));
			String codeType = HealthAuthorityPreferencesDAO.getHealthAuthorityPreferences(healthAuthority).getDiagnosis_code_type();
			
			for (int i=0; i<diagnosis_ids.length-1; i++) {
				if (!pbo.updateDiangoisDetails(con, diagnosis_ids[i], diagnosis_code[i],
						diagnosis_description[i], diagnosis_year_of_onset[i], diagnosis_status_id[i], diagnosis_remarks[i],
						diag_doctor[i], diag_datetime[i], new Boolean(diag_delete[i]), new Boolean(diag_edited[i]),
						patientId, diag_type[i], userName,null, "N", regBean, null, codeType))
						return false;
			}
		}
		return true;
	}

	// OP orders
	public boolean regOrders(Connection con, Map requestParams) throws SQLException, IOException, ParseException {

		String[] testPresIds = (String[])requestParams.get("test.doc_presc_id");
		if (testPresIds != null) {
			for (int i=0; i<testPresIds.length; i++ ) {
				if (!testPresIds[i].isEmpty()) {
					PrescribeDAO.updateTestPrescription(con, "O", Integer.parseInt(testPresIds[i]));
				}
			}
		}

		String[] serPresIds = (String[])requestParams.get("service.doc_presc_id");
		if (serPresIds != null) {
			for (int i=0; i<serPresIds.length; i++ ) {
				if (!serPresIds[i].isEmpty()) {
					PrescribeDAO.updateServicePrescription(con, "O", Integer.parseInt(serPresIds[i]));
				}
			}
		}

		String[] consultationPresIds = (String[])requestParams.get("doctor.prescription_id");
		if (consultationPresIds != null) {
			PatientPrescriptionDAO consultDAO = new PatientPrescriptionDAO();
			for (int i=0; i<consultationPresIds.length; i++) {
				if (!consultationPresIds[i].isEmpty()) {
					BasicDynaBean cpBean = consultDAO.getBean();
					cpBean.set("patient_presc_id", Integer.parseInt(consultationPresIds[i]));
					cpBean.set("status", "O");

					consultDAO.updateWithName(con, cpBean.getMap(), "patient_presc_id");
				}
			}
		}
		return true;
	}

	// IP Bed Allocation
	public boolean regIPBedAllocation(Connection con, BasicDynaBean ipBedBean, BasicDynaBean admBean, String userName,
				String dayCare, String bedType, String mrNo, String patientId, String billNo, boolean insured)
								throws IOException, SQLException, Exception {
		boolean success = true;
		BasicDynaBean ipPrefs = new GenericDAO("ip_preferences").getRecord();

		if (ipPrefs.get("allocate_bed_at_reg") != null && ((String)ipPrefs.get("allocate_bed_at_reg")).equals("Y")) {

			ipBedBean.set("mrno", mrNo);
			ipBedBean.set("patient_id", patientId);
			if (dayCare != null)
				ipBedBean.set("end_date", DateUtil.addHours(DataBaseUtil.getDateandTime(), 1));
			ipBedBean.set("estimated_days", BigDecimal.ONE);
			ipBedBean.set("status", "A");
			ipBedBean.set("bed_state", "O");
			ipBedBean.set("updated_date", DataBaseUtil.getDateandTime());
			ipBedBean.set("charged_bed_type", bedType);
			ipBedBean.set("username", userName);
			ipBedBean.set("admitted_by", userName);
			ipBedBean.set("is_bystander", false);

			admBean.set("mr_no", mrNo);
			admBean.set("patient_id", patientId );
			admBean.set("admit_date", DateUtil.getCurrentDate());
			admBean.set("admit_time", DataBaseUtil.getDateandTime());
			admBean.set("estimated_days", BigDecimal.ONE);

			if (dayCare == null)		// checkbox can be null if unchecked
				admBean.set("daycare_status", "N");

			OrderBO orderBo = new OrderBO();
			orderBo.setBillInfo(con, patientId, billNo, insured, userName);
			orderBo.setUserName(userName);

			success = (orderBo.allocateBed(con, ipBedBean, admBean) == null) ;

			if (!success)
				return success;


			success &= orderBo.recalculateBedCharges(con, patientId) == null;
		}
		return success;
	}

	public boolean regPrimarySponsorDocs(Connection con, List <BasicDynaBean> allVisits, Map requestParams,
			FormFile priInsDocBytea,
			String specializedDocType, boolean specialized, String userName) throws IOException, SQLException {
		boolean success = true;

		for(BasicDynaBean bean : allVisits){
			BasicDynaBean visitDetailsBean=VisitDetailsDAO.getVisitDetails(con, (String) bean.get("patient_id"));

			MimeUtil2 mimeUtil = getMimeUtil();

			Integer currId = null;
			List<String> columns = new ArrayList<String>();
			columns.add("patient_id");
			columns.add("priority");
			Map<String, Object> identifiers = new HashMap<String, Object>();
			identifiers.put("patient_id", visitDetailsBean.get("patient_id"));
			identifiers.put("priority", 1);

			BasicDynaBean primaryInsBean = new PatientInsurancePlanDAO().findByKey(con, identifiers);
			boolean planSelected = (visitDetailsBean.get("plan_id") != null
					&& ((Integer)visitDetailsBean.get("plan_id")).intValue() > 0) ? true
							: (primaryInsBean != null && primaryInsBean.get("plan_id") != null && !primaryInsBean.get("plan_id").equals("")
									? ((Integer)primaryInsBean.get("plan_id")).intValue() > 0 : false);

			String primarySponsor =  getParamArrValue (requestParams, "primary_sponsor", null);

			String insCardFileLocation = null;
			String insCardFileContentType = null;
			String docsName    = null;
			String format      = null;
			String docType     = null;
			String docDate     = null;
			FormFile docContentBytea = null;
			AbstractDocumentPersistence persistenceAPI =  null;
			BasicDynaBean docBean = null;
			String opType=getParamArrValue (requestParams, "op_type", null);
			if(null == opType)
			{
				opType=(String) visitDetailsBean.get("op_type");
			}
				//(String) requestParams.get("op_type");

			if (primarySponsor != null) {
				if (primarySponsor.equals("I")) {
					insCardFileLocation = getParamArrValue (requestParams, "primary_sponsor_cardfileLocationI", null);
					insCardFileContentType = getParamArrValue (requestParams, "primary_sponsor_cardContentTypeI", null);
					docsName    = getParamArrValue (requestParams, "primary_insurance_doc_name", null);
					format      = getParamArrValue (requestParams, "primary_insurance_format", null);
					docType     = getParamArrValue (requestParams, "primary_insurance_doc_type", null);
					docDate     = getParamArrValue (requestParams, "primary_insurance_doc_date", null);
					docContentBytea = priInsDocBytea;

					if (docsName != null && docsName.equals("Insurance Card") && planSelected) {
						persistenceAPI  = AbstractDocumentPersistence.getInstance("plan_card", true);
					}else {
						persistenceAPI = AbstractDocumentPersistence.getInstance(specializedDocType, specialized);
					}

					currId = (visitDetailsBean.get("patient_policy_id")== null || visitDetailsBean.get("patient_policy_id").equals("") || (Integer)visitDetailsBean.get("patient_policy_id") == 0)
							? (primaryInsBean == null || primaryInsBean.get("patient_policy_id") == null || primaryInsBean.get("patient_policy_id").equals(""))
									? 0 : (Integer)primaryInsBean.get("patient_policy_id")
											: (Integer)visitDetailsBean.get("patient_policy_id");

					if (currId > 0)
						docBean = planDocsDetailsDAO.findByKey("patient_policy_id", currId);

					//docBean = editInsHelper.findLatestPlanDocumentBean(con,currId);

				}			
			}

			String mrNo = (String)visitDetailsBean.get("mr_no");
			String patientId = (String)visitDetailsBean.get("patient_id");

			if (docsName != null && !docsName.equals("")) {

				Map<String,Object[]> newparamMap = new HashMap<String,Object[]>();
				copyStringToMap(newparamMap, "username", userName);
				copyStringToMap(newparamMap, "doc_date", docDate);
				copyStringToMap(newparamMap, "doc_name", docsName);
				copyStringToMap(newparamMap, "doc_format", format);
				copyStringToMap(newparamMap, "format", format);
				copyStringToMap(newparamMap, "doc_type", docType);
				copyStringToMap(newparamMap, "mr_no", mrNo);
				copyStringToMap(newparamMap, "patient_id", patientId);
				if (primarySponsor.equals("I"))
					copyStringToMap(newparamMap, "patient_policy_id", currId.toString());
				if (docContentBytea != null && docContentBytea.getFileSize()>0) {

					copyObjectToMap(newparamMap, "doc_content_bytea", docContentBytea.getInputStream());
					copyObjectToMap(newparamMap, "content_type", mimeUtil.getMimeTypes(docContentBytea.getFileData()));
					copyObjectToMap(newparamMap, "fileName", docContentBytea.getFileName());

				}else if (null != insCardFileLocation && !insCardFileLocation.equals("")) {
					copyObjectToMap(newparamMap, "doc_content_bytea", new FileInputStream(new File(insCardFileLocation)));
					copyObjectToMap(newparamMap, "content_type", insCardFileContentType);
					copyObjectToMap(newparamMap, "fileName", "pasteImage");
				}else if(null !=docContentBytea && docContentBytea.getFileSize()==0 && null != opType && (opType.equals("R") || opType.equals("F") || opType.equals("D"))){
                  Map<String, Object> identifiers1 = new HashMap<String, Object>();
                  identifiers1.put("patient_id",
                      getParamArrValue(requestParams, "main_visit_id", null));
                  identifiers1.put("priority", 1);
                  BasicDynaBean patInsuPlanBean = patientInsurancePlansDAO.findByKey(identifiers1);
                  BasicDynaBean patDocDetailsBean = null;
                  if (null != patInsuPlanBean)
                    patDocDetailsBean = planDocsDetailsDAO.findByKey(con, "patient_policy_id",
                        patInsuPlanBean.get("patient_policy_id"));
                  BasicDynaBean patDocsBean = patientDocumentsDAO.getBean();
                  if (null != patDocDetailsBean)
                    patientDocumentsDAO.loadByteaRecords(con, patDocsBean, "doc_id",
                        patDocDetailsBean.get("doc_id"));

                  if (null != patDocsBean && patDocsBean.getMap().size() > 0) {
                    copyObjectToMap(newparamMap, "doc_content_bytea",
                        patDocsBean.get("doc_content_bytea"));
                    copyObjectToMap(newparamMap, "content_type", patDocsBean.get("content_type"));
                    copyObjectToMap(newparamMap, "fileName", patDocDetailsBean.get("doc_name"));
                  }

				}
				if(newparamMap.get("doc_content_bytea")!= null){
					if(docBean!= null && docBean.get("doc_id")!= null) {
						copyStringToMap(newparamMap, "doc_id", docBean.get("doc_id").toString());
						success = persistenceAPI.update(newparamMap, con, false);
					} else {
						success = persistenceAPI.create(newparamMap, con);
					}
				}
				newparamMap.clear();
			}
		}
		return success;
	}
	public boolean regPrimarySponsorDocs(Connection con, BasicDynaBean visitDetailsBean, Map requestParams,
			FormFile priInsDocBytea, FormFile priCorpDocBytea, FormFile priNatDocBytea, FormFile insPastedPhoto,
			String specializedDocType, boolean specialized, String userName) throws IOException, SQLException {
		boolean success = true;

		MimeUtil2 mimeUtil = getMimeUtil();

		Integer currId = null;
		List<String> columns = new ArrayList<String>();
		columns.add("patient_id");
		columns.add("priority");
		Map<String, Object> identifiers = new HashMap<String, Object>();
		identifiers.put("patient_id", visitDetailsBean.get("patient_id"));
		identifiers.put("priority", 1);

		BasicDynaBean primaryInsBean = new PatientInsurancePlanDAO().findByKey(con, identifiers);
		boolean planSelected = (visitDetailsBean.get("plan_id") != null
				&& ((Integer)visitDetailsBean.get("plan_id")).intValue() > 0) ? true
						: (primaryInsBean != null && primaryInsBean.get("plan_id") != null && !primaryInsBean.get("plan_id").equals("")
								? ((Integer)primaryInsBean.get("plan_id")).intValue() > 0 : false);

		String primarySponsor =  getParamArrValue (requestParams, "primary_sponsor", null);

		String docsName    = null;
		String format      = null;
		String docType     = null;
		String docDate     = null;
		FormFile docContentBytea = null;
		AbstractDocumentPersistence persistenceAPI =  null;
		BasicDynaBean docBean = null;
		String opType=getParamArrValue (requestParams, "op_type", null);
		String mainVisitID=getParamArrValue (requestParams, "main_visit_id", null);
		String visitType = getParamArrValue (requestParams, "group" ,null);

		if(null == opType)
		{
			opType=(String) visitDetailsBean.get("op_type");
		}
			//(String) requestParams.get("op_type");

		if (primarySponsor != null) {
			if (primarySponsor.equals("I")) {
				docsName    = getParamArrValue (requestParams, "primary_insurance_doc_name", null);
				format      = getParamArrValue (requestParams, "primary_insurance_format", null);
				docType     = getParamArrValue (requestParams, "primary_insurance_doc_type", null);
				docDate     = getParamArrValue (requestParams, "primary_insurance_doc_date", null);
				docContentBytea = priInsDocBytea;

				if (docsName != null && docsName.equals("Insurance Card") && planSelected) {
					persistenceAPI  = AbstractDocumentPersistence.getInstance("plan_card", true);
				}else {
					persistenceAPI = AbstractDocumentPersistence.getInstance(specializedDocType, specialized);
				}

				currId = (visitDetailsBean.get("patient_policy_id")== null || visitDetailsBean.get("patient_policy_id").equals("") || (Integer)visitDetailsBean.get("patient_policy_id") == 0)
						? (primaryInsBean == null || primaryInsBean.get("patient_policy_id") == null || primaryInsBean.get("patient_policy_id").equals(""))
								? 0 : (Integer)primaryInsBean.get("patient_policy_id")
										: (Integer)visitDetailsBean.get("patient_policy_id");

				if (currId > 0)
					docBean = planDocsDetailsDAO.findByKey("patient_policy_id", currId);

				//docBean = editInsHelper.findLatestPlanDocumentBean(con,currId);

			}else if (primarySponsor.equals("C")) {
				docsName    = getParamArrValue (requestParams, "primary_corporate_doc_name", null);
				format      = getParamArrValue (requestParams, "primary_corporate_format", null);
				docType     = getParamArrValue (requestParams, "primary_corporate_doc_type", null);
				docDate     = getParamArrValue (requestParams, "primary_corporate_doc_date", null);
				docContentBytea = priCorpDocBytea;

				if (docsName != null && docsName.equals("Corporate Card")) {
					persistenceAPI  = AbstractDocumentPersistence.getInstance("corporate_card", true);
				}else {
					persistenceAPI = AbstractDocumentPersistence.getInstance(specializedDocType, specialized);
				}

				currId = (visitDetailsBean.get("patient_corporate_id")== null || visitDetailsBean.get("patient_corporate_id").equals(""))
						? 0 : (Integer)visitDetailsBean.get("patient_corporate_id");

				if (currId > 0)
					docBean = new GenericDAO("corporate_docs_details").findByKey("patient_corporate_id", currId);

				//docBean = editInsHelper.findLatestCorporateDocumentBean(con,currId);

			}else if (primarySponsor.equals("N")) {
				docsName    = getParamArrValue (requestParams, "primary_national_doc_name", null);
				format      = getParamArrValue (requestParams, "primary_national_format", null);
				docType     = getParamArrValue (requestParams, "primary_national_doc_type", null);
				docDate     = getParamArrValue (requestParams, "primary_national_doc_date", null);
				docContentBytea = priNatDocBytea;

				if (docsName != null && docsName.equals("National Card")) {
					persistenceAPI  = AbstractDocumentPersistence.getInstance("national_card", true);
				}else {
					persistenceAPI = AbstractDocumentPersistence.getInstance(specializedDocType, specialized);
				}

				currId = (visitDetailsBean.get("patient_national_sponsor_id")== null || visitDetailsBean.get("patient_national_sponsor_id").equals(""))
						? 0 : (Integer)visitDetailsBean.get("patient_national_sponsor_id");
				if (currId > 0)
					docBean = new GenericDAO("national_sponsor_docs_details").findByKey("patient_national_sponsor_id", currId);

				//docBean = editInsHelper.findLatestNationalDocumentBean(con,currId);
			}
		}

		String mrNo = (String)visitDetailsBean.get("mr_no");
		String patientId = (String)visitDetailsBean.get("patient_id");

		if (docsName != null && !docsName.equals("")) {

			Map<String,Object[]> newparamMap = new HashMap<String,Object[]>();
			copyStringToMap(newparamMap, "username", userName);
			copyStringToMap(newparamMap, "doc_date", docDate);
			copyStringToMap(newparamMap, "doc_name", docsName);
			copyStringToMap(newparamMap, "doc_format", format);
			copyStringToMap(newparamMap, "format", format);
			copyStringToMap(newparamMap, "doc_type", docType);
			copyStringToMap(newparamMap, "mr_no", mrNo);
			copyStringToMap(newparamMap, "patient_id", patientId);
			if (primarySponsor.equals("I"))
				copyStringToMap(newparamMap, "patient_policy_id", currId.toString());
			if (docContentBytea != null && docContentBytea.getFileSize()>0) {

				copyObjectToMap(newparamMap, "doc_content_bytea", docContentBytea.getInputStream());
				copyObjectToMap(newparamMap, "content_type", mimeUtil.getMimeTypes(docContentBytea.getFileData()));
				copyObjectToMap(newparamMap, "fileName", docContentBytea.getFileName());

			}else if (null != insPastedPhoto && insPastedPhoto.getFileSize() > 0) {
				copyObjectToMap(newparamMap, "doc_content_bytea", insPastedPhoto.getInputStream());
				copyObjectToMap(newparamMap, "content_type", insPastedPhoto.getContentType());
				copyObjectToMap(newparamMap, "fileName", "pasteImage");
			}else if(null !=docContentBytea && docContentBytea.getFileSize()==0){
				Map<String, Object> identifiers1 = new HashMap<String, Object>();
				if(null != mainVisitID && !mainVisitID.equals("")){
					identifiers1.put("patient_id", mainVisitID);
				}else{
					if(null != visitType && visitType.equals("ipreg"))
						mainVisitID = VisitDetailsDAO.getPatientLatestVisitId(mrNo, null, "i");
					else
						mainVisitID = VisitDetailsDAO.getPatientLatestVisitId(mrNo, null, "o");

					identifiers1.put("patient_id", mainVisitID);
				}
				identifiers1.put("priority", 1);
                BasicDynaBean patInsuPlanBean = patientInsurancePlansDAO.findByKey(identifiers1);
				BasicDynaBean patDocDetailsBean=null;
				if(null != patInsuPlanBean)
					patDocDetailsBean=planDocsDetailsDAO.findByKey(con, "patient_policy_id",patInsuPlanBean.get("patient_policy_id"));
				BasicDynaBean patDocsBean = patientDocumentsDAO.getBean();
				if(null != patDocDetailsBean)
					patientDocumentsDAO.loadByteaRecords(con, patDocsBean, "doc_id", patDocDetailsBean.get("doc_id"));

				if(null != patDocsBean && patDocsBean.getMap().size()>0){
					copyObjectToMap(newparamMap, "doc_content_bytea", patDocsBean.get("doc_content_bytea"));
					copyObjectToMap(newparamMap, "content_type", patDocsBean.get("content_type"));
					copyObjectToMap(newparamMap, "fileName", patDocDetailsBean.get("doc_name"));
				}

			}
			if(newparamMap.get("doc_content_bytea")!= null){
				if(docBean!= null && docBean.get("doc_id")!= null) {
					copyStringToMap(newparamMap, "doc_id", docBean.get("doc_id").toString());
					success = persistenceAPI.update(newparamMap, con, false);
				} else {
					success = persistenceAPI.create(newparamMap, con);
				}
			}
			newparamMap.clear();
		}
		return success;
	}

	public static  String getParamArrValue(Map requestParams, String paramName, String defaultValue) {
		String value = (requestParams.get(paramName) != null
							&& ((String[])requestParams.get(paramName)).length > 0)
						? ((String[])requestParams.get(paramName))[0] : null ;
		if ((value == null) || value.equals(""))
			value = defaultValue;
		return value;
	}

	public boolean regSecondarySponsorDocs(Connection con,List<BasicDynaBean> allVisits, Map requestParams,
			FormFile secInsDocBytea,
			String specializedDocType, boolean specialized, String userName) throws IOException, SQLException {
		boolean success = true;

		MimeUtil2 mimeUtil = getMimeUtil();
		VisitDetailsDAO visitDAO=new VisitDetailsDAO();
		for(BasicDynaBean bean:allVisits){
			BasicDynaBean visitDetailsBean=visitDAO.getVisitDetails(con,(String) bean.get("patient_id"));
			List<String> columns = new ArrayList<String>();
			columns.add("patient_id");
			columns.add("priority");
			Map<String, Object> identifiers = new HashMap<String, Object>();
			identifiers.put("patient_id", visitDetailsBean.get("patient_id"));
			identifiers.put("priority", 2);

			BasicDynaBean secondaryInsBean = new PatientInsurancePlanDAO().findByKey(con, identifiers);
			Integer currId = null;
			boolean planSelected = (visitDetailsBean.get("plan_id") != null
					&& ((Integer)visitDetailsBean.get("plan_id")).intValue() > 0) ? true
							: (secondaryInsBean !=null && secondaryInsBean.get("plan_id") != null && !secondaryInsBean.get("plan_id").equals("")
									? ((Integer)secondaryInsBean.get("plan_id")).intValue() > 0 : false);

			String secondarySponsor = getParamArrValue (requestParams, "secondary_sponsor", null);

			String insCardFileLocation = null;
			String insCardFileContentType = null;
			String docsName    = null;
			String format      = null;
			String docType     = null;
			String docDate     = null;
			FormFile docContentBytea = null;
			AbstractDocumentPersistence persistenceAPI =  null;
			BasicDynaBean docBean = null;
			String opType=getParamArrValue (requestParams, "op_type", null);
			if(null == opType)
			{
				opType=(String) visitDetailsBean.get("op_type");
			}

			if (secondarySponsor != null) {
				if (secondarySponsor.equals("I")) {
					insCardFileLocation = getParamArrValue (requestParams, "primary_sponsor_cardfileLocationI", null);
					insCardFileContentType = getParamArrValue (requestParams, "primary_sponsor_cardContentTypeI", null);

					docsName    = getParamArrValue (requestParams, "secondary_insurance_doc_name", null);
					format      = getParamArrValue (requestParams, "secondary_insurance_format", null);
					docType     = getParamArrValue (requestParams, "secondary_insurance_doc_type", null);
					docDate     = getParamArrValue (requestParams, "secondary_insurance_doc_date", null);
					docContentBytea = secInsDocBytea;

					if (docsName != null && docsName.equals("Insurance Card") && planSelected) {
						persistenceAPI  = AbstractDocumentPersistence.getInstance("plan_card", true);
					}else {
						persistenceAPI = AbstractDocumentPersistence.getInstance(specializedDocType, specialized);
					}

					currId = (visitDetailsBean.get("patient_policy_id")== null || visitDetailsBean.get("patient_policy_id").equals(""))
							? 0 : (Integer)visitDetailsBean.get("patient_policy_id");

					currId = (visitDetailsBean.get("patient_policy_id")== null || visitDetailsBean.get("patient_policy_id").equals("") || (Integer)visitDetailsBean.get("patient_policy_id") == 0)
							? (secondaryInsBean == null || secondaryInsBean.get("patient_policy_id") == null || secondaryInsBean.get("patient_policy_id").equals(""))
									? 0 : (Integer)secondaryInsBean.get("patient_policy_id")
											: (Integer)visitDetailsBean.get("patient_policy_id");

					if (currId > 0)
						docBean = planDocsDetailsDAO.findByKey("patient_policy_id", currId);

					//docBean = editInsHelper.findLatestPlanDocumentBean(con,currId);

				}			
			}

			String mrNo = (String)visitDetailsBean.get("mr_no");
			String patientId = (String)visitDetailsBean.get("patient_id");

			if (docsName != null && !docsName.equals("")) {

				Map<String,Object[]> newparamMap = new HashMap<String,Object[]>();
				copyStringToMap(newparamMap, "username", userName);
				copyStringToMap(newparamMap, "doc_date", docDate);
				copyStringToMap(newparamMap, "doc_name", docsName);
				copyStringToMap(newparamMap, "doc_format", format);
				copyStringToMap(newparamMap, "format", format);
				copyStringToMap(newparamMap, "doc_type", docType);
				copyStringToMap(newparamMap, "mr_no", mrNo);
				copyStringToMap(newparamMap, "patient_id", patientId);
				if (secondarySponsor.equals("I"))
					copyStringToMap(newparamMap, "patient_policy_id", currId.toString());
				if (docContentBytea != null && docContentBytea.getFileSize()>0) {

					copyObjectToMap(newparamMap, "doc_content_bytea", docContentBytea.getInputStream());
					copyObjectToMap(newparamMap, "content_type", mimeUtil.getMimeTypes(docContentBytea.getFileData()));
					copyObjectToMap(newparamMap, "fileName", docContentBytea.getFileName());

				}else if (null != insCardFileLocation && !insCardFileLocation.equals("")) {
					copyObjectToMap(newparamMap, "doc_content_bytea", new FileInputStream(new File(insCardFileLocation)));
					copyObjectToMap(newparamMap, "content_type", insCardFileContentType);
					copyObjectToMap(newparamMap, "fileName", "pasteImage");
				}else if(null !=docContentBytea && docContentBytea.getFileSize()==0 && null != opType && (opType.equals("R") || opType.equals("F") || opType.equals("D"))){
                  Map<String, Object> identifiers1 = new HashMap<String, Object>();
                  identifiers1.put("patient_id",
                      getParamArrValue(requestParams, "main_visit_id", null));
                  identifiers1.put("priority", 2);
                  BasicDynaBean patInsuPlanBean = patientInsurancePlansDAO.findByKey(identifiers1);
                  BasicDynaBean patDocDetailsBean = null;
                  if (null != patInsuPlanBean)
                    patDocDetailsBean = planDocsDetailsDAO.findByKey(con, "patient_policy_id",
                        patInsuPlanBean.get("patient_policy_id"));
                  BasicDynaBean patDocsBean = patientDocumentsDAO.getBean();
                  if (null != patDocDetailsBean)
                    patientDocumentsDAO.loadByteaRecords(con, patDocsBean, "doc_id",
                        patDocDetailsBean.get("doc_id"));

                  if (null != patDocsBean && patDocsBean.getMap().size() > 0) {
                    copyObjectToMap(newparamMap, "doc_content_bytea",
                        patDocsBean.get("doc_content_bytea"));
                    copyObjectToMap(newparamMap, "content_type", patDocsBean.get("content_type"));
                    copyObjectToMap(newparamMap, "fileName", patDocDetailsBean.get("doc_name"));
                  }

				}
				if(newparamMap.get("doc_content_bytea")!= null){
					if(docBean!= null && docBean.get("doc_id")!= null) {
						copyStringToMap(newparamMap, "doc_id", docBean.get("doc_id").toString());
						success = persistenceAPI.update(newparamMap, con, false);
					} else {
						success = persistenceAPI.create(newparamMap, con);
					}
				}
				newparamMap.clear();
			}
		}
		return success;
	}

	public boolean regSecondarySponsorDocs(Connection con, BasicDynaBean visitDetailsBean, Map requestParams,
				FormFile secInsDocBytea, FormFile secCorpDocBytea, FormFile secNatDocBytea, FormFile insPastedPhoto,
				String specializedDocType, boolean specialized, String userName) throws IOException, SQLException {
		boolean success = true;

		MimeUtil2 mimeUtil = getMimeUtil();

		List<String> columns = new ArrayList<String>();
		columns.add("patient_id");
		columns.add("priority");
		Map<String, Object> identifiers = new HashMap<String, Object>();
		identifiers.put("patient_id", visitDetailsBean.get("patient_id"));
		identifiers.put("priority", 2);

		BasicDynaBean secondaryInsBean = new PatientInsurancePlanDAO().findByKey(con, identifiers);
		Integer currId = null;
		boolean planSelected = (visitDetailsBean.get("plan_id") != null
				&& ((Integer)visitDetailsBean.get("plan_id")).intValue() > 0) ? true
						: (secondaryInsBean !=null && secondaryInsBean.get("plan_id") != null && !secondaryInsBean.get("plan_id").equals("")
								? ((Integer)secondaryInsBean.get("plan_id")).intValue() > 0 : false);

		String secondarySponsor = getParamArrValue (requestParams, "secondary_sponsor", null);

		String docsName    = null;
		String format      = null;
		String docType     = null;
		String docDate     = null;
		FormFile docContentBytea = null;
		AbstractDocumentPersistence persistenceAPI =  null;
		BasicDynaBean docBean = null;
		String opType=getParamArrValue (requestParams, "op_type", null);
		String mainVisitID=getParamArrValue (requestParams, "main_visit_id", null);
		String visitType = getParamArrValue (requestParams, "group" ,null);

		if(null == opType)
		{
			opType=(String) visitDetailsBean.get("op_type");
		}

		if (secondarySponsor != null) {
			if (secondarySponsor.equals("I")) {
				docsName    = getParamArrValue (requestParams, "secondary_insurance_doc_name", null);
				format      = getParamArrValue (requestParams, "secondary_insurance_format", null);
				docType     = getParamArrValue (requestParams, "secondary_insurance_doc_type", null);
				docDate     = getParamArrValue (requestParams, "secondary_insurance_doc_date", null);
				docContentBytea = secInsDocBytea;

				if (docsName != null && docsName.equals("Insurance Card") && planSelected) {
					persistenceAPI  = AbstractDocumentPersistence.getInstance("plan_card", true);
				}else {
					persistenceAPI = AbstractDocumentPersistence.getInstance(specializedDocType, specialized);
				}

				currId = (visitDetailsBean.get("patient_policy_id")== null || visitDetailsBean.get("patient_policy_id").equals(""))
						? 0 : (Integer)visitDetailsBean.get("patient_policy_id");

				currId = (visitDetailsBean.get("patient_policy_id")== null || visitDetailsBean.get("patient_policy_id").equals("") || (Integer)visitDetailsBean.get("patient_policy_id") == 0)
						? (secondaryInsBean == null || secondaryInsBean.get("patient_policy_id") == null || secondaryInsBean.get("patient_policy_id").equals(""))
								? 0 : (Integer)secondaryInsBean.get("patient_policy_id")
										: (Integer)visitDetailsBean.get("patient_policy_id");

				if (currId > 0)
					docBean = planDocsDetailsDAO.findByKey("patient_policy_id", currId);

				//docBean = editInsHelper.findLatestPlanDocumentBean(con,currId);

			}else if (secondarySponsor.equals("C")) {
				docsName    = getParamArrValue (requestParams, "secondary_corporate_doc_name", null);
				format      = getParamArrValue (requestParams, "secondary_corporate_format", null);
				docType     = getParamArrValue (requestParams, "secondary_corporate_doc_type", null);
				docDate     = getParamArrValue (requestParams, "secondary_corporate_doc_date", null);
				docContentBytea = secCorpDocBytea;

				if (docsName != null && docsName.equals("Corporate Card")) {
					persistenceAPI  = AbstractDocumentPersistence.getInstance("corporate_card", true);
				}else {
					persistenceAPI = AbstractDocumentPersistence.getInstance(specializedDocType, specialized);
				}

				currId = (visitDetailsBean.get("patient_corporate_id")== null || visitDetailsBean.get("patient_corporate_id").equals(""))
						? 0 : (Integer)visitDetailsBean.get("patient_corporate_id");

				if (currId > 0)
					docBean = new GenericDAO("corporate_docs_details").findByKey("patient_corporate_id", currId);

				//docBean = editInsHelper.findLatestCorporateDocumentBean(con,currId);

			}else if (secondarySponsor.equals("N")) {
				docsName    = getParamArrValue (requestParams, "secondary_national_doc_name", null);
				format      = getParamArrValue (requestParams, "secondary_national_format", null);
				docType     = getParamArrValue (requestParams, "secondary_national_doc_type", null);
				docDate     = getParamArrValue (requestParams, "secondary_national_doc_date", null);
				docContentBytea = secNatDocBytea;

				if (docsName != null && docsName.equals("National Card")) {
					persistenceAPI  = AbstractDocumentPersistence.getInstance("national_card", true);
				}else {
					persistenceAPI = AbstractDocumentPersistence.getInstance(specializedDocType, specialized);
				}

				currId = (visitDetailsBean.get("patient_national_sponsor_id")== null || visitDetailsBean.get("patient_national_sponsor_id").equals(""))
						? 0 : (Integer)visitDetailsBean.get("patient_national_sponsor_id");

				if (currId > 0)
					docBean = new GenericDAO("national_sponsor_docs_details").findByKey("patient_national_sponsor_id", currId);

				//docBean = editInsHelper.findLatestNationalDocumentBean(con,currId);
			}
		}

		String mrNo = (String)visitDetailsBean.get("mr_no");
		String patientId = (String)visitDetailsBean.get("patient_id");

		if (docsName != null && !docsName.equals("")) {

			Map<String,Object[]> newparamMap = new HashMap<String,Object[]>();
			copyStringToMap(newparamMap, "username", userName);
			copyStringToMap(newparamMap, "doc_date", docDate);
			copyStringToMap(newparamMap, "doc_name", docsName);
			copyStringToMap(newparamMap, "doc_format", format);
			copyStringToMap(newparamMap, "format", format);
			copyStringToMap(newparamMap, "doc_type", docType);
			copyStringToMap(newparamMap, "mr_no", mrNo);
			copyStringToMap(newparamMap, "patient_id", patientId);
			if (secondarySponsor.equals("I"))
				copyStringToMap(newparamMap, "patient_policy_id", currId.toString());
			if (docContentBytea != null && docContentBytea.getFileSize()>0) {

				copyObjectToMap(newparamMap, "doc_content_bytea", docContentBytea.getInputStream());
				copyObjectToMap(newparamMap, "content_type", mimeUtil.getMimeTypes(docContentBytea.getFileData()));
				copyObjectToMap(newparamMap, "fileName", docContentBytea.getFileName());

			}else if (null != insPastedPhoto && insPastedPhoto.getFileSize() > 0) {
				copyObjectToMap(newparamMap, "doc_content_bytea", insPastedPhoto.getInputStream());
				copyObjectToMap(newparamMap, "content_type", insPastedPhoto.getContentType());
				copyObjectToMap(newparamMap, "fileName", "pasteImage");
			}else if(null !=docContentBytea && docContentBytea.getFileSize()==0){
              Map<String, Object> identifiers1 = new HashMap<String, Object>();
              if (null != mainVisitID && !mainVisitID.equals("")) {
                identifiers1.put("patient_id", mainVisitID);
              } else {
                if (null != visitType && visitType.equals("ipreg"))
                  mainVisitID = VisitDetailsDAO.getPatientLatestVisitId(mrNo, null, "i");
                else
                  mainVisitID = VisitDetailsDAO.getPatientLatestVisitId(mrNo, null, "o");

                identifiers1.put("patient_id", mainVisitID);
              }
              identifiers1.put("priority", 2);
              BasicDynaBean patInsuPlanBean = patientInsurancePlansDAO.findByKey(identifiers1);
              BasicDynaBean patDocDetailsBean = null;
              if (null != patInsuPlanBean)
                patDocDetailsBean = planDocsDetailsDAO.findByKey(con, "patient_policy_id",
                    patInsuPlanBean.get("patient_policy_id"));
              BasicDynaBean patDocsBean = patientDocumentsDAO.getBean();
              if (null != patDocDetailsBean)
                patientDocumentsDAO.loadByteaRecords(con, patDocsBean, "doc_id",
                    patDocDetailsBean.get("doc_id"));

              if (null != patDocsBean && patDocsBean.getMap().size() > 0) {
                copyObjectToMap(newparamMap, "doc_content_bytea",
                    patDocsBean.get("doc_content_bytea"));
                copyObjectToMap(newparamMap, "content_type", patDocsBean.get("content_type"));
                copyObjectToMap(newparamMap, "fileName", patDocDetailsBean.get("doc_name"));
              }

			}
			if(newparamMap.get("doc_content_bytea")!= null){
				if(docBean!= null && docBean.get("doc_id")!= null) {
					copyStringToMap(newparamMap, "doc_id", docBean.get("doc_id").toString());
					success = persistenceAPI.update(newparamMap, con, false);
				} else {
					success = persistenceAPI.create(newparamMap, con);
				}
			}
			newparamMap.clear();
		}
		return success;
	}

	public boolean regUploadDocs(Connection con, BasicDynaBean visitDetailsBean, Map requestParams, RegistrationForm regForm,
			String specializedDocType, boolean specialized, String userName) throws IOException, SQLException {
		boolean success = true;

		MimeUtil2 mimeUtil = getMimeUtil();

		String mrNo = (String)visitDetailsBean.get("mr_no");
		String patientId = (String)visitDetailsBean.get("patient_id");

		String[] docsName    = (String[]) requestParams.get("doc_name");
		String[] format      = (String[]) requestParams.get("format");
		String[] docType     = (String[]) requestParams.get("doc_type");
		String[] docDate     = (String[]) requestParams.get("doc_date");

		if (docsName != null && docsName.length > 0) {
			for (int i=0; i< docsName.length; i++) {
				AbstractDocumentPersistence persistenceAPI =
					AbstractDocumentPersistence.getInstance(specializedDocType, specialized);

				Map<String,Object[]> newparamMap = new HashMap<String,Object[]>();
				copyStringToMap(newparamMap, "username", userName);
				copyStringToMap(newparamMap, "doc_date", docDate[0]);
				copyStringToMap(newparamMap, "doc_name", docsName[i]);
				copyStringToMap(newparamMap, "doc_format", format[i]);
				copyStringToMap(newparamMap, "format", format[i]);
				copyStringToMap(newparamMap, "doc_type", docType[0]);
				copyStringToMap(newparamMap, "mr_no", mrNo);
				copyStringToMap(newparamMap, "patient_id", patientId);
				if (i==0 && regForm.getDoc_content_bytea1() != null && regForm.getDoc_content_bytea1().getFileSize()>0) {
					copyObjectToMap(newparamMap, "doc_content_bytea", regForm.getDoc_content_bytea1().getInputStream());
					copyObjectToMap(newparamMap, "content_type", mimeUtil.getMimeTypes(regForm.getDoc_content_bytea1().getFileData()));
					copyObjectToMap(newparamMap, "fileName", regForm.getDoc_content_bytea1().getFileName());
				} else if (i==1 && regForm.getDoc_content_bytea2() != null && regForm.getDoc_content_bytea2().getFileSize()>0) {
					copyObjectToMap(newparamMap, "doc_content_bytea", regForm.getDoc_content_bytea2().getInputStream());
					copyObjectToMap(newparamMap, "content_type", mimeUtil.getMimeTypes(regForm.getDoc_content_bytea2().getFileData()));
					copyObjectToMap(newparamMap, "fileName", regForm.getDoc_content_bytea2().getFileName());
				} else if (i==2 && regForm.getDoc_content_bytea3() != null && regForm.getDoc_content_bytea3().getFileSize()>0) {
					copyObjectToMap(newparamMap, "doc_content_bytea", regForm.getDoc_content_bytea3().getInputStream());
					copyObjectToMap(newparamMap, "content_type", mimeUtil.getMimeTypes(regForm.getDoc_content_bytea3().getFileData()));
					copyObjectToMap(newparamMap, "fileName", regForm.getDoc_content_bytea3().getFileName());
				} else if (i==3 && regForm.getDoc_content_bytea4() != null && regForm.getDoc_content_bytea4().getFileSize()>0) {
					copyObjectToMap(newparamMap, "doc_content_bytea", regForm.getDoc_content_bytea4().getInputStream());
					copyObjectToMap(newparamMap, "content_type", mimeUtil.getMimeTypes(regForm.getDoc_content_bytea4().getFileData()));
					copyObjectToMap(newparamMap, "fileName", regForm.getDoc_content_bytea4().getFileName());
				} else if (i==4 && regForm.getDoc_content_bytea5() != null && regForm.getDoc_content_bytea5().getFileSize()>0) {
					copyObjectToMap(newparamMap, "doc_content_bytea", regForm.getDoc_content_bytea5().getInputStream());
					copyObjectToMap(newparamMap, "content_type", mimeUtil.getMimeTypes(regForm.getDoc_content_bytea5().getFileData()));
					copyObjectToMap(newparamMap, "fileName", regForm.getDoc_content_bytea5().getFileName());
				} else if (i==5 && regForm.getDoc_content_bytea6() != null && regForm.getDoc_content_bytea6().getFileSize()>0) {
					copyObjectToMap(newparamMap, "doc_content_bytea", regForm.getDoc_content_bytea6().getInputStream());
					copyObjectToMap(newparamMap, "content_type", mimeUtil.getMimeTypes(regForm.getDoc_content_bytea6().getFileData()));
					copyObjectToMap(newparamMap, "fileName", regForm.getDoc_content_bytea6().getFileName());
				} else if (i==6 && regForm.getDoc_content_bytea7() != null && regForm.getDoc_content_bytea7().getFileSize()>0) {
					copyObjectToMap(newparamMap, "doc_content_bytea", regForm.getDoc_content_bytea7().getInputStream());
					copyObjectToMap(newparamMap, "content_type", mimeUtil.getMimeTypes(regForm.getDoc_content_bytea7().getFileData()));
					copyObjectToMap(newparamMap, "fileName", regForm.getDoc_content_bytea7().getFileName());
				}
				if(i>=0 && i<=6 && newparamMap.get("doc_content_bytea")!= null){
					success = persistenceAPI.create(newparamMap, con);
				}
				newparamMap.clear();
			}
		}
		return success;
	}

	public void copyStringToMap(Map params, String key, String value) {

		if (params.containsKey(key)) {
        	String[] obj = (String[]) params.get(key);
        	String[] newArray = Arrays.copyOf(obj, obj.length+1);
        	newArray[obj.length] = value;
        	params.put(key, newArray);

        } else {
        	params.put(key, new String[]{value});
        }
	}

	public void copyObjectToMap(Map params, String key, Object value) {

		if (params.containsKey(key)) {
	       	Object[] obj = (Object[]) params.get(key);
	       	Object[] newArray = Arrays.copyOf(obj, obj.length+1);
	       	newArray[obj.length] = value;
	       	params.put(key, newArray);

		} else {
       		params.put(key, new Object[]{value});
		}
	}

	// Generate Bill

	public boolean regBill(Connection con, String patientId, String patientType, String userName,
			Bill bill, String billType, boolean allowBillInsurance,
			BigDecimal primaryInsuranceApproval, BigDecimal secondaryInsuranceApproval,
			BigDecimal billDeduction,String orgId) throws IOException, SQLException {
		return regBill(con, patientId, patientType, userName, bill,
				billType, allowBillInsurance, primaryInsuranceApproval, secondaryInsuranceApproval, billDeduction, orgId, null);
	}


	public boolean regBill(Connection con, String patientId, String patientType, String userName,
				Bill bill, String billType, boolean allowBillInsurance,
				BigDecimal primaryInsuranceApproval, BigDecimal secondaryInsuranceApproval,
				BigDecimal billDeduction,String orgId,String discountPlanId) throws IOException, SQLException {
		boolean success = true;
		java.util.Date parsedDate = new java.util.Date();
		java.sql.Timestamp datetime = new java.sql.Timestamp(parsedDate.getTime());

		if (billType == null || billType.equals(""))
			return success;

		bill.setVisitId(patientId);
		bill.setVisitType(patientType);
		bill.setOpenedBy(userName);
		bill.setUserName(userName);

		bill.setOpenDate(new Timestamp(parsedDate.getTime()));
		bill.setModTime(new Timestamp(datetime.getTime()));
		bill.setDischarge(Bill.BILL_DISCHARGE_NOTOK);
		bill.setDepositSetOff(BigDecimal.ZERO);
		bill.setPrimaryApprovalAmount(primaryInsuranceApproval);
		bill.setSecondaryApprovalAmount(secondaryInsuranceApproval);
		bill.setInsuranceDeduction(billDeduction);
		bill.setBillLabelId(-1);

		bill.setIs_tpa(allowBillInsurance);
		bill.setBillRatePlanId(orgId);
		bill.setBillDiscountCategory(discountPlanId != null && !discountPlanId.isEmpty() ? Integer.parseInt(discountPlanId) : 0);

    	if (billType.equals("P")) {
    		bill.setBillType(Bill.BILL_TYPE_PREPAID);
			bill.setIsPrimaryBill("N");
			bill.setStatus(Bill.BILL_STATUS_OPEN);

    	} else if (billType.equals("C")) {
    		bill.setBillType(Bill.BILL_TYPE_CREDIT);
			bill.setIsPrimaryBill("Y");
    		bill.setStatus(Bill.BILL_STATUS_OPEN);
    	}

    	Map msgMap = new BillBO().createNewBill(con, bill, false);
		if (msgMap.get("error") != null && !msgMap.get("error").equals("")) {
			success = false;
				return success;
		}
    	return success;
	}

	public boolean updateBillStatus(Connection con, String regAndBill, Bill bill,
			boolean doc_eandm_codification_required) throws SQLException {
		boolean updated = true;
		if (bill != null && bill.getBillType().equals(Bill.BILL_TYPE_PREPAID)) {
			bill.setClaimRecdAmount(BigDecimal.ZERO);
			bill.setRewardPointsRedeemedAmount(BigDecimal.ZERO);

			Timestamp now = DateUtil.getCurrentTimestamp();
			// Close the bill if register and pay.
			if (regAndBill.equals("Y")) {
				// register and pay: close the bill if non-insurance, keep open if insurance
				bill.setPaymentStatus(Bill.BILL_PAYMENT_PAID);
				if (bill.getIs_tpa()) {
					// For a insured pre-paid bill and registered using Register and Pay
					// when doc_eandm_codification_required then bill is open.
					// otherwise bill is finalized.
					if (doc_eandm_codification_required) {
						bill.setOkToDischarge(Bill.BILL_DISCHARGE_NOTOK);
						bill.setStatus(Bill.BILL_STATUS_OPEN);
					} else {
						bill.setStatus(Bill.BILL_STATUS_FINALIZED);
            bill.setFinalizedDate(now);
            bill.setLastFinalizedAt(now);
						bill.setFinalizedBy(bill.getUserName());
					}
				} else {
					bill.setOkToDischarge(Bill.BILL_DISCHARGE_OK);
					bill.setStatus(Bill.BILL_STATUS_CLOSED);
					bill.setClosedBy(bill.getUserName());
					bill.setClosedDate(now);
				}
			}
			updated = new BillDAO(con).updateBill(bill);
		}
		return updated;
	}

	//Generate receipt
	public String regReceipt(String billNo, BigDecimal patientAmt, boolean is_tpa, String billRemarks,
							String printerTypeStr, String customTemplate,
							boolean doc_eandm_codification_required, Map<String, String> billData) throws SQLException, ParseException, Exception {
		boolean success = true;
		HttpSession	session = RequestContext.getSession();
		String userName = (String)session.getAttribute("userid");
		AbstractPaymentDetails bpImpl = AbstractPaymentDetails.getReceiptImpl(AbstractPaymentDetails.BILL_PAYMENT);
		BasicDynaBean billDetails = new GenericDAO("bill").findByKey("bill_no", billNo);
		BillBO billBOObj = new BillBO();
		Bill bill = billBOObj.getBill(billNo);
		String billStatus = bill.getStatus();
		String paymentStatus = bill.BILL_PAYMENT_PAID;

		// Bug # 23460
		if (bill.getIs_tpa() && bill.getBillType().equals(Bill.BILL_TYPE_PREPAID)) {
			List<BasicDynaBean> billCharges = ChargeDAO.getChargeDetailsBean(billNo);
			boolean hasConsultation = false;
			for (BasicDynaBean bean : billCharges) {
				if (((String)bean.get("charge_group")).equals("DOC")) {
					hasConsultation = true;
					break;
				}
			}

			if (hasConsultation && doc_eandm_codification_required)
				billStatus = Bill.BILL_STATUS_OPEN;
			else
				billStatus = Bill.BILL_STATUS_FINALIZED;
		}

		if (((BigDecimal)billDetails.get("total_amount")).compareTo(BigDecimal.ZERO) > 0) {
			boolean isAmountZero = false;

			List insertBillChargeList = new ArrayList();
			List updateBillChargeList = new ArrayList();
			List<Receipt> receiptList = new ArrayList<Receipt>();

			Receipt receiptObj = new Receipt();

			receiptObj.setReceiptType("R");
			receiptObj.setIsSettlement(true);
			receiptObj.setPaymentType("R");
			receiptObj.setBillNo(billNo);
			if (is_tpa) {
				receiptObj.setAmount(patientAmt);
				if (patientAmt.compareTo(BigDecimal.ZERO) == 0)
					isAmountZero = true;
			} else {
				receiptObj.setAmount((BigDecimal)billDetails.get("total_amount"));
				isAmountZero = false;
			}
			receiptObj.setReceiptDate(DateUtil.getCurrentTimestamp());
			receiptObj.setPaymentModeId(-1);
			receiptObj.setCardTypeId(0);
			receiptObj.setUsername(userName);
			receiptObj.setRemarks(billRemarks);
			receiptObj.setCounter((String)session.getAttribute("billingcounterId"));
			receiptList.add(receiptObj);

			if (!isAmountZero) {
				success = billBOObj.updateBillDetails(bill, billStatus, updateBillChargeList,
						insertBillChargeList, receiptList, false, null, false, null, null);
				if (!success)
					return "Bill Updation Error...";

				if (receiptList != null && receiptList.size() > 0) {

					Map printParamMap = new HashMap();
					printParamMap.put("printerTypeStr", printerTypeStr);
					printParamMap.put("customTemplate", customTemplate);
					List<String> printURLs = bpImpl.generatePrintReceiptUrls(receiptList, printParamMap);
					session.setAttribute("printURLs", printURLs);
					if(billData != null) {
					if(bill.getBillType().equalsIgnoreCase(Bill.BILL_TYPE_PREPAID) && bill.getVisitType().equalsIgnoreCase(Bill.BILL_VISIT_TYPE_OP)) {
						if(billStatus != null && (billStatus.equalsIgnoreCase(Bill.BILL_STATUS_CLOSED) || billStatus.equalsIgnoreCase(Bill.BILL_STATUS_FINALIZED))) {
						    BigDecimal netPatientDue = BillDAO.getNetPatientDue(bill.getBillNo());
						    BigDecimal patientDue = BillDAO.getPatientDue(bill.getBillNo());
					        if(netPatientDue.intValue() <= 0 || patientDue.intValue() == 0) {
					        	StringWriter writer = new StringWriter();//"BUILTIN_HTML"		        
						        String template=null;
								if(bill.getBillType().equals("P")){ // Bill Now
									template = GenericPreferencesDAO.getGenericPreferences().getEmailBillNowTemplate();
								}
								else{  // Bill Later
									template = GenericPreferencesDAO.getGenericPreferences().getEmailBillLaterTemplate();
								}
								String[] templateName = template.split("-");
			    		        String[] returnVals = BillPrintHelper.processBillTemplate(writer, bill.getBillNo(), templateName[1], billData.get("userId"));
			        	        String report = writer.toString();
			        	        billData.put("_report_content", report);
			        	        billData.put("_message_attachment", report);
			        	        billData.put("message_attachment_name", "Bill_"+bill.getBillNo());
			        	        billData.put("bill_no", bill.getBillNo());
			        	        billData.put("printtype",String.valueOf(GenericPreferencesDAO.getGenericPreferences().getEmailBillPrint()));
			        	        billData.put("category", "Bill");
			        	        billData.put("bill_date", billData.get("payment_date"));		        	    
			        	        billData.put("bill_amount", ((BigDecimal)billDetails.get("total_amount")).toString());
			        	        billData.put("receipient_id__", billData.get("mr_no"));
			        	        billData.put("receipient_type__", "PATIENT");
						        MessageManager mgr = new MessageManager();
						        if(!bill.getIs_tpa() && patientDue.intValue() == 0){
						        mgr.processEvent("op_bn_cash_bill_paid_closed", billData);
						        }
						        //Sharing of Bill over PHR Practo Drive
								if(netPatientDue.floatValue() <= 0){
							        mgr.processEvent("op_bill_paid", billData);
								}
					        }
						}
					}
					}
				}
			}
		}
		java.sql.Timestamp finalizedDate = DateUtil.getCurrentTimestamp();
		String error = new BillBO().updateBillStatus(bill, billStatus, paymentStatus, "Y", finalizedDate, userName, false, false);

		if (error == null || error.equals("")) error = "";
		else if(error.startsWith("Bill status")) {}
		else error = error + "<br/> Please modify the discount amount and finalize /close the bill again.";

		return error;
	}


	private static final String GET_CATEGORY_DETAILS = "SELECT code,seperate_num_seq FROM patient_category_master "
			+ "WHERE category_id=?";

	public boolean registerPatient(Connection con, BasicDynaBean bean, String password)
			throws SQLException, IOException, NoSuchAlgorithmException {
		PatientDetailsDAO patientDAO = new PatientDetailsDAO();

		PreparedStatement ps = null;
		ResultSet rs = null;
		String new_mrno = null;

		int patientCategoryId = (Integer) bean.get("patient_category_id");
		int defalutCatId = 1;

		if (patientCategoryId != defalutCatId) {
			String categoryCode = null;
			String isSeperateSeqRequire = null;
			ps = con.prepareStatement(GET_CATEGORY_DETAILS);
			ps.setInt(1, patientCategoryId);
			rs = ps.executeQuery();
			if (rs.next()) {
				categoryCode = rs.getString("code");
				isSeperateSeqRequire = rs.getString("seperate_num_seq");
			}
			if ("Y".equals(isSeperateSeqRequire) && !"".equals(categoryCode)) {
				new_mrno = DataBaseUtil.getNextPatternId(categoryCode);

			} else {
				new_mrno = DataBaseUtil.getNextPatternId("MRNO");
			}
		} else {
			new_mrno = DataBaseUtil.getNextPatternId("MRNO");
		}
		bean.set("mr_no", new_mrno);

		boolean success = patientDAO.insert(con, bean);
		if (success) {
			// check if patient portal access is enabled
			Boolean portalAccess = (Boolean) bean.get("portal_access");
			if ((new Boolean(true)).equals(portalAccess)) {
				success = createLoginAccess(con, new_mrno, password);
			}

			if (success) {
				String cityid = (String) bean.get("patient_city");
				String area = (String) bean.get("patient_area");
				if (area != null && !area.equals("")) {
					success = checkAndInsertArea(con, cityid, area);
				}
			}
		}
		DataBaseUtil.closeConnections(null, ps, rs);
		return success;
	}

	public boolean updatePatient(Connection con, BasicDynaBean bean, String password)
			throws SQLException, IOException, NoSuchAlgorithmException {
		PatientDetailsDAO patientDAO = new PatientDetailsDAO();

		Map<String, String> keys = new HashMap<String, String>();
		keys.put("mr_no", bean.get("mr_no").toString());

		Map changes = bean.getMap();

		boolean success = false;
		int update = patientDAO.update(con, changes, keys);
		if (update > 0) {
			success = true;
			Boolean portalAccess = (Boolean) changes.get("portal_access");
			if ((new Boolean(true)).equals(portalAccess)) {
				success = createLoginAccess(con, bean.get("mr_no").toString(), password);
			}

			if (success) {
				String cityid = (String) bean.get("patient_city");
				String area = (String) changes.get("patient_area");
				if (area != null && !area.equals("")) {
					success = checkAndInsertArea(con, cityid, area);
				}
			}
		}
		return success;
	}

	private boolean createLoginAccess(Connection con, String mrNo, String password)throws SQLException,
		IOException, NoSuchAlgorithmException {

		String roleid = DataBaseUtil.getStringValueFromDb("SELECT role_id FROM u_role WHERE portal_id='P' ");
		if (null == roleid || roleid.equals(""))
			return false;
		int roleId = Integer.parseInt(roleid);

		boolean success = false;

		GenericDAO userDAO = new GenericDAO("u_user");
		BasicDynaBean existing = userDAO.findByKey("emp_username", mrNo);
		if (existing != null
				&& (new BigDecimal(roleId).equals(existing.get("role_id")))) {
			// update password for existing
			Map updates = new HashMap();
			boolean isEncrypted = (Boolean)existing.get("is_encrypted");
			password = isEncrypted?PasswordEncoder.encode(password):password;
			updates.put("emp_password", password);

			Map key = new HashMap();
			key.put("emp_username", mrNo);

			int update = userDAO.update(con, updates, key);
			success = (update > 0);
		} else {
			BasicDynaBean userbean = userDAO.getBean();

			userbean.set("emp_username", mrNo);
			userbean.set("emp_password", PasswordEncoder.encode(password));
			userbean.set("role_id", new BigDecimal(roleId));
			userbean.set("hosp_user", "Y");
			userbean.set("emp_status", "A");
			userbean.set("password_change_date", DateUtil.getCurrentTimestamp());
			userbean.set("is_encrypted", true);

			success = userDAO.insert(con, userbean);
		}
		return success;
	}

	private boolean checkAndInsertArea(Connection con, String cityid,
			String areaName) throws SQLException, IOException {
		boolean success = true;

		AreaMasterDAO areadao = new AreaMasterDAO();
		BasicDynaBean areaByCity = areadao.getAreaByCity(cityid, areaName);
		if (areaByCity == null) {
			BasicDynaBean bean = areadao.getBean();
			bean.set("area_id", areadao.getNextFormattedId());
			bean.set("area_name", areaName);
			bean.set("city_id", cityid);

			success = areadao.insert(con, bean);
		}
		return success;
	}

	private static final String getPatMailId = " SELECT pd.mr_no, "
			+ " get_patient_name(pd.salutation, pd.patient_name, pd.middle_name, pd.last_name) as patient_name ,"
			+ " pd.email_id, uu.emp_password"
			+ " FROM patient_details pd "
			+ " LEFT JOIN u_user uu ON (pd.mr_no = uu.emp_username)  "
			+ " WHERE pd.mr_no= ? ";

	private static final String getMailParameters = " SELECT email_template_id, template_name, from_address, subject, mail_message "
			+ " FROM email_template WHERE template_name = 'Patient Registration' and email_category='C' LIMIT 1 ";

	public void sendActivationMail(BasicDynaBean patient, String password) throws SQLException,AddressException, MessagingException {
		HttpServletRequest req = RequestContext.getHttpRequest();

		GenericPreferencesDTO genericPreferences = GenericPreferencesDAO.getGenericPreferences();
		StringBuilder builder = new StringBuilder(req.getScheme());
		builder.append("://");

		String domainName = genericPreferences.getDomainName();
		if (domainName == null || domainName.isEmpty()) {
			domainName = req.getLocalName();
		}
		builder.append(domainName).append(req.getContextPath()).append("/patient/loginForm.do");

		String portalUrl = builder.toString();

		String hospitalName = genericPreferences.getHospitalName();

		String recipients[] = new String[1];
		String from = null;
		String subject = null;
		String message = null;
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet patResultSet = null, emilResultSet = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(getPatMailId);
			ps.setString(1, patient.get("mr_no").toString());
			patResultSet = ps.executeQuery();
			if (patResultSet.next()) {
				recipients[0] = patResultSet.getString("email_id");
			}

			ps = con.prepareStatement(getMailParameters);
			emilResultSet = ps.executeQuery();
			if (emilResultSet.next()) {
				from = emilResultSet.getString("from_address");
				subject = emilResultSet.getString("subject");
				message = emilResultSet.getString("mail_message");
			}

			TemplateMatcher matcher = new TemplateMatcher("${", "}");

			Map<String, String> vars = new HashMap<String, String>();
			vars.put("user", patResultSet.getString("patient_name"));
			vars.put("hospital", hospitalName);
			vars.put("mrno", patient.get("mr_no").toString());
			vars.put("password", password);

			vars.put("portalurl", portalUrl);

			MailService ms = new MailService();
			ms.sendMail(from, recipients, matcher.replace(subject, vars),
					matcher.replace(message, vars));

		} finally {
			DataBaseUtil.closeConnections(null, null, patResultSet);
			DataBaseUtil.closeConnections(con, ps, emilResultSet);
		}
	}

	public boolean insertRegistrationCharges(Connection con, Bill bill,
			BasicDynaBean visitDetailsBean, int[] planIds, boolean isRenewal,String registrationChargesApplicable,
			String[] preAuthIds, Integer[] preAuthModeIds, boolean isInsurance, boolean noGenRegCharge)
			throws SQLException, IOException {
		Map<Integer, Integer> categoryCountMap = new HashMap<Integer, Integer>();
		boolean status = true;
		// boolean isInsurance = Integer.parseInt(visitDetailsBean.get("insurance_id")== null? "0": (String)visitDetailsBean.get("insurance_id")) > 0;
		ChargeDAO chargeDAO = new ChargeDAO(con);
		BillChargeClaimDAO chargeClaimDAO = new BillChargeClaimDAO();

		List<ChargeDTO> regCharges = new ArrayList();

		// BUG : 20027 - Post Reg charges if tpa selected and registration
		// charge applicable = 'Y'
		String regChargeApplicable = "Y";
		if (visitDetailsBean.get("patient_category_id") != null
				&& !(visitDetailsBean.get("patient_category_id").toString()).equals("")) {
			BasicDynaBean bean = new PatientCategoryDAO().findByKey(
					"category_id", new Integer(visitDetailsBean.get(
							"patient_category_id").toString()));
			if (bean != null
					&& bean.get("registration_charge_applicable") != null)
				regChargeApplicable = (String) bean.get("registration_charge_applicable");
		}

		if(registrationChargesApplicable.equals("N"))
			regChargeApplicable = "N";

		if (regChargeApplicable != null && regChargeApplicable.equals("Y")) {
			/*
			 * General Registration charge or renewal charge. (validity is
			 * checked before setting reg_charge_accepted)
			 */
			if ("Y".equals(visitDetailsBean.get("reg_charge_accepted")) && !noGenRegCharge) {

				List<ChargeDTO> genRegCharges = OrderBO.getRegistrationCharges(
						(String) visitDetailsBean.get("bed_type"),
						(String) visitDetailsBean.get("org_id"), "GREG",
						isRenewal, isInsurance, planIds, true, bill
								.getVisitType(), visitDetailsBean
								.get("patient_id") == null ? null
								: (String) visitDetailsBean.get("patient_id"), con, null);

				if (isRenewal) {
					for (ChargeDTO ch : genRegCharges) {
						ch.setActDescription("Registration Renewal charge");
					}
				}

				regCharges.addAll(genRegCharges);
			}

			/*
			 * Ip or Op visit charge.
			 */
			if ("o".equals(visitDetailsBean.get("visit_type"))) {
				List<ChargeDTO> opRegCharges = OrderBO.getRegistrationCharges(
						(String) visitDetailsBean.get("bed_type"),
						(String) visitDetailsBean.get("org_id"), "OPREG",
						false, isInsurance, planIds, true, bill.getVisitType(),
						visitDetailsBean.get("patient_id") == null ? null
								: (String) visitDetailsBean.get("patient_id"), con, null);
				regCharges.addAll(opRegCharges);
			}

			if ("i".equals(visitDetailsBean.get("visit_type"))) {
				List<ChargeDTO> ipRegCharges = OrderBO.getRegistrationCharges(
						(String) visitDetailsBean.get("bed_type"),
						(String) visitDetailsBean.get("org_id"), "IPREG",
						false, isInsurance, planIds, true, bill.getVisitType(),
						visitDetailsBean.get("patient_id") == null ? null
								: (String) visitDetailsBean.get("patient_id"), con, null);
				regCharges.addAll(ipRegCharges);
			}
			
			/*
	     * Mrd charge.
	     */
	    List<ChargeDTO> mrdCharges = OrderBO.getRegistrationCharges(
	          (String) visitDetailsBean.get("bed_type"),
	          (String) visitDetailsBean.get("org_id"), "EMREG", false,
	          isInsurance, planIds, true, bill.getVisitType(),
	          visitDetailsBean.get("patient_id") == null ? null
	              : (String) visitDetailsBean.get("patient_id"), con, null);
	    regCharges.addAll(mrdCharges);
		}

		/*
		 * Mlc charge.
		 */
		if ("Y".equals(visitDetailsBean.get("mlc_status"))) {
			List<ChargeDTO> mlcCharges = OrderBO.getRegistrationCharges(
					(String) visitDetailsBean.get("bed_type"),
					(String) visitDetailsBean.get("org_id"), "MLREG", false,
					isInsurance, planIds, true, bill.getVisitType(),
					visitDetailsBean.get("patient_id") == null ? null
							: (String) visitDetailsBean.get("patient_id"), con,	null);
			regCharges.addAll(mlcCharges);
		}

		DiscountPlanBO discBO = new DiscountPlanBO();
		//say some details abt discount plan
		discBO.setDiscountPlanDetails(bill.getBillDiscountCategory());
		// set the bill no, next charge id and username in all the charges
		for (ChargeDTO charge : regCharges) {
			charge.setBillNo(bill.getBillNo());
			charge.setChargeId(chargeDAO.getNextChargeId());
			charge.setUsername(bill.getUserName());
			discBO.applyDiscountRule(con, charge);

			if (charge.getVisitId() == null)
				charge.setVisitId(visitDetailsBean.get("patient_id") == null ? null
								: (String) visitDetailsBean.get("patient_id"));
			if (categoryCountMap.containsKey(charge.getInsuranceCategoryId())) {
				int count = (Integer) categoryCountMap.get(charge.getInsuranceCategoryId());
				count++;
				categoryCountMap.put(charge.getInsuranceCategoryId(), count);
				charge.setFirstOfCategory(false);
				if(null != planIds && planIds.length > 0) {
					charge.setInsuranceAmt(planIds, charge.getVisitType(), false);
					charge.setPreAuthIds(preAuthIds);
					charge.setPreAuthModeIds(preAuthModeIds);
				}
			} else {
				categoryCountMap.put(charge.getInsuranceCategoryId(), 1);
				if(null != planIds && planIds.length > 0) {
					charge.setInsuranceAmt(planIds, charge.getVisitType(), true);
					charge.setPreAuthIds(preAuthIds);
					charge.setPreAuthModeIds(preAuthModeIds);
				}
			}
		}

		status = chargeDAO.insertCharges(regCharges);

		if (status) {
			for (ChargeDTO charge : regCharges) {
				if (charge.getChargeHead().equals("MLREG")) {
					chargeDAO.insertActivity(charge.getChargeId(),
							visitDetailsBean.get("patient_id").toString(),
							ChargeDTO.CH_MLC_REGISTRATION);
				}
			}
		}

		if(status && bill.getIs_tpa()){
			chargeClaimDAO.insertBillChargeClaims(con,regCharges,planIds,
					(String)visitDetailsBean.get("patient_id"),bill.getBillNo());
		}
		return status;
	}

	public static final String GET_AUTO_REG_DOCS = "SELECT prc.doc_id, template_id, doc_format, doc_name "
			+ "FROM patient_documents pd "
			+ "JOIN patient_registration_cards prc USING (doc_id) WHERE patient_id = ?";

	public static List<BasicDynaBean> getAutoTemplates(String patientId)
			throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(GET_AUTO_REG_DOCS);
			ps.setString(1, patientId);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public static final String GET_PATIENT_POLICY_DETAILS = "SELECT DISTINCT pip.insurance_co, pip.sponsor_id, pip.plan_id, "
			+  "pip.plan_type_id, ppd.member_id, ppd.policy_number,ppd.policy_validity_start, ppd.policy_validity_end, "
			+  "ppd.policy_holder_name, ppd.patient_relationship, pip.patient_policy_id, pip.prior_auth_id, pip.prior_auth_mode_id, "
			+  "ppd.mr_no, pip.priority, icm.insurance_co_id, icm.insurance_co_name "
			+"FROM patient_policy_details ppd "
			+  "JOIN insurance_plan_main ipm USING (plan_id) "
			+  "JOIN patient_insurance_plans pip ON (ppd.patient_policy_id = pip.patient_policy_id) "
			+  "LEFT JOIN insurance_company_master icm USING (insurance_co_id) "
			+"WHERE ppd.mr_no= ? AND ppd.member_id!='' AND ppd.status ='A' ORDER BY pip.priority";

	public static List<BasicDynaBean> getPatientPolicyDetails(String mrNo)
			throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(GET_PATIENT_POLICY_DETAILS);
			ps.setString(1, mrNo);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public static final String GET_PATIENT_CORPORATE_DETAILS = "SELECT * "
		+ " FROM patient_corporate_details pcd "
		+ " JOIN tpa_master tm ON (tm.tpa_id = pcd.sponsor_id) "
		+ " WHERE mr_no= ? AND employee_id!='' AND pcd.status ='A' ";

	public static List<BasicDynaBean> getPatientCorporateDetails(String mrNo)
			throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(GET_PATIENT_CORPORATE_DETAILS);
			ps.setString(1, mrNo);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public static final String GET_PATIENT_NATIONAL_DETAILS = "SELECT * "
		+ " FROM patient_national_sponsor_details pnd "
		+ " JOIN tpa_master tm ON (tm.tpa_id = pnd.sponsor_id) "
		+ " WHERE mr_no= ? AND national_id!='' AND pnd.status ='A' ";

	public static List<BasicDynaBean> getPatientNationalDetails(String mrNo)
		throws SQLException {
	Connection con = DataBaseUtil.getConnection();
	PreparedStatement ps = null;
	try {
		ps = con.prepareStatement(GET_PATIENT_NATIONAL_DETAILS);
		ps.setString(1, mrNo);
		return DataBaseUtil.queryToDynaList(ps);
	} finally {
		DataBaseUtil.closeConnections(con, ps);
	}
	}

	/*public boolean regCorporateDetails(Connection con, BasicDynaBean visitDetailsBean,
			String regType, HashMap<String, Object> corporateDetailsMap) throws SQLException, IOException, ParseException {
		boolean success = true;
		String sponsorId = (String)corporateDetailsMap.get("sponsor_id");
		String mrNo = (String)visitDetailsBean.get("mr_no");
		String employeeId = (String)corporateDetailsMap.get("employee_id");
		String employeeName = (String)corporateDetailsMap.get("employee_name");
		String relation = (String)corporateDetailsMap.get("patient_relationship");
		String startValidity = (String)corporateDetailsMap.get("validity_start");
		String endValidity = (String)corporateDetailsMap.get("validity_end");

		int nextpatientCorporateId = getnextPatientCorporateDetailsId();
		boolean insertCorporateDet = false;

		GenericDAO corporateDetDao= new GenericDAO("patient_corporate_details");
		if(sponsorId != null && !sponsorId.equals("")) {
			BasicDynaBean corporateBean = corporateDetDao.getBean();
			corporateBean.set("mr_no", mrNo);
			corporateBean.set("employee_id", employeeId);
			corporateBean.set("employee_name", employeeName);
			corporateBean.set("sponsor_id", sponsorId);
			corporateBean.set("patient_relationship", relation);
			corporateBean.set("validity_start", startValidity != null ? DataBaseUtil.parseDate(startValidity) : null);
			corporateBean.set("validity_end", endValidity != null ? DataBaseUtil.parseDate(endValidity) : null);

			if (regType.equals("new")) {
				insertCorporateDet = true;

			}else if (regType.equals("regd")) {
				List<BasicDynaBean> existingCorporateDetailsList = corporateDetDao.findAllByKey("mr_no", mrNo);

				// Get the patient corporate id if the corporate has validity.
				int patientCorporateId = corporateValidityExists(existingCorporateDetailsList,
						employeeId, sponsorId, DataBaseUtil.parseDate(endValidity));

				if (patientCorporateId == 0)
					insertCorporateDet = true;
				else
					visitDetailsBean.set("patient_policy_id", patientCorporateId);
			}

			if (insertCorporateDet) {

				// Update the expired policies as Inactive.
				Map keys = new HashMap();
				Map fields = new HashMap();
				fields.put("status", "I");

				keys.put("mr_no", mrNo);
				keys.put("employee_id", employeeId);

				corporateDetDao.update(con, fields, keys);

				// Insert the corporate details with new corporate Validity end date.
				visitDetailsBean.set("patient_corporate_id", nextpatientCorporateId);
				corporateBean.set("patient_corporate_id", nextpatientCorporateId);
				success = corporateDetDao.insert(con, corporateBean);
			}
		}
		return success;
	}

	public int corporateValidityExists(List<BasicDynaBean> existingPatPolicyDetailsList,
				String employeeId, String sponsorId, Date endValidity) {
		if (existingPatPolicyDetailsList == null || existingPatPolicyDetailsList.size() == 0)
			return 0;

		for(BasicDynaBean b: existingPatPolicyDetailsList) {
			if(((String)b.get("employee_id")).equals(employeeId)
				&& ((Integer)b.get("sponsor_id")).equals(sponsorId)
				&& ((Date)b.get("validity_end")).getTime() == endValidity.getTime()) {
				return (Integer)b.get("patient_corporate_id");
			}
		}
		return 0;
	}*/

	public boolean regCorporateDetails(Connection con, BasicDynaBean visitDetailsBean,
			 HashMap<String, Object> corporateDetailsMap, boolean isPrimary) throws SQLException, IOException, ParseException {
		boolean success = true;
		String sponsorId = (String)corporateDetailsMap.get("sponsor_id");
		String mrNo = (String)visitDetailsBean.get("mr_no");
		String employeeId = (String)corporateDetailsMap.get("employee_id");
		String employeeName = (String)corporateDetailsMap.get("employee_name");
		String relation = (String)corporateDetailsMap.get("patient_relationship");

		GenericDAO corporateDetDao= new GenericDAO("patient_corporate_details");
		List<BasicDynaBean> existingCorporateIdList = corporateDetDao.findAllByKey("mr_no", mrNo);

		int patientCorporateId = corporateIdExists(existingCorporateIdList,	employeeId, sponsorId);

		if(sponsorId != null && !sponsorId.equals("")) {

			if (patientCorporateId == 0) {
				patientCorporateId = getnextPatientCorporateDetailsId();
				BasicDynaBean corporateBean = corporateDetDao.getBean();
				corporateBean.set("mr_no", mrNo);
				corporateBean.set("employee_id", employeeId);
				corporateBean.set("employee_name", employeeName);
				corporateBean.set("sponsor_id", sponsorId);
				corporateBean.set("patient_relationship", relation);

				corporateBean.set("patient_corporate_id", patientCorporateId);

				success = corporateDetDao.insert(con, corporateBean);
			}else {
				BasicDynaBean corporateBean = corporateDetDao.findByKey("patient_corporate_id", patientCorporateId);

				corporateBean.set("employee_name", employeeName);
				corporateBean.set("sponsor_id", sponsorId);
				corporateBean.set("patient_relationship", relation);

				corporateBean.set("patient_corporate_id", patientCorporateId);

				success = (corporateDetDao.updateWithName(con, corporateBean.getMap(), "patient_corporate_id")) > 0;
			}

			if(isPrimary)
				visitDetailsBean.set("patient_corporate_id", patientCorporateId);
			else
				visitDetailsBean.set("secondary_patient_corporate_id", patientCorporateId);
		}
		return success;
	}

	public int corporateIdExists(List<BasicDynaBean> existingCorporateIdList,
			String employeeId, String sponsorId) {
		if (existingCorporateIdList == null || existingCorporateIdList.size() == 0)
			return 0;

		for(BasicDynaBean b: existingCorporateIdList) {
			if(((String)b.get("employee_id")).equals(employeeId)
				&& ((String)b.get("sponsor_id")).equals(sponsorId)) {
				return (Integer)b.get("patient_corporate_id");
			}
		}
		return 0;
	}

	public Integer getnextPatientCorporateDetailsId() throws SQLException{
		String query = "SELECT nextval('patient_corporate_id_seq')";
		Connection con = DataBaseUtil.getReadOnlyConnection();
		PreparedStatement ps=null;
		try {
			ps = con.prepareStatement(query);
			return DataBaseUtil.getIntValueFromDb(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public boolean regNationalDetails(Connection con, BasicDynaBean visitDetailsBean,
			 HashMap<String, Object> nationalDetailsMap, boolean isPrimary) throws SQLException, IOException, ParseException {
		boolean success = true;
		String sponsorId = (String)nationalDetailsMap.get("sponsor_id");
		String mrNo = (String)visitDetailsBean.get("mr_no");
		String nationalId = (String)nationalDetailsMap.get("national_id");
		String citizenName = (String)nationalDetailsMap.get("citizen_name");
		String relation = (String)nationalDetailsMap.get("patient_relationship");

		GenericDAO nationalDetDao= new GenericDAO("patient_national_sponsor_details");
		List<BasicDynaBean> existingNationalIdList = nationalDetDao.findAllByKey("mr_no", mrNo);

		int patientNationalId = nationalIdExists(existingNationalIdList, nationalId, sponsorId);

		if(sponsorId != null && !sponsorId.equals("")) {

			if (patientNationalId == 0) {
				patientNationalId = getnextPatientNationalDetailsId();
				BasicDynaBean nationalBean = nationalDetDao.getBean();
				nationalBean.set("mr_no", mrNo);
				nationalBean.set("national_id", nationalId);
				nationalBean.set("citizen_name", citizenName);
				nationalBean.set("sponsor_id", sponsorId);
				nationalBean.set("patient_relationship", relation);

				nationalBean.set("patient_national_sponsor_id", patientNationalId);

				success = nationalDetDao.insert(con, nationalBean);
			}else {
				BasicDynaBean nationalBean = nationalDetDao.findByKey("patient_national_sponsor_id", patientNationalId);

				nationalBean.set("citizen_name", citizenName);
				nationalBean.set("sponsor_id", sponsorId);
				nationalBean.set("patient_relationship", relation);

				nationalBean.set("patient_national_sponsor_id", patientNationalId);

				success = (nationalDetDao.updateWithName(con, nationalBean.getMap(), "patient_national_sponsor_id")) > 0;
			}

			if(isPrimary)
				visitDetailsBean.set("patient_national_sponsor_id", patientNationalId);
			else
				visitDetailsBean.set("secondary_patient_national_sponsor_id", patientNationalId);
		}
		return success;
	}

	public int nationalIdExists(List<BasicDynaBean> existingNationalIdList,
			String nationalId, String sponsorId) {
		if (existingNationalIdList == null || existingNationalIdList.size() == 0)
			return 0;

		for(BasicDynaBean b: existingNationalIdList) {
			if(((String)b.get("national_id")).equals(nationalId)
				&& ((String)b.get("sponsor_id")).equals(sponsorId)) {
				return (Integer)b.get("patient_national_sponsor_id");
			}
		}
		return 0;
	}


	public Integer getnextPatientNationalDetailsId() throws SQLException{
		String query = "SELECT nextval('patient_national_sponsor_id_seq')";
		Connection con = DataBaseUtil.getReadOnlyConnection();
		PreparedStatement ps=null;
		try {
			ps = con.prepareStatement(query);
			return DataBaseUtil.getIntValueFromDb(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public boolean checkToCreatePrepaidBill(String regAndBill, boolean isTpa,
			BigDecimal estimatedAmt, String billType) {

		if (!isTpa && (regAndBill != null && regAndBill.equals("Y"))
				&& billType.equals(Bill.BILL_TYPE_PREPAID)
				&& estimatedAmt.compareTo(BigDecimal.ZERO) == 0)
			return false;
		return true;
	}

	/*
	 * Need a singleton mimeutil, it leaks memory everytime we register.
	 */
	private static MimeUtil2 mimeUtil = null;
	private MimeUtil2 getMimeUtil() {
		if (mimeUtil == null) {
			mimeUtil = new MimeUtil2();
			mimeUtil.registerMimeDetector("eu.medsea.mimeutil.detector.MagicMimeMimeDetector");
		}
		return mimeUtil;
	}


	public void createInsuranceDetailsMap(HttpServletRequest request,
			HashMap<String, Object> policyDetailsMap,HashMap<String, Object> secPolicyDetailsMap) throws ParseException{

		String primarySponsor = request.getParameter("primary_sponsor");
		String secondarySponsor = request.getParameter("secondary_sponsor");

		if (primarySponsor != null) {
			if (primarySponsor.equals("I")) {

				BigDecimal policyApprovalAmount = null;

				if (request.getParameter("primary_insurance_approval") != null
						&& !request.getParameter("primary_insurance_approval").trim().equals("")) {
					policyApprovalAmount = new BigDecimal(request.getParameter("primary_insurance_approval"));
				}

				String startDateStr = getParamDefault(request,"primary_policy_validity_start", null);
				String endDateStr = getParamDefault(request,"primary_policy_validity_end", null);

				java.sql.Date startValidity = startDateStr!= null ? DataBaseUtil.parseDate(startDateStr) : null;
				java.sql.Date endValidity = endDateStr!= null ? DataBaseUtil.parseDate(endDateStr) : null;

				policyDetailsMap.put("policy_no", getParamDefault(request, "primary_member_id", null));
				policyDetailsMap.put("policy_number", getParamDefault(request, "primary_policy_number", null));
				policyDetailsMap.put("policy_holder_name", getParamDefault(request,
						"primary_policy_holder_name", null));
				policyDetailsMap.put("policy_validity_start",startValidity);
				policyDetailsMap.put("policy_validity_end", endValidity);
				policyDetailsMap.put("patient_relationship", getParamDefault(request,
						"primary_patient_relationship", null));
				policyDetailsMap.put("approved_amt", policyApprovalAmount);
				policyDetailsMap.put("prior_auth_id",  getParamDefault(request,
						"primary_prior_auth_id", null));
				policyDetailsMap.put("prior_auth_mode_id",  getParamDefault(request,
						"primary_prior_auth_mode_id", "1"));
				policyDetailsMap.put("approved_amt", policyApprovalAmount);
				policyDetailsMap.put("document_usage", getParamDefault(request,
						"primary_insurance_document_usage", "New"));

			} 
		}

		if (secondarySponsor != null) {
			if (secondarySponsor.equals("I")) {

				BigDecimal policyApprovalAmount = null;

				if (request.getParameter("secondary_insurance_approval") != null
						&& !request.getParameter("secondary_insurance_approval").trim().equals("")) {
					policyApprovalAmount = new BigDecimal(request
							.getParameter("secondary_insurance_approval"));
				}
				String startDateStr = getParamDefault(request,"secondary_policy_validity_start", null);
				String endDateStr = getParamDefault(request,"secondary_policy_validity_end", null);

				java.sql.Date startValidity = startDateStr!= null ? DataBaseUtil.parseDate(startDateStr) : null;
				java.sql.Date endValidity = startDateStr!= null ? DataBaseUtil.parseDate(endDateStr) : null;

				secPolicyDetailsMap.put("policy_no", getParamDefault(request, "secondary_member_id", null));
				secPolicyDetailsMap.put("policy_number", getParamDefault(request, "secondary_policy_number", null));
				secPolicyDetailsMap.put("policy_holder_name", getParamDefault(request,
						"secondary_policy_holder_name", null));
				secPolicyDetailsMap.put("policy_validity_start", startValidity);
				secPolicyDetailsMap.put("policy_validity_end", endValidity);
				secPolicyDetailsMap.put("patient_relationship", getParamDefault(request,
						"secondary_patient_relationship", null));
				secPolicyDetailsMap.put("approved_amt", policyApprovalAmount);
				secPolicyDetailsMap.put("prior_auth_id",  getParamDefault(request,
						"secondary_prior_auth_id", null));
				secPolicyDetailsMap.put("prior_auth_mode_id",  getParamDefault(request,
						"secondary_prior_auth_mode_id", "1"));
				secPolicyDetailsMap.put("document_usage", getParamDefault(request,
						"secondary_insurance_document_usage", "New"));

			} 
		}
	}

	public static String getParamDefault(HttpServletRequest req, String paramName, String defaultValue) {
		String value = req.getParameter(paramName);
		if ((value == null) || value.equals(""))
			value = defaultValue;
		return value;
	}

	public void copyComplaintAndEncounterTypes(BasicDynaBean visitDetailsBean) throws SQLException {
		String opType = (String)visitDetailsBean.get("op_type");

		if (opType != null && (opType.equals("F") || opType.equals("D"))) {

			String mainVisitId = (String)visitDetailsBean.get("main_visit_id");
			String visitId = (String)visitDetailsBean.get("patient_id");
			List<BasicDynaBean> episodePreviousVisitDetails = VisitDetailsDAO.getPreviousEpisodeVisitDetails(mainVisitId);
			String latestEpisodeVisitId = null;

			if (episodePreviousVisitDetails != null && episodePreviousVisitDetails.size() > 0) {
				for (BasicDynaBean eb : episodePreviousVisitDetails) {
					latestEpisodeVisitId = (String)eb.get("patient_id");
					break;
				}
			}

			VisitDetailsDAO visitdao = new VisitDetailsDAO();
			BasicDynaBean previousVisitBean = visitdao.findByKey("patient_id", latestEpisodeVisitId);
			if (previousVisitBean != null) {
				visitDetailsBean.set("encounter_start_type", previousVisitBean.get("encounter_start_type"));
				visitDetailsBean.set("encounter_end_type", previousVisitBean.get("encounter_end_type"));
				visitDetailsBean.set("complaint", previousVisitBean.get("complaint"));
			}
		}
	}

	public static final String GET_PATIENT_PLANS_DETAILS = "SELECT DISTINCT pip.insurance_co, pip.sponsor_id, pip.plan_id, "
			+  "pip.plan_type_id, ppd.member_id, ppd.policy_number,ppd.policy_validity_start, ppd.policy_validity_end, "
			+  "ppd.policy_holder_name, ppd.patient_relationship, pip.patient_policy_id, pip.prior_auth_id, pip.prior_auth_mode_id, "
			+  "ppd.mr_no, pip.priority, icm.insurance_co_id, icm.insurance_co_name,"
			+  "pip.use_drg, pip.use_perdiem, pip.per_diem_code, pip.insurance_approval,ppd.status, "
			+  "pip.plan_limit,pip.visit_limit,pip.visit_deductible,pip.visit_copay_percentage,pip.visit_max_copay_percentage, "
			+  "pip.visit_per_day_limit,pip.episode_limit,pip.episode_deductible,pip.episode_copay_percentage,pip.episode_max_copay_percentage, "
			+"  pip.utilization_amount FROM patient_insurance_plans pip "
			+  "LEFT JOIN insurance_plan_main ipm USING (plan_id) "
			+  "LEFT JOIN patient_policy_details ppd ON (ppd.patient_policy_id = pip.patient_policy_id) "
			+  "LEFT JOIN insurance_company_master icm USING (insurance_co_id) "
			+"WHERE pip.mr_no= ? AND pip.patient_id = ?  "
			+"AND pip.use_drg = 'N' ";

	public static List<BasicDynaBean> getPatientPlansDetails(String mrNo, String lastVisitID)
			throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(GET_PATIENT_PLANS_DETAILS+ " ORDER BY pip.priority");
			ps.setString(1, mrNo);
			ps.setString(2, lastVisitID);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public static List<BasicDynaBean> getPatientPlansDetailsForEditIns(String mrNo, String lastVisitID)
			throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		String GET_PATIENT_PLANS_DETAILS_FOR_EDIT_INS = GET_PATIENT_PLANS_DETAILS+" ORDER BY pip.priority";
		GET_PATIENT_PLANS_DETAILS_FOR_EDIT_INS = GET_PATIENT_PLANS_DETAILS_FOR_EDIT_INS.replace("AND pip.use_drg = 'N'", "");
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(GET_PATIENT_PLANS_DETAILS_FOR_EDIT_INS);
			ps.setString(1, mrNo);
			ps.setString(2, lastVisitID);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public boolean regPolicyDetailsN(Connection con, BasicDynaBean visitDetailsBean, String regType,
			int planId, Map policyDetailsMap) throws SQLException, IOException, ParseException {
		boolean success = true;
		String mrNo = (String)visitDetailsBean.get("mr_no");
		String visitId =(String)visitDetailsBean.get("patient_id");
		String policyNo = (String)policyDetailsMap.get("policy_no");
		String policyNumber = (String)policyDetailsMap.get("policy_number");
		String policyHolder = (String)policyDetailsMap.get("policy_holder_name");
		String relation = (String)policyDetailsMap.get("patient_relationship");
		Date startValidity = (Date)policyDetailsMap.get("policy_validity_start");
		Date endValidity = (Date)policyDetailsMap.get("policy_validity_end");

		int nextPatientPolicyDetId = getnextPatientPolicyDetailsId();

		GenericDAO patPolicyDao= new GenericDAO("patient_policy_details");
		if(planId > 0) {
			BasicDynaBean patientPolicyBean = patPolicyDao.getBean();
			patientPolicyBean.set("mr_no", mrNo);
			patientPolicyBean.set("member_id", (policyNo != null ? policyNo.trim() : null));
			patientPolicyBean.set("policy_number", policyNumber);
			patientPolicyBean.set("policy_holder_name", policyHolder);
			patientPolicyBean.set("policy_validity_start", startValidity);
			patientPolicyBean.set("policy_validity_end", endValidity);
			patientPolicyBean.set("plan_id", planId);
			patientPolicyBean.set("patient_relationship", relation);
			patientPolicyBean.set("visit_id", visitId);

			BasicDynaBean existingPatPolicyDetails = patPolicyDao.findByKey("visit_id", visitId);

			if(null != existingPatPolicyDetails && existingPatPolicyDetails.getMap().size()>0){
				Map keys = new HashMap();
				keys.put("mr_no", mrNo);
				keys.put("plan_id", planId);
				keys.put("visit_id",visitId);
				patPolicyDao.update(con, patientPolicyBean.getMap(), keys);
				visitDetailsBean.set("patient_policy_id", existingPatPolicyDetails.get("patient_policy_id"));
			}else{
				visitDetailsBean.set("patient_policy_id", nextPatientPolicyDetId);
				patientPolicyBean.set("patient_policy_id", nextPatientPolicyDetId);
				success = patPolicyDao.insert(con, patientPolicyBean);
			}

		}
		return true;
	}

	public static final String GET_ALL_PATIENT_PLANS_DETAILS = "SELECT DISTINCT pip.insurance_co, pip.sponsor_id, pip.plan_id, "
		+  "pip.plan_type_id, ppd.member_id, ppd.policy_number,ppd.policy_validity_start, ppd.policy_validity_end, "
		+  "ppd.policy_holder_name, ppd.patient_relationship, pip.patient_policy_id, pip.prior_auth_id, pip.prior_auth_mode_id, "
		+  "ppd.mr_no, pip.priority, icm.insurance_co_id, icm.insurance_co_name,"
		+  "pip.use_drg, pip.use_perdiem, pip.per_diem_code, pip.insurance_approval,ppd.status, "
		+  "pip.plan_limit,pip.visit_limit,pip.visit_deductible,pip.visit_copay_percentage,pip.visit_max_copay_percentage, "
		+  "pip.visit_per_day_limit,pip.episode_limit,pip.episode_deductible,pip.episode_copay_percentage,pip.episode_max_copay_percentage, "
		+"  pip.utilization_amount FROM patient_insurance_plans pip "
		+  "LEFT JOIN insurance_plan_main ipm USING (plan_id) "
		+  "LEFT JOIN patient_policy_details ppd ON (ppd.patient_policy_id = pip.patient_policy_id) "
		+  "LEFT JOIN insurance_company_master icm USING (insurance_co_id) "
		+"WHERE pip.mr_no= ? "
		+"AND pip.use_drg = 'N' ";

	public static List<BasicDynaBean> getAllPatientPlansDetailsUsingMRNO(String mrno) throws SQLException {

		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(GET_ALL_PATIENT_PLANS_DETAILS+ " ORDER BY pip.priority,pip.patient_policy_id");
			ps.setString(1, mrno);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}
	
	public static BigDecimal getAvailableGeneralAndIpDeposit(String mrNo) throws SQLException {
		BigDecimal depositBal = BigDecimal.ZERO;
		BasicDynaBean depositBean = DepositsDAO.getAvailableGeneralAndIpDeposit(mrNo);
		if(depositBean != null) {
			depositBal = (BigDecimal)depositBean.get("hosp_available_deposit");
		}
		return depositBal;
	}

  public static BigDecimal getAvailableGeneralAndIpDeposit(Connection con, String mrNo) throws SQLException {
    BigDecimal depositBal = BigDecimal.ZERO;
    BasicDynaBean depositBean = DepositsDAO.getAvailableGeneralAndIpDeposit(con, mrNo);
    if(depositBean != null) {
      depositBal = (BigDecimal)depositBean.get("hosp_available_deposit");
    }
    return depositBal;
  }
}
