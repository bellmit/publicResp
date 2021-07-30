package com.insta.hms.fpmodule;

import com.insta.hms.common.BaseController;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/fingerprintVerification")
public class FingerprintVerificationIndexController extends BaseController {

  @GetMapping("/index")
  public ModelAndView getFingerprintVerifyPage() {
    return renderFlowUi("Fingerprint", "fingerprint", "withFlow", "fingerprintFlow",
        "fingerprintVerification", false);
  }

}
