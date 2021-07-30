package com.insta.hms.diagnosticmodule.internallab;

import com.bob.hms.common.AutoIncrementId;
import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.Registration.PatientDetailsDAO;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.billing.Bill;
import com.insta.hms.billing.BillActivityCharge;
import com.insta.hms.billing.BillActivityChargeDAO;
import com.insta.hms.billing.BillDAO;
import com.insta.hms.billing.ChargeBO;
import com.insta.hms.billing.ChargeDAO;
import com.insta.hms.billing.ChargeDTO;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.diagnosticmodule.common.DiagnosticsDAO;
import com.insta.hms.diagnosticmodule.common.OutHouseSampleDetails;
import com.insta.hms.diagnosticmodule.laboratory.DeptTokenGeneratorDAO;
import com.insta.hms.diagnosticmodule.laboratory.LaboratoryBO;
import com.insta.hms.diagnosticmodule.laboratory.LaboratoryDAO;
import com.insta.hms.diagnosticmodule.laboratory.PendingSamplesDAO;
import com.insta.hms.diagnosticsmasters.addtest.AddTestDAOImpl;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.InComingHospitals.InComingHospitalDAO;
import com.insta.hms.master.SampleType.SampleTypeDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The Class AutomaticSampleRegistration.
 */
public class AutomaticSampleRegistration {

  /** The logger. */
  static Logger logger = LoggerFactory
      .getLogger(AutomaticSampleRegistration.class);
  
  private static final GenericDAO incomingSampleRegistrationDAO =
      new GenericDAO("incoming_sample_registration");
  private static final GenericDAO incomingSampleRegistrationDetailsDAO =
      new GenericDAO("incoming_sample_registration_details");
  private static final GenericDAO sampleCollectionDAO = new GenericDAO("sample_collection");
  private static final GenericDAO billChargeDAO = new GenericDAO("bill_charge");
  private static final GenericDAO testsPrescribedDAO = new GenericDAO("tests_prescribed");
  private static final GenericDAO billActivityChargeDAO = new GenericDAO("bill_activity_charge");
  private static final GenericDAO billDAO = new GenericDAO("bill");
  private static final GenericDAO diagnosticsDAO = new GenericDAO("diagnostics");
  
  /**
   * Sample registration in internal lab.
   *
   * @param con
   *          the con
   * @param lab
   *          the lab
   * @param visitId
   *          the visit id
   * @param diagGenericPref
   *          the diag generic pref
   * @return true, if successful
   * @throws Exception
   *           the exception
   * @throws SQLException
   *           the SQL exception
   */
  public boolean sampleRegistrationInInternalLab(Connection con, InternalLab lab, String visitId,
      BasicDynaBean diagGenericPref) throws Exception {
    boolean success = true;
    boolean reCondForInternalLabs = true;
    boolean reconductionStatus = true;
    BasicDynaBean visitCenterBean = VisitDetailsDAO.gettVisitCenterRelatedFields(con, visitId);

    String mrNo = (String) visitCenterBean.get("mr_no");
    String userName = RequestContext.getUserName();

    for (int i = 0; i < lab.getInternalLabPrescIds().length; i++) {
      String prescribedID = lab.getInternalLabPrescIds()[i];
      BasicDynaBean prescBean = null;
      BasicDynaBean prescReferenceBean = null;
      if (null != prescribedID && !prescribedID.equals("")) {
        prescBean = testsPrescribedDAO.findByKey(con, "prescribed_id",
            Integer.parseInt(prescribedID));
        prescReferenceBean = testsPrescribedDAO.findByKey(con, "prescribed_id",
            prescBean.get("reference_pres"));
        if (null != prescBean && ((Boolean) prescBean.get("re_conduction"))
            && null != prescReferenceBean.get("outsource_dest_prescribed_id")
            && !prescReferenceBean.get("outsource_dest_prescribed_id").equals("")) {

          reCondForInternalLabs &= true;
        } else {
          reCondForInternalLabs &= false;
        }
      }

    }

    reconductionStatus =
        automaticReconductionForInternalLabs(con, lab, userName, mrNo, visitId, diagGenericPref);

    if (reCondForInternalLabs) {
      return reconductionStatus;
    }

    if (!reconductionStatus) {
      return reconductionStatus;
    }

    String incomingVisitId = AutoIncrementId.getSequenceId("INHOUSE_VISIT_ID", "INHOUSEVISITID");
    int internalLabCenterId = Integer.parseInt(lab.getInternalLabCenterid());
    String billNo = BillDAO.getNextBillNo(con, Bill.BILL_TYPE_CREDIT, "t", "T", internalLabCenterId,
        false, false);

    BasicDynaBean sampleMainBean = incomingSampleRegistrationDAO.getBean();

    BasicDynaBean sampleBean = null;
    BasicDynaBean testPrescBean = null;
    BasicDynaBean incomingSampleBean = null;
    BasicDynaBean billchargeBean = null;
    BasicDynaBean billActivityBean = null;
    ArrayList<BasicDynaBean> testPrescBeanList = new ArrayList<>();
    ArrayList<BasicDynaBean> incomingsampleBeanList = new ArrayList<>();
    ArrayList<BasicDynaBean> billchargeBeanList = new ArrayList<>();
    ArrayList<BasicDynaBean> billactivityChargelist = new ArrayList<>();
    BasicDynaBean patientBean = null;
    BasicDynaBean incSampleRegBean = null;
    if (mrNo == null) {
      incSampleRegBean = incomingSampleRegistrationDAO.findByKey(con, "incoming_visit_id", visitId);
      sampleMainBean.set("patient_name", incSampleRegBean.get("patient_name"));
      sampleMainBean.set("patient_age", incSampleRegBean.get("patient_age"));
      sampleMainBean.set("age_unit", incSampleRegBean.get("age_unit"));
      sampleMainBean.set("patient_gender", incSampleRegBean.get("patient_gender"));
      sampleMainBean.set("phone_no", incSampleRegBean.get("phone_no"));
      sampleMainBean.set("phone_no_country_code", incSampleRegBean.get("phone_no_country_code"));
      sampleMainBean.set("referring_doctor", incSampleRegBean.get("referring_doctor"));
      sampleMainBean.set("isr_dateofbirth", incSampleRegBean.get("isr_dateofbirth"));
    } else {
      patientBean = PatientDetailsDAO.getPatientGeneralDetailsBean(con, mrNo);
      sampleMainBean.set("patient_name", patientBean.get("full_name"));
      sampleMainBean.set("patient_age", patientBean.get("age"));
      sampleMainBean.set("age_unit", patientBean.get("agein"));
      sampleMainBean.set("patient_gender", patientBean.get("patient_gender"));
      sampleMainBean.set("phone_no", patientBean.get("patient_phone"));
      sampleMainBean.set("phone_no_country_code", patientBean.get("patient_phone_country_code"));
      sampleMainBean.set("referring_doctor", patientBean.get("reference_docto_id"));
      sampleMainBean.set("isr_dateofbirth", patientBean.get("expected_dob"));
    }
    sampleMainBean.set("incoming_visit_id", incomingVisitId);
    sampleMainBean.set("billno", billNo);
    java.sql.Timestamp timestamp = new java.sql.Timestamp(new java.util.Date().getTime());
    sampleMainBean.set("date", timestamp);
    sampleMainBean.set("center_id", Integer.parseInt(lab.getInternalLabCenterid()));
    int visitCenterId = (Integer) visitCenterBean.get("center_id");
    sampleMainBean.set("source_center_id", visitCenterId);
    sampleMainBean.set("mr_no", mrNo);
    sampleMainBean.set("category", "DEP_LAB");

    String orginalLabId = null;
    InComingHospitalDAO dao = new InComingHospitalDAO();
    PendingSamplesDAO pendingSampleDao = new PendingSamplesDAO();
    BasicDynaBean bean = dao.getBean();
    String visitCenterName = (String) visitCenterBean.get("center_name");
    BasicDynaBean exists = dao.findByKey("hospital_name", visitCenterName);
    if (exists == null) {
      orginalLabId = dao.getNextHospitalId();
      bean.set("hospital_id", orginalLabId);
      bean.set("hospital_name", visitCenterName);
      bean.set("status", "A");
      success &= dao.insert(con, bean);
    } else {
      bean = dao.findByKey("hospital_name", visitCenterName);
      orginalLabId = (String) bean.get("hospital_id");
    }

    sampleMainBean.set("orig_lab_name", orginalLabId);
    sampleMainBean.set("incoming_source_type", "C");
    ArrayList<BasicDynaBean> sampleMainBeanList = new ArrayList<>();
    sampleMainBeanList.add(sampleMainBean);
    BasicDynaBean billBean = billDAO.getBean();
    billBean.set("bill_no", billNo);
    billBean.set("bill_type", Bill.BILL_TYPE_CREDIT);
    billBean.set("status", "A");
    billBean.set("restriction_type", "T");
    billBean.set("visit_id", incomingVisitId);
    billBean.set("open_date", timestamp);
    billBean.set("bill_rate_plan_id", "ORG0001");
    billBean.set("username", "InstaAdmin");
    billBean.set("opened_by", "InstaAdmin");
    billBean.set("visit_type", Bill.BILL_VISIT_TYPE_INCOMING);
    billBean.set("remarks", "Incoming sample registration");
    billBean.set("mod_time", timestamp);
    ArrayList<BasicDynaBean> billBeanList = new ArrayList<>();
    billBeanList.add(billBean);

    String[] testIds = lab.getInternalLabTestIds();
    String[] originalSampleNos = lab.getInternalLabSampleNos();
    String[] sampletypeIds = lab.getInternalLabSampleTypeIds();
    String[] testPrescIds = lab.getInternalLabPrescIds();
    List<Object> sampleNoList = new ArrayList<Object>();

    Map<String, String> testSampleMap = new HashMap<String, String>();
    Map<String, Integer> testSampleCollectionIdMap = new HashMap<String, Integer>();
    ArrayList<BasicDynaBean> sampleBeanList = new ArrayList<>();
    for (int i = 0; i < testIds.length; i++) {
      BasicDynaBean sendingCenterBean = testsPrescribedDAO.findByKey(con, "prescribed_id",
          Integer.parseInt(testPrescIds[i]));
      BasicDynaBean sendingCenterReferenceBean = testsPrescribedDAO.findByKey(con, "prescribed_id",
          sendingCenterBean.get("reference_pres"));
      if (!((Boolean) sendingCenterBean.get("re_conduction"))
          || null == sendingCenterReferenceBean.get("outsource_dest_prescribed_id")
          || sendingCenterReferenceBean.get("outsource_dest_prescribed_id").equals("")) {
        BasicDynaBean sendingSampleBean = sampleCollectionDAO.findByKey(con, "sample_collection_id",
            sendingCenterBean.get("sample_collection_id"));
        String origSampleNo = originalSampleNos[i];
        Timestamp sendingSampleDate = (Timestamp) sendingSampleBean.get("sample_date");
        String sampleNo = getSampleNo(con, sampletypeIds[i],
            Integer.parseInt(lab.getInternalLabCenterid()));
        if (!sampleNoList.contains(origSampleNo)) {
          sampleBean = sampleCollectionDAO.getBean();
          // Added sampletypeId also along with testId because
          // when we order same test twice and collecting sample with different sample types we have
          // an issue there
          testSampleMap.put(testIds[i] + "" + sampletypeIds[i], sampleNo);
          int sampleCollectionID = sampleCollectionDAO.getNextSequence();
          testSampleCollectionIdMap.put(testIds[i] + "" + sampletypeIds[i], sampleCollectionID);
          for (int j = i + 1; j < originalSampleNos.length; j++) {
            if (origSampleNo.equals(originalSampleNos[j])) {
              testSampleMap.put(testIds[j] + "" + sampletypeIds[j], sampleNo);
              testSampleCollectionIdMap.put(testIds[j] + "" + sampletypeIds[j], sampleCollectionID);
            }
          }
          sampleNoList.add(origSampleNo);
          sampleBean.set("sample_collection_id", sampleCollectionID);
          sampleBean.set("patient_id", incomingVisitId);
          sampleBean.set("sample_type_id", Integer.parseInt(sampletypeIds[i]));
          sampleBean.set("sample_sno", sampleNo);
          sampleBean.set("sample_status", "C");
          if (null != sendingSampleDate || !"".equals(sendingSampleDate)) {
            sampleBean.set("sample_date", sendingSampleDate);
          } else {
            sampleBean.set("sample_date", timestamp);
          }
          sampleBean.set("sample_receive_status", "P");
          sampleBean.set("sample_transfer_status", "P");
          sampleBean.set("sample_qty", sendingSampleBean.get("sample_qty"));
          sampleBean.set("orig_sample_no", sendingSampleBean.get("sample_sno"));
          String localColSampleNo = (String) sendingSampleBean.get("coll_sample_no");
          if (localColSampleNo == null || "".equals(localColSampleNo)) {
            sampleBean.set("coll_sample_no", (String) sendingSampleBean.get("sample_sno"));
          } else {
            sampleBean.set("coll_sample_no", localColSampleNo);
          }
          sampleBean.set("bed_id", sendingSampleBean.get("bed_id"));
          sampleBean.set("ward_id", sendingSampleBean.get("ward_id"));

          sampleBeanList.add(sampleBean);
        }
      }

    }

    BasicDynaBean testBean = null;
    HashMap<Integer, Set<Integer>> sampleOutSourceMap = new HashMap<Integer, Set<Integer>>();
    HashMap<Integer, Integer> totalTestsWithMultipleDestinations = new HashMap<Integer, Integer>();
    for (int i = 0; i < testIds.length; i++) {
      BasicDynaBean sendingCenterBean = testsPrescribedDAO.findByKey(con, "prescribed_id",
          Integer.parseInt(testPrescIds[i]));
      BasicDynaBean sendingCenterReferenceBean = testsPrescribedDAO.findByKey(con, "prescribed_id",
          sendingCenterBean.get("reference_pres"));
      Integer collPrescId = (Integer) sendingCenterBean.get("coll_prescribed_id");
      if (!((Boolean) sendingCenterBean.get("re_conduction"))
          || null == sendingCenterReferenceBean.get("outsource_dest_prescribed_id")
          || sendingCenterReferenceBean.get("outsource_dest_prescribed_id").equals("")) {

        testPrescBean = testsPrescribedDAO.getBean();
        incomingSampleBean = incomingSampleRegistrationDetailsDAO.getBean();
        billchargeBean = billChargeDAO.getBean();
        billActivityBean = billActivityChargeDAO.getBean();

        int prescId = DataBaseUtil.getIntValueFromDb("select nextval('test_prescribed')");
        testPrescBean.set("mr_no", mrNo);
        testPrescBean.set("prescribed_id", prescId);
        testPrescBean.set("test_id", testIds[i]);
        testPrescBean.set("pat_id", incomingVisitId);
        testPrescBean.set("exp_rep_ready_time", sendingCenterBean.get("exp_rep_ready_time"));
        testBean = diagnosticsDAO.findByKey("test_id", testIds[i]);
        if (testBean.get("conduction_applicable").toString().equals("false")) {
          testPrescBean.set("conducted", "U");
        } else {
          if (testBean.get("results_entry_applicable").toString().equals("false")) {
            testPrescBean.set("conducted", "NRN");
          } else {
            testPrescBean.set("conducted", "N");
          }
        }
        testPrescBean.set("pres_date", timestamp);
        testPrescBean.set("sflag", "1");
        testPrescBean.set("user_name", "InstaAdmin");
        testPrescBean.set("prescription_type", "i");
        testPrescBean.set("sample_no", testSampleMap.get(testIds[i] + "" + sampletypeIds[i]));
        testPrescBean.set("sample_collection_id",
            testSampleCollectionIdMap.get(testIds[i] + "" + sampletypeIds[i]));
        testPrescBean.set("priority", sendingCenterBean.get("priority"));
        testPrescBean.set("source_test_prescribed_id", sendingCenterBean.get("prescribed_id"));
        testPrescBean.set("curr_location_presc_id", prescId);
        if (collPrescId == null) {
          testPrescBean.set("coll_prescribed_id", sendingCenterBean.get("prescribed_id"));
        } else {
          testPrescBean.set("coll_prescribed_id", collPrescId);
        }

        if (diagGenericPref.get("autogenerate_labno").equals("Y")) {
          testPrescBean.set("labno", DiagnosticsDAO.getNextSequenceNo("LABNO"));
        }
        String testDeptId = (String) ((BasicDynaBean) AddTestDAOImpl.getTestBean(testIds[i]))
            .get("ddept_id");
        if (diagGenericPref.get("gen_token_for_lab").equals("Y")) {
          testPrescBean.set("token_number", DeptTokenGeneratorDAO.getToken(testDeptId,
              Integer.parseInt(lab.getInternalLabCenterid())));
        }

        BasicDynaBean outsourceAssociation = pendingSampleDao.isNextOutsourceAssociated(testIds[i],
            internalLabCenterId);
        int sampleCollectionID = (Integer) testPrescBean.get("sample_collection_id");
        Set<Integer> outsourceDestIds = sampleOutSourceMap.get(sampleCollectionID);
        if (outsourceDestIds == null) {
          outsourceDestIds = new HashSet<Integer>();
        }
        if (outsourceAssociation != null) {
          String[] outsources = outsourceAssociation.get("outsource_dest_ids").toString()
              .split(", ");
          if (outsources.length > 1) {
            Integer lastCount = totalTestsWithMultipleDestinations.get(sampleCollectionID);
            if (lastCount == null) {
              totalTestsWithMultipleDestinations.put(sampleCollectionID, 1);
            } else {
              totalTestsWithMultipleDestinations.put(sampleCollectionID, lastCount + 1);
            }
          } else {
            Integer outsourceDestinationID = Integer.parseInt(outsources[0]);
            outsourceDestIds.add(outsourceDestinationID);
          }
          testPrescBean.set("conduction_type", "o");
        } else {
          testPrescBean.set("conduction_type", "i");
          outsourceDestIds.add(-1);
        }
        sampleOutSourceMap.put(sampleCollectionID, outsourceDestIds);

        testPrescBeanList.add(testPrescBean);
        incomingSampleBean.set("incoming_visit_id", incomingVisitId);
        incomingSampleBean.set("test_id", testIds[i]);
        incomingSampleBean.set("orig_sample_no", originalSampleNos[i]);
        incomingSampleBean.set("prescribed_id", prescId);
        incomingSampleBean.set("source_test_prescribed", Integer.parseInt(testPrescIds[i]));

        incomingsampleBeanList.add(incomingSampleBean);

        BasicDynaBean parentTestPrescribedBean = testsPrescribedDAO.findByKey(con, "prescribed_id",
            Integer.parseInt(testPrescIds[i]));
        parentTestPrescribedBean.set("outsource_dest_prescribed_id", prescId);
        if (parentTestPrescribedBean != null
            && parentTestPrescribedBean.get("conduction_type").equals("i")) {
          parentTestPrescribedBean.set("conduction_type", "o");
        }

        success &= testsPrescribedDAO.updateWithNames(con, parentTestPrescribedBean.getMap(),
            new String[] { "prescribed_id" }) > 0;

        Map<String, Object> keys = new HashMap<>();
        int outSourceDestId = (Integer) new GenericDAO("diag_outsource_master")
            .findByKey("outsource_dest", lab.getInternalLabCenterid()).get("outsource_dest_id");
        keys.put("outsource_dest_id", outSourceDestId);
        keys.put("test_id", testIds[i]);
        keys.put("source_center_id", visitCenterId);
        BasicDynaBean chargeBean = (BasicDynaBean) new GenericDAO("diag_outsource_detail")
            .findByKey(keys);
        BigDecimal charge = null;
        if (null != chargeBean && chargeBean.get("charge") != null) {
          charge = (BigDecimal) chargeBean.get("charge");
        } else {
          charge = new BigDecimal(0);
        }

        billchargeBean.set("bill_no", billNo);
        String chargeId = new ChargeDAO(con).getNextChargeId();
        billchargeBean.set("charge_id", chargeId);
        billchargeBean.set("charge_group", ChargeDTO.CG_DIAGNOSTICS);
        billchargeBean.set("charge_head", ChargeDTO.CH_DIAG_LAB);
        BasicDynaBean testMasterDetails = diagnosticsDAO.findByKey(con, "test_id",
            testIds[i]);
        billchargeBean.set("act_department_id", testMasterDetails.get("ddept_id"));
        billchargeBean.set("act_description", testMasterDetails.get("test_name"));
        billchargeBean.set("act_remarks", "incoming sample reg..");
        billchargeBean.set("service_sub_group_id",
            (Integer) (testMasterDetails.get("service_sub_group_id")));
        billchargeBean.set("act_rate", charge);
        billchargeBean.set("orig_rate", charge);
        billchargeBean.set("act_unit", null);
        billchargeBean.set("act_quantity", new BigDecimal(1));
        billchargeBean.set("amount", charge);
        billchargeBean.set("paid_amount", new BigDecimal(0));
        billchargeBean.set("discount", new BigDecimal(0));
        billchargeBean.set("posted_date", timestamp);
        billchargeBean.set("status", "A");
        billchargeBean.set("username", "InstaAdmin");
        billchargeBean.set("mod_time", timestamp);
        billchargeBean.set("hasactivity", true);
        billchargeBean.set("act_description_id", testIds[i]);
        billchargeBeanList.add(billchargeBean);

        billActivityBean.set("charge_id", chargeId);
        billActivityBean.set("activity_code", "DIA");
        billActivityBean.set("activity_id", new Integer(prescId).toString());
        billActivityBean.set("payment_charge_head", "LTDIA");

        billactivityChargelist.add(billActivityBean);
      }
    }

    if (!sampleMainBeanList.isEmpty()) {
      success &= incomingSampleRegistrationDAO.insertAll(con, sampleMainBeanList);
    }
    if (!billBeanList.isEmpty()) {
      success &= billDAO.insertAll(con, billBeanList);
    }
    if (!sampleBeanList.isEmpty()) {
      for (BasicDynaBean sampleBeanElement : sampleBeanList) {
        Integer localSampleCollectionID = (Integer) sampleBeanElement.get("sample_collection_id");
        Integer totalOutSources = totalOutsourcesForSample(sampleOutSourceMap,
            totalTestsWithMultipleDestinations, localSampleCollectionID);

        if (totalOutSources > 1) {
          sampleBeanElement.set("sample_split_status", "P");
        } else {
          sampleBeanElement.set("sample_split_status", "N");
        }
      }
      success &= sampleCollectionDAO.insertAll(con, sampleBeanList);
    }
    if (testPrescBeanList.size() > 0) {
      /*
       * Set outsource_dest_id only when: 1) all tests in sample are going to same location. 2) no
       * test in sample is going to more than one location.
       */
      for (BasicDynaBean testPresBean : testPrescBeanList) {
        int sampleCollectionID = (Integer) testPresBean.get("sample_collection_id");
        Set<Integer> outsources = sampleOutSourceMap.get(sampleCollectionID);
        Integer testToMultipleOutsourceTotal = totalTestsWithMultipleDestinations
            .get(sampleCollectionID);
        if (testToMultipleOutsourceTotal != null && testToMultipleOutsourceTotal > 0) {
          testPresBean.set("outsource_dest_id", null);
        } else if (outsources != null && outsources.size() == 1) {
          Iterator<Integer> outsourcesIterator = sampleOutSourceMap.get(sampleCollectionID)
              .iterator();
          Integer outsourceDestinationID = outsourcesIterator.next();
          if (outsourceDestinationID != -1) {
            testPresBean.set("outsource_dest_id", outsourceDestinationID);
          } else {
            testPresBean.set("outsource_dest_id", null);
          }
        } else {
          testPresBean.set("outsource_dest_id", null);
        }
      }
      success &= LaboratoryBO.copyDataToMultipleChains(con, testPrescBeanList, null,
          "source_test_prescribed_id", Arrays.asList(new String[] { "curr_location_presc_id" }));
      success &= testsPrescribedDAO.insertAll(con, testPrescBeanList);
      // update outsource_sample_details and payments while incoming sample registration.
      success &= insertoutsourceSampleDetails(con, lab, visitId);
    }
    if (!incomingsampleBeanList.isEmpty()) {
      success &= incomingSampleRegistrationDetailsDAO.insertAll(con,
          incomingsampleBeanList);
    }
    if (!billchargeBeanList.isEmpty()) {
      success &= billChargeDAO.insertAll(con, billchargeBeanList);
    }
    if (!billactivityChargelist.isEmpty()) {
      success &= billActivityChargeDAO.insertAll(con, billactivityChargelist);
    }

    return success;
  }

  /**
   * Gets the sample no.
   *
   * @param con
   *          the con
   * @param sampleTypeId
   *          the sample type id
   * @param centerID
   *          the center ID
   * @return the sample no
   * @throws SQLException
   *           the SQL exception
   * @throws Exception
   *           the exception
   */
  public String getSampleNo(Connection con, String sampleTypeId, Integer centerID)
      throws SQLException, Exception {
    String sampleNo = null;
    String noGeneration = (String) GenericPreferencesDAO.getdiagGenericPref()
        .get("sample_no_generation");
    SampleTypeDAO sampleTypeDao = new SampleTypeDAO();
    if (noGeneration.equals("P")) {
      sampleNo = SampleTypeDAO.getNextSampleNumber(Integer.parseInt(sampleTypeId), centerID);
    } else if (noGeneration.equals("B")) {
      sampleNo = sampleTypeDao.getBatchBasedSampleNo(con);
    }
    return sampleNo;
  }

  /**
   * Automatic reconduction for internal labs.
   *
   * @param con
   *          the con
   * @param lab
   *          the lab
   * @param userName
   *          the user name
   * @param mrNo
   *          the mr no
   * @param visitId
   *          the visit id
   * @param diagGenericPref
   *          the diag generic pref
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws Exception
   *           the exception
   */
  private boolean automaticReconductionForInternalLabs(Connection con, InternalLab lab,
      String userName, String mrNo, String visitId, BasicDynaBean diagGenericPref)
      throws SQLException, IOException, Exception {
    boolean success = true;
    List<Object> sampleNoList = new ArrayList<>();

    Map<String, String> testSampleMap = new HashMap<>();
    Map<String, Integer> testSampleCollectionIdMap = new HashMap<>();
    BasicDynaBean sampleBean = null;
    BasicDynaBean testPrescBean = null;

    ArrayList<BasicDynaBean> sampleBeanList = new ArrayList<>();
    ArrayList<BasicDynaBean> testPrescBeanList = new ArrayList<>();
    Map<String, Object> keys = new HashMap<>();
    java.sql.Timestamp timestamp = new java.sql.Timestamp(new java.util.Date().getTime());
    Map<String, Object> inRegKeys = new HashMap<>();
    List<String> inRegColumns = new ArrayList<>();
    PendingSamplesDAO pendingSampleDao = new PendingSamplesDAO();
    inRegColumns.add("source_test_prescribed");
    inRegColumns.add("prescribed_id");
    inRegColumns.add("orig_sample_no");
    List<String> columns = new ArrayList<>();
    columns.add("outsource_dest_prescribed_id");

    String[] testIds = lab.getInternalLabTestIds();
    String[] originalSampleNos = lab.getInternalLabSampleNos();
    String[] sampletypeIds = lab.getInternalLabSampleTypeIds();
    String[] testPrescIds = lab.getInternalLabPrescIds();
    for (int i = 0; i < testIds.length; i++) {
      String origSampleNo = originalSampleNos[i];

      BasicDynaBean sendingCenterBean = testsPrescribedDAO.findByKey(con, "prescribed_id",
          Integer.parseInt(testPrescIds[i]));
      BasicDynaBean sendingCenterReferenceBean = testsPrescribedDAO.findByKey(con, "prescribed_id",
          sendingCenterBean.get("reference_pres"));
      if (((Boolean) sendingCenterBean.get("re_conduction"))
          && null != sendingCenterReferenceBean.get("outsource_dest_prescribed_id")
          && !sendingCenterReferenceBean.get("outsource_dest_prescribed_id").equals("")) {
        BasicDynaBean sendingSampleBean = sampleCollectionDAO.findByKey(con,
            "sample_collection_id", sendingCenterBean.get("sample_collection_id"));
        BasicDynaBean receivingCenterBean = testsPrescribedDAO.findByKey(con, "prescribed_id",
            sendingCenterReferenceBean.get("outsource_dest_prescribed_id"));
        String thisColSampleNo = (String) sendingSampleBean.get("coll_sample_no");
        Timestamp sendingSampleDate = (Timestamp) sendingSampleBean.get("sample_date");
        if (!sampleNoList.contains(origSampleNo)) {
          String sampleNo = getSampleNo(con, sampletypeIds[i],
              Integer.parseInt(lab.getInternalLabCenterid()));
          sampleBean = sampleCollectionDAO.getBean();
          testSampleMap.put(testIds[i], sampleNo);
          int sampleCollectionID = sampleCollectionDAO.getNextSequence();
          testSampleCollectionIdMap.put(testIds[i], sampleCollectionID);
          for (int j = i + 1; j < originalSampleNos.length; j++) {
            if (origSampleNo.equals(originalSampleNos[j])) {
              testSampleMap.put(testIds[j], sampleNo);
              testSampleCollectionIdMap.put(testIds[j], sampleCollectionID);
            }
          }
          sampleNoList.add(origSampleNo);
          sampleBean.set("sample_collection_id", sampleCollectionID);
          sampleBean.set("patient_id", receivingCenterBean.get("pat_id"));
          sampleBean.set("sample_type_id", Integer.parseInt(sampletypeIds[i]));
          sampleBean.set("sample_sno", sampleNo);
          sampleBean.set("sample_status", "C");
          if (null != sendingSampleDate || !"".equals(sendingSampleDate)) {
            sampleBean.set("sample_date", sendingSampleDate);
          } else {
            sampleBean.set("sample_date", timestamp);
          }
          sampleBean.set("sample_receive_status", "P");
          sampleBean.set("sample_transfer_status", "P");
          sampleBean.set("sample_qty", sendingSampleBean.get("sample_qty"));
          sampleBean.set("orig_sample_no", sendingSampleBean.get("sample_sno"));
          if (thisColSampleNo == null || "".equals(thisColSampleNo)) {
            sampleBean.set("coll_sample_no", sendingSampleBean.get("sample_sno"));
          } else {
            sampleBean.set("coll_sample_no", thisColSampleNo);
          }

          sampleBeanList.add(sampleBean);
        }
      }

    }

    HashMap<Integer, Set<Integer>> sampleOutSourceMap = new HashMap<Integer, Set<Integer>>();
    HashMap<Integer, Integer> totalTestsWithMultipleDestinations = new HashMap<Integer, Integer>();
    for (int i = 0; i < testIds.length; i++) {

      BasicDynaBean sendingCenterBean = testsPrescribedDAO.findByKey(con, "prescribed_id",
          Integer.parseInt(testPrescIds[i]));
      BasicDynaBean sendingCenterReferenceBean = testsPrescribedDAO.findByKey(con, "prescribed_id",
          sendingCenterBean.get("reference_pres"));
      Integer collPrescId = (Integer) sendingCenterBean.get("coll_prescribed_id");
      if (((Boolean) sendingCenterBean.get("re_conduction"))
          && null != sendingCenterReferenceBean.get("outsource_dest_prescribed_id")
          && !sendingCenterReferenceBean.get("outsource_dest_prescribed_id").equals("")) {
        testPrescBean = testsPrescribedDAO.getBean();

        int prescId = DataBaseUtil.getIntValueFromDb("select nextval('test_prescribed')");
        testPrescBean.set("prescribed_id", prescId);
        testPrescBean.set("mr_no", mrNo);
        testPrescBean.set("test_id", testIds[i]);
        BasicDynaBean receivingCenterBean = testsPrescribedDAO.findByKey(con, "prescribed_id",
            sendingCenterReferenceBean.get("outsource_dest_prescribed_id"));
        testPrescBean.set("pat_id", receivingCenterBean.get("pat_id"));
        testPrescBean.set("conducted", "N");
        testPrescBean.set("pres_date", timestamp);
        testPrescBean.set("sflag", "1");
        testPrescBean.set("user_name", "InstaAdmin");
        testPrescBean.set("prescription_type", "i");
        testPrescBean.set("sample_no", testSampleMap.get(testIds[i]));
        testPrescBean.set("sample_collection_id", testSampleCollectionIdMap.get(testIds[i]));
        testPrescBean.set("labno", receivingCenterBean.get("labno"));
        testPrescBean.set("re_conduction", true);
        testPrescBean.set("reference_pres",
            new BigDecimal((Integer) receivingCenterBean.get("prescribed_id")));
        testPrescBean.set("source_test_prescribed_id", sendingCenterBean.get("prescribed_id"));
        testPrescBean.set("curr_location_presc_id", prescId);
        if (collPrescId == null) {
          testPrescBean.set("coll_prescribed_id", sendingCenterBean.get("prescribed_id"));
        } else {
          testPrescBean.set("coll_prescribed_id", collPrescId);
        }

        String testDeptId = (String) ((BasicDynaBean) AddTestDAOImpl.getTestBean(testIds[i]))
            .get("ddept_id");
        if (diagGenericPref.get("gen_token_for_lab").equals("Y")) {
          testPrescBean.set("token_number", DeptTokenGeneratorDAO.getToken(testDeptId,
              Integer.parseInt(lab.getInternalLabCenterid())));
        }

        BasicDynaBean outsourceAssociation = pendingSampleDao.isNextOutsourceAssociated(testIds[i],
            Integer.parseInt(lab.getInternalLabCenterid()));
        int sampleCollectionID = (Integer) testPrescBean.get("sample_collection_id");
        Set<Integer> outsourceDestIds = sampleOutSourceMap.get(sampleCollectionID);
        if (outsourceDestIds == null) {
          outsourceDestIds = new HashSet<Integer>();
        }
        if (outsourceAssociation != null) {
          String[] outsources = outsourceAssociation.get("outsource_dest_ids").toString()
              .split(", ");
          if (outsources.length > 1) {
            Integer lastCount = totalTestsWithMultipleDestinations.get(sampleCollectionID);
            if (lastCount == null) {
              totalTestsWithMultipleDestinations.put(sampleCollectionID, 1);
            } else {
              totalTestsWithMultipleDestinations.put(sampleCollectionID, lastCount + 1);
            }
          } else {
            outsourceDestIds.add(Integer.parseInt(outsources[0]));
          }
          testPrescBean.set("conduction_type", "o");
        } else {
          testPrescBean.set("conduction_type", "i");
          outsourceDestIds.add(-1);
        }
        sampleOutSourceMap.put(sampleCollectionID, outsourceDestIds);

        BasicDynaBean inRegDetailsBean = incomingSampleRegistrationDetailsDAO.getBean();
        inRegDetailsBean.set("source_test_prescribed", Integer.parseInt(testPrescIds[i]));
        inRegDetailsBean.set("prescribed_id", prescId);
        inRegDetailsBean.set("orig_sample_no", originalSampleNos[i]);

        inRegKeys.put("incoming_visit_id", receivingCenterBean.get("pat_id"));
        inRegKeys.put("test_id", testIds[i]);
        inRegKeys.put("prescribed_id", receivingCenterBean.get("prescribed_id"));

        success = incomingSampleRegistrationDetailsDAO.update(con, inRegColumns,
            inRegDetailsBean.getMap(), inRegKeys) > 0;

        sendingCenterBean.set("outsource_dest_prescribed_id", prescId);
        if (sendingCenterBean != null && sendingCenterBean.get("conduction_type").equals("i")) {
          sendingCenterBean.set("conduction_type", "o");
        }

        testPrescBeanList.add(testPrescBean);
        keys.put("prescribed_id", sendingCenterBean.get("prescribed_id"));
        success &= testsPrescribedDAO.update(con, columns, sendingCenterBean.getMap(), keys) > 0;

        success &= new BillActivityChargeDAO(con).updateActivityId(
            receivingCenterBean.get("prescribed_id").toString(),
            BillActivityCharge.DIAG_ACTIVITY_CODE, String.valueOf(prescId));

        // update activity_conducted to N in bill_charge,bill_activity_charge
        BillActivityChargeDAO.updateActivityDetails(con, BillActivityCharge.DIAG_ACTIVITY_CODE,
            String.valueOf(prescId), null, "N", null,userName);
      }

    }

    if (sampleBeanList.size() > 0) {
      for (BasicDynaBean sampleBeanElement : sampleBeanList) {
        Integer thisSampleCollectionID = (Integer) sampleBeanElement.get("sample_collection_id");
        Integer thisTotalOutSources = totalOutsourcesForSample(sampleOutSourceMap,
            totalTestsWithMultipleDestinations, thisSampleCollectionID);

        if (thisTotalOutSources > 1) {
          sampleBeanElement.set("sample_split_status", "P");
        } else {
          sampleBeanElement.set("sample_split_status", "N");
        }
      }
      success &= sampleCollectionDAO.insertAll(con, sampleBeanList);
    }
    if (testPrescBeanList.size() > 0) {
      for (BasicDynaBean testPresBean : testPrescBeanList) {
        int sampleCollectionID = (Integer) testPresBean.get("sample_collection_id");
        Set<Integer> outsources = sampleOutSourceMap.get(sampleCollectionID);
        Integer testToMultipleOutsourceTotal = totalTestsWithMultipleDestinations
            .get(sampleCollectionID);
        if (testToMultipleOutsourceTotal != null && testToMultipleOutsourceTotal > 0) {
          testPresBean.set("outsource_dest_id", null);
        } else if (outsources != null && outsources.size() == 1) {
          Iterator<Integer> outsourcesIterator = sampleOutSourceMap.get(sampleCollectionID)
              .iterator();
          Integer outsourceDestinationID = outsourcesIterator.next();
          if (outsourceDestinationID != -1) {
            testPresBean.set("outsource_dest_id", outsourceDestinationID);
          } else {
            testPresBean.set("outsource_dest_id", null);
          }
        } else {
          testPresBean.set("outsource_dest_id", null);
        }
      }
      success &= LaboratoryBO.copyDataToMultipleChains(con, testPrescBeanList, null,
          "source_test_prescribed_id", Arrays.asList(new String[] { "curr_location_presc_id" }));
      success &= testsPrescribedDAO.insertAll(con, testPrescBeanList);
      // update outsource_sample_details and payments while incoming sample registration.
      success &= insertoutsourceSampleDetails(con, lab, visitId);
    }

    return success;
  }

  /**
   * Total outsources for sample.
   *
   * @param sampleOutsourceMap
   *          the sample outsource map
   * @param testToMultipleDestinationsCountMap
   *          the test to multiple destinations count map
   * @param sampleCollectionID
   *          the sample collection ID
   * @return the int
   */
  private int totalOutsourcesForSample(HashMap<Integer, Set<Integer>> sampleOutsourceMap,
      HashMap<Integer, Integer> testToMultipleDestinationsCountMap, int sampleCollectionID) {
    int totalOutsources = 0;
    Set<Integer> outsources = sampleOutsourceMap.get(sampleCollectionID);
    if (outsources != null) {
      totalOutsources += outsources.size();
    }

    Integer testToMultipleOutsourceTotal = testToMultipleDestinationsCountMap
        .get(sampleCollectionID);
    if (testToMultipleOutsourceTotal != null) {
      totalOutsources += testToMultipleOutsourceTotal;
    }

    return totalOutsources;
  }

  /**
   * Insertoutsource sample details.
   *
   * @param con
   *          the con
   * @param lab
   *          the lab
   * @param visitId
   *          the visit id
   * @return true, if successful
   * @throws Exception
   *           the exception
   */
  private boolean insertoutsourceSampleDetails(Connection con, InternalLab lab, String visitId)
      throws Exception {

    boolean success = true;
    OutHouseSampleDetails ohDetails = new OutHouseSampleDetails();
    ChargeBO chargeBo = new ChargeBO();
    String[] prescIds = lab.getInternalLabPrescIds();
    String[] sampleNos = lab.getInternalLabSampleNos();
    String[] testIds = lab.getInternalLabTestIds();
    BasicDynaBean visitCenterBean = VisitDetailsDAO.gettVisitCenterRelatedFields(con, visitId);
    int visitCenterId = (Integer) visitCenterBean.get("center_id");

    for (int m = 0; m < prescIds.length; m++) {
      BasicDynaBean outHouseSampleBean = new GenericDAO("outsource_sample_details").findByKey(con,
          "prescribed_id", Integer.parseInt(prescIds[m]));
      BasicDynaBean testPrescBean = testsPrescribedDAO.findByKey(con,
          "prescribed_id", Integer.parseInt(prescIds[m]));
      Integer outSourceDestId = (Integer) testPrescBean.get("outsource_dest_id");

      if (outHouseSampleBean == null) {
        ohDetails.setVisitId(visitId);
        ohDetails.setPrescribedId(Integer.parseInt(prescIds[m]));
        ohDetails.setSampleNo(sampleNos[m]);
        ohDetails.setTestId(testIds[m]);
        ohDetails.setoutSourceId(outSourceDestId.toString());
        success &= LaboratoryDAO.setSamplesToOuthouse(ohDetails, con);
        if (success) {
          String chargeId = LaboratoryDAO.getOhTestChargeId(con, ohDetails.getPrescribedId(),
              ChargeDTO.CH_DIAG_LAB);
          success &= chargeBo.updateOhPayment(con, chargeId, visitCenterId);
        }
      }
    }

    return success;
  }

}