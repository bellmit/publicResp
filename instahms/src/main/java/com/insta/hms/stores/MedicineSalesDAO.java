package com.insta.hms.stores;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.billing.BillChargeClaimTaxDAO;
import com.insta.hms.billing.BillChargeTaxDAO;
import com.insta.hms.common.CommonUtils;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.DynaBeanBuilder;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * DAO to manipulate the table store_sales_details. All query methods
 * are static, and don't need a connection to be passed in. All update/insert
 * methods are non-static and must be used only on a DAO object which has been
 * constructed using a connection.
 */
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MedicineSalesDAO {
	static Logger logger = LoggerFactory.getLogger(MedicineSalesDAO.class);

	Connection con = null;
	
	private static GenericDAO organisationDetails = new GenericDAO("organization_details");

	public MedicineSalesDAO(Connection con) {
		this.con = con;
	}

	private static final String SALES_SEQ_PREFS = " SELECT pattern_id FROM hosp_pharmacy_sale_seq_prefs "
			+ " WHERE priority = ( " + "  SELECT min(priority) FROM hosp_pharmacy_sale_seq_prefs "
			+ "  WHERE (bill_type =  ? or bill_type =  '*') " + "    AND (visit_type = ? or visit_type = '*') "
			+ "    AND (sale_type =  ? or sale_type =  '*') " + "    AND (dept_id  =   ? or dept_id  =  '*') " + " ) ";

	public static final String getSalesIDPattern(String billType, String visitType, String saleType, String deptId)
			throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(SALES_SEQ_PREFS);
			ps.setString(1, billType);
			ps.setString(2, visitType);
			ps.setString(3, saleType);
			ps.setString(4, deptId);
			List<BasicDynaBean> l = DataBaseUtil.queryToDynaList(ps);
			BasicDynaBean b = l.get(0);
			return (String) b.get("pattern_id");
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public int getNextSaleItemId() throws SQLException {
		PreparedStatement ps = con.prepareStatement("select nextval('store_sales_details_seq')");
		return DataBaseUtil.getIntValueFromDb(ps);
	}

	public String getNextSaleId(String billType, String visitType, String saleType, String deptId) throws SQLException {
		String pattern = getSalesIDPattern(billType, visitType, saleType, deptId);
		return DataBaseUtil.getNextPatternId(pattern);
		// PreparedStatement ps=con.prepareStatement("select
		// nextval('store_sales_main_seq')");
		// return DataBaseUtil.getIntValueFromDB(ps);
	}

	public int getNextEstimateItemId() throws SQLException {
		PreparedStatement ps = con.prepareStatement("select nextval('store_estimate_details_seq')");
		return DataBaseUtil.getIntValueFromDb(ps);
	}

	public String getNextEstimateId() throws SQLException {
		PreparedStatement ps = con.prepareStatement("select nextval('store_estimate_main_seq')");
		return DataBaseUtil.getStringValueFromDb(ps);
	}

	private static final String INSERT_SALE_ITEM = "INSERT INTO store_sales_details "
			+ " (sale_item_id, sale_id, medicine_id, batch_no, quantity, tax, original_tax_amt,  tax_rate, rate, amount, "
			+ "  package_unit, orig_rate, expiry_date, disc, discount_per, discount_type, basis,pkg_mrp,pkg_cp, "
			+ " insurance_claim_amt, claim_status, claim_recd_total, code_type, item_code, insurance_category_id, "
			+ " sale_unit, prior_auth_id, prior_auth_mode_id, item_batch_id,cost_value ,"
			+ " frequency, dosage, dosage_unit, route_of_admin, doctor_remarks, special_instr, sales_remarks, "
			+ " label_id,duration, duration_units, sponsor_tax_amt, allow_zero_claim, erx_activity_id, billing_group_id, item_excluded_from_doctor, item_excluded_from_doctor_remarks) "
			+ " VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ";


	public boolean insertSaleItems(ArrayList<MedicineSalesDTO> sales) throws SQLException {

		PreparedStatement ps = con.prepareStatement(INSERT_SALE_ITEM);
		if (ps == null)
			return false;

		try {
			Iterator<MedicineSalesDTO> it = sales.iterator();
			while (it.hasNext()) {
				MedicineSalesDTO sale = it.next();
				int i = 1;
				ps.setInt(i++, sale.getSaleItemId());
				ps.setString(i++, sale.getSaleId());
				ps.setInt(i++, Integer.parseInt(sale.getMedicineId()));
				ps.setString(i++, sale.getBatchNo());
				ps.setBigDecimal(i++, sale.getQuantity());
				ps.setBigDecimal(i++, sale.getTax());
				ps.setBigDecimal(i++, sale.getOrgTaxAmt());
				ps.setBigDecimal(i++, sale.getTaxPer());
				ps.setBigDecimal(i++, sale.getRate());
				ps.setBigDecimal(i++, sale.getAmount());
				ps.setBigDecimal(i++, sale.getPackageUnit());
				ps.setBigDecimal(i++, sale.getOrigRate());
				ps.setDate(i++, sale.getExpiryDate());
				ps.setBigDecimal(i++, sale.getMedDiscRS());
				ps.setBigDecimal(i++, sale.getMedDisc());
				ps.setString(i++, sale.getMedDiscType());
				ps.setString(i++, sale.getBasis());
				ps.setBigDecimal(i++, sale.getMrp());
				ps.setBigDecimal(i++, sale.getCp());
				ps.setBigDecimal(i++, sale.getInsuranceClaimAmt());
				ps.setString(i++, sale.getClaimStatus());
				ps.setBigDecimal(i++, sale.getClaimRecdAmt());
				ps.setString(i++, sale.getCodeType());
				ps.setString(i++, sale.getItemCode());
				ps.setInt(i++, sale.getInsuranceCategoryId());
				ps.setString(i++, sale.getSaleUnit());
				ps.setString(i++, sale.getPreAuthId());
				if (sale.getPreAuthModeId() == null)
					ps.setNull(i++, java.sql.Types.INTEGER);
				else
					ps.setInt(i++, sale.getPreAuthModeId());
				ps.setInt(i++, sale.getItemBatchId());
				ps.setBigDecimal(i++, sale.getCostValue());

				ps.setString(i++, sale.getFrequency());
				ps.setString(i++, sale.getDosage());
				ps.setString(i++, sale.getDosage_unit());
				ps.setBigDecimal(i++, sale.getRoute_of_admin());
				ps.setString(i++, sale.getDoctor_remarks());
				ps.setString(i++, sale.getSpecial_instr());
				ps.setString(i++, sale.getSales_remarks());
				if (sale.getWarning_label() == null || sale.getWarning_label().equals(""))
					ps.setNull(i++, java.sql.Types.INTEGER);
				else
					ps.setInt(i++, Integer.parseInt(sale.getWarning_label()));
				ps.setBigDecimal(i++, sale.getDuration());
				ps.setString(i++, sale.getDuration_unit());
				if (sale.getSponsorTaxAmt() == null) {
					ps.setBigDecimal(i++, BigDecimal.ZERO);
				} else {
					ps.setBigDecimal(i++, sale.getSponsorTaxAmt());
				}
				ps.setBoolean(i++, sale.isAllowZeroClaim());

				if (sale.getErxActivityId() != null && (sale.getErxActivityId()).trim().length() > 0) {
					ps.setString(i++, sale.getErxActivityId());
				} else {
					ps.setString(i++, null);
				}
				if (sale.getBillingGroupId() == null || sale.getBillingGroupId().equals("") ) {
				  ps.setNull(i++, java.sql.Types.INTEGER);
				} else {
				  ps.setInt(i++, Integer.parseInt(sale.getBillingGroupId()));
				}

				ps.setBoolean(i++, sale.getItemExcludedFromDoctor());
				ps.setString(i++, sale.getItemExcludedFromDoctorRemarks());
				ps.addBatch();
			}
			int[] updates = ps.executeBatch();
			return DataBaseUtil.checkBatchUpdates(updates);

		} finally {
			ps.close();
		}
	}

	private static final String UPDATE_SALE_ITEM = " UPDATE store_sales_details SET return_insurance_claim_amt = 0, "
			+ " return_amt= return_amt + ?, return_qty= return_qty + ?, "
			+ " insurance_claim_amt = ? WHERE sale_item_id = ?";

	public boolean updateSaleItems(ArrayList<MedicineSalesDTO> salesForReturns) throws SQLException, IOException {
		try (PreparedStatement ps = con.prepareStatement(UPDATE_SALE_ITEM);) {
    
    		Iterator iterator = salesForReturns.iterator();
    		while (iterator.hasNext()) {
    			MedicineSalesDTO sale = (MedicineSalesDTO) iterator.next();
    
    			int i = 1;
    			ps.setBigDecimal(i++, sale.getReturnAmt());
    			ps.setBigDecimal(i++, sale.getReturnQty());
    			ps.setBigDecimal(i++, sale.getInsuranceClaimAmt());
    
    			ps.setInt(i++, sale.getSaleItemId());
    			int k = ps.executeUpdate();
    
    			if (k == 0)
    				return false;
    		}
		}
		return true;
	}

	private static final String SETOFF_AGAINST_SALE = " UPDATE store_sales_details SET return_qty=return_qty-?, return_amt=return_amt-?, "
			+ "  insurance_claim_amt=insurance_claim_amt-?, return_tax_amt=return_tax_amt-?, return_original_tax_amt=return_original_tax_amt-? "
			+ " WHERE sale_item_id=?";

	public void setOffAgainstSaleItem(int saleItemId, BigDecimal returnQty, BigDecimal returnAmt, BigDecimal claimAmt,
			BigDecimal setOffTaxAmt, BigDecimal setOffOriginalTax) throws SQLException {
		DataBaseUtil.executeQuery(con, SETOFF_AGAINST_SALE,
				new Object[] { returnQty, returnAmt, claimAmt, setOffTaxAmt, setOffOriginalTax, saleItemId });
	}

	private static final String UPDATE_SALE_ITEM_RET_INS_AMT = " UPDATE store_sales_details SET return_insurance_claim_amt = return_insurance_claim_amt + ? "
			+ " WHERE sale_item_id = ?";

	public boolean updateSaleItemsReturnInsAmt(ArrayList<MedicineSalesDTO> salesForReturns)
			throws SQLException, IOException {
		try (PreparedStatement ps = con.prepareStatement(UPDATE_SALE_ITEM_RET_INS_AMT);) {
    
    		Iterator iterator = salesForReturns.iterator();
    		while (iterator.hasNext()) {
    			MedicineSalesDTO sale = (MedicineSalesDTO) iterator.next();
    
    			int i = 1;
    			ps.setBigDecimal(i++, sale.getReturnInsuranceClaimAmt());
    
    			ps.setInt(i++, sale.getSaleItemId());
    			int k = ps.executeUpdate();
    
    			if (k == 0)
    				return false;
    		}
		}
		return true;
	}

	private static final String UPDATE_SALE_ITEM_CLAIM = " UPDATE store_sales_details SET insurance_claim_amt = ? "
			+ "  WHERE sale_item_id = ?";

	public boolean updateSaleClaimItems(ArrayList<MedicineSalesDTO> sales) throws SQLException, IOException {
		try (PreparedStatement ps = con.prepareStatement(UPDATE_SALE_ITEM_CLAIM);) {
    
    		Iterator iterator = sales.iterator();
    		while (iterator.hasNext()) {
    			MedicineSalesDTO sale = (MedicineSalesDTO) iterator.next();
    
    			int i = 1;
    			ps.setBigDecimal(i++, sale.getInsuranceClaimAmt());
    			ps.setInt(i++, sale.getSaleItemId());
    			int k = ps.executeUpdate();
    
    			if (k == 0)
    				return false;
    		}
		}
		return true;
	}

	private static final String INSERT_SALE = "INSERT INTO store_sales_main " +
		" (sale_id, sale_date, store_id, type, bill_no, date_time, username, discount, return_bill_no, " +
		"  round_off, discount_per, doctor_name, total_item_amount, total_item_discount, " +
		"  total_item_tax, sale_unit, ward_no, store_rate_plan_id, charge_id, erx_reference_no, " +
	  " is_external_pbm, discount_category_id) " +
		" VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

	public boolean insertSale(MedicineSalesMainDTO sale) throws SQLException {
		PreparedStatement ps = con.prepareStatement(INSERT_SALE);

		try {
			int i = 1;
			ps.setString(i++, sale.getSaleId());
			ps.setTimestamp(i++, sale.getSaleDate());
			ps.setInt(i++, Integer.parseInt(sale.getStoreId()));
			ps.setString(i++, sale.getType());
			ps.setString(i++, sale.getBillNo());
			ps.setTimestamp(i++, sale.getDateTime());
			ps.setString(i++, sale.getUsername());
			ps.setBigDecimal(i++, sale.getDiscount());
			ps.setString(i++, sale.getRBillNo());
			ps.setBigDecimal(i++, sale.getRoundOffPaise());
			if (sale.getDiscountPer() != null)
				ps.setBigDecimal(i++, sale.getDiscountPer());
			else
				ps.setBigDecimal(i++, BigDecimal.ZERO);
			ps.setString(i++, sale.getDoctor());

			ps.setBigDecimal(i++, sale.getTotalItemAmount());
			ps.setBigDecimal(i++, sale.getTotalItemDiscount());
			ps.setBigDecimal(i++, sale.getTotalItemTax());
			ps.setString(i++, sale.getSaleUnit());
			ps.setString(i++, sale.getWardNo());
			if (sale.getStoreRatePlanId() == null) {
				ps.setNull(i++, Types.INTEGER);
			} else {
				ps.setInt(i++, (Integer) sale.getStoreRatePlanId());
			}

			ps.setString(i++, sale.getChargeId());
			if (sale.getErxReferenceNo() != null && (sale.getErxReferenceNo()).trim().length() > 0) {
				ps.setString(i++, sale.getErxReferenceNo());
			} else {
				ps.setString(i++, null);
			}
			ps.setBoolean(i++, sale.getIsExternalPbm());
			if (sale.getDiscountPlan()  == null) {
			  ps.setNull(i++, Types.INTEGER);
			} else {
			  ps.setInt(i++, sale.getDiscountPlan());
			}

			int count = ps.executeUpdate();
			return (count == 1);

		} finally {
			ps.close();
		}
	}

	private static final String INSERT_ESTIMATE = "INSERT INTO store_estimate_main "
			+ " (estimate_id, estimate_date, store_id, visit_id, username, discount, "
			+ " round_off, discount_per, doctor_name) " + " VALUES (?,?,?,?,?,?,?,?,?)";

	public boolean insertEstimate(MedicineSalesMainDTO sale, String visitId) throws SQLException {

		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(INSERT_ESTIMATE);
			int i = 1;
			ps.setString(i++, sale.getSaleId());
			ps.setTimestamp(i++, sale.getSaleDate());
			ps.setInt(i++, Integer.parseInt(sale.getStoreId()));
			ps.setString(i++, visitId);
			ps.setString(i++, sale.getUsername());
			ps.setBigDecimal(i++, sale.getDiscount());
			ps.setBigDecimal(i++, sale.getRoundOffPaise());
			if (sale.getDiscountPer() != null)
				ps.setBigDecimal(i++, sale.getDiscountPer());
			else
				ps.setBigDecimal(i++, BigDecimal.ZERO);
			ps.setString(i++, sale.getDoctor());
			int count = ps.executeUpdate();
			return (count == 1);

		} finally {
			DataBaseUtil.closeConnections(null, ps);
		}
	}

	private static final String INSERT_ESTIMATE_ITEM = "INSERT INTO store_estimate_details "
			+ " (estimate_item_id, estimate_id, medicine_id, batch_no, quantity, tax, tax_rate, rate, amount, "
			+ "  package_unit, orig_rate, expiry_date, disc, discount_per, basis) "
			+ " VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ";

	public boolean insertEstimateItems(ArrayList<MedicineSalesDTO> sales) throws SQLException {

		PreparedStatement ps = con.prepareStatement(INSERT_ESTIMATE_ITEM);

		try {
			Iterator<MedicineSalesDTO> it = sales.iterator();
			while (it.hasNext()) {
				MedicineSalesDTO sale = it.next();
				int i = 1;
				ps.setInt(i++, sale.getSaleItemId());
				ps.setString(i++, sale.getSaleId());
				ps.setInt(i++, Integer.parseInt(sale.getMedicineId()));
				ps.setString(i++, sale.getBatchNo());
				ps.setBigDecimal(i++, sale.getQuantity());
				ps.setBigDecimal(i++, sale.getTax());
				ps.setBigDecimal(i++, sale.getTaxPer());
				ps.setBigDecimal(i++, sale.getRate());
				ps.setBigDecimal(i++, sale.getAmount());
				ps.setBigDecimal(i++, sale.getPackageUnit());
				ps.setBigDecimal(i++, sale.getOrigRate());
				ps.setDate(i++, sale.getExpiryDate());
				ps.setBigDecimal(i++, sale.getMedDiscRS());
				ps.setBigDecimal(i++, sale.getMedDisc());
				ps.setString(i++, sale.getBasis());
				ps.addBatch();
			}
			int[] updates = ps.executeBatch();
			return DataBaseUtil.checkBatchUpdates(updates);

		} finally {
			ps.close();
		}
	}

	private static final String USER_NAME = "SELECT temp_username FROM u_user WHERE emp_username=?";

	public static String getuserName(String username) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(USER_NAME);
			ps.setString(1, username);

			return DataBaseUtil.getStringValueFromDb(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String GET_SALE_DETAILS = "SELECT "
			+ " pmd.medicine_name, pmd.medicine_short_name, pmd.issue_units, pmd.package_uom, mm.manf_mnemonic, mm.manf_name, "
			+ " pmsm.doctor_name, pmsm.bill_no,pmsm.charge_id, pmsm.return_bill_no, pmsm.type , pmsm.sale_date, pmsm.date_time, "
			+ " pmsm.discount as bill_discount, pmsm.discount_per as bill_discount_per, pmsm.round_off, sibd.batch_no, "
			+ " pm.sale_id,pm.sale_item_id, pm.item_code, case when pm.sale_unit='I' then pm.quantity else round(pm.quantity/pm.package_unit,2) end as quantity, pm.return_qty, "
			+ " pm.sale_unit, pm.expiry_date, pm.amount, pm.disc as discount, pm.discount_per, pm.tax, pm.tax_rate, pm.erx_activity_id, pmsm.erx_reference_no,  "
			+ " pm.package_unit, pm.insurance_claim_amt, pm.prior_auth_id, pm.return_insurance_claim_amt, pm.return_amt,"
			+ " pscd.insurance_claim_amt as pri_insurance_claim_amt, pscd.tax_amt as pri_sponsor_tax_amt, sscd.insurance_claim_amt as sec_insurance_claim_amt,sscd.tax_amt as sec_sponsor_tax_amt, "
			+ " (pm.tax - (coalesce(pscd.tax_amt,0) + coalesce(sscd.tax_amt,0))) as patient_tax_amt, gn.generic_name, sic.control_type_name, package_type, "
			+ " rate as package_rate, case when pm.sale_unit='I' then round(rate/pm.package_unit,2) else rate end as rate,"
			+ " (select count(*) from store_sales_details join store_sales_main using(sale_id)where sale_id=pm.sale_id ) as rcount, "
			+ " scm.category as item_category,pmsm.store_id,pmd.cust_item_code, " +
			// "
			// ig.item_group_name,isb.item_subgroup_name,sstd.tax_rate,sstd.tax_amt,
			// "+
			" s.pharmacy_tin_no, hcms.tin_number, ptm.tin_number as pri_tpa_tin_number,stm.tin_number as sec_tpa_tin_number, picm.tin_number as pri_insur_tin_number,sicm.tin_number as sec_insur_tin_number "
			+ " FROM store_sales_details pm " + " JOIN store_item_batch_details sibd USING(item_batch_id) "
			+ " JOIN store_item_details pmd ON (pmd.medicine_id=pm.medicine_id) "
			+ " LEFT JOIN generic_name gn on gn.generic_code=pmd.generic_name "
			+ " LEFT JOIN store_category_master scm ON (scm.category_id = pmd.med_category_id) "
			+ " LEFT JOIN store_item_controltype sic ON (sic.control_type_id = pmd.control_type_id) "
			+ " JOIN store_sales_main pmsm ON (pmsm.sale_id=pm.sale_id ) "
			+ " JOIN manf_master mm ON (mm.manf_code = pmd.manf_name) " + " JOIN bill b ON (pmsm.bill_no=b.bill_no) "
			+ " LEFT JOIN bill_charge bc ON (bc.bill_no = b.bill_no AND bc.charge_id = pmsm.charge_id) "
			+ " LEFT JOIN bill_claim pbcl ON (bc.bill_no = pbcl.bill_no AND pbcl.priority = 1) "
			+ " LEFT JOIN bill_claim sbcl ON (bc.bill_no = sbcl.bill_no AND sbcl.priority = 2) "
			+ " LEFT JOIN bill_charge_claim pbccl ON (bc.charge_id = pbccl.charge_id AND pbcl.claim_id = pbccl.claim_id ) "
			+ " LEFT JOIN bill_charge_claim sbccl ON (bc.charge_id = sbccl.charge_id AND sbcl.claim_id = sbccl.claim_id) "
			+ " LEFT JOIN sales_claim_details pscd ON (pscd.sale_item_id = pm.sale_item_id AND pscd.claim_id = pbcl.claim_id AND pbcl.priority =1) "
			+ " LEFT JOIN sales_claim_details sscd ON (sscd.sale_item_id = pm.sale_item_id AND sscd.claim_id = sbcl.claim_id AND sbcl.priority =2) "
			+
			// " JOIN store_sales_main ssm ON (ssm.sale_id=pm.sale_id AND
			// ssm.charge_id = bc.charge_id) "+
			" LEFT JOIN stores s ON (s.dept_id=pmsm.store_id) "
			+ " LEFT JOIN hospital_center_master hcms ON (hcms.center_id=s.center_id) "
			+ " LEFT JOIN patient_registration pr ON (pr.patient_id = b.visit_id) " +
			// " LEFT JOIN store_sales_tax_details sstd on sstd.sale_item_id =
			// pm.sale_item_id "+
			// " LEFT JOIN item_sub_groups_tax_details isgtd ON
			// isgtd.item_subgroup_id = sstd.item_subgroup_id "+
			// " LEFT JOIN item_sub_groups isb ON isb.item_subgroup_id =
			// isgtd.item_subgroup_id "+
			// " LEFT JOIN item_groups ig on ig.item_group_id =
			// isb.item_group_id "+
			" LEFT JOIN tpa_master ptm ON (ptm.tpa_id = pr.primary_sponsor_id) "
			+ " LEFT JOIN tpa_master stm ON (stm.tpa_id = pr.secondary_sponsor_id) "
			+ " LEFT JOIN patient_insurance_plans ppip ON (ppip.patient_id = b.visit_id AND ppip.priority = 1) "
			+ " LEFT JOIN patient_insurance_plans spip ON (spip.patient_id = b.visit_id AND spip.priority = 2) "
			+ " LEFT JOIN insurance_company_master picm ON (picm.insurance_co_id = ppip.insurance_co AND ppip.priority = 1) "
			+ " LEFT JOIN insurance_company_master sicm ON (sicm.insurance_co_id = spip.insurance_co AND spip.priority = 2) "
			+ " WHERE ";

	private static final String ORDER_BY_SALE_DATE = " ORDER BY sale_date";

	public static List getSalesList1(String saleId) throws SQLException {
		List salesList = null;
		Connection c = null;
		PreparedStatement ps = null;
		try {
			c = DataBaseUtil.getReadOnlyConnection();
			ps = c.prepareStatement(GET_SALE_DETAILS + "pm.sale_id=? ORDER BY pm.sale_item_id");
			ps.setString(1, saleId);
			salesList = DataBaseUtil.queryToDynaList(ps);
		} finally {
		  if (ps != null) {
		    ps.close();
		  }
		  if (c != null) {
		    c.close();
		  }
		}
		return salesList;
	}

	private static final String GET_SALE_DETAILS_PRESCLABEL = "SELECT ssd.dosage,ssd.frequency,ssd.doctor_remarks,ssd.special_instr,ssd.sales_remarks,ssd.quantity,ssd.duration,ssd.expiry_date,ssd.duration_units,mr.route_name, "
			+ " get_patient_full_name(sm.salutation, COALESCE(pd.patient_name, src.customer_name), pd.middle_name, pd.last_name) AS patient_full_name,ssm.doctor_name,b.bill_no,ssm.sale_date,lm.label_msg,ssm.sale_id, "
			+ " mm.manf_name,mm.manf_mnemonic,scm.category as category_name ,pr.mr_no,src.customer_name,sid.medicine_name,sid.issue_base_unit,sid.issue_qty,ssd.sale_unit,sid.medicine_id, 0 as lblcount, "
			+ " CASE WHEN hcm.center_id = 0 THEN '' ELSE hcm.center_name END AS center_name,sid.cust_item_code,sibd.item_batch_id, sibd.batch_no,pd.government_identifier,sid.issue_units,sid.medicine_short_name,ssd.dosage_unit  "
			+ " FROM store_sales_details ssd JOIN store_sales_main ssm ON (ssm.sale_id = ssd.sale_id) "
			+ " JOIN store_item_batch_details sibd ON (sibd.item_batch_id = ssd.item_batch_id) " 
			+ " JOIN bill b ON (b.bill_no = ssm.bill_no) "
			+ " JOIN store_item_details sid ON (sid.medicine_id = ssd.medicine_id)"
			+ " LEFT JOIN patient_registration pr ON (pr.patient_id = b.visit_id) "
			+ " LEFT JOIN store_category_master scm ON (scm.category_id = sid.med_category_id) "
			+ " LEFT JOIN store_retail_customers src ON (src.customer_id = b.visit_id) "
			+ " LEFT JOIN patient_details pd ON (pd.mr_no = pr.mr_no) "
			+ " LEFT JOIN salutation_master sm ON (sm.salutation_id = pd.salutation) "
			+ " LEFT JOIN manf_master mm ON (mm.manf_name = sid.manf_name) "
			+ " LEFT JOIN label_master lm ON (lm.label_id = ssd.label_id) "
			+ " LEFT JOIN medicine_route mr ON (mr.route_id = ssd.route_of_admin)	"
			+ " LEFT JOIN hospital_center_master hcm ON (hcm.center_id = pr.center_id or hcm.center_id = src.center_id) "
			+ " WHERE ";

	public static List getPrescList(String saleId) throws SQLException {
		List salesList = null;
		Connection c = null;
		PreparedStatement ps = null;
		try {
			c = DataBaseUtil.getReadOnlyConnection();
			ps = c.prepareStatement(GET_SALE_DETAILS_PRESCLABEL + "ssd.sale_id=?");
			ps.setString(1, saleId);
			salesList = DataBaseUtil.queryToDynaList(ps);
		} finally {
			if (ps != null) {ps.close();}
			if (c != null) {
        c.close();
      }
		}
		return salesList;
	}

	public List<BasicDynaBean> getSalesItemList(String saleId) throws SQLException {
		List salesList = null;
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(GET_SALE_DETAILS + "pm.sale_id=? ORDER BY pm.sale_item_id");
			ps.setString(1, saleId);
			salesList = DataBaseUtil.queryToDynaList(ps);
		} finally {
		  if (ps != null) {ps.close();}
		}
		return salesList;
	}

	public static List getSalesAgtRetList(String saleId) throws SQLException {
		List salesList = null;
		Connection c = null;
		PreparedStatement ps = null;
		try {
			c = DataBaseUtil.getReadOnlyConnection();
			ps = c.prepareStatement(GET_SALE_DETAILS + " pmsm.return_bill_no=? ORDER BY pm.sale_item_id");
			ps.setString(1, saleId);
			salesList = DataBaseUtil.queryToDynaList(ps);
		} finally {
		  if (ps != null) {ps.close();}
		  if (c != null) {
        c.close();
      }
		}
		return salesList;
	}

	public static List getSalesList(String billno) throws SQLException {
		List salesList = null;
		Connection c = null;
		PreparedStatement ps = null;
		try {
			c = DataBaseUtil.getReadOnlyConnection();
			ps = c.prepareStatement(GET_SALE_DETAILS + " pmsm.bill_no=?" + ORDER_BY_SALE_DATE);
			ps.setString(1, billno);
			salesList = DataBaseUtil.queryToDynaList(ps);
		} finally {
		  if (ps != null) {ps.close();}
		  if (c != null) { c.close();}
		}
		return salesList;
	}

	private static final String GET_SALE_FOR_BILL = "select pmsm.doctor_name, pmsm.sale_id, pmsm.sale_date, pmsm.discount as bill_discount, pmd.medicine_name,pmd.cust_item_code,"
			+ "pm.batch_no, pm.quantity, pm.expiry_date, pm.amount, mm.manf_mnemonic,round(rate/package_unit,2) as rate,  rate as package_rate, pmsm.round_off,"
			+ "(select count(*) from store_sales_details join store_sales_main using(sale_id)where sale_id=pm.sale_id ) "
			+ "as rcount  FROM store_sales_details pm  "
			+ "JOIN store_item_details pmd ON (pmd.medicine_id=pm.medicine_id) "
			+ "JOIN store_sales_main pmsm ON (pmsm.sale_id=pm.sale_id ) "
			+ "JOIN manf_master mm ON (mm.manf_code = pmd.manf_name) "
			+ "JOIN bill b ON (pmsm.bill_no=b.bill_no)  WHERE  pmsm.bill_no=? ORDER BY pmsm.sale_id ";

	public static List getSaleListForPendingBill(String billNo) throws SQLException {
		String billNoLocal = billNo;
		List<BasicDynaBean> saleList = new ArrayList<BasicDynaBean>();
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_SALE_FOR_BILL);
			ps.setString(1, billNoLocal);
			saleList = (ArrayList<BasicDynaBean>) DataBaseUtil.queryToDynaList(ps);
		} finally {
		  if (ps != null) {ps.close();}
		  if (con != null) { con.close();}
		}
		return saleList;

	}

	private static final String GET_SALE_MAIN_INFO = "SELECT " +
			"  pmsm.sale_id, pmsm.sale_date, pmsm.type, pmsm.bill_no, pmsm.sale_date, pmsm.date_time, " +
			"  pmsm.username, pmsm.discount, pmsm.discount_per, pmsm.round_off, pmsm.return_bill_no, " +
			"  pmsm.store_id, c.counter_id, c.counter_no as counter_name, erx_reference_no, is_external_pbm,   " +
			"  b.visit_type,b.closed_date, b.status, b.visit_id, b.bill_type, s.dept_name, COALESCE(u.temp_username, u.emp_username) AS user_display_name, " +
			"  hcms.tin_number as center_tin_no, s.pharmacy_tin_no as tin_no, s.pharmacy_drug_license_no as dl_no,template_name, bc.user_remarks, pmsm.total_item_amount, pmsm.total_item_discount, total_item_tax " +
			" FROM store_sales_main pmsm " +
			"  JOIN bill b ON (b.bill_no = pmsm.bill_no) " +
			"  JOIN bill_charge bc ON (bc.charge_id = pmsm.charge_id) " +
			"  JOIN stores s ON (s.dept_id = pmsm.store_id) " +
			"  LEFT JOIN hospital_center_master hcms ON (hcms.center_id=s.center_id) "+
			"  LEFT JOIN counters c ON (s.counter_id = c.counter_id) " +
			"  JOIN u_user u ON (u.emp_username = pmsm.username) " +
			" WHERE sale_id = ? ";

	public static BasicDynaBean getSalesMain(String saleId) throws SQLException {
		List l = DataBaseUtil.queryToDynaList(GET_SALE_MAIN_INFO, saleId);
		if (l.size() > 0)
			return (BasicDynaBean) l.get(0);
		else
			return null;
	}

	private static final String GET_ESTIMATE_MAIN_DETAILS = "SELECT pmsm.estimate_id, pmsm.estimate_date, "
			+ "pmsm.username, pmsm.discount, pmsm.discount_per, pmsm.round_off, pmsm.store_id, c.counter_id, "
			+ "c.counter_no as counter_name, pmsm.visit_id, s.dept_name, u.temp_username AS user_display_name, "
			+ "s.pharmacy_tin_no AS tin_no, s.pharmacy_drug_license_no as dl_no "
			+ "FROM store_estimate_main pmsm JOIN stores s ON (s.dept_id = pmsm.store_id) "
			+ "LEFT JOIN counters c ON (s.counter_id = c.counter_id) "
			+ "JOIN u_user u ON (u.emp_username = pmsm.username) WHERE estimate_id = ? ";

	public static BasicDynaBean getEstimateMain(String estimateId) throws SQLException {
		List l = DataBaseUtil.queryToDynaList(GET_ESTIMATE_MAIN_DETAILS, estimateId);
		if (l.size() > 0)
			return (BasicDynaBean) l.get(0);
		else
			return null;
	}

	private static final String GET_ESTIMATE_DETAILS = "SELECT pmsm.doctor_name,pmd.medicine_name,pmd.cust_item_code, "
			+ "pmd.medicine_short_name, mm.manf_mnemonic, mm.manf_name, pm.batch_no, pm.expiry_date, pm.quantity, pm.amount, "
			+ "pm.disc as discount, pm.discount_per, pm.tax, pm.tax_rate, pmsm.estimate_date, "
			+ "pmsm.discount as bill_discount, pmsm.round_off, pmsm.discount_per as bill_discount_per, "
			+ "ROUND(rate/package_unit,2) as rate, " + "(SELECT COUNT(*) from store_estimate_details "
			+ "JOIN store_estimate_main using(estimate_id) " + "WHERE estimate_id=pm.estimate_id ) AS rcount "
			+ " FROM store_estimate_details pm " + " JOIN store_item_details pmd ON (pmd.medicine_id=pm.medicine_id) "
			+ " JOIN store_estimate_main pmsm ON (pmsm.estimate_id=pm.estimate_id ) "
			+ " JOIN manf_master mm ON (mm.manf_code = pmd.manf_name) " + " WHERE ";

	public static List getEstimateList(String estimateId) throws SQLException {
		List salesList = null;
		Connection c = null;
		PreparedStatement ps = null;
		try {
			c = DataBaseUtil.getReadOnlyConnection();
			ps = c.prepareStatement(GET_ESTIMATE_DETAILS + "pm.estimate_id=? ORDER BY pm.estimate_item_id");
			ps.setString(1, estimateId);
			salesList = DataBaseUtil.queryToDynaList(ps);
		} finally {
		  if (ps != null) {ps.close();}
		  if (c != null) { c.close();}
		}
		return salesList;
	}

	private static final String GET_SALE_ID_BILL_NO = "SELECT * FROM store_sales_main WHERE sale_id = ?";

	public static BasicDynaBean getSaleIdBillNo(String saleId) throws SQLException {
		Connection c = null;
		PreparedStatement ps = null;
		try {
			c = DataBaseUtil.getReadOnlyConnection();
			ps = c.prepareStatement(GET_SALE_ID_BILL_NO);
			ps.setString(1, saleId);
			List l = DataBaseUtil.queryToDynaList(ps);
			if (l.size() > 0) {
				return (BasicDynaBean) l.get(0);
			}
		} finally {
			DataBaseUtil.closeConnections(c, ps);
		}
		return null;
	}

	private static final String PENDING_SALE_BILLS_LIST_QUERY_FIELDS = " SELECT pr.mr_no, COALESCE(pr.patient_id, customer_id) AS patient_id, "
			+ "   coalesce(get_patient_name(pd.salutation, pd.patient_name, pd.middle_name, pd.last_name), prc.customer_name) as NAME, "
			+ " b.bill_no, b.bill_type, to_char(pmsm.sale_date,'DD-MM-YYYY') AS sale_date, pmsm.sale_id ";

	private static final String PENDING_SALE_BILLS_LIST_QUERY_COUNT = "SELECT count(pmsm.sale_id) ";

	private static final String PENDING_SALE_BILLS_LIST_QUERY_TABLES = " FROM bill b "
			+ " LEFT JOIN patient_registration pr ON (pr.patient_id = b.visit_id) "
			+ " LEFT JOIN store_retail_customers prc ON (prc.customer_id = b.visit_id) "
			+ " LEFT JOIN patient_details pd ON (pd.mr_no=pr.mr_no) "
			+ " JOIN store_sales_main pmsm ON (pmsm.bill_no=b.bill_no) ";

	private static final String PENDING_SALE_BILLS_LIST_INIT_WHERE = "WHERE b.bill_type='P' AND b.restriction_type='P' AND b.status='A'";

	public static PagedList getPendingSaleList(String mrno, String billNo, int pageNum, String patName, Date fromDate,
			Date toDate, String sortFeild, boolean sortReverse) throws SQLException {

		Connection con = null;
		ArrayList pendingSalesList = null;
		int totalCount = 0;

		try {
			con = DataBaseUtil.getReadOnlyConnection();

			SearchQueryBuilder qb = null;
			qb = new SearchQueryBuilder(con, PENDING_SALE_BILLS_LIST_QUERY_FIELDS, PENDING_SALE_BILLS_LIST_QUERY_COUNT,
					PENDING_SALE_BILLS_LIST_QUERY_TABLES, PENDING_SALE_BILLS_LIST_INIT_WHERE, sortFeild, sortReverse,
					20, pageNum);

			// add the value for the initial where clause

			if (sortFeild != null) {

			} else {
				sortFeild = "pr.mr_no";
				sortReverse = true;
				qb.addSecondarySort("pmsm.sale_id");
			}
			qb.addFilter(SearchQueryBuilder.STRING, "pr.mr_no", "=", mrno);
			qb.addFilter(SearchQueryBuilder.STRING, "COALESCE((pd.patient_name|| ' ' ||pd.last_name), customer_name)",
					"ILIKE", patName);
			qb.addFilter(SearchQueryBuilder.STRING, "pmsm.sale_id", "=", billNo);
			qb.addFilter(SearchQueryBuilder.DATE, "DATE(pmsm.sale_date)", ">=", fromDate);
			qb.addFilter(SearchQueryBuilder.DATE, "DATE(pmsm.sale_date)", "<=", toDate);

			qb.build();
			PreparedStatement psData = qb.getDataStatement();
			PreparedStatement psCount = qb.getCountStatement();
			pendingSalesList = DataBaseUtil.queryToArrayList(psData);
			totalCount = Integer.parseInt(DataBaseUtil.getStringValueFromDb(psCount));
			psData.close();
			psCount.close();

		} finally {
			if (con != null)
				con.close();
		}

		return new PagedList(pendingSalesList, totalCount, 20, pageNum);
	}

	public static final String GET_RETAIL_CREDIT_FIELDS = "SELECT  b.bill_no,prc.customer_name,prc.customer_id,"
			+ " prc.phone_no,prc.credit_limit,sum(bc.amount) as current_due,sum(bc.discount)as discount ";

	private static final String GET_RETAIL_CREDIT_FIELDS_COUNT = "SELECT count(distinct(b.bill_no)) ";

	private static final String GET_RETAIL_CREDIT_FIELDS_TABLES = "FROM store_retail_customers prc"
			+ " JOIN bill b ON b.visit_id=prc.customer_id AND b.bill_type='C' AND b.status='A' AND b.visit_type='r'"
			+ " JOIN store_sales_main pmsm ON (pmsm.bill_no = b.bill_no) "
			+ " JOIN bill_charge bc ON (bc.charge_id = pmsm.charge_id) ";

	private static final String GET_RETAIL_CREDIT_FIELDS_INIT_WHERE = "WHERE pmsm.type='S'";

	private static final String GET_RETAIL_CREDIT_FIELDS_GROUP_BY = "b.bill_no, "
			+ " prc.customer_name,prc.customer_id,prc.phone_no,prc.credit_limit";

	public static PagedList getRetailPendingSaleList(String billNo, int pageNum, String rCustName, Date fromDate,
			Date toDate, String sortFeild, boolean sortReverse) throws SQLException {

		Connection con = null;
		ArrayList pendingSalesList = null;
		int totalCount = 0;

		try {
			con = DataBaseUtil.getReadOnlyConnection();

			SearchQueryBuilder qb = null;
			qb = new SearchQueryBuilder(con, GET_RETAIL_CREDIT_FIELDS, GET_RETAIL_CREDIT_FIELDS_COUNT,
					GET_RETAIL_CREDIT_FIELDS_TABLES, null, GET_RETAIL_CREDIT_FIELDS_GROUP_BY, sortFeild, sortReverse,
					20, pageNum);

			// add the value for the initial where clause

			if (sortFeild != null) {

			} else {
				sortFeild = "b.bill_no";
				sortReverse = true;
				qb.addSecondarySort("prc.customer_name");
			}
			qb.addFilter(SearchQueryBuilder.STRING, "prc.customer_name", "ILIKE", rCustName);
			qb.addFilter(SearchQueryBuilder.STRING, "b.bill_no", "=", billNo);
			qb.addFilter(SearchQueryBuilder.DATE, "DATE(pmsm.sale_date)", ">=", fromDate);
			qb.addFilter(SearchQueryBuilder.DATE, "DATE(pmsm.sale_date)", "<=", toDate);

			qb.build();
			PreparedStatement psData = qb.getDataStatement();
			PreparedStatement psCount = qb.getCountStatement();
			pendingSalesList = DataBaseUtil.queryToArrayList(psData);
			totalCount = Integer.parseInt(DataBaseUtil.getStringValueFromDb(psCount));
			psData.close();
			psCount.close();

		} finally {
			if (con != null)
				con.close();
		}

		return new PagedList(pendingSalesList, totalCount, 20, pageNum);

	}

	/*
	 * Returns a list of all sold items for the given sale ID. This is net of
	 * any returns made against the same bill no.
	 */
	private static final String SOLD_ITEMS = "SELECT sibd.batch_no,pm.sale_id, pm.store_id as dept_id, pm.doctor_name, ps.medicine_id, sico.item_code, "
			+ " pm.bill_no, pm.return_bill_no,  "
			+ "  ps.quantity + COALESCE(ps.return_qty, 0) AS qty,ps.quantity as sale_qty, "
			+ "  ps.insurance_claim_amt + COALESCE(ps.return_insurance_claim_amt, 0) as insurance_claim_amt, "
			+ "  ps.rate, ps.amount + COALESCE(ps.return_amt) as amount, "
			+ "  ps.tax_rate, ps.tax + COALESCE(ps.return_tax_amt, 0) AS tax, ps.original_tax_amt + COALESCE(ps.return_original_tax_amt, 0) AS original_tax_amt, ps.disc, ps.discount_per, ps.disc as discount_amt, "
			+ "  ps.discount_type, ps.basis, ps.expiry_date as exp_dt, ps.sale_unit, "
			+ "  m.medicine_name, mf.manf_code, mf.manf_name, mf.manf_mnemonic, "
			+ "  ps.package_unit as issue_base_unit,m.item_barcode_id, "
			+ "  m.package_type, mc.category, sic.control_type_name, m.issue_units, "
			+ "  mc.identification, mc.claimable, " + "  CASE WHEN ps.basis='M' THEN ps.rate "
			+ "		ELSE COALESCE(sir.selling_price,COALESCE(ssir.selling_price, COALESCE(m.item_selling_price, sibd.mrp))) END AS selling_price, "
			+ "  COALESCE(sir.tax_type,COALESCE(ssir.tax_type,m.tax_type)) as tax_type,"
			+ "  COALESCE(isld.bin,m.bin) as bin, g.generic_name, "
			+ "  ipd.patient_amount, ipd.patient_amount_per_category, ipm.is_copay_pc_on_post_discnt_amt, "
			+ "  ipd.patient_percent, ipd.patient_amount_cap, m.insurance_category_id, "
			+ "  COALESCE(m.package_uom,'') AS master_package_uom, bc.charge_id, ps.sale_item_id, "
			+ "  ps.item_batch_id, m.cust_item_code, "
			+ "  CASE WHEN m.cust_item_code IS NOT NULL AND  TRIM(m.cust_item_code) != ''  THEN m.medicine_name||' - '||m.cust_item_code ELSE medicine_name END as cust_item_code_with_name, "
			+ " CASE WHEN m.cust_item_code IS NOT NULL AND  TRIM(m.cust_item_code) != '' AND item_barcode_id IS NOT NULL AND  TRIM(item_barcode_id) != '' "
      + " THEN m.medicine_name||' - '||m.cust_item_code||' - '||item_barcode_id  "
      + " WHEN m.cust_item_code IS NOT NULL AND  TRIM(m.cust_item_code) != ''  THEN m.medicine_name||' - '||m.cust_item_code "
      + " WHEN item_barcode_id IS NOT NULL AND  TRIM(item_barcode_id) != ''  THEN m.medicine_name||' - '||item_barcode_id "
      + " ELSE m.medicine_name END as cust_item_code_barcode_with_name "
			+ " FROM store_sales_details ps " + "  JOIN store_sales_main pm USING (sale_id) "
			+ "  JOIN bill b on (pm.bill_no = b.bill_no) " + "  JOIN bill_charge bc ON (bc.charge_id = pm.charge_id)"
			+ "  JOIN store_item_details m USING (medicine_id) "
			+ "  LEFT JOIN item_store_level_details  isld ON isld.medicine_id = ps.medicine_id AND isld.dept_id = pm.store_id "
			+ "  JOIN store_item_batch_details sibd ON (sibd.item_batch_id = ps.item_batch_id )"
			+ "  JOIN manf_master mf ON (mf.manf_code = m.manf_name) "
			+ "  JOIN store_category_master mc ON mc.category_id = m.med_category_id "
			+ "  LEFT JOIN ha_item_code_type hict ON (hict.medicine_id = m.medicine_id AND hict.health_authority=?) "
			+ "  LEFT JOIN store_item_codes sico ON (sico.medicine_id = m.medicine_id AND sico.code_type = hict.code_type) "
			+ "  LEFT JOIN generic_name g ON (m.generic_name = g.generic_code) "
			+ "  LEFT JOIN store_item_controltype sic ON (sic.control_type_id = m.control_type_id) "
			+ "  LEFT JOIN insurance_plan_details ipd on (m.insurance_category_id = ipd.insurance_category_id "
			+ "    AND ipd.patient_type = b.visit_type AND ipd.plan_id = ?) "
			+ "  LEFT JOIN insurance_plan_main ipm ON (ipm.plan_id = ipd.plan_id) "
			+ "  JOIN stores str ON( str.dept_id = pm.store_id ) "
			+ "  LEFT JOIN store_item_rates sir ON (ps.medicine_id = sir.medicine_id AND "
			+ "    sir.store_rate_plan_id = str.store_rate_plan_id) "
			+ "  LEFT JOIN store_item_rates ssir ON (ps.medicine_id = ssir.medicine_id AND "
			+ "    ssir.store_rate_plan_id = str.store_rate_plan_id) " + " WHERE pm.sale_id =? ";

	public static List<BasicDynaBean> getSoldItems(String saleId, int planId, String healthAuthority)
			throws SQLException {
		return DataBaseUtil.queryToDynaList(SOLD_ITEMS, new Object[] { healthAuthority, planId, saleId });
	}

	public static List<BasicDynaBean> getSoldItems(Connection con, String saleId, int planId, String healthAuthority)
			throws SQLException {
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(SOLD_ITEMS);
			ps.setString(1, healthAuthority);
			ps.setInt(2, planId);
			ps.setString(3, saleId);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			if (ps != null) {
				ps.close();
			}
		}
	}

	/*
	 * Returns a list of all sold items for the given visit ID. This is net of
	 * any returns made against the same visit.
	 */
	private static final String VISIT_SOLD_ITEMS_INS = " SELECT s.sale_item_id, s.item_batch_id, s.batch_no, s.medicine_id, s.basis, sico.item_code, s.sponsor_tax_amt, "
			+ "  s.quantity + COALESCE(s.return_qty, 0) as qty, s.insurance_claim_amt,s.quantity as sale_qty, "
			+ "  s.amount + COALESCE(s.return_amt, 0) as amount, "
			+ "  s.insurance_claim_amt + COALESCE(s.return_insurance_claim_amt, 0) as insurance_claim_amt, "
			+ "  CASE WHEN s.basis = 'M' then s.rate ELSE 0 END as mrp, "
			+ "  CASE WHEN s.basis = 'M' then s.rate ELSE sibd.mrp END as selling_price, "
			+ "  s.discount_per, s.disc as discount_amt, s.tax + COALESCE(s.return_tax_amt, 0) AS tax, s.original_tax_amt + COALESCE(s.return_original_tax_amt, 0) AS original_tax_amt, pm.store_id as dept_id, "
			+ "  s.discount_type, s.package_unit as issue_base_unit, "
			+ "  m.medicine_name, sic.control_type_name, m.package_type, m.issue_units, m.item_barcode_id, "
			+ "  mf.manf_code, mf.manf_name, mf.manf_mnemonic, sibd.exp_dt as exp_dt, "
			+ "  COALESCE(sir.tax_rate,COALESCE(ssir.tax_rate,m.tax_rate)) as tax_rate,"
			+ "  COALESCE(sir.tax_type,COALESCE(ssir.tax_type,m.tax_type)) as tax_type, COALESCE(isld.bin,m.bin) as bin, m.insurance_category_id, "
			+ "  m.billing_group_id, scm.category, g.generic_name, scm.identification, scm.claimable, "
			+ "  ipd.patient_amount as patient_amount, ipd.patient_amount_per_category, ipd.patient_percent, "
			+ "  ipm.is_copay_pc_on_post_discnt_amt, ipd.patient_amount_cap as patient_amount_cap, "
			+ "  COALESCE(m.package_uom,'') AS master_package_uom, "
			+ "  pm.sale_unit, bc.charge_id, b.is_tpa, b.bill_type, b.visit_type, scm.billable, scm.retailable,m.cust_item_code, "
			+ "  CASE WHEN m.cust_item_code IS NOT NULL AND  TRIM(m.cust_item_code) != ''  THEN m.medicine_name||' - '||m.cust_item_code ELSE m.medicine_name END as cust_item_code_with_name, "
			+ " CASE WHEN m.cust_item_code IS NOT NULL AND  TRIM(m.cust_item_code) != '' AND item_barcode_id IS NOT NULL AND  TRIM(item_barcode_id) != '' "
			+ " THEN m.medicine_name||' - '||m.cust_item_code||' - '||item_barcode_id  "
			+ " WHEN m.cust_item_code IS NOT NULL AND  TRIM(m.cust_item_code) != ''  THEN m.medicine_name||' - '||m.cust_item_code "
			+ " WHEN item_barcode_id IS NOT NULL AND  TRIM(item_barcode_id) != ''  THEN m.medicine_name||' - '||item_barcode_id "
			+ " ELSE m.medicine_name END as cust_item_code_barcode_with_name "
			+ " FROM store_sales_details s  " + "  JOIN store_sales_main pm USING (sale_id)  "
			+ "  JOIN bill b USING (bill_no)  " + "  JOIN bill_charge bc ON (bc.charge_id = pm.charge_id)"
			+ "  JOIN store_item_details m ON (m.medicine_id = s.medicine_id) "
			+ "  LEFT JOIN item_store_level_details  isld ON isld.medicine_id = s.medicine_id AND isld.dept_id = pm.store_id "
			+ "  LEFT JOIN ha_item_code_type hict ON (hict.medicine_id = m.medicine_id AND hict.health_authority=?) "
			+ "  LEFT JOIN store_item_codes sico ON (sico.medicine_id = m.medicine_id AND sico.code_type = hict.code_type) "
			+ "  JOIN manf_master mf ON (mf.manf_code = m.manf_name) "
			+ "  LEFT JOIN generic_name g ON (m.generic_name = g.generic_code) "
			+ "  JOIN store_item_batch_details sibd ON(sibd.item_batch_id = s.item_batch_id) "
			+ "  JOIN store_category_master scm ON (scm.category_id = m.med_category_id) "
			+ "  LEFT JOIN store_item_controltype sic ON (sic.control_type_id = m.control_type_id) "
			+ "  LEFT JOIN insurance_plan_details ipd ON "
			+ "    (m.insurance_category_id = ipd.insurance_category_id and ipd.patient_type = b.visit_type "
			+ "      AND ipd.plan_id = ?) " + "  LEFT JOIN insurance_plan_main ipm ON (ipm.plan_id = ipd.plan_id) "
			+ "  LEFT JOIN store_item_rates sir ON (m.medicine_id = sir.medicine_id AND "
			+ "    sir.store_rate_plan_id = ?) "
			+ "  LEFT JOIN store_item_rates ssir ON (m.medicine_id = ssir.medicine_id AND "
			+ "    ssir.store_rate_plan_id = ?) " + " WHERE b.visit_id=? AND store_id=? AND pm.type = 'S' "
			+ " ORDER BY sale_item_id DESC ";

	public static List<BasicDynaBean> getVisitSoldItems(String visitId, int storeId, int planId) throws SQLException {

		BasicDynaBean storeBean = StoreDAO.findByStore(storeId);
		int storeRatePlanId = (storeBean.get("store_rate_plan_id") == null ? 0
				: (Integer) storeBean.get("store_rate_plan_id"));

		BasicDynaBean pd = VisitDetailsDAO.getPatientVisitDetailsBean(visitId);
		BasicDynaBean ratePlanBean = organisationDetails.findByKey("org_id",
				(String) pd.get("org_id"));
		int visitRtoreRatePlanId = (ratePlanBean == null || ratePlanBean.get("store_rate_plan_id") == null ? 0
				: (Integer) ratePlanBean.get("store_rate_plan_id"));
		BasicDynaBean storeDetails = StoreDAO.findByStore(storeId);
		int centerId = (Integer) storeDetails.get("center_id");
		String healthAuthority = CenterMasterDAO.getHealthAuthorityForCenter(centerId);

		return DataBaseUtil.queryToDynaList(VISIT_SOLD_ITEMS_INS,
				new Object[] { healthAuthority, planId, visitRtoreRatePlanId, storeRatePlanId, visitId, storeId });
	}

	public static List<BasicDynaBean> getVisitSoldItems(Connection con, String visitId, int storeId, int planId)
			throws SQLException {

		BasicDynaBean storeBean = StoreDAO.findByStore(storeId);
		int storeRatePlanId = (storeBean.get("store_rate_plan_id") == null ? 0
				: (Integer) storeBean.get("store_rate_plan_id"));

		BasicDynaBean pd = VisitDetailsDAO.getPatientVisitDetailsBean(visitId);
		BasicDynaBean ratePlanBean = organisationDetails.findByKey("org_id",
				(String) pd.get("org_id"));
		int visitRtoreRatePlanId = (ratePlanBean == null || ratePlanBean.get("store_rate_plan_id") == null ? 0
				: (Integer) ratePlanBean.get("store_rate_plan_id"));
		int centerId = (Integer) storeBean.get("center_id");
		String healthAuthority = CenterMasterDAO.getHealthAuthorityForCenter(centerId);

		return DataBaseUtil.queryToDynaList(con, VISIT_SOLD_ITEMS_INS,
				new Object[] { healthAuthority, planId, visitRtoreRatePlanId, storeRatePlanId, visitId, storeId });
	}

	private static final String UPDATE_RETURN_AGAINST_VISIT = " UPDATE store_sales_main sm SET returned_against_visit=? "
			+ "  FROM bill b " + " WHERE b.bill_no = sm.bill_no AND b.visit_id=? ";

	public void updateReturnAgainstVisit(String visitId, String status) throws SQLException {
		try (PreparedStatement ps = con.prepareStatement(UPDATE_RETURN_AGAINST_VISIT);) {
    		ps.setString(1, status);
    		ps.setString(2, visitId);
    		int count = ps.executeUpdate();
		}
	}

	private static final String GET_MEDICINE_LIST = " SELECT medicine_name AS medicinename, coalesce(g.generic_name, '') AS genericname, g.generic_code,cust_item_code, "
			+ " CASE WHEN cust_item_code IS NOT NULL AND  TRIM(cust_item_code) != ''  THEN medicine_name||' - '||cust_item_code ELSE medicine_name END as cust_item_code_with_name "
			+ " FROM store_item_details pmd " + " 	LEFT JOIN generic_name g ON (pmd.generic_name=g.generic_code) "
			+ " WHERE (medicine_name ILIKE ?  OR cust_item_code ILIKE ?) LIMIT 50";

	public static List<BasicDynaBean> getMedicineNames(String searchInput) throws SQLException {

		Connection con = null;
		PreparedStatement ps = null;
		List<BasicDynaBean> l = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_MEDICINE_LIST);
			ps.setString(1, searchInput);
			ps.setString(2, searchInput);
			l = DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return l;
	}

	private static final String MEDICINES_PRESCIBED_AND_SOLD = " SELECT medicine_name FROM store_sales_main pmsm "
			+ " 	JOIN bill b USING (bill_no) " + " 	JOIN store_sales_details pms USING (sale_id) "
			+ " 	JOIN store_item_details USING (medicine_id) " + " WHERE visit_id=? " + " UNION "
			+ " SELECT coalesce(sid.medicine_name, g.generic_name) as medicine_name "
			+ "	FROM patient_prescription pp "
			+ "	JOIN patient_medicine_prescriptions pmp ON (pp.patient_presc_id=pmp.op_medicine_pres_id) "
			+ "	LEFT JOIN store_item_details sid ON (pmp.medicine_id=sid.medicine_id) "
			+ "	LEFT JOIN generic_name g ON (pmp.generic_code=g.generic_code) "
			+ " 	JOIN doctor_consultation dc USING (consultation_id) " + " WHERE patient_id=? " + " UNION "
			+ " SELECT coalesce(sid.medicine_name, g.generic_name) as medicine_name FROM pbm_medicine_prescriptions pbmp "
			+ "	LEFT JOIN store_item_details sid ON (pbmp.medicine_id=sid.medicine_id) "
			+ "	LEFT JOIN generic_name g ON (pbmp.generic_code=g.generic_code) "
			+ " 	JOIN doctor_consultation dc USING (consultation_id) " + " WHERE patient_id=? ";

	/**
	 * returns list of medicines prescribed and medicines sold.
	 *
	 * @param patientId
	 * @return
	 */
	public static List<BasicDynaBean> getMedicinesPrescribedAndSold(String patientId) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		List<BasicDynaBean> l = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(MEDICINES_PRESCIBED_AND_SOLD);
			ps.setString(1, patientId);
			ps.setString(2, patientId);
			ps.setString(3, patientId);

			l = DataBaseUtil.queryToDynaList(ps);

		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return l;
	}

	private static final String DOCTOR_NAME_TO_ID = "select doctor_id::varchar from store_retail_doctor where doctor_name=?"
			+ " UNION " + " select doctor_id from doctors where doctor_name = ? ";

	public static String doctorNameToId(String docName) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(DOCTOR_NAME_TO_ID);
			ps.setString(1, docName);
			ps.setString(2, docName);

			return DataBaseUtil.getStringValueFromDb(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String GET_DOCTOR_NAMES = "select distinct(doctor_name) from doctors where status='A'";

	public static ArrayList getDoctorNames() throws SQLException {
		return DataBaseUtil.queryToArrayList(GET_DOCTOR_NAMES);
	}

	private static final String SALES_REPORT_QUERY = "SELECT m.sale_id||'' AS sale_id,m.sale_date,m.date_time ,"
			+ "m.type AS sales_type,m.username, m.bill_no, d.dept_name,"
			+ " SUM(amount) AS amount, SUM(tax) AS tax, SUM(discount) AS discount, tax_rate||'%' AS tax_rate "
			+ " FROM  store_sales_main m" + " JOIN store_sales_details ps USING (sale_id)"
			+ " JOIN bill b on (m.bill_no = b.bill_no)" + " JOIN stores d on (m.store_id = d.dept_id)"
			+ " WHERE date_trunc('day',date_time) between ? AND ?"
			+ " GROUP BY dept_name, sale_id, sale_date, date_time,type, m.bill_no, m.username,tax_rate";

	public static List<BasicDynaBean> getReport(Connection con, Date from, Date to) throws SQLException {
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(SALES_REPORT_QUERY);
			ps.setDate(1, from);
			ps.setDate(2, to);

			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			if (ps != null)
				ps.close();
		}

	}

	public String getValueFromDB(String query, String param) throws SQLException {
		try (PreparedStatement ps = con.prepareStatement(query);) {
    		ps.setString(1, param);
    		return DataBaseUtil.getStringValueFromDb(ps);
		}
	}

	private static String SEARCH_ESTIMATE_SELECT = "SELECT * ";
	private static String SEARCH_ESTIMATE_COUNT = "SELECT count(*) ";
	private static String SEARCH_ESTIMATE_TABLES = "FROM "
			+ " (SELECT coalesce(prc.customer_name, get_patient_name(pd.salutation, pd.patient_name, pd.middle_name, pd.last_name)) as full_name, "
			+ "   pd.mr_no, doctor_name, pe.estimate_id, estimate_date, discount, round_off, SUM (sed.amount) AS total_item_amount, "
			+ "   estimate_date as estimate_date_from, estimate_date as estimate_date_to "
			+ "  FROM store_estimate_main pe "
			+ "	 JOIN store_estimate_details sed ON (sed.estimate_id = pe.estimate_id) "
			+ "  LEFT JOIN store_retail_customers prc ON (prc.customer_id = pe.visit_id) "
			+ "  LEFT JOIN ( SELECT	pr.mr_no, pr.patient_id, pd.salutation, pd.patient_name, "
			+ "	 pd.middle_name, pd.last_name "
			+ "  FROM patient_registration pr "
			+ "  LEFT JOIN patient_details pd ON (pd.mr_no = pr.mr_no) "
			+ "  WHERE ( patient_confidentiality_check(pd.patient_group,pd.mr_no))) pd ON (pd.patient_id = pe.visit_id)"
			+ "  GROUP BY prc.customer_name,pd.patient_name,pd.last_name,pd.salutation, pd.middle_name,pd.mr_no,doctor_name,pe.estimate_id,"
			+ "  estimate_date, discount, round_off,estimate_date , estimate_date) AS tbl ";

	public static PagedList searchEstimates(Map filter, Map listing) throws ParseException, SQLException {
		Connection con = null;
		SearchQueryBuilder qb = null;
		PagedList l = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();

			qb = new SearchQueryBuilder(con, SEARCH_ESTIMATE_SELECT, SEARCH_ESTIMATE_COUNT, SEARCH_ESTIMATE_TABLES,
					listing);
			qb.addFilterFromParamMap(filter);
			qb.addSecondarySort("estimate_id");
			qb.build();
			l = qb.getMappedPagedList();
			return l;
		} finally {
		  if (qb != null) {
		    qb.close();
		  }
			DataBaseUtil.closeConnections(con, null);
		}

	}

	private static final String GET_PHARMA_BREAKUP = "SELECT sm.sale_id, sm.type, sm.sale_date, sm.discount, sm.round_off,"
			+ "  m.medicine_name, mf.manf_name,mf.manf_mnemonic, s.batch_no,to_char(s.expiry_date,'MM-YYYY') as expiry_date, "
			+ " s.tax, s.tax_rate, s.quantity, s.amount, s.rate, s.package_unit, s.insurance_claim_amt,s.claim_recd_total, "
			+ " s.return_insurance_claim_amt, s.return_amt, s.return_qty," + " s.disc, s.discount_per,m.cust_item_code,"
			+ " ig.item_group_name as billing_group_name, s.billing_group_id "
			+ "FROM store_sales_main sm "
			+ "JOIN store_sales_details s USING (sale_id) JOIN store_item_details m ON (s.medicine_id = m.medicine_id) "
			+ "JOIN manf_master mf ON (m.manf_name = mf.manf_code) "
			+ "LEFT JOIN item_groups ig ON ig.item_group_id = s.billing_group_id  and item_group_type_id='BILLGRP' "
			+ "WHERE sm.bill_no = ? "
			+ "ORDER by sm.sale_id, s.sale_item_id";

	public static List<BasicDynaBean> getPharmaBreakupList(String billNo) throws SQLException {
		PreparedStatement ps = null;
		Connection con = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_PHARMA_BREAKUP);
			ps.setString(1, billNo);
			List saleList = DataBaseUtil.queryToDynaList(ps);
			return saleList;
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String NUM_SALES_FOR_CATEGORY = " SELECT count(*) as count " + " FROM store_sales_details s "
			+ "  JOIN store_sales_main sm USING (sale_id) " + "  JOIN bill b USING (bill_no) "
			+ "  JOIN patient_registration pr ON (b.visit_id = pr.patient_id) "
			+ " WHERE pr.main_visit_id = (SELECT main_visit_id FROM patient_registration pr1 WHERE patient_id=? LIMIT 1) AND s.insurance_category_id=? ";

	public static int getNumSalesForCategory(String visitId, int categoryId) throws SQLException {
		BasicDynaBean b = DataBaseUtil.queryToDynaBean(NUM_SALES_FOR_CATEGORY, new Object[] { visitId, categoryId });
		return ((Long) b.get("count")).intValue();
	}

	private static final String GET_PHARMA_CODE_TYPES = " SELECT * FROM  mrd_supported_codes msc  "
			+ " JOIN (select regexp_split_to_table(drug_code_type,E',') as drug_code_type,health_authority FROM health_authority_preferences "
			+ " 		) as hap ON(msc.code_type = hap.drug_code_type) "
			+ " JOIN health_authority_master ham ON(ham.health_authority = hap.health_authority)"
			+ " WHERE msc.code_category='Drug' ";

	public static List getPharmaCodeTypes() throws SQLException {
		return ConversionUtils.listBeanToListMap(DataBaseUtil.queryToDynaList(GET_PHARMA_CODE_TYPES));
	}

	public static List getPharmaCodeTypes(String healthAuthority) throws SQLException {
		StringBuilder query = new StringBuilder(GET_PHARMA_CODE_TYPES);
		query.append(" AND ham.health_authority = ?");
		return ConversionUtils.listBeanToListMap(DataBaseUtil.queryToDynaList(query.toString(), healthAuthority));
	}

	/*
	 * public static List getPharmaCodes(String drugCodeType) throws
	 * SQLException { final String GET_PHARMA_CODES =
	 * " SELECT * FROM  getItemCodesForCodeType('"+drugCodeType+"') "; return
	 * ConversionUtils.listBeanToListMap(DataBaseUtil.queryToDynaList(GET_PHARMA_CODES
	 * )); }
	 */

	public static List getPharmaCodes(String[] drugCodeTypes) throws SQLException {
		StringBuilder query = new StringBuilder("SELECT * FROM  getItemCodesForCodeType('*') ");
		Connection con = null;
		PreparedStatement ps = null;
		int index = 1;
		int j = 0;
		try {
			con = DataBaseUtil.getConnection();
			if (drugCodeTypes != null) {
				DataBaseUtil.addWhereFieldInList(query, "code_type", Arrays.asList(drugCodeTypes), false);
			} else {
				List<BasicDynaBean> drugCodeTypeList = DataBaseUtil.queryToDynaList(
						"select distinct code_type from mrd_supported_codes where code_category = 'Drug';");
				DataBaseUtil.addWhereFieldInList(query, "code_type", drugCodeTypeList, false);
				if (drugCodeTypeList != null && drugCodeTypeList.size() > 0) {
					drugCodeTypes = new String[drugCodeTypeList.size()];
					for (BasicDynaBean codeTypebean : drugCodeTypeList) {
						drugCodeTypes[j++] = (String) codeTypebean.get("code_type");
					}
				}
			}
			ps = con.prepareStatement(query.toString());

			if (drugCodeTypes == null)
				return null;

			if (drugCodeTypes != null) {
				for (String codeType : drugCodeTypes) {
					ps.setString(index++, codeType);
				}
			}
			return ConversionUtils.listBeanToListMap(DataBaseUtil.queryToDynaList(ps));
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}

	}

	private static final String GET_CURRENT_STOCK = "select sum(qty) as qty from store_stock_details WHERE item_batch_id = ? AND dept_id=?";

	public boolean getCurrentStock(ArrayList<StockChangeDTO> medicines) throws SQLException {
		PreparedStatement ps = con.prepareStatement(GET_CURRENT_STOCK);
		boolean success = true;
		try {
			Iterator<StockChangeDTO> it = medicines.iterator();
			while (it.hasNext()) {
				StockChangeDTO m = it.next();
				ps.setInt(1, m.getItemBatchId());
				ps.setInt(2, Integer.parseInt(m.getStoreId()));
				BigDecimal avbl = new BigDecimal(DataBaseUtil.getStringValueFromDb(ps));
				if (avbl.floatValue() < m.getChangeQuantity().floatValue()) {
					success = false;
					break;
				}
			}
			return success;
		} finally {
			ps.close();
		}
	}

	private static final String GET_SALE_ITEM_DETAILS = " SELECT s.medicine_id," 
			+ " 	sm.doctor_name,  "
			+ " 	s.sale_id, gd.dept_name, store_type_name, " 
			+ " 	date(sm.sale_date) as sale_date, sm.username, "
			+ " 	CASE WHEN sm.type = 'R' THEN 'Return' ELSE 'Sale' END AS type, "
			+ " 	it.medicine_name, scm.category, scm.claimable, mm.manf_mnemonic, mm.manf_name, gn.generic_name, "
			+ " 	s.batch_no,  to_char(s.expiry_date, 'Mon-yyyy') AS exp_dt_str, " 
			+ " 	s.package_unit, "
			+ " 	round(s.rate/s.package_unit,2) as unit_rate, s.rate/s.package_unit as unit_rate_wrf, s.rate as pkg_rate, "
			+ " 	CASE WHEN s.sale_unit = 'I' THEN round(s.rate/s.package_unit,2) ELSE round(s.rate,2) END AS sale_rate, "
			+ " 	s.quantity AS issue_quantity, (s.quantity/s.package_unit) AS pkg_quantity, "
			+ " 	CASE WHEN s.sale_unit = 'I' THEN s.quantity ELSE round(s.quantity/s.package_unit, 2) END AS quantity, "
			+ " 	it.issue_units, it.package_uom as pkg_uom, s.erx_activity_id, "
			+ " 	CASE WHEN s.sale_unit = 'I' THEN it.issue_units ELSE it.package_uom END AS sale_uom, "
			+ " 	s.amount+s.disc AS amount, s.amount as post_discount_amt_wrf,  "
			+ " 	CASE WHEN s.sale_unit = 'I' THEN 'Issue' ELSE 'Pkg' END AS sale_unit, "
			+ " 	s.disc AS itemwise_discount, s.amount AS net_amount,s.discount_type,s.discount_per, "
			+ " 	s.tax_rate, s.tax, scd.insurance_claim_amt, it.insurance_category_id, "
			+ "   s.return_qty, s.return_insurance_claim_amt, s.return_amt, s.return_tax_amt, "
			+ "   s.code_type, s.item_code, s.prior_auth_id, pam.prior_auth_mode_name, "
			+ " 	s.pkg_mrp, round(s.pkg_mrp/s.package_unit,2) AS unit_mrp, "
			+ " 	s.pkg_mrp/s.package_unit AS unit_mrp_wrf, "
			+ " 	s.pkg_cp, round(s.pkg_cp/s.package_unit,2) as unit_cp, "
			+ " round(s.quantity*s.pkg_mrp/s.package_unit,2) AS mrp_value, "
			+ " round(s.quantity*s.pkg_mrp/s.package_unit,2) AS sp_value, "
			+ " round(s.quantity*s.pkg_cp/s.package_unit,2) AS cp_value, "
			+ " round((s.amount - s.quantity*s.pkg_cp/s.package_unit),2) as profit, "
			+ " s.sale_item_id, b.status AS billstatus, s.prior_auth_mode_id, "
			+ " round(s.amount,2) as post_discount_amt, bc.charge_id AS chargeId, bc.charge_id, "
			+ " s.amount_included, s.qty_included, s.package_finalized,"
			+ " CASE WHEN (b.dyna_package_id = 0 OR coalesce(s.amount_included,0) = coalesce((s.amount + s.return_amt),0)) THEN 'N' "
			+ "	   WHEN (coalesce(s.amount_included,0) = 0) THEN 'Y' ELSE 'P' END AS charge_excluded,"
			+ "  sm.bill_no,b.visit_id,s.claim_status,s.denial_code,s.claim_recd_total,it.cust_item_code "
			+ " 	FROM store_sales_details s " 
			+ " JOIN store_sales_main sm USING (sale_id) "
			+ " JOIN store_item_details it USING (medicine_id) "
			+ " LEFT JOIN LATERAL (select sum(insurance_claim_amt) as insurance_claim_amt,sale_item_id "
			+ " from sales_claim_details scm where scm.sale_item_id = s.sale_item_id "
			+ " group by sale_item_id) scd ON (scd.sale_item_id=s.sale_item_id) "
			+ " JOIN manf_master mm ON (mm.manf_code = it.manf_name) "
			+ " JOIN bill b USING (bill_no) "
			+ " JOIN stores gd ON (gd.dept_id = sm.store_id) "
			+ " LEFT JOIN generic_name gn ON (gn.generic_code = it.generic_name) "
			+ " LEFT JOIN store_type_master stm ON (stm.store_type_id = gd.store_type_id) "
			+ " LEFT JOIN store_category_master scm ON (scm.category_id = it.med_category_id) "
			+ " LEFT JOIN store_item_controltype sic ON (sic.control_type_id = it.control_Type_id) "
			+ " LEFT JOIN bill_charge bc ON (bc.charge_id = sm.charge_id) "
			+ " LEFT JOIN prior_auth_modes pam ON (pam.prior_auth_mode_id = s.prior_auth_mode_id) ";

	public static List<BasicDynaBean> getSaleItemsDetails(String sale_id) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_SALE_ITEM_DETAILS + " WHERE s.sale_id = ? ");
			ps.setString(1, sale_id);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public static List<BasicDynaBean> getBillSaleItemDetails(String billNo) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_SALE_ITEM_DETAILS
					+ " WHERE b.bill_no = ? AND (s.quantity + s.return_qty) > 0 AND bc.status != 'X' ORDER BY sm.sale_id ");
			ps.setString(1, billNo);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public static List<BasicDynaBean> getBillReturnItemDetails(String billNo) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_SALE_ITEM_DETAILS
					+ " WHERE b.bill_no = ? AND s.quantity < 0  AND bc.status != 'X' ORDER BY sm.sale_id ");
			ps.setString(1, billNo);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String GET_WARD_NAME = "SELECT ward_name FROM store_sales_main ssm "
			+ "JOIN ward_names USING(ward_no) where sale_id=?";

	public static String getWardNameForHospiatlPatient(String saleID) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_WARD_NAME);
			ps.setString(1, saleID);
			return DataBaseUtil.getStringValueFromDb(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	/**
	 * Gets primary insurance plan payable status. If multiple insurance
	 * category exists, this gives the payable status depending on the priority
	 * of Insurance Category Master.
	 * 
	 */

	private static final String GET_PRIMARY_INSURANCE_PLAN_PAYABLE_STATUS = "SELECT CASE WHEN ipd.category_payable='Y' THEN 't' ELSE 'f' END as plan_category_payable, ipd.category_payable as pri_cat_payable"
			+ " FROM patient_insurance_plans pip "
			+ " JOIN insurance_plan_details ipd ON (pip.plan_id = ipd.plan_id and pip.priority=1 and ipd.patient_type=?) "
			+ " JOIN store_items_insurance_category_mapping sicm ON (sicm.insurance_category_id = ipd.insurance_category_id and sicm.medicine_id = ?) "
			+ " JOIN item_insurance_categories iic ON(iic.insurance_category_id = sicm.insurance_category_id and iic.insurance_payable='Y') "
			+ " WHERE pip.patient_id = ? ORDER BY iic.priority LIMIT 1";
	
	private static final BasicDynaBean DEFAULT_PAYABLE_STATUS = new DynaBeanBuilder()
			.addPropertyValue("plan_category_payable", 'f').addPropertyValue("pri_cat_payable", 'N').build();

	public static BasicDynaBean getInsuranceCategoryPayableStatus(String visitId, int medicineId, String patientType)
			throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		BasicDynaBean dynaBean = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_PRIMARY_INSURANCE_PLAN_PAYABLE_STATUS);
			ps.setString(1, patientType);
			ps.setInt(2, medicineId);
			ps.setString(3, visitId);
			dynaBean = CommonUtils.getFirstNonNull(DataBaseUtil.queryToDynaBean(ps),DEFAULT_PAYABLE_STATUS);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return dynaBean;
	}

	

	private static final String GET_SALE_STORE_INFO = "SELECT" + "  pmsm.sale_id,pmsm.store_id "
			+ "  FROM store_sales_main pmsm " + "  JOIN stores s ON (s.dept_id = pmsm.store_id) "
			+ "  WHERE sale_id = ? ";

	public static BasicDynaBean getPrescriptionLabelPrintName(String saleID) throws SQLException {
		List l = DataBaseUtil.queryToDynaList(GET_SALE_STORE_INFO, saleID);
		if (l.size() > 0)
			return (BasicDynaBean) l.get(0);
		else
			return null;
	}

	private static final String GET_ITEM_SUBGROUPS = "SELECT * FROM item_sub_groups isg JOIN store_item_sub_groups sisg ON(isg.item_subgroup_id = sisg.item_subgroup_id)"
			+ " JOIN item_groups ig ON (ig.item_group_id = isg.item_group_id) "
			+ " where medicine_id = ? AND isg.status = 'A'";

	public static List<BasicDynaBean> getMedicineSubgroups(int medicineId) throws SQLException {
		List l = DataBaseUtil.queryToDynaList(GET_ITEM_SUBGROUPS, medicineId);
		if (l.size() > 0)
			return l;
		else
			return null;
	}

	private static final String GET_TAX_DETAILS = "SELECT ssd.sale_id,ssd.sale_item_id,ig.item_group_name,isb.item_subgroup_name,sstd.tax_rate,sstd.tax_amt,s.pharmacy_tin_no,hcms.tin_number, "
			+ " ptm.tin_number as pri_tpa_tin_number,stm.tin_number as sec_tpa_tin_number, picm.tin_number as pri_insur_tin_number,sicm.tin_number as sec_insur_tin_number "
			+ " FROM store_sales_details ssd " + " JOIN store_sales_main pssm ON (pssm.sale_id=ssd.sale_id ) "
			+ " JOIN bill b ON (pssm.bill_no=b.bill_no) " + " LEFT JOIN bill_charge bc ON (bc.bill_no = b.bill_no) "
			+ " LEFT JOIN bill_claim pbcl ON (bc.bill_no = pbcl.bill_no AND pbcl.priority = 1) "
			+ " LEFT JOIN bill_claim sbcl ON (bc.bill_no = sbcl.bill_no AND sbcl.priority = 2) "
			+ " LEFT JOIN bill_charge_claim pbccl ON (bc.charge_id = pbccl.charge_id AND pbcl.claim_id = pbccl.claim_id ) "
			+ " LEFT JOIN bill_charge_claim sbccl ON (bc.charge_id = sbccl.charge_id AND sbcl.claim_id = sbccl.claim_id) "
			+ " LEFT JOIN sales_claim_details pscd ON (pscd.sale_item_id = ssd.sale_item_id AND pscd.claim_id = pbcl.claim_id AND pbcl.priority =1) "
			+ " LEFT JOIN sales_claim_details sscd ON (sscd.sale_item_id = ssd.sale_item_id AND sscd.claim_id = sbcl.claim_id AND sbcl.priority =2) "
			+ " JOIN store_sales_main ssm ON (ssm.sale_id=ssd.sale_id AND ssm.charge_id = bc.charge_id) "
			+ " LEFT JOIN stores s ON (s.dept_id=pssm.store_id) "
			+ " LEFT JOIN hospital_center_master hcms ON (hcms.center_id=s.center_id) "
			+ " LEFT JOIN patient_registration pr ON (pr.patient_id = b.visit_id) "
			+ " LEFT JOIN store_sales_tax_details sstd on sstd.sale_item_id = ssd.sale_item_id "
			+ " LEFT JOIN item_sub_groups_tax_details isgtd ON isgtd.item_subgroup_id = sstd.item_subgroup_id "
			+ " LEFT JOIN item_sub_groups isb ON isb.item_subgroup_id = isgtd.item_subgroup_id "
			+ " LEFT JOIN item_groups ig on ig.item_group_id = isb.item_group_id "
			+ " LEFT JOIN tpa_master ptm ON (ptm.tpa_id = pr.primary_sponsor_id) "
			+ " LEFT JOIN tpa_master stm ON (stm.tpa_id = pr.secondary_sponsor_id) "
			+ " LEFT JOIN patient_insurance_plans ppip ON (ppip.patient_id = b.visit_id AND ppip.priority = 1) "
			+ " LEFT JOIN patient_insurance_plans spip ON (spip.patient_id = b.visit_id AND spip.priority = 2) "
			+ " LEFT JOIN insurance_company_master picm ON (picm.insurance_co_id = ppip.insurance_co AND ppip.priority = 1) "
			+ " LEFT JOIN insurance_company_master sicm ON (sicm.insurance_co_id = spip.insurance_co AND spip.priority = 2) "
			+ " WHERE ";

	public static List getSalesTaxDetails(String saleId) throws SQLException {
		List salesList = null;
		Connection c = null;
		PreparedStatement ps = null;
		try {
			c = DataBaseUtil.getReadOnlyConnection();
			ps = c.prepareStatement(GET_TAX_DETAILS + "ssd.sale_id=? ORDER BY ssd.sale_item_id");
			ps.setString(1, saleId);
			salesList = DataBaseUtil.queryToDynaList(ps);
		} finally {
		  if (ps != null) {ps.close();}
		  if (c != null) { c.close();}
		}
		return salesList;
	}

	public static List getSalesReturnTaxDetails(String saleId) throws SQLException {
		List salesList = null;
		Connection c = null;
		PreparedStatement ps = null;
		try {
			c = DataBaseUtil.getReadOnlyConnection();
			ps = c.prepareStatement(GET_TAX_DETAILS + " ssm.return_bill_no=? ORDER BY ssd.sale_item_id");
			ps.setString(1, saleId);
			salesList = DataBaseUtil.queryToDynaList(ps);
		} finally {
		  if (ps != null) {ps.close();}
		  if (c != null) { c.close();}
		}
		return salesList;
	}

	private static final String GET_ITEM_SUBGROUP_CODES = "SELECT sstd.item_subgroup_id, isg.subgroup_code, isg.item_subgroup_name,"
			+ " sstd.tax_amt, sstd.tax_rate, isg.item_group_id, ig.group_code  " + " FROM store_sales_tax_details sstd "
			+ " JOIN item_sub_groups isg ON(sstd.item_subgroup_id = isg.item_subgroup_id) "
			+ " JOIN item_groups ig ON(isg.item_group_id = ig.item_group_id) " + " WHERE sstd.sale_item_id = ? ";

	public static List<BasicDynaBean> getItemSubgroupCodes(String chargeId) throws SQLException {
		List<BasicDynaBean> salesTaxList = null;
		Connection c = null;
		PreparedStatement ps = null;
		if (chargeId.contains("-")) {
			int saleItemId = Integer.parseInt(chargeId.split("\\-")[1]);
			try {
				c = DataBaseUtil.getReadOnlyConnection();
				ps = c.prepareStatement(GET_ITEM_SUBGROUP_CODES);
				ps.setInt(1, saleItemId);
				salesTaxList = DataBaseUtil.queryToDynaList(ps);
			} finally {
			  if (ps != null) {ps.close();}
			  if (c != null) { c.close();}
			}
		}
		return salesTaxList;
	}

	private static final String GET_ITEM_SALES_TAX_DETAILS = " SELECT ssd.sale_id,ssd.sale_item_id, "
			+ " ig.item_group_name, isb.item_subgroup_name, sstd.tax_rate, sstd.tax_amt"
			+ " FROM store_sales_details ssd  " + " JOIN store_sales_main ssm ON (ssm.sale_id=ssd.sale_id ) "
			+ " LEFT JOIN store_sales_tax_details sstd ON (sstd.sale_item_id = ssd.sale_item_id) "
			+ " LEFT JOIN item_sub_groups isb ON (isb.item_subgroup_id = sstd.item_subgroup_id) "
			+ " LEFT JOIN item_groups ig ON (ig.item_group_id = isb.item_group_id) "
			+ " WHERE # ORDER BY ssd.sale_item_id";

	public static List getItemsSaleTaxDetails(String saleId) throws SQLException {
		List salesList = null;
		Connection c = null;
		PreparedStatement ps = null;
		try {
			c = DataBaseUtil.getReadOnlyConnection();
			ps = c.prepareStatement(GET_ITEM_SALES_TAX_DETAILS.replace("#", "ssd.sale_id=? and ssm.type='S' "));
			ps.setString(1, saleId);
			salesList = DataBaseUtil.queryToDynaList(ps);
		} finally {
		  if (ps != null) {ps.close();}
		  if (c != null) { c.close();}
		}
		return salesList;
	}

	public static List getItemsReturnTaxDetails(String saleId) throws SQLException {
		List salesList = null;
		Connection c = null;
		PreparedStatement ps = null;
		try {
			c = DataBaseUtil.getReadOnlyConnection();
			ps = c.prepareStatement(GET_ITEM_SALES_TAX_DETAILS.replace("#", "ssd.sale_id=? and ssm.type='R' "));
			ps.setString(1, saleId);
			salesList = DataBaseUtil.queryToDynaList(ps);
		} finally {
		  if (ps != null) {ps.close();}
		  if (c != null) { c.close();}
		}
		return salesList;
	}

	public static List getItemsSaleTaxDetailsByBillNo(String billNo) throws SQLException {
		List salesList = null;
		Connection c = null;
		PreparedStatement ps = null;
		try {
			c = DataBaseUtil.getReadOnlyConnection();
			ps = c.prepareStatement(GET_ITEM_SALES_TAX_DETAILS.replace("#", "ssm.bill_no=? and ssm.type='S' "));
			ps.setString(1, billNo);
			salesList = DataBaseUtil.queryToDynaList(ps);
		} finally {
		  if (ps != null) {ps.close();}
		  if (c != null) { c.close();}
		}
		return salesList;
	}

	public static List getItemsReturnTaxDetailsByBillNo(String billNo) throws SQLException {
		List salesList = null;
		Connection c = null;
		PreparedStatement ps = null;
		try {
			c = DataBaseUtil.getReadOnlyConnection();
			ps = c.prepareStatement(GET_ITEM_SALES_TAX_DETAILS.replace("#", "ssm.bill_no=? and ssm.type='R' "));
			ps.setString(1, billNo);
			salesList = DataBaseUtil.queryToDynaList(ps);
		} finally {
		  if (ps != null) {ps.close();}
		  if (c != null) { c.close();}
		}
		return salesList;
	}

	private static final String GET_CLAIM_SALES_TAX_DETAILS = " SELECT ssd.sale_id,ssd.sale_item_id, "
			+ " ssd.sale_id,ssd.sale_item_id,pig.item_group_name,sig.item_group_name,pisb.item_subgroup_name,sisb.item_subgroup_name, "
			+ " psctd.tax_rate,psctd.tax_amt,ssctd.tax_rate,ssctd.tax_amt " + " FROM store_sales_details ssd "
			+ " JOIN store_sales_main ssm ON (ssm.sale_id=ssd.sale_id ) " + " JOIN bill b ON (ssm.bill_no=b.bill_no) "
			+ " LEFT JOIN bill_charge bc ON (bc.bill_no = b.bill_no AND bc.charge_id = ssm.charge_id) "
			+ " LEFT JOIN bill_claim pbcl ON (bc.bill_no = pbcl.bill_no AND pbcl.priority = 1) "
			+ " LEFT JOIN bill_claim sbcl ON (bc.bill_no = sbcl.bill_no AND sbcl.priority = 2) "
			+ " LEFT JOIN store_sales_tax_details sstd ON (sstd.sale_item_id = ssd.sale_item_id) "
			+ " LEFT JOIN sales_claim_details pscd ON (pscd.sale_item_id = ssd.sale_item_id  AND pscd.claim_id = pbcl.claim_id AND pbcl.priority =1) "
			+ " LEFT JOIN sales_claim_details sscd ON (sscd.sale_item_id = ssd.sale_item_id  AND sscd.claim_id = pbcl.claim_id AND pbcl.priority =2) "
			+ " LEFT JOIN sales_claim_tax_details psctd ON (psctd.sale_item_id = ssd.sale_item_id  AND psctd.claim_id = pscd.claim_id AND pbcl.priority =1 AND sstd.item_subgroup_id = psctd.item_subgroup_id) "
			+ " LEFT JOIN sales_claim_tax_details ssctd ON (ssctd.sale_item_id = ssd.sale_item_id  AND ssctd.claim_id = sscd.claim_id AND pbcl.priority =2 AND sstd.item_subgroup_id = psctd.item_subgroup_id) "
			+ " LEFT JOIN item_sub_groups pisb ON (pisb.item_subgroup_id = psctd.item_subgroup_id) "
			+ " LEFT JOIN item_sub_groups sisb ON (sisb.item_subgroup_id = ssctd.item_subgroup_id)  "
			+ " LEFT JOIN item_groups pig ON (pig.item_group_id = pisb.item_group_id) "
			+ " LEFT JOIN item_groups sig ON (sig.item_group_id = sisb.item_group_id) "
			+ " WHERE # ORDER BY ssd.sale_item_id";

	public static List getClaimSaleTaxDetails(String saleId) throws SQLException {
		List salesList = null;
		Connection c = null;
		PreparedStatement ps = null;
		try {
			c = DataBaseUtil.getReadOnlyConnection();
			ps = c.prepareStatement(GET_CLAIM_SALES_TAX_DETAILS.replace("#", "ssd.sale_id=? and ssm.type='S' "));
			ps.setString(1, saleId);
			salesList = DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(c, ps);
		}
		return salesList;
	}

	public static List getClaimReturnTaxDetails(String saleId) throws SQLException {
		List salesList = null;
		Connection c = null;
		PreparedStatement ps = null;
		try {
			c = DataBaseUtil.getReadOnlyConnection();
			ps = c.prepareStatement(GET_CLAIM_SALES_TAX_DETAILS.replace("#", "ssd.sale_id=? and ssm.type='R' "));
			ps.setString(1, saleId);
			salesList = DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(c, ps);
		}
		return salesList;
	}

	private static final String GET_SALES_CLAIM_TAX_DETAILS_FOR_VISIT = " SELECT sctd.item_subgroup_id, sum(sctd.tax_amt) as tax_amt, sctd.sale_item_id "
			+ " FROM bill b  " + " JOIN store_sales_main ssm ON (ssm.bill_no = b.bill_no) "
			+ " JOIN bill_charge bc ON (bc.charge_id = ssm.charge_id AND bc.bill_no = b.bill_no) "
			+ " JOIN store_sales_details ssd ON (ssd.sale_id = ssm.sale_id)  "
			+ " JOIN sales_claim_details scd ON (scd.sale_item_id = ssd.sale_item_id) "
			+ " JOIN sales_claim_tax_details sctd ON (sctd.sale_item_id = scd.sale_item_id AND sctd.claim_id = scd.claim_id) "
			+ " WHERE b.visit_id = ? AND sctd.adj_amt = 'Y'" + " GROUP BY sctd.item_subgroup_id,sctd.sale_item_id ";

	public List getSalesClaimTaxDetailsForVisit(String visitId) throws SQLException {
		List salesClaimList = null;
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(GET_SALES_CLAIM_TAX_DETAILS_FOR_VISIT);
			ps.setString(1, visitId);
			salesClaimList = DataBaseUtil.queryToDynaList(ps);
		} finally {
		  if (ps != null) {ps.close();}
		}
		return salesClaimList;
	}

	private static final String GET_SALES_CLAIM_TAX_DETAILS_FOR_SALE = " SELECT sctd.item_subgroup_id, sum(sctd.tax_amt) as tax_amt, sctd.sale_item_id "
			+ " FROM store_sales_main ssm " + " JOIN store_sales_details ssd ON (ssd.sale_id = ssm.sale_id)  "
			+ " JOIN sales_claim_details scd ON (scd.sale_item_id = ssd.sale_item_id)  "
			+ " JOIN sales_claim_tax_details sctd ON (sctd.sale_item_id = ssd.sale_item_id AND sctd.claim_id = scd.claim_id) "
			+ " WHERE ssm.sale_id = ? AND sctd.adj_amt = 'Y' " + " GROUP BY sctd.item_subgroup_id,sctd.sale_item_id ";

	public List getSalesClaimTaxDetailsForSale(String saleId) throws SQLException {
		List salesClaimList = null;
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(GET_SALES_CLAIM_TAX_DETAILS_FOR_SALE);
			ps.setString(1, saleId);
			salesClaimList = DataBaseUtil.queryToDynaList(ps);
		} finally {
		  if (ps != null) {ps.close();}
		}
		return salesClaimList;
	}

	private static final String UPDATE_SALE_TAX_DETAILS = " UPDATE store_sales_tax_details SET return_tax_amt=return_tax_amt-? WHERE sale_item_id=? AND item_subgroup_id=?";

	public boolean updateItemSalesTaxDetails(int saleItemId, Object itemSubgroupId, Object taxAmt) throws SQLException {
		return DataBaseUtil.executeQuery(con, UPDATE_SALE_TAX_DETAILS,
				new Object[] { taxAmt, saleItemId, itemSubgroupId }) > 0;
	}

	private static final String UPDATE_SALE_TAX_DETAILS_AGAIN_SALE = " UPDATE store_sales_tax_details SET return_tax_amt=tax_amt-? WHERE sale_item_id=? AND item_subgroup_id=?";

	public boolean updateItemSalesTaxDetailsForSaleReturn(int saleItemId, Object itemSubgroupId, Object taxAmt)
			throws SQLException {
		return DataBaseUtil.executeQuery(con, UPDATE_SALE_TAX_DETAILS_AGAIN_SALE,
				new Object[] { taxAmt, saleItemId, itemSubgroupId }) > 0;
	}

	private static final String GET_SALES_ITEM_TAX_DETAILS = " SELECT item_subgroup_id as tax_sub_group_id, sstd.tax_rate as rate, (sstd.tax_amt/quantity)*# as amount, (sstd.original_tax_amt/quantity)*# as original_tax_amt "
			+ " FROM store_sales_tax_details sstd "
			+ " JOIN store_sales_details ssd ON (ssd.sale_item_id = sstd.sale_item_id) where sstd.sale_item_id = ?";

	public static List getSalesItemTaxDetailsReturns(int saleItemId, String qty) throws SQLException {
		List salesItemTaxList = null;
		Connection c = null;
		PreparedStatement ps = null;
		try {
			c = DataBaseUtil.getConnection();
			ps = c.prepareStatement(GET_SALES_ITEM_TAX_DETAILS.replaceAll("#", qty));
			ps.setInt(1, saleItemId);
			salesItemTaxList = DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(c, ps);
		}
		return salesItemTaxList;
	}

	private static final String GET_SALES_ITEM_DETAILS = " SELECT ssd.sale_item_id, ssd.medicine_id, (ssd.amount/quantity)*# as sale_amount,"
			+ " (ssd.disc/quantity)*# as discount, (ssd.original_tax_amt/quantity)*# as original_tax_amt, (ssd.tax/quantity)*# as sale_item_tax"
			+ " FROM store_sales_details ssd where ssd.sale_item_id = ?";

	public static BasicDynaBean getSalesItemDetailsReturns(int saleItemId, String qty) throws SQLException {
		BasicDynaBean saleItemBean = null;
		Connection c = null;
		PreparedStatement ps = null;
		try {
			c = DataBaseUtil.getConnection();
			ps = c.prepareStatement(GET_SALES_ITEM_DETAILS.replaceAll("#", qty));
			ps.setInt(1, saleItemId);
			saleItemBean = DataBaseUtil.queryToDynaBean(ps);
		} finally {
			DataBaseUtil.closeConnections(c, ps);
		}
		return saleItemBean;
	}

	public static List getSalesTaxDetails(int saleItemId) throws SQLException {
		return getSalesTaxDetails(saleItemId, -1);
	}

	public static final String GET_SALES_TAX_DETAILS = "SELECT tax_amt,item_subgroup_id,sale_item_id FROM store_sales_tax_details where sale_item_id=?";

	public static List getSalesTaxDetails(int saleItemId, int subGroupId) throws SQLException {
		List salesItemTaxList = null;
		Connection c = null;
		PreparedStatement ps = null;
		try {
			c = DataBaseUtil.getConnection();
			ps = c.prepareStatement(
					subGroupId != -1 ? GET_SALES_TAX_DETAILS + " AND item_subgroup_id = ?" : GET_SALES_TAX_DETAILS);
			ps.setInt(1, saleItemId);
			if (subGroupId != -1)
				ps.setInt(2, subGroupId);
			salesItemTaxList = DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(c, ps);
		}
		return salesItemTaxList;
	}

	private static final String GET_SALE_ITEM_TAX_DETAILS = " SELECT sum(tax_amt) as tax_amt FROM store_sales_tax_details where sale_item_id = ?";

	public BasicDynaBean getSumOfItemTax(int saleItemId) throws SQLException {
		BasicDynaBean salesTax = null;
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(GET_SALE_ITEM_TAX_DETAILS);
			ps.setInt(1, saleItemId);
			salesTax = DataBaseUtil.queryToDynaBean(ps);
		} finally {
			DataBaseUtil.closeConnections(null, ps);
		}
		return salesTax;
	}

	private static final String GET_SALES_TAX_DETAILS_FOR_SALE_ID = " SELECT ssm.charge_id, sstd.item_subgroup_id AS tax_sub_group_id,"
			+ " MAX(sstd.tax_rate) AS tax_rate, "
			+ " CASE WHEN ssm.type='R' THEN -sum(sstd.tax_amt) ELSE sum(sstd.tax_amt) END AS tax_amount, "
			+ " CASE WHEN ssm.type='R' THEN -sum(sstd.original_tax_amt) ELSE sum(sstd.original_tax_amt) END AS original_tax_amt "
			+ " FROM store_sales_main ssm" + " JOIN store_sales_details ssd ON (ssd.sale_id = ssm.sale_id) "
			+ " JOIN store_sales_tax_details sstd ON (sstd.sale_item_id = ssd.sale_item_id) "
			+ " WHERE ssm.sale_id = ? " + " GROUP BY ssm.charge_id,ssm.sale_id,sstd.item_subgroup_id,ssm.type ";

	private static final String GET_SALES_CLAIM_TAX_DETAILS_SALE_ID = " SELECT ssm.charge_id,sctd.claim_id,sctd.item_subgroup_id AS tax_sub_group_id, "
			+ " MAX(sctd.tax_rate) AS tax_rate, "
			+ " CASE WHEN ssm.type='R' THEN -sum(sctd.tax_amt) ELSE sum(sctd.tax_amt) END AS sponsor_tax_amount, 0 AS charge_tax_id "
			+ " FROM store_sales_main ssm " + " JOIN store_sales_details ssd ON (ssd.sale_id = ssm.sale_id)  "
			+ " JOIN sales_claim_details scd ON (scd.sale_item_id = ssd.sale_item_id) "
			+ " JOIN sales_claim_tax_details sctd ON (sctd.sale_item_id = scd.sale_item_id AND sctd.claim_id = scd.claim_id) "
			+ " WHERE ssm.sale_id = ? "
			+ " GROUP BY ssm.charge_id,ssm.sale_id,sctd.claim_id,sctd.item_subgroup_id,ssm.type ";

	public boolean insertOrUpdateBillChargeTaxesForSales(Connection con, String saleId)
			throws SQLException, IOException {
		PreparedStatement ps = null;
		boolean success = true;
		BillChargeTaxDAO bcTaxDao = new BillChargeTaxDAO();
		BillChargeClaimTaxDAO bcclTaxDao = new BillChargeClaimTaxDAO();
		List<BasicDynaBean> salesTaxList = new ArrayList<BasicDynaBean>();
		List<BasicDynaBean> salesClaimTaxList = new ArrayList<BasicDynaBean>();
		List<BasicDynaBean> billChargeTaxesToInsert = new ArrayList<BasicDynaBean>();
		List<BasicDynaBean> billChargeClaimTaxesToInsert = new ArrayList<BasicDynaBean>();
		List<BasicDynaBean> billChargeTaxesToUpdate = new ArrayList<BasicDynaBean>();
		List<BasicDynaBean> billChargeclaimTaxesToUpdate = new ArrayList<BasicDynaBean>();
		try {
			ps = con.prepareStatement(GET_SALES_TAX_DETAILS_FOR_SALE_ID);
			ps.setString(1, saleId);
			salesTaxList = DataBaseUtil.queryToDynaList(ps);
			ps = con.prepareStatement(GET_SALES_CLAIM_TAX_DETAILS_SALE_ID);
			ps.setString(1, saleId);
			salesClaimTaxList = DataBaseUtil.queryToDynaList(ps);

			// insert or update bill_charge_tax entries
			for (BasicDynaBean salesTaxBean : salesTaxList) {
				String chargeId = (String) salesTaxBean.get("charge_id");
				Integer taxSubGroupId = (Integer) salesTaxBean.get("tax_sub_group_id");
				if (!bcTaxDao.isBillChargeTaxExist(con, chargeId, taxSubGroupId)) {
					billChargeTaxesToInsert.add(salesTaxBean);
				} else {
					billChargeTaxesToUpdate.add(salesTaxBean);
				}
			}

			if (!billChargeTaxesToInsert.isEmpty())
				success &= bcTaxDao.insertAll(con, billChargeTaxesToInsert);

			for (BasicDynaBean bean : billChargeTaxesToUpdate) {
				success &= bcTaxDao.updateWithNames(con, bean.getMap(),
						new String[] { "charge_id", "tax_sub_group_id" }) > 0;
			}

			// insert or update bill_charge_claim_tax entries
			for (BasicDynaBean salesClaimTaxBean : salesClaimTaxList) {
				String chargeId = (String) salesClaimTaxBean.get("charge_id");
				String claimId = (String) salesClaimTaxBean.get("claim_id");
				Integer taxSubGroupId = (Integer) salesClaimTaxBean.get("tax_sub_group_id");

				Map<String, Object> chargeTaxMap = new HashMap<String, Object>();
				chargeTaxMap.put("charge_id", chargeId);
				chargeTaxMap.put("tax_sub_group_id", taxSubGroupId);
				BasicDynaBean bcTaxBean = bcTaxDao.findByKey(con, chargeTaxMap);

				int chargeTaxId = 0;
				// List<BasicDynaBean> bcTaxList =
				// bcTaxDao.getBillChargeTax(chargeId, taxSubGroupId);
				if (null != bcTaxBean && null != bcTaxBean.get("charge_tax_id")) {
					chargeTaxId = (Integer) bcTaxBean.get("charge_tax_id");
					salesClaimTaxBean.set("charge_tax_id", chargeTaxId);
					if (!bcclTaxDao.isBillChargeClaimTaxExist(con, chargeId, claimId, taxSubGroupId)) {
						billChargeClaimTaxesToInsert.add(salesClaimTaxBean);
					} else {
						billChargeclaimTaxesToUpdate.add(salesClaimTaxBean);
					}
				}
			}

			if (!billChargeClaimTaxesToInsert.isEmpty())
				success &= bcclTaxDao.insertAll(con, billChargeClaimTaxesToInsert);

			for (BasicDynaBean bean : billChargeclaimTaxesToUpdate) {
				success &= bcclTaxDao.updateWithNames(con, bean.getMap(),
						new String[] { "charge_id", "claim_id", "tax_sub_group_id" }) > 0;
			}
		} finally {
			DataBaseUtil.closeConnections(null, ps);
		}
		return success;
	}

	public static final String GET_BILL_DETAILS = "SELECT b.* FROM bill b where b.bill_no = (select ssm.bill_no FROM store_sales_main ssm where ssm.sale_id = ?)";

	public BasicDynaBean getBillDetails(String saleId) throws SQLException {
		BasicDynaBean billDetails = null;
		Connection c = null;
		PreparedStatement ps = null;
		try {
			c = DataBaseUtil.getConnection();
			ps = c.prepareStatement(GET_BILL_DETAILS);
			ps.setString(1, saleId);
			billDetails = DataBaseUtil.queryToDynaBean(ps);
		} finally {
			DataBaseUtil.closeConnections(c, ps);
		}
		return billDetails;
	}
	
	private static final String GET_SALES_TAX_DETAILS_FOR_VISIT = " SELECT ssm.bill_no, ssd.sale_id, "
	    + " sstd.sale_item_id, sstd.item_subgroup_id, isg.subgroup_code, isg.item_subgroup_name,"
	    + " sstd.tax_amt, sstd.tax_rate, isg.item_group_id, ig.group_code"
	    + " FROM bill b"
	    + " JOIN store_sales_main ssm ON ssm.bill_no = b.bill_no"
	    + " JOIN store_sales_details ssd ON ssd.sale_id = ssm.sale_id"
	    + " JOIN store_sales_tax_details sstd ON sstd.sale_item_id = ssd.sale_item_id"
	    + " JOIN item_sub_groups isg ON(sstd.item_subgroup_id = isg.item_subgroup_id)"
	    + " JOIN item_groups ig ON(isg.item_group_id = ig.item_group_id)"
	    + " WHERE b.visit_id= ?" ;

  public List getSalesTaxDetailsForvisit(String visitId) throws SQLException {
    return DataBaseUtil.queryToDynaList(GET_SALES_TAX_DETAILS_FOR_VISIT, visitId);
  }
  
  private static final String FAMILY_ANNUAL_PHARMACY_UTILIZATION = "SELECT pd.mr_no AS mr_no, pd.patient_name AS patient_name, pd.family_id AS emp_id, "
		  + "	CASE "
		  + "		WHEN SUM(bc.insurance_claim_amount + bc.sponsor_tax_amt) IS NULL THEN "
		  + "			(CASE WHEN pd.custom_field17 IS NULL THEN 0 ELSE pd.custom_field17 END) "
		  + "		ELSE (SUM(bc.insurance_claim_amount + bc.sponsor_tax_amt)+ "
		  + "			(CASE WHEN pd.custom_field17 IS NULL THEN 0 ELSE pd.custom_field17 END)) "
		  + "	END "
		  + "AS utilized_amount "
		  + "FROM patient_details pd "
		  + "JOIN patient_registration pr using(mr_no) "
		  + "JOIN bill bl ON (bl.visit_id = pr.patient_id) "
		  + "LEFT OUTER JOIN "
		  + "( SELECT * FROM bill_charge WHERE charge_head IN('PHMED','PHCMED') AND posted_date >= date_trunc('year', now() - interval '3 months') + interval '3 months' ) AS bc using(bill_no) "
		  + "WHERE pd.family_id = ? "
		  + "AND pr.visit_type='o' "
		  + "AND pd.patient_category_id= 3 "
		  + "GROUP BY (pd.mr_no)";

  public static List getFamilyAnnualPharmacyUtilization(String familyId) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(FAMILY_ANNUAL_PHARMACY_UTILIZATION);
			ps.setString(1, familyId);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}
}
