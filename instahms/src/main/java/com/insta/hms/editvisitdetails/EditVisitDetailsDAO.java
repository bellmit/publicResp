package com.insta.hms.editvisitdetails;

import com.bob.hms.adminmasters.organization.OrgMasterDao;
import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.Registration.PatientInsurancePlanDAO;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.billing.Bill;
import com.insta.hms.billing.BillActivityChargeDAO;
import com.insta.hms.billing.BillBO;
import com.insta.hms.billing.BillChargeClaimDAO;
import com.insta.hms.billing.BillChargeTaxDAO;
import com.insta.hms.billing.BillDAO;
import com.insta.hms.billing.BillDetails;
import com.insta.hms.billing.BillingHelper;
import com.insta.hms.billing.ChargeDAO;
import com.insta.hms.billing.ChargeDTO;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.dischargesummary.DischargeSummaryBOImpl;
import com.insta.hms.documentpersitence.MLCDocumentAbstractImpl;
import com.insta.hms.insurance.AdvanceInsuranceCalculator;
import com.insta.hms.insurance.InsuranceDAO;
import com.insta.hms.ipservices.BedDTO;
import com.insta.hms.ipservices.IPBedDAO;
import com.insta.hms.master.CenterPreferences.CenterPreferencesDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.PatientCategory.PatientCategoryDAO;
import com.insta.hms.master.PlanMaster.PlanDetailsDAO;
import com.insta.hms.master.TpaMaster.TpaMasterDAO;
import com.insta.hms.mlcdocuments.MLCDocumentsBO;
import com.insta.hms.orders.OrderBO;
import com.insta.hms.stores.MedicineSalesDAO;
import com.insta.hms.stores.MedicineSalesDTO;
import com.insta.hms.stores.SalesClaimDetailsDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * The Class EditVisitDetailsDAO.
 */
public class EditVisitDetailsDAO {

  /** The logger. */
  static Logger logger = LoggerFactory
      .getLogger(EditVisitDetailsDAO.class);

  /** The bills with store charges. */
  public static List billsWithStoreCharges = new ArrayList();
  
  private static final GenericDAO storeSalesMainDAO = new GenericDAO("store_sales_main");
  private static final GenericDAO storeSalesDetailsDAO = new GenericDAO("store_sales_details");

  /** The centerwise get doctor dept list. */
  private static String CENTERWISE_GET_DOCTOR_DEPT_LIST = "SELECT d.doctor_id,d.doctor_name, "
      + " dep.dept_name, dep.dept_id, d.doctor_license_number FROM doctors d "
      + " JOIN doctor_center_master dcm ON(d.doctor_id=dcm.doctor_id)"
      + " JOIN department dep USING(dept_id) WHERE d.status = 'A' "
      + " AND (dcm.center_id=? OR dcm.center_id = 0) AND dcm.status ='A' " + " UNION "
      + " SELECT doctor_id,doctor_name, dep.dept_name, dept_id, d.doctor_license_number AS "
      + " doctor_license_number FROM patient_registration pr "
      + " JOIN doctors d on (d.doctor_id = pr.doctor) JOIN department dep USING(dept_id) "
      + " WHERE patient_id  = ? ORDER BY doctor_name ";

  /**
   * This method gets the patient doctor-dept details(irrespective of active or inactive) union of
   * all active doctors-dept details.
   * 
   * @param visitId
   *          the visit id
   * @return the doctor dept list
   * @throws SQLException
   *           the SQL exception
   */
  public static List<BasicDynaBean> getDoctorDeptList(String visitId) throws SQLException {
    int centerId = RequestContext.getCenterId();
    PreparedStatement ps = null;
    List<BasicDynaBean> doctorDeptList = null;
    Connection con = null;
    List<String> list = new ArrayList<String>();
    list.add("center_id");
    Map<String, Object> identifiers = new HashMap<String, Object>();
    identifiers.put("patient_id", visitId);

    try {
      con = DataBaseUtil.getConnection();
      // patientRegBean should come visit_id with corresponding center_id in patient_registration.
      BasicDynaBean patientRegBean = new GenericDAO("patient_registration").findByKey(con, list,
          identifiers);
      if (centerId == 0 && patientRegBean != null
          && GenericPreferencesDAO.getGenericPreferences().getMax_centers_inc_default() > 1) {
        centerId = (Integer) patientRegBean.get("center_id");
      }
      ps = con.prepareStatement(CENTERWISE_GET_DOCTOR_DEPT_LIST);
      ps.setInt(1, centerId);
      ps.setString(2, visitId);
      doctorDeptList = DataBaseUtil.queryToDynaList(ps);
    } catch (SQLException sqlException) {
      logger.error("", sqlException);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return doctorDeptList;
  }

  /**
   * Update visit and bill insurance details.
   *
   * @param con
   *          the con
   * @param mainVisitBean
   *          the main visit bean
   * @param allVisits
   *          the all visits
   * @param allBills
   *          the all bills
   * @param userName
   *          the user name
   * @param insuranceId
   *          the insurance id
   * @param priTpaId
   *          the pri tpa id
   * @param priInsuranceCoId
   *          the pri insurance co id
   * @param secTpaId
   *          the sec tpa id
   * @param secInsuranceCoId
   *          the sec insurance co id
   * @param planIds
   *          the plan ids
   * @param categoryId
   *          the category id
   * @param policyValidFrom
   *          the policy valid from
   * @param policyValidTo
   *          the policy valid to
   * @param policyHolderName
   *          the policy holder name
   * @param policyNumber
   *          the policy number
   * @param policyNo
   *          the policy no
   * @param patientRelationship
   *          the patient relationship
   * @param priApprovalAmt
   *          the pri approval amt
   * @param secApprovalAmt
   *          the sec approval amt
   * @param priorAuthId
   *          the prior auth id
   * @param priorAuthModeId
   *          the prior auth mode id
   * @param organization
   *          the organization
   * @param useDRG
   *          the use DRG
   * @param usePerdiem
   *          the use perdiem
   * @return the string
   * @throws Exception
   *           the exception
   */
  public static String updateVisitAndBillInsuranceDetails(Connection con,
      BasicDynaBean mainVisitBean, List<BasicDynaBean> allVisits, List<BasicDynaBean> allBills,
      String userName, int insuranceId, String priTpaId, String priInsuranceCoId, String secTpaId,
      String secInsuranceCoId, int[] planIds, int categoryId, Date policyValidFrom,
      Date policyValidTo, String policyHolderName, String policyNumber, String policyNo,
      String patientRelationship, BigDecimal priApprovalAmt, BigDecimal secApprovalAmt,
      String priorAuthId, Integer priorAuthModeId, String organization, String useDRG,
      String usePerdiem) throws Exception {

    VisitDetailsDAO visitdao = new VisitDetailsDAO();
    String mrNo = null;
    boolean allowBillNowInsurance = BillDAO.isBillInsuranceAllowed(Bill.BILL_TYPE_PREPAID, true);

    // Update all visits
    if (allVisits != null && allVisits.size() > 0) {

      mrNo = (String) allVisits.get(0).get("mr_no");

      for (BasicDynaBean b : allVisits) {

        if (planIds != null && planIds.length > 0) {
          b.set("plan_id", planIds[0]); // update the primary plan id into the patient registration.
        }

        b.set("primary_sponsor_id", priTpaId);
        b.set("primary_insurance_co", priInsuranceCoId);
        b.set("category_id", categoryId);
        b.set("prior_auth_id", priorAuthId);
        b.set("prior_auth_mode_id", priorAuthModeId);
        b.set("org_id", organization);// Rate plan should be updated for all visits

        b.set("patient_policy_id", mainVisitBean.get("patient_policy_id"));
        b.set("patient_corporate_id", mainVisitBean.get("patient_corporate_id"));
        b.set("patient_national_sponsor_id", mainVisitBean.get("patient_national_sponsor_id"));
        b.set("secondary_patient_corporate_id",
            mainVisitBean.get("secondary_patient_corporate_id"));
        b.set("secondary_patient_national_sponsor_id",
            mainVisitBean.get("secondary_patient_national_sponsor_id"));

        b.set("secondary_sponsor_id", secTpaId);
        b.set("secondary_insurance_co", secInsuranceCoId);

        boolean isMainVisit = !(b.get("op_type") != null
            && (b.get("op_type").equals("F") || b.get("op_type").equals("D")));

        // Update approval amount, insurance id for main visit. Follow up visit has no approval
        // amount.
        if (isMainVisit) {
          b.set("insurance_id", 0);
          b.set("primary_insurance_approval", priApprovalAmt);
          b.set("secondary_insurance_approval", secApprovalAmt);
          b.set("use_drg", useDRG);
          b.set("use_perdiem", usePerdiem);
        } else {
          b.set("insurance_id", 0);
          b.set("primary_insurance_approval", null);
          b.set("secondary_insurance_approval", null);
          b.set("use_drg", "N");
          b.set("use_perdiem", "N");
        }

        b.set("user_name", userName);
        visitdao.updateWithName(con, b.getMap(), "patient_id");
      }
    }

    // Update all bills
    if (allBills != null && allBills.size() > 0) {
      EditVisitDetailsDAO.map = new HashMap<Integer, Integer>();
      for (BasicDynaBean bill : allBills) {

        String billNo = (String) bill.get("bill_no");
        String visitId = (String) bill.get("visit_id");

        insertOrUpdateInsurance(con, userName, insuranceId, mrNo, visitId, billNo, priTpaId,
            policyValidFrom, policyValidTo, policyHolderName, policyNo, policyNumber,
            patientRelationship, priApprovalAmt, secApprovalAmt, priorAuthId, priorAuthModeId);

        if ((!bill.get("bill_type").equals("C") && !allowBillNowInsurance)
            && bill.get("is_tpa").equals(false)) {
          continue;
        }

        String billStatus = (String)bill.get("status");
        
        if (billStatus.equals("A")) {
          policyUpdate(con, priTpaId, billNo, planIds, allowBillNowInsurance, organization);
        }
      }
    }
    return null;
  }

  /**
   * Insert or update insurance.
   *
   * @param con
   *          the con
   * @param userName
   *          the user name
   * @param insuranceId
   *          the insurance id
   * @param mrNo
   *          the mr no
   * @param visitId
   *          the visit id
   * @param billNo
   *          the bill no
   * @param tpaId
   *          the tpa id
   * @param policyValidFrom
   *          the policy valid from
   * @param policyValidTo
   *          the policy valid to
   * @param policyHolderName
   *          the policy holder name
   * @param policyNo
   *          the policy no
   * @param policyNumber
   *          the policy number
   * @param patientRelationship
   *          the patient relationship
   * @param priApprovalAmt
   *          the pri approval amt
   * @param secApprovalAmt
   *          the sec approval amt
   * @param priorAuthId
   *          the prior auth id
   * @param priorAuthModeId
   *          the prior auth mode id
   * @return the string
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public static String insertOrUpdateInsurance(Connection con, String userName, int insuranceId,
      String mrNo, String visitId, String billNo, String tpaId, Date policyValidFrom,
      Date policyValidTo, String policyHolderName, String policyNo, String policyNumber,
      String patientRelationship, BigDecimal priApprovalAmt, BigDecimal secApprovalAmt,
      String priorAuthId, Integer priorAuthModeId) throws SQLException, IOException {

    InsuranceDAO insdao = new InsuranceDAO();

    Boolean newInsuranceCase = false;
    if (insuranceId == 0 || (insdao.findByKey(con, "insurance_id", insuranceId) == null)) {
      newInsuranceCase = true;
    }

    // Create or Edit Insurance Case
    BasicDynaBean insBean = insdao.getBean();
    insBean.set("insurance_id", insuranceId); // primary key
    insBean.set("mr_no", mrNo);
    insBean.set("tpa_id", tpaId);
    insBean.set("case_added_date", DateUtil.getCurrentTimestamp());
    insBean.set("status", "P");
    if (policyValidFrom != null && policyValidTo != null) {
      insBean.set("policy_validity_start", policyValidFrom);
      insBean.set("policy_validity_end", policyValidTo);
    }
    if (policyHolderName != null && !policyHolderName.equals("")) {
      insBean.set("policy_holder_name", policyHolderName);
    }
    if (policyNo != null && !policyNo.equals("")) {
      insBean.set("policy_no", policyNo);
    }
    if (policyNumber != null && !policyNumber.equals("")) {
      insBean.set("insurance_no", policyNumber);
    }
    insBean.set("patient_relationship", patientRelationship);
    insBean.set("prior_auth_id", priorAuthId);
    insBean.set("prior_auth_mode_id", priorAuthModeId);

    VisitDetailsDAO visitdao = new VisitDetailsDAO();
    // Update patient registration insurance_id
    BasicDynaBean visitBean = visitdao.getBean();
    visitBean.set("user_name", userName);
    visitBean.set("insurance_id", newInsuranceCase ? 0 : insuranceId);
    visitBean.set("patient_id", visitId); // primary key

    visitdao.updateWithName(con, visitBean.getMap(), "patient_id");

    if (!newInsuranceCase) {
      insdao.updateWithName(con, insBean.getMap(), "insurance_id");
      Map fields = new HashMap<String, Object>();
      fields.put("procedure_no", 0);
      fields.put("primary_approval_amount", priApprovalAmt);
      fields.put("secondary_approval_amount", secApprovalAmt);
      fields.put("bill_no", billNo);
      new GenericDAO("bill").updateWithName(con, fields, "bill_no");
    }
    return null;
  }

  /*
   * Update bill charges according to plan, set is_tpa (true/false) based on TPA (exists/not exists)
   * for a bill
   */

  /**
   * Policy update.
   *
   * @param con
   *          the con
   * @param tpaId
   *          the tpa id
   * @param billNo
   *          the bill no
   * @param planIds
   *          the plan ids
   * @param allowBillNowInsurance
   *          the allow bill now insurance
   * @param organization
   *          the organization
   * @return the string
   * @throws Exception
   *           the exception
   */
  public static String policyUpdate(Connection con, String tpaId, String billNo, int[] planIds,
      boolean allowBillNowInsurance, String organization) throws Exception {
    Bill bill = new BillDAO(con).getBill(billNo);
    String patientId = bill.getVisitId();
    BasicDynaBean visitBean = new GenericDAO("patient_registration").findByKey(con, "patient_id",
        patientId);
    Integer centerId = (Integer) visitBean.get("center_id");
    String prefRatePlan = CenterPreferencesDAO.getRatePlanForNonInsuredBills(centerId);
    BasicDynaBean planDetails = new PatientInsurancePlanDAO().getVisitPrimaryPlan(con, patientId);

    // If Sponsor is removed then set bill tpa as false.
    // Otherwise bill tpa is not changed.

    boolean isTpa = (tpaId != null && !tpaId.equals(""));

    if (bill != null) {
      if (isTpa) {
        if (bill.getBillType().equals(Bill.BILL_TYPE_PREPAID) && allowBillNowInsurance) {
          bill.setBillRatePlanId(bill.getIs_tpa() ? organization
              : (prefRatePlan == null ? organization : prefRatePlan));

        } else if (bill.getBillType().equals(Bill.BILL_TYPE_CREDIT)) {
          bill.setBillRatePlanId(bill.getIs_tpa() ? organization
              : (prefRatePlan == null ? organization : prefRatePlan));

        } else {
          bill.setBillRatePlanId(prefRatePlan == null ? organization : prefRatePlan);
        }
      } else {
        bill.setIs_tpa(false);
        bill.setPrimaryClaimStatus(null);
        bill.setBillRatePlanId(organization);
      }
      // primary plan linked discount plan
      bill.setBillDiscountCategory(
          bill.getIs_tpa() && planDetails != null && planDetails.get("discount_plan_id") != null
              && (Integer) planDetails.get("discount_plan_id") != 0
                  ? (Integer) planDetails.get("discount_plan_id") : 0);
      updateBillChargesForPolicy(con, bill.getVisitId(), bill.getIs_tpa(), billNo, planIds);
      new BillDAO(con).updateBill(bill);
    }
    return null;
  }

  /**
   * Update bill charges for policy.
   *
   * @param con
   *          the con
   * @param visitId
   *          the visit id
   * @param isTpa
   *          the is tpa
   * @param billNo
   *          the bill no
   * @param planId
   *          the plan id
   * @return true, if successful
   * @throws Exception
   *           the exception
   */
  public static boolean updateBillChargesForPolicy(Connection con, String visitId, boolean isTpa,
      String billNo, int planId) throws Exception {
    boolean success = false;
    int[] planIds = { planId };
    success = updateBillChargesForPolicy(con, visitId, isTpa, billNo,
        ((0 == planId) ? null : planIds));
    return success;
  }

  /**
   * Update bill charges for policy.
   *
   * @param con
   *          the con
   * @param visitId
   *          the visit id
   * @param isTpa
   *          the is tpa
   * @param billNo
   *          the bill no
   * @param planIds
   *          the plan ids
   * @return true, if successful
   * @throws Exception
   *           the exception
   */
  @SuppressWarnings("unchecked")
  public static boolean updateBillChargesForPolicy(Connection con, String visitId, boolean isTpa,
      String billNo, int[] planIds) throws Exception {
    BillBO billBOObj = new BillBO();
    ChargeDAO chargeDAO = new ChargeDAO(con);
    BillDetails billDetails = null;
    boolean success = true;

    billDetails = billBOObj.getBillDetails(con, billNo);
    List<ChargeDTO> chargeList = billDetails.getCharges();
    Iterator<ChargeDTO> it = chargeList.iterator();
    while (it.hasNext()) {
      ChargeDTO cdto = it.next();
      cdto.getActDescription();
      if (cdto.getVisitId() == null || cdto.getVisitId().equals("")) {
        cdto.setVisitId(billDetails.getBill().getVisitId());
      }

      if (isTpa && cdto.getInsurancePayable() != null && cdto.getInsurancePayable().equals("Y")) {
        if (null != planIds) {
          cdto = updateInsuranceAmtForPlan(con, billDetails.getBill().getVisitType(), billNo,
              planIds, billDetails.getBill().getVisitId(), cdto);
        } else {
          cdto.setInsuranceAmtForPlan(0, billDetails.getBill().getVisitType());
        }
        if (cdto.getChargeGroup().equals("MED")) {
          /*
           * Set appropriate insurance amounts in sales details.
           */
          setMedicineInsuranceAmtForPlan(con, planIds, cdto);
        }
        if (cdto.getChargeHead().equals("PHRET") || cdto.getChargeHead().equals("PHCRET")) {
          /*
           * Set appropriate insurance amounts in sales details.
           */
          setMedicineInsuranceAmtForPlan(con, planIds, cdto, true);
        }
      } else {
        cdto.setInsuranceClaimAmount(BigDecimal.ZERO);
        cdto.setReturnInsuranceClaimAmt(BigDecimal.ZERO);
        cdto.setOrigInsuranceClaimAmount(BigDecimal.ZERO);
        if (cdto.getChargeGroup().equals("MED") || cdto.getChargeHead().equals("PHRET")
            || cdto.getChargeHead().equals("PHCRET")) {
          // Set claim amount to zero, in sales bill.
          BasicDynaBean salebean = storeSalesMainDAO.findByKey("charge_id", cdto.getChargeId());
          if (salebean != null) {
            // set the sale insurance claim amounts to zero.
            Map columnUpdtMap = new HashMap();
            columnUpdtMap.put("insurance_claim_amt", BigDecimal.ZERO);
            columnUpdtMap.put("return_insurance_claim_amt", BigDecimal.ZERO);
            storeSalesDetailsDAO.update(con, columnUpdtMap, "sale_id",
                (String) salebean.get("sale_id"));
          }
        }
      }
    }
    // finally set the charge amounts
    success = new ChargeDAO(con).setInsuranceAmounts(chargeList);
    BasicDynaBean bill = new BillDAO().findByKey("bill_no", billDetails.getBill().getBillNo());
    BillChargeTaxDAO billChargeTaxDAO = new BillChargeTaxDAO();
    billChargeTaxDAO.calculateAndUpdateBillChargeTaxes(con, chargeList, bill);

    if (null != planIds) {
      for (ChargeDTO charge : chargeList) {
        if (!charge.getChargeGroup().equals("MED") && !charge.getChargeGroup().equals("RET")) {
          charge.setInsuranceAmt(planIds, billDetails.getBill().getVisitType(),
              charge.getFirstOfCategory());
        }
      }
      new BillChargeClaimDAO().updateBillChargeClaims(con, chargeList, visitId, billNo, planIds,
          false);
    }

    Iterator<ChargeDTO> itr = chargeList.iterator();
    while (itr.hasNext()) {
      ChargeDTO cdto = itr.next();
      // Cancel Claim Service TAX when disconnecting from insurance
      if (!isTpa && cdto.getChargeHead().equals("CSTAX")) {
        chargeDAO.cancelCharge(con, cdto.getChargeId());
      }
    }

    return success;
  }

  /**
   * Update bill charges for policy new.
   *
   * @param con
   *          the con
   * @param visitId
   *          the visit id
   * @param isTpa
   *          the is tpa
   * @param billNo
   *          the bill no
   * @param planIds
   *          the plan ids
   * @return true, if successful
   * @throws Exception
   *           the exception
   */
  public static boolean updateBillChargesForPolicyNew(Connection con, String visitId, boolean isTpa,
      String billNo, int[] planIds) throws Exception {
    BillBO billBOObj = new BillBO();
    ChargeDAO chargeDAO = new ChargeDAO(con);
    BillDetails billDetails = null;
    BillChargeTaxDAO billChargeTaxDAO = new BillChargeTaxDAO();

    billDetails = billBOObj.getBillDetails(con, billNo);
    List<ChargeDTO> chargeList = billDetails.getCharges();
    Iterator<ChargeDTO> it = chargeList.iterator();
    while (it.hasNext()) {
      ChargeDTO cdto = it.next();
      cdto.getActDescription();
      if (cdto.getVisitId() == null || cdto.getVisitId().equals("")) {
        cdto.setVisitId(billDetails.getBill().getVisitId());
      }

      if (isTpa && cdto.getInsurancePayable() != null && cdto.getInsurancePayable().equals("Y")) {
        if (cdto.getChargeGroup().equals("MED")) {
          /*
           * Set appropriate insurance amounts in sales details.
           */
          setMedicineInsuranceAmtForPlan(con, planIds, cdto);
        }
        if (cdto.getChargeHead().equals("PHRET") || cdto.getChargeHead().equals("PHCRET")) {
          /*
           * Set appropriate insurance amounts in sales details.
           */
          setMedicineInsuranceAmtForPlan(con, planIds, cdto, true);
        }
      } else {
        cdto.setInsuranceClaimAmount(BigDecimal.ZERO);
        cdto.setReturnInsuranceClaimAmt(BigDecimal.ZERO);
        cdto.setOrigInsuranceClaimAmount(BigDecimal.ZERO);
        if (cdto.getChargeGroup().equals("MED") || cdto.getChargeHead().equals("PHRET")
            || cdto.getChargeHead().equals("PHCRET")) {
          // Set claim amount to zero, in sales bill.
          BasicDynaBean salebean = storeSalesMainDAO.findByKey("charge_id", cdto.getChargeId());
          if (salebean != null) {
            // set the sale insurance claim amounts to zero.
            Map columnUpdtMap = new HashMap();
            columnUpdtMap.put("insurance_claim_amt", BigDecimal.ZERO);
            columnUpdtMap.put("return_insurance_claim_amt", BigDecimal.ZERO);
            storeSalesDetailsDAO.update(con, columnUpdtMap, "sale_id",
                (String) salebean.get("sale_id"));
          }
        }
      }
    }
    // finally set the charge amounts
    // success = new ChargeDAO(con).setInsuranceAmounts(chargeList);
    BasicDynaBean bill = new BillDAO().findByKey("bill_no", billDetails.getBill().getBillNo());
    billChargeTaxDAO.calculateAndUpdateBillChargeTaxes(con, chargeList, bill);

    if (null != planIds) {
      new BillChargeClaimDAO().updateBillChargeClaims(con, chargeList, visitId, billNo, planIds,
          false);
    }

    Iterator<ChargeDTO> itr = chargeList.iterator();
    while (itr.hasNext()) {
      ChargeDTO cdto = itr.next();
      // Cancel Claim Service TAX when disconnecting from insurance
      if (!isTpa && cdto.getChargeHead().equals("CSTAX")) {
        chargeDAO.cancelCharge(con, cdto.getChargeId());
      }
    }

    return true;
  }

  /**
   * Check DRG update bill charges for policy.
   *
   * @param con
   *          the con
   * @param visitId
   *          the visit id
   * @param isTpa
   *          the is tpa
   * @param billNo
   *          the bill no
   * @param planIds
   *          the plan ids
   * @return true, if successful
   * @throws Exception
   *           the exception
   */
  // Check If the patient has DRG and update charges.
  public static boolean checkDRGUpdateBillChargesForPolicy(Connection con, String visitId,
      boolean isTpa, String billNo, int[] planIds) throws Exception {
    boolean useDRG = VisitDetailsDAO.visitUsesDRG(con, visitId);
    boolean success = true;

    if (!useDRG) {
      success = updateBillChargesForPolicyNew(con, visitId, isTpa, billNo, planIds);
      if (!success) {
        return success;
      }

      String userId = (String) RequestContext.getSession().getAttribute("userid");
      success = BillBO.updateReturns(con, userId, billNo, visitId);
      if (!success) {
        return success;
      }
    }
    return success;
  }

  /**
   * Sets the medicine insurance amt for plan.
   *
   * @param con
   *          the con
   * @param planId
   *          the plan id
   * @param cdto
   *          the cdto
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public static void setMedicineInsuranceAmtForPlan(Connection con, int[] planId, ChargeDTO cdto)
      throws SQLException, IOException {
    setMedicineInsuranceAmtForPlan(con, planId, cdto, false);
  }

  /**
   * Evry time edit insurence happens deleting old sales claim rows nd inserts new sales claim
   * details.
   *
   * @param con
   *          the con
   * @param planIds
   *          the plan ids
   * @param cdto
   *          the cdto
   * @param isReturn
   *          the is return
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public static void setMedicineInsuranceAmtForPlan(Connection con, int[] planIds, ChargeDTO cdto,
      Boolean isReturn) throws SQLException, IOException {

    AdvanceInsuranceCalculator advClaimCalc = new AdvanceInsuranceCalculator();
    String chargeId = cdto.getChargeId();
    BasicDynaBean salebean = storeSalesMainDAO.findByKey("charge_id", chargeId);
    if (salebean == null) {
      return;
    }

    String saleId = (String) salebean.get("sale_id");
    List<BasicDynaBean> saleItems = MedicineSalesDAO.getSaleItemsDetails(saleId);

    String visitType = cdto.getVisitType();
    String visitId = cdto.getVisitId();
    BigDecimal totalClaimAmount = BigDecimal.ZERO;
    SalesClaimDetailsDAO salesClaimDAO = new SalesClaimDetailsDAO();
    BillChargeClaimDAO billChargeClaimDAO = new BillChargeClaimDAO();
    PatientInsurancePlanDAO insPlanDAO = new PatientInsurancePlanDAO();
    ChargeDAO cdao = new ChargeDAO(con);
    BillingHelper billingHelper = new BillingHelper();
    Map keys = new HashMap();
    int noOfPlans = planIds == null ? 0 : planIds.length;
    BigDecimal[] conssolidatedClaimAmtsOfSponsors = new BigDecimal[noOfPlans];
    String[] saleItemIds = new String[saleItems.size()];

    for (int j = 0; j < saleItems.size(); j++) {

      BasicDynaBean saleItem = saleItems.get(j);
      BigDecimal claimAmount = BigDecimal.ZERO;
      BigDecimal[] claimAmounts = new BigDecimal[noOfPlans];
      BigDecimal amount = (BigDecimal) saleItem.get("net_amount");
      BigDecimal returnAmount = (BigDecimal) saleItem.get("return_amt");
      BigDecimal netAmount = amount.add(returnAmount);
      BigDecimal patientAmount = netAmount;
      boolean claimable = (Boolean) saleItem.get("claimable");
      BasicDynaBean salesClaimBean = null;
      // delete old sales claim details
      salesClaimDAO.delete(con, "sale_item_id", (Integer) saleItem.get("sale_item_id"));

      saleItemIds[j] = saleItem.get("sale_item_id").toString();

      if (noOfPlans == 0) {
        claimAmount = claimable && !isReturn ? netAmount : BigDecimal.ZERO;

      } else {

        if (cdto.getVisitType() == null || cdto.getVisitType().equals("")) {
          if (cdto.getBillNo() != null && !cdto.getBillNo().equals("")) {
            visitType = BillDAO.getBillTypeAndVisitType(con, cdto.getBillNo()).getVisitType();
          }
        }
        if (cdto.getVisitId() == null || cdto.getVisitId().equals("")) {
          if (cdto.getBillNo() != null && !cdto.getBillNo().equals("")) {
            visitId = BillDAO.getBillTypeAndVisitType(con, cdto.getBillNo()).getVisitId();
          }
        }

        List<BasicDynaBean> saleClaimDetails = salesClaimDAO.listAll(con, null, "sale_item_id",
            (Integer) saleItem.get("sale_item_id"), "sales_item_plan_claim_id");

        for (int i = 0; i < noOfPlans; i++) {

          BasicDynaBean planDetails = PlanDetailsDAO.getChargeAmtForPlan(planIds[i],
              (Integer) saleItem.get("insurance_category_id"), visitType);

          if (planDetails != null) {
            claimAmount = advClaimCalc.calculateClaim(netAmount,
                (BigDecimal) saleItem.get("itemwise_discount"), (String) saleItem.get("bill_no"),
                planIds[i], cdto.getFirstOfCategory(), visitType,
                (Integer) saleItem.get("insurance_category_id"));
            patientAmount = netAmount.subtract(claimAmount);

          } else {
            // this means that there is no row in the plan details for this category.
            // hence, we assume full amount is paid by insurance
            claimAmount = netAmount;
          }
          claimAmounts[i] = claimAmount;
          netAmount = netAmount.subtract(claimAmount);// amount for next plan
          String sponsorId = insPlanDAO.getSponsorId(con, visitId, planIds[i]);
          String claimId = billChargeClaimDAO.getClaimId(con, planIds[i], cdto.getBillNo(), visitId,
              sponsorId);

          // insert new sale claim details
          salesClaimBean = salesClaimDAO.getBean();
          salesClaimBean.set("claim_id", claimId);
          salesClaimBean.set("sale_item_id", (Integer) saleItem.get("sale_item_id"));
          salesClaimBean.set("claim_status", (String) saleItem.get("claim_status"));
          salesClaimBean.set("insurance_claim_amt", isReturn ? BigDecimal.ZERO : claimAmount);
          salesClaimBean.set("claim_recd", (BigDecimal) saleItem.get("claim_recd_total"));
          salesClaimBean.set("denial_code", (Integer) saleItem.get("denial_code"));
          MedicineSalesDTO saleItemDto = new MedicineSalesDTO();
          saleItemDto.setMedicineId(saleItem.get("medicine_id").toString());
          billingHelper.checkSaleItemsForInsCatInRedis(cdto, saleItemDto, planIds[i]);
          salesClaimBean.set("insurance_category_id", saleItemDto.getInsuranceCategoryId());
          salesClaimBean.set("return_insurance_claim_amt", BigDecimal.ZERO);
          salesClaimBean.set("ref_insurance_claim_amount",
              isReturn ? BigDecimal.ZERO : claimAmount);
          salesClaimBean.set("prior_auth_id", (String) saleItem.get("prior_auth_id"));
          salesClaimBean.set("prior_auth_mode_id", (Integer) saleItem.get("prior_auth_mode_id"));
          salesClaimBean.set("sponsor_id", sponsorId);

          salesClaimDAO.insert(con, salesClaimBean);

        }
        // update bill charge claim row
      }
      totalClaimAmount = totalClaimAmount.add(noOfPlans > 0 ? claimAmounts[0] : claimAmount);
      HashMap updateSale = new HashMap<String, Object>();
      updateSale.put("return_insurance_claim_amt", BigDecimal.ZERO);
      updateSale.put("insurance_claim_amt",
          claimAmounts.length > 0 ? isReturn ? BigDecimal.ZERO : claimAmounts[0] : claimAmount);

      storeSalesDetailsDAO.update(con, updateSale, "sale_item_id", saleItem.get("sale_item_id"));
    }

    // update bill charge claim

    // get all sales claim details group by claim,charge,billno
    List<BasicDynaBean> salesClaimDetails = salesClaimDAO.getSalesClaimDetails(con, saleItemIds);

    if (noOfPlans > 0) {
      totalClaimAmount = BigDecimal.ZERO;
    }

    for (int k = 0; k < salesClaimDetails.size(); k++) {
      BasicDynaBean salesClaimDetail = salesClaimDetails.get(k);
      conssolidatedClaimAmtsOfSponsors[k] = (BigDecimal) salesClaimDetail
          .get("total_insurance_claim_amt");
      totalClaimAmount = totalClaimAmount.add(conssolidatedClaimAmtsOfSponsors[k]);
    }

    cdto.setInsuranceClaimAmount(totalClaimAmount);
    cdto.setClaimAmounts(conssolidatedClaimAmtsOfSponsors);
    // ensure return claim is zero, since we calculated claim after excluding returns.
    cdto.setReturnInsuranceClaimAmt(BigDecimal.ZERO);
  }

  /** The map. */
  public static HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();

  /**
   * Update insurance amt for plan. Determining and updating first of category (FOC). 1. Fetch all
   * charges for patient in an episode i.e bills which are connected to TPA - Open/Finalized/Closed
   * 2. According to category charge encountered, the charges are updated with plan FOC values.
   * 
   * @param con
   *          the con
   * @param visitType
   *          the visit type
   * @param billNum
   *          the bill num
   * @param planIds
   *          the plan ids
   * @param visitId
   *          the visit id
   * @param chargeDTO
   *          the charge DTO
   * @return the charge DTO
   * @throws SQLException
   *           the SQL exception
   */
  public static ChargeDTO updateInsuranceAmtForPlan(Connection con, String visitType,
      String billNum, int[] planIds, String visitId, ChargeDTO chargeDTO) throws SQLException {
    int insuranceCategoryId = chargeDTO.getInsuranceCategoryId();
    Integer categoryCount = 0;
    List<BasicDynaBean> allPrevEpisodeVisits = VisitDetailsDAO.getAllPrevEpisodeVisits(con, visitId,
        visitId);
    if (allPrevEpisodeVisits == null || allPrevEpisodeVisits.isEmpty()) {
      List<String> allPrevEpisodeBills = new ArrayList();
      for (BasicDynaBean b : allPrevEpisodeVisits) {
        List<Bill> allPrevVisitBills = new BillDAO(con)
            .getPatientBills((String) b.get("patient_id"));
        for (Bill bill : allPrevVisitBills) {
          if (!(bill.getStatus().equals("X")) && (Boolean) bill.getIs_tpa()) {
            allPrevEpisodeBills.add(bill.getBillNo());
          }
        }
      }

      for (String billNo : allPrevEpisodeBills) {
        List<BasicDynaBean> allPrevEpisodeCharges = new ChargeDAO(con)
            .getBillChargesDynaList(billNo);
        for (BasicDynaBean charge : allPrevEpisodeCharges) {
          if (!charge.get("status").equals("X") && charge.get("insurance_category_id") != null
              && ((Integer) charge.get("insurance_category_id")).equals(insuranceCategoryId)) {
            categoryCount++;
          }
        }
      }
    }
    if (categoryCount > 0) {
      chargeDTO.setInsuranceAmt(planIds, visitType, false, false);
      return chargeDTO;
    } else {
      List<Bill> allCurrVisitBills = new BillDAO(con).getPatientBills(visitId);
      List<String> allActiveCurrVisitBills = new ArrayList();
      for (Bill bill : allCurrVisitBills) {
        if (!(bill.getStatus().equals("X")) && (Boolean) bill.getIs_tpa()) {
          allActiveCurrVisitBills.add(bill.getBillNo());
        }
      }

      for (String billNo : allActiveCurrVisitBills) {
        List<BasicDynaBean> allBillCharges = new ChargeDAO(con).getBillChargesDynaList(billNo);
        for (BasicDynaBean charge : allBillCharges) {
          if (!charge.get("status").equals("X") && charge.get("insurance_category_id") != null
              && ((Integer) charge.get("insurance_category_id")).equals(insuranceCategoryId)) {

            if (map.containsKey((Integer) charge.get("insurance_category_id"))) {
              int count = (Integer) map.get(charge.get("insurance_category_id"));
              count++;
              map.put((Integer) charge.get("insurance_category_id"), count);
              chargeDTO.setFirstOfCategory(false);
              chargeDTO.setInsuranceAmt(planIds, visitType, false, false);
              break;
            } else {
              map.put((Integer) charge.get("insurance_category_id"), 1);
              chargeDTO.setFirstOfCategory(true);
              chargeDTO.setInsuranceAmt(planIds, visitType, true, false);
              break;
            }
          } else if (charge.get("status").equals("X")) {
            chargeDTO.setFirstOfCategory(false);
          }
        }
      }

    }
    return chargeDTO;
  }

  /**
   * Removes the insurance.
   *
   * @param con
   *          the con
   * @param visitId
   *          the visit id
   * @param userName
   *          the user name
   * @return the string
   * @throws Exception
   *           the exception
   */
  public static String removeInsurance(Connection con, String visitId, String userName)
      throws Exception {
    BasicDynaBean visitdetails = new VisitDetailsDAO().findByKey("patient_id", visitId);

    Integer visitInsuranceId = (Integer) visitdetails.get("insurance_id");

    BasicDynaBean insuranceBean = new InsuranceDAO().findByKey("insurance_id", visitInsuranceId);

    // Disconnect bill no. from insurance.
    Map fields = new HashMap();
    // fields.put("bill_no", null);
    fields.put("policy_holder_name", null);
    fields.put("policy_no", null);
    fields.put("insurance_no", null);
    fields.put("policy_validity_start", null);
    fields.put("policy_validity_end", null);

    Map keys = new HashMap();
    keys.put("insurance_id", visitInsuranceId);

    DataBaseUtil.dynaUpdate(con, "insurance_case", fields, keys);

    // Disconnect tpa_id , insurance_id from registration
    fields = new HashMap();
    fields.put("primary_sponsor_id", null);
    fields.put("secondary_sponsor_id", null);
    fields.put("insurance_id", null);
    fields.put("plan_id", 0);
    fields.put("category_id", 0);
    fields.put("primary_insurance_co", null);
    fields.put("secondary_insurance_co", null);
    fields.put("primary_insurance_approval", 0);
    fields.put("secondary_insurance_approval", 0);
    fields.put("patient_national_sponsor_id", 0);
    fields.put("patient_policy_id", 0);
    fields.put("patient_corporate_id ", 0);
    fields.put("secondary_patient_national_sponsor_id", 0);
    fields.put("secondary_patient_corporate_id ", 0);
    fields.put("user_name ", userName);
    keys = new HashMap();
    keys.put("patient_id", visitId);
    DataBaseUtil.dynaUpdate(con, "patient_registration", fields, keys);
    return null;
  }

  /**
   * Creates the bill and add charge.
   *
   * @param con
   *          the con
   * @param bill
   *          the bill
   * @param ipVisitId
   *          the ip visit id
   * @param orgId
   *          the org id
   * @param bedType
   *          the bed type
   * @param username
   *          the username
   * @param isInsurance
   *          the is insurance
   * @param planIds
   *          the plan ids
   * @param patientCategoryId
   *          the patient category id
   * @param openDate
   *          the open date
   * @return the string
   * @throws Exception
   *           the exception
   */
  public static String createBillAndAddCharge(Connection con, Bill bill, String ipVisitId,
      String orgId, String bedType, String username, boolean isInsurance, int[] planIds,
      int patientCategoryId, Timestamp openDate) throws Exception {

    bill.setBillType(Bill.BILL_TYPE_CREDIT);
    bill.setIsPrimaryBill("Y");
    bill.setOpenDate(new java.util.Date());
    bill.setStatus("A");
    bill.setOpenedBy(username);
    bill.setUserName(username);
    bill.setStatus(Bill.BILL_STATUS_OPEN);
    bill.setOkToDischarge(Bill.BILL_DISCHARGE_NOTOK);
    bill.setVisitId(ipVisitId);
    bill.setVisitType("i");
    bill.setDepositSetOff(BigDecimal.ZERO);
    bill.setPrimaryApprovalAmount(new BigDecimal(0));
    bill.setSecondaryApprovalAmount(new BigDecimal(0));
    bill.setBillRatePlanId(orgId);
    bill.setOpenDate(openDate);

    Map msgMap = new BillBO().createNewBill(con, bill, false);
    if (msgMap.get("error") != null && !msgMap.get("error").equals("")) {
      return (String) msgMap.get("error");
    }

    // BUG : 20027 - Post Reg charges if patient category is selected and registration charge
    // applicable = 'Y'
    String regChargeApplicable = "Y";

    if (patientCategoryId != 0) {
      BasicDynaBean bean = new PatientCategoryDAO().findByKey("category_id", patientCategoryId);
      if (bean != null && bean.get("registration_charge_applicable") != null) {
        regChargeApplicable = (String) bean.get("registration_charge_applicable");
      }
    }

    if (regChargeApplicable != null && regChargeApplicable.equals("Y")) {
      ChargeDAO chargeDAO = new ChargeDAO(con);
      List<ChargeDTO> regCharges = OrderBO.getRegistrationCharges(bedType, orgId, "IPREG", false,
          isInsurance, planIds, true, "i", ipVisitId, con, null); // true: skip if charge is 0.

      BasicDynaBean recordPref = new GenericDAO("registration_preferences").getRecord();
      String conversionDescription = recordPref.get("default_op_ip_description") != null
          ? recordPref.get("default_op_ip_description").toString() : "OP to IP Conversion";

      for (ChargeDTO charge : regCharges) {
        charge.setBillNo(bill.getBillNo());
        charge.setChargeId(chargeDAO.getNextChargeId());
        charge.setUsername(username);
        charge.setActDescription(conversionDescription);
        charge.setInsuranceAmt(planIds, charge.getVisitType(), charge.getFirstOfCategory());
      }

      chargeDAO.insertCharges(regCharges);
      BillChargeClaimDAO chargeClaimDAO = new BillChargeClaimDAO();
      if (bill.getIs_tpa()) {
        chargeClaimDAO.insertBillChargeClaims(con, regCharges, planIds, ipVisitId,
            bill.getBillNo());
      }
    }
    return null;
  }

  /** The Constant GET_ACTIVE_BILLS_FOR_THE_VISIT. */
  public static final String GET_ACTIVE_BILLS_FOR_THE_VISIT = "SELECT bill_no FROM bill WHERE "
      + " visit_id=? AND status NOT IN ('F', 'C', 'X')";

  /**
   * Are any bills open for this visit.
   *
   * @param visitId
   *          the visit id
   * @return the boolean
   * @throws SQLException
   *           the SQL exception
   */
  public static Boolean areAnyBillsOpenForThisVisit(String visitId) throws SQLException {
    PreparedStatement ps = null;
    Connection con = DataBaseUtil.getReadOnlyConnection();
    try {
      ps = con.prepareStatement(GET_ACTIVE_BILLS_FOR_THE_VISIT);
      ps.setString(1, visitId);
      List list = DataBaseUtil.queryToDynaList(ps);

      if (list != null && list.size() > 0 && !list.isEmpty()) {
        return true;
      } else {
        return false;
      }
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Update visit ids.
   *
   * @param con
   *          the con
   * @param opVisitId
   *          the op visit id
   * @param ipVisitId
   *          the ip visit id
   * @return the string
   * @throws Exception
   *           the exception
   */
  public static String updateVisitIds(Connection con, String opVisitId, String ipVisitId)
      throws Exception {
    try {
      // Update op visit set inactive
      Map fields = new HashMap();
      Map keys = new HashMap();
      fields.put("status", "I");
      fields.put("mrd_remarks", "Converted to " + ipVisitId);
      keys.put("patient_id", opVisitId);

      VisitDetailsDAO visitdao = new VisitDetailsDAO();
      visitdao.update(con, fields, keys);

      Map oldVisit = new HashMap();
      Map newVisit = new HashMap();
      oldVisit.put("patient_id", opVisitId);
      newVisit.put("patient_id", ipVisitId);

      // Update all related tables with new visitid (ipVisitId)

      DataBaseUtil.dynaUpdate(con, "tests_conducted", newVisit, oldVisit);
      DataBaseUtil.dynaUpdate(con, "test_visit_reports", newVisit, oldVisit);
      DataBaseUtil.dynaUpdate(con, "doctor_prescription", newVisit, oldVisit);
      DataBaseUtil.dynaUpdate(con, "other_services_prescribed", newVisit, oldVisit);
      DataBaseUtil.dynaUpdate(con, "patient_bed_eqipmentcharges", newVisit, oldVisit);

      oldVisit = new HashMap();
      newVisit = new HashMap();
      oldVisit.put("visit_id", opVisitId);
      newVisit.put("visit_id", ipVisitId);

      DataBaseUtil.dynaUpdate(con, "diet_prescribed", newVisit, oldVisit);

      oldVisit = new HashMap();
      newVisit = new HashMap();
      oldVisit.put("activity_id", opVisitId);
      newVisit.put("activity_id", ipVisitId);

      DataBaseUtil.dynaUpdate(con, "bill_activity_charge", newVisit, oldVisit);

    } catch (Exception exception) {
      throw exception;
    }
    return null;
  }

  /**
   * Discharge patient or close visit.
   *
   * @param con
   *          the con
   * @param map
   *          the map
   * @return the string
   * @throws Exception
   *           the exception
   */
  public static String dischargePatientOrCloseVisit(Connection con, HashMap map) throws Exception {
    try {
      String disDate = DataBaseUtil.dateFormatter.format(new java.util.Date());
      String disTime = new java.sql.Time(new java.util.Date().getTime()).toString();
      List prevBedDetails = new IPBedDAO().getBeds((String) map.get("visitId"));
      BedDTO bed = null;
      for (int i = 0; i < prevBedDetails.size(); i++) {
        bed = (BedDTO) prevBedDetails.get(i);
        if (bed.getBed_state().equals("F")
            && (bed.getStatus().equals("A") || bed.getStatus().equals("C"))) {
          SimpleDateFormat timeFornmater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
          SimpleDateFormat dateFornmater = new SimpleDateFormat("yyyy-MM-dd");
          // greater among currentdate and bed finalized date will be discharged date of patient
          if (DataBaseUtil.getDateandTime().compareTo(timeFornmater.parse(bed.getEnddate())) > 0) {
            disDate = DataBaseUtil.dateFormatter.format(new java.util.Date());
            disTime = new java.sql.Time(new java.util.Date().getTime()).toString();
          } else {

            disDate = DataBaseUtil.dateFormatter
                .format(dateFornmater.parse(bed.getEnddate().split(" ")[0]));
            disTime = bed.getEnddate().split(" ")[1];
            // To Do : display msg...
            // returndto.setDischargedMsg("Bed is finalised for "+dis_date+"
            // "+dis_time.substring(0,5)+",this will be the discharge date");
          }
        }
        if (!bed.getBed_state().equals("F")) {
          disDate = DataBaseUtil.dateFormatter.format(new java.util.Date());
          disTime = new java.sql.Time(new java.util.Date().getTime()).toString();
          break;
        }
      }
      map.put("dis_date", disDate);
      map.put("dis_time", disTime);
      String mrNo = (String) map.get("mr_no");
      String visitId = (String) map.get("visitId");
      String userName = (String) map.get("userName");
      new DischargeSummaryBOImpl().dischargePatient(con, mrNo, visitId, userName, disDate, disTime);
    } catch (Exception exception) {
      throw exception;
    }
    return null;
  }

  /**
   * Check rate plan validity.
   *
   * @param ratePlanId
   *          the rate plan id
   * @param currentDate
   *          the current date
   * @return the string
   * @throws SQLException
   *           the SQL exception
   */
  public static String checkRatePlanValidity(String ratePlanId, Date currentDate)
      throws SQLException {

    BasicDynaBean orgbean = OrgMasterDao.getOrgdetailsDynaBean(ratePlanId);
    Date validTo = null;

    if (orgbean != null) {
      Boolean hasValidity = (Boolean) orgbean.get("has_date_validity");
      if (orgbean.get("valid_to_date") != null) {
        validTo = (Date) orgbean.get("valid_to_date");
      }
      if (!hasValidity || (hasValidity && validTo != null && validTo.compareTo(currentDate) >= 0)) {
        return "true";
      }
    }
    return "false";
  }

  /**
   * Check sponsor validity.
   *
   * @param sponsorId
   *          the sponsor id
   * @param currentDate
   *          the current date
   * @return the string
   * @throws SQLException
   *           the SQL exception
   */
  public static String checkSponsorValidity(String sponsorId, Date currentDate)
      throws SQLException {

    BasicDynaBean tpabean = new TpaMasterDAO().findByKey("tpa_id", sponsorId);
    Date validityEndDate = null;

    if (tpabean != null) {
      if (tpabean.get("validity_end_date") != null) {
        validityEndDate = (Date) tpabean.get("validity_end_date");
      }
      if (validityEndDate == null
          || (validityEndDate != null && validityEndDate.compareTo(currentDate) >= 0)) {
        return "true";
      }
    }
    return "false";
  }

  /** The Constant ADT_BILL_AND_PAYMENT_STATUS. */
  private static final String ADT_BILL_AND_PAYMENT_STATUS = "SELECT * FROM "
      + " adt_bill_and_discharge_status_view WHERE visit_id = ?";

  /** The Constant OP_BILL_AND_PAYMENT_STATUS. */
  private static final String OP_BILL_AND_PAYMENT_STATUS = "SELECT * FROM "
      + " op_bill_and_discharge_status_view WHERE visit_id = ?";

  /**
   * Gets the bill and payment status.
   *
   * @param visitId
   *          the visit id
   * @param visitType
   *          the visit type
   * @return the bill and payment status
   * @throws SQLException
   *           the SQL exception
   */
  public static BasicDynaBean getBillAndPaymentStatus(String visitId, String visitType)
      throws SQLException {
    if (visitType.equals("i")) {
      return DataBaseUtil.queryToDynaBean(ADT_BILL_AND_PAYMENT_STATUS, visitId);
    } else if (visitType.equals("o")) {
      return DataBaseUtil.queryToDynaBean(OP_BILL_AND_PAYMENT_STATUS, visitId);
    } else {
      return null;
    }
  }

  /**
   * Update MLC.
   *
   * @param con
   *          the con
   * @param mrNo
   *          the mr no
   * @param visitId
   *          the visit id
   * @param orgId
   *          the org id
   * @param bedType
   *          the bed type
   * @param templateIdFormat
   *          the template id format
   * @param templateName
   *          the template name
   * @param billNo
   *          the bill no
   * @param userName
   *          the user name
   * @param isInsurance
   *          the is insurance
   * @param planIds
   *          the plan ids
   * @return the string
   * @throws Exception
   *           the exception
   */
  public static String updateMLC(Connection con, String mrNo, String visitId, String orgId,
      String bedType, String templateIdFormat, String templateName, String billNo, String userName,
      boolean isInsurance, int[] planIds) throws Exception {

    Map map = new HashMap();
    map.put("mr_no", mrNo);
    map.put("patient_id", visitId);
    map.put("doc_name", templateName);
    map.put("username", userName);
    map.put("mlc_template_id", templateIdFormat);

    new MLCDocumentAbstractImpl().create(map, con);

    // adding new mlc charge to bill_charge table
    ChargeDAO chargeDAO = new ChargeDAO(con);
    String visitType = BillDAO.getBillTypeAndVisitType(con, billNo).getVisitType();
    List<ChargeDTO> regCharges = OrderBO.getRegistrationCharges(bedType, orgId, "MLREG", false,
        isInsurance, planIds, false, visitType, visitId, con, null); // true: skip if charge is 0.

    if (regCharges.size() > 0) {
      ChargeDTO charge = regCharges.get(0);

      charge.setBillNo(billNo);
      charge.setChargeId(chargeDAO.getNextChargeId());
      charge.setUsername(userName);
      charge.setInsuranceAmt(planIds, visitType, charge.getFirstOfCategory());
      chargeDAO.insertCharge(charge);
      chargeDAO.insertActivity(charge.getChargeId(), visitId, charge.getChargeHead());
    }

    BillChargeClaimDAO billChgClaimDAO = new BillChargeClaimDAO();
    billChgClaimDAO.insertBillChargeClaims(con, regCharges, planIds, visitId, billNo);

    return null;
  }

  /**
   * Delete MLC.
   *
   * @param con
   *          the con
   * @param mrNo
   *          the mr no
   * @param visitId
   *          the visit id
   * @param userName
   *          the user name
   * @return the string
   * @throws Exception
   *           the exception
   */
  public static String deleteMLC(Connection con, String mrNo, String visitId, String userName)
      throws Exception {
    GenericDAO gdao = new GenericDAO("bill_activity_charge");
    ChargeDAO chargeDao = new ChargeDAO(con);
    String chargeId = BillActivityChargeDAO.getChargeId("MLREG", visitId);
    if (chargeId != null) {
      String billStatus = chargeDao.getBillStatus(chargeId);
      if (!billStatus.equals("A")) {
        return "Cannot delete MLC details. Please reopen the bill having MLC charge.";
      }
      gdao.delete(con, "activity_id", chargeId);
      chargeDao.updateActivity(chargeId, "X");
    }
    MLCDocumentsBO.deleteMLCDetails(con, mrNo, visitId, userName);
    return null;
  }

  /** The claim bills map. */
  public static HashMap<String, String> claimBillsMap = new HashMap<String, String>();

  /**
   * Checks if is tpa valid for ip.
   *
   * @param patientId
   *          the patient id
   * @param tpaId
   *          the tpa id
   * @return true, if is tpa valid for ip
   * @throws SQLException
   *           the SQL exception
   */
  public static boolean isTpaValidForIp(String patientId, String tpaId) throws SQLException {
    if (tpaId == null || tpaId.equals("")) {
      return true;
    }
    BasicDynaBean visitBean = VisitDetailsDAO.getVisitDetails(patientId);
    Integer patientCategory = visitBean.get("patient_category_id") == null
        || visitBean.get("patient_category_id").equals("") ? null
            : (Integer) visitBean.get("patient_category_id");
    List<BasicDynaBean> patCatBeanList = new ArrayList<BasicDynaBean>();
    if (patientCategory != null && !patientCategory.equals("")) {
      patCatBeanList = PatientCategoryDAO.getAllowedSponsors(patientCategory, "i");
      if (patCatBeanList != null && !patCatBeanList.isEmpty()) {
        for (BasicDynaBean b : patCatBeanList) {
          if (b.get("tpa_id").equals(tpaId)) {
            return true;
          }

        }

      } else {
        return false;
      }
    }
    return false;
  }

  /**
   * Gets the allowed rate plans.
   *
   * @param patientId
   *          the patient id
   * @return the allowed rate plans
   * @throws SQLException
   *           the SQL exception
   */
  public static Map getAllowedRatePlans(String patientId) throws SQLException {
    return getAllowedRatePlans(patientId, null);
  }

  /**
   * Gets the allowed rate plans.
   *
   * @param patientId
   *          the patient id
   * @param ofVisitType
   *          the of visit type
   * @return the allowed rate plans
   * @throws SQLException
   *           the SQL exception
   */
  public static Map getAllowedRatePlans(String patientId, String ofVisitType) throws SQLException {
    List ratePlanList = new ArrayList();
    List<Hashtable> origRatePlanList = OrgMasterDao.getorgnames();
    Boolean isGeneralRatePlanPresent = false;
    BasicDynaBean visitBean = VisitDetailsDAO.getVisitDetails(patientId);
    Integer patientCategory = visitBean.get("patient_category_id") == null
        || visitBean.get("patient_category_id").equals("") ? null
            : (Integer) visitBean.get("patient_category_id");
    PatientInsurancePlanDAO planDAO = new PatientInsurancePlanDAO();
    int[] planIds = planDAO.getPlanIds(patientId);
    Integer planId = (null != planIds && planIds.length > 0) ? planIds[0] : 0;
    List<BasicDynaBean> patCatBeanList = null;
    String patientCategoryDefaultRatePlan = null;
    String planDefaultRatePlan = null;
    String errorMsg = "";

    // if patient category is present, populate with category-wise, rate plan list based on the
    // patient visit type
    if (patientCategory != null && !patientCategory.equals("")) {
      BasicDynaBean defltRatePlanBean = PatientCategoryDAO.getDefaultRatePlan(patientCategory,
          ofVisitType != null ? ofVisitType : (String) visitBean.get("visit_type"));
      patientCategoryDefaultRatePlan = defltRatePlanBean == null
          || defltRatePlanBean.get("org_id") == null ? null
              : (String) defltRatePlanBean.get("org_id");
      patCatBeanList = PatientCategoryDAO.getAllowedRatePlans(patientCategory,
          ofVisitType != null ? ofVisitType : (String) visitBean.get("visit_type"));
    }

    if (planId != 0) {
      BasicDynaBean planDetails = new GenericDAO("insurance_plan_main").findByKey("plan_id",
          planId);
      if (planDetails != null) {
        planDefaultRatePlan = planDetails.get("default_rate_plan") == null
            || planDetails.get("default_rate_plan").equals("") ? null
                : (String) planDetails.get("default_rate_plan");
      }
    }

    ratePlanList.clear();
    if (planDefaultRatePlan != null && !planDefaultRatePlan.equals("")) {
      // check whether the plan default rate plan is one amongst the
      // category allowed rate plans
      if (patCatBeanList != null && !patCatBeanList.isEmpty()) {
        for (BasicDynaBean b : patCatBeanList) {
          if (b.get("org_id").equals(planDefaultRatePlan)) {
            Hashtable temp = new Hashtable();
            temp.put("ORG_ID", b.get("org_id"));
            temp.put("ORG_NAME", b.get("org_name"));
            if (b.get("org_id").equals("ORG0001")) {
              isGeneralRatePlanPresent = true;
            }
            ratePlanList.add(temp);
          }
        }
      } else {
        // check whether the plan default rate plan is one amongst all
        // the valid rate plans
        ratePlanList.clear();
        for (Hashtable h : origRatePlanList) {
          if (h.get("ORG_ID").equals(planDefaultRatePlan)) {
            ratePlanList.add(h);
            if (h.get("ORG_ID").equals("ORG0001")) {
              isGeneralRatePlanPresent = true;
            }
          }
        }
      }
      if (ratePlanList.isEmpty()) {
        errorMsg = "No valid rate plans for category and plan available.";
      }
    } else {
      // if there is no plan default rate plan
      if (patCatBeanList != null && !patCatBeanList.isEmpty()) {
        ratePlanList.clear();
        for (BasicDynaBean b : patCatBeanList) {
          Hashtable temp = new Hashtable();
          temp.put("ORG_ID", b.get("org_id"));
          temp.put("ORG_NAME", b.get("org_name"));
          if (b.get("org_id").equals("ORG0001")) {
            isGeneralRatePlanPresent = true;
          }
          ratePlanList.add(temp);
        }
      } else {
        ratePlanList.clear();
        ratePlanList.addAll(origRatePlanList);
        for (Hashtable b : origRatePlanList) {
          if (b.get("ORG_ID").equals("ORG0001")) {
            isGeneralRatePlanPresent = true;
          }
        }
      }
    }
    Map allowdRatePlanMap = new HashMap();
    allowdRatePlanMap.put("errorMsg", errorMsg);
    allowdRatePlanMap.put("ratePlanList", ratePlanList);
    allowdRatePlanMap.put("isGeneralRatePlanPresent", isGeneralRatePlanPresent ? 'Y' : 'N');
    allowdRatePlanMap.put("defaultRatePlan",
        planDefaultRatePlan != null ? planDefaultRatePlan
            : patientCategoryDefaultRatePlan != null ? patientCategoryDefaultRatePlan
                : isGeneralRatePlanPresent ? "ORG0001" : null);
    return allowdRatePlanMap;
  }

}
