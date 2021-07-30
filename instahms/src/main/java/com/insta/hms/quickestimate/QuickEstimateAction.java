package com.insta.hms.quickestimate;

import com.bob.hms.adminmasters.organization.OrgMasterDao;
import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.bob.hms.common.Preferences;
import com.bob.hms.common.RequestContext;
import com.insta.hms.adminmasters.bedmaster.BedMasterDAO;
import com.insta.hms.billing.BillBO;
import com.insta.hms.billing.BillChargeTaxDAO;
import com.insta.hms.billing.ChargeDTO;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.HtmlConverter;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.PhoneNumberUtil;
import com.insta.hms.common.StringUtil;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.common.ftl.FtlReportGenerator;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.CommonPrintTemplates.PrintTemplatesDAO;
import com.insta.hms.master.ConsultationTypes.ConsultationTypesDAO;
import com.insta.hms.master.DiscountAuthorizerMaster.DiscountAuthorizerMasterAction;
import com.insta.hms.master.DoctorMaster.DoctorMasterDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.InsuranceCategoryMaster.InsuranceCategoryMasterDAO;
import com.insta.hms.master.InsuranceCompMaster.InsuCompMasterDAO;
import com.insta.hms.master.InsuranceCompanyTPAMaster.InsuranceCompanyTPAMasterDAO;
import com.insta.hms.master.PatientCategory.PatientCategoryDAO;
import com.insta.hms.master.PlanMaster.PlanDetailsDAO;
import com.insta.hms.master.PlanMaster.PlanMasterDAO;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;
import com.insta.hms.master.RegistrationPreferences.RegistrationPreferencesDAO;
import com.insta.hms.master.SalutationMaster.SalutationMasterDAO;
import com.insta.hms.master.TpaMaster.TpaMasterDAO;
import com.lowagie.text.DocumentException;

import flexjson.JSONSerializer;
import freemarker.template.TemplateException;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.http.HttpHeaders;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


// TODO: Auto-generated Javadoc
/**
 * The Class QuickEstimateAction.
 *
 * @author lakshmi.p
 */
public class QuickEstimateAction extends BaseAction {

  /** The log. */
  static Logger log = LoggerFactory.getLogger(QuickEstimateAction.class);

  /** The center master DAO. */
  private static CenterMasterDAO centerMasterDAO = new CenterMasterDAO();

  /** The bill charge tax dao. */
  static BillChargeTaxDAO billChargeTaxDao = new BillChargeTaxDAO();
  
  private static final GenericDAO serviceGroupsDAO = new GenericDAO("service_groups");
  private static final GenericDAO estimateBillDAO = new GenericDAO("estimate_bill");
  private static final GenericDAO estimateChargeDAO = new GenericDAO("estimate_charge");

  /**
   * List.
   *
   * @param mapping
   *          the mapping
   * @param form
   *          the form
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the action forward
   * @throws SQLException
   *           the SQL exception
   * @throws ParseException
   *           the parse exception
   */
  @IgnoreConfidentialFilters
  public ActionForward list(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws SQLException, ParseException {

    JSONSerializer js = new JSONSerializer().exclude("class");
    PagedList pagedList = QuickEstimateDAO.getQuickEstimateList(request.getParameterMap(),
        ConversionUtils.getListingParameter(request.getParameterMap()));
    List<BasicDynaBean> allbedtypes = new BedMasterDAO().getexistingbedtypes(true);   
    request.setAttribute("allbedtypes", allbedtypes);
    request.setAttribute("pagedList", pagedList);
    request.setAttribute("orgNameJSON",
        js.serialize(ConversionUtils.listBeanToListMap(OrgMasterDao.getOrganizations())));
    int centerId = RequestContext.getCenterId();
    if (centerId != 0) {
      request.setAttribute("centerWiseOrgNameJSON",
          js.serialize(ConversionUtils.listBeanToListMap(OrgMasterDao.getOrganizations(centerId))));
    }
    return mapping.findForward("list");
  }

  /**
   * Gets the plan details.
   *
   * @param mapping
   *          the mapping
   * @param form
   *          the form
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the plan details
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws ServletException
   *           the servlet exception
   * @throws SQLException
   *           the SQL exception
   */
  @IgnoreConfidentialFilters
  public ActionForward getPlanDetails(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
          throws IOException, ServletException, SQLException {
    String planId = request.getParameter("plan_id");
    JSONSerializer js = new JSONSerializer().exclude("class");
    response.setContentType("text/plain");
    response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    response.getWriter()
        .write(js.serialize(PlanMasterDAO.getPlanDetails(Integer.parseInt(planId)).getMap()));
    response.flushBuffer();
    return null;
  }

  /**
   * Gets the insurance plan details.
   *
   * @param mapping
   *          the mapping
   * @param form
   *          the form
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the insurance plan details
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws ServletException
   *           the servlet exception
   * @throws SQLException
   *           the SQL exception
   */
  @IgnoreConfidentialFilters
  public ActionForward getInsurancePlanDetails(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
          throws IOException, ServletException, SQLException {
    PlanDetailsDAO planDetailsDao = new PlanDetailsDAO();
    String planId = request.getParameter("plan_id");
    String visitType = request.getParameter("visitType");
    JSONSerializer js = new JSONSerializer().exclude("class");
    response.setContentType("text/plain");
    response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    response.getWriter().write(js.serialize(ConversionUtils.listBeanToListMap(planDetailsDao
        .getAllPlanChargesBasedonPatientType(Integer.parseInt(planId), visitType, null))));
    response.flushBuffer();
    return null;
  }

  /**
   * Gets the insurance plan details for follow up visit.
   *
   * @param mapping
   *          the mapping
   * @param form
   *          the form
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the insurance plan details for follow up visit
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws ServletException
   *           the servlet exception
   * @throws SQLException
   *           the SQL exception
   */
  public ActionForward getInsurancePlanDetailsForFollowUpVisit(ActionMapping mapping,
      ActionForm form, HttpServletRequest request, HttpServletResponse response)
          throws IOException, ServletException, SQLException {
    PlanDetailsDAO planDetailsDao = new PlanDetailsDAO();
    String planId = request.getParameter("plan_id");
    String mrno = request.getParameter("mrno");
    String visitType = request.getParameter("visitType");
    String mainvisitId = request.getParameter("mainvisitId");
    JSONSerializer js = new JSONSerializer().exclude("class");
    response.setContentType("text/plain");
    response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    response.getWriter()
        .write(js.serialize(ConversionUtils.listBeanToListMap(
            planDetailsDao.getInsurancePlanDetailsForFollowUpVisit(Integer.parseInt(planId), mrno,
                visitType, mainvisitId))));
    response.flushBuffer();
    return null;
  }

  /**
   * Gets the quick estimate screen.
   *
   * @param mapping
   *          the mapping
   * @param form
   *          the form
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the quick estimate screen
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws ParseException
   *           the parse exception
   */
  @IgnoreConfidentialFilters
  public ActionForward getQuickEstimateScreen(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
          throws SQLException, IOException, ParseException {

    JSONSerializer js = new JSONSerializer().exclude("class"); 
    setBillDetails(request);
    int centerId = RequestContext.getCenterId();
    String countryCode = centerMasterDAO.getCountryCode(centerId);
    if (StringUtil.isNullOrEmpty(countryCode)) {
      countryCode = centerMasterDAO.getCountryCode(0);
    }
    request.setAttribute("defaultCountryCode", countryCode);
    request.setAttribute("countryList", PhoneNumberUtil.getAllCountries());
    String estimateno = request.getParameter("estimate_no");
    if (estimateno != null && !estimateno.equals("")) {
      int estimateNo = new Integer(estimateno);
      BasicDynaBean estimate = QuickEstimateDAO.getEstimateBillDetails(estimateNo);
      if (estimate != null) {
        List<BasicDynaBean> charges = QuickEstimateDAO.getChargeDetailsBean(estimateNo);

        request.setAttribute("charges", ConversionUtils.listBeanToListMap(charges));
        request.setAttribute("estimate", estimate);
        if (estimate.get("plan_id") != null && !estimate.get("plan_id").equals("")) {
          List policyCharges = ConversionUtils.listBeanToListMap(
              new PlanDetailsDAO().getAllPlanCharges((Integer) estimate.get("plan_id")));
          request.setAttribute("policyCharges", js.serialize(policyCharges));
        }

      } else {
        request.setAttribute("resultMsg", "There is no estimate bill with number: " + estimateNo);
        request.setAttribute("policyCharges", js.serialize(new ArrayList()));
        return mapping.findForward("getQuickEstimateScreen");
      }
    }

    BasicDynaBean printPref = PrintConfigurationsDAO
        .getPrintMode(PrintConfigurationsDAO.PRINT_TYPE_BILL);
    request.setAttribute("pref", printPref);

    List printType = PrintConfigurationsDAO.getPrinterTypes();
    request.setAttribute("printerType", printType);

    request.setAttribute("screenId", mapping.getProperty("screen_id"));
    request.setAttribute("serviceGroups",
        serviceGroupsDAO.listAll(null, "status", "A", "service_group_name"));
    request.setAttribute("serviceGroupsJSON",
        new JSONSerializer().serialize(ConversionUtils.copyListDynaBeansToMap(
            serviceGroupsDAO.listAll(null, "status", "A", null))));
    request.setAttribute("servicesSubGroupsJSON",
        new JSONSerializer()
            .serialize(ConversionUtils.copyListDynaBeansToMap(new GenericDAO("service_sub_groups")
                .listAll(null, "status", "A", "service_sub_group_name"))));
    request.setAttribute("anaeTypesJSON",
        new JSONSerializer().serialize(ConversionUtils.copyListDynaBeansToMap(
            new GenericDAO("anesthesia_type_master").listAll(null, "status", "A", null))));
    Map<String, Object> filterMap = new HashMap<String, Object>();
    // in multi center scheema theatre must belong to visit center.
    if (GenericPreferencesDAO.getGenericPreferences().getMax_centers_inc_default() > 1) {
      filterMap.put("center_id", (Integer) request.getSession().getAttribute("centerId"));
    } 
    filterMap.put("status", "A");
    request.setAttribute("otlist_applicabletovisitcenter",
        new GenericDAO("theatre_master").listAll(null, filterMap, "theatre_name"));
    List<BasicDynaBean> allbedtypes = new BedMasterDAO().getexistingbedtypes(true);
    // List<BasicDynaBean> quickEstimateCategory = new CategoryMasterDAO().getCategoryDetails();
    request.setAttribute("allbedtypes", allbedtypes);
    // request.setAttribute("quickEstimateCat", quickEstimateCategory);
    request.setAttribute("insuCompanyDetails", js.serialize(
        ConversionUtils.listBeanToListMap(new InsuCompMasterDAO().getinsuCompanyDetailsList())));
    request.setAttribute("categoryWiseRateplans", js.serialize(ConversionUtils.listBeanToListMap(
        PatientCategoryDAO.getAllCategoriesIncSuperCenter(RequestContext.getCenterId()))));
    request.setAttribute("insCompTpaList", js.serialize(
        ConversionUtils.listBeanToListMap(new InsuranceCompanyTPAMasterDAO().getCompanyTpaList())));
    request.setAttribute("tpanames",
        js.serialize(ConversionUtils.listBeanToListMap(TpaMasterDAO.gettpanames())));
    request.setAttribute("policyNames",
        js.serialize(ConversionUtils.listBeanToListMap(new PlanMasterDAO().listAll("plan_name"))));
    // request.setAttribute("insuCategoryNames", js.serialize(ConversionUtils.listBeanToListMap(new
    // GenericDAO("insurance_category_master").listAll())));
    request.setAttribute("insuCategoryNames", js.serialize(ConversionUtils.listBeanToListMap(
        InsuranceCategoryMasterDAO.getInsCatCenter(RequestContext.getCenterId()))));
    request.setAttribute("regPrefJSON",
        js.serialize(RegistrationPreferencesDAO.getRegistrationPreferences()));
    request.setAttribute("orgNameJSON",
        js.serialize(ConversionUtils.listBeanToListMap(OrgMasterDao.getOrganizations())));
    if (centerId != 0) {
      request.setAttribute("centerWiseOrgNameJSON",
          js.serialize(ConversionUtils.listBeanToListMap(OrgMasterDao.getOrganizations(centerId))));
    } 
    request.setAttribute("bedChargesJson",
        js.serialize(ConversionUtils.listBeanToListMap(BedMasterDAO.getBillingBedDetails())));
    request.setAttribute("salutationQueryJson",
        js.serialize(ConversionUtils.listBeanToListMap(SalutationMasterDAO.getSalutationIdName())));
    request.setAttribute("quickEstimateTemplates", new GenericDAO("common_print_templates")
        .findAllByKey("template_type", "QuickEstimateBillPrint"));

    request.setAttribute("allDoctorConsultationTypes",
        js.serialize(ConversionUtils.copyListDynaBeansToMap(new ConsultationTypesDAO().listAll())));

    return mapping.findForward("getQuickEstimateScreen");
  }

  /**
   * Sets the bill details.
   *
   * @param req
   *          the new bill details
   * @throws SQLException
   *           the SQL exception
   */
  private void setBillDetails(HttpServletRequest req) throws SQLException {

    BillBO billBOObj = new BillBO();
    JSONSerializer js = new JSONSerializer().exclude("class");

    // get Constants
    req.setAttribute("chargeGroupConstList", billBOObj.getChargeGroupConstNames());
    List chargeHeadList = billBOObj.getChargeHeadConstNames();
    req.setAttribute("chargeHeadsJSON", js.serialize(chargeHeadList));

    Preferences pref = (Preferences) req.getSession().getAttribute("preferences");
    if (pref != null) {
      req.setAttribute("modulesActivatedJSON", js.serialize(pref.getModulesActivatedMap()));
    }

    /*
     * Create a map of chargeHeadId => associated_module
     */
    HashMap chargeHeadModule = new HashMap();
    Iterator it = chargeHeadList.iterator();
    while (it.hasNext()) {
      Hashtable chargeHead = (Hashtable) it.next();
      chargeHeadModule.put(chargeHead.get("CHARGEHEAD_ID"), chargeHead.get("ASSOCIATED_MODULE"));
    }
    req.setAttribute("chargeHeadModule", chargeHeadModule);

    /*
     * JSON data for use in loading dept/ward
     */

    req.setAttribute("genPrefs", GenericPreferencesDAO.getGenericPreferences());
    int centerId = (Integer) req.getSession(false).getAttribute("centerId");
    List<BasicDynaBean> authorizers = DiscountAuthorizerMasterAction
        .getDiscountAuthorizers(centerId);

    req.setAttribute("discountAuthorizersJSON",
        js.serialize(ConversionUtils.listBeanToListMap(authorizers)));

    List<String> doctorsList = DoctorMasterDAO.getDoctorDepartmentsDynaList();
    req.setAttribute("doctorsJSON", js.serialize(ConversionUtils.listBeanToListMap(doctorsList)));

    List<BasicDynaBean> taxSubGroups = billChargeTaxDao.getTaxSubGroupsDetails();
    req.setAttribute("taxSubGroupsJSON",
        js.serialize(ConversionUtils.copyListDynaBeansToMap(taxSubGroups)));

    BasicDynaBean mst = (BasicDynaBean) new GenericDAO("master_timestamp").getRecord();
    req.setAttribute("masterTimeStamp", mst.get("master_count"));
  }

  /**
   * Save quick estimate.
   *
   * @param mapping
   *          the mapping
   * @param form
   *          the form
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the action forward
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws ServletException
   *           the servlet exception
   * @throws SQLException
   *           the SQL exception
   * @throws ParseException
   *           the parse exception
   * @throws Exception
   *           the exception
   */
  public ActionForward saveQuickEstimate(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
          throws IOException, ServletException, SQLException, ParseException, Exception {
    
    HttpSession session = request.getSession();
    String userid = (String) session.getAttribute("userid");

    FlashScope flash = FlashScope.getScope(request);
    ActionRedirect redirect = new ActionRedirect(mapping.findForward("savedetails"));

    List<BasicDynaBean> insertChargeList = new ArrayList<BasicDynaBean>();
    List<BasicDynaBean> updateChargeList = new ArrayList<BasicDynaBean>();

    boolean success = false;
    Connection con = null;
    BasicDynaBean estBill = null;

    String[] chargeIds = request.getParameterValues("chargeId");
    String[] edited = request.getParameterValues("edited");
    String[] delCharge = request.getParameterValues("delCharge");
    String[] chargeGroupId = request.getParameterValues("chargeGroupId");
    String[] chargeHeadId = request.getParameterValues("chargeHeadId");
    String[] chargeRef = request.getParameterValues("chargeRef");
    String[] departmentId = request.getParameterValues("departmentId");
    String[] descriptionId = request.getParameterValues("descriptionId");
    String[] description = request.getParameterValues("description");

    String[] actrRemarks = request.getParameterValues("act_remarks");
    String[] remarks = request.getParameterValues("remarks");
    String[] rate = request.getParameterValues("rate");
    String[] originalRate = request.getParameterValues("originalRate");
    String[] qty = request.getParameterValues("qty");
    String[] amt = request.getParameterValues("amt");
    String[] taxAmt = request.getParameterValues("tax_amt");
    String[] units = request.getParameterValues("units");
    String[] hasActivity = request.getParameterValues("hasActivity");
    String[] discount = request.getParameterValues("discount");
    String[] sponsorAmount = request.getParameterValues("sponsor_amt");
    String[] sponsorTax = request.getParameterValues("sponsor_tax");
    String[] patientAmount = request.getParameterValues("patient_amt");
    String[] patientTax = request.getParameterValues("patient_tax");
    String printerId = request.getParameter("printType");
    String quickEstimateTemplateName = request.getParameter("quickEstimateTemplate");
    String estimateno = request.getParameter("estimate_no");
    String isPrint = request.getParameter("isPrint");
    int estimateNo = 0;
    try {

      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);

      if (estimateno != null && !estimateno.equals("")) {
        estimateNo = new Integer(request.getParameter("estimate_no"));
        estBill = estimateBillDAO.findByKey("estimate_no", estimateNo);
        Map fields = new HashMap();
        Map keys = new HashMap();

        keys.put("estimate_no", estimateNo);

        fields.put("username", userid);
        fields.put("remarks", request.getParameter("estimate_remarks"));
        fields.put("nationality_id", request.getParameter("nationality_id"));
        fields.put("mod_time", DateUtil.getCurrentTimestamp());

        success = (estimateBillDAO.update(con, fields, keys)) > 0;

      } else {
        estimateNo = DataBaseUtil.getNextSequence("estimate_bill_sequence");
        estBill = estimateBillDAO.getBean();
        estBill.set("estimate_no", estimateNo);

        estBill = copyEstimateToBean(request, estBill);

        String mobileNum = String.valueOf(estBill.get("mobile_no"));
        String defaultCode = centerMasterDAO.getCountryCode(RequestContext.getCenterId());
        List<String> splitCountryCodeAndText = PhoneNumberUtil.getCountryCodeAndNationalPart(
            mobileNum, null);
        if (splitCountryCodeAndText != null && !splitCountryCodeAndText.isEmpty()
            && !splitCountryCodeAndText.get(0).isEmpty()) {
          estBill.set("mobile_no_country_code", "+" + splitCountryCodeAndText.get(0));
          estBill.set("mobile_no", "+" + splitCountryCodeAndText.get(0)
              + splitCountryCodeAndText.get(1));
        } else if (defaultCode != null) {
          estBill.set("mobile_no_country_code", "+" + defaultCode);
          if (mobileNum != null && mobileNum.equals("") && !mobileNum.startsWith("+")) {
            estBill.set("mobile_no", "+" + defaultCode + mobileNum);
          }
        }

        success = estimateBillDAO.insert(con, estBill);

      }

      int numCharges = 0;
      if (chargeIds != null) {
        numCharges = chargeIds.length;
      }

      for (int i = 0; i < numCharges; i++) {
        String chargeId = chargeIds[i];

        // ignore the "template" charge: this is unavoidable. New charges id start with _
        if ((chargeId == null) || chargeId.equals("")) {
          continue;
        } 
        // ignore charges that have not been edited or new (New charges will have _ as the id)
        if (!chargeId.startsWith("_") && !new Boolean(edited[i])) {
          continue;
        }
        // ignore charges that have been added + deleted in the UI
        if (chargeId.startsWith("_") && new Boolean(delCharge[i])) {
          continue;
        }
        
        BasicDynaBean chargebean = estimateChargeDAO.getBean();

        chargebean.set("estimate_no", estimateNo);
        chargebean.set("charge_group", chargeGroupId[i]);
        chargebean.set("charge_head", chargeHeadId[i]);
        chargebean.set("act_remarks", actrRemarks[i]);
        chargebean.set("remarks", remarks[i]);
        chargebean.set("act_rate", new BigDecimal(rate[i]));
        chargebean.set("act_quantity", new BigDecimal(qty[i]));
        chargebean.set("amount", new BigDecimal(amt[i]));
        chargebean.set("tax_amt", new BigDecimal(taxAmt[i]));
        chargebean.set("mod_time", DateUtil.getCurrentTimestamp());
        if (chargeRef[i] != null && !chargeRef[i].equals("_")) {
          chargebean.set("charge_ref", chargeRef[i]);
        } 
        chargebean.set("act_department_id", departmentId[i]);
        chargebean.set("act_description", description[i]);
        chargebean.set("act_description_id", descriptionId[i]);
        chargebean.set("act_unit", units[i]);
        chargebean.set("username", userid);
        chargebean.set("orig_rate", new BigDecimal(originalRate[i]));
        chargebean.set("hasactivity", new Boolean(hasActivity[i]));
        chargebean.set("discount",
            discount[i].equals("") ? BigDecimal.ZERO : new BigDecimal(discount[i]));
        chargebean.set("sponsor_amt",
            sponsorAmount[i].equals("") ? BigDecimal.ZERO : new BigDecimal(sponsorAmount[i]));
        chargebean.set("sponsor_tax",
            sponsorTax[i].equals("") ? BigDecimal.ZERO : new BigDecimal(sponsorTax[i]));
        chargebean.set("patient_amt",
            patientAmount[i].equals("") ? BigDecimal.ZERO : new BigDecimal(patientAmount[i]));
        chargebean.set("patient_tax",
            patientTax[i].equals("") ? BigDecimal.ZERO : new BigDecimal(patientTax[i]));

        if (new Boolean(delCharge[i])) {
          chargebean.set("status", ChargeDTO.CHARGE_STATUS_CANCELLED);
        } else {
          chargebean.set("status", ChargeDTO.CHARGE_STATUS_ACTIVE);
        }

        if (chargeId.startsWith("_")) {
          // charge added in the UI
          int chargeid = new Integer(DataBaseUtil.getNextSequence("estimate_charge_seq"));
          chargebean.set("charge_id", chargeid);

          insertChargeList.add(chargebean);

        } else {
          // this is an existing charge being updated, we only set values
          // that can be changed in the UI, ignoring the rest.

          chargebean.set("charge_id", new Integer(chargeId));
          updateChargeList.add(chargebean);

        }
      }
      if (success) {
        if (insertChargeList.size() > 0) {
          success = success && estimateChargeDAO.insertAll(con, insertChargeList);
        } 

        int resultInt = 0;
        if (updateChargeList.size() > 0) {
          for (BasicDynaBean bean : updateChargeList) {
            resultInt = resultInt + estimateChargeDAO.update(con, bean.getMap(),
                "charge_id", bean.get("charge_id"));
          }
        }

        if (isPrint.equals("P")) {
          if (resultInt == updateChargeList.size()) {

            List<String> printURLs = new ArrayList<String>();
            printURLs.add(request.getContextPath()
                + "/pages/registration/QuickEstimate.do?_method=estimateBillPrint" + "&estimate_no="
                + estimateNo + "&printerId=" + printerId + "&template_name="
                + quickEstimateTemplateName);
            session.setAttribute("printURLs", printURLs);

            success = success && true;
          }
        }
      }
      if (success) {
        flash.put("message", "Estimated Bill saved successfully.");
      } else {
        flash.put("error", "Failed to save.");
      } 

    } finally {
      DataBaseUtil.commitClose(con, success);
    }

    String printerTypeStr = request.getParameter("printType");
    int printerType = 0;
    if ((printerTypeStr != null) && !printerTypeStr.equals("")) {
      printerType = Integer.parseInt(printerTypeStr);
    } 
    String customTemplate = request.getParameter("printBill");

    if (estimateno != null && !estimateno.equals("")) {
      redirect.addParameter("estimate_no", estimateNo);
    }
    //redirect.addParameter("message", message);
    redirect.addParameter("printerType", printerType);
    redirect.addParameter("customTemplate", customTemplate);

    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

    String returnQuickEstimatePre = request.getParameter("returnQuickEstimatePre");
    if (returnQuickEstimatePre != null && !returnQuickEstimatePre.equals("")) {
      return mapping.findForward("getQuickEstimatePrerequisitesScreen");
    } else {
      return redirect;
    } 
  }

  /**
   * Copy estimate to bean.
   *
   * @param req
   *          the req
   * @param bean
   *          the bean
   * @return the basic dyna bean
   * @throws Exception
   *           the exception
   */
  private BasicDynaBean copyEstimateToBean(HttpServletRequest req, BasicDynaBean bean)
      throws Exception {
    String userid = (String) req.getSession(false).getAttribute("userid");
    bean.set("estimate_date", DateUtil.getCurrentTimestamp());
    bean.set("username", userid);
    bean.set("remarks", req.getParameter("estimate_remarks"));
    bean.set("nationality_id", req.getParameter("nationality_id"));
    bean.set("mod_time", DateUtil.getCurrentTimestamp());
    bean.set("mr_no", req.getParameter("mrno"));
    String patCatStr = req.getParameter("patient_category_id");
    bean.set("patient_category_id",
        (patCatStr != null && !patCatStr.isEmpty() ? Integer.parseInt(patCatStr) : null));
    bean.set("insurance_co_id", req.getParameter("primary_insurance_co"));
    bean.set("tpa_id", req.getParameter("primary_sponsor_id"));
    String planTypeStr = req.getParameter("primary_plan_type");
    bean.set("plan_type_id",
        (planTypeStr != null && !planTypeStr.isEmpty()) ? Integer.parseInt(planTypeStr) : null);
    String planIdStr = req.getParameter("primary_plan_id");
    bean.set("plan_id",
        (planIdStr != null && !planIdStr.isEmpty()) ? Integer.parseInt(planIdStr) : null);
    String mrNo = req.getParameter("mrno");
    if (mrNo != null && !mrNo.isEmpty()) {
      bean.set("salutation_id", req.getParameter("i_salutation_id"));
      bean.set("person_name", req.getParameter("i_patient_full_name"));
      bean.set("mobile_no", req.getParameter("i_patient_mobile"));
      String ageStr = req.getParameter("i_patient_age");
      bean.set("age", (ageStr != null && !ageStr.isEmpty()) ? Integer.parseInt(ageStr) : null);
      bean.set("age_in", req.getParameter("i_patient_age_in"));
      bean.set("gender", req.getParameter("i_patient_gender"));
      bean.set("visit_type", req.getParameter("insurancevisittype"));
      bean.set("rate_plan", req.getParameter("organization"));
      String bedTypeStr = req.getParameter("bed_type_ip");
      bean.set("bed_type", (bedTypeStr != null && !bedTypeStr.isEmpty()) ? bedTypeStr : "GENERAL");
    } else {
      bean.set("salutation_id", req.getParameter("salutation"));
      bean.set("person_name", req.getParameter("person_full_name"));
      bean.set("mobile_no", req.getParameter("mobile_no"));
      bean.set("mobile_no_country_code", req.getParameter("mobile_no_country_code"));
      String ageStr = req.getParameter("patient_age");
      bean.set("age", (ageStr != null && !ageStr.isEmpty()) ? Integer.parseInt(ageStr) : null);
      bean.set("age_in", req.getParameter("ageIn"));
      bean.set("gender", req.getParameter("patient_gender"));
      bean.set("visit_type", req.getParameter("directestimatevisittype"));
      bean.set("rate_plan", req.getParameter("organization"));
      String bedTypeStr = req.getParameter("bed_type_op");
      bean.set("bed_type", (bedTypeStr != null && !bedTypeStr.isEmpty()) ? bedTypeStr : "GENERAL");
    }

    String primarySponsorRadio = req.getParameter("primary_sponsor");
    if (primarySponsorRadio != null && primarySponsorRadio.equals("I")) {
      bean.set("visit_type", req.getParameter("insurancevisittype"));
      bean.set("rate_plan", req.getParameter("organization"));
      String bedTypeStr = req.getParameter("bed_type_ip");
      bean.set("bed_type", (bedTypeStr != null && !bedTypeStr.isEmpty()) ? bedTypeStr : "GENERAL");
    } else {
      bean.set("visit_type", req.getParameter("directestimatevisittype"));
      bean.set("rate_plan", req.getParameter("rate_plan_op"));
      String bedTypeStr = req.getParameter("bed_type_op");
      bean.set("bed_type", (bedTypeStr != null && !bedTypeStr.isEmpty()) ? bedTypeStr : "GENERAL");
    }

    return bean;
  }

  /**
   * Estimate bill print.
   *
   * @param mapping
   *          the m
   * @param form
   *          the f
   * @param req
   *          the req
   * @param res
   *          the res
   * @return the action forward
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws TemplateException
   *           the template exception
   * @throws DocumentException
   *           the document exception
   * @throws ParseException
   *           the parse exception
   * @throws Exception
   *           the exception
   */
  @IgnoreConfidentialFilters
  public ActionForward estimateBillPrint(ActionMapping mapping, ActionForm form, 
      HttpServletRequest req,HttpServletResponse res) throws SQLException, IOException, 
          TemplateException, DocumentException, ParseException, Exception {

    String printerIdStr = req.getParameter("printerId");

    Map params = new HashMap();
    /*
     * Print preferences
     */
    int printerId = 0;
    if ((printerIdStr != null) && !printerIdStr.equals("")) {
      printerId = Integer.parseInt(printerIdStr);
    }

    int estimateNo = new Integer(req.getParameter("estimate_no"));
    BasicDynaBean estimateBill = QuickEstimateDAO.getEstimateBillDetails(estimateNo);

    params.put("estimateBill", estimateBill);

    List<BasicDynaBean> charges = QuickEstimateDAO.getChargeDetailsBean(estimateNo);
    params.put("charges", charges);

    Map chargeGroupMap = ConversionUtils.listBeanToMapListBean(charges, "chargegroup_name");
    params.put("chargeGroupMap", chargeGroupMap);
    params.put("chargeGroups", chargeGroupMap.keySet());

    Map chargeHeadMap = ConversionUtils.listBeanToMapListBean(charges, "chargehead_name");
    params.put("chargeHeadMap", chargeHeadMap);
    params.put("chargeHeads", chargeHeadMap.keySet());

    PrintTemplatesDAO templateDao = new PrintTemplatesDAO();
    String templateName = req.getParameter("template_name");
    BasicDynaBean tmpBean = templateDao.getTemplateContent(templateName);
    String templateMode = null;
    String templateContent = null;
    if (tmpBean != null) {
      templateContent = (String) tmpBean.get("template_content");
      templateMode = (String) tmpBean.get("template_mode");
    }

    FtlReportGenerator ftlGen = null;
    if (templateContent != null) {
      StringReader reader = new StringReader(templateContent);
      if (templateMode.equals("H")) {
        ftlGen = new FtlReportGenerator("QuickEstimateBillPrint", reader);
      } else {
        ftlGen = new FtlReportGenerator("QuickEstimateBillPrintText", reader);
      }
    } else {
      if (templateName.equals("QuickEstimateBillPrint")) {
        ftlGen = new FtlReportGenerator("QuickEstimateBillPrint");
        templateMode = "H";
      } else {
        ftlGen = new FtlReportGenerator("QuickEstimateBillPrintText");
        templateMode = "T";
      }
    }

    StringWriter writer = new StringWriter();
    ftlGen.setReportParams(params);
    ftlGen.process(writer);
    String textContent = writer.toString();

    /*
     * Conver the html to text or PDF and send it as response
     */
    HtmlConverter hc = new HtmlConverter();
    BasicDynaBean printPref = PrintConfigurationsDAO
        .getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_BILL, printerId);

    if (printPref.get("print_mode").equals("T")) {

      String textReport = null;
      if (templateMode != null && templateMode.equals("T")) {
        textReport = textContent;
      } else {
        textReport = new String(hc.getText(textContent, "EstimateBill", printPref, true, true));
      }
      req.setAttribute("textReport", textReport);
      req.setAttribute("textColumns", printPref.get("text_mode_column"));
      req.setAttribute("printerType", "DMP");
      return mapping.findForward("textPrintApplet");

    } else if (printPref.get("print_mode").equals("H")) {
      res.setContentType("text/html");
      res.getWriter().write(textContent);
      return null;
    } else if (printPref.get("print_mode").equals("R")) {
      res.setContentType("application/rtf");
      res.getWriter().write(textContent);
      return null;
    } else {
      OutputStream os = res.getOutputStream();
      res.setContentType("application/pdf");
      try {
        if (templateMode != null && templateMode.equals("T")) {
          hc.textToPDF(writer.toString(), os, printPref);
        } else {
          hc.writePdf(os, textContent, "EstimateBill", printPref, false, false, true, true, true,
              false);
        }
      } catch (Exception ex) {
        log.error("Generated HTML content:");
        log.error(textContent);
        throw (ex);
      } finally {
        os.close();
      }
      return null;
    }
  }

}
