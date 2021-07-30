package com.insta.hms.stores.stockconsumption;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.integration.scm.inventory.ScmOutBoundInvService;
import com.insta.hms.modules.ModulesDAO;
import com.insta.hms.stores.StockFIFODAO;
import com.insta.hms.stores.StoresDBTablesUtil;
import com.insta.hms.stores.stockconsumption.StoreStockConsumptionBO.StockConsumptionDetails;

import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The Class StoreStockConsumptionAction.
 *
 * @author mithun.saha
 */

public class StoreStockConsumptionAction extends BaseAction {

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(StoreStockConsumptionAction.class);

  /** The dao. */
  StoreStockConsumptionDAO dao = new StoreStockConsumptionDAO();

  /** The maindao. */
  GenericDAO maindao = new GenericDAO("general_reagent_usage_main");

  /** The detailsdao. */
  GenericDAO detailsdao = new GenericDAO("general_reagent_usage_details");

  /** The js. */
  JSONSerializer js = new JSONSerializer().exclude("class");

  /** The modules dao. */
  private static ModulesDAO modules = new ModulesDAO();

  /** The scm outbound inventory service. */
  private static ScmOutBoundInvService scmOutService = ApplicationContextProvider
      .getBean(ScmOutBoundInvService.class);

  /**
   * Gets the store stock consumption list search screen.
   *
   * @param mapping the mapping
   * @param form    the form
   * @param req     the req
   * @param res     the res
   * @return the store stock consumption list search screen
   * @throws ServletException the servlet exception
   * @throws Exception        the exception
   */
  @IgnoreConfidentialFilters
  public ActionForward getStoreStockConsumptionListSearchScreen(ActionMapping mapping,
      ActionForm form, HttpServletRequest req, HttpServletResponse res)
      throws ServletException, Exception {

    return mapping.findForward("consumptionlist");
  }

  /**
   * Gets the store stock consumption list.
   *
   * @param mapping the mapping
   * @param form    the form
   * @param req     the req
   * @param res     the res
   * @return the store stock consumption list
   * @throws ServletException the servlet exception
   * @throws Exception        the exception
   */
  @IgnoreConfidentialFilters
  public ActionForward getStoreStockConsumptionList(ActionMapping mapping, ActionForm form,
      HttpServletRequest req, HttpServletResponse res) throws ServletException, Exception {

    String deptId = req.getParameter("store_id");
    deptId = (deptId == null || deptId.isEmpty()) ? null : deptId;
    PagedList pagedList = null;
    Map paramMap = req.getParameterMap();
    if (deptId != null) {
      pagedList = dao.getStoreConsumptionDetailsList(paramMap,
          ConversionUtils.getListingParameter(paramMap));
    }
    req.setAttribute("pagedList", pagedList);
    return mapping.findForward("consumptionlist");
  }

  /**
   * Gets the store stock consumption search screen.
   *
   * @param mapping the mapping
   * @param form    the form
   * @param req     the req
   * @param res     the res
   * @return the store stock consumption search screen
   * @throws ServletException the servlet exception
   * @throws Exception        the exception
   */
  @IgnoreConfidentialFilters
  public ActionForward getStoreStockConsumptionSearchScreen(ActionMapping mapping, ActionForm form,
      HttpServletRequest req, HttpServletResponse res) throws ServletException, Exception {

    req.setAttribute("itemNames",
        StoresDBTablesUtil.getTableDataInJSON(StoresDBTablesUtil.GET_MEDICINE_NAMES_IN_MASTER));
    return mapping.findForward("addStockConsumption");
  }

  /**
   * Show stock consumption.
   *
   * @param mapping the mapping
   * @param form    the form
   * @param req     the req
   * @param res     the res
   * @return the action forward
   * @throws ServletException the servlet exception
   * @throws Exception        the exception
   */
  @IgnoreConfidentialFilters
  public ActionForward showStockConsumption(ActionMapping mapping, ActionForm form,
      HttpServletRequest req, HttpServletResponse res) throws ServletException, Exception {

    PagedList pagedList = null;
    PagedList consumedDetailsList = null;
    BasicDynaBean mainBean = null;
    String consumptionStatus = null;
    String consumptionId = req.getParameter("_consumption_id");
    String storeName = null;
    consumptionId = (consumptionId == null || consumptionId.isEmpty()) ? null : consumptionId;
    Map paramMap = getParameterMap(req);
    paramMap.put("pageNum", req.getParameterValues("_stockPageNum"));
    mainBean = dao.findByKey("consumption_id", consumptionId);
    String deptId = req.getParameter("dept_id");

    if (deptId != null && !paramMap.containsKey("dept_id@type")) {
      paramMap.put("dept_id@type", new String[] { "integer" });
    }

    if (mainBean != null) {
      consumptionStatus = (String) mainBean.get("status");
    }

    if (!(consumptionStatus != null
        && (consumptionStatus.equals("X") || consumptionStatus.equals("F")))) {
      pagedList = dao.getStoreStockDetails(paramMap, ConversionUtils.getListingParameter(paramMap),
          consumptionId);
    }

    paramMap.put("pageNum", req.getParameterValues("_consumptionPageNum"));

    if (consumptionId != null) {
      consumedDetailsList = dao.getStockConsumptionDetails(paramMap,
          ConversionUtils.getListingParameter(paramMap), consumptionId);
      storeName = DataBaseUtil.getStringValueFromDb(
          "SELECT dept_name FROM stores WHERE dept_id = ? ",
          Integer.parseInt(req.getParameter("dept_id")));
    }
    req.setAttribute("itemNames",
        StoresDBTablesUtil.getTableDataInJSON(StoresDBTablesUtil.GET_MEDICINE_NAMES_IN_MASTER));
    req.setAttribute("stockConsumptionDetailsList", consumedDetailsList);
    req.setAttribute("pagedList", pagedList);
    req.setAttribute("consumptionMainBean", mainBean);
    req.setAttribute("storeName", storeName);

    return mapping.findForward("addStockConsumption");
  }

  /**
   * Save stock consumptions.
   *
   * @param mapping the mapping
   * @param form    the form
   * @param req     the req
   * @param res     the res
   * @return the action forward
   * @throws ServletException the servlet exception
   * @throws Exception        the exception
   */
  public ActionForward saveStockConsumptions(ActionMapping mapping, ActionForm form,
      HttpServletRequest req, HttpServletResponse res) throws ServletException, Exception {

    String[] recordInsert = req.getParameterValues("_s_is_insert");
    String[] itemBatchid = req.getParameterValues("_s_item_batch_id");
    String[] storeQty = req.getParameterValues("_s_store_qty");
    String[] consumedQty = req.getParameterValues("_s_consumed_qty");
    String[] balanceQty = req.getParameterValues("_s_balance_qty");
    String deptId = req.getParameter("dept_id");
    String userName = (String) req.getSession(false).getAttribute("userid");
    String reqConSumptionId = req.getParameter("_consumption_id");
    reqConSumptionId = (reqConSumptionId == null || reqConSumptionId.isEmpty()) ? null
        : reqConSumptionId;
    String status = "O";
    Timestamp openDate = DataBaseUtil.getDateandTime();
    FlashScope flash = FlashScope.getScope(req);
    boolean success = false;
    Connection con = null;
    String consumptonId = null;
    boolean recordExists = false;
    ActionRedirect redirect = new ActionRedirect(mapping.findForward("showRedirect"));

    BasicDynaBean generalReagentUsageMainBean = maindao.getBean();
    ArrayList<StockConsumptionDetails> generalReagentUsageDetailsList = new ArrayList<>();
    StockConsumptionDetails scd = null;
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      if (reqConSumptionId != null) {
        recordExists = maindao.exist("consumption_id", reqConSumptionId);
      }

      if (recordInsert != null && !recordExists) {
        consumptonId = DataBaseUtil.getNextPatternId("CONSUMPTIONNO");
        generalReagentUsageMainBean.set("consumption_id", consumptonId);
        generalReagentUsageMainBean.set("store_id", Integer.parseInt(deptId));
        generalReagentUsageMainBean.set("user_name", userName);
        generalReagentUsageMainBean.set("status", status);
        generalReagentUsageMainBean.set("open_date", openDate);
        generalReagentUsageMainBean.set("finalized_date", null);
        success = maindao.insert(con, generalReagentUsageMainBean);
      } else {
        consumptonId = reqConSumptionId;
      }

      if (recordInsert != null) {
        for (int i = 0; i < recordInsert.length; i++) {
          if (recordInsert[i] != null && recordInsert[i].equals("Y")) {
            scd = new StockConsumptionDetails();
            int reagentDetId = detailsdao.getNextSequence();
            scd.setConsumptionDetialsId(reagentDetId);
            scd.setConsumptionId(consumptonId);
            scd.setItemBatchid(Integer.parseInt(itemBatchid[i]));
            scd.setStockQty(new BigDecimal(storeQty[i]));
            scd.setQty(new BigDecimal(consumedQty[i]));

            generalReagentUsageDetailsList.add(scd);
          }
        }
      }

      if (generalReagentUsageDetailsList != null && generalReagentUsageDetailsList.size() > 0) {
        success = dao.insertStockConsumptions(con, generalReagentUsageDetailsList);
        success = true;
      }
    } finally {
      DataBaseUtil.commitClose(con, success);
    }
    if (success) {
      redirect.addParameter("dept_id", deptId);
      redirect.addParameter("dept_id@type", req.getParameter("dept_id@type"));
      redirect.addParameter("_consumption_id", consumptonId);
      redirect.addParameter("med_category_id", req.getParameter("med_category_id"));
      redirect.addParameter("med_category_id@type", req.getParameter("med_category_id@type"));
      redirect.addParameter("medicine_name", req.getParameter("medicine_name"));
      redirect.addParameter("medicine_name@op", req.getParameter("medicine_name@op"));
      redirect.addParameter("bin", req.getParameter("bin"));
      redirect.addParameter("bin@op", req.getParameter("bin@op"));
      redirect.addParameter("bin", req.getParameter("service_group_id"));
      redirect.addParameter("bin@op", req.getParameter("service_group_id@type"));
      redirect.addParameter("service_sub_group_id", req.getParameter("service_sub_group_id"));
      redirect.addParameter("service_sub_group_id@type",
          req.getParameter("service_sub_group_id@type"));
      redirect.addParameter("_stockPageNum", req.getParameter("_stockPageNum"));
      redirect.addParameter("_consumptionPageNum", req.getParameter("_consumptionPageNum"));
      redirect.addParameter("sortOrder", req.getParameter("sortOrder"));
      redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
    }
    return redirect;
  }

  /**
   * Update stock consumptions.
   *
   * @param mapping the mapping
   * @param form    the form
   * @param req     the req
   * @param res     the res
   * @return the action forward
   * @throws ServletException the servlet exception
   * @throws Exception        the exception
   */
  public ActionForward updateStockConsumptions(ActionMapping mapping, ActionForm form,
      HttpServletRequest req, HttpServletResponse res) throws ServletException, Exception {
    String deptId = req.getParameter("dept_id");
    String reqConSumptionId = req.getParameter("_consumption_id");
    reqConSumptionId = (reqConSumptionId == null || reqConSumptionId.isEmpty()) ? null
        : reqConSumptionId;
    FlashScope flash = FlashScope.getScope(req);
    boolean success = false;
    Connection con = null;
    ActionRedirect redirect = new ActionRedirect(mapping.findForward("showRedirect"));
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      success = StoreStockConsumptionBO.updateStockConsumptionDetails(con, req);
      success = true;
    } finally {
      DataBaseUtil.commitClose(con, success);
    }
    if (success) {
      redirect.addParameter("dept_id", deptId);
      redirect.addParameter("dept_id@type", req.getParameter("dept_id@type"));
      redirect.addParameter("_consumption_id", reqConSumptionId);
      redirect.addParameter("med_category_id", req.getParameter("med_category_id"));
      redirect.addParameter("med_category_id@type", req.getParameter("med_category_id@type"));
      redirect.addParameter("medicine_name", req.getParameter("medicine_name"));
      redirect.addParameter("medicine_name@op", req.getParameter("medicine_name@op"));
      redirect.addParameter("bin", req.getParameter("bin"));
      redirect.addParameter("bin@op", req.getParameter("bin@op"));
      redirect.addParameter("bin", req.getParameter("service_group_id"));
      redirect.addParameter("bin@op", req.getParameter("service_group_id@type"));
      redirect.addParameter("service_sub_group_id", req.getParameter("service_sub_group_id"));
      redirect.addParameter("service_sub_group_id@type",
          req.getParameter("service_sub_group_id@type"));
      redirect.addParameter("_stockPageNum", req.getParameter("_stockPageNum"));
      redirect.addParameter("_consumptionPageNum", req.getParameter("_consumptionPageNum"));
      redirect.addParameter("sortOrder", req.getParameter("sortOrder"));
      redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
    }

    return redirect;
  }

  /**
   * Cancel consumption transaction.
   *
   * @param mapping the mapping
   * @param form    the form
   * @param req     the req
   * @param res     the res
   * @return the action forward
   * @throws ServletException the servlet exception
   * @throws Exception        the exception
   */
  public ActionForward cancelConsumptionTransaction(ActionMapping mapping, ActionForm form,
      HttpServletRequest req, HttpServletResponse res) throws ServletException, Exception {

    String userName = (String) req.getSession(false).getAttribute("userid");
    String reqConSumptionId = req.getParameter("_consumption_id");
    String deptId = req.getParameter("dept_id");
    reqConSumptionId = (reqConSumptionId == null || reqConSumptionId.isEmpty()) ? null
        : reqConSumptionId;
    ActionRedirect redirect = new ActionRedirect(mapping.findForward("showRedirect"));
    Connection con = null;
    boolean success = false;
    BasicDynaBean updateMainBean = maindao.getBean();
    FlashScope flash = FlashScope.getScope(req);

    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      if (reqConSumptionId != null) {
        Map<String, String> keys = new HashMap<String, String>();
        String key = reqConSumptionId;
        keys.put("consumption_id", key);
        updateMainBean.set("user_name", userName);
        updateMainBean.set("status", "X");
        success = maindao.update(con, updateMainBean.getMap(), keys) > 0;
      }
      success = true;
    } finally {
      DataBaseUtil.commitClose(con, success);
    }

    if (success) {
      redirect.addParameter("dept_id", deptId);
      redirect.addParameter("dept_id@type", req.getParameter("dept_id@type"));
      redirect.addParameter("_consumption_id", reqConSumptionId);
      redirect.addParameter("med_category_id", req.getParameter("med_category_id"));
      redirect.addParameter("med_category_id@type", req.getParameter("med_category_id@type"));
      redirect.addParameter("medicine_name", req.getParameter("medicine_name"));
      redirect.addParameter("medicine_name@op", req.getParameter("medicine_name@op"));
      redirect.addParameter("bin", req.getParameter("bin"));
      redirect.addParameter("bin@op", req.getParameter("bin@op"));
      redirect.addParameter("bin", req.getParameter("service_group_id"));
      redirect.addParameter("bin@op", req.getParameter("service_group_id@type"));
      redirect.addParameter("service_sub_group_id", req.getParameter("service_sub_group_id"));
      redirect.addParameter("service_sub_group_id@type",
          req.getParameter("service_sub_group_id@type"));
      redirect.addParameter("_stockPageNum", req.getParameter("_stockPageNum"));
      redirect.addParameter("_consumptionPageNum", req.getParameter("_consumptionPageNum"));
      redirect.addParameter("sortOrder", req.getParameter("sortOrder"));
      redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
    }
    return redirect;
  }

  /**
   * Finalize consumption details.
   *
   * @param mapping the mapping
   * @param form    the form
   * @param req     the req
   * @param res     the res
   * @return the action forward
   * @throws ServletException the servlet exception
   * @throws Exception        the exception
   */
  public ActionForward finalizeConsumptionDetails(ActionMapping mapping, ActionForm form,
      HttpServletRequest req, HttpServletResponse res) throws ServletException, Exception {
    String deptId = req.getParameter("dept_id");
    String reqConSumptionId = req.getParameter("_consumption_id");
    reqConSumptionId = (reqConSumptionId == null || reqConSumptionId.isEmpty()) ? null
        : reqConSumptionId;
    FlashScope flash = FlashScope.getScope(req);
    boolean success = false;
    Connection con = null;
    String msg = null;
    BasicDynaBean updateMainBean = maindao.getBean();
    String userName = (String) req.getSession(false).getAttribute("userid");
    ActionRedirect redirect = new ActionRedirect(mapping.findForward("showRedirect"));
    List<Map<String, Object>> cacheConsumptionTxns = new ArrayList<>();

    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);

      if (reqConSumptionId != null) {
        Map<String, String> keys = new HashMap<String, String>();
        String key = reqConSumptionId;
        keys.put("consumption_id", key);
        updateMainBean.set("user_name", userName);
        updateMainBean.set("status", "F");
        updateMainBean.set("finalized_date", DataBaseUtil.getDateandTime());
        success = maindao.update(con, updateMainBean.getMap(), keys) > 0;
      }

      if (success) {
        success = StoreStockConsumptionBO.updateStockConsumptionDetails(con, req);
        msg = StoreStockConsumptionBO.finalizeStockConsumptionDetails(con, req,
            cacheConsumptionTxns);
      }

      if (msg != null) {
        success = false;
        flash.put("error", msg);
      } else {
        success = true;
      }
    } finally {
      DataBaseUtil.commitClose(con, success);
      BasicDynaBean module = modules.findByKey("module_id", "mod_scm");
      if (!cacheConsumptionTxns.isEmpty() && module != null && success
          && ((String) module.get("activation_status")).equals("Y")) {
        scmOutService.scheduleStockConsumeTxns(cacheConsumptionTxns);
      }

      // update stock timestamp
      StockFIFODAO stockFIFODAO = new StockFIFODAO();
      stockFIFODAO.updateStockTimeStamp();
      stockFIFODAO.updateStoresStockTimeStamp(Integer.parseInt(deptId));
    }

    redirect.addParameter("dept_id", deptId);
    redirect.addParameter("dept_id@type", req.getParameter("dept_id@type"));
    redirect.addParameter("_consumption_id", reqConSumptionId);
    redirect.addParameter("med_category_id", req.getParameter("med_category_id"));
    redirect.addParameter("med_category_id@type", req.getParameter("med_category_id@type"));
    redirect.addParameter("medicine_name", req.getParameter("medicine_name"));
    redirect.addParameter("medicine_name@op", req.getParameter("medicine_name@op"));
    redirect.addParameter("bin", req.getParameter("bin"));
    redirect.addParameter("bin@op", req.getParameter("bin@op"));
    redirect.addParameter("bin", req.getParameter("service_group_id"));
    redirect.addParameter("bin@op", req.getParameter("service_group_id@type"));
    redirect.addParameter("service_sub_group_id", req.getParameter("service_sub_group_id"));
    redirect.addParameter("service_sub_group_id@type",
        req.getParameter("service_sub_group_id@type"));
    redirect.addParameter("_stockPageNum", req.getParameter("_stockPageNum"));
    redirect.addParameter("_consumptionPageNum", req.getParameter("_consumptionPageNum"));
    redirect.addParameter("sortOrder", req.getParameter("sortOrder"));
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

    return redirect;
  }

}
