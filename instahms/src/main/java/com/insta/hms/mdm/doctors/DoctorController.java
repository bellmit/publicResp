package com.insta.hms.mdm.doctors;

import com.insta.hms.common.BaseController;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.master.URLRoute;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

@Controller("doctorController")
@RequestMapping(URLRoute.DOCTOR_MASTER_PATH)
public class DoctorController extends BaseController {

  // TODO: This doesn't extend MasterController because this is not fully
  // migrated.It needs to extend MasterController when migrating.
  // There is no action_id in insta-master-config.xml .
  // Remove the auth passthru and replace it with correct action_id.
  // Remove this lookup after migrating the code.

  @LazyAutowired private DoctorService service;

  /**
   * lookup request for doctor.
   *
   * @param request HttpServletRequest
   * @return ModelMap
   */
  @RequestMapping(value = "/lookup", method = RequestMethod.GET)
  public ModelMap lookup(HttpServletRequest request) {
    Map<String, String[]> parameters = request.getParameterMap();
    String filterText =
        (null != parameters && parameters.containsKey("filterText"))
            ? parameters.get("filterText")[0]
            : null;

    String columnNameSearch =
        (null != parameters && parameters.containsKey("columnName"))
            ? parameters.get("columnName")[0]
            : null;
    if (columnNameSearch == null || columnNameSearch.equals("")) {
      columnNameSearch = "doctor_name";
    }

    List<BasicDynaBean> searchSet =
        (null != filterText)
            ? service.autocomplete(columnNameSearch, filterText, false, parameters)
            : service.lookup(true);

    ModelMap modelMap = new ModelMap();
    modelMap.addAttribute("dtoList", ConversionUtils.listBeanToListMap(searchSet));
    modelMap.addAttribute("listSize", searchSet.size());
    return modelMap;
  }
  
  /**
   * Get Doctor and ReferalDoctor list.
   * 
   * @param searchQuery the search string
   * @return list of map
   */
  @GetMapping(value = "/doctorAndReferralDoctorList")
  public List<Map<String, Object>> getDoctorAndReferalDoctor(
      @RequestParam(value = "search_query") String searchQuery) {
    return service.getDoctorAndReferalDoctor(searchQuery);
  }
}
