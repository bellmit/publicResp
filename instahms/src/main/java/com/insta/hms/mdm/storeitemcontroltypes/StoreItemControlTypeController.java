package com.insta.hms.mdm.storeitemcontroltypes;

import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterController;
import com.insta.hms.mdm.MasterResponseRouter;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

/**
 * The Class StoreItemControlTypeController.
 */
@Controller
@RequestMapping(URLRoute.STORE_ITEM_CONTROL_TYPE_PATH)
public class StoreItemControlTypeController extends MasterController {

  /**
   * Instantiates a new store item control type controller.
   *
   * @param service the service
   */
  public StoreItemControlTypeController(StoreItemControlTypeService service) {
    super(service, MasterResponseRouter.STORE_ITEM_CONTROL_TYPE_ROUTER);
  }

  /* (non-Javadoc)
   * @see com.insta.hms.mdm.MasterController#getReferenceLists(java.util.Map)
   */
  @Override
  protected Map<String, List<BasicDynaBean>> getReferenceLists(Map params) {
    return ((StoreItemControlTypeService) getService()).getAddEditPageData();
  }

}
