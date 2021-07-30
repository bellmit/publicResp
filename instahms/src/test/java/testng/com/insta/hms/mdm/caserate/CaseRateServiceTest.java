package testng.com.insta.hms.mdm.caserate;

import static org.mockito.MockitoAnnotations.initMocks;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.mdm.caserate.CaseRateDetailRepository;
import com.insta.hms.mdm.caserate.CaseRateRepository;
import com.insta.hms.mdm.caserate.CaseRateService;
import com.insta.hms.mdm.caserate.CaseRateValidator;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.io.FileUtils;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTransactionalTestNGSpringContextTests;
import org.springframework.ui.ModelMap;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import testng.utils.TestRepoInit;

@Test
@ContextConfiguration(locations = {"classpath:spring/test-spring-config.xml"})
public class CaseRateServiceTest extends AbstractTransactionalTestNGSpringContextTests {

  private static final String FILENAME = "CaseRate.json";

  @InjectMocks
  private CaseRateService caseRateService;

  @Spy
  private CaseRateRepository caseRateRepository;

  @Spy
  private CaseRateDetailRepository caseRateDetailRepository;

  @Spy
  private CaseRateValidator caseRateValidator;

  @Mock
  private SessionService sessionService;

  private Map<String, Object> dbDataMap = null;
  private List<ModelMap> dataProviderInsertJson;

  @BeforeMethod
  public void init() throws IOException {
    initMocks(this);
    TestRepoInit testRepo = new TestRepoInit();
    testRepo.insert("insurance_company_master");
    testRepo.insert("insurance_category_master");
    testRepo.insert("insurance_plan_main");
    testRepo.insert("item_insurance_categories");
    testRepo.insert("case_rate_main");
    testRepo.insert("case_rate_detail");

    dbDataMap = testRepo.initializeRepo();
  }

  @Test
  public void findByFiltersSuccessTest() throws JsonProcessingException {

    Map<String, String> params = new HashMap<>();
    params.put("page_size", "1");
    params.put("page_number", "0");
    params.put("code", "code");
    params.put("code_type", "code_type");
    params.put("case_rate_number", "1");
    params.put("status", "A");

    Map<String, Object> result = this.caseRateService.findByFilters(params);
    assertNotNull(result);
    assertEquals(((Map) ((List) result.get("case_rate_main")).get(0)).get("case_rate_id").toString(),
        ((Map) ((List) dbDataMap.get("case_rate_main")).get(0)).get("case_rate_id"));
    assertEquals((result.get("total_records")), 1L);
    assertEquals((result.get("page_size")), 1);
  }

  @Test
  public void findByFiltersEmptyTest() throws JsonProcessingException {
    Map<String, String> params = new HashMap<>();
    params.put("code", "notExisting");
    params.put("code_type", "code_type");
    Map<String, Object> result = this.caseRateService.findByFilters(params);
    assertNotNull(result);
    assertTrue(((List) result.get("case_rate_main")).isEmpty());
    assertEquals((result.get("total_records")), 0);
  }

  @Test
  public void findByFilterStatusTest() throws JsonProcessingException {
    Map<String, String> params = new HashMap<>();
    params.put("status", "A");
    Map<String, Object> result = this.caseRateService.findByFilters(params);
    assertNotNull(result);
    assertEquals(((Map) ((List) result.get("case_rate_main")).get(0)).get("case_rate_id").toString(),
        ((Map) ((List) dbDataMap.get("case_rate_main")).get(0)).get("case_rate_id"));
    assertEquals((result.get("total_records")), 1L);
  }

  @Test
  public void findByPkSuccessTest() {
    Map<String, Object> params = new HashMap<>();
    params.put("case_rate_id", 100);

    BasicDynaBean result = this.caseRateService.findByPk(params, true);
    assertNotNull(result);
    assertEquals(result.get("case_rate_id").toString(),
        ((Map) ((List) dbDataMap.get("case_rate_main")).get(0)).get("case_rate_id"));
  }

  @Test
  public void findByPkNotFoundTest() {
    Map<String, Object> params = new HashMap<>();
    params.put("case_rate_id", 3);

    BasicDynaBean result = this.caseRateService.findByPk(params, true);
    assertNull(result);
  }

  @Test(dataProvider = "insertData")
  public void insertCaseRateTest(ModelMap jsonRequest) throws Exception {
    Mockito.when(sessionService.getSessionAttributes()).thenReturn(getSessionServiceAttributes());

    BasicDynaBean parentBean = caseRateService.toBean(jsonRequest);
    Map<String, Map<String, Map<String, BasicDynaBean>>> detailMapBean = caseRateService
        .toBeansMap(jsonRequest, parentBean);
    caseRateService.insertDetailsMap(parentBean, detailMapBean);
    BasicDynaBean bean = caseRateService.findByUniqueName(
        (String) jsonRequest.get("insurance_company_id"), "insurance_company_id");

    Assert.assertNotNull(bean);
  }

  @DataProvider(name = "insertData")
  public Object[][] insertData() throws Exception {
    ClassLoader classLoader = getClass().getClassLoader();
    String jsonString =
			FileUtils.readFileToString(new File(classLoader.getResource(FILENAME).getFile()), "UTF-8");
    ObjectMapper mapper = new ObjectMapper();
    Map<String, Object> map = mapper.readValue(jsonString, new TypeReference<Map<String,
			List<ModelMap>>>() {
    });

    this.dataProviderInsertJson = (List<ModelMap>) map.get("dataProviderInsert");
    Object[][] returnData = new Object[dataProviderInsertJson.size()][1];
    for (int i = 0; i < dataProviderInsertJson.size(); i++) {
      returnData[i][0] = dataProviderInsertJson.get(i);
    }
    return returnData;
  }

  public Map<String, Object> getSessionServiceAttributes() {
    Map<String, Object> sessionAttributes = new HashMap<String, Object>();
    sessionAttributes.put("userId", "InstaAdmin");
    return sessionAttributes;
  }
}
