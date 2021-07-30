package com.insta.hms.billing;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.bob.hms.common.Preferences;
import com.bob.hms.common.RequestContext;
import com.insta.hms.Registration.PatientDetailsDAO;
import com.insta.hms.Registration.PatientInsurancePlanDAO;
import com.insta.hms.Registration.VisitCaseRateDetailDAO;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.billing.payment.PaymentEngine;
import com.insta.hms.billing.paymentdetails.AbstractPaymentDetails;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.discharge.DischargeDAO;
import com.insta.hms.dischargesummary.DischargeSummaryBOImpl;
import com.insta.hms.editvisitdetails.EditVisitDetailsDAO;
import com.insta.hms.insurance.RemittanceAdviceDAO;
import com.insta.hms.insurance.RuleAdjustmentType;
import com.insta.hms.insurance.SponsorBO;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.CenterPreferences.CenterPreferencesDAO;
import com.insta.hms.master.PerDiemCodes.PerDiemCodesDAO;
import com.insta.hms.master.PlanMaster.PlanMasterDAO;
import com.insta.hms.master.RegistrationPreferences.CustomField;
import com.insta.hms.master.RegistrationPreferences.RegistrationPreferencesDAO;
import com.insta.hms.master.ServiceSubGroup.ServiceSubGroupDAO;
import com.insta.hms.medicalrecorddepartment.MRDCaseFileIssueDAO;
import com.insta.hms.stores.MedicineSalesBO;
import com.insta.hms.stores.MedicineSalesDAO;
import com.insta.hms.stores.MedicineSalesDTO;
import com.insta.hms.stores.SalesClaimDetailsDAO;
import com.insta.hms.stores.StockUserIssueDAO;
import com.insta.hms.stores.StockUserReturnDAO;
import com.insta.hms.stores.StoreDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpSession;

/*
 * Business Object for bill, mainly for use from the Billing and
 * collection screens. This class contains all the required methods for maniuplating
 * the bill as well as its sub-objects like charges and receipts.
 */
public class BillBO {

	static Logger logger = LoggerFactory.getLogger(BillBO.class);
	
	static BillChargeTaxDAO billChargeTaxDao = new BillChargeTaxDAO();
	static DischargeDAO dischargeDao = new DischargeDAO();
	static BillDAO billDao = new BillDAO();
	static PatientInsurancePlanDAO insPlanDAO = new PatientInsurancePlanDAO();
	static BillClaimDAO billClaimDAO = new BillClaimDAO();
	static GenericDAO billChargeDAO = new GenericDAO("bill_charge");
	static GenericDAO saleDetailDAO = new GenericDAO("store_sales_details");
	static GenericDAO saleMainDAO = new GenericDAO("store_sales_main");
	static ServiceSubGroupDAO subGrpDAO = new ServiceSubGroupDAO();
	static VisitDetailsDAO visitDetailsDao = new VisitDetailsDAO();
	static GenericDAO orgDetailsDao = new GenericDAO("organization_details");
	static PlanMasterDAO planMasterDao = new PlanMasterDAO();
	static BillChargeClaimDAO billChgClaimDAO = new BillChargeClaimDAO();
	static VisitCaseRateDetailDAO visitCaseRateDetailsDAO = new VisitCaseRateDetailDAO();
	static SalesClaimDetailsDAO salesClaimDetailsDAO = new SalesClaimDetailsDAO();

	/*
	 * Get the contents (shallow) of a Bill
	 */
	public Bill getBill(String billNo) throws SQLException {
		Connection con = DataBaseUtil.getConnection();

		try {
			BillDAO billDAO = new BillDAO(con);
			Bill bill = billDAO.getBill(billNo);
			return bill;
		} finally {
			if (con != null)
				con.close();
		}
	}

	public String getMrNo(String billNo) throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		try {
			BillDAO billDAO = new BillDAO(con);
			String mrNo = billDAO.getMrNo(billNo);
			return mrNo;
		} finally {
			if (con != null)
				con.close();
		}
	}

	/*
	 * Return all details about a bill (deep): the bill itself and all contained
	 * objects The return value is BillDetails object, which contains all these.
	 */
	public BillDetails getBillDetails(String billNo) throws SQLException {

		Connection con = DataBaseUtil.getConnection();

		try {
			BillDAO billDAO = new BillDAO(con);
			ChargeDAO chargeDAO = new ChargeDAO(con);
			ReceiptRelatedDAO receiptDAO = new ReceiptRelatedDAO(con);

			BillDetails billDetails = new BillDetails();

			billDetails.setBill(billDAO.getBill(billNo));
			if (billDetails.getBill() == null) {
				return null;
			}
			billDetails.setCharges(chargeDAO.getBillCharges(billNo));
			billDetails.setReceipts(receiptDAO.getBillReceipts(billNo, "R"));
			billDetails.setRefunds(receiptDAO.getBillReceipts(billNo, "F"));

			return billDetails;
		} finally {
			if (con != null)
				con.close();
		}
	}
	
	
  /**
   * This method used in View/Edit Bill screen for getting only logged in user center bill.
   * @param billNo the billNo
   * @param centerId the centerId
   * @return the BillDetails
   * @throws SQLException the SQLException
   */
  public BillDetails getBillDetails(String billNo, int centerId) throws SQLException {

    Connection con = DataBaseUtil.getConnection();

    try {
      BillDAO billDAO = new BillDAO(con);
      ChargeDAO chargeDAO = new ChargeDAO(con);
      ReceiptRelatedDAO receiptDAO = new ReceiptRelatedDAO(con);

      BillDetails billDetails = new BillDetails();

      billDetails.setBill(billDAO.getBill(billNo, centerId));
      if (billDetails.getBill() == null) {
        return null;
      }
      billDetails.setCharges(chargeDAO.getBillCharges(billNo));
      billDetails.setReceipts(receiptDAO.getBillReceipts(billNo, "R"));
      billDetails.setRefunds(receiptDAO.getBillReceipts(billNo, "F"));

      return billDetails;
    } finally {
      if (con != null)
        con.close();
    }
  }
  
  /**
   * This method used in View/Edit Bill screen for getting only logged in user center bill.
   * @param billNo the billNo
   * @param centerId the centerId
   * @return the BillDetails
   * @throws SQLException the SQLException
   */
  public BillDetails getBillDetails(Connection con, String billNo, int centerId) throws SQLException {

      BillDAO billDAO = new BillDAO(con);
      ChargeDAO chargeDAO = new ChargeDAO(con);
      ReceiptRelatedDAO receiptDAO = new ReceiptRelatedDAO(con);

      BillDetails billDetails = new BillDetails();

      billDetails.setBill(billDAO.getBill(billNo, centerId));
      if (billDetails.getBill() == null) {
        return null;
      }
      billDetails.setCharges(chargeDAO.getBillCharges(billNo));
      billDetails.setReceipts(receiptDAO.getBillReceipts(billNo, "R"));
      billDetails.setRefunds(receiptDAO.getBillReceipts(billNo, "F"));

      return billDetails;
  }
  
	public BillDetails getBillDetails(Connection con, String billNo) throws SQLException {
		BillDAO billDAO = new BillDAO(con);
		ChargeDAO chargeDAO = new ChargeDAO(con);
		ReceiptRelatedDAO receiptDAO = new ReceiptRelatedDAO(con);

		BillDetails billDetails = new BillDetails();

		billDetails.setBill(billDAO.getBill(billNo));
		if (billDetails.getBill() == null) {
			return null;
		}
		billDetails.setCharges(chargeDAO.getBillCharges(billNo));
		billDetails.setReceipts(receiptDAO.getBillReceipts(billNo, "R"));
		billDetails.setRefunds(receiptDAO.getBillReceipts(billNo, "F"));

		return billDetails;
	}


	public boolean getOkToDischarge(String patientId) throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		try {
			BillDAO billDAO = new BillDAO(con);
			return billDAO.getOkToDischarge(patientId);
		} finally {
			if (con != null) con.close();
		}
	}

	public List getChargeGroupConstNames() throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		try {
			return (new ChargeDAO(con)).getChargeGroupConstNames();
		} finally {
			if (con != null)
				con.close();
		}
	}
	
	public List getChargeGroupConstNames(Connection con) throws SQLException {
      return (new ChargeDAO(con)).getChargeGroupConstNames();
  }
	
	public List getChargeGroupConstNames(boolean orderable) throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		try {
			return (new ChargeDAO(con)).getChargeGroupConstNames(orderable);
		} finally {
			if (con != null)
				con.close();
		}
	}

	public List getChargeHeadConstNames() throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		try {
			return (new ChargeDAO(con)).getChargeHeadConstNames();
		} finally {
			if (con != null)
				con.close();
		}
	}
	
	public List getChargeHeadConstNames(Connection con) throws SQLException {
      return (new ChargeDAO(con)).getChargeHeadConstNames();
  }

	/*
	 * Reopen a bill atomically. Other related changes that may happen:
	 * 1. OK to discharge status will be set to Not OK.
	 * 2. If reopening a cancelled bill, and if it is the only credit bill, then set it to primary bill.
	 *    (This is equivalent to creating a new bill ... should we allow this?)
	 */
	public boolean reopenBill(String billNo, String reopenReason, String userid, String secSponsorExists)
	  throws SQLException, IOException {
		Connection con = DataBaseUtil.getConnection();
		boolean success = false;

		try {
			BillDAO billDAO = new BillDAO(con);
			Bill bill = billDAO.getBill(billNo);

			// set the status to open
			String origStatus = bill.getStatus();
			bill.setStatus(Bill.BILL_STATUS_OPEN);
			bill.setPaymentStatus(Bill.BILL_PAYMENT_UNPAID);
			bill.setPrimaryClaimStatus(Bill.BILL_CLAIM_OPEN);
			if (secSponsorExists != null && secSponsorExists.equals("Y")) {
				bill.setSecondaryClaimStatus(Bill.BILL_CLAIM_OPEN);
			}
			bill.setReopenReason(reopenReason);
			bill.setOkToDischarge("N");
			bill.setBillPrinted("N");
			bill.setWriteOffRemarks("");
			bill.setSpnrWriteOffRemarks("");
			bill.setBillSignature("");
			
			// OK to discharge can not remain because bed finalization should be checked
			//  for an open/unpaid bill

			bill.setModTime(new java.util.Date());
			bill.setUserName(userid);
			success = billDAO.updateBill(bill);
			
			// Set financial discharge to false
			if(success && dischargeDao.checkIfPatientDischargeEntryExists(bill.getVisitId()) !=null) {
				success = dischargeDao.updateFinancialDischargeDetails(con, bill.getVisitId(), false, bill.getUserName());
			}

			if(success) {
				BasicDynaBean billBean = billDao.findByKey(con, "bill_no", billNo);
				billBean.set("patient_writeoff", "N");
				billBean.set("sponsor_writeoff", "N");
				billDao.update(con, billBean.getMap(), "bill_no", billNo);
			}

			// set the primary flag for credit bills for convenience (if it is the only credit bill)
			if (success && origStatus.equals("X") && bill.getBillType().equals("C"))
				billDAO.setPrimaryBillConditional(billNo);

		} finally {
			if (con != null) { con.close(); }
		}
		return success;
	}
	
	/*
	 *Reopen bill with passing connection object
	 */
	public boolean reopenBill(Connection con,String billNo, String reopenReason, String userid, String secSponsorExists)
			throws SQLException, IOException {
		boolean success = false;	
		BillDAO billDAO = new BillDAO(con);
		Bill bill = billDAO.getBill(billNo);

		// set the status to open
		String origStatus = bill.getStatus();
		bill.setStatus(Bill.BILL_STATUS_OPEN);
		bill.setPaymentStatus(Bill.BILL_PAYMENT_UNPAID);
		bill.setPrimaryClaimStatus(Bill.BILL_CLAIM_OPEN);
		if (secSponsorExists != null && secSponsorExists.equals("Y")) {
			bill.setSecondaryClaimStatus(Bill.BILL_CLAIM_OPEN);
		}
		bill.setReopenReason(reopenReason);
		bill.setOkToDischarge("N");
		bill.setBillPrinted("N");
		bill.setWriteOffRemarks("");
		bill.setSpnrWriteOffRemarks("");

		// OK to discharge can not remain because bed finalization should be checked
		//  for an open/unpaid bill

		bill.setModTime(new java.util.Date());
		bill.setUserName(userid);
		success = billDAO.updateBill(bill);

		// Set financial discharge to false
		if(success && dischargeDao.checkIfPatientDischargeEntryExists(bill.getVisitId()) !=null) {
			success = dischargeDao.updateFinancialDischargeDetails(con, bill.getVisitId(), false, bill.getUserName());
		}

		if(success) {
			BasicDynaBean billBean = billDao.findByKey(con, "bill_no", billNo);
			billBean.set("patient_writeoff", "N");
			billBean.set("sponsor_writeoff", "N");
			billDao.update(con, billBean.getMap(), "bill_no", billNo);
		}

		// set the primary flag for credit bills for convenience (if it is the only credit bill)
		if (success && origStatus.equals("X") && bill.getBillType().equals("C"))
			billDAO.setPrimaryBillConditional(billNo);


		return success;
	}

	/*
	 * Update the bill, insert charges, and create any receipts/refunds etc. all in
	 * one shot
	 */
	public boolean updateBillDetails(Bill bill, String newStatus, List updateBillChargeList,
			List insertBillChargeList, List<Receipt> receiptList,
			boolean updateDynaPkg, Map drgCodeMap, boolean billHasPlanVisitCopayLimit,
			Map<String, List<BasicDynaBean>> insertBillChargeTaxMap, Map<String, List<BasicDynaBean>> updateBillChargeTaxMap) throws SQLException, ParseException, Exception{

		Connection con = DataBaseUtil.getConnection();
		con.setAutoCommit(false);
		boolean success = false;
		boolean allSuccess = false;
		boolean resetDynaPackage = false;
		boolean processPackage = false;
		boolean perdiemProcess = visitDetailsDao.visitUsesPerdiem(bill.getVisitId());
		
		try {
			do {
				/*
				 * 1. Update the bill itself
				 */
				BillDAO billDAO = new BillDAO(con);
				bill.setModTime(new java.util.Date());
				success = billDAO.updateBill(bill);
				if (!success) break;

				/*
				 * Discharge the patient if required
				 */
				//it will work in the case of OP Bill Later.
				if (null!=bill.getDischarge() &&  bill.getDischarge().equals("Y")) {
					// discharge the patient.
					success = new DischargeSummaryBOImpl().dischargePatient(con, bill.getMrno(),
							bill.getVisitId(), bill.getUserName(), bill.getDisDate(), bill.getDisTime());
					if (!success) break;
				}
				
				// insert or update the financial discharge date and time
				Boolean isEntryExists = dischargeDao.checkIfPatientDischargeEntryExists(bill.getVisitId()) != null;
				if (bill.getOkToDischarge().equals("Y")) {
					boolean isAllBillsFinalized = billDAO.getOkToDischarge(bill.getVisitId());
					if (success && isEntryExists && isAllBillsFinalized) {
						Map columndata = new HashMap();
						Map keys = new HashMap();
					    keys.put("patient_id", bill.getVisitId());
						columndata.put("financial_discharge_date", DateUtil.parseDate(bill.getDisDate()));
						columndata.put("financial_discharge_time", DateUtil.parseTime(bill.getDisTime()));
						columndata.put("financial_entered_by", bill.getUserName());
						columndata.put("financial_discharge_status", true);
						success = dischargeDao.update(con, columndata, keys) > 0;
					} else if (success && isAllBillsFinalized) {
						BasicDynaBean bean = dischargeDao.getBean();
					    bean.set("patient_id", bill.getVisitId());
					    bean.set("financial_discharge_status", true);
					    bean.set("financial_discharge_date", DateUtil.parseDate(bill.getDisDate()));
					    bean.set("financial_discharge_time", DateUtil.parseTime(bill.getDisTime()));
					    bean.set("financial_entered_by", bill.getUserName()); 
					    success = dischargeDao.insert(con, bean);
					}
				} else if (success && isEntryExists) {
					success = dischargeDao.updateFinancialDischargeDetails(con, bill.getVisitId(), false, bill.getUserName());
				}

				/*
				 * Insert any new charges that were added
				 */
				ChargeDAO chargeDAOObj = new ChargeDAO(con);
				ChargeDTO chargeDTOObj = null;
				if (!insertBillChargeList.isEmpty()) {
					HashMap<String, String> chargeIdMap = new HashMap<String, String>();
					Iterator chargeItr = insertBillChargeList.iterator();
					while (chargeItr.hasNext()) {
						chargeDTOObj = (ChargeDTO) chargeItr.next();

						String origChargeId = chargeDTOObj.getChargeId();
						String newChargeId = chargeDAOObj.getNextChargeId();
						chargeIdMap.put(origChargeId, newChargeId);
						chargeDTOObj.setChargeId(newChargeId);

						List<BasicDynaBean> billChargeTaxList = insertBillChargeTaxMap.get(origChargeId);
						for(BasicDynaBean taxBean : billChargeTaxList){
							taxBean.set("charge_id", newChargeId);
						}
						insertBillChargeTaxMap.remove(origChargeId);
						insertBillChargeTaxMap.put(newChargeId, billChargeTaxList);
					}

					// update any charge references with the new charge ID generated
					Iterator chargeRefItr = insertBillChargeList.iterator();
					while (chargeRefItr.hasNext()) {
						ChargeDTO charge = (ChargeDTO) chargeRefItr.next();
						String chargeRef = charge.getChargeRef();

						if (chargeRef.equals("_")) {
							charge.setChargeRef("");
						} else if (!chargeRef.equals("") && !chargeRef.startsWith("_")) {
							charge.setChargeRef(chargeRef);
						}else {
							logger.debug("Found reference to: " + chargeRef);
							String newId = (String) chargeIdMap.get(chargeRef);
							charge.setChargeRef(newId);
						}
					}

					success = chargeDAOObj.insertCharges(insertBillChargeList);
					if (!success) break;

					success = billChargeTaxDao.insertBillChargeTaxes(con, insertBillChargeTaxMap);

					if(!success) break;

					//Insert the Special Service Codes in bill charge transaction table
					String healthAuthority = CenterMasterDAO.getHealthAuthorityForCenter(RequestContext.getCenterId());
					if("DHA".equals(healthAuthority)) {
						chargeDAOObj.insertSpecialServiceObservation(con, insertBillChargeList, bill.getBillNo());
					}
				}

				/*
				 * 3a. Update existing charges
				 */
				if (!updateBillChargeList.isEmpty()) {
					success = chargeDAOObj.updateChargeAmountsList(updateBillChargeList);
					if (!success) break;
					
					success = billChargeTaxDao.updateBillChargeTaxes(con,updateBillChargeTaxMap);
				}

				/*
				 * 4. updateDynaPkg
				 */
				if (updateDynaPkg) {
					success = chargeDAOObj.updatePackageMarginCharge(updateBillChargeList);
					if (!success) break;
				}

				/*
				 * 5. Process DRG if bill contains DRG Code.
				 */

				//success = billProcessDRG(con, bill.getBillNo());
				if (!success) break;

				/*
				 * 6. Process Dyna Package if required.
				 */
				success = new DynaPackageProcessor().reset(con, bill.getBillNo(),updateBillChargeList);
				if (!success) break;

				resetDynaPackage = new DynaPackageProcessor().isDynaPackageChanged(con, bill.getBillNo());


				/*
				 * 7. Process Dyna Package when package is not processed.
				 */
				if (resetDynaPackage || (bill.getDynaPkgProcessed().equals("N") && bill.getDynaPkgId() != 0)) {
					processPackage = true;
				}

				/*
				 * 8. Add any receipts for amounts paid/refunded
				 */

				AbstractPaymentDetails bpImpl = AbstractPaymentDetails.getReceiptImpl(AbstractPaymentDetails.BILL_PAYMENT);
				success = bpImpl.createReceipts(con, receiptList, bill, bill.getVisitType(), newStatus);
				if (!success) break;

				allSuccess = true;
				
				if(null != bill.getSponsorWriteOff() && bill.getSponsorWriteOff().equals("M") &&
						null != bill.getPrimaryClaimID() && ! bill.getPrimaryClaimID().equals("")){
					GenericDAO insuranceClaimDAO =new GenericDAO("insurance_claim");
					Map fields = new HashMap();
					fields.put("closure_type", "W");
					Map keys = new HashMap();
					keys.put("claim_id", bill.getPrimaryClaimID());
					insuranceClaimDAO.update(con,fields,keys);
					
				}

			} while (false);

		} finally {
			if (allSuccess) {
				logger.info("Bill operations success");
				con.commit();
				con.close();
			} else {
				logger.error("Bill operations failed");
				if (receiptList != null) receiptList.clear();
				con.rollback();
				con.close();
			}

			// Reset sponsor amounts bill has change in dyna package or bill has drg code.
			if (allSuccess && (resetDynaPackage || perdiemProcess
								|| (drgCodeMap != null && drgCodeMap.get("drg_charge_id") != null)
								|| billHasPlanVisitCopayLimit))
				BillDAO.resetTotalsOrReProcess(bill.getBillNo(), processPackage, false, true);
		}
		return allSuccess;
	}

	/*
	 * Create a new bill for a patient. Will fail if the Visit is not active.
	 * Returns null for no errors, and the error string if any error.
	 */
	public String createNewBill(Bill bill, boolean visitCheck) throws SQLException, IOException {
		Connection con = null;
		boolean success = false;
		String error = null;
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			Map msgMap = createNewBill(con, bill, visitCheck);
			int planIds[] = insPlanDAO.getPlanIds(bill.getVisitId());

			if(bill.getIs_tpa()){
				billChgClaimDAO.insertBillChargeClaims(con, null, planIds, bill.getVisitId(), bill.getBillNo());
			}
			
			error = (String) msgMap.get("error");
			// Set financial discharge to false
			if(error == null && dischargeDao.checkIfPatientDischargeEntryExists(bill.getVisitId()) != null) {
				boolean flag = dischargeDao.updateFinancialDischargeDetails(con, bill.getVisitId(), false, bill.getUserName());
				error = flag ? null : "Failed to reset the discharge details";
			}
			
			success = (error == null);
		} finally {
			DataBaseUtil.commitClose(con, success);
		}
		return error;
	}

	public Map createNewBill(Connection con, Bill bill, boolean visitCheck) throws SQLException, IOException {
		Map map = new HashMap();
		String billNo = null;
		String visitId         =  bill.getVisitId();
		String billType        =  bill.getBillType();
		String visitType       =  bill.getVisitType();
		String secClaimStatus  =  bill.getSecondaryClaimStatus();
		String restrictionType =  bill.getRestrictionType();

		BasicDynaBean planDetails = insPlanDAO.getVisitPrimaryPlan(con, bill.getVisitId());

		HttpSession session	= RequestContext.getSession();
		Integer centerId = (Integer)session.getAttribute("centerId");

		boolean mod_adv_ins	= (Boolean)session.getAttribute("mod_adv_ins");
		boolean allowBillInsurance = BillDAO.isBillInsuranceAllowed(billType, bill.getIs_tpa());

		BillDAO billDAO = new BillDAO(con);

		if (visitCheck) {
			BasicDynaBean patientDetails = VisitDetailsDAO.getPatientVisitDetailsBean(con, visitId);

			if (patientDetails == null) {
				map.put("error", "Invalid Visit Number: " + visitId);
				return map;
			}

			if (visitType == null || visitType.equals("")) {
				visitType = (String)patientDetails.get("visit_type");
				bill.setVisitType(visitType);
			}

			if (secClaimStatus == null || secClaimStatus.equals("")) {
				String secSponsorId = (patientDetails.get("secondary_sponsor_id") != null
						&& !((String)patientDetails.get("secondary_sponsor_id")).equals(""))
						? (String)patientDetails.get("secondary_sponsor_id") : null;
				if (secSponsorId != null)
					bill.setSecondaryClaimStatus(bill.BILL_CLAIM_OPEN);
			}
		}

		if (restrictionType == null || restrictionType.equals("")) {
			restrictionType = "N";
			bill.setRestrictionType(restrictionType);
		}

		billNo = billDAO.getNextBillNo(con, billType, visitType, restrictionType, centerId,allowBillInsurance,false);
		if (billNo == null || billNo.equals(""))
			map.put("error", "Unknown error while generating bill number.");

		bill.setBillNo(billNo);
		if (bill.getDepositSetOff() == null)
			bill.setDepositSetOff(BigDecimal.ZERO);
		
		if (bill.getIpDepositSetOff() == null)
      bill.setIpDepositSetOff(BigDecimal.ZERO);

		if (bill.getStatus() == null) {
			bill.setStatus(Bill.BILL_STATUS_OPEN);
		}

		if (bill.getOkToDischarge() == null) {
			// default the discharge status to not ok.
			bill.setOkToDischarge(Bill.BILL_DISCHARGE_NOTOK);
		}

		bill.setIs_tpa(allowBillInsurance);

		if (bill.getIs_tpa())
			bill.setPrimaryClaimStatus(bill.BILL_CLAIM_OPEN);

		bill.setCenterId(centerId);

		if ((visitType.equals("r") || visitType.equals("t")) && bill.getBillRatePlanId() == null) {
			bill.setBillRatePlanId("ORG0001");
		}

		if ( bill.getIs_tpa() && planDetails != null ){
			bill.setBillDiscountCategory(planDetails.get("discount_plan_id") != null ? (Integer)planDetails.get("discount_plan_id") : 0) ;
		}

		billDAO.createBill(bill);

		if (bill.getBillType().equals("C") && restrictionType.equals("N"))
			billDAO.setPrimaryBillConditional(billNo);

		map.put("BILLNO", billNo);

		logger.debug("Bill creation..."+map);

		return map;
	}

	public String updateBillStatus(Bill bill, String newStatus, String paymentStatus,  
	    String dischargeStatus, java.sql.Timestamp finalizedDate, String userId, 
	    boolean paymentForceClose, boolean claimForceClose) throws SQLException, Exception {
		return updateBillStatus(bill, newStatus, paymentStatus, dischargeStatus, finalizedDate, userId,
		    false, paymentForceClose, claimForceClose);
	}

	public String updateBillStatus(Bill bill, String newStatus, String paymentStatus, 
	    String dischargeStatus, java.sql.Timestamp finalizedDate, String userId, 
	    boolean removePrimary, boolean paymentForceClose, boolean claimForceClose)
		throws SQLException, Exception {
		Connection con = null;
		try{
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			return updateBillStatus(con, bill, newStatus, paymentStatus, dischargeStatus, finalizedDate, 
			    userId, removePrimary, paymentForceClose, claimForceClose);
		} finally {
			DataBaseUtil.commitClose(con, true);
		}
	}

	public String updateBillStatus(Connection con, Bill bill, String newStatus, String paymentStatus,
	    String dischargeStatus, java.sql.Timestamp finalizedDate, String userId,
	    boolean removePrimary, boolean paymentForceClose, boolean claimForceClose)
		throws SQLException, Exception {

		StringBuilder sb = new StringBuilder();
		boolean allSuccess = false;
		boolean discountProblems = false;

		try {
			ChargeDAO chargeDAOObj = new ChargeDAO(con);
			BillDAO billDAO = new BillDAO(con);

	updateLabel: {

				/*
				 * If status is changing to closed, or finalized, then update the payment amounts
				 */
				if (newStatus.equals(Bill.BILL_STATUS_CLOSED)
						|| newStatus.equals(Bill.BILL_STATUS_FINALIZED)) {
					List allBillCharges = chargeDAOObj.getBillCharges(bill.getBillNo());
					boolean success = false;

					for (int i=0;i<allBillCharges.size(); i++) {
						ChargeDTO c = (ChargeDTO) allBillCharges.get(i);

						if (c.getStatus().equals("X")) {
							success = true;
						} else {
							success = PaymentEngine.updateAllPayoutAmounts(con, c.getChargeId());
						}
						if (!success) {
							sb.append("<br/>Discount in " + c.getChargeHeadName()
									+ "/" + c.getActDescription() + " Dated: "
									+ c.getPostedDate()
									+ " is greater than one or more doctor payment amounts. ");
							discountProblems = true;
						}
					}
				}

				if (!discountProblems) {

					java.sql.Timestamp closedDate = null;
					String closedBy = null;
					String finalizedBy = bill.getFinalizedBy();
					boolean isTpa = bill.getIs_tpa();


					BasicDynaBean billAmtBean = billDAO.getBillAmounts(bill.getBillNo());
					if (billAmtBean != null) {
						BigDecimal billAmt = (BigDecimal) billAmtBean.get("total_amount");
						BigDecimal taxAmt = (BigDecimal) billAmtBean.get("total_tax");
						BigDecimal claimAmt = ((BigDecimal) billAmtBean.get("total_claim"));
						BigDecimal claimTaxAmt = (BigDecimal) billAmtBean.get("total_claim_tax");
						BigDecimal priClaimAmt = ((BigDecimal) billAmtBean.get("primary_total_claim"));
						BigDecimal secClaimAmt = ((BigDecimal) billAmtBean.get("secondary_total_claim"));
						BigDecimal dedn = (BigDecimal) billAmtBean.get("insurance_deduction");

						BigDecimal totalReceipts = (BigDecimal) billAmtBean.get("total_receipts");
						BigDecimal depositSetOff = (BigDecimal) billAmtBean.get("deposit_set_off");
						BigDecimal pointsRedeemedAmt = (BigDecimal) billAmtBean.get("points_redeemed_amt");

						BigDecimal totalSponsorReceipts = BigDecimal.ZERO;
						BigDecimal primaryTotalSponsorReceipts = billAmtBean.get("primary_total_sponsor_receipts") == null
							? BigDecimal.ZERO : (BigDecimal) billAmtBean.get("primary_total_sponsor_receipts");
						BigDecimal secondaryTotalSponsorReceipts = billAmtBean.get("secondary_total_sponsor_receipts") == null
							? BigDecimal.ZERO : (BigDecimal) billAmtBean.get("secondary_total_sponsor_receipts");

						totalSponsorReceipts = totalSponsorReceipts.add(primaryTotalSponsorReceipts).add(secondaryTotalSponsorReceipts);
						BigDecimal totalClaimRecd = billAmtBean.get("claim_recd_amount") == null ? BigDecimal.ZERO : (BigDecimal) billAmtBean.get("claim_recd_amount");
						//BigDecimal claimReturnAmt = billAmtBean.get("total_claim_return") == null ? BigDecimal.ZERO : (BigDecimal) billAmtBean.get("total_claim_return");
						BigDecimal claimReturnAmt = BigDecimal.ZERO;

						BigDecimal patientAmt = billAmt.add(taxAmt).subtract(priClaimAmt).subtract(secClaimAmt).subtract(claimTaxAmt);
						BigDecimal patientCredits = totalReceipts.add(depositSetOff).add(pointsRedeemedAmt);
						BigDecimal insAmt = priClaimAmt.add(secClaimAmt).add(claimTaxAmt);
						
						BasicDynaBean creditNoteBean = billDao.getCreditNoteDetails(bill.getBillNo());
						
						if(null != creditNoteBean){
							BigDecimal patientCreditnoteAmt = null != creditNoteBean.get("total_pat_amt") ? (BigDecimal)creditNoteBean.get("total_pat_amt") : BigDecimal.ZERO;
							BigDecimal sponsorCreditNoteAmt = null != creditNoteBean.get("total_claim") ? (BigDecimal)creditNoteBean.get("total_claim") : BigDecimal.ZERO;
							
							patientCredits = patientCredits.subtract(patientCreditnoteAmt);
							totalSponsorReceipts = totalSponsorReceipts.subtract(sponsorCreditNoteAmt);
						}

						if (newStatus.equals(Bill.BILL_STATUS_CLOSED)) {

							// Payment status check
							if (!paymentForceClose) {
								if (patientAmt.compareTo(patientCredits)!= 0) {
									sb.append("Bill status update failed: Bill amount (" + patientAmt +
										") is not equal to receipts total amount (" + patientCredits + ")" +
										(dedn.compareTo(BigDecimal.ZERO) != 0 ? " (or) check patient deduction." : "")) ;
									break updateLabel;
								}
							}

							// Claim status check
							if (isTpa && !claimForceClose) {
							  BigDecimal totalTpaReceipts = BigDecimal.ZERO;
							  if(totalSponsorReceipts.compareTo(BigDecimal.ZERO) == 0) {
							    totalTpaReceipts = totalClaimRecd;
							  } else {
							    totalTpaReceipts = totalSponsorReceipts;
							  }
								if (insAmt.add(claimReturnAmt).compareTo(BigDecimal.ZERO) > 0) {
									if (insAmt.add(claimReturnAmt).compareTo(totalTpaReceipts) != 0) {
										sb.append("Bill status update failed: Claim amount (" + insAmt.add(claimReturnAmt) +
											") is not equal to sponsor received amount (" + totalTpaReceipts + ")");
										break updateLabel;
									}
								}
							}

							closedDate = com.bob.hms.common.DateUtil.getCurrentTimestamp();
							closedBy = userId;

						}
					}

					Timestamp lastFinalizedAt = bill.getLastFinalizedAt();
					//	Set the finalized by when the new status is finalized or closed
					// (And) the Bill is a Credit bill or Bill now with TPA (And) the finalized by is empty.
					if ((newStatus.equals(Bill.BILL_STATUS_FINALIZED)
							|| (newStatus.equals(Bill.BILL_STATUS_CLOSED)
								&& (bill.getBillType().equals(Bill.BILL_TYPE_CREDIT)
										|| (bill.getBillType().equals(Bill.BILL_TYPE_PREPAID) && bill.getIs_tpa()))))
							&& (bill.getFinalizedBy() == null || bill.getFinalizedBy().equals(""))) {
            finalizedBy = userId;
					}
          if (newStatus.equals(Bill.BILL_STATUS_FINALIZED)
              || (newStatus.equals(Bill.BILL_STATUS_CLOSED) && !bill.getStatus().equals(Bill.BILL_STATUS_FINALIZED))) {
            lastFinalizedAt = DateUtil.getCurrentTimestamp();
          }
					if (newStatus.equals(Bill.BILL_STATUS_CANCELLED)) {
						closedBy = userId;
						if (lastFinalizedAt != null) {
						  lastFinalizedAt = DateUtil.getCurrentTimestamp();
						}
					}

					boolean success = billDAO.updateBillStatus(
					    userId, bill.getBillNo(), newStatus, paymentStatus, dischargeStatus,
							finalizedDate, closedDate, closedBy, finalizedBy, lastFinalizedAt);

					if (removePrimary)
						billDAO.updatePrimaryBill(bill.getBillNo(), "N");

					if(dischargeStatus.equals("Y") && success)
						success = success && MRDCaseFileIssueDAO.setMRDCaseFileStatus(con, bill.getMrno(),
								MRDCaseFileIssueDAO.MRD_CASE_FILE_STATUS_ON_DISCHARGE);

					int[] planIds = insPlanDAO.getPlanIds(bill.getVisitId());

					// Update claim if all bills are closed
					List<String> claims = new ArrayList<String>();
					if(null != planIds && bill.getIs_tpa()){
						for(int i=0; i < planIds.length; i++){
							Map<String,Object> keys = new HashMap<String, Object>();
							keys.put("plan_id", planIds[i]);
							keys.put("bill_no",  bill.getBillNo());
							keys.put("visit_id", bill.getVisitId());
							BasicDynaBean bean = billClaimDAO.findByKey(keys);
							if(null != bean)
								claims.add((String)bean.get("claim_id"));
						}
					}

					if (bill.getIs_tpa()) {
						String bno = bill.getBillNo();
						if (newStatus.equals(Bill.BILL_STATUS_CLOSED) ) {
							for (String claimId : claims) {
								billClaimDAO.closeBillClaim(con, bno, claimId);
							}
						} else {
							// if only the claim status was updated, close the claim
							Bill b = billDAO.getBill(bill.getBillNo());
							for (String claimId : claims) {
								if (isClaimClosed(con, b, claimId)) {
									billClaimDAO.closeBillClaim(con, bill.getBillNo(), claimId);
								}
							}
						}
						String error = new RemittanceAdviceDAO().updateAllClaimsStatus(con, claims, userId);
						if (error == null) success = success && true;
					}
					if (success)
						allSuccess = true;
				}
			}
		} finally {
			DataBaseUtil.commitClose(null, allSuccess);
		}
		return sb.toString();
	}

	private boolean isClaimClosed(Connection con, Bill b, String claimId) throws SQLException {
		String billNo = (null != b) ? b.getBillNo() : "";
		BasicDynaBean pbc = billClaimDAO.getPrimaryBillClaim(billNo);
		if (null != claimId && claimId.equals(pbc.get("claim_id")) && b != null) {
			return "R".equals(b.getPrimaryClaimStatus());
		}
		BasicDynaBean sbc = billClaimDAO.getSecondaryBillClaim(billNo);
		if (null != claimId && claimId.equals(sbc.get("claim_id")) && b != null) {
			return "R".equals(b.getSecondaryClaimStatus());
		}
		return false;
	}
	private static String CHECK_SPONSOR_BILL = "SELECT COALESCE(sponsor_bill_no,receipt_no) AS sponsor_bill_receipt   " +
			" FROM bill b "
			+ "LEFT JOIN bill_receipts br ON(br.bill_no = b.bill_no) "
			+ "JOIN receipts r ON (r.receipt_id = br.receipt_no AND r.receipt_type = 'R' AND r.tpa_id IS NOT NULL) " +
			" WHERE b.bill_no  = ? LIMIT 1";

  public String getSponsorBillOrReceipt(String billNo) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    String sponsor_bill_receipt = null;

    try (PreparedStatement ps = con.prepareStatement(CHECK_SPONSOR_BILL);) {
      ps.setString(1, billNo);
      try (ResultSet rs = ps.executeQuery();) {
        if (rs.next() && rs.getString(1) != null && !rs.getString(1).equals("")) {
          sponsor_bill_receipt = rs.getString(1);
        }
      }
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
    return sponsor_bill_receipt;
  }

	public String getSponsorBillOrReceipt(Connection con, String billNo) throws SQLException {
    String sponsor_bill_receipt = null;
    ResultSet rs = null;
    try (PreparedStatement ps = con.prepareStatement(CHECK_SPONSOR_BILL);) {
      ps.setString(1, billNo);
      rs = ps.executeQuery();
      if(rs.next() && rs.getString(1)!=null && !rs.getString(1).equals("")) {
        sponsor_bill_receipt = rs.getString(1);
      }
    } finally {
      if (rs != null) { rs.close(); }
    }
    return sponsor_bill_receipt;
  }

	public static boolean billProcessDRG(Connection con, String billNo) throws Exception {
		Map drgCodeMap = ChargeDAO.getBillDRGCode(con, billNo);
		Map<String, String> drgMap = null;
		boolean hasDRGCode = false;
		boolean success = true;
		if (drgCodeMap != null && drgCodeMap.get("drg_charge_id") != null) {
			hasDRGCode = true;
		}

		if (hasDRGCode) {
	    	drgMap = new HashMap<String, String>();
			drgMap.put("act_rate_plan_item_code", (String)drgCodeMap.get("drg_code"));
	    	drgMap.put("charge_id", (String)drgCodeMap.get("drg_charge_id"));
	    	drgMap.put("bill_no", (String)drgCodeMap.get("drg_bill_no"));

	    	success = new DRGCalculator().processDRG(billNo, (String)drgCodeMap.get("drg_bill_no"));
			if (!success) return success;
		}
		return success;
	}


	public static boolean processDRG(Connection con, Map drgMap) throws Exception {

		String drgCode = (drgMap.get("act_rate_plan_item_code") != null && !drgMap.get("act_rate_plan_item_code").equals(""))
							? (String)drgMap.get("act_rate_plan_item_code") : null;
		String drgChargeId = (drgMap.get("charge_id") != null && !drgMap.get("charge_id").equals(""))
							? (String)drgMap.get("charge_id") : null;

		if ((drgCode == null || drgCode.trim().equals("")) && (drgChargeId == null || drgChargeId.trim().equals(""))) {
			return true;
		}
		
		boolean success = new DRGCalculator().processDRG((String)drgMap.get("bill_no"), (String)drgMap.get("act_rate_plan_item_code"));
		
		/*

		String drgBillNo = (String)drgMap.get("bill_no");
		String userId = (String)RequestContext.getSession().getAttribute("userid");

		BillDAO billDAO = new BillDAO(con);
		ChargeDAO chargeDAOObj = new ChargeDAO(con);
		Bill bill = billDAO.getBill(drgBillNo);
		String patientId = bill.getVisitId();
		String visitType = bill.getVisitType();
		boolean isTpa = bill.getIs_tpa();
		DRGService drgService = new DRGService();
		if (!bill.getStatus().equals("A")) {
			return true;
		}

		BasicDynaBean visitBean = visitDetailsDao.findByKey(con, "patient_id", patientId);
		//int planId = visitBean.get("plan_id") == null ? 0 : (Integer)visitBean.get("plan_id");
		// TODO : Change this to a call to getPrimaryPlan
		GenericDAO pipdao = new GenericDAO("patient_insurance_plans");
		Map keymap = new HashMap();
		keymap.put("patient_id", patientId);
		keymap.put("priority", new Integer(1));
		BasicDynaBean pipbean = pipdao.findByKey(con, keymap);
		int planId = (Integer)pipbean.get("plan_id");

		String mainVisitId = (String)visitBean.get("main_visit_id");
		boolean useDRG = VisitDetailsDAO.visitUsesDRG(con, patientId);

		drgCode = (useDRG) ? drgCode : "";

		GenericDAO chargeDAO = new GenericDAO("bill_charge");
		VisitDetailsDAO visitDAO = visitDetailsDao;

		String drgOutlierChargeId = chargeDAOObj.getChargeUsingChargeHead(drgBillNo, "OUTDRG");

		boolean success = true;

		// Cancel DRG Charge if exists
		if ((drgCode == null || drgCode.trim().equals("")) && drgChargeId != null) {

			success = updateDRGClaimCharges(con, userId, bill, true);
			if (!success) return success;

			success = EditVisitDetailsDAO.updateBillChargesForPolicy(con, patientId, isTpa, drgBillNo, planId);
			if (!success) return success;

			success = updateReturns(con, userId, bill, mainVisitId);
			if (!success) return success;

			success = chargeDAOObj.cancelCharge(con, drgChargeId);
			if (!success) return success;

			visitBean.set("drg_code", "");
			int i = visitDAO.updateWithName(con, visitBean.getMap(), "patient_id");
			success = (i > 0);
			if (!success) return success;

			drgService.updateClaimAmount(con, drgChargeId);

			if (drgOutlierChargeId != null) {
				success = chargeDAOObj.cancelCharge(con, drgOutlierChargeId);
				if (success) drgService.updateClaimAmount(con, drgOutlierChargeId);
				if (!success) return success;
			}
		}else {

			success = updateDRGClaimCharges(con, userId, bill, false);
			if (!success) return success;

			BasicDynaBean planBean = planMasterDao.findByKey("plan_id", planId);
			BigDecimal baseRate = (BigDecimal)planBean.get("base_rate");
			BigDecimal gapAmount = (BigDecimal)planBean.get("gap_amount");
			BigDecimal marginalPercent = (BigDecimal)planBean.get("marginal_percent");

			BasicDynaBean drgCodeBean = new DRGCodesMasterDAO().findByKey("drg_code", drgCode);
			BigDecimal relativeWeight = (BigDecimal)drgCodeBean.get("relative_weight");
			String codeType = (String)drgCodeBean.get("code_type");

			BigDecimal totalCost = billDAO.getBillTotalAmountWithoutDRG(drgBillNo);
			BigDecimal basePayment = relativeWeight.multiply(baseRate);
			BigDecimal outlierPayment =
					((totalCost.subtract(basePayment).subtract(gapAmount))
							.multiply(marginalPercent)).divide(new BigDecimal(100));
			BigDecimal totalPayment = ConversionUtils.setScale(basePayment);

			if (outlierPayment.compareTo(BigDecimal.ZERO) > 0)
				outlierPayment = ConversionUtils.setScale(outlierPayment);
			else
				outlierPayment = BigDecimal.ZERO;

			BigDecimal totalRate = BigDecimal.ZERO.subtract(totalCost.subtract(basePayment));
			totalRate = ConversionUtils.setScale(totalRate);
			BigDecimal totalInsAmount = totalPayment;

			String drgRemarks = "Base Payment.     BaseRate : " +baseRate+"  RelativeWeight : "+relativeWeight;

			String outlierRemarks = "Excess Payment over base payment.       "+
								"  GapAmount : "+gapAmount+"   MarginalPercent : "+marginalPercent;

			if (drgChargeId == null || drgChargeId.trim().equals("")) {
				ChargeDTO chargeDTO = new ChargeDTO("DRG", "MARDRG",
						BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO,
						null, null, "Calculated DRG Payment", null, true,
						planId, 0, -1, visitType, patientId, true);
				drgChargeId = chargeDAOObj.getNextChargeId();
				chargeDTO.setBillNo(drgBillNo);
				chargeDTO.setChargeId(drgChargeId);
				chargeDTO.setActRemarks(drgRemarks);
				chargeDTO.setPostedDate(getPostedDateForDRGCharge(visitBean));

				success = chargeDAOObj.insertCharge(chargeDTO);

				if (success) {
					success = drgService.createMarginClaim(con, drgBillNo, drgChargeId);
				}
				if (!success) return success;
			}

			if (drgOutlierChargeId == null && outlierPayment.compareTo(BigDecimal.ZERO) > 0) {
				ChargeDTO chargeDTO = new ChargeDTO("DRG", "OUTDRG",
						BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO,
						null, null, "Calculated Outlier Payment", null, true,
						planId, 0, -1, visitType, patientId, true);
				drgChargeId = chargeDAOObj.getNextChargeId();
				chargeDTO.setBillNo(drgBillNo);
				chargeDTO.setChargeId(drgChargeId);
				chargeDTO.setActRemarks(outlierRemarks);
				chargeDTO.setPostedDate(getPostedDateForDRGCharge(visitBean));

				success = chargeDAOObj.insertCharge(chargeDTO);
				if (success) {
					success = drgService.createOutlierClaim(con, drgBillNo, drgChargeId);
				}
				if (!success) return success;
			}

			visitBean.set("drg_code", drgCode);
			int j = visitDAO.updateWithName(con, visitBean.getMap(), "patient_id");
			success = (j > 0);
			if (!success) return success;

			List<BasicDynaBean> charges = chargeDAO.findAllByKey(con, "bill_no", drgBillNo);

			if (!charges.isEmpty()) {
				for (BasicDynaBean chrg : charges) {

					if (((String)chrg.get("charge_head")).equals("MARDRG")) {

						chrg.set("act_rate_plan_item_code", drgCode);
						chrg.set("code_type", codeType);
						chrg.set("orig_rate", totalRate);
						chrg.set("act_rate", totalRate);
						chrg.set("act_quantity", BigDecimal.ONE);
						chrg.set("amount", totalRate);
						chrg.set("insurance_claim_amount", totalInsAmount);
						chrg.set("act_remarks", drgRemarks);
						chrg.set("status", "A");
						chrg.set("is_claim_locked", Boolean.TRUE);
						chrg.set("include_in_claim_calc", Boolean.FALSE);
					}

					int i = chargeDAO.updateWithName(con, chrg.getMap(), "charge_id");
					success = (i > 0);
					if (success) {
						drgService.updateClaimAmount(con, chrg);
					}
					if (!success) return success;

					if (((String)chrg.get("charge_head")).equals("OUTDRG")) {
						if (outlierPayment.compareTo(BigDecimal.ZERO) > 0) {
							chrg.set("act_rate_plan_item_code", "99");
							chrg.set("code_type", "Service Code");
							chrg.set("orig_rate", outlierPayment);
							chrg.set("act_rate", outlierPayment);
							chrg.set("act_quantity", BigDecimal.ONE);
							chrg.set("amount", outlierPayment);
							chrg.set("insurance_claim_amount", outlierPayment);
							chrg.set("act_remarks", outlierRemarks);
							chrg.set("status", "A");
							chrg.set("is_claim_locked", Boolean.TRUE);
							chrg.set("include_in_claim_calc", Boolean.FALSE);

							i = chargeDAO.updateWithName(con, chrg.getMap(), "charge_id");
							success = (i > 0);
							if (success) drgService.updateClaimAmount(con, chrg);

							if (!success) return success;

						}else {
							success = chargeDAOObj.cancelCharge(con, (String)chrg.get("charge_id"));
							if (success) drgService.updateClaimAmount(con, chrg);
							if (!success) return success;
						}
					}
				}
			}
		}
		drgMap.put("charge_id", drgChargeId);
		*/
		return success;
	} 

	private static java.sql.Timestamp getPostedDateForDRGCharge(BasicDynaBean visitBean) throws Exception {

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

	private static boolean updateDRGClaimCharges(Connection con, String userId, Bill bill, boolean cancelDRGCode) throws Exception {

		String drgBillNo = bill.getBillNo();
		boolean success = true;
		DRGService drgService = new DRGService();
		List<BasicDynaBean> charges = billChargeDAO.findAllByKey(con, "bill_no", drgBillNo);

		if (!charges.isEmpty()) {
			for (BasicDynaBean chrg : charges) {
				String chargeHead = (String)chrg.get("charge_head");
				String chargeId = (String)chrg.get("charge_id");
				boolean hasActivity = (Boolean)chrg.get("hasactivity");

				if (hasActivity && chargeHead.equals("PHCMED")) {

					BasicDynaBean salebean = saleMainDAO.findByKey(con, "charge_id", chargeId);
					if (salebean != null) {
						String saleId = (String)salebean.get("sale_id");

						Map<String, Comparable> fields = new HashMap<String, Comparable>();
						fields.put("sale_id", saleId);
						fields.put("insurance_claim_amt", BigDecimal.ZERO);
						fields.put("return_insurance_claim_amt", BigDecimal.ZERO);

						int i = saleDetailDAO.updateWithName(con, fields, "sale_id");
						success = (i > 0);
						if(success) drgService.updateSalesClaimAmount(con,saleId);
						if (!success) return success;
					}
				}

				chrg.set("username", userId);

				if (((String)chrg.get("charge_head")).equals("MARDRG") || ((String)chrg.get("charge_head")).equals("OUTDRG")) {
					if (cancelDRGCode) {
						chrg.set("act_rate_plan_item_code", null);
						chrg.set("code_type", null);
						chrg.set("act_rate", BigDecimal.ZERO);
						chrg.set("insurance_claim_amount", BigDecimal.ZERO);
						chrg.set("return_insurance_claim_amt", BigDecimal.ZERO);
					}else {
						chrg.set("insurance_claim_amount", chrg.get("insurance_claim_amount"));
						chrg.set("return_insurance_claim_amt", BigDecimal.ZERO);
					}
				}else {
					chrg.set("insurance_claim_amount", BigDecimal.ZERO);
					chrg.set("return_insurance_claim_amt", BigDecimal.ZERO);
				}

				chrg.set("is_claim_locked", new Boolean(!cancelDRGCode));
				int i = billChargeDAO.updateWithName(con, chrg.getMap(), "charge_id");
				success = (i > 0);
				if (success) drgService.updateClaimAmount(con, chrg);
				if (!success) return success;
			}
		}
		return success;
	}

	public static boolean updateReturns(Connection con, String userId,
			String billNo, String mainVisitId) throws Exception {
		MedicineSalesDAO mdao = new MedicineSalesDAO(con);
		ChargeDAO cdao = new ChargeDAO(con);
		MedicineSalesBO medbo = new MedicineSalesBO();

		boolean success = true;

		BasicDynaBean visitBean = visitDetailsDao.findByKey("patient_id", mainVisitId);
		int planid = (visitBean != null && visitBean.get("plan_id") != null) ? (Integer)visitBean.get("plan_id") : 0;

		List<BasicDynaBean> charges = billChargeDAO.findAllByKey("bill_no", billNo);
		ArrayList<MedicineSalesDTO> saleItemsForReturns = new ArrayList<MedicineSalesDTO>();
		List<ChargeDTO> updateChargeList = new ArrayList<ChargeDTO>();
		List<String> chargesToUpdate = new ArrayList<String>();

		if (!charges.isEmpty()) {
			for (BasicDynaBean chrg : charges) {
				String chargeHead = (String)chrg.get("charge_head");
				String chargeId = (String)chrg.get("charge_id");

				if (chargeHead.equals("PHCRET")) {

					BasicDynaBean salebean = saleMainDAO.findByKey("charge_id", chargeId);
					String saleId = (String)salebean.get("sale_id");

					BasicDynaBean returnSale = saleMainDAO.findByKey("sale_id", saleId);
					String returnBillNo = (returnSale != null && returnSale.get("return_bill_no") != null) ? (String)returnSale.get("return_bill_no") : null;
					int storeid = (returnSale != null && returnSale.get("store_id") != null) ? (Integer)returnSale.get("store_id") : 0;
					List<BasicDynaBean> returnItems = saleDetailDAO.findAllByKey("sale_id", saleId);

					BasicDynaBean storeDetails = StoreDAO.findByStore(storeid);
					int centerId = (Integer)storeDetails.get("center_id");
					String healthAuthority = CenterMasterDAO.getHealthAuthorityForCenter(centerId);

					List<BasicDynaBean> salesForReturn = null;
					if (returnBillNo != null)
						 salesForReturn = MedicineSalesDAO.getSoldItems(con, returnBillNo, planid, healthAuthority);
					else
						 salesForReturn = MedicineSalesDAO.getVisitSoldItems(con, mainVisitId, storeid, planid);

					for (BasicDynaBean item : returnItems) {
						MedicineSalesDTO saleItem = medbo.getSaleItemDetail(con, (Integer)item.get("sale_item_id"));
						MedicineSalesBO.setSaleItemsForReturns(saleItem, salesForReturn,
								saleItemsForReturns, chargesToUpdate);
					}

				}else if (chargeHead.equals("INVRET")) {

					BasicDynaBean issuebean = StockUserIssueDAO.getIssueCharge(con, chargeId);
					ChargeDTO chargeDTO = cdao.getCharge(chargeId);
					StockUserReturnDAO.setIssueItemsForReturns(chargeDTO.getActQuantity().negate(),
							chargeDTO.getInsuranceClaimAmount().negate(),chargeDTO.getAmount().negate()
							, updateChargeList, issuebean);
				}
			}
		}

		success = mdao.updateSaleItemsReturnInsAmt(saleItemsForReturns);
		if (!success) return success;

		// Update the return qty, return amount and return claim amt for the charge of the sale id
		if (chargesToUpdate != null && chargesToUpdate.size() > 0) {
			for (String chargeId : chargesToUpdate) {

				ChargeDTO chdto = new ChargeDTO();

				BasicDynaBean salebean = saleMainDAO.findByKey("charge_id", chargeId);
				String saleId = (String)salebean.get("sale_id");

				List<BasicDynaBean> itemList = mdao.getSalesItemList(saleId);

				BigDecimal amt    = BigDecimal.ZERO;
				BigDecimal insamt = BigDecimal.ZERO;
				BigDecimal qty    = BigDecimal.ZERO;

				for (BasicDynaBean saleitem : itemList) {
					amt    = amt.add((BigDecimal)saleitem.get("return_amt"));
					insamt = insamt.add((BigDecimal)saleitem.get("return_insurance_claim_amt"));
					qty    = qty.add((BigDecimal)saleitem.get("return_qty"));
				}

				chdto.setReturnInsuranceClaimAmt(insamt);
				chdto.setReturnAmt(amt);
				chdto.setReturnQty(qty);
				chdto.setChargeId(chargeId);

				updateChargeList.add(chdto);
			}
		}

		success = cdao.updateSaleChargesReturnInsAmt(updateChargeList);
		if (!success) return success;

		return success;
	}

	public String perdiemProcess(String billNo, boolean reProcess,
					boolean perdiemRecalc) throws SQLException,IOException {

		Connection con = null;
		boolean success = false;
		String err = null;
		PerdiemService perdiemService = new PerdiemService();

		try {

			perdiem: {
				con = DataBaseUtil.getConnection();
				con.setAutoCommit(false);

				BillDAO billDAO = new BillDAO(con);
				ChargeDAO chargeDAOObj = new ChargeDAO(con);

				Bill bill = billDAO.getBill(billNo);
				String visitId = bill.getVisitId();
				boolean isTpa = bill.getIs_tpa();
				String ratePlan = bill.getBillRatePlanId();

				boolean usePerdiem = VisitDetailsDAO.visitUsesPerdiem(visitId);
				BasicDynaBean visitBean = visitDetailsDao.findByKey(con, "patient_id", visitId);
				String perdiemCode = visitBean.get("per_diem_code") != null ? (String)visitBean.get("per_diem_code") : null;
				String bedType = (String)visitBean.get("bed_type");
				//int planId = visitbean.get("plan_id") == null ? 0 : (Integer)visitbean.get("plan_id");
				int planId = getPrimaryPlanId(con, visitId);
				String visitType = (String)visitBean.get("visit_type");

				Map perdiemMap = ChargeDAO.getBillPerdiemCharge(billNo);

				String perdiemChargeId = (perdiemMap.get("perdiem_charge_id") != null && !perdiemMap.get("perdiem_charge_id").equals(""))
									? (String)perdiemMap.get("perdiem_charge_id") : null;

				// Visit does not uses perdiem, no perdiem code, no perdiem charge so no further perdiem process.
			    if ((!usePerdiem && (StringUtils.isEmpty(perdiemChargeId)))
			          || (StringUtils.isEmpty(perdiemCode))) {
			        return null;
			    }

				BasicDynaBean perdiemBean = new PerDiemCodesDAO().getBillPerdiemCharge(ratePlan, bedType, perdiemCode);
				String includedServGrps = perdiemBean != null && (perdiemBean.get("service_groups_incl") != null && !perdiemBean.get("service_groups_incl").equals(""))
									? (String)perdiemBean.get("service_groups_incl") : "";
									
				String[] servGrpIds = new String[0];
				if (StringUtils.isNotBlank(includedServGrps)) {
					servGrpIds = includedServGrps.split(",");
				}
				
				//For Bed charges user will be auto_update as session is null.
				HttpSession session	= RequestContext.getSession();
				String userid = session == null ? "auto_update" : (String)session.getAttribute("userId");
				userid = userid != null ? userid.toString() : "auto_update";

				Timestamp currentTimestamp = DateUtil.getCurrentTimestamp();

				BasicDynaBean billAmounts = billDAO.getBillAmounts(billNo);
				BigDecimal paidReceiptsAmount = (BigDecimal)billAmounts.get("total_receipts");

				perdiemCode = (usePerdiem) ? perdiemCode : "";

				// Cancel Perdiem Charge if exists
				if ((perdiemCode == null || perdiemCode.trim().equals("")) && perdiemChargeId != null) {

					success = updatePerdiemClaimCharges(con, userid, bill, paidReceiptsAmount, servGrpIds, null, true);
					if (!success) {
						err = "Claim Charges Updation failure.";
						break perdiem;
					}

					success = EditVisitDetailsDAO.updateBillChargesForPolicy(con, visitId, isTpa, billNo, planId);
					if (!success) {
						err = "Bill Charges Updation failure.";
						break perdiem;
					}

					success = updateReturns(con, userid, billNo, visitId);
					if (!success) {
						err = "Sales/Inventory Returns Updation failure.";
						break perdiem;
					}

					success = chargeDAOObj.cancelCharge(con, perdiemChargeId);
					if (!success) {
						err = "Perdiem Charge cancellation failure.";
						break perdiem;
					}

					visitBean.set("per_diem_code", "");
					int i = visitDetailsDao.updateWithName(con, visitBean.getMap(), "patient_id");
					success = (i > 0);
					if (!success) {
						err = "Visit perdiem updation failure.";
						break perdiem;
					}

					perdiemService.updateClaimAmount(con, perdiemChargeId);
				}else {

					if (!bill.getStatus().equals(Bill.BILL_STATUS_OPEN)) {
						err = "Bill is not Open to add/edit perdiem code.";
						break perdiem;
					}

					if (bill.getPaymentStatus().equals(Bill.BILL_PAYMENT_PAID)) {
						err = "Bill payment status is paid to add/edit perdiem code.";
						break perdiem;
					}

					if (!bill.getIs_tpa()) {
						err = "Bill is not connected to sponsor to add/edit perdiem code.";
						break perdiem;
					}

					if (bill.getIsPrimaryBill().equals("N")) {
						err = "Bill is not a primary bill to add/edit perdiem code.";
						break perdiem;
					}

					if (!bill.getIs_tpa()) {
						err = "Bill is not connected to sponsor to add/edit perdiem code.";
						break perdiem;
					}

					Map<String,BigDecimal> excludedCoPayMap = new HashMap<String, BigDecimal>();

					if (reProcess) {
						success = updatePerdiemClaimCharges(con, userid, bill, paidReceiptsAmount, servGrpIds, excludedCoPayMap, false);
						if (!success) {
							err = "Claim Charges Updation failed.";
							break perdiem;
						}
					}else if (perdiemRecalc) {
						success = calcExcludedCopay(con, bill, servGrpIds, excludedCoPayMap);
						if (!success) {
							err = "Claim excluded co-pay calculation failed.";
							break perdiem;
						}
					}

					BigDecimal excludedCoPay =
						(excludedCoPayMap != null && excludedCoPayMap.get("ExcludedCopay") != null)
						? (BigDecimal)excludedCoPayMap.get("ExcludedCopay") : BigDecimal.ZERO;

					BasicDynaBean planBean = planMasterDao.findByKey("plan_id", planId);
					BigDecimal copayPer = planBean.get("perdiem_copay_per") != null
											? (BigDecimal)planBean.get("perdiem_copay_per") : BigDecimal.ZERO;
					BigDecimal copayAmount = planBean.get("perdiem_copay_amount") != null
											? (BigDecimal)planBean.get("perdiem_copay_amount") : BigDecimal.ZERO;

					BigDecimal perdiemCharge = (BigDecimal)perdiemBean.get("charge");
					String codeType = (String)perdiemBean.get("code_type");

					BigDecimal totalCopay = billDAO.getBillTotalCopayWithoutPDM(billNo);

					BigDecimal perdiemPlanCoPay = ConversionUtils.setScale(
							copayPer.multiply(perdiemCharge).divide(new BigDecimal("100"),BigDecimal.ROUND_HALF_UP));

					// Consider minimum of plan perdiem copay amount (or) percentage of perdiem copay.
					perdiemPlanCoPay = (perdiemPlanCoPay.compareTo(BigDecimal.ZERO) != 0) ?
										(copayAmount.compareTo(BigDecimal.ZERO) != 0 ? perdiemPlanCoPay.min(copayAmount) : perdiemPlanCoPay)
										: copayAmount;

					// Perdiem charge is the claim amount.
					BigDecimal perdiemClaimAmount = perdiemCharge.subtract(perdiemPlanCoPay);

					BigDecimal perdiemRate = perdiemClaimAmount.subtract(totalCopay.subtract(perdiemPlanCoPay).subtract(excludedCoPay));

					String perdiemRemarks = "Perdiem Payment.   Charge : " +perdiemCharge+"  CoPay : "+perdiemPlanCoPay+"  ExcludedCoPay : "+excludedCoPay;

					if (perdiemChargeId == null || perdiemChargeId.trim().equals("")) {
						ChargeDTO chargeDTO = new ChargeDTO("PDM", "MARPDM",
								BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO,
								null, null, "Calculated Perdiem Cost", null, true,
								planId, 0, -1, visitType, visitId, true);
						perdiemChargeId = chargeDAOObj.getNextChargeId();
						chargeDTO.setBillNo(billNo);
						chargeDTO.setChargeId(perdiemChargeId);
						chargeDTO.setActRemarks(perdiemRemarks);

						success = chargeDAOObj.insertCharge(chargeDTO);
						if (!success) {
							err = "Perdiem Charge Insertion failed.";
							break perdiem;
						}
						perdiemService.createMarginClaim(con, billNo, perdiemChargeId);
					}

					BasicDynaBean charge = billChargeDAO.findByKey(con, "charge_id", perdiemChargeId);

					if (charge != null) {
						charge.set("act_rate_plan_item_code", perdiemCode);
						charge.set("code_type", codeType);
						charge.set("orig_rate", perdiemRate);
						charge.set("act_rate", perdiemRate);
						charge.set("act_quantity", BigDecimal.ONE);
						charge.set("amount", perdiemRate);
						charge.set("insurance_claim_amount", perdiemClaimAmount);
						charge.set("act_remarks", perdiemRemarks);
						charge.set("status", "A");
						charge.set("is_claim_locked", Boolean.TRUE);
						charge.set("include_in_claim_calc", Boolean.FALSE);

						int i = billChargeDAO.updateWithName(con, charge.getMap(), "charge_id");
						success = (i > 0);
						if (success) {
							perdiemService.updateClaimAmount(con, perdiemChargeId);
						}
						if (!success) {
							err = "Perdiem Charge updation failed... "+(String)charge.get("charge_id");
							break perdiem;
						}
					}
					List<BasicDynaBean> charges = billChargeDAO.findAllByKey(con, "bill_no", billNo);

					// We could arrive here when a new charge is added to an existing perdiem processed bill
					if (!charges.isEmpty()) {
						for (BasicDynaBean chrg : charges) {
							perdiemService.updateClaimAmount(con, chrg);
						}
					}
				}

				logger.info("Bill Perdiem processing done for bill no: "+billNo);
			}// perdiem label

		}catch (Exception e) {
			success = false;
			err = "Error while bill perdiem process. Bill No: "+billNo+ " ERROR: " +err;
			logger.error(err+" : "+e.getMessage());
			return err;
		}finally {

			DataBaseUtil.commitClose(con, success);

			if (success && billNo != null && !billNo.equals("")){
				BillDAO.resetTotalsOrReProcess(billNo, false);
				String patientId = BillDAO.getVisitId(billNo);
				new SponsorBO().recalculateSponsorAmount(patientId);
			}
		}
		return null;
	}

	private boolean updatePerdiemClaimCharges(Connection con, String userId, Bill bill,
			BigDecimal paidReceiptsAmount, String[] servGrpIds,
			Map<String,BigDecimal> excludedCoPayMap, boolean cancelPerdiemCode) throws Exception {

		String perdiemBillNo = bill.getBillNo();
		boolean success = true;
		BigDecimal excludedCoPay = BigDecimal.ZERO;
		PerdiemService perdiemService = new PerdiemService();
		Map<String, String> filterMap = new HashMap<String, String>();
		filterMap.put("bill_no", perdiemBillNo);
		filterMap.put("status", "A");

		List<BasicDynaBean> charges = billChargeDAO.listAll(con, null, filterMap, "posted_date");

		if (!charges.isEmpty()) {

			if (cancelPerdiemCode) {
				for (BasicDynaBean chrg : charges) {

					String chargeHead = (String)chrg.get("charge_head");
					chrg.set("username", userId);

					if (chargeHead.equals("MARPDM")) {
						chrg.set("act_rate_plan_item_code", null);
						chrg.set("code_type", null);
						chrg.set("act_rate", BigDecimal.ZERO);
						chrg.set("insurance_claim_amount", BigDecimal.ZERO);
						chrg.set("return_insurance_claim_amt", BigDecimal.ZERO);
						chrg.set("is_claim_locked", Boolean.FALSE);
						chrg.set("include_in_claim_calc", Boolean.TRUE);
					}else {
						chrg.set("insurance_claim_amount", BigDecimal.ZERO);
						chrg.set("return_insurance_claim_amt", BigDecimal.ZERO);
						chrg.set("is_claim_locked", Boolean.FALSE);
						chrg.set("include_in_claim_calc", Boolean.TRUE);
					}

					int i = billChargeDAO.updateWithName(con, chrg.getMap(), "charge_id");
					success = (i > 0);
					if (success) {
						perdiemService.updateClaimAmount(con, chrg);
					}
					if (!success) return success;
				}
			}else {

				// Update perdiem charge based or perdiem rules.
				for (BasicDynaBean chrg : charges) {

					String chargeHead = (String)chrg.get("charge_head");
					int serviceSubGrp = (Integer)chrg.get("service_sub_group_id");
					BigDecimal amount = (BigDecimal)chrg.get("amount");
					BigDecimal insurance_claim_amount = (BigDecimal)chrg.get("insurance_claim_amount");

					if (!chargeHead.equals("MARPDM")) {

						if (!isChargeServiceSubIncluded(servGrpIds, serviceSubGrp)) {
							excludedCoPay = excludedCoPay.add(amount.subtract(insurance_claim_amount));
							continue;
						}

						BigDecimal chargeCoPay = ((BigDecimal)chrg.get("amount")).subtract((BigDecimal)chrg.get("insurance_claim_amount"));

						if (chargeCoPay.compareTo(BigDecimal.ZERO) > 0
								&& (paidReceiptsAmount.subtract(chargeCoPay)).compareTo(BigDecimal.ZERO) >= 0) {
							paidReceiptsAmount = paidReceiptsAmount.subtract(chargeCoPay);
						}

						String chargeId = (String)chrg.get("charge_id");
						boolean hasActivity = (Boolean)chrg.get("hasactivity");

						if (hasActivity && chargeHead.equals("PHCMED")) {

							BasicDynaBean salebean = saleMainDAO.findByKey(con, "charge_id", chargeId);
							if (salebean != null) {
								String saleId = (String)salebean.get("sale_id");

								Map<String, Comparable> fields = new HashMap<String, Comparable>();
								fields.put("sale_id", saleId);
								fields.put("insurance_claim_amt", BigDecimal.ZERO);
								fields.put("return_insurance_claim_amt", BigDecimal.ZERO);

								int i = saleDetailDAO.updateWithName(con, fields, "sale_id");
								success = (i > 0);
								if (!success) return success;
								if (success) {
									success =salesClaimDetailsDAO.updateSalesClaimAmt(saleId);
									if (!success) return success;
								}
							}
						}

						chrg.set("insurance_claim_amount", BigDecimal.ZERO);
						chrg.set("return_insurance_claim_amt", BigDecimal.ZERO);
						chrg.set("is_claim_locked", Boolean.TRUE);
						chrg.set("include_in_claim_calc", Boolean.FALSE);
						chrg.set("username", userId);

						int i = billChargeDAO.updateWithName(con, chrg.getMap(), "charge_id");
						success = (i > 0);
						if (success) {
							perdiemService.updateClaimAmount(con, chrg);
						}
						if (!success) return success;

					}else if (chargeHead.equals("MARPDM")) {

						chrg.set("username", userId);
						chrg.set("insurance_claim_amount", chrg.get("insurance_claim_amount"));
						chrg.set("return_insurance_claim_amt", BigDecimal.ZERO);
						chrg.set("is_claim_locked", Boolean.TRUE);
						chrg.set("include_in_claim_calc", Boolean.FALSE);

						int i = billChargeDAO.updateWithName(con, chrg.getMap(), "charge_id");
						success = (i > 0);
						if (success) {
							perdiemService.updateClaimAmount(con, chrg);
						}
						if (!success) return success;
					}
				}
			}
		}
		if (excludedCoPayMap != null)
			excludedCoPayMap.put("ExcludedCopay", excludedCoPay);
		return success;
	}


	private boolean calcExcludedCopay(Connection con, Bill bill,
		String[] servGrpIds, Map<String,BigDecimal> excludedCoPayMap) throws Exception {

		String perdiemBillNo = bill.getBillNo();
		boolean success = true;
		BigDecimal excludedCoPay = BigDecimal.ZERO;

		Map<String, String> filterMap = new HashMap<String, String>();
		filterMap.put("bill_no", perdiemBillNo);
		filterMap.put("status", "A");

		List<BasicDynaBean> charges = billChargeDAO.listAll(con, null, filterMap, "posted_date");

		if (!charges.isEmpty()) {

			for (BasicDynaBean chrg : charges) {

				String chargeHead = (String)chrg.get("charge_head");
				int serviceSubGrp = (Integer)chrg.get("service_sub_group_id");
				BigDecimal amount = (BigDecimal)chrg.get("amount");
				BigDecimal insurance_claim_amount = (BigDecimal)chrg.get("insurance_claim_amount");

				if (!chargeHead.equals("MARPDM")) {

					if (!isChargeServiceSubIncluded(servGrpIds, serviceSubGrp)) {
						excludedCoPay = excludedCoPay.add(amount.subtract(insurance_claim_amount));
						continue;
					}
				}
			}
		}

		if (excludedCoPayMap != null)
			excludedCoPayMap.put("ExcludedCopay", excludedCoPay);
		return success;
	}

	public boolean isChargeServiceSubIncluded(String[] servGrpIds, int chServiceSubGrp) throws SQLException {
		BasicDynaBean subgrpBean = subGrpDAO.findByKey("service_sub_group_id", chServiceSubGrp);
		int serviceGrp = subgrpBean != null ? (Integer)subgrpBean.get("service_group_id") : 0;

		for (int i = 0; i < servGrpIds.length; i++) {
			if (serviceGrp == Integer.parseInt(servGrpIds[i])) {
				return true;
			}
		}
		return false;
	}


	public String setBillChargesCopayZero(String billNo) throws SQLException {
		Connection con = null;
		boolean success = true;
		String err = null;

		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);

			BasicDynaBean billbean = billDao.findByKey("bill_no", billNo);
			String visitId = (String)billbean.get("visit_id");
			BasicDynaBean visitBean = visitDetailsDao.findByKey(con, "patient_id", visitId);
			int planId = visitBean.get("plan_id") == null ? 0 : (Integer)visitBean.get("plan_id");
			String visitType   = billbean.get("visit_type") != null ? (String)billbean.get("visit_type") : null;
			boolean hasPlanVisitCopayLimit = planMasterDao.hasPlanVisitCopayLimit(planId, visitType);

			if (!hasPlanVisitCopayLimit) {
				return null;
			}

			// For Bed charges user will be auto_update as session is null.
			HttpSession session	= RequestContext.getSession();
			String userid = session == null ? "auto_update" : (String)session.getAttribute("userId");
			userid = userid != null ? userid.toString() : "auto_update";

			Timestamp currentTimestamp = DateUtil.getCurrentTimestamp();

			Map<String, String> filterMap = new HashMap<String, String>();
			filterMap.put("bill_no", billNo);
			filterMap.put("status", "A");

			List<BasicDynaBean> charges = billChargeDAO.listAll(null, filterMap, null);

			if (!charges.isEmpty()) {

				for (BasicDynaBean charge : charges) {

					charge.set("mod_time", currentTimestamp);
					charge.set("username", userid);

					Boolean hasactivity = (Boolean)charge.get("hasactivity");
					BigDecimal amount = (BigDecimal)charge.get("amount");
					BigDecimal returnAmount = (BigDecimal)charge.get("return_amt");
					BigDecimal orig_insurance_claim_amount = (BigDecimal)charge.get("orig_insurance_claim_amount");
					BigDecimal insurance_claim_amount = (BigDecimal)charge.get("insurance_claim_amount");

					if (hasactivity && charge.get("charge_head").equals("PHCMED")) {
						amount = amount.add((BigDecimal)charge.get("return_amt"));
						if (orig_insurance_claim_amount.compareTo(BigDecimal.ZERO) == 0
								&& (amount.subtract(insurance_claim_amount)).compareTo(BigDecimal.ZERO) != 0)
							charge.set("orig_insurance_claim_amount", amount.subtract(insurance_claim_amount));

						charge.set("insurance_claim_amount", amount);

					}else if (hasactivity && charge.get("charge_head").equals("PHCRET")) {
						charge.set("orig_insurance_claim_amount", amount.add(returnAmount));

					}else {
						if (orig_insurance_claim_amount.compareTo(BigDecimal.ZERO) == 0
								&& (amount.subtract(insurance_claim_amount)).compareTo(BigDecimal.ZERO) != 0)
							charge.set("orig_insurance_claim_amount", insurance_claim_amount);

						charge.set("insurance_claim_amount", amount);
					}

					int i = billChargeDAO.updateWithName(con, charge.getMap(), "charge_id");
					success = (i > 0);
					if (!success)
						break;
				}
			}
		}catch (Exception e) {
			success = false;
			err = "Error while processing bill charges copay as zero for bill no: "+billNo+ " ERROR: " +err;
			logger.error(err+" : "+e.getMessage());
			return err;
		}finally {
			DataBaseUtil.commitClose(con, success);
		}
		return null;
	}

	public static void updateMultiVistDepositBalance(CreditBillForm creditBillForm) throws Exception{
		Connection con = null;
		GenericDAO packdao = new GenericDAO("packages");
		GenericDAO patPackDao = new GenericDAO("patient_packages");
		String mrNo = creditBillForm.getMrNo();
		BigDecimal depositSetOff = creditBillForm.getDepositSetOff();
		BigDecimal depositBalance = BigDecimal.ZERO;
		boolean success = false;
		try {
			Map filterMap = new HashMap();
			filterMap.put("status", "P");
			filterMap.put("mr_no", mrNo);
			List<BasicDynaBean> packageDepositDetails = patPackDao.listAll(null, filterMap, "pat_package_id");
			if(packageDepositDetails != null && packageDepositDetails.size() > 0) {
				for(int i=0;i<packageDepositDetails.size();i++) {
					BasicDynaBean packDepositBean = packageDepositDetails.get(i);
					int packageId = (Integer)packDepositBean.get("package_id");
					BasicDynaBean packageBaen = packdao.findByKey("package_id", packageId);
					if(packageBaen != null && (Boolean)packageBaen.get("multi_visit_package")) {
						Map columndata = new HashMap();
						Map keys = new HashMap();
						depositBalance = (BigDecimal)packDepositBean.get("deposit_balance");
						if(depositSetOff.compareTo(depositBalance) < 0) {
							depositBalance = depositBalance.subtract(depositSetOff);
						} else {
							depositBalance = BigDecimal.ZERO;
						}
						columndata.put("deposit_balance", depositBalance);
						keys.put("mr_no", mrNo);
						keys.put("status", "P");
						keys.put("package_id", packageId);
						success = patPackDao.update(con, columndata, keys) >= 0;
					}
				}
			}
		} finally {
			DataBaseUtil.commitClose(con, success);
		}

	}

	private static final String MULTIVISIT_BILL_QUERY = "SELECT mbv.bill_no FROM multivisit_bills_view mbv WHERE mbv.bill_no = ?";
	
	
	private static final String MULTIVISIT_BILL_QUERY_MR_NO  = " SELECT orders.bill_no  "+
			" FROM ( "+
				" SELECT bc.bill_no "+
				" FROM services_prescribed sp "+ 
				" JOIN bill_charge bc ON(bc.order_number = sp.common_order_id) "+
				" JOIN package_prescribed pp ON (sp.package_ref = pp.prescription_id) "+
				" JOIN packages p ON (pp.package_id = p.package_id AND p.multi_visit_package = true) "+
				" WHERE bc.bill_no = ? AND sp.patient_id = ?"+
				" UNION ALL "+
				" SELECT bc.bill_no "+
				" FROM tests_prescribed tp "+
				" JOIN bill_charge bc ON(bc.order_number = tp.common_order_id) "+
				" JOIN package_prescribed pp ON (tp.package_ref = pp.prescription_id) "+
				" JOIN packages p ON (pp.package_id = p.package_id AND p.multi_visit_package = true) "+
				" WHERE bc.bill_no = ? AND tp.pat_id = ? "+
				" UNION ALL "+
				" SELECT bcc.bill_no "+
				" FROM doctor_consultation dc  "+
				" JOIN bill_charge bcc ON(bcc.order_number = dc.common_order_id) "+
				" JOIN package_prescribed pp ON (dc.package_ref = pp.prescription_id) "+
				" JOIN packages p ON (pp.package_id = p.package_id AND p.multi_visit_package = true) "+
				" LEFT JOIN bill_activity_charge bac ON bac.activity_id=dc.consultation_id::text AND bac.activity_code='DOC' "+
				" LEFT JOIN bill_charge bc ON (bc.charge_id = bac.charge_id) "+
				" WHERE bcc.bill_no = ? AND dc.patient_id = ?"+
				" UNION ALL "+
				" SELECT bc.bill_no "+
				" FROM other_services_prescribed osp "+ 
				" JOIN bill_charge bc ON (bc.order_number = osp.common_order_id) "+
				" JOIN package_prescribed pp ON (osp.package_ref = pp.prescription_id) "+
				" JOIN packages p ON (pp.package_id = p.package_id AND p.multi_visit_package = true) "+
				" WHERE bc.bill_no = ? AND osp.patient_id = ? "+
				" UNION ALL "+
				" SELECT bc.bill_no "+
				" FROM bill_charge bc "+
        " JOIN bill b on b.bill_no = bc.bill_no"+
				" JOIN packages p ON (p.package_id = bc.package_id AND p.multi_visit_package = true) "+
				" WHERE bc.bill_no = ? AND b.visit_id = ? AND bc.charge_group='BED'"+
			" ) as orders  ";
	

	public Boolean checkMultiVisitBill(String billNo,String visitId) throws SQLException {

		Connection con = DataBaseUtil.getConnection();
		Boolean multiVisitBill = false;
		ResultSet rs = null;
		try (PreparedStatement ps = con.prepareStatement(MULTIVISIT_BILL_QUERY_MR_NO);) {
			ps.setString(1, billNo);
			ps.setString(2, visitId);
			ps.setString(3, billNo);
			ps.setString(4, visitId);
			ps.setString(5, billNo);
			ps.setString(6, visitId);
			ps.setString(7, billNo);
			ps.setString(8, visitId);
			ps.setString(9, billNo);
			ps.setString(10, visitId);
			
			rs = ps.executeQuery();
			if(rs.next() && rs.getString(1)!=null && !rs.getString(1).equals("")) {
				multiVisitBill  = true;
			}
		} finally {
			if (rs != null) { rs.close(); }
		}
		return multiVisitBill ;
	}
	
	public Boolean checkMultiVisitBill(Connection con, String billNo,String visitId) throws SQLException {

    Boolean multiVisitBill = false;
    try (PreparedStatement ps = con.prepareStatement(MULTIVISIT_BILL_QUERY_MR_NO);) {
		ps.setString(1, billNo);
		ps.setString(2, visitId);
		ps.setString(3, billNo);
		ps.setString(4, visitId);
		ps.setString(5, billNo);
		ps.setString(6, visitId);
		ps.setString(7, billNo);
		ps.setString(8, visitId);
		ps.setString(9, billNo);
		ps.setString(10, visitId);

      try (ResultSet rs = ps.executeQuery();) {
        if (rs.next() && rs.getString(1) != null && !rs.getString(1).equals("")) {
          multiVisitBill = true;
        }
      }
    } 
    return multiVisitBill;
  }

  public Boolean isMultiVisitBill(String billNo) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    Boolean multiVisitBill = false;
    try (PreparedStatement ps = con.prepareStatement(MULTIVISIT_BILL_QUERY);) {
      ps.setString(1, billNo);
      try (ResultSet rs = ps.executeQuery();) {
        if (rs.next() && rs.getString(1) != null && !rs.getString(1).equals("")) {
          multiVisitBill = true;
        }
      }
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
    return multiVisitBill;
  }

	//private static String GET_MULTIVISIT_BILL_PCAKGE_ID = "SELECT mbv.package_id FROM multivisit_bills_view mbv WHERE mbv.bill_no = ?";
	private static final String GET_MULTIVISIT_BILL_PCAKGE_ID =" SELECT orders.package_id  "+
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
    " LEFT JOIN bill_activity_charge bac ON bac.activity_id=dc.consultation_id::text AND bac.activity_code='DOC' "+
    " LEFT JOIN bill_charge bc ON (bc.charge_id = bac.charge_id) "+
    " WHERE bcc.bill_no = ? "+
    " UNION ALL "+
    " SELECT bc.bill_no,p.package_id "+
    " FROM other_services_prescribed osp "+ 
    " JOIN bill_charge bc ON (bc.order_number = osp.common_order_id) "+
    " JOIN package_prescribed pp ON (osp.package_ref = pp.prescription_id) "+
    " JOIN packages p ON (pp.package_id = p.package_id AND p.multi_visit_package = true) "+
    " WHERE bc.bill_no = ? "+
    " UNION ALL "+
    " SELECT bc.bill_no,p.package_id "+
    " FROM bill_charge bc "+
    " JOIN packages p ON (p.package_id = bc.package_id AND p.multi_visit_package = true) "+
    " WHERE bc.bill_no = ? AND bc.charge_group='BED'"+
  " ) as orders  ";

	public Integer getMultiVisitBillPackageId(String billNo) throws SQLException {

		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(GET_MULTIVISIT_BILL_PCAKGE_ID);
			ps.setString(1, billNo);
			ps.setString(2, billNo);
			ps.setString(3, billNo);
			ps.setString(4, billNo);
			ps.setString(5, billNo);
			return DataBaseUtil.getIntValueFromDb(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private Integer getPrimaryPlanId(Connection con, String patientId) throws SQLException {
		GenericDAO pipdao = new GenericDAO("patient_insurance_plans");
		Map keymap = new HashMap();
		keymap.put("patient_id", patientId);
		keymap.put("priority", new Integer(1));
		BasicDynaBean pipbean = pipdao.findByKey(con, keymap);
		if (null != pipbean) {
			return (Integer)pipbean.get("plan_id");
		}
		return new Integer(0);
	}

	public String closeBillAutomaticallyOnWriteOffApproval(String billNo, String writeOffType,
			String userId, Preferences pref) throws SQLException,Exception{
		BigDecimal sponsorDue = BillDAO.getSponsorDue(billNo);
		BigDecimal patientDue = BillDAO.getPatientDue(billNo);
		
		BasicDynaBean creditNoteDetails = billDao.getCreditNoteDetails(billNo);
		if(null != creditNoteDetails){
		   BigDecimal pat_credit_amt =(BigDecimal) creditNoteDetails.get("total_pat_amt");
		   BigDecimal sponsor_credit_amt =(BigDecimal) creditNoteDetails.get("total_claim");
		   patientDue = patientDue.add(pat_credit_amt);
		   sponsorDue = sponsorDue.add(sponsor_credit_amt);
		}
		
		String error = null;
		GenericDAO dao = billDao;
		BasicDynaBean billBean = dao.findByKey("bill_no", billNo);
		String patientWriteOff = (String)billBean.get("patient_writeoff");
		String sponsorWriteOff = (String)billBean.get("sponsor_writeoff");

		if((writeOffType.equals("P") && (sponsorDue.compareTo(BigDecimal.ZERO) == 0 || sponsorWriteOff.equals("A"))) ||
				(writeOffType.equals("S") && (patientDue.compareTo(BigDecimal.ZERO) == 0 || patientWriteOff.equals("A")))) {
				error = closeBill(billNo, userId, true, true,pref);
		}
		return error;
	}

	public String closeBill(String billNo, String userId, boolean paymentForceClose,
			boolean claimForceClose, Preferences pref) throws Exception{
	  java.sql.Timestamp now = DateUtil.getCurrentTimestamp(); 
		String error = null;
		Bill bill = getBill(billNo);
		BasicDynaBean billBean = billDao.findByKey("bill_no",billNo);

		if(null != billBean.get("discharge_status")){
			String dischargeStatus = (String)billBean.get("discharge_status");
			bill.setDischarge(dischargeStatus);
		}

		boolean isSecSponsorexists = isSecondarySponsorExists(bill.getVisitId());
		java.sql.Timestamp finalizedDate = bill.getFinalizedDate() == null ? now  :
			new java.sql.Timestamp(bill.getFinalizedDate().getTime());
		java.sql.Timestamp lastFinalizedAt = bill.getLastFinalizedAt() == null ?  now :
			new java.sql.Timestamp(bill.getLastFinalizedAt().getTime());


        String ipModAct = "N";
        if ( (pref!=null) && (pref.getModulesActivatedMap() != null) ) {
        	ipModAct = (String)pref.getModulesActivatedMap().get("mod_adt");
        	 if(ipModAct == null || ipModAct.equals("")){
        		 ipModAct = "N";
             }
        }

		bill.setPaymentStatus(Bill.BILL_PAYMENT_PAID);
		bill.setPrimaryClaimStatus(Bill.BILL_CLAIM_RECEIVED);
		bill.setUserName(userId);
		String origDischargeStatus = bill.getOkToDischarge();
		if (isSecSponsorexists) {
			bill.setSecondaryClaimStatus(Bill.BILL_CLAIM_RECEIVED);
		}

		if (bill.getOkToDischarge() == null) {
			if (bill.getBillType().equals(Bill.BILL_TYPE_PREPAID)) {
				bill.setOkToDischarge("Y");
			} else {
				bill.setOkToDischarge("N");
			}
		}


		if (bill.getBillType().equals(Bill.BILL_TYPE_CREDIT)) {
			if (ipModAct.equalsIgnoreCase("Y") && bill.getVisitType().equals(Bill.BILL_VISIT_TYPE_IP)) {
				if (bill.getOkToDischarge().equals("Y"))
					bill.setDischarge("D");
			} else {
				if (origDischargeStatus.equals("N") && bill.getOkToDischarge().equals("Y")) {
					bill.setDischarge("Y");
				} else if (bill.getOkToDischarge().equals("Y")) {
					bill.setDischarge("D");
				} else {
					bill.setDischarge("N");
				}
			}
		}


		if(bill.getFinalizedBy() == null || bill.getFinalizedBy().equals(""))
			bill.setFinalizedBy(userId);

		bill.setFinalizedDate(finalizedDate);

		Connection con = null;
		try{
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			BillDAO billDAO = new BillDAO(con);
			billDAO.updateBill(bill);
			error = updateBillStatus(con, bill, Bill.BILL_STATUS_CLOSED, bill.getPaymentStatus(), 
			    bill.getDischarge(), finalizedDate, userId, false, paymentForceClose, claimForceClose);
		}finally{
			DataBaseUtil.commitClose(con, true);
		}



		return error;
	}

	public boolean isSecondarySponsorExists(String visitId) throws SQLException{

		String secSponsorId = null;
		BasicDynaBean bean = visitDetailsDao.findByKey("patient_id", visitId);
		if(null != bean && null != bean.get("secondary_sponsor_id"))
			secSponsorId = (String)bean.get("secondary_sponsor_id");
		if(null != secSponsorId && !secSponsorId.equals(""))
			return true;
		else
			return false;

	}

	public static BasicDynaBean getRatePlanId(String billNo, String visitId, int centerId)
	throws SQLException, IOException {
		Connection con = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			return getRatePlanId(con,billNo,visitId,centerId);
		} finally {
			DataBaseUtil.closeConnections(con, null);
		}

	}

/*This method is use to get rate plan id   */
public static BasicDynaBean getRatePlanId(Connection con, String billNo, String visitId, int centerId)
	throws SQLException, IOException {

		Map<Integer,String> billTypes = new HashMap<Integer,String>();
		int caseKey =0;
		billTypes.put(1, "");
		billTypes.put(2,"BN");
		billTypes.put(3,"new");
		billTypes.put(4,"BN-I");
		billTypes.put(5,"NewInsurance");

		BasicDynaBean bean = null;
		if (billNo == null )
			return null;

		  Set<Map.Entry<Integer, String>> entrySet = billTypes.entrySet();
		  for(Map.Entry<Integer,String> entry: entrySet){
		        if(billNo.trim().equalsIgnoreCase(entry.getValue())){
		        	caseKey = entry.getKey();
		            break;
		        }
		    }
		  switch (caseKey) {
			case 1:						//No bill case
				if (visitId != null){
					BasicDynaBean visitBean = visitDetailsDao.getPatientVisitDetailsBean(con,visitId);
					bean = visitBean;
				}
				break;
			case 2:						//Cash bill
			case 3:
				BasicDynaBean visitDetailBean = visitDetailsDao.getPatientVisitDetailsBean(con,visitId);
				String primarySponserId = (String)visitDetailBean.get("primary_sponsor_id");
				String secondrySponserId = (String)visitDetailBean.get("secondary_sponsor_id");
				if (primarySponserId != null && !primarySponserId.equals("") || secondrySponserId != null && !secondrySponserId.equals("")){

					String orgIdForNonInsuBills = CenterPreferencesDAO.getRatePlanForNonInsuredBills(centerId);
		            BasicDynaBean orgDetailBean = orgDetailsDao.findByKey("org_id",orgIdForNonInsuBills);
		            if (orgDetailBean != null) {
		            	bean = orgDetailBean;
		            }
				}else {
					if (visitId != null){
						BasicDynaBean visitBean = visitDetailsDao.getPatientVisitDetailsBean(con,visitId);
						bean = visitBean;
					}
				}
				break;
			case 4:						//Insureance bill
			case 5:
				if (visitId != null){
					BasicDynaBean visitBean = visitDetailsDao.getPatientVisitDetailsBean(con,visitId);
					bean = visitBean;
				}
				break;
			default:					//If the bill is present

				BasicDynaBean billBean =(BasicDynaBean)billDao.getBillBean(billNo);
				String orgId =(String) billBean.get("bill_rate_plan_id");
				BasicDynaBean orgBean = orgDetailsDao.findByKey("org_id",orgId);
				if (orgBean != null) {
					bean = orgBean;
				}
				break;
			}
		return bean ;
}

	public static boolean isMandatoryCustomFieldsExistForPatient(String visitID)throws SQLException {

		List<CustomField> customFields = RegistrationPreferencesDAO.getCustomFieldsForValidation();
		List<CustomField> visitCustomFields = RegistrationPreferencesDAO.getVisitCustomFields();
		String mrNo = VisitDetailsDAO.getMrno(visitID);
		String visitType = VisitDetailsDAO.getVisitType(visitID);
		final StringBuilder query = new StringBuilder();

		Connection con = null;
		PreparedStatement pstmt = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			query.append("SELECT mr_no FROM patient_details WHERE mr_no = ?");
			for (int i=0; i<customFields.size(); i++) {
				 CustomField customField = customFields.get(i);
				 if (customField.getRequired().equalsIgnoreCase("A") || customField.getRequired().equalsIgnoreCase(visitType)) {
					 query.append(" AND (" + customField.getTxColumnName() + " IS NOT NULL ) ");
				 }
			}
			pstmt = con.prepareStatement(query.toString());
			pstmt.setString(1, mrNo);
			BasicDynaBean patientBean = DataBaseUtil.queryToDynaBean(pstmt);
			if (patientBean == null)
				return false;

			query.setLength(0);
			query.append("SELECT mr_no FROM patient_registration WHERE patient_id = ?");
			for (int i=0; i<visitCustomFields.size(); i++) {
				CustomField customField = visitCustomFields.get(i);
				 if (customField.getRequired().equalsIgnoreCase("A") || customField.getRequired().equalsIgnoreCase(visitType)) {
					 query.append(" AND (" + customField.getTxColumnName() + " IS NOT NULL ) ");
				 }
			}
			pstmt = con.prepareStatement(query.toString());
			pstmt.setString(1, visitID);
			BasicDynaBean visitBean = DataBaseUtil.queryToDynaBean(pstmt);
			if (visitBean == null)
				return false;

		} finally {
			DataBaseUtil.closeConnections(con, pstmt);
		}

		return true;
	 }

	public Map<String,Map<String,String>> getBillAdjustmentAlerts(Connection con,String visitId) throws SQLException{

		List<BasicDynaBean> adjAlerts = billDao.getBillAdjustmentAlerts(con,visitId);

		Map<String,Map<String,String>> adjMap = new HashMap<String, Map<String,String>>();
		Map<String, String> priAdjMap = new HashMap<String, String>();
		Map<String, String> secAdjMap = new HashMap<String, String>();

		Map<Integer,List<BasicDynaBean>> billAdjAlertMap = ConversionUtils.listBeanToMapListBean(adjAlerts, "plan_id");
		List<String> primaryVisitRules = new ArrayList<String>();
		List<String> secondaryVisitRules = new ArrayList<String>();

		for(Integer key : billAdjAlertMap.keySet()){
			List<BasicDynaBean> billAdjAlerts = billAdjAlertMap.get(key);
			int priority = (Integer)billAdjAlerts.get(0).get("priority");
			String sponsorName = (String)billAdjAlerts.get(0).get("tpa_name");


			Map<String,List<String>> catMap = new HashMap<String, List<String>>();

			for(BasicDynaBean bean : billAdjAlerts){
				int adjStatus = (Integer)bean.get("adjstment_status");
				int categoryId = (Integer)bean.get("category_id");
				String categoryName = categoryId == -2 ? "visit" : (String)bean.get("insurance_category_name");
				List<String> rules = new ArrayList<String>();
				rules = getInsuranceRules(adjStatus);
				catMap.put(categoryName, rules);
			}


			for(String category : catMap.keySet()){
				List<String> rules = catMap.get(category);

				for(String r : rules){
					String catName = null != priAdjMap.get(r) ? priAdjMap.get(r)+"," : "";
					if(!category.equals("visit")){
						if(priority == 1)
							priAdjMap.put(r, catName+ category);
						else
							secAdjMap.put(r, catName+ category);
					}
					else{
						if(priority == 1)
							primaryVisitRules.add(r);
						else
							secondaryVisitRules.add(r);
					}

				}

			}

			if(priority == 1)
				priAdjMap.put("sponsor", sponsorName);
			else
				secAdjMap.put("sponsor", sponsorName);
		}

		String priVisitMessage = getVisitAlertMessage(primaryVisitRules);
		String secVisitMessage = getVisitAlertMessage(secondaryVisitRules);
		Map<String,String> visitAdjMap = new HashMap<String, String>();

		visitAdjMap.put("priVisitMessage", priVisitMessage);
		visitAdjMap.put("secVisitMessage", secVisitMessage);

		adjMap.put("primary", priAdjMap);
		adjMap.put("secondary", secAdjMap);
		adjMap.put("visitRules", visitAdjMap);
		return adjMap;
	}

	private String getVisitAlertMessage(List<String> visitRules) {
		String visitMsg = "";

		for(String rule : visitRules){
			visitMsg = visitMsg + rule + " Rule could not be adjusted.";
		}
		return visitMsg;
	}

	private List<String> getInsuranceRules(int adjStatus) {

		List<String> rules = new ArrayList<String>();

		setInsuranceRules(rules,RuleAdjustmentType.CATEGORY_DEDUCTIBLE_ADJ, adjStatus);
		setInsuranceRules(rules,RuleAdjustmentType.CATEGORY_MAX_COPAY_ADJ, adjStatus);
		setInsuranceRules(rules,RuleAdjustmentType.CATEGORY_SPONSOR_LIMIT_ADJ, adjStatus);
		setInsuranceRules(rules,RuleAdjustmentType.VISIT_DEDUCTIBLE_ADJ, adjStatus);
		setInsuranceRules(rules,RuleAdjustmentType.VISIT_MAX_COPAY_ADJ, adjStatus);
		setInsuranceRules(rules,RuleAdjustmentType.VISIT_PER_DAY_LIMIT_ADJ, adjStatus);
		setInsuranceRules(rules,RuleAdjustmentType.VISIT_SPONSOR_LIMIT_ADJ, adjStatus);

		return rules;
	}

	private void setInsuranceRules(List<String> rules, RuleAdjustmentType adjType, int adjStatus) {

		int catDedRuleAdjExists = adjType.getCode() & adjStatus;
		if(catDedRuleAdjExists > 0)rules.add(adjType.getMessage());

	}
	
	public Map<String, Object> getFinancialDischargeTokens(Bill bill, String mrNo, BasicDynaBean billAmounts) throws SQLException {
		Map<String, Object> patientDischargeData = new HashMap<String, Object>();
		Map<String, String> filterMap = new HashMap<String, String>();
		filterMap.put("mr_no", mrNo);
		List<String> patientDetailsColumns = new ArrayList<String>();
		patientDetailsColumns.add("patient_phone");
		patientDetailsColumns.add("patient_name");
		patientDetailsColumns.add("relation");
		patientDetailsColumns.add("relation");
		patientDetailsColumns.add("email_id");
	    List<BasicDynaBean> patient_details = new PatientDetailsDAO().listAll(patientDetailsColumns, filterMap, null);
	    BasicDynaBean patientDetails = patient_details.get(0);
    	String patientName = (String)patientDetails.get("patient_name");
    	String patient_phone = (String)patientDetails.get("patient_phone");
    	String emailId = (String)patientDetails.get("email_id");
    	String nextOfKin = (String)patientDetails.get("relation");
		BasicDynaBean dischargeBean = dischargeDao.checkIfPatientDischargeEntryExists(bill.getVisitId());
		if (dischargeBean != null) {
			    boolean dischargeStatus = (Boolean)(dischargeBean.get("financial_discharge_status"));
				patientDischargeData.put("receipient_id__", mrNo);
				if (dischargeBean.get("financial_discharge_date") != null &&
						dischargeBean.get("financial_discharge_time") != null) {
					patientDischargeData.put("financial_discharge_date", new SimpleDateFormat("dd-MM-yyyy").
				         format(dischargeBean.get("financial_discharge_date")));
					patientDischargeData.put("financial_discharge_time", new SimpleDateFormat("HH:mm").
				         format(dischargeBean.get("financial_discharge_time")));
				}
				patientDischargeData.put("sponsor_amount", (BigDecimal) billAmounts.get("sponsor_amount"));
				patientDischargeData.put("billed_amount", (BigDecimal) billAmounts.get("amount"));
				patientDischargeData.put("patient_amount", (BigDecimal) billAmounts.get("patient_amt"));
				patientDischargeData.put("patient_due", (BigDecimal) billAmounts.get("patient_due"));
				patientDischargeData.put("sponsor_due",  (BigDecimal) billAmounts.get("sponsor_due"));
				patientDischargeData.put("discharge_state", "Financial Discharge");
				patientDischargeData.put("discharge_status", "F");
				patientDischargeData.put("patient_name", patientName);
			    patientDischargeData.put("next_of_kin_name", nextOfKin);
				patientDischargeData.put("patient_phone", patient_phone);
				patientDischargeData.put("recipient_email", emailId);
				patientDischargeData.put("financial_discharge_status", dischargeStatus);
		}
	    return patientDischargeData;
	}
	
  /**
   * Method to update case rate limits
   *
   * @param visitBean
   * @return
   */
  public Boolean updateCaseRateLimts(BasicDynaBean visitBean) {

    String visitId = (String) visitBean.get("patient_id");
    Boolean success = false;

    Integer priCaseRateId =
        null != visitBean.get("primary_case_rate_id")
            ? (Integer) visitBean.get("primary_case_rate_id")
            : null;
    Integer secCaseRateId =
        null != visitBean.get("secondary_case_rate_id")
            ? (Integer) visitBean.get("secondary_case_rate_id")
            : null;

    if (null != priCaseRateId) {
      success = insPlanDAO.updateCaseRateLimits(priCaseRateId, visitId, 1);
    }

    if (null != secCaseRateId) {
      success = insPlanDAO.updateCaseRateLimits(secCaseRateId, visitId, 2);
    }

    return success;
  }
	
	private static final String GET_VISIT_FINALIZED_AND_CLOSED_BILLS = "SELECT * FROM bill b WHERE "
      + " b.visit_id = ? AND b.status IN ('F','C') ";
  
  public List<BasicDynaBean> getVisitFinalizedAndClosedBills(String visitId) throws SQLException {
    Connection con =null;
    PreparedStatement ps = null;
    List<BasicDynaBean> billsList = null;
    try {
      con=DataBaseUtil.getConnection();
      ps = con.prepareStatement(GET_VISIT_FINALIZED_AND_CLOSED_BILLS);
      ps.setString(1, visitId);
      billsList = DataBaseUtil.queryToDynaList(ps);
      return billsList != null ? billsList : new ArrayList<BasicDynaBean>();
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Method to get case rate amount in bill
   *
   * @param visitId
   * @param billNo
   * @return
   * @throws SQLException
   */
  public Map<String, Map<String, BigDecimal>> getCaseRateAmountInBill(String visitId, String billNo)
      throws SQLException {

    String claimId = billClaimDAO.getPrimaryClaimID(billNo);

    BasicDynaBean visitBean = visitDetailsDao.findByKey("patient_id", visitId);
    Map<String, BigDecimal> remCaseRateCatAmtInBill = new HashMap<>();
    Map<String, Map<String, BigDecimal>> caseRateCaAmtMap = new HashMap<>();

    if (null != visitBean && null != visitBean.get("primary_case_rate_id")) {
      Integer priCaseRateId = (Integer) visitBean.get("primary_case_rate_id");
      setCaseRateCategoryAmtInBill(
          priCaseRateId, remCaseRateCatAmtInBill, caseRateCaAmtMap, billNo, claimId, visitId);
    }

    if (null != visitBean && null != visitBean.get("secondary_case_rate_id")) {
      Integer secCaseRateId = (Integer) visitBean.get("secondary_case_rate_id");
      setCaseRateCategoryAmtInBill(
          secCaseRateId, remCaseRateCatAmtInBill, caseRateCaAmtMap, billNo, claimId, visitId);
    }

    return caseRateCaAmtMap;
  }

  /**
   * Method to set case rate category amount in bill
   *
   * @param caseRateId
   * @param remCaseRateCatAmtInBill
   * @param caseRateCaAmtMap
   * @param billNo
   * @param claimId
   * @param visitId
   * @throws SQLException
   */
  private void setCaseRateCategoryAmtInBill(
      Integer caseRateId,
      Map<String, BigDecimal> remCaseRateCatAmtInBill,
      Map<String, Map<String, BigDecimal>> caseRateCaAmtMap,
      String billNo,
      String claimId,
      String visitId)
      throws SQLException {

    List<BasicDynaBean> caseRateDetails =
        visitCaseRateDetailsDAO.getCaseRateCategoryLimits(visitId, caseRateId);

    Map<String, BigDecimal> caseRateCatAmtMapInBill = new HashMap<>();

    for (BasicDynaBean caseRateBean : caseRateDetails) {
      int insCatId = (Integer) caseRateBean.get("insurance_category_id");
      String insCatName = (String) caseRateBean.get("insurance_category_name");
      BigDecimal catAmt = (BigDecimal) caseRateBean.get("amount");

      if (remCaseRateCatAmtInBill.containsKey(insCatName)) {
        caseRateCatAmtMapInBill.put(
            insCatName, catAmt.min(remCaseRateCatAmtInBill.get(insCatName)));
      } else {
        BigDecimal caseRateCatAmtInBill =
            billChgClaimDAO.getCaseRateCatAmtInBill(billNo, claimId, insCatId);
        caseRateCatAmtInBill =
            null == caseRateCatAmtInBill ? BigDecimal.ZERO : caseRateCatAmtInBill;
        caseRateCatAmtMapInBill.put(insCatName, caseRateCatAmtInBill.min(catAmt));

        if (caseRateCatAmtInBill.compareTo(catAmt) > 0) {
          remCaseRateCatAmtInBill.put(insCatName, caseRateCatAmtInBill.subtract(catAmt));
        } else {
          remCaseRateCatAmtInBill.put(insCatName, BigDecimal.ZERO);
        }
      }
    }

    caseRateCaAmtMap.put(caseRateId.toString(), caseRateCatAmtMapInBill);
  }

  private static final String GET_BILLS_BY_BILL_NOS = "SELECT * FROM bill b WHERE "
      + " b.bill_no IN ('#') ";
  
  public List<BasicDynaBean> getBillBeans(Set<String> keySet) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    String bills = "";
    try {
      if (keySet != null && keySet.size() > 0) {
        Iterator<String> iterator = keySet.iterator();
        while (iterator.hasNext()) {
          String billNo = iterator.next();
          bills = bills + billNo;
          if (iterator.hasNext()) {
            bills = bills + "', '";
          }
        }
      }

      StringBuilder query = new StringBuilder(GET_BILLS_BY_BILL_NOS.replaceAll("#", bills));
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(query.toString());
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }
}
