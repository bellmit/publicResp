package com.insta.hms.mdm.paymentcategories;

import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterController;
import com.insta.hms.mdm.MasterResponseRouter;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(URLRoute.PAYMENT_CATEGORY_PATH)
public class PaymentCategoryController extends MasterController {

  public PaymentCategoryController(PaymentCategoryService service) {
    super(service, MasterResponseRouter.PAYMENT_CATEGORY_ROUTER);
  }

}
