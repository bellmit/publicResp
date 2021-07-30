package com.insta.hms.mdm.sections;

import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterController;
import com.insta.hms.mdm.MasterResponseRouter;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

/**
 * The Class SectionsController.
 */
@Controller
@RequestMapping(URLRoute.SECTIONS_PATH)
public class SectionsController extends MasterController {

  /**
   * Instantiates a new sections controller.
   *
   * @param service the service
   */
  public SectionsController(SectionsService service) {
    super(service, MasterResponseRouter.SECTIONS_ROUTER);
  }

  @Override
  protected Map<String, List<BasicDynaBean>> getReferenceLists(Map params) {
    return ((SectionsService) getService()).getReferenceLists(params);
  }

}
