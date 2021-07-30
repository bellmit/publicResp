package com.insta.hms.mdm.regularexpression;

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
@RequestMapping(URLRoute.REGULAR_EXPRESSION_PATH)
public class RegularExpressionController extends MasterController {

  public RegularExpressionController(RegularExpressionService service) {
    super(service, MasterResponseRouter.REGULAR_EXPRESSION_ROUTER);
  }

  @Override
  public Map<String, List<BasicDynaBean>> getFilterLookupLists(Map params) {
    Map<String, List<BasicDynaBean>> lookupMaps = new HashMap<String, List<BasicDynaBean>>();
    lookupMaps.put("RegExpList", ((RegularExpressionService) getService()).lookup(false));
    return lookupMaps;
  }

}
