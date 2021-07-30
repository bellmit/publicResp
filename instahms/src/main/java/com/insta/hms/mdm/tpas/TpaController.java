package com.insta.hms.mdm.tpas;

import com.insta.hms.common.BaseController;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.insuranceplans.InsurancePlanService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * The Class TpaController.
 */
@Controller("tpaController")
@RequestMapping(URLRoute.TPA_MASTER_PATH)
public class TpaController extends BaseController {

  private static final String LOOKUP = "/lookup";

  private static final String GET_SPONSOR_DETAILS = "/getsponsordetails";

  // TODO: This doesn't extend MasterController because this is not fully
  // migrated.It needs to extend MasterController when migrating.
  // There is no action_id in insta-master-config.xml .
  // Remove the auth passthru and replace it with correct action_id.

  private static final String GET_TPA_DETAILS = "/details";

  /** The service. */
  @LazyAutowired
  private TpaService service;

  @LazyAutowired
  private InsurancePlanService insurancePlanService;

  /**
   * Gets the sponsor.
   *
   * @param sponsorId the sponsor id
   * @return the sponsor
   */
  @GetMapping(value = GET_SPONSOR_DETAILS)
  public ModelMap getSponsor(@RequestParam(required = true, name = "sponsor_id") String sponsorId) {
    ModelMap modelMap = new ModelMap();
    BasicDynaBean tpaBean = service.getDetails(sponsorId);
    return modelMap.addAttribute("sponsor_details", tpaBean == null ? null : tpaBean.getMap());
  }

  /**
   * Lookup.
   *
   * @param request the request
   * @return the model map
   */
  @GetMapping(value = LOOKUP)
  public ModelMap lookup(HttpServletRequest request) {
    Map<String, String[]> parameters = request.getParameterMap();
    String filterText = (null != parameters && parameters.containsKey("filterText"))
        ? parameters.get("filterText")[0] : null;
    List<BasicDynaBean> searchSet = service.autocomplete(filterText, parameters);
    ModelMap modelMap = new ModelMap();
    modelMap.addAttribute("dtoList", ConversionUtils.listBeanToListMap(searchSet));
    modelMap.addAttribute("listSize", searchSet.size());
    return modelMap;
  }

  /**
   * Get TPA Details from plan code.
   * @param planCode the plan code associated with TPA
   * @param categoryId the patient category ID
   * @param visitType - OP/IP
   * @return the model map
   */
  @GetMapping(value = GET_TPA_DETAILS)
  public ModelMap getPlanDetails(@RequestParam(required = true, name = "plan_code") String planCode,
      @RequestParam(required = true, name = "category_id") Integer categoryId,
      @RequestParam(required = true, name = "visit_type") String visitType) {
    List<BasicDynaBean> searchSet =
        insurancePlanService.getTPADetailsFromCode(planCode, categoryId, visitType);
    ModelMap modelMap = new ModelMap();
    modelMap.addAttribute("dtoList", ConversionUtils.listBeanToListMap(searchSet));
    modelMap.addAttribute("listSize", searchSet.size());
    return modelMap;
  }

}
