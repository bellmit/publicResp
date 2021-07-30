package com.insta.hms.mdm.insuranceplans;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterDetailsController;
import com.insta.hms.mdm.MasterResponseRouter;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class InsurancePlanController.
 */
@RequestMapping(URLRoute.INSURANCE_PLANS_PATH)
public class InsurancePlanController extends MasterDetailsController {

  /**
   * Instantiates a new insurance plan controller.
   *
   * @param service the service
   */
  public InsurancePlanController(InsurancePlanService service) {
    super(service, MasterResponseRouter.INSURANCE_PLANS_ROUTER);
  }

  /* (non-Javadoc)
   * @see com.insta.hms.mdm.MasterController#getReferenceLists(java.util.Map)
   */
  @SuppressWarnings("rawtypes")
  @Override
  protected Map<String, List<BasicDynaBean>> getReferenceLists(Map params) {
    return ((InsurancePlanService) getService()).getAddShowPageData(params);
  }

  /**
   * Gets the insurance category mapped to insurance company.
   *
   * @param insuranceCoId the insurance co id
   * @return the insu cat mapped to insu comp
   */
  @GetMapping(value = "/getcategories")
  public Map<String, Object> getInsuCatMappedToInsuComp(
      @RequestParam(name = "insurance_co_id") String insuranceCoId) {
    Map<String, Object> map = new HashMap<>();
    List<BasicDynaBean> listBean = ((InsurancePlanService) getService())
        .getInsuCatMappedToInsuComp(insuranceCoId);
    map.put("chargeBean", ConversionUtils.listBeanToListMap(listBean));
    return map;
  }

  /**
   * Get plan list by categoryId and insuranceCompanyId.
   *
   * @param categoryId         unique identifier of category
   * @param insuranceCompanyId unique identifier of insurance company
   * @return list of plan
   */
  @GetMapping(value = URLRoute.PLAN_LIST)
  public ResponseEntity<Map<String, Object>> getPlansByCategoryId(Integer categoryId,
                                                                  String insuranceCompanyId) {
    Map<String, Object> responseBody = ((InsurancePlanService) getService())
        .getPlansByCategory(categoryId, insuranceCompanyId);
    return new ResponseEntity<>(responseBody, HttpStatus.OK);
  }

  /**
   * Gets the plans by category and company id.
   *
   * @param categoryId the category id
   * @param insuranceCompanyId the insurance company id
   * @return the plans by category and company id
   */
  @GetMapping(value = URLRoute.PLAN_LIST_BY_INSCO_CAT)
  public ResponseEntity<Map<String, Object>> getPlansByCategoryAndCompanyId(
      @RequestParam(name = "category_id") Integer categoryId,
      @RequestParam(name = "insurance_co_id") String insuranceCompanyId,
      @RequestParam(name = "sponsor_id") String sponsorId) {
    Map<String, Object> responseBody = ((InsurancePlanService) getService())
        .getPlansByCategoryAndSponsor(categoryId, insuranceCompanyId, sponsorId);
    return new ResponseEntity<>(responseBody, HttpStatus.OK);
  }

  /**
   * Get category list by insuranceCompanyId.
   *
   * @param insuranceCompanyId unique identifier of insurance company
   * @return list of category
   */
  @GetMapping(value = "/categoryList")
  public ResponseEntity<Map<String, Object>> getCategoriesByCompanyId(
      @RequestParam String insuranceCompanyId,
      @RequestParam(required = false) String sponsorId) {
    Map<String, Object> responseBody = ((InsurancePlanService) getService())
        .getCategoriesByCompanyId(insuranceCompanyId, sponsorId);
    return new ResponseEntity<>(responseBody, HttpStatus.OK);
  }

  /**
   * Gets the plan list by categories.
   *
   * @param categoryIds
   *          the category ids
   * @return the plan list by categories
   */
  @GetMapping(value = "/planListByCategoriesAndSponsor")
  public ResponseEntity<Map<String, Object>> getPlanListByCategories(
      @RequestParam(name = "categoryList") String[] categoryIds,
      @RequestParam(name = "sponsorId", required = false) String sponsorId,
      @RequestParam(name = "searchQuery", required = false) String searchQuery,
      @RequestParam(name = "limit", required = false) Integer limit) {
    Map<String, Object> responseBody = ((InsurancePlanService) getService())
        .getPlansByCategoryIdsAndSponsor(categoryIds, sponsorId, searchQuery, limit);
    return new ResponseEntity<>(responseBody, HttpStatus.OK);
  }

  /**
   * Gets the plan type list by sponsor.
   *
   * @param sponsorId
   *          the sponsor id
   * @return the plan type list by sponsor
   */
  @GetMapping(value = "/planTypeListBySponsor")
  public ResponseEntity<Map<String, Object>> getPlanTypeListBySponsor(
      @RequestParam(name = "sponsorId") String sponsorId) {
    Map<String, Object> responseBody = ((InsurancePlanService) getService())
        .getPlanTypeListBySponsor(sponsorId);
    return new ResponseEntity<>(responseBody, HttpStatus.OK);
  }

}
