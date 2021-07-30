package com.insta.hms.mdm.rateplan;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.List;

/*
 * @author eshwar-chandra
 */
@Repository
public class RatePlanParametersRepository extends GenericRepository {

  /**
   * The Constant GET_DERIVED_RATE_PLAN_IDS.
   */
  private static final String GET_DERIVED_RATE_PLAN_IDS = "select org_id from "
      + "rate_plan_parameters " + " where base_rate_sheet_id =?";

  public RatePlanParametersRepository() {
    super("rate_plan_parameters");
  }

  /**
   * Gets the derived rate plan ids.
   *
   * @param baseRateSheetId the base rate sheet id
   * @return the derived rate plan ids
   */
  public List<BasicDynaBean> getDerivedRatePlanIds(String baseRateSheetId) {
    return DatabaseHelper
        .queryToDynaList(GET_DERIVED_RATE_PLAN_IDS, baseRateSheetId);
  }

}
