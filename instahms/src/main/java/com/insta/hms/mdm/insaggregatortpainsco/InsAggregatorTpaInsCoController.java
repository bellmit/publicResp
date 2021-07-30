package com.insta.hms.mdm.insaggregatortpainsco;

import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterDetailsRestController;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** The Class InsAggregatorTpaInsCoController. */
@RestController
@RequestMapping(URLRoute.INS_AGGREGATOR_TPA_INSCO_PATH)
public class InsAggregatorTpaInsCoController extends MasterDetailsRestController {

  /**
   * Instantiates a new ins aggregator tpa ins co controller.
   *
   * @param service the service
   */
  public InsAggregatorTpaInsCoController(InsAggregatorTpaInsCoService service) {
    super(service);
  }
}
