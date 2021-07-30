package com.insta.hms.mdm.consumptionuom;

import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterController;
import com.insta.hms.mdm.MasterResponseRouter;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

/**
 * Consumption UOM Controller.
 * 
 * @author VinayKumarJavalkar
 */
@Controller("consumptionUOMController")
@RequestMapping(URLRoute.CONSUMPTION_UOM_PATH)
public class ConsumptionUOMController extends MasterController {
  
  public ConsumptionUOMController(ConsumptionUOMService service) {
    super(service, MasterResponseRouter.CONSUMPTION_UOM_ROUTER);
    
  }
  
  /* (non-Javadoc)
   * @see com.insta.hms.mdm.MasterController#getReferenceLists(java.util.Map)
   */
  @Override
  protected Map<String, List<BasicDynaBean>> getReferenceLists(Map params) {
    return ((ConsumptionUOMService) getService()).getAddEditPageData();
  }
}
