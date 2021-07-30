package com.insta.hms.mdm.dialyzertypes;

import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterController;
import com.insta.hms.mdm.MasterResponseRouter;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * The Class DialyzerTypeController.
 */
@Controller
@RequestMapping(URLRoute.DIALYSIS_MASTER_PATH)
public class DialyzerTypeController extends MasterController {

  /**
   * Instantiates a new dialyzer type controller.
   *
   * @param service the service
   */
  public DialyzerTypeController(DialyzerTypeService service) {
    super(service, MasterResponseRouter.DIALYSIS_MASTER_ROUTER);
  }

}
