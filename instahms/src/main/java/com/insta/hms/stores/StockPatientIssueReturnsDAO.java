package com.insta.hms.stores;

import com.bob.hms.common.DataBaseUtil;
import org.apache.commons.beanutils.BasicDynaBean;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class StockPatientIssueReturnsDAO {

  public static final String ISSUED_ITEMS = "SELECT distinct sibd.batch_no,i.medicine_name,i.medicine_id,sum(u.qty) as qty,sum(u.return_qty) as return_qty,"
      + "      sum(u.qty-u.return_qty) AS returnqty, sibd.exp_dt, min(bc.act_rate) as mrp, "
      + "      sum(u.amount) as amount, min(COALESCE(bc.act_rate,ROUND(u.amount/u.qty, 2),0)) as unit_rate,u.item_batch_id, "
      + "      c.identification,c.billable,(CASE WHEN c.issue_type='P' THEN 'Permanent' WHEN c.issue_type='L'  "
      + "      THEN 'Reusable' ELSE 'Consumable' END) AS issue_type, i.issue_base_unit as issue_units, "
      + "      min(COALESCE(ROUND(sibd.mrp/i.issue_base_unit, 2),0)) as unit_mrp, sum(COALESCE(bc.amount,0)) as  pkg_mrp, "
      + "      i.package_uom, i.issue_units as issue_uom, "
      + "      0 AS discount,issued_to,ui.dept_from as dept_id,"
      + "      i.item_barcode_id ,i.issue_base_unit,bc.insurance_category_id,i.control_type_id,sict.control_type_name,i.cust_item_code, "
      + " CASE WHEN i.cust_item_code IS NOT NULL AND  TRIM(i.cust_item_code) != ''  THEN i.medicine_name||' - '||i.cust_item_code ELSE i.medicine_name END as cust_item_code_with_name "
      + " FROM stock_issue_main ui "
      + " JOIN stock_issue_details u on (ui.user_issue_no = u.user_issue_no and ui.package_id is null)"
      + " JOIN store_item_batch_details sibd USING(item_batch_id)"
      + " JOIN store_item_details i ON(i.medicine_id = u.medicine_id)  "
      + " LEFT JOIN store_item_issue_rates siir ON (siir.medicine_id = sibd.medicine_id) "
      + " LEFT JOIN bill_activity_charge bac ON (u.item_issue_no::varchar = bac.activity_id "
      + " AND activity_code = 'PHI' AND payment_charge_head = 'INVITE') "
      + " LEFT JOIN bill_charge bc  ON (bc.charge_id = bac.charge_id)"
      + " LEFT JOIN patient_package_content_consumed ppcc ON (ppcc.bill_charge_id = bc.charge_id) "
      + " LEFT JOIN patient_package_contents ppc ON (ppc.patient_package_content_id = ppcc.patient_package_content_id) "
      + " LEFT JOIN store_item_controltype sict ON (sict.control_type_id = i.control_type_id) "
      + " JOIN store_category_master c ON c.category_id=i.med_category_id ";

  public static final String ISSUED_ITEMS_GROUP_BY = " GROUP BY i.medicine_name,i.medicine_id,sibd.exp_dt,"
      + "          c.identification,c.billable,c.issue_type,issued_to,i.item_barcode_id,"
      + "          sibd.batch_no,i.package_uom,i.issue_units,"
      + "          ui.dept_from,i.issue_base_unit,u.item_batch_id,bc.insurance_category_id,"
      + "          sict.control_type_name,i.control_type_id,i.cust_item_code,bc.charge_id ";

  public static final String ISSUED_ITEMS_WITH_PAT_AMTS = "SELECT sibd.batch_no, i.medicine_name,i.medicine_id,sum(u.qty) as qty,sum(u.return_qty) as return_qty, sum(u.qty-u.return_qty) AS returnqty,"
      + "       sibd.exp_dt, min( bc.act_rate) as mrp,sum(u.amount), min(COALESCE(bc.act_rate,ROUND(u.amount/u.qty, 2),0)) as unit_rate,u.item_batch_id,"
      + "       c.identification,c.billable,(CASE WHEN c.issue_type='P' THEN 'Permanent' WHEN c.issue_type='L'  "
      + "       THEN 'Reusable' ELSE 'Consumable' END) AS issue_type,  i.issue_units, "
      + "       min(COALESCE(ROUND(sibd.mrp/i.issue_base_unit, 2),0)) as unit_mrp, sum(COALESCE(bc.amount,0)) as pkg_mrp,"
      + "       sum(CASE WHEN iic.insurance_payable = 'Y' THEN ipd.patient_amount ELSE 0 END) AS patient_amount,"
      + "       sum(CASE WHEN iic.insurance_payable = 'Y' THEN ipd.patient_amount_per_category ELSE 0 END) AS patient_amount_per_category,  "
      + "       min(CASE WHEN iic.insurance_payable = 'Y' THEN ipd.patient_percent ELSE 100 END) AS patient_percent, "
      + "       sum(u.insurance_claim_amt) as insurance_claim_amt , i.package_uom, i.issue_units as issue_uom, "
      + "       u.item_unit, 0 AS discount,issued_to,ui.dept_from as dept_id,i.item_barcode_id,"
      + "		ipd.patient_amount_cap,i.issue_base_unit,bcc.insurance_category_id,i.control_type_id,sict.control_type_name,i.cust_item_code,  "
      + " CASE WHEN i.cust_item_code IS NOT NULL AND  TRIM(i.cust_item_code) != ''  THEN i.medicine_name||' - '||i.cust_item_code ELSE i.medicine_name END as cust_item_code_with_name "
      + " FROM stock_issue_main ui  JOIN stock_issue_details u USING(user_issue_no) "
      + " JOIN store_item_batch_details sibd USING(item_batch_id)"
      + " JOIN  store_item_details i ON(i.medicine_id = u.medicine_id)  "
      + " LEFT JOIN store_item_issue_rates siir ON (siir.medicine_id = sibd.medicine_id) "
      + " LEFT  JOIN bill_activity_charge bac ON (u.item_issue_no::varchar = bac.activity_id "
      + " AND activity_code = 'PHI' AND payment_charge_head = 'INVITE') "
      + " LEFT JOIN bill_charge bc  On bc.charge_id = bac.charge_id"
      + " LEFT JOIN bill_claim bcl ON (bcl.bill_no = bc.bill_no AND bcl.plan_id = ?) "
      + " LEFT JOIN bill_charge_claim bcc ON (bcc.charge_id = bc.charge_id AND bcc.sponsor_id = bcl.sponsor_id) "
      + " JOIN store_category_master c ON c.category_id=i.med_category_id   "
      + " LEFT JOIN patient_registration pr on (patient_id = issued_to) "
      + " LEFT JOIN item_insurance_categories iic ON (iic.insurance_category_id = bcc.insurance_category_id) "
      + " LEFT JOIN store_item_controltype sict ON (sict.control_type_id = i.control_type_id) "
      + " LEFT OUTER JOIN patient_insurance_plan_details ipd on (ipd.insurance_category_id = "
      + " bcc.insurance_category_id and ipd.patient_type = pr.visit_type AND ipd.visit_id = ?) ";

  public static final String ISSUED_ITEMS_WITH_PAT_AMTS_GROUP_BY = " GROUP BY i.medicine_name,i.medicine_id,sibd.exp_dt,"
      + "          c.identification,c.billable,c.issue_type "
      + "          ,issued_to,insurance_payable,i.item_barcode_id,"
      + "           sibd.batch_no,i.package_uom,i.issue_units,"
      + "          u.item_unit,issued_to,ui.dept_from,patient_amount_cap,issue_base_unit, "
      + "		u.item_batch_id,bcc.insurance_category_id,sict.control_type_name,i.control_type_id,i.cust_item_code,bc.charge_id ";

  public List<BasicDynaBean> getVisitIssuedItemsDetails(String patientId, int planId)
      throws SQLException, IOException {

    String query = ISSUED_ITEMS + " where issued_to = ? AND  u.qty != u.return_qty " + ISSUED_ITEMS_GROUP_BY;
    List<Object> values = new ArrayList<>();
    if (planId == 0) {
      values.add(patientId);
    } else {
      query = ISSUED_ITEMS_WITH_PAT_AMTS
          + " where issued_to = ? AND  u.qty != u.return_qty and (ipd.plan_id = ? OR ipd.plan_id IS NULL) "
          + ISSUED_ITEMS_WITH_PAT_AMTS_GROUP_BY;
      values.add(planId);
      values.add(patientId);
      values.add(patientId);
      values.add(planId);
    }
    try (Connection con = DataBaseUtil.getConnection();
        PreparedStatement ps = con.prepareStatement(query)) {
      int index = 1;
      for (Object value : values) {
        ps.setObject(index++, value);
      }
      return DataBaseUtil.queryToDynaList(ps);
    }
	}
	
	public static final String GET_VISIT_ISSUED_ITEMS_TAX_DETAILS = "select bc.package_id, sid.item_batch_id,sid.medicine_id,MAX(bct.tax_rate) as tax_rate, "+
	    " min(COALESCE(ROUND(bct.tax_amount/sid.qty, 2),0)) as unit_tax,"
	    + "min(COALESCE(ROUND(bct.original_tax_amt/sid.qty, 2),0)) as unit_original_tax, bct.tax_sub_group_id " +
	    " FROM stock_issue_main sim " +
			" JOIN stock_issue_details sid ON (sim.user_issue_no = sid.user_issue_no) "+
		    " JOIN bill_activity_charge bac ON (sid.item_issue_no::varchar = bac.activity_id " +
				" AND activity_code = 'PHI' AND payment_charge_head = 'INVITE') " +
			" JOIN bill_charge_tax bct ON (bct.charge_id = bac.charge_id)"
        + " JOIN bill_charge bc ON  (bct.charge_id = bc.charge_id)"
        + " where sim.issued_to = ? " +
			" GROUP BY bc.package_id, sid.medicine_id,sid.item_batch_id,bct.tax_sub_group_id ";
	
	public List<BasicDynaBean> getVisitIssuedItemsTaxDetails(String patientId) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		try{
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_VISIT_ISSUED_ITEMS_TAX_DETAILS);
			ps.setString(1, patientId);
			return DataBaseUtil.queryToDynaList(ps);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}

	}

	private static final String VISIT_ISSUES_OF_A_MEDICINE =
		" SELECT * FROM stock_issue_main JOIN stock_issue_details USING(user_issue_no)" +
		" WHERE issued_to = ? AND medicine_id = ? AND return_qty < qty AND item_batch_id = ? AND dept_from = ? ";

	public List<BasicDynaBean> getVisitItemIssues(Connection con, String visitId, int medicineId,
	    int itemBatchId, int storeId, Integer pkgId) throws SQLException {
	    PreparedStatement ps = null;
	    try {
    	    StringBuilder query = new StringBuilder(VISIT_ISSUES_OF_A_MEDICINE);
    	    if(pkgId == 0) {
    	      query.append(" AND package_id IS NULL ");
    	      query.append(" ORDER BY user_issue_no DESC");
    	      ps = con.prepareStatement(query.toString());
    	      ps.setString(1, visitId);
    	      ps.setInt(2, medicineId);
    	      ps.setInt(3, itemBatchId);
    	      ps.setInt(4, storeId);
    	    } else {
    	      query.append(" AND package_id = ? ");
    	      query.append(" ORDER BY user_issue_no DESC");
    	      ps = con.prepareStatement(query.toString());
    	      ps.setString(1, visitId);
    	      ps.setInt(2, medicineId);
    	      ps.setInt(3, itemBatchId);
    	      ps.setInt(4, storeId);
    	      ps.setInt(5, pkgId);
    	    }
    	    return DataBaseUtil.queryToDynaList(ps);
	    } finally {
	      DataBaseUtil.closeConnections(null, ps);
	    }
	  }


  private static final String VISIT_ISSUES_OF_A_MEDICINE_FOR_RETURN = " SELECT  bc.charge_id, bc.act_rate, bc.act_quantity, sid.qty,"
      + " (SELECT COALESCE(SUM(tax_amount), 0) from bill_charge_tax bct WHERE bct.charge_id = bc.charge_id) tax_amt, "
      + "  (SELECT COALESCE(SUM(original_tax_amt), 0) from bill_charge_tax bct WHERE bct.charge_id = bc.charge_id) original_tax_amt, "
      + " sid.medicine_id, sid.item_batch_id, sid.return_qty, sid.item_issue_no "
      + " FROM stock_issue_main sim JOIN stock_issue_details sid USING(user_issue_no) "
      + "JOIN bill_activity_charge bac ON (item_issue_no = activity_id::integer AND activity_code = 'PHI') "
      + "JOIN bill_charge bc ON (bc.charge_id = bac.charge_id) "
      + " WHERE issued_to = ? AND medicine_id = ? AND sid.return_qty < qty AND item_batch_id = ? AND dept_from = ? "
      + "AND substring(bc.charge_id from 3)::integer < ?" + " ORDER BY user_issue_no DESC";

  public List<BasicDynaBean> getVisitItemIssues(Connection con, String visitId, Integer medicineId,
      int itemBatchId, int storeId, String returnChargeId) throws SQLException {
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(VISIT_ISSUES_OF_A_MEDICINE_FOR_RETURN);
      ps.setString(1, visitId);
      ps.setInt(2, medicineId);
      ps.setInt(3, itemBatchId);
      ps.setInt(4, storeId);
      ps.setInt(5, Integer.parseInt(returnChargeId.substring(2)));

      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(null, ps);
    }
  }

  private String visit_issued_items_claim_details = " SELECT dept_from,item_batch_id,sum(bcc.insurance_claim_amt) as insurance_claim_amt "
      + " FROM stock_issue_main sim " + " JOIN stock_issue_details sid USING(user_issue_no)  "
      + " LEFT  JOIN bill_activity_charge bac ON (sid.item_issue_no::varchar = bac.activity_id "
      + " AND activity_code = 'PHI' AND payment_charge_head = 'INVITE') "
      + " LEFT JOIN bill_charge bc  On bc.charge_id = bac.charge_id"
      + " JOIN bill_charge_claim bcc ON(bcc.charge_id = bc.charge_id) "
      + "WHERE sim.issued_to = ?  " + " GROUP BY item_batch_id,dept_from,claim_id";

  /**
   * Gives visit id and store id this method list out item claim amount for no of plans of the visit
   * sales.
   * 
   * @param visitId
   * @param storeId
   * @return
   * @throws SQLException
   */
  public List<BasicDynaBean> getVisitIssuesClaimDetails(String visitId) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(visit_issued_items_claim_details);
      ps.setString(1, visitId);

      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }
  private String RETRUN_INDENT_ITEMS_DETAILS = "SELECT * from store_patient_indent_details spd "
      + "JOIN store_patient_indent_main spim USING (patient_indent_no) "
      + "WHERE patient_indent_no =? and indent_type ='R'";
  /**
   *
   * Return the return indent items details
   * 
   * @param patientIndentNo
   * @return
   * @throws SQLException
   * @throws IOException
   */
  public List<BasicDynaBean> getReturnIndentItemsDetails(String patientIndentNo)
      throws SQLException, IOException {

    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(RETRUN_INDENT_ITEMS_DETAILS);
      ps.setString(1, patientIndentNo);

      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  private static final String GET_PATIENT_ISSUE_RETURNS_DETAILS = " select distinct sibd.batch_no,s.dept_name as to_store,"
      + "sitd.medicine_name,sitd.cust_item_code," + "sirm.dept_to, pd.mr_no, "
      + "to_char(sirm.date_time, 'DD-MM-YYYY HH:MI AM')as date," + "sirm.username,"
      + "case when sir.item_unit = 'I' then sir.qty else round(sir.qty/rtn_pkg_size,2) end as qty,"
      + "sirm.reference," + "sirm.user_return_no, sibd.exp_dt, " + "sirm.returned_by ,"
      + "get_patient_full_name(sm.salutation,pd.patient_name,pd.middle_name, pd.last_name) AS patient_name,"
      + "coalesce(bc.amount,0) + coalesce(bct.tax_amount , 0) as amount,"
      + "case when issue_type='P' then 'Permanent' when issue_type='C' then 'Consumable'  when issue_type='R' then 'Retailable' else 'Reusable' end as issue_type,"
      + "case when consignment_stock='t' then 'Consignment' else 'Normal' end as stocktype, "
      + "scm.category as item_category, "
      + "coalesce(wn.ward_name,wnr.ward_name) as ward_name,coalesce(wn.ward_no,wn.ward_no) as ward_no,bn.bed_name, "
      + "s.pharmacy_tin_no, hcms.tin_number, ptm.tin_number as pri_tpa_tin_number, "
      + "stm.tin_number as sec_tpa_tin_number, picm.tin_number as pri_insur_tin_number,"
      + "sicm.tin_number as sec_insur_tin_number, s.pharmacy_drug_license_no, sic.control_type_name,"
      + "ptm.tpa_name as pri_tpa_name, stm.tpa_name as sec_tpa_name, "
      + "picm.insurance_co_name as pri_insurance_co_name, sicm.insurance_co_name as sec_insurance_co_name, "
      + "pipm.plan_name as pri_plan_name, sipm.plan_name as sec_plan_name, "
      + "d.doctor_name, hcms.center_address, bc.code_type as drug_code "
      + " from store_issue_returns_main sirm"
      + " join store_issue_returns_details sir using(user_return_no)"
      + " JOIN store_item_batch_details sibd USING(item_batch_id)"
      + " LEFT JOIN bill_activity_charge bac  ON sir.item_return_no::varchar = bac.activity_id AND activity_code = 'PHI' AND payment_charge_head = 'INVRET'"
      + " LEFT JOIN bill_charge bc  On bc.charge_id = bac.charge_id"
      + " LEFT JOIN bill_charge_tax bct  On bc.charge_id = bct.charge_id"
      + " left join patient_registration pr on pr.patient_id=sirm.returned_by"
      + " LEFT JOIN ward_names wnr ON wnr.ward_no = pr.ward_id "
      + " left join patient_details pd on pd.mr_no=pr.mr_no"
      + " join store_item_details  sitd on(sitd.medicine_id = sir.medicine_id) "
      + " join store_category_master scm ON (scm.category_id = sitd.med_category_id)"
      + " join stores s on(s.dept_id = sirm.dept_to)"
      + " join store_stock_details ssd on (ssd.dept_id=dept_to and ssd.medicine_id=sir.medicine_id and sir.batch_no=ssd.batch_no)"
      + " left join salutation_master sm ON (sm.salutation_id = pd.salutation) "
      + " LEFT JOIN admission ad ON ad.patient_id = pr.patient_id "
      + " LEFT JOIN bed_names bn ON bn.bed_id = ad.bed_id " 
      + " LEFT JOIN ward_names wn ON wn.ward_no = bn.ward_no "
      + " LEFT JOIN bill_claim pbcl ON (bc.bill_no = pbcl.bill_no AND pbcl.priority = 1) "
      + " LEFT JOIN bill_claim sbcl ON (bc.bill_no = sbcl.bill_no AND sbcl.priority = 2) "
      + " LEFT JOIN bill_charge_claim pbccl ON (bc.charge_id = pbccl.charge_id AND pbcl.claim_id = pbccl.claim_id ) "
      + " LEFT JOIN bill_charge_claim sbccl ON (bc.charge_id = sbccl.charge_id AND sbcl.claim_id = sbccl.claim_id) "
      + "LEFT JOIN hospital_center_master hcms ON (hcms.center_id=s.center_id) "
      + "LEFT JOIN tpa_master ptm ON (ptm.tpa_id = pr.primary_sponsor_id) "
      + "LEFT JOIN tpa_master stm ON (stm.tpa_id = pr.secondary_sponsor_id) "
      + "left join bill b on (bc.bill_no = b.bill_no)"
      + "LEFT JOIN patient_insurance_plans ppip ON (ppip.patient_id = b.visit_id AND ppip.priority = 1) "
      + "LEFT JOIN patient_insurance_plans spip ON (spip.patient_id = b.visit_id AND spip.priority = 2) "
      + "LEFT JOIN insurance_plan_main pipm ON (pipm.plan_id = ppip.plan_id) "
      + "LEFT JOIN insurance_plan_main sipm ON (sipm.plan_id = spip.plan_id) "
      + "LEFT JOIN insurance_company_master picm ON (picm.insurance_co_id = ppip.insurance_co AND ppip.priority = 1) "
      + "LEFT JOIN insurance_company_master sicm ON (sicm.insurance_co_id = spip.insurance_co AND spip.priority = 2) "
      + "LEFT JOIN store_item_controltype sic ON (sic.control_type_id = sitd.control_type_id) "
      + "LEFT JOIN doctors d ON ( pr.doctor = d.doctor_id) "
      + " where sirm.user_return_no=? AND ( patient_confidentiality_check(pd.patient_group,pd.mr_no) )";

  public static List<BasicDynaBean> getPatientIssueReturnInfo(String returnNo) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_PATIENT_ISSUE_RETURNS_DETAILS);
      ps.setInt(1, Integer.parseInt(returnNo));
      // ps.setInt(1, 131);
      // System.out.println(" Query is "+ps.toString());
      List<BasicDynaBean> list = DataBaseUtil.queryToDynaList(ps);
      if (list.size() > 0)
        return list;
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return null;
  }

  private static final String RESET_RETURN_QUANTITY = "UPDATE stock_issue_details set return_qty = 0 "
      + "WHERE item_issue_no = " + "(SELECT bac.activity_id::integer from bill_activity_charge bac "
      + "left join bill_charge bc ON(bc.charge_id = bac.charge_id) "
      + "where bc.charge_head = 'INVITE' AND bc.charge_id = ?)";

  public boolean resetReturnQuantity(Connection con, String chargeId) throws SQLException {
    Boolean result = false;
    try (PreparedStatement ps = con.prepareStatement(RESET_RETURN_QUANTITY)) {
      ps.setString(1, chargeId);
      result = ps.executeUpdate() > 0;
    }
    return result;
  }

  public static final String PACKAGE_ISSUED_ITEMS = "SELECT distinct sibd.batch_no,i.medicine_name,i.medicine_id,sum(u.qty) as qty,sum(u.return_qty) as return_qty,"
	      + "      sum(u.qty-u.return_qty) AS returnqty, sibd.exp_dt, min(bc.act_rate) as mrp, "
	      + "      sum(u.amount) as amount, min(COALESCE(bc.act_rate,ROUND(u.amount/u.qty, 2),0)) as unit_rate,u.item_batch_id, "
	      + "      c.identification,c.billable,(CASE WHEN c.issue_type='P' THEN 'Permanent' WHEN c.issue_type='L'  "
	      + "      THEN 'Reusable' ELSE 'Consumable' END) AS issue_type, i.issue_base_unit as issue_units, "
	      + "      min(COALESCE(ROUND(sibd.mrp/i.issue_base_unit, 2),0)) as unit_mrp, sum(COALESCE(bc.amount,0)) as  pkg_mrp, "
	      + "      i.package_uom, i.issue_units as issue_uom, "
	      + "      0 AS discount,issued_to,ui.dept_from as dept_id,"
	      + "      i.item_barcode_id ,i.issue_base_unit,bc.insurance_category_id,i.control_type_id,sict.control_type_name,i.cust_item_code, "
	      + " CASE WHEN i.cust_item_code IS NOT NULL AND  TRIM(i.cust_item_code) != ''  THEN i.medicine_name||' - '||i.cust_item_code ELSE i.medicine_name END as cust_item_code_with_name "
	      + " FROM stock_issue_main ui "
	      + " JOIN stock_issue_details u on (ui.user_issue_no = u.user_issue_no and ui.package_id is not null)"
	      + " JOIN store_item_batch_details sibd USING(item_batch_id)"
	      + " JOIN store_item_details i ON(i.medicine_id = u.medicine_id)  "
	      + " LEFT JOIN store_item_issue_rates siir ON (siir.medicine_id = sibd.medicine_id) "
	      + " LEFT JOIN bill_activity_charge bac ON (u.item_issue_no::varchar = bac.activity_id "
	      + " AND activity_code = 'PHI' AND payment_charge_head = 'INVITE') "
	      + " LEFT JOIN bill_charge bc  ON (bc.charge_id = bac.charge_id)"
	      + " LEFT JOIN patient_package_content_consumed ppcc ON (ppcc.bill_charge_id = bc.charge_id) "
	      + " LEFT JOIN patient_package_contents ppc ON (ppc.patient_package_content_id = ppcc.patient_package_content_id) "
	      + " LEFT JOIN store_item_controltype sict ON (sict.control_type_id = i.control_type_id) "
	      + " JOIN store_category_master c ON c.category_id=i.med_category_id ";
  
  public  List<BasicDynaBean> getPkgIssuedItemDetails(String visitId, Integer pkgId,
      Integer patPkgId) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;

    try {

      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(PACKAGE_ISSUED_ITEMS + " where issued_to = ? AND "
          + " ui.package_id = ? AND ppc.patient_package_id = ? AND u.qty != u.return_qty "
          + ISSUED_ITEMS_GROUP_BY);
      ps.setString(1, visitId);
      ps.setInt(2, pkgId);
      ps.setInt(3, patPkgId);

      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

}
