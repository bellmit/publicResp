package com.insta.hms.mdm.referraldoctors;

import com.insta.hms.common.BaseController;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.master.URLRoute;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

@Controller("referralDoctorController")
@RequestMapping(URLRoute.REFERRAL_DOCTOR_MASTER_PATH)
public class ReferralDoctorController extends BaseController {

  // TODO: This doesn't extend MasterController because this is not fully
  // migrated.It needs to extend MasterController when migrating.
  // There is no action_id in insta-master-config.xml .
  // Remove the auth passthru and replace it with correct action_id.
  // Remove this lookup after migrating the code.

  @LazyAutowired private ReferralDoctorService service;

  /**
   * Lookup.
   *
   * @param request the request
   * @return the model map
   */
  @RequestMapping(value = "/lookup", method = RequestMethod.GET)
  public ModelMap lookup(HttpServletRequest request) {
    Map<String, String[]> parameters = request.getParameterMap();
    String filterText =
        (null != parameters && parameters.containsKey("filterText"))
            ? parameters.get("filterText")[0]
            : null;
    List<BasicDynaBean> searchSet =
        (null != filterText) ? service.autocomplete(filterText, parameters) : service.lookup(true);
    ModelMap modelMap = new ModelMap();
    modelMap.addAttribute("dtoList", ConversionUtils.listBeanToListMap(searchSet));
    modelMap.addAttribute("listSize", searchSet.size());
    return modelMap;
  }
}
