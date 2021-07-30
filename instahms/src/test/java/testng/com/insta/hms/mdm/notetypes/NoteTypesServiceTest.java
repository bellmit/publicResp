package testng.com.insta.hms.mdm.notetypes;

import static org.testng.Assert.assertEquals;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BasicDynaBean;
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
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import testng.utils.TestRepoInit;

import com.insta.hms.common.PagedList;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.exception.ConversionException;
import com.insta.hms.exception.DuplicateEntityException;
import com.insta.hms.exception.ValidationErrorMap;
import com.insta.hms.mdm.hospitalroles.HospitalRoleService;
import com.insta.hms.mdm.notetypes.NoteTypeTemplateRepository;
import com.insta.hms.mdm.notetypes.NoteTypesRepository;
import com.insta.hms.mdm.notetypes.NoteTypesService;
import com.insta.hms.mdm.notetypes.NoteTypesValidator;

// TODO: Auto-generated Javadoc
/**
 * The Class NoteTypesServiceTest.
 */
@Test
@ContextConfiguration(locations = { "classpath:spring/test-spring-config.xml" })
public class NoteTypesServiceTest extends AbstractTransactionalTestNGSpringContextTests {

  /** The hospital role service. */
  @Mock
  private HospitalRoleService hospitalRoleService;

  /** The note types repository. */
  @Spy
  private NoteTypesRepository noteTypesRepository;

  /** The note types validator. */
  @Mock
  private NoteTypesValidator noteTypesValidator;

  /** The note type template repository. */
  @Spy
  private NoteTypeTemplateRepository noteTypeTemplateRepository;

  /** The session service. */
  @Mock
  private SessionService sessionService;

  /** The service. */
  @Spy
  @InjectMocks
  private NoteTypesService service;

  /** The logger. */
  private Logger logger = LoggerFactory.getLogger(NoteTypesService.class);

  /** The db data map. */
  private Map<String, Object> dbDataMap = null;

  /**
   * Inits the mocks.
   */
  @SuppressWarnings("unchecked")
  @BeforeMethod
  public void initMocks() {
    logger.info("Before every Note type service test");
    MockitoAnnotations.initMocks(this);
    Mockito.when(sessionService.getSessionAttributes()).thenReturn(getSessionServiceAttributes());
    Mockito.when(noteTypesValidator.validateTemplateId(Mockito.any(HashMap.class),Mockito.any(ValidationErrorMap.class))).thenReturn(true);
    TestRepoInit testRepo = new TestRepoInit();
    testRepo.insert("note_type_master");
    testRepo.insert("note_type_template_master");
    dbDataMap = testRepo.initializeRepo();
  }

  /**
   * Gets the hospital roles list.
   *
   * @return the hospital roles list
   */
  @Test
  public void getHospitalRolesList() {
    Map<String, Object> hospitalroles = service.getHospitalRolesList();
    assertEquals(true, hospitalroles.containsKey("hospitalroles"), " hospitalroles tested");
  }

  /**
   * Insert note type.
   */
  @SuppressWarnings("unchecked")
  @Test
  public void insertNoteType(){
    Map<String, Object> paramsMap = getInsertParams();
    service.insertNoteType(paramsMap);
    BasicDynaBean bean = noteTypesRepository.findByKey("note_type_name",
        paramsMap.get("note_type_name"));
    Assert.assertEquals(paramsMap.get("note_type_name"), bean.get("note_type_name"));
    Map<String, Object> tempParam = (Map<String, Object>) paramsMap.get("template");
    BasicDynaBean tempbean = noteTypeTemplateRepository.findByKey("template_name",
        tempParam.get("template_name"));
    Assert.assertEquals(tempParam.get("template_name"), tempbean.get("template_name"));
  }

  /**
   * Update note type.
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Test
  public void updateNoteType() throws ConversionException{
    List<Map<String, Object>> noteTypesList = (List) dbDataMap.get("note_type_master");
    List<Map<String, Object>> noteTypeTempList = (List) dbDataMap.get("note_type_template_master");
    if (!noteTypesList.isEmpty() && !noteTypeTempList.isEmpty()) {
      Map<String, Object> updateparams = getUpdateParams(noteTypesList.get(0),
          noteTypeTempList.get(0));
      service.updateNoteType(updateparams);
      BasicDynaBean bean = noteTypesRepository.findByKey("note_type_id",
          Integer.parseInt((String) updateparams.get("note_type_id")));
      Assert.assertEquals(updateparams.get("note_type_name"), bean.get("note_type_name"));
      Map<String, Object> tempParam = (Map<String, Object>) updateparams.get("template");
      BasicDynaBean tempbean = noteTypeTemplateRepository.findByKey("template_id",
          Integer.parseInt((String) tempParam.get("template_id")));
      Assert.assertEquals(tempParam.get("template_name"), tempbean.get("template_name"));
    }

  }
  
  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Test
  public void updateNoteTypeWithCreateTemplate() {
    List<Map<String, Object>> noteTypesList = (List) dbDataMap.get("note_type_master");
    List<Map<String, Object>> noteTypeTempList = (List) dbDataMap.get("note_type_template_master");
    if (!noteTypesList.isEmpty() && !noteTypeTempList.isEmpty()) {
      Map<String, Object> updateparams = getUpdateParams(noteTypesList.get(0),
          noteTypeTempList.get(0));
      Map<String, Object> tempParam = (Map<String, Object>) updateparams.get("template");
      tempParam.remove("note_type_id");
      tempParam.remove("template_id");
      tempParam.put("template_name", "new_template_create");
      updateparams.put("template", tempParam);
      service.updateNoteType(updateparams);
     
      BasicDynaBean tempbean = noteTypeTemplateRepository.findByKey("template_name","new_template_create");
      Assert.assertEquals(tempParam.get("template_content"), tempbean.get("template_content"));
    }

  }

  /**
   * Save note type insert.
   */
  @Test
  public void saveNoteTypeInsert() {
    Map<String, Object> paramsMap = getInsertParams();
    Map<String, Object> responseMap = service.saveNoteType(paramsMap);
    Assert.assertEquals(paramsMap, responseMap);
  }

  /**
   * Save note type update.
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Test
  public void saveNoteTypeUpdate() {
    List<Map<String, Object>> noteTypesList = (List) dbDataMap.get("note_type_master");
    List<Map<String, Object>> noteTypeTempList = (List) dbDataMap.get("note_type_template_master");
    if (!noteTypesList.isEmpty() && !noteTypeTempList.isEmpty()) {
      Map<String, Object> updateparams = getUpdateParams(noteTypesList.get(0),
          noteTypeTempList.get(0));
      Map<String, Object> responseMap = service.saveNoteType(updateparams);
      Assert.assertEquals(updateparams, responseMap);
    }
  }

  /**
   * Gets the note types details.
   *
   * @return the note types details
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Test
  public void getNoteTypesDetails() {
    logger.info("note types paged list");
    Map<String, String[]> map = new HashMap<>();
    List<Map<String, Object>> noteTypesList = (List) dbDataMap.get("note_type_master");
    if (!noteTypesList.isEmpty()) {
      try {
        String dateTime=(String) noteTypesList.get(0).get("created_time");
        SimpleDateFormat formatter=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = formatter.parse(dateTime);
        formatter.applyPattern("dd-MM-yyyy");
        String startDate = formatter.format(date);        
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, 1);
        Date openDt = cal.getTime();
        String endDate = formatter.format(openDt);
        map.put("start_date", new String[]{startDate});
        map.put("end_date", new String[]{endDate});
      } catch (ParseException e) {
        e.printStackTrace();
      }
      PagedList noteTypeList = service.getNoteTypesDetails(map);
      Assert.assertEquals(true, noteTypeList.getDtoList().size() > 0);
    }
  }
  
  /**
   * Gets the user note types.
   *
   * @return the user note types
   */
  @Test
  public void getUserNoteTypes() {
    List<BasicDynaBean> userNoteTypes = service.getUserNoteTypes("InstaAdmin");
    Assert.assertEquals(true, userNoteTypes.isEmpty());
  }

  /**
   * Gets the template auto complete.
   *
   * @return the template auto complete
   */
  @SuppressWarnings("unchecked")
  @Test
  public void gettemplateAutoComplete() {
    Map<String, Object> responseMap = service.gettemplateAutoComplete(Collections.EMPTY_MAP);
    assertEquals(true, responseMap.containsKey("dtoList"), " dtoList tested");
    assertEquals(true, responseMap.containsKey("listSize"), " listSize tested");

  }

  /**
   * Template autocomplete.
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Test
  public void templateAutocomplete() {
    List<Map<String, Object>> noteTypeTempList = (List) dbDataMap.get("note_type_template_master");
    if (!noteTypeTempList.isEmpty()) {
      Map<String, String[]> params = new HashMap<String, String[]>();
      params.put("contains", new String[] { "true" });
      params.put("filterText", new String[] { (String) noteTypeTempList.get(0).get("template_name") });
    
      Map<String, Object> responseMap = service.gettemplateAutoComplete(params);
      assertEquals(true, responseMap.containsKey("dtoList"), " dtoList tested");
      assertEquals(true, responseMap.containsKey("listSize"), " listSize tested");
    }
  }

  /**
   * Gets the template details.
   *
   * @return the template details
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Test
  public void getTemplateDetails() {
    List<Map<String, Object>> noteTypeTempList = (List) dbDataMap.get("note_type_template_master");
    if (!noteTypeTempList.isEmpty()) {
      Map<String, String[]> params = new HashMap<String, String[]>();
      params.put("template_id", new String[] { noteTypeTempList.get(0).get("template_id")
          .toString() });
      Map<String, Object> resMap = service.getTemplateDetails(params);
      assertEquals(true, resMap.containsKey("template_content"), "template_content exists ");
    }
  }

  /**
   * Delete template.
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Test
  public void deleteTemplate() {
    List<Map<String, Object>> noteTypeTempList = (List) dbDataMap.get("note_type_template_master");
    if (!noteTypeTempList.isEmpty()) {
      Map<String, Object> map = noteTypeTempList.get(0);
      String template_id = (String) map.get("template_id");
      map.put("template_id", Integer.parseInt(template_id));
      service.deleteTemplate(map);
      BasicDynaBean tempbean = noteTypeTemplateRepository.findByKey("template_id",
          Integer.parseInt(template_id));
      Assert.assertEquals(true, tempbean == null);
    }

  }

  /**
   * Gets the insert params.
   *
   * @return the insert params
   */
  private Map<String, Object> getInsertParams() {
    Map<String, Object> params = new HashMap<>();
    params.put("note_type_name", "Doctor Note");
    params.put("assoc_hosp_role_id", 1);
    params.put("editable_by", "O");
    params.put("status", "A");
    params.put("billing_option", "N");
    params.put("transcribing_role_id", 2);
    Map<String, Object> tempparams = new HashMap<>();
    tempparams.put("template_name", "Template 1");
    tempparams.put("template_content", "Template 1 content");
    params.put("template", tempparams);

    return params;
  }

  /**
   * Gets the update params.
   *
   * @param noteType the note type
   * @param noteTemplate the note template
   * @return the update params
   */
  private Map<String, Object> getUpdateParams(Map<String, Object> noteType,
      Map<String, Object> noteTemplate) {
    Map<String, Object> params = new HashMap<>();
    params.put("note_type_id", noteType.get("note_type_id"));
    params.put("note_type_name", "Doctor Note");
    params.put("assoc_hosp_role_id", 1);
    params.put("editable_by", "O");
    params.put("status", "A");
    params.put("billing_option", "N");
    params.put("transcribing_role_id", 2);
    Map<String, Object> tempparams = new HashMap<>();
    tempparams.put("template_id", noteTemplate.get("template_id"));
    tempparams.put("note_type_id", noteType.get("note_type_id"));
    tempparams.put("template_name", "Template update");
    tempparams.put("template_content", "Template 1 content update");
    params.put("template", tempparams);

    return params;
  }

  /**
   * Gets the session service attributes.
   *
   * @return the session service attributes
   */
  public Map<String, Object> getSessionServiceAttributes() {
    Map<String, Object> sessionAttributes = new HashMap<String, Object>();
    sessionAttributes.put("userId", "InstaAdmin");
    return sessionAttributes;
  }
}
