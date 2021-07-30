package com.insta.hms.core.billing;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * The Class StoreSalesDetailsRepository.
 */
@Repository
public class StoreSalesDetailsRepository extends GenericRepository{
	
	/**
	 * Instantiates a new store sales details repository.
	 */
	public StoreSalesDetailsRepository() {
		super("store_sales_details");
	}
	
	//To:Do this query brings a lot of redundant fields, needs to be cleaned up
	public static final String GET_ALL_CHARGES_HOSPITAL_PHARMACY=
			"SELECT sum((coalesce(s.original_tax_amt, 0.00))) AS vat_net, " +
			" COALESCE(dpt.is_referral_doc_as_ordering_clinician,'N') " +
			"     AS is_referral_doc_as_ordering_clinician, " +
			"           presc_doc.doctor_name as prescribing_doctor_name, " +
			"			      presc_doc.doctor_id AS prescribing_doctor_id, " +
			"           sum(s.quantity) AS qty, " +
			"			      bc.charge_group AS charge_group," +
			"		        bc.act_description AS act_description," +
			"		        bc.act_description_id AS act_description_id," +
			"	      		bc.charge_head AS charge_head," +
			"	      		'P-'||sm.charge_id||'-'||s.sale_item_id AS charge_id," +
			"           sum(0.0) AS net, " +
			"           sum(s.return_amt) AS return_amt, " +
			"           sum(s.return_qty) AS return_qty, " +
			"           (s.amount + s.return_amt - s.tax - s.return_tax_amt) AS amount, " +
			"           sum(s.rate) AS rate, " +
			"           sum(s.quantity + s.return_qty) AS quantity, " +
			"           sum(s.disc) AS discount, " +
			"           max(sstd.tax_rate) AS vat_tax_rate, " +
			"	        (item_code) AS item_code, " +
			"           'Pharmacy' AS activity_group, " +
			"           max(sale_date) AS item_posted_date, " +
			"           to_char(max(sale_date),'dd/MM/yyyy hh24:mi') AS posted_date, " +
			"           array_to_string(array_agg(bc.charge_id), ',') AS charge_id_list, " +
			"           msct.haad_code AS act_type, " +
			"           msct.code_type AS act_type_desc, " +
			"           chc.codification_supported, " +
			"           act_item_code, " +
			"           b.status AS bill_status, " +
			"           pr.visit_type AS patient_visit_type, " +
			"           CASE " +
			"               WHEN (pr.op_type != 'O') THEN doc.doctor_license_number " +
			"               ELSE ref.doctor_license_number " +
			"           END AS doctor_license_number, " +
			"           CASE " +
			"               WHEN (pr.op_type != 'O') THEN doc.doctor_name " +
			"               ELSE ref.referal_name " +
			"           END AS doctor_name, " +
			"           CASE " +
			"               WHEN (pr.op_type != 'O') THEN doc.doctor_id " +
			"               ELSE ref.referal_no " +
			"           END AS doctor_id, " +
			"           CASE " +
			"               WHEN (pr.op_type != 'O') THEN 'Doctor' " +
			"               ELSE ref.doctor_type " +
			"           END AS doctor_type, " +
			"           min(drs.doctor_license_number) AS conducting_dr_license_number, " +
			"           m.issue_base_unit, " +
			"           min(presc_doc.doctor_license_number) AS prescribing_doctor_license_number, " +
			"           CASE " +
			"               WHEN bc.charge_group='DRG' THEN 'Y' " +
			"               ELSE 'N' " +
			"           END AS is_drg_group, " +
			"           CASE " +
			"               WHEN bc.charge_head='MARDRG' THEN 'Y' " +
			"               ELSE 'N' " +
			"           END AS is_drg_charge, " +
			"           CASE " +
			"               WHEN bc.charge_group ='PDM' THEN 'Y' " +
			"               ELSE 'N' " +
			"           END AS is_perdiem_code, " +
			"           ref.doctor_license_number AS ref_doctor_license_number, " +
			"           is_home_care_code, " +
			"           pr.dept_name " +
			"FROM store_sales_details s " +
			"LEFT JOIN store_sales_tax_details sstd ON (sstd.sale_item_id = s.sale_item_id) " +
			"JOIN store_sales_main sm ON (s.sale_id = sm.sale_id) " +
			"JOIN store_item_details m ON s.medicine_id = m.medicine_id " +
			"JOIN bill_charge bc ON (sm.charge_id= bc.charge_id) " +
			"JOIN chargehead_constants chc ON (bc.charge_head = chc.chargehead_id) " +
			"JOIN chargegroup_constants cgc ON (bc.charge_group = cgc.chargegroup_id) " +
			"JOIN bill b ON (b.bill_no = bc.bill_no) " +
			"JOIN patient_registration pr ON (pr.patient_id = b.visit_id) " +
			"LEFT JOIN doctors doc ON (doc.doctor_id = pr.doctor) " +
			"LEFT JOIN doctors presc_doc ON (presc_doc.doctor_id = bc.prescribing_dr_id) " +
			"LEFT JOIN " +
			"  (SELECT 'Referal' AS doctor_type, " +
			"          referal_no, " +
			"          referal_name, " +
			"          clinician_id AS doctor_license_number " +
			"   FROM referral " +
			"   UNION ALL SELECT 'Doctor' AS doctor_type, " +
			"                    doctor_id, " +
			"                    doctor_name, " +
			"                    doctor_license_number " +
			"   FROM doctors) AS REF ON (ref.referal_no = pr.reference_docto_id) " +
			"LEFT JOIN department dpt ON(dpt.dept_id = pr.dept_name) " +
			"LEFT JOIN mrd_supported_code_types msct ON (msct.code_type = s.code_type) " +
			"LEFT JOIN doctors drs ON (drs.doctor_id = bc.payee_doctor_id) " +
			"LEFT JOIN per_diem_codes_master pcm ON (bc.act_rate_plan_item_code = pcm.per_diem_code) " +
			"WHERE (b.status != 'X' " +
			"       AND (b.status = 'F' " +
			"            OR b.status = 'C')) " +
			"  AND bc.status!='X' " +
			"  AND (s.quantity + s.return_qty) > 0 " +
			"  AND sm.bill_no = ? " +
			"GROUP BY s.item_code, " +
			"   	  bc.charge_group," +
			"     s.sale_item_id, " +
			"   	  bc.act_description," +
			"   	  bc.act_description_id," +
			"   	  bc.charge_head," +
			"   	  sm.charge_id," + 	
			"     sale_date::date, " +
			"     dpt.is_referral_doc_as_ordering_clinician, " +   
			"         msct.haad_code, " +
			"         msct.code_type, " +
			"         chc.codification_supported, " +
			"         b.status, " +
			"         pr.visit_type, " +
			"         pr.op_type, " +
			"         doc.doctor_license_number, " +
			"         ref.doctor_license_number, " +
			"         doc.doctor_name, " +
			"         ref.referal_name, " +
			"         doc.doctor_id, " +
			"         ref.referal_no, " +
			"         ref.doctor_type, " +
			"         bc.act_item_code, " +
			"         m.issue_base_unit, " +
			"         ref_doctor_license_number, " +
			"         is_home_care_code, " +
			"		  presc_doc.doctor_name, " +
			"		  presc_doc.doctor_id, " +
			"         CASE " +
			"             WHEN bc.charge_group='DRG' THEN 'Y' " +
			"             ELSE 'N' " +
			"         END, " +
			"         CASE " +
			"             WHEN bc.charge_head='MARDRG' THEN 'Y' " +
			"             ELSE 'N' " +
			"         END, " +
			"         CASE " +
			"             WHEN bc.charge_group='PDM' THEN 'Y' " +
			"             ELSE 'N' " +
			"         END, " +
			"         pr.dept_name " +
			"UNION ALL " +
			"SELECT  " +
      " CASE WHEN bcc.submission_batch_type='P' THEN FOO.package_vat " +
      " ELSE sum((coalesce(bcc.tax_amt, 0.00))) END AS vat_net, " +
			"       COALESCE(dpt.is_referral_doc_as_ordering_clinician,'N') " +
      "          AS is_referral_doc_as_ordering_clinician, " +
			"       presc_doc.doctor_name as prescribing_doctor_name, " +
			"	    presc_doc.doctor_id AS prescribing_doctor_id, " +
			"       sum(bcc.act_quantity) AS qty, " +
			"		bcc.charge_group AS charge_group," +
			"		bcc.act_description AS act_description," +
			"		bcc.act_description_id AS act_description_id," +
			"		bcc.charge_head AS charge_group," +
			"		bcc.charge_id AS charge_id," +
			"       sum(0.0) AS net, " +
			"       sum(bcc.return_amt) AS return_amt, " +
			"       sum(bcc.return_qty) AS return_qty, " +
      " CASE WHEN bcc.submission_batch_type='P' THEN FOO.total_package_amount " +
      " ELSE (bcc.amount + bcc.return_amt) END AS amount, " +
			"       sum(bcc.act_rate) AS rate, " +
			"       sum(bcc.act_quantity + bcc.return_qty) AS quantity, " +
			"       sum(bcc.discount) AS discount, " +
			"       max(bct.tax_rate) AS vat_tax_rate, " +
			"       (act_rate_plan_item_code) AS item_code, " +
			"       'Hospital' AS activity_group, " +
			"       max(posted_date) AS item_posted_date, " +
			"       to_char(max(posted_date), 'dd/MM/yyyy hh24:mi') AS posted_date, " +
			"       array_to_string(array_agg(bcc.charge_id), ',') AS charge_id_list, " +
			"       msct.haad_code AS act_type, " +
			"       msct.code_type AS act_type_desc, " +
			"       chc.codification_supported, " +
			"       '' AS act_item_code, " +
			"       b.status AS bill_status, " +
			"       pr.visit_type AS patient_visit_type, " +
			"       CASE " +
			"           WHEN (pr.op_type != 'O') THEN doc.doctor_license_number " +
			"           ELSE ref.doctor_license_number " +
			"       END AS doctor_license_number, " +
			"       CASE " +
			"           WHEN (pr.op_type != 'O') THEN doc.doctor_name " +
			"           ELSE ref.referal_name " +
			"       END AS doctor_name, " +
			"       CASE " +
			"           WHEN (pr.op_type != 'O') THEN doc.doctor_id " +
			"           ELSE ref.referal_no " +
			"       END AS doctor_id, " +
			"       CASE " +
			"           WHEN (pr.op_type != 'O') THEN 'Doctor' " +
			"           ELSE ref.doctor_type " +
			"       END AS doctor_type, " +
			"       min(drs.doctor_license_number) AS conducting_dr_license_number, " +
			"       1 AS issue_base_unit, " +
			"       min(presc_doc.doctor_license_number) AS prescribing_doctor_license_number, " +
			"       CASE " +
			"           WHEN bcc.charge_group='DRG' THEN 'Y' " +
			"           ELSE 'N' " +
			"       END AS is_drg_group, " +
			"       CASE " +
			"           WHEN bcc.charge_head='MARDRG' THEN 'Y' " +
			"           ELSE 'N' " +
			"       END AS is_drg_charge, " +
			"       CASE " +
			"           WHEN bcc.charge_group ='PDM' THEN 'Y' " +
			"           ELSE 'N' " +
			"       END AS is_perdiem_code, " +
			"       ref.doctor_license_number AS ref_doctor_license_number, " +
			"       is_home_care_code, " +
			"       pr.dept_name " +
			"FROM bill_charge bcc " +
			"LEFT JOIN bill_charge_tax bct ON (bcc.charge_id = bct.charge_id) " +
			"JOIN chargehead_constants chc ON (bcc.charge_head = chc.chargehead_id) " +
			"JOIN chargegroup_constants cgc ON (bcc.charge_group = cgc.chargegroup_id) " +
			"JOIN bill b ON (b.bill_no = bcc.bill_no) " +
			"JOIN patient_registration pr ON (pr.patient_id = b.visit_id) " +
			"LEFT JOIN LATERAL (SELECT sum(bci.amount+bci.return_amt) as total_package_amount, " +
			    " sum((coalesce(bci.tax_amt, 0.00))) AS package_vat, bci.package_id, " +
			    " bci.bill_no " +
			    "FROM bill_charge bci " +
			    "WHERE (bci.submission_batch_type='P' AND bci.charge_head<>'PKGPKG') " +
			    "GROUP BY bci.bill_no, bci.package_id) AS FOO " +
      "ON (FOO.package_id=bcc.package_id AND FOO.bill_no = bcc.bill_no) " +
			"LEFT JOIN doctors doc ON (doc.doctor_id = pr.doctor) " +
			"LEFT JOIN doctors presc_doc ON (presc_doc.doctor_id = bcc.prescribing_dr_id) " +
			"LEFT JOIN " +
			"  (SELECT 'Referal' AS doctor_type, " +
			"          referal_no, " +
			"          referal_name, " +
			"          clinician_id AS doctor_license_number " +
			"   FROM referral " +
			"   UNION ALL SELECT 'Doctor' AS doctor_type, " +
			"                    doctor_id, " +
			"                    doctor_name, " +
			"                    doctor_license_number " +
			"   FROM doctors) AS REF ON (ref.referal_no = pr.reference_docto_id) " +
			"LEFT JOIN mrd_supported_code_types msct ON (msct.code_type = bcc.code_type) " +
			"LEFT JOIN doctors drs ON (drs.doctor_id = bcc.payee_doctor_id) " +
			"LEFT JOIN department dpt ON(dpt.dept_id = pr.dept_name) " +
			"LEFT JOIN per_diem_codes_master pcm ON (bcc.act_rate_plan_item_code = pcm.per_diem_code) " +
			"WHERE (b.status != 'X') " +
			"  AND bcc.charge_head NOT IN ('PHMED', " +
			"                              'PHCMED', " +
			"                              'PHRET', " +
			"                              'PHCRET', " +
			"                              'ADJDRG') " +
			"  AND (bcc.act_quantity + bcc.return_qty) > 0 " +
			"  AND (bcc.submission_batch_type IS NULL " +
			" OR (  bcc.submission_batch_type = 'P' AND bcc.charge_head='PKGPKG' ) " +
			" OR ( bcc.submission_batch_type = 'I' AND bcc.charge_head!='PKGPKG')) " +
			"  AND bcc.bill_no = ? " +
			"  AND bcc.status != 'X' " +
			"GROUP BY  " +
			"         act_rate_plan_item_code, " +
			"		  bcc.charge_group," +
			"   	  bcc.act_description," +
			"   	  bcc.act_description_id," +
			"		  bcc.charge_head," +
			"		  bcc.charge_id," +			
			"         posted_date::date, " +
			"         msct.haad_code, " +
			"         msct.code_type, " +
			"         chc.codification_supported, " +
			"         dpt.is_referral_doc_as_ordering_clinician, " +
			"         b.status, " +
			"         pr.visit_type, " +
			"         pr.op_type, " +
			"         doc.doctor_license_number, " +
			"         ref.doctor_license_number, " +
			"         doc.doctor_name, " +
			"         ref.referal_name, " +
			"         doc.doctor_id, " +
			"         ref.referal_no, " +
			"         ref.doctor_type, " +
			"         issue_base_unit, " +
			"		  presc_doc.doctor_name, " +
			"		  presc_doc.doctor_id, " +
			"         CASE " +
			"             WHEN bcc.charge_group='DRG' THEN 'Y' " +
			"             ELSE 'N' " +
			"         END, " +
			"         CASE " +
			"             WHEN bcc.charge_head='MARDRG' THEN 'Y' " +
			"             ELSE 'N' " +
			"         END, " +
			"         ref_doctor_license_number, " +
			"         is_home_care_code, " +
			"         CASE " +
			"             WHEN bcc.charge_group='PDM' THEN 'Y' " +
			"             ELSE 'N' " +
			"         END, " +
			"         pr.dept_name, FOO.package_vat, FOO.total_package_amount";
	
	/** The Constant ORDER_BY_CHARGES. */
	public static final String ORDER_BY_CHARGES = " ORDER BY charge_id, main_visit_id, claim_id" ;
	
	/**
	 * Find all charges.
	 *
	 * @param billNo the bill no
	 * @param genPrefs the gen prefs
	 * @return the list
	 */
	public List<BasicDynaBean> findAllCharges(String billNo, BasicDynaBean genPrefs) {

		return DatabaseHelper.queryToDynaList(GET_ALL_CHARGES_HOSPITAL_PHARMACY, billNo, billNo);
	}
	
  /**
   * Find all by key.
   *
   * @param map the map
   * @return the list
   */
  public List<BasicDynaBean> findAllByKey(Map map) {
    return findByCriteria(map);
  }
  
  private static final String FILTER_MEDICINE_SALE_ITEM_ID = "SELECT ssd.sale_item_id"
      + " FROM store_sales_details ssd"
      + " JOIN store_item_details sid ON (ssd.medicine_id = sid.medicine_id)"
      + " JOIN store_category_master scm ON (sid.med_category_id = scm.category_id)"
      + " WHERE scm.is_drug='Y' AND ssd.sale_id = ?";
  
  /**
   * Gets store item id which are marked as drug category.
   * 
   * @param saleId the saleid
   * @return list
   */
  public List<BasicDynaBean> filterMedicineSaleItemId(String saleId) {
    return DatabaseHelper.queryToDynaList(FILTER_MEDICINE_SALE_ITEM_ID,saleId);
  }
}
