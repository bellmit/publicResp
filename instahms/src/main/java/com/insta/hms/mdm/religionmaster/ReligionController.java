package com.insta.hms.mdm.religionmaster;

import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterController;
import com.insta.hms.mdm.MasterResponseRouter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller("religionController")
@RequestMapping(URLRoute.RELIGION_MASTER)
public class ReligionController extends MasterController {

  /**
   * Instantiates a new master controller.
   *
   * @param service the service
   */
  public ReligionController(ReligionService service) {
    super(service, MasterResponseRouter.RELIGION_ROUTER);
  }
}
