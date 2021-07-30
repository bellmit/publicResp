package com.insta.hms.bedview;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.adminmasters.bedmaster.BedMasterDAO;
import com.insta.hms.billing.BillDAO;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.diagnosticmodule.common.DiagnosticsDAO;
import com.insta.hms.ipservices.BedChargeJobDetails;
import com.insta.hms.ipservices.BedDTO;
import com.insta.hms.ipservices.DashBoardDAO;
import com.insta.hms.ipservices.IpBedAction;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;

import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class BedViewAction extends IpBedAction {
  private JSONSerializer js = new JSONSerializer().exclude("class");
  private GenericDAO wardsDao = new GenericDAO("ward_names");
  private BedViewDao bedViewDao = new BedViewDao();
  private BedMasterDAO bedMasterDao = new BedMasterDAO();
  private DashBoardDAO dashboardDao = new DashBoardDAO();

  /**
   * Gets the bed view.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the bed view
   * @throws Exception the exception
   */
  @IgnoreConfidentialFilters
  public ActionForward getBedView(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws Exception {
    saveToken(request);

    Map filterMap = new HashMap<String, Object>();
    int centerId = (Integer) request.getSession().getAttribute("centerId");
    boolean multicentered = GenericPreferencesDAO.getGenericPreferences()
        .getMax_centers_inc_default() > 1;

    if (centerId != 0 && multicentered) {
      filterMap.put("center_id", (Integer) request.getSession().getAttribute("centerId"));
    }
    filterMap.put("status", "A");

    List wards = wardsDao.listAll(null, filterMap, "ward_name");
    HttpSession session = request.getSession(false);
    
    request.setAttribute("wardName", wards);
    request.setAttribute("wardsJSON", js.serialize(ConversionUtils.listBeanToListMap(wards)));
    session.setAttribute("arroccupiedbeds", bedViewDao.getOccupiedBeds());
    request.setAttribute("doctorlist", js.serialize(dashboardDao.getDoctors()));
    request.setAttribute("patientstartdateanddayslist",
        js.serialize(dashboardDao.getStartdateAndDaysList()));
    request.setAttribute("bedNames", js.serialize(
        ConversionUtils.listBeanToListMap(BedMasterDAO.getAllBedNames(centerId, multicentered))));

    List beds = dashboardDao.getFreeBeds();
    String billno = (String) request.getParameter("billno");
    request.setAttribute("billno", billno);
    request.setAttribute("bedTypes", dashboardDao.getBedTypes());
    request.setAttribute("freebeds", beds);
    request.setAttribute("freebedjason", js.serialize(beds));
    request.setAttribute("pendingtests", js.serialize(DiagnosticsDAO.getAllPendingTests("IP")));
    request.setAttribute("wards", wards);
    request.setAttribute("doctors", dashboardDao.getDoctors());
    Map map = getParameterMap(request);
    String[] pageNumbers = new String[1];
    pageNumbers[0] = "0";
    map.put("pageSize", pageNumbers);

    PagedList pagedList = bedViewDao.getBedView(map, ConversionUtils.getListingParameter(map),
        (String) request.getSession(false).getAttribute("userid"), centerId, multicentered);

    request.setAttribute("pagedList", pagedList);
    request.setAttribute("userWard", (String) ((String[]) map.get("ward_no"))[0]);

    return mapping.findForward("bedviewscreen");

  }

  /**
   * Gets the allocate bed screen.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the allocate bed screen
   * @throws Exception the exception
   */
  @IgnoreConfidentialFilters
  public ActionForward getAllocateBedScreen(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws Exception {
    String orgId = "ORG0001";
    BedDTO bedDetails = BedMasterDAO
        .getBedDeails(Integer.parseInt(request.getParameter("bed_id")));
    request.setAttribute("beddetails", bedDetails);
    request.setAttribute("beddetailsJSON", js.serialize(bedDetails));
    request.setAttribute("bedtypesJSON", js
        .serialize(ConversionUtils.copyListDynaBeansToMap(new GenericDAO("bed_types").listAll())));
    BasicDynaBean details = VisitDetailsDAO
        .getPatientVisitDetailsBean(request.getParameter("patient_id"));
    if (details != null) {
      orgId = (String) details.get("org_id");
    }
    request.setAttribute("ip_preferences", new GenericDAO("ip_preferences").getRecord().getMap());
    request.setAttribute("normalbed_initialpayments",
        js.serialize(bedMasterDao.getBedWardCharges(orgId)));
    request.setAttribute("icubed_initialpayments",
        js.serialize(bedMasterDao.getIcuWardCharges("GENERAL", orgId)));
    request.setAttribute("nonICUBedTypes",
        js.serialize(ConversionUtils.listBeanToListMap(BedMasterDAO.getNonIcuBedtypes())));
    if (details != null) {
      request.setAttribute("initial_payment",
          js.serialize(dashboardDao.getBedDetails(details.get("patient_id").toString())));
      request.setAttribute("wardNamejson", js.serialize(dashboardDao.getWardNameForBeds()));
      request.setAttribute("bedChargesJson",
          js.serialize(ConversionUtils.listBeanToListMap(BedMasterDAO.getAllBedDetails())));
      request.setAttribute("details", details);
      request.setAttribute("detailsJSON", js.serialize(details.getMap()));
      request.setAttribute("billadvancebalance",
          dashboardDao.getAdvanceBalance(details.get("patient_id").toString()));
      List bills = BillDAO.getActiveHospitalBills(details.get("patient_id").toString(),
          BillDAO.bill_type.BOTH);
      request.setAttribute("billnos", bills);
      request.setAttribute("billAmtDetailsJson",
          js.serialize(ConversionUtils.listBeanToListMap(bills)));
    }

    int centerId = (Integer) request.getSession().getAttribute("centerId");
    boolean multiCentered = GenericPreferencesDAO.getGenericPreferences()
        .getMax_centers_inc_default() > 1;
    if (multiCentered && centerId == 0) {
      request.setAttribute("error", "Bed Allocation is allowed only for center users.");
    }
    request.setAttribute("multiCentered", multiCentered);
    request.setAttribute("bedChargeJobDetails", BedChargeJobDetails.getJobTimeSummery());

    return mapping.findForward("allocatebedscreen");
  }

  /**
   * Gets the bed status screen.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the bed status screen
   * @throws Exception the exception
   */
  @IgnoreConfidentialFilters
  public ActionForward getBedStatusScreen(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws Exception {
    request.setAttribute("beddetails",
        BedMasterDAO.getBedDetailsBean(Integer.parseInt(request.getParameter("bed_id"))));
    return mapping.findForward("bedstatusmodificationscreen");
  }

  /**
   * Modify bed status.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the action forward
   * @throws Exception the exception
   */
  public ActionForward modifyBedStatus(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws Exception {
    List errs = new ArrayList();
    BasicDynaBean bedNamesBean = new GenericDAO("bed_names").getBean();
    ConversionUtils.copyIndexToDynaBean(request.getParameterMap(), 0, bedNamesBean, errs);
    Timestamp avblTimeStamp = DataBaseUtil.parseTimestamp(
        request.getParameter("avbl_date") + " " + request.getParameter("avbl_time") + ":00");
    if (((String) bedNamesBean.get("bed_status")).equals("A")) {
      avblTimeStamp = null;
    }

    bedNamesBean.set("avilable_date", avblTimeStamp);

    boolean status = bedViewDao.modifyBedStatus(bedNamesBean);

    FlashScope flash = FlashScope.getScope(request);
    if (!status) {
      flash.error("Failed to modify bed status,Please try again");
    }

    ActionRedirect redirect = new ActionRedirect("BedView.do?_method=getBedStatusScreen");
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
    redirect.addParameter("bed_id", bedNamesBean.get("bed_id"));
    return redirect;
  }
}
