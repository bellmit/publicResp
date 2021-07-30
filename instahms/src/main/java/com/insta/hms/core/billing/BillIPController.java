package com.insta.hms.core.billing;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(URLRoute.IP_URL)
public class BillIPController extends BillController {
  public BillIPController() {
      super("ipFlow");
  }
}
