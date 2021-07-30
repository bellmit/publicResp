package com.insta.hms.core.patient.registration;

import com.bob.hms.common.DateUtil;
import com.bob.hms.common.MimeTypeDetector;
import com.insta.hms.common.BaseRestController;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.patient.URLRoute;
import com.insta.hms.exception.HMSException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ModelMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The Class RegistrationController.
 */
@RestController
@RequestMapping(URLRoute.OP_REGISTRATION_URL)
public class RegistrationController extends BaseRestController {

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(RegistrationController.class);

  /** The registration service. */
  @LazyAutowired
  private RegistrationService registrationService;

  /**
   * Gets the registration index page.
   *
   * @return the registration index page
   */
  @IgnoreConfidentialFilters
  @GetMapping(URLRoute.VIEW_INDEX_URL)
  public ModelAndView getRegistrationIndexPage() {
    return renderFlowUi("OP Registration", "v12", "withFlow", "opFlow", "registration", false);
  }

  /**
   * Gets the details.
   *
   * @param request
   *          the request
   * @param mmap
   *          the mmap
   * @param response
   *          the response
   * @return the details
   */
  @IgnoreConfidentialFilters
  @RequestMapping(value = "/details", method = RequestMethod.GET)
  public Map<String, Object> getdetails(HttpServletRequest request, ModelMap mmap,
      HttpServletResponse response) {
    return registrationService.getDetails(request.getParameterMap(),
        (String) request.getAttribute("language"));
  }

  /**
   * Creates the.
   *
   * @param request
   *          the request
   * @param response
   *          the response
   * @param requestBody
   *          the request body
   * @return the map
   * @throws Exception
   *           the exception
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  @RequestMapping(value = "/create", method = RequestMethod.POST, consumes = "application/json")
  public Map<String, Object> create(HttpServletRequest request, HttpServletResponse response,
      @RequestBody ModelMap requestBody) throws Exception {
    if (requestBody == null) {
      throw new HMSException(HttpStatus.BAD_REQUEST, "exception.bad.request", null);
    }
    Map<String, Object> map = null;
    Integer contactId = (Integer) ((Map)requestBody.get("patient")).get("contact_id");
    try {
      registrationService.obtainLock();
      map = registrationService.createNewVisit(requestBody);
    } finally {
      registrationService.releaseLock();
    }
    map = registrationService.createReceipt(requestBody, map);
    if (contactId != null) {
      String mrNo = (String) ((Map)map.get("patient")).get("mr_no");
      registrationService.convertContactToMrNo(mrNo, contactId);
      ((Map)map.get("patient")).put("contact_id",null);
    }
    registrationService.pushAppointmentsToWebSockets(requestBody);
    return map;
  }

  /**
   * Load details.
   *
   * @param request
   *          the request
   * @param mmap
   *          the mmap
   * @param response
   *          the response
   * @return the map
   */
  @RequestMapping(value = "/show", method = RequestMethod.GET)
  public Map<String, Object> loadDetails(HttpServletRequest request, ModelMap mmap,
      HttpServletResponse response) {
    Map<String, Object> data = new HashMap<String, Object>();
    String appointmentId = request.getParameter("appointment_id");
    String visitId = request.getParameter("visit_id");
    String mrno = request.getParameter("mr_no");
    String contactId = request.getParameter("contact_id");
    if (appointmentId != null && !appointmentId.equals("")) {
      data = registrationService.getAppointmentDetails(appointmentId);
    } else if (visitId != null && !visitId.equals("")) {
      data = registrationService.getPatientVisitDetails(visitId);
    } else if (mrno != null && !mrno.equals("")) {
      data = registrationService.getPatientDetailsForNewVisit(mrno);
    } else if (contactId != null && !contactId.equals("")) {
      data = registrationService.getContactPatientDetailsForNewVisit(Integer.parseInt(contactId));
    } else {
      throw new HMSException(HttpStatus.BAD_REQUEST, "exception.bad.request", null);
    }
    data.put("server_time", new Date());
    return data;
  }

  /**
   * Update details.
   *
   * @param request
   *          the request
   * @param response
   *          the response
   * @param requestBody
   *          the request body
   * @return the map
   * @throws NoSuchAlgorithmException
   *           the no such algorithm exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws ParseException
   *           the parse exception
   */
  @SuppressWarnings("unchecked")
  @RequestMapping(value = "/update", method = RequestMethod.POST, consumes = "application/json")
  public Map<String, Object> updateDetails(HttpServletRequest request,
      HttpServletResponse response, @RequestBody ModelMap requestBody)
      throws NoSuchAlgorithmException, IOException, ParseException {
    if (requestBody == null) {
      throw new HMSException(HttpStatus.BAD_REQUEST, "exception.bad.request", null);
    }
    registrationService.updatePatientVisit(requestBody);
    // then send the updated details again ?
    String visitId = requestBody.get("visit") != null ? (String) ((Map<String, Object>) requestBody
        .get("visit")).get("patient_id") : "";
    return registrationService.getPatientVisitDetails(visitId);
  }

  /**
   * Gets the visits.
   *
   * @param request
   *          the request
   * @param mmap
   *          the mmap
   * @param response
   *          the response
   * @return the visits
   */
  @RequestMapping(value = "/visits", method = RequestMethod.GET)
  public Map<String, Object> getVisits(HttpServletRequest request, ModelMap mmap,
      HttpServletResponse response) {
    Map<String, Object> visits = new HashMap<String, Object>();
    // getting op visits.
    boolean activeOnly = null != request.getParameter("status")
        && request.getParameter("status").equals("A");
    HashMap<String, BigDecimal> cashLimitMap = registrationService.getPatientCashLimitDetails(
            request.getParameter("mr_no"));
    visits
        .put("visits", registrationService.getPatientVisits(request.getParameter("mr_no"), "o",
            activeOnly, false));
    visits.put("cashLimitDetails", cashLimitMap);
    return visits;
  }

  /**
   * Gets the patient doctor visits.
   *
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the patient doctor visits
   */
  @RequestMapping(value = "/getpatientdoctorvisits", method = RequestMethod.GET)
  public Map<String, Object> getPatientDoctorVisits(HttpServletRequest request,
      HttpServletResponse response) {
    String mrNo = request.getParameter("mr_no");
    String doctor = request.getParameter("doctor_id");
    String opType = request.getParameter("op_type");
    Map<String, Object> map = new HashMap<String, Object>();
    map.put("patient_doctor_visits",
        registrationService.getPatientDoctorVisits(mrNo, doctor, opType));
    return map;
  }

  /**
   * Patient HIE consent.
   *
   * @param request
   *          the request
   * @param response
   *          the response
 * @throws ParseException 
   *
   */
  @RequestMapping(value = "/patientconsent", method = RequestMethod.GET)
  public void patientConsent(HttpServletRequest request,
      HttpServletResponse response) throws ParseException {
    Map<String,String[]> parameterMap = request.getParameterMap();
    registrationService.sendPatientConsent(parameterMap);
  }

  /**
   * Gets the prepopulate visit info.
   *
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the prepopulate visit info
   */
  @RequestMapping(value = "/getprepopulatevisitinfo", method = RequestMethod.GET)
  public Map<String, String> getPrepopulateVisitInfo(HttpServletRequest request,
      HttpServletResponse response) {
    String mrNo = request.getParameter("mr_no");
    Map<String, String> map = new HashMap<String, String>();
    map.put("prepopulate_visit_info", (String) registrationService.getPrePopulateVisitInfo(mrNo));
    return map;
  }




  /**
   * Gets the sponsor details.
   *
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the sponsor details
   */
  @IgnoreConfidentialFilters
  @RequestMapping(value = "/sponsorold", method = RequestMethod.GET)
  public Map<String, Object> getSponsorDetails(HttpServletRequest request,
      HttpServletResponse response) {
    String tpaId = request.getParameter("tpa_id");
    String categoryId = request.getParameter("category_id");
    Map<String, Object> map = new HashMap<String, Object>();
    map.put("insurance_sponsor_details", registrationService.getSponsorDetails(tpaId, categoryId));
    return map;
  }

  /**
   * Gets the sponsor detailsnew.
   *
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the sponsor detailsnew
   */
  @IgnoreConfidentialFilters
  @RequestMapping(value = "/sponsor", method = RequestMethod.GET)
  public Map<String, Object> getSponsorDetailsnew(HttpServletRequest request,
      HttpServletResponse response) {
    logger.debug("starting sponsor details" + DateUtil.getCurrentTimestamp());
    String tpaId = request.getParameter("tpa_id");
    String categoryId = request.getParameter("category_id");
    Map<String, Object> map = new HashMap<String, Object>();
    map.put("insurance_sponsor_details", registrationService.getSponsordet(tpaId, categoryId));
    logger.debug("ending sponsor details" + DateUtil.getCurrentTimestamp());
    return map;
  }

  /**
   * Gets the plan details.
   *
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the plan details
   */
  @IgnoreConfidentialFilters
  @RequestMapping(value = "/plandetails", method = RequestMethod.GET)
  public Map<String, Object> getPlanDetails(
      HttpServletRequest request, HttpServletResponse response) {
    String planId = request.getParameter("plan_id");
    String visitType = request.getParameter("visit_type");
    Map<String, Object> map = new HashMap<String, Object>();
    registrationService.getPlanDetails(planId, visitType, map);
    return map;
  }

  /**
   * Gets the patient visit type.
   *
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the patient visit type
   * @throws ParseException
   *           the parse exception
   */
  @RequestMapping(value = "/getpatientvisittype", method = RequestMethod.GET)
  public Map<String, Object> getPatientVisitType(HttpServletRequest request,
      HttpServletResponse response) throws ParseException {
    String mrNo = request.getParameter("mr_no");
    String doctorId = request.getParameter("doctor_id");
    String sponsorId = request.getParameter("sponsor_id");
    if ((doctorId == null || doctorId.isEmpty()) && (mrNo == null || mrNo.isEmpty())) {
      throw new HMSException(HttpStatus.BAD_REQUEST, "exception.bad.request", null);
    }
    return registrationService.getPatientVisitType(mrNo, doctorId, sponsorId);
  }

  /**
   * Gets the consultation types for rateplan.
   *
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the consultation types for rateplan
   */
  @IgnoreConfidentialFilters
  @RequestMapping(value = "/getconsultationtypes", method = RequestMethod.GET)
  public Map<String, Object> getConsultationTypesForRateplan(HttpServletRequest request,
      HttpServletResponse response) {
    String orgId = request.getParameter("org_id");
    if ((orgId == null || orgId.isEmpty())) {
      throw new HMSException(HttpStatus.BAD_REQUEST, "exception.bad.request", null);
    }
    Map<String, Object> map = new HashMap<String, Object>();
    map.put("consultation_types", ConversionUtils.copyListDynaBeansToMap(registrationService
        .getConsultationTypesForRateplan(orgId)));
    return map;
  }

  /**
   * Gets the insurance document.
   *
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the insurance document
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  @RequestMapping(value = "/getinsurancedoc", method = RequestMethod.GET)
  public ResponseEntity<byte[]> getInsuranceDocument(HttpServletRequest request,
      HttpServletResponse response) throws IOException {
    String visitId = request.getParameter("visit_id");
    int patientPolicyId = Integer.parseInt(request.getParameter("patient_policy_id"));
    String planName = request.getParameter("plan_name");
    byte[] data = registrationService.getInsuranceDocument(visitId, patientPolicyId, planName);

    String contentType = MimeTypeDetector.getMimeUtil().getMimeTypes(data).toString();
    response.addHeader("Content-Type", contentType);

    OutputStream responseStream = null;
    try {
      responseStream = response.getOutputStream();
      StreamUtils.copy(data, responseStream);
    } finally {
      if (null != responseStream) {
        responseStream.close();
      }
    }
    return null;
  }

  /**
   * Gets the rateplans.
   *
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the rateplans
   */
  @IgnoreConfidentialFilters
  @RequestMapping(value = "/getrateplans", method = RequestMethod.GET)
  public Map<String, Object> getRateplans(
      HttpServletRequest request, HttpServletResponse response) {
    String categoryId = request.getParameter("category_id");
    String planId = request.getParameter("plan_id");
    String insCompId = request.getParameter("insurance_co_id");

    Map<String, Object> map = new HashMap<String, Object>();
    map.put("rate_plans", ConversionUtils.copyListDynaBeansToMap(registrationService
        .getRatePlans(categoryId, planId, insCompId)));
    return map;
  }

  /**
   * Gets the bill nos.
   *
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the bill nos
   */
  @RequestMapping(value = "/getbillnos", method = RequestMethod.GET)
  public Map<String, Object> getBillNos(HttpServletRequest request, HttpServletResponse response) {
    String visitId = request.getParameter("visit_id");
    Map<String, Object> map = new HashMap<String, Object>();
    map.put("bill_nos", ConversionUtils.listBeanToListMap(registrationService.getBillNos(visitId)));
    return map;
  }

  /**
   * Gets the receipt for patient payments.
   *
   * @param params
   *          the params
   * @return the receipt for patient payments
   */
  @RequestMapping(value = "/getbillorreceiptforpatientpayments", method = RequestMethod.GET)
  public Map<String, Object> getReceiptForPatientPayments(
      @RequestParam MultiValueMap<String, String> params) {
    Map<String, Object> map = new HashMap<String, Object>();
    map.put("bill_or_receipts", registrationService.getBillOrReceiptForPatientPayments(params));
    return map;
  }

  /**
   * Estimate amount.
   *
   * @param request
   *          the request
   * @param response
   *          the response
   * @param requestBody
   *          the request body
   * @return the map
   * @throws SQLException
   *           the SQL exception
   */
  @SuppressWarnings("unchecked")
  @RequestMapping(
      value = "/estimateamount", method = RequestMethod.POST, consumes = "application/json")
  public Map<String, Object> estimateAmount(HttpServletRequest request,
      HttpServletResponse response, @RequestBody ModelMap requestBody) throws SQLException {
    if (requestBody == null) {
      throw new HMSException(HttpStatus.BAD_REQUEST, "exception.bad.request", null);
    }
    Map<String, Object> map = registrationService.estimateAmount(requestBody);
    return map;
  }

  /**
   * Gets the policy details.
   *
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the policy details
   */
  @RequestMapping(value = "/getpolicydetails", method = RequestMethod.GET)
  public Map<String, Object> getPolicyDetails(HttpServletRequest request,
      HttpServletResponse response) {
    String mrNo = request.getParameter("mr_no");
    if (mrNo == null || mrNo.isEmpty()) {
      throw new HMSException(HttpStatus.BAD_REQUEST, "exception.bad.request", null);
    }
    Map<String, Object> map = new HashMap<String, Object>();
    map.put("policy_details",
        ConversionUtils.listBeanToListMap(registrationService.getPolicyDetails(mrNo)));
    return map;
  }

  /**
   * Gets the department unit by rules.
   *
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the department unit by rules
   */
  @IgnoreConfidentialFilters
  @RequestMapping(value = "/getdepartmentunitbyrules", method = RequestMethod.GET)
  public Map<String, Object> getDepartmentUnitByRules(HttpServletRequest request,
      HttpServletResponse response) {
    String deptId = request.getParameter("dept_id");
    if ((deptId == null || deptId.isEmpty())) {
      throw new HMSException(HttpStatus.BAD_REQUEST, "exception.bad.request", null);
    }
    return registrationService.getDepartmentUnitByRules(deptId).getMap();
  }

  /**
   * Gets the order package details.
   *
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the order package details
   */
  @IgnoreConfidentialFilters
  @GetMapping(value = "/getorderpackagedetails")
  public Map<String, Object> getOrderPackageDetails(HttpServletRequest request,
      HttpServletResponse response) {

    String packageId = request.getParameter("pkg_id");
    Map<String, Object> map = new HashMap<String, Object>();
    if (packageId != null && !packageId.equals("")) {
      map.put("package_details",
          ConversionUtils.listBeanToListMap(registrationService.getOrderPackageDetails(packageId)));
    } else {
      throw new HMSException(HttpStatus.BAD_REQUEST, "exception.bad.request", null);
    }
    return map;
  }

  /**
   * Gets the default values for unidentfied patients.
   *
   * @return the default values for unidentfied patients
   */
  @IgnoreConfidentialFilters
  @GetMapping(value = "/unidentifieddefaultvalues")
  public Map<String, Object> getDefaultValuesForUnidentfiedPatients() {
    return registrationService.getDefaultValuesForUnidentifiedPatients();
  }

  /**
   * Check if active ip visit exists.
   *
   * @param request the request
   * @param response the response
   * @return visit details
   */
  @IgnoreConfidentialFilters
  @GetMapping(value = "/checkIfActiveIpVisitExists")
  public Map<String, Object> checkIfActiveIPVisitExits(
      HttpServletRequest request, HttpServletResponse response) {
    String mrNo = request.getParameter("mr_no");
    Map map = new HashMap();
    List<Map> results = registrationService.getPatientAllVisitsInAllCenters(mrNo, true);
    for (Map m : results) {
      if (m.get("visit_type") != null
          && m.get("visit_type").equals("i")
          && m.get("status").equals("A")) {
        map.put("patient_id", m.get("visit_id"));
        break;
      }
    }
    return map;
  }

  /**
   * Marks patient package as discontinued while updating the remark.
   * @param requestBody the request
   */
  @RequestMapping(value = "/discontinuepackage", method = RequestMethod.POST,
      consumes = "application/json")
  public void discontinuePackage(@RequestBody ModelMap requestBody) {
    Integer patientPackageId = (Integer) requestBody.get("patientPackageId");
    String discontinueRemark = (String) requestBody.get("discontinueRemark");
    this.registrationService.discontinuePackage(patientPackageId, discontinueRemark);
  }

}
