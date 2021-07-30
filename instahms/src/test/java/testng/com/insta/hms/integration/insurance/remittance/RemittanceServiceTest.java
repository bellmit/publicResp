package testng.com.insta.hms.integration.insurance.remittance;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.preferences.genericpreferences.GenericPreferencesService;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.insurance.RemittanceAdvice;
import com.insta.hms.integration.insurance.InsurancePluginManager;
import com.insta.hms.integration.insurance.remittance.InsuranceRemittanceDetailsRepository;
import com.insta.hms.integration.insurance.remittance.RemittanceService;
import com.insta.hms.integration.insurance.remittance.XMLRemittanceDigester;
import com.insta.hms.mdm.centers.CenterRepository;
import com.insta.hms.mdm.centers.CenterService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.digester.Digester;
import org.apache.commons.io.input.BOMInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTransactionalTestNGSpringContextTests;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Test
@RunWith(MockitoJUnitRunner.class)
@Transactional
@ContextConfiguration(locations = { "classpath:spring/test-spring-config.xml" })
public class RemittanceServiceTest extends AbstractTransactionalTestNGSpringContextTests {

	private Logger logger = LoggerFactory.getLogger(RemittanceServiceTest.class);
	private static final String FILENAME = "/MockTestData/testRemittance.xml";

	@Mock
	SessionService sessionService;

	@Mock
	InsurancePluginManager manager;

	@Autowired
	GenericPreferencesService genericPreferencesService;
	@InjectMocks
	@Autowired
	RemittanceService remittanceService;
	@Autowired
	private InsuranceRemittanceDetailsRepository irdRepository;
	@Autowired
	private CenterService centerService;
	@Autowired
	private CenterRepository centerRepository;

	private Integer centerId;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		ReflectionTestUtils.setField(remittanceService, "sessionService", sessionService);
	}

	@BeforeMethod
	public void mockData() {

		centerId = 25;
		logger.info("Before every RegionServiceTest");
		irdRepository.deleteAllRecords();
		String INSERT_CENTER_HAAD = "INSERT INTO hospital_center_master (center_id, health_authority, center_code, center_name, status, city_id, state_id, country_id) VALUES (?,?,?,?,?,?,?,?)";

		DatabaseHelper.insert(INSERT_CENTER_HAAD,
				new Object[] { centerId, "HAAD", "Test Center", "Test Center", "A", "CT0088", "ST0017", "CM0109" });
	}

	@Test
	public void testXMLHeader() throws Exception {
		// test for multiple header
		PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
		FileInputStream fis = new FileInputStream(resolver.getResource("testRemittance.xml").getFile());
		MockMultipartFile multipartFile = new MockMultipartFile("file", fis);

		Map<String, String[]> map = new HashMap<String, String[]>();
		map.put("center_or_account_group", new String[] { "1" });
		map.put("remittance_id", new String[] { "1" });
		map.put("tpa_id", new String[] { "TPAID0094" });
		map.put("account_group", new String[] { "0" });
		Assert.assertEquals("XML parsing failed: Receiver ID not same as the center service registration no. ...",
				remittanceService.create(map, multipartFile, centerId, null));
	}

	@Test
	public void testInvalidCenterId() throws Exception {
		// test for invalid service registration number
		PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
		FileInputStream fis = new FileInputStream(resolver.getResource("testRemittance.xml").getFile());
		MockMultipartFile multipartFile = new MockMultipartFile("file", fis);

		Map<String, String[]> map = new HashMap<String, String[]>();
		map.put("center_or_account_group", new String[] { "1" });
		map.put("remittance_id", new String[] { "1" });
		map.put("tpa_id", new String[] { "TPAID0094" });
		map.put("account_group", new String[] { "0" });
		Assert.assertEquals("Unknown Health authority cannot parse the remittance advice file. ",
				remittanceService.create(map, multipartFile, -1, null));
	}

	@Test
	public void testRaDownloadListEmpty() throws IOException, SAXException, SQLException {
		Map<String, String[]> map = new HashMap<String, String[]>();
		map.put("received_date", new String[] { "01-01-2016", "01-02-2016" });
		map.put("status", new String[] { "new" });
		map.put("tpaId", new String[] { "TPAID0057" });
		Mockito.when(sessionService.getSessionAttributes()).thenReturn(getSessionServiceAttributes());
		PagedList result = remittanceService.radownloadlist(map, null);
		List dtoList = result.getDtoList();
		Assert.assertEquals(true, dtoList.isEmpty());
	}

	@Test
	public void testInsertXML() throws FileNotFoundException, IOException, SAXException, ParseException {
		PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
		BOMInputStream fis = new BOMInputStream(
				new FileInputStream(resolver.getResource("testRemittance.xml").getFile()));
		XMLRemittanceDigester xmlRemittanceDigester = new XMLRemittanceDigester();
		Digester digester = xmlRemittanceDigester.getDigester(centerId, centerService);
		RemittanceAdvice desc = (RemittanceAdvice) digester.parse(fis);

		remittanceService.insertXML(1, centerId, fis, digester, desc);
		Map<String, Object> filterMap = new HashMap<>();
		filterMap.put("remittance_id", 1);
		BasicDynaBean irdBean = irdRepository.findByKey(filterMap);
		Assert.assertNotNull(irdBean);
		Assert.assertEquals(irdBean.get("claim_id"), "CLD000089");
		Assert.assertEquals(irdBean.get("payer_id"), "TESTPAYERID");
		Assert.assertEquals(irdBean.get("provider_id"), "TESTPROID");
	}

	public Map<String, Object> getSessionServiceAttributes() {
		Map<String, Object> sessionAttributes = new HashMap<String, Object>();
		sessionAttributes.put("centerId", centerId);
		return sessionAttributes;
	}

}
