package com.insta.hms.mdm.genericsubclassifications;

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
 * 
 * @author irshadmohammed.
 *
 */
@Controller
@RequestMapping(URLRoute.GENERIC_SUB_CLASSIFICATION_PATH)
public class GenericSubClassificationController extends MasterController {

  public GenericSubClassificationController(GenericSubClassificationService service) {
    super(service, MasterResponseRouter.GENERIC_SUB_CLASSIFICATION_ROUTER);
  }

  @Override
  public Map<String, List<BasicDynaBean>> getReferenceLists(Map params) {
    Map<String, List<BasicDynaBean>> refGenericClassificationMap = 
        new HashMap<String, List<BasicDynaBean>>();
    refGenericClassificationMap.put("genericSubClassificationsLists", getService().lookup(false));
    refGenericClassificationMap.put("classificationdetails",
        ((GenericSubClassificationService) getService()).getClassificationdetails());
    return refGenericClassificationMap;
  }
}
