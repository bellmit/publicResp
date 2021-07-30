package com.insta.hms.core.clinical.order.operationitems;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.annotations.Order;
import com.insta.hms.common.preferences.genericpreferences.GenericPreferencesService;
import com.insta.hms.common.preferences.ippreferences.IpPreferencesService;
import com.insta.hms.common.security.SecurityService;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.core.billing.BillActivityChargeService;
import com.insta.hms.core.billing.BillChargeService;
import com.insta.hms.core.billing.BillChargeTaxService;
import com.insta.hms.core.billing.BillChargeTransactionService;
import com.insta.hms.core.billing.BillService;
import com.insta.hms.core.clinical.operationbillableresources.OperationBillableResourcesService;
import com.insta.hms.core.clinical.operationdetails.OperationAnaesthesiaService;
import com.insta.hms.core.clinical.operationdetails.OperationDetailsService;
import com.insta.hms.core.clinical.operationdetails.SurgeryAnesthesiaDetailsService;
import com.insta.hms.core.clinical.operationprocedures.OperationProceduresService;
import com.insta.hms.core.clinical.operationteams.OperationTeamService;
import com.insta.hms.core.clinical.order.doctoritems.DoctorOrderItemService;
import com.insta.hms.core.clinical.order.master.OrderItemService;
import com.insta.hms.core.clinical.order.master.OrderService;
import com.insta.hms.core.clinical.order.packageitems.MultiVisitPackageService;
import com.insta.hms.core.clinical.order.packageitems.MultiVisitRepository;
import com.insta.hms.core.clinical.outpatient.DoctorConsultationService;
import com.insta.hms.core.clinical.prescriptions.PatientOperationPrescriptionsService;
import com.insta.hms.core.clinical.prescriptions.PatientPrescriptionsRepository;
import com.insta.hms.exception.ConversionException;
import com.insta.hms.mdm.anesthesiatypecharges.AnesthesiaTypeChargesService;
import com.insta.hms.mdm.centerpreferences.CenterPreferencesService;
import com.insta.hms.mdm.operations.OperationOrgDetailsRepository;
import com.insta.hms.mdm.operations.OperationsService;
import com.insta.hms.mdm.theatrecharges.TheatreChargesService;
import com.insta.hms.orders.OrderDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Order(key = "Operation", value = { "Operation" }, prefix = "operations")
public class OperationOrderItemService extends OrderItemService {

  private static final String CONSULTATION_TYPE_ID = "consultation_type_id";
  private static final String TAX_AMT = "tax_amt";
  private static final String INSURANCE_CATEGORY_ID = "insurance_category_id";
  private static final String CHARGE_ID = "charge_id";
  private static final String PRIMARY_SURGEON = "primarySurgeon";
  private static final String THEATRE_DETAILS = "theatreDetails";
  private static final String SURGICAL_ASSISTANCE = "surgicalAssistance";
  private static final String PRIMARY_ANAESTHETIST = "primaryAnaesthetist";
  private static final String SURGERY_TEAM = "surgeryTeam";
  private static final String AMOUNT = "amount";
  private static final String PATIENT_ID = "patient_id";
  private static final String BED_TYPE = "bed_type";
  private static final String PRESCRIBED_ID = "prescribed_id";
  private static final String CONSULTANT_DOCTOR = "consultant_doctor";
  private static final String ANAESTHESIA_DETAILS = "anaesthesiaDetails";
  private static final String ADDITIONAL_THEATRE_DETAILS = "additionalTheatreDetails";
  private static final String OPERATION_NAME = "operation_name";
  private static final String THEATRE_ID = "theatre_id";
  private static final String THEATRE_NAME = "theatre_name";
  private static final String OPERATION_PROC_ID = "operation_proc_id";
  private static final String OPERATION_DETAILS_ID = "operation_details_id";
  private static final String ACTIVITY_CODE = "OPE";
  private static final String MR_NO = "mr_no";
  private static final String CANCELLED = "cancelled";

  @Autowired
  private OperationOrderItemRepository operationOrderItemRepository;

  @LazyAutowired
  private OperationsService operationsService;

  @LazyAutowired
  private DoctorConsultationService doctorConsultationService;

  @LazyAutowired
  private TheatreChargesService theatreChargesService;

  @LazyAutowired
  private GenericPreferencesService genericPreferencesService;

  @LazyAutowired
  private IpPreferencesService ipPreferencesService;

  @LazyAutowired
  private BillChargeService billChargeService;

  @LazyAutowired
  private BillChargeTaxService billChargeTaxService;

  @LazyAutowired
  private AnesthesiaTypeChargesService anesthesiaTypeChargesService;

  @LazyAutowired
  private DoctorOrderItemService doctorOrderItemService;

  @LazyAutowired
  private SessionService sessionService;

  @LazyAutowired
  private SecurityService securityService;

  @LazyAutowired
  private PatientOperationPrescriptionsService patientOpPrescService;

  @LazyAutowired
  private PatientPrescriptionsRepository patientPrescRepo;

  @LazyAutowired
  private SurgeryAnesthesiaDetailsService surgeryAnaesthesiaDetService;

  @LazyAutowired
  private OperationDetailsService operationDetailsService;

  @LazyAutowired
  private OperationProceduresService operationProceduresService;

  @LazyAutowired
  private OperationBillableResourcesService opBillableResService;

  @LazyAutowired
  private OperationAnaesthesiaService opAnaesthesiaService;

  @LazyAutowired
  private OperationTeamService operationTeamsService;

  @LazyAutowired
  private OrderService orderService;

  @LazyAutowired
  private BillActivityChargeService billActivityChargeService;

  @LazyAutowired
  private BillService billService;
  
  @LazyAutowired
  private CenterPreferencesService centerPreferencesService;
  
  @LazyAutowired
  private BillChargeTransactionService billChargeTransactionService;
  
  @LazyAutowired
  private MultiVisitPackageService multiVisitPackageService;
  
  /** The multi visit package repository. */
  @LazyAutowired
  private MultiVisitRepository multiVisitRepository;

  public OperationOrderItemService(OperationOrderItemRepository repo,
      OperationOrgDetailsRepository operationOrgDetailsRepository) {
    super(repo, "operation", OPERATION_NAME, operationOrgDetailsRepository);
  }

  @Override
  @SuppressWarnings("unchecked")
  public BasicDynaBean setItemBeanProperties(BasicDynaBean orderBean,
      BasicDynaBean headerInformation, String username, boolean isMultiVisitPackItem,
      Map<String, Object> orderedItemMap, BasicDynaBean operationBean) throws ParseException {

    String operId = (String) orderedItemMap.get("item_id");
    String bedType = (String) headerInformation.get(BED_TYPE);
    String ratePlanId = (String) headerInformation.get("bill_rate_plan_id");

    BasicDynaBean opChargeBean = getMasterChargesBean(operId, bedType, ratePlanId, null);
    if ((opChargeBean.get("applicable") == null
          || !((Boolean) opChargeBean.get("applicable"))) && !isMultiVisitPackItem) {
      return null;
    }

    super.setItemBeanProperties(orderBean, headerInformation, username, isMultiVisitPackItem,
        orderedItemMap, operationBean);

    setBeanValue(orderBean, OPERATION_NAME, operId);
    String patientId = (String) headerInformation.get(PATIENT_ID);
    setBeanValue(orderBean, PATIENT_ID, patientId);
    setBeanValue(orderBean, PRESCRIBED_ID, operationOrderItemRepository.getNextSequence());

    setSurgeon(orderBean, orderedItemMap);

    setTheatre(orderBean, orderedItemMap);

    setAnaesthetist(orderBean, orderedItemMap);

    setStartEndTimes(orderBean, orderedItemMap);

    setBeanValue(orderBean, "hrly", "H".equals(orderedItemMap.get("units")) ? "checked" : "");

    setBeanValue(orderBean, CONSULTANT_DOCTOR, orderedItemMap.get("prescribed_doctor_id"));

    setBeanValue(orderBean, "finalization_status",
        "F".equals(orderedItemMap.get("finalized")) ? "F" : "N");

    setConduction(opChargeBean, orderBean);
    return orderBean;
  }

  private void setAnaesthetist(BasicDynaBean orderBean, Map<String, Object> orderedItemMap) {
    if (orderedItemMap.get(PRIMARY_ANAESTHETIST) != null
        && !"".equals(orderedItemMap.get(PRIMARY_ANAESTHETIST))) {
      Map<String, Object> primaryAnaesthetist = (Map<String, Object>) orderedItemMap
          .get(PRIMARY_ANAESTHETIST);
      setBeanValue(orderBean, "anaesthetist", primaryAnaesthetist.get("anaesthetist_id"));
    } else {
      setBeanValue(orderBean, "anaesthetist", null);
    }
  }

  private void setTheatre(BasicDynaBean orderBean, Map<String, Object> orderedItemMap) {
    String theatreId = (String) ((Map<String, Object>) orderedItemMap.get(THEATRE_DETAILS))
        .get(THEATRE_ID);
    setBeanValue(orderBean, THEATRE_NAME, theatreId);
  }

  private void setSurgeon(BasicDynaBean orderBean, Map<String, Object> orderedItemList) {
    String surgeonId = (String) ((Map<String, Object>) orderedItemList.get(PRIMARY_SURGEON))
        .get("surgeon_id");
    setBeanValue(orderBean, "surgeon", surgeonId);
  }

  private void setStartEndTimes(BasicDynaBean orderBean, Map<String, Object> orderedItemList)
      throws ParseException {
    Timestamp from = DateUtil.parseTimestamp((String) orderedItemList.get("start_date_date"),
        (String) orderedItemList.get("start_date_time"));
    Timestamp to = DateUtil.parseTimestamp((String) orderedItemList.get("end_date_date"),
        (String) orderedItemList.get("end_date_time"));

    setBeanValue(orderBean, "start_datetime", from);
    setBeanValue(orderBean, "end_datetime", to);
  }

  private void setConduction(BasicDynaBean opChargeBean, BasicDynaBean orderBean) {
    boolean condApplicable = (Boolean) opChargeBean.get("conduction_applicable");
    setBeanValue(orderBean, "status", condApplicable ? "N" : "U");
  }

  /**
   * Update the status to added_to_bill if any are prescribed and ordered now.
   * 
   * @param operId the operId
   * @param patientId the patientId
   * @throws Exception the Exception
   */
  private void updateStatus(String operId, String patientId) throws Exception {
    Connection con = null;
    List<BasicDynaBean> opPreslist = new OrderDAO().getOperationPrescriptions(patientId);
    for (BasicDynaBean presBean : opPreslist) {
      if (operId.equals((String) presBean.get("operation_id"))) {
        GenericDAO opPresDao = new GenericDAO("patient_prescription");
        BasicDynaBean issuedBean = opPresDao.getBean();
        issuedBean.set("status", "O");
        opPresDao.update(con, issuedBean.getMap(), "patient_presc_id", presBean.get("pres_id"));
      }
    }
  }

  public List<BasicDynaBean> getCompletedOperation(String patientId) {

    return operationOrderItemRepository.getCompletedOperation(patientId);
  }

  public int update(BasicDynaBean bean, Map<String, Object> keys) {
    return operationOrderItemRepository.update(bean, keys);
  }

  @Override
  public List<BasicDynaBean> getCharges(Map<String, Object> paramMap) throws ParseException {
    Boolean isInsurance = (Boolean) paramMap.get("is_insurance");
    BigDecimal quantity = (BigDecimal) paramMap.get("quantity");

    return getChargesList(null, quantity, isInsurance, null, paramMap);
  }

  @Override
  public BasicDynaBean getMasterChargesBean(Object opId, String bedType, String ratePlanId,
      Integer centerId) {
    return operationsService.getOperationCharge((String) opId, bedType, ratePlanId);
  }

  @Override
  public List<BasicDynaBean> getAllBedTypeMasterChargesBean(Object opId, String ratePlanId) {
    return operationsService.getAllOperationCharge((String) opId, ratePlanId);
  }

  @SuppressWarnings("unchecked")
  @Override
  protected List<BasicDynaBean> getChargesList(BasicDynaBean itemType, BigDecimal quantity,
      Boolean isInsurance, String condDoctorId, Map<String, Object> otherParams)
      throws ParseException {
    String bedType = (String) otherParams.get(BED_TYPE);
    String orgId = (String) otherParams.get("org_id");
    String ot = (String) otherParams.get("ot");
    String id = (String) otherParams.get("id");
    String surgeon = (String) otherParams.get("surgeon");
    String anaesthetist = (String) otherParams.get("anaesthetist");
    Timestamp from = (Timestamp) otherParams.get("from_date");
    Timestamp to = (Timestamp) otherParams.get("to_date");
    String units = (String) otherParams.get("units");
    String finalized = (String) otherParams.get("finalized");
    Boolean isDoctorExcluded = null;
    if (otherParams != null && otherParams.get("item_excluded_from_doctor") != null) {
      isDoctorExcluded = isDoctorExcluded(otherParams);
    }
    String visitType = (String) otherParams.get("visit_type");

    List<String> anesthesiaTypesFromDates = (List<String>) otherParams
        .get("anesthesia_type_from_date");
    List<String> anesthesiaTypesToDates = (List<String>) otherParams.get("anesthesia_type_to_date");
    List<String> anesthesiaTypesFromTimes = (List<String>) otherParams
        .get("anesthesia_type_from_time");
    List<String> anesthesiaTypesToTimes = (List<String>) otherParams.get("anesthesia_type_to_time");
    List<String> surgeonOtDoctorIds = (List<String>) otherParams.get("surgeon_ot_doctor_id");
    List<String> surgeonOtDoctorTypes = (List<String>) otherParams.get("surgeon_ot_doctor_type");

    BasicDynaBean theatre = theatreChargesService.getTheatreChargeDetails(ot, bedType, orgId);
    BasicDynaBean operBean = getMasterChargesBean(id, bedType, orgId, null);
    BasicDynaBean surgeonBean = doctorConsultationService.getOTDoctorChargesBean(surgeon, bedType,
        orgId);
    BasicDynaBean anasthesiaTypeChargeBean = null;
    List<BasicDynaBean> anaesTypeCharges = new ArrayList<BasicDynaBean>();
    BasicDynaBean anaBean = null;
    List<BasicDynaBean> chargesList;
    if (anaesthetist != null && !anaesthetist.equals("")) {
      anaBean = doctorConsultationService.getOTDoctorChargesBean(anaesthetist, bedType, orgId);
    }
    /*
     * Theatre Charges, Surgical Assistance Charges, Surgeon Charge, Anaesthetist Charge.
     */
    chargesList = getOperationCharges(id, operBean, theatre, surgeonBean, anaBean, from, to, units,
        isInsurance, null, finalized, visitType, otherParams, false);
    /*
     * Surgeon Team Charges.
     */

    if (surgeonOtDoctorIds != null && !surgeonOtDoctorIds.isEmpty() && surgeonOtDoctorTypes != null
        && !surgeonOtDoctorTypes.isEmpty()
        && (surgeonOtDoctorIds.size() == surgeonOtDoctorTypes.size())) {
      Map<String, Object> paramMap = new HashMap<String, Object>();
      paramMap.put("is_insurance", isInsurance);
      paramMap.put("quantity", BigDecimal.ONE);
      paramMap.put(BED_TYPE, bedType);
      paramMap.put("org_id", orgId);
      paramMap.put("operation_id", id);
      paramMap.put("item_excluded_from_doctor", isDoctorExcluded);
      for (int index = 0; index < surgeonOtDoctorIds.size(); index++) {
        paramMap.put("id", surgeonOtDoctorIds.get(index));
        paramMap.put("charge_type", surgeonOtDoctorTypes.get(index));
        chargesList.addAll(doctorOrderItemService.getCharges(paramMap));
      }
    }

    /*
     * Aneaesthesia Type charges
     */
    List<String> anesthesiaTypes = (List<String>) otherParams.get("anesthesia_type");
    if (anesthesiaTypes != null && !anesthesiaTypes.isEmpty()) {
      for (int i = 0; i < anesthesiaTypes.size(); i++) {
        anasthesiaTypeChargeBean = anesthesiaTypeChargesService
            .getAnesthesiaTypeCharge(anesthesiaTypes.get(i), bedType, orgId);

        DateUtil dateUtil = new DateUtil();
        Timestamp anaeTypeFromTime = null;
        Timestamp anaeTypeToTime = null;
        anaeTypeFromTime = dateUtil.parseTheTimestamp(
            anesthesiaTypesFromDates.get(i) + " " + anesthesiaTypesFromTimes.get(i));
        anaeTypeToTime = dateUtil
            .parseTheTimestamp(anesthesiaTypesToDates.get(i) + " " + anesthesiaTypesToTimes.get(i));
        if (anasthesiaTypeChargeBean != null) {
          anaesTypeCharges = getAnaesthesiaTypeCharges(id, operBean, anaeTypeFromTime,
              anaeTypeToTime, units, isInsurance, finalized, anasthesiaTypeChargeBean, visitType,
              false, otherParams);
        }

        if (chargesList != null) {
          chargesList.addAll(anaesTypeCharges);
        } else {
          chargesList = anaesTypeCharges;
        }
      }
    }

    return chargesList;
  }

  /**
   * Returns the list of operation item details by passing there id.
   * 
   */
  @Override
  public List<BasicDynaBean> getItemDetails(List<Object> entityIdList,
      Map<String, Object> paramMap) {
    return operationOrderItemRepository.getItemDetails(entityIdList);
  }

  @Override
  public List<BasicDynaBean> getOrderedItems(Map<String, Object> parameters) {
    if (parameters.get("operation_id") == null && parameters.get("package_ref") == null) {
      List<BasicDynaBean> operationItems = operationOrderItemRepository
          .getOrderedItems((String) parameters.get("visit_id"));
      return groupOperations(operationItems);
    }
    return Collections.emptyList();
  }

  private List<BasicDynaBean> groupOperations(List<BasicDynaBean> operationItems) {
   
    String[] surgicalAssistanceAmt = new String[] { "surgical_assistance_amount",
        "surgical_assistance_tax_amount", "surgical_assistance_insurance_claim_amount",
        "surgical_assistance_sponsor_tax_amount", "surgical_assistance_discount" };
    
    String[] surgeonAmt = new String[]{"surgeon_amount","surgeon_tax_amount",
        "surgeon_insurance_claim_amount","surgeon_sponsor_tax_amount","surgeon_discount"};
    
    String[] theaterAmt = new String[]{"theatre_amount","theatre_tax_amount",
        "theatre_insurance_claim_amount","theatre_sponsor_tax_amount","theatre_discount"};
    
    Map<Integer, BasicDynaBean> map = new HashMap<>();
   
    for (BasicDynaBean bean : operationItems) {
      if (!map.containsKey(bean.get("order_id"))) {
        map.put((Integer) bean.get("order_id"), bean);
      } else {
        BasicDynaBean mapBean = map.get(bean.get("order_id"));
        if (mapBean.get("surgical_charge_id") == null
            || !mapBean.get("surgical_charge_id").equals(bean.get("surgical_charge_id"))) {
          for (int i = 0; i < surgicalAssistanceAmt.length; i++) {
            mapBean.set(surgicalAssistanceAmt[i],
                ((BigDecimal) mapBean.get(surgicalAssistanceAmt[i]))
                    .add((BigDecimal) bean.get(surgicalAssistanceAmt[i])));
          }
        }
        if (mapBean.get("surgeon_charge_id") == null
            || !mapBean.get("surgeon_charge_id").equals(bean.get("surgeon_charge_id"))) {
          for (int i = 0; i < surgeonAmt.length; i++) {
            mapBean.set(surgeonAmt[i], ((BigDecimal) mapBean.get(surgeonAmt[i]))
                .add((BigDecimal) bean.get(surgeonAmt[i])));
          }
        }
      }
    }
    
    // multiplying theater amounts with number of times its posted in bill
    for (Integer orderId : map.keySet()) {
      BasicDynaBean mapBean = map.get(orderId);
      String billNo = (String) mapBean.get("bill_no");
      Integer commonOrderId = (Integer) mapBean.get("common_order_id");
      
      Map<String,Object> filterMap = new HashMap<>();
      filterMap.put("bill_no", billNo);
      filterMap.put("charge_head", "TCOPE");
      filterMap.put("order_number", commonOrderId);
      List<BasicDynaBean> theaterCharges = billChargeService.list(filterMap, "charge_id");
      
      for (int i = 0; i < theaterAmt.length; i++) {
        mapBean.set(theaterAmt[i], ((BigDecimal) mapBean.get(theaterAmt[i]))
            .multiply(BigDecimal.valueOf(theaterCharges.size())));
      }
      mapBean.set("amount", ((BigDecimal)mapBean.get("theatre_amount"))
          .add((BigDecimal)mapBean.get("surgeon_amount"))
          .add((BigDecimal)mapBean.get("surgical_assistance_amount")));
      //Secondary operations were not adding in query HMS-28639
      mapBean.set("tax_amt", ((BigDecimal)mapBean.get("theatre_tax_amount"))
              .add((BigDecimal)mapBean.get("surgeon_tax_amount"))
              .add((BigDecimal)mapBean.get("surgical_assistance_tax_amount")));
    }
    
    List<BasicDynaBean> groupedOperations = new ArrayList<>();
    groupedOperations.addAll(map.values());
    return groupedOperations;
  }

  /**
   * get operation charges.
   * @param opnId the opnId
   * @param opn the opn
   * @param theatre the theatre
   * @param surgeonDoc the surgeonDoc
   * @param anaDoc the anaDoc
   * @param from the from
   * @param to the to
   * @param units the units
   * @param isInsurance the isInsurance
   * @param anaesthesiaTypeBean the anaesthesiaTypeBean
   * @param finalizationStatus the finalizationStatus
   * @param visitType the visitType
   * @param otherParams the otherParams
   * @return list of basic dyna bean
   */
  public List<BasicDynaBean> getOperationCharges(String opnId, BasicDynaBean opn,
      BasicDynaBean theatre, BasicDynaBean surgeonDoc, BasicDynaBean anaDoc, Timestamp from,
      Timestamp to, String units, Boolean isInsurance, BasicDynaBean anaesthesiaTypeBean,
      String finalizationStatus, String visitType, Map<String, Object> otherParams,
      Boolean multiVisitPackage) {

    List<BasicDynaBean> chargesList = new ArrayList<BasicDynaBean>();
    BasicDynaBean gprefs = genericPreferencesService.getAllPreferences();
    BasicDynaBean ipprefs = ipPreferencesService.getPreferences();

    if (gprefs.get("fixed_ot_charges").equals("Y")) {
      from = DataBaseUtil.getDateandTime();
      to = DataBaseUtil.getDateandTime();
    }

    String itemCode = null;
    String ratePlanItemCode = null;
    String operName = "";

    itemCode = (String) opn.get("operation_code");
    operName = (String) opn.get(OPERATION_NAME);
    ratePlanItemCode = (String) opn.get("item_code");
    String codeType = null;
    codeType = (String) opn.get("code_type");
    Integer insuranceCategoryId = 0;
    insuranceCategoryId = opn.get("insurance_category_id") != null
        ? (Integer) opn.get("insurance_category_id") : 0;

    /*
     * Theatre charge
     *
     * Theatre charge: this is like equipment charge: we get only a single charge amount. Depending
     * on Daily or Hourly, we get number of Days or number of Hours the equipment is used. For Days,
     * it is a straightforward calcluation: rate*numDays. For hourly, depending on the min duration
     * etc, we get an amount that is not directly proportional to the number of hours, thus qty=1
     * and rate is variable.
     */
    if (null != theatre) {
      List<BasicDynaBean> theatreCharges = getTheatreCharges(theatre, opnId, opn, from, to, units,
          isInsurance, finalizationStatus, ratePlanItemCode, codeType, ipprefs, visitType,
          multiVisitPackage, otherParams);
      chargesList.addAll(theatreCharges);
    }

    /*
     * Surgical Assistance Charge Always adding SAC irrespective of surgical assistance charge.
     */
    BigDecimal sacAmount = (BigDecimal) opn.get("surg_asstance_charge");
    int serviceSubGroupId = (Integer) opn.get("service_sub_group_id");
    BigDecimal sacDiscount =  (BigDecimal) opn.get("surg_asst_discount");

    if (multiVisitPackage) {
      Map<String, Object> surgicalAssistanceDetails = (Map<String, Object>) otherParams
          .get("surgicalAssistance");
      sacDiscount = new BigDecimal((String) surgicalAssistanceDetails.get("discount"));
      sacAmount = new BigDecimal(String.valueOf(surgicalAssistanceDetails.get("act_rate")));
    }

    BasicDynaBean sacCharge = billChargeService.setBillChargeBean("OPE", "SACOPE", sacAmount,
        BigDecimal.ONE, sacDiscount, (String) opn.get("op_id"),
        operName, (String) opn.get("dept_id"), serviceSubGroupId, insuranceCategoryId, isInsurance);
    
    if (otherParams != null && otherParams.get("item_excluded_from_doctor") != null) {
      if (isDoctorExcluded(otherParams)) {
        sacCharge.set("item_excluded_from_doctor", true);
        sacCharge.set("item_excluded_from_doctor_remarks",
            otherParams.get("item_excluded_from_doctor_remarks"));
      } else {
        sacCharge.set("item_excluded_from_doctor", false);
        sacCharge.set("item_excluded_from_doctor_remarks",
            otherParams.get("item_excluded_from_doctor_remarks"));
      }
    }

    String allowZeroClaimfor = (String) opn.get("allow_zero_claim_amount");
    if (visitType.equalsIgnoreCase(allowZeroClaimfor) || "b".equals(allowZeroClaimfor)) {
      sacCharge.set("allow_zero_claim", true);
    }

    setChargeAttributes(sacCharge, null, opnId, itemCode, ratePlanItemCode, codeType,
        (Boolean) opn.get("allow_rate_increase"), (Boolean) opn.get("allow_rate_decrease"), "N",
        from, to);
    if (opn != null || opn.get("billing_group_id") != null) {
      sacCharge.set("billing_group_id", (Integer) opn.get("billing_group_id"));
    }
    if (null != otherParams && otherParams.get("package_id") != null) {
      sacCharge.set("package_id", otherParams.get("package_id"));
    }
    chargesList.add(sacCharge);

    /*
     * Surgeon Charge = surgeon charge from operation + doctor's OT charge
     */

    if (surgeonDoc != null) {
      List<BasicDynaBean> surgeonCharges = getSurgeonCharges(opnId, opn, from, to, units,
          isInsurance, finalizationStatus, surgeonDoc, ratePlanItemCode, codeType, visitType,
          otherParams, multiVisitPackage);
      chargesList.addAll(surgeonCharges);
    }

    /*
     * Anaesthetist Charge = anaesthetist charge from operation + doctor's OT Charge
     */

    if (anaDoc != null) {
      List<BasicDynaBean> anaestiatistCharges = getAnestiatistCharges(opnId, opn, from, to,
          isInsurance, finalizationStatus, anaDoc, ratePlanItemCode, codeType, visitType,
          otherParams, multiVisitPackage);
      chargesList.addAll(anaestiatistCharges);
    }

    /*
     * Aneaesthesia charges
     */

    if (anaesthesiaTypeBean != null) {
      List<BasicDynaBean> anaesthesiaTypeCharge = getAnaesthesiaTypeCharges(opnId, opn, from, to,
          units, isInsurance, finalizationStatus, anaesthesiaTypeBean, visitType,
          multiVisitPackage, otherParams);
      chargesList.addAll(anaesthesiaTypeCharge);
    }

    return chargesList;
  }

  private List<BasicDynaBean> getTheatreCharges(BasicDynaBean theatre, String opnId,
      BasicDynaBean opn, Timestamp from, Timestamp to, String units, Boolean isInsurance,
      String finalizationStatus, String ratePlanItemCode, String codeType, BasicDynaBean ipprefs, 
      String visitType, Boolean multiVisitPackage, Map<String, Object> otherParams) {

    if (ipprefs == null) {
      ipprefs = ipPreferencesService.getPreferences();
    }

    String itemCode = null;
    String operName = "";
    String theatreCodeType = null;
    Integer serviceSubGroupId = (Integer) opn.get("service_sub_group_id");

    Integer insuranceCategoryId = 0;
    if (opn != null) {
      itemCode = (String) opn.get("operation_code");
      operName = (String) opn.get(OPERATION_NAME);
      ratePlanItemCode = (String) opn.get("item_code");
      codeType = (String) opn.get("code_type");
      theatreCodeType = ipprefs.get("theatre_charge_code_type") == null ? ""
          : (String) ipprefs.get("theatre_charge_code_type");
      insuranceCategoryId = opn.get("insurance_category_id") != null
          ? (Integer) opn.get("insurance_category_id") : 0;
    }

    String splitTheatreCharges = ipprefs.get("split_theatre_charges") != null
        ? (String) ipprefs.get("split_theatre_charges") : "N";

    List<BasicDynaBean> chargesList = new ArrayList<BasicDynaBean>();
    if (multiVisitPackage) {
      Map<String, Object> theatreDetails = (Map<String, Object>) otherParams.get("theatreDetails");
      String qty = (String) theatreDetails.get("act_quantity");
      String discount = (String) theatreDetails.get("discount");
      BigDecimal amount = new BigDecimal(String.valueOf(theatreDetails.get("act_rate")));

      BasicDynaBean thCharge = billChargeService.setBillChargeBean("OPE", "TCOPE",
             amount, new BigDecimal(qty),
              new BigDecimal(discount).multiply(new BigDecimal(qty)),
              (String) theatre.get(THEATRE_ID), operName + "/" + (String) theatre.get(THEATRE_NAME),
              null, serviceSubGroupId, insuranceCategoryId, isInsurance);
      setChargeAttributes(thCharge, null, opnId, itemCode, null, theatreCodeType,
              (Boolean) opn.get("allow_rate_increase"), (Boolean) opn.get("allow_rate_decrease"),
              finalizationStatus, from, to);
      if (otherParams.get("package_id") != null) {
        thCharge.set("package_id", otherParams.get("package_id"));
      }
      chargesList.add(thCharge);

    } else if (units.equals("D")) {
      BigDecimal rate = (BigDecimal) theatre.get("daily_charge");
      BigDecimal discount = (BigDecimal) theatre.get("daily_charge_discount");
      int qty = getDuration(from, to, "D");
      String dialyChrgItemCode = ipprefs.get("theatre_daily_charge_code") == null ? ""
          : (String) ipprefs.get("theatre_daily_charge_code");

      BasicDynaBean thCharge = billChargeService.setBillChargeBean("OPE", "TCOPE", rate,
          new BigDecimal(qty), discount.multiply(new BigDecimal(qty)),
          (String) theatre.get(THEATRE_ID), operName + "/" + (String) theatre.get(THEATRE_NAME),
          null, serviceSubGroupId, insuranceCategoryId, isInsurance);
      setChargeAttributes(thCharge, "Days", opnId, itemCode, dialyChrgItemCode, theatreCodeType,
          (Boolean) opn.get("allow_rate_increase"), (Boolean) opn.get("allow_rate_decrease"),
          finalizationStatus, from, to);
      String allowZeroClaimfor = (String) theatre.get("allow_zero_claim_amount");
      if (visitType.equalsIgnoreCase(allowZeroClaimfor) || "b".equals(allowZeroClaimfor)) {
        thCharge.set("allow_zero_claim", true);
      }
      if (theatre != null && theatre.get("billing_group_id") != null) {
        thCharge.set("billing_group_id", (Integer) theatre.get("billing_group_id"));
      }
      if (otherParams.get("package_id") != null) {
        thCharge.set("package_id", otherParams.get("package_id"));
      }
      chargesList.add(thCharge);

    } else if (splitTheatreCharges.equals("N")) {
      /*
       * Do the hourly charge calculations
       */
      BigDecimal minRate = (BigDecimal) theatre.get("min_charge");
      BigDecimal minDiscount = (BigDecimal) theatre.get("min_charge_discount");
      BigDecimal minDuration = (BigDecimal) theatre.get("min_duration");
      BigDecimal slab1Rate = (BigDecimal) theatre.get("slab_1_charge");
      BigDecimal slab1Discount = (BigDecimal) theatre.get("slab_1_charge_discount");
      BigDecimal slab1Duration = (BigDecimal) theatre.get("slab_1_threshold");
      BigDecimal incrRate = (BigDecimal) theatre.get("incr_charge");
      BigDecimal incrDiscount = (BigDecimal) theatre.get("incr_charge_discount");
      BigDecimal incrDuration = (BigDecimal) theatre.get("incr_duration");
      String slab1ChrgItemCode = ipprefs.get("theatre_slab1_charge_code") == null ? ""
          : (String) ipprefs.get("theatre_slab1_charge_code");
      String minChrgItemCode = ipprefs.get("theatre_min_charge_code") == null ? ""
          : (String) ipprefs.get("theatre_min_charge_code");

      int duration = getDuration(from, to, "H", (Integer) theatre.get("duration_unit_minutes"));
      BigDecimal rate = getDurationCharge(duration, minDuration.intValue(),
          slab1Duration.intValue(), incrDuration.intValue(), minRate, slab1Rate, incrRate, false);
      BigDecimal discount = getDurationCharge(duration, minDuration.intValue(),
          slab1Duration.intValue(), incrDuration.intValue(), minDiscount, slab1Discount,
          incrDiscount, false);

      BasicDynaBean thCharge = billChargeService.setBillChargeBean("OPE", "TCOPE", rate,
          BigDecimal.ONE, discount, (String) theatre.get(THEATRE_ID),
          operName + "/" + (String) theatre.get(THEATRE_NAME), null, serviceSubGroupId,
          insuranceCategoryId, isInsurance);

      String actRatePlanItemCode = (duration <= minDuration.intValue()) ? minChrgItemCode
          : slab1ChrgItemCode;

      setChargeAttributes(thCharge, null, opnId, itemCode, actRatePlanItemCode, theatreCodeType,
          (Boolean) opn.get("allow_rate_increase"), (Boolean) opn.get("allow_rate_decrease"),
          finalizationStatus, from, to);
      String allowZeroClaimfor = (String) theatre.get("allow_zero_claim_amount");
      if (visitType.equalsIgnoreCase(allowZeroClaimfor) || "b".equals(allowZeroClaimfor)) {
        thCharge.set("allow_zero_claim", true);
      }
      if (theatre != null && theatre.get("billing_group_id") != null) {
        thCharge.set("billing_group_id", (Integer) theatre.get("billing_group_id"));
      }
      if (otherParams.get("package_id") != null) {
        thCharge.set("package_id", otherParams.get("package_id"));
      }
      chargesList.add(thCharge);

    } else {

      BigDecimal minRate = (BigDecimal) theatre.get("min_charge");
      BigDecimal minDiscount = (BigDecimal) theatre.get("min_charge_discount");
      BigDecimal minDuration = (BigDecimal) theatre.get("min_duration");
      BigDecimal slab1Rate = (BigDecimal) theatre.get("slab_1_charge");
      BigDecimal slab1Discount = (BigDecimal) theatre.get("slab_1_charge_discount");
      BigDecimal slab1Duration = (BigDecimal) theatre.get("slab_1_threshold");
      BigDecimal incrRate = (BigDecimal) theatre.get("incr_charge");
      BigDecimal incrDiscount = (BigDecimal) theatre.get("incr_charge_discount");
      String slab1ChrgItemCode = ipprefs.get("theatre_slab1_charge_code") == null ? ""
          : (String) ipprefs.get("theatre_slab1_charge_code");
      String minChrgItemCode = ipprefs.get("theatre_min_charge_code") == null ? ""
          : (String) ipprefs.get("theatre_min_charge_code");
      String incrChrgItemCode = ipprefs.get("theatre_incr_charge_code") == null ? ""
          : (String) ipprefs.get("theatre_incr_charge_code");
      int unitSize = (Integer) theatre.get("duration_unit_minutes");
      String hrlytItemCode = "";

      int duration = getDuration(from, to, "H", unitSize);
      int hrlyDuration = 0;
      int addlnDuration = 0;

      if (duration <= minDuration.intValue()) {
        hrlyDuration = minDuration.intValue();
        hrlytItemCode = minChrgItemCode;
        addlnDuration = duration - minDuration.intValue();

      } else {
        hrlyDuration = slab1Duration.intValue();
        hrlytItemCode = slab1ChrgItemCode;
        addlnDuration = duration - slab1Duration.intValue();
      }

      BigDecimal rate = getDurationCharge(hrlyDuration, minDuration.intValue(),
          slab1Duration.intValue(), 0, minRate, slab1Rate, incrRate, false);
      BigDecimal discount = getDurationCharge(hrlyDuration, minDuration.intValue(),
          slab1Duration.intValue(), 0, minDiscount, slab1Discount, incrDiscount, false);

      BasicDynaBean thCharge = billChargeService.setBillChargeBean("OPE", "TCOPE", rate,
          BigDecimal.ONE, discount, (String) theatre.get(THEATRE_ID),
          operName + "/" + (String) theatre.get(THEATRE_NAME), null, serviceSubGroupId,
          insuranceCategoryId, isInsurance);

      setChargeAttributes(thCharge, null, opnId, itemCode, hrlytItemCode, theatreCodeType,
          (Boolean) opn.get("allow_rate_increase"), (Boolean) opn.get("allow_rate_decrease"),
          finalizationStatus, from, to);
      String allowZeroClaimfor = (String) theatre.get("allow_zero_claim_amount");
      if (visitType.equalsIgnoreCase(allowZeroClaimfor) || "b".equals(allowZeroClaimfor)) {
        thCharge.set("allow_zero_claim", true);
      }
      if (theatre != null && theatre.get("billing_group_id") != null) {
        thCharge.set("billing_group_id", (Integer) theatre.get("billing_group_id"));
      }
      if (otherParams.get("package_id") != null) {
        thCharge.set("package_id", otherParams.get("package_id"));
      }
      chargesList.add(thCharge);
      BigDecimal incrDuration = (BigDecimal) theatre.get("incr_duration");
      if ((addlnDuration > 0) && incrDuration.intValue() > 0) {

        rate = getDurationCharge(addlnDuration, 0, 0, incrDuration.intValue(), BigDecimal.ZERO,
            slab1Rate, incrRate, true);
        discount = getDurationCharge(addlnDuration, 0, 0, incrDuration.intValue(), BigDecimal.ZERO,
            slab1Discount, incrDiscount, true);

        thCharge = billChargeService.setBillChargeBean("OPE", "TCAOPE", incrRate,
            (incrRate.compareTo(BigDecimal.ZERO) > 0) ? rate.divide(incrRate) : BigDecimal.ZERO,
            discount, (String) theatre.get(THEATRE_ID),
            operName + "/" + (String) theatre.get(THEATRE_NAME), null, serviceSubGroupId,
            insuranceCategoryId, isInsurance);

        setChargeAttributes(thCharge, null, opnId, itemCode, incrChrgItemCode, theatreCodeType,
            (Boolean) opn.get("allow_rate_increase"), (Boolean) opn.get("allow_rate_decrease"),
            finalizationStatus, from, to);
        String allowZeroClaimforAddThChg = (String) theatre.get("allow_zero_claim_amount");
        if (visitType.equalsIgnoreCase(allowZeroClaimforAddThChg)
            || "b".equals(allowZeroClaimforAddThChg)) {
          thCharge.set("allow_zero_claim", true);
        }
        if (theatre != null && theatre.get("billing_group_id") != null) {
          thCharge.set("billing_group_id", (Integer) theatre.get("billing_group_id"));
        }
        if (otherParams.get("package_id") != null) {
          thCharge.set("package_id", otherParams.get("package_id"));
        }
        chargesList.add(thCharge);
      }
    }
    return chargesList;
  }

  private void setChargeAttributes(BasicDynaBean chargeBean, String actUnit, String opnId,
      String actItemCode, String actRatePlanItemCode, String codeType, Boolean allowRateIncrease,
      Boolean allowRateDecrease, String finalizationStatus, Timestamp from, Timestamp to) {
    chargeBean.set("act_unit", actUnit);
    chargeBean.set("op_id", opnId);
    chargeBean.set("act_item_code", actItemCode);
    chargeBean.set("act_rate_plan_item_code", actRatePlanItemCode);
    chargeBean.set("code_type", codeType);
    chargeBean.set("allow_rate_increase", allowRateIncrease);
    chargeBean.set("allow_rate_decrease", allowRateDecrease);
    if (!finalizationStatus.equals("N")) {
      chargeBean.set("act_remarks",
          DateUtil.formatTimestamp(from) + " to  " + DateUtil.formatTimestamp(to));
    }
    chargeBean.set("from_date", from);
    chargeBean.set("to_date", to);
  }

  /**
   * get surgeon charges.
   * @param opnId the opnId
   * @param opn the opn
   * @param from the from
   * @param to the to
   * @param units the units
   * @param isInsurance the isInsurance
   * @param finalizationStatus the finalizationStatus
   * @param surgeonDoc the surgeonDoc
   * @param actRatePlanItemCode the actRatePlanItemCode
   * @param codeType the codeType
   * @param visitType the visitType
   * @param otherParams the otherParams
   * @return list of basic dyna bean.
   */
  public List<BasicDynaBean> getSurgeonCharges(String opnId, BasicDynaBean opn, Timestamp from,
      Timestamp to, String units, Boolean isInsurance, String finalizationStatus,
      BasicDynaBean surgeonDoc, String actRatePlanItemCode, String codeType, String visitType,
      Map<String, Object> otherParams, Boolean multiVisitPackage) {
    if (opn == null || surgeonDoc == null) {
      return Collections.emptyList();
    }

    Integer insuranceCategoryId = 0;
    if (opn.get("insurance_category_id") != null) {
      insuranceCategoryId = (Integer) opn.get("insurance_category_id");
    }

    BigDecimal rate = (BigDecimal) opn.get("surgeon_charge");
    rate = rate.add((BigDecimal) surgeonDoc.get("charge"));
    String operName = (String) opn.get(OPERATION_NAME);

    BigDecimal discount = (BigDecimal) opn.get("surg_discount");
    discount = discount.add((BigDecimal) surgeonDoc.get("discount"));

    BigDecimal qty = BigDecimal.ONE;

    if (multiVisitPackage) {
      Map<String, Object> surgeonChargeDetails = (Map<String, Object>) otherParams
          .get("primarySurgeon");
      qty = new BigDecimal((String) surgeonChargeDetails.get("act_quantity"));
      discount = new BigDecimal((String) surgeonChargeDetails.get("discount"));
      rate = new BigDecimal(String.valueOf(surgeonChargeDetails.get("act_rate")));
    }

    BasicDynaBean surgeonCharge = billChargeService.setBillChargeBean("OPE", "SUOPE", rate,
            qty, discount, (String) surgeonDoc.get("doctor_id"),
        operName + "/" + (String) surgeonDoc.get("doctor_name"), (String) surgeonDoc.get("dept_id"),
        -1, insuranceCategoryId, isInsurance);

    setChargeAttributes(surgeonCharge, null, opnId, null, actRatePlanItemCode, codeType,
        (Boolean) opn.get("allow_rate_increase"), (Boolean) opn.get("allow_rate_decrease"), "N",
        from, to);
    surgeonCharge.set("payee_doctor_id", surgeonDoc.get("doctor_id"));


    if (otherParams != null && otherParams.get("item_excluded_from_doctor") != null) {
      if (isDoctorExcluded(otherParams)) {
        surgeonCharge.set("item_excluded_from_doctor", true);
        surgeonCharge.set("item_excluded_from_doctor_remarks",
            otherParams.get("item_excluded_from_doctor_remarks"));
      } else {
        surgeonCharge.set("item_excluded_from_doctor", false);
        surgeonCharge.set("item_excluded_from_doctor_remarks",
            otherParams.get("item_excluded_from_doctor_remarks"));
      }
    }

    String allowZeroClaimfor = (String) opn.get("allow_zero_claim_amount");
    if (visitType.equalsIgnoreCase(allowZeroClaimfor) || "b".equals(allowZeroClaimfor)) {
      surgeonCharge.set("allow_zero_claim", true);
    }

    if (opn != null || opn.get("billing_group_id") != null) {
      surgeonCharge.set("billing_group_id", (Integer) opn.get("billing_group_id"));
    }
    List<BasicDynaBean> chargesList = new ArrayList<>();
    if (null != otherParams && otherParams.get("package_id") != null) {
      surgeonCharge.set("package_id", otherParams.get("package_id"));
    }
    chargesList.add(surgeonCharge);
    return chargesList;
  }

  /**
   * get Anestiatist Charges.
   * @param opnId the opnId
   * @param opn the opn
   * @param from the from
   * @param to the to
   * @param isInsurance the isInsurance
   * @param finalizationStatus the finalizationStatus
   * @param anaDoc the anaDoc
   * @param ratePlanItemCode the ratePlanItemCode
   * @param codeType the codeType
   * @param visitType the visitType
   * @param otherParams the otherParams
   * @return list of basic dyna bean
   */
  public List<BasicDynaBean> getAnestiatistCharges(String opnId, BasicDynaBean opn, Timestamp from,
      Timestamp to, Boolean isInsurance, String finalizationStatus, BasicDynaBean anaDoc,
      String ratePlanItemCode, String codeType, String visitType, Map<String, Object> otherParams,
      Boolean multiVisitPackage) {

    BigDecimal rate;
    String operName = "";

    List<BasicDynaBean> chargesList = new ArrayList<>();

    Integer insuranceCategoryId = 0;
    if (opn != null && opn.get("insurance_category_id") != null) {
      insuranceCategoryId = (Integer) opn.get("insurance_category_id");
    }

    if (opn != null) {
      operName = (String) opn.get(OPERATION_NAME);
    }

    if (anaDoc != null) {
      rate = (BigDecimal) opn.get("anesthetist_charge");
      rate = rate.add((BigDecimal) anaDoc.get("charge"));
      BigDecimal discount = (BigDecimal) opn.get("anest_discount");
      discount = discount.add((BigDecimal) anaDoc.get("discount"));

      BigDecimal qty = BigDecimal.ONE;

      if (multiVisitPackage) {
        Map<String, Object> anesthetistChargeDetails = (Map<String, Object>) otherParams
            .get("primaryAnaesthetist");
        qty = new BigDecimal((String) anesthetistChargeDetails.get("act_quantity"));
        discount = new BigDecimal((String) anesthetistChargeDetails.get("discount"));
        rate = new BigDecimal(String.valueOf(anesthetistChargeDetails.get("act_rate")));
      }

      BasicDynaBean anaCharge = billChargeService.setBillChargeBean("OPE", "ANAOPE", rate,
          qty, discount, (String) anaDoc.get("doctor_id"),
          operName + "/" + (String) anaDoc.get("doctor_name"), (String) anaDoc.get("dept_id"), -1,
          insuranceCategoryId, isInsurance);

      setChargeAttributes(anaCharge, null, opnId, null, ratePlanItemCode, codeType,
          (Boolean) opn.get("allow_rate_increase"), (Boolean) opn.get("allow_rate_decrease"), "N",
          from, to);
      anaCharge.set("payee_doctor_id", anaDoc.get("doctor_id"));

      if (otherParams != null && otherParams.get("item_excluded_from_doctor") != null) {
        if (isDoctorExcluded(otherParams)) {
          anaCharge.set("item_excluded_from_doctor", true);
          anaCharge.set("item_excluded_from_doctor_remarks",
              otherParams.get("item_excluded_from_doctor_remarks"));
        } else {
          anaCharge.set("item_excluded_from_doctor", false);
          anaCharge.set("item_excluded_from_doctor_remarks",
              otherParams.get("item_excluded_from_doctor_remarks"));
        }
      }

      if (opn != null || opn.get("billing_group_id") != null) {
        anaCharge.set("billing_group_id", (Integer) opn.get("billing_group_id"));
      }
      String allowZeroClaimfor = (String) opn.get("allow_zero_claim_amount");
      if (visitType.equalsIgnoreCase(allowZeroClaimfor) || "b".equals(allowZeroClaimfor)) {
        anaCharge.set("allow_zero_claim", true);
      }
      if (null != otherParams && otherParams.get("package_id") != null) {
        anaCharge.set("package_id", otherParams.get("package_id"));
      }

      chargesList.add(anaCharge);
    }

    return chargesList;
  }

  /**
   * get anaesthesia type charges.
   * @param opnId the opnId
   * @param opn the opn
   * @param from the from
   * @param to the to
   * @param units the units
   * @param isInsurance the isInsurance
   * @param finalizationStatus the finalizationStatus
   * @param anasthesiaTypeBean the anasthesiaTypeBean
   * @param visitType the visitType
   * @return list of basic dyna bean
   */
  public List<BasicDynaBean> getAnaesthesiaTypeCharges(String opnId, BasicDynaBean opn,
      Timestamp from, Timestamp to, String units, Boolean isInsurance, String finalizationStatus,
      BasicDynaBean anasthesiaTypeBean, String visitType, Boolean multiVisitPackage,
      Map<String, Object> orderItemDetails) {

    BigDecimal rate = BigDecimal.ZERO;
    BigDecimal discount = BigDecimal.ZERO;
    String itemCode = null;
    String operName = null;
    List<BasicDynaBean> anaetypeChargeList = new ArrayList<BasicDynaBean>();

    int serviceSubGroupId = (Integer) opn.get("service_sub_group_id");

    if (opn != null) {
      itemCode = (String) opn.get("operation_code");
      operName = (String) opn.get(OPERATION_NAME);
    }

    Integer insuranceCategoryId = 0;
    if (opn != null && opn.get("insurance_category_id") != null) {
      insuranceCategoryId = (Integer) opn.get("insurance_category_id");
    }
    if (multiVisitPackage) {
      Map<String, Object> anaesthesiaTypeDetails  = ((Map<String, Object>)
              ((List) orderItemDetails.get("anaesthesiaDetails")).get(0));


      String qty = (String) anaesthesiaTypeDetails .get("act_quantity");
      discount = new BigDecimal((String) anaesthesiaTypeDetails .get("discount"));
      BigDecimal amount = new BigDecimal(String.valueOf(anaesthesiaTypeDetails .get("act_rate")));
      BasicDynaBean anaetypeCharge = billChargeService.setBillChargeBean("OPE", "ANATOPE", amount,
          new BigDecimal(qty), discount, (String) anasthesiaTypeBean.get("anesthesia_type_id"),
          operName + "/" + (String) anasthesiaTypeBean.get("anesthesia_type_name"), null,
          serviceSubGroupId, insuranceCategoryId, isInsurance);
      setChargeAttributes(anaetypeCharge, null, opnId, itemCode,
              (String) anasthesiaTypeBean.get("item_code"),
              (String) anasthesiaTypeBean.get("code_type"),
              (Boolean) opn.get("allow_rate_increase"),
              (Boolean) opn.get("allow_rate_decrease"), finalizationStatus, from, to);
      if (orderItemDetails.get("package_id") != null) {
        anaetypeCharge.set("package_id", orderItemDetails.get("package_id"));
      }
      anaetypeChargeList.add(anaetypeCharge);

    } else if (anasthesiaTypeBean != null) {
      BigDecimal minRate = (BigDecimal) anasthesiaTypeBean.get("min_charge");
      BigDecimal minDiscount = (BigDecimal) anasthesiaTypeBean.get("min_charge_discount");
      BigDecimal minDuration = (BigDecimal) anasthesiaTypeBean.get("min_duration");
      BigDecimal slab1Rate = (BigDecimal) anasthesiaTypeBean.get("slab_1_charge");
      BigDecimal slab1Discount = (BigDecimal) anasthesiaTypeBean.get("slab_1_charge_discount");
      BigDecimal slab1Duration = (BigDecimal) anasthesiaTypeBean.get("slab_1_threshold");
      BigDecimal incrRate = (BigDecimal) anasthesiaTypeBean.get("incr_charge");
      BigDecimal incrDiscount = (BigDecimal) anasthesiaTypeBean.get("incr_charge_discount");
      BigDecimal incrDuration = (BigDecimal) anasthesiaTypeBean.get("incr_duration");
      Integer baseUnit = (Integer) anasthesiaTypeBean.get("base_unit");
      Integer totalUnit = 0;

      int duration = getDuration(from, to, "H",
          (Integer) anasthesiaTypeBean.get("duration_unit_minutes"));
      if (baseUnit != null) {
        totalUnit = baseUnit + duration;
        rate = incrRate;
        discount = incrDiscount.multiply(new BigDecimal(totalUnit));
      } else {
        totalUnit = 1;
        rate = getDurationCharge(duration, minDuration.intValue(), slab1Duration.intValue(),
            incrDuration.intValue(), minRate, slab1Rate, incrRate, false);
        discount = getDurationCharge(duration, minDuration.intValue(), slab1Duration.intValue(),
            incrDuration.intValue(), minDiscount, slab1Discount, incrDiscount, false);
      }

      BasicDynaBean anaetypeCharge = billChargeService.setBillChargeBean("OPE", "ANATOPE", rate,
          new BigDecimal(totalUnit), discount,
          (String) anasthesiaTypeBean.get("anesthesia_type_id"),
          operName + "/" + (String) anasthesiaTypeBean.get("anesthesia_type_name"), null,
          serviceSubGroupId, insuranceCategoryId, isInsurance);

      setChargeAttributes(anaetypeCharge, null, opnId, itemCode,
          (String) anasthesiaTypeBean.get("item_code"),
          (String) anasthesiaTypeBean.get("code_type"), (Boolean) opn.get("allow_rate_increase"),
          (Boolean) opn.get("allow_rate_decrease"), finalizationStatus, from, to);
      if (anasthesiaTypeBean != null || anasthesiaTypeBean.get("billing_group_id") != null) {
        anaetypeCharge.set("billing_group_id",
            (Integer) anasthesiaTypeBean.get("billing_group_id"));
      }
      String allowZeroClaimfor = (String) anasthesiaTypeBean.get("allow_zero_claim_amount");
      if (visitType.equalsIgnoreCase(allowZeroClaimfor) || "b".equals(allowZeroClaimfor)) {
        anaetypeCharge.set("allow_zero_claim", true);
      }
      if (orderItemDetails.get("package_id") != null) {
        anaetypeCharge.set("package_id", orderItemDetails.get("package_id"));
      }
      anaetypeChargeList.add(anaetypeCharge);
    }
    return anaetypeChargeList;
  }

  /**
   * Return operation Ref Orders from all order item services, (doctor, service, equipment, other
   * charge).
   * 
   * @param visitId the visitId
   * @param opId the opId
   * @return list of basic dyna bean
   */
  public List<BasicDynaBean> getOperationReferenceOrders(String visitId, int opId) {
    List<BasicDynaBean> orderedItems = new ArrayList<BasicDynaBean>();
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("visit_id", visitId);
    parameters.put("operation_id", opId);
    Iterator<Class<? extends OrderItemService>> orderItemsTypesIterator = orderService
        .getOrderItemsTypes().iterator();
    while (orderItemsTypesIterator.hasNext()) {
      Class<? extends OrderItemService> orderItemsTypesClassName = orderItemsTypesIterator.next();
      Annotation[] annotations = orderItemsTypesClassName.getAnnotations();
      Order orderAnnotation = (Order) checkOrderAnnotionExist(annotations);
      if (orderAnnotation != null) {
        OrderItemService orderItem = ApplicationContextProvider.getBean(orderItemsTypesClassName);
        List<BasicDynaBean> orderedItem = orderItem.getOrderedItems(parameters);
        if (orderedItem != null) {
          orderedItems.addAll(orderedItem);
        }
      }
    }
    return orderedItems;
  }

  public List<BasicDynaBean> getOperationAnesthesiaDetails(Integer prescribedId) {
    return operationOrderItemRepository.getOperationAnaesthesiaDetails(prescribedId);
  }

  public List<BasicDynaBean> getOperationAdditionalTheaterDetails(Integer prescribedId) {
    return operationOrderItemRepository.getOperationAdditionalTheaterDetails(prescribedId);
  }

  public List<BasicDynaBean> getAdvanceOperationAnesthesiaDetails(Integer prescribedId) {
    return operationOrderItemRepository.getAdvanceOperationAnaesthesiaDetails(prescribedId);
  }

  @Override
  public BasicDynaBean getEditBean(Map<String, Object> item) throws ParseException {
    BasicDynaBean bean = getBean();
    bean.set(PRESCRIBED_ID, item.get("order_id"));
    bean.set("operation_name", item.get("item_id"));
    bean.set("remarks", item.get("remarks"));
    bean.set(CONSULTANT_DOCTOR, item.get("prescribed_doctor_id"));
    bean.set("start_datetime", DateUtil.parseTimestamp((String) item.get("start_date_date"),
        (String) item.get("start_date_time")));
    bean.set("end_datetime", DateUtil.parseTimestamp((String) item.get("end_date_date"),
        (String) item.get("end_date_time")));
    bean.set("finalization_status", "F".equals(item.get("finalized")) ? "F" : "N");
    return bean;
  }

  @Override
  public BasicDynaBean getCancelBean(Map<String, Object> item) {
    BasicDynaBean bean = getCancelBean(item.get("order_id"));
    bean.set("operation_name", item.get("item_id"));
    return bean;
  }

  @Override
  public BasicDynaBean getCancelBean(Object orderId) {
    BasicDynaBean bean = getBean();
    bean.set(PRESCRIBED_ID, orderId);
    bean.set("status", "X");
    bean.set("user_name", sessionService.getSessionAttributes().get("userId"));
    return bean;
  }

  @Override
  public BasicDynaBean toItemBean(Map<String, Object> item, BasicDynaBean headerInformation,
      String username, Map<String, List<Object>> orderedItemAuths, String[] preAuthIds,
      Integer[] preAuthModeIds, BasicDynaBean operationBean) throws ParseException {
    List<Object> newPreAuths = orderedItemAuths.get("newPreAuths");
    List<Object> newPreAuthModesList = orderedItemAuths.get("newPreAuthModesList");
    List<Object> secNewPreAuths = orderedItemAuths.get("secNewPreAuths");
    List<Object> secNewPreAuthModesList = orderedItemAuths.get("secNewPreAuthModesList");

    Object priAuthId = item.get("prior_auth_id");
    if (priAuthId == null || ((String) (priAuthId)).equals("")) {
      if (preAuthIds != null && preAuthIds.length != 0 && preAuthIds[0] != null) {
        newPreAuths.add(preAuthIds[0]);
      } else {
        newPreAuths.add(null);
      }
    } else {
      newPreAuths.add((String) (priAuthId));
    }

    Object priAuthModeId = item.get("prior_auth_mode_id");
    if (priAuthModeId == null || ((Integer) priAuthModeId) == 0) {
      if (preAuthModeIds != null && preAuthModeIds.length != 0 && preAuthModeIds[0] != null
          && preAuthModeIds[0] != 0) {
        newPreAuthModesList.add(preAuthModeIds[0]);
      } else {
        newPreAuthModesList.add(1);
      }
    } else {
      newPreAuthModesList.add(priAuthModeId);
    }

    Object secAuthId = item.get("sec_prior_auth_id");
    if (secAuthId == null || ((String) secAuthId).equals("")) {
      if (preAuthIds != null && preAuthIds.length > 1 && preAuthIds[1] != null) {
        secNewPreAuths.add(preAuthIds[1]);
      } else {
        secNewPreAuths.add(null);
      }
    } else {
      secNewPreAuths.add((String) secAuthId);
    }

    Object secAuthModeId = item.get("sec_prior_auth_mode_id");
    if (secAuthModeId == null || (Integer) secAuthModeId == 0) {
      if (preAuthModeIds != null && preAuthModeIds.length > 1 && preAuthModeIds[1] != null
          && preAuthModeIds[1] != 0) {
        secNewPreAuthModesList.add(preAuthModeIds[1]);
      } else {
        secNewPreAuthModesList.add(1);
      }
    } else {
      secNewPreAuthModesList.add(secAuthModeId);
    }

    orderedItemAuths.get("conductingDoctorList").add(null);

    List errorList = new ArrayList();
    BasicDynaBean orderedItemBean = operationOrderItemRepository.getBean();
    ConversionUtils.copyJsonToDynaBean(item, orderedItemBean, errorList, true);

    if (!errorList.isEmpty()) {
      throw new ConversionException(errorList);
    }
    Object mvp = item.get("multi_visit_package");
    Boolean isMultivisitPackage = mvp != null && !"".equals(mvp) ? Boolean.valueOf(mvp.toString())
        : false;
    orderedItemAuths.get("isMultivisitPackageList").add(isMultivisitPackage);

    orderedItemBean = setItemBeanProperties(orderedItemBean, headerInformation, username,
        isMultivisitPackage, item, null);
    return orderedItemBean;
  }

  @Override
  public int[] updateItemBeans(List<BasicDynaBean> items) {

    Map<String, Object> keys = new HashMap<>();
    List<Object> prescriptionIdsList = new ArrayList<>();
    for (BasicDynaBean item : items) {
      prescriptionIdsList.add(item.get(PRESCRIBED_ID));
    }
    keys.put(PRESCRIBED_ID, prescriptionIdsList);
    return operationOrderItemRepository.batchUpdate(items, keys);

  }

  @Override
  public void updateCancelStatusAndRefOrders(List<BasicDynaBean> items, boolean cancel,
      boolean cancelCharges, List<String> editOrCancelOrderBills, Map<String, Object> itemInfoMap)
      throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, IOException,
      ParseException {
    if (cancel) {
      String visitId = (String) itemInfoMap.get("visit_id");
      cancelOperationRefOrders(visitId, cancelCharges, items, editOrCancelOrderBills);
    }

    List<String> modulesActivated = securityService.getActivatedModules();
    if (((modulesActivated != null) && (modulesActivated.contains("mod_advanced_ot")))
        && (cancel || cancelCharges)) {
      cancelAdvancedOTSurgery(items);
    }

    // Get all operation orders Finalization status.
    List<Integer> prescribedIdsList = new ArrayList<>();
    for (BasicDynaBean item : items) {
      prescribedIdsList.add((Integer) item.get(PRESCRIBED_ID));
    }

    List<BasicDynaBean> beans = operationOrderItemRepository
        .getFinalizationStatus(prescribedIdsList);
    itemInfoMap.put("before_update_beans", beans);

  }

  /**
   * Cancel advanced OT surgery.
   *
   * @param items
   *          the items
   */
  private void cancelAdvancedOTSurgery(List<BasicDynaBean> items) {
    List<Integer> prescribedIds = new ArrayList<>();

    for (BasicDynaBean itemBean : items) {
      prescribedIds.add((Integer) itemBean.get(PRESCRIBED_ID));
    }

    operationDetailsService.cancelAdvancedOTSurgery(prescribedIds);
  }

  /**
   * Cancel operation ref orders.
   *
   * @param visitId
   *          the visit id
   * @param cancelCharges
   *          the cancel charges
   * @param items
   *          the items
   * @param editOrCancelOrderBills
   *          the edit or cancel order bills
   * @throws InvocationTargetException the InvocationTargetException
   * @throws IllegalAccessException the IllegalAccessException
   * @throws NoSuchMethodException the NoSuchMethodException
   * @throws IOException the IOException
   * @throws ParseException the ParseException
   */
  private void cancelOperationRefOrders(String visitId, boolean cancelCharges,
      List<BasicDynaBean> items, List<String> editOrCancelOrderBills) throws NoSuchMethodException,
      IllegalAccessException, InvocationTargetException, IOException, ParseException {
    List<Integer> prescribedIdList = new ArrayList<>();
    for (BasicDynaBean itemBean : items) {
      prescribedIdList.add((Integer) itemBean.get(PRESCRIBED_ID));
    }

    List<Object> params = new ArrayList<>();
    params.add(visitId);
    params.add(prescribedIdList);

    List<Class<?>> paramTypes = new ArrayList<>();
    paramTypes.add(String.class);
    paramTypes.add(List.class);
    List<BasicDynaBean> orders = orderService.callOrderItemServiceMethod("getOperationRefOrders",
        paramTypes, params);

    paramTypes = new ArrayList<>();
    paramTypes.add(Object.class);
    Map<String, List<BasicDynaBean>> cancelItemsTypeMap = new HashMap<>();
    List<BasicDynaBean> cancelBeans;
    for (BasicDynaBean order : orders) {
      params.clear();
      params.add(order.get("order_id"));
      cancelBeans = orderService.callParticularOrderItemServiceMethod("getCancelBean", paramTypes,
          params, (String) order.get("type"));
      if (cancelItemsTypeMap.get(order.get("type")) == null) {
        cancelItemsTypeMap.put((String) order.get("type"), new ArrayList<>(cancelBeans));
      } else {
        cancelItemsTypeMap.get(order.get("type")).addAll(cancelBeans);
      }
    }

    // cancel the sub-orders, also canceling charges as required.
    Map<String, Object> itemInfo = new HashMap<>();
    itemInfo.put("visit_id", visitId);
    orderService.updateOrders(cancelItemsTypeMap, true, cancelCharges, false,
        editOrCancelOrderBills, itemInfo);

  }

  @Override
  @SuppressWarnings("unchecked")
  public boolean updateFinalizableItemCharges(List<BasicDynaBean> orders,
      Map<String, Object> itemInfoMap) throws ParseException {
    List<Object> itemsMapList = (List<Object>) itemInfoMap.get(ITEMS_MAP);
    for (int index = 0; index < orders.size(); index++) {
      BasicDynaBean orderBean = orders.get(index);
      Map<String, Object> itemMap = (Map<String, Object>) itemsMapList.get(index);
      updateTheatreCharges(orderBean, itemInfoMap);
      updateAnaesthesiaTypeCharges(orderBean, itemInfoMap, itemMap);
      updateAnaesthesiaTypeDetails(itemMap);
    }
    return true;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void updateAdditionalInfo(List<BasicDynaBean> orders, boolean cancel,
      boolean cancelCharges, List<String> editOrCancelOrderBills, Map<String, Object> itemInfoMap)
      throws IOException, ParseException, NoSuchMethodException, IllegalAccessException,
      InvocationTargetException {

    if (cancel || cancelCharges || orders == null || orders.isEmpty()) {
      return;
    }

    List<Object> itemsMapList = (List<Object>) itemInfoMap.get(ITEMS_MAP);
    for (int index = 0; index < orders.size(); index++) {
      BasicDynaBean orderBean = orders.get(index);
      Map<String, Object> itemMap = (Map<String, Object>) itemsMapList.get(index);
      updateOrInsertSurgeryTeam(orderBean, itemInfoMap, editOrCancelOrderBills, itemMap);
    }

  }

  /**
   * Update or insert surgery team.
   *
   * @param orderBean
   *          the order bean
   * @param itemInfoMap
   *          the item info map
   * @param editOrCancelOrderBills the editOrCancelOrderBills
   * @param itemMap
   *          the item map
   * @throws ParseException the ParseException
   * @throws IOException the IOException
   * @throws InvocationTargetException the InvocationTargetException
   * @throws IllegalAccessException the IllegalAccessException
   * @throws NoSuchMethodException the NoSuchMethodException
   */
  @SuppressWarnings("unchecked")
  private void updateOrInsertSurgeryTeam(BasicDynaBean orderBean, Map<String, Object> itemInfoMap,
      List<String> editOrCancelOrderBills, Map<String, Object> itemMap) throws ParseException,
      NoSuchMethodException, IllegalAccessException, InvocationTargetException, IOException {
    if (itemMap == null) {
      return;
    }
    Map<String, Map<String, Object>> billsInfoMap = (Map<String, Map<String, Object>>) itemInfoMap
        .get("billsInfoMap");

    if (billsInfoMap != null) {
      String billNo = (String) itemMap.get("bill_no");
      Map<String, Object> billInfoMap = billsInfoMap.get(billNo);

      if (billInfoMap != null) {
        billInfoMap.put("operationBean", orderBean);
        billInfoMap.put("operationIdsMap", new HashMap<>());
      }
    }

    List<Object> surgeryTeamList = (List<Object>) itemMap.get(SURGERY_TEAM);
    if (itemMap.get(PRIMARY_ANAESTHETIST) != null
        && !"".equals(itemMap.get(PRIMARY_ANAESTHETIST))) {
      surgeryTeamList.add(itemMap.get(PRIMARY_ANAESTHETIST));
    }
    Map<String, Object> visitParams = new HashMap<>();
    visitParams.put("visit_id", itemInfoMap.get("visit_id"));
    doctorOrderItemService.orderItems(surgeryTeamList, visitParams, editOrCancelOrderBills,
        billsInfoMap, null);
  }

  @SuppressWarnings("unchecked")
  @Override
  public void insertPackageContent(BasicDynaBean packageBean, BasicDynaBean packageItem,
      BasicDynaBean headerInformation, String userName, Integer centerId,
      BasicDynaBean mainChargeBean, Map<String, Object> packageItemDetails, Integer index)
      throws ParseException {
    LinkedHashMap<String, Object> packageContent = ((ArrayList<LinkedHashMap<String, Object>>)
            packageItemDetails.get("package_contents")).get(index);
    if (null == packageContent.get("is_grouped_item")
        || (Boolean) packageContent.get("is_grouped_item")) {
      return;
    }
    if (mainChargeBean.get("charge_head").equals("TCOPE")) {
      String packageChargeId = mainChargeBean != null ? (String) mainChargeBean
          .get(CHARGE_ID) : null;

      String bedType = (String) headerInformation.get(BED_TYPE);
      String ratePlanId = (String) headerInformation.get("bill_rate_plan_id");
      String operationId = (Boolean) packageItemDetails.get("multi_visit_package")
          ? (String) packageContent.get("operation_id")
          : (String) packageItem.get("activity_id");

      BasicDynaBean operationChargeBean = getMasterChargesBean(operationId,
          bedType, ratePlanId, centerId);

      if (operationChargeBean != null && operationChargeBean.get("applicable") == null
          || !(Boolean) operationChargeBean.get("applicable")) {
        return;
      }
      
      BasicDynaBean opBean = orderOperationPackage(packageBean, packageItemDetails, 
          headerInformation, operationChargeBean, packageChargeId, userName, packageContent,
          packageItem);

      Map<String, Object> primaryAnaesthetist = null;
    
      if (packageContent.get(PRIMARY_ANAESTHETIST) != null
          && !"".equals(packageContent.get(PRIMARY_ANAESTHETIST))) {
        primaryAnaesthetist = (Map<String, Object>) packageItemDetails
            .get(PRIMARY_ANAESTHETIST);
      }
    

      String primaryAnaesthetistId = primaryAnaesthetist != null
          ? (String) primaryAnaesthetist.get("anaesthetist_id") : null;

      if (primaryAnaesthetistId != null && !"".equals(primaryAnaesthetistId)) {
        orderPackageAnaesthetistDoctor(packageBean, packageItemDetails, headerInformation, opBean,
            packageChargeId, userName, centerId);
      }
    }

  }

  /**
   * Order package anaesthetist doctor.
   *
   * @param packageBean
   *          the package bean
   * @param packageItemDetails
   *          the package item details
   * @param headerInformation
   *          the header information
   * @param operationBean
   *          the operation bean
   * @param operationChargeBean
   *          the operation charge bean
   * @param packageChargeId
   *          the package charge id
   * @param userName
   *          the user name
   * @throws ParseException
   *           the parse exception
   */
  private void orderPackageAnaesthetistDoctor(BasicDynaBean packageBean,
      Map<String, Object> packageItemDetails, BasicDynaBean headerInformation,
      BasicDynaBean operationBean, String packageChargeId, String userName, Integer centerId)
      throws ParseException {

    Timestamp startDateTime = DateUtil.parseTimestamp(
        (String) packageItemDetails.get("start_date_date"),
        (String) packageItemDetails.get("start_date_time"));

    BasicDynaBean anaBean = doctorOrderItemService.getBean();
    setBeanValue(anaBean, "doctor_name", operationBean.get("anaesthetist"));
    setBeanValue(anaBean, "presc_date", packageBean.get("presc_date"));
    setBeanValue(anaBean, "visited_date", startDateTime);
    setBeanValue(anaBean, "head", "ANAOPE");
    setBeanValue(anaBean, "package_ref", packageBean.get("prescription_id"));
    setBeanValue(anaBean, "operation_ref", operationBean.get(PRESCRIBED_ID));
    setBeanValue(anaBean, "ot_doc_role", "ANAOPE");

    setBeanValue(anaBean, MR_NO, (String) headerInformation.get(MR_NO));
    setBeanValue(anaBean, "common_order_id", headerInformation.get("commonorderid"));
    setBeanValue(anaBean, "username", userName);
    setBeanValue(anaBean, PATIENT_ID, headerInformation.get(PATIENT_ID));
    setBeanValue(anaBean, "consultation_id", doctorOrderItemService.getNextSequence());

    List<String> modulesActivated = securityService.getActivatedModules();
    if ((modulesActivated != null) && (modulesActivated.contains("mod_advanced_ot"))) {
      Map<String, Object> idsMap = new HashMap<>();
      copySurgeonAndAnaestiatistDetails(anaBean, false, idsMap);
    }

    doctorOrderItemService.setStatus(anaBean);

    doctorOrderItemService.insert(anaBean);
    if (packageChargeId != null) {
      doctorOrderItemService.insertPackageBillActivityCharge(anaBean, headerInformation, centerId,
          packageChargeId, null);
    }
  }

  /**
   * Order operation package.
   *
   * @param packageBean
   *          the package bean
   * @param packageItemDetails
   *          the package item details which are common to all contents
   * @param headerInformation
   *          the header information
   * @param operationChargeBean
   *          the operation charge bean
   * @param packageChargeId
   *          the package charge id
   * @param userName
   *          the user name
   * @param packageContent
   *          the data specific to the package content
   * @return the basic dyna bean
   * @throws ParseException
   *           the parse exception
   */
  @SuppressWarnings("unchecked")
  private BasicDynaBean orderOperationPackage(BasicDynaBean packageBean,
      Map<String, Object> packageItemDetails, BasicDynaBean headerInformation,
      BasicDynaBean operationChargeBean, String packageChargeId, String userName,
      LinkedHashMap<String, Object> packageContent, BasicDynaBean packageItem)
      throws ParseException {
    String primaryAnaesthetistId = null;

    if (packageContent.get(PRIMARY_ANAESTHETIST) != null
        && !"".equals(packageContent.get(PRIMARY_ANAESTHETIST))) {
      Map<String, Object> primaryAnaesthetist = (Map<String, Object>) packageContent
          .get(PRIMARY_ANAESTHETIST);
      primaryAnaesthetistId = (String) primaryAnaesthetist.get("anaesthetist_id");
    }

    String operationId = (Boolean) packageItemDetails.get("multi_visit_package")
        ? (String) packageContent.get("operation_id")
        : (String) packageItem.get("activity_id");
    BasicDynaBean opBean = getBean();
    setBeanValue(opBean, PRESCRIBED_ID, operationOrderItemRepository.getNextSequence());
    setBeanValue(opBean, CONSULTANT_DOCTOR, packageBean.get("doctor_id"));
    setBeanValue(opBean, "operation_name", operationId);
    
    Map<String, Object> theatreDetails = (Map<String, Object>) packageContent
        .get(THEATRE_DETAILS);
    setBeanValue(opBean, THEATRE_NAME, theatreDetails.get(THEATRE_ID));
    setBeanValue(opBean, "department", "");
    
    Timestamp startDateTime = DateUtil.parseTimestamp(
        (String) packageItemDetails.get("start_date_date"),
        (String) packageItemDetails.get("start_date_time"));
    setBeanValue(opBean, "start_datetime", startDateTime);
    
    Timestamp endDateTime = DateUtil.parseTimestamp(
        (String) packageItemDetails.get("end_date_date"),
        (String) packageItemDetails.get("end_date_time"));
    setBeanValue(opBean, "end_datetime", endDateTime);
    
    Map<String, Object> primarySurgeon = (Map<String, Object>) packageContent
        .get(PRIMARY_SURGEON);
    setBeanValue(opBean, "surgeon", primarySurgeon.get("surgeon_id"));
    setBeanValue(opBean, "anaesthetist", primaryAnaesthetistId);
    setBeanValue(opBean, "prescribed_date", packageBean.get("presc_date"));
    setBeanValue(opBean, "package_ref", packageBean.get("prescription_id"));

    setBeanValue(opBean, MR_NO, (String) headerInformation.get(MR_NO));
    setBeanValue(opBean, "common_order_id", headerInformation.get("commonorderid"));
    setBeanValue(opBean, "user_name", userName);
    setBeanValue(opBean, PATIENT_ID, headerInformation.get(PATIENT_ID));

    setBeanValue(opBean, "hrly", "H".equals(packageItemDetails.get("units")) ? "checked" : null);

    setConduction(operationChargeBean, opBean);

    operationOrderItemRepository.insert(opBean);

    updateOperationPrescription(opBean, (String) headerInformation.get(PATIENT_ID));

    List<String> modulesActivated = securityService.getActivatedModules();
    if ((modulesActivated != null) && (modulesActivated.contains("mod_advanced_ot"))) {
      copyOperationDetails(opBean, false, null);
    }

    if (packageChargeId != null) {
      insertPackageBillActivityCharge(opBean, packageChargeId);
    }

    return opBean;
  }

  /**
   * Insert package bill activity charge.
   *
   * @param operationBean
   *          the operation bean
   * @param packageChargeID
   *          the package charge ID
   */
  private void insertPackageBillActivityCharge(BasicDynaBean operationBean,
      String packageChargeID) {

    BasicDynaBean billActicityChargeBean = billActivityChargeService.getBillActivityChargeBean(
        packageChargeID, "OPE", "TCOPE", operationBean.get(PRESCRIBED_ID).toString(),
        (String) operationBean.get("operation_name"), (String) operationBean.get("surgeon"),
        "N".equals(operationBean.get("status")) ? "N" : "Y",
        (Timestamp) operationBean.get("start_datetime"));

    billActivityChargeService.insert(billActicityChargeBean);

  }

  /**
   * Update anaesthesia type details.
   *
   * @param orderBean
   *          the order bean
   * @param itemMap
   *          the item map
   * @throws ParseException the ParseException
   */
  private void updateAnaesthesiaTypeDetails(Map<String, Object> itemMap) throws ParseException {
    if (itemMap == null) {
      return;
    }

    List<Map<String, Object>> anaesthesiaDetailsList = (List<Map<String, Object>>) itemMap
        .get(ANAESTHESIA_DETAILS);

    if (anaesthesiaDetailsList == null || !anaesthesiaDetailsList.isEmpty()) {
      return;
    }

    List<BasicDynaBean> surgAnaesBeanList = new ArrayList<>();
    List<Object> surgeryAnaesDetIds = new ArrayList<>();
    List<BasicDynaBean> advAnaesDetBeanList = new ArrayList<>();
    List<Object> advAnaesDetIds = new ArrayList<>();

    for (Map<String, Object> anaesthesiaDetailMap : anaesthesiaDetailsList) {
      BasicDynaBean surgAnaesBean = surgeryAnaesthesiaDetService.getBean();

      Timestamp startTime = DateUtil.parseTimestamp(
          (String) anaesthesiaDetailMap.get("anaesthesia_start_date"),
          (String) anaesthesiaDetailMap.get("anaesthesia_start_time"));
      Timestamp endTime = DateUtil.parseTimestamp(
          (String) anaesthesiaDetailMap.get("anaesthesia_end_date"),
          (String) anaesthesiaDetailMap.get("anaesthesia_end_time"));
      surgAnaesBean.set("anaes_start_datetime", startTime);
      surgAnaesBean.set("anaes_end_datetime", endTime);
      surgAnaesBeanList.add(surgAnaesBean);
      surgeryAnaesDetIds.add(anaesthesiaDetailMap.get("surgery_anesthesia_details_id"));

      // This is for advanced OT module.
      Object advAnaesDetId = itemMap.get("surgery_anesthesia_details_id");
      if (advAnaesDetId != null) {
        BasicDynaBean advAnaesDetBean = opAnaesthesiaService.getBean();
        advAnaesDetBean.set("anaes_start_datetime", startTime);
        advAnaesDetBean.set("anaes_end_datetime", endTime);
        advAnaesDetBeanList.add(advAnaesDetBean);
        advAnaesDetIds.add(anaesthesiaDetailMap.get("operation_anae_detail_id"));
      }
    }
    Map<String, Object> keysMap = new HashMap<>();
    keysMap.put("surgery_anesthesia_details_id", surgeryAnaesDetIds);
    surgeryAnaesthesiaDetService.batchUpdate(surgAnaesBeanList, keysMap);

    if (!advAnaesDetBeanList.isEmpty() && !advAnaesDetIds.isEmpty()) {
      keysMap.clear();
      keysMap.put("operation_anae_detail_id", advAnaesDetIds);
      opAnaesthesiaService.batchUpdate(advAnaesDetBeanList, keysMap);
    }
  }

  /**
   * Update anaesthesia type charges.
   *
   * @param orderBean
   *          the order bean
   * @param itemInfoMap
   *          the item info map
   * @param itemMap
   *          the item map
   * @return true, if successful
   * @throws ParseException
   *           the parse exception
   */
  @SuppressWarnings("unchecked")
  private boolean updateAnaesthesiaTypeCharges(BasicDynaBean orderBean,
      Map<String, Object> itemInfoMap, Map<String, Object> itemMap) throws ParseException {

    List<Map<String, Object>> anaesthesiaDetailsList = (List<Map<String, Object>>) itemMap
        .get(ANAESTHESIA_DETAILS);

    if (anaesthesiaDetailsList == null || anaesthesiaDetailsList.isEmpty()) {
      return true;
    }

    Integer prescId = (Integer) orderBean.get(PRESCRIBED_ID);

    // These are set in updateCancelStatusAndRefOrders.
    List<BasicDynaBean> oldBeans = (List<BasicDynaBean>) itemInfoMap.get("before_update_beans");
    String finalizationStatus = "";    

    BasicDynaBean opPrescribedBean = null;
    String billNo = null;
    for (BasicDynaBean item : oldBeans) {
      if (prescId.equals(item.get(PRESCRIBED_ID))) {
        opPrescribedBean = item;
        finalizationStatus = (String) item.get("finalization_status");
        billNo = (String) item.get("bill_no");
        break;
      }
    }
    
    BasicDynaBean headerInformation = null;

    Map<String, Object> billsInfoMap = (Map<String, Object>) itemInfoMap.get("billsInfoMap");
    if (("F".equals(finalizationStatus) || "Y".equals(finalizationStatus))
        || opPrescribedBean == null || billsInfoMap == null || billsInfoMap.get(billNo) == null) {
      return false;
    } else {
      headerInformation = (BasicDynaBean) ((Map<String, Object>) billsInfoMap.get(billNo))
          .get("header_information");
      if (headerInformation == null || headerInformation.get("bill_no") == null) {
        return false;
      }
    }

    BasicDynaBean curCharge = billActivityChargeService.getChargeAndBillDetails(ACTIVITY_CODE,
        String.valueOf(prescId));

    if (curCharge == null) {
      return false;
    }

    BasicDynaBean bill = billService.getBillRatePlanAndBedType((String) curCharge.get("bill_no"));

    BasicDynaBean operationChargebean = getMasterChargesBean(opPrescribedBean.get(OPERATION_NAME),
        (String) bill.get(BED_TYPE), (String) bill.get("bill_rate_plan_id"), null);

    BasicDynaBean anaesTypeChargeBean = null;
    List<BasicDynaBean> newCharges = new ArrayList<>();
    String units = ((String) opPrescribedBean.get("hrly")).equals("checked") ? "H" : "D";

    String userName = (String) sessionService.getSessionAttributes().get("userId");
    List<String> chargeIds = new ArrayList<>();
    Map<String, Object> keys = new HashMap<>();
    List<BasicDynaBean> updateBeans = new ArrayList<>();

    for (Map<String, Object> anaesthesiaDetailMap : anaesthesiaDetailsList) {
      anaesTypeChargeBean = anesthesiaTypeChargesService.getAnesthesiaTypeCharge(
          (String) anaesthesiaDetailMap.get("anaesthesia_type"), (String) bill.get(BED_TYPE),
          (String) bill.get("bill_rate_plan_id"));
      Timestamp from = DateUtil.parseTimestamp(
          (String) anaesthesiaDetailMap.get("anaesthesia_start_date"),
          (String) anaesthesiaDetailMap.get("anaesthesia_start_time"));
      Timestamp to = DateUtil.parseTimestamp(
          (String) anaesthesiaDetailMap.get("anaesthesia_end_date"),
          (String) anaesthesiaDetailMap.get("anaesthesia_end_time"));

      List<BasicDynaBean> charges = getAnaesthesiaTypeCharges(
          (String) opPrescribedBean.get(OPERATION_NAME), operationChargebean, from, to, units,
          (Boolean) headerInformation.get("is_tpa"), (String) orderBean.get("finalization_status"),
          anaesTypeChargeBean, (String)bill.get("visit_type"), false, itemMap);

      BasicDynaBean charge = charges != null && !charges.isEmpty() ? charges.get(0) : null;

      if (charge != null) {
        if ("ANATOPE".equals(charge.get("charge_head"))) {
          charge.set(CHARGE_ID, anaesthesiaDetailMap.get(CHARGE_ID));
          charge.set("username", userName);
          if (null == anaesthesiaDetailMap.get(CONSULTATION_TYPE_ID)) {
            charge.set(CONSULTATION_TYPE_ID, 0);
          } else {
            charge.set(CONSULTATION_TYPE_ID, anaesthesiaDetailMap.get(CONSULTATION_TYPE_ID));
          } 
          charge.set("mod_time", DateUtil.getCurrentTimestamp());
          chargeIds.add((String) anaesthesiaDetailMap.get(CHARGE_ID));
          updateBeans.add(charge);
        }
        newCharges.add(charge);
      }

    }

    keys.put(CHARGE_ID, chargeIds);
    billChargeService.batchUpdate(updateBeans, keys);

    billChargeTaxService.calculateAndUpdateBillChargeTaxes(newCharges,
        (String) headerInformation.get("patient_id"));

    return true;
  }

  /**
   * Update theatre charges.
   *
   * @param orderBean
   *          the order bean
   * @param itemInfoMap
   *          the item info map
   * @return true, if successful
   */
  private boolean updateTheatreCharges(BasicDynaBean orderBean, Map<String, Object> itemInfoMap) {
    Integer prescId = (Integer) orderBean.get(PRESCRIBED_ID);

    // These are set in updateCancelStatusAndRefOrders.
    List<BasicDynaBean> oldBeans = (List<BasicDynaBean>) itemInfoMap.get("before_update_beans");
    String finalizationStatus = "";

    BasicDynaBean opPrescribedBean = null;
    String billNo = null;
    for (BasicDynaBean item : oldBeans) {
      if (prescId.equals(item.get(PRESCRIBED_ID))) {
        opPrescribedBean = item;
        finalizationStatus = (String) item.get("finalization_status");
        billNo = (String) item.get("bill_no");
        break;
      }
    }
    
    BasicDynaBean headerInformation = null;

    Map<String, Object> billsInfoMap = (Map<String, Object>) itemInfoMap.get("billsInfoMap");
    if (("F".equals(finalizationStatus) || "Y".equals(finalizationStatus))
        || opPrescribedBean == null || billsInfoMap == null || billsInfoMap.get(billNo) == null) {
      return false;
    } else {
      headerInformation = (BasicDynaBean) ((Map<String, Object>) billsInfoMap.get(billNo))
          .get("header_information");
      if (headerInformation == null || headerInformation.get("bill_no") == null) {
        return false;
      }
    }

    BasicDynaBean curCharge = billActivityChargeService.getCharge(ACTIVITY_CODE,
        String.valueOf(prescId));

    if (curCharge == null) {
      return false;
    }

    BasicDynaBean bill = billService.getBillRatePlanAndBedType((String) curCharge.get("bill_no"));

    BasicDynaBean operationChargebean = getMasterChargesBean(opPrescribedBean.get(OPERATION_NAME),
        (String) bill.get(BED_TYPE), (String) bill.get("bill_rate_plan_id"), null);

    BasicDynaBean theatreChargeBean = theatreChargesService.getTheatreChargeDetails(
        (String) opPrescribedBean.get(THEATRE_NAME), (String) bill.get(BED_TYPE),
        (String) bill.get("bill_rate_plan_id"));

    if (theatreChargeBean != null) {
      String units = ((String) opPrescribedBean.get("hrly")).equals("checked") ? "H" : "D";
      Timestamp from = (Timestamp) orderBean.get("start_datetime");
      Timestamp to = (Timestamp) orderBean.get("end_datetime");
      List<BasicDynaBean> newCharges = getOperationCharges(
          (String) opPrescribedBean.get(OPERATION_NAME), operationChargebean, theatreChargeBean,
          null, null, from, to, units, (Boolean) headerInformation.get("is_tpa"), null,
          (String) orderBean.get("finalization_status"), (String) bill.get("visit_type"),
          new HashMap<String, Object>(), false);

      BasicDynaBean newMainCharge = newCharges.get(0);

      billChargeService.copyChargeAmounts(newMainCharge, curCharge, true);
      curCharge.set("act_remarks",
          DateUtil.formatTimestamp(from) + " to " + DateUtil.formatTimestamp(to));
      Map<String, Object> keys = new HashMap<>();
      keys.put(CHARGE_ID, curCharge.get(CHARGE_ID));
      billChargeService.update(curCharge, keys);
    }
    return true;
  }

  @Override
  public String getOrderItemPrimaryKey() {
    return PRESCRIBED_ID;
  }

  @Override
  public String getOrderItemActivityCode() {
    return ACTIVITY_CODE;
  }

  @Override
  public String getPrescriptionDocKey() {
    return CONSULTANT_DOCTOR;
  }

  @Override
  public List<BasicDynaBean> insertOrders(List<Object> itemsMapsList, Boolean chargeable,
      Map<String, List<Object>> billItemAuthMap, Map<String, Object> billInfoMap,
      List<Object> patientPackageidsList) throws ParseException {
    if (itemsMapsList.isEmpty()) {
      return Collections.emptyList();
    }
    BasicDynaBean headerInformation = (BasicDynaBean) billInfoMap.get("header_information");
    String username = (String) billInfoMap.get("user_name");
    String[] preAuthIds = (String[]) billInfoMap.get("pre_auth_ids");
    Integer[] preAuthModeIds = (Integer[]) billInfoMap.get("pre_auth_mode_ids");
    BasicDynaBean bill = (BasicDynaBean) billInfoMap.get("bill");
    Integer centerId = (Integer) sessionService.getSessionAttributes().get("centerId");
    int[] planIds = (int[]) billInfoMap.get("plan_ids");

    List<BasicDynaBean> itemList = getOrderItemBeanList(itemsMapsList, billItemAuthMap,
        headerInformation, preAuthIds, preAuthModeIds, username, null);

    List<BasicDynaBean> insertItemsList = new ArrayList<>();
    for (BasicDynaBean itemBean : itemList) {
      // Item can be null because test may not be applicable.
      if (itemBean != null) {
        insertItemsList.add(itemBean);
      }
    }
    operationOrderItemRepository.batchInsert(insertItemsList);

    updateOperationsPrescription(itemList, (String) headerInformation.get(PATIENT_ID));

    List<List<BasicDynaBean>> surgeriesAnaesthesiaList = insertSurgeryAnaesthesiaDetails(
        itemsMapsList, itemList);

    List<String> modulesActivated = securityService.getActivatedModules();
    List<Map<String, Object>> idsMapList = new ArrayList<>();
    if ((modulesActivated != null) && (modulesActivated.contains("mod_advanced_ot"))) {
      idsMapList = copyOperationDetails(itemList, true, itemsMapsList);
    }

    for (int index = 0; index < itemList.size(); index++) {
      BasicDynaBean operationBean = itemList.get(index);
      if (operationBean == null) {
        continue;
      }
      String condDoctorId = null;
      String[] finalPreAuthIds = new String[2];
      Integer[] finalPreAuthModeIds = new Integer[2];
      Map<String, Object> operationItemDetails = (Map<String, Object>) itemsMapsList.get(index);

      List<BasicDynaBean> surgAnaesDetailsList = surgeriesAnaesthesiaList.get(index);

      setPreAuthIdAndMode(finalPreAuthIds, finalPreAuthModeIds, billItemAuthMap, index);
      if (null != bill) {
        boolean isMVP = (Boolean) operationItemDetails.get("multi_visit_package");
        List<BasicDynaBean> chargesList = insertOrderItemCharges(chargeable,
            headerInformation, operationBean, bill, finalPreAuthIds,
            finalPreAuthModeIds, planIds, condDoctorId, ACTIVITY_CODE, centerId,
                isMVP, operationItemDetails,
                surgAnaesDetailsList);
        if (isMVP) {
          insertMVPCharges(operationBean, operationItemDetails, chargesList, index);
        }
      }
    }

    insertSurgeryTeam(itemsMapsList, itemList, billItemAuthMap, billInfoMap, idsMapList);

    return itemList;
  }

  /**
   * Insert surgery team.
   *
   * @param itemsMapsList
   *          the items maps list
   * @param itemList
   *          the item list
   * @param billInfoMap the billInfoMap
   * @param billItemAuthMap the billItemAuthMap
   * @param idsMapList the idsMapList
   * @throws ParseException the ParseException
   */
  @SuppressWarnings("unchecked")
  private void insertSurgeryTeam(List<Object> itemsMapsList, List<BasicDynaBean> itemList,
      Map<String, List<Object>> itemAuthMap, Map<String, Object> billInfoMap,
      List<Map<String, Object>> idsMapList) throws ParseException {
    if (itemsMapsList.size() != itemList.size() || itemList.size() != idsMapList.size()) {
      return;
    }

    List<Object> surgeryTeam = null;
    BasicDynaBean itemBean = null;
    Map<String, Object> idsMap = null;
    for (int index = 0; index < itemsMapsList.size(); index++) {
      Map<String, Object> itemMap = (Map<String, Object>) itemsMapsList.get(index);
      itemBean = itemList.get(index);

      if (itemBean != null && itemMap.get(SURGERY_TEAM) != null) {
        idsMap = idsMapList.get(index);
        // Needed in ordering surgery Team.
        billInfoMap.put("operationBean", itemBean);
        billInfoMap.put("operationIdsMap", idsMap);
        billInfoMap.put("mvp_details", itemMap);

        surgeryTeam = (List<Object>) itemMap.get(SURGERY_TEAM);
        if (itemMap.get(PRIMARY_ANAESTHETIST) != null
            && !"".equals(itemMap.get(PRIMARY_ANAESTHETIST))) {
          surgeryTeam.add(itemMap.get(PRIMARY_ANAESTHETIST));
        }
        doctorOrderItemService.insertOrders(surgeryTeam, true, itemAuthMap, billInfoMap, null);

      }
    }
    // Remove it from the map. As items ordered seperately should not receive operationBeanList.
    billInfoMap.remove("operationBean");
    billInfoMap.remove("operationIdsMap");
    billInfoMap.remove("mvp_details");

  }

  /**
   * Copy operation details.
   *
   * @param itemList
   *          the item list
   * @param chargeable
   *          the chargeable
   * @param itemsMapsList
   *          the items maps list
   * @throws ParseException the ParseException
   */
  @SuppressWarnings("unchecked")
  private List<Map<String, Object>> copyOperationDetails(List<BasicDynaBean> itemList,
      boolean chargeable, List<Object> itemsMapsList) throws ParseException {
    if (itemsMapsList.size() != itemList.size()) {
      return Collections.emptyList();
    }

    List<Map<String, Object>> idsMapList = new ArrayList<>();
    for (int index = 0; index < itemsMapsList.size(); index++) {
      Map<String, Object> itemMap = (Map<String, Object>) itemsMapsList.get(index);
      BasicDynaBean itemBean = itemList.get(index);
      if (itemBean != null) {
        idsMapList.add(copyOperationDetails(itemBean, chargeable, itemMap));
      } else {
        idsMapList.add(null);
      }
    }

    return idsMapList;
  }

  /**
   * Copy operation details.
   *
   * @param itemMap
   *          the item map
   * @param chargeable
   *          the chargeable
   * @param itemBean
   *          the item bean
   * @return the list
   * @throws ParseException the ParseException
   */
  @SuppressWarnings("unchecked")
  private Map<String, Object> copyOperationDetails(BasicDynaBean orderBean, boolean chargeable,
      Map<String, Object> itemMap) throws ParseException {

    Map<String, Object> returnMap = new HashMap<>();
    BasicDynaBean operationDetailsBean = operationDetailsService.getBean();
    setOperationDetailsBean(operationDetailsBean, orderBean);
    Integer operationDetailsId = (Integer) operationDetailsBean.get(OPERATION_DETAILS_ID);
    returnMap.put(OPERATION_DETAILS_ID, operationDetailsId);

    operationDetailsService.insert(operationDetailsBean);

    BasicDynaBean operationProcedureBean = operationProceduresService.getBean();
    setOperationProcedureBean(operationProcedureBean, orderBean, operationDetailsId);
    Integer operationProcId = (Integer) operationProcedureBean.get(OPERATION_PROC_ID);
    returnMap.put(OPERATION_PROC_ID, operationProcId);

    operationProceduresService.insert(operationProcedureBean);

    if (chargeable) {
      BasicDynaBean opBillableResourceBean = null;
      List<BasicDynaBean> opBillableResBeanList = new ArrayList<>();

      if (orderBean.get(THEATRE_NAME) != null && !"".equals(orderBean.get(THEATRE_NAME))) {
        opBillableResourceBean = opBillableResService.getBean();
        setOpBillableResourceBean(opBillableResourceBean, (String) orderBean.get(THEATRE_NAME),
            "THEAT", operationProcId, "Y");
        opBillableResBeanList.add(opBillableResourceBean);
      }

      List<Map<String, Object>> anaesthesiaDetailsList = (List<Map<String, Object>>) itemMap
          .get(ANAESTHESIA_DETAILS);
      anaesthesiaDetailsList = anaesthesiaDetailsList != null ? anaesthesiaDetailsList
          : Collections.EMPTY_LIST;

      List<BasicDynaBean> opAnTypeDetailsBeanList = new ArrayList<>();

      for (Map<String, Object> anaesthesiaDetailMap : anaesthesiaDetailsList) {
        BasicDynaBean opAnTypeDetailsBean = opAnaesthesiaService.getBean();
        setOpAnTypeDetailBean(opAnTypeDetailsBean, anaesthesiaDetailMap, operationDetailsId);
        Integer opAnaeDetailId = (Integer) opAnTypeDetailsBean.get("operation_anae_detail_id");
        opAnTypeDetailsBeanList.add(opAnTypeDetailsBean);

        opBillableResourceBean = opBillableResService.getBean();
        setOpBillableResourceBean(opBillableResourceBean, opAnaeDetailId.toString(), "ANAE",
            operationProcId, "Y");
        opBillableResBeanList.add(opBillableResourceBean);
      }

      opAnaesthesiaService.batchInsert(opAnTypeDetailsBeanList);

      opBillableResService.batchInsert(opBillableResBeanList);

    }

    if (orderBean.get("surgeon") != null && !"".equals(orderBean.get("surgeon"))) {
      String surgeonId = (String) orderBean.get("surgeon");
      orderBean = doctorOrderItemService.getBean();
      orderBean.set("doctor_name", surgeonId);
      orderBean.set("head", "SUOPE");
      orderBean.set("ot_doc_role","SUOPE");

      copySurgeonAndAnaestiatistDetails(orderBean, chargeable, returnMap);
    }

    return returnMap;
  }

  /**
   * Copy surgeon and anaestiatist details.
   *
   * @param orderBean
   *          the order bean
   * @param chargeable
   *          the chargeable
   * @param idsMap
   *          the ids map
   */
  public void copySurgeonAndAnaestiatistDetails(BasicDynaBean orderBean, boolean chargeable,
      Map<String, Object> idsMap) {
    Integer operationDetailsId = (Integer) idsMap.get(OPERATION_DETAILS_ID);
    Integer operationProcId = (Integer) idsMap.get(OPERATION_PROC_ID);

    if (operationDetailsId == null && orderBean.get("operation_ref") != null
        && !"".equals(orderBean.get("operation_ref"))) {
      Integer opRef = (Integer) orderBean.get("operation_ref");
      BasicDynaBean operationDetailsBean = operationDetailsService
          .getPrimaryOperationDetailsByPrescribedId(opRef);
      operationDetailsId = (Integer) operationDetailsBean.get(OPERATION_DETAILS_ID);
      BasicDynaBean opProcBean = operationProceduresService.findByKey(OPERATION_DETAILS_ID,
          operationDetailsId);
      operationProcId = (Integer) opProcBean.get(OPERATION_PROC_ID);
    }

    String opertionSpeciality = getOperationSpeciality((String) orderBean.get("ot_doc_role"));

    if (null != opertionSpeciality) {
      BasicDynaBean operationTeamBean = operationTeamsService.getBean();
      Integer operationTeamId = operationTeamsService.getNextSequence();
      operationTeamBean.set("operation_team_id", operationTeamId);
      operationTeamBean.set(OPERATION_DETAILS_ID, operationDetailsId);
      operationTeamBean.set("resource_id", orderBean.get("doctor_name"));
      operationTeamBean.set("operation_speciality", opertionSpeciality);
  
      operationTeamsService.insert(operationTeamBean);

      if (chargeable && orderBean.get("doctor_name") != null
          && !"".equals(orderBean.get("doctor_name"))) {
        BasicDynaBean opBillableResourceBean = opBillableResService.getBean();
        setOpBillableResourceBean(opBillableResourceBean, operationTeamId.toString(), "TEAM",
            operationProcId, "Y");
  
        opBillableResService.insert(opBillableResourceBean);
      }
    }

    idsMap.put(OPERATION_DETAILS_ID, operationDetailsId);
    idsMap.put(OPERATION_PROC_ID, operationProcId);
  }

  /**
   * Gets the operation speciality.
   *
   * @param head
   *          the head
   * @return the operation speciality
   */
  private String getOperationSpeciality(String head) {
    if ("SUOPE".equals(head)) {
      return "SU";
    } else if ("ASUOPE".equals(head)) {
      return "ASU";
    } else if ("ANAOPE".equals(head)) {
      return "AN";
    } else if ("AANOPE".equals(head)) {
      return "ASAN";
    } else if ("COSOPE".equals(head)) {
      return "COSOPE";
    }
    return null;
  }

  /**
   * Sets the op an type detail bean.
   *
   * @param opAnTypeDetailsBean
   *          the op an type details bean
   * @param anaesthesiaDetailMap
   *          the anaesthesia detail map
   * @param operationDetailsId
   *          the operation details id
   * @throws ParseException
   *           the parse exception
   */
  private void setOpAnTypeDetailBean(BasicDynaBean opAnTypeDetailsBean,
      Map<String, Object> anaesthesiaDetailMap, Integer operationDetailsId) throws ParseException {
    opAnTypeDetailsBean.set("operation_anae_detail_id", opAnaesthesiaService.getNextSequence());
    opAnTypeDetailsBean.set(OPERATION_DETAILS_ID, operationDetailsId);
    opAnTypeDetailsBean.set("anesthesia_type",
        (String) anaesthesiaDetailMap.get("anaesthesia_type"));
    opAnTypeDetailsBean.set("anaes_start_datetime",
        DateUtil.parseTimestamp((String) anaesthesiaDetailMap.get("anaesthesia_start_date"),
            (String) anaesthesiaDetailMap.get("anaesthesia_start_time")));
    opAnTypeDetailsBean.set("anaes_end_datetime",
        DateUtil.parseTimestamp((String) anaesthesiaDetailMap.get("anaesthesia_end_date"),
            (String) anaesthesiaDetailMap.get("anaesthesia_end_time")));

  }

  /**
   * Sets the op billable resource bean.
   *
   * @param opBillableResourceBean
   *          the op billable resource bean
   * @param resourceId
   *          the resource id
   * @param resourceType
   *          the resource type
   * @param operationProcId
   *          the operation proc id
   * @param billable
   *          the billable
   */
  private void setOpBillableResourceBean(BasicDynaBean opBillableResourceBean, String resourceId,
      String resourceType, Integer operationProcId, String billable) {
    opBillableResourceBean.set("operation_billable_resources_id",
        opBillableResService.getNextSequence());
    opBillableResourceBean.set("resource_id", resourceId);
    opBillableResourceBean.set("resource_type", resourceType);
    opBillableResourceBean.set(OPERATION_PROC_ID, operationProcId);
    opBillableResourceBean.set("billable", billable);
  }

  /**
   * Sets the operation procedure bean.
   *
   * @param operationProcedureBean
   *          the operation procedure bean
   * @param orderBean
   *          the order bean
   * @param operationDetailsId
   *          the operation details id
   */
  private void setOperationProcedureBean(BasicDynaBean operationProcedureBean,
      BasicDynaBean orderBean, Integer operationDetailsId) {
    operationProcedureBean.set(OPERATION_PROC_ID, operationProceduresService.getNextSequence());
    operationProcedureBean.set(OPERATION_DETAILS_ID, operationDetailsId);
    operationProcedureBean.set("oper_priority", "P");
    operationProcedureBean.set("operation_id", orderBean.get(OPERATION_NAME));
    operationProcedureBean.set(PRESCRIBED_ID, orderBean.get(PRESCRIBED_ID));
  }

  /**
   * Sets the operation details bean.
   *
   * @param operationDetailsBean
   *          the operation details bean
   * @param orderBean
   *          the order bean
   */
  private void setOperationDetailsBean(BasicDynaBean operationDetailsBean,
      BasicDynaBean orderBean) {
    operationDetailsBean.set(OPERATION_DETAILS_ID, operationDetailsService.getNextSequence());
    operationDetailsBean.set(MR_NO, orderBean.get(MR_NO));
    operationDetailsBean.set(PATIENT_ID, orderBean.get(PATIENT_ID));
    operationDetailsBean.set(THEATRE_ID, orderBean.get(THEATRE_NAME));
    operationDetailsBean.set("operation_status",
        orderBean.get("status").equals("N") ? "P" : orderBean.get("status"));
    operationDetailsBean.set("surgery_start", orderBean.get("start_datetime"));
    operationDetailsBean.set("surgery_end", orderBean.get("end_datetime"));
    operationDetailsBean.set("prescribing_doctor", orderBean.get(CONSULTANT_DOCTOR));
    operationDetailsBean.set("order_remarks", orderBean.get("remarks"));
    operationDetailsBean.set("added_to_bill", "Y");
    String chargeType = (orderBean.get("hrly") != null
        && !orderBean.get("hrly").toString().isEmpty())
            ? orderBean.get("hrly").equals("checked") ? "H" : "D" : null;
    operationDetailsBean.set("charge_type", chargeType);
  }

  /**
   * Insert surgery anaesthesia details.
   *
   * @param itemsMapsList
   *          the items maps list
   * @param itemList
   *          the item list
   * @throws ParseException
   *           the parse exception
   */
  @SuppressWarnings("unchecked")
  private List<List<BasicDynaBean>> insertSurgeryAnaesthesiaDetails(List<Object> itemsMapsList,
      List<BasicDynaBean> itemList) throws ParseException {

    if (itemsMapsList.size() != itemList.size()) {
      return Collections.emptyList();
    }

    List<List<BasicDynaBean>> surgeriesAnaesthesiaList = new ArrayList<>();
    List<BasicDynaBean> allBeans = new ArrayList<>();
    for (int index = 0; index < itemsMapsList.size(); index++) {
      Map<String, Object> itemMap = (Map<String, Object>) itemsMapsList.get(index);
      BasicDynaBean itemBean = itemList.get(index);
      if (itemBean != null) {
        List<BasicDynaBean> anaesthesiaList = getSurgeryAnaesthesiaDetails(itemBean, itemMap);
        surgeriesAnaesthesiaList.add(anaesthesiaList);
        allBeans.addAll(anaesthesiaList);
      } else {
        surgeriesAnaesthesiaList.add(null);
      }

    }

    surgeryAnaesthesiaDetService.batchInsert(allBeans);
    return surgeriesAnaesthesiaList;
  }

  /**
   * Gets the surgery anaesthesia details.
   *
   * @param itemMap
   *          the item map
   * @param itemBean
   *          the item bean
   * @return the surgery anaesthesia details
   * @throws ParseException
   *           the parse exception
   */
  @SuppressWarnings("unchecked")
  private List<BasicDynaBean> getSurgeryAnaesthesiaDetails(BasicDynaBean itemBean,
      Map<String, Object> itemMap) throws ParseException {

    List<Map<String, Object>> anaesthesiaDetailsList = (List<Map<String, Object>>) itemMap
        .get(ANAESTHESIA_DETAILS);
    anaesthesiaDetailsList = anaesthesiaDetailsList != null ? anaesthesiaDetailsList
        : Collections.EMPTY_LIST;
    List<BasicDynaBean> anaesthesiaBeanList = new ArrayList<>();
    for (int index = 0; index < anaesthesiaDetailsList.size(); index++) {
      Map<String, Object> anaesthesiaDetailMap = anaesthesiaDetailsList.get(index);
      BasicDynaBean bean = surgeryAnaesthesiaDetService.getBean();
      int id = surgeryAnaesthesiaDetService.getNextSequence();
      bean.set("surgery_anesthesia_details_id", id);
      bean.set(PRESCRIBED_ID, (Integer) itemBean.get(PRESCRIBED_ID));
      bean.set("anesthesia_type", (String) anaesthesiaDetailMap.get("anaesthesia_type"));
      bean.set("anaes_start_datetime",
          DateUtil.parseTimestamp((String) anaesthesiaDetailMap.get("anaesthesia_start_date"),
              (String) anaesthesiaDetailMap.get("anaesthesia_start_time")));
      bean.set("anaes_end_datetime",
          DateUtil.parseTimestamp((String) anaesthesiaDetailMap.get("anaesthesia_end_date"),
              (String) anaesthesiaDetailMap.get("anaesthesia_end_time")));
      anaesthesiaBeanList.add(bean);
    }
    return anaesthesiaBeanList;
  }

  private void updateOperationPrescription(BasicDynaBean operationBean, String patientId) {
    List<BasicDynaBean> operationList = new ArrayList<>();
    operationList.add(operationBean);
    updateOperationsPrescription(operationList, patientId);
  }

  /**
   * Update operations prescription.
   *
   * @param operationList
   *          the operation list
   * @param patientId
   *          the patient id
   */
  private void updateOperationsPrescription(List<BasicDynaBean> operationList, String patientId) {
    List<String> operationIds = new ArrayList<>();
    for (BasicDynaBean operationBean : operationList) {
      if (operationBean != null) {
        operationIds.add((String) operationBean.get(OPERATION_NAME));
      }
    }

    List<BasicDynaBean> opPrescribedList = patientOpPrescService.getPrescriptions(patientId);
    List<BasicDynaBean> updateBeanList = new ArrayList<>();
    List<Object> prescriptionIdsList = new ArrayList<>();
    Map<String, Object> keys = new HashMap<>();

    for (BasicDynaBean prescBean : opPrescribedList) {
      if (operationIds.contains((String) prescBean.get("operation_id"))) {
        BasicDynaBean issuedBean = patientPrescRepo.getBean();
        issuedBean.set("status", "O");
        issuedBean.set("username", sessionService.getSessionAttributes().get("userId"));
        updateBeanList.add(issuedBean);
        prescriptionIdsList.add(prescBean.get("pres_id"));
      }
    }
    keys.put("patient_presc_id", prescriptionIdsList);
    patientPrescRepo.batchUpdate(updateBeanList, keys);
  }

  @Override
  public List<BasicDynaBean> insertOrderItemCharges(boolean chargeable,
      BasicDynaBean headerInformation, BasicDynaBean orderBean, BasicDynaBean bill,
      String[] preAuthIds, Integer[] preAuthModeIds, int[] planIds, String condDoctorId,
      String activityCode, Integer centerId, Boolean isMultivisitPackage,
      Map<String, Object> orderItemDetails) throws ParseException {
    return insertOrderItemCharges(chargeable, headerInformation, orderBean, bill, preAuthIds,
        preAuthModeIds, planIds, condDoctorId, activityCode, centerId, isMultivisitPackage,
        orderItemDetails, null);
  }

  /**
   * Insert order item charges.
   *
   * @param chargeable
   *          the chargeable
   * @param headerInformation
   *          the header information
   * @param orderBean
   *          the order bean
   * @param bill
   *          the bill
   * @param preAuthIds
   *          the pre auth ids
   * @param preAuthModeIds
   *          the pre auth mode ids
   * @param planIds
   *          the plan ids
   * @param condDoctorId
   *          the cond doctor id
   * @param activityCode
   *          the activity code
   * @param centerId
   *          the center id
   * @param isMultivisitPackage
   *          the is multivisit package
   * @param orderItemDetails
   *          the order item details
   * @param surgAnaesTypeDetList
   *          the surg anaes type de list
   * @return the basic dyna bean
   * @throws ParseException the ParseException
   */
  public List<BasicDynaBean> insertOrderItemCharges(boolean chargeable,
      BasicDynaBean headerInformation, BasicDynaBean orderBean, BasicDynaBean bill,
      String[] preAuthIds, Integer[] preAuthModeIds, int[] planIds, String condDoctorId,
      String activityCode, Integer centerId, Boolean isMultivisitPackage,
      Map<String, Object> orderItemDetails, List<BasicDynaBean> surgAnaesTypeDetList)
      throws ParseException {
    String bedType = (String) headerInformation.get(BED_TYPE);
    String ratePlanId = (String) headerInformation.get("bill_rate_plan_id");
    String cashRateRatePlan = (String) centerPreferencesService.getCenterPreferences(centerId)
        .get("pref_rate_plan_for_non_insured_bill");
    
    Boolean isInsurance = (Boolean) bill.get("is_tpa");

    List<BasicDynaBean> chargesList = null;
    List<BasicDynaBean> cashRateChargesList = null;
    List<BasicDynaBean> chargeTransactionList = new ArrayList<>();
    if (chargeable) {

      String operId = (String) orderBean.get(OPERATION_NAME);
      String finalizationStatus = (String) orderBean.get("finalization_status");

      String ot = (String) orderBean.get(THEATRE_NAME);
      String surgeon = (String) orderBean.get("surgeon");
      String units = ((String) orderBean.get("hrly")).equals("checked") ? "H" : "D";
      Timestamp from = (Timestamp) orderBean.get("start_datetime");
      Timestamp to = (Timestamp) orderBean.get("end_datetime");

      BasicDynaBean theatreCharge = theatreChargesService.getTheatreChargeDetails(ot, bedType,
          ratePlanId);
      BasicDynaBean cashRateTheatreCharge = theatreChargesService.getTheatreChargeDetails(ot,
          bedType, cashRateRatePlan);
      
      BasicDynaBean operationBean = getMasterChargesBean(operId, bedType, ratePlanId, null);
      BasicDynaBean cashRateOperationBean = getMasterChargesBean(operId, bedType, cashRateRatePlan,
          null);
      BasicDynaBean surgeonBean = doctorConsultationService.getOTDoctorChargesBean(surgeon, bedType,
          ratePlanId);
      BasicDynaBean cashRateSurgeonBean = doctorConsultationService.getOTDoctorChargesBean(surgeon,
          bedType, cashRateRatePlan);

      chargesList = getOperationCharges(operId, operationBean, theatreCharge, surgeonBean, null,
          from, to, units, isInsurance, null, finalizationStatus, (String) bill.get("visit_type"),
          orderItemDetails, isMultivisitPackage);

      cashRateChargesList = getOperationCharges(operId, cashRateOperationBean, theatreCharge,
          surgeonBean, null, from, to, units, isInsurance, null, finalizationStatus,
          (String) bill.get("visit_type"), orderItemDetails, isMultivisitPackage);
      
      List<Map<String, Object>> anaesthesiaDetailsList = 
          (List<Map<String, Object>>) orderItemDetails.get(ANAESTHESIA_DETAILS);
      anaesthesiaDetailsList = anaesthesiaDetailsList != null ? anaesthesiaDetailsList
          : Collections.EMPTY_LIST;

      boolean condApplicable = (Boolean) operationBean.get("conduction_applicable");
      BasicDynaBean anasthesiaTypeChargeBean = null;
      BasicDynaBean cashRateAnasthesiaTypeChargeBean = null;
      List<BasicDynaBean> anasthesiaTypeCharges = null;
      List<BasicDynaBean> cashRateAnasthesiaTypeCharges = null;
      for (int index = 0; index < anaesthesiaDetailsList.size(); index++) {
        Map<String, Object> anaesthesiaDetailMap = anaesthesiaDetailsList.get(index);
        anasthesiaTypeChargeBean = anesthesiaTypeChargesService.getAnesthesiaTypeCharge(
            (String) anaesthesiaDetailMap.get("anaesthesia_type"), bedType, ratePlanId);
        cashRateAnasthesiaTypeChargeBean = anesthesiaTypeChargesService.getAnesthesiaTypeCharge(
            (String) anaesthesiaDetailMap.get("anaesthesia_type"), bedType, cashRateRatePlan);

        anasthesiaTypeCharges = getAnaesthesiaTypeCharges(operId, operationBean,
            DateUtil.parseTimestamp((String) anaesthesiaDetailMap.get("anaesthesia_start_date"),
                (String) anaesthesiaDetailMap.get("anaesthesia_start_time")),
            DateUtil.parseTimestamp((String) anaesthesiaDetailMap.get("anaesthesia_end_date"),
                (String) anaesthesiaDetailMap.get("anaesthesia_end_time")),
            units, isInsurance, finalizationStatus, anasthesiaTypeChargeBean,
            (String) bill.get("visit_type"), isMultivisitPackage, orderItemDetails);
        cashRateAnasthesiaTypeCharges = getAnaesthesiaTypeCharges(operId, cashRateOperationBean,
            DateUtil.parseTimestamp((String) anaesthesiaDetailMap.get("anaesthesia_start_date"),
                (String) anaesthesiaDetailMap.get("anaesthesia_start_time")),
            DateUtil.parseTimestamp((String) anaesthesiaDetailMap.get("anaesthesia_end_date"),
                (String) anaesthesiaDetailMap.get("anaesthesia_end_time")),
            units, isInsurance, finalizationStatus, anasthesiaTypeChargeBean,
            (String) bill.get("visit_type"), isMultivisitPackage, orderItemDetails);

        for (BasicDynaBean charge : anasthesiaTypeCharges) {
          if (charge.get("charge_head").equals("ANATOPE") && index < surgAnaesTypeDetList.size()) {
            charge.set("surgery_anesthesia_details_id",
                surgAnaesTypeDetList.get(index).get("surgery_anesthesia_details_id"));
          }
        }
        chargesList.addAll(anasthesiaTypeCharges);
        cashRateChargesList.addAll(cashRateAnasthesiaTypeCharges);
      }
      if (isMultivisitPackage) {
        for (BasicDynaBean charge : chargesList) {
          BasicDynaBean componentDeatilBean = multiVisitRepository.findByKey("package_id", 
              (Integer) charge.get("package_id"));
          charge.set("allow_rate_increase", 
              (Boolean)componentDeatilBean.get("allow_rate_increase"));
          charge.set("allow_rate_decrease", 
              (Boolean)componentDeatilBean.get("allow_rate_decrease"));
        }
      }
      if (orderItemDetails != null && orderItemDetails.get("posted_date") != null
          && !orderItemDetails.get("posted_date").equals("")) {
        Date postedDate = formatter.parse((String) orderItemDetails.get("posted_date"));
        insertOrderBillCharges(chargesList, activityCode, condApplicable ? "N" : "Y",
            (Timestamp) orderBean.get("start_datetime"), preAuthIds, preAuthModeIds, bill,
            orderBean, planIds, (Integer) orderBean.get(PRESCRIBED_ID),
            (String) orderBean.get(CONSULTANT_DOCTOR), new Timestamp(postedDate.getTime()),
            cashChargesToTransactionCharges(cashRateChargesList));
      } else {
        insertOrderBillCharges(chargesList, activityCode, condApplicable ? "N" : "Y",
            (Timestamp) orderBean.get("start_datetime"), preAuthIds, preAuthModeIds, bill,
            orderBean, planIds, (Integer) orderBean.get(PRESCRIBED_ID),
            (String) orderBean.get(CONSULTANT_DOCTOR),
            (Timestamp) orderBean.get("prescribed_date"),
            cashChargesToTransactionCharges(cashRateChargesList));
      }
    }
    return chargesList;
  }

  /**
   * Insert MVP contents and update consumed qty.
   *
   * @param orderItemDetails
   *          the orderItemDetails
   * @param chargesList
   *          the chargesList
   * @param orderBean
   *          the orderBean
   * @param operationItemIndex
   *          the index of operation when multiple are added at the same time
   **/
  private void insertMVPCharges(BasicDynaBean orderBean, Map<String, Object> orderItemDetails,
                                List<BasicDynaBean> chargesList, int operationItemIndex) {
    List<BasicDynaBean> contentbeans = multiVisitPackageService
            .getPatientPackageContentsForOperation(orderItemDetails.get("pat_package_id"),
                    orderItemDetails.get("item_id"));
    Map<String, Object> mvpItem = (Map<String, Object>) orderItemDetails.get("mvp_item");
    // MVP which is partially consumed in 12.3 and upgraded to 12.4
    // Not inserting the data into patient package consumed table
    boolean isOldMvp = mvpItem.get("is_old_mvp") != null
            ? (boolean) mvpItem.get("is_old_mvp") : false;
    Integer contentIdRef = contentbeans.stream()
        .map(contentbean -> ((Integer) contentbean.get("content_id_ref")))
        .distinct()
        .skip(operationItemIndex)
        .findFirst().get();
    if (!isOldMvp) {
      List<BasicDynaBean> finalChargesList = chargesList;
      chargesList.forEach((billCharge) -> {
        BasicDynaBean contentBean = contentbeans.stream()
            .filter(packageContent
                -> packageContent.get("content_id_ref").equals(contentIdRef)
                  && (packageContent.get("charge_head"))
                  .equals(billCharge.get("charge_head")))
            .findFirst().get();
        multiVisitPackageService.insertPatientPackageConsumed(
                contentBean.get("patient_package_content_id"),
                contentBean.get("package_content_id"),
                orderItemDetails.get("pat_package_id"),
                orderItemDetails.get("quantity"),
                (String) billCharge.get("charge_id"),
                (Integer) orderBean.get("prescribed_id"),
                orderItemDetails.get("type"));
      });
    }
  }
  
  private List<BasicDynaBean> cashChargesToTransactionCharges(List<BasicDynaBean> cashCharges) {
    List<BasicDynaBean> transactionCharges = new ArrayList<>();
    for (BasicDynaBean cashCharge : cashCharges) {
      BasicDynaBean transactionBean = billChargeTransactionService.getBean();
      transactionBean.set("bill_charge_id", cashCharge.get("charge_id"));
      transactionBean.set("cash_rate", cashCharge.get("amount"));
      transactionCharges.add(transactionBean);
    }
    return transactionCharges;
  }


  /**
   * Expanding the operation order items to single line item. Used for Estimate Amount.
   * 
   * @param orderedItem the orderedItem
   * @param refStr the refStr
   * @return list of map
   */
  @SuppressWarnings("unchecked")
  public List<Map<String, Object>> expandOperationMap(Map<String, Object> orderedItem,
      String refStr) {
    List<Map<String, Object>> operationObject = new ArrayList<>();
    if (orderedItem.get(THEATRE_DETAILS) != null
        && !(orderedItem.get(THEATRE_DETAILS).equals(""))) {
      Map<String, Object> theatreDetailsMap = (Map<String, Object>) orderedItem
          .get(THEATRE_DETAILS);
      if (theatreDetailsMap.get(CHARGE_ID) != null) {
        operationObject.add(itemDetailExpand(theatreDetailsMap, refStr, orderedItem));
      }
    }

    List<Map<String, Object>> additionalTheatreDetails = (List<Map<String, Object>>) orderedItem
            .get(ADDITIONAL_THEATRE_DETAILS);
    if (null != additionalTheatreDetails) {
      for (Map<String, Object> map : additionalTheatreDetails) {
        if (map.get(CHARGE_ID) != null) {
          operationObject.add(itemDetailExpand(map, refStr, orderedItem));
        }
      }
    }
    if (orderedItem.get(SURGICAL_ASSISTANCE) != null
        && !orderedItem.get(SURGICAL_ASSISTANCE).equals("")) {
      Map<String, Object> surgicalAssistanceMap = (Map<String, Object>) orderedItem
          .get(SURGICAL_ASSISTANCE);
      if (surgicalAssistanceMap.get(CHARGE_ID) != null) {
        operationObject.add(itemDetailExpand(surgicalAssistanceMap, refStr, orderedItem));
      }
    }

    if (orderedItem.get(PRIMARY_SURGEON) != null && !orderedItem.get(PRIMARY_SURGEON).equals("")) {
      Map<String, Object> primarySurgeonMap = (Map<String, Object>) orderedItem
          .get(PRIMARY_SURGEON);
      if (primarySurgeonMap.get(CHARGE_ID) != null) {
        operationObject.add(itemDetailExpand(primarySurgeonMap, refStr, orderedItem));
      }
    }

    if (orderedItem.get(PRIMARY_ANAESTHETIST) != null
        && !orderedItem.get(PRIMARY_ANAESTHETIST).equals("")) {
      Map<String, Object> primaryAnaesthetistMap = (Map<String, Object>) orderedItem
          .get(PRIMARY_ANAESTHETIST);
      if (primaryAnaesthetistMap.get(CHARGE_ID) != null) {
        operationObject.add(itemDetailExpand(primaryAnaesthetistMap, refStr, orderedItem));
      }
    }

    List<Map<String, Object>> surgeonTeam = (List<Map<String, Object>>) orderedItem
        .get(SURGERY_TEAM);
    if (null != surgeonTeam) {
      for (Map<String, Object> map : surgeonTeam) {
        if (map.get(CHARGE_ID) != null) {
          operationObject.add(itemDetailExpand(map, refStr, orderedItem));
        }
      }
    }

    List<Map<String, Object>> anaesthesiaDetails = (List<Map<String, Object>>) orderedItem
        .get(ANAESTHESIA_DETAILS);
    if (null != anaesthesiaDetails) {
      for (Map<String, Object> map : anaesthesiaDetails) {
        if (map.get(CHARGE_ID) != null) {
          operationObject.add(itemDetailExpand(map, refStr, orderedItem));

        }
      }
    }

    return operationObject;
  }

  private Map<String, Object> itemDetailExpand(Map<String, Object> map, String refStr,
      Map<String, Object> orderedItem) {
    Map<String, Object> itemChargeMap = new HashMap<>();
    itemChargeMap.put(CHARGE_ID, map.get(CHARGE_ID));
    BigDecimal amount = new BigDecimal(map.get(AMOUNT).toString());
    itemChargeMap.put("new", map.get("new_order"));
    if (!"Y".equals((String) map.get("new"))) {
      amount = amount.add((new BigDecimal(map.get("tax_amount").toString())));
    }

    itemChargeMap.put(AMOUNT, amount.toString());
    itemChargeMap.put(TAX_AMT, map.get("tax_amount").toString());
    itemChargeMap.put("type", orderedItem.get("type"));
    itemChargeMap.put("discount", map.get("discount").toString());
    itemChargeMap.put(INSURANCE_CATEGORY_ID, orderedItem.get(INSURANCE_CATEGORY_ID).toString());
    if (map.get(CONSULTATION_TYPE_ID) != null) {
      itemChargeMap.put(CONSULTATION_TYPE_ID, map.get(CONSULTATION_TYPE_ID));
    } else {
      itemChargeMap.put(CONSULTATION_TYPE_ID, 0);
    }
    itemChargeMap.put("operation_id", refStr);
    itemChargeMap.put("charge_head", map.get("charge_head"));
    itemChargeMap.put("charge_group", map.get("charge_group"));
    itemChargeMap.put("act_description_id", map.get("act_description_id"));
    itemChargeMap.put("op_id", map.get("op_id"));
    if (orderedItem.get(CANCELLED) != null && !orderedItem.get(CANCELLED).equals("")) {
      itemChargeMap.put(CANCELLED, orderedItem.get(CANCELLED));
    }
    if ((map.get(CANCELLED) != null && !map.get(CANCELLED).equals(""))) {
      itemChargeMap.put(CANCELLED, map.get(CANCELLED));
    }
    if (map.get("is_tpa") != null && !map.get("is_tpa").equals("")) {
      itemChargeMap.put("is_tpa", map.get("is_tpa"));
    }
    return itemChargeMap;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Boolean checkOperationEdited(Map<String, Object> item) {

    Object orderId = item.get("order_id");
    String cancelled = (String) item.get(CANCELLED);
    String edited = (String) item.get("edited");
    String billNo = (String) item.get("bill_no");
    Boolean returnBool = false;
    if (orderId != null && !"".equals(orderId)
        && ("I".equals(cancelled) || "IC".equals(cancelled) || "Y".equals(edited))) {
      returnBool = true;
    } else if (orderId == null || "".equals(orderId)) {
      return false;
    }

    if (item.get(PRIMARY_ANAESTHETIST) != null && !"".equals(item.get(PRIMARY_ANAESTHETIST))) {
      Map<String, Object> primaryAnaesthetist = (Map<String, Object>) item
          .get(PRIMARY_ANAESTHETIST);
      Object paOrderId = primaryAnaesthetist.get("order_id");
      String paCancelled = (String) primaryAnaesthetist.get(CANCELLED);
      String paEdited = (String) primaryAnaesthetist.get("edited");
      if (paOrderId != null && !"".equals(paOrderId)
          && ("I".equals(paCancelled) || "IC".equals(paCancelled) || "Y".equals(paEdited))) {
        returnBool = true;
      } else if (paOrderId == null || "".equals(paOrderId)) {
        primaryAnaesthetist.put("bill_no", billNo);
        returnBool = true;
      }
    }

    List<Map<String, Object>> surgeryTeam = (List<Map<String, Object>>) item.get(SURGERY_TEAM);
    for (Map<String, Object> surgeryMember : surgeryTeam) {
      Object surgeryOrderId = surgeryMember.get("order_id");
      String surgeryCancelled = (String) surgeryMember.get(CANCELLED);
      String surgeryEdited = (String) surgeryMember.get("edited");
      if (surgeryOrderId != null && !"".equals(surgeryOrderId) && ("I".equals(surgeryCancelled)
          || "IC".equals(surgeryCancelled) || "Y".equals(surgeryEdited))) {
        returnBool = true;
      } else if (surgeryOrderId == null || "".equals(surgeryOrderId)) {
        surgeryMember.put("bill_no", billNo);
        returnBool = true;
      }
    }
    return returnBool;
  }

  /**
   * expand edit operation.
   * @param orderedItem the orderedItem
   * @param itemChargesList the itemChargesList
   * @param refrenceId the refrenceId
   * @param selectedBillRatePlanMap the selectedBillRatePlanMap
   * @return list of Map
   */
  @SuppressWarnings("unchecked")
  public List<Map<String, Object>> expandEditOperationMap(Map<String, Object> orderedItem,
      List<BasicDynaBean> itemChargesList, String refrenceId,
      Map<String, Object> selectedBillRatePlanMap) {

    List<Map<String, Object>> operationObject = new ArrayList<>();
    int index = 0;

    if (orderedItem.get(THEATRE_DETAILS) != null
        && !(orderedItem.get(THEATRE_DETAILS).equals(""))) {
      Map<String, Object> theatreDetailsMap = (Map<String, Object>) orderedItem
          .get(THEATRE_DETAILS);
      if (theatreDetailsMap.get(CHARGE_ID) != null) {
        operationObject.add(editItemDetailExpand(theatreDetailsMap, refrenceId,
            itemChargesList.get(index), orderedItem, selectedBillRatePlanMap));
      }
      index++;
    }

    List<Map<String, Object>> additionalTheatreDetails = (List<Map<String, Object>>) orderedItem
        .get(ADDITIONAL_THEATRE_DETAILS);
    if (null != additionalTheatreDetails) {
      for (Map<String, Object> map : additionalTheatreDetails) {
        if (map.get(CHARGE_ID) != null) {
          operationObject.add(editItemDetailExpand(map, refrenceId, itemChargesList.get(index),
              orderedItem, selectedBillRatePlanMap));

        }
      }
      index += additionalTheatreDetails.size();
    }

    if (orderedItem.get(SURGICAL_ASSISTANCE) != null
        && !orderedItem.get(SURGICAL_ASSISTANCE).equals("")) {
      Map<String, Object> surgicalAssistanceMap = (Map<String, Object>) orderedItem
          .get(SURGICAL_ASSISTANCE);
      if (surgicalAssistanceMap.get(CHARGE_ID) != null) {
        operationObject.add(editItemDetailExpand(surgicalAssistanceMap, refrenceId,
            itemChargesList.get(index), orderedItem, selectedBillRatePlanMap));
      }
      index++;
    }

    if (orderedItem.get(PRIMARY_SURGEON) != null && !orderedItem.get(PRIMARY_SURGEON).equals("")) {
      Map<String, Object> primarySurgeonMap = (Map<String, Object>) orderedItem
          .get(PRIMARY_SURGEON);
      if (primarySurgeonMap.get(CHARGE_ID) != null) {
        operationObject.add(editItemDetailExpand(primarySurgeonMap, refrenceId,
            itemChargesList.get(index), orderedItem, selectedBillRatePlanMap));
      }
      index++;
    }

    if (orderedItem.get(PRIMARY_ANAESTHETIST) != null
        && !orderedItem.get(PRIMARY_ANAESTHETIST).equals("")) {
      Map<String, Object> primaryAnaesthetistMap = (Map<String, Object>) orderedItem
          .get(PRIMARY_ANAESTHETIST);
      if (primaryAnaesthetistMap.get(CHARGE_ID) != null) {
        operationObject.add(editItemDetailExpand(primaryAnaesthetistMap, refrenceId,
            itemChargesList.get(index), orderedItem, selectedBillRatePlanMap));
      }
      index++;
    }

    List<Map<String, Object>> surgeonTeam = (List<Map<String, Object>>) orderedItem
        .get(SURGERY_TEAM);
    if (null != surgeonTeam) {
      for (Map<String, Object> map : surgeonTeam) {
        if (map.get(CHARGE_ID) != null) {
          operationObject.add(editItemDetailExpand(map, refrenceId, itemChargesList.get(index),
              orderedItem, selectedBillRatePlanMap));
        }
      }
      index += surgeonTeam.size();
    }

    List<Map<String, Object>> anaesthesiaDetails = (List<Map<String, Object>>) orderedItem
        .get(ANAESTHESIA_DETAILS);
    if (null != anaesthesiaDetails) {
      for (Map<String, Object> map : anaesthesiaDetails) {
        if (map.get(CHARGE_ID) != null) {
          operationObject.add(editItemDetailExpand(map, refrenceId, itemChargesList.get(index),
              orderedItem, selectedBillRatePlanMap));

        }
      }
      index += anaesthesiaDetails.size();
    }

    return operationObject;

  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> editItemDetailExpand(Map<String, Object> map, String refrenceId,
      BasicDynaBean itemCharge, Map<String, Object> orderedItem,
      Map<String, Object> selectedBillRatePlanMap) {
    Map<String, Object> itemChargeMap = itemCharge.getMap();
    itemChargeMap.put("charge_id", map.get(CHARGE_ID));
    itemChargeMap.put("edited", "Y");
    itemChargeMap.put("amount", itemChargeMap.get("amount").toString());
    itemChargeMap.put(TAX_AMT, itemChargeMap.get(TAX_AMT).toString());
    itemChargeMap.put("discount", itemChargeMap.get("discount").toString());
    itemChargeMap.put("type", "Operation");
    itemChargeMap.put(INSURANCE_CATEGORY_ID, itemChargeMap.get(INSURANCE_CATEGORY_ID).toString());
    int consultationTypeId = 0;
    if (null != itemChargeMap.get(CONSULTATION_TYPE_ID)) {
      consultationTypeId = (Integer) itemChargeMap.get(CONSULTATION_TYPE_ID);
    }
    itemChargeMap.put(CONSULTATION_TYPE_ID, consultationTypeId);
    itemChargeMap.put("operation_id", refrenceId);
    itemChargeMap.put("charge_head", itemChargeMap.get("charge_head"));
    itemChargeMap.put("charge_group", itemChargeMap.get("charge_group"));
    itemChargeMap.put("act_description_id", map.get("act_description_id"));
    itemChargeMap.put("op_id", map.get("op_id"));
    if (orderedItem.get(CANCELLED) != null && !orderedItem.get(CANCELLED).equals("")) {
      itemChargeMap.put(CANCELLED, orderedItem.get(CANCELLED));
    }
    if ((map.get(CANCELLED) != null && !map.get(CANCELLED).equals(""))) {
      itemChargeMap.put(CANCELLED, map.get(CANCELLED));
    }
    if (selectedBillRatePlanMap.get("is_tpa") != null
        && !selectedBillRatePlanMap.get("is_tpa").equals("")) {
      itemChargeMap.put("is_tpa", selectedBillRatePlanMap.get("is_tpa"));
    }
    return itemChargeMap;
  }

  /**
   * Find by key.
   *
   * @param keyColumn the key column
   * @param identifier the identifier
   * @return the basic dyna bean
   */
  public BasicDynaBean findByKey(String keyColumn, Object identifier) {
    return operationOrderItemRepository.findByKey(keyColumn, identifier);
  }

  private boolean isDoctorExcluded(Map<String, Object> params) {
    if (params.get("item_excluded_from_doctor").equals("Y") || params.get(
        "item_excluded_from_doctor").equals(true)) {
      return true;
    }
    return false;
  }

}
