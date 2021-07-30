package com.insta.hms.api.repositories;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.MasterRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;


@Repository
public class ApiRatePlanRepository extends MasterRepository<String> {

  public ApiRatePlanRepository() {
    super("organization_details", "org_id", "org_name");
  }

  private static final String DEFAULT_RATE_PLANS_SEARCH_QUERY =
      "SELECT * "
          + " FROM (SELECT org_id AS rateplan_code, org_name AS rateplan_name, status,"
          + " valid_from_date AS valid_from, valid_to_date AS valid_to,"
          + " created_timestamp AS created_at, updated_timestamp AS modified_at "
          + " FROM organization_details od "
          + " WHERE ((od.has_date_validity AND current_date "
          + " BETWEEN od.valid_from_date AND od.valid_to_date ) "
          + " OR (NOT od.has_date_validity)) order by created_at) as foo";

  @Override
  public String getLookupQuery() {
    return DEFAULT_RATE_PLANS_SEARCH_QUERY;
  }

  public BasicDynaBean getRatePlanById(String ratePlanCode) {
    return DatabaseHelper.queryToDynaBean(
        DEFAULT_RATE_PLANS_SEARCH_QUERY + " WHERE rateplan_code = ?", ratePlanCode);
  }

}
