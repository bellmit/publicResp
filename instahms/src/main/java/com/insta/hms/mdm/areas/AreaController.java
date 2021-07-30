package com.insta.hms.mdm.areas;

import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterController;
import com.insta.hms.mdm.MasterResponseRouter;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

@Controller("areaController")
@RequestMapping(URLRoute.AREA_MASTER_PATH)
public class AreaController extends MasterController {

  public AreaController(AreaService service) {
    super(service, MasterResponseRouter.AREA_MASTER_ROUTER);
  }

  @SuppressWarnings("rawtypes")
  @Override
  protected Map<String, List<BasicDynaBean>> getReferenceLists(Map params) {
    return ((AreaService) getService()).getAddEditPageData();
  }
}
