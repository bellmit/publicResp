package com.insta.hms.mdm.samplecollectioncenters;

import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterController;
import com.insta.hms.mdm.MasterResponseRouter;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping(URLRoute.SAMPLE_COLLECTION_CENTER)
public class SampleCollectionCenterController extends MasterController {

  public SampleCollectionCenterController(SampleCollectionCenterService service) {
    super(service, MasterResponseRouter.SAMPLE_COLLECTION_CENTER_ROUTER);
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Override
  protected Map<String, List<BasicDynaBean>> getFilterLookupLists(Map params) {
    return ((SampleCollectionCenterService) getService()).getListPageData(params);
  }

  @SuppressWarnings("rawtypes")
  @Override
  protected Map<String, List<BasicDynaBean>> getReferenceLists(Map params) {
    return ((SampleCollectionCenterService) getService()).getAddEditPageData();
  }
}
