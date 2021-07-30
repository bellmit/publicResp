package testng.com.insta.hms.core.clinical.consultationnotes;

import com.insta.hms.common.DynaBeanBuilder;
import com.insta.hms.core.clinical.consultationnotes.ConsultationFieldValuesRepository;
import com.insta.hms.core.clinical.consultationnotes.ConsultationFieldValuesService;
import com.insta.hms.core.clinical.forms.FormParameter;
import com.insta.hms.core.clinical.outpatient.DoctorConsultationRepository;
import com.insta.hms.core.clinical.outpatient.DoctorConsultationService;
import testng.utils.TestRepoInit;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.Assert;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class ConsultationFieldValuesServiceTest.
 */
@Test
@ContextConfiguration(locations = { "classpath:spring/test-spring-config.xml" })
public class ConsultationFieldValuesServiceTest
    extends AbstractTestNGSpringContextTests {
  
  /** The logger. */
  private Logger logger = LoggerFactory.getLogger(ConsultationFieldValuesServiceTest.class);

  /** The doc consultation repo. */
  @Spy
  private DoctorConsultationRepository docConsultationRepo;
  
  /** The doctor consultation service. */
  @Spy
  @InjectMocks
  private DoctorConsultationService doctorConsultationService;

  
  /** The consultation field values repository. */
  @Spy
  private ConsultationFieldValuesRepository consultationFieldValuesRepository;

  /** The consultation field values service. */
  @Spy
  @InjectMocks
  private ConsultationFieldValuesService consultationFieldValuesService;


  /** The db data map. */
  private Map<String, Object> dbDataMap = null;

  /**
   * Inits the mocks.
   */
  @BeforeMethod
  public void initMocks() {
    logger.info("Before every test of ConsultationFieldValuesService");
    MockitoAnnotations.initMocks(this);
    TestRepoInit testRepo = new TestRepoInit();
    testRepo.insert("doctors");
    testRepo.insert("doctor_consultation");
    testRepo.insert("patient_section_details");
    testRepo.insert("patient_section_forms");
    testRepo.insert("patient_consultation_field_values");
    dbDataMap = testRepo.initializeRepo();
  }
  
  /**
   * Gets the section data.
   *
   * @return the section data
   */
  private BasicDynaBean getSectionData() {
    
    Map<String, Object> sectionDetails = getSectionDetails();
   
    DynaBeanBuilder builder = new DynaBeanBuilder();
    BasicDynaBean bean = null;
    if (sectionDetails != null) {
      builder.add("section_item_id", Integer.class);
      builder.add("item_type");
      builder.add("mr_no");
      builder.add("patient_id");
      builder.add("section_detail_id", Integer.class);
      builder.add("section_status");
      builder.add("generic_form_id", Integer.class);
      builder.add("finalized");
      builder.add("mod_time");
      builder.add("user_name");
      Integer sectionDetailId = Integer.valueOf((String) sectionDetails.get("section_detail_id"));
      Integer sectionItemId = Integer.valueOf((String) sectionDetails.get("section_item_id"));
      bean = builder.build();
      bean.set("section_item_id", sectionItemId);
      bean.set("item_type", sectionDetails.get("item_type"));
      bean.set("mr_no", sectionDetails.get("mr_no"));
      bean.set("patient_id", sectionDetails.get("patient_id"));
      bean.set("section_detail_id", sectionDetailId);
      bean.set("section_status", sectionDetails.get("section_status"));
      bean.set("generic_form_id", 0);
      bean.set("finalized", sectionDetails.get("finalized"));
    }
    return bean;
  }

  /**
   * Gets the section details.
   *
   * @return the section details
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  private  Map<String, Object> getSectionDetails(){
    List<Map> patientSectionDetails = (List) dbDataMap.get("patient_section_details");
    Map<String, Object> sectionDetails = null;
    for (Map<String, Object> map : patientSectionDetails) {
      if (map.get("section_id").equals("-5")){
        sectionDetails = map;
        break;
      }
    }
    return sectionDetails;
  }
  
  /**
   * Gets the section parameter.
   *
   * @return the section parameter
   */
  private FormParameter getFormParameter() {
    Map<String, Object> sectionDetails = getSectionDetails();
    FormParameter parameters = null;
    if (sectionDetails != null) {
      String sectionItem = (String) sectionDetails.get("section_item_id");
      int sectionItemId = Integer.valueOf(sectionItem);
      parameters = new FormParameter("Form_CONS",
          "CONS",
          (String) sectionDetails.get("mr_no"),
          (String) sectionDetails.get("patient_id"),
          sectionItemId,
          "section_item_id");
    }
    return parameters;
  }
  

  /**
   * Request which does't have fields key word. Expecting empty response
   */
  @Test
  public void testSaveSectionNoDataInRequest() {
    logger.info("TestCase 1: Consultation Notes with empty request data :");
    Map<String, Object> requestBody = new HashMap<String, Object>();
    Map<String, Object> response = consultationFieldValuesService.saveSection(requestBody, null,
        null, null);
    logger.info("Response :" + response.get("fields"));
    Assert.assertEquals(true, response.isEmpty());
    response = consultationFieldValuesService.saveSection(dbDataMap, null, null, null);
  }

  /**
   * Request : Having null field_value for consultation notes Response : generated value_id.
   */
  @SuppressWarnings("unchecked")
  @Test
  public void testSaveSectionWithNullFieldValue() {
    logger.info("TestCase 2: Consultation Notes with null field_values :");
    Map<String, Object> requestBody = new HashMap<String, Object>();
    Map<String, Object> temp = new HashMap<>();
    temp.put("field_value", null);
    temp.put("value_id", null);
    temp.put("doc_id", 100);
    temp.put("field_id", null);
    requestBody.put("fields", Arrays.asList(temp));
    Map<String, Object> response = consultationFieldValuesService.saveSection(requestBody, getSectionData(),
        getFormParameter() ,null);
    Assert.assertNotNull(response.get("fields"));
    Map<String, Object> res = ((List<Map<String, Object>>) response.get("fields")).get(0);
    logger.info("Response " + res);
    Assert.assertNotNull(res);
    Assert.assertNotNull(res.get("value_id"));
    Assert.assertTrue("Response is not empty", !res.isEmpty());
    Assert.assertTrue("Value id should be auto generated", (Integer) res.get("value_id") > 0);

  }

  /**
   * Request : Having empty field_value for consultation notes Response : generated value_id.
   */
  @SuppressWarnings("unchecked")
  @Test
  public void testSaveSectionWithEmptyFieldValue() {
    logger.info("TestCase 3: Consultation Notes with empty field_value :");
    Map<String, Object> requestBody = new HashMap<String, Object>();
    Map<String, Object> temp = new HashMap<>();
    temp.put("field_value", "");
    temp.put("value_id", null);
    temp.put("doc_id", 100);
    temp.put("field_id", null);
    requestBody.put("fields", Arrays.asList(temp));
    Map<String, Object> response = consultationFieldValuesService.saveSection(requestBody, getSectionData(),
        getFormParameter(), null);
    Map<String, Object> res = ((List<Map<String, Object>>) response.get("fields")).get(0);
    logger.info("Response " + res);
    Assert.assertNotNull(res);
    Assert.assertNotNull(res.get("value_id"));
    Assert.assertTrue("Response is not empty", !res.isEmpty());
    int valueId = (Integer) res.get("value_id");
    Assert.assertTrue(valueId > 0);
  }

  /**
   * Test doc_id null in request and it will fetch from doctor_consultation table
   */
  @SuppressWarnings("unchecked")
  @Test
  public void testSaveSectionWithNullDocId() {
    logger.info("TestCase 4: Consultation Notes with null doc_id :");
    Map<String, Object> requestBody = new HashMap<String, Object>();
    Map<String, Object> temp = new HashMap<>();
    temp.put("field_value", "Progress notes");
    temp.put("value_id", null);
    temp.put("doc_id", null);
    temp.put("field_id", null);
    requestBody.put("fields", Arrays.asList(temp));
    Map<String, Object> response = consultationFieldValuesService.saveSection(requestBody, getSectionData(),
        getFormParameter(), null);
    Map<String, Object> res = ((List<Map<String, Object>>) response.get("fields")).get(0);
    logger.info("Response " + res);
    Assert.assertNotNull(res);
    Assert.assertNotNull(res.get("doc_id"));
    Assert.assertTrue("Response is not empty", !res.isEmpty());
    int valueId = (Integer) res.get("value_id");
    Assert.assertTrue(valueId > 0);
  }

  /**
   * Updating existing saved data.
   */
  @SuppressWarnings("unchecked")
  @Test
  public void testUpdateSectionFieldValue() {
    logger.info("TestCase 5: Updating consultation notes data :");
    Map<String, Object> requestBody = new HashMap<String, Object>();
    List<Map<String, Object>> insertedData = (List<Map<String, Object>>) dbDataMap
        .get("patient_consultation_field_values");
    Map<String, Object> insertedMap = insertedData.get(0);
    logger.info("Inserted Data :" + insertedMap);
    String fieldValue = "Yoga is good for this patient";
    insertedMap.put("field_value", fieldValue);
    requestBody.put("fields", insertedData);
    Map<String, Object> response = consultationFieldValuesService.saveSection(requestBody, null,
        null, null);
    Map<String, Object> res = ((List<Map<String, Object>>) response.get("fields")).get(0);
    logger.info("Updated Data :" + res);
    Assert.assertNotNull(res);
    Assert.assertTrue("Response is not empty", !res.isEmpty());
    Assert.assertTrue(fieldValue.equals(res.get("field_value")));
    Assert
        .assertTrue((Integer) res.get("value_id") > 0 && fieldValue.equals(res.get("field_value")));
    Assert.assertTrue(
        (Integer) res.get("value_id") == Integer.parseInt((String) insertedMap.get("value_id")));
    Assert.assertTrue(
        (Integer) res.get("doc_id") == Integer.parseInt((String) insertedMap.get("doc_id")));
  }

  /**
   * Test get consultation notes section data.
   */
  @SuppressWarnings("unchecked")
  @Test
  public void testGetSectionData() {
    logger.info("TestCase 6: Get consultation notes data :");
    Map<String, Object> response = consultationFieldValuesService.getSectionDetailsFromCurrentForm(getFormParameter());
    logger.info("GET Consultation Data :" + response);
    Assert.assertTrue(!((ArrayList<Object>) response.get("records")).isEmpty());
    Assert.assertNotNull(response.get("records"));
    Map<String, Object> res = ((List<Map<String, Object>>) response.get("records")).get(0);
    Assert.assertNotNull(res);
  
  }
  
  /**
   * Test get active consultation notes section data.
   */
  @SuppressWarnings("unchecked")
  @Test
  public void testGetActiveSectionData() {
    logger.info("TestCase 7: Get active consultation notes data :");
    Map<String, Object> response = consultationFieldValuesService.getSectionDetailsFromLastSavedForm(null);
    logger.info("GET Consultation Data :" + response);
    Assert.assertTrue(((ArrayList<Object>) response.get("records")).isEmpty());

  }
  
  /**
   * Test delete consultation notes section.
   */
  public void testDeleteSection() {
    logger.info("TestCase 8: Deleting consultation notes :");
    Boolean response = consultationFieldValuesService.deleteSection(142587, getFormParameter(), null);
    Assert.assertTrue(response);
  }
}
