package com.insta.hms.mdm.insaggregatordoctors;

import com.insta.hms.mdm.MasterRepository;
import com.insta.hms.mdm.SearchQuery;

import org.springframework.stereotype.Repository;

/** The Class InsAggregatorDoctorsRepository. */
@Repository
public class InsAggregatorDoctorsRepository extends MasterRepository<Integer> {

  /** Instantiates a new ins aggregator doctors repository. */
  public InsAggregatorDoctorsRepository() {

    super(
        new String[] {"ia_id", "doctor_id", "status"},
        null,
        "insurance_aggregator_doctor_config",
        "ia_doctor_id");
  }

  /** The Constant INSURANCE_AGGREGATOR_DOCTORS_CONFIG_TABLES. */
  /*
   * Search query to get the list of aggregators, and doctors mapping details
   *
   */
  private static final String INSURANCE_AGGREGATOR_DOCTORS_CONFIG_TABLES =
      " FROM "
          + "(SELECT iadc.*, dr.doctor_name "
          + "FROM doctors dr, "
          + "insurance_aggregator_doctor_config iadc "
          + "WHERE dr.doctor_id = iadc.doctor_id) as foo ";

  /* (non-Javadoc)
   * @see com.insta.hms.mdm.MasterRepository#getSearchQuery()
   */
  @Override
  public SearchQuery getSearchQuery() {
    return new SearchQuery(INSURANCE_AGGREGATOR_DOCTORS_CONFIG_TABLES);
  }

  /** The Constant INSURANCE_AGGREGATOR_DOCTORS_LOOKUP_QUERY. */
  private static final String INSURANCE_AGGREGATOR_DOCTORS_LOOKUP_QUERY =
      "SELECT * "
          + "FROM  (SELECT iadc.*, dr.doctor_name "
          + "FROM doctors dr, "
          + "insurance_aggregator_doctor_config iadc "
          + "WHERE dr.doctor_id = iadc.doctor_id "
          + "AND iadc.status = 'A' "
          + ") as foo ";

  /* (non-Javadoc)
   * @see com.insta.hms.mdm.MasterRepository#getLookupQuery()
   */
  @Override
  public String getLookupQuery() {
    return INSURANCE_AGGREGATOR_DOCTORS_LOOKUP_QUERY;
  }
}
