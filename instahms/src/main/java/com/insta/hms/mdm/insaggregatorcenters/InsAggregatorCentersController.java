package com.insta.hms.mdm.insaggregatorcenters;

import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterRestController;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** The Class InsAggregatorCentersController. */
@RestController
@RequestMapping(URLRoute.INS_AGGREGATOR_CENTERS_PATH)
public class InsAggregatorCentersController extends MasterRestController {

  /**
   * Instantiates a new ins aggregator centers controller.
   *
   * @param service the service
   */
  public InsAggregatorCentersController(InsAggregatorCentersService service) {
    super(service);
  }
}
