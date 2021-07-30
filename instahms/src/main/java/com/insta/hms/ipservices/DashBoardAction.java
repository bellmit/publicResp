package com.insta.hms.ipservices;

import com.bob.hms.adminmasters.wardandbed.WardAndBedMasterDao;
import com.bob.hms.common.Constants;
import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.adminmaster.packagemaster.PackageDAO;
import com.insta.hms.adminmasters.bedmaster.BedMasterDAO;
import com.insta.hms.bedview.BedViewDao;
import com.insta.hms.billing.BillDAO;
import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.common.preferences.clinicalpreferences.ClinicalPreferencesService;
import com.insta.hms.common.utils.JsonUtility;
import com.insta.hms.diagnosticmodule.common.DiagnosticsDAO;
import com.insta.hms.diagnosticmodule.prescribetest.DiagnoDAOImpl;
import com.insta.hms.dischargesummary.DischargeSummaryBOImpl;
import com.insta.hms.editvisitdetails.EditVisitDetailsDAO;
import com.insta.hms.integration.configuration.InterfaceEventMappingService;
import com.insta.hms.ipwardassignment.WardDashBoardDAO;
import com.insta.hms.master.CenterPreferences.CenterPreferencesDAO;
import com.insta.hms.master.DepartmentMaster.DepartmentMasterDAO;
import com.insta.hms.master.DoctorMaster.DoctorMasterDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.RegistrationPreferences.RegistrationPreferencesDAO;
import com.insta.hms.orders.ConsultationTypesDAO;
import com.insta.hms.orders.OrderBO;
import com.insta.hms.orders.TestDocumentDTO;
import com.insta.hms.outpatient.DoctorConsultationDAO;
import com.insta.hms.usermanager.User;
import com.insta.hms.usermanager.UserDAO;
import com.insta.hms.util.hijricalender.UmmalquraCalendar;
import com.insta.hms.util.hijricalender.UmmalquraGregorianConverter;

import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.http.HttpHeaders;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

// TODO: Auto-generated Javadoc
/**
 * The Class DashBoardAction.
 */
public class DashBoardAction extends BaseAction {

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(DashBoardAction.class);

  /** The js. */
  final JSONSerializer js = new JSONSerializer().exclude("class");
  
  /** The c types dao. */
  final ConsultationTypesDAO consTypesDao = new ConsultationTypesDAO();
  
  /** The ward dao. */
  private final GenericDAO wardDao = new GenericDAO("ward_names");
  
  /** The bed view dao. */
  private final BedViewDao bedViewDao = new BedViewDao();
  
  /** The dashboard dao. */
  private final DashBoardDAO dashboardDao = new DashBoardDAO();

  private static InterfaceEventMappingService interfaceEventMappingService =
        ApplicationContextProvider.getBean(InterfaceEventMappingService.class);

  /**
   * Adds the to map.
   *
   * @param params the params
   * @param key the key
   * @param value the value
   */
  private void addToMap(Map params, String key, Object value) {
    if (params == null || key == null || value == null || key.equals("")) {
      return;
    }

    String[] array = (String[]) params.get(key);
    if (array == null) {
      if (value instanceof String[]) {
        params.put(key, value);
      } else {
        params.put(key, new String[] { value.toString() });
      }
    }
  }

  /**
   * Gets the IP dash board.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the IP dash board
   * @throws Exception the exception
   */
  @IgnoreConfidentialFilters
  public ActionForward getIPDashBoard(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws Exception {
    saveToken(request);

    String msg = request.getParameter("msg");
    if (msg != null) {
      request.setAttribute("message", msg);
    }
    // getInPatientDetails(request);
    int selectedPage = request.getParameter("selectedPage") != null ? Integer.parseInt(request
        .getParameter("selectedPage")) : 0;
    request.setAttribute("selectedPage", selectedPage);

    Map filterMap = new HashMap<String, Object>();
    int centerId = (Integer) request.getSession().getAttribute("centerId");
    BasicDynaBean genprefs = GenericPreferencesDAO.getAllPrefs();
    BasicDynaBean clinicalPrefs = ApplicationContextProvider.getApplicationContext()
        .getBean(ClinicalPreferencesService.class).getClinicalPreferences();
    boolean multicentered = (Integer) genprefs.get("max_centers_inc_default") > 1;

    if (centerId != 0 && multicentered) {
      filterMap.put("center_id", (Integer) request.getSession().getAttribute("centerId"));
    }
    filterMap.put("status", "A");

    boolean applyNurseRules = clinicalPrefs.get("nurse_staff_ward_assignments_applicable").equals(
        "Y");
    Connection con = DataBaseUtil.getConnection();
    String doctorId = null;
    String userName = null;
    int roleId = 0;
    try {
      User user = new UserDAO(con).getUser((String) request.getSession(false)
          .getAttribute("userid"));
      if (user != null) {
        doctorId = user.getDoctorId();
        userName = user.getName();
        roleId = user.getRoleId();
      }
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
    List wards = applyNurseRules ? new WardDashBoardDAO().getUserWardDetails(userName, centerId)
        : wardDao.listAll(null, filterMap, "ward_name");
    request.setAttribute("wardName", wards);
    request.setAttribute("wardsJSON", JsonUtility.toJson(wards));

    request.setAttribute("doctorlist", JsonUtility.toJson(dashboardDao.getDoctors()));
    request.setAttribute("patientstartdateanddayslist",
        JsonUtility.toJson(dashboardDao.getStartdateAndDaysList()));

    String billno = (String) request.getParameter("billno");
    request.setAttribute("billno", billno);

    request.setAttribute("doclist", DoctorMasterDAO.getAllActiveDoctors());
    
    Map params = new HashMap(getParameterMap(request));

    boolean isDoctorLogin = false;
    if (clinicalPrefs.get("ip_cases_across_doctors").equals("N") && doctorId != null
        && !doctorId.equals("")) {
      // if the doctor logged into the application, show patients of that doctor in IP list.
      addToMap(params, "doctor_id", doctorId);
      request.setAttribute("doctor_logged_in", doctorId);
      isDoctorLogin = true;
    }
    PagedList pagedList = dashboardDao.getInpatientList(params,
        ConversionUtils.getListingParameter(params), centerId, multicentered, isDoctorLogin,
        doctorId, applyNurseRules && roleId > 2, userName);
    request.setAttribute("pagedList", pagedList);

    return mapping.findForward("ipservicesscreen");

  }

  /**
   * Gets the new born.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the new born
   * @throws Exception the exception
   */
  public ActionForward getNewBorn(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws Exception {

    BasicDynaBean details = VisitDetailsDAO.getPatientVisitDetailsBean(request
        .getParameter("patientid"));
    request.setAttribute("motherdetails", details);
    Integer centerId = details != null ? (Integer) details.get("center_id") : 0;
    BasicDynaBean centerPrefs = new CenterPreferencesDAO().getCenterPreferences(centerId);
    request.setAttribute("centerPrefs", centerPrefs);

    request.setAttribute(
        "existBaby",
        dashboardDao.getExistBabyDetails(request.getParameter("mrno"),
            request.getParameter("patientid")));

    request.setAttribute("regPref", RegistrationPreferencesDAO.getRegistrationPreferences());

    request.setAttribute("arrdeptDetails", DepartmentMasterDAO.getDeapartmentlist());
    List docDeptNameList = EditVisitDetailsDAO.getDoctorDeptList(request.getParameter("patientid"));
    if (docDeptNameList != null) {
      request.setAttribute("docDeptNameList",
          new JSONSerializer().serialize(ConversionUtils.listBeanToListMap(docDeptNameList)));
    } else {
      request.setAttribute("docDeptNameList", new JSONSerializer().serialize(null));
    }
    request.setAttribute("bedChargesJson",
        js.serialize(ConversionUtils.listBeanToListMap(BedMasterDAO.getBillingBedDetails())));

    return mapping.findForward("newborn");
  }

  /**
   * Register.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the action forward
   * @throws Exception the exception
   */
  public ActionForward register(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws Exception {

    IPDashBoardForm dform = (IPDashBoardForm) form;
    NewBornDTO dto = new NewBornDTO();
    BeanUtils.copyProperties(dto, dform);
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    dto.setDateOfBirth(sdf.format(DataBaseUtil.parseDate(dform.getDateOfBirth())));
    dto.setVisitType(VisitDetailsDAO.getVisitType(dform.getPatientid()));
    dto.setRegDate(new java.sql.Date((new java.util.Date()).getTime()));

    String billno = (String) request.getParameter("billno");
    String salutation = (String) request.getParameter("salutation");
    dto.setSalutation(salutation);
    Integer centerId = (Integer) request.getSession().getAttribute("centerId");

    boolean result = new DashBoardBO().register(dto, billno, (String) request.getSession(false)
        .getAttribute("userid"), centerId);
    String msg = "";
    FlashScope flash = FlashScope.getScope(request);

    if (result) {
      flash.success("Baby registred successfully");
      interfaceEventMappingService.visitRegistrationEvent(dto.getAutogeneratedpatient(), true);
    } else {
      flash.error("Baby registration failed");
    }
    ActionRedirect redirect = new ActionRedirect("/patients/orders/ipflow/"
        + "index.htm#/filter/default/patient/" + URLEncoder.encode(dto.getMrNo(), "UTF-8")
        + "/order/visit/" + URLEncoder.encode(dto.getPatientid(), "UTF-8"));
    redirect.addParameter("retain_route_params", true);
    return redirect;
  }

  /**
   * Gets the wards details.
   *
   * @param request the request
   * @return the wards details
   */
  private void getWardsDetails(HttpServletRequest request) {
    WardAndBedMasterDao wardDao = new WardAndBedMasterDao();

    List wards = wardDao.getWardNames();
    request.setAttribute("wardsJSON", js.serialize(wards));
  }

  /**
   * Gets the in patient details.
   *
   * @param request the request
   * @return the in patient details
   * @throws SQLException the SQL exception
   */
  private void getInPatientDetails(HttpServletRequest request) throws SQLException {
    HttpSession session = request.getSession(false);

    int selectedPage = request.getParameter("selectedPage") != null ? Integer.parseInt(request
        .getParameter("selectedPage")) : 0;

    request.setAttribute("selectedPage", selectedPage);
    List wards = dashboardDao.getWardName();
    request.setAttribute("wardsJSON", js.serialize(wards));
    session.setAttribute("arroccupiedbeds", bedViewDao.getOccupiedBeds());
    request.setAttribute("doctorlist", js.serialize(dashboardDao.getDoctors()));
    request.setAttribute("patientstartdateanddayslist",
        js.serialize(dashboardDao.getStartdateAndDaysList()));
  }

  /**
   * Gets the patient details.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the patient details
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ServletException the servlet exception
   * @throws SQLException the SQL exception
   */
  public ActionForward getpatientDetails(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException, SQLException {
    String mrNo = request.getParameter("Mrno");
    String xmlContent = dashboardDao.getpatientXmlDetails(mrNo);
    response.setContentType("text/xml");
    response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    response.getWriter().write(xmlContent);
    response.flushBuffer();

    return null;
  }

  /**
   * Gets the ward details.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the ward details
   * @throws Exception the exception
   */
  public ActionForward getWardDetails(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws Exception {
    return null;

  }

  /**
   * Gets the ward wise patients.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the ward wise patients
   * @throws Exception the exception
   */
  public ActionForward getWardWisePatients(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws Exception {
    saveToken(request);
    Connection con = null;
    try {
      con = DataBaseUtil.getConnection();
      request.setAttribute("index", request.getParameter("selected"));

      int selectedPage = request.getParameter("selectedPage") != null ? Integer.parseInt(request
          .getParameter("selectedPage")) : 0;

      request.setAttribute("selectedPage", selectedPage);
      getWardsDetails(request);
      request.setAttribute("wardName", dashboardDao.getWardName());
      request.setAttribute("arroccupiedbeds", bedViewDao.getOccupiedBeds());
      request.setAttribute("patientslist", js.serialize(dashboardDao.getInPatientDetails()));
      int offSetVal = request.getParameter("offsetval") != null ? Integer.parseInt(request
          .getParameter("offsetval")) : 0;
      String wardname = request.getParameter("wardname");
      request.setAttribute("admittedpatients",
          dashboardDao.getWardWisePatients(wardname, offSetVal));

      SearchDTO sdto = new SearchDTO();
      int count = dashboardDao.fetchNoOfRecordsBasedOnSearch(sdto, wardname);
      count = count / 15;
      request.setAttribute("count", count);
      String patientId = request.getParameter("patientid");
      request.setAttribute("doctorlist", js.serialize(dashboardDao.getDoctors()));
      request.setAttribute("patientsawaiting",
          js.serialize(dashboardDao.getListOfAwaitingIPPatients()));
      request.setAttribute("bedTypes", dashboardDao.getBedTypes());
      request.setAttribute("freebeds", dashboardDao.getFreeBeds());
      request.setAttribute("freebedjason", js.serialize(dashboardDao.getFreeBeds()));
      request.setAttribute("wardName", dashboardDao.getWardNameForBeds());
      request.setAttribute("opencreditbills", js.serialize(ConversionUtils
          .copyListDynaBeansToMap(BillDAO.getIPOpenCreditBills(patientId))));
      request.setAttribute("dischargeStatuses", js.serialize(ConversionUtils
          .copyListDynaBeansToMap(BillDAO.getDischargeStatuses(patientId))));
      request.setAttribute("patientvisitdetails",
          VisitDetailsDAO.getPatientVisitDetailsBean(patientId));
    } finally {
      if (con != null) {
        con.close();
      }
    }

    return mapping.findForward("ipservicesscreen");
  }

  /**
   * Gets the search data.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the search data
   * @throws Exception the exception
   */
  public ActionForward getSearchData(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws Exception {
    saveToken(request);
    Connection con = null;
    try {
      con = DataBaseUtil.getConnection();
      IPDashBoardForm dform = (IPDashBoardForm) form;
      SearchDTO sdto = new SearchDTO();

      if (dform.getFirstName() != null) {
        sdto.setFirstName(dform.getFirstName());
      }
      if (dform.getLastName() != null) {
        sdto.setLastName(dform.getLastName());
      }
      if (dform.getSearchmrno() != null) {
        sdto.setSearchmrno(dform.getSearchmrno());
      }

      String selectedDoctor = request.getParameter("selectedDoctor") != null ? request
          .getParameter("selectedDoctor") : "";
          
      if (!(selectedDoctor.equals(""))) {
        sdto.setSearchdoctor(selectedDoctor);
      } else {
        if (dform.getSearchdoctor() != null) {
          sdto.setSearchdoctor(dform.getSearchdoctor());
        }
      }

      int selectedPage = request.getParameter("selectedPage") != null ? Integer.parseInt(request
          .getParameter("selectedPage")) : 0;
      request.setAttribute("selectedPage", selectedPage);

      request.setAttribute("index", request.getParameter("selected"));
      request.setAttribute("selectedConsultantIndex", request.getParameter("selectedConsultant"));

      getWardsDetails(request);
      request.setAttribute("wardName", dashboardDao.getWardName());
      String patientId = request.getParameter("patientid");
      request.setAttribute("selectedSearchMrno", dform.getSearchmrno());
      request.setAttribute("selectedSearchFirstName", dform.getFirstName());
      request.setAttribute("selectedSearchLastName", dform.getLastName());
      request.setAttribute("selectedSearchdoctor", dform.getSearchdoctor());

      request.setAttribute("arroccupiedbeds", bedViewDao.getOccupiedBeds());
      request.setAttribute("patientslist", js.serialize(dashboardDao.getInPatientDetails()));
      int offSetVal = request.getParameter("offsetval") != null ? Integer.parseInt(request
          .getParameter("offsetval")) : 0;
      request.setAttribute("admittedpatients", dashboardDao.getSearchData(sdto, offSetVal));
      request.setAttribute("patientsawaiting",
          js.serialize(dashboardDao.getListOfAwaitingIPPatients()));
      request.setAttribute("opencreditbills", js.serialize(ConversionUtils
          .copyListDynaBeansToMap(BillDAO.getIPOpenCreditBills(patientId))));
      request.setAttribute("dischargeStatuses", js.serialize(ConversionUtils
          .copyListDynaBeansToMap(BillDAO.getDischargeStatuses(patientId))));

      int count = dashboardDao.fetchNoOfRecordsBasedOnSearch(sdto, "");
      count = count / 15;
      request.setAttribute("count", count);
      request.setAttribute("bedTypes", dashboardDao.getBedTypes());
      request.setAttribute("freebeds", dashboardDao.getFreeBeds());
      request.setAttribute("freebedjason", js.serialize(dashboardDao.getFreeBeds()));
      request.setAttribute("wardName", dashboardDao.getWardNameForBeds());
      request.setAttribute("doctorlist", js.serialize(dashboardDao.getDoctors()));
      request.setAttribute("patientvisitdetails",
          VisitDetailsDAO.getPatientVisitDetailsBean(request.getParameter("patientid")));
    } finally {
      if (con != null) {
        con.close();
      }
    }
    return mapping.findForward("ipservicesscreen");
  }

  /**
   * Gets the warddetailsjson.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the warddetailsjson
   * @throws Exception the exception
   */
  public ActionForward getWarddetailsjson(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws Exception {
    return null;

  }

  /**
   * Gets the patient details for bill.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the patient details for bill
   * @throws Exception the exception
   */
  public ActionForward getPatientDetailsForBill(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws Exception {

    Hashtable ht1 = null;
    Hashtable ht = null;
    String url = null;
    Connection con = null;
    boolean admitStatus = false;
    String billno = null;
    try {
      con = DataBaseUtil.getConnection();
      billno = request.getParameter("billno");
      request.getSession(false).setAttribute("arroccupiedbeds", bedViewDao.getOccupiedBeds());
      request.getSession(false).setAttribute("age", request.getParameter("age"));
      request.getSession(false).setAttribute("sex", request.getParameter("gender"));
      request.setAttribute("bed", request.getParameter("bed"));
      request.setAttribute("patientvisitdetails",
          VisitDetailsDAO.getPatientVisitDetailsBean(request.getParameter("patientid")));

      ArrayList list = (ArrayList) dashboardDao.getPatientDetailsForBill(request
          .getParameter("patientid"));

      ht = (Hashtable) list.get(0);

      ArrayList admitlist = (ArrayList) dashboardDao.getPatientDetailsForAdmit(request
          .getParameter("patientid"));
      if (admitlist.size() > 0) {
        ht1 = (Hashtable) admitlist.get(0);
        admitStatus = true;
      }
    } finally {
      if (con != null) {
        con.close();
      }
    }
    url = "/pages/ipservices/Ipservices.do";
    ActionRedirect redirect = new ActionRedirect(url);
    redirect.addParameter("method", "getIpBedDetailsScreen");
    redirect.addParameter("bedno", ht.get("BED_NAME"));
    redirect.addParameter("mrno", ht.get("MR_NO"));
    redirect.addParameter("patientname", ht.get("PATIENT_NAME"));
    redirect.addParameter("doctor", ht.get("DOCTOR_NAME"));
    redirect.addParameter("patientid", ht.get("PATIENT_ID"));
    redirect.addParameter("doa", ht.get("ADMIT_DATE"));
    redirect.addParameter("ward", ht.get("WARD_NAME"));
    redirect.addParameter("orgid", ht.get("PATIENT_ORG_ID"));
    redirect.addParameter("billno", billno);
    redirect.addParameter("admitStatus", admitStatus);
    redirect.addParameter("deptid", ht.get("DEPT_ID"));

    // response.sendRedirect(url);
    return redirect;

  }

  /**
   * Discharges patient visit and updates patient registration tables.
   *
   * @param mapping the mapping
   * @param af the af
   * @param request the request
   * @param response the response
   * @return the action forward
   * @throws Exception the exception
   */
  public ActionForward patientDischarge(ActionMapping mapping, ActionForm af,
      HttpServletRequest request, HttpServletResponse response) throws Exception {
    HttpSession session = request.getSession(false);
    String mrNo = request.getParameter("mrno") != null ? request.getParameter("mrno") : "";
    String patientID = request.getParameter("patid") != null ? request.getParameter("patid") : "";
    Connection con = null;
    boolean result = false;
    String disDate = null;
    String disTime = null;
    String msg = "Successfully Discharged";
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      FlashScope flash = FlashScope.getScope(request);
      List prevBedDetails = new IPBedDAO().getBeds(patientID);
      BedDTO bed = null;
      for (int i = 0; i < prevBedDetails.size(); i++) {
        bed = (BedDTO) prevBedDetails.get(i);
        if (bed.getBed_state().equals("F")
            && (bed.getStatus().equals("A") || bed.getStatus().equals("C"))) {
          SimpleDateFormat timeFornmater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
          SimpleDateFormat dateFornmater = new SimpleDateFormat("yyyy-MM-dd");
          // greater among currentdate and bed finalized date will be discharged date of patient
          if (DataBaseUtil.getDateandTime().compareTo(timeFornmater.parse(bed.getEnddate())) > 0) {
            disDate = DataBaseUtil.dateFormatter.format(new java.util.Date());
            disTime = new java.sql.Time(new java.util.Date().getTime()).toString();
          } else {

            disDate = DataBaseUtil.dateFormatter.format(dateFornmater.parse(bed.getEnddate()
                .split(" ")[0]));
            disTime = bed.getEnddate().split(" ")[1];
            flash.info("Bed is finalised for " + disDate + " " + disTime.substring(0, 5)
                + ",this will be the discharge date");
          }
        }
        if (!bed.getBed_state().equals("F")) {
          disDate = DataBaseUtil.dateFormatter.format(new java.util.Date());
          disTime = new java.sql.Time(new java.util.Date().getTime()).toString();
          break;
        }
      }
      result = new DischargeSummaryBOImpl().dischargePatient(con, mrNo, patientID,
          (String) session.getAttribute("userid"), disDate, disTime);

      if (result) {
        flash.success(msg);
      } else {
        flash.error("Failed to  Discharge");
      }
      String action = "adt";
      String method = "getADTScreen";
      if (request.getParameter("dischargeFromBedView") != null) {
        action = "BedView";
        method = "getBedView";
      }
      String url = "/pages/ipservices/" + action + ".do?_method=" + method;
      ActionRedirect redirect = new ActionRedirect(url);
      redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
      return redirect;
    } finally {
      DataBaseUtil.commitClose(con, result);
    }
  }

  /**
   * Gets the ADT screen.
   *
   * @param mapping the mapping
   * @param af the af
   * @param request the request
   * @param response the response
   * @return the ADT screen
   * @throws Exception the exception
   */
  @IgnoreConfidentialFilters
  public ActionForward getADTScreen(ActionMapping mapping, ActionForm af,
      HttpServletRequest request, HttpServletResponse response) throws Exception {
    saveToken(request);

    String msg = request.getParameter("msg");
    if (msg != null) {
      request.setAttribute("message", msg);
    }


    int selectedPage = request.getParameter("selectedPage") != null ? Integer.parseInt(request
        .getParameter("selectedPage")) : 0;

    request.setAttribute("selectedPage", selectedPage);
    List wards = dashboardDao.getWardName();
    List doctors = dashboardDao.getDoctors();
    request.setAttribute("wardsJSON", js.serialize(wards));
    request.setAttribute("doctorlist", js.serialize(doctors));
    request.setAttribute("patientstartdateanddayslist",
        js.serialize(dashboardDao.getStartdateAndDaysList()));

    request.setAttribute("ip_preferences", new GenericDAO("ip_preferences").getRecord().getMap());

    String billno = (String) request.getParameter("billno");
    request.setAttribute("billno", billno);


    // in multi center scheema wards must belong to user center.
    Map filterMap = new HashMap<String, Object>();
    int centerId = (Integer) request.getSession().getAttribute("centerId");
    boolean multicentered = GenericPreferencesDAO.getGenericPreferences()
        .getMax_centers_inc_default() > 1;

    if (centerId != 0 && multicentered) {
      filterMap.put("center_id", (Integer) request.getSession().getAttribute("centerId"));
    }
    filterMap.put("status", "A");

    Connection con = DataBaseUtil.getConnection();
    String userName = null;
    int roleId = 0;
    try {
      User user = new UserDAO(con).getUser((String) request.getSession(false)
          .getAttribute("userid"));
      if (user != null) {
        userName = user.getName();
        roleId = user.getRoleId();
      }

    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
    
    BasicDynaBean clinicalPrefs = ApplicationContextProvider.getApplicationContext()
        .getBean(ClinicalPreferencesService.class).getClinicalPreferences();
    boolean applyNurseRules = clinicalPrefs.get("nurse_staff_ward_assignments_applicable").equals(
        "Y");
    List wardsList = applyNurseRules ? new WardDashBoardDAO()
        .getUserWardDetails(userName, centerId) : wardDao.listAll(null, filterMap, "ward_name");
    request.setAttribute("wards", wardsList);
    request.setAttribute("doctors", doctors);
    Map map = getParameterMap(request);
    PagedList pagedList = dashboardDao.getAdmittedPatientDetails1(map,
        ConversionUtils.getListingParameter(map), centerId, multicentered, false, null,
        applyNurseRules && roleId > 2, userName);
    request.setAttribute("pagedList", pagedList);

    return mapping.findForward("adtscreen");

  }

  /**
   * Gets the all doctor visits.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the all doctor visits
   * @throws ServletException the servlet exception
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public ActionForward getAllDoctorVisits(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws ServletException,
      SQLException, IOException {

    String bedtype = request.getParameter("bed") == null
        || (request.getParameter("bed")).equals("") ? "GENERAL" : request.getParameter("bed");
    String orgid = request.getParameter("orgid");
    String patientId = request.getParameter("patientId");
    request.setAttribute("bed", bedtype);
    request.setAttribute("orgid", orgid);
    request.setAttribute("doctorVisitsList", DoctorConsultationDAO.getDoctorVisits(patientId, "i"));
    request.setAttribute("consultation_types", consTypesDao.getConsultationTypes("i", orgid, ""));
    Map doctormap = new HashMap();
    doctormap.put("doctors", new DoctorMasterDAO().getAllDoctorDeptCharges(bedtype, orgid));
    request.setAttribute("AllDoctorsList", js.deepSerialize(doctormap));

    return mapping.findForward("getAllDoctorVisits");
  }

  /**
   * Adds the new doctor visit.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the action forward
   * @throws Exception the exception
   */
  public ActionForward addNewDoctorVisit(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws Exception {

    Connection con = null;
    boolean success = false;
    String patientId = request.getParameter("patientId");
    HttpSession session = request.getSession(false);
    String userName = (String) session.getAttribute("userId");

    ActionRedirect redirect = new ActionRedirect("Ipservices.do?_method=getAllDoctorVisits");

    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      GenericDAO docDao = new GenericDAO("doctor_consultation");
      BasicDynaBean bean = docDao.getBean();
      bean.set("head", request.getParameter("chargetype1"));
      bean.set("doctor_name", request.getParameter("doctorId1"));
      bean.set("presc_date", DateUtil.getCurrentTimestamp());
      bean.set(
          "visited_date",
          DateUtil.parseTimestamp(request.getParameter("visit_date") + " "
              + request.getParameter("visit_time")));
      bean.set("status", "A");

      ArrayList<BasicDynaBean> beanList = new ArrayList<BasicDynaBean>();
      beanList.add(bean);
      OrderBO order = new OrderBO();
      String billno = new BillDAO(con).getPatientCreditBillOpenOnly(patientId, true, true);
      order.setBillInfo(con, patientId, billno, false, userName);
      order.orderItems(con, beanList, new ArrayList<String>(), new ArrayList<Integer>(),
          new ArrayList<String>(), new ArrayList<String>(), null, null, null, null, 0, new Boolean(
              request.getParameter("isChargeable")), null, null, null,
          new ArrayList<List<TestDocumentDTO>>());
      success = true;
    } finally {
      DataBaseUtil.commitClose(con, success);
    }

    redirect.addParameter("bed", request.getParameter("bed"));
    redirect.addParameter("orgid", request.getParameter("orgid"));
    redirect.addParameter("patientId", request.getParameter("patientId"));
    return redirect;
  }

  /**
   * Gets the revisit count of doctor.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the revisit count of doctor
   * @throws ServletException the servlet exception
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public ActionForward getRevisitCountOfDoctor(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws ServletException,
      SQLException, IOException {

    String doctor = request.getParameter("doctor");
    String mrno = request.getParameter("mrno");
    BasicDynaBean revisitbean = null;
    if (mrno != null) {
      revisitbean = VisitDetailsDAO.getRevisitDetails(mrno, doctor);
    }
    response.setContentType("text/javascript");
    response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    if (revisitbean != null) {
      response.getWriter().write(js.serialize(revisitbean.getMap()));
    } else {
      response.getWriter().write(js.serialize(null));
    }
    response.flushBuffer();
    return null;
  }

  /**
   * Gets the test or package charge.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the test or package charge
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ServletException the servlet exception
   * @throws SQLException the SQL exception
   */

  @IgnoreConfidentialFilters
  public ActionForward getTestOrPackageCharge(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException, SQLException {
    String strOrgId = request.getParameter("orgid");
    String strBedType = request.getParameter("bedtype");
    String id = request.getParameter("testid");
    String strPriority = request.getParameter("priority");
    String type = request.getParameter("type");

    if (strOrgId.equals("")) {
      strOrgId = "ORG0001";
    }
    if (strBedType.equals("")) {
      strBedType = Constants.getConstantValue("BEDTYPE");
    }

    JSONSerializer js = new JSONSerializer().exclude("class");
    Connection con = null;
    BigDecimal charge = BigDecimal.ZERO;
    BigDecimal discount = BigDecimal.ZERO;
    try {
      con = DataBaseUtil.getConnection();
      if (type.equals("DIA")) {
        List testList = new DiagnoDAOImpl().getTestChargeForOrganizationAndBedType(id, strOrgId,
            strBedType, strPriority);
        Hashtable ht = (Hashtable) testList.get(0);
        charge = new BigDecimal(ht.get("CHARGE").toString());
        discount = new BigDecimal(ht.get("DISCOUNT").toString());
      } else {
        BasicDynaBean bean = new PackageDAO(con).getPackageCharge(Integer.parseInt(id), strOrgId,
            strBedType);
        charge = (BigDecimal) bean.get("charge");
        discount = (BigDecimal) bean.get("discount");
      }
      Map map = new HashMap<String, String>();
      map.put("charge", charge);
      map.put("discount", discount);
      response.setContentType("text/javascript");
      response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
      response.getWriter().write(js.deepSerialize(map));
      response.flushBuffer();
      return null;
    } finally {
      if (con != null) {
        con.close();
      }
    }
  }

  /**
   * Ajax pending tests check.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the action forward
   * @throws Exception the exception
   */
  public ActionForward ajaxPendingTestsCheck(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws Exception {

    String visitId = request.getParameter("visitId");
    boolean testexists = DiagnosticsDAO.isPendingTestExist(visitId);

    response.setContentType("text/javascript");
    response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    if (!testexists) {
      response.getWriter().write("Not Pending");
    } else {
      response.getWriter().write("Pending");
    }

    return null;
  }

  /**
   * Ajax credit bill check.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the action forward
   * @throws Exception the exception
   */
  public ActionForward ajaxCreditBillCheck(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws Exception {

    String visitId = request.getParameter("visitId");

    List<BasicDynaBean> bills = BillDAO.getIPOpenCreditBills(visitId);

    response.setContentType("text/javascript");
    response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    if (!bills.isEmpty()) {
      response.getWriter().write("CreditBillExists");
    } else {
      response.getWriter().write("No CreditBillExists");
    }

    return null;
  }

  /**
   * Check open unpaid credit bills.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the action forward
   * @throws Exception the exception
   */
  public ActionForward checkOpenUnpaidCreditBills(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws Exception {

    String visitId = request.getParameter("visitId");

    List<BasicDynaBean> bills = BillDAO.getIPOpenCreditBills(visitId, true);
    response.setContentType("text/javascript");
    response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    if (!bills.isEmpty()) {
      response.getWriter().write("CreditBillExists");
    } else {
      response.getWriter().write("No CreditBillExists");
    }

    return null;
  }

  /**
   * Gets the gregorian to hijri.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the gregorian to hijri
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws Exception the exception
   */
  public ActionForward getGregorianToHijri(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws IOException, Exception {

    String day = request.getParameter("dobDay");
    String month = request.getParameter("dobMonth");
    String year = request.getParameter("dobYear");

    Calendar cal = new GregorianCalendar(Integer.parseInt(year), Integer.parseInt(month) - 1,
        Integer.parseInt(day));
    int[] hijriDateInfo = UmmalquraGregorianConverter.toHijri(cal.getTime());

    response.setContentType("application/x-json");
    response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");

    Map result = new HashMap();
    result.put("day", hijriDateInfo[2]);
    result.put("month", hijriDateInfo[1]);
    result.put("year", hijriDateInfo[0]);
    JSONSerializer js = new JSONSerializer();
    response.getWriter().write(js.serialize(result));

    response.flushBuffer();
    return null;
  }

  /**
   * Gets the hijri to gregorian.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the hijri to gregorian
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws Exception the exception
   */
  public ActionForward getHijriToGregorian(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws IOException, Exception {
    String day = request.getParameter("dobDay");
    String month = request.getParameter("dobMonth");
    String year = request.getParameter("dobYear");

    UmmalquraCalendar cal = new UmmalquraCalendar(Integer.parseInt(year), Integer.parseInt(month),
        Integer.parseInt(day));
    java.util.Date date = cal.getTime();

    response.setContentType("application/x-json");
    response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    String dateStr = DateUtil.formatDate(date);
    String[] dateArr = dateStr.split("-");
    Map result = new HashMap();
    result.put("day", dateArr[0]);
    result.put("month", dateArr[1]);
    result.put("year", dateArr[2]);
    JSONSerializer js = new JSONSerializer();
    response.getWriter().write(js.serialize(result));

    response.flushBuffer();
    return null;
  }
}
