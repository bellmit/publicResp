package com.insta.hms.integration.configuration;

import com.insta.hms.common.PagedList;
import com.insta.hms.exception.HMSException;
import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterRestController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
* The Class ManageInterfacesController.
*/
@Controller
@RequestMapping(URLRoute.INTERFACE_MASTER_PATH)
public class ManageInterfacesController extends MasterRestController  {

  /** The Interface Config service. */
  @Autowired
  private InterfaceConfigService interfaceConfigService;

  /**
   * Instantiates a new interfaces controller.
   *
   * @param interfaceConfigService
   *          the interface config service
   */
  public ManageInterfacesController(InterfaceConfigService interfaceConfigService) {
    super(interfaceConfigService);
  }

  /**
   * Gets the manage interfaces index page.
   *
   * @return the manage interfaces index page
   */
  @GetMapping(URLRoute.MASTER_INDEX_URL)
  public ModelAndView getInterfacesIndexPage() {
    return renderMasterUi("Master", "interfaceMaster", true);
  }
  
  /**
   * Create a new interface in interface config master.
   *
   * @param req the request
   * @param resp the response
   * @param requestBody the request body
   * @return the response entity
   */
  @Override
  @PostMapping(value = "/create", consumes = "application/json")
  protected ResponseEntity<Map<String, Object>> create(HttpServletRequest req, 
      HttpServletResponse resp, @RequestBody ModelMap requestBody) {
    if (requestBody == null) {
      throw new HMSException(HttpStatus.BAD_REQUEST, "exception.bad.request", null);
    }
    return new ResponseEntity<>(interfaceConfigService.saveInterface(requestBody), 
        HttpStatus.CREATED);
  }
  
  /**
   * List all interfaces created in interface config master.
   *
   * @param request the request
   * @param response the response
   * @return the response entity
   */
  @Override
  @GetMapping(value = { "/list", "" })
  public ResponseEntity<Map<String, Object>> list(HttpServletRequest request,
      HttpServletResponse response) {
    Map<String, Object> responseMap = new HashMap<>();
    Map<String, String[]> paramMap = new HashMap<>(request.getParameterMap());
    PagedList pagedList = interfaceConfigService.getInterfacesDetails(paramMap);
    responseMap.put("pagedList", pagedList);
    return new ResponseEntity<>(responseMap, HttpStatus.OK);

  }
}
