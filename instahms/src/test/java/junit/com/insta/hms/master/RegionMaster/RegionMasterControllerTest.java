package junit.com.insta.hms.master.RegionMaster;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.regions.RegionController;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

/**
 * 
 * @author yashwant
 *
 */
public class RegionMasterControllerTest {

    private static final String CONTROLLER_URL =  URLRoute.REGION_MASTER;
    
    @Autowired
    private RegionController controller;
    
    @Test
    public void testList() throws Exception {

        MockMvc mockMvc = standaloneSetup(controller).build();
        
        mockMvc.perform(get(CONTROLLER_URL+"/list.htm")
                            .param("sortOrder", "region_id")
                            .param("ortReverse", "false")
                            .param("status", "A"))// GET method call and URL add.htm
                        .andExpect(view().name(URLRoute.REGION_MASTER_LIST)); // Expected page call
                        //.andExpect(model().attributeExists("regionsList"));
        
    }
    
    @Test
    public void testAdd() throws Exception {
 
        MockMvc mockMvc = standaloneSetup(controller).build();
        
        mockMvc.perform(get(CONTROLLER_URL+"/add.htm")) // GET method call and URL add.htm
                        .andExpect(view().name(URLRoute.REGION_MASTER_ADD)) // Expected page call
                        .andExpect(model().attributeExists("regionsList")); // Expected attribute in request
        //mockMvc.perform(get("/master/RegionMaster/add.htm")).andExpect(view().name("/pages/master/RegionMaster/list"));
        
    }
    
    @Test
    public void testShow() throws Exception {

        MockMvc mockMvc = standaloneSetup(controller).build();
        
        mockMvc.perform(get(CONTROLLER_URL+"/show.htm")) // GET method call and URL add.htm
                        .andExpect(view().name(URLRoute.REGION_MASTER_SHOW));
        
    }
    
    @Test
    public void testCreate() throws Exception {
        
    }
    
    @Test
    public void testUpdate() throws Exception {
        
    }
}
