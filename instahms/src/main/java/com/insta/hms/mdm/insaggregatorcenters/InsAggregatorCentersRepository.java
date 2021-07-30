package com.insta.hms.mdm.insaggregatorcenters;

import com.insta.hms.mdm.MasterRepository;
import com.insta.hms.mdm.SearchQuery;

import org.springframework.stereotype.Repository;

/** The Class InsAggregatorCentersRepository. */
@Repository
public class InsAggregatorCentersRepository extends MasterRepository<Integer> {

  /** Instantiates a new ins aggregator centers repository. */
  public InsAggregatorCentersRepository() {

    super(
        new String[] {"ia_id", "center_id", "status"},
        null,
        "insurance_aggregator_center_config",
        "ia_center_id");
  }

  /** The Constant INSURANCE_AGGREGATOR_CENTERS_CONFIG_TABLES. */
  /*
   * Search query to get the list of aggregators and center mapping details
   *
   */
  private static final String INSURANCE_AGGREGATOR_CENTERS_CONFIG_TABLES =
      " FROM "
          + " (SELECT  iac.ia_center_id, iac.facility_id, iac.center_id, iac.ia_id, iac.status, "
          + " iac.ia_center_conf, hcm.center_name  "
          + " FROM  insurance_aggregator_center_config iac "
          + " JOIN hospital_center_master hcm ON(iac.center_id = hcm.center_id) ) as foo ";

  /* (non-Javadoc)
   * @see com.insta.hms.mdm.MasterRepository#getSearchQuery()
   */
  @Override
  public SearchQuery getSearchQuery() {
    return new SearchQuery(INSURANCE_AGGREGATOR_CENTERS_CONFIG_TABLES);
  }

  /** The Constant INSURACNE_AGGREGATOR_LOOKUP_QUERY. */
  private static final String INSURACNE_AGGREGATOR_LOOKUP_QUERY =
      "SELECT * "
          + "FROM (SELECT iac.ia_center_id, iac.facility_id, iac.center_id, iac.ia_id, iac.status,"
          + "iac.ia_center_conf, hcm.center_name  "
          + "FROM insurance_aggregator_center_config iac "
          + "LEFT JOIN hospital_center_master hcm on(hcm.center_id = iac.center_id) "
          + "where iac.status='A') as foo";

  /* (non-Javadoc)
   * @see com.insta.hms.mdm.MasterRepository#getLookupQuery()
   */
  @Override
  public String getLookupQuery() {
    return INSURACNE_AGGREGATOR_LOOKUP_QUERY;
  }
}
