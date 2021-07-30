package com.insta.hms.mdm.sequences;

import com.insta.hms.mdm.MasterController;
import com.insta.hms.mdm.MasterResponseRouter;

import org.apache.commons.beanutils.BasicDynaBean;

import java.util.List;
import java.util.Map;

/**
 * The Class SequenceMasterController.
 */
public class SequenceMasterController extends MasterController {

  /**
   * Instantiates a new sequence master controller.
   *
   * @param service the service
   * @param router the router
   */
  public SequenceMasterController(SequenceMasterService service, MasterResponseRouter router) {
    super(service, router);
  }

  /**
   * Gets the map page data.
   *
   * @param params the params
   * @return the Map page data
   */
  @Override
  protected Map<String, List<BasicDynaBean>> getFilterLookupLists(Map params) {
    return ((SequenceMasterService) getService()).getListPageData(params);
  }

  /**
   * Gets the map page data.
   *
   * @param params the params
   * @return the Map page data
   */
  @Override
  protected Map<String, List<BasicDynaBean>> getReferenceLists(Map params) {
    return ((SequenceMasterService) getService()).getHospitalIdPatternList(params);
  }
}
