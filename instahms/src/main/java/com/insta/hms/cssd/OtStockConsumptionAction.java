package com.insta.hms.cssd;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.stores.StockFIFODAO;
import com.insta.hms.stores.StockUserIssueDAO;
import com.insta.hms.stores.StockUserReturnDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class OtStockConsumptionAction extends BaseAction {

  /**
   * List.
   *
   * @param am
   *          the am
   * @param af
   *          the af
   * @param req
   *          the req
   * @param res
   *          the res
   * @return the action forward
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws ServletException
   *           the servlet exception
   * @throws Exception
   *           the exception
   */
  @IgnoreConfidentialFilters
  public ActionForward list(ActionMapping am, ActionForm af, HttpServletRequest req,
      HttpServletResponse res) throws IOException, ServletException, Exception {

    Map params = getParameterMap(req);

    DateUtil dateUtil = new DateUtil();
    String fromDate = dateUtil.getDateFormatter().format(new java.util.Date());
    String toDate = dateUtil.getDateFormatter().format(new java.util.Date());
    String startTime = req.getParameter("date");
    startTime = startTime == null ? "" : startTime;
    params.remove("date");
    if (startTime.equals("") || !startTime.equals("today")) {
      String[] appointDate = req.getParameterValues("appointment_date");
      params.put("appointment_date@op", new String[] { "ge", "le" });
      if (appointDate != null) {
        req.setAttribute("fromdate", appointDate[0]);
        req.setAttribute("todate", appointDate[1]);
      }
    }

    OtStockConsumptionDao dao = new OtStockConsumptionDao();

    req.setAttribute("pagedList",
        dao.getKitIssuedOperations(params, ConversionUtils.getListingParameter(params)));

    return am.findForward("list");
  }

  /**
   * Show.
   *
   * @param am
   *          the am
   * @param af
   *          the af
   * @param req
   *          the req
   * @param res
   *          the res
   * @return the action forward
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws ServletException
   *           the servlet exception
   * @throws Exception
   *           the exception
   */
  @IgnoreConfidentialFilters
  public ActionForward show(ActionMapping am, ActionForm af, HttpServletRequest req,
      HttpServletResponse res) throws IOException, ServletException, Exception {

    OtStockConsumptionDao dao = new OtStockConsumptionDao();

    String appointmentId = getParameter(req, "appointment_id");
    String kitId = getParameter(req, "kit_id");
    req.setAttribute("op", dao.getKitIssuedOpDetails(Integer.parseInt(appointmentId)));
    req.setAttribute("kititems",
        dao.getKitItemIssueDetails(Integer.parseInt(appointmentId), Integer.parseInt(kitId)));
    BasicDynaBean nonSterileStore = new GenericDAO("stores").findByKey("is_sterile_store", "N");
    if (nonSterileStore == null) {
      req.setAttribute("error", "At least one Non-Sterile store is needed for stock consumption.");
    }

    return am.findForward("show");

  }

  /**
   * Consume.
   *
   * @param am
   *          the am
   * @param af
   *          the af
   * @param req
   *          the req
   * @param res
   *          the res
   * @return the action forward
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws ServletException
   *           the servlet exception
   * @throws Exception
   *           the exception
   */
  public ActionForward consume(ActionMapping am, ActionForm af, HttpServletRequest req,
      HttpServletResponse res) throws IOException, ServletException, Exception {

    GenericDAO usageMain = new GenericDAO("store_reagent_usage_main");
    GenericDAO usageDet = new GenericDAO("store_reagent_usage_details");
    GenericDAO transferMain = new GenericDAO("store_transfer_main");
    GenericDAO transferDet = new GenericDAO("store_transfer_details");
    GenericDAO stockDetDao = new GenericDAO("store_stock_details");
    StockFIFODAO stockFifoDao = new StockFIFODAO();
    BasicDynaBean prefs = GenericPreferencesDAO.getAllPrefs();

    Map itemsmap = req.getParameterMap();
    String[] consumedQty = (String[]) itemsmap.get("qty_consumed");
    String[] returnedQty = (String[]) itemsmap.get("qty_returned");
    String[] medicineId = (String[]) itemsmap.get("medicine_id");
    String[] itemBatchId = (String[]) itemsmap.get("item_batch_id");
    String[] itemUnit = (String[]) itemsmap.get("item_unit");
    String[] trnPkSize = (String[]) itemsmap.get("trn_pkg_size");

    String nonsterileStore = getParameter(itemsmap, "non_sterile_store");
    String otStore = getParameter(itemsmap, "ot_store");
    String user = (String) req.getSession(false).getAttribute("userid");
    String kitIssueTransferNo = getParameter(itemsmap, "transfer_no");
    String userName = (String) req.getSession(false).getAttribute("userid");

    BasicDynaBean reagMainBean = usageMain.getBean();
    BasicDynaBean tranferMainBean = transferMain.getBean();
    BasicDynaBean tranferDetBean = transferDet.getBean();
    String consumptonId = null;
    int transferNo = 0;
    int tranDetNo = 0;
    boolean success = true;

    Connection con = null;

    try {

      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      for (int i = 0; i < itemBatchId.length; i++) {

        // storing consumed qty in general_reagent_usage_*** tables

        int reagentSeq = 0;
        if (Integer.parseInt(consumedQty[i]) != 0) {

          if (reagentSeq == 0) {
            reagentSeq = usageMain.getNextSequence();
            reagMainBean.set("store_id", Integer.parseInt(otStore));
            reagMainBean.set("date_time", DataBaseUtil.getDateandTime());
            reagMainBean.set("consumer_id", null);
            reagMainBean.set("user_name", userName);
            reagMainBean.set("ref_no", null);
            reagMainBean.set("reagent_usage_seq", reagentSeq);
            reagMainBean.set("reagent_type", "G");

            success = usageMain.insert(con, reagMainBean);
          }

          BasicDynaBean reagDetBean = usageDet.getBean();
          int reagentUsageDetailsId = usageDet.getNextSequence();
          reagDetBean.set("reagent_usage_seq", reagentSeq);
          reagDetBean.set("reagent_usage_det_id", reagentUsageDetailsId);
          reagDetBean.set("ref_no", null);
          reagDetBean.set("item_batch_id", Integer.parseInt(itemBatchId[i]));
          reagDetBean.set("qty", new BigDecimal(consumedQty[i]));

          success &= usageDet.insert(con, reagDetBean);
        }

        // transfering returning quantity from OT store to non sterile store
        if (Integer.parseInt(returnedQty[i]) != 0) {
          if (transferNo == 0) {
            transferNo = Integer
                .parseInt((new StockUserIssueDAO(con).getSequenceId("stock_transfer_seq")));
            tranferMainBean.set("transfer_no", transferNo);
            tranferMainBean.set("date_time", DateUtil.getCurrentTimestamp());
            tranferMainBean.set("store_from", Integer.parseInt(otStore));
            tranferMainBean.set("store_to", Integer.parseInt(nonsterileStore));
            tranferMainBean.set("reason", "CSSD returns");
            tranferMainBean.set("username", req.getSession(false).getAttribute("userid"));

            success &= transferMain.insert(con, tranferMainBean);
          }

          tranferDetBean = transferDet.getBean();

          tranferDetBean.set("transfer_no", transferNo);
          tranferDetBean.set("medicine_id", Integer.parseInt(medicineId[i]));
          tranferDetBean.set("qty", new BigDecimal(returnedQty[i]));
          tranferDetBean.set("item_unit", itemUnit[i]);
          tranferDetBean.set("trn_pkg_size", new BigDecimal(trnPkSize[i]));
          tranferDetBean.set("item_batch_id", Integer.parseInt(itemBatchId[i]));
          tranDetNo = transferDet.getNextSequence();
          tranferDetBean.set("transfer_detail_no", tranDetNo);

          HashMap<String, Object> stockKeys = new HashMap<String, Object>();
          stockKeys.put("item_batch_id", Integer.parseInt(itemBatchId[i]));
          stockKeys.put("dept_id", Integer.parseInt(nonsterileStore));
          // this is actual stock of that item batch,store,lot
          BasicDynaBean stockBean = stockDetDao.findByKey(con, new ArrayList<String>(), stockKeys);
          Map statusMap = null;

          // FIFO
          if (stockBean != null) {

            // reducing stock
            statusMap = stockFifoDao.reduceStock(con, Integer.parseInt(otStore),
                Integer.parseInt(itemBatchId[i]), "T", new BigDecimal(returnedQty[i]), null,
                (String) req.getSession(false).getAttribute("userid"), "StockTransfer", tranDetNo);
            success &= (Boolean) statusMap.get("status");

            // add stock
            statusMap = stockFifoDao.addStock(con, Integer.parseInt(nonsterileStore), tranDetNo,
                "T", new BigDecimal(returnedQty[i]),
                (String) req.getSession(false).getAttribute("userid"), "StockTransfer",
                Integer.parseInt(otStore));

            success = (Boolean) statusMap.get("status");
            tranferDetBean.set("cost_value", statusMap.get("costValue"));

            if (success && ((String) (prefs.get("show_central_excise_duty"))).equals("Y")) {
              success = new StockUserReturnDAO(con).updateStock(Integer.parseInt(nonsterileStore),
                  Integer.parseInt(otStore), itemBatchId[i], Integer.parseInt(medicineId[i]));
            }
          } else {

            // transfer to new store
            statusMap = stockFifoDao.transferStock(con, Integer.parseInt(medicineId[i]),
                Integer.parseInt(itemBatchId[i]), Integer.parseInt(otStore),
                Integer.parseInt(nonsterileStore), new BigDecimal(returnedQty[i]), user, tranDetNo,
                "N");

            // set cost value of reduces stock
            tranferDetBean.set("cost_value", statusMap.get("costValue"));

            success = (Boolean) statusMap.get("status");

          }

          // insert into transfer details
          success &= transferDet.insert(con, tranferDetBean);

        }

        // updating original transfer of kit issued operation to returned state

        Map keys = new HashMap<String, String>();
        keys.put("return_status", "Y");
        keys.put("transfer_no", Integer.parseInt(kitIssueTransferNo));

        success &= transferMain.updateWithName(con, keys, "transfer_no") > 0;

      }

      ActionRedirect redirect = new ActionRedirect("OTStockConsumption.do?_method=list");
      redirect.addParameter("date", "today");
      return redirect;

    } finally {
      DataBaseUtil.commitClose(con, success);

      // update stock timestamp
      stockFifoDao.updateStockTimeStamp();
      stockFifoDao.updateStoresStockTimeStamp(Integer.parseInt(otStore));
    }

  }
}
