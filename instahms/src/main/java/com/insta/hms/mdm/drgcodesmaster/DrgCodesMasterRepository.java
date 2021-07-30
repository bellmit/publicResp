package com.insta.hms.mdm.drgcodesmaster;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.MasterRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The Class DrgCodesMasterRepository.
 */
@Repository
public class DrgCodesMasterRepository extends MasterRepository<String> {

  /**
   * Instantiates a new drg codes master repository.
   */
  public DrgCodesMasterRepository() {
    super("drg_codes_master", "drg_code");
  }

  /** The Constant GET_DRG_SUB_GROUP_TAX_DETAILS. */
  private static final String GET_DRG_SUB_GROUP_TAX_DETAILS = "SELECT isg.item_subgroup_id, "
      + " isg.subgroup_code, isg.item_subgroup_name, ig.group_code "
      + " FROM drg_code_item_sub_groups drgisg "
      + " JOIN item_sub_groups isg ON(drgisg.item_subgroup_id = isg.item_subgroup_id) "
      + " JOIN item_groups ig ON (ig.item_group_id = isg.item_group_id) "
      + " WHERE drgisg.drg_code = ? ";

  /**
   * Gets the drg item sub group tax details.
   *
   * @param itemId the item id
   * @return the drg item sub group tax details
   */
  public List<BasicDynaBean> getDrgItemSubGroupTaxDetails(String itemId) {
    return DatabaseHelper.queryToDynaList(GET_DRG_SUB_GROUP_TAX_DETAILS, itemId);
  }
  
}
