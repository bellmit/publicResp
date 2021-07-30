package testng.com.insta.hms.mdm.equipment;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.DynaBeanBuilder;
import com.insta.hms.mdm.bedtypes.BedTypeService;
import com.insta.hms.mdm.departments.DepartmentService;
import com.insta.hms.mdm.equipment.EquipmentRepository;
import com.insta.hms.mdm.equipment.EquipmentService;
import com.insta.hms.mdm.equipmentcharges.EquipmentChargesService;
import com.insta.hms.mdm.servicegroup.ServiceGroupService;
import com.insta.hms.mdm.servicesubgroup.ServiceSubGroupService;
import com.insta.hms.mdm.taxgroups.TaxGroupService;
import com.insta.hms.mdm.taxsubgroups.TaxSubGroupService;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Test
@ContextConfiguration(locations = { "classpath:spring/test-spring-config.xml" })
public class EquipmentTest extends
AbstractTransactionalTestNGSpringContextTests {
  private Logger logger = LoggerFactory.getLogger(EquipmentTest.class);
  @InjectMocks
  private EquipmentService equiService;
  @Spy
  private EquipmentRepository equiRepo = new EquipmentRepository();
  @Mock
  private HttpServletRequest req;
  @Mock
  private HttpSession session;
  @Mock
  private EquipmentChargesService equipmentChargeService;
  @Mock
  private BedTypeService bedTypeService;
  @Mock
  private ServiceSubGroupService serviceSubGroupService;
  @Mock
  private ServiceGroupService serviceGroupService;
  @Mock
  private DepartmentService departmentService;
  @Mock 
  private TaxGroupService taxGroupService;
  @Mock 
  private TaxSubGroupService taxSubGroupService;

  @BeforeMethod
  public void mockData() {
    logger.info("Before every EquipmentTest");
    MockitoAnnotations.initMocks(this);
    equiRepo.deleteAllRecords();
  }

  @Test
  public void insertEquipmentDetails() {
    logger.info("Equipment insert test");
    StringBuilder msg = new StringBuilder();
    Mockito.when(req.getSession(false)).thenReturn(session);
    Mockito.when(session.getAttribute("userid")).thenReturn("testuser");
    Mockito.when(equipmentChargeService.initItemCharges(Mockito.anyString())).
      thenReturn(true);
    Map<String,String[]> params = buildParamMap("Cardiac Monitor Test");
    String eqId = equiService.insertEquipmentDetails(req, params, msg);
    Assert.assertNotNull(eqId);
  }

  @Test
  public void updateEquipDetails() {
    logger.info("Equipment update test");
    insertData();
    StringBuilder msg = new StringBuilder();
    Map<String,String[]> params = buildParamMap("Cardiac Monitor Test");
    boolean value= equiService.updateEquipmentDetails(req, params, "EQI001", msg);
    Assert.assertEquals(value, true);
  }

  @Test
  public void getEditPageDate() {
    logger.info("Equipment display test");
    insertData();
    Mockito.when(bedTypeService.getAllBedTypes()).thenReturn(new ArrayList<BasicDynaBean>());
    DynaBeanBuilder builder = new DynaBeanBuilder();
    builder.add("service_sub_group_id", Integer.class);
    builder.add("dept_id");
    builder.add("service_group_id");
    BasicDynaBean bean = builder.build();
    bean = Mockito.spy(bean);
    Mockito.when(serviceSubGroupService.findByPk(Mockito.anyMap())).thenReturn(bean);
    Mockito.when(equiRepo.getEquipmentDetails("EQI001", "ORG001")).thenReturn(bean);  
    Mockito.when(bean.get("service_sub_group_id")).thenReturn(1);
    Mockito.when(bean.get("service_group_id")).thenReturn("1");
    Mockito.when(bean.get("dept_id")).thenReturn("DEP0001");
    Map mergeMap = equiService.getEditPageDate("EQI001", "ORG001");
    Assert.assertNotNull(mergeMap);
  }

  public Map<String,String[]> buildParamMap(String eqName) {
    Map<String,String[]> paramMap = new HashMap<String,String[]>();
    paramMap.put("equipment_name", new String[] {eqName});
    paramMap.put("status", new String[] {"A"});
    paramMap.put("dept_id", new String[] {"DEP0001"});
    paramMap.put("serviceSubGroupId", new String[] {"1"});
    paramMap.put("min_duration", new String[] {"0.00"});
    paramMap.put("incr_duration", new String[] {"0.00"});
    paramMap.put("slab_1_threshold", new String[] {"1"});
    return paramMap;
  }

  public void insertData() {
    String insertEquipmentTest = "INSERT INTO equipment_master (eq_id, equipment_name,status,dept_id, service_sub_group_id,"
        + " min_duration,incr_duration, slab_1_threshold)"
        + " VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    Object[] params = new Object[] {"EQI001", "Cardiac Monitor Test","A", "DEP0001", 1,
        0.0, 0.0, 1};
    DatabaseHelper.insert(insertEquipmentTest, params);
  }
}
