package testng.com.insta.hms.integration;

import com.insta.hms.mdm.areas.AreaService;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.xpath;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import com.bob.hms.common.RequestContext;
import com.insta.hms.common.AppInit;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.modulesactivated.ModulesActivatedRepository;
import com.insta.hms.common.modulesactivated.ModulesActivatedService;
import com.insta.hms.common.preferences.genericpreferences.GenericPreferencesService;
import com.insta.hms.common.security.SecurityService;
import com.insta.hms.common.validation.EmailIdRule;
import com.insta.hms.core.diagnostics.incomingsampleregistration.IncomingSampleRegistrationRepository;
import com.insta.hms.core.diagnostics.incomingsampleregistration.IncomingSampleRegistrationService;
import com.insta.hms.core.medicalrecords.MRDCaseFileIndentRepository;
import com.insta.hms.core.medicalrecords.MRDCaseFileIndentService;
import com.insta.hms.core.patient.PatientDetailsRepository;
import com.insta.hms.core.patient.PatientDetailsService;
import com.insta.hms.core.patient.registration.PatientRegistrationRepository;
import com.insta.hms.core.patient.registration.PrepopulateVisitInfoRepository;
import com.insta.hms.core.patient.registration.PrepopulateVisitInfoService;
import com.insta.hms.core.patient.registration.RegistrationCustomFieldsService;
import com.insta.hms.core.patient.registration.RegistrationPreferencesService;
import com.insta.hms.core.patient.registration.RegistrationService;
import com.insta.hms.core.patient.registration.RegistrationValidator;
import com.insta.hms.integration.InstaIntegrationRepository;
import com.insta.hms.integration.InstaIntegrationService;
import com.insta.hms.integration.SyncExternalPatientController;
import com.insta.hms.integration.configuration.InterfaceConfigRepository;
import com.insta.hms.integration.configuration.InterfaceConfigService;
import com.insta.hms.integration.configuration.InterfaceEventMappingRepository;
import com.insta.hms.integration.configuration.InterfaceEventMappingService;
import com.insta.hms.integration.hl7.message.v23.ADTService;
import com.insta.hms.jobs.JobService;
import com.insta.hms.mdm.cities.CityService;
import com.insta.hms.mdm.codesets.CodeSetsService;
import com.insta.hms.mdm.countries.CountryService;
import com.insta.hms.mdm.govtidentifiers.GovtIdentifierRepository;
import com.insta.hms.mdm.patientcategories.PatientCategoryService;
import com.insta.hms.mdm.salutations.SalutationService;
import com.insta.hms.security.usermanager.UserService;
import testng.utils.TestRepoInit;
import testng.utils.TestingUtils;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.xpath.XPathAPI;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTransactionalTestNGSpringContextTests;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathExpressionException;

@Test
@ContextConfiguration(locations = { "classpath:spring/test-spring-config.xml" })
@RunWith(MockitoJUnitRunner.class)

public class SyncExternalPatientControllerTest extends AbstractTransactionalTestNGSpringContextTests {
	private Logger logger = LoggerFactory.getLogger(SyncExternalPatientControllerTest.class);
	static DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

	@Mock
	private ADTService adtService;
	
	@Mock
	UserService userService;

	@Mock
	SecurityService securityService;
	
	@Mock
  GovtIdentifierRepository govtIdentifierRepository;
	
	@Mock
	CountryService countryService;
	
	@Spy
	PatientDetailsRepository patientDetailsRepository;
	
	@Autowired
	ApplicationContext appCtx;

	@InjectMocks
	@Spy
	PatientDetailsService patientDetailsService;

	@Mock
	RegistrationPreferencesService registrationPreferencesService;

	@Spy
	RegistrationCustomFieldsService regCustomFieldService;

	@Mock
	InstaIntegrationRepository instaIntegrationRepository;

	@Spy
	PatientCategoryService patientCategoryService;

	@Spy
	SalutationService salutationService;

	@Spy
	CityService cityService;

	@Spy
	AreaService areaService;

	@Spy
	PrepopulateVisitInfoRepository prepopulateVisitInfoRepository;

	@Spy
	@InjectMocks
	PrepopulateVisitInfoService prepopulateVisitInfoService;

	@Mock
	GenericPreferencesService genPrefService;
	
	@Spy
	EmailIdRule emailIdValidator;

	@Spy
	@InjectMocks
	private RegistrationValidator registrationValidator;

	@Spy
	PatientCategoryService dsjfh;

	@Spy
	PatientRegistrationRepository patientRegistrationRepository;

	@Spy
	MRDCaseFileIndentRepository mrdCAseFielIndentRepo;

	@Spy
	@InjectMocks
	MRDCaseFileIndentService mrdCaseFileIndentService;

	@Spy
	IncomingSampleRegistrationRepository incomingSampleRegRepo;

	@Spy
	@InjectMocks
	IncomingSampleRegistrationService incomingSampleRegService;

	@InjectMocks
	@Spy
	RegistrationService registrationService;
	
	@InjectMocks
	@Spy
	InterfaceConfigService interfaceConfigService;

	@InjectMocks
	@Spy
	InterfaceConfigRepository interfaceRepo;

	@InjectMocks
	@Spy
	JobService jobservice;
	
	@InjectMocks
	@Spy
	CodeSetsService codeSetsService;
	
	@Spy
	@InjectMocks
	InstaIntegrationService instaIntegrationService;

	@InjectMocks
	SyncExternalPatientController controller;
	MockMvc mockMvc;
	
	@InjectMocks
    @Spy
    ModulesActivatedService modulesActivatedService;
	
	@InjectMocks
    @Spy
	ModulesActivatedRepository modulesActivatedRepository;
	
	@InjectMocks
    @Spy
	InterfaceEventMappingService interfaceEventService;
	
	@InjectMocks
    @Spy
	InterfaceEventMappingRepository interfaceEventMappingRepository;

	private final String idealContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
			+ "  <patient_record xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
			+ "     xsi:noNamespaceSchemaLocation='insta-patient-sync.xsd'> " 
			+ "    <schema>test_schema</schema> "
			+ "    <patient_details> " 
			+ "        <old_mr_no>emp123123123</old_mr_no> "
			+ "        <user_name>InstaAdmin</user_name> " 
			+ "  	   <first_name>First Name</first_name> "
			+ "        <middle_name>Middle Name</middle_name> " 
			+ "        <last_name>Last Name</last_name> "
		    + "        <gender> " 
			+ "        	<code>M</code> " 
		    + "         <description>Male</description> "
			+ "        </gender> " 
		    + "        <date_of_birth>1990-09-01</date_of_birth> " 
			+ "        <nationality_id> "
			+ "        	<code>1</code> " 
			+ "         <description>IN</description> "
			+ "        </nationality_id> "
			+ "        <mobile_number>+919999999999</mobile_number>"
			+ "        <government_identifier>7000-1000-3000000-0</government_identifier> "
			+ "        <email>test@test.com</email> " 
			+ "        <custom_list2_value> " 
			+ "        	<code>1</code> "
			+ "         <description>Active</description> " 
			+ "        </custom_list2_value> "
			+ "        <custom_list3_value> " 
			+ "        	<code>Single</code> "
			+ "         <description>Single</description> " + "        </custom_list3_value> "
			+ "        <custom_field15>2018-04-01</custom_field15> "
			+ "        <passport_number>abcd1234</passport_number> "
			+ "		   <city>"
			+ "        	<code>1</code> "
			+ "         <description>Bangalore</description> " 
			+ "		   </city>"
			+ "    </patient_details> "
			+ "    <prepopulate_visit_info> " 
			+ "        <visit_custom_list1> "
			+ "        	<code>Etihad Guest LLC</code> " 
			+ "         <description>Etihad Guest LLC</description> "
			+ "        </visit_custom_list1> " 
			+ "        <visit_custom_list2> " 
			+ "            <code>Abu Dhani</code> "
			+ "            <description>Abu Dhani</description> " 
			+ "        </visit_custom_list2> "
			+ "        <visit_custom_field1>Cost Center example</visit_custom_field1> "
			+ "        <visit_custom_field2>Department example</visit_custom_field2> "
			+ "        <visit_custom_field3>Job Title example</visit_custom_field3> " 
			+ "    </prepopulate_visit_info> "
			+ "	</patient_record> ";

	@BeforeMethod
	void setUpr() throws SQLException, TransformerException, SAXException, IOException, ParserConfigurationException {
		patientCategoryService = appCtx.getBean(PatientCategoryService.class);
		dsjfh = appCtx.getBean(PatientCategoryService.class);
		salutationService = appCtx.getBean(SalutationService.class);
		cityService = appCtx.getBean(CityService.class);
		areaService = appCtx.getBean(AreaService.class);
		regCustomFieldService = appCtx.getBean(RegistrationCustomFieldsService.class);

		TestRepoInit testRepo = new TestRepoInit();
    testRepo.insert("country_master");
    testRepo.initializeRepo();
    
		MockitoAnnotations.initMocks(this);
		mockMvc = standaloneSetup(controller).build();
		RequestContext.setConnectionDetails(null);
		AppInit.initializeConverters();

	}

	@Test
	public void emptyContentTest() throws Exception {
		
//		Object obj = ConvertUtils.convert("01-09-1994", java.sql.Date.class);
//		java.sql.Date obj = java.sql.Date.valueOf("1994-09-01");
//		System.out.println("conversion succ " + obj);
		init();
		logger.info("empty content test");
		Mockito.when(securityService.getActivatedModules()).thenReturn(Arrays.asList(new String[] {}));
		mockMvc.perform(post("/syncexternalpatient/preregister.xml").contentType(MediaType.APPLICATION_XML))
				.andExpect(status().is(400));

	}

	@Test
	public void moduleDisabledTest() throws Exception {
		init();
		logger.info("module activation status test");
		String content = idealContent;
		Mockito.when(securityService.getActivatedModules()).thenReturn(Arrays.asList(new String[] {}));
		mockMvc.perform(
				post("/syncexternalpatient/preregister.xml").contentType(MediaType.APPLICATION_XML).content(content))
				.andExpect(status().is(404));

	}

	@Test
	public void moduleEnabledTest() throws Exception {
		init();
		String content = idealContent;

		Mockito.when(securityService.getActivatedModules())
				.thenReturn(Arrays.asList(new String[] { "mod_sync_external_patient" }));
		final BasicDynaBean dummyPatientDetailsBean = TestingUtils.getDummyBean("mr_no");
		mockMvc.perform(
				post("/syncexternalpatient/preregister.xml").contentType(MediaType.APPLICATION_XML).content(content))
				.andExpect(status().isOk());
	}

	@Test
	public void schemaInvalidTest() throws Exception {
		Map<String, String> changeSpec = new HashMap<String, String>();
		changeSpec.put("//patient_record/schema", "dummySchema_123");
		String content = changeXML(idealContent, changeSpec);
		Mockito.when(securityService.getActivatedModules()).thenReturn(Arrays.asList("mod_sync_external_patient"));
		mockMvc.perform(
				post("/syncexternalpatient/preregister.xml").contentType(MediaType.APPLICATION_XML).content(content))
				.andExpect(status().is(400)).andExpect(xpath("//patient_record_response/errors/schema").exists());
	}

	@Test
	public void schemaAbsentTest() throws Exception {
		Map<String, String> changeSpec = new HashMap<String, String>();
		changeSpec.put("//patient_record/schema", null);
		String content = changeXML(idealContent, changeSpec);
		Mockito.when(securityService.getActivatedModules())
				.thenReturn(Arrays.asList(new String[] { "mod_sync_external_patient" }));
		mockMvc.perform(
				post("/syncexternalpatient/preregister.xml").contentType(MediaType.APPLICATION_XML).content(content))
				.andExpect(status().is(400)).andExpect(xpath("//patient_record_response/errors/schema").exists());
	}

	private void init() {
		Mockito.doAnswer(new Answer<List<BasicDynaBean>>() {

			@Override
			public List<BasicDynaBean> answer(InvocationOnMock invocation) throws Throwable {
				BasicDynaBean ipWhitelistBean = TestingUtils.getDummyBean("ip_start", "ip_end");
				RequestContext.setConnectionDetails(null);
				ipWhitelistBean.set("ip_start", "127.0.0.1");
				return Arrays.asList(ipWhitelistBean);
			}
		}).when(instaIntegrationRepository).getWhitelistedIps("sync_ext");

		BasicDynaBean dummyRegPrefs = TestingUtils.getDummyBean(new HashMap<String, Class>() {
		  {
		    put("allow_multiple_active_visits", java.lang.String.class);
		    put("last_name_required",java.lang.String.class);
		  }
	    });
		dummyRegPrefs.set("allow_multiple_active_visits", "N");
		dummyRegPrefs.set("last_name_required", "N");
		Mockito.when(registrationPreferencesService.getRegistrationPreferences()).thenReturn(dummyRegPrefs);
		BasicDynaBean dummyPreferences = TestingUtils.getDummyBean(new HashMap<String, Class>() {
			{
				put("max_centers_inc_default", java.lang.Integer.class);
				put("mobile_number_validation", java.lang.String.class);
			}
		});
		dummyPreferences.set("max_centers_inc_default", 1);
		dummyPreferences.set("mobile_number_validation", "N");

		Mockito.when(genPrefService.getPreferences()).thenReturn(dummyPreferences);

		Mockito.when(genPrefService.getAllPreferences()).thenReturn(dummyPreferences);
		
    Mockito.when(countryService.findByUniqueName( Mockito.anyString(),
        Mockito.anyString())).thenReturn(dummyCountryId());

    Mockito.when(govtIdentifierRepository.findByKey(Mockito.anyMap()))
        .thenReturn(dummyIdentifierId());
	}

  private BasicDynaBean dummyIdentifierId() {
    BasicDynaBean bean = TestingUtils.getDummyBean(new HashMap<String, Class>() {
      {
        put("identifier_id", java.lang.Integer.class);
      }
    });
    bean.set("identifier_id", (Integer) 2);
    return bean;
  }

  private BasicDynaBean dummyCountryId() {
    BasicDynaBean bean = TestingUtils.getDummyBean(new HashMap<String, Class>() {
      {
        put("country_id", java.lang.String.class);
      }
    });
    bean.set("country_id", "CM0118");
    return bean;
  }

	public void userInsertAndUpdate() throws Exception {
		logger.info("user insertion and updation test");
		init();

		String mrNo = DatabaseHelper.getString("SELECT mr_no FROM patient_details WHERE lower(oldmrno)=?",
				getValueByXpath(idealContent, "//patient_record/patient_details/old_mr_no").toLowerCase());
		if (mrNo != null && !mrNo.isEmpty()) {
			DatabaseHelper.delete("DELETE FROM prepopulate_visit_info where lower(mr_no) = ?", mrNo.toLowerCase());
			DatabaseHelper.delete("DELETE FROM patient_details where lower(mr_no) = ?", mrNo.toLowerCase());
		}

		String content = idealContent;

		Mockito.when(securityService.getActivatedModules())
				.thenReturn(Arrays.asList(new String[] { "mod_sync_external_patient" }));

		MvcResult result = mockMvc
				.perform(post("/syncexternalpatient/preregister.xml").contentType(MediaType.APPLICATION_XML)
						.content(content))
				.andExpect(status().isOk())
				.andExpect(xpath("//patient_record_response/status/text()").string("success"))
				.andExpect(xpath("//patient_record_response/sync_operation/text()").string("create"))
				.andExpect(xpath("//patient_record_response/mr_no/text()").exists()).andReturn();

		String mrNoFromDb = DatabaseHelper.getString("SELECT mr_no FROM patient_details WHERE lower(oldmrno)=?",
				getValueByXpath(idealContent, "//patient_record/patient_details/old_mr_no").toLowerCase());

		Assert.assertNotNull(mrNoFromDb);

		JSONObject customFieldsFromDb = new JSONObject(
				DatabaseHelper.getString("select visit_values from prepopulate_visit_info"));
		Assert.assertEquals(
				getValueByXpath(content, "//patient_record/prepopulate_visit_info/visit_custom_list1/description"),
				customFieldsFromDb.get("visit_custom_list1"));
		Assert.assertEquals(
				getValueByXpath(content, "//patient_record/prepopulate_visit_info/visit_custom_list2/description"),
				customFieldsFromDb.get("visit_custom_list2"));
		Assert.assertEquals(getValueByXpath(content, "//patient_record/prepopulate_visit_info/visit_custom_field1"),
				customFieldsFromDb.get("visit_custom_field1"));
		Assert.assertEquals(getValueByXpath(content, "//patient_record/prepopulate_visit_info/visit_custom_field2"),
				customFieldsFromDb.get("visit_custom_field2"));
		Assert.assertEquals(getValueByXpath(content, "//patient_record/prepopulate_visit_info/visit_custom_field3"),
				customFieldsFromDb.get("visit_custom_field3"));

		logger.info("Response after insert " + result.getResponse().getContentAsString());

		content = changeXML(idealContent, new HashMap<String, String>() {
			{
				put("//patient_record/patient_details/first_name", "TempName");
			}
		});
		result = mockMvc
				.perform(post("/syncexternalpatient/preregister.xml").contentType(MediaType.APPLICATION_XML)
						.content(content))
				.andExpect(status().isOk())
				.andExpect(xpath("//patient_record_response/status/text()").string("success"))
				.andExpect(xpath("//patient_record_response/sync_operation/text()").string("update"))
				.andExpect(xpath("//patient_record_response/mr_no/text()").exists()).andReturn();

		logger.info("Response after update " + result.getResponse().getContentAsString());

		String nameFromDb = DatabaseHelper.getString(
				"select patient_name from patient_details where lower(oldmrno) = ?",
				getValueByXpath(idealContent, "//patient_record/patient_details/old_mr_no").toLowerCase());
		Assert.assertEquals(nameFromDb, "TempName");

	}

	@Test
	public void partialDataTest() throws XPathExpressionException, Exception {
		init();
		logger.info("partial data test");
		Mockito.when(securityService.getActivatedModules())
				.thenReturn(Arrays.asList(new String[] { "mod_sync_external_patient" }));

		Map<String, String> changeSpec = new HashMap<String, String>();
		changeSpec.put("//patient_record/prepopulate_visit_info/visit_custom_list1", null);

		String content = changeXML(idealContent, changeSpec);
		mockMvc.perform(
				post("/syncexternalpatient/preregister.xml").contentType(MediaType.APPLICATION_XML).content(content))
				.andExpect(status().is(200)).andExpect(xpath("//patient_record_response/status/text()").string("success"));

	}
	
	@Test
	public void emailValidation() throws XPathExpressionException, Exception {
		init();
		logger.info("email validation test");

		Mockito.when(securityService.getActivatedModules())
				.thenReturn(Arrays.asList(new String[] { "mod_sync_external_patient" }));

		String content = changeXML(idealContent, new HashMap<String, String>() {
			{
				put("//patient_record/patient_details/email", null);
			}
		});

		mockMvc.perform(
				post("/syncexternalpatient/preregister.xml").contentType(MediaType.APPLICATION_XML).content(content))
				.andExpect(status().is(200))
				.andExpect(xpath("//patient_record_response/status/text()").string("success"));
		
		content = changeXML(idealContent, new HashMap<String, String>() {
			{
				put("//patient_record/patient_details/email", "thisisinvalidemail");
			}
		});

		mockMvc.perform(
				post("/syncexternalpatient/preregister.xml").contentType(MediaType.APPLICATION_XML).content(content))
				.andExpect(status().is(400)).andExpect(xpath("//patient_record_response/status/text()").string("fail"))
				.andExpect(xpath("//patient_record_response/errors/email").exists());
	}

	// @Test
	// public void largeValueTest() throws XPathExpressionException, Exception {
	// init();
	// logger.info("large value test");
	// Map<String, String> changeSpec = new HashMap<String, String>();
	// changeSpec.put("//patient_record/patient_details/first_name",
	// "qwertqwertqwertqwertwqertweqtrteetwrqterqwretwrterqwterywterywqtreyqwtreytryryterwqtyreytwreytwqerewrewqrewrqerqewrqwerweqrwerqwerqwerqwerqwerqwereqwrewqrweqr");
	// String content = changeXML(idealContent, changeSpec);
	//
	// //TODO This must not be mocked
	//// Mockito.doReturn(false).when(registrationValidator).validatePatientDemographyNewVisit(Matchers.any(BasicDynaBean.class),
	// Matchers.any(ValidationErrorMap.class));
	//
	//
	// Mockito.when(securityService.getActivatedModules())
	// .thenReturn(Arrays.asList(new String[] { "mod_sync_external_patient" }));
	//
	// mockMvc.perform(
	// post("/syncexternalpatient/preregister.xml").contentType(MediaType.APPLICATION_XML).content(content))
	// .andExpect(status().is(400)).andExpect(xpath("//patient_record_response/status/text()").string("fail"));
	//
	// }

	private static String changeXML(String xmlDocument, Map<String, String> changeSpec)
			throws ParserConfigurationException, SAXException, IOException, TransformerException {
		if (xmlDocument != null && !xmlDocument.isEmpty() && changeSpec != null && !changeSpec.isEmpty()) {
			DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
			Document document = builder.parse(new InputSource(new StringReader(xmlDocument)));

			for (String xpath : changeSpec.keySet()) {
				Node node = XPathAPI.selectSingleNode(document, xpath);
				node.setTextContent(changeSpec.get(xpath));
			}

			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			StringWriter writer = new StringWriter();
			transformer.transform(new DOMSource(document), new StreamResult(writer));
			return writer.toString();
		}
		return null;
	}

	@AfterMethod
	void cleanUp() {
		logger.info("after method");
		RequestContext.setConnectionDetails(null);
	}

	private static String getValueByXpath(String xmlDocument, String xpath)
			throws TransformerException, SAXException, IOException, ParserConfigurationException {
		if (xmlDocument != null && !xmlDocument.isEmpty() && xpath != null && !xpath.isEmpty()) {
			DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
			Document document = builder.parse(new InputSource(new StringReader(xmlDocument)));
			Node node = XPathAPI.selectSingleNode(document, xpath);
			if (node != null) {
				return node.getTextContent();
			}
		}
		return null;
	}
}
