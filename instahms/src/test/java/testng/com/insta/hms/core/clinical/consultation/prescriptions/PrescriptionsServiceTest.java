package testng.com.insta.hms.core.clinical.consultation.prescriptions;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.DynaBeanBuilder;
import com.insta.hms.common.preferences.genericpreferences.GenericPreferencesService;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.core.clinical.consultation.prescriptions.InvestigationItemService;
import com.insta.hms.core.clinical.consultation.prescriptions.MedicineItemService;
import com.insta.hms.core.clinical.consultation.prescriptions.PrescriptionsRepository;
import com.insta.hms.core.clinical.consultation.prescriptions.PrescriptionsService;
import com.insta.hms.core.clinical.forms.FormParameter;
import com.insta.hms.core.clinical.forms.FormTemplateDataService;
import com.insta.hms.core.clinical.forms.SectionDetailsService;
import com.insta.hms.mdm.centers.CenterService;
import com.insta.hms.mdm.doctors.DoctorService;
import com.insta.hms.mdm.healthauthoritypreferences.HealthAuthorityPreferencesService;
import com.insta.hms.mdm.operations.OperationsService;
import com.insta.hms.mdm.services.ServicesService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTransactionalTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import testng.utils.TestingUtils;

@Test
@ContextConfiguration(locations = { "classpath:spring/test-spring-config.xml" })
public class PrescriptionsServiceTest extends AbstractTransactionalTestNGSpringContextTests {

  private Logger logger = LoggerFactory.getLogger(PrescriptionsServiceTest.class);

  @Mock
  private SessionService sessionService;
  @Mock
  private GenericPreferencesService genPrefService;
  @Mock
  private OperationsService operService;
  @Mock
  private ServicesService srvService;
  @Mock
  private DoctorService docService;
  @Mock
  private HealthAuthorityPreferencesService healthAuthPrefService;
  @Mock
  private SectionDetailsService stnDetailService;
  @Mock
  private FormTemplateDataService formTemplateDataService;
  @Mock
  private PrescriptionsRepository presRepositoryMock;
  @Mock
  private InvestigationItemService investigationItemService;
  @Mock
  private MedicineItemService medicineItemService;
  @Mock
  private CenterService centerService;

  @InjectMocks
  private PrescriptionsService testingService;

  @Autowired
  private PrescriptionsService presService;
  @Autowired
  private PrescriptionsRepository presRepo;

  private TestingUtils testUtils = new TestingUtils();

  @BeforeMethod
  public void initMocks() {
    MockitoAnnotations.initMocks(this);
    logger.info("Before every PrescriptionTest");
  }

  @SuppressWarnings("unchecked")
  @Test
  public void getPrescriptions_NewConsultation() {
    DatabaseHelper.delete("DELETE FROM patient_section_details");
    FormParameter prameters = new FormParameter("", "", "", "", 1, "section_item_id");
    Mockito.when(formTemplateDataService.getdata(0, -7)).thenReturn(null);
    Map<String, Object> records = testingService.getSectionDetailsFromLastSavedForm(prameters);
    Assert.assertEquals(((List<Map<String, Object>>) records.get("records")).isEmpty(), true);
  }

  // @SuppressWarnings("unchecked")
  // @Test
  // public void getPrescriptions_WithNoRecords() {
  // DatabaseHelper.delete("DELETE FROM patient_prescription");
  // DynaBeanBuilder builder = new DynaBeanBuilder();
  // builder.add("section_detail_id", Integer.class);
  // builder.add("finalized");
  // BasicDynaBean record = builder.build();
  // record.set("section_detail_id", 1);
  // record.set("finalized", "N");
  // Mockito.when(
  // stnDetailService.getRecord("MR001", "OP001", 1, 0, "CONS",
  // "Form_CONS", -7)).thenReturn(record);
  //
  // BasicDynaBean genprefs = testUtils.getGenericPreferences();
  // genprefs.set("prescription_uses_stores", "Y");
  // Mockito.when(genPrefService.getAllPreferences()).thenReturn(genprefs);
  // Mockito.when(presRepositoryMock.getPrescriptions(1, true)).thenReturn(
  // presRepo.getPrescriptions(1, true));
  // SectionParameter prameters = new SectionParameter("Form_CONS", "CONS",
  // "MR001", "OP001", 1, 0);
  //
  // Map<String, Object> records = testingService.getData(prameters);
  // Mockito.verify(stnDetailService).getRecord("MR001", "OP001", 1, 0,
  // "CONS", "Form_CONS", -7);
  // Assert.assertEquals(
  // ((List<Map<String, Object>>) records.get("records")).isEmpty(),
  // true);
  // }

  @Test
  public void getALLPrescriptionItemsTest_NoRecords() {
    BasicDynaBean genprefs = testUtils.getGenericPreferences();
    genprefs.set("prescription_uses_stores", "Y");
    Mockito.when(genPrefService.getAllPreferences()).thenReturn(genprefs);
    Mockito.when(centerService.findByKey(0)).thenReturn(getHealthAuthorityBean());
    Map<String, Object> filterMap = new HashMap<String, Object>();
    filterMap.put("health_authority", "Default");
    Mockito.when(healthAuthPrefService.findByPk(filterMap))
        .thenReturn(getHealthAuthorityPreferences());
    Mockito
        .when(presRepositoryMock.getAllPrescriptionItems(true, true, true, true, true, true, true,
            true, true, "o", "M", "ORG", 0, "DEPT", 1, "Y", 0, "Tpa001", "", "Default"))
        .thenReturn(null);
    List<Map<String, Object>> records = testingService.getALLPrescriptionItems(true, true, true,
        true, true, true, true, "o", "M", "ORG", 0, "DEPT", 1, "Y", 0, "Tpa001", "", false);
    Assert.assertEquals(records.isEmpty(), true);
  }

  @Test
  public void getPrescriptionItemsTest_Operations() {
    Mockito.when(operService.getOperationsForPrescription("GENERAL", "ORG001", "o", 1, "test", 20))
        .thenReturn(new ArrayList<Map<String, Object>>());
    testingService.getPrescriptionItems("MR0001", "GENERAL", "ORG001", "o", "M", 1, "Operation",
        "Tpa001", 0, "DEPT", 1, "Y", true, "test", false);
    Mockito.verify(operService).getOperationsForPrescription("GENERAL", "ORG001", "o", 1, "test",
        PrescriptionsService.ITEMS_LIMIT);
  }

  @Test
  public void getPrescriptionItemsTest_Services() {
    Mockito.when(srvService.getServicesForPrescription("GENERAL", "ORG001", "o", 1, "test", 20))
        .thenReturn(new ArrayList<Map<String, Object>>());
    testingService.getPrescriptionItems("MR0001", "GENERAL", "ORG001", "o", "M", 1, "Service",
        "Tpa001", 0, "DEPT", 1, "Y", true, "test", false);
    Mockito.verify(srvService).getServicesForPrescription("GENERAL", "ORG001", "o", 1, "test",
        PrescriptionsService.ITEMS_LIMIT);
  }

  @Test
  public void getPrescriptionItemsTest_Medicines() {

    Mockito.when(centerService.findByKey(0)).thenReturn(getHealthAuthorityBean());

    BasicDynaBean genprefs = testUtils.getGenericPreferences();
    genprefs.set("prescription_uses_stores", "Y");
    Mockito.when(genPrefService.getAllPreferences()).thenReturn(genprefs);

    Map<String, Object> filterMap = new HashMap<String, Object>();
    filterMap.put("health_authority", "Default");
    Mockito.when(healthAuthPrefService.findByPk(filterMap))
        .thenReturn(getHealthAuthorityPreferences());

    Mockito.doReturn(new ArrayList<Map<String, Object>>()).when(medicineItemService)
        .getMedicinesForPrescription(false, "ORG001", "o", 1, true, "Default", 0, "test",
            PrescriptionsService.ITEMS_LIMIT);
    testingService.getPrescriptionItems("MR0001", "GENERAL", "ORG001", "o", "M", 1, "Medicine",
        "Tpa001", 0, "DEPT", 1, "Y", true, "test", false);
    Mockito.verify(medicineItemService).getMedicinesForPrescription(false, "ORG001", "o", 1, true,
        "Default", 0, "test", PrescriptionsService.ITEMS_LIMIT);
  }

  @Test
  public void getPrescriptionItemsTest_Doctors() {
    Mockito.when(docService.getDoctorsForPrescription("GENERAL", "ORG001", "o", 1, "test",
        PrescriptionsService.ITEMS_LIMIT)).thenReturn(new ArrayList<Map<String, Object>>());
    testingService.getPrescriptionItems("MR0001", "GENERAL", "ORG001", "o", "M", 1, "DOC", "Tpa001",
        0, "DEPT", 1, "Y", true, "test", false);
    Mockito.verify(docService).getDoctorsForPrescription("GENERAL", "ORG001", "o", 1, "test",
        PrescriptionsService.ITEMS_LIMIT);
  }

  @Test
  public void getPrescriptionItemsTest_Investigations() {

    Mockito.when(sessionService.getSessionAttributes()).thenReturn(getSessionServiceAttributes());
    Mockito.doReturn(new ArrayList<Map<String, Object>>()).when(investigationItemService)
        .getTestsForPrescription("MR0001", "GENERAL", "ORG001", "o", 1, 0, "Tpa001", "test",
            "DEP001", 1, "Y", "M", true, PrescriptionsService.ITEMS_LIMIT);
    testingService.getPrescriptionItems("MR0001", "GENERAL", "ORG001", "o", "M", 1, "Inv.",
        "Tpa001", 0, "DEP001", 1, "Y", true, "test", false);
    Mockito.verify(investigationItemService).getTestsForPrescription("MR0001", "GENERAL", "ORG001",
        "o", 1, 0, "Tpa001", "test", "DEP001", 1, "Y", "M", true, PrescriptionsService.ITEMS_LIMIT);
  }

  @Test
  public void getPrescriptionItemsTest_RandomPresType() {
    List<Map<String, Object>> records = testingService.getPrescriptionItems("MR0001", "GENERAL",
        "ORG001", "o", "M", 1, "RANDOM", "Tpa001", 0, "DEPT", 1, "Y", true, "test", false);
    Assert.assertEquals(records, null);
  }

  public Map<String, Object> getSessionServiceAttributes() {
    Map<String, Object> sessionAttributes = new HashMap<String, Object>();
    sessionAttributes.put("loginCenterHealthAuthority", "Default");
    sessionAttributes.put("centerId", 1);
    return sessionAttributes;
  }

  public BasicDynaBean getHealthAuthorityBean() {
    DynaBeanBuilder builder = new DynaBeanBuilder();
    builder.add("health_authority");
    BasicDynaBean healthAuthorityBean = builder.build();
    healthAuthorityBean.set("health_authority", "Default");
    return healthAuthorityBean;
  }

  public BasicDynaBean getHealthAuthorityPreferences() {
    DynaBeanBuilder builder = new DynaBeanBuilder();
    builder.add("prescriptions_by_generics");
    BasicDynaBean healthAuthorityPrefs = builder.build();
    healthAuthorityPrefs.set("prescriptions_by_generics", "N");
    return healthAuthorityPrefs;
  }

}
