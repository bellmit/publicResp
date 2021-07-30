package com.insta.hms.mdm.insaggregatorcenters;

import com.insta.hms.mdm.MasterService;

import org.springframework.stereotype.Service;

/** The Class InsAggregatorCentersService. */
@Service
public class InsAggregatorCentersService extends MasterService {

  /**
   * Instantiates a new ins aggregator centers service.
   *
   * @param repo the repo
   * @param validator the validator
   */
  public InsAggregatorCentersService(
      InsAggregatorCentersRepository repo, InsAggregatorCentersValidator validator) {
    super(repo, validator);
  }
}
