package testng.com.insta.hms.mdm.tpas;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BasicDynaBean;
import org.mockito.InjectMocks;
import static org.mockito.MockitoAnnotations.initMocks;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTransactionalTestNGSpringContextTests;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.insta.hms.mdm.tpas.TpaRepository;

import testng.utils.TestRepoInit;

@Test
@ContextConfiguration(locations = {"classpath:spring/test-spring-config.xml"})
public class TpaRepositoryTest extends AbstractTransactionalTestNGSpringContextTests {

	@InjectMocks
	private TpaRepository tpaRepository;
	
	private Map<String, Object> dbDataMap = null;
	
	@BeforeMethod
	public void init() throws IOException {
		initMocks(this);
		
		 TestRepoInit testRepo = new TestRepoInit();
	     testRepo.insert("patient_category_master");
	     testRepo.insert("tpa_master");
	     
	     dbDataMap = testRepo.initializeRepo();
	    
	}
	
	
	@Test
	public void testGetDetails() {
		Object[] params = new Object[3];
		
		/**
		 * Set Patient Category Id
		 */
		params[0] = 1;
		
		/**
		 * Set tpa name that needs to be searched
		 */
		params[1] = "a";
		
		/**
		 * Set the limit, total number of results to be
		 * returned
		 */
		
		params[2] = 25;
		
		List<BasicDynaBean> result = this.tpaRepository.getDetails(params);
				
		assertEquals(5, result.size());
		
		params[1] = "tk";
		result = this.tpaRepository.getDetails(params);
		assertEquals(1, result.size());
	}
}
