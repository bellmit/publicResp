package com.insta.hms.mdm.insuranceitemcategories;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.MasterRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * The Class InsuranceItemCategoryRepository.
 */
@Repository
public class InsuranceItemCategoryRepository extends MasterRepository<Integer> {

  /**
   * Instantiates a new insurance item category repository.
   */
  public InsuranceItemCategoryRepository() {
    super("item_insurance_categories", "insurance_category_id", "insurance_category_name");

  }

  /* (non-Javadoc)
   * @see com.insta.hms.mdm.MasterRepository#getNextId()
   */
  @Override
  public Object getNextId() {
    return super.getNextSequence();
  }

  /** The Constant GET_INSURANCE_CATEGORY. */
  private static final String GET_INSURANCE_CATEGORY = " SELECT insurance_category_id, "
      + " insurance_category_name,insurance_payable,system_category "
      + " FROM item_insurance_categories ORDER BY display_order";

  /**
   * Gets the insurance category list.
   *
   * @return the insurance category list
   */
  public List<BasicDynaBean> getInsuranceCategoryList() {
    return DatabaseHelper.queryToDynaList(GET_INSURANCE_CATEGORY);
  }

}
