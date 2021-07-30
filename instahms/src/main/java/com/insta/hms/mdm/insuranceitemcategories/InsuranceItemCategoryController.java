package com.insta.hms.mdm.insuranceitemcategories;

import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterController;
import com.insta.hms.mdm.MasterResponseRouter;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class InsuranceItemCategoryController.
 */
//@Controller
@RequestMapping(URLRoute.INSURANCE_ITEM_CATEGORY_MASTER_PATH)
public class InsuranceItemCategoryController extends MasterController {

  /**
   * Instantiates a new insurance item category controller.
   *
   * @param service the service
   */
  public InsuranceItemCategoryController(InsuranceItemCategoryService service) {
    super(service, MasterResponseRouter.INSURANCE_ITEM_CATEGORY_ROUTER);
  }

  /* (non-Javadoc)
   * @see com.insta.hms.mdm.MasterController#getReferenceLists(java.util.Map)
   */
  @Override
  protected Map<String, List<BasicDynaBean>> getReferenceLists(Map params) {
    return ((InsuranceItemCategoryService) getService()).getAddEditPageData();
  }
}
