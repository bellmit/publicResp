package com.insta.hms.mdm.stockadjustmentreason;
/*
 * Owner : Ashok Pal, 7th Aug 2017
 */

import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterController;
import com.insta.hms.mdm.MasterResponseRouter;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class StockAdjustmentReasonController.
 */
@Controller
@RequestMapping(URLRoute.STOCK_ADJUSTMENT_REASON_MASTER_PATH)
public class StockAdjustmentReasonController extends MasterController {

  /**
   * Instantiates a new stock adjustment reason controller.
   *
   * @param service the service
   */
  public StockAdjustmentReasonController(StockAdjustmentReasonService service) {
    super(service, MasterResponseRouter.STOCK_ADJUSTMENT_REASON_ROUTER);
    // TODO Auto-generated constructor stub
  }

  /* (non-Javadoc)
   * @see com.insta.hms.mdm.MasterController#getReferenceLists(java.util.Map)
   */
  @Override
  protected Map<String, List<BasicDynaBean>> getReferenceLists(Map params) {
    return ((StockAdjustmentReasonService) getService()).getAddEditPageData();
  }
}
