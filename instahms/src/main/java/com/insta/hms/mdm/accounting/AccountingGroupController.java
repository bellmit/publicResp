package com.insta.hms.mdm.accounting;

import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterController;
import com.insta.hms.mdm.MasterResponseRouter;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping(URLRoute.ACCOUNTING_GROUP_PATH)
public class AccountingGroupController extends MasterController {
  public AccountingGroupController(AccountingGroupService service) {
    super(service, MasterResponseRouter.ACCOUNTING_GROUP_ROUTER);
  }

  @SuppressWarnings("rawtypes")
  @Override
  protected Map<String, List<BasicDynaBean>> getReferenceLists(Map params) {
    return ((AccountingGroupService) getService()).getRecord();
  }
}
