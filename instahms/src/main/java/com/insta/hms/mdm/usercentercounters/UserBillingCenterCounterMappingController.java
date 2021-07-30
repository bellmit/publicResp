package com.insta.hms.mdm.usercentercounters;

import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterDetailsController;
import com.insta.hms.mdm.MasterResponseRouter;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

@RequestMapping(URLRoute.BILLING_COUNTER_MAPPING_TO_CENTER)
public class UserBillingCenterCounterMappingController extends MasterDetailsController {

  public UserBillingCenterCounterMappingController(UserBillingCenterCounterMappingService service) {
    super(service, MasterResponseRouter.COUNTER_CENTER_MAPPING_ROUTER);
  }
  
  /* (non-Javadoc)
   * @see com.insta.hms.mdm.MasterController#getReferenceLists(java.util.Map)
   */
  @SuppressWarnings("rawtypes")
  @Override
  protected Map<String, List<BasicDynaBean>> getReferenceLists(Map params) {
    return ((UserBillingCenterCounterMappingService) getService()).getAddShowPageData(params);
  }
  
  /**
   * Lookup.
   *
   * @param request
   *          the request
   * @return the model map
   */
  @RequestMapping(value = "/lookup", method = RequestMethod.GET)
  public ModelMap lookup(HttpServletRequest request) {

    Map<String, String[]> parameters = request.getParameterMap();
    List<String> filterSet = ((UserBillingCenterCounterMappingService) getService())
            .lookupUsers(parameters);
    ModelMap modelMap = new ModelMap();
    modelMap.addAttribute("dtoList", filterSet);
    modelMap.addAttribute("listSize", filterSet.size());
    return modelMap;
  }

}
