package com.insta.hms.mdm.bank;

import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterRestController;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller("bankController")
@RequestMapping(URLRoute.BANK_MASTER)
public class BankController extends MasterRestController {

  /**
   * Instantiates a new bank controller.
   *
   * @param service the service
   */
  public BankController(BankService service) {
    super(service);
  }

}
