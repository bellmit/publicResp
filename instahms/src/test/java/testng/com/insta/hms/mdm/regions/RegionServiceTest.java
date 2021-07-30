package testng.com.insta.hms.mdm.regions;

import com.insta.hms.mdm.regions.RegionRepository;
import com.insta.hms.mdm.regions.RegionService;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author aditya
 *
 */
@Test
@ContextConfiguration(locations = { "classpath:spring/test-spring-config.xml" })
public class RegionServiceTest extends
		AbstractTransactionalTestNGSpringContextTests {

	private Logger logger = LoggerFactory.getLogger(RegionServiceTest.class);
	@Autowired
	private RegionService regionService;
	@Autowired
	private RegionRepository regionRepository;

	@BeforeMethod
	public void mockData() {
		logger.info("Before every RegionServiceTest");
		regionRepository.deleteAllRecords();
		regionService.insertRegion(buildParamMap(null, "Delhi", "A"));
		regionService.insertRegion(buildParamMap(null, "Mumbai", "A"));
		regionService.insertRegion(buildParamMap(null, "Banglore", "I"));
	}

	/**
	 * Test to retrieve all regions
	 */
	@Test
	public void getAllRegions() {
		logger.info("getAllRegions test");
		List<BasicDynaBean> listRegions = regionService.getAllRegions();
		List<String> regionNames = new ArrayList<String>();
		regionNames.add("Delhi");
		regionNames.add("Mumbai");
		regionNames.add("Banglore");
		
		List<String> listStatus = new ArrayList<String>();
		listStatus.add("A");
		listStatus.add("A");
		listStatus.add("I");
		Assert.assertEquals(getStringAttribute("region_name", listRegions), regionNames);
		Assert.assertEquals(getStringAttribute("status", listRegions), listStatus);
	}
	
	/**
	 * Test to retrieve region by id
	 */
	@Test
	public void getRegion() {
		logger.info("getRegion test");
		BasicDynaBean insertedBean = regionService.insertRegion(buildParamMap(
				null, "Pune", "A"));
		Integer region_id = (Integer) insertedBean.get("region_id");
		BasicDynaBean regionBean = regionService.getRegion("region_id", String.valueOf(region_id));
		Assert.assertEquals((String) regionBean.get("region_name"), "Pune");
	}
	
	/**
	 * Test for inserting a new region
	 * @param regionName
	 * @param status
	 */
	@Test(dataProvider="insertData")
	public void insertRegion(String regionName, String status) {
		logger.info("insertRegion test");
		Map<String,String[]> paramMap = buildParamMap(null ,regionName,status);
		regionService.insertRegion(paramMap);
		BasicDynaBean bean = regionRepository.findByKey("region_name", regionName);
		Assert.assertNotNull(bean);
	}
	
	/**
	 * Test for updating a region
	 * @param id 
	 * @param regionName
	 * @param status
	 */
	@Test(dataProvider="insertData")
	public void updateRegion(String regionName, String status) {
		logger.info("updateRegion test");
		BasicDynaBean insertedBean = regionService.insertRegion(buildParamMap(
				null, "Agra", "I"));
		Integer id = (Integer) insertedBean.get("region_id");
		Map<String, String[]> paramMap = buildParamMap(id, regionName, status);
		regionService.updateRegion(paramMap);
		BasicDynaBean bean = regionRepository.findByKey("region_id", id);
		System.out.println(bean + "region_id:" + id);
		Assert.assertNotNull(bean);
		Assert.assertEquals((String) bean.get("region_name"), regionName);
	}
	
	/*
	 * Data provider for insert region test
	 */
	@DataProvider(name="insertData")
	public static Object[][] insertData() {
		return new Object[][]{{"Pune","A"}};
	}
	
	public Map<String,String[]> buildParamMap(Integer id,String regionName,String status) {
		Map<String,String[]> paramMap = new HashMap<String,String[]>();
		if (null != id)
			paramMap.put("region_id", new String[]{id.toString()});
		if (null != regionName)
			paramMap.put("region_name", new String[]{regionName});
		if (null != status)
			paramMap.put("status", new String[]{status});
		return paramMap;
	}
	
	public List<String> getStringAttribute(String attribute, List<BasicDynaBean> listBean) {
		List<String> listAttribute = new ArrayList<String>();
		for(BasicDynaBean bean : listBean) {
			listAttribute.add((String) bean.get(attribute));
		}
		return listAttribute;
	}
}