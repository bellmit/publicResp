package com.insta.hms.mdm.insaggregatortpainsco;

import com.insta.hms.mdm.MasterRepository;
import com.insta.hms.mdm.SearchQuery;

import org.springframework.stereotype.Repository;

/** The Class InsAggregatorTpaInsCoRepository. */
@Repository
public class InsAggregatorTpaInsCoRepository extends MasterRepository<Integer> {

  /** Instantiates a new ins aggregator tpa ins co repository. */
  public InsAggregatorTpaInsCoRepository() {

    super(
        new String[] {"ia_id", "tpa_id", "insurance_co_id", "status"},
        null,
        "ia_tpa_insco_config",
        "ia_tpa_insco_id");
  }

  /** The Constant INSURANCE_AGGREGATOR_TPA_INSCO_CONFIG_TABLES. */
  /*
   * Search query to get the list of aggregators, TPA and Insurance Company mapping details
   *
   */
  private static final String INSURANCE_AGGREGATOR_TPA_INSCO_CONFIG_TABLES =
      "FROM "
          + "(SELECT  iac.*, tm.tpa_name, icm.insurance_co_name  "
          + "FROM  ia_tpa_insco_config iac "
          + "LEFT JOIN  tpa_master tm ON(iac.tpa_id = tm.tpa_id AND tm.status = 'A') "
          + "LEFT JOIN  insurance_company_master icm ON(icm.insurance_co_id = iac.insurance_co_id "
          + "AND icm.status = 'A')) as foo ";

  /* (non-Javadoc)
   * @see com.insta.hms.mdm.MasterRepository#getSearchQuery()
   */
  @Override
  public SearchQuery getSearchQuery() {
    return new SearchQuery(INSURANCE_AGGREGATOR_TPA_INSCO_CONFIG_TABLES);
  }

  /** The Constant INSURACNE_AGGREGATOR_TPA_INSCO_LOOKUP_QUERY. */
  private static final String INSURACNE_AGGREGATOR_TPA_INSCO_LOOKUP_QUERY =
      "SELECT * FROM ("
          + "("
          + "SELECT tm.tpa_name AS tpainsco_name, tm.tpa_id AS tpainsco_id, true AS is_tpa, status "
          + "FROM  tpa_master tm WHERE tm.status='A' "
          + "ORDER BY tm.tpa_name "
          + ") "
          + "UNION ALL ( "
          + "SELECT icm.insurance_co_name AS tpainsco_name, icm.insurance_co_id AS tpainsco_id, "
          + "false AS is_tpa, status "
          + "FROM  insurance_company_master icm  WHERE icm.status='A' "
          + "ORDER BY icm.insurance_co_name "
          + ") "
          + ") as foo ";

  /* (non-Javadoc)
   * @see com.insta.hms.mdm.MasterRepository#getLookupQuery()
   */
  @Override
  public String getLookupQuery() {
    return INSURACNE_AGGREGATOR_TPA_INSCO_LOOKUP_QUERY;
  }
}
