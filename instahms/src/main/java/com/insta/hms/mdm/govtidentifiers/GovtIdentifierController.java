package com.insta.hms.mdm.govtidentifiers;

import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterController;
import com.insta.hms.mdm.MasterResponseRouter;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping(URLRoute.GOVT_IDENTIFIER_MASTER_PATH)
public class GovtIdentifierController extends MasterController {

  public GovtIdentifierController(GovtIdentifierService service) {
    super(service, MasterResponseRouter.GOVT_IDENTIFIER_MASTER_ROUTER);
  }

  @Override
  protected Map<String, List<BasicDynaBean>> getReferenceLists(Map params) {
    return ((GovtIdentifierService) getService()).getAddEditPageData();
  }
}
