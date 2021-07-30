package com.insta.hms.billing;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.Registration.PatientInsurancePlanDAO;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.master.DRGCodesMaster.DRGCodesMasterDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DrgUpdateDAO {
	
	static Logger logger = LoggerFactory.getLogger(DRGCalculator.class);
	PatientInsurancePlanDAO pipDao = new PatientInsurancePlanDAO();

	private static final GenericDAO patientInsurancePlansDAO = new GenericDAO("patient_insurance_plans");
	private static final GenericDAO insurancePlanMainDAO = new GenericDAO("insurance_plan_main");
	private static final GenericDAO billChargeDAO = new GenericDAO("bill_charge");
	
	public void updateDRGCode(String visitId, String drgCode) throws SQLException, IOException{
		Connection con = null;
		Boolean success = true;
		try{
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			BasicDynaBean visitBean = new VisitDetailsDAO().findByKey(con, "patient_id", visitId);
			visitBean.set("drg_code", drgCode);
			success = new VisitDetailsDAO().update(con, visitBean.getMap(), "patient_id", visitId) >= 0;
		}finally{
			DataBaseUtil.commitClose(con, success);
		}
		
	}

	public BigDecimal getBaseRate(String visitId) throws SQLException{
		Map<String, Object> keys = new HashMap<String, Object>();
		keys.put("patient_id", visitId);
		keys.put("priority", 1);
		BasicDynaBean visitInsBean = patientInsurancePlansDAO.findByKey(keys);
		int planId = (Integer)visitInsBean.get("plan_id");
		BasicDynaBean planBean = insurancePlanMainDAO.findByKey("plan_id", planId);
		BigDecimal baseRate = null != planBean.get("base_rate") ? (BigDecimal) planBean.get("base_rate") : BigDecimal.ZERO;
		return baseRate;
	}
	
	public BasicDynaBean getDrgCodeBean(String drgCode) throws SQLException{
		BasicDynaBean drgCodeBean = new DRGCodesMasterDAO().findByKey("drg_code", drgCode);
		return drgCodeBean;
	}

	public BigDecimal getRelativeWeight(String drgCode) throws SQLException{
		BasicDynaBean drgCodeBean = new DRGCodesMasterDAO().findByKey("drg_code", drgCode);
		BigDecimal relativeWeight = null != drgCodeBean.get("relative_weight") ? (BigDecimal)drgCodeBean.get("relative_weight") : BigDecimal.ZERO;
		return relativeWeight;
	}
	
	public BigDecimal getAddOnPyamentFactor(String visitId) throws SQLException {
		Map<String, Object> keys = new HashMap<String, Object>();
		keys.put("patient_id", visitId);
		keys.put("priority", 1);
		BasicDynaBean visitInsBean = patientInsurancePlansDAO.findByKey(keys);
		int planId = (Integer) visitInsBean.get("plan_id");
		BasicDynaBean planBean = insurancePlanMainDAO.findByKey("plan_id", planId);
		BigDecimal addOnPaymentFactor = null != planBean.get("add_on_payment_factor") ? (BigDecimal) planBean
				.get("add_on_payment_factor") : new BigDecimal(75);
		return addOnPaymentFactor;
	}
	
	public BigDecimal getHCPCSFactor(String drgCode) throws SQLException {
		BasicDynaBean drgCodeBean = new DRGCodesMasterDAO().findByKey("drg_code", drgCode);
		BigDecimal relativeWeight = null != drgCodeBean.get("hcpcs_portion_per") ? (BigDecimal) drgCodeBean
				.get("hcpcs_portion_per") : BigDecimal.ZERO;
		return relativeWeight;
	}
	
	public void postBillCharge(ChargeDTO charge) throws SQLException, IOException{
		Connection con = null;
		Boolean success = true;
		try{
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			ChargeDAO chargeDAOObj = new ChargeDAO(con);
			charge.setChargeId(chargeDAOObj.getNextChargeId());
			success = chargeDAOObj.insertCharge(charge);
		}finally{
			DataBaseUtil.commitClose(con, success);
		}
	}
	
	private static final String UPDATE_DRG_BILL_HOSPITAL_ITEMS = "UPDATE bill_charge_claim SET insurance_claim_amt = 0, tax_amt = 0.00, include_in_claim_calc = false "+
			" WHERE charge_head not in('BPDRG','OUTDRG','APDRG') AND bill_no = ? ";
	
	private static final String UPDATE_DRG_BILL_PHARMACY_ITEMS = "UPDATE sales_claim_details scl SET insurance_claim_amt = 0, tax_amt = 0.00, "+
	    " ref_insurance_claim_amount=0, include_in_claim_calc = false "+
			" FROM store_sales_details ssd "+
			" JOIN store_sales_main ssm ON(ssd.sale_id = ssm.sale_id) "+
			" JOIN bill_charge bc on(bc.charge_id = ssm.charge_id) "+
			" WHERE scl.sale_item_id = ssd.sale_item_id AND bc.bill_no = ?";

	public Boolean setItemsSponsorAmount(String billNo) throws SQLException{
		Connection con  = null;
		Boolean success = true;
		try{
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			try(PreparedStatement ps = con.prepareStatement(UPDATE_DRG_BILL_HOSPITAL_ITEMS);){
			  ps.setString(1, billNo);
	      success = ps.executeUpdate() >= 0;
			}
			    
			try(PreparedStatement ps1 = con.prepareStatement(UPDATE_DRG_BILL_PHARMACY_ITEMS);) {
			  ps1.setString(1, billNo);
	      success = ps1.executeUpdate() >= 0;
			}
		}finally{
		  DataBaseUtil.commitClose(con, success);

		}
		return success;
		
	}
	
	private static final String GET_ADJUSTMENT_AMT = "SELECT sum(amount) as amount, sum(tax_amt) as tax FROM bill_charge where charge_head not in('BPDRG','OUTDRG','APDRG','ADJDRG') "
			+ " AND status != 'X' AND bill_no = ?";

	public BasicDynaBean getAdjustmentAmt(String billNo)throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		try{
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_ADJUSTMENT_AMT);
			ps.setString(1, billNo);
			return DataBaseUtil.queryToDynaBean(GET_ADJUSTMENT_AMT, new String[] {billNo});
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String GET_HCPCS_ITEMS_TOTAL_AMT = 
			"SELECT SUM(bill_total) as hcpcs_items_total "
			+ "FROM ( "
			+ "SELECT SUM(COALESCE(amount, 0)) bill_total FROM bill_charge bc "
			+ "JOIN store_item_details sit ON (sit.medicine_id::text=bc.act_description_id::text and sit.high_cost_consumable= 'Y') "
			+ "WHERE bc.bill_no = ? AND bc.status != 'X' AND bc.charge_head IN ('INVITE', 'INVRET')"
			+ " UNION ALL "
			+ "SELECT SUM(COALESCE(ssd.amount, 0)) - SUM(COALESCE(ssd.tax, 0)) bill_total FROM store_sales_main ssm "
			+ "JOIN store_sales_details ssd ON (ssd.sale_id = ssm.sale_id)"
			+ "JOIN store_item_details sid ON (sid.medicine_id = ssd.medicine_id AND sid.high_cost_consumable='Y')"
			+ "WHERE ssm.bill_no = ? "
			+ ") as foo";

	public BigDecimal getAddOnPaymentAmt(String billNo) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_HCPCS_ITEMS_TOTAL_AMT);
			ps.setString(1, billNo);
			ps.setString(2, billNo);
			return DataBaseUtil.getBigDecimalValueFromDb(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}
	
	public BigDecimal getHCPCSPaymentAmt(String billNo, String visitId,
			String drgCode) throws SQLException {

		BigDecimal totalHCPCsCost = getAddOnPaymentAmt(billNo);
		BigDecimal hcpcsPayment = BigDecimal.ZERO;
		BigDecimal baseRate = getBaseRate(visitId);
		BigDecimal relativeWeight = getRelativeWeight(drgCode);
		BigDecimal addOnPaymentFactor = getAddOnPyamentFactor(visitId);
		BigDecimal hcpcsFactor = getHCPCSFactor(drgCode);

		BigDecimal drgbasePaymentAmt = baseRate.multiply(relativeWeight);
		BigDecimal accountedCostofHCPCs = drgbasePaymentAmt
				.multiply(hcpcsFactor.divide(new BigDecimal(100)));

		if (totalHCPCsCost != null && totalHCPCsCost.compareTo(accountedCostofHCPCs) > 0)
			hcpcsPayment = addOnPaymentFactor.divide(new BigDecimal(100))
					.multiply(totalHCPCsCost.subtract(accountedCostofHCPCs));

		return hcpcsPayment;

	}
	
	public void postBillChargeClaims(ChargeDTO charge, String visitId,
			String billNo) throws SQLException, IOException{
		Connection con = null;
		Boolean success = true;
		int[] planIds = null;
		List<ChargeDTO> charges = new ArrayList<ChargeDTO>();
		charges.add(charge);
		try{
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			planIds = new PatientInsurancePlanDAO().getPlanIds(visitId);
			success = new BillChargeClaimDAO().insertBillChargeClaims(con, charges, planIds,visitId, billNo);
		}finally{
			DataBaseUtil.commitClose(con, success);
		}
		
		
	}
	
	private static final String LOCK_ITEMS_IN_DRG_BILL = "UPDATE bill_charge SET is_claim_locked = true "
			+ " WHERE charge_head NOT IN('BPDRG','OUTDRG','APDRG') AND bill_no = ?";

	public Boolean lockItemsInDRGBill(String billNo) throws SQLException {
		Connection con = null;
		Boolean success = true;
		try{
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			try(PreparedStatement ps = con.prepareStatement(LOCK_ITEMS_IN_DRG_BILL);){
			  ps.setString(1, billNo);
	      success = ps.executeUpdate() >= 0;
			}
		}finally{
			DataBaseUtil.commitClose(con, success);
		}
		return success;
	}
	
	public Boolean updateAdjustmenEntry(String billNo, String visitId) throws SQLException, IOException{
		Connection con = null;
		Boolean success = false;
		try{
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			Map<String,Object> keys = new HashMap<String, Object>();
			keys.put("bill_no", billNo);
			keys.put("charge_head", "ADJDRG");
			BasicDynaBean adjChargeBean = billChargeDAO.findByKey(keys);
			BasicDynaBean bean =getAdjustmentAmt(billNo);
			BigDecimal adjAmt = BigDecimal.ZERO;
			BigDecimal taxAmt = BigDecimal.ZERO;
			if(null != bean && null != bean.get("amount"))
				adjAmt = (BigDecimal)bean.get("amount");
			if(null != bean && null != bean.get("tax"))
				taxAmt = (BigDecimal)bean.get("tax");			
			adjChargeBean.set("act_rate", null != adjAmt ? adjAmt.negate() : BigDecimal.ZERO);
			adjChargeBean.set("amount", null != adjAmt ? adjAmt.negate() : BigDecimal.ZERO);
			adjChargeBean.set("tax_amt",taxAmt.negate());
			adjChargeBean.set("status", "A");
			success = billChargeDAO.update(con, adjChargeBean.getMap(), keys) >= 0;
		}finally{
			DataBaseUtil.commitClose(con, success);
		}
		 return success;
		
	}

	public void updateDRGBasePayment(String visitId, String billNo, String drgCode, String basePyamentChg)throws SQLException,IOException{
		Connection con = null;
		Boolean success = false;
		try{
			con  = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			BigDecimal baseRate = getBaseRate(visitId);
			
			BasicDynaBean drgCodeBean = getDrgCodeBean(drgCode);
			BigDecimal relativeWeight = null != drgCodeBean.get("relative_weight") ? (BigDecimal)drgCodeBean.get("relative_weight") : BigDecimal.ZERO;
			
			BigDecimal drgBasePayAmt = baseRate.multiply(relativeWeight);
			BasicDynaBean chgBean = billChargeDAO.findByKey("charge_id", basePyamentChg);
			Boolean isDrgCodeChanged = !drgCode.equals(chgBean.get("act_description_id"));
			chgBean.set("act_rate", drgBasePayAmt);
			chgBean.set("amount", drgBasePayAmt);
			chgBean.set("status", "A");
			chgBean.set("act_remarks", "Base Payment.     BaseRate : "+baseRate+"  RelativeWeight : "+relativeWeight);
			chgBean.set("act_rate_plan_item_code", drgCode);
			chgBean.set("act_description_id", drgCode);
			success = billChargeDAO.update(con, chgBean.getMap(), "charge_id", basePyamentChg) >= 0;
			calculateDrgCodeTaxAmt(con, chgBean, isDrgCodeChanged);
		}finally{
			DataBaseUtil.commitClose(con, success);
		}
		
	}
	
	public void updateAddOnPayment(String visitId, String billNo, String drgCode)
			throws SQLException, IOException {
		Connection con = null;
		Boolean success = false;
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);

			BigDecimal hcpcsPyamentAmt = getHCPCSPaymentAmt(billNo, visitId,drgCode);
			if(hcpcsPyamentAmt != null && hcpcsPyamentAmt.compareTo(BigDecimal.ZERO) == 0) {
				success = cancelAddOnPaymentDRGItems(con, billNo);
			} else {
				BasicDynaBean chgBean = getChargeBeanForAddOnPayMent(billNo);
				String addOnPaymentRemarks = "HCPCs Add On Payment.   HCPCs Portion Per : " + getHCPCSFactor(drgCode) + "  Add On Payment Factor Per : "+ getAddOnPyamentFactor(visitId);

				String status = (String)chgBean.get("status");
				
				String itemCode = status.equals("X") ? "98" : (String)chgBean.get("act_rate_plan_item_code");
	      Boolean isDrgCodeChanged = !drgCode.equals(chgBean.get("act_description_id"));
				chgBean.set("act_rate", hcpcsPyamentAmt);
				chgBean.set("amount", hcpcsPyamentAmt);
				chgBean.set("status", "A");
				chgBean.set("act_rate_plan_item_code", itemCode);
				chgBean.set("act_description_id", drgCode);
				chgBean.set("act_remarks", addOnPaymentRemarks);
                success = billChargeDAO.update(con, chgBean.getMap(), "charge_id",
                    chgBean.get("charge_id")) >= 0;
                calculateDrgCodeTaxAmt(con, chgBean, isDrgCodeChanged);
			}

		} finally {
			DataBaseUtil.commitClose(con, success);
		}
	}
	
	public void updateDRGOutlierEntry(String visitId, String billNo, String drgCode)
			throws SQLException, IOException, Exception {
		Connection con = null;
		Boolean success = false;
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);

			BigDecimal outlierAmt = getDrgOutlierTotalAmount(billNo, visitId);
			if(outlierAmt != null && outlierAmt.compareTo(BigDecimal.ZERO) <= 0) {
				success = cancelDRGOutlierAmountEntry(con, billNo);
			} else {
				BasicDynaBean chgBean = getChargeBeanForOutlierAmount(billNo);
				String addOnPaymentRemarks = "DRG Outlier Amount update";
				
				String status = (String)chgBean.get("status");
				
				String itemCode = status.equals("X") ? "99" : (String)chgBean.get("act_rate_plan_item_code");

				Boolean isDrgCodeChanged = !drgCode.equals(chgBean.get("act_description_id"));
				chgBean.set("act_rate", outlierAmt);
				chgBean.set("amount", outlierAmt);
				chgBean.set("status", "A");
				chgBean.set("act_rate_plan_item_code", itemCode);
				chgBean.set("act_remarks", addOnPaymentRemarks);
				chgBean.set("act_description_id", drgCode);
				success = billChargeDAO.update(con,
						chgBean.getMap(), "charge_id", chgBean.get("charge_id")) >= 0;
				calculateDrgCodeTaxAmt(con,chgBean, isDrgCodeChanged);
			}

		} finally {
			DataBaseUtil.commitClose(con, success);
		}
	}
	
	private static final String CANCEL_DRG_ITEMS = "UPDATE bill_charge SET status = 'X', amount=0.00, tax_amt=0.00, act_rate=0.00, act_rate_plan_item_code = ''  "+
			" WHERE charge_head in('BPDRG','ADJDRG','OUTDRG','APDRG','MARDRG') AND bill_no=? ";
	
	private static final String CANCEL_DRG_ITEMS_CLAIM = "UPDATE bill_charge_claim SET insurance_claim_amt = 0.00, tax_amt = 0.00 "+
			" WHERE charge_head in('BPDRG','ADJDRG','OUTDRG','APDRG','MARDRG') AND bill_no=? ";

	public void cancelDRGItems(String billNo) throws SQLException, IOException{
		Connection con = null;
		PreparedStatement ps = null;
		Boolean success = false;
		try{
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			ps = con.prepareStatement(CANCEL_DRG_ITEMS);
			ps.setString(1, billNo);
			success = ps.executeUpdate() > 0;
		}finally{
			if(ps!= null) ps.close();
			DataBaseUtil.commitClose(con, success);
		}	
	}
	
	public void cancelDRGItemsClaims(String billNo)throws SQLException, IOException{
		Connection con = null;
		PreparedStatement ps = null;
		
		Boolean success = false;
		try{
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			ps = con.prepareStatement(CANCEL_DRG_ITEMS_CLAIM);
			ps.setString(1, billNo);
			success = ps.executeUpdate() > 0;
		}finally{
			if(ps!= null) ps.close();
			DataBaseUtil.commitClose(con, success);
		}	
	}
	
	private static final String CANCEL_ADD_ON_PAYMENT_DRG_ITEMS = "UPDATE bill_charge SET status = 'X', amount=0.00, tax_amt=0.00, act_rate=0.00, act_rate_plan_item_code = ''  "
			+ " WHERE charge_head ='APDRG' AND bill_no=? ";

	private static final String CANCEL_ADD_ON_PAYMENT_DRG_ITEMS_CLAIM = "UPDATE bill_charge_claim SET insurance_claim_amt = 0.00 , tax_amt = 0.00"
			+ " WHERE charge_head ='APDRG' AND bill_no=? ";
	
	public Boolean cancelAddOnPaymentDRGItems(Connection con, String billNo) throws SQLException, IOException {
		Boolean success = false;
		try {
			con.setAutoCommit(false);
			try(PreparedStatement ps = con.prepareStatement(CANCEL_ADD_ON_PAYMENT_DRG_ITEMS_CLAIM);){
			  ps.setString(1, billNo);
	      success = ps.executeUpdate() > 0;
			}
			try(PreparedStatement ps1 = con.prepareStatement(CANCEL_ADD_ON_PAYMENT_DRG_ITEMS);){
			  ps1.setString(1, billNo);
	      success &= ps1.executeUpdate() > 0;
			}
		} finally {
		  DataBaseUtil.commitClose(null, success);
		}
		return success;
	}
	
	private static final String CANCEL_DRG_OUTLIER_AMOUNT = "UPDATE bill_charge SET status = 'X', amount=0.00, tax_amt=0.00, act_rate=0.00, act_rate_plan_item_code = ''  "
			+ " WHERE charge_head ='OUTDRG' AND bill_no=? ";

	private static final String CANCEL_DRG_ITEMS_OUTLIER_AMOUNT_CLAIM = "UPDATE bill_charge_claim SET insurance_claim_amt = 0.00, tax_amt = 0.00 "
			+ " WHERE charge_head ='OUTDRG' AND bill_no=? ";
	
	public Boolean cancelDRGOutlierAmountEntry(Connection con, String billNo) throws SQLException, IOException {
		Boolean success = false;
		try {
		  try(PreparedStatement ps = con.prepareStatement(CANCEL_DRG_ITEMS_OUTLIER_AMOUNT_CLAIM);){
		    ps.setString(1, billNo);
	      success = ps.executeUpdate() > 0;
		  }
			try(PreparedStatement ps1 = con.prepareStatement(CANCEL_DRG_OUTLIER_AMOUNT);){
			  ps1.setString(1, billNo);
	      success &= ps1.executeUpdate() > 0;
			}
		} finally {
		  DataBaseUtil.commitClose(null, success);
		}
		return success;
	}
	
	private static final String UNLOCK_ITEMS_IN_DRG_BILL ="UPDATE bill_charge SET is_claim_locked = false WHERE bill_no = ? "+
			" AND charge_head NOT IN('PHCMED','PHMED','PHRET','PHCRET','INVRET')";
	
	private static final String INCLUDE_ITEMS_IN_INSURANCE_CALC = "UPDATE bill_charge_claim SET include_in_claim_calc = true WHERE bill_no = ? "+
			" AND charge_head NOT IN('PHCMED','PHMED','PHRET','PHCRET','INVRET')";
	
	public void unLockItemsInDrgBill(String billNo)throws SQLException, IOException{
		Connection con = null;
		Boolean success = false;
		try{
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			try(PreparedStatement ps = con.prepareStatement(UNLOCK_ITEMS_IN_DRG_BILL);){
			  ps.setString(1, billNo);
	      
	      success = ps.executeUpdate() > 0;
			}
		}finally{
			DataBaseUtil.commitClose(con, success);
		}
	}
	
	public void includeItemsInInsCalc(String billNo)throws SQLException, IOException{
		Connection con = null;
		Boolean success = false;
		try{
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			try(PreparedStatement ps = con.prepareStatement(INCLUDE_ITEMS_IN_INSURANCE_CALC);){
			  ps.setString(1, billNo);
	      success = ps.executeUpdate() > 0;
			}
		}finally{
			DataBaseUtil.commitClose(con, success);
		}
	}
	
	public BasicDynaBean getChargeBeanForAddOnPayMent (String billNo) throws SQLException {
		Map filterMapValues = new HashMap<String, String>();
		List columns = new ArrayList<String>();
		
		filterMapValues.put("bill_no", billNo);
		filterMapValues.put("charge_head", "APDRG");
		
		return billChargeDAO.findByKey(filterMapValues);
		
	}
	
	public BasicDynaBean getChargeBeanForOutlierAmount(String billNo) throws SQLException {
		Map filterMapValues = new HashMap<String, String>();
		List columns = new ArrayList<String>();
		
		filterMapValues.put("bill_no", billNo);
		filterMapValues.put("charge_head", "OUTDRG");
		
		return billChargeDAO.findByKey(filterMapValues);
	}

	private static final String CENTER_HEALTH_AUTH_BASE_RATE_PLAN = " SELECT  base_rate_plan "
			+ " FROM health_authority_preferences hap "
			+ " JOIN health_authority_master ham ON (ham.health_authority=hap.health_authority) "
			+ " JOIN hospital_center_master hcm ON (hcm.health_authority=hap.health_authority) "
			+ " WHERE hcm.center_id=? ";
	
	public String getCenterHealthAuthBaseRatePlan() throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(CENTER_HEALTH_AUTH_BASE_RATE_PLAN);
			ps.setInt(1, RequestContext.getCenterId());
			List<String> orgIdList = DataBaseUtil.queryToStringList(ps);
			String baseOrgId = orgIdList.size() > 0 ? orgIdList.get(0) : null;
			 return (baseOrgId != null && !baseOrgId.equals("")) ? baseOrgId : null;
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}
	
	public String getCenterHealthAuthBaseRatePlan(Connection con) throws SQLException {
    PreparedStatement ps = null;
    
    try {
      ps = con.prepareStatement(CENTER_HEALTH_AUTH_BASE_RATE_PLAN);
      ps.setInt(1, RequestContext.getCenterId());
      List<String> orgIdList = DataBaseUtil.queryToStringList(ps);
      String baseOrgId = orgIdList.size() > 0 ? orgIdList.get(0) : null;
       return (baseOrgId != null && !baseOrgId.equals("")) ? baseOrgId : null;
    } finally {
      DataBaseUtil.closeConnections(null, ps);
    }
  }
	
	public Bill getBillDetails(String billNo) throws SQLException {
		Connection con = null;
		try {
			con = DataBaseUtil.getConnection();
			Bill bill = new BillDAO(con).getBill(billNo);
			return bill;
		} finally {
			DataBaseUtil.closeConnections(con, null);
		}
	}
	
	public static final String GET_ALL_HOSP_CHARGES_FOR_DRG = "SELECT * "
			+ " FROM bill_charge "
			+ " WHERE charge_group NOT IN ('MED','ITE','RET','TAX','DIS','DRG','PDM') AND "
			+ " status != 'X' AND bill_no = ? ";

	public List<BasicDynaBean> getAllHospitalChargesForDRG(String billNo) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_ALL_HOSP_CHARGES_FOR_DRG);
			ps.setString(1, billNo);
			return DataBaseUtil.queryToDynaList(ps);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
	}
	
	public static final String GET_INV_AND_PHARM_TOTAL_AMT = "SELECT SUM(amount+return_amt) as totalinvandpharmamt "
			+ " FROM bill_charge "
			+ " WHERE charge_group IN ('ITE','MED') AND "
			+ " status != 'X' AND bill_no = ? ";
	public BigDecimal getInvAndPharmTotalAmt(String billNo) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_INV_AND_PHARM_TOTAL_AMT);
			ps.setString(1, billNo);
			BasicDynaBean amountBean = DataBaseUtil.queryToDynaBean(ps);
			
			BigDecimal totalInvAndPharmamt = BigDecimal.ZERO;
			if(amountBean != null) {
				totalInvAndPharmamt = (amountBean.get("totalinvandpharmamt") == null) ? BigDecimal.ZERO : (BigDecimal)amountBean.get("totalinvandpharmamt");
			}
			return totalInvAndPharmamt;
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
	}
	
	public BigDecimal getDrgMarginPercentage(String visitId, int priority) throws SQLException {
		Map<String, Object> keys = new HashMap<String, Object>();
		keys.put("patient_id", visitId);
		keys.put("priority", priority);
		BasicDynaBean visitInsBean = patientInsurancePlansDAO.findByKey(keys);
		int planId = (Integer)visitInsBean.get("plan_id");
		BasicDynaBean planBean = insurancePlanMainDAO.findByKey("plan_id", planId);
		BigDecimal marginPer = null != planBean.get("marginal_percent") ? (BigDecimal) planBean.get("marginal_percent") : BigDecimal.ZERO;
		return marginPer;
	}
	
	public BigDecimal getDrgGapAmount(String visitId, int priority) throws SQLException {
		Map<String, Object> keys = new HashMap<String, Object>();
		keys.put("patient_id", visitId);
		keys.put("priority", priority);
		BasicDynaBean visitInsBean = patientInsurancePlansDAO.findByKey(keys);
		int planId = (Integer)visitInsBean.get("plan_id");
		BasicDynaBean planBean = insurancePlanMainDAO.findByKey("plan_id", planId);
		BigDecimal gapAmount = null != planBean.get("gap_amount") ? (BigDecimal) planBean.get("gap_amount") : BigDecimal.ZERO;
		return gapAmount;
	}
	
	private BigDecimal getDrgBasePaymentAmount(String billNo) throws SQLException {
		Map keys = new HashMap();
		keys.put("bill_no", billNo);
		keys.put("charge_head", "BPDRG");
		BasicDynaBean chargeBean = billChargeDAO.findByKey(keys);
		BigDecimal basePaymentAmt = BigDecimal.ZERO;
		if(chargeBean != null) {
			basePaymentAmt = chargeBean.get("amount") == null ? BigDecimal.ZERO : (BigDecimal)chargeBean.get("amount");
		}
		return basePaymentAmt;
	}
	
	public BigDecimal getDrgOutlierTotalAmount(String billNo, String visitId) throws SQLException , IOException , Exception {
		//get center mapped health authority base rate plan
		String baseRateplanId = getCenterHealthAuthBaseRatePlan();
		Bill bill = getBillDetails(billNo);
		BasicDynaBean visitBean = new VisitDetailsDAO().findByKey("patient_id", visitId);
		boolean isInsurance = bill.getIs_tpa();
		String bedType = (String)visitBean.get("bed_type");
		String visitType = (String)visitBean.get("visit_type");
		int[] planIds = pipDao.getPlanIds(visitId);
		// get the list of hospital charges
		logger.info("DRG outlier amount calculation is started for hospital items");
		List<BasicDynaBean> chargeBeansList = getAllHospitalChargesForDRG(billNo);
		BigDecimal totHospItemsAmt = BigDecimal.ZERO;
		for(BasicDynaBean chargeBean : chargeBeansList) {
			String chargeId = (String)chargeBean.get("charge_id");
			String chargeGrp = (String)chargeBean.get("charge_group");
			String chargeHead = (String)chargeBean.get("charge_head");
			String itemId = (String)chargeBean.get("act_description_id");
			BigDecimal actQuantity = (BigDecimal)chargeBean.get("act_quantity");
			BigDecimal itemRate = (BigDecimal)chargeBean.get("act_rate");
			BigDecimal itemDisc = (BigDecimal)chargeBean.get("discount");
			BigDecimal itemAmount = (BigDecimal)chargeBean.get("amount");
			String actUnit = (String)chargeBean.get("act_unit");
			String actDeptId = (String)chargeBean.get("act_department_id");
			Timestamp fromDate = (Timestamp)chargeBean.get("from_date");
			Timestamp toDate = (Timestamp)chargeBean.get("to_date");
			String opId = (String)chargeBean.get("op_id");
			
			boolean hasactivity = chargeBean.get("hasactivity") == null ? false : (Boolean)chargeBean.get("hasactivity");
			String chargeType = chargeBean.get("consultation_type_id")+""; //consultationtype
			
			Map<String, BigDecimal> itemRateMap = null;
			BigDecimal rate = BigDecimal.ZERO;
			BigDecimal discount = BigDecimal.ZERO;
			BigDecimal amount = BigDecimal.ZERO;
			if(baseRateplanId != null) {
				itemRateMap = getItemRates(baseRateplanId, bedType, itemId, chargeId, chargeGrp, chargeHead,
					actQuantity, actDeptId, chargeType, actUnit, fromDate, toDate, opId, 
					 visitId, visitType, isInsurance, planIds, hasactivity, false);
			
				if(itemRateMap == null) {
					itemRateMap = new HashMap<String, BigDecimal>();
					itemRateMap.put("item_rate", itemRate);
					itemRateMap.put("discount", itemDisc);
				}
				//calculate amount
				rate = itemRateMap.get("item_rate") == null ? itemRate : (BigDecimal)itemRateMap.get("item_rate");
				discount = itemRateMap.get("discount") == null ? itemDisc : (BigDecimal)itemRateMap.get("discount");
				amount = rate.multiply(actQuantity).subtract(discount);
			} else {
				rate = itemRate;
				discount = itemDisc;
				amount = itemAmount;
			}
			
			totHospItemsAmt = totHospItemsAmt.add(amount);
			//logger.info("chargeId : "+chargeId+" chargeHead : "+chargeHead+" rate : "+rate+" discount : "+discount+" qty : "+
							//actQuantity+" Amt : "+amount+" desc : "+(String)chargeBean.get("act_description"));
			logger.info(chargeId+"\t"+chargeHead+"\t"+rate+"\t"+discount+"\t"+
					actQuantity+"\t"+amount+"\t"+
					((String)chargeBean.get("act_description")).substring(0, 
							((String)chargeBean.get("act_description")).length() > 30 ? 30 : ((String)chargeBean.get("act_description")).length() ));
		}
		
		BigDecimal invAndPharmTotalAmt = getInvAndPharmTotalAmt(billNo);
		
		BigDecimal totCostBill = totHospItemsAmt.add(invAndPharmTotalAmt);
		
		//GETAdd-on payment
		BigDecimal addOnPayment = BigDecimal.ZERO;
		BasicDynaBean addOnChgBean = getChargeBeanForAddOnPayMent(billNo);
		if(addOnChgBean != null) {
			addOnPayment = addOnChgBean.get("amount") == null ? BigDecimal.ZERO : (BigDecimal)addOnChgBean.get("amount");
		}
		
		BigDecimal marginPer = getDrgMarginPercentage(visitId, 1);
		BigDecimal gapAmount = getDrgGapAmount(visitId, 1);
		
		
		BigDecimal drgbasePaymentAmt = getDrgBasePaymentAmount(billNo);
		
		//BigDecimal outlierRate = totCostBill.subtract((((drgbasePaymentAmt.subtract(gapAmount)).subtract(addOnPayment)).multiply(marginPer)).divide(new BigDecimal(100)));
		
		BigDecimal outlierRate = ((totCostBill.subtract(drgbasePaymentAmt).subtract(addOnPayment).subtract(gapAmount)).multiply(marginPer)).divide(new BigDecimal(100));
		
		// add totalHospItemsAMt + pharamcy_inventory_items amount -> total cost of bill
		
		// get the drg margin perc and gap amount from plan
		
		// get the base payment amount in drg bill --> "BPDRG" DRG Base pay
		
		// (Total cost -DRG Base pay - Gap - AddOnPayment)*Margin  -> X
		
		// If X > 0 then post outlier payment
		
		// outlier payment charge -> rate=X, quantity=1, amount=X
		
		return outlierRate;
	}
	
	private enum ChargeGroup {
		REG, DIA, DOC, BED, ICU, OPE, MED, ITE, OTC, TAX, SNP, DIS, PKG, RET, DIE, DRG, PDM;
	};
	
	public Map<String , BigDecimal> getItemRates(Connection con, String orgId, String bedType, String itemId, String chargeId, String chargeGrp, String chargeHead,
			BigDecimal itemQty, String actDeptId, String chargeType, String actUnit, Timestamp fromDate, Timestamp toDate,String opId, 
			String visitId, String visitType, boolean isInsurance, int[] planIds, boolean hasactivity, boolean firstOfCategory) 
												throws SQLException , IOException , Exception {
		Map<String , BigDecimal> rateMap = new HashMap<String , BigDecimal>();
		ChargeRates chargeRates = new ChargeRates(con, visitId, chargeType, planIds, isInsurance);
		chargeRates.setPatientDetailsMap(visitId);
		
		switch (ChargeGroup.valueOf(chargeGrp)) {
		case REG:
			rateMap = chargeRates.getRegistrationChargeRate(chargeHead, orgId, bedType, itemId);
			break;
		case DOC:
			rateMap = chargeRates.getDoctorCharge(orgId, bedType, itemId, chargeId, itemQty, chargeType, hasactivity);
			break;
		case DIA:
			rateMap = chargeRates.getDiagCharge(orgId, bedType, itemId, itemQty);
			break;
		case SNP:
			rateMap = chargeRates.getServiceCharge(orgId, bedType, itemId, itemQty, actDeptId);
			break;
		case DIE:
			rateMap = chargeRates.getDietaryCharge(orgId, bedType, itemId, itemQty);
			break;
		case OTC:
			if (chargeHead.startsWith("EQ")) {
				rateMap = chargeRates.getEquipmentCharge(orgId, bedType, itemId, chargeHead, chargeId, itemQty, 
					 actUnit, fromDate, toDate, hasactivity);
			} else if (chargeHead.startsWith("MIS")) {
				//chargeRates.getAmountFomBillCharge(con,cdto.getChargeId());  need to keep original charge rates only
			} else {
				rateMap = chargeRates.getOtherCharge(orgId, bedType, itemId, itemQty);
			}
			break;
		case PKG:
			if (chargeHead.equals("PKGPKG")) {
				rateMap = chargeRates.getPackageCharge(orgId, bedType, itemId, itemQty);
			}
			break;
		case BED:
		case ICU:
			rateMap = chargeRates.getBedCharge(orgId, bedType, itemId, itemQty, chargeGrp, chargeHead, chargeId, hasactivity);
			break;
		case OPE:
			if (chargeHead.startsWith("EQ")) {
				rateMap = chargeRates.getEquipmentCharge(orgId, bedType, itemId, chargeHead, chargeId, itemQty, 
						 actUnit, fromDate, toDate, hasactivity);
			} else {
				rateMap = chargeRates.getSurgeryCharge(orgId, bedType, itemId, itemQty, chargeId, chargeGrp, chargeHead, actUnit, fromDate, toDate, opId);
			}
			break;
		default:
			/*
			 * Ignore medicine, inventory, tax, DRG, PDM charges and other misc charges as
			 * they are rate plan independent charges.
			 * so getting value form bill_charge table directly
			 */
			break;

		}
		return rateMap;
	}
	
	public Map<String , BigDecimal> getItemRates(String orgId, String bedType, String itemId, String chargeId, String chargeGrp, String chargeHead,
			BigDecimal itemQty, String actDeptId, String chargeType, String actUnit, Timestamp fromDate, Timestamp toDate,String opId, 
			String visitId, String visitType, boolean isInsurance, int[] planIds, boolean hasactivity, boolean firstOfCategory) 
												throws SQLException , IOException , Exception {
		Connection con = null;
		try {
			con = DataBaseUtil.getConnection();
			return getItemRates(con, orgId, bedType, itemId, chargeId, chargeGrp, chargeHead, itemQty, actDeptId, chargeType, actUnit, fromDate, toDate, 
					opId, visitId, visitType, isInsurance, planIds, hasactivity, false) ;
		} finally {
			DataBaseUtil.closeConnections(con, null);
		}
	}
	
	private static final String UNLOCK_DRG_ITEMS_IN_BILL = "UPDATE bill_charge bc SET is_claim_locked = false "+
			" WHERE bc.bill_no = ? AND bc.charge_head in('BPDRG','OUTDRG','APDRG')";

	public Boolean unlockDRGItems(String billNo) throws SQLException{
		Connection con = null;
		Boolean success = false;
		try{
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			try(PreparedStatement ps = con.prepareStatement(UNLOCK_DRG_ITEMS_IN_BILL);){
			  ps.setString(1, billNo);
	      success = ps.executeUpdate() >= 0;
			}
		}finally{
			DataBaseUtil.commitClose(con, success);
		}
		return success;
		
	}
	
	private static final String DRG_CHARGES_COUNT = "SELECT COUNT(*) FROM bill_charge bc WHERE bc.bill_no = ? "+
			" AND bc.charge_head in('BPDRG','OUTDRG','APDRG') AND bc.status = 'A' ";

	public Boolean isDrgCodeExists(String billNo) throws SQLException{
		Connection con = null;
		Boolean success = false;
		try{
			con = DataBaseUtil.getReadOnlyConnection();
			try(PreparedStatement ps = con.prepareStatement(DRG_CHARGES_COUNT);){
			  ps.setString(1, billNo);
	      success = DataBaseUtil.getIntValueFromDb(ps) > 0;
			}
		}finally{
		  DataBaseUtil.closeConnections(con, null);
		}
		return success;
	}
	
	private static final String UNLOCK_SALE_ITEMS = "UPDATE store_sales_details ssd SET is_claim_locked = ? "+
			 " FROM store_sales_main ssm "+
			 " WHERE ssm.sale_id = ssd.sale_id AND ssm.bill_no = ? ";

	public Boolean lockOrUnlockSaleItems(String billNo, Boolean lockItem) throws SQLException{
		Connection con = null;
		Boolean success = false;
		try{
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			try(PreparedStatement ps = con.prepareStatement(UNLOCK_SALE_ITEMS);){
			  ps.setBoolean(1, lockItem);
	      ps.setString(2, billNo);
	      success = ps.executeUpdate() >= 0;
			}
		}finally{
			DataBaseUtil.commitClose(con, success);
		}
		return success;
	}
	
	private static final String INCLUDE_SALE_ITEMS_IN_CLAIM = "UPDATE sales_claim_details scd SET include_in_claim_calc = true "+
			" FROM store_sales_details ssd "+
			" JOIN store_sales_main ssm ON (ssd.sale_id = ssm.sale_id) "+
			" WHERE ssd.sale_item_id = scd.sale_item_id AND ssm.bill_no = ? ";

	public Boolean includeSaleItemsInClaim(String billNo) throws SQLException {
		Connection con = null;
		Boolean success = false;
		try{
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			try(PreparedStatement ps = con.prepareStatement(INCLUDE_SALE_ITEMS_IN_CLAIM);){
			  ps.setString(1, billNo);
	      success = ps.executeUpdate() >= 0;
			}
		}finally{
			DataBaseUtil.commitClose(con, success);
		}
		return success;
	}
	
	private static final String GET_DRG_BILL_NO = "SELECT b.bill_no "+
			 " FROM bill b "+
			 " JOIN bill_charge bc ON (b.bill_no = bc.bill_no AND bc.charge_head = 'BPDRG') "+
			 " WHERE bc.status != 'X' AND b.status='A' AND b.is_primary_bill = 'Y' AND b.visit_id = ? LIMIT 1";
	
	public String getDRGBillNo(String visitId)throws SQLException{
		Connection con = null;
		try{
			con = DataBaseUtil.getReadOnlyConnection();
			try(PreparedStatement ps = con.prepareStatement(GET_DRG_BILL_NO);){
			  ps.setString(1, visitId);
	      return DataBaseUtil.getStringValueFromDb(ps);
			}
		}finally{
		  DataBaseUtil.closeConnections(con, null);
		}
	}
	
	private static final String IS_DRG_MARGIN_EXISTS = "SELECT * FROM bill b JOIN bill_charge bc ON(b.bill_no = bc.bill_no AND bc.charge_head = 'MARDRG') "+
			" WHERE bc.status != 'X' AND b.is_primary_bill = 'Y' AND b.bill_no = ? ";
	
	public Boolean isMarginDRGExists(String billNo)throws SQLException{
		Connection con = null;
		PreparedStatement ps = null;
		try{
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(IS_DRG_MARGIN_EXISTS);
			ps.setString(1, billNo);
			BasicDynaBean drgMarginBean = DataBaseUtil.queryToDynaBean(ps);
			if(null != drgMarginBean) return true;
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
		return false;
	}
	
	private static final String GET_DRG_MARGIN_BILL_IN_VISIT = "SELECT * FROM bill b JOIN bill_charge bc ON(b.bill_no = bc.bill_no AND bc.charge_head = 'MARDRG') "+
			" WHERE bc.status != 'X' AND b.is_primary_bill = 'Y' AND b.visit_id = ? ";

	public String getMarginDRGBillInVisit(String visitId)throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		try{
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_DRG_MARGIN_BILL_IN_VISIT);
			ps.setString(1, visitId);
			return DataBaseUtil.getStringValueFromDb(ps);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
	}
	
	private void calculateDrgCodeTaxAmt(Connection con,BasicDynaBean charge, Boolean isDrgCodeChanged) throws SQLException, IOException {
		new BillChargeTaxDAO().updateBillChargeTaxes(con, charge, isDrgCodeChanged);		
	}

	private static final String REMOVE_DRG_OBSERVATIONS = " DELETE FROM mrd_observations "
	    + " WHERE charge_id IN(SELECT charge_id FROM bill_charge WHERE bill_no = ? AND charge_group='DRG')";
	
  public void removeDRGObservations(String billNo) throws SQLException{
    Connection con = null;
    PreparedStatement ps = null;
    Boolean success = false;
    try{
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      ps = con.prepareStatement(REMOVE_DRG_OBSERVATIONS);
      ps.setString(1, billNo);
      success = ps.executeUpdate() > 0;
    }finally{
      if(ps != null) ps.close();
      DataBaseUtil.commitClose(con, success);
    }
  }
}
