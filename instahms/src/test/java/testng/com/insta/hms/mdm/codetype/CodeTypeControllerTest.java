package testng.com.insta.hms.mdm.codetype;

import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.codetype.CodeTypeController;
import com.insta.hms.mdm.codetype.CodeTypeService;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTransactionalTestNGSpringContextTests;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.HashMap;

@Test
@ContextConfiguration(locations = { "classpath:spring/test-spring-config.xml" })
public class CodeTypeControllerTest extends AbstractTransactionalTestNGSpringContextTests{
	
	@InjectMocks
	private CodeTypeController codeTypeController;
	
	@Mock
	private CodeTypeService codeTypeService;
	
	private MockMvc mockMvc;
	
	@BeforeTest
	public void init() {
		initMocks(this);
		this.mockMvc = MockMvcBuilders.standaloneSetup(this.codeTypeController).build();
	}
	
	@Test
	public void getCodeTypeListTest() throws Exception {
		Mockito.when(this.codeTypeService.getCodeTypeList())
			.thenReturn(new HashMap<String, Object>());
		this.mockMvc.perform(get(URLRoute.CODE_TYPE_MASTER + "/codeTypeList")
			.accept(MediaType.APPLICATION_JSON_VALUE)).andExpect(status().isOk());
		Mockito.verify(this.codeTypeService).getCodeTypeList();
	}
	
	@Test
	public void getCodeDetailsByCodeTypeTest() throws Exception {
		String codeType = "codeType";
		String searchInput = "searchInput";
		Mockito.when(this.codeTypeService.getCodeDetailsByCodeType(searchInput, codeType))
				.thenReturn(new HashMap<String, Object>());
		this.mockMvc.perform(get(URLRoute.CODE_TYPE_MASTER + "/detailList")
				.param("codeType", codeType).param("searchInput", searchInput)
				.accept(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isOk());
		Mockito.verify(this.codeTypeService).getCodeDetailsByCodeType(searchInput, codeType);
	}
	
	@AfterTest
	public void tearDown() {
		Mockito.verifyNoMoreInteractions(this.codeTypeService);
	}
}
