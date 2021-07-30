package testng.com.insta.hms.core.scheduler;

import static org.mockito.Mockito.doReturn;
import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.Assert;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTransactionalTestNGSpringContextTests;
import org.springframework.ui.ModelMap;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import testng.utils.TestRepoInit;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.insta.hms.common.DynaBeanBuilder;
import com.insta.hms.common.PushService;
import com.insta.hms.common.preferences.genericpreferences.GenericPreferencesService;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.core.patient.communication.PatientCommunicationService;
import com.insta.hms.core.patient.outpatientlist.PatientSearchService;
import com.insta.hms.core.scheduler.AppointmentCategory;
import com.insta.hms.core.scheduler.AppointmentCategoryFactory;
import com.insta.hms.core.scheduler.AppointmentItemsRepository;
import com.insta.hms.core.scheduler.AppointmentRepository;
import com.insta.hms.core.scheduler.AppointmentService;
import com.insta.hms.core.scheduler.AppointmentValidator;
import com.insta.hms.core.scheduler.DoctorAppointmentCategory;
import com.insta.hms.core.scheduler.ResourceRepository;
import com.insta.hms.core.scheduler.SchedulerResourceTypesRepository;
import com.insta.hms.core.scheduler.ServiceAppointmentCategory;
import com.insta.hms.exception.ValidationErrorMap;
import com.insta.hms.integration.book.BookIntegrationService;
import com.insta.hms.jobs.JobService;
import com.insta.hms.mdm.centers.CenterService;
import com.insta.hms.mdm.doctors.DoctorService;
import com.insta.hms.mdm.resourceavailability.ResourceAvailabilityRepository;
import com.insta.hms.mdm.resourceavailability.ResourceAvailabilityService;
import com.insta.hms.mdm.serviceresources.ServiceResourcesService;
import com.insta.hms.mdm.testequipments.TestEquipmentService;
import com.insta.hms.redis.RedisMessagePublisher;

@Test
@ContextConfiguration(locations = { "classpath:spring/test-spring-config.xml" })
public class AppointmentServiceTest extends AbstractTransactionalTestNGSpringContextTests{
  
  @Spy
  private AppointmentItemsRepository appointmentItemsRepository;

  @Spy
  @InjectMocks
  private AppointmentRepository repository;
  
  @Spy
  private ResourceRepository resourceRepository;
  
  @Mock
  private GenericPreferencesService genericPreferencesService; 
  
  @Mock
  private BookIntegrationService bookIntegrationService;
  
  @Mock
  private CenterService centerService;
  
  @Mock
  private PatientSearchService patientSearchService;
  
  @Mock
  private JobService jobService;

  @Mock
  private AppointmentValidator appointmentValidator;

  @Spy
  @InjectMocks
  private ResourceAvailabilityService resAvailabilityService;

  @Spy
  @InjectMocks
  private ResourceAvailabilityRepository resourceAvailabilityRepository;

  @Mock
  private SessionService sessionService;
  
  @Mock
  private PushService pushService; 
  
  @Mock
  private RedisMessagePublisher redisMessagePublisher;
  
  @Mock
  private PatientCommunicationService patientCommunicationService;
  
  @Mock
  private DoctorService doctorService;
  
  @Mock
  private TestEquipmentService equipmentService;

  @Spy
  private SchedulerResourceTypesRepository schedulerResourceTypesRepository;
  
  @Mock
  private AppointmentCategoryFactory appointmentCategoryFactory;

  @Spy
  @InjectMocks
  private DoctorAppointmentCategory doctorAppointmentCategoryMock;

  @Spy
  @InjectMocks
  private ServiceAppointmentCategory serviceAppointmentCategory;

  private static final String FILENAME = "BulkAppointments.json";

  private List<ModelMap> dataProviderInsertJson;

  @Spy
  @InjectMocks
  private AppointmentService service;

  @Mock
  private ServiceResourcesService serviceResourcesService;

  private Logger logger = LoggerFactory.getLogger(AppointmentServiceTest.class);

  private Map<String,Object> dbDataMap = null;

  @BeforeTest
  public void stringToJsonData() throws IOException {
    ClassLoader classLoader = getClass().getClassLoader();
    String jsonString = FileUtils.readFileToString(new File(classLoader.getResource(FILENAME)
            .getFile()), "UTF-8");
    ObjectMapper mapper = new ObjectMapper();
    Map<String, Object> map = mapper.readValue(jsonString,
            new TypeReference<Map<String, List<ModelMap>>>() {
            });
    this.dataProviderInsertJson = (List<ModelMap>) map.get("values");
  }
  
  @BeforeMethod
  public void initMocks() {
    logger.info("Before every appointment service test");
    MockitoAnnotations.initMocks(this);
    TestRepoInit testRepo = new TestRepoInit();
    testRepo.insert("scheduler_appointments");
    testRepo.insert("scheduler_appointment_items");
    testRepo.insert("patient_registration");
    testRepo.insert("doctors");
    testRepo.insert("consultation_types");
    testRepo.insert("services");
    testRepo.insert("service_resource_master");
    testRepo.insert("services_departments");
    dbDataMap = testRepo.initializeRepo(); 
  }
  
  @SuppressWarnings("unchecked")
  public void MockitofunctionOverridesForUpdateAppointment(){
    Mockito.when(appointmentCategoryFactory.getInstance(Mockito.anyString()))
    .thenReturn(doctorAppointmentCategoryMock);
    Mockito.when(repository.getAppointmentsByAppointmentId(
        Mockito.any(DoctorAppointmentCategory.class),
        Mockito.anyInt(),
    	Mockito.anyString()))
    .thenReturn(null);
    Mockito.when(appointmentValidator.validateUpdateAppointmentStatusParams(Mockito.any(ArrayList.class), Mockito.any(ValidationErrorMap.class)))
    .thenReturn(true);

  }
  
  @Test
  public void updateAppointmentsStatusForCancel() throws Exception{
    logger.info("Update Appointment Cancel Status");
    MockitofunctionOverridesForUpdateAppointment();
    List<BasicDynaBean> beanlist = repository.listAll();
    Map updateStatusParams = createDummyUpdateStatusList(beanlist, "Cancel");
    Mockito.doNothing().when(service).unscheduleAppointmentMsg(Mockito.anyInt(), Mockito.anyString());
    Mockito.doNothing().when(service).upgradeAppointmentsWaitlist(Mockito.any(Timestamp.class),
        Mockito.anyInt(), Mockito.anyString(),  Mockito.anyString(), Mockito.anyInt());
    Mockito.doNothing().when(service).schedulePushEvent(Mockito.anyString(), Mockito.anyString());
    Mockito.doNothing().when(service).schedulePushEvent(Mockito.any(String[].class),
        Mockito.anyString());
    service.updateAppointmentsStatus(updateStatusParams);
    List<Map> appointmentsList = (List) dbDataMap.get("scheduler_appointments");
    for (Map appointment : appointmentsList) {
      BasicDynaBean bean = repository.findByKey("patient_name", appointment.get("patient_name"));
      Assert.assertEquals("Cancel",bean.get("appointment_status"));   
    }
    
  }
  
  @Test
  public void updateAppointmentsStatusForConfirmed() throws Exception{
    logger.info("Update Appointment Confirmed Status");
    MockitofunctionOverridesForUpdateAppointment();
    List<BasicDynaBean> beanlist = repository.listAll();
    Map updateStatusParams = createDummyUpdateStatusList(beanlist, "Confirmed");
    Mockito.doNothing().when(service).schedulePushEvent(Mockito.anyString(), Mockito.anyString());
    Mockito.doNothing().when(service).schedulePushEvent(Mockito.any(String[].class),
        Mockito.anyString());
    service.updateAppointmentsStatus(updateStatusParams);
    List<Map> appointmentsList = (List) dbDataMap.get("scheduler_appointments");
    for (Map appointment : appointmentsList) {
      BasicDynaBean bean = repository.findByKey("patient_name", appointment.get("patient_name"));
      Assert.assertEquals("Confirmed",bean.get("appointment_status"));   
    }    
  }
    
  @Test
  public void updateAppointmentsStatusForNoshow() throws Exception{
    logger.info("Update Appointment Noshow Status");
    MockitofunctionOverridesForUpdateAppointment();
    List<BasicDynaBean> beanlist = repository.listAll();
    Mockito.doNothing().when(service).schedulePushEvent(Mockito.anyString(), Mockito.anyString());
    Mockito.doNothing().when(service).schedulePushEvent(Mockito.any(String[].class),
        Mockito.anyString());
    Map updateStatusParams = createDummyUpdateStatusList(beanlist, "Noshow");
    service.updateAppointmentsStatus(updateStatusParams);
    List<Map> appointmentsList = (List) dbDataMap.get("scheduler_appointments");
    for (Map appointment : appointmentsList) {
      BasicDynaBean bean = repository.findByKey("patient_name", appointment.get("patient_name"));
      Assert.assertEquals("Noshow",bean.get("appointment_status"));   
    }
  }
    
  @Test
  public void updateAppointmentsStatusForCompleted() throws Exception{
    logger.info("Update Appointment Completed Status");
    MockitofunctionOverridesForUpdateAppointment();
    List<BasicDynaBean> beanlist = repository.listAll();
    Mockito.doNothing().when(service).schedulePushEvent(Mockito.anyString(), Mockito.anyString());
    Mockito.doNothing().when(service).schedulePushEvent(Mockito.any(String[].class),
        Mockito.anyString());
    Map updateStatusParams = createDummyUpdateStatusList(beanlist, "Completed");
    service.updateAppointmentsStatus(updateStatusParams);
    List<Map> appointmentsList = (List) dbDataMap.get("scheduler_appointments");
    for (Map appointment : appointmentsList) {
      BasicDynaBean bean = repository.findByKey("patient_name", appointment.get("patient_name"));
      Assert.assertEquals("Completed",bean.get("appointment_status"));   
    }
  }

  public Map<String, Object> createDummyUpdateStatusList(List<BasicDynaBean> beanlist, String appointmentStatus) {
     Map params = new HashMap<String, Object>();
     List maplist = new ArrayList<Map<String, Object>>();
        if ( beanlist != null){
            for (Object object : beanlist) {
                 BasicDynaBean bean = (BasicDynaBean)object; 
                 Map appParams =  new HashMap();
                 appParams.put("appointment_id", (Integer) bean.get("appointment_id"));
             if(appointmentStatus.equals("Cancel")){
               appParams.put("cancel_type", "O");
               appParams.put("cancel_reason", "cancel appointment");                   
             }
             appParams.put("appointment_status",appointmentStatus );
             appParams.put("category", "DOC");
             maplist.add(appParams);
          }
       }
       params.put("update_app_status", maplist);
       return params;
  }


  @Test(dataProvider = "insertAppointmentsData")
  public void createNewAppointmentTest(ModelMap jsonRequestBody) {
    logger.info("Creating new appointments");
    Map<String,Object> appointment = (Map<String, Object>) jsonRequestBody.get("appointments");
    List appointmentsList = new ArrayList();
    appointmentsList.add(appointment);
    Map<String, Object> paramsMap = new HashMap<>();
    paramsMap.put("patient", jsonRequestBody.get("patient"));
    paramsMap.put("appointments", appointmentsList);
    paramsMap.put("additional_info", jsonRequestBody.get("additional_info"));
    AppointmentCategory apptCategory = null;
    Mockito.when(appointmentCategoryFactory.getInstance(Mockito.anyString())).thenReturn(appointment.get("category").equals("DOC") ? apptCategory = doctorAppointmentCategoryMock : appointment.get("category").equals("SNP") ? apptCategory = serviceAppointmentCategory : null);
    doReturn(true).when(service).validate(org.mockito.Mockito.eq(apptCategory), Mockito.any(Map.class),Mockito.any(Map.class), Mockito.any(Map.class),Mockito.anyString());
    Mockito.when(sessionService.getSessionAttributes()).thenReturn(dummySessionObject());
    Mockito.when(genericPreferencesService.getAllPreferences()).thenReturn(dummyPrefObject());
    Mockito.when(appointmentValidator.validateIfPrimResExists(org.mockito.Mockito.eq(apptCategory), Mockito.anyString(), Mockito.anyInt())).thenReturn(true);
    Mockito.when(appointmentValidator.validateIfSecResExists(org.mockito.Mockito.eq(apptCategory),Mockito.anyString(), Mockito.anyInt())).thenReturn(true);
    Mockito.when(serviceResourcesService.getOverbookLimit(Mockito.anyString())).thenReturn(0);
    Mockito.doNothing().when(service).schedulePushEvent(Mockito.anyString(), Mockito.anyString());
    Mockito.doNothing().when(service).schedulePushEvent(Mockito.any(String[].class),
        Mockito.anyString());
    Map responseMap = service.createNewAppointments(Arrays.asList(apptCategory), paramsMap);
    Map appointmentResponseMap =  ((List<Map>)responseMap.get("appointments")).get(0);
    Integer appointmentId = (Integer) ((List)appointmentResponseMap.get("appointment_ids_list")).get(0);
    Assert.assertNotNull(appointmentResponseMap);
    Assert.assertNotNull(appointmentId);
  }

  @DataProvider(name = "insertAppointmentsData")
  public Object[][] insertAppointmentsData() {
    Object[][] returnData = new Object[dataProviderInsertJson.size()][1];
    for(int i=0; i<dataProviderInsertJson.size(); i++) {
      returnData[i][0] = dataProviderInsertJson.get(i);
    }
    return  returnData;
  }

  /**TODO : can be read from .json file  common thing*/
  public Map dummySessionObject() {
    HashMap dummySessionMap = new HashMap();
    dummySessionMap.put("userId", "InstaAdmin");
    return dummySessionMap;
  }

  public BasicDynaBean dummyPrefObject() {
    DynaBeanBuilder builder = new DynaBeanBuilder();
    builder.add("contact_pref_lang_code", String.class);
    BasicDynaBean bean = builder.build();
    bean.set("contact_pref_lang_code", "en");
    return bean;
  }

}
