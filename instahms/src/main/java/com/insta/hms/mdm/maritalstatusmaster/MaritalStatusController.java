package com.insta.hms.mdm.maritalstatusmaster;

import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterController;
import com.insta.hms.mdm.MasterResponseRouter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller("maritalStatusController")
@RequestMapping(URLRoute.MARITAL_STATUS_MASTER)
public class MaritalStatusController extends MasterController {


  /**
   * Instantiates a new master controller.
   *
   * @param maritalStatusService the Martial status master service class
   */
  public MaritalStatusController(MaritalStatusService maritalStatusService) {
    super(maritalStatusService, MasterResponseRouter.MARTIAL_STATUS_ROUTER);
  }
}
