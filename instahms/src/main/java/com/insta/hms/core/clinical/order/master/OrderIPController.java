package com.insta.hms.core.clinical.order.master;

import com.insta.hms.core.clinical.URLRoute;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(URLRoute.ORDER_IP_URL)
public class OrderIPController extends OrderController {

  public OrderIPController() {
    super("ipFlow");
  }

}
