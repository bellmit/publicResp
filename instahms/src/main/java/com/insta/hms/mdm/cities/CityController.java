package com.insta.hms.mdm.cities;

import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterController;
import com.insta.hms.mdm.MasterResponseRouter;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

@Controller("cityController")
@RequestMapping(URLRoute.CITY_MASTER_PATH)
public class CityController extends MasterController {

  public CityController(CityService service) {
    super(service, MasterResponseRouter.CITY_MASTER_ROUTER);
  }

  @SuppressWarnings("rawtypes")
  @Override
  protected Map<String, List<BasicDynaBean>> getReferenceLists(Map params) {
    return ((CityService) getService()).getAddEditPageData(params);
  }
}
