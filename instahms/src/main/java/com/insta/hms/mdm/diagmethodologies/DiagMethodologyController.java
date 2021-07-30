/**
 * 
 */

package com.insta.hms.mdm.diagmethodologies;

import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterController;
import com.insta.hms.mdm.MasterResponseRouter;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

/**
 * The Class DiagMethodologyController.
 *
 * @author anup.v
 */

@Controller
@RequestMapping(URLRoute.DIAG_METHODOLOGY_MASTER_PATH)
public class DiagMethodologyController extends MasterController {

  /**
   * Instantiates a new diag methodology controller.
   *
   * @param service
   *          the service
   */
  public DiagMethodologyController(DiagMethodologyService service) {
    super(service, MasterResponseRouter.DIAG_METHODOLOGY_MASTER_ROUTER);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.mdm.MasterController#getReferenceLists(java.util.Map)
   */
  @Override
  protected Map<String, List<BasicDynaBean>> getReferenceLists(Map params) {
    return ((DiagMethodologyService) getService()).getAddEditPageData();
  }

}
