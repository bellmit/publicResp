package com.insta.hms.orders;

import com.bob.hms.adminmasters.organization.OrgMasterDao;
import com.bob.hms.adminmasters.services.MasterServicesDao;
import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.bob.hms.common.Preferences;
import com.bob.hms.common.RequestContext;
import com.bob.hms.otmasters.opemaster.OperationMasterDAO;
import com.bob.hms.otmasters.theamaster.TheatreMasterDAO;
import com.insta.hms.OTServices.OperationDetailsBO;
import com.insta.hms.OTServices.OperationDetailsDAO;
import com.insta.hms.Registration.PatientInsurancePlanDAO;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.adminmaster.packagemaster.PackageDAO;
import com.insta.hms.adminmasters.bedmaster.BedMasterDAO;
import com.insta.hms.advancedpackages.PatientPackagesDAO;
import com.insta.hms.billing.Bill;
import com.insta.hms.billing.BillActivityChargeDAO;
import com.insta.hms.billing.BillBO;
import com.insta.hms.billing.BillChargeClaimDAO;
import com.insta.hms.billing.BillChargeClaimTaxDAO;
import com.insta.hms.billing.BillChargeTaxBO;
import com.insta.hms.billing.BillChargeTaxDAO;
import com.insta.hms.billing.BillDAO;
import com.insta.hms.billing.BillingHelper;
import com.insta.hms.billing.ChangeRatePlanBO;
import com.insta.hms.billing.ChangeRatePlanBO.ChargeGroup;
import com.insta.hms.billing.ChargeDAO;
import com.insta.hms.billing.ChargeDTO;
import com.insta.hms.billing.DialysisOrderDao;
import com.insta.hms.billing.DiscountPlanBO;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PatientTokenGenerator;
import com.insta.hms.diagnosticmodule.common.DiagnosticsDAO;
import com.insta.hms.diagnosticmodule.laboratory.DeptTokenGeneratorDAO;
import com.insta.hms.diagnosticmodule.laboratory.PendingSamplesDAO;
import com.insta.hms.diagnosticmodule.radiology.RadiologyBO;
import com.insta.hms.diagnosticsmasters.addtest.AddTestDAOImpl;
import com.insta.hms.diagnosticsmasters.addtest.TestTATBO;
import com.insta.hms.diagnosticsmasters.addtest.TestTATDAO;
import com.insta.hms.documentpersitence.AbstractDocumentPersistence;
import com.insta.hms.ipservices.IPBedDAO;
import com.insta.hms.master.Accounting.ChargeHeadsDAO;
import com.insta.hms.master.AnaesthesiaTypeMaster.AnaesthesiaTypeChargesDAO;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.CenterPreferences.CenterPreferencesDAO;
import com.insta.hms.master.CommonChargesMaster.CommonChargesDAO;
import com.insta.hms.master.DietaryMaster.DietaryMasterDAO;
import com.insta.hms.master.DoctorMaster.DoctorMasterDAO;
import com.insta.hms.master.EquipmentMaster.EquipmentChargeDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.IPPreferences.IPPreferencesDAO;
import com.insta.hms.master.RegistrationCharges.RegistrationChargesDAO;
import com.insta.hms.master.RegistrationPreferences.RegistrationPreferencesDAO;
import com.insta.hms.master.RegistrationPreferences.RegistrationPreferencesDTO;
import com.insta.hms.medicalrecorddepartment.MRDUpdateScreenDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.DynaClass;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

// TODO: Auto-generated Javadoc
/**
 * The Class OrderBO.
 */
public class OrderBO {

  private static final BedMasterDAO bedMasterDao = new BedMasterDAO();

  private static final OperationMasterDAO operationMasterDao = new OperationMasterDAO();

  private static final TheatreMasterDAO theatreMasterDao = new TheatreMasterDAO();

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(OrderBO.class);

  /** The common order id. */
  private int commonOrderId = 0;

  /** The lab no. */
  private String labNo;

  /** The bill no. */
  private String billNo = null;

  /** The bill. */
  private BasicDynaBean bill = null;

  /** The patient id. */
  private String patientId = null;

  /** The mr no. */
  private String mrNo = null;

  /** The user name. */
  private String userName = null;

  /** The is insurance. */
  private boolean isInsurance = false;

  /** The plan id. */
  private int planId = 0;

  /** The plan ids. */
  private int[] planIds = null;

  /** The radiology no. */
  private String radiologyNo;

  /** The visit type. */
  private String visitType = null;

  /** The bed type. */
  private String bedType = null;

  /** The rate plan id. */
  private String ratePlanId = null;

  /** The package ref. */
  private int packageRef;

  /** The package id. */
  private String packageId;

  /** The operation presc ids map. */
  private HashMap operationPrescIdsMap = new HashMap();

  /** The operation bean map. */
  private HashMap<Integer, BasicDynaBean> operationBeanMap = new HashMap();

  /** The package charge id. */
  private String packageChargeId = null;

  /** The operation details id. */
  private Integer operationDetailsId;

  /** The operation proc id. */
  private Integer operationProcId;

  /** The main charge id. */
  private String mainChargeId;

  /** The bill charge dao. */
  private static GenericDAO billChargeDao = new GenericDAO("bill_charge");

  /** The test dao. */
  private static GenericDAO testDao = new GenericDAO("tests_prescribed");

  /** The service dao. */
  private static GenericDAO serviceDao = new GenericDAO("services_prescribed");

  /** The other service dao. */
  private static GenericDAO otherServiceDao = new GenericDAO("other_services_prescribed");

  /** The doctor consultation dao. */
  private static GenericDAO doctorConsultationDao = new GenericDAO("doctor_consultation");

  /** The doctors dao. */
  private static final GenericDAO doctorsDao = new GenericDAO("doctors");

  /** The equipment dao. */
  private static GenericDAO equipmentDao = new GenericDAO("equipment_prescribed");

  /** The diet dao. */
  private static GenericDAO dietDao = new GenericDAO("diet_prescribed");

  /** The operation dao. */
  private static GenericDAO operationDao = new GenericDAO("bed_operation_schedule");

  /** The pkg dao. */
  private static GenericDAO pkgDao = new GenericDAO("package_prescribed");

  /** The ip bed dao. */
  private static GenericDAO ipBedDao = new GenericDAO("ip_bed_details");

  /** The adm dao. */
  private static GenericDAO admDao = new GenericDAO("admission");

  /** The patpkg dao. */
  private static GenericDAO patpkgDao = new GenericDAO("patient_packages");

  /** The patient package dao. */
  private static GenericDAO patientPackageDao = new GenericDAO("patient_package");

  /** The patient package DAO. */
  private static PatientPackagesDAO patPackageDao = new PatientPackagesDAO();

  /** The op det DAO. */
  private static OperationDetailsDAO opDetDao = new OperationDetailsDAO();

  /** The proc DAO. */
  private static GenericDAO procDao = new GenericDAO("operation_procedures");

  /** The proc resource DAO. */
  private static GenericDAO procResourceDao = new GenericDAO("operation_team");

  /** The op det BO. */
  private OperationDetailsBO opDetBo = new OperationDetailsBO();

  /** The ope billable resources DAO. */
  private static GenericDAO opeBillableResourcesDao = new GenericDAO(
      "operation_billable_resources");

  /** The bed ope sec DAO. */
  private static GenericDAO bedOpeSecDao = new GenericDAO("bed_operation_secondary");

  /** The surgery anesthesia det DAO. */
  private static GenericDAO surgeryAnesthesiaDetDao = new GenericDAO("surgery_anesthesia_details");

  /** The operation anesthesia det DAO. */
  private static GenericDAO operationAnesthesiaDetDao = new GenericDAO(
      "operation_anaesthesia_details");

  /** The scheduler appointments DAO. */
  private static GenericDAO schedulerAppointmentsDao = new GenericDAO("scheduler_appointments");

  /** The patient registration. */
  private static GenericDAO patientRegistration = new GenericDAO("patient_registration");

  /** The service sub groups. */
  private static GenericDAO serviceSubGroups = new GenericDAO("service_sub_groups");

  /** The test TATDAO. */
  private static TestTATDAO testTATDAO = new TestTATDAO("diag_tat_center_master");

  /** The test TATBO. */
  TestTATBO testTatBo = new TestTATBO();

  /** The pending sample dao. */
  private static PendingSamplesDAO pendingSampleDao = new PendingSamplesDAO();

  /** The dia ord dao. */
  private static DialysisOrderDao diaOrdDao = new DialysisOrderDao();

  /** The bill charge tax DAO. */
  private static BillChargeTaxDAO billChargeTaxDAO = new BillChargeTaxDAO();

  /** The bill charge tax BO. */
  private static BillChargeTaxBO billChargeTaxBO = new BillChargeTaxBO();

  /** The charge heads DAO. */
  private static ChargeHeadsDAO chargeHeadsDAO = new ChargeHeadsDAO();

  /** The newly added item approval detail ids. */
  private String[] newlyAddedItemApprovalDetailIds = null;

  /** The newly added item approval limit values. */
  private String[] newlyAddedItemApprovalLimitValues = null;

  /** The approval detail ids. */
  private String[] approvalDetailIds = null;

  /** The approval limit values. */
  private String[] approvalLimitValues = null;

  /**
   * Gets the common order id.
   *
   * @return the common order id
   */
  public int getCommonOrderId() {
    return commonOrderId;
  }

  /**
   * Sets the common order id.
   *
   * @param commonOrderId the new common order id
   */
  public void setCommonOrderId(int commonOrderId) {
    this.commonOrderId = commonOrderId;
  }

  /**
   * Sets the package charge id.
   *
   * @param packageChargeId the new package charge id
   */
  public void setPackageChargeId(String packageChargeId) {
    this.packageChargeId = packageChargeId;
  }

  /**
   * Sets the user name.
   *
   * @param userName the new user name
   */
  public void setUserName(String userName) {
    this.userName = userName;
  }

  /**
   * Gets the integer value from bean.
   *
   * @param bean  the bean
   * @param value the value
   * @return the integer value from bean
   */
  public static int getIntegerValueFromBean(BasicDynaBean bean, String value) {
    if (bean == null || value == null || bean.get(value) == null) {
      return 0;
    }
    return (Integer) bean.get(value);
  }

  /**
   * Sets the bill info.
   *
   * @param con          the con
   * @param patientId    the patient id
   * @param useBillNo    the use bill no
   * @param setInsurance the set insurance
   * @param userName     the user name
   * @return the string
   * @throws SQLException the SQL exception
   * @throws IOException  Signals that an I/O exception has occurred.
   */
  public String setBillInfo(Connection con, String patientId, String useBillNo,
      boolean setInsurance, String userName) throws SQLException, IOException {
    return setBillInfo(con, patientId, useBillNo, setInsurance, userName, "P");
  }

  /**
   * Sets the bill info.
   *
   * @param con          the con
   * @param patientId    the patient id
   * @param useBillNo    the use bill no
   * @param setInsurance the set insurance
   * @param userName     the user name
   * @param billType     the bill type
   * @return the string
   * @throws SQLException the SQL exception
   * @throws IOException  Signals that an I/O exception has occurred.
   */
  public String setBillInfo(Connection con, String patientId, String useBillNo,
      boolean setInsurance, String userName, String billType)
      throws SQLException, IOException {
    this.patientId = patientId;
    this.billNo = useBillNo;
    this.userName = userName;
    BasicDynaBean patientDetails = VisitDetailsDAO.getPatientVisitDetailsBean(con, patientId);
    this.mrNo = (String) patientDetails.get("mr_no");
    Integer centerId = (Integer) patientDetails.get("center_id");
    String prefRatePlan = CenterPreferencesDAO.getRatePlanForNonInsuredBills(centerId);

    if (this.billNo == null) {
      Bill createdBill = new Bill();
      if (billType.equals("P")) {
        createdBill.setBillType(Bill.BILL_TYPE_PREPAID);
      } else if (billType.equals("C")) {
        createdBill.setBillType(Bill.BILL_TYPE_CREDIT);
      }
      createdBill.setUserName(userName);
      createdBill.setOpenedBy(userName);
      createdBill.setVisitId(patientId);
      createdBill.setBillRatePlanId((String) patientDetails.get("org_id"));

      // need visit type from the patient details
      // HMS-12881 as part of ahll optimization
      // BasicDynaBean pd = VisitDetailsDAO.getPatientVisitDetailsBean(con,
      // patientId);
      createdBill.setVisitType((String) patientDetails.get("visit_type"));
      if (setInsurance) {
        this.isInsurance = true;
        createdBill.setIs_tpa(true);
        createdBill.setBillRatePlanId((String) patientDetails.get("org_id"));
      }

      if (patientDetails.get("primary_sponsor_id") != null
          && !patientDetails.get("primary_sponsor_id").equals("") && !setInsurance) {
        createdBill.setBillRatePlanId(
            prefRatePlan != null ? prefRatePlan : (String) patientDetails.get("org_id"));
      }
      Map msgMap = new BillBO().createNewBill(con, createdBill, true);
      if (msgMap.get("error") != null && !msgMap.get("error").equals("")) {
        return (String) msgMap.get("error");
      }
      this.billNo = createdBill.getBillNo();
    }

    /*
     * We require the MR NO, bill details like rate plan etc. from the bill.
     */
    BasicDynaBean bill = BillDAO.getBillBean(con, this.billNo);
    PatientInsurancePlanDAO insPlanDao = new PatientInsurancePlanDAO();

    this.planIds = insPlanDao.getPlanIds(con, patientId);

    this.isInsurance = bill.get("is_tpa") == null ? false : (Boolean) bill.get("is_tpa");
    this.planId = (bill.get("plan_id") == null) ? 0 : (Integer) bill.get("plan_id");
    this.visitType = (String) bill.get("visit_type");
    this.bill = bill;

    this.bedType = (String) bill.get("bed_type");
    this.ratePlanId = (String) bill.get("bill_rate_plan_id");

    if (bill.get("visit_status").equals("I")) {
      return "Patient visit is not active, cannot add/edit orders to patient.";
    }
    return null;
  }

  /**
   * Sets the patient info.
   *
   * @param patientId the patient id
   * @param mrNo      the mr no
   * @param userName  the user name
   * @return the string
   */
  public String setPatientInfo(String patientId, String mrNo, String userName) {
    this.patientId = patientId;
    this.mrNo = mrNo;
    this.userName = userName;

    this.isInsurance = false;
    this.planId = 0;
    this.visitType = "i";

    this.bedType = "GENERAL";
    this.ratePlanId = "ORG0001";
    return null;
  }

  /**
   * Gets the bill.
   *
   * @return the bill
   */
  public BasicDynaBean getBill() {
    return bill;
  }

  /**
   * Checks if is insurance.
   *
   * @return true, if is insurance
   */
  public boolean isInsurance() {
    return isInsurance;
  }

  /**
   * Gets the plan id.
   *
   * @return the plan id
   */
  public int getPlanId() {
    return planId;
  }

  /**
   * Gets the bed type.
   *
   * @return the bed type
   */
  public String getBedType() {
    return bedType;
  }

  /**
   * Gets the bill rate plan id.
   *
   * @return the bill rate plan id
   */
  public String getBillRatePlanId() {
    return ratePlanId;
  }

  /**
   * Order items.
   *
   * @param con                   the con
   * @param newOrders             the new orders
   * @param newPreAuths           the new pre auths
   * @param newPreAuthModes       the new pre auth modes
   * @param firstOfCategory       the first of category
   * @param condDoctrsList        the cond doctrs list
   * @param multiVisitPackageList the multi visit package list
   * @param appointmentId         the appointment id
   * @return the string
   * @throws Exception the exception
   */
  public String orderItems(Connection con, List<BasicDynaBean> newOrders,
      List<String> newPreAuths, List<Integer> newPreAuthModes, List<String> firstOfCategory,
      List<String> condDoctrsList, List<Boolean> multiVisitPackageList, int appointmentId)
      throws Exception {
    // TODO need to check from scheduler
    return orderItems(con, newOrders, newPreAuths, newPreAuthModes, firstOfCategory,
        condDoctrsList, multiVisitPackageList, null, null, null, appointmentId, true,
        new ArrayList<String>(), new ArrayList<Integer>(),
        new ArrayList<Map<String, Object>>(), new ArrayList<List<TestDocumentDTO>>());
  }

  /**
   * Order items.
   *
   * @param con                     the con
   * @param newOrders               the new orders
   * @param newPreAuths             the new pre auths
   * @param newPreAuthModes         the new pre auth modes
   * @param firstOfCategory         the first of category
   * @param condDoctrsList          the cond doctrs list
   * @param multiVisitPackageList   the multi visit package list
   * @param appointmentId           the appointment id
   * @param newSecPreAuths          the new sec pre auths
   * @param newsecPreAuthModes      the newsec pre auth modes
   * @param operationAnaesTypesList the operation anaes types list
   * @param testAdditionalDocs      the test additional docs
   * @return the string
   * @throws Exception the exception
   */
  public String orderItems(Connection con, List<BasicDynaBean> newOrders,
      List<String> newPreAuths, List<Integer> newPreAuthModes, List<String> firstOfCategory,
      List<String> condDoctrsList, List<Boolean> multiVisitPackageList, int appointmentId,
      List<String> newSecPreAuths, List<Integer> newsecPreAuthModes,
      List<Map<String, Object>> operationAnaesTypesList,
      List<List<TestDocumentDTO>> testAdditionalDocs) throws Exception {
    return orderItems(con, newOrders, newPreAuths, newPreAuthModes, firstOfCategory,
        condDoctrsList, multiVisitPackageList, null, null, null, appointmentId, newSecPreAuths,
        newsecPreAuthModes, operationAnaesTypesList, testAdditionalDocs);
  }

  /**
   * Order items.
   *
   * @param con                     the con
   * @param newOrders               the new orders
   * @param newPreAuths             the new pre auths
   * @param newPreAuthModes         the new pre auth modes
   * @param firstOfCategory         the first of category
   * @param condDoctrsList          the cond doctrs list
   * @param multiVisitPackageList   the multi visit package list
   * @param packageIdsList          the package ids list
   * @param packObIdsList           the pack ob ids list
   * @param patPackageIdsList       the pat package ids list
   * @param appointmentId           the appointment id
   * @param newSecPreAuths          the new sec pre auths
   * @param newsecPreAuthModes      the newsec pre auth modes
   * @param operationAnaesTypesList the operation anaes types list
   * @param testAdditionalDocs      the test additional docs
   * @return the string
   * @throws Exception the exception
   */
  public String orderItems(Connection con, List<BasicDynaBean> newOrders,
      List<String> newPreAuths, List<Integer> newPreAuthModes, List<String> firstOfCategory,
      List<String> condDoctrsList, List<Boolean> multiVisitPackageList,
      List<String> packageIdsList, List<String> packObIdsList, List<Integer> patPackageIdsList,
      int appointmentId, List<String> newSecPreAuths, List<Integer> newsecPreAuthModes,
      List<Map<String, Object>> operationAnaesTypesList,
      List<List<TestDocumentDTO>> testAdditionalDocs) throws Exception {
    return orderItems(con, newOrders, newPreAuths, newPreAuthModes, firstOfCategory,
        condDoctrsList, multiVisitPackageList, packageIdsList, packObIdsList,
        patPackageIdsList, appointmentId, true, newSecPreAuths, newsecPreAuthModes,
        operationAnaesTypesList, testAdditionalDocs);
  }

  /**
   * Order items.
   *
   * @param con                     the con
   * @param newOrders               the new orders
   * @param newPreAuths             the new pre auths
   * @param newPreAuthModes         the new pre auth modes
   * @param firstOfCategoryList     the first of category list
   * @param condDoctrsList          the cond doctrs list
   * @param multiVisitPackageList   the multi visit package list
   * @param packageIdsList          the package ids list
   * @param packObIdsList           the pack ob ids list
   * @param patPackageIdsList       the pat package ids list
   * @param appointmentId           the appointment id
   * @param chargeable              the chargeable
   * @param newSecPreAuths          the new sec pre auths
   * @param newsecPreAuthModes      the newsec pre auth modes
   * @param operationAnaesTypesList the operation anaes types list
   * @param testAdditionalDocs      the test additional docs
   * @return the string
   * @throws Exception the exception
   */
  public String orderItems(Connection con, List<BasicDynaBean> newOrders,
      List<String> newPreAuths, List<Integer> newPreAuthModes,
      List<String> firstOfCategoryList, List<String> condDoctrsList,
      List<Boolean> multiVisitPackageList, List<String> packageIdsList,
      List<String> packObIdsList, List<Integer> patPackageIdsList, int appointmentId,
      Boolean chargeable, List<String> newSecPreAuths, List<Integer> newsecPreAuthModes,
      List<Map<String, Object>> operationAnaesTypesList,
      List<List<TestDocumentDTO>> testAdditionalDocs) throws Exception {
    return orderItems(con, newOrders, newPreAuths, newPreAuthModes, firstOfCategoryList,
        condDoctrsList, multiVisitPackageList, packageIdsList, packObIdsList,
        patPackageIdsList, appointmentId, chargeable, false, newSecPreAuths,
        newsecPreAuthModes, operationAnaesTypesList, testAdditionalDocs);
  }

  /**
   * Order items.
   *
   * @param con                     the con
   * @param newOrders               the new orders
   * @param newPreAuths             the new pre auths
   * @param newPreAuthModes         the new pre auth modes
   * @param firstOfCategoryList     the first of category list
   * @param condDoctrsList          the cond doctrs list
   * @param multiVisitPackageList   the multi visit package list
   * @param packageIdsList          the package ids list
   * @param packObIdsList           the pack ob ids list
   * @param patPackageIdsList       the pat package ids list
   * @param appointmentId           the appointment id
   * @param chargeable              the chargeable
   * @param allowClosedBills        the allow closed bills
   * @param newSecPreAuths          the new sec pre auths
   * @param newSecPreAuthModes      the new sec pre auth modes
   * @param operationAnaesTypesList the operation anaes types list
   * @param testAdditionalDocs      the test additional docs
   * @return the string
   * @throws Exception the exception
   */
  public String orderItems(Connection con, List<BasicDynaBean> newOrders,
      List<String> newPreAuths, List<Integer> newPreAuthModes,
      List<String> firstOfCategoryList, List<String> condDoctrsList,
      List<Boolean> multiVisitPackageList, List<String> packageIdsList,
      List<String> packObIdsList, List<Integer> patPackageIdsList, int appointmentId,
      Boolean chargeable, boolean allowClosedBills, List<String> newSecPreAuths,
      List<Integer> newSecPreAuthModes, List<Map<String, Object>> operationAnaesTypesList,
      List<List<TestDocumentDTO>> testAdditionalDocs) throws Exception {
    return orderItems(con, newOrders, newPreAuths, newPreAuthModes, firstOfCategoryList,
        condDoctrsList, multiVisitPackageList, packageIdsList, packObIdsList,
        patPackageIdsList, appointmentId, chargeable, allowClosedBills, false, newSecPreAuths,
        newSecPreAuthModes, operationAnaesTypesList, testAdditionalDocs);
  }

  /**
   * Order items.
   *
   * @param con                     the con
   * @param newOrders               the new orders
   * @param newPreAuths             the new pre auths
   * @param newPreAuthModes         the new pre auth modes
   * @param firstOfCategoryList     the first of category list
   * @param condDoctrsList          the cond doctrs list
   * @param multiVisitPackageList   the multi visit package list
   * @param packageIdsList          the package ids list
   * @param packObIdsList           the pack ob ids list
   * @param patPackageIdsList       the pat package ids list
   * @param appointmentId           the appointment id
   * @param chargeable              the chargeable
   * @param allowClosedBills        the allow closed bills
   * @param forChannellingAppt      the for channelling appt
   * @param newSecPreAuths          the new sec pre auths
   * @param newSecPreAuthModes      the new sec pre auth modes
   * @param operationAnaesTypesList the operation anaes types list
   * @param testAdditionalDocs      the test additional docs
   * @return the string
   * @throws Exception the exception
   */
  public String orderItems(Connection con, List<BasicDynaBean> newOrders,
      List<String> newPreAuths, List<Integer> newPreAuthModes,
      List<String> firstOfCategoryList, List<String> condDoctrsList,
      List<Boolean> multiVisitPackageList, List<String> packageIdsList,
      List<String> packObIdsList, List<Integer> patPackageIdsList, int appointmentId,
      Boolean chargeable, boolean allowClosedBills, boolean forChannellingAppt,
      List<String> newSecPreAuths, List<Integer> newSecPreAuthModes,
      List<Map<String, Object>> operationAnaesTypesList,
      List<List<TestDocumentDTO>> testAdditionalDocs) throws Exception {

    if (newOrders.size() == 0) {
      return null;
    }

    if (!allowClosedBills && bill != null) {
      if (!bill.get("status").equals("A")) {
        return "Bill is not open, cannot add new items to the bill";
      }
      if (bill.get("payment_status").equals("P")) {
        return "Bill is paid, cannot add new items to the bill";
      }
    }

    /*
     * Generate a common order ID for all orders which are being done in this batch, Set the initial
     * labNo and radiologyNo to null, so that if it is required, it will be generated (only once
     * each for the common order).
     */
    if (commonOrderId == 0) {
      commonOrderId = DataBaseUtil.getNextSequence("common_order_seq");
    }
    labNo = null;
    radiologyNo = null;
    int anaesTypeIndex = 0;

    for (int i = 0; i < newOrders.size(); i++) {

      String[] preAuthIds = new String[2];
      Integer[] preAuthModeIds = new Integer[2];
      if (newPreAuths.size() > 0 && i < newPreAuths.size() && null != preAuthIds) {
        preAuthIds[0] = newPreAuths.get(i);
      }

      if (newSecPreAuths.size() > 0 && i < newSecPreAuths.size() && null != preAuthIds) {
        preAuthIds[1] = newSecPreAuths.get(i);
      }

      if (newPreAuthModes.size() > 0 && i < newPreAuthModes.size() && null != preAuthModeIds) {
        preAuthModeIds[0] = newPreAuthModes.get(i) == null || newPreAuthModes.get(i).equals("")
            ? null : Integer.valueOf(newPreAuthModes.get(i));
      }

      if (newSecPreAuthModes.size() > 0 && i < newSecPreAuthModes.size()
          && null != preAuthModeIds) {
        preAuthModeIds[1] =
            newSecPreAuthModes.get(i) == null || newSecPreAuthModes.get(i).equals("") ? null
                : Integer.valueOf(newSecPreAuthModes.get(i));
      }
      Boolean firstOfCategory = null;
      Map<String, Object> anaesTypesMap = null;

      if (firstOfCategoryList.size() > 0 && i < firstOfCategoryList.size()) {
        firstOfCategory =
            firstOfCategoryList.get(i) == null || firstOfCategoryList.get(i).equals("") ? null
                : Boolean.valueOf(firstOfCategoryList.get(i));
      }

      BasicDynaBean bean = newOrders.get(i);
      DynaClass clas = bean.getDynaClass();
      if (clas.getName().equals("bed_operation_schedule") && operationAnaesTypesList != null
          && operationAnaesTypesList.size() > 0) {
        anaesTypesMap = operationAnaesTypesList.get(anaesTypeIndex);
        anaesTypeIndex++;
      }

      if (clas.getName().equals("bed_operation_schedule")) {
        orderOperation(con, bean, chargeable, preAuthIds, preAuthModeIds, firstOfCategory,
            anaesTypesMap);
      }
    }

    for (int i = 0; i < newOrders.size(); i++) {

      String[] preAuthIds = new String[2];
      String condDoctorId = null;
      Integer[] preAuthModeIds = new Integer[2];
      if (newPreAuths.size() > 0 && i < newPreAuths.size() && null != preAuthIds) {
        preAuthIds[0] = newPreAuths.get(i);
      }

      if (newSecPreAuths.size() > 0 && i < newSecPreAuths.size() && null != preAuthIds) {
        preAuthIds[1] = newSecPreAuths.get(i);
      }

      if (newPreAuthModes.size() > 0 && i < newPreAuthModes.size() && null != preAuthModeIds) {
        preAuthModeIds[0] = newPreAuthModes.get(i);
      }

      if (newSecPreAuthModes.size() > 0 && i < newSecPreAuthModes.size()
          && null != preAuthModeIds) {
        preAuthModeIds[1] = newSecPreAuthModes.get(i);
      }

      if (condDoctrsList.size() > 0 && i < condDoctrsList.size()) {
        condDoctorId = condDoctrsList.get(i);
      }

      Boolean firstOfCategory = null;
      if (firstOfCategoryList.size() > 0 && i < firstOfCategoryList.size()) {
        firstOfCategory =
            firstOfCategoryList.get(i) == null || firstOfCategoryList.get(i).equals("") ? null
                : Boolean.valueOf(firstOfCategoryList.get(i));
      }

      boolean isMultiVisitPackItem = false;
      if (multiVisitPackageList != null && multiVisitPackageList.size() > 0) {
        isMultiVisitPackItem = multiVisitPackageList.get(i);
      }

      String packageId = null;
      if (packageIdsList != null) {
        packageId = packageIdsList.get(i);
      }

      String packObId = null;
      if (packObIdsList != null) {
        packObId = packObIdsList.get(i);
      }

      Integer patPackageId = null;
      if (patPackageIdsList != null) {
        patPackageId = patPackageIdsList.get(i);
      }

      approvalDetailIds = getRequiredApprovalIds(i, newlyAddedItemApprovalDetailIds);
      approvalLimitValues = getRequiredApprovalIds(i, newlyAddedItemApprovalLimitValues);

      BasicDynaBean bean = newOrders.get(i);
      DynaClass cls = bean.getDynaClass();
      logger.debug("Trying to add new order of type: " + cls.getName());
      if (cls.getName().equals("tests_prescribed")) {
        orderTests(con, bean, chargeable, appointmentId, preAuthIds, preAuthModeIds,
            firstOfCategory, condDoctorId, isMultiVisitPackItem,
            testAdditionalDocs.isEmpty() ? Collections.EMPTY_LIST : testAdditionalDocs.get(i),
            packageId, packObId, patPackageId, null);
      } else if (cls.getName().equals("services_prescribed")) {
        orderServices(con, bean, chargeable, appointmentId, preAuthIds, preAuthModeIds,
            firstOfCategory, condDoctorId, isMultiVisitPackItem, packageId, packObId,
            patPackageId, null);
      } else if (cls.getName().equals("equipment_prescribed")) {
        orderEquipment(con, bean, chargeable, firstOfCategory, isMultiVisitPackItem, packageId,
            packObId, patPackageId);
      } else if (cls.getName().equals("other_services_prescribed")) {
        orderOtherServices(con, bean, chargeable, firstOfCategory, isMultiVisitPackItem,
            packageId, packObId, patPackageId, null);
      } else if (cls.getName().equals("doctor_consultation")) {
        if (forChannellingAppt) {
          orderDoctor(con, bean, chargeable, firstOfCategory, isMultiVisitPackItem, true,
              packageId, packObId, patPackageId);
        } else {
          orderDoctor(con, bean, chargeable, firstOfCategory, isMultiVisitPackItem, false,
              packageId, packObId, patPackageId);
        }
      } else if (cls.getName().equals("diet_prescribed")) {
        orderDiet(con, bean, chargeable, firstOfCategory);
      } else if (cls.getName().equals("package_prescribed")) {
        PackageOrderDTO pkgDTO = new PackageOrderDTO();
        pkgDTO.setPackageId((int) bean.get("package_id"));
        pkgDTO.setOrderedTime((Timestamp) bean.get("presc_date"));
        pkgDTO.setDocPrescId((int) bean.get("doc_presc_id"));
        pkgDTO.setDoctorId(bean.get("doctor_id").toString());
        pkgDTO.setPrescribedId((int) bean.get("prescription_id"));
        orderPackage(con, pkgDTO, chargeable);
      }
    }

    return null;
  }

  /**
   * Gets the required approval ids.
   *
   * @param intVal                          the intVal
   * @param newlyAddedItemApprovalDetailIds the newly added item approval detail ids
   * @return the required approval ids
   */
  private String[] getRequiredApprovalIds(int intVal,
      String[] newlyAddedItemApprovalDetailIds) {
    if (intVal == 0) {
      return null;
    }

    if (null == newlyAddedItemApprovalDetailIds) {
      return null;
    }

    String[] appDetailIds = new String[intVal];
    for (int k = 0; k < intVal; k++) {
      appDetailIds[k] = newlyAddedItemApprovalDetailIds[k];
    }
    return appDetailIds;
  }

  /**
   * Order packages.
   *
   * @param con       the con
   * @param newOrders the new orders
   * @return the string
   * @throws Exception the exception
   */
  public String orderPackages(Connection con, List<PackageOrderDTO> newOrders)
      throws Exception {
    return orderPackages(con, newOrders, true);
  }

  /**
   * Order packages.
   *
   * @param con        the con
   * @param newOrders  the new orders
   * @param chargeable the chargeable
   * @return the string
   * @throws Exception the exception
   */
  public String orderPackages(Connection con, List<PackageOrderDTO> newOrders,
      boolean chargeable) throws Exception {

    if (newOrders.size() == 0) {
      return null;
    }

    if (commonOrderId == 0) {
      commonOrderId = DataBaseUtil.getNextSequence("common_order_seq");
    }

    for (PackageOrderDTO pkg : newOrders) {
      orderPackage(con, pkg, chargeable);
    }

    return null;
  }

  /**
   * Order multi visit package for channelling.
   *
   * @param con                             the con
   * @param prescribedMultiVisitPackageBean the prescribed multi visit package bean
   * @param patPackageId                    the pat package id
   * @return the string
   * @throws Exception the exception
   */
  // multivisitpackage
  public String orderMultiVisitPackageForChannelling(Connection con,
      BasicDynaBean prescribedMultiVisitPackageBean, int patPackageId) throws Exception {
    if (prescribedMultiVisitPackageBean != null) {
      BasicDynaBean patPackBean = null;
      if (patPackageId != -1) {
        Map<String, Object> identifiers = new HashMap<String, Object>();
        identifiers.put("mr_no", this.mrNo);
        identifiers.put("package_id", prescribedMultiVisitPackageBean.get("package_id"));
        identifiers.put("status", "P");
        identifiers.put("pat_package_id", patPackageId);

        patPackBean = patpkgDao.findByKey(identifiers);
      }

      if (patPackBean == null) {
        patPackBean = patpkgDao.getBean();
        patPackBean.set("pat_package_id", patPackageDao.getNextSequence());
        patPackBean.set("mr_no", this.mrNo);
        patPackBean.set("package_id", prescribedMultiVisitPackageBean.get("package_id"));
        patPackBean.set("status", "P");
        patpkgDao.insert(con, patPackBean);
      }

      if (commonOrderId == 0) {
        commonOrderId = DataBaseUtil.getNextSequence("common_order_seq");
      }
      prescribedMultiVisitPackageBean.set("mr_no", this.mrNo);
      prescribedMultiVisitPackageBean.set("patient_id", this.patientId);
      prescribedMultiVisitPackageBean.set("prescription_id",
          DataBaseUtil.getNextSequence("package_prescribed_sequence"));
      this.packageRef = (Integer) prescribedMultiVisitPackageBean.get("prescription_id");
      this.packageId = prescribedMultiVisitPackageBean.get("package_id").toString();
      prescribedMultiVisitPackageBean.set("common_order_id", commonOrderId);
      prescribedMultiVisitPackageBean.set("presc_date", DateUtil.getCurrentTimestamp());
      prescribedMultiVisitPackageBean.set("user_name", userName);
      prescribedMultiVisitPackageBean.set("pat_package_id", patPackBean.get("pat_package_id"));
      pkgDao.insert(con, prescribedMultiVisitPackageBean);
    }

    return null;
  }

  /**
   * Order multi visit package.
   *
   * @param con                             the con
   * @param prescribedMultiVisitPackageBean the prescribed multi visit package bean
   * @return the string
   * @throws Exception the exception
   */
  public String orderMultiVisitPackage(Connection con,
      BasicDynaBean prescribedMultiVisitPackageBean) throws Exception {
    if (prescribedMultiVisitPackageBean != null) {
      Map<String, Object> identifiers = new HashMap<String, Object>();
      identifiers.put("mr_no", this.mrNo);
      identifiers.put("package_id", prescribedMultiVisitPackageBean.get("package_id"));
      identifiers.put("status", "P");
      BasicDynaBean patPackBean = null;
      patPackBean = patpkgDao.findByKey(identifiers);

      if (patPackBean == null) {
        patPackBean = patpkgDao.getBean();
        patPackBean.set("pat_package_id", patPackageDao.getNextSequence());
        patPackBean.set("mr_no", this.mrNo);
        patPackBean.set("package_id", prescribedMultiVisitPackageBean.get("package_id"));
        patPackBean.set("status", "P");
        patpkgDao.insert(con, patPackBean);
      }

      if (commonOrderId == 0) {
        commonOrderId = DataBaseUtil.getNextSequence("common_order_seq");
      }
      prescribedMultiVisitPackageBean.set("mr_no", this.mrNo);
      prescribedMultiVisitPackageBean.set("patient_id", this.patientId);
      prescribedMultiVisitPackageBean.set("prescription_id",
          DataBaseUtil.getNextSequence("package_prescribed_sequence"));
      this.packageRef = (Integer) prescribedMultiVisitPackageBean.get("prescription_id");
      this.packageId = prescribedMultiVisitPackageBean.get("package_id").toString();
      prescribedMultiVisitPackageBean.set("common_order_id", commonOrderId);
      prescribedMultiVisitPackageBean.set("presc_date", DateUtil.getCurrentTimestamp());
      prescribedMultiVisitPackageBean.set("user_name", userName);
      prescribedMultiVisitPackageBean.set("pat_package_id", patPackBean.get("pat_package_id"));
      pkgDao.insert(con, prescribedMultiVisitPackageBean);
    }

    return null;
  }

  /**
   * Update multivisit package status.
   *
   * @param con                             the con
   * @param prescribedMultiVisitPackageBean the prescribed multi visit package bean
   * @return the string
   * @throws Exception the exception
   */
  public String updateMultivisitPackageStatus(Connection con,
      BasicDynaBean prescribedMultiVisitPackageBean) throws Exception {
    int packageId = (Integer) prescribedMultiVisitPackageBean.get("package_id");
    String mrNo = this.mrNo;
    PackageDAO pdao = new PackageDAO(con);
    pdao.updateMultivisitPackageStatus(packageId, mrNo);
    return null;
  }

  /**
   * Update channelling multivisit package status.
   *
   * @param con                             the con
   * @param prescribedMultiVisitPackageBean the prescribed multi visit package bean
   * @param patPackageId                    the pat package id
   * @return the string
   * @throws Exception the exception
   */
  public String updateChannellingMultivisitPackageStatus(Connection con,
      BasicDynaBean prescribedMultiVisitPackageBean, int patPackageId) throws Exception {
    int packageId = (Integer) prescribedMultiVisitPackageBean.get("package_id");
    String mrNo = this.mrNo;
    PackageDAO pdao = new PackageDAO(con);
    pdao.updateChannellingMultivisitPackageStatus(packageId, mrNo, patPackageId);
    return null;
  }

  /**
   * Order package.
   *
   * @param con        the con
   * @param packDto    the pack DTO
   * @param chargeable the chargeable
   * @throws Exception the exception
   */
  public void orderPackage(Connection con, PackageOrderDTO packDto, boolean chargeable)
      throws Exception {

    BasicDynaBean packBean = pkgDao.getBean();
    int patPackId = patPackageDao.getNextSequence();
    int prescribedId = (int) packDto.getPrescribedId() != 0 ? packDto.getPrescribedId()
        : DataBaseUtil.getNextSequence("package_prescribed_sequence");

    packBean.set("mr_no", this.mrNo);
    packBean.set("patient_id", this.patientId);
    packBean.set("prescription_id", prescribedId );
    packBean.set("package_id", packDto.getPackageId());
    packBean.set("doctor_id", packDto.getDoctorId());
    packBean.set("presc_date", packDto.getOrderedTime());
    packBean.set("user_name", userName);
    packBean.set("remarks", packDto.getRemarks());
    packBean.set("common_order_id", commonOrderId);
    packBean.set("doc_presc_id", packDto.getDocPrescId());
    packBean.set("pat_package_id", patPackId);

    BasicDynaBean patPackBean = patpkgDao.getBean();
    patPackBean.set("mr_no", this.mrNo);
    patPackBean.set("package_id", packDto.getPackageId());
    patPackBean.set("pat_package_id", patPackId);

    packDto.setPrescribedId((int) packBean.get("prescription_id"));

    // Patient pack row
    patpkgDao.insert(con, patPackBean);

    pkgDao.insert(con, packBean);

    BigDecimal pkgCharge = BigDecimal.ZERO;
    BigDecimal pkgDisc = BigDecimal.ZERO;
    BigDecimal itemAmount = BigDecimal.ZERO;
    BigDecimal itemDisc = BigDecimal.ZERO;
    BigDecimal itemCharge = BigDecimal.ZERO;
    BigDecimal chgDisc = BigDecimal.ZERO;

    BasicDynaBean pkg  = PackageDAO.getPackageDetails(packDto.getPackageId(), 
        this.ratePlanId, this.bedType);

    if (pkg != null) {
      BigDecimal packageCharge = (BigDecimal) pkg.get("charge");
      BigDecimal packageDiscount = (BigDecimal) pkg.get("discount");

      BasicDynaBean pkgInv = PackageDAO.getPackContChargesForInventory(packDto.getPackageId(),
          this.ratePlanId, this.bedType);
      if (pkgInv != null && (pkgInv.get("charge") != null || pkgInv.get("discount") != null)) {
        itemAmount = ((BigDecimal) pkgInv.get("charge"));
        itemCharge = (itemAmount).divide(BigDecimal.ONE, 2);
        chgDisc = discountSplit(itemCharge, packageCharge, packageDiscount);
        itemDisc = itemDisc.add(chgDisc.multiply(BigDecimal.ONE));
      }
      pkg.set("charge", itemAmount);
      pkg.set("discount", itemDisc);

      List<ChargeDTO> charges = getPackageContentCharges(pkg, BigDecimal.ONE, packDto.getSurgeon(),
          isInsurance, planIds, visitType, patientId, null,"PKGPKG",
          pkg.get("package_name").toString(), pkg.get("package_id").toString() , null, null);

      if (chargeable) {
        insertOrderCharges(con, charges, "PKG", (Integer) packBean.get("prescription_id"),
            (String) packBean.get("remarks"), (String) packBean.get("doctor_id"),
            (Timestamp) packBean.get("presc_date"), "Y", null, commonOrderId,
            packDto.getPreAuthIds(), packDto.getPreAuthModeIds());
      }

      /*
       * Save the charge ID for the package for reference by each of the components
       */
      this.packageChargeId = charges.get(0).getChargeId();

      List<BasicDynaBean> packComponents =
          PackageDAO.getPackageComponents(packDto.getPackageId());
      int index = 0;
      BigDecimal actAmount = BigDecimal.ZERO;
      BigDecimal actDisc = BigDecimal.ZERO;
      BigDecimal actCharge = BigDecimal.ZERO;
      BigDecimal actDiscount = BigDecimal.ZERO;
      for (BasicDynaBean item : packComponents) {

        /* This is a case of package having operation alone as a component */
        if (packComponents.size() == 1 && item.get("item_type") == null) {
          break;
        }
        int packContentId = (int) item.get("package_content_id");

        BasicDynaBean pkgContents =
                PackageDAO.getPackageContentDetails(packContentId, this.ratePlanId, this.bedType);
        actAmount = ((BigDecimal) pkgContents.get("charge"));
        actCharge = (actAmount).divide(BigDecimal.ONE, 2);
        actDiscount = discountSplit(actCharge, packageCharge, packageDiscount);
        pkgContents.set("charge", actAmount);
        pkgContents.set("discount", actDiscount);

        String chargeHead = (String) item.get("activity_charge");
        String actDesc = (String) item.get("activity_description");
        String actDescId = String.valueOf(item.get("package_content_id"));
        String actRemarks = (String) item.get("package_name");

        List<ChargeDTO> itemCharges = getPackageContentCharges(pkgContents, BigDecimal.ONE,
                packDto.getSurgeon(),isInsurance, planIds, visitType, patientId, null,
                chargeHead, actDesc, actDescId , actRemarks, this.packageChargeId);

        if (chargeable) {
          insertOrderCharges(con, itemCharges, "PKG", (Integer) packBean.get("prescription_id"),
              (String) packBean.get("remarks"), (String) packBean.get("doctor_id"),
              (Timestamp) packBean.get("presc_date"), "Y", null, commonOrderId,
              packDto.getPreAuthIds(), packDto.getPreAuthModeIds());
        }

        if (item.get("item_type") == null) {
          // unsupported item in the package master.
          logger.warn("Unsupported item in package master, package ID: " + packDto.getPackageId()
              + "; head: " + item.get("charge_head"));
          break;
        }
        String conductingDoctorId = null;
        if (item.get("item_type").equals("Laboratory")
            || item.get("item_type").equals("Radiology")) {

          for (Map map : packDto.getConductingDoctors()) {
            int actIndex = Integer.parseInt((String) map.get("act_index"));
            if (actIndex == index) {
              conductingDoctorId = (String) map.get("doctor_id");
              break;
            }
          }

          BasicDynaBean itemBean = testDao.getBean();
          itemBean.set("test_id", item.get("activity_id"));

          BasicDynaBean test = AddTestDAOImpl.getTestDetails((String) itemBean.get("test_id"),
              bedType, ratePlanId, VisitDetailsDAO.getCenterId(con, this.patientId));

          String testCategory = (String) test.get("category");

          BasicDynaBean prefs = GenericPreferencesDAO.getPrefsBean();

          if ((Boolean) test.get("applicable")) {
            if (prefs.get("autogenerate_labno").equals("Y")) {
              if (testCategory.equals("DEP_LAB")) {
                if (labNo == null) {
                  labNo = DiagnosticsDAO.getNextSequenceNo("LABNO");
                }
                itemBean.set("labno", labNo);
              } else {
                if (radiologyNo == null) {
                  radiologyNo = DiagnosticsDAO.getNextSequenceNo("RADNO");
                }
                itemBean.set("labno", radiologyNo);
              }
            }

            itemBean.set("pres_date", packDto.getOrderedTime());
            itemBean.set("pres_doctor", packDto.getDoctorId());
            itemBean.set("conducted", "N");
            itemBean.set("priority", "R");
            itemBean.set("remarks", packDto.getRemarks());
            itemBean.set("prescription_type",
                ((String) test.get("house_status")).equals("I") ? "h" : "o");
            itemBean.set("package_ref", packBean.get("prescription_id"));

            // filter documents for this activity and save.
            List<TestDocumentDTO> docList = new ArrayList<TestDocumentDTO>();
            String clinicalNotes = "";
            for (TestDocumentDTO dto : packDto.getTestDocuments()) {
              if (dto.getActivityIndex() == index) {
                docList.add(dto);
                clinicalNotes = dto.getClinicalNotes();
              }
            }
            itemBean.set("clinical_notes", clinicalNotes);
            orderTests(con, itemBean, false, 0, null, null, null, conductingDoctorId, false,
                docList, String.valueOf(packDto.getPackageId()),String.valueOf(packContentId),
                (Integer) patPackBean.get("pat_package_id"), itemCharges);
          }

        } else if (item.get("item_type").equals("Service")) {

          for (Map map : packDto.getConductingDoctors()) {
            int actIndex = Integer.parseInt((String) map.get("act_index"));
            if (actIndex == index) {
              conductingDoctorId = (String) map.get("doctor_id");
              break;
            }
          }

          BasicDynaBean itemBean = serviceDao.getBean();
          itemBean.set("service_id", item.get("activity_id"));
          itemBean.set("quantity", BigDecimal.ONE);
          itemBean.set("presc_date", packDto.getOrderedTime());
          itemBean.set("doctor_id", packDto.getDoctorId());
          itemBean.set("conducted", "N");
          itemBean.set("remarks", packDto.getRemarks());
          itemBean.set("package_ref", packBean.get("prescription_id"));

          BasicDynaBean service = new MasterServicesDao()
              .getServiceChargeBean((String) itemBean.get("service_id"), bedType, ratePlanId);
          itemBean.set("specialization", service.get("specialization"));

          if ((Boolean) service.get("applicable")) {
            orderServices(con, itemBean, false, 0, null, null, null, conductingDoctorId, false,
                 String.valueOf(packDto.getPackageId()),String.valueOf(packContentId),
                 (Integer) patPackBean.get("pat_package_id"), itemCharges);
          }

        } else if (item.get("item_type").equals("Implant")
            || item.get("item_type").equals("Consumable")
            || item.get("item_type").equals("Other Charge")) {
          BasicDynaBean itemBean = otherServiceDao.getBean();
          itemBean.set("service_name", item.get("activity_id"));
          itemBean.set("pres_time", packDto.getOrderedTime());
          itemBean.set("doctor_id", packDto.getDoctorId());
          itemBean.set("remarks", packDto.getRemarks());
          itemBean.set("service_group", item.get("charge_head"));
          itemBean.set("package_ref", packBean.get("prescription_id"));

          orderOtherServices(con, itemBean, false, null, false, 
              String.valueOf(packDto.getPackageId()),
              String.valueOf(packContentId),
              (Integer) patPackBean.get("pat_package_id"), itemCharges);

        }
        index++;
      }

      for (PackageDoctorVisit docVisit : packDto.getDoctorVisits()) {
        BasicDynaBean itemBean = doctorConsultationDao.getBean();
        itemBean.set("doctor_name", docVisit.getVDoctor());
        itemBean.set("visited_date", docVisit.getDocVisitDateTime());
        itemBean.set("presc_date", packDto.getOrderedTime());
        itemBean.set("remarks", packDto.getRemarks());
        itemBean.set("head", docVisit.getDoctorHead());
        itemBean.set("package_ref", packBean.get("prescription_id"));

        orderDoctor(con, itemBean, false, null, false);
      }

      if (packDto.getOpId() != null && !packDto.getOpId().equals("")) {
        BasicDynaBean opBean = operationDao.getBean();

        opBean.set("consultant_doctor", packDto.getDoctorId());
        opBean.set("operation_name", packDto.getOpId());
        opBean.set("theatre_name", packDto.getTheatreName());
        opBean.set("department", "");
        opBean.set("status", packDto.getStatus());
        opBean.set("start_datetime", packDto.getStartDateTime());
        opBean.set("end_datetime", packDto.getToDateTime());
        opBean.set("surgeon", packDto.getSurgeon());
        opBean.set("anaesthetist", packDto.getAnaesth());
        opBean.set("prescribed_date", packDto.getOrderedTime());
        opBean.set("package_ref", packBean.get("prescription_id"));

        orderOperation(con, opBean, false, null, null, null, null);

        if (packDto.getAnaesth() != null && !packDto.getAnaesth().equals("")) {
          BasicDynaBean anaBean = doctorConsultationDao.getBean();
          anaBean.set("doctor_name", packDto.getAnaesth());
          anaBean.set("presc_date", packDto.getOrderedTime());
          anaBean.set("visited_date", packDto.getStartDateTime());
          anaBean.set("head", "ANAOPE");
          anaBean.set("package_ref", packBean.get("prescription_id"));
          anaBean.set("operation_ref", opBean.get("prescribed_id"));
          anaBean.set("ot_doc_role", "ANAOPE");

          orderDoctor(con, anaBean, false, null, false);
        }
      }
      this.packageChargeId = null;
    }
  }

  /**
   * Order equipment.
   *
   * @param con                  the con
   * @param orderBean            the order bean
   * @param chargeable           the chargeable
   * @param firstOfCategory      the first of category
   * @param isMultiVisitPackItem the is multi visit pack item
   * @param packageId            the package id
   * @param packObId             the pack ob id
   * @param patPackageId         the pat package id
   * @throws Exception the exception
   */
  public void orderEquipment(Connection con, BasicDynaBean orderBean, boolean chargeable,
      Boolean firstOfCategory, boolean isMultiVisitPackItem, String packageId, String packObId,
      Integer patPackageId) throws Exception {

    Timestamp from = (Timestamp) orderBean.get("used_from");
    Timestamp to = (Timestamp) orderBean.get("used_till");
    String units = (String) orderBean.get("units");

    Integer prescriptionId = DataBaseUtil.getNextSequence("equipment_prescribed_seq");
    orderBean.set("mr_no", this.mrNo);
    orderBean.set("patient_id", this.patientId);
    orderBean.set("common_order_id", commonOrderId);
    orderBean.set("user_name", userName);
    orderBean.set("duration", getDuration(from, to, units));
    orderBean.set("prescribed_id", prescriptionId);
    boolean isOperation = setOperationRef(orderBean);
    equipmentDao.insert(con, orderBean);

    BasicDynaBean visitBean = VisitDetailsDAO.getVisitDetails(con, this.patientId);
    String mainVisitId = null != visitBean ? (String) visitBean.get("main_visit_id") : null;
    String orgId = null;
    String bedtype = null;

    // Davita changes
    Integer servSubGrpId = (Integer) ((BasicDynaBean) new GenericDAO("equipment_master")
        .findByKey("eq_id", (String) orderBean.get("eq_id"))).get("service_sub_group_id");
    String servGrpId = ((Integer) ((BasicDynaBean) serviceSubGroups
        .findByKey("service_sub_group_id", servSubGrpId)).get("service_group_id")).toString();
    BasicDynaBean approvalsBean = diaOrdDao.getSponsorApprovalDetails(con, this.mrNo,
        servGrpId, (String) orderBean.get("eq_id"), mainVisitId, approvalDetailIds,
        approvalLimitValues);

    orgId = approvalsBean != null ? (String) approvalsBean.get("org_id")
        : (String) bill.get("bill_rate_plan_id");
    bedtype = approvalsBean != null ? "GENERAL" : (String) bill.get("bed_type");

    String chargeId = null;
    if (chargeable) {
      String billno = (String) bill.get("bill_no");

      BasicDynaBean equipDetails = new EquipmentChargeDAO()
          .getEquipmentCharge((String) orderBean.get("eq_id"), bedtype, orgId);

      List<ChargeDTO> charges = getEquipmentCharges(equipDetails, from, to, units, isOperation,
          BigDecimal.ZERO, isInsurance, planIds, visitType, patientId, firstOfCategory);

      SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
      String usageRemarks = sdf.format(from) + " to " + sdf.format(to);

      insertOrderCharges(con, charges, "EQU", (Integer) orderBean.get("prescribed_id"),
          usageRemarks, (String) orderBean.get("doctor_id"), (Timestamp) orderBean.get("date"),
          "Y", from, (Integer) orderBean.get("common_order_id"), null, null);
      ChargeDTO mainCharge = charges.get(0);
      chargeId = mainCharge.getChargeId();

    } else if (packageChargeId != null) {
      new BillActivityChargeDAO(con).insertBillActivityCharge(this.packageChargeId, "EQU",
          "EQUOTC", orderBean.get("prescribed_id").toString(), (String) orderBean.get("eq_id"),
          null, "Y", from);
      chargeId = this.packageChargeId;
    }

    if ((packageId != null && !packageId.equals(""))
        && (packObId != null && !packObId.equals("")) && (patPackageId != null)
        && !isMultiVisitPackItem) {
      insertIntoPatientPackageContentAndConsumed(con, patPackageId, Integer.parseInt(packObId),
          Integer.parseInt(packageId), (String) orderBean.get("eq_id"),
          (Integer) orderBean.get("duration"), prescriptionId, chargeId, "equipment");
    }
  }

  /**
   * Order tests.
   *
   * @param con                  the con
   * @param orderBean            the order bean
   * @param chargeable           the chargeable
   * @param appointmentId        the appointment id
   * @param preAuthIds           the pre auth ids
   * @param preAuthModeIds       the pre auth mode ids
   * @param firstOfCategory      the first of category
   * @param condDoctorId         the cond doctor id
   * @param isMultiVisitPackItem the is multi visit pack item
   * @param docList              the doc list
   * @param packageId            the package id
   * @param packObId             the pack ob id
   * @param patPackageId         the pat package id
   * @return the string
   * @throws Exception the exception
   */
  public String orderTests(Connection con, BasicDynaBean orderBean, boolean chargeable,
      int appointmentId, String[] preAuthIds, Integer[] preAuthModeIds,
      Boolean firstOfCategory, String condDoctorId, boolean isMultiVisitPackItem,
      List<TestDocumentDTO> docList, String packageId, String packObId, Integer patPackageId,
      List<ChargeDTO> itemCharges )
      throws Exception {

    BasicDynaBean visitBean = VisitDetailsDAO.getVisitDetails(con, this.patientId);
    String mainVisitId = null != visitBean ? (String) visitBean.get("main_visit_id") : null;
    String orgId = null;
    String bedtype = null;

    // Davita changes
    Integer servSubGrpId = (Integer) ((BasicDynaBean) new GenericDAO("diagnostics")
        .findByKey("test_id", (String) orderBean.get("test_id"))).get("service_sub_group_id");
    String servGrpId = ((Integer) ((BasicDynaBean) serviceSubGroups
        .findByKey("service_sub_group_id", servSubGrpId)).get("service_group_id")).toString();
    BasicDynaBean approvalsBean = diaOrdDao.getSponsorApprovalDetails(con, this.mrNo,
        servGrpId, (String) orderBean.get("test_id"), mainVisitId, approvalDetailIds,
        approvalLimitValues);

    orgId = approvalsBean != null ? (String) approvalsBean.get("org_id") : this.ratePlanId;
    bedtype = approvalsBean != null ? "GENERAL" : this.bedType;

    BasicDynaBean test = AddTestDAOImpl.getTestDetails((String) orderBean.get("test_id"),
        bedtype, orgId, VisitDetailsDAO.getCenterId(con, this.patientId));

    String testCategory = (String) test.get("category");

    BasicDynaBean prefs = GenericPreferencesDAO.getPrefsBean();

    if ((Boolean) test.get("applicable")) {
      if (prefs.get("autogenerate_labno").equals("Y")) {
        if (testCategory.equals("DEP_LAB")) {
          if (labNo == null) {
            labNo = DiagnosticsDAO.getNextSequenceNo("LABNO");
          }
          orderBean.set("labno", labNo);
        } else {
          if (radiologyNo == null) {
            radiologyNo = DiagnosticsDAO.getNextSequenceNo("RADNO");
          }
          orderBean.set("labno", radiologyNo);
        }
      }

      String testDeptId = (String) ((BasicDynaBean) AddTestDAOImpl
          .getTestBean((String) orderBean.get("test_id"))).get("ddept_id");
      String testDocType = "";
      if (testCategory.equals("DEP_LAB")) {
        if (prefs.get("gen_token_for_lab").equals("Y")) {
          orderBean.set("token_number", DeptTokenGeneratorDAO.getToken(testDeptId,
              (VisitDetailsDAO.getCenterId(con, this.patientId))));
        }
        testDocType = "SYS_LR";
      } else {
        if (prefs.get("gen_token_for_rad").equals("Y")) {
          orderBean.set("token_number", DeptTokenGeneratorDAO.getToken(testDeptId,
              (VisitDetailsDAO.getCenterId(con, this.patientId))));
        }
        testDocType = "SYS_RR";
      }

      int prescId = DataBaseUtil.getNextSequence("test_prescribed");
      orderBean.set("mr_no", this.mrNo);
      orderBean.set("pat_id", this.patientId);
      orderBean.set("user_name", userName);
      orderBean.set("common_order_id", commonOrderId);
      orderBean.set("prescribed_id", prescId);
      orderBean.set("curr_location_presc_id", prescId);
      orderBean.set("prescription_type",
          ((String) test.get("house_status")).equals("I") ? "h" : "o");
      orderBean.set("exp_rep_ready_time",
          testTatBo.calculateExptRptReadyTime((Timestamp) orderBean.get("pres_date"),
              (String) orderBean.get("test_id"),
              VisitDetailsDAO.getCenterId(con, this.patientId)));
      if (isMultiVisitPackItem) {
        orderBean.set("package_ref", getPackageRef());
      }

      if (appointmentId > 0) {
        BasicDynaBean apptBean =
            schedulerAppointmentsDao.findByKey(con, "appointment_id", appointmentId);
        if (orderBean.get("test_id").equals(apptBean.get("res_sch_name"))) {
          orderBean.set("appointment_id", appointmentId);
        }
      }

      boolean condApplicable = (Boolean) test.get("conduction_applicable");
      boolean resultEntryApplicable = (Boolean) test.get("results_entry_applicable");
      if (!condApplicable) {
        orderBean.set("conducted", "U");
        orderBean.set("sflag", "1");
      }
      if (condApplicable && !resultEntryApplicable) {
        orderBean.set("conducted", "NRN");
      }
      if (pendingSampleDao.isOutsourceTest((String) orderBean.get("test_id"),
          (VisitDetailsDAO.getCenterId(con, this.patientId)))) {
        orderBean.set("conduction_type", "o");
      }

      testDao.insert(con, orderBean);

      String error =
          insertTestDocuments(con, docList, (Integer) orderBean.get("prescribed_id"));
      if (error != null) {
        return error;
      }

      String chargeId = null;
      if (chargeable) {
        // multivisitpackage
        BigDecimal charge = BigDecimal.ZERO;
        if (isMultiVisitPackItem) {
          charge = this.getMultiVisitPackaheItemCharge(con, orderBean, "test", orgId, bedtype);
          test.set("charge", charge);
          test.set("discount", BigDecimal.ZERO);
        }

        List<ChargeDTO> charges = getTestCharges(test, BigDecimal.ONE, isInsurance, planIds,
            visitType, patientId, firstOfCategory, condDoctorId);

        insertOrderCharges(con, charges, "DIA", (Integer) orderBean.get("prescribed_id"),
            (String) orderBean.get("remarks"), (String) orderBean.get("pres_doctor"),
            (Timestamp) orderBean.get("pres_date"), condApplicable ? "N" : "Y", null,
            (Integer) orderBean.get("common_order_id"), preAuthIds, preAuthModeIds);
        ChargeDTO mainCharge = charges.get(0);
        chargeId = mainCharge.getChargeId();

      } else if (packageChargeId != null) {
        String pmtChargeHead = testCategory.equals("DEP_LAB") ? "LTDIA" : "RTDIA";
        new BillActivityChargeDAO(con).insertBillActivityCharge(this.packageChargeId, "DIA",
            pmtChargeHead, orderBean.get("prescribed_id").toString(),
            (String) orderBean.get("test_id"), condDoctorId, condApplicable ? "N" : "Y", null);
        chargeId = this.packageChargeId;
      }
      if ((null != itemCharges && !itemCharges.equals(""))
          && (packageId != null && !packageId.equals(""))
          && (packObId != null && !packObId.equals("")) && (patPackageId != null)
          && !isMultiVisitPackItem) {
        ChargeDTO mainCharge = itemCharges.get(0);
        chargeId = mainCharge.getChargeId();
        insertIntoPatientPackageContentAndConsumed(con, patPackageId,
            Integer.parseInt(packObId), Integer.parseInt(packageId),
            (String) orderBean.get("test_id"), 1, prescId, chargeId, "tests");
      }
    }
    return null;
  }

  /**
   * Order services.
   *
   * @param con                  the con
   * @param orderBean            the order bean
   * @param chargeable           the chargeable
   * @param appointmentId        the appointment id
   * @param preAuthIds           the pre auth ids
   * @param preAuthModeIds       the pre auth mode ids
   * @param firstOfCategory      the first of category
   * @param condDoctorId         the cond doctor id
   * @param isMultiVisitPackItem the is multi visit pack item
   * @param packageId            the package id
   * @param packObId             the pack ob id
   * @param patPackageId         the pat package id
   * @throws Exception the exception
   */
  public void orderServices(Connection con, BasicDynaBean orderBean, boolean chargeable,
      int appointmentId, String[] preAuthIds, Integer[] preAuthModeIds,
      Boolean firstOfCategory, String condDoctorId, boolean isMultiVisitPackItem,
      String packageId, String packObId, Integer patPackageId,
      List<ChargeDTO> itemCharges) throws Exception {
    Integer prescriptionId = DataBaseUtil.getNextSequence("service_prescribed");
    orderBean.set("mr_no", this.mrNo);
    orderBean.set("patient_id", this.patientId);
    orderBean.set("prescription_id", prescriptionId);
    orderBean.set("common_order_id", commonOrderId);
    orderBean.set("user_name", userName);
    BasicDynaBean visitBean = VisitDetailsDAO.getVisitDetails(con, this.patientId);
    String mainVisitId = null != visitBean ? (String) visitBean.get("main_visit_id") : null;
    String orgId = null;
    if (isMultiVisitPackItem) {
      orderBean.set("package_ref", getPackageRef());
    }

    if (appointmentId > 0) {
      BasicDynaBean apptBean =
          schedulerAppointmentsDao.findByKey(con, "appointment_id", appointmentId);
      if (orderBean.get("service_id").equals(apptBean.get("res_sch_name"))) {
        orderBean.set("appointment_id", appointmentId);
      }
    }

    // Davita changes
    Integer servSubGrpId =
        (Integer) ((BasicDynaBean) new GenericDAO("services").findByKey("service_id",
            (String) orderBean.get("service_id"))).get("service_sub_group_id");
    String servGrpId = ((Integer) ((BasicDynaBean) serviceSubGroups
        .findByKey("service_sub_group_id", servSubGrpId)).get("service_group_id")).toString();
    BasicDynaBean approvalsBean = diaOrdDao.getSponsorApprovalDetails(con, this.mrNo,
        servGrpId, (String) orderBean.get("service_id"), mainVisitId, approvalDetailIds,
        approvalLimitValues);

    String bedtype = null;
    orgId = approvalsBean != null ? (String) approvalsBean.get("org_id") : this.ratePlanId;
    bedtype = approvalsBean != null ? "GENERAL" : this.bedType;

    // need service details to check for specializatoin
    BasicDynaBean service = new MasterServicesDao()
        .getServiceChargeBean((String) orderBean.get("service_id"), bedtype, orgId);

    orderBean.set("specialization", service.get("specialization"));

    boolean condApplicable = (Boolean) service.get("conduction_applicable");
    orderBean.set("conducted", condApplicable ? "N" : "U");

    setOperationRef(orderBean);
    serviceDao.insert(con, orderBean);

    if (service.get("specialization") != null && service.get("specialization").equals("I")) {
      GenericDAO dao = new GenericDAO("ivf_cycle");
      BasicDynaBean bean = dao.getBean();
      bean.set("ivf_cycle_id", dao.getNextSequence());
      bean.set("mr_no", this.mrNo);
      bean.set("patient_id", this.patientId);
      bean.set("start_date", DateUtil.getCurrentDate());
      bean.set("cycle_status", "O");
      dao.insert(con, bean);
    }

    String chargeId = null;
    if (chargeable) {
      // multivisitpackage
      BigDecimal charge = BigDecimal.ZERO;

      if (isMultiVisitPackItem) {
        charge =
            this.getMultiVisitPackaheItemCharge(con, orderBean, "service", orgId, bedtype);
        service.set("unit_charge", charge);
        service.set("discount", BigDecimal.ZERO);
      }

      List<ChargeDTO> charges =
          getServiceCharges(service, (BigDecimal) orderBean.get("quantity"), isInsurance,
              planIds, visitType, patientId, firstOfCategory, condDoctorId);

      insertOrderCharges(con, charges, "SER", (Integer) orderBean.get("prescription_id"),
          (String) orderBean.get("remarks"), (String) orderBean.get("doctor_id"),
          (Timestamp) orderBean.get("presc_date"), condApplicable ? "N" : "Y", null,
          (Integer) orderBean.get("common_order_id"), preAuthIds, preAuthModeIds);
      ChargeDTO mainCharge = charges.get(0);
      chargeId = mainCharge.getChargeId();

    } else if (packageChargeId != null) {
      new BillActivityChargeDAO(con).insertBillActivityCharge(this.packageChargeId, "SER",
          "SERSNP", orderBean.get("prescription_id").toString(),
          (String) orderBean.get("service_id"), condDoctorId, condApplicable ? "N" : "Y",
          null);
      chargeId = this.packageChargeId;
    }

    if ((null != itemCharges && !itemCharges.equals(""))
        && (packageId != null && !packageId.equals(""))
        && (packObId != null && !packObId.equals("")) && (patPackageId != null)
        && !isMultiVisitPackItem) {
      ChargeDTO mainCharge = itemCharges.get(0);
      chargeId = mainCharge.getChargeId();
      insertIntoPatientPackageContentAndConsumed(con, patPackageId, Integer.parseInt(packObId),
          Integer.parseInt(packageId), (String) orderBean.get("service_id"),
          Integer.valueOf(((BigDecimal) orderBean.get("quantity")).intValue()), prescriptionId,
          chargeId, "services");
    }
  }

  /**
   * Order other services.
   *
   * @param con                  the con
   * @param orderBean            the order bean
   * @param chargeable           the chargeable
   * @param firstOfCategory      the first of category
   * @param isMultiVisitPackItem the is multi visit pack item
   * @param packageId            the package id
   * @param packObId             the pack ob id
   * @param patPackageId         the pat package id
   * @throws Exception the exception
   */
  public void orderOtherServices(Connection con, BasicDynaBean orderBean, boolean chargeable,
      Boolean firstOfCategory, boolean isMultiVisitPackItem, String packageId, String packObId,
      Integer patPackageId, List<ChargeDTO> itemCharges) throws Exception {

    Integer prescriptionId = DataBaseUtil.getNextSequence("ip_other_services_sequence");
    orderBean.set("mr_no", this.mrNo);
    orderBean.set("patient_id", this.patientId);
    orderBean.set("user_name", userName);
    orderBean.set("prescribed_id", prescriptionId);
    orderBean.set("common_order_id", commonOrderId);
    BasicDynaBean visitBean = VisitDetailsDAO.getVisitDetails(con, this.patientId);
    String mainVisitId = null != visitBean ? (String) visitBean.get("main_visit_id") : null;
    String orgId = null;
    String bedtype = null;

    // Davita changes
    Integer servSubGrpId = (Integer) ((BasicDynaBean) new GenericDAO("common_charges_master")
        .findByKey("charge_name", (String) orderBean.get("service_name")))
            .get("service_sub_group_id");
    String servGrpId = ((Integer) ((BasicDynaBean) serviceSubGroups
        .findByKey("service_sub_group_id", servSubGrpId)).get("service_group_id")).toString();
    BasicDynaBean approvalsBean = diaOrdDao.getSponsorApprovalDetails(con, this.mrNo,
        servGrpId, (String) orderBean.get("service_name"), mainVisitId, approvalDetailIds,
        approvalLimitValues);

    orgId = approvalsBean != null ? (String) approvalsBean.get("org_id") : ratePlanId;
    bedtype = approvalsBean != null ? "GENERAL" : bedType;

    if (isMultiVisitPackItem) {
      orderBean.set("package_ref", getPackageRef());
    }
    boolean isOperation = setOperationRef(orderBean);
    otherServiceDao.insert(con, orderBean);

    String serviceGroup = (String) orderBean.get("service_group");

    String chargeId = null;
    if (chargeable) {
      BigDecimal quantity = (BigDecimal) orderBean.get("quantity");
      List<ChargeDTO> charges = null;

      if ("OCOTC".equals(serviceGroup)) {
        BasicDynaBean otherService =
            new CommonChargesDAO().getCommonCharge(orderBean.get("service_name").toString());

        if (isMultiVisitPackItem) {
          BigDecimal charge = this.getMultiVisitPackaheItemCharge(con, orderBean,
              "otherservices", orgId, bedtype);
          otherService.set("charge", charge);
        }

        charges = getOtherCharges(otherService, quantity, isInsurance, planIds, visitType,
            patientId, firstOfCategory);

        insertOrderCharges(con, charges, "OTC", ((Integer) orderBean.get("prescribed_id")),
            (String) orderBean.get("remarks"), (String) orderBean.get("doctor_id"),
            (Timestamp) orderBean.get("pres_time"), "Y", null,
            (Integer) orderBean.get("common_order_id"), null, null);
        ChargeDTO mainCharge = charges.get(0);
        chargeId = mainCharge.getChargeId();
      }

    } else if (packageChargeId != null) {
      new BillActivityChargeDAO(con).insertBillActivityCharge(this.packageChargeId, "OTC",
          serviceGroup, orderBean.get("prescribed_id").toString(),
          (String) orderBean.get("service_name"), null, "Y", null);
      chargeId = this.packageChargeId;
    }

    if ((null != itemCharges && !itemCharges.equals(""))
        && (packageId != null) && (packObId != null && !packObId.equals(""))
        && (patPackageId != null) && !isMultiVisitPackItem) {
      ChargeDTO mainCharge = itemCharges.get(0);
      chargeId = mainCharge.getChargeId();
      insertIntoPatientPackageContentAndConsumed(con, patPackageId, Integer.parseInt(packObId),
          Integer.parseInt(packageId), (String) orderBean.get("service_name"),
          Integer.valueOf(((BigDecimal) orderBean.get("quantity")).intValue()), prescriptionId,
          chargeId, "other_services");
    }
  }

  /**
   * Order doctor.
   *
   * @param con                  the con
   * @param orderBean            the order bean
   * @param chargeable           the chargeable
   * @param firstOfCategory      the first of category
   * @param isMultiVisitPackItem the is multi visit pack item
   * @throws Exception the exception
   */
  public void orderDoctor(Connection con, BasicDynaBean orderBean, boolean chargeable,
      Boolean firstOfCategory, boolean isMultiVisitPackItem) throws Exception {
    orderDoctor(con, orderBean, chargeable, firstOfCategory, isMultiVisitPackItem, false, null,
        null, null);
  }

  /**
   * Order doctor.
   *
   * @param con                  the con
   * @param orderBean            the order bean
   * @param chargeable           the chargeable
   * @param firstOfCategory      the first of category
   * @param isMultiVisitPackItem the is multi visit pack item
   * @param forChannellingAppt   the for channelling appt
   * @param packageId            the package id
   * @param packObId             the pack ob id
   * @param patPackageId         the pat package id
   * @throws Exception the exception
   */
  public void orderDoctor(Connection con, BasicDynaBean orderBean, boolean chargeable,
      Boolean firstOfCategory, boolean isMultiVisitPackItem, boolean forChannellingAppt,
      String packageId, String packObId, Integer patPackageId) throws Exception {

    HttpSession session = (HttpSession) RequestContext.getSession();
    Preferences pref = (Preferences) session.getAttribute("preferences");
    String modAdavncedOt = "Y";
    if ((pref != null) && (pref.getModulesActivatedMap() != null)) {
      modAdavncedOt = (String) pref.getModulesActivatedMap().get("mod_advanced_ot");
      if (modAdavncedOt == null || modAdavncedOt.equals("")) {
        modAdavncedOt = "N";
      }
    }

    orderBean.set("mr_no", this.mrNo);
    orderBean.set("patient_id", this.patientId);
    orderBean.set("username", userName);
    orderBean.set("common_order_id", commonOrderId);
    if (isMultiVisitPackItem) {
      orderBean.set("package_ref", getPackageRef());
    }

    orderBean.set("consultation_id",
        DataBaseUtil.getNextSequence("doctor_consultation_sequence"));
    setOperationRef(orderBean);

    BasicDynaBean visitBean = VisitDetailsDAO.getVisitDetails(con, this.patientId);
    String mainVisitId = null != visitBean ? (String) visitBean.get("main_visit_id") : null;
    String orgId = null;
    String bedtype = null;

    // Davita changes
    BasicDynaBean approvalsBean = null;
    if (((String) orderBean.get("doctor_name")) != null
        && !((String) orderBean.get("doctor_name")).equals("")) {
      Integer servSubGrpId =
          (Integer) ((BasicDynaBean) new GenericDAO("doctors").findByKey("doctor_id",
              (String) orderBean.get("doctor_name"))).get("service_sub_group_id");
      String servGrpId =
          ((Integer) ((BasicDynaBean) serviceSubGroups.findByKey("service_sub_group_id",
              servSubGrpId)).get("service_group_id")).toString();
      approvalsBean = diaOrdDao.getSponsorApprovalDetails(con, this.mrNo, servGrpId,
          (String) orderBean.get("doctor_name"), mainVisitId, approvalDetailIds,
          approvalLimitValues);
    }

    orgId = approvalsBean != null ? (String) approvalsBean.get("org_id") : this.ratePlanId;
    bedtype = approvalsBean != null ? "GENERAL" : this.bedType;

    BasicDynaBean opMasterBean = null;

    if (null != orderBean.get("operation_ref")) {
      // the doctor charges belong to an operation: we need operation master info also
      // to be able to get the doctor charges. Check the map first to see if we have
      // the master
      // bean already with us.
      opMasterBean = this.operationBeanMap.get(orderBean.get("operation_ref"));

      if (opMasterBean == null) {
        // the operation was ordered earlier, not in this transaction. So we don't have
        // a copy
        // of the charges relating to the operatin. Need to get it.
        BasicDynaBean opBean =
            operationDao.findByKey("prescribed_id", orderBean.get("operation_ref"));
        String operId = (String) opBean.get("operation_name");
        opMasterBean = operationMasterDao.getOperationChargeBean(operId, this.bedType,
            this.ratePlanId);
      }

      // copying OT Management related data into respective tables when advanced OT
      // module is active.
      if (modAdavncedOt.equals("Y")) {
        copySurgeonAndAnaestiatistDetails(con, orderBean, chargeable);
      }
    }

    String consType = (String) orderBean.get("head");
    BasicDynaBean consTypeBean = null;

    if (opMasterBean == null) {
      // get the consultation type bean
      consTypeBean = getConsultationTypeBean(Integer.parseInt(consType));

      // generate token
      if (consTypeBean.get("patient_type").equals("o")) {
        if (RegistrationPreferencesDAO.isTokenGenerationEnabled()) {
          int token =
              PatientTokenGenerator.generateToken((String) orderBean.get("doctor_name"));
          orderBean.set("consultation_token", new BigDecimal(token)); // todo: convert to int
        }
      }
    }

    // for ot related doctor consultation insertions, set status to U to indicate
    // that
    // conduction is unnecessary.
    String otDocRole = (String) orderBean.get("ot_doc_role");
    if (otDocRole != null && !otDocRole.equals("")) {
      orderBean.set("status", "U");
    }
    doctorConsultationDao.insert(con, orderBean);

    String chargeId = null;
    if (chargeable) {
      BasicDynaBean doctor = DoctorMasterDAO
          .getDoctorCharges((String) orderBean.get("doctor_name"), orgId, bedtype);

      List<ChargeDTO> charges = null;
      if (opMasterBean != null) {
        // This is an operation related doctor order.
        charges = getOtDoctorCharges(doctor, otDocRole, this.visitType, opMasterBean,
            BigDecimal.ONE, isInsurance, planIds, this.bedType, patientId, firstOfCategory);
      } else {
        // normal consultation order
        if (isMultiVisitPackItem) {
          // multivisitpackage
          BigDecimal charge = BigDecimal.ZERO;
          if (forChannellingAppt) {
            charge = PackageDAO.getDocSpecificMultiVisitPackageDocItemCharge(packageId,
                (String) orderBean.get("head"), bedType, ratePlanId,
                (String) orderBean.get("doctor_name"), BigDecimal.ONE);
          } else {
            charge =
                this.getMultiVisitPackaheItemCharge(con, orderBean, "doctor", orgId, bedtype);
          }
          charges = getDoctorConsCharges(doctor, consTypeBean, this.visitType,
              OrgMasterDao.getOrgdetailsDynaBean(orgId), BigDecimal.ONE, isInsurance, planIds,
              bedtype, patientId, firstOfCategory, charge);

        } else {
          charges = getDoctorConsCharges(doctor, consTypeBean, this.visitType,
              OrgMasterDao.getOrgdetailsDynaBean(orgId), BigDecimal.ONE, isInsurance, planIds,
              bedtype, patientId, firstOfCategory);
        }

        // Defalult code for consultation for followup.
        Boolean modEclaim = (Boolean) RequestContext.getSession().getAttribute("mod_eclaim");
        RegistrationPreferencesDTO regPrefs =
            RegistrationPreferencesDAO.getRegistrationPreferences();
        if (modEclaim && regPrefs.getDoc_eandm_codification_required() != null
            && regPrefs.getDoc_eandm_codification_required().equals("Y")) {
          BasicDynaBean visitbean =
              VisitDetailsDAO.getPatientVisitDetailsBean(con, this.patientId);
          String opType = (String) visitbean.get("op_type");
          if (opType != null && opType.equals("F")) {

            String followUpCode = regPrefs.getDefault_followup_eandm_code();
            if (followUpCode != null && !followUpCode.equals("")) {
              ChargeDTO mainCharge = charges.get(0);
              mainCharge.setActRatePlanItemCode(followUpCode);
              mainCharge.setCodeType("E&M");

              Map fields = new HashMap();
              fields.put("consultation_id", orderBean.get("consultation_id"));
              fields.put("description", MRDUpdateScreenDAO.getCodeDesc(followUpCode, "E&M"));

              doctorConsultationDao.updateWithName(con, fields, "consultation_id");
            }
          }
        }
      }

      insertOrderCharges(con, charges, "DOC", (Integer) orderBean.get("consultation_id"),
          (String) orderBean.get("remarks"), (String) orderBean.get("presc_doctor_id"),
          (Timestamp) orderBean.get("presc_date"), "Y",
          (Timestamp) orderBean.get("visited_date"),
          (Integer) orderBean.get("common_order_id"), null, null);
      ChargeDTO mainCharge = charges.get(0);
      chargeId = mainCharge.getChargeId();

    } else if (packageChargeId != null) {

      String chargeHead = (null != orderBean.get("operation_ref")) ? otDocRole
          : (String) consTypeBean.get("charge_head");

      new BillActivityChargeDAO(con).insertBillActivityCharge(this.packageChargeId, "DOC",
          chargeHead, orderBean.get("consultation_id").toString(),
          (String) orderBean.get("doctor_name"), (String) orderBean.get("doctor_name"), "Y",
          (Timestamp) orderBean.get("visited_date"));
      chargeId = this.packageChargeId;
    }

    if ((packageId != null && !packageId.equals(""))
        && (packObId != null && !packObId.equals(""))
        && (patPackageId != null && !patPackageId.equals("")) && !isMultiVisitPackItem) {
      insertIntoPatientPackageContentAndConsumed(con, patPackageId, Integer.parseInt(packObId),
          Integer.parseInt(packageId), (String) orderBean.get("doctor_name"), 1,
          (Integer) orderBean.get("consultation_id"), chargeId, "doctor");
    }
  }

  /**
   * Order diet.
   *
   * @param con             the con
   * @param orderBean       the order bean
   * @param chargeable      the chargeable
   * @param firstOfCategory the first of category
   * @throws Exception the exception
   */
  public void orderDiet(Connection con, BasicDynaBean orderBean, boolean chargeable,
      Boolean firstOfCategory) throws Exception {

    orderBean.set("visit_id", this.patientId);
    orderBean.set("user_name", this.userName);
    orderBean.set("common_order_id", this.commonOrderId);

    orderBean.set("ordered_id", DataBaseUtil.getNextSequence("diet_prescribed_seq"));
    dietDao.insert(con, orderBean);

    BasicDynaBean visitBean = VisitDetailsDAO.getVisitDetails(con, this.patientId);
    String mainVisitId = null != visitBean ? (String) visitBean.get("main_visit_id") : null;
    String orgId = null;
    String bedtype = null;

    // Davita changes
    Integer servSubGrpId =
        (Integer) ((BasicDynaBean) new GenericDAO("diet_master").findByKey("diet_id",
            (BigDecimal) orderBean.get("diet_id"))).get("service_sub_group_id");
    String servGrpId = ((Integer) ((BasicDynaBean) serviceSubGroups
        .findByKey("service_sub_group_id", servSubGrpId)).get("service_group_id")).toString();
    BasicDynaBean approvalsBean = diaOrdDao.getSponsorApprovalDetails(con, this.mrNo,
        servGrpId, ((BigDecimal) orderBean.get("diet_id")).toString(), mainVisitId,
        approvalDetailIds, approvalLimitValues);

    orgId = approvalsBean != null ? (String) approvalsBean.get("org_id") : this.ratePlanId;
    bedtype = approvalsBean != null ? "GENERAL" : this.bedType;

    if (chargeable) {
      BasicDynaBean meal = new DietaryMasterDAO().getChargeForMeal(orgId,
          orderBean.get("diet_id").toString(), bedtype);

      List<ChargeDTO> charges = getMealCharges(meal, BigDecimal.ONE, isInsurance, planIds,
          visitType, patientId, firstOfCategory);

      insertOrderCharges(con, charges, "DIE", (Integer) orderBean.get("ordered_id"), "",
          (String) orderBean.get("ordered_by"), (Timestamp) orderBean.get("ordered_time"), "Y",
          new Timestamp(((java.sql.Date) orderBean.get("meal_date")).getTime()),
          (Integer) orderBean.get("common_order_id"), null, null);

    } else if (packageChargeId != null) {
      new BillActivityChargeDAO(con).insertBillActivityCharge(this.packageChargeId, "DIE",
          "MDIE", orderBean.get("ordered_id").toString(), (String) orderBean.get("diet_id"),
          null, "Y", (Timestamp) orderBean.get("meal_date"));
    }
  }

  /**
   * Order operation.
   *
   * @param con                 the con
   * @param orderBean           the order bean
   * @param chargeable          the chargeable
   * @param preAuthIds          the pre auth ids
   * @param preAuthModeIds      the pre auth mode ids
   * @param firstOfCategory     the first of category
   * @param operationAnaesTypes the operation anaes types
   * @throws Exception the exception
   */
  public void orderOperation(Connection con, BasicDynaBean orderBean, boolean chargeable,
      String[] preAuthIds, Integer[] preAuthModeIds, Boolean firstOfCategory,
      Map<String, Object> operationAnaesTypes) throws Exception {

    HttpSession session = (HttpSession) RequestContext.getSession();
    Preferences pref = (Preferences) session.getAttribute("preferences");
    String modAdavncedOt = "Y";
    if ((pref != null) && (pref.getModulesActivatedMap() != null)) {
      modAdavncedOt = (String) pref.getModulesActivatedMap().get("mod_advanced_ot");
      if (modAdavncedOt == null || modAdavncedOt.equals("")) {
        modAdavncedOt = "N";
      }
    }
    orderBean.set("patient_id", this.patientId);
    orderBean.set("mr_no", this.mrNo);
    orderBean.set("user_name", userName);
    orderBean.set("common_order_id", commonOrderId);

    String operId = (String) orderBean.get("operation_name");
    String finalizationStatus = (String) orderBean.get("finalization_status");
    BasicDynaBean operation = operationMasterDao.getOperationChargeBean(operId, this.bedType,
        this.ratePlanId);

    // updating the status to added_to_bill if any are prescribed and ordered now.
    List<BasicDynaBean> opPreslist = new OrderDAO().getOperationPrescriptions(this.patientId);
    for (BasicDynaBean presBean : opPreslist) {
      if (operId.equals((String) presBean.get("operation_id"))) {
        GenericDAO opPresDao = new GenericDAO("patient_prescription");
        BasicDynaBean issuedBean = opPresDao.getBean();
        issuedBean.set("status", "O");
        issuedBean.set("username", RequestContext.getUserName());
        opPresDao.update(con, issuedBean.getMap(), "patient_presc_id",
            presBean.get("pres_id"));
      }
    }

    if (operation.get("applicable") != null && (Boolean) operation.get("applicable")) {
      boolean condApplicable = (Boolean) operation.get("conduction_applicable");
      orderBean.set("status", condApplicable ? "N" : "U");

      orderBean.set("prescribed_id", DataBaseUtil.getNextSequence("ip_operation_sequence"));
      operationDao.insert(con, orderBean);
      List anaesTypes = operationAnaesTypes != null
          ? (List) operationAnaesTypes.get("anaestesia_types") : null;
      List anaesTypesFrom = operationAnaesTypes != null
          ? (List) operationAnaesTypes.get("anaestesia_types_from_date_time") : null;
      List anaesTypesTo = operationAnaesTypes != null
          ? (List) operationAnaesTypes.get("anaestesia_types_to_date_time") : null;
      List<Integer> surgAnaesDetIds = new ArrayList<Integer>();
      if (anaesTypes != null && anaesTypes.size() > 0) {
        for (int i = 0; i < anaesTypes.size(); i++) {
          BasicDynaBean bean = surgeryAnesthesiaDetDao.getBean();
          int id = surgeryAnesthesiaDetDao.getNextSequence();
          surgAnaesDetIds.add(id);
          bean.set("surgery_anesthesia_details_id", id);
          bean.set("prescribed_id", (Integer) orderBean.get("prescribed_id"));
          bean.set("anesthesia_type", (String) anaesTypes.get(i));
          bean.set("anaes_start_datetime", (Timestamp) anaesTypesFrom.get(i));
          bean.set("anaes_end_datetime", (Timestamp) anaesTypesTo.get(i));

          surgeryAnesthesiaDetDao.insert(con, bean);
        }
      }

      Integer newOptransId = (Integer) orderBean.get("prescribed_id");
      this.operationPrescIdsMap.put(newOptransId, (Integer) orderBean.get("prescribed_id"));
      this.operationBeanMap.put((Integer) orderBean.get("prescribed_id"), operation);

      // copying OT Management related data into respective tables.
      if (modAdavncedOt.equals("Y")) {
        copyOperationDetails(con, orderBean, chargeable, operationAnaesTypes);
      }

      if (chargeable) {
        String ot = (String) orderBean.get("theatre_name");
        String surgeon = (String) orderBean.get("surgeon");
        String units = ((String) orderBean.get("hrly")).equals("checked") ? "H" : "D";
        // String anastecianType = (String)orderBean.get("anesthesia_type");
        Timestamp from = (Timestamp) orderBean.get("start_datetime");
        Timestamp to = (Timestamp) orderBean.get("end_datetime");
        List<ChargeDTO> charges = new ArrayList<ChargeDTO>();
        List<ChargeDTO> anaesTypeCharges = new ArrayList<ChargeDTO>();

        BasicDynaBean theatre = theatreMasterDao.getTheatreChargeDetails(ot, this.bedType,
            this.ratePlanId);
        BasicDynaBean surgeonBean = DoctorMasterDAO.getOTDoctorChargesBean(surgeon, this.bedType,
            this.ratePlanId);
        BasicDynaBean anasthesiaTypeChargeBean = null;
        // for adding multiple AnaesthesiaTypeCharges(Bug#42705)
        // don't include anaesthetist charges in the order, there will be a separate
        // order item
        // for this.
        charges = getOperationCharges(operId, operation, theatre, surgeonBean, null, from, to,
            units, isInsurance, planIds, finalizationStatus, visitType,
            anasthesiaTypeChargeBean, patientId, firstOfCategory);

        if (anaesTypes != null && anaesTypes.size() > 0) {
          for (int i = 0; i < anaesTypes.size(); i++) {

            anasthesiaTypeChargeBean = new AnaesthesiaTypeChargesDAO().getAnasthesiaTypeCharge(
                (String) anaesTypes.get(i), this.bedType, this.ratePlanId);

            anaesTypeCharges = getAnaesthesiaTypeCharges(operId, operation,
                (Timestamp) anaesTypesFrom.get(i), (Timestamp) anaesTypesTo.get(i), units,
                isInsurance, planIds, finalizationStatus, visitType, anasthesiaTypeChargeBean,
                patientId, firstOfCategory);
            for (ChargeDTO charge : anaesTypeCharges) {
              if (charge.getChargeHead().equals("ANATOPE")) {
                charge.setSurgeryAnesthesiaDetailsId(surgAnaesDetIds.get(i));
              }
            }
            charges.addAll(anaesTypeCharges);
          }
        }

        insertOrderCharges(con, charges, "OPE", (Integer) orderBean.get("prescribed_id"),
            (String) orderBean.get("remarks"), (String) orderBean.get("consultant_doctor"),
            (Timestamp) orderBean.get("prescribed_date"), condApplicable ? "N" : "Y",
            (java.sql.Timestamp) orderBean.get("start_datetime"),
            (Integer) orderBean.get("common_order_id"), preAuthIds, preAuthModeIds);

      } else if (packageChargeId != null) {
        new BillActivityChargeDAO(con).insertBillActivityCharge(this.packageChargeId, "OPE",
            "SUOPE", orderBean.get("prescribed_id").toString(),
            (String) orderBean.get("operation_name"), (String) orderBean.get("surgeon"),
            condApplicable ? "N" : "Y", (java.sql.Timestamp) orderBean.get("start_datetime"));
      }
    }
  }

  // this method is customized for advanced ot module.

  /**
   * Order operation.
   *
   * @param order            the order
   * @param con              the con
   * @param chargeable       the chargeable
   * @param firstOfCategory  the first of category
   * @param opDetailsId      the op details id
   * @param userName         the user name
   * @param allowClosedBills the allow closed bills
   * @return the string
   * @throws Exception the exception
   */
  public String orderOperation(OrderBO order, Connection con, boolean chargeable,
      Boolean firstOfCategory, Integer opDetailsId, String userName, Boolean allowClosedBills)
      throws Exception {

    BasicDynaBean primarySurgeryDetails = opDetDao.getPrimaryOperationDetails(opDetailsId);
    BasicDynaBean primarySurgeonBean = null;
    List<String> secResourceIds = new ArrayList<String>();
    BasicDynaBean primaryAnestiatistBean = null;
    List<BasicDynaBean> secondarySurgeryDetails = opDetDao
        .getSecondaryOperationDetails(opDetailsId);
    List<BasicDynaBean> procedureDetails = opDetDao.getProcedures(con, opDetailsId);
    List<BasicDynaBean> anaethesiaTypes = null;
    String patientId = (String) primarySurgeryDetails.get("patient_id");
    String billno = new BillDAO(con).getPatientCreditBillOpenOnly(patientId, true, true);
    String setBillCheck = order.setBillInfo(con, patientId, billno, false, userName);
    String fixedOtCharges = GenericPreferencesDAO.getGenericPreferences().getFixedOtCharges();
    fixedOtCharges = (fixedOtCharges == null || fixedOtCharges.equals("")) ? "N" : fixedOtCharges;
    Boolean isSecondary = false;
    order.setPlanIds(new PatientInsurancePlanDAO().getPlanIds(con, patientId));

    if (setBillCheck != null) {
      return setBillCheck;
    }

    if (!allowClosedBills && order.getBill() != null) {
      if (!order.getBill().get("status").equals("A")) {
        return "Bill is not open, cannot add new items to the bill";
      }
      if (order.getBill().get("payment_status").equals("P")) {
        return "Bill is paid, cannot add new items to the bill";
      }
    }

    List<BasicDynaBean> opPreslist = new OrderDAO().getOperationPrescriptions(patientId);
    BasicDynaBean orderOperationBean = operationDao.getBean();
    Timestamp prescribedDate = DateUtil.getCurrentTimestamp();

    List<BasicDynaBean> surgeonDetails =
        opDetDao.getProcedureResources("surgeon", opDetailsId);
    List<BasicDynaBean> anestiatistDetails =
        opDetDao.getProcedureResources("anestiatist", opDetailsId);
    Map<String, Object> columns = new HashMap<String, Object>();
    Map<String, Object> keys = new HashMap<String, Object>();
    for (int i = 0; i < surgeonDetails.size(); i++) {
      BasicDynaBean primaryTeam = opDetDao.getOperationTeam(con,
          (int)primarySurgeryDetails.get("operation_proc_id"));
     
      //primarySurgeonBean = surgeonDetails.get(i);
      if ( primarySurgeonBean == null && surgeonDetails.get(i) != null
          && surgeonDetails.get(i).get("operation_speciality").toString().equals("SU")
          && ( primaryTeam != null &&  primaryTeam.get("team_doc_id") != null 
          && primaryTeam.get("team_doc_id")
            .equals((String)surgeonDetails.get(i).get("resource_id"))) ) {
        primarySurgeonBean = surgeonDetails.get(i);
        secResourceIds.add((String)surgeonDetails.get(i).get("resource_id"));
      } else if (surgeonDetails.get(i) != null
          && surgeonDetails.get(i).get("operation_speciality").toString().equals("SU")) {
        secResourceIds.add((String)surgeonDetails.get(i).get("resource_id"));
      }
    }

    for (int i = 0; i < anestiatistDetails.size(); i++) {
      primaryAnestiatistBean = anestiatistDetails.get(i);
      if (primaryAnestiatistBean != null
          && primaryAnestiatistBean.get("operation_speciality").toString().equals("AN")) {
        primaryAnestiatistBean = anestiatistDetails.get(i);
        break;
      }
    }

    if (chargeable) {
      boolean primarySurgeonAdded = false;
      String opeId = (String) primarySurgeryDetails.get("operation_id");
      BasicDynaBean operationBean = operationMasterDao.getOperationChargeBean(opeId,
          order.getBedType(), order.getBillRatePlanId());
      boolean primarySurgeryCondApplicable =
          (Boolean) operationBean.get("conduction_applicable");
      orderOperationBean = opDetBo.getOperationDetailsBean(orderOperationBean,
          primarySurgeryDetails, primarySurgeonBean, primaryAnestiatistBean, userName,
          primarySurgeryCondApplicable, prescribedDate);

      String units = (orderOperationBean.get("hrly") != null
          && ((String) orderOperationBean.get("hrly")).equals("checked")) ? "H" : "D";
      Timestamp from = (Timestamp) orderOperationBean.get("start_datetime");
      Timestamp to = (Timestamp) orderOperationBean.get("end_datetime");

      if (fixedOtCharges.equals("Y")) {
        from = DataBaseUtil.getDateandTime();
        to = DataBaseUtil.getDateandTime();
      }

      for (int i = 0; i < procedureDetails.size(); i++) {
        List<ChargeDTO> charges = new LinkedList<ChargeDTO>();
        BasicDynaBean surgDetailsBean = procedureDetails.get(i);
        Integer opProcId = (Integer) surgDetailsBean.get("operation_proc_id");
        Integer operationRef = (Integer) surgDetailsBean.get("prescribed_id");
        List<BasicDynaBean> billableResources =
            opDetDao.getOperationBillableResources(con, opProcId);
        BasicDynaBean theatreAsBillableResourceBean =
            opDetDao.getTheatreAsBillableResource(con, opProcId);
        String operationId = (String) surgDetailsBean.get("operation_id");
        BasicDynaBean surgery = operationMasterDao.getOperationChargeBean(operationId,
            order.getBedType(), order.getBillRatePlanId());
        String[] preAuthId = new String[] 
            {(String) surgDetailsBean.get("prior_auth_id")};
        Integer[] preAuthModeId = new Integer[] 
            {(Integer) surgDetailsBean.get("prior_auth_mode_id")};
        String operPriority = (String) surgDetailsBean.get("oper_priority");
        Boolean theatreChargeExists = theatreAsBillableResourceBean != null;
        boolean surgicalAssistanceChargeAdded = false;
        BasicDynaBean bedOpeSecondaryBean = bedOpeSecDao.getBean();

        if (surgery.get("applicable") != null && (Boolean) surgery.get("applicable")) {
          boolean condApplicable = (Boolean) surgery.get("conduction_applicable");
          if (surgDetailsBean.get("oper_priority") != null
              && surgDetailsBean.get("oper_priority").equals("P")) {
            operationDao.insert(con, orderOperationBean);
            /*
             * columns.put("prescribed_id", orderOperationBean.get("prescribed_id"));
             * keys.put("operation_details_id", opDetailsId); opDetDao.update(con, columns, keys);
             */

          } else if (surgDetailsBean.get("oper_priority") != null
              && surgDetailsBean.get("oper_priority").toString().equals("S")) {
            bedOpeSecondaryBean = opDetBo.getBedSecondaryBean(bedOpeSecondaryBean, surgDetailsBean,
                orderOperationBean);

            bedOpeSecDao.insert(con, bedOpeSecondaryBean);
          }

          for (BasicDynaBean presBean : opPreslist) {
            if (operationId.equals((String) presBean.get("operation_id"))) {

              GenericDAO opPresDao = new GenericDAO("patient_prescription");
              BasicDynaBean issuedBean = opPresDao.getBean();
              issuedBean.set("status", "O");
              issuedBean.set("username", RequestContext.getUserName());
              opPresDao.update(con, issuedBean.getMap(), "patient_presc_id",
                  presBean.get("pres_id"));

            }
          }

          for (int j = 0; j < billableResources.size(); j++) {
            BasicDynaBean resourcesBean = billableResources.get(j);
            String resourceType = (String) resourcesBean.get("resource_type");

            if (!theatreChargeExists && !surgicalAssistanceChargeAdded) {

              List<ChargeDTO> surgicalAssistanceCharges =
                  order.getOperationCharges(operationId, surgery, null, null, null, from, to,
                      units, order.isInsurance(), order.getPlanIds(), "Y",
                      (String) order.getBill().get("visit_type"), null, patientId, null);

              surgicalAssistanceChargeAdded = true;
              charges.addAll(surgicalAssistanceCharges);

            }

            if ( resourceType.equals("THEAT") ) { 

              if ( operPriority.equals("S") ) { 
                to = from;//no charges for secondary
                isSecondary = true;
              } 
              String thetareId = (String) resourcesBean.get("resource_id");
              BasicDynaBean theatre = theatreMasterDao.getTheatreChargeDetails(thetareId,
                  order.getBedType(), order.getBillRatePlanId());
              List<ChargeDTO> theatreCharges = order.getOperationCharges(operationId, surgery,
                  theatre, null, null, from, to, units, order.isInsurance(), order.getPlanIds(),
                  "Y", (String) order.getBill().get("visit_type"), null, patientId, null, 
                  isSecondary);

              charges.addAll(theatreCharges);

            } else if (resourceType.equals("ANAE")) {

              String anastecianType = (String) resourcesBean.get("anesthesia_type");
              Timestamp anFrom = (Timestamp) resourcesBean.get("anaes_start_datetime");
              Timestamp anTo = (Timestamp) resourcesBean.get("anaes_end_datetime");

              BasicDynaBean bean = surgeryAnesthesiaDetDao.getBean();
              int id = surgeryAnesthesiaDetDao.getNextSequence();
              bean.set("surgery_anesthesia_details_id", id);
              bean.set("prescribed_id",( "S".equals(operPriority) ) 
                  ? (Integer) bedOpeSecondaryBean.get("prescribed_id") 
                  : (Integer) orderOperationBean.get("prescribed_id"));
              bean.set("anesthesia_type", anastecianType);
              bean.set("anaes_start_datetime", anFrom);
              bean.set("anaes_end_datetime", anTo);
              surgeryAnesthesiaDetDao.insert(con, bean);

              BasicDynaBean anasthesiaTypeChargeBean =
                  new AnaesthesiaTypeChargesDAO().getAnasthesiaTypeCharge(anastecianType,
                      order.getBedType(), order.getBillRatePlanId());

              List<ChargeDTO> anasthesiaTypeCharges =
                  order.getAnaesthesiaTypeCharges(operationId, surgery, anFrom, anTo, units,
                      order.isInsurance(), order.getPlanIds(), "Y", order.getVisitType(),
                      anasthesiaTypeChargeBean, patientId, null);

              for (ChargeDTO charge : anasthesiaTypeCharges) {
                if (charge.getChargeHead().equals("ANATOPE")) {
                  charge.setSurgeryAnesthesiaDetailsId(id);
                }
              }

              charges.addAll(anasthesiaTypeCharges);

            } else if (resourceType.equals("TEAM")) {

              BasicDynaBean operationTeam = procResourceDao.findByKey(con, "operation_team_id",
                  Integer.parseInt(resourcesBean.get("resource_id").toString()));
              String speciality = (String) operationTeam.get("operation_speciality");
              String resourceId = (String) operationTeam.get("resource_id");

              if ((speciality.equals("SU")) && primarySurgeonBean != null && primarySurgeonBean
                  .get("resource_id").equals(operationTeam.get("resource_id"))
                  && !primarySurgeonAdded) {

                BasicDynaBean surgeonBean = DoctorMasterDAO.getOTDoctorChargesBean(resourceId,
                    order.getBedType(), order.getBillRatePlanId());
                List<ChargeDTO> surgeonCharges = order.getSurgeonCharges(operationId, surgery,
                    from, to, units, order.isInsurance(), order.getPlanIds(), "Y",
                    order.getVisitType(), surgeonBean, patientId, null,
                    (String) surgery.get("item_code"), (String) surgery.get("code_type"));

                primarySurgeonAdded = true;
                charges.addAll(surgeonCharges);

              } else if ((speciality.equals("SU")) && secResourceIds != null
                  && secResourceIds.contains(operationTeam.get("resource_id"))
                  && operPriority.equals("S")) {

                BasicDynaBean surgeonBean = DoctorMasterDAO.getOTDoctorChargesBean(resourceId,
                    order.getBedType(), order.getBillRatePlanId());
                List<ChargeDTO> surgeonCharges = order.getSurgeonCharges(operationId, surgery, from,
                    to, units, order.isInsurance(), order.getPlanIds(), "Y", order.getVisitType(),
                    surgeonBean, patientId, null, (String) surgery.get("item_code"),
                    (String) surgery.get("code_type"));

                charges.addAll(surgeonCharges);
                
              } else {
                BasicDynaBean orderOpeBean =
                    opDetBo.getOrderBean(orderOperationBean, primarySurgeryDetails, resourceId,
                        operationTeam, userName, prescribedDate, operationRef);
                opDetBo.orderDoctor(con, orderOpeBean, true, null, order,
                    (String) primarySurgeryDetails.get("patient_id"), operationId);
              }
            }
          }

          if (operPriority.equals("P")) {
            if (charges.size() > 0) {
              order.insertOrderCharges(con, charges, "OPE",
                  (Integer) orderOperationBean.get("prescribed_id"),
                  (String) orderOperationBean.get("remarks"),
                  (String) primarySurgeryDetails.get("prescribing_doctor"), prescribedDate,
                  condApplicable ? "N" : "Y", from,
                  (Integer) orderOperationBean.get("common_order_id"), preAuthId, preAuthModeId);
            }
          } else {
            if (charges.size() > 0) {
              order.insertOrderCharges(con, charges, "OPE",
                  (Integer) bedOpeSecondaryBean.get("sec_prescribed_id"),
                  (String) orderOperationBean.get("remarks"),
                  (String) primarySurgeryDetails.get("prescribing_doctor"), prescribedDate,
                  condApplicable ? "N" : "Y", from,
                  (Integer) orderOperationBean.get("common_order_id"), preAuthId, preAuthModeId);
            }
          }

        }
      }
    }

    return null;
  }

  // copying operation details AND procedure details into OT Management related
  // tables.

  /**
   * Copy operation details.
   *
   * @param con                 the con
   * @param orderBean           the order bean
   * @param chargeable          the chargeable
   * @param operationAnaesTypes the operation anaes types
   * @throws Exception the exception
   */
  public void copyOperationDetails(Connection con, BasicDynaBean orderBean, Boolean chargeable,
      Map<String, Object> operationAnaesTypes) throws Exception {

    BasicDynaBean operationDetailsBean = opDetDao.getBean();
    BasicDynaBean opeBillableResourceBaen = null;
    operationDetailsBean.set("operation_details_id", opDetDao.getNextSequence());
    this.operationDetailsId = (Integer) operationDetailsBean.get("operation_details_id");
    operationDetailsBean.set("mr_no", orderBean.get("mr_no"));
    operationDetailsBean.set("patient_id", orderBean.get("patient_id"));
    operationDetailsBean.set("theatre_id", orderBean.get("theatre_name"));
    operationDetailsBean.set("operation_status",
        orderBean.get("status").equals("N") ? "P" : orderBean.get("status"));
    operationDetailsBean.set("surgery_start", orderBean.get("start_datetime"));
    operationDetailsBean.set("surgery_end", orderBean.get("end_datetime"));
    // operationDetailsBean.set("prescribed_id", orderBean.get("prescribed_id"));
    // operationDetailsBean.set("anaesthesia_type",
    // orderBean.get("anesthesia_type"));
    operationDetailsBean.set("prescribing_doctor", orderBean.get("consultant_doctor"));
    operationDetailsBean.set("order_remarks", orderBean.get("remarks"));
    operationDetailsBean.set("added_to_bill", "Y");
    operationDetailsBean.set("charge_type",
        (orderBean.get("hrly") != null && !orderBean.get("hrly").toString().isEmpty())
            ? orderBean.get("hrly").equals("checked") ? "H" : "D" : null);

    opDetDao.insert(con, operationDetailsBean);

    BasicDynaBean operationProcedureBean = procDao.getBean();
    operationProcedureBean.set("operation_proc_id", procDao.getNextSequence());
    this.operationProcId = (Integer) operationProcedureBean.get("operation_proc_id");
    operationProcedureBean.set("operation_details_id",
        operationDetailsBean.get("operation_details_id"));
    operationProcedureBean.set("oper_priority", "P");
    operationProcedureBean.set("operation_id", orderBean.get("operation_name"));
    operationProcedureBean.set("prescribed_id", orderBean.get("prescribed_id"));

    procDao.insert(con, operationProcedureBean);

    if (chargeable) {

      if (orderBean.get("theatre_name") != null
          && !orderBean.get("theatre_name").toString().isEmpty()) {
        opeBillableResourceBaen = opeBillableResourcesDao.getBean();
        opeBillableResourceBaen.set("operation_billable_resources_id",
            opeBillableResourcesDao.getNextSequence());
        opeBillableResourceBaen.set("resource_id", orderBean.get("theatre_name"));
        opeBillableResourceBaen.set("resource_type", "THEAT");
        opeBillableResourceBaen.set("operation_proc_id", this.operationProcId);
        opeBillableResourceBaen.set("billable", "Y");

        opeBillableResourcesDao.insert(con, opeBillableResourceBaen);
      }

      List anaesTypes = operationAnaesTypes != null
          ? (List) operationAnaesTypes.get("anaestesia_types") : null;
      List anaesTypesFrom = operationAnaesTypes != null
          ? (List) operationAnaesTypes.get("anaestesia_types_from_date_time") : null;
      List anaesTypesTo = operationAnaesTypes != null
          ? (List) operationAnaesTypes.get("anaestesia_types_to_date_time") : null;
      BasicDynaBean opAnTypeDetailsBean = null;
      if (anaesTypes != null && anaesTypes.size() > 0) {
        for (int i = 0; i < anaesTypes.size(); i++) {
          opAnTypeDetailsBean = operationAnesthesiaDetDao.getBean();
          opAnTypeDetailsBean.set("operation_anae_detail_id",
              operationAnesthesiaDetDao.getNextSequence());
          opAnTypeDetailsBean.set("operation_details_id",
              (Integer) operationDetailsBean.get("operation_details_id"));
          opAnTypeDetailsBean.set("anesthesia_type", (String) anaesTypes.get(i));
          opAnTypeDetailsBean.set("anaes_start_datetime", (Timestamp) anaesTypesFrom.get(i));
          opAnTypeDetailsBean.set("anaes_end_datetime", (Timestamp) anaesTypesTo.get(i));
          operationAnesthesiaDetDao.insert(con, opAnTypeDetailsBean);

          opeBillableResourceBaen = opeBillableResourcesDao.getBean();
          opeBillableResourceBaen.set("operation_billable_resources_id",
              opeBillableResourcesDao.getNextSequence());
          opeBillableResourceBaen.set("resource_id",
              opAnTypeDetailsBean.get("operation_anae_detail_id") + "");
          opeBillableResourceBaen.set("resource_type", "ANAE");
          opeBillableResourceBaen.set("operation_proc_id", this.operationProcId);
          opeBillableResourceBaen.set("billable", "Y");
          opeBillableResourcesDao.insert(con, opeBillableResourceBaen);
        }
      }
    }

    if (orderBean.get("surgeon") != null && !orderBean.get("surgeon").toString().isEmpty()) {
      String surgeonId = (String) orderBean.get("surgeon");
      orderBean = doctorConsultationDao.getBean();
      orderBean.set("doctor_name", surgeonId);
      orderBean.set("head", "SUOPE");

      copySurgeonAndAnaestiatistDetails(con, orderBean, chargeable);
    }
  }

  // copy Surgeon And Anaestiatists details into OT Management related tables.

  /**
   * Copy surgeon and anaestiatist details.
   *
   * @param con        the con
   * @param orderBean  the order bean
   * @param chargeable the chargeable
   * @throws Exception the exception
   */
  public void copySurgeonAndAnaestiatistDetails(Connection con, BasicDynaBean orderBean,
      Boolean chargeable) throws Exception {
    String opertionSpeciality = null;
    BasicDynaBean opeBillableResourceBaen = null;
    if (this.operationDetailsId == null) {
      if (orderBean.get("operation_ref") != null
          && !orderBean.get("operation_ref").toString().isEmpty()) {
        Integer opRef = (Integer) orderBean.get("operation_ref");
        BasicDynaBean operationDetailsBean =
            opDetDao.getPrimaryOperationDetailsByPrescribedId(opRef);
        this.operationDetailsId = (Integer) operationDetailsBean.get("operation_details_id");
        BasicDynaBean opProcBean =
            procDao.findByKey(con, "operation_details_id", this.operationDetailsId);
        this.operationProcId = (Integer) opProcBean.get("operation_proc_id");
      }
    }
    String head = (String) orderBean.get("head");
    if (head.equals("SUOPE")) {
      opertionSpeciality = "SU";
    } else if (head.equals("ASUOPE")) {
      opertionSpeciality = "ASU";
    } else if (head.equals("ANAOPE")) {
      opertionSpeciality = "AN";
    } else if (head.equals("AANOPE")) {
      opertionSpeciality = "ASAN";
    } else if (head.equals("COSOPE")) {
      opertionSpeciality = "COSOPE";
    }
    BasicDynaBean procedureResourcesBean = procResourceDao.getBean();
    procedureResourcesBean.set("operation_team_id", procResourceDao.getNextSequence());
    procedureResourcesBean.set("operation_details_id", this.operationDetailsId);
    procedureResourcesBean.set("resource_id", orderBean.get("doctor_name"));
    procedureResourcesBean.set("operation_speciality", opertionSpeciality);

    procResourceDao.insert(con, procedureResourcesBean);

    if (chargeable) {
      if (orderBean.get("doctor_name") != null
          && !orderBean.get("doctor_name").toString().isEmpty()) {
        opeBillableResourceBaen = opeBillableResourcesDao.getBean();
        opeBillableResourceBaen.set("operation_billable_resources_id",
            opeBillableResourcesDao.getNextSequence());
        opeBillableResourceBaen.set("resource_id",
            procedureResourcesBean.get("operation_team_id").toString());
        opeBillableResourceBaen.set("resource_type", "TEAM");
        opeBillableResourceBaen.set("operation_proc_id", this.operationProcId);
        opeBillableResourceBaen.set("billable", "Y");

        opeBillableResourcesDao.insert(con, opeBillableResourceBaen);
      }
    }

  }

  /**
   * Gets the multi visit packahe item charge.
   *
   * @param con      the con
   * @param itemBean the item bean
   * @param itemType the item type
   * @param orgId    the org id
   * @param bedType  the bed type
   * @return the multi visit packahe item charge
   * @throws Exception the exception
   */
  //// multivisitpackage
  public BigDecimal getMultiVisitPackaheItemCharge(Connection con, BasicDynaBean itemBean,
      String itemType, String orgId, String bedType) throws Exception {
    BigDecimal charge = BigDecimal.ZERO;
    PackageDAO pdao = new PackageDAO(con);
    String packageId = this.packageId;

    if (itemType.equals("test")) {
      charge = pdao.getMultiVisitPackageItemCharge(packageId, itemBean,
          (String) itemBean.get("test_id"), bedType, orgId, itemType, BigDecimal.ONE);
    } else if (itemType.equals("service")) {
      charge = pdao.getMultiVisitPackageItemCharge(packageId, itemBean,
          (String) itemBean.get("service_id"), bedType, orgId, itemType, BigDecimal.ONE);
    } else if (itemType.equals("otherservices")) {
      charge = pdao.getMultiVisitPackageItemCharge(packageId, itemBean,
          (String) itemBean.get("service_name"), bedType, orgId, itemType, BigDecimal.ONE);
    } else if (itemType.equals("doctor")) {
      charge = pdao.getMultiVisitPackageItemCharge(packageId, itemBean,
          (String) itemBean.get("head"), bedType, orgId, itemType, BigDecimal.ONE);
    }
    return charge;
  }

  /*
   * Add a new bed to ip_bed_details, and add the corresponding charge to bill_charge. ipBedBean is
   * constructed from ip_bed_details table. admBean (optional) is constructed from admission table,
   * and is inserted for new admissions.
   *
   * admBean can be null for the following cases: Bed shifting: for the new bed being added Bed
   * shifting: for the retained bed new entry being added Add bystander bed
   *
   * Note: setBillInfo must be called before calling this method.
   */

  /**
   * Allocate bed.
   *
   * @param con       the con
   * @param ipBedBean the ip bed bean
   * @param admBean   the adm bean
   * @return the string
   * @throws Exception the exception
   */
  public String allocateBed(Connection con, BasicDynaBean ipBedBean, BasicDynaBean admBean)
      throws Exception {
    return allocateBed(con, ipBedBean, admBean, false);
  }

  /**
   * Allocate bed.
   *
   * @param con          the con
   * @param ipBedBean    the ip bed bean
   * @param admBean      the adm bean
   * @param hasAdmission the has admission
   * @return the string
   * @throws Exception the exception
   */
  public String allocateBed(Connection con, BasicDynaBean ipBedBean, BasicDynaBean admBean,
      boolean hasAdmission) throws Exception {

    int bedId = (Integer) ipBedBean.get("bed_id");
    String patientId = (String) ipBedBean.get("patient_id");
    BasicDynaBean prefs = new IPPreferencesDAO().getPreferences();

    BasicDynaBean bedBean = BedMasterDAO.getBedDetailsBean(con, bedId);

    String dayCare = "N";

    // validations
    if (!bedBean.get("occupancy").equals("N")) {
      return "Bed is already occupied, cannot allocate. Choose another bed.";
    }

    // set the bed status (master) to occupied
    if (BedMasterDAO.setBedOccupied(con, bedId, "Y") <= 0) {
      return "Bed is already occupied, cannot allocate. Choose another bed.";
    }

    boolean isBystander = (Boolean) ipBedBean.get("is_bystander");
    // insert the ipbed details
    ipBedBean.set("admitted_by", userName);
    ipBedBean.set("admit_id", DataBaseUtil.getNextSequence("ip_admission_sequence"));
    ipBedBean.set("status", isBystander ? "R" : ipBedBean.get("status"));
    ipBedDao.insert(con, ipBedBean);

    boolean isRetained = ipBedBean.get("status").equals("R") || isBystander;
    // insert admission record if there does not exist one
    if (!hasAdmission) {
      dayCare = (String) admBean.get("daycare_status");
      admDao.insert(con, admBean);

    } else if (!isRetained && !isBystander) {
      dayCare = (String) admBean.get("daycare_status");
      // update the existing admission record with new bed_id
      Map<String, Object> values = new HashMap<String, Object>();
      values.put("bed_id", bedId);
      values.put("last_updated", DataBaseUtil.getDateandTime());
      values.put("daycare_status", dayCare);
      admDao.update(con, values, "patient_id", patientId);

    } else if (isRetained) {
      dayCare = (String) admBean.get("daycare_status");
    }

    BasicDynaBean finalizedBed = new IPBedDAO().getIpBedDetails(con, patientId, "C", "F");
    finalizedBed = (finalizedBed == null)
        ? new IPBedDAO().getIpBedDetails(con, patientId, "A", "F") : finalizedBed;

    if (finalizedBed != null) {
      // bed allocation after finalization, need to make bed as P and no need to
      // update charges since its already finalized
      // todo: check this.
      Map<String, Object> values = new HashMap<String, Object>();
      values.put("status", "P");
      ipBedDao.update(con, values, "admit_id", finalizedBed.get("admit_id"));
    }

    String isIcu = (String) bedBean.get("is_icu");

    // update registration bed type to admitting bed type if
    // curnt_bed_type_is_bill_bed_type is 'Y'
    // and the new bed being allocated is a billing bed type.
    if (bedBean.get("billing_bed_type").equals("Y") && (!isBystander && !isRetained)) {
      String updatePref = (String) prefs.get("current_bed_type_is_bill_bed_type");
      if (updatePref.equals("A") || (updatePref.equals("I") && isIcu.equals("N"))) {
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("bed_type", bedBean.get("bed_type"));
        values.put("ward_name", bedBean.get("ward_name"));
        values.put("ward_id", bedBean.get("ward_no"));
        values.put("user_name", this.userName);
        // todo: use patient related DAO
        patientRegistration.update(con, values, "patient_id", patientId);
      }
    }

    // Set default encounter type id if patient is day care patient.
    if (dayCare != null && dayCare.equals("Y")) {
      BasicDynaBean encBean = new VisitDetailsDAO().getVisitDefaultEncounter("i", true);
      if (encBean != null) {
        int encounterType = (Integer) encBean.get("encounter_type_id");
        Map<String, Integer> encValues = new HashMap<String, Integer>();
        encValues.put("encounter_type", encounterType);
        patientRegistration.update(con, encValues, "patient_id", patientId);
      }
    }

    return null;
  }

  /**
   * Finalize bed.
   *
   * @param con        the con
   * @param ipBedBean  the ip bed bean
   * @param closeDate  the close date
   * @param release    the release
   * @param isRetained the is retained
   * @return the string
   * @throws SQLException   the SQL exception
   * @throws IOException    Signals that an I/O exception has occurred.
   * @throws ParseException the parse exception
   */
  public String finalizeBed(Connection con, BasicDynaBean ipBedBean, Timestamp closeDate,
      boolean release, boolean isRetained) throws SQLException, IOException, ParseException {

    // update the bed details
    ipBedBean.set("end_date", closeDate);
    ipBedBean.set("username", userName);
    if (release) {
      ipBedBean.set("status", "P");
    }
    ipBedBean.set("bed_state", "F");
    ipBedDao.updateWithName(con, new String[] {"end_date", "username", "status", "bed_state"},
        ipBedBean.getMap(), "admit_id");

    // release the occupancy status of the bed master
    if (release) {
      BedMasterDAO.releaseBed(con, (Integer) ipBedBean.get("bed_id"));
    }
    return null;
  }

  /**
   * Cancel bed.
   *
   * @param con          the con
   * @param ipBedBean    the ip bed bean
   * @param admitBedBean the admit bed bean
   * @return the string
   * @throws SQLException   the SQL exception
   * @throws IOException    Signals that an I/O exception has occurred.
   * @throws ParseException the parse exception
   */
  public String cancelBed(Connection con, BasicDynaBean ipBedBean, BasicDynaBean admitBedBean)
      throws SQLException, IOException, ParseException {

    GenericDAO ipbedDao = new GenericDAO("ip_bed_details");
    GenericDAO admissionDao = new GenericDAO("admission");
    ChargeDAO chargeDao = new ChargeDAO(con);
    BasicDynaBean prefs = new IPPreferencesDAO().getPreferences();

    // 1.cancle bed
    Map keys = new HashMap();
    keys.put("status", "X");
    keys.put("bed_state", "F");

    ipbedDao.update(con, keys, "admit_id", ipBedBean.get("admit_id"));

    // 2(optional).update admission if cancles current bed
    if (admitBedBean != null) {
      keys = new HashMap();
      keys.put("bed_id", 0);
      keys.put("daycare_status", (String) admitBedBean.get("daycare_status"));
      admissionDao.update(con, keys, "patient_id", ipBedBean.get("patient_id"));
    }

    // 3.releasing the bed if its not blocked or occupied by some one else
    int bedId = (Integer) ipBedBean.get("bed_id");
    BasicDynaBean bedStatusBean = new IPBedDAO().getBedStatus(bedId);
    // can be null if bed is inactive.
    String bedStatus = bedStatusBean != null ? (String) bedStatusBean.get("status") : null;

    if (new IPBedDAO().canRelease(con, bedId) && bedStatus != null && !bedStatus.equals("B")) {
      BedMasterDAO.releaseBed(con, (Integer) ipBedBean.get("bed_id"));
    }

    String chargeId = BillActivityChargeDAO.getActiveChargeId(con, "BED",
        String.valueOf((Integer) ipBedBean.get("admit_id")));

    List<ChargeDTO> charges = new ChargeDAO(con).getChargeAndRefs(chargeId);

    if (chargeId != null) {
      String billStatus = chargeDao.getBillStatus(chargeId);
      if (billStatus != null && !billStatus.equals("A")) {
        return "Bill status is not open: cannot cancel Bed";
      }
    }

    // 4.cancelling the charges
    // cancle charge refs also
    chargeDao.cancelChargeUpdateAuditLog(con, chargeId, true, userName);

    chargeDao.cancelBillChargeClaim(con, chargeId);
    List<BasicDynaBean> associatedChargeList = chargeDao.getAssociatedCharges(con, chargeId);
    for (BasicDynaBean bean : associatedChargeList) {
      String chargeRef = String.valueOf(bean.get("charge_id"));
      chargeDao.cancelBillChargeClaim(con, chargeRef);
    }

    // 5.Update reference charge if it is main bed.
    updateReferenceBed(con, patientId, ipBedBean, admitBedBean);

    return null;
  }

  /**
   * Usefule when Terminating the main bed of thread. It shifts the main reference to immediate next
   * bed
   *
   * @param con       the con
   * @param patientId the patient id
   * @param ipBedBean the ip bed bean
   * @param admitBean the admit bean
   * @throws SQLException   the SQL exception
   * @throws IOException    Signals that an I/O exception has occurred.
   * @throws ParseException the parse exception
   */
  public void updateReferenceBed(Connection con, String patientId, BasicDynaBean ipBedBean,
      BasicDynaBean admitBean) throws SQLException, IOException, ParseException {

    BasicDynaBean prefs = new IPPreferencesDAO().getPreferences();
    GenericDAO admissionDao = new GenericDAO("admission");
    GenericDAO ipbedDao = new GenericDAO("ip_bed_details");

    if (admitBean == null) {
      admitBean = admissionDao.findByKey(con, "patient_id", patientId);
    }

    // If process is slot wise and if trying to cancle Thread first bed
    // i.e, refernce bed for rest of beds,
    // then the immediate next bed should become Thread first bed in
    // ip_bed_details as well as in bill_activity_charge tables
    if (prefs.get("merge_beds").equals("Y") && ipBedBean.get("ref_admit_id") == null) {
      List<BasicDynaBean> refBeds =
          IPBedDAO.getRefBeds(con, (Integer) ipBedBean.get("admit_id"));
      if (refBeds.size() > 0) {
        BasicDynaBean referenceBed = refBeds.get(0);
        int refAdmitId = (Integer) referenceBed.get("admit_id");
        Map keys = new HashMap();
        keys.put("ref_admit_id", null);
        // update next bed as reference bed
        ipbedDao.update(con, keys, "admit_id", refAdmitId);

        // rest all beds are refering the above bed
        for (int i = 1; i < refBeds.size(); i++) {
          referenceBed = refBeds.get(i);
          keys = new HashMap();
          keys.put("ref_admit_id", refAdmitId);
          // update next bed as reference bed
          ipbedDao.update(con, keys, "admit_id", referenceBed.get("admit_id"));
        }

        recalculateBedCharges(con, patientId);
      }

    }
  }
  /**
   * Gets cash rate
   * @param chargeDto the charge d
   * @param centerId the center id
   * @param bedType the bed type
   * @return cash rate for the given charge
   * @throws SQLException Signals that a SQL Exception has occurred.
   * @throws ParseException Signals that a Parse Exception has occurred.
   */

  public static BigDecimal getCashRate(ChargeDTO chargeDto, Integer centerId, String bedType)
      throws SQLException, ParseException {
    // String centerId = request.getSession(false).getAttribute("centerId").toString();
    BasicDynaBean centerPreferences = CenterPreferencesDAO.getAllCenterPrefs(centerId);
    String defaultRatePlan = (String) centerPreferences.get("pref_rate_plan_for_non_insured_bill");
    if (StringUtils.isEmpty(defaultRatePlan)) {
      return null;
    }
    List<ChargeDTO> chargeList = null;
    ChangeRatePlanBO changeRatePlanBo = new ChangeRatePlanBO();
    changeRatePlanBo.setRatePlanNotApplicableList(new ArrayList<String>());
    switch (ChargeGroup.valueOf(chargeDto.getChargeGroup())) {
      case DOC:
        chargeList = changeRatePlanBo.getDoctorCharges(chargeDto, defaultRatePlan, bedType);
        break;
      case DIA:
        chargeList = changeRatePlanBo.getDiagChargeNew(chargeDto, defaultRatePlan, bedType);
        break;
      case SNP:
        chargeList = changeRatePlanBo.getServiceChargeNew(chargeDto, defaultRatePlan, bedType);
        break;
      case DIE:
        chargeList = changeRatePlanBo.getDietaryChargeNew(chargeDto, defaultRatePlan, bedType);
        break;
      case OTC:
        if (chargeDto.getChargeHead().startsWith("EQ")) {
          chargeList = changeRatePlanBo.getEquipmentChargeNew(chargeDto, defaultRatePlan, bedType);
        } else if (chargeDto.getChargeHead().startsWith("MIS")) {
          break;
        } else {
          chargeList = changeRatePlanBo.getOtherChargeNew(chargeDto);
        }
        break;
      case PKG:
        if (chargeDto.getChargeHead().equals("PKGPKG")) {
          chargeList = changeRatePlanBo.getPackageChargeNew(chargeDto, defaultRatePlan, bedType);
        }
        break;
  
      case BED:
      {
        BasicDynaBean bedRates;
        if (chargeDto.getHasActivity()) {
          bedRates = bedMasterDao.getBedCharges(Integer.parseInt(chargeDto.getActDescriptionId()),
              defaultRatePlan);
        } else {
          bedRates = bedMasterDao.getNormalBedChargesBean(chargeDto.getActDescriptionId(),
              defaultRatePlan);
        }
        if (bedRates == null) {
          return null;
        }
        switch (chargeDto.getChargeHead()) {
          case ChargeDTO.CH_DUTY_DOCTOR:
            return (BigDecimal) bedRates.get("duty_charge");
          case ChargeDTO.CH_NURSE:
            return (BigDecimal) bedRates.get("nursing_charge");
          case ChargeDTO.CH_BED:
            return (BigDecimal) bedRates.get("bed_charge");
          case ChargeDTO.CH_PROFESSIONAL:
            return (BigDecimal) bedRates.get("maintainance_charge");
          default:
            break;
        }
  
      }
        break;
      case ICU:
      {
        BasicDynaBean bedRates = bedMasterDao.getIcuBedChargesBean(chargeDto.getActDescriptionId(),
            bedType, defaultRatePlan);
        if (bedRates == null) {
          return null;
        }
        switch (chargeDto.getChargeHead()) {
          case ChargeDTO.CH_DUTY_DOCTOR_ICU:
            return (BigDecimal) bedRates.get("duty_charge");
          case ChargeDTO.CH_NURSE_ICU:
            return (BigDecimal) bedRates.get("nursing_charge");
          case ChargeDTO.CH_BED_ICU:
            return (BigDecimal) bedRates.get("bed_charge");
          case ChargeDTO.CH_PROFESSIONAL_ICU:
            return (BigDecimal) bedRates.get("maintainance_charge");
          default:
            break;
        }
      }
        break;
      default:
        break;

    }

    if (chargeList != null && !chargeList.isEmpty()) {
      return (BigDecimal) chargeList.get(0).getActRate();
    }
    return null;
  }

  /**
   * Insert order charges.
   *
   * @param con               the con
   * @param charges           the charges
   * @param activityCode      the activity code
   * @param prescId           the presc id
   * @param remarks           the remarks
   * @param presDrId          the pres dr id
   * @param postedDate        the posted date
   * @param activityConducted the activity conducted
   * @param conductedOn       the conducted on
   * @param commonOrderId     the common order id
   * @param preAuthIds        the pre auth ids
   * @param preAuthModeIds    the pre auth mode ids
   * @throws SQLException the SQL exception
   * @throws IOException  Signals that an I/O exception has occurred.
   * @throws ParseException Signals that a Parse exception has occured.
   */
  public void insertOrderCharges(Connection con, List<ChargeDTO> charges, String activityCode,
      int prescId, String remarks, String presDrId, Timestamp postedDate,
      String activityConducted, Timestamp conductedOn, Integer commonOrderId,
      String[] preAuthIds, Integer[] preAuthModeIds) 
          throws SQLException, IOException, ParseException {

    ChargeDAO chargedao = new ChargeDAO(con);
    BillChargeClaimDAO chgClaimDao = new BillChargeClaimDAO();
    DiscountPlanBO discBo = new DiscountPlanBO();
    // say some details abt discount plan
    discBo.setDiscountPlanDetails(bill.get("discount_category_id") != null
        ? (Integer) bill.get("discount_category_id") : 0);

    Map<String, List<BasicDynaBean>> billChargeTaxBeanMap = new HashMap<>();
    /*
     * Set the common attributes for all charges
     */
    for (ChargeDTO charge : charges) {

      if (postedDate.getTime() < ((java.sql.Timestamp) bill.get("open_date")).getTime()) {
        postedDate = (java.sql.Timestamp) bill.get("open_date");
      }

      charge.setOrderAttributes(chargedao.getNextChargeId(), billNo, userName, remarks,
          presDrId, postedDate);
      if (charge.getDiscount().compareTo(BigDecimal.ZERO) > 0) {
        charge.setOverall_discount_amt(charge.getDiscount());
        charge.setOverall_discount_auth(-1);
      }
      charge.setBillNo(
          null != bill && bill.get("bill_no") != null ? (String) bill.get("bill_no") : null);
      /*
       * Apply discount Category discount to a item when discount category exists for a bill and
       * item charge head exists in discount catgeory charge headlist
       */
      if (charge.isAllowDiscount()) {
        discBo.applyDiscountRule(con, charge);
      }

      charge.setOrderNumber(commonOrderId);
      // need to set conductedOn for all charges, not just the main. This is so that
      // the SUOPE gets the conductedOn, which is required.
      charge.setConductedDateTime(conductedOn);
      charge.setPreAuthIds(preAuthIds);
      charge.setPreAuthModeIds(preAuthModeIds);
      charge.setInsuranceAmt(planIds, visitType, charge.getFirstOfCategory());
      

      List<BasicDynaBean> billChargeTaxBeans = new ArrayList<BasicDynaBean>();

      billChargeTaxBeans = billChargeTaxBO.getBillChargeTaxBeans(con, charge);

      billChargeTaxBeanMap.put(charge.getChargeId(), billChargeTaxBeans);
    }
    BasicDynaBean visitBean = patientRegistration.findByKey("patient_id", patientId);
    Integer centerId = visitBean != null ? (Integer) visitBean.get("center_id")
            : RequestContext.getCenterId();
    /*
     * For the main charge only: set the activity_id and code
     */
    ChargeDTO mainCharge = charges.get(0);
    mainCharge.setActivityDetails(activityCode, prescId, activityConducted, conductedOn);
    if (mainCharge.getActivityCode().equals("OPE")) {
      this.mainChargeId = mainCharge.getChargeId();
    }
    mainCharge.setCashRate(getCashRate(mainCharge, centerId,bedType ));

    /*
     * For all other charges: set chargeref to main charge, and also has activity.
     */
    for (ChargeDTO charge : charges.subList(1, charges.size())) {
      charge.setActivityConducted(activityConducted);
      charge.setChargeRef(mainCharge.getChargeId());
      charge.setHasActivity(true);
      if (remarks != null && !charge.getChargeHead().equals("ANATOPE")) {
        charge.setActRemarks(mainCharge.getActRemarks());
      }
      charge.setCashRate(getCashRate(charge, centerId,bedType ));
    } 

    chargedao.insertCharges(charges);
    billChargeTaxDAO.insertBillChargeTaxes(con, billChargeTaxBeanMap);

    if (isInsurance) {
      chgClaimDao.insertBillChargeClaims(con, charges, planIds, patientId, billNo);
    }
    
    
  }

  /**
   * Gets the discount rule.
   *
   * @param con  the con
   * @param cdto the cdto
   * @return the discount rule
   * @throws SQLException the SQL exception
   */
  public BasicDynaBean getDiscountRule(Connection con, ChargeDTO cdto) throws SQLException {
    List<BasicDynaBean> discountPlanDetails = new GenericDAO("discount_plan_details").listAll(
        con, null, "discount_plan_id", (Integer) bill.get("discount_category_id"), "priority");
    BasicDynaBean discountRuleBean = null;

    /*
     * applicable_type tells on which to apply discount rule.It can have 3 values. N : insurance
     * category id of item in the charge C : charge head of the charge I : item id of the charge. :
     * if it is item id there is one more parameter which will decide which type of item to look at
     * it.
     */
    for (BasicDynaBean detailBean : discountPlanDetails) {
      if ((detailBean.get("applicable_type").equals("N")
          && cdto.getInsuranceCategoryId() == Integer
              .parseInt(((String) detailBean.get("applicable_to_id")).trim()))
          || (detailBean.get("applicable_type").equals("C") && cdto.getChargeHead()
              .equals(((String) detailBean.get("applicable_to_id")).trim()))
          || (detailBean.get("applicable_type").equals("I") && cdto.getActDescriptionId()
              .equals(((String) detailBean.get("applicable_to_id")).trim()))) {
        discountRuleBean = detailBean;
        break;
      }
    }

    return discountRuleBean;

  }

  /**
   * Insert secondary operation order charges.
   *
   * @param con               the con
   * @param charges           the charges
   * @param activityCode      the activity code
   * @param prescId           the presc id
   * @param remarks           the remarks
   * @param presDrId          the pres dr id
   * @param postedDate        the posted date
   * @param activityConducted the activity conducted
   * @param conductedOn       the conducted on
   * @param commonOrderId     the common order id
   * @param preAuthId         the pre auth id
   * @param preAuthModeId     the pre auth mode id
   * @param mainChargeId      the main charge id
   * @throws SQLException the SQL exception
   * @throws IOException  Signals that an I/O exception has occurred.
   */
  public void insertSecondaryOperationOrderCharges(Connection con, List<ChargeDTO> charges,
      String activityCode, int prescId, String remarks, String presDrId, Timestamp postedDate,
      String activityConducted, Timestamp conductedOn, Integer commonOrderId, String preAuthId,
      Integer preAuthModeId, String mainChargeId) throws SQLException, IOException {

    ChargeDAO chargedao = new ChargeDAO(con);
    BillChargeClaimDAO chgClaimDao = new BillChargeClaimDAO();
    /*
     * Set the common attributes for all charges
     */
    for (ChargeDTO charge : charges) {

      if (postedDate.getTime() < ((java.sql.Timestamp) bill.get("open_date")).getTime()) {
        postedDate = (java.sql.Timestamp) bill.get("open_date");
      }

      charge.setOrderAttributes(chargedao.getNextChargeId(), billNo, userName, remarks,
          presDrId, postedDate);
      if (charge.getDiscount().compareTo(BigDecimal.ZERO) > 0) {
        charge.setOverall_discount_amt(charge.getDiscount());
        charge.setOverall_discount_auth(-1);
      }
      charge.setOrderNumber(commonOrderId);
      // need to set conductedOn for all charges, not just the main. This is so that
      // the SUOPE gets the conductedOn, which is required.
      charge.setConductedDateTime(conductedOn);
      charge.setPreAuthId(preAuthId);
      charge.setPreAuthModeId(preAuthModeId);
      charge.setInsuranceAmt(planIds, visitType, charge.getFirstOfCategory());
      charge.setBillNo(
          null != bill && bill.get("bill_no") != null ? (String) bill.get("bill_no") : null);
    }

    /*
     * here no main cahrges all charges are going to bill chatrge only which will be reffered by
     * mainChargeId, in bill_activity charge
     */
    ChargeDTO mainCharge = charges.get(0);
    if ( mainChargeId == null ) {
      mainChargeId = mainCharge.getChargeId();
    }
    mainCharge.setActivityDetails(null, 0, activityConducted, conductedOn);
    Map<String, List<BasicDynaBean>> billChargeTaxBeanMap = new HashMap<>();
    /*
     * For all other charges: set chargeref to main charge, and also has activity.
     */
    for (ChargeDTO charge : charges) {
      charge.setActivityConducted(activityConducted);
      charge.setChargeRef(mainChargeId);
      charge.setHasActivity(true);
      if (remarks != null && !charge.getChargeHead().equals("ANATOPE")) {
        charge.setActRemarks(mainCharge.getActRemarks());
      }

      List<BasicDynaBean> billChargeTaxBeans = new ArrayList<BasicDynaBean>();

      billChargeTaxBeans = billChargeTaxBO.getBillChargeTaxBeans(con, charge);

      billChargeTaxBeanMap.put(charge.getChargeId(), billChargeTaxBeans);
    }

    chargedao.insertCharges(charges);
    billChargeTaxDAO.insertBillChargeTaxes(con, billChargeTaxBeanMap);
    if (isInsurance) {
      chgClaimDao.insertBillChargeClaims(con, charges, planIds, patientId, billNo);
    }
  }

  /**
   * Update charge amounts.
   *
   * @param con                   the con
   * @param origCharges           the orig charges
   * @param newAmounts            the new amounts
   * @param activityCode          the activity code
   * @param activityId            the activity id
   * @param keepOriginalDiscounts the keep original discounts
   * @throws SQLException the SQL exception
   * @throws Exception    the exception
   */
  public void updateChargeAmounts(Connection con, List<ChargeDTO> origCharges,
      List<ChargeDTO> newAmounts, String activityCode, String activityId,
      Boolean keepOriginalDiscounts) throws SQLException, Exception {

    ChargeDTO mainCharge = origCharges.get(0);
    List<ChargeDTO> updatedCharges = new ArrayList<ChargeDTO>();
    List<ChargeDTO> insertedCharges = new ArrayList<ChargeDTO>();
    List<ChargeDTO> deleteCharges = new ArrayList<ChargeDTO>();
    ChargeDAO chargedao = new ChargeDAO(con);
    String billNo = "";

    for (ChargeDTO ch : newAmounts) {
      String chargeHead = ch.getChargeHead();
      String units =
          ch.getActUnit() == null || ch.getActUnit().equals("") ? "" : ch.getActUnit();
      String actDescriptionId = ch.getActDescriptionId();
      String actRemarks = ch.getActRemarks();
      ChargeDTO existingCharge = null;

      // find the corresponding existing charge
      // can not have set of charges with same chargeHead and units
      for (ChargeDTO echarge : origCharges) {
        String eunit = echarge.getActUnit() == null || echarge.getActUnit().equals("") ? ""
            : echarge.getActUnit();
        String eactRemarks = (echarge.getActRemarks() == null || echarge.getActRemarks().equals(""))
            ? ""
            : echarge.getActRemarks();
        if (echarge.getChargeHead().equals("ANATOPE")
            && echarge.getActDescriptionId().equals(actDescriptionId)
            && (eactRemarks.equals("") || eactRemarks.equals(actRemarks))
            && echarge.getChargeHead().equals(chargeHead) && eunit.equals(units)) {

          existingCharge = echarge;
          break;

        } else {
          if (!echarge.getChargeHead().equals("ANATOPE")
              && echarge.getChargeHead().equals(chargeHead) && eunit.equals(units)) {

            existingCharge = echarge;
            break;
          }
        }
      }

      if (existingCharge != null) {

        // If existing charge is cancelled which is a reference charge then uncancel
        // the charge and update charge.
        existingCharge.setStatus("A");

        // update this charge with new amounts
        existingCharge.setActRatePlanItemCode(ch.getActRatePlanItemCode());
        existingCharge.setCodeType(ch.getCodeType());
        if (keepOriginalDiscounts) {
          if (existingCharge.getOverall_discount_auth() != -1
              && ch.getDiscount().compareTo(BigDecimal.ZERO) == 0) {
            existingCharge.copyChargeAmountsWithoutDiscountFrom(ch, true);
            existingCharge.setUsername(this.userName);
            BigDecimal exisitingDiscount = existingCharge.getDiscount();
            BigDecimal existIngAmount = existingCharge.getAmount();
            if (!existingCharge.getChargeHead().equals("INVRET")) {
              exisitingDiscount = exisitingDiscount.min(existIngAmount);
            }
            existingCharge.setDiscount(exisitingDiscount);
            existingCharge.setOverall_discount_amt(exisitingDiscount);
            existingCharge.setAmount(existIngAmount.subtract(exisitingDiscount));
            String visitId =
                existingCharge.getVisitId() == null || existingCharge.getVisitId().equals("")
                    ? new BillDAO(con).getBill(existingCharge.getBillNo()).getVisitId()
                    : existingCharge.getVisitId();
            BasicDynaBean visitBean = new VisitDetailsDAO().getVisitDetails(con, visitId);
            if (null != planIds) {
              // existingCharge.setInsuranceAmt(planIds, existingCharge.getVisitType(),
              // existingCharge.getFirstOfCategory());
            }

          } else {
            existingCharge.setOverall_discount_auth(ch.getOverall_discount_auth());
            if (-1 == ch.getOverall_discount_auth()) {
              existingCharge.setIsSystemDiscount("Y");
            }
            existingCharge.copyChargeAmountsFrom(ch, true);
          }
        } else {
          existingCharge.setOverall_discount_auth(ch.getOverall_discount_auth());
          if (-1 == ch.getOverall_discount_auth()) {
            existingCharge.setIsSystemDiscount("Y");
          }
          existingCharge.copyChargeAmountsFrom(ch, true);
        }

        // Set insurance claim amount as Zero for non-insured bill charges.
        boolean isTpa = (Boolean) ((BasicDynaBean) new GenericDAO("bill").findByKey(con,
            "bill_no", existingCharge.getBillNo())).get("is_tpa");
        billNo = existingCharge.getBillNo();

        if (!isTpa) {
          existingCharge.setInsuranceClaimAmount(BigDecimal.ZERO);
        }

        updatedCharges.add(existingCharge);

      } else {
        // insert a new charge
        ch.setOrderAttributes(chargedao.getNextChargeId(), mainCharge.getBillNo(), userName,
            mainCharge.getActRemarks(), mainCharge.getPrescribingDrId(),
            DateUtil.getCurrentTimestamp());
        ch.setActRatePlanItemCode(mainCharge.getActRatePlanItemCode());
        ch.setChargeRef(mainCharge.getChargeId());
        ch.setHasActivity(true);
        ch.setCodeType(mainCharge.getCodeType());
        insertedCharges.add(ch);
      }
    }

    for (ChargeDTO ch : origCharges) {
      String chargeHead = ch.getChargeHead();
      String units = ch.getActUnit();
      ChargeDTO existingCharge = null;

      // find the corresponding existing charge
      // can not have set of charges with same chargeHead and units
      for (ChargeDTO echarge : newAmounts) {
        if (echarge.getChargeHead().equals(chargeHead) && echarge.getActUnit().equals(units)) {
          existingCharge = echarge;
          break;
        }
      }

      if (existingCharge == null) {

        ch.setStatus("X");
        deleteCharges.add(ch);
      }
    }

    chargedao.updateChargeAmountsList(updatedCharges);
    chargedao.insertCharges(insertedCharges);
    chargedao.updateChargeAmountsList(deleteCharges);

    BasicDynaBean visitBean = patientRegistration.findByKey("patient_id", patientId);
    Integer centerId = visitBean != null ? (Integer) visitBean.get("center_id")
        : RequestContext.getCenterId();
    String healthAuthority = CenterMasterDAO.getHealthAuthorityForCenter(centerId);

    BasicDynaBean billDto = new BillDAO(con).findByKey("bill_no", billNo);
    String serBillNo = null;
    if (null != billDto) {
      serBillNo = (String) billDto.get("bill_no");
    }
    if (insertedCharges != null && !insertedCharges.isEmpty() && billDto != null) {
      billChargeTaxDAO.calculateAndInsertBillChargeTaxes(con, insertedCharges, billDto);
      if ("DHA".equals(healthAuthority)) {
        // pending verification DHA flow.Post which we can remove the commented code
        // chargedao.insertBillChargeTransaction(con, insertedCharges, serBillNo);
        chargedao.insertSpecialServiceObservation(con, insertedCharges, serBillNo);
      }
    }
    if (billDto != null) {
      billChargeTaxDAO.calculateAndUpdateBillChargeTaxes(con, updatedCharges, billDto);
      if ("DHA".equals(healthAuthority)) {
        chargedao.updateBillChargeTransaction(con, updatedCharges, serBillNo);
        chargedao.updateSpecialServiceObservation(con, updatedCharges, serBillNo);
      }
    }

  }

  /**
   * Recalculate charge amounts.
   *
   * @param con             the con
   * @param originalCharges the original charges
   * @param newCharges      the new charges
   * @throws SQLException the SQL exception
   * @throws IOException  Signals that an I/O exception has occurred.
   */
  public void recalculateChargeAmounts(Connection con, List<ChargeDTO> originalCharges,
      List<ChargeDTO> newCharges) throws SQLException, IOException {

    List<ChargeDTO> deleteCharges = new ArrayList<ChargeDTO>();
    List<ChargeDTO> updatedCharges = new ArrayList<ChargeDTO>();
    List<ChargeDTO> insertedCharges = new ArrayList<ChargeDTO>();
    ChargeDAO chargedao = new ChargeDAO(con);

    ChargeDTO mainCharge = null;
    if (newCharges.size() > 0) {
      // new charges can be empty if we are removing all charges.
      mainCharge = newCharges.get(0);
      mainCharge.setChargeRef(null);
    }
    String mainChargeId = null;

    String origMainChargeId = originalCharges.get(0).getChargeId();
    for (ChargeDTO ch : newCharges) {
      String chargeHead = ch.getChargeHead();
      String units = ch.getActUnit();
      String descrId = ch.getActDescriptionId();
      ChargeDTO existingCharge = null;

      for (ChargeDTO echarge : originalCharges) {
        if (echarge.getChargeHead().equals(chargeHead) && echarge.getActUnit().equals(units)
            && echarge.getActDescriptionId().equals(descrId)) {
          existingCharge = echarge;
          originalCharges.remove(echarge);
          break;
        }
      }

      if (existingCharge != null) {
        // update this charge with new amounts
        existingCharge.copyChargeAmountsFrom(ch, true);
        updatedCharges.add(existingCharge);
        if (mainChargeId == null) {
          mainChargeId = existingCharge.getChargeId();
        } else {
          existingCharge.setChargeRef(mainChargeId);
        }

      } else {
        // need to add a new charge
        ch.setOrderAttributes(chargedao.getNextChargeId(), this.billNo, userName, null,
            mainCharge.getPrescribingDrId(), DateUtil.getCurrentTimestamp());
        ch.setHasActivity(true);
        ch.setActivityConducted(mainCharge.getActivityConducted());
        if (mainChargeId == null) {
          mainChargeId = ch.getChargeId();
        } else {
          ch.setChargeRef(mainChargeId);
        }
        insertedCharges.add(ch);
      }
    }
    String mainOriginalChargeId = origMainChargeId;
    // All remaining charges in original set have to be deleted.
    for (ChargeDTO ch : originalCharges) {
      ch.setStatus("X");
      ch.setChargeRef(mainChargeId);
      ch.setAmount(BigDecimal.ZERO);
      ch.setDiscount(BigDecimal.ZERO);
      ch.setTaxAmt(BigDecimal.ZERO);
      deleteCharges.add(ch);
    }

    boolean newChargeForMainChageExists = false;
    for (ChargeDTO ch : insertedCharges) {
      newChargeForMainChageExists = (ch.getChargeId().equals(mainChargeId));
      if (newChargeForMainChageExists) {
        break;
      }
    }

    BillActivityChargeDAO activityDao = new BillActivityChargeDAO(con);
    if ((newCharges.size() > 0) && !mainChargeId.equals(mainOriginalChargeId)) {
      if (newChargeForMainChageExists) {
        activityDao.deleteActivity("" + mainCharge.getActivityId(), "BED");
      } else {
        activityDao.updateActivityCharge(String.valueOf(mainCharge.getActivityId()), "BED",
            mainChargeId);
        /**
         * since we update old activity to have new charge id.This should not be main charge in
         * bill_charge as well. Update charge_ref of this charge to null.
         */

        for (ChargeDTO ch : updatedCharges) {
          if (ch.getChargeId().equals(mainChargeId)) {
            ch.setChargeRef(null);
          }

        }
      }
    }

    chargedao.updateChargeAmountsList(updatedCharges);
    chargedao.insertCharges(insertedCharges);
    chargedao.updateChargeAmountsList(deleteCharges);
    chargedao.updateChargeRefsList(updatedCharges);

    BillChargeClaimDAO chgClaimDao = new BillChargeClaimDAO();
    chgClaimDao.cancelBillChargeClaims(con, deleteCharges);// to set claim amt to 0
    for (ChargeDTO chg : insertedCharges) {
      chg.setInsuranceAmt(planIds, visitType, chg.getFirstOfCategory());
    }
    if (null != insertedCharges && insertedCharges.size() > 0 && null != planIds
        && this.isInsurance) {
      chgClaimDao.insertBillChargeClaims(con, insertedCharges, planIds, patientId, billNo);
    }

    for (ChargeDTO chg : updatedCharges) {
      chg.setInsuranceAmt(planIds, visitType, chg.getFirstOfCategory());
    }
    if (null != planIds && this.isInsurance) {
      chgClaimDao.updateBillChargeClaims(con, updatedCharges, patientId, billNo, planIds,
          false);
    }

    BasicDynaBean billDto = new BillDAO(con).findByKey("bill_no", billNo);
    billChargeTaxDAO.calculateAndUpdateBillChargeTaxes(con, updatedCharges, billDto);
  }

  /**
   * Gets the duration.
   *
   * @param fromDateTimeStr the from date time str
   * @param toDateTimeStr   the to date time str
   * @param units           the units
   * @return the duration
   * @throws ParseException the parse exception
   */
  public static int getDuration(String fromDateTimeStr, String toDateTimeStr, String units)
      throws ParseException {

    Timestamp from = DateUtil.parseTimestamp(fromDateTimeStr);
    Timestamp to = DateUtil.parseTimestamp(toDateTimeStr);
    return getDuration(from, to, units);
  }

  /**
   * Gets the duration.
   *
   * @param from  the from
   * @param to    the to
   * @param units the units
   * @return the duration
   */
  public static int getDuration(Timestamp from, Timestamp to, String units) {
    return getDuration(from, to, units, 60);
  }

  /**
   * Gets the duration.
   *
   * @param from     the from
   * @param to       the to
   * @param type     the type
   * @param unitSize the unit size
   * @return the duration
   */
  public static int getDuration(Timestamp from, Timestamp to, String type, int unitSize) {
    long timeDiff = to.getTime() - from.getTime(); // milliseconds
    int minutes = (int) (timeDiff / 60 / 1000);

    int duration;
    if (type.equals("D")) {
      // any part of an hour is considered a full hour, eg, 60 minutes = 1hr, but 61
      // minutes = 2 hrs
      int hours = minutes / 60 + ((minutes % 60 > 0) ? 1 : 0);
      duration = hours / 24 + ((hours % 24 > 0) ? 1 : 0);
    } else {
      /*
       * We use ceil (any part thereof): if unitSize is 15, then the following are the conversions:
       * 0-15: 1, 16-30: 2, 31-45: 3, 46-60: 4, 61-75: 5 ...
       */
      duration = minutes / unitSize + ((minutes % unitSize > 0) ? 1 : 0);
    }

    return duration;
  }

  /**
   * Gets the duration charge.
   *
   * @param duration      the duration
   * @param minDuration   the min duration
   * @param slab1Duration the slab 1 duration
   * @param incrDuration  the incr duration
   * @param minCharge     the min charge
   * @param slab1Rate     the slab 1 rate
   * @param incrRate      the incr rate
   * @param splitCharge   the split charge
   * @return the duration charge
   */
  public static BigDecimal getDurationCharge(int duration, int minDuration, int slab1Duration,
      int incrDuration, BigDecimal minCharge, BigDecimal slab1Rate, BigDecimal incrRate,
      boolean splitCharge) {

    return getDurationCharge(duration, minDuration, slab1Duration, slab1Duration, incrDuration,
        minCharge, slab1Rate, slab1Rate, incrRate, splitCharge);
  }

  /**
   * Gets the duration charge.
   *
   * @param duration      the duration
   * @param minDuration   the min duration
   * @param slab1Duration the slab 1 duration
   * @param slab2Duration the slab 2 duration
   * @param incrDuration  the incr duration
   * @param minCharge     the min charge
   * @param slab1Rate     the slab 1 rate
   * @param slab2Rate     the slab 2 rate
   * @param incrRate      the incr rate
   * @param splitCharge   the split charge
   * @return the duration charge
   */
  public static BigDecimal getDurationCharge(int duration, int minDuration, int slab1Duration,
      int slab2Duration, int incrDuration, BigDecimal minCharge, BigDecimal slab1Rate,
      BigDecimal slab2Rate, BigDecimal incrRate, boolean splitCharge) {

    logger.debug("Getting duration charge for " + duration + ": " + minDuration + " "
        + slab1Duration + " " + slab2Duration + " " + incrDuration + " " + minCharge + " "
        + slab1Rate + " " + slab2Rate + " " + incrRate);

    if (duration <= minDuration) {
      return minCharge;

    } else if (duration <= slab1Duration) {
      return slab1Rate;

    } else if (duration <= slab2Duration) {
      return slab2Rate;

    } else {
      if (incrDuration != 0) {
        int addnlUnits = duration - slab2Duration; // eg, 5 - 4 = 1
        // again we apply ceil, ie, any part thereof will get into the next slot
        // eg, 1-2 => 1, 3-4 => 2 etc.
        int incrUnits = addnlUnits / incrDuration + (addnlUnits % incrDuration > 0 ? 1 : 0);
        if (splitCharge) {
          return incrRate.multiply(new BigDecimal(incrUnits));
        } else {
          return slab2Rate.add(incrRate.multiply(new BigDecimal(incrUnits)));
        }

      } else {
        if (minDuration == 0) {
          return minCharge.add(incrRate.multiply(new BigDecimal(duration)));
        } else {
          return minCharge;
        }
      }
    }
  }

  /**
   * Gets the base charge.
   *
   * @param totalUnit  the total unit
   * @param baseCharge the base charge
   * @return the base charge
   */
  public static BigDecimal getBaseCharge(Integer totalUnit, BigDecimal baseCharge) {
    return baseCharge.multiply(new BigDecimal(totalUnit));
  }

  /**
   * Gets the item charges.
   *
   * @param orgId                    the org id
   * @param bedType                  the bed type
   * @param itemType                 the item type
   * @param id                       the id
   * @param chargeType               the charge type
   * @param quantity                 the quantity
   * @param from                     the from
   * @param to                       the to
   * @param units                    the units
   * @param ot                       the ot
   * @param surgeon                  the surgeon
   * @param anaesthetist             the anaesthetist
   * @param visitType                the visit type
   * @param operationId              the operation id
   * @param isInsurance              the is insurance
   * @param finalized                the finalized
   * @param anesthesiaTypes          the anesthesia types
   * @param patientId                the patient id
   * @param firstOfCategory          the first of category
   * @param billNo                   the bill no
   * @param isNonInsuBill            the is non insu bill
   * @param multiVisitPackage        the multi visit package
   * @param packObId                 the pack ob id
   * @param packageId                the package id
   * @param anesthesiaTypesFromDates the anesthesia types from dates
   * @param anesthesiaTypesToDates   the anesthesia types to dates
   * @param anesthesiaTypesFromTimes the anesthesia types from times
   * @param anesthesiaTypesToTimes   the anesthesia types to times
   * @return the item charges
   * @throws SQLException the SQL exception
   * @throws Exception    the exception
   */
  public static List<ChargeDTO> getItemCharges(String orgId, String bedType, String itemType,
      String id, String chargeType, BigDecimal quantity, Timestamp from, Timestamp to,
      String units, String ot, String surgeon, String anaesthetist, String visitType,
      String operationId, Boolean isInsurance, String finalized, String[] anesthesiaTypes,
      String patientId, Boolean firstOfCategory, String billNo, boolean isNonInsuBill,
      Boolean multiVisitPackage, String packObId, String packageId,
      String[] anesthesiaTypesFromDates, String[] anesthesiaTypesToDates,
      String[] anesthesiaTypesFromTimes, String[] anesthesiaTypesToTimes)
      throws SQLException, Exception {

    if (id == null || id.equals("")) {
      return null;
    }

    List<ChargeDTO> charges = null;

    BasicDynaBean bill = BillDAO.getBillBean(billNo);

    int[] planIds = new PatientInsurancePlanDAO().getPlanIds(patientId);
    BasicDynaBean visitBean = patientRegistration.findByKey("patient_id", patientId);
    Integer centerId = visitBean != null ? (Integer) visitBean.get("center_id")
        : RequestContext.getCenterId();
    String ratePlnForNonInsuBills =
        CenterPreferencesDAO.getRatePlanForNonInsuredBills(centerId);

    if (bill != null) {
      orgId = (String) bill.get("bill_rate_plan_id");
    } else if (isInsurance && isNonInsuBill && ratePlnForNonInsuBills != null) {
      orgId = ratePlnForNonInsuBills;
    }

    if (multiVisitPackage) {
      charges = getMultiVisitPackItemCharges(packageId, packObId, bedType, orgId, id, quantity,
          isInsurance, visitType, patientId, firstOfCategory);
    } else if (itemType.equals("Operation")) {
      BasicDynaBean theatre = theatreMasterDao.getTheatreChargeDetails(ot, bedType, orgId);
      BasicDynaBean operBean = operationMasterDao.getOperationChargeBean(id, bedType, orgId);
      BasicDynaBean surgeonBean = DoctorMasterDAO.getOTDoctorChargesBean(surgeon, bedType, orgId);
      BasicDynaBean anasthesiaTypeChargeBean = null;
      List<ChargeDTO> anaesTypeCharges = new ArrayList<ChargeDTO>();
      BasicDynaBean anaBean = null;
      if (anaesthetist != null && !anaesthetist.equals("")) {
        anaBean = DoctorMasterDAO.getOTDoctorChargesBean(anaesthetist, bedType, orgId);
      }
      charges = getOperationCharges(id, operBean, theatre, surgeonBean, anaBean, from, to,
          units, isInsurance, planIds, finalized, visitType, null, patientId, firstOfCategory);

      if (anesthesiaTypes != null && anesthesiaTypes.length > 0) {
        for (int i = 0; i < anesthesiaTypes.length; i++) {
          anasthesiaTypeChargeBean = new AnaesthesiaTypeChargesDAO()
              .getAnasthesiaTypeCharge(anesthesiaTypes[i], bedType, orgId);

          Timestamp anaeTypeFromTime = new DateUtil().parseTheTimestamp(
              anesthesiaTypesFromDates[i] + " " + anesthesiaTypesFromTimes[i]);
          Timestamp anaeTypeToTime = new DateUtil()
              .parseTheTimestamp(anesthesiaTypesToDates[i] + " " + anesthesiaTypesToTimes[i]);

          if (anasthesiaTypeChargeBean != null) {
            anaesTypeCharges = getAnaesthesiaTypeCharges(id, operBean, anaeTypeFromTime,
                anaeTypeToTime, units, isInsurance, planIds, finalized, visitType,
                anasthesiaTypeChargeBean, patientId, firstOfCategory);
          }

          if (charges != null) {
            charges.addAll(anaesTypeCharges);
          } else {
            charges = anaesTypeCharges;
          }
        }
      }
    } else if (itemType.equals("Service")) {
      BasicDynaBean service = new MasterServicesDao().getServiceChargeBean(id, bedType, orgId);
      charges = getServiceCharges(service, quantity, isInsurance, planIds, visitType,
          patientId, firstOfCategory, null);

    } else if (itemType.equals("Equipment")) {
      BasicDynaBean equipDetails =
          new EquipmentChargeDAO().getEquipmentCharge(id, bedType, orgId);
      charges = getEquipmentCharges(equipDetails, from, to, units, false, quantity,
          isInsurance, planIds, visitType, patientId, firstOfCategory);

    } else if (itemType.equals("Laboratory") || itemType.equals("Radiology")) {
      BasicDynaBean testDetails = AddTestDAOImpl.getTestDetails(id, bedType, orgId);
      charges = getTestCharges(testDetails, quantity, isInsurance, planIds, visitType,
          patientId, firstOfCategory, null);

    } else if ("Other Charge".equals(itemType)) {
      BasicDynaBean otherService = new CommonChargesDAO().getCommonCharge(id);
      charges = getOtherCharges(otherService, quantity, isInsurance, planIds, visitType,
          patientId, firstOfCategory);

    } else if ("Meal".equals(itemType)) {
      BasicDynaBean meal = new DietaryMasterDAO().getChargeForMeal(orgId, id, bedType);
      charges = getMealCharges(meal, quantity, isInsurance, planIds, visitType, patientId,
          firstOfCategory);

    } else if ("Doctor".equals(itemType)) {
      if (id.equals("Doctor")) {
        charges = getPackageDoctorCharges(chargeType, isInsurance, planIds, visitType,
            patientId, firstOfCategory);
      } else {
        BasicDynaBean doctorBean = DoctorMasterDAO.getDoctorCharges(id, orgId, bedType);
        BasicDynaBean opMasterBean = null;

        if (operationId != null && !operationId.equals("")) {
          opMasterBean = operationMasterDao.getOperationChargeBean(operationId, bedType,
              orgId);
          if (opMasterBean == null) {
            logger.warn("No operation found for: " + operationId);
          }
          charges = getOtDoctorCharges(doctorBean, chargeType, visitType, opMasterBean,
              quantity, isInsurance, planIds, bedType, patientId, firstOfCategory);
        } else {
          BasicDynaBean consTypeBean = getConsultationTypeBean(Integer.parseInt(chargeType));
          charges = getDoctorConsCharges(doctorBean, consTypeBean, visitType,
              OrgMasterDao.getOrgdetailsDynaBean(orgId), quantity, isInsurance, planIds,
              bedType, patientId, firstOfCategory);
        }
      }

    } else if ("Package".equals(itemType)) {
      BasicDynaBean pkg = PackageDAO.getPackageDetails(Integer.parseInt(id), orgId, bedType);
      charges = getPackageCharges(pkg, quantity, surgeon, isInsurance, planIds, visitType,
          patientId, firstOfCategory);

    } else if ("Bed".equals(itemType)) {
      BasicDynaBean bedCharges = bedMasterDao.getNormalBedChargesBean(id, orgId);
      charges = getBedCharges("BED", bedCharges, from, to, quantity, false, "N", false, isInsurance,
          planIds, visitType, patientId, true, "O", firstOfCategory, null);

    } else if ("ICU".equals(itemType)) {
      BasicDynaBean bedCharges = bedMasterDao.getIcuBedChargesBean(id, bedType, orgId);
      charges = getBedCharges("ICU", bedCharges, from, to, quantity, false, "N", false, isInsurance,
          planIds, visitType, patientId, true, "O", firstOfCategory, null);

    } else if ("Direct Charge".equals(itemType)) {

      if (id.contains("REG")) {
        boolean isRenewal = (chargeType != null) && chargeType.equalsIgnoreCase("renewal");
        charges = getRegistrationCharges(bedType, orgId, id, isRenewal, isInsurance, planIds,
            false, visitType, patientId, null, firstOfCategory);

      } else {
        charges = getDirectCharges(id);
      }
    }
    BillingHelper billingHelper = new BillingHelper();
    billingHelper.saveBillChargeBillingGroup(charges);
    return charges;
  }

  /**
   * Gets the operation charges.
   *
   * @param opnId              the opn id
   * @param opn                the opn
   * @param theatre            the theatre
   * @param surgeonDoc         the surgeon doc
   * @param anaDoc             the ana doc
   * @param from               the from
   * @param to                 the to
   * @param units              the units
   * @param isInsurance        the is insurance
   * @param planIds            the plan ids
   * @param finalizationStatus the finalization status
   * @param visitType          the visit type
   * @param anasthesiaTypeBean the anasthesia type bean
   * @param patientId          the patient id
   * @param firstOfCategory    the first of category
   * @return the operation charges
   * @throws SQLException the SQL exception
   */
  public static List<ChargeDTO> getOperationCharges(String opnId, BasicDynaBean opn,
      BasicDynaBean theatre, BasicDynaBean surgeonDoc, BasicDynaBean anaDoc, Timestamp from,
      Timestamp to, String units, Boolean isInsurance, int[] planIds, String finalizationStatus,
      String visitType, BasicDynaBean anasthesiaTypeBean, String patientId, Boolean firstOfCategory)
          throws SQLException {
    return getOperationCharges(opnId, opn,
              theatre, surgeonDoc, anaDoc, from, to, units, isInsurance, planIds,
              "Y", (String) visitType, anasthesiaTypeBean, patientId, firstOfCategory, false);
  }

  /**
   * Gets the operation charges.
   *
   * @param opnId              the opn id
   * @param opn                the opn
   * @param theatre            the theatre
   * @param surgeonDoc         the surgeon doc
   * @param anaDoc             the ana doc
   * @param from               the from
   * @param to                 the to
   * @param units              the units
   * @param isInsurance        the is insurance
   * @param planIds            the plan ids
   * @param finalizationStatus the finalization status
   * @param visitType          the visit type
   * @param anasthesiaTypeBean the anasthesia type bean
   * @param patientId          the patient id
   * @param firstOfCategory    the first of category
   * @param isSecondary        the operation is secondary
   * @return the operation charges
   * @throws SQLException the SQL exception
   */
  public static List<ChargeDTO> getOperationCharges(String opnId, BasicDynaBean opn,
      BasicDynaBean theatre, BasicDynaBean surgeonDoc, BasicDynaBean anaDoc, Timestamp from,
      Timestamp to, String units, Boolean isInsurance, int[] planIds, String finalizationStatus,
      String visitType, BasicDynaBean anasthesiaTypeBean, String patientId, 
      Boolean firstOfCategory, Boolean isSecondary)
      throws SQLException {

    List lis = new ArrayList();
    BasicDynaBean gprefs = GenericPreferencesDAO.getPrefsBean();
    BasicDynaBean ipprefs = new IPPreferencesDAO().getPreferences();
    String splitTheatreCharges = ipprefs.get("split_theatre_charges") != null
        ? (String) ipprefs.get("split_theatre_charges") : "N";

    if (gprefs.get("fixed_ot_charges").equals("Y")) {
      from = DataBaseUtil.getDateandTime();
      to = DataBaseUtil.getDateandTime();
    }

    String itemCode = null;
    String ratePlanItemCode = null;
    String operName = "";
    String theatreCodeType = null;
    String codeType = null;
    boolean addSurAssCharge = false;
    int serviceSubGroupId = (Integer) opn.get("service_sub_group_id");

    if (opn != null) {
      itemCode = (String) opn.get("operation_code");
      operName = (String) opn.get("operation_name");
      ratePlanItemCode = (String) opn.get("item_code");
      operName = (String) opn.get("operation_name");
      codeType = (String) opn.get("code_type");
      theatreCodeType = ipprefs.get("theatre_charge_code_type") == null ? ""
          : (String) ipprefs.get("theatre_charge_code_type");
    }

    int insuranceCategoryId = getIntegerValueFromBean(opn, "insurance_category_id");

    if (theatre != null) {
      if (units.equals("D")) {
        BigDecimal rate = (BigDecimal) theatre.get("daily_charge");
        BigDecimal discount = (BigDecimal) theatre.get("daily_charge_discount");
        int qty = getDuration(from, to, "D");
        String dialyChrgItemCode = ipprefs.get("theatre_daily_charge_code") == null ? ""
            : (String) ipprefs.get("theatre_daily_charge_code");

        ChargeDTO thCharge = new ChargeDTO("OPE", "TCOPE", rate, new BigDecimal(qty),
            discount.multiply(new BigDecimal(qty)), "Days", (String) theatre.get("theatre_id"),
            operName + "/" + (String) theatre.get("theatre_name"), null, isInsurance,
            serviceSubGroupId, insuranceCategoryId, visitType, patientId, firstOfCategory);
        // thCharge.setInsuranceAmt(planIds, visitType, thCharge.getFirstOfCategory());
        thCharge.setOp_id(opnId);
        thCharge.setActItemCode(itemCode);
        thCharge.setActRatePlanItemCode(dialyChrgItemCode);
        thCharge.setCodeType(theatreCodeType);
        thCharge.setAllowRateIncrease((Boolean) opn.get("allow_rate_increase"));
        thCharge.setAllowRateDecrease((Boolean) opn.get("allow_rate_decrease"));
        if (!finalizationStatus.equals("N")) {
          thCharge.setActRemarks(DataBaseUtil.timeStampFormatter.format(from) + " to  "
              + DataBaseUtil.timeStampFormatter.format(to));
        }
        thCharge.setFrom_date(from);
        thCharge.setTo_date(to);
        if (null != theatre.get("billing_group_id")) {
          thCharge.setBillingGroupId((Integer) theatre.get("billing_group_id"));
        }
        lis.add(thCharge);

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
        
        BigDecimal hqty;
        if (isSecondary) { 
          hqty = BigDecimal.ZERO;
          discount = BigDecimal.ZERO;
        } else {
          hqty = BigDecimal.ONE;
        }

        ChargeDTO thCharge = new ChargeDTO("OPE", "TCOPE", rate, hqty, discount, "",
            (String) theatre.get("theatre_id"),
            operName + "/" + (String) theatre.get("theatre_name"), null, isInsurance,
            serviceSubGroupId, insuranceCategoryId, visitType, patientId, firstOfCategory);

        // thCharge.setInsuranceAmt(planIds, visitType, thCharge.getFirstOfCategory());
        thCharge.setActItemCode(itemCode);
        thCharge.setActRatePlanItemCode(
            (duration <= minDuration.intValue()) ? minChrgItemCode : slab1ChrgItemCode);
        thCharge.setCodeType(theatreCodeType);
        thCharge.setOp_id(opnId);
        thCharge.setAllowRateIncrease((Boolean) opn.get("allow_rate_increase"));
        thCharge.setAllowRateDecrease((Boolean) opn.get("allow_rate_decrease"));
        if (!finalizationStatus.equals("N")) {
          thCharge.setActRemarks(DataBaseUtil.timeStampFormatter.format(from) + " to  "
              + DataBaseUtil.timeStampFormatter.format(to));
        }
        thCharge.setFrom_date(from);
        thCharge.setTo_date(to);
        if (null != theatre.get("billing_group_id")) {
          thCharge.setBillingGroupId((Integer) theatre.get("billing_group_id"));
        }
        lis.add(thCharge);

      } else {

        BigDecimal minRate = (BigDecimal) theatre.get("min_charge");
        BigDecimal minDiscount = (BigDecimal) theatre.get("min_charge_discount");
        BigDecimal minDuration = (BigDecimal) theatre.get("min_duration");
        BigDecimal slab1Rate = (BigDecimal) theatre.get("slab_1_charge");
        BigDecimal slab1Discount = (BigDecimal) theatre.get("slab_1_charge_discount");
        BigDecimal slab1Duration = (BigDecimal) theatre.get("slab_1_threshold");
        BigDecimal incrRate = (BigDecimal) theatre.get("incr_charge");
        BigDecimal incrDiscount = (BigDecimal) theatre.get("incr_charge_discount");
        String minChrgItemCode = ipprefs.get("theatre_min_charge_code") == null ? ""
            : (String) ipprefs.get("theatre_min_charge_code");
        String slab1ChrgItemCode = ipprefs.get("theatre_slab1_charge_code") == null ? ""
            : (String) ipprefs.get("theatre_slab1_charge_code");
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
        
        BigDecimal hqty;
        if (isSecondary) { 
          hqty = BigDecimal.ZERO;
          discount = BigDecimal.ZERO;
        } else {
          hqty = BigDecimal.ONE;
        }

        ChargeDTO thCharge = new ChargeDTO("OPE", "TCOPE", rate, hqty, discount, "",
            (String) theatre.get("theatre_id"),
            operName + "/" + (String) theatre.get("theatre_name"), null, isInsurance,
            serviceSubGroupId, insuranceCategoryId, visitType, patientId, firstOfCategory);

        // thCharge.setInsuranceAmt(planIds, visitType, thCharge.getFirstOfCategory());
        thCharge.setActItemCode(itemCode);
        thCharge.setActRatePlanItemCode(hrlytItemCode);
        thCharge.setCodeType(theatreCodeType);
        thCharge.setOp_id(opnId);
        thCharge.setAllowRateIncrease((Boolean) opn.get("allow_rate_increase"));
        thCharge.setAllowRateDecrease((Boolean) opn.get("allow_rate_decrease"));
        if (!finalizationStatus.equals("N")) {
          thCharge.setActRemarks(DataBaseUtil.timeStampFormatter.format(from) + " to  "
              + DataBaseUtil.timeStampFormatter.format(to));
        }
        thCharge.setFrom_date(from);
        thCharge.setTo_date(to);
        if (null != theatre.get("billing_group_id")) {
          thCharge.setBillingGroupId((Integer) theatre.get("billing_group_id"));
        }
        lis.add(thCharge);

        BigDecimal incrDuration = (BigDecimal) theatre.get("incr_duration");
        if ((addlnDuration > 0) && incrDuration.intValue() > 0) {

          rate = getDurationCharge(addlnDuration, 0, 0, incrDuration.intValue(),
              BigDecimal.ZERO, slab1Rate, incrRate, true);
          discount = getDurationCharge(addlnDuration, 0, 0, incrDuration.intValue(),
              BigDecimal.ZERO, slab1Discount, incrDiscount, true);

          thCharge = new ChargeDTO("OPE", "TCAOPE", incrRate,
              (incrRate.compareTo(BigDecimal.ZERO) > 0) ? rate.divide(incrRate)
                  : BigDecimal.ZERO,
              discount, "", (String) theatre.get("theatre_id"),
              operName + "/" + (String) theatre.get("theatre_name"), null, isInsurance,
              serviceSubGroupId, insuranceCategoryId, visitType, patientId, firstOfCategory);

          // thCharge.setInsuranceAmt(planIds, visitType, thCharge.getFirstOfCategory());
          thCharge.setActItemCode(itemCode);
          thCharge.setActRatePlanItemCode(incrChrgItemCode);
          thCharge.setCodeType(theatreCodeType);
          thCharge.setOp_id(opnId);
          thCharge.setAllowRateIncrease((Boolean) opn.get("allow_rate_increase"));
          thCharge.setAllowRateDecrease((Boolean) opn.get("allow_rate_decrease"));
          if (!finalizationStatus.equals("N")) {
            thCharge.setActRemarks(DataBaseUtil.timeStampFormatter.format(from) + " to  "
                + DataBaseUtil.timeStampFormatter.format(to));
          }
          thCharge.setFrom_date(from);
          thCharge.setTo_date(to);
          if (null != theatre.get("billing_group_id")) {
            thCharge.setBillingGroupId((Integer) theatre.get("billing_group_id"));
          }
          lis.add(thCharge);
        }
      }
    }

    if (opn == null) {
      return lis;
    }

    /*
     * Surgical Assistance Charge
     */
    BigDecimal sacAmount = (BigDecimal) opn.get("surg_asstance_charge");
    // if (theatre == null) {
    // addSurAssCharge = true;
    // }

    // if (!addSurAssCharge && sacAmount.compareTo(BigDecimal.ZERO) > 0) {
    // addSurAssCharge = true;
    // }

    // if (addSurAssCharge) {
    ChargeDTO sacCharge = new ChargeDTO("OPE", "SACOPE", sacAmount, BigDecimal.ONE,
        (BigDecimal) opn.get("surg_asst_discount"), "", (String) opn.get("op_id"), operName,
        (String) opn.get("dept_id"), isInsurance, serviceSubGroupId, insuranceCategoryId, visitType,
        patientId, firstOfCategory);

    sacCharge.setActItemCode(itemCode);
    sacCharge.setActRatePlanItemCode(ratePlanItemCode);
    sacCharge.setCodeType(codeType);
    sacCharge.setOp_id(opnId);
    sacCharge.setFrom_date(from);
    sacCharge.setTo_date(to);
    sacCharge.setAllowRateIncrease((Boolean) opn.get("allow_rate_increase"));
    sacCharge.setAllowRateDecrease((Boolean) opn.get("allow_rate_decrease"));
    if (null != opn.get("billing_group_id")) {
      sacCharge.setBillingGroupId((Integer) opn.get("billing_group_id"));
    }
    lis.add(sacCharge);
    // }

    /*
     * Surgeon Charge = surgeon charge from operation + doctor's OT charge
     */

    List<ChargeDTO> surgeonCharges = new ArrayList<ChargeDTO>();
    if (surgeonDoc != null) {
      surgeonCharges = getSurgeonCharges(opnId, opn, from, to, units, isInsurance, planIds,
          finalizationStatus, visitType, surgeonDoc, patientId, firstOfCategory,
          ratePlanItemCode, codeType);
      lis.addAll(surgeonCharges);
    }

    /*
     * Anaesthetist Charge = anaesthetist charge from operation + doctor's OT Charge
     */

    if (anaDoc != null) {
      List<ChargeDTO> anaestiatistCharges = new ArrayList<ChargeDTO>();
      anaestiatistCharges = getAnestiatistCharges(opnId, opn, from, to, units, isInsurance,
          planIds, finalizationStatus, visitType, anaDoc, patientId, firstOfCategory,
          ratePlanItemCode, codeType);
      lis.addAll(anaestiatistCharges);
    }

    /*
     * Aneaesthesia charges
     */

    if (anasthesiaTypeBean != null) {
      List<ChargeDTO> anaesthesiaTypeCharge = new ArrayList<ChargeDTO>();
      anaesthesiaTypeCharge =
          getAnaesthesiaTypeCharges(opnId, opn, from, to, units, isInsurance, planIds,
              finalizationStatus, visitType, anasthesiaTypeBean, patientId, firstOfCategory);
      lis.addAll(anaesthesiaTypeCharge);
    }

    return lis;
  }

  /**
   * Gets the anaesthesia type charges.
   *
   * @param opnId              the opn id
   * @param opn                the opn
   * @param from               the from
   * @param to                 the to
   * @param units              the units
   * @param isInsurance        the is insurance
   * @param planIds            the plan ids
   * @param finalizationStatus the finalization status
   * @param visitType          the visit type
   * @param anasthesiaTypeBean the anasthesia type bean
   * @param patientId          the patient id
   * @param firstOfCategory    the first of category
   * @return the anaesthesia type charges
   * @throws SQLException the SQL exception
   */
  public static List<ChargeDTO> getAnaesthesiaTypeCharges(String opnId, BasicDynaBean opn,
      Timestamp from, Timestamp to, String units, Boolean isInsurance, int[] planIds,
      String finalizationStatus, String visitType, BasicDynaBean anasthesiaTypeBean,
      String patientId, Boolean firstOfCategory) throws SQLException {

    BigDecimal rate = BigDecimal.ZERO;
    BigDecimal discount = BigDecimal.ZERO;
    String itemCode = null;
    String operName = null;
    List<ChargeDTO> lis = new ArrayList<ChargeDTO>();

    int serviceSubGroupId = (Integer) opn.get("service_sub_group_id");

    if (opn != null) {
      itemCode = (String) opn.get("operation_code");
      operName = (String) opn.get("operation_name");
    }

    int insuranceCategoryId = getIntegerValueFromBean(opn, "insurance_category_id");

    if (anasthesiaTypeBean != null) {
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
        discount = getBaseCharge(totalUnit, incrDiscount);
      } else {
        totalUnit = 1;
        rate = getDurationCharge(duration, minDuration.intValue(), slab1Duration.intValue(),
            incrDuration.intValue(), minRate, slab1Rate, incrRate, false);
        discount =
            getDurationCharge(duration, minDuration.intValue(), slab1Duration.intValue(),
                incrDuration.intValue(), minDiscount, slab1Discount, incrDiscount, false);
      }

      ChargeDTO anaetypeCharge =
          new ChargeDTO("OPE", "ANATOPE", rate, new BigDecimal(totalUnit), discount, "",
              (String) anasthesiaTypeBean.get("anesthesia_type_id"),
              operName + "/" + (String) anasthesiaTypeBean.get("anesthesia_type_name"), null,
              isInsurance, serviceSubGroupId, insuranceCategoryId, visitType, patientId,
              firstOfCategory);

      // anaetypeCharge.setInsuranceAmt(planIds, visitType,
      // anaetypeCharge.getFirstOfCategory());
      anaetypeCharge.setFrom_date(from);
      anaetypeCharge.setTo_date(to);

      anaetypeCharge.setActItemCode(itemCode);
      // anesthesia has its own item code and code type, not using the operations.
      anaetypeCharge.setActRatePlanItemCode((String) anasthesiaTypeBean.get("item_code"));
      anaetypeCharge.setCodeType((String) anasthesiaTypeBean.get("code_type"));
      anaetypeCharge.setAllowRateIncrease((Boolean) opn.get("allow_rate_increase"));
      anaetypeCharge.setAllowRateDecrease((Boolean) opn.get("allow_rate_decrease"));
      if (!finalizationStatus.equals("N")) {
        anaetypeCharge.setActRemarks(DataBaseUtil.timeStampFormatter.format(from) + " to "
            + DataBaseUtil.timeStampFormatter.format(to));
      }
      anaetypeCharge.setOp_id(opnId);
      if (null != anasthesiaTypeBean.get("billing_group_id")) {
        anaetypeCharge.setBillingGroupId((Integer) anasthesiaTypeBean.get("billing_group_id"));
      }
      lis.add(anaetypeCharge);
    }
    return lis;
  }

  /**
   * Gets the surgeon charges.
   *
   * @param opnId               the opn id
   * @param opn                 the opn
   * @param from                the from
   * @param to                  the to
   * @param units               the units
   * @param isInsurance         the is insurance
   * @param planIds             the plan ids
   * @param finalizationStatus  the finalization status
   * @param visitType           the visit type
   * @param surgeonDoc          the surgeon doc
   * @param patientId           the patient id
   * @param firstOfCategory     the first of category
   * @param actRatePlanItemCode the act rate plan item code
   * @param codeType            the code type
   * @return the surgeon charges
   * @throws SQLException the SQL exception
   */
  public static List<ChargeDTO> getSurgeonCharges(String opnId, BasicDynaBean opn,
      Timestamp from, Timestamp to, String units, Boolean isInsurance, int[] planIds,
      String finalizationStatus, String visitType, BasicDynaBean surgeonDoc, String patientId,
      Boolean firstOfCategory, String actRatePlanItemCode, String codeType)
      throws SQLException {

    BigDecimal rate = BigDecimal.ZERO;
    String operName = "";

    List<ChargeDTO> lis = new ArrayList<ChargeDTO>();

    int insuranceCategoryId = getIntegerValueFromBean(opn, "insurance_category_id");

    if (opn != null) {
      operName = (String) opn.get("operation_name");
    }

    if (surgeonDoc != null) {
      rate = (BigDecimal) opn.get("surgeon_charge");
      rate = rate.add((BigDecimal) surgeonDoc.get("charge"));

      BigDecimal discount = (BigDecimal) opn.get("surg_discount");
      discount = discount.add((BigDecimal) surgeonDoc.get("discount"));

      ChargeDTO surgeonCharge = new ChargeDTO("OPE", "SUOPE", rate, BigDecimal.ONE, discount, "",
          (String) surgeonDoc.get("doctor_id"),
          operName + "/" + (String) surgeonDoc.get("doctor_name"),
          (String) surgeonDoc.get("dept_id"), isInsurance, -1, insuranceCategoryId, visitType,
          patientId, firstOfCategory);

      // set the conducting doctor ID same as the surgeon id.
      // surgeonCharge.setInsuranceAmt(planIds, visitType,
      // surgeonCharge.getFirstOfCategory());
      surgeonCharge.setOp_id(opnId);
      surgeonCharge.setFrom_date(from);
      surgeonCharge.setTo_date(to);
      surgeonCharge.setPayeeDoctorId((String) surgeonDoc.get("doctor_id"));
      surgeonCharge.setAllowRateIncrease((Boolean) opn.get("allow_rate_increase"));
      surgeonCharge.setAllowRateDecrease((Boolean) opn.get("allow_rate_decrease"));
      surgeonCharge.setActRatePlanItemCode(actRatePlanItemCode);
      surgeonCharge.setCodeType(codeType);
      if (null != opn.get("billing_group_id")) {
        surgeonCharge.setBillingGroupId((Integer) opn.get("billing_group_id"));
      }
      lis.add(surgeonCharge);
    }
    return lis;
  }

  /**
   * Gets the anestiatist charges.
   *
   * @param opnId              the opn id
   * @param opn                the opn
   * @param from               the from
   * @param to                 the to
   * @param units              the units
   * @param isInsurance        the is insurance
   * @param planIds            the plan ids
   * @param finalizationStatus the finalization status
   * @param visitType          the visit type
   * @param anaDoc             the ana doc
   * @param patientId          the patient id
   * @param firstOfCategory    the first of category
   * @param ratePlanItemCode   the rate plan item code
   * @param codeType           the code type
   * @return the anestiatist charges
   * @throws SQLException the SQL exception
   */
  public static List<ChargeDTO> getAnestiatistCharges(String opnId, BasicDynaBean opn,
      Timestamp from, Timestamp to, String units, Boolean isInsurance, int[] planIds,
      String finalizationStatus, String visitType, BasicDynaBean anaDoc, String patientId,
      Boolean firstOfCategory, String ratePlanItemCode, String codeType) throws SQLException {

    BigDecimal rate = BigDecimal.ZERO;
    String operName = "";

    List<ChargeDTO> lis = new ArrayList<ChargeDTO>();

    int insuranceCategoryId = getIntegerValueFromBean(opn, "insurance_category_id");

    if (opn != null) {
      operName = (String) opn.get("operation_name");
    }

    if (anaDoc != null) {
      rate = (BigDecimal) opn.get("anesthetist_charge");
      rate = rate.add((BigDecimal) anaDoc.get("charge"));
      if (rate.compareTo(BigDecimal.ZERO) > 0) {
        BigDecimal discount = (BigDecimal) opn.get("anest_discount");
        discount = discount.add((BigDecimal) anaDoc.get("discount"));

        ChargeDTO anaCharge = new ChargeDTO("OPE", "ANAOPE", rate, BigDecimal.ONE, discount,
            "", (String) anaDoc.get("doctor_id"),
            operName + "/" + (String) anaDoc.get("doctor_name"),
            (String) anaDoc.get("dept_id"), isInsurance, -1, insuranceCategoryId, visitType,
            patientId, firstOfCategory);
        // anaCharge.setInsuranceAmt(planIds, visitType,
        // anaCharge.getFirstOfCategory());
        anaCharge.setFrom_date(from);
        anaCharge.setTo_date(to);
        anaCharge.setPayeeDoctorId((String) anaDoc.get("doctor_id"));
        anaCharge.setOp_id(opnId);
        anaCharge.setAllowRateIncrease((Boolean) opn.get("allow_rate_increase"));
        anaCharge.setAllowRateDecrease((Boolean) opn.get("allow_rate_decrease"));
        anaCharge.setActRatePlanItemCode(ratePlanItemCode);
        anaCharge.setCodeType(codeType);
        if (null != opn.get("billing_group_id")) {
          anaCharge.setBillingGroupId((Integer) opn.get("billing_group_id"));
        }
        lis.add(anaCharge);
      }
    }

    return lis;
  }

  /**
   * Gets the equipment charges.
   *
   * @param equip           the equip
   * @param from            the from
   * @param to              the to
   * @param units           the units
   * @param isOperation     the is operation
   * @param quantity        the quantity
   * @param isInsurance     the is insurance
   * @param planIds         the plan ids
   * @param visitType       the visit type
   * @param patientId       the patient id
   * @param firstOfCategory the first of category
   * @return the equipment charges
   * @throws SQLException the SQL exception
   */
  public static List<ChargeDTO> getEquipmentCharges(BasicDynaBean equip, Timestamp from,
      Timestamp to, String units, boolean isOperation, BigDecimal quantity,
      Boolean isInsurance, int[] planIds, String visitType, String patientId,
      Boolean firstOfCategory) throws SQLException {

    BigDecimal rate = null;
    BigDecimal discount = null;
    int qty = 1;
    int duration = 0;
    String unitsStr = "";
    int serviceSubGroupId = (Integer) equip.get("service_sub_group_id");
    if (units == null || units.equals("")) {
      units = "H";
    }

    if ((units.equals("D") || units.equals("Days"))) {
      rate = (BigDecimal) equip.get("charge");
      discount = (BigDecimal) equip.get("daily_charge_discount");
      if (from == null) {
        qty = quantity.intValue(); // equipment supports only integer quantities
      } else {
        qty = getDuration(from, to, "D");
      }
      unitsStr = "Days";

    } else {
      /*
       * rate*qty-discount = amt must be maintained. So, we calculate the total charge as per
       * min/incr and put the rate as the total charge, set the qty=1. Note that we should not
       * display units as Hrs because it is not 1 hrs. The trade off is between maintaining
       * rate*qty-discount=amt vs. showing the correct amount of Hrs in the display. We choose the
       * former.
       */
      qty = 1;
      BigDecimal minRate = (BigDecimal) equip.get("min_charge");
      BigDecimal minDiscount = (BigDecimal) equip.get("min_charge_discount");
      BigDecimal slab1Rate = (BigDecimal) equip.get("slab_1_charge");
      BigDecimal slab1Discount = (BigDecimal) equip.get("slab_1_charge_discount");
      BigDecimal incrRate = (BigDecimal) equip.get("incr_charge");
      BigDecimal incrDiscount = (BigDecimal) equip.get("incr_charge_discount");

      int minDuration = ((BigDecimal) equip.get("min_duration")).intValue();
      int slab1Duration = ((BigDecimal) equip.get("slab_1_threshold")).intValue();
      int incrDuration = ((BigDecimal) equip.get("incr_duration")).intValue();

      if (from == null) {
        duration = quantity.intValue(); // equipment supports only integer quantities
        unitsStr = "Hrs";
      } else {
        duration = getDuration(from, to, "H", (Integer) equip.get("duration_unit_minutes"));
        unitsStr = "";
      }

      rate = getDurationCharge(duration, minDuration, slab1Duration, incrDuration, minRate,
          slab1Rate, incrRate, false);
      discount = getDurationCharge(duration, minDuration, slab1Duration, incrDuration,
          minDiscount, slab1Discount, incrDiscount, false);
    }

    int insuranceCategoryId = getIntegerValueFromBean(equip, "insurance_category_id");
    ChargeDTO chrgdto = new ChargeDTO(isOperation ? "OPE" : "OTC",
        isOperation ? "EQOPE" : "EQUOTC", rate, new BigDecimal(qty),
        discount.multiply(new BigDecimal(qty)), unitsStr, (String) equip.get("equip_id"),
        (String) equip.get("equipment_name"), (String) equip.get("dept_id"), isInsurance,
        serviceSubGroupId, insuranceCategoryId, visitType, patientId, firstOfCategory);

    // chrgdto.setInsuranceAmt(planIds, visitType, chrgdto.getFirstOfCategory());
    chrgdto.setActItemCode((String) equip.get("equipment_code"));
    chrgdto.setAllowRateIncrease((Boolean) equip.get("allow_rate_increase"));
    chrgdto.setAllowRateDecrease((Boolean) equip.get("allow_rate_decrease"));
    // no rate plan code for equipment

    chrgdto.setFrom_date(from);
    chrgdto.setTo_date(to);
    if (null != equip.get("billing_group_id")) {
      chrgdto.setBillingGroupId((Integer) equip.get("billing_group_id"));
    }
    List lis = new ArrayList();
    lis.add(chrgdto);

    BigDecimal taxPer = (BigDecimal) equip.get("tax");

    if (taxPer.compareTo(BigDecimal.ZERO) > 0) {
      BigDecimal taxAmount = rate.multiply(new BigDecimal(qty)).subtract(discount)
          .multiply(taxPer).divide(new BigDecimal(100), 2);

      chrgdto = new ChargeDTO("TAX", "STAX", taxAmount, BigDecimal.ONE, BigDecimal.ZERO, "",
          null, "Service Tax (" + (String) equip.get("equipment_name") + ")",
          (String) equip.get("dept_id"), isInsurance, serviceSubGroupId, insuranceCategoryId,
          visitType, patientId, firstOfCategory);
      chrgdto.setInsuranceAmt(planIds, visitType, chrgdto.getFirstOfCategory());
      chrgdto.setFrom_date(from);
      chrgdto.setTo_date(to);
      chrgdto.setAllowRateIncrease((Boolean) equip.get("allow_rate_increase"));
      chrgdto.setAllowRateDecrease((Boolean) equip.get("allow_rate_decrease"));
      if (null != equip.get("billing_group_id")) {
        chrgdto.setBillingGroupId((Integer) equip.get("billing_group_id"));
      }
      lis.add(chrgdto);
    }

    return lis;
  }

  /**
   * Gets the service charges.
   *
   * @param service         the service
   * @param quantity        the quantity
   * @param isInsurance     the is insurance
   * @param planIds         the plan ids
   * @param visitType       the visit type
   * @param patientId       the patient id
   * @param firstOfCategory the first of category
   * @param condDoctorId    the cond doctor id
   * @return the service charges
   * @throws SQLException the SQL exception
   */
  public static List<ChargeDTO> getServiceCharges(BasicDynaBean service, BigDecimal quantity,
      Boolean isInsurance, int[] planIds, String visitType, String patientId,
      Boolean firstOfCategory, String condDoctorId) throws SQLException {

    String serviceName = (String) service.get("service_name");

    BigDecimal unitCharge = (BigDecimal) service.get("unit_charge");
    int insuranceCategoryId = getIntegerValueFromBean(service, "insurance_category_id");
    ChargeDTO serviceCharge = new ChargeDTO("SNP", "SERSNP", unitCharge, quantity,
        ((BigDecimal) service.get("discount")).multiply(quantity), "",
        (String) service.get("service_id"), serviceName,
        service.get("serv_dept_id").toString(), isInsurance,
        (Integer) service.get("service_sub_group_id"), insuranceCategoryId, visitType,
        patientId, firstOfCategory);

    // serviceCharge.setInsuranceAmt(planIds, visitType,
    // serviceCharge.getFirstOfCategory());
    serviceCharge.setActItemCode((String) service.get("service_code"));
    serviceCharge.setActRatePlanItemCode((String) service.get("item_code"));
    serviceCharge.setCodeType((String) service.get("code_type"));
    serviceCharge
        .setConducting_doc_mandatory((String) service.get("conducting_doc_mandatory"));
    serviceCharge.setConduction_required((Boolean) service.get("conduction_applicable"));
    serviceCharge.setPayeeDoctorId(condDoctorId);
    serviceCharge.setAllowRateIncrease((Boolean) service.get("allow_rate_increase"));
    serviceCharge.setAllowRateDecrease((Boolean) service.get("allow_rate_decrease"));
    if (condDoctorId != null) {
      serviceCharge.setActivityConducted("Y");
    }
    if (service.get("billing_group_id") != null) {
      serviceCharge.setBillingGroupId((Integer) service.get("billing_group_id"));
    }
    List lis = new ArrayList();
    lis.add(serviceCharge);

    BigDecimal taxPer = (BigDecimal) service.get("service_tax");
    if (taxPer != null && taxPer.compareTo(BigDecimal.ZERO) != 0) {
      BigDecimal taxAmount =
          taxPer.multiply(unitCharge).multiply(quantity).divide(new BigDecimal("100"), 2);
      ChargeDTO taxCharge =
          new ChargeDTO("TAX", "STAX", taxAmount, BigDecimal.ONE, BigDecimal.ZERO, "", null,
              "Service Tax (" + serviceName + ")", service.get("serv_dept_id").toString(),
              isInsurance, (Integer) service.get("service_sub_group_id"), insuranceCategoryId,
              visitType, patientId, firstOfCategory);
      taxCharge.setAllowRateIncrease((Boolean) service.get("allow_rate_increase"));
      taxCharge.setAllowRateDecrease((Boolean) service.get("allow_rate_decrease"));
      if (service.get("billing_group_id") != null) {
        taxCharge.setBillingGroupId((Integer) service.get("billing_group_id"));
      }
      lis.add(taxCharge);
    }

    return lis;
  }

  /**
   * Gets the test charges.
   *
   * @param test            the test
   * @param quantity        the quantity
   * @param isInsurance     the is insurance
   * @param planIds         the plan ids
   * @param visitType       the visit type
   * @param patientId       the patient id
   * @param firstOfCategory the first of category
   * @param condDoctorId    the cond doctor id
   * @return the test charges
   * @throws SQLException the SQL exception
   */
  public static List<ChargeDTO> getTestCharges(BasicDynaBean test, BigDecimal quantity,
      boolean isInsurance, int[] planIds, String visitType, String patientId,
      Boolean firstOfCategory, String condDoctorId) throws SQLException {

    String testCategory = (String) test.get("category");
    String testName = (String) test.get("test_name");

    String head =
        testCategory.equals("DEP_LAB") ? ChargeDTO.CH_DIAG_LAB : ChargeDTO.CH_DIAG_RAD;
    int insuranceCategoryId = getIntegerValueFromBean(test, "insurance_category_id");

    ChargeDTO chrgdto = new ChargeDTO("DIA", head, (BigDecimal) test.get("charge"), quantity,
        ((BigDecimal) test.get("discount")).multiply(quantity), "",
        (String) test.get("test_id"), testName, (String) test.get("ddept_id"), isInsurance,
        (Integer) test.get("service_sub_group_id"), insuranceCategoryId, visitType, patientId,
        firstOfCategory);
    // chrgdto.setInsuranceAmt(planIds, visitType, chrgdto.getFirstOfCategory());
    chrgdto.setActItemCode((String) test.get("diag_code"));
    chrgdto.setActRatePlanItemCode((String) test.get("rate_plan_code"));
    chrgdto.setCodeType((String) test.get("code_type"));
    chrgdto.setConducting_doc_mandatory((String) test.get("conducting_doc_mandatory"));
    chrgdto.setConduction_required((Boolean) test.get("conduction_applicable"));
    chrgdto.setPayeeDoctorId(condDoctorId);
    chrgdto.setAllowRateIncrease((Boolean) test.get("allow_rate_increase"));
    chrgdto.setAllowRateDecrease((Boolean) test.get("allow_rate_decrease"));
    if (condDoctorId != null) {
      chrgdto.setActivityConducted("Y");
    }
    if (test.get("billing_group_id") != null) {
      chrgdto.setBillingGroupId((Integer) test.get("billing_group_id"));
    }

    List lis = new ArrayList();
    lis.add(chrgdto);
    return lis;
  }

  /**
   * Gets the multi visit pack item charges.
   *
   * @param packageId       the package id
   * @param packObId        the pack ob id
   * @param bedType         the bed type
   * @param orgId           the org id
   * @param id              the id
   * @param quantity        the quantity
   * @param isInsurance     the is insurance
   * @param visitType       the visit type
   * @param patientId       the patient id
   * @param firstOfCategory the first of category
   * @return the multi visit pack item charges
   * @throws SQLException the SQL exception
   */
  public static List<ChargeDTO> getMultiVisitPackItemCharges(String packageId, String packObId,
      String bedType, String orgId, String id, BigDecimal quantity, boolean isInsurance,
      String visitType, String patientId, boolean firstOfCategory) throws SQLException {
    Map<String, Object> identifiers = new HashMap<String, Object>();
    identifiers.put("package_content_id", Integer.parseInt(packObId));
    identifiers.put("org_id", orgId);
    identifiers.put("bed_type", bedType);
    ChargeDTO chrgdto = null;
    List<BasicDynaBean> packageComponentDetails =
        PackageDAO.getPackageComponents(Integer.parseInt(packageId));
    BasicDynaBean itemChargeBean =
        new GenericDAO("package_content_charges").findByKey(identifiers);
    if (packageComponentDetails != null && packageComponentDetails.size() > 0) {
      for (int i = 0; i < packageComponentDetails.size(); i++) {
        BasicDynaBean packCompDetailsBean = packageComponentDetails.get(i);
        String packObIdStr = (String) packCompDetailsBean.get("pack_ob_id").toString();
        if (packObIdStr.equals(packObId) && itemChargeBean != null) {
          String chargeGroup = null;
          String itemType = (String) packCompDetailsBean.get("item_type");
          if (itemType.equals("Doctor")) {
            chargeGroup = "DOC";
          } else if (itemType.equals("Laboratory") || itemType.equals("Radiology")) {
            chargeGroup = "DIA";
          } else if (itemType.equals("Service")) {
            chargeGroup = "SNP";
          } else if (itemType.equals("Other Charge")) {
            chargeGroup = "OTC";
          }

          BigDecimal charge = (BigDecimal) itemChargeBean.get("charge");
          Integer totalItemQty = (Integer) packCompDetailsBean.get("activity_qty");
          if (charge.compareTo(BigDecimal.ZERO) != 0) {
            charge = charge.divide(new BigDecimal(totalItemQty), RoundingMode.FLOOR);
          }

          chrgdto = new ChargeDTO(chargeGroup, (String) packCompDetailsBean.get("charge_head"),
              charge, quantity, BigDecimal.ZERO, "",
              (String) packCompDetailsBean.get("activity_id"),
              (String) packCompDetailsBean.get("activity_description"), null, isInsurance,
              (Integer) packCompDetailsBean.get("service_sub_group_id"),
              (Integer) packCompDetailsBean.get("insurance_category_id"), visitType, patientId,
              firstOfCategory);
          if (null != packCompDetailsBean.get("billing_group_id")) {
            chrgdto.setBillingGroupId((Integer) packCompDetailsBean.get("billing_group_id"));
          }
          if (null != packCompDetailsBean.get("consultation_type_id")) {
            chrgdto.setConsultation_type_id(
                (Integer) packCompDetailsBean.get("consultation_type_id"));
          }
        }
      }
    }
    List lis = new ArrayList();
    lis.add(chrgdto);
    return lis;
  }

  /**
   * Gets the other charges.
   *
   * @param oc              the oc
   * @param quantity        the quantity
   * @param isInsurance     the is insurance
   * @param planIds         the plan ids
   * @param visitType       the visit type
   * @param patientId       the patient id
   * @param firstOfCategory the first of category
   * @return the other charges
   * @throws SQLException the SQL exception
   */
  public static List<ChargeDTO> getOtherCharges(BasicDynaBean oc, BigDecimal quantity,
      Boolean isInsurance, int[] planIds, String visitType, String patientId,
      Boolean firstOfCategory) throws SQLException {
    int insuranceCategoryId = getIntegerValueFromBean(oc, "insurance_category_id");
    ChargeDTO chrgdto = new ChargeDTO((String) oc.get("charge_group"),
        (String) oc.get("charge_type"), (BigDecimal) oc.get("charge"), quantity,
        BigDecimal.ZERO, "", (String) oc.get("charge_name"), (String) oc.get("charge_name"),
        null, isInsurance, (Integer) oc.get("service_sub_group_id"), insuranceCategoryId,
        visitType, patientId, firstOfCategory);
    chrgdto.setAllowRateIncrease((Boolean) oc.get("allow_rate_increase"));
    chrgdto.setAllowRateDecrease((Boolean) oc.get("allow_rate_decrease"));
    // chrgdto.setInsuranceAmt(planIds, visitType, chrgdto.getFirstOfCategory());
    if (oc.get("billing_group_id") != null) {
      chrgdto.setBillingGroupId((Integer) oc.get("billing_group_id"));
    }
    List lis = new ArrayList();
    lis.add(chrgdto);
    return lis;
  }

  /**
   * Gets the meal charges.
   *
   * @param meal            the meal
   * @param quantity        the quantity
   * @param isInsurance     the is insurance
   * @param planIds         the plan ids
   * @param visitType       the visit type
   * @param patientId       the patient id
   * @param firstOfCategory the first of category
   * @return the meal charges
   * @throws SQLException the SQL exception
   */
  public static List<ChargeDTO> getMealCharges(BasicDynaBean meal, BigDecimal quantity,
      Boolean isInsurance, int[] planIds, String visitType, String patientId,
      Boolean firstOfCategory) throws SQLException {

    int insuranceCategoryId = getIntegerValueFromBean(meal, "insurance_category_id");
    BasicDynaBean chargeHeadBean = chargeHeadsDAO.findByKey("chargehead_id", "MDIE");
    ChargeDTO chrgdto = new ChargeDTO("DIE", "MDIE", (BigDecimal) meal.get("charge"), quantity,
        ((BigDecimal) meal.get("discount")).multiply(quantity), "",
        meal.get("diet_id").toString(), (String) meal.get("meal_name"), null, isInsurance,
        (Integer) meal.get("service_sub_group_id"), insuranceCategoryId, visitType, patientId,
        firstOfCategory);
    chrgdto.setAllowRateIncrease((Boolean) chargeHeadBean.get("allow_rate_increase"));
    chrgdto.setAllowRateDecrease((Boolean) chargeHeadBean.get("allow_rate_decrease"));
    // chrgdto.setInsuranceAmt(planIds, visitType, chrgdto.getFirstOfCategory());
    if (meal.get("billing_group_id") != null) {
      chrgdto.setBillingGroupId((Integer) meal.get("billing_group_id"));
    }
    List lis = new ArrayList();
    lis.add(chrgdto);

    BigDecimal taxPer = (BigDecimal) meal.get("service_tax");
    if (taxPer != null && taxPer.compareTo(BigDecimal.ZERO) != 0) {
      BigDecimal mealCharge = (BigDecimal) meal.get("charge");
      BigDecimal taxAmount =
          taxPer.multiply(mealCharge).multiply(quantity).divide(new BigDecimal("100"), 2);
      ChargeDTO taxCharge = new ChargeDTO("TAX", "STAX", taxAmount, BigDecimal.ONE,
          BigDecimal.ZERO, "", null, "Service Tax (" + (String) meal.get("meal_name") + ")",
          null, isInsurance, (Integer) meal.get("service_sub_group_id"), insuranceCategoryId,
          visitType, patientId, firstOfCategory);
      // taxCharge.setInsuranceAmt(planIds, visitType, chrgdto.getFirstOfCategory());
      if (meal.get("billing_group_id") != null) {
        taxCharge.setBillingGroupId((Integer) meal.get("billing_group_id"));
      }
      lis.add(taxCharge);
    }
    return lis;
  }

  /**
   * Gets the bed charges.
   *
   * @param cg              the cg
   * @param bc              the bc
   * @param from            the from
   * @param to              the to
   * @param quantity        the quantity
   * @param isBystander     the is bystander
   * @param daycareStatus   the daycare status
   * @param onlyMainCharges the only main charges
   * @param isFirstBed      the is first bed
   * @param bedState        the bed state
   * @param firstOfCategory the first of category
   * @return the bed charges
   * @throws SQLException   the SQL exception
   * @throws ParseException the parse exception
   */
  public List<ChargeDTO> getBedCharges(String cg, BasicDynaBean bc, Timestamp from,
      Timestamp to, BigDecimal quantity, boolean isBystander, String daycareStatus,
      boolean onlyMainCharges, boolean isFirstBed, String bedState, Boolean firstOfCategory)
      throws SQLException, ParseException {
    return getBedCharges(cg, bc, from, to, quantity, isBystander, daycareStatus,
        onlyMainCharges, this.isInsurance, this.planIds, this.visitType, this.patientId,
        isFirstBed, bedState, firstOfCategory, null);
  }

  /**
   * Gets the bed charges.
   *
   * @param cg              the cg
   * @param bc              the bc
   * @param from            the from
   * @param to              the to
   * @param quantity        the quantity
   * @param isBystander     the is bystander
   * @param daycareStatus   the daycare status
   * @param onlyMainCharges the only main charges
   * @param isInsurance     the is insurance
   * @param planIds         the plan ids
   * @param visitType       the visit type
   * @param patientId       the patient id
   * @param isFirstBed      the is first bed
   * @param bedState        the bed state
   * @param firstOfCategory the first of category
   * @param retBedDaysHours the ret bed days hours
   * @return the bed charges
   * @throws SQLException   the SQL exception
   * @throws ParseException the parse exception
   */
  public static List<ChargeDTO> getBedCharges(String cg, BasicDynaBean bc, Timestamp from,
      Timestamp to, BigDecimal quantity, boolean isBystander, String daycareStatus,
      boolean onlyMainCharges, boolean isInsurance, int[] planIds, String visitType,
      String patientId, boolean isFirstBed, String bedState, Boolean firstOfCategory,
      BigDecimal[] retBedDaysHours) throws SQLException, ParseException {
    return getBedCharges(cg, bc, from, to, quantity, isBystander, daycareStatus,
        onlyMainCharges, isInsurance, planIds, visitType, patientId, isFirstBed, bedState,
        firstOfCategory, retBedDaysHours, 0, null, null);
  }

  /**
   * Gets the bed charges.
   *
   * @param cg              the cg
   * @param bc              the bc
   * @param from            the from
   * @param to              the to
   * @param quantity        the quantity
   * @param isBystander     the is bystander
   * @param daycareStatus   the daycare status
   * @param onlyMainCharges the only main charges
   * @param isInsurance     the is insurance
   * @param planIds         the plan ids
   * @param visitType       the visit type
   * @param patientId       the patient id
   * @param isFirstBed      the is first bed
   * @param bedState        the bed state
   * @param firstOfCategory the first of category
   * @param retBedDaysHours the ret bed days hours
   * @param discCatId       the disc cat id
   * @param billNo          the bill no
   * @param con             the con
   * @return the bed charges
   * @throws SQLException   the SQL exception
   * @throws ParseException the parse exception
   */
  public static List<ChargeDTO> getBedCharges(String cg, BasicDynaBean bc, Timestamp from,
      Timestamp to, BigDecimal quantity, boolean isBystander, String daycareStatus,
      boolean onlyMainCharges, boolean isInsurance, int[] planIds, String visitType,
      String patientId, boolean isFirstBed, String bedState, Boolean firstOfCategory,
      BigDecimal[] retBedDaysHours, Object discCatId, Object billNo, Connection con)
      throws SQLException, ParseException {

    String bedType = (String) (cg.equals("ICU") ? bc.get("intensive_bed_type")
        : bc.get("bed_type"));
    BasicDynaBean bedTypeBean = bedMasterDao.getBedType(bedType);
    BasicDynaBean prefs = new IPPreferencesDAO().getPreferences();

    BigDecimal[] bedDaysHours = getBedDaysHours(from, to, quantity, daycareStatus, isFirstBed,
        (String) bedTypeBean.get("is_icu"), prefs);

    DiscountPlanBO discBO = new DiscountPlanBO();
    // say some details abt discount plan
    discBO.setDiscountPlanDetails(discCatId != null ? (Integer) discCatId : 0);

    BigDecimal bedDays = bedDaysHours[0];
    BigDecimal bedHours = bedDaysHours[1];
    if (retBedDaysHours != null) {
      retBedDaysHours[0] = bedDays;
      retBedDaysHours[1] = bedHours;
    }

    List<ChargeDTO> lis = new ArrayList<ChargeDTO>();
    BigDecimal bedAmount = BigDecimal.ZERO;
    BigDecimal totalAmount = BigDecimal.ZERO;

    BasicDynaBean mainChargeHeadBean =
        isBystander ? chargeHeadsDAO.findByKey("chargehead_id", "BYBED")
            : chargeHeadsDAO.findByKey("chargehead_id", "B" + cg);
    int mainServiceSubGroup = (Integer) mainChargeHeadBean.get("service_sub_group_id");

    int insuranceCategoryId = getIntegerValueFromBean(bedTypeBean, "insurance_category_id");

    /*
     * Daily bed charge: One daily charge OR one hourly charge is added
     */
    ChargeDTO mainCharge = null;

    if (isBystander && bedDays.compareTo(BigDecimal.ZERO) > 0) {
      BigDecimal bystanderBedCharges = BigDecimal.ZERO;
      BigDecimal bystanderBedDiscount = BigDecimal.ZERO;
      if (prefs.get("bystander_bed_charges_applicable_on").equals("W")) {
        bystanderBedCharges = ((BigDecimal) bc.get("bed_charge"))
            .add((BigDecimal) bc.get("nursing_charge")).add((BigDecimal) bc.get("duty_charge"))
            .add((BigDecimal) bc.get("maintainance_charge"));

        bystanderBedDiscount = ((BigDecimal) bc.get("bed_charge_discount"))
            .add((BigDecimal) bc.get("nursing_charge_discount"))
            .add((BigDecimal) bc.get("duty_charge_discount"))
            .add((BigDecimal) bc.get("maintainance_charge_discount"));
      } else {
        bystanderBedCharges = (BigDecimal) bc.get("bed_charge");
        bystanderBedDiscount = (BigDecimal) bc.get("bed_charge_discount");
      }

      mainCharge = new ChargeDTO(cg, "BYBED", bystanderBedCharges, bedDays,
          bystanderBedDiscount.multiply(bedDays), "Days", bedType, bedType, null, isInsurance,
          mainServiceSubGroup, insuranceCategoryId, visitType, patientId, firstOfCategory);
      mainCharge.setAllowRateIncrease(true);
      mainCharge.setAllowRateDecrease(true);
      mainCharge.setBillNo((String) billNo);
      if (null != con) {
        discBO.applyDiscountRule(con, mainCharge);
      } else {
        discBO.applyDiscountRule(mainCharge);
      }

    } else if (daycareStatus.equals("Y")) {
      // day care: special handling based on slabs and incr rate
      int duration = bedHours.intValue();
      int minDuration = (Integer) prefs.get("daycare_min_duration");
      int slab1Duration = (Integer) prefs.get("daycare_slab_1_threshold");
      int slab2Duration = (Integer) prefs.get("daycare_slab_2_threshold");
      int incrDuration = 1;

      if (minDuration == 0 && slab1Duration == 0 && slab2Duration == 0) {
        // only hourly charges: now we can have rate*hours = amount, qty is Hours.
        BigDecimal rate = (BigDecimal) bc.get("hourly_charge");
        BigDecimal discount = (BigDecimal) bc.get("hourly_charge_discount");
        mainCharge = new ChargeDTO(cg, "B" + cg, rate, bedHours, discount.multiply(bedHours),
            "Hrs", bedType, bedType, null, isInsurance, mainServiceSubGroup,
            insuranceCategoryId, visitType, patientId, firstOfCategory);
        mainCharge.setAllowRateIncrease(true);
        mainCharge.setAllowRateDecrease(true);

      } else {
        // rate is slab determined, quantity has to be 1
        BigDecimal minRate = (BigDecimal) bc.get("daycare_slab_1_charge");
        BigDecimal minDiscount = (BigDecimal) bc.get("daycare_slab_1_charge_discount");
        BigDecimal slab1Rate = (BigDecimal) bc.get("daycare_slab_2_charge");
        BigDecimal slab1Discount = (BigDecimal) bc.get("daycare_slab_2_charge_discount");
        BigDecimal slab2Rate = (BigDecimal) bc.get("daycare_slab_3_charge");
        BigDecimal slab2Discount = (BigDecimal) bc.get("daycare_slab_3_charge_discount");
        BigDecimal incrRate = (BigDecimal) bc.get("hourly_charge");
        BigDecimal incrDiscount = (BigDecimal) bc.get("hourly_charge_discount");

        BigDecimal charge = getDurationCharge(duration, minDuration, slab1Duration,
            slab2Duration, incrDuration, minRate, slab1Rate, slab2Rate, incrRate, false);
        BigDecimal discount =
            getDurationCharge(duration, minDuration, slab1Duration, slab2Duration,
                incrDuration, minDiscount, slab1Discount, slab2Discount, incrDiscount, false);

        mainCharge = new ChargeDTO(cg, "B" + cg, // "BED", "BBED" or "ICU", "BICU"
            charge, BigDecimal.ONE, discount, "", bedType, bedType, null, isInsurance,
            mainServiceSubGroup, insuranceCategoryId, visitType, patientId, firstOfCategory);
        mainCharge.setAllowRateIncrease(true);
        mainCharge.setAllowRateDecrease(true);
      }

      if (null != con) {
        discBO.applyDiscountRule(con, mainCharge);
      } else {
        discBO.applyDiscountRule(mainCharge);
      }
    } else {
      // normal bed (or ICU): add a "Day" charge if num days is not 0
      if (bedDays.compareTo(BigDecimal.ZERO) > 0) {
        logger.debug("Main charge bed days: " + bedDays);
        mainCharge = new ChargeDTO(cg, "B" + cg, // "BED", "BBED" or "ICU", "BICU"
            (BigDecimal) bc.get("bed_charge"), bedDays,
            ((BigDecimal) bc.get("bed_charge_discount")).multiply(bedDays), "Days", bedType,
            bedType, null, isInsurance, mainServiceSubGroup, insuranceCategoryId, visitType,
            patientId, firstOfCategory);
        mainCharge.setAllowRateIncrease(true);
        mainCharge.setAllowRateDecrease(true);
        mainCharge.setBillNo((String) billNo);
        if (null != con) {
          discBO.applyDiscountRule(con, mainCharge);
        } else {
          discBO.applyDiscountRule(mainCharge);
        }
      }
    }

    if (mainCharge != null) {
      mainCharge.setFrom_date(from);
      mainCharge.setTo_date(to);
      mainCharge.setActRatePlanItemCode((String) bc.get("item_code"));
      mainCharge.setCodeType((String) bc.get("code_type"));
      if (bc.get("billing_group_id") != null) {
        mainCharge.setBillingGroupId((Integer) bc.get("billing_group_id"));
      }
      lis.add(mainCharge);
      bedAmount = mainCharge.getAmount();
      totalAmount = mainCharge.getAmount();
    }

    /*
     * Additional hourly charge component for the main bed charge, if not daycare
     */
    if (daycareStatus.equals("N") && bedHours.compareTo(BigDecimal.ZERO) > 0) {
      // add another charge item for the hourly charge, even if hourly rate is 0
      ChargeDTO ch = new ChargeDTO(cg, isBystander ? "BYBED" : "B" + cg,
          (BigDecimal) bc.get("hourly_charge"), bedHours,
          ((BigDecimal) bc.get("hourly_charge_discount")).multiply(bedHours), "Hrs", bedType,
          bedType, null, isInsurance, mainServiceSubGroup, insuranceCategoryId, visitType,
          patientId, firstOfCategory);
      ch.setAllowRateIncrease(true);
      ch.setAllowRateDecrease(true);
      ch.setFrom_date(from);
      ch.setTo_date(to);
      if (null != con) {
        discBO.applyDiscountRule(con, ch);
      } else {
        discBO.applyDiscountRule(ch);
      }

      if (mainCharge == null) {
        mainCharge = ch;
      }
      if (bc.get("billing_group_id") != null) {
        ch.setBillingGroupId((Integer) bc.get("billing_group_id"));
      }
      lis.add(ch);
      bedAmount = bedAmount.add(ch.getAmount());
      totalAmount = totalAmount.add(ch.getAmount());
    }

    if (mainCharge == null) {
      return lis;
    }

    /*
     * Associated charges for non-bystander, non-daycare beds provided !onlyMainCharges: nurse, duty
     * doctor, professional charge: only daily charges added if the corresponding rate is non-zero.
     */
    if (daycareStatus.equals("N") && !onlyMainCharges
        && bedDays.compareTo(BigDecimal.ZERO) > 0) {

      if (((BigDecimal) bc.get("nursing_charge")).compareTo(BigDecimal.ZERO) > 0) {
        BasicDynaBean chargeHeadBean = chargeHeadsDAO.findByKey("chargehead_id", "NC" + cg);
        ChargeDTO ch = new ChargeDTO(cg, "NC" + cg, (BigDecimal) bc.get("nursing_charge"),
            bedDays, ((BigDecimal) bc.get("nursing_charge_discount")).multiply(bedDays),
            "Days", bedType, bedType, null, isInsurance,
            (Integer) chargeHeadBean.get("service_sub_group_id"), insuranceCategoryId,
            visitType, patientId, firstOfCategory);
        ch.setAllowRateIncrease(true);
        ch.setAllowRateDecrease(true);
        ch.setFrom_date(from);
        ch.setTo_date(to);
        ch.setBillNo((String) billNo);
        if (null != con) {
          discBO.applyDiscountRule(con, ch);
        } else {
          discBO.applyDiscountRule(ch);
        }
        if (bc.get("billing_group_id") != null) {
          ch.setBillingGroupId((Integer) bc.get("billing_group_id"));
        }
        lis.add(ch);
        totalAmount = totalAmount.add(ch.getAmount());
      }
      if (((BigDecimal) bc.get("duty_charge")).compareTo(BigDecimal.ZERO) > 0) {
        BasicDynaBean chargeHeadBean = chargeHeadsDAO.findByKey("chargehead_id", "DD" + cg);
        ChargeDTO ch = new ChargeDTO(cg, "DD" + cg, (BigDecimal) bc.get("duty_charge"),
            bedDays, ((BigDecimal) bc.get("duty_charge_discount")).multiply(bedDays), "Days",
            bedType, bedType, null, isInsurance,
            (Integer) chargeHeadBean.get("service_sub_group_id"), insuranceCategoryId,
            visitType, patientId, firstOfCategory);
        ch.setAllowRateIncrease(true);
        ch.setAllowRateDecrease(true);
        ch.setFrom_date(from);
        ch.setTo_date(to);
        ch.setBillNo((String) billNo);
        if (null != con) {
          discBO.applyDiscountRule(con, ch);
        } else {
          discBO.applyDiscountRule(ch);
        }
        if (bc.get("billing_group_id") != null) {
          ch.setBillingGroupId((Integer) bc.get("billing_group_id"));
        }
        lis.add(ch);
        totalAmount = totalAmount.add(ch.getAmount());
      }
      if (((BigDecimal) bc.get("maintainance_charge")).compareTo(BigDecimal.ZERO) > 0) {
        BasicDynaBean chargeHeadBean = chargeHeadsDAO.findByKey("chargehead_id", "PC" + cg);
        ChargeDTO ch = new ChargeDTO(cg, "PC" + cg, (BigDecimal) bc.get("maintainance_charge"),
            bedDays, ((BigDecimal) bc.get("maintainance_charge_discount")).multiply(bedDays),
            "Days", bedType, bedType, null, isInsurance,
            (Integer) chargeHeadBean.get("service_sub_group_id"), insuranceCategoryId,
            visitType, patientId, firstOfCategory);
        ch.setAllowRateIncrease(true);
        ch.setAllowRateDecrease(true);
        ch.setChargeHeadName("maintainance_charge");
        ch.setFrom_date(from);
        ch.setTo_date(to);
        ch.setBillNo((String) billNo);
        if (null != con) {
          discBO.applyDiscountRule(con, ch);
        } else {
          discBO.applyDiscountRule(ch);
        }
        if (bc.get("billing_group_id") != null) {
          ch.setBillingGroupId((Integer) bc.get("billing_group_id"));
        }
        lis.add(ch);
        totalAmount = totalAmount.add(ch.getAmount());
      }
    }

    /*
     * Luxury tax
     */
    BigDecimal taxPer = (BigDecimal) bc.get("luxary_tax");

    if (taxPer.compareTo(BigDecimal.ZERO) > 0) {
      BasicDynaBean chargeHeadBean = chargeHeadsDAO.findByKey("chargehead_id", "LTAX");
      BasicDynaBean gprefs = GenericPreferencesDAO.getPrefsBean();
      BigDecimal taxAmount = ((String) gprefs.get("luxary_tax_applicable_on")).equals("B")
          ? bedAmount : totalAmount;
      taxAmount = taxAmount.multiply(taxPer).divide(new BigDecimal(100), 2);

      ChargeDTO ch = new ChargeDTO("TAX", "LTAX", taxAmount, BigDecimal.ONE, BigDecimal.ZERO,
          "", bedType, "On Bed Charges (" + bedType + ")", null, isInsurance,
          (Integer) chargeHeadBean.get("service_sub_group_id"), insuranceCategoryId, visitType,
          patientId, firstOfCategory);
      ch.setAllowRateIncrease(true);
      ch.setAllowRateDecrease(true);
      ch.setFrom_date(from);
      ch.setTo_date(to);
      if (bc.get("billing_group_id") != null) {
        ch.setBillingGroupId((Integer) bc.get("billing_group_id"));
      }
      lis.add(ch);
    }

    return lis;
  }

  /**
   * Gets the bed days hours.
   *
   * @param from          the from
   * @param to            the to
   * @param quantity      the quantity
   * @param daycareStatus the daycare status
   * @param isFirstBed    the is first bed
   * @param icuStatus     the icu status
   * @param prefs         the prefs
   * @return the bed days hours
   */
  public static BigDecimal[] getBedDaysHours(Timestamp from, Timestamp to, BigDecimal quantity,
      String daycareStatus, boolean isFirstBed, String icuStatus, BasicDynaBean prefs) {

    int[] daysHours;

    if (from == null) {
      if (daycareStatus.equals("Y")) {
        daysHours = new int[] {0, quantity.intValue()}; // supports only integer values
      } else {
        daysHours = DateUtil.getDaysHours(quantity);
      }
    } else if (daycareStatus.equals("Y")) {
      if (prefs.get("merge_beds").equals("Y")) {
        // returns days, hours for the given range with out round off
        daysHours = new int[] {0, DateUtil.getHours(from, to, false)};
      } else {
        // returns days, hours for the given range
        daysHours = new int[] {0, DateUtil.getHours(from, to)};
      }
    } else {
      if (prefs.get("merge_beds").equals("Y")) {
        daysHours = DateUtil.getDaysHours(from, to, false);
      } else {
        daysHours = DateUtil.getDaysHours(from, to);
      }
    }

    if (daycareStatus.equals("Y")) {
      return new BigDecimal[] {BigDecimal.ZERO, new BigDecimal(daysHours[1])};
    }

    // use days, hours and get number of days and hours to charge for the bed.
    String[] thresholds = getThresholds(prefs, daysHours, isFirstBed, icuStatus);

    String hourlyThreshold = thresholds[0];
    String halfDayThreshold = thresholds[1];
    String fullDayThreshold = thresholds[2];
    int fullDay = 24; // > this is treated as full day
    if (!fullDayThreshold.equals("-")) {
      fullDay = Integer.parseInt(fullDayThreshold);
    }

    int halfDay = fullDay; // (> halfDay, <= fullDay) considered half-a-day.
    if (!halfDayThreshold.equals("-")) {
      halfDay = Integer.parseInt(halfDayThreshold);
    }

    int hourly = halfDay; // (> hourly, <= halfDay), then it is charged hourly
    if (!hourlyThreshold.equals("-")) {
      hourly = Integer.parseInt(hourlyThreshold);
    }
    int days = daysHours[0];
    int hours = daysHours[1];
    // > 0, <= hourly no charges
    BigDecimal bedDays = new BigDecimal(days).setScale(1);
    BigDecimal bedHours = BigDecimal.ZERO;

    if (hours > fullDay) {
      bedDays = bedDays.add(BigDecimal.ONE);
    } else if (hours > halfDay) {
      bedDays = bedDays.add(new BigDecimal("0.5"));
    } else if (hours > hourly) {
      bedHours = new BigDecimal(hours);
    } else {
      // the hours are not charged
    }
    logger.debug("Actual: " + days + " Days " + hours + " hours; Thresholds (first bed "
        + isFirstBed + "): " + hourlyThreshold + "," + halfDayThreshold + ","
        + fullDayThreshold + " Bed days/hours: " + bedDays + "," + bedHours);

    return new BigDecimal[] {bedDays, bedHours};
  }

  // method to get a registration charges for multiple plans.

  /**
   * Gets the registration charges.
   *
   * @param bedType         the bed type
   * @param orgId           the org id
   * @param chargeHead      the charge head
   * @param isRenewal       the is renewal
   * @param isInsurance     the is insurance
   * @param planIds         the plan ids
   * @param excludeZero     the exclude zero
   * @param visitType       the visit type
   * @param patientId       the patient id
   * @param con             the con
   * @param firstOfCategory the first of category
   * @return the registration charges
   * @throws SQLException the SQL exception
   */
  public static List<ChargeDTO> getRegistrationCharges(String bedType, String orgId,
      String chargeHead, boolean isRenewal, Boolean isInsurance, int[] planIds,
      boolean excludeZero, String visitType, String patientId, Connection con,
      Boolean firstOfCategory) throws SQLException {

    BigDecimal charge = null;
    BigDecimal discount = null;

    Map<String, BigDecimal> map = new HashMap<String, BigDecimal>();
    map = getRegChargeandDiscount(chargeHead, isRenewal, orgId, bedType, visitType);
    charge = map.get("charge");
    discount = map.get("discount");

    BasicDynaBean chargeHeadBean = chargeHeadsDAO.findByKey("chargehead_id", chargeHead);
    ChargeDTO chrgdto = new ChargeDTO("REG", chargeHead, (BigDecimal) charge, BigDecimal.ONE,
        (BigDecimal) discount, "", chargeHead, "", null, isInsurance,
        (Integer) chargeHeadBean.get("service_sub_group_id"),
        (Integer) chargeHeadBean.get("insurance_category_id"), visitType, patientId,
        firstOfCategory);
    chrgdto.setPreAuthModeId(1);
    chrgdto.setAllowRateIncrease((Boolean) chargeHeadBean.get("allow_rate_increase"));
    chrgdto.setAllowRateDecrease((Boolean) chargeHeadBean.get("allow_rate_decrease"));
    // chrgdto.setInsuranceAmt(planIds, visitType,firstOfCategory);

    List lis = new ArrayList();
    if ((charge.compareTo(BigDecimal.ZERO) != 0) || !excludeZero) {
      lis.add(chrgdto);
    }
    return lis;
  }

  /**
   * Gets the registration charges.
   *
   * @param bedType         the bed type
   * @param orgId           the org id
   * @param chargeHead      the charge head
   * @param isRenewal       the is renewal
   * @param isInsurance     the is insurance
   * @param planId          the plan id
   * @param excludeZero     the exclude zero
   * @param visitType       the visit type
   * @param patientId       the patient id
   * @param con             the con
   * @param firstOfCategory the first of category
   * @return the registration charges
   * @throws SQLException the SQL exception
   */
  public static List<ChargeDTO> getRegistrationCharges(String bedType, String orgId,
      String chargeHead, boolean isRenewal, Boolean isInsurance, int planId,
      boolean excludeZero, String visitType, String patientId, Connection con,
      Boolean firstOfCategory) throws SQLException {

    BigDecimal charge = null;
    BigDecimal discount = null;

    Map<String, BigDecimal> map = new HashMap<String, BigDecimal>();
    map = getRegChargeandDiscount(chargeHead, isRenewal, orgId, bedType, visitType);
    charge = map.get("charge");
    discount = map.get("discount");

    BasicDynaBean chargeHeadBean = chargeHeadsDAO.findByKey("chargehead_id", chargeHead);
    ChargeDTO chrgdto = new ChargeDTO("REG", chargeHead, (BigDecimal) charge, BigDecimal.ONE,
        (BigDecimal) discount, "", chargeHead, "", null, isInsurance, planId,
        (Integer) chargeHeadBean.get("service_sub_group_id"),
        (Integer) chargeHeadBean.get("insurance_category_id"), visitType, patientId,
        firstOfCategory);
    chrgdto.setPreAuthModeId(1);
    chrgdto.setAllowRateIncrease((Boolean) chargeHeadBean.get("allow_rate_increase"));
    chrgdto.setAllowRateDecrease((Boolean) chargeHeadBean.get("allow_rate_decrease"));
    List lis = new ArrayList();
    if ((charge.compareTo(BigDecimal.ZERO) != 0) || !excludeZero) {
      lis.add(chrgdto);
    }
    return lis;
  }

  /**
   * Gets the reg chargeand discount.
   *
   * @param chargeHead the charge head
   * @param isRenewal  the is renewal
   * @param orgId      the org id
   * @param bedType    the bed type
   * @param visitType  the visit type
   * @return the reg chargeand discount
   * @throws SQLException the SQL exception
   */
  public static Map<String, BigDecimal> getRegChargeandDiscount(String chargeHead,
      boolean isRenewal, String orgId, String bedType, String visitType) throws SQLException {

    BigDecimal charge = null;
    BigDecimal discount = null;
    BasicDynaBean regChargesBean =
        new RegistrationChargesDAO().getRegistrationCharges(bedType, orgId);
    if (chargeHead.equals("GREG")) {
      if (isRenewal) {
        charge = (BigDecimal) regChargesBean.get("reg_renewal_charge");
        discount = (BigDecimal) regChargesBean.get("reg_renewal_charge_discount");
      } else {
        charge = (BigDecimal) regChargesBean.get("gen_reg_charge");
        discount = (BigDecimal) regChargesBean.get("gen_reg_charge_discount");
      }
    } else if (chargeHead.equals("IPREG")) {
      charge = (BigDecimal) regChargesBean.get("ip_reg_charge");
      discount = (BigDecimal) regChargesBean.get("ip_reg_charge_discount");
    } else if (chargeHead.equals("OPREG")) {
      charge = (BigDecimal) regChargesBean.get("op_reg_charge");
      discount = (BigDecimal) regChargesBean.get("op_reg_charge_discount");
    } else if (chargeHead.equals("MLREG")) {
      if (visitType.equalsIgnoreCase("i")) {
        charge = (BigDecimal) regChargesBean.get("ip_mlccharge");
        discount = (BigDecimal) regChargesBean.get("ip_mlccharge_discount");
      } else {
        charge = (BigDecimal) regChargesBean.get("op_mlccharge");
        discount = (BigDecimal) regChargesBean.get("op_mlccharge_discount");
      }
    } else if (chargeHead.equals("EMREG")) {
      charge = (BigDecimal) regChargesBean.get("mrcharge");
      discount = (BigDecimal) regChargesBean.get("mrcharge_discount");
    } else {
      logger.error("Invalid registration charge head: " + chargeHead);
    }

    Map<String, BigDecimal> map = new HashMap<String, BigDecimal>();
    map.put("charge", charge);
    map.put("discount", discount);
    return map;
  }

  /**
   * Gets the direct charges.
   *
   * @param chargeHead the charge head
   * @return the direct charges
   * @throws SQLException the SQL exception
   */
  public static List<ChargeDTO> getDirectCharges(String chargeHead) throws SQLException {
    // there are no specified charges. Return a "0" charge item.
    ChargeDTO charge = new ChargeDTO();
    charge.setChargeHead(chargeHead);
    // set the sub Group ID from chargehead constants
    BasicDynaBean chargeHeadBean = chargeHeadsDAO.findByKey("chargehead_id", chargeHead);
    charge.setServiceSubGroupId((Integer) chargeHeadBean.get("service_sub_group_id"));
    charge.setAllowRateIncrease((Boolean) chargeHeadBean.get("allow_rate_increase"));
    charge.setAllowRateDecrease((Boolean) chargeHeadBean.get("allow_rate_decrease"));
    charge.setAmount_included(BigDecimal.ZERO);
    charge.setQty_included(BigDecimal.ZERO);

    List<ChargeDTO> charges = new ArrayList<ChargeDTO>();
    charges.add(charge);
    return charges;
  }

  /**
   * Gets the ot doctor charges.
   *
   * @param doctor          the doctor
   * @param otDocRole       the ot doc role
   * @param visitType       the visit type
   * @param operationRates  the operation rates
   * @param quantity        the quantity
   * @param isInsurance     the is insurance
   * @param planIds         the plan ids
   * @param bedType         the bed type
   * @param patientId       the patient id
   * @param firstOfCategory the first of category
   * @return the ot doctor charges
   * @throws SQLException the SQL exception
   */
  public static List<ChargeDTO> getOtDoctorCharges(BasicDynaBean doctor, String otDocRole,
      String visitType, BasicDynaBean operationRates, BigDecimal quantity, boolean isInsurance,
      int[] planIds, String bedType, String patientId, Boolean firstOfCategory)
      throws SQLException {

    String desc = (String) doctor.get("doctor_name");

    int insuranceCategoryId = 0;

    BigDecimal doctorCharge = BigDecimal.ZERO;
    BigDecimal discount = BigDecimal.ZERO;

    // base charge is based on the operation
    if (operationRates == null) {
      logger.warn("Surgeon/anaesthetist charge without operation " + desc);

    } else {
      if (otDocRole.equals("SUOPE")) {
        doctorCharge = (BigDecimal) operationRates.get("surgeon_charge");
        discount = (BigDecimal) operationRates.get("surg_discount");
      } else if (otDocRole.equals("ANAOPE")) {
        doctorCharge = (BigDecimal) operationRates.get("anesthetist_charge");
        discount = (BigDecimal) operationRates.get("anest_discount");
      } else {
        logger.error("Invalid OT Doc role supplied: " + otDocRole);
      }
      desc = operationRates.get("operation_name") + "/" + desc;
      insuranceCategoryId = (Integer) operationRates.get("insurance_category_id");
    }

    // the doctor premium is based on the ot doc role, hardcoded to doctor fields
    if (otDocRole.equals("COSOPE")) {
      doctorCharge = doctorCharge.add((BigDecimal) doctor.get("co_surgeon_charge"));
      discount = discount.add((BigDecimal) doctor.get("co_surgeon_charge_discount"));
    } else if (otDocRole.equals("ASUOPE") || otDocRole.equals("AANOPE")) {
      doctorCharge = doctorCharge.add((BigDecimal) doctor.get("assnt_surgeon_charge"));
      discount = discount.add((BigDecimal) doctor.get("assnt_surgeon_charge_discount"));
    } else if (otDocRole.equals("IPDOC")) {
      doctorCharge = doctorCharge.add((BigDecimal) doctor.get("doctor_ip_charge"));
      discount = discount.add((BigDecimal) doctor.get("doctor_ip_charge_discount"));
    } else {
      doctorCharge = doctorCharge.add((BigDecimal) doctor.get("ot_charge"));
      discount = discount.add((BigDecimal) doctor.get("ot_charge_discount"));
    }

   
    // chrgdto.setInsuranceAmt(planIds, visitType, chrgdto.getFirstOfCategory());

    // no rate plan code applicable for OT doctor charges

    // Consider surgery and charge head rate increase, rate decrease options for
    // these doctor roles i.e SUOPE, ASUOPE, COSOPE, ANAOPE, AANOPE charge heads.

    boolean allowRateIncrease = false;
    boolean allowRateDecrease = false;

    if (operationRates != null) {
      allowRateIncrease = (Boolean) operationRates.get("allow_rate_increase");
      allowRateDecrease = (Boolean) operationRates.get("allow_rate_decrease");
    }

    chargeHeadsDAO.clearCache();
    BasicDynaBean chargeheadBean = chargeHeadsDAO.findByKey("chargehead_id", otDocRole);
    if (chargeheadBean != null) {
      allowRateIncrease &= (Boolean) chargeheadBean.get("allow_rate_increase");
      allowRateDecrease &= (Boolean) chargeheadBean.get("allow_rate_decrease");
    }
    
    String chargeGroup = "OPE";
    int subGroupId = (Integer) doctor.get("service_sub_group_id");

    ChargeDTO chrgdto = new ChargeDTO(chargeGroup, otDocRole, doctorCharge, quantity,
        discount.multiply(quantity), "", (String) doctor.get("doctor_id"), desc,
        (String) doctor.get("dept_id"), isInsurance, subGroupId, insuranceCategoryId,
        visitType, patientId, firstOfCategory);

    chrgdto.setAllowRateIncrease(allowRateIncrease);
    chrgdto.setAllowRateDecrease(allowRateDecrease);

    chrgdto.setActRatePlanItemCode((String) operationRates.get("item_code"));
    chrgdto.setCodeType((String) operationRates.get("code_type"));

    // set the conducting doctor ID same as the consulting doctor id.
    chrgdto.setPayeeDoctorId((String) doctor.get("doctor_id"));

    if (null != operationRates.get("op_id")) {
      chrgdto.setOp_id((String) operationRates.get("op_id"));
    }
    if (null != operationRates.get("billing_group_id")) {
      chrgdto.setBillingGroupId((Integer) operationRates.get("billing_group_id"));
    }
    List lis = new ArrayList();
    lis.add(chrgdto);
    return lis;
  }

  /**
   * Gets the doctor cons charges.
   *
   * @param doctor          the doctor
   * @param consTypeBean    the cons type bean
   * @param visitType       the visit type
   * @param orgDetails      the org details
   * @param quantity        the quantity
   * @param isInsurance     the is insurance
   * @param planIds         the plan ids
   * @param bedType         the bed type
   * @param patientId       the patient id
   * @param firstOfCategory the first of category
   * @return the doctor cons charges
   * @throws SQLException the SQL exception
   */
  public static List<ChargeDTO> getDoctorConsCharges(BasicDynaBean doctor,
      BasicDynaBean consTypeBean, String visitType, BasicDynaBean orgDetails,
      BigDecimal quantity, boolean isInsurance, int[] planIds, String bedType,
      String patientId, Boolean firstOfCategory) throws SQLException {

    String rpItemCode = null;

    BigDecimal doctorCharge = BigDecimal.ZERO;
    BigDecimal discount = BigDecimal.ZERO;

    int consTypeId = (Integer) consTypeBean.get("consultation_type_id");
    BasicDynaBean consultationTypeCharge =
        getConsultationCharge(consTypeId, bedType, (String) orgDetails.get("org_id"));

    String docChargeType = (String) consTypeBean.get("doctor_charge_type");

    doctorCharge = (BigDecimal) doctor.get(docChargeType);
    discount = (BigDecimal) doctor.get(docChargeType + "_discount");
    rpItemCode = consultationTypeCharge.get("item_code") != null
        ? (String) consultationTypeCharge.get("item_code") : null;
    String codeType = null;
    codeType = consultationTypeCharge.get("code_type") != null
        ? (String) consultationTypeCharge.get("code_type") : null;

    doctorCharge = doctorCharge.add((BigDecimal) consultationTypeCharge.get("charge"));
    discount = (consultationTypeCharge.get("discount") != null
        ? discount.add((BigDecimal) consultationTypeCharge.get("discount")) : discount);

    String chargeGroup = "DOC";
    String consultationChargeHead = (String) consTypeBean.get("charge_head");
    String desc = (String) doctor.get("doctor_name");
    ChargeDTO chrgdto = new ChargeDTO(chargeGroup, consultationChargeHead, doctorCharge,
        quantity, discount.multiply(quantity), "", (String) doctor.get("doctor_id"), desc,
        (String) doctor.get("dept_id"), isInsurance,
        (Integer) consTypeBean.get("service_sub_group_id"),
        (Integer) consTypeBean.get("insurance_category_id"), visitType, patientId,
        firstOfCategory);

    chrgdto.setInsuranceAmt(planIds, visitType, chrgdto.getFirstOfCategory());

    chrgdto.setActRatePlanItemCode(rpItemCode);
    chrgdto.setCodeType(codeType);
    chrgdto.setConsultation_type_id(consTypeId);
    chrgdto.setAllowRateIncrease((Boolean) consTypeBean.get("allow_rate_increase"));
    chrgdto.setAllowRateDecrease((Boolean) consTypeBean.get("allow_rate_decrease"));

    // set the conducting doctor ID same as the consulting doctor id.
    chrgdto.setPayeeDoctorId((String) doctor.get("doctor_id"));
    if (consTypeBean.get("billing_group_id") != null) {
      chrgdto.setBillingGroupId((Integer) consTypeBean.get("billing_group_id"));
    }
    List lis = new ArrayList();
    lis.add(chrgdto);
    return lis;
  }

  // multivisitpackage
  /**
   * Gets the doctor cons charges.
   *
   * @param doctor          the doctor
   * @param consTypeBean    the cons type bean
   * @param visitType       the visit type
   * @param orgDetails      the org details
   * @param quantity        the quantity
   * @param isInsurance     the is insurance
   * @param planIds         the plan ids
   * @param bedType         the bed type
   * @param patientId       the patient id
   * @param firstOfCategory the first of category
   * @param doctorCharge    the doctor charge
   * @return the doctor cons charges
   * @throws SQLException the SQL exception
   */
  public static List<ChargeDTO> getDoctorConsCharges(BasicDynaBean doctor,
      BasicDynaBean consTypeBean, String visitType, BasicDynaBean orgDetails,
      BigDecimal quantity, boolean isInsurance, int[] planIds, String bedType,
      String patientId, Boolean firstOfCategory, BigDecimal doctorCharge) throws SQLException {

    String desc = (String) doctor.get("doctor_name");
    String rpItemCode = null;
    String chargeGroup = "DOC";
    String codeType = null;

    BigDecimal discount = BigDecimal.ZERO;

    int consTypeId = (Integer) consTypeBean.get("consultation_type_id");
    BasicDynaBean consultationTypeCharge =
        getConsultationCharge(consTypeId, bedType, (String) orgDetails.get("org_id"));

    String consultationChargeHead = (String) consTypeBean.get("charge_head");

    rpItemCode = consultationTypeCharge.get("item_code") != null
        ? (String) consultationTypeCharge.get("item_code") : null;
    codeType = consultationTypeCharge.get("code_type") != null
        ? (String) consultationTypeCharge.get("code_type") : null;

    ChargeDTO chrgdto = new ChargeDTO(chargeGroup, consultationChargeHead, doctorCharge,
        quantity, discount.multiply(quantity), "", (String) doctor.get("doctor_id"), desc,
        (String) doctor.get("dept_id"), isInsurance,
        (Integer) consTypeBean.get("service_sub_group_id"),
        (Integer) consTypeBean.get("insurance_category_id"), visitType, patientId,
        firstOfCategory);

    chrgdto.setInsuranceAmt(planIds, visitType, chrgdto.getFirstOfCategory());
    chrgdto.setActRatePlanItemCode(rpItemCode);
    chrgdto.setCodeType(codeType);
    chrgdto.setConsultation_type_id(consTypeId);
    chrgdto.setAllowRateIncrease((Boolean) consTypeBean.get("allow_rate_increase"));
    chrgdto.setAllowRateDecrease((Boolean) consTypeBean.get("allow_rate_decrease"));

    chrgdto.setPayeeDoctorId((String) doctor.get("doctor_id"));

    List lis = new ArrayList();
    lis.add(chrgdto);
    return lis;
  }

  /**
   * Gets the package charges.
   *
   * @param pkg             the pkg
   * @param quantity        the quantity
   * @param surgeon         the surgeon
   * @param isInsurance     the is insurance
   * @param planIds         the plan ids
   * @param visitType       the visit type
   * @param patientId       the patient id
   * @param firstOfCategory the first of category
   * @return the package charges
   * @throws SQLException the SQL exception
   */
  public static List<ChargeDTO> getPackageCharges(BasicDynaBean pkg, BigDecimal quantity,
      String surgeon, boolean isInsurance, int[] planIds, String visitType, String patientId,
      Boolean firstOfCategory) throws SQLException {
    int insuranceCategoryId = getIntegerValueFromBean(pkg, "insurance_category_id");
    ChargeDTO chrgdto = new ChargeDTO("PKG", "PKGPKG", (BigDecimal) pkg.get("charge"),
        quantity, ((BigDecimal) pkg.get("discount")).multiply(quantity), "",
        pkg.get("package_id").toString(), (String) pkg.get("package_name"), null, isInsurance,
        (Integer) pkg.get("service_sub_group_id"), insuranceCategoryId, visitType, patientId,
        firstOfCategory);
    // chrgdto.setInsuranceAmt(planIds, visitType, chrgdto.getFirstOfCategory());
    chrgdto.setActRatePlanItemCode((String) pkg.get("item_code"));
    chrgdto.setCodeType((String) pkg.get("code_type"));
    chrgdto.setAllowDiscount((Boolean) pkg.get("allow_discount"));
    chrgdto.setAllowRateIncrease((Boolean) pkg.get("allow_rate_increase"));
    chrgdto.setAllowRateDecrease((Boolean) pkg.get("allow_rate_decrease"));
    chrgdto.setPackageId((Integer) pkg.get("package_id"));
    // special case: if there is a surgeon, set the package conducting doctor
    // to be the same as the surgeon.
    if (surgeon != null && !surgeon.equals("")) {
      chrgdto.setPayeeDoctorId(surgeon);
    }
    if (pkg.get("billing_group_id") != null) {
      chrgdto.setBillingGroupId((Integer) pkg.get("billing_group_id"));
    }
    List lis = new ArrayList();
    lis.add(chrgdto);
    return lis;
  }

  /**
   * If this item ordered with reference to any newly ordered surgery, actual operation_ref gets
   * from 'orderOperation' which keeps track of them in 'operationPrescIdsMap'.
   *
   * @param orderBean the order bean
   * @return true, if successful
   */
  private boolean setOperationRef(BasicDynaBean orderBean) {
    Integer operationRef = (Integer) orderBean.get("operation_ref");
    if (operationRef != null && operationRef <= 0) {
      orderBean.set("operation_ref",
          (Integer) operationPrescIdsMap.get(orderBean.get("operation_ref")));
      return true;
    }
    return (operationRef != null);
  }

  // this method gets called for inserting or updating the documents.
  // if called from update, the list contains all the orders.
  /**
   * Insert test documents.
   *
   * @param con          the con
   * @param list         the list
   * @param prescribedId the prescribed id
   * @return the string
   * @throws SQLException the SQL exception
   * @throws IOException  Signals that an I/O exception has occurred.
   */
  // if called from insert, the list contains only the documents of that item.
  public String insertTestDocuments(Connection con, List<TestDocumentDTO> list,
      Integer prescribedId) throws SQLException, IOException {

    if (list != null && !list.isEmpty()) {
      AbstractDocumentPersistence persistenceApi =
          AbstractDocumentPersistence.getInstance("lab_test_doc", true);
      for (TestDocumentDTO dto : list) {
        boolean success = true;
        if (prescribedId != null && prescribedId != 0) {
          dto.setTestPrescId(prescribedId);
        }
        if (dto.isDeleteDocument() && dto.getDocId() != 0) {
          Map<String, Object[]> deleteParamMap = new HashMap<String, Object[]>();
          deleteParamMap.put("deleteDocument",
              new String[] {dto.getDocId() + "," + "doc_fileupload"});
          success = persistenceApi.delete(deleteParamMap, con);

          if (!success) {
            return (String) deleteParamMap.get("error")[0];
          }
        }

        if (dto.isNotesEdited()) {
          Map columndata = new HashMap();
          columndata.put("clinical_notes", dto.getClinicalNotes());
          if (testDao.update(con, columndata, "prescribed_id", dto.getTestPrescId()) != 1) {
            return "Failed to update the clinical notes";
          }
        }
        if (dto.getDocContent() == null) {
          continue;
        }

        Map<String, Object[]> newparamMap = new HashMap<String, Object[]>();
        ConversionUtils.copyStringToMap(newparamMap, "username", userName);
        ConversionUtils.copyObjectToMap(newparamMap, "doc_content_bytea", dto.getDocContent());
        ConversionUtils.copyObjectToMap(newparamMap, "content_type", dto.getContentType());
        ConversionUtils.copyObjectToMap(newparamMap, "fileName", dto.getFileName());
        ConversionUtils.copyStringToMap(newparamMap, "doc_date",
            DateUtil.formatDate(new java.util.Date()));

        if (dto.getDocId() != 0) {
          ConversionUtils.copyObjectToMap(newparamMap, "doc_id", dto.getDocId());
          success = persistenceApi.update(newparamMap, con, false);
        } else {
          // insert the document in two cases.
          // 1) user attached a new document for the existing ordered item.
          // 2) user attached a new document for freshly added ordered item.
          ConversionUtils.copyStringToMap(newparamMap, "doc_type",
              dto.getTestCategory().equals("DEP_LAB") ? "SYS_LR" : "SYS_RR");
          ConversionUtils.copyStringToMap(newparamMap, "prescribed_id",
              dto.getTestPrescId() + "");
          ConversionUtils.copyStringToMap(newparamMap, "doc_name", "Test Clinical Document");
          ConversionUtils.copyStringToMap(newparamMap, "doc_format", "doc_fileupload");
          ConversionUtils.copyStringToMap(newparamMap, "format", "doc_fileupload");

          success = persistenceApi.create(newparamMap, con);
        }
        String error = success ? null : (String) newparamMap.get("error")[0];
        newparamMap.clear();

        if (!success) {
          return error;
        }

      }
    }
    return null;
  }

  /**
   * Update orders.
   *
   * @param con                    the con
   * @param orders                 the orders
   * @param cancel                 the cancel
   * @param cancelCharges          the cancel charges
   * @param unlinkActivity         the unlink activity
   * @param editOrCancelOrderBills the edit or cancel order bills
   * @param opEditAnaesTypesList   the op edit anaes types list
   * @param testAdditionalDocs     the test additional docs
   * @return the string
   * @throws SQLException the SQL exception
   * @throws IOException  Signals that an I/O exception has occurred.
   */
  public String updateOrders(Connection con, List<BasicDynaBean> orders, boolean cancel,
      boolean cancelCharges, boolean unlinkActivity, List<String> editOrCancelOrderBills,
      List<Map<String, Object>> opEditAnaesTypesList, List<TestDocumentDTO> testAdditionalDocs)
      throws SQLException, IOException {
    return updateOrders(con, orders, cancel, cancelCharges, unlinkActivity, false,
        editOrCancelOrderBills, opEditAnaesTypesList, testAdditionalDocs);
  }

  /**
   * Update orders.
   *
   * @param con                    the con
   * @param orders                 the orders
   * @param cancel                 the cancel
   * @param cancelCharges          the cancel charges
   * @param unlinkActivity         the unlink activity
   * @param ignoreBillStatus       the ignore bill status
   * @param editOrCancelOrderBills the edit or cancel order bills
   * @param opEditAnaesTypesList   the op edit anaes types list
   * @param testAdditionalDocs     the test additional docs
   * @return the string
   * @throws SQLException the SQL exception
   * @throws IOException  Signals that an I/O exception has occurred.
   */
  public String updateOrders(Connection con, List<BasicDynaBean> orders, boolean cancel,
      boolean cancelCharges, boolean unlinkActivity, boolean ignoreBillStatus,
      List<String> editOrCancelOrderBills, List<Map<String, Object>> opEditAnaesTypesList,
      List<TestDocumentDTO> testAdditionalDocs) throws SQLException, IOException {

    HttpSession session = (HttpSession) RequestContext.getSession();
    Preferences pref = (Preferences) session.getAttribute("preferences");
    String modAdavncedOt = "Y";
    if ((pref != null) && (pref.getModulesActivatedMap() != null)) {
      modAdavncedOt = (String) pref.getModulesActivatedMap().get("mod_advanced_ot");
      if (modAdavncedOt == null || modAdavncedOt.equals("")) {
        modAdavncedOt = "N";
      }
    }
    int opEditAnaesTypeIndex = 0;

    ChargeDAO chargeDao = new ChargeDAO(con);
    BillChargeClaimTaxDAO claimTaxDao = new BillChargeClaimTaxDAO();
    List<ChargeDTO> chargeListToUpdateTax = new ArrayList<ChargeDTO>();

    String error = insertTestDocuments(con, testAdditionalDocs, null);
    if (error != null) {
      return error;
    }

    for (BasicDynaBean bean : orders) {

      DynaClass clas = bean.getDynaClass();
      String activityCode = null;
      String primaryKey = null;
      String prescDrFieldName = null;
      RadiologyBO cancTestBO = new RadiologyBO();
      BasicDynaBean cancleTestBean = null;
      BasicDynaBean cancelChildTestBean = null;
      boolean docPartOfPack = false;
      String childTestChargeId = null;

      if (clas.getName().equals("tests_prescribed")) {
        primaryKey = "prescribed_id";
        prescDrFieldName = "pres_doctor";
        activityCode = "DIA";
        if (cancel) {
          OrderDAO.updateCancelStatusToPatient(con, activityCode,
              (Integer) bean.get("prescribed_id"));
        }

      } else if (clas.getName().equals("services_prescribed")) {
        primaryKey = "prescription_id";
        prescDrFieldName = "doctor_id";
        activityCode = "SER";
        if (cancel) {
          OrderDAO.updateCancelStatusToPatient(con, activityCode,
              (Integer) bean.get("prescription_id"));
        }

      } else if (clas.getName().equals("equipment_prescribed")) {
        primaryKey = "prescribed_id";
        prescDrFieldName = "doctor_id";
        activityCode = "EQU";

      } else if (clas.getName().equals("other_services_prescribed")) {
        primaryKey = "prescribed_id";
        prescDrFieldName = "doctor_id";
        activityCode = "OTC";

      } else if (clas.getName().equals("doctor_consultation")) {
        primaryKey = "consultation_id";
        prescDrFieldName = "presc_doctor_id";
        activityCode = "DOC";
        if (cancel) {
          OrderDAO.updateCancelStatusToPatient(con, activityCode,
              (Integer) bean.get("consultation_id"));
        }
        if (bean.get("package_ref") != null) {
          docPartOfPack = true;
        }

      } else if (clas.getName().equals("diet_prescribed")) {
        primaryKey = "ordered_id";
        prescDrFieldName = "ordered_by";
        activityCode = "DIE";

      } else if (clas.getName().equals("bed_operation_schedule")) {
        primaryKey = "prescribed_id";
        prescDrFieldName = "consultant_doctor";
        activityCode = "OPE";
        if (cancel) {
          cancelOperationRefOrders(con, patientId, (Integer) bean.get(primaryKey),
              cancelCharges, userName, editOrCancelOrderBills);
        }
        // if advanced ot module is enabled then marking an opeartion status as 'X' in
        // opeartion_details table.
        if (modAdavncedOt.equals("Y") && (cancel || cancelCharges)) {
          cancelAdvancedOTSurgery(con, (Integer) bean.get(primaryKey));
        }

      } else if (clas.getName().equals("package_prescribed")) {
        primaryKey = "prescription_id";
        prescDrFieldName = "doctor_id";
        activityCode = "PKG";
        if (cancel) {
          cancelPackageRefOrders(con, patientId, (Integer) bean.get(primaryKey), userName,
              editOrCancelOrderBills);
        }
      }

      logger.debug("Trying to update order of type: " + clas.getName() + " PK: " + primaryKey
          + " activityCode: " + activityCode);

      GenericDAO orderDao = new GenericDAO(clas.getName());

      /*
       * update the record with new values
       */
      orderDao.updateWithName(con, bean.getMap(), primaryKey);

      /*
       * Modify the charge and related elements also.
       */
      // update charges for finalizable items.
      if (!cancel && clas.getName().equals("equipment_prescribed")) {
        updateEquipmentCharges(con, bean);
      } else if (!cancel && clas.getName().equals("bed_operation_schedule")) {
        Map opEditAnaesTypesMap =
            (opEditAnaesTypesList != null && opEditAnaesTypesList.size() > 0)
                ? opEditAnaesTypesList.get(opEditAnaesTypeIndex) : null;
        opEditAnaesTypeIndex++;
        updateTheatreCharges(con, bean);
        updateAnaesthesiaTypeCharges(con, bean, opEditAnaesTypesMap);
        updateAnaesthesiaTypeDetails(con, bean, opEditAnaesTypesMap);
      }

      String activityId = bean.get(primaryKey).toString();
      String chargeId = BillActivityChargeDAO.getChargeId(activityCode, activityId);

      // If cancelling with charges (cancelCharges), check if bill is open
      // It is valid to cancel the activity or update user_remarks
      // after the bill is closed, by doing cancle without refund.
      if (!ignoreBillStatus && chargeId != null && (cancelCharges)) {

        String billStatus = chargeDao.getBillStatus(chargeId);
        if (billStatus != null && !billStatus.equals("A")) {
          return "Bill status is not open: cannot update/cancel charge";
        }
      }

      if (cancel) {

        if (clas.getName().equals("tests_prescribed")) {
          cancleTestBean = testDao.findByKey("prescribed_id", bean.get("prescribed_id"));

          // handling to cancel the test from child center also for Internal Lab
          if (cancleTestBean.get("outsource_dest_prescribed_id") != null
              && !cancleTestBean.get("outsource_dest_prescribed_id").equals("")) {

            cancelChildTestBean = testDao.findByKey("prescribed_id",
                cancleTestBean.get("outsource_dest_prescribed_id"));
            childTestChargeId = BillActivityChargeDAO.getChargeId(activityCode, activityId);
            RadiologyBO.cancelChildTest(con, cancelChildTestBean);
          }
        }

        if (clas.getName().equals("tests_prescribed")
            && (Boolean) cancleTestBean.get("re_conduction")) {
          cancTestBO.onCancleReconductTest(con, cancleTestBean);
          if (cancelChildTestBean != null) {
            cancTestBO.onCancleReconductTest(con, cancelChildTestBean);
          }
        } else {
          // cancel charge and associated charges
          if (cancelCharges) {
            chargeDao.cancelChargeUpdateAuditLog(con, chargeId, true, userName);
            if (cancelChildTestBean != null) {
              chargeDao.cancelChargeUpdateAuditLog(con, childTestChargeId, true, userName);
            }

            billChargeTaxDAO.cancelBillChargeTax(con, chargeId);
            chargeDao.cancelBillChargeClaim(con, chargeId);
            claimTaxDao.cancelBillChargeClaimTax(con, chargeId);

            List<BasicDynaBean> associatedChargeList =
                chargeDao.getAssociatedCharges(con, chargeId);
            for (BasicDynaBean bean1 : associatedChargeList) {
              String chargeRef = String.valueOf(bean1.get("charge_id"));
              billChargeTaxDAO.cancelBillChargeTax(con, chargeRef);
              chargeDao.cancelBillChargeClaim(con, chargeRef);
              claimTaxDao.cancelBillChargeClaimTax(con, chargeId);
            }

          } else if (unlinkActivity) {
            // update the charge as hasActivity = false, when not canceling the charge.
            chargeDao.updateHasActivityStatus(chargeId, false, true); // true: refs also
            new BillActivityChargeDAO(con).deleteActivity(activityCode, activityId);

            if (cancelChildTestBean != null) {
              chargeDao.updateHasActivityStatus(childTestChargeId, false, true); // true: refs
              new BillActivityChargeDAO(con).deleteActivity(activityCode,
                  cancelChildTestBean.get("prescribed_id").toString());
            }

          } else if (docPartOfPack) {
            // doctor is part of package, just delete the activity. this can be executed
            // when
            // user changes the consulting doctor from op list.
            new BillActivityChargeDAO(con).deleteActivity(activityCode, activityId);
          }
        }
      } else {
        // update the prescribing doctor ID in the bill_charge
        String prescDrId = (String) bean.get(prescDrFieldName);
        chargeDao.updatePrescribingDoctor(chargeId, prescDrId, true);

        ChargeDTO curCharge = chargeDao.getCharge(chargeId);
        String remarks = "";
        if (clas.getName().equals("diet_prescribed")) {
          remarks = (String) bean.get("special_instructions");
        } else {
          remarks = (String) bean.get("remarks");
        }
        curCharge.setUserRemarks(remarks);
        if (activityCode.equals("SER")) {
          curCharge.setActQuantity((BigDecimal) bean.get("quantity"));
          curCharge.recalcAmount();
        }

        chargeDao.updateChargeAmounts(curCharge);
        chargeListToUpdateTax.add(curCharge);
      }

      // Get the edited or cancelled order charge related bills.
      if (editOrCancelOrderBills != null && chargeId != null) {
        BasicDynaBean chargeBean = billChargeDao.findByKey("charge_id", chargeId);
        String billNo = (chargeBean != null) ? (String) chargeBean.get("bill_no") : null;
        if (billNo != null && !editOrCancelOrderBills.contains(billNo)) {
          editOrCancelOrderBills.add(billNo);
        }
      }
      if (null == bill && !cancel && (clas.getName().equals("equipment_prescribed")
          || clas.getName().equals("bed_operation_schedule"))) {
        return "The "
            + (clas.getName().equals("equipment_prescribed") ? "equipment" : "operation")
            + " cannot be updated as it is posted in a bill which has payment status paid, "
            + "please reopen the bill or mark as unpaid to proceed";
      }
    }
    if (!chargeListToUpdateTax.isEmpty()) {
      if (null != bill) {
        BasicDynaBean billDto = new BillDAO(con).findByKey("bill_no", billNo);
        billChargeTaxDAO.calculateAndUpdateBillChargeTaxes(con, chargeListToUpdateTax,
            billDto);
      }
    }
    return null;
  }

  /**
   * Cancel package ref orders.
   *
   * @param con                    the con
   * @param visitId                the visit id
   * @param prescId                the presc id
   * @param userId                 the user id
   * @param editOrCancelOrderBills the edit or cancel order bills
   * @throws SQLException the SQL exception
   * @throws IOException  Signals that an I/O exception has occurred.
   */
  public void cancelPackageRefOrders(Connection con, String visitId, int prescId,
      String userId, List<String> editOrCancelOrderBills) throws SQLException, IOException {
    List<BasicDynaBean> orders = OrderDAO.getPackageRefOrders(visitId, prescId);
    List<BasicDynaBean> cancelItems = new ArrayList<BasicDynaBean>();
    for (BasicDynaBean order : orders) {
      BasicDynaBean bean =
          getCancelBean((String) order.get("type"), order.get("order_id").toString(), userId);
      cancelItems.add(bean);
    }
    List<BasicDynaBean> opOrders = OrderDAO.getPackageRefOperation(visitId, prescId);
    BasicDynaBean opOrder = (opOrders != null && opOrders.size() > 0) ? opOrders.get(0) : null;
    if (opOrder != null) {
      BasicDynaBean bean = getCancelBean("Operation", opOrder.get("id").toString(), userId);
      cancelItems.add(bean);
    }
    // cancel the sub-orders, but without updating related charge or its hasactivity
    // status
    // since this item has no related charge of its own.
    updateOrders(con, cancelItems, true, false, false, editOrCancelOrderBills, null, null);
  }

  /*
   * Cancel orders referenced in an operation
   */

  /**
   * Cancel operation ref orders.
   *
   * @param con                    the con
   * @param visitId                the visit id
   * @param prescId                the presc id
   * @param cancelCharges          the cancel charges
   * @param userId                 the user id
   * @param editOrCancelOrderBills the edit or cancel order bills
   * @throws SQLException the SQL exception
   * @throws IOException  Signals that an I/O exception has occurred.
   */
  public void cancelOperationRefOrders(Connection con, String visitId, int prescId,
      boolean cancelCharges, String userId, List<String> editOrCancelOrderBills)
      throws SQLException, IOException {

    List<BasicDynaBean> orders = OrderDAO.getOperationRefOrders(visitId, prescId);

    List<BasicDynaBean> cancelItems = new ArrayList<BasicDynaBean>();
    for (BasicDynaBean order : orders) {
      BasicDynaBean bean =
          getCancelBean((String) order.get("type"), order.get("order_id").toString(), userId);
      cancelItems.add(bean);
    }
    // cancel the sub-orders, also canceling charges as required.
    updateOrders(con, cancelItems, true, cancelCharges, false, editOrCancelOrderBills, null,
        null);
  }

  /**
   * Cancel advanced OT surgery.
   *
   * @param con          the con
   * @param prescribedId the prescribed id
   * @throws SQLException the SQL exception
   * @throws IOException  Signals that an I/O exception has occurred.
   */
  public void cancelAdvancedOTSurgery(Connection con, Integer prescribedId)
      throws SQLException, IOException {
    Map<String, Object> columndata = new HashMap<String, Object>();
    columndata.put("operation_status", "X");
    Map<String, Object> identifiers = new HashMap<String, Object>();
    identifiers.put("prescribed_id", prescribedId);
    identifiers.put("oper_priority", "P");
    BasicDynaBean operationDetailsBean =
        new GenericDAO("operation_procedures").findByKey(con, identifiers);
    new OperationDetailsDAO().update(con, columndata, "operation_details_id",
        operationDetailsBean != null
            ? (Integer) operationDetailsBean.get("operation_details_id") : null);
  }

  /**
   * Gets the cancel bean.
   *
   * @param type         the type
   * @param prescribedId the prescribed id
   * @param userName     the user name
   * @return the cancel bean
   * @throws SQLException the SQL exception
   */
  public BasicDynaBean getCancelBean(String type, String prescribedId, String userName)
      throws SQLException {
    BasicDynaBean bean = null;
    int prescIdInt = Integer.parseInt(prescribedId);

    if (type.equals("Laboratory") || type.equals("Radiology")) {
      bean = testDao.getBean();
      bean.set("prescribed_id", prescIdInt);
      bean.set("conducted", "X");
      bean.set("cancelled_by", userName);
      bean.set("cancel_date", DateUtil.getCurrentDate());

    } else if (type.equals("Service")) {
      bean = serviceDao.getBean();
      bean.set("prescription_id", prescIdInt);
      bean.set("conducted", "X");
      bean.set("cancelled_by", userName);
      bean.set("cancel_date", DateUtil.getCurrentDate());

    } else if (type.equals("Equipment")) {
      bean = equipmentDao.getBean();
      bean.set("prescribed_id", prescIdInt);
      bean.set("cancel_status", "C");
      bean.set("user_name", userName);

    } else if (type.equals("Doctor")) {
      bean = doctorConsultationDao.getBean();
      bean.set("consultation_id", prescIdInt);
      bean.set("cancel_status", "C");
      bean.set("username", userName);

    } else if (type.equals("Other Charge")) {
      bean = otherServiceDao.getBean();
      bean.set("prescribed_id", prescIdInt);
      bean.set("cancel_status", "C");
      bean.set("user_name", userName);

    } else if (type.equals("Meal")) {
      bean = dietDao.getBean();
      bean.set("ordered_id", prescIdInt);
      bean.set("status", "X");
      bean.set("status_updated_by", userName);
      bean.set("status_updated_time", DateUtil.getCurrentTimestamp());

    } else if (type.equals("Operation")) {
      bean = operationDao.getBean();
      bean.set("prescribed_id", prescIdInt);
      bean.set("status", "X");
      bean.set("user_name", userName);
    } else if (type.equals("Package")) {
      bean = pkgDao.getBean();
      bean.set("prescription_id", prescIdInt);
      bean.set("status", "X");
      bean.set("user_name", userName);
    } else {
      logger.error("Invalid type: " + type + " for prescribedId=" + prescribedId);
    }

    return bean;
  }

  /**
   * Gets the edits the bean.
   *
   * @param type         the type
   * @param prescribedId the prescribed id
   * @param remarks      the remarks
   * @param presDocId    the pres doc id
   * @param priority     the priority
   * @return the edits the bean
   * @throws SQLException the SQL exception
   */
  public BasicDynaBean getEditBean(String type, String prescribedId, String remarks,
      String presDocId, String priority) throws SQLException {
    BasicDynaBean bean = null;
    int prescIdInt = Integer.parseInt(prescribedId);

    if (type.equals("Laboratory") || type.equals("Radiology")) {
      bean = testDao.getBean();
      bean.set("prescribed_id", prescIdInt);
      bean.set("remarks", remarks);
      bean.set("pres_doctor", presDocId);
      bean.set("priority", priority);

    } else if (type.equals("Service")) {
      bean = serviceDao.getBean();
      bean.set("prescription_id", prescIdInt);
      bean.set("remarks", remarks);
      bean.set("doctor_id", presDocId);

    } else if (type.equals("Equipment")) {
      bean = equipmentDao.getBean();
      bean.set("prescribed_id", prescIdInt);
      bean.set("remarks", remarks);
      bean.set("doctor_id", presDocId);

    } else if (type.equals("Doctor")) {
      bean = doctorConsultationDao.getBean();
      bean.set("consultation_id", prescIdInt);
      bean.set("remarks", remarks);
      bean.set("presc_doctor_id", presDocId);

    } else if (type.equals("Other Charge")) {
      bean = otherServiceDao.getBean();
      bean.set("prescribed_id", prescIdInt);
      bean.set("remarks", remarks);
      bean.set("doctor_id", presDocId);

    } else if (type.equals("Meal")) {
      bean = dietDao.getBean();
      bean.set("ordered_id", prescIdInt);
      bean.set("special_instructions", remarks);
      bean.set("ordered_by", presDocId);

    } else if (type.equals("Operation")) {
      bean = operationDao.getBean();
      bean.set("prescribed_id", prescIdInt);
      bean.set("remarks", remarks);
      bean.set("consultant_doctor", presDocId);

    } else if (type.equals("Package")) {
      bean = pkgDao.getBean();
      bean.set("prescription_id", prescIdInt);
      bean.set("remarks", remarks);
      bean.set("doctor_id", presDocId);

    } else {
      logger.error("Invalid type: " + type + " for prescribedId=" + prescribedId);
    }
    return bean;
  }

  /**
   * Update equipment charges.
   *
   * @param con       the con
   * @param orderBean the order bean
   * @return true, if successful
   * @throws SQLException the SQL exception
   * @throws IOException  Signals that an I/O exception has occurred.
   */
  public boolean updateEquipmentCharges(Connection con, BasicDynaBean orderBean)
      throws SQLException, IOException {

    /*
     * Get the current charge and bill details of the existing order
     */
    BillActivityChargeDAO bacdao = new BillActivityChargeDAO(con);

    int prescId = (Integer) orderBean.get("prescribed_id");

    // get the existing record in the DB, we need some stuff from it. Use a
    // different
    // connection since the given connection would have already modified some fields
    // and updated the record. We need the original finalization status.
    Connection origCon = DataBaseUtil.getReadOnlyConnection();
    BasicDynaBean eqPrescribed = equipmentDao.findByKey(origCon, "prescribed_id", prescId);
    origCon.close();
    String units = (String) eqPrescribed.get("units");
    if (eqPrescribed.get("finalization_status").equals("F") || null == bill) {
      // don't touch an existing finalized equipment. If the bill_charge amount had
      // been
      // changed manually, we should not overwrite it.
      return false;
    }

    /*
     * get the new charges based on new dates.
     */
    Timestamp from = (Timestamp) orderBean.get("used_from");
    Timestamp to = (Timestamp) orderBean.get("used_till");

    BasicDynaBean equipDetails =
        new EquipmentChargeDAO().getEquipmentCharge((String) eqPrescribed.get("eq_id"),
            (String) bill.get("bed_type"), (String) bill.get("bill_rate_plan_id"));

    List<ChargeDTO> newCharges = getEquipmentCharges(equipDetails, from, to, units, false,
        BigDecimal.ZERO, isInsurance, planIds, visitType, patientId, null);

    ChargeDTO curCharge = bacdao.getCharge("EQU", prescId);
    ChargeDTO newMainCharge = newCharges.get(0);
    copyChargeAmounts(newMainCharge, curCharge, true);
    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
    String usage = sdf.format(from) + " to " + sdf.format(to);
    curCharge.setActRemarks(usage);
    ChargeDAO cdao = new ChargeDAO(con);
    cdao.updateCharge(curCharge.getChargeId(), curCharge);

    /*
     * The referenced charge (tax) may or may not exist. If it exists, update, else insert a new
     * one. Other way round, cancel the existing charge.
     */
    List<ChargeDTO> curChargeRefs = cdao.getChargeReferences(curCharge.getChargeId());
    ChargeDTO newTaxCharge = newCharges.size() > 1 ? newCharges.get(1) : null;
    ChargeDTO curTaxCharge = curChargeRefs.size() > 0 ? curChargeRefs.get(0) : null;
    if (newTaxCharge != null && curTaxCharge != null) {
      copyChargeAmounts(newTaxCharge, curTaxCharge, true);
      cdao.updateCharge(curTaxCharge.getChargeId(), curTaxCharge);

    } else if (newTaxCharge != null) {
      newTaxCharge.setOrderAttributes(cdao.getNextChargeId(), billNo, userName, "", "",
          curCharge.getPostedDate());

      newTaxCharge.setChargeRef(curCharge.getChargeId());
      newTaxCharge.setHasActivity(true);
      cdao.insertCharge(newTaxCharge);

    } else if (curTaxCharge != null) {
      cdao.cancelCharge(con, curTaxCharge.getChargeId(), false);
    } // else nothing to do, both are not there.
    return true;
  }

  /**
   * Update theatre charges.
   *
   * @param con       the con
   * @param orderBean the order bean
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public boolean updateTheatreCharges(Connection con, BasicDynaBean orderBean)
      throws SQLException {

    /*
     * Get the current charge and bill details of the existing order
     */
    ChargeDAO cdao = new ChargeDAO(con);
    BillActivityChargeDAO bacdao = new BillActivityChargeDAO(con);

    int prescId = (Integer) orderBean.get("prescribed_id");

    // get the existing record in the DB, we need some stuff from it.
    // (Use new connection, because we want the original finalization status)
    Connection origCon = DataBaseUtil.getReadOnlyConnection();
    BasicDynaBean opPrescribed = operationDao.findByKey(origCon, "prescribed_id", prescId);
    origCon.close();

    if (opPrescribed.get("finalization_status").equals("F") || bill == null) {
      // already finalized, don't update the charges.
      return false;
    }

    String units = ((String) opPrescribed.get("hrly")).equals("checked") ? "H" : "D";

    ChargeDTO curCharge = bacdao.getCharge("OPE", prescId);
    if (curCharge == null) {
      // non-chargeable
      return false;
    }

    String billNo = curCharge.getBillNo();
    BasicDynaBean bill = BillDAO.getBillBean(con, billNo);

    /*
     * get the new charges based on new dates.
     */
    Timestamp from = (Timestamp) orderBean.get("start_datetime");
    Timestamp to = (Timestamp) orderBean.get("end_datetime");

    BasicDynaBean operBean = operationMasterDao.getOperationChargeBean(
        (String) opPrescribed.get("operation_name"), (String) bill.get("bed_type"),
        (String) bill.get("bill_rate_plan_id"));

    BasicDynaBean theatre = theatreMasterDao.getTheatreChargeDetails(
        (String) opPrescribed.get("theatre_name"), (String) bill.get("bed_type"),
        (String) bill.get("bill_rate_plan_id"));

    if (theatre != null) {
      List<ChargeDTO> newCharges =
          getOperationCharges((String) opPrescribed.get("operation_name"), operBean, theatre,
              null, null, from, to, units, isInsurance, planIds,
              (String) orderBean.get("finalization_status"), visitType, null, patientId, null);

      ChargeDTO newMainCharge = newCharges.get(0);
      copyChargeAmounts(newMainCharge, curCharge, true);
      SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
      String usage = sdf.format(from) + " to " + sdf.format(to);
      curCharge.setActRemarks(usage);
      cdao.updateCharge(curCharge.getChargeId(), curCharge);
    }

    return true;
  }

  /**
   * Update anaesthesia type charges.
   *
   * @param con       the con
   * @param orderBean the order bean
   * @param map       the map
   * @return true, if successful
   * @throws SQLException the SQL exception
   * @throws IOException  Signals that an I/O exception has occurred.
   */
  public boolean updateAnaesthesiaTypeCharges(Connection con, BasicDynaBean orderBean,
      Map<String, Object> map) throws SQLException, IOException {
    /*
     * Get the current charge and bill details of the existing order
     */
    ChargeDAO cdao = new ChargeDAO(con);
    int prescId = (Integer) orderBean.get("prescribed_id");
    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
    // get the existing record in the DB, we need some stuff from it.
    // (Use new connection, because we want the original finalization status)
    Connection origCon = DataBaseUtil.getReadOnlyConnection();
    BasicDynaBean opPrescribed = operationDao.findByKey(origCon, "prescribed_id", prescId);
    origCon.close();

    if (opPrescribed.get("finalization_status").equals("F") || bill == null) {
      // already finalized, don't update the charges.
      return false;
    }

    String units = ((String) opPrescribed.get("hrly")).equals("checked") ? "H" : "D";

    BasicDynaBean operBean = operationMasterDao.getOperationChargeBean(
        (String) opPrescribed.get("operation_name"), (String) bill.get("bed_type"),
        (String) bill.get("bill_rate_plan_id"));

    List<ChargeDTO> newCharges = new ArrayList<ChargeDTO>();

    if (map != null) {
      List opEditAnaesStartDateTime = (List) map.get("op_edit_anaes_start_date_times");
      List opEditAnaesEndDateTime = (List) map.get("op_edit_anaes_end_date_times");
      List opEditAnaesChargeIds = (List) map.get("op_edit_anaes_charge_ids");
      List opEditAnaesthesiaTypeIds = (List) map.get("op_edit_anaesthesia_type_ids");
      BasicDynaBean anaesTypeChargeBean = null;

      if (opEditAnaesStartDateTime != null && opEditAnaesStartDateTime.size() > 0) {
        for (int i = 0; i < opEditAnaesStartDateTime.size(); i++) {
          anaesTypeChargeBean = new AnaesthesiaTypeChargesDAO().getAnasthesiaTypeCharge(
              (String) opEditAnaesthesiaTypeIds.get(i), (String) bill.get("bed_type"),
              (String) bill.get("bill_rate_plan_id"));

          newCharges
              .addAll(getAnaesthesiaTypeCharges((String) opPrescribed.get("operation_name"),
                  operBean, (Timestamp) opEditAnaesStartDateTime.get(i),
                  (Timestamp) opEditAnaesEndDateTime.get(i), units, isInsurance, planIds,
                  (String) orderBean.get("finalization_status"), visitType,
                  anaesTypeChargeBean, patientId, null));
        }
      }
      int chIndex = 0;
      for (int ch = 0; ch < newCharges.size(); ch++) {
        if (newCharges.get(ch).getChargeHead().equals("ANATOPE")) {
          String usage = sdf.format((Timestamp) opEditAnaesStartDateTime.get(chIndex)) + " to "
              + sdf.format((Timestamp) opEditAnaesEndDateTime.get(chIndex));
          if (opPrescribed.get("finalization_status").equals("F")) {
            newCharges.get(ch).setActRemarks(usage);
          }
          newCharges.get(ch).setChargeId((String) opEditAnaesChargeIds.get(chIndex));
          cdao.updateAnaethesiaCharges((String) opEditAnaesChargeIds.get(chIndex),
              newCharges.get(ch));
        }
        chIndex++;
      }
      BasicDynaBean billDto = new BillDAO(con).findByKey("bill_no", billNo);
      billChargeTaxDAO.calculateAndUpdateBillChargeTaxes(con, newCharges, billDto);
    }
    return true;
  }

  /**
   * Update anaesthesia type details.
   *
   * @param con       the con
   * @param orderBean the order bean
   * @param map       the map
   * @return true, if successful
   * @throws SQLException the SQL exception
   * @throws IOException  Signals that an I/O exception has occurred.
   */
  public boolean updateAnaesthesiaTypeDetails(Connection con, BasicDynaBean orderBean,
      Map<String, Object> map) throws SQLException, IOException {

    if (map != null) {
      List opEditAnaesStartDateTime = (List) map.get("op_edit_anaes_start_date_times");
      List opEditAnaesEndDateTime = (List) map.get("op_edit_anaes_end_date_times");
      List opEditAnaesChargeIds = (List) map.get("op_edit_anaes_charge_ids");
      List opEditAnaesthesiaTypeIds = (List) map.get("op_edit_anaesthesia_type_ids");
      List opEditAnaesSurgDetailIds = (List) map.get("op_edit_surg_anaes_details_ids");
      List opAdvEditAnaesOperDetailIds = (List) map.get("op_adv_edit_oper_anaes_details_ids");
      Map surgAnaesDetailsMap = null;
      Map advOperAnaesDetailsMap = null;
      GenericDAO surgAnaesDetDao = new GenericDAO("surgery_anesthesia_details");
      GenericDAO advOperationAnaesDetDao = new GenericDAO("operation_anaesthesia_details");

      if (opEditAnaesthesiaTypeIds != null && opEditAnaesthesiaTypeIds.size() > 0) {
        for (int i = 0; i < opEditAnaesthesiaTypeIds.size(); i++) {
          surgAnaesDetailsMap = new HashMap();
          surgAnaesDetailsMap.put("anaes_start_datetime",
              (Timestamp) opEditAnaesStartDateTime.get(i));
          surgAnaesDetailsMap.put("anaes_end_datetime",
              (Timestamp) opEditAnaesEndDateTime.get(i));
          surgAnaesDetDao.update(con, surgAnaesDetailsMap, "surgery_anesthesia_details_id",
              (Integer) opEditAnaesSurgDetailIds.get(i));
        }

        for (int i = 0; i < opAdvEditAnaesOperDetailIds.size(); i++) {
          advOperAnaesDetailsMap = new HashMap();
          surgAnaesDetailsMap.put("anaes_start_datetime",
              (Timestamp) opEditAnaesStartDateTime.get(i));
          surgAnaesDetailsMap.put("anaes_end_datetime",
              (Timestamp) opEditAnaesEndDateTime.get(i));
          advOperationAnaesDetDao.update(con, surgAnaesDetailsMap, "operation_anae_detail_id",
              (Integer) opAdvEditAnaesOperDetailIds.get(i));
        }
      }
    }
    return true;
  }

  /**
   * Copy charge amounts.
   *
   * @param from       the from
   * @param to         the to
   * @param setModTime the set mod time
   * @throws SQLException the SQL exception
   */
  public static void copyChargeAmounts(ChargeDTO from, ChargeDTO to, boolean setModTime)
      throws SQLException {
    to.copyChargeAmountsFrom(from, setModTime);
  }

  /**
   * Package does not required a perticuler doctor but doctor consultation. So passing a dummy
   * chargedto for packages
   *
   * @param chargeType      the charge type
   * @param isInsurance     the is insurance
   * @param planIds         the plan ids
   * @param visitType       the visit type
   * @param patientId       the patient id
   * @param firstOfCategory the first of category
   * @return the package doctor charges
   * @throws Exception the exception
   */
  public static List<ChargeDTO> getPackageDoctorCharges(String chargeType, boolean isInsurance,
      int[] planIds, String visitType, String patientId, Boolean firstOfCategory)
      throws Exception {

    BasicDynaBean consultationType = getConsultationTypeBean(Integer.parseInt(chargeType));
    int insuranceCategoryId =
        getIntegerValueFromBean(consultationType, "insurance_category_id");

    ChargeDTO chrgdto = new ChargeDTO("DOC", (String) consultationType.get("charge_head"),
        BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO, "", "Doctor", "Doctor", "Doctor",
        isInsurance, 0, insuranceCategoryId, visitType, patientId, firstOfCategory);

    chrgdto.setInsuranceAmt(planIds, visitType, chrgdto.getFirstOfCategory());
    if (consultationType.get("billing_group_id") != null) {
      chrgdto.setBillingGroupId((Integer) consultationType.get("billing_group_id"));
    }
    List<ChargeDTO> list = new ArrayList<ChargeDTO>();
    list.add(chrgdto);
    return list;
  }

  /**
   * Gets the consultation type bean.
   *
   * @param consultationId the consultation id
   * @return the consultation type bean
   * @throws SQLException the SQL exception
   */
  public static BasicDynaBean getConsultationTypeBean(int consultationId) throws SQLException {
    return new GenericDAO("consultation_types").findByKey("consultation_type_id",
        consultationId);
  }

  /**
   * Gets the consultation charge.
   *
   * @param consultationId the consultation id
   * @param bedType        the bed type
   * @param ratePlan       the rate plan
   * @return the consultation charge
   * @throws SQLException the SQL exception
   */
  public static BasicDynaBean getConsultationCharge(int consultationId, String bedType,
      String ratePlan) throws SQLException {
    BasicDynaBean conschargebean =
        new OrderDAO().getConsultationCharges(consultationId, bedType, ratePlan);
    if (conschargebean == null) {
      conschargebean =
          new OrderDAO().getConsultationCharges(consultationId, "GENERAL", "ORG0001");
    }
    return conschargebean;
  }

  /**
   * Gets the thresholds.
   *
   * @param prefs      the prefs
   * @param daysHours  the days hours
   * @param isFirstBed the is first bed
   * @param isIcu      the is ICU
   * @return the thresholds
   */
  public static String[] getThresholds(BasicDynaBean prefs, int[] daysHours,
      boolean isFirstBed, String isIcu) {
    String hrlyThreshHold = null;
    String halfDayThreshold = null;
    String dailyThreshold = null;
    if (isIcu.equals("Y")) {
      // use days, hours and get number of days and hours to charge for the bed.
      String prefix = isFirstBed ? "icu_" : "icu_bedshift_";
      hrlyThreshHold = (String) prefs.get(prefix + "hrly_charge_threshold");
      halfDayThreshold = (String) prefs.get(prefix + "halfday_charge_threshold");
      dailyThreshold = (String) prefs.get(prefix + "fullday_charge_threshold");
    } else {
      // use days, hours and get number of days and hours to charge for the bed.
      String prefix = isFirstBed ? "" : "bedshift_";
      hrlyThreshHold = (String) prefs.get(prefix + "hrly_charge_threshold");
      halfDayThreshold = (String) prefs.get(prefix + "halfday_charge_threshold");
      dailyThreshold = (String) prefs.get(prefix + "fullday_charge_threshold");
    }
    return new String[] {hrlyThreshHold, halfDayThreshold, dailyThreshold};
  }

  /**
   * Recalculate bed charges.
   *
   * @param con       the con
   * @param patientId the patient id
   * @return the string
   * @throws SQLException   the SQL exception
   * @throws IOException    Signals that an I/O exception has occurred.
   * @throws ParseException the parse exception
   */
  public String recalculateBedCharges(Connection con, String patientId)
      throws SQLException, IOException, ParseException {

    BasicDynaBean patientDetails = VisitDetailsDAO.getPatientVisitDetailsBean(con, patientId);

    List<BasicDynaBean> mainBeds = IPBedDAO.getVistMainBeds(con, patientId);
    BasicDynaBean existingBed = IPBedDAO.getActiveBedDetails(con, patientId);
    String currentBedType = null;
    if (null != existingBed) {
      currentBedType = (String) existingBed.get("charged_bed_type");
    }

    if (existingBed == null) {
      // possible in case in a transaction cancelling current bed and updating charges
      existingBed = IPBedDAO.getAdmissionDetails(con, patientId);
    }
    // HMS-12881 as part of ahll optimization
    // BasicDynaBean pd = VisitDetailsDAO.getPatientVisitDetailsBean(con,
    // patientId);

    Map<String, BasicDynaBean> icuChargesMap = ConversionUtils.listBeanToMapBean(bedMasterDao
        .getIcuBedChargesList((String) patientDetails.get("bill_bed_type"), this.ratePlanId),
        "intensive_bed_type");
    Map<String, BasicDynaBean> normalBedChargesMap = ConversionUtils
        .listBeanToMapBean(bedMasterDao.getNormalBedChargesList(this.ratePlanId), "bed_type");

    for (BasicDynaBean mainBed : mainBeds) {
      int mainAdmitId = (Integer) mainBed.get("admit_id");
      List<BasicDynaBean> threadBeds = IPBedDAO.getThreadBeds(con, patientId, mainAdmitId);
      Timestamp[] thrStartEndDates = getThreadStartEnd(threadBeds, false);
      logger.debug("Thread start/end (" + mainAdmitId + "): " + thrStartEndDates[0] + " - "
          + thrStartEndDates[1]);

      Map<String, BasicDynaBean> bedCharges =
          mainBed.get("is_icu").equals("Y") ? icuChargesMap : normalBedChargesMap;

      List<ChargeDTO> newCharges =
          getThreadCharges(con, mainAdmitId, existingBed, threadBeds, bedCharges);

      DiscountPlanBO discBo = new DiscountPlanBO();
      // say some details abt discount plan
      discBo.setDiscountPlanDetails(bill.get("discount_category_id") != null
          ? (Integer) bill.get("discount_category_id") : 0);

      BigDecimal bedAmount = BigDecimal.ZERO;
      BigDecimal totalAmount = BigDecimal.ZERO;
      for (ChargeDTO chargeDto : newCharges) {
        chargeDto.setBedType(currentBedType);
        chargeDto.setPrescribingDrId((String) patientDetails.get("doctor"));// admitting doctor
        chargeDto.setActivityConducted(mainBed.get("bed_state").equals("F") ? "Y" : "N");
        chargeDto.setUsername(this.userName);
        if (chargeDto.getChargeHead().equals(ChargeDTO.CH_DUTY_DOCTOR)
            || chargeDto.getChargeHead().equals(ChargeDTO.CH_DUTY_DOCTOR_ICU)) {
          chargeDto.setPayeeDoctorId((String) mainBed.get("duty_doctor_id"));
        }

        String chargeHead = chargeDto.getChargeHead();
        chargeHeadsDAO.clearCache();
        BasicDynaBean chargeHeadBean = chargeHeadsDAO.findByKey("chargehead_id", chargeHead);
        chargeDto.setAllowRateIncrease((Boolean) chargeHeadBean.get("allow_rate_increase"));
        chargeDto.setAllowRateDecrease((Boolean) chargeHeadBean.get("allow_rate_decrease"));
        
        chargeDto.setBillNo(
            (null != bill && bill.get("bill_no") != null) ? (String) bill.get("bill_no") : null);

        if (chargeDto.isAllowDiscount()) {
          discBo.applyDiscountRule(con, chargeDto);
        }
        chargeDto.setInsuranceAmt(planIds, visitType, chargeDto.getFirstOfCategory());
        chargeDto.setVisitId((null != bill && bill.get("visit_id") != null)
            ? (String) bill.get("visit_id") : null);
      }
      BillingHelper billingHelper = new BillingHelper();
      billingHelper.saveBillChargeBillingGroup(newCharges);

      String chargeId = BillActivityChargeDAO.getActiveChargeId(con, "BED", "" + mainAdmitId);
      if (chargeId == null) {
        // newCharges can be 0 if the period is very small.
        if (newCharges.size() > 0) {
          // we will never have two beds being inserted the first time, so this is OK.
          // Otherwise, this is the wrong thing to do, as we will need two main charges.
          insertOrderCharges(con, newCharges, "BED", mainAdmitId, null,
              newCharges.get(0).getPrescribingDrId(), DateUtil.getCurrentTimestamp(), "N",
              null, null, null, null);
        }
      } else {
        if (new ChargeDAO(con).getBillStatus(chargeId).equals(Bill.BILL_STATUS_OPEN)) {
          List<ChargeDTO> originalCharges = new ChargeDAO(con).getChargeAndRefs(chargeId);
          recalculateChargeAmounts(con, originalCharges, newCharges);
          // billChargeTaxDAO.calculateAndUpdateBillChargeTaxes(con,newCharges);
        }
      }
    }

    return null;
  }

  /**
   * Gets the thread charges.
   *
   * @param con           the con
   * @param admitId       the admit id
   * @param existingBed   the existing bed
   * @param threadBeds    the thread beds
   * @param bedChargesMap the bed charges map
   * @return the thread charges
   * @throws SQLException   the SQL exception
   * @throws ParseException the parse exception
   */
  public List<ChargeDTO> getThreadCharges(Connection con, int admitId,
      BasicDynaBean existingBed, List<BasicDynaBean> threadBeds,
      Map<String, BasicDynaBean> bedChargesMap) throws SQLException, ParseException {

    String dayCareStatus = (String) existingBed.get("daycare_status");
    boolean isDayCare = dayCareStatus.equals("Y");
    List<ChargeDTO> threadCharges = new ArrayList<ChargeDTO>();

    BasicDynaBean prefs = new IPPreferencesDAO().getPreferences();
    BasicDynaBean chargedBed = null;
    boolean cutOff = prefs.get("cut_off_required").equals("Y") && !isDayCare;

    Timestamp[] threadStartEnd = getThreadStartEnd(threadBeds, false);

    List<SlotDesc> slotDetailsList = getThreadSlots(threadStartEnd[0], threadStartEnd[1],
        (Integer) prefs.get("slab1_duration"), (Integer) prefs.get("next_slabs_duration"),
        isDayCare, cutOff);

    SlotDesc prvsSlot = null;
    BasicDynaBean prvsBed = null;
    BigDecimal[] prvsDaysHours = null;
    List<ChargeDTO> prvsCharges = null;
    int firstBedIdx = 0;
    int numBeds = threadBeds.size();

    for (SlotDesc slot : slotDetailsList) {
      Timestamp from = slot.getStartTime();
      Timestamp to = slot.getEndTime();
      // find the first bed in the slot: skip all beds whose end time is before slot
      // start
      // TODO: sync start/end times of beds in thread for beds cancelled in between.
      // Or,
      // break the thread if a bed gets cancelled in between.
      while (firstBedIdx < numBeds) {
        if (((Timestamp) threadBeds.get(firstBedIdx).get("end_date")).compareTo(from) < 0) {
          firstBedIdx++;
        } else {
          break;
        }
      }
      if (firstBedIdx == numBeds) {
        logger.error("No beds in slot: " + from + " for admit_id" + admitId);
        return null;
      }

      // iterate through all beds in slot and find the max bed type
      int bedIdx = firstBedIdx;
      BasicDynaBean maxBed = threadBeds.get(bedIdx);
      BasicDynaBean maxBedRates = bedChargesMap.get(maxBed.get("charged_bed_type"));

      while (bedIdx < numBeds) {
        BasicDynaBean curBed = threadBeds.get(bedIdx);
        if (((Timestamp) curBed.get("start_date")).compareTo(slot.getEndTime()) > 0) {
          break;
        }
        BasicDynaBean curBedRates = bedChargesMap.get(curBed.get("charged_bed_type"));
        if (((BigDecimal) maxBedRates.get("bed_charge"))
            .compareTo((BigDecimal) curBedRates.get("bed_charge")) < 0) {
          maxBed = curBed;
          maxBedRates = curBedRates;
        }
        bedIdx++;
      }

      // now we have the max bed in our slot. Get the charges for this.
      BigDecimal[] daysHours = new BigDecimal[2];
      List<ChargeDTO> slotCharges =
          getSlotCharges(con, prefs, maxBed, slot, dayCareStatus, maxBedRates, daysHours);

      // see if we can merge it to previous set of charges
      if (prvsCharges != null && prvsCharges.size() > 0 && (prvsBed != null)
          && prvsBed.get("bed_id").equals(maxBed.get("bed_id"))
          && prvsDaysHours[1].equals(BigDecimal.ZERO) // previous slot should not have hourly
          && !daysHours[0].equals(BigDecimal.ZERO)) { // this days != 0

        prvsSlot.setEndTime(to); // merge
        List<ChargeDTO> newCharges = mergeBedCharges(prvsCharges, slotCharges,
            prvsSlot.getStartTime(), prvsSlot.getEndTime());
        threadCharges.addAll(newCharges);
      } else {
        prvsCharges = slotCharges;
        prvsSlot = slot;
        threadCharges.addAll(slotCharges);
      }

      prvsBed = maxBed;
      prvsDaysHours = daysHours;
      firstBedIdx = bedIdx - 1; // next slot can at most have our last bed as its first bed
    }

    if (threadCharges.size() > 0) {
      threadCharges.get(0).setActivityDetails("BED", admitId, "N", null);
    }

    return threadCharges;
  }

  /**
   * Gets the slot charges.
   *
   * @param con           the con
   * @param prefs         the prefs
   * @param ipBed         the ip bed
   * @param slot          the slot
   * @param dayCareStatus the day care status
   * @param bedCharges    the bed charges
   * @param retDaysHours  the ret days hours
   * @return the slot charges
   * @throws SQLException   the SQL exception
   * @throws ParseException the parse exception
   */
  public List<ChargeDTO> getSlotCharges(Connection con, BasicDynaBean prefs,
      BasicDynaBean ipBed, SlotDesc slot, String dayCareStatus, BasicDynaBean bedCharges,
      BigDecimal[] retDaysHours) throws SQLException, ParseException {

    boolean onlyMainCharges = false;
    if ((Boolean) ipBed.get("is_bystander") || ((Boolean) ipBed.get("is_retained")
        && prefs.get("retain_bed_charges").equals("B"))) {
      onlyMainCharges = true;
    }

    String cg = ipBed.get("is_icu").equals("Y") ? "ICU" : "BED";
    boolean isBystander = (Boolean) ipBed.get("is_bystander");
    Timestamp from = slot.getStartTime();
    Timestamp to = slot.getEndTime();

    List<ChargeDTO> charges = getBedCharges(cg, bedCharges, from, to, null, isBystander,
        dayCareStatus, onlyMainCharges, this.isInsurance, this.planIds, this.visitType,
        this.patientId, slot.isFirstSlab(), (String) ipBed.get("bed_state"), null,
        retDaysHours, this.bill.get("discount_category_id"), this.bill.get("bill_no"), con);

    DateUtil dateUtil = new DateUtil();
    String remarks = dateUtil.getTimeStampFormatter().format(from) + " to "
        + dateUtil.getTimeStampFormatter().format(to);
    logger.debug("Bed remarks: " + remarks);

    if (charges.size() == 0) {
      return charges;
    }

    for (ChargeDTO c : charges) {
      logger.debug("Bed Charge head: " + c.getChargeHead() + ", qty: " + c.getActQuantity()
          + ", charge: " + c.getAmount());
      c.setActDescriptionId("" + (Integer) ipBed.get("bed_id"));
      c.setActDescription((String) ipBed.get("bed_name"));
      c.setActRemarks(remarks);
      c.setActivityConducted(ipBed.get("bed_state").equals("F") ? "Y" : "N");
      if (c.getChargeHead().equals(ChargeDTO.CH_DUTY_DOCTOR)
          || c.getChargeHead().equals(ChargeDTO.CH_DUTY_DOCTOR_ICU)) {
        c.setPayeeDoctorId((String) ipBed.get("duty_doctor_id"));
      }
      if (bedCharges.get("billing_group_id") != null) {
        c.setBillingGroupId((Integer) bedCharges.get("billing_group_id"));
      }
    }

    return charges;
  }

  /**
   * Gets the thread slots.
   *
   * @param actStart the act start
   * @param actEnd   the act end
   * @param mainSlab the main slab
   * @param subSlab  the sub slab
   * @param dayCare  the day care
   * @param cutOff   the cut off
   * @return the thread slots
   */
  public List<SlotDesc> getThreadSlots(Timestamp actStart, Timestamp actEnd, Integer mainSlab,
      Integer subSlab, boolean dayCare, boolean cutOff) {

    List<SlotDesc> slotDetailsList = new ArrayList<SlotDesc>();
    int slotedHrs = mainSlab.intValue();
    Timestamp expectedDate = null;

    if (dayCare) {
      slotDetailsList.add(new SlotDesc(actStart, actEnd, false));

    } else if (cutOff) {
      // start with actStart and unknown end.
      SlotDesc slot = new SlotDesc(actStart, null, true);
      // Get 00:00 of actStart date, ie, beginning of the day.
      Calendar cal = Calendar.getInstance();
      cal.setTime(actStart);
      DateUtil.dateTrunc(cal); // truncate the min/hour etc. part.

      int ival = 0;
      while (DateUtil.dateDiff(cal, actEnd) < 0) {
        // till we reach the same date as the actEnd, keep creating new slots.
        cal.add(Calendar.DATE, 1);
        slot.setEndTime(new java.sql.Timestamp(cal.getTime().getTime()));
        slotDetailsList.add(slot);
        logger.debug("Cutoff Slot: " + slot.getStartTime() + "-" + slot.getEndTime());
        // create a new slot.
        slot = new SlotDesc(slot.getEndTime(), null, false);
        if (ival++ > 2000) {
          break;
        }
      }
      // set the last slot's end time to actEnd and add that also.
      slot.setEndTime(actEnd);
      slotDetailsList.add(slot);
      logger.debug("Final Cutoff Slot: " + slot.getStartTime() + "-" + slot.getEndTime());

    } else {
      SlotDesc slot = new SlotDesc(actStart, null, true);
      int slabDuration = mainSlab;

      int intVal = 0;
      while (DateUtil.addHours(slot.getStartTime(), slabDuration).compareTo(actEnd) < 0) {
        Timestamp slabEnd = DateUtil.addHours(slot.getStartTime(), slabDuration);
        slabDuration = subSlab;
        slot.setEndTime(slabEnd);
        slotDetailsList.add(slot);
        logger.debug("Slot: " + slot.getStartTime() + "-" + slot.getEndTime());
        // create a new slot.
        slot = new SlotDesc(slot.getEndTime(), null, false);
        if (intVal++ > 2000) {
          break;
        }
      }
      // set the last slot's end time to actEnd and add that also.
      slot.setEndTime(actEnd);
      slotDetailsList.add(slot);
      logger.debug("Final Slot: " + slot.getStartTime() + "-" + slot.getEndTime());
    }

    return slotDetailsList;
  }

  /**
   * Sets the bill paid status.
   *
   * @param con the con
   * @return the string
   * @throws SQLException the SQL exception
   */
  public String setBillPaidStatus(Connection con) throws SQLException {
    BasicDynaBean billAmountsBean = new BillDAO(con).getBillAmounts(this.billNo);

    BigDecimal totalAmt = (BigDecimal) billAmountsBean.get("total_amount");
    BigDecimal totalInsAmt = (BigDecimal) billAmountsBean.get("total_claim");
    BigDecimal depSetoff = (BigDecimal) billAmountsBean.get("deposit_set_off");
    BigDecimal recpt = (BigDecimal) billAmountsBean.get("total_receipts");
    BigDecimal insRecpt = (BigDecimal) billAmountsBean.get("primary_total_sponsor_receipts");

    BigDecimal totAmtDue =
        totalAmt.subtract(totalInsAmt).subtract(depSetoff).subtract((recpt.add(insRecpt)));

    if (totAmtDue.compareTo(BigDecimal.ZERO) == 0) {
      new BillDAO(con).updatePaymentStatus(this.billNo, "P", "Y");
    }

    return null;
  }

  /**
   * Gets the thread start end.
   *
   * @param threadBeds the thread beds
   * @param cutOff     the cut off
   * @return the thread start end
   * @throws SQLException   the SQL exception
   * @throws ParseException the parse exception
   */
  public Timestamp[] getThreadStartEnd(List<BasicDynaBean> threadBeds, boolean cutOff)
      throws SQLException, ParseException {

    Timestamp startDate = null;
    Timestamp endDate = null;
    int startBed = 0;
    int endBed = threadBeds.size() - 1;

    if (threadBeds.size() > 0) {
      startDate = (Timestamp) threadBeds.get(startBed).get("start_date");
      endDate = (Timestamp) threadBeds.get(endBed).get("end_date");
    }

    if (cutOff) {
      startDate = DateUtil
          .parseTimestamp(DataBaseUtil.dateFormatter.format(startDate).toString(), "00:00");
    }

    return new Timestamp[] {startDate, endDate};
  }

  /**
   * Merge bed charges.
   *
   * @param toCharges   the to charges
   * @param fromCharges the from charges
   * @param fromDate    the from date
   * @param toDate      the to date
   * @return the list
   * @throws SQLException the SQL exception
   */
  public List<ChargeDTO> mergeBedCharges(List<ChargeDTO> toCharges,
      List<ChargeDTO> fromCharges, Timestamp fromDate, Timestamp toDate) throws SQLException {

    if (toCharges.size() == 0) {
      return fromCharges; // all are to be added
    }

    ArrayList<ChargeDTO> newCharges = new ArrayList<ChargeDTO>();

    if (fromCharges.size() == 0) {
      return newCharges; // nothing to be added
    }

    logger.debug("Merging: " + fromCharges.size() + " into " + toCharges.size() + " ["
        + fromDate + "-" + toDate + "]");

    logger.debug("Merging: " + fromCharges.get(0).getActivityId() + " into "
        + toCharges.get(0).getActivityId() + " [" + fromDate + "-" + toDate + "]");

    for (ChargeDTO mergeFrom : fromCharges) {
      // find the corresponding charge head in the list of toCharges
      ChargeDTO mergeTo = null;
      for (ChargeDTO to : toCharges) {
        if (to.getChargeHead().equals(mergeFrom.getChargeHead())
            && to.getActUnit().equals(mergeFrom.getActUnit())) {
          mergeTo = to;
          break;
        }
      }

      if (mergeTo != null) {
        mergeCharges(mergeTo, mergeFrom, fromDate, toDate);
      } else {
        // could not find a charge to merge into, need to add it to the original list.
        newCharges.add(mergeFrom);
      }
    }
    return newCharges;
  }

  /**
   * Merge charges.
   *
   * @param to       the to
   * @param from     the from
   * @param fromDate the from date
   * @param toDate   the to date
   * @throws SQLException the SQL exception
   */
  public void mergeCharges(ChargeDTO to, ChargeDTO from, Timestamp fromDate, Timestamp toDate)
      throws SQLException {

    to.setActQuantity(to.getChargeHead().equals(ChargeDTO.CH_LUXURY_TAX) ? BigDecimal.ONE
        : to.getActQuantity().add(from.getActQuantity()));

    to.setActRemarks(
        DateUtil.formatTimestamp(fromDate) + " to " + DateUtil.formatTimestamp(toDate));

    if (to.getChargeHead().equals(ChargeDTO.CH_LUXURY_TAX)) {
      to.setAmount(to.getAmount().add(from.getAmount()));
      to.setActRate(to.getAmount());
    } else {
      to.setAmount(to.getAmount().add(from.getAmount()));
    }

    to.setDiscount(to.getDiscount().add(from.getDiscount()));
    // TODO: can we just add the insurance claim amounts?
    if (isInsurance) {
      BasicDynaBean chrgbean = chargeHeadsDAO.findByKey("chargehead_id", to.getChargeHead());

      if (chrgbean.get("insurance_payable") != null
          && ((String) chrgbean.get("insurance_payable")).equals("Y")) {
        to.setInsuranceAmtForPlan(planId, visitType, to.getFirstOfCategory());
      } else {
        to.setInsuranceClaimAmount(BigDecimal.ZERO);
      }
    }
  }

  /**
   * Sets the remarks.
   *
   * @param con     the con
   * @param charges the charges
   * @throws SQLException   the SQL exception
   * @throws ParseException the parse exception
   */
  public void setRemarks(Connection con, List<ChargeDTO> charges)
      throws SQLException, ParseException {

    boolean activityCharge = true;
    String activityRemarks = null;
    for (ChargeDTO mainCharge : charges) {
      activityCharge = mainCharge.getHasActivity();
      if ((activityCharge)) {
        BasicDynaBean activityBean =
            ipBedDao.findByKey(con, "admit_id", mainCharge.getActivityId());

        if (activityBean == null) {
          continue;
        }
        activityRemarks = DateUtil.formatTimestamp((Timestamp) activityBean.get("start_date"))
            + " to " + DateUtil.formatTimestamp((Timestamp) activityBean.get("end_date"));
        mainCharge.setActRemarks(activityRemarks);
      } else {
        // refering charge shd have remarks as main charge
        mainCharge.setActRemarks(activityRemarks);
      }
    }

  }

  /**
   * The Class SlotDesc.
   */
  public class SlotDesc {

    /** The start time. */
    private Timestamp startTime;

    /** The end time. */
    private Timestamp endTime;

    /** The is first slab. */
    private boolean isFirstSlab;

    /**
     * Instantiates a new slot desc.
     *
     * @param startTime   the start time
     * @param endTime     the end time
     * @param isFirstSlab the is first slab
     */
    public SlotDesc(Timestamp startTime, Timestamp endTime, boolean isFirstSlab) {
      this.startTime = startTime;
      this.endTime = endTime;
      this.isFirstSlab = isFirstSlab;
    }

    /**
     * Gets the end time.
     *
     * @return the end time
     */
    public Timestamp getEndTime() {
      return endTime;
    }

    /**
     * Sets the end time.
     *
     * @param endTime the new end time
     */
    public void setEndTime(Timestamp endTime) {
      this.endTime = endTime;
    }

    /**
     * Gets the start time.
     *
     * @return the start time
     */
    public Timestamp getStartTime() {
      return startTime;
    }

    /**
     * Sets the start time.
     *
     * @param startTime the new start time
     */
    public void setStartTime(Timestamp startTime) {
      this.startTime = startTime;
    }

    /**
     * Checks if is first slab.
     *
     * @return true, if is first slab
     */
    public boolean isFirstSlab() {
      return isFirstSlab;
    }

    /**
     * Sets the first slab.
     *
     * @param isFirstSlab the new first slab
     */
    public void setFirstSlab(boolean isFirstSlab) {
      this.isFirstSlab = isFirstSlab;
    }
  }

  /**
   * Filter bed charges.
   *
   * @param charges       the charges
   * @param admitId       the admit id
   * @param resultCharges the result charges
   */
  public void filterBedCharges(List<ChargeDTO> charges, int admitId,
      List<ChargeDTO> resultCharges) {
    for (ChargeDTO charge : charges) {
      if (charge.getActivityId() == admitId) {
        resultCharges.add(charge);
      }
    }
  }

  /**
   * Gets the package ref.
   *
   * @return the package ref
   */
  public int getPackageRef() {
    return packageRef;
  }

  /**
   * Sets the package ref.
   *
   * @param packageRef the new package ref
   */
  public void setPackageRef(int packageRef) {
    this.packageRef = packageRef;
  }

  /**
   * Gets the package id.
   *
   * @return the package id
   */
  public String getPackageId() {
    return packageId;
  }

  /**
   * Sets the package id.
   *
   * @param packageId the new package id
   */
  public void setPackageId(String packageId) {
    this.packageId = packageId;
  }

  /**
   * Gets the visit type.
   *
   * @return the visit type
   */
  public String getVisitType() {
    return visitType;
  }

  /**
   * Sets the visit type.
   *
   * @param visitType the new visit type
   */
  public void setVisitType(String visitType) {
    this.visitType = visitType;
  }

  /**
   * Gets the operation details id.
   *
   * @return the operation details id
   */
  public Integer getOperationDetailsId() {
    return operationDetailsId;
  }

  /**
   * Sets the operation details id.
   *
   * @param operationDetailsId the new operation details id
   */
  public void setOperationDetailsId(Integer operationDetailsId) {
    this.operationDetailsId = operationDetailsId;
  }

  /**
   * Gets the operation proc id.
   *
   * @return the operation proc id
   */
  public Integer getOperationProcId() {
    return operationProcId;
  }

  /**
   * Sets the operation proc id.
   *
   * @param operationProcId the new operation proc id
   */
  public void setOperationProcId(Integer operationProcId) {
    this.operationProcId = operationProcId;
  }

  /**
   * Gets the main charge id.
   *
   * @return the main charge id
   */
  public String getMainChargeId() {
    return mainChargeId;
  }

  /**
   * Sets the main charge id.
   *
   * @param mainChargeId the new main charge id
   */
  public void setMainChargeId(String mainChargeId) {
    this.mainChargeId = mainChargeId;
  }

  /**
   * Gets the plan ids.
   *
   * @return the plan ids
   */
  public int[] getPlanIds() {
    return planIds;
  }

  /**
   * Sets the plan ids.
   *
   * @param planIds the new plan ids
   */
  public void setPlanIds(int[] planIds) {
    this.planIds = planIds;
  }

  /**
   * Gets the newly added item approval detail ids.
   *
   * @return the newly added item approval detail ids
   */
  public String[] getNewlyAddedItemApprovalDetailIds() {
    return newlyAddedItemApprovalDetailIds;
  }

  /**
   * Sets the newly added item approval detail ids.
   *
   * @param newlyAddedItemApprovalDetailIds the new newly added item approval detail ids
   */
  public void setNewlyAddedItemApprovalDetailIds(String[] newlyAddedItemApprovalDetailIds) {
    this.newlyAddedItemApprovalDetailIds = newlyAddedItemApprovalDetailIds;
  }

  /**
   * Gets the newly added item approval limit values.
   *
   * @return the newly added item approval limit values
   */
  public String[] getNewlyAddedItemApprovalLimitValues() {
    return newlyAddedItemApprovalLimitValues;
  }

  /**
   * Sets the newly added item approval limit values.
   *
   * @param newlyAddedItemApprovalLimitValues the new newly added item approval limit values
   */
  public void setNewlyAddedItemApprovalLimitValues(
      String[] newlyAddedItemApprovalLimitValues) {
    this.newlyAddedItemApprovalLimitValues = newlyAddedItemApprovalLimitValues;
  }

  /**
   * Insert order sets into patient packages.
   *
   * @param con                   the con
   * @param packageIdsList        the package ids list
   * @param packObIdsList         the pack ob ids list
   * @param multiVisitPackageList the multi visit package list
   * @param patPackageIdsList     the pat package ids list
   * @return the string
   * @throws SQLException the SQL exception
   * @throws IOException  Signals that an I/O exception has occurred.
   */
  public String insertOrderSetsIntoPatientPackages(Connection con, List<String> packageIdsList,
      List<String> packObIdsList, List<Boolean> multiVisitPackageList,
      List<Integer> patPackageIdsList) throws SQLException, IOException {
    Map<String, Integer> packagesMap = new HashMap<String, Integer>();
    for (int index = 0; index < packageIdsList.size(); index++) {
      String packageId = packageIdsList.get(index);
      String packObId = packObIdsList.get(index);
      if (packageId != null && !"".equals(packageId) && packObId != null
          && !"".equals(packObId) && !multiVisitPackageList.get(index)) {
        Integer patPackageId = packagesMap.get(packageId);
        if (patPackageId == null) {
          BasicDynaBean bean = patientPackageDao.getBean();
          patPackageId = patPackageDao.getNextSequence();
          bean.set("patient_package_id", patPackageId);
          bean.set("mr_no", this.mrNo);
          bean.set("package_id", Integer.parseInt(packageId));
          bean.set("status", "C");
          patientPackageDao.insert(con, bean);
          packagesMap.put(packageId, patPackageId);
        }
        patPackageIdsList.add(patPackageId);
      } else {
        patPackageIdsList.add(null);
      }
    }
    return null;
  }

  /**
   * Insert into patient package content and consumed.
   *
   * @param con              the con
   * @param patPackageId     the pat package id
   * @param packageContentId the package content id
   * @param packageId        the package id
   * @param activityId       the activity id
   * @param consumedQuantity the consumed quantity
   * @param prescriptionId   the prescription id
   * @param chargeId         the charge id
   * @param itemType         the item type
   * @throws SQLException the SQL exception
   * @throws IOException  Signals that an I/O exception has occurred.
   */
  public void insertIntoPatientPackageContentAndConsumed(Connection con, Integer patPackageId,
      Integer packageContentId, Integer packageId, String activityId, Integer consumedQuantity,
      Integer prescriptionId, String chargeId, String itemType)
      throws SQLException, IOException {
    // Inserting into new patient_package table.
    GenericDAO ppcDao = new GenericDAO("patient_package_contents");
    BasicDynaBean ppcBean = ppcDao.getBean();
    PackageDAO pdao = new PackageDAO(con);
    BasicDynaBean packageContent = pdao.getPackageContentDetail(packageId, packageContentId);
    Integer patientPackageContentId = ppcDao.getNextSequence();
    ppcBean.set("patient_package_content_id", patientPackageContentId);
    ppcBean.set("patient_package_id", patPackageId);
    ppcBean.set("package_content_id", packageContentId);
    ppcBean.set("package_id", packageId);
    ppcBean.set("activity_id", activityId);
    ppcBean.set("consultation_type_id", packageContent.get("consultation_type_id"));
    ppcBean.set("activity_type", packageContent.get("activity_type"));
    ppcBean.set("activity_qty", packageContent.get("activity_qty"));
    ppcBean.set("charge_head", packageContent.get("charge_head"));
    ppcBean.set("created_by", this.userName);
    ppcDao.insert(con, ppcBean);

    // inserting into patient_package_consumed
    GenericDAO ppccDao = new GenericDAO("patient_package_content_consumed");
    BasicDynaBean ppccBean = ppccDao.getBean();
    ppccBean.set("patient_package_consumed_id", ppccDao.getNextSequence());
    ppccBean.set("patient_package_content_id", patientPackageContentId);
    ppccBean.set("quantity",
        consumedQuantity != null ? consumedQuantity : packageContent.get("activity_qty"));
    ppccBean.set("prescription_id", prescriptionId);
    ppccBean.set("bill_charge_id", chargeId);
    ppccBean.set("item_type", itemType);
    ppccDao.insert(con, ppccBean);
  }


  /**
   * Gets the package content charges.
   *
   * @param pkg             the pkg
   * @param quantity        the quantity
   * @param surgeon         the surgeon
   * @param isInsurance     the is insurance
   * @param planIds         the plan ids
   * @param visitType       the visit type
   * @param patientId       the patient id
   * @param firstOfCategory the first of category
   * @return the package charges
   * @throws SQLException the SQL exception
   */
  public static List<ChargeDTO> getPackageContentCharges(BasicDynaBean pkg, BigDecimal quantity,
      String surgeon, boolean isInsurance, int[] planIds, String visitType, String patientId,
      Boolean firstOfCategory,String chargeHead, String actDesc, String actDescId,
      String actRemarks, String chargeRef) throws SQLException {
    int insuranceCategoryId = getIntegerValueFromBean(pkg, "insurance_category_id");
    ChargeDTO chrgdto = new ChargeDTO("PKG",chargeHead,(BigDecimal) pkg.get("charge"),
                   quantity, ((BigDecimal) pkg.get("discount")).multiply(quantity), "",
                   actDescId, actDesc,
                   null, isInsurance,(Integer) pkg.get("service_sub_group_id"),insuranceCategoryId,
                   visitType, patientId,firstOfCategory);
 
    chrgdto.setActRatePlanItemCode((String) pkg.get("item_code"));
    chrgdto.setCodeType((String) pkg.get("code_type"));
    chrgdto.setAllowDiscount((Boolean) pkg.get("allow_discount"));
    chrgdto.setAllowRateIncrease((Boolean) pkg.get("allow_rate_increase"));
    chrgdto.setAllowRateDecrease((Boolean) pkg.get("allow_rate_decrease"));
    chrgdto.setPackageId((Integer) pkg.get("package_id"));
    if (actRemarks != null && !actRemarks.equals("")) {
      chrgdto.setActRemarks(actRemarks);
    }
    if (null != chargeRef  && !chargeRef.equals("")) {
      chrgdto.setChargeRef(chargeRef);
    }
    // special case: if there is a surgeon, set the package conducting doctor
    // to be the same as the surgeon.
    if (surgeon != null && !surgeon.equals("")) {
      chrgdto.setPayeeDoctorId(surgeon);
    }
    if (pkg.get("billing_group_id") != null) {
      chrgdto.setBillingGroupId((Integer) pkg.get("billing_group_id"));
    }
    chrgdto.setSubmissionBatchType((String) pkg.get("submission_batch_type"));
    List lis = new ArrayList();
    lis.add(chrgdto);
    return lis;
  }

  /**
   * Split discount.
   *
   * @param charge          Main charge
   * @param packageCharge   Package charge
   * @param packageDiscount Package discount
   *
   * @return BigDecimal
   */
  public static BigDecimal discountSplit(BigDecimal charge, BigDecimal packageCharge, 
      BigDecimal packageDiscount) {
    BigDecimal discount = BigDecimal.ZERO;
    if ((packageCharge.compareTo(BigDecimal.ZERO) != 0) 
         && (packageDiscount.compareTo(BigDecimal.ZERO) != 0)) {
      BigDecimal newCharg = charge.divide(packageCharge, 10, RoundingMode.CEILING);
      discount = (BigDecimal) packageDiscount.multiply(newCharg);
    }
    return discount;
  }

}
