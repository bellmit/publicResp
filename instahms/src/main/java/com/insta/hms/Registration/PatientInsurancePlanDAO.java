package com.insta.hms.Registration;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.beanutils.BasicDynaBean;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericDAO;

public class PatientInsurancePlanDAO extends GenericDAO {
  
  private static final GenericDAO patientInsurancePlanDetailsDAO =
      new GenericDAO("patient_insurance_plan_details");
  
  private static final GenericDAO patientRegistrationDAO = new GenericDAO("patient_registration");
  private static final GenericDAO insurancePlanMainDAO = new GenericDAO("insurance_plan_main");


	public PatientInsurancePlanDAO() {
		super("patient_insurance_plans");
		// TODO Auto-generated constructor stub
	}

	// TODO MP: return a list of all the sponsors (with plan) for this bill using the visit id

	private static String SPONSOR_DETAILS = " SELECT pip.patient_id, pip.plan_id, " +
		" pip.sponsor_id, tpa.tpa_name,tpa.sponsor_type,pip.priority, tpa.claim_amount_includes_tax, tpa.limit_includes_tax "+
	    " FROM patient_insurance_plans pip "+
	    " LEFT JOIN tpa_master tpa ON (tpa.tpa_id = pip.sponsor_id) "+
	    " WHERE pip.patient_id = ? order by priority";

	private static String PLAN_DETAILS_FIELDS = " SELECT pip.patient_id, pip.plan_id, " +
		" pip.sponsor_id, tpa.tpa_name,tpa.sponsor_type,pip.priority ";

	private String PLAN_DETAILS_TABLES =
		 " FROM patient_insurance_plans pip "+
		 " LEFT JOIN tpa_master tpa ON (tpa.tpa_id = pip.sponsor_id) ";

	private String PLAN_DETAILS_WHERE =
		 " WHERE pip.patient_id = ? AND pip.plan_id IS NOT NULL order by priority";

	private String PLAN_SPONSOR_DETAILS_FIELDS =
		" ,ppd.member_id,ppd.policy_validity_end,icam.category_name AS plan_type_name," +
		" ipm.plan_name,ipm.plan_exclusions, ipm.plan_notes, ipm.discount_plan_id, icm.insurance_co_name,icm.insurance_rules_doc_name";

	private String PLAN_SPONSOR_DETAILS_JOINS =
		" JOIN insurance_company_master icm ON icm.insurance_co_id = pip.insurance_co " +
		" JOIN insurance_plan_main ipm ON (pip.plan_id = ipm.plan_id) " +
		" JOIN insurance_category_master icam  ON icam.category_id=ipm.category_id" +
		" JOIN patient_policy_details ppd ON (ppd.patient_policy_id = pip.patient_policy_id)";

	public  List<BasicDynaBean> getSponsorDetails(Connection con,String visitId) throws SQLException {
		return DataBaseUtil.queryToDynaList(con,SPONSOR_DETAILS, visitId);
	}

	public  List<BasicDynaBean> getPlanDetails(Connection con,String visitId) throws SQLException {
		return DataBaseUtil.queryToDynaList(con, PLAN_DETAILS_FIELDS+PLAN_DETAILS_TABLES+PLAN_DETAILS_WHERE, visitId);
	}

	public  List<BasicDynaBean> getVisitPlanSponsorsDetails(String visitId) throws SQLException {
		return DataBaseUtil.queryToDynaList(PLAN_DETAILS_FIELDS+PLAN_SPONSOR_DETAILS_FIELDS+PLAN_DETAILS_TABLES+PLAN_SPONSOR_DETAILS_JOINS+PLAN_DETAILS_WHERE, visitId);
	}

	public  List<BasicDynaBean> getSponsorDetails(String visitId) throws SQLException {
		return DataBaseUtil.queryToDynaList(SPONSOR_DETAILS, visitId);
	}

	public  List<BasicDynaBean> getPlanDetails(String visitId) throws SQLException {
		return DataBaseUtil.queryToDynaList(PLAN_DETAILS_FIELDS+PLAN_DETAILS_TABLES+PLAN_DETAILS_WHERE, visitId);
	}


	private static final String CHECK_DUP_MEMBERID = "SELECT member_id,ppd.mr_no,pip.patient_id " +
		" FROM patient_insurance_plans pip " +
		" LEFT JOIN patient_policy_details ppd USING(patient_policy_id) " +
		" WHERE ppd.member_id=? AND pip.insurance_co =? ";

	public boolean checkDupliMemberID(String memberid,String companyId,String mrno) throws SQLException {

		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		boolean status = false;
		try {
			String querySql = CHECK_DUP_MEMBERID;
			if (!mrno.isEmpty()) {
			    querySql += " and pip.mr_no not in(SELECT mr_no FROM patient_details WHERE COALESCE(CASE WHEN original_mr_no = '' THEN NULL ELSE original_mr_no END, mr_no) = ?) ";
			}
			ps = con.prepareStatement(querySql);
			ps.setString(1, memberid);
			ps.setString(2, companyId);
			if (!mrno.isEmpty()) {
			    ps.setString(3, mrno);
			}
			List l = DataBaseUtil.queryToDynaList(ps);
			if (l != null && !l.isEmpty()){ 
				BasicDynaBean firstItem = (BasicDynaBean)l.get(0);
				List<BasicDynaBean> adBeans = null;
				BasicDynaBean pdBean = null;
				BasicDynaBean mompdBean = null;
				BasicDynaBean adMomBean = null;
				
				if ( firstItem != null ){
					pdBean = new PatientDetailsDAO().findByKey("mr_no",firstItem.get("mr_no"));
					adBeans = new GenericDAO("admission").findAllByKey("mr_no",mrno);
					
				}
				
				for(BasicDynaBean adBean : adBeans){
					if ( adBean.get("parent_id") != null ){
						adMomBean = adBean;//visit admission bean when child birth was entered into db
						mompdBean = new PatientDetailsDAO().findByKey("mr_no",adMomBean.get("mr_no"));
					}
				}
				//in case of no admission duplicate
				//in case of admission check if baby or mother
				
				if ( (//duplicate mrno is if baby || of baby's mother then ok
						( pdBean.get("timeofbirth") != null && !pdBean.get("timeofbirth").toString().isEmpty()) || 
								( mompdBean != null && mompdBean.get("mr_no").toString().equals(firstItem.get("mr_no").toString()))  ) ){
					status = false;
				} else {
					status = true;
				}
			}else
				status = false;
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return status;
	}


	public boolean updatePlanDetails(Connection con, List<BasicDynaBean> allVisits, BasicDynaBean primaryInsBean,
			BasicDynaBean secondaryInsBean, int[] planIds, List<BasicDynaBean> patPrimaryInsuranceDetailsBeanList,
			List<BasicDynaBean> patSecondaryInsuranceDetailsBeanList)throws SQLException, IOException {

		PatientInsurancePlanDAO planDAO = new PatientInsurancePlanDAO();
		GenericDAO policyDAO = new GenericDAO("patient_policy_details");
		GenericDAO patInsPlansDAO = new GenericDAO("patient_insurance_plans");
		List<BasicDynaBean> noOfPatientPlanBeans = null;

		for (BasicDynaBean bean : allVisits) {
			noOfPatientPlanBeans = listAll(con, null, "patient_id", bean.get("patient_id"), "priority");

			if (noOfPatientPlanBeans.size() == 2) {
				BasicDynaBean existingPlan1 = noOfPatientPlanBeans.get(0);
				BasicDynaBean existingPlan2 = noOfPatientPlanBeans.get(1);
				boolean pripolicyForOtherVistsAswell = getOtherPatientPlanDetails(con,(Integer)existingPlan1.get("patient_policy_id"), (String)bean.get("patient_id")) != null;
				boolean secpolicyForOtherVistsAswell = getOtherPatientPlanDetails(con,(Integer)existingPlan2.get("patient_policy_id"), (String)bean.get("patient_id")) != null;

				if(null == primaryInsBean) {
					if(pripolicyForOtherVistsAswell){
						//policy is alive
					} else {
						//delete the policy
						policyDAO.delete(con, "patient_policy_id", existingPlan1.get("patient_policy_id"));
					}
					//delete the plan
					planDAO.delete(con, "patient_insurance_plans_id", existingPlan1.get("patient_insurance_plans_id"));
					patientInsurancePlanDetailsDAO.delete(con, "visit_id", existingPlan1.get("patient_id"), "plan_id", existingPlan1.get("plan_id"));
				} else {

					//delete policy details if there is no plan (ie only TPA)
					if (null == primaryInsBean.get("plan_id")) {
						policyDAO.delete(con, "patient_policy_id", primaryInsBean.get("patient_policy_id"));
						primaryInsBean.set("patient_policy_id", 0);
					}

					updatePlan(con, primaryInsBean, existingPlan1);
					if(null != patPrimaryInsuranceDetailsBeanList && patPrimaryInsuranceDetailsBeanList.size()>0)
						insertORUpdatePatientInsuranceDetails(con,patPrimaryInsuranceDetailsBeanList);
				}

				if(null == secondaryInsBean) {
					if(secpolicyForOtherVistsAswell){
						//policy is alive
					}else {
						//delete the policy
						policyDAO.delete(con, "patient_policy_id", existingPlan2.get("patient_policy_id"));
					}
					//delete visit plan
					planDAO.delete(con, "patient_insurance_plans_id", existingPlan2.get("patient_insurance_plans_id"));
					patientInsurancePlanDetailsDAO.delete(con, "visit_id", existingPlan2.get("patient_id"), "plan_id", existingPlan2.get("plan_id"));
				} else {

					//delete policy details if there is no plan (only TPA)
					if (null == secondaryInsBean.get("plan_id")) {
						policyDAO.delete(con, "patient_policy_id", secondaryInsBean.get("patient_policy_id"));
						secondaryInsBean.set("patient_policy_id", 0);
					}
					updatePlan(con, secondaryInsBean, existingPlan2);
					if(null != patSecondaryInsuranceDetailsBeanList && patSecondaryInsuranceDetailsBeanList.size()>0)
						insertORUpdatePatientInsuranceDetails(con,patSecondaryInsuranceDetailsBeanList);
				}


			} else if (noOfPatientPlanBeans.size() == 1) {
				BasicDynaBean existingPlan = noOfPatientPlanBeans.get(0);
				BasicDynaBean newPlan = planDAO.getBean();
				String result = isPrimaryOrSecondaryPlan((String)bean.get("patient_id"));
				boolean pripolicyForOtherVistsAswell = getOtherPatientPlanDetails(con,(Integer)existingPlan.get("patient_policy_id"), (String)bean.get("patient_id")) != null;
				if (result.equals("primary")) {
					if (primaryInsBean != null) {
						//delete policy details if there is no plan (only TPA)
						if (null == primaryInsBean.get("plan_id")) {
							policyDAO.delete(con, "patient_policy_id", primaryInsBean.get("patient_policy_id"));
							primaryInsBean.set("patient_policy_id", 0);
						}
						updatePlan(con, primaryInsBean, existingPlan);
						if(null != patPrimaryInsuranceDetailsBeanList && patPrimaryInsuranceDetailsBeanList.size()>0)
							insertORUpdatePatientInsuranceDetails(con,patPrimaryInsuranceDetailsBeanList);
					} else {
						if(pripolicyForOtherVistsAswell){
							//policy is alive
						}else {
							policyDAO.delete(con, "patient_policy_id", existingPlan.get("patient_policy_id"));
						}
						//delete the visit plan
						planDAO.delete(con, "patient_insurance_plans_id", existingPlan.get("patient_insurance_plans_id"));
						patientInsurancePlanDetailsDAO.delete(con, "visit_id", existingPlan.get("patient_id"), "plan_id", existingPlan.get("plan_id"));
					}

					if (secondaryInsBean != null) {
						newPlan.set("patient_id", bean.get("patient_id"));
						insertPlan(con, secondaryInsBean, newPlan);
						if(null!=patSecondaryInsuranceDetailsBeanList && patSecondaryInsuranceDetailsBeanList.size()>0)
						  patientInsurancePlanDetailsDAO.insertAll(con, patSecondaryInsuranceDetailsBeanList);
					}

				} else if (result.equals("secondary")) {

					if (secondaryInsBean != null) {

						if (null == secondaryInsBean.get("plan_id")) {
							policyDAO.delete(con, "patient_policy_id", secondaryInsBean.get("patient_policy_id"));
							secondaryInsBean.set("patient_policy_id", 0);
						}
						updatePlan(con, secondaryInsBean, existingPlan);
						if(null != patSecondaryInsuranceDetailsBeanList && patSecondaryInsuranceDetailsBeanList.size()>0)
							insertORUpdatePatientInsuranceDetails(con,patSecondaryInsuranceDetailsBeanList);

					} else {

						if(pripolicyForOtherVistsAswell){
							//policy is alive
						}else {
							policyDAO.delete(con, "patient_policy_id", existingPlan.get("patient_policy_id"));
						}
						//delete plan
						planDAO.delete(con, "patient_insurance_plans_id", existingPlan.get("patient_insurance_plans_id"));
						patientInsurancePlanDetailsDAO.delete(con, "visit_id", existingPlan.get("patient_id"), "plan_id", existingPlan.get("plan_id"));
					}

					if (primaryInsBean != null) {
						newPlan.set("patient_id", bean.get("patient_id"));
						insertPlan(con, primaryInsBean, newPlan);
						if(null != patPrimaryInsuranceDetailsBeanList && patPrimaryInsuranceDetailsBeanList.size()>0)
						  patientInsurancePlanDetailsDAO.insertAll(con, patPrimaryInsuranceDetailsBeanList);
					}
				}
			} else if (noOfPatientPlanBeans.size() == 0) {
				BasicDynaBean newPlan1 = planDAO.getBean();
				BasicDynaBean newPlan2 = planDAO.getBean();

				if (null != primaryInsBean) {
					newPlan1.set("patient_id", bean.get("patient_id"));
					insertPlan(con, primaryInsBean, newPlan1);

				}

				if (null != secondaryInsBean) {
					newPlan2.set("patient_id", bean.get("patient_id"));
					insertPlan(con, secondaryInsBean, newPlan2);
				}
//				patPlanDetailsDAO.insertAll(con, patPrimaryInsuranceDetailsBeanList);
				if(null != patPrimaryInsuranceDetailsBeanList && patPrimaryInsuranceDetailsBeanList.size()>0)
					insertORUpdatePatientInsuranceDetails(con, patPrimaryInsuranceDetailsBeanList);

				if(null!=patSecondaryInsuranceDetailsBeanList && patSecondaryInsuranceDetailsBeanList.size()>0)
					insertORUpdatePatientInsuranceDetails(con, patSecondaryInsuranceDetailsBeanList);

			}
		}
		if (null != primaryInsBean) {
			for(BasicDynaBean bean2 : allVisits){
				Map idenfiers = new HashMap();
				idenfiers.put("visit_id", bean2.get("patient_id"));
				idenfiers.put("plan_id", primaryInsBean.get("plan_id"));
				BasicDynaBean existingPolicyDetailsFollowUps = policyDAO.findByKey(con,idenfiers);
				if(null != existingPolicyDetailsFollowUps){
					Map keys1 = new HashMap();
					keys1.put("mr_no", existingPolicyDetailsFollowUps.get("mr_no"));
					keys1.put("plan_id", existingPolicyDetailsFollowUps.get("plan_id"));
					keys1.put("patient_id",existingPolicyDetailsFollowUps.get("visit_id"));
					Map fields = new HashMap();
					fields.put("patient_policy_id", existingPolicyDetailsFollowUps.get("patient_policy_id"));
					patInsPlansDAO.update(con, fields, keys1);
				}
			}
		}

		if (null != secondaryInsBean) {
			for(BasicDynaBean bean2 : allVisits){
				Map idenfiers = new HashMap();
				idenfiers.put("visit_id", bean2.get("patient_id"));
				idenfiers.put("plan_id", secondaryInsBean.get("plan_id"));
				BasicDynaBean existingPolicyDetailsFollowUps = policyDAO.findByKey(con,idenfiers);
				if(null != existingPolicyDetailsFollowUps){
					Map keys1 = new HashMap();
					keys1.put("mr_no", existingPolicyDetailsFollowUps.get("mr_no"));
					keys1.put("plan_id", existingPolicyDetailsFollowUps.get("plan_id"));
					keys1.put("patient_id",existingPolicyDetailsFollowUps.get("visit_id"));
					Map fields = new HashMap();
					fields.put("patient_policy_id", existingPolicyDetailsFollowUps.get("patient_policy_id"));
					patInsPlansDAO.update(con, fields, keys1);
				}
			}
		}

		return true;
	}

	private void insertORUpdatePatientInsuranceDetails(Connection con, List<BasicDynaBean> insuranceDetailsBeanList) throws SQLException, IOException {

		List<BasicDynaBean> planDetailsList = new ArrayList<BasicDynaBean>();
		Map filterMap = new HashMap();
		for(int i=0;i<insuranceDetailsBeanList.size();i++){
			BasicDynaBean bean= insuranceDetailsBeanList.get(i);
			filterMap.put("plan_id",bean.get("plan_id"));
			filterMap.put("visit_id", bean.get("visit_id"));

			planDetailsList = patientInsurancePlanDetailsDAO.listAll(null, filterMap,"plan_id");
			patientInsurancePlanDetailsDAO.delete(con, "visit_id", bean.get("visit_id"), "plan_id", bean.get("plan_id"));
		}
		patientInsurancePlanDetailsDAO.insertAll(con, insuranceDetailsBeanList);
	}

	public String isPrimaryOrSecondaryPlan(String patient_id)throws SQLException {

		BasicDynaBean visitBean = patientRegistrationDAO.findByKey("patient_id", patient_id);
		if (visitBean != null) {
			Integer patient_corporate_id = (Integer)visitBean.get("patient_corporate_id");
			Integer patient_national_sponsor_id = (Integer)visitBean.get("patient_national_sponsor_id");
			Integer secondary_corporate_id = (Integer)visitBean.get("patient_national_sponsor_id");
			Integer secondary_national_sponsor_id = (Integer)visitBean.get("secondary_patient_national_sponsor_id");

			if ((null != patient_corporate_id && !patient_corporate_id.equals("") && patient_corporate_id.intValue() > 0)
					|| (null != patient_national_sponsor_id && !patient_national_sponsor_id.equals("")
							&& patient_national_sponsor_id.intValue() > 0)) {

				return "secondary";

			} else if (((null != secondary_corporate_id && !secondary_corporate_id.equals("") && secondary_corporate_id.intValue() > 0)
							|| (null != secondary_national_sponsor_id && !secondary_national_sponsor_id.equals("")
									&& secondary_national_sponsor_id.intValue() > 0))
									|| ((null == secondary_corporate_id || secondary_corporate_id.equals("") || secondary_corporate_id.intValue() == 0)
											&& (null == secondary_national_sponsor_id || secondary_national_sponsor_id.equals("") || secondary_national_sponsor_id.intValue() == 0))) {

				return "primary";
			}
			return "none";
		}
		return "none";
	}

	private void copyBeanValues(BasicDynaBean from, BasicDynaBean to) {

		Set<String> columnSet = from.getMap().keySet();
		columnSet.remove("patient_insurance_policy_id");
		columnSet.remove("patient_id");
		columnSet.remove("patient_policy_id");
		Iterator<String> it = columnSet.iterator();
		String column = null;

		while(it.hasNext()) {
			column = it.next();
			to.set(column, from.get(column));

		}

	}

	private void insertPlan(Connection con, BasicDynaBean valueBean, BasicDynaBean templateBean)throws SQLException, IOException {
		PatientInsurancePlanDAO planDAO = new PatientInsurancePlanDAO();
		copyBeanValues(valueBean, templateBean);
		templateBean.set("patient_insurance_plans_id", planDAO.getNextSequence());
		setDefaultvaluesForOtherThanMainVisit(templateBean);
		planDAO.insert(con, templateBean);
	}

	private void updatePlan(Connection con, BasicDynaBean from, BasicDynaBean to)throws SQLException, IOException {
		PatientInsurancePlanDAO planDAO = new PatientInsurancePlanDAO();
		HashMap<String, Integer> keys = new HashMap<String, Integer>();
		copyBeanValues(from, to);
		setDefaultvaluesForOtherThanMainVisit(to);
		keys.put("patient_insurance_plans_id", (Integer)to.get("patient_insurance_plans_id"));
		planDAO.update(con, to.getMap(), keys);

	}

	private void setDefaultvaluesForOtherThanMainVisit(BasicDynaBean planBean)throws SQLException{

		BasicDynaBean visitBean = new VisitDetailsDAO().findByKey("patient_id", planBean.get("patient_id"));
		boolean isMainVisit = !(visitBean.get("op_type") != null && (visitBean.get("op_type").equals("F") || visitBean.get("op_type").equals("D")));
		if (!isMainVisit) {
			//planBean.set("insurance_id", 0);
			planBean.set("insurance_approval", null);
			planBean.set("insurance_approval", null);
			planBean.set("use_drg", "N");
			planBean.set("use_perdiem", "N");
		}
	}

	public int[] getPlanIds(Connection con,String patientId) throws SQLException{

		List<BasicDynaBean> planList = getPlanDetails(con,patientId);
		int[] planIds = null != planList && planList.size() > 0 ? new int[planList.size()] : null;
		int planIdx = 0;
		for(BasicDynaBean bean : planList){
			planIds[planIdx++] = (Integer)bean.get("plan_id");
		}
		return planIds;
	}

	public int[] getPlanIds(String patientId) throws SQLException{
		Connection con = null;
		int[] planIds = null;
		try{
			con = DataBaseUtil.getReadOnlyConnection();
			planIds = getPlanIds(con,patientId);
		}finally{
			DataBaseUtil.closeConnections(con, null);
		}
		return planIds;

	}

	public String[] getSponsortypes(String patientID)throws SQLException {

		List<BasicDynaBean> noOfPatientPlanBeans = null;
		String[] sponsorTypes = new String[2];

		noOfPatientPlanBeans = listAll(null, "patient_id", patientID, "priority");
		BasicDynaBean visitBean = patientRegistrationDAO.findByKey("patient_id", patientID);
		Integer patient_corporate_id = null;
		Integer patient_national_sponsor_id = null;
		Integer secondary_corporate_id = null;
		Integer secondary_national_sponsor_id = null;

		if (noOfPatientPlanBeans.size() == 2) {

			sponsorTypes[0] = "I";
			sponsorTypes[1] = "I";
		} else if (noOfPatientPlanBeans.size() == 1) {

			if (visitBean != null) {
				patient_corporate_id = (Integer)visitBean.get("patient_corporate_id");
				patient_national_sponsor_id = (Integer)visitBean.get("patient_national_sponsor_id");
				secondary_corporate_id = (Integer)visitBean.get("patient_national_sponsor_id");
				secondary_national_sponsor_id = (Integer)visitBean.get("secondary_patient_national_sponsor_id");

				if (patient_corporate_id != null && !patient_corporate_id.equals("")) {
					sponsorTypes[0] = "C";
					sponsorTypes[1] = "I";
				} else if (patient_national_sponsor_id != null && !patient_national_sponsor_id.equals("")) {
					sponsorTypes[0] = "N";
					sponsorTypes[1] = "I";
				} else if (secondary_corporate_id != null && !secondary_corporate_id.equals("")) {
					sponsorTypes[0] = "I";
					sponsorTypes[1] = "C";
				} else if (secondary_national_sponsor_id != null && !secondary_national_sponsor_id.equals("")) {
					sponsorTypes[0] = "I";
					sponsorTypes[1] = "N";
				} else {
					sponsorTypes[0] = "I";
					sponsorTypes[1] = null;
				}
			}
		} else if (noOfPatientPlanBeans.size() == 0) {

			if (visitBean != null) {
				patient_corporate_id = (Integer)visitBean.get("patient_corporate_id");
				patient_national_sponsor_id = (Integer)visitBean.get("patient_national_sponsor_id");
				secondary_corporate_id = (Integer)visitBean.get("patient_national_sponsor_id");
				secondary_national_sponsor_id = (Integer)visitBean.get("secondary_patient_national_sponsor_id");

				if (patient_corporate_id != null && !patient_corporate_id.equals("")) {
					sponsorTypes[0] = "C";
					sponsorTypes[1] = null;
				} else if (patient_national_sponsor_id != null && !patient_national_sponsor_id.equals("")) {
					sponsorTypes[0] = "N";
					sponsorTypes[1] = null;
				} else if (secondary_corporate_id != null && !secondary_corporate_id.equals("")) {
					sponsorTypes[0] = null;
					sponsorTypes[1] = "C";
				} else if (secondary_national_sponsor_id != null && !secondary_national_sponsor_id.equals("")) {
					sponsorTypes[0] = null;
					sponsorTypes[1] = "N";
				} else {
					sponsorTypes[0] = null;
					sponsorTypes[1] = null;
				}
			}

		}

		return sponsorTypes;
	}

	private final static String PATIENT_PLAN_DETAILS =
		" SELECT * from patient_insurance_plans pip" +
		" JOIN patient_policy_details ppd USING(patient_policy_id) WHERE ppd.patient_policy_id = ? " ;

	public static BasicDynaBean patientPlanDetails(int patientPolicyId) throws SQLException{
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(PATIENT_PLAN_DETAILS);
			ps.setInt(1, patientPolicyId);
			return DataBaseUtil.queryToDynaBean(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public BasicDynaBean getOtherPatientPlanDetails(Connection con,int patientPolicyId,String visitId) throws SQLException{
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(PATIENT_PLAN_DETAILS+ " AND patient_id != ? ");
			ps.setInt(1, patientPolicyId);
			ps.setString(2, visitId);
			return DataBaseUtil.queryToDynaBean(ps);
		} finally {
			DataBaseUtil.closeConnections(null, ps);
		}
	}

	private static final String GET_SPONSOR_ID = "SELECT sponsor_id FROM patient_insurance_plans pip " +
		" WHERE pip.patient_id=? and pip.plan_id=? " ;

	public String getSponsorId(Connection con,String visitId, int planId)throws SQLException {
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(GET_SPONSOR_ID);
			ps.setString(1, visitId);
			ps.setInt(2, planId);
			return DataBaseUtil.getStringValueFromDb(ps);
		}finally{
			if(ps!=null) ps.close();
		}
	}

	//Gives u primary insurance plan main details.
	public BasicDynaBean getVisitPrimaryPlan(Connection con,String patientId)throws SQLException{

		int[] visitplans =  new PatientInsurancePlanDAO().getPlanIds(con,patientId);
		return (visitplans != null ? insurancePlanMainDAO.findByKey("plan_id",visitplans[0]) : null);

	}

	//Gives u primary insurance plan main details.
		public BasicDynaBean getVisitPrimaryPlan(String patientId)throws SQLException{

			int[] visitplans =  new PatientInsurancePlanDAO().getPlanIds(patientId);
			return (visitplans != null ? insurancePlanMainDAO.findByKey("plan_id",visitplans[0]) : null);

		}
	
	public BasicDynaBean getPrimarySponsorDetails(String visitId) throws SQLException {
		List<BasicDynaBean> sponsorDetails = getSponsorDetails(visitId);
		if(sponsorDetails != null && sponsorDetails.size() > 0) {
			return sponsorDetails.get(0);
		}
		return null;
	}
		
	public BasicDynaBean getPrimarySponsorDetails(Connection con, String visitId) throws SQLException {
		List<BasicDynaBean> sponsorDetails = getSponsorDetails(con, visitId);
		if(sponsorDetails != null && sponsorDetails.size() > 0) {
			return sponsorDetails.get(0);
		}
		return null;
	}
	
	private static final String UPDATE_PRI_CASE_RATE_LIMITS = " UPDATE patient_insurance_plan_details pipd "+ 
	    " SET per_treatment_limit = crd.amount "+
	    " FROM patient_registration pr "+
	    " JOIN patient_insurance_plans pip ON(pr.patient_id = pip.patient_id AND pip.priority=1) "+
	    " JOIN visit_case_rate_detail crd ON(pr.primary_case_rate_id = crd.case_rate_id) "+
	    " WHERE pr.patient_id = pipd.visit_id AND pipd.plan_id = pip.plan_id AND pipd.insurance_category_id = crd.insurance_category_id "+
	    " AND pr.patient_id = ?  AND crd.case_rate_id = ? ";
	
	private static final String UPDATE_SEC_CASE_RATE_LIMITS = " UPDATE patient_insurance_plan_details pipd "+ 
      " SET per_treatment_limit = (COALESCE(per_treatment_limit,0) + crd.amount) "+
      " FROM patient_registration pr "+
      " JOIN patient_insurance_plans pip ON(pr.patient_id = pip.patient_id AND pip.priority=1) "+
      " JOIN visit_case_rate_detail crd ON(pr.secondary_case_rate_id = crd.case_rate_id) "+
      " WHERE pr.patient_id = pipd.visit_id AND pipd.plan_id = pip.plan_id AND pipd.insurance_category_id = crd.insurance_category_id "+
      " AND pr.patient_id = ?  AND crd.case_rate_id = ? "; 

  public Boolean updateCaseRateLimits(Integer caseRateId, String visitId, Integer caseRateNo) {
    String query = null;
    if(caseRateNo == 1){
      query = UPDATE_PRI_CASE_RATE_LIMITS;
    } else if(caseRateNo == 2) {
      query = UPDATE_SEC_CASE_RATE_LIMITS;
    }  
    return DatabaseHelper.update(query, new Object[]{visitId, caseRateId}) >= 0;
  }

  private static final String REMOVE_CASE_RATE_LIMITS = " UPDATE patient_insurance_plan_details pipd "+ 
      " SET per_treatment_limit = 0 "+
      " FROM patient_registration pr "+
      " JOIN patient_insurance_plans pip ON(pr.patient_id = pip.patient_id AND pip.priority=1) "+
      " JOIN case_rate_main crm ON(pip.plan_id = crm.plan_id AND crm.case_rate_id = ?) "+
      " JOIN case_rate_detail crd ON(crm.case_rate_id = crd.case_rate_id) "+
      " WHERE pr.patient_id = pipd.visit_id AND pipd.plan_id = pip.plan_id AND pipd.insurance_category_id = crd.insurance_category_id "+
      " AND pr.patient_id = ? ";
  
  public void removeCaseRateLimits(Integer caseRateId, String visitId) {
    DatabaseHelper.update(REMOVE_CASE_RATE_LIMITS, new Object[]{caseRateId, visitId});
  }
}
