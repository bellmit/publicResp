package com.insta.hms.mdm.discountplans;

import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterDetailsController;
import com.insta.hms.mdm.MasterResponseRouter;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

/**
 * The Class DiscountPlanController.
 */
@Controller
@RequestMapping(URLRoute.DISCOUNT_PLAN_PATH)
public class DiscountPlanController extends MasterDetailsController {

  /**
   * Instantiates a new discount plan controller.
   *
   * @param service the service
   */
  public DiscountPlanController(DiscountPlanService service) {
    super(service, MasterResponseRouter.DISCOUNT_PLAN_ROUTER);
  }

  /* (non-Javadoc)
   * @see com.insta.hms.mdm.MasterController#getFilterLookupLists(java.util.Map)
   */
  @SuppressWarnings("rawtypes")
  @Override
  protected Map<String, List<BasicDynaBean>> getFilterLookupLists(Map params) {
    return ((DiscountPlanService) getService()).getListPageLookup(params);
  }

  /* (non-Javadoc)
   * @see com.insta.hms.mdm.MasterController#getReferenceLists(java.util.Map)
   */
  @SuppressWarnings("rawtypes")
  @Override
  protected Map<String, List<BasicDynaBean>> getReferenceLists(Map params) {
    return ((DiscountPlanService) getService()).getAddShowPageData(params);
  }

}
