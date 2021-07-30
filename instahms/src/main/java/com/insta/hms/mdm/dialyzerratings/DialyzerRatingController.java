package com.insta.hms.mdm.dialyzerratings;

import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterController;
import com.insta.hms.mdm.MasterResponseRouter;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(URLRoute.DIALYZER_RATINGS_PATH)
public class DialyzerRatingController extends MasterController {

  public DialyzerRatingController(DialyzerRatingService service) {
    super(service, MasterResponseRouter.DIALYZER_RATINGS_ROUTER);
  }

}