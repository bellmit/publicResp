package com.insta.hms.mdm.caserate;

import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterDetailsRestController;

import org.apache.commons.beanutils.DynaBeanMapDecorator;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping(URLRoute.CASE_RATE_MASTER)
public class CaseRateController extends MasterDetailsRestController {

  public CaseRateController(CaseRateService caseRateService) {
    super(caseRateService);
  }

  @GetMapping(URLRoute.MASTER_INDEX_URL)
  public ModelAndView getCaseRateIndexPage() {
    return renderMasterUi("Master", "hospitalBillingMasters");
  }

  /**
   * Find case rate based on filters.
   *
   * @param params filter map
   * @return map of case rate list
   */
  @GetMapping(value = URLRoute.FIND_BY_FILTER)
  public ResponseEntity<Map<String, Object>> findByFilters(
      @RequestParam Map<String, String> params) {
    Map<String, Object> responseBody = new HashMap<>();
    responseBody.putAll(((CaseRateService) getService())
        .findByFilters(params));
    return new ResponseEntity<>(responseBody, HttpStatus.OK);
  }

  /**
   * Fetch case rate details.
   *
   * @return map of case rate details
   */
  @Override
  @RequestMapping(value = "/show", method = RequestMethod.GET)
  public ResponseEntity<Map<String, Object>> show(HttpServletRequest req,
      HttpServletResponse resp) {
    ResponseEntity<Map<String, Object>> re = super.show(req, resp);
    Map<String, Object> responseBody = re.getBody();
    DynaBeanMapDecorator caseRate = (DynaBeanMapDecorator) responseBody.get("bean");
    Integer planId = (Integer) caseRate.get("plan_id");
    Map<String, Object> insuranceItemCategories = ((CaseRateService) getService())
        .getCategoryDetailsByPlanId(planId);
    responseBody.put("insuranceItemCategories", insuranceItemCategories.get("category_details"));
    return re;
  }
  
  /**
   * Get Insurance categories by plan id.
   *
   * @param planId unique identifier of insurance plan
   * @return list of categories by plan id
   */
  @GetMapping(value = URLRoute.CATEGORY_LIST)
  public ResponseEntity<Map<String, Object>> getCategoryDetailByPlanId(
      @RequestParam("planId") Integer planId) {
    Map<String, Object> responseBody = ((CaseRateService) getService())
        .getCategoryDetailsByPlanId(planId);
    return new ResponseEntity<>(responseBody, HttpStatus.OK);
  }

  @Override
  @RequestMapping(value = "/create", method = RequestMethod.POST, consumes = "application/json")
  protected ResponseEntity create(HttpServletRequest req, HttpServletResponse resp,
                                  @RequestBody ModelMap requestBody) {
    ResponseEntity responseBody = null;
    try {
      return super.create(req, resp, requestBody);
    } catch (DuplicateKeyException dke) {
      return new ResponseEntity<>(responseBody, HttpStatus.CONFLICT);
    }
  }
}
