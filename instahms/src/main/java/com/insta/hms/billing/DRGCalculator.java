package com.insta.hms.billing;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.insta.hms.Registration.PatientInsurancePlanDAO;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.medicalrecorddepartment.MRDUpdateScreenBO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DRGCalculator {
	static Logger logger = LoggerFactory.getLogger(DRGCalculator.class);
	DrgUpdateDAO drgDAO = new DrgUpdateDAO();
	PatientInsurancePlanDAO pipDao = new PatientInsurancePlanDAO();
	
	public boolean addDRG(String billNo, String drgCode) throws SQLException, IOException, ParseException, Exception{
		
		String visitId = BillDAO.getVisitId(billNo);
		
		// save the drg code in patient registration table
		drgDAO.updateDRGCode(visitId, drgCode);
		
		
		//Map drgCodeMap = ChargeDAO.getBillDRGCode(billNo);
		
		Map drgCodeMap = new MRDUpdateScreenBO().getDRGCode(visitId);
		
		if (null != drgCodeMap && null != drgCodeMap.get("drg_charge_id")){
			drgDAO.updateDRGBasePayment(visitId, billNo,drgCode,(String)drgCodeMap.get("drg_charge_id"));
			drgDAO.updateAdjustmenEntry(billNo, visitId);
			
			BasicDynaBean chgBean = drgDAO.getChargeBeanForAddOnPayMent(billNo);
			if (null != chgBean)
				drgDAO.updateAddOnPayment(visitId, billNo, drgCode);
			else
				postAddOnPaymentEntry(billNo, visitId, drgCode);
			
			BasicDynaBean outlierChgBean = drgDAO.getChargeBeanForOutlierAmount(billNo);
			if (null != outlierChgBean)
				drgDAO.updateDRGOutlierEntry(visitId, billNo, drgCode);
			else
				postDRGOutlierEntry(billNo, visitId, drgCode);
			
		} else {
			// DRG base payment should be posted
			postDRGBasePayment(visitId,billNo,drgCode);
			// Post an adjustment for bill totals
			postAdjustmenEntry(billNo,visitId);
			//Post add-on payment  applicable
			postAddOnPaymentEntry(billNo, visitId, drgCode);
			//post outlier amount
			postDRGOutlierEntry(billNo, visitId, drgCode);
		}
			
		// Item sponsor amount set to zero
		drgDAO.setItemsSponsorAmount(billNo);
		
		drgDAO.lockItemsInDRGBill(billNo);
		
		// process DRG
		// Post outlier payment if applicable
		
		// Post add-on payment if applicable
		
		// Apply DRG Copay, DRG deductible, Sponsor Limit 
		// (we don't know if there is a max-copay in these cases)
		
		// Connect Insurance for a DRG visit
		// Code is already applied at visit level, but the bill is not connected to insurance so far. But connected now.
		// This is equivalent of adding DRG
		
		
		
		/* As discussed will create a new insurance category for DRG items. Will map this category to DRG charge head. 
		 * While posting drg items will save drg charge head mapped insurance category id. 
		 * make sure in all billing master this drg category should not come. don't allow user to map other items to this category.
		 * will hard code this category in charge head master for DRG charge heads.
		 * 
		 * DRG copay percentage can be defined at DRG category level. 
		 * 
		 * */
	
		return false;
	}

	private void postAdjustmenEntry(String billNo, String visitId) throws SQLException,ParseException,IOException {
		
		BasicDynaBean bean =drgDAO.getAdjustmentAmt(billNo);
		BigDecimal adjAmt = BigDecimal.ZERO;
		BigDecimal taxAmt = BigDecimal.ZERO;
		if(null != bean && null != bean.get("amount"))
			adjAmt = (BigDecimal)bean.get("amount");
		if(null != bean && null != bean.get("tax"))
			taxAmt = (BigDecimal)bean.get("tax");
		BasicDynaBean visitBean = new VisitDetailsDAO().findByKey("patient_id", visitId);
		
		int planIds[] = new PatientInsurancePlanDAO().getPlanIds(visitId);
		
		ChargeDTO adjCharge = new ChargeDTO("DRG", "ADJDRG",
				adjAmt.negate(), BigDecimal.ONE, BigDecimal.ZERO,
				null, null, "DRG Adjustment", null, true,
				planIds[0], 0, -1, (String)visitBean.get("visit_type"), visitId, true);
		adjCharge.setBillNo(billNo);
		adjCharge.setInsuranceAmt(planIds, (String)visitBean.get("visit_type"), true);
		adjCharge.setPostedDate(getPostedDateForDRGCharge(visitBean));
		adjCharge.setTaxAmt(taxAmt.negate());
		BigDecimal claimAmounts[] = adjCharge.getClaimAmounts();
		claimAmounts[0] = BigDecimal.ZERO;
		if(planIds.length > 1) claimAmounts[1] = BigDecimal.ZERO;
		adjCharge.setClaimAmounts(claimAmounts);
		drgDAO.postBillCharge(adjCharge);
		drgDAO.postBillChargeClaims(adjCharge,visitId, billNo);
	}
	
	private void postDRGBasePayment(String visitId, String billNo,
			String drgCode)throws ParseException, SQLException, IOException {
		
		BigDecimal baseRate = drgDAO.getBaseRate(visitId);
		
		BasicDynaBean drgCodeBean = drgDAO.getDrgCodeBean(drgCode);
		BigDecimal relativeWeight = null != drgCodeBean.get("relative_weight") ? (BigDecimal)drgCodeBean.get("relative_weight") : BigDecimal.ZERO;
		
		String codeType = null!= drgCodeBean.get("code_type") ? (String)drgCodeBean.get("code_type") : "";
		BigDecimal drgbasePaymentAmt = baseRate.multiply(relativeWeight);
		
		BasicDynaBean visitBean = new VisitDetailsDAO().findByKey("patient_id", visitId);
		
		int planIds[] = new PatientInsurancePlanDAO().getPlanIds(visitId);
		
		ChargeDTO drgBasePaymentCharge = new ChargeDTO("DRG", "BPDRG",
				drgbasePaymentAmt, BigDecimal.ONE, BigDecimal.ZERO,
				null, drgCode, "DRG Base Payment", null, true,
				planIds[0], 0, -2, (String)visitBean.get("visit_type"), visitId, true);
		String drgRemarks = "Base Payment.     BaseRate : " +baseRate+"  RelativeWeight : "+relativeWeight;
	
		drgBasePaymentCharge.setBillNo(billNo);
		drgBasePaymentCharge.setActRemarks(drgRemarks);
		drgBasePaymentCharge.setPostedDate(getPostedDateForDRGCharge(visitBean));

		drgBasePaymentCharge.setActRatePlanItemCode(drgCode);
		drgBasePaymentCharge.setCodeType(codeType);
		
		drgBasePaymentCharge.setInsuranceAmt(planIds, (String)visitBean.get("visit_type"), true);
		
		drgDAO.postBillCharge(drgBasePaymentCharge);
		drgDAO.postBillChargeClaims(drgBasePaymentCharge,visitId, billNo);
		calculateDrgCodeTaxAmt(drgBasePaymentCharge);
	}
	
	private void calculateDrgCodeTaxAmt(ChargeDTO charge) throws SQLException, IOException {
		Connection con = null;
		boolean success = false;
		try{		
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			Map<String, List<BasicDynaBean>> billChargeTaxBeanMap = new HashMap<String, List<BasicDynaBean>>();	
			List<BasicDynaBean> billChargeTaxBeans = new ArrayList<BasicDynaBean>();					
			billChargeTaxBeans = new BillChargeTaxBO().getBillChargeTaxBeans(con, charge);			
			billChargeTaxBeanMap.put(charge.getChargeId(), billChargeTaxBeans);	
			success = new BillChargeTaxDAO().insertBillChargeTaxes(con, billChargeTaxBeanMap);
		}finally{
			DataBaseUtil.commitClose(con, success);
		}		
	}
	
	public void postDRGOutlierEntry(String billNo, String visitId, String drgCode) throws SQLException , IOException , Exception {
		
		BasicDynaBean visitBean = new VisitDetailsDAO().findByKey("patient_id", visitId);
		String visitType = (String)visitBean.get("visit_type");
		int[] planIds = pipDao.getPlanIds(visitId);
		
		BigDecimal outlierRate = drgDAO.getDrgOutlierTotalAmount(billNo, visitId);
		
		//BasicDynaBean drgCodeBean = drgDAO.getDrgCodeBean(drgCode);
		//String codeType = null!= drgCodeBean.get("code_type") ? (String)drgCodeBean.get("code_type") : "";
		
		//post the outlier charge 
		if(outlierRate.compareTo(BigDecimal.ZERO) > 0) {
			ChargeDTO drgOutlierCharge = new ChargeDTO("DRG", "OUTDRG",
					outlierRate, BigDecimal.ONE, BigDecimal.ZERO,
					null, drgCode, "DRG Outlier Amount", null, true,
					planIds[0], 0, -2, visitType, visitId, true);
			
			String drgRemarks = "DRG Outlier Amount : "+outlierRate;
			drgOutlierCharge.setBillNo(billNo);
			drgOutlierCharge.setActRemarks(drgRemarks);
			drgOutlierCharge.setPostedDate(getPostedDateForDRGCharge(visitBean));
	
			drgOutlierCharge.setActRatePlanItemCode("99");
			drgOutlierCharge.setCodeType("Service Code");
			drgOutlierCharge.setInsuranceAmt(planIds, (String)visitBean.get("visit_type"), true);
			
			drgDAO.postBillCharge(drgOutlierCharge);
			drgDAO.postBillChargeClaims(drgOutlierCharge,visitId, billNo);
			calculateDrgCodeTaxAmt(drgOutlierCharge); 
		}
	}
	
	private void postAddOnPaymentEntry(String billNo, String visitId, String drgCode) throws SQLException,IOException, ParseException {
		
		BasicDynaBean visitBean = new VisitDetailsDAO().findByKey("patient_id", visitId);
		BigDecimal hcpcsPayment = drgDAO.getHCPCSPaymentAmt(billNo, visitId, drgCode);
		
		BasicDynaBean drgCodeBean = drgDAO.getDrgCodeBean(drgCode);
		String codeType = null!= drgCodeBean.get("code_type") ? (String)drgCodeBean.get("code_type") : "";
		String addOnPaymentRemarks = "HCPCs Add On Payment.   HCPCs Portion Per : " + drgDAO.getHCPCSFactor(drgCode) + "  Add On Payment Factor Per : "+ drgDAO.getAddOnPyamentFactor(visitId);


		if (hcpcsPayment.compareTo(BigDecimal.ZERO) > 0) {
			
			int planIds[] = new PatientInsurancePlanDAO().getPlanIds(visitId);
		
			ChargeDTO addOnPaymentCharge = new ChargeDTO("DRG", "APDRG",
					hcpcsPayment, BigDecimal.ONE, BigDecimal.ZERO,
					null, drgCode, "DRG Add On Payment", null, true,
					planIds[0], 0, -2, (String)visitBean.get("visit_type"), visitId, true);
			addOnPaymentCharge.setBillNo(billNo);
			addOnPaymentCharge.setInsuranceAmt(planIds, (String)visitBean.get("visit_type"), true);
			addOnPaymentCharge.setPostedDate(getPostedDateForDRGCharge(visitBean));
			addOnPaymentCharge.setActRatePlanItemCode("98");
			addOnPaymentCharge.setCodeType("Service Code");
			addOnPaymentCharge.setActRemarks(addOnPaymentRemarks);

			
			BigDecimal claimAmounts[] = addOnPaymentCharge.getClaimAmounts();
			claimAmounts[0] = BigDecimal.ZERO;
			if(planIds.length > 1) claimAmounts[1] = BigDecimal.ZERO;
			addOnPaymentCharge.setClaimAmounts(claimAmounts);
			
			drgDAO.postBillCharge(addOnPaymentCharge);
			drgDAO.postBillChargeClaims(addOnPaymentCharge,visitId, billNo);
			calculateDrgCodeTaxAmt(addOnPaymentCharge);
		}
	}
	
	private static java.sql.Timestamp getPostedDateForDRGCharge(BasicDynaBean visitBean) throws ParseException {

		java.sql.Timestamp now = DateUtil.getCurrentTimestamp();

			String dischargeDate = visitBean.get("discharge_date") == null ?
						null : visitBean.get("discharge_date").toString();
			String dischargeTime = visitBean.get("discharge_time") == null ?
						null : visitBean.get("discharge_time").toString();
			if(dischargeDate == null || dischargeTime == null ||
					dischargeDate.equals("") || dischargeTime.equals(""))
				return now;
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			java.util.Date parsedDate = (java.util.Date) dateFormat.parse(dischargeDate
													+ " " + dischargeTime);

			java.sql.Timestamp dischDatetime = new java.sql.Timestamp(parsedDate.getTime());

			if(now.after(dischDatetime))
				return dischDatetime;
			else
				return now;
	}
	
	public Boolean processDRG(String billNo, String drgCode)throws SQLException, IOException {
		
		Boolean success = false;
		String visitId = BillDAO.getVisitId(billNo);
		
		//If bill status is not open, then don't process DRG calculation.
 		BasicDynaBean billBean = new GenericDAO("bill").findByKey("bill_no", billNo);
 		if(!((String)billBean.get("status")).equals("A")) return true;		
		// Item sponsor amount set to zero
		success = drgDAO.setItemsSponsorAmount(billNo);
		
		success = drgDAO.lockItemsInDRGBill(billNo);
		
		// DRG Base Payment changes after changing Insurance Details
		Map drgCodeMap = new MRDUpdateScreenBO().getDRGCode(visitId);		
		drgDAO.updateDRGBasePayment(visitId, billNo,drgCode,(String)drgCodeMap.get("drg_charge_id"));
				
		// Post an adjustment for bill totals
		success = drgDAO.updateAdjustmenEntry(billNo,visitId);
		
		BasicDynaBean chgBean = drgDAO.getChargeBeanForAddOnPayMent(billNo);
		if (null != chgBean)
			drgDAO.updateAddOnPayment(visitId, billNo, drgCode);
		else
			try {
				postAddOnPaymentEntry(billNo, visitId, drgCode);
			} catch (ParseException e) {
			  logger.error("Exception while calculating DRG Add on payment ");
	      logger.error(e.getMessage());
			}
		
		try {
		BasicDynaBean outlierChgBean = drgDAO.getChargeBeanForOutlierAmount(billNo);
		if (null != outlierChgBean)
			drgDAO.updateDRGOutlierEntry(visitId, billNo, drgCode);
		else
			postDRGOutlierEntry(billNo, visitId, drgCode);
		} catch (Exception e) {
			logger.error("Exception while calculating DRG outlier amount ");
			logger.error(e.getMessage());
		}
		
		return success;
	}

	public void removeDRG(String billNo) throws SQLException,IOException{
		String visitId = BillDAO.getVisitId(billNo);
		drgDAO.updateDRGCode(visitId, "");
		drgDAO.removeDRGObservations(billNo);
		drgDAO.cancelDRGItemsClaims(billNo);
		drgDAO.cancelDRGItems(billNo);
		drgDAO.includeItemsInInsCalc(billNo);
		drgDAO.unLockItemsInDrgBill(billNo);
	}
	
	/*Remove DRG happens from edit insurance screen, codification screen.
	 * Update DRG happens from bill, order, edit insurance, connect/disconnect insurance
	 * 
	 * 
	 */

	
	

}
