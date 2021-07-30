package com.insta.hms.mdm.dietary;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.MasterRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The Class DietaryRepository.
 */
@Repository
public class DietaryRepository extends MasterRepository<Integer> {

  /**
   * Instantiates a new dietary repository.
   */
  public DietaryRepository() {
    super("diet_master", "diet_id");
  }

  /** The Constant GET_DIETARY_ITEM_SUB_GROUP_TAX_DETAILS. */
  private static final String GET_DIETARY_ITEM_SUB_GROUP_TAX_DETAILS = "SELECT "
      + " isg.item_subgroup_id, isg.subgroup_code, isg.item_subgroup_name, ig.group_code "
      + " FROM dietary_item_sub_groups disg "
      + " JOIN item_sub_groups isg ON(disg.item_subgroup_id = isg.item_subgroup_id) "
      + " JOIN item_groups ig ON (ig.item_group_id = isg.item_group_id) "
      + " WHERE disg.diet_id = ? ";

  /**
   * Gets the dietary item sub group tax details.
   *
   * @param actDescriptionId
   *          the act description id
   * @return the dietary item sub group tax details
   */
  public List<BasicDynaBean> getDietaryItemSubGroupTaxDetails(String actDescriptionId) {
    return DatabaseHelper.queryToDynaList(GET_DIETARY_ITEM_SUB_GROUP_TAX_DETAILS,
        new Object[] { Integer.parseInt(actDescriptionId) });
  }

  /** The Constant GET_ROUTINE_CHARGE_DISCOUNT. */
  private static final String GET_ROUTINE_CHARGE_DISCOUNT = "SELECT "
      + " dc.charge, dc.discount, dm.service_tax, dm.diet_id, dm.meal_name, "
      + " dm.service_sub_group_id, dm.insurance_category_id " + " FROM  diet_charges dc "
      + "  JOIN diet_master dm ON (dm.diet_id = dc.diet_id) "
      + " WHERE dc.diet_id=? AND bed_type=? AND org_id=? ";

  /**
   * Gets the charge for meal.
   *
   * @param orgId
   *          the org id
   * @param dietId
   *          the diet id
   * @param bedType
   *          the bed type
   * @return the charge for meal
   */
  public BasicDynaBean getChargeForMeal(String orgId, int dietId, String bedType) {
    return DatabaseHelper.queryToDynaBean(GET_ROUTINE_CHARGE_DISCOUNT, 
                                          new Object[] {dietId, bedType, orgId});
  }

}
