package com.insta.hms.mdm.discountplans;

import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.SearchQueryAssembler;
import com.insta.hms.mdm.MasterRepository;

import org.springframework.stereotype.Repository;

import java.util.Map;

/**
 * The Class DiscountPlanRepository.
 */
@Repository("discountPlanRepository")
public class DiscountPlanRepository extends MasterRepository<Integer> {

  /**
   * Instantiates a new discount plan repository.
   */
  public DiscountPlanRepository() {
    super("discount_plan_main", "discount_plan_id", "discount_plan_name");
  }

  /** The Constant DISCOUNT_FILEDS. */
  private static final String DISCOUNT_FILEDS = "SELECT * ";
  
  /** The discount from. */
  private static final String DISCOUNT_FROM = " FROM "
      + " (SELECT dpm.*,dpcm.center_id FROM discount_plan_main dpm "
      + " left join discount_plan_center_master  dpcm "
      + " on(dpm.discount_plan_id=dpcm.discount_plan_id )   # ) as foo ";
  
  /** The Constant DISCOUNT_FROM1. */
  private static final String DISCOUNT_FROM1 = " FROM discount_plan_main ";
  
  /** The Constant COUNT. */
  private static final String COUNT = " SELECT count(discount_plan_id) ";

  /**
   * Gets the discount plan main details qa.
   *
   * @param params the params
   * @param listingParams the listing params
   * @return the discount plan main details qa
   */
  @SuppressWarnings("rawtypes")
  public SearchQueryAssembler getDiscountPlanMainDetailsQa(Map params,
      Map<LISTING, Object> listingParams) {
    SearchQueryAssembler qa = null;
    String[] centerIds = (String[]) params.get("_center_id");
    int centerId = -1;
    if (centerIds != null) {
      centerId = Integer.parseInt(centerIds[0]);
    }

    if (centerId == -1) {
      qa = new SearchQueryAssembler(DISCOUNT_FILEDS, COUNT, DISCOUNT_FROM1, listingParams);
    } else {
      String from = null;
      if (centerId == 0) {
        from = DISCOUNT_FROM.replace("#", " where (dpcm.center_id=0 or dpcm.center_id is null)");
      } else {
        from = DISCOUNT_FROM.replace("#", " where dpcm.center_id=" + centerId);
      }
      qa = new SearchQueryAssembler(DISCOUNT_FILEDS, COUNT, from, listingParams);
    }
    qa.addFilterFromParamMap(params);
    return qa;
  }

}
