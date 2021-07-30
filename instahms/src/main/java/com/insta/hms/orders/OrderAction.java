package com.insta.hms.orders;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.bob.hms.common.MimeTypeDetector;
import com.bob.hms.common.Preferences;
import com.bob.hms.common.RequestContext;
import com.insta.hms.Registration.PatientInsurancePlanDAO;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.billing.Bill;
import com.insta.hms.billing.BillBO;
import com.insta.hms.billing.BillDAO;
import com.insta.hms.billing.ClaimSubmissionDAO;
import com.insta.hms.billing.EditInsuranceHelper;
import com.insta.hms.common.AppInit;
import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.HtmlConverter;
import com.insta.hms.core.billing.AllocationService;
import com.insta.hms.genericdocuments.GenericDocumentsFields;
import com.insta.hms.insurance.SponsorBO;
import com.insta.hms.ipservices.DashBoardDAO;
import com.insta.hms.ipservices.PrescriptionViewDAO;
import com.insta.hms.master.ConsultationTypes.ConsultationTypesDAO;
import com.insta.hms.master.DoctorMaster.DoctorMasterDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDTO;
import com.insta.hms.master.PlanMaster.PlanMasterDAO;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;
import com.insta.hms.master.PrintTemplates.PrintTemplate;
import com.insta.hms.master.PrintTemplates.PrintTemplatesDAO;
import com.insta.hms.master.RegistrationPreferences.RegistrationPreferencesDAO;
import com.insta.hms.outpatient.DentalChartHelperDAO;
import com.insta.hms.outpatient.PatientPrescriptionDAO;
import com.insta.hms.outpatient.PrescribeDAO;
import com.insta.hms.outpatient.ToothImageDetails;
import com.insta.hms.wardactivities.PatientActivitiesDAO;

import flexjson.JSONSerializer;
import freemarker.template.Configuration;
import freemarker.template.Template;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.DynaClass;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;
import org.apache.struts.upload.FormFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

// TODO: Auto-generated Javadoc
/**
 * The Class OrderAction.
 */
public class OrderAction extends DispatchAction {

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(OrderAction.class);

  /** The order dao. */
  private static OrderDAO orderDao = new OrderDAO();

  private final AllocationService allocationService = (AllocationService) ApplicationContextProvider
      .getApplicationContext().getBean("allocationService");
  
  /**
   * Gets the orders.
   *
   * @param mapping  the mapping
   * @param form     the form
   * @param request  the request
   * @param response the response
   * @return the orders
   * @throws Exception the exception
   */
  public ActionForward getOrders(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws Exception {

    String filter = mapping.getProperty("category") == null ? "" : mapping.getProperty("category");
    request.setAttribute("filter", filter);
    String visitId = request.getParameter("patient_id");

    BasicDynaBean mst = (BasicDynaBean) new GenericDAO("master_timestamp").getRecord();
    request.setAttribute("masterTimeStamp", mst.get("master_count"));

    if (visitId == null) {
      String billNo = request.getParameter("billNo");
      BasicDynaBean bill = (new GenericDAO("bill").findByKey("bill_no", billNo));
      if (bill != null) {
        visitId = (String) bill.get("visit_id");
      }
    }

    if (visitId == null || visitId.equals("")) {
      request.setAttribute("multiPlanExists", false);
      return mapping.findForward("showorders");
    }
    HttpSession session = (HttpSession) request.getSession(false);
    Integer centerId = (Integer) session.getAttribute("centerId");
    Map patientDetails = null;
    if (centerId != 0) {
      patientDetails = VisitDetailsDAO.getPatientVisitDetailsMapByCenter(visitId, centerId);
    } else {
      patientDetails = VisitDetailsDAO.getPatientVisitDetailsMap(visitId);
    }
    if (patientDetails == null) {
      BasicDynaBean patientDetailsBean = VisitDetailsDAO.getDetailsForMrnoOrVisitId(visitId);
      if (patientDetailsBean == null || patientDetailsBean.get("visit_id") == null
          || patientDetailsBean.get("visit_id").equals("")) {
        ActionRedirect redirect = new ActionRedirect(mapping.findForward("showordersRedirect"));
        FlashScope flash = FlashScope.getScope(request);
        flash.put("error", "No patient with id: " + visitId);
        redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
        return redirect;
      } else {

        if (centerId != 0) {
          patientDetails = VisitDetailsDAO.getPatientVisitDetailsMapByCenter(
              (String) patientDetailsBean.get("visit_id"), centerId);
        } else {
          patientDetails = VisitDetailsDAO
              .getPatientVisitDetailsMap((String) patientDetailsBean.get("visit_id"));
        }
        if (patientDetails == null) {
          ActionRedirect redirect = new ActionRedirect(mapping.findForward("showordersRedirect"));
          FlashScope flash = FlashScope.getScope(request);
          flash.put("error", "No patient with id: " + visitId);
          redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
          return redirect;
        }
      }
    }

    visitId = (String) patientDetails.get("patient_id");
    int patCenterId = (Integer) patientDetails.get("center_id");

    JSONSerializer js = new JSONSerializer().exclude("class");

    List doctorsList = DoctorMasterDAO.getDoctorDepartmentsDynaList(patCenterId);
    request.setAttribute("doctorsJSON",
        js.serialize(ConversionUtils.listBeanToListMap(doctorsList)));

    request.setAttribute("chargeHeads", new BillBO().getChargeHeadConstNames());

    request.setAttribute("patient_id", visitId);
    request.setAttribute("genPrefs", GenericPreferencesDAO.getGenericPreferences());

    GenericDAO serviceGroupDao = new GenericDAO("service_groups");
    request.setAttribute("serviceGroups",
        serviceGroupDao.listAll(null, "status", "A", "service_group_name"));
    request.setAttribute("serviceGroupsJSON", js.serialize(ConversionUtils
        .copyListDynaBeansToMap(serviceGroupDao.listAll(null, "status", "A", null))));
    request.setAttribute("servicesSubGroupsJSON",
        js.serialize(ConversionUtils.copyListDynaBeansToMap(new GenericDAO("service_sub_groups")
            .listAll(null, "status", "A", "service_sub_group_name"))));
    request.setAttribute("doctorConsultationTypes",
        js.serialize(ConversionUtils.copyListDynaBeansToMap(
            OrderDAO.getConsultationTypes((String) patientDetails.get("visit_type"), false))));
    request.setAttribute("allDoctorConsultationTypes",
        js.serialize(ConversionUtils.copyListDynaBeansToMap(new ConsultationTypesDAO().listAll())));
    request.setAttribute("regPrefJSON",
        js.serialize(RegistrationPreferencesDAO.getRegistrationPreferences()));

    request.setAttribute("anaeTypesJSON", js.serialize(ConversionUtils.copyListDynaBeansToMap(
        new GenericDAO("anesthesia_type_master").listAll(null, "status", "A", null))));

    Map<String, Object> filterMap = new HashMap<String, Object>();

    // in multi center scheema theatre must belong to visit center.
    if (GenericPreferencesDAO.getGenericPreferences().getMax_centers_inc_default() > 1) {
      filterMap.put("center_id", (Integer) patientDetails.get("center_id"));
    }
    filterMap.put("status", "A");

    request.setAttribute("otlist_applicabletovisitcenter",
        new GenericDAO("theatre_master").listAll(null, filterMap, "theatre_name"));

    if (visitId == null) {
      return mapping.findForward("showorders");
    }

    request.setAttribute("patient", patientDetails);

    List bills = BillDAO.getActiveUnpaidBills(visitId, BillDAO.bill_type.BOTH);
    bills = ConversionUtils.listBeanToListMap(bills);
    request.setAttribute("bills", bills);
    request.setAttribute("billsJSON", js.serialize(bills));
    request.setAttribute("allowBillNowInsurance",
        BillDAO.isBillInsuranceAllowed(Bill.BILL_TYPE_PREPAID, true));
    String visitType = (String) patientDetails.get("visit_type");

    PatientInsurancePlanDAO insPlanDao = new PatientInsurancePlanDAO();
    List<BasicDynaBean> planListBean = insPlanDao.getPlanDetails(visitId);
    request.setAttribute("planList", js.serialize(ConversionUtils.listBeanToListMap(planListBean)));

    boolean multiPlanExists = null != planListBean && planListBean.size() == 2;
    request.setAttribute("multiPlanExists", multiPlanExists);
    int[] planIds = insPlanDao.getPlanIds(visitId);
    boolean multiPlanExisits = null != planIds && planIds.length == 2;
    boolean hasPlanVisitCopayLimit = false;
    if (null != planIds) {
      for (int i = 0; i < planIds.length; i++) {
        hasPlanVisitCopayLimit = new PlanMasterDAO().hasPlanVisitCopayLimit(planIds[i], visitType);
        if (hasPlanVisitCopayLimit) {
          break;
        }
      }
    }

    request.setAttribute("hasPlanVisitCopayLimit", hasPlanVisitCopayLimit);
    request.setAttribute("multiPlanExisits", multiPlanExisits);

    // Visit bills
    String path = request.getContextPath();
    ClaimSubmissionDAO submitdao = new ClaimSubmissionDAO();
    EditInsuranceHelper editInsHelper = new EditInsuranceHelper();
    List<BasicDynaBean> visitTpaBills = editInsHelper.getVisitTPABills(visitId,
        EditInsuranceHelper.BillsRequired.all_bills.toString());
    boolean hasOpenUnpaidPriInsCreditBillForPerdiem = false;
    BasicDynaBean perdiemBillBean = null;
    if (patientDetails.get("use_perdiem").equals("Y")) {
      for (BasicDynaBean billbean : visitTpaBills) {

        // Do not include pharmacy bill now insured bills.
        if (((String) billbean.get("bill_type")).equals(Bill.BILL_TYPE_PREPAID)
            && ((String) billbean.get("restriction_type")).equals(Bill.BILL_RESTRICTION_PHARMACY)) {
          continue;
        }

        perdiemBillBean = billbean;
        if (billbean.get("bill_type").equals(Bill.BILL_TYPE_CREDIT)
            && billbean.get("status").equals(Bill.BILL_STATUS_OPEN)
            && billbean.get("payment_status").equals(Bill.BILL_PAYMENT_UNPAID)
            && billbean.get("is_primary_bill").equals("Y")) {
          hasOpenUnpaidPriInsCreditBillForPerdiem = true;
          break;
        }
      }
      if (!hasOpenUnpaidPriInsCreditBillForPerdiem) {
        if (perdiemBillBean != null) {
          String err = "<b> This patient is a perdiem patient. </br>"
              + " Need an open and unpaid insured credit bill to order. </br> Please ";
          err += ((String) perdiemBillBean.get("bill_type")).equals(Bill.BILL_TYPE_PREPAID)
              ? " convert to bill later, "
              : "";
          err += ((String) perdiemBillBean.get("is_primary_bill")).equals("N")
              ? " convert to primary bill, "
              : "";
          err += !((String) perdiemBillBean.get("status")).equals(Bill.BILL_STATUS_OPEN)
              ? " reopen, "
              : "";
          err += ((String) perdiemBillBean.get("payment_status")).equals(Bill.BILL_PAYMENT_PAID)
              ? " mark as unpaid, "
              : "";
          request.setAttribute("error",
              err + " Bill no. <u>"
                  + submitdao.urlString(path, "bill", (String) perdiemBillBean.get("bill_no"), null)
                  + "</u> to order.</b>");
        } else {
          request.setAttribute("error",
              "<b> This patient is a perdiem patient. </br>"
                  + " There is no insured credit bill to order. </br> "
                  + " Please check and create a new insured credit bill before ordering.</b>");
        }
      }
    } else {
      hasOpenUnpaidPriInsCreditBillForPerdiem = true;
    }
    request.setAttribute("hasOpenUnpaidPriInsCreditBillForPerdiem",
        hasOpenUnpaidPriInsCreditBillForPerdiem);

    List allOrders = null;
    List allOrdersMultiVisitPack = null;
    if (filter == null || filter.equals("")) {
      allOrders = orderDao.getAllOrders(visitId);
    } else if (filter.equals("Laboratory")) {
      allOrders = orderDao.getFilteredOrdersAndDiagPackages(visitId, filter);
    } else if (filter.equals("Radiology")) {
      allOrders = orderDao.getFilteredOrdersAndRadioPackages(visitId, filter);
    } else {
      allOrders = orderDao.getFilteredOrders(visitId, filter);
    }

    if (allOrders == null || allOrders.size() < 1) {
      allOrders = new LinkedList();
    }
    allOrdersMultiVisitPack = orderDao.getAllOrdersForMultiVisitPackage(visitId, filter);
    if (allOrdersMultiVisitPack != null && allOrdersMultiVisitPack.size() > 0) {
      for (int i = 0; i < allOrdersMultiVisitPack.size(); i++) {
        allOrders.add(allOrdersMultiVisitPack.get(i));
      }
    }

    request.setAttribute("allOrders", allOrders);
    request.setAttribute("allOrdersJSON",
        js.serialize(ConversionUtils.copyListDynaBeansToMap(allOrders)));
    request.setAttribute("visitTotalPatientDue", BillDAO.getVisitPatientDue(visitId));

    List<BasicDynaBean> preAuthNoAndModeIdsList = orderDao.getPreAuthNoAndModeIdsList(visitId);

    request.setAttribute("preAuthNoAndModeIdsList",
        ConversionUtils.copyListDynaBeansToMap(preAuthNoAndModeIdsList));

    // prescriptions for auto-filling the order
    String[] prescIds = request.getParameterValues("patient_presc_id");
    if (prescIds != null) {
      request.setAttribute("testPrescriptionsJSON", js.serialize(
          ConversionUtils.copyListDynaBeansToMap(orderDao.getPatientTestPrescriptions(prescIds))));
      request.setAttribute("servicePrescriptionsJSON", js.serialize(ConversionUtils
          .copyListDynaBeansToMap(orderDao.getPatientServicePrescriptions(prescIds))));
      request.setAttribute("operationPrescriptionsJSON", js.serialize(
          ConversionUtils.copyListDynaBeansToMap(orderDao.getOperationPrescriptions(prescIds))));
      request.setAttribute("consultationPrescriptionsJSON", js.serialize(ConversionUtils
          .copyListDynaBeansToMap(orderDao.getPatientConsultationPrescriptions(prescIds))));

      request.setAttribute("from_pending_prescriptions", true);
    } else {
      request.setAttribute("testPrescriptionsJSON", js.serialize(
          ConversionUtils.copyListDynaBeansToMap(orderDao.getPatientTestPrescriptions(visitId))));
      request.setAttribute("servicePrescriptionsJSON", js.serialize(ConversionUtils
          .copyListDynaBeansToMap(orderDao.getPatientServicePrescriptions(visitId))));
      request.setAttribute("operationPrescriptionsJSON", js.serialize(
          ConversionUtils.copyListDynaBeansToMap(orderDao.getOperationPrescriptions(visitId))));
      request.setAttribute("consultationPrescriptionsJSON", js.serialize(ConversionUtils
          .copyListDynaBeansToMap(orderDao.getPatientConsultationPrescriptions(visitId))));
    }
    request.setAttribute("dietPrescriptionsJSON", js.serialize(
        ConversionUtils.copyListDynaBeansToMap(orderDao.getPatientDietPrescriptions(visitId))));

    GenericDAO patRegDao = new GenericDAO("patient_registration");
    BasicDynaBean patRegBean = patRegDao.findByKey("patient_id", visitId);
    request.setAttribute("consultationsAcrVisits",
        js.serialize(ConversionUtils.copyListDynaBeansToMap(
            orderDao.consultationsEncounterWise((String) patRegBean.get("main_visit_id")))));

    ToothImageDetails adultToothImageDetails = DentalChartHelperDAO.getToothImageDetails(true);
    ToothImageDetails pediacToothImageDetails = DentalChartHelperDAO.getToothImageDetails(false);

    String toothNumberingSystem = (String) GenericPreferencesDAO.getAllPrefs()
        .get("tooth_numbering_system");

    request.setAttribute("adult_tooth_numbers",
        DentalChartHelperDAO.getToothNumbers(toothNumberingSystem, adultToothImageDetails));
    request.setAttribute("pediac_tooth_numbers",
        DentalChartHelperDAO.getToothNumbers(toothNumberingSystem, pediacToothImageDetails));

    if (filter == null || filter.equals("")) {
      request.setAttribute("babyOrMother", new DashBoardDAO().getBabyOrMotherDetails(visitId));

      List<BasicDynaBean> operations = OrderDAO.getOperations(visitId);
      Map operOrdersMap = new HashMap();
      Map<Integer, List<BasicDynaBean>> operAnaesthesiaTypeOrdersMap = new HashMap<>();
      Map<Integer, List<BasicDynaBean>> advanceOTAnaesthesiaTypeOrdersMap = new HashMap<>();
      for (BasicDynaBean op : operations) {
        int opId = (Integer) op.get("id");
        List operOrders = OrderDAO.getOperationRefOrders(visitId, opId);
        List operAnaesthesiaTypes = OrderDAO.getOperAnaesthesiaDetails(opId);
        operOrdersMap.put(opId, operOrders);
        operAnaesthesiaTypeOrdersMap.put(opId, operAnaesthesiaTypes);
        List advOtAnaesthesiaTypes = OrderDAO.getAdvanceOperAnaesthesiaDetails(opId);
        advanceOTAnaesthesiaTypeOrdersMap.put(opId, advOtAnaesthesiaTypes);
      }
      request.setAttribute("operations", operations);
      request.setAttribute("operOrdersMap", operOrdersMap);
      request.setAttribute("operAnaesthesiaTypeOrdersMap", operAnaesthesiaTypeOrdersMap);
      request.setAttribute("advotanaesthesiatypeordersmap", advanceOTAnaesthesiaTypeOrdersMap);
    }
    request.setAttribute("operation_details_id", request.getParameter("operation_details_id"));
    request.setAttribute("fromOTScreen", request.getParameter("fromOTScreen"));

    // get all the multivisit package details for the visit
    GenericDAO mvdao = new GenericDAO("multivisit_patient_package_view");
    List<BasicDynaBean> mvpackList = mvdao.findAllByKey("patient_id", visitId);
    if (null != mvpackList && mvpackList.size() > 0) {
      request.setAttribute("mvpackageList",
          ConversionUtils.listBeanToMapBean(mvpackList, "bill_no"));
    }

    return mapping.findForward("showorders");
  }

  /**
   * Update.
   *
   * @param mapping the mapping
   * @param form    the form
   * @param req     the req
   * @param res     the res
   * @return the action forward
   * @throws Exception the exception
   */
  public ActionForward update(ActionMapping mapping, ActionForm form, HttpServletRequest req,
      HttpServletResponse res) throws Exception {

    String patientId = req.getParameter("patientid");
    String billNo = req.getParameter("billNo");
    String refBillParam = req.getParameter("billNo");

    boolean isInsurance = false;

    String fromOtScreen = req.getParameter("fromOTScreen");
    String operationDetailsId = req.getParameter("operation_details_id");

    if (billNo != null && billNo.startsWith("new")) {
      if (billNo.equals("newInsurance")) {
        isInsurance = true;
      }
      billNo = null;
    }

    String print = req.getParameter("print");
    String printerId = req.getParameter("printerId");
    logger.debug("PrinterId = " + printerId);

    FlashScope flash = FlashScope.getScope(req);
    // todo: get this from forwards
    String url = req.getRequestURI() + "?_method=getOrders&patient_id=" + patientId + "&prgkey="
        + flash.key() + "&operation_details_id=" + operationDetailsId + "&fromOTScreen="
        + fromOtScreen;

    HttpSession session = req.getSession(false);
    String userId = (String) session.getAttribute("userId");
    if (userId.equals("")) {
      return null;
    }

    List errorList = new ArrayList();
    OrderBO order = new OrderBO(); // BO to share the commonOrderId, billNo etc.

    /*
     * Parse the params into a set of new order beans, and a set of edits to existing Orders. (edits
     * includes cancellation, if any)
     */

    List newOrders = new ArrayList();
    // List newMultiVisitPackageOrders = new ArrayList();
    List newPreAuths = new ArrayList();
    List secNewPreAuths = new ArrayList();
    List<String> packObIdsList = new ArrayList<String>();
    List<String> packageIdsList = new ArrayList<String>();
    List<Integer> patPackageIdsList = new ArrayList<Integer>();
    List firstOfCategoryList = new ArrayList();
    List<String> condDoctrsList = new ArrayList<String>();
    List<Integer> preAuthModeList = new ArrayList<Integer>();
    List<Integer> secPreAuthModeList = new ArrayList<Integer>();
    List<Boolean> multiVisitPackageList = new ArrayList<Boolean>();
    List<Map<String, Object>> operationAnaesTypesList = new ArrayList<Map<String, Object>>();
    List<Map<String, Object>> opEditAnaesTypesList = new ArrayList<Map<String, Object>>();
    List<List<TestDocumentDTO>> testAdditionalDocList = new ArrayList<List<TestDocumentDTO>>();

    getNewOrderBeans(req.getParameterMap(), newOrders, newPreAuths, preAuthModeList,
        firstOfCategoryList, condDoctrsList, multiVisitPackageList, packageIdsList, packObIdsList,
        errorList, secNewPreAuths, secPreAuthModeList, operationAnaesTypesList,
        testAdditionalDocList, form);

    List<PackageOrderDTO> newPackageOrders = getNewPackageOrders(req.getParameterMap(), form);
    BasicDynaBean newMultiVisitPackageOrdersBean = getNewMultiVisitPackageOrders(req);

    List<BasicDynaBean> cancelItemOrders = new ArrayList<BasicDynaBean>();
    List<BasicDynaBean> cancelItemChargeOrders = new ArrayList<BasicDynaBean>();
    List<BasicDynaBean> editOrders = new ArrayList<BasicDynaBean>();

    List<String> editOrCancelOrderBills = new ArrayList<String>();
    List<TestDocumentDTO> editTestAdditionalDocList = new ArrayList<TestDocumentDTO>();

    getModifiedOrderBeans(order, req.getParameterMap(), userId, cancelItemOrders,
        cancelItemChargeOrders, editOrders, opEditAnaesTypesList, editTestAdditionalDocList, form);

    logger.debug("New orders: " + newOrders.size() + "; Cancel: " + cancelItemChargeOrders.size()
        + "; Cancel w.o.r.: " + cancelItemOrders.size() + "; Edit: " + editOrders.size());

    if (errorList.size() > 0) {
      flash.error("Parameter conversion error.");
      res.sendRedirect(url);
      return null;
    }

    Connection con = null;
    boolean success = false;
    String err = null;
    try {
      validate: {
        con = DataBaseUtil.getConnection();
        con.setAutoCommit(false);

        /**
         * bill may be empty if only updating orders, and no bill selected. Bill may be null if "New
         * bill" is selected. If no new orders are passed new bill creation is restricted.
         */
        boolean ordersExists = false;
        if (newOrders.size() > 0) {
          ordersExists = true;
        }
        if (!ordersExists && newPackageOrders.size() > 0) {
          ordersExists = true;
        }

        if (refBillParam != null && !refBillParam.isEmpty() && !(!ordersExists && billNo == null)) {
          err = order.setBillInfo(con, patientId, billNo, isInsurance, userId);
          if (err != null) {
            break validate;
          }
        }
        if (newMultiVisitPackageOrdersBean != null) {
          err = order.orderMultiVisitPackage(con, newMultiVisitPackageOrdersBean);
        }

        if (err != null) {
          break validate;
        }

        err = order.insertOrderSetsIntoPatientPackages(con, packageIdsList, packObIdsList,
            multiVisitPackageList, patPackageIdsList);

        if (err != null) {
          break validate;
        }

        err = order.orderItems(con, newOrders, newPreAuths, preAuthModeList, firstOfCategoryList,
            condDoctrsList, multiVisitPackageList, packageIdsList, packObIdsList, patPackageIdsList,
            0, secNewPreAuths, secPreAuthModeList, operationAnaesTypesList, testAdditionalDocList);
        if (err != null) {
          break validate;
        }

        err = order.orderPackages(con, newPackageOrders);
        if (err != null) {
          break validate;
        }

        // booleans: cancel, cancelCharges, updateHasActivity
        err = order.updateOrders(con, cancelItemOrders, true, false, true, editOrCancelOrderBills,
            opEditAnaesTypesList, editTestAdditionalDocList);
        if (err != null) {
          break validate;
        }

        err = order.updateOrders(con, cancelItemChargeOrders, true, true, false,
            editOrCancelOrderBills, opEditAnaesTypesList, null);
        if (err != null) {
          break validate;
        }

        err = order.updateOrders(con, editOrders, false, false, false, editOrCancelOrderBills,
            opEditAnaesTypesList, null);
        if (err != null) {
          break validate;
        }

        if (null != newMultiVisitPackageOrdersBean) {
          order.updateMultivisitPackageStatus(con, newMultiVisitPackageOrdersBean);
        }

        /*
         * If there are any a test (include packages)/service/diet prescriptions, update the
         * prescription saying it has been fulfilled.
         */
        String[] testPresIds = (String[]) req.getParameterValues("test.doc_presc_id");
        if (testPresIds != null) {
          for (int i = 0; i < testPresIds.length; i++) {
            if (!testPresIds[i].isEmpty()) {
              PrescribeDAO.updateTestPrescription(con, "O", Integer.parseInt(testPresIds[i]));
            }
          }
        }

        // package prescriptions are nothing but test prescriptions.
        String[] packagePresIds = (String[]) req.getParameterValues("package.doc_presc_id");
        if (packagePresIds != null) {
          for (int i = 0; i < packagePresIds.length; i++) {
            if (!packagePresIds[i].isEmpty()) {
              PrescribeDAO.updateTestPrescription(con, "O", Integer.parseInt(packagePresIds[i]));
            }
          }
        }

        String[] serPresIds = (String[]) req.getParameterValues("service.doc_presc_id");
        String[] isServiceStanding = (String[]) req.getParameterValues("service.is_standing");
        if (serPresIds != null) {
          for (int i = 0; i < serPresIds.length; i++) {
            if (!serPresIds[i].isEmpty()) {
              PrescribeDAO.updateServicePrescription(con, "O", Integer.parseInt(serPresIds[i]));
            }
          }
        }

        String[] dietPresIds = (String[]) req.getParameterValues("diet.doc_presc_id");
        if (dietPresIds != null) {
          GenericDAO dietDao = new GenericDAO("patient_diet_prescriptions");
          for (int i = 0; i < dietPresIds.length; i++) {
            if (!dietPresIds[i].isEmpty()) {
              BasicDynaBean dpBean = dietDao.getBean();
              dpBean.set("diet_pres_id", Integer.parseInt(dietPresIds[i]));
              dpBean.set("added_to_bill", true);
              dietDao.updateWithName(con, dpBean.getMap(), "diet_pres_id");
            }
          }
        }

        String[] consultationPresIds = (String[]) req.getParameterValues("doctor.prescription_id");
        if (consultationPresIds != null) {
          PatientPrescriptionDAO consultDao = new PatientPrescriptionDAO();
          for (int i = 0; i < consultationPresIds.length; i++) {
            if (!consultationPresIds[i].isEmpty()) {
              BasicDynaBean cpBean = consultDao.getBean();
              cpBean.set("patient_presc_id", Integer.parseInt(consultationPresIds[i]));
              cpBean.set("status", "O");
              cpBean.set("username", RequestContext.getUserName());

              consultDao.updateWithName(con, cpBean.getMap(), "patient_presc_id");
            }
          }
        }
        for (BasicDynaBean ben : cancelItemOrders) {
          DynaClass clBean = ben.getDynaClass();
          String primaryKey = null;
          String type = null;
          if (clBean.getName().equals("tests_prescribed")) {
            primaryKey = "prescribed_id";
            type = "I";
          } else if (clBean.getName().equals("services_prescribed")) {
            primaryKey = "prescription_id";
            type = "S";
          }
          if (type != null
              && PatientActivitiesDAO.getActivity(con, (Integer) ben.get(primaryKey), type) != null
              && !PatientActivitiesDAO.updateStatus(con, (Integer) ben.get(primaryKey), type)) {
            err = "Failed to update the status for patient activity";
            break validate;
          }
        }
        for (BasicDynaBean b : cancelItemChargeOrders) {
          DynaClass cla = b.getDynaClass();
          String primaryKey = null;
          String type = null;
          if (cla.getName().equals("tests_prescribed")) {
            primaryKey = "prescribed_id";
            type = "I";
          } else if (cla.getName().equals("services_prescribed")) {
            primaryKey = "prescription_id";
            type = "S";
          }
          if (type != null
              && PatientActivitiesDAO.getActivity(con, (Integer) b.get(primaryKey), type) != null
              && !PatientActivitiesDAO.updateStatus(con, (Integer) b.get(primaryKey), type)) {
            err = "Failed to update the status for patient activity";
            break validate;
          }
        }

        if (print.equalsIgnoreCase("Y") && order.getCommonOrderId() != 0) {
          List<String> printUrls = new ArrayList<String>();
          printUrls.add(req.getContextPath() + "/pages/orders/ordersPrint.do?_method=printOrder"
              + "&mrno=" + req.getParameter("mrno") + "&patientId=" + patientId + "&orderid="
              + order.getCommonOrderId() + "&printerId=" + printerId);
          session.setAttribute("printURLs", printUrls);
        }
        success = true;

        if (isInsurance) { // update payment_status to 'P' if patient due is 0
          DataBaseUtil.commitClose(con, success);

          con = DataBaseUtil.getConnection();
          con.setAutoCommit(false);

          if (!(!ordersExists && billNo == null)) {

            String newPatientAmt = req.getParameter("newPatientAmt");
            BigDecimal patientDue = null != newPatientAmt && !newPatientAmt.equals("")
                ? new BigDecimal(newPatientAmt)
                : BigDecimal.ZERO;

            if (patientDue.compareTo(BigDecimal.ZERO) == 0) {
              err = order.setBillPaidStatus(con);
            }

            if (err != null) {
              break validate;
            }
          }
        }
      }
      if (err != null) {
        flash.error(err);
        res.sendRedirect(url);
        return null;
      }

      // Include order bill if not a new bill with the edited order bills for sponsor
      // claim amounts reset.
      // Do not include new bill with the edited order bills.
      if (billNo != null && !billNo.startsWith("new")) {
        BasicDynaBean billbean = order.getBill();
        if (billbean != null && editOrCancelOrderBills != null) {
          String billNo1 = (String) billbean.get("bill_no");
          if (!editOrCancelOrderBills.contains(billNo1)) {
            editOrCancelOrderBills.add(billNo1);
            // Update the bill total amount.
            allocationService.updateBillTotal(billNo1);
            int centerId = (Integer) req.getSession().getAttribute("centerId");
            // Call the Allocation method.
            allocationService.allocate(billNo1, centerId);
          }
        }
      }

    } catch (Exception ex) {
      success = false;
      throw ex;
    } finally {
      DataBaseUtil.commitClose(con, success);
      if (success) {

        String orderBillNo = req.getParameter("billNo");

        // New bill's deduction and claim amount should be reset newly.
        if (orderBillNo != null && orderBillNo.startsWith("new")) {
          BasicDynaBean billbean = order.getBill();
          if (billbean != null) {
            String billNo1 = (String) billbean.get("bill_no");
            if (billNo1 != null && !billNo1.equals("")) {
              BillDAO.setDeductionAndSponsorClaimTotals(billNo1);
            }
          }
        }

        if (editOrCancelOrderBills != null) {
          for (String billNo2 : editOrCancelOrderBills) {
            if (billNo2 != null && !billNo2.equals("")) {
              BillDAO.resetTotalsOrReProcess(billNo2);
            }
          }
        }
      }
      // change as per new insurance-3.0 calculator
      new SponsorBO().recalculateSponsorAmount(patientId);
    }

    res.sendRedirect(url);
    return null;
  }

  /**
   * Prints the order.
   *
   * @param mapping  the mapping
   * @param form     the form
   * @param request  the request
   * @param response the response
   * @return the action forward
   * @throws Exception the exception
   */
  public ActionForward printOrder(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws Exception {

    Map mapDetails = new HashMap();
    String patientId = request.getParameter("patientId");

    GenericDocumentsFields.copyPatientDetails(mapDetails, null, patientId, false);
    Map templateParamsMap = new HashMap();
    templateParamsMap.put("visitdetails", mapDetails);
    templateParamsMap.put("modules_activated",
        RequestContext.getSession().getAttribute("preferences"));
    templateParamsMap.put("username", RequestContext.getSession().getAttribute("userid"));

    PrescriptionViewDAO preDao = new PrescriptionViewDAO();
    int orderId = Integer.parseInt(request.getParameter("orderid"));
    List<BasicDynaBean> allPrescribedListBean = orderDao.getAllOrdersByCommonOrderId(patientId,
        orderId);

    for (BasicDynaBean chrg : allPrescribedListBean) {
      BigDecimal packAmount = (BigDecimal)chrg.get("pack_amount");
      if (packAmount != null && packAmount.compareTo(BigDecimal.ZERO) != 0) {
        chrg.set("amount", packAmount);
      }
    }

    List allItems = new ArrayList();
    allItems.addAll(ConversionUtils.listBeanToListMap(allPrescribedListBean));

    // Get the package details if it is available
    boolean multivisitPackage = false;
    Integer pid = null;
    String packageId = request.getParameter("packageId");
    pid = (null != packageId && !"".equals(packageId)) ? Integer.parseInt(packageId) : null;

    BasicDynaBean mvpBean = getMultivisitPackage(pid, orderId);
    multivisitPackage = (null != mvpBean) ? (Boolean) mvpBean.get("multi_visit_package") : false;

    if (multivisitPackage) {
      List<BasicDynaBean> allPackageItems = orderDao.getPackageRefOrdersByCommonOrderId(patientId,
          orderId);
      allItems.addAll(ConversionUtils.listBeanToListMap(allPackageItems));
    }

    templateParamsMap.put("orders", allItems);
    List<BasicDynaBean> operations = orderDao.getOperationsByCommonOrderId(patientId, orderId);
    List<BasicDynaBean> opSubOrders = orderDao.getOperationRefOrdersByCommonOrderId(patientId,
        orderId);

    templateParamsMap.put("operations", ConversionUtils.listBeanToListMap(operations));
    templateParamsMap.put("operationSubOrders", ConversionUtils.listBeanToListMap(opSubOrders));
    templateParamsMap.put("orderId", orderId);

    // Look for orders within operationRefOrders, but the operation itself is not
    // there
    // This happens if an operationRef order is added to an existing operation, so
    // the
    // common order id of the operation and the sub-order are different.
    List<Map> orphanSubOrders = new ArrayList();
    for (BasicDynaBean bean : opSubOrders) {
      boolean operExists = false;
      int opRef = (Integer) bean.get("operation_ref");

      for (BasicDynaBean op : operations) {
        if ((Integer) op.get("id") == opRef) {
          operExists = true;
          break;
        }
      }
      if (!operExists) {
        orphanSubOrders.add(bean.getMap());
      }
    }
    templateParamsMap.put("orphanSubOrders", orphanSubOrders);

    Configuration cfg;
    cfg = AppInit.getFmConfig();
    cfg.setTagSyntax(Configuration.AUTO_DETECT_TAG_SYNTAX);

    OutputStream os = null;
    HtmlConverter hc = new HtmlConverter();
    int printerId = 0;
    String printerIdStr = request.getParameter("printerId");
    if ((printerIdStr != null) && !printerIdStr.equals("")) {
      printerId = Integer.parseInt(printerIdStr);
    }
    BasicDynaBean printPrefs = PrintConfigurationsDAO
        .getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_SERVICE, printerId);

    String printMode = "P";
    if (printPrefs.get("print_mode") != null) {
      printMode = (String) printPrefs.get("print_mode");
    }

    Template tem = null;

    String templateContent = null;
    StringWriter writer = new StringWriter();
    PrintTemplatesDAO printtemplatedao = new PrintTemplatesDAO();
    templateContent = printtemplatedao.getCustomizedTemplate(PrintTemplate.Order);
    if (templateContent == null || templateContent.equals("")) {
      tem = cfg.getTemplate(PrintTemplate.Order.getFtlName() + ".ftl");
    } else {
      StringReader reader = new StringReader(templateContent);
      tem = new Template("IPPrescription.ftl", reader, AppInit.getFmConfig());
    }

    tem.process(templateParamsMap, writer);
    if (printMode.equals("P")) {
      os = response.getOutputStream();
      response.setContentType("application/pdf");
      boolean repeatPatientHeader = ((String) printPrefs.get("repeat_patient_info"))
          .equalsIgnoreCase("Y");
      hc.writePdf(os, writer.toString(), "IP Prescription", printPrefs, false, repeatPatientHeader,
          true, true, true, false);
      os.close();
      return null;
    } else {
      String textReport = new String(
          hc.getText(writer.toString(), "IP Prescription", printPrefs, true, true));
      request.setAttribute("textReport", textReport);
      request.setAttribute("textColumns", printPrefs.get("text_mode_column"));
      request.setAttribute("printerType", "DMP");
      return mapping.findForward("textPrintApplet");
    }
  }

  /**
   * Gets the parameter.
   *
   * @param params the params
   * @param key    the key
   * @param index  the index
   * @return the parameter
   */
  public static String getParameter(Map params, String key, int index) {
    Object[] obj = (Object[]) params.get(key);
    if (obj == null || index >= obj.length || obj[index] == null) {
      return null;
    }
    return obj[index].toString();
  }

  /**
   * Gets the new order beans.
   *
   * @param params                 the params
   * @param orders                 the orders
   * @param newPreAuths            the new pre auths
   * @param newPreAuthModesList    the new pre auth modes list
   * @param firstOfCategoryList    the first of category list
   * @param condDoctrsList         the cond doctrs list
   * @param multiVisitPackageList  the multi visit package list
   * @param errorList              the error list
   * @param secNewPreAuths         the sec new pre auths
   * @param secNewPreAuthModesList the sec new pre auth modes list
   * @param opAnesTypesList        the op anes types list
   * @param testAdditionalDocs     the test additional docs
   * @param form                   the for
   * @throws SQLException          the SQL exception
   * @throws ParseException        the parse exception
   * @throws FileNotFoundException the file not found exception
   * @throws IOException           Signals that an I/O exception has occurred.
   */
  public static void getNewOrderBeans(Map<String, String[]> params, List orders, List newPreAuths,
      List<Integer> newPreAuthModesList, List firstOfCategoryList, List condDoctrsList,
      List multiVisitPackageList, List errorList, List secNewPreAuths,
      List<Integer> secNewPreAuthModesList, List<Map<String, Object>> opAnesTypesList,
      List<List<TestDocumentDTO>> testAdditionalDocs, ActionForm form)
      throws SQLException, ParseException, FileNotFoundException, IOException {
    getNewOrderBeans(params, orders, newPreAuths, newPreAuthModesList, firstOfCategoryList,
        condDoctrsList, multiVisitPackageList, new ArrayList<String>(), new ArrayList<String>(),
        errorList, secNewPreAuths, secNewPreAuthModesList, opAnesTypesList, testAdditionalDocs,
        form);
  }

  /**
   * Gets the new order beans.
   *
   * @param params                 the params
   * @param orders                 the orders
   * @param newPreAuths            the new pre auths
   * @param newPreAuthModesList    the new pre auth modes list
   * @param firstOfCategoryList    the first of category list
   * @param condDoctrsList         the cond doctrs list
   * @param multiVisitPackageList  the multi visit package list
   * @param packageIdsList         the package ids list
   * @param packObIdsList          the pack ob ids list
   * @param errorList              the error list
   * @param secNewPreAuths         the sec new pre auths
   * @param secNewPreAuthModesList the sec new pre auth modes list
   * @param opAnesTypesList        the op anes types list
   * @param testAdditionalDocs     the test additional docs
   * @param form                   the form
   * @throws SQLException          the SQL exception
   * @throws ParseException        the parse exception
   * @throws FileNotFoundException the file not found exception
   * @throws IOException           Signals that an I/O exception has occurred.
   */
  public static void getNewOrderBeans(Map<String, String[]> params, List orders, List newPreAuths,
      List<Integer> newPreAuthModesList, List firstOfCategoryList, List condDoctrsList,
      List multiVisitPackageList, List<String> packageIdsList, List<String> packObIdsList,
      List errorList, List secNewPreAuths, List<Integer> secNewPreAuthModesList,
      List<Map<String, Object>> opAnesTypesList, List<List<TestDocumentDTO>> testAdditionalDocs,
      ActionForm form) throws SQLException, ParseException, FileNotFoundException, IOException {

    CommonOrderForm orderForm = (CommonOrderForm) form;

    GenericDAO testDao = new GenericDAO("tests_prescribed");
    GenericDAO servDao = new GenericDAO("services_prescribed");
    GenericDAO othServDao = new GenericDAO("other_services_prescribed");
    GenericDAO docDao = new GenericDAO("doctor_consultation");
    GenericDAO eqDao = new GenericDAO("equipment_prescribed");
    GenericDAO dietDao = new GenericDAO("diet_prescribed");
    GenericDAO opDao = new GenericDAO("bed_operation_schedule");
    String[] testIds = (String[]) params.get("test.test_id");
    if ((testIds != null)) {
      String[] multiVisitPackage = (String[]) params.get("test.multi_visit_package");
      String[] clinicalNotesAr = orderForm.getAd_clinical_notes();
      String[] rowIdsAr = orderForm.getAd_main_row_id();
      String[] docIdAr = orderForm.getAd_test_doc_id();
      List uploadedDocAr = orderForm.getAd_test_file_upload();
      String[] testCategory = orderForm.getAd_test_category();
      Boolean[] notesEdited = orderForm.getAd_notes_entered();

      for (int i = 0; i < testIds.length; i++) {
        BasicDynaBean bea = testDao.getBean();
        ConversionUtils.copyIndexToDynaBeanPrefixed(params, i, bea, errorList, "test.");

        String prescId = getParameter(params, "test.prescId", i);

        List docList = new ArrayList();
        if (rowIdsAr != null) {
          for (int d = 0; d < rowIdsAr.length; d++) {
            if (rowIdsAr[d].equals(prescId)) {
              TestDocumentDTO tddto = new TestDocumentDTO();
              tddto.setDocId(
                  docIdAr[d] == null || docIdAr[d].equals("") ? 0 : Integer.parseInt(docIdAr[d]));
              tddto.setTestCategory(testCategory[d]);
              tddto.setNotesEdited(notesEdited[d]);
              tddto.setClinicalNotes(clinicalNotesAr[d]);

              FormFile file = (FormFile) uploadedDocAr.get(d);
              if (file != null && file.getFileSize() > 0) {
                tddto.setDocContent(file.getInputStream());
                tddto.setFileName(file.getFileName());

                String fileName = file.getFileName();
                String extension = null;
                if (fileName.contains(".")) {
                  extension = fileName.substring(fileName.indexOf(".") + 1);
                  if (extension.equals("odt") || extension.equals("ods")) {
                    tddto.setContentType("application/vnd.oasis.opendocument.text");
                  } else {
                    tddto.setContentType(MimeTypeDetector.getMimeTypes(file.getInputStream()));
                  }
                } else {
                  tddto.setContentType(MimeTypeDetector.getMimeTypes(file.getInputStream()));
                }
                tddto.setExtension(extension);
              }

              docList.add(tddto);
            }
          }
        }
        testAdditionalDocs.add(docList);

        bea.set("priority", params.get("test.urgent")[i]);
        orders.add(bea);
        newPreAuths.add(params.get("test.prior_auth_id")[i]);
        newPreAuthModesList.add((params.get("test.prior_auth_mode_id")[i]) == null
            || (params.get("test.prior_auth_mode_id")[i]).equals("") ? null
                : Integer.parseInt((params.get("test.prior_auth_mode_id")[i])));
        secNewPreAuths.add(params.get("test.sec_prior_auth_id")[i]);
        secNewPreAuthModesList.add((params.get("test.sec_prior_auth_mode_id")[i]) == null
            || (params.get("test.sec_prior_auth_mode_id")[i]).equals("") ? null
                : Integer.parseInt((params.get("test.sec_prior_auth_mode_id")[i])));

        firstOfCategoryList.add(params.get("test.firstOfCategory")[i]);
        condDoctrsList.add(params.get("test.payee_doctor_id")[i]);
        multiVisitPackageList.add(new Boolean(multiVisitPackage[i]));
        packageIdsList.add(params.get("test.package_id")[i]);
        packObIdsList.add(params.get("test.pack_ob_id")[i]);
      }
      logger.debug("Number of orders after tests: " + orders.size());
    }

    String[] serviceIds = (String[]) params.get("service.service_id");
    if ((serviceIds != null)) {
      String[] multiVisitPackage = (String[]) params.get("service.multi_visit_package");
      for (int i = 0; i < serviceIds.length; i++) {
        BasicDynaBean bea = servDao.getBean();
        ConversionUtils.copyIndexToDynaBeanPrefixed(params, i, bea, errorList, "service.");
        newPreAuths.add(params.get("service.prior_auth_id")[i]);
        newPreAuthModesList.add(params.get("service.prior_auth_mode_id")[i] == null
            || params.get("service.prior_auth_mode_id")[i].equals("") ? null
                : Integer.parseInt(params.get("service.prior_auth_mode_id")[i]));

        secNewPreAuths.add(params.get("service.sec_prior_auth_id")[i]);
        secNewPreAuthModesList.add(params.get("service.sec_prior_auth_mode_id")[i] == null
            || params.get("service.sec_prior_auth_mode_id")[i].equals("") ? null
                : Integer.parseInt(params.get("service.sec_prior_auth_mode_id")[i]));

        firstOfCategoryList.add(params.get("service.firstOfCategory")[i]);
        condDoctrsList.add(params.get("service.payee_doctor_id")[i]);
        orders.add(bea);
        multiVisitPackageList.add(new Boolean(multiVisitPackage[i]));
        packageIdsList.add(params.get("service.package_id")[i]);
        packObIdsList.add(params.get("service.pack_ob_id")[i]);
      }
      logger.debug("Number of orders after services: " + orders.size());
    }

    String[] equipmentIds = (String[]) params.get("equipment.eq_id");
    if ((equipmentIds != null)) {
      String[] multiVisitPackage = (String[]) params.get("equipment.multi_visit_package");
      for (int i = 0; i < equipmentIds.length; i++) {
        BasicDynaBean bea = eqDao.getBean();
        ConversionUtils.copyIndexToDynaBeanPrefixed(params, i, bea, errorList, "equipment.");
        firstOfCategoryList.add(params.get("equipment.firstOfCategory")[i]);
        newPreAuthModesList.add(1);
        newPreAuths.add(null);
        secNewPreAuths.add(null);
        secNewPreAuthModesList.add(1);
        condDoctrsList.add(null);
        orders.add(bea);
        multiVisitPackageList.add(new Boolean(multiVisitPackage[i]));
        packageIdsList.add(params.get("equipment.package_id")[i]);
        packObIdsList.add(params.get("equipment.pack_ob_id")[i]);
      }
    }

    String[] othServiceNames = (String[]) params.get("other.service_name");
    if ((othServiceNames != null)) {
      String[] multiVisitPackage = (String[]) params.get("other.multi_visit_package");
      for (int i = 0; i < othServiceNames.length; i++) {
        BasicDynaBean bea = othServDao.getBean();
        ConversionUtils.copyIndexToDynaBeanPrefixed(params, i, bea, errorList, "other.");
        newPreAuths.add(null);
        newPreAuthModesList.add(1);
        secNewPreAuths.add(null);
        secNewPreAuthModesList.add(1);
        firstOfCategoryList.add(params.get("other.firstOfCategory")[i]);
        condDoctrsList.add(null);
        orders.add(bea);
        multiVisitPackageList.add(new Boolean(multiVisitPackage[i]));
        packageIdsList.add(params.get("other.package_id")[i]);
        packObIdsList.add(params.get("other.pack_ob_id")[i]);
      }
    }

    String[] doctorIds = (String[]) params.get("doctor.doctor_name");
    if ((doctorIds != null)) {
      String[] multiVisitPackage = (String[]) params.get("doctor.multi_visit_package");
      for (int i = 0; i < doctorIds.length; i++) {
        BasicDynaBean bea = docDao.getBean();
        ConversionUtils.copyIndexToDynaBeanPrefixed(params, i, bea, errorList, "doctor.");
        newPreAuths.add(null);
        newPreAuthModesList.add(1);
        secNewPreAuths.add(null);
        secNewPreAuthModesList.add(1);
        firstOfCategoryList.add(params.get("doctor.firstOfCategory")[i]);
        condDoctrsList.add(null);
        orders.add(bea);
        multiVisitPackageList.add(new Boolean(multiVisitPackage[i]));
        packageIdsList.add(params.get("doctor.package_id")[i]);
        packObIdsList.add(params.get("doctor.pack_ob_id")[i]);
      }
    }

    String[] dietIds = (String[]) params.get("diet.diet_id");
    if ((dietIds != null)) {
      String[] multiVisitPackage = (String[]) params.get("diet.multi_visit_package");
      for (int i = 0; i < dietIds.length; i++) {
        BasicDynaBean bea = dietDao.getBean();
        ConversionUtils.copyIndexToDynaBeanPrefixed(params, i, bea, errorList, "diet.");
        firstOfCategoryList.add(params.get("diet.firstOfCategory")[i]);
        newPreAuths.add(null);
        newPreAuthModesList.add(1);
        secNewPreAuths.add(null);
        secNewPreAuthModesList.add(1);
        condDoctrsList.add(null);
        orders.add(bea);
        multiVisitPackageList.add(new Boolean(multiVisitPackage[i]));
        packageIdsList.add(params.get("diet.package_id")[i]);
        packObIdsList.add(params.get("diet.pack_ob_id")[i]);
      }
    }

    String[] opIds = params.get("operation.operation_name");
    String[] opePrescribedIds = params.get("operation.prescribed_id");
    if (opIds != null) {
      String[] multiVisitPackage = (String[]) params.get("operation.multi_visit_package");
      for (int i = 0; i < opIds.length; i++) {
        String[] anaTypeFromDates = params
            .get("operation.anesthesia_type_from_date" + opePrescribedIds[i]);
        String[] anaTypeToDates = params
            .get("operation.anesthesia_type_to_date" + opePrescribedIds[i]);
        String[] anaTypeFromTimes = params
            .get("operation.anesthesia_type_from_time" + opePrescribedIds[i]);
        String[] anaTypeToTimes = params
            .get("operation.anesthesia_type_to_time" + opePrescribedIds[i]);
        BasicDynaBean bea = opDao.getBean();
        ConversionUtils.copyIndexToDynaBeanPrefixed(params, i, bea, errorList, "operation.");
        if (!params.get("operation.prior_auth_id")[i].equals("")) {
          newPreAuths.add(params.get("operation.prior_auth_id")[i]);
        }
        if (params.get("operation.prior_auth_mode_id")[i] != null
            && !params.get("operation.prior_auth_mode_id")[i].equals("")) {
          newPreAuthModesList.add(Integer.parseInt(params.get("operation.prior_auth_mode_id")[i]));
        }

        if (!params.get("operation.sec_prior_auth_id")[i].equals("")) {
          secNewPreAuths.add(params.get("operation.sec_prior_auth_id")[i]);
        }
        if (params.get("operation.sec_prior_auth_mode_id")[i] != null
            && !params.get("operation.sec_prior_auth_mode_id")[i].equals("")) {
          secNewPreAuthModesList
              .add(Integer.parseInt(params.get("operation.sec_prior_auth_mode_id")[i]));
        }

        String[] anaesTypes = params.get("operation.anesthesia_type" + opePrescribedIds[i]);
        firstOfCategoryList.add(params.get("operation.firstOfCategory")[i]);
        condDoctrsList.add(null);
        if (anaesTypes != null && anaesTypes.length > 0) {
          List<Object> anesTypeList = new ArrayList<Object>();
          List<Object> anesTypeFromList = new ArrayList<Object>();
          List<Object> anesTypToList = new ArrayList<Object>();
          for (int j = 0; j < anaesTypes.length; j++) {
            anesTypeList.add(anaesTypes[j]);
            anesTypeFromList.add(
                new DateUtil().parseTheTimestamp(anaTypeFromDates[j] + " " + anaTypeFromTimes[j]));
            anesTypToList
                .add(new DateUtil().parseTheTimestamp(anaTypeToDates[j] + " " + anaTypeToTimes[j]));
          }
          Map<String, Object> map = new HashMap<String, Object>();
          map.put("anaestesia_types", anesTypeList);
          map.put("anaestesia_types_from_date_time", anesTypeFromList);
          map.put("anaestesia_types_to_date_time", anesTypToList);
          opAnesTypesList.add(map);
        }
        orders.add(bea);
        multiVisitPackageList.add(new Boolean(multiVisitPackage[i]));
        packageIdsList.add(params.get("operation.package_id")[i]);
        packObIdsList.add(params.get("operation.pack_ob_id")[i]);
      }
      logger.debug("Number of orders after operations: " + orders.size());
    }

  }

  /**
   * Gets the new package orders.
   *
   * @param params the params
   * @param form   the form
   * @return the new package orders
   * @throws SQLException the SQL exception
   * @throws Exception    the exception
   */
  public static List getNewPackageOrders(Map params, ActionForm form)
      throws SQLException, Exception {

    String[] packageIds = (String[]) params.get("package.packageId");
    String[] packagePrescId = (String[]) params.get("package.prescId");

    String[] packageIdForDocs = (String[]) params.get("package.packIdFordoc");
    String[] packagePrescIdForDocs = (String[]) params.get("package.packPrescIdFordoc");

    String[] packIdForCondDocs = (String[]) params.get("package.packIdForCondDoc");
    String[] packPrescIdForCondDocs = (String[]) params.get("package.packPrescIdForCondDoc");

    GenericPreferencesDTO genPrefs = GenericPreferencesDAO.getGenericPreferences();

    List<PackageOrderDTO> packageOrders = new ArrayList<PackageOrderDTO>();

    if ((packageIds != null)) {
      for (int i = 0; i < packageIds.length; i++) {
        PackageOrderDTO dto = new PackageOrderDTO();
        if (!((String[]) params.get("package.doc_presc_id"))[i].equals("")) {
          dto.setDocPrescId(Integer.parseInt(((String[]) params.get("package.doc_presc_id"))[i]));
        }
        dto.setPackageId(Integer.parseInt(((String[]) params.get("package.packageId"))[i]));
        dto.setDoctorId(((String[]) params.get("package.doctorId"))[i]);
        dto.setRemarks(((String[]) params.get("package.remarks"))[i]);
        dto.setOrderedTime(
            DataBaseUtil.parseTimestamp(((String[]) params.get("package.orderedTime"))[i] + ":00"));

        if (((String[]) params.get("package.opId"))[i] != null
            && !((String[]) params.get("package.opId"))[i].equals("")) {
          dto.setOpId(((String[]) params.get("package.opId"))[i]);
          dto.setStartDateTime((Timestamp) ConvertUtils
              .convert(((Object[]) params.get("package.startDateTime"))[i], Timestamp.class));
          if (genPrefs.getFixedOtCharges().equals("N")) {
            dto.setToTime((Timestamp) ConvertUtils
                .convert(((Object[]) params.get("package.toDateTime"))[i], Timestamp.class));
            dto.setTheatreName(((String[]) params.get("package.theatreName"))[i]);
            dto.setUnits(((String[]) params.get("package.units"))[i]);
          }
          dto.setSurgeon(((String[]) params.get("package.surgeon"))[i]);
          dto.setAnaesth(((String[]) params.get("package.anaesth"))[i]);
          dto.setStatus(((String[]) params.get("package.status"))[i]);
          dto.setNeedReport((Boolean) ConvertUtils
              .convert(((Object[]) params.get("package.needReport"))[i], Boolean.class));
        }

        if (packageIdForDocs != null) {
          ArrayList<PackageDoctorVisit> list = new ArrayList<PackageDoctorVisit>();
          for (int j = 0; j < packageIdForDocs.length; j++) {
            PackageDoctorVisit docVisit = new PackageDoctorVisit();
            if (packagePrescId[i].equals(packagePrescIdForDocs[j])) {
              docVisit.setDoctorHead(((String[]) params.get("package.doctorHead"))[j]);
              docVisit.setVDoctor(((String[]) params.get("package.vDoctor"))[j]);
              docVisit.setDocVisitDateTime((Timestamp) ConvertUtils.convert(
                  ((Object[]) params.get("package.docVisitDateTime"))[j], Timestamp.class));
              list.add(docVisit);
            }
          }
          dto.setDoctorVisits(list);
        }
        if (packIdForCondDocs != null) {
          ArrayList list = new ArrayList();
          for (int j = 0; j < packIdForCondDocs.length; j++) {
            if (packagePrescId[i].equals(packPrescIdForCondDocs[j])) {
              Map values = new HashMap();
              values.put("act_index", ((String[]) params.get("package.packageActivityIndex"))[j]);
              values.put("doctor_id", ((String[]) params.get("package.condDoctor"))[j]);
              list.add(values);
            }
          }
          dto.setConductingDoctors(list);
        }
        CommonOrderForm orderForm = (CommonOrderForm) form;

        String[] clinicalNotesAr = orderForm.getAd_clinical_notes();
        String[] rowIdsAr = orderForm.getAd_main_row_id();
        String[] docIdAr = orderForm.getAd_test_doc_id();
        List uploadedDocAr = orderForm.getAd_test_file_upload();
        String[] testCategory = orderForm.getAd_test_category();
        Boolean[] notesEdited = orderForm.getAd_notes_entered();

        List docList = new ArrayList();
        if (rowIdsAr != null) {
          for (int d = 0; d < rowIdsAr.length; d++) {
            if (rowIdsAr[d].equals(packagePrescId[i])) {

              TestDocumentDTO tddto = new TestDocumentDTO();
              tddto.setDocId(docIdAr[d] == null || docIdAr[d].equals("") ? 0
                  : Integer.parseInt((String) docIdAr[d]));
              tddto.setTestCategory(testCategory[d]);
              tddto.setNotesEdited(notesEdited[d]);

              FormFile file = (FormFile) uploadedDocAr.get(d);
              if (file != null && file.getFileSize() > 0) {
                tddto.setDocContent(file.getInputStream());
                tddto.setFileName(file.getFileName());

                String fileName = file.getFileName();
                String extension = null;

                if (fileName.contains(".")) {
                  extension = fileName.substring(fileName.indexOf(".") + 1);
                  if (extension.equals("odt") || extension.equals("ods")) {
                    tddto.setContentType("application/vnd.oasis.opendocument.text");
                  } else {
                    tddto.setContentType(MimeTypeDetector.getMimeTypes(file.getInputStream()));
                  }
                } else {
                  tddto.setContentType(MimeTypeDetector.getMimeTypes(file.getInputStream()));
                }
                tddto.setExtension(extension);
              }
              tddto.setClinicalNotes(clinicalNotesAr[d]);
              tddto.setActivityIndex(orderForm.getAd_package_activity_index()[d]);

              docList.add(tddto);
            }
          }
        }
        dto.setTestDocuments(docList);
        dto.setPreAuthId(((String[]) params.get("package.prior_auth_id"))[i]);
        packageOrders.add(dto);
      }
    }

    return packageOrders;
  }

  /**
   * Gets the new multi visit package orders.
   *
   * @param req the req
   * @return the new multi visit package orders
   * @throws SQLException the SQL exception
   * @throws Exception    the exception
   */
  // multivisitpackage
  public static BasicDynaBean getNewMultiVisitPackageOrders(HttpServletRequest req)
      throws SQLException, Exception {

    GenericDAO packPresDao = new GenericDAO("package_prescribed");
    BasicDynaBean patPackPresBean = null;
    String[] testIds = req.getParameterValues("test.test_id");
    int index = 0;
    if ((testIds != null)) {
      String[] packageIds = req.getParameterValues("test.package_id");
      String[] remarks = req.getParameterValues("test.remarks");
      for (int i = 0; i < testIds.length; i++) {
        String packId = packageIds[i];
        if (packId != null && !packId.equals("")) {
          patPackPresBean = packPresDao.getBean();
          patPackPresBean.set("package_id", Integer.parseInt(packId));
          patPackPresBean.set("remarks", remarks[i]);
          index++;
          if (index > 0) {
            break;
          }
        }
      }
    }

    String[] serviceIds = req.getParameterValues("service.service_id");

    if ((index < 1 && serviceIds != null)) {
      String[] packageIds = req.getParameterValues("service.package_id");
      String[] remarks = req.getParameterValues("service.remarks");
      for (int i = 0; i < serviceIds.length; i++) {
        String packId = packageIds[i];
        if (packId != null && !packId.equals("")) {
          patPackPresBean = packPresDao.getBean();
          patPackPresBean.set("package_id", Integer.parseInt(packId));
          patPackPresBean.set("remarks", remarks[i]);
          index++;
          if (index > 0) {
            break;
          }
        }
      }
    }

    String[] othServiceNames = req.getParameterValues("other.service_name");

    if ((index < 1 && othServiceNames != null)) {
      String[] packageIds = req.getParameterValues("other.package_id");
      String[] remarks = req.getParameterValues("other.remarks");
      for (int i = 0; i < othServiceNames.length; i++) {
        String packId = packageIds[i];
        if (packId != null && !packId.equals("")) {
          patPackPresBean = packPresDao.getBean();
          patPackPresBean.set("package_id", Integer.parseInt(packId));
          patPackPresBean.set("remarks", remarks[i]);
          index++;
          if (index > 0) {
            break;
          }
        }
      }
    }

    String[] doctorIds = req.getParameterValues("doctor.doctor_name");

    if ((index < 1 && doctorIds != null)) {
      String[] packageIds = req.getParameterValues("doctor.package_id");
      String[] remarks = req.getParameterValues("doctor.remarks");
      for (int i = 0; i < doctorIds.length; i++) {
        String packId = packageIds[i];
        if (packId != null && !packId.equals("")) {
          patPackPresBean = packPresDao.getBean();
          patPackPresBean.set("package_id", Integer.parseInt(packId));
          patPackPresBean.set("remarks", remarks[i]);
          index++;
          if (index > 0) {
            break;
          }
        }
      }
    }

    return patPackPresBean;
  }

  /**
   * Gets the modified test documents.
   *
   * @param additionalDocs the additional docs
   * @param form           the form
   * @throws SQLException   the SQL exception
   * @throws ParseException the parse exception
   * @throws Exception      the exception
   */
  public void getModifiedTestDocuments(List<TestDocumentDTO> additionalDocs, ActionForm form)
      throws SQLException, ParseException, Exception {
    CommonOrderForm orderForm = (CommonOrderForm) form;

    String[] clinicalNotesAr = orderForm.getAd_clinical_notes();
    String[] rowIdsAr = orderForm.getAd_main_row_id();
    String[] docIdAr = orderForm.getAd_test_doc_id();
    List uploadedDocAr = orderForm.getAd_test_file_upload();
    Boolean[] edited = orderForm.getAd_test_row_edited();
    String[] testCategory = orderForm.getAd_test_category();
    Boolean[] notesEdited = orderForm.getAd_notes_entered();
    Boolean[] deleteDocument = orderForm.getAd_test_doc_delete();
    if (rowIdsAr != null) {
      for (int d = 0; d < rowIdsAr.length; d++) {
        if (edited[d] && !rowIdsAr[d].startsWith("_")) {
          TestDocumentDTO tddto = new TestDocumentDTO();
          tddto.setDocId(docIdAr[d] == null || docIdAr[d].equals("") ? 0
              : Integer.parseInt((String) docIdAr[d]));
          tddto.setTestPrescId(Integer.parseInt(rowIdsAr[d]));
          tddto.setTestCategory(testCategory[d]);
          tddto.setNotesEdited(notesEdited[d]);
          tddto.setDeleteDocument(deleteDocument[d]);

          FormFile file = (FormFile) uploadedDocAr.get(d);
          if (file != null && file.getFileSize() > 0) {
            tddto.setDocContent(file.getInputStream());
            tddto.setFileName(file.getFileName());

            String fileName = file.getFileName();
            String extension = null;

            if (fileName.contains(".")) {
              extension = fileName.substring(fileName.indexOf(".") + 1);
              if (extension.equals("odt") || extension.equals("ods")) {
                tddto.setContentType("application/vnd.oasis.opendocument.text");
              } else {
                tddto.setContentType(MimeTypeDetector.getMimeTypes(file.getInputStream()));
              }
            } else {
              tddto.setContentType(MimeTypeDetector.getMimeTypes(file.getInputStream()));
            }
            tddto.setExtension(extension);
          }
          tddto.setClinicalNotes(clinicalNotesAr[d]);

          additionalDocs.add(tddto);
        }
      }
    }
  }

  /**
   * Gets the modified order beans.
   *
   * @param orderBo           the order bo
   * @param params            the params
   * @param userName          the user name
   * @param cancelItems       the cancel items
   * @param cancelItemCharges the cancel item charges
   * @param editItems         the edit items
   * @param anaesTypesList    the anaes types list
   * @param additionalDocs    the additional docs
   * @param form              the form
   * @throws SQLException   the SQL exception
   * @throws ParseException the parse exception
   * @throws Exception      the exception
   */
  public void getModifiedOrderBeans(OrderBO orderBo, Map<String, String[]> params, String userName,
      List cancelItems, List cancelItemCharges, List editItems,
      List<Map<String, Object>> anaesTypesList, List<TestDocumentDTO> additionalDocs,
      ActionForm form) throws SQLException, ParseException, Exception {

    int surgeryCount = 0;
    String[] type = (String[]) params.get("existingtype");
    String[] cancelled = (String[]) params.get("cancelled");
    String[] prescribedId = (String[]) params.get("prescribedId");
    String[] edited = (String[]) params.get("edited");
    String[] remarks = (String[]) params.get("remarks");
    String[] presDocId = (String[]) params.get("presDocId");

    String[] fromDate = (String[]) params.get("fromDate");
    String[] fromTime = (String[]) params.get("fromTime");
    String[] toDate = (String[]) params.get("toDate");
    String[] toTime = (String[]) params.get("toTime");
    String[] newFinStatus = params.get("newFinStatus");
    String[] quantity = params.get("quantity");
    String[] priority = (String[]) params.get("urgent");
    Map<String, Object> map = new HashMap<String, Object>();

    for (int i = 0; i < type.length; i++) {

      BasicDynaBean bea = null;
      if (!prescribedId[i].equals("") && (!cancelled[i].equals("") || edited[i].equals("Y"))) {

        if (!cancelled[i].equals("")) {
          bea = orderBo.getCancelBean(type[i], prescribedId[i], userName);

        } else if (edited[i].equals("Y")) {
          bea = orderBo.getEditBean(type[i], prescribedId[i], remarks[i], presDocId[i],
              priority[i]);

          if (type[i].equals("Equipment")) {
            bea.set("used_from", DateUtil.parseTimestamp(fromDate[i], fromTime[i]));
            bea.set("used_till", DateUtil.parseTimestamp(toDate[i], toTime[i]));
            bea.set("finalization_status", newFinStatus[i]);
          }

          if (type[i].equals("Operation")) {
            bea.set("start_datetime", DateUtil.parseTimestamp(fromDate[i], fromTime[i]));
            bea.set("end_datetime", DateUtil.parseTimestamp(toDate[i], toTime[i]));
            bea.set("finalization_status", newFinStatus[i]);
            map = getAnaethesiaTypeDetailsMap(params, surgeryCount);
            anaesTypesList.add(map);
          }

          if (type[i].equals("Service")) {
            bea.set("quantity", new BigDecimal(quantity[i]));
          }
        }

        if (cancelled[i].equals("IC")) {
          cancelItemCharges.add(bea);
        } else if (cancelled[i].equals("I")) {
          cancelItems.add(bea);
        } else if (edited[i].equals("Y")) {
          editItems.add(bea);
        } else {
          logger.error(
              "Invalid update operation: Cancelled: " + cancelled[i] + "; edited: " + edited[i]);
        }
      } else {
        if (type[i].equals("Operation")) {
          surgeryCount++;
        }
      }
    }
    getModifiedTestDocuments(additionalDocs, form);
  }

  /**
   * Gets the anaethesia type details map.
   *
   * @param params the params
   * @param index  the index
   * @return the anaethesia type details map
   * @throws ParseException the parse exception
   */
  private Map<String, Object> getAnaethesiaTypeDetailsMap(Map<String, String[]> params, int index)
      throws ParseException {
    String[] opEditAnaesTypes = (String[]) params.get("anaesthesia_type_" + index);
    String[] opEditAnaesTypeFromDateTime = (String[]) params.get("anaes_start_datetime_" + index);
    String[] opEditAnaesTypeToDateTime = (String[]) params.get("anaes_end_datetime_" + index);
    String[] opEditChargeIds = (String[]) params.get("op_edit_charge_id_" + index);
    String[] opEditSurgAnaesDetId = (String[]) params.get("surgery_anesthesia_details_id_" + index);
    String[] opAdvEditOperationAnaesDetId = (String[]) params
        .get("operation_anae_detail_id_" + index);
    HttpSession session = (HttpSession) RequestContext.getSession();
    Preferences pref = (Preferences) session.getAttribute("preferences");
    String modAdvancedOt = "Y";
    if ((pref != null) && (pref.getModulesActivatedMap() != null)) {
      modAdvancedOt = (String) pref.getModulesActivatedMap().get("mod_advanced_ot");
      if (modAdvancedOt == null || modAdvancedOt.equals("")) {
        modAdvancedOt = "N";
      }
    }

    Map<String, Object> map = null;
    if (opEditAnaesTypes != null && opEditAnaesTypes.length > 0) {
      List<Timestamp> l1 = new ArrayList<Timestamp>();
      List<Timestamp> l2 = new ArrayList<Timestamp>();
      List<String> l3 = new ArrayList<String>();
      List<String> l4 = new ArrayList<String>();
      List<Integer> l5 = new ArrayList<Integer>();
      List<Integer> l6 = new ArrayList<Integer>();
      map = new HashMap<String, Object>();
      for (int j = 0; j < opEditAnaesTypes.length; j++) {
        l1.add(new DateUtil().parseTheTimestamp(opEditAnaesTypeFromDateTime[j]));
        l2.add(new DateUtil().parseTheTimestamp(opEditAnaesTypeToDateTime[j]));
        l3.add(opEditChargeIds[j]);
        l4.add(opEditAnaesTypes[j]);
        l5.add(Integer.parseInt(opEditSurgAnaesDetId[j]));
        if (modAdvancedOt.equals("Y")) {
          l6.add(Integer.parseInt(opAdvEditOperationAnaesDetId[j]));
        }
      }
      map.put("op_edit_anaes_start_date_times", l1);
      map.put("op_edit_anaes_end_date_times", l2);
      map.put("op_edit_anaes_charge_ids", l3);
      map.put("op_edit_anaesthesia_type_ids", l4);
      map.put("op_edit_surg_anaes_details_ids", l5);
      map.put("op_adv_edit_oper_anaes_details_ids", l6);
    }

    return map;
  }

  /**
   * Gets the multivisit package.
   *
   * @param pId           the id
   * @param commonOrderId the common order id
   * @return the multivisit package
   * @throws SQLException the SQL exception
   */
  private BasicDynaBean getMultivisitPackage(Integer priId, Integer commonOrderId)
      throws SQLException {
    BasicDynaBean packageBean = null;
    if (null == priId) {
      // no package id, we will get from the db
      GenericDAO mvDao = new GenericDAO("multivisit_bills_view");
      BasicDynaBean mvBean = mvDao.findByKey("order_number", commonOrderId);
      if (null != mvBean) {
        priId = (Integer) mvBean.get("pack_id");
      }
    }
    GenericDAO packageDao = new GenericDAO("packages");
    packageBean = packageDao.findByKey("package_id", priId);
    return packageBean;
  }

}
