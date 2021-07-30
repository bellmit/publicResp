package com.insta.hms.mdm.dentalsupplies;

import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterController;
import com.insta.hms.mdm.MasterResponseRouter;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;


/**
 * The Class is for Dental Supplies Master.
 *
 */
@Controller
@RequestMapping(URLRoute.DENTAL_SUPPLIES_MASTER)
public class DentalSuppliesController extends MasterController {

  public DentalSuppliesController(DentalSuppliesService service) {
    super(service, MasterResponseRouter.DENTAL_SUPPLIES_MASTER_ROUTER);
  }

  @SuppressWarnings("rawtypes")
  @Override
  protected Map<String, List<BasicDynaBean>> getFilterLookupLists(Map params) {
    return ((DentalSuppliesService) getService()).getListPageData();
  }

  @SuppressWarnings("rawtypes")
  @Override
  protected Map<String, List<BasicDynaBean>> getReferenceLists(Map params) {
    return ((DentalSuppliesService) getService()).getAddEditPageData();
  }
}
