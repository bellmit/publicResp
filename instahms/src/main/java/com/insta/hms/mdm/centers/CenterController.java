package com.insta.hms.mdm.centers;

import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterController;
import com.insta.hms.mdm.MasterResponseRouter;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The Class CenterController.
 *
 * @author yashwant
 */

@Controller
@RequestMapping(URLRoute.CENTER_PATH)
public class CenterController extends MasterController {

  /** The center service. */
  private CenterService centerService = (CenterService) getService();

  /**
   * Instantiates a new center controller.
   *
   * @param service the service
   */
  public CenterController(CenterService service) {
    super(service, MasterResponseRouter.CENTER_ROUTER);
  }

  /* @see com.insta.hms.mdm.MasterController#getReferenceLists(java.util.Map)
   */
  @SuppressWarnings("rawtypes")
  @Override
  protected Map<String, List<BasicDynaBean>> getReferenceLists(Map params) {

    return centerService.getAddEditPageData(params);
  }

  /**
   * Endpoint to get the name of the billing counter mapped to a user for a center.
   *
   * @param userName emp user name of the user
   * @param centerId center id
   * @return name of the mapped billing counter
   */
  @IgnoreConfidentialFilters
  @GetMapping("/billingCounter")
  public ModelMap getBillingCounter(@RequestParam("user_name") String userName,
      @RequestParam(value = "center_id") Integer centerId
  ) {
    ModelMap modelMap = new ModelMap();
    modelMap.addAttribute("counter_name", 
        centerService.getMappedBillingCounter(userName, centerId));
    return modelMap;
  }

}
