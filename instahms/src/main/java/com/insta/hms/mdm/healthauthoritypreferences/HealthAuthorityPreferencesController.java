package com.insta.hms.mdm.healthauthoritypreferences;

import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterController;
import com.insta.hms.mdm.MasterResponseRouter;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

/** The Class HealthAuthorityPreferencesController. */
@Controller
@RequestMapping(URLRoute.HEALTH_AUTH_PREFERENCES_PATH)
public class HealthAuthorityPreferencesController extends MasterController {

  /**
   * Instantiates a new health authority preferences controller.
   *
   * @param service the service
   */
  public HealthAuthorityPreferencesController(HealthAuthorityPreferencesService service) {
    super(service, MasterResponseRouter.HEALTH_AUTH_PREFERENCES_ROUTER);
  }

  /**
   * This method used to get ReferenceLists .
   *
   * @param params Map
   * @return Map
   */
  @Override
  protected Map<String, List<BasicDynaBean>> getReferenceLists(Map params) {
    return ((HealthAuthorityPreferencesService) getService()).getListPageData(params);
  }
}
