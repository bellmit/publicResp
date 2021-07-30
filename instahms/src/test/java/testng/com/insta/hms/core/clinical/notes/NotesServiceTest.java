package testng.com.insta.hms.core.clinical.notes;

import com.insta.hms.common.DynaBeanBuilder;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.preferences.ippreferences.IpPreferencesService;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.core.billing.BillService;
import com.insta.hms.core.clinical.forms.FormParameter;
import com.insta.hms.core.clinical.notes.NotesRepository;
import com.insta.hms.core.clinical.notes.NotesService;
import com.insta.hms.core.clinical.notes.NotesValidator;
import com.insta.hms.core.clinical.order.doctoritems.DoctorOrderItemService;
import com.insta.hms.core.patient.registration.PatientInsurancePlansService;
import com.insta.hms.core.patient.registration.RegistrationService;
import com.insta.hms.exception.ValidationErrorMap;
import com.insta.hms.mdm.centers.CenterService;
import com.insta.hms.mdm.consultationtypes.ConsultationTypesService;
import com.insta.hms.mdm.doctors.DoctorService;
import com.insta.hms.mdm.hospitalroles.HospitalRoleService;
import com.insta.hms.mdm.notetypes.NoteTypeTemplateRepository;
import com.insta.hms.mdm.notetypes.NoteTypesRepository;
import com.insta.hms.mdm.notetypes.NoteTypesService;
import org.apache.commons.beanutils.BasicDynaBean;
import org.junit.Assert;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTransactionalTestNGSpringContextTests;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import testng.utils.TestRepoInit;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Test
@ContextConfiguration(locations = { "classpath:spring/test-spring-config.xml" })
public class NotesServiceTest extends AbstractTransactionalTestNGSpringContextTests{
  
  @Mock
  private HospitalRoleService hospitalRoleService;
  
  @Spy
  private NoteTypesRepository noteTypesRepository;
  
  @Spy
  private NoteTypeTemplateRepository noteTypeTemplateRepository;
  
  @Mock
  private SessionService sessionService;;
  
  @Mock
  private NoteTypesService noteTypesService;
  
  @Mock
  private BillService billService;
  
  @Mock
  private RegistrationService registrationService;
  
  @Mock
  private PatientInsurancePlansService patientInsurancePlansService;
  
  @Mock
  private DoctorOrderItemService doctorOrderService;
  
  @Mock
  private NotesValidator notesValidator;
  
  @Mock
  private CenterService centerService;
  
  @Mock
  private IpPreferencesService ipPrefService;
  
  
  @Mock
  private ConsultationTypesService consultationTypesService;
  
  @Spy
  private NotesRepository notesRepository;
  
  @Mock
  private DoctorService doctorService;
  
  @Spy
  @InjectMocks
  private NotesService service;
  
  private Logger logger = LoggerFactory.getLogger(NotesService.class);

  private Map<String, Object> dbDataMap = null;
  
  @BeforeMethod
  public void initMocks() {
    logger.info("Before every Notes service test");
    TestRepoInit testRepo = new TestRepoInit();
    testRepo.insert("note_type_master");
    testRepo.insert("note_type_template_master");
    testRepo.insert("patient_notes");
    dbDataMap = testRepo.initializeRepo();
    MockitoAnnotations.initMocks(this);
  }
  
  @SuppressWarnings("unchecked")
  @Test
  public void saveSectionInsert() {
    Map<String, Object> reqBody = getSaveReqBody();
    Map<String, Object> errorMap = new HashMap<String, Object>();
    Mockito.when(sessionService.getSessionAttributes()).thenReturn(getSessionServiceAttributes());
    Mockito.when(notesValidator.validateDuplicateDraftNoteType(Mockito.any(String.class),
        Mockito.any(ValidationErrorMap.class), Mockito.any(String.class))).thenReturn(true);
    Mockito.when(notesValidator
        .validateSaveNoteParams(Mockito.anyMap(), Mockito.any(ValidationErrorMap.class)))
        .thenReturn(true);
    Mockito.doNothing().when(service).setDoctorCharges(Mockito.anyMap(), Mockito.any(String.class),
        Mockito.any(BasicDynaBean.class), Mockito.any(Integer.class), Mockito.anyMap(),
        Mockito.anyMap(), Mockito.any(Integer.class));
    Mockito.when(ipPrefService.getPreferences()).thenReturn(getPreferences());
    Mockito.when(noteTypesRepository.getNotesTypeIdByName(Mockito.any(String.class)))
        .thenReturn(getAdmissionReqNotesTypeId());
    FormParameter fp = new FormParameter("", "", "", "", new Object[] {}, "");
    Map<String, Object> resMap = service.saveSection(reqBody, null, fp, errorMap);
    Map<String, Object> insMap = (Map<String, Object>) resMap.get("insert");
    Map<String, Object> insInd = (Map<String, Object>) insMap.get("0");
    BasicDynaBean notebean = notesRepository.findByKey("note_id", insInd.get("note_id"));
    Assert.assertEquals(true, notebean != null);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void saveSectionInsertNull() {
    Map<String, Object> reqBody = getSaveReqBody();
    Map<String, Object> errorMap = new HashMap<String, Object>();
    Mockito.when(sessionService.getSessionAttributes()).thenReturn(getSessionServiceAttributes());
    Mockito.when(notesValidator.validateDuplicateDraftNoteType(Mockito.any(String.class),
        Mockito.any(ValidationErrorMap.class),Mockito.any(String.class))).thenReturn(true);
    Mockito.doNothing().when(service).setDoctorCharges(Mockito.anyMap(),
        Mockito.any(String.class),Mockito.any(BasicDynaBean.class),Mockito.any(Integer.class), 
        Mockito.anyMap(), Mockito.anyMap(),Mockito.any(Integer.class));
    Mockito.when(ipPrefService.getPreferences()).thenReturn(getPreferences());
    Mockito.when(noteTypesRepository.getNotesTypeIdByName(Mockito.any(String.class)))
            .thenReturn(getAdmissionReqNotesTypeId());
    FormParameter fp = new FormParameter("", "", "", "", new Object[] {}, "");
    Map<String, Object> resMap = service.saveSection(reqBody, null, fp, errorMap);
    Assert.assertEquals(true, resMap == null);   
  }

  private Integer getAdmissionReqNotesTypeId() {
    return -1;
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Test
  public void saveSectionUpdate() {
    List<Map<String, Object>> patientNotes = (List) dbDataMap.get("patient_notes");
    Map<String, Object> reqBody = getSaveUpdateReqBody(patientNotes.get(0));
    Mockito.when(sessionService.getSessionAttributes()).thenReturn(getSessionServiceAttributes());
    Map<String, Object> errorMap = new HashMap<String, Object>();
    Mockito.when(notesValidator.validateEditNoteParams(Mockito.anyMap(),
        Mockito.any(ValidationErrorMap.class))).thenReturn(true);
    Mockito.when(notesValidator.isEditableNoteType(Mockito.anyMap(),
        Mockito.any(ValidationErrorMap.class))).thenReturn(true);
    Mockito.when(notesValidator.validateDuplicateDraftNoteType(Mockito.any(String.class),
        Mockito.any(ValidationErrorMap.class),Mockito.any(String.class))).thenReturn(true);
    Mockito.when(ipPrefService.getPreferences()).thenReturn(getPreferences());
    FormParameter fp = new FormParameter("","","","",new Object[] {},"");
    Map<String, Object> resMap = service.saveSection(reqBody, null, fp, errorMap);
    Map<String, Object> updateMap = (Map<String, Object>)resMap.get("update");
    Map<String, Object> updateInd = (Map<String, Object>)updateMap.get("0");
    BasicDynaBean notebean = notesRepository.findByKey("note_id",updateInd.get("new_note_id"));
    Assert.assertEquals(true, notebean != null);   
  }
  
  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Test
  public void saveSectionUpdateDraft() {
    List<Map<String, Object>> patientNotes = (List) dbDataMap.get("patient_notes");
    Map<String, Object> reqBody = getSaveUpdateReqBody(patientNotes.get(1));
    Mockito.when(sessionService.getSessionAttributes()).thenReturn(getSessionServiceAttributes());
    Mockito.when(notesValidator.validateDuplicateDraftNoteType(Mockito.any(String.class),
        Mockito.any(ValidationErrorMap.class),Mockito.any(String.class))).thenReturn(true);
    Map<String, Object> errorMap = new HashMap<String, Object>();
    Mockito.when(notesValidator.validateEditNoteParams(Mockito.anyMap(),
        Mockito.any(ValidationErrorMap.class))).thenReturn(true);
    Mockito.when(notesValidator.isEditableNoteType(Mockito.anyMap(),
        Mockito.any(ValidationErrorMap.class))).thenReturn(true);
    Mockito.doNothing().when(service).setDoctorCharges(Mockito.anyMap(),
        Mockito.any(String.class),Mockito.any(BasicDynaBean.class),
        Mockito.any(Integer.class), Mockito.anyMap(), 
        Mockito.anyMap(), Mockito.any(Integer.class));
    Mockito.when(ipPrefService.getPreferences()).thenReturn(getPreferences());
    FormParameter fp = new FormParameter("","","","",new Object[] {},"");
    Map<String, Object> resMap = service.saveSection(reqBody, null, fp, errorMap);
    Map<String, Object> updateMap = (Map<String, Object>)resMap.get("update");
    Map<String, Object> updateInd = (Map<String, Object>)updateMap.get("0");
    BasicDynaBean notebean = notesRepository.findByKey("note_id",updateInd.get("note_id"));
    Assert.assertEquals(true, notebean != null);  
  }
  
  @Test
  public void deleteSection(){
    boolean value = service.deleteSection(null, null, null);
    Assert.assertEquals(false, value);  
  }
  
  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Test
  public void getPatientNotes(){
    Map<String, String[]> paramMap = new HashMap<>();
    List<Map<String, Object>> patientNotesList = (List) dbDataMap.get("patient_notes");
    String patientId = (String) patientNotesList.get(0).get("patient_id");
    Mockito.when(sessionService.getSessionAttributes()).thenReturn(getSessionServiceAttributes());
      try {
        String dateTime="2018-11-19 10:03:55";
        SimpleDateFormat formatter=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = formatter.parse(dateTime);
        formatter.applyPattern("dd-MM-yyyy");
        String startDate = formatter.format(date);        
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, 1);
        Date openDt = cal.getTime();
        String endDate = formatter.format(openDt);
        paramMap.put("start_date", new String[]{startDate});
        paramMap.put("end_date", new String[]{endDate});
      } catch (ParseException e) {
        e.printStackTrace();
      }
      PagedList patientNotes = service.getPatientNotes(patientId, paramMap);
      Assert.assertEquals(true, patientNotes.getDtoList().size() >1);  
  }
  
  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Test
  public void getPatientNotesNonAdmin(){
    Map<String, String[]> paramMap = new HashMap<>();
    List<Map<String, Object>> patientNotesList = (List) dbDataMap.get("patient_notes");
    String patientId = (String) patientNotesList.get(0).get("patient_id");
    Mockito.when(sessionService.getSessionAttributes()).thenReturn(getSessionServiceAttributesNonAdmin());
      try {
        String dateTime="2018-11-19 10:03:55";
        SimpleDateFormat formatter=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = formatter.parse(dateTime);
        formatter.applyPattern("dd-MM-yyyy");
        String startDate = formatter.format(date);        
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, 1);
        Date openDt = cal.getTime();
        String endDate = formatter.format(openDt);
        paramMap.put("start_date", new String[]{startDate});
        paramMap.put("end_date", new String[]{endDate});
      } catch (ParseException e) {
        e.printStackTrace();
      }
      PagedList patientNotes = service.getPatientNotes(patientId, paramMap);
      Assert.assertEquals(true, patientNotes.getDtoList().size() >1);  
  }
  
  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Test
  public void getSectionDetailsFromLastSavedForm(){
    List<Map<String, Object>> patientNotesList = (List) dbDataMap.get("patient_notes");
    String patientId = (String) patientNotesList.get(0).get("patient_id");
    Mockito.when(sessionService.getSessionAttributes()).thenReturn(getSessionServiceAttributes());
    FormParameter parameters = new FormParameter(null,null,null,patientId,null, null);
    Map<String, Object> resMap = service.getSectionDetailsFromLastSavedForm(parameters);
    Assert.assertEquals(true, resMap.containsKey("records"));    
  }
  
  @Test
  public void getUserNoteTypesAdminRole(){
    Mockito.when(sessionService.getSessionAttributes()).thenReturn(getSessionServiceAttributes());
    List<Map<String, Object>> userNotes = service.getUserNoteTypes();
    Assert.assertEquals(true, userNotes.size() > 0);  
  }
  
  @SuppressWarnings("unused")
  @Test
  public void getUserNoteTypesNonAdmin(){
    Mockito.when(sessionService.getSessionAttributes()).thenReturn(getSessionServiceAttributesNonAdmin());
    List<Map<String, Object>> userNotes = service.getUserNoteTypes();
  }
  
  @Test
  public void getConsultationTypesForRateplan(){
    DynaBeanBuilder builder = new DynaBeanBuilder();
    builder.add("center_id");
    builder.add("health_authority");
    BasicDynaBean bean = builder.build();
    Mockito.when(centerService.findByPk(Mockito.anyMap())).thenReturn(bean);
    Mockito.when(consultationTypesService.getConsultationTypes(
        Mockito.anyString(),
        Mockito.any(String.class),
        Mockito.any(String.class)))
        .thenReturn(basiBeanList());
    List<BasicDynaBean> conList = service.getConsultationTypesForRateplan(null, 1);
    Assert.assertEquals(0, conList.size());
  }
  
  @SuppressWarnings("unchecked")
  @Test
  public void setDoctorChargesExe(){
    Map<String, Object> row = new HashMap<>();
    row.put("patient_id", "OP098011");
    row.put("consultation_type_id", 2);
    row.put("on_behalf_doctor_id", "DOC0075");
    String userName = "InstaAdmin";
    Integer centerId = 2;
    Integer maxBillableConsDay = 2;
    DynaBeanBuilder builder = new DynaBeanBuilder();
    builder.add("charge_id");
    builder.add("consultation_type_id");
    builder.add("billable_consultation");
    BasicDynaBean patientNoteBean = builder.build();   
    Map<String,Integer> billableConsDocs = new HashMap<>();
    Map<String, Object> responseMap = new HashMap<>();
    Mockito.when(billService.getBean()).thenReturn(visitBean());
    Mockito.when(registrationService.findByKey(Mockito.any(String.class)))
    .thenReturn(visitBean());
    Mockito.doNothing().when(billService).generateBill(Mockito.anyString(), Mockito.anyString(),
        Mockito.anyString(), Mockito.any(BasicDynaBean.class), Mockito.anyString(), 
        Mockito.anyBoolean(), Mockito.any(BigDecimal.class),
        Mockito.anyString(), Mockito.anyString());
    Mockito.when(registrationService.getBillPatientInfo(Mockito.anyString(), Mockito.anyString()))
    .thenReturn(headerBean());
    Mockito.when(registrationService.regBaseDoctor(Mockito.anyString(),Mockito.anyString(),
        Mockito.anyString(),Mockito.anyString(),Mockito.anyString(),
        Mockito.anyInt())).thenReturn(orderBean());
    Mockito.when(doctorOrderService.insertOrderCharges( Mockito.anyBoolean(),Mockito.any(BasicDynaBean.class),
        Mockito.any(BasicDynaBean.class), Mockito.any(BasicDynaBean.class),
        Mockito.any(String[].class),Mockito.any(Integer[].class),Mockito.any(int[].class),
        Mockito.anyString(),Mockito.anyString(),Mockito.any(Integer.class),
        Mockito.anyBoolean(),Mockito.anyMap())).thenReturn(chargesList());
    service.setDoctorCharges(row, userName, patientNoteBean, centerId, billableConsDocs,responseMap,maxBillableConsDay);
  }
  
  
  @SuppressWarnings("unchecked")
  @Test
  public void setDoctorCharges(){
    Map<String, Object> row = new HashMap<>();
    row.put("patient_id", "OP098011");
    row.put("consultation_type_id", 2);
    row.put("on_behalf_doctor_id", "DOC0075");
    String userName = "InstaAdmin";
    Integer centerId = 2;
    DynaBeanBuilder builder = new DynaBeanBuilder();
    Map<String,Integer> billableConsDocs = new HashMap<>();
    Map<String, Object> responseMap = new HashMap<>();
    Integer maxBillableConsDay = 2;
    builder.add("charge_id");
    builder.add("consultation_type_id", Integer.class);
    builder.add("billable_consultation");
    BasicDynaBean patientNoteBean = builder.build();   
    Mockito.when(billService.getBean()).thenReturn(billBean());
    Mockito.when(registrationService.findByKey(Mockito.any(String.class)))
    .thenReturn(visitBean());
    Mockito.doNothing().when(billService).generateBill(Mockito.anyString(), Mockito.anyString(),
        Mockito.anyString(), Mockito.any(BasicDynaBean.class), Mockito.anyString(), 
        Mockito.anyBoolean(), Mockito.any(BigDecimal.class),
        Mockito.anyString(), Mockito.anyString());
    Mockito.when(registrationService.getBillPatientInfo(Mockito.anyString(), Mockito.anyString()))
    .thenReturn(headerBean());
    Mockito.when(registrationService.regBaseDoctor(Mockito.anyString(),Mockito.anyString(),
        Mockito.anyString(),Mockito.anyString(),Mockito.anyString(),
        Mockito.anyInt())).thenReturn(orderBean());
    Mockito.when(doctorOrderService.insertOrderCharges( Mockito.anyBoolean(),Mockito.any(BasicDynaBean.class),
        Mockito.any(BasicDynaBean.class), Mockito.any(BasicDynaBean.class),
        Mockito.any(String[].class),Mockito.any(Integer[].class),Mockito.any(int[].class),
        Mockito.anyString(),Mockito.anyString(),Mockito.any(Integer.class),
        Mockito.anyBoolean(),Mockito.anyMap())).thenReturn(chargesList());
    service.setDoctorCharges(row, userName, patientNoteBean, centerId, billableConsDocs,responseMap,maxBillableConsDay);
  }
  
  
  private List<BasicDynaBean> chargesList() {
    List<BasicDynaBean> chargeList = new ArrayList<BasicDynaBean>();
    DynaBeanBuilder builder = new DynaBeanBuilder();
    builder.add("charge_id");
    BasicDynaBean bean = builder.build();
    chargeList.add(bean);
    return chargeList;
  }

  private BasicDynaBean orderBean() {
    DynaBeanBuilder builder = new DynaBeanBuilder();
    builder.add("common_order_id");
    builder.add("username");
    builder.add("patient_id");
    builder.add("consultation_id", Integer.class);
    builder.add("presc_doctor_id");
    return builder.build();
  }
  
  private BasicDynaBean visitBean() {
    DynaBeanBuilder builder = new DynaBeanBuilder();
    builder.add("org_id");
    return builder.build();
  }
  
  private BasicDynaBean billBean() {
    DynaBeanBuilder builder = new DynaBeanBuilder();
    builder.add("bill_no");
    BasicDynaBean bean = builder.build();
    bean.set("bill_no", "BN0001");    
    return bean;
  }

  private BasicDynaBean headerBean() {
    DynaBeanBuilder builder = new DynaBeanBuilder();
    builder.add("commonorderid");
    builder.add("user_name");
    builder.add("patient_id");
    BasicDynaBean bean = builder.build();
    bean.set("commonorderid", "2");
    bean.set("user_name", "InstaAdmin");
    bean.set("patient_id", "IP0001");
    return bean;
  }

  private List<BasicDynaBean> basiBeanList() {
    List<BasicDynaBean> beanlist = new ArrayList<BasicDynaBean>();
    return beanlist;
  }

  private Map<String, Object> getSaveUpdateReqBody(Map<String, Object> patientNoteMap) {
    Map<String, Object> updateparams = new HashMap<String, Object>();
    List<Map<String, Object>> insertList = new ArrayList<Map<String,Object>>();
    List<Map<String, Object>> updateList = new ArrayList<Map<String,Object>>();
    Map<String, Object> updateMap = new HashMap<String, Object>();
    updateMap.put("note_id", patientNoteMap.get("note_id"));
    updateMap.put("patient_id", patientNoteMap.get("patient_id"));
    updateMap.put("note_content", "<p>Note update content </p>");
    updateMap.put("note_type_id", 1);
    updateMap.put("behalf_doctor_id", "DOC0075");
    updateMap.put("billable_consultation", "Y");
    updateMap.put("save_status","F");
    updateMap.put("consultation_type_id", 0);
    updateList.add(updateMap);
    updateparams.put("insert", insertList);
    updateparams.put("update", updateList);
    
    return updateparams;
  }

  private Map<String, Object> getSaveReqBody() {
    Map<String, Object> reqMap = new HashMap<String, Object>();
    List<Map<String, Object>> insertList = new ArrayList<Map<String,Object>>();
    List<Map<String, Object>> updateList = new ArrayList<Map<String,Object>>();
    Map<String, Object> insMap = new HashMap<String, Object>();
    insMap.put("patient_id", "OP098011");
    insMap.put("note_content", "<p>Note content </p>");
    insMap.put("note_type_id", 3);
    insMap.put("behalf_doctor_id", "DOC0075");
    insMap.put("billable_consultation", "Y");
    insMap.put("save_status","F");
    insMap.put("consultation_type_id", 0);
    insertList.add(insMap);
    reqMap.put("insert", insertList);
    reqMap.put("update", updateList);
    return reqMap;
  }
  
  public Map<String, Object> getSessionServiceAttributes() {
    Map<String, Object> sessionAttributes = new HashMap<String, Object>();
    sessionAttributes.put("userId", "InstaAdmin");
    sessionAttributes.put("centerId", 3);
    sessionAttributes.put("roleId", 1);
    return sessionAttributes;
  }
  public Map<String, Object> getSessionServiceAttributesNonAdmin() {
    Map<String, Object> sessionAttributes = new HashMap<String, Object>();
    sessionAttributes.put("userId", "InstaAdmin");
    sessionAttributes.put("centerId", 3);
    sessionAttributes.put("roleId", 4);
    return sessionAttributes;
  }
  
  public BasicDynaBean getPreferences() {
    DynaBeanBuilder builder = new DynaBeanBuilder();
    builder.add("max_billable_cons_day", Integer.class);
    BasicDynaBean ipprefs = builder.build();
    ipprefs.set("max_billable_cons_day", 2);
    return ipprefs;
  }
  
}
