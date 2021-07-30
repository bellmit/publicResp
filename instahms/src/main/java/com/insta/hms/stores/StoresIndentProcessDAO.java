package com.insta.hms.stores;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.integration.scm.inventory.ScmOutBoundInvService;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.modules.ModulesDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class StoresIndentProcessDAO {

  Connection con = null;
  public StoresIndentProcessDAO() {
  }

  public StoresIndentProcessDAO(Connection con) {
    this.con = con;
  }

  private static ScmOutBoundInvService scmOutService = ApplicationContextProvider
      .getBean(ScmOutBoundInvService.class);
  private static ModulesDAO modules = new ModulesDAO();
  private static GenericDAO storeTransferMainDao = new GenericDAO("store_transfer_main");
  private static GenericDAO storeTransferDetailsDao = new GenericDAO("store_transfer_details");
  private static final GenericDAO storeIndentDetailsDAO = new GenericDAO("store_indent_details");
  private static final GenericDAO storeIndentMainDAO = new GenericDAO("store_indent_main");
  private static final GenericDAO stockIssueMainDAO = new GenericDAO("stock_issue_main");
  private static final GenericDAO stockIssueDetailsDAO = new GenericDAO("stock_issue_details");

  public static boolean updateIndentStatus(Connection con, int indent_no, String userid,
      String remarks, String status, String issueType) throws SQLException, IOException {
    boolean success = false;
    int mresult = 0;
    Map<String, Object> fields = new HashMap<String, Object>();
    Map<String, Object> keys = new HashMap<String, Object>();

    java.sql.Timestamp mod_time = DataBaseUtil.getDateandTime();
    if (status.equals("X")) {
      fields.put("closure_reasons", remarks);
      fields.put("status", status);
      fields.put("cancelled_date", mod_time);
      fields.put("cancelled_by", userid);
    } else {
      fields.put("remarks", remarks);
    }
    fields.put("updated_date", DataBaseUtil.getDateandTime());
    keys.put("indent_no", indent_no);
    try {
      mresult = storeIndentMainDAO.update(con, fields, keys);
      if (mresult > 0)
        success = true;
    } finally {
    }
    return success;
  }

  // called by StoresIndentProcessAction when a Indent is processed
  public static int stockTransfer(Connection con, BasicDynaBean indentBean,
      List<Map<String, Object>> itemList, String remarks, String userid, String receiveprocess,
      ArrayList<String> failedItems, List<Map<String, Object>> cacheTransferTxns
      ) throws SQLException, IOException {

    boolean success = false;
    int stocktsfrid = 0, newTransferDetId = 0;
    Map fields = null;
    Map keys = null;
    BasicDynaBean stock_transfer, store_transfer_main = null;
    Map map = null;
    List<Map<String, Object>> list = null;
    Map<Object, Object> itemmap = null;
    int indent_no = (Integer) indentBean.get("indent_no");

    try {
      stocktsfrid = Integer
          .parseInt(new StockUserIssueDAO(con).getSequenceId("stock_transfer_seq"));
      store_transfer_main = storeTransferMainDao.getBean();
      store_transfer_main.set("transfer_no", stocktsfrid);
      store_transfer_main.set("date_time", DataBaseUtil.getDateandTime());
      store_transfer_main.set("store_from",
          Integer.parseInt(indentBean.get("indent_store").toString()));
      store_transfer_main.set("store_to", Integer.parseInt(indentBean.get("dept_from").toString()));
      store_transfer_main.set("reason", remarks);
      store_transfer_main.set("username", userid);
      store_transfer_main.set("indent_no", indent_no);
      success = storeTransferMainDao.insert(con, store_transfer_main);
      BasicDynaBean module = modules.findByKey("module_id", "mod_scm");

      if (success) {
        String receiveProccTranfPref = GenericPreferencesDAO.getGenericPreferences()
            .getRecTransIndent();

        for (int k = 0; k < itemList.size(); k++) {
          String item_id = null;
          BigDecimal qtyplus = new BigDecimal(0);
          map = (HashMap<String, Object>) itemList.get(k);
          list = (List) map.get("ITEM_LIST");
          item_id = (String) map.get("MEDICINE_ID");
          String itemName = (String) map.get("MEDICINE_NAME");

          // ** Following is based on generic preferences since for transferred stock, we also have
          // a receive process in place *//*
          if ((receiveprocess == null) || (receiveprocess.trim().equals(""))) {
            receiveprocess = "N";
          }

          for (int j = 0; j < list.size(); j++) {
            stock_transfer = storeTransferDetailsDao.getBean();
            stock_transfer.set("transfer_no", stocktsfrid);
            stock_transfer.set("medicine_id", Integer.parseInt(map.get("MEDICINE_ID").toString()));
            itemmap = (HashMap) list.get(j);
            stock_transfer.set("item_batch_id",
                Integer.parseInt((String) itemmap.get("IDENTIFIER")));
            stock_transfer.set("qty", new BigDecimal(
                ((String) itemmap.get("QTY")).equals("") ? "0" : (String) itemmap.get("QTY")));
            stock_transfer.set("indent_no", indent_no);
            stock_transfer.set("processed_date", DataBaseUtil.getDateandTime());
            stock_transfer.set("processed_by", userid);
            // This fix for stock movement report ,it was earler shows the transit qty ,cost and mrp
            // in report
            // Now we are inserting received qty as qty for Receive Process for Stock Transfer
            // Indent pref = NO
            if (receiveProccTranfPref.equals("N")) {
              stock_transfer.set("qty_recd", new BigDecimal(
                  ((String) itemmap.get("QTY")).equals("") ? "0" : (String) itemmap.get("QTY")));
            }

            newTransferDetId = storeTransferDetailsDao.getNextSequence();
            stock_transfer.set("transfer_detail_no", newTransferDetId);

            // qtyplus = qtyplus.add(new BigDecimal((String) itemmap.get("QTY")));

            if (((String) itemmap.get("QTY")).trim().equals("")) {
              itemmap.put("QTY", "0");
            }

            StockFIFODAO stockFIFODAO = new StockFIFODAO();
            Map statusMap = null;
            BigDecimal transferredQty = new BigDecimal((String) itemmap.get("QTY"));

            if (success) {
              // This gets the last lot for the item + item batch
              BasicDynaBean stock = new StockEntryDAO().getStock(con,
                  Integer.parseInt(indentBean.get("dept_from").toString()),
                  Integer.parseInt(map.get("MEDICINE_ID").toString()),
                  Integer.parseInt((String) itemmap.get("IDENTIFIER")));
              if (stock != null) {

                // reducing stock
                statusMap = stockFIFODAO.reduceStock(con, (Integer) indentBean.get("indent_store"),
                    Integer.parseInt((String) itemmap.get("IDENTIFIER")), "T",
                    new BigDecimal(itemmap.get("QTY").toString()), null, userid, "StockTransfer",
                    newTransferDetId);
                success &= (Boolean) statusMap.get("status");
                if (!(Boolean) statusMap.get("status") && statusMap.get("statusReason") != null) {
                  transferredQty = transferredQty.subtract((BigDecimal) statusMap.get("left_qty"));
                  failedItems.add((String) statusMap.get("statusReason") + " for item <b>"
                      + itemName + "</b>, Actual quantity is " + itemmap.get("QTY").toString()
                      + "  Processed Qty is " + transferredQty.toString());
                  success = true;// let next item transfer
                }

                if (receiveprocess.equalsIgnoreCase("N")) {

                  // add stock
                  statusMap = stockFIFODAO.addStock(con,
                      Integer.parseInt(indentBean.get("dept_from").toString()), newTransferDetId,
                      "T", new BigDecimal(itemmap.get("QTY").toString()), userid, "StockTransfer",
                      (Integer) indentBean.get("indent_store"));

                  // cost value of transfer
                  stock_transfer.set("cost_value", statusMap.get("costValue"));

                  success &= (Boolean) statusMap.get("status");
									// Scm export of Transfer
                  if (success && module != null
                      && ((String) module.get("activation_status")).equals("Y")) {
                    cacheStockTransfer(store_transfer_main, stock_transfer, cacheTransferTxns);
                  }
                } else {
                  // update the qty_in_transit column to indicate that this stock needs further
                  // processing
                  statusMap = stockFIFODAO.addTransitStock(con,
                      Integer.parseInt(indentBean.get("dept_from").toString()), newTransferDetId,
                      "T", new BigDecimal(itemmap.get("QTY").toString()), userid, "StockTransfer",
                      (Integer) indentBean.get("indent_store"));

                  stock_transfer.set("cost_value", statusMap.get("costValue"));
                }

              } else {

                // transfer to new store. This always inserts a record into store_stock_details for
                // the toStore.
                statusMap = stockFIFODAO.transferStock(con,
                    Integer.parseInt(map.get("MEDICINE_ID").toString()),
                    Integer.parseInt((String) itemmap.get("IDENTIFIER")),
                    (Integer) indentBean.get("indent_store"),
                    Integer.parseInt((String) indentBean.get("dept_from")),
                    new BigDecimal((String) itemmap.get("QTY")), userid, newTransferDetId,
                    receiveprocess);

                // cost value of transfer
                stock_transfer.set("cost_value", statusMap.get("costValue"));
                success &= (Boolean) statusMap.get("status");

                if (receiveprocess.equalsIgnoreCase("N") && success && module != null
                    && ((String) module.get("activation_status")).equals("Y")) {
                  cacheStockTransfer(store_transfer_main, stock_transfer, cacheTransferTxns);
                }

                if (!(Boolean) statusMap.get("status") && statusMap.get("statusReason") != null) {
                  transferredQty = transferredQty.subtract((BigDecimal) statusMap.get("left_qty"));
                  failedItems.add((String) statusMap.get("statusReason") + " for item <b>"
                      + itemName + "</b>, Actual quantity is " + itemmap.get("QTY").toString()
                      + "  Processed Qty is " + transferredQty.toString());
                  success = true;// let next item transfer
                }

              }

            }

            stock_transfer.set("qty", transferredQty);;
            success &= storeTransferDetailsDao.insert(con, stock_transfer);

            qtyplus = qtyplus.add(new BigDecimal((String) itemmap.get("QTY")));
          }

          if (success) {
            // veracode
            Object[] values = new Object[2];
            values[0] = indent_no;
            values[1] = Integer.parseInt(item_id);

            BasicDynaBean ibean = (BasicDynaBean) (DataBaseUtil
                .queryToDynaList("SELECT qty,qty_fullfilled from "
                    + " store_indent_details WHERE indent_no = ? and medicine_id= ? ", values))
                        .get(0);
            fields = new HashMap();
            keys = new HashMap();

            BigDecimal qty = (BigDecimal) ibean.get("qty");
            BigDecimal qty_fullfilled = ((BigDecimal) ibean.get("qty_fullfilled")).add(qtyplus);

            if (receiveprocess.equalsIgnoreCase("N")) {
              if (qty.floatValue() == qty_fullfilled.floatValue()) {
                fields.put("status", "C"); // Closed
              } else if (qty.floatValue() < qty_fullfilled.floatValue()) { // abnormal case..
                success = false;
              }
            } else {
              if (qty.floatValue() == qty_fullfilled.floatValue()) {
                fields.put("status", "T"); // Transferred
              } else if (qty.floatValue() < qty_fullfilled.floatValue()) { // abnormal case..
                success = false;
              }
            }

            fields.put("qty_fullfilled", qty_fullfilled);

            keys.put("indent_no", indent_no);
            keys.put("medicine_id", Integer.parseInt(item_id));
            int indentresult = 0;
            if (success)
              indentresult = storeIndentDetailsDAO.update(con, fields, keys);

            if (indentresult > 0) {
              success = true;
            } else {
              success = false;
              break;
            }
          }

        }
      } else {
        success = false;
      }
      if (!success)
        stocktsfrid = 0;
    } finally {
      if (!success)
        stocktsfrid = 0;
    }
    return stocktsfrid;
  }

  /**
   * Called by StoreIndentReceiveAction when a Indent is received
   * 
   * @param con
   * @param indentBean
   * @param itemList
   * @param remarks
   * @param userid
   * @param failedItems
   * @param cacheTransferTxns
   * @return
   * @throws SQLException
   * @throws IOException
   */
  public static boolean updateStockTransfer(Connection con, BasicDynaBean indentBean, List itemList,
      String remarks, String userid, ArrayList<String> failedItems,
      Map<String, String> indentStatusInfoMap, List<Map<String, Object>> cacheTransferTxns
      ) throws SQLException, IOException {

    StockUserReturnDAO surDao = new StockUserReturnDAO(con);

    int indent_no = (Integer) indentBean.get("indent_no");
    boolean status = true;
    BasicDynaBean existingDetailsBean = null;
    BasicDynaBean module = modules.findByKey("module_id", "mod_scm");
    List<BasicDynaBean> existingDetailsBeans = StoresIndentDAO.getIndentItemDetailsForReceipt(
        indent_no, Integer.parseInt((String) indentBean.get("dept_from")));;
    Map<Integer, BasicDynaBean> existingDetailsBeansMap = ConversionUtils
        .listBeanToMapBean(existingDetailsBeans, "medicine_id");

    for (int k = 0; k < itemList.size(); k++) {

      String item_id = null;
      BigDecimal fulfilledQty = BigDecimal.ZERO;
      Map map = (HashMap) itemList.get(k);
      List list = (List) map.get("ITEM_LIST");
      item_id = (String) map.get("MEDICINE_ID");
      String itemName = (String) map.get("MEDICINE_NAME");
      int medId = Integer.parseInt(item_id);
      int recvStore = Integer.parseInt((String) map.get("RECV_STORE"));
      int indentStore = Integer.parseInt((String) map.get("INDENT_STORE"));
      Map<String, Object> keys = new HashMap<String, Object>();

      BigDecimal indentQty = (BigDecimal) map.get("INDENT_QTY");
      keys.put("indent_no", indent_no);
      keys.put("medicine_id", Integer.parseInt(item_id));

      // skip items which are already received
      existingDetailsBean = existingDetailsBeansMap.get(Integer.parseInt(item_id));
      BigDecimal qtyRecandrej = ((BigDecimal) (existingDetailsBean.get("qty_recd")))
          .add((BigDecimal) (existingDetailsBean.get("qty_rej")));

      // qty received + qty rejected should not exceed qty fullfilled
      if ((qtyRecandrej).compareTo((BigDecimal) existingDetailsBean.get("qty_fullfilled")) >= 0) {
        indentStatusInfoMap.put("indent_status",
            "Indent no " + indent_no + " is already received.");
        continue;
      }

      for (int j = 0; j < list.size(); j++) {
        Map tfrItem = (HashMap) list.get(j);

        if (tfrItem != null) {
          String itemBatchId = (String) tfrItem.get("IDENTIFIER");
          BigDecimal recdQty = (BigDecimal) tfrItem.get("RECD_QTY");
          BigDecimal rejQty = (BigDecimal) tfrItem.get("REJ_QTY");
          BigDecimal transferredQty = (BigDecimal) tfrItem.get("RECD_QTY");

          Map<String, Object> keysXfer = new HashMap<String, Object>();
          Map<String, Object> fieldsXfer = new HashMap<String, Object>();
          Map recStatusMap = null;
          Map rejStatusMap = null;
          StockFIFODAO stockFIFODAO = new StockFIFODAO();
          StoreTransactionLotDAO transLotDAO = new StoreTransactionLotDAO();

          /**
           * FIFO ll try to get lots of recorded transactions in store_transaction_lot_details, But
           * records can exists for transaction after 9.0 only. Old transactions should be treated
           * differently.
           **/
          Map<String, Object> transLotKeys = new HashMap<String, Object>();
          transLotKeys.put(StoreTransactionLotDAO.FILTER_COLUMN_1,
              Integer.parseInt((String) tfrItem.get("TRANSFER_DETAIL_NO")));
          transLotKeys.put(StoreTransactionLotDAO.FILTER_COLUMN_2,
              StoreTransactionLotDAO.STOCK_TRANSFER);

          BasicDynaBean transactionLotBean = transLotDAO.findByKey(transLotKeys);

          if (transactionLotBean == null) {// This is an old transaction which is not been recorded
                                           // in transaction lot table
            if (recdQty.compareTo(BigDecimal.ZERO) > 0) {
              // add stock
              recStatusMap = stockFIFODAO.receiveLots(con, recvStore,
                  Integer.parseInt((String) tfrItem.get("TRANSFER_DETAIL_NO")), "T", recdQty,
                  userid, "StockTransfer", indentStore, Integer.parseInt(itemBatchId));
              status &= (Boolean) recStatusMap.get("status");
              if (!status && recStatusMap.get("statusReason") != null) {
                transferredQty = transferredQty.subtract((BigDecimal) recStatusMap.get("left_qty"));
                failedItems.add((String) recStatusMap.get("statusReason") + " for item <b>"
                    + itemName + "</b>, Actual quantity is " + recdQty + "  Received Qty is "
                    + transferredQty.toString());
              }
              surDao.updateTransitStock(medId, Integer.parseInt(itemBatchId), recdQty, recvStore,
                  userid, "Transfer");
              fieldsXfer.put("qty_recd", transferredQty);
            }

            /* If any quantity is rejected, update the transit qty for the transfering store also */
            if (rejQty.compareTo(BigDecimal.ZERO) > 0) {

              // add rejected stock back to indent store
              rejStatusMap = stockFIFODAO.receiveLots(con, indentStore,
                  Integer.parseInt((String) tfrItem.get("TRANSFER_DETAIL_NO")), "T", rejQty, userid,
                  "StockTransfer", null, Integer.parseInt(itemBatchId));
              status &= (Boolean) rejStatusMap.get("status");
              if (!status && rejStatusMap.get("statusReason") != null) {
                rejQty = rejQty.subtract((BigDecimal) rejStatusMap.get("left_qty"));
                failedItems.add((String) rejStatusMap.get("statusReason") + " for item <b>"
                    + itemName + "</b>, Actual quantity is " + tfrItem.get("REJ_QTY").toString()
                    + "  Rejected Qty is " + rejQty.toString());
              }
              // reduce transit stock for rejected qty in received store
              surDao.updateTransitStock(medId, Integer.parseInt(itemBatchId), rejQty, recvStore,
                  userid, "Transfer");
              fieldsXfer.put("qty_rejected", rejQty);
            }

          } else {

            if (recdQty.compareTo(BigDecimal.ZERO) > 0) {
              // add stock
              recStatusMap = stockFIFODAO.receiveTransactionLots(con, recvStore,
                  Integer.parseInt((String) tfrItem.get("TRANSFER_DETAIL_NO")), "T", recdQty,
                  userid, "StockTransfer", indentStore);
              status &= (Boolean) recStatusMap.get("status");
              if (!status && recStatusMap.get("statusReason") != null) {
                recdQty = recdQty.subtract((BigDecimal) recStatusMap.get("left_qty"));
                failedItems.add((String) recStatusMap.get("statusReason") + " for item <b>"
                    + itemName + "</b>, Actual quantity is " + tfrItem.get("RECD_QTY").toString()
                    + "  Received Qty is " + recdQty.toString());
              }

              // reduceTransitQuantity(con, surDao,
              // Integer.parseInt((String)tfrItem.get("TRANSFER_DETAIL_NO")), recdQty, recvStore,
              // medId, itemBatchId, userid);
              fieldsXfer.put("qty_recd", recdQty);
            }

            /*
             * If any quantity is rejected, update the transit qty for the transferring store also
             * and update the qty_in_rejection column with rejected qty .
             */
            if (rejQty.compareTo(BigDecimal.ZERO) > 0) {

              // add rejected stock back to indent store
              rejStatusMap = stockFIFODAO.rejectTransactionLots(con, indentStore,
                  Integer.parseInt((String) tfrItem.get("TRANSFER_DETAIL_NO")), "T", rejQty, userid,
                  "StockTransfer", recvStore);

              if (!(Boolean) rejStatusMap.get("status")
                  && rejStatusMap.get("statusReason") != null) {
                rejQty = rejQty.subtract((BigDecimal) rejStatusMap.get("left_qty"));
                failedItems.add((String) rejStatusMap.get("statusReason") + " for item <b>"
                    + itemName + "</b>, Actual quantity is " + tfrItem.get("REJ_QTY").toString()
                    + "  Rejected Qty is " + rejQty.toString());
              }
              // reduce transit stock for rejected qty in received store
              // surDao.updateTransitStock(medId, Integer.parseInt(itemBatchId), rejQty, recvStore,
              // userid, "Transfer");
              // reduceTransitQuantity(con, surDao,
              // Integer.parseInt((String)tfrItem.get("TRANSFER_DETAIL_NO")), rejQty, recvStore,
              // medId, itemBatchId, userid);
              fieldsXfer.put("qty_rejected", rejQty);
              if (rejQty.compareTo(BigDecimal.ZERO) > 0) {
                fieldsXfer.put("is_rejected_qty_taken", "N");
              }

            }

          }

          fieldsXfer.put("received_cost_value",
              recStatusMap != null ? recStatusMap.get("costValue") : BigDecimal.ZERO);
          fieldsXfer.put("received_date", DataBaseUtil.getDateandTime());
          fieldsXfer.put("received_by", userid);

          keysXfer.put("medicine_id", medId);
          keysXfer.put("indent_no", indent_no);
          keysXfer.put("item_batch_id", Integer.parseInt(itemBatchId));
          keysXfer.put("transfer_detail_no",
              Integer.parseInt((String) tfrItem.get("TRANSFER_DETAIL_NO")));

          storeTransferDetailsDao.update(con, fieldsXfer, keysXfer);
          fulfilledQty = fulfilledQty.add(recdQty).add(rejQty);
          if (recdQty.compareTo(BigDecimal.ZERO) > 0 && module != null
              && ((String) module.get("activation_status")).equals("Y")) {
            Map<String, Object> mainKey = new HashMap<>();
            mainKey.put("indent_no", indent_no);
            BasicDynaBean storeTransferMainBean = storeTransferMainDao.findByKey(con, mainKey);
            BasicDynaBean storeTransferDetailBean = storeTransferDetailsDao.findByKey(con,
                keysXfer);
            /*overriding the quantity with received because getStockTransferMap() 
             method is used for process indent and receive indent csv creation, so
             for process indent we take qty column of store_transfer_detail table
             and for receive indent we take only received quantity i.e. qty_recd column
            */
            storeTransferDetailBean.set("qty", recdQty);
            cacheStockTransfer(storeTransferMainBean, storeTransferDetailBean, cacheTransferTxns);
          }
        }
      }

      Map<String, Object> fields = new HashMap<String, Object>();

      List<BasicDynaBean> transferIndentDetails =
          storeTransferDetailsDao.listAll(con, null, keys, null);
      fulfilledQty = BigDecimal.ZERO;

      for (BasicDynaBean transferDetail : transferIndentDetails) {
        fulfilledQty = fulfilledQty.add(((BigDecimal) transferDetail.get("qty_recd"))
            .add((BigDecimal) transferDetail.get("qty_rejected")));
      }

      if (fulfilledQty.compareTo(BigDecimal.ZERO) > 0) {
        // bug 31878: sometimes both req and rej qty is 0 -- assume no processing done in that case
        if (fulfilledQty.compareTo(indentQty) == 0) {
          fields.put("status", "C"); // Transfered
        }

        if (fields != null && fields.size() > 0) {
          status &= storeIndentDetailsDAO.update(con, fields, keys) > 0;
        }
      }
    }

    return status;
  }

  private static final String query = "SELECT * FROM store_stock_details ssd "
      + " JOIN store_item_batch_details sibd USING(item_batch_id)"
      + " JOIN store_item_details sib ON(sib.medicine_id = ssd.medicine_id) "
      + " WHERE dept_id=? AND ssd.medicine_id = ? AND ssd.item_batch_id = ? ";

  public static BigDecimal userIssue(Connection con, BasicDynaBean indentBean, List itemList,
      String remarks, String userid) throws SQLException, IOException {
    boolean success = true;

    BigDecimal userIssueid = null;

    Map<String, Object> fields = null;
    Map<String, Object> keys = null;

    BasicDynaBean user_issue, user_issue_main = null;
    Map map = null;
    List list = null;
    Map itemmap = null;

    String location = (String) indentBean.get("location_type");
    String fromName = null;

    PreparedStatement ps = null;

    List userissueList = new ArrayList();
    BasicDynaBean stockBean = null;
    StockFIFODAO stockFIFODAO = new StockFIFODAO();

    int indent_no = (Integer) indentBean.get("indent_no");

    try {// veracode
      if (location.equals("D")) {
        fromName = DataBaseUtil.getStringValueFromDb(
            "SELECT dept_name FROM department WHERE dept_id = ? ",
            (String) indentBean.get("dept_from"));
      } else {
        fromName = DataBaseUtil.getStringValueFromDb(
            "SELECT ward_name FROM ward_names WHERE ward_no = ? ",
            (String) indentBean.get("dept_from"));
      }

      userIssueid = new BigDecimal(
          new StockUserIssueDAO(con).getSequenceId("store_issue_sequence"));
      user_issue_main = stockIssueMainDAO.getBean();
      user_issue_main.set("user_issue_no", userIssueid);
      user_issue_main.set("date_time", DataBaseUtil.getDateandTime());
      user_issue_main.set("dept_from", Integer.parseInt(indentBean.get("indent_store").toString()));
      user_issue_main.set("user_type", "Hospital");
      user_issue_main.set("issued_to",
          (String) indentBean.get("requester_name") + "(" + fromName + ")");
      user_issue_main.set("reference", remarks);
      user_issue_main.set("username", userid);

      // check if user exists else create a new user for the issue.
      Boolean result = new StockUserIssueDAO(con)
          .checkUser((String) indentBean.get("requester_name") + "(" + fromName + ")");
      if (!result) {
        result = new StockUserIssueDAO(con)
            .saveUser((String) indentBean.get("requester_name") + "(" + fromName + ")");
      }

      for (int k = 0; k < itemList.size(); k++) {
        map = (HashMap) itemList.get(k);
        list = (List) map.get("ITEM_LIST");

        for (int j = 0; j < list.size(); j++) {

          int itemissueid = Integer
              .parseInt(new StockUserIssueDAO(con).getSequenceId("store_issue_details_sequence"));

          itemmap = (HashMap) list.get(j);

          int item_id = Integer.parseInt((String) map.get("MEDICINE_ID"));
          BigDecimal qty = new BigDecimal(
              ((String) itemmap.get("QTY")).equals("") ? "0" : (String) itemmap.get("QTY"));
          String identifier = (String) itemmap.get("IDENTIFIER");
          int store_id = Integer.parseInt(indentBean.get("indent_store").toString());

          user_issue = stockIssueDetailsDAO.getBean();
          user_issue.set("user_issue_no", userIssueid);
          user_issue.set("medicine_id", item_id);
          user_issue.set("item_batch_id", Integer.parseInt(identifier));
          user_issue.set("qty", qty);
          user_issue.set("return_qty", new BigDecimal(0));
          user_issue.set("item_issue_no", itemissueid);

          ps = con.prepareStatement(query);
          ps.setInt(1, store_id);
          ps.setInt(2, item_id);
          ps.setInt(3, Integer.parseInt(identifier));
          stockBean = (BasicDynaBean) DataBaseUtil.queryToDynaList(ps).get(0);

          user_issue.set("vat", (BigDecimal) stockBean.get("tax_rate"));
          user_issue.set("amount", (BigDecimal) stockBean.get("mrp"));
          user_issue.set("pkg_size", (BigDecimal) stockBean.get("stock_pkg_size"));
          user_issue.set("pkg_cp", (BigDecimal) stockBean.get("package_cp"));
          user_issue.set("pkg_mrp", (BigDecimal) stockBean.get("mrp"));
          user_issue.set("indent_no", indent_no);

          map = (HashMap) itemList.get(k);
          list = (List) map.get("ITEM_LIST");
          item_id = 0;
          BigDecimal qtyplus = new BigDecimal(0);
          fields = new HashMap<String, Object>();
          keys = new HashMap<String, Object>();

          itemmap = (HashMap) list.get(j);

          item_id = Integer.parseInt((String) map.get("MEDICINE_ID"));
          qty = new BigDecimal(
              ((String) itemmap.get("QTY")).equals("") ? "0" : (String) itemmap.get("QTY"));
          identifier = (String) itemmap.get("IDENTIFIER");
          BigDecimal qty_avbl = new BigDecimal((String) itemmap.get("AVBL_QTY"));

          store_id = Integer.parseInt(indentBean.get("indent_store").toString());

          // stock reduction by FIFO method
          // reducing stock
          Map statusMap = stockFIFODAO.reduceStock(con, store_id, Integer.parseInt(identifier), "U",
              qty, null, userid, "UserIssue", itemissueid);
          success &= (Boolean) statusMap.get("status");

          // set cost value of issue
          user_issue.set("cost_value", statusMap.get("costValue"));
          userissueList.add(user_issue);

          qty_avbl = qty_avbl.subtract(qty);
          qtyplus = qtyplus.add(qty);

          if (success) {

            Object[] values = new Object[2];
            values[0] = indent_no;
            values[1] = item_id;

            BasicDynaBean ibean = (BasicDynaBean) (DataBaseUtil
                .queryToDynaList("SELECT qty,qty_fullfilled from "
                    + " store_indent_details WHERE indent_no = ? and medicine_id= ? ", values))
                        .get(0);// veracode
            fields = new HashMap<String, Object>();
            keys = new HashMap<String, Object>();

            qty = (BigDecimal) ibean.get("qty");
            BigDecimal qty_fullfilled = ((BigDecimal) ibean.get("qty_fullfilled")).add(qtyplus);

            if (qty.floatValue() == qty_fullfilled.floatValue()) {
              fields.put("status", "C"); // Fullfilled and Received
            } else if (qty.floatValue() < qty_fullfilled.floatValue()) { // abnormal case..
              success = false;
              userIssueid = null;
            }
            fields.put("qty_fullfilled", qty_fullfilled);

            keys.put("indent_no", indent_no);
            keys.put("medicine_id", item_id);
            int indentresult = 0;
            if (success)
              indentresult = storeIndentDetailsDAO.update(con, fields, keys);
            if (indentresult > 0) {
              success = true;
            } else {
              success = false;
              break;
            }
            if (success) {
              con.commit();
            } else {
              con.rollback();
              userIssueid = null;
            }
          } else {
            userIssueid = null;
          }

        }
      }

      success = stockIssueMainDAO.insert(con, user_issue_main);
      if (success) {
        success = success && stockIssueDetailsDAO.insertAll(con, userissueList);
      }

    } finally {
      DataBaseUtil.closeConnections(null, ps);
    }
    return userIssueid;
  }

  public boolean updateIndentItem(Connection con, int indentNo, int medicineId, BigDecimal qtyplus)
      throws SQLException, IOException {
    boolean success;
    Object[] values = new Object[2];
    values[0] = indentNo;
    values[1] = medicineId;

    BasicDynaBean ibean = (BasicDynaBean) (DataBaseUtil.queryToDynaList("SELECT qty,qty_fullfilled from "
        + " store_indent_details WHERE indent_no = ? and medicine_id= ?", values)).get(0);// veracode
    Map fields = new HashMap();
    Map keys = new HashMap();

    BigDecimal qty = (BigDecimal) ibean.get("qty");
    BigDecimal qty_fullfilled = ((BigDecimal) ibean.get("qty_fullfilled")).add(qtyplus);

    if (qty.floatValue() <= qty_fullfilled.floatValue()) {
      fields.put("status", "F"); // Fullfilled and Received
    } else {
      fields.put("status", "P"); // Partially Fullfilled
    }
    fields.put("qty_fullfilled", qty_fullfilled);

    keys.put("indent_no", indentNo);
    keys.put("medicine_id", medicineId);

    int indentresult = storeIndentDetailsDAO.update(con, fields, keys);
    if (indentresult > 0) {
      success = true;
    } else {
      success = false;
    }
    return success;
  }

  public boolean updateIndentStatus(Connection con, int indent_no, String userName)
      throws SQLException, IOException {

    List indentList = getIndentItems(con, indent_no);
    int fullfilledCount = 0;

    Map fields = null;
    Map keys = null;
    boolean success = true;
    for (int i = 0; i < indentList.size(); i++) {
      BasicDynaBean b = (BasicDynaBean) indentList.get(i);
      if (b.get("status").equals("F") || (b.get("status").equals("R"))) {
        fullfilledCount = fullfilledCount + 1;
      }
    }
    if (fullfilledCount == indentList.size()) {
      fields = new HashMap();
      keys = new HashMap();

      fields.put("closure_reasons", "All indent item qty is fullfilled.");
      fields.put("status", "C");
      fields.put("store_user", userName);

      keys.put("indent_no", indent_no);

      int update = storeIndentMainDAO.update(con, fields, keys);

      if (update > 0) {
        success = true;
      } else {
        success = false;
      }
    }
    return success;
  }

  public static String updateIndentFullfilledStatus(int indent_no, String msg, String userid,
      String issueType, String receiveProcess) throws SQLException, IOException {

    List<BasicDynaBean> indentList = storeIndentDetailsDAO.findAllByKey("indent_no", indent_no);
    int fullfilledCount = 0;
    Map<String, Object> fields = null;
    Map<String, Object> keys = null;
    boolean success = false;
    Connection con = null;
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      for (int i = 0; i < indentList.size(); i++) {
        BasicDynaBean b = (BasicDynaBean) indentList.get(i);
        if (b.get("status").equals("T") || b.get("status").equals("C")
            || b.get("status").equals("R")) {
          fullfilledCount = fullfilledCount + 1;
        }
      }
      if (fullfilledCount == indentList.size()) {
        fields = new HashMap<String, Object>();
        keys = new HashMap<String, Object>();
        fields.put("closure_reasons", "All indent item qty is fullfilled.");
        if ((receiveProcess.equalsIgnoreCase("Y") || receiveProcess.equalsIgnoreCase("F"))
            && issueType.equals("S")) {
          fields.put("status", "P");
        } else {
          fields.put("status", "C");
        }
        keys.put("indent_no", indent_no);
        int update = storeIndentMainDAO.update(con, fields, keys);
        if (update > 0) {
          msg = msg + "Also Indent closed...";
          success = true;
          con.commit();
        } else {
          msg = msg + "Indent closure failed...";
          con.rollback();
        }
      }
    } finally {
      DataBaseUtil.commitClose(con, success);
    }
    return msg;
  }

  public static String updateIndentReceivedStatus(Connection con, int indent_no, String msg,
      String userid) throws SQLException, IOException {

    List<BasicDynaBean> indentList =
        storeIndentDetailsDAO.findAllByKey(con, "indent_no", indent_no);
    int receivedCount = 0;
    Map<String, Object> fields = null;
    Map<String, Object> keys = null;

    try {
      for (int i = 0; i < indentList.size(); i++) {
        BasicDynaBean b = (BasicDynaBean) indentList.get(i);
        if (b.get("status").equals("C")) {
          receivedCount = receivedCount + 1;
        }
      }
      fields = new HashMap<String, Object>();
      keys = new HashMap<String, Object>();
      keys.put("indent_no", indent_no);
      java.sql.Timestamp mod_time = DataBaseUtil.getDateandTime();
      fields.put("updated_date", mod_time);
      if (receivedCount == indentList.size()) {
        fields.put("status", "C");
        fields.put("store_user", userid);
        int update = storeIndentMainDAO.update(con, fields, keys);
        if (update > 0) {
          msg = msg + "Also Indent accepted...";
          con.commit();
        } else {
          msg = msg + "Indent accept failed...";
          con.rollback();
        }
      } else {
        int update = storeIndentMainDAO.update(con, fields, keys);
        if (update > 0) {
          con.commit();
        } else {
          con.rollback();
        }
      }
    } finally {
    }
    return msg;
  }

  private final static String INDENT_SERIAL_TRANSFER_QTY = "SELECT sum(qty_in_transit) as pending_qty "
      + " FROM store_stock_details sd  " + " group by medicine_id, dept_id "
      + " HAVING medicine_id = ? and dept_id = ? ";

  public static boolean updateIndentNStockTransfer(BasicDynaBean indentbean, String userid)
      throws Exception {
    boolean success = false;
    BasicDynaBean stocktransfer = null;
    BasicDynaBean indentstatusbean = null;
    Map<String, Object> fields = new HashMap<String, Object>();
    Map<String, Object> keys = new HashMap<String, Object>();
    Connection con1 = null;
    PreparedStatement ps = null;
    boolean toUpdate = false;
    try {
      con1 = DataBaseUtil.getConnection();
      con1.setAutoCommit(false);
      int indentno = ((Integer) indentbean.get("indent_no")).intValue();
      int itemid = ((Integer) indentbean.get("medicine_id")).intValue();
      int transferno = ((Integer) indentbean.get("transfer_no")).intValue();
      String batchno = (String) indentbean.get("batch_no");
      String status = (String) indentbean.get("status");
      String identifier = (String) indentbean.get("identification");
      BigDecimal rejectedQty = (BigDecimal) indentbean.get("qty_rejected");
      BigDecimal recdQty = (BigDecimal) indentbean.get("qty_recd");
      BigDecimal qty = (BigDecimal) indentbean.get("qty");
      keys.put("indent_no", indentno);
      keys.put("medicine_id", itemid);

      if ((null != identifier) && (identifier.equalsIgnoreCase("B"))) {
        /** This is a batch item. */
        if ((rejectedQty.add(recdQty)).compareTo(qty) == 0) {
          /**
           * If sum of rejected qty & recd Qty is equal to the total qty transfered, update the
           * indent status to 'S' which is stock adjusted in indenting store
           */
          fields.put("status", "S");
          toUpdate = true;
        }
      } else {
        /**
         * This is a serial item. Need different logic here since qty transfered will always be 1 or
         * 0
         */
        ps = con1.prepareStatement(INDENT_SERIAL_TRANSFER_QTY);
        ps.setInt(1, itemid);
        ps.setInt(2, (Integer) indentbean.get("store_from"));
        BigDecimal pendingQty = DataBaseUtil.getBigDecimalValueFromDb(ps);
        if (pendingQty.compareTo(BigDecimal.ZERO) == 0) {
          /**
           * Total sum of qty_in_transit pending is 1, means this is the last qty to be adjusted,
           * update the indent status
           */
          fields.put("status", "S");
          toUpdate = true;
        }
      }
      if (toUpdate) {
        int indentsuccess = storeIndentDetailsDAO.update(con1, fields, keys);
        if (indentsuccess > 0) {
          success = true;
        }
      }

    } finally {
      if(null != ps) {
        ps.close();
      }
      DataBaseUtil.commitClose(con1, success);
    }

    return success;
  }

  public static String updateIndentRevertStatus(List indentList, String msg, String userid)
      throws SQLException, IOException {
    List<BasicDynaBean> indentItemsList = new ArrayList<BasicDynaBean>();
    for (Iterator iter = indentList.iterator(); iter.hasNext();) {
      int indentno = ((Integer) (((BasicDynaBean) iter.next()).get("indent_no"))).intValue();
      if (indentno > 0) {
        indentItemsList = storeIndentDetailsDAO.findAllByKey("indent_no",
            indentno);

        int receivedCount = 0;

        Map<String, Object> fields = new HashMap<String, Object>();
        Map<String, Object> keys = new HashMap<String, Object>();
        boolean success = false;
        Connection con1 = null;
        try {
          con1 = DataBaseUtil.getConnection();
          for (Iterator iterator = indentItemsList.iterator(); iterator.hasNext();) {
            BasicDynaBean b = (BasicDynaBean) iterator.next();
            if ((b.get("status") != null) && (b.get("status").equals("S"))) {
              receivedCount = receivedCount + 1;
            }
          }

        } finally {
          DataBaseUtil.commitClose(con1, success);
        }
      }

    }
    return msg;
  }

  String indentquery = "SELECT si.* from store_indent_main sim JOIN store_indent_details si using (indent_no) where "
      + "si.medicine_id = ? and sim.dept_from = ? ";

  public boolean updateIndentRevertStatus(int itemid, String batchno, int storeid, int indentno,
      String userid, String msg) throws SQLException, IOException {
    Connection con = null;
    PreparedStatement ps = null;
    boolean success = true;
    Map<Object, Object> fields = new HashMap<Object, Object>();
    Map<Object, Object> keys = new HashMap<Object, Object>();
    try {
      con = DataBaseUtil.getConnection();
      keys.put("indent_no", indentno);
      keys.put("medicine_id", itemid);
      fields.put("status", "S");

      int rows = storeIndentDetailsDAO.update(con, fields, keys);
      if (rows >= 0) {
        List<BasicDynaBean> indentItemsList =
            storeIndentDetailsDAO.findAllByKey("indent_no", indentno);

        int receivedCount = 0;
        keys.remove("medicine_id");
        for (Iterator iterator = indentItemsList.iterator(); iterator.hasNext();) {
          BasicDynaBean b = (BasicDynaBean) iterator.next();
          if ((b.get("status") != null) && (b.get("status").equals("S"))) {
            receivedCount = receivedCount + 1;
          }
          if (receivedCount == indentItemsList.size()) {
            fields.remove("status");
            fields.put("status", "P");
            fields.put("store_user", userid);
            int update = storeIndentMainDAO.update(con, fields, keys);

            if (update > 0) {
              msg = msg + "Also Indent reverted to Partially fulfilled...";
              success = true;
              con.commit();
            } else {
              msg = msg + "Indent accept failed...";
              success = false;
              con.rollback();
            }
          }
        }
      }

    } finally {
      if (ps != null) {
        ps.close();
        con.close();

      }
    }
    return success;
  }

  private static final String GET_INDENT_LIST = "select * from store_indent_details where indent_no=?";

  public List<BasicDynaBean> getIndentItems(Connection con, int indentNo) throws SQLException {
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(GET_INDENT_LIST);
      ps.setInt(1, indentNo);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(null, ps);
    }
  }

  private static void cacheStockTransfer(BasicDynaBean transferMain, BasicDynaBean transferDetails,
      List<Map<String, Object>> cacheTransferTxns) {
    Map<String, Object> data = scmOutService.getStockTransferMap(transferMain, transferDetails);
    if (!data.isEmpty()) {
      cacheTransferTxns.add(data);
    }
  }
}
