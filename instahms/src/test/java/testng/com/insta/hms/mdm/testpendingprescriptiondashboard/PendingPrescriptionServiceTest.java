package testng.com.insta.hms.mdm.testpendingprescriptiondashboard;

import com.insta.hms.common.preferences.genericpreferences.GenericPreferencesService;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.core.clinical.consultation.prescriptions.InvestigationPrescriptionsRepository;
import com.insta.hms.core.clinical.consultation.prescriptions.PendingPrescriptionsRemarksRepository;
import com.insta.hms.core.clinical.consultation.prescriptions.PendingPrescriptionsRepository;
import com.insta.hms.core.clinical.consultation.prescriptions.PendingPrescriptionsService;
import com.insta.hms.core.clinical.consultation.prescriptions.PrescriptionsRepository;
import com.insta.hms.core.patient.followupdetails.FollowUpRepository;
import com.insta.hms.core.scheduler.AppointmentRepository;
import com.insta.hms.mdm.departments.DepartmentRepository;
import com.insta.hms.mdm.diagdepartments.DiagDepartmentRepository;
import com.insta.hms.mdm.diagnostics.DiagnosticTestRepository;
import com.insta.hms.mdm.doctors.DoctorRepository;
import com.insta.hms.mdm.hospitalroles.HospitalRoleRepository;
import com.insta.hms.mdm.prescriptionsdeclinedreasonmaster.PrescDeclinedReasonRepository;
import com.insta.hms.mdm.servicedepartments.ServiceDepartmentsRepository;
import com.insta.hms.mdm.services.ServicesRepository;
import com.insta.hms.mdm.tpas.TpaRepository;
import com.insta.hms.security.usermanager.UserRepository;
import com.insta.hms.security.usermanager.UserService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTransactionalTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.sql.Date;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import testng.utils.TestRepoInit;

@Test
@ContextConfiguration(locations = { "classpath:spring/test-spring-config.xml" })
public class PendingPrescriptionServiceTest extends AbstractTransactionalTestNGSpringContextTests {
  private Logger logger = LoggerFactory.getLogger(PendingPrescriptionServiceTest.class);

  @Spy
  @InjectMocks
  private PendingPrescriptionsService pendingPrescriptionsService;

  @Spy
  @InjectMocks
  private PendingPrescriptionsRepository pendingPrescriptionsRepository;

  @Mock
  private SessionService sessionService;

  @Spy
  @InjectMocks
  private AppointmentRepository appointmentRepository;

  @Spy
  @InjectMocks
  private UserService userService;

  @Spy
  @InjectMocks
  private UserRepository userRepository;

  @Spy
  @InjectMocks
  private DoctorRepository doctorRepository;

  @Spy
  @InjectMocks
  private DiagnosticTestRepository diagnosticTestRepository;

  @Spy
  @InjectMocks
  private DiagDepartmentRepository diagDepartmentRepository;

  @Spy
  @InjectMocks
  private ServiceDepartmentsRepository serviceDepartmentsRepository;

  @Spy
  @InjectMocks
  private ServicesRepository servicesRepository;

  @Spy
  @InjectMocks
  private DepartmentRepository departmentRepository;

  @Spy
  @InjectMocks
  private InvestigationPrescriptionsRepository investigationPrescriptionsRepository;

  @Spy
  @InjectMocks
  private TpaRepository tpaRepository;

  @Spy
  @InjectMocks
  private GenericPreferencesService genPrefService;

  @Spy
  @InjectMocks
  private PendingPrescriptionsRemarksRepository pendingPrescriptionsRemarksRepository;

  @Spy
  @InjectMocks
  private HospitalRoleRepository hospitalRoleRepository;

  @Spy
  @InjectMocks
  private PrescDeclinedReasonRepository prescDeclinedReasonRepository;

  @Spy
  @InjectMocks
  private PrescriptionsRepository presRepo;

  @Spy
  @InjectMocks
  private FollowUpRepository followUpRepository;

  @BeforeClass
  public void mockData() {
    logger.info("Before PendingPrescriptionServiceTest");
    MockitoAnnotations.initMocks(this);
    TestRepoInit testRepo = new TestRepoInit();
    testRepo.insert("modules_activated");
    testRepo.insert("patient_details");
    testRepo.insert("patient_registration");
    testRepo.insert("doctor_consultation");
    testRepo.insert("patient_prescription");
    testRepo.insert("patient_test_prescriptions");
    testRepo.insert("patient_service_prescriptions");
    testRepo.insert("patient_consultation_prescriptions");
    testRepo.insert("follow_up_details");
    testRepo.insert("patient_pending_prescriptions");
    testRepo.insert("pending_prescription_details");
    testRepo.insert("diagnostics_departments");
    testRepo.insert("doctors");
    testRepo.insert("diagnostics");
    testRepo.insert("services");
    testRepo.insert("insurance_company_master");
    testRepo.insert("tpa_master");
    testRepo.initializeRepo();
    logger.info("Complted setting up data for PendingPrescriptionServiceTest");
  }

  @SuppressWarnings("unchecked")
  @Test
  public void getPendingPrescriptionsTest() throws ParseException {
    logger.info("Pending Prescription Service Test Start");
    Map<String, String[]> requestMap = new HashMap<String, String[]>();
    String[] centerId = { "0" };
    requestMap.put("center_id", centerId);
    String[] mrNo = { "MR013905" };
    requestMap.put("mr_no", mrNo);
    String[] tpaId = { "TPAID0100" };
    requestMap.put("tpa_id", tpaId);
    String[] prescribedFromDate = { "23-07-2019" };
    requestMap.put("prescribed_from_date", prescribedFromDate);
    String[] prescribedToDate = { "27-07-2019" };
    requestMap.put("prescribed_to_date", prescribedToDate);
    String[] prescribedBy = { "DOC0115" };
    requestMap.put("prescribed_by", prescribedBy);
    String[] prescriptionType = {"Lab,Rad,Ser,Dep,Ref,Followup"};
    requestMap.put("prescription_type", prescriptionType);
    String[] pageSize = { "10" };
    requestMap.put("page_size", pageSize);
    String[] pageNum = { "1" };
    requestMap.put("page_num", pageNum);
    Mockito.when(sessionService.getSessionAttributes()).thenReturn(dummySessionObject());
    Map<String, Object> pendPresc = pendingPrescriptionsService.getPendingPrescriptions(requestMap);
    if (pendPresc != null) {
      List<Map<String, Object>> mapList = (List<Map<String, Object>>) pendPresc
          .get("prescriptions");
      Assert.assertEquals("9", pendPresc.get("record_count").toString());
      Assert.assertEquals("1", pendPresc.get("num_pages").toString());
      for (Map<String, Object> res : mapList) {
        Assert.assertEquals("MR013905", res.get("mr_no").toString());
        Assert.assertEquals("TPAID0100", res.get("pri_sponsor_id").toString());
        Assert.assertEquals("2019-07-25", res.get("prescription_date").toString());
        Assert.assertEquals("Mr. JOHN ABC DOE1", res.get("patient_name").toString());
        Assert.assertEquals("MRS.PRIYANKA SINGH", res.get("prescribed_by").toString());
        Assert.assertEquals("OP121922", res.get("visit_id").toString());
        Assert.assertEquals("ICM00014", res.get("insurance_co_id").toString());
      }
    } else {
      Assert.assertNotNull(pendPresc);
    }
    logger.info("Pending Prescription Service Test End");
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  private Map dummySessionObject() {
    HashMap dummySessionMap = new HashMap();
    dummySessionMap.put("userId", "InstaAdmin");
    dummySessionMap.put("roleId", 1);
    List<Integer> hospRoleId = new ArrayList<Integer>();
    dummySessionMap.put("hospital_role_ids", hospRoleId);
    return dummySessionMap;
  }

  @SuppressWarnings("unchecked")
  @Test
  public void insertPendingPrescriptions() {
    logger.info("Insert Pending Prescriptions Test Start");
    Mockito.when(sessionService.getSessionAttributes()).thenReturn(dummySessionObject());
    BasicDynaBean mainBean1 = presRepo.getBean();
    mainBean1.set("patient_presc_id", 2296);
    mainBean1.set("status", "P");
    Map<String, Object> prescription1 = new HashMap<String, Object>();
    prescription1.put("item_id", "DGC0999");
    prescription1.put("item_type", "Inv.");
    prescription1.put("is_package", false);
    prescription1.put("presc_activity_type", null);
    prescription1.put("prescribed_qty", null);
    prescription1.put("visit_id", "OP121922");
    prescription1.put("start_datetime", "2019-07-25 00:00:00");
    prescription1.put("prescribed_date", "2019-07-25 15:03:00");
    prescription1.put("doctor_id", "DOC0115");
    prescription1.put("item_remarks", "Lab 1 Remarks");

    BasicDynaBean mainBean2 = presRepo.getBean();
    mainBean2.set("patient_presc_id", 2297);
    mainBean2.set("status", "P");
    Map<String, Object> prescription2 = new HashMap<String, Object>();
    prescription2.put("item_id", "DGC1014");
    prescription2.put("item_type", "Inv.");
    prescription2.put("is_package", false);
    prescription2.put("presc_activity_type", null);
    prescription2.put("prescribed_qty", null);
    prescription2.put("visit_id", "OP121922");
    prescription2.put("start_datetime", "2019-07-25 00:00:00");
    prescription2.put("prescribed_date", "2019-07-25 15:04:00");
    prescription2.put("doctor_id", "DOC0115");
    prescription2.put("item_remarks", "Lab 2 Remarks");

    BasicDynaBean mainBean3 = presRepo.getBean();
    mainBean3.set("patient_presc_id", 2298);
    mainBean3.set("status", "P");
    Map<String, Object> prescription3 = new HashMap<String, Object>();
    prescription3.put("item_id", "SERV0089");
    prescription3.put("item_type", "Service");
    prescription3.put("is_package", false);
    prescription3.put("presc_activity_type", null);
    prescription3.put("prescribed_qty", 1);
    prescription3.put("visit_id", "OP121922");
    prescription3.put("start_datetime", "2019-07-25 00:00:00");
    prescription3.put("prescribed_date", "2019-07-25 15:04:00");
    prescription3.put("doctor_id", "DOC0115");
    prescription3.put("item_remarks", "Service 1 Remarks");

    BasicDynaBean mainBean4 = presRepo.getBean();
    mainBean4.set("patient_presc_id", 2299);
    mainBean4.set("status", "P");
    Map<String, Object> prescription4 = new HashMap<String, Object>();
    prescription4.put("item_id", "SERV0183");
    prescription4.put("item_type", "Service");
    prescription4.put("is_package", false);
    prescription4.put("presc_activity_type", null);
    prescription4.put("prescribed_qty", 1);
    prescription4.put("visit_id", "OP121922");
    prescription4.put("start_datetime", "2019-07-25 00:00:00");
    prescription4.put("prescribed_date", "2019-07-25 15:04:00");
    prescription4.put("doctor_id", "DOC0115");
    prescription4.put("item_remarks", "Service 2 Remarks");

    BasicDynaBean mainBean5 = presRepo.getBean();
    mainBean5.set("patient_presc_id", 2302);
    mainBean5.set("status", "P");
    Map<String, Object> prescription5 = new HashMap<String, Object>();
    prescription5.put("item_id", "DGC1145");
    prescription5.put("item_type", "Inv.");
    prescription5.put("is_package", false);
    prescription5.put("presc_activity_type", null);
    prescription5.put("prescribed_qty", null);
    prescription5.put("visit_id", "OP121922");
    prescription5.put("start_datetime", "2019-07-25 00:00:00");
    prescription5.put("prescribed_date", "2019-07-25 15:06:00");
    prescription5.put("doctor_id", "DOC0115");
    prescription5.put("item_remarks", "Rad 1 Remarks");

    BasicDynaBean mainBean6 = presRepo.getBean();
    mainBean6.set("patient_presc_id", 2300);
    mainBean6.set("status", "P");
    Map<String, Object> prescription6 = new HashMap<String, Object>();
    prescription6.put("item_id", "DOC0202");
    prescription6.put("item_type", "Doctor");
    prescription6.put("is_package", false);
    prescription6.put("presc_activity_type", "DOC");
    prescription6.put("prescribed_qty", null);
    prescription6.put("visit_id", "OP121922");
    prescription6.put("start_datetime", "2019-07-25 00:00:00");
    prescription6.put("prescribed_date", "2019-07-25 15:04:00");
    prescription6.put("doctor_id", "DOC0115");
    prescription6.put("item_remarks", "Doctor 1 Remarks");

    BasicDynaBean mainBean7 = presRepo.getBean();
    mainBean7.set("patient_presc_id", 2301);
    mainBean7.set("status", "P");
    Map<String, Object> prescription7 = new HashMap<String, Object>();
    prescription7.put("item_id", "DOC0202");
    prescription7.put("item_type", "Doctor");
    prescription7.put("is_package", false);
    prescription7.put("presc_activity_type", "DEPT");
    prescription7.put("prescribed_qty", null);
    prescription7.put("visit_id", "OP121922");
    prescription7.put("start_datetime", "2019-07-25 00:00:00");
    prescription7.put("prescribed_date", "2019-07-25 15:05:00");
    prescription7.put("doctor_id", "DOC0115");
    prescription7.put("item_remarks", "Department 1 Remarks");

    pendingPrescriptionsService.insertPrescriptions(mainBean1, prescription1);
    pendingPrescriptionsService.insertPrescriptions(mainBean2, prescription2);
    pendingPrescriptionsService.insertPrescriptions(mainBean3, prescription3);
    pendingPrescriptionsService.insertPrescriptions(mainBean4, prescription4);
    pendingPrescriptionsService.insertPrescriptions(mainBean5, prescription5);
    pendingPrescriptionsService.insertPrescriptions(mainBean6, prescription6);
    pendingPrescriptionsService.insertPrescriptions(mainBean7, prescription7);
    insertUpdateFollowUpPrescriptionsTest();
    List<BasicDynaBean> pendingPrescBeans = pendingPrescriptionsRepository.listAll();
    Assert.assertEquals(18, pendingPrescBeans.size());
    logger.info("Insert Pending Prescriptions Test End");
  }

  private void insertUpdateFollowUpPrescriptionsTest() {
    logger.info("Insert Followup Pending Prescriptions Test Start");
    List<BasicDynaBean> followup = new ArrayList<BasicDynaBean>();
    BasicDynaBean followUpBean1 = followUpRepository.getBean();
    followUpBean1.set("followup_id", "FUD0058");
    followUpBean1.set("followup_doctor_id", "DOC0115");
    followUpBean1.set("followup_date", new Date(2019 - 07 - 26));
    followUpBean1.set("patient_id", "OP121922");
    followUpBean1.set("followup_remarks", "Follow-up 1 remarks");
    followup.add(followUpBean1);

    BasicDynaBean followUpBean2 = followUpRepository.getBean();
    followUpBean2.set("followup_id", "FUD0059");
    followUpBean2.set("followup_doctor_id", "DOC0115");
    followUpBean2.set("followup_date", new Date(2019 - 07 - 27));
    followUpBean2.set("patient_id", "OP121922");
    followUpBean2.set("followup_remarks", "Follow-up 2 remarks");
    followup.add(followUpBean2);
    pendingPrescriptionsService.insertUpdateFollowUpPrescriptions("insert", followup);
    
    List<BasicDynaBean> followupUpdate = new ArrayList<BasicDynaBean>();
    BasicDynaBean followUpBean1Update = followUpRepository.getBean();
    followUpBean1Update.set("followup_id", "FUD0058");
    followUpBean1Update.set("followup_doctor_id", "DOC0115");
    followUpBean1Update.set("followup_date", new Date(2019 - 07 - 28));
    followUpBean1Update.set("patient_id", "OP121922");
    followUpBean1Update.set("followup_remarks", "Follow-up 3 remarks");
    followupUpdate.add(followUpBean1Update);

    BasicDynaBean followUpBean2Update = followUpRepository.getBean();
    followUpBean2Update.set("followup_id", "FUD0059");
    followUpBean2Update.set("followup_doctor_id", "DOC0115");
    followUpBean2Update.set("followup_date", new Date(2019 - 07 - 29));
    followUpBean2Update.set("patient_id", "OP121922");
    followUpBean2Update.set("followup_remarks", "Follow-up 4 remarks");
    followupUpdate.add(followUpBean2Update);
    pendingPrescriptionsService.insertUpdateFollowUpPrescriptions("update", followupUpdate);
    logger.info("Insert Followup Pending Prescriptions Test End");
  }
}
