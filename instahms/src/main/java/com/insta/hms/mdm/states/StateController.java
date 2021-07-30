package com.insta.hms.mdm.states;

import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterController;
import com.insta.hms.mdm.MasterResponseRouter;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

@Controller("stateController")
@RequestMapping(URLRoute.STATE_MASTER_PATH)
public class StateController extends MasterController {

  public StateController(StateService service) {
    super(service, MasterResponseRouter.STATE_MASTER_ROUTER);
  }

  @SuppressWarnings("rawtypes")
  @Override
  protected Map<String, List<BasicDynaBean>> getReferenceLists(Map params) {
    return ((StateService) getService()).getAddEditPageData(params);
  }
}
