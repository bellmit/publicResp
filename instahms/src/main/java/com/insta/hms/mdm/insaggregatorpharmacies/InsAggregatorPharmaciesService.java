package com.insta.hms.mdm.insaggregatorpharmacies;

import com.insta.hms.mdm.MasterService;

import org.springframework.stereotype.Service;

/** The Class InsAggregatorPharmaciesService. */
@Service
public class InsAggregatorPharmaciesService extends MasterService {

  /**
   * Instantiates a new ins aggregator pharmacies service.
   *
   * @param repo the repo
   * @param validator the validator
   */
  public InsAggregatorPharmaciesService(
      InsAggregatorPharmaciesRepository repo, InsAggregatorPharmaciesValidator validator) {
    super(repo, validator);
  }
}
