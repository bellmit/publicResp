package com.insta.hms.mdm.departments;

import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterController;
import com.insta.hms.mdm.MasterResponseRouter;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping(URLRoute.DEPARTMENT_PATH)
public class DepartmentController extends MasterController {

  public DepartmentController(DepartmentService service) {
    super(service, MasterResponseRouter.DEPARTMENT_ROUTER);
  }

  @SuppressWarnings("rawtypes")
  @Override
  protected Map<String, List<BasicDynaBean>> getReferenceLists(Map params) {
    return ((DepartmentService) getService()).getAddEditPageData(params);
  }

  @SuppressWarnings("rawtypes")
  @Override
  protected Map<String, List<BasicDynaBean>> getFilterLookupLists(Map params) {
    return ((DepartmentService) getService()).getListPageData(params);
  }
}
