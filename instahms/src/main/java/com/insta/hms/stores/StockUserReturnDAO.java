package com.insta.hms.stores;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.billing.ChargeDTO;
import com.insta.hms.common.GenericDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

public class StockUserReturnDAO {

  Connection con = null;
  public StockUserReturnDAO(Connection con) {
    this.con = con;
  }
  private static final String update_stock_minus = "update store_stock_details set qty=qty-? ";

  private static final String update_stock_plus = "update store_stock_details set qty=qty+? ";

  private static final String update_stock_non_returnable = update_stock_minus
      + " , username=?, change_source=? where dept_id=? and medicine_id=? and item_batch_id=?";

  private static final String update_kit_stock = "update store_stock_details set qty_in_use=qty_in_use+? , qty_kit=qty_kit-?, username=?, change_source=? where item_batch_id=? and dept_id=?";

  private static final String update_stock_returnable = update_stock_minus
      + ", qty_in_use=qty_in_use+? , username=?, change_source=? where dept_id=? and medicine_id=? and item_batch_id=?";

  private static final String update_stock_returnable_minus = update_stock_plus
      + ", qty_in_use=qty_in_use-? , username=?, change_source=? where dept_id=? and medicine_id=? and item_batch_id=?";

  private static final String update_stock_transit_plus = "update store_stock_details set qty_in_transit = qty_in_transit + ?,username=?, change_source=? where dept_id=? and medicine_id=? and item_batch_id=? ";

  private static final String update_stock_transit_minus = "update store_stock_details set qty_in_transit = qty_in_transit - ?,username=?, change_source=? where dept_id=? and medicine_id=? and item_batch_id=? ";

  private static final String update_stock_transit_lot_plus = "update store_stock_details set qty_in_transit = qty_in_transit + ?,username=?, change_source=? where dept_id=? and medicine_id=? and item_batch_id=? and item_lot_id=?";

  private static final String update_stock_transit_lot_minus = "update store_stock_details set qty_in_transit = qty_in_transit - ?,username=?, change_source=? where dept_id=? and medicine_id=? and item_batch_id=? and item_lot_id=?";

  private static final String set_stock_transit = "update store_stock_details set qty_in_transit = qty_in_transit,username=?, change_source=? where dept_id=? and medicine_id=? and item_batch_id=? ";

  private static final String update_stock_maint_minus = "update store_stock_details set qty_maint = qty_maint - ?";

  private static final String update_stock_maint_plus = "update store_stock_details set qty_maint = qty_maint + ?";

  private static final String update_stock_qtyRetire_plus = "update store_stock_details set qty_retired = qty_retired + ?";

  private static final String update_qtyInUse_minus_qtymaint_plus = update_stock_maint_plus
      + ", qty_in_use=qty_in_use-? , username=?, change_source=? where dept_id=? and medicine_id=? and item_batch_id=?";

  private static final String update_qtyInUse_minus_qtyRetire_plus = update_stock_qtyRetire_plus
      + ", qty_in_use=qty_in_use-? , username=?, change_source=? where dept_id=? and medicine_id=? and item_batch_id=?";
  
  private static final GenericDAO storeItemBatchDetailsDAO =
      new GenericDAO("store_item_batch_details");
  /**
   *
   * @param item_id
   * @param item_identifier
   * @param qty
   * @param store_id
   * @param issue_type
   * @param stock_update
   *          1 for stock issue 2 for stock_user_return 3 for stock_transfer decrease from store 4
   *          for stock_transfer increase to store
   * @return
   * @throws SQLException
   */
  public boolean updateStock(int medicine_id, String batchNo, Float qty, int dept_id,
      int stock_update, String itemorkits, String username) throws SQLException {
    HashMap<String, Object> keys = new HashMap<String, Object>();
    keys.put("medicine_id", medicine_id);
    keys.put("batch_no", batchNo);
    BasicDynaBean batchDetails = storeItemBatchDetailsDAO.findByKey(keys);
    return updateStock(medicine_id, (Integer) batchDetails.get("item_batch_id"),
        BigDecimal.valueOf(qty), dept_id, stock_update, itemorkits, username);
  }

  public boolean updateStock(int medicine_id, int item_batch_id, Float qty, int dept_id,
      int stock_update, String itemorkits, String username) throws SQLException {
    return updateStock(medicine_id, item_batch_id, BigDecimal.valueOf(qty), dept_id, stock_update,
        itemorkits, username);
  }

  public boolean updateStock(int medicine_id, int item_batch_id, BigDecimal qty, int dept_id,
      int stock_update, String itemorkits, String username) throws SQLException {
    PreparedStatement ps = null;
    int result = 0;
    // check the stock has gone below the 0 value,if so stops updating the stock and returns true to
    // continue the trasaction
    try {
      switch (stock_update) {
        case 1 :
          if (itemorkits.equalsIgnoreCase("kit")) {
            ps = con.prepareStatement(update_kit_stock);
            ps.setBigDecimal(1, qty.setScale(2, BigDecimal.ROUND_HALF_UP));
            ps.setBigDecimal(2, qty.setScale(2, BigDecimal.ROUND_HALF_UP));
            ps.setString(3, username);
            ps.setString(4, "KitIssue");
            ps.setInt(5, item_batch_id);
            ps.setInt(6, dept_id);
            break;
          }
          ps = con.prepareStatement(update_stock_returnable);
          ps.setBigDecimal(1, qty.setScale(2, BigDecimal.ROUND_HALF_UP));
          ps.setBigDecimal(2, qty.setScale(2, BigDecimal.ROUND_HALF_UP));
          ps.setString(3, username);
          ps.setString(4, "UserIssue");
          ps.setInt(5, dept_id);
          ps.setInt(6, medicine_id);
          ps.setInt(7, item_batch_id);

          break;
        case 2 :
          ps = con.prepareStatement(update_stock_returnable_minus);
          ps.setBigDecimal(1, qty.setScale(2, BigDecimal.ROUND_HALF_UP));
          ps.setBigDecimal(2, qty.setScale(2, BigDecimal.ROUND_HALF_UP));
          ps.setString(3, username);
          ps.setString(4, "UserReturns");
          ps.setInt(5, dept_id);
          ps.setInt(6, medicine_id);
          ps.setInt(7, item_batch_id);
          break;
        case 3 :
          ps = con.prepareStatement(update_stock_non_returnable);
          ps.setBigDecimal(1, qty.setScale(2, BigDecimal.ROUND_HALF_UP));
          ps.setString(2, username);
          ps.setString(3, "StockTransfer");
          ps.setInt(4, dept_id);
          ps.setInt(5, medicine_id);
          ps.setInt(6, item_batch_id);
          break;
        case 4 :
          ps = con.prepareStatement(update_stock_plus
              + " , username=?, change_source=? where dept_id=? and medicine_id=? and item_batch_id=?");
          ps.setBigDecimal(1, qty.setScale(2, BigDecimal.ROUND_HALF_UP));
          ps.setString(2, username);
          ps.setString(3, "StockTransfer");
          ps.setInt(4, dept_id);
          ps.setInt(5, medicine_id);
          ps.setInt(6, item_batch_id);
          break;
        case 5 :
          ps = con.prepareStatement(update_stock_returnable);
          ps.setBigDecimal(1, qty.setScale(2, BigDecimal.ROUND_HALF_UP));
          ps.setBigDecimal(2, qty.setScale(2, BigDecimal.ROUND_HALF_UP));
          ps.setString(3, username);
          ps.setString(4, "MaintStockAdjMinus");
          ps.setInt(5, dept_id);
          ps.setInt(6, medicine_id);
          ps.setInt(7, item_batch_id);
          break;
        case 6 :
          ps = con.prepareStatement(update_stock_maint_plus
              + " , username=?, change_source=? where dept_id=? and medicine_id=? and item_batch_id=?");
          ps.setBigDecimal(1, qty.setScale(2, BigDecimal.ROUND_HALF_UP));
          ps.setString(2, username);
          ps.setString(3, "MaintStockAdjPlus");
          ps.setInt(4, dept_id);
          ps.setInt(5, medicine_id);
          ps.setInt(6, item_batch_id);
          break;
        case 7 :
          ps = con.prepareStatement(update_qtyInUse_minus_qtymaint_plus);
          ps.setBigDecimal(1, qty.setScale(2, BigDecimal.ROUND_HALF_UP));
          ps.setBigDecimal(2, qty.setScale(2, BigDecimal.ROUND_HALF_UP));
          ps.setString(3, username);
          ps.setString(4, "UserReturns");
          ps.setInt(5, dept_id);
          ps.setInt(6, medicine_id);
          ps.setInt(7, item_batch_id);
          break;
        case 8 :
          ps = con.prepareStatement(update_qtyInUse_minus_qtyRetire_plus);
          ps.setBigDecimal(1, qty.setScale(2, BigDecimal.ROUND_HALF_UP));
          ps.setBigDecimal(2, qty.setScale(2, BigDecimal.ROUND_HALF_UP));
          ps.setString(3, username);
          ps.setString(4, "UserReturns");
          ps.setInt(5, dept_id);
          ps.setInt(6, medicine_id);
          ps.setInt(7, item_batch_id);
          break;
        case 9 :
          ps = con.prepareStatement(update_stock_maint_minus
              + " , username=?, change_source=? where dept_id=? and medicine_id=? and item_batch_id=?");
          ps.setBigDecimal(1, qty.setScale(2, BigDecimal.ROUND_HALF_UP));
          ps.setString(2, username);
          ps.setString(3, "MaintStockAdjMinus");
          ps.setInt(4, dept_id);
          ps.setInt(5, medicine_id);
          ps.setInt(6, item_batch_id);
          break;
        default :
          break;
      }
      if (ps != null) {
        result = ps.executeUpdate();
      }
    } finally {
      if (ps != null)
        ps.close();
    }
    return (result != 0);
  }

  public boolean updateTransitStock(int medicineId, int itemBatchId, BigDecimal qty, int deptId,
      String username, String transaction) throws SQLException {

    boolean success = false;
    PreparedStatement ps = null;
    int result = 0;
    try {
      if (transaction.equalsIgnoreCase("Receive")) {
        ps = con.prepareStatement(update_stock_transit_plus);
      } else {
        ps = con.prepareStatement(update_stock_transit_minus);
      }
      ps.setBigDecimal(1, qty.setScale(2, BigDecimal.ROUND_HALF_UP));
      ps.setString(2, username);
      ps.setString(3, "Receive StockTransfer");
      ps.setInt(4, deptId);
      ps.setInt(5, medicineId);
      ps.setInt(6, itemBatchId);
      result = ps.executeUpdate();
      success = (result != 0);
    } finally {
      if (ps != null)
        ps.close();
    }

    return success;
  }

  public boolean updateTransitStock(int medicineId, int itemBatchId, int itemLotId, BigDecimal qty,
      int deptId, String username, String transaction) throws SQLException {

    boolean success = false;
    PreparedStatement ps = null;
    int result = 0;
    try {
      if (transaction.equalsIgnoreCase("Receive")) {
        ps = con.prepareStatement(update_stock_transit_lot_plus);
      } else {
        ps = con.prepareStatement(update_stock_transit_lot_minus);
      }
      ps.setBigDecimal(1, qty.setScale(2, BigDecimal.ROUND_HALF_UP));
      ps.setString(2, username);
      ps.setString(3, "Receive StockTransfer");
      ps.setInt(4, deptId);
      ps.setInt(5, medicineId);
      ps.setInt(6, itemBatchId);
      ps.setInt(7, itemLotId);
      result = ps.executeUpdate();
      success = (result != 0);
    } finally {
      if (ps != null)
        ps.close();
    }

    return success;
  }

  public boolean updateTransitStock(int medicineId, int itemBatchId, Float qty, int deptId,
      String username, String transaction) throws SQLException {
    return updateTransitStock(medicineId, itemBatchId, BigDecimal.valueOf(qty), deptId, username,
        transaction);
  }

  public boolean updateTransitStock(int medicineId, String batchNo, Float qty, int deptId,
      String username, String transaction) throws SQLException {
    HashMap<String, Object> keys = new HashMap<String, Object>();
    keys.put("medicine_id", medicineId);
    keys.put("batch_no", batchNo);
    BasicDynaBean batchDetails = storeItemBatchDetailsDAO.findByKey(keys);
    return updateTransitStock(medicineId, (Integer) batchDetails.get("item_batch_id"),
        BigDecimal.valueOf(qty), deptId, username, transaction);
  }

  public static HashMap getIssueAmtAndDiscount(String issueId, String medicineId, String batchno,
      boolean userReturns) throws SQLException {
    ResultSet rs = null;
    Connection con = null;
    PreparedStatement ps = null;
    try {
      HashMap<String, BigDecimal> issueWiseAmts = new HashMap<String, BigDecimal>();
      con = DataBaseUtil.getReadOnlyConnection();
      int medicineIdInt = Integer.parseInt(medicineId);
      int issueIdInt = Integer.parseInt(issueId);
      String batchNoLocal = batchno;
      String rateQuery = null;
      if (userReturns) {
        // if returns is for the user, then there will be no records found in bill_activity_charge,
        // so rate and discounts will be retrieved from the stock issue details table itself
        rateQuery = "SELECT amount, (discount/qty) as discount, discount as full_discount "
            + " FROM stock_issue_details where user_issue_no=? and medicine_id=? and "
            + " batch_no=?";
      } else {
        // due some round off problems in tally, rate and discounts will be retrieved from the
        // bill_charge table.
        // to retain the bill and tally amounts same
        rateQuery = "select bc.amount/qty as amount, (sid.discount/qty) as discount, sid.discount AS full_discount "
            + "	FROM stock_issue_details sid"
            + " 		JOIN bill_activity_charge bac ON sid.item_issue_no::varchar = bac.activity_id AND activity_code = 'PHI' AND payment_charge_head = 'INVITE' "
            + " 		JOIN bill_charge bc  On bc.charge_id = bac.charge_id "
            + " 	WHERE user_issue_no=? and medicine_id=? and batch_no=?";
      }
      ps = con.prepareStatement(rateQuery);
      ps.setInt(1, issueIdInt);
      ps.setInt(2, medicineIdInt);
      ps.setString(3, batchNoLocal);

      rs = ps.executeQuery();
      while (rs.next()) {
        issueWiseAmts.put("amt", rs.getBigDecimal("amount").setScale(4, BigDecimal.ROUND_HALF_UP));
        if (rs.getString("discount") != null)
          issueWiseAmts.put("discount",
              rs.getBigDecimal("discount").setScale(4, BigDecimal.ROUND_HALF_UP));
        else
          issueWiseAmts.put("discount", BigDecimal.ZERO);

        if (rs.getString("full_discount") != null)
          issueWiseAmts.put("full_discount",
              rs.getBigDecimal("full_discount").setScale(4, BigDecimal.ROUND_HALF_UP));
        else
          issueWiseAmts.put("full_discount", BigDecimal.ZERO);
      }
      return issueWiseAmts;
    } finally {
      DataBaseUtil.closeConnections(con, ps);
      if (rs != null) {
        rs.close();
      }
    }

  }

  private static final String UPDATE_STOCK_CED_AMT = "UPDATE store_stock_details SET item_ced_amt = ? "
      + " WHERE medicine_id = ?AND batch_no=? AND dept_id=?";

  private static final String GET_ITEM_CED_AMT = "SELECT  item_ced_amt FROM store_stock_details "
      + " WHERE dept_id=? AND medicine_id=? AND batch_no=? ";

  public boolean updateStock(int deptIdTo, int deptIdFrom, String batchNo, int medicineId)
      throws SQLException {
    PreparedStatement ps1 = null;
    PreparedStatement ps = null;
    boolean success = false;
    int result = 0;
    try {
      ps1 = con.prepareStatement(GET_ITEM_CED_AMT);
      ps1.setInt(1, deptIdFrom);
      ps1.setInt(2, medicineId);
      ps1.setString(3, batchNo);
      ps = con.prepareStatement(UPDATE_STOCK_CED_AMT);
      ps.setBigDecimal(1, DataBaseUtil.getBigDecimalValueFromDb(ps1));
      ps.setInt(2, medicineId);
      ps.setString(3, batchNo);
      ps.setInt(4, deptIdTo);
      result = ps.executeUpdate();
      success = (result != 0);
    } finally {
      if (ps != null)
        ps.close();
      if (ps1 != null) {
        ps1.close();
      }
    }

    return success;
  }

  /**
   *
   * @param chargeDTO
   *          -- Item which is being returned.
   * @param issuebean
   *          -- Issue item which was sold before return.
   * @param updateChargeList
   *          -- Sale item charges in bill to be updated.
   */

  public static void setIssueItemsForReturns(BigDecimal totReturnQty, BigDecimal totReturnAmt,
      BigDecimal totReturnNet, List<ChargeDTO> updateChargeList, BasicDynaBean issuebean) {

    if (issuebean != null) {

      if (totReturnQty.compareTo(BigDecimal.ZERO) > 0) {
        BigDecimal salenet = (BigDecimal) issuebean.get("insurance_claim_amount"); // insurance
                                                                                   // claim amount
        BigDecimal saleamt = (BigDecimal) issuebean.get("amount");
        BigDecimal saleqty = (BigDecimal) issuebean.get("act_quantity");

        BigDecimal saleReturnQty = ((BigDecimal) issuebean.get("return_qty")).negate();
        saleqty = saleqty.add(saleReturnQty);

        String charge_id = (String) issuebean.get("charge_id");

        /*
         * If the remaining sale qty for the sold item exists then calculate the sale item related
         * return qty, net & amount to be updated.
         */

        if (saleqty.compareTo(BigDecimal.ZERO) > 0) {
          if ((totReturnQty.subtract(saleqty)).compareTo(BigDecimal.ZERO) > 0) {

            ChargeDTO rChdto = new ChargeDTO();

            BigDecimal returnNet = totReturnNet.subtract(salenet);
            BigDecimal returnAmt = totReturnAmt.subtract(saleamt);
            BigDecimal returnQty = totReturnQty.subtract(saleqty);
            BigDecimal discount = ((BigDecimal) issuebean.get("discount"))
                .multiply(totReturnQty.subtract(saleqty));

            rChdto.setActDescriptionId((String) issuebean.get("act_description_id"));
            rChdto.setReturnInsuranceClaimAmt(returnNet.negate());
            rChdto.setReturnAmt(returnAmt.negate());
            rChdto.setReturnQty(returnQty.negate());
            rChdto.setChargeId(charge_id);
            rChdto.setDiscount(discount);
            rChdto.setIsClaimLocked((Boolean) issuebean.get("is_claim_locked"));

            updateChargeList.add(rChdto);

            totReturnQty = totReturnQty.subtract(saleqty);
            totReturnNet = totReturnNet.subtract(salenet);
            totReturnAmt = totReturnAmt.subtract(salenet);

          } else if ((totReturnQty.subtract(saleqty)).compareTo(BigDecimal.ZERO) <= 0) {

            ChargeDTO rChdto = new ChargeDTO();

            BigDecimal returnNet = totReturnNet;
            BigDecimal returnAmt = totReturnAmt;
            BigDecimal returnQty = totReturnQty;
            BigDecimal discount = ((BigDecimal) issuebean.get("discount")).multiply(totReturnQty);

            rChdto.setActDescriptionId((String) issuebean.get("act_description_id"));
            rChdto.setReturnInsuranceClaimAmt(returnNet.negate());
            rChdto.setReturnAmt(returnAmt.negate());
            rChdto.setReturnQty(returnQty.negate());
            rChdto.setChargeId(charge_id);
            rChdto.setDiscount(discount);
            rChdto.setIsClaimLocked((Boolean) issuebean.get("is_claim_locked"));

            updateChargeList.add(rChdto);

            totReturnQty = totReturnQty.subtract(saleqty);
            totReturnNet = totReturnNet.subtract(salenet);
            totReturnAmt = totReturnAmt.subtract(saleamt);
          }
        }
      }
    }
  }

  private static final String GET_ISSUE_RETURN_CHARGES = "SELECT bc.charge_id, "
      + "bac.act_description_id as medicine_id, b.visit_id, sird.item_batch_id, "
      + "sirm.dept_to, bc.act_quantity " + "FROM bill_charge bc "
      + "LEFT JOIN bill b ON (bc.bill_no = b.bill_no) "
      + "LEFT JOIN bill_activity_charge bac ON (bc.charge_id = bac.charge_id) "
      + "LEFT JOIN store_issue_returns_details sird ON (bac.activity_id::integer = item_return_no) "
      + "LEFT JOIN store_issue_returns_main sirm on (sird.user_return_no = sirm.user_return_no) "
      + "WHERE bc.charge_head = 'INVRET' AND b.visit_id = ? order by sirm.user_return_no ";
  public List<BasicDynaBean> getIssueReturnCharges(String visitId) throws SQLException {
    PreparedStatement statement = null;
    List<BasicDynaBean> result = null;
    try {
      statement = con.prepareStatement(GET_ISSUE_RETURN_CHARGES);
      statement.setString(1, visitId);
      result = DataBaseUtil.queryToDynaList(statement);
    } finally {
      if (statement != null) {
        statement.close();
      }
    }
    
    return result;
  }
}
