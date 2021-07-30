package com.insta.hms.mdm.prescriptionsdeclinedreasonmaster;

import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterRestController;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping(URLRoute.PRESCRIPTION_DECLINE_REASON_MATER)
public class PrescDeclinedReasonController extends MasterRestController {
  public PrescDeclinedReasonController(PrescDeclinedReasonService service) {
    super(service);
  }

  @GetMapping(URLRoute.MASTER_INDEX_URL)
  public ModelAndView getReviewTypesIndexPage() {
    return renderMasterUi("Master", "hospitalAdminMasters");
  }
  
}
