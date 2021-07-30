package com.insta.hms.eauthorization;

import com.bob.hms.common.APIUtility;
import com.bob.hms.common.CenterHelper;
import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.Registration.PatientDetailsDAO;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.billing.ChargeDTO;
import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.TypeAwareMap;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.core.billing.BillChargeClaimService;
import com.insta.hms.core.billing.BillChargeService;
import com.insta.hms.core.clinical.consultation.prescriptions.PendingPrescriptionsService;
import com.insta.hms.core.clinical.order.master.OrderService;
import com.insta.hms.core.patient.registration.PatientInsurancePlanDetailsService;
import com.insta.hms.instaforms.AbstractInstaForms;
import com.insta.hms.instaforms.ConsultationForms;
import com.insta.hms.instaforms.IPForms;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.HealthAuthorityPreferences.HealthAuthorityPreferencesDAO;
import com.insta.hms.master.InsuranceCompMaster.InsuCompMasterDAO;
import com.insta.hms.master.PhraseSuggestionsMaster.PhraseSuggestionsMasterDAO;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;
import com.insta.hms.master.TpaMaster.TpaMasterDAO;
import com.insta.hms.medicalrecorddepartment.MRDDiagnosisDAO;
import com.insta.hms.medicalrecorddepartment.MRDUpdateScreenBO;
import com.insta.hms.orders.ConsultationTypesDAO;
import com.insta.hms.outpatient.DentalChartHelperDAO;
import com.insta.hms.outpatient.PrescriptionBO;
import com.insta.hms.outpatient.SecondaryComplaintDAO;
import com.insta.hms.outpatient.ToothImageDetails;
import com.lowagie.text.DocumentException;


import flexjson.JSONSerializer;
import freemarker.template.TemplateException;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.http.HttpHeaders;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.upload.FormFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

/**
 * The Class EAuthPrescriptionAction.
 *
 * @author lakshmi
 */
public class EAuthPrescriptionAction extends BaseAction {

  /**
   * The eauth act dao.
   */
  private static EAuthPrescriptionActivitiesDAO eauthActDao;

  /**
   * The eauth presc DAO.
   */
  private static EAuthPrescriptionDAO eauthPrescDAO;

  /**
   * The eauth presc obs DAO.
   */
  private static GenericDAO eauthPrescObsDAO;

  /**
   * The prescription BO.
   */
  private static PrescriptionBO prescriptionBO;

  /**
   * The visitdao.
   */
  private static VisitDetailsDAO visitdao;

  /**
   * The scomplaint dao.
   */
  private static SecondaryComplaintDAO scomplaintDao;

  /**
   * The preauth req DAO.
   */
  private static GenericDAO preauthReqDAO;

  /**
   * The preauth req app DAO.
   */
  private static GenericDAO preauthReqAppDAO;

  /**
   * The pre auth activity upload DAO.
   */
  private static GenericDAO preAuthActivityUploadDAO;

  /** The Constant INVESTIGATION. */
  public static final String INVESTIGATION = "DIA";

  /** The Constant SERVICE. */
  public static final String SERVICE = "SER";

  /** The Constant DOCTOR. */
  public static final String DOCTOR = "DOC";

  /** Prescription item type constants list. */
  protected static final List<String> PENDING_PRESCRIPTION_TYPES =
      Collections.unmodifiableList(Arrays.asList(INVESTIGATION, SERVICE, DOCTOR));

  /** The pending prescriptions service. */
  private static final PendingPrescriptionsService pendingPrescriptionsService =
      ApplicationContextProvider.getBean(PendingPrescriptionsService.class);
  
  private static final BillChargeClaimService billChargeClaimService = ApplicationContextProvider
      .getBean(BillChargeClaimService.class);

  private static final BillChargeService billChargeService = ApplicationContextProvider
      .getBean(BillChargeService.class);

  private static final OrderService orderService =
      ApplicationContextProvider.getBean(OrderService.class);

  private static PatientInsurancePlanDetailsService patientInsurancePlanDetailsService;


  /**
   * Instantiates a new e auth prescription action.
   */
  public EAuthPrescriptionAction() {
    eauthActDao = new EAuthPrescriptionActivitiesDAO();
    eauthPrescDAO = new EAuthPrescriptionDAO();
    eauthPrescObsDAO = new GenericDAO("preauth_activities_observations");
    prescriptionBO = new PrescriptionBO();
    visitdao = new VisitDetailsDAO();
    scomplaintDao = new SecondaryComplaintDAO();
    preauthReqDAO = new GenericDAO("preauth_prescription_request");
    preauthReqAppDAO = new GenericDAO("preauth_request_approval_details");
    preAuthActivityUploadDAO = new GenericDAO("preauth_prescription_activities_docs");
    patientInsurancePlanDetailsService = new PatientInsurancePlanDetailsService();
  }

  /**
   * Gets the list.
   *
   * @param mapping  the mapping
   * @param fm       the fm
   * @param request  the request
   * @param response the response
   * @return the list
   * @throws SQLException the SQL exception
   * @throws Exception    the exception
   */
  @SuppressWarnings("unchecked")
  @IgnoreConfidentialFilters
  public ActionForward getList(ActionMapping mapping, ActionForm fm,
                               HttpServletRequest request, HttpServletResponse response)
      throws SQLException, Exception {

    Integer userCenterId = RequestContext.getCenterId();
    String errorMsg = CenterHelper.authenticateCenterUser(userCenterId);
    if (errorMsg != null) {
      request.setAttribute("error", errorMsg);
      return mapping.findForward("list");
    }

    Map<Object, Object> map = getParameterMap(request);
    
    String dateRange = request.getParameter("date_range");
    String weekStartDate = null;
    if (dateRange != null && dateRange.equals("week")) {
      SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
      Calendar cal = Calendar.getInstance();
      cal.add(Calendar.DATE, -7);
      Date openDt = cal.getTime();
      weekStartDate = dateFormat.format(openDt);

      map.put("visited_date", new String[] { weekStartDate, "" });
      map.put("visited_date@op", new String[] { "ge,le" });
      map.put("visited_date@cast", new String[] { "y" });
      map.remove("date_range");
    }
    
    
    JSONSerializer js = new JSONSerializer().exclude("class");
    PagedList list = EAuthPrescriptionDAO.searchEAuthPrescriptionList(map,
        ConversionUtils.getListingParameter(map));
    request.setAttribute("pagedList", list);

    request.setAttribute("insCompList",
        js.serialize(ConversionUtils
            .listBeanToListMap(new InsuCompMasterDAO().listAll(null,
                "status", "A", "insurance_co_name"))));

    request.setAttribute("insCategoryList",
        js.serialize(ConversionUtils.listBeanToListMap(
            new GenericDAO("insurance_category_master").listAll(
                null, "status", "A", "category_name"))));

    request.setAttribute("tpaList",
        js.serialize(ConversionUtils
            .listBeanToListMap(new GenericDAO("tpa_master")
                .listAll(null, "status", "A", "tpa_name"))));
    
    ActionForward forward = new ActionForward(mapping.findForward("list").getPath());
    
    if (dateRange != null && dateRange.equals("week") 
        && request.getParameter("visited_date") == null) {
      addParameter("visited_date", weekStartDate, forward);
    }

    return forward;
  }

  /**
   * Gets the sponsor list.
   *
   * @param mapping  the mapping
   * @param fm       the fm
   * @param request  the request
   * @param response the response
   * @return the sponsor list
   * @throws SQLException the SQL exception
   * @throws Exception    the exception
   */
  @SuppressWarnings("unchecked")
  @IgnoreConfidentialFilters
  public ActionForward getSponsorList(ActionMapping mapping, ActionForm fm,
                                      HttpServletRequest request, HttpServletResponse response)
      throws SQLException, Exception {

    String patientId = request.getParameter("patient_id");

    if (patientId != null) {
      List<BasicDynaBean> patientInsurancePlanList = eauthPrescDAO
          .getVisitPlanSponsorsDetails(patientId);
      if (patientInsurancePlanList == null
          || patientInsurancePlanList.isEmpty()) {
        FlashScope flash = FlashScope.getScope(request);
        flash.put("error",
            "This patient does not have TPA/Sponsor, is not Insured with Plan.");
        ActionRedirect redirect = new ActionRedirect(
            mapping.findForward("sponsorRedirect"));
        redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
        return redirect;
      }
      request.setAttribute("patientInsurancePlanList",
          patientInsurancePlanList);
      request.setAttribute("patient_id", patientId);

    }
    return mapping.findForward("selectSponsor");
  }

  /**
   * Gets the e auth prescription screen.
   *
   * @param mapping  the mapping
   * @param form     the form
   * @param request  the request
   * @param response the response
   * @return the e auth prescription screen
   * @throws SQLException the SQL exception
   * @throws Exception    the exception
   */
  public ActionForward getEAuthPrescriptionScreen(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws SQLException, Exception {

    String patientId = request.getParameter("patient_id");
    String priorityStr = request.getParameter("priority");
    int priority = (priorityStr != null) ? Integer.parseInt(priorityStr)
        : 0;
    String insuranceCoId = request.getParameter("insurance_co_id");

    BasicDynaBean preauthPrescBean = eauthPrescDAO
        .getEAuthPatient(patientId, insuranceCoId, priority);
    if (preauthPrescBean == null) {
      FlashScope flash = FlashScope.getScope(request);
      flash.put("error", "Invalid Patient Id : " + patientId);
      ActionRedirect redirect = new ActionRedirect(
          mapping.findForward("listRedirect"));
      redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
      return redirect;
    }

    String error = "This patient";
    String tpaId = (preauthPrescBean != null
        && preauthPrescBean.get("tpa_id") != null)
        ? (String) preauthPrescBean.get("tpa_id")
        : null;

    if (tpaId == null || tpaId.equals("")) {
      error += " does not have TPA/Sponsor,";
    }
    int planId = (preauthPrescBean != null
        && preauthPrescBean.get("plan_id") != null)
        ? (Integer) preauthPrescBean.get("plan_id")
        : 0;
    if (planId == 0) {
      error += " is not Insured with Plan,";
    }

    if (error.equals("This patient")) {
      request.setAttribute("error",
          (error.equals("This patient") ? "" : error));

      request.setAttribute("preauthPrescBean", preauthPrescBean);
      setAttributes(request, patientId, preauthPrescBean);
      request.setAttribute("approval_bean", null);

      return mapping.findForward("show");
    } else {
      request.setAttribute("error", error);
      return mapping.findForward("selectSponsor");
    }
  }

  /**
   * Sets the attributes.
   *
   * @param request          the request
   * @param patientId        the patient id
   * @param preauthPrescBean the preauth presc bean
   * @throws SQLException   the SQL exception
   * @throws IOException    Signals that an I/O exception has occurred.
   * @throws ParseException the parse exception
   */
  private void setAttributes(HttpServletRequest request, String patientId,
                             BasicDynaBean preauthPrescBean)
      throws SQLException, IOException, ParseException {

    boolean isInsuranceCardAvailable = PatientDetailsDAO
        .getCurrentPatientCardImage(patientId, null) != null;
    request.setAttribute("isInsuranceCardAvailable",
        isInsuranceCardAvailable);

    java.util.Map patient = com.insta.hms.Registration.VisitDetailsDAO
        .getPatientVisitDetailsMap(patientId);
    request.setAttribute("patient", patient);

    if (patient.get("visit_type").equals("o")) {
      Integer consId = (Integer) preauthPrescBean.get("consultation_id");
      Map params = new HashMap();
      params.put("consultation_id",
          consId != null ? new String[] {consId + ""} : null);
      AbstractInstaForms formDAO = new ConsultationForms();
      BasicDynaBean opform = formDAO.getComponents(params);
      request.setAttribute("form", opform);
    } else {
      Map params = new HashMap();
      params.put("patient_id", new String[] {patientId});
      AbstractInstaForms formDAO = new IPForms();
      BasicDynaBean ipform = formDAO.getComponents(params);
      request.setAttribute("form", ipform);
    }

    request.setAttribute("diagnosis_details",
        MRDDiagnosisDAO.getAllDiagnosisDetails(patientId));
    request.setAttribute("secondary_complaints",
        scomplaintDao.getSecondaryComplaints(patientId));

    Integer centerId = RequestContext.getCenterId();
    Integer preauthCenterId = centerId;
    String serviceRegNo = null;

    if (null != preauthPrescBean) {
      if (null != preauthPrescBean.get("center_id")) { // Visit Center
        preauthCenterId = (Integer) preauthPrescBean.get("center_id");
      }

      if (null != preauthPrescBean.get("preauth_center_id")) { // Prior
        // Auth
        // Request
        // Center
        preauthCenterId = (Integer) preauthPrescBean
            .get("preauth_center_id");
      }
    }

    if (preauthCenterId != null) {
      BasicDynaBean centerbean = new CenterMasterDAO()
          .findByKey("center_id", preauthCenterId);
      serviceRegNo = centerbean
          .get("hospital_center_service_reg_no") != null
          ? (String) centerbean
          .get("hospital_center_service_reg_no")
          : "";
    }
    request.setAttribute("service_reg_no", serviceRegNo);

    String healthAuthority = CenterMasterDAO
        .getHealthAuthorityForCenter(preauthCenterId);
    request.setAttribute("defaultDiagnosisCodeType",
        HealthAuthorityPreferencesDAO
            .getHealthAuthorityPreferences(healthAuthority)
            .getDiagnosis_code_type());

    List<BasicDynaBean> diagnosisList = eauthPrescDAO
        .findAllDiagnosis(patientId);
    request.setAttribute("diagnosisList", diagnosisList);

    String visitType = (String) patient.get("visit_type");
    String orgId = (String) patient.get("org_id");
    List docCharges = null;
    if (visitType.equals("o")) {
      // OP consultation types
      docCharges = ConsultationTypesDAO.getConsultationTypes("o", orgId,
          healthAuthority);
    } else if (visitType.equals("i")) {
      // IP consultation types
      docCharges = ConsultationTypesDAO.getConsultationTypes("i", orgId,
          healthAuthority);
    }
    request.setAttribute("docCharges", docCharges);

    String tpaId = (preauthPrescBean != null
        && preauthPrescBean.get("tpa_id") != null)
        ? (String) preauthPrescBean.get("tpa_id")
        : null;
    boolean modEclaimPreauth = (Boolean) request.getSession(false)
        .getAttribute("mod_eclaim_preauth");
    String tpaRequiresPreAuth = "N";
    String tpaEAuthMode = "M";

    /*
     * E-PreAuth is required only when mod_eclaim_preauth module is enabled
     * and TPA has Prior Auth mode Online, for OP/IP/OSP patients
     */
    if (modEclaimPreauth) {
      BasicDynaBean tpaBean = new TpaMasterDAO().findByKey("tpa_id",
          tpaId);
      if (tpaBean != null) {
        if (((String) tpaBean.get("pre_auth_mode")).equals("M")
            || ((String) tpaBean.get("pre_auth_mode"))
            .equals("O")) {
          tpaRequiresPreAuth = "Y";
        }
        if (((String) tpaBean.get("pre_auth_mode")).equals("O")) {
          tpaEAuthMode = "O";
        }
      }
    }
    request.setAttribute("TPArequiresPreAuth", tpaRequiresPreAuth);
    request.setAttribute("TPAEAuthMode", tpaEAuthMode);

    Map keys = new HashMap();
    keys.put("code_category",
        new String[] {"Treatment", "Consultations"});
    PagedList codeCategories = new GenericDAO("mrd_supported_codes")
        .search(keys);
    request.setAttribute("codeCategories", codeCategories.getDtoList());

    BasicDynaBean genericPrefs = GenericPreferencesDAO.getAllPrefs();
    request.setAttribute("genericPrefs", genericPrefs.getMap());

    String toothNumberingSystem = (String) genericPrefs
        .get("tooth_numbering_system");
    ToothImageDetails adultToothImageDetails = DentalChartHelperDAO
        .getToothImageDetails(true);
    ToothImageDetails pediacToothImageDetails = DentalChartHelperDAO
        .getToothImageDetails(false);

    request.setAttribute("adult_tooth_numbers", DentalChartHelperDAO
        .getToothNumbers(toothNumberingSystem, adultToothImageDetails));

    JSONSerializer js = new JSONSerializer().exclude("class");
    request.setAttribute("pediac_tooth_numbers",
        DentalChartHelperDAO.getToothNumbers(toothNumberingSystem,
            pediacToothImageDetails));
    request.setAttribute("mrdSupportedCodeTypes",
        js.deepSerialize(ConversionUtils.copyListDynaBeansToMap(
            new GenericDAO("mrd_supported_code_types").listAll())));
    request.setAttribute("observationCodeTypeList", js.deepSerialize(
        new MRDUpdateScreenBO().getObservationListCodes()));

    java.util.List<
        BasicDynaBean> uniquePhraseSuggestions = PhraseSuggestionsMasterDAO
        .getUniquePhraseSuggestionsDynaList();
    request.setAttribute("phrase_suggestions_by_dept_json",
        js.deepSerialize(ConversionUtils.listBeanToMapListMap(
            uniquePhraseSuggestions,
            "phrase_suggestions_category_id")));
  }

  /**
   * Gets the e auth prescription.
   *
   * @param mapping  the mapping
   * @param form     the form
   * @param request  the request
   * @param response the response
   * @return the e auth prescription
   * @throws SQLException the SQL exception
   * @throws Exception    the exception
   */
  public ActionForward getEAuthPrescription(ActionMapping mapping,
                                            ActionForm form, HttpServletRequest request,
                                            HttpServletResponse response) throws SQLException,
      Exception {
    String preauthPresc = request.getParameter("preauth_presc_id");
    int preauthPrescId = (preauthPresc != null)
        ? Integer.parseInt(preauthPresc)
        : 0;
    String insuranceCoId = request.getParameter("insurance_co_id");
    BasicDynaBean preauthPrescBean = eauthPrescDAO
        .getEAuthPresc(preauthPrescId, insuranceCoId);
    if (preauthPrescBean == null) {
      FlashScope flash = FlashScope.getScope(request);
      flash.put("error", "Prior Auth prescription with id : "
          + preauthPrescId + " does not " + "exists.");
      ActionRedirect redirect = new ActionRedirect(
          mapping.findForward("listRedirect"));
      redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
      return redirect;
    }

    Integer centerId = RequestContext.getCenterId();
    Integer preauthCenterId = centerId;
    String patientId = null;

    if (null != preauthPrescBean) {
      if (null != preauthPrescBean.get("center_id")) { // Visit Center
        preauthCenterId = (Integer) preauthPrescBean.get("center_id");
      }

      if (null != preauthPrescBean.get("preauth_center_id")) { // Prior
        // Auth
        // Request
        // Center
        preauthCenterId = (Integer) preauthPrescBean
            .get("preauth_center_id");
      }

      if (null != preauthPrescBean.get("patient_id")) { // Prior Auth
        // Request
        // Center
        patientId = (String) preauthPrescBean.get("patient_id");
      }
    }

    String healthAuthority = CenterMasterDAO
        .getHealthAuthorityForCenter(preauthCenterId);
    String eclaimXMLSchema = HealthAuthorityPreferencesDAO
        .getHealthAuthorityPreferences(healthAuthority)
        .getHealth_authority();

    List<BasicDynaBean> prescActivities = eauthPrescDAO
        .getEAuthPrescriptionActivities(preauthPrescId);

    List<Map> prescActivitiesList = new ArrayList<Map>();
    Map<String, Object> prescActivitiesMap = null;

    // Update complaint observation
    Connection con = null;
    boolean success = true;
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);

      for (BasicDynaBean activity : prescActivities) {
        int preauthActId = (Integer) activity.get("preauth_act_id");

        deleteComplObs(con, preauthActId, patientId);
        String actCode = activity.get("act_code") != null ? (String) activity.get("act_code") :
            null;
        success = eauthActDao.addPresentingComplaint(con, patientId, preauthActId, actCode);
        if (!success) {
          break;
        }
      }
    } finally {
      DataBaseUtil.commitClose(con, success);
    }

    for (BasicDynaBean activity : prescActivities) {
      prescActivitiesMap = new HashMap<String, Object>();

      String preauthActType = (String) activity.get("preauth_act_type");
      if (preauthActType.equals("PDM")) {
        BasicDynaBean perdiemBean = activity;
        request.setAttribute("perdiemCode", perdiemBean.getMap());
        continue;
      }
      if (preauthActType.equals("DRG")) {
        BasicDynaBean drgBean = activity;
        String drgCode = activity.get("act_code") != null
            ? (String) activity.get("act_code")
            : null;
        String visitType = preauthPrescBean.get("visit_type") != null
            ? (String) preauthPrescBean.get("visit_type")
            : null;
        String descQuery = "SELECT drg_description FROM drg_codes_master WHERE drg_code= ?"
            + " AND patient_type = ?";
        String drgDesc = DataBaseUtil.getStringValueFromDb(descQuery,
            new Object[] {drgCode, visitType.toUpperCase()});
        request.setAttribute("drgDescription", drgDesc);
        request.setAttribute("drgCode", drgBean.getMap());
        continue;
      }
      int preauthActId = (Integer) activity.get("preauth_act_id");
      List<BasicDynaBean> observations = eauthPrescDAO.getEAuthActObservations(preauthActId,
          eclaimXMLSchema);
      prescActivitiesMap.put("activity", activity.getMap());
      prescActivitiesMap.put("observations", ConversionUtils.listBeanToListMap(observations));
      prescActivitiesList.add(prescActivitiesMap);
    }

    request.setAttribute("prescActivitiesList", prescActivitiesList);
    request.setAttribute("preauthPrescBean", preauthPrescBean);
    request.setAttribute("approval_bean",
        new EAuthApprovalsDAO().getApprovalBean(preauthPrescId));

    setAttributes(request, (String) preauthPrescBean.get("patient_id"),
        preauthPrescBean);
    JSONSerializer js = new JSONSerializer().exclude("class");
    // for phrase suggestions
    java.util.List<
        BasicDynaBean> phraseSuggestions = PhraseSuggestionsMasterDAO
        .getPhraseSuggestionsDynaList();
    request.setAttribute("phrase_suggestions_json",
        js.deepSerialize(ConversionUtils.listBeanToMapListMap(
            phraseSuggestions, "phrase_suggestions_category_id")));

    java.util.List<
        BasicDynaBean> uniquePhraseSuggestions = PhraseSuggestionsMasterDAO
        .getUniquePhraseSuggestionsDynaList();
    request.setAttribute("phrase_suggestions_by_dept_json",
        js.deepSerialize(ConversionUtils.listBeanToMapListMap(
            uniquePhraseSuggestions,
            "phrase_suggestions_category_id")));
    BasicDynaBean printPref = PrintConfigurationsDAO
        .getPrintMode(PrintConfigurationsDAO.PRINT_TYPE_BILL);
    request.setAttribute("pref", printPref);
    return mapping.findForward("show");
  }

  /**
   * Ajax item rate code charge.
   *
   * @param mapping  the mapping
   * @param form     the form
   * @param request  the request
   * @param response the response
   * @return the action forward
   * @throws SQLException the SQL exception
   * @throws Exception    the exception
   */
  public ActionForward ajaxItemRateCodeCharge(ActionMapping mapping,
                                              ActionForm form, HttpServletRequest request,
                                              HttpServletResponse response) throws SQLException,
      Exception {

    String visitId = request.getParameter("patient_id");
    String itemType = request.getParameter("itemType");
    String itemId = request.getParameter("itemId");
    String chargeType = request.getParameter("chargeType");
    String qty = request.getParameter("act_qty");
    BigDecimal itemQty = new BigDecimal(qty);

    ChargeDTO itemCharge = eauthPrescDAO.getItemRateCodeCharge(itemType,
        itemId, itemQty, visitId, chargeType);

    JSONSerializer js = new JSONSerializer().exclude("class");
    response.setContentType("text/javascript");
    response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");

    response.getWriter().write(js.serialize(itemCharge));
    return null;
  }

  private ChargeDTO normalizeRateCodeChangeResponse(Map<String, Object> itemChargeEstimate)
      throws SQLException {
    TypeAwareMap<String, Object> itemChargeTypeAwareMap = new TypeAwareMap<>(
        (Map<String, Object>) ((List<Object>) itemChargeEstimate.get("overall_adjustment"))
            .get(0));
    String chargeGroup = itemChargeTypeAwareMap.get("charge_group");
    String chargeHead = itemChargeTypeAwareMap.get("charge_head");
    BigDecimal rate = new BigDecimal(itemChargeTypeAwareMap.<String>get("amount"));
    BigDecimal qty = new BigDecimal(itemChargeTypeAwareMap.<Integer>get("quantity"));
    BigDecimal discount = new BigDecimal(itemChargeTypeAwareMap.<String>get("discount"));
    String units = itemChargeTypeAwareMap.getOrDefault("act_unit", "");
    String descId = itemChargeTypeAwareMap.get("act_description_id");
    String desc = itemChargeTypeAwareMap.get("act_description");
    String deptId = itemChargeTypeAwareMap.get("act_department_id");
    Boolean isInsurance = itemChargeTypeAwareMap.getOrDefault("insurance", true);
    List<Integer> planIds = itemChargeTypeAwareMap.get("plan_ids");
    Integer planId = -1;
    if (CollectionUtils.isNotEmpty(planIds)) {
      planId = planIds.get(0);
    }
    Integer serviceSubGroupId = itemChargeTypeAwareMap.get("service_sub_group_id");
    Integer insuranceCategoryId =
        Integer.parseInt(itemChargeTypeAwareMap.<String>get("insurance_category_id"));
    String visitType = itemChargeTypeAwareMap.get("visit_type");
    String visitId = itemChargeTypeAwareMap.get("patient_id");
    Boolean firstOfCategory = itemChargeTypeAwareMap.getOrDefault("first_of_category", false);
    return new ChargeDTO(chargeGroup, chargeHead, rate, qty, discount, units, descId, desc,
        deptId, isInsurance, planId, serviceSubGroupId, insuranceCategoryId, visitType,
        visitId, firstOfCategory);
  }

  /**
   * Normalize item code change request.
   *
   * @param request the request
   * @return the map
   */
  private Map<String, Object> normalizeItemCodeChangeRequest(HttpServletRequest request) {
    Map<String, Object> normalizedRequestMap = new HashMap<>();
    normalizedRequestMap.put("existing_ordered_items", Collections.emptyList());

    Map<String, Object> orderItemMap = new HashMap<>();
    String itemType = request.getParameter("itemType");
    itemType = "Laboratory";
    String visitType = request.getParameter("visit_type");
    Integer planId = Integer.parseInt(request.getParameter("plan_id"));
    String visitId = request.getParameter("patient_id");
    orderItemMap.put("type", itemType);
    orderItemMap.put("entity", Arrays.asList(itemType));
    String itemId = request.getParameter("itemId");
    orderItemMap.put("id", itemId);
    orderItemMap.put("bed_type", "GENERAL");
    orderItemMap.put("insurance", Boolean.TRUE);
    orderItemMap.put("quantity", 1);
    orderItemMap.put("visit_type", visitType);
    orderItemMap.put("multi_visit_package", Boolean.FALSE);
    orderItemMap.put("plan_ids", Arrays.asList(planId));
    orderItemMap.put("patient_id", visitId);
    orderItemMap.put("charge_type", request.getParameter("chargeType"));
    String currentDateTime = DateUtil.currentDate("dd-MM-yyyy HH:mm");
    String[] dateTimeArray = currentDateTime.split(" ");
    String prescribedDate = dateTimeArray[0];
    String prescribedTime = dateTimeArray[1];
    orderItemMap.put("prescribed_date_date", prescribedDate);
    orderItemMap.put("prescribed_date_time", prescribedTime);
    orderItemMap.put("serverSeconds", System.currentTimeMillis());
    String tpaId = request.getParameter("tpa_id");
    orderItemMap.put("tpa_id", tpaId);
    normalizedRequestMap.put("newly_added_ordered_item", orderItemMap);

    String insuranceCompanyId = request.getParameter("insurance_co_id");
    Map<String, Object> insuranceDetails = new HashMap<>();
    insuranceDetails.put("tpa_id", tpaId);
    insuranceDetails.put("plan_id", planId);
    insuranceDetails.put("sponsor_id", tpaId);
    insuranceDetails.put("insurance_co_id", insuranceCompanyId);
    List<Map<String, Object>> insuranceDetailsList = new ArrayList<>();
    insuranceDetailsList.add(insuranceDetails);
    normalizedRequestMap.put("insurance_details", insuranceDetailsList);

    Map<String, Object> visitMap = new HashMap<>();
    visitMap.put("visit_id", visitId);
    normalizedRequestMap.put("visit", visitMap);

    return normalizedRequestMap;
  }

  /**
   * Save E auth details.
   *
   * @param mapping  the mapping
   * @param form     the form
   * @param request  the request
   * @param response the response
   * @return the action forward
   * @throws SQLException the SQL exception
   * @throws Exception    the exception
   */
  public ActionForward saveEAuthDetails(ActionMapping mapping,
                                        ActionForm form, HttpServletRequest request,
                                        HttpServletResponse response) throws SQLException,
      Exception {

    HttpSession session = request.getSession(false);
    String userid = (String) session.getAttribute("userid");
    Connection con = null;

    EAuthPrescriptionUploadForm eauthuplaodForm = (EAuthPrescriptionUploadForm) form;

    //HMS-38306 Attachments were not getting uploaded
    Hashtable formFileHashtable = eauthuplaodForm.getMultipartRequestHandler().getFileElements();
    TreeMap formFileHashMap = new TreeMap(formFileHashtable);
    List itemAttachedFilesList = new ArrayList();
    formFileHashMap.forEach((key, value) -> {
      itemAttachedFilesList.add(value);
    });

    String preauthPrescStr = request.getParameter("preauth_presc_id");
    String consIdStr = request.getParameter("consultation_id");
    String patientId = request.getParameter("patient_id");
    String insuranceCoId = request.getParameter("insurance_co_id");
    String mrNumber = request.getParameter("mr_no");
    String comments = request.getParameter("_comments");
    String resubmissionType = request.getParameter("_resubmit_type");

    FlashScope flash = FlashScope.getScope(request);
    ActionRedirect redirect = new ActionRedirect(
        "//EAuthorization/EAuthPresc.do");

    int consId = (consIdStr != null && !consIdStr.equals(""))
        ? Integer.parseInt(consIdStr)
        : 0;
    int preauthPrescId = (preauthPrescStr != null
        && !preauthPrescStr.equals(""))
        ? Integer.parseInt(preauthPrescStr)
        : 0;

    Map<String, String[]> params = request.getParameterMap();
    String error = null;
    boolean allSuccess = false;

    try {
      txn:
      {
        con = DataBaseUtil.getConnection();
        con.setAutoCommit(false);

        // update encounter type details.
        BasicDynaBean visitDetailsBean = visitdao
            .findByKey("patient_id", patientId);
        String encType = request.getParameter("encCode");
        int encounterType = visitDetailsBean
            .get("encounter_type") != null
            ? (Integer) visitDetailsBean
            .get("encounter_type")
            : 0;
        encounterType = (encType != null && !encType.trim().equals(""))
            ? Integer.parseInt(encType)
            : encounterType;
        visitDetailsBean.set("encounter_type", encounterType);

        int res = visitdao.updateWithName(con,
            visitDetailsBean.getMap(), "patient_id");
        if (res <= 0) {
          break txn;
        }

        // update complaint details.
        String complaint = request.getParameter("complaint");
        if (!visitdao.updateComplaint(con, complaint, patientId)) {
          break txn;
        }
        String[] rowIds = request
            .getParameterValues("s_complaint_row_id");
        String[] complaintNames = request
            .getParameterValues("s_complaint");
        Map<String, Object> resultMap = scomplaintDao.insert(con,
            rowIds, complaintNames, patientId, userid);
        if (!(Boolean) resultMap.get("isSuccess")) {
          error = (String) resultMap.get("msg");
          break txn;
        }

        // update the diagnosis details.
        String[] diagnosisIds = request
            .getParameterValues("diagnosis_id");
        String[] diagnosisCode = request
            .getParameterValues("diagnosis_code");
        String[] diagnosisDescription = request
            .getParameterValues("diagnosis_description");
        String[] diagnosisYearOfOnset = (String[]) params
            .get("diagnosis_year_of_onset");
        String[] diagnosisStatusId = request
            .getParameterValues("diagnosis_status_id");
        String[] diagnosisRemarks = request
            .getParameterValues("diagnosis_remarks");
        String[] diagType = request
            .getParameterValues("diagnosis_type");
        String[] diagDelete = request
            .getParameterValues("diagnosis_deleted");
        String[] diagEdited = request
            .getParameterValues("diagnosis_edited");
        String[] diagDoctor = request
            .getParameterValues("diagnosis_doctor_id");
        String[] diagDatetime = request
            .getParameterValues("diagnosis_datetime");
        if (diagnosisIds != null) {
          BasicDynaBean regBean = new GenericDAO(
              "patient_registration").findByKey(con, "patient_id",
              patientId);
          String healthAuthority = CenterMasterDAO
              .getHealthAuthorityForCenter(
                  (Integer) regBean.get("center_id"));
          String codeType = HealthAuthorityPreferencesDAO
              .getHealthAuthorityPreferences(healthAuthority)
              .getDiagnosis_code_type();
          for (int i = 0; i < diagnosisIds.length - 1; i++) {
            if (!prescriptionBO.updateDiangoisDetails(con,
                diagnosisIds[i], diagnosisCode[i],
                diagnosisDescription[i],
                diagnosisYearOfOnset[i], diagnosisStatusId[i],
                diagnosisRemarks[i], diagDoctor[i],
                diagDatetime[i], new Boolean(diagDelete[i]),
                new Boolean(diagEdited[i]), patientId,
                diagType[i], userid, null, "N", regBean, null,
                codeType)) {
              break txn;
            }
          }
        }

        String[] prescribedIds = request
            .getParameterValues("s_preauth_act_id");
        String[] prescribedDates = request
            .getParameterValues("s_prescribed_date");
        String[] prescribedTime = request
            .getParameterValues("s_prescribedTime");

        String[] itemNames = request.getParameterValues("s_item_name");
        String[] itemIds = request.getParameterValues("s_item_id");
        String[] itemRemarks = request
            .getParameterValues("s_item_remarks");
        String[] itemMaster = request
            .getParameterValues("s_item_master");
        String[] ispackage = request.getParameterValues("s_ispackage");
        String[] addActivities = request
            .getParameterValues("s_addActivity");

        String[] rate = request.getParameterValues("s_item_rate");
        String[] qty = request.getParameterValues("s_item_qty");
        String[] remQty = request.getParameterValues("s_item_rem_qty");
        String[] discount = request.getParameterValues("s_item_disc");
        String[] amount = request.getParameterValues("s_item_amount");
        String[] claimNetAmount = request
            .getParameterValues("s_claim_net_amount");
        String[] patientAmount = request
            .getParameterValues("s_patient_amount");

        String[] preauthActType = request
            .getParameterValues("s_itemType");
        String[] delItems = request.getParameterValues("s_delItem");
        String[] editItems = request.getParameterValues("s_edited");

        String[] actStatus = request.getParameterValues("s_status");
        String[] preauthActStatus = request
            .getParameterValues("s_preauth_act_status");
        String[] codeType = request
            .getParameterValues("s_item_code_type");
        String[] actCode = request.getParameterValues("s_item_code");
        String[] preauthID = request
            .getParameterValues("s_item_preauth_id");
        String[] preauthMode = request
            .getParameterValues("s_item_preauth_mode");

        String[] itemDocConsType = request
            .getParameterValues("s_doc_cons_type");

        String[] toothNumberUNV = request
            .getParameterValues("tooth_unv_number");
        String[] toothNumberFDI = request
            .getParameterValues("tooth_fdi_number");
        String[] requirePriorAuth = request
            .getParameterValues("s_preauth_required");
        String[] claimApprovedAmounts = request.getParameterValues("s_claim_approved_amount");
        String[] delAttach = request.getParameterValues("s_del_attach");
        String[] approvedQty = request.getParameterValues("s_item_approved_qty");
        String[] remApprvdQty = request.getParameterValues("s_item_approved_rem_qty");

        boolean modPatientPendingPres = (Boolean) request.getSession(false)
            .getAttribute("mod_pat_pending_prescription");
        if (prescribedIds != null) {

          List uploadedDocAr = eauthuplaodForm
              .getActivity_file_upload();
          List<Map<String, Object>> preauthInsertBeansList = new ArrayList<>();
          FormFile file = null;
          for (int i = 0; i < prescribedIds.length - 1; i++) {
            boolean deleteItem = new Boolean(delItems[i]);
            boolean editItem = new Boolean(editItems[i]);
            if (!itemAttachedFilesList.isEmpty()) {
              file = (FormFile) itemAttachedFilesList.get(i);
            }
            BasicDynaBean docUploadBean = preAuthActivityUploadDAO
                .getBean();
            if (file != null && file.getFileSize() > 0) {

              if (!prescribedIds[i].equals("_")) {
                docUploadBean.set("preauth_act_id",
                    Integer.parseInt(prescribedIds[i]));
              }
              docUploadBean.set("attachment",
                  file.getInputStream());

              String fileName = file.getFileName();
              String extension = null;
              if (fileName.contains(".")) {
                extension = fileName
                    .substring(fileName.indexOf(".") + 1);
                docUploadBean.set("attachment_extension",
                    extension);
                docUploadBean.set("file_name", fileName);

                if (extension.equals("odt")
                    || extension.equals("ods")) {
                  docUploadBean.set("attachment_content_type",
                      "application/vnd.oasis.opendocument.text");
                } else {
                  // docUploadBean.set("attachment_content_type",
                  // MimeTypeDetector.getMimeTypes(file.getInputStream()));
                  docUploadBean.set("attachment_content_type",
                      file.getContentType());
                }
              } else {
                // docUploadBean.set("attachment_content_type",
                // MimeTypeDetector.getMimeTypes(file.getInputStream()));
                docUploadBean.set("attachment_content_type",
                    file.getContentType());
              }
            }

            BasicDynaBean itemBean = eauthActDao.getBean();

            itemBean.set("act_qty", new Integer(qty[i]));
            itemBean.set("rem_qty", new Integer(remQty[i]));
            itemBean.set("rate", new BigDecimal(rate[i]));
            itemBean.set("discount", new BigDecimal(discount[i]));
            itemBean.set("amount", new BigDecimal(amount[i]));
            itemBean.set("claim_net_amount", new BigDecimal(claimNetAmount[i]));
            itemBean.set("claim_net_approved_amount", new BigDecimal(claimApprovedAmounts[i]));
            itemBean.set("patient_share", new BigDecimal(patientAmount[i]));
            itemBean.set("approved_qty", new Integer(approvedQty[i]));
            itemBean.set("rem_approved_qty", new Integer(remApprvdQty[i]));

            // itemBean.set("prescribed_date",
            // DateUtil.parseDate(prescribedDates[i]));
            itemBean.set("prescribed_date",
                new DateUtil().parseTimestamp(prescribedDates[i]
                    + " " + prescribedTime[i]));
            itemBean.set("ispackage", new Boolean(ispackage[i]));
            itemBean.set("preauth_required",
                requirePriorAuth[i] != null
                    ? requirePriorAuth[i]
                    : "N");

            itemBean.set("doc_cons_type",
                (itemDocConsType[i] != null
                    && !itemDocConsType[i].equals(""))
                    ? Integer.parseInt(
                    itemDocConsType[i])
                    : 0);

            itemBean.set("tooth_unv_number", toothNumberUNV[i]);
            itemBean.set("tooth_fdi_number", toothNumberFDI[i]);

            itemBean.set("mod_time", new java.sql.Timestamp(
                new java.util.Date().getTime()));
            itemBean.set("username", userid);

            itemBean.set("consultation_id", consId);
            itemBean.set("visit_id", patientId);
            itemBean.set("preauth_act_item_remarks",
                itemRemarks[i]);
            itemBean.set("preauth_act_item_id", itemIds[i]);
            itemBean.set("preauth_act_type", preauthActType[i]);
            itemBean.set("mr_no", mrNumber);
            itemBean.set("status", actStatus[i]);
            itemBean.set("preauth_act_status", preauthActStatus[i]);
            itemBean.set("preauth_id", preauthID[i]);
            itemBean.set("preauth_mode",
                (preauthMode[i] != null
                    && !preauthMode[i].equals("")) ? Integer
                    .parseInt(preauthMode[i]) : 0);

            itemBean.set("act_code_type", codeType[i]);
            itemBean.set("act_code", actCode[i]);

            Map<String, Object> pendingPrescMap = new HashMap<>();
            pendingPrescMap.put("preauth_activity_id", 0);
            pendingPrescMap.put("mr_no", mrNumber);
            pendingPrescMap.put("consultation_id", consId);
            pendingPrescMap.put("prescribed_date", itemBean.get("prescribed_date"));
            pendingPrescMap.put("patient_prescription_type", preauthActType[i]);
            pendingPrescMap.put("preauth_act_item_id", itemIds[i]);
            pendingPrescMap.put("quantity", itemBean.get("act_qty"));
            pendingPrescMap.put("visit_id", patientId);
            pendingPrescMap.put("preauth_act_item_remarks",
                itemRemarks[i]);
            pendingPrescMap.put("preauth_act_status", itemBean.get("preauth_act_status"));
            pendingPrescMap.put("approved_qty", itemBean.get("approved_qty"));
            pendingPrescMap.put("rem_approved_qty", itemBean.get("rem_approved_qty"));
            Integer itemPrescriptionId = 0;
            String prescribedId = prescribedIds[i];
            if (prescribedId.equals("_")) {
              pendingPrescMap.put("action", "insert");
              preauthPrescId = eauthPrescDAO
                  .getEAuthPrescSequenceId(con, userid,
                      preauthPrescId, consId, patientId,
                      insuranceCoId);
              itemBean.set("preauth_presc_id", preauthPrescId);

              itemPrescriptionId = eauthActDao.getNextSequence();
              itemBean.set("preauth_act_id", itemPrescriptionId);
              pendingPrescMap.put("preauth_activity_id", itemPrescriptionId);

              docUploadBean.set("preauth_act_id", itemPrescriptionId);


              if (!eauthActDao.insert(con, itemBean)) {
                break txn;
              }
              if (modPatientPendingPres && PENDING_PRESCRIPTION_TYPES.contains(preauthActType[i])) {
                preauthInsertBeansList.add(pendingPrescMap);
              }
              if (file != null && file.getFileSize() > 0
                  && null != delAttach[i]
                  && !delAttach[i].equals("true")) {
                if (!preAuthActivityUploadDAO.insert(con,
                    docUploadBean)) {
                  break txn;
                }
              }
            } else {
              itemPrescriptionId = Integer
                  .parseInt(prescribedIds[i]);
              pendingPrescMap.put("preauth_activity_id", itemPrescriptionId);
              if (deleteItem) {
                pendingPrescMap.put("action", "delete");
                if (modPatientPendingPres
                    && PENDING_PRESCRIPTION_TYPES.contains(preauthActType[i])) {
                  preauthInsertBeansList.add(pendingPrescMap);
                }
                if (!eauthActDao.delete(con, "preauth_act_id",
                    itemPrescriptionId)) {
                  break txn;
                }
                //set preauth_act_id to null in bill charge table
                BasicDynaBean chargeBean = billChargeService.getBean();
                chargeBean.set("preauth_act_id", null);
                Map<String, Object> keys = new HashMap<>();
                keys.put("preauth_act_id",itemPrescriptionId);
                billChargeService.update(chargeBean, keys);

                /*
                 * BasicDynaBean preAuthBean =
                 * eauthActDao.findByKey("preauth_act_id",
                 * itemPrescriptionId);
                 * preAuthBean.set("status", "X");
                 * preAuthBean.set("mod_time", new
                 * java.sql.Timestamp(new
                 * java.util.Date().getTime()));
                 * preAuthBean.set("username", userid); if
                 * (eauthActDao.updateWithName(con,
                 * preAuthBean.getMap(), "preauth_act_id") <= 0)
                 * break txn;
                 */
              } else {
                if (editItem
                    && modPatientPendingPres
                    && PENDING_PRESCRIPTION_TYPES.contains(preauthActType[i])) {
                  pendingPrescMap.put("action", "update");
                  preauthInsertBeansList.add(pendingPrescMap);
                }
                itemPrescriptionId = Integer
                    .parseInt(prescribedIds[i]);
                itemBean.set("preauth_act_id",
                    itemPrescriptionId);

                BasicDynaBean preAuthBean = eauthActDao
                    .findByKey("preauth_act_id",
                        itemPrescriptionId);
                if (preAuthBean.get("preauth_presc_id") == null
                    || ((Integer) preAuthBean.get(
                    "preauth_presc_id")) == 0) {
                  preauthPrescId = eauthPrescDAO
                      .getEAuthPrescSequenceId(con,
                          userid, preauthPrescId,
                          consId, patientId,
                          insuranceCoId);
                  itemBean.set("preauth_presc_id",
                      preauthPrescId);

                }

                if (eauthActDao.updateWithName(con,
                    itemBean.getMap(),
                    "preauth_act_id") <= 0) {
                  break txn;
                }
                
                BasicDynaBean genericPrefs = GenericPreferencesDAO.getAllPrefs();
                // If the preference is set to update claim amount of ordered items then update the
                // claim amount.
                boolean shouldUpdateClaim = "Y"
                    .equals(genericPrefs.get("update_claim_of_ordered_item_on_preauth_approval"));
                boolean statusChanged = null != preAuthBean 
                    && !(preAuthBean.get("preauth_act_status")
                    .equals(itemBean.get("preauth_act_status")));
                boolean eligibleForClaimUpdate = null != preAuthBean
                    //eligible only if status changed
                    && statusChanged
                    && "Y".equals(preAuthBean.get("added_to_bill"))
                    && ("C".equals(itemBean.get("preauth_act_status")) 
                        || "D".equals(itemBean.get("preauth_act_status")));
                if (shouldUpdateClaim && eligibleForClaimUpdate) {
                  // Have to send the amount because spring creates another transaction for the
                  // below queries, thus setting the old approval amount or 0 as claim amount
                  // when calculated in query.
                  Object approvedAmount = itemBean.get("claim_net_approved_amount");
                  String preAuthId = (String) itemBean.get("preauth_id");
                  Integer priorAuthModeId = (Integer) itemBean.get("preauth_mode");
                  billChargeClaimService.setPriorAuthApprovalAmountAsClaimAmount(itemPrescriptionId,
                      approvedAmount, preAuthId, priorAuthModeId);
                  billChargeService.setPriorAuthApprovalAmountAsClaimAmount(itemPrescriptionId,
                      approvedAmount);
                  billChargeService.setPriorAuthDetailsForCharges(itemPrescriptionId, preAuthId,
                      priorAuthModeId);
                }
                
                Boolean preAuthActivityDocExists = preAuthActivityUploadDAO
                    .exist("preauth_act_id",
                        Integer.parseInt(prescribedId));

                if (file != null && file.getFileSize() > 0) {
                  if (preAuthActivityDocExists) {
                    if (null != delAttach[i] && delAttach[i]
                        .equals("true")) {
                      if (!preAuthActivityUploadDAO
                          .delete(con,
                              "preauth_act_id",
                              Integer.parseInt(
                                  prescribedId))) {
                        break txn;
                      }
                    } else {
                      if (preAuthActivityUploadDAO
                          .updateWithName(con,
                              docUploadBean
                                  .getMap(),
                              "preauth_act_id") <= 0) {
                        break txn;
                      }
                    }
                  } else {
                    if (null != delAttach[i]
                        && !delAttach[i]
                        .equals("true")) {
                      if (!preAuthActivityUploadDAO
                          .insert(con,
                              docUploadBean)) {
                        break txn;
                      }
                    }
                  }
                } else {
                  if (preAuthActivityDocExists
                      && null != delAttach[i]
                      && delAttach[i].equals("true")) {
                    if (!preAuthActivityUploadDAO.delete(
                        con, "preauth_act_id",
                        Integer.parseInt(
                            prescribedId))) {
                      break txn;
                    }
                  }
                }
              }
            }

            // Delete Observations
            String[] observationCode = params
                .get("obserCode." + itemIds[i]);
            String[] observationType = params
                .get("obserType." + itemIds[i]);
            String[] observationValue = params
                .get("obserValue." + itemIds[i]);
            String[] observationValueType = params
                .get("obserValueType." + itemIds[i]);

            eauthPrescObsDAO.delete(con, "preauth_act_id",
                itemPrescriptionId);

            if (!deleteItem) {
              // Add Observations
              if (observationCode != null) {
                for (int j = 0; j < observationCode.length; j++) {
                  BasicDynaBean obserBean = eauthPrescObsDAO
                      .getBean();
                  obserBean.set("preauth_act_id",
                      itemPrescriptionId);
                  obserBean.set("code", observationCode[j]);
                  obserBean.set("obs_type",
                      observationType[j]);
                  obserBean.set("value", observationValue[j]);
                  obserBean.set("value_type",
                      observationValueType[j]);
                  if (!eauthPrescObsDAO.insert(con,
                      obserBean)) {
                    break txn;
                  }
                }
              }

              deleteComplObs(con, itemPrescriptionId, patientId);
              if (!eauthActDao.addPresentingComplaint(con,
                  patientId, itemPrescriptionId,
                  actCode[i])) {
                break txn;
              }
            }
          }
          if (modPatientPendingPres
              && preauthInsertBeansList != null && !preauthInsertBeansList.isEmpty()) {
            pendingPrescriptionsService.insertUpdatePreauthPrescriptions(preauthInsertBeansList,
                consId);
          }
        }

        String perdiemCode = request.getParameter("perdiemCode");
        perdiemCode = (perdiemCode == null)
            ? request.getParameter("perdiem_code")
            : perdiemCode;
        String perdiemCodeType = request
            .getParameter("perdiem_code_type");
        String perdiemActId = request.getParameter("perdiem_act_id");
        String perdiemNet = request.getParameter("perdiem_net");
        String perdiemPreauthId = request
            .getParameter("perdiem_preauth_id");
        String perdiemPreauthMode = request
            .getParameter("perdiem_preauth_mode");

        BasicDynaBean preauthBean = eauthActDao.getBean();

        // Perdiem
        if (perdiemActId == null || perdiemActId.equals("")) {
          if (perdiemCode != null && !perdiemCode.trim().equals("")) {

            BigDecimal net = (perdiemNet != null
                && !perdiemNet.equals(""))
                ? new BigDecimal(perdiemNet)
                : BigDecimal.ZERO;

            preauthBean.set("act_qty", new Integer(1));
            preauthBean.set("rate", net);
            preauthBean.set("discount", new BigDecimal(0));
            preauthBean.set("amount", net);
            preauthBean.set("claim_net_amount", net);

            // preauthBean.set("prescribed_date",
            // DateUtil.getCurrentDate());
            preauthBean.set("prescribed_date",
                DateUtil.getCurrentTimestamp());
            preauthBean.set("ispackage", false);
            preauthBean.set("preauth_required", "Y");

            preauthBean.set("mod_time", new java.sql.Timestamp(
                new java.util.Date().getTime()));
            preauthBean.set("username", userid);

            preauthBean.set("consultation_id", consId);
            preauthBean.set("visit_id", patientId);
            preauthBean.set("preauth_act_type", "PDM");

            preauthBean.set("status", "A");
            preauthBean.set("preauth_act_status", "O");
            preauthBean.set("preauth_id", perdiemPreauthId);
            preauthBean.set("preauth_mode",
                (perdiemPreauthMode != null
                    && !perdiemPreauthMode.equals(""))
                    ? Integer.parseInt(
                    perdiemPreauthMode)
                    : 0);

            preauthBean.set("act_code_type", perdiemCodeType);
            preauthBean.set("act_code", perdiemCode);

            preauthPrescId = eauthPrescDAO.getEAuthPrescSequenceId(
                con, userid, preauthPrescId, consId, patientId,
                insuranceCoId);
            preauthBean.set("preauth_presc_id", preauthPrescId);

            int itemPrescriptionId = eauthActDao.getNextSequence();
            preauthBean.set("preauth_act_id", itemPrescriptionId);

            if (!eauthActDao.insert(con, preauthBean)) {
              break txn;
            }
          }
        } else {
          int itemPrescriptionId = Integer.parseInt(perdiemActId);
          preauthBean.set("preauth_act_id", itemPrescriptionId);

          BigDecimal net = (perdiemNet != null
              && !perdiemNet.equals(""))
              ? new BigDecimal(perdiemNet)
              : BigDecimal.ZERO;

          preauthBean.set("rate", net);
          preauthBean.set("discount", new BigDecimal(0));
          preauthBean.set("amount", net);
          preauthBean.set("claim_net_amount", net);

          preauthBean.set("mod_time", new java.sql.Timestamp(
              new java.util.Date().getTime()));
          preauthBean.set("username", userid);

          preauthBean.set("preauth_id", perdiemPreauthId);
          preauthBean.set("preauth_mode",
              (perdiemPreauthMode != null
                  && !perdiemPreauthMode.equals("")) ? Integer
                  .parseInt(perdiemPreauthMode) : 0);

          preauthBean.set("act_code_type", perdiemCodeType);
          preauthBean.set("act_code", perdiemCode);

          preauthPrescId = eauthPrescDAO.getEAuthPrescSequenceId(con,
              userid, preauthPrescId, consId, patientId,
              insuranceCoId);
          preauthBean.set("preauth_presc_id", preauthPrescId);

          if (perdiemCode != null && !perdiemCode.trim().equals("")) {

            if (eauthActDao.updateWithName(con,
                preauthBean.getMap(), "preauth_act_id") <= 0) {
              break txn;
            }

          } else {
            if (!eauthActDao.delete(con, "preauth_act_id",
                itemPrescriptionId)) {
              break txn;
            }
          }
          deleteComplObs(con, itemPrescriptionId, patientId);
        }

        String drgCode = request.getParameter("drgCode");
        drgCode = (drgCode == null) ? request.getParameter("drg_code")
            : drgCode;
        String drgCodeType = request.getParameter("drg_code_type");
        String drgActId = request.getParameter("drg_act_id");
        String drgNet = request.getParameter("drg_net");
        String drgPreauthId = request.getParameter("drg_preauth_id");
        String drgPreauthMode = request
            .getParameter("drg_preauth_mode");

        BasicDynaBean drgBean = eauthActDao.getBean();

        // DRG
        if (drgActId == null || drgActId.equals("")) {
          if (drgCode != null && !drgCode.trim().equals("")) {

            BigDecimal net = (drgNet != null && !drgNet.equals(""))
                ? new BigDecimal(drgNet)
                : BigDecimal.ZERO;

            drgBean.set("act_qty", new Integer(1));
            drgBean.set("rate", net);
            drgBean.set("discount", new BigDecimal(0));
            drgBean.set("amount", net);
            drgBean.set("claim_net_amount", net);

            // drgBean.set("prescribed_date",
            // DateUtil.getCurrentDate());
            preauthBean.set("prescribed_date",
                DateUtil.getCurrentTimestamp());
            drgBean.set("ispackage", false);
            drgBean.set("preauth_required", "Y");

            drgBean.set("mod_time", new java.sql.Timestamp(
                new java.util.Date().getTime()));
            drgBean.set("username", userid);

            drgBean.set("consultation_id", consId);
            drgBean.set("visit_id", patientId);
            drgBean.set("preauth_act_type", "DRG");

            drgBean.set("status", "A");
            drgBean.set("preauth_act_status", "O");
            drgBean.set("preauth_id", drgPreauthId);
            drgBean.set("preauth_mode",
                (drgPreauthMode != null
                    && !drgPreauthMode.equals("")) ? Integer
                    .parseInt(drgPreauthMode) : 0);

            drgBean.set("act_code_type", drgCodeType);
            drgBean.set("act_code", drgCode);

            preauthPrescId = eauthPrescDAO.getEAuthPrescSequenceId(
                con, userid, preauthPrescId, consId, patientId,
                insuranceCoId);
            drgBean.set("preauth_presc_id", preauthPrescId);

            int itemPrescriptionId = eauthActDao.getNextSequence();
            drgBean.set("preauth_act_id", itemPrescriptionId);

            if (!eauthActDao.insert(con, drgBean)) {
              break txn;
            }
          }
        } else {
          int itemPrescriptionId = Integer.parseInt(drgActId);
          drgBean.set("preauth_act_id", itemPrescriptionId);

          BigDecimal net = (drgNet != null && !drgNet.equals(""))
              ? new BigDecimal(drgNet)
              : BigDecimal.ZERO;

          drgBean.set("rate", net);
          drgBean.set("discount", new BigDecimal(0));
          drgBean.set("amount", net);
          drgBean.set("claim_net_amount", net);

          drgBean.set("mod_time", new java.sql.Timestamp(
              new java.util.Date().getTime()));
          drgBean.set("username", userid);

          drgBean.set("preauth_id", drgPreauthId);
          drgBean.set("preauth_mode",
              (drgPreauthMode != null
                  && !drgPreauthMode.equals(""))
                  ? Integer.parseInt(drgPreauthMode)
                  : 0);

          drgBean.set("act_code_type", drgCodeType);
          drgBean.set("act_code", drgCode);

          preauthPrescId = eauthPrescDAO.getEAuthPrescSequenceId(con,
              userid, preauthPrescId, consId, patientId,
              insuranceCoId);
          drgBean.set("preauth_presc_id", preauthPrescId);

          if (drgCode != null && !drgCode.trim().equals("")) {

            if (eauthActDao.updateWithName(con, drgBean.getMap(),
                "preauth_act_id") <= 0) {
              break txn;
            }

          } else {
            if (!eauthActDao.delete(con, "preauth_act_id",
                itemPrescriptionId)) {
              break txn;
            }
          }
          deleteComplObs(con, itemPrescriptionId, patientId);
        }

        String preauthEndDate = request
            .getParameter("preauth_enc_end_date");
        String preauthEndTime = request
            .getParameter("preauth_enc_end_time");
        Timestamp preauthDtTime = null;

        if (preauthEndDate != null && !preauthEndDate.trim().equals("")
            && preauthEndTime != null
            && !preauthEndTime.trim().equals("")) {
          preauthDtTime = DateUtil.parseTimestamp(
              preauthEndDate + " " + preauthEndTime);
        }

        BasicDynaBean eauthPrescBean = eauthPrescDAO.getBean();
        eauthPrescBean.set("preauth_presc_id", preauthPrescId);
        eauthPrescBean.set("username", userid);
        eauthPrescBean.set("preauth_enc_end_datetime", preauthDtTime);
        eauthPrescBean.set("preauth_payer_id", insuranceCoId);

        if (preauthPrescId != 0) {
          if (eauthPrescDAO.updateWithName(con,
              eauthPrescBean.getMap(), "preauth_presc_id") <= 0) {
            break txn;
          }
        }

        String manualPreauthStatus = request
            .getParameter("manual_preauth_status");
        if (manualPreauthStatus != null
            && !manualPreauthStatus.equals("")) {
          BasicDynaBean manualPrescBean = eauthPrescDAO.getBean();
          manualPrescBean.set("preauth_status", manualPreauthStatus);
          manualPrescBean.set("preauth_presc_id", preauthPrescId);
          manualPrescBean.set("username", userid);

          if (preauthPrescId != 0) {
            if (eauthPrescDAO.updateWithName(con,
                manualPrescBean.getMap(),
                "preauth_presc_id") <= 0) {
              break txn;
            }
          }
        }

        allSuccess = true;

      } // txn
    } finally {
      DataBaseUtil.commitClose(con, allSuccess);
    }

    if (!allSuccess) {
      flash.put("error", "Transaction failed"
          + ((error != null && !error.trim().equals("")) ? error
          : ""));
    }

    String method = "getEAuthPrescription";
    if (allSuccess) {
      String markResubmit = request.getParameter("markResubmit");
      if (markResubmit != null
          && markResubmit.equals("markForResubmission")) {
        method = "markForResubmission";
        redirect.addParameter("_comments", comments);
        redirect.addParameter("_resubmit_type", resubmissionType);
      }
    }
    redirect.addParameter("_method", method);
    redirect.addParameter("preauth_presc_id", preauthPrescId);
    redirect.addParameter("insurance_co_id", insuranceCoId);
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

    return redirect;
  }

  /**
   * Delete compl obs.
   *
   * @param con                the con
   * @param itemPrescriptionId the item prescription id
   * @param patientId          the patient id
   * @throws SQLException the SQL exception
   */
  public void deleteComplObs(Connection con, int itemPrescriptionId,
                             String patientId) throws SQLException {

    // Delete existing complaint observation.
    BasicDynaBean eauthActBean = eauthActDao.findByKey("preauth_act_id",
        itemPrescriptionId);
    String existingActCode = (eauthActBean != null
        && eauthActBean.get("act_code") != null)
        ? (String) eauthActBean.get("act_code")
        : "";

    Map<String, Object> keys = new HashMap<String, Object>();
    keys.put("preauth_act_id", itemPrescriptionId);
    keys.put("code", existingActCode);

    BasicDynaBean eauthNewActBean = eauthActDao.findByKey(con,
        "preauth_act_id", itemPrescriptionId);
    String newActCode = (eauthNewActBean != null
        && eauthNewActBean.get("act_code") != null)
        ? (String) eauthNewActBean.get("act_code")
        : "";

    String complaintVal = eauthActDao.getComplaintObsValue(con, patientId);
    BasicDynaBean eauthObsComplBean = eauthPrescObsDAO.findByKey(con, keys);
    if (eauthObsComplBean != null) {
      // Delete if is code not same
      if (!existingActCode.trim().equals(newActCode.trim())) {
        eauthPrescObsDAO.delete(con, "act_obs_id",
            eauthObsComplBean.get("act_obs_id"));
      } else {
        // Delete if code is same but complaint changed.
        String codeVal = eauthObsComplBean.get("value") != null
            ? (String) eauthObsComplBean.get("value")
            : "";
        if (!codeVal.trim().equals(complaintVal)) {
          eauthPrescObsDAO.delete(con, "act_obs_id",
              eauthObsComplBean.get("act_obs_id"));
        }
      }
    }
  }

  /**
   * Mark for resubmission.
   *
   * @param mapping the mapping
   * @param form    the form
   * @param req     the req
   * @param res     the res
   * @return the action forward
   * @throws Exception    the exception
   * @throws SQLException the SQL exception
   */
  @IgnoreConfidentialFilters
  public ActionForward markForResubmission(ActionMapping mapping,
                                           ActionForm form, HttpServletRequest req,
                                           HttpServletResponse res)
      throws Exception, SQLException {

    String preauthPresc = req.getParameter("preauth_presc_id");
    String comments = req.getParameter("_comments");
    String resubmissionType = req.getParameter("_resubmit_type");

    Connection con = null;
    boolean success = true;

    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);

      if (preauthPresc != null && !preauthPresc.equals("")) {

        int preauthPrescId = Integer.parseInt(preauthPresc);
        BasicDynaBean preauthPresBean = eauthPrescDAO
            .getPreauthPrescriptionBean(preauthPrescId);
        String preauthRequestId = (String) preauthPresBean
            .get("preauth_request_id");
        String preauthResubmitRequestId = preauthPresBean
            .get("preauth_resubmit_request_id") != null
            ? (String) preauthPresBean
            .get("preauth_resubmit_request_id")
            : null;
        String preauthPrescriptionRequestId =
            (preauthResubmitRequestId != null && !preauthResubmitRequestId.equals(""))
            ? preauthResubmitRequestId : preauthRequestId;

        BasicDynaBean reqApprovalBean = preauthReqAppDAO
            .findByKey("preauth_request_id", preauthRequestId);
        int count = 0;
        if (reqApprovalBean != null) {
          reqApprovalBean.set("file_id", null);
          reqApprovalBean.set("approval_recd_date", null);
          reqApprovalBean.set("preauth_id_payer", null);
          reqApprovalBean.set("approval_status", null);
          reqApprovalBean.set("approval_comments", null);
          reqApprovalBean.set("approval_result", null);
          reqApprovalBean.set("approval_limit", null);
          reqApprovalBean.set("is_resubmit", "Y");

          count = preauthReqAppDAO.updateWithName(con,
              reqApprovalBean.getMap(), "preauth_request_id");
          success = success && (count > 0);
        }
        BasicDynaBean preauthBean = eauthPrescDAO.getBean();
        preauthBean.set("preauth_presc_id", preauthPrescId);
        preauthBean.set("preauth_status", "R");
        preauthBean.set("resubmit_type", resubmissionType);
        preauthBean.set("comments", comments);

        int requestCount = DataBaseUtil.getIntValueFromDb(
            "SELECT count(*) FROM preauth_prescription_request WHERE preauth_presc_id = "
                + preauthPrescId);
        preauthResubmitRequestId = preauthRequestId + "-RESUBMIT-"
            + requestCount;
        preauthBean.set("preauth_resubmit_request_id",
            preauthResubmitRequestId);

        if (resubmissionType.equals("correction")) {
          preauthBean.set("resubmit_request_id_with_correction",
              "" + requestCount);
        } else {
          preauthBean.set("resubmit_request_id_with_correction",
              null);
        }

        count = eauthPrescDAO.updateWithName(con, preauthBean.getMap(), "preauth_presc_id");
        success = success && (count > 0);
      }
    } finally {
      DataBaseUtil.commitClose(con, success);
    }

    FlashScope flash = FlashScope.getScope(req);
    ActionRedirect redirect = new ActionRedirect(req.getHeader("Referer")
        .replaceAll("&" + FlashScope.FLASH_KEY + "=[0-9A-Fa-f]+", ""));
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
    return redirect;
  }

  /**
   * Adds the or edit attachment.
   *
   * @param mapping the mapping
   * @param form    the form
   * @param req     the req
   * @param res     the res
   * @return the action forward
   * @throws Exception    the exception
   * @throws SQLException the SQL exception
   */
  public ActionForward addOrEditAttachment(ActionMapping mapping,
                                           ActionForm form, HttpServletRequest req,
                                           HttpServletResponse res)
      throws Exception, SQLException {

    String preauthPresc = req.getParameter("preauth_presc_id");
    int preauthPrescId = Integer.parseInt(preauthPresc);
    String insuranceCoId = req.getParameter("insurance_co_id");
    BasicDynaBean preauthPrescBean = eauthPrescDAO
        .getEAuthPresc(preauthPrescId, insuranceCoId);
    req.setAttribute("preauthPrescBean", preauthPrescBean);

    Integer centerId = RequestContext.getCenterId();
    Integer preauthCenterId = centerId;
    String serviceRegNo = null;

    if (null != preauthPrescBean) {
      if (null != preauthPrescBean.get("center_id")) { // Visit Center
        preauthCenterId = (Integer) preauthPrescBean.get("center_id");
      }

      if (null != preauthPrescBean.get("preauth_center_id")) { // Prior
        // Auth
        // Request
        // Center
        preauthCenterId = (Integer) preauthPrescBean
            .get("preauth_center_id");
      }
    }
    if (preauthCenterId != null) {
      BasicDynaBean centerbean = new CenterMasterDAO()
          .findByKey("center_id", preauthCenterId);
      serviceRegNo = centerbean
          .get("hospital_center_service_reg_no") != null
          ? (String) centerbean
          .get("hospital_center_service_reg_no")
          : "";
    }
    req.setAttribute("service_reg_no", serviceRegNo);

    int size = eauthPrescDAO.getFileSize(preauthPrescId);
    req.setAttribute("fileSize", size);

    return mapping.findForward("addAttachment");

  }

  /**
   * Show attachment.
   *
   * @param mapping the mapping
   * @param form    the form
   * @param req     the req
   * @param res     the res
   * @return the action forward
   * @throws Exception    the exception
   * @throws SQLException the SQL exception
   */
  @IgnoreConfidentialFilters
  public ActionForward showAttachment(ActionMapping mapping, ActionForm form,
                                      HttpServletRequest req, HttpServletResponse res)
      throws Exception, SQLException {

    String preauthPresc = req.getParameter("preauth_presc_id");
    int preauthPrescId = Integer.parseInt(preauthPresc);
    Map attchMap = eauthPrescDAO.getAttachment(preauthPrescId);

    String type = (String) attchMap.get("Type");
    res.setContentType(type);

    OutputStream os = res.getOutputStream();
    InputStream file = (InputStream) attchMap.get("Content");

    byte[] bytes = new byte[4096];
    int len = 0;
    while ((len = file.read(bytes)) > 0) {
      os.write(bytes, 0, len);
    }

    os.flush();
    file.close();
    return null;
  }

  /**
   * Show activity attachment.
   *
   * @param mapping the mapping
   * @param form    the form
   * @param req     the req
   * @param res     the res
   * @return the action forward
   * @throws Exception    the exception
   * @throws SQLException the SQL exception
   */
  @IgnoreConfidentialFilters
  public ActionForward showActivityAttachment(ActionMapping mapping,
                                              ActionForm form, HttpServletRequest req,
                                              HttpServletResponse res)
      throws Exception, SQLException {

    String preauthActivity = req.getParameter("preauth_act_id");
    int preauthActivityId = Integer.parseInt(preauthActivity);
    Map attchMap = eauthPrescDAO.getActivityAttachment(preauthActivityId);

    String type = (String) attchMap.get("Type");
    res.setContentType(type);

    OutputStream os = res.getOutputStream();
    InputStream file = (InputStream) attchMap.get("Content");

    byte[] bytes = new byte[4096];
    int len = 0;
    while ((len = file.read(bytes)) > 0) {
      os.write(bytes, 0, len);
    }

    os.flush();
    file.close();
    return null;
  }

  /**
   * Delete attachment.
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
  @IgnoreConfidentialFilters
  public ActionForward deleteAttachment(ActionMapping mapping,
                                        ActionForm form, HttpServletRequest request,
                                        HttpServletResponse response)
      throws IOException, ServletException, Exception {

    String preauthPresc = request.getParameter("preauth_presc_id");
    int preauthPrescId = Integer.parseInt(preauthPresc);
    boolean success = eauthPrescDAO.deleteAttachment(preauthPrescId);
    FlashScope flash = FlashScope.getScope(request);
    if (!success) {
      flash.put("error", "Attachment could not be deleted.");
    }

    String insuranceCoId = request.getParameter("insurance_co_id");
    ActionRedirect redirect = new ActionRedirect(
        "//EAuthorization/EAuthPresc.do");
    redirect.addParameter("_method", "addOrEditAttachment");
    redirect.addParameter("preauth_presc_id", preauthPrescId);
    redirect.addParameter("insurance_co_id", insuranceCoId);
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
    return redirect;
  }

  /**
   * Gets the attachment map.
   *
   * @param request the request
   * @return the attachment map
   * @throws FileUploadException the file upload exception
   * @throws IOException         Signals that an I/O exception has occurred.
   */
  @IgnoreConfidentialFilters
  public Map<String, Object> getAttachmentMap(HttpServletRequest request)
      throws FileUploadException, IOException {

    Map<String, Object> params = new HashMap<String, Object>();
    String contentType = null;
    if (request.getContentType().split("/")[0].equals("multipart")) {

      DiskFileItemFactory factory = new DiskFileItemFactory();

      ServletFileUpload upload = new ServletFileUpload(factory);
      List<FileItem> items = upload.parseRequest(request);
      Iterator it = items.iterator();
      while (it.hasNext()) {
        FileItem item = (FileItem) it.next();
        if (item.isFormField()) {
          String name = item.getFieldName();
          String value = item.getString();
          params.put(name, new Object[] {value});
        } else {
          String fieldName = item.getFieldName();
          String fileName = item.getName();
          contentType = item.getContentType();
          boolean isInMempry = item.isInMemory();
          long sizeInBytes = item.getSize();
          if (!fileName.equals("")) {
            params.put(fieldName,
                new InputStream[] {item.getInputStream()});
            params.put("attachment_content_type",
                new String[] {contentType});
            params.put("attachment_size", new Integer[] {
                item.getInputStream().available()});
          }
        }
      }
    } else {
      params.putAll(request.getParameterMap());
    }
    return params;
  }

  /**
   * Save attachment.
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
  @IgnoreConfidentialFilters
  public ActionForward saveAttachment(ActionMapping mapping, ActionForm form,
                                      HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException, Exception {

    FlashScope flash = FlashScope.getScope(request);
    String preauthPresc = request.getParameter("preauth_presc_id");
    int preauthPrescId = Integer.parseInt(preauthPresc);

    Map<String, Object> params = getAttachmentMap(request);
    boolean success = eauthPrescDAO.updateAttachment(params,
        preauthPrescId);
    if (!success) {
      flash.put("error", "Failed to update attachment");
    }
    String insuranceCoId = request.getParameter("insurance_co_id");
    ActionRedirect redirect = new ActionRedirect(
        "//EAuthorization/EAuthPresc.do");
    redirect.addParameter("_method", "addOrEditAttachment");
    redirect.addParameter("preauth_presc_id", preauthPrescId);
    redirect.addParameter("insurance_co_id", insuranceCoId);
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
    return redirect;
  }

  /**
   * Clone prescription.
   *
   * @param mapping  the mapping
   * @param form     the form
   * @param request  the request
   * @param response the response
   * @return the action forward
   * @throws ServletException the servlet exception
   * @throws IOException      Signals that an I/O exception has occurred.
   * @throws SQLException     the SQL exception
   * @throws Exception        the exception
   */
  @IgnoreConfidentialFilters
  public ActionForward clonePrescription(ActionMapping mapping,
                                         ActionForm form, HttpServletRequest request,
                                         HttpServletResponse response)
      throws ServletException, IOException, SQLException, Exception {
    Map params = request.getParameterMap();
    int preauthPrescId = Integer.parseInt(
        ConversionUtils.getParamValue(params, "preauth_presc_id", "0"));
    boolean error = true;
    if (preauthPrescId != 0) {
      error = eauthPrescDAO.clonePrescription(preauthPrescId) == 0;
    }
    FlashScope flash = FlashScope.getScope(request);
    if (error) {
      flash.put("error", "Failed to clone the prescription");
    }
    ActionRedirect redirect = new ActionRedirect(
        mapping.findForward("listRedirect"));
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
    return redirect;
  }

  /**
   * Prints the E auth prescription.
   *
   * @param mapping  the mapping
   * @param form     the form
   * @param request  the request
   * @param response the response
   * @return the action forward
   * @throws ServletException         the servlet exception
   * @throws IOException              Signals that an I/O exception has
   *                                  occurred.
   * @throws SQLException             the SQL exception
   * @throws DocumentException        the document exception
   * @throws TemplateException        the template exception
   * @throws XPathExpressionException the x path expression exception
   * @throws TransformerException     the transformer exception
   * @throws ParseException           the parse exception
   */
  // Prior prescription print
  public ActionForward printEAuthPrescription(ActionMapping mapping,
                                              ActionForm form, HttpServletRequest request,
                                              HttpServletResponse response)
      throws ServletException, IOException, SQLException, DocumentException,
      TemplateException, XPathExpressionException, TransformerException, ParseException {

    FlashScope flash = FlashScope.getScope(request);
    String error = APIUtility.setConnectionDetails(
        servlet.getServletContext(),
        request.getParameter("request_handler_key"));
    if (error != null) {
      APIUtility.setInvalidLoginError(response, error);
      return null;
    }
    String consIdStr = request.getParameter("consultation_id");
    int consId = 0;
    if (consIdStr != null && !consIdStr.equals("")) {
      consId = Integer.parseInt(consIdStr);
    }
    String preauthPrecId = request.getParameter("preauth_presc_id");
    int prescId = 0;
    if (preauthPrecId != null && !preauthPrecId.equals("")) {
      prescId = Integer.parseInt(preauthPrecId);
    }
    String patientId = request.getParameter("patient_id");
    String printerIdStr = request.getParameter("printerType");
    BasicDynaBean prefs = null;
    int printerId = 0;
    if ((printerIdStr != null) && !printerIdStr.equals("")) {
      printerId = Integer.parseInt(printerIdStr);
    }
    prefs = PrintConfigurationsDAO.getPageOptions(
        PrintConfigurationsDAO.PRINT_TYPE_BILL, printerId);

    String printMode = "P";
    if (prefs.get("print_mode") != null) {
      printMode = (String) prefs.get("print_mode");
    }

    String userName = RequestContext.getUserName();
    Map params = new HashMap();
    EAuthPrescriptionFtlHelper ftlHelper = new EAuthPrescriptionFtlHelper();

    if (printMode.equals("P")) {
      response.setContentType("application/pdf");
      OutputStream os = response.getOutputStream();
      ftlHelper.getPriorAuthPrescriptionFtlReport(prescId, consId,
          EAuthPrescriptionFtlHelper.ReturnType.PDF, prefs, os,
          userName, patientId);
      os.close();

    } else {
      String textReport = new String(
          ftlHelper.getPriorAuthPrescriptionFtlReport(prescId, consId,
              EAuthPrescriptionFtlHelper.ReturnType.TEXT_BYTES,
              prefs, null, userName, patientId));
      request.setAttribute("textReport", textReport);
      request.setAttribute("textColumns", prefs.get("text_mode_column"));
      request.setAttribute("printerType", "DMP");
      return mapping.findForward("textPrintApplet");

    }

    return null;
  }
}
