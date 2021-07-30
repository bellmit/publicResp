package com.insta.hms.mdm.insaggregatortpainsco;

import com.insta.hms.mdm.MasterRepository;

import org.springframework.stereotype.Repository;

/** The Class InsAggregatorTpaInsCoDetailsRepository. */
@Repository
public class InsAggregatorTpaInsCoDetailsRepository extends MasterRepository<Integer> {

  /** Instantiates a new ins aggregator tpa ins co details repository. */
  public InsAggregatorTpaInsCoDetailsRepository() {

    super(
        new String[] {"ia_tpa_insco_id", "service_name", "applicable", "status"},
        null,
        "ia_tpa_insco_supported_services",
        "ia_tpa_insco_service_id");
  }
}
