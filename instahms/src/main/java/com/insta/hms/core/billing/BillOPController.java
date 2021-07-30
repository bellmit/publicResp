package com.insta.hms.core.billing;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(URLRoute.OP_URL)
public class BillOPController extends BillController {
  public BillOPController() {
      super("opFlow");
  }
}
