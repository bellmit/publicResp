package testng.com.insta.hms.mdm.testequipments;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import com.bob.hms.common.RequestContext;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.SearchQueryAssembler;
import com.insta.hms.mdm.testequipments.TestEquipmentRepository;
import com.insta.hms.mdm.testequipments.TestEquipmentResultsRepository;
import com.insta.hms.mdm.testequipments.TestEquipmentService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTransactionalTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Test
@ContextConfiguration(locations = { "classpath:spring/test-spring-config.xml" })
public class TestEquipmentServiceTest extends AbstractTransactionalTestNGSpringContextTests{
	private Logger logger = LoggerFactory.getLogger(TestEquipmentServiceTest.class);
	@Autowired private TestEquipmentService testEquipmentService;
	@Autowired private TestEquipmentRepository testEquipmentRepository;
	@Autowired private TestEquipmentResultsRepository testEquipmentResultsRepository;
	
	@BeforeMethod
	public void mockData() throws SQLException {
		logger.info("Before every TestEquipmentServiceTest");
//		testEquipmentRepository.deleteAllRecords();
		testEquipmentResultsRepository.deleteAllRecords();
		Map<String,String[]> params = buildParamMap(null, "testeq",true,"322", "A", 7,"DDept0039",0,null,752,"Y","N");
		Map<String,String[]> params1 = buildParamMap(null, "testeq11",true,"326", "A", 7,"DDept0039",0,null,752,"Y","N");
		StringBuilder msg = new StringBuilder();
		testEquipmentService.insertTestEquipmentDetails(params,msg);
		testEquipmentService.insertTestEquipmentDetails(params1,msg);
	}
	
	@Test
	public void getSearchQueryAssembler() {
		logger.info("get test equipmentlist");
		Map<String,String[]> searchparams = buildSearchParamMap("A","equipment_name",false);
		SearchQueryAssembler qb = null;
		RequestContext.setConnectionDetails(new String[]{"","","","","0"});
		qb = testEquipmentService.getSearchQueryAssembler(searchparams, ConversionUtils.getListingParameter(searchparams));
		assertNotNull(qb);
	}	
	
	/*
	 * Data provider for insert region test
	 */
	@DataProvider(name="insertData")
	public static Object[][] insertData() {
		return new Object[][]{{"testequip",true,"43","A",0,"DDept0039",0,241,"Y","N"}};
	}
	
	@DataProvider(name="showData")
	public static Object[][] showData() {
		return new Object[][]{{"testequip"}};
	}
	
	private Map<String, String[]> buildSearchParamMap(String status,
			String sortOrder, Boolean sortReverse) {
		Map<String,String[]> paramMap = new HashMap<String,String[]>();
		if (null != status)
			paramMap.put("tm.status", new String[]{status});
		if (null != sortOrder)
			paramMap.put("sortOrder", new String[]{sortOrder});
		if(null != sortReverse)
			paramMap.put("sortReverse", new String[]{String.valueOf(sortReverse)});
		return paramMap;
	}

	@Test
	public void testGetListPageLookup(){
		logger.info("Testing testGetListPageLookup method");
		Map<String, String[]> params = new HashMap<String, String[]>();
		Map<String, List<BasicDynaBean>> mapBeans = null;
		mapBeans = testEquipmentService.getListPageLookup(params);
		
		assertNotNull(mapBeans);
		assertEquals(true,mapBeans.containsKey("genPrefs")," genPrefs tested");
		assertEquals(true, mapBeans.containsKey("centers"), "centers tested ");
		assertEquals(true, mapBeans.containsKey("diagdepts")," diagdepts tested");		
	}
	
	@Test
	public void testgetAddPageData(){
		logger.info("Testing testgetAddPageData method");
		Map<String, String[]> params = new HashMap<String, String[]>();
		Map<String, List<BasicDynaBean>> mapBeans = null;
		mapBeans = testEquipmentService.getAddPageData(params);
		
		assertNotNull(mapBeans);
		assertEquals(true,mapBeans.containsKey("genPrefs")," genPrefs tested");
		assertEquals(true, mapBeans.containsKey("centers"), "centers tested ");
		assertEquals(true, mapBeans.containsKey("diagdepts")," diagdepts tested");
		assertEquals(true, mapBeans.containsKey("equipmentNames"), "equipmentNames tested");
		assertEquals(true, mapBeans.containsKey("result"), "result tested");
		
	}
	@Test(dataProvider="insertData")
	public void testinsertTestEquipmentDetails(String equipment_name, Boolean schedule,String hl7_export_code, String status, 
			Integer center_id, String ddept_id, Integer overbook_limit, Integer resultlabel_id,String newentry, String deleted) throws SQLException {
		logger.info("insert test equipments test");
		Map<String,String[]> paramMap = buildParamMap(null, equipment_name, schedule, hl7_export_code, status, 
			center_id, ddept_id, overbook_limit, null, resultlabel_id,newentry, deleted);
		StringBuilder msg = new StringBuilder();
		testEquipmentService.insertTestEquipmentDetails(paramMap,msg);
		BasicDynaBean bean = testEquipmentRepository.findByKey("equipment_name", equipment_name);
		Assert.assertNotNull(bean);
	}
	
	@Test(dataProvider="insertData")
	public void testupdateTestEquipmentDetails(String equipment_name, Boolean schedule,String hl7_export_code, String status, 
			Integer center_id, String ddept_id, Integer overbook_limit, Integer resultlabel_id,String newentry, String deleted) throws SQLException {
		logger.info("update test equipment test");
		Map<String,String[]> params = buildParamMap(null, "testuq",true,"322", "A", 7,"DDept0039",0,null,752,"Y","N");
		StringBuilder msg = new StringBuilder();
		int eq_id = testEquipmentService.insertTestEquipmentDetails(params,msg);
		Map<String, String[]> paramMap = buildParamMap(eq_id, equipment_name, schedule,hl7_export_code, status, 
				center_id, ddept_id, overbook_limit, eq_id, resultlabel_id,newentry,deleted);
		testEquipmentService.updateTestEquipmentDetails(paramMap,String.valueOf(eq_id),msg);
		BasicDynaBean bean = testEquipmentRepository.findByKey("eq_id", eq_id);
		Assert.assertNotNull(bean);
		Assert.assertEquals((String) bean.get("equipment_name"), equipment_name);
	}
	
	@Test
	@DataProvider(name="showData")
	public void testgetListEditPageData(String equipment_name) throws SQLException{
		logger.info("Testing testgetListEditPageData method");
		Map<String, String[]> params = new HashMap<String, String[]>();
		Map<String, List<BasicDynaBean>> mapBeans = null;
		BasicDynaBean bean = testEquipmentRepository.findByKey("equipment_name", equipment_name);
		mapBeans = testEquipmentService.getListEditPageData((String) bean.get("eq_id"));
		
		assertNotNull(mapBeans);
		assertEquals(true, mapBeans.containsKey("bean"), "bean tested");
		assertEquals(true,mapBeans.containsKey("genPrefs")," genPrefs tested");
		assertEquals(true, mapBeans.containsKey("centers"), "centers tested ");
		assertEquals(true, mapBeans.containsKey("diagdepts")," diagdepts tested");
		assertEquals(true, mapBeans.containsKey("equipmentNames"), "equipmentNames tested");
		assertEquals(true, mapBeans.containsKey("result"), "result tested");
		
	}

	public Map<String,String[]> buildParamMap(Integer eq_id,String equipment_name, Boolean schedule,
			String hl7_export_code, String status, Integer center_id, String ddept_id, Integer overbook_limit,
			Integer equipment_id, Integer resultlabel_id,String newentry, String deleted) {
		
		Map<String,String[]> paramMap = new HashMap<String,String[]>();
		if (null != eq_id)
			paramMap.put("eq_id", new String[]{eq_id.toString()});
		if (null != equipment_name)
			paramMap.put("equipment_name", new String[]{equipment_name});
		if(null != schedule)
			paramMap.put("schedule", new String[]{String.valueOf(schedule)});
		if(null != hl7_export_code)
			paramMap.put("hl7_export_code", new String[]{hl7_export_code});
		if (null != status)
			paramMap.put("status", new String[]{status});
		if(null != center_id)
			paramMap.put("center_id", new String[]{center_id.toString()});
		if(null != ddept_id)
			paramMap.put("ddept_id", new String[]{ddept_id});
		if(null != overbook_limit)
			paramMap.put("overbook_limit", new String[]{overbook_limit.toString()});
		if(null != equipment_id)
			paramMap.put("equipment_id", new String[]{equipment_id.toString()});
		if(null != resultlabel_id)
			paramMap.put("resultlabel_id", new String[]{resultlabel_id.toString()});
		if (null != status)
			paramMap.put("new", new String[]{newentry});
		if (null != status)
			paramMap.put("deleted", new String[]{deleted});
		
		return paramMap;
	}
	
}
