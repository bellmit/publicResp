package com.insta.hms.mdm.discountplans;

import com.insta.hms.mdm.MasterRepository;

import org.springframework.stereotype.Repository;

/**
 * The Class DiscountPlanDetailsRepository.
 */
@Repository
public class DiscountPlanDetailsRepository extends MasterRepository<Integer> {

  /**
   * Instantiates a new discount plan details repository.
   */
  public DiscountPlanDetailsRepository() {
    super("discount_plan_details", "discount_plan_detail_id", "applicable_to_id");
  }

  /* (non-Javadoc)
   * @see com.insta.hms.mdm.MasterRepository#allowsDuplicates()
   */
  @Override
  protected boolean allowsDuplicates() {
    return true; 
  }
}
