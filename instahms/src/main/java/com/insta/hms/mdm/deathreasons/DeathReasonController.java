package com.insta.hms.mdm.deathreasons;

import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterController;
import com.insta.hms.mdm.MasterResponseRouter;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * The Class {@link DeathReasonController}.
 * @author deepak_kk
 *
 */
@Controller
@RequestMapping(URLRoute.DEATH_REASON_PATH)
public class DeathReasonController extends MasterController {

  public DeathReasonController(DeathReasonService service) {
    super(service, MasterResponseRouter.DEATH_REASON_ROUTER);
  }
}
