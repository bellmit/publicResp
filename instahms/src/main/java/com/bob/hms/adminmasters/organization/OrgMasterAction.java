package com.bob.hms.adminmasters.organization;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.PagedList;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The Class OrgMasterAction.
 */
public class OrgMasterAction extends DispatchAction {

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(OrgMasterAction.class);

  /** The dao. */
  private static OrgMasterDao dao = new OrgMasterDao();
  private static RateMasterDao rdao = new RateMasterDao();
  private static RateSheetSchedulerDetailsDAO rateSheetSchedulerDetailsDao = 
      new RateSheetSchedulerDetailsDAO();

  /**
   * Gets the organization details.
   *
   * @param mapping  the mapping
   * @param form     the form
   * @param request  the request
   * @param response the response
   * @return the organization details
   * @throws IOException      Signals that an I/O exception has occurred.
   * @throws ServletException the servlet exception
   * @throws Exception        the exception
   */
  public ActionForward getOrganizationDetails(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException, Exception {

    setAttributes(request);

    return mapping.findForward("OnLoadOrgDetails");
  }

  /**
   * Gets the new organiaztion screen.
   *
   * @param mapping  the mapping
   * @param form     the form
   * @param request  the request
   * @param response the response
   * @return the new organiaztion screen
   * @throws IOException      Signals that an I/O exception has occurred.
   * @throws ServletException the servlet exception
   * @throws Exception        the exception
   */
  public ActionForward getNewOrganiaztionScreen(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException, Exception {

    String cessApplicable = null;
    String vatApplicable = null;
    ArrayList al = dao.getOrganiaztions();
    OrgMasterForm oform = (OrgMasterForm) form;
    BasicDynaBean prefBean = OrgMasterDao.getVatAndCessValues();
    if (prefBean != null) {
      cessApplicable = (String) prefBean.get("cess_applicable");
      vatApplicable = (String) prefBean.get("vat_applicable");
    }

    request.setAttribute("organizations", (al));
    request.setAttribute("cessApplicable", cessApplicable);
    request.setAttribute("vatApplicable", vatApplicable);
    request.setAttribute("_method", "saveNewOrganization");
    return mapping.findForward("getNewOrganizationScreen");
  }

  /**
   * Save new organization.
   *
   * @param mapping  the mapping
   * @param af       the af
   * @param request  the request
   * @param response the response
   * @return the action forward
   * @throws IOException      Signals that an I/O exception has occurred.
   * @throws ServletException the servlet exception
   * @throws Exception        the exception
   */
  public ActionForward saveNewOrganization(ActionMapping mapping, ActionForm af,
      HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException, Exception {

    OrgMasterForm form = (OrgMasterForm) af;
    Organization org = new Organization();

    // &sortOrder=org_name&sortReverse=false&status=A
    org.setOrgName(form.getOrgName());
    org.setAddress(form.getAddress());
    org.setContactPerson(form.getContactPerson());
    org.setEmail(form.getEmail());
    org.setStatus(form.getStatus());
    org.setPhone(form.getPhone());

    org.setOpconsvisitcode(form.getOpconsvisitcode());
    org.setOpconsrevisitcode(form.getOpconsrevisitcode());
    org.setPrivateconsvisitcode(form.getPrivateconsvisitcode());
    org.setPrivateconsrevisitcode(form.getPrivateconsrevisitcode());
    org.setDutyconsvisitcode(form.getDutyconsvisitcode());
    org.setDutyconsrevisitcode(form.getDutyconsrevisitcode());
    org.setSplconsvisitcode(form.getSplconsvisitcode());
    org.setSplconsrevisitcode(form.getSplconsrevisitcode());
    org.setDiscperc(form.getDiscperc());
    org.setDiscType(form.getDiscType());
    org.setHasDateValidity(form.getHasDateValidity());
    org.setFromDate(form.getFromDate());
    org.setToDate(form.getToDate());
    org.setRateVariation(form.getRateVariation());
    org.setEligible_to_earn_points(form.getEligible_to_earn_points());
    org.setStore_rate_plan_id(form.getStore_rate_plan_id());

    boolean useValue = false;
    Double varianceValue = new Double("0.00").doubleValue();
    String varianceType = "Incr";
    Double varianceBy = ((null != form.getVarianceBy()) ? form.getVarianceBy()
        : new Double("0.00").doubleValue());
    String baseOrgId = form.getBaseOrgId();
    Double nearstRoundOfValue = ((null != form.getNearsetRoundofValue())
        ? form.getNearsetRoundofValue()
        : new Double("0.0").doubleValue());

    /*
     * if(varianceValue != null && varianceValue.doubleValue() == 0.0){ useValue = false; }
     */

    OrgMasterBo bo = new OrgMasterBo();

    String orgId = bo.saveNewOrganization(org, varianceType, varianceValue, varianceBy, useValue,
        baseOrgId, nearstRoundOfValue, (String) request.getSession(false).getAttribute("userid"));
    ActionRedirect redirect = new ActionRedirect(mapping.findForward("redirectDashboard"));
    redirect.addParameter("sortOrder", "org_name");
    redirect.addParameter("sortReverse", false);
    redirect.addParameter("status", "A");

    return redirect;
  }

  /**
   * Sets the attributes.
   *
   * @param request the new attributes
   * @throws Exception the exception
   */
  private void setAttributes(HttpServletRequest request) throws Exception {

    Map<String, String[]> requestParams = request.getParameterMap();
    Map<LISTING, Object> pagingParams = ConversionUtils.getListingParameter(requestParams);
    PagedList pagedList = dao.getOrgDetailPages(requestParams, pagingParams);
    request.setAttribute("pagedList", pagedList);
    request.setAttribute("createdJobList", ConversionUtils
        .copyListDynaBeansToMap(rateSheetSchedulerDetailsDao.getRecentRateSheetSchedulerDetails()));
  }

  /**
   * Edits the organiaztion details.
   *
   * @param mapping  the mapping
   * @param af       the af
   * @param request  the request
   * @param response the response
   * @return the action forward
   * @throws IOException      Signals that an I/O exception has occurred.
   * @throws ServletException the servlet exception
   * @throws Exception        the exception
   */
  public ActionForward editOrganiaztionDetails(ActionMapping mapping, ActionForm af,
      HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException, Exception {

    String cessApplicable = null;
    String vatApplicable = null;
    String orgId = request.getParameter("orgId");
    ArrayList<Hashtable<String, String>> list = dao.getOrgdetails(orgId);
    request.setAttribute("orgDetails", dao.findByKey("org_id", orgId));

    BasicDynaBean prefBean = OrgMasterDao.getVatAndCessValues();
    if (prefBean != null) {
      cessApplicable = (String) prefBean.get("cess_applicable");
      vatApplicable = (String) prefBean.get("vat_applicable");
    }

    Iterator<Hashtable<String, String>> it = list.iterator();
    OrgMasterForm form = (OrgMasterForm) af;
    form.setEditOrgId(orgId);
    while (it.hasNext()) {
      Hashtable<String, String> ht = it.next();
      form.setOrgName(ht.get("ORG_NAME"));
      form.setStatus(ht.get("STATUS"));
      form.setContactPerson(ht.get("ORG_CONTACT_PERSON"));
      form.setPhone(ht.get("ORG_PHONE"));
      form.setEmail(ht.get("ORG_MAILID"));
      form.setAddress(ht.get("ORG_ADDRESS"));

      form.setOpconsvisitcode(ht.get("CONS_CODE"));
      form.setOpconsrevisitcode(ht.get("REVISIT_CODE"));
      form.setPrivateconsvisitcode(ht.get("PRIVATE_CONS_CODE"));
      form.setPrivateconsrevisitcode(ht.get("PRIVATE_CONS_REVISIT_CODE"));
      form.setDutyconsvisitcode(ht.get("MODD_CONS_CODE"));
      form.setDutyconsrevisitcode(ht.get("MODD_CONS_REVISIT_CODE"));
      form.setSplconsvisitcode(ht.get("SPL_CONS_CODE"));
      form.setSplconsrevisitcode(ht.get("SPL_CONS_REVISIT_CODE"));
      form.setHasDateValidity(ht.get("HAS_DATE_VALIDITY").equalsIgnoreCase("t"));
      form.setFromDate(ht.get("VALID_FROM_DATE"));
      form.setToDate(ht.get("VALID_TO_DATE"));
      form.setRateVariation(ht.get("RATE_VARIATION"));
      form.setEligible_to_earn_points(ht.get("ELIGIBLE_TO_EARN_POINTS"));

      if (!ht.get("PHARMACY_DISCOUNT_PERCENTAGE").isEmpty()) {
        form.setDiscperc(Double.parseDouble(ht.get("PHARMACY_DISCOUNT_PERCENTAGE")));
      }
      form.setDiscType(ht.get("PHARMACY_DISCOUNT_TYPE"));
      form.setStore_rate_plan_id(
          ht.get("STORE_RATE_PLAN_ID") == null ? "" : ht.get("STORE_RATE_PLAN_ID"));
      logger.debug("ht.get(STORE_RATE_PLAN_ID)****" + ht.get("STORE_RATE_PLAN_ID"));
    }

    request.setAttribute("anesthesiaTotal",
        rdao.getTotalCount("anesthesia_type_org_details", orgId));
    request.setAttribute("anesthesiaExcluded",
        rdao.getItemsExcludedCount("anesthesia_type_org_details", orgId));

    request.setAttribute("consultationTotal",
        rdao.getTotalCount("consultation_org_details", orgId));
    request.setAttribute("consultationExcluded",
        rdao.getItemsExcludedCount("consultation_org_details", orgId));

    request.setAttribute("testTotal", rdao.getTotalCount("test_org_details", orgId));
    request.setAttribute("testExcluded", rdao.getItemsExcludedCount("test_org_details", orgId));

    request.setAttribute("dynaTotal", rdao.getTotalCount("dyna_package_org_details", orgId));
    request.setAttribute("dynaExcluded",
        rdao.getItemsExcludedCount("dyna_package_org_details", orgId));

    request.setAttribute("opeTotal", rdao.getTotalCount("operation_org_details", orgId));
    request.setAttribute("opeExcluded", rdao.getItemsExcludedCount("operation_org_details", orgId));

    request.setAttribute("packTotal", rdao.getTotalCount("pack_org_details", orgId));
    request.setAttribute("packExcluded", rdao.getItemsExcludedCount("pack_org_details", orgId));

    request.setAttribute("serviceTotal", rdao.getTotalCount("service_org_details", orgId));
    request.setAttribute("serviceExcluded",
        rdao.getItemsExcludedCount("service_org_details", orgId));

    ArrayList al = dao.getOrganiaztions();
    // this is not required,to aviod script problem kept this.just workaroun.need to fix later.
    request.setAttribute("organizations", (al));

    request.setAttribute("_method", "updateOrgDetails");
    request.setAttribute("cessApplicable", cessApplicable);
    request.setAttribute("vatApplicable", vatApplicable);
    return mapping.findForward("getNewOrganizationScreen");
  }

  /**
   * Update org details.
   *
   * @param mapping  the mapping
   * @param af       the af
   * @param request  the request
   * @param response the response
   * @return the action forward
   * @throws IOException      Signals that an I/O exception has occurred.
   * @throws ServletException the servlet exception
   * @throws Exception        the exception
   */
  public ActionForward updateOrgDetails(ActionMapping mapping, ActionForm af,
      HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException, Exception {

    OrgMasterForm form = (OrgMasterForm) af;
    FlashScope flash = FlashScope.getScope(request);
    Organization org = new Organization();

    org.setOrgId(form.getEditOrgId());
    org.setOrgName(form.getOrgName());
    org.setAddress(form.getAddress());
    org.setContactPerson(form.getContactPerson());
    org.setEmail(form.getEmail());
    org.setStatus(form.getStatus());
    org.setPhone(form.getPhone());

    org.setOpconsvisitcode(form.getOpconsvisitcode());
    org.setOpconsrevisitcode(form.getOpconsrevisitcode());
    org.setPrivateconsvisitcode(form.getPrivateconsvisitcode());
    org.setPrivateconsrevisitcode(form.getPrivateconsrevisitcode());
    org.setDutyconsvisitcode(form.getDutyconsvisitcode());
    org.setDutyconsrevisitcode(form.getDutyconsrevisitcode());
    org.setSplconsvisitcode(form.getSplconsvisitcode());
    org.setSplconsrevisitcode(form.getSplconsrevisitcode());
    org.setDiscperc(form.getDiscperc());
    org.setDiscType(form.getDiscType());
    org.setHasDateValidity(form.getHasDateValidity());
    org.setFromDate(form.getFromDate());
    org.setToDate(form.getToDate());
    org.setRateVariation(form.getRateVariation());
    org.setStore_rate_plan_id(form.getStore_rate_plan_id());
    org.setEligible_to_earn_points(form.getEligible_to_earn_points());
    OrgMasterBo bo = new OrgMasterBo();

    if (org.getStatus() != null && org.getStatus().equals("I")) {

      ActionRedirect redirect = new ActionRedirect(mapping.findForward("showRedirect"));
      redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
      redirect.addParameter("orgId", form.getEditOrgId());
      redirect.addParameter("cessApplicable", request.getParameter("cessApplicable"));
      redirect.addParameter("vatApplicable", request.getParameter("vatApplicable"));

      List<BasicDynaBean> patCatDependants = getPatientCategoryDependants(org.getOrgId());
      if (patCatDependants != null && patCatDependants.size() > 0) {
        flash.error("Cannot mark Rate Plan as inactive. <br/>"
            + " One (or) more Patient Category Rate Plans "
            + "(OP Allowed Rate Plans/IP Allowed Rate Plans) are linked with this Rate Plan.");
        return redirect;
      }

      List<BasicDynaBean> insCompDependants = getInsCompDependants(org.getOrgId());
      if (insCompDependants != null && insCompDependants.size() > 0) {
        flash.error(
            "Cannot mark Rate Plan as as inactive. <br/>" + " One (or) more Insurance Company "
                + "Default Rate Plans are linked with this Rate Plan.");
        return redirect;
      }

      List<BasicDynaBean> planDependants = getPlanDependants(org.getOrgId());
      if (planDependants != null && planDependants.size() > 0) {
        flash.error("Cannot mark Rate Plan as as inactive. <br/>"
            + " One (or) more Insurance Plan Default Rate Plans are linked with this Rate Plan.");
        return redirect;
      }
    }

    boolean status = bo.updateORgDetails(org);
    boolean useValue = true;
    Double varianceValue = form.getVarianceValue();
    String varianceType = form.getVariaceType();
    Double varianceBy = form.getVarianceBy();
    String baseOrgId = form.getBaseOrgId();
    Double nearstRoundOfValue = form.getNearsetRoundofValue();

    /*
     * if(varianceValue.doubleValue() == 0.0){ useValue = false; }
     */

    if (status) {
      /*
       * status &= bo.updateExistOrganization(org,varianceType, form.getEditOrgId(),
       * varianceValue,varianceBy,useValue,baseOrgId,nearstRoundOfValue,
       * (String)request.getSession(false).getAttribute("userid")); if (!status) flash.error(
       * "Failed to update Rate Plan..");
       */
    } else {
      flash.error("Duplicate name not allowed");
    }

    if (status) {
      flash.success("Rate Plan updated successfully..");
    }

    ActionRedirect redirect = new ActionRedirect(mapping.findForward("showRedirect"));
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
    redirect.addParameter("orgId", form.getEditOrgId());
    redirect.addParameter("cessApplicable", request.getParameter("cessApplicable"));
    redirect.addParameter("vatApplicable", request.getParameter("vatApplicable"));

    return redirect;
  }

  /** The Constant GET_PATIENT_CATEGORY_DEPENDANTS. */
  public static final String GET_PATIENT_CATEGORY_DEPENDANTS = " SELECT ip_allowed_rate_plans, "
      + " op_allowed_rate_plans, status FROM patient_category_master"
      + " WHERE status = 'A'" + " AND (" + "  ("
      + "    ip_allowed_rate_plans IS NOT NULL AND ip_allowed_rate_plans != '*'"
      + "    AND ? = any(string_to_array(replace(ip_allowed_rate_plans, ' ', ''), ','))"
      + "  ) OR (" + "    op_allowed_rate_plans IS NOT NULL AND op_allowed_rate_plans != '*'"
      + "    AND ? = any(string_to_array(replace(op_allowed_rate_plans, ' ', ''), ','))" + "  )"
      + " )";

  /**
   * Gets the patient category dependants.
   *
   * @param ratePlan the rate plan
   * @return the patient category dependants
   * @throws SQLException the SQL exception
   */
  private List<BasicDynaBean> getPatientCategoryDependants(String ratePlan) throws SQLException {
    return DataBaseUtil.queryToDynaList(GET_PATIENT_CATEGORY_DEPENDANTS,
        new Object[] { ratePlan, ratePlan });
  }

  /** The Constant GET_INS_COMPANY_DEPENDANTS. */
  public static final String GET_INS_COMPANY_DEPENDANTS = " SELECT insurance_co_id,"
      + " insurance_co_name,insurance_co_address,insurance_co_city,"
      + "  insurance_co_state,insurance_co_country,insurance_co_phone,insurance_co_email,"
      + "  default_rate_plan,insurance_co_code_obsolete "
      + " FROM insurance_company_master WHERE default_rate_plan = ? AND status = 'A' ";

  /**
   * Gets the ins comp dependants.
   *
   * @param ratePlan the rate plan
   * @return the ins comp dependants
   * @throws SQLException the SQL exception
   */
  private List<BasicDynaBean> getInsCompDependants(String ratePlan) throws SQLException {
    return DataBaseUtil.queryToDynaList(GET_INS_COMPANY_DEPENDANTS, new Object[] { ratePlan });
  }

  /** The Constant GET_PLAN_DEPENDANTS. */
  public static final String GET_PLAN_DEPENDANTS = " SELECT * FROM insurance_plan_main "
      + " WHERE default_rate_plan = ? AND status = 'A' ";

  /**
   * Gets the plan dependants.
   *
   * @param ratePlan the rate plan
   * @return the plan dependants
   * @throws SQLException the SQL exception
   */
  private List<BasicDynaBean> getPlanDependants(String ratePlan) throws SQLException {
    return DataBaseUtil.queryToDynaList(GET_PLAN_DEPENDANTS, new Object[] { ratePlan });
  }
}
