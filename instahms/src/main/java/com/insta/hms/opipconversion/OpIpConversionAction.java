package com.insta.hms.opipconversion;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.bob.hms.common.Preferences;
import com.insta.hms.OTServices.OperationDetailsBO;
import com.insta.hms.Registration.PatientDetailsDAO;
import com.insta.hms.Registration.PatientInsurancePlanDAO;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.adminmasters.bedmaster.BedMasterDAO;
import com.insta.hms.billing.Bill;
import com.insta.hms.billing.BillActivityChargeDAO;
import com.insta.hms.billing.BillBO;
import com.insta.hms.billing.BillChargeClaimDAO;
import com.insta.hms.billing.BillDAO;
import com.insta.hms.billing.ChargeDAO;
import com.insta.hms.billing.ChargeDTO;
import com.insta.hms.billing.DRGCalculator;
import com.insta.hms.billing.ReceiptRelatedDAO;
import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.modulesactivated.ModulesActivatedService;
import com.insta.hms.core.billing.AllocationService;
import com.insta.hms.core.billing.DepositType;
import com.insta.hms.core.clinical.consultation.prescriptions.PendingPrescriptionsService;
import com.insta.hms.core.clinical.forms.SectionFormService;
import com.insta.hms.core.fa.AccountingJobScheduler;
import com.insta.hms.diagnosticmodule.laboratory.LaboratoryDAO;
import com.insta.hms.editvisitdetails.EditVisitDetailsDAO;
import com.insta.hms.instaforms.PatientSectionDetailsDAO;
import com.insta.hms.insurance.InsuranceDAO;
import com.insta.hms.insurance.SponsorBO;
import com.insta.hms.insurance.SponsorDAO;
import com.insta.hms.integration.configuration.InterfaceEventMappingService;
import com.insta.hms.integration.hl7.message.v23.ADTService;
import com.insta.hms.master.DepartmentMaster.DepartmentMasterDAO;
import com.insta.hms.master.IPPreferences.IPPreferencesDAO;
import com.insta.hms.master.RegistrationPreferences.RegistrationPreferencesDAO;
import com.insta.hms.master.RegistrationPreferences.RegistrationPreferencesDTO;
import com.insta.hms.medicalrecorddepartment.MRDDiagnosisDAO;
import com.insta.hms.messaging.MessageManager;
import com.insta.hms.messaging.MessageUtil;
import com.insta.hms.orders.OrderBO;
import com.insta.hms.orders.OrderDAO;
import com.insta.hms.outpatient.DoctorConsultationDAO;
import com.insta.hms.outpatient.SecondaryComplaintDAO;
import com.insta.hms.patientcategorychange.PatientCategoryChangeDAO;
import com.insta.hms.resourcescheduler.ResourceBO;
import com.insta.hms.resourcescheduler.ResourceDAO;
import com.insta.hms.resourcescheduler.ResourceDTO;
import com.insta.hms.services.ServicesDAO;
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

// TODO: Auto-generated Javadoc
/**
 * The Class OpIpConversionAction.
 */
public class OpIpConversionAction extends DispatchAction {

  /** The log 4 j logger. */
  static Logger log4jLogger = LoggerFactory.getLogger(OpIpConversionAction.class);

  /** The credit note dao. */
  GenericDAO creditNoteDao = new GenericDAO("bill_credit_notes");

  /** The ins plan DAO. */
  static PatientInsurancePlanDAO insPlanDAO = new PatientInsurancePlanDAO();

  /** The pat ins plan details DAO. */
  GenericDAO patInsPlanDetailsDAO = new GenericDAO("patient_insurance_plan_details");

  /** The ins plan details DAO. */
  GenericDAO insPlanDetailsDAO = new GenericDAO("insurance_plan_details");

  /** The visit detailsdao. */
  static VisitDetailsDAO visitDetailsdao = new VisitDetailsDAO();

  /** The scheduler appointments dao. */
  static GenericDAO schedulerAppointmentsDao = new GenericDAO("scheduler_appointments");

  /** The insurance plan main dao. */
  static GenericDAO insurancePlanMainDao = new GenericDAO("insurance_plan_main");
  
  private final AllocationService allocationService = (AllocationService) ApplicationContextProvider
      .getApplicationContext().getBean("allocationService");
  
  ModulesActivatedService modulesActivatedService = ApplicationContextProvider
      .getBean(ModulesActivatedService.class);

  PendingPrescriptionsService pendingPrescriptionsService = ApplicationContextProvider
      .getBean(PendingPrescriptionsService.class);
  
  InterfaceEventMappingService interfaceEventMappingService = ApplicationContextProvider
      .getBean(InterfaceEventMappingService.class);
  
  /** The accounting job scheduler. */
  static AccountingJobScheduler accountingJobScheduler = ApplicationContextProvider
      .getBean(AccountingJobScheduler.class);
  
  /**
   * Gets the OPIP conversion.
   *
   * @param mapping
   *          the mapping
   * @param form
   *          the form
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the OPIP conversion
   * @throws ServletException
   *           the servlet exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws SQLException
   *           the SQL exception
   */
  public ActionForward getOPIPConversion(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException, SQLException {

    String visitId = request.getParameter("patient_id");
    String category = request.getParameter("category");

    List docDeptNameList = EditVisitDetailsDAO.getDoctorDeptList(visitId);
    if (docDeptNameList != null) {
      request.setAttribute("docDeptNameList",
          new JSONSerializer().serialize(ConversionUtils.listBeanToListMap(docDeptNameList)));
    } else {
      request.setAttribute("docDeptNameList", new JSONSerializer().serialize(null));
    }


    List<BasicDynaBean> sponsorList = insPlanDAO.getSponsorDetails(visitId);
    List<BasicDynaBean> planList = insPlanDAO.getPlanDetails(visitId);

    boolean isPlanExist = null != planList && planList.size() > 0;
    request.setAttribute("isPlanExist", isPlanExist);

    String ipApplicable = "Y";
    for (BasicDynaBean bean : sponsorList) {
      if (null != bean.get("plan_id") && !bean.get("plan_id").toString().equals("0")) {
        BasicDynaBean planBean = insurancePlanMainDao.findByKey("plan_id",
            Integer.parseInt(bean.get("plan_id").toString()));
        if (planBean != null
            && planBean.get("ip_applicable") != null && !ipApplicable.equals("N")) {
          ipApplicable = (String) planBean.get("ip_applicable");
        }
      }
    }
    request.setAttribute("ipApplicable", ipApplicable);

    BasicDynaBean visitbean = visitDetailsdao.findByKey("patient_id", visitId);
    request.setAttribute("isTpa", visitbean.get("primary_sponsor_id") == null
        || visitbean.get("primary_sponsor_id").equals("") ? "N" : "Y");

    Integer patientCategory = (Integer) visitbean.get("patient_category_id");

    if (patientCategory != null && patientCategory != 0) {
      PatientCategoryChangeDAO.setCategoryDetails(patientCategory, request);
    }

    List visitCreditBills = BillDAO.getActiveHospitalBills(visitId, BillDAO.bill_type.CREDIT);
    request.setAttribute("patientHasCreditBill",
        visitCreditBills != null && !visitCreditBills.isEmpty());

    Connection con = DataBaseUtil.getConnection();
    try {
      Map<String, List<BasicDynaBean>> creditNoteBillsMap =
          new HashMap<String, List<BasicDynaBean>>();
      List patientBills = new BillDAO(con).getPatientBills(visitId);
      request.setAttribute("patientBills", patientBills);

      for (Object bill : patientBills) {
        Bill billPat = (Bill) bill;
        if (!(!billPat.getRestrictionType().equals("P")
            && billPat.getTotalAmount().compareTo(BigDecimal.ZERO) == -1)) {
          String billNo = billPat.getBillNo();
          List<BasicDynaBean> creditNotesList = new BillDAO(con).getListOfCreditNotesOfBill(billNo);
          // creditNoteDao.findAllByKey(con, "bill_no", billNo);
          creditNoteBillsMap.put(billNo, creditNotesList);
        }
      }
      request.setAttribute("creditNoteBillsMap", creditNoteBillsMap);
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }

    JSONSerializer js = new JSONSerializer().exclude("class");

    request.setAttribute("arrdeptDetails", DepartmentMasterDAO.getDeapartmentlist());
    request.setAttribute("deptsList",
        js.serialize(ConversionUtils.listBeanToListMap(new DepartmentMasterDAO().listAll())));

    request.setAttribute("bedChargesJson",
        js.serialize(ConversionUtils.listBeanToListMap(BedMasterDAO.getBillingBedDetails())));
    request.setAttribute("regPrefFields", RegistrationPreferencesDAO.getRegistrationPreferences());
    request.setAttribute("visitId", visitId);

    String appointId = request.getParameter("appointment_id");
    if (appointId != null && !appointId.equals("")) {
      request.setAttribute("appointId", appointId);
      request.setAttribute("category", category);
      request.setAttribute("mrno", request.getParameter("mrno"));
      request.setAttribute("patientId", visitId);
    }
    Map allowedRatePlansMap = EditVisitDetailsDAO.getAllowedRatePlans(visitId, "i");
    boolean isTpaValidForIp = EditVisitDetailsDAO.isTpaValidForIp(visitId,
        (String) visitbean.get("primary_sponsor_id"));
    request.setAttribute("isTpaValidForIP", isTpaValidForIp ? "Y" : "N");
    request.setAttribute("allowedRatePlansMap", js.deepSerialize(allowedRatePlansMap));

    return mapping.findForward("getopipconversionscreen");
  }

  /**
   * Check if unused case present.
   *
   * @param mrNo
   *          the mr no
   * @param visitBean
   *          the visit bean
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   */
  public boolean checkIfUnusedCasePresent(String mrNo, BasicDynaBean visitBean)
      throws SQLException {
    Boolean unUsedCasePresnt = false;
    Connection con = DataBaseUtil.getConnection();
    List<BasicDynaBean> patientCases = new InsuranceDAO().findAllByKey("mr_no", mrNo);
    for (BasicDynaBean patientCase : patientCases) {
      BasicDynaBean insurVisitBean = visitDetailsdao.findByKey("insurance_id",
          (Integer) patientCase.get("insurance_id"));
      if (insurVisitBean == null
          && patientCase.get("tpa_id").equals(visitBean.get("primary_sponsor_id"))) {
        unUsedCasePresnt = true;
      }
    }
    DataBaseUtil.closeConnections(con, null);
    return unUsedCasePresnt;
  }

  /**
   * Disconnect TPA.
   *
   * @param visitBean
   *          the visit bean
   */
  public void disconnectTPA(BasicDynaBean visitBean) {
    if (visitBean != null && !visitBean.equals("")) {
      visitBean.set("primary_sponsor_id", null);
      visitBean.set("secondary_sponsor_id", null);
      visitBean.set("insurance_id", null);
      visitBean.set("primary_insurance_approval", null);
      visitBean.set("secondary_insurance_approval", null);
      visitBean.set("plan_id", 0);
      visitBean.set("category_id", 0);
      visitBean.set("primary_insurance_co", null);
      visitBean.set("secondary_insurance_co", null);
      visitBean.set("patient_policy_id", null);
      visitBean.set("patient_corporate_id", null);
      visitBean.set("secondary_patient_corporate_id", null);
      visitBean.set("patient_national_sponsor_id", null);
      visitBean.set("secondary_patient_national_sponsor_id", null);
    }
  }

  /**
   * Save OPIP conversion.
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
   * @throws ServletException
   *           the servlet exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws SQLException
   *           the SQL exception
   * @throws Exception
   *           the exception
   */
  public ActionForward saveOPIPConversion(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException, SQLException, Exception {

    PatientDetailsDAO pdao = new PatientDetailsDAO();
    ActionRedirect redirect = new ActionRedirect(
        mapping.findForwardConfig("opipconversionredirect"));
    redirect.addParameter("ps_status", "active");
    FlashScope flash = FlashScope.getScope(request);
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

    HttpSession session = request.getSession();
    String username = (String) session.getAttribute("userid");
    Preferences pref = (Preferences) session.getAttribute("preferences");
    String modAdvanceOTActive = "Y";
    String errorMsg = null;
    if ((pref != null) && (pref.getModulesActivatedMap() != null)) {
      modAdvanceOTActive = (String) pref.getModulesActivatedMap().get("mod_advanced_ot");
      if (modAdvanceOTActive == null || modAdvanceOTActive.equals("")) {
        modAdvanceOTActive = "N";
      }
    }

    String opVisitId = request.getParameter("patient_id");
    String ipVisitId = null;
    String appointId = request.getParameter("appointId");
    String category = request.getParameter("category");
    BillChargeClaimDAO billChgClaimDAO = new BillChargeClaimDAO();
    Connection con = null;
    boolean allSuccess = false;

    String[] nullifyFields = { "discharge_date", "discharge_time", "discharge_doctor_id",
        "discharge_format", "discharge_doc_id", "discharge_finalized_date",
        "discharge_finalized_time", "discharged_by", "disch_date_for_disch_summary",
        "disch_time_for_disch_summary", "discharge_type_id", "discharge_remarks" };

    RegistrationPreferencesDTO regPrefs = RegistrationPreferencesDAO.getRegistrationPreferences();
    // Map params = request.getParameterMap();

    String[] billNosArray = request.getParameterValues("bilNo");
    String[] billActionsArray = request.getParameterValues("billAction");

    List<String> billNosList = new ArrayList<String>();
    List<String> billActionsList = new ArrayList<String>();
    if (billNosArray != null) {
      for (int i = 0; i < billNosArray.length; i++) {
        String billNo = billNosArray[i];
        billNosList.add(billNo);
        billActionsList.add(billActionsArray[i]);

        List<BasicDynaBean> creditNotesList = creditNoteDao.findAllByKey("bill_no", billNo);
        for (BasicDynaBean bean : creditNotesList) {
          String creditNoteBillNo = (String) bean.get("credit_note_bill_no");
          billNosList.add(creditNoteBillNo);
          billActionsList.add(billActionsArray[i]);
        }
      }
    }

    String[] billNos = billNosList.toArray(new String[0]);
    String[] billActions = billActionsList.toArray(new String[0]);


    // String prescriptionId = request.getParameter("prescription_id");
    String newBillNo = null;
    BasicDynaBean opToIpVisitBean = null;
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);


      opToIpVisitBean = visitDetailsdao.findByKey("patient_id", opVisitId);


      ChargeDAO chDao = new ChargeDAO(con);
      
      resetDoctorExclusionCharges(chDao, billNosArray, billActionsArray);

      String bedtype = request.getParameter("bed_type");
      if (!(bedtype != null && !bedtype.equals(""))) {
        redirect = new ActionRedirect(mapping.findForwardConfig("opipconversionerrorredirect"));
        redirect.addParameter("patient_id", opVisitId);
        redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
        log4jLogger.error("bed type can't be empty ...");
        flash.error("bed type can't be empty...");
        return redirect;
      }

      String allocateBedAtRegPref = (String) new IPPreferencesDAO().getPreferences()
          .get("allocate_bed_at_reg");
      if (allocateBedAtRegPref.equals("Y")) {
        BasicDynaBean bedTypeBean = new BedMasterDAO().getBedType(bedtype);
        bedtype = (bedTypeBean.get("billing_bed_type").equals("Y") ? bedtype : "GENERAL");
      }

      BillDAO billDao = new BillDAO(con);

      int visitCenterId = (Integer) opToIpVisitBean.get("center_id");
      
      ipVisitId = VisitDetailsDAO.getNextVisitId("i", visitCenterId);
      opToIpVisitBean.set("patient_id", ipVisitId);
      setIPVisitDate(billDao, opToIpVisitBean, billNos, billActions);
      // opToIpVisitBean.set("reg_date", DateUtil.getCurrentDate());
      // opToIpVisitBean.set("reg_time", DateUtil.getCurrentTime());
      opToIpVisitBean.set("user_name", username);
      opToIpVisitBean.set("status", "A");
      opToIpVisitBean.set("revisit", "N");
      opToIpVisitBean.set("visit_type", "i");
      opToIpVisitBean.set("original_visit_id", opVisitId);

      String deptId = request.getParameter("dept_name");
      if (deptId != null && !deptId.equals(opToIpVisitBean.get("dept_name"))) {
        opToIpVisitBean.set("unit_id", null);
      }
      String doctor = request.getParameter("doctor");
      String bedward = request.getParameter("ward_id");

      String orgId = request.getParameter("org_id");
      BigDecimal ipCreditLimitAmt = getSanctionedIpCreditLimitAmount(
          (String) opToIpVisitBean.get("mr_no"), orgId, bedtype, allocateBedAtRegPref);
      opToIpVisitBean.set("admitted_dept", deptId);
      opToIpVisitBean.set("dept_name", deptId);
      opToIpVisitBean.set("doctor", doctor);
      opToIpVisitBean.set("bed_type", bedtype);
      opToIpVisitBean.set("ward_id", bedward);
      opToIpVisitBean.set("org_id", orgId);
      opToIpVisitBean.set("main_visit_id", ipVisitId);
      opToIpVisitBean.set("op_type", "M");
      opToIpVisitBean.set("use_drg", "N");
      opToIpVisitBean.set("drg_code", "");
      opToIpVisitBean.set("use_perdiem", "N");
      opToIpVisitBean.set("per_diem_code", "");
      opToIpVisitBean.set("patient_discharge_status", "N");
      opToIpVisitBean.set("discharge_flag", "");
      opToIpVisitBean.set("discharge_finalized_user", "");
      opToIpVisitBean.set("ip_credit_limit_amount", ipCreditLimitAmt);

      opToIpVisitBean.set("primary_case_rate_id", null);
      opToIpVisitBean.set("secondary_case_rate_id", null);

      nullify(opToIpVisitBean, nullifyFields);

      opToIpVisitBean.set("encounter_start_type", null);
      opToIpVisitBean.set("encounter_end_type", null);

      /* Default Encounter Start type and Encounter End type for IP */
      opToIpVisitBean.set("encounter_start_type",
          (regPrefs.getDefault_ip_encounter_start_type() != null
              && !regPrefs.getDefault_ip_encounter_start_type().equals(""))
                  ? new Integer(regPrefs.getDefault_ip_encounter_start_type())
                  : null);
      opToIpVisitBean.set("encounter_end_type",
          (regPrefs.getDefault_ip_encounter_end_type() != null
              && !regPrefs.getDefault_ip_encounter_end_type().equals(""))
                  ? new Integer(regPrefs.getDefault_ip_encounter_end_type())
                  : null);

      boolean isPriTpaValidForIp = EditVisitDetailsDAO.isTpaValidForIp(opVisitId,
          (String) opToIpVisitBean.get("primary_sponsor_id"));
      boolean isSecTpaValidForIp = EditVisitDetailsDAO.isTpaValidForIp(opVisitId,
          (String) opToIpVisitBean.get("secondary_sponsor_id"));
      boolean isTpaValidForIp = isPriTpaValidForIp || isSecTpaValidForIp;
      if (!isTpaValidForIp) {
        disconnectTPA(opToIpVisitBean);
      }

      String visitId = VisitDetailsDAO.getPatientLatestVisitId(opToIpVisitBean
          .get("mr_no").toString(), true, "i");
      if (visitId != null) {
        log4jLogger.error("Error while inactivating OP visit, active IP Visit exists...");
        flash.error("MR NO : " + opToIpVisitBean.get("mr_no") + " has active IP Visit");
        return redirect;
      }

      /*
       * Deactivate the old visit (needs to be done first, so that patient_details is
       * updated correctly with the visit_id and previous_visit_id).
       */
      visitDetailsdao.updateVisitStatus(con, opVisitId, "I", "Converted to " + ipVisitId, username);
      // visitdao.updateInsuranceStatus(con, opVisitId, 0);

      /*
       * Create the new visit and a bill later for that visit.
       */
      boolean success = visitDetailsdao.insert(con, opToIpVisitBean);

      List<BasicDynaBean> insurancePlanList = insPlanDAO.listAll(null, "patient_id", opVisitId,
          "priority");
      List<BasicDynaBean> ipVisitsInsPlanList = new ArrayList<BasicDynaBean>();
      if (isTpaValidForIp) {
        if (isPriTpaValidForIp && isSecTpaValidForIp) {
          for (BasicDynaBean bean : insurancePlanList) {
            setIPInsuranceVisitPlanLimits(con, bean);
            bean.set("patient_insurance_plans_id", insPlanDAO.getNextSequence());
            bean.set("patient_id", ipVisitId);
            insPlanDAO.insert(con, bean);
            List<BasicDynaBean> patInsPlanDetailsBeans = setIPInsurancePlanDetails(con, bean,
                ipVisitId);
            patInsPlanDetailsDAO.insertAll(con, patInsPlanDetailsBeans);
            ipVisitsInsPlanList.add(bean);
          }
        } else if (isPriTpaValidForIp) {
          Map<String, Object> keys = new HashMap<String, Object>();
          keys.put("patient_id", opVisitId);
          keys.put("priority", 1);
          BasicDynaBean bean = insPlanDAO.findByKey(keys);
          List<BasicDynaBean> patInsPlanDetailsBeans = setIPInsurancePlanDetails(con, bean,
              ipVisitId);
          if (null != bean) {
            setIPInsuranceVisitPlanLimits(con, bean);
            bean.set("patient_insurance_plans_id", insPlanDAO.getNextSequence());
            bean.set("patient_id", ipVisitId);
            insPlanDAO.insert(con, bean);
            patInsPlanDetailsDAO.insertAll(con, patInsPlanDetailsBeans);
            ipVisitsInsPlanList.add(bean);
          }
        } else if (isSecTpaValidForIp) {
          Map<String, Object> keys = new HashMap<String, Object>();
          keys.put("patient_id", opVisitId);
          keys.put("priority", 2);
          BasicDynaBean bean = insPlanDAO.findByKey(keys);
          List<BasicDynaBean> patInsPlanDetailsBeans = setIPInsurancePlanDetails(con, bean,
              ipVisitId);
          if (null != bean) {
            setIPInsuranceVisitPlanLimits(con, bean);
            bean.set("patient_insurance_plans_id", insPlanDAO.getNextSequence());
            bean.set("patient_id", ipVisitId);
            insPlanDAO.insert(con, bean);
            patInsPlanDetailsDAO.insertAll(con, patInsPlanDetailsBeans);
            ipVisitsInsPlanList.add(bean);
          }
        }
      }

      int[] planIds = ipVisitsInsPlanList.size() > 0 ? new int[ipVisitsInsPlanList.size()] : null;
      int planIdIdx = 0;
      for (BasicDynaBean bean : ipVisitsInsPlanList) {
        if (null != bean.get("plan_id")) {
          int planId = (Integer) bean.get("plan_id");
          BasicDynaBean planBean = insurancePlanMainDao.findByKey(con, "plan_id", planId);
          String ipApplicable = (String) planBean.get("ip_applicable");
          if (ipApplicable.equals("Y")) {
            planIds[planIdIdx++] = (Integer) bean.get("plan_id");
          }
        }
      }
      if (success) {
        success = new IPCareDAO().insertVisitCareDeatils(con, opToIpVisitBean);
      }
      if (!success) {
        log4jLogger.error("Error while updating patient registration while OP to IP conversion...");
        flash.error("Updation of patient registration failed...");
        return redirect;
      }

      Bill bill = new Bill();
      boolean isPriSponsorExisits = opToIpVisitBean.get("primary_sponsor_id") != null
          && !opToIpVisitBean.get("primary_sponsor_id").equals("");
      boolean isSecSponsorExisits = opToIpVisitBean.get("secondary_sponsor_id") != null
          && !opToIpVisitBean.get("secondary_sponsor_id").equals("");
      bill.setIs_tpa((isPriSponsorExisits || isSecSponsorExisits) && isTpaValidForIp);
      String err = EditVisitDetailsDAO.createBillAndAddCharge(con, bill, ipVisitId, orgId, bedtype,
          username, bill.getIs_tpa(), planIds, (Integer) opToIpVisitBean.get("patient_category_id"),
          DateUtil.parseTimestamp(
              DataBaseUtil.dateFormatter.format(opToIpVisitBean.get("reg_date")),
              opToIpVisitBean.get("reg_time").toString()));

      if (err != null) {
        log4jLogger.error("Error while creating bill...");
        flash.error("Bill creation failed...");
        return redirect;
      }

      newBillNo = bill.getBillNo();

      /*
       * Take specific action on each bill: Possible actions are: - (L) Leave
       * connected to old visit, ie, do nothing. - (I) Keep the bill but connect to
       * the new visit - (C) Copy charges to the new bill that was created, and close
       * the old one
       */
      if (billNos != null && billNos.length > 0) {
        for (int i = 0; i < billNos.length; i++) {
          String billNo = billNos[i];
          String action = billActions[i];

          if (action.equals("I")) {
            // Include all bills into new claim id
            // billDao.updateBillClaim(billNo, bill.getClaim_id());
            // Move the OP bill to IP visit only if connect to the new visit
            billDao.updateVisitId(billNo, ipVisitId, "i");
          }

          if (action.equals("I") || action.equals("C")) {

            // move all the orders to the new visit
            LaboratoryDAO.updateVisitId(con, billNo, opVisitId, ipVisitId);
            new ServicesDAO().updateVisitId(con, billNo, ipVisitId);
            /*
             * Pendingprescription table is updated only when mod_pat_pending_prescription
             * module is enabled
             */
            boolean modPatPendingPres = modulesActivatedService
                .isModuleActivated("mod_pat_pending_prescription");
            if (modPatPendingPres) {
              pendingPrescriptionsService.updateOpToIpPrescriptions(billNo, ipVisitId);
            }
            OrderDAO.updatePackageVisitId(con, billNo, ipVisitId);
            OrderDAO.updateOtherServicesVisitId(con, billNo, ipVisitId);
            OrderDAO.updateEquipmentVisitId(con, billNo, ipVisitId);
            OrderDAO.updateOperationVisitId(con, billNo, ipVisitId);

            Boolean isConsultationMoved = new DoctorConsultationDAO()
                .updateVisitId(con, billNo, ipVisitId);
            if (isConsultationMoved) {
              MRDDiagnosisDAO.updateVisitId(con, opVisitId, ipVisitId);
              SecondaryComplaintDAO.updateVisitId(con, opVisitId, ipVisitId);
              PatientSectionDetailsDAO.updateVisitId(con, opVisitId, ipVisitId);
              SectionFormService sectionFormService = ApplicationContextProvider
                  .getBean(SectionFormService.class);
              sectionFormService.updateFormTypeOnConsultationTypeChange(opVisitId, "Form_CONS",
                  "Form_OP_FOLLOW_UP_CONS");
            }

            // bill_activity_charge may have MLREG references to this visit
            new BillActivityChargeDAO(con).updateActivityId(billNo, ipVisitId);
            if (!isTpaValidForIp) {
              EditVisitDetailsDAO.updateBillChargesForPolicy(con, ipVisitId, false, billNo,
                  planIds);
            }

          }
          Bill billBo = new BillBO().getBill(billNo);

          if (billActions[i].equals("C")) {
            ReceiptRelatedDAO recDao = new ReceiptRelatedDAO(con);
            
            // Reduce the deposit setoff to 0
            BigDecimal totalSetOff = recDao.getBillSetoff(billNo);
            allocationService.reduceDepositSetoffAmount(billNo, DepositType.GENERAL, totalSetOff);
            
            // Move all the charges to the new bill.
            chDao.updateBillNo(billNo, newBillNo);

            // While copying pharmacy charges, update the sale bill (or) return bill with
            // new bill
            billDao.updatePharmacyBill(billNo, newBillNo);
            
            // Update bill receipts count as zero and receipt totals as zero
            recDao.updateBillReceiptTotals(billNo);

            // Move all the receipts to the new bill.
            recDao.updateReceiptBillNo(billNo, newBillNo);

            // close the old bill
            java.sql.Timestamp finalizedDate = DateUtil.getCurrentTimestamp();
            java.sql.Timestamp closedDate = DateUtil.getCurrentTimestamp();
            billDao.updatePrimaryBill(billNo, "N");

            String finalizedBy = billBo.getFinalizedBy();

            // Set the finalized by when the status is closed
            // (And) the Bill is a Credit bill or Bill now with TPA (And) the finalized by
            // is empty.
            if ((bill.getBillType().equals(Bill.BILL_TYPE_CREDIT)
                || (billBo.getBillType().equals(Bill.BILL_TYPE_PREPAID) && billBo.getIs_tpa()))
                && (finalizedBy == null || finalizedBy.equals(""))) {
              finalizedBy = username;
            }

            billDao.updateBillStatus(username, billNo, "C", "P", "Y", finalizedDate, closedDate,
                username, finalizedBy, finalizedDate);

            String primaryClaimStatus = null;
            if (opToIpVisitBean.get("primary_sponsor_id") != null
                && !((String) opToIpVisitBean.get("primary_sponsor_id")).equals("")) {
              primaryClaimStatus = Bill.BILL_CLAIM_RECEIVED;
            }
            String secondaryClaimStatus = null;
            if (opToIpVisitBean.get("secondary_sponsor_id") != null
                && !((String) opToIpVisitBean.get("secondary_sponsor_id")).equals("")) {
              secondaryClaimStatus = Bill.BILL_CLAIM_RECEIVED;
            }

            billDao.updateBillClaimStatus(billNo, primaryClaimStatus, secondaryClaimStatus);

            // Update claim if all bills are closed
            List<String> claims = new ArrayList<String>();
            claims.add(bill.getClaim_id());
          }

          String billStatus = billBo.getStatus();

          if (billActions[i].equals("I")) {
            if (!billStatus.equals("F") && !billStatus.equals("C")) {
              if (null != planIds) {
                billChgClaimDAO.changesToBillChargeClaim(con, billNo, ipVisitId, planIds, "i");
              }
            } else {
              if (null != planIds) {
                for (int planId : planIds) {
                  if (planId > 0) {
                    billChgClaimDAO.updateClaimAndVisitId(con, billNo, ipVisitId, opVisitId, planId,
                        billBo.getAccount_group());
                  }
                }
              }
            }
          } else if (billActions[i].equals("C")) {
            if (null != planIds) {
              billChgClaimDAO.delete(con, "bill_no", billNo);
              billChgClaimDAO.changesToBillChargeClaim(con, newBillNo, ipVisitId, planIds, "i");
            }
          }

          if (!billActions[i].equals("L")) { // not applicable for op bills
            Bill billBean = billDao.getBill(billNo);
            billBean.setOpenDate(DateUtil.parseTimestamp(
                DataBaseUtil.dateFormatter.format(opToIpVisitBean.get("reg_date")),
                opToIpVisitBean.get("reg_time").toString()));

            if (!billBean.getVisitType().equals("o")) {
              billDao.updateBill(billBean);
            }
          }
        }
        LaboratoryDAO.splitReportIds(con, opVisitId, ipVisitId);
      }

      // Updating the ip visitid in scheduler_appointment table.

      String schPriorAuthId = null;
      int schPriorAuthModeId = 0;
      BasicDynaBean patientBean = null;
      if (appointId != null && !appointId.equals("")) {
        BasicDynaBean apptBean = schedulerAppointmentsDao.findByKey("appointment_id",
            Integer.parseInt(appointId));
        String scheduleName = (String) apptBean.get("res_sch_name");

        OrderBO orderBo = new OrderBO();
        if (category.equals("OPE") && scheduleName != null) {
          orderBo.setBillInfo(con, ipVisitId, newBillNo, false, username);
          if (apptBean != null) {
            schPriorAuthId = (String) apptBean.get("scheduler_prior_auth_no");
            schPriorAuthModeId = (Integer) apptBean.get("scheduler_prior_auth_mode_id");
          }
        }

        String mrno = (String) opToIpVisitBean.get("mr_no");
        BasicDynaBean appbean = schedulerAppointmentsDao.findByKey("appointment_id",
            new Integer(appointId));
        boolean conduction = ResourceDAO.getConductionForTestOrServiceOrOperation(category,
            (String) appbean.get("res_sch_name"));
        patientBean = PatientDetailsDAO.getPatientGeneralDetailsBean(mrno);

        ResourceDAO resdao = new ResourceDAO(con);
        allSuccess = resdao.updateStatus(ResourceDTO.APPT_ARRIVED_STATUS,
            Integer.parseInt(appointId), username);

        if (allSuccess) {
          allSuccess = ResourceBO.updateScheduler(con, Integer.parseInt(appointId), mrno, ipVisitId,
              patientBean, (String) appbean.get("complaint"), username,
              category.equals("DOC") ? (Integer) appbean.get("consultation_type_id") : 0, null,
              (String) appbean.get("presc_doc_id"), "Reg");
        }

        if (allSuccess) {
          resdao.updateVisitId(ipVisitId, Integer.parseInt(appointId));
        }

        // Ordering the operation for the patient.

        BasicDynaBean operationBean = null;
        List<BasicDynaBean> newOrders = new ArrayList<BasicDynaBean>();
        List newPreAuths = new ArrayList();
        List newPreAuthModes = new ArrayList();
        List firstOfCategoryList = new ArrayList();
        List condDoctrsList = new ArrayList();

        if (category.equals("OPE") && scheduleName != null) {
          if (!(modAdvanceOTActive.equals("Y"))) {
            operationBean = ResourceBO.setOperationOrderDetails(con, Integer.parseInt(appointId),
                ipVisitId, operationBean);
          }
        }

        if (operationBean != null) {
          newOrders.add(operationBean);
          newPreAuths
              .add((schPriorAuthId != null && !schPriorAuthId.equals("")) ? schPriorAuthId : "");
          newPreAuthModes.add(schPriorAuthModeId != 0 ? schPriorAuthModeId : 1);
          condDoctrsList.add("");
        }

        orderBo.orderItems(con, newOrders, newPreAuths, newPreAuthModes, firstOfCategoryList,
            condDoctrsList, null, Integer.parseInt(appointId));

        Map columndata = new HashMap();
        columndata.put("scheduler_prior_auth_no", schPriorAuthId);
        columndata.put("scheduler_prior_auth_mode_id", schPriorAuthModeId);
        Map keys = new HashMap();
        keys.put("appointment_id", Integer.parseInt(appointId));
        allSuccess = schedulerAppointmentsDao.update(con, columndata, keys) > 0;

        if (category.equals("OPE") && modAdvanceOTActive.equals("Y")) {
          errorMsg = new OperationDetailsBO().saveSurgeryAppointmnetToOpertionDetails(con,
              Integer.parseInt(appointId), orgId);
          if (errorMsg != null) {
            errorMsg = getResources(request).getMessage(errorMsg);
            flash.error(errorMsg);
            redirect = new ActionRedirect(mapping.findForward("opipconversionerrorredirect"));
            redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
            return redirect;
          } else {
            allSuccess = true;
          }
        }

        if (modAdvanceOTActive.equals("Y") && category.equals("OPE")) {
          allSuccess = resdao.updateStatus(ResourceDTO.APPT_ARRIVED_STATUS,
              Integer.parseInt(appointId), username);
        } else {
          if (allSuccess && !conduction && scheduleName != null) {

            allSuccess = ResourceBO
                .updateTestOrServiceOrOperationStatus(Integer.parseInt(appointId), category);

            allSuccess = resdao.updateStatus(ResourceDTO.APPT_COMPLETED_STATUS,
                Integer.parseInt(appointId), username);
          } else {
            allSuccess = resdao.updateStatus(ResourceDTO.APPT_ARRIVED_STATUS,
                Integer.parseInt(appointId), username);
          }
        }

      }

      visitId = VisitDetailsDAO.getPatientLatestVisitId(opToIpVisitBean.get("mr_no")
        .toString(), true, "i");
      if (visitId != null) {
        log4jLogger.error("Error while inactivating OP visit, active IP Visit exists...");
        flash.error("MR NO : " + opToIpVisitBean.get("mr_no") + " has active IP Visit");
        return redirect;
      }

      allSuccess = true;

    } catch (Exception ex) {
      allSuccess = false;
      throw ex;
    } finally {
      if (allSuccess) {
        interfaceEventMappingService.opToIpConvertionEvent(ipVisitId);
        redirect.addParameter("patient_id", ipVisitId);
      } else {
        redirect.addParameter("patient_id", opVisitId);
      }
      DataBaseUtil.commitClose(con, allSuccess);

      boolean useDrg = VisitDetailsDAO.visitUsesDRG(opVisitId);
      if (allSuccess) {
        if (billNos != null && billNos.length > 0) {
          for (int i = 0; i < billNos.length; i++) {
            String billNo = billNos[i];
            if (billNo != null && !(billNo).equals("") && !billActions[i].equals("C")) {
              BillDAO.resetTotalsOrReProcess(billNo);
            }
            if (useDrg) {
              DRGCalculator drcCalc = new DRGCalculator();
              if (billActions[i].equals("C")) {
                drcCalc.removeDRG(newBillNo);
              } else if (billActions[i].equals("I")) {
                drcCalc.removeDRG(billNo);
              }
            }
          }
        }
        // New bill's deduction and claim amount should be reset newly.
        if (newBillNo != null && !(newBillNo).equals("")) {
          BillDAO.setDeductionAndSponsorClaimTotals(newBillNo);
        }

        // new insurance-3.0 sponsor calculations
        /*
         * For OP visit we no need to unlock the charges - i.e. leave as OP option. For
         * IP visit we have to unlock all charges and calculate sponsor amounts i.e.
         * connect to IP or copy charges option is selected.
         */
        new SponsorBO().recalculateSponsorAmount(opVisitId);

        // unlock all the charges of IP visit.
        SponsorDAO spnDAO = new SponsorDAO();
        spnDAO.unlockVisitBillsCharges(ipVisitId);
        spnDAO.unlockVisitSaleItems(ipVisitId);
        spnDAO.includeBillChargesInClaimCalc(ipVisitId);
        new SponsorBO().recalculateSponsorAmount(ipVisitId);
        spnDAO.setIssueReturnsClaimAmountTOZero(ipVisitId);
        spnDAO.insertOrUpdateBillChargeTaxesForSales(ipVisitId);
        spnDAO.lockVisitSaleItems(ipVisitId);
        spnDAO.updateSalesBillCharges(ipVisitId);
        spnDAO.updateTaxDetails(ipVisitId);
      }

      // This code needs to go into some common component so that we dont duplicate
      // this
      String visitType = (String) opToIpVisitBean.get("visit_type");
      String patientId = (String) opToIpVisitBean.get("patient_id");
      if (allSuccess && // registration successful
          null != visitType && visitType.equalsIgnoreCase("i") // IP registration
          && null != patientId
          && MessageUtil.allowMessageNotification(request, "scheduler_message_send")) {
        log4jLogger
            .debug("Admission successful : Sending SMS to admitting doctor and referal doctor");
        sendIPAdmissionSMS(patientId);
      }
    }
    
    // Update the bill totals
    allocationService.updateBillTotal(newBillNo);

    // schedule accounting for OP,IP visit bills
    BillBO billBo = new BillBO();
    List<BasicDynaBean> opVisitBillsList = billBo.getVisitFinalizedAndClosedBills(opVisitId);
    List<BasicDynaBean> ipVisitBillsList = billBo.getVisitFinalizedAndClosedBills(ipVisitId);
    accountingJobScheduler.scheduleAccountingForBills(opVisitBillsList);
    accountingJobScheduler.scheduleAccountingForBills(ipVisitBillsList);

    return redirect;
  }

  /**
   * Gets the sanctioned ip credit limit amount.
   *
   * @param mrNo
   *          the mr no
   * @param orgId
   *          the orgId
   * @param bedtype
   *          the bedtype
   * @param allocateBedAtRegPref
   *          the allocate bed at reg pref
   * @return the sanctioned ip credit limit amount
   * @throws SQLException
   *           the SQL exception
   */
  private BigDecimal getSanctionedIpCreditLimitAmount(String mrNo, String orgId, String bedtype,
      String allocateBedAtRegPref) throws SQLException {
    List<BasicDynaBean> bedChargesList = new ArrayList<BasicDynaBean>();
    if (allocateBedAtRegPref.equals("Y")) {
      bedChargesList = new BedMasterDAO().getAllBedDetails(orgId, bedtype);
    } else {
      bedChargesList = new BedMasterDAO().getBillingBedDetails(orgId, bedtype);
    }

    BigDecimal ipCreditLimitAmt = BigDecimal.ZERO;
    if (bedChargesList != null && bedChargesList.size() > 0) {
      BigDecimal initialPayment = (BigDecimal) bedChargesList.get(0).get("initial_payment");
      BigDecimal initialPaymentDisc = (BigDecimal) bedChargesList.get(0)
          .get("initial_payment_discount");
      ipCreditLimitAmt = (initialPayment).subtract(initialPaymentDisc);
    }

    return ipCreditLimitAmt;
  }

  /**
   * Sets the IP insurance visit plan limits.
   *
   * @param con
   *          the con
   * @param bean
   *          the bean
   * @throws SQLException
   *           the SQL exception
   */
  private void setIPInsuranceVisitPlanLimits(Connection con, BasicDynaBean bean)
      throws SQLException {
    BasicDynaBean planbean = insurancePlanMainDao.findByKey(con, "plan_id", bean.get("plan_id"));
    if (null != planbean && !planbean.equals("")) {
      bean.set("plan_limit", planbean.get("ip_plan_limit"));
      bean.set("visit_limit", planbean.get("ip_visit_limit"));
      bean.set("visit_deductible", planbean.get("ip_visit_deductible"));
      bean.set("visit_copay_percentage", planbean.get("ip_copay_percent"));
      bean.set("visit_max_copay_percentage", planbean.get("ip_visit_copay_limit"));
      bean.set("visit_per_day_limit", planbean.get("ip_per_day_limit"));
    }
  }

  /**
   * Sets the IP insurance plan details.
   *
   * @param con
   *          the con
   * @param planBean
   *          the plan bean
   * @param ipVisitId
   *          the ip visit id
   * @return the list
   * @throws SQLException
   *           the SQL exception
   */
  private List<BasicDynaBean> setIPInsurancePlanDetails(Connection con, BasicDynaBean planBean,
      String ipVisitId) throws SQLException {
    List<BasicDynaBean> patInsurancePlanDetailsList = new ArrayList<BasicDynaBean>();
    Map filterMap = new HashMap();
    filterMap.put("patient_type", "i");
    filterMap.put("plan_id", planBean.get("plan_id"));
    List<BasicDynaBean> insurancePlanDetailsList = insPlanDetailsDAO.listAll(null, filterMap, null);
    if (null != insurancePlanDetailsList && !insurancePlanDetailsList.isEmpty()) {
      for (BasicDynaBean detBean : insurancePlanDetailsList) {
        BasicDynaBean bean = patInsPlanDetailsDAO.getBean();
        bean.set("patient_type", "i");
        bean.set("visit_id", ipVisitId);
        bean.set("plan_id", detBean.get("plan_id"));
        bean.set("insurance_category_id", detBean.get("insurance_category_id"));
        bean.set("patient_amount", detBean.get("patient_amount"));
        bean.set("patient_percent", detBean.get("patient_percent"));
        bean.set("patient_amount_cap", detBean.get("patient_amount_cap"));
        bean.set("per_treatment_limit", detBean.get("per_treatment_limit"));
        bean.set("patient_amount_per_category", detBean.get("patient_amount_per_category"));
        patInsurancePlanDetailsList.add(bean);
      }
    }
    return patInsurancePlanDetailsList;
  }

  /**
   * Send IP admission SMS.
   *
   * @param patientId
   *          the patient id
   * @throws SQLException
   *           the SQL exception
   * @throws ParseException
   *           the parse exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  private void sendIPAdmissionSMS(String patientId)
      throws SQLException, ParseException, IOException {
    
    Map<String, Object> adtData = new HashMap<>();
    adtData.put("patient_id", patientId);
    adtData.put("mr_no", null);
    ADTService adtService = (ADTService) ApplicationContextProvider.getApplicationContext()
        .getBean("adtService");
    adtService.createAndSendADTMessage("ADT_04", adtData);
    
    MessageManager mgr = new MessageManager();
    Map<String, String> admissionData = getIPAdmissionData(patientId);

    String refDocMobileNo = admissionData.get("referal_doctor_mobile");
    String admitDocMobileNo = admissionData.get("doctor_mobile");

    String admAndRefDocMobileno = null;

    if (null != admitDocMobileNo && !admitDocMobileNo.trim().equals("")) {
      admAndRefDocMobileno = admitDocMobileNo.trim();
    }

    if (null != refDocMobileNo && !refDocMobileNo.trim().equals("")) {
      if (admAndRefDocMobileno == null) { // no mobile number for admitting doctor
        admAndRefDocMobileno = refDocMobileNo.trim();
      } else {
        admAndRefDocMobileno = admAndRefDocMobileno.concat(",").concat(refDocMobileNo.trim());
      }
    }

    // send admitting or referral doctor SMS.
    if (null != admAndRefDocMobileno && !admAndRefDocMobileno.trim().equals("")) {
      admissionData.put("recipient_mobile", admAndRefDocMobileno.trim());
      mgr.processEvent("patient_admitted", admissionData);
    } else {
      log4jLogger
          .info("Admitting and Referral Doctor mobile # not available, skipping SMS on admission");
    }

    // in OP to IP conversion always revise SMS type
    // Send Revise SMS to Patient

    String patientMobileNo = admissionData.get("patient_phone");
    if (null != patientMobileNo && !patientMobileNo.trim().equals("")) {
      admissionData.put("recipient_mobile", patientMobileNo.trim());
      admissionData.put("lang_code",
          (String) PatientDetailsDAO.getContactPreference((String) admissionData.get("mr_no")));
      mgr.processEvent("patient_on_ip_patient_revisit", admissionData);
    } else {
      log4jLogger.info("Patient mobile # not available, skipping SMS on admission");
    }
    // Send Revise SMS to Patient Party
    String patientPartyMobileNo = admissionData.get("next_of_kin_contact");
    if (null != patientPartyMobileNo && !patientPartyMobileNo.trim().equals("")) {
      admissionData.put("recipient_mobile", patientPartyMobileNo.trim());
      admissionData.put("lang_code",
          (String) PatientDetailsDAO.getContactPreference((String) admissionData.get("mr_no")));
      mgr.processEvent("family_on_ip_patient_revisit", admissionData);
    } else {
      log4jLogger.info("Patient Party mobile # not available, skipping SMS on admission");
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
    String referalDocMobile = DataBaseUtil.getStringValueFromDb(referalDoctorQuery,
        referalDoctorId);

    admissionData.put("mr_no", (String) bean.get("mr_no"));
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
    admissionData.put("next_of_kin_contact", (String) bean.get("patient_care_oftext"));
    admissionData.put("next_of_kin_name", (String) bean.get("relation"));
    admissionData.put("receipient_id__", (String) bean.get("mr_no"));
    admissionData.put("receipient_type__", "PATIENT");
    admissionData.put("doctor_name", (String) bean.get("doctor_name"));
    admissionData.put("referal_doctor", (String) bean.get("refdoctorname"));
    admissionData.put("doctor_mobile", (String) bean.get("doctor_mobile"));
    admissionData.put("referal_doctor_mobile", referalDocMobile);
    admissionData.put("admitting_doctor_id__", (String) bean.get("doctor"));
    admissionData.put("referal_doctor_id__", (String) bean.get("reference_docto_id"));
    admissionData.put("salutation_name", (String) bean.get("salutation"));
    admissionData.put("doctor_specialization", (String) bean.get("specialization"));
    admissionData.put("visit_id", patientId);
    return admissionData;
  }

  /**
   * Sets the IP visit date.
   *
   * @param billdao
   *          the billdao
   * @param opToIpVisitBean
   *          the op to ip visit bean
   * @param billNos
   *          the bill nos
   * @param billActions
   *          the bill actions
   * @throws SQLException
   *           the SQL exception
   */
  public void setIPVisitDate(BillDAO billdao, BasicDynaBean opToIpVisitBean, String[] billNos,
      String[] billActions) throws SQLException {
    Bill opbill = null;
    boolean caryFrwdVisit = false;

    if (billNos != null && billNos.length > 0) {
      for (int i = 0; i < billNos.length; i++) {
        String billNo = billNos[i];
        String action = billActions[i];
        opbill = billdao.getBill(billNo);

        // In case any of the visit bill is chosen to I/C option that means that
        // ip visit needs op visit data as well.Such cases can have reg date,reg time of
        // new ip visit
        // as First such bills(among I/C chosen bills)open date,time.

        if (action.equals("I") || action.equals("C")) {
          opToIpVisitBean.set("reg_date", DateUtil.getDatePart(opbill.getOpenDateTime()));
          opToIpVisitBean.set("reg_time", DateUtil.getTimePart(opbill.getOpenDateTime()));
          caryFrwdVisit = true;
          break;
        }
      }

    }
    if (!caryFrwdVisit || billNos == null) {
      opToIpVisitBean.set("reg_date", DateUtil.getCurrentDate());
      opToIpVisitBean.set("reg_time", DateUtil.getCurrentTime());
    }

  }

  /**
   * Nullify.
   *
   * @param bean
   *          the bean
   * @param fields
   *          the fields
   */
  private void nullify(BasicDynaBean bean, String[] fields) {
    for (String field : fields) {
      bean.set(field, null);
    }
  }

  /**
   * Reset doctor exclusion charges.
   *
   * @param chargeDAO
   *          the charge DAO
   * @param billNos
   *          the bill nos
   * @param billActions
   *          the bill actions
   * @throws SQLException
   *           the SQL exception
   */
  private void resetDoctorExclusionCharges(ChargeDAO chargeDAO, String[] billNos,
      String[] billActions) throws SQLException {

    if ( billActions != null && billActions.length != 0 && billNos != null) {
      for (int i = 0; i < billNos.length; i++) {
        if (("C").equals(billActions[i]) || ("I").equals(billActions[i])) {
          List<ChargeDTO> billCharge = chargeDAO.getBillCharges(billNos[i]);
          for (ChargeDTO bc : billCharge) {
            bc.setItemExcludedFromDoctor(null);
            bc.setDoctorExclusionRemarks(null);
            chargeDAO.updateExclusionFields(bc.getChargeId(), bc);
          }
        }
      }
    }
  }
}
