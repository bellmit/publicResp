package com.insta.hms.core.billing;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Repository
public class BillChargeRepository extends GenericRepository {

	public BillChargeRepository() {
		super("bill_charge");
	}

	public static final String GET_BILL_FROM_CHARGE_ID = "SELECT b.status FROM bill_charge bc "
			+ " JOIN bill b USING (bill_no)  WHERE bc.charge_id=?";

	public BasicDynaBean getBillFromChargeId(String chargeId) {
		return DatabaseHelper.queryToDynaBean(GET_BILL_FROM_CHARGE_ID, chargeId);
	}


	private static final String DRG_MARGIN_EXIST = " SELECT bc.* "+
		      " FROM bill_charge bc "+
		      " JOIN bill_claim bcl ON(bcl.bill_no = bc.bill_no AND priority=?) "+
		      " WHERE charge_head='MARDRG' AND bcl.claim_id=? AND bc.status != 'X' "; 

		  public boolean getDRGMarginExist(String claim_id, Integer priority) {
		    boolean marginExist = false;
		    BasicDynaBean drgMarginBean = DatabaseHelper.queryToDynaBean(DRG_MARGIN_EXIST, new Object[] {priority, claim_id});
		    if(drgMarginBean != null) {
		      marginExist = true;
		    }
		    return marginExist;
		  }
	
	/*
	 * This is the common charge query that is used by multiple getXxx methods
	 * after adding one or more WHERE clauses.
	 */
	private static final String CHARGE_QUERY =
		  " SELECT coalesce(bill_charge.insurance_claim_amount,0) as insurance_claim_amount, "
		+ "  bill_charge.charge_id, bill_charge.bill_no, bill_charge.charge_group, bill_charge.charge_head, act_department_id, COALESCE(act_description,'') as act_description, "
		+ "  act_remarks, act_rate, act_unit, act_quantity, amount, discount, discount_reason, "
		+ "  charge_ref, paid_amount, posted_date, bill_charge.status, bill_charge.username, bill_charge.mod_time, bill_charge.approval_id, orig_rate, "
		+ "  package_unit, doctor_amount, doc_payment_id, ref_payment_id, oh_payment_id, act_description_id, "
		+ "  hasactivity, insurance_claim_amount, return_qty, bill_charge.return_insurance_claim_amt, return_amt, "
		+ "  payee_doctor_id, referal_amount, out_house_amount, "
		+ "  prescribing_dr_id, prescribing_dr_amount, prescribing_dr_payment_id, overall_discount_auth, "
		+ "  overall_discount_amt, discount_auth_dr, dr_discount_amt, discount_auth_pres_dr, pres_dr_discount_amt,"
		+ "  discount_auth_ref, ref_discount_amt, discount_auth_hosp, hosp_discount_amt, activity_conducted,"
		+ "  bill_charge.account_group, act_item_code, act_rate_plan_item_code, bill_charge.code_type, allow_discount, order_number,"
		+ "  chargegroup_name, chargehead_name, dept_name, prd.doctor_name as prescribing_dr_name, "
		+ "  dac.disc_auth_name AS discount_auth_dr_name, dap.disc_auth_name AS discount_auth_pres_dr_name,"
		+ "  dar.disc_auth_name AS discount_auth_ref_name, dah.disc_auth_name AS discount_auth_hosp_name,"
		+ "  daov.disc_auth_name AS overall_discount_auth_name, insurance_payable, "
		+ "  claim_service_tax_applicable,bill_charge.service_sub_group_id,ss.service_group_id,"
		+ "  ss.service_sub_group_name,sg.service_group_name, conducting_doc_mandatory, "
		+ "  CASE WHEN (bill_charge.charge_head = 'MARPKG') THEN 'N' "
		+ "  	  WHEN (bill.dyna_package_id = 0 OR (coalesce(bill_charge.qty_included,0) = coalesce(act_quantity,0))) THEN 'N' "
		+ "       WHEN (coalesce(bill_charge.amount_included,0) = 0) AND (coalesce(bill_charge.qty_included,0) = 0 AND (coalesce(amount,0) = 0 )) THEN 'Y' "
		+ "		  WHEN ((coalesce(bill_charge.amount_included,0) = coalesce(amount,0)) AND (coalesce(bill_charge.qty_included,0) = 0)) THEN 'N' "
		+ "  	  WHEN (coalesce(bill_charge.amount_included,0) = 0) AND (coalesce(bill_charge.qty_included,0) = 0) THEN 'Y' ELSE 'P' END AS charge_excluded, "
		+ "  bill_charge.amount_included, qty_included, package_finalized, "
		+ "  bill_charge.consultation_type_id, consultation_type, user_remarks, "
		+ "  bill_charge.insurance_category_id,bill_charge.prior_auth_id, bill_charge.prior_auth_mode_id, bill_charge.first_of_category, "
		+ "  op_id, bill_charge.from_date, bill_charge.to_date, tdv.dept_name, item_remarks, "
		+ "  bill_charge.allow_rate_increase, bill_charge.allow_rate_decrease, "
		+ "  bill_charge.claim_status, bill_charge.claim_recd_total, bill_charge.redeemed_points, "
		+ "  ss.eligible_to_redeem_points, ss.redemption_cap_percent, service_charge_applicable,"
		+ "  orig_insurance_claim_amount, bill.visit_id, bill.visit_type, is_tpa, "
		+ "  pbccl.insurance_claim_amt as pri_claim_amt, sbccl.insurance_claim_amt as sec_claim_amt, "
		+ "	 pbccl.prior_auth_id as pri_prior_auth_id, sbccl.prior_auth_id as sec_prior_auth_id, "
		+ "  pbccl.prior_auth_mode_id as pri_prior_auth_mode, sbccl.prior_auth_mode_id as sec_prior_auth_mode, "
		+ "  pbcl.plan_id as pri_plan_id, sbcl.plan_id as sec_plan_id, is_claim_locked, pbccl.include_in_claim_calc as pri_include_in_claim, "
		+ "	 sbccl.include_in_claim_calc as sec_include_in_claim "
		+ "  FROM bill_charge  "
		+ "  LEFT JOIN bill_claim pbcl ON(bill_charge.bill_no = pbcl.bill_no and pbcl.priority = 1) "
		+ "  LEFT JOIN bill_claim sbcl ON(bill_charge.bill_no = sbcl.bill_no and sbcl.priority = 2) "
		+ "  LEFT JOIN bill_charge_claim pbccl ON(bill_charge.charge_id = pbccl.charge_id and pbcl.claim_id = pbccl.claim_id) "
		+ "  LEFT JOIN bill_charge_claim sbccl ON(bill_charge.charge_id = sbccl.charge_id and sbcl.claim_id = sbccl.claim_id) "
		+ "	 JOIN bill ON (bill.bill_no = bill_charge.bill_no) "
		+ "  LEFT JOIN service_sub_groups ss using(service_sub_group_id) "
		+ "  LEFT JOIN service_groups sg using(service_group_id) "
		+ "  LEFT OUTER JOIN treating_departments_view tdv ON (bill_charge.act_department_id=tdv.dept_id) "
		+ "  JOIN chargehead_constants ON (bill_charge.charge_head = chargehead_constants.chargehead_id) "
		+ "  JOIN chargegroup_constants ON (bill_charge.charge_group = chargegroup_constants.chargegroup_id)"
		+ "  LEFT OUTER JOIN doctors prd ON (prd.doctor_id = bill_charge.prescribing_dr_id) "
		+ "  LEFT OUTER JOIN discount_authorizer dac ON (bill_charge.discount_auth_dr=dac.disc_auth_id)"
		+ "  LEFT OUTER JOIN discount_authorizer dap ON (bill_charge.discount_auth_pres_dr=dap.disc_auth_id)"
		+ "  LEFT OUTER JOIN discount_authorizer dar ON (bill_charge.discount_auth_ref = dar.disc_auth_id)"
		+ "  LEFT OUTER JOIN discount_authorizer dah ON (bill_charge.discount_auth_hosp = dah.disc_auth_id)"
		+ "  LEFT OUTER JOIN discount_authorizer daov ON (bill_charge.overall_discount_auth=daov.disc_auth_id)"
		+ "  LEFT OUTER JOIN consultation_types ct ON (bill_charge.consultation_type_id = ct.consultation_type_id) " ;
	
	
	/*
	 * Returns distinct  bill charge prescribing doctors applicable for the given bill number
	 */
	public static final String GET_BILL_CHARGE_PRES_DOCTORS = "SELECT DISTINCT bc.prescribing_dr_id "
			+ " FROM bill_charge bc "
			+ "	JOIN bill b ON (b.bill_no = bc.bill_no) "
			+ " WHERE b.visit_id=? ";

	/*
	 * Returns all charges applicable for the given bill number
	 */
	public static final String GET_BILL_CHARGES = CHARGE_QUERY
			+ " WHERE bill_charge.bill_no=? "
			+ " ORDER BY chargegroup_constants.display_order, chargehead_constants.display_order, "
			+ " bill_charge.posted_date ";

	 public List<BasicDynaBean> getBillCharges(Object[] object){ 
		 return DatabaseHelper.queryToDynaList(GET_BILL_CHARGES, object);
	 }

	 public List<BasicDynaBean> getChargePresDoctorsByVisit(Object[] object){
		 return DatabaseHelper.queryToDynaList(GET_BILL_CHARGE_PRES_DOCTORS, object);
	 }

	 public static final String CHARGE_PAYMENT_DETAILS =
			" SELECT bc.*, bac.activity_conducted,pr.center_id::text, " +
			"  pr.patient_id, pr.org_id, pr.reference_docto_id, "  +
			"  cd.payment_category::text as con_doc_category, cd.payment_eligible as con_doc_eligible, " +
			"  pd.payment_category::text as pres_doc_category, pd.payment_eligible as pres_doc_eligible, " +
			"  COALESCE(rd.payment_category::text, rr.payment_category::text) AS ref_doc_category, " +
			"  COALESCE(rd.payment_eligible, rr.payment_eligible) AS ref_doc_eligible " +
			" FROM bill_charge bc " +
			"  JOIN bill b USING(bill_no) " +
			"  LEFT JOIN bill_activity_charge bac USING(charge_id) " +
			"  LEFT JOIN patient_registration pr on pr.patient_id=b.visit_id " +
			"  LEFT JOIN incoming_sample_registration isr ON  (isr.billno = b.bill_no) "+
			"  LEFT JOIN doctors cd ON (cd.doctor_id = bc.payee_doctor_id) " +
			"  LEFT JOIN doctors pd ON (pd.doctor_id = bc.prescribing_dr_id) " +
			"  LEFT JOIN doctors rd ON (rd.doctor_id = pr.reference_docto_id OR  "+
			"  rd.doctor_id= isr.referring_doctor) " +
			"  LEFT JOIN referral rr ON (rr.referal_no = pr.reference_docto_id OR  " +
			"  rr.referal_no = isr.referring_doctor ) " +
			" WHERE bc.charge_id=? ";

	 public BasicDynaBean getChargePaymentDetails(String chargeId) {
		 return DatabaseHelper.queryToDynaBean(CHARGE_PAYMENT_DETAILS, new Object[]{chargeId});
	 }
	
	 public BasicDynaBean getPrefixPatternAndNextVal(){
		 return DatabaseHelper.queryToDynaBean(GET_SEQUENCE_ID_QUERY, "chargeid_sequence", "CHARGEID");
	 }
	 
	 private static final String GET_BILL_TOTALS_EXCLUDE_CHARGES =
       " SELECT SUM(COALESCE(amount, 0)) AS bill_total," +
           " SUM(COALESCE(tax_amt, 0)) AS bill_tax, "+
           " SUM(COALESCE(insurance_claim_amount, 0)) AS claim_total, " +
           " SUM(COALESCE(sponsor_tax_amt,0)) AS claim_tax "+
           " FROM bill_charge WHERE bill_no = ? AND status != 'X' ";

   public BasicDynaBean getBillChargeExcludeTotals(String billNo,List<String> chargeHeads) {
     String[] placeHolderArr = new String[chargeHeads.size()];
     Arrays.fill(placeHolderArr, "?");
     String placeHolders = StringUtils.arrayToCommaDelimitedString(placeHolderArr);
     String query = GET_BILL_TOTALS_EXCLUDE_CHARGES + "AND charge_head NOT IN ( " + placeHolders  + ")";
     List<Object> args = new ArrayList<Object>();
     args.add(billNo);  
     args.addAll(chargeHeads);
     return DatabaseHelper.queryToDynaBean(query, args.toArray());
   }

	 public static final String CANCEL_CHARGE = "UPDATE bill_charge " +
				" SET status='X', act_quantity=0, discount=0, amount=0, insurance_claim_amount=0, " +
				"  act_remarks = replace(act_remarks, 'Occupied', 'Cancelled'), username = ?, mod_time = NOW() " +
				" WHERE charge_id=?";

	 public static final String CANCEL_CHARGE_REFS= "UPDATE bill_charge " +
				" SET status='X', act_quantity=0, discount=0, amount=0, insurance_claim_amount=0, " +
				"  act_remarks = replace(act_remarks, 'Occupied', 'Cancelled'), username = ?, mod_time = NOW() " +
				" WHERE charge_ref=?";
	 
	 public int cancelCharge(String chargeId, boolean chargeRef,String userName){		 
		 int i =DatabaseHelper.update(CANCEL_CHARGE, new Object[]{userName,chargeId});
		 if(chargeRef)
			 i = DatabaseHelper.update(CANCEL_CHARGE_REFS, new Object[]{userName,chargeId});
		 return i;
	 }

	 public static final String GET_ASSOCIATED_CHARGES = "SELECT charge_id FROM bill_charge WHERE charge_ref = ?";
	
	 public List<BasicDynaBean> getAssociatedCharges(String chargeId) {
		 return DatabaseHelper.queryToDynaList(GET_ASSOCIATED_CHARGES, chargeId);
	 }
	 
	 private static final String UPDATE_HAS_ACTIVITY = "UPDATE bill_charge SET hasactivity=? WHERE charge_id=?";
	 private static final String UPDATE_HAS_ACTIVITY_REFS = "UPDATE bill_charge SET hasactivity=? WHERE charge_ref=?";

	 public int updateHasActivityStatus(String chargeId, boolean hasActivity, boolean refs){
		 int i =DatabaseHelper.update(UPDATE_HAS_ACTIVITY, new Object[]{hasActivity,chargeId});
		 if(refs)
			 i = DatabaseHelper.update(UPDATE_HAS_ACTIVITY_REFS, new Object[]{hasActivity,chargeId});
		 return i;
	 }
	 
	 public static final String UPDATE_PRESCRIBING_DR = " UPDATE bill_charge SET prescribing_dr_id=? WHERE charge_id=?";
	 
	 public static final String UPDATE_PRESCRIBING_DR_REFS = " UPDATE bill_charge SET prescribing_dr_id=? WHERE charge_ref=?";
	 
	 public int updatePrescribingDoctor(String chargeId, String docId, boolean updateRefs){
		 int i = DatabaseHelper.update(UPDATE_PRESCRIBING_DR, new Object[]{docId,chargeId});
		 if(updateRefs)
			 i = DatabaseHelper.update(UPDATE_PRESCRIBING_DR_REFS, new Object[]{docId,chargeId});
		 return i;
	 }

	 public static final String GET_CHARGE = CHARGE_QUERY + " WHERE bill_charge.charge_id=?";
	 
	public BasicDynaBean getCharge(String chargeId) {
		return DatabaseHelper.queryToDynaBean(GET_CHARGE, chargeId);
	}

	private static final String UPDATE_CHARGE_AMOUNTS = "UPDATE bill_charge SET "
			+ "  act_remarks=?, act_rate=?, act_quantity=?, act_item_code=?, act_rate_plan_item_code=?,"
			+ "  amount=?, discount=?, discount_reason=?, "
			+ "  status=?, posted_date=?, mod_time=?, username=?,  "
			+ "  insurance_claim_amount=?, discount_auth_dr=?, dr_discount_amt=?, discount_auth_pres_dr=?,"
			+ "  pres_dr_discount_amt=?, discount_auth_ref=?, ref_discount_amt=?, discount_auth_hosp=?, "
			+ "  hosp_discount_amt=?, overall_discount_auth=?, overall_discount_amt=?, payee_doctor_id=?, "
			+ "  conducting_doc_mandatory=?, amount_included=?, qty_included=?, package_finalized = ?, "
			+ "	 user_remarks=?, insurance_category_id=?,"
			+ "  prior_auth_id = ?, prior_auth_mode_id=?,  act_unit = ?, first_of_category = ?,act_description = ?,"
			+ "  act_description_id = ? , item_remarks = ?, redeemed_points = ?, is_claim_locked = ? "
			+ " WHERE charge_id=?";

	private static final String GET_BILL_STATUS = "SELECT b.status  FROM bill_charge bc "
			+ " JOIN bill b USING (bill_no) WHERE bc.charge_id=?";
	
	public String getBillStatus(String chargeId) {
		return DatabaseHelper.getString(GET_BILL_STATUS, chargeId);
	}

	public Boolean updateCharges(String query, String ratePlanId, String bedType) {
		// TODO Auto-generated method stub
		return DatabaseHelper.update(query, new Object[]{ratePlanId, bedType}) >= 0;
	}

	public Boolean updateCharges(String query) {
		// TODO Auto-generated method stub
		return DatabaseHelper.update(query) >= 0;
	}

	public List<BasicDynaBean> getCharges(String query) {
		// TODO Auto-generated method stub
		return DatabaseHelper.queryToDynaList(query);
	}
	
	private static final String UNLOCK_BILL_CHARGES = "UPDATE bill_charge SET is_claim_locked = false WHERE bill_no = ? ";

	public boolean unlockBillCharges(String billNo) {
		// TODO Auto-generated method stub
		return DatabaseHelper.update(UNLOCK_BILL_CHARGES, new Object[]{billNo}) >= 0;
	}

	public static final String UNLOCK_VISIT_BILLS_CHARGES = " UPDATE bill_charge bc SET is_claim_locked=false " +
			" FROM bill b WHERE b.bill_no = bc.bill_no AND " +
			" bc.is_claim_locked=true AND b.visit_id = ? ";
	
	public Boolean unlockVisitBillsCharges(String visitId, String billStatus) {
		// TODO Auto-generated method stub 
		String query = billStatus.equals("open") ? UNLOCK_VISIT_BILLS_CHARGES.concat(" AND b.status='A' ") : UNLOCK_VISIT_BILLS_CHARGES;
		return DatabaseHelper.update(query, new Object[]{visitId}) >= 0;
	}
	
	private static final String SET_ISSUE_RETURN_INS_CLAIM_AMT_TO_ZERO = " UPDATE bill_charge_claim bcc "+
			" set insurance_claim_amt = 0 "+
			" FROM bill_charge bc "+
			" JOIN bill b ON(b.bill_no = bc.bill_no) "+
			" WHERE bcc.charge_id = bc.charge_id AND bcc.charge_head in('INVRET') AND b.visit_id=? ";

	public Boolean setIssueReturnsClaimAmountTOZero(String visitId,
			String billStatus) {
		// TODO Auto-generated method stub
		String query = billStatus.equals("open") ? SET_ISSUE_RETURN_INS_CLAIM_AMT_TO_ZERO.concat(" AND b.status='A' ") : SET_ISSUE_RETURN_INS_CLAIM_AMT_TO_ZERO;
		return DatabaseHelper.update(query, new Object[]{visitId}) >= 0;
	}
	
	private static final String GET_ALL_BILL_CHARGES = " SELECT * FROM bill_charge bc WHERE bc.bill_no in (#) ";

  public List<BasicDynaBean> getAllBillCharges(String billNos) {
    // TODO Auto-generated method stub
    String query = GET_ALL_BILL_CHARGES;
    query = query.replaceAll("#", billNos);
    return DatabaseHelper.queryToDynaList(query);
  }
  
  private static final String GET_VISIT_BILL_CHARGES = "SELECT bc.* FROM bill_charge bc JOIN bill b ON(b.bill_no = bc.bill_no) WHERE b.visit_id = ?";
  
  public List<BasicDynaBean> getVisitBillCharges(String visitId){
    return DatabaseHelper.queryToDynaList(GET_VISIT_BILL_CHARGES, new Object[]{visitId});
  }
  
  private static final String GET_VISIT_BILL_CHARGES_WITH_PREAUTH = GET_VISIT_BILL_CHARGES
      + " AND preauth_act_id IS NOT NULL";

  public List<BasicDynaBean> getVisitBillChargesWithPreAuth(String visitId) {
    return DatabaseHelper.queryToDynaList(GET_VISIT_BILL_CHARGES_WITH_PREAUTH,
        new Object[] { visitId });
  }

  private static final String GET_BILL_CHARGES_EXCLUDING_PHARMACY = " SELECT * FROM bill_charge bc WHERE bc.bill_no in (#) "
      + " AND bc.charge_group not in('MED', 'ITE', 'RET')";
  
  public List<BasicDynaBean> getBillChargesExcludingPharmacy(String billNos) {
    // TODO Auto-generated method stub
    String query = GET_BILL_CHARGES_EXCLUDING_PHARMACY;
    query = query.replaceAll("#", billNos);
    return DatabaseHelper.queryToDynaList(query);
  }

  public static final String CANCEL_CHARGE_UPDATE = "UPDATE bill_charge "
      + " SET status='X', act_quantity=0, discount=0, amount=0, insurance_claim_amount=0, "
      + " tax_amt=0, sponsor_tax_amt=0, "
      + " activity_conducted = 'N', "
      + "  act_remarks = replace(act_remarks, 'Occupied', 'Cancelled'), username = ?, mod_time = NOW() "
      + " WHERE charge_id=?";

  /**
   * This method will change the status of a charge to 'X' which means cancelled charge. If
   * cancelRefs is true, it will also cancel charges whose charge_ref is this charge. This is mainly
   * called when cancelling an order.
   */
  public int cancelChargeUpdate(String chargeId, boolean cancelRefs, String userName) {
    Object[] values = new Object[] { userName, chargeId };
    StringBuilder query = new StringBuilder(CANCEL_CHARGE_UPDATE);
    if (cancelRefs) {
      query.append(" OR charge_ref = ? ");
      values = new Object[] { userName, chargeId, chargeId };
    }

    return DatabaseHelper.update(query.toString(), values);
  }

  public static final String GET_CHARGE_REFERENCES = "SELECT * FROM bill_charge WHERE charge_ref = ?";

  public List<BasicDynaBean> getChargeReferences(String chargeId) {
    return DatabaseHelper.queryToDynaList(GET_CHARGE_REFERENCES, chargeId);
  }

  private static final String GET_EXCLUDED_BED_CHARGES =
      " SELECT charge_id FROM bill_charge " +
      " WHERE bill_no = ? AND status != 'X' AND charge_group IN ('BED','ICU') " +
      " AND charge_excluded = 'Y' AND hasactivity = false ";
  
  public List<BasicDynaBean> getExcludedBedCharges(String billNo){
    return DatabaseHelper.queryToDynaList(GET_EXCLUDED_BED_CHARGES, billNo);
  }
  
  public static final String GET_CHARGE_AND_REFS = " select * from bill_charge bc " +
      " WHERE bc.charge_id=? OR charge_ref=? AND bc.status != 'X' " +
      " ORDER BY (charge_ref is null) desc, bc.charge_id ";

  public List<BasicDynaBean> getChargeAndRefs(String chargeId){
    return DatabaseHelper.queryToDynaList(GET_CHARGE_AND_REFS, new Object[]{chargeId, chargeId});
  }

  public static final String CANCEL_CHARGE_UPDATE_AUDIT_LOG = "UPDATE bill_charge "
      + " SET status='X', act_quantity=0, discount=0, amount=0, insurance_claim_amount=0, "
      + "  act_remarks = replace(act_remarks, 'Occupied', 'Cancelled'), username = ?, mod_time = NOW() "
      + " WHERE charge_id=?";

  /**
   * This method will change the status of a charge to 'X' which means
   * cancelled charge. If cancelRefs is true, it will also cancel charges
   * whose charge_ref is this charge. This is mainly called when cancelling an
   * order.
   */
  public int cancelChargeUpdateAuditLog(String chargeId, boolean cancelRefs, String userName) {
    Object[] values = new Object[] { userName, chargeId };
    StringBuilder query = new StringBuilder(CANCEL_CHARGE_UPDATE_AUDIT_LOG);
    if (cancelRefs) {
      query.append(" OR charge_ref = ? ");
      values = new Object[] { userName, chargeId, chargeId };
    }

    return DatabaseHelper.update(query.toString(), values);
  }

  private static final String UPDATE_CHARGE_EXCLUDED =
      " UPDATE bill_charge SET charge_excluded = 'N' WHERE bill_no = ? ";

  public void updateChargeExcluded(String billNo) {
     DatabaseHelper.update(UPDATE_CHARGE_EXCLUDED, new Object[]{billNo});
    
  }
  
  /** The Constant GET_ADJUSTMENT_AMT. */
  private static final String GET_ADJUSTMENT_AMT = "SELECT sum(amount) as amount, sum(tax_amt) as tax "
      + " FROM bill_charge where charge_head not in('BPDRG','OUTDRG','APDRG','ADJDRG') "
      + " AND status != 'X' AND bill_no = ?";

  /**
   * Gets the adjustment amount and tax.
   *
   * @param billNo
   *          String
   * @return the sum of adjustment amount and adjustment tax as BasicDynaBean
   */
  public BasicDynaBean getAdjustmentAmt(String billNo) {
    return DatabaseHelper.queryToDynaBean(GET_ADJUSTMENT_AMT, billNo);
  }

  /** The Constant GET_HCPCS_ITEMS_TOTAL_AMT. */
  private static final String GET_HCPCS_ITEMS_TOTAL_AMT = "SELECT SUM(bill_total) as "
      + " hcpcs_items_total FROM ( " + "SELECT SUM(COALESCE(amount, 0)) bill_total FROM bill_charge"
      + " bc JOIN store_item_details sit ON (sit.medicine_id::text=bc.act_description_id::text and "
      + " sit.high_cost_consumable= 'Y') "
      + " WHERE bc.bill_no = ? AND bc.status != 'X' AND bc.charge_head IN ('INVITE', 'INVRET')"
      + " UNION ALL " + "SELECT SUM(COALESCE(ssd.amount, 0)) - SUM(COALESCE(ssd.tax, 0)) bill_total"
      + " FROM store_sales_main ssm "
      + " JOIN store_sales_details ssd ON (ssd.sale_id = ssm.sale_id)"
      + " JOIN store_item_details sid ON (sid.medicine_id = ssd.medicine_id "
      + " AND sid.high_cost_consumable='Y')" + " WHERE ssm.bill_no = ? " + ") as foo";

  /**
   * Gets the adds the on payment amt.
   *
   * @param billNo
   *          String
   * @return the adds the on payment amt
   */
  public BigDecimal getAddOnPaymentAmt(String billNo) {
    return DatabaseHelper.getBigDecimal(GET_HCPCS_ITEMS_TOTAL_AMT, new Object[] { billNo, billNo });
  }

  /** The Constant CANCEL_ADD_ON_PAYMENT_DRG_ITEMS. */
  private static final String CANCEL_ADD_ON_PAYMENT_DRG_ITEMS = "UPDATE bill_charge SET status = "
      + " 'X', amount=0.00, tax_amt=0.00, act_rate=0.00, act_rate_plan_item_code = ''  "
      + " WHERE charge_head ='APDRG' AND bill_no=? ";

  /**
   * Cancel add on payment DRG items.
   *
   * @param billNo
   *          String
   * @return the boolean
   */
  public Boolean cancelAddOnPaymentDRGItems(String billNo) {
    return DatabaseHelper.update(CANCEL_ADD_ON_PAYMENT_DRG_ITEMS, billNo) > 0;
  }
  
  private static final String GET_DRG_ADJ_AMT_PER_PRIORITY = " SELECT sum(bc.amount) AS adjamt "+
	      " FROM bill_charge bc "+
	      " JOIN bill_claim bcl ON(bcl.bill_no = bc.bill_no AND priority=?) "+
	      " WHERE charge_head='ADJDRG' AND bcl.claim_id=? AND bc.status != 'X' "; 

	  public BigDecimal getDRGAdjustmentAmt(String claimId, Integer priority) {
	    BigDecimal drgAdjAmt = BigDecimal.ZERO;
	    BasicDynaBean adjAmtBean = DatabaseHelper.queryToDynaBean(GET_DRG_ADJ_AMT_PER_PRIORITY, new Object[] {priority, claimId});
	    if(adjAmtBean != null) {
	      drgAdjAmt = adjAmtBean.get("adjamt") != null ? (BigDecimal)adjAmtBean.get("adjamt") : BigDecimal.ZERO;
	    }
	    return drgAdjAmt;
	  }

  /** The Constant CANCEL_DRG_OUTLIER_AMOUNT. */
  private static final String CANCEL_DRG_OUTLIER_AMOUNT = "UPDATE bill_charge SET status = 'X',"
      + " amount=0.00, tax_amt=0.00, act_rate=0.00, act_rate_plan_item_code = ''  "
      + " WHERE charge_head ='OUTDRG' AND bill_no=? ";

  /**
   * Cancel DRG outlier amount entry.
   *
   * @param billNo
   *          String
   * @return the boolean
   */
  public Boolean cancelDRGOutlierAmountEntry(String billNo) {
    return DatabaseHelper.update(CANCEL_DRG_OUTLIER_AMOUNT, billNo) > 0;
  }

  /** The Constant CANCEL_DRG_ITEMS. */
  private static final String CANCEL_DRG_ITEMS = "UPDATE bill_charge SET status = 'X', amount=0.00,"
      + " tax_amt=0.00, act_rate=0.00, act_rate_plan_item_code = ''  "
      + " WHERE charge_head in('BPDRG','ADJDRG','OUTDRG','APDRG','MARDRG') AND bill_no=? ";

  /**
   * Cancel DRG items.
   *
   * @param billNo
   *          String
   */
  public void cancelDRGItems(String billNo) {
    DatabaseHelper.update(CANCEL_DRG_ITEMS, billNo);
  }

  /** The Constant UNLOCK_ITEMS_IN_DRG_BILL. */
  private static final String UNLOCK_ITEMS_IN_DRG_BILL = "UPDATE bill_charge SET "
      + " is_claim_locked = false WHERE bill_no = ? "
      + " AND charge_head NOT IN('PHCMED','PHMED','PHRET','PHCRET','INVRET')";

  /**
   * Un lock items in drg bill.
   *
   * @param billNo
   *          String
   */
  public void unLockItemsInDrgBill(String billNo) {
    DatabaseHelper.update(UNLOCK_ITEMS_IN_DRG_BILL, billNo);
  }

  /** The Constant LOCK_ITEMS_IN_DRG_BILL. */
  private static final String LOCK_ITEMS_IN_DRG_BILL = "UPDATE bill_charge "
      + " SET is_claim_locked = true "
      + " WHERE charge_head NOT IN('BPDRG','OUTDRG','APDRG') AND bill_no = ?";

  /**
   * Lock items in DRG bill.
   *
   * @param billNo
   *          String
   * @return the boolean
   */
  public Boolean lockItemsInDRGBill(String billNo) {
    return DatabaseHelper.update(LOCK_ITEMS_IN_DRG_BILL, billNo) > 0;
  }

  /** The Constant GET_ALL_HOSP_CHARGES_FOR_DRG. */
  public static final String GET_ALL_HOSP_CHARGES_FOR_DRG = "SELECT * " + " FROM bill_charge "
      + " WHERE charge_group NOT IN ('MED','ITE','RET','TAX','DIS','DRG','PDM') AND "
      + " status != 'X' AND bill_no = ? ";

  /**
   * Gets the all hospital charges for DRG.
   *
   * @param billNo
   *          String
   * @return the all hospital charges for DRG
   */
  public List<BasicDynaBean> getAllHospitalChargesForDRG(String billNo) {
    return DatabaseHelper.queryToDynaList(GET_ALL_HOSP_CHARGES_FOR_DRG, billNo);
  }

  /** The Constant GET_INV_AND_PHARM_TOTAL_AMT. */
  public static final String GET_INV_AND_PHARM_TOTAL_AMT = "SELECT SUM(amount+return_amt) "
      + " as totalinvandpharmamt " + " FROM bill_charge "
      + " WHERE charge_group IN ('ITE','MED') AND " + " status != 'X' AND bill_no = ? ";

  /**
   * Gets the inv and pharm total amt.
   *
   * @param billNo
   *          String
   * @return the inventory and pharmacy total amt
   */
  public BasicDynaBean getInvAndPharmTotalAmt(String billNo) {
    return DatabaseHelper.queryToDynaBean(GET_INV_AND_PHARM_TOTAL_AMT, billNo);
  }

  private static final String UPDATE_DISCOUNT_AUTH_AS_RATE_PLAN_DISCOUNT ="UPDATE bill_charge bc SET overall_discount_auth = -1, "
      + " is_system_discount ='Y' WHERE bc.discount > 0 AND bc.bill_no in (#) ";
  
  public Boolean updateDiscountAuthAsRatePlanDiscount(String billNos) {
    
    String query = UPDATE_DISCOUNT_AUTH_AS_RATE_PLAN_DISCOUNT;
    query = query.replaceAll("#", billNos);
    
    return DatabaseHelper.update(query) >= 0;
  }
  
  private static final String GET_CHARGES_FOR_PRIOR_AUTH = "SELECT bc.*, CASE "
      + "WHEN bac.activity_code='DIA' THEN tp.doc_presc_id "
      + "WHEN bac.activity_code='SER' THEN sp.doc_presc_id "
      + "WHEN bac.activity_code='DOC' THEN dc.doc_presc_id "
      + "WHEN bac.activity_code='PKG' THEN pp.doc_presc_id " 
      + "ELSE 0 END AS doc_presc_id "
      + "FROM bill_charge bc "
      + "JOIN bill_activity_charge bac ON (bc.charge_id = bac.charge_id) "
      + "LEFT JOIN services_prescribed sp ON (bac.activity_id::integer=sp.prescription_id AND bac.activity_code='SER') "
      + "LEFT JOIN tests_prescribed tp ON (bac.activity_id::integer=tp.prescribed_id AND bac.activity_code='DIA') "
      + "LEFT JOIN doctor_consultation dc ON (bac.activity_id::integer=dc.consultation_id AND bac.activity_code='DOC') "
      + "LEFT JOIN equipment_prescribed ep ON (bac.activity_id::integer=ep.prescribed_id AND bac.activity_code='EQU') "
      + "LEFT JOIN package_prescribed pp ON (bac.activity_id::integer=pp.prescription_id AND bac.activity_code='PKG') "
      + "WHERE bc.preauth_act_id IS NULL AND bc.hasactivity AND bc.bill_no = :billNo "
      + "AND bc.status != 'X' AND bc.charge_group NOT IN (:excludedChargeGroups)";

  public List<BasicDynaBean> getChargesForPriorAuth(String billNo,
      Set<String> excludedChargeGroups) {
    MapSqlParameterSource params = new MapSqlParameterSource();
    params.addValue("billNo", billNo);
    params.addValue("excludedChargeGroups", excludedChargeGroups);
    return DatabaseHelper.queryToDynaList(GET_CHARGES_FOR_PRIOR_AUTH, params);
  }
  
  private static final String GET_CHARGES_FOR_PRIOR_AUTH_BY_ID = "SELECT bc.*, CASE "
      + "WHEN bac.activity_code='DIA' THEN tp.doc_presc_id "
      + "WHEN bac.activity_code='SER' THEN sp.doc_presc_id "
      + "WHEN bac.activity_code='DOC' THEN dc.doc_presc_id "
      + "WHEN bac.activity_code='PKG' THEN pp.doc_presc_id " 
      + "ELSE 0 END AS doc_presc_id "
      + "FROM bill_charge bc "
      + "JOIN bill_activity_charge bac ON (bc.charge_id = bac.charge_id) "
      + "LEFT JOIN services_prescribed sp ON (bac.activity_id=sp.prescription_id::text AND bac.activity_code='SER') "
      + "LEFT JOIN tests_prescribed tp ON (bac.activity_id=tp.prescribed_id::text AND bac.activity_code='DIA') "
      + "LEFT JOIN doctor_consultation dc ON (bac.activity_id=dc.consultation_id::text AND bac.activity_code='DOC') "
      + "LEFT JOIN equipment_prescribed ep ON (bac.activity_id=ep.prescribed_id::text AND bac.activity_code='EQU') "
      + "LEFT JOIN package_prescribed pp ON (bac.activity_id=pp.prescription_id::text AND bac.activity_code='PKG') "
      + "WHERE bc.charge_id IN (:chargeIds)";

  public List<BasicDynaBean> getChargesForPriorAuth(List<String> chargeIds) {
    MapSqlParameterSource params = new MapSqlParameterSource();
    params.addValue("chargeIds", chargeIds);
    return DatabaseHelper.queryToDynaList(GET_CHARGES_FOR_PRIOR_AUTH_BY_ID, params);
  }
  
  private static final String UPDATE_CLAIM_AMT_AND_LOCK_STATUS_BASED_ON_PREAUTH =
      "UPDATE bill_charge bcharge  " //
          + " set insurance_claim_amount = foo.claim_net_approved_amount,  " //
          + " is_claim_locked = foo.is_claim_locked, " //
          + " include_in_claim_calc = foo.include_in_claim_calc   " //
          + " FROM ( " //
          + "       SELECT CASE WHEN pip.insurance_co IS NOT NULL AND ppa.status= 'A' " //
          + "                  AND pip.insurance_co = pp.preauth_payer_id  " //
          + "                   THEN ppa.claim_net_approved_amount " //
          + "                   ELSE 0.00 " //
          + "              END AS claim_net_approved_amount, " //
          + "        bcc.claim_id, bc.charge_id, " //
          + "        CASE WHEN ppa.status='D' AND ppa.claim_net_approved_amount =0 " //
          + "             THEN false " //
          + "             ELSE true  " //
          + "        END AS include_in_claim_calc , " //
          + "        CASE WHEN pip.insurance_co IS NOT NULL AND ppa.status= 'A' " //
          + "                  AND pip.insurance_co = pp.preauth_payer_id " //
          + "             THEN true " //
          + "             ELSE false " //
          + "        END AS is_claim_locked  " //
          + "        FROM bill_charge_claim bcc  " //
          + "        JOIN bill_charge bc ON " //
          + "                     (bc.charge_id = bcc.charge_id AND "
          + "                      bc.preauth_act_id IS NOT NULL) " //
          + "        JOIN bill b ON (b.bill_no = bcc.bill_no) " //
          + "        LEFT JOIN patient_insurance_plans pip ON " //
          + "                  (pip.patient_id = b.visit_id AND priority = 1)" //
          + "        LEFT JOIN preauth_prescription_activities ppa ON " //
          + "                  (bc.preauth_act_id = ppa.preauth_act_id) " //
          + "        LEFT JOIN preauth_prescription pp ON "
          + "                  (pp.preauth_presc_id = ppa.preauth_presc_id)" //
          + "        WHERE b.visit_id=? AND b.status='A' AND bc.status='A'" //
          + " ) as foo   " //
          + " WHERE foo.charge_id = bcharge.charge_id";

  public Integer setClaimAmountAndExclusionBasedOnPreAuth(String visitId) {
    return DatabaseHelper.update(UPDATE_CLAIM_AMT_AND_LOCK_STATUS_BASED_ON_PREAUTH, visitId);
  }
  
  private static final String UPDATE_CLAIM_AMT_AND_EXCLUSION_BASED_ON_PREAUTH_ACTIVITY =
      "UPDATE bill_charge bcharge  " //
          + " SET insurance_claim_amount = ?, " //
          + " is_claim_locked = TRUE " //
          + " FROM ( " //
          + "       SELECT bcc.claim_id, bc.charge_id " //
          + "        FROM bill_charge_claim bcc  " //
          + "        JOIN bill_charge bc ON (bc.charge_id = bcc.charge_id) " //
          + "        JOIN bill b ON (b.bill_no = bcc.bill_no) " //
          + "        JOIN patient_insurance_plans pip ON " //
          + "                (pip.patient_id = b.visit_id AND priority = 1)" //
          + "        JOIN preauth_prescription_activities ppa ON " //
          + "                (bc.preauth_act_id = ppa.preauth_act_id AND ppa.added_to_bill='Y') "//
          + "        JOIN preauth_prescription pp ON "
          + "                (pip.insurance_co = pp.preauth_payer_id "
          + "                    AND pp.preauth_presc_id = ppa.preauth_presc_id)" //
          + "        WHERE bc.preauth_act_id=? AND b.status='A' AND bc.status='A' " //
          + " ) as foo   " //
          + " WHERE foo.charge_id = bcharge.charge_id";
  
  public Integer setPriorAuthApprovalAmountAsClaimAmount(int preauthActId, Object approvedAmount) {
    return DatabaseHelper.update(UPDATE_CLAIM_AMT_AND_EXCLUSION_BASED_ON_PREAUTH_ACTIVITY, approvedAmount, (Object)preauthActId);
  }

  private static final String UPDATE_PREAUTH_DETAILS_FOR_CHARGE = "UPDATE "
      + "bill_charge SET prior_auth_id = ?, prior_auth_mode_id = ? "
      + "WHERE preauth_act_id = ?";

  public Integer setPriorAuthDetailsForCharges(int preAuthActId, String priorAuthId, Integer priorAuthModeId) {
    return DatabaseHelper.update(UPDATE_PREAUTH_DETAILS_FOR_CHARGE, priorAuthId, priorAuthModeId, preAuthActId);
  }

	private static final String LOCK_CHARGES =
			"UPDATE bill_charge SET is_claim_locked = true WHERE charge_id in (:chargeIds)";

	public Integer lockChargeClaim(List<String> chargeIds) {
		MapSqlParameterSource parameters = new MapSqlParameterSource();
		parameters.addValue("chargeIds", chargeIds);
		return DatabaseHelper.update(LOCK_CHARGES, parameters);
	}
	private static final String UNLOCK_CHARGES =
			"UPDATE bill_charge SET is_claim_locked = false WHERE charge_id in (:chargeIds)";

	public Integer unlockChargeClaim(List<String> chargeIds) {
		MapSqlParameterSource parameters = new MapSqlParameterSource();
		parameters.addValue("chargeIds", chargeIds);
		return DatabaseHelper.update(UNLOCK_CHARGES, parameters);
	}

}
