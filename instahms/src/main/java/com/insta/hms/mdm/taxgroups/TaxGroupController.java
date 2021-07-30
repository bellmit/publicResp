package com.insta.hms.mdm.taxgroups;

import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterController;
import com.insta.hms.mdm.MasterResponseRouter;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

/**
 * The Class TaxGroupController.
 *
 * @author prasanna
 */

@Controller
@RequestMapping(URLRoute.ITEM_GROUP_PATH)
public class TaxGroupController extends MasterController {

  /**
   * Instantiates a new tax group controller.
   *
   * @param service
   *          the service
   */
  public TaxGroupController(TaxGroupService service) {
    super(service, MasterResponseRouter.ITEM_GROUP_ROUTER);
  }

  @Override
  protected Map<String, List<BasicDynaBean>> getReferenceLists(Map params) {
    return ((TaxGroupService) getService()).getAddEditPageData(params);
  }

}
