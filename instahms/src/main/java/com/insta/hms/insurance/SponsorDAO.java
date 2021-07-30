package com.insta.hms.insurance;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.billing.BillChargeClaimTaxDAO;
import com.insta.hms.billing.BillChargeTaxDAO;
import com.insta.hms.billing.BillClaimDAO;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * The Class SponsorDAO.
 */
public class SponsorDAO {
  
  private static final GenericDAO billAdjustmentAlertsDAO =
      new GenericDAO("bill_adjustment_alerts");
  
  private static final GenericDAO billChargeClaimDAO = new GenericDAO("bill_charge_claim");

  /** The Constant GET_VISIT_INS_DETAILS. */
  public static final String GET_VISIT_INS_DETAILS = "SELECT "
      + " pipd.visit_id, pipd.plan_id, pipd.insurance_category_id, "
      + " pipd.patient_amount, pipd.patient_percent,  pipd.patient_amount_cap,"
      + " pipd.per_treatment_limit, pipd.patient_type, " + " pipd.patient_amount_per_category, "
      + " ipm.is_copay_pc_on_post_discnt_amt,  pip.priority, "
      + " CASE WHEN iic.insurance_payable='Y' THEN true ELSE false END"
      + " AS is_category_payable,  " + " pip.plan_limit, "
      + " CASE WHEN (ipm.limits_include_followup='Y' AND pr.visit_type='o') "
      + " THEN pip.episode_limit ELSE pip.visit_limit END AS visit_limit , "
      + " CASE WHEN (ipm.limits_include_followup='Y' AND pr.visit_type='o') "
      + " THEN pip.episode_deductible ELSE pip.visit_deductible END AS visit_deductible , "
      + " CASE WHEN (ipm.limits_include_followup='Y' AND pr.visit_type='o') "
      + " THEN pip.episode_copay_percentage ELSE pip.visit_copay_percentage END"
      + " AS visit_copay_percentage , "
      + " CASE WHEN (ipm.limits_include_followup='Y' AND pr.visit_type='o') "
      + " THEN pip.episode_max_copay_percentage ELSE pip.visit_max_copay_percentage END"
      + "  AS visit_max_copay_percentage , "
      + " pip.visit_per_day_limit, ipm.limits_include_followup,"
      + "  pr.reg_date, pr.discharge_date, pr.visit_type, "
      + " COALESCE(ipd.category_payable, iic.insurance_payable) as plan_category_payable,"
      + " ipm.limit_type " + " FROM patient_insurance_plan_details pipd "
      + " JOIN insurance_plan_main ipm ON(ipm.plan_id = pipd.plan_id) "
      + " JOIN patient_insurance_plans pip ON "
      + " (pip.patient_id = pipd.visit_id and pip.plan_id = pipd.plan_id) "
      + " JOIN patient_registration pr ON(pip.patient_id = pr.patient_id) "
      + " LEFT JOIN insurance_plan_details ipd ON " + " (ipd.plan_id = pipd.plan_id and "
      + " ipd.insurance_category_id = pipd.insurance_category_id"
      + " AND ipd.patient_type = pipd.patient_type) " + " JOIN item_insurance_categories iic ON"
      + " (iic.insurance_category_id = pipd.insurance_category_id) "
      + " WHERE pipd.visit_id=? ORDER BY pip.priority ";

  /**
   * Gets the visit ins details.
   *
   * @param visitID the visit ID
   * @return the visit ins details
   * @throws SQLException the SQL exception
   */
  public List<BasicDynaBean> getVisitInsDetails(String visitID) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    List<BasicDynaBean> visitInsDetails = new ArrayList<BasicDynaBean>();

    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_VISIT_INS_DETAILS);
      ps.setString(1, visitID);
      visitInsDetails = DataBaseUtil.queryToDynaList(ps);
      if (null == visitInsDetails || visitInsDetails.size() == 0) {
        visitInsDetails = getInsuranceDetailsFromMaster(con, visitID);
      }

    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return visitInsDetails;
  }

  /** The Constant GET_INSURANCE_PLAN_DETAILS. */
  private static final String GET_INSURANCE_PLAN_DETAILS = " SELECT  "
      + " pipd.plan_id, pipd.insurance_category_id, "
      + "   pipd.patient_amount, pipd.patient_percent,  pipd.patient_amount_cap,"
      + "  pipd.per_treatment_limit, " + "   pipd.patient_type, pipd.patient_amount_per_category,"
      + "  ipm.is_copay_pc_on_post_discnt_amt,  "
      + "   pip.priority, CASE WHEN iic.insurance_payable='Y' THEN true ELSE false END"
      + " AS is_category_payable,  " + " pip.plan_limit, "
      + " CASE WHEN (ipm.limits_include_followup='Y' AND pr.visit_type='o') "
      + " THEN  pip.episode_limit ELSE pip.visit_limit END AS visit_limit , "
      + " CASE WHEN (ipm.limits_include_followup='Y' AND pr.visit_type='o') "
      + " THEN  pip.episode_deductible ELSE pip.visit_deductible END AS visit_deductible, "
      + " CASE WHEN (ipm.limits_include_followup='Y' AND pr.visit_type='o') "
      + " THEN  pip.episode_copay_percentage ELSE pip.visit_copay_percentage"
      + " END AS visit_copay_percentage , "
      + " CASE WHEN (ipm.limits_include_followup='Y' AND pr.visit_type='o') "
      + " THEN  pip.episode_max_copay_percentage ELSE pip.visit_max_copay_percentage"
      + " END AS visit_max_copay_percentage , "
      + " pip.visit_per_day_limit, ipm.limits_include_followup, pr.reg_date,"
      + "  pr.discharge_date, pipd.category_payable as plan_category_payable, " + " ipm.limit_type "
      + " FROM insurance_plan_details pipd "
      + " JOIN insurance_plan_main ipm ON(ipm.plan_id = pipd.plan_id) "
      + " JOIN patient_insurance_plans pip ON"
      + " (pip.patient_id = ? and pip.plan_id = pipd.plan_id) " + " JOIN patient_registration pr ON"
      + " (pip.patient_id = pr.patient_id) " + " JOIN item_insurance_categories iic ON"
      + " (iic.insurance_category_id = pipd.insurance_category_id)  "
      + " WHERE pipd.patient_type = ? ORDER BY pip.priority ";

  /**
   * Gets the insurance details from master.
   *
   * @param con     the con
   * @param visitId the visit id
   * @return the insurance details from master
   * @throws SQLException the SQL exception
   */
  private List<BasicDynaBean> getInsuranceDetailsFromMaster(Connection con, String visitId)
      throws SQLException {

    String visitType = VisitDetailsDAO.getVisitType(visitId);

    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(GET_INSURANCE_PLAN_DETAILS);
      ps.setString(1, visitId);
      ps.setString(2, visitType);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      if (null != ps) {
        ps.close();
      }
    }
  }

  /** The Constant UNLOCK_VISIT_BILLS_CHARGES. */
  public static final String UNLOCK_VISIT_BILLS_CHARGES = " UPDATE "
      + " bill_charge bc SET is_claim_locked=false "
      + " FROM bill b WHERE b.bill_no = bc.bill_no AND "
      + " bc.is_claim_locked=true AND b.visit_id=? AND b.status='A' ";

  /**
   * Unlock visit bills charges.
   *
   * @param visitId the visit id
   * @throws SQLException the SQL exception
   */
  public void unlockVisitBillsCharges(String visitId) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;

    try {
      con = DataBaseUtil.getReadOnlyConnection();
      con.setAutoCommit(false);
      ps = con.prepareStatement(UNLOCK_VISIT_BILLS_CHARGES);
      ps.setString(1, visitId);
      ps.execute();

    } finally {
      DataBaseUtil.commitClose(con, true);
      DataBaseUtil.closeConnections(con, ps);
    }
  }
  
  private static final String UNLOCK_VISIT_BILLS_CHARGES_WITHOUT_PREAUTH = "UPDATE "
      + " bill_charge bc SET is_claim_locked=false "
      + " FROM bill b WHERE b.bill_no = bc.bill_no AND bc.is_claim_locked "
      + " AND b.visit_id=? AND b.status='A' AND bc.preauth_act_id IS NULL";
  
  /**
   * Unlock visit bills charges without pre auth.
   *
   * @param visitId the visit id
   * @throws SQLException the SQL exception
   */
  public void unlockVisitBillsChargesWithoutPreAuth(String visitId) throws SQLException {
    try (Connection con = DataBaseUtil.getConnection();
        PreparedStatement ps = con.prepareStatement(UNLOCK_VISIT_BILLS_CHARGES_WITHOUT_PREAUTH);) {
      con.setAutoCommit(false);
      ps.setString(1, visitId);
      ps.execute();
      DataBaseUtil.commitClose(con, true);
    }
  }

  /** The Constant INCLUDE_BILL_CHARGES_IN_CLAIM_CALC. */
  public static final String INCLUDE_BILL_CHARGES_IN_CLAIM_CALC = "UPDATE "
      + " bill_charge_claim bcc set include_in_claim_calc = true "
      + " FROM bill b WHERE b.bill_no = bcc.bill_no AND b.visit_id=? AND b.status = 'A' ";

  /**
   * Include bill charges in claim calc.
   *
   * @param visitId the visit id
   * @throws SQLException the SQL exception
   */
  public void includeBillChargesInClaimCalc(String visitId) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;

    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      ps = con.prepareStatement(INCLUDE_BILL_CHARGES_IN_CLAIM_CALC);
      ps.setString(1, visitId);
      ps.execute();
    } finally {
      if (null != ps) {
        ps.close();
      }
      DataBaseUtil.commitClose(con, true);
    }
  }

  /** The Constant INCLUDE_ALL_BILL_CHARGES_IN_CLAIM_CALC. */
  public static final String INCLUDE_ALL_BILL_CHARGES_IN_CLAIM_CALC = "UPDATE "
      + " bill_charge_claim bcc set include_in_claim_calc = true "
      + " FROM bill b WHERE b.bill_no = bcc.bill_no AND b.visit_id=?  ";

  /**
   * Include all bill charges in claim calc.
   *
   * @param visitId the visit id
   * @throws SQLException the SQL exception
   */
  public void includeAllBillChargesInClaimCalc(String visitId) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;

    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      ps = con.prepareStatement(INCLUDE_ALL_BILL_CHARGES_IN_CLAIM_CALC);
      ps.setString(1, visitId);
      ps.execute();
    } finally {
      if (null != ps) {
        ps.close();
      }
      DataBaseUtil.commitClose(con, true);
    }
  }

  /** The Constant UNLOCK_VISIT_ALL_BILLS_CHARGES. */
  public static final String UNLOCK_VISIT_ALL_BILLS_CHARGES = " UPDATE "
      + " bill_charge bc SET is_claim_locked=false "
      + " FROM bill b WHERE b.bill_no = bc.bill_no AND "
      + " bc.is_claim_locked=true AND b.visit_id=? ";

  /**
   * Unlock visit all bills charges.
   *
   * @param visitId the visit id
   * @throws SQLException the SQL exception
   */
  public void unlockVisitAllBillsCharges(String visitId) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;

    try {
      con = DataBaseUtil.getReadOnlyConnection();
      con.setAutoCommit(false);
      ps = con.prepareStatement(UNLOCK_VISIT_ALL_BILLS_CHARGES);
      ps.setString(1, visitId);
      ps.execute();

    } finally {
      DataBaseUtil.commitClose(con, true);
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant UNLOCK_VISIT_SALE_ITEMS. */
  public static final String UNLOCK_VISIT_SALE_ITEMS = " UPDATE "
      + " store_sales_details ssd SET is_claim_locked=false "
      + " FROM store_sales_main ssm  JOIN bill b ON"
      + " (b.bill_no = ssm.bill_no) WHERE ssd.sale_id = ssm.sale_id AND "
      + " ssd.is_claim_locked=true AND b.visit_id=? AND b.status='A' ";

  /**
   * Unlock visit sale items.
   *
   * @param visitId the visit id
   * @throws SQLException the SQL exception
   */
  public void unlockVisitSaleItems(String visitId) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;

    try {
      con = DataBaseUtil.getReadOnlyConnection();
      con.setAutoCommit(false);
      ps = con.prepareStatement(UNLOCK_VISIT_SALE_ITEMS);
      ps.setString(1, visitId);
      ps.execute();

    } finally {
      DataBaseUtil.commitClose(con, true);
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant UNLOCK_VISIT_ALL_SALE_ITEMS. */
  public static final String UNLOCK_VISIT_ALL_SALE_ITEMS = " UPDATE "
      + " store_sales_details ssd SET is_claim_locked=false "
      + " FROM store_sales_main ssm  JOIN bill b ON"
      + " (b.bill_no = ssm.bill_no) WHERE ssd.sale_id = ssm.sale_id AND "
      + " ssd.is_claim_locked=true AND b.visit_id=? ";

  /**
   * Unlock visit all sale items.
   *
   * @param visitId the visit id
   * @throws SQLException the SQL exception
   */
  public void unlockVisitAllSaleItems(String visitId) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;

    try {
      con = DataBaseUtil.getReadOnlyConnection();
      con.setAutoCommit(false);
      ps = con.prepareStatement(UNLOCK_VISIT_ALL_SALE_ITEMS);
      ps.setString(1, visitId);
      ps.execute();

    } finally {
      DataBaseUtil.commitClose(con, true);
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant LOCK_VISIT_SALE_ITEMS. */
  public static final String LOCK_VISIT_SALE_ITEMS = " UPDATE "
      + " store_sales_details ssd SET is_claim_locked=true "
      + " FROM store_sales_main ssm  JOIN bill b ON"
      + " (b.bill_no = ssm.bill_no) WHERE ssd.sale_id = ssm.sale_id AND "
      + " ssd.is_claim_locked=false AND b.visit_id=? AND b.status='A' ";

  /**
   * Lock visit sale items.
   *
   * @param visitId the visit id
   * @throws SQLException the SQL exception
   */
  public void lockVisitSaleItems(String visitId) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;

    try {
      con = DataBaseUtil.getReadOnlyConnection();
      con.setAutoCommit(false);
      ps = con.prepareStatement(LOCK_VISIT_SALE_ITEMS);
      ps.setString(1, visitId);
      ps.execute();

    } finally {
      DataBaseUtil.commitClose(con, true);
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant LOCK_VISIT_ALL_SALE_ITEMS. */
  public static final String LOCK_VISIT_ALL_SALE_ITEMS = " UPDATE "
      + " store_sales_details ssd SET is_claim_locked=true "
      + " FROM store_sales_main ssm  JOIN bill b ON"
      + " (b.bill_no = ssm.bill_no) WHERE ssd.sale_id = ssm.sale_id AND "
      + " ssd.is_claim_locked=false AND b.visit_id=? ";

  /**
   * Lock visit all sale items.
   *
   * @param visitId the visit id
   * @throws SQLException the SQL exception
   */
  public void lockVisitAllSaleItems(String visitId) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;

    try {
      con = DataBaseUtil.getReadOnlyConnection();
      con.setAutoCommit(false);
      ps = con.prepareStatement(LOCK_VISIT_ALL_SALE_ITEMS);
      ps.setString(1, visitId);
      ps.execute();

    } finally {
      DataBaseUtil.commitClose(con, true);
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant UNLOCK_CHARGES. */
  public static final String UNLOCK_CHARGES = " UPDATE bill_charge SET is_claim_locked = false ";

  /**
   * Unlock charges.
   *
   * @param charges the charges
   * @throws SQLException the SQL exception
   */
  public void unlockCharges(List<String> charges) throws SQLException {
    Connection con = null;
    // PreparedStatement ps = null;

    try {
      con = DataBaseUtil.getReadOnlyConnection();
      con.setAutoCommit(false);

      String[] arrCharges = new String[charges.size()];
      arrCharges = charges.toArray(arrCharges);

      StringBuilder where = new StringBuilder();
      String[] placeHolderArr = new String[arrCharges.length];
      Arrays.fill(placeHolderArr, "?");
      String placeHolders = StringUtils.arrayToCommaDelimitedString(placeHolderArr);
      where.append("WHERE charge_id in ( " + placeHolders + ")");
      // DataBaseUtil.addWhereFieldInList(where, "charge_id", Arrays.asList(arrCharges));

      // ps = con.prepareStatement(UNLOCK_CHARGES + where);
      DataBaseUtil.executeQuery(con, UNLOCK_CHARGES + where.toString(), arrCharges);

      // int i = 1;
      // for (String chargeId : arrCharges) {
      // ps.setString(i++, chargeId);
      // }

      // ps.executeUpdate();

    } finally {
      DataBaseUtil.commitClose(con, true);
      DataBaseUtil.closeConnections(con, null);
    }
  }

  /** The Constant GET_MAIN_VISIT_ID. */
  private static final String GET_MAIN_VISIT_ID = "SELECT main_visit_id "
      + " FROM patient_registration WHERE patient_id = ? ";

  /**
   * Gets the main visit id.
   *
   * @param visitId the visit id
   * @return the main visit id
   * @throws SQLException the SQL exception
   */
  public String getMainVisitId(String visitId) throws SQLException {

    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_MAIN_VISIT_ID);
      ps.setString(1, visitId);
      return DataBaseUtil.getStringValueFromDb(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant GET_PLAN_DETAILS. */
  private static final String GET_PLAN_DETAILS = "SELECT "
      + " pip.patient_id, pip.plan_id, ipm.limits_include_followup"
      + " FROM patient_insurance_plans pip " + " JOIN insurance_plan_main ipm ON"
      + " (pip.plan_id = ipm.plan_id AND pip.patient_id = ?) ";

  /**
   * Gets the plan details.
   *
   * @param visitId the visit id
   * @return the plan details
   * @throws SQLException the SQL exception
   */
  public List<BasicDynaBean> getPlanDetails(String visitId) throws SQLException {

    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_PLAN_DETAILS);
      ps.setString(1, visitId);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Insert bill adjustment alerts.
   *
   * @param adjMap  the adj map
   * @param visitId the visit id
   * @throws SQLException the SQL exception
   * @throws IOException  Signals that an I/O exception has occurred.
   */
  public void insertBillAdjustmentAlerts(Map<Integer, Map<Integer, Integer>> adjMap, String visitId)
      throws SQLException, IOException {

    Connection con = null;
    boolean success = false;
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);

      success = billAdjustmentAlertsDAO.delete(con, "visit_id", visitId);

      BasicDynaBean billAdjAlertBean = billAdjustmentAlertsDAO.getBean();
      for (int planId : adjMap.keySet()) {
        Map<Integer, Integer> catAdjMap = adjMap.get(planId);
        for (int categoryId : catAdjMap.keySet()) {
          Integer adjStatus = catAdjMap.get(categoryId);
          billAdjAlertBean.set("visit_id", visitId);
          billAdjAlertBean.set("plan_id", planId);
          billAdjAlertBean.set("category_id", categoryId);
          billAdjAlertBean.set("adjstment_status", adjStatus);

          success = billAdjustmentAlertsDAO.insert(con, billAdjAlertBean);
        }
      }

    } finally {
      DataBaseUtil.commitClose(con, success);
    }
  }

  /** The Constant LOCK_BILL_CHARGES. */
  private static final String LOCK_BILL_CHARGES = "UPDATE "
      + " bill_charge SET is_claim_locked = true WHERE bill_no = ? ";

  /**
   * Lock bill charges.
   *
   * @param billNo the bill no
   * @throws SQLException the SQL exception
   */
  public void lockBillCharges(String billNo) throws SQLException {

    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      ps = con.prepareStatement(LOCK_BILL_CHARGES);
      ps.setString(1, billNo);
      ps.executeUpdate();

    } finally {
      DataBaseUtil.commitClose(con, true);
      if (ps != null) {
        ps.close();
      }
    }
  }

  /** The Constant UNLOCK_BILL_CHARGES. */
  private static final String UNLOCK_BILL_CHARGES = "UPDATE "
      + " bill_charge SET is_claim_locked = false WHERE bill_no = ? ";

  /**
   * Unlock bill charges.
   *
   * @param billNo the bill no
   * @throws SQLException the SQL exception
   */
  public void unlockBillCharges(String billNo) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      ps = con.prepareStatement(UNLOCK_BILL_CHARGES);
      ps.setString(1, billNo);
      ps.executeUpdate();

    } finally {
      DataBaseUtil.commitClose(con, true);
      if (ps != null) {
        ps.close();
      }
    }
  }

  /**
   * Update sales bill charge claim tax.
   *
   * @param patientId the patient id
   * @throws SQLException the SQL exception
   * @throws IOException  Signals that an I/O exception has occurred.
   */
  public void updateSalesBillChargeClaimTax(String patientId) throws SQLException, IOException {
    List<BasicDynaBean> saleBillChargeClaims = getSaleBillCharges(patientId);
    Connection con = null;
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      for (BasicDynaBean saleBillChargeClaim : saleBillChargeClaims) {
        String chargeId = (String) saleBillChargeClaim.get("charge_id");
        String claimId = (String) saleBillChargeClaim.get("claim_id");
        Map<String, Object> keys = new HashMap<String, Object>();
        keys.put("charge_id", chargeId);
        keys.put("claim_id", claimId);
        // BigDecimal insClaimAmt =
        // getInsuranceClaimAmtFromSalesClaimDetails(chargeId,claimId);
        BigDecimal insClaimTaxAmt = getInsuranceClaimTaxAmtFromSalesClaimDetails(chargeId, claimId);
        // saleBillChargeClaim.set("insurance_claim_amt", insClaimAmt);
        saleBillChargeClaim.set("tax_amt", insClaimTaxAmt);
        billChargeClaimDAO.update(con, saleBillChargeClaim.getMap(), keys);
      }
    } finally {
      DataBaseUtil.commitClose(con, true);
    }
  }

  /**
   * Update sales bill charges.
   *
   * @param patientId the patient id
   * @throws SQLException the SQL exception
   * @throws IOException  Signals that an I/O exception has occurred.
   */
  public void updateSalesBillCharges(String patientId) throws SQLException, IOException {
    List<BasicDynaBean> saleBillChargeClaims = getSaleBillCharges(patientId);
    Connection con = null;
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      for (BasicDynaBean saleBillChargeClaim : saleBillChargeClaims) {
        String chargeId = (String) saleBillChargeClaim.get("charge_id");
        String claimId = (String) saleBillChargeClaim.get("claim_id");
        Map<String, Object> keys = new HashMap<String, Object>();
        keys.put("charge_id", chargeId);
        keys.put("claim_id", claimId);
        BigDecimal insClaimAmt = getInsuranceClaimAmtFromSalesClaimDetails(chargeId, claimId);
        BigDecimal insClaimTaxAmt = getInsuranceClaimTaxAmtFromSalesClaimDetails(chargeId, claimId);
        saleBillChargeClaim.set("insurance_claim_amt", insClaimAmt);
        saleBillChargeClaim.set("tax_amt", insClaimTaxAmt);
        billChargeClaimDAO.update(con, saleBillChargeClaim.getMap(), keys);
      }
    } finally {
      DataBaseUtil.commitClose(con, true);
    }
  }

  /**
   * Update sale tax to bill charge tax.
   * 
   * @param saleId sale bill number of the sale
   * @param chargeId charge id of the sale
   * @return true or false
   * @throws SQLException on any error from database transaction
   * @throws IOException any exception on transaction
   */
  public boolean addupSaleTaxtoBillChrgeTax(String saleId,String chargeId)
      throws SQLException, IOException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);

      String copyTaxtobillcharge = 
            " UPDATE bill_charge SET tax_amt = "
          + " (select total_item_tax from store_sales_main where sale_id = ?) "
          + " WHERE charge_id = ?";
      ps = con.prepareStatement(copyTaxtobillcharge);
      ps.setString(1, saleId);
      ps.setString(2, chargeId);
      return ps.executeUpdate() > 0;
    } finally {
      DataBaseUtil.commitClose(con, true);
    }

  }

  /**
   * Update sale tax to bill charge tax.
   * 
   * @param billNo hospital bill number of the sale
   * @param chargeId charge id of the sale
   * @return true or false
   * @throws SQLException on any error from database transaction
   * @throws IOException any exception on transaction
   */
  public boolean addupSaleClaimTaxtoBillChrgeClaimTax(String billNo,String chargeId)
      throws SQLException, IOException {
    Connection con = null;
    PreparedStatement ps = null;
    List<BasicDynaBean> claims = null;
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      BillClaimDAO claimDao = new BillClaimDAO();
      claims = claimDao.getOpenBillClaims(con, billNo);


      String copyTaxtobillcharge = 
          " UPDATE bill_charge_claim_tax bcct SET sponsor_tax_amount = st.tax_amt"
          + " FROM (SELECT SUM(sctd.tax_amt) as tax_amt, sctd.item_subgroup_id "
          + " FROM store_sales_main ssm "
          + " JOIN store_sales_details ssd ON(ssd.sale_id = ssm.sale_id) "
          + " JOIN sales_claim_details scd ON(scd.sale_item_id = ssd.sale_item_id) "
          + " JOIN sales_claim_tax_details sctd ON (scd.sale_item_id = sctd.sale_item_id)"
          + " WHERE ssm.charge_id = ?  AND scd.claim_id = ? "
          + " GROUP BY sctd.item_subgroup_id) st"
          + " WHERE charge_id = ? and claim_id = ? and bcct.tax_sub_group_id = st.item_subgroup_id";

      ps = con.prepareStatement(copyTaxtobillcharge);

      for ( BasicDynaBean claim : claims ) {

        ps.setString(1, chargeId);
        ps.setString(2, (String)claim.get("claim_id"));
        ps.setString(3, chargeId);
        ps.setString(4, (String)claim.get("claim_id"));
        ps.addBatch();
      }

      return ps.executeBatch().length > 0;
    } finally {
      DataBaseUtil.commitClose(con, true);
    }

  }
  
  /** The Constant GET_INSCLAIMAMT_FROM_SALESCLAIMDETAILS. */
  private static final String GET_INSCLAIMAMT_FROM_SALESCLAIMDETAILS = "SELECT "
      + " SUM(scd.insurance_claim_amt) "
      + " FROM store_sales_main ssm "
      + " JOIN store_sales_details ssd ON(ssd.sale_id = ssm.sale_id) "
      + " JOIN sales_claim_details scd ON(scd.sale_item_id = ssd.sale_item_id) "
      + " WHERE ssm.charge_id = ?  AND scd.claim_id = ? ";

  /**
   * Gets the insurance claim amt from sales claim details.
   *
   * @param chargeId the charge id
   * @param claimId  the claim id
   * @return the insurance claim amt from sales claim details
   * @throws SQLException the SQL exception
   */
  private BigDecimal getInsuranceClaimAmtFromSalesClaimDetails(String chargeId, String claimId)
      throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_INSCLAIMAMT_FROM_SALESCLAIMDETAILS);
      ps.setString(1, chargeId);
      ps.setString(2, claimId);
      return DataBaseUtil.getBigDecimalValueFromDb(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant GET_INSCLAIMTAXAMT_FROM_SALESCLAIMDETAILS. */
  private static final String GET_INSCLAIMTAXAMT_FROM_SALESCLAIMDETAILS = "SELECT SUM(scd.tax_amt) "
      + " FROM store_sales_main ssm "
      + " JOIN store_sales_details ssd ON(ssd.sale_id = ssm.sale_id) "
      + " JOIN sales_claim_details scd ON(scd.sale_item_id = ssd.sale_item_id) "
      + " WHERE ssm.charge_id = ?  AND scd.claim_id = ? ";

  /**
   * Gets the insurance claim tax amt from sales claim details.
   *
   * @param chargeId the charge id
   * @param claimId  the claim id
   * @return the insurance claim tax amt from sales claim details
   * @throws SQLException the SQL exception
   */
  private BigDecimal getInsuranceClaimTaxAmtFromSalesClaimDetails(String chargeId, String claimId)
      throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_INSCLAIMTAXAMT_FROM_SALESCLAIMDETAILS);
      ps.setString(1, chargeId);
      ps.setString(2, claimId);
      return DataBaseUtil.getBigDecimalValueFromDb(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant GET_SALE_BILL_CHARGES. */
  private static final String GET_SALE_BILL_CHARGES = "SELECT bc.* " + " FROM store_sales_main ssm "
      + " JOIN bill_charge_claim bc ON(bc.charge_id = ssm.charge_id) "
      + " JOIN bill b ON(b.bill_no = ssm.bill_no) "
      + " WHERE ssm.type='S' AND b.visit_id = ? AND b.status = 'A' ";

  /**
   * Gets the sale bill charges.
   *
   * @param patientId the patient id
   * @return the sale bill charges
   * @throws SQLException the SQL exception
   */
  private List<BasicDynaBean> getSaleBillCharges(String patientId) throws SQLException {

    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_SALE_BILL_CHARGES);
      ps.setString(1, patientId);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant SET_ISSUE_RETURN_INS_CLAIM_AMT_TO_ZERO. */
  private static final String SET_ISSUE_RETURN_INS_CLAIM_AMT_TO_ZERO = " UPDATE "
      + " bill_charge_claim bcc "
      + " set insurance_claim_amt = 0 " + " FROM bill_charge bc "
      + " JOIN bill b ON(b.bill_no = bc.bill_no) "
      + " WHERE bcc.charge_id = bc.charge_id AND bcc.charge_head in('INVRET') "
      + " AND b.visit_id=? AND b.status='A'";

  /**
   * Sets the issue returns claim amount TO zero.
   *
   * @param patientId the new issue returns claim amount TO zero
   * @throws SQLException the SQL exception
   */
  public void setIssueReturnsClaimAmountTOZero(String patientId) throws SQLException {
    Connection con = null;
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      try (PreparedStatement ps = con.prepareStatement(SET_ISSUE_RETURN_INS_CLAIM_AMT_TO_ZERO)) {
        ps.setString(1, patientId);
        ps.executeUpdate();
      }
    } finally {
      DataBaseUtil.commitClose(con, true);
    }
  }

  /** The Constant GET_SALES_TAX_DETAILS. */
  private static final String GET_SALES_TAX_DETAILS = " SELECT "
      + " ssm.charge_id, sstd.item_subgroup_id AS tax_sub_group_id,"
      + " MAX(sstd.tax_rate) AS tax_rate, "
      + " sum(sstd.tax_amt) AS tax_amount, sum(sstd.original_tax_amt) AS original_tax_amt "
      + " FROM bill b " + " JOIN store_sales_main ssm ON (ssm.bill_no = b.bill_no) "
      + " JOIN bill_charge bc ON (bc.charge_id = ssm.charge_id AND bc.bill_no = ssm.bill_no) "
      + " JOIN store_sales_details ssd ON (ssd.sale_id = ssm.sale_id) "
      + " JOIN store_sales_tax_details sstd ON (sstd.sale_item_id = ssd.sale_item_id) "
      + " WHERE b.visit_id = ? ";
  
  private static final String CHECK_BILL_STATUS = "AND b.status = 'A' ";
  
  private static final String SALE_TAX_GROUP_BY = " GROUP BY ssm.charge_id,ssm.sale_id,"
      + "sstd.item_subgroup_id,ssm.type ";
  
  /** The Constant GET_SALES_CLAIM_TAX_DETAILS. */
  private static final String GET_SALES_CLAIM_TAX_DETAILS = " SELECT "
      + " ssm.charge_id,sctd.claim_id,sctd.item_subgroup_id AS tax_sub_group_id, "
      + " MAX(sctd.tax_rate) AS tax_rate, sum(sctd.tax_amt) "
      + " AS sponsor_tax_amount, 0 AS charge_tax_id " + " FROM bill b  "
      + " JOIN store_sales_main ssm ON (ssm.bill_no = b.bill_no) "
      + " JOIN bill_charge bc ON (bc.charge_id = ssm.charge_id AND bc.bill_no = b.bill_no) "
      + " JOIN store_sales_details ssd ON (ssd.sale_id = ssm.sale_id)  "
      + " JOIN sales_claim_details scd ON (scd.sale_item_id = ssd.sale_item_id) "
      + " JOIN sales_claim_tax_details sctd ON (sctd.sale_item_id = scd.sale_item_id "
      + " AND sctd.claim_id = scd.claim_id) WHERE b.visit_id = ? ";

  private static final String SALE_CLAIM_TAX_GROUP_BY = " GROUP BY ssm.charge_id,ssm.sale_id,"
      + "sctd.claim_id,sctd.item_subgroup_id,ssm.type ";

  /**
   * Insert or update bill charge taxes for sales.
   *
   * @param patientId the patient id
   * @throws SQLException the SQL exception
   * @throws IOException  Signals that an I/O exception has occurred.
   */
  public void insertOrUpdateBillChargeTaxesForSales(String patientId)
        throws SQLException, IOException {
    insertOrUpdateBillChargeTaxesForSales(patientId, true);
  }
  
  /**
   * Insert or update bill charge taxes for sales.
   *
   * @param patientId the patient id
   * @param checkBillStatus the patient id
   * @throws SQLException the SQL exception
   * @throws IOException  Signals that an I/O exception has occurred.
   */
  public void insertOrUpdateBillChargeTaxesForSales(String patientId, boolean checkBillStatus)
      throws SQLException, IOException {
    Connection con = null;
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
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      
      StringBuilder salesTaxQuery = new StringBuilder();
      salesTaxQuery.append(GET_SALES_TAX_DETAILS);
      if (checkBillStatus) {
        salesTaxQuery.append(CHECK_BILL_STATUS);
      }
      salesTaxQuery.append(SALE_TAX_GROUP_BY);
      ps = con.prepareStatement(salesTaxQuery.toString()); 
      
      ps.setString(1, patientId);
      salesTaxList = DataBaseUtil.queryToDynaList(ps);
      
      StringBuilder salesClaimTaxQuery = new StringBuilder();
      salesClaimTaxQuery.append(GET_SALES_CLAIM_TAX_DETAILS);
      if (checkBillStatus) {
        salesClaimTaxQuery.append(CHECK_BILL_STATUS);
      }
      salesClaimTaxQuery.append(SALE_CLAIM_TAX_GROUP_BY);
      ps = con.prepareStatement(salesClaimTaxQuery.toString());
      ps.setString(1, patientId);
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

      if (!billChargeTaxesToInsert.isEmpty()) {
        success &= bcTaxDao.insertAll(con, billChargeTaxesToInsert);
      }

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
        // List<BasicDynaBean> bcTaxList = bcTaxDao.getBillChargeTax(chargeId, taxSubGroupId);
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

      if (!billChargeClaimTaxesToInsert.isEmpty()) {
        success &= bcclTaxDao.insertAll(con, billChargeClaimTaxesToInsert);
      }

      for (BasicDynaBean bean : billChargeclaimTaxesToUpdate) {
        success &= bcclTaxDao.updateWithNames(con, bean.getMap(),
            new String[] { "charge_id", "claim_id", "tax_sub_group_id" }) > 0;
      }
    } finally {
      DataBaseUtil.closeConnections(null, ps);
      DataBaseUtil.commitClose(con, success);
    }
  }

  /** The Constant SALE_ITEM_CLAIM_AGG_TAX_DETAILS. */
  public static final String SALE_ITEM_CLAIM_AGG_TAX_DETAILS = "SELECT "
      + " sctd.sale_item_id,sctd.item_subgroup_id,"
      + " COALESCE(SUM(sctd.tax_amt),0) tax_amt FROM bill b "
      + " JOIN store_sales_main ssm ON (ssm.bill_no = b.bill_no) " + " JOIN bill_charge bc ON "
      + " (bc.charge_id = ssm.charge_id AND bc.bill_no = b.bill_no) "
      + " JOIN store_sales_details ssd ON (ssd.sale_id = ssm.sale_id) "
      + " JOIN sales_claim_details scd ON (scd.sale_item_id = ssd.sale_item_id) "
      + " JOIN sales_claim_tax_details sctd ON (sctd.sale_item_id = scd.sale_item_id "
      + " AND sctd.claim_id = scd.claim_id) "
      + " WHERE b.visit_id = ? AND b.status = 'A' AND sctd.adj_amt = 'Y' AND ssm.type='S' "
      + " GROUP BY sctd.sale_item_id,item_subgroup_id";

  /** The Constant SALE_ITEM_TAX_AGG_DETAILS. */
  public static final String SALE_ITEM_TAX_AGG_DETAILS = "SELECT "
      + " sstd.sale_item_id, COALESCE(SUM(sstd.tax_amt),0) tax FROM bill b "
      + " JOIN store_sales_main ssm ON (ssm.bill_no = b.bill_no) "
      + " JOIN bill_charge bc ON (bc.charge_id = ssm.charge_id AND bc.bill_no = b.bill_no) "
      + " JOIN store_sales_details ssd ON (ssd.sale_id = ssm.sale_id) "
      + " JOIN store_sales_tax_details sstd ON (sstd.sale_item_id = ssd.sale_item_id) "
      + " WHERE b.visit_id = ? AND b.status = 'A' AND ssm.type='S' "
      + " GROUP BY sstd.sale_item_id";

  /** The Constant SALE_ITEM_TAX_DETAILS. */
  private static final String SALE_ITEM_TAX_DETAILS = " SELECT "
      + " ssm.charge_id, sstd.item_subgroup_id AS tax_sub_group_id,"
      + " MAX(sstd.tax_rate) AS tax_rate, "
      + " sum(sstd.tax_amt) AS tax_amount, sum(sstd.original_tax_amt) AS original_tax_amt "
      + " FROM bill b " + " JOIN store_sales_main ssm ON (ssm.bill_no = b.bill_no) "
      + " JOIN bill_charge bc ON (bc.charge_id = ssm.charge_id AND bc.bill_no = ssm.bill_no) "
      + " JOIN store_sales_details ssd ON (ssd.sale_id = ssm.sale_id) "
      + " JOIN store_sales_tax_details sstd ON (sstd.sale_item_id = ssd.sale_item_id) "
      + " WHERE b.visit_id = ? AND b.status = 'A' AND ssm.type='S' "
      + " GROUP BY ssm.charge_id,ssm.sale_id,sstd.item_subgroup_id";

  /** The update sales tax details. */
  private static String UPDATE_SALES_TAX_DETAILS = "UPDATE store_sales_details "
      + " SET amount = (amount - tax) + ?, tax = ? WHERE sale_item_id = ?";

  /**
   * This method is used to update store_sales_tax_details AND bill_charge_tax tables based on
   * adj_amt flag. Because on Connect and Disconnect insurance sponsor tax amounts are calculated
   * but not updated in sales tables as per KSA rules.
   *
   * @param patientId the patient id
   * @throws SQLException the SQL exception
   * @throws IOException  Signals that an I/O exception has occurred.
   */
  public void updateTaxDetails(String patientId) throws SQLException, IOException {
    Connection con = null;
    PreparedStatement ps = null;
    boolean success = true;
    List<BasicDynaBean> salesClaimAggTaxList = new ArrayList<BasicDynaBean>();
    List<BasicDynaBean> salesItemAggTaxList = new ArrayList<BasicDynaBean>();
    List<BasicDynaBean> salesItemTaxList = new ArrayList<BasicDynaBean>();
    GenericDAO saleTaxDAO = new GenericDAO("store_sales_tax_details");
    GenericDAO billChargeTaxDAO = new GenericDAO("bill_charge_tax");

    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);

      ps = con.prepareStatement(SALE_ITEM_CLAIM_AGG_TAX_DETAILS);
      ps.setString(1, patientId);
      salesClaimAggTaxList = DataBaseUtil.queryToDynaList(ps);
      if (salesClaimAggTaxList != null && salesClaimAggTaxList.size() > 0) {
        // Update store_sales_tax_details with updated sales_claim_tax_details.
        Iterator<BasicDynaBean> salesClaimTaxListIterator = salesClaimAggTaxList.iterator();
        while (salesClaimTaxListIterator.hasNext()) {
          BasicDynaBean taxClaimBean = salesClaimTaxListIterator.next();
          success &= saleTaxDAO.updateWithNames(con, taxClaimBean.getMap(),
              new String[] { "sale_item_id", "item_subgroup_id" }) > 0;
        }
      }

      // Update store_sales_details tax amount bcz tax split is updated in
      // store_sales_tax_details.
      ps = con.prepareStatement(SALE_ITEM_TAX_AGG_DETAILS);
      ps.setString(1, patientId);
      salesItemAggTaxList = DataBaseUtil.queryToDynaList(ps);
      if (salesItemAggTaxList != null && salesItemAggTaxList.size() > 0) {
        Iterator<BasicDynaBean> salesItemTaxListIterator = salesItemAggTaxList.iterator();
        while (salesItemTaxListIterator.hasNext()) {
          BasicDynaBean taxItemBean = salesItemTaxListIterator.next();
          success &= DataBaseUtil.executeQuery(con, UPDATE_SALES_TAX_DETAILS,
              new Object[] { taxItemBean.get("tax"), taxItemBean.get("tax"),
                  taxItemBean.get("sale_item_id") }) > 0;
        }
      }

      // Update bill_charge_tax tax amount with latest updated columns in
      // store_sales_tax_details.
      ps = con.prepareStatement(SALE_ITEM_TAX_DETAILS);
      ps.setString(1, patientId);
      salesItemTaxList = DataBaseUtil.queryToDynaList(ps);
      if (salesItemTaxList != null && salesItemTaxList.size() > 0) {
        Iterator<BasicDynaBean> salesItemTaxListIterator = salesItemTaxList.iterator();
        while (salesItemTaxListIterator.hasNext()) {
          BasicDynaBean taxItemBean = salesItemTaxListIterator.next();
          success &= billChargeTaxDAO.updateWithNames(con, taxItemBean.getMap(),
              new String[] { "charge_id", "tax_sub_group_id" }) > 0;
        }
      }

    } finally {
      DataBaseUtil.closeConnections(null, ps);
      DataBaseUtil.commitClose(con, success);
    }
  }

  /** The Constant UPDATE_SALES_CLAIM_DETAILS. */
  public static final String UPDATE_SALES_CLAIM_DETAILS = "UPDATE "
      + " sales_claim_details scd SET tax_amt=0,insurance_claim_amt=0,"
      + " ref_insurance_claim_amount=0,org_insurance_claim_amount=0"
      + " FROM  store_sales_details ssd "
      + " JOIN store_sales_main ssm ON(ssm.sale_id = ssd.sale_id) "
      + " where bill_no=? AND (ssd.sale_item_id = scd.sale_item_id) ";

  /**
   * Update sales claim details.
   *
   * @param billNo the bill no
   * @throws SQLException the SQL exception
   * @throws IOException  Signals that an I/O exception has occurred.
   */
  public void updateSalesClaimDetails(String billNo) throws SQLException, IOException {
    Connection con = null;
    PreparedStatement ps = null;
    boolean success = true;
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      ps = con.prepareStatement(UPDATE_SALES_CLAIM_DETAILS);
      ps.setString(1, billNo);
      success &= ps.executeUpdate() > 0;
    } finally {
      DataBaseUtil.closeConnections(null, ps);
      DataBaseUtil.commitClose(con, success);
    }
  }

  /** The Constant UPDATE_SALES_CLAIM_TAX_DETAILS. */
  public static final String UPDATE_SALES_CLAIM_TAX_DETAILS = "UPDATE "
      + " sales_claim_tax_details scd SET tax_amt=0"
      + " FROM  store_sales_details ssd "
      + " JOIN store_sales_main ssm ON(ssm.sale_id = ssd.sale_id) "
      + " where bill_no=? AND (ssd.sale_item_id = scd.sale_item_id) ";

  /**
   * Update sales claim tax details.
   *
   * @param billNo the bill no
   * @throws SQLException the SQL exception
   * @throws IOException  Signals that an I/O exception has occurred.
   */
  public void updateSalesClaimTaxDetails(String billNo) throws SQLException, IOException {
    Connection con = null;
    PreparedStatement ps = null;
    boolean success = true;
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      ps = con.prepareStatement(UPDATE_SALES_CLAIM_TAX_DETAILS);
      ps.setString(1, billNo);
      success &= ps.executeUpdate() > 0;
    } finally {
      DataBaseUtil.closeConnections(null, ps);
      DataBaseUtil.commitClose(con, success);
    }
  }
  
  private static final String UNLOCK_PACKAGE_CHARGE_ITEMS = "UPDATE bill_charge "
      + " SET is_claim_locked = false "
      + " WHERE submission_batch_type in ('I','P') AND charge_head = 'PKGPKG' AND bill_no = ? ";

  public Boolean unlockPackageChargeItem(String billNo) {
    return DatabaseHelper.update(UNLOCK_PACKAGE_CHARGE_ITEMS, new Object[]{billNo}) >= 0;
  }
}
