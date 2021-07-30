package com.insta.hms.mdm.insaggregatorpharmacies;

import com.insta.hms.mdm.MasterRepository;
import com.insta.hms.mdm.SearchQuery;

import org.springframework.stereotype.Repository;

/** The Class InsAggregatorPharmaciesRepository. */
@Repository
public class InsAggregatorPharmaciesRepository extends MasterRepository<Integer> {

  /** Instantiates a new ins aggregator pharmacies repository. */
  public InsAggregatorPharmaciesRepository() {

    super(
        new String[] {"ia_id", "pharmacy_id", "status"},
        null,
        "insurance_aggregator_pharmacy_config",
        "ia_pharmacy_id");
  }

  /** The Constant INSURANCE_AGGREGATOR_PHARMACY_CONFIG_TABLES. */
  /*
   * Search query to get the list of aggregators, and stores mapping details
   *
   */
  private static final String INSURANCE_AGGREGATOR_PHARMACY_CONFIG_TABLES =
      " FROM "
          + "(SELECT iapc.*, sm.dept_name, sm.dept_id "
          + "FROM stores sm, "
          + "insurance_aggregator_pharmacy_config iapc "
          + "WHERE sm.dept_id = iapc.pharmacy_id) as foo ";

  /* (non-Javadoc)
   * @see com.insta.hms.mdm.MasterRepository#getSearchQuery()
   */
  @Override
  public SearchQuery getSearchQuery() {
    return new SearchQuery(INSURANCE_AGGREGATOR_PHARMACY_CONFIG_TABLES);
  }

  /** The Constant INSURANCE_AGGREGATOR_PHARMACY_LOOKUP_QUERY. */
  private static final String INSURANCE_AGGREGATOR_PHARMACY_LOOKUP_QUERY =
      "SELECT * "
          + "FROM   (SELECT iapc.*, sm.dept_name, sm.dept_id "
          + "FROM stores sm,"
          + "insurance_aggregator_pharmacy_config iapc "
          + "WHERE sm.dept_id= iapc.pharmacy_id AND sm.status='A') as foo ";

  /* (non-Javadoc)
   * @see com.insta.hms.mdm.MasterRepository#getLookupQuery()
   */
  @Override
  public String getLookupQuery() {
    return INSURANCE_AGGREGATOR_PHARMACY_LOOKUP_QUERY;
  }
}
