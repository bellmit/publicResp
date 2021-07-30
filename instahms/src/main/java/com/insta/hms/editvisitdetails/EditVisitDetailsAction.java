package com.insta.hms.editvisitdetails;

import com.bob.hms.adminmasters.organization.OrgMasterDao;
import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.Registration.PatientDetailsDAO;
import com.insta.hms.Registration.PatientInsurancePlanDAO;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.adminmasters.bedmaster.BedMasterDAO;
import com.insta.hms.billing.Bill;
import com.insta.hms.billing.BillDAO;
import com.insta.hms.billing.BillingHelper;
import com.insta.hms.billing.ChangeRatePlanBO;
import com.insta.hms.billing.ChargeBO;
import com.insta.hms.billing.DiscountPlanBO;
import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PhoneNumberUtil;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.core.billing.AllocationService;
import com.insta.hms.core.clinical.forms.SectionFormService;
import com.insta.hms.diagnosticmodule.laboratory.LaboratoryDAO;
import com.insta.hms.dischargesummary.DischargeSummaryBOImpl;
import com.insta.hms.insurance.SponsorBO;
import com.insta.hms.insurance.SponsorDAO;
import com.insta.hms.integration.configuration.InterfaceEventMappingService;
import com.insta.hms.ipservices.IPBedDAO;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.CenterPreferences.CenterPreferencesDAO;
import com.insta.hms.master.DepartmentMaster.DepartmentMasterDAO;
import com.insta.hms.master.DepartmentUnitMaster.DepartmentUnitMasterDAO;
import com.insta.hms.master.DoctorMaster.DoctorMasterDAO;
import com.insta.hms.master.DynaPackage.DynaPackageChargesDAO;
import com.insta.hms.master.GenericDocumentTemplate.GenericDocumentTemplateDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.HealthAuthorityPreferences.HealthAuthorityPreferencesDAO;
import com.insta.hms.master.PatientCategory.PatientCategoryDAO;
import com.insta.hms.master.ReferalDoctor.ReferalDoctorDAO;
import com.insta.hms.master.RegistrationPreferences.RegistrationPreferencesDAO;
import com.insta.hms.messaging.MessageManager;
import com.insta.hms.messaging.MessageUtil;
import com.insta.hms.mlcdocuments.MLCDocumentsBO;
import com.insta.hms.patientcategorychange.PatientCategoryChangeDAO;
import com.insta.hms.wardactivities.defineipcareteam.IPCareDAO;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * The Class EditVisitDetailsAction.
 */
public class EditVisitDetailsAction extends DispatchAction {

  /** The log 4 j logger. */
  static Logger log4jLogger = LoggerFactory
      .getLogger(EditVisitDetailsAction.class);

  /** The reg dao. */
  VisitDetailsDAO regDao = new VisitDetailsDAO();

  /** The rd dao. */
  ReferalDoctorDAO rdDao = new ReferalDoctorDAO();

  /** The js. */
  JSONSerializer js = new JSONSerializer().exclude("class");

  private static final BillingHelper billingHelper = new BillingHelper();

  private static CenterMasterDAO centerMasterDAO = new CenterMasterDAO();

  private final AllocationService allocationService = (AllocationService) ApplicationContextProvider
      .getApplicationContext().getBean("allocationService");
  
  private final InterfaceEventMappingService interfaceEventMappingService =
      (InterfaceEventMappingService) ApplicationContextProvider.getApplicationContext()
          .getBean("interfaceEventMappingService");
  
  private static final GenericDAO sampleCollectionCentersDAO =
      new GenericDAO("sample_collection_centers");
  
  /**
   * Gets the edits the visit screen.
   *
   * @param mapping
   *          the mapping
   * @param form
   *          the form
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the edits the visit screen
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws ServletException
   *           the servlet exception
   * @throws SQLException
   *           the SQL exception
   * @throws ParseException
   *           the parse exception
   */
  @IgnoreConfidentialFilters
  public ActionForward getEditVisitScreen(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException, SQLException, ParseException {
    request.setAttribute("prevDepts", js.serialize(new HashMap()));
    return mapping.findForward("editPatientVisit");
  }

  /**
   * Gets the patient visit details.
   *
   * @param mapping
   *          the mapping
   * @param form
   *          the form
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the patient visit details
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws ServletException
   *           the servlet exception
   * @throws SQLException
   *           the SQL exception
   * @throws ParseException
   *           the parse exception
   */
  @IgnoreConfidentialFilters
  public ActionForward getPatientVisitDetails(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException, SQLException, ParseException {
    String visitId = request.getParameter("patient_id");
    Map visitbean = VisitDetailsDAO.getPatientVisitDetailsMap(visitId);
    if (visitbean == null) {
      ActionRedirect redirect = new ActionRedirect("editvisitdetails.do");
      FlashScope flash = FlashScope.getScope(request);
      redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
      redirect.addParameter("_method", "getEditVisitScreen");
      flash.error("No Patient with Id:" + visitId);
      return redirect;
    }
    Integer patientCategory = (Integer) visitbean.get("patient_category");

    if (patientCategory != null && patientCategory != 0) {
      PatientCategoryChangeDAO.setCategoryDetails(patientCategory, request);
    }

    if (VisitDetailsDAO.isVisitActive(visitId)) {
      request.setAttribute("active", true);
    } else {
      request.setAttribute("active", false);
    }

    List docDeptNameList = EditVisitDetailsDAO.getDoctorDeptList(visitId);
    if (docDeptNameList != null) {
      request.setAttribute("docDeptNameList",
          new JSONSerializer().serialize(ConversionUtils.listBeanToListMap(docDeptNameList)));
    } else {
      request.setAttribute("docDeptNameList", new JSONSerializer().serialize(null));
    }

    request.setAttribute("doctorsList", js.serialize(ConversionUtils.listBeanToListMap(
        DoctorMasterDAO.getDoctorDepartmentsDynaList((Integer) visitbean.get("center_id")))));

    HttpSession session = request.getSession();
    int sampleCollectionCenter = (Integer) session.getAttribute("sampleCollectionCenterId");
    BasicDynaBean userCollectionCenter =
        sampleCollectionCentersDAO.findByKey("collection_center_id", sampleCollectionCenter);
    request.setAttribute("userCollectionCenter", userCollectionCenter.get("collection_center"));

    int centerId = (Integer) visitbean.get("center_id");
    request.setAttribute("visitCenterWiseCollectionCenters",
        sampleCollectionCentersDAO.listAll(null, "center_id", centerId, "collection_center"));
    BasicDynaBean colcenterBean = sampleCollectionCentersDAO.findByKey("collection_center_id", -1);
    request.setAttribute("defautlCollectionCenter", colcenterBean.get("collection_center"));

    List mlcTemplateList = MLCDocumentsBO.getMLCTemplatePatList(visitId);
    if (mlcTemplateList != null && mlcTemplateList.size() > 0) {
      request.setAttribute("visit_mlc_template", mlcTemplateList.get(0));
    }

    request.setAttribute("unitList", js.serialize(
        ConversionUtils.listBeanToListMap(DepartmentUnitMasterDAO.getAllDepartmentUnits())));
    String referalNo = (String) visitbean.get("reference_docto_id");
    if (referalNo == null) {
      referalNo = "";
    }
    BasicDynaBean referalBean = rdDao.getReferalOrCenterDoctorDetails(referalNo);
    Map<String, Object> referalMap = referalBean == null ? null : referalBean.getMap();
    request.setAttribute("referalDetails", referalMap);
    request.setAttribute("regPref", RegistrationPreferencesDAO.getRegistrationPreferences());
    request.setAttribute("regPrefJSON",
        js.serialize(RegistrationPreferencesDAO.getRegistrationPreferences()));
    int userCenter = (Integer) session.getAttribute("centerId");
    request.setAttribute("healthAuthoPrefJSON", js.serialize(HealthAuthorityPreferencesDAO
        .getHealthAuthorityPreferences(CenterMasterDAO.getHealthAuthorityForCenter(userCenter))));
    request.setAttribute("mlc_templates", GenericDocumentTemplateDAO.getTemplates(true, "4", "A"));
    request.setAttribute("areAnyBillsOpen",
        EditVisitDetailsDAO.areAnyBillsOpenForThisVisit(visitId));
    request.setAttribute("isAdmitted", IPBedDAO.getAdmissionDetails(visitId) == null ? "N" : "Y");
    request.setAttribute("visitId", visitId);
    request.setAttribute("visitbean", visitbean);
    request.setAttribute("hasVisitAddlnFields",
        RegistrationPreferencesDAO.getVisitAddlnFieldsCount() > 0);
    request.setAttribute("prevDepts",
        js.serialize(VisitDetailsDAO.getAllEstablishedStatsOfPrevVisits(
            (String) visitbean.get("mr_no"), (String) visitbean.get("patient_id"))));
    if ((visitbean.get("visit_type")).equals("i")) {
      request.setAttribute("bedChargesJson",
          js.serialize(ConversionUtils.listBeanToListMap(BedMasterDAO.getAllBedDetails())));
    } else {
      request.setAttribute("bedChargesJson", js.serialize(null));
    }

    if (visitbean.get("previous_visit_id") != null
        && !((String) visitbean.get("previous_visit_id")).equals("")) {
      BasicDynaBean prevVisit = VisitDetailsDAO
          .getPatientVisitDetailsBean((String) visitbean.get("previous_visit_id"));
      request.setAttribute("prevRegdate", prevVisit.get("reg_date"));
      request.setAttribute("prevRegtime", prevVisit.get("reg_time"));
    }
    VisitDetailsDAO visitDetailsDao = new VisitDetailsDAO();
    Map<String, Object> creditLimitDetailsMap = visitDetailsDao.getCreditLimitDetails(visitId);
    request.setAttribute("creditLimitDetailsJSON", js.serialize(creditLimitDetailsMap));
    request.setAttribute("creditLimitDetailsMap", creditLimitDetailsMap);
    request.setAttribute("availableCreditLimit",
        (BigDecimal) creditLimitDetailsMap.get("availableCreditLimit"));
    request.setAttribute("deptsList",
        js.serialize(ConversionUtils.listBeanToListMap(new DepartmentMasterDAO().listAll())));
    request.setAttribute("arrdeptDetails", DepartmentMasterDAO.getDeapartmentlist());

    return mapping.findForward("editPatientVisit");
  }

  /**
   * Save patient visit details.
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
   * @throws Exception
   *           the exception
   */
  public ActionForward savePatientVisitDetails(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws Exception {
    boolean patientDischarged = false;
    boolean visitClosed = false;
    String visitId = request.getParameter("patient_id");
    ActionRedirect redirect = new ActionRedirect(
        mapping.findForwardConfig("editPatientVisitRedirect"));
    redirect.addParameter("patient_id", visitId);
    redirect.addParameter("ps_status", "active");
    FlashScope flash = FlashScope.getScope(request);

    BasicDynaBean patientRegistration = new GenericDAO("patient_registration")
        .findByKey("patient_id", visitId);
    
    long previousLogId = regDao.getLatestLogId(visitId);
    String oldDischargeStatus = "";
    if (patientRegistration != null
        && patientRegistration.get("patient_discharge_status") != null) {
      oldDischargeStatus = (String) patientRegistration.get("patient_discharge_status");
    }

    List errors = new ArrayList();
    Connection con = null;
    boolean success = true;
    String msg = "";

    VisitDetailsDAO visitdao = new VisitDetailsDAO();

    BasicDynaBean visit = visitdao.findByKey("patient_id", visitId);
    Map params = request.getParameterMap();
    BasicDynaBean editVisitBean = new VisitDetailsDAO().getBean();

    ConversionUtils.copyToDynaBean(params, editVisitBean, errors, true);
    if (!errors.isEmpty()) {
      flash.error("Error in copying the visit details");
      return redirect;
    }
    String visitOrgId = (String) visit.get("org_id");
    String mrNo = (String) visit.get("mr_no");
    String oldAdmitingDoc = (String) visit.get("doctor");
    String orgId = "ORG0001";
    if (visitOrgId != null && !visitOrgId.equals("")) {
      orgId = visitOrgId;
    }
    String patientCareOfText = (String) editVisitBean.get("patient_care_oftext");
    String defaultCode = centerMasterDAO.getCountryCode(RequestContext.getCenterId());
    List<String> splitCountryCodeAndText = PhoneNumberUtil.getCountryCodeAndNationalPart(
        patientCareOfText, null);
    if (splitCountryCodeAndText != null && !splitCountryCodeAndText.isEmpty()
        && !splitCountryCodeAndText.get(0).isEmpty()
        && patientCareOfText != null && !patientCareOfText.equals("")) {
      editVisitBean.set("patient_care_oftext", "+" + splitCountryCodeAndText.get(0)
          + splitCountryCodeAndText.get(1));
    } else if (defaultCode != null) {
      if (patientCareOfText != null && !patientCareOfText.equals("")
          && !patientCareOfText.startsWith("+")) {
        editVisitBean.set("patient_care_oftext", "+" + defaultCode + patientCareOfText);
      }
    }
    HttpSession session = request.getSession();
    String userName = (String) session.getAttribute("userid");
    editVisitBean.set("user_name", userName);
    String newAdmitingDoc = (String) editVisitBean.get("doctor");
    try {

      int billsCount = 0;
      String billNo = null;

      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);

      List mlcTemplateList = MLCDocumentsBO.getMLCTemplatePatList(visitId);
      BasicDynaBean visitMLCTemplateBean = null;
      if (mlcTemplateList != null && mlcTemplateList.size() > 0) {
        visitMLCTemplateBean = (BasicDynaBean) mlcTemplateList.get(0);
      }

      String existingMlcDocIdFormat = null;

      if (visitMLCTemplateBean != null) {
        existingMlcDocIdFormat = visitMLCTemplateBean.get("template_id") + ","
            + visitMLCTemplateBean.get("doc_format");
      }

      String templateIdFormat = request.getParameter("mlc_template");
      if (existingMlcDocIdFormat != null && !existingMlcDocIdFormat.equals("")
          && templateIdFormat != null && !existingMlcDocIdFormat.equals(templateIdFormat)) {
        // Remove MLC
        String err = EditVisitDetailsDAO.deleteMLC(con, mrNo, visitId, userName);
        if (err != null) {
          success = false;
          log4jLogger.error("Error while deleting MLC...");
          if (err.startsWith("Cannot")) {
            flash.info(err);
          } else {
            flash.error("Deletion of MLC failed...");
          }
          redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
          return redirect;
        } else {
          existingMlcDocIdFormat = null;
        }
      }

      // If MLC is added for Visit, need a bill to post MLC charge.
      if ((existingMlcDocIdFormat == null || existingMlcDocIdFormat.equals(""))
          && templateIdFormat != null && !templateIdFormat.equals("")) {

        List allActiveBills = BillDAO.getAllActiveBills(visitId);
        if (allActiveBills.size() > 0) {
          billsCount = allActiveBills.size();
        }

        Bill bill = BillDAO.getLatestOpenBillLaterElseBillNow(visitId);

        if (bill == null) {
          success = false;
          msg = msg + "No Active and Unpaid bill for MLC charge posting ...";
          redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
          redirect.addParameter("template_name", request.getParameter("mlc_template"));
          flash.info(msg);
          return redirect;
        } else {
          billNo = bill.getBillNo();
        }
      }

      // Add New Referral
      String referralName = request.getParameter("referal_name");

      if (referralName != null && !referralName.equals("")) {

        DoctorMasterDAO ddao = new DoctorMasterDAO();
        boolean duplicate = rdDao.checkDuplicateReferal(con, referralName);
        boolean dupDoctor = ddao.checkDuplicateDoctor(con, referralName);

        if (!duplicate && !dupDoctor) {
          String refId = rdDao.getNextReferalId(con);
          int referalCategory = EditVisitDetails.DEFALUT_REFERRAL_CATEGORY;
          String paymentEligible = EditVisitDetails.DEFALUT_REFERRAL_PAYMENT_ELIGIBLE;
          success = rdDao.saveNewReferal(con, refId, referralName, null, referalCategory,
              paymentEligible);
          editVisitBean.set("reference_docto_id", refId);
        }
      }

      if ((existingMlcDocIdFormat == null || existingMlcDocIdFormat.equals(""))
          && templateIdFormat != null && !templateIdFormat.equals("")) {

        editVisitBean.set("mlc_status", "Y");

        if (editVisitBean.get("mlc_no") == null
            || ((String) editVisitBean.get("mlc_no")).equals("")) {
          String mlcNumber = DataBaseUtil.getNextPatternId("MLCNO");
          editVisitBean.set("mlc_no", mlcNumber);
        }

        String templateName = request.getParameter("mlc_template_name");
        // Add MLC
        String bedType = (String) visit.get("bed_type");
        Boolean isInsurance = visit.get("primary_sponsor_id") != null
            && !((String) visit.get("primary_sponsor_id")).isEmpty();
        int planId = visit.get("plan_id") == null || visit.get("plan_id").equals("") ? 0
            : Integer.parseInt(visit.get("plan_id").toString());

        int[] planIds = new PatientInsurancePlanDAO().getPlanIds(con, visitId);

        String err = EditVisitDetailsDAO.updateMLC(con, mrNo, visitId, orgId, bedType,
            templateIdFormat, templateName, billNo, userName, isInsurance, planIds);
        if (err != null) {
          success = false;
          log4jLogger.error("Error while updating MLC...");
          flash.error("Updation of MLC failed...");
          return redirect;
        } else {
          msg = billsCount
              + " Bill(s) are open for this patient.</br> MLC charge posted to bill no " + billNo;
        }
      }

      String existingMainVisitId = visit.get("main_visit_id") != null
          ? (String) visit.get("main_visit_id") : null;
      String existingOpType = visit.get("op_type") != null ? (String) visit.get("op_type") : null;
      String newOpType = request.getParameter("op_type");

      if (existingOpType != null && newOpType != null && !existingOpType.equals(newOpType)) {
        String newMainVisitId = editVisitBean.get("main_visit_id") != null
            ? (String) editVisitBean.get("main_visit_id") : null;
        // Set Main visit Id and Op type
        editVisitBean.set("main_visit_id", newMainVisitId);
        editVisitBean.set("op_type", newOpType);
        SectionFormService sectionFormService =
            ApplicationContextProvider.getBean(SectionFormService.class);
        if ("M".equals(newOpType) || "R".equals(newOpType)) {
          sectionFormService.updateFormTypeOnConsultationTypeChange(newMainVisitId, "Form_CONS",
              "Form_OP_FOLLOW_UP_CONS");
        } else if ("F".equals(newOpType)) {
          sectionFormService
              .updateFormTypeOnConsultationTypeChange(newMainVisitId, "Form_OP_FOLLOW_UP_CONS",
                  "Form_CONS");
        }

      } else {
        editVisitBean.set("op_type", existingOpType);
        editVisitBean.set("main_visit_id", existingMainVisitId);

      }

      // calculate and update sanctioned credit limit
      String visitType = (String) visit.get("visit_type");
      if (visitType != null && visitType.equals("i")) {
        BigDecimal availableIpCreditLimit = editVisitBean.get("ip_credit_limit_amount") == null
            ? BigDecimal.ZERO : (BigDecimal) editVisitBean.get("ip_credit_limit_amount");
        BigDecimal sanctionedCreditLimit = visitdao.calculateSanctionedCreditLimit(visitId,
            availableIpCreditLimit);
        sanctionedCreditLimit = (sanctionedCreditLimit.compareTo(BigDecimal.ZERO) < 0)
            ? BigDecimal.ZERO : sanctionedCreditLimit;
        editVisitBean.set("ip_credit_limit_amount", sanctionedCreditLimit);
      }

      visitdao.update(con, editVisitBean.getMap(), "patient_id", visitId);
      // update care doctor on changing of admiting doctor.....................
      IPCareDAO ipcareDAO = new IPCareDAO();
      if (visit.get("visit_type").equals("i")) {
        List<BasicDynaBean> careBean = (List<BasicDynaBean>) ipcareDAO.findAllByKey("patient_id",
            visitId);
        Boolean flag = true;
        for (int i = 0; i < careBean.size(); i++) {
          BasicDynaBean careTeamBean = (BasicDynaBean) careBean.get(i);
          String careId = (String) careTeamBean.get("care_doctor_id");
          if (careId.equals(newAdmitingDoc)) {
            ipcareDAO.updateCareDaetaild(con, visitId, careId, newAdmitingDoc, userName);
            flag = false;
            break;
          }
        }
        if (flag) {
          ipcareDAO.updateCareDaetaild(con, visitId, oldAdmitingDoc, newAdmitingDoc, userName);
        }
      }
      Map fields = new HashMap();
      String mlcVisitId = visitdao.getMlcStatusVisitId(con, mrNo);
      if (mlcVisitId != null && !mlcVisitId.equals("")) {
        fields.put("first_mlc_visitid", mlcVisitId);
      } else {
        fields.put("first_mlc_visitid", null);
      }

      Map keys = new HashMap();
      keys.put("mr_no", mrNo);

      DataBaseUtil.dynaUpdate(con, "patient_details", fields, keys);

      String err = null;
      HashMap map = new HashMap();
      // Discharge Patient or Close Visit
      String dischargeOrClose = request.getParameter("dischargeOrcloseVisit");
      visitClosed = dischargeOrClose.equals("Y") ? true : false; 
      if (dischargeOrClose != null && dischargeOrClose.equals("Y")) {
        map.put("mr_no", mrNo);
        map.put("visitId", visitId);
        map.put("userName", userName);
        err = EditVisitDetailsDAO.dischargePatientOrCloseVisit(con, map);
        if (visitType != null && visitType.equals("i")) {
          patientDischarged = true;
        }
        if (err != null) {
          success = false;
          log4jLogger.error("Error while discharging patient or closing visit...");
          flash.error("Discharge Patient / Close Visit failed...");
          return redirect;
        }
      }
      HashMap<String, Object> patientDetailsMap = new HashMap<String, Object>();
      patientDetailsMap.put("referral_doctor_id", request.getParameter("reference_docto_id"));
      patientDetailsMap.put("discharge_doctor_id", request.getParameter("doctor"));
      patientDetailsMap.put("dept_name", request.getParameter("new_dept_name"));
      patientDetailsMap.put("admission_date", request.getParameter("reg_date"));
      patientDetailsMap.put("admission_time", request.getParameter("reg_time"));
      patientDetailsMap.put("relation", request.getParameter("relation"));
      patientDetailsMap.put("nok_contact", request.getParameter("patient_care_oftext"));
      patientDetailsMap.put("discharge_date", map.get("dis_date"));
      patientDetailsMap.put("discharge_time", map.get("dis_time"));
      patientDetailsMap.put("patient_name", request.getParameter("patient_name"));
      patientDetailsMap.put("visit_id", request.getParameter("patient_id"));
      patientDetailsMap.put("patient_contact", request.getParameter("patient_phone"));
      patientDetailsMap.put("bed_name", request.getParameter("bed_name"));
      patientDetailsMap.put("ward_name", request.getParameter("ward_name"));
      patientDetailsMap.put("patient_mail", request.getParameter("email_id"));
      patientDetailsMap.put("visit_type", visit.get("visit_type"));
      patientDetailsMap.put("mr_no", mrNo);
      patientDetailsMap.put("lang_code", PatientDetailsDAO.getContactPreference(mrNo));

      Map<String, Object> patientDischargeData = new DischargeSummaryBOImpl()
          .getPhysicalDischargeTokens(patientDetailsMap);
      boolean isSuccess = err == null
          && MessageUtil.allowMessageNotification(request, "general_message_send");
      if (isSuccess && patientDischargeData.get("patient_contact") != null
          && patientDischargeData.get("visit_type").equals("i") && dischargeOrClose != null
          && dischargeOrClose.equals("Y")) {
        patientDischargeData.put("recipient_mobile", patientDischargeData.get("patient_contact"));
        patientDischargeData.put("recipient_email", patientDischargeData.get("patient_mail"));
        patientDischargeData.put("receipient_id__", mrNo);
        MessageManager mgr = new MessageManager();
        mgr.processEvent("patient_on_discharge", patientDischargeData);
      }
      if (isSuccess && patientDischargeData.get("nok_contact") != null
          && patientDischargeData.get("visit_type").equals("i") && dischargeOrClose != null
          && dischargeOrClose.equals("Y")) {
        patientDischargeData.put("receipient_id__", mrNo);
        patientDischargeData.put("recipient_mobile", patientDischargeData.get("nok_contact"));
        MessageManager mgr = new MessageManager();
        mgr.processEvent("inform_nok_on_patient_discharge", patientDischargeData);
      }
      if (isSuccess && patientDischargeData.get("doctor_mobile") != null
          && patientDischargeData.get("visit_type").equals("i") && dischargeOrClose != null
          && dischargeOrClose.equals("Y")) {
        patientDischargeData.put("receipient_id__", mrNo);
        patientDischargeData.put("recipient_mobile", patientDischargeData.get("doctor_mobile"));
        patientDischargeData.put("recipient_email", patientDischargeData.get("doctor_mail"));
        MessageManager mgr = new MessageManager();
        mgr.processEvent("patient_physical_discharge", patientDischargeData);
      }

      if (!oldDischargeStatus.equals("D") && isSuccess && visit.get("visit_type").equals("i")) {
        List<String> signOffList = new ArrayList<>();
        List<BasicDynaBean> reportList = LaboratoryDAO.getAllReportsForPatientId(visitId);
        String contextPath = RequestContext.getRequest().getServletContext().getRealPath("");
        if (null != reportList && !reportList.isEmpty()) {
          for (BasicDynaBean reportListBean : reportList) {
            if (reportListBean.get("report_id") != null) {
              int reportId = (Integer) reportListBean.get("report_id");
              String reportIdStr = Integer.toString(reportId);
              signOffList.add(reportIdStr);
            }
          }
          String[] signOff = signOffList.toArray(new String[signOffList.size()]);
          Map<String, Object> jobData = new HashMap<>();
          jobData.put("report_id", signOff);
          jobData.put("path", contextPath);
          MessageManager mgr = new MessageManager();
          mgr.processEvent("ip_phr_diag_share", jobData);
        }
      }

    } finally {
      DataBaseUtil.commitClose(con, success);
      //Triggers HL7 message based on the event
      if (success) {
        long currentLogId = regDao.getLatestLogId(visitId);
        if (previousLogId != currentLogId && !patientDischarged && !visitClosed) {
          interfaceEventMappingService.editVisitEvent(visitId);
        }
        
        /*
         * patientDischarged will be true for IP patient if close visit is selected in edit visit
         * details screen. visitClosed will be true if close visit is selected in edit visit details
         * screen. Both are used in if else to avoid duplicate trigger of hl7 message.
         */
        if (patientDischarged) {
          interfaceEventMappingService.physicalDischargeEvent(visitId);
        } else if (visitClosed) {
          interfaceEventMappingService.visitCloseEvent(visitId, null);
        }
      }
    }
    // SMS on IP admission
    String visitType = (String) visit.get("visit_type");

    // registration successful and is IP registration and Message notification allowed
    if (success && null != visitType && visitType.equalsIgnoreCase("i")
        && MessageUtil.allowMessageNotification(request, "scheduler_message_send")) {
      // Send SMS to referal doctor
      log4jLogger
          .debug("Admission successful : Sending SMS to admitting doctor and referal doctor");

      String oldAdmittingDoctor = (String) visit.get("doctor");
      String oldReferalDoctor = (String) visit.get("reference_docto_id");
      String patientId = (String) visit.get("patient_id");
      sendIPAdmissionSMS(patientId, oldAdmittingDoctor, oldReferalDoctor);
    }
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
    redirect.addParameter("template_name", request.getParameter("mlc_template"));
    flash.info(msg);
    return redirect;
  }

  /**
   * Update rate plan.
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
   * @throws Exception
   *           the exception
   */
  @IgnoreConfidentialFilters
  public ActionForward updateRatePlan(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws Exception {
    String visitId = request.getParameter("visitId");
    if (visitId != null) {
      BasicDynaBean bean = regDao.getVisitDetailsWithConfCheck(visitId);
      if (bean == null) {
        // user entered visit id is not a valid patient id.
        FlashScope flash = FlashScope.getScope(request);
        flash.put("error", "No Patient with Id:" + visitId);
        ActionRedirect redirect = new ActionRedirect(mapping.findForward("ratePlanRedirect"));
        redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
        return redirect;
      }
      Map patientDetails = VisitDetailsDAO.getPatientVisitDetailsMap(visitId);
      Integer centerId = (Integer) patientDetails.get("center_id");
      request.setAttribute("genPrefsRatePLan", OrgMasterDao
          .getOrgdetailsDynaBean(CenterPreferencesDAO.getRatePlanForNonInsuredBills(centerId)));
      request.setAttribute("patient", patientDetails);
      request.setAttribute("billNo", request.getParameter("billNo"));
      request.setAttribute("mrNo", patientDetails.get("mr_no"));
      request.setAttribute("visitType", patientDetails.get("visit_type"));
      request.setAttribute("isNewUX", request.getParameter("isNewUX"));

      request.setAttribute("allowedRatePlans",
          js.deepSerialize(EditVisitDetailsDAO.getAllowedRatePlans(visitId)));
      request.setAttribute("bedTypesListJSON", js
          .serialize(ConversionUtils.copyListDynaBeansToMap(BedMasterDAO.getAllBillingBedTypes())));
      request.setAttribute("categoryWiseRateplans",
          js.serialize(ConversionUtils.listBeanToListMap(PatientCategoryDAO.getAllowedRatePlans(
              patientDetails.get("patient_category") == null ? 0
                  : (Integer) patientDetails.get("patient_category"),
              (String) patientDetails.get("visit_type")))));

      List bills = BillDAO.getAllActiveBills(visitId);
      request.setAttribute("bills", bills);
      request.setAttribute("allOpenBillsJSON", js.serialize(bills));

    }
    return mapping.findForward("changeRatePlan");
  }

  /**
   * Sets the rate plan details.
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
   * @throws Exception
   *           the exception
   */
  public ActionForward setRatePlanDetails(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws Exception {
    String updatedRatePlan = request.getParameter("updated_rate_plan");
    String updatedBedType = request.getParameter("updated_bed_type");
    String visitId = request.getParameter("visitId");
    HttpSession session = request.getSession();
    String userName = (String) session.getAttribute("userid");
    SponsorDAO sponsorDAO = new SponsorDAO();
    Connection con = DataBaseUtil.getConnection();
    log4jLogger.debug("Updating Rate Plan...");
    if (updatedRatePlan == null || updatedRatePlan.equals("")) {
      Map columndata = new HashMap();
      columndata.put("org_id", "ORG0001");
      columndata.put("user_name", userName);
      new VisitDetailsDAO().update(con, columndata, "patient_id", visitId);
    } else {
      Map columndata = new HashMap();
      columndata.put("org_id", updatedRatePlan);
      columndata.put("user_name", userName);
      new VisitDetailsDAO().update(con, columndata, "patient_id", visitId);
    }
    if (updatedBedType == null || updatedBedType.equals("")) {
      Map columndata = new HashMap();
      columndata.put("bed_type", "GENERAL");
      columndata.put("user_name", userName);
      new VisitDetailsDAO().update(con, columndata, "patient_id", visitId);
    } else {
      Map columndata = new HashMap();
      columndata.put("bed_type", updatedBedType);
      columndata.put("user_name", userName);
      new VisitDetailsDAO().update(con, columndata, "patient_id", visitId);
    }
    DataBaseUtil.closeConnections(con, null);
    con = DataBaseUtil.getConnection();
    con.setAutoCommit(false);
    String success = "";

    ChangeRatePlanBO chRatePlanBO = new ChangeRatePlanBO();
    StringBuilder successMsg = new StringBuilder();
    ArrayList<String> ratePlanNotApplicableList = new ArrayList<>();
    chRatePlanBO.setSuccessMsg(successMsg);
    chRatePlanBO.setRatePlanNotApplicableList(ratePlanNotApplicableList);
    chRatePlanBO.setEditVisits(false);

    // List<Bill> allActiveBills = BillDAO.getAllActiveBills(visitId);
    List<BasicDynaBean> allActiveBills = BillDAO.getAllActiveBillsNew(visitId);
    BasicDynaBean patientDetails = new VisitDetailsDAO().findByKey("patient_id", visitId);
    Integer centerId = (Integer) patientDetails.get("center_id");
    String prefRatePlan = CenterPreferencesDAO.getRatePlanForNonInsuredBills(centerId);
    String organization = (String) patientDetails.get("org_id");
    String primarySponsorId = patientDetails.get("primary_sponsor_id") != null
        ? (String) patientDetails.get("primary_sponsor_id") : null;

    if (updatedRatePlan == null || updatedRatePlan.equals("")) {
      updatedRatePlan = organization;
    }

    if (updatedRatePlan == null || updatedRatePlan.equals("")) {
      updatedRatePlan = "ORG0001";
    }

    boolean visitHasTpa = primarySponsorId != null && !primarySponsorId.equals("");
    boolean allowBillNowInsurance = BillDAO.isBillInsuranceAllowed(Bill.BILL_TYPE_PREPAID, true);

    for (BasicDynaBean billDTO : allActiveBills) {

      String billRatePlan = (String) billDTO.get("bill_rate_plan_id");
      int billDynaPkgId = (int) billDTO.get("dyna_package_id");

      if (visitHasTpa) {
        if (billDTO.get("bill_type").equals(Bill.BILL_TYPE_PREPAID) && allowBillNowInsurance) {
          billDTO.set("bill_rate_plan_id", (boolean) billDTO.get("is_tpa") ? organization
              : (prefRatePlan == null ? organization : prefRatePlan));

        } else if (billDTO.get("bill_type").equals(Bill.BILL_TYPE_CREDIT)) {
          billDTO.set("bill_rate_plan_id", (boolean) billDTO.get("is_tpa") ? organization
              : (prefRatePlan == null ? organization : prefRatePlan));

        } else {
          billDTO.set("bill_rate_plan_id", prefRatePlan == null ? organization : prefRatePlan);
        }
      } else {
        billDTO.set("bill_rate_plan_id", organization);
      }

      String visitBedType = (String) patientDetails.get("bed_type");
      if (billDynaPkgId != 0 && billRatePlan != null
          && !billRatePlan.equals(billDTO.get("bill_rate_plan_id"))) {
        Map<String, Object> keys = new HashMap<String, Object>();
        keys.put("dyna_package_id", billDynaPkgId);
        keys.put("org_id", billDTO.get("bill_rate_plan_id"));
        keys.put("bed_type", visitBedType);
        BasicDynaBean dynapgkbean = new DynaPackageChargesDAO().findByKey(keys);
        if (dynapgkbean != null) {
          billDTO.set("dyna_package_charge", (BigDecimal) dynapgkbean.get("charge"));
        } else {
          billDTO.set("dyna_package_charge", BigDecimal.ZERO);
        }

        billDTO.set("mod_time", new java.sql.Timestamp(new java.util.Date().getTime()));
      }

      success = new GenericDAO("bill").updateWithName(con, billDTO.getMap(), "bill_no") > 0 ? ""
          : "Failed to update bills rate plan";
    }

    DataBaseUtil.commitClose(con, success.equals(""));

    String applyCharges = request.getParameter("apply_charge");
    BasicDynaBean genericPrefs = GenericPreferencesDAO.getAllPrefs();
    boolean shouldUpdateCharge = "Y".equals(genericPrefs.get("update_charge_on_rate_plan_change"));
    if (shouldUpdateCharge) {
      if (applyCharges.equals("Y")) {
        success = chRatePlanBO.updateChargesBedAndRateWiseNew(visitId, updatedBedType);
        if ("Y".equals(
            GenericPreferencesDAO.getAllPrefs().get("set_preauth_approved_amt_as_claim_amt"))) {
          // Only unlock the charges without priorauth. check HMS-33687
          sponsorDAO.unlockVisitBillsChargesWithoutPreAuth(visitId);
        } else {
          // unlock all the charges.
          sponsorDAO.unlockVisitBillsCharges(visitId);
        }
      } else if (applyCharges.equals("D")) {
        String fromDate = request.getParameter("fromDate");
        String fromTime = request.getParameter("fromTime");
        String toDate = request.getParameter("toDate");
        String toTime = request.getParameter("toTime");
        java.sql.Timestamp fromDateTime = DateUtil.parseTimestamp(fromDate, fromTime);
        java.sql.Timestamp toDateTime = DateUtil.parseTimestamp(toDate, toTime);
        success = chRatePlanBO.updateChargesBedAndRateWiseNew(visitId, updatedBedType, fromDateTime,
            toDateTime);

        // get the charges in date range and unlock the charges.
        ChargeBO chargeBO = new ChargeBO();
        List<String> chargesList = chargeBO.getChargesByPostedDateRange(visitId, fromDateTime,
            toDateTime);
        if (null != chargesList && !chargesList.isEmpty()) {
          sponsorDAO.unlockCharges(chargesList);
        }
      }
    }

    FlashScope flash = FlashScope.getScope(request);

    if (success != null && !success.equals("")) {
      flash.info(success);
      // List<Bill> openBills = BillDAO.getAllActiveBills(visitId);

      // apply discount rule on bills
      DiscountPlanBO discBo = new DiscountPlanBO();
      String openbillNo = null;
      billingHelper.resetInventoryCharges(visitId);

      for (BasicDynaBean billBean : allActiveBills) {
        try {
          con = DataBaseUtil.getConnection();
          con.setAutoCommit(false);
          openbillNo = (String) billBean.get("bill_no");

          discBo.applyDiscountRule(con, openbillNo);

          EditVisitDetailsDAO.updateBillChargesForPolicyNew(con, visitId, false, openbillNo, null);
        } finally {
          DataBaseUtil.commitClose(con, true);
        }
      }

      billingHelper.replayInventoryReturns(visitId);
      for (BasicDynaBean bill : allActiveBills) {
        if (bill.get("bill_no") != null && !bill.get("bill_no").equals("")) {
          BillDAO.resetTotalsOrReProcessNew((String) bill.get("bill_no"));
        }
      }
      // Call the allocation job and update the patient payments for the created bill.
      List<BasicDynaBean> visitbills = BillDAO.getAllActiveBillsNew(visitId);
      for (BasicDynaBean bill : visitbills) {
        String billNo = bill.get("bill_no").toString();
        allocationService.updateBillTotal(billNo);
        // Call the Allocation method.
        allocationService.allocate(billNo, centerId);
      }
    }

    new SponsorBO().recalculateSponsorAmount(visitId);

    if (success != null && !success.equals("")) {
      for (BasicDynaBean bill : allActiveBills) {
        if (bill.get("bill_no") != null && !bill.get("bill_no").equals("")) {
          BillDAO.resetRoundOff((String) bill.get("bill_no"));
        }
      }
    }

    if (updatedRatePlan != null && !updatedRatePlan.equals("")
        && chRatePlanBO.getRatePlanNotApplicableList() != null
        && chRatePlanBO.getRatePlanNotApplicableList().size() > 0) {
      BasicDynaBean orgBean = new GenericDAO("organization_details").findByKey("org_id",
          updatedRatePlan);
      String newRatePlanName = (String) orgBean.get("org_name");
      StringBuffer sb = new StringBuffer(
          "There are some charges in bill(s) which are not applicable for rate plan: "
              + newRatePlanName + "</br>");
      if (chRatePlanBO.getRatePlanNotApplicableList().size() > 5) {
        flash.warning(sb.toString());
      } else {
        for (String chargeDesc : (ArrayList<String>) chRatePlanBO.getRatePlanNotApplicableList()) {
          sb.append(chargeDesc + "</br>");
        }
        flash.warning(sb.toString());
      }
    }

    String visitType = (String) patientDetails.get("visit_type");
    BigDecimal visitPatientDue = BillDAO.getVisitPatientDue(visitId);
    Map genPrefMap = GenericPreferencesDAO.getAllPrefs().getMap();
    String crLimitRulePref = genPrefMap.get("ip_credit_limit_rule") != null
        ? (String) genPrefMap.get("ip_credit_limit_rule") : "A";
    BigDecimal availableCreditLimit = regDao.getAvailableCreditLimit(visitId, false);
    if (visitType.equals("i") && availableCreditLimit.compareTo(BigDecimal.ZERO) < 0
        && (crLimitRulePref.equals("W") || crLimitRulePref.equals("B"))) {
      String crLimitMsg = "The current patient outstanding is : " + visitPatientDue
          + " Available Credit Limit is : " + availableCreditLimit;
      flash.info(flash.get("info") == null || ((String) flash.get("info")).equals("") ? crLimitMsg
          : (String) flash.get("info") + " <br/> " + crLimitMsg);
    }

    ActionRedirect redirect = new ActionRedirect(mapping.findForward("ratePlanRedirect"));
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
    redirect.addParameter("isNewUX", request.getParameter("isNewUX"));
    redirect.addParameter("visitId", visitId);
    redirect.addParameter("apply_charge", applyCharges);
    return redirect;
  }

  /**
   * Send IP admission SMS.
   *
   * @param patientId
   *          the patient id
   * @param oldAdmittingDoctor
   *          the old admitting doctor
   * @param oldReferalDoctor
   *          the old referal doctor
   * @throws SQLException
   *           the SQL exception
   * @throws ParseException
   *           the parse exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  private void sendIPAdmissionSMS(String patientId, String oldAdmittingDoctor,
      String oldReferalDoctor) throws SQLException, ParseException, IOException {
    MessageManager mgr = new MessageManager();
    Map<String, String> admissionData = getIPAdmissionData(patientId);

    String admittingDoctorId = admissionData.get("admitting_doctor_id__");
    String referalDoctorId = admissionData.get("referal_doctor_id__");
    String admitDocMobileNo = admissionData.get("doctor_mobile");
    String refDocMobileNo = admissionData.get("referal_doctor_mobile");
    String admAndRefDocMobileno = null;

    if (null != admitDocMobileNo && !admitDocMobileNo.trim().equals("")
        && !admittingDoctorId.equalsIgnoreCase(oldAdmittingDoctor)) {
      admAndRefDocMobileno = admitDocMobileNo.trim();
    }

    if (null != refDocMobileNo && !refDocMobileNo.trim().equals("")
        && !referalDoctorId.equalsIgnoreCase(oldReferalDoctor)) {
      // no mobile number for admitting doctor
      if (admAndRefDocMobileno == null) {
        admAndRefDocMobileno = refDocMobileNo.trim();
      } else {
        admAndRefDocMobileno = admAndRefDocMobileno.concat(",").concat(refDocMobileNo.trim());
      }
    }
    // Send SMS to admitting doctor
    if (null != admAndRefDocMobileno && !admAndRefDocMobileno.trim().equals("")) {

      admissionData.put("recipient_mobile", admAndRefDocMobileno);
      mgr.processEvent("patient_admitted", admissionData);
    } else {
      log4jLogger.info("Admitting and Referal Doctor mobile # not available / admitting doctor "
          + "not changed, skipping SMS on admission");
    }

  }

  /**
   * Gets the IP admission data.
   *
   * @param patientId
   *          the patient id
   * @return the IP admission data
   * @throws SQLException
   *           the SQL exception
   */
  private Map<String, String> getIPAdmissionData(String patientId) throws SQLException {
    BasicDynaBean bean = VisitDetailsDAO.getPatientVisitDetailsBean(patientId);
    Map<String, String> admissionData = new HashMap<String, String>();

    String referalDoctorId = (String) bean.get("reference_docto_id");
    // referal doctor mobile no is not available as part of patient_details_ext_view
    String referalDoctorQuery = "SELECT referrer_phone from all_referrers_view WHERE id = ?";
    String referalDocMobile = DataBaseUtil.getStringValueFromDb(
        referalDoctorQuery, referalDoctorId);

    admissionData.put("ward_name", (String) bean.get("reg_ward_name"));
    admissionData.put("patient_name", (String) bean.get("full_name"));
    admissionData.put("bed_name", (String) bean.get("alloc_bed_name"));
    admissionData.put("admission_date", DateUtil.formatDate((java.util.Date) bean.get("reg_date")));
    admissionData.put("admission_date_yyyy_mm_dd",
        new DateUtil().getSqlDateFormatter().format((java.util.Date) bean.get("reg_date")));
    admissionData.put("admission_time", (String) bean.get("reg_time").toString());
    admissionData.put("admission_time_12hr",
        DateUtil.formatTimeMeridiem((java.sql.Time) bean.get("reg_time")));
    admissionData.put("center_name", (String) bean.get("center_name"));
    admissionData.put("center_code", (String) bean.get("center_code"));
    admissionData.put("complaint", (String) bean.get("complaint"));
    admissionData.put("admitted_by", (String) bean.get("admitted_by"));
    admissionData.put("department", (String) bean.get("dept_name"));
    admissionData.put("department_id", (String) bean.get("dept_id"));
    admissionData.put("patient_phone", (String) bean.get("patient_phone"));
    admissionData.put("receipient_id__", (String) bean.get("mr_no"));
    admissionData.put("receipient_type__", "PATIENT");
    admissionData.put("visit_id", patientId);

    admissionData.put("doctor_name", (String) bean.get("doctor_name"));
    admissionData.put("referal_doctor", (String) bean.get("refdoctorname"));
    admissionData.put("doctor_mobile", (String) bean.get("doctor_mobile"));
    admissionData.put("referal_doctor_mobile", referalDocMobile);
    admissionData.put("admitting_doctor_id__", (String) bean.get("doctor"));
    admissionData.put("referal_doctor_id__", (String) bean.get("reference_docto_id"));
    admissionData.put("salutation_name", (String) bean.get("salutation"));
    return admissionData;
  }
}
