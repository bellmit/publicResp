package testng.com.insta.hms.mdm.resourceavailability;

import com.bob.hms.common.DateUtil;
import com.insta.hms.common.DynaBeanBuilder;
import com.insta.hms.common.preferences.genericpreferences.GenericPreferencesService;
import com.insta.hms.core.scheduler.AppointmentService;
import com.insta.hms.exception.EntityNotFoundException;
import com.insta.hms.exception.ValidationException;
import com.insta.hms.mdm.doctors.DoctorRepository;
import com.insta.hms.mdm.doctors.DoctorService;
import com.insta.hms.mdm.resourceavailability.ResourceAvailabilityRepository;
import com.insta.hms.mdm.resourceavailability.ResourceAvailabilityService;
import com.insta.hms.mdm.resourceoverride.ResourceOverrideRepository;
import com.insta.hms.mdm.resourceoverride.ResourceOverrideService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.Assert;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTransactionalTestNGSpringContextTests;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import testng.utils.TestRepoInit;

import java.sql.Date;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

@Test
@ContextConfiguration(locations = { "classpath:spring/test-spring-config.xml" })
public class ResourceAvailabilityServiceTest extends AbstractTransactionalTestNGSpringContextTests {

  @Mock
  private ResourceAvailabilityService mockService;

  @Mock
  private AppointmentService appointmentService;

  @Spy
  private ResourceOverrideRepository resourceOverrideRepository;

  @Spy
  private DoctorRepository doctorRepository;

  @Mock
  private ResourceOverrideService resourceOverrideService;

  @Mock
  private DoctorService doctorService;

  @Mock
  private GenericPreferencesService genericPreferencesService;

  @Mock
  private ResourceAvailabilityRepository resAvailRepository;

  @InjectMocks
  @Spy
  private ResourceAvailabilityService service;

  private Logger logger = LoggerFactory.getLogger(ResourceAvailabilityService.class);

  private Map<String, Object> dbDataMap = null;

  @BeforeMethod
  public void initMocks() {
    logger.info("Before every ResourceAvailabilityService test");
    MockitoAnnotations.initMocks(this);
    TestRepoInit testRepo = new TestRepoInit();
    testRepo.insert("doctors");
    dbDataMap = testRepo.initializeRepo();
  }

  @Test
  public void getAvailableSlotsTestWithOverride() {
    BasicDynaBean doctorBean = doctorRepository.listAll().get(0);
    String doctorId = (String) doctorBean.get("doctor_id");
    mockData();
    List<BasicDynaBean> appointments = new ArrayList<>();
    Mockito
        .when(
            appointmentService.getAppointmentCountAndTime(Mockito.anyString(),
                Mockito.any(Date.class))).thenReturn(appointments);

    Mockito.when(
        resourceOverrideService.getResourceOverrides(Mockito.anyString(), Mockito.anyString(),
            Mockito.any(Date.class), Mockito.any(Date.class), Mockito.anyString(),
            Mockito.any(Integer.class))).thenReturn(mockAvailability());

    Map<String, Object> slots = service.getAvailableSlots(null, doctorId, null, null,null,null,null,"Y");
    List slotsList = (List) slots.get("slots");
    Assert.assertEquals(16, slotsList.size());
    Assert.assertEquals(DateUtil.getCurrentDate(), slots.get("first_available_date"));
    logger.info("{}", slots);

  }
  
  @Test
  public void getAvailableSlotsTestWithOverrideWithDate() {
    BasicDynaBean doctorBean = doctorRepository.listAll().get(0);
    String doctorId = (String) doctorBean.get("doctor_id");
    mockData();
    List<BasicDynaBean> appointments = new ArrayList<>();
    Mockito
        .when(
            appointmentService.getAppointmentCountAndTime(Mockito.anyString(),
                Mockito.any(Date.class))).thenReturn(appointments);

    Mockito.when(
        resourceOverrideService.getResourceOverrides(Mockito.anyString(), Mockito.anyString(),
            Mockito.any(Date.class), Mockito.any(Date.class), Mockito.anyString(),
            Mockito.any(Integer.class))).thenReturn(mockAvailability());

    Map<String, Object> slots = service.getAvailableSlots(DateUtil.currentDate("yyyy-MM-dd"), doctorId, null, null,null,null,null,"Y");
    List slotsList = (List) slots.get("slots");
    Assert.assertEquals(16, slotsList.size());
    Assert.assertEquals(DateUtil.getCurrentDate(), slots.get("first_available_date"));
    logger.info("{}", slots);

  }
  
  @Test
  public void getAvailableSlotsTestWithOverrideWithAppointment() {
    BasicDynaBean doctorBean = doctorRepository.listAll().get(0);
    String doctorId = (String) doctorBean.get("doctor_id");
    mockData();
    Mockito
        .when(
            appointmentService.getAppointmentCountAndTime(Mockito.anyString(),
                Mockito.any(Date.class))).thenReturn(mockAppointment());

    Mockito.when(
        resourceOverrideService.getResourceOverrides(Mockito.anyString(), Mockito.anyString(),
            Mockito.any(Date.class), Mockito.any(Date.class), Mockito.anyString(),
            Mockito.any(Integer.class))).thenReturn(mockAvailability());

    Map<String, Object> slots = service.getAvailableSlots(null, doctorId, null, null,null,null,null,"Y");
    List slotsList = (List) slots.get("slots");
    Assert.assertEquals(15, slotsList.size());
    Assert.assertEquals(DateUtil.getCurrentDate(), slots.get("first_available_date"));
    logger.info("{}", slots);

  }

  @Test
  public void getAvailableSlotsTestWithDefaultAvailability() {
    BasicDynaBean doctorBean = doctorRepository.listAll().get(0);
    String doctorId = (String) doctorBean.get("doctor_id");

    mockData();
    List<BasicDynaBean> overrideMock = new ArrayList<>();

    Mockito.when(
        resourceOverrideService.getResourceOverrides(Mockito.anyString(), Mockito.anyString(),
            Mockito.any(Date.class), Mockito.any(Date.class), Mockito.anyString(),
            Mockito.any(Integer.class))).thenReturn(overrideMock);
    Mockito
        .doReturn(mockAvailability())
        .when(service)
        .getDefaultResourceAvailabilities(Mockito.anyString(), Mockito.any(Integer.class),
            Mockito.anyString(), Mockito.anyString(), Mockito.any(Integer.class));
    Map<String, Object> slots = service.getAvailableSlots(null, doctorId, 1, null,null,null,null,"Y");
    List slotsList = (List) slots.get("slots");
    logger.info("{}", slots);
    Assert.assertEquals(16, slotsList.size());
    Assert.assertEquals(DateUtil.getCurrentDate(), slots.get("first_available_date"));

  }
  
  @Test(expectedExceptions = ValidationException.class)
  public void getAvailableSlotsDateException() {
    BasicDynaBean doctorBean = doctorRepository.listAll().get(0);
    String doctorId = (String) doctorBean.get("doctor_id");
    Map<String, Object> slots = service.getAvailableSlots("abcd",doctorId, 1, null,null,null,null,"Y");

  }

  private List<BasicDynaBean> mockAvailability() {
    List<BasicDynaBean> availability = new ArrayList<>();
    DynaBeanBuilder builder = new DynaBeanBuilder();
    builder.add("from_time", Time.class);
    builder.add("to_time", Time.class);
    builder.add("center_id", Integer.class);
    builder.add("availability_status");
    builder.add("visit_mode");
    BasicDynaBean bean = builder.build();
    bean.set("from_time", Time.valueOf("06:00:00"));
    bean.set("to_time", Time.valueOf("10:00:00"));
    bean.set("center_id", 1);
    bean.set("availability_status", "A");
    bean.set("visit_mode", "I");
    availability.add(bean);
    return availability;
  }
  
  private List<BasicDynaBean> mockAppointment() {
    List<BasicDynaBean> appointment = new ArrayList<>();
    DynaBeanBuilder builder = new DynaBeanBuilder();
    builder.add("time", Time.class);
    builder.add("duration", Integer.class);
    builder.add("center_id", Integer.class);
    builder.add("count",Long.class);
    BasicDynaBean bean = builder.build();
    bean.set("time", Time.valueOf("06:00:00"));
    bean.set("duration", 15);
    bean.set("center_id", 1);
    bean.set("count", 1L);
    appointment.add(bean);
    return appointment;
  }

  private void mockData() {
    BasicDynaBean doctorBean = doctorRepository.listAll().get(0);
    doctorBean.set("overbook_limit", 0);
    Mockito.when(doctorService.getDoctorById(Mockito.anyString())).thenReturn(doctorBean);

    DynaBeanBuilder prefBeanBuilder = new DynaBeanBuilder();
    prefBeanBuilder.add("max_centers_inc_default", Integer.class);
    BasicDynaBean prefBean = prefBeanBuilder.build();
    prefBean.set("max_centers_inc_default", 12);
    Mockito.when(genericPreferencesService.getAllPreferences()).thenReturn(prefBean);

    Mockito.when(resAvailRepository.getDefaultDuration(Mockito.anyString(), Mockito.anyString()))
        .thenReturn(15);
  }
  
//TODO fix later(timezone error)
//public void getFirstAvailableSlotTestWithOverride() {
//  BasicDynaBean doctorBean = doctorRepository.listAll().get(0);
//  String doctorId = (String) doctorBean.get("doctor_id");
//  mockData();
//  List<BasicDynaBean> appointments = new ArrayList<>();
//  Mockito
//      .when(
//          appointmentService.getAppointmentCountAndTime(Mockito.anyString(),
//              Mockito.any(Date.class))).thenReturn(appointments);
//
//  Mockito.when(
//      resourceOverrideService.getResourceOverrides(Mockito.anyString(), Mockito.anyString(),
//          Mockito.any(Date.class), Mockito.any(Date.class), Mockito.anyString(),
//          Mockito.any(Integer.class))).thenReturn(mockAvailability());
//  
//  Calendar calendar = Calendar.getInstance();
//  calendar.add(Calendar.DAY_OF_YEAR, 1);
//  SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//  String tomorrowDateStr= sdf.format(calendar.getTime());
//
//  Map<String, Object> slots = service.getFirstAvailableSlots(tomorrowDateStr, new String[] {doctorId}, null, null);
//  Map<String,List<Map<String,String>>> slotsMap = (Map<String, List<Map<String,String>>>) slots.get("slots");
//  List<Map<String, String>> doctSlot = slotsMap.get(doctorId);
//  Map<String,String> centerSlotObject = doctSlot.get(0);
//  Assert.assertEquals(1, slotsMap.size());
//  Assert.assertEquals(1, centerSlotObject.get("center_id"));
//  String expectedSlot= tomorrowDateStr + "T" + DateUtil.formatISO8601Time(java.sql.Time.valueOf("06:00:00"));
//  Assert.assertEquals(expectedSlot, centerSlotObject.get("timeslot"));
//
//}
@Test(expectedExceptions = ValidationException.class)
public void getFirstAvailableSlotTestWithOverrideDateParseException() {
  BasicDynaBean doctorBean = doctorRepository.listAll().get(0);
  String doctorId = (String) doctorBean.get("doctor_id");
  service.getFirstAvailableSlots("abcd", new String[] {doctorId}, null, null,null,null);
}
@Test(expectedExceptions = EntityNotFoundException.class)
public void getFirstAvailableSlotTestWithOverrideEntityException() {
  service.getFirstAvailableSlots(null, new String[] {"DOC0321"}, null, null,null,null);
}
}
