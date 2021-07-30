package com.insta.hms.mdm.storetypes;

import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterController;
import com.insta.hms.mdm.MasterResponseRouter;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping(URLRoute.STORE_TYPE_PATH)
public class StoreTypeController extends MasterController {

  public StoreTypeController(StoreTypeService service) {
    super(service, MasterResponseRouter.STORE_TYPE_ROUTER);
  }

  @SuppressWarnings("rawtypes")
  @Override
  protected Map<String, List<BasicDynaBean>> getReferenceLists(Map params) {
    Map<String, List<BasicDynaBean>> referenceMap = new HashMap<String, List<BasicDynaBean>>();
    referenceMap.put("stortypedetails", getService().lookup(false));
    return referenceMap;
  }
}
