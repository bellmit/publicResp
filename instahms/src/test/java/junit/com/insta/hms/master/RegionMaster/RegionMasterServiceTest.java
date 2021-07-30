package junit.com.insta.hms.master.RegionMaster;

import static org.junit.Assert.assertEquals;

import com.insta.hms.common.PagedList;
import com.insta.hms.exception.ConversionException;
import com.insta.hms.exception.ValidationException;
import com.insta.hms.mdm.regions.RegionService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author yashwant
 *  
 *
 */

/**
 * 
 * Followings are the ways of reading spring configuration file
 * 
 * 1. @ContextConfiguration(locations="classpath:spring/junit-test-config.xml")
 * 
 * 2. @ContextConfiguration(locations={"/spring/junit-test-config.xml","/spring/test-spring-config.xml"})
 * 
 * 3. @ContextConfiguration(locations={"classpath:/spring/junit-test-config.xml","classpath:/spring/test-spring-config.xml"})
 * 
 * 4. @ContextConfiguration(locations="file:WEB-INF/spring/spring-config.xml")
 * 
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:spring/junit-test-config.xml"})
@Transactional
public class RegionMasterServiceTest {

	@Autowired
	private RegionService regionMasterService;
	
	private Logger logger = LoggerFactory.getLogger(RegionMasterServiceTest.class);
	
	@Test
	public void testInsertRegion() throws ConversionException, ValidationException  {
		logger.info("Testing testInsertRegion ::: ");
		Map<String,String[]> params = preparedParam();
		logger.info("Inserting regionName : "+params.get("region_name"));
		
		BasicDynaBean bean = regionMasterService.insertRegion(params);

		assertEquals(params.get("region_name")[0], bean.get("region_name"));
		assertEquals(params.get("status")[0], bean.get("status"));
	}
	
	/**
	 * 
	 * @throws DuplicateDataException
	 * @throws ConversionException
	 * @throws ValidationException
	 * 
	 * First this method insert the data into database and then its update
	 */
	@Test
	public void testUpdateRegion() throws ConversionException, ValidationException  {
		logger.info("Testing testUpdateRegion ");
		Map<String,String[]> params = preparedParam();
		logger.info("Inserting regionName : "+params.get("region_name"));
		
		BasicDynaBean bean = regionMasterService.insertRegion(params);

		Map<String,String[]> updateParams = new HashMap<String, String[]>();
		
		updateParams.put("region_id", new String[]{((Integer)bean.get("region_id")).toString()});
		
		String updateStatus = params.get("status")[0].equals("A") ? "I" : "A";
		updateParams.put("status", new String[]{updateStatus});
		logger.info("Updating status of regionName : "+params.get("region_name"));
		
		updateParams.put("region_name",new String[]{"testRegion"});
		
		int status = regionMasterService.updateRegion(updateParams);
		assertEquals(1, status);
	}
	
	/**
	 * 
	 * @throws DuplicateDataException
	 * @throws ConversionException
	 * @throws ValidationException
	 * This method insert the data and then query the data and verify whether data is available in the list or not
	 */
	@Test
	public void testGetAllRegions() throws ConversionException, ValidationException {
		
		logger.info("Testing testGetRegionMasterDetails ");
		Map<String,String[]> params = preparedParam();
		String regionName = params.get("region_name")[0];
		logger.info("Inserting regionName : "+regionName);
		regionMasterService.insertRegion(params);
		
		List<BasicDynaBean> beans = regionMasterService.getAllRegions();
		Boolean status = verifyRegionName(regionName,beans);
		assertEquals( true , beans.size() > 0);
		assertEquals(true, status);
		
	}
	
	/**
	 * 
	 * @throws SQLException
	 * @throws ParseException
	 * @throws DuplicateDataException
	 * @throws ConversionException
	 * @throws ValidationException
	 * 
	 * This method insert the data and then query the data and verify whether data is available in the list or not
	 * 
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testGetRegionMasterDetails() throws SQLException, ParseException, ConversionException, ValidationException {
		
		logger.info("Testing testGetRegionMasterDetails ");
		Map<String,String[]> params = preparedParam();
		String regionName = params.get("region_name")[0];
		logger.info("Inserting regionName : "+regionName);
		BasicDynaBean bean = regionMasterService.insertRegion(params);

		Map<String, String[]> readParams = new HashMap<String, String[]>();
		readParams.put("region_name", new String[]{ (String)bean.get("region_name") });
		
		PagedList pagedList = regionMasterService.getRegionMasterDetails(readParams);
		
		Boolean status = verifyRegionName(regionName,pagedList.getDtoList());
		assertEquals(true, status);
	}

	@Test
	public void testGetRegion() throws ConversionException, ValidationException {
		
		logger.info("Testing testGetRegionMasterDetails ");
		Map<String,String[]> params = preparedParam();
		String regionName = params.get("region_name")[0];
		logger.info("Inserting regionName : "+regionName);
		BasicDynaBean insertedBean = regionMasterService.insertRegion(params);

		Map<String, String[]> readParams = new HashMap<String, String[]>();
		readParams.put("region_name", new String[]{ (String)insertedBean.get("region_name") });

		BasicDynaBean bean = regionMasterService.getRegion("region_id", insertedBean.get("region_id").toString());
		
		assertEquals(regionName, (String)bean.get("region_name"));
	}
	
	private Map<String,String[]> preparedParam() {
		
		Map<String, String[]> params = new HashMap<String, String[]>();
		SimpleDateFormat formater = new SimpleDateFormat("yyyyddMMHHmmSS");
		String dynamicRegionName = "Bangalore-"+formater.format(new Date());
		params.put("region_name", new String[]{dynamicRegionName} );
		params.put("status", new String[]{"A"} );
		return params;
		
	}
	
	@SuppressWarnings("rawtypes")
	private Boolean verifyRegionName(String dynamicRegionName, List<BasicDynaBean> dtoList) {

		Iterator it = dtoList.iterator();
		while(it.hasNext()) {
			BasicDynaBean bean = (BasicDynaBean) it.next();
			if((dynamicRegionName.equals(bean.get("region_name")))) 
				return true;
		}
		return false;
	}
}
