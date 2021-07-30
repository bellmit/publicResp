package testng.com.insta.hms.core.scheduler;

import com.bob.hms.common.DateUtil;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.core.scheduler.AppointmentCategory;
import com.insta.hms.core.scheduler.AppointmentCategoryFactory;
import com.insta.hms.core.scheduler.AppointmentService;
import com.insta.hms.core.scheduler.AppointmentValidator;
import com.insta.hms.core.scheduler.DoctorAppointmentCategory;
import com.insta.hms.core.scheduler.SchedulerBulkAppointmentsService;
import com.insta.hms.core.scheduler.SchedulerResourceTypesRepository;
import com.insta.hms.core.scheduler.resourcelist.SchedulerResourceSearchService;
import com.insta.hms.exception.ValidationErrorMap;
import com.insta.hms.mdm.consultationtypes.ConsultationTypesService;
import com.insta.hms.mdm.doctors.DoctorService;
import com.insta.hms.mdm.hospitalcenters.HospitalCenterService;
import com.insta.hms.mdm.testequipments.TestEquipmentService;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTransactionalTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import testng.utils.TestRepoInit;

import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//TODO : remove comments when code for getSlots is stable

@Test
@ContextConfiguration(locations = { "classpath:spring/test-spring-config.xml" })
@SuppressWarnings({ "unchecked", "rawtypes", "unused" })
public class SchedulerBulkAppointmentsServiceTest extends AbstractTransactionalTestNGSpringContextTests{

  @Mock
  private AppointmentService appointmentService;
  
  @Mock
  private SessionService sessionService;
  
  @Mock
  private AppointmentValidator appointmentValidator;
  
  @Mock
  private SchedulerResourceTypesRepository schedulerResourceTypesRepository;
  
  @Mock
  private DoctorService doctorService;
  
  @Mock
  private TestEquipmentService equipmentService;
  
  @Mock
  private HospitalCenterService hospitalCenterService;

  @Mock
  private ConsultationTypesService consultationTypesService;
  
  @Mock
  private SchedulerResourceSearchService schedulerResourceSearchService;

  @Mock
  private AppointmentCategoryFactory appointmentCategoryFactory;
  
  @Spy
  @InjectMocks
  private DoctorAppointmentCategory doctorAppointmentCategoryMock;
  
  @Spy
  @InjectMocks
  private SchedulerBulkAppointmentsService schedulerBulkAppointmentsService;
  
  private Map<String,Object> dbDataMap = null;
  
  @BeforeMethod
  public void initMocks() {
    MockitoAnnotations.initMocks(this);
    TestRepoInit testRepo = new TestRepoInit();
    testRepo.insert("scheduler_appointments");
    testRepo.insert("scheduler_appointment_items");
    testRepo.insert("department");
    testRepo.insert("registration_preferences");
    testRepo.insert("doctors");
    testRepo.insert("doctor_center_master");
    testRepo.insert("consultation_types");
    dbDataMap = testRepo.initializeRepo(); 
  }
  
  public void MockitofunctionOverridesForFetchAppointment1() {
    Mockito.when(appointmentService.getAppointmentsForPatient(
        Mockito.anyList(), 
        Mockito.any(Timestamp.class), 
        Mockito.anyString(), 
        Mockito.anyInt(), 
        Mockito.anyString(), 
        Mockito.anyInt(), 
        Mockito.anyString()))
        .thenReturn(prepareAppointmentsList1());
  }
  
  public void MockitofunctionOverridesForFetchAppointment2() {
    Mockito.when(appointmentService.getAppointmentsForPatient(
        Mockito.anyList(), 
        Mockito.any(Timestamp.class), 
        Mockito.anyString(), 
        Mockito.anyInt(), 
        Mockito.anyString(), 
        Mockito.anyInt(), 
        Mockito.anyString()))
        .thenReturn(prepareAppointmentsList2());
  }
  
  @Test
  public void fetchAppointments1() {
    MockitofunctionOverridesForFetchAppointment1();
    Map resMap = schedulerBulkAppointmentsService.getAppointmentsForPatient(createMockParamsForFetchAppointments1(),null);
    Assert.assertEquals(( (List)resMap.get("appointments") ).size() , 4);
  }
  
  @Test
  public void fetchAppointments2() {
    MockitofunctionOverridesForFetchAppointment2();
    Map resMap = schedulerBulkAppointmentsService.getAppointmentsForPatient(createMockParamsForFetchAppointments2(),null);
    Assert.assertEquals(( (List)resMap.get("appointments") ).size() , 4);
  }
  
  @Test(expectedExceptions = Exception.class)
  public void fetchAppointmentsAppError() {
    MockitofunctionOverridesForFetchAppointment1();
    Map resMap = schedulerBulkAppointmentsService.getAppointmentsForPatient(createMockParamsForFetchAppointmentsAppError(),null);
    Assert.assertEquals(( (List)resMap.get("appointments") ).size() , 4);
  }
  
  @Test(expectedExceptions = Exception.class)
  public void fetchAppointmentsCenterError() {
    MockitofunctionOverridesForFetchAppointment1();
    Map resMap = schedulerBulkAppointmentsService.getAppointmentsForPatient(createMockParamsForFetchAppointmentsCenterError(),null);
    Assert.assertEquals(( (List)resMap.get("appointments") ).size() , 4);
  }
  
  @Test(expectedExceptions = Exception.class)
  public void fetchAppointmentsDateError() {
    MockitofunctionOverridesForFetchAppointment1();
    Map resMap = schedulerBulkAppointmentsService.getAppointmentsForPatient(createMockParamsForFetchAppointmentsDateError(),null);
    Assert.assertEquals(( (List)resMap.get("appointments") ).size() , 4);
  }
  
  public Map createMockParamsForFetchAppointments1() {
    Map params = new HashMap();
    params.put("appointment_id",new String[]{"1"});
    params.put("mr_no",new String[]{"MR001"});
    params.put("from_date",new String[]{"11-11-2018"});
    params.put("center_id",new String[]{"7"});
    params.put("app_status",new String[]{"all"});
    return params;
  }
  
  public Map createMockParamsForFetchAppointments2() {
    Map params = new HashMap();
    params.put("appointment_id",new String[]{"1"});
    params.put("mr_no",new String[]{"MR001"});
    params.put("from_date",new String[]{"11-11-2018"});
    params.put("center_id",new String[]{"7"});
    params.put("app_status",new String[]{"all"});
    return params;
  }
  
  public Map createMockParamsForFetchAppointmentsAppError() {
    Map params = new HashMap();
    params.put("appointment_id",new String[]{"app01"});
    params.put("mr_no",new String[]{"MR001"});
    params.put("from_date",new String[]{"11-11-2018"});
    params.put("center_id",new String[]{"7"});
    params.put("app_status",new String[]{"all"});
    return params;
  }
  
  public Map createMockParamsForFetchAppointmentsCenterError() {
    Map params = new HashMap();
    params.put("mr_no",new String[]{"MR001"});
    params.put("from_date",new String[]{"11-11-2018"});
    params.put("center_id",new String[]{"a7"});
    params.put("app_status",new String[]{"all"});
    return params;
  }
  
  public Map createMockParamsForFetchAppointmentsDateError() {
    Map params = new HashMap();
    params.put("mr_no",new String[]{"MR001"});
    params.put("from_date",new String[]{"xy-11-2018"});
    params.put("center_id",new String[]{"7"});
    params.put("app_status",new String[]{"all"});
    return params;
  }
  
  public List prepareAppointmentsList1() {
    //add before prim
    List appList = new ArrayList();
    
    // DOC app
    
    Map app11Map = new HashMap();
    app11Map.put("appointment_id",1);
    app11Map.put("prim_res_id","DOC01");
    app11Map.put("prim_res_name","Akshay");
    app11Map.put("sec_res_id","-1");
    app11Map.put("sec_res_name","ConsType1");
    app11Map.put("app_cat","DOC");
    app11Map.put("add_res_id","22");
    app11Map.put("add_res_name","Equipment1");
    app11Map.put("add_res_type","EQID");
    app11Map.put("appointment_time","start_time");
    app11Map.put("appointment_date", "2018-11-11");
    app11Map.put("appointment_status", "Booked");
    app11Map.put("duration", 30);
    app11Map.put("center_id", 7);
    app11Map.put("center_name", "BRS");
    appList.add(app11Map);
    
    Map appMap = new HashMap();
    appMap.put("appointment_id",1);
    appMap.put("prim_res_id","DOC01");
    appMap.put("prim_res_name","Akshay");
    appMap.put("sec_res_id","-1");
    appMap.put("sec_res_name","ConsType1");
    appMap.put("app_cat","DOC");
    appMap.put("add_res_id","22");
    appMap.put("add_res_name","Equipment1");
    appMap.put("add_res_type","EQID");
    appMap.put("appointment_time","start_time");
    appMap.put("appointment_date", "2018-11-11");
    appMap.put("appointment_status", "Booked");
    appMap.put("duration", 30);
    appMap.put("center_id", 7);
    appMap.put("center_name", "BRS");
    appList.add(appMap);
    
    //Ser app
    
    Map app31Map = new HashMap();
    app31Map.put("appointment_id",2);
    app31Map.put("prim_res_id","22");
    app31Map.put("prim_res_name","SRID1");
    app31Map.put("sec_res_id","SERV001");
    app31Map.put("sec_res_name","Service1");
    app31Map.put("app_cat","SNP");
    app31Map.put("add_res_id","DOC01");
    app31Map.put("add_res_name","Akshay");
    app31Map.put("add_res_type","DOC");
    app31Map.put("appointment_time","start_time");
    app31Map.put("appointment_date", "2018-11-11");
    app31Map.put("appointment_status", "Booked");
    app31Map.put("duration", 30);
    app31Map.put("center_id", 7);
    app31Map.put("center_name", "BRS");
    appList.add(app31Map);
    
    Map app2Map = new HashMap();
    app2Map.put("appointment_id",2);
    app2Map.put("prim_res_id","22");
    app2Map.put("prim_res_name","SRID1");
    app2Map.put("sec_res_id","SERV001");
    app2Map.put("sec_res_name","Service1");
    app2Map.put("app_cat","SNP");
    app2Map.put("add_res_id","22");
    app2Map.put("add_res_name","SRID1");
    app2Map.put("add_res_type","SRID");
    app2Map.put("appointment_time","start_time");
    app2Map.put("appointment_date", "2018-11-11");
    app2Map.put("appointment_status", "Booked");
    app2Map.put("duration", 30);
    app2Map.put("center_id", 7);
    app2Map.put("center_name", "BRS");
    appList.add(app2Map);
    
    
    //Dia app
    
    Map app51Map = new HashMap();
    app51Map.put("appointment_id",3);
    app51Map.put("prim_res_id","22");
    app51Map.put("prim_res_name","SRID1");
    app51Map.put("sec_res_id","SERV001");
    app51Map.put("sec_res_name","Service1");
    app51Map.put("app_cat","DIA");
    app51Map.put("add_res_id","DOC01");
    app51Map.put("add_res_name","Akshay");
    app51Map.put("add_res_type","DOC");
    app51Map.put("appointment_time","start_time");
    app51Map.put("appointment_date", "2018-11-11");
    app51Map.put("appointment_status", "Booked");
    app51Map.put("duration", 30);
    app51Map.put("center_id", 7);
    app51Map.put("center_name", "BRS");
    appList.add(app51Map);
    
    Map app4Map = new HashMap();
    app4Map.put("appointment_id",3);
    app4Map.put("prim_res_id","22");
    app4Map.put("prim_res_name","SRID1");
    app4Map.put("sec_res_id","SERV001");
    app4Map.put("sec_res_name","Service1");
    app4Map.put("app_cat","DIA");
    app4Map.put("add_res_id","22");
    app4Map.put("add_res_name","SRID1");
    app4Map.put("add_res_type","EQID");
    app4Map.put("appointment_time","start_time");
    app4Map.put("appointment_date", "2018-11-11");
    app4Map.put("appointment_status", "Booked");
    app4Map.put("duration", 30);
    app4Map.put("center_id", 7);
    app4Map.put("center_name", "BRS");
    appList.add(app4Map);
    
    //Sur app
    
    Map app71Map = new HashMap();
    app71Map.put("appointment_id",4);
    app71Map.put("prim_res_id","THID0002");
    app71Map.put("prim_res_name","Theatre1");
    app71Map.put("sec_res_id","0");
    app71Map.put("sec_res_name","Surgery1");
    app71Map.put("app_cat","OPE");
    app71Map.put("add_res_id","DOC01");
    app71Map.put("add_res_name","Akshay");
    app71Map.put("add_res_type","SURDOC");
    app71Map.put("appointment_time","start_time");
    app71Map.put("appointment_date", "2018-11-11");
    app71Map.put("appointment_status", "Booked");
    app71Map.put("duration", 30);
    app71Map.put("center_id", 7);
    app71Map.put("center_name", "BRS");
    appList.add(app71Map);
    
    Map app6Map = new HashMap();
    app6Map.put("appointment_id",4);
    app6Map.put("prim_res_id","THID0002");
    app6Map.put("prim_res_name","Theatre1");
    app6Map.put("sec_res_id","0");
    app6Map.put("sec_res_name","Surgery1");
    app6Map.put("app_cat","OPE");
    app6Map.put("add_res_id","THID0002");
    app6Map.put("add_res_name","Theatre1");
    app6Map.put("add_res_type","THID");
    app6Map.put("appointment_time","start_time");
    app6Map.put("appointment_date", "2018-11-11");
    app6Map.put("appointment_status", "Booked");
    app6Map.put("duration", 30);
    app6Map.put("center_id", 7);
    app6Map.put("center_name", "BRS");
    appList.add(app6Map);
    
    return appList;
  }
  
  public List prepareAppointmentsList2() {
    //prim before add
    List appList = new ArrayList();
    
    // DOC app
    
    Map app1Map = new HashMap();
    app1Map.put("appointment_id",1);
    app1Map.put("prim_res_id","DOC01");
    app1Map.put("prim_res_name","Akshay");
    app1Map.put("sec_res_id","-1");
    app1Map.put("sec_res_name","ConsType1");
    app1Map.put("app_cat","DOC");
    app1Map.put("add_res_id","DOC01");
    app1Map.put("add_res_name","Akshay");
    app1Map.put("add_res_type","OPDOC");
    app1Map.put("appointment_time","start_time");
    app1Map.put("appointment_date", "2018-11-11");
    app1Map.put("appointment_status", "Booked");
    app1Map.put("duration", 30);
    app1Map.put("center_id", 7);
    app1Map.put("center_name", "BRS");
    appList.add(app1Map);
    
    Map appMap = new HashMap();
    appMap.put("appointment_id",1);
    appMap.put("prim_res_id","DOC01");
    appMap.put("prim_res_name","Akshay");
    appMap.put("sec_res_id","-1");
    appMap.put("sec_res_name","ConsType1");
    appMap.put("app_cat","DOC");
    appMap.put("add_res_id","22");
    appMap.put("add_res_name","Equipment1");
    appMap.put("add_res_type","EQID");
    appMap.put("appointment_time","start_time");
    appMap.put("appointment_date", "2018-11-11");
    appMap.put("appointment_status", "Booked");
    appMap.put("duration", 30);
    appMap.put("center_id", 7);
    appMap.put("center_name", "BRS");
    appList.add(appMap);
    
    //Ser app
    
    Map app2Map = new HashMap();
    app2Map.put("appointment_id",2);
    app2Map.put("prim_res_id","22");
    app2Map.put("prim_res_name","SRID1");
    app2Map.put("sec_res_id","SERV001");
    app2Map.put("sec_res_name","Service1");
    app2Map.put("app_cat","SNP");
    app2Map.put("add_res_id","22");
    app2Map.put("add_res_name","SRID1");
    app2Map.put("add_res_type","SRID");
    app2Map.put("appointment_time","start_time");
    app2Map.put("appointment_date", "2018-11-11");
    app2Map.put("appointment_status", "Booked");
    app2Map.put("duration", 30);
    app2Map.put("center_id", 7);
    app2Map.put("center_name", "BRS");
    appList.add(app2Map);
    
    Map app3Map = new HashMap();
    app3Map.put("appointment_id",2);
    app3Map.put("prim_res_id","22");
    app3Map.put("prim_res_name","SRID1");
    app3Map.put("sec_res_id","SERV001");
    app3Map.put("sec_res_name","Service1");
    app3Map.put("app_cat","SNP");
    app3Map.put("add_res_id","DOC01");
    app3Map.put("add_res_name","Akshay");
    app3Map.put("add_res_type","DOC");
    app3Map.put("appointment_time","start_time");
    app3Map.put("appointment_date", "2018-11-11");
    app3Map.put("appointment_status", "Booked");
    app3Map.put("duration", 30);
    app3Map.put("center_id", 7);
    app3Map.put("center_name", "BRS");
    appList.add(app3Map);
    
    //Dia app
    
    Map app4Map = new HashMap();
    app4Map.put("appointment_id",3);
    app4Map.put("prim_res_id","22");
    app4Map.put("prim_res_name","SRID1");
    app4Map.put("sec_res_id","SERV001");
    app4Map.put("sec_res_name","Service1");
    app4Map.put("app_cat","DIA");
    app4Map.put("add_res_id","22");
    app4Map.put("add_res_name","SRID1");
    app4Map.put("add_res_type","EQID");
    app4Map.put("appointment_time","start_time");
    app4Map.put("appointment_date", "2018-11-11");
    app4Map.put("appointment_status", "Booked");
    app4Map.put("duration", 30);
    app4Map.put("center_id", 7);
    app4Map.put("center_name", "BRS");
    appList.add(app4Map);
    
    Map app5Map = new HashMap();
    app5Map.put("appointment_id",3);
    app5Map.put("prim_res_id","22");
    app5Map.put("prim_res_name","SRID1");
    app5Map.put("sec_res_id","SERV001");
    app5Map.put("sec_res_name","Service1");
    app5Map.put("app_cat","DIA");
    app5Map.put("add_res_id","DOC01");
    app5Map.put("add_res_name","Akshay");
    app5Map.put("add_res_type","DOC");
    app5Map.put("appointment_time","start_time");
    app5Map.put("appointment_date", "2018-11-11");
    app5Map.put("appointment_status", "Booked");
    app5Map.put("duration", 30);
    app5Map.put("center_id", 7);
    app5Map.put("center_name", "BRS");
    appList.add(app5Map);
    
    //Sur app
    
    Map app6Map = new HashMap();
    app6Map.put("appointment_id",4);
    app6Map.put("prim_res_id","THID0002");
    app6Map.put("prim_res_name","Theatre1");
    app6Map.put("sec_res_id","0");
    app6Map.put("sec_res_name","Surgery1");
    app6Map.put("app_cat","OPE");
    app6Map.put("add_res_id","THID0002");
    app6Map.put("add_res_name","Theatre1");
    app6Map.put("add_res_type","THID");
    app6Map.put("appointment_time","start_time");
    app6Map.put("appointment_date", "2018-11-11");
    app6Map.put("appointment_status", "Booked");
    app6Map.put("duration", 30);
    app6Map.put("center_id", 7);
    app6Map.put("center_name", "BRS");
    appList.add(app6Map);
    
    Map app7Map = new HashMap();
    app7Map.put("appointment_id",4);
    app7Map.put("prim_res_id","THID0002");
    app7Map.put("prim_res_name","Theatre1");
    app7Map.put("sec_res_id","0");
    app7Map.put("sec_res_name","Surgery1");
    app7Map.put("app_cat","OPE");
    app7Map.put("add_res_id","DOC01");
    app7Map.put("add_res_name","Akshay");
    app7Map.put("add_res_type","SURDOC");
    app7Map.put("appointment_time","start_time");
    app7Map.put("appointment_date", "2018-11-11");
    app7Map.put("appointment_status", "Booked");
    app7Map.put("duration", 30);
    app7Map.put("center_id", 7);
    app7Map.put("center_name", "BRS");
    appList.add(app7Map);
    return appList;
  }
  
  public Map timeMap = new HashMap();
  
  public void MockfunctionOverridesForGetSlots() {
    
    try {
      initializeMap();
    } catch(Exception e) {
      
    }
    Mockito.when(appointmentCategoryFactory.getInstance(Mockito.anyString()))
    .thenReturn(doctorAppointmentCategoryMock);
    
    try {
      Mockito.when(schedulerResourceSearchService.getNextSlotTime(
          Mockito.anyString(),
          Mockito.anyInt()))
          .thenReturn(DateUtil.parseTime("18:00"));
    } catch (ParseException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    Mockito.when(doctorAppointmentCategoryMock.getSlotDurationOfPrimRes(
        Mockito.anyString()))
        .thenReturn(15);
    Mockito.when(doctorAppointmentCategoryMock.getPrimResApplicableForSecRes(
        Mockito.anyString(), 
        Mockito.anyInt(), 
        Mockito.anyString()))
        .thenReturn(getResourceListForSLots());
    Mockito.when(appointmentValidator.validateIfSlotOverbooked( 
        Mockito.any(AppointmentCategory.class),
        Mockito.anyString(),
        Mockito.anyString(),
        Mockito.any(Timestamp.class),
        Mockito.any(Timestamp.class),
        Mockito.any(ValidationErrorMap.class),
        Mockito.anyString(),
        Mockito.anyString()
        ))
        .thenReturn(true);
    Mockito.when(appointmentValidator.validateResourcesAvailability(
        Mockito.any(AppointmentCategory.class),
        Mockito.any(Timestamp.class),
        Mockito.any(Timestamp.class),
        Mockito.anyString(),
        Mockito.anyString(),
        Mockito.anyString(),
        Mockito.anyInt(),
        Mockito.any(ValidationErrorMap.class),
        Mockito.anyString(),
        Mockito.any(Map.class)
        ))
        .thenReturn(true);
    Mockito.when(appointmentValidator.validateWithinSameDay(
        Mockito.any(Timestamp.class),
        Mockito.any(Timestamp.class),
        Mockito.any(ValidationErrorMap.class)
        ))
        .thenReturn(true);
  }
  
  public void MockfunctionOverridesForGetSlotsDurationError() {
    
    try {
      initializeMap();
    } catch(Exception e) {
      
    }
    Mockito.when(appointmentCategoryFactory.getInstance(Mockito.anyString()))
    .thenReturn(doctorAppointmentCategoryMock);
    
    try {
      Mockito.when(schedulerResourceSearchService.getNextSlotTime(
          Mockito.anyString(),
          Mockito.anyInt()))
          .thenReturn(DateUtil.parseTime("18:00"));
    } catch (ParseException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    Mockito.when(doctorAppointmentCategoryMock.getSlotDurationOfPrimRes(
        Mockito.anyString()))
        .thenReturn(15);
    Mockito.when(doctorAppointmentCategoryMock.getAppointmentDuration(
        Mockito.any(Integer.class).toString(),
        Mockito.anyString()
        ))
        .thenReturn(-1);
    Mockito.when(doctorAppointmentCategoryMock.getPrimResApplicableForSecRes(
        Mockito.anyString(), 
        Mockito.anyInt(), 
        Mockito.anyString()))
        .thenReturn(getResourceListForSLots());
    Mockito.when(appointmentValidator.validateIfSlotOverbooked( 
        Mockito.any(AppointmentCategory.class),
        Mockito.anyString(),
        Mockito.anyString(),
        Mockito.any(Timestamp.class),
        Mockito.any(Timestamp.class),
        Mockito.any(ValidationErrorMap.class),
        Mockito.anyString(),
        Mockito.anyString()
        ))
        .thenReturn(true);
    Mockito.when(appointmentValidator.validateResourcesAvailability(
        Mockito.any(AppointmentCategory.class),
        Mockito.any(Timestamp.class),
        Mockito.any(Timestamp.class),
        Mockito.anyString(),
        Mockito.anyString(),
        Mockito.anyString(),
        Mockito.anyInt(),
        Mockito.any(ValidationErrorMap.class),
        Mockito.anyString(),
        Mockito.any(Map.class)
        ))
        .thenReturn(true);
    Mockito.when(appointmentValidator.validateWithinSameDay(
        Mockito.any(Timestamp.class),
        Mockito.any(Timestamp.class),
        Mockito.any(ValidationErrorMap.class)
        ))
        .thenReturn(true);
  }
 
  public void initializeMap() throws ParseException {
    timeMap = new HashMap();
    timeMap.put("18:00",DateUtil.parseTime("18:00"));
    timeMap.put("18:15",DateUtil.parseTime("18:15"));
    timeMap.put("18:30",DateUtil.parseTime("18:30"));
  }
  
//  @Test
//  public void getSlots() {
//    MockfunctionOverridesForGetSlots();
//    Map resMap = schedulerBulkAppointmentsService.getAvailableSlots(createMockParamsForGetSlots());
//    Assert.assertNotNull(resMap);;
//  }
//  
//  @Test(expectedExceptions = Exception.class)
//  public void getSlotsDurationError() {
//    MockfunctionOverridesForGetSlotsDurationError();
//    Map resMap = schedulerBulkAppointmentsService.getAvailableSlots(createMockParamsForGetSlots());
//    Assert.assertNotNull(resMap);;
//  }
//  
//  @Test(expectedExceptions = Exception.class)
//  public void getSlotsCenterError() {
//    MockfunctionOverridesForGetSlots();
//    Map resMap = schedulerBulkAppointmentsService.getAvailableSlots(createMockParamsForGetSlotsCenterError());
//    Assert.assertNotNull(resMap);;
//  }
//  
//  @Test(expectedExceptions = Exception.class)
//  public void getSlotsSecResError() {
//    MockfunctionOverridesForGetSlots();
//    Map resMap = schedulerBulkAppointmentsService.getAvailableSlots(createMockParamsForGetSlotsSecResError());
//    Assert.assertNotNull(resMap);;
//  }
//  
//  @Test(expectedExceptions = Exception.class)
//  public void getSlotsSecResLenError() {
//    MockfunctionOverridesForGetSlots();
//    Map resMap = schedulerBulkAppointmentsService.getAvailableSlots(createMockParamsForGetSlotsSecResLenError());
//    Assert.assertNotNull(resMap);;
//  }
//  
//  @Test(expectedExceptions = Exception.class)
//  public void getSlotsAppCatError() {
//    MockfunctionOverridesForGetSlots();
//    Map resMap = schedulerBulkAppointmentsService.getAvailableSlots(createMockParamsForGetSlotsAppCatError());
//    Assert.assertNotNull(resMap);;
//  }
//  
//  @Test(expectedExceptions = Exception.class)
//  public void getSlotsAppCatLenError() {
//    MockfunctionOverridesForGetSlots();
//    Map resMap = schedulerBulkAppointmentsService.getAvailableSlots(createMockParamsForGetSlotsAppCatLenError());
//    Assert.assertNotNull(resMap);;
//  }
//  
//  @Test(expectedExceptions = Exception.class)
//  public void getSlotsDeptError() {
//    MockfunctionOverridesForGetSlots();
//    Map resMap = schedulerBulkAppointmentsService.getAvailableSlots(createMockParamsForGetSlotsDepError());
//    Assert.assertNotNull(resMap);;
//  }
//  
//  @Test(expectedExceptions = Exception.class)
//  public void getSlotsDeptLenError() {
//    MockfunctionOverridesForGetSlots();
//    Map resMap = schedulerBulkAppointmentsService.getAvailableSlots(createMockParamsForGetSlotsDepLenError());
//    Assert.assertNotNull(resMap);;
//  }
//  
//  @Test(expectedExceptions = Exception.class)
//  public void getSlotsDependencyError() {
//    MockfunctionOverridesForGetSlots();
//    Map resMap = schedulerBulkAppointmentsService.getAvailableSlots(createMockParamsForGetSlotsDependencyError());
//    Assert.assertNotNull(resMap);;
//  }
//  
//  @Test(expectedExceptions = Exception.class)
//  public void getSlotsDependencyLenError() {
//    MockfunctionOverridesForGetSlots();
//    Map resMap = schedulerBulkAppointmentsService.getAvailableSlots(createMockParamsForGetSlotsDependencyLenError());
//    Assert.assertNotNull(resMap);;
//  }
//  
//  @Test(expectedExceptions = Exception.class)
//  public void getSlotsDateError() {
//    MockfunctionOverridesForGetSlots();
//    Map resMap = schedulerBulkAppointmentsService.getAvailableSlots(createMockParamsForGetSlotsDateError());
//    Assert.assertNotNull(resMap);;
//  }
//  
//  @Test(expectedExceptions = Exception.class)
//  public void getSlotsPastDateError() {
//    MockfunctionOverridesForGetSlots();
//    Map resMap = schedulerBulkAppointmentsService.getAvailableSlots(createMockParamsForGetSlotsPastDateError());
//    Assert.assertNotNull(resMap);;
//  }
  
  public Map createMockParamsForGetSlots() {
    Map params = new HashMap();
    params.put("secondary_resources_id_list",new String[]{"-1,-1"});
    params.put("dept_list",new String[]{"DEP0001,DEP0001"});
    params.put("interdependency_list",new String[]{"0,15"});
    params.put("center_id",new String[]{"7"});
    params.put("date",new String[]{"25-10-2025"});
    params.put("time",new String[]{"18:00"});
    params.put("appointment_category_list",new String[]{"DOC,DOC"});
    return params;
  }
  
  public Map createMockParamsForGetSlotsPastDateError() {
    Map params = new HashMap();
    params.put("secondary_resources_id_list",new String[]{"-1,-1"});
    params.put("dept_list",new String[]{"DEP0001,DEP0001"});
    params.put("interdependency_list",new String[]{"0,0"});
    params.put("center_id",new String[]{"7"});
    params.put("date",new String[]{"01-01-2001"});
    params.put("time",new String[]{"18:00"});
    params.put("appointment_category_list",new String[]{"DOC,DOC"});
    return params;
  }
  
  public Map createMockParamsForGetSlotsDateError() {
    Map params = new HashMap();
    params.put("secondary_resources_id_list",new String[]{"-1,-1"});
    params.put("dept_list",new String[]{"DEP0001,DEP0001"});
    params.put("interdependency_list",new String[]{"0,0"});
    params.put("center_id",new String[]{"7"});
    params.put("date",new String[]{"xy-10-2019"});
    params.put("time",new String[]{"18:00"});
    params.put("appointment_category_list",new String[]{"DOC,DOC"});
    return params;
  }
  
  public Map createMockParamsForGetSlotsCenterError() {
    Map params = new HashMap();
    params.put("secondary_resources_id_list",new String[]{"-1,-1"});
    params.put("dept_list",new String[]{"DEP0001,DEP0001"});
    params.put("interdependency_list",new String[]{"0,0"});
    params.put("center_id",new String[]{"a7"});
    params.put("date",new String[]{"25-10-2019"});
    params.put("time",new String[]{"18:00"});
    params.put("appointment_category_list",new String[]{"DOC,DOC"});
    return params;
  }
  
  public Map createMockParamsForGetSlotsSecResError() {
    Map params = new HashMap();
    params.put("dept_list",new String[]{"DEP0001,DEP0001"});
    params.put("interdependency_list",new String[]{"0,0"});
    params.put("center_id",new String[]{"7"});
    params.put("date",new String[]{"25-10-2019"});
    params.put("time",new String[]{"18:00"});
    params.put("appointment_category_list",new String[]{"DOC,DOC"});
    return params;
  }
  
  public Map createMockParamsForGetSlotsSecResLenError() {
    Map params = new HashMap();
    params.put("secondary_resources_id_list",new String[]{"-1"});
    params.put("dept_list",new String[]{"DEP0001,DEP0001"});
    params.put("interdependency_list",new String[]{"0,0"});
    params.put("center_id",new String[]{"7"});
    params.put("date",new String[]{"25-10-2019"});
    params.put("time",new String[]{"18:00"});
    params.put("appointment_category_list",new String[]{"DOC,DOC"});
    return params;
  }
  
  public Map createMockParamsForGetSlotsAppCatError() {
    Map params = new HashMap();
    params.put("secondary_resources_id_list",new String[]{"-1,-1"});
    params.put("dept_list",new String[]{"DEP0001,DEP0001"});
    params.put("interdependency_list",new String[]{"0,0"});
    params.put("center_id",new String[]{"7"});
    params.put("date",new String[]{"25-10-2019"});
    params.put("time",new String[]{"18:00"});
    return params;
  }
  
  public Map createMockParamsForGetSlotsAppCatLenError() {
    Map params = new HashMap();
    params.put("secondary_resources_id_list",new String[]{"-1,-1"});
    params.put("dept_list",new String[]{"DEP0001,DEP0001"});
    params.put("interdependency_list",new String[]{"0,0"});
    params.put("center_id",new String[]{"7"});
    params.put("date",new String[]{"25-10-2019"});
    params.put("time",new String[]{"18:00"});
    params.put("appointment_category_list",new String[]{"DOC"});
    return params;
  }
  
  public Map createMockParamsForGetSlotsDepError() {
    Map params = new HashMap();
    params.put("secondary_resources_id_list",new String[]{"-1,-1"});
    params.put("interdependency_list",new String[]{"0,0"});
    params.put("center_id",new String[]{"7"});
    params.put("date",new String[]{"25-10-2019"});
    params.put("time",new String[]{"18:00"});
    params.put("appointment_category_list",new String[]{"DOC,DOC"});
    return params;
  }
  
  public Map createMockParamsForGetSlotsDepLenError() {
    Map params = new HashMap();
    params.put("secondary_resources_id_list",new String[]{"-1,-1"});
    params.put("dept_list",new String[]{"DEP0001"});
    params.put("interdependency_list",new String[]{"0,0"});
    params.put("center_id",new String[]{"7"});
    params.put("date",new String[]{"25-10-2019"});
    params.put("time",new String[]{"18:00"});
    params.put("appointment_category_list",new String[]{"DOC,DOC"});
    return params;
  }
  
  public Map createMockParamsForGetSlotsDependencyError() {
    Map params = new HashMap();
    params.put("secondary_resources_id_list",new String[]{"-1,-1"});
    params.put("dept_list",new String[]{"DEP0001,DEP0001"});
    params.put("center_id",new String[]{"7"});
    params.put("date",new String[]{"25-10-2019"});
    params.put("time",new String[]{"18:00"});
    params.put("appointment_category_list",new String[]{"DOC,DOC"});
    return params;
  }
  
  public Map createMockParamsForGetSlotsDependencyLenError() {
    Map params = new HashMap();
    params.put("secondary_resources_id_list",new String[]{"-1,-1"});
    params.put("dept_list",new String[]{"DEP0001,DEP0001"});
    params.put("interdependency_list",new String[]{"0"});
    params.put("center_id",new String[]{"7"});
    params.put("date",new String[]{"25-10-2019"});
    params.put("time",new String[]{"18:00"});
    params.put("appointment_category_list",new String[]{"DOC,DOC"});
    return params;
  }
  
  public List getResourceListForSLots() {
    List list = new ArrayList();
    Map map1 = new HashMap();
    map1.put("resource_id","DOC0190");
    map1.put("resource_name","Dr Akshay");
    list.add(map1);
    Map map2 = new HashMap();
    map2.put("resource_id","DOC0190");
    map2.put("resource_name","Dr Akshay");
    list.add(map2);
    Map map3 = new HashMap();
    map3.put("resource_id","DOC0190");
    map3.put("resource_name","Dr Akshay");
    list.add(map3);
    return list;
  }
  
  public void MockfunctionOverridesForCancelBulkAppointments() {
    Mockito.when(appointmentService.updateAppointmentsStatus(Mockito.any(Map.class))).thenReturn(new HashMap());
  }
  
  @Test
  public void cancelAppointments() {
    MockfunctionOverridesForCancelBulkAppointments();
    try {
      Map map = schedulerBulkAppointmentsService.cancelBulkAppointments(new HashMap());
      Assert.assertNotNull(map);
    } catch (Exception e) {
      
    }
  }
}
