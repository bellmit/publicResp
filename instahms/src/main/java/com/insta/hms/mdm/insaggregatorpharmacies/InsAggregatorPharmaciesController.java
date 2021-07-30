package com.insta.hms.mdm.insaggregatorpharmacies;

import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterRestController;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** The Class InsAggregatorPharmaciesController. */
@RestController
@RequestMapping(URLRoute.INS_AGGREGATOR_PHARMACIES_PATH)
public class InsAggregatorPharmaciesController extends MasterRestController {

  /**
   * Instantiates a new ins aggregator pharmacies controller.
   *
   * @param service the service
   */
  public InsAggregatorPharmaciesController(InsAggregatorPharmaciesService service) {
    super(service);
  }
}
