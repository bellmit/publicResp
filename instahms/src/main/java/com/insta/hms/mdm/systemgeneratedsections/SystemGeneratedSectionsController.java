package com.insta.hms.mdm.systemgeneratedsections;

import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterController;
import com.insta.hms.mdm.MasterResponseRouter;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

/**
 * The Class SystemGeneratedSectionsController.
 */
@Controller
@RequestMapping(URLRoute.SYS_GEN_SEC_PATH)
public class SystemGeneratedSectionsController extends MasterController {
  public SystemGeneratedSectionsController(SystemGeneratedSectionsService service) {
    super(service, MasterResponseRouter.SYS_GEN_SEC_PATH);
  }

  @SuppressWarnings("rawtypes")
  @Override
  public Map<String, List<BasicDynaBean>> getFilterLookupLists(Map params) {
    return ((SystemGeneratedSectionsService) getService()).getSectionsdata(params);
  }

  @Override
  public Map<String, List<BasicDynaBean>> getReferenceLists(Map params) {
    return ((SystemGeneratedSectionsService) getService()).getAddEditPageData();

  }
}
