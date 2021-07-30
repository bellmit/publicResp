package com.insta.hms.billing;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.Registration.PatientInsurancePlanDAO;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.editvisitdetails.EditVisitDetailsDAO;
import com.insta.hms.insurance.SponsorDAO;
import com.insta.hms.stores.MedicineSalesDAO;
import com.insta.hms.stores.MedicineSalesDTO;
import com.insta.hms.stores.SalesClaimDetailsDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class BillChargeClaimDAO extends GenericDAO {

	static Logger logger = LoggerFactory.getLogger(BillDAO.class);

	PatientInsurancePlanDAO insPlanDAO = new PatientInsurancePlanDAO();
	BillDAO billDAO = new BillDAO();
	GenericDAO billClaimDAO = new GenericDAO("bill_claim");
	BillChargeClaimTaxDAO billChargeClaimTaxDAO = new BillChargeClaimTaxDAO();
	ClaimDAO claimDAO = new ClaimDAO();
	BillingHelper billingHelper = new BillingHelper();
  SalesClaimDetailsDAO salesClaimDetailsDAO = new SalesClaimDetailsDAO();
    
    private static final GenericDAO billChargeClaimDAO = new GenericDAO("bill_charge_claim");
    private static final GenericDAO storeClaimDetails = new GenericDAO("sales_claim_details");

	public BillChargeClaimDAO() {
		super("bill_charge_claim");
	}

	public boolean insertBillChargeClaims(Connection con,List<ChargeDTO> charges,int[] planIds,
			String visitId,String billNo)throws SQLException,IOException {
		boolean success = false;

		try{
		if(null != planIds) {
			for(int i=0; i<planIds.length; i++) {
				if(planIds[i] != 0) {
					BasicDynaBean billbean = new GenericDAO("bill").findByKey(con, "bill_no", billNo);
					Integer accGroup = 0;
					accGroup = ( null != billbean ) ?
								((null != billbean.get("account_group")) ? (Integer)billbean.get("account_group") : 0) : 0;
					boolean visitClaimExists = isVisitClaimExists(con,planIds[i],visitId,accGroup);

					String claimId = null;
					if(visitClaimExists) {

						claimId = getVisitClaimId(con,planIds[i],visitId, accGroup);
					} else {
						int centerId = VisitDetailsDAO.getCenterId(con, visitId);
						claimId = claimDAO.getGeneratedClaimIdBasedonCenterId(centerId, accGroup);
						insertInsuranceClaimId(con,visitId,planIds[i],claimId,billNo);
					}

					boolean billClaimExist = isBillClaimExist(con,planIds[i], visitId, billNo);
					if(!billClaimExist){
						Map<String,Object>keys = new HashMap<String, Object>();
						keys.put("patient_id", visitId);
						keys.put("plan_id", planIds[i]);
						BasicDynaBean planBean = insPlanDAO.findByKey(con,keys);
						int priority = (Integer) planBean.get("priority");
						String sponsorId = insPlanDAO.getSponsorId(con,visitId,planIds[i]);
						BasicDynaBean billClaimBean = billClaimDAO.getBean();
						billClaimBean.set("visit_id", visitId);
						billClaimBean.set("bill_no", billNo);
						billClaimBean.set("plan_id", planIds[i]);
						billClaimBean.set("sponsor_id",sponsorId);
						billClaimBean.set("claim_id", claimId);
						billClaimBean.set("priority", priority);
						success = billClaimDAO.insert(con, billClaimBean);
					}else{
						claimId = getClaimId(con, planIds[i], billNo, visitId);
					}
					if(null != charges) {
						for(ChargeDTO charge : charges){
							BasicDynaBean bean = getBean();
							boolean billChgClaimExists = isBillChargeClaimExists(con, billNo, charge.getChargeId(), claimId);
							if(!billChgClaimExists) {
								String sponsorId = getSponsorIdFrombillClaim(con,visitId, billNo,planIds[i]);
								bean.set("claim_id", claimId);
								bean.set("charge_id", charge.getChargeId());
								bean.set("bill_no", charge.getBillNo());
								bean.set("sponsor_id", sponsorId);
								bean.set("approval_id", charge.getApprovalId());
								BigDecimal[] claimAmounts = charge.getClaimAmounts();
								BigDecimal[] claimTaxAmounts = charge.getSponsorTaxAmounts();
								String[] preAuthIds = charge.getPreAuthIds();
								Integer[] preAuthModeIds = charge.getPreAuthModeIds();
								String[] inclInClaimCalc = charge.getIncludeInClaimCalc();
								
								if(null != inclInClaimCalc && null != inclInClaimCalc[i])
									bean.set("include_in_claim_calc", inclInClaimCalc[i].equals("Y"));
								
								if(claimAmounts != null && claimAmounts[i] != null) {
								  bean.set("insurance_claim_amt", claimAmounts[i]);
								} else {
								  bean.set("insurance_claim_amt", BigDecimal.ZERO);
								}
								if(claimTaxAmounts != null && claimTaxAmounts[i] != null)
									bean.set("tax_amt", claimTaxAmounts[i]);
								bean.set("claim_recd_total", charge.getClaimRecdAmount());
								if(null != preAuthIds && planIds.length < 3)
									bean.set("prior_auth_id", preAuthIds[i]);
								if(null != preAuthModeIds && planIds.length < 3)
									bean.set("prior_auth_mode_id", preAuthModeIds[i]);
								bean.set("code_type", charge.getCodeType());
								bean.set("charge_excluded", charge.getChargeExcluded());
								
								String chargeGroup = charge.getChargeGroup();
								if(!chargeGroup.equals("MED") && !chargeGroup.equals("RET")){
								  billingHelper.checkForInsCatInRedis(con,charge,planIds[i]);
								}
								
								bean.set("insurance_category_id", charge.getInsuranceCategoryId());
								bean.set("first_of_category", charge.getFirstOfCategory());
								bean.set("amount_included", charge.getAmount_included());
								success = insert(con, bean);
							}
						}
					}
				}
			}
		}
		}catch(Exception e ){
			logger.error(e.getMessage(), e);
		}
		return success;
	}

	private static final String GET_SPONSOR_ID_FROM_BILL_CLAIM = "SELECT sponsor_id FROM bill_claim WHERE visit_id = ? AND " +
			" bill_no = ? AND plan_id = ? ";

	private String getSponsorIdFrombillClaim(Connection con, String visitId, String billNo, int planId)
		throws SQLException{
		String sponsorId = null;
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(GET_SPONSOR_ID_FROM_BILL_CLAIM);
			ps.setString(1, visitId);
			ps.setString(2, billNo);
			ps.setInt(3, planId);
			sponsorId = DataBaseUtil.getStringValueFromDb(ps);
		}finally{
			if(ps != null) ps.close();
		}
		return sponsorId;
	}

	private boolean insertInsuranceClaimId(Connection con,String visitId,int planId,
			String claimId, String billNo) throws SQLException,IOException {

		VisitDetailsDAO visitDAO = new VisitDetailsDAO();
		BasicDynaBean visitDetailsBean = visitDAO.findByKey(con, "patient_id", visitId);
		BasicDynaBean billBean = BillDAO.getBillBean(con, billNo);
		int accountGroup = (Integer)billBean.get("account_group");
		boolean success = true;

		BasicDynaBean claimbean = null;
		String mainVisitId = visitDetailsBean.get("main_visit_id") != null ? (String)visitDetailsBean.get("main_visit_id") : null;
		String patientId   = visitDetailsBean.get("patient_id") != null ? (String)visitDetailsBean.get("patient_id") : null;
		String opType	   = visitDetailsBean.get("op_type") != null ? (String)visitDetailsBean.get("op_type") : null;

		ClaimDAO claimdao = new ClaimDAO();
		if (visitDetailsBean.get("op_type") != null && opType != null) {

			opType = opType.equals("R") ? "M" : opType; // If Revisit then claim op type is Main.

			claimbean = claimdao.getBean();
			claimbean.set("claim_id", claimId);
			claimbean.set("main_visit_id", mainVisitId);
			claimbean.set("patient_id", patientId);
			claimbean.set("plan_id", planId);
			claimbean.set("op_type", opType);
			claimbean.set("status", "O");
			claimbean.set("account_group", accountGroup);
			claimdao.insert(con, claimbean);
		}
		return success;
	}

	public boolean isVisitClaimExists(Connection con, int planId, String visitId,
			int accountGroup)throws SQLException {

		String visitClaimId = getVisitClaimId(con, planId, visitId, accountGroup);
		boolean billClaimExists = null != visitClaimId;
		return billClaimExists;
	}

	private static final String GET_VISIT_CLAIM_ID = " SELECT icl.claim_id "+
		" FROM bill_claim bc "+
		" JOIN bill b ON (bc.bill_no = b.bill_no AND bc.visit_id = b.visit_id) "+
		" JOIN insurance_claim icl on(icl.claim_id = bc.claim_id) "+
		" WHERE bc.visit_id = ? AND bc.plan_id = ? AND b.account_group = ?	AND icl.status = 'O' " +
		" ORDER BY icl.claim_id DESC LIMIT 1 ";

	private String getVisitClaimId(Connection con, int planId, String visitId, int accountGroup) throws SQLException{
		String visitClaimId = null;
		PreparedStatement ps = null;
		try{
			ps = con.prepareStatement(GET_VISIT_CLAIM_ID);
			ps.setString(1, visitId);
			ps.setInt(2, planId);
			ps.setInt(3, accountGroup);
			visitClaimId = DataBaseUtil.getStringValueFromDb(ps);
		}finally{
			if(ps != null) ps.close();
		}
		return visitClaimId;
	}

	public boolean isBillClaimExist(Connection con, int planId, String visitId, String billNo)throws SQLException {

		String billClaimId = null;
		billClaimId = getClaimId(con, planId, billNo, visitId);
		boolean billClaimExists = null != billClaimId;
		return billClaimExists;
	}

	private static final String IS_BILL_CHARGE_CLAIM_EXISTS = "SELECT * FROM bill_charge_claim WHERE bill_no = ? AND charge_id=? " +
			" AND claim_id=? ";

  public boolean isBillChargeClaimExists(Connection con, String billNo, String chargeId,
      String claimId) throws SQLException {
    try (PreparedStatement ps = con.prepareStatement(IS_BILL_CHARGE_CLAIM_EXISTS);) {
      ps.setString(1, billNo);
      ps.setString(2, chargeId);
      ps.setString(3, claimId);
      try (ResultSet rs = ps.executeQuery();) {
        if (rs.next()) {
          return true;
        }
      }
    }
    return false;
  }

	private static final String GET_CLAIM_ID = "SELECT claim_id FROM bill_claim WHERE visit_id=? AND bill_no=? AND " +
			" sponsor_id=? AND plan_id=? ";

	public String getClaimId(Connection con, int planId, String billNo, String visitId,String sponsorId)throws SQLException {
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(GET_CLAIM_ID);
			ps.setString(1, visitId);
			ps.setString(2, billNo);
			ps.setString(3, sponsorId);
			ps.setInt(4, planId);
			return DataBaseUtil.getStringValueFromDb(ps);
		}finally{
			if(ps != null) ps.close();
		}
	}


	private static final String GET_CLAIM_ID_FROM_BILL_CLAIM = "SELECT claim_id FROM bill_claim WHERE visit_id=? AND bill_no=? AND plan_id=? ";

	public String getClaimId(Connection con, int planId, String billNo, String visitId)throws SQLException {
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(GET_CLAIM_ID_FROM_BILL_CLAIM);
			ps.setString(1, visitId);
			ps.setString(2, billNo);
			ps.setInt(3, planId);
			return DataBaseUtil.getStringValueFromDb(ps);
		}finally{
			if(ps != null) ps.close();
		}
	}

	private static final String CANCEL_BILL_CHARGE_CLAIMS = "UPDATE bill_charge_claim SET insurance_claim_amt = 0, tax_amt = 0 WHERE charge_id=?";

	public boolean cancelBillChargeClaims(Connection con, List<ChargeDTO> cancelBillChargeList)throws SQLException {
		PreparedStatement ps= null;
		boolean success = true;
		try{
			ps = con.prepareStatement(CANCEL_BILL_CHARGE_CLAIMS);

			Iterator iterator = cancelBillChargeList.iterator();
			while (iterator.hasNext()) {
				ChargeDTO charge = (ChargeDTO) iterator.next();
				if(charge.getStatus().equals("X")) {
					ps.setString(1, charge.getChargeId());
					ps.addBatch();
				}
			}
			ps.executeBatch();
		}finally{
			if(ps != null) ps.close();
		}
		return success;

	}

	public boolean updateBillChargeClaims(Connection con, List<ChargeDTO> updateBillChargeList, String visitId,
			String billNo, int[] planIds,boolean preAuthUpdateReq)throws SQLException,IOException {
		boolean success = false;
		BasicDynaBean bean = getBean();
		if (planIds != null && planIds.length > 0) {
			for(int i=0; i<planIds.length; i++){
				Map<String,Object> keys = new HashMap<String, Object>();
				keys.put("bill_no", billNo);
				keys.put("visit_id", visitId);
				keys.put("plan_id", planIds[i]);
				BasicDynaBean billClaimBean = billClaimDAO.findByKey(con,keys);
				if(null != billClaimBean) {
					for(ChargeDTO charge : updateBillChargeList){
						String claimId = (String)billClaimBean.get("claim_id");
						Map<String,Object> chKeys = new HashMap<String, Object>();
						BigDecimal claimAmounts[] = charge.getClaimAmounts();
						BigDecimal claimTaxAmounts[] = charge.getSponsorTaxAmounts();
						String inclInClaimCalc[] = charge.getIncludeInClaimCalc();
						chKeys.put("bill_no", billNo);
						chKeys.put("claim_id", claimId);
						chKeys.put("charge_id", charge.getChargeId());
						bean.set("insurance_claim_amt", claimAmounts[i]);
						bean.set("tax_amt", claimTaxAmounts[i]);
						if(null != inclInClaimCalc && null != inclInClaimCalc[i])
							bean.set("include_in_claim_calc", inclInClaimCalc[i].equals("Y"));
						if(preAuthUpdateReq){
							String preAuthIds[] = charge.getPreAuthIds();
							Integer preAuthModeIds[] = charge.getPreAuthModeIds();
							if(null != preAuthIds)
								bean.set("prior_auth_id", preAuthIds[i]);
							if(null != preAuthModeIds)
								bean.set("prior_auth_mode_id", preAuthModeIds[i]);
						}
						charge.setVisitId(visitId);
						String chargeGroup = charge.getChargeGroup();
						if(!chargeGroup.equals("MED") && !chargeGroup.equals("RET")){
						  billingHelper.checkForInsCatInRedis(con,charge,planIds[i]);
						}
						bean.set("insurance_category_id", charge.getInsuranceCategoryId());
						success = update(con, bean.getMap(), chKeys) >= 0;
					}
				}
			}
		}
		return success;
	}


	/**
	 * Update given chargeid,billno,claim ids bill_charge_claim amount
	 * @param con
	 * @param billCharge
	 * @param visitId
	 * @param billNo
	 * @param claimId
	 * @param preAuthUpdateReq
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 */
	public boolean updateBillChargeClaim(Connection con,ChargeDTO billCharge, String visitId,
			String billNo,String claimId)throws SQLException,IOException {
		boolean success = false;
		BasicDynaBean bean = getBean();
		Map<String,Object> keys = new HashMap<String, Object>();
		keys.put("bill_no", billNo);
		keys.put("visit_id", visitId);
		keys.put("claim_id", claimId);
		BasicDynaBean billClaimBean = billClaimDAO.findByKey(con,keys);
		if(null != billClaimBean) {
			Map<String,Object> chKeys = new HashMap<String, Object>();

			chKeys.put("bill_no", billNo);
			chKeys.put("claim_id", claimId);
			chKeys.put("charge_id", billCharge.getChargeId());
			bean.set("insurance_claim_amt", billCharge.getInsuranceClaimAmount());

			success = update(con, bean.getMap(), chKeys) >= 0;
		}

		return success;
	}

	public boolean reduceBillChargeClaims(Connection con, List<ChargeDTO> updateBillChargeList, String visitId,
			String billNo, int[] planIds,boolean preAuthUpdateReq)throws SQLException,IOException {
		boolean success = false;
		BasicDynaBean bean = getBean();
		for(int i=0; i<planIds.length; i++){
			Map<String,Object> keys = new HashMap<String, Object>();
			keys.put("bill_no", billNo);
			keys.put("visit_id", visitId);
			keys.put("plan_id", planIds[i]);
			BasicDynaBean billClaimBean = billClaimDAO.findByKey(con,keys);
			if(null != billClaimBean) {
				for(ChargeDTO charge : updateBillChargeList){
					String claimId = (String)billClaimBean.get("claim_id");
					Map<String,Object> chKeys = new HashMap<String, Object>();
					BigDecimal claimAmounts[] = charge.getClaimAmounts();
					BigDecimal claimTaxAmounts[] = charge.getSponsorTaxAmounts();
					chKeys.put("bill_no", billNo);
					chKeys.put("claim_id", claimId);
					chKeys.put("charge_id", charge.getChargeId());

					BasicDynaBean billChargeClaimBean = findByKey(con,chKeys);
					bean.set("insurance_claim_amt", ((BigDecimal)billChargeClaimBean.get("insurance_claim_amt")).subtract(claimAmounts[i]));
					bean.set("tax_amt", ((BigDecimal)billChargeClaimBean.get("tax_amt")).subtract(claimTaxAmounts[i]));
					if(preAuthUpdateReq){
						String preAuthIds[] = charge.getPreAuthIds();
						Integer preAuthModeIds[] = charge.getPreAuthModeIds();
						if(null != preAuthIds)
							bean.set("prior_auth_id", preAuthIds[i]);
						if(null != preAuthModeIds)
							bean.set("prior_auth_mode_id", preAuthModeIds[i]);
					}
					success = update(con, bean.getMap(), chKeys) >= 0;
				}
			}
		}

		return success;
	}

	private static final String UPDATE_BILL_CLAIM_ON_BILL_TYPE_CHANGE = "UPDATE bill_claim SET bill_no=? WHERE bill_no=?";

	public boolean updateBillClaimOnBillTypeChange(Connection con,String billNo, String newBillNo) throws SQLException{
		PreparedStatement ps = null;
		boolean success = false;
		try{
			ps = con.prepareStatement(UPDATE_BILL_CLAIM_ON_BILL_TYPE_CHANGE);
			ps.setString(1, newBillNo);
			ps.setString(2, billNo);
			success = ps.executeUpdate() >= 0;
		}finally{
			if(ps != null) ps.close();
		}
		return success;
	}

	private static final String UPDATE_BILL_CHARGE_CLAIM_ON_BILL_TYPE_CHAGE = "UPDATE bill_charge_claim SET bill_no=? WHERE bill_no=?";

	public boolean updateBillChargeClaimOnBillTypeChange(Connection con,String billNo, String newBillNo) throws SQLException{
		PreparedStatement ps = null;
		boolean success = false;
		try{
			ps = con.prepareStatement(UPDATE_BILL_CHARGE_CLAIM_ON_BILL_TYPE_CHAGE);
			ps.setString(1, newBillNo);
			ps.setString(2,billNo);
			success = ps.executeUpdate() >= 0;
		}finally{
			if(ps != null)ps.close();
		}
		return success;
	}

	// Changes to bill claim and bill charge claim in case of edit insurance and op-ip conversion..

	public boolean changesToBillChargeClaim(Connection con, String billNo, String visitId,
			int[] planIds, String visitType)throws SQLException,IOException {

		boolean success = true;;
		ChargeDAO cdao = new ChargeDAO(con);

		List<BasicDynaBean> existingChargeList = getBillChargeClaims(con, billNo);

		billClaimDAO.delete(con, "bill_no", billNo);
		delete(con, "bill_no", billNo);

		List<ChargeDTO> chargeList = cdao.getBillCharges(billNo);
		for(ChargeDTO charge : chargeList) {
			charge.setInsuranceAmt(planIds, visitType, charge.getFirstOfCategory());
			charge.setPreAuthIds(null);
			charge.setPreAuthModeIds(null);
			charge.setIncludeInClaimCalc(null);
			for(int planId : planIds){
			  billingHelper.invalidateInsCatIdKeyInRedis(charge, planId, visitType);
			}
			
		}

		boolean isTpa = new BillDAO(con).getBill(billNo).getIs_tpa();
 		if(isTpa) {
 			insertBillChargeClaims(con, chargeList, planIds, visitId, billNo);
 			if(null != existingChargeList && existingChargeList.size() > 0)
 				updatepreAuthDetails(con, existingChargeList, billNo, visitId);

			if(null != planIds) {
				for(ChargeDTO charge : chargeList) {
					if(charge.getChargeGroup().equals("MED"))
						EditVisitDetailsDAO.setMedicineInsuranceAmtForPlan(con, planIds, charge);
					if(charge.getChargeHead().equals("PHRET") || charge.getChargeHead().equals("PHCRET"))
						EditVisitDetailsDAO.setMedicineInsuranceAmtForPlan(con, planIds, charge,true);
				}
				updateBillChargeClaims(con, chargeList, visitId, billNo, planIds, false);
			}

 		}
 		return success;
	}


	public boolean changesToBillChargeClaimOnEditIns(Connection con, String billNo, String visitId,
			int[] planIds, String visitType, int existingPriPlan, int exisitingSecPlan) throws SQLException,IOException {
	  Bill bill = new BillDAO(con).getBill(billNo);
		boolean isTpa = bill.getIs_tpa();
		SponsorDAO sponsorDAO = new SponsorDAO();
		String billStatus = bill.getStatus();
 		if(isTpa && null != planIds) {
			for(int i=0; i < planIds.length; i++){
				int existingPlan = i==0 ? existingPriPlan : exisitingSecPlan;
				int planId = planIds[i];
				String claimId = getClaimId(con, existingPlan, billNo, visitId);
				boolean submissionBatchExistis = false;
				GenericDAO insClaimDAO = new GenericDAO("insurance_claim");
				BasicDynaBean insClaimBean = insClaimDAO.getBean();
				insClaimDAO.loadByteaRecords(con, insClaimBean, "claim_id", claimId);
				String submissionBatchId = null;
				if(null != insClaimBean && null != insClaimBean.get("last_submission_batch_id"))
					submissionBatchId = (String)insClaimBean.get("last_submission_batch_id");
				if(null != submissionBatchId && !submissionBatchId.equals(""))
					submissionBatchExistis = true;
				if(submissionBatchExistis) {
					updateBillClaimOnEditIns(con,billNo,planId,claimId,visitId,visitType,null);
					insClaimBean.set("plan_id", planId);
					insClaimDAO.update(con, insClaimBean.getMap(), "claim_id", claimId);
				}else {
					String newClaimId = getNewClaimId(con, billNo, planId, visitId);
          if (null != claimId && !claimId.equals("")) {
            updateBillClaimOnEditIns(con, billNo, planId, claimId, visitId, visitType, newClaimId);
          } else {
            insertbillClaimOnEditIns(con, billNo, planId, visitId, visitType, newClaimId);
            if (billStatus.equals("F")) {
              insertSalesClaimOnEditIns(con, billNo, planId, visitId, visitType, newClaimId);
            }
          }   
				}
			}
			
      if (billStatus.equals("A")) {
        ChargeDAO cdao = new ChargeDAO(con);
        List<ChargeDTO> chargeList = cdao.getBillCharges(billNo);
        for (ChargeDTO charge : chargeList) {
          if (charge.getChargeGroup().equals("MED")) {
            EditVisitDetailsDAO.setMedicineInsuranceAmtForPlan(con, planIds, charge);
          } else if (charge.getChargeHead().equals("PHRET")
              || charge.getChargeHead().equals("PHCRET")) {
            EditVisitDetailsDAO.setMedicineInsuranceAmtForPlan(con, planIds, charge, true);
          } else {
            charge.setInsuranceAmt(planIds, visitType, charge.getFirstOfCategory());
          }
        }
        // On Edit Insurance Updating bill charge tax amount to zero
        updateBillChargeClaimTax(con, chargeList);

        updateBillChargeClaims(con, chargeList, visitId, billNo, planIds, false);
      }
      
			if(planIds.length == 1 && exisitingSecPlan != 0){
				String secClaimId = getClaimId(con, exisitingSecPlan, billNo, visitId);
				LinkedHashMap<String, Object> hKeys = new LinkedHashMap<String, Object>();
				hKeys.put("bill_no", billNo);
				hKeys.put("claim_id", secClaimId);
				billClaimDAO.delete(con, hKeys);
				delete(con, hKeys);
			}

 		}else if(null == planIds && (existingPriPlan !=0 || exisitingSecPlan !=0 )){
 			billClaimDAO.delete(con, "bill_no", billNo);
 			delete(con, "bill_no", billNo);
 			//Update sales claim details and sales claim tax details with ZERO Amts.
 			sponsorDAO.updateSalesClaimDetails(billNo);
			sponsorDAO.updateSalesClaimTaxDetails(billNo);
 			
 		}

		return true;
	}

  private void updateBillChargeClaimTax(Connection con,
			List<ChargeDTO> chargeList) throws SQLException {
		for(ChargeDTO bean:chargeList){
			billChargeClaimTaxDAO.cancelBillChargeClaimTax(con, bean.getChargeId());
			BigDecimal[] sponsorTaxAmts = bean.getSponsorTaxAmounts();
			for(int i =0 ; i< sponsorTaxAmts.length;i ++){
				sponsorTaxAmts[i]= BigDecimal.ZERO;
			}
			bean.setSponsorTaxAmounts(sponsorTaxAmts);
		}
	}

	public boolean insertbillClaimOnEditIns(Connection con, String billNo, int planId,
			String visitId, String visitType, String newClaimId) throws SQLException,IOException {
		boolean success = true;
		Map<String,Object>keys = new HashMap<String, Object>();
		keys.put("patient_id", visitId);
		keys.put("plan_id", planId);
		BasicDynaBean planBean = insPlanDAO.findByKey(con,keys);

		int priority = (Integer) planBean.get("priority");
		String sponsorId = insPlanDAO.getSponsorId(con, visitId, planId);

		BasicDynaBean billClaimBean = billClaimDAO.getBean();

		billClaimBean.set("claim_id", newClaimId);
		billClaimBean.set("bill_no", billNo);
		billClaimBean.set("plan_id", planId);
		billClaimBean.set("visit_id", visitId);
		billClaimBean.set("sponsor_id", sponsorId);
		billClaimBean.set("priority", priority);

		billClaimDAO.insert(con, billClaimBean);

		ChargeDAO cdao = new ChargeDAO(con);
		List<ChargeDTO> chargeList = cdao.getBillCharges(billNo);
		for(ChargeDTO chg : chargeList){
			BasicDynaBean chgClmBean = getBean();
			chgClmBean.set("charge_id", chg.getChargeId());
			chgClmBean.set("claim_id", newClaimId);
			chgClmBean.set("bill_no",  billNo);
			chgClmBean.set("sponsor_id", sponsorId);
			String chargeGroup = (String)chg.getChargeGroup();
      if(!chargeGroup.equals("MED") && !chargeGroup.equals("RET")){
        billingHelper.checkForInsCatInRedis(con,chg, planId);
      }
			chgClmBean.set("insurance_category_id", chg.getInsuranceCategoryId());
			insert(con, chgClmBean);
		}

		return success;
	}
	
  private void insertSalesClaimOnEditIns(Connection con, String billNo, int planId, String visitId,
      String visitType, String newClaimId) throws SQLException, IOException {

    Map<String, Object> keys = new HashMap<String, Object>();
    keys.put("patient_id", visitId);
    keys.put("plan_id", planId);
    BasicDynaBean visitInsBean = insPlanDAO.findByKey(con, keys);

    List<BasicDynaBean> salesClaimBeanList = new ArrayList<BasicDynaBean>();

    String sponsorId = (String) visitInsBean.get("sponsor_id");

    ChargeDAO cdao = new ChargeDAO(con);
    List<ChargeDTO> chargeList = cdao.getBillCharges(billNo);
    for (ChargeDTO chg : chargeList) {
      String chargeGroup = (String) chg.getChargeGroup();
      String chargeHead = (String) chg.getChargeHead();

      if (chargeGroup.equals("MED") || chargeHead.equals("PHRET") || chargeHead.equals("PHCRET")) {

        String chargeId = (String) chg.getChargeId();

        BasicDynaBean salebean = new GenericDAO("store_sales_main")
            .findByKey("charge_id", chargeId);
        if (salebean == null) {
          return;
        }

        String saleId = (String) salebean.get("sale_id");
        List<BasicDynaBean> saleItems = MedicineSalesDAO.getSaleItemsDetails(saleId);

        for (BasicDynaBean saleItem : saleItems) {
          // insert new sale claim details
          BasicDynaBean salesClaimBean = salesClaimDetailsDAO.getBean();

          salesClaimBean.set("claim_id", newClaimId);
          salesClaimBean.set("sale_item_id", (Integer) saleItem.get("sale_item_id"));
          salesClaimBean.set("claim_status", (String) saleItem.get("claim_status"));
          salesClaimBean.set("insurance_claim_amt", BigDecimal.ZERO);
          salesClaimBean.set("ref_insurance_claim_amount", BigDecimal.ZERO);

          MedicineSalesDTO saleItemDto = new MedicineSalesDTO();
          saleItemDto.setMedicineId(saleItem.get("medicine_id").toString());
          billingHelper.checkSaleItemsForInsCatInRedis(chg, saleItemDto, planId);
          salesClaimBean.set("insurance_category_id", saleItemDto.getInsuranceCategoryId());

          salesClaimBean.set("return_insurance_claim_amt", BigDecimal.ZERO);
          salesClaimBean.set("prior_auth_id", (String) saleItem.get("prior_auth_id"));
          salesClaimBean.set("prior_auth_mode_id", (Integer) saleItem.get("prior_auth_mode_id"));
          salesClaimBean.set("sponsor_id", sponsorId);

          salesClaimBeanList.add(salesClaimBean);
        }
      }
    }
    salesClaimDetailsDAO.insertAll(con, salesClaimBeanList);
  }


	public String getNewClaimId(Connection con, String billNo, int planId, String visitId)
		throws SQLException,IOException{

		BillDetails billDetails = new BillBO().getBillDetails(con,billNo);
		Bill bill = billDetails.getBill();
		boolean visitClaimExists = isVisitClaimExists(con,planId,visitId,bill.getAccount_group());

		String claimId = null;
		if(visitClaimExists) {

			claimId = getVisitClaimId(con,planId,visitId, bill.getAccount_group());
		} else {
			int centerId = VisitDetailsDAO.getCenterId(con, visitId);
			claimId = claimDAO.getGeneratedClaimIdBasedonCenterId(centerId, bill.getAccount_group());
			insertInsuranceClaimId(con,visitId,planId,claimId,billNo);
		}
		return claimId;
	}

	public boolean updateBillClaimOnEditIns(Connection con, String billNo, int planId, String oldClaimId,
			String visitId, String visitType,String newClaimId) throws SQLException, IOException {
		boolean success = false;
		BasicDynaBean billClaimBean = billClaimDAO.getBean();
		ChargeDAO cdao = new ChargeDAO(con);
		Map<String,Object>keys = new HashMap<String, Object>();
		keys.put("patient_id", visitId);
		keys.put("plan_id", planId);
		BasicDynaBean planBean = insPlanDAO.findByKey(con,keys);
		int priority = (Integer) planBean.get("priority");
		String sponsorId = insPlanDAO.getSponsorId(con, visitId, planId);

		billClaimBean.set("plan_id", planId);
		billClaimBean.set("visit_id", visitId);
		billClaimBean.set("sponsor_id", sponsorId);
		billClaimBean.set("priority", priority);
		if(null != newClaimId)
			billClaimBean.set("claim_id", newClaimId);

		Map<String,Object>billClmkeys = new HashMap<String, Object>();
		billClmkeys.put("bill_no", billNo);
		billClmkeys.put("claim_id", oldClaimId);

		success = billClaimDAO.update(con, billClaimBean.getMap(), billClmkeys) >= 0;

		if(success) {
			updateBillCharegClaimOnEditIns(con, billNo, planId, oldClaimId, visitId, visitType,
					sponsorId, newClaimId);
			String billStatus = billDAO.getBillStatus(billNo);
			if ("F".equals(billStatus)) {
				salesClaimDetailsDAO.updateSalesClaimOnEditIns(billNo, sponsorId, oldClaimId,
						newClaimId);
			}
		}
		List<ChargeDTO> charges = cdao.getBillCharges(billNo);
    for (ChargeDTO charge : charges) {
      String chargeGroup = (String)charge.getChargeGroup();
      if(!chargeGroup.equals("MED") && !chargeGroup.equals("RET")){
        billingHelper.checkForInsCatInRedis(con,charge, planId);
      }
      BasicDynaBean bean = getBean();
      Map<String, Object> keys1 = new HashMap<String, Object>();
      keys1.put("charge_id", charge.getChargeId());
      if (null != newClaimId) {
        keys1.put("claim_id", newClaimId);
      } else {
        keys1.put("claim_id", oldClaimId);
      }
      bean.set("insurance_category_id", charge.getInsuranceCategoryId());
      update(con, bean.getMap(), keys1);
    }
		return success;
	}

	public boolean updateBillCharegClaimOnEditIns(Connection con,String billNo,int planId,String oldClaimId,String visitId,
			String visitType,String sponsorId,String newClaimId)  throws SQLException,IOException {
		boolean success = true;
		BasicDynaBean bean = getBean();

		Map<String,Object> keys = new HashMap<String, Object>();
		keys.put("bill_no", billNo);
		keys.put("claim_id", oldClaimId);

		bean.set("sponsor_id", sponsorId);
		if(null != newClaimId)
			bean.set("claim_id", newClaimId);
		update(con, bean.getMap(), keys);

		return success;
	}

	private boolean updatepreAuthDetails(Connection con, List<BasicDynaBean> existingChgList, String billNo,
		String visitId) throws SQLException, IOException {

		boolean success = true;
		List<BasicDynaBean> newchargeList = getBillChargeClaims(con, billNo);

		for(BasicDynaBean charge : newchargeList){
			String chargeId = (String)charge.get("charge_id");
			String claimId = (String)charge.get("claim_id");
			int priority = (Integer)charge.get("priority");
			for(BasicDynaBean exChg : existingChgList){
				if(chargeId.equals((String)exChg.get("charge_id")) && priority==(Integer)exChg.get("priority")){
					Map<String, Object> keys = new HashMap<String, Object>();
					BasicDynaBean bean = getBean();
					keys.put("charge_id", chargeId);
					keys.put("claim_id", claimId);
					bean.set("prior_auth_id", (String)exChg.get("prior_auth_id"));
					if(null != exChg.get("prior_auth_mode_id"))
						bean.set("prior_auth_mode_id", (Integer)exChg.get("prior_auth_mode_id"));
					update(con, bean.getMap(), keys);
				}
			}
		}
		return success;
	}

	private static final String GET_BILL_CHARGE_CLAIMS = " SELECT bc.bill_no, bc.visit_id, bc.plan_id, " +
		" bc.claim_id, bcc.charge_id, bcc.prior_auth_id, bcc.prior_auth_mode_id, bc.priority "+
		" FROM bill_claim bc "+
		" JOIN bill_charge_claim  bcc ON(bc.bill_no = bcc.bill_no AND bc.claim_id = bcc.claim_id) "+
		" WHERE bc.bill_no = ? ORDER BY charge_id ";

	public List<BasicDynaBean> getBillChargeClaims(Connection con, String billNo) throws SQLException {
		PreparedStatement ps = null;
		try{
			ps = con.prepareStatement(GET_BILL_CHARGE_CLAIMS);
			ps.setString(1, billNo);
			return DataBaseUtil.queryToDynaList(ps);
		}finally{
			if(ps != null) ps.close();
		}
	}

	public boolean updateClaimIds(Connection con, String billNo,String claimId,int planId)throws SQLException,IOException {

		boolean success = true;
		BillChargeClaimDAO billChgClaimDAO = new BillChargeClaimDAO();
		BasicDynaBean billClaimBean = billClaimDAO.getBean();
		BasicDynaBean billChgClaimBean = billChgClaimDAO.getBean();

		Map<String,Object> keys = new HashMap<String, Object>();
		keys.put("bill_no", billNo);
		keys.put("plan_id", planId);
		String oldClaimId = (String)billClaimDAO.findByKey(con,keys).get("claim_id");
		billClaimBean.set("claim_id", claimId);
		billClaimDAO.update(con, billClaimBean.getMap(), keys) ;

		Map<String,Object> chgKeys = new HashMap<String, Object>();
		chgKeys.put("claim_id", oldClaimId);
		chgKeys.put("bill_no", billNo);

		billChgClaimBean.set("claim_id", claimId);

		billChgClaimDAO.update(con, billChgClaimBean.getMap(), chgKeys);

		return success;
	}

	// Will call this method when connect to IP visit option is selected for finalized/closed
	// bills during OP to IP conversion.
	public boolean updateClaimAndVisitId(Connection con, String billNo, String ipVisitId, String opVisitId,
			int planId, int accountGroup) throws SQLException, IOException {
		boolean success = true;

		// Get visit claim id
		String oldVisitClaimId = getClaimId(con, planId, billNo, opVisitId);
		String visitClaimId = getVisitClaimId(con, planId, ipVisitId, accountGroup);

		if(null == visitClaimId){
			int centerId = VisitDetailsDAO.getCenterId(con, ipVisitId);
			visitClaimId = claimDAO.getGeneratedClaimIdBasedonCenterId(centerId, accountGroup);
			insertInsuranceClaimId(con, ipVisitId, planId, visitClaimId, billNo);
		}

		// Update bill with new visit id and claim id
		Map<String, Object> billClaimKeys = new HashMap<String, Object>();
		billClaimKeys.put("bill_no", billNo);
		billClaimKeys.put("plan_id", planId);
		BasicDynaBean billClaimBean = billClaimDAO.getBean();
		billClaimBean.set("visit_id", ipVisitId);
		billClaimBean.set("claim_id",  visitClaimId);
		billClaimDAO.update(con, billClaimBean.getMap(), billClaimKeys);

		// Update bill charge claim with ip visit claim id
		Map<String, Object> billChgClaimKeys = new HashMap<String, Object>();
		billChgClaimKeys.put("bill_no", billNo);
		billChgClaimKeys.put("claim_id", oldVisitClaimId);
		BasicDynaBean billChgClaimBean = getBean();
		billChgClaimBean.set("claim_id", visitClaimId);
		update(con, billChgClaimBean.getMap(), billChgClaimKeys);

		// Update sales claim details with ip visit claim id
		Map<String,Object> salesClaimKeys = new HashMap<String, Object>();
		salesClaimKeys.put("claim_id", oldVisitClaimId);
		BasicDynaBean salesClaimBean = storeClaimDetails.getBean();
		salesClaimBean.set("claim_id", visitClaimId);
		storeClaimDetails.update(con, salesClaimBean.getMap(), salesClaimKeys);
		return success;
	}

	/* method to update a plan id and sponsor ids of all insurance bills without recalcualting claim amounts in case of
	 * "only open bills" option is selected during edit insurace. */

	public boolean updateInsuranceBillClaim(Connection con, String billNo, int planId, String sponsorId,
			int priority) throws SQLException, IOException{
		boolean success = false;
		BasicDynaBean billClaimBean = billClaimDAO.getBean();
		BasicDynaBean billChgClaimBean = getBean();

		billClaimBean.set("plan_id", planId);
		billClaimBean.set("sponsor_id", sponsorId);

		Map<String,Object>keys = new HashMap<String, Object>();
		keys.put("bill_no", billNo);
		keys.put("priority", priority);

		billClaimDAO.update(con, billClaimBean.getMap(), keys);

		String claimId = (String)billClaimDAO.findByKey(keys).get("claim_id");

		Map<String,Object>bcKeys = new HashMap<String, Object>();
		bcKeys.put("bill_no", billNo);
		bcKeys.put("claim_id", claimId);

		billChgClaimBean.set("sponsor_id", sponsorId);
		update(con, billChgClaimBean.getMap(), bcKeys);

		BasicDynaBean claimBean = claimDAO.getBean();
		claimBean.set("plan_id",planId);
		claimDAO.update(con, claimBean.getMap(), "claim_id", claimId);

		success = true;
		return success;

	}
	
	
	public boolean updatepackageMarginInBillChgClaim(BasicDynaBean pkgMarginChargeBean)
			throws SQLException, IOException{
		Connection con = null;
		boolean success = true;
		try{
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			success = updatepackageMarginInBillChgClaim(con,pkgMarginChargeBean);
		}finally{
			DataBaseUtil.commitClose(con, success);
		}
		return success;
	}

	public boolean updatepackageMarginInBillChgClaim(Connection con, BasicDynaBean pkgMarginChargeBean)
		throws SQLException, IOException{

		String billNo = (String) pkgMarginChargeBean.get("bill_no");
		String charge_id = (String) pkgMarginChargeBean.get("charge_id");
		Map<String,Object> keys = new HashMap<String, Object>();
		keys.put("bill_no", billNo);
		keys.put("priority", 1);
		BasicDynaBean billClaimBean = billClaimDAO.findByKey(con, keys);

		Map<String,Object> billChgClaimKeys = new HashMap<String, Object>();
		billChgClaimKeys.put("charge_id", charge_id);
		billChgClaimKeys.put("claim_id", (String)billClaimBean.get("claim_id"));
		BasicDynaBean billChgClaimBean = billChargeClaimDAO.findByKey(con, billChgClaimKeys);

		billChgClaimBean.set("insurance_claim_amt", (BigDecimal)pkgMarginChargeBean.get("insurance_claim_amount"));
		billChgClaimBean.set("amount_included", (BigDecimal)pkgMarginChargeBean.get("amount_included"));
		billChgClaimBean.set("include_in_claim_calc", true);
		billChargeClaimDAO.update(con, billChgClaimBean.getMap(), billChgClaimKeys);

		return true;
	}

	public void updateBillChargeClaims(List<BasicDynaBean> billCharges, Map<String, List<BasicDynaBean>> billChargeClaimsMap)
		throws SQLException,IOException{

		Connection con = null;
		boolean success = false;
		try{
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			for(BasicDynaBean billCharge : billCharges){
				String chargeID = (String)billCharge.get("charge_id");
				String chargeType = (String)billCharge.get("charge_type");
				List<BasicDynaBean>billChargeClaims = billChargeClaimsMap.get(chargeID);
				for(BasicDynaBean billChargeClaim : billChargeClaims){
					String claimId = (String)billChargeClaim.get("claim_id");
					Map<String, Object> keys = new HashMap<>();
					if(chargeType.equals("hospital")){
						keys.put("charge_id", chargeID);
						keys.put("claim_id", claimId);
						update(con, billChargeClaim.getMap(), keys);
					}else if(chargeType.equals("pharmacy")){
						int saleItemId = Integer.parseInt(chargeID.split("-")[1]);
						keys.put("sale_item_id", saleItemId);
						keys.put("claim_id", claimId);
						BigDecimal insClaimAmt = (BigDecimal)billChargeClaim.get("insurance_claim_amt");
						BigDecimal insClaimTaxAmt = (BigDecimal)billChargeClaim.get("tax_amt");
						Boolean inclInClaimCalc = (Boolean)billChargeClaim.get("include_in_claim_calc");
						BasicDynaBean salesClaimBean = storeClaimDetails.findByKey(con,keys);
						salesClaimBean.set("insurance_claim_amt", insClaimAmt);
						salesClaimBean.set("ref_insurance_claim_amount", insClaimAmt);
						salesClaimBean.set("tax_amt", insClaimTaxAmt);
						salesClaimBean.set("include_in_claim_calc", inclInClaimCalc);
						storeClaimDetails.update(con, salesClaimBean.getMap(), keys);
					}
				}
			}
			success = true;
		}finally{
			DataBaseUtil.commitClose(con, success);
			
		}

	}
	
	
	private static final String SPONSOR_AMMOUNT_QUERY ="SELECT sum(bccl.insurance_claim_amt) as sponsor_amt, sum(bccl.claim_recd_total) as claim_recd_amt "+
			" FROM bill_charge_claim bccl "+
			//" JOIN bill_claim bcl ON(bcl.bill_no = b.bill_no) "+
			" JOIN bill_charge bc ON(bc.charge_id = bccl.charge_id) "+
			" JOIN bill b ON (bc.bill_no = b.bill_no) "+		
			" WHERE bccl.claim_id = ?  AND  b.sponsor_writeoff != 'A'  AND bc.status != 'X'";

	public BasicDynaBean getSponsorBean(String claimId) throws SQLException {
		return DataBaseUtil.queryToDynaBean(SPONSOR_AMMOUNT_QUERY, claimId);
	}
	
	public BasicDynaBean getSponsorBean(Connection con,String claimId) throws SQLException {
	  String claimIds[] = new String[]{claimId};
    return DataBaseUtil.queryToDynaBean(con,SPONSOR_AMMOUNT_QUERY, claimIds);
  }
	
	private static final String  SPONSORS_RECEIPT_AMMOUNT_QUERY ="select sum(primary_total_sponsor_receipts) as pri_sponsor_receipt_amt, "+
			" sum(secondary_total_sponsor_receipts) as sec_sponsor_receipt_amt "+
			" FROM bill b "+
			" JOIN bill_claim pbcl ON(pbcl.bill_no = b.bill_no and pbcl.claim_id = ?) "+
			" LEFT JOIN bill_claim sbcl ON(sbcl.bill_no = b.bill_no and sbcl.claim_id = ?) "+
			" WHERE b.sponsor_writeoff != 'A' ";

	public  BasicDynaBean getSponsorsReceipts(Connection con,String priCaimId, String secClaimId) throws SQLException {
		String claimIds[] = new String[]{priCaimId,secClaimId};
		return DataBaseUtil.queryToDynaBean(con,SPONSORS_RECEIPT_AMMOUNT_QUERY, claimIds);
				
	}

  	public Boolean updateRoundOffInBillChargeClaim(Connection con, BasicDynaBean roundOffChBean, String claimId) throws SQLException, IOException{
	    // TODO Auto-generated method stub
	    String chargeId = (String)roundOffChBean.get("charge_id");
	    BigDecimal insuranceClaimAmt = (BigDecimal)roundOffChBean.get("insurance_claim_amount");
	    Map<String,Object> keys = new HashMap<String,Object>();
	    keys.put("charge_id", chargeId);
	    keys.put("claim_id", claimId);
	    BasicDynaBean billChargeClaimBean = findByKey(keys);
	    Boolean returnStatus = Boolean.TRUE;
	    if (billChargeClaimBean != null) {
	      billChargeClaimBean.set("insurance_claim_amt", insuranceClaimAmt);
	      returnStatus = update(con, billChargeClaimBean.getMap(), keys) >= 0;
	    }
	    return returnStatus;
	  }

  	private static final String CLAIM_AMOUNT_TOTAL = "SELECT COALESCE(sum(insurance_claim_amt),0.0) AS insurance_claim_amt  from bill_claim bc"
  			+ " JOIN bill_charge_claim bcc on(bcc.bill_no = bc.bill_no AND bcc.claim_id = bc.claim_id)"
  			+ " JOIN bill_charge on(bill_charge.bill_no = bc.bill_no AND bill_charge.charge_id = bcc.charge_id AND bill_charge.status != 'X') "
  			+ " where bc.bill_no =? AND priority=? ";
  	
	public static BasicDynaBean getPrimarySecondarySponsorAmountDetailsBean(String billNo,
			int priority) throws SQLException {
		return DataBaseUtil.queryToDynaBean(CLAIM_AMOUNT_TOTAL, new Object[]{billNo,priority});
	}
	
	private static final String GET_CASE_RATE_CAT_AMT_IN_HOSP_BILL = 
	        " SELECT SUM(bcc.insurance_claim_amt + bcc.tax_amt) "
	        + " FROM bill_charge_claim bcc "
	        + " WHERE bcc.bill_no = ? AND bcc.claim_id = ? AND bcc.insurance_category_id = ? "
	        + " AND bcc.charge_head NOT IN ('PHMED','PHCMED','PHRET','PHCRET') " ;
	        
	        
	private static final String GET_CASE_RATE_CAT_AMT_IN_PHAR_BILL = 
	         " SELECT SUM(scd.insurance_claim_amt + scd.tax_amt) "
	        + " FROM sales_claim_details scd "
	        + " JOIN store_sales_details ssd ON(scd.sale_item_id = ssd.sale_item_id) "
	        + " JOIN store_sales_main ssm ON(ssm.sale_id = ssd.sale_id) "
	        + " WHERE ssm.bill_no = ? AND scd.claim_id = ? AND scd.insurance_category_id = ? ";
	
	public BigDecimal getCaseRateCatAmtInBill(String billNo, String claimId, int insCatId) throws SQLException{
    BigDecimal hospAmt = getCaseRateCatAmt(GET_CASE_RATE_CAT_AMT_IN_HOSP_BILL, billNo, claimId, insCatId);
    BigDecimal pharAmt = getCaseRateCatAmt(GET_CASE_RATE_CAT_AMT_IN_PHAR_BILL, billNo, claimId, insCatId);
    
    hospAmt = null == hospAmt ? BigDecimal.ZERO : hospAmt;
    pharAmt = null == pharAmt ? BigDecimal.ZERO : pharAmt;
    
    return hospAmt.add(pharAmt);
  }
	        
  public BigDecimal getCaseRateCatAmt(String query, String billNo, String claimId, int insCatId)throws SQLException{
    Connection con = null;
    PreparedStatement ps = null;
    try{
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(query);
      ps.setString(1, billNo);
      ps.setString(2, claimId);
      ps.setInt(3, insCatId);
      return DataBaseUtil.getBigDecimalValueFromDb(ps);
    }finally{
      DataBaseUtil.closeConnections(con, ps);
    }
  }
}
