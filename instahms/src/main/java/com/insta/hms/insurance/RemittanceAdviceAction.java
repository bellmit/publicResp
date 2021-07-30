package com.insta.hms.insurance;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.insta.hms.billing.ClaimDAO;
import com.insta.hms.billing.ClaimSubmissionDAO;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.master.Accounting.AccountingGroupMasterDAO;
import com.insta.hms.master.Accounting.AccountingPrefsDAO;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.InsuranceCompMaster.InsuCompMasterDAO;
import com.insta.hms.master.InsuranceCompanyTPAMaster.InsuranceCompanyTPAMasterDAO;
import com.insta.hms.master.TpaMaster.TpaMasterDAO;

import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * The Class RemittanceAdviceAction.
 *
 * @author lakshmi.p
 */

public class RemittanceAdviceAction extends BaseAction {

  /** The cdao. */
  private static ClaimDAO cdao = new ClaimDAO();

  /** The submitdao. */
  private static ClaimSubmissionDAO submitdao = new ClaimSubmissionDAO();

  /** The dao. */
  private static RemittanceAdviceDAO dao = new RemittanceAdviceDAO();

  private static final GenericDAO tpaMasterDAO = new GenericDAO("tpa_master");
  private static final GenericDAO tpaCenterMasterDAO = new GenericDAO("tpa_center_master");
  private static final GenericDAO accountGroupAndCenterViewDAO =
      new GenericDAO("accountgrp_and_center_view");
  private static final GenericDAO billDAO = new GenericDAO("bill");

  /**
   * Adds the.
   *
   * @param mapping
   *          the m
   * @param actionForm
   *          the f
   * @param req
   *          the req
   * @param resp
   *          the resp
   * @return the action forward
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws ServletException
   *           the servlet exception
   * @throws Exception
   *           the exception
   */
  @IgnoreConfidentialFilters
  public ActionForward add(ActionMapping mapping, ActionForm actionForm, HttpServletRequest req,
      HttpServletResponse resp) throws IOException, ServletException, Exception {
    JSONSerializer js = new JSONSerializer().exclude("class");

    AccountingGroupMasterDAO accGroupDAO = new AccountingGroupMasterDAO();
    // setting dropdown list for Center/Account Group
    int userCenterId = (Integer) req.getSession(false).getAttribute("centerId");
    List<BasicDynaBean> accGrpAndCenterList = accountGroupAndCenterViewDAO.listAll();
    boolean filterDispFlag = false;
    Map accGrpAndCenterType = null;
    List<BasicDynaBean> accGrpAndCenterDropdn = new ArrayList<BasicDynaBean>();

    if (userCenterId != 0) {
      for (int accGrpCenterIndex = 0; accGrpCenterIndex < accGrpAndCenterList
          .size(); accGrpCenterIndex++) {
        accGrpAndCenterType = new HashMap(
            ((BasicDynaBean) accGrpAndCenterList.get(accGrpCenterIndex)).getMap());
        String type = (String) accGrpAndCenterType.get("type");
        if (type.equals("C")) {
          // filterDisp_flag = true;
          int acId = Integer.parseInt(accGrpAndCenterType.get("ac_id") + "");
          if (acId == userCenterId) {
            accGrpAndCenterDropdn.add((BasicDynaBean) accGrpAndCenterList.get(accGrpCenterIndex));
          }
        } else {
          if (null != accGrpAndCenterType.get("store_center_id")) {
            int storeCenterId = (Integer) accGrpAndCenterType.get("store_center_id");
            if (userCenterId == storeCenterId) {
              accGrpAndCenterDropdn.add((BasicDynaBean) accGrpAndCenterList.get(accGrpCenterIndex));
            }
          } else {
            accGrpAndCenterDropdn.add((BasicDynaBean) accGrpAndCenterList.get(accGrpCenterIndex));
          }
        }
      }
    }

    if (userCenterId == 0) {
      accGrpAndCenterDropdn = accGrpAndCenterList;
    }

    req.setAttribute("tpaJSONList",
        js.serialize(ConversionUtils.copyListDynaBeansToMap(new TpaMasterDAO().listAll())));

    // TODO: create a DAO for insurance company master. dont use GenericDAO()
    req.setAttribute("insCompList", js.serialize(ConversionUtils.listBeanToListMap(
        new InsuCompMasterDAO().listAll(null, "status", "A", "insurance_co_name"))));

    req.setAttribute("insCompTpaList", js.serialize(ConversionUtils
        .listBeanToListMap(new InsuranceCompanyTPAMasterDAO().getCompanyTpaXLList())));

    req.setAttribute("xlTpaList",
        (ConversionUtils.listBeanToListMap(new InsuranceCompanyTPAMasterDAO().getAllXLTpaList())));
    req.setAttribute("xlTpaListJSON", js.serialize(
        (ConversionUtils.listBeanToListMap(new InsuranceCompanyTPAMasterDAO().getAllXLTpaList()))));

    Map xlTpaKeyMap = new HashMap();
    xlTpaKeyMap.put("status", "A");
    xlTpaKeyMap.put("claim_format", "XL");

    req.setAttribute("tpaList", js.serialize(
        ConversionUtils.listBeanToListMap(tpaMasterDAO.listAll(null, xlTpaKeyMap, "tpa_name"))));

    req.setAttribute("tpaCenterList", js.serialize(ConversionUtils
        .listBeanToListMap(tpaCenterMasterDAO.listAll(null, xlTpaKeyMap, "tpa_center_id"))));
    AccountingPrefsDAO acPrefsDAO = new AccountingPrefsDAO();
    req.setAttribute("acc_prefs", acPrefsDAO.getRecord());
    // TODO : rename the dao method as getStoreWiseAccountGroups()
    // TODO : change method to have a boolean to say whether default center should be included
    // or not.

    // List<BasicDynaBean> accGrpAndCenterList = new
    // GenericDAO("accountgrp_and_center_view").listAll();
    req.setAttribute("accountGrpAndCenterList", accGrpAndCenterDropdn);
    req.setAttribute("remittanceLists", js.serialize(null));
    return mapping.findForward("addshow");
  }

  /**
   * Show.
   *
   * @param mapping
   *          the m
   * @param actionForm
   *          the f
   * @param req
   *          the req
   * @param resp
   *          the resp
   * @return the action forward
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws ServletException
   *           the servlet exception
   * @throws Exception
   *           the exception
   */
  @IgnoreConfidentialFilters
  public ActionForward show(ActionMapping mapping, ActionForm actionForm, HttpServletRequest req,
      HttpServletResponse resp) throws IOException, ServletException, Exception {
    JSONSerializer js = new JSONSerializer().exclude("class");
    AccountingPrefsDAO acPrefsDAO = new AccountingPrefsDAO();
    AccountingGroupMasterDAO accGroupDAO = new AccountingGroupMasterDAO();

    RemittanceAdviceDAO dao = new RemittanceAdviceDAO();
    BasicDynaBean bean = dao.findByKey("remittance_id",
        Integer.parseInt(req.getParameter("remittance_id")));
    String tpaId = (String) bean.get("tpa_id");
    String claimFormat = (String) new TpaMasterDAO().findByKey("tpa_id", tpaId).get("claim_format");
    req.setAttribute("claimFormat", claimFormat);
    req.setAttribute("bean", bean);

    req.setAttribute("insCompList", js.serialize(ConversionUtils.listBeanToListMap(
        new InsuCompMasterDAO().listAll(null, "status", "A", "insurance_co_name"))));

    req.setAttribute("insCompTpaList", js.serialize(
        ConversionUtils.listBeanToListMap(new InsuranceCompanyTPAMasterDAO().getCompanyTpaList())));

    req.setAttribute("tpaList", js.serialize(
        ConversionUtils.listBeanToListMap(tpaMasterDAO.listAll(null, "status", "A", "tpa_name"))));

    req.setAttribute("tpaCenterList", js.serialize(
        ConversionUtils.listBeanToListMap(tpaCenterMasterDAO.listAll(null, "status", "A"))));

    req.setAttribute("acc_prefs", acPrefsDAO.getRecord());

    // setting dropdown list for Center/Account Group
    int userCenterId = (Integer) req.getSession(false).getAttribute("centerId");
    List<BasicDynaBean> accGrpAndCenterList = accountGroupAndCenterViewDAO.listAll();
    boolean filterDispFlag = false;
    Map accGrpAndCenterType = null;
    List<BasicDynaBean> accGrpAndCenterDropdn = new ArrayList<BasicDynaBean>();

    if (userCenterId != 0) {
      for (int accGrpCenterIndex = 0; accGrpCenterIndex < accGrpAndCenterList
          .size(); accGrpCenterIndex++) {
        accGrpAndCenterType = new HashMap(
            ((BasicDynaBean) accGrpAndCenterList.get(accGrpCenterIndex)).getMap());
        String type = (String) accGrpAndCenterType.get("type");
        if (type.equals("C")) {
          // filterDisp_flag = true;
          int acId = Integer.parseInt(accGrpAndCenterType.get("ac_id") + "");
          if (acId == userCenterId) {
            accGrpAndCenterDropdn.add((BasicDynaBean) accGrpAndCenterList.get(accGrpCenterIndex));
          }
        } else {
          if (null != accGrpAndCenterType.get("store_center_id")) {
            int storeCenterId = (Integer) accGrpAndCenterType.get("store_center_id");
            if (userCenterId == storeCenterId) {
              accGrpAndCenterDropdn.add((BasicDynaBean) accGrpAndCenterList.get(accGrpCenterIndex));
            }
          } else {
            accGrpAndCenterDropdn.add((BasicDynaBean) accGrpAndCenterList.get(accGrpCenterIndex));
          }
        }
      }
    }

    if (userCenterId == 0) {
      accGrpAndCenterDropdn = accGrpAndCenterList;
    }

    req.setAttribute("accountGrpAndCenterList", accGrpAndCenterDropdn);

    req.setAttribute("remittanceLists", js.serialize(bean.getMap()));
    return mapping.findForward("addshow");
  }

  /**
   * Delete.
   *
   * @param mapping
   *          the m
   * @param actionForm
   *          the f
   * @param req
   *          the req
   * @param resp
   *          the resp
   * @return the action forward
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws ServletException
   *           the servlet exception
   * @throws Exception
   *           the exception
   */
  @IgnoreConfidentialFilters
  public ActionForward delete(ActionMapping mapping, ActionForm actionForm, HttpServletRequest req,
      HttpServletResponse resp) throws IOException, ServletException, Exception {

    RemittanceAdviceDAO dao = new RemittanceAdviceDAO();
    BasicDynaBean bean = dao.findByKey("remittance_id",
        Integer.parseInt(req.getParameter("remittance_id")));

    if (bean == null) {
      ActionRedirect redirect = new ActionRedirect(mapping.findForward("listRedirect"));
      FlashScope flash = FlashScope.getScope(req);
      redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
      flash.error("Remittance File with " + req.getParameter("remittance_id")
          + " is invalid (or) deleted. ");
      return redirect;
    }

    String tpaId = (String) bean.get("tpa_id");
    String claimFormat = (String) new TpaMasterDAO().findByKey("tpa_id", tpaId).get("claim_format");
    req.setAttribute("claimFormat", claimFormat);

    req.setAttribute("bean", bean);
    JSONSerializer js = new JSONSerializer().exclude("class");
    req.setAttribute("insCompList", js.serialize(ConversionUtils.listBeanToListMap(
        new InsuCompMasterDAO().listAll(null, "status", "A", "insurance_co_name"))));

    req.setAttribute("insCompTpaList", js.serialize(
        ConversionUtils.listBeanToListMap(new InsuranceCompanyTPAMasterDAO().getCompanyTpaList())));

    // TODO : create a DAO for TPA master, dont use GenericDAO()
    req.setAttribute("tpaList", js.serialize(
        ConversionUtils.listBeanToListMap(tpaMasterDAO.listAll(null, "status", "A", "tpa_name"))));
    req.setAttribute("remittanceLists", js.serialize(bean.getMap()));
    return mapping.findForward("addshow");
  }

  /**
   * Delete remittance.
   *
   * @param mapping
   *          the m
   * @param actionForm
   *          the f
   * @param req
   *          the req
   * @param resp
   *          the resp
   * @return the action forward
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws ServletException
   *           the servlet exception
   * @throws Exception
   *           the exception
   */
  @IgnoreConfidentialFilters
  public ActionForward deleteRemittance(ActionMapping mapping, ActionForm actionForm,
      HttpServletRequest req, HttpServletResponse resp)
      throws IOException, ServletException, Exception {

    FlashScope flash = FlashScope.getScope(req);
    ActionRedirect redirect = new ActionRedirect(mapping.findForward("deleteRedirect"));
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
    Boolean success = true;
    String msg = "";

    String remittanceIdStr = req.getParameter("remittance_id");

    int remittanceId = (remittanceIdStr != null && !remittanceIdStr.equals(""))
        ? Integer.parseInt(remittanceIdStr)
        : 0;

    if (remittanceId == 0) {
      flash.error("Invalid remittance file. This remittance file does not exists.");
      redirect.addParameter("remittance_id", remittanceId);
      return setRedirectParams(redirect, req);
    }

    BasicDynaBean remittanceBean = dao.findByKey("remittance_id", remittanceId);

    // Need to check the payers reference ids updation after deleteion since the claim payer id
    // will be empty after deletion
    // Need action rights for deleting the remittance file ?

    HttpSession session = req.getSession(false);
    String userid = (String) session.getAttribute("userid");

    List<String> billsToUpdtList = new ArrayList<String>();
    List<String> claimsToUpdList = new ArrayList<String>();
    Connection con = null;

    try {
      do {
        con = DataBaseUtil.getConnection();
        con.setAutoCommit(false);

        Map map = dao.deleteRemittance(con, remittanceId, userid);

        if (map.get("error") != null) {
          flash.error((String) map.get("error"));
          success = false;
          break;
        } else {
          billsToUpdtList = (List) map.get("bills");
          claimsToUpdList = (List) map.get("claims");
        }

        // Check bill status while deleting the remittance. Bill should not be Open.
        // Finalized bills are updated. If bill is open show a warning message.
        if (billsToUpdtList != null && billsToUpdtList.size() > 0) {

          int openBillsCount = 0;
          String openBillsStr = "";

          for (String billNo : billsToUpdtList) {
            BasicDynaBean bill = billDAO.findByKey("bill_no", billNo);
            if (bill.get("status").equals("A")) {
              openBillsCount++;
              openBillsStr = billNo + ", " + openBillsStr;
            }
          }

          if (openBillsCount > 0) {
            flash.warning("The deleted remittance contains some bills which are Open. <br/>"
                + " Please check the Open bills : <br/>" + openBillsStr);
          }
          if (null == claimsToUpdList || claimsToUpdList.size() == 0) {
            claimsToUpdList = dao.getAllClaimsForBills(billsToUpdtList);
          }
        }

        success = success && dao.delete(con, "remittance_id", remittanceId);

      } while (false);
    } finally {
      DataBaseUtil.commitClose(con, success);
    }

    if (success) {
      redirect = new ActionRedirect(mapping.findForward("listRedirect"));
      redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
      msg += "<br /> " + claimsToUpdList.size() + " claim(s) and " + billsToUpdtList != null
          ? billsToUpdtList.size()
          : 0 + " bills were successfully processed ... ";
      msg = "Remittance deleted successfully...<br /> " + msg;
      flash.info(msg);
      return redirect;
    } else {
      redirect = new ActionRedirect(mapping.findForward("deleteRedirect"));
      redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
      redirect.addParameter("remittance_id", remittanceId);
      flash.put("bean", remittanceBean);
      flash.info(msg);
      return redirect;
    }
  }

  /**
   * Validate and process bill level remittance from excel sheet.
   *
   * @param req
   *          the req
   * @param bean
   *          the bean
   * @param remittanceForm
   *          the r form
   * @return the map
   * @throws Exception
   *           the exception
   */
  private Map validateAndProcessBillLevelRemittanceFromExcelSheet(HttpServletRequest req,
      BasicDynaBean bean, RemittanceForm remittanceForm) throws Exception {

    selectedValues = new HashMap();

    String centerOrAccountGroup = req.getParameter("center_or_account_group");

    String accGrpOrCenterIdStr = (centerOrAccountGroup == null || centerOrAccountGroup.equals(""))
        ? ""
        : (centerOrAccountGroup.toString());
    String accGrpIdStr = null;
    String centerIdStr = null;
    int accGrpId = 0;
    int centerId = 0;
    if (!accGrpOrCenterIdStr.equals("")) {
      if (accGrpOrCenterIdStr.startsWith("A")) {
        accGrpIdStr = accGrpOrCenterIdStr.substring(1, accGrpOrCenterIdStr.length());
        accGrpId = accGrpIdStr != null && !accGrpIdStr.trim().equals("") ? new Integer(accGrpIdStr)
            : 0;
        BasicDynaBean accbean = (BasicDynaBean) new AccountingGroupMasterDAO()
            .findByKey("account_group_id", accGrpId);
        String accountGrpName = accbean != null ? (String) accbean.get("account_group_name") : "";
        selectedValues.put("account_group", accGrpIdStr);
        selectedValues.put("account_group_name", accountGrpName);
      } else if (accGrpOrCenterIdStr.startsWith("C")) {
        centerIdStr = accGrpOrCenterIdStr.substring(1, accGrpOrCenterIdStr.length());
        centerId = centerIdStr != null && !centerIdStr.trim().equals("") ? new Integer(centerIdStr)
            : 0;
        BasicDynaBean centerbean = (BasicDynaBean) new CenterMasterDAO().findByKey("center_id",
            centerId);
        String centerName = centerbean != null ? (String) centerbean.get("center_name") : "";
        selectedValues.put("center_id", centerIdStr);
        selectedValues.put("center_name", centerName);
      }
    }

    selectedValues.put("tpa_id", req.getParameter("tpa_id"));

    selectedValues.put("item_identification", "B");
    selectedValues.put("bill_no_heading", req.getParameter("bill_no_heading"));

    selectedValues.put("payment_ref_type", req.getParameter("payment_ref_type"));

    if (req.getParameter("payment_ref_type").equals("PerItem")) {
      selectedValues.put("payment_reference_heading",
          req.getParameter("payment_reference_heading"));
    } else {
      selectedValues.put("payment_reference", req.getParameter("payment_reference"));
    }

    selectedValues.put("amount_heading", req.getParameter("amount_heading"));
    selectedValues.put("denial_remarks_heading", req.getParameter("denial_remarks_heading"));
    selectedValues.put("payer_id_heading", req.getParameter("payer_id_heading"));
    selectedValues.put("worksheet_index", req.getParameter("worksheet_index"));
    selectedValues.put("detail_level", req.getParameter("detail_level"));

    int selectedSheetNo = new Integer(req.getParameter("worksheet_index"));

    selectedSheetNo -= 1;

    RemittanceSpreadsheetProvider remittanceSpreadsheetProvider = 
        new RemittanceSpreadsheetProvider();

    HSSFWorkbook workBook = new HSSFWorkbook(
        remittanceForm.getRemittance_metadata().getInputStream());
    Map descMap = remittanceSpreadsheetProvider.getReportDescFromSheetBillLevel(selectedValues,
        workBook, selectedSheetNo);

    if (descMap.get("error") != null
        && ((String) descMap.get("error")).startsWith("Column Not Found")) {
      String invalidColumn = ((String) descMap.get("error")).split(":")[1].trim();

      String error = "Required columns are not found (or) incorrect column names.<br/>"
          + "Please check the Excel file <b>"
          + remittanceForm.getRemittance_metadata().getFileName() + "</b> sheet number : <b>"
          + (selectedSheetNo + 1) + "</b> .<br/>" + "Column Not Found  "
          + req.getParameter(invalidColumn + "_lbl") + " : <b> " + req.getParameter(invalidColumn)
          + "</b>";
      descMap.put("error", error);
    }

    return descMap;
  }

  /**
   * Remittance payment allocation and bill, claim status updation (Item wise & Bill wise).
   *
   * @param mapping
   *          the m
   * @param actionForm
   *          the form
   * @param req
   *          the req
   * @param res
   *          the res
   * @return the action forward
   * @throws Exception
   *           the exception
   */
  @IgnoreConfidentialFilters
  public ActionForward create(ActionMapping mapping, ActionForm actionForm, HttpServletRequest req,
      HttpServletResponse res) throws Exception {
    RemittanceForm remittanceForm = (RemittanceForm) actionForm;
    FlashScope flash = FlashScope.getScope(req);
    ActionRedirect redirect = new ActionRedirect(mapping.findForward("addRedirect"));
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
    Boolean success = true;
    String msg = "";

    String path = req.getContextPath();
    HttpSession session = req.getSession(false);
    String userid = (String) session.getAttribute("userid");
    String remittanceId = req.getParameter("remittance_id");
    String action = req.getParameter("actionBtn");
    String claimFormat = "XML";
    String tpaId = req.getParameter("tpa_id");
    String cenGrpId = req.getParameter("center_or_account_group");
    String detaillevel = req.getParameter("detail_level");
    String fileName = null;
    Map<String, String> columnMap = new HashMap<String, String>();

    BasicDynaBean bean = dao.getBean();

    setRemittancedetailstoBean(req, bean, action, tpaId, remittanceId, remittanceForm, columnMap);

    if (null != tpaId) {
      // compare with first character 'C' in center
      // id.
      if (cenGrpId.substring(0, 1).equals("C")) {
        String cenId = cenGrpId.substring(1, cenGrpId.length());
        int cenid = Integer.parseInt(cenId);
        claimFormat = TpaMasterDAO.getClaimformat(tpaId, cenid);
        if (null == claimFormat) {
          claimFormat = (String) new TpaMasterDAO().findByKey("tpa_id", tpaId).get("claim_format");
        }
      } else {
        claimFormat = (String) new TpaMasterDAO().findByKey("tpa_id", tpaId).get("claim_format");
      }

    }
    if (action == null || !action.equals("update")) {
      fileName = (null != remittanceForm.getRemittance_metadata())
          ? remittanceForm.getRemittance_metadata().getFileName()
          : "";
      if (!validateFileFormat(flash, bean, claimFormat, fileName)) {
        return setRedirectParams(redirect, req);
      }
    }

    BasicDynaBean exists = dao.findByKey("file_name", bean.get("file_name"));
    Connection con = null;

    ArrayList<RemittanceAdviceClaim> claims = new ArrayList<RemittanceAdviceClaim>();
    Set billsSet = new HashSet();
    List<String> billsToUpdtList = new ArrayList<String>();
    List<String> claimsToUpdList = new ArrayList<String>();

    /*
     * If Remittance upload file does not exists, insert all activity related payment details.
     */

    // updating remittance and bill charge for remittance
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);

      success = updateRemittanceDetails(exists, con, bean, req, detaillevel, billsToUpdtList,
          claimsToUpdList, claims, flash, columnMap, claimFormat, fileName, remittanceForm, action,
          path);
    } finally {
      DataBaseUtil.commitClose(con, success);
    }

    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      if (success) {
        Map map = updateBillAndClaimStatus(con, billsToUpdtList, flash, claimsToUpdList, billsSet,
            userid, bean);
        msg += map.get("msg");
        success = map.get("success").equals("true");
      }
    } finally {
      DataBaseUtil.commitClose(con, success);
    }

    if (success) {
      String errormsg = setUpdatedBillsAndClaimsCount(detaillevel, action, billsToUpdtList,
          claimsToUpdList, claims);
      msg += errormsg;
      redirect = new ActionRedirect(mapping.findForward("showRedirect"));
      redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
      redirect.addParameter("remittance_id", bean.get("remittance_id"));
      msg = "Remittance Advice update successful...<br /> " + msg;
      flash.info(msg);
      return redirect;
    } else {
      redirect.addParameter("remittance_id", bean.get("remittance_id"));
      flash.put("bean", bean);
      return setRedirectParams(redirect, req);
    }
  }

  /**
   * Sets the updated bills and claims count.
   *
   * @param detailLevel
   *          the detail level
   * @param action
   *          the action
   * @param billsToUpdtList
   *          the bills to updt list
   * @param claimsToUpdList
   *          the claims to upd list
   * @param claims
   *          the claims
   * @return the string
   * @throws SQLException
   *           the SQL exception
   * @throws Exception
   *           the exception
   */
  private String setUpdatedBillsAndClaimsCount(String detailLevel, String action,
      List<String> billsToUpdtList, List<String> claimsToUpdList,
      ArrayList<RemittanceAdviceClaim> claims) throws SQLException, Exception {
    String msg = "";
    if (!detailLevel.equals("B")) {
      if (action != null && action.equals("update")) {
        int claimsCount = 0;
        int billsCount = 0;
        if (claimsToUpdList != null) {
          for (int claimToUpdIndex = 0; claimToUpdIndex < claimsToUpdList
              .size(); claimToUpdIndex++) {
            BasicDynaBean claim = cdao.getClaimById(claimsToUpdList.get(claimToUpdIndex));
            if (!claim.get("status").equals("C")) {
              claimsCount += claimsCount;
            }
          }
        }
        if (billsToUpdtList != null) {
          for (int claimsToUpdtIndex = 0; claimsToUpdtIndex < billsToUpdtList
              .size(); claimsToUpdtIndex++) {
            BasicDynaBean bill = billDAO.findByKey("bill_no",
                (billsToUpdtList.get(claimsToUpdtIndex)));
            if (!bill.get("status").equals("C")) {
              billsCount += billsCount;
            }
          }
        }
        int claimsToUpdListSize = claimsToUpdList != null ? claimsToUpdList.size() : 0;
        int billToUpdListSize = billsToUpdtList != null ? billsToUpdtList.size() : 0;
        msg += "<br /> " + claimsToUpdListSize + " claim(s) and";
        msg += " " + billToUpdListSize + " bill(s) were updated... ";

        // If no bills update errors
        if (msg.equals("")) {
          msg += "<br /> " + claimsCount + " claim(s) are still not closed and ";
          msg += " " + billsCount + " bill(s) are still not closed... ";
        }

      } else {
        msg += "<br /> " + claims.size() + " claim(s) and";
        int activitiesTotal = 0;
        for (int claimIndex = 0; claimIndex < claims.size(); claimIndex++) {
          activitiesTotal += ((RemittanceAdviceClaim) claims.get(claimIndex)).getActivities()
              .size();
        }
        msg += " " + activitiesTotal
            + " activities (or charges) were successfully processed and updated... ";
      }
    } else {
      msg += " " + billsToUpdtList.size() + " bills/items were successfully processed ... ";
    }
    return msg;
  }

  /**
   * Update bill and claim status.
   *
   * @param con
   *          the con
   * @param billsToUpdtList
   *          the bills to updt list
   * @param flash
   *          the flash
   * @param claimsToUpdList
   *          the claims to upd list
   * @param billsSet
   *          the bills set
   * @param userid
   *          the userid
   * @param bean
   *          the bean
   * @return the map
   * @throws SQLException
   *           the SQL exception
   * @throws Exception
   *           the exception
   */
  private Map updateBillAndClaimStatus(Connection con, List<String> billsToUpdtList,
      FlashScope flash, List<String> claimsToUpdList, Set billsSet, String userid,
      BasicDynaBean bean) throws SQLException, Exception {
    Map map = new HashMap<String, String>();
    boolean success = true;
    String msg = "";
    do {
      if (billsToUpdtList != null && billsToUpdtList.size() == 0) {
        flash.error("Remittance has no activities. Please check the file." + bean.get("file_name"));
        success = false;
        break;
      }

      /*
       * Get claim bills which are not part of claim XML, as bills which are finalized has Zero
       * sponsor due.
       */
      if (null == claimsToUpdList || claimsToUpdList.size() == 0) {
        // resort to the
        // original logic
        if (claimsToUpdList == null) {
          claimsToUpdList = new ArrayList<>();
        }
        claimsToUpdList.addAll(dao.getAllClaimsForBills(billsToUpdtList));
      }

      for (String claimId : claimsToUpdList) {
        if (claimId != null && !claimId.equals("")) {
          List<BasicDynaBean> zeroClaimBills = submitdao.findAllBills(claimId);
          billsSet.addAll((ConversionUtils.listBeanToMapBean(zeroClaimBills, "bill_no")).keySet());
        }
      }

      billsSet.addAll(billsToUpdtList);
      List<String> billList = new ArrayList<String>();
      billList.addAll(billsSet);

      /* Update bills status */
      String error = dao.updateAllBillsStatus(con, billList, userid);
      if (error != null) {
        msg += " <br /> " + error;
      }

      /* Update claims status */
      error = dao.updateAllClaimsStatus(con, claimsToUpdList, userid);
      if (error != null && !error.equals("")) {
        msg += " <br /> " + error;
        flash.error(error);
        success = false;
        break;
      }
    } while (false);
    map.put("msg", msg);
    map.put("success", success ? "true" : "false");
    return map;
  }

  /**
   * Update remittance details.
   *
   * @param exists
   *          the exists
   * @param con
   *          the con
   * @param bean
   *          the bean
   * @param req
   *          the req
   * @param detailLevel
   *          the detail level
   * @param billsToUpdtList
   *          the bills to updt list
   * @param claimsToUpdList
   *          the claims to upd list
   * @param claims
   *          the claims
   * @param flash
   *          the flash
   * @param columnMap
   *          the column map
   * @param claimFormat
   *          the claim format
   * @param fileName
   *          the file name
   * @param remittanceForm
   *          the r form
   * @param action
   *          the action
   * @param path
   *          the path
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   * @throws Exception
   *           the exception
   */
  private boolean updateRemittanceDetails(BasicDynaBean exists, Connection con, BasicDynaBean bean,
      HttpServletRequest req, String detailLevel, List<String> billsToUpdtList,
      List<String> claimsToUpdList, ArrayList<RemittanceAdviceClaim> claims, FlashScope flash,
      Map columnMap, String claimFormat, String fileName, RemittanceForm remittanceForm,
      String action, String path) throws SQLException, Exception {
    List<String> billsList = new ArrayList<String>();
    List<String> claimsList = new ArrayList<String>();
    boolean success = true;
    do {
      if (exists == null) {
        bean.set("remittance_id", dao.getNextSequence());

        Map descMap = new HashMap();
        Map errorMap = new HashMap();

        String errorMsg = null;
        RemittanceAdvice desc = null;
        /* Fix for update remittance bug */
        // setColumnMapValues(columnMap,req);

        String workSheetIndexStr = req.getParameter("worksheet_index");
        int workSheetIndex = null != workSheetIndexStr && !workSheetIndexStr.equals("")
            ? Integer.parseInt(workSheetIndexStr)
            : 1;
        // Validate and Process the remittance file
        if (claimFormat.equals("XML")) {
          desc = new XMLItemRemittanceProcessor().process(bean, remittanceForm, errorMap);
          // descMap = validateAndProcessItemLevelRemittanceFromXML(bean, rForm);
        } else {
          /* Fix for update remittance bug */
          setColumnMapValues(columnMap, req);
          if (detailLevel.equals("I")) {
            desc = new XLItemRemittanceProcessor(columnMap).process(bean, remittanceForm,
                workSheetIndex, columnMap, errorMap);
            // descMap = validateAndProcessItemLevelRemittanceFromExcelSheet(req, bean, rForm);

          } else if (detailLevel.equals("B")) {
            desc = new XLBillRemittanceProcessor(columnMap).process(bean, remittanceForm,
                workSheetIndex, columnMap, errorMap);
            // descMap = validateAndProcessBillLevelRemittanceFromExcelSheet(req, bean, rForm);
          }

          if (null != errorMap && null != errorMap.get("error")
              && !errorMap.get("error").equals("")) {
            descMap.put("error", errorMap.get("error"));
          }
          if (null != desc) {
            descMap.put("RemittanceDesc", desc);
            dao.updateClaimActivityIDsForXLUpload(desc.getClaim());
          }
        }

        // Check if any errors and Get the RemittanceAdvice object
        if (errorMap.get("error") != null && !errorMap.get("error").equals("")) {
          errorMsg = (String) errorMap.get("error");
        } else {
          // desc = descMap.get("RemittanceDesc") != null ?
          // (RemittanceAdvice)descMap.get("RemittanceDesc") : null;

          String msg = "";
          if (errorMap != null) {
            msg = (String) errorMap.get("error") == null ? "" : (String) errorMap.get("error");
          }
          errorMsg = (desc == null) ? "Invalid File: Incorrectly formatted values supplied" : msg;
        }

        if (!errorMsg.equals("")) {
          flash.error(errorMsg);
          flash.put("bean", bean);
          success = false;
          return success;
        }
        if (success) {
          success = dao.insert(con, bean);
        }

        if (!success) {
          flash.error("Error while adding remittance details...");
          success = false;
          return success;
        }
        if (null != desc) {
          claims.addAll(desc.getClaim());
        }

        if (claims == null || claims.size() == 0) {
          flash.error("Remittance has no claims. Please check the file." + bean.get("file_name"));
          return success;
        }

        if (!detailLevel.equals("B")) {
          Map map = dao.updateChargesForRemittanceAdvice(con, claims, bean);
          if (map.get("error") != null) {
            flash.error((String) map.get("error"));
            success = false;
            return success;
          } else {
            billsList = (List) map.get("bills");
            claimsList = (List) map.get("claims");
          }
        } else {
          Map map = dao.updateBillsForRemittanceAdvice(con, claims, bean);
          if (map.get("error") != null) {
            flash.error((String) map.get("error"));
            success = false;
            return success;
          } else {
            billsList = (List) map.get("bills");
          }
        }
      } else {
        detailLevel = (String) bean.get("detail_level");
        if (action == null || !action.equals("update")) {
          flash.error("This file already exists..." + fileName);
          success = false;
          return success;

        } else if (success && !detailLevel.equals("B")) {
          /* If remitance file exists update the bill and claim status only. */
          Integer remittanceId = (Integer) bean.get("remittance_id");
          fileName = (String) bean.get("file_name");
          List<BasicDynaBean> chargeIds = dao.getAllChargeIds(con, remittanceId);
          billsList = dao.getAllBillsForCharges(con, chargeIds);
          List<BasicDynaBean> claimIds = dao.getAllClaimsIds(con, remittanceId);
          for (BasicDynaBean cb : claimIds) {
            claimsList.add((String) cb.get("claim_id"));
          }
        }
      }

      // Check bill status while saving the remittance. Bill should not be Open / Closed
      // Finalized bills are updated. If bill is closed show a warning message.
      // Payment status is ignored (not validated)
      if (billsList != null && billsList.size() > 0) {

        int openBillsCount = 0;
        int closedBillsCount = 0;
        String openBillsStr = "";
        String closedBillsStr = "";

        for (String billNo : billsList) {
          BasicDynaBean bill = billDAO.findByKey("bill_no", billNo);
          if (bill.get("status").equals("A")) {
            openBillsCount++;
            openBillsStr = submitdao.urlString(path, "bill", billNo, null) + ", " + openBillsStr;
          }
          if (bill.get("status").equals("C")) {
            closedBillsCount++;
            closedBillsStr = submitdao.urlString(path, "bill", billNo, null) + ", "
                + closedBillsStr;
          }
        }

        if (openBillsCount > 0) {
          flash.error(
              ((exists == null) ? ("The uploaded Remittance file : <b> " + fileName + " </b> <br/>")
                  : ("This Remittance ")) + " contains some <b> Open </b> bills. <br/>"
                  + " Please finalize the Open bills and upload the file again : <br/> <b>"
                  + openBillsStr + "</b>");
          success = false;
          return success;
        }
        if (closedBillsCount > 0) {
          flash.warning("Updated claim amount for some closed bills.<br/> "
              + " Remittance file : <b> " + fileName
              + " </b> <br/> contains some <b> Closed </b> bills : <b>" + closedBillsStr + "</b>");
        }
      }
      billsToUpdtList.addAll(billsList);
      claimsToUpdList.addAll(claimsList);
    } while (false);
    return success;
  }

  /**
   * Sets the column map values.
   *
   * @param columnMap
   *          the column map
   * @param req
   *          the req
   */
  private void setColumnMapValues(Map<String, String> columnMap, HttpServletRequest req) {

    columnMap.put("tpa_id", req.getParameter("tpa_id"));

    columnMap.put("item_identification", req.getParameter("item_identification"));

    if (null != req.getParameter("item_identification")
        && req.getParameter("item_identification").equals("ActivityId")) {
      columnMap.put("item_id_heading", req.getParameter("item_id_heading"));
    } else {
      columnMap.put("bill_no_heading", req.getParameter("bill_no_heading"));
      columnMap.put("service_name_heading", req.getParameter("service_name_heading"));
      columnMap.put("service_posted_date_heading", req.getParameter("service_posted_date_heading"));
      columnMap.put("charge_insurance_claim_amount_heading",
          req.getParameter("charge_insurance_claim_amount_heading"));
    }

    columnMap.put("payment_ref_type", req.getParameter("payment_ref_type"));

    if (req.getParameter("payment_ref_type").equals("PerItem")) {
      columnMap.put("payment_reference_heading", req.getParameter("payment_reference_heading"));
    } else {
      columnMap.put("payment_reference", req.getParameter("payment_reference"));
    }

    columnMap.put("amount_heading", req.getParameter("amount_heading"));
    columnMap.put("denial_remarks_heading", req.getParameter("denial_remarks_heading"));
    columnMap.put("payer_id_heading", req.getParameter("payer_id_heading"));
    columnMap.put("worksheet_index", req.getParameter("worksheet_index"));
    columnMap.put("detail_level", req.getParameter("detail_level"));
    columnMap.put("claim_id_heading", "Claim ID");

  }

  /**
   * Validate file format.
   *
   * @param flash
   *          the flash
   * @param bean
   *          the bean
   * @param claimFormat
   *          the claim format
   * @param fileName
   *          the file name
   * @return true, if successful
   */
  private boolean validateFileFormat(FlashScope flash, BasicDynaBean bean, String claimFormat,
      String fileName) {
    String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
    extension = extension.toLowerCase();

    if (claimFormat.equals("XML")) {
      if (!extension.startsWith("xm")) {
        if (extension.startsWith("xl")) {
          flash.error("Upload file : " + fileName
              + ". Excel file based Remittance is not allowed when TPA claim format is XML.");
          flash.put("bean", bean);
          return false;
        } else {
          flash.error("Upload file : " + fileName
              + " is an invalid file format. Please upload valid XML file.");
          flash.put("bean", bean);
          return false;
        }
      }
    } else {
      if (!extension.startsWith("xl")) {
        if (extension.startsWith("xm")) {
          flash.error("Upload file : " + fileName
              + ". XML file based Remittance is not allowed when TPA claim format is XL.");
          flash.put("bean", bean);
          return false;
        } else {
          flash.error("Upload file : " + fileName
              + " is an invalid file format. Please upload valid Excel file.");
          flash.put("bean", bean);
          return false;
        }
      }
    }
    return true;
  }

  /**
   * Sets the remittancedetailsto bean.
   *
   * @param req
   *          the req
   * @param bean
   *          the bean
   * @param action
   *          the action
   * @param tpaId
   *          the tpa id
   * @param remittanceId
   *          the remittance id
   * @param remittanceForm
   *          the r form
   * @param columnMap
   *          the column map
   * @throws SQLException
   *           the SQL exception
   * @throws Exception
   *           the exception
   */
  private void setRemittancedetailstoBean(HttpServletRequest req, BasicDynaBean bean, String action,
      String tpaId, String remittanceId, RemittanceForm remittanceForm, Map columnMap)
      throws SQLException, Exception {
    if (action != null && action.equals("update") && remittanceId != null
        && !remittanceId.equals("")) {
      bean = dao.findByKey("remittance_id", Integer.parseInt(remittanceId));
    } else {
      bean.set("received_date",
          req.getParameter("received_date") == null || req.getParameter("received_date").equals("")
              ? null
              : DateUtil.parseDate(req.getParameter("received_date")));
      bean.set("insurance_co_id", req.getParameter("insurance_co_id"));
      bean.set("tpa_id", req.getParameter("tpa_id"));
      bean.set("file_name", remittanceForm.getRemittance_metadata().getFileName());
      bean.set("detail_level", req.getParameter("detail_level"));
      bean.set("reference_no", req.getParameter("reference_no"));
      bean.set("is_recovery",
          req.getParameter("is_recovery") != null ? req.getParameter("is_recovery") : "N");

      String centerOrAccountGroup = req.getParameter("center_or_account_group");

      String accGrpOrCenterIdStr = (centerOrAccountGroup == null || centerOrAccountGroup.equals(""))
          ? ""
          : (centerOrAccountGroup.toString());
      int accGrpId = 0;
      int centerId = 0;
      if (!accGrpOrCenterIdStr.equals("")) {
        if (accGrpOrCenterIdStr.startsWith("A")) {
          accGrpId = new Integer(accGrpOrCenterIdStr.substring(1, accGrpOrCenterIdStr.length()));
          setAccountingGroupDetails(accGrpId, columnMap);
        } else if (accGrpOrCenterIdStr.startsWith("C")) {
          centerId = new Integer(accGrpOrCenterIdStr.substring(1, accGrpOrCenterIdStr.length()));
          setCenterDetails(centerId, columnMap);
        }
      }
      bean.set("account_group", accGrpId);
      bean.set("center_id", centerId);
    }

  }

  /**
   * Sets the redirect params.
   *
   * @param redirect
   *          the redirect
   * @param req
   *          the req
   * @return the action forward
   */
  private ActionForward setRedirectParams(ActionRedirect redirect, HttpServletRequest req) {
    if (req.getParameterMap() != null) {
      Set<String> paramNames = req.getParameterMap().keySet();
      for (String name : paramNames) {
        redirect.addParameter(name, req.getParameter(name));
      }
    }
    return redirect;
  }

  /** The selected values. */
  private static Map selectedValues = null;

  /**
   * Validate and process item level remittance from excel sheet.
   *
   * @param req
   *          the req
   * @param bean
   *          the bean
   * @param remittanceForm
   *          the r form
   * @return the map
   * @throws Exception
   *           the exception
   */
  private Map validateAndProcessItemLevelRemittanceFromExcelSheet(HttpServletRequest req,
      BasicDynaBean bean, RemittanceForm remittanceForm) throws Exception {

    String centerOrAccountGroup = req.getParameter("center_or_account_group");

    String accGrpOrCenterIdStr = (centerOrAccountGroup == null || centerOrAccountGroup.equals(""))
        ? ""
        : (centerOrAccountGroup.toString());
    String accGrpIdStr = null;
    String centerIdStr = null;
    int accGrpId = 0;
    int centerId = 0;
    selectedValues = new HashMap();
    if (!accGrpOrCenterIdStr.equals("")) {
      if (accGrpOrCenterIdStr.startsWith("A")) {
        accGrpIdStr = accGrpOrCenterIdStr.substring(1, accGrpOrCenterIdStr.length());
        accGrpId = accGrpIdStr != null && !accGrpIdStr.trim().equals("") ? new Integer(accGrpIdStr)
            : 0;
        BasicDynaBean accbean = (BasicDynaBean) new AccountingGroupMasterDAO()
            .findByKey("account_group_id", accGrpId);
        String accountGrpName = accbean != null ? (String) accbean.get("account_group_name") : "";
        selectedValues.put("account_group", accGrpIdStr);
        selectedValues.put("account_group_name", accountGrpName);
      } else if (accGrpOrCenterIdStr.startsWith("C")) {
        centerIdStr = accGrpOrCenterIdStr.substring(1, accGrpOrCenterIdStr.length());
        centerId = centerIdStr != null && !centerIdStr.trim().equals("") ? new Integer(centerIdStr)
            : 0;
        BasicDynaBean centerbean = (BasicDynaBean) new CenterMasterDAO().findByKey("center_id",
            centerId);
        String centerName = centerbean != null ? (String) centerbean.get("center_name") : "";
        selectedValues.put("center_id", centerIdStr);
        selectedValues.put("center_name", centerName);
      }
    }

    selectedValues.put("tpa_id", req.getParameter("tpa_id"));

    selectedValues.put("item_identification", req.getParameter("item_identification"));

    if (req.getParameter("item_identification").equals("ActivityId")) {
      selectedValues.put("item_id_heading", req.getParameter("item_id_heading"));
    } else {
      selectedValues.put("bill_no_heading", req.getParameter("bill_no_heading"));
      selectedValues.put("service_name_heading", req.getParameter("service_name_heading"));
      selectedValues.put("service_posted_date_heading",
          req.getParameter("service_posted_date_heading"));
      selectedValues.put("charge_insurance_claim_amount_heading",
          req.getParameter("charge_insurance_claim_amount_heading"));
    }

    selectedValues.put("payment_ref_type", req.getParameter("payment_ref_type"));

    if (req.getParameter("payment_ref_type").equals("PerItem")) {
      selectedValues.put("payment_reference_heading",
          req.getParameter("payment_reference_heading"));
    } else {
      selectedValues.put("payment_reference", req.getParameter("payment_reference"));
    }

    selectedValues.put("amount_heading", req.getParameter("amount_heading"));
    selectedValues.put("denial_remarks_heading", req.getParameter("denial_remarks_heading"));
    selectedValues.put("payer_id_heading", req.getParameter("payer_id_heading"));
    selectedValues.put("worksheet_index", req.getParameter("worksheet_index"));
    selectedValues.put("detail_level", req.getParameter("detail_level"));

    int selectedSheetNo = new Integer(req.getParameter("worksheet_index"));

    selectedSheetNo -= 1;

    RemittanceSpreadsheetProvider remittanceSpreadsheetProvider = 
        new RemittanceSpreadsheetProvider();

    HSSFWorkbook workBook = new HSSFWorkbook(
        remittanceForm.getRemittance_metadata().getInputStream());
    Map descMap = remittanceSpreadsheetProvider.getReportDescFromSheetItemLevel(selectedValues,
        workBook, selectedSheetNo);

    if (descMap.get("error") != null
        && ((String) descMap.get("error")).startsWith("Column Not Found")) {
      String invalidColumn = ((String) descMap.get("error")).split(":")[1].trim();

      String error = "Required columns are not found (or) incorrect column names.<br/>"
          + "Please check the Excel file <b>"
          + remittanceForm.getRemittance_metadata().getFileName() + "</b> sheet number : <b>"
          + (selectedSheetNo + 1) + "</b> .<br/>" + "Column Not Found  "
          + req.getParameter(invalidColumn + "_lbl") + " : <b> " + req.getParameter(invalidColumn)
          + "</b>";
      descMap.put("error", error);
    }

    return descMap;
  }

  /**
   * Validate and process item level remittance from XML.
   *
   * @param bean
   *          the bean
   * @param remittanceForm
   *          the r form
   * @return the map
   * @throws Exception
   *           the exception
   */
  private Map validateAndProcessItemLevelRemittanceFromXML(BasicDynaBean bean,
      RemittanceForm remittanceForm) throws Exception {
    RemittanceXmlProvider remittanceXmlProvider = new RemittanceXmlProvider();
    Map descMap = new HashMap();
    String errorMsg = null;
    RemittanceAdvice desc = remittanceXmlProvider
        .getReportDescForStream(remittanceForm.getRemittance_metadata().getInputStream());
    errorMsg = desc == null ? "XML parsing failed: Incorrectly formatted values supplied"
        : remittanceXmlProvider.validateXmlForXsdConformance(desc, bean);
    descMap.put("error", errorMsg);
    descMap.put("RemittanceDesc", desc);

    return descMap;
  }

  /**
   * List.
   *
   * @param mapping
   *          the m
   * @param actionForm
   *          the f
   * @param req
   *          the req
   * @param resp
   *          the resp
   * @return the action forward
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws ServletException
   *           the servlet exception
   * @throws Exception
   *           the exception
   */
  @IgnoreConfidentialFilters
  public ActionForward list(ActionMapping mapping, ActionForm actionForm, HttpServletRequest req,
      HttpServletResponse resp) throws IOException, ServletException, Exception {
    RemittanceAdviceDAO dao = new RemittanceAdviceDAO();
    Map map = req.getParameterMap();
    PagedList pagedList = dao.search(map,
        ConversionUtils.getListingParameter(req.getParameterMap()));
    req.setAttribute("pagedList", pagedList);
    return mapping.findForward("list");
  }

  /**
   * Sets the accounting group details.
   *
   * @param accGrpId
   *          the acc grp id
   * @param columnMap
   *          the column map
   * @throws SQLException
   *           the SQL exception
   */
  private void setAccountingGroupDetails(Integer accGrpId, Map columnMap) throws SQLException {
    if (null != accGrpId) {
      BasicDynaBean accbean = (BasicDynaBean) new AccountingGroupMasterDAO()
          .findByKey("account_group_id", accGrpId);
      String accountGrpName = accbean != null ? (String) accbean.get("account_group_name") : "";
      columnMap.put("account_group", accGrpId.toString());
      columnMap.put("account_group_name", accountGrpName);
    }
  }

  /**
   * Sets the center details.
   *
   * @param centerId
   *          the center id
   * @param columnMap
   *          the column map
   * @throws SQLException
   *           the SQL exception
   */
  private void setCenterDetails(Integer centerId, Map columnMap) throws SQLException {
    if (null != centerId) {
      BasicDynaBean centerbean = (BasicDynaBean) new CenterMasterDAO().findByKey("center_id",
          centerId);
      String centerName = centerbean != null ? (String) centerbean.get("center_name") : "";
      columnMap.put("center_id", centerId.toString());
      columnMap.put("center_name", centerName);
    }
  }

}
