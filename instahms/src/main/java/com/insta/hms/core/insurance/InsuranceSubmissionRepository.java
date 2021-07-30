package com.insta.hms.core.insurance;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.medicalrecords.codification.MRDObservationsRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.codec.binary.Base64;
import org.springframework.stereotype.Repository;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * The Class InsuranceSubmissionRepository.
 */
@Repository
public class InsuranceSubmissionRepository extends GenericRepository {

  /** The mrd observations repository. */
  @LazyAutowired
  private MRDObservationsRepository mrdObservationsRepository;

  /**
   * Instantiates a new insurance submission repository.
   */
  public InsuranceSubmissionRepository() {
    super("insurance_submission_batch");
  }

  /** The find claims fields for xml. */
  private static String FIND_CLAIMS_FIELDS_FOR_XML =
      " SELECT " + " ic.claim_id, isb.submission_batch_id, ic.submission_id_with_correction,"
          + " ic.last_submission_batch_id, "
          + " ppd.member_id, ppd.policy_number, pip.patient_policy_id, pip.plan_id,"
          + " pr.category_id, pip.priority, "
          + " pip.sponsor_id, COALESCE (hit.tpa_code, '@'||tm.tpa_name) AS receiver_id,"
          + " tm.tpa_name, pip.insurance_co, icm.insurance_co_name,"
          + " ppd.eligibility_reference_number, ic.account_group as claim_account_group,"
          + " COALESCE (hic.insurance_co_code,'@'||icm.insurance_co_name) AS payer_id,"
          + " ic.payers_reference_no, " + " agm.account_group_id, agm.account_group_name, "
          + " CASE WHEN isb.account_group != 0 THEN agm.account_group_service_reg_no "
          + " ELSE hcm.hospital_center_service_reg_no END AS provider_id, "
          + " CASE WHEN pd.government_identifier IS NULL OR pd.government_identifier = '' THEN "
          + " COALESCE(gim.identifier_type,'') ELSE  pd.government_identifier"
          + " END AS emirates_id_number, " + " SUM(total_amount) AS gross, "
          + " SUM(total_amount-(total_claim + total_claim_return)) AS patient_share, "
          + " SUM(total_claim + total_claim_return) AS net, "
          + " SUM(insurance_deduction) AS deduction, pip.use_drg, pip.use_perdiem, "
          + " agm.account_group_service_reg_no AS facility_id, "
          + " pr.encounter_type, pr.encounter_start_type, pr.encounter_end_type, "
          + " CASE WHEN pr.established_type='Y' THEN true ELSE false END as established_status, "
          + " pr.mr_no AS patient_id, ic.main_visit_id,"
          + " ic.patient_id AS claim_patient_id, pr.visit_type, "
          + " trim(COALESCE(pip.prior_auth_id,'')) AS prior_auth_id, "
          + " to_char((pr.reg_date||' '||pr.reg_time) :: timestamp without time zone,"
          + " 'dd/MM/yyyy hh24:mi') as start_date,"
          + " to_char((pr.discharge_date||' '||pr.discharge_time) :: timestamp without time zone,"
          + " 'dd/MM/yyyy hh24:mi') as end_date,"
          + " to_char(current_timestamp::timestamp, 'dd-MM-yyyy hh24:mi') AS todays_date, "
          + " icm.insurance_co_id, isb.is_resubmission, ic.resubmission_type, ic.comments,"
          + " etc.encounter_type_desc, est.code_desc AS encounter_start_type_desc,"
          + " eet.code_desc AS encounter_end_type_desc,"
          + " tsrc.transfer_hospital_service_regn_no AS source_service_regn_no, "
          + " tdest.transfer_hospital_service_regn_no AS destination_service_regn_no, "
          + " pd.patient_name, pd.middle_name, pd.last_name, pd.patient_gender, pd.salutation, "
          + " pd.custom_list1_value AS nationality, "
          + " to_char(coalesce(pd.expected_dob, pd.dateofbirth), 'dd/MM/yyyy') AS date_of_birth,"
          + " icam.category_name AS package_name, "
          + " CASE WHEN ppd.policy_validity_start IS NOT NULL "
          + "   THEN to_char(ppd.policy_validity_start, 'dd/MM/yyyy')"
          + " ELSE '' END AS policy_validity_start, "
          + " CASE WHEN ppd.policy_validity_end IS NOT NULL "
          + "   THEN to_char(ppd.policy_validity_end, 'dd/MM/yyyy')"
          + " ELSE '' END AS policy_validity_end, "
          + " pdd.doc_id, COALESCE(pdoc.content_type, '') AS card_type, "
          + " COALESCE(pdoc.original_extension, '') AS card_ext, "
          + " pdd.doc_name AS card_comment,isb.center_id as batch_center_id,"
          + " ipm.require_pbm_authorization, pr.reg_date as patient_reg_date, "
          + " CASE WHEN ic.account_group = 1 THEN CASE WHEN pr.encounter_end_date IS NOT NULL THEN "
          + " to_char(pr.encounter_end_date::date, 'dd/MM/yyyy') || ' ' || "
          + " to_char(pr.encounter_end_time::time, 'hh24:mi') "
          + " ELSE to_char(bc.max_order_date, 'dd/MM/yyyy hh24:mi') END "
          + " ELSE to_char(bc.max_order_date, 'dd/MM/yyyy hh24:mi') END AS encounter_end_datetime ";

  /** The find claims tables. */
  private static String FIND_CLAIMS_TABLES =
      " FROM bill b "
          + " JOIN bill_claim bcl ON (b.bill_no = bcl.bill_no) "
          + " JOIN insurance_claim ic ON (ic.claim_id = bcl.claim_id)"
          + " LEFT JOIN LATERAL(SELECT bc_sub.claim_id, max(bc_sub.encounterEndDate) "
          + " as max_order_date "
          + " FROM(SELECT charge_id, claim_id, CASE WHEN bch.charge_head='SERSNP' "
          + " AND ser.activity_timing_eclaim=true THEN COALESCE(conducted_datetime, posted_date) "
          + " ELSE posted_date END AS encounterEndDate "
          + " FROM bill_charge bch LEFT JOIN services ser "
          + " ON(bch.charge_head='SERSNP' AND bch.act_description_id = ser.service_id) "
          + " JOIN bill_claim cl ON(bch.bill_no = cl.bill_no "
          + " AND cl.claim_id = bcl.claim_id) WHERE "
          + " bch.status<>'X' AND "
          + " bch.charge_head NOT IN('PHCRET','PHRET','INVRET')) "
          + " as bc_sub WHERE bc_sub.claim_id = bcl.claim_id "
          + " group by bc_sub.claim_id) as bc ON(bc.claim_id = ic.claim_id) "
          + " JOIN insurance_submission_batch isb "
          + "   ON (isb.submission_batch_id = ic.last_submission_batch_id) "
          + " @ JOIN account_group_master agm ON (agm.account_group_id = isb.account_group) "
          + " % JOIN hospital_center_master hcm ON (hcm.center_id = isb.center_id) "
          + " JOIN patient_registration pr ON (pr.patient_id = ic.patient_id) "
          + " JOIN patient_insurance_plans pip "
          + "   ON (pr.patient_id = pip.patient_id and ic.plan_id = pip.plan_id) "
          + " JOIN patient_details pd ON (pd.mr_no = pr.mr_no) "
          + " # JOIN tpa_master tm ON (tm.tpa_id = pip.sponsor_id) "
          + " LEFT JOIN ha_tpa_code hit ON(hit.tpa_id = tm.tpa_id"
          + "    AND hit.health_authority = hcm.health_authority)"
          + " # JOIN insurance_company_master icm ON (icm.insurance_co_id = pip.insurance_co) "
          + " LEFT JOIN ha_ins_company_code hic ON(hic.insurance_co_id=icm.insurance_co_id"
          + "    AND hic.health_authority = hcm.health_authority)"
          + " LEFT JOIN insurance_plan_main ipm ON (ipm.plan_id = pip.plan_id)"
          + " LEFT JOIN insurance_category_master icam ON (icam.category_id = pr.category_id)"
          + " LEFT JOIN govt_identifier_master gim ON (gim.identifier_id = pd.identifier_id) "
          + " LEFT JOIN patient_policy_details ppd ON "
          + " (ppd.patient_policy_id = pip.patient_policy_id) "
          + " LEFT JOIN plan_docs_details pdd ON ppd.patient_policy_id = pdd.patient_policy_id "
          + " LEFT JOIN patient_documents pdoc ON (pdoc.doc_id = pdd.doc_id)  "
          + " LEFT JOIN encounter_type_codes etc ON (etc.encounter_type_id = encounter_type) "
          + " LEFT JOIN encounter_start_types est ON (est.code = pr.encounter_start_type::text) "
          + " LEFT JOIN encounter_end_types eet ON (eet.code = pr.encounter_end_type::text)  "
          + " LEFT JOIN transfer_hospitals tsrc ON "
          + " (tsrc.transfer_hospital_id = pr.transfer_source) "
          + " LEFT JOIN transfer_hospitals tdest "
          + "   ON (tdest.transfer_hospital_id = pr.transfer_destination)  "
          + " WHERE isb.submission_batch_id = ? AND is_tpa "
          + " GROUP BY ic.claim_id, isb.submission_batch_id,"
          + " ic.last_submission_batch_id, ppd.member_id, ppd.policy_number, "
          + " hit.tpa_code, tm.tpa_name, hic.insurance_co_code,"
          + " icm.insurance_co_name,ic.payers_reference_no, ppd.eligibility_reference_number,"
          + " isb.account_group, agm.account_group_id, agm.account_group_name,"
          + " agm.account_group_service_reg_no, ic.account_group,"
          + " hcm.center_id, hcm.center_name, hcm.hospital_center_service_reg_no,"
          + " pr.encounter_type, " + " pip.use_drg, pip.use_perdiem, pip.patient_policy_id,"
          + " pr.mr_no,pr.op_type,pip.plan_id,pr.category_id,pip.priority, "
          + " pip.sponsor_id, pip.insurance_co, "
          + " pd.patient_name, pd.middle_name, pd.last_name, pd.patient_gender, pd.salutation, "
          + " pd.custom_list1_value,pd.expected_dob, pd.dateofbirth, "
          + " pr.patient_id,pr.visit_type,pip.prior_auth_id,pr.reg_date,pr.reg_time,"
          + " pr.discharge_date,pr.discharge_time, "
          + " pr.encounter_start_type, pr.encounter_end_type, "
          + " tsrc.transfer_hospital_service_regn_no, tdest.transfer_hospital_service_regn_no,"
          + " pd.government_identifier,gim.identifier_type, icm.insurance_co_id,"
          + " ic.main_visit_id, ic.patient_id, "
          + " isb.is_resubmission, ic.resubmission_type, ic.comments,"
          + " etc.encounter_type_desc, est.code_desc, eet.code_desc,"
          + " ipm.plan_name, icam.category_name,"
          + " ppd.policy_validity_start, ppd.policy_validity_end, "
          + " pdd.doc_id, pdoc.content_type, pdoc.original_extension,"
          + " pdd.doc_name,isb.center_id,ipm.require_pbm_authorization,bc.max_order_date ";

  /** The find claims tables resubmission. */
  private static String FIND_CLAIMS_TABLES_RESUBMISSION =
      " FROM bill b "
          + " JOIN bill_claim bcl ON (b.bill_no = bcl.bill_no) "
          + " JOIN insurance_claim ic ON (ic.claim_id = bcl.claim_id)"
          + " LEFT  JOIN LATERAL(SELECT bc_sub.claim_id, max(bc_sub.encounterEndDate) "
          + " as max_order_date "
          + " FROM(SELECT charge_id, claim_id, CASE WHEN bch.charge_head='SERSNP' "
          + " AND ser.activity_timing_eclaim=true THEN COALESCE(conducted_datetime, posted_date) "
          + " ELSE posted_date END AS encounterEndDate "
          + " FROM bill_charge bch LEFT JOIN services ser "
          + " ON(bch.charge_head='SERSNP' AND bch.act_description_id = ser.service_id) "
          + " JOIN bill_claim cl ON(bch.bill_no = cl.bill_no AND cl.claim_id = bcl.claim_id) WHERE "
          + " bch.status<>'X' AND bch.charge_head NOT IN('PHCRET','PHRET','INVRET')) "
          + " as bc_sub WHERE bc_sub.claim_id = bcl.claim_id "
          + " group by bc_sub.claim_id) as bc ON(bc.claim_id = ic.claim_id) "
          + " JOIN claim_submissions cs" + "   ON (ic.claim_id = cs.claim_id) "
          + " JOIN insurance_submission_batch isb"
          + " ON (isb.submission_batch_id = cs.submission_batch_id) "
          + " @ JOIN account_group_master agm ON (agm.account_group_id = isb.account_group) "
          + " % JOIN hospital_center_master hcm ON (hcm.center_id = isb.center_id) "
          + " JOIN patient_registration pr ON (pr.patient_id = ic.patient_id) "
          + " JOIN patient_insurance_plans pip "
          + "   ON (pr.patient_id = pip.patient_id AND pip.plan_id = ic.plan_id) "
          + " JOIN patient_details pd ON (pd.mr_no = pr.mr_no) "
          + " # JOIN tpa_master tm ON (tm.tpa_id = pip.sponsor_id) "
          + " LEFT JOIN ha_tpa_code hit ON(hit.tpa_id = tm.tpa_id "
          + "         AND hit.health_authority = hcm.health_authority)"
          + " # JOIN insurance_company_master icm ON (icm.insurance_co_id = pip.insurance_co) "
          + " LEFT JOIN ha_ins_company_code hic "
          + "   ON(hic.insurance_co_id=icm.insurance_co_id "
          + "       AND hic.health_authority = hcm.health_authority)"
          + " LEFT JOIN insurance_plan_main ipm ON (ipm.plan_id = pip.plan_id)"
          + " LEFT JOIN insurance_category_master icam ON (icam.category_id = pr.category_id)"
          + " LEFT JOIN govt_identifier_master gim ON (gim.identifier_id = pd.identifier_id) "
          + " LEFT JOIN patient_policy_details ppd ON "
          + " (ppd.patient_policy_id = pip.patient_policy_id) "
          + " LEFT JOIN plan_docs_details pdd ON ppd.patient_policy_id = pdd.patient_policy_id "
          + " LEFT JOIN patient_documents pdoc ON (pdoc.doc_id = pdd.doc_id)  "
          + " LEFT JOIN encounter_type_codes etc ON (etc.encounter_type_id = encounter_type) "
          + " LEFT JOIN encounter_start_types est ON (est.code = pr.encounter_start_type::text) "
          + " LEFT JOIN encounter_end_types eet"
          + "  ON (eet.code = pr.encounter_end_type::text)  "
          + " LEFT JOIN transfer_hospitals tsrc ON "
          + " (tsrc.transfer_hospital_id = pr.transfer_source) "
          + " LEFT JOIN transfer_hospitals tdest "
          + "   ON (tdest.transfer_hospital_id = pr.transfer_destination)  "
          + " WHERE isb.submission_batch_id = ? AND is_tpa AND isb.is_resubmission = 'Y' "
          + " GROUP BY ic.claim_id, isb.submission_batch_id,"
          + " ic.last_submission_batch_id, ppd.member_id, ppd.policy_number,"
          + " hit.tpa_code, tm.tpa_name, hic.insurance_co_code,"
          + " icm.insurance_co_name, ic.payers_reference_no,  "
          + " isb.account_group, agm.account_group_id,"
          + " agm.account_group_name, agm.account_group_service_reg_no,"
          + " hcm.center_id, hcm.center_name, ppd.eligibility_reference_number, "
          + " hcm.hospital_center_service_reg_no, pr.encounter_type, "
          + " pip.use_drg, pip.use_perdiem, pip.patient_policy_id, "
          + " pr.mr_no,pr.op_type,pip.plan_id,pr.category_id,pip.priority, "
          + " pip.sponsor_id, pip.insurance_co, "
          + " pd.patient_name, pd.middle_name, pd.last_name,"
          + " pd.patient_gender, pd.salutation, "
          + " pd.custom_list1_value,pd.expected_dob, pd.dateofbirth, "
          + " pr.patient_id,pr.visit_type,pip.prior_auth_id,pr.reg_date,"
          + " pr.reg_time,pr.discharge_date,pr.discharge_time, "
          + " pr.encounter_start_type, pr.encounter_end_type,"
          + " tsrc.transfer_hospital_service_regn_no, tdest.transfer_hospital_service_regn_no,"
          + " pd.government_identifier,gim.identifier_type, icm.insurance_co_id, "
          + " ic.main_visit_id, ic.patient_id, "
          + " isb.is_resubmission, ic.resubmission_type, ic.comments, "
          + " etc.encounter_type_desc, est.code_desc, eet.code_desc,"
          + " ipm.plan_name, icam.category_name,"
          + " ppd.policy_validity_start, ppd.policy_validity_end, "
          + " pdd.doc_id, pdoc.content_type, pdoc.original_extension,"
          + " pdd.doc_name,isb.center_id,ipm.require_pbm_authorization,bc.max_order_date ";

  /**
   * Gets the xml header fields.
   *
   * @param submissionBean the submission bean
   * @param healthAuthority the health authority
   * @return the xml header fields
   * @throws SQLException the SQL exception
   */
  public BasicDynaBean getXmlHeaderFields(BasicDynaBean submissionBean, String healthAuthority)
      throws SQLException {
    String tpaId = (String) submissionBean.get("tpa_id");
    String submissionBatchId = (String) submissionBean.get("submission_batch_id");
    if (submissionBean != null) {
      if (submissionBean.get("account_group") != null
          && (Integer) submissionBean.get("account_group") != 0) {
        return DataBaseUtil.queryToDynaBean(GET_HAAD_ACCOUNT_XML_HEADER,
            new Object[] {healthAuthority, tpaId, submissionBatchId});
      } else if (submissionBean.get("center_id") != null) {
        return DataBaseUtil.queryToDynaBean(GET_HAAD_HOSPITAL_CENTER_XML_HEADER,
            new Object[] {healthAuthority, tpaId, submissionBatchId});
      }
    }
    return null;
  }

  /** The Constant GET_HAAD_ACCOUNT_XML_HEADER. */
  private static final String GET_HAAD_ACCOUNT_XML_HEADER = "SELECT "
      + " account_group_id, account_group_name, "
      + " account_group_service_reg_no AS provider_id, '' AS eclaim_xml_schema, "
      + " (SELECT COALESCE (tpa_code, '@'||tpa_name) FROM tpa_master tm"
      + " LEFT JOIN ha_tpa_code hta ON(hta.tpa_id=tm.tpa_id AND health_authority = ?)"
      + "  WHERE tm.tpa_id= ?) " + " AS receiver_id,"
      + " to_char(current_timestamp::timestamp, 'dd/MM/yyyy hh24:mi') AS todays_date, "
      + " 0 as claims_count, 'Y' AS testing, '' AS from_date, "
      + " '' AS to_date, '' AS operation, '' AS disposition_flag "
      + " FROM account_group_master WHERE account_group_id = " + " (SELECT account_group FROM"
      + " insurance_submission_batch WHERE submission_batch_id = ?) ";

  /** The Constant GET_HAAD_HOSPITAL_CENTER_XML_HEADER. */
  private static final String GET_HAAD_HOSPITAL_CENTER_XML_HEADER = "SELECT "
      + " center_id, center_name, "
      + " hospital_center_service_reg_no AS provider_id, '' AS eclaim_xml_schema, "
      + " (SELECT COALESCE (tpa_code, '@'||tpa_name) FROM tpa_master tm "
      + "  LEFT JOIN ha_tpa_code hta"
      + "  ON(hta.tpa_id=tm.tpa_id AND health_authority = ?) WHERE tm.tpa_id= ?) "
      + " AS receiver_id, "
      + " to_char(current_timestamp::timestamp, 'dd/MM/yyyy hh24:mi') AS todays_date, "
      + " 0 as claims_count, 'Y' AS testing, '' AS from_date, "
      + "'' AS to_date, '' AS operation, '' AS disposition_flag "
      + " FROM hospital_center_master WHERE center_id = "
      + "  (SELECT center_id FROM insurance_submission_batch WHERE submission_batch_id = ?) ";

  /**
   * Gets the claims by submission batch id.
   *
   * @param submissionBatchId the submission batch id
   * @return the claims by submission batch id
   */
  public List<BasicDynaBean> getClaimsBySubmissionBatchId(String submissionBatchId) {
    BasicDynaBean submissionBean = findByKey("submission_batch_id", submissionBatchId);
    String tpaId =
        submissionBean.get("tpa_id") != null ? (String) submissionBean.get("tpa_id") : null;
    int accountGroup = submissionBean.get("account_group") != null
        ? (Integer) submissionBean.get("account_group") : 0;

    String isResubmission = (String) submissionBean.get("is_resubmission");

    String query = FIND_CLAIMS_FIELDS_FOR_XML;

    if (isResubmission.equals("Y")) {
      query = query + FIND_CLAIMS_TABLES_RESUBMISSION;
    } else {
      query = query + FIND_CLAIMS_TABLES;
    }
    if (tpaId == null || tpaId.trim().equals("")) {
      query = query.replaceAll(" # ", " LEFT ");
    } else {
      query = query.replaceAll(" # ", " ");
    }

    if (accountGroup != 0) {
      query = query.replaceAll(" @ ", " ");
      query = query.replaceAll(" % ", " LEFT ");
    } else {
      query = query.replaceAll(" @ ", " LEFT ");
      query = query.replaceAll(" % ", " ");
    }

    return DatabaseHelper.queryToDynaList(query, new Object[] {submissionBatchId});
  }

  /**
   * Convert to base 64 binary.
   *
   * @param file the file
   * @return the string
   */
  public String convertToBase64Binary(InputStream file) {
    // TODO Auto-generated method stub
    String encodedStr = null;

    byte[] bytes = new byte[4096];

    ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    while (true) {
      int numBytes = 0;
      try {
        numBytes = file.read(bytes);
      } catch (IOException ex) {
        // TODO Auto-generated catch block
        ex.printStackTrace();
      }
      if (numBytes <= 0) {
        break;
      }
      buffer.write(bytes, 0, numBytes);
    }
    byte[] filebytes = buffer.toByteArray();

    // all chars in encoded are guaranteed to be 7-bit ASCII
    byte[] encoded = Base64.encodeBase64(filebytes);
    try {
      encodedStr = new String(encoded, "ASCII");
    } catch (UnsupportedEncodingException exception) {
      // TODO Auto-generated catch block
      exception.printStackTrace();
    }

    return encodedStr;

  }

  /** The Constant UPDATE_BILL_CHARGE_CLAIM_ACTIVITY_ID. */
  private static final String UPDATE_BILL_CHARGE_CLAIM_ACTIVITY_ID =
      " UPDATE " + " bill_charge_claim bcc " + " SET claim_activity_id = charge_id "
          + " FROM insurance_claim ic "
          + " WHERE ic.claim_id = bcc.claim_id AND ic.last_submission_batch_id = ? ";

  /** The Constant UPDATE_BILL_CHAGRE_CLAIM_ACTIVITY_ID_RESUBMISSION. */
  private static final String UPDATE_BILL_CHAGRE_CLAIM_ACTIVITY_ID_RESUBMISSION =
      " UPDATE " + " bill_charge_claim bcc " + " SET claim_activity_id = charge_id "
          + " FROM insurance_claim ic "
          + " JOIN claim_submissions cs ON(cs.claim_id = ic.claim_id) "
          + " JOIN insurance_submission_batch isb"
          + "  ON(isb.submission_batch_id = cs.submission_batch_id) "
          + " WHERE ic.claim_id = bcc.claim_id"
          + " AND isb.submission_batch_id = ? AND isb.is_resubmission = 'Y' ";

  /** The Constant UPDATE_BC_REPEATING_ITEMS_CLAIM_ACTIVITY_ID. */
  private static final String UPDATE_BC_REPEATING_ITEMS_CLAIM_ACTIVITY_ID = " UPDATE "
      + " bill_charge_claim bcc  " + " SET claim_activity_id = 'ACT'||'-'|| min_charge_id "
      + " FROM (SELECT * FROM "
      + "(SELECT ic.last_submission_batch_id,bcc.claim_id, bcc.charge_id, "
      + " min(bcc.charge_id) OVER (PARTITION BY "
      + " bcc.claim_id,act_rate_plan_item_code, posted_date::date,ic.last_submission_batch_id, "
      + " bc.code_type,coalesce(bcc.prior_auth_id, pip.prior_auth_id, ''),"
      + " aac.alternate_code, mccg.code_group) AS min_charge_id, "
      + " act_rate_plan_item_code, count(bcc.charge_id) OVER (PARTITION BY"
      + " bcc.claim_id,act_rate_plan_item_code,posted_date::date,"
      + " ic.last_submission_batch_id,bc.code_type, coalesce(bcc.prior_auth_id,"
      + " pip.prior_auth_id, ''), aac.alternate_code, " + " mccg.code_group) AS no_of_items "
      + " FROM bill_charge_claim bcc " + " JOIN bill_charge bc ON(bc.charge_id=bcc.charge_id) "
      + " JOIN insurance_claim ic ON(bcc.claim_id = ic.claim_id) "
      + " JOIN patient_insurance_plans pip ON(pip.patient_id = ic.patient_id"
      + " and pip.plan_id = ic.plan_id " + " and pip.sponsor_id = bcc.sponsor_id) "
      + " LEFT JOIN mrd_codes_master mcm ON(mcm.code = bc.act_rate_plan_item_code"
      + " AND mcm.code_type = bc.code_type) "
      + " LEFT JOIN mrd_code_claim_groups mccg ON(mccg.mrd_code_id = mcm.mrd_code_id)"
      + " LEFT JOIN alternate_activity_codes  aac ON(aac.item_id = bc.act_description_id "
      + " and aac.item_code = bc.act_rate_plan_item_code "
      + " and aac.code_type = bc.code_type and (aac.sponsor_id = bcc.sponsor_id"
      + "  OR aac.sponsor_id is null)) "
      + " WHERE bc.act_rate_plan_item_code is not null AND bc.act_rate_plan_item_code != ''  "
      + " AND (mccg.code_group = 'LG' OR mccg.code_group is null) "
      + " AND last_submission_batch_id = ?) cav1 " + " WHERE no_of_items > 1) cav  "
      + " WHERE cav.claim_id = bcc.claim_id AND cav.no_of_items > 1 "
      + " AND cav.charge_id = bcc.charge_id  AND cav.last_submission_batch_id=? ";

  /** The Constant UPDATE_UNLISTED_BC_REPEATING_ITEMS_CLAIM_ACTIVITY_ID. */
  private static final String UPDATE_UNLISTED_BC_REPEATING_ITEMS_CLAIM_ACTIVITY_ID = " UPDATE "
      + " bill_charge_claim bcc  " + " SET claim_activity_id = 'ACT'||'-'|| min_charge_id "
      + " FROM (SELECT * FROM "
      + "(SELECT ic.last_submission_batch_id,bcc.claim_id, bcc.charge_id, "
      + " min(bcc.charge_id) OVER (PARTITION BY "
      + " bcc.claim_id,act_rate_plan_item_code, posted_date::date,ic.last_submission_batch_id, "
      + " bc.code_type,coalesce(bcc.prior_auth_id, pip.prior_auth_id, ''),"
      + " bc.act_description_id, aac.alternate_code, mccg.code_group) AS min_charge_id, "
      + " act_rate_plan_item_code, count(bcc.charge_id) OVER"
      + "    (PARTITION BY bcc.claim_id,act_rate_plan_item_code,posted_date::date,"
      + " ic.last_submission_batch_id,bc.code_type, "
      + " coalesce(bcc.prior_auth_id, pip.prior_auth_id, ''),"
      + " bc.act_description_id, aac.alternate_code, " + " mccg.code_group) AS no_of_items "
      + " FROM bill_charge_claim bcc " + " JOIN bill_charge bc ON(bc.charge_id=bcc.charge_id) "
      + " JOIN insurance_claim ic ON(bcc.claim_id = ic.claim_id) "
      + " JOIN patient_insurance_plans pip ON(pip.patient_id = ic.patient_id"
      + " and pip.plan_id = ic.plan_id " + " and pip.sponsor_id = bcc.sponsor_id) "
      + " JOIN mrd_codes_master mcm "
      + "   ON(mcm.code = bc.act_rate_plan_item_code AND mcm.code_type = bc.code_type) "
      + " JOIN mrd_code_claim_groups mccg "
      + "   ON(mccg.mrd_code_id = mcm.mrd_code_id AND mccg.code_group = 'UG')"
      + " LEFT JOIN alternate_activity_codes  aac ON(aac.item_id = bc.act_description_id "
      + "   and aac.item_code = bc.act_rate_plan_item_code "
      + " and aac.code_type = bc.code_type"
      + " and (aac.sponsor_id = bcc.sponsor_id OR aac.sponsor_id is null)) "
      + " WHERE bc.act_rate_plan_item_code is not null AND bc.act_rate_plan_item_code != '' "
      + "  AND last_submission_batch_id = ?) cav1 " + " WHERE no_of_items > 1) cav  "
      + " WHERE cav.claim_id = bcc.claim_id AND cav.no_of_items > 1 "
      + " AND cav.charge_id = bcc.charge_id  AND cav.last_submission_batch_id=? ";

  /** The Constant UPDATE_SALES_CLAIM_ACTIVITY_ID. */
  private static final String UPDATE_SALES_CLAIM_ACTIVITY_ID =
      " UPDATE " + " sales_claim_details scd "
          + " SET claim_activity_id = ssm.charge_id || '-' || ssd.sale_item_id "
          + " FROM store_sales_details ssd "
          + " JOIN store_sales_main ssm ON(ssd.sale_id = ssm.sale_id) "
          + " JOIN bill b ON(b.bill_no = ssm.bill_no) "
          + " JOIN bill_claim bcl on(bcl.bill_no = b.bill_no) "
          + " JOIN insurance_claim icl ON(icl.claim_id = bcl.claim_id) "
          + " WHERE scd.sale_item_id = ssd.sale_item_id  AND  icl.claim_id = scd.claim_id "
          + " AND icl.last_submission_batch_id = ? ";

  /** The Constant UPDATE_SALES_CLAIM_ACTIVITY_ID_RESUBMISSION. */
  private static final String UPDATE_SALES_CLAIM_ACTIVITY_ID_RESUBMISSION =
      " UPDATE " + " sales_claim_details scd "
          + " SET claim_activity_id = ssm.charge_id || '-' || ssd.sale_item_id "
          + " FROM store_sales_details ssd "
          + " JOIN store_sales_main ssm ON(ssd.sale_id = ssm.sale_id) "
          + " JOIN bill b ON(b.bill_no = ssm.bill_no) "
          + " JOIN bill_claim bcl on(bcl.bill_no = b.bill_no) "
          + " JOIN insurance_claim icl ON(icl.claim_id = bcl.claim_id) "
          + " JOIN claim_submissions cs ON(cs.claim_id = icl.claim_id) "
          + " JOIN insurance_submission_batch isb "
          + "   ON (isb.submission_batch_id = cs.submission_batch_id) "
          + " WHERE scd.sale_item_id = ssd.sale_item_id  AND  icl.claim_id = scd.claim_id "
          + " AND isb.submission_batch_id = ? AND isb.is_resubmission = 'Y' ";

  /** The Constant UPDATE_SC_REPEATED_ITEMS_CLAIM_ACTIVITY_ID. */
  private static final String UPDATE_SC_REPEATED_ITEMS_CLAIM_ACTIVITY_ID = " UPDATE "
      + " sales_claim_details scd "
      + " SET claim_activity_id = 'ACT'||'-'|| min_charge_id || '-' || min_sale_item_id  "
      + " FROM ( SELECT * FROM ( "
      + " SELECT ic.last_submission_batch_id,scd.claim_id, scd.sale_item_id, "
      + " min(scd.sale_item_id) OVER (PARTITION BY scd.claim_id, sd.item_code, "
      + " sale_date::date,ic.last_submission_batch_id, sd.code_type,"
      + " coalesce(scd.prior_auth_id, pip.prior_auth_id, ''), "
      + " mccg.code_group, m.issue_base_unit) AS min_sale_item_id, item_code, "
      + " count(scd.sale_item_id) OVER  (PARTITION BY scd.claim_id, sd.item_code, "
      + " sale_date::date,ic.last_submission_batch_id, "
      + " sd.code_type,coalesce(scd.prior_auth_id, pip.prior_auth_id, ''), "
      + " mccg.code_group, m.issue_base_unit) AS no_of_items, "
      + " sd.code_type, min(sm.charge_id) OVER (PARTITION BY scd.claim_id, "
      + " sd.item_code, sale_date::date,ic.last_submission_batch_id, "
      + " sd.code_type,coalesce(scd.prior_auth_id, pip.prior_auth_id, ''), "
      + " mccg.code_group, m.issue_base_unit) AS min_charge_id "
      + " FROM sales_claim_details scd "
      + " JOIN store_sales_details sd ON(scd.sale_item_id=sd.sale_item_id) "
      + " JOIN store_sales_main sm ON(sd.sale_id = sm.sale_id) "
      + " JOIN store_item_details m ON (sd.medicine_id = m.medicine_id) "
      + " JOIN insurance_claim ic ON(scd.claim_id = ic.claim_id) "
      + " JOIN patient_insurance_plans pip "
      + " ON(pip.patient_id = ic.patient_id and pip.plan_id = ic.plan_id "
      + " and pip.sponsor_id = scd.sponsor_id) " + " LEFT JOIN mrd_codes_master mcm "
      + " ON(mcm.code = sd.item_code AND mcm.code_type = sd.code_type) "
      + " LEFT JOIN mrd_code_claim_groups mccg " + " ON(mccg.mrd_code_id = mcm.mrd_code_id) "
      + " WHERE sd.item_code IS not null AND sd.item_code!='' "
      + " AND (mccg.code_group = 'LG' OR mccg.code_group is null) "
      + " AND ic.last_submission_batch_id = ?) cav1 " + " WHERE no_of_items > 1) cav "
      + " WHERE cav.claim_id = scd.claim_id  AND scd.sale_item_id = cav.sale_item_id  "
      + " AND  cav.last_submission_batch_id = ? ";

  /** The Constant UPDATE_UNLISTED_SC_REPEATED_ITEMS_CLAIM_ACTIVITY_ID. */
  private static final String UPDATE_UNLISTED_SC_REPEATED_ITEMS_CLAIM_ACTIVITY_ID =
      " UPDATE " + " sales_claim_details scd "
          + " SET claim_activity_id = 'ACT'||'-'|| min_charge_id || '-' || min_sale_item_id  "
          + " FROM ( SELECT * FROM  " + " (SELECT ic.last_submission_batch_id,scd.claim_id,"
          + " scd.sale_item_id, min(scd.sale_item_id) "
          + " OVER (PARTITION BY scd.claim_id, sd.item_code, "
          + " sale_date::date,ic.last_submission_batch_id, sd.code_type, "
          + " coalesce(scd.prior_auth_id, pip.prior_auth_id, ''), mccg.code_group,"
          + " m.issue_base_unit, sd.medicine_id) AS min_sale_item_id, "
          + " item_code, count(scd.sale_item_id) OVER "
          + "  (PARTITION BY scd.claim_id, sd.item_code,"
          + " sale_date::date,ic.last_submission_batch_id, "
          + " sd.code_type,coalesce(scd.prior_auth_id, pip.prior_auth_id, ''),"
          + " mccg.code_group, m.issue_base_unit, sd.medicine_id) AS no_of_items, "
          + " sd.code_type, min(sm.charge_id) OVER (PARTITION BY scd.claim_id, sd.item_code,"
          + " sale_date::date,ic.last_submission_batch_id, "
          + " sd.code_type,coalesce(scd.prior_auth_id, pip.prior_auth_id, ''), mccg.code_group,"
          + " m.issue_base_unit, sd.medicine_id) AS min_charge_id "
          + " FROM sales_claim_details scd "
          + " JOIN store_sales_details sd ON(scd.sale_item_id=sd.sale_item_id) "
          + " JOIN store_sales_main sm ON(sd.sale_id = sm.sale_id) "
          + " JOIN store_item_details m ON (sd.medicine_id = m.medicine_id) "
          + " JOIN insurance_claim ic ON(scd.claim_id = ic.claim_id) "
          + " JOIN patient_insurance_plans pip ON(pip.patient_id = ic.patient_id "
          + "   AND pip.plan_id = ic.plan_id AND pip.sponsor_id = scd.sponsor_id) "
          + " JOIN mrd_codes_master mcm "
          + " ON(mcm.code = sd.item_code AND mcm.code_type = sd.code_type) "
          + " JOIN mrd_code_claim_groups mccg "
          + " ON(mccg.mrd_code_id = mcm.mrd_code_id AND mccg.code_group = 'UG') "
          + " WHERE sd.item_code IS not null AND sd.item_code!=''"
          + " AND ic.last_submission_batch_id = ?) cav1 " + " WHERE no_of_items > 1) cav "
          + " WHERE cav.claim_id = scd.claim_id  AND scd.sale_item_id = cav.sale_item_id  "
          + " AND  cav.last_submission_batch_id = ? ";

  /**
   * Update claim activity id.
   *
   * @param submissionBatchID the submission batch ID
   * @param isResubmission the is resubmission
   * @return true, if successful
   */
  public boolean updateClaimActivityId(String submissionBatchID, String isResubmission) {
    // First we are updating charge id as claim activity id for all items..
    if (!isResubmission.equals("Y")) {
      updateClaimActivityIds(submissionBatchID, UPDATE_SALES_CLAIM_ACTIVITY_ID);
      updateClaimActivityIds(submissionBatchID, UPDATE_BILL_CHARGE_CLAIM_ACTIVITY_ID);
    } else {
      updateClaimActivityIds(submissionBatchID, UPDATE_SALES_CLAIM_ACTIVITY_ID_RESUBMISSION);
      updateClaimActivityIds(submissionBatchID,
          UPDATE_BILL_CHAGRE_CLAIM_ACTIVITY_ID_RESUBMISSION);
    }

    // Updating activity id for all repeating items..

    // Update claim activity id for listed group able items.
    updateCombinedClaimActivityIds(submissionBatchID,
        UPDATE_BC_REPEATING_ITEMS_CLAIM_ACTIVITY_ID);
    updateCombinedClaimActivityIds(submissionBatchID,
        UPDATE_SC_REPEATED_ITEMS_CLAIM_ACTIVITY_ID);

    // Update claim activity id for Unlisted group able items..
    updateCombinedClaimActivityIds(submissionBatchID,
        UPDATE_UNLISTED_BC_REPEATING_ITEMS_CLAIM_ACTIVITY_ID);
    updateCombinedClaimActivityIds(submissionBatchID,
        UPDATE_UNLISTED_SC_REPEATED_ITEMS_CLAIM_ACTIVITY_ID);

    return true;
  }

  /**
   * Update claim activity ids.
   *
   * @param submissionBatchID the submission batch ID
   * @param query the query
   * @return true, if successful
   */
  public boolean updateClaimActivityIds(String submissionBatchID, String query) {
    return DatabaseHelper.update(query, new Object[] {submissionBatchID}) > 0;
  }

  /**
   * Update combined claim activity ids.
   *
   * @param submissionBatchID the submission batch ID
   * @param query the query
   * @return true, if successful
   */
  private boolean updateCombinedClaimActivityIds(String submissionBatchID, String query) {
    return DatabaseHelper.update(query,
        new Object[] {submissionBatchID, submissionBatchID}) > 0;
  }

  /**
   * Insert observation for unlisted items.
   *
   * @param submissionBatchId the submission batch id
   * @param healthAuthority the health authority
   * @return true, if successful
   */
  public boolean insertObservationForUnlistedItems(String submissionBatchId,
      String healthAuthority) {

    List<BasicDynaBean> unListedItems = getUnlistedItems(submissionBatchId);
    List<BasicDynaBean> obsList = new ArrayList<BasicDynaBean>();
    obsList = getObservationList(unListedItems, healthAuthority);

    Boolean success = true;
    int[] insertResult;

    if (!obsList.isEmpty()) {
      insertResult = mrdObservationsRepository.batchInsert(obsList);
      for (int i = 0; i < insertResult.length; i++) {
        success &= (insertResult[i] > 0);
      }
    }

    return success;
  }

  /** The Constant GET_UNLISTED_ITEMS. */
  private static final String GET_UNLISTED_ITEMS = "SELECT min(bc.charge_id) as charge_id, "
      + " min(bc.act_description) as act_description, bcc.claim_activity_id "
      + " FROM bill_charge bc "
      + " JOIN bill_charge_claim bcc ON(bc.charge_id = bcc.charge_id) "
      + " JOIN insurance_claim ic ON(bcc.claim_id = ic.claim_id) "
      + " JOIN mrd_codes_master mcm "
      + "   ON(mcm.code = bc.act_rate_plan_item_code AND mcm.code_type = bc.code_type) "
      + " JOIN mrd_code_claim_groups mccg "
      + "   ON(mccg.mrd_code_id = mcm.mrd_code_id AND mccg.code_group = 'UG') "
      + " WHERE bcc.claim_activity_id NOT IN(SELECT bccl.claim_activity_id FROM "
      + " mrd_observations mo "
      + " JOIN bill_charge_claim bccl ON(bccl.charge_id = mo.charge_id)  "
      + " WHERE mo.code in ('Description', 'Activity description')  "
      + " AND bccl.claim_activity_id IS NOT NULL " + " GROUP BY bccl.claim_activity_id ) "
      + " AND ic.last_submission_batch_id = ? " + " GROUP BY bcc.claim_activity_id ";

  /**
   * Gets the unlisted items.
   *
   * @param submissionBatchId the submission batch id
   * @return the unlisted items
   */
  public List<BasicDynaBean> getUnlistedItems(String submissionBatchId) {
    return DatabaseHelper.queryToDynaList(GET_UNLISTED_ITEMS,
        new Object[] {submissionBatchId});
  }

  /**
   * Gets the observation list.
   *
   * @param unListedItems the un listed items
   * @param healthAuthority the health authority
   * @return the observation list
   */
  private List<BasicDynaBean> getObservationList(List<BasicDynaBean> unListedItems,
      String healthAuthority) {

    List<BasicDynaBean> obsList = new ArrayList<BasicDynaBean>();
    for (BasicDynaBean bean : unListedItems) {
      String chargeId = (String) bean.get("charge_id");
      String itemName = (String) bean.get("act_description");
      BasicDynaBean obsBean = mrdObservationsRepository.getBean();
      obsBean.set("observation_id",
          DatabaseHelper.getNextSequence("mrd_observations_observation_id_seq"));
      obsBean.set("charge_id", chargeId);
      obsBean.set("observation_type", "Text");
      obsBean.set("code",
          healthAuthority.equals("DHA") ? "Description" : "Activity description");
      obsBean.set("value", itemName);
      obsBean.set("value_type", healthAuthority.equals("DHA") ? "Other" : "Text");
      obsBean.set("value_editable", "Y");
      obsList.add(obsBean);
    }
    return obsList;
  }

  /** The find primary diagnosis. */
  public static String FIND_PRIMARY_DIAGNOSIS =
      " SELECT  " + " md.diag_type as diagnosis_type, "
          + " md.code_type , icd_code, code_desc, md.sent_for_approval "
          + " FROM mrd_diagnosis md " + " JOIN mrd_codes_master mcm ON "
          + " (mcm.code_type = md.code_type AND mcm.code = md.icd_code) "
          + " WHERE visit_id = ? AND diag_type = 'P'";

  /**
   * Find primary diagnosis.
   *
   * @param mainVisitId the main visit id
   * @return the list
   */
  public List<BasicDynaBean> findPrimaryDiagnosis(String mainVisitId) {
    return DatabaseHelper.queryToDynaList(FIND_PRIMARY_DIAGNOSIS, mainVisitId);
  }

  /** The Constant FIND_BILLS. */
  private static final String FIND_BILLS =
      " SELECT b.bill_no,b.status,b.bill_type," + " b.restriction_type,b.primary_claim_status "
          + " FROM bill b " + " JOIN bill_claim bclm ON(b.bill_no = bclm.bill_no)"
          + " WHERE bclm.claim_id = ? AND b.status != 'X' "
          + " AND b.total_amount >= 0 ORDER BY b.bill_no ";

  /**
   * Find all bills.
   *
   * @param claimId the claim id
   * @return the list
   */
  public List<BasicDynaBean> findAllBills(String claimId) {
    return DatabaseHelper.queryToDynaList(FIND_BILLS, claimId);
  }

  /** The Constant FIND_CLAIM_OBSERVATIONS_WITHOUT_PRESENTING_COMPLAINT. */
  private static final String FIND_CLAIM_OBSERVATIONS_WITHOUT_PRESENTING_COMPLAINT = "SELECT  "
      + "  value, value_type, observation_type AS type, code, mo.charge_id "
      + " FROM mrd_observations mo" + " JOIN bill_charge bc ON (mo.charge_id= bc.charge_id) "
      + " JOIN bill_claim bcl ON (bc.bill_no = bcl.bill_no  AND bcl.claim_id = ? ) "
      + " WHERE  code != 'Presenting-Complaint' AND (mo.sponsor_id = ? "
      + " OR mo.sponsor_id is NULL OR mo.sponsor_id='' ) ";

  /** The Constant FIND_CLAIM_OBSERVATIONS. */
  private static final String FIND_CLAIM_OBSERVATIONS = "SELECT  "
      + "  value, value_type, observation_type AS type, code, mo.charge_id "
      + " FROM mrd_observations mo" + " JOIN bill_charge bc ON (mo.charge_id= bc.charge_id) "
      + " JOIN bill_claim bcl ON (bc.bill_no = bcl.bill_no  AND bcl.claim_id = ? ) "
      + " WHERE (mo.sponsor_id = ? OR mo.sponsor_id is NULL OR mo.sponsor_id='' ) ";

  /**
   * Find all claim observations.
   *
   * @param claimId the claim id
   * @param sponsorId the sponsor id
   * @param healthAuthority the health authority
   * @return the list
   */
  public List<BasicDynaBean> findAllClaimObservations(String claimId, String sponsorId,
      String healthAuthority) {
    if (!healthAuthority.equals("DHA")) {
      return DatabaseHelper.queryToDynaList(
          FIND_CLAIM_OBSERVATIONS_WITHOUT_PRESENTING_COMPLAINT,
          new Object[] {claimId, sponsorId});
    } else {
      return DatabaseHelper.queryToDynaList(FIND_CLAIM_OBSERVATIONS,
          new Object[] {claimId, sponsorId});
    }
  }

}
