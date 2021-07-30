package com.insta.hms.mdm.gendermaster;

import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterController;
import com.insta.hms.mdm.MasterResponseRouter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller("genderController")
@RequestMapping(URLRoute.GENDER_MASTER)
public class GenderController extends MasterController {

  /**
   * Instantiates a new master controller.
   *
   * @param genderMasterService the service
   */
  public GenderController(GenderService genderMasterService) {
    super(genderMasterService, MasterResponseRouter.GENDER_ROUTER);
  }
}
