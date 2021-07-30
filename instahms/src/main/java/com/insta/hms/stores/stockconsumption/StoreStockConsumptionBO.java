package com.insta.hms.stores.stockconsumption;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.integration.scm.inventory.ScmOutBoundInvService;
import com.insta.hms.modules.ModulesDAO;
import com.insta.hms.stores.StockFIFODAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author mithun.saha
 *
 */

import javax.servlet.http.HttpServletRequest;

/**
 * The Class StoreStockConsumptionBO.
 */
public class StoreStockConsumptionBO {

  /** The maindao. */
  private static final GenericDAO generalReagentUsageMainDAO =
      new GenericDAO("general_reagent_usage_main");

  /** The detailsdao. */
  private static final GenericDAO generalReagentUsageDetailsDAO =
      new GenericDAO("general_reagent_usage_details");

  /** The modules dao. */
  private static ModulesDAO modules = new ModulesDAO();

  /** The scm outbound inventory service. */
  private static ScmOutBoundInvService scmOutService = ApplicationContextProvider
      .getBean(ScmOutBoundInvService.class);

  /**
   * The Class StockConsumptionDetails.
   */
  public static class StockConsumptionDetails {

    /** The consumption detials id. */
    public int consumptionDetialsId;

    /** The consumption id. */
    public String consumptionId;

    /** The item batchid. */
    public int itemBatchid;

    /** The stock qty. */
    public BigDecimal stockQty;

    /** The qty. */
    public BigDecimal qty;

    /**
     * Gets the consumption detials id.
     *
     * @return the consumption detials id
     */
    public int getConsumptionDetialsId() {
      return consumptionDetialsId;
    }

    /**
     * Sets the consumption detials id.
     *
     * @param consumptionDetialsId the new consumption detials id
     */
    public void setConsumptionDetialsId(int consumptionDetialsId) {
      this.consumptionDetialsId = consumptionDetialsId;
    }

    /**
     * Gets the consumption id.
     *
     * @return the consumption id
     */
    public String getConsumptionId() {
      return consumptionId;
    }

    /**
     * Sets the consumption id.
     *
     * @param consumptionId the new consumption id
     */
    public void setConsumptionId(String consumptionId) {
      this.consumptionId = consumptionId;
    }

    /**
     * Gets the item batchid.
     *
     * @return the item batchid
     */
    public int getItemBatchid() {
      return itemBatchid;
    }

    /**
     * Sets the item batchid.
     *
     * @param itemBatchid the new item batchid
     */
    public void setItemBatchid(int itemBatchid) {
      this.itemBatchid = itemBatchid;
    }

    /**
     * Gets the qty.
     *
     * @return the qty
     */
    public BigDecimal getQty() {
      return qty;
    }

    /**
     * Sets the qty.
     *
     * @param qty the new qty
     */
    public void setQty(BigDecimal qty) {
      this.qty = qty;
    }

    /**
     * Gets the stock qty.
     *
     * @return the stock qty
     */
    public BigDecimal getStockQty() {
      return stockQty;
    }

    /**
     * Sets the stock qty.
     *
     * @param stockQty the new stock qty
     */
    public void setStockQty(BigDecimal stockQty) {
      this.stockQty = stockQty;
    }
  }

  /**
   * Update stock consumption details.
   *
   * @param con the con
   * @param req the req
   * @return true, if successful
   * @throws SQLException the SQL exception
   * @throws Exception    the exception
   */
  public static boolean updateStockConsumptionDetails(Connection con, HttpServletRequest req)
      throws SQLException, Exception {
    String[] recordUpdate = req.getParameterValues("_c_is_update");
    String[] consumedQty = req.getParameterValues("_c_consumed_qty");
    String userName = (String) req.getSession(false).getAttribute("userid");
    String reqConSumptionId = req.getParameter("_consumption_id");
    String[] reqConSumptionDetId = req.getParameterValues("_reagent_detail_id");
    reqConSumptionId = (reqConSumptionId == null || reqConSumptionId.isEmpty()) ? null
        : reqConSumptionId;
    BasicDynaBean updateMainBean = generalReagentUsageMainDAO.getBean();
    BasicDynaBean updateDetBean = generalReagentUsageDetailsDAO.getBean();
    boolean success = false;
    try {
      if (reqConSumptionId != null) {
        Map<String, String> keys = new HashMap<String, String>();
        String key = reqConSumptionId;
        keys.put("consumption_id", key);
        updateMainBean.set("user_name", userName);
        success = generalReagentUsageMainDAO.update(con, updateMainBean.getMap(), keys) > 0;
      }

      if (recordUpdate != null) {
        for (int i = 0; i < recordUpdate.length; i++) {
          if (recordUpdate[i] != null && reqConSumptionDetId[i] != null
              && !reqConSumptionDetId[i].isEmpty()) {
            int key = Integer.parseInt(reqConSumptionDetId[i]);
            if (recordUpdate[i].equals("Y")) {
              Map<String, Integer> keys = new HashMap<String, Integer>();
              keys.put("reagent_detail_id", key);
              updateDetBean.set("qty", new BigDecimal(consumedQty[i]));
              success = generalReagentUsageDetailsDAO.update(con, updateDetBean.getMap(), keys) > 0;
            } else {
              success = generalReagentUsageDetailsDAO.delete(con, "reagent_detail_id", key);
            }
          }
        }
      }
    } finally {
      DataBaseUtil.closeConnections(null, null);
    }
    return success;
  }

  /**
   * Finalize stock consumption details.
   *
   * @param con the con
   * @param req the req
   * @return the string
   * @throws SQLException the SQL exception
   * @throws Exception    the exception
   */
  public static String finalizeStockConsumptionDetails(Connection con, HttpServletRequest req,
      List<Map<String, Object>> cacheConsumptionTxns) throws SQLException, Exception {
    GenericDAO reagentmaindao = new GenericDAO("store_reagent_usage_main");
    GenericDAO reagentdetailsdao = new GenericDAO("store_reagent_usage_details");
    GenericDAO storeItemBatchDetailsDAO = new GenericDAO("store_item_batch_details");
    StoreStockConsumptionDAO dao = new StoreStockConsumptionDAO();
    List<BasicDynaBean> stockConusmedList = new ArrayList<BasicDynaBean>();
    String deptId = req.getParameter("dept_id");
    String userName = (String) req.getSession(false).getAttribute("userid");
    BasicDynaBean reagentUsageMainBean = null;
    BasicDynaBean reagentUsageDetBean = null;
    String consumptionId = req.getParameter("_consumption_id");
    String msgBatchNo = null;
    String msgItemName = null;
    BigDecimal msgQty = BigDecimal.ZERO;
    String msg = null;
    boolean isStockAvailable = true;
    boolean status = false;
    Map statusMap = null;

    try {
      String storeName = DataBaseUtil
          .getStringValueFromDb("SELECT dept_name from stores WHERE dept_id =?",
           Integer.parseInt(deptId));
      stockConusmedList = generalReagentUsageDetailsDAO.findAllByKey(con,
          "consumption_id", consumptionId);
      for (BasicDynaBean bean : stockConusmedList) {
        BigDecimal stockQty = (BigDecimal) bean.get("stock_qty");
        BigDecimal consumedQty = (BigDecimal) bean.get("qty");
        int itemBatchId = (Integer) bean.get("item_batch_id");
        BigDecimal currStockQty = BigDecimal.ZERO;
        currStockQty = (BigDecimal) dao
            .getStockItemsCurrentQty(itemBatchId, Integer.parseInt(deptId)).get("qty");
        if (currStockQty.compareTo(stockQty) == -1) {
          BasicDynaBean stBean = storeItemBatchDetailsDAO.findByKey(con,
              "item_batch_id", itemBatchId);
          msgBatchNo = (String) stBean.get("batch_no");
          msgItemName = DataBaseUtil.getStringValueFromDb(
              "SELECT medicine_name from store_item_details WHERE medicine_id = ?",
                  (Integer) stBean.get("medicine_id"));
          isStockAvailable = false;
          msgQty = stockQty;
          msg = "currently " + storeName + " has less stock than that consumed for item:"
              + msgItemName + " And Batch No:" + msgBatchNo;
          break;
        }
      }

      if (!isStockAvailable) {
        return msg;
      }

      if (isStockAvailable) {
        reagentUsageMainBean = reagentmaindao.getBean();
        reagentUsageMainBean.set("store_id", Integer.parseInt(deptId));
        reagentUsageMainBean.set("date_time", DataBaseUtil.getDateandTime());
        reagentUsageMainBean.set("consumer_id", null);
        reagentUsageMainBean.set("user_name", userName);
        reagentUsageMainBean.set("ref_no", null);
        int reagentSeq = reagentmaindao.getNextSequence();
        reagentUsageMainBean.set("reagent_usage_seq", reagentSeq);
        reagentUsageMainBean.set("reagent_type", "G");
        status = reagentmaindao.insert(con, reagentUsageMainBean);

        for (BasicDynaBean bean : stockConusmedList) {
          int reagentUsageDetailsId = reagentdetailsdao.getNextSequence();
          reagentUsageDetBean = reagentdetailsdao.getBean();
          reagentUsageDetBean.set("reagent_usage_seq", reagentSeq);
          reagentUsageDetBean.set("reagent_usage_det_id", reagentUsageDetailsId);
          reagentUsageDetBean.set("ref_no", null);
          reagentUsageDetBean.set("item_batch_id", (Integer) bean.get("item_batch_id"));
          reagentUsageDetBean.set("qty", (BigDecimal) bean.get("qty"));

          if (status) {
            statusMap = new StockFIFODAO().reduceStock(con, Integer.parseInt(deptId),
                (Integer) bean.get("item_batch_id"), "GC", (BigDecimal) bean.get("qty"), null,
                userName, "General Consumable Usage",
                (Integer) reagentUsageDetBean.get("reagent_usage_det_id"));
          }

          status = (Boolean) statusMap.get("status");
          BigDecimal costValue = (BigDecimal) statusMap.get("costValue");
          reagentUsageDetBean.set("cost_value", costValue);

          if (!status) {
            break;
          }

          status = reagentdetailsdao.insert(con, reagentUsageDetBean);
          BasicDynaBean module = modules.findByKey("module_id", "mod_scm");
          if (module != null && ((String) module.get("activation_status")).equals("Y")) {
            cacheConsumeTxns(cacheConsumptionTxns, reagentUsageMainBean, reagentUsageDetBean,
                consumptionId);
          }
        }
      }
    } finally {
      DataBaseUtil.closeConnections(null, null);
    }

    if (!status) {
      msg = "failed to insert reagent usage details";
      return msg;
    }

    return null;
  }

  private static void cacheConsumeTxns(List<Map<String, Object>> cacheConsumptionTxns,
      BasicDynaBean main, BasicDynaBean details, String consumptionId) {
    Map<String, Object> data = scmOutService.getStockConsumeMap(main, details, consumptionId);
    if (!data.isEmpty()) {
      cacheConsumptionTxns.add(data);
    }
  }

}
