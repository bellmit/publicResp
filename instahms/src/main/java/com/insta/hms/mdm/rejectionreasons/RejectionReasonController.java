package com.insta.hms.mdm.rejectionreasons;

import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterController;
import com.insta.hms.mdm.MasterResponseRouter;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(URLRoute.REJECTION_REASON_PATH)
public class RejectionReasonController extends MasterController {

  public RejectionReasonController(RejectionReasonService service) {
    super(service, MasterResponseRouter.REJECTION_REASON_ROUTER);
  }

}
