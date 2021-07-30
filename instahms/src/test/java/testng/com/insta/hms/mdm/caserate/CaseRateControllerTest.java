package testng.com.insta.hms.mdm.caserate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.caserate.CaseRateController;
import com.insta.hms.mdm.caserate.CaseRateService;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTransactionalTestNGSpringContextTests;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@Test
@ContextConfiguration(locations = {"classpath:spring/test-spring-config.xml"})
public class CaseRateControllerTest extends AbstractTransactionalTestNGSpringContextTests {

  @InjectMocks
  private CaseRateController caseRateController;

  @Mock
  private CaseRateService caseRateService;

  private MockMvc mockMvc;

  @BeforeTest
  public void init() {
    initMocks(this);
    this.mockMvc = MockMvcBuilders.standaloneSetup(this.caseRateController).build();
  }

  @Test
  public void findByFilters() throws Exception {
    Map<String, String> params = new HashMap<>();
    this.mockMvc.perform(get(URLRoute.CASE_RATE_MASTER + URLRoute.FIND_BY_FILTER)
        .accept(MediaType.APPLICATION_JSON_VALUE).param("params",
            new ObjectMapper().writeValueAsString(params))).andExpect(status().isOk());
  }
}
