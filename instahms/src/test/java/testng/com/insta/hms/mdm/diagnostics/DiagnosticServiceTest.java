package testng.com.insta.hms.mdm.diagnostics;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.log4j.Logger;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTransactionalTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.bob.hms.common.RequestContext;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.diagexportinterfaces.DiagExportInterfaceService;
import com.insta.hms.mdm.diagnostics.DiagnosticTestRepository;
import com.insta.hms.mdm.diagnostics.DiagnosticTestService;
import com.insta.hms.mdm.diagtatcenter.DiagTatCenterService;
import com.insta.hms.mdm.diagtestresults.DiagTestResultService;

import static org.mockito.Mockito.doReturn;

@Test
@ContextConfiguration(locations = { "classpath:spring/test-spring-config.xml" })
public class DiagnosticServiceTest extends
AbstractTransactionalTestNGSpringContextTests {
  private Logger logger = Logger.getLogger(DiagnosticServiceTest.class);
  @Spy
  @InjectMocks
  private DiagnosticTestService diagService;
  @Spy
  private DiagnosticTestRepository diagRepo = new DiagnosticTestRepository();
  @Mock
  private HttpServletRequest req;
  @Mock
  private HttpSession session;
  @Mock
  private DiagExportInterfaceService diagExportInterfaceService;
  @Mock
  private DiagTestResultService diagTestResultService;
  @Mock
  private DiagTatCenterService diagTatCenterService;
  @Autowired
  private DiagnosticTestService diagTestService;

  @BeforeMethod
  public void mockData() {
    logger.info("Before every DiagnosticServiceTest");
    MockitoAnnotations.initMocks(this);
    diagRepo.deleteAllRecords();
  }

  @Test
  public void insertTestDetails() {
    logger.info("Diagnostic Test insert test");
    StringBuilder msg = new StringBuilder();
    Mockito.when(req.getParameter("serviceSubGroupId")).thenReturn("1");
    Mockito.when(req.getSession(false)).thenReturn(session);
    Mockito.when(session.getAttribute("userid")).thenReturn("testuser");
    Mockito.when(req.getParameter("allow_zero_claim_amount")).thenReturn("n");
    Mockito.when(req.getParameter("billing_group_id")).thenReturn("1");
    Mockito.when(diagExportInterfaceService.insertDiagExprtInterfaces(
        Mockito.anyString(),Mockito.any(String[].class),
        Mockito.any(String[].class),Mockito.any(String[].class))).thenReturn(true);
    Mockito.when(diagTestResultService.insertTestResults(
        Mockito.anyString(),Mockito.any(HttpServletRequest.class),Mockito.anyBoolean(),
        Mockito.any(StringBuilder.class))).thenReturn(true);
    Mockito.when(diagTatCenterService.addTestTatCenters(Mockito.anyString())).thenReturn(true);

    try {
      doReturn(true).when(diagService).saveOrUpdateTestEquipments(Mockito.anyString(), Mockito.any(HttpServletRequest.class));
    } catch (Exception e) {
      logger.equals(e.getMessage());
    }
    Mockito.when(req.getParameterMap()).thenReturn(buildParamMap("TST001", "24 HR URINE TEST"));
    boolean value=diagService.insertTestDetails(req, "TST001", msg);
    Assert.assertEquals(value, true);
  }

  @Test
  public void updateTestDetails() {
    logger.info("Diagnostic Test update test");
    insertData();
    StringBuilder msg = new StringBuilder();
    Mockito.when(req.getParameter("serviceSubGroupId")).thenReturn("1");
    Mockito.when(req.getSession(false)).thenReturn(session);
    Mockito.when(session.getAttribute("userid")).thenReturn("testuser");
    Mockito.when(req.getParameter("allow_zero_claim_amount")).thenReturn("n");
    Mockito.when(req.getParameterMap()).thenReturn(buildParamMap("TST001", "24 HR URINE TEST"));
    Mockito.when(diagExportInterfaceService.updateDiagExprtInterfaces(
        Mockito.anyString(),Mockito.any(String[].class),Mockito.any(String[].class),
        Mockito.any(String[].class),Mockito.any(String[].class))).thenReturn(true);
    Mockito.when(diagTestResultService.updateTestResults(
        Mockito.anyString(),Mockito.any(HttpServletRequest.class),Mockito.anyBoolean(),
        Mockito.any(StringBuilder.class))).thenReturn(true);
    Mockito.when(diagTestResultService.resultsCenterApplicabilityCheck("TST001", msg)).
      thenReturn(true);
    try {
      doReturn(true).when(diagService).saveOrUpdateTestEquipments(Mockito.anyString(), Mockito.any(HttpServletRequest.class));
    } catch (Exception e) {
      logger.equals(e.getMessage());
    }
    boolean value = diagService.updateTestDetails(req, "TST001", msg);
    Assert.assertEquals(value, true);
  }

  @Test
  public void getListEditPageData( ) {
    logger.info("Diagnostic Test display test");
    insertData();
    List<BasicDynaBean> testDeatailsList;
    testDeatailsList = diagTestService.getTestDetails("TST001");
    BasicDynaBean bean = testDeatailsList.get(0);
    bean = Mockito.spy(bean);
    Mockito.when(bean.get("service_sub_group_id")).thenReturn(new Integer("1"));
    testDeatailsList.set(0, bean);
    RequestContext.setConnectionDetails(new String[]{"","","","","0"});
    Map mergeMap = diagTestService.getListEditPageData(testDeatailsList, "TST001", "ORG001");
    Assert.assertNotNull(mergeMap);
  }

  public Map<String,String[]> buildParamMap(String testId, String testName) {
    Map<String,String[]> paramMap = new HashMap<String,String[]>();
    paramMap.put("test_id", new String[] {testId});
    paramMap.put("test_name", new String[] {testName});
    paramMap.put("status", new String[] {"A"});
    paramMap.put("ddept_id", new String[] {"DDept0001"});
    paramMap.put("sample_needed", new String[] {"y"});
    paramMap.put("conduction_applicable", new String[] {"false"});
    paramMap.put("allow_rate_increase", new String[] {"false"});
    paramMap.put("allow_rate_decrease", new String[] {"false"});
    paramMap.put("mandate_additional_info", new String[] {"n"});
    paramMap.put("test_duration", new String[] {"0"});
    paramMap.put("mandate_clinical_info", new String[] {"N"});
    return paramMap;
  }

  public void insertData() {
    String insertDiagnosticsTest = "INSERT INTO diagnostics ( test_id, test_name, status, ddept_id, sample_needed, allow_zero_claim_amount, test_duration)"
        + " VALUES (?, ?, ?, ?, ?, ?, ?)";
    Object[] params = new Object[] {"TST001", "24 HR URINE TEST","A", "DDept0001", "y", "n", 0};
    DatabaseHelper.insert(insertDiagnosticsTest, params);
  }
}
