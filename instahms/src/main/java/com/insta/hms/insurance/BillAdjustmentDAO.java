package com.insta.hms.insurance;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.billing.ChargeDAO;
import com.insta.hms.billing.ChargeDTO;
import com.insta.hms.common.GenericDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * The Class BillAdjustmentDAO.
 */
public class BillAdjustmentDAO {

  /** The bill claim DAO. */
  GenericDAO billClaimDAO = new GenericDAO("bill_claim");

  /** The bcc DAO. */
  GenericDAO bccDAO = new GenericDAO("bill_charge_claim");

  /** The charge DAO. */
  GenericDAO chargeDAO = new GenericDAO("bill_charge");

  /**
   * Post adj to bill charge.
   *
   * @param billBean the bill bean
   * @param adjAmt   the adj amt
   * @throws SQLException the SQL exception
   * @throws IOException  Signals that an I/O exception has occurred.
   */
  public void postAdjToBillCharge(BasicDynaBean billBean, BigDecimal adjAmt)
      throws SQLException, IOException {
    if (!adjChargeExists(billBean)) {
      insertAdjToBillCharge(billBean, adjAmt);
    } else {
      updateBillChargeAdj(billBean, adjAmt);
    }
  }

  /**
   * Adj charge exists.
   *
   * @param billBean the bill bean
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  private boolean adjChargeExists(BasicDynaBean billBean) throws SQLException {

    boolean adjChargeExists = false;
    String billNo = (String) billBean.get("bill_no");

    Map<String, Object> keys = new HashMap<String, Object>();
    keys.put("bill_no", billNo);
    keys.put("charge_head", "SPNADJ");
    BasicDynaBean chgBean = chargeDAO.findByKey(keys);

    adjChargeExists = null != chgBean;

    return adjChargeExists;
  }

  /**
   * Insert adj to bill charge.
   *
   * @param billBean the bill bean
   * @param adjAmt   the adj amt
   * @throws SQLException the SQL exception
   */
  private void insertAdjToBillCharge(BasicDynaBean billBean, BigDecimal adjAmt)
      throws SQLException {
    Connection con = null;
    boolean success = true;
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);

      ChargeDAO chgDAO = new ChargeDAO(con);
      ChargeDTO chargeDTO = new ChargeDTO();
      String chargeId = chgDAO.getNextChargeId();
      chargeDTO.setBillNo((String) billBean.get("bill_no"));
      chargeDTO.setChargeId(chargeId);
      chargeDTO.setChargeGroup("ADJ");
      chargeDTO.setChargeHead("SPNADJ");
      chargeDTO.setAmount(BigDecimal.ZERO);
      chargeDTO.setInsuranceClaimAmount(adjAmt.negate());
      chargeDTO.setActQuantity(BigDecimal.ZERO);
      chargeDTO.setStatus("A");
      chargeDTO.setInsuranceBill(true);

      chgDAO.insertCharge(chargeDTO);

    } finally {
      DataBaseUtil.commitClose(con, success);
    }
  }

  /**
   * Update bill charge adj.
   *
   * @param billBean the bill bean
   * @param adjAmt   the adj amt
   * @throws SQLException the SQL exception
   * @throws IOException  Signals that an I/O exception has occurred.
   */
  private void updateBillChargeAdj(BasicDynaBean billBean, BigDecimal adjAmt)
      throws SQLException, IOException {
    String billNo = (String) billBean.get("bill_no");
    Connection con = null;
    boolean success = true;
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      Map<String, Object> keys = new HashMap<String, Object>();
      keys.put("bill_no", billNo);
      keys.put("charge_head", "SPNADJ");
      BasicDynaBean chgBean = chargeDAO.findByKey(keys);
      chgBean.set("insurance_claim_amount", adjAmt.negate());
      chargeDAO.update(con, chgBean.getMap(), keys);
    } finally {
      DataBaseUtil.commitClose(con, success);
    }
  }

  /**
   * Post adj to bill charge claim.
   *
   * @param billBean the bill bean
   * @param adjAmt   the adj amt
   * @param priority the priority
   * @throws SQLException the SQL exception
   * @throws IOException  Signals that an I/O exception has occurred.
   */
  public void postAdjToBillChargeClaim(BasicDynaBean billBean, BigDecimal adjAmt, int priority)
      throws SQLException, IOException {
    if (!adjBillChargeClaimExists(billBean, priority)) {
      insertAdjToBillChargeClaim(billBean, adjAmt, priority);
    } else {
      updateBillChargeClaimAdj(billBean, adjAmt, priority);
    }

  }

  /**
   * Adj bill charge claim exists.
   *
   * @param billBean the bill bean
   * @param priority the priority
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  private boolean adjBillChargeClaimExists(BasicDynaBean billBean, int priority)
      throws SQLException {

    boolean adjBillChgExists = false;
    String billNo = (String) billBean.get("bill_no");

    String claimId = getClaimId(billNo, priority);

    if (null != claimId) {
      Map<String, Object> keys = new HashMap<String, Object>();
      keys.put("bill_no", billNo);
      keys.put("charge_head", "SPNADJ");
      keys.put("claim_id", claimId);
      BasicDynaBean billChargeClaimBean = bccDAO.findByKey(keys);
      adjBillChgExists = null != billChargeClaimBean;
    }

    return adjBillChgExists;
  }

  /**
   * Insert adj to bill charge claim.
   *
   * @param billBean the bill bean
   * @param adjAmt   the adj amt
   * @param priority the priority
   * @throws SQLException the SQL exception
   * @throws IOException  Signals that an I/O exception has occurred.
   */
  private void insertAdjToBillChargeClaim(BasicDynaBean billBean, BigDecimal adjAmt, int priority)
      throws SQLException, IOException {

    String billNo = (String) billBean.get("bill_no");

    Connection con = null;
    boolean success = true;
    try {

      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);

      Map<String, Object> keys = new HashMap<String, Object>();
      keys.put("bill_no", billNo);
      keys.put("charge_head", "SPNADJ");
      BasicDynaBean charge = chargeDAO.findByKey(con, keys);
      String adjCharge = (String) charge.get("charge_id");

      Map<String, Object> billClaimKeys = new HashMap<String, Object>();
      billClaimKeys.put("bill_no", billNo);
      billClaimKeys.put("priority", priority);
      BasicDynaBean billClaimBean = billClaimDAO.findByKey(billClaimKeys);

      if (null != billClaimBean) {

        BasicDynaBean billChgClaimBean = bccDAO.getBean();

        billChgClaimBean.set("charge_id", adjCharge);
        billChgClaimBean.set("claim_id", (String) billClaimBean.get("claim_id"));
        billChgClaimBean.set("bill_no", billNo);
        billChgClaimBean.set("insurance_claim_amt", adjAmt.negate());
        billChgClaimBean.set("charge_head", "SPNADJ");
        billChgClaimBean.set("sponsor_id", (String) billClaimBean.get("sponsor_id"));

        bccDAO.insert(con, billChgClaimBean);

      }

    } finally {
      DataBaseUtil.commitClose(con, success);
    }

  }

  /**
   * Update bill charge claim adj.
   *
   * @param billBean the bill bean
   * @param adjAmt   the adj amt
   * @param priority the priority
   * @throws SQLException the SQL exception
   * @throws IOException  Signals that an I/O exception has occurred.
   */
  private void updateBillChargeClaimAdj(BasicDynaBean billBean, BigDecimal adjAmt, int priority)
      throws SQLException, IOException {
    String billNo = (String) billBean.get("bill_no");
    String claimId = getClaimId(billNo, priority);

    Connection con = null;
    boolean success = true;
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);

      Map<String, Object> keys = new HashMap<String, Object>();
      keys.put("bill_no", billNo);
      keys.put("claim_id", claimId);
      keys.put("charge_head", "SPNADJ");
      BasicDynaBean billChgClaimBean = bccDAO.findByKey(keys);
      billChgClaimBean.set("insurance_claim_amt", adjAmt.negate());

      bccDAO.update(con, billChgClaimBean.getMap(), keys);

    } finally {
      DataBaseUtil.commitClose(con, success);
    }

  }

  /**
   * Gets the claim id.
   *
   * @param billNo   the bill no
   * @param priority the priority
   * @return the claim id
   * @throws SQLException the SQL exception
   */
  private String getClaimId(String billNo, int priority) throws SQLException {
    String claimId = null;
    Map<String, Object> billClaimKeys = new HashMap<String, Object>();

    billClaimKeys.put("bill_no", billNo);
    billClaimKeys.put("priority", priority);
    BasicDynaBean billClaimBean = billClaimDAO.findByKey(billClaimKeys);

    claimId = null != billClaimBean ? (String) billClaimBean.get("claim_id") : null;
    return claimId;
  }

}
