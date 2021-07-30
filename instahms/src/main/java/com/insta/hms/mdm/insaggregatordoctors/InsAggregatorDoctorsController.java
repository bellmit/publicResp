package com.insta.hms.mdm.insaggregatordoctors;

import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterRestController;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** The Class InsAggregatorDoctorsController. */
@RestController
@RequestMapping(URLRoute.INS_AGGREGATOR_DOCTORS_PATH)
public class InsAggregatorDoctorsController extends MasterRestController {

  /**
   * Instantiates a new ins aggregator doctors controller.
   *
   * @param service the service
   */
  public InsAggregatorDoctorsController(InsAggregatorDoctorsService service) {
    super(service);
  }
}
