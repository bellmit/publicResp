package com.insta.hms.mdm.accountingheads;

import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterController;
import com.insta.hms.mdm.MasterResponseRouter;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(URLRoute.ACCOUNTING_HEAD_PATH)
public class AccountingHeadsController extends MasterController {

  public AccountingHeadsController(AccountingHeadsService service) {
    super(service, MasterResponseRouter.ACCOUNTING_HEADS_ROUTER);
  }
}
