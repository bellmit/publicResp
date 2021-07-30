package com.insta.hms.mdm.billlabel;

import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterController;
import com.insta.hms.mdm.MasterResponseRouter;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping(URLRoute.BILL_LABEL_PATH)
public class BillLabelController extends MasterController {

  public BillLabelController(BillLabelService service) {
    super(service, MasterResponseRouter.BILL_LABEL_ROUTER);
  }

  @Override
  protected Map<String, List<BasicDynaBean>> getReferenceLists(Map params) {
    return ((BillLabelService) getService()).getAddEditPageData();
  }

}
