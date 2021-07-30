package com.insta.hms.mdm.storescontractor;

/*
 * Owner : Ashok Pal, 5th April 2017
 */
import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterController;
import com.insta.hms.mdm.MasterResponseRouter;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

/**
 * The Class ContractorController.
 */
@Controller
@RequestMapping(URLRoute.CONTRACTOR_MASTER_PATH)
public class ContractorController extends MasterController {

  /**
   * Instantiates a new contractor controller.
   *
   * @param service the service
   */
  public ContractorController(ContractorService service) {
    super(service, MasterResponseRouter.CONTRACTOR_MASTER_ROUTER);
  }

  /* (non-Javadoc)
   * @see com.insta.hms.mdm.MasterController#getReferenceLists(java.util.Map)
   */
  @Override
  protected Map<String, List<BasicDynaBean>> getReferenceLists(Map params) {
    return ((ContractorService) getService()).getAddEditPageData();
  }

}
