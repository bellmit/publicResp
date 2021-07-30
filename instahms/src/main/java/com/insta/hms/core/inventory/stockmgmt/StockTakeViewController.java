package com.insta.hms.core.inventory.stockmgmt;

import com.insta.hms.core.inventory.URLRoute;
import com.insta.hms.core.inventory.stockmgmt.StockTakeService.StockTakeAction;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(URLRoute.STOCK_TAKE_VIEW_BASE)
public class StockTakeViewController extends StockTakeCommonController {

  @Override
  protected StockTakeAction getDelegateAction() {
    return StockTakeAction.VIEW;
  }
}
