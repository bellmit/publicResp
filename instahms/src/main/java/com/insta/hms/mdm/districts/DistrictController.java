package com.insta.hms.mdm.districts;

import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterController;
import com.insta.hms.mdm.MasterResponseRouter;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

@Controller("districtController")
@RequestMapping(URLRoute.DISTRICT_MASTER_PATH)
public class DistrictController extends MasterController {

  public DistrictController(DistrictService service) {
    super(service, MasterResponseRouter.DISTRICT_MASTER_ROUTER);
  }

  @SuppressWarnings("rawtypes")
  @Override
  protected Map<String, List<BasicDynaBean>> getReferenceLists(Map params) {
    return ((DistrictService) getService()).getAddEditPageData(params);
  }
}
