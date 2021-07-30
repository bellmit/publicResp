package com.insta.hms.mdm.insaggregatordoctors;

import com.insta.hms.mdm.MasterService;

import org.springframework.stereotype.Service;

/** The Class InsAggregatorDoctorsService. */
@Service
public class InsAggregatorDoctorsService extends MasterService {

  /**
   * Instantiates a new ins aggregator doctors service.
   *
   * @param repo the repo
   * @param validator the validator
   */
  public InsAggregatorDoctorsService(
      InsAggregatorDoctorsRepository repo, InsAggregatorDoctorsValidator validator) {
    super(repo, validator);
  }
}
