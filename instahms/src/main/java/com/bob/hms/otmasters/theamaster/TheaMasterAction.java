package com.bob.hms.otmasters.theamaster;

import com.bob.hms.adminmasters.organization.OrgMasterDao;
import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.adminmasters.bedmaster.BedMasterDAO;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.mdm.taxsubgroups.TaxSubGroupRepository;

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
import java.sql.Date;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class TheaMasterAction extends DispatchAction {

  static Logger log = LoggerFactory.getLogger(TheaMasterAction.class);

  TheatreMasterBO bo = new TheatreMasterBO();
  private static TheatreMasterDAO dao = new TheatreMasterDAO();
  private static GenericDAO theatreMasterDao = new GenericDAO("theatre_master");
  private static GenericDAO storesDao = new GenericDAO("stores");
  private static GenericDAO itemGroupsDao = new GenericDAO("item_groups");
  private static GenericDAO itemGroupTypeDao = new GenericDAO("item_group_type");

  /**
   * Get Theatre Master.
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return Rendered response
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ServletException Generic servlet exception
   * @throws Exception Other generic exception
   */
  public ActionForward getTheatMast(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException, Exception {

    TheaMasterForm tf = (TheaMasterForm) form;

    if (tf.getChargeType() == null) {
      tf.setChargeType("DC");
    } else if (tf.getChargeType().equals("")) {
      tf.setChargeType("DC");
    }

    if (tf.getOrgId() == null) {
      tf.setOrgId("ORG0001");
      tf.setOrgName("GENERAL");
    } else if (tf.getOrgId().equals("")) {
      tf.setOrgId("ORG0001");
      tf.setOrgName("GENERAL");
    }
    request.setAttribute("chargeType", tf.getChargeType());
    request.setAttribute("multiCenters",
        GenericPreferencesDAO.getGenericPreferences().getMax_centers_inc_default() > 1);
    request.setAttribute("centers", CenterMasterDAO.getAllCentersExceptSuper());
    setAttributes(request, tf);
    return mapping.findForward("getTheatMast");
  }

  private void setAttributes(HttpServletRequest request, TheaMasterForm tf) throws Exception {
    OrgMasterDao orgDao = new OrgMasterDao();
    JSONSerializer js = new JSONSerializer();
    request.setAttribute("orgnames", js.serialize(orgDao.getAllOrgs()));

    ArrayList<String> statusList = new ArrayList<String>();
    String[] status = request.getParameterValues("status");
    if (status != null && !status[0].equals("")) {
      for (int index = 0; index < status.length; index++) {
        statusList.add(status[index]);
      }
    } else {
      tf.setAllTheatres("ALL");
      statusList.add("A");
      statusList.add("I");
    }

    int pageNum = 1;
    String pageString = tf.getPageNum();
    if (pageString != null && !pageString.isEmpty()) {
      pageNum = Integer.parseInt(pageString);
    }

    tf.setPageNum(new Integer(pageNum).toString());

    String orgId = tf.getOrgId();
    String chargeType = tf.getChargeType();
    PagedList pagedList = dao.getTheatreDetails(statusList, orgId, chargeType, tf.getCenterId(),
        pageNum);
    request.setAttribute("pagedList", pagedList);
    request.setAttribute("bedTypes", new BedMasterDAO().getUnionOfAllBedTypes());

  }

  /**
   * Get new theatre creation screen.
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return Rendered response
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ServletException Generic servlet exception
   * @throws Exception Other generic exception
   */
  public ActionForward getNewTheatreScreen(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException, Exception {

    Map<String, Object> filterMap = new HashMap<String, Object>();
    filterMap.put("status", "A");

    TheaMasterForm tf = (TheaMasterForm) form;
    tf.setOrgName("GENERAL");
    tf.setOrgId("ORG0001");
    request.setAttribute("map", dao.getTheatreChargesForNewOperation());
    request.setAttribute("_method", "addNewTheatre");
    request.setAttribute("centers", CenterMasterDAO.getAllCentersExceptSuper());
    List<BasicDynaBean> stores = storesDao.listAll(null, filterMap, "dept_name");
    request.setAttribute("stores", stores);
    JSONSerializer js = new JSONSerializer().exclude("class");
    request.setAttribute("storesJson", js.serialize(ConversionUtils.listBeanToListMap(stores)));
    request.setAttribute("multiCenters",
        GenericPreferencesDAO.getGenericPreferences().getMax_centers_inc_default() > 1);
    // request.setAttribute("taxsubgroup",
    // ConversionUtils.copyListDynaBeansToMap(dao.getOtItemSubGroupDetails(tf.getTheatreId())));

    request.setAttribute("itemGroupTypeList", ConversionUtils
        .listBeanToListMap(itemGroupTypeDao.findAllByKey("item_group_type_id", "TAX")));
    request.setAttribute("itemGroupListJson",
        js.serialize(ConversionUtils.listBeanToListMap(itemGroupsDao.findAllByKey("status", "A"))));
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
    return mapping.findForward("addOrEditTheatre");
  }

  /**
   * Save new theatre creation form details.
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return Rendered response
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ServletException Generic servlet exception
   * @throws Exception Other generic exception
   */
  public ActionForward addNewTheatre(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException, Exception {

    TheaMasterForm tf = (TheaMasterForm) form;
    String theatreId = dao.getNextTheatreId();
    Theatre th = new Theatre();

    Connection con = null;
    boolean status = false;

    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      HttpSession session = request.getSession();
      String userName = (String) session.getAttribute("userid");

      th.setTheatreId(theatreId);
      th.setIncrDuration(tf.getIncrDuration());
      th.setMinDuration(tf.getMinDuration());
      th.setStatus(tf.getStatus());
      th.setTheatreName(tf.getTheatreName());
      th.setSchedule(tf.getSchedule());
      th.setOverbookLimit(tf.getOverbook_limit());
      th.setCenterId(tf.getCenterId());
      th.setUnitSize(tf.getUnitSize());
      th.setSlab1Threshold(tf.getSlab1Threshold());
      th.setStoreId(tf.getStoreId());
      th.setAllowZeroClaimAmount(tf.getAllowZeroClaimAmount());
      th.setBillingGroupId(tf.getBillingGroupId());

      TheatreMasterBO bo = new TheatreMasterBO();
      boolean duplicate = TheatreMasterDAO.checkDuplicate(tf.getTheatreName());
      ActionRedirect redirect = new ActionRedirect(mapping.findForward("addRedirect"));

      FlashScope flash = FlashScope.getScope(request);
      if (duplicate) {
        status = false;
      } else {
        status = bo.addNewTheatre(th, con, userName);
        status &= dao.initItemCharges(con, theatreId, userName);

        if (status) {
          status = saveOrUpdateItemSubGroup(theatreId, con, request);
        }

        if (status) {
          status = saveOrUpdateInsuranceCategory(theatreId, con, request);
        }

        if (status) {
          flash.put("success", "Theatre inserted Successfully...");
          redirect = new ActionRedirect(mapping.findForward("showRedirect"));
          redirect.addParameter("theatreId", theatreId);
          redirect.addParameter("orgId", request.getParameter("orgId"));
          redirect.addParameter("chargeType", request.getParameter("chargeTyp"));
          redirect.addParameter("center_id", tf.getCenterId());
          redirect.addParameter("store_id", tf.getStoreId());
        } else {
          flash.put("error", "Failed to insert Theatre...");
        }
      }
      redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
      return redirect;

    } finally {
      DataBaseUtil.commitClose(con, status);
    }
  }

  /**
   * Get edit theatre charge screen.
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return Rendered response
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ServletException Generic servlet exception
   * @throws Exception Other generic exception
   */
  public ActionForward geteditChargeScreen(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException, Exception {

    String theatreId = request.getParameter("theatreId");
    String orgId = request.getParameter("orgId");

    BasicDynaBean bean = OrgMasterDao.getOrgdetailsDynaBean(orgId);
    String orgName = (String) bean.get("org_name");

    String chargeType = request.getParameter("chargeType");
    String pageNum = request.getParameter("pageNum");

    TheaMasterForm tf = (TheaMasterForm) form;
    ArrayList<Hashtable<String, String>> def = dao.getTheatreDef(theatreId);
    Iterator<Hashtable<String, String>> it = def.iterator();
    Map<String, Object> filterMap = new HashMap<String, Object>();
    filterMap.put("status", "A");

    while (it.hasNext()) {
      Hashtable<String, String> ht = it.next();
      tf.setTheatreId(theatreId);
      tf.setTheatreName(ht.get("THEATRE_NAME"));
      tf.setMinDuration(Integer.parseInt(ht.get("MIN_DURATION")));
      tf.setSlab1Threshold(Integer.parseInt(ht.get("SLAB_1_THRESHOLD")));
      tf.setUnitSize(Integer.parseInt(ht.get("DURATION_UNIT_MINUTES")));
      tf.setIncrDuration(Integer.parseInt(ht.get("INCR_DURATION")));
      tf.setStatus(ht.get("STATUS"));
      tf.setAllowZeroClaimAmount(ht.get("allow_zero_claim_amount"));
      if (ht.get("SCHEDULE").equals("t")) {
        tf.setSchedule(true);
      } else {
        tf.setSchedule(false);
      }
      /*
       * if (ht.get("OVERBOOK").equals("t")) { tf.setOverbook_limit(true); } else {
       * tf.setOverbook_limit(false); }
       */

      String olimit = ht.get("OVERBOOK_LIMIT");
      tf.setOverbook_limit(olimit == null || olimit.equals("") ? null
          : Integer.parseInt(olimit));

      tf.setStoreId(Integer.parseInt(ht.get("STORE_ID")));
      tf.setOrgName(orgName);
      tf.setChargeType(chargeType);
      tf.setPageNum(pageNum);
      tf.setCenterId(Integer.parseInt(ht.get("CENTER_ID")));

    }
    if (tf.getCenterId() != 0) {
      filterMap.put("center_id", tf.getCenterId());
    }
    List<BasicDynaBean> stores = storesDao.listAll(null, filterMap, "dept_name");

    JSONSerializer js = new JSONSerializer();
    request.setAttribute("theatresLists", js.serialize(dao.getTheatresNamesAndIds()));
    request.setAttribute("_method", "updateTheatreCharges");
    request.setAttribute("theaterDetails", theatreMasterDao.findByKey("theatre_id", theatreId));
    request.setAttribute("stores", stores);
    request.setAttribute("storesJson", js.serialize(ConversionUtils.listBeanToListMap(stores)));
    request.setAttribute("multiCenters",
        GenericPreferencesDAO.getGenericPreferences().getMax_centers_inc_default() > 1);
    List<BasicDynaBean> activeInsurance = dao.getActiveInsuranceCategories(theatreId);
    StringBuilder activeInsuranceCategories = new StringBuilder();
    for (BasicDynaBean activeInsuranceCategory : activeInsurance) {
      activeInsuranceCategories.append(activeInsuranceCategory.get("insurance_category_id"));
      activeInsuranceCategories.append(",");
    }
    request.setAttribute("insurance_categories", activeInsuranceCategories.toString());
    request.setAttribute("taxsubgroup",
        ConversionUtils.copyListDynaBeansToMap(dao.getOtItemSubGroupDetails(tf.getTheatreId())));
    request.setAttribute("itemGroupTypeList", ConversionUtils
        .listBeanToListMap(itemGroupTypeDao.findAllByKey("item_group_type_id", "TAX")));
    request.setAttribute("itemGroupListJson",
        js.serialize(ConversionUtils.listBeanToListMap(itemGroupsDao.findAllByKey("status", "A"))));
    // request.setAttribute("itemGroupListJson",
    // js.serialize(ConversionUtils.listBeanToListMap(itemGroupsDao.findAllByKey("status","A"))));
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

    return mapping.findForward("addOrEditTheatre");
  }

  /**
   * Get charges for a theatre.
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return JSON Response
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ServletException Generic servlet exception
   * @throws Exception Other generic exception
   */
  public ActionForward showCharges(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws Exception {

    JSONSerializer js = new JSONSerializer();
    String theatreId = request.getParameter("theatreId");
    String orgId = request.getParameter("orgId");

    request.setAttribute("map", dao.getTheatreChargesForEdit(theatreId, orgId));
    request.setAttribute("theatresLists", js.serialize(dao.getTheatresNamesAndIds()));
    request.setAttribute("_method", "updateCharges");
    request.setAttribute("theaterDetails", theatreMasterDao.findByKey("theatre_id", theatreId));

    List<BasicDynaBean> derivedRatePlanDetails = dao.getDerivedRatePlanDetails(orgId, theatreId);

    if (derivedRatePlanDetails.size() < 0) {
      request.setAttribute("derivedRatePlanDetails", js.serialize(Collections.EMPTY_LIST));
    } else {
      request.setAttribute("derivedRatePlanDetails",
          js.serialize(ConversionUtils.copyListDynaBeansToMap(derivedRatePlanDetails)));
    }
    
    return mapping.findForward("showCharges");
  }

  /**
   * Update theatre charges.
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return Rendered response
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ServletException Generic servlet exception
   * @throws Exception Other generic exception
   */
  public ActionForward updateTheatreCharges(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException, Exception {

    TheaMasterForm tf = (TheaMasterForm) form;
    String theatreId = tf.getTheatreId();
    Theatre th = new Theatre();

    Connection con = null;
    boolean status = false;

    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);

      th.setTheatreId(theatreId);
      th.setIncrDuration(tf.getIncrDuration());
      th.setMinDuration(tf.getMinDuration());
      th.setSlab1Threshold(tf.getSlab1Threshold());
      th.setUnitSize(tf.getUnitSize());
      th.setStatus(tf.getStatus());
      th.setTheatreName(tf.getTheatreName());
      th.setSchedule(tf.getSchedule());
      th.setOverbookLimit(tf.getOverbook_limit());
      th.setCenterId(tf.getCenterId());
      th.setStoreId(tf.getStoreId());
      th.setAllowZeroClaimAmount(tf.getAllowZeroClaimAmount());
      th.setBillingGroupId(tf.getBillingGroupId());
      HttpSession session = request.getSession();
      String userName = (String) session.getAttribute("userid");

      TheatreMasterBO bo = new TheatreMasterBO();
      status = bo.addNewTheatre(th, con, userName);
      status = status && saveOrUpdateItemSubGroup(theatreId, con, request);
      status = status && saveOrUpdateInsuranceCategory(theatreId, con, request);

      FlashScope flash = FlashScope.getScope(request);
      if (status) {
        flash.put("success", "Theatre Charges are updated Successfully...");

      } else {
        flash.put("error", "Failed to update Theatre Charges...");
      }

      ActionRedirect redirect = new ActionRedirect(mapping.findForward("showRedirect"));
      redirect.addParameter("theatreId", request.getParameter("theatreID"));
      redirect.addParameter("orgId", request.getParameter("orgId"));
      redirect.addParameter("chargeType", request.getParameter("chargeType"));
      redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
      redirect.addParameter("center_id", tf.getCenterId());
      redirect.addParameter("store_id", tf.getStoreId());
      return redirect;

    } finally {
      DataBaseUtil.commitClose(con, status);
    }
  }

  /**
   * Update charges.
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return Rendered response
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ServletException Generic servlet exception
   * @throws Exception Other generic exception
   */
  public ActionForward updateCharges(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws SQLException, IOException, Exception {

    TheaMasterForm tf = (TheaMasterForm) form;
    String theatreId = tf.getTheatreId();
    ArrayList<TheatreCharges> theatreList = new ArrayList<TheatreCharges>();
    String[] bedTypes = tf.getBedTypes();
    String[] derivedRateplanIds = request.getParameterValues("ratePlanId");

    boolean status = false;
    Connection con = null;
    FlashScope flash = FlashScope.getScope(request);
    ActionRedirect redirect = new ActionRedirect(mapping.findForward("showChargesRedirect"));

    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);

      for (int i = 0; i < bedTypes.length; i++) {
        TheatreCharges thCharges = new TheatreCharges();
        thCharges.setOrgId(tf.getOrgId());
        thCharges.setBedType(bedTypes[i]);
        thCharges.setDailyCharge(new BigDecimal(tf.getDailyCharge()[i]));
        thCharges.setIncrCharge(new BigDecimal(tf.getIncrCharge()[i]));
        thCharges.setMinCharge(new BigDecimal(tf.getMinCharge()[i]));
        thCharges.setSlab1Charge(new BigDecimal(tf.getSlab1Charge()[i]));
        thCharges.setTheatreId(theatreId);
        thCharges.setDailyChargeDiscount(new BigDecimal(tf.getDailyChargeDiscount()[i]));
        thCharges.setIncrChargeDiscount(new BigDecimal(tf.getIncrChargeDiscount()[i]));
        thCharges.setMinChargeDiscount(new BigDecimal(tf.getMinChargeDiscount()[i]));
        thCharges.setSlab1ChargeDiscount(new BigDecimal(tf.getSlab1ChargeDiscount()[i]));
        theatreList.add(thCharges);
      }

      status = dao.addOrEditTheatreCharges(con, theatreList);

      if (status) {
        if (null != derivedRateplanIds && derivedRateplanIds.length > 0) {

          String[] dailyCharge = request.getParameterValues("dailyCharge");
          String[] minCharge = request.getParameterValues("minCharge");
          String[] incrCharge = request.getParameterValues("incrCharge");
          String[] slabOneCharge = request.getParameterValues("slab1Charge");

          String[] dailyChargeDiscount = request.getParameterValues("dailyChargeDiscount");
          String[] minChargeDiscount = request.getParameterValues("minChargeDiscount");
          String[] incrChargeDiscount = request.getParameterValues("incrChargeDiscount");
          String[] slabOneChargeDiscount = request.getParameterValues("slab1ChargeDiscount");

          Double[] dailyChg = new Double[dailyCharge.length];
          Double[] minChg = new Double[minCharge.length];
          Double[] incrChg = new Double[incrCharge.length];
          Double[] slabChg = new Double[slabOneCharge.length];

          Double[] dailyDisc = new Double[dailyChargeDiscount.length];
          Double[] minDisc = new Double[minChargeDiscount.length];
          Double[] incrDisc = new Double[incrChargeDiscount.length];
          Double[] slabDisc = new Double[slabOneChargeDiscount.length];

          for (int index = 0; index < dailyCharge.length; index++) {
            dailyChg[index] = new Double(dailyCharge[index]);
            minChg[index] = new Double(minCharge[index]);
            incrChg[index] = new Double(incrCharge[index]);
            slabChg[index] = new Double(slabOneCharge[index]);
            dailyDisc[index] = new Double(dailyChargeDiscount[index]);
            minDisc[index] = new Double(minChargeDiscount[index]);
            incrDisc[index] = new Double(incrChargeDiscount[index]);
            slabDisc[index] = new Double(slabOneChargeDiscount[index]);
          }

          dao.updateChargesForDerivedRatePlans(con, tf.getOrgId(), derivedRateplanIds, bedTypes,
              dailyChg, minChg, incrChg, slabChg, theatreId, dailyDisc, minDisc, incrDisc,
              slabDisc);
        }
      }

      redirect.addParameter("theatreId", theatreId);
      redirect.addParameter("orgId", tf.getOrgId());
      redirect.addParameter("chargeType", tf.getChargeType());
      redirect.addParameter("pageNum", tf.getPageNum());
      redirect.addParameter("center_id", request.getParameter("center_id"));
      redirect.addParameter("store_id", request.getParameter("store_id"));
      redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

      if (!status) {
        flash.put("error", "Failed to update Theatre Charges...");
        return redirect;
      }

    } finally {
      DataBaseUtil.commitClose(con, status);
    }

    return redirect;
  }

  /**
   * group update.
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return Rendered response
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ServletException Generic servlet exception
   * @throws Exception Other generic exception
   */
  public ActionForward groupUpdate(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException, Exception {

    String userName = (String) request.getSession(false).getAttribute("userid");



    BigDecimal amount;
    BigDecimal round;
    amount = new BigDecimal(request.getParameter("amount"));
    round = new BigDecimal(request.getParameter("round"));

    String strIncType = request.getParameter("incType");
    if (strIncType.equals("-")) {
      amount = amount.negate();
    }

    List<String> theatreArr = null;
    String[] groupTheatres = request.getParameterValues("groupTheatres");
    String strAllTheatres = request.getParameter("theatre");
    if ((groupTheatres != null || !strAllTheatres.equals(""))) {
      for (int index = 0; index < groupTheatres.length; index++) {
        theatreArr = Arrays.asList(groupTheatres);
      }
    }

    List<String> bedTypes = null;
    String[] groupBeds = request.getParameterValues("groupBeds");
    String strAllbedTypes = request.getParameter("allBedTypes");
    if (groupBeds != null || !strAllbedTypes.equals("")) {
      for (int index = 0; index < groupBeds.length; index++) {
        bedTypes = Arrays.asList(groupBeds);
      }
    }

    TheatreMasterBO bo = new TheatreMasterBO();
    TheaMasterForm tf = (TheaMasterForm) form;
    String orgId = tf.getOrgId();
    boolean success = bo.groupUpdateCharges(orgId, bedTypes, theatreArr, 
        tf.getGroupUpdatComponent(), amount, 
        request.getParameter("amtType").equals("%"), round, request.getParameter("updateTable"));

    if (success) {
      dao.updateChargesForDerivedRatePlans(orgId, userName, "theatre", false);
    }

    FlashScope flash = FlashScope.getScope(request);
    if (success) {
      flash.success("Theatre Charges are updated Successfully...");
    } else {
      flash.error("Failed to update Theatre Charges...");
    }

    ActionRedirect redirect = new ActionRedirect(mapping.findForward("getDashboard"));
    redirect.addParameter("pageNum", request.getParameter("pageNum"));
    redirect.addParameter("orgId", request.getParameter("orgId"));
    redirect.addParameter("chargeType", request.getParameter("chargeType"));
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

    return redirect;
  }

  /**
   * check for duplicate theatre.
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return Rendered response
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ServletException Generic servlet exception
   * @throws Exception Other generic exception
   */
  public ActionForward checkDuplicate(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException, Exception {

    String theatreName = request.getParameter("newTheatre");

    response.setContentType("text/plain");
    response.setHeader("Cache-Control", "no-cache");
    response.getWriter().write(String.valueOf(TheatreMasterDAO.checkDuplicate(theatreName)));

    return null;
  }

  private boolean saveOrUpdateItemSubGroup(String theatreId, Connection con,
      HttpServletRequest request) throws SQLException, IOException {
    Map params = request.getParameterMap();
    List errors = new ArrayList();

    boolean flag = true;
    String[] itemSubgroupId = request.getParameterValues("item_subgroup_id");
    String[] delete = request.getParameterValues("deleted");

    if (errors.isEmpty()) {
      if (itemSubgroupId != null && itemSubgroupId.length > 0 && !itemSubgroupId[0].equals("")) {
        GenericDAO itemsubgroupdao = new GenericDAO("theatre_item_sub_groups");
        BasicDynaBean itemsubgroupbean = itemsubgroupdao.getBean();
        ConversionUtils.copyToDynaBean(params, itemsubgroupbean, errors);
        List records = itemsubgroupdao.findAllByKey("theatre_id", theatreId);
        if (records.size() > 0) {
          flag = itemsubgroupdao.delete(con, "theatre_id", theatreId);
        }
        for (int i = 0; i < itemSubgroupId.length; i++) {
          if (itemSubgroupId[i] != null && !itemSubgroupId[i].isEmpty()) {
            if (delete[i].equalsIgnoreCase("false")) {
              itemsubgroupbean.set("theatre_id", theatreId);
              itemsubgroupbean.set("item_subgroup_id", Integer.parseInt(itemSubgroupId[i]));
              flag = itemsubgroupdao.insert(con, itemsubgroupbean);
            }
          }
        }
      }
    }
    return flag;

  }

  private boolean saveOrUpdateInsuranceCategory(String theatreId, Connection con,
      HttpServletRequest request) throws SQLException, IOException {
    boolean flag = true;
    String[] insuranceCategories = request.getParameterValues("insurance_category_id");
    if (insuranceCategories != null && insuranceCategories.length > 0
        && !insuranceCategories[0].equals("")) {
      GenericDAO insuranceCategoryDAO = new GenericDAO("theatre_insurance_category_mapping");
      BasicDynaBean insuranceCategoryBean = insuranceCategoryDAO.getBean();
      List<BasicDynaBean> records = insuranceCategoryDAO.findAllByKey("theatre_id", theatreId);
      if (records != null && records.size() > 0) {
        flag = insuranceCategoryDAO.delete(con, "theatre_id", theatreId);
      }
      for (String insuranceCategory : insuranceCategories) {
        insuranceCategoryBean.set("theatre_id", theatreId);
        insuranceCategoryBean.set("insurance_category_id", Integer.parseInt(insuranceCategory));
        flag = insuranceCategoryDAO.insert(con, insuranceCategoryBean);
      }
    }
    return flag;
  }

}
