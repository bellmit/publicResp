package com.insta.hms.mdm.ordersets;

import com.insta.hms.master.URLRoute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(URLRoute.ORDER_SETS_PATH)
public class OrderSetsController extends PackageBaseController {

  public OrderSetsController(OrderSetsService service) {
    super(service, "ordersets");
  }
}
