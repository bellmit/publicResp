package com.insta.hms.mdm.sponsors;

import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterController;
import com.insta.hms.mdm.MasterResponseRouter;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class SponsorTypeController.
 */
@Controller
@RequestMapping(URLRoute.SPONSOR_TYPE_PATH)
public class SponsorTypeController extends MasterController {
  
  /**
   * Instantiates a new sponsor type controller.
   *
   * @param service the service
   */
  public SponsorTypeController(SponsorTypeService service) {
    super(service, MasterResponseRouter.SPONSOR_TPYE_ROUTER);
  }

  /* (non-Javadoc)
   * @see com.insta.hms.mdm.MasterController#getFilterLookupLists(java.util.Map)
   */
  @Override
  protected Map<String, List<BasicDynaBean>> getFilterLookupLists(Map params) {
    Map<String, List<BasicDynaBean>> lookupMaps = new HashMap<String, List<BasicDynaBean>>();
    lookupMaps.put("sponsortypeList", ((SponsorTypeService) getService()).lookup(false));
    return lookupMaps;
  }

  /* (non-Javadoc)
   * @see com.insta.hms.mdm.MasterController#getReferenceLists(java.util.Map)
   */
  @SuppressWarnings("rawtypes")
  @Override
  protected Map<String, List<BasicDynaBean>> getReferenceLists(Map params) {
    return ((SponsorTypeService) getService()).getAddEditPageData(params);
  }
}
