package com.insta.hms.mdm.dialysisaccesstypes;

import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterController;
import com.insta.hms.mdm.MasterResponseRouter;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * The Class DialysisAccessTypeController.
 */
@Controller
@RequestMapping(URLRoute.DIALYSIS_ACCESSS_TYPES_PATH)
public class DialysisAccessTypeController extends MasterController {

  /**
   * Instantiates a new dialysis access type controller.
   *
   * @param dialysisAccessTypesService the dialysis access types service
   */
  public DialysisAccessTypeController(DialysisAccessTypeService dialysisAccessTypesService) {
    super(dialysisAccessTypesService, MasterResponseRouter.DIALYSIS_ACCESS_TYPES_ROUTER);
  }

}
