package com.insta.hms.mdm.issueuser;

/*
 * Owner : Ashok Pal, 7th Aug 2017
 */
import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterController;
import com.insta.hms.mdm.MasterResponseRouter;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class IssueUserController.
 */
@Controller
@RequestMapping(URLRoute.ISSUE_USER_MASTER_PATH)
public class IssueUserController extends MasterController {

  /**
   * Instantiates a new issue user controller.
   *
   * @param service the service
   */
  public IssueUserController(IssueUserService service) {
    super(service, MasterResponseRouter.ISSUE_USER_ROUTER);
  }

 
  @Override
  public Map<String, List<BasicDynaBean>> getFilterLookupLists(Map params) {
    Map<String, List<BasicDynaBean>> refIssueUserMap = new HashMap<String, List<BasicDynaBean>>();
    refIssueUserMap.put("lookupListMap", ((IssueUserService) getService()).lookup(false));
    return refIssueUserMap;
  }

  
  @Override
  protected Map<String, List<BasicDynaBean>> getReferenceLists(Map params) {
    return ((IssueUserService) getService()).getAddEditPageData();
  }

}
