package com.insta.hms.stores;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.integration.scm.inventory.ScmOutBoundInvService;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.modules.ModulesDAO;
import com.insta.hms.usermanager.Role;
import com.insta.hms.usermanager.UserDAO;
import com.insta.hms.usermanager.UserDashBoardDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import flexjson.JSONSerializer;

/**
 * The Class StoresIndentReceiveAction.
 */
public class StoresIndentReceiveAction extends BaseAction {

  /** The log. */
  private static final Logger log = LoggerFactory.getLogger(StoresIndentReceiveAction.class);

  /** The mdao. */
  private static GenericDAO mdao = new GenericDAO("store_indent_main");

  private static ScmOutBoundInvService scmOutService = ApplicationContextProvider
      .getBean(ScmOutBoundInvService.class);
  private static final ModulesDAO modules = new ModulesDAO();
  
  private static final GenericDAO storesDAO = new GenericDAO("stores");

  /**
   * List.
   *
   * @param am the am
   * @param af the af
   * @param req the req
   * @param res the res
   * @return the action forward
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ServletException the servlet exception
   * @throws Exception the exception
   */
  @IgnoreConfidentialFilters
  public ActionForward list(ActionMapping am, ActionForm af, HttpServletRequest req,
      HttpServletResponse res) throws IOException, ServletException, Exception {

    Map<Object, Object> map = getParameterMap(req);
    PagedList pagedList = null;
    BasicDynaBean usreStoresBean = null;
    String userStores = null;

    String deptFrom = req.getParameter("dept_from");
    deptFrom = (deptFrom == null || deptFrom.isEmpty()) ? null : deptFrom;
    String indentStore = req.getParameter("indent_store");
    indentStore = (indentStore == null || indentStore.isEmpty()) ? null : indentStore;
    HttpSession session = req.getSession(false);
    String userName = (String) req.getSession(false).getAttribute("userid");
    String deptId = (String) session.getAttribute("pharmacyStoreId");
    String indentApprovalBy = (String) GenericPreferencesDAO.getAllPrefs()
        .get("indent_approval_by");
    String indentStores = null;
    String requestStores = null;
    indentStores = getIndentStores(userName, indentStore, indentApprovalBy);
    requestStores = getRequestingStores(userName, deptFrom, indentApprovalBy);
    int centerid = (Integer) req.getSession(false).getAttribute("centerId");
    if (centerid > 0) {
      map.put("requesting_center_id", new String[] { centerid + "" });
      map.put("requesting_center_id@type", new String[] { "integer" });
    }

    /* check if user has access right to view data of all stores */
    HashMap actionRightsMap = new HashMap();
    Object roleID = null;
    Role role = new Role();
    actionRightsMap = (HashMap) req.getSession(false).getAttribute("actionRightsMap");
    roleID = req.getSession(false).getAttribute("roleId");
    String actionRightStatus = (String) session.getAttribute("multiStoreAccess");
    if (actionRightStatus == null) {
      actionRightStatus = "N";
    }
    int roleId = ((Integer) roleID).intValue();
    if ((roleId == 1) || (roleId == 2)) {
      /** User has access to all stores or he is in admin role..let him see all */
      // show all indents for receive
      pagedList = StoresIndentDAO.searchReceivedIndentList(map,
          ConversionUtils.getListingParameter(map), null);
    } else if (actionRightStatus.equals("A")) {
      /** User with multi store access except role1 and role2 */
      userStores = getUserStores(userName);
      if (userStores != null) {
        pagedList = StoresIndentDAO.searchReceivedIndentList(map,
            ConversionUtils.getListingParameter(map), userStores);
      }
    } else {
      /** get indents pertaining to logged in user's store */
      if (deptId != null && !deptId.equals("")) {
        map.put("dept_from", new String[] { deptId });
        pagedList = StoresIndentDAO.searchReceivedIndentList(map,
            ConversionUtils.getListingParameter(map), null);
      } else {
        pagedList = new PagedList();
      }
    }
    req.setAttribute("pagedList", pagedList);
    req.setAttribute("genPrefs", GenericPreferencesDAO.getGenericPreferences());
    List allUserNames = UserDashBoardDAO.getAllUserNames();
    req.setAttribute("userNameList", new JSONSerializer().serialize(allUserNames));
    if (GenericPreferencesDAO.getGenericPreferences().getAllow_cross_center_indents().equals("N")) {
      req.setAttribute("storesList", getStoresList(req));
      req.setAttribute("indent_store", indentStore);
    }
    req.setAttribute("multiCentered",
        GenericPreferencesDAO.getGenericPreferences().getMax_centers_inc_default() > 1);
    req.setAttribute("centers", CenterMasterDAO.getAllCentersExceptSuper());
    return am.findForward("list");
  }

  /**
   * Gets the indent stores.
   *
   * @param userName the user name
   * @param indentStore the indent store
   * @param indentApprovalBy the indent approval by
   * @return the indent stores
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ServletException the servlet exception
   * @throws Exception the exception
   */
  public String getIndentStores(String userName, String indentStore, String indentApprovalBy)
      throws IOException, ServletException, Exception {
    String indentStores = null;
    if (null != indentStore) {
      // 1 store, return the same
      return indentStore;
    }

    if ("I".equalsIgnoreCase(indentApprovalBy)) {
      // Approval by Indent store user, return all user stores
      indentStores = getUserStores(userName);
    }
    // will be comma separated list of user stores if approval by "I", null if
    // approval by "R"
    return indentStores; 
  }

  /**
   * Gets the requesting stores.
   *
   * @param userName the user name
   * @param requestingStrore the requesting strore
   * @param indentApprovalBy the indent approval by
   * @return the requesting stores
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ServletException the servlet exception
   * @throws Exception the exception
   */
  public String getRequestingStores(String userName, String requestingStrore,
      String indentApprovalBy) throws IOException, ServletException, Exception {
    String deptFrom = null;
    if (null != requestingStrore) {
      // 1 store, return the same
      return requestingStrore;
    }

    if ("R".equalsIgnoreCase(indentApprovalBy)) {
      // Approval by Requesting store user, return all user stores
      deptFrom = getUserStores(userName);
    }

    // will be comma separated list of user stores if approval by "R", null if
    // approval by "I"
    return deptFrom; 
  }

  /* To get user stores */

  /**
   * Gets the user stores.
   *
   * @param userName the user name
   * @return the user stores
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ServletException the servlet exception
   * @throws Exception the exception
   */
  public String getUserStores(String userName) throws IOException, ServletException, Exception {
    BasicDynaBean usreStoresBean = null;
    String userStores = null;
    usreStoresBean = UserDAO.getRecord(userName);
    if (usreStoresBean != null) {
      userStores = (String) usreStoresBean.get("multi_store");
    }
    return userStores;
  }

  /**
   * Gets the stores list.
   *
   * @param req the req
   * @return the stores list
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ServletException the servlet exception
   * @throws Exception the exception
   */
  public List getStoresList(HttpServletRequest req)
      throws IOException, ServletException, Exception {
    int centerId = (Integer) req.getSession().getAttribute("centerId");
    List storeList = null;
    if (GenericPreferencesDAO.getGenericPreferences().getAllow_cross_center_indents().equals("N")) {
      HashMap filterMap = new HashMap();
      filterMap.put("is_super_store", "Y");
      filterMap.put("status", "A");
      if (centerId != 0) {
        filterMap.put("center_id", centerId);
      }
      List columns = new ArrayList();
      columns.add("dept_name");
      columns.add("dept_id");
      storeList = storesDAO.listAll(columns, filterMap, "dept_name");
    }
    return storeList;
  }

  /**
   * List reject.
   *
   * @param am the am
   * @param af the af
   * @param req the req
   * @param res the res
   * @return the action forward
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ServletException the servlet exception
   * @throws Exception the exception
   */
  @IgnoreConfidentialFilters
  public ActionForward listReject(ActionMapping am, ActionForm af, HttpServletRequest req,
      HttpServletResponse res) throws IOException, ServletException, Exception {

    Map<Object, Object> map = getParameterMap(req);
    PagedList pagedList = null;
    HttpSession session = req.getSession(false);
    String deptId = (String) session.getAttribute("pharmacyStoreId");
    String indentStores = null;
    String requestStores = null;
    String isUserHavingSuperStore = "N";
    String userName = (String) req.getSession(false).getAttribute("userid");
    String storeAccess = "N";

    indentStores = getIndentStores(userName,
        map.get("indent_store") != null
            ? String.valueOf(((String[]) map.get("indent_store"))[0])
            : null,
        "I");
    isUserHavingSuperStore = (new StoresIndentApprovalAction().isSuperStoreUser(indentStores))
        ? "Y"
        : "N";
    String accessStores = getUserStores(userName);
    storeAccess = (accessStores != null && !accessStores.isEmpty()) ? "Y" : "N";

    /* check if user has access right to view data of all stores */
    HashMap actionRightsMap = new HashMap();
    Object roleID = null;
    Role role = new Role();
    actionRightsMap = (HashMap) req.getSession(false).getAttribute("actionRightsMap");
    roleID = req.getSession(false).getAttribute("roleId");
    String actionRightStatus = (String) session.getAttribute("multiStoreAccess");
    if (actionRightStatus == null) {
      actionRightStatus = "N";
    }
    int roleId = ((Integer) roleID).intValue();
    if ((actionRightStatus.equals("A")) || (roleId == 1 || roleId == 2)) {
      /** User has access to all stores or he is in admin role..let him see all indents */
      if (map.get("indent_store") == null) {
        // setting a default store search
        map.put("indent_store", new String[] { deptId });
        map.put("indent_store@type", new String[] { "integer" });
      }
      pagedList = StoresIndentDAO.searchRejectedIndentList(map,
          ConversionUtils.getListingParameter(map));

    } else {
      /** user can see data only pertaining to his store */
      if (deptId != null && !deptId.equals("")) {
        if (map.get("indent_store") == null) {
          map.put("indent_store", new String[] { deptId });
          map.put("indent_store@type", new String[] { "integer" });
        }
        pagedList = StoresIndentDAO.searchRejectedIndentList(map,
            ConversionUtils.getListingParameter(map));
      } else {
        pagedList = new PagedList();
      }
    }
    req.setAttribute("pagedList", pagedList);

    if (deptId != null && !deptId.equals("")) {
      BasicDynaBean dept = storesDAO.findByKey("dept_id", Integer.parseInt(deptId));
      String deptName = dept.get("dept_name").toString();
      req.setAttribute("indent_store", deptId);
      req.setAttribute("dept_name", deptName);
      req.setAttribute("dept_id", deptId);
    } else {
      req.setAttribute("indent_store", deptId);
      req.setAttribute("dept_id", deptId);
    }
    req.setAttribute("genPrefs", GenericPreferencesDAO.getGenericPreferences());
    List allUserNames = UserDashBoardDAO.getAllUserNames();
    req.setAttribute("userNameList", new JSONSerializer().serialize(allUserNames));
    req.setAttribute("isUserHavingSuperStoreJson",
        new JSONSerializer().exclude("class").serialize(isUserHavingSuperStore));
    req.setAttribute("storeAccess", storeAccess);
    return am.findForward("listreject");
  }

  /**
   * Show.
   *
   * @param map the map
   * @param form the form
   * @param req the req
   * @param resp the resp
   * @return the action forward
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ServletException the servlet exception
   * @throws Exception the exception
   */
  @IgnoreConfidentialFilters
  public ActionForward show(ActionMapping map, ActionForm form, HttpServletRequest req,
      HttpServletResponse resp) throws IOException, ServletException, Exception {
    int indentNo = Integer.parseInt(req.getParameter("indent_no"));

    BasicDynaBean indentdetails = StoresIndentDAO.getIndentApprovalRejectDetails(indentNo);
    req.setAttribute("indentdetails", indentdetails);
    int storeId = 0;
    String deptName = null;
    List<BasicDynaBean> indentlist = null;
    if (indentdetails.get("indent_type").equals("S")) {
      storeId = Integer.parseInt(indentdetails.get("dept_from").toString());
      indentlist = StoresIndentDAO.getIndentItemDetailsForReceipt(indentNo, storeId);
      deptName = StoresIndentDAO.getStoreName(storeId);
    }

    HttpSession session = req.getSession(false);
    String deptId = (String) session.getAttribute("pharmacyStoreId");
    if (deptId != null && !deptId.equals("")) {
      BasicDynaBean dept = storesDAO.findByKey("dept_id", Integer.parseInt(deptId));
      String deptName1 = dept.get("dept_name").toString();
      req.setAttribute("dept_id", deptId);
      req.setAttribute("dept_name", deptName1);
    }
    if (deptId != null && deptId.equals("")) {
      req.setAttribute("dept_id", deptId);
    }

    req.setAttribute("indentlist", indentlist);

    List<BasicDynaBean> transfers = StoresIndentDAO.getIndentTransfers(indentNo);
    req.setAttribute("transferList",
        ConversionUtils.listBeanToMapListMap(transfers, "medicine_id"));

    String storeName = StoresIndentDAO
        .getStoreName(Integer.parseInt(indentdetails.get("indent_store").toString()));
    req.setAttribute("storeName", storeName);
    req.setAttribute("deptName", deptName);
    req.setAttribute("genPrefs", GenericPreferencesDAO.getGenericPreferences());
    return map.findForward("addshow");
  }

  /**
   * Update.
   *
   * @param map the map
   * @param form the f
   * @param req the req
   * @param resp the resp
   * @return the action forward
   * @throws Exception the exception
   */
  @IgnoreConfidentialFilters
  public ActionForward update(ActionMapping map, ActionForm form, HttpServletRequest req,
      HttpServletResponse resp) throws Exception {
    Connection con = null;
    ActionRedirect redirect = null;
    FlashScope flash = null;
    String msg = null;
    String issuemessage = null;
    String transfermessage = null;
    int indentNo = 0;
    boolean success = true;
    ArrayList<String> failedItems = new ArrayList<String>();
    String recvStore = (String) req.getParameter("recv_store");
    Map<String, String> indentStatusInfoMap = new HashMap<String, String>();
    BasicDynaBean module = modules.findByKey("module_id", "mod_scm");
		List<Map<String, Object>> cacheTransferTxns = new ArrayList<>();

    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      flash = FlashScope.getScope(req);
      redirect = new ActionRedirect(map.findForward("listRedirect"));
      PharmacymasterDAO itemmasterDAO = new PharmacymasterDAO(con);

      indentNo = Integer.parseInt(req.getParameter("indentNo"));
      String indentStore = (String) req.getParameter("send_store");
      HttpSession session = req.getSession(false);
      String userid = (String) session.getAttribute("userid");
      String remarks = req.getParameter("remarks");
      msg = "Receiving failed for Indent No: " + indentNo;

      String[] itemid = req.getParameterValues("itemid");
      String[] indentSelect = req.getParameterValues("indentSelect");
      BigDecimal[] indentQtys = StoresDBTablesUtil.copyStringArrayTOBigdecimal(itemid.length,
          req.getParameterValues("indent_qty"));

      List<Object> itemList = new ArrayList<Object>();
      Map<String, Object> itemBatchQtyMap = null;
      Map<String, Object> qtyBatchMap = null;
      List<Map<String, Object>> attributeList = null;

      // for all items in indent, get the required values
      for (int i = 0; i < itemid.length; i++) {
        String[] transferNo = req.getParameterValues("transfer_no" + i);
        String[] transferDetailNo = req.getParameterValues("transfer_detail_no" + i);
        String[] batch = req.getParameterValues("batch_no" + i);
        String[] itemBatchId = req.getParameterValues("item_batch_id" + i);
        String[] process = req.getParameterValues("process" + i);
        if (batch == null || batch.length == 0) {
          continue;
        }
        int len = batch.length;

        BigDecimal[] qtyRecd = StoresDBTablesUtil.copyStringArrayTOBigdecimal(len,
            req.getParameterValues("item_qty_rec" + i));
        BigDecimal[] qtyRej = StoresDBTablesUtil.copyStringArrayTOBigdecimal(len,
            req.getParameterValues("item_qty_rej" + i));
        attributeList = new ArrayList<Map<String, Object>>();
        for (int j = 0; j < len; j++) {
          if (!process[j].equals("Y")) {
            continue;
          }
          qtyBatchMap = new HashMap<String, Object>();
          qtyBatchMap.put("TRANSFERNO", transferNo[j]);
          qtyBatchMap.put("IDENTIFIER", itemBatchId[j]);
          qtyBatchMap.put("RECD_QTY", qtyRecd[j]);
          qtyBatchMap.put("REJ_QTY", qtyRej[j]);
          qtyBatchMap.put("TRANSFER_DETAIL_NO", transferDetailNo[j]);

          Map<String, Object> fields = new HashMap<String, Object>();
          Map<String, Object> keyFields = new HashMap<String, Object>();
          keyFields.put("transfer_no", Integer.parseInt(transferNo[j]));
          fields.put("received_date", DataBaseUtil.getDateandTime());
          fields.put("received_by", userid);

          log.debug("Tx: " + transferNo[j] + " Batch: " + batch[j] + " R: " + qtyRecd[j] + " J: "
              + qtyRej[j]);
          if (new GenericDAO("store_transfer_main").update(con, fields, keyFields) > 0) {
            attributeList.add(qtyBatchMap);
          }

        }
        String medName = (String) itemmasterDAO
            .findByKey("medicine_id", Integer.parseInt(itemid[i])).get("medicine_name");
        BasicDynaBean mediicneBean = itemmasterDAO.findByKey("medicine_id",
            Integer.parseInt(itemid[i]));
        if (attributeList.size() > 0) {
          itemBatchQtyMap = new HashMap<String, Object>();
          itemBatchQtyMap.put("MEDICINE_ID", itemid[i]);
          itemBatchQtyMap.put("MEDICINE_NAME", (String) mediicneBean.get("medicine_name"));
          itemBatchQtyMap.put("RECV_STORE", recvStore);
          itemBatchQtyMap.put("INDENT_STORE", indentStore);
          itemBatchQtyMap.put("INDENT_QTY", indentQtys[i]);
          itemBatchQtyMap.put("ITEM_LIST", attributeList);
          itemList.add(itemBatchQtyMap);
        }
      }

      if (itemList.size() > 0) {
        BasicDynaBean indentBean = mdao.findByKey(con, "indent_no", indentNo);
        success &= StoresIndentProcessDAO.updateStockTransfer(con, indentBean, itemList, remarks,
            userid, failedItems, indentStatusInfoMap, cacheTransferTxns);
        StoresIndentProcessDAO.updateIndentReceivedStatus(con, indentNo, msg, userid);
      }

      success = success && true;
      if (!success) {
        msg = "Receive Failed.";
      } else {
        msg = "Receive processed successfully.";
      }

    } finally {
      DataBaseUtil.commitClose(con, success);

      // update stock timestamp
      StockFIFODAO stockFIFODAO = new StockFIFODAO();
      stockFIFODAO.updateStockTimeStamp();
      stockFIFODAO.updateStoresStockTimeStamp(Integer.parseInt(recvStore));
      
      if (success && !cacheTransferTxns.isEmpty() && module != null
          && ((String) module.get("activation_status")).equals("Y")) {
        scmOutService.scheduleStockTransferTxns(cacheTransferTxns);
      }

      if (success) {
        flash.put("info", msg);
      } else {
        flash.put("error", msg);
      }
      String insuffInfo = "";
      for (String failedItem : failedItems) {
        insuffInfo = insuffInfo.concat(failedItem).concat("</br>");
      }
      String indentStatusInfo = indentStatusInfoMap.get("indent_status");
      if (indentStatusInfo != null && !indentStatusInfo.isEmpty()) {
        flash.put("info", indentStatusInfo);
      } else {
        flash.put("info", insuffInfo.isEmpty() ? msg : insuffInfo);
      }
      redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
    }

    return redirect;
  }

  /**
   * Update rejected stock in store.
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
  public ActionForward updateRejectedStockInStore(ActionMapping map, ActionForm form,
      HttpServletRequest req, HttpServletResponse res) throws IOException, SQLException, Exception {

    Connection con = null;
    GenericDAO stockDetDAO = new GenericDAO("store_stock_details");
    GenericDAO lotTxnDetDAO = new GenericDAO("store_transaction_lot_details");

    ActionRedirect redirect = null;
    boolean success = true;
    req.setAttribute("medicine_timestamp", MedicineStockDAO.getMedicineTimestamp());
    String transaction = req.getParameter("_transaction");
    HttpSession session = req.getSession(false);
    String userid = (String) session.getAttribute("userid");
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      if (transaction != null) {
        Map<String, String[]> params = req.getParameterMap();
        String[] selected = (String[]) params.get("_selected");
        String[] medicineId = (String[]) params.get("_medicine_id");
        String[] dept = (String[]) params.get("_dept_id");
        String[] transferDetailsId = (String[]) params.get("_transfer_detail_no");
        for (int i = 0; i < medicineId.length; i++) {
          if (selected[i].equals("Y")) {
            int deptIdNum = Integer.parseInt(dept[i]);

            HashMap<String, Object> transLotKeys = new HashMap<String, Object>();
            transLotKeys.put("transaction_id", Integer.parseInt(transferDetailsId[i]));
            transLotKeys.put("transaction_type", "RJ");
            List<BasicDynaBean> transactionLotBeans = lotTxnDetDAO.listAll(null, transLotKeys,
                null);

            for (BasicDynaBean lot : transactionLotBeans) {
              int itemLotId = (Integer) lot.get("item_lot_id");
              BigDecimal lotQty = (BigDecimal) lot.get("qty");

              HashMap<String, Object> stockKeys = new HashMap<String, Object>();
              stockKeys.put("item_lot_id", itemLotId);
              stockKeys.put("dept_id", deptIdNum);
              BasicDynaBean istockBean = stockDetDAO.findByKey(con, stockKeys);
              success &= StoresIndentReceiveAction.addQtyToStockDetails(con,
                  (Integer) istockBean.get("store_stock_id"), lotQty) > 0;
            }

            if (success) {
              HashMap<String, Object> indentDetailsKey = new HashMap<String, Object>();
              indentDetailsKey.put("transfer_detail_no", Integer.parseInt(transferDetailsId[i]));

              HashMap<String, Object> indentDetailsColumns = new HashMap<String, Object>();
              indentDetailsColumns.put("is_rejected_qty_taken", "Y");
              indentDetailsColumns.put("reconsolidated_date", DataBaseUtil.getDateandTime());
              indentDetailsColumns.put("reconsolidated_by", userid);

              success &= new GenericDAO("store_transfer_details").update(con, indentDetailsColumns,
                  indentDetailsKey) > 0;
            }
          }
        }
        if (success) {
          con.commit();
        } else {
          con.rollback();
        }
      }

    } finally {
      if (con != null) {
        con.close();
      }
    }

    redirect = new ActionRedirect(
        "/stores/StoresRejectIndents.do?_method=listReject&sortOrder=indent_no&sortReverse=true");

    FlashScope flash = FlashScope.getScope(req);
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
    if (success) {
      flash.put("msg", "Take Back into Stock is successful");
    } else {
      flash.put("error", "Transaction Failed");
    }
    return redirect;
  }

  /** The Constant ADD_QTY_TO_STOCK_DETAILS. */
  private static final String ADD_QTY_TO_STOCK_DETAILS = " UPDATE store_stock_details SET"
      + " qty=qty+?, qty_in_rejection=qty_in_rejection-?  WHERE store_stock_id=? AND"
      + " qty_in_rejection > 0";

  /**
   * Adds the qty to stock details.
   *
   * @param con the con
   * @param stockId the stock id
   * @param qtyToAdd the qty to add
   * @return the int
   * @throws SQLException the SQL exception
   */
  private static int addQtyToStockDetails(Connection con, int stockId, BigDecimal qtyToAdd)
      throws SQLException {
    return DataBaseUtil.executeQuery(con, ADD_QTY_TO_STOCK_DETAILS,
        new Object[] { qtyToAdd, qtyToAdd, stockId });
  }

}