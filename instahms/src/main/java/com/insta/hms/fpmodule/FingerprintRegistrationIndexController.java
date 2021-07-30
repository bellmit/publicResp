package com.insta.hms.fpmodule;

import com.insta.hms.common.BaseController;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/fingerprintRegistration")
public class FingerprintRegistrationIndexController extends BaseController {

  @GetMapping("/index")
  public ModelAndView getFingerprintIndexPage() {
    return renderFlowUi("Fingerprint", "fingerprint", "withFlow", "fingerprintFlow",
        "fingerprintRegistration", false);
  }

}
