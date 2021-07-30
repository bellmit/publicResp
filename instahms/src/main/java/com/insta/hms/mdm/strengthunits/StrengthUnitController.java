package com.insta.hms.mdm.strengthunits;

import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterController;
import com.insta.hms.mdm.MasterResponseRouter;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

/**
 * The Class StrengthUnitController.
 *
 * @author sainathbatthala The controller for Strength Unit Master
 */
@Controller
@RequestMapping(URLRoute.STRENGTH_UNIT_MASTER)
public class StrengthUnitController extends MasterController {

  /**
   * Instantiates a new strength unit controller.
   *
   * @param service the service
   */
  public StrengthUnitController(StrengthUnitService service) {
    super(service, MasterResponseRouter.STRENGTH_UNIT_MASTER);
  }

  /**
   * This method adds extra request attribute to add/show page.
   *
   * @param params the params
   * @return the reference lists
   */
  @Override
  protected Map<String, List<BasicDynaBean>> getReferenceLists(Map params) {
    return ((StrengthUnitService) getService()).getAddEditPageData(params);
  }

  /**
   * This method adds extra request attribute to list page.
   *
   * @param params the params
   * @return the filter lookup lists
   */
  @Override
  protected Map<String, List<BasicDynaBean>> getFilterLookupLists(Map params) {
    return ((StrengthUnitService) getService()).getAddEditPageData(params);
  }

}
