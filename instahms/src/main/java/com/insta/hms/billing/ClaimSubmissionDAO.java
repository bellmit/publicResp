/**
 *
 */
package com.insta.hms.billing;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.Preferences;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;
import com.insta.hms.common.UrlUtil;
import com.insta.hms.diagnosticmodule.common.DiagnosticsDAO;
import com.insta.hms.dischargesummary.DischargeSummaryReportHelper;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;
import com.insta.hms.mdm.mrdcodes.MrdCodeRepository;
import com.insta.hms.outpatient.DoctorConsultationDAO;
import com.insta.hms.outpatient.OPPrescriptionFtlHelper;
import com.lowagie.text.DocumentException;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.RowSetDynaClass;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import freemarker.template.TemplateException;

/**
 * @author lakshmi.p
 *
 */
public class ClaimSubmissionDAO extends GenericDAO {

	static Logger logger = LoggerFactory.getLogger(ClaimSubmissionDAO.class);
	
	private static final GenericDAO mrdObservationDAO = new GenericDAO("mrd_observations");

	public ClaimSubmissionDAO() {
		super("insurance_submission_batch");
	}

	public String getGeneratedSubmissionBatchId() throws SQLException {
		return DataBaseUtil.getNextPatternId("insurance_submission_batch");
	}

	public static String fillSubmissionIDSearch(String submissionID) {
		if((submissionID == null) || (submissionID.equals("")))
			return null;
		int submissionIDDigits = DataBaseUtil.getStringValueFromDb("SELECT num_pattern FROM hosp_id_patterns " +
				" WHERE pattern_id ='insurance_submission_batch'").length();

		if(submissionID.length() < submissionIDDigits){
			String submitID = DataBaseUtil.getStringValueFromDb("SELECT std_prefix||''||trim((TO_CHAR("
				+submissionID+",num_pattern))) FROM hosp_id_patterns WHERE pattern_id ='insurance_submission_batch'");
			if(submitID != null && !submitID.equals(""))
				return submitID;
		}
		return submissionID;
	}

	private static String getSubmissionDetails =
			"  SELECT submission_batch_id, created_date, submission_date, insurance_co_id, " +
			"  file_name, tpa_id, insurance_category_id, patient_type, is_resubmission, status, username, account_group " +
			"  FROM insurance_submission_batch" +
			"  WHERE submission_batch_id =" +
			"     (SELECT last_submission_batch_id FROM insurance_claim ic " + 
			"		JOIN claim_submissions cs ON (cs.claim_id = ic.claim_id) " + 
			"		JOIN insurance_submission_batch isb ON (cs.submission_batch_id = isb.submission_batch_id) " + 
			"		WHERE ic.claim_id = ? AND isb.is_resubmission = 'Y' GROUP BY last_submission_batch_id HAVING  COUNT(cs.submission_batch_id) = 0) " +
			"UNION " +
			"  SELECT submission_batch_id, created_date, submission_date, insurance_co_id,  " +
			"  file_name, tpa_id, insurance_category_id, patient_type, is_resubmission, status, username, account_group " +
			"  FROM insurance_submission_batch " +
			"  WHERE submission_batch_id = (SELECT submission_batch_id FROM insurance_submission_batch WHERE submission_batch_id " +
			"								IN ( SELECT submission_batch_id FROM claim_submissions WHERE claim_id = ?)" +
			"										ORDER BY created_date DESC LIMIT 1) ";

	public BasicDynaBean getSubmissionDetails(String claimId) throws SQLException {
		return DataBaseUtil.queryToDynaBean(getSubmissionDetails, new Object[] {claimId, claimId});
	}

	private static final String CLAIM_SUBMISSION_FIELDS = " SELECT *  ";

	private static final String CLAIM_SUBMISSION_COUNT = " SELECT count(submission_batch_id) ";

	private static final String CLAIM_SUBMISSION_TABLES = " FROM (SELECT isb.submission_batch_id, created_date,"
		+" submission_date, isb.insurance_co_id, file_name, isb.tpa_id, instab.insurance_category_id, patient_type, is_resubmission,"
		+" isb.status,isb.username,instab.insurance_category_id AS category_id,cat.category_name, tp.tpa_name,isb.plan_id,ipm.plan_name, "
		+" icm.insurance_co_name,isb.account_group,ag.account_group_name,isb.center_id,hc.center_name, isb.processing_status, isb.processing_type, "
		+" ag.account_group_service_reg_no, hc.hospital_center_service_reg_no,COALESCE(tcp.claim_format, tp.claim_format) AS claim_format "
		+" FROM insurance_submission_batch isb "
		+" LEFT JOIN tpa_master tp ON(tp.tpa_id = isb.tpa_id)"
		+" LEFT JOIN tpa_center_master tcp ON(tcp.tpa_id = isb.tpa_id  and tcp.center_id = isb.center_id)"
		+" LEFT JOIN insurance_company_master icm ON(icm.insurance_co_id = isb.insurance_co_id)"
		+" LEFT JOIN (SELECT submission_batch_id, " +
				" regexp_split_to_table(insurance_category_id, E'\\,')::TEXT AS insurance_category_id " +
				" FROM insurance_submission_batch ) AS instab " +
				" ON (instab.submission_batch_id = isb.submission_batch_id) "
		+" LEFT JOIN insurance_category_master cat ON (cat.category_id::text = instab.insurance_category_id)"
		+" LEFT JOIN insurance_plan_main ipm ON(ipm.plan_id = isb.plan_id)"
		+" LEFT JOIN account_group_master ag ON(ag.account_group_id = isb.account_group)"
		+" LEFT JOIN hospital_center_master hc ON(hc.center_id = isb.center_id)) AS foo ";

	public static PagedList searchClaimSubmissions(Map filter, Map<LISTING, Object> listing,List<String> accGrpFilter,List<String> centerFilter) throws SQLException, ParseException {

		Connection con = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			String[] accGrp = accGrpFilter.toArray(new String[accGrpFilter.size()]);
			String[] centerId = centerFilter.toArray(new String[centerFilter.size()]);

			SearchQueryBuilder qb = new SearchQueryBuilder(con, CLAIM_SUBMISSION_FIELDS,
					CLAIM_SUBMISSION_COUNT, CLAIM_SUBMISSION_TABLES, listing);
			qb.addFilterFromParamMap(filter);

			StringBuilder qryExp=new StringBuilder("");

				if(centerId != null && centerId.length > 0){
				  qryExp = qryExp.append(" center_id IN (");
					for(int i=0;i<centerId.length; i++){
					  qryExp.append(centerId[0]);
							if(i<centerId.length-1)
								qryExp.append(",");
					}
					qryExp.append(") ");
				}

				if((centerId != null && centerId.length > 0) && (accGrp != null && accGrp.length > 0)){
				  qryExp.append(" OR ");
				}

				if(accGrp != null && accGrp.length > 0){
				  qryExp.append(" account_group IN (");
					for(int i=0;i<accGrp.length; i++){
					  qryExp.append(accGrp[i]);
							if(i<accGrp.length-1)
							  qryExp.append(",");
					}
					qryExp.append(") ");
				}

				if(!qryExp.toString().isEmpty()) {
					qb.appendToQuery(" ( "+qryExp+" ) ");
				}
			
			qb.addSecondarySort("submission_batch_id");
			qb.build();
			return qb.getMappedPagedList();
		} finally {
			DataBaseUtil.closeConnections(con, null);
		}
	}

	private static final String SUBMISSION_BATCH_DETAILS =
		" SELECT * from ( " +
		"		SELECT submission_batch_id,created_date,reference_number,submission_date," +
		"		insurance_co_id,file_name,tpa_id," +
		"		patient_type, is_resubmission, status,username, textcat_commacat(category_name) AS category_name, " +
		"		tpa_name,plan_id,plan_name,insurance_co_name,account_group, " +
		"		account_group_name,account_group_service_reg_no,center_name,hospital_center_service_reg_no  " +
		"	FROM  " +
		"	 (SELECT isb.submission_batch_id, created_date, reference_number, " +
		"	 submission_date, isb.insurance_co_id, file_name, isb.tpa_id, instab.insurance_category_id, " +
		"	 patient_type, is_resubmission, isb.status,isb.username, cat.category_name, " +
		"	 tp.tpa_name,isb.plan_id,ipm.plan_name, icm.insurance_co_name," +
		"	 isb.account_group,ag.account_group_name, ag.account_group_service_reg_no, " +
		"	 hc.center_name, hc.hospital_center_service_reg_no " +
		"    FROM insurance_submission_batch isb " +
		"	 	LEFT JOIN tpa_master tp ON(tp.tpa_id = isb.tpa_id) " +
		"	 	LEFT JOIN insurance_company_master icm ON(icm.insurance_co_id = isb.insurance_co_id) " +
		"	 	LEFT JOIN (SELECT submission_batch_id, " +
		"			 regexp_split_to_table(insurance_category_id, E'\\,')::TEXT AS insurance_category_id " +
		"			 FROM insurance_submission_batch ) AS instab " +
		"			 ON (instab.submission_batch_id = isb.submission_batch_id) " +
		"	 	LEFT JOIN insurance_category_master cat ON (cat.category_id::text = instab.insurance_category_id) " +
		"	 	LEFT JOIN insurance_plan_main ipm ON(ipm.plan_id = isb.plan_id) " +
		"	 	LEFT JOIN account_group_master ag ON(ag.account_group_id = isb.account_group) " +
		"		LEFT JOIN hospital_center_master hc ON(hc.center_id = isb.center_id) " +
		"	 ) as foo " +
		"	 GROUP BY submission_batch_id, created_date,reference_number, submission_date, " +
		"	 	insurance_co_id, file_name, tpa_id, patient_type, is_resubmission, " +
		"	 	status,username,tpa_name,plan_id,plan_name, " +
		"	 	insurance_co_name,account_group,account_group_name," +
		"		account_group_service_reg_no,center_name,hospital_center_service_reg_no " +
		"	) AS foo1 " +
		" WHERE submission_batch_id = ? ";


	public BasicDynaBean findSubmissionBatch(String submissionBatchId) throws SQLException {
		return DataBaseUtil.queryToDynaBean(SUBMISSION_BATCH_DETAILS, submissionBatchId);
	}


	private static final String GET_CLAIM_BILLS_OPEN = "SELECT b.visit_id, b.bill_no, b.status from bill b " +
		" JOIN bill_claim bcl ON (b.bill_no = bcl.bill_no) " +
		" JOIN insurance_claim ic ON (ic.claim_id = bcl.claim_id) " +
		" JOIN insurance_submission_batch isb ON (isb.submission_batch_id = ic.last_submission_batch_id) " +
		" WHERE isb.submission_batch_id = ? AND b.status = 'A'";

	public List getOpenClaimBills(String submissionBatchId) throws SQLException {
		return DataBaseUtil.queryToDynaList(GET_CLAIM_BILLS_OPEN, submissionBatchId);
	}

	private static final String GET_HAAD_CLAIMS_COUNT =" SELECT sum(count) AS count FROM "
			+ "( SELECT count(ic.claim_id) "
			+ " FROM insurance_claim ic "
			+ " JOIN insurance_submission_batch isb ON (isb.submission_batch_id = ic.last_submission_batch_id) "
			+ " WHERE isb.submission_batch_id = ?) AS foo";

	private static final String GET_HAAD_CLAIMS_COUNT_RESUBMIT ="SELECT sum(count) AS count FROM "
			+ " ( SELECT count(isr.claim_id) "
			+ " FROM insurance_claim_resubmission isr "
			+ " JOIN insurance_submission_batch isb ON (isb.submission_batch_id = isr.resubmission_batch_id) "
			+ " WHERE isb.submission_batch_id = ?) AS foo ";

	public static int getHaadXmlClaimsCount(String submissionBatchId, String isResubmission)throws SQLException {
		PreparedStatement ps = null;
		int count=0;
		Connection con = DataBaseUtil.getReadOnlyConnection();
		try {
			if (isResubmission.equals("Y"))
				ps = con.prepareStatement(GET_HAAD_CLAIMS_COUNT_RESUBMIT);
			else
				ps = con.prepareStatement(GET_HAAD_CLAIMS_COUNT);
			ps.setString(1, submissionBatchId);
			count = DataBaseUtil.getIntValueFromDb(ps);
		}finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return count;
	}

	private static final String GET_HAAD_ACCOUNT_XML_HEADER =  "SELECT account_group_id, account_group_name, " +
		" account_group_service_reg_no AS provider_id, '' AS eclaim_xml_schema, " +
		" (SELECT COALESCE (tpa_code, '@'||tpa_name) FROM tpa_master tm" +
		"	LEFT JOIN ha_tpa_code hta ON(hta.tpa_id=tm.tpa_id AND health_authority = ?) WHERE tm.tpa_id= ?) " +
		" AS receiver_id, to_char(current_timestamp::timestamp, 'dd/MM/yyyy hh24:mi') AS todays_date, " +
		" 0 as claims_count, 'Y' AS testing, '' AS from_date, '' AS to_date, '' AS operation, '' AS disposition_flag "+
		" FROM account_group_master WHERE account_group_id = " +
		"	(SELECT account_group FROM insurance_submission_batch WHERE submission_batch_id = ?) ";

	private static final String GET_HAAD_HOSPITAL_CENTER_XML_HEADER =  "SELECT center_id, center_name, " +
		" hospital_center_service_reg_no AS provider_id, '' AS eclaim_xml_schema, " +
		" (SELECT COALESCE (tpa_code, '@'||tpa_name) FROM tpa_master tm " +
		"	LEFT JOIN ha_tpa_code hta ON(hta.tpa_id=tm.tpa_id AND health_authority = ?) WHERE tm.tpa_id= ?) " +
		" AS receiver_id, to_char(current_timestamp::timestamp, 'dd/MM/yyyy hh24:mi') AS todays_date, " +
		" 0 as claims_count, 'Y' AS testing, '' AS from_date, '' AS to_date, '' AS operation, '' AS disposition_flag "+
		" FROM hospital_center_master WHERE center_id = " +
		"	(SELECT center_id FROM insurance_submission_batch WHERE submission_batch_id = ?) ";

	/**
	 *
	 * @param submissionBatchId
	 * @param tpa_id
	 * @return
	 * @throws SQLException
	 *
	 * If submission batch is created for a center then center's hospital_center_service_reg_no is considered as provider id.
	 * If submission batch is created for a account group then account's account_group_service_reg_no is considered as provider id.
	 */

	public BasicDynaBean getHaadXmlHeaderFields(String submissionBatchId, String tpaId,String healthAuthority) throws SQLException{
		BasicDynaBean submissionbean = new ClaimSubmissionDAO().findByKey("submission_batch_id", submissionBatchId);

		if (submissionbean != null) {
			if (submissionbean.get("account_group") != null && ((Integer)submissionbean.get("account_group")).intValue() != 0)
				return DataBaseUtil.queryToDynaBean(GET_HAAD_ACCOUNT_XML_HEADER, new Object[] {healthAuthority,tpaId, submissionBatchId});
			else if (submissionbean.get("center_id") != null)
				return DataBaseUtil.queryToDynaBean(GET_HAAD_HOSPITAL_CENTER_XML_HEADER, new Object[] {healthAuthority,tpaId, submissionBatchId});
		}
		return null;
	}
	
	private static final String FIND_CLAIMS_FIELDS = " SELECT ic.claim_id, isb.submission_batch_id, ic.submission_id_with_correction, ic.last_submission_batch_id, " +
		" ppd.member_id, ppd.policy_number, pip.patient_policy_id, pip.plan_id, pr.category_id, pip.priority, " +
		" pip.sponsor_id, COALESCE (hit.tpa_code, '@'||tm.tpa_name) AS receiver_id, tm.tpa_name, " +
		" pip.insurance_co, icm.insurance_co_name, ppd.eligibility_reference_number, " +
		" COALESCE (hic.insurance_co_code,'@'||icm.insurance_co_name) AS payer_id, ic.payers_reference_no, " +
		" agm.account_group_id, agm.account_group_name, ic.account_group as claim_account_group, " +
		" CASE WHEN isb.account_group != 0 THEN agm.account_group_service_reg_no " +
		"	ELSE hcm.hospital_center_service_reg_no END AS provider_id, " +
		" CASE WHEN pd.government_identifier IS NULL OR pd.government_identifier = '' THEN " +
		" COALESCE(gim.identifier_type,'') ELSE  pd.government_identifier END AS emirates_id_number, " +
		" SUM(total_amount) AS gross, " +
		" SUM(total_amount-(total_claim + total_claim_return)) AS patient_share, " +
		" SUM(total_claim + total_claim_return) AS net, " +
		" SUM(insurance_deduction) AS deduction, pip.use_drg, pip.use_perdiem, " +
		" agm.account_group_service_reg_no AS facility_id, " +
		" pr.encounter_type, pr.encounter_start_type, pr.encounter_end_type, " +
		" pr.mr_no AS patient_id, ic.main_visit_id, ic.patient_id AS claim_patient_id, pr.visit_type, " +
		" trim(COALESCE(pip.prior_auth_id,'')) AS prior_auth_id, " +
		" to_char((pr.reg_date||' '||pr.reg_time) :: timestamp without time zone, 'dd/MM/yyyy hh24:mi') as start_date," +
		" to_char((pr.discharge_date||' '||pr.discharge_time) :: timestamp without time zone, 'dd/MM/yyyy hh24:mi') as end_date," +
		" to_char(current_timestamp::timestamp, 'dd-MM-yyyy hh24:mi') AS todays_date, " +
		" icm.insurance_co_id, isb.is_resubmission, ic.resubmission_type, ic.comments," +
		" etc.encounter_type_desc, est.code_desc AS encounter_start_type_desc, eet.code_desc AS encounter_end_type_desc," +
		" tsrc.transfer_hospital_service_regn_no AS source_service_regn_no, " +
		" tdest.transfer_hospital_service_regn_no AS destination_service_regn_no, " +
		" pd.patient_name, pd.middle_name, pd.last_name, pd.patient_gender, pd.salutation, " +
		" pd.custom_list1_value AS nationality, " +
		" to_char(coalesce(pd.expected_dob, pd.dateofbirth), 'dd/MM/yyyy') AS date_of_birth," +
		" icam.category_name AS package_name, " +
		" CASE WHEN ppd.policy_validity_start IS NOT NULL " +
		"		THEN to_char(ppd.policy_validity_start, 'dd/MM/yyyy') ELSE '' END AS policy_validity_start, " +
		" CASE WHEN ppd.policy_validity_end IS NOT NULL " +
		"		THEN to_char(ppd.policy_validity_end, 'dd/MM/yyyy') ELSE '' END AS policy_validity_end, " +
		" pdd.doc_id, COALESCE(pdoc.content_type, '') AS card_type, " +
		" COALESCE(pdoc.original_extension, '') AS card_ext, " +
		" pdd.doc_name AS card_comment,isb.center_id as batch_center_id,ipm.require_pbm_authorization " ;

	private static final String FIND_CLAIMS_TABLES =
	    " FROM bill b " +
			" JOIN bill_claim bcl ON (b.bill_no = bcl.bill_no) " +
			" JOIN insurance_claim ic ON (ic.claim_id = bcl.claim_id)" +
			" JOIN insurance_submission_batch isb ON (isb.submission_batch_id = ic.last_submission_batch_id) " +
			" @ JOIN account_group_master agm ON (agm.account_group_id = isb.account_group) "+
			" % JOIN hospital_center_master hcm ON (hcm.center_id = isb.center_id) "+
			" JOIN patient_registration pr ON (pr.patient_id = ic.patient_id) " +
			" JOIN patient_insurance_plans pip ON (pr.patient_id = pip.patient_id and ic.plan_id = pip.plan_id) " +
			" JOIN patient_details pd ON (pd.mr_no = pr.mr_no) " +
			" # JOIN tpa_master tm ON (tm.tpa_id = pip.sponsor_id) " +
			" LEFT JOIN ha_tpa_code hit ON(hit.tpa_id = tm.tpa_id AND hit.health_authority = ?)" +
			" # JOIN insurance_company_master icm ON (icm.insurance_co_id = pip.insurance_co) " +
			" LEFT JOIN ha_ins_company_code hic ON(hic.insurance_co_id=icm.insurance_co_id AND hic.health_authority = ?)" +
			" LEFT JOIN insurance_plan_main ipm ON (ipm.plan_id = pip.plan_id)" +
			" LEFT JOIN insurance_category_master icam ON (icam.category_id = pr.category_id)" +
			" LEFT JOIN govt_identifier_master gim ON (gim.identifier_id = pd.identifier_id) "+
			" LEFT JOIN patient_policy_details ppd ON (ppd.patient_policy_id = pip.patient_policy_id) "+
			" LEFT JOIN plan_docs_details pdd ON ppd.patient_policy_id = pdd.patient_policy_id " +
			" LEFT JOIN patient_documents pdoc ON (pdoc.doc_id = pdd.doc_id)  " +
			" LEFT JOIN encounter_type_codes etc ON (etc.encounter_type_id = encounter_type) " +
			" LEFT JOIN encounter_start_types est ON (est.code = pr.encounter_start_type::text) " +
			" LEFT JOIN encounter_end_types eet ON (eet.code = pr.encounter_end_type::text)  "+
			" LEFT JOIN transfer_hospitals tsrc ON (tsrc.transfer_hospital_id = pr.transfer_source) " +
			" LEFT JOIN transfer_hospitals tdest ON (tdest.transfer_hospital_id = pr.transfer_destination)  " +
		" WHERE isb.submission_batch_id = ? AND is_tpa "+
		" GROUP BY ic.claim_id, isb.submission_batch_id, ic.last_submission_batch_id, ppd.member_id, ppd.policy_number, " +
		" hit.tpa_code, tm.tpa_name, hic.insurance_co_code, icm.insurance_co_name,ic.payers_reference_no, " +
		" isb.account_group, agm.account_group_id, ic.account_group, agm.account_group_name, agm.account_group_service_reg_no, " +
		" hcm.center_id, hcm.center_name, hcm.hospital_center_service_reg_no, pr.encounter_type, " +
		" pip.use_drg, pip.use_perdiem, pip.patient_policy_id,pr.mr_no,pr.op_type,pip.plan_id,pr.category_id,pip.priority, " +
		" pip.sponsor_id, pip.insurance_co, " +
		" pd.patient_name, pd.middle_name, pd.last_name, pd.patient_gender, pd.salutation, " +
		" pd.custom_list1_value,pd.expected_dob, pd.dateofbirth, " +
		" pr.patient_id,pr.visit_type,pip.prior_auth_id,pr.reg_date,pr.reg_time,pr.discharge_date,pr.discharge_time, " +
		" pr.encounter_start_type, pr.encounter_end_type, ppd.eligibility_reference_number, " +
		" tsrc.transfer_hospital_service_regn_no, tdest.transfer_hospital_service_regn_no," +
		" pd.government_identifier,gim.identifier_type, icm.insurance_co_id, ic.main_visit_id, ic.patient_id, " +
		" isb.is_resubmission, ic.resubmission_type, ic.comments," +
		" etc.encounter_type_desc, est.code_desc, eet.code_desc," +
		" ipm.plan_name, icam.category_name, ppd.policy_validity_start, ppd.policy_validity_end, " +
		" pdd.doc_id, pdoc.content_type, pdoc.original_extension, pdd.doc_name,isb.center_id,ipm.require_pbm_authorization ";

	private static final String FIND_CLAIMS_TABLES_RESUBMISSION =
	    " FROM bill b " +
			" JOIN bill_claim bcl ON (b.bill_no = bcl.bill_no) " +
			" JOIN insurance_claim ic ON (ic.claim_id = bcl.claim_id)" +
			" JOIN claim_submissions cs ON (ic.claim_id = cs.claim_id) " +
			" JOIN insurance_submission_batch isb ON (isb.submission_batch_id = cs.submission_batch_id) " +
			" @ JOIN account_group_master agm ON (agm.account_group_id = isb.account_group) "+
			" % JOIN hospital_center_master hcm ON (hcm.center_id = isb.center_id) "+
			" JOIN patient_registration pr ON (pr.patient_id = ic.patient_id) " +
			" JOIN patient_insurance_plans pip ON (pr.patient_id = pip.patient_id AND pip.plan_id = ic.plan_id) " +
			" JOIN patient_details pd ON (pd.mr_no = pr.mr_no) " +
			" # JOIN tpa_master tm ON (tm.tpa_id = pip.sponsor_id) " +
			" LEFT JOIN ha_tpa_code hit ON(hit.tpa_id = tm.tpa_id AND hit.health_authority = ?)" +
			" # JOIN insurance_company_master icm ON (icm.insurance_co_id = pip.insurance_co) " +
			" LEFT JOIN ha_ins_company_code hic ON(hic.insurance_co_id=icm.insurance_co_id AND hic.health_authority = ?)" +
			" LEFT JOIN insurance_plan_main ipm ON (ipm.plan_id = pip.plan_id)" +
			" LEFT JOIN insurance_category_master icam ON (icam.category_id = pr.category_id)" +
			" LEFT JOIN govt_identifier_master gim ON (gim.identifier_id = pd.identifier_id) "+
			" LEFT JOIN patient_policy_details ppd ON (ppd.patient_policy_id = pip.patient_policy_id) "+
			" LEFT JOIN plan_docs_details pdd ON ppd.patient_policy_id = pdd.patient_policy_id " +
			" LEFT JOIN patient_documents pdoc ON (pdoc.doc_id = pdd.doc_id)  " +
			" LEFT JOIN encounter_type_codes etc ON (etc.encounter_type_id = encounter_type) " +
			" LEFT JOIN encounter_start_types est ON (est.code = pr.encounter_start_type::text) " +
			" LEFT JOIN encounter_end_types eet ON (eet.code = pr.encounter_end_type::text)  " +
			" LEFT JOIN transfer_hospitals tsrc ON (tsrc.transfer_hospital_id = pr.transfer_source) " +
			" LEFT JOIN transfer_hospitals tdest ON (tdest.transfer_hospital_id = pr.transfer_destination)  " +
		" WHERE isb.submission_batch_id = ? AND is_tpa AND isb.is_resubmission = 'Y' "+
		" GROUP BY ic.claim_id, isb.submission_batch_id, ic.last_submission_batch_id, ppd.member_id, ppd.policy_number," +
		" hit.tpa_code, tm.tpa_name, hic.insurance_co_code, icm.insurance_co_name, ic.payers_reference_no,  " +
		" isb.account_group, agm.account_group_id, agm.account_group_name, agm.account_group_service_reg_no," +
		" hcm.center_id, hcm.center_name, hcm.hospital_center_service_reg_no, pr.encounter_type, " +
		" pip.use_drg, pip.use_perdiem, pip.patient_policy_id,pr.mr_no,pr.op_type,pip.plan_id,pr.category_id,pip.priority, " +
		" pip.sponsor_id, pip.insurance_co, " +
		" pd.patient_name, pd.middle_name, pd.last_name, pd.patient_gender, pd.salutation, " +
		" pd.custom_list1_value,pd.expected_dob, pd.dateofbirth, " +
		" pr.patient_id,pr.visit_type,pip.prior_auth_id,pr.reg_date,pr.reg_time,pr.discharge_date,pr.discharge_time, " +
		" pr.encounter_start_type, pr.encounter_end_type," +
		" tsrc.transfer_hospital_service_regn_no, tdest.transfer_hospital_service_regn_no," +
		" pd.government_identifier,gim.identifier_type, icm.insurance_co_id, ic.main_visit_id, ic.patient_id, " +
		" isb.is_resubmission, ic.resubmission_type, ic.comments, " +
		" etc.encounter_type_desc, est.code_desc, eet.code_desc," +
		" ipm.plan_name, icam.category_name, ppd.policy_validity_start, ppd.policy_validity_end, " +
		" pdd.doc_id, pdoc.content_type, pdoc.original_extension, pdd.doc_name,isb.center_id,ipm.require_pbm_authorization ";

	public List<BasicDynaBean> findAllClaims(String submissionBatchId, String isResubmission, String healthAuthority) throws SQLException {
		BasicDynaBean submissionbean = new ClaimSubmissionDAO().findByKey("submission_batch_id", submissionBatchId);
		String tpaId  = submissionbean.get("tpa_id") != null ? (String)submissionbean.get("tpa_id") : null;

		int accountGroup  = submissionbean.get("account_group") != null ? (Integer)submissionbean.get("account_group") : 0;

		String query = FIND_CLAIMS_FIELDS;

		if (isResubmission.equals("Y"))	query = query + FIND_CLAIMS_TABLES_RESUBMISSION;
		else query = query + FIND_CLAIMS_TABLES;

		if (tpaId == null || tpaId.trim().equals(""))	query = query.replaceAll(" # ", " LEFT ");
		else query = query.replaceAll(" # ", " ");

		if (accountGroup != 0) {
			query = query.replaceAll(" @ ", " ");
			query = query.replaceAll(" % ", " LEFT ");
		}else {
			query = query.replaceAll(" @ ", " LEFT ");
			query = query.replaceAll(" % ", " ");
		}

		return DataBaseUtil.queryToDynaList(query, new Object[] {healthAuthority, healthAuthority, submissionBatchId});
	}

	private static final String BATCH_FROM_TO_DATE =
		" SELECT to_char(min(reg_date), 'dd/MM/yyyy') AS from_date," +
		" to_char(max(reg_date), 'dd/MM/yyyy') AS to_date " +
		" FROM (SELECT reg_date " +
		" FROM patient_registration pr " +
		" JOIN bill b ON (b.visit_id = pr.patient_id AND is_tpa AND b.status != 'X')" +
		" JOIN bill_claim bcl ON (b.bill_no = bcl.bill_no) " +
		" JOIN claim_submission_batch_view isv ON (isv.claim_id = bcl.claim_id) " +
		" WHERE isv.submission_batch_id = ? ) AS foo ";

	public BasicDynaBean findBatchFromToDate(String submissionBatchId) throws SQLException {
		return DataBaseUtil.queryToDynaBean(BATCH_FROM_TO_DATE, submissionBatchId);
	}

	public List<BasicDynaBean> findAllCoderDiagnosis(String visitId) throws SQLException {
		return DataBaseUtil.queryToDynaList(MrdCodeRepository.FIND_CODER_DIAGNOSIS, visitId);
	}
	
	public List<BasicDynaBean> findAllDiagnosis(String visitId) throws SQLException {
    return DataBaseUtil.queryToDynaList(MrdCodeRepository.FIND_DIAGNOSIS, visitId);
  }

	private static final String FIND_BILLS = " SELECT b.bill_no,b.status,b.bill_type,b.restriction_type,b.primary_claim_status " +
	    " FROM bill b " +
			" JOIN bill_claim bclm ON(b.bill_no = bclm.bill_no)"+
			" WHERE bclm.claim_id = ? AND b.status != 'X' AND b.total_amount >= 0 ORDER BY b.bill_no ";

	public List<BasicDynaBean> findAllBills(String claimId) throws SQLException {
		return DataBaseUtil.queryToDynaList(FIND_BILLS, claimId);
	}

	private static final String FIND_OBSERVATIONS_WITHOUT_PRESENTING_COMPLAINT = "SELECT  " +
			"  value, value_type, observation_type AS type, code " +
			" FROM mrd_observations mo" +
			" WHERE mo.charge_id = ? AND code != 'Presenting-Complaint' AND (mo.sponsor_id = ? " +
			" OR mo.sponsor_id is NULL OR mo.sponsor_id='' ) ";

	private static final String FIND_OBSERVATIONS = "SELECT  " +
			"  value, value_type, observation_type AS type, code " +
			" FROM mrd_observations mo" +
			" WHERE mo.charge_id = ? AND (mo.sponsor_id = ? OR mo.sponsor_id is NULL OR mo.sponsor_id='' ) ";

	public List<BasicDynaBean> findAllObservations(String chargeId, String sponsorId, String healthAuthority) throws SQLException {
		if (!healthAuthority.equals("DHA")) {
			return DataBaseUtil.queryToDynaList(FIND_OBSERVATIONS_WITHOUT_PRESENTING_COMPLAINT, chargeId,sponsorId);
		}else {
			return DataBaseUtil.queryToDynaList(FIND_OBSERVATIONS, chargeId,sponsorId);
		}
	}
	private static final String FIND_CLAIM_OBSERVATIONS_WITHOUT_PRESENTING_COMPLAINT = "SELECT  " +
			"  value, value_type, observation_type AS type, code, mo.charge_id, mo.document_id, pd.doc_content_bytea as file_bytes" +
			" FROM mrd_observations mo" +
			" JOIN bill_charge bc ON (mo.charge_id= bc.charge_id) " +
			" JOIN bill_claim bcl ON (bc.bill_no = bcl.bill_no  AND bcl.claim_id = ? ) " +
			" LEFT JOIN patient_documents pd ON (pd.doc_id = mo.document_id) " +
			" WHERE  code != 'Presenting-Complaint' AND (mo.sponsor_id = ? " +
			" OR mo.sponsor_id is NULL OR mo.sponsor_id='' ) ";

	private static final String FIND_CLAIM_OBSERVATIONS = "SELECT  " +
			"  value, value_type, observation_type AS type, code, mo.charge_id, mo.document_id, pd.doc_content_bytea as file_bytes " +
			" FROM mrd_observations mo" +
			" JOIN bill_charge bc ON (mo.charge_id= bc.charge_id) " +
			" JOIN bill_claim bcl ON (bc.bill_no = bcl.bill_no  AND bcl.claim_id = ? ) " +
			" LEFT JOIN patient_documents pd ON (pd.doc_id = mo.document_id) " +
			" WHERE (mo.sponsor_id = ? OR mo.sponsor_id is NULL OR mo.sponsor_id='' ) ";
	public List<BasicDynaBean> findAllClaimObservations(String claimId,String sponsorId, String healthAuthority) throws SQLException {
		if (!healthAuthority.equals("DHA")) {
			return DataBaseUtil.queryToDynaList(FIND_CLAIM_OBSERVATIONS_WITHOUT_PRESENTING_COMPLAINT, claimId,sponsorId);
		}else {
			return DataBaseUtil.queryToDynaList(FIND_CLAIM_OBSERVATIONS, claimId,sponsorId);
		}
	}

	public void getAllSupportingDocAttachments(String claimPatientId,
			List<BasicDynaBean> supportedAttachments) throws Exception {

		getDischargeSummaryReport(claimPatientId, supportedAttachments);
		getDoctorConsultationReport(claimPatientId, supportedAttachments);
	}

	private static final String GET_PDF_DISCHARGE_SUMMARY_DOCS =
		" SELECT 'F' as format, docid, form_caption as name, '' as content_type, access_rights, username,	pdis.discharge_finalized_user, " +
		"	pdis.discharge_date, pdis.discharge_format, pdis.discharge_doc_id,  pdis.patient_id, " +
		"   doc.doctor_name, form_caption AS doc_name, 'PDF' AS doc_type, '' AS doc_comment, '' AS content " +
		" FROM patient_registration pdis " +
		"	JOIN dis_header dh ON (pdis.discharge_doc_id=dh.docid AND pdis.discharge_format='F') " +
		"	JOIN form_header fh using (form_id) " +
		" LEFT JOIN doctors doc ON (doc.doctor_id = pdis.discharge_doctor_id) " +
		" WHERE pdis.patient_id=? " +
		" UNION " +
		" SELECT 'T', docid, case when template_title is null or template_title ='' then template_caption else template_title end, " +
		"	'' as content_type, access_rights, username, pdis.discharge_finalized_user, pdis.discharge_date, pdis.discharge_format, " +
		"	pdis.discharge_doc_id,  pdis.patient_id, doc.doctor_name," +
		"   case when template_title is null or template_title ='' then template_caption else template_title end AS doc_name, 'PDF' AS doc_type, '' AS doc_comment, '' AS content  " +
		" FROM patient_registration pdis " +
		"	JOIN discharge_format_detail dfd ON (pdis.discharge_doc_id=dfd.docid AND pdis.discharge_format='T') " +
		"	JOIN discharge_format df using(format_id) " +
		" LEFT JOIN doctors doc ON (doc.doctor_id = pdis.discharge_doctor_id) " +
		" WHERE pdis.patient_id=? " +
		" UNION " +
		" SELECT 'P', pd.doc_id, template_name, '' as content_type, access_rights, null AS username, " +
		"	pdis.discharge_finalized_user, pdis.discharge_date, pdis.discharge_format, pdis.discharge_doc_id,  pdis.patient_id, " +
		"	doc.doctor_name, template_name AS doc_name, 'PDF' AS doc_type, '' AS doc_comment, '' AS content " +
		" FROM patient_registration pdis " +
		"	JOIN patient_documents pd  ON (pdis.discharge_doc_id=pd.doc_id and pdis.discharge_format='P') " +
		"	JOIN doc_pdf_form_templates USING (template_id) " +
		" LEFT JOIN doctors doc ON (doc.doctor_id = pdis.discharge_doctor_id) " +
		" WHERE pdis.patient_id=?";

	public void getDischargeSummaryReport(String claimPatientId,
			List<BasicDynaBean> supportedAttachments) throws SQLException,IOException, TransformerException, XPathExpressionException, DocumentException, TemplateException {
		DischargeSummaryReportHelper reportHelper = new DischargeSummaryReportHelper();
		List<BasicDynaBean> disSummaryDocList = DataBaseUtil.queryToDynaList(GET_PDF_DISCHARGE_SUMMARY_DOCS, new String[]{claimPatientId, claimPatientId, claimPatientId});
		Preferences sessionPrefs = (Preferences)RequestContext.getSession().getAttribute("preferences");

		BasicDynaBean prefs = PrintConfigurationsDAO.getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_DISCHARGE, 0);

		if (disSummaryDocList != null && !disSummaryDocList.isEmpty()) {
			for (BasicDynaBean disbean : disSummaryDocList) {
				int docId = (Integer)disbean.get("docid");
				String format = (String)disbean.get("format");
				disbean.set("doc_comment", "Discharge Summary Report");

				DischargeSummaryReportHelper.FormatType formatType = format.equals("F") ?
						DischargeSummaryReportHelper.FormatType.HVF :
						(format.equals("T") ? DischargeSummaryReportHelper.FormatType.RICH_TEXT : DischargeSummaryReportHelper.FormatType.PDF);

				byte[] pdfbytes = reportHelper.getDischargeSummaryReport(docId, formatType,
					DischargeSummaryReportHelper.ReturnType.PDF_BYTES, prefs, sessionPrefs, null);

				if (pdfbytes != null) {
					InputStream file = new ByteArrayInputStream(pdfbytes);
					if (file != null) {
						String attachment = convertToBase64Binary(file);
						if (attachment != null) {
							disbean.set("content", attachment);
							supportedAttachments.add(disbean);
						}
					}
				}
			}
		}
	}

	private static final String GET_DOCTOR_CONSULTATION =
		" SELECT '' AS doc_name, 'PDF' AS doc_type, " +
		"   '' AS doc_comment, '' AS content " +
		" FROM doctor_consultation dc WHERE consultation_id = ? ";

	public void getDoctorConsultationReport(String claimPatientId,
			List<BasicDynaBean> supportedAttachments) throws SQLException, XPathExpressionException, DocumentException, TemplateException,
	  IOException, TransformerException, ParseException {
		OPPrescriptionFtlHelper ftlHelper = new OPPrescriptionFtlHelper();

		int consultationId = new DoctorConsultationDAO().getAdmittingDoctorConsultationId(claimPatientId);
		BasicDynaBean consultationBean = new DoctorConsultationDAO().findByKey("consultation_id", consultationId);
		if (consultationBean == null ||
				(consultationBean.get("cancel_status") != null && (consultationBean.get("cancel_status")).equals("C"))){
		  return;
		}

		String templateName = (String) GenericPreferencesDAO.getAllPrefs().get("default_consultation_print_template");
		BasicDynaBean consBean = DataBaseUtil.queryToDynaBean(GET_DOCTOR_CONSULTATION, consultationId);
		HttpSession session = RequestContext.getSession();

		consBean.set("doc_name", templateName);
		consBean.set("doc_comment", "Doctor Notes");

		int printerId = 0;
		BasicDynaBean prefs = PrintConfigurationsDAO.getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_PATIENT, printerId);
		String printMode = "P";

		if (printMode.equals("P")) {
			String allFields = "Y";
			String userName = (String)session.getAttribute("userid");

			OPPrescriptionFtlHelper.DefaultType templateType = OPPrescriptionFtlHelper.DefaultType.CONSULTATION;

			byte[] pdfbytes = ftlHelper.getConsultationFtlReport(consultationId, templateName,
					OPPrescriptionFtlHelper.ReturnType.PDF_BYTES, prefs, allFields.equals("Y"), null, userName, templateType);

			if (pdfbytes != null) {
				InputStream file = new ByteArrayInputStream(pdfbytes);
				String attachment = convertToBase64Binary(file);
				if (attachment != null) {
					consBean.set("content", attachment);
					supportedAttachments.add(consBean);
				}
			}
		}
	}

	private static final String GET_TEST_REPORT_ID =
		" SELECT report_id, bc.charge_head, " +
		"  '' AS doc_name, 'PDF' AS doc_type, '' AS doc_comment, '' AS content " +
		" FROM tests_prescribed tp " +
		" JOIN bill_activity_charge bac ON " +
		" (bac.activity_id = tp.prescribed_id::varchar) AND bac.activity_code='DIA' " +
		" JOIN bill_charge bc ON bac.charge_id=bc.charge_id " +
		" WHERE bc.charge_id = ? " ;

	public void getTestReportDocAttachments(String chargeId, List<BasicDynaBean> supportedAttachments)
				throws  Exception {
		BasicDynaBean testReportBean = DataBaseUtil.queryToDynaBean(GET_TEST_REPORT_ID, chargeId);

		if (testReportBean != null) {
			int reportId = (testReportBean.get("report_id") != null) ? (Integer)testReportBean.get("report_id") : 0;
			String chargeHead = (testReportBean.get("charge_head") != null) ? (String)testReportBean.get("charge_head") : null;
			String category = null;
			if(chargeHead != null){
			  category = chargeHead.equals("LTDIA") ? "DEP_LAB" : "DEP_RAD";
			}
			if (reportId != 0) {
				BasicDynaBean report = DiagnosticsDAO.getReportDynaBean(reportId);
				String reportName = (String) report.get("report_name");
				testReportBean.set("doc_name", reportName);
				if (null != category && category.equals("DEP_LAB"))
					testReportBean.set("doc_comment", "Laboratory Report");
				else
					testReportBean.set("doc_comment", "Radiology Report");

				InputStream file = new DiagnosticsDAO().getDiagReportPDFStream(report, category);
				if (file != null) {
					String attachment = convertToBase64Binary(file);
					if (attachment != null) {
						testReportBean.set("content", attachment);
						supportedAttachments.add(testReportBean);
					}
				}
			}
		}
	}

  public String convertToBase64Binary(InputStream in) throws IOException {
    String encodedStr = null;
    byte[] bytes = new byte[4096];

    ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    while (true) {
      int r = in.read(bytes);
      if (r <= 0) {
        break;
      }
      buffer.write(bytes, 0, r);
    }
    byte[] filebytes = buffer.toByteArray();

    //all chars in encoded are guaranteed to be 7-bit ASCII
    byte[] encoded = Base64.encodeBase64(filebytes);
    encodedStr = new String(encoded, "ASCII");

    return encodedStr;
  }

	public List<BasicDynaBean> getAllBills(Connection con, String claimID) throws SQLException {
		ResultSet rs = null;
		List<BasicDynaBean> l;
		try(PreparedStatement ps = con.prepareStatement(FIND_BILLS)){
		  
	    ps.setString(1, claimID);
	    rs = ps.executeQuery();
	    RowSetDynaClass rsd = new RowSetDynaClass(rs);
	    l = rsd.getRows();
		}finally{
		  if (rs != null) {
		    rs.close();
		  }
		}
		return l;
	}

	private static final String GET_LATEST_SUBMISSION_BATCH_ID = "SELECT * FROM claim_submission_batch_view WHERE claim_id = ? AND is_resubmission = ? ";

	public BasicDynaBean getLatestSubmissionBatch(String claimId, String isResubmission) throws SQLException{
		return DataBaseUtil.queryToDynaBean(GET_LATEST_SUBMISSION_BATCH_ID, new Object[] {claimId, isResubmission});
	}

	/*
	 * TODO - Kindly change usage of the following scenarios as you migrate
	 * or work on the specific scenarios to use UrlUtil's build url as used in
	 * the case when type is account group.
	 */
	public String urlString(String path, String type, String id, String name, HashMap actionUrlMap) throws SQLException {

		String url = "";
		path = path +"/";
		String targetStr = "<b><a target='_blank' href='";
		String endStr = "</a></b>";

		if (type.equals("diagnosis") || type.equals("drg")) {
				url = (String)actionUrlMap.get("update_mrd");
				url = targetStr+path + url+"?_method=getMRDUpdateScreen&patient_id="+id+"'>"+id+endStr;
		}else if (type.equals("bill")) {
				url = (String)actionUrlMap.get("credit_bill_collection");
				url = targetStr+path + url+"?_method=getCreditBillingCollectScreen&billNo="+id+"'>"+id+endStr;
		}else if (type.equals("claim")) {
				url = (String)actionUrlMap.get("insurance_claim_reconciliation");
				url = targetStr+path+url+"?_method=getClaimBillsActivities&claim_id="+id+"'>"+id+endStr;
		}else if (type.equals("attachment")) {
				url = (String)actionUrlMap.get("insurance_claim_reconciliation");
				url = targetStr+path+url+"?_method=addOrEditAttachment&claim_id="+id+"'>"+id+endStr;
		}else if (type.equals("doctor")) {
				url = (String)actionUrlMap.get("mas_doctors_detail");
				url = targetStr+path+url+"?_method=getDoctorDetailsScreen&mode=update&doctor_id="+id+"'>"+name+endStr;
		}else if (type.equals("referral")) {
				url = (String)actionUrlMap.get("mas_ref_doctors");
				url = targetStr+path+url+"?_method=show&referal_no="+id+"'>"+name+endStr;
		}else if (type.equals("patient")) {
				url = (String)actionUrlMap.get("edit_visit_details");
				url = targetStr+path+url+"?_method=getPatientVisitDetails&ps_status=all&patient_id="+id+"'>"+id+endStr;
		}else if (type.equals("account-group")) {
				url = UrlUtil.buildURL("accounting_group_master", UrlUtil.SHOW_URL_VALUE, "account_group_id="+id, null, id);
				url = targetStr+url+"'>"+name+" Group</a></b>";
		}else if (type.equals("center-name")) {
				url = UrlUtil.buildURL("mas_centers", UrlUtil.SHOW_URL_VALUE, "center_id="+id, null, id);
				url = targetStr+url+"'>"+name+" Center</a></b>";
		}else if (type.equals("submission")) {
				url = (String)actionUrlMap.get("insurance_claim_reconciliation");
				url = targetStr+path+url+"?_method=list&status=&submission_batch_id="+id+"'>"+id+endStr;
		}else if (type.equals("pre-registration")) {
				url = (String)actionUrlMap.get("reg_general");
				url = targetStr+path+url+"?_method=show&regType=regd&mr_no="+id+"&mrno="+id+"'>"+id+endStr;
		}else if (type.equals("drug")) {
				url = (String)actionUrlMap.get("pharma_sale_edit_bill");
				url = targetStr+path+url+"?_method=getSaleDetails&sale_item_id="+id+"'>"+name+endStr;
		}else if(type.equals("adt")) {
				url = (String)actionUrlMap.get("adt");
				url = targetStr+path
					+ url+"?_method=getADTScreen&_searchMethod=getADTScreen&mr_no%40op=ilike&_actionId=adt&mr_no="+id+"'>"+id+endStr;
		}else if (type.equals("bill-remittance")) {
				url = (String)actionUrlMap.get("bill_remittance");
				url = targetStr+path + url+"?_method=getBillRemittance&billNo="+id+"'>"+name+endStr;
		}else if (type.equals("ins-remittance")) {
				url = (String)actionUrlMap.get("ins_remittance_xl");
				url = targetStr+path + url+"?_method=show&remittance_id="+id+"'>"+name+endStr;
		}else if (type.equals("insurance")) {
				url = (String)actionUrlMap.get("change_visit_tpa");
				url = targetStr+path + url+"?_method=changeTpa&visitId="+id+"'>"+id+endStr;
		}else if (type.equals("sponsor")) {
				url = (String)actionUrlMap.get("mas_ins_tpas");
				url = targetStr+path+url+"?_method=show&tpa_id="+id+"'>"+name+endStr;
		}else if (type.equals("company")) {
				url = (String)actionUrlMap.get("mas_insurance_comp");
				url = targetStr+path+url+"?_method=show&insurance_co_id="+id+"'>"+name+endStr;
		}
		return url;
	}
	public String urlString(String path, String type, String id, String name) throws SQLException {
		HttpSession session = RequestContext.getSession();
		java.util.HashMap actionUrlMap = (java.util.HashMap) session.getServletContext().getAttribute("actionUrlMap");
		
		return urlString( path, type, id, name, actionUrlMap);
	}

	public String getValidCardType(String cardType, String cardExt) {
	  cardType = cardType == null ? "" : cardType.trim().toLowerCase();
	  cardExt  = cardExt == null ? "" : cardExt.trim().toUpperCase();

		Map<String, String> contentExtTypes = new HashMap<>();
		contentExtTypes.put("image/gif", "GIF");

		contentExtTypes.put("image/jpeg", "JPEG");
		contentExtTypes.put("image/jpg", "JPG");

		contentExtTypes.put("application/pdf", "PDF");

		contentExtTypes.put("image/png", "PNG");

		contentExtTypes.put("application/rtf", "RTF");

		contentExtTypes.put("text/plain", "TXT");

		contentExtTypes.put("image/tiff", "TIFF");

		if (!cardType.equals("") && contentExtTypes.containsKey(cardType)) {

			return contentExtTypes.get(cardType);

			// Valid card type.
		}else if (!cardExt.equals("")
					&& (cardExt.equalsIgnoreCase("GIF") || cardExt.equalsIgnoreCase("JPEG")
						|| cardExt.equalsIgnoreCase("JPG") || cardExt.equalsIgnoreCase("PNG")
						|| cardExt.equalsIgnoreCase("PDF") || cardExt.equalsIgnoreCase("RTF")
						|| cardExt.equalsIgnoreCase("TXT")	|| cardExt.equalsIgnoreCase("TIFF"))) {
			// Valid card extension.
			return cardExt;
		}
		return null;
	}

	public List<BasicDynaBean> getPackageDocs(String visitId)throws SQLException{

		Connection con = null;
		PreparedStatement ps = null;

		try{
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement("SELECT pdm.doc_type_id, foo.doc_type, dt.doc_type_name,foo.doc_id FROM pack_doc_master pdm  " +
					" JOIN doc_type dt ON (dt.doc_type_id=pdm.doc_type_id) " +
					" JOIN package_prescribed pp ON (pp.patient_id=? AND pdm.pack_id=pp.package_id)" +
					" LEFT JOIN (SELECT pd.doc_type,pd.doc_id FROM patient_general_docs pgd JOIN patient_documents pd " +
					"			USING (doc_id) where patient_id=? group by doc_type,pd.doc_id) foo ON (foo.doc_type=pdm.doc_type_id) " +
					" WHERE pp.status!='X' " +
					" GROUP BY pdm.doc_type_id, foo.doc_type, dt.doc_type_name,foo.doc_id");
			ps.setString(1, visitId);
			ps.setString(2, visitId);
			return DataBaseUtil.queryToDynaList(ps);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
	}


	private static final String UPDATE_BILL_CHARGE_CLAIM_ACTIVITY_ID = " UPDATE bill_charge_claim bcc " +
			" SET claim_activity_id = charge_id "+
			" FROM insurance_claim ic "+
			" WHERE ic.claim_id = bcc.claim_id AND ic.last_submission_batch_id = ? ";

	private static final String UPDATE_BILL_CHAGRE_CLAIM_ACTIVITY_ID_RESUBMISSION = " UPDATE bill_charge_claim bcc " +
			" SET claim_activity_id = charge_id "+
			" FROM insurance_claim ic "+
			" JOIN claim_submissions cs ON(cs.claim_id = ic.claim_id) "+
			" JOIN insurance_submission_batch isb ON(isb.submission_batch_id = cs.submission_batch_id) "+
			" WHERE ic.claim_id = bcc.claim_id AND isb.submission_batch_id = ? AND isb.is_resubmission = 'Y' ";
	
	private static final String UPDATE_BC_REPEATING_ITEMS_CLAIM_ACTIVITY_ID = " UPDATE bill_charge_claim bcc  "+
			" SET claim_activity_id = 'ACT'||'-'|| min_charge_id "+
			" FROM (SELECT * FROM "+ 
						"(SELECT ic.last_submission_batch_id,bcc.claim_id, bcc.charge_id, min(bcc.charge_id) OVER (PARTITION BY "+			
							" bcc.claim_id,act_rate_plan_item_code, posted_date::date,conducted_datetime::date,ic.last_submission_batch_id, "+
							" bc.code_type,coalesce(bcc.prior_auth_id, pip.prior_auth_id, ''), aac.alternate_code, mccg.code_group) AS min_charge_id, "+
							" act_rate_plan_item_code, count(bcc.charge_id) OVER (PARTITION BY bcc.claim_id,act_rate_plan_item_code,posted_date::date,conducted_datetime::date,"+
							" ic.last_submission_batch_id,bc.code_type, coalesce(bcc.prior_auth_id, pip.prior_auth_id, ''), aac.alternate_code, "+
							" mccg.code_group) AS no_of_items "+
						" FROM bill_charge_claim bcc "+
						" JOIN bill_charge bc ON(bc.charge_id=bcc.charge_id AND bc.submission_batch_type IS NULL) "+
						" JOIN insurance_claim ic ON(bcc.claim_id = ic.claim_id) "+
						" JOIN patient_insurance_plans pip ON(pip.patient_id = ic.patient_id and pip.plan_id = ic.plan_id "+
						" and pip.sponsor_id = bcc.sponsor_id) "+
						" LEFT JOIN mrd_codes_master mcm ON(mcm.code = bc.act_rate_plan_item_code AND mcm.code_type = bc.code_type) "+
						" LEFT JOIN mrd_code_claim_groups mccg ON(mccg.mrd_code_id = mcm.mrd_code_id)"+
						" LEFT JOIN alternate_activity_codes  aac ON(aac.item_id = bc.act_description_id and aac.item_code = bc.act_rate_plan_item_code "+
							" and aac.code_type = bc.code_type and (aac.sponsor_id = bcc.sponsor_id OR aac.sponsor_id is null)) "+
						" WHERE bc.act_rate_plan_item_code is not null AND bc.act_rate_plan_item_code != ''  "+
						" AND (mccg.code_group = 'LG' OR mccg.code_group is null) "+
						" AND last_submission_batch_id = ?) cav1 "+
						" WHERE no_of_items > 1) cav  "+
			" WHERE cav.claim_id = bcc.claim_id AND cav.no_of_items > 1 AND cav.charge_id = bcc.charge_id  AND cav.last_submission_batch_id=? ";
	
	private static final String UPDATE_UNLISTED_BC_REPEATING_ITEMS_CLAIM_ACTIVITY_ID = " UPDATE bill_charge_claim bcc  "+
			" SET claim_activity_id = 'ACT'||'-'|| min_charge_id "+
			" FROM (SELECT * FROM "+ 
						"(SELECT ic.last_submission_batch_id,bcc.claim_id, bcc.charge_id, min(bcc.charge_id) OVER (PARTITION BY "+			
							" bcc.claim_id,act_rate_plan_item_code, posted_date::date,conducted_datetime::date,ic.last_submission_batch_id, "+
							" bc.code_type,coalesce(bcc.prior_auth_id, pip.prior_auth_id, ''), bc.act_description_id, aac.alternate_code, mccg.code_group) AS min_charge_id, "+
							" act_rate_plan_item_code, count(bcc.charge_id) OVER (PARTITION BY bcc.claim_id,act_rate_plan_item_code,posted_date::date,conducted_datetime::date,"+
							" ic.last_submission_batch_id,bc.code_type, coalesce(bcc.prior_auth_id, pip.prior_auth_id, ''), bc.act_description_id, aac.alternate_code, "+
							" mccg.code_group) AS no_of_items "+
						" FROM bill_charge_claim bcc "+
						" JOIN bill_charge bc ON(bc.charge_id=bcc.charge_id AND bc.submission_batch_type IS NULL) "+
						" JOIN insurance_claim ic ON(bcc.claim_id = ic.claim_id) "+
						" JOIN patient_insurance_plans pip ON(pip.patient_id = ic.patient_id and pip.plan_id = ic.plan_id "+
						" and pip.sponsor_id = bcc.sponsor_id) "+
						" JOIN mrd_codes_master mcm ON(mcm.code = bc.act_rate_plan_item_code AND mcm.code_type = bc.code_type) "+
						" JOIN mrd_code_claim_groups mccg ON(mccg.mrd_code_id = mcm.mrd_code_id AND mccg.code_group = 'UG')"+
						" LEFT JOIN alternate_activity_codes  aac ON(aac.item_id = bc.act_description_id and aac.item_code = bc.act_rate_plan_item_code "+
							" and aac.code_type = bc.code_type and (aac.sponsor_id = bcc.sponsor_id OR aac.sponsor_id is null)) "+
						" WHERE bc.act_rate_plan_item_code is not null AND bc.act_rate_plan_item_code != ''  AND last_submission_batch_id = ?) cav1 "+
						" WHERE no_of_items > 1) cav  "+
			" WHERE cav.claim_id = bcc.claim_id AND cav.no_of_items > 1 AND cav.charge_id = bcc.charge_id  AND cav.last_submission_batch_id=? ";

	private static final String UPDATE_SALES_CLAIM_ACTIVITY_ID = " UPDATE sales_claim_details scd "+
		 	" SET claim_activity_id = ssm.charge_id || '-' || ssd.sale_item_id "+
		 	" FROM store_sales_details ssd "+
		 	" JOIN store_sales_main ssm ON(ssd.sale_id = ssm.sale_id) "+
		 	" JOIN bill b ON(b.bill_no = ssm.bill_no) "+
		 	" JOIN bill_claim bcl on(bcl.bill_no = b.bill_no) "+
		 	" JOIN insurance_claim icl ON(icl.claim_id = bcl.claim_id) "+
		 	" WHERE scd.sale_item_id = ssd.sale_item_id  AND  icl.claim_id = scd.claim_id " +
		 	" AND icl.last_submission_batch_id = ? ";

	private static final String UPDATE_SALES_CLAIM_ACTIVITY_ID_RESUBMISSION = " UPDATE sales_claim_details scd "+
		 	" SET claim_activity_id = ssm.charge_id || '-' || ssd.sale_item_id "+
		 	" FROM store_sales_details ssd "+
		 	" JOIN store_sales_main ssm ON(ssd.sale_id = ssm.sale_id) "+
		 	" JOIN bill b ON(b.bill_no = ssm.bill_no) "+
		 	" JOIN bill_claim bcl on(bcl.bill_no = b.bill_no) "+
		 	" JOIN insurance_claim icl ON(icl.claim_id = bcl.claim_id) "+
		 	" JOIN claim_submissions cs ON(cs.claim_id = icl.claim_id) "+
		 	" JOIN insurance_submission_batch isb ON (isb.submission_batch_id = cs.submission_batch_id) "+
		 	" WHERE scd.sale_item_id = ssd.sale_item_id  AND  icl.claim_id = scd.claim_id " +
		 	" AND isb.submission_batch_id = ? AND isb.is_resubmission = 'Y' ";
	
	private static final String UPDATE_SC_REPEATED_ITEMS_CLAIM_ACTIVITY_ID = " UPDATE sales_claim_details scd "+ 
			" SET claim_activity_id = 'ACT'||'-'|| min_charge_id || '-' || min_sale_item_id  "+
			" FROM ( SELECT * FROM ( "+ 
						" SELECT ic.last_submission_batch_id,scd.claim_id, scd.sale_item_id, "+
						" min(scd.sale_item_id) OVER (PARTITION BY scd.claim_id, sd.item_code, sale_date::date,ic.last_submission_batch_id, sd.code_type,"+
						" coalesce(scd.prior_auth_id, pip.prior_auth_id, ''), "+ 
						" mccg.code_group, m.issue_base_unit) AS min_sale_item_id, item_code, "+
						" count(scd.sale_item_id) OVER  (PARTITION BY scd.claim_id, sd.item_code, sale_date::date,ic.last_submission_batch_id, "+
						" sd.code_type,coalesce(scd.prior_auth_id, pip.prior_auth_id, ''), mccg.code_group, m.issue_base_unit) AS no_of_items, "+ 
						" sd.code_type, min(sm.charge_id) OVER (PARTITION BY scd.claim_id, sd.item_code, sale_date::date,ic.last_submission_batch_id, "+ 
						" sd.code_type,coalesce(scd.prior_auth_id, pip.prior_auth_id, ''), mccg.code_group, m.issue_base_unit) AS min_charge_id "+
						" FROM sales_claim_details scd "+
						" JOIN store_sales_details sd ON(scd.sale_item_id=sd.sale_item_id) "+
						" JOIN store_sales_main sm ON(sd.sale_id = sm.sale_id AND sm.is_external_pbm = FALSE) "+
						" JOIN store_item_details m ON (sd.medicine_id = m.medicine_id) "+
						" JOIN insurance_claim ic ON(scd.claim_id = ic.claim_id) "+
						" JOIN patient_insurance_plans pip ON(pip.patient_id = ic.patient_id and pip.plan_id = ic.plan_id "+
								" and pip.sponsor_id = scd.sponsor_id) "+
						" LEFT JOIN mrd_codes_master mcm ON(mcm.code = sd.item_code AND mcm.code_type = sd.code_type) "+
						" LEFT JOIN mrd_code_claim_groups mccg ON(mccg.mrd_code_id = mcm.mrd_code_id) "+
						" WHERE sd.item_code IS not null AND sd.item_code!='' AND (mccg.code_group = 'LG' OR mccg.code_group is null) "+
						" AND ic.last_submission_batch_id = ?) cav1 "+
					" WHERE no_of_items > 1) cav "+
			" WHERE cav.claim_id = scd.claim_id  AND scd.sale_item_id = cav.sale_item_id  AND  cav.last_submission_batch_id = ? ";
	
	private static final String UPDATE_UNLISTED_SC_REPEATED_ITEMS_CLAIM_ACTIVITY_ID = " UPDATE sales_claim_details scd "+
			" SET claim_activity_id = 'ACT'||'-'|| min_charge_id || '-' || min_sale_item_id  "+
			" FROM ( SELECT * FROM  "+
						" (SELECT ic.last_submission_batch_id,scd.claim_id, scd.sale_item_id, min(scd.sale_item_id) "+ 
						" OVER (PARTITION BY scd.claim_id, sd.item_code, sale_date::date,ic.last_submission_batch_id, sd.code_type, "+ 
						" coalesce(scd.prior_auth_id, pip.prior_auth_id, ''), mccg.code_group, m.issue_base_unit, sd.medicine_id) AS min_sale_item_id, "+ 
						" item_code, count(scd.sale_item_id) OVER  (PARTITION BY scd.claim_id, sd.item_code, sale_date::date,ic.last_submission_batch_id, "+ 
						" sd.code_type,coalesce(scd.prior_auth_id, pip.prior_auth_id, ''), mccg.code_group, m.issue_base_unit, sd.medicine_id) AS no_of_items, "+ 
						" sd.code_type, min(sm.charge_id) OVER (PARTITION BY scd.claim_id, sd.item_code, sale_date::date,ic.last_submission_batch_id, "+ 
						" sd.code_type,coalesce(scd.prior_auth_id, pip.prior_auth_id, ''), mccg.code_group, m.issue_base_unit, sd.medicine_id) AS min_charge_id "+
					" FROM sales_claim_details scd "+
					" JOIN store_sales_details sd ON(scd.sale_item_id=sd.sale_item_id) "+
					" JOIN store_sales_main sm ON(sd.sale_id = sm.sale_id AND sm.is_external_pbm = FALSE) "+
					" JOIN store_item_details m ON (sd.medicine_id = m.medicine_id) "+
					" JOIN insurance_claim ic ON(scd.claim_id = ic.claim_id) "+
					" JOIN patient_insurance_plans pip ON(pip.patient_id = ic.patient_id AND pip.plan_id = ic.plan_id AND pip.sponsor_id = scd.sponsor_id) "+
					" JOIN mrd_codes_master mcm ON(mcm.code = sd.item_code AND mcm.code_type = sd.code_type) "+
					" JOIN mrd_code_claim_groups mccg ON(mccg.mrd_code_id = mcm.mrd_code_id AND mccg.code_group = 'UG') "+
					" WHERE sd.item_code IS not null AND sd.item_code!='' AND ic.last_submission_batch_id = ?) cav1 "+
				" WHERE no_of_items > 1) cav "+
			" WHERE cav.claim_id = scd.claim_id  AND scd.sale_item_id = cav.sale_item_id  AND  cav.last_submission_batch_id = ? ";

	public boolean updateClaimActivityId(String submissionBatchID, String isResubmission)throws SQLException {

		boolean success = false;

		// First we are updating charge id as claim activity id for all items..
		if(!isResubmission.equals("Y")) {
			updateClaimActivityIds(submissionBatchID, UPDATE_SALES_CLAIM_ACTIVITY_ID);
			updateClaimActivityIds(submissionBatchID, UPDATE_BILL_CHARGE_CLAIM_ACTIVITY_ID);
		}
		else {
			updateClaimActivityIds(submissionBatchID, UPDATE_SALES_CLAIM_ACTIVITY_ID_RESUBMISSION);
			updateClaimActivityIds(submissionBatchID, UPDATE_BILL_CHAGRE_CLAIM_ACTIVITY_ID_RESUBMISSION);
		}

		// Updating activity id for all repeating items.. (Combined Activities)
		// Items are mapped to LG (Listed group), UG (Unlisted Group) or NLG (Non Listed Group).
		// Refer https://practo.atlassian.net/wiki/spaces/HIMS/pages/391512355/DHA+HAAD+Claim+Compliance+Changes
		// Update claim activity id for listed group able items.
		updateCombinedClaimActivityIds(submissionBatchID, UPDATE_BC_REPEATING_ITEMS_CLAIM_ACTIVITY_ID);
		updateCombinedClaimActivityIds(submissionBatchID, UPDATE_SC_REPEATED_ITEMS_CLAIM_ACTIVITY_ID);
		
		// Update claim activity id for Unlisted group able items..
		updateCombinedClaimActivityIds(submissionBatchID, UPDATE_UNLISTED_BC_REPEATING_ITEMS_CLAIM_ACTIVITY_ID);
		updateCombinedClaimActivityIds(submissionBatchID, UPDATE_UNLISTED_SC_REPEATED_ITEMS_CLAIM_ACTIVITY_ID);

		success = true;

		return success;
	}

	private boolean updateCombinedClaimActivityIds(String submissionBatchID,
			String query) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		boolean success = false;

		try{
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			ps = con.prepareStatement(query);
			ps.setString(1, submissionBatchID);
			ps.setString(2, submissionBatchID);
			int i = ps.executeUpdate();
			if(i > 0) success = true;

		}finally{
			if(null != ps) ps.close();
			DataBaseUtil.commitClose(con, success);
		}
		return success;
		
	}

	public boolean updateClaimActivityIds(String submissionBatchID, String query)throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		boolean success = false;

		try{
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			ps = con.prepareStatement(query);
			ps.setString(1, submissionBatchID);
			int i = ps.executeUpdate();
			if(i > 0) success = true;

		}finally{
			if(null != ps) ps.close();
			DataBaseUtil.commitClose(con, success);
		}
		return success;
	}
	private static final String UPDATE_INSURANCE_SUBMISSION = " UPDATE insurance_submission_batch isb "
			+" SET status = 'S', submission_date= now() WHERE submission_batch_id= ? ";
	public boolean updateInsuranceSubmissionBatch(String submissionBatchID)throws SQLException {
		return updateClaimActivityIds(submissionBatchID,UPDATE_INSURANCE_SUBMISSION);
	}
	private static final String UPDATE_CLAIM_SUBMISSION = " UPDATE claim_submissions isb "
			+" SET status = 'S' WHERE submission_batch_id= ? ";
	public boolean updateClaimSubmission(String submissionBatchID)throws SQLException {
		return updateClaimActivityIds(submissionBatchID,UPDATE_CLAIM_SUBMISSION);
	}
	public static final String FIND_PRIMARY_DIAGNOSIS =
			" SELECT  "+
			" md.diag_type as diagnosis_type, " +
			" md.present_on_admission, md.year_of_onset, " +
			" md.code_type , icd_code, code_desc, md.sent_for_approval " +
			" FROM mrd_diagnosis md " +
			" JOIN mrd_codes_master mcm ON (mcm.code_type = md.code_type AND mcm.code = md.icd_code) " +
			" WHERE visit_id = ? AND diag_type = 'P'";

	public List<BasicDynaBean> findPrimaryDiagnosis(String mainVisitId) throws SQLException {
		return DataBaseUtil.queryToDynaList(FIND_PRIMARY_DIAGNOSIS, mainVisitId);
	}

	private static final String GET_UNLISTED_ITEMS = "SELECT min(bc.charge_id) as charge_id, "+
			" min(bc.act_description) as act_description, bcc.claim_activity_id "+
			" FROM bill_charge bc "+
			" JOIN bill_charge_claim bcc ON(bc.charge_id = bcc.charge_id) "+
			" JOIN insurance_claim ic ON(bcc.claim_id = ic.claim_id) "+
			" JOIN mrd_codes_master mcm ON(mcm.code = bc.act_rate_plan_item_code AND mcm.code_type = bc.code_type) "+
			" JOIN mrd_code_claim_groups mccg ON(mccg.mrd_code_id = mcm.mrd_code_id AND mccg.code_group = 'UG') "+
			" WHERE bcc.claim_activity_id NOT IN(SELECT bccl.claim_activity_id FROM "+
										" mrd_observations mo "+
										" JOIN bill_charge_claim bccl ON(bccl.charge_id = mo.charge_id)  "+
										" WHERE mo.code in ('Description', 'Activity description')  "+
										" AND bccl.claim_activity_id IS NOT NULL "+
										" GROUP BY bccl.claim_activity_id ) "+
			" AND ic.last_submission_batch_id = ? "+
			" GROUP BY bcc.claim_activity_id ";
	
	public List<BasicDynaBean> getUnlistedItems(String submissionBatchId)throws SQLException{
		return DataBaseUtil.queryToDynaList(GET_UNLISTED_ITEMS, new Object[] {submissionBatchId});
	}
	
	public boolean  insertObservationForUnlistedItems(String submissionBatchId, String healthAuthority)
	throws SQLException ,IOException{
		
		List<BasicDynaBean> unListedItems = getUnlistedItems(submissionBatchId);
		List<BasicDynaBean> obsList = new ArrayList<>();
		obsList = getObservationList(unListedItems, healthAuthority);
		
		Boolean success = true;
		Connection con = null;
		if(!obsList.isEmpty()){
			try{
				con = DataBaseUtil.getConnection();
				con.setAutoCommit(false);
				success = mrdObservationDAO.insertAll(con,obsList);
			}finally{
				DataBaseUtil.commitClose(con, success);
			}
		}
		
		return success;
	}
	
	private List<BasicDynaBean> getObservationList(List<BasicDynaBean> unListedItems, String healthAuthority) throws SQLException{
		List<BasicDynaBean> obsList = new ArrayList<>();
		for(BasicDynaBean bean : unListedItems){
			String chargeId = (String)bean.get("charge_id");
			String itemName = (String)bean.get("act_description");
			BasicDynaBean obsBean = mrdObservationDAO.getBean();
			obsBean.set("observation_id", DataBaseUtil.getNextSequence("mrd_observations_observation_id_seq"));
			obsBean.set("charge_id", chargeId);
			obsBean.set("observation_type", "Text");
			obsBean.set("code", healthAuthority.equals("DHA") ? "Description" : "Activity description");
			obsBean.set("value", itemName);
			obsBean.set("value_type", healthAuthority.equals("DHA") ? "Other" : "Text");
			obsBean.set("value_editable", "Y");
			obsList.add(obsBean);
		}
		return obsList;
	}
	
	public int update(Map columndata, Map keys) throws SQLException, IOException {
		Boolean success = true;
		Connection con = null;
		try{
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			return update(con, columndata, keys);
		}finally{
			DataBaseUtil.commitClose(con, success);
		}
	}
	private static final String GET_SUBMISSION_BATCH_ID_LIST = " SELECT isb.submission_batch_id,isb.created_date  from claim_submissions cs"
			+ " JOIN insurance_submission_batch isb using(submission_batch_id)"
			+ " WHERE claim_id= ? ORDER BY isb.submission_batch_id ";
	
	public List<BasicDynaBean> getSubmissionId(String claimId) throws SQLException {
		return DataBaseUtil.queryToDynaList(GET_SUBMISSION_BATCH_ID_LIST, new Object[] {claimId});
	}
	
	private static final String GET_IN_PROGRESS_SUBMISSION_BATCH_LIST = " SELECT *  from insurance_submission_batch isb"
			+ " WHERE isb.processing_status= ? ORDER BY isb.submission_batch_id ";
	public List<BasicDynaBean> getInProgressSubmission() throws SQLException{
		return DataBaseUtil.queryToDynaList(GET_IN_PROGRESS_SUBMISSION_BATCH_LIST, new Object[] {"P"});
	}
	
}
