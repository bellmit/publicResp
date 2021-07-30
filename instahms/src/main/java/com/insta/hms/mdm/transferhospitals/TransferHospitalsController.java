package com.insta.hms.mdm.transferhospitals;

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
@RequestMapping(URLRoute.TRANSFER_HOSPITALS_PATH)
public class TransferHospitalsController extends MasterController {

  public TransferHospitalsController(TransferHospitalsService service) {
    super(service, MasterResponseRouter.TRANSFER_HOSPITALS_ROUTER);
  }

  @Override
  public Map<String, List<BasicDynaBean>> getReferenceLists(Map params) {
    Map<String, List<BasicDynaBean>> lookupMaps = new HashMap<String, List<BasicDynaBean>>();
    lookupMaps.put("listbean", ((TransferHospitalsService) getService()).lookup(false));
    return lookupMaps;
  }

}
