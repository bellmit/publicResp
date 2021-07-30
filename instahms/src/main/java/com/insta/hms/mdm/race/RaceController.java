package com.insta.hms.mdm.race;

import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterController;
import com.insta.hms.mdm.MasterResponseRouter;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller("raceController")
@RequestMapping(URLRoute.RACE_MASTER_PATH)
public class RaceController extends MasterController {

  public RaceController(RaceService service) {
    super(service, MasterResponseRouter.RACE_ROUTER);
  }

}
