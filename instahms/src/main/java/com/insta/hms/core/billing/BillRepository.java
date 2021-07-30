package com.insta.hms.core.billing;

import com.bob.hms.common.DateUtil;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.patient.registration.PatientInsurancePlansService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Repository
public class BillRepository extends GenericRepository {

  @LazyAutowired
  private PatientInsurancePlansService patInsPlanService;

  public BillRepository() {
    super("bill");
  }

  private static final String ADT_BILL_AND_PAYMENT_STATUS = "SELECT * FROM adt_bill_and_discharge_status_view WHERE visit_id = ?";

  private static final String OP_BILL_AND_PAYMENT_STATUS = "SELECT * FROM op_bill_and_discharge_status_view WHERE visit_id = ?";

  public BasicDynaBean getBillAndPaymentStatus(String visitId, String visitType) {
    if (visitType.equals("i"))
      return DatabaseHelper.queryToDynaBean(ADT_BILL_AND_PAYMENT_STATUS, visitId);
    else if (visitType.equals("o"))
      return DatabaseHelper.queryToDynaBean(OP_BILL_AND_PAYMENT_STATUS, visitId);
    else
      return null;
  }

  private static final String GET_LATEST_OPEN_LATER_NOW_BILL = " SELECT b.* FROM bill b WHERE "
      + " visit_id = ? and b.status = 'A' and b.payment_status = 'U' and b.restriction_type='N' "
      + " ORDER BY bill_type, open_date desc limit 1";

  private static final String GET_PRIMARY_OPEN_LATER_NOW_BILL = " SELECT b.* FROM bill b WHERE "
      + " visit_id = ? and b.status = 'A' and b.payment_status = 'U' and b.restriction_type='N' "
      + " and b.is_primary_bill='Y' limit 1";

  public BasicDynaBean getPrimaryOpenBillLaterElseBillNow(String visitId) {
    return DatabaseHelper.queryToDynaBean(GET_PRIMARY_OPEN_LATER_NOW_BILL, new Object[] { visitId });
  }

  public BasicDynaBean getLatestOpenBillLaterElseBillNow(String visitId) {
    return DatabaseHelper.queryToDynaBean(GET_LATEST_OPEN_LATER_NOW_BILL, new Object[] { visitId });
  }

  private static final String SET_PRIMARY_CONDITIONAL = " UPDATE bill b SET is_primary_bill='Y' "
      + " WHERE bill_no=? "
      + "   AND NOT EXISTS (SELECT bill_no FROM bill b2 WHERE b2.visit_id=b.visit_id "
      + "     AND b2.bill_no != b.bill_no AND b2.bill_type='C' AND b2.is_primary_bill='Y')";

  public int setPrimaryBillConditional(String billNo) {
    return DatabaseHelper.update(SET_PRIMARY_CONDITIONAL, billNo);
  }

  private static final String BILL_SEQUENCE_PATTERN = " SELECT pattern_id FROM hosp_bill_seq_prefs "
      + "  WHERE (bill_type = ? or bill_type ='*') AND "
      + "  (visit_type = ? or visit_type = '*') AND "
      + "  (restriction_type = ? OR restriction_type = '*') AND "
      + "  (center_id = ? OR center_id = 0) AND "
      + "  (is_tpa = ? OR is_tpa = '*') AND "
      + "  (is_credit_note =? OR is_credit_note ='*') order by center_id desc, priority limit 1; ";

  public String getNextPatternId(Object[] obj) {
    return DatabaseHelper.getNextPatternId(DatabaseHelper.getString(BILL_SEQUENCE_PATTERN, obj));
  }
  
  private static final String GET_MAIN_AND_FOLLOWUP_VISIT_BILLS_CHARGES =  "SELECT * FROM ( "+
      " SELECT bc.charge_id, # as amount, bc.discount, bc.charge_group, bc.charge_head, bc.act_description_id as act_description_id, "+
      "   CASE WHEN (CASE WHEN bc.charge_group='PKG' THEN (select insurance_payable from " +
      " chargehead_constants where chargehead_id = 'PKGPKG') ELSE chc.insurance_payable END)='Y' "
      + " THEN true ELSE false END AS is_charge_head_payable, "+
      "   bc.insurance_claim_amount, bc.insurance_category_id, bc.is_claim_locked, bc.include_in_claim_calc, "+
      "   bc. copay_ded_adj, bc.max_copay_adj, bc.sponsor_limit_adj, bc.copay_perc_adj, 'hospital' as charge_type, "+
      " CASE WHEN bc.charge_head = 'INVITE' THEN scm.claimable ELSE true END AS store_item_category_payable, bc.amount_included, " +
      "   bc.consultation_type_id, bc.op_id, bc.item_excluded_from_doctor, bc.item_excluded_from_doctor_remarks,"+
      " tpa.claim_amount_includes_tax,tpa.limit_includes_tax,b.bill_no,b.is_tpa, "+
      "   bc.package_id FROM bill b "+
      "   JOIN bill_charge bc ON(b.bill_no = bc.bill_no) "+
      " LEFT JOIN bill_claim pbcl ON(pbcl.bill_no = b.bill_no AND pbcl.priority=1) "+
      " LEFT JOIN tpa_master tpa ON(tpa.tpa_id = pbcl.sponsor_id) "+
      "   JOIN patient_registration pr ON(pr.patient_id = b.visit_id) "+
      "   JOIN chargehead_constants chc ON(chc.chargehead_id = bc.charge_head) "+
      " LEFT JOIN store_item_details sid ON(sid.medicine_id::text = bc.act_description_id AND bc.charge_head='INVITE') "+
      " LEFT JOIN store_category_master scm ON(scm.category_id = sid.med_category_id) "+
      "   WHERE (pr.patient_id in(##)) AND b.is_tpa AND pr.op_type IN ('M', 'F','D','R','O') AND " +
      "   bc.charge_head NOT IN('PHCMED','PHMED','PHRET','PHCRET','INVRET') AND bc.status != 'X' AND b.total_amount >= 0"+
      "   UNION ALL "+
      "   SELECT bc.charge_id || '-' || ssd.sale_item_id as charge_id, @ as amount, 0 as discount, bc.charge_group, bc.charge_head, "+
      "   ssd.medicine_id::text as act_description_id, CASE WHEN (CASE WHEN bc.charge_group='PKG'" +
      " THEN (select insurance_payable from chargehead_constants where chargehead_id = 'PKGPKG') " +
      " ELSE chc.insurance_payable END)='Y' "
      + " THEN true ELSE false END AS is_charge_head_payable, "+
      "   ssd.insurance_claim_amt as insurance_claim_amount, ssd.insurance_category_id, ssd.is_claim_locked, ssd.include_in_claim_calc, "+
      " 0 AS copay_ded_adj, 0 AS max_copay_adj, 0 AS sponsor_limit_adj, 0 AS copay_perc_adj, 'pharmacy' as charge_type, "+
      " true, ssd.amount_included, bc.consultation_type_id, bc.op_id, bc.item_excluded_from_doctor, bc.item_excluded_from_doctor_remarks, "+
      " tpa.claim_amount_includes_tax,tpa.limit_includes_tax,b.bill_no,b.is_tpa, "+
      "   bc.package_id FROM bill b "+
      "   JOIN store_sales_main ssm ON(b.bill_no = ssm.bill_no AND ssm.type = 'S') "+
      "   JOIN bill_charge bc ON(bc.charge_id = ssm.charge_id) "+
      " LEFT JOIN bill_claim pbcl ON(pbcl.bill_no = b.bill_no AND pbcl.priority=1) "+
      " LEFT JOIN tpa_master tpa ON(tpa.tpa_id = pbcl.sponsor_id) "+
      "   JOIN store_sales_details ssd ON(ssm.sale_id = ssd.sale_id) "+
      "   JOIN chargehead_constants chc ON(chc.chargehead_id = bc.charge_head) "+
      "   JOIN patient_registration pr ON(pr.patient_id = b.visit_id) "+
      "   WHERE (pr.patient_id in(##)) AND b.is_tpa AND pr.op_type IN ('M', 'F','D','R','O') AND "+
      "   bc.status != 'X' AND b.total_amount >= 0) AS foo ORDER BY foo.charge_id ";

  private static final String GET_VISIT_BILLS_CHARGES = "SELECT * FROM ( "
      + "	SELECT bc.charge_id, # as amount, bc.discount, bc.charge_group, bc.charge_head, bc.act_description_id as act_description_id, "
      + " 	CASE WHEN (CASE WHEN bc.charge_group='PKG' THEN (select insurance_payable from "
      + " chargehead_constants where chargehead_id = 'PKGPKG') ELSE chc.insurance_payable END)='Y' "
      + " THEN true ELSE false "
      + " END AS is_charge_head_payable, "
      + " 	bc.insurance_claim_amount, bc.insurance_category_id, bc.is_claim_locked, bc.include_in_claim_calc,  "
      + "   bc. copay_ded_adj, bc.max_copay_adj, bc.sponsor_limit_adj, bc.copay_perc_adj, 'hospital' as charge_type, "
      + "	CASE WHEN bc.charge_head = 'INVITE' THEN scm.claimable ELSE true END AS store_item_category_payable, bc.amount_included, bc.consultation_type_id, bc.op_id,"
      + " bc.item_excluded_from_doctor, bc.item_excluded_from_doctor_remarks,"
      + "	tpa.claim_amount_includes_tax,tpa.limit_includes_tax,b.bill_no,b.is_tpa, "
      + " 	bc.package_id FROM bill b "
      + " 	JOIN bill_charge bc ON(b.bill_no = bc.bill_no) "
      + "	LEFT JOIN bill_claim pbcl ON(pbcl.bill_no = b.bill_no AND pbcl.priority=1) "
      + "	LEFT JOIN tpa_master tpa ON(tpa.tpa_id = pbcl.sponsor_id) "
      + " 	JOIN chargehead_constants chc ON(chc.chargehead_id = bc.charge_head) "
      + "	LEFT JOIN store_item_details sid ON(sid.medicine_id::text = bc.act_description_id AND bc.charge_head='INVITE') "
      + "	LEFT JOIN store_category_master scm ON(scm.category_id = sid.med_category_id) "
      + " 	WHERE b.visit_id = ? AND b.is_tpa AND "
      + " 	bc.charge_head NOT IN('PHCMED','PHMED','PHRET','PHCRET','INVRET') AND bc.status != 'X' AND b.total_amount >= 0 "
      + " 	UNION ALL "
      + " 	SELECT bc.charge_id || '-' || ssd.sale_item_id as charge_id, @ as amount, 0 as discount, bc.charge_group, bc.charge_head,"
      + "   ssd.medicine_id::text as act_description_id, CASE WHEN (CASE WHEN bc.charge_group='PKG' "
      + "   THEN (select insurance_payable from chargehead_constants where chargehead_id = 'PKGPKG') "
      + "   ELSE chc.insurance_payable END)='Y' THEN true ELSE false END AS is_charge_head_payable, "
      + " 	ssd.insurance_claim_amt as insurance_claim_amount, ssd.insurance_category_id, ssd.is_claim_locked, ssd.include_in_claim_calc,  "
      + "   bc. copay_ded_adj, bc.max_copay_adj, bc.sponsor_limit_adj, bc.copay_perc_adj, 'pharmacy' as charge_type, "
      + "	true AS store_item_category_payable, ssd.amount_included, bc.consultation_type_id, bc.op_id, bc.item_excluded_from_doctor,"
      + "   bc.item_excluded_from_doctor_remarks,"
      + "	tpa.claim_amount_includes_tax,tpa.limit_includes_tax,b.bill_no,b.is_tpa,  "
      + " 	bc.package_id FROM bill b "
      + " 	JOIN store_sales_main ssm ON(b.bill_no = ssm.bill_no AND ssm.type = 'S') "
      + " 	JOIN bill_charge bc ON(bc.charge_id = ssm.charge_id) "
      + "	LEFT JOIN bill_claim pbcl ON(pbcl.bill_no = b.bill_no AND pbcl.priority=1) "
      + "	LEFT JOIN tpa_master tpa ON(tpa.tpa_id = pbcl.sponsor_id) "
      + " 	JOIN store_sales_details ssd ON(ssm.sale_id = ssd.sale_id) "
      + " 	JOIN chargehead_constants chc ON(chc.chargehead_id = bc.charge_head) "
      + " 	WHERE b.visit_id = ? AND b.is_tpa AND bc.status != 'X'  AND b.total_amount >= 0 "
      + " 	 ) AS foo ORDER BY foo.charge_id ";

  public List<BasicDynaBean> getVisitBillCharges(String visitId, Boolean includeFollowUpVisits, String followUpVisitIds) {

    String isClaimAmtIncludesTax = "N";
    String isLimitIncludesTax = "N";

    List<BasicDynaBean> planList = patInsPlanService.getPlanDetails(visitId);

    for (BasicDynaBean planBean : planList) {
      isClaimAmtIncludesTax = (String) planBean.get("claim_amount_includes_tax");
      isLimitIncludesTax = (String) planBean.get("limit_includes_tax");
      break;
    }

    String query = includeFollowUpVisits ? GET_MAIN_AND_FOLLOWUP_VISIT_BILLS_CHARGES : GET_VISIT_BILLS_CHARGES;

    if (isClaimAmtIncludesTax.equals("Y") && isLimitIncludesTax.equals("Y")) {
      query = query.replaceAll(" # ", " (bc.amount+bc.return_amt+bc.original_tax_amt+bc.return_original_tax_amt) ");
      query = query.replaceAll(" @ ", " (ssd.amount+ssd.return_amt) "); // ssd.amount is already has
                                                                        // tax amount
    } else {
      query = query.replaceAll(" # ", " (bc.amount+bc.return_amt) ");
      query = query.replaceAll(" @ ", " (ssd.amount+ssd.return_amt-(ssd.tax+ssd.return_tax_amt)) ");
    }

    Object[] object;
    if(includeFollowUpVisits){
      query = query.replaceAll("##", followUpVisitIds);
      object = new Object[]{};
    }else{
      object = new Object[]{visitId,visitId};
    }

    return DatabaseHelper.queryToDynaList(query, object);

  }

  private static final String GET_MAIN_AND_FOLLOWUP_VISIT_BILL_CHARGE_CLAIM = " SELECT * FROM ( "
      + " 	SELECT bcc.charge_id, bcc.claim_id, bcc.sponsor_id, bcc.insurance_claim_amt, "
      + " 	bcc.copay_ded_adj, bcc.max_copay_adj, bcc.sponsor_limit_adj, bcc.copay_perc_adj, bcc.insurance_category_id, "
      + "	bcc.include_in_claim_calc, COALESCE(bcc.tax_amt, 0) as tax_amt "
      + "	FROM bill b "
      + "	JOIN bill_claim bcl ON(b.bill_no = bcl.bill_no) "
      + "	JOIN bill_charge_claim bcc ON(bcl.bill_no = bcc.bill_no AND bcl.claim_id = bcc.claim_id) "
      + "	JOIN bill_charge bc ON(bc.charge_id = bcc.charge_id) "
      + "	JOIN patient_registration pr ON(pr.patient_id = bcl.visit_id) "
      + " 	WHERE (pr.patient_id in(##)) AND bcl.plan_id = ? AND pr.op_type IN ('M', 'F','D','R','O') AND "
      + "	bcc.charge_head NOT IN('PHCMED','PHMED','PHRET','PHCRET','INVRET') AND bc.status != 'X' AND b.total_amount >= 0 "
      + "	UNION ALL "
      + "	SELECT bc.charge_id || '-' || ssd.sale_item_id as charge_id, scd.claim_id, scd.sponsor_id, scd.ref_insurance_claim_amount as insurance_claim_amt,  "
      + "	0 as copay_ded_adj, 0 as max_copay_adj, 0 as sponsor_limit_adj, 0 as copay_perc_adj, scd.insurance_category_id, "
      + "   scd.include_in_claim_calc, 0.00 as tax_amt "
      + "   FROM bill b "
      + "	JOIN bill_claim bcl ON(b.bill_no = bcl.bill_no) "
      + "	JOIN store_sales_main ssm ON(bcl.bill_no = ssm.bill_no AND ssm.type = 'S') "
      + "	JOIN bill_charge bc ON(ssm.charge_id = bc.charge_id) "
      + "	JOIN store_sales_details ssd ON(ssd.sale_id = ssm.sale_id) "
      + "	JOIN sales_claim_details scd ON(scd.sale_item_id = ssd.sale_item_id AND bcl.claim_id = scd.claim_id) "
      + "	JOIN patient_registration pr ON(pr.patient_id = bcl.visit_id) "
      + "	WHERE (pr.patient_id in(##)) AND bcl.plan_id = ? AND pr.op_type IN ('M', 'F','D','R','O') "
      + "   AND bc.status != 'X' AND b.total_amount >= 0 " + "	) AS foo ";

  private static final String GET_VISIT_BILL_CHARGE_CLAIM = " SELECT * FROM ( "
      + "  SELECT bcc.charge_id, bcc.claim_id, bcc.sponsor_id, bcc.insurance_claim_amt,  "
      + "  bcc.copay_ded_adj, bcc.max_copay_adj, bcc.sponsor_limit_adj, bcc.copay_perc_adj, bcc.insurance_category_id, "
      + "  bcc.include_in_claim_calc,COALESCE(bcc.tax_amt, 0) as tax_amt "
      + "   FROM bill b "
      + "	JOIN bill_claim bcl ON(b.bill_no = bcl.bill_no) "
      + "	JOIN bill_charge_claim bcc ON(bcl.bill_no = bcc.bill_no AND bcl.claim_id = bcc.claim_id) "
      + "	JOIN bill_charge bc ON(bc.charge_id = bcc.charge_id) "
      + " 	WHERE bcl.visit_id = ? AND bcl.plan_id = ?  AND "
      + "	bcc.charge_head NOT IN('PHCMED','PHMED','PHRET','PHCRET','INVRET') AND bc.status != 'X'  AND b.total_amount >= 0"
      + "	UNION ALL "
      + "	SELECT bc.charge_id || '-' || ssd.sale_item_id as charge_id, scd.claim_id, scd.sponsor_id, scd.ref_insurance_claim_amount as insurance_claim_amt,  "
      + "	0 as copay_ded_adj, 0 as max_copay_adj, 0 as sponsor_limit_adj, 0 as copay_perc_adj, scd.insurance_category_id, "
      + "	scd.include_in_claim_calc, 0.00 as tax_amt "
      + "   FROM bill b "
      + "	JOIN bill_claim bcl ON(b.bill_no = bcl.bill_no) "
      + "	JOIN store_sales_main ssm ON(bcl.bill_no = ssm.bill_no AND ssm.type = 'S') "
      + "	JOIN bill_charge bc ON(ssm.charge_id = bc.charge_id) "
      + "	JOIN store_sales_details ssd ON(ssd.sale_id = ssm.sale_id) "
      + "	JOIN sales_claim_details scd ON(scd.sale_item_id = ssd.sale_item_id AND bcl.claim_id = scd.claim_id) "
      + "	WHERE bcl.visit_id = ? AND bcl.plan_id = ?  AND bc.status != 'X'  AND b.total_amount >= 0 "
      + "	) AS foo  ";

  public List<BasicDynaBean> getVisitBillChargeClaims(String visitId, int planId, Boolean includeFollowUpVisits, String followUpVisitIds) {
    String query = includeFollowUpVisits ? GET_MAIN_AND_FOLLOWUP_VISIT_BILL_CHARGE_CLAIM : GET_VISIT_BILL_CHARGE_CLAIM;
    Object[] object;
    if(includeFollowUpVisits){
      query = query.replaceAll("##", followUpVisitIds);
      object = new Object[]{planId, planId};
    }else{
      object = new Object[]{visitId,planId,visitId,planId};
    }

    return DatabaseHelper.queryToDynaList(query, object);
  }

  private static final String GET_PATIENTDUE = "SELECT total_amount-primary_total_claim-secondary_total_claim-total_receipts-deposit_set_off-points_redeemed_amt "
      + " FROM bill WHERE bill_no=? ";

  public static BigDecimal getPatientDue(String billNo) {
    return DatabaseHelper.getBigDecimal(GET_PATIENTDUE, billNo);
  }

  private static final String GET_NET_PATIENTDUE = "  SELECT CASE WHEN creditNote.bill_no IS NOT NULL AND creditNote.bill_no != '' THEN b.total_amount-b.primary_total_claim-b.secondary_total_claim-b.total_receipts-b.deposit_set_off-b.points_redeemed_amt + creditNote.total_credit_amount - creditNote.total_credit_claim ELSE b.total_amount-b.primary_total_claim-b.secondary_total_claim-b.total_receipts-b.deposit_set_off-b.points_redeemed_amt END AS net_patient_due "
      + "  FROM bill b "
      + "  LEFT JOIN ( SELECT bcn.bill_no , sum(cn.total_amount) AS total_credit_amount, sum(cn.total_claim) AS total_credit_claim "
      + "  FROM bill_credit_notes bcn "
      + "  LEFT JOIN bill cn ON(bcn.credit_note_bill_no = cn.bill_no ) GROUP BY bcn.bill_no  ) "
      + "  AS creditNote ON(creditNote.bill_no = b.bill_no)" + "  WHERE b.bill_no=?";

  public static BigDecimal getNetPatientDue(String billNo) {
    return DatabaseHelper.getBigDecimal(GET_NET_PATIENTDUE, billNo);
  }

  private static final String GET_DISCHARGE_NOTOK_BILLS = "SELECT bill_no FROM bill "
      + " WHERE discharge_status != 'Y'  AND visit_id=? AND status != 'X' ";

  public List<BasicDynaBean> getOkToDischarge(String patientId) {
    return DatabaseHelper.queryToDynaList(GET_DISCHARGE_NOTOK_BILLS, new Object[] { patientId });
  }

  private static final String GET_BILL_AMOUNTS = "SELECT bill_no, total_amount, total_discount, total_claim, total_claim_return, insurance_deduction, "
      + " deposit_set_off, points_redeemed_amt, total_receipts, "
      + " primary_total_sponsor_receipts, secondary_total_sponsor_receipts,"
      + " claim_recd_amount, total_claim_return, primary_total_claim, secondary_total_claim "
      + " FROM bill WHERE bill_no=?";

  public BasicDynaBean getBillAmounts(String billNo) {
    return DatabaseHelper.queryToDynaBean(GET_BILL_AMOUNTS, new Object[] { billNo });
  }

  // may not be required as we should be updating sponsor_writeoff_remarks along with
  // updateSponsorWriteOff().
  private final String UPDATE_AUTO_CLOSE_CLAIM_SPONSOR_WRITEOFF_REMARKS = "UPDATE bill b "
      + " SET sponsor_writeoff_remarks = 'Denial accepted with amount difference. Closing the bill.' "
      + "	FROM bill_claim bcl "
      + " JOIN insurance_claim ic ON(bcl.claim_id = ic.claim_id) "
      + " JOIN insurance_remittance_details ird ON(bcl.claim_id = ird.claim_id AND ird.remittance_id = ?) "
      + " WHERE  b.bill_no = bcl.bill_no AND ic.status = 'C' AND ic.closure_type = 'D' ";

  // updates bill sponsor writeoff remarks for Excess sponsor payments
  private final String UPDATE_EXCESS_AUTO_CLOSE_CLAIM_SPONSOR_WRITEOFF_REMARKS = "UPDATE bill b "
      + " SET sponsor_writeoff_remarks = 'Excess payment received.' "
      + "	FROM bill_claim bcl "
      + " JOIN insurance_claim ic ON(bcl.claim_id = ic.claim_id) "
      + " JOIN insurance_remittance_details ird ON(bcl.claim_id = ird.claim_id AND ird.remittance_id = ?) "
      + "	JOIN ("
      + "			SELECT bcc.claim_id, sum(insurance_claim_amt) -	sum(claim_recd_total) as sponsor_due "
      + "				FROM bill_charge_claim bcc "
      + "				JOIN insurance_remittance_details ird ON(bcc.claim_id = ird.claim_id AND remittance_id = ?)"
      + "				GROUP BY bcc.claim_id"
      + "		) as foo ON (foo.claim_id = ird.claim_id)"
      + " WHERE  b.bill_no = bcl.bill_no AND ic.status = 'C' AND ic.closure_type = 'F' AND foo.sponsor_due < 0 ";

  // updates bill remarks if claims are closed but there is pending patient due
  private final String UPDATE_PATIENT_DUE_BILL_REMARKS = "UPDATE bill b "
      + "			 SET remarks = 'Patient Due pending. Cannot close bill yet.' "
      + "				FROM bill_claim bcl "
      + "			 JOIN insurance_claim ic ON(bcl.claim_id = ic.claim_id) "
      + "			 JOIN insurance_remittance_details ird ON(bcl.claim_id = ird.claim_id AND ird.remittance_id = ?) "
      + "				LEFT JOIN ("
      + "						 SELECT bcn.bill_no, SUM(b.total_amount) as total_amount,"
      + "						 SUM(b.total_claim) as total_claim, SUM(b.total_amount-b.total_claim) as total_pat_amt "
      + "						 FROM bill_credit_notes bcn "
      + "						 JOIN bill b ON(bcn.credit_note_bill_no = b.bill_no) "
      + "						 WHERE b.status!='X' GROUP BY bcn.bill_no "
      + "					) as crnote ON (crnote.bill_no = bcl.bill_no )"
      + "				WHERE b.bill_no = bcl.bill_no  "
      + "				AND (b.total_amount - b.total_claim  + coalesce(crnote.total_amount,0) "
      + "						- total_receipts - deposit_set_off - points_redeemed_amt > 0) "
      + "					AND b.patient_writeoff ='N'" + "					AND ird.remittance_id = ?";

  public void updateRemarksOnAutoCloseClaim(Integer remittanceId) {
    DatabaseHelper.update(UPDATE_AUTO_CLOSE_CLAIM_SPONSOR_WRITEOFF_REMARKS,
        new Object[] { remittanceId });
    DatabaseHelper.update(UPDATE_EXCESS_AUTO_CLOSE_CLAIM_SPONSOR_WRITEOFF_REMARKS, new Object[] {
        remittanceId, remittanceId });
    DatabaseHelper.update(UPDATE_PATIENT_DUE_BILL_REMARKS, new Object[] { remittanceId,
        remittanceId });
  }

  public final String GET_BILL_TYPE = " select bill_type from bill where visit_id = ? order by mod_time desc limit 1 ";

  public String getBillType(String patientId) {
    return DatabaseHelper.getString(GET_BILL_TYPE, new Object[] { patientId });
  }

  public List<BasicDynaBean> getBillOrReceiptForPatientPayments(String visitId) {
    List<Object> args = new ArrayList<Object>();
    String query = "SELECT "
        + "   b.bill_no, b.is_tpa, b.bill_type, b.open_date,"
        + "   (b.total_amount - b.total_claim) AS patient_amount, b.payment_status, br.receipt_no,"
        + "   CASE WHEN r.is_settlement = 't' THEN 'S' "
        + "        WHEN r.is_settlement = 'f' THEN 'A' "
        + "        ELSE '' END AS recpt_type, r.receipt_type AS payment_type, r.amount, r.display_date"
        + " FROM bill b "
        + " JOIN bill_receipts br ON (b.bill_no = br.bill_no AND br.sponsor_index is NULL)"
        + " JOIN receipts r ON (r.receipt_id = br.receipt_no AND r.receipt_type in ('R','F') )"
        + " WHERE b.visit_id = ? AND b.status != 'X'"
        + " AND b.restriction_type != 'P'";
    args.add(visitId);
    return DatabaseHelper.queryToDynaList(query, args.toArray());
  }

  public BasicDynaBean getFirstBillForVisit(String visitId) {
    List<Object> args = new ArrayList<Object>();
    String query = "SELECT * FROM bill WHERE"
        + " visit_id = ? AND status != 'X' AND restriction_type != 'P' ORDER BY bill_no LIMIT 1";
    args.add(visitId);
    return DatabaseHelper.queryToDynaBean(query, args.toArray());
  }

  public final String UPDATE_BILL_CLOSE_STATUS = "UPDATE bill b SET "
      + "	status = 'C', payment_status = 'P', closed_by = 'auto_update', closed_date = now(), remarks = 'Claim received. Closing the bill' "
      + "	FROM bill_claim bcl"
      + "	JOIN insurance_remittance_details ird ON(bcl.claim_id = ird.claim_id)"
      + "	LEFT JOIN ("
      + "			 SELECT bcn.bill_no, SUM(b.total_amount) as total_amount,"
      + "			 SUM(b.total_claim) as total_claim, SUM(b.total_amount-b.total_claim) as total_pat_amt "
      + "			 FROM bill_credit_notes bcn "
      + "			 JOIN bill b ON(bcn.credit_note_bill_no = b.bill_no) "
      + "			 WHERE b.status!='X' GROUP BY bcn.bill_no "
      + "		) as crnote ON (crnote.bill_no = bcl.bill_no )"
      + "	WHERE b.bill_no = bcl.bill_no  "
      + "	AND ((b.total_claim + coalesce(crnote.total_claim,0) - primary_total_sponsor_receipts - secondary_total_sponsor_receipts - claim_recd_amount = 0) "
      + "		OR b.sponsor_writeoff = 'A')"
      + "	AND ((b.total_amount - b.total_claim  + coalesce(crnote.total_amount,0) - total_receipts - deposit_set_off - points_redeemed_amt = 0) "
      + "		OR b.patient_writeoff ='A')" + "	AND ird.remittance_id = ?";

  public final String UPDATE_RETURN_SALE_BILL_CLOSE = "UPDATE bill b SET status = 'C', payment_status = 'P', closed_by = 'auto_update', closed_date = now(), remarks = 'Claim received. Closing the bill' "
      + "	FROM bill_claim bcl "
      + "	JOIN insurance_remittance_details ird ON(bcl.claim_id = ird.claim_id)"
      + "	JOIN bill_claim rbcl ON(rbcl.claim_id = ird.claim_id and rbcl.bill_no != bcl.bill_no)"
      + "	JOIN bill rb ON(rb.bill_no = rbcl.bill_no  and rb.restriction_type = 'P')"
      + "	LEFT JOIN ("
      + "				SELECT bcn.bill_no, SUM(b.total_amount) as total_amount,"
      + "					SUM(b.total_claim) as total_claim, SUM(b.total_amount-b.total_claim) as total_pat_amt "
      + "				FROM bill_credit_notes bcn "
      + "				JOIN bill b ON(bcn.credit_note_bill_no = b.bill_no) "
      + "				WHERE b.status!='X' GROUP BY bcn.bill_no "
      + "			) as crnote ON (crnote.bill_no = bcl.bill_no )"
      + "	WHERE b.bill_no = bcl.bill_no AND b.restriction_type = 'P'"
      + "		AND ((b.total_claim + coalesce(crnote.total_claim,0) - b.primary_total_sponsor_receipts - b.secondary_total_sponsor_receipts - b.claim_recd_amount = 0) "
      + "		OR b.sponsor_writeoff = 'A')"
      + "		AND ((b.total_amount + rb.total_amount - b.total_claim - rb.total_claim  "
      + "			+ coalesce(crnote.total_amount,0) - b.total_receipts - rb.total_receipts"
      + "			- b.deposit_set_off - rb.deposit_set_off - b.points_redeemed_amt - rb.points_redeemed_amt = 0) "
      + "		OR b.patient_writeoff ='A')" + "		AND ird.remittance_id = ?";

  public void updateBillCloseStatus(Integer remittanceId) {
    DatabaseHelper.update(UPDATE_BILL_CLOSE_STATUS, new Object[] { remittanceId });
    DatabaseHelper.update(UPDATE_RETURN_SALE_BILL_CLOSE, new Object[] { remittanceId });
  }

  public final String UPDATE_BILL_SPONSOR_WRITEOFF_A = "UPDATE bill b SET sponsor_writeoff ='A'"
      + "FROM insurance_remittance_details ird "
      + "	JOIN bill_claim bcl ON (bcl.claim_id = ird.claim_id) "
      + "	JOIN ("
      + "			SELECT bcc.claim_id, sum(insurance_claim_amt) -	sum(claim_recd_total) as sponsor_due "
      + "				FROM bill_charge_claim bcc "
      + "				JOIN insurance_remittance_details ird ON(bcc.claim_id = ird.claim_id AND remittance_id = ?)"
      + "				GROUP BY bcc.claim_id"
      + "		) as foo ON (foo.claim_id = ird.claim_id)"
      + "	JOIN insurance_claim ic ON (ic.claim_id = ird.claim_id), generic_preferences gp"
      + "	WHERE b.bill_no  = bcl.bill_no AND ic.status = 'C' "
      + " AND gp.auto_close_claims_with_difference != 0 AND gp.auto_close_claims_with_difference >= foo.sponsor_due AND ird.remittance_id = ? "
      + "	AND sponsor_writeoff = 'N'";

  public final String UPDATE_BILL_SPONSOR_WRITEOFF_B = "UPDATE bill b SET sponsor_writeoff ='N'"
      + "	FROM insurance_remittance_details ird "
      + "	JOIN bill_claim bcl ON (bcl.claim_id = ird.claim_id) "
      + "	JOIN insurance_claim ic ON (ic.claim_id = ird.claim_id), generic_preferences gp"
      + "	WHERE b.bill_no  = bcl.bill_no AND ic.status = 'C' AND ird.remittance_id = ?"
      + "	AND (b.total_claim - primary_total_sponsor_receipts - secondary_total_sponsor_receipts - claim_recd_amount ) > gp.auto_close_claims_with_difference "
      + "	AND b.sponsor_writeoff = 'A' ";

  public void updateSponsorWriteOff(Integer remittanceId) {
    // These queries are dependent and must be run one after the other. If ran individually, it may
    // cause issues.
    DatabaseHelper.update(UPDATE_BILL_SPONSOR_WRITEOFF_A,
        new Object[] { remittanceId, remittanceId });
    DatabaseHelper.update(UPDATE_BILL_SPONSOR_WRITEOFF_B, new Object[] { remittanceId });
  }

  private final String OPEN_BILLS_FOR_ACTIVE_VISIT = "SELECT b.bill_no FROM bill b "
      + "LEFT JOIN patient_registration pr ON (b.visit_id = pr.patient_id) "
      + "WHERE pr.patient_id = ? AND pr.status = 'A' AND pr.visit_type = ? AND b.status ='A' ";

  private final String PHARMACY_NOT_INCLUDED_A = " AND restriction_type NOT IN ('P') ";

  public List<BasicDynaBean> getBillsForActiveVisit(String visitId, String visitType,
      String pharmacy) {
    if (null != pharmacy && !pharmacy.equals("") && pharmacy.equals("Y"))
      return DatabaseHelper.queryToDynaList(OPEN_BILLS_FOR_ACTIVE_VISIT + PHARMACY_NOT_INCLUDED_A,
          new Object[] { visitId, visitType });
    else
      return DatabaseHelper.queryToDynaList(OPEN_BILLS_FOR_ACTIVE_VISIT, new Object[] { visitId,
          visitType });
  }

  private static final String GET_CREDIT_NOTES = "SELECT b.* from bill b "
      + "JOIN bill_credit_notes bcn ON(bcn.credit_note_bill_no = b.bill_no) "
      + "JOIN bill nb ON(bcn.bill_no=nb.bill_no and nb.is_tpa='t') "
      + "WHERE b.visit_id=? and not b.RESTRICTION_TYPE='P' and b.TOTAL_AMOUNT < 0";

  public List<BasicDynaBean> getCreditNoteList(String visitId) {
    return DatabaseHelper.queryToDynaList(GET_CREDIT_NOTES, new Object[] { visitId });
  }

  private static final String GET_OPEN_TPA_BILLS = "SELECT * FROM bill b WHERE b.visit_id = ?  AND b.status ='A' AND b.total_amount >= 0 AND b.is_tpa ";

  public List<BasicDynaBean> getOpenTpaBills(String visitId) {
    // TODO Auto-generated method stub
    return DatabaseHelper.queryToDynaList(GET_OPEN_TPA_BILLS, new Object[] { visitId });
  }

  private static final String GET_ALL_TPA_BILLS = "SELECT * FROM bill b WHERE b.visit_id = ?  AND b.status != 'X' AND b.total_amount >= 0 AND b.is_tpa ";

  public List<BasicDynaBean> getAllTpaBills(String visitId) {
    // TODO Auto-generated method stub
    return DatabaseHelper.queryToDynaList(GET_ALL_TPA_BILLS, new Object[] { visitId });
  }

  private static final String GET_ALL_NON_INSURANCE_BILLS = "SELECT * FROM bill b WHERE b.visit_id = ?  AND b.status = 'A' AND b.is_tpa = false";

  public List<BasicDynaBean> getNonInsuranceOpenBills(String visitId) {
    // TODO Auto-generated method stub
    return DatabaseHelper.queryToDynaList(GET_ALL_NON_INSURANCE_BILLS, new Object[] { visitId });
  }

  private static final String GET_CLOSED_AND_FINALIZED_TPA_BILLS = "SELECT * FROM bill b WHERE b.status in ('F','C') AND b.restriction_type = 'N' "
      + " AND b.is_tpa = true AND b.total_amount >= 0 AND b.visit_id = ? ";

  public List<BasicDynaBean> getClosedAndFinalizedTpaBills(String visitId) {
    // TODO Auto-generated method stub
    return DatabaseHelper.queryToDynaList(GET_CLOSED_AND_FINALIZED_TPA_BILLS,
        new Object[] { visitId });
  }

  private static final String GET_ALL_TPA_BILLS_AND_CREDITNOTES = "SELECT * FROM bill b WHERE b.visit_id = ?  AND b.status != 'X' AND b.is_tpa ";

  public List<BasicDynaBean> getAllTpaBillsAndCreditNotes(String visitId) {
    // TODO Auto-generated method stub
    return DatabaseHelper.queryToDynaList(GET_ALL_TPA_BILLS_AND_CREDITNOTES,
        new Object[] { visitId });
  }

  private static final String UPDATE_BILL_RATE_PLAN = "UPDATE bill SET bill_rate_plan_id = ? WHERE bill_no in(#)";

  public Boolean updateBillRatePlan(String insurancePlanDefaultRatePlan, String billNos) {
    // TODO Auto-generated method stub
    String query = UPDATE_BILL_RATE_PLAN.replaceAll("#", billNos);
    return DatabaseHelper.update(query, new Object[] { insurancePlanDefaultRatePlan }) >= 0;
  }

  private static final String IS_MULTI_VISIT_PACKAGE_BILL = " SELECT orders.package_id  "+
      " FROM ( "+
      " SELECT bc.bill_no,p.package_id "+
      " FROM services_prescribed sp "+
      " JOIN bill_charge bc ON(bc.order_number = sp.common_order_id) "+
      " JOIN package_prescribed pp ON (sp.package_ref = pp.prescription_id) "+
      " JOIN packages p ON (pp.package_id = p.package_id AND p.multi_visit_package = true) "+
      " WHERE bc.bill_no = ? "+
      " UNION ALL "+
      " SELECT bc.bill_no,p.package_id "+
      " FROM tests_prescribed tp "+
      " JOIN bill_charge bc ON(bc.order_number = tp.common_order_id) "+
      " JOIN package_prescribed pp ON (tp.package_ref = pp.prescription_id) "+
      " JOIN packages p ON (pp.package_id = p.package_id AND p.multi_visit_package = true) "+
      " WHERE bc.bill_no = ? "+
      " UNION ALL "+
      " SELECT bcc.bill_no,p.package_id "+
      " FROM doctor_consultation dc  "+
      " JOIN bill_charge bcc ON(bcc.order_number = dc.common_order_id) "+
      " JOIN package_prescribed pp ON (dc.package_ref = pp.prescription_id) "+
      " JOIN packages p ON (pp.package_id = p.package_id AND p.multi_visit_package = true) "+
      " LEFT JOIN bill_activity_charge bac ON bac.activity_id=dc.consultation_id::text "+
      " AND bac.activity_code='DOC' "+
      " LEFT JOIN bill_charge bc ON (bc.charge_id = bac.charge_id) "+
      " WHERE bcc.bill_no = ? "+
      " UNION ALL "+
      " SELECT bc.bill_no,p.package_id "+
      " FROM other_services_prescribed osp "+
      " JOIN bill_charge bc ON (bc.order_number = osp.common_order_id) "+
      " JOIN package_prescribed pp ON (osp.package_ref = pp.prescription_id) "+
      " JOIN packages p ON (pp.package_id = p.package_id AND p.multi_visit_package = true) "+
      " WHERE bc.bill_no = ? "+
    " ) as orders  limit 1";

  public Boolean isMultiVisitPackageBill(String billNo) {
    // TODO Auto-generated method stub
    Integer packageId = DatabaseHelper.getInteger(IS_MULTI_VISIT_PACKAGE_BILL,
        new Object[] {billNo, billNo, billNo, billNo} );
    return null == packageId ? false : packageId > 0;
  }

  private static final String GET_OPEN_MULTIVISIT_PKG_TPA_BILLS = " SELECT orders.* "+
      " FROM ( "+
      " SELECT bc.* ,p.package_id,b.discount_category_id "+
      " FROM services_prescribed sp "+
      " JOIN bill_charge bc ON(bc.order_number = sp.common_order_id) "+
      " JOIN bill b ON (b.bill_no = bc.bill_no) " +
      " JOIN package_prescribed pp ON (sp.package_ref = pp.prescription_id) "+
      " JOIN packages p ON (pp.package_id = p.package_id AND p.multi_visit_package = true) "+
      " WHERE sp.patient_id = ? AND b.status='A' AND b.is_tpa = true "+
      " UNION ALL "+
      " SELECT bc.*,p.package_id,b.discount_category_id "+
      " FROM tests_prescribed tp "+
      " JOIN bill_charge bc ON(bc.order_number = tp.common_order_id) "+
      " JOIN bill b ON (b.bill_no = bc.bill_no) " +
      " JOIN package_prescribed pp ON (tp.package_ref = pp.prescription_id) "+
      " JOIN packages p ON (pp.package_id = p.package_id AND p.multi_visit_package = true) "+
      " WHERE tp.pat_id = ? AND b.status='A' AND b.is_tpa = true  "+
      " UNION ALL "+
      " SELECT bcc.*,p.package_id,b.discount_category_id "+
      " FROM doctor_consultation dc  "+
      " JOIN bill_charge bcc ON(bcc.order_number = dc.common_order_id) "+
      " JOIN bill b ON (b.bill_no = bcc.bill_no) " +
      " JOIN package_prescribed pp ON (dc.package_ref = pp.prescription_id) "+
      " JOIN packages p ON (pp.package_id = p.package_id AND p.multi_visit_package = true) "+
      " LEFT JOIN bill_activity_charge bac ON bac.activity_id=dc.consultation_id::text "+
      " AND bac.activity_code='DOC' "+
      " LEFT JOIN bill_charge bc ON (bc.charge_id = bac.charge_id) "+
      " WHERE dc.patient_id = ? AND b.status='A' AND b.is_tpa = true "+
      " UNION ALL "+
      " SELECT bc.*,p.package_id ,b.discount_category_id"+
      " FROM other_services_prescribed osp "+
      " JOIN bill_charge bc ON (bc.order_number = osp.common_order_id) "+
      " JOIN bill b ON (b.bill_no = bc.bill_no) " +
      " JOIN package_prescribed pp ON (osp.package_ref = pp.prescription_id) "+
      " JOIN packages p ON (pp.package_id = p.package_id AND p.multi_visit_package = true) "+
      " WHERE osp.patient_id = ? AND b.status='A' AND b.is_tpa = true "+
      " ) as orders  ";


      //"SELECT * FROM multivisit_bills_view mbv "
     // + " JOIN bill b using(bill_no) "
     // + " WHERE b.visit_id = ? AND b.status='A' AND b.is_tpa = true ";

  public List<BasicDynaBean> getOpenMultiVisitPkgTPABills(String visitId) {
    // TODO Auto-generated method stub
    return DatabaseHelper.queryToDynaList(GET_OPEN_MULTIVISIT_PKG_TPA_BILLS,
        new Object[] { visitId, visitId, visitId, visitId });
  }

  private static final String GET_ALL_MULTI_VISIT_PKG_TPA_BILLS = " SELECT orders.* "+
      " FROM ( "+
      " SELECT bc.* ,p.package_id,b.discount_category_id "+
      " FROM services_prescribed sp "+
      " JOIN bill_charge bc ON(bc.order_number = sp.common_order_id) "+
      " JOIN bill b ON (b.bill_no = bc.bill_no) " +
      " JOIN package_prescribed pp ON (sp.package_ref = pp.prescription_id) "+
      " JOIN packages p ON (pp.package_id = p.package_id AND p.multi_visit_package = true) "+
      " WHERE sp.patient_id = ? AND b.status != 'X' AND b.is_tpa = true "+
      " UNION ALL "+
      " SELECT bc.*,p.package_id,b.discount_category_id "+
      " FROM tests_prescribed tp "+
      " JOIN bill_charge bc ON(bc.order_number = tp.common_order_id) "+
      " JOIN bill b ON (b.bill_no = bc.bill_no) " +
      " JOIN package_prescribed pp ON (tp.package_ref = pp.prescription_id) "+
      " JOIN packages p ON (pp.package_id = p.package_id AND p.multi_visit_package = true) "+
      " WHERE tp.pat_id = ? AND b.status != 'X' AND b.is_tpa = true "+
      " UNION ALL "+
      " SELECT bcc.*,p.package_id,b.discount_category_id "+
      " FROM doctor_consultation dc  "+
      " JOIN bill_charge bcc ON(bcc.order_number = dc.common_order_id) "+
      " JOIN bill b ON (b.bill_no = bcc.bill_no) " +
      " JOIN package_prescribed pp ON (dc.package_ref = pp.prescription_id) "+
      " JOIN packages p ON (pp.package_id = p.package_id AND p.multi_visit_package = true) "+
      " LEFT JOIN bill_activity_charge bac ON bac.activity_id=dc.consultation_id::text "+
      " AND bac.activity_code='DOC' "+
      " LEFT JOIN bill_charge bc ON (bc.charge_id = bac.charge_id) "+
      " WHERE dc.patient_id = ? AND b.status != 'X' AND b.is_tpa = true "+
      " UNION ALL "+
      " SELECT bc.*,p.package_id ,b.discount_category_id"+
      " FROM other_services_prescribed osp "+
      " JOIN bill_charge bc ON (bc.order_number = osp.common_order_id) "+
      " JOIN bill b ON (b.bill_no = bc.bill_no) " +
      " JOIN package_prescribed pp ON (osp.package_ref = pp.prescription_id) "+
      " JOIN packages p ON (pp.package_id = p.package_id AND p.multi_visit_package = true) "+
      " WHERE osp.patient_id = ? AND b.status != 'X' AND b.is_tpa = true "+
      " ) as orders  ";

      //"SELECT * FROM multivisit_bills_view mbv "
      //+ " JOIN bill b using(bill_no) "
      //+ " WHERE b.visit_id = ? AND b.status != 'X' AND b.is_tpa = true ";

  public List<BasicDynaBean> getAllMultiVisitPkgTPABills(String visitId) {
    // TODO Auto-generated method stub
    return DatabaseHelper.queryToDynaList(GET_ALL_MULTI_VISIT_PKG_TPA_BILLS,
        new Object[] { visitId, visitId, visitId, visitId });
  }
  
  private static final String UPDATE_INSURANCE_PLAN_DEFAULT_DISCOUNT_PLAN = "UPDATE bill b SET discount_category_id = ?  WHERE b.bill_no in (#) ";

  public Boolean updateInsurancePlanDefaultDiscountPlan(Integer defaultDiscPlanId, String billNos) {
    // TODO Auto-generated method stub 
       String query = UPDATE_INSURANCE_PLAN_DEFAULT_DISCOUNT_PLAN.replaceAll("#", billNos);
       return DatabaseHelper.update(query, new Object[]{defaultDiscPlanId}) >= 0;
  }
  
  private static final String OPEN_BILLS_FOR_VISIT = "SELECT b.bill_no, b.bill_rate_plan_id, "
      + "b.bill_type, b.is_tpa, b.open_date, b.is_primary_bill FROM bill b "
      + "LEFT JOIN patient_registration pr ON (b.visit_id = pr.patient_id) "
      + "WHERE pr.patient_id = ? AND pr.visit_type = ? AND b.status ='A' AND b.payment_status ='U' ";

  private static final String PHARMACY_NOT_INCLUDED = " AND restriction_type NOT IN ('P','T') ";
  
  private static final String BILLS_OPEN_DATE = " AND b.open_date <= ? ";

  private static final String EXCLUDE_MULTI_VISIT_PACKAGE_BILLS = " AND b.bill_no NOT IN "+
      " (SELECT distinct(orders.bill_no) "+
      " FROM ( "+
        " SELECT bc.bill_no "+
        " FROM services_prescribed sp "+ 
        " JOIN bill_charge bc ON(bc.order_number = sp.common_order_id) "+
        " JOIN package_prescribed pp ON (sp.package_ref = pp.prescription_id) "+
        " JOIN packages p ON (pp.package_id = p.package_id AND p.multi_visit_package = true) "+
        " JOIN bill b ON(bc.bill_no = b.bill_no) WHERE b.visit_id = ? " +
        " UNION ALL "+
        " SELECT bc.bill_no "+
        " FROM tests_prescribed tp "+
        " JOIN bill_charge bc ON(bc.order_number = tp.common_order_id) "+
        " JOIN package_prescribed pp ON (tp.package_ref = pp.prescription_id) "+
        " JOIN packages p ON (pp.package_id = p.package_id AND p.multi_visit_package = true) "+
        " JOIN bill b ON(bc.bill_no = b.bill_no) WHERE b.visit_id = ? " +
        " UNION ALL "+
        " SELECT bcc.bill_no "+
        " FROM doctor_consultation dc  "+
        " JOIN bill_charge bcc ON(bcc.order_number = dc.common_order_id) "+
        " JOIN package_prescribed pp ON (dc.package_ref = pp.prescription_id) "+
        " JOIN packages p ON (pp.package_id = p.package_id AND p.multi_visit_package = true) "+
        " LEFT JOIN bill_activity_charge bac ON bac.activity_id=dc.consultation_id::text AND bac.activity_code='DOC' "+
        " LEFT JOIN bill_charge bc ON (bc.charge_id = bac.charge_id) "+
        " JOIN bill b ON(bcc.bill_no = b.bill_no) WHERE b.visit_id = ? " +
        " UNION ALL "+
        " SELECT bc.bill_no "+
        " FROM other_services_prescribed osp "+ 
        " JOIN bill_charge bc ON (bc.order_number = osp.common_order_id) "+
        " JOIN package_prescribed pp ON (osp.package_ref = pp.prescription_id) "+
        " JOIN packages p ON (pp.package_id = p.package_id AND p.multi_visit_package = true) "+
        " JOIN bill b ON(bc.bill_no = b.bill_no) WHERE b.visit_id = ? " +
        " UNION ALL "+
        " SELECT bc.bill_no "+
        " FROM equipment_prescribed sp "+
        " JOIN bill_charge bc ON(bc.order_number = sp.common_order_id) "+
        " JOIN package_prescribed pp ON (sp.package_ref = pp.prescription_id) "+
        " LEFT JOIN patient_packages patp ON (pp.pat_package_id = patp.pat_package_id) "+
        " JOIN packages p ON (pp.package_id = p.package_id AND p.multi_visit_package = true) "+
        " JOIN bill b ON(bc.bill_no = b.bill_no) WHERE b.visit_id = ? " +
      " ) as orders)  ";

  private static final String ORDER_OLDEST_BILL_FIRST = "ORDER BY b.open_date";

  public List<BasicDynaBean> getUnpaidBillsForVisit(String visitId, String visitType, String pharmacy,
      String includeMultiVisitPackageBills, Date orderDateTime) {

    StringBuilder query = new StringBuilder();
    List<Object> params = new ArrayList<>();
    query.append(OPEN_BILLS_FOR_VISIT);
    params.add(visitId);
    params.add(visitType);

    if (null == pharmacy || pharmacy.equals("") || pharmacy.equals("N")) {
    	query.append(PHARMACY_NOT_INCLUDED);
    }
    
    if (null != orderDateTime) {
      query.append(BILLS_OPEN_DATE);
      params.add(orderDateTime);
    }
    
    if (includeMultiVisitPackageBills != null && includeMultiVisitPackageBills.equals("N")) {
      query.append(EXCLUDE_MULTI_VISIT_PACKAGE_BILLS);
      params.add(visitId);
      params.add(visitId);
      params.add(visitId);
      params.add(visitId);
      params.add(visitId);
    }
    query.append(ORDER_OLDEST_BILL_FIRST);
    return DatabaseHelper.queryToDynaList(query.toString(), params.toArray());
  }
  
  private static final String ACTIVE_MULTI_VISIT_PACKAGE_BILLS = "SELECT distinct(mbv.bill_no), mbv.pack_id as package_id, b.is_tpa, b.bill_rate_plan_id, "
      + " b.bill_type, b.open_date FROM multivisit_bills_view mbv "
      + " JOIN bill b ON(mbv.bill_no = b.bill_no) "
      + " JOIN patient_registration pr ON (pr.patient_id = b.visit_id) "
      + " JOIN patient_packages pp ON (pp.mr_no = pr.mr_no) "
      + " WHERE b.visit_id = ? AND mbv.pat_package_id = ? AND pp.status = 'P' AND b.status ='A' AND b.payment_status ='U' ";

  /*private static final String MULTI_VISIT_PACKAGE_BILLS = " SELECT distinct(mbv.bill_no), mbv.package_id, b.bill_rate_plan_id, b.is_tpa "
      + " FROM multivisit_bills_view mbv "
      + " JOIN bill b ON(mbv.bill_no = b.bill_no) WHERE b.visit_id = ? ";*/
  
  private static final String MULTI_VISIT_PACKAGE_BILLS = " SELECT distinct(orders.bill_no), orders.status, orders.bill_type, orders.package_id,orders.bill_rate_plan_id, orders.is_tpa, orders.is_primary_bill, orders.pat_package_id "+
      " FROM ( "+
      " SELECT bc.bill_no,p.package_id,b.bill_rate_plan_id, b.is_tpa, b.bill_type, b.is_primary_bill, patp.status, patp.pat_package_id "+
      " FROM services_prescribed sp "+ 
      " JOIN bill_charge bc ON(bc.order_number = sp.common_order_id) "+
      " JOIN package_prescribed pp ON (sp.package_ref = pp.prescription_id) "+
      " LEFT JOIN patient_packages patp ON (pp.pat_package_id = patp.pat_package_id) "+
      " JOIN packages p ON (pp.package_id = p.package_id AND p.multi_visit_package = true) "+
      " JOIN bill b ON(bc.bill_no = b.bill_no) WHERE b.visit_id = ? " +
      " UNION ALL "+
      " SELECT bc.bill_no,p.package_id,b.bill_rate_plan_id, b.is_tpa, b.bill_type, b.is_primary_bill, patp.status, patp.pat_package_id "+
      " FROM tests_prescribed tp "+
      " JOIN bill_charge bc ON(bc.order_number = tp.common_order_id) "+
      " JOIN package_prescribed pp ON (tp.package_ref = pp.prescription_id) "+
      " LEFT JOIN patient_packages patp ON (pp.pat_package_id = patp.pat_package_id) "+
      " JOIN packages p ON (pp.package_id = p.package_id AND p.multi_visit_package = true) "+
      " JOIN bill b ON(bc.bill_no = b.bill_no) WHERE b.visit_id = ? " +
      " UNION ALL "+
      " SELECT bcc.bill_no,p.package_id,b.bill_rate_plan_id, b.is_tpa, b.bill_type, b.is_primary_bill, patp.status, patp.pat_package_id "+
      " FROM doctor_consultation dc  "+
      " JOIN bill_charge bcc ON(bcc.order_number = dc.common_order_id) "+
      " JOIN package_prescribed pp ON (dc.package_ref = pp.prescription_id) "+
      " LEFT JOIN patient_packages patp ON (pp.pat_package_id = patp.pat_package_id) "+
      " JOIN packages p ON (pp.package_id = p.package_id AND p.multi_visit_package = true) "+
      " LEFT JOIN bill_activity_charge bac ON bac.activity_id=dc.consultation_id::text AND bac.activity_code='DOC' "+
      " LEFT JOIN bill_charge bc ON (bc.charge_id = bac.charge_id) "+
      " JOIN bill b ON(bcc.bill_no = b.bill_no) WHERE b.visit_id = ? " +
      " UNION ALL "+
      " SELECT bc.bill_no,p.package_id,b.bill_rate_plan_id, b.is_tpa, b.bill_type, b.is_primary_bill, patp.status, patp.pat_package_id "+
      " FROM other_services_prescribed osp "+ 
      " JOIN bill_charge bc ON (bc.order_number = osp.common_order_id) "+
      " JOIN package_prescribed pp ON (osp.package_ref = pp.prescription_id) "+
      " LEFT JOIN patient_packages patp ON (pp.pat_package_id = patp.pat_package_id) "+
      " JOIN packages p ON (pp.package_id = p.package_id AND p.multi_visit_package = true) "+
      " JOIN bill b ON(bc.bill_no = b.bill_no) WHERE b.visit_id = ? "+
      " UNION ALL "+
      " SELECT bc.bill_no,p.package_id,b.bill_rate_plan_id, b.is_tpa, b.bill_type, b.is_primary_bill, patp.status, patp.pat_package_id "+
      " FROM equipment_prescribed sp "+
      " JOIN bill_charge bc ON(bc.order_number = sp.common_order_id) "+
      " JOIN package_prescribed pp ON (sp.package_ref = pp.prescription_id) "+
      " LEFT JOIN patient_packages patp ON (pp.pat_package_id = patp.pat_package_id) "+
      " JOIN packages p ON (pp.package_id = p.package_id AND p.multi_visit_package = true) "+
      " JOIN bill b ON(bc.bill_no = b.bill_no) WHERE b.visit_id = ? " +
      " ) as orders  ";

  public List<BasicDynaBean> getMvpUnpaidBillsForVisit(String visitId, Integer patPackageId,
      Date orderDateTime) {
    return DatabaseHelper.queryToDynaList(ACTIVE_MULTI_VISIT_PACKAGE_BILLS + BILLS_OPEN_DATE,
        new Object[] { visitId, patPackageId, orderDateTime });
  }

  public List<BasicDynaBean> getMvpBillsForVisit(String visitId) {
    return DatabaseHelper.queryToDynaList(MULTI_VISIT_PACKAGE_BILLS, new Object[] { visitId, visitId, visitId, visitId, visitId });
  }

  private static final String GET_COMMMON_ORDER_IDS = "SELECT distinct(order_number) as order_number "
      + " FROM bill JOIN bill_charge bc USING (bill_no) "
      + " JOIN bill_activity_charge USING (charge_id) "
      + " WHERE visit_id = ? AND order_number IS NOT NULL "
      + " AND bc.status = 'A' "
      + " ORDER BY order_number DESC ";

  public List<BasicDynaBean> getCommonOrderIds(String visitId) {
    return DatabaseHelper.queryToDynaList(GET_COMMMON_ORDER_IDS, new Object[] { visitId });
  }
  
  private static final String GET_VISIT_PATIENTDUE = " SELECT COALESCE("
      + " sum(b.total_amount+b.total_tax "
      + " -b.total_claim-b.total_claim_tax-b.total_receipts-b.deposit_set_off"
      + " -b.points_redeemed_amt),0.00) "
      + " AS visit_patient_due FROM bill b WHERE b.visit_id=? GROUP BY b.visit_id ";

  public BigDecimal getVisitPatientDue(String visitId) {
    BigDecimal visitPatientDue = DatabaseHelper.getBigDecimal(GET_VISIT_PATIENTDUE, visitId);
    return visitPatientDue == null ? BigDecimal.ZERO : visitPatientDue;
  }

  private static final String BILL_RATE_PLAN_BED_TYPE = "SELECT b.bill_rate_plan_id, pr.bed_type, b.visit_type "
      + " FROM bill b JOIN patient_registration pr ON (b.visit_id = pr.patient_id) "
      + " WHERE b.bill_no = ?";

  public BasicDynaBean getBillRatePlanAndBedType(String billNo) {
    return DatabaseHelper.queryToDynaBean(BILL_RATE_PLAN_BED_TYPE, new Object[] { billNo });
  }
  
  public static final String GET_AVAILABLE_GENERAL_AND_IP_DEPOSIT = " SELECT "
      + " patDepDet.hosp_total_balance - "
      + " SUM( COALESCE(mvpDep.total_deposits,0) - COALESCE(mvpDep.total_set_offs,0) ) "
      + " AS hosp_available_deposit "
      + " FROM patient_deposit_details_view patDepDet "
      + " LEFT JOIN multivisit_deposits_view mvpDep ON (mvpDep.mr_no = patDepDet.mr_no) "
      + " WHERE patDepDet.mr_no=? "
      + " GROUP BY mvpDep.mr_no, patDepDet.hosp_total_balance ";
  
  public BasicDynaBean getAvailableGeneralAndIpDeposit(String mrNo) {
    return DatabaseHelper.queryToDynaBean(GET_AVAILABLE_GENERAL_AND_IP_DEPOSIT, 
                                          new Object[] { mrNo });
  }
  
  /**
   * Gets the visit id.
   *
   * @param billNo String
   * @return the visit id
   */
  public String getVisitId(String billNo) {
    return DatabaseHelper.getString("SELECT VISIT_ID FROM BILL WHERE BILL_NO=? ", billNo);
  }

  /** The Constant GET_DRG_CODE. */
  private static final String GET_DRG_CODE = " SELECT b.bill_no AS drg_bill_no, b.status AS drg_bill_status,"
      + " bc.charge_id AS drg_charge_id," + " bc.act_rate_plan_item_code AS drg_code, "
      + " bc.code_type AS drg_code_type,"
      + " COALESCE((SELECT code_desc from getItemCodesForCodeType('IR-DRG', b.visit_type) mcm "
      + " WHERE (bc.act_rate_plan_item_code::text = mcm.code)  LIMIT 1), '') AS drg_description "
      + " FROM bill b "
      + " LEFT JOIN bill_charge bc ON (bc.charge_head = 'BPDRG' AND bc.bill_no = b.bill_no) "
      + " WHERE b.bill_type = 'C' AND b.is_primary_bill = 'Y' AND b.status != 'X' AND b.is_tpa AND b.visit_id=?  ";

  /**
   * Gets the DRG code.
   *
   * @param patientId String
   * @return the DRG code
   */
  public Map getDRGCode(String patientId) {
    List drgCodeList = DatabaseHelper.queryToDynaList(GET_DRG_CODE, patientId);
    if (drgCodeList != null && !drgCodeList.isEmpty())
      return ((BasicDynaBean) drgCodeList.get(0)).getMap();
    else
      return null;
  }
  
  /** The Constant GET_BILL_DRG_CODE. */
  private static final String GET_BILL_DRG_CODE = " SELECT b.bill_no AS drg_bill_no,"
      + " bc.charge_id AS drg_charge_id," + " bc.act_rate_plan_item_code AS drg_code, "
      + " COALESCE((SELECT code_desc from getItemCodesForCodeType('IR-DRG', b.visit_type) mcm "
      + " WHERE (bc.act_rate_plan_item_code::text = mcm.code)  LIMIT 1), '') AS drg_description "
      + " FROM bill b "
      + " LEFT JOIN bill_charge bc ON (bc.charge_head = 'BPDRG' AND bc.status != 'X'"
      + " AND bc.bill_no = b.bill_no) "
      + " WHERE b.bill_type = 'C' AND b.is_primary_bill = 'Y' AND b.status != 'X'"
      + " AND b.is_tpa AND b.bill_no = ?  ";
  
  /**
   * Gets the bill DRG code.
   *
   * @param billNo the bill no
   * @return the bill DRG code
   */
  public Map getBillDRGCode(String billNo) {
    List drgCodeList = DatabaseHelper.queryToDynaList(GET_BILL_DRG_CODE, billNo);
    if (drgCodeList != null && !drgCodeList.isEmpty())
      return ((BasicDynaBean) drgCodeList.get(0)).getMap();
    else
      return null;
  }

  /** The Constant BILL_QUERY. */
  private static final String BILL_QUERY = "SELECT b.bill_no, b.visit_id, pr.mr_no, b.visit_type, "
      + " total_amount, b.bill_type, b.open_date, b.mod_time, "
      + " pic.status as primary_claim_status, sic.status as secondary_claim_status, b.claim_recd_amount, "
      + " b.status, b.payment_status, pr.status as visit_status, b.discharge_status, "
      + " b.finalized_date, b.closed_date, b.opened_by, b.username, b.closed_by, b.finalized_by, b.account_group, "
      + " uc.temp_username as closed_by_name, uco.temp_username as opened_by_name, "
      + " ufo.temp_username as finalized_by_name, b.last_finalized_at, "
      + " b.app_modified, b.remarks,b.credit_note_reasons, b.discount_auth, pr.ready_to_discharge, "
      + " pr.org_id, pr.bed_type, pr.dept_name, b.total_amount, b.total_tax, b.total_discount,b.total_claim,b.total_claim_return, "
      + " b.approval_amount, b.primary_approval_amount, b.secondary_approval_amount, "
      + " b.primary_total_claim, b.secondary_total_claim, u.temp_username, b.deposit_set_off,"
      + " b.points_earned, b.points_redeemed, b.points_redeemed_amt, "
      + " da.disc_auth_name, b.restriction_type, b.procedure_no, sp.procedure_code, sp.procedure_name, "
      + " sp.procedure_limit, b.total_claim, b.sponsor_bill_no, b.total_receipts, "
      + " b.primary_total_sponsor_receipts, b.secondary_total_sponsor_receipts, 0 as insurance_deduction, "
      + " b.dyna_package_id, b.dyna_package_charge, dyp.dyna_package_name, "
      + " dpo.item_code as dyna_pkg_rate_plan_code,is_tpa, is_primary_bill, b.dyna_pkg_processed, "
      + " bill_rate_plan_id, od.org_name, b.bill_printed, b.cancel_reason, b.reopen_reason, "
      + " coalesce(b.bill_label_id, -1) AS bill_label_id,"
      + " b.points_earned, b.points_redeemed, b.points_redeemed_amt, pbc.plan_id as primary_plan_id, "
      + " sbc.plan_id as secondary_plan_id, b.patient_writeoff, b.sponsor_writeoff, b.writeoff_remarks, "
      + " b.sponsor_writeoff_remarks, b.cancellation_approval_status, b.discount_category_id,"
      + " dcm.discount_plan_name discount_cat_name,b.cancellation_approved_by, b.ip_deposit_set_off,"
      + " pic.closure_type as primary_closure_type ,sic.closure_type as secondary_closure_type,"
      + " pbc.claim_id as primaryClaimId, sbc.claim_id as secondaryClaimId, pd.financial_discharge_date, "
      + " pd.financial_discharge_time, b.total_claim_tax, pr.center_id ";

  /** The Constant BILL_QUERY_TABLES. */
  static final String BILL_QUERY_TABLES = " FROM bill b "
      + " LEFT JOIN bill_claim pbc ON (b.bill_no = pbc.bill_no AND pbc.priority = 1) "
      + " LEFT JOIN bill_claim sbc ON (b.bill_no = sbc.bill_no AND sbc.priority = 2) "
      + " LEFT JOIN insurance_claim pic ON (pic.claim_id=pbc.claim_id AND pic.plan_id=pbc.plan_id) "
      + " LEFT JOIN insurance_claim sic ON (sic.claim_id=sbc.claim_id AND sic.plan_id=sbc.plan_id) "
      + " LEFT join patient_registration pr on (pr.patient_id = b.visit_id) "
      + " LEFT JOIN u_user u ON (u.emp_username = b.username) "
      + " LEFT JOIN u_user uc ON (uc.emp_username = b.closed_by) "
      + " LEFT JOIN u_user uco ON (uco.emp_username = b.opened_by) "
      + " LEFT JOIN u_user ufo ON (ufo.emp_username = b.finalized_by) "
      + " LEFT JOIN discount_authorizer da ON (da.disc_auth_id = b.discount_auth) "
      + " LEFT JOIN discount_plan_main dcm ON (dcm.discount_plan_id = b.discount_category_id) "
      + " LEFT JOIN sponsor_procedure_limit sp ON (sp.procedure_no = b.procedure_no) "
      + " LEFT JOIN dyna_packages dyp ON (dyp.dyna_package_id = b.dyna_package_id) "
      + " LEFT JOIN dyna_package_org_details dpo ON (dpo.dyna_package_id = b.dyna_package_id AND dpo.org_id = pr.org_id)"
      + " LEFT JOIN organization_details od ON (b.bill_rate_plan_id = od.org_id ) "
      + " LEFT JOIN patient_discharge pd ON (b.visit_id = pd.patient_id ) ";

  /** The Constant GET_BILL. */
  private static final String GET_BILL = BILL_QUERY + BILL_QUERY_TABLES + " WHERE b.bill_no=?";

  /**
   * Gets the bill.
   *
   * @param billNo String
   * @return the bill
   */
  public BasicDynaBean getBill(String billNo) {
    return DatabaseHelper.queryToDynaBean(GET_BILL, billNo);
  }
  
  private static final String GET_VISIT_OPEN_BILLS_EXCLUDED_PACKAGE_BILLS =
      " SELECT pr.mr_no, b.visit_id, b.bill_no, b.status, b.payment_status, b.account_group, "
          + "  b.approval_amount, b.total_amount, b.deposit_set_off, b.total_receipts, "
          + "  pr.primary_sponsor_id, b.is_tpa, b.bill_rate_plan_id,b.open_date::date, "
          + " b.is_primary_bill, b.bill_type FROM bill b "
          + "  JOIN patient_registration pr ON (b.visit_id = pr.patient_id)"
          + " WHERE b.status='A' AND b.bill_type IN ('C','P') AND b.restriction_type = 'N' AND "
          + " b.visit_id = ? AND b.bill_no NOT IN (select mvb.bill_no from multivisit_bills_view mvb JOIN bill bl ON(bl.bill_no = mvb.bill_no) where bl.visit_id = ?)"
          + " ORDER BY is_primary_bill DESC";

  public List<BasicDynaBean> getVisitOpenBillsExcludingPackageBills(String visitId) {
    return DatabaseHelper.queryToDynaList(
        GET_VISIT_OPEN_BILLS_EXCLUDED_PACKAGE_BILLS, new Object[] {visitId, visitId});
  }
  
  private static final String GET_VISIT_MVP_OPEN_BILLS = "SELECT DISTINCT mvb.bill_no, "
      + " mvb.pack_id, pp.pat_package_id, bl.is_tpa FROM multivisit_bills_view mvb "
      + " JOIN package_prescribed pp ON (mvb.order_number = pp.common_order_id and mvb.package_id = pp.package_id)"
      + " JOIN bill bl ON (bl.bill_no = mvb.bill_no) "
      + " WHERE bl.visit_id = ? AND bl.status='A' AND bl.total_amount >= 0";

  public List<BasicDynaBean> getVisitOpenMvpBills(String visitId) {
    return DatabaseHelper.queryToDynaList(GET_VISIT_MVP_OPEN_BILLS, visitId);
  }

  private static final String GET_VISIT_OPEN_BILLS = "SELECT * FROM bill b WHERE b.visit_id = ? AND b.status='A' AND b.total_amount >= 0";

  public List<BasicDynaBean> getVisitOpenBills(String visitId) {
    // TODO Auto-generated method stub
    return DatabaseHelper.queryToDynaList(GET_VISIT_OPEN_BILLS, new Object[]{visitId});
  }
  
  /** The Constant GET_VISIT_FINALIZED_BILLS. */
  private static final String GET_VISIT_FINALIZED_AND_CLOSED_BILLS = "SELECT * FROM bill b WHERE "
      + " b.visit_id = ? AND b.status IN ('F','C') ";

  /**
   * Gets the visit finalized bills.
   *
   * @param visitId
   *          the visit id
   * @return the visit finalized bills
   */
  public List<BasicDynaBean> getVisitFinalizedAndClosedBills(String visitId) {
    return DatabaseHelper.queryToDynaList(GET_VISIT_FINALIZED_AND_CLOSED_BILLS,
        new Object[] { visitId });
  }

  private static final String GET_ALL_OPEN_DYNA_PKG_BILLS = "select bill_no,visit_id,dyna_package_id,dyna_package_charge,total_claim "
      + " from bill where status='A' AND dyna_package_id != 0  and visit_id= ?";

  public List<BasicDynaBean> getAllOpenDynaPkgBills(String visitId) {
   return DatabaseHelper.queryToDynaList(GET_ALL_OPEN_DYNA_PKG_BILLS, new Object[]{visitId});
  }

  public static final String UPDATE_BILL_PRINTED_STATUS = " UPDATE bill SET bill_printed='Y' WHERE bill_no=?";
  
  public int updateBillPrintedStatus(String billNo) {
    return DatabaseHelper.update(UPDATE_BILL_PRINTED_STATUS, new Object[]{billNo});
  }

  public static final String GET_OPEN_FINALIZED_BILL_VISIT_DETAILS ="select b.bill_no, b.status, b.bill_type, "
  		+ " b.open_date, b.finalized_date,b.payment_status,b.bill_signature, "
        + " b.is_primary_bill, b.last_finalized_at,"
  		+ " pr.patient_id, pr.status as visit_status, doc.doctor_name, icm.insurance_co_name,"
  		+ " dep.dept_name ,tpa.tpa_name "
  		+ " from bill b Join patient_registration pr on pr.patient_id = b.visit_id"
  		+ " Left Join patient_insurance_plans pip on pip.patient_id = b.visit_id"
  		+ " LEFT JOIN store_retail_customers phc ON (b.visit_id = phc.customer_id) "
  		+ " LEFT JOIN incoming_sample_registration isr ON (isr.incoming_visit_id = b.visit_id) "
  		+ " left join insurance_company_master icm on icm.insurance_co_id = pip.insurance_co"
  		+ " left join tpa_master tpa on tpa.tpa_id = pip.sponsor_id "
  		+ " left join department dep  on dep.dept_id = pr.dept_name"
  		+ " left join doctors doc on doc.doctor_id = pr.doctor"
  		+ " where pr.mr_no =? and b.visit_type =? ";

  private static final String CENTER_ID_CONDITION = 
      " and coalesce(pr.center_id, isr.center_id, phc.center_id) = ? ";
  
  private static final String BILL_ORDER_BY = 
      " ORDER BY b.open_date DESC ";

  public List<BasicDynaBean> getOpenFinalizedBillDetails(String mrNo, String visitType,
      int centerID) {
    if (centerID != 0) {
      return DatabaseHelper.queryToDynaList(
          GET_OPEN_FINALIZED_BILL_VISIT_DETAILS + CENTER_ID_CONDITION + BILL_ORDER_BY, mrNo, visitType, centerID);
    } else {
      return DatabaseHelper.queryToDynaList(GET_OPEN_FINALIZED_BILL_VISIT_DETAILS + BILL_ORDER_BY, mrNo, visitType);
    }
  }

  private static final String PHARMACY_BILLS_FOR_BILL = " SELECT sale_id "
      + " FROM store_sales_main WHERE bill_no=? ";

  public List<BasicDynaBean> getAllPharmacyBillsOfBill(String billNo) {
    return DatabaseHelper.queryToDynaList(PHARMACY_BILLS_FOR_BILL, billNo);
  }

  private static final String BILL_NOT_IN_HMS_ACCOUNTING_INFO = "SELECT b.bill_no, b.visit_id"
      + " FROM bill b "
      + " LEFT JOIN ("
      + "   SELECT voucher_no AS bill_no, max(bill_last_finalized_date) AS last_finalized_at"
      + "   FROM hms_accounting_info"
      + "   WHERE bill_last_finalized_date BETWEEN ? AND ? "
      + "     AND voucher_type IN (select voucher_definition from fa_voucher_definitions where voucher_key in ('VOUCHER_TYPE_HOSPBILLS','VOUCHER_TYPE_PHBILLS','VOUCHER_TYPE_PAYMENT','VOUCHER_TYPE_CREDITNOTE'))"
      + "   GROUP BY voucher_no) ac ON ac.bill_no = b.bill_no"
      + " WHERE b.last_finalized_at BETWEEN ? AND ?"
      + "   AND b.status IN ('F','C','X')"
      + "   AND (ac.last_finalized_at IS NULL OR ac.last_finalized_at != b.last_finalized_at)"
      + "   AND b.bill_no not in ("
	  + "   SELECT bz.bill_no FROM bill bz JOIN bill_charge bcz ON bcz.bill_no = bz.bill_no"
      + "     WHERE bz.last_finalized_at BETWEEN ? AND ?"
      + "     AND bz.status IN ('F','C','X')"
      + "     GROUP BY bz.bill_no HAVING max(bcz.act_rate) = 0 AND min(bcz.act_rate) = 0)";

  public List<BasicDynaBean> getBillsNotInHmsAccountingInfo(int relFromHour, int relToHour) {
	Timestamp now = DateUtil.getCurrentTimestamp();

	Timestamp startTime = DateUtil.addHours(now, relFromHour * -1);
	Timestamp endTime = DateUtil.addHours(now, relToHour * -1);
	
    return DatabaseHelper.queryToDynaList(BILL_NOT_IN_HMS_ACCOUNTING_INFO, 
    		new Object[] {startTime, endTime, startTime, endTime, startTime, endTime});
  }

}
