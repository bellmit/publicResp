package testng.com.insta.hms.mdm.codetype;

import static org.mockito.MockitoAnnotations.initMocks;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import com.insta.hms.mdm.codetype.CodeTypeRepository;
import com.insta.hms.mdm.codetype.CodeTypeService;

import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTransactionalTestNGSpringContextTests;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import testng.utils.TestRepoInit;

@Test
@ContextConfiguration(locations = { "classpath:spring/test-spring-config.xml" })
public class CodeTypeServiceTest extends AbstractTransactionalTestNGSpringContextTests {

	
	@InjectMocks
	private CodeTypeService codeTypeService;

	@Spy
	private CodeTypeRepository codeTypeRepository;

	private Map<String, Object> dbDataMap;
	
	@BeforeMethod
	public void init() throws IOException {
	        initMocks(this);
	        TestRepoInit testRepo = new TestRepoInit();
	        testRepo.insert("mrd_supported_code_types");       
	        dbDataMap  = testRepo.initializeRepo(); 
	}
	
	@Test
	public void getCodeTypeListTest() {

		Map<String, Object> result = this.codeTypeService.getCodeTypeList();
		List<Map<String, Object>> codeTypeList = (ArrayList<Map<String, Object>>) result.get("code_type_list");
		assertNotNull(codeTypeList);
		assertEquals(codeTypeList.get(0).get("code_type"),
				((Map) ((List) dbDataMap.get("mrd_supported_code_types")).get(0)).get("code_type"));
		
	}
}
