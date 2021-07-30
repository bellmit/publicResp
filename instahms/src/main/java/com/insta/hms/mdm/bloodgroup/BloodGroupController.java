package com.insta.hms.mdm.bloodgroup;

import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterController;
import com.insta.hms.mdm.MasterResponseRouter;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller("bloodGroupController")
@RequestMapping(URLRoute.Blood_GROUP_MASTER_PATH)
public class BloodGroupController extends MasterController {

  public BloodGroupController(BloodGroupService service) {
    super(service, MasterResponseRouter.BLOOD_GROUP_ROUTER);
  }

}
