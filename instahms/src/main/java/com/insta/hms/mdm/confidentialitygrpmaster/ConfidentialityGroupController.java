package com.insta.hms.mdm.confidentialitygrpmaster;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.PagedList;
import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterDetailsRestController;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping(URLRoute.CONFIDENTIALITY_GROUPS_PATH)
public class ConfidentialityGroupController extends MasterDetailsRestController {

  public ConfidentialityGroupController(ConfidentialityGroupService service) {
    super(service);
  }

  @SuppressWarnings("unchecked")
  @RequestMapping(value = "/allUsers", method = RequestMethod.GET)
  public List<String> getAllUsers() {
    return ((ConfidentialityGroupService) this.getService()).getAllUsers();
  }

  @GetMapping(value = "/index")
  public ModelAndView getGeneralMastersIndexPage() {
    return renderMasterUi("Master", "generalMasters");
  }

  /**
   * Gets the users mapped with a confidential group.
   *
   * @param id
   *          the id
   * @return the confidentiality group users
   */
  @RequestMapping(value = "/groupUsers", method = RequestMethod.GET)
  public ResponseEntity<Map<String, Object>> getConfidentialityGroupUsers(@RequestParam int id) {
    Map<String, Object> responseMap = new HashMap<String, Object>();
    responseMap.put("users", ConversionUtils.copyListDynaBeansToMap(
        ((ConfidentialityGroupService) this.getService()).getConfidentialityGroupUsers(id)));
    return new ResponseEntity<Map<String, Object>>(responseMap, HttpStatus.OK);
  }

  /**
   * Filtered List.
   *
   * @param request
   *          the request
   * @return the response entity
   */
  @RequestMapping(value = { "/filteredlist" }, method = RequestMethod.GET)
  public ResponseEntity<Map<String, Object>> filteredList(HttpServletRequest request) {
    Map<String, Object> responseMap = new HashMap<String, Object>();
    Map<String, String[]> parameters = request.getParameterMap();
    String filterText = (null != parameters && parameters.containsKey("filterText"))
        ? parameters.get("filterText")[0] : "";
    PagedList pagedList = ((ConfidentialityGroupService) this.getService())
        .filterOnNameAndAbbreviation(parameters, filterText);
    responseMap.put("paged_list", pagedList);
    return new ResponseEntity<Map<String, Object>>(responseMap, HttpStatus.OK);
  }

}
