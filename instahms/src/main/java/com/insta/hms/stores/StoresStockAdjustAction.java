package com.insta.hms.stores;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.integration.scm.inventory.ScmOutBoundInvService;
import com.insta.hms.modules.ModulesDAO;
import com.insta.hms.usermanager.UserDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.http.HttpHeaders;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import flexjson.JSONSerializer;

/**
 * The Class StoresStockAdjustAction.
 */
public class StoresStockAdjustAction extends DispatchAction {

  /**  The modules dao. */
  private static ModulesDAO modules = new ModulesDAO();

  /** The scm outbound inventory service. */
  private static ScmOutBoundInvService scmOutService = ApplicationContextProvider
      .getBean(ScmOutBoundInvService.class);

  /**
   * Adds the.
   *
   * @param map the map
   * @param form the form
   * @param req the req
   * @param res the res
   * @return the action forward
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws SQLException the SQL exception
   * @throws Exception the exception
   */
  @IgnoreConfidentialFilters
  public ActionForward add(ActionMapping map, ActionForm form, HttpServletRequest req,
      HttpServletResponse res) throws IOException, SQLException, Exception {

    List<BasicDynaBean> stockadjustreasonList = null;
    stockadjustreasonList = new UserDAO().getStockAdjustReason();
    req.setAttribute("stockadjustreasonList",
        new JSONSerializer().serialize(ConversionUtils.listBeanToListMap(stockadjustreasonList)));

    String transaction = req.getParameter("transaction");
    List stockDetailsToAdjust = null;

    HttpSession session = req.getSession(false);
    String deptId = (String) session.getAttribute("pharmacyStoreId");
    if (deptId != null && !deptId.equals("")) {
      BasicDynaBean dept = new GenericDAO("stores").findByKey("dept_id", Integer.parseInt(deptId));
      String deptName = dept.get("dept_name").toString();
      req.setAttribute("dept_id", deptId);
      req.setAttribute("dept_name", deptName);
    }
    if (deptId != null && deptId.equals("")) {
      req.setAttribute("dept_id", deptId);
    }

    if (transaction != null) {

      stockDetailsToAdjust = new StoresStockAdjustDAO().getItemsFromStore(
          req.getParameter("storeid"), req.getParameter("itemid"), req.getParameter("identifier"));

    }
    JSONSerializer js = new JSONSerializer().exclude("class");
    req.setAttribute("stockDetailsToAdjust", js.serialize(stockDetailsToAdjust));
    req.setAttribute("master_timestamp", PharmacymasterDAO.getItemMasterTimestamp());

    return map.findForward("addshow");
  }

  /**
   * Gets the categories.
   *
   * @param map the map
   * @param form the form
   * @param req the req
   * @param res the res
   * @return the categories
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws SQLException the SQL exception
   * @throws Exception the exception
   */
  @IgnoreConfidentialFilters
  public ActionForward getCategories(ActionMapping map, ActionForm form, HttpServletRequest req,
      HttpServletResponse res) throws IOException, SQLException, Exception {
    JSONSerializer js = new JSONSerializer().exclude("calss");
    List clist = new StoresStockAdjustDAO().getCategoriesFromStore(req.getParameter("storeid"));
    res.setContentType("text/plain");
    res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    res.getWriter().write(js.serialize(clist));
    res.flushBuffer();
    return null;
  }

  /**
   * Gets the items.
   *
   * @param map the map
   * @param form the form
   * @param req the req
   * @param res the res
   * @return the items
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws SQLException the SQL exception
   * @throws Exception the exception
   */
  @IgnoreConfidentialFilters
  public ActionForward getItems(ActionMapping map, ActionForm form, HttpServletRequest req,
      HttpServletResponse res) throws IOException, SQLException, Exception {
    JSONSerializer js = new JSONSerializer().exclude("calss");
    List ilist = new StoresStockAdjustDAO().getItemFromStore(req.getParameter("storeid"));
    res.setContentType("text/plain");
    res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    js.deepSerialize(ConversionUtils.listBeanToListMap(ilist), res.getWriter());
    res.flushBuffer();
    return null;
  }

  /**
   * Gets the items without approval.
   *
   * @param map the map
   * @param form the form
   * @param req the req
   * @param res the res
   * @return the items without approval
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws SQLException the SQL exception
   * @throws Exception the exception
   */
  @IgnoreConfidentialFilters
  public ActionForward getItemsWithoutApproval(ActionMapping map, ActionForm form,
      HttpServletRequest req, HttpServletResponse res) throws IOException, SQLException, Exception {
    JSONSerializer js = new JSONSerializer().exclude("calss");
    List ilist = new StoresStockAdjustDAO()
        .getItemFromStoreWithoutApproval(req.getParameter("storeid"));
    res.setContentType("text/plain");
    res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    res.getWriter().write(js.serialize(ilist));
    res.flushBuffer();
    return null;
  }

  /**
   * Gets the identifiers.
   *
   * @param map the map
   * @param form the form
   * @param req the req
   * @param res the res
   * @return the identifiers
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws SQLException the SQL exception
   * @throws Exception the exception
   */
  @IgnoreConfidentialFilters
  public ActionForward getIdentifiers(ActionMapping map, ActionForm form, HttpServletRequest req,
      HttpServletResponse res) throws IOException, SQLException, Exception {
    JSONSerializer js = new JSONSerializer().exclude("calss");
    List list = new StoresStockAdjustDAO().getItemsFromStore(req.getParameter("storeid"),
        req.getParameter("itemid"));
    res.setContentType("text/plain");
    res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    res.getWriter().write(js.serialize(list));
    res.flushBuffer();
    return null;
  }

  /**
   * Save.
   *
   * @param map the map
   * @param form the form
   * @param req the req
   * @param res the res
   * @return the action forward
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws SQLException the SQL exception
   * @throws Exception the exception
   */
  public ActionForward save(ActionMapping map, ActionForm form, HttpServletRequest req,
      HttpServletResponse res) throws IOException, SQLException, Exception {

    ArrayList<StockAdjustDTO> stockList = new ArrayList<StockAdjustDTO>();
    Connection con = null;
    GenericDAO adjMainDao = null;
    int adjNo = -1;
    BasicDynaBean bean = null;
    BasicDynaBean adjBean = null;
    ActionRedirect redirect = null;

    HttpSession session = req.getSession();
    String username = (String) session.getAttribute("userid");

    boolean success = true;
    String msg = "";
    String infomsg = "";
    Map params = req.getParameterMap();
    String[] itemId = (String[]) params.get("medicine_id");
    String[] remarks = (String[]) params.get("adjRemarks");
    String[] qty = (String[]) params.get("adjQty");

    String[] incType = (String[]) params.get("adjType");
    String storeId = req.getParameter("store_id");
    String reason = req.getParameter("reason");
    String[] itemBatchId = (String[]) params.get("item_batch_id");
    String[] itemLotId = (String[]) params.get("item_lot_id");
    String[] description = (String[]) params.get("description");
    List<Map<String, Object>> cacheAdjTxns = new ArrayList<>();
		BasicDynaBean module = modules.findByKey("module_id", "mod_scm");

    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      int storeIdNum = -1;
      if ((storeId != null) && (storeId.length() > 0)) {
        storeIdNum = Integer.parseInt(storeId);
      }
      for (int i = 0; i < itemId.length - 1; i++) {
        if ((null != itemId[i]) && !(((String) itemId[i]).equals(""))) {
          StockAdjustDTO dto = new StockAdjustDTO();
          dto.setQty(qty[i]);
          dto.setRemarks(remarks[i]);
          dto.setStoreId(storeIdNum);
          dto.setItemId(Integer.parseInt(itemId[i]));
          dto.setIncType(incType[i]);
          dto.setItemBatchId(Integer.parseInt(itemBatchId[i]));
          dto.setItemLotId(Integer.parseInt(itemLotId[i]));
          dto.setDescription(description[i]);

          stockList.add(dto);
        }
      }

      if (null != stockList && stockList.size() > 0) {
        adjMainDao = new GenericDAO("store_adj_main");
        adjNo = Integer
            .parseInt((new StockUserIssueDAO(con).getSequenceId("stockadjust_sequence")));
        bean = adjMainDao.getBean();
        bean.set("adj_no", adjNo);
        Date date = new Date();
        java.sql.Timestamp dt = new java.sql.Timestamp(date.getTime());
        bean.set("date_time", dt);
        bean.set("store_id", storeIdNum);
        bean.set("username", (String) req.getSession(false).getAttribute("userid"));
        bean.set("reason", reason);
        success = adjMainDao.insert(con, bean);

        GenericDAO adjDAo = new GenericDAO("store_adj_details");

        if (success) {

          if (stockList != null && stockList.size() > 0) {
            for (StockAdjustDTO stockAdjDTO : stockList) {
              // stock adjustment
              Map statusMap = null;
              StockFIFODAO fifoDAO = new StockFIFODAO();

              // adjustment details
              int adjDetailNo = adjDAo.getNextSequence();
              adjBean = adjDAo.getBean();
              adjBean.set("adj_no", adjNo);
              adjBean.set("adj_detail_no", adjDetailNo);
              adjBean.set("medicine_id", stockAdjDTO.getItemId());
              adjBean.set("item_batch_id", stockAdjDTO.getItemBatchId());
              adjBean.set("type", stockAdjDTO.getIncType());
              adjBean.set("description", stockAdjDTO.getDescription());
              BigDecimal quantity = null;
              if (stockAdjDTO.getQty() != null && !stockAdjDTO.getQty().equals("")) {
                quantity = new BigDecimal(Float.parseFloat(stockAdjDTO.getQty()));
              } else {
                quantity = new BigDecimal(0);
              }
              adjBean.set("qty", quantity);
              adjBean.set("description", stockAdjDTO.getDescription());

              if (stockAdjDTO.getIncType().equals("A")) {
                statusMap = fifoDAO.addStockByLot(con, storeIdNum, stockAdjDTO.getItemLotId(), "A",
                    new BigDecimal(stockAdjDTO.getQty()), username, "StockAdjust", adjDetailNo,
                    stockAdjDTO.getRemarks());

                success = (Boolean) statusMap.get("status");
                adjBean.set("cost_value", ((BigDecimal) statusMap.get("costValue")).negate());
              } else {
                statusMap = fifoDAO.reduceStockByLot(con, storeIdNum, stockAdjDTO.getItemLotId(), "A",
                    new BigDecimal(stockAdjDTO.getQty()), username, "StockAdjust", adjDetailNo,
                    stockAdjDTO.getRemarks());

                success = (Boolean) statusMap.get("status");
                adjBean.set("cost_value", statusMap.get("costValue"));

                if (!success) {
                  infomsg = "Stock can not be decreased to negative for few items.";
                  continue;
                }
              }

              // adjustment details
              success = adjDAo.insert(con, adjBean);
              if (!success) {
                break;
              }

              if ( module != null && success &&
				          ((String)module.get("activation_status")).equals("Y")) {
				        cacheStockAdj(bean, adjBean, cacheAdjTxns);
				      }
              adjBean = null;
            }
          }

        }

      }

    } finally {
      DataBaseUtil.commitClose(con, success);

      if (!cacheAdjTxns.isEmpty() && module != null && success &&
          ((String)module.get("activation_status")).equals("Y")) {
        scmOutService.scheduleStockAdjTxns(cacheAdjTxns);
      }

      // update stock timestamp
      StockFIFODAO stockFIFODAO = new StockFIFODAO();
      stockFIFODAO.updateStockTimeStamp();
      stockFIFODAO.updateStoresStockTimeStamp(Integer.parseInt(storeId));
      if (success) {
        msg = "" + adjNo;
      }
    }

    redirect = new ActionRedirect(map.findForward("addRedirect"));
    FlashScope flash = FlashScope.getScope(req);
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
    flash.put("msg", msg);
    if (!infomsg.isEmpty()) {
      flash.put("info", infomsg);
    }
    return redirect;
  }

  /**
   * Gets the stock adj for rej indents.
   *
   * @param map the map
   * @param form the form
   * @param req the req
   * @param res the res
   * @return the stock adj for rej indents
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws SQLException the SQL exception
   * @throws Exception the exception
   */
  @IgnoreConfidentialFilters
  /** From Rejected Transfer Indents dashboard */
  public ActionForward getStockAdjForRejIndents(ActionMapping map, ActionForm form,
      HttpServletRequest req, HttpServletResponse res) throws IOException, SQLException, Exception {

    List stockDetailsToAdjust = null;
    JSONSerializer js = new JSONSerializer().exclude("class");
    req.setAttribute("medicine_timestamp", MedicineStockDAO.getMedicineTimestamp());
    String deptId = "";

    String transaction = req.getParameter("_transaction");
    if (transaction != null) {
      List medIds = new ArrayList();
      List batchNos = new ArrayList();
      List qty = new ArrayList();
      List indentno = new ArrayList();
      List deptIds = new ArrayList();
      Map<String, String[]> params = req.getParameterMap();
      String[] selected = (String[]) params.get("_selected");
      String[] medicineId = (String[]) params.get("_medicine_id");
      String[] dept = (String[]) params.get("_dept_id");
      String[] rejQty = (String[]) params.get("_qty");
      String[] indent = (String[]) params.get("_indentno");
      String[] batchNo = (String[]) params.get("_batchno");
      for (int i = 0; i < medicineId.length; i++) {
        if (selected[i].equals("Y")) {
          medIds.add(medicineId[i]);
          batchNos.add(batchNo[i]);
          qty.add(rejQty[i]);
          indentno.add(indent[i]);
          deptId = (String) dept[0];
        }

      }

      stockDetailsToAdjust = new StoresStockAdjustDAO().getItemsListFromStore(deptId, medIds,
          batchNos, indentno);

    }
    req.setAttribute("stockDetailsToAdjust", js.serialize(stockDetailsToAdjust));
    if (!deptId.equals("")) {
      req.setAttribute("dept_id", deptId);
      String deptName = StoresIndentDAO.getStoreName(Integer.parseInt(deptId));
      req.setAttribute("dept_name", deptName);
    }

    return map.findForward("addshow");
  }

  /**
   *  From Rejected Transfer Indents dashboard.
   *
   * @param map the map
   * @param form the form
   * @param req the req
   * @param res the res
   * @return the action forward
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws SQLException the SQL exception
   * @throws Exception the exception
   */
  public ActionForward updateStockInStore(ActionMapping map, ActionForm form,
      HttpServletRequest req, HttpServletResponse res) throws IOException, SQLException,
      Exception {

    HttpSession session = req.getSession();
    String username = (String) session.getAttribute("userid");
    Connection con = null;

    String msg = "Updating Stock ";
    ActionRedirect redirect = null;
    boolean success = false;
    JSONSerializer js = new JSONSerializer().exclude("class");
    req.setAttribute("medicine_timestamp", MedicineStockDAO.getMedicineTimestamp());
    String deptId = "";
    String transaction = req.getParameter("_transaction");
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      if (transaction != null) {
        deptId = req.getParameter("dept_id");
        Map<String, String[]> params = req.getParameterMap();
        String[] selected = (String[]) params.get("_selected");
        String[] medicineId = (String[]) params.get("_medicine_id");
        String[] dept = (String[]) params.get("_dept_id");
        String[] rejQty = (String[]) params.get("_qty");
        String[] indent = (String[]) params.get("_indentno");
        String[] batchNo = (String[]) params.get("_batchno");
        String[] itemBatchId = (String[]) params.get("_item_batch_no");
        for (int i = 0; i < medicineId.length; i++) {
          if (selected[i].equals("Y")) {
            int medId = Integer.parseInt(medicineId[i]);
            String batchno = batchNo[i];
            float qtyRejected = Float.parseFloat(rejQty[i]);
            int indentno = Integer.parseInt(indent[i]);
            int deptIdNum = Integer.parseInt(dept[i]);
            if (new StockUserReturnDAO(con).updateStock(medId, batchno, qtyRejected, deptIdNum, 4,
                "item", (String) session.getAttribute("userid"))) {
              success = new StockUserReturnDAO(con).updateTransitStock(medId,
                  Integer.parseInt(itemBatchId[i]), qtyRejected, deptIdNum,
                  (String) session.getAttribute("userid"), "Transfer");
            }
            if (success) {
              /** update the indent status. */
              BasicDynaBean stockTransfer = DataBaseUtil.queryToDynaBean(
                  StoresStockAdjustDAO.INDENT_TRANSFER_ITEMS, new Object[] { indentno, medId,
                      Integer.parseInt(itemBatchId[i]), deptIdNum, new Float(qtyRejected) });

              if (StoresIndentProcessDAO.updateIndentNStockTransfer(stockTransfer, username)) {
                success = true;
              }
            }
          }
        }
        con.commit();
      }

    } finally {
      if (con != null) {
        con.close();
      }
    }

    redirect = new ActionRedirect(
        "/stores/StoresViewRejectIndents.do?_method=listReject&sortOrder=indent_no"
        + "&sortReverse=true");

    FlashScope flash = FlashScope.getScope(req);
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
    if (success) {
      flash.put("msg", "Take Back into Stock is successful");
    } else {
      flash.put("error", "Transaction Failed");
    }
    return redirect;
  }

  /**
   * Gets the item details.
   *
   * @param am the am
   * @param af the af
   * @param req the req
   * @param res the res
   * @return the item details
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ServletException the servlet exception
   * @throws SQLException the SQL exception
   */
  @IgnoreConfidentialFilters
  public ActionForward getItemDetails(ActionMapping am, ActionForm af, HttpServletRequest req,
      HttpServletResponse res) throws IOException, ServletException, SQLException {

    int medicineId = Integer.parseInt(req.getParameter("medicineId"));
    int storeId = Integer.parseInt(req.getParameter("store"));

    BasicDynaBean item = StockEntryDAO.getItemDetails(medicineId, storeId);
    Map resultMap = new HashMap(item.getMap());

    // item batches
    List<BasicDynaBean> batches = StockEntryDAO.getItemBatcheLotDetails(storeId, medicineId);
    resultMap.put("batches", ConversionUtils.copyListDynaBeansToMap(batches));

    res.setContentType("application/x-json");
    res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    res.setHeader("Expires", "0");
    JSONSerializer js = new JSONSerializer().exclude("class");
    res.getWriter().write(js.deepSerialize(resultMap));
    res.flushBuffer();
    return null;
  }

  /**
   * Gets the batch lot details.
   *
   * @param am the am
   * @param af the af
   * @param req the req
   * @param res the res
   * @return the batch lot details
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ServletException the servlet exception
   * @throws SQLException the SQL exception
   */
  @IgnoreConfidentialFilters
  public ActionForward getBatchLotDetails(ActionMapping am, ActionForm af, HttpServletRequest req,
      HttpServletResponse res) throws IOException, ServletException, SQLException {

    int itemBatchId = Integer.parseInt(req.getParameter("item_batch_id"));
    int storeId = Integer.parseInt(req.getParameter("store"));

    res.setContentType("application/x-json");
    res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    res.setHeader("Expires", "0");
    List<BasicDynaBean> batchLots = new StockFIFODAO().getBatchLotDetails(storeId, itemBatchId);
    JSONSerializer js = new JSONSerializer().exclude("class");
    res.getWriter().write(js.deepSerialize(ConversionUtils.listBeanToListMap(batchLots)));
    res.flushBuffer();
    return null;
  }

  private void cacheStockAdj(BasicDynaBean adjMain, BasicDynaBean adjDetails,
      List<Map<String,Object>> cacheAdjTxns) {
    Map<String, Object> data = scmOutService.getStockAdjMap(adjMain, adjDetails);
    if(!data.isEmpty()) {
      cacheAdjTxns.add(data);
    }

  }

}
