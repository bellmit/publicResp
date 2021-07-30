package com.bob.hms.adminmasters.organization;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.otmasters.theamaster.TheatreMasterDAO;
import com.insta.hms.adminmaster.packagemaster.PackageChargeDAO;
import com.insta.hms.adminmaster.packagemaster.PackageDAO;
import com.insta.hms.adminmasters.bedmaster.BedMasterDAO;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.diagnosticsmasters.addtest.AddTestDAOImpl;
import com.insta.hms.master.AnaesthesiaTypeMaster.AnaesthesiaTypeChargesDAO;
import com.insta.hms.master.AnaesthesiaTypeMaster.AnaesthesiaTypeMasterDAO;
import com.insta.hms.master.ConsultationCharges.ConsultationChargesDAO;
import com.insta.hms.master.ConsultationTypes.ConsultationTypesDAO;
import com.insta.hms.master.DoctorMaster.DoctorChargeDAO;
import com.insta.hms.master.DoctorMaster.DoctorMasterDAO;
import com.insta.hms.master.DynaPackage.DynaPackageCategoryLimitsDAO;
import com.insta.hms.master.DynaPackage.DynaPackageChargesDAO;
import com.insta.hms.master.DynaPackage.DynaPackageDAO;
import com.insta.hms.master.DynaPackage.DynaPackageOrgDAO;
import com.insta.hms.master.DynaPackageCategory.DynaPackageCategoryMasterDAO;
import com.insta.hms.master.EquipmentMaster.EquipmentChargeDAO;
import com.insta.hms.master.EquipmentMaster.EquipmentMasterDAO;
import com.insta.hms.master.OperationMaster.OperationChargeDAO;
import com.insta.hms.master.OperationMaster.OperationMasterDAO;
import com.insta.hms.master.RegistrationCharges.RegistrationChargesDAO;
import com.insta.hms.master.ServiceMaster.ServiceChargeDAO;
import com.insta.hms.master.ServiceMaster.ServiceMasterDAO;
import com.insta.hms.master.rateplan.ItemChargeDAO;

import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


/**
 * The Class RateMasterAction.
 */
public class RateMasterAction extends DispatchAction {

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(RateMasterAction.class);

  /** The rdao. */
  static RateMasterDao rdao = new RateMasterDao();

  /** The rate parameter dao. */
  static GenericDAO rateParameterDao = new GenericDAO("rate_plan_parameters");

  /** The package charge dao. */
  static PackageChargeDAO packageChargeDao = new PackageChargeDAO();

  /** The bed master dao. */
  static BedMasterDAO bedMasterDao = new BedMasterDAO();

  /** The pack master dao. */
  static GenericDAO packMasterDao = new GenericDAO("packages");

  /** The consultation type dao. */
  static ConsultationTypesDAO consultationTypeDao = new ConsultationTypesDAO();

  /** The org master dao. */
  static OrgMasterDao orgMasterDao = new OrgMasterDao();

  /** The doctor charge dao. */
  static DoctorChargeDAO doctorChargeDao = new DoctorChargeDAO();

  /** The theatre master dao. */
  static TheatreMasterDAO theatreMasterDao = new TheatreMasterDAO();

  /** The doctor master dao. */
  static DoctorMasterDAO doctorMasterDao = new DoctorMasterDAO();

  /** The equipment charge dao. */
  static EquipmentChargeDAO equipmentChargeDao = new EquipmentChargeDAO();

  /** The equipment master dao. */
  static EquipmentMasterDAO equipmentMasterDao = new EquipmentMasterDAO();

  /** The operation master dao. */
  static OperationMasterDAO operationMasterDao = new OperationMasterDAO();

  /** The operation charge dao. */
  static OperationChargeDAO operationChargeDao = new OperationChargeDAO();

  /** The service master dao. */
  static ServiceMasterDAO serviceMasterDao = new ServiceMasterDAO();

  /** The service charge dao. */
  static ServiceChargeDAO serviceChargeDao = new ServiceChargeDAO();

  /** The anaesthesia type master dao. */
  static AnaesthesiaTypeMasterDAO anaesthesiaTypeMasterDao = new AnaesthesiaTypeMasterDAO();

  /** The dyna package dao. */
  static DynaPackageDAO dynaPackageDao = new DynaPackageDAO();

  /** The package item charges dao. */
  static GenericDAO packageItemChargesDao = new GenericDAO("package_item_charges");

  /** The anaesthesia type charges dao. */
  static AnaesthesiaTypeChargesDAO anaesthesiaTypeChargesDao = new AnaesthesiaTypeChargesDAO();

  /** The consultation charges dao. */
  static ConsultationChargesDAO consultationChargesDao = new ConsultationChargesDAO();

  /** The dyna package charges dao. */
  static DynaPackageChargesDAO dynaPackageChargesDao = new DynaPackageChargesDAO();

  /** The registration charges dao. */
  static RegistrationChargesDAO registrationChargesDao = new RegistrationChargesDAO();

  /**
   * Gets the rate plan details.
   *
   * @param mapping  the mapping
   * @param form     the form
   * @param request  the request
   * @param response the response
   * @return the rate plan details
   * @throws IOException  Signals that an I/O exception has occurred.
   * @throws SQLException the SQL exception
   * @throws Exception    the exception
   */
  public ActionForward getRatePlanDetails(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws IOException, SQLException, Exception {

    Map<String, String[]> paramMap = request.getParameterMap();
    Map requestParams = new HashMap();
    requestParams.putAll(paramMap);
    requestParams.remove("orgId");
    Map<LISTING, Object> pagingParams = ConversionUtils.getListingParameter(requestParams);

    PagedList pagedList = rdao.getRatePlanDetails(requestParams, pagingParams);
    request.setAttribute("pagedList", pagedList);
    return mapping.findForward("ratePlanDetails");
  }

  /**
   * Adds the rate plan details.
   *
   * @param mapping  the mapping
   * @param form     the form
   * @param request  the request
   * @param response the response
   * @return the action forward
   * @throws IOException  Signals that an I/O exception has occurred.
   * @throws SQLException the SQL exception
   * @throws Exception    the exception
   */
  public ActionForward addRatePlanDetails(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws IOException, SQLException, Exception {
    JSONSerializer js = new JSONSerializer().exclude("class");
    request.setAttribute("rateSheetList",
        js.serialize(ConversionUtils.listBeanToListMap(rdao.getRateSheetList())));
    request.setAttribute("baseRateSheetsDetails", js.serialize(Collections.EMPTY_LIST));
    return mapping.findForward("addRatePlanDetaisl");
  }

  /**
   * Show rate plan details.
   *
   * @param mapping  the mapping
   * @param form     the form
   * @param request  the request
   * @param response the response
   * @return the action forward
   * @throws IOException      Signals that an I/O exception has occurred.
   * @throws ServletException the servlet exception
   * @throws Exception        the exception
   */
  public ActionForward showRatePlanDetails(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException, Exception {
    JSONSerializer js = new JSONSerializer().exclude("class");
    String orgId = (String) request.getParameter("org_id");
    request.setAttribute("rateSheetList",
        js.serialize(ConversionUtils.listBeanToListMap(rdao.getRateSheetList())));
    BasicDynaBean bean = orgMasterDao.findByKey("org_id", orgId);
    request.setAttribute("ratePlan", bean);
    List<BasicDynaBean> baseRateSheetsDetails = rdao.getRateSheetDetails(orgId);
    request.setAttribute("baseRateSheetsDetails",
        js.serialize(ConversionUtils.copyListDynaBeansToMap(baseRateSheetsDetails)));

    request.setAttribute("anesthesiaTotal",
        rdao.getTotalCount("anesthesia_type_org_details", orgId));
    request.setAttribute("anesthesiaExcluded",
        rdao.getItemsExcludedCount("anesthesia_type_org_details", orgId));
    request.setAttribute("anesthesiaOverrided",
        rdao.getItemsOverridedCount("anesthesia_type_org_details", orgId));

    request.setAttribute("consultationTotal",
        rdao.getTotalCount("consultation_org_details", orgId));
    request.setAttribute("consultationExcluded",
        rdao.getItemsExcludedCount("consultation_org_details", orgId));
    request.setAttribute("consultationOverrided",
        rdao.getItemsOverridedCount("consultation_org_details", orgId));

    request.setAttribute("testTotal", rdao.getTotalCount("test_org_details", orgId));
    request.setAttribute("testExcluded", rdao.getItemsExcludedCount("test_org_details", orgId));
    request.setAttribute("testOverrided", rdao.getItemsOverridedCount("test_org_details", orgId));

    request.setAttribute("dynaTotal", rdao.getTotalCount("dyna_package_org_details", orgId));
    request.setAttribute("dynaExcluded",
        rdao.getItemsExcludedCount("dyna_package_org_details", orgId));
    request.setAttribute("dynaOverrided",
        rdao.getItemsOverridedCount("dyna_package_org_details", orgId));

    request.setAttribute("opeTotal", rdao.getTotalCount("operation_org_details", orgId));
    request.setAttribute("opeExcluded", rdao.getItemsExcludedCount("operation_org_details", orgId));
    request.setAttribute("opeOverrided",
        rdao.getItemsOverridedCount("operation_org_details", orgId));

    request.setAttribute("packTotal", rdao.getTotalCount("pack_org_details", orgId));
    request.setAttribute("packExcluded", rdao.getItemsExcludedCount("pack_org_details", orgId));
    request.setAttribute("packCount", rdao.getPackagesCount());
    request.setAttribute("packOverrided", rdao.getItemsOverridedCount("pack_org_details", orgId));

    request.setAttribute("serviceTotal", rdao.getTotalCount("service_org_details", orgId));
    request.setAttribute("serviceExcluded",
        rdao.getItemsExcludedCount("service_org_details", orgId));
    request.setAttribute("serviceOverrided",
        rdao.getItemsOverridedCount("service_org_details", orgId));

    request.setAttribute("doctorTotal", rdao.getTotalCount("doctor_org_details", orgId));
    request.setAttribute("doctorOverrided",
        rdao.getItemsOverridedCount("doctor_org_details", orgId));

    request.setAttribute("equipTotal", rdao.getTotalCount("equip_org_details", orgId));
    request.setAttribute("equipOverrided", rdao.getItemsOverridedCount("equip_org_details", orgId));

    request.setAttribute("theatreTotal", rdao.getTotalCount("theatre_org_details", orgId));
    request.setAttribute("theatreOverrided",
        rdao.getItemsOverridedCount("theatre_org_details", orgId));

    request.setAttribute("bedtypeTotal", rdao.getbedTypeTotalCount());
    request.setAttribute("bedTypeOverrided", rdao.getbedTypeOverridedCount(orgId));

    int regChargesOverridedCount = rdao.getItemsOverridedCount("registration_charges", orgId);
    regChargesOverridedCount = regChargesOverridedCount > 0 ? 1 : 0;

    request.setAttribute("regChargesOverided", regChargesOverridedCount);

    return mapping.findForward("showRatePlanDetails");
  }

  /**
   * Creates the rate plan.
   *
   * @param mapping  the mapping
   * @param form     the form
   * @param request  the request
   * @param response the response
   * @return the action forward
   * @throws IOException  Signals that an I/O exception has occurred.
   * @throws SQLException the SQL exception
   * @throws Exception    the exception
   */
  public ActionForward createRatePlan(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws IOException, SQLException, Exception {
    Map<String, String[]> requestParams = request.getParameterMap();

    String orgId = orgMasterDao.getNextOrgId();

    String[] rateSheet = request.getParameterValues("ratesheet");
    String[] discORmarkup = request.getParameterValues("discORmarkup");
    String[] rateVariation = request.getParameterValues("rateVariation");
    String[] roundOff = request.getParameterValues("roundOff");
    String[] priority = request.getParameterValues("priority");
    String[] deleted = request.getParameterValues("deleted");

    List errors = new ArrayList();
    BasicDynaBean bean = orgMasterDao.getBean();
    ConversionUtils.copyToDynaBean(requestParams, bean, errors);
    String error = null;

    String hasDateValidity = request.getParameter("has_date_validity");
    String fromDate = request.getParameter("fromDate");
    String toDate = request.getParameter("toDate");

    bean.set("org_id", orgId);
    bean.set("is_rate_sheet", "N");

    if (null != hasDateValidity && hasDateValidity.equals("on") && null != fromDate
        && null != toDate) {
      SimpleDateFormat sm = new SimpleDateFormat("dd-MM-yyyy");
      java.util.Date fromDateDate = sm.parse(fromDate);
      java.util.Date toDateDate = sm.parse(toDate);
      bean.set("has_date_validity", true);
      bean.set("valid_from_date", new java.sql.Date(fromDateDate.getTime()));
      bean.set("valid_to_date", new java.sql.Date(toDateDate.getTime()));
    }
    Connection con = null;
    boolean success = false;
    OrgMasterBo orgBo = new OrgMasterBo();
    try {
      con = DataBaseUtil.getConnection(300);
      con.setAutoCommit(false);
      if (errors.isEmpty()) {
        BasicDynaBean existsbean = orgMasterDao.findExistsByKey("org_name",
            ((String) (bean.get("org_name"))).trim());
        if (existsbean != null) {
          if ((existsbean.get("is_rate_sheet")).equals("Y")) {
            error = "A Rate Sheet already exists with name : " + (String) (bean.get("org_name"));
          } else {
            error = "Rate plan with this name " + (String) (bean.get("org_name"))
                + " already exists.";
          }
        } else {
          success = orgMasterDao.insert(con, bean);
          if (!success) {
            error = "Failed to save rate plan details.";
          }

          if (success) {
            BasicDynaBean rateBean = rateParameterDao.getBean();
            int highestPriority = 0;
            String highPriorityRateSheet = null;
            int selectedRateSheet = 0;

            for (int i = 0; i < rateSheet.length; i++) {
              if (deleted[i].equals("false")) {
                highPriorityRateSheet = rateSheet[i];
                highestPriority = Integer.parseInt(priority[i]);
                selectedRateSheet = i;
                break;
              }
            }

            List<Integer> priorities = new ArrayList<Integer>();
            Map<Integer, Integer> priorityIndexMap = new HashMap<Integer, Integer>();
            for (int i = 0; i < rateSheet.length; i++) {
              if (deleted[i].equals("false")) {
                rateBean.set("org_id", orgId);
                rateBean.set("base_rate_sheet_id", rateSheet[i]);
                if (discORmarkup[i].equals("I")) {
                  rateBean.set("rate_variation_percent", +Integer.parseInt(rateVariation[i]));
                } else {
                  rateBean.set("rate_variation_percent", -Integer.parseInt(rateVariation[i]));
                }
                rateBean.set("round_off_amount", Integer.parseInt(roundOff[i]));
                rateBean.set("priority", Integer.parseInt(priority[i]));
                // getting highest priority rate sheet here
                if (Integer.parseInt(priority[i]) < highestPriority) {
                  highestPriority = Integer.parseInt(priority[i]);
                  highPriorityRateSheet = rateSheet[i];
                  selectedRateSheet = i;
                }
                success = success && rateParameterDao.insert(con, rateBean);
                priorities.add(Integer.parseInt(priority[i]));
                priorityIndexMap.put(Integer.parseInt(priority[i]), i);
              }
            }

            String userName = (String) request.getSession(false).getAttribute("userid");
            String orgName = (String) bean.get("org_name");
            logger.info("priorityIndxMap :"
                + ((null != priorityIndexMap) ? priorityIndexMap.size() : "null"));
            orgBo.switchTriggers(con, "DISABLE");
            createDependentRatePlans(con, orgId, rateSheet, deleted, discORmarkup, rateVariation,
                roundOff, priorities, priorityIndexMap, userName, orgName);
          }

        }
      } else {
        error = "Incorrectly formatted values supplied.";
      }
    } finally {
      logger.info("Finally Block : " + new Date());
      DataBaseUtil.commitClose(con, success);
    }
    orgBo.switchTriggers(con, "ENABLE");
    ActionRedirect redirect = null;
    FlashScope flash = FlashScope.getScope(request);
    if (error != null) {
      redirect = new ActionRedirect(mapping.findForward("addRedirect"));
      flash.error(error);

    } else {
      redirect = new ActionRedirect(mapping.findForward("showRedirect"));
      redirect.addParameter("org_id", bean.get("org_id"));
    }
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

    return redirect;

  }

  /**
   * Creates the dependent rate plans.
   *
   * @param con              the con
   * @param orgId            the org id
   * @param rateSheet        the rate sheet
   * @param deleted          the deleted
   * @param discORmarkup     the disc O rmarkup
   * @param rateVariation    the rate variation
   * @param roundOff         the round off
   * @param priorities       the priorities
   * @param priorityIndexMap the priority index map
   * @param userName         the user name
   * @param orgName          the org name
   * @return true, if successful
   * @throws Exception the exception
   */
  private boolean createDependentRatePlans(Connection con, String orgId, String[] rateSheet,
      String[] deleted, String[] discORmarkup, String[] rateVariation, String[] roundOff,
      List<Integer> priorities, Map<Integer, Integer> priorityIndexMap, String userName,
      String orgName) throws Exception {

    int currentSheet = -1;
    boolean success = false;
    OrgMasterBo orgBo = new OrgMasterBo();
    if (null != priorities) {
      Collections.sort(priorities);
      // int i = priorities.get(0);
      for (int i = 0; i < priorities.size(); i++) {
        success = false;
        currentSheet = priorityIndexMap.get(priorities.get(i));
        String varianceType = discORmarkup[currentSheet].equals("I") ? "Incr" : "Decr";
        Double varianceBy = new Double(rateVariation[currentSheet]);
        Double nearestRoundOffValue = new Double(roundOff[currentSheet]);
        if (i == 0) {
          success = orgBo.initRatePlan(con, orgId, varianceType, varianceBy,
              rateSheet[currentSheet], nearestRoundOffValue, userName, orgName);
        } else {
          success = orgBo.updateRatePlan(con, orgId, varianceType, varianceBy, false,
              rateSheet[currentSheet], nearestRoundOffValue, userName, orgName);
        }
        if (!success) {
          break; // early exit
        }
      }
    }
    // orgBO.switchTriggers(con, "ENABLE");
    return success;
  }

  /**
   * Update dependent rate plans.
   *
   * @param con           the con
   * @param rateSheetList the rate sheet list
   * @param orgId         the org id
   * @param userName      the user name
   * @param orgName       the org name
   * @return true, if successful
   * @throws Exception the exception
   */
  private boolean updateDependentRatePlans(Connection con, List<BasicDynaBean> rateSheetList,
      String orgId, String userName, String orgName) throws Exception {
    boolean success = false;
    OrgMasterBo orgBo = new OrgMasterBo();
    // orgBO.switchTriggers(con, "DISABLE");
    for (int i = 0; i < rateSheetList.size(); i++) {
      success = false;
      BasicDynaBean currentSheet = rateSheetList.get(i);
      Integer variation = (Integer) currentSheet.get("rate_variation_percent");
      String varianceType = (variation >= 0) ? "Incr" : "Decr";
      Double varianceBy = new Double((variation >= 0 ? variation : -variation));
      Double roundOff = new Double((Integer) currentSheet.get("round_off_amount"));
      if (i == 0) {
        success = orgBo.reinitRatePlan(con, orgId, varianceType, varianceBy,
            (String) currentSheet.get("base_rate_sheet_id"), roundOff, userName, orgName);
      } else {
        success = orgBo.updateRatePlan(con, orgId, varianceType, varianceBy, false,
            (String) currentSheet.get("base_rate_sheet_id"), roundOff, userName, orgName);
      }
    }
    // orgBO.switchTriggers(con, "ENABLE");
    return success;
  }

  /**
   * Gets the rate sheets by priority.
   *
   * @param con              the con
   * @param orgId            the org id
   * @param rateParameterDao the rate parameter dao
   * @return the rate sheets by priority
   * @throws SQLException the SQL exception
   */
  private List<BasicDynaBean> getRateSheetsByPriority(Connection con, String orgId,
      GenericDAO rateParameterDao) throws SQLException {
    Map filterMap = new HashMap();
    filterMap.put("org_id", orgId);
    List<BasicDynaBean> sheets = rateParameterDao.listAll(con, null, filterMap, "priority");
    logger.info("base rate sheet count :" + ((null != sheets) ? sheets.size() : "null"));
    return sheets;
  }

  /**
   * Update rate plan details.
   *
   * @param mapping the mapping
   * @param form    the form
   * @param request the request
   * @param resp    the resp
   * @return the action forward
   * @throws IOException      Signals that an I/O exception has occurred.
   * @throws ServletException the servlet exception
   * @throws Exception        the exception
   */
  public ActionForward updateRatePlanDetails(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse resp)
      throws IOException, ServletException, Exception {

    Map<String, String[]> params = request.getParameterMap();
    String hasDateValidity = request.getParameter("has_date_validity");
    String fromDate = request.getParameter("fromDate");
    String toDate = request.getParameter("toDate");

    String[] rateSheet = request.getParameterValues("ratesheet");
    String[] discORmarkup = request.getParameterValues("discORmarkup");
    String[] rateVariation = request.getParameterValues("rateVariation");
    String[] roundOff = request.getParameterValues("roundOff");
    String[] priority = request.getParameterValues("priority");
    String[] deleted = request.getParameterValues("deleted");
    String[] addedNew = request.getParameterValues("addedNew");

    List errors = new ArrayList();
    Connection con = null;
    boolean success = false;
    BasicDynaBean bean = orgMasterDao.getBean();
    ConversionUtils.copyToDynaBean(params, bean, errors);
    OrgMasterBo orgBo = new OrgMasterBo();

    if (null != hasDateValidity && hasDateValidity.equals("on") && null != fromDate
        && null != toDate) {
      SimpleDateFormat sm = new SimpleDateFormat("dd-MM-yyyy");
      java.util.Date fromDateDate = sm.parse(fromDate);
      java.util.Date toDateDate = sm.parse(toDate);
      bean.set("has_date_validity", true);
      bean.set("valid_from_date", new java.sql.Date(fromDateDate.getTime()));
      bean.set("valid_to_date", new java.sql.Date(toDateDate.getTime()));
    }

    FlashScope flash = FlashScope.getScope(request);
    String key = (String) request.getParameter("org_id");
    List<BasicDynaBean> baseRateSheets = new ArrayList<BasicDynaBean>();
    try {
      con = DataBaseUtil.getConnection(300);
      con.setAutoCommit(false);

      Map<String, String> keys = new HashMap<String, String>();
      keys.put("org_id", key);

      if (errors.isEmpty()) {
        int updatedOrgs = orgMasterDao.update(con, bean.getMap(), keys);
        if (updatedOrgs > 0) {
          success = true;
        }
        if (success) {
          BasicDynaBean rateBean = rateParameterDao.getBean();

          for (int i = 0; i < rateSheet.length; i++) {
            if (!(addedNew[i].equals("true") && deleted[i].equals("true"))) {
              rateBean.set("org_id", key);
              rateBean.set("base_rate_sheet_id", rateSheet[i]);
              if (discORmarkup[i].equals("I")) {
                rateBean.set("rate_variation_percent", +Integer.parseInt(rateVariation[i]));
              } else {
                rateBean.set("rate_variation_percent", -Integer.parseInt(rateVariation[i]));
              }
              rateBean.set("round_off_amount", Integer.parseInt(roundOff[i]));
              if (null != priority[i] && !priority[i].equals("")) {
                rateBean.set("priority", Integer.parseInt(priority[i]));
              }

              Map<String, Object> upKeys = new HashMap<String, Object>();
              upKeys.put("org_id", key);
              upKeys.put("base_rate_sheet_id", rateSheet[i]);

              if (addedNew[i].equals("true") && deleted[i].equals("false")) {
                success = success && rateParameterDao.insert(con, rateBean);
              } else if (addedNew[i].equals("false") && deleted[i].equals("false")) {
                int updatedRateParams = rateParameterDao.update(con, rateBean.getMap(), upKeys);
                success = success && updatedRateParams > 0;
              } else if (addedNew[i].equals("false") && deleted[i].equals("true")) {
                LinkedHashMap<String, Object> delKeys = new LinkedHashMap<String, Object>();
                delKeys.put("org_id", key);
                delKeys.put("base_rate_sheet_id", rateSheet[i]);
                success = success && rateParameterDao.delete(con, delKeys);
              }
            }
          }

          baseRateSheets = getRateSheetsByPriority(con, key, rateParameterDao);
          String userName = (String) request.getSession(false).getAttribute("userid");
          String orgName = (String) bean.get("org_name");
          orgBo.switchTriggers(con, "DISABLE");
          updateDependentRatePlans(con, baseRateSheets, key, userName, orgName);
        }

        if (success) {
          flash.success("Rate Plan details updated successfully..");
        } else {
          con.rollback();
          flash.error("Failed to update Rate plan details..");
        }
      } else {
        flash.error("Incorrectly formatted values supplied");
      }
    } finally {
      DataBaseUtil.commitClose(con, success);
    }

    orgBo.switchTriggers(con, "ENABLE");

    ActionRedirect redirect = new ActionRedirect(mapping.findForward("showRedirect"));
    redirect.addParameter("org_id", key);
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
    return redirect;
  }

  /**
   * Gets the exclude charges screen.
   *
   * @param mapping  the mapping
   * @param form     the form
   * @param request  the request
   * @param response the response
   * @return the exclude charges screen
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws Exception   the exception
   */
  public ActionForward getExcludeChargesScreen(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws IOException, Exception {
    String chargeCategory = (String) request.getParameter("chargeCategory");
    String orgId = (String) request.getParameter("org_id");
    String excludeCharges = null;
    List<String> categoryNames = null;

    if (chargeCategory.equals("anesthesia")) {
      excludeCharges = "excludeAnaesthesiaCharges";
      categoryNames = anaesthesiaTypeMasterDao.getAllNames();
    } else if (chargeCategory.equals("consultation")) {
      excludeCharges = "excludeConsultationCharges";
      categoryNames = consultationTypeDao.getAllNames();
    } else if (chargeCategory.equals("diagnostics")) {
      excludeCharges = "excludeDiagnosticsCharges";
      categoryNames = new GenericDAO("diagnostics").getColumnList("test_name");
    } else if (chargeCategory.equals("dynapackages")) {
      excludeCharges = "excludeDynaPackageCharges";
      categoryNames = dynaPackageDao.getColumnList("dyna_package_name");
    } else if (chargeCategory.equals("operations")) {
      excludeCharges = "excludeOperationCharges";
    } else if (chargeCategory.equals("packages")) {
      excludeCharges = "excludePackageCharges";
    } else if (chargeCategory.equals("services")) {
      excludeCharges = "excludeServiceCharges";
    }

    String orgName = (String) request.getParameter("org_name");
    BasicDynaBean bean = orgMasterDao.findByKey("org_id", orgId);
    String rateSheetUrl = "/pages/masters/hosp/admin"
        + "/Organ.do?_method=editOrganiaztionDetails&orgId=";
    String ratePlanUrl = "/pages/masters/ratePlan.do?_method=showRatePlanDetails&org_id=";

    JSONSerializer js = new JSONSerializer().exclude("class");
    Map<String, String[]> requestParams = request.getParameterMap();
    Map<LISTING, Object> pagingParams = ConversionUtils.getListingParameter(requestParams);
    PagedList pagedlist = RateMasterDao.getCategoryChargesList(orgId, chargeCategory, requestParams,
        pagingParams);
    request.setAttribute("pagedList", pagedlist);

    request.setAttribute("namesJSON", js.serialize(categoryNames));
    request.setAttribute("org_id", orgId);
    request.setAttribute("org_name", orgName);
    request.setAttribute("screen",
        ((String) bean.get("is_rate_sheet")).equals("Y") ? "Rate Sheet" : "Rate Plan");
    request.setAttribute("screenURL",
        ((String) bean.get("is_rate_sheet")).equals("Y") ? rateSheetUrl : ratePlanUrl);

    return mapping.findForward(excludeCharges);
  }

  /**
   * Exclude charges.
   *
   * @param mapping  the mapping
   * @param form     the form
   * @param request  the request
   * @param response the response
   * @return the action forward
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws Exception   the exception
   */
  public ActionForward excludeCharges(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws IOException, Exception {
    Map<String, String[]> map = request.getParameterMap();
    String[] categoryIds = (String[]) map.get("category_id");
    String[] applicable = (String[]) map.get("applicable");
    String orgId = (String) request.getParameter("org_id");
    String chargeCategory = (String) request.getParameter("chargeCategory");
    String orgDetailTblName = null;
    String chargeTblName = null;
    String categoryIdName = null;

    String isRateSheet = (String) orgMasterDao.findByKey("org_id", orgId).get("is_rate_sheet");

    if (chargeCategory.equals("anesthesia")) {
      orgDetailTblName = "anesthesia_type_org_details";
      chargeTblName = "anesthesia_type_charges";
      categoryIdName = "anesthesia_type_id";
    } else if (chargeCategory.equals("consultation")) {
      orgDetailTblName = "consultation_org_details";
      chargeTblName = "consultation_charges";
      categoryIdName = "consultation_type_id";
    } else if (chargeCategory.equals("diagnostics")) {
      orgDetailTblName = "test_org_details";
      chargeTblName = "diagnostic_charges";
      categoryIdName = "test_id";
    } else if (chargeCategory.equals("dynapackages")) {
      orgDetailTblName = "dyna_package_org_details";
      chargeTblName = "dyna_package_charges";
      categoryIdName = "dyna_package_id";
    } else if (chargeCategory.equals("operations")) {
      orgDetailTblName = "operation_org_details";
      chargeTblName = "operation_charges";
      categoryIdName = "operation_id";
    } else if (chargeCategory.equals("packages")) {
      orgDetailTblName = "pack_org_details";
      chargeTblName = "package_charges";
      categoryIdName = "package_id";
    } else if (chargeCategory.equals("services")) {
      orgDetailTblName = "service_org_details";
      chargeTblName = "service_master_charges";
      categoryIdName = "service_id";
    }
    List<BasicDynaBean> derivedRatePlanIds = rdao.getDerivedRatePlanIds(orgId);
    ItemChargeDAO itemChgDao = new ItemChargeDAO(chargeTblName);

    Connection con = null;
    boolean success = false;
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      if (categoryIds != null) {
        GenericDAO dao = new GenericDAO(orgDetailTblName);

        for (int i = 0; i < categoryIds.length; i++) {
          Map<String, Object> keys = new HashMap<String, Object>();
          if (chargeCategory.equals("consultation") || chargeCategory.equals("dynapackages")
              || chargeCategory.equals("packages")) {
            keys.put(categoryIdName, Integer.parseInt(categoryIds[i]));
          } else {
            keys.put(categoryIdName, categoryIds[i]);
          }
          keys.put("org_id", orgId);
          BasicDynaBean bean = dao.findByKey(con, keys);
          if (applicable[i].equals("false")) {
            bean.set("applicable", false);
          } else {
            bean.set("applicable", true);
          }

          /*
           * Check the item exclusion status in base rate sheet and rate plan. If rateplan status is
           * different from base rate sheet status then mark rate plan item as overrided
           */
          boolean overrideItem = false;
          if (isRateSheet.equals("N")) {
            overrideItem = itemChgDao.checkItemStatus(con, orgId, orgDetailTblName, chargeCategory,
                categoryIdName, categoryIds[i], applicable[i]);
          }
          // Set override flag to yes for rate plan exclusion.
          if (isRateSheet.equals("N") && overrideItem) {
            bean.set("is_override", "Y");
          }

          int updatedDetails = dao.update(con, bean.getMap(), keys);
          if (updatedDetails > 0) {
            success = true;
          }

          // Set override flag of charges table to yes for rate plan exclusion.
          if (isRateSheet.equals("N") && overrideItem && success) {
            itemChgDao.OverrideItemCharges(con, chargeTblName, chargeCategory, orgId,
                categoryIdName, categoryIds[i]);
          }

          if (derivedRatePlanIds != null) {
            itemChgDao.updateApplicableflagForDerivedRatePlans(con, derivedRatePlanIds,
                chargeCategory, categoryIdName, categoryIds[i], orgDetailTblName, orgId);
          }

          if (chargeCategory.equals("packages")) {
            BasicDynaBean packBean = packMasterDao.findByKey(con, "package_id",
                Integer.parseInt(categoryIds[i]));
            boolean isMultiVisitPackage = (Boolean) packBean.get("multi_visit_package");
            if (isMultiVisitPackage) {
              updatePackageItemCharges(con, orgId, isRateSheet.equals("Y"), overrideItem,
                  categoryIds[i], derivedRatePlanIds);
            }
          }
        }
      }
    } finally {
      DataBaseUtil.commitClose(con, success);
    }

    String orgName = (String) request.getParameter("org_name");

    ActionRedirect redirect = new ActionRedirect(mapping.findForward("exclusionChargesRedirect"));
    redirect.addParameter("org_id", orgId);
    redirect.addParameter("org_name", orgName);
    redirect.addParameter("chargeCategory", chargeCategory);

    return redirect;
  }

  /**
   * Update package item charges.
   *
   * @param con                the con
   * @param orgId              the org id
   * @param isRateSheet        the is rate sheet
   * @param overrideItem       the override item
   * @param packageId          the package id
   * @param derivedRatePlanIds the derived rate plan ids
   * @return true, if successful
   * @throws SQLException the SQL exception
   * @throws Exception    the exception
   */
  public boolean updatePackageItemCharges(Connection con, String orgId, boolean isRateSheet,
      boolean overrideItem, String packageId, List<BasicDynaBean> derivedRatePlanIds)
      throws SQLException, Exception {
    boolean success = true;
    BasicDynaBean bean = packageItemChargesDao.getBean();
    Map<String, Object> keys = new HashMap<String, Object>();
    keys.put("package_id", Integer.parseInt(packageId));
    keys.put("org_id", orgId);
    if (!isRateSheet && overrideItem) {
      bean.set("is_override", "Y");
      success = packageItemChargesDao.update(con, bean.getMap(), keys) >= 0;
    }
    if (derivedRatePlanIds != null && isRateSheet) {
      GenericDAO odao = new GenericDAO("pack_org_details");
      Map<String, Object> okeys = new HashMap<String, Object>();

      okeys.put("package_id", Integer.parseInt(packageId));

      for (int k = 0; k < derivedRatePlanIds.size(); k++) {
        BasicDynaBean ratePlanBean = derivedRatePlanIds.get(k);
        String ratePlanId = (String) ratePlanBean.get("org_id");
        okeys.put("org_id", ratePlanId);
        BasicDynaBean rpBean = odao.findByKey(con, okeys);
        String isOverrided = (String) (rpBean.get("is_override") != null 
            ? rpBean.get("is_override") : "N");
        if (!isOverrided.equals("Y")) {
          packageChargeDao.updatePkgItemCharges(con, ratePlanId, orgId, packageId,
              rateParameterDao);
        }
      }
    }
    return success;
  }

  /**
   * Gets the charges list screen.
   *
   * @param mapping  the mapping
   * @param form     the form
   * @param request  the request
   * @param response the response
   * @return the charges list screen
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws Exception   the exception
   */
  public ActionForward getChargesListScreen(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws IOException, Exception {
    String chargeCategory = (String) request.getParameter("chargeCategory");
    String orgId = (String) request.getParameter("org_id");
    String overideCharges = null;
    List<String> categoryNames = null;
    Map<String, String[]> requestParams = request.getParameterMap();
    PagedList list = null;
    Map chargesMap = new LinkedHashMap();
    List<String> bedTypes = bedMasterDao.getUnionOfBedTypes();
    Map<LISTING, Object> pagingParams = ConversionUtils.getListingParameter(requestParams);
    List chargeList = null;

    if (chargeCategory.equals("anesthesia")) {
      overideCharges = "anaesthesiaChargesList";
      categoryNames = anaesthesiaTypeMasterDao.getAllNames();
      String chargeType = request.getParameter("_chargeType");
      if ((chargeType == null) || chargeType.isEmpty()) {
        chargeType = "min_charge";
      }
      list = RateMasterDao.getCategoryChargesList(orgId, chargeCategory, requestParams,
          pagingParams);
      List<String> ids = new ArrayList<String>();
      for (Map obj : (List<Map>) list.getDtoList()) {
        ids.add((String) obj.get("anesthesia_type_id"));
      }
      chargeList = anaesthesiaTypeChargesDao.getAllChargesForOrg(orgId, ids);
      chargesMap = ConversionUtils.listBeanToMapMapBean(chargeList, "anesthesia_type_id",
          "bed_type");
      request.setAttribute("chargeType", chargeType);

    } else if (chargeCategory.equals("consultation")) {
      overideCharges = "consultationChargesList";
      categoryNames = consultationTypeDao.getAllNames();
      list = consultationChargesDao.searchList(requestParams, pagingParams);
      chargeList = consultationChargesDao.getAllChargesForOrganisation(orgId);
      chargesMap = ConversionUtils.listBeanToMapMapBean(chargeList, "consultation_type_id",
          "bed_type");
    } else if (chargeCategory.equals("diagnostics")) {
      overideCharges = "diagnosticsChargesList";
      categoryNames = AddTestDAOImpl.getAllTestNames();
      list = AddTestDAOImpl.searchTests(requestParams, pagingParams);

      List<String> testIds = new ArrayList<String>();
      for (Map obj : (List<Map>) list.getDtoList()) {
        testIds.add((String) obj.get("test_id"));
      }
      chargeList = AddTestDAOImpl.getTestChargesForAllBedTypes(orgId, bedTypes, testIds);
      chargesMap = ConversionUtils.listBeanToMapMap(chargeList, "test_id");
    } else if (chargeCategory.equals("dynapackages")) {
      overideCharges = "dynaPackChargesList";
      categoryNames = dynaPackageDao.getColumnList("dyna_package_name");
      list = dynaPackageDao.search(requestParams, pagingParams);
      chargeList = dynaPackageChargesDao.getAllPackageChargesForOrganisation(orgId);
      chargesMap = ConversionUtils.listBeanToMapMapBean(chargeList, "dyna_package_id", "bed_type");
    } else if (chargeCategory.equals("operations")) {
      overideCharges = "operationChargesList";
      categoryNames = operationMasterDao.getAllNames();
      list = operationMasterDao.search(requestParams, pagingParams);

      List<String> ids = new ArrayList<String>();
      for (Map obj : (List<Map>) list.getDtoList()) {
        ids.add((String) obj.get("op_id"));
      }
      chargeList = operationChargeDao.getAllChargesForOrg(orgId, ids);
      chargesMap = ConversionUtils.listBeanToMapMapBean(chargeList, "op_id", "bed_type");

      String chargeType = request.getParameter("_chargeType");
      if ((chargeType == null) || chargeType.isEmpty()) {
        chargeType = "surg_asstance_charge";
      }
      request.setAttribute("chargeType", chargeType);
    } else if (chargeCategory.equals("packages")) {
      overideCharges = "packageChargesList";
      categoryNames = packMasterDao.getColumnList("package_name");
      list = PackageDAO.getPackages(requestParams, pagingParams, orgId);
    } else if (chargeCategory.equals("services")) {
      overideCharges = "serviceChargesList";
      categoryNames = serviceMasterDao.getAllNames();
      list = serviceMasterDao.search(requestParams, pagingParams);

      List<String> ids = new ArrayList<String>();
      for (Map obj : (List<Map>) list.getDtoList()) {
        ids.add((String) obj.get("service_id"));
      }
      chargeList = serviceChargeDao.getAllChargesForBedTypes(orgId, bedTypes, ids);
      chargesMap = ConversionUtils.listBeanToMapMap(chargeList, "service_id");
    } else if (chargeCategory.equals("equipment")) {
      overideCharges = "equipmentChargesList";
      list = equipmentMasterDao.search(requestParams,
          ConversionUtils.getListingParameter(requestParams));
      categoryNames = equipmentMasterDao.getAllNames();
      List<String> ids = new ArrayList<String>();
      for (Map obj : (List<Map>) list.getDtoList()) {
        ids.add((String) obj.get("eq_id"));
      }
      chargeList = equipmentChargeDao.getAllChargesForOrg(orgId, ids);
      chargesMap = ConversionUtils.listBeanToMapMapBean(chargeList, "equip_id", "bed_type");
      request.setAttribute("org_id", orgId);

      String chargeType = request.getParameter("_chargeType");
      if ((chargeType == null) || chargeType.isEmpty()) {
        chargeType = "daily_charge";
      }
      request.setAttribute("chargeType", chargeType);
    }

    String orgName = (String) request.getParameter("org_name");

    JSONSerializer js = new JSONSerializer().exclude("class");
    request.setAttribute("namesJSON", js.serialize(categoryNames));
    request.setAttribute("pagedList", list);

    request.setAttribute("bedTypes", bedTypes);
    request.setAttribute("charges", chargesMap);

    request.setAttribute("org_id", orgId);
    request.setAttribute("org_name", orgName);

    return mapping.findForward(overideCharges);
  }

  /**
   * Gets the doctor charges list.
   *
   * @param mapping  the mapping
   * @param form     the form
   * @param request  the request
   * @param response the response
   * @return the doctor charges list
   * @throws SQLException the SQL exception
   * @throws Exception    the exception
   */
  public ActionForward getDoctorChargesList(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws SQLException, Exception {

    String orgId = (String) request.getParameter("org_id");
    Map<String, String[]> requestParams = request.getParameterMap();
    PagedList list = null;
    Map chargesMap = new LinkedHashMap();
    List chargeList = null;
    Map chargeMap = new HashMap();

    for (int i = 0; i < DoctorMasterDAO.chargeValues.length; i++) {
      chargeMap.put(DoctorMasterDAO.chargeValues[i], DoctorMasterDAO.chargeTexts[i]);
    }
    String chargeType = request.getParameter("_charge_type");
    if ((chargeType == null) || chargeType.isEmpty()) {
      chargeType = "op_charge";
    }
    list = doctorMasterDao.search(requestParams,
        ConversionUtils.getListingParameter(requestParams));

    List<String> ids = new ArrayList<String>();
    for (Map obj : (List<Map>) list.getDtoList()) {
      ids.add((String) obj.get("doctor_id"));
    }

    if (chargeType.equals("op_charge") || chargeType.equals("op_revisit_charge")
        || chargeType.equals("private_cons_charge")
        || chargeType.equals("private_cons_revisit_charge")) {
      chargeList = doctorMasterDao.getAllOPChargesForOrg(orgId, ids);
      chargesMap = ConversionUtils.listBeanToMapMapBean(chargeList, "doctor_id", "org_id");
    } else {
      chargeList = doctorMasterDao.getAllIPChargesForOrg(orgId, ids);
      chargesMap = ConversionUtils.listBeanToMapMapBean(chargeList, "doctor_name", "bed_type");
    }

    List<String> categoryNames = doctorMasterDao.getAllNames();
    String orgName = (String) request.getParameter("org_name");
    List<String> bedTypes = bedMasterDao.getUnionOfBedTypes();

    JSONSerializer js = new JSONSerializer().exclude("class");
    request.setAttribute("chargeValues", DoctorMasterDAO.chargeValues);
    request.setAttribute("chargeMap", chargeMap);
    request.setAttribute("charges", chargesMap);
    request.setAttribute("namesJSON", js.serialize(categoryNames));
    request.setAttribute("pagedList", list);
    request.setAttribute("allDoctorsCount", DoctorMasterDAO.getAllDocorsCoust());
    request.setAttribute("bedTypes", bedTypes);
    request.setAttribute("org_id", orgId);
    request.setAttribute("org_name", orgName);
    request.setAttribute("charge_type", chargeType);

    return mapping.findForward("doctorChargesList");
  }

  /**
   * Gets the OT charges list screen.
   *
   * @param mapping  the mapping
   * @param form     the form
   * @param request  the request
   * @param response the response
   * @return the OT charges list screen
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws Exception   the exception
   */
  public ActionForward getOtChargesListScreen(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws IOException, Exception {

    Map<String, String[]> requestParams = request.getParameterMap();
    String orgId = request.getParameter("org_id");
    String chargeType = request.getParameter("_chargeType");
    if ((chargeType == null) || chargeType.isEmpty()) {
      chargeType = "min_charge";
    }

    PagedList list = theatreMasterDao.search(requestParams,
        ConversionUtils.getListingParameter(requestParams));

    List<String> ids = new ArrayList<String>();
    for (Map obj : (List<Map>) list.getDtoList()) {
      ids.add((String) obj.get("theatre_id"));
    }

    List<String> bedTypes = bedMasterDao.getUnionOfBedTypes();
    List chargeList = theatreMasterDao.getAllChargesForOrg(orgId, ids);
    Map chargesMap = ConversionUtils.listBeanToMapMapBean(chargeList, "theatre_id", "bed_type");

    String orgName = (String) request.getParameter("org_name");

    JSONSerializer js = new JSONSerializer().exclude("class");
    request.setAttribute("pagedList", list);
    request.setAttribute("bedTypes", bedTypes);
    request.setAttribute("charges", chargesMap);
    request.setAttribute("org_id", orgId);
    request.setAttribute("org_name", orgName);
    request.setAttribute("chargeType", chargeType);

    return mapping.findForward("otChargesList");
  }

  /**
   * Gets the overide charges screen.
   *
   * @param mapping  the mapping
   * @param form     the form
   * @param request  the request
   * @param response the response
   * @return the overide charges screen
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws Exception   the exception
   */
  public ActionForward getOverideChargesScreen(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws IOException, Exception {
    String orgId = (String) request.getParameter("org_id");
    String chargeCategory = (String) request.getParameter("chargeCategory");
    List<String> bedTypes = bedMasterDao.getUnionOfBedTypes();
    String overideCharges = null;
    List chargeList = null;
    List pkgItemChargeList = null;

    if (chargeCategory.equals("anesthesia")) {

      String id = (String) request.getParameter("anesthesia_type_id");
      BasicDynaBean bean = anaesthesiaTypeMasterDao.anaesthesiaTypeDetails(id, orgId);
      chargeList = anaesthesiaTypeChargesDao.getAllChargesForOrgAnaesthesiaType(orgId, id);
      request.setAttribute("bean", bean);
      overideCharges = "anaesthesiaChargesOveride";

    } else if (chargeCategory.equals("consultation")) {

      String id = (String) request.getParameter("consultation_type_id");
      BasicDynaBean bean = consultationTypeDao.consultationTypeOrgDetails(Integer.parseInt(id),
          orgId);
      chargeList = consultationChargesDao.getAllChargesForOrg(orgId, id);
      request.setAttribute("bean", bean);
      overideCharges = "consultationChargesOveride";

    } else if (chargeCategory.equals("diagnostics")) {

      String id = (String) request.getParameter("test_id");
      BasicDynaBean bean = AddTestDAOImpl.getTestOrgDetails(id, orgId);
      chargeList = AddTestDAOImpl.getAllChargesForOrg(orgId, id);
      request.setAttribute("bean", bean);
      overideCharges = "diagnosticChargesOveride";
    } else if (chargeCategory.equals("dynapackages")) {

      String id = (String) request.getParameter("dyna_package_id");
      DynaPackageCategoryMasterDAO catdao = new DynaPackageCategoryMasterDAO();
      List<BasicDynaBean> categories = catdao.getCategories();
      BasicDynaBean bean = dynaPackageDao.getDynaPackageDetailsBean(Integer.parseInt(id), orgId);
      chargeList = dynaPackageChargesDao.getAllChargesForOrg(orgId, id, categories);
      request.setAttribute("bean", bean);
      request.setAttribute("categories", ConversionUtils.listBeanToListMap(categories));
      overideCharges = "dynaPackChargesOveride";
    } else if (chargeCategory.equals("operations")) {

      String id = (String) request.getParameter("op_id");
      BasicDynaBean bean = operationMasterDao.getOperationDetails(id, orgId);
      chargeList = operationChargeDao.getAllChargesForOrgOperation(orgId, id);
      request.setAttribute("bean", bean);
      overideCharges = "operationChargesOveride";
    } else if (chargeCategory.equals("packages")) {
      String id = (String) request.getParameter("package_id");
      BasicDynaBean bean = PackageDAO.packageOrgDetails(Integer.parseInt(id), orgId);
      boolean isMultiVisitPackage = (Boolean) bean.get("multi_visit_package");
      chargeList = PackageDAO.getAllChargesForOrg(orgId, id);
      pkgItemChargeList = packageChargeDao.getPackageItemChargesForOrg(id, orgId);
      request.setAttribute("isMultiVisitPack", isMultiVisitPackage);
      request.setAttribute("pkgItemCharges",
          ConversionUtils.listBeanToMapMapBean(pkgItemChargeList, "bed_type", "pack_ob_id"));
      List<BasicDynaBean> packageItemsList = packageChargeDao.getPackageItemsList(id);
      request.setAttribute("pkgItemList", ConversionUtils.listBeanToListMap(packageItemsList));
      request.setAttribute("bedTypesLengthJson",
          new JSONSerializer().exclude("class").serialize(bedTypes.size()));
      request.setAttribute("bedTypesLength", bedTypes.size());
      request.setAttribute("isMultiVisitPackageJson",
          new JSONSerializer().serialize(isMultiVisitPackage));
      request.setAttribute("packageItemsLength",
          new JSONSerializer().exclude("class").serialize(packageItemsList.size()));
      request.setAttribute("bean", bean);
      overideCharges = "packageChargesOveride";
    } else if (chargeCategory.equals("services")) {

      String id = (String) request.getParameter("service_id");
      BasicDynaBean bean = serviceMasterDao.getServiceDetails(id, orgId);
      request.setAttribute("bean", bean);
      chargeList = serviceChargeDao.getAllChargesForOrg(orgId, id);
      overideCharges = "serviceChargesOveride";
    } else if (chargeCategory.equals("doctor")) {

      String id = (String) request.getParameter("doctor_id");
      BasicDynaBean bean = doctorMasterDao.getAllChargesForEdit(orgId, id, "update");
      request.setAttribute("bean", bean);
      chargeList = doctorChargeDao.getAllChargesForOrgDoctor(orgId, id);
      overideCharges = "doctorChargesOverride";
    } else if (chargeCategory.equals("equipment")) {

      String id = (String) request.getParameter("equip_id");
      BasicDynaBean bean = equipmentMasterDao.getEquipmentDetails(id, orgId);
      request.setAttribute("bean", bean);
      chargeList = equipmentChargeDao.getAllChargesForOrgEquipment(orgId, id);
      overideCharges = "equipmentChargesOverride";
      request.setAttribute("org_name", request.getParameter("org_name"));
    } else if (chargeCategory.equals("operationTheatre")) {

      String id = (String) request.getParameter("theatre_id");
      GenericDAO otDao = new GenericDAO("theatre_master");
      BasicDynaBean bean = otDao.findByKey("theatre_id", id);
      chargeList = theatreMasterDao.getAllChargesForOrgOT(orgId, id);
      request.setAttribute("bean", bean);
      overideCharges = "otChargesOveride";
      request.setAttribute("org_name", request.getParameter("org_name"));
    }

    if (chargeCategory.equals("dynapackages")) {
      request.setAttribute("charges",
          ConversionUtils.listBeanToMapMapBean(chargeList, "bed_type", "dyna_pkg_cat_id"));
    } else {
      request.setAttribute("charges", ConversionUtils.listBeanToMapMap(chargeList, "bed_type"));
    }

    request.setAttribute("bedTypes", bedTypes);
    request.setAttribute("org_id", orgId);

    String fromItemMaster = (String) request.getParameter("fromItemMaster");
    String baseRateSheet = (String) request.getParameter("baseRateSheet");
    request.setAttribute("fromItemMaster", fromItemMaster);
    request.setAttribute("baseRateSheet", baseRateSheet);
    return mapping.findForward(overideCharges);

  }

  /**
   * Gets the registration charges screen.
   *
   * @param mapping  the mapping
   * @param form     the form
   * @param request  the request
   * @param response the response
   * @return the registration charges screen
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws Exception   the exception
   */
  public ActionForward getRegistrationChargesScreen(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws IOException, Exception {
    String orgId = (String) request.getParameter("org_id");
    String orgName = (String) request.getParameter("org_name");

    List bedTypes = BedMasterDAO.getUnionOfBedTypes();
    List<BasicDynaBean> beans = registrationChargesDao.getRegistrationChargesBeans(orgId);

    request.setAttribute("bedTypes", bedTypes);
    request.setAttribute("beans", beans);
    request.setAttribute("org_id", orgId);
    request.setAttribute("org_name", orgName);
    request.setAttribute("fromItemMaster", (String) request.getParameter("fromItemMaster"));
    request.setAttribute("baseRateSheet", (String) request.getParameter("baseRateSheet"));

    return mapping.findForward("registrationChargesOverride");
  }

  /**
   * Over ride anesthesia charges.
   *
   * @param mapping  the mapping
   * @param form     the form
   * @param request  the request
   * @param response the response
   * @return the action forward
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws Exception   the exception
   */
  public ActionForward overRideAnesthesiaCharges(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws IOException, Exception {

    String anesthesiaId = (String) request.getParameter("anesthesia_type_id");
    ActionRedirect redirect = new ActionRedirect(mapping.findForward("overideRedirect"));
    overRideCharges(request, "anesthesia_type_id", anesthesiaId, "anesthesia",
        "anesthesia_type_charges", "anesthesia_type_org_details", redirect);
    return redirect;
  }

  /**
   * Over ride consultation charges.
   *
   * @param mapping  the mapping
   * @param form     the form
   * @param request  the request
   * @param response the response
   * @return the action forward
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws Exception   the exception
   */
  public ActionForward overRideConsultationCharges(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws IOException, Exception {
    String consultationTypeId = (String) request.getParameter("consultation_type_id");
    ActionRedirect redirect = new ActionRedirect(mapping.findForward("overideRedirect"));
    overRideCharges(request, "consultation_type_id", consultationTypeId, "consultation",
        "consultation_charges", "consultation_org_details", redirect);
    return redirect;
  }

  /**
   * Over ride diagnostic charges.
   *
   * @param mapping  the mapping
   * @param form     the form
   * @param request  the request
   * @param response the response
   * @return the action forward
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws Exception   the exception
   */
  public ActionForward overRideDiagnosticCharges(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws IOException, Exception {

    String testId = (String) request.getParameter("test_id");

    ActionRedirect redirect = new ActionRedirect(mapping.findForward("overideRedirect"));
    overRideCharges(request, "test_id", testId, "diagnostics", "diagnostic_charges",
        "test_org_details", redirect);
    return redirect;
  }

  /**
   * Over ride dyna pack charges.
   *
   * @param mapping  the mapping
   * @param form     the form
   * @param request  the request
   * @param response the response
   * @return the action forward
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws Exception   the exception
   */
  public ActionForward overRideDynaPackCharges(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws IOException, Exception {
    String dynaPackId = (String) request.getParameter("dyna_package_id");
    ActionRedirect redirect = new ActionRedirect(mapping.findForward("overideRedirect"));
    overRideCharges(request, "dyna_package_id", dynaPackId, "dynapackages", "dyna_package_charges",
        "dyna_package_org_details", redirect);
    overRideDynaPackCategoryLimits(request);
    return redirect;
  }

  /**
   * Over ride operation charges.
   *
   * @param mapping  the mapping
   * @param form     the form
   * @param request  the request
   * @param response the response
   * @return the action forward
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws Exception   the exception
   */
  public ActionForward overRideOperationCharges(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws IOException, Exception {

    String opId = (String) request.getParameter("op_id");
    ActionRedirect redirect = new ActionRedirect(mapping.findForward("overideRedirect"));
    overRideCharges(request, "op_id", opId, "operations", "operation_charges",
        "operation_org_details", redirect);
    return redirect;
  }

  /**
   * Over ride package charges.
   *
   * @param mapping  the mapping
   * @param form     the form
   * @param request  the request
   * @param response the response
   * @return the action forward
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws Exception   the exception
   */
  public ActionForward overRidePackageCharges(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws IOException, Exception {
    String packageId = (String) request.getParameter("package_id");
    ActionRedirect redirect = new ActionRedirect(mapping.findForward("overideRedirect"));
    overRideCharges(request, "package_id", packageId, "packages", "package_charges",
        "pack_org_details", redirect);
    BasicDynaBean bean = packMasterDao.findByKey("package_id", Integer.parseInt(packageId));
    boolean isMultiVisitPackage = (Boolean) bean.get("multi_visit_package");
    if (isMultiVisitPackage) {
      overRidePackageItemCharges(request, packageId);
    }
    return redirect;
  }

  /**
   * Over ride service charges.
   *
   * @param mapping  the mapping
   * @param form     the form
   * @param request  the request
   * @param response the response
   * @return the action forward
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws Exception   the exception
   */
  public ActionForward overRideServiceCharges(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws IOException, Exception {
    String serviceId = (String) request.getParameter("service_id");
    ActionRedirect redirect = new ActionRedirect(mapping.findForward("overideRedirect"));
    overRideCharges(request, "service_id", serviceId, "services", "service_master_charges",
        "service_org_details", redirect);
    return redirect;
  }

  /**
   * Over ride equipment charges.
   *
   * @param mapping  the mapping
   * @param form     the form
   * @param request  the request
   * @param response the response
   * @return the action forward
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws Exception   the exception
   */
  public ActionForward overRideEquipmentCharges(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws IOException, Exception {
    String orgId = (String) request.getParameter("org_id");
    String orgName = (String) request.getParameter("org_name");
    GenericDAO odao = new GenericDAO("equip_org_details");
    ArrayList errors = new ArrayList();
    ActionRedirect redirect = new ActionRedirect(mapping.findForward("overideRedirect"));

    String baseRateSheet = (String) request.getParameter("baseRateSheet");
    String fromItemMaster = (String) request.getParameter("fromItemMaster");

    String equipId = (String) request.getParameter("equip_id");
    BasicDynaBean orgDetails = odao.getBean();
    orgDetails.set("equip_id", equipId);
    orgDetails.set("org_id", orgId);
    orgDetails.set("is_override", "Y");
    orgDetails.set("applicable", true);

    String[] beds = request.getParameterValues("bed_type");
    List<BasicDynaBean> chargeList = new ArrayList();
    for (int i = 0; i < beds.length; i++) {
      BasicDynaBean charge = equipmentChargeDao.getBean();
      ConversionUtils.copyToDynaBean(request.getParameterMap(), charge, errors);
      ConversionUtils.copyIndexToDynaBean(request.getParameterMap(), i, charge, errors);
      chargeList.add(charge);
    }
    Connection con = null;
    boolean success = false;
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);

      odao.updateWithNames(con, orgDetails.getMap(), new String[] { "equip_id", "org_id" });
      for (BasicDynaBean c : chargeList) {
        c.set("is_override", "Y");
        equipmentChargeDao.updateWithNames(con, c.getMap(),
            new String[] { "equip_id", "org_id", "bed_type" });
      }
      success = true;
      redirect.addParameter("org_id", orgId);
      redirect.addParameter("org_name", orgName);
      redirect.addParameter("equip_id", request.getParameter("equip_id"));
      redirect.addParameter("chargeCategory", "equipment");
      redirect.addParameter("baseRateSheet", baseRateSheet);
      redirect.addParameter("fromItemMaster", fromItemMaster);
    } finally {
      DataBaseUtil.commitClose(con, success);
    }
    return redirect;
  }

  /**
   * Over ride OT charges.
   *
   * @param mapping  the mapping
   * @param form     the form
   * @param request  the request
   * @param response the response
   * @return the action forward
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws Exception   the exception
   */
  public ActionForward overRideOtCharges(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws IOException, Exception {

    GenericDAO odao = new GenericDAO("theatre_org_details");

    ActionRedirect redirect = new ActionRedirect(mapping.findForward("overideRedirect"));
    String orgId = request.getParameter("org_id");
    String orgName = request.getParameter("org_name");
    ArrayList errors = new ArrayList();

    String baseRateSheet = (String) request.getParameter("baseRateSheet");
    String fromItemMaster = (String) request.getParameter("fromItemMaster");

    BasicDynaBean orgDetails = odao.getBean();
    String theatreId = (String) request.getParameter("theatre_id");
    orgDetails.set("theatre_id", theatreId);
    orgDetails.set("org_id", orgId);
    orgDetails.set("is_override", "Y");
    orgDetails.set("applicable", true);

    String[] beds = request.getParameterValues("bed_type");
    List<BasicDynaBean> chargeList = new ArrayList();
    for (int i = 0; i < beds.length; i++) {
      BasicDynaBean charge = theatreMasterDao.getBean();
      ConversionUtils.copyToDynaBean(request.getParameterMap(), charge, errors);
      ConversionUtils.copyIndexToDynaBean(request.getParameterMap(), i, charge, errors);
      chargeList.add(charge);
    }
    Connection con = null;
    boolean success = false;
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      odao.updateWithNames(con, orgDetails.getMap(), new String[] { "theatre_id", "org_id" });
      for (BasicDynaBean c : chargeList) {
        c.set("is_override", "Y");
        theatreMasterDao.updateWithNames(con, c.getMap(),
            new String[] { "theatre_id", "org_id", "bed_type" });
      }
      success = true;
      redirect.addParameter("org_id", orgId);
      redirect.addParameter("org_name", orgName);
      redirect.addParameter("theatre_id", request.getParameter("theatre_id"));
      redirect.addParameter("chargeCategory", "operationTheatre");
      redirect.addParameter("baseRateSheet", baseRateSheet);
      redirect.addParameter("fromItemMaster", fromItemMaster);

      return redirect;
    } finally {
      DataBaseUtil.commitClose(con, success);
    }
  }

  /**
   * Over ride charges.
   *
   * @param request         the request
   * @param categoryId      the category id
   * @param categoryIdValue the category id value
   * @param chargeCategory  the charge category
   * @param chargeTbl       the charge tbl
   * @param orgTbl          the org tbl
   * @param redirect        the redirect
   * @return true, if successful
   * @throws SQLException the SQL exception
   * @throws IOException  Signals that an I/O exception has occurred.
   */
  public boolean overRideCharges(HttpServletRequest request, String categoryId,
      String categoryIdValue, String chargeCategory, String chargeTbl, String orgTbl,
      ActionRedirect redirect) throws SQLException, IOException {
    HttpSession session = request.getSession();
    String userName = (String) session.getAttribute("userid");
    String baseRateSheet = (String) request.getParameter("baseRateSheet");
    String fromItemMaster = (String) request.getParameter("fromItemMaster");
    boolean success = false;
    String orgId = (String) request.getParameter("org_id");
    Connection con = null;
    ArrayList errors = new ArrayList();
    GenericDAO cdao = new GenericDAO(chargeTbl);
    GenericDAO odao = new GenericDAO(orgTbl);
    String[] beds = request.getParameterValues("bed_type");
    List<BasicDynaBean> chargeList = new ArrayList();

    for (int i = 0; i < beds.length; i++) {
      BasicDynaBean charge = cdao.getBean();
      ConversionUtils.copyToDynaBean(request.getParameterMap(), charge, errors);
      ConversionUtils.copyIndexToDynaBean(request.getParameterMap(), i, charge, errors);
      chargeList.add(charge);
    }
    BasicDynaBean orgDetails = odao.getBean();
    ConversionUtils.copyToDynaBean(request.getParameterMap(), orgDetails, errors);
    if (chargeCategory.equals("consultation") || chargeCategory.equals("dynapackages")
        || chargeCategory.equals("packages")) {
      orgDetails.set(categoryId, Integer.parseInt(categoryIdValue));
    } else {
      if (chargeCategory.equals("operations")) {
        orgDetails.set("operation_id", categoryIdValue);
      } else {
        orgDetails.set(categoryId, categoryIdValue);
      }
    }

    orgDetails.set("is_override", "Y");
    orgDetails.set("applicable", true);

    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);

      for (BasicDynaBean c : chargeList) {
        c.set("is_override", "Y");
        if (!(chargeCategory.equals("anesthesia") || chargeCategory.equals("packages"))) {
          c.set("username", userName);
        }

        if (chargeCategory.equals("diagnostics")) {
          cdao.updateWithNames(con, c.getMap(),
              new String[] { categoryId, "org_name", "bed_type" });
        } else {
          cdao.updateWithNames(con, c.getMap(), new String[] { categoryId, "org_id", "bed_type" });
        }
      }
      if (chargeCategory.equals("operations")) {
        odao.updateWithNames(con, orgDetails.getMap(), new String[] { "operation_id", "org_id" });
      } else {
        odao.updateWithNames(con, orgDetails.getMap(), new String[] { categoryId, "org_id" });
      }

      success = true;
      redirect.addParameter("org_id", orgId);
      redirect.addParameter(categoryId, request.getParameter(categoryId));
      redirect.addParameter("chargeCategory", chargeCategory);
      redirect.addParameter("baseRateSheet", baseRateSheet);
      redirect.addParameter("fromItemMaster", fromItemMaster);

    } finally {
      DataBaseUtil.commitClose(con, success);
    }
    return success;
  }

  /**
   * Over ride dyna pack category limits.
   *
   * @param req the req
   * @return true, if successful
   * @throws SQLException   the SQL exception
   * @throws ParseException the parse exception
   * @throws IOException    Signals that an I/O exception has occurred.
   */
  public boolean overRideDynaPackCategoryLimits(HttpServletRequest req)
      throws SQLException, ParseException, IOException {

    HttpSession session = req.getSession();
    String userName = (String) session.getAttribute("userid");
    String[] beds = req.getParameterValues("bed_type");
    ArrayList errors = new ArrayList();

    DynaPackageOrgDAO odao = new DynaPackageOrgDAO();
    DynaPackageCategoryLimitsDAO limitdao = new DynaPackageCategoryLimitsDAO();

    BasicDynaBean dynaPackageBean = dynaPackageDao.getBean();
    ConversionUtils.copyToDynaBean(req.getParameterMap(), dynaPackageBean, errors, true);
    BasicDynaBean orgDetails = odao.getBean();
    ConversionUtils.copyToDynaBean(req.getParameterMap(), orgDetails, errors);

    dynaPackageBean.set("username", userName);

    String[] categoryIds = req.getParameterValues("dyna_pkg_cat_id");
    Map<String, String[]> from = req.getParameterMap();
    List<BasicDynaBean> limitsList = new ArrayList();

    if (categoryIds != null && categoryIds.length > 0) {
      for (int j = 0; j < categoryIds.length; j++) {
        for (int i = 0; i < beds.length; i++) {
          BasicDynaBean charge = limitdao.getBean();

          charge.set("dyna_package_id", (Integer) dynaPackageBean.get("dyna_package_id"));
          charge.set("dyna_pkg_cat_id", new Integer(categoryIds[j]));
          charge.set("bed_type", beds[i]);
          charge.set("org_id", (String) orgDetails.get("org_id"));
          charge.set("is_override", "Y");

          Object[] object = (Object[]) from.get(categoryIds[j] + "." + "pkg_included");
          if (object != null && object.length > i && object[i] != null) {
            charge.set("pkg_included", new String(object[i].toString()));
          }

          object = (Object[]) from.get(categoryIds[j] + "." + "amount_limit");
          if (object != null && object.length > i && object[i] != null) {
            if (object[i].toString().trim().equals("")) {
              charge.set("amount_limit", new BigDecimal(0));
            } else {
              charge.set("amount_limit", new BigDecimal(object[i].toString()));
            }
          }

          object = (Object[]) from.get(categoryIds[j] + "." + "qty_limit");
          if (object != null && object.length > i && object[i] != null) {
            if (object[i].toString().trim().equals("")) {
              charge.set("qty_limit", new BigDecimal(0));
            } else {
              charge.set("qty_limit", new BigDecimal(object[i].toString()));
            }
          }

          charge.set("username", userName);
          limitsList.add(charge);
        }
      }
    }

    Connection con = null;
    boolean success = false;
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      odao.updateWithNames(con, orgDetails.getMap(), new String[] { "dyna_package_id", "org_id" });

      for (BasicDynaBean lmt : limitsList) {
        limitdao.updateWithNames(con, lmt.getMap(),
            new String[] { "dyna_package_id", "dyna_pkg_cat_id", "org_id", "bed_type" });
      }
      success = true;
    } finally {
      DataBaseUtil.commitClose(con, success);
    }
    return success;
  }

  /**
   * Over ride doctor charges.
   *
   * @param mapping  the mapping
   * @param form     the form
   * @param request  the request
   * @param response the response
   * @return the action forward
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws Exception   the exception
   */
  public ActionForward overRideDoctorCharges(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws IOException, Exception {
    String doctorId = (String) request.getParameter("doctor_id");
    GenericDAO opChargesdao = new GenericDAO("doctor_op_consultation_charge");
    GenericDAO odao = new GenericDAO("doctor_org_details");
    ArrayList errors = new ArrayList();

    BasicDynaBean doctorOp = opChargesdao.getBean();
    BasicDynaBean doctorIp = doctorChargeDao.getBean();
    BasicDynaBean orgDetails = odao.getBean();

    ConversionUtils.copyToDynaBean(request.getParameterMap(), doctorOp, errors);
    doctorIp.set("doctor_name", doctorId);

    String orgId = (String) request.getParameter("org_id");

    orgDetails.set("doctor_id", doctorId);
    orgDetails.set("org_id", orgId);
    orgDetails.set("is_override", "Y");
    orgDetails.set("applicable", true);

    String[] beds = request.getParameterValues("bed_type");
    List<BasicDynaBean> chargeList = new ArrayList();
    for (int i = 0; i < beds.length; i++) {
      BasicDynaBean charge = doctorChargeDao.getBean();
      ConversionUtils.copyToDynaBean(request.getParameterMap(), charge, errors);
      ConversionUtils.copyIndexToDynaBean(request.getParameterMap(), i, charge, errors);
      charge.set("doctor_name", doctorId);
      chargeList.add(charge);
    }

    Connection con = null;
    boolean success = false;
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);

      odao.updateWithNames(con, orgDetails.getMap(), new String[] { "doctor_id", "org_id" });

      doctorOp.set("is_override", "Y");
      success = (1 == opChargesdao.updateWithNames(con, doctorOp.getMap(),
          new String[] { "doctor_id", "org_id" }));

      if (success) {
        for (BasicDynaBean c : chargeList) {
          c.set("is_override", "Y");
          doctorChargeDao.updateWithNames(con, c.getMap(),
              new String[] { "doctor_name", "organization", "bed_type" });
        }
      }
    } finally {
      DataBaseUtil.commitClose(con, success);
    }

    String orgName = (String) request.getParameter("org_name");
    ActionRedirect redirect = new ActionRedirect(mapping.findForward("overideRedirect"));
    String baseRateSheet = (String) request.getParameter("baseRateSheet");
    String fromItemMaster = (String) request.getParameter("fromItemMaster");

    redirect.addParameter("org_id", orgId);
    redirect.addParameter("org_name", orgName);
    redirect.addParameter("doctor_id", doctorId);
    redirect.addParameter("chargeCategory", "doctor");
    redirect.addParameter("baseRateSheet", baseRateSheet);
    redirect.addParameter("fromItemMaster", fromItemMaster);
    return redirect;
  }

  /**
   * Over ride registration charges.
   *
   * @param mapping  the mapping
   * @param form     the form
   * @param request  the request
   * @param response the response
   * @return the action forward
   * @throws SQLException the SQL exception
   * @throws Exception    the exception
   */
  public ActionForward overRideRegistrationCharges(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws SQLException, Exception {

    Connection con = null;
    boolean success = false;
    String orgId = request.getParameter("org_id");
    String orgName = request.getParameter("org_name");
    List errors = new ArrayList();
    String[] beds = request.getParameterValues("beds");

    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      BasicDynaBean bean = registrationChargesDao.getBean();
      for (int i = 0; i < beds.length; i++) {
        Map keys = new HashMap<String, String>();
        keys.put("org_id", orgId);
        keys.put("bed_type", beds[i]);
        bean.set("is_override", "Y");
        ConversionUtils.copyIndexToDynaBean(request.getParameterMap(), i, bean, errors);
        success = registrationChargesDao.update(con, bean.getMap(), keys) > 0;
      }
    } finally {
      DataBaseUtil.commitClose(con, success);

    }
    ActionRedirect redirect = new ActionRedirect(
        mapping.findForward("registrationOverrideRedirect"));
    redirect.addParameter("org_id", orgId);
    redirect.addParameter("org_name", orgName);
    redirect.addParameter("baseRateSheet", (String) request.getParameter("baseRateSheet"));
    redirect.addParameter("fromItemMaster", (String) request.getParameter("fromItemMaster"));
    return redirect;
  }

  /**
   * Over ride package item charges.
   *
   * @param request   the request
   * @param packageId the package id
   * @return true, if successful
   * @throws SQLException the SQL exception
   * @throws Exception    the exception
   */
  public boolean overRidePackageItemCharges(HttpServletRequest request, String packageId)
      throws SQLException, Exception {

    String[] beds = request.getParameterValues("bed_type_for_item");
    String[] packObids = request.getParameterValues("pack_ob_id");
    String[] itemCharges = request.getParameterValues("pack_item_charge");
    String orgId = (String) request.getParameter("org_id");
    BasicDynaBean bean = packageItemChargesDao.getBean();
    int idx = 0;
    Connection con = null;
    boolean success = false;
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      for (int i = 0; i < packObids.length; i++) {
        for (int j = 0; j < beds.length; j++) {
          Map<String, Object> keys = new HashMap<String, Object>();
          keys.put("package_id", Integer.parseInt(packageId));
          keys.put("pack_ob_id", packObids[i]);
          keys.put("bed_type", beds[j]);
          keys.put("org_id", orgId);
          bean.set("charge", new BigDecimal(itemCharges[idx]));
          bean.set("is_override", "Y");
          success = packageItemChargesDao.update(con, bean.getMap(), keys) > 0;
          idx++;
        }
      }
    } finally {
      DataBaseUtil.commitClose(con, success);
    }
    return success;
  }

}
