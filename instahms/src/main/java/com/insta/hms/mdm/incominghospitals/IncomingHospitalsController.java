package com.insta.hms.mdm.incominghospitals;

import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterController;
import com.insta.hms.mdm.MasterResponseRouter;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

/**
 * The Class IncomingHospitalsController.
 */
@Controller
@RequestMapping(URLRoute.INCOMING_HOSPITALS)
public class IncomingHospitalsController extends MasterController {

  /**
   * Instantiates a new incoming hospitals controller.
   *
   * @param service the service
   */
  public IncomingHospitalsController(IncomingHospitalsService service) {
    super(service, MasterResponseRouter.INCOMING_HOSPITALS_ROUTER);
  }

  /* (non-Javadoc)
   * @see com.insta.hms.mdm.MasterController#getReferenceLists(java.util.Map)
   */
  @Override
  protected Map<String, List<BasicDynaBean>> getReferenceLists(Map params) {
    return ((IncomingHospitalsService) getService()).getAddEditPageData();
  }
}
