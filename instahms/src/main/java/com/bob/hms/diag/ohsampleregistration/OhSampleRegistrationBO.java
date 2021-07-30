package com.bob.hms.diag.ohsampleregistration;

import com.bob.hms.common.AutoIncrementId;
import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.Registration.RegistrationForm;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.billing.Bill;
import com.insta.hms.billing.BillDAO;
import com.insta.hms.billing.ChargeBO;
import com.insta.hms.billing.ChargeDAO;
import com.insta.hms.billing.ChargeDTO;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PhoneNumberUtil;
import com.insta.hms.diagnosticmodule.common.BatchSampleIdGenerator;
import com.insta.hms.diagnosticmodule.common.DiagnosticsDAO;
import com.insta.hms.diagnosticmodule.common.PrefixSampleIdGenerator;
import com.insta.hms.diagnosticmodule.internallab.AutomaticSampleRegistration;
import com.insta.hms.diagnosticmodule.internallab.InternalLab;
import com.insta.hms.diagnosticmodule.laboratory.DeptTokenGeneratorDAO;
import com.insta.hms.diagnosticmodule.laboratory.LaboratoryDAO;
import com.insta.hms.diagnosticmodule.laboratory.PendingSamplesDAO;
import com.insta.hms.diagnosticmodule.laboratory.TransferSamplesDAO;
import com.insta.hms.diagnosticsmasters.addtest.AddTestDAOImpl;
import com.insta.hms.diagnosticsmasters.addtest.TestTATBO;
import com.insta.hms.diagnosticsmasters.outhousemaster.OutHouseMasterDAO;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.DoctorMaster.DoctorMasterDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.InComingHospitals.InComingHospitalDAO;
import com.insta.hms.master.ReferalDoctor.ReferalDoctorDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The Class OhSampleRegistrationBO.
 *
 * @author hanumanth
 */
public class OhSampleRegistrationBO {

  private static Logger logger = LoggerFactory.getLogger(OhSampleRegistrationBO.class);

  /** The dao. */
  private static OhSampleRegistrationDAO dao = new OhSampleRegistrationDAO();

  /** The diag DAO. */
  private static GenericDAO diagDAO = new GenericDAO("diagnostics");

  /** The sample collection DAO. */
  private static GenericDAO sampleCollectionDAO = new GenericDAO("sample_collection");

  /** The bill dao. */
  private static GenericDAO billDao = new GenericDAO("bill");

  /**
   * Gets the out house details.
   *
   * @return the out house details
   */
  @SuppressWarnings("rawtypes")
  public List getOutHouseDetails() {
    return dao.getOutHouseDetails();
  }

  /**
   * Gets the incoming tests.
   *
   * @return the incoming tests
   */
  public String getIncomingTests() {
    return dao.getIncomingTests();
  }

  /**
   * Checking duplicate sampleId for each test.
   *
   * @param testId    the test id
   * @param isPackage the is package
   * @param sampleId  the sample id
   * @return For unique sampleId for all tests it will return false, else it will throw exception.
   * @throws Exception the exception
   */
  public boolean isSampleIdsExists(String[] testId, String[] isPackage, String[] sampleId)
      throws Exception {
    boolean status = true;
    BasicDynaBean testBean = null;
    for (int i = 0; i < testId.length; i++) {
      if (isPackage[i].equalsIgnoreCase("n")) {
        testBean = diagDAO.findByKey("test_id", testId[i]);
        if (testBean != null && testBean.get("sample_needed").equals("n")) {
          continue;
        }
        BasicDynaBean sampBean = sampleCollectionDAO.findByKey("sample_sno", sampleId[i]);
        if (sampBean != null) {
          status = false;
          throw new DuplicateSampleIdException("Duplicate Sample Id " + sampleId[i]);
          // return errorResponse(request, response, "Duplicate Sample Id "+sampleId[i]);
        }
      }
    }
    return status;
  }

  /**
   * Checking Lab Name or Incoming Hospital name exists or not.
   *
   * @param orginalLabName the orginal lab name
   * @return false if Incoming Hospital doesn't exists
   * @throws SQLException the SQL exception
   */
  @Deprecated
  public boolean isIncommingHospitalExists(String orginalLabName) throws SQLException {
    InComingHospitalDAO dao = new InComingHospitalDAO();
    BasicDynaBean exists = dao.findByKey("hospital_name", orginalLabName);

    return exists != null;
  }

  /**
   * Insert referral doctor.
   *
   * @param con             the con
   * @param referralDocName the referral doc name
   * @return true on success
   * @throws SQLException the SQL exception
   */
  public boolean insertReferalDoctor(Connection con, String referralDocName) throws SQLException {
    boolean status = true;

    ReferalDoctorDAO rdao = new ReferalDoctorDAO();
    DoctorMasterDAO ddao = new DoctorMasterDAO();
    boolean duplicate = rdao.checkDuplicateReferal(con, referralDocName);
    boolean dupDoctor = ddao.checkDuplicateDoctor(con, referralDocName);
    if (!duplicate && !dupDoctor) {
      String referralDocId = rdao.getNextReferalId(con);
      String mobileNo = "";
      int referalCategory = RegistrationForm.DEFALUT_REFERRAL_CATEGORY;
      String paymentEligible = RegistrationForm.DEFALUT_REFERRAL_PAYMENT_ELIGIBLE;
      status = rdao.saveNewReferal(con, referralDocId, referralDocName, mobileNo, referalCategory,
          paymentEligible);
    }

    return status;
  }

  /**
   * Create a map which contains sampleId as a key and sampleTypeId as a value.
   *
   * @param testIds      the test ids
   * @param sampleId     the sample id
   * @param sampleTypeId the sample type id
   * @return the map
   */
  public Map<String, String> createSampleTypeMap(String[] testIds, String[] sampleId,
      String[] sampleTypeId) {
    Map<String, String> map = new HashMap<String, String>();
    for (int i = 0; i < testIds.length; i++) {
      map.put(sampleId[i], sampleTypeId[i]);
    }

    return map;
  }

  /**
   * Create a map which contains sampleId as a key and orig_sample_no as a value.
   *
   * @param testIds       the test ids
   * @param sampleId      the sample id
   * @param origSampleNum the orig sample num
   * @return the map
   */
  public Map<String, String> createOrigSampleNoMap(String[] testIds, String[] sampleId,
      String[] origSampleNum) {
    Map<String, String> map = new HashMap<String, String>();
    for (int i = 0; i < testIds.length; i++) {
      map.put(sampleId[i], origSampleNum[i]);
    }

    return map;
  }

  /**
   * Create a map which contains sampleId as a key and outhouseid as a value.
   *
   * @param testIds    the test ids
   * @param sampleId   the sample id
   * @param outhouseId the outhouse id
   * @return the map
   */
  public Map<String, String> createOutSourceDestMap(String[] testIds, String[] sampleId,
      String[] outhouseId) {
    Map<String, String> map = new HashMap<String, String>();
    for (int i = 0; i < testIds.length; i++) {
      map.put(sampleId[i], outhouseId[i]);
    }

    return map;
  }

  /**
   * Create a map which contains (testid-outsourcedestid) as a key and outSourceChain as a value.
   *
   * @param centerId    the center id
   * @param testIdArray the test id array
   * @return the map
   * @throws SQLException the SQL exception
   */
  public Map<String, String> createOutSourceChainMap(int centerId, String[] testIdArray)
      throws SQLException {
    Map<String, String> map = new HashMap<String, String>();
    List<BasicDynaBean> allOutSourceList = new InComingHospitalDAO()
        .getCenterOutSourceAgainstTest(centerId, testIdArray);
    for (int i = 0; i < allOutSourceList.size(); i++) {
      BasicDynaBean outSourceBean = allOutSourceList.get(i);
      String testId = (String) outSourceBean.get("test_id");
      int outSourceDestId = (Integer) outSourceBean.get("outsource_dest_id");
      String outSourceChain = OutHouseMasterDAO.getOutsourceChainAgainstTest(testId,
          (Integer) outSourceBean.get("center_id"));
      map.put(testId + "-" + outSourceDestId, outSourceChain);
    }
    return map;
  }

  /**
   * This is creating one visit map and keeping patient name with visit id.
   *
   * @param pname    the pname
   * @param visitMap the visit map
   * @throws SQLException the SQL exception
   */

  public void createVisitMap(String pname, Map<String, String> visitMap) throws SQLException {

    String visitId = AutoIncrementId.getSequenceId("INHOUSE_VISIT_ID", "INHOUSEVISITID");
    visitMap.put(pname, visitId);
  }

  /**
   * This is creating sequence of Lab or Rad and storing in a map with patientName.
   *
   * @param category the category
   * @param pname    the pname
   * @return the map
   * @throws SQLException the SQL exception
   */
  public Map<String, String> createLabNo(String category, String pname) throws SQLException {
    Map<String, String> map = new HashMap<String, String>();
    if (category.equals("DEP_LAB")) {
      map.put(pname, DiagnosticsDAO.getNextSequenceNo("LABNO"));
    } else {
      map.put(pname, DiagnosticsDAO.getNextSequenceNo("RADNO"));
    }
    return map;
  }

  /**
   * Creating Bill Bean storing in List and returning List.
   *
   * @param billType
   *          the bill type
   * @param centerId
   *          the center id
   * @param pname
   *          the pname
   * @param userName
   *          the user name
   * @param billRatePlan
   *          the bill rate plan
   * @param discAuth
   *          the disc auth
   * @param visitMap
   *          the visit map
   * @return the list
   * @throws SQLException
   *           the SQL exception
   */
  public List<BasicDynaBean> createBillListBean(Connection con, String billType, Integer centerId,
      String pname, String userName, String[] billRatePlan, Integer discAuth,
      Map<String, String> visitMap) throws SQLException {

    BasicDynaBean billBean = null;
    String billNo;

    billBean = billDao.getBean();
    java.sql.Timestamp now = getTimeStamp();

    if (billType.equals("BN")) {
      billNo = BillDAO.getNextBillNo(con, Bill.BILL_TYPE_PREPAID, "t", "T", centerId, false, false);
      billBean.set("bill_type", Bill.BILL_TYPE_PREPAID);
      if (centerId.toString().equals("")) {
        billBean.set("status", "A");
      } else {
        billBean.set("status", "C");
        billBean.set("closed_date", now);
        billBean.set("finalized_date", now);
        billBean.set("last_finalized_at", now);
        billBean.set("payment_status", "P");
      }
    } else {
      billNo = BillDAO.getNextBillNo(con, Bill.BILL_TYPE_CREDIT, "t", "T", centerId, false, false);
      billBean.set("bill_type", Bill.BILL_TYPE_CREDIT);
      billBean.set("status", "A");
    }
    billBean.set("bill_no", billNo);
    billBean.set("restriction_type", "T");
    billBean.set("visit_id", visitMap.get(pname));
    billBean.set("open_date", now);
    billBean.set("bill_rate_plan_id", billRatePlan[0]);
    billBean.set("username", userName);
    billBean.set("opened_by", userName);
    billBean.set("visit_type", Bill.BILL_VISIT_TYPE_INCOMING);
    billBean.set("remarks", "Incoming sample registration");
    billBean.set("mod_time", now);
    if (discAuth != -1) {
      billBean.set("discount_auth", discAuth);
    }
    List<BasicDynaBean> bills = new ArrayList<BasicDynaBean>();
    bills.add(billBean);

    return bills;

  }

  /**
   * Creating IncomingSampleBeanmain for incoming_sample_registration table and storing in linst and
   * returning list.
   *
   * @param pname              the pname
   * @param orginalLabId       the orginal lab id
   * @param phoneNo            the phone no
   * @param phoneNoCountryCode the phone no country code
   * @param govtIdType         the govt id type
   * @param govtId             the govt id
   * @param ageStr             the age str
   * @param patOtherInfo       the pat other info
   * @param genderValue        the gender value
   * @param referralDocId      the referral doc id
   * @param category           the category
   * @param centerId           the center id
   * @param visitMap           the visit map
   * @param bills              the bills
   * @param isRequestFromHIS   the is request from HIS
   * @param hisPatientID       the his patient ID
   * @param ageUnit            the age unit
   * @param dob                the dob
   * @return the list
   * @throws SQLException the SQL exception
   */
  public List<BasicDynaBean> createIncomingSampleBeanMain(String pname, String orginalLabId,
      String phoneNo, String phoneNoCountryCode, Integer govtIdType, String govtId, String ageStr,
      String patOtherInfo, String genderValue, String referralDocId, String category, int centerId,
      Map<String, String> visitMap, List<BasicDynaBean> bills, String isRequestFromHIS,
      String hisPatientID, String ageUnit, Date dob) throws SQLException {

    BasicDynaBean inComingSampleBeanMain = null;
    inComingSampleBeanMain = new GenericDAO("incoming_sample_registration").getBean();
    inComingSampleBeanMain.set("incoming_visit_id", visitMap.get(pname));
    inComingSampleBeanMain.set("billno", bills.get(0).get("bill_no"));
    inComingSampleBeanMain.set("orig_lab_name", orginalLabId);
    inComingSampleBeanMain.set("patient_name", pname);
    inComingSampleBeanMain.set("date", getTimeStamp());
    inComingSampleBeanMain.set("phone_no", phoneNo);
    inComingSampleBeanMain.set("identifier_id", govtIdType);
    inComingSampleBeanMain.set("government_identifier", govtId);
    inComingSampleBeanMain.set("phone_no_country_code", phoneNoCountryCode);
    inComingSampleBeanMain.set("patient_other_info", patOtherInfo);
    int age = 0;
    if (!ageStr.equals("")) {
      age = Integer.parseInt(ageStr);
    }
    inComingSampleBeanMain.set("patient_age", age);
    inComingSampleBeanMain.set("isr_dateofbirth", dob);
    inComingSampleBeanMain.set("age_unit", ageUnit);
    inComingSampleBeanMain.set("patient_gender", genderValue);
    inComingSampleBeanMain.set("referring_doctor", referralDocId);
    inComingSampleBeanMain.set("category", category);
    inComingSampleBeanMain.set("center_id", centerId);
    inComingSampleBeanMain.set("incoming_source_type",
        (null != isRequestFromHIS && isRequestFromHIS.equalsIgnoreCase("y") ? "IH" : "H"));
    inComingSampleBeanMain.set("his_visit_id", hisPatientID);
    ArrayList<BasicDynaBean> list = new ArrayList<BasicDynaBean>();
    list.add(inComingSampleBeanMain);
    return list;
  }

  /**
   * Creating Sample set for either batch Based Sample No or Prefix Based sample No.
   *
   * @param prefixOrbatchBased the prefix orbatch based
   * @return the sample set
   */
  public Set<String> getSampleSet(String[] prefixOrbatchBased) {
    Set<String> sampleSet = null;
    sampleSet = new HashSet<String>(Arrays.asList(prefixOrbatchBased));
    return sampleSet;
  }

  /**
   * Creating sampleBean.
   *
   * @param sampleSet               the sample set
   * @param sampleTypeId            the sample type id
   * @param pname                   the pname
   * @param isBatchBased            the is batch based
   * @param batchBasedNo            the batch based no
   * @param outhouseId              the outhouse id
   * @param visitMap                the visit map
   * @param sampNotoTypemap         the samp noto typemap
   * @param sampleNotoSampleID      the sample noto sample ID
   * @param sampleNoToOrigSmplNoMap the sample no to orig smpl no map
   * @param outSourceDestIdMap      the out source dest id map
   * @param colSampleDate           the col sample date
   * @param isBillExists4LIS        the is bill exists 4 LIS
   * @param sampleDeetsBean         the sample deets bean
   * @return the list
   * @throws NumberFormatException the number format exception
   * @throws SQLException          the SQL exception
   * @throws ParseException        the parse exception
   */
  @SuppressWarnings("rawtypes")
  public List<BasicDynaBean> createSampleBean(Set<String> sampleSet, String[] sampleTypeId,
      String pname, Boolean isBatchBased, String batchBasedNo, String[] outhouseId,
      Map<String, String> visitMap, Map<String, String> sampNotoTypemap,
      Map<Object, Integer> sampleNotoSampleID, Map<String, String> sampleNoToOrigSmplNoMap,
      Map<String, String> outSourceDestIdMap, String colSampleDate, boolean isBillExists4LIS,
      BasicDynaBean sampleDeetsBean) throws NumberFormatException, SQLException, ParseException {

    List<BasicDynaBean> list = new ArrayList<BasicDynaBean>();

    BasicDynaBean sampleBean = null;
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");

    Iterator sampleIt = null;
    sampleIt = null != sampleSet ? sampleSet.iterator() : sampleIt;
    String sampleNo;
    Integer sampleCollectionID = null;

    while (sampleIt.hasNext()) {
      sampleNo = (String) sampleIt.next();

      if (sampleNo.isEmpty()) {
        continue;// possible for Lab test with sample_needed = No
      }
      if (isBillExists4LIS && sampleDeetsBean != null) {
        sampleNotoSampleID.put(sampleNo, (Integer) sampleDeetsBean.get("sample_collection_id"));
        continue;
      }

      sampleBean = sampleCollectionDAO.getBean();
      sampleCollectionID = sampleCollectionDAO.getNextSequence();
      sampleBean.set("sample_collection_id", sampleCollectionID);
      sampleBean.set("patient_id", visitMap.get(pname));
      sampleBean.set("sample_sno", isBatchBased ? batchBasedNo : sampleNo);

      if (null != colSampleDate) {
        sampleBean.set("sample_date",
            new java.sql.Timestamp(dateFormat.parse(colSampleDate).getTime()));
      } else {
        sampleBean.set("sample_date", getTimeStamp());
      }

      if (sampNotoTypemap.get(sampleNo) != "") {
        sampleBean.set("sample_type_id", Integer.parseInt(sampNotoTypemap.get(sampleNo)));
      }
      if (sampleNoToOrigSmplNoMap.get(sampleNo) != "") {
        sampleBean.set("orig_sample_no", sampleNoToOrigSmplNoMap.get(sampleNo));
      }
      sampleBean.set("sample_status", "C");
      if (!outSourceDestIdMap.get(sampleNo).isEmpty() && outSourceDestIdMap.get(sampleNo) != "") {
        sampleBean.set("outsource_dest_id", Integer.parseInt(outSourceDestIdMap.get(sampleNo)));
      }
      sampleBean.set("sample_transfer_status", "P");
      list.add(sampleBean);
      sampleNotoSampleID.put(sampleNo, sampleCollectionID);

    }
    return list;
  }

  /**
   * Creating inComingSampleBean for incoming_sample_registration_details table.
   *
   * @param testId        the test id
   * @param pname         the pname
   * @param category      the category
   * @param origSampleNum the orig sample num
   * @param visitMap      the visit map
   * @param nextPresId    the next pres id
   * @return the list
   * @throws SQLException the SQL exception
   */
  public List<BasicDynaBean> createIncommingSamplebean(String testId, String pname, String category,
      String origSampleNum, Map<String, String> visitMap, int nextPresId) throws SQLException {

    BasicDynaBean inComingSampleBean = null;
    inComingSampleBean = new GenericDAO("incoming_sample_registration_details").getBean();
    inComingSampleBean.set("incoming_visit_id", visitMap.get(pname));
    if (category.equals("DEP_LAB")) {
      inComingSampleBean.set("orig_sample_no", origSampleNum);
    }
    inComingSampleBean.set("test_id", testId);
    inComingSampleBean.set("prescribed_id", nextPresId);
    List<BasicDynaBean> list = new ArrayList<BasicDynaBean>();
    list.add(inComingSampleBean);

    return list;
  }

  /**
   * Creating testPresCribedBean for tests_prescribed.
   *
   * @param con                  the con
   * @param testId               the test id
   * @param outhouseId           the outhouse id
   * @param pname                the pname
   * @param userid               the userid
   * @param packageRef           the package ref
   * @param category             the category
   * @param sampleId             the sample id
   * @param isBatchBased         the is batch based
   * @param houseType            the house type
   * @param centerId             the center id
   * @param batchBased           the batch based
   * @param visitMap             the visit map
   * @param labNOMap             the lab NO map
   * @param sampleNotoSampleID   the sample noto sample ID
   * @param nextPresId           the next pres id
   * @param packageActivityIndex the package activity index
   * @return the list
   * @throws Exception the exception
   */
  public List<BasicDynaBean> createTestPrescribedBean(Connection con, String testId,
      String outhouseId, String pname, String userid, String packageRef, String category,
      String sampleId, Boolean isBatchBased, String houseType, Integer centerId, String batchBased,
      Map<String, String> visitMap, Map<String, String> labNOMap,
      Map<Object, Integer> sampleNotoSampleID, int nextPresId, int packageActivityIndex)
      throws Exception {

    BasicDynaBean testPresCribedBean = null;
    BasicDynaBean testBean = null;
    Integer sampleCollectionID = null;

    testPresCribedBean = new GenericDAO("tests_prescribed").getBean();
    testPresCribedBean.set("pat_id", visitMap.get(pname));
    testBean = diagDAO.findByKey("test_id", testId);
    if ("false".equals(testBean.get("conduction_applicable").toString())) {
      testPresCribedBean.set("conducted", "U");
    } else {
      if ("false".equals(testBean.get("results_entry_applicable").toString())) {
        testPresCribedBean.set("conducted", "NRN");
      } else {
        testPresCribedBean.set("conducted", "N");
      }
    }
    testPresCribedBean.set("test_id", testId);
    if (null != outhouseId && !outhouseId.equals("")) {
      testPresCribedBean.set("outsource_dest_id", Integer.parseInt(outhouseId));
    }
    testPresCribedBean.set("pres_date", getTimeStamp());
    testPresCribedBean.set("pres_doctor", null);
    testPresCribedBean.set("sflag", "1");
    testPresCribedBean.set("prescribed_id", nextPresId);
    testPresCribedBean.set("curr_location_presc_id", nextPresId);
    testPresCribedBean.set("user_name", userid);// session.getAttribute("userid")
    testPresCribedBean.set("prescription_type", "i");
    testPresCribedBean.set("package_ref",
        packageRef == null || packageRef.equals("") ? null : Integer.parseInt(packageRef));
    testPresCribedBean.set("exp_rep_ready_time",
        new TestTATBO().calculateExptRptReadyTime(getTimeStamp(), testId, centerId));
    if (new PendingSamplesDAO().isOutsourceTest((String) testPresCribedBean.get("test_id"),
        centerId)) {
      testPresCribedBean.set("conduction_type", "o");
    }

    if (category.equals("DEP_LAB")) {
      testPresCribedBean.set("sample_no", sampleId);
      sampleCollectionID = isBatchBased ? sampleNotoSampleID.get(batchBased)
          : sampleNotoSampleID.get(sampleId);
      testPresCribedBean.set("sample_collection_id", sampleCollectionID);
      testPresCribedBean.set("is_outhouse_selected", houseType.equalsIgnoreCase("O") ? "Y" : "N");

    }

    BasicDynaBean diagGenericPref = GenericPreferencesDAO.getdiagGenericPref();

    if (diagGenericPref.get("autogenerate_labno").equals("Y")) {
      testPresCribedBean.set("labno", labNOMap.get(pname));
    }
    String testDeptId = (String) ((BasicDynaBean) AddTestDAOImpl.getTestBean(testId))
        .get("ddept_id");
    if (category.equals("DEP_LAB")) {
      if (diagGenericPref.get("gen_token_for_lab").equals("Y")) {
        testPresCribedBean.set("token_number",
            DeptTokenGeneratorDAO.getToken(testDeptId, centerId));
      }
    } else {
      if (diagGenericPref.get("gen_token_for_rad").equals("Y")) {
        testPresCribedBean.set("token_number",
            DeptTokenGeneratorDAO.getToken(testDeptId, centerId));
      }
    }

    List<BasicDynaBean> list = new ArrayList<BasicDynaBean>();
    list.add(testPresCribedBean);

    return list;
  }

  /**
   * Creating packagePrescribedBean for package_prescribed table.
   *
   * @param con               the con
   * @param pname             the pname
   * @param testId            the test id
   * @param userName          the user name
   * @param visitMap          the visit map
   * @param packMasterDetails the pack master details
   * @param packagePrecId     the package prec id
   * @return the list
   * @throws SQLException the SQL exception
   */
  public List<BasicDynaBean> createPackagePrescribedBean(Connection con, String pname,
      String testId, String userName, Map<String, String> visitMap, BasicDynaBean packMasterDetails,
      int packagePrecId) throws SQLException {

    BasicDynaBean packagePrescribedBean = new GenericDAO("package_prescribed").getBean();

    packagePrescribedBean.set("mr_no", "");
    packagePrescribedBean.set("patient_id", visitMap.get(pname));
    packagePrescribedBean.set("prescription_id", packagePrecId);
    packagePrescribedBean.set("package_id", Integer.parseInt(testId));
    packagePrescribedBean.set("presc_date", getTimeStamp());
    packagePrescribedBean.set("doctor_id", null);
    packagePrescribedBean.set("user_name", userName);
    packagePrescribedBean.set("common_order_id",
        com.bob.hms.common.DataBaseUtil.getNextSequence("common_order_seq"));
    List<BasicDynaBean> list = new ArrayList<BasicDynaBean>();
    list.add(packagePrescribedBean);

    return list;

  }

  /**
   * Creating billChargeBean for bill_charge.
   *
   * @param con                the con
   * @param testId             the test id
   * @param pname              the pname
   * @param isPackage          the is package
   * @param testMasterDetails  the test master details
   * @param category           the category
   * @param chargeStr          the charge str
   * @param packMasterDetails  the pack master details
   * @param amtStr             the amt str
   * @param discStr            the disc str
   * @param userName           the user name
   * @param discountAuth       the discount auth
   * @param conductingDoctorId the conducting doctor id
   * @param bills              the bills
   * @param chargeId           the charge id
   * @return the list
   * @throws SQLException the SQL exception
   */
  public List<BasicDynaBean> createBillChargeBean(Connection con, String testId, String pname,
      String isPackage, BasicDynaBean testMasterDetails, String category, String chargeStr,
      BasicDynaBean packMasterDetails, String amtStr, String discStr, String userName,
      String discountAuth, String conductingDoctorId, List<BasicDynaBean> bills, String chargeId)
      throws SQLException {

    BasicDynaBean billChargeBean = null;
    // String chargeId = "";
    int discAuth = discountAuth == null || discountAuth.equals("") ? -1
        : Integer.parseInt(discountAuth);

    billChargeBean = new GenericDAO("bill_charge").getBean();
    // chargeId = new ChargeDAO(con).getNextChargeId();
    billChargeBean.set("charge_id", chargeId);
    billChargeBean.set("bill_no", bills.get(0).get("bill_no"));
    if (isPackage.equalsIgnoreCase("y")) {
      billChargeBean.set("charge_group", ChargeDTO.CG_PACKAGE);
    } else {
      billChargeBean.set("charge_group", ChargeDTO.CG_DIAGNOSTICS);
    }
    if (category.equals("DEP_LAB")) {
      if (isPackage.equalsIgnoreCase("y")) {
        billChargeBean.set("charge_head", ChargeDTO.CH_PACKAGE);
      } else {
        billChargeBean.set("charge_head", ChargeDTO.CH_DIAG_LAB);
      }
    } else if (category.equals("DEP_RAD")) {
      if (isPackage.equalsIgnoreCase("y")) {
        billChargeBean.set("charge_head", ChargeDTO.CH_PACKAGE);
      } else {
        billChargeBean.set("charge_head", ChargeDTO.CH_DIAG_RAD);
      }
    }

    if (isPackage.equalsIgnoreCase("n")) {
      billChargeBean.set("act_department_id", testMasterDetails.get("ddept_id"));
      billChargeBean.set("act_description", testMasterDetails.get("test_name"));
      billChargeBean.set("billing_group_id", testMasterDetails.get("billing_group_id"));
    } else {
      billChargeBean.set("act_description", packMasterDetails.get("package_name"));
      billChargeBean.set("billing_group_id", packMasterDetails.get("billing_group_id"));
    }

    billChargeBean.set("act_remarks", "incoming sample reg..");
    Double charge = null;
    Double amt = null;
    Double disc = null;
    if (!chargeStr.equals("")) {
      charge = Double.parseDouble(chargeStr);
    }
    if (!amtStr.equals("")) {
      amt = Double.parseDouble(amtStr);
    }
    if (!discStr.equals("")) {
      disc = Double.parseDouble(discStr);
    }

    billChargeBean.set("act_rate", new BigDecimal(charge));
    billChargeBean.set("orig_rate", new BigDecimal(charge));
    billChargeBean.set("act_unit", null);
    billChargeBean.set("act_quantity", new BigDecimal(1));
    billChargeBean.set("amount", new BigDecimal(amt));
    billChargeBean.set("paid_amount", new BigDecimal(0));
    billChargeBean.set("discount", new BigDecimal(disc));
    billChargeBean.set("posted_date", getTimeStamp());
    billChargeBean.set("status", "A");
    billChargeBean.set("username", userName);
    billChargeBean.set("mod_time", getTimeStamp());
    billChargeBean.set("hasactivity", true);
    billChargeBean.set("act_description_id", testId);
    if (isPackage.equalsIgnoreCase("n")) {
      billChargeBean.set("service_sub_group_id",
          (Integer) (testMasterDetails.get("service_sub_group_id")));
    } else {
      billChargeBean.set("service_sub_group_id",
          (Integer) (packMasterDetails.get("service_sub_group_id")));
    }
    if ((new BigDecimal(disc)).compareTo(BigDecimal.ZERO) != 0) {
      billChargeBean.set("overall_discount_auth", discAuth);
    }
    BigDecimal totalAmt = BigDecimal.ZERO;
    totalAmt = totalAmt.add(new BigDecimal(amt));

    if ((conductingDoctorId != null) && !conductingDoctorId.equals("")) {
      billChargeBean.set("payee_doctor_id", conductingDoctorId);
    } else {
      billChargeBean.set("payee_doctor_id", null);
    }
    List<BasicDynaBean> list = new ArrayList<BasicDynaBean>();
    list.add(billChargeBean);

    return list;

  }

  /**
   * Creating billActivityBean for bill_activity_charge table.
   *
   * @param chargeId           the charge id
   * @param isPackage          the is package
   * @param packagePrecId      the package prec id
   * @param nextPresId         the next pres id
   * @param category           the category
   * @param conductingDoctorId the conducting doctor id
   * @return the array list
   * @throws SQLException the SQL exception
   */
  public ArrayList<BasicDynaBean> createBillActivityBean(String chargeId, String isPackage,
      Integer packagePrecId, Integer nextPresId, String category, String conductingDoctorId)
      throws SQLException {

    BasicDynaBean billActivityBean = null;

    billActivityBean = new GenericDAO("bill_activity_charge").getBean();
    billActivityBean.set("charge_id", chargeId);

    if (isPackage.equalsIgnoreCase("y")) {
      billActivityBean.set("activity_code", "PKG");
      billActivityBean.set("activity_id", packagePrecId.toString());
      billActivityBean.set("payment_charge_head", null);

    } else {
      billActivityBean.set("activity_code", "DIA");
      billActivityBean.set("activity_id", nextPresId.toString());
      billActivityBean.set("payment_charge_head",
          category.equalsIgnoreCase("DEP_LAB") ? "LTDIA" : "RTDIA");
    }

    if (conductingDoctorId != null && !conductingDoctorId.equals("")) {
      billActivityBean.set("doctor_id", conductingDoctorId);
    } else {
      billActivityBean.set("doctor_id", null);
    }

    ArrayList<BasicDynaBean> list = new ArrayList<BasicDynaBean>();
    list.add(billActivityBean);

    return list;
  }

  /**
   * Creating outhouseSampleBean for outsource_sample_details table.
   *
   * @param testId     the test id
   * @param nextPresId the next pres id
   * @param pname      the pname
   * @param outhouseId the outhouse id
   * @param category   the category
   * @param sampleId   the sample id
   * @param visitMap   the visit map
   * @return the list
   * @throws SQLException the SQL exception
   */
  public List<BasicDynaBean> createOudHouseSampleBean(String testId, Integer nextPresId,
      String pname, String outhouseId, String category, String sampleId,
      Map<String, String> visitMap) throws SQLException {
    BasicDynaBean outhouseSampleBean = null;
    outhouseSampleBean = new GenericDAO("outsource_sample_details").getBean();
    outhouseSampleBean.set("prescribed_id", nextPresId);
    outhouseSampleBean.set("visit_id", visitMap.get(pname));
    if (null != outhouseId && !outhouseId.equals("")) {
      outhouseSampleBean.set("outsource_dest_id", Integer.parseInt(outhouseId));
    }
    outhouseSampleBean.set("sample_no",
        category.equalsIgnoreCase("DEP_LAB") ? sampleId : visitMap.get(pname));
    outhouseSampleBean.set("test_id", testId);
    List<BasicDynaBean> list = new ArrayList<BasicDynaBean>();
    list.add(outhouseSampleBean);

    return list;

  }

  /**
   * Creates the automatic sample reg map.
   *
   * @param outHouseId            the out house id
   * @param testId                the test id
   * @param nextPresId            the next pres id
   * @param sampleId              the sample id
   * @param sampleTypeId          the sample type id
   * @param pname                 the pname
   * @param visitMap              the visit map
   * @param incomingSamplesRegMap the incoming samples reg map
   * @throws Exception the exception
   */
  // use to create a map for the automatic sample registration for internal lab tests
  public void createAutomaticSampleRegMap(int outHouseId, String testId, Integer nextPresId,
      String sampleId, String sampleTypeId, String pname, Map<String, String> visitMap,
      Map<String, InternalLab> incomingSamplesRegMap) throws Exception {

    BasicDynaBean outSourceBean = new TransferSamplesDAO().getDiagOutSourceDetails(outHouseId);
    Integer conductionCenterId = (Integer) outSourceBean.get("center_id");
    LaboratoryDAO labDao = new LaboratoryDAO();

    if (incomingSamplesRegMap.containsKey(visitMap.get(pname) + "," + outHouseId)) {
      InternalLab labDto = incomingSamplesRegMap.get(visitMap.get(pname) + "," + outHouseId);

      String[] testIDs = labDto.getInternalLabTestIds();
      String[] prescIds = labDto.getInternalLabPrescIds();
      String[] smplNos = labDto.getInternalLabSampleNos();
      String[] smplTypIds = labDto.getInternalLabSampleTypeIds();
      // String internalLabCenterId = labDto.getInternalLabCenterid();

      labDto.setInternalLabTestIds((String[]) labDao.addToArray(testIDs, new String[] { testId }));
      labDto.setInternalLabPrescIds(
          (String[]) labDao.addToArray(prescIds, new String[] { nextPresId.toString() }));
      labDto.setInternalLabSampleNos(
          (String[]) labDao.addToArray(smplNos, new String[] { sampleId }));
      labDto.setInternalLabSampleTypeIds(
          (String[]) labDao.addToArray(smplTypIds, new String[] { sampleTypeId }));
    } else {
      InternalLab labDto = new InternalLab();
      labDto.setInternalLabTestIds(new String[] { testId });
      labDto.setInternalLabPrescIds((new String[] { nextPresId.toString() }));
      labDto.setInternalLabSampleNos(new String[] { sampleId });
      labDto.setInternalLabSampleTypeIds(new String[] { sampleTypeId });
      labDto.setInternalLabCenterId(conductionCenterId.toString());
      incomingSamplesRegMap.put(visitMap.get(pname) + "," + outHouseId, labDto);
    }
  }

  /**
   * Updating ohPayment.
   *
   * @param con                the con
   * @param testPrescribedList the test prescribed list
   * @return true, if successful
   * @throws Exception the exception
   */
  public boolean ohPayment(Connection con, List<BasicDynaBean> testPrescribedList)
      throws Exception {
    boolean ohAmtStatus = false;

    for (int i = 0; i < testPrescribedList.size(); i++) {
      ChargeBO chargeBo = new ChargeBO();
      String chargeID = LaboratoryDAO.getOhTestChargeId(con,
          (Integer) testPrescribedList.get(i).get("prescribed_id"), ChargeDTO.CH_DIAG_LAB);
      ohAmtStatus = chargeBo.updateOhPayment(con, chargeID,
          VisitDetailsDAO.getCenterId(con, (String) testPrescribedList.get(i).get("pat_id")));
    }

    return ohAmtStatus;
  }

  /**
   * Insert transaction.
   *
   * @param con   the con
   * @param list  the list
   * @param table the table
   * @return true, if successful
   * @throws Exception the exception
   */
  public boolean insertTransaction(Connection con, List<BasicDynaBean> list, String table)
      throws Exception {
    boolean status = false;
    if (list.size() > 0) {
      status = new GenericDAO(table).insertAll(con, list);
    }
    return status;
  }

  /**
   * Gets the time stamp.
   *
   * @return the time stamp
   */
  private java.sql.Timestamp getTimeStamp() {

    return new java.sql.Timestamp(new java.util.Date().getTime());
  }

  /*
   * This method is the entry point for ISR through INSTA API.
   * 
   * All the method which is used in processPrescriptions is refactor from
   * OhSampleRegistrationAction savePrescription method
   * 
   */

  /**
   * Process prescriptions.
   *
   * @param testIds                 the test ids
   * @param isPackages              the is packages
   * @param sampleIds               the sample ids
   * @param originalLabName         the original lab name
   * @param referalDoctorname       the referal doctorname
   * @param sampleTypeIds           the sample type ids
   * @param isAutoGenerateSampleReq the is auto generate sample req
   * @param category                the category
   * @param origSampleNum           the orig sample num
   * @param centerId                the center id
   * @param pname                   the pname
   * @param billType                the bill type
   * @param userName                the user name
   * @param billRatePlan            the bill rate plan
   * @param discountAuth            the discount auth
   * @param phoneNo                 the phone no
   * @param ageStr                  the age str
   * @param patOtherInfo            the pat other info
   * @param genderValue             the gender value
   * @param referralDocId           the referral doc id
   * @param outhouseId              the outhouse id
   * @param packageRefs             the package refs
   * @param userid                  the userid
   * @param houseType               the house type
   * @param amtStr                  the amt str
   * @param discStr                 the disc str
   * @param conductingDoctorId      the conducting doctor id
   * @param chargeStr               the charge str
   * @param testINpackage           the test I npackage
   * @param diagGenericPref         the diag generic pref
   * @param outsourceDestType       the outsource dest type
   * @param colSampleDate           the col sample date
   * @param hisParams               the his params
   * @return the map
   * @throws Exception the exception
   */
  @SuppressWarnings("static-access")
  public Map<String, String> processPrescriptions(String[] testIds, String[] isPackages,
      String[] sampleIds, String originalLabName, String referalDoctorname, String[] sampleTypeIds,
      String isAutoGenerateSampleReq, String category, String[] origSampleNum, int centerId,
      String pname, String billType, String userName, String[] billRatePlan, String discountAuth,
      String phoneNo, String ageStr, String patOtherInfo, String genderValue, String referralDocId,
      String[] outhouseId, String[] packageRefs, String userid, String[] houseType, String[] amtStr,
      String[] discStr, String[] conductingDoctorId, String[] chargeStr, String[] testINpackage,
      BasicDynaBean diagGenericPref, String[] outsourceDestType, String colSampleDate,
      Map<String, String> hisParams) throws Exception {

    Map<String, String> visitMap = new HashMap<String, String>();
    Map<String, String> labNOMap = new HashMap<String, String>();
    Map<String, String> sampNotoTypemap = new HashMap<String, String>();
    Map<String, String> sampleNoToOrigSmplNoMap = new HashMap<String, String>();
    Map<String, String> outSourceDestIdMap = new HashMap<String, String>();
    Map<String, String> outSourceChainMap = new HashMap<String, String>();

    List<BasicDynaBean> sampleBeanList = new ArrayList<BasicDynaBean>();
    List<BasicDynaBean> incomingList = new ArrayList<BasicDynaBean>();
    List<BasicDynaBean> testPrescribedList = new ArrayList<BasicDynaBean>();
    List<BasicDynaBean> packagePrescribedList = new ArrayList<BasicDynaBean>();
    List<BasicDynaBean> billCharges = new ArrayList<BasicDynaBean>();
    List<BasicDynaBean> bills = new ArrayList<BasicDynaBean>();
    List<BasicDynaBean> incomingMainList = new ArrayList<BasicDynaBean>();
    List<BasicDynaBean> billActivityCharges = new ArrayList<BasicDynaBean>();
    List<BasicDynaBean> outhouseSampleBeanList = new ArrayList<BasicDynaBean>();

    Map<Object, Integer> sampleNotoSampleID = new HashMap<Object, Integer>();
    Map<String, InternalLab> incomingSamplesRegMap = new HashMap<String, InternalLab>();

    Connection con = null;
    try {

      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);

      String noGeneration = (String) diagGenericPref.get("sample_no_generation");
      int discAuth = discountAuth == null || discountAuth.equals("") ? -1
          : Integer.parseInt(discountAuth);
      boolean isBatchBased = isAutoGenerateSampleReq.equals("Y") && noGeneration.equals("B");

      BasicDynaBean patientDeets4LIS = null;
      String hisToken = hisParams.get("hisToken");
      String hisPatientID = hisParams.get("hisPatientID");
      boolean isBillExists4LIS = false;
      BasicDynaBean sampleBean = null;
      boolean isRequestFromHIS = (null != hisToken && hisToken.equalsIgnoreCase("y"));
      if (isRequestFromHIS) {
        isBillExists4LIS = (patientDeets4LIS = OhSampleRegistrationDAO
            .isHisPatientidExists(hisPatientID)) != null;
      }

      // Checking duplicate sample no for manual entry .

      if (((isAutoGenerateSampleReq.equals("N") && !isRequestFromHIS)
          || ((isRequestFromHIS && isBillExists4LIS)
              ? ((sampleBean = OhSampleRegistrationDAO.isSampleNumExistsForPatient(con,
                  (String) patientDeets4LIS.get("incoming_visit_id"), sampleIds[0])) == null)
              : (isRequestFromHIS)))
          && category.equals("DEP_LAB")) {
        isSampleIdsExists(testIds, isPackages, sampleIds);
      }

      InComingHospitalDAO hospitalDao = new InComingHospitalDAO();

      BasicDynaBean incomingLabBean = hospitalDao.checkOrCreateIncomingHospital(con,
          originalLabName);
      String orginalLabId = null;
      if (incomingLabBean != null) {
        orginalLabId = (String) incomingLabBean.get("hospital_id");
      } else {
        throw new Exception("Fail to insert incoming hospital");
      }

      if ((referalDoctorname != null && referalDoctorname != "")
          && !insertReferalDoctor(con, referalDoctorname)) {

        throw new Exception("Fail to insert referal doctor");
      }

      // used when the tests are mapped with multiple hops and multiple tests collected as same
      // sample type
      // then we force to collect as different sample.
      String[] outSourceChain = new String[testIds.length];
      if (category.equals("DEP_LAB")) {
        outSourceChainMap.putAll(createOutSourceChainMap(centerId, testIds));
        if (outSourceChainMap != null) {
          for (int i = 0; i < testIds.length; i++) {
            String chainkey = testIds[i] + "-" + outhouseId[i];
            outSourceChain[i] = outSourceChainMap.get(chainkey);
          }
        }
      }

      // TODO : sampNotoTypemap is creating for manual sample no, automated (Prefix based and Batch
      // based) sample no,
      // so it should be a common method for all in future

      if ((isAutoGenerateSampleReq.equals("N") && category.equals("DEP_LAB"))
          || (null != hisToken)) {
        sampNotoTypemap.putAll(createSampleTypeMap(testIds, sampleIds, sampleTypeIds));
        sampleNoToOrigSmplNoMap.putAll(createOrigSampleNoMap(testIds, sampleIds, origSampleNum));
        outSourceDestIdMap.putAll(createOutSourceDestMap(testIds, sampleIds, outhouseId));
      }
      String phoneNoCountryCode = PhoneNumberUtil.getCountryCode(phoneNo);
      if (phoneNoCountryCode == null) {
        phoneNoCountryCode = new CenterMasterDAO().getCountryCode(0);
      }

      for (int i = 0; i < testIds.length; i++) {

        if ((!visitMap.containsKey(pname)) && !isBillExists4LIS) {
          createVisitMap(pname, visitMap);
          labNOMap.putAll(createLabNo(category, pname));

          bills.addAll(createBillListBean(con, billType, centerId, pname, userName, billRatePlan,
              discAuth, visitMap));

          incomingMainList.addAll(createIncomingSampleBeanMain(pname, orginalLabId, phoneNo,
              phoneNoCountryCode, null, null, ageStr, patOtherInfo, genderValue, referralDocId,
              category, centerId, visitMap, bills, hisToken, hisPatientID, null, null));
        }

      }

      if (isBillExists4LIS) {
        if (null == patientDeets4LIS.get("bill_no")
            || ((String) patientDeets4LIS.get("bill_no")).equals("")) {
          throw new Exception("Bill not found in LIS for the patient " + pname);
        }
        visitMap.put(pname, (String) patientDeets4LIS.get("incoming_visit_id"));
        bills.add(patientDeets4LIS);
        labNOMap.put(pname, (String) patientDeets4LIS.get("labno"));
      }

      String[] batchBased = new String[testIds.length];
      StringBuilder batchBasedNo = new StringBuilder();

      // RC : this block should be changed to - getSampleIdGenerator().generate().
      if (null == hisToken) {
        if (isAutoGenerateSampleReq.equals("Y") && noGeneration.equals("P")
            && category.equals("DEP_LAB") && testIds != null) {
          sampleIds = new String[testIds.length];
          new PrefixSampleIdGenerator().generatePrefixBasedSampleId(con, testIds, isPackages,
              origSampleNum, sampleIds, sampleTypeIds, centerId, outSourceChain, outhouseId,
              sampNotoTypemap, sampleNoToOrigSmplNoMap, outSourceDestIdMap);

        } else if (isAutoGenerateSampleReq.equals("Y") && noGeneration.equals("B")
            && category.equals("DEP_LAB") && testIds != null) {
          sampleIds = new String[testIds.length];
          batchBased = new String[testIds.length];
          new BatchSampleIdGenerator().generateBatchBasedSampleId(con, testIds, isPackages,
              sampleIds, sampleTypeIds, batchBased, batchBasedNo, origSampleNum, outhouseId,
              sampNotoTypemap, sampleNoToOrigSmplNoMap, outSourceDestIdMap);
        }
      }

      // needs to be changed the data type
      Set<String> sampleSet = null;
      if (category.equals("DEP_LAB")) {
        if (isBatchBased) {
          sampleSet = getSampleSet(batchBased);
        } else {
          sampleSet = getSampleSet(sampleIds);
        }

        sampleBeanList.addAll(
            createSampleBean(sampleSet, sampleTypeIds, pname, isBatchBased, batchBasedNo.toString(),
                outhouseId, visitMap, sampNotoTypemap, sampleNotoSampleID, sampleNoToOrigSmplNoMap,
                outSourceDestIdMap, colSampleDate, isBillExists4LIS, sampleBean));
      }

      int nextPresId = 0;
      String chargeId = "";
      int packageActivityIndex = 0;

      for (int i = 0; i < testIds.length; i++) {

        Boolean isPackage = isPackages[i].equalsIgnoreCase("y");

        BasicDynaBean testMasterDetails = null;
        BasicDynaBean packMasterDetails = null;
        nextPresId = getPrescriptionId(isPackage);

        if (!isPackage) {
          incomingList.addAll(createIncommingSamplebean(testIds[i], pname, category,
              origSampleNum[i], visitMap, nextPresId));

          testPrescribedList.addAll(createTestPrescribedBean(con, testIds[i],
              ((null != outhouseId) ? outhouseId[i] : null), pname, userid, packageRefs[i],
              category, sampleIds[i], isBatchBased, houseType[i], centerId, batchBased[i], visitMap,
              labNOMap, sampleNotoSampleID, nextPresId, packageActivityIndex));
          testMasterDetails = diagDAO.findByKey(con, "test_id", testIds[i]);
          // RC : This method should be called outside the if / else
          if (testINpackage[i].equalsIgnoreCase("n")) {
            chargeId = new ChargeDAO(con).getNextChargeId();
            billCharges.addAll(createBillChargeBean(con, testIds[i], pname, isPackages[i],
                testMasterDetails, category, chargeStr[i], packMasterDetails, amtStr[i], discStr[i],
                userName, discountAuth, (conductingDoctorId != null ? conductingDoctorId[i] : null),
                bills, chargeId));
          }

          // RC : This method should be called outside the if / else
          billActivityCharges.addAll(createBillActivityBean(chargeId, isPackages[i], null,
              nextPresId, category, (conductingDoctorId != null ? conductingDoctorId[i] : null)));
        }
        if (isPackage) {
          // RC : createIncomingSamplebean() not required for isPackage() ?
          packagePrescribedList.addAll(createPackagePrescribedBean(con, pname, testIds[i], userName,
              visitMap, packMasterDetails, nextPresId));
          packMasterDetails = new GenericDAO("packages").findByKey(con, "package_id",
              Integer.parseInt(testIds[i]));

          // RC : This method should be called outside the if / else
          if (testINpackage[i].equalsIgnoreCase("n")) {
            chargeId = new ChargeDAO(con).getNextChargeId();
            billCharges.addAll(createBillChargeBean(con, testIds[i], pname, isPackages[i],
                testMasterDetails, category, chargeStr[i], packMasterDetails, amtStr[i], discStr[i],
                userName, discountAuth, (conductingDoctorId != null ? conductingDoctorId[i] : null),
                bills, chargeId));
          }

          // RC : This method should be called outside the if / else
          billActivityCharges.addAll(createBillActivityBean(chargeId, isPackages[i], nextPresId,
              null, category, (conductingDoctorId != null ? conductingDoctorId[i] : null)));
        }

        if (category.equals("DEP_LAB")) {
          if (houseType[i].equalsIgnoreCase("O") && outsourceDestType[i].equalsIgnoreCase("O")) {
            outhouseSampleBeanList.addAll(createOudHouseSampleBean(testIds[i], nextPresId, pname,
                outhouseId[i], category, sampleIds[i], visitMap));

          } else if (houseType[i].equalsIgnoreCase("O")
              && outsourceDestType[i].equalsIgnoreCase("C")) {

            createAutomaticSampleRegMap(Integer.parseInt(outhouseId[i]), testIds[i], nextPresId,
                sampleIds[i], sampleTypeIds[i], pname, visitMap, incomingSamplesRegMap);
          }
        }

      }

      if (incomingMainList.size() > 0) {
        new GenericDAO("incoming_sample_registration").insertAll(con, incomingMainList);
      }
      if (incomingList.size() > 0) {
        new GenericDAO("incoming_sample_registration_details").insertAll(con, incomingList);
      }
      if (sampleBeanList.size() > 0) {
        for (int i = 0; i < sampleBeanList.size(); i++) {
          BasicDynaBean samplecollBean = sampleBeanList.get(i);
          sampleCollectionDAO.insert(con, samplecollBean);
        }
      }
      if (testPrescribedList.size() > 0) {
        for (int i = 0; i < testPrescribedList.size(); i++) {
          BasicDynaBean testPrescBean = testPrescribedList.get(i);
          new GenericDAO("tests_prescribed").insert(con, testPrescBean);
        }
      }
      if (packagePrescribedList.size() > 0) {
        new GenericDAO("package_prescribed").insertAll(con, packagePrescribedList);
      }
      if (bills.size() > 0 && !isBillExists4LIS) {
        billDao.insertAll(con, bills);
      }

      if (billCharges.size() > 0) {
        new GenericDAO("bill_charge").insertAll(con, billCharges);
      }

      if (billActivityCharges.size() > 0) {
        new GenericDAO("bill_activity_charge").insertAll(con, billActivityCharges);
      }

      if (outhouseSampleBeanList.size() > 0) {
        new GenericDAO("outsource_sample_details").insertAll(con, outhouseSampleBeanList);
      }

      AutomaticSampleRegistration incomingSampleReg = new AutomaticSampleRegistration();
      if (incomingSamplesRegMap != null) {
        Set<String> keys = incomingSamplesRegMap.keySet();
        Iterator it = keys.iterator();
        while (it.hasNext()) {
          String key = (String) it.next();
          String[] splitParts = key.split(",");
          boolean status = incomingSampleReg.sampleRegistrationInInternalLab(con,
              incomingSamplesRegMap.get(key), splitParts[0], diagGenericPref);
          if (!status) {
            break;
          }
        }
      }

      ohPayment(con, testPrescribedList);
      con.commit();
      return visitMap;

    } catch (Exception exception) {
      logger.error(exception.toString(), exception);
      con.rollback();
      throw exception;
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }

  }

  /**
   * Gets the prescription id.
   *
   * @param isPackage the is package
   * @return the prescription id
   * @throws SQLException the SQL exception
   */
  public int getPrescriptionId(Boolean isPackage) throws SQLException {

    if (isPackage) {
      return com.bob.hms.common.DataBaseUtil.getNextSequence("package_prescribed_sequence");
    } else {
      return com.bob.hms.common.DataBaseUtil.getIntValueFromDb("select nextval('test_prescribed')");
    }
  }

}
