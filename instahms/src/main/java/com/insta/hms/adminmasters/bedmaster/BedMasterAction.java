package com.insta.hms.adminmasters.bedmaster;

import static com.insta.hms.jobs.common.QuartzJobHelper.buildJob;

import com.bob.hms.adminmasters.organization.OrgMasterDao;
import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.JobSchedulingService;
import com.insta.hms.common.PagedList;
import com.insta.hms.csvutils.TableDataHandler;
import com.insta.hms.jobs.JobService;
import com.insta.hms.jobs.MasterChargesCronSchedulerDetailsRepository;
import com.insta.hms.mdm.taxsubgroups.TaxSubGroupRepository;
import com.insta.hms.visitdetailssearch.VisitDetailsSearchDAO;

import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.http.HttpHeaders;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// TODO: Auto-generated Javadoc
/**
 * The Class BedMasterAction.
 */
public class BedMasterAction extends DispatchAction {

  /** The logger. */
  public static final Logger logger = LoggerFactory.getLogger(BedMasterAction.class);

  /** The dao. */
  public static final BedMasterDAO dao = new BedMasterDAO();
  
  private static final GenericDAO priorityRateSheetParametersView =
      new GenericDAO("priority_rate_sheet_parameters_view");
  private static final GenericDAO icuBedChargesDAO = new GenericDAO("icu_bed_charges");
  private static final GenericDAO itemGroupType = new GenericDAO("item_group_type");
  private static final GenericDAO itemGroups = new GenericDAO("item_groups");
  
  /**
   * Gets the details.
   *
   * @param mapping
   *          the mapping
   * @param form
   *          the form
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the details
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws ServletException
   *           the servlet exception
   * @throws Exception
   *           the exception
   */
  public ActionForward getdetails(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException, Exception {
    // request.setAttribute("Bedtypes", dao.getexistingbedtypes());
    request.setAttribute("filterClosed", true);
    BedMasterForm bf = (BedMasterForm) form;

    if (bf.getChargeHead() == null) {
      bf.setChargeHead("BEDCHARGE");
    }
    if (bf.getOrgId() == null || bf.getOrgId().equals("")) {
      bf.setOrgId("ORG0001");
    }
    BasicDynaBean bean = OrgMasterDao.getOrgdetailsDynaBean(bf.getOrgId());
    bf.setOrgName((String) bean.get("org_name"));

    setAttributes(request, bf);

    return mapping.findForward("success");
  }

  /**
   * Sets the attributes.
   *
   * @param request
   *          the request
   * @param form
   *          the form
   * @throws Exception
   *           the exception
   */
  private static void setAttributes(HttpServletRequest request, BedMasterForm form)
      throws Exception {
    int pageNum = 1;
    String pageString = form.getPageNum();
    if (pageString != null) {
      if (!pageString.equals("")) {
        pageNum = Integer.parseInt(pageString);
      }
    }
    String[] isOverride = request.getParameterValues("is_override");
    String override = null;
    if (null != isOverride) {
      override = isOverride.length == 2 ? null : isOverride[0];
    }

    PagedList pagedList = dao.getBedDetails(form.getChargeHead(), form.getOrgId(), pageNum,
        override);
    request.setAttribute("chargeHead", form.getChargeHead());
    request.setAttribute("pagedList", pagedList);
    Map<String, Object> cronJobKeys = new HashMap<String, Object>();
    cronJobKeys.put("entity", "NewBedType");
    ArrayList<String> status = new ArrayList<String>();
    status.add("F");
    status.add("P");
    cronJobKeys.put("status", status);
    MasterChargesCronSchedulerDetailsRepository masterChargesCronSchedulerDetailsRepository =
           ApplicationContextProvider.getBean(MasterChargesCronSchedulerDetailsRepository.class);
    List<BasicDynaBean> masterCronJobDetails =
        masterChargesCronSchedulerDetailsRepository.findByCriteria(cronJobKeys);
    request.setAttribute("masterCronJobDetails",
        ConversionUtils.listBeanToListMap(masterCronJobDetails));
  }

  /**
   * Search results.
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
   * @throws Exception
   *           the exception
   */
  public ActionForward searchResults(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException, Exception {
    BedMasterForm bf = (BedMasterForm) form;
    if (bf.getChargeHead() == null || bf.getChargeHead().isEmpty()) {
      bf.setChargeHead("BEDCHARGE");
    }
    if (bf.getOrgId() == null || bf.getOrgId().isEmpty()) {
      bf.setOrgId("ORG0001");
    }
    setAttributes(request, bf);
    request.setAttribute("filterClosed", true);
    return mapping.findForward("success");
  }

  /**
   * Gets the new screen.
   *
   * @param mapping
   *          the mapping
   * @param form
   *          the form
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the new screen
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws ServletException
   *           the servlet exception
   * @throws Exception
   *           the exception
   */
  public ActionForward getNewScreen(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException, Exception {

    // brings for adding new bedtypes.
    String icuStatus = request.getParameter("ICU");
    BedMasterForm bf = (BedMasterForm) form;
    bf.setOrgName("GENERAL");
    bf.setOrgId("ORG0001");
    Map map = null;
    boolean isIcu = false;
    if (icuStatus.equals("N")) {
      isIcu = false;
      request.setAttribute("isIcuCategory", "N");
    } else {
      isIcu = true;
      request.setAttribute("isIcuCategory", "Y");
    }

    map = dao.getNewScreenforAddingNewBed(isIcu);

    List bedTypesList = (List) map.get("BEDTYPES");
    request.setAttribute("bedTypesCount", bedTypesList.size());
    request.setAttribute("output", map);
    request.setAttribute("method", "addNewBed");
    request.setAttribute("existingBeds", dao.getexistingbedtypes(isIcu));
    request.setAttribute("ICU", icuStatus);
    JSONSerializer js = new JSONSerializer().exclude("class");
    request.setAttribute("derivedRatePlanDetails", js.serialize(Collections.EMPTY_LIST));

    request.setAttribute("itemGroupTypeList", ConversionUtils.listBeanToListMap(
        itemGroupType.findAllByKey("item_group_type_id", "TAX")));
    request.setAttribute("itemGroupListJson", js.serialize(ConversionUtils
        .listBeanToListMap(itemGroups.findAllByKey("status", "A"))));
    // request.setAttribute("itemSubGroupListJson",
    // js.serialize(ConversionUtils.listBeanToListMap(new
    // GenericDAO("item_sub_groups").findAllByKey("status","A"))));
    List<BasicDynaBean> itemSubGroupList = new TaxSubGroupRepository()
        .getItemSubGroup(new java.sql.Date(new java.util.Date().getTime()));
    Iterator<BasicDynaBean> itemSubGroupListIterator = itemSubGroupList.iterator();
    List<BasicDynaBean> validateItemSubGrouList = new ArrayList<BasicDynaBean>();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    String currentDateStr = sdf.format(new java.util.Date());
    while (itemSubGroupListIterator.hasNext()) {
      BasicDynaBean itenSubGroupbean = itemSubGroupListIterator.next();
      if (itenSubGroupbean.get("validity_end") != null) {
        Date endDate = (Date) itenSubGroupbean.get("validity_end");

        try {
          if (sdf.parse(currentDateStr).getTime() <= endDate.getTime()) {
            validateItemSubGrouList.add(itenSubGroupbean);
          }
        } catch (ParseException ex) {
          continue;
        }
      } else {
        validateItemSubGrouList.add(itenSubGroupbean);
      }
    }
    request.setAttribute("itemSubGroupListJson",
        js.serialize(ConversionUtils.listBeanToListMap(validateItemSubGrouList)));
    return mapping.findForward("getNewScreen");
  }

  /**
   * Adds the new bed.
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
   * @throws Exception
   *           the exception
   */
  public ActionForward addNewBed(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws IOException, ServletException, Exception {

    BedMasterForm bf = (BedMasterForm) form;
    String baseBedForCharges = bf.getBaseBeadForCharges();
    String userName = (String) request.getSession(false).getAttribute("userid");

    boolean useValue = true;
    Double varianceValue = bf.getVarianceValue();
    String varianceType = bf.getVariaceType();
    Double varianceBy = bf.getVarianceBy();
    Double nearstRoundOfValue = bf.getNearsetRoundofValue();
    ActionRedirect redirect = new ActionRedirect(mapping.findForwardConfig("addRedirect"));
    FlashScope flash = FlashScope.getScope(request);

    // Cheking for all available Bedtypes are available for ICU bed.
    int bedTypeCount = Integer.parseInt(request.getParameter("noOfBedTypes"));
    int dbBedTypeCount = DataBaseUtil
        .getIntValueFromDb("select distinct count(bed_type_name) from bed_types");
    if (request.getParameter("ICU").equals("Y") && dbBedTypeCount != bedTypeCount) {
      redirect.addParameter("bedType", "New");
      redirect.addParameter("ICU", request.getParameter("ICU"));
      redirect.addParameter("bedTypeCount", bedTypeCount);
      flash.put("error", "Add ICU Bed Type again,Failed to update BedTypes.");
      redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
      return redirect;
    }

    if (varianceValue.doubleValue() == 0.0) {
      useValue = false;
    }

    BedMasterBO bo = new BedMasterBO();
    ArrayList<BedDetails> al = new ArrayList<BedDetails>();
    BedDetails bd = null;

    String[] baseBed = bf.getBaseBed();
    for (int i = 0; i < baseBed.length; i++) {
      bd = new BedDetails();
      bd.setBedType(bf.getBedtype());
      bd.setOrgId(bf.getOrgId());
      bd.setBedStatus(bf.getStatus());
      bd.setDisplayOrder(bf.getDisplayOrder());
      bd.setBillBedType(bf.getBillBedType());
      bd.setCodeType(bf.getCodeType());
      bd.setOrgItemCode(bf.getOrgItemCode());
      bd.setAllowZeroClaimAmount(bf.getAllowZeroClaimAmount());

      bd.setBaseBed(baseBed[i]);
      bd.setBedCharge(bf.getBedCharge()[i]);
      bd.setDutyCharge(bf.getDutyCharge()[i]);
      bd.setHourlyCharge(bf.getHourlyCharge()[i]);
      bd.setIntialCharge(bf.getIntialCharge()[i]);
      bd.setLuxaryCharge(bf.getLuxaryCharge()[i]);
      bd.setNursingCharge(bf.getNursingCharge()[i]);
      bd.setProfCharge(bf.getProfCharge()[i]);

      bd.setBedChargeDiscount(bf.getBedChargeDiscount()[i]);
      bd.setNursingChargeDiscount(bf.getNursingChargeDiscount()[i]);
      bd.setDutyChargeDiscount(bf.getDutyChargeDiscount()[i]);
      bd.setProfChargeDiscount(bf.getProfChargeDiscount()[i]);
      bd.setHourlyChargeDiscount(bf.getHourlyChargeDiscount()[i]);
      bd.setDaycareSlab1Charge(bf.getDaycareSlab1Charge()[i]);
      bd.setDaycareSlab2Charge(bf.getDaycareSlab2Charge()[i]);
      bd.setDaycareSlab3Charge(bf.getDaycareSlab3Charge()[i]);
      bd.setDaycareSlab1ChargeDiscount(bf.getDaycareSlab1ChargeDiscount()[i]);
      bd.setDaycareSlab2ChargeDiscount(bf.getDaycareSlab2ChargeDiscount()[i]);
      bd.setDaycareSlab3ChargeDiscount(bf.getDaycareSlab3ChargeDiscount()[i]);

      bd.setBillingGroupId(bf.getBillingGroupId());

      al.add(bd);
    }
    boolean status = true;
    String isIcuCategory = bf.getIsIcuCategory();
    dao.addIcuBedOwnCharges(new BedDetails(), al);
    if (isIcuCategory.equals("N")) {
      status = bo.addNewNormalBed(al, baseBedForCharges, varianceType, varianceBy, varianceValue,
          useValue, nearstRoundOfValue, userName);
    } else {
      status = bo.addNewIcuBed(al, baseBedForCharges, varianceType, varianceBy, varianceValue,
          useValue, nearstRoundOfValue, userName);
    }
    if (status) {
      List<BasicDynaBean> ratePlanList = priorityRateSheetParametersView.listAll();
      if (ratePlanList.size() > 0) {
        for (int i = 0; i < ratePlanList.size(); i++) {
          BasicDynaBean bean = ratePlanList.get(i);
          String ratePlanId = (String) bean.get("org_id");
          Double variance = new Double((Integer) bean.get("rate_variation_percent"));
          Double roundOff = new Double((Integer) bean.get("round_off_amount"));
          if (isIcuCategory.equals("N")) {
            status = dao.updateBedChargesForRatePlan(ratePlanId, bf.getBedtype(), 17, variance,
                roundOff);
          } else {
            status = dao.updateIcuBedChargesForDerivedRatePlans(ratePlanId, bf.getOrgId(),
                bf.getBedtype(), 18, variance, roundOff, false);
          }
        }
      }
    }

    if (status) {
      String bedtype = (String) bf.getBedtype();
      status = saveOrUpdateItemSubGroup(bedtype, request);
    }

    if (status) {
      String bedtype = (String) bf.getBedtype();
      status = saveOrUpdateInsuranceCategory(bedtype, request);
    }
    scheduleSaveBedTypeCreation(al, baseBedForCharges, varianceType, varianceBy, varianceValue,
        useValue, nearstRoundOfValue, userName, isIcuCategory, bf.getBedtype());

    if (status) {
      redirect = new ActionRedirect(mapping.findForwardConfig("showRedirect"));
      redirect.addParameter("bedType", bf.getBedtype());
      redirect.addParameter("orgId", bf.getOrgId());
      flash.put("success", "Bed added Successfully...");

    } else {
      flash.put("error", "Either Bed is Duplicate (Or) Failed to add new Bed....");
      redirect.addParameter("bedType", "New");
      redirect.addParameter("ICU", request.getParameter("ICU"));
    }
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
    return redirect;
  }

  /**
   * Gets the edits the charges screen.
   *
   * @param mapping
   *          the mapping
   * @param form
   *          the form
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the edits the charges screen
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws ServletException
   *           the servlet exception
   * @throws Exception
   *           the exception
   */
  public ActionForward getEditChargesScreen(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException, Exception {

    JSONSerializer js = new JSONSerializer().exclude("class");
    String orgId = request.getParameter("orgId");

    List<String> columns = new ArrayList<String>();
    columns.add("code_type");
    columns.add("item_code");
    Map<String, Object> identifiers = new HashMap<String, Object>();
    identifiers.put("organization", orgId);
    BasicDynaBean bedBean = null;
    String bedType = request.getParameter("bedType");
    if (!BedMasterDAO.isIcuBedType(bedType)) {
      identifiers.put("bed_type", bedType);
      bedBean = dao.findByKey(columns, identifiers);
    } else {
      identifiers.put("intensive_bed_type", bedType);
      bedBean = icuBedChargesDAO.findByKey(columns, identifiers);
    }
    BasicDynaBean bean = OrgMasterDao.getOrgdetailsDynaBean(orgId);
    String orgName = (String) bean.get("org_name");
    BedMasterForm bf = (BedMasterForm) form;
    BasicDynaBean bedTypeDetails = dao.getBedType(bedType);
    bf.setBedtype(bedType);
    bf.setOrgName(orgName);
    bf.setOrgId(orgId);
    bf.setStatus(dao.getBedStatus(bedType));
    bf.setDisplayOrder((Integer) bedTypeDetails.get("display_order"));
    bf.setBillBedType((String) bedTypeDetails.get("billing_bed_type"));
    bf.setAllowZeroClaimAmount((String) bedTypeDetails.get("allow_zero_claim_amount"));
    bf.setBillingGroupId((Integer) bedTypeDetails.get("billing_group_id"));

    Map al = dao.getEditChargesScreen(bedType, orgId);
    if (null != bedBean) {
      request.setAttribute("codeType", bedBean.get("code_type"));
      request.setAttribute("orgItemCode", bedBean.get("item_code"));
    }
    request.setAttribute("output", al);
    StringBuilder activeInsuranceCategories = new StringBuilder();
    List<BasicDynaBean> activeInsurance = dao.getActiveInsuranceCategories(bedType);
    for (BasicDynaBean activeInsuranceCategory : activeInsurance) {
      activeInsuranceCategories.append(activeInsuranceCategory.get("insurance_category_id"));
      activeInsuranceCategories.append(",");
    }
    request.setAttribute("insurance_categories", activeInsuranceCategories.toString());
    request.setAttribute("method", "updateBedCharges");
    request.setAttribute("bedTypeInUse", new VisitDetailsSearchDAO().isBedTypeInUse(bedType));
    request.setAttribute("bedTypesCount", ((List) al.get("BEDTYPES")).size());

    boolean isicuBedType = BedMasterDAO.isIcuBedType(bedType);
    List<BasicDynaBean> derivedRatePlanDetails = dao.getDerivedRatePlanDetails(orgId, bedType,
        isicuBedType);

    if (derivedRatePlanDetails.size() < 0) {
      request.setAttribute("derivedRatePlanDetails", js.serialize(Collections.EMPTY_LIST));
    } else {
      request.setAttribute("derivedRatePlanDetails",
          js.serialize(ConversionUtils.copyListDynaBeansToMap(derivedRatePlanDetails)));
    }

    request.setAttribute("taxsubgroup",
        ConversionUtils.copyListDynaBeansToMap(dao.getBedItemSubGroupDetails(bf.getBedtype())));
    request.setAttribute("itemGroupTypeList", ConversionUtils.listBeanToListMap(
        itemGroupType.findAllByKey("item_group_type_id", "TAX")));
    request.setAttribute("itemGroupListJson", js.serialize(ConversionUtils
        .listBeanToListMap(itemGroups.findAllByKey("status", "A"))));
    // request.setAttribute("itemSubGroupListJson",
    // js.serialize(ConversionUtils.listBeanToListMap(new
    // GenericDAO("item_sub_groups").findAllByKey("status","A"))));
    List<BasicDynaBean> itemSubGroupList = new TaxSubGroupRepository()
        .getItemSubGroup(new java.sql.Date(new java.util.Date().getTime()));
    Iterator<BasicDynaBean> itemSubGroupListIterator = itemSubGroupList.iterator();
    List<BasicDynaBean> validateItemSubGrouList = new ArrayList<BasicDynaBean>();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    String currentDateStr = sdf.format(new java.util.Date());
    while (itemSubGroupListIterator.hasNext()) {
      BasicDynaBean itenSubGroupbean = itemSubGroupListIterator.next();
      if (itenSubGroupbean.get("validity_end") != null) {
        Date endDate = (Date) itenSubGroupbean.get("validity_end");

        try {
          if (sdf.parse(currentDateStr).getTime() <= endDate.getTime()) {
            validateItemSubGrouList.add(itenSubGroupbean);
          }
        } catch (ParseException ex) {
          continue;
        }
      } else {
        validateItemSubGrouList.add(itenSubGroupbean);
      }
    }
    request.setAttribute("itemSubGroupListJson",
        js.serialize(ConversionUtils.listBeanToListMap(validateItemSubGrouList)));
    return mapping.findForward("getNewScreen");
  }

  /**
   * Update bed charges.
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
   * @throws Exception
   *           the exception
   */
  public ActionForward updateBedCharges(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException, Exception {

    BedMasterForm bf = (BedMasterForm) form;
    ArrayList<BedDetails> al = new ArrayList<BedDetails>();
    BedDetails bd = null;
    FlashScope flash = FlashScope.getScope(request);
    String[] baseBed = bf.getBaseBed();
    for (int i = 0; i < baseBed.length; i++) {
      bd = new BedDetails();
      bd.setBedType(bf.getBedtype());
      bd.setOrgId(bf.getOrgId());
      bd.setBedStatus(bf.getStatus());
      bd.setDisplayOrder(bf.getDisplayOrder());
      bd.setBillBedType(bf.getBillBedType() == null ? "Y" : bf.getBillBedType());
      bd.setCodeType(bf.getCodeType());
      bd.setOrgItemCode(bf.getOrgItemCode());
      bd.setAllowZeroClaimAmount(bf.getAllowZeroClaimAmount());
      bd.setBaseBed(baseBed[i]);
      bd.setBedCharge(bf.getBedCharge()[i]);
      bd.setDutyCharge(bf.getDutyCharge()[i]);
      bd.setHourlyCharge(bf.getHourlyCharge()[i]);
      bd.setIntialCharge(bf.getIntialCharge()[i]);
      bd.setLuxaryCharge(bf.getLuxaryCharge()[i]);
      bd.setNursingCharge(bf.getNursingCharge()[i]);
      bd.setProfCharge(bf.getProfCharge()[i]);

      bd.setBedChargeDiscount(bf.getBedChargeDiscount()[i]);
      bd.setNursingChargeDiscount(bf.getNursingChargeDiscount()[i]);
      bd.setDutyChargeDiscount(bf.getDutyChargeDiscount()[i]);
      bd.setProfChargeDiscount(bf.getProfChargeDiscount()[i]);
      bd.setHourlyChargeDiscount(bf.getHourlyChargeDiscount()[i]);
      bd.setDaycareSlab1Charge(bf.getDaycareSlab1Charge()[i]);
      bd.setDaycareSlab2Charge(bf.getDaycareSlab2Charge()[i]);
      bd.setDaycareSlab3Charge(bf.getDaycareSlab3Charge()[i]);
      bd.setDaycareSlab1ChargeDiscount(bf.getDaycareSlab1ChargeDiscount()[i]);
      bd.setDaycareSlab2ChargeDiscount(bf.getDaycareSlab2ChargeDiscount()[i]);
      bd.setDaycareSlab3ChargeDiscount(bf.getDaycareSlab3ChargeDiscount()[i]);
      bd.setBillingGroupId(bf.getBillingGroupId());

      al.add(bd);
    }

    BedMasterBO bo = new BedMasterBO();

    boolean status = bo.addOrUpdateBedCharge(al);
    if (status) {
      String bedtype = (String) bf.getBedtype();
      status = saveOrUpdateItemSubGroup(bedtype, request);
    }

    if (status) {
      String bedtype = (String) bf.getBedtype();
      status = saveOrUpdateInsuranceCategory(bedtype, request);
    }

    if (status) {
      boolean isIcu = BedMasterDAO.isIcuBedType(bf.getBedtype());
      List<BasicDynaBean> ratePlanList =
          priorityRateSheetParametersView.findAllByKey("base_rate_sheet_id", bf.getOrgId());
      if (ratePlanList.size() > 0) {
        for (int i = 0; i < ratePlanList.size(); i++) {
          BasicDynaBean bean = ratePlanList.get(i);
          String ratePlanId = (String) bean.get("org_id");
          Double variance = new Double((Integer) bean.get("rate_variation_percent"));
          Double roundOff = new Double((Integer) bean.get("round_off_amount"));
          if (!isIcu) {
            status = dao.updateChargesForDerivedRateplans(ratePlanId, bf.getOrgId(),
                bf.getBedtype(), 18, variance, roundOff, false);
          } else {
            status = dao.updateIcuBedChargesForDerivedRatePlans(ratePlanId, bf.getOrgId(),
                bf.getBedtype(), 18, variance, roundOff, false);
          }
        }
      }
    }

    if (status) {
      flash.put("success", "Changes are updated Successfully...");
    } else {
      flash.put("error", "Failed to update Changes...");
    }
    ActionRedirect redirect = new ActionRedirect(mapping.findForwardConfig("showRedirect"));
    redirect.addParameter("bedType", bf.getBedtype());
    redirect.addParameter("orgId", bf.getOrgId());
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
    return redirect;
  }

  /**
   * Group update.
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
   * @throws Exception
   *           the exception
   */
  public ActionForward groupUpdate(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException, Exception {

    String strVarianceBy = request.getParameter("varianceBy");
    String strVarianceValue = request.getParameter("varianceValue");
    String strVarianceType = request.getParameter("variaceType");

    String[] groupBeds = request.getParameterValues("groupbedType");

    String strAllbedTypes = request.getParameter("All");

    BigDecimal amount = null;

    boolean isPercentage = false;

    if (strVarianceValue != null && !strVarianceValue.equals("")) {
      amount = new BigDecimal(strVarianceValue);

    } else if (strVarianceBy != null && !strVarianceBy.equals("")) {
      amount = new BigDecimal(strVarianceBy);
      isPercentage = true;
    }

    BigDecimal roundOff = BigDecimal.ONE;

    if (strVarianceType.equals("-")) {
      amount = amount.negate();
    }

    List<String> bedTypes = new ArrayList<String>();
    if (groupBeds != null || !strAllbedTypes.equals("")) {
      for (int i = 0; i < groupBeds.length; i++) {
        if (!groupBeds[i].equals("")) {
          bedTypes.add(groupBeds[i]);
        }
      }
    }
    BedMasterForm bf = (BedMasterForm) form;
    BedMasterBO bo = new BedMasterBO();
    String groupUpdate = bf.getGroupUpdatComponent();
    String orgId = bf.getOrgId();
    String updateTable = request.getParameter("updateTable");
    boolean success = true;
    success = bo.groupUpdateCharges(orgId, bedTypes, groupUpdate, amount, isPercentage, roundOff,
        updateTable);

    FlashScope flash = FlashScope.getScope(request);
    if (success) {
      flash.success("Charge is updated Successfully...");
    } else {
      flash.error("Failed to update Charge...");
    }

    ActionRedirect redirect = new ActionRedirect(mapping.findForward("dashboard"));
    redirect.addParameter("orgId", request.getParameter("orgId"));
    redirect.addParameter("chargeHead", request.getParameter("chargeHead"));
    redirect.addParameter("pageNum", request.getParameter("pageNum"));
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

    return redirect;
  }

  /**
   * Check duplicate.
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
   * @throws Exception
   *           the exception
   */
  public ActionForward checkDuplicate(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException, Exception {

    String newBedType = request.getParameter("newBedType");
    logger.debug(newBedType);
    boolean status = BedMasterDAO.chekDuplicateBedType(newBedType);
    String res = "false";
    if (status) {
      // true means bed is duplicate bed
      res = "true";
    }
    response.setContentType("text/plain");
    response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    response.getWriter().write(res);
    return null;
  }

  /**
   * Gets the charges list.
   *
   * @param mapping
   *          the mapping
   * @param form
   *          the form
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the charges list
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws ServletException
   *           the servlet exception
   * @throws Exception
   *           the exception
   */
  public ActionForward getChargesList(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException, Exception {

    // request.setAttribute("Bedtypes", dao.getexistingbedtypes());
    request.setAttribute("filterClosed", true);
    BedMasterForm bf = (BedMasterForm) form;

    if (bf.getChargeHead() == null) {
      bf.setChargeHead("BEDCHARGE");
    }
    if (bf.getOrgId() == null) {
      bf.setOrgId("ORG0001");
    }
    BasicDynaBean bean = OrgMasterDao.getOrgdetailsDynaBean(bf.getOrgId());
    bf.setOrgName((String) bean.get("org_name"));

    setAttributes(request, bf);

    return mapping.findForward("bedChargesList");
  }

  /**
   * Gets the charges override screen.
   *
   * @param mapping
   *          the mapping
   * @param form
   *          the form
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the charges override screen
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws ServletException
   *           the servlet exception
   * @throws Exception
   *           the exception
   */
  public ActionForward getChargesOverrideScreen(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException, Exception {

    String bedType = request.getParameter("bedType");
    String orgId = request.getParameter("orgId");

    BasicDynaBean bean = OrgMasterDao.getOrgdetailsDynaBean(orgId);
    String orgName = (String) bean.get("org_name");

    BedMasterForm bf = (BedMasterForm) form;

    bf.setBedtype(bedType);
    bf.setOrgName(orgName);
    bf.setOrgId(orgId);

    List<String> columns = new ArrayList<String>();
    columns.add("code_type");
    columns.add("item_code");
    Map<String, Object> identifiers = new HashMap<String, Object>();
    identifiers.put("bed_type", bedType);
    identifiers.put("organization", orgId);
    BasicDynaBean bedBean = null;
    if (!BedMasterDAO.isIcuBedType(bedType)) {
      bedBean = dao.findByKey(columns, identifiers);
    } else {
      bedBean = icuBedChargesDAO.findByKey(columns, identifiers);
    }
    if (null != bedBean) {
      request.setAttribute("codeType", bedBean.get("code_type"));
      request.setAttribute("orgItemCode", bedBean.get("item_code"));
    }

    Map al = dao.getEditChargesScreen(bedType, orgId);
    request.setAttribute("output", al);
    request.setAttribute("bedType", bedType);
    request.setAttribute("orgId", orgId);
    request.setAttribute("orgName", orgName);
    request.setAttribute("bedTypesCount", ((List) al.get("BEDTYPES")).size());
    request.setAttribute("fromItemMaster", request.getParameter("fromItemMaster"));
    request.setAttribute("baseRateSheet", request.getParameter("baseRateSheet"));

    return mapping.findForward("bedChargesOverrideScreen");
  }

  /**
   * Override bed charges.
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
   * @throws Exception
   *           the exception
   */
  public ActionForward overrideBedCharges(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws IOException, Exception {

    BedMasterForm bf = (BedMasterForm) form;
    String bedType = bf.getBedtype();
    boolean isIcu = dao.isIcuBedType(bedType);
    Connection con = null;
    boolean success = false;

    if (!isIcu) {
      GenericDAO dao = new GenericDAO("bed_details");
      BasicDynaBean bean = dao.getBean();
      bean.set("bed_type", bf.getBedtype());
      bean.set("organization", bf.getOrgId());
      bean.set("code_type", bf.getCodeType());
      bean.set("item_code", bf.getOrgItemCode());
      bean.set("bed_charge", new BigDecimal(bf.getBedCharge()[0]));
      bean.set("nursing_charge", new BigDecimal(bf.getNursingCharge()[0]));
      bean.set("initial_payment", new BigDecimal(bf.getIntialCharge()[0]));
      bean.set("duty_charge", new BigDecimal(bf.getDutyCharge()[0]));
      bean.set("maintainance_charge", new BigDecimal(bf.getProfCharge()[0]));
      bean.set("luxary_tax", new BigDecimal(bf.getLuxaryCharge()[0]));
      bean.set("hourly_charge", new BigDecimal(bf.getHourlyCharge()[0]));
      bean.set("bed_charge_discount", new BigDecimal(bf.getBedChargeDiscount()[0]));
      bean.set("nursing_charge_discount", new BigDecimal(bf.getNursingChargeDiscount()[0]));
      bean.set("duty_charge_discount", new BigDecimal(bf.getDutyChargeDiscount()[0]));
      bean.set("maintainance_charge_discount", new BigDecimal(bf.getProfChargeDiscount()[0]));
      bean.set("hourly_charge_discount", new BigDecimal(bf.getHourlyChargeDiscount()[0]));
      bean.set("daycare_slab_1_charge", new BigDecimal(bf.getDaycareSlab1Charge()[0]));
      bean.set("daycare_slab_2_charge", new BigDecimal(bf.getDaycareSlab2Charge()[0]));
      bean.set("daycare_slab_3_charge", new BigDecimal(bf.getDaycareSlab3Charge()[0]));
      bean.set("daycare_slab_1_charge_discount",
          new BigDecimal(bf.getDaycareSlab1ChargeDiscount()[0]));
      bean.set("daycare_slab_2_charge_discount",
          new BigDecimal(bf.getDaycareSlab2ChargeDiscount()[0]));
      bean.set("daycare_slab_3_charge_discount",
          new BigDecimal(bf.getDaycareSlab3ChargeDiscount()[0]));
      bean.set("is_override", "Y");
      Map<String, Object> keys = new HashMap<String, Object>();
      keys.put("bed_type", bf.getBedtype());
      keys.put("organization", bf.getOrgId());
      try {
        con = DataBaseUtil.getConnection();
        con.setAutoCommit(false);
        int val = dao.update(con, bean.getMap(), keys);
        if (val >= 0) {
          success = true;
        }
      } finally {
        DataBaseUtil.commitClose(con, success);
      }
    } else {
      String[] baseBed = bf.getBaseBed();
      List<BasicDynaBean> chargeList = new ArrayList();
      for (int i = 0; i < baseBed.length; i++) {
        BasicDynaBean charge = icuBedChargesDAO.getBean();
        charge.set("bed_type", baseBed[i]);
        charge.set("bed_charge", new BigDecimal(bf.getBedCharge()[i]));
        charge.set("nursing_charge", new BigDecimal(bf.getNursingCharge()[i]));
        charge.set("initial_payment", new BigDecimal(bf.getIntialCharge()[i]));
        charge.set("duty_charge", new BigDecimal(bf.getDutyCharge()[i]));
        charge.set("maintainance_charge", new BigDecimal(bf.getProfCharge()[i]));
        charge.set("organization", bf.getOrgId());
        charge.set("luxary_tax", new BigDecimal(bf.getLuxaryCharge()[i]));
        charge.set("intensive_bed_type", bf.getBedtype());
        charge.set("code_type", bf.getCodeType());
        charge.set("item_code", bf.getOrgItemCode());
        charge.set("hourly_charge", new BigDecimal(bf.getHourlyCharge()[i]));
        charge.set("bed_charge_discount", new BigDecimal(bf.getBedChargeDiscount()[i]));
        charge.set("nursing_charge_discount", new BigDecimal(bf.getNursingChargeDiscount()[i]));
        charge.set("duty_charge_discount", new BigDecimal(bf.getDutyChargeDiscount()[i]));
        charge.set("maintainance_charge_discount", new BigDecimal(bf.getProfChargeDiscount()[i]));
        charge.set("hourly_charge_discount", new BigDecimal(bf.getHourlyChargeDiscount()[i]));
        charge.set("daycare_slab_1_charge", new BigDecimal(bf.getDaycareSlab1Charge()[i]));
        charge.set("daycare_slab_1_charge_discount",
            new BigDecimal(bf.getDaycareSlab1ChargeDiscount()[i]));
        charge.set("daycare_slab_2_charge", new BigDecimal(bf.getDaycareSlab2Charge()[i]));
        charge.set("daycare_slab_2_charge_discount",
            new BigDecimal(bf.getDaycareSlab2ChargeDiscount()[i]));
        charge.set("daycare_slab_3_charge", new BigDecimal(bf.getDaycareSlab3Charge()[i]));
        charge.set("daycare_slab_3_charge_discount",
            new BigDecimal(bf.getDaycareSlab3ChargeDiscount()[i]));
        charge.set("is_override", "Y");
        chargeList.add(charge);
      }
      try {
        con = DataBaseUtil.getConnection();
        con.setAutoCommit(false);
        for (BasicDynaBean c : chargeList) {
          success = dao.updateWithNames(con, c.getMap(),
              new String[] { "intensive_bed_type", "organization", "bed_type" }) >= 0;
        }
      } finally {
        DataBaseUtil.commitClose(con, success);
      }
    }
    ActionRedirect redirect = new ActionRedirect(mapping.findForward("overrideRedirect"));
    redirect.addParameter("orgId", bf.getOrgId());
    redirect.addParameter("bedType", bf.getBedtype());
    redirect.addParameter("fromItemMaster", request.getParameter("fromItemMaster"));
    redirect.addParameter("baseRateSheet", request.getParameter("baseRateSheet"));

    return redirect;
  }

  /**
   * Gets the bed charges csv handler.
   *
   * @param orgId
   *          the org id
   * @return the bed charges csv handler
   */
  private TableDataHandler getbedChargesCsvHandler(String orgId) {
    boolean filterByOrgId = (null != orgId && !"".equals(orgId));
    String orgIdFilter = (filterByOrgId) ? "bed_details.organization = '" + orgId + "'" : "";
    String[] filters = null;

    if (filterByOrgId) {
      filters = new String[] { "organization_details.status = 'A'", "bed_types.status = 'A'",
          "bed_types.billing_bed_type='Y'", orgIdFilter };
    } else {
      filters = new String[] { "organization_details.status = 'A'", "bed_types.status = 'A'",
          "bed_types.billing_bed_type='Y'" };
    }
    TableDataHandler bedchargesCsvHandler = new TableDataHandler("bed_details", // table name
        new String[] { "organization", "bed_type" }, // keys
        new String[] { "bed_charge", "nursing_charge", "initial_payment", "duty_charge",
            "maintainance_charge", "charge_type", "bed_status", "luxary_tax",
            "intensive_bed_status", "child_bed_status", "hourly_charge", "bed_charge_discount",
            "nursing_charge_discount", "duty_charge_discount", "maintainance_charge_discount",
            "hourly_charge_discount", "initial_payment_discount", "daycare_slab_1_charge",
            "daycare_slab_1_charge_discount", "daycare_slab_2_charge",
            "daycare_slab_2_charge_discount", "daycare_slab_3_charge",
            "daycare_slab_3_charge_discount", "code_type", "item_code" }, // other fields
        new String[][] { // masters
            // our field ref table ref table id field ref table name field
            { "organization", "organization_details", "org_id", "org_name" },
            { "bed_type", "bed_types", "bed_type_name", "bed_type_name" } },
        filters);
    bedchargesCsvHandler.setAlias("organization", "rate_plan");

    return bedchargesCsvHandler;
  }

  /**
   * Export bedcharges to csv.
   *
   * @param map
   *          the map
   * @param form
   *          the form
   * @param req
   *          the req
   * @param res
   *          the res
   * @return the action forward
   * @throws Exception
   *           the exception
   */
  public ActionForward exportBedchargesToCsv(ActionMapping map, ActionForm form,
      HttpServletRequest req, HttpServletResponse res) throws Exception {
    String orgId = (String) req.getParameter("orgId");
    String orgName = (String) OrgMasterDao.getOrgdetailsDynaBean(orgId).get("org_name");
    getbedChargesCsvHandler(orgId).exportTable(res, "bedcharges_" + orgName);
    return null;
  }

  /**
   * Import bedcharges from csv.
   *
   * @param map
   *          the map
   * @param form
   *          the form
   * @param req
   *          the req
   * @param res
   *          the res
   * @return the action forward
   * @throws Exception
   *           the exception
   */
  public ActionForward importBedchargesFromCsv(ActionMapping map, ActionForm form,
      HttpServletRequest req, HttpServletResponse res) throws Exception {

    FlashScope flash = FlashScope.getScope(req);
    String referer = req.getHeader("Referer");
    String orgId = (String) req.getParameter("orgId");
    referer = referer.replaceAll("&" + FlashScope.FLASH_KEY + "=[0-9A-Fa-f]+", "");
    ActionRedirect redirect = new ActionRedirect(referer);
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

    BedMasterForm uploadForm = (BedMasterForm) form;
    InputStreamReader isReader = new InputStreamReader(
        uploadForm.getUploadBedChargesFile().getInputStream());

    StringBuilder infoMsg = new StringBuilder();
    String error = getbedChargesCsvHandler(orgId).importTable(isReader, infoMsg);

    List<BasicDynaBean> ratePlanList =
        priorityRateSheetParametersView.findAllByKey("base_rate_sheet_id", orgId);
    if (ratePlanList.size() > 0) {
      for (int i = 0; i < ratePlanList.size(); i++) {
        BasicDynaBean bean = ratePlanList.get(i);
        String ratePlanId = (String) bean.get("org_id");
        Double variance = new Double((Integer) bean.get("rate_variation_percent"));
        Double roundOff = new Double((Integer) bean.get("round_off_amount"));
        dao.updateChargesForDerivedRateplans(ratePlanId, orgId, null, 18, variance, roundOff, true);
      }
    }

    if (error != null) {
      flash.put("error", error);
      return redirect;
    }

    flash.put("info", infoMsg.toString());
    return redirect;

  }

  /**
   * Gets the ICU bed charges csv handler.
   *
   * @param orgId
   *          the org id
   * @return the ICU bed charges csv handler
   */
  private TableDataHandler getIcuBedChargesCsvHandler(String orgId) {
    boolean filterByOrgId = (null != orgId && !"".equals(orgId));
    String orgIdFilter = (filterByOrgId) ? "icu_bed_charges.organization = '" + orgId + "'" : "";
    String[] filters = null;

    if (filterByOrgId) {
      filters = new String[] { "organization_details.status = 'A'", "bed_types.status = 'A'",
          "bed_types.billing_bed_type='Y'", orgIdFilter };
    } else {
      filters = new String[] { "organization_details.status = 'A'", "bed_types.status = 'A'",
          "bed_types.billing_bed_type='Y'" };
    }
    TableDataHandler icuBedChargesCsvHandler = new TableDataHandler("icu_bed_charges", // table name
        new String[] { "intensive_bed_type", "organization", "bed_type" }, // keys
        new String[] { "bed_charge", "nursing_charge", "initial_payment", "duty_charge",
            "maintainance_charge", "charge_type", "bed_status", "luxary_tax", "hourly_charge",
            "bed_charge_discount", "nursing_charge_discount", "duty_charge_discount",
            "maintainance_charge_discount", "hourly_charge_discount", "initial_payment_discount",
            "daycare_slab_1_charge", "daycare_slab_1_charge_discount", "daycare_slab_2_charge",
            "daycare_slab_2_charge_discount", "daycare_slab_3_charge",
            "daycare_slab_3_charge_discount", "code_type", "item_code" }, // other fields
        new String[][] { // masters
            // our field ref table ref table id field ref table name field
            { "organization", "organization_details", "org_id", "org_name" },
            { "bed_type", "bed_types", "bed_type_name", "bed_type_name" } },
        filters);
    icuBedChargesCsvHandler.setAlias("organization", "rate_plan");

    return icuBedChargesCsvHandler;
  }

  /**
   * Export ICU bed charges to csv.
   *
   * @param map
   *          the map
   * @param form
   *          the form
   * @param req
   *          the req
   * @param res
   *          the res
   * @return the action forward
   * @throws Exception
   *           the exception
   */
  public ActionForward exportIcuBedChargesToCsv(ActionMapping map, ActionForm form,
      HttpServletRequest req, HttpServletResponse res) throws Exception {
    String orgId = (String) req.getParameter("orgId");
    String orgName = (String) OrgMasterDao.getOrgdetailsDynaBean(orgId).get("org_name");
    getIcuBedChargesCsvHandler(orgId).exportTable(res, "ICUbedCharges_" + orgName);
    return null;
  }

  /**
   * Import ICU bed charges from csv.
   *
   * @param map
   *          the map
   * @param form
   *          the form
   * @param req
   *          the req
   * @param res
   *          the res
   * @return the action forward
   * @throws Exception
   *           the exception
   */
  public ActionForward importIcuBedChargesFromCsv(ActionMapping map, ActionForm form,
      HttpServletRequest req, HttpServletResponse res) throws Exception {

    FlashScope flash = FlashScope.getScope(req);
    String referer = req.getHeader("Referer");
    String orgId = (String) req.getParameter("orgId");
    referer = referer.replaceAll("&" + FlashScope.FLASH_KEY + "=[0-9A-Fa-f]+", "");
    ActionRedirect redirect = new ActionRedirect(referer);
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

    BedMasterForm uploadForm = (BedMasterForm) form;
    InputStreamReader isReader =
        new InputStreamReader(uploadForm.getUploadIcuBedChargesFile().getInputStream());

    StringBuilder infoMsg = new StringBuilder();
    String error = getIcuBedChargesCsvHandler(orgId).importTable(isReader, infoMsg);

    List<BasicDynaBean> ratePlanList =
        priorityRateSheetParametersView.findAllByKey("base_rate_sheet_id", orgId);
    if (ratePlanList.size() > 0) {
      for (int i = 0; i < ratePlanList.size(); i++) {
        BasicDynaBean bean = ratePlanList.get(i);
        String ratePlanId = (String) bean.get("org_id");
        Double variance = new Double((Integer) bean.get("rate_variation_percent"));
        Double roundOff = new Double((Integer) bean.get("round_off_amount"));
        dao.updateIcuBedChargesForDerivedRatePlans(ratePlanId, orgId, null, 18, variance, roundOff,
            true);
      }
    }

    if (error != null) {
      flash.put("error", error);
      return redirect;
    }

    flash.put("info", infoMsg.toString());
    return redirect;

  }

  /**
   * Save or update item sub group.
   *
   * @param bedTypeName
   *          the bed type name
   * @param request
   *          the request
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  private boolean saveOrUpdateItemSubGroup(String bedTypeName, HttpServletRequest request)
      throws SQLException, IOException {
    Map params = request.getParameterMap();
    List errors = new ArrayList();
    Connection con = null;
    boolean flag = true;
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      String[] itemSubgroupId = request.getParameterValues("item_subgroup_id");
      String[] delete = request.getParameterValues("deleted");

      if (errors.isEmpty()) {
        if (itemSubgroupId != null && itemSubgroupId.length > 0 && !itemSubgroupId[0].equals("")) {
          GenericDAO itemsubgroupdao = new GenericDAO("bed_item_sub_groups");
          BasicDynaBean itemsubgroupbean = itemsubgroupdao.getBean();
          ConversionUtils.copyToDynaBean(params, itemsubgroupbean, errors);
          List records = itemsubgroupdao.findAllByKey("bed_type_name", bedTypeName);
          if (records.size() > 0) {
            flag = itemsubgroupdao.delete(con, "bed_type_name", bedTypeName);
          }

          for (int i = 0; i < itemSubgroupId.length; i++) {
            if (itemSubgroupId[i] != null && !itemSubgroupId[i].isEmpty()) {
              if (delete[i].equalsIgnoreCase("false")) {
                itemsubgroupbean.set("bed_type_name", bedTypeName);
                itemsubgroupbean.set("item_subgroup_id", Integer.parseInt(itemSubgroupId[i]));
                flag = itemsubgroupdao.insert(con, itemsubgroupbean);
              }
            }
          }
        }
      }

    } finally {
      DataBaseUtil.commitClose(con, flag);
    }

    return flag;
  }

  /**
   * Save or update insurance category.
   *
   * @param bedType
   *          the bed type
   * @param request
   *          the request
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  private boolean saveOrUpdateInsuranceCategory(String bedType, HttpServletRequest request)
      throws SQLException, IOException {
    boolean flag = true;
    Connection con = null;
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      String[] insuranceCategories = request.getParameterValues("insurance_category_id");
      if (insuranceCategories != null && insuranceCategories.length > 0
          && !insuranceCategories[0].equals("")) {
        GenericDAO insuranceCategoryDao = new GenericDAO("bed_types_insurance_category_mapping");
        BasicDynaBean insuranceCategoryBean = insuranceCategoryDao.getBean();
        List<BasicDynaBean> records = insuranceCategoryDao.findAllByKey("bed_type_name", bedType);
        if (records != null && records.size() > 0) {
          flag = insuranceCategoryDao.delete(con, "bed_type_name", bedType);
        }
        for (String insuranceCategory : insuranceCategories) {
          insuranceCategoryBean.set("bed_type_name", bedType);
          insuranceCategoryBean.set("insurance_category_id", Integer.parseInt(insuranceCategory));
          flag = insuranceCategoryDao.insert(con, insuranceCategoryBean);
        }
      }
    } finally {
      DataBaseUtil.commitClose(con, flag);
    }

    return flag;
  }

  /**
   * Schedule bed type creation.
   *
   * @param bedDetailsList       bed Details List
   * @param baseBedForCharges    base Bed For Charges
   * @param varianceType         variance Type
   * @param varianceBy           variance By
   * @param varianceValue        variance Value
   * @param useValue             use Value
   * @param nearestRoundOffValue nearest RoundOff Value
   * @param userName             user Name
   * @param isIcuCategory        is Icu Category
   * @param bedType              bed Type
   */
  private void scheduleSaveBedTypeCreation(ArrayList<BedDetails> bedDetailsList,
      String baseBedForCharges, String varianceType, Double varianceBy,
      Double varianceValue, boolean useValue, Double nearestRoundOffValue, String userName,
      String isIcuCategory, String bedType) {
    Map<String, Object> jobData = new HashMap<>();
    jobData.put("al", bedDetailsList);
    jobData.put("base_bed_for_charges", baseBedForCharges);
    jobData.put("variance_type", varianceType);
    jobData.put("varianceBy", varianceBy);
    jobData.put("varianceValue", varianceValue);
    jobData.put("useValue", useValue);
    jobData.put("nearestRoundOffValue", nearestRoundOffValue);
    jobData.put("userName", userName);
    jobData.put("isIcuCategory", isIcuCategory);
    jobData.put("bedType", bedType);
    jobData.put("schema", RequestContext.getSchema());
    jobData.put("center_id", RequestContext.getCenterId());

    JobService jobService = JobSchedulingService.getJobService();
    jobService.scheduleImmediate(
        buildJob("NewBedTypeCreationJob-" + bedType, NewBedTypeCreationJob.class, jobData));
  }
}
