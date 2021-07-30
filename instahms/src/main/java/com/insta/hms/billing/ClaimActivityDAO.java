package com.insta.hms.billing;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ClaimActivityDAO {


	public static final String GET_PHARMACY_DENIED_CHARGE_NET =
		" SELECT sum(coalesce(scl.insurance_claim_amt, 0.00) + coalesce(scl.return_insurance_claim_amt, 0.00) - coalesce(scl.claim_recd, 0.00)) AS net, sum((coalesce(scl.tax_amt, 0.00))) AS vat_net ";

	public static final String GET_PHARMACY_CHARGE_NET =
		" SELECT sum((coalesce(scl.insurance_claim_amt, 0.00) + coalesce(scl.return_insurance_claim_amt, 0))) AS net, sum((coalesce(scl.tax_amt, 0.00))) AS vat_net ";

	private static final GenericDAO billClaimDAO = new GenericDAO("bill_claim");
	private static final GenericDAO patientRegistrationDAO = new GenericDAO("patient_registration");
	
	
	public static final String GET_ALL_PHARMACY_CHARGES_FIELDS =
		"	," +
		//"  s.batch_no, m.medicine_id::TEXT AS item_id, " +
		"   sum(s.quantity) AS qty," +
		"	sum(scl.insurance_claim_amt) AS insurance_claim_amt, " +
		"   sum(s.amount) AS amt, " +
		"   sum(scl.return_insurance_claim_amt) AS return_insurance_claim_amt, " +
		"   sum(s.return_amt) AS return_amt, " +
		"   sum(s.return_qty) AS return_qty, " +
		"	sum(s.amount + s.return_amt - s.tax - s.return_tax_amt) AS amount, " +
		"	sum(s.rate) AS rate ," +
		"	sum(s.quantity + s.return_qty) AS quantity, " +
		"   sum(s.disc) AS discount, " +
		//"   s.package_unit, " +
		//"   s.sale_item_id AS sale_item_id,
		"	scl.claim_activity_id as charge_id, " +
		"   max(sstd.tax_rate) AS vat_tax_rate, " +
		//"  bc.bill_no,b.bill_type,b.is_tpa," +
		"	s.item_code, 'P-'||scl.claim_activity_id AS activity_charge_id," +
		//"	m.medicine_name||' ('||bc.act_description||') ' AS act_description," +
		"	'Pharmacy' AS activity_group, " +
		"	max(sale_date) AS item_posted_date, to_char(max(sale_date),'dd/MM/yyyy hh24:mi') AS posted_date,  " +
		"   sum(coalesce(scl.claim_recd, 0.00)) as claim_recd_total," +
		"   array_to_string(array_agg(bc.charge_id), ',') as charge_id_list, " +
		//"	s.sale_id, bc.charge_id AS activity_id , " +
		"	msct.haad_code as act_type, msct.code_type AS act_type_desc, " +
		"	act_item_code, ic.main_visit_id, ic.claim_id, b.status AS bill_status, pr.visit_type AS patient_visit_type," +
		"	coalesce(scl.prior_auth_id, pip.prior_auth_id) AS prior_auth_id,  " +
		"	CASE WHEN (pr.op_type != 'O') THEN doc.doctor_license_number ELSE ref.doctor_license_number END AS doctor_license_number," +
		"	CASE WHEN (pr.op_type != 'O') THEN doc.doctor_name ELSE ref.referal_name END AS doctor_name," +
		"	CASE WHEN (pr.op_type != 'O') THEN doc.doctor_id ELSE ref.referal_no END AS doctor_id," +
		"	CASE WHEN (pr.op_type != 'O') THEN 'Doctor' ELSE ref.doctor_type END AS doctor_type,	" +
		"	min(drs.doctor_license_number) AS conducting_dr_license_number,m.issue_base_unit," +
		"	min(presc_doc.doctor_license_number) AS prescribing_doctor_license_number," +
		"	CASE WHEN bc.charge_group='DRG' THEN 'Y' ELSE 'N' END AS is_drg_group," +
		"   CASE WHEN bc.charge_head='MARDRG' THEN 'Y' ELSE 'N' END AS is_drg_charge, "+
		"   CASE WHEN bc.charge_group ='PDM' THEN 'Y' ELSE 'N' END AS is_perdiem_code, "+
		"   ref.doctor_license_number as ref_doctor_license_number, is_home_care_code,pr.dept_name  " ;


	public static final String GROUP_BY_PHARMACY_CHARGES = " GROUP BY scl.claim_activity_id, s.item_code, "+
		" sale_date::date , msct.haad_code,msct.code_type, "+
		" ic.main_visit_id, ic.claim_id, b.status,pr.visit_type, coalesce(scl.prior_auth_id, pip.prior_auth_id), "+
		" pr.op_type, doc.doctor_license_number," +
		" ref.doctor_license_number,doc.doctor_name,ref.referal_name,doc.doctor_id,ref.referal_no, ref.doctor_type,scl.claim_id," +
		" bc.act_item_code,m.issue_base_unit, ref_doctor_license_number, is_home_care_code, " +
		" CASE WHEN bc.charge_group='DRG' THEN 'Y' ELSE 'N' END ," +
		" CASE WHEN bc.charge_head='MARDRG' THEN 'Y' ELSE 'N' END, "+
		" CASE WHEN bc.charge_group='PDM' THEN 'Y' ELSE 'N' END,pr.dept_name, "+ 
		" dpt.is_referral_doc_as_ordering_clinician";


		public static final String GET_ALL_PHARMACY_CHARGES_TABLES =
		"	FROM store_sales_details s" +
		"	JOIN sales_claim_details scl ON (scl.sale_item_id = s.sale_item_id) " +
		"	LEFT JOIN store_sales_tax_details sstd ON (sstd.sale_item_id = s.sale_item_id) " +
		"	JOIN store_sales_main sm on (s.sale_id = sm.sale_id)" +
		"	JOIN store_item_details m ON s.medicine_id = m.medicine_id" +
		"	JOIN bill_charge bc ON (sm.charge_id= bc.charge_id)" +
		"   JOIN chargehead_constants chc ON (bc.charge_head = chc.chargehead_id) " +
		"   JOIN chargegroup_constants cgc ON (bc.charge_group = cgc.chargegroup_id)" +
		"	JOIN bill b ON (b.bill_no = bc.bill_no)" +
		"	JOIN bill_claim bcl ON (b.bill_no = bcl.bill_no and bcl.claim_id = scl.claim_id) " +
		"	JOIN patient_registration pr ON (pr.patient_id = b.visit_id)" +
		"	JOIN patient_insurance_plans pip ON (pip.patient_id = bcl.visit_id AND pip.plan_id = bcl.plan_id " +
		"		AND pip.sponsor_id = bcl.sponsor_id) "+
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
		"	LEFT JOIN per_diem_codes_master pcm ON (bc.act_rate_plan_item_code = pcm.per_diem_code)	"+
		"   LEFT JOIN department dpt ON(dpt.dept_id = pr.dept_name) " +
		"	WHERE (b.status != 'X' AND (b.status = 'F' OR b.status = 'C')) AND bc.status!='X' " ;


	public static final String GET_HOSPITAL_DENIED_CHARGE_NET =
		" SELECT sum(coalesce(bccl.insurance_claim_amt, 0.00) + coalesce(bcc.return_insurance_claim_amt, 0.00) - coalesce(bccl.claim_recd_total, 0.00)) AS net, sum((coalesce(bccl.tax_amt, 0.00))) AS vat_net ";

	public static final String GET_HOSPITAL_CHARGE_NET =
		" SELECT sum((coalesce(bccl.insurance_claim_amt, 0.00) + coalesce(bcc.return_insurance_claim_amt, 0.00))) AS net, sum((coalesce(bccl.tax_amt, 0.00))) AS vat_net ";


	public static final String GET_ALL_HOSPITAL_CHARGES_FIELDS =
		"	," +
		//" 	'' AS batch_no, '' AS item_id, " +
		"   sum(bcc.act_quantity) AS qty," +
		"	sum(bccl.insurance_claim_amt) AS insurance_claim_amt, " +
		"   sum(bcc.amount) AS amt, " +
		"   sum(bcc.return_insurance_claim_amt) AS return_insurance_claim_amt, " +
		"   sum(bcc.return_amt) AS return_amt, " +
		"   sum(bcc.return_qty) AS return_qty, " +
		"	sum(bcc.amount + bcc.return_amt) AS amount, " +
		"   sum(bcc.act_rate) AS rate," +
		"	sum(bcc.act_quantity + bcc.return_qty) AS quantity, " +
		"   sum(bcc.discount) as discount, " +
		//"   1 AS package_unit, " +
		//"   0 AS sale_item_id, " +
		"   bccl.claim_activity_id AS charge_id, " +
		"   max(bct.tax_rate) AS vat_tax_rate, " +
		//"   bcc.bill_no, b.bill_type,b.is_tpa," +
		"	(act_rate_plan_item_code) as item_code, 'A-'||bccl.claim_activity_id AS activity_charge_id," +
		"	'Hospital' AS activity_group, " +
		//"	to_char(posted_date, 'dd/MM/yyyy hh24:mi') AS posted_date,
		"   max(posted_date) AS item_posted_date," +
		"   to_char(max(posted_date), 'dd/MM/yyyy hh24:mi') AS posted_date," +
		"   sum(coalesce(bccl.claim_recd_total, 0.00)) as claim_recd_total, "+
                "   array_to_string(array_agg(bcc.charge_id), ',') as charge_id_list, " +
		//"	'' AS sale_id, '' AS activity_id ," +
		"	msct.haad_code as act_type, msct.code_type AS act_type_desc, " +
		"   '' AS act_item_code, ic.main_visit_id, ic.claim_id, b.status AS bill_status, pr.visit_type AS patient_visit_type," +
		"	COALESCE(bccl.prior_auth_id, pip.prior_auth_id, '') AS prior_auth_id, " +
		"	CASE WHEN (pr.op_type != 'O') THEN doc.doctor_license_number ELSE ref.doctor_license_number END AS doctor_license_number," +
		"	CASE WHEN (pr.op_type != 'O') THEN doc.doctor_name ELSE ref.referal_name END AS doctor_name," +
		"	CASE WHEN (pr.op_type != 'O') THEN doc.doctor_id ELSE ref.referal_no END AS doctor_id," +
		"	CASE WHEN (pr.op_type != 'O') THEN 'Doctor' ELSE ref.doctor_type END AS doctor_type, " +
		"	min(drs.doctor_license_number) AS conducting_dr_license_number,1 AS issue_base_unit," +
		"	min(presc_doc.doctor_license_number) AS prescribing_doctor_license_number," +
		"	CASE WHEN bcc.charge_group='DRG' THEN 'Y' ELSE 'N' END AS is_drg_group," +
		"   CASE WHEN bcc.charge_head='MARDRG' THEN 'Y' ELSE 'N' END AS is_drg_charge, "+
		"	CASE WHEN bcc.charge_group ='PDM' THEN 'Y' ELSE 'N' END as is_perdiem_code, " +
		"   ref.doctor_license_number as ref_doctor_license_number, is_home_care_code,pr.dept_name  " ;

		public static final String GET_ALL_HOSPITAL_CHARGES_TABLES =
		"	FROM bill_charge bcc " +
		"	JOIN bill_charge_claim bccl ON (bcc.charge_id = bccl.charge_id) " +
		"	LEFT JOIN bill_charge_tax bct ON (bcc.charge_id = bct.charge_id) " +
		"   JOIN chargehead_constants chc ON (bcc.charge_head = chc.chargehead_id) " +
		"   JOIN chargegroup_constants cgc ON (bcc.charge_group = cgc.chargegroup_id)" +
		"	JOIN bill b ON (b.bill_no = bcc.bill_no)" +
		"	JOIN bill_claim bcl ON (b.bill_no = bcl.bill_no and bcl.claim_id = bccl.claim_id) " +
		"	JOIN patient_registration pr ON (pr.patient_id = b.visit_id)" +
		"	JOIN patient_insurance_plans pip ON(pip.patient_id = bcl.visit_id AND pip.plan_id = bcl.plan_id AND " +
		"		pip.sponsor_id = bcl.sponsor_id) "+
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
		"	LEFT JOIN doctors drs ON (drs.doctor_id = bcc.payee_doctor_id)" +
		" 	LEFT JOIN per_diem_codes_master pcm ON (bcc.act_rate_plan_item_code = pcm.per_diem_code)" +
		" LEFT JOIN department dpt ON (dpt.dept_id = pr.dept_name) " +
		"	WHERE (b.status != 'X' AND (b.status = 'F' OR b.status = 'C')) AND bcc.status!='X' AND bcc.charge_head NOT IN ('PHMED','PHCMED','PHRET','PHCRET','ADJDRG') ";

	public static final String ORDER_BY_CHARGES = " ORDER BY charge_id, main_visit_id, claim_id" ;


	public static final String GROUP_BY_HOSPITAL_CHARGES = " GROUP BY bccl.claim_activity_id,act_rate_plan_item_code, "+
			" posted_date::date , msct.haad_code,msct.code_type, "+
			" ic.main_visit_id, ic.claim_id, b.status,pr.visit_type, coalesce(bccl.prior_auth_id, pip.prior_auth_id, ''),"+
			" pr.op_type, doc.doctor_license_number," +
			" ref.doctor_license_number,doc.doctor_name,ref.referal_name,doc.doctor_id,ref.referal_no, ref.doctor_type,bccl.claim_id," +
			" issue_base_unit, CASE WHEN bcc.charge_group='DRG' THEN 'Y' ELSE 'N' END," +
			" CASE WHEN bcc.charge_head='MARDRG' THEN 'Y' ELSE 'N' END, ref_doctor_license_number, is_home_care_code, "+
			" CASE WHEN bcc.charge_group='PDM' THEN 'Y' ELSE 'N' END,pr.dept_name, " +
	 		" dpt.is_referral_doc_as_ordering_clinician ";

	private static final String GET_DEPT_FIELD_FOR_CLAIM_ACTIVITY = " , COALESCE(dpt.is_referral_doc_as_ordering_clinician,'N') AS is_referral_doc_as_ordering_clinician";
	//	Filtered charges.
	public List<BasicDynaBean> findAllCharges(String claimId, Boolean ignoreExternalPbm) throws SQLException {

		BasicDynaBean billClaimBean = billClaimDAO.findByKey("claim_id", claimId);
		List<BasicDynaBean> allCharges = new ArrayList<>();
		if (billClaimBean != null) {
			String chargesQuery = null;
			BasicDynaBean visitbean = patientRegistrationDAO.findByKey("patient_id", billClaimBean.get("visit_id"));
			boolean hasDRG = visitbean != null && visitbean.get("use_drg").equals("Y");
			boolean hasPerdiem = visitbean != null && visitbean.get("use_perdiem").equals("Y");
			boolean checkDRGorPRD = (hasDRG || hasPerdiem);

		  	chargesQuery =
				GET_PHARMACY_CHARGE_NET + GET_DEPT_FIELD_FOR_CLAIM_ACTIVITY + GET_ALL_PHARMACY_CHARGES_FIELDS + GET_ALL_PHARMACY_CHARGES_TABLES
					+ " AND CASE WHEN (?) OR s.allow_zero_claim = true THEN (coalesce(scl.insurance_claim_amt, 0.00) + coalesce(scl.return_insurance_claim_amt, 0.00)) >= 0 "
					+ "	ELSE (coalesce(scl.insurance_claim_amt, 0.00) + coalesce(scl.return_insurance_claim_amt, 0.00)) > 0 END "
					+ " AND (s.quantity + s.return_qty) > 0 "
					+ " AND scl.claim_id = ? ";
		  if (ignoreExternalPbm) {
		    //  (This flag indicates that the sale bill claim will
		    //  be processed via an external software provider)
		    chargesQuery += " AND sm.is_external_pbm = FALSE ";
		  }
		  chargesQuery += GROUP_BY_PHARMACY_CHARGES
				+ " UNION ALL "
				+ GET_HOSPITAL_CHARGE_NET + GET_DEPT_FIELD_FOR_CLAIM_ACTIVITY 
				+ GET_ALL_HOSPITAL_CHARGES_FIELDS
				+ GET_ALL_HOSPITAL_CHARGES_TABLES
				+ " AND CASE WHEN (?) OR bcc.allow_zero_claim = true THEN (coalesce(bccl.insurance_claim_amt, 0.00) + coalesce(bcc.return_insurance_claim_amt, 0.00)) >= 0 "
				+ "	ELSE (coalesce(bccl.insurance_claim_amt, 0.00) + coalesce(bcc.return_insurance_claim_amt, 0.00)) > 0 END "
				+ " AND (bcc.act_quantity + bcc.return_qty) > 0 "
				+ " AND bccl.claim_id = ? " +
				GROUP_BY_HOSPITAL_CHARGES ;
		  allCharges =  DataBaseUtil.queryToDynaList(chargesQuery + ORDER_BY_CHARGES, new Object[] {checkDRGorPRD,claimId,checkDRGorPRD,claimId});
		}

		return allCharges;
	}

	// While Resubmitting Filtered charges, Resubmission type is Internal
	// complaint then,
	// only those activites which are denied should go in XML.
	public List<BasicDynaBean> findAllChargesForResub(String claimId, Boolean ignoreExternalPbm) throws SQLException {

		String chargesQuery = null;

		List<BasicDynaBean> allChargesForResub = new ArrayList<>();

		BasicDynaBean billClaimbean = billClaimDAO.findByKey("claim_id", claimId);

		if (billClaimbean != null) {

			BasicDynaBean visitbean = patientRegistrationDAO.findByKey("patient_id", billClaimbean.get("visit_id"));
			boolean hasDRG = visitbean != null && visitbean.get("use_drg").equals("Y");
			boolean hasPerdiem = visitbean != null && visitbean.get("use_perdiem").equals("Y");
			boolean checkDRGorPRD = (hasDRG || hasPerdiem);

		  chargesQuery =
				GET_PHARMACY_DENIED_CHARGE_NET + GET_DEPT_FIELD_FOR_CLAIM_ACTIVITY + GET_ALL_PHARMACY_CHARGES_FIELDS + GET_ALL_PHARMACY_CHARGES_TABLES
					+ " AND CASE WHEN (?) OR s.allow_zero_claim = true "
					+ "  THEN (coalesce(scl.insurance_claim_amt, 0.00) + coalesce(scl.return_insurance_claim_amt, 0.00)) >= 0 "
					+ "	ELSE (coalesce(scl.insurance_claim_amt, 0.00) + coalesce(scl.return_insurance_claim_amt, 0.00)) > 0 END"
					+ " AND (s.quantity + s.return_qty) > 0 "
					+ " AND (coalesce(scl.insurance_claim_amt, 0.00) > coalesce(scl.claim_recd, 0.00)) AND scl.claim_id = ? "
					+ " AND scl.closure_type != 'D' ";
		  if (ignoreExternalPbm) {
		    chargesQuery += " AND sm.is_external_pbm = FALSE ";
		  }
		  chargesQuery += GROUP_BY_PHARMACY_CHARGES

				+ " UNION ALL " +

				GET_HOSPITAL_DENIED_CHARGE_NET + GET_DEPT_FIELD_FOR_CLAIM_ACTIVITY + GET_ALL_HOSPITAL_CHARGES_FIELDS + GET_ALL_HOSPITAL_CHARGES_TABLES
					+ " AND CASE WHEN (?) OR bcc.allow_zero_claim = true "
					+ "  THEN (coalesce(bccl.insurance_claim_amt, 0.00) + coalesce(bcc.return_insurance_claim_amt, 0.00)) >= 0 "
					+ "	ELSE (coalesce(bccl.insurance_claim_amt, 0.00) + coalesce(bcc.return_insurance_claim_amt, 0.00)) > 0 END"
					+ " AND (bcc.act_quantity + bcc.return_qty) > 0 "
					+ " AND (coalesce(bccl.insurance_claim_amt, 0.00) > coalesce(bccl.claim_recd_total, 0.00)) AND bccl.claim_id = ? "
					+ " AND bccl.closure_type != 'D' "
					+  GROUP_BY_HOSPITAL_CHARGES ;
		  allChargesForResub =  DataBaseUtil.queryToDynaList(chargesQuery + ORDER_BY_CHARGES, new Object[] {checkDRGorPRD, claimId, checkDRGorPRD, claimId});
		}

		return allChargesForResub;
	}

	private static final String GET_DRG_ADJ_AMT_PER_PRIORITY = " SELECT sum(bc.amount) AS adjamt "
			+ " FROM bill_charge bc " + " JOIN bill_claim bcl ON(bcl.bill_no = bc.bill_no AND priority=?) "
			+ " WHERE charge_head='ADJDRG' AND bcl.claim_id=? AND bc.status != 'X' ";

	public BigDecimal getDRGAdjustmentAmt(String claimId, Integer priority) throws SQLException {
		BigDecimal drgAdjAmt = BigDecimal.ZERO;
		BasicDynaBean adjAmtBean = DataBaseUtil.queryToDynaBean(GET_DRG_ADJ_AMT_PER_PRIORITY,
				new Object[] { priority, claimId });
		if (adjAmtBean != null) {
			drgAdjAmt = adjAmtBean.get("adjamt") != null ? (BigDecimal) adjAmtBean.get("adjamt") : BigDecimal.ZERO;
		}
		return drgAdjAmt;
	}

	private static final String DRG_MARGIN_EXIST = " SELECT bc.* " + " FROM bill_charge bc "
			+ " JOIN bill_claim bcl ON(bcl.bill_no = bc.bill_no AND priority=?) "
			+ " WHERE charge_head='MARDRG' AND bcl.claim_id=? AND bc.status != 'X' ";

	public boolean getDRGMarginExist(String claimId, Integer priority) throws SQLException {
		boolean marginExist = false;
		BasicDynaBean drgMarginBean = DataBaseUtil.queryToDynaBean(DRG_MARGIN_EXIST, new Object[] { priority, claimId });
		if (drgMarginBean != null)
			marginExist = true;
		return marginExist;
	}
	
	private static final String GET_NOT_CONSUMED_INVENTORY_ITEM_AMT = "SELECT sum(bc.amount) "
	    + " as amount, sum(bc.tax_amt) as tax_amt, sum(bc.discount) as discount "
	    + " FROM bill_charge bc "
	    + " JOIN bill_claim bcl ON(bcl.bill_no = bc.bill_no AND bcl.claim_id = ?) "
	    + " WHERE bc.charge_head='PKGPKG' AND bc.submission_batch_type in ('P','I') ";

  public BasicDynaBean getNotConsumedInvItemAmt(String claimId) {
    return DatabaseHelper.queryToDynaBean(GET_NOT_CONSUMED_INVENTORY_ITEM_AMT, new Object[]{claimId});
  }

}
