package com.insta.hms.core.insurance;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

/**
 * The Class InsuranceClaimRepository.
 */
@Repository
public class InsuranceClaimRepository extends GenericRepository {

  /**
   * Instantiates a new insurance claim repository.
   */
  public InsuranceClaimRepository() {
    super("insurance_claim");
  }

  /**
   * Update closed claims in insurance_claim where charges in bill_charge_claim are closed.
   * 
   * @param remittanceId the remittance id
   * @return the integer
   */
  public Integer updateClosedHospClaims(Integer remittanceId) {
    return DatabaseHelper.update(updateClosedHospClaimsQuery,
        new Object[] {remittanceId, remittanceId});
  }

  /**
   * Update closed phar claims.
   * 
   * @param remittanceId the remittance id
   * @return the integer
   */
  public Integer updateClosedPharClaims(Integer remittanceId) {
    return DatabaseHelper.update(updateClosedPharClaimsQuery,
        new Object[] {remittanceId, remittanceId});
  }

  /** The Constant CLAIM_SEQUENCE_PATTERN. */
  private static final String claimSequencePatternQuery =
      " SELECT pattern_id FROM hosp_claim_seq_prefs " + " WHERE priority = ( "
          + "  SELECT min(priority)" + " FROM hosp_claim_seq_prefs "
          + "  WHERE (center_id=? or center_id=0) "
          + "  AND (account_group = ? OR account_group = 0) " + " ) ";

  /**
   * Gets the next prefixed id.
   *
   * @param centerId the center id
   * @param accGrpId the acc grp id
   * @return the next prefixed id
   */
  public String getNextPrefixedId(int centerId, Integer accGrpId) {
    String patternId =
        DatabaseHelper.getString(claimSequencePatternQuery, new Object[] {centerId, accGrpId});
    return DatabaseHelper.getNextPatternId(patternId);
  }

  /** The update closed hosp claims. */
  private static final String updateClosedHospClaimsQuery = "UPDATE insurance_claim ic "
      + "SET    status = 'C', action_remarks = 'Claim amount received.', closure_type = 'F' "
      + "FROM   insurance_remittance_details ird "
      + "WHERE  ic.claim_id NOT IN ((SELECT bcc2.claim_id "
      + "FROM   insurance_remittance_details ird2, " + "bill_charge_claim bcc2, "
      + "insurance_claim ic2 " + "WHERE  ird2.claim_id = bcc2.claim_id "
      + "AND ird2.remittance_id = ? " + "AND bcc2.insurance_claim_amt != 0 "
      + "AND ic2.claim_id = bcc2.claim_id " + "AND bcc2.claim_status != 'C' "
      + "GROUP  BY bcc2.claim_id)) " + "AND ird.claim_id = ic.claim_id "
      + "AND ird.remittance_id = ? ";

  /**
   * The update closed phar claims. This query is not really required as we now copy charges
   * from sales_claim_Details to bill_charge_claim table and the above query takes care of it
   */
  private static final String updateClosedPharClaimsQuery = "UPDATE insurance_claim ic "
      + "SET    status = 'C' " + "FROM   insurance_remittance_details ird "
      + "WHERE  ic.claim_id NOT IN ((SELECT scd2.claim_id "
      + "FROM   insurance_remittance_details ird2, " + "bill_charge_claim scd2, "
      + "insurance_claim ic2 " + "WHERE  ird2.claim_id = scd2.claim_id "
      + "AND ird2.remittance_id = ? " + "AND scd2.insurance_claim_amt != 0 "
      + "AND ic2.claim_id = scd2.claim_id " + "AND scd2.claim_status != 'C' "
      + "GROUP  BY scd2.claim_id)) " + "AND ird.claim_id = ic.claim_id "
      + "AND ird.remittance_id = ? ";

  /**
   * The update auto close claim.
   */
  /*
   * Updates claims to closed if they fall below auto_close_claims_difference preference in
   * generic preference
   */
  private static final String updateAutoCloseClaim =
      "UPDATE insurance_claim ic " + "SET    status = 'C', " + "       action_remarks = "
          + "               'Denial accepted with amount difference. Closing the claim.' , "
          + "       closure_type = 'D' "
          + "FROM   (SELECT COALESCE(ABS(SUM(insurance_claim_amt - claim_recd_total)), 0) AS "
          + "                      amount_diff, " + "               bcc.claim_id "
          + "        FROM   bill_charge_claim bcc "
          + "               JOIN insurance_remittance_details ird "
          + "                 ON ( ird.remittance_id = ? "
          + "                      AND ird.claim_id = bcc.claim_id ) "
          + "        GROUP  BY bcc.claim_id) AS foo, " + "       generic_preferences gp "
          + "WHERE  gp.auto_close_claims_with_difference >= foo.amount_diff "
          + "       AND foo.amount_diff != 0 " + "       AND foo.claim_id = ic.claim_id "
          + "       AND ic.status != 'C' ";

  /**
   * Update auto closed.
   *
   * @param remittanceId the remittance id
   * @return the integer
   */
  public Integer updateAutoClosed(Integer remittanceId) {
    return DatabaseHelper.update(updateAutoCloseClaim, new Object[] {remittanceId});
  }

  /** The update payers ref. */
  private static final String updatePayersRefQuery =
      "UPDATE insurance_claim ic SET payers_reference_no = ird.payer_id FROM "
          + "insurance_remittance_details ird"
          + " WHERE ird.remittance_id = ? AND ird.claim_id = ic.claim_id";

  /**
   * Update payers ref.
   *
   * @param remittanceId the remittance id
   * @return the integer
   */
  public Integer updatePayersRef(Integer remittanceId) {
    return DatabaseHelper.update(updatePayersRefQuery, new Object[] {remittanceId});
  }

  /**
   * The Constant GET_ATTACHMENT.
   */
  private static final String GET_ATTACHMENT =
      "SELECT attachment,attachment_content_type FROM insurance_claim WHERE claim_id = ?";

  /**
   * Gets the attachment.
   *
   * @param claimId the claim id
   * @return the attachment
   */
  public BasicDynaBean getAttachment(String claimId) {

    return DatabaseHelper.queryToDynaBean(GET_ATTACHMENT, new Object[] {claimId});
  }

  /**
   * The Constant GET_PHARMACY_DENIED_CHARGE_NET.
   */
  public static final String GET_PHARMACY_DENIED_CHARGE_NET = 
      " SELECT distinct case when bc.submission_batch_type = 'P' "
      + " THEN foop.total_package_ins_claim_amt - foop.total_package_claim_recd_total "
      + " ELSE sum(coalesce(scl.insurance_claim_amt, 0.00) "
      + " + coalesce(scl.return_insurance_claim_amt, 0.00) "
      + " - coalesce(scl.claim_recd, 0.00)) END AS net, "
      + " CASE WHEN bc.submission_batch_type='P' THEN foop.package_vat_net ELSE "
      + " sum((coalesce(scl.tax_amt, 0.00))) END AS vat_net ";
  
  public static final String GET_PHARMACY_RESUB_CHARGE_NET =
      " SELECT (coalesce(scl.insurance_claim_amt, 0.00)"
      + " + coalesce(scl.return_insurance_claim_amt, 0.00)"
      + " - coalesce(scl.claim_recd, 0.00)) AS net ";

  /**
   * The Constant GET_PHARMACY_CHARGE_NET.
   */
  public static final String GET_PHARMACY_CHARGE_NET =
      " SELECT distinct case when bc.submission_batch_type = 'P'"
          + " THEN foop.total_package_ins_claim_amt ELSE "
          + " (coalesce(scl.insurance_claim_amt, 0.00)"
          + " + coalesce(scl.return_insurance_claim_amt, 0)) END AS net ";

  /**
   * The Constant GET_PHARMACY_CHARGE_NET_XML.
   */
  public static final String GET_PHARMACY_CHARGE_NET_XML =
      " SELECT distinct case when bc.submission_batch_type = 'P'"
          + " THEN foop.total_package_ins_claim_amt"
          + " ELSE sum((coalesce(scl.insurance_claim_amt, 0.00)"
          + " + coalesce(scl.return_insurance_claim_amt, 0))) END AS net,"
          + " CASE WHEN bc.submission_batch_type='P' THEN foop.package_vat_net "
          + " ELSE sum((coalesce(scl.tax_amt, 0.00))) END AS vat_net ";

  /**
   * The Constant GET_ALL_PHARMACY_CHARGES_FIELDS_XML.
   */
  public static final String GET_ALL_PHARMACY_CHARGES_FIELDS_XML = " ,"
      + "   sum(s.quantity) AS qty," + " sum(scl.insurance_claim_amt) AS insurance_claim_amt, "
      + "   sum(s.amount) AS amt, "
      + "   sum(scl.return_insurance_claim_amt) AS return_insurance_claim_amt, "
      + "   sum(s.return_amt) AS return_amt, " + "   sum(s.return_qty) AS return_qty, "
      + " case when bc.submission_batch_type = 'P' "
      + " then sum(s.amount + s.return_amt + foop.total_package_amount - s.tax - s.return_tax_amt) "
      + " else sum(s.amount + s.return_amt - s.tax - s.return_tax_amt) end AS amount, "
      + " sum(s.rate) AS rate ," + " sum(s.quantity + s.return_qty) AS quantity, "
      + "   sum(s.disc) AS discount, " + " scl.claim_activity_id as charge_id, "
      + "   max(sstd.tax_rate) AS vat_tax_rate, "
      + " s.item_code, 'P-'||scl.claim_activity_id AS activity_charge_id,"
      + " 'Pharmacy' AS activity_group, " + " max(sale_date) AS item_posted_date,"
      + "   to_char(max(sale_date),'dd/MM/yyyy hh24:mi') AS posted_date,  "
      + "   sum(coalesce(scl.claim_recd, 0.00)) as claim_recd_total,"
      + "   array_to_string(array_agg(bc.charge_id), ',') as charge_id_list, "
      + " msct.haad_code as act_type, msct.code_type AS act_type_desc,"
      + " act_item_code, ic.main_visit_id, ic.claim_id,"
      + " b.status AS bill_status, pr.visit_type AS patient_visit_type,"
      + " coalesce(scl.prior_auth_id, pip.prior_auth_id) AS prior_auth_id,  "
      + " CASE WHEN (pr.op_type != 'O') THEN doc.doctor_license_number"
      + " ELSE ref.doctor_license_number END AS doctor_license_number,"
      + " CASE WHEN (pr.op_type != 'O') THEN doc.doctor_name"
      + " ELSE ref.referal_name END AS doctor_name,"
      + " CASE WHEN (pr.op_type != 'O') THEN doc.doctor_id"
      + " ELSE ref.referal_no END AS doctor_id,"
      + " CASE WHEN (pr.op_type != 'O') THEN 'Doctor'"
      + " ELSE ref.doctor_type END AS doctor_type,  "
      + " min(drs.doctor_license_number) AS conducting_dr_license_number,m.issue_base_unit,"
      + " min(presc_doc.doctor_license_number) AS prescribing_doctor_license_number,"
      + " CASE WHEN bc.charge_group='DRG' THEN 'Y' ELSE 'N' END AS is_drg_group,"
      + "   CASE WHEN bc.charge_head='MARDRG' THEN 'Y' ELSE 'N' END AS is_drg_charge, "
      + "   CASE WHEN bc.charge_group ='PDM' THEN 'Y' ELSE 'N' END AS is_perdiem_code, "
      + "   ref.doctor_license_number as ref_doctor_license_number,"
      + " is_home_care_code,pr.dept_name,#bc.bill_no# '' as special_service_contract_name, "
      + " bc.submission_batch_type, NULL AS ip_cons_cond_dr_license, "
      + " to_char(max(sale_date),'dd/MM/yyyy hh24:mi') AS activity_start_datetime ";

  /**
   * The Constant GET_ALL_PHARMACY_CHARGES_FIELDS.
   */
  public static final String GET_ALL_PHARMACY_CHARGES_FIELDS =
      " ,s.batch_no, m.medicine_id::TEXT AS item_id,"
      + " bc.act_description_id , s.quantity AS qty," + " scl.insurance_claim_amt, s.amount AS amt,"
      + " scl.return_insurance_claim_amt, s.return_amt, s.return_qty,"
      + " CASE WHEN bc.submission_batch_type='P' THEN foop.total_package_amount "
      + " ELSE (s.amount + s.return_amt - s.tax - s.return_tax_amt) END AS amount , s.rate,"
      + " NULL :: timestamp without time zone AS conducted_date,"
      + " s.erx_activity_id AS erx_activity_id,"
      + " sm.erx_reference_no AS erx_reference_no, " + " (s.quantity + s.return_qty) AS quantity,"
      + " s.disc AS discount, s.package_unit, " + "   s.sale_item_id AS sale_item_id, bc.charge_id,"
      + " bc.charge_group,bc.charge_head,bc.bill_no,b.bill_type,b.is_tpa,"
      + " s.item_code, 'P-'||bc.charge_id||'-'||s.sale_item_id AS activity_charge_id,"
      + " m.medicine_name||' ('||bc.act_description||') ' AS act_description,"
      + "scl.claim_activity_id, "
      + " 'Pharmacy' AS activity_group, cgc.chargegroup_name, chc.chargehead_name,"
      + " to_char(sale_date,'dd/MM/yyyy hh24:mi') AS posted_date,"
      + " sale_date AS item_posted_date, "
      + " scl.denial_code, idc.code_description AS denial_desc,"
      + " idc.status AS denial_code_status, idc.example, idc.type AS denial_code_type,"
      + "   s.denial_remarks, scl.closure_type,"
      + " scl.rejection_reason_category_id,scl.claim_status,"
      + " coalesce(scl.claim_recd, 0.00) as claim_recd_total,"
      + " s.sale_id, bc.charge_id AS activity_id , "
      + " msct.haad_code as act_type, msct.code_type AS act_type_desc,"
      + " act_item_code, ic.main_visit_id, ic.claim_id,"
      + " b.status AS bill_status, pr.visit_type AS patient_visit_type,"
      + " scl.prior_auth_id AS prior_auth_id, scl.prior_auth_mode_id, "
      + " CASE WHEN (pr.op_type != 'O') THEN doc.doctor_license_number"
      + " ELSE ref.doctor_license_number END AS doctor_license_number,"
      + " CASE WHEN (pr.op_type != 'O') THEN doc.doctor_name"
      + " ELSE ref.referal_name END AS doctor_name,"
      + " CASE WHEN (pr.op_type != 'O') THEN doc.doctor_id"
      + " ELSE ref.referal_no END AS doctor_id," + " CASE WHEN (pr.op_type != 'O') THEN 'Doctor'"
      + " ELSE ref.doctor_type END AS doctor_type,  "
      + " drs.doctor_license_number AS conducting_dr_license_number, "
      + "   presc_doc.doctor_license_number AS prescribing_doctor_license_number,"
      + " presc_doc.doctor_name AS prescribing_doctor_name, "
      + "   presc_doc.doctor_id AS prescribing_doctor_id, s.code_type AS code_type,"
      + " bc.submission_batch_type ";

  /**
   * The Constant GROUP_BY_PHARMACY_CHARGES.
   */
  public static final String GROUP_BY_PHARMACY_CHARGES =
      " GROUP BY scl.claim_activity_id, s.item_code, "
          + " sale_date::date , msct.haad_code,msct.code_type, "
          + " ic.main_visit_id, ic.claim_id,"
          + " b.status,pr.visit_type, coalesce(scl.prior_auth_id, pip.prior_auth_id), "
          + " pr.op_type, doc.doctor_license_number,"
          + " ref.doctor_license_number,doc.doctor_name,ref.referal_name,"
          + "doc.doctor_id,ref.referal_no, ref.doctor_type,scl.claim_id,"
          + " bc.act_item_code,m.issue_base_unit, ref_doctor_license_number,"
          + " is_home_care_code, " + " CASE WHEN bc.charge_group='DRG' THEN 'Y' ELSE 'N' END ,"
          + " CASE WHEN bc.charge_head='MARDRG' THEN 'Y' ELSE 'N' END, "
          + " CASE WHEN bc.charge_group='PDM' THEN 'Y' ELSE 'N' END,pr.dept_name, "
          + " dpt.is_referral_doc_as_ordering_clinician, #bc.bill_no# bc.submission_batch_type,"
          + " foop.package_vat_net, foop.total_package_ins_claim_amt, "
          + " foop.total_package_claim_recd_total, "
          + " special_service_contract_name ";

  /**
   * The Constant GET_ALL_PHARMACY_CHARGES_TABLES.
   */
  public static final String GET_ALL_PHARMACY_CHARGES_TABLES = "  FROM store_sales_details s"
      + " JOIN sales_claim_details scl ON (scl.sale_item_id = s.sale_item_id) "
      + " JOIN store_sales_main sm on (s.sale_id = sm.sale_id)"
      + " JOIN store_item_details m ON s.medicine_id = m.medicine_id"
      + " JOIN bill_charge bc ON (sm.charge_id= bc.charge_id)"
      + "   JOIN chargehead_constants chc ON (bc.charge_head = chc.chargehead_id) "
      + "   JOIN chargegroup_constants cgc ON (bc.charge_group = cgc.chargegroup_id)"
      + " JOIN bill b ON (b.bill_no = bc.bill_no)"
      + " LEFT JOIN bill_activity_charge bac ON (bac.charge_id = bc.charge_id) " 
      + " LEFT JOIN tests_conducted tc ON (bac.activity_id = text(tc.prescribed_id) " 
      + "   AND tc.patient_id=b.visit_id) "
      + " JOIN bill_claim bcl ON (b.bill_no = bcl.bill_no and bcl.claim_id = scl.claim_id) "
      + " JOIN patient_registration pr ON (pr.patient_id = b.visit_id)"
      + " LEFT JOIN doctors doc ON (doc.doctor_id = pr.doctor)"
      + " LEFT JOIN doctors presc_doc ON (presc_doc.doctor_id = bc.prescribing_dr_id)"
      + "   LEFT JOIN (" + "     SELECT 'Referal' AS doctor_type,referal_no, referal_name,"
      + " clinician_id AS doctor_license_number FROM referral" + "     UNION ALL"
      + "     SELECT 'Doctor' AS doctor_type,doctor_id, doctor_name,"
      + " doctor_license_number FROM doctors"
      + "     ) AS ref ON (ref.referal_no = pr.reference_docto_id)"
      + " JOIN insurance_claim ic ON (ic.claim_id = bcl.claim_id"
      + " AND ic.patient_id = pr.patient_id)"
      + " LEFT JOIN mrd_supported_code_types msct ON (msct.code_type = s.code_type)"
      + " LEFT JOIN insurance_denial_codes idc ON (idc.denial_code = scl.denial_code) "
      + " LEFT JOIN doctors drs ON (drs.doctor_id = bc.payee_doctor_id)"
      + " LEFT JOIN LATERAL (SELECT sum(scd.insurance_claim_amt) as total_package_ins_claim_amt,"
      + "  sum(ssd.amount + ssd.return_amt - ssd.tax - ssd.return_tax_amt) as total_package_amount,"
      + "  sum((coalesce(scd.tax_amt, 0.00))) AS package_vat_net, bch.package_id, "
      + "  bch.bill_no FROM sales_claim_details scd "
      + "  JOIN store_sales_details ssd ON(ssd.sale_item_id = scd.sale_item_id) "
      + "  JOIN store_sales_main sm on (ssd.sale_id = sm.sale_id)"
      + "  JOIN bill_charge bch ON (sm.charge_id= bch.charge_id)"
      + "  WHERE (bch.submission_batch_type='P' AND bch.charge_head<>'PKGPKG') "
      + "  GROUP BY bch.bill_no, bch.package_id) AS foop "
      + " ON (foop.package_id=bc.package_id AND foop.bill_no = bc.bill_no) "
      + " LEFT join packages pk ON (bc.package_id = pk.package_id) "
      + " WHERE (b.status != 'X') AND bc.status!='X' ";

  /**
   * The Constant GET_ALL_PHARMACY_CHARGES_TABLES_XML.
   */
  public static final String GET_ALL_PHARMACY_CHARGES_TABLES_XML =
      " FROM store_sales_details s"
          + " JOIN sales_claim_details scl ON (scl.sale_item_id = s.sale_item_id) "
          + " LEFT JOIN store_sales_tax_details sstd ON (sstd.sale_item_id = s.sale_item_id) "
          + " JOIN store_sales_main sm on (s.sale_id = sm.sale_id)"
          + " JOIN store_item_details m ON s.medicine_id = m.medicine_id"
          + " JOIN bill_charge bc ON (sm.charge_id= bc.charge_id)"
          + "   JOIN chargehead_constants chc ON (bc.charge_head = chc.chargehead_id) "
          + "   JOIN chargegroup_constants cgc ON (bc.charge_group = cgc.chargegroup_id)"
          + " JOIN bill b ON (b.bill_no = bc.bill_no)"
          + " JOIN bill_claim bcl ON (b.bill_no = bcl.bill_no and bcl.claim_id = scl.claim_id) "
          + " JOIN patient_registration pr ON (pr.patient_id = b.visit_id)"
          + " JOIN patient_insurance_plans pip "
          + "  ON (pip.patient_id = bcl.visit_id AND pip.plan_id = bcl.plan_id "
          + "   AND pip.sponsor_id = bcl.sponsor_id) "
          + " LEFT JOIN doctors doc ON (doc.doctor_id = pr.doctor)"
          + " LEFT JOIN doctors presc_doc ON (presc_doc.doctor_id = bc.prescribing_dr_id)"
          + "   LEFT JOIN (" + "     SELECT 'Referal' AS doctor_type,referal_no, referal_name,"
          + " clinician_id AS doctor_license_number FROM referral" + "     UNION ALL"
          + "     SELECT 'Doctor' AS doctor_type,doctor_id,"
          + " doctor_name, doctor_license_number FROM doctors"
          + "     ) AS ref ON (ref.referal_no = pr.reference_docto_id)"
          + " JOIN insurance_claim ic ON "
          + " (ic.claim_id = bcl.claim_id AND ic.patient_id = pr.patient_id)"
          + " LEFT JOIN mrd_supported_code_types msct ON (msct.code_type = s.code_type)"
          + "   LEFT JOIN insurance_denial_codes idc ON (idc.denial_code = scl.denial_code) "
          + " LEFT JOIN doctors drs ON (drs.doctor_id = bc.payee_doctor_id)"
          + " LEFT JOIN per_diem_codes_master pcm ON "
          + " (bc.act_rate_plan_item_code = pcm.per_diem_code) "
          + "   LEFT JOIN department dpt ON(dpt.dept_id = pr.dept_name) "
          + " LEFT JOIN LATERAL (SELECT sum(bcci.insurance_claim_amt) "
          + " as total_package_ins_claim_amt,"
          + " sum(bcci.claim_recd) as total_package_claim_recd_total, "
          + " sum(bci.amount + bci.return_amt - bci.tax - bci.return_tax_amt)"
          + " as total_package_amount, sum((coalesce(bcci.tax_amt, 0.00))) AS package_vat_net, "
          + " bch.package_id, bch.bill_no FROM sales_claim_details"
          + " bcci JOIN store_sales_details bci ON(bci.sale_item_id = bcci.sale_item_id) "
          + " JOIN store_sales_main sm on (bci.sale_id = sm.sale_id)"
          + " JOIN bill_charge bch ON (sm.charge_id= bch.charge_id)"
          + " WHERE (bch.submission_batch_type='P' AND bch.charge_head<>'PKGPKG') "
          + " GROUP BY bch.bill_no, bch.package_id) AS foop "
          + " ON (foop.package_id=bc.package_id AND foop.bill_no = bc.bill_no) "
          + " LEFT join packages pk ON (bc.package_id = pk.package_id) "
          + " WHERE (b.status != 'X' AND (b.status = 'F' OR b.status = 'C')) AND bc.status!='X' ";

  /**
   * The Constant GET_HOSPITAL_DENIED_CHARGE_NET.
   */
  public static final String GET_HOSPITAL_DENIED_CHARGE_NET =
      " SELECT sum(coalesce(bccl.insurance_claim_amt, 0.00)"
      + " + coalesce(bcc.return_insurance_claim_amt, 0.00)"
      + " - coalesce(bccl.claim_recd_total, 0.00)) AS net ";
  
  public static final String GET_HOSPITAL_DENIED_CHARGE_RESUB =
      " SELECT (coalesce(bccl.insurance_claim_amt, 0.00) "
      + " + coalesce(bcc.return_insurance_claim_amt, 0.00) "
      + " - coalesce(bccl.claim_recd_total, 0.00)) AS net ";

  /**
   * The Constant GET_HOSPITAL_CHARGE_NET.
   */
  public static final String GET_HOSPITAL_CHARGE_NET = " SELECT distinct"
      + " CASE WHEN bcc.submission_batch_type = 'P' "
      + " THEN FOO.total_package_ins_claim_amt "
      + " ELSE  (coalesce(bccl.insurance_claim_amt, 0.00)"
      + " + coalesce(bcc.return_insurance_claim_amt, 0.00)) END AS net ";

  /**
   * The Constant GET_HOSPITAL_CHARGE_NET_XML.
   */
  public static final String GET_HOSPITAL_CHARGE_NET_XML =
      " SELECT distinct CASE WHEN bcc.submission_batch_type='P' THEN "
          + " FOO.total_package_ins_claim_amt "
          + " ELSE "
          + " sum((COALESCE(bccl.insurance_claim_amt, 0.00)"
          + " + COALESCE(bcc.return_insurance_claim_amt, 0.00))) "
          + " END AS net,"
          + " CASE WHEN bcc.submission_batch_type='P' THEN "
          + " FOO.package_vat_net ELSE sum((coalesce(bccl.tax_amt, 0.00))) END AS vat_net ";

  /**
   * The Constant GET_ALL_HOSPITAL_CHARGES_FIELDS.
   */
  public static final String GET_ALL_HOSPITAL_CHARGES_FIELDS =
      "  ,'' AS batch_no, '' AS item_id, bcc.act_description_id, bcc.act_quantity AS qty,"
      + " bccl.insurance_claim_amt, bcc.amount AS amt,"
      + " bcc.return_insurance_claim_amt, bcc.return_amt, bcc.return_qty,"
      + " CASE when bcc.submission_batch_type='P' THEN "
      +    "  FOO.total_package_amount ELSE (bcc.amount + bcc.return_amt) END AS amount,"
      + " bcc.act_rate AS rate,  footc.conducted_date,"
      + " '' AS erx_activity_id, '' AS erx_reference_no, "
      + " (bcc.act_quantity + bcc.return_qty) AS quantity," + " bcc.discount, 1 AS package_unit,"
      + " 0 AS sale_item_id, bcc.charge_id AS charge_id,"
      + " bcc.charge_group,bcc.charge_head,bcc.bill_no,b.bill_type," + "b.is_tpa,"
      + " (act_rate_plan_item_code) as item_code," + " 'A-'||bcc.charge_id AS activity_charge_id,"
      + " bcc.act_description,bccl.claim_activity_id, "
      + " 'Hospital' AS activity_group, cgc.chargegroup_name, chc.chargehead_name,"
      + " to_char(posted_date, 'dd/MM/yyyy hh24:mi') AS posted_date,"
      + " posted_date AS item_posted_date,"
      + "   bccl.denial_code, idc.code_description AS denial_desc,"
      + " idc.status AS denial_code_status, "
      + "   idc.example, idc.type AS denial_code_type, bccl.denial_remarks,"
      + " bccl.closure_type , bccl.rejection_reason_category_id, "
      + "   bccl.claim_status, coalesce(bccl.claim_recd_total, 0.00) as claim_recd_total, "
      + " '' AS sale_id, '' AS activity_id ,"
      + " msct.haad_code as act_type, msct.code_type AS act_type_desc, "
      + "   '' AS act_item_code, ic.main_visit_id, ic.claim_id,"
      + " b.status AS bill_status, pr.visit_type AS patient_visit_type,"
      + " COALESCE(bccl.prior_auth_id, pr.prior_auth_id) AS prior_auth_id,"
      + "  COALESCE(bccl.prior_auth_mode_id, pr.prior_auth_mode_id) AS prior_auth_mode_id,"
      + " CASE WHEN (pr.op_type != 'O') THEN doc.doctor_license_number"
      + " ELSE ref.doctor_license_number END AS doctor_license_number,"
      + " CASE WHEN (pr.op_type != 'O') THEN doc.doctor_name"
      + " ELSE ref.referal_name END AS doctor_name,"
      + " CASE WHEN (pr.op_type != 'O') THEN doc.doctor_id"
      + " ELSE ref.referal_no END AS doctor_id," + " CASE WHEN (pr.op_type != 'O') THEN 'Doctor'"
      + " ELSE ref.doctor_type END AS doctor_type, "
      + " drs.doctor_license_number AS conducting_dr_license_number, "
      + "   presc_doc.doctor_license_number AS prescribing_doctor_license_number,"
      + " presc_doc.doctor_name AS prescribing_doctor_name, "
      + "   presc_doc.doctor_id AS prescribing_doctor_id, bcc.code_type AS code_type,"
      + " bcc.submission_batch_type ";

  /**
   * The Constant GET_ALL_HOSPITAL_CHARGES_TABLES.
   */
  public static final String GET_ALL_HOSPITAL_CHARGES_TABLES = 
      "  FROM bill_charge bcc "
      + " LEFT JOIN bill_activity_charge bac ON (bac.charge_id = bcc.charge_id) "
      + " LEFT JOIN tests_conducted tc ON (bac.activity_id = text(tc.prescribed_id)) "
      + " JOIN bill_charge_claim bccl ON (bcc.charge_id = bccl.charge_id) "
      + "   JOIN chargehead_constants chc ON (bcc.charge_head = chc.chargehead_id) "
      + "   JOIN chargegroup_constants cgc ON (bcc.charge_group = cgc.chargegroup_id)"
      + " JOIN bill b ON (b.bill_no = bcc.bill_no)"
      + " LEFT JOIN LATERAL "
      + "  (SELECT MAX(tc.conducted_date) AS conducted_date, " 
      + "          bac.charge_id " 
      + "   FROM bill_activity_charge bac " 
      + "   JOIN tests_conducted tc ON (bac.activity_id = text(tc.prescribed_id)) " 
      + "   WHERE bac.charge_id = bcc.charge_id " 
      + "     AND tc.patient_id=b.visit_id " 
      + "   GROUP BY bac.charge_id) AS footc ON (footc.charge_id = bcc.charge_id)"
      + " JOIN bill_claim bcl ON (b.bill_no = bcl.bill_no and bcl.claim_id = bccl.claim_id) "
      + " JOIN patient_registration pr ON (pr.patient_id = b.visit_id)"
      + " LEFT JOIN doctors doc ON (doc.doctor_id = pr.doctor)"
      + " LEFT JOIN doctors presc_doc ON (presc_doc.doctor_id = bcc.prescribing_dr_id)"
      + "   LEFT JOIN (" + "     SELECT 'Referal' AS doctor_type,referal_no, referal_name,"
      + " clinician_id AS doctor_license_number FROM referral" + "     UNION ALL"
      + "     SELECT 'Doctor' AS doctor_type,doctor_id, doctor_name,"
      + " doctor_license_number FROM doctors"
      + "     ) AS ref ON (ref.referal_no = pr.reference_docto_id)"
      + " JOIN insurance_claim ic ON (ic.claim_id = bcl.claim_id AND ic.patient_id = pr.patient_id)"
      + " LEFT JOIN mrd_supported_code_types msct ON (msct.code_type = bcc.code_type)"
      + "   LEFT JOIN insurance_denial_codes idc ON (idc.denial_code = bccl.denial_code)"
      + " LEFT JOIN doctors drs ON (drs.doctor_id = bcc.payee_doctor_id)"
      + " LEFT JOIN LATERAL (SELECT sum(bcci.insurance_claim_amt) as total_package_ins_claim_amt,"
      + " sum(bci.amount+bci.return_amt) as total_package_amount,"
      + " sum((coalesce(bcci.tax_amt, 0.00))) AS package_vat_net, bci.package_id, "
      + "    bci.bill_no FROM bill_charge_claim bcci "
      + "   JOIN bill_charge bci ON(bci.charge_id = bcci.charge_id) "
      + "   WHERE (bci.submission_batch_type='P' AND bci.charge_head<>'PKGPKG') "
      + "   GROUP BY bci.bill_no, bci.package_id) AS FOO "
      + " ON (FOO.package_id=bcc.package_id AND FOO.bill_no = bcc.bill_no) "
      + " LEFT join packages pkg ON (bcc.package_id = pkg.package_id) "
      + " WHERE (b.status != 'X') AND bcc.status!='X'"
      + " AND bcc.charge_head NOT IN ('PHMED','PHCMED','PHRET','PHCRET') ";

  /**
   * The Constant ORDER_BY_CHARGES.
   */
  public static final String ORDER_BY_CHARGES = " ORDER BY charge_id, main_visit_id, claim_id";

  /**
   * The Constant GROUP_BY_HOSPITAL_CHARGES.
   */
  public static final String GROUP_BY_HOSPITAL_CHARGES =
      " GROUP BY bccl.claim_activity_id,act_rate_plan_item_code,ser.activity_timing_eclaim, "
          + " posted_date::date , msct.haad_code,msct.code_type, "
          + " ic.main_visit_id, ic.claim_id,"
          + " b.status,pr.visit_type, coalesce(bccl.prior_auth_id, pip.prior_auth_id, ''),"
          + " pr.op_type, doc.doctor_license_number,"
          + " ref.doctor_license_number,doc.doctor_name,"
          + " ref.referal_name,doc.doctor_id,ref.referal_no, ref.doctor_type,bccl.claim_id,"
          + " issue_base_unit, CASE WHEN bcc.charge_group='DRG' THEN 'Y' ELSE 'N' END,"
          + " CASE WHEN bcc.charge_head='MARDRG' THEN 'Y' ELSE 'N' END,"
          + " ref_doctor_license_number, is_home_care_code, "
          + " CASE WHEN bcc.charge_group='PDM' THEN 'Y' ELSE 'N' END,pr.dept_name, "
          + " dpt.is_referral_doc_as_ordering_clinician#bccbill_no# , "
          + "bcc.submission_batch_type, foo.package_vat_net, FOO.total_package_ins_claim_amt, "
          + " FOO.total_package_claim_recd_total, "
          + " bctr.special_service_contract_name";

  /**
   * The get claim.
   */
  private static String GET_CLAIM = " SELECT ic.claim_id, ic.last_submission_batch_id,"
      + " ic.payers_reference_no, ic.main_visit_id, ic.patient_id, "
      + " ic.status, foo.resubmission_count, ic.closure_type,"
      + " ic.action_remarks, ic.resubmission_type, ic.comments " + " FROM insurance_claim ic "
      + " LEFT JOIN (SELECT COUNT(cs.submission_batch_id) AS resubmission_count,"
      + " cs.claim_id FROM insurance_claim ic "
      + "    JOIN claim_submissions cs ON (cs.claim_id = ic.claim_id) "
      + "    JOIN insurance_submission_batch isb"
      + "     ON (cs.submission_batch_id = isb.submission_batch_id) "
      + "    WHERE isb.is_resubmission = 'Y'"
      + " GROUP BY cs.claim_id) as foo ON (foo.claim_id = ic.claim_id)" 
      + "WHERE ic.claim_id = ? ";

  /**
   * Gets the claim by id.
   *
   * @param claimId the claim id
   * @return the claim by id
   */
  public BasicDynaBean getClaimById(String claimId) {
    return DatabaseHelper.queryToDynaBean(GET_CLAIM, new Object[] {claimId});
  }

  /**
   * The Constant GET_HOSPITAL_DENIED_CHARGE_NET_XML.
   */
  
  public static final String GET_HOSPITAL_DENIED_CHARGE_NET_XML =
      " SELECT distinct CASE WHEN bcc.submission_batch_type='P' THEN "
       + " FOO.total_package_ins_claim_amt - FOO.total_package_claim_recd_total ELSE "
       + " sum(coalesce(bccl.insurance_claim_amt, 0.00) "
       + " + coalesce(bcc.return_insurance_claim_amt, 0.00) "
       + " - coalesce(bccl.claim_recd_total, 0.00)) END AS net,"
       + " CASE WHEN bcc.submission_batch_type='P' THEN "
       + " FOO.package_vat_net ELSE sum((coalesce(bccl.tax_amt, 0.00))) END AS vat_net ";

  /**
   * The Constant GET_DEPT_FIELD_FOR_CLAIM_ACTIVITY.
   */
  private static final String GET_DEPT_FIELD_FOR_CLAIM_ACTIVITY =
      " , COALESCE(dpt.is_referral_doc_as_ordering_clinician,'N')"
          + "  AS is_referral_doc_as_ordering_clinician";

  /**
   * The Constant GET_ALL_HOSPITAL_CHARGES_FIELDS_XML.
   */
  public static final String GET_ALL_HOSPITAL_CHARGES_FIELDS_XML = " ,"
      + "   sum(bcc.act_quantity) AS qty,"
      + " sum(bccl.insurance_claim_amt) AS insurance_claim_amt, "
      + "   sum(bcc.amount) AS amt, "
      + "   sum(bcc.return_insurance_claim_amt) AS return_insurance_claim_amt, "
      + "   sum(bcc.return_amt) AS return_amt, " + "   sum(bcc.return_qty) AS return_qty, "
      + " CASE WHEN bcc.submission_batch_type='P' "
      + " THEN sum(bcc.amount + bcc.return_amt+ FOO.total_package_amount)"
      + " ELSE sum(bcc.amount + bcc.return_amt) END AS amount, "
      + "   sum(bcc.act_rate) AS rate,"
      + " sum(bcc.act_quantity + bcc.return_qty) AS quantity, "
      + "   sum(bcc.discount) as discount, " + "   bccl.claim_activity_id AS charge_id, "
      + "   max(bct.tax_rate) AS vat_tax_rate, " + " (act_rate_plan_item_code) as item_code,"
      + " 'A-'||bccl.claim_activity_id AS activity_charge_id,"
      + " 'Hospital' AS activity_group, " + "   max(posted_date) AS item_posted_date,"
      + "   to_char(max(posted_date), 'dd/MM/yyyy hh24:mi') AS posted_date,"
      + "   sum(coalesce(bccl.claim_recd_total, 0.00)) as claim_recd_total, "
      + "   array_to_string(array_agg(bcc.charge_id), ',') as charge_id_list, "
      + " msct.haad_code as act_type, msct.code_type AS act_type_desc,"
      + "   '' AS act_item_code, ic.main_visit_id, ic.claim_id,"
      + " b.status AS bill_status, pr.visit_type AS patient_visit_type,"
      + " COALESCE(bccl.prior_auth_id, pip.prior_auth_id, '') AS prior_auth_id, "
      + " CASE WHEN (pr.op_type != 'O') THEN doc.doctor_license_number"
      + " ELSE ref.doctor_license_number END AS doctor_license_number,"
      + " CASE WHEN (pr.op_type != 'O') THEN doc.doctor_name"
      + " ELSE ref.referal_name END AS doctor_name,"
      + " CASE WHEN (pr.op_type != 'O') THEN doc.doctor_id ELSE ref.referal_no END AS doctor_id,"
      + " CASE WHEN (pr.op_type != 'O') THEN 'Doctor' ELSE ref.doctor_type END AS doctor_type, "
      + " min(drs.doctor_license_number) AS conducting_dr_license_number,1 AS issue_base_unit,"
      + " min(presc_doc.doctor_license_number) AS prescribing_doctor_license_number,"
      + " CASE WHEN bcc.charge_group='DRG' THEN 'Y' ELSE 'N' END AS is_drg_group,"
      + "   CASE WHEN bcc.charge_head='MARDRG' THEN 'Y' ELSE 'N' END AS is_drg_charge, "
      + " CASE WHEN bcc.charge_group ='PDM' THEN 'Y' ELSE 'N' END as is_perdiem_code, "
      + "   ref.doctor_license_number as ref_doctor_license_number,"
      + " is_home_care_code,pr.dept_name,#bcc.bill_no# bctr.special_service_contract_name, "
      + " bcc.submission_batch_type, min(condrs.doctor_license_number) AS ip_cons_cond_dr_license, "
      + " CASE WHEN max(bcc.charge_group)='SNP' AND ser.activity_timing_eclaim='Y' THEN "
      + " CASE WHEN max(bcc.posted_date) >= coalesce(max(bcc.conducted_datetime), "
      + " max(bcc.posted_date)) THEN to_char(max(bcc.posted_date),'dd/MM/yyyy hh24:mi') "
      + " ELSE to_char(max(bcc.conducted_datetime),'dd/MM/yyyy hh24:mi') END "
      + " ELSE to_char(max(bcc.posted_date),'dd/MM/yyyy hh24:mi') END AS activity_start_datetime ";


  /**
   * Find all charges for XML.
   *
   * @param claimId the claim id
   * @param ignoreExternalPbm the ignore external pbm
   * @param isResubmission the is resubmission
   * @param checkDRGorPRD the check DR gor PRD
   * @return the list
   */
  public List<BasicDynaBean> findAllChargesForXML(String claimId, Boolean ignoreExternalPbm,
      Boolean isResubmission, Boolean checkDRGorPRD ,boolean needBillNoforXML) {
    StringBuilder chargesQuery = new StringBuilder();
    chargesQuery.append(ALL_CHARGES);
    if (isResubmission) {
      chargesQuery.append(GET_PHARMACY_DENIED_CHARGE_NET);
    } else {
      chargesQuery.append(GET_PHARMACY_CHARGE_NET_XML);
    }
    chargesQuery.append(GET_DEPT_FIELD_FOR_CLAIM_ACTIVITY 
        + GET_ALL_PHARMACY_CHARGES_FIELDS_XML
        .replace("#bc.bill_no#", needBillNoforXML ? "bc.bill_no," : "")
        + GET_ALL_PHARMACY_CHARGES_TABLES_XML
        + " AND CASE WHEN (?) OR s.allow_zero_claim = true OR bc.submission_batch_type='P'"
        + " THEN (coalesce(scl.insurance_claim_amt, 0.00)"
        + " + coalesce(scl.return_insurance_claim_amt, 0.00)) >= 0 "
        + " ELSE (coalesce(scl.insurance_claim_amt, 0.00)"
        + " + coalesce(scl.return_insurance_claim_amt, 0.00)) > 0 END "
        + " AND (s.quantity + s.return_qty) > 0  AND scl.claim_id = ? "
        + " AND (bc.submission_batch_type IS NULL "
        + " OR (  bc.submission_batch_type = 'P' "
        + " AND bc.charge_head='PKGPKG' ) OR ( bc.submission_batch_type = 'I' "
        + " AND bc.charge_head!='PKGPKG'))");
    if (isResubmission) {
      chargesQuery.append(
          " AND (coalesce(scl.insurance_claim_amt, 0.00) > coalesce(scl.claim_recd, 0.00)"
          + " OR coalesce(foop.total_package_ins_claim_amt, 0.00) > coalesce("
          + " foop.total_package_claim_recd_total,0.00)) "
              + " AND scl.closure_type != 'D' ");
    }
    if (ignoreExternalPbm) {
      // (This flag indicates that the sale bill claim will
      // be processed via an external software provider)
      chargesQuery.append(" AND sm.is_external_pbm = FALSE ");
    }

    chargesQuery.append(GROUP_BY_PHARMACY_CHARGES
        .replace("#bc.bill_no#", needBillNoforXML ? "bc.bill_no," : "")
        + " UNION ALL ");
    if (isResubmission) {
      chargesQuery.append(GET_HOSPITAL_DENIED_CHARGE_NET_XML);
    } else {
      chargesQuery.append(GET_HOSPITAL_CHARGE_NET_XML);
    }
    chargesQuery.append(GET_DEPT_FIELD_FOR_CLAIM_ACTIVITY 
        + GET_ALL_HOSPITAL_CHARGES_FIELDS_XML
        .replace("#bcc.bill_no#", needBillNoforXML ? "bcc.bill_no," : "")
        + GET_ALL_HOSPITAL_CHARGES_TABLES_XML
        + " AND CASE WHEN (?) OR bcc.allow_zero_claim = true OR bcc.submission_batch_type='P' "
        + " THEN (coalesce(bccl.insurance_claim_amt, 0.00)"
        + " + coalesce(bcc.return_insurance_claim_amt, 0.00)) >= 0 "
        + " ELSE (coalesce(bccl.insurance_claim_amt, 0.00)"
        + " + coalesce(bcc.return_insurance_claim_amt, 0.00)) > 0 END "
        + " AND (bcc.act_quantity + bcc.return_qty) > 0 " + " AND bccl.claim_id = ? "
        + " AND ( bcc.submission_batch_type IS NULL "
        + " OR (  bcc.submission_batch_type = 'P' "
        + " AND bcc.charge_head='PKGPKG' ) OR ( bcc.submission_batch_type = 'I' "
        + " AND bcc.charge_head!='PKGPKG')) ");

    if (isResubmission) {
      chargesQuery.append(
          " AND (coalesce(bccl.insurance_claim_amt, 0.00) > coalesce(bccl.claim_recd_total, 0.00)"
          + " OR coalesce(FOO.total_package_ins_claim_amt, 0.00) > coalesce("
          + " FOO.total_package_claim_recd_total, 0.00)) "
              + " AND bccl.closure_type != 'D' ");
    }
    chargesQuery.append(GROUP_BY_HOSPITAL_CHARGES
        .replace("#bccbill_no#", needBillNoforXML ? ",bcc.bill_no" : "") );
    return DatabaseHelper.queryToDynaList(chargesQuery + EXLCUDE_PACKAGES_WITH_ZERO_AMT 
        + ORDER_BY_CHARGES,
        new Object[] {checkDRGorPRD, claimId, checkDRGorPRD, claimId});
  }

  /**
   * The Constant GET_ALL_HOSPITAL_CHARGES_TABLES_XML.
   */
  public static final String GET_ALL_HOSPITAL_CHARGES_TABLES_XML = " FROM bill_charge bcc "
      + " LEFT JOIN services ser ON (bcc.charge_group = 'SNP' "
      + " AND bcc.act_description_id = ser.service_id) "
      + " JOIN bill_charge_claim bccl ON (bcc.charge_id = bccl.charge_id) "
      + " LEFT JOIN bill_charge_tax bct ON (bcc.charge_id = bct.charge_id) "
      + "   JOIN chargehead_constants chc ON (bcc.charge_head = chc.chargehead_id) "
      + "   JOIN chargegroup_constants cgc ON (bcc.charge_group = cgc.chargegroup_id)"
      + " JOIN bill b ON (b.bill_no = bcc.bill_no)"
      + " JOIN bill_claim bcl ON (b.bill_no = bcl.bill_no and bcl.claim_id = bccl.claim_id) "
      + " JOIN patient_registration pr ON (pr.patient_id = b.visit_id)"
      + " JOIN patient_insurance_plans pip"
      + " ON(pip.patient_id = bcl.visit_id AND pip.plan_id = bcl.plan_id AND "
      + "   pip.sponsor_id = bcl.sponsor_id) "
      + " LEFT JOIN doctors doc ON (doc.doctor_id = pr.doctor)"
      + " LEFT JOIN doctors presc_doc ON (presc_doc.doctor_id = bcc.prescribing_dr_id)"
      + "   LEFT JOIN (" + "     SELECT 'Referal' AS doctor_type,referal_no,"
      + "      referal_name, clinician_id AS doctor_license_number FROM referral"
      + "     UNION ALL" + "     SELECT 'Doctor' AS doctor_type,doctor_id,"
      + "   doctor_name, doctor_license_number FROM doctors"
      + "     ) AS ref ON (ref.referal_no = pr.reference_docto_id)"
      + " JOIN insurance_claim ic ON (ic.claim_id = bcl.claim_id AND ic.patient_id = pr.patient_id)"
      + " LEFT JOIN mrd_supported_code_types msct ON (msct.code_type = bcc.code_type)"
      + " LEFT JOIN insurance_denial_codes idc ON (idc.denial_code = bccl.denial_code)"
      + " LEFT JOIN doctors drs ON (drs.doctor_id = bcc.payee_doctor_id)"
      + "   LEFT JOIN per_diem_codes_master pcm"
      + "  ON (bcc.act_rate_plan_item_code = pcm.per_diem_code)"
      + " LEFT JOIN department dpt ON (dpt.dept_id = pr.dept_name) "
      + " LEFT JOIN LATERAL (SELECT sum(bcci.insurance_claim_amt) as total_package_ins_claim_amt,"
      + "  sum(coalesce(bcci.claim_recd_total,0.00)) as total_package_claim_recd_total, "
      + "  sum((coalesce(bcci.tax_amt, 0.00))) as package_vat_net,"
      + " SUM(bci.amount+bci.return_amt) as total_package_amount, bci.package_id,"
      + "   bci.bill_no FROM bill_charge_claim bcci  "
      + "  JOIN bill_charge bci ON(bci.charge_id = bcci.charge_id) "
      + "  WHERE (bci.submission_batch_type='P' AND bci.charge_head<>'PKGPKG') "
      + "  GROUP BY bci.bill_no, bci.package_id) AS FOO "
      + "  ON (FOO.package_id=bcc.package_id AND bcc.bill_no = FOO.bill_no) "
      + " LEFT JOIN packages pkg ON(pkg.package_id = bcc.package_id)"
      + " LEFT JOIN bill_charge_transaction bctr on (bcc.charge_id = bctr.bill_charge_id AND "
      + " bctr.special_service_contract_name is not null )"
      + " LEFT JOIN doctors condrs ON (condrs.doctor_id = bcc.payee_doctor_id) "
      + " AND bcc.charge_group='DOC' AND pr.visit_type = 'i' "
      + " WHERE (b.status != 'X' AND (b.status = 'F' OR b.status = 'C')) "
      + " AND bcc.status!='X' AND bcc.charge_head"
      + " NOT IN ('PHMED','PHCMED','PHRET','PHCRET','ADJDRG') ";

  /**
   * The Constant FIND_CLAIM_OBSERVATIONS_WITHOUT_PRESENTING_COMPLAINT.
   */
  private static final String FIND_CLAIM_OBSERVATIONS_WITHOUT_PRESENTING_COMPLAINT = "SELECT  "
      + "  value, value_type, observation_type AS type, code,"
      + "  mo.charge_id, mo.document_id, pd.doc_content_bytea as file_bytes"
      + " FROM mrd_observations mo" + " JOIN bill_charge bc ON (mo.charge_id= bc.charge_id) "
      + " JOIN bill_claim bcl ON (bc.bill_no = bcl.bill_no  AND bcl.claim_id = ? ) "
      + " LEFT JOIN patient_documents pd ON (pd.doc_id = mo.document_id) "
      + " WHERE  code != 'Presenting-Complaint' AND (mo.sponsor_id = ? "
      + " OR mo.sponsor_id is NULL OR mo.sponsor_id='' ) ";

  /**
   * The Constant FIND_CLAIM_OBSERVATIONS.
   */
  private static final String FIND_CLAIM_OBSERVATIONS = "SELECT  "
      + "  value, value_type, observation_type AS type, code,"
      + " mo.charge_id, mo.document_id, pd.doc_content_bytea as file_bytes "
      + " FROM mrd_observations mo" + " JOIN bill_charge bc ON (mo.charge_id= bc.charge_id) "
      + " JOIN bill_claim bcl ON (bc.bill_no = bcl.bill_no  AND bcl.claim_id = ? ) "
      + " LEFT JOIN patient_documents pd ON (pd.doc_id = mo.document_id) "
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
          FIND_CLAIM_OBSERVATIONS_WITHOUT_PRESENTING_COMPLAINT, claimId, sponsorId);
    } else {
      return DatabaseHelper.queryToDynaList(FIND_CLAIM_OBSERVATIONS, claimId, sponsorId);
    }
  }

  /**
   * The Constant ALL_CHARGES.
   */
  public static final String ALL_CHARGES = "SELECT * FROM ( ";

  /**
   * The Constant EXLCUDE_PACKAGES_WITH_ZERO_AMT.
   */
  /* Excluding the Package Charge Activity if the Claim Amt is zero 
   * and Submission Batch Type is P */
  public static final String EXLCUDE_PACKAGES_WITH_ZERO_AMT = ") AS FINAL WHERE "
        + " CASE WHEN submission_batch_type = 'P' " 
        + " THEN  net > 0  ELSE submission_batch_type IS NULL OR submission_batch_type='I' END";

  /**
   * The Constant CHARGES_QUERY_FOR_RESUB.
   */
  private static final String CHARGES_QUERY_FOR_RESUB = GET_PHARMACY_RESUB_CHARGE_NET
      + GET_ALL_PHARMACY_CHARGES_FIELDS + GET_ALL_PHARMACY_CHARGES_TABLES
      + " AND CASE WHEN (?) OR s.allow_zero_claim = true OR bc.submission_batch_type='P'"
      + " THEN (coalesce(scl.insurance_claim_amt, 0.00)"
      + " + coalesce(scl.return_insurance_claim_amt, 0.00)) >= 0 "
      + " ELSE (coalesce(scl.insurance_claim_amt, 0.00)"
      + " + coalesce(scl.return_insurance_claim_amt, 0.00)) > 0 END"
      + " AND (s.quantity + s.return_qty) > 0 "
      + " AND (coalesce(scl.insurance_claim_amt, 0.00) != coalesce(scl.claim_recd, 0.00))"
      + " AND b.bill_no = ? AND scl.claim_id = ? " + " AND scl.closure_type != 'D' "

      + " UNION ALL "

      + GET_HOSPITAL_DENIED_CHARGE_RESUB + GET_ALL_HOSPITAL_CHARGES_FIELDS
      + GET_ALL_HOSPITAL_CHARGES_TABLES + " AND CASE WHEN (?) OR bcc.allow_zero_claim = true"
      + " OR bcc.submission_batch_type='P' "
      + " THEN (coalesce(bccl.insurance_claim_amt, 0.00)"
      + " + coalesce(bcc.return_insurance_claim_amt, 0.00)) >= 0 "
      + " ELSE (coalesce(bccl.insurance_claim_amt, 0.00)"
      + " + coalesce(bcc.return_insurance_claim_amt, 0.00)) > 0 END"
      + " AND (bcc.act_quantity + bcc.return_qty) > 0 "
      + " AND (coalesce(bccl.insurance_claim_amt, 0.00) != coalesce(bccl.claim_recd_total, 0.00))"
      + " AND b.bill_no = ? AND bccl.claim_id = ? " + " AND bccl.closure_type != 'D' "
      + " AND ( bcc.submission_batch_type IS NULL "
      + " OR ( bcc.submission_batch_type = 'P' AND bcc.charge_head='PKGPKG') "
      + " OR ( bcc.submission_batch_type = 'I' AND bcc.charge_head!='PKGPKG')) " ;

  /**
   * The Constant CHARGES_QUERY.
   */
  private static final String CHARGES_QUERY = GET_PHARMACY_CHARGE_NET
      + GET_ALL_PHARMACY_CHARGES_FIELDS + GET_ALL_PHARMACY_CHARGES_TABLES
      + " AND CASE WHEN (?) OR s.allow_zero_claim = true"
      + " THEN (coalesce(scl.insurance_claim_amt, 0.00)"
      + " + coalesce(scl.return_insurance_claim_amt, 0.00)) >= 0 "
      + " ELSE (coalesce(scl.insurance_claim_amt, 0.00)"
      + " + coalesce(scl.return_insurance_claim_amt, 0.00)) > 0 END"
      + " AND (s.quantity + s.return_qty) > 0 " + " AND b.bill_no = ? and scl.claim_id = ? "

      + " UNION ALL "

      + GET_HOSPITAL_CHARGE_NET + GET_ALL_HOSPITAL_CHARGES_FIELDS
      + GET_ALL_HOSPITAL_CHARGES_TABLES + " AND CASE WHEN (?) OR bcc.allow_zero_claim = true "
      + " OR bcc.submission_batch_type = 'P' "
      + " THEN (coalesce(bccl.insurance_claim_amt, 0.00)"
      + " + coalesce(bcc.return_insurance_claim_amt, 0.00)) >= 0 "
      + " ELSE (coalesce(bccl.insurance_claim_amt, 0.00)"
      + " + coalesce(bcc.return_insurance_claim_amt, 0.00)) > 0 END"
      + " AND (bcc.act_quantity + bcc.return_qty) > 0 "
      + " AND b.bill_no = ? and bccl.claim_id = ? "
      + " AND ( bcc.submission_batch_type IS NULL "
      + " OR ( bcc.submission_batch_type = 'P' AND bcc.charge_head='PKGPKG') "
      + " OR ( bcc.submission_batch_type = 'I' AND bcc.charge_head!='PKGPKG')) ";

  /**
   * Find all charges.
   *
   * @param billNo the bill no
   * @param claimId the claim id
   * @param isResubmission the is resubmission
   * @param checkDrgOrPerDiem the check drg or per diem
   * @return the list
   * @throws SQLException the SQL exception
   */
  public List<BasicDynaBean> findAllCharges(String billNo, String claimId,
      Boolean isResubmission, Boolean checkDrgOrPerDiem) throws SQLException {
    if (isResubmission) {
      return DataBaseUtil.queryToDynaList(ALL_CHARGES + CHARGES_QUERY_FOR_RESUB 
        + EXLCUDE_PACKAGES_WITH_ZERO_AMT + ORDER_BY_CHARGES,
          new Object[] {checkDrgOrPerDiem, billNo, claimId, checkDrgOrPerDiem, billNo,
              claimId});
    } else {
      return DataBaseUtil.queryToDynaList(ALL_CHARGES + CHARGES_QUERY 
        + EXLCUDE_PACKAGES_WITH_ZERO_AMT + ORDER_BY_CHARGES, new Object[] {
          checkDrgOrPerDiem, billNo, claimId, checkDrgOrPerDiem, billNo, claimId});
    }
  }
  
  private static String UPDATE_CLAIM_CLOSE_SPONSOR_WRITEOFF =  "UPDATE insurance_claim ic "
          + " SET status = ? "
          + " FROM bill_claim bcl "
          + " WHERE bcl.bill_no = ? AND bcl.claim_id = ic.claim_id";
  /**
   * update the status.
   *
   * @param billNo the bill no
   * @update the status 
   */
  
  public Integer updateSponsorWriteOffClaimClose(String billNo ,String billStatus) {
    return DatabaseHelper.update(UPDATE_CLAIM_CLOSE_SPONSOR_WRITEOFF, new Object[] {billStatus, 
        billNo});
  }
}
