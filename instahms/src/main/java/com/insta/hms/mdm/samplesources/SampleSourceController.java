package com.insta.hms.mdm.samplesources;

import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterController;
import com.insta.hms.mdm.MasterResponseRouter;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

@Controller("sampleSourceController")
@RequestMapping(URLRoute.SAMPLE_SOURCE_MASTER)
public class SampleSourceController extends MasterController {
  public SampleSourceController(SampleSourceService sampleSourceService) {
    super(sampleSourceService, MasterResponseRouter.SAMPLE_SOURCE_ROUTER);
  }

  @Override
  protected Map<String, List<BasicDynaBean>> getReferenceLists(Map params) {
    return ((SampleSourceService) getService()).getAddEditPageData();
  }
}
