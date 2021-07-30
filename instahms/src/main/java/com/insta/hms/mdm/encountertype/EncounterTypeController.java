package com.insta.hms.mdm.encountertype;

import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterController;
import com.insta.hms.mdm.MasterResponseRouter;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

/**
 * The Class EncounterTypeController.
 *
 * @author deepak_kk
 */
@Controller
@RequestMapping(URLRoute.ENCOUNTER_TYPE_PATH)
public class EncounterTypeController extends MasterController {

  public EncounterTypeController(EncounterTypeService service) {
    super(service, MasterResponseRouter.ENCOUNTER_TYPE_ROUTER);
  }

  @Override
  protected Map<String, List<BasicDynaBean>> getReferenceLists(Map params) {
    return ((EncounterTypeService) getService()).getAddEditPageData();
  }
}
