/**
 *
 */
package com.insta.hms.billing;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.QueryBuilder;
import com.insta.hms.common.SearchQueryBuilder;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author lakshmi.p
 *
 */
public class ClaimDAO extends GenericDAO {

	static Logger logger = LoggerFactory.getLogger(ClaimDAO.class);
	private static final GenericDAO billDAO = new GenericDAO("bill");
    private static final GenericDAO patientRegistrationDAO = new GenericDAO("patient_registration");
	
	private Connection con = null;

	public ClaimDAO() {
		super("insurance_claim");
	}

	public ClaimDAO(Connection con) {
		super("insurance_claim");
		this.con = con;
	}

	public String getGeneratedClaimId() throws SQLException {
		return DataBaseUtil.getNextPatternId("insurance_claim");
	}

	public static String fillClaimIDSearch(String claimID) {
		if((claimID == null) || (claimID.equals("")))
			return null;
		int claimIDDigits = DataBaseUtil.getStringValueFromDb("SELECT num_pattern FROM hosp_id_patterns " +
				" WHERE pattern_id ='insurance_claim'").length();

		if(claimID.length() < claimIDDigits){
			String submitID = DataBaseUtil.getStringValueFromDb("SELECT std_prefix||''||trim((TO_CHAR("
				+claimID+",num_pattern))) FROM hosp_id_patterns WHERE pattern_id ='insurance_claim'");
			if(submitID != null && !submitID.equals(""))
				return submitID;
		}
		return claimID;
	}

	public Map getNewOrEpisodeClaimId(Connection con, String visitId, int accountGroup) throws SQLException, IOException {
		Map map = new HashMap();
		String claimId = null;
		boolean success = true;

		boolean modAdvIns =  (Boolean)RequestContext.getSession().getAttribute("mod_adv_ins");
		accountGroup = (accountGroup == 0) ? Bill.BILL_DEFAULT_ACCOUNT_GROUP : accountGroup;

		BasicDynaBean visit = new VisitDetailsDAO().findVisitById(con, visitId);
		if (visit != null) {
			String mainVisitId = visit.get("main_visit_id") != null ? (String)visit.get("main_visit_id") : null;
			String patientId   = visit.get("patient_id") != null ? (String)visit.get("patient_id") : null;
			String opType	   = visit.get("op_type") != null ? (String)visit.get("op_type") : "M";
			String tpaId       = visit.get("primary_sponsor_id") != null ? (String)visit.get("primary_sponsor_id") : null;

			if (tpaId == null) {
				map.put("error", "Patient visit is not connected with TPA. Visit ID: " + visitId);
				return map;
			}

			if (mainVisitId == null) {
				map.put("error", "Patient main visit Id is empty.");
				return map;
			}

			if (modAdvIns && mainVisitId != null && tpaId != null && !tpaId.equals("")) {
				opType = opType.equals("R") ? "M" : opType; // If Revisit then claim op type is Main.

				int centerId = VisitDetailsDAO.getCenterId(con, visitId);
				claimId = getGeneratedClaimIdBasedonCenterId(centerId, accountGroup);
				BasicDynaBean claimbean = getBean();
				claimbean.set("claim_id", claimId);
				claimbean.set("main_visit_id", mainVisitId);
				claimbean.set("patient_id", patientId);
				claimbean.set("op_type", opType);
				claimbean.set("status", "O");
				claimbean.set("account_group", accountGroup);
				success = insert(con, claimbean);
			}

			if (success)
				map.put("CLAIMID", claimId);
			else
				map.put("error", "Error while creating claim id...");
		}
		return map;
	}

	private static final String GET_OPEN_CLAIM_DETAILS = " SELECT ic.claim_id, ic.status as claim_status, ic.op_type " +
			" FROM insurance_claim ic WHERE ic.status = 'O' " +
			" AND ic.main_visit_id=? AND ic.patient_id=? AND ic.op_type=? AND account_group=? AND plan_id = ?" +
			" ORDER BY claim_id DESC LIMIT 1 ";

	public BasicDynaBean getEpisodeOpenClaim(Connection con, String mainVisitId, String patientId,
			String opType, int accountGroup, int planId) throws SQLException {
		PreparedStatement ps = null;
		try{
		  opType = opType.equals("R") ? "M" : opType; // If Revisit then claim op type is Main.
	    ps = con.prepareStatement(GET_OPEN_CLAIM_DETAILS); // Main/Follow up(with/without cons.) claim)
	    ps.setString(1, mainVisitId);
	    ps.setString(2, patientId);
	    ps.setString(3, opType);
	    ps.setInt(4, accountGroup);
	    ps.setInt(5, planId);
	    return DataBaseUtil.queryToDynaBean(ps);
		}finally{
		  if(null != ps){
		    ps.close();
		  }
		}
		
	}

	private static final String GET_CLAIM_DETAILS=
		        " SELECT ic.claim_id, ic.last_submission_batch_id, ppd.member_id, ppd.policy_number, " +
		        " ic.payers_reference_no, ic.main_visit_id, ic.status,ic.account_group, " +
		        " foo.resubmission_count, closure_type, action_remarks, denial_remarks, attachment_content_type,resubmission_type,comments, " +
		        " COALESCE (hta.tpa_code,'/'||tm.tpa_name) AS payer_id," +
		        " CASE WHEN pd.government_identifier IS NULL OR pd.government_identifier = '' THEN " +
		        " COALESCE(gim.remarks,'') || '(' || COALESCE(gim.identifier_type,'---') || ')' ELSE  pd.government_identifier END AS emirates_id_number, " +
		        " SUM(total_amount) AS gross, " +
		        " SUM(total_amount-total_claim) AS patient_share, SUM(total_claim) AS net, " +
		        " SUM(insurance_deduction) AS deduction, " +
		        " pr.use_drg, pr.use_perdiem, pr.encounter_type, " +
		        " pr.patient_id, pr.mr_no AS mr_no," +
		        " to_char((pr.reg_date||' '||pr.reg_time) :: timestamp without time zone, 'dd/MM/yyyy hh24:mi') as start_date," +
		        " to_char((pr.discharge_date||' '||pr.discharge_time) :: timestamp without time zone, 'dd/MM/yyyy hh24:mi') as end_date," +
		        " pr.encounter_start_type, " +
		        " pr.encounter_end_type, " +
		        " to_char(current_date::date, 'dd-MM-yyyy') AS todays_date, " +
		        " doctor_license_number,doc.doctor_name, tm.tpa_id," +
		        " sal.salutation as salutation_name, " +
		        " pd.patient_name || ' ' || coalesce(pd.middle_name, '') || ' ' || coalesce(pd.last_name, '') AS patient_full_name," +
		        " get_patient_age(dateofbirth, expected_dob) as age," +
		        " get_patient_age_in(dateofbirth, expected_dob) as age_in,pd.patient_phone,pd.patient_gender, tm.tpa_name, icm.insurance_co_name  " +
		                " FROM bill b " +
		                " JOIN bill_claim bcl ON (b.bill_no = bcl.bill_no) " +
		                " JOIN insurance_claim ic ON (ic.claim_id = bcl.claim_id)" +
		                " JOIN patient_registration pr ON (pr.patient_id = ic.patient_id) " +
		                " JOIN hospital_center_master hcm ON(pr.center_id = hcm.center_id)" +
		                " JOIN tpa_master tm ON (tm.tpa_id = pr.primary_sponsor_id) " +
		                " LEFT JOIN (SELECT count(*) as resubmission_count, ic.claim_id FROM claim_submissions cs " +
		                "                       JOIN insurance_submission_batch isb ON (isb.submission_batch_id = cs.submission_batch_id) " +
		                "                       JOIN insurance_claim ic ON (ic.claim_id = cs.claim_id) " +
		                "                       WHERE isb.is_resubmission = 'Y' GROUP BY ic.claim_id) as foo " +
		                "                               ON (foo.claim_id = ic.claim_id) " +
		                " LEFT JOIN ha_tpa_code hta ON(hta.tpa_id=tm.tpa_id AND hta.health_authority = hcm.health_authority)" +
		                " LEFT JOIN insurance_company_master icm ON (icm.insurance_co_id = pr.primary_insurance_co) " +
		                " LEFT JOIN doctors doc ON (doc.doctor_id = pr.doctor) " +
		                " LEFT JOIN patient_details pd ON (pd.mr_no = pr.mr_no)" +
		                " LEFT JOIN salutation_master sal ON (sal.salutation_id = pd.salutation)" +
		                " LEFT JOIN govt_identifier_master gim ON (gim.identifier_id = pd.identifier_id) " +
		                " LEFT JOIN patient_policy_details ppd ON (ppd.patient_policy_id = pr.patient_policy_id) "+
		        " WHERE ic.claim_id = ? "+
		        " GROUP BY ic.claim_id,ic.last_submission_batch_id, ic.payers_reference_no, ic.main_visit_id, ic.status,ic.account_group," +
		        " foo.resubmission_count, " +
		        " ic.closure_type, action_remarks, denial_remarks, attachment_content_type,resubmission_type,comments, ppd.member_id," +
		        " ppd.policy_number,pr.use_drg, pr.use_perdiem, hta.tpa_code, tm.tpa_name, pr.encounter_type, pr.mr_no,pr.patient_id," +
		        " pr.reg_date,pr.reg_time,pr.discharge_date,pr.discharge_time, pr.encounter_start_type, pr.encounter_end_type," +
		        " pd.government_identifier,gim.identifier_type,gim.remarks, doctor_license_number,doc.doctor_name,tm.tpa_id," +
		        " sal.salutation,pd.patient_name,pd.middle_name,pd.last_name," +
		        " dateofbirth, expected_dob, pd.patient_phone,pd.patient_gender,tm.tpa_name, icm.insurance_co_name ";
        
	private static final String GET_CLAIM_DETAILS_NEW =
		" SELECT ic.claim_id, ic.last_submission_batch_id, ppd.member_id, ppd.policy_number, " +
		" ic.payers_reference_no, ic.main_visit_id, ic.status, cs.status as claim_batch_status, ic.account_group, " +
		" COALESCE(foo.resubmission_count,0) as resubmission_count, ic.closure_type,ic.rejection_reason_category_id, action_remarks, ic.denial_remarks, attachment_content_type,resubmission_type,comments, " +
		" COALESCE (hta.tpa_code,'/'||tm.tpa_name) AS payer_id," +
		" CASE WHEN pd.government_identifier IS NULL OR pd.government_identifier = '' THEN " +
		" COALESCE(gim.remarks,'') || '(' || COALESCE(gim.identifier_type,'---') || ')' ELSE  pd.government_identifier END AS emirates_id_number, " +
		" SUM(bc.amount) AS gross, " +
		" SUM(bc.amount-bc.insurance_claim_amount) AS patient_share, SUM(bcl.insurance_claim_amt) AS net, " +
		" SUM(bc.tax_amt) as total_tax, "+
		" SUM(bcl.tax_amt) as total_claim_tax, "+
		" SUM(insurance_deduction) AS deduction, " +
		" pr.use_drg, pr.use_perdiem, pr.encounter_type, " +
		" pr.patient_id, pr.mr_no AS mr_no," +
		" to_char((pr.reg_date||' '||pr.reg_time) :: timestamp without time zone, 'dd/MM/yyyy hh24:mi') as start_date," +
		" to_char((pr.discharge_date||' '||pr.discharge_time) :: timestamp without time zone, 'dd/MM/yyyy hh24:mi') as end_date," +
		" pr.encounter_start_type, " +
		" pr.encounter_end_type, " +
		" to_char(current_date::date, 'dd-MM-yyyy') AS todays_date, " +
		" doctor_license_number,doc.doctor_name, tm.tpa_id," +
		" sal.salutation as salutation_name, " +
		" pd.patient_name || ' ' || coalesce(pd.middle_name, '') || ' ' || coalesce(pd.last_name, '') AS patient_full_name," +
		" get_patient_age(dateofbirth, expected_dob) as age," +
		" get_patient_age_in(dateofbirth, expected_dob) as age_in,pd.patient_phone,pd.patient_gender, tm.tpa_name, tm.max_resubmission_count, icm.insurance_co_name, pip.priority,  " +
		" CASE WHEN (ic.status = 'O' AND cs.status IS NULL) THEN 'Open' " +
		" 	   WHEN (ic.status = 'B' AND cs.status = 'O') THEN 'Batched' " +
		" 	   WHEN (ic.status = 'B' AND cs.status = 'S') THEN 'Sent' " +
		" 	   WHEN (ic.status = 'C' AND cs.status = 'D') THEN 'Closed' " +
		"      WHEN (ic.status = 'B' AND cs.status = 'D') THEN 'Denied' " +
		"      WHEN (ic.status = 'M') THEN 'ForResub.' " +
		"      WHEN (ic.status = 'C' AND cs.status = 'R') THEN 'Closed' ELSE NULL END AS claim_status " +
			" FROM bill b " +
			" JOIN bill_charge bc ON (b.bill_no = bc.bill_no)" +
			" JOIN bill_charge_claim bcl ON (bc.charge_id = bcl.charge_id) " +
			" JOIN insurance_claim ic ON (ic.claim_id = bcl.claim_id)" +
			" JOIN patient_registration pr ON (pr.patient_id = ic.patient_id) " +
			" JOIN patient_insurance_plans pip ON (pr.patient_id = pip.patient_id AND pip.plan_id = ic.plan_id) " +
			" JOIN hospital_center_master hcm ON(pr.center_id = hcm.center_id)" +
			" JOIN tpa_master tm ON (tm.tpa_id = pr.primary_sponsor_id) " +
			" LEFT JOIN claim_submissions cs ON (cs.claim_id = ic.claim_id AND ic.last_submission_batch_id = cs.submission_batch_id) " +
			" LEFT JOIN (SELECT COALESCE(count(*),0) as resubmission_count, ic.claim_id FROM claim_submissions cs " +
			"                       JOIN insurance_submission_batch isb ON (isb.submission_batch_id = cs.submission_batch_id) " +
			"                       JOIN insurance_claim ic ON (ic.claim_id = cs.claim_id) " +
			"                       WHERE isb.is_resubmission = 'Y' GROUP BY ic.claim_id) as foo " +
			"                               ON (foo.claim_id = ic.claim_id) " +
			" LEFT JOIN ha_tpa_code hta ON(hta.tpa_id=tm.tpa_id AND hta.health_authority = hcm.health_authority)" +
			" LEFT JOIN insurance_company_master icm ON (icm.insurance_co_id = pr.primary_insurance_co) " +
			" LEFT JOIN doctors doc ON (doc.doctor_id = pr.doctor) " +
			" LEFT JOIN patient_details pd ON (pd.mr_no = pr.mr_no)" +
			" LEFT JOIN salutation_master sal ON (sal.salutation_id = pd.salutation)" +
			" LEFT JOIN govt_identifier_master gim ON (gim.identifier_id = pd.identifier_id) " +
			" LEFT JOIN patient_policy_details ppd ON (ppd.patient_policy_id = pr.patient_policy_id) "+
		" WHERE ic.claim_id = ? AND b.total_amount >= 0 "+
		" AND (patient_confidentiality_check(pd.patient_group,pd.mr_no))" +
		" AND (bc.submission_batch_type IS NULL OR (bc.submission_batch_type != 'P' OR bc.charge_head != 'PKGPKG')) "+
		" GROUP BY cs.status, ic.claim_id,ic.last_submission_batch_id, ic.payers_reference_no, ic.main_visit_id, ic.status,cs.status, ic.account_group," +
		" ic.closure_type, action_remarks, ic.denial_remarks, attachment_content_type,resubmission_type,comments, ppd.member_id," +
		" ppd.policy_number,pr.use_drg, pr.use_perdiem, hta.tpa_code, tm.tpa_name, tm.max_resubmission_count, pr.encounter_type, pr.mr_no,pr.patient_id," +
		" pr.reg_date,pr.reg_time,pr.discharge_date,pr.discharge_time, pr.encounter_start_type, pr.encounter_end_type," +
		" pd.government_identifier,gim.identifier_type,gim.remarks, foo.resubmission_count, doctor_license_number,doc.doctor_name,tm.tpa_id," +
		" sal.salutation,pd.patient_name,pd.middle_name,pd.last_name," +
		" dateofbirth, expected_dob, pd.patient_phone,pd.patient_gender,tm.tpa_name, icm.insurance_co_name, pip.priority ";

	public BasicDynaBean findClaimById(String claimId) throws SQLException {
		return DataBaseUtil.queryToDynaBean(GET_CLAIM_DETAILS, claimId);
	}

	public BasicDynaBean findClaimById(Connection con, String claimId) throws SQLException {
		PreparedStatement ps = null;
		try{
		  ps = con.prepareStatement(GET_CLAIM_DETAILS);
	    ps.setString(1, claimId);
	    return DataBaseUtil.queryToDynaBean(ps);
		}finally{
		  if(null != ps){
		    ps.close();
		  }
		}
		
	}

	public BasicDynaBean findClaimDetailsById(String claimId) throws SQLException {
		return DataBaseUtil.queryToDynaBean(GET_CLAIM_DETAILS_NEW, claimId);
	}

	private static final String CLAIM_FIELDS = " SELECT *  ";

	private static final String CLAIM_COUNT = " SELECT count(claim_id) ";

	private static final String FROM = " FROM ( " ;

	private String claimTables =
			"SELECT ic.claim_id, " + 
			"          pr.reg_date+pr.reg_time AS reg_date_time, " + 
			"          pr.discharge_date+pr.discharge_time AS discharge_date_time, " + 
			"          max(b.finalized_date) AS last_bill_finalized_date, " + 
			"          min(b.open_date) AS first_bill_open_date, " + 
			"          bool_and(b.status = 'F') AS all_finalized, " + 
			"          bool_and(b.status = 'A') AS all_opened " + 
			"   FROM bill b " + 
			" 	JOIN bill_claim bcl ON (b.bill_no = bcl.bill_no) " +	
			"   JOIN insurance_claim ic ON (ic.patient_id = b.visit_id"
			+ "								AND ic.claim_id = bcl.claim_id) " + 
			"   JOIN patient_insurance_plans pip ON (b.visit_id = pip.patient_id " + 
			"                                        AND ic.plan_id = pip.plan_id " +
			"										#tpaId ) " + 
			"   JOIN patient_registration pr ON (pip.patient_id = pr.patient_id) " + 
			"   LEFT JOIN store_sales_main ssm ON (ssm.bill_no = b.bill_no) " + 
			"   WHERE b.status IN ('A', 'F') "
			+ "		AND b.is_tpa IS TRUE # " +
			"	GROUP BY ic.claim_id, " + 
			"            pr.reg_date, " + 
			"            pr.reg_time, " + 
			"            pr.discharge_date, " + 
			"            pr.discharge_time ";

	private static final String ALIAS = " ) AS claims WHERE 1=1 ";

	@SuppressWarnings("unchecked")
	public PagedList searchClaimsForSubmission(Map filter, String isResubmission) throws SQLException, ParseException {
		Connection readOnlyCon = null;
		try {
		  readOnlyCon = DataBaseUtil.getReadOnlyConnection();
			String[] billStatus = (String[]) filter.get("bill_status");
			String[] insuranceCoId = (String[]) filter.get("insurance_co_id");
			String[] tpaId = (String[]) filter.get("tpa_id");
			String[] visitType = (String[]) filter.get("visit_type");
			String[] accountGroup = (String[]) filter.get("account_group");
			String[] categoryId = (String[]) filter.get("category_id");
			String[] planId = (String[]) filter.get("plan_id");
			String[] centerId = (String[]) filter.get("center_id");
			String[] dischargeDate = (String[]) filter.get("discharge_date");
			String[] codificationStatus = (String[]) filter.get("codification_status");

			boolean onlyFinalizedBills = false;
			boolean onlyOpenedBills = false;
			String billsFilter = null;

			boolean append = true;
			StringBuilder query = new StringBuilder();

			ArrayList<Integer> types = new ArrayList<>();
			ArrayList<String> values = new ArrayList<>();

			if (isResubmission != null && isResubmission.equals("Y")) {
				query.append(" AND ic.status = 'M' ");
			} else {
				query.append(" AND ic.status = 'O' ");
			}

			if (insuranceCoId != null && insuranceCoId.length > 0 && !insuranceCoId[0].equals("")) {
				append = QueryBuilder.addWhereFieldOpValue(append, query, "pip.insurance_co", "=", insuranceCoId[0]);
				values.add(insuranceCoId[0]);
			}
			
			if (tpaId != null && tpaId.length > 0 && !tpaId[0].equals("")) {
			  claimTables = claimTables.replace("#tpaId", " AND pip.sponsor_id = '" + tpaId[0].trim() + "'");
			} else {
			  claimTables = claimTables.replace("#tpaId", " ");
			}
			if (categoryId != null && categoryId.length > 0 && !categoryId[0].equals("")) {
				List<String> planTypeList = Arrays.asList(categoryId);
				append = QueryBuilder.addWhereFieldOpValue(append, query, "pip.plan_type_id::text", "IN", planTypeList);
				values.addAll(planTypeList);
			}
			if (planId != null && planId.length > 0 && !planId[0].equals("")) {
				append = QueryBuilder.addWhereFieldOpValue(append, query, "ic.plan_id::text", "=", planId[0]);
				values.add(planId[0]);
			}
			if (visitType != null && visitType.length > 0 && !visitType[0].equals("")) {
				List<String> visitTypeList = Arrays.asList(visitType);
				append = QueryBuilder.addWhereFieldOpValue(append, query, "pr.visit_type", "IN", visitTypeList);
				values.addAll(visitTypeList);
			}
			if (accountGroup != null) {
				append = QueryBuilder.addWhereFieldOpValue(append, query, "ic.account_group::text", "=",
						accountGroup[0]);
				values.add(accountGroup[0]);
			}
			if (centerId != null) {
				append = QueryBuilder.addWhereFieldOpValue(append, query, "pr.center_id::text", "=", centerId[0]);
				values.add(centerId[0]);
			}
			if (dischargeDate != null && dischargeDate.length > 0 && !dischargeDate[0].equals("")) {
				append = QueryBuilder.addWhereFieldOpValue(append, query, "pr.discharge_flag", "=", "D");
				values.add("D");
			}
			if (codificationStatus != null && codificationStatus.length > 0 && !codificationStatus[0].equals("")) {
				List<String> codificationStatusList = Arrays.asList(codificationStatus);
				append = QueryBuilder.addWhereFieldOpValue(append, query, "pr.codification_status", "IN", codificationStatusList);
				values.addAll(codificationStatusList);
			}

			claimTables = claimTables.replace("#", query.toString());

			if (billStatus != null && billStatus.length == 1) {
				if (billStatus[0].equals("F")) {
					onlyFinalizedBills = true;
				}
				if (billStatus[0].equals("A")) {
					onlyOpenedBills = true;
				}
			}

			if (onlyFinalizedBills) {
				billsFilter = " all_finalized = true ";

			} else if (onlyOpenedBills) {
				billsFilter = " all_opened = true ";
			}

			for (int i = 0; i < values.size(); i++) {
				types.add(QueryBuilder.STRING);
			}
			SearchQueryBuilder qb = new SearchQueryBuilder(readOnlyCon, CLAIM_FIELDS, null, FROM + claimTables + ALIAS,
					"", null, null, false, 0, 0);
			qb.appendExpression("", types, values);

			filter.remove("bill_status");
			filter.remove("insurance_co_id");
			filter.remove("tpa_id");
			filter.remove("category_id");
			filter.remove("plan_id");
			filter.remove("account_group");
			filter.remove("center_id");
			filter.remove("visit_type");
			filter.remove("is_external_pbm");
			filter.remove("codification_status");
			qb.addFilterFromParamMap(filter);
			if (billsFilter != null)
				qb.appendToQuery(billsFilter);
			qb.build();
			return qb.getMappedPagedList();
		} finally {
			DataBaseUtil.closeConnections(readOnlyCon, null);
		}
	}

	/*
	 * Claim Submission for the first time.
	 */

	private static final String UPDATE_CLAIM_SUBMISSION = " UPDATE insurance_claim SET last_submission_batch_id=?, status = 'B' " +
			" WHERE status = 'O' ";

	public boolean updateClaimSubmission(Connection con, List claims, String submissionBatchID) throws SQLException {
		PreparedStatement ps = null;
		boolean success = false;
		try{
		  StringBuilder query = new StringBuilder(UPDATE_CLAIM_SUBMISSION);
	    DataBaseUtil.addWhereFieldInList(query, "claim_id", claims);
	    ps = con.prepareStatement(query.toString());

	    ps.setString(1, submissionBatchID);
	    int i = 2;
	    for(int j=0;j<claims.size();j++) {
	      ps.setString(i++, claims.get(j).toString());
	    }
	    success = ps.executeUpdate() != 0;
		}finally{
		  if (ps != null){
		    ps.close();
		  }
		}
		return success;
	}
	
	/*
	 * Add to Claim ReSubmission 
	 */

	private static final String UPDATE_CLAIM_RESUBMISSION = " UPDATE insurance_claim SET last_submission_batch_id=?, status = 'B' " +
			" WHERE status = 'M' ";

	public boolean updateClaimResubmission(Connection con, List claims, String submissionBatchID) throws SQLException {
		PreparedStatement ps = null;
		boolean success = false;
		try{
		  StringBuilder query = new StringBuilder(UPDATE_CLAIM_RESUBMISSION);
	    DataBaseUtil.addWhereFieldInList(query, "claim_id", claims);
	    ps = con.prepareStatement(query.toString());

	    ps.setString(1, submissionBatchID);
	    int i = 2;
	    for(int j=0;j<claims.size();j++) {
	      ps.setString(i++, claims.get(j).toString());
	    }
	    success = ps.executeUpdate() != 0;
		}finally{
		  if (ps != null){
		    ps.close();
		  }
		}
		return success;
	}

	private static final String REMOVE_CLAIM_SUBMISSION = " UPDATE insurance_claim " +
			" SET last_submission_batch_id=NULL, status = 'O' WHERE 1=1 ";

	public boolean removeClaimSubmission(Connection con, List claims) throws SQLException {
		PreparedStatement ps = null;
		boolean success = false;
		try{
		  StringBuilder query = new StringBuilder(REMOVE_CLAIM_SUBMISSION);
	    DataBaseUtil.addWhereFieldInList(query, "claim_id", claims);
	    ps = con.prepareStatement(query.toString());

	    int i = 1;
	    for(int j=0;j<claims.size();j++) {
	      ps.setString(i++, claims.get(j).toString());
	    }
	    success = ps.executeUpdate() != 0;
		}finally{
		  if (ps != null){
		    ps.close();
		  }
		}
		return success;
	}

	private static final String REMOVE_CLAIM_RESUBMISSION = "UPDATE insurance_claim ic "
			+ "	SET last_submission_batch_id = foo2.latest_submission_id, status ='M' FROM "
			+ "("
			+ "	SELECT max(submission_batch_id) as latest_submission_id, claim_id "
			+ "	FROM claim_submissions WHERE claim_id = ? GROUP BY claim_id"
			+ ") as foo2 "
			+ "	WHERE foo2.claim_id = ? AND foo2.claim_id = ic.claim_id";
	
	public boolean deleteClaimResubmission(Connection con, List claims) throws SQLException {
		PreparedStatement ps = null;
		boolean success = false;
		try{
		  StringBuilder query = new StringBuilder(REMOVE_CLAIM_RESUBMISSION);
	    ps = con.prepareStatement(query.toString());

	    int i = 1;
	    for(int j=0;j<claims.size();j++) {
	      ps.setString(i++, claims.get(j).toString());
	      ps.setString(i++, claims.get(j).toString());
	    }
	    success = ps.executeUpdate()!= 0; 
		}finally{
		  if (ps != null){
		    ps.close();
		  } 
		}
		return success;
	}

	private static final String UPDATE_CLAIM_SUBMISSION_WITH_CORRECTION = " UPDATE insurance_claim SET " +
		" submission_id_with_correction = ? WHERE resubmission_type = 'correction' ";

  public boolean updateClaimSubmissionIdWithCorrection(
      Connection con, List claims, String submissionBatchID) throws SQLException {
    PreparedStatement ps = null;
    boolean success = false;
    try {
      StringBuilder query = new StringBuilder(UPDATE_CLAIM_SUBMISSION_WITH_CORRECTION);
      DataBaseUtil.addWhereFieldInList(query, "claim_id", claims);
      ps = con.prepareStatement(query.toString());

      int i = 1;
      ps.setString(i++, submissionBatchID);
      for (int j = 0; j < claims.size(); j++) {
        ps.setString(i++, claims.get(j).toString());
      }
      success = ps.executeUpdate() != 0;
    } finally {
      if (ps != null) {
        ps.close();
      }
    }
    return success;
  }

	private static final String DELETE_RESUBMISSION_FROM_INSURANCE = "UPDATE insurance_claim ic SET status = 'M', last_submission_batch_id = foo2.max_submission_batch_id "
			+ "FROM ("
			+ "        SELECT max(submission_batch_id) as max_submission_batch_id, foo.claim_id as claim_id  FROM claim_submissions cs,"
			+ "        ("
			+ "            SELECT claim_id FROM claim_submissions where submission_batch_id = ? "
			+ "        ) as foo WHERE cs.claim_id = foo.claim_id "
			+ "        AND submission_batch_id != ? GROUP BY foo.claim_id"
			+ "    ) as foo2 WHERE foo2.claim_id = ic.claim_id";

	public boolean deleteReSubmissionFromInsurance(Connection con, String submissionBatchID) throws SQLException {
		PreparedStatement ps = null;
		boolean success = false;
		try{
		  StringBuilder query = new StringBuilder(DELETE_RESUBMISSION_FROM_INSURANCE);
	    ps = con.prepareStatement(query.toString());
	    ps.setString(1, submissionBatchID);
	    ps.setString(2, submissionBatchID);
	    success = ps.executeUpdate() != 0;
		}finally{
		  if (ps != null){
		    ps.close();
		  }
		}
		return success;
	}

	private static final String DELETE_SUBMISSION_FROM_INSURANCE = "UPDATE insurance_claim SET last_submission_batch_id = null, status = 'O' "
			+ "WHERE last_submission_batch_id = ?";

	public String deleteSubmissionFromInsurance(Connection con, String submissionBatchID) throws SQLException {
		PreparedStatement ps = null;
		try{
		  ps = con.prepareStatement(DELETE_SUBMISSION_FROM_INSURANCE);
	    ps.setString(1, submissionBatchID);
	    ps.executeUpdate();
		}finally{
		  if (ps != null){
		    ps.close();
		  }
		}
		return null;
	}

	private static final String DELETE_SUBMISSION = "DELETE FROM claim_submissions WHERE submission_batch_id = ?";

	public boolean deleteSubmission(Connection con, String submissionBatchID) throws SQLException {
		PreparedStatement ps = null;
		int rowsDeleted = 0;
		try{
		  ps = con.prepareStatement(DELETE_SUBMISSION);
	    ps.setString(1, submissionBatchID);
	    rowsDeleted = ps.executeUpdate();
		}finally{
		  if (ps != null){
		    ps.close();
		  }
		}
		return (rowsDeleted != 0);

	}

	/*
	 * When a submission is sent i.e claims are sent, then update the claim status as sent.
	 * When a submission is rejected i.e claims are rejected, then update the claim status as open.
	 */
	private static final String UPDATE_SUBM_CLAIM_STATUS = "UPDATE claim_submissions SET status=? WHERE submission_batch_id= ?";

	public String updateClaimBatchStatus(Connection con, String submissionBatchID, String status) throws SQLException {
		PreparedStatement ps = null;
		try{
		  ps = con.prepareStatement(UPDATE_SUBM_CLAIM_STATUS);
	    ps.setString(1, status);
	    ps.setString(2, submissionBatchID);
	    ps.executeUpdate();
		}finally{
		  if (ps != null){
		    ps.close();
		  }
		}
		return null;
	}


	private static final String GET_CLAIM = " SELECT ic.claim_id, ic.last_submission_batch_id, ic.payers_reference_no, ic.main_visit_id, ic.patient_id, " +
		" ic.status, foo.resubmission_count, ic.closure_type, ic.action_remarks, ic.resubmission_type, ic.comments " +
		" FROM insurance_claim ic " +
		" LEFT JOIN (SELECT COUNT(cs.submission_batch_id) AS resubmission_count, cs.claim_id FROM insurance_claim ic " + 
		"		JOIN claim_submissions cs ON (cs.claim_id = ic.claim_id) " +
		"		JOIN insurance_submission_batch isb ON (cs.submission_batch_id = isb.submission_batch_id) " +
		"		WHERE isb.is_resubmission = 'Y' GROUP BY cs.claim_id) as foo ON (foo.claim_id = ic.claim_id)" +
		"WHERE ic.claim_id = ? " ;

	public BasicDynaBean getClaimById(String claimId) throws SQLException {
		return DataBaseUtil.queryToDynaBean(GET_CLAIM, claimId);
	}

	private static final String GET_CLAIM_BY_MAIN_AND_ACCOUNT = " SELECT ic.claim_id, ic.last_submission_batch_id, " + 
		"ic.payers_reference_no, ic.main_visit_id, ic.patient_id, " +
		" ic.status, foo.resubmission_count, ic.closure_type, ic.action_remarks, ic.resubmission_type, ic.comments " +
		" FROM insurance_claim ic " +
		"JOIN (SELECT COUNT(cs.submission_batch_id) AS resubmission_count, cs.claim_id FROM insurance_claim ic " + 
		"		JOIN claim_submissions cs ON (cs.claim_id = ic.claim_id) " +
		"		JOIN insurance_submission_batch isb ON (cs.submission_batch_id = isb.submission_batch_id) " +
		"		WHERE isb.is_resubmission = 'Y' GROUP BY cs.claim_id) as foo ON (foo.claim_id = ic.claim_id)" +
		"WHERE ic.main_visit_id = ? AND ic.patient_id=? AND ic.op_type=? AND ic.account_group=? " ;

	public BasicDynaBean findClaimByMainVisitAndAccount(String mainVisitId, String patientId, String opType, int accountGroup) throws SQLException {
		PreparedStatement ps = null;
		opType = opType.equals("R") ? "M" : opType; // If Revisit then claim op type is Main.
		try{
		  ps = con.prepareStatement(GET_CLAIM_BY_MAIN_AND_ACCOUNT);
	    ps.setString(1, mainVisitId);
	    ps.setString(2, patientId);
	    ps.setString(3, opType);
	    ps.setInt(4, accountGroup);
	    return DataBaseUtil.queryToDynaBean(ps);
		}finally{
		  if(null != ps){
		    ps.close();
		  }
		}
		
	}

	public static final String CLAIM_DETAILS = "SELECT ic.claim_id, ic.status as claim_status, ic.main_visit_id, ic.patient_id, ic.op_type, ic.plan_id "+
		" FROM insurance_claim ic WHERE main_visit_id = ? AND patient_id=? AND op_type=? AND account_group=? " ;

	public static List<BasicDynaBean> searchClaims(String mainVisitId,
		String patientId, String opType, int accountGroup) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		List<BasicDynaBean> claims = null;

		try {
			con = DataBaseUtil.getConnection();
			opType = opType.equals("R") ? "M" : opType; // If Revisit then claim op type is Main.
			ps = con.prepareStatement(CLAIM_DETAILS);
			ps.setString(1, mainVisitId);
			ps.setString(2, patientId);
			ps.setString(3, opType);
			ps.setInt(4, accountGroup);
			claims = DataBaseUtil.queryToDynaList(ps);
		}finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return claims;
	}

	// Query to fetch all charges for a patient in an Episode (sales and returns item wise)

	public static final String GET_PHARMACY_DENIED_CHARGE_NET =
		" SELECT (coalesce(scl.insurance_claim_amt, 0.00) + coalesce(scl.return_insurance_claim_amt, 0.00) - coalesce(scl.claim_recd, 0.00)) AS net ";

	public static final String GET_PHARMACY_CHARGE_NET =
		" SELECT (coalesce(scl.insurance_claim_amt, 0.00) + coalesce(scl.return_insurance_claim_amt, 0)) AS net ";


	public static final String GET_ALL_PHARMACY_CHARGES_FIELDS =
		"	,s.batch_no, m.medicine_id::TEXT AS item_id, bc.act_description_id , s.quantity AS qty," +
		"	scl.insurance_claim_amt, s.amount AS amt, scl.return_insurance_claim_amt, s.return_amt, s.return_qty," +
		"	(s.amount + s.return_amt - s.tax - s.return_tax_amt) AS amount, s.rate, null as conducted_date, s.erx_activity_id AS erx_activity_id, sm.erx_reference_no AS erx_reference_no, " +
		"	(s.quantity + s.return_qty) AS quantity, s.disc AS discount, s.package_unit, " +
		"   s.sale_item_id AS sale_item_id, bc.charge_id, bc.charge_group,bc.charge_head,bc.bill_no,b.bill_type,b.is_tpa," +
		"	s.item_code, 'P-'||bc.charge_id||'-'||s.sale_item_id AS activity_charge_id," +
		"	m.medicine_name||' ('||bc.act_description||') ' AS act_description,scl.claim_activity_id, " +
		"	'Pharmacy' AS activity_group, cgc.chargegroup_name, chc.chargehead_name," +
		"	to_char(sale_date,'dd/MM/yyyy hh24:mi') AS posted_date, sale_date AS item_posted_date, " +
		"	scl.denial_code, idc.code_description AS denial_desc, idc.status AS denial_code_status, idc.example, idc.type AS denial_code_type," +
		"   s.denial_remarks, scl.closure_type, scl.rejection_reason_category_id,scl.claim_status, coalesce(scl.claim_recd, 0.00) as claim_recd_total," +
		"	s.sale_id, bc.charge_id AS activity_id , " +
		"	msct.haad_code as act_type, msct.code_type AS act_type_desc, chc.codification_supported, " +
		"	act_item_code, ic.main_visit_id, ic.claim_id, b.status AS bill_status, pr.visit_type AS patient_visit_type," +
		"	scl.prior_auth_id AS prior_auth_id, scl.prior_auth_mode_id, " +
		"	CASE WHEN (pr.op_type != 'O') THEN doc.doctor_license_number ELSE ref.doctor_license_number END AS doctor_license_number," +
		"	CASE WHEN (pr.op_type != 'O') THEN doc.doctor_name ELSE ref.referal_name END AS doctor_name," +
		"	CASE WHEN (pr.op_type != 'O') THEN doc.doctor_id ELSE ref.referal_no END AS doctor_id," +
		"	CASE WHEN (pr.op_type != 'O') THEN 'Doctor' ELSE ref.doctor_type END AS doctor_type,	" +
		"	drs.doctor_license_number AS conducting_dr_license_number, "+
		"   presc_doc.doctor_license_number AS prescribing_doctor_license_number, presc_doc.doctor_name AS prescribing_doctor_name, " +
		"   presc_doc.doctor_id AS prescribing_doctor_id, s.code_type AS code_type, NULL as submission_batch_type ";


	public static final String GET_ALL_PHARMACY_CHARGES_TABLES =
		"	FROM store_sales_details s" +
		"	JOIN sales_claim_details scl ON (scl.sale_item_id = s.sale_item_id) " +
		"	JOIN store_sales_main sm on (s.sale_id = sm.sale_id)" +
		"	JOIN store_item_details m ON s.medicine_id = m.medicine_id " +
		"	JOIN bill_charge bc ON (sm.charge_id= bc.charge_id)" +
		"   JOIN chargehead_constants chc ON (bc.charge_head = chc.chargehead_id) " +
		"   JOIN chargegroup_constants cgc ON (bc.charge_group = cgc.chargegroup_id)" +
		"	JOIN bill b ON (b.bill_no = bc.bill_no)" +
		" LEFT JOIN bill_activity_charge bac ON (bac.charge_id = bc.charge_id) " +
		" LEFT JOIN tests_conducted tc ON (bac.activity_id = text(tc.prescribed_id) AND tc.patient_id=b.visit_id  "
		+ " AND bac.activity_code ='DIA') " +
		"	JOIN bill_claim bcl ON (b.bill_no = bcl.bill_no and bcl.claim_id = scl.claim_id) " +
		"	JOIN patient_registration pr ON (pr.patient_id = b.visit_id)" +
		"	LEFT JOIN doctors doc ON (doc.doctor_id = pr.doctor)" +
		"	LEFT JOIN doctors presc_doc ON (presc_doc.doctor_id = bc.prescribing_dr_id)" +
		" 	LEFT JOIN (" +
		"			SELECT 'Referal' AS doctor_type,referal_no, referal_name, clinician_id AS doctor_license_number FROM referral" +
		"			UNION ALL" +
		"			SELECT 'Doctor' AS doctor_type,doctor_id, doctor_name, doctor_license_number FROM doctors" +
		"			) AS ref ON (ref.referal_no = pr.reference_docto_id)" +
		"	JOIN insurance_claim ic ON (ic.claim_id = bcl.claim_id AND ic.patient_id = pr.patient_id)" +
		"	LEFT JOIN mrd_supported_code_types msct ON (msct.code_type = s.code_type)" +
		"   LEFT JOIN insurance_denial_codes idc ON (idc.denial_code = scl.denial_code) " +
		"	LEFT JOIN doctors drs ON (drs.doctor_id = bc.payee_doctor_id)" +
		"	WHERE (b.status != 'X') AND bc.status!='X' " ;


	public static final String GET_HOSPITAL_DENIED_CHARGE_NET =
		" SELECT (coalesce(bccl.insurance_claim_amt, 0.00) + coalesce(bcc.return_insurance_claim_amt, 0.00) - coalesce(bccl.claim_recd_total, 0.00)) AS net ";

	public static final String GET_HOSPITAL_CHARGE_NET = " SELECT " +
				"CASE WHEN bcc.submission_batch_type = 'P' THEN FOO.total_package_ins_claim_amt " +
				"ELSE (coalesce(bccl.insurance_claim_amt, 0.00) + "
				+ " coalesce(bcc.return_insurance_claim_amt, 0.00)) END AS net ";


	public static final String GET_ALL_HOSPITAL_CHARGES_FIELDS = " , '' AS batch_no, " +
			" ''   AS item_id, " +
			" bcc.act_description_id, " +
			" bcc.act_quantity AS qty, " +
			" CASE WHEN bcc.submission_batch_type = 'P' " +
			" THEN " +
			" FOO.total_package_ins_claim_amt " +
			" ELSE" +
			" bccl.insurance_claim_amt END as insurance_claim_amt, " +
			" CASE WHEN bcc.submission_batch_type = 'P' " +
			" THEN " +
			" FOO.total_amount" +
			" ELSE" +
			" bcc.amount END AS amt,  " +
			" CASE WHEN bcc.submission_batch_type = 'P' " +
			" THEN " +
			" FOO.tot_return_insurance_claim_amt" +
			" ELSE" +
			" bcc.return_insurance_claim_amt END as return_insurance_claim_amt, " +
			" CASE WHEN bcc.submission_batch_type = 'P' " +
			" THEN FOO.tot_return_amt" +
			" ELSE" +
			" bcc.return_amt END as return_amt, " +
			" bcc.return_qty  as return_qty, " +
			" CASE WHEN bcc.submission_batch_type = 'P' " +
			" THEN FOO.total_amount" +
			" ELSE" +
			" (bcc.amount + bcc.return_amt) END AS amount, " +
			" CASE WHEN bcc.submission_batch_type = 'P' " +
			" THEN" +
			" FOO.tot_rate" +
			" ELSE" +
			" bcc.act_rate           END       AS rate, " +
			" footc.conducted_date, " +
			" '' AS erx_activity_id, " +
			" '' AS erx_reference_no, " +
			" (bcc.act_quantity + bcc.return_qty) AS quantity, " +
			" CASE WHEN bcc.submission_batch_type = 'P' " +
			" THEN" +
			" FOO.discount" +
			" ELSE" +
			" bcc.discount END AS discount, " +
			" 1 AS package_unit, " +
			" 0 AS sale_item_id, " +
			" bcc.charge_id AS charge_id, " +
			" bcc.charge_group, " +
			" bcc.charge_head, " +
			" bcc.bill_no, " +
			" b.bill_type, " +
			" b.is_tpa, " +
			" (act_rate_plan_item_code) AS item_code, " +
			" 'A-' " +
			" ||bcc.charge_id AS activity_charge_id, " +
			" bcc.act_description, " +
			" bccl.claim_activity_id, " +
			" 'Hospital' AS activity_group, " +
			" cgc.chargegroup_name, " +
			" chc.chargehead_name, " +
			" to_char(posted_date, 'dd/MM/yyyy hh24:mi') AS posted_date, " +
			" posted_date AS item_posted_date, " +
			" bccl.denial_code, " +
			" idc.code_description AS denial_desc, " +
			" idc.status AS denial_code_status, " +
			" idc.example, " +
			" idc.type AS denial_code_type, " +
			" bccl.denial_remarks, " +
			" bccl.closure_type , " +
			" bccl.rejection_reason_category_id, " +
			" bccl.claim_status, " +
			" CASE WHEN bcc.submission_batch_type = 'P' THEN FOO.tot_claim_recd_amt ELSE"+
			"          COALESCE(bccl.claim_recd_total, 0.00) END AS claim_recd_total, " + 
			"          ''                                    AS sale_id, " +
			"          ''                                    AS activity_id , " +
			"          msct.haad_code                        AS act_type, " +
			"          msct.code_type                        AS act_type_desc, " +
			"          chc.codification_supported, " +
			"          '' AS act_item_code, " +
			"          ic.main_visit_id, " +
			"          ic.claim_id, " +
			"          b.status AS bill_status, " +
			"          pr.visit_type AS " +
			"          patient_visit_type, " +
			"          COALESCE(bccl.prior_auth_id, pr.prior_auth_id) AS prior_auth_id, " +
			"          COALESCE(bccl.prior_auth_mode_id, pr.prior_auth_mode_id) AS " +
			"prior_auth_mode_id, " +
			"          CASE " +
			"                    WHEN ( pr.op_type != 'O') THEN doc.doctor_license_number " +
			"                    ELSE ref.doctor_license_number " +
			"          END AS doctor_license_number, " +
			"          CASE " +
			"                    WHEN ( pr.op_type != 'O') THEN doc.doctor_name " +
			"                    ELSE ref.referal_name " +
			"          END AS doctor_name, " +
			"          CASE " +
			"                    WHEN ( pr.op_type != 'O') THEN doc.doctor_id " +
			"                    ELSE ref.referal_no " +
			"          END AS doctor_id, " +
			"          CASE " +
			"                    WHEN (  pr.op_type != 'O') THEN 'Doctor' " +
			"                    ELSE ref.doctor_type " +
			"          END                             AS doctor_type, " +
			"          drs.doctor_license_number       AS conducting_dr_license_number, " +
			"          presc_doc.doctor_license_number AS prescribing_doctor_license_number, " +
			"          presc_doc.doctor_name           AS prescribing_doctor_name, " +
			"          presc_doc.doctor_id             AS prescribing_doctor_id, " +
			"          bcc.code_type                   AS code_type, bcc.submission_batch_type ";


	public static final String GET_ALL_HOSPITAL_CHARGES_TABLES =
		"	FROM bill_charge bcc " +
		"	JOIN bill_charge_claim bccl ON (bcc.charge_id = bccl.charge_id) " +
		"   JOIN chargehead_constants chc ON (bcc.charge_head = chc.chargehead_id) " +
		"   JOIN chargegroup_constants cgc ON (bcc.charge_group = cgc.chargegroup_id)" +
		"	JOIN bill b ON (b.bill_no = bcc.bill_no)" +
		" LEFT JOIN LATERAL " + 
		"  (SELECT MAX(tc.conducted_date) AS conducted_date, " + 
		"          bac.charge_id " + 
		"   FROM bill_activity_charge bac " + 
		"   JOIN tests_conducted tc ON (bac.activity_id = text(tc.prescribed_id) AND bac.activity_code ='DIA') " + 
		"   WHERE bac.charge_id = bcc.charge_id " + 
		"     AND tc.patient_id=b.visit_id " + 
		"   GROUP BY bac.charge_id) AS footc ON (footc.charge_id = bcc.charge_id)" + 
		"	JOIN bill_claim bcl ON (b.bill_no = bcl.bill_no and bcl.claim_id = bccl.claim_id) " +
		"	JOIN patient_registration pr ON (pr.patient_id = b.visit_id)" +
		"	LEFT JOIN doctors doc ON (doc.doctor_id = pr.doctor)" +
		"	LEFT JOIN doctors presc_doc ON (presc_doc.doctor_id = bcc.prescribing_dr_id)" +
		" 	LEFT JOIN (" +
		"			SELECT 'Referal' AS doctor_type,referal_no, referal_name, clinician_id AS doctor_license_number FROM referral" +
		"			UNION ALL" +
		"			SELECT 'Doctor' AS doctor_type,doctor_id, doctor_name, doctor_license_number FROM doctors" +
		"			) AS ref ON (ref.referal_no = pr.reference_docto_id)" +
		"	JOIN insurance_claim ic ON (ic.claim_id = bcl.claim_id AND ic.patient_id = pr.patient_id)" +
		"	LEFT JOIN mrd_supported_code_types msct ON (msct.code_type = bcc.code_type)" +
		"   LEFT JOIN insurance_denial_codes idc ON (idc.denial_code = bccl.denial_code)" +
		"	LEFT JOIN doctors drs ON (drs.doctor_id = bcc.payee_doctor_id) " +
		" LEFT JOIN LATERAL (SELECT sum(bcci.insurance_claim_amt) as total_package_ins_claim_amt, "+
		     " sum(bci.amount) AS total_amt, "+
		     " sum(bcci.return_insurance_claim_amt) as tot_return_insurance_claim_amt, "+
		     " sum(bci.return_amt) AS tot_return_amt, "+
		     " sum(bci.amount + bci.return_amt) AS total_amount, "+
		     " sum(bcci.insurance_claim_amt) AS tot_insurance_claim_amt, "+
		     " sum(bci.act_rate * bci.act_quantity)  AS tot_rate, "+
		     " sum(bci.act_quantity + bci.return_qty) AS tot_quantity, "+
		     " sum(bci.discount) as discount, COALESCE(sum(bcci.claim_recd_total), 0.00) "+
		     " as tot_claim_recd_amt,"+
		     " bci.package_id, bci.bill_no, "+
		     " CASE WHEN bcref.charge_ref ='' THEN bci.charge_ref "+
         " ELSE COALESCE(bcref.charge_ref,bci.charge_ref) END AS charge_ref, "+
		     " bcci.claim_id "+
		     " FROM bill_charge_claim bcci "+
		     " JOIN bill_charge bci ON(bci.charge_id = bcci.charge_id) "+
		     " LEFT JOIN LATERAL (select charge_ref,charge_id from bill_charge bc where bc.charge_id=bci.charge_ref) "+
		     " bcref on bcref.charge_id=bci.charge_ref "+
		        " WHERE (bci.submission_batch_type='P' AND bci.charge_head<>'PKGPKG') "+
		        " GROUP BY bci.bill_no, bci.package_id, "+
		        " CASE WHEN bcref.charge_ref ='' THEN bci.charge_ref "+
		        " ELSE COALESCE(bcref.charge_ref,bci.charge_ref) END, "+
		        " bcci.claim_id) AS FOO "+
		        " ON (FOO.package_id=bcc.package_id AND bcc.bill_no = FOO.bill_no "+
		        " AND FOO.claim_id=bcl.claim_id AND FOO.charge_ref = bcc.charge_id)"+
		"	WHERE (b.status != 'X') AND bcc.status!='X' AND bcc.charge_head NOT IN ('PHMED','PHCMED','PHRET','PHCRET') ";

	
	  /**
	   * The Constant ALL_CHARGES_QUERY.
	   */
	  public static final String ALL_CHARGES_QUERY = "SELECT * FROM ( ";

	  /**
	   * The Constant EXLCUDE_PACKAGES_WITH_ZERO_AMT.
	   */
	  /* Excluding the Package Charge Activity if the Claim Amt is zero and Submission Batch Type is P */
	  public static final String EXLCUDE_PACKAGES_WITH_ZERO_AMT = ") AS FINAL WHERE "
	  		+ " CASE WHEN submission_batch_type = 'P' " 
	  		+ " THEN  net > 0  ELSE submission_batch_type IS NULL OR submission_batch_type='I' END";
	  
	  public static final String DRG_PACKAGE_WITH_ZERO_AMT =  ") AS FINAL WHERE "
        + " CASE WHEN (submission_batch_type = 'P' AND charge_head = 'PKGPKG') " 
        + " THEN  net >= 0  ELSE submission_batch_type IS NULL OR submission_batch_type='I' END";

	public static final String ORDER_BY_CHARGES = " ORDER BY charge_group, charge_head,batch_no,item_id,act_description_id,charge_id, act_description, main_visit_id, claim_id" ;

	private static final String CHARGES_QUERY =
      GET_PHARMACY_CHARGE_NET + GET_ALL_PHARMACY_CHARGES_FIELDS + GET_ALL_PHARMACY_CHARGES_TABLES
        + " AND CASE WHEN (?) OR s.allow_zero_claim = true THEN (coalesce(scl.insurance_claim_amt, 0.00) + coalesce(scl.return_insurance_claim_amt, 0.00)) >= 0 "
        + " ELSE (coalesce(scl.insurance_claim_amt, 0.00) + coalesce(scl.return_insurance_claim_amt, 0.00)) > 0 END"
        + " AND (s.quantity + s.return_qty) > 0 "
        + " AND b.bill_no = ? and scl.claim_id = ? "

      +" UNION ALL " +

      GET_HOSPITAL_CHARGE_NET + GET_ALL_HOSPITAL_CHARGES_FIELDS + GET_ALL_HOSPITAL_CHARGES_TABLES
        + " AND CASE WHEN (?) OR bcc.allow_zero_claim = true OR bcc.submission_batch_type = 'P' THEN (coalesce(bccl.insurance_claim_amt, 0.00) + coalesce(bcc.return_insurance_claim_amt, 0.00)) >= 0 "
        + " ELSE (coalesce(bccl.insurance_claim_amt, 0.00) + coalesce(bcc.return_insurance_claim_amt, 0.00)) > 0 END"
        + " AND (bcc.act_quantity + bcc.return_qty) > 0 "
        + " AND b.bill_no = ? and bccl.claim_id = ? "
		+ " AND (bcc.submission_batch_type IS NULL OR (bcc.submission_batch_type = 'P' and bcc"
        + ".charge_head = 'PKGPKG') OR (bcc.submission_batch_type = 'I' and bcc.charge_head <>'PKGPKG')) ";
	
	public List<BasicDynaBean> findAllCharges(String billNo, String claimId) throws SQLException {
	  List<BasicDynaBean> chargeList = new ArrayList<>();
		BasicDynaBean billbean = billDAO.findByKey("bill_no", billNo);
		if (billbean != null) {
		  BasicDynaBean visitbean = patientRegistrationDAO.findByKey("patient_id", billbean.get("visit_id"));
	      boolean hasDRG = visitbean != null && visitbean.get("use_drg").equals("Y");
	      boolean hasPerdiem = visitbean != null && visitbean.get("use_perdiem").equals("Y");
	      boolean checkDrgOrPerDiem = (hasDRG || hasPerdiem);
	      String query = ALL_CHARGES_QUERY + CHARGES_QUERY;
	      if (checkDrgOrPerDiem) {
	        query = query + DRG_PACKAGE_WITH_ZERO_AMT + ORDER_BY_CHARGES;
	      } else {
	        query = query + EXLCUDE_PACKAGES_WITH_ZERO_AMT + ORDER_BY_CHARGES;
	      }
		  chargeList = DataBaseUtil.queryToDynaList(query, new Object[] {checkDrgOrPerDiem, billNo, claimId, checkDrgOrPerDiem, billNo, claimId});
		}
		return chargeList;
	}

	// While Resubmitting Filtered charges, Resubmission type is Internal complaint then,
	// only those activites which are denied should go in XML.
	private static final String CHARGES_QUERY_FOR_RESUB =
      GET_PHARMACY_DENIED_CHARGE_NET + GET_ALL_PHARMACY_CHARGES_FIELDS + GET_ALL_PHARMACY_CHARGES_TABLES
        + " AND CASE WHEN (?) OR s.allow_zero_claim = true THEN (coalesce(scl.insurance_claim_amt, 0.00) + coalesce(scl.return_insurance_claim_amt, 0.00)) >= 0 "
        + " ELSE (coalesce(scl.insurance_claim_amt, 0.00) + coalesce(scl.return_insurance_claim_amt, 0.00)) > 0 END"
        + " AND (s.quantity + s.return_qty) > 0 "
        + " AND (coalesce(scl.insurance_claim_amt, 0.00) != coalesce(scl.claim_recd, 0.00)) AND b.bill_no = ? AND scl.claim_id = ? "
        + " AND scl.closure_type != 'D' "
        + " UNION ALL " +

      GET_HOSPITAL_DENIED_CHARGE_NET + GET_ALL_HOSPITAL_CHARGES_FIELDS + GET_ALL_HOSPITAL_CHARGES_TABLES
        + " AND CASE WHEN (?) OR bcc.allow_zero_claim = true THEN (coalesce(bccl.insurance_claim_amt, 0.00) + coalesce(bcc.return_insurance_claim_amt, 0.00)) >= 0 "
        + " ELSE (coalesce(bccl.insurance_claim_amt, 0.00) + coalesce(bcc.return_insurance_claim_amt, 0.00)) > 0 END"
        + " AND (bcc.act_quantity + bcc.return_qty) > 0 "
        + " AND (coalesce(bccl.insurance_claim_amt, 0.00) != coalesce(bccl.claim_recd_total, 0.00)) AND b.bill_no = ? AND bccl.claim_id = ? "
        + " AND bccl.closure_type != 'D' ";
	public List<BasicDynaBean> findAllChargesForResub(String billNo, String claimId) throws SQLException {
	  List<BasicDynaBean> chargesListForResub = new ArrayList<>();
		BasicDynaBean billbean = billDAO.findByKey("bill_no", billNo);
		if (billbean != null) {
	      BasicDynaBean visitbean = patientRegistrationDAO.findByKey("patient_id", billbean.get("visit_id"));
	      boolean hasDRG = visitbean != null && visitbean.get("use_drg").equals("Y");
	      boolean hasPerdiem = visitbean != null && visitbean.get("use_perdiem").equals("Y");
	      boolean checkDrgOrPerDiem = (hasDRG || hasPerdiem);
		  chargesListForResub = DataBaseUtil.queryToDynaList(CHARGES_QUERY_FOR_RESUB + ORDER_BY_CHARGES, new Object[] {checkDrgOrPerDiem, billNo, claimId, checkDrgOrPerDiem, billNo, claimId});
		}
		return chargesListForResub;
	}

	private static final String CHARGES_QUERY_INCLUDING_ZERO_CLAIM =
      GET_PHARMACY_CHARGE_NET + GET_ALL_PHARMACY_CHARGES_FIELDS + GET_ALL_PHARMACY_CHARGES_TABLES
        + " AND CASE WHEN (?) OR s.allow_zero_claim = true THEN (coalesce(scl.insurance_claim_amt, 0.00) + coalesce(scl.return_insurance_claim_amt, 0.00)) >= 0 "
        + " ELSE (coalesce(scl.insurance_claim_amt, 0.00) + coalesce(scl.return_insurance_claim_amt, 0.00)) > 0 END"
        + " AND (s.quantity + s.return_qty) > 0 "
        + " AND b.bill_no = ? AND scl.claim_id = ? "

      +" UNION ALL " +

      GET_HOSPITAL_CHARGE_NET + GET_ALL_HOSPITAL_CHARGES_FIELDS + GET_ALL_HOSPITAL_CHARGES_TABLES
        + " AND CASE WHEN (?) OR bcc.allow_zero_claim = true OR bcc.submission_batch_type = 'P' THEN (coalesce(bccl.insurance_claim_amt, 0.00) + coalesce(bcc.return_insurance_claim_amt, 0.00)) >= 0 "
        + " ELSE (coalesce(bccl.insurance_claim_amt, 0.00) + coalesce(bcc.return_insurance_claim_amt, 0.00)) > 0 END"
        + " AND (bcc.act_quantity + bcc.return_qty) > 0 "
        + " AND b.bill_no = ? AND bccl.claim_id = ? "
			  + " AND (bcc.submission_batch_type IS NULL OR (bcc.submission_batch_type = 'P' and bcc.charge_head = 'PKGPKG')"
			  + " OR (bcc.submission_batch_type = 'I' and bcc.charge_head <>'PKGPKG'))";
	public List<BasicDynaBean> findAllBillChargesIncludingZeroClaim(String billNo, String claimId) throws SQLException {
	  List<BasicDynaBean> chargesIncludingZeroClaim = new ArrayList<>();
		BasicDynaBean billbean = billDAO.findByKey("bill_no", billNo);

		if (billbean != null) {
	      BasicDynaBean visitbean = patientRegistrationDAO.findByKey("patient_id", billbean.get("visit_id"));
	      boolean hasDRG = visitbean != null && visitbean.get("use_drg").equals("Y");
	      boolean hasPerdiem = visitbean != null && visitbean.get("use_perdiem").equals("Y");
	      boolean checkDrgOrPerDiem = (hasDRG || hasPerdiem);
	      String query = ALL_CHARGES_QUERY + CHARGES_QUERY_INCLUDING_ZERO_CLAIM;
	      if (checkDrgOrPerDiem) {
	        query = query + DRG_PACKAGE_WITH_ZERO_AMT + ORDER_BY_CHARGES;
	      } else {
	        query = query + EXLCUDE_PACKAGES_WITH_ZERO_AMT + ORDER_BY_CHARGES;
	      }
		  chargesIncludingZeroClaim = DataBaseUtil.queryToDynaList(query, new Object[] {checkDrgOrPerDiem, billNo, claimId, checkDrgOrPerDiem, billNo, claimId});
		}
		return chargesIncludingZeroClaim;
	}

	public static final String ALL_CHARGES = "SELECT * FROM " +
		" ( "+	GET_PHARMACY_CHARGE_NET + GET_ALL_PHARMACY_CHARGES_FIELDS + GET_ALL_PHARMACY_CHARGES_TABLES
			 + " AND (coalesce(scl.insurance_claim_amt, 0.00) + coalesce(scl.return_insurance_claim_amt, 0.00)) >= 0 " +
		"	UNION ALL " +
				GET_HOSPITAL_CHARGE_NET + GET_ALL_HOSPITAL_CHARGES_FIELDS + GET_ALL_HOSPITAL_CHARGES_TABLES
			+ " AND (coalesce(bccl.insurance_claim_amt, 0.00) + coalesce(bcc.return_insurance_claim_amt, 0.00)) >= 0 ) AS FOO " ;

	public List<BasicDynaBean> findChargesByDescription(String billNo, String actDescription) throws SQLException {

		String findChargesByDescription = ALL_CHARGES + " WHERE bill_no = ?  AND act_description = ? ";

		return DataBaseUtil.queryToDynaList(findChargesByDescription + ORDER_BY_CHARGES, new Object[] {billNo, actDescription});
	}

	public List<BasicDynaBean> findChargesByDescription(String billNo, String claimId, String actDescription) throws SQLException {

		String findChargesByDescription = ALL_CHARGES + " WHERE bill_no = ?  AND act_description = ? AND claim_id = ?";

		return DataBaseUtil.queryToDynaList(findChargesByDescription + ORDER_BY_CHARGES, new Object[] {billNo, actDescription, claimId});
	}
	
	private static final String BILL_CLAIM_TOTAL = "SELECT SUM(net) FROM " +
      " ( "+  GET_PHARMACY_CHARGE_NET + GET_ALL_PHARMACY_CHARGES_TABLES
         + " AND (coalesce(scl.insurance_claim_amt, 0.00) + coalesce(scl.return_insurance_claim_amt, 0.00)) >= 0 "
         + " AND (s.quantity + s.return_qty) > 0 "
         + " AND b.bill_no = ? AND scl.claim_id = ? "

        +" UNION ALL " +

        GET_HOSPITAL_CHARGE_NET + GET_ALL_HOSPITAL_CHARGES_TABLES
          + " AND (coalesce(bccl.insurance_claim_amt, 0.00) + coalesce(bcc.return_insurance_claim_amt, 0.00)) >= 0 "
          + " AND (bcc.act_quantity + bcc.return_qty) > 0 "
          + " AND b.bill_no = ? AND bccl.claim_id = ?) AS FOO " ;
	// Bill claim net amount
	public BigDecimal getBillClaimTotal(Connection con, String billNo, String claimId) throws SQLException {
		PreparedStatement ps = null;
		try{
		  ps = con.prepareStatement(BILL_CLAIM_TOTAL);
	    ps.setString(1, billNo);
	    ps.setString(2, claimId);
	    ps.setString(3, billNo);
	    ps.setString(4, claimId);
	    BigDecimal totalClaim = DataBaseUtil.getBigDecimalValueFromDb(ps);
	    return (totalClaim) == null ? BigDecimal.ZERO : totalClaim;
		}finally{
		  if(null != ps){
		    ps.close();
		  }
		}
		
	}

	private static final String GET_CLAIM_BILL_OPEN =
		" SELECT bill_no FROM bill JOIN bill_claim bcl ON (bcl.bill_no = b.bill_no) WHERE bcl.claim_id = ? AND b.status IN ('A','F') " +
		" AND primary_claim_status != 'R' ORDER BY open_date LIMIT 1 ";

	public String getClaimOpenBillNo(String claimID) throws SQLException {
		return DataBaseUtil.getStringValueFromDb(GET_CLAIM_BILL_OPEN, claimID);
	}

	private static final String CLAIM_SEQUENCE_PATTERN =
		" SELECT pattern_id " +
		" FROM (SELECT min(priority) as priority,  pattern_id FROM hosp_claim_seq_prefs " +
		" WHERE (center_id=? or center_id= 0) " +
		" AND (account_group = ? or account_group = 0) "+
		" GROUP BY pattern_id ORDER BY priority limit 1) as foo";

	public String getGeneratedClaimIdBasedonCenterId(int centerId, Integer accGrpId) throws SQLException {

		BasicDynaBean b = DataBaseUtil.queryToDynaBean(CLAIM_SEQUENCE_PATTERN, new Object[]{centerId, accGrpId});
		String patternId = (String) b.get("pattern_id");
		return DataBaseUtil.getNextPatternId(patternId);
	}
	
	private static final String UPDATE_CLAIM_CLOSE_WRITEOFF = "UPDATE insurance_claim ic SET status = 'C' "
			+ "	FROM bill_claim bcl "
			+ "	WHERE bcl.bill_no = ? AND bcl.claim_id = ic.claim_id";
	
	public static boolean updateWriteOffClaimClose(String billNo) throws SQLException {
		boolean success = false;
		Connection con = null;
		PreparedStatement ps = null;
		try{
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			ps = con.prepareStatement(UPDATE_CLAIM_CLOSE_WRITEOFF);
			ps.setString(1, billNo);
			success = ps.executeUpdate() >= 0;
		}finally{
		  if(null != ps){
		    ps.close();
		  }
			DataBaseUtil.commitClose(con, success);
		}
		return success;
	}

	private static String GET_CLAIM_ACTIVITY_IDS= " SELECT bc.charge_id" +
			" FROM store_sales_details s " +
			" JOIN sales_claim_details scl ON (scl.sale_item_id = s.sale_item_id) " +
			" JOIN store_sales_main sm on (s.sale_id = sm.sale_id) " +
			" JOIN bill_charge bc ON (sm.charge_id= bc.charge_id) " +
			" JOIN bill b ON (b.bill_no = bc.bill_no) " +
			" WHERE (b.status != 'X') AND bc.status!='X' " +
			" AND CASE WHEN  s.allow_zero_claim = true "+
			" THEN (coalesce(scl.insurance_claim_amt, "+
			" 0.00) + coalesce(scl.return_insurance_claim_amt, 0.00)) >= 0"+
			" ELSE (coalesce(scl.insurance_claim_amt, "+
			" 0.00) + coalesce(scl.return_insurance_claim_amt, 0.00)) > 0 "+
			" END AND (s.quantity + s.return_qty) > 0  AND scl.claim_id = ? "+
			" #externalPBM " +
			" UNION ALL " +
			" SELECT  bcc.charge_id " +
			" FROM bill_charge bcc " +
			" JOIN bill_charge_claim bccl ON (bcc.charge_id = bccl.charge_id)" +
			" JOIN bill b ON (b.bill_no = bcc.bill_no) " +
			" WHERE (b.status != 'X') AND bcc.status!='X' "+
			" AND bcc.charge_head NOT IN ('PHMED','PHCMED','PHRET','PHCRET') "+
			" AND CASE WHEN  bcc.allow_zero_claim = true "+
			" THEN (coalesce(bccl.insurance_claim_amt, "+
			" 0.00) + coalesce(bcc.return_insurance_claim_amt, 0.00)) >= 0 "+
			" ELSE (coalesce(bccl.insurance_claim_amt, "+
			" 0.00) + coalesce(bcc.return_insurance_claim_amt, 0.00)) > 0 "+
			" END AND (bcc.act_quantity + bcc.return_qty) > 0  AND bccl.claim_id = ?";

	public boolean hasClaimAcitivity(String claimId, Boolean ignoreExternalPbm) throws SQLException {
    List<String> claimActivityIds = null;
    String query = GET_CLAIM_ACTIVITY_IDS;

    if (ignoreExternalPbm) {
      query = query.replace("#externalPBM", "AND is_external_pbm = false ");
    } else {
      query = query.replace("#externalPBM", "");
    }

		try (Connection con = DataBaseUtil.getReadOnlyConnection();
				PreparedStatement ps = con.prepareStatement(query);) {
				ps.setString(1, claimId);
				ps.setString(2, claimId);
				claimActivityIds = DataBaseUtil.queryToDynaList(ps);
				if (claimActivityIds != null && claimActivityIds.size() > 0) {
					return true;
				}
		}
		return false;
	}

}
