package com.insta.hms.mdm.commoncharges;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.MasterRepository;
import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class CommonChargesRepository extends MasterRepository<String> {

  public CommonChargesRepository() {
    super("common_charges_master", "charge_name");
  }

  private static final String GET_COMMON_CHARGES_ITEM_SUB_GROUP_TAX_DETAILS =
      "SELECT isg.item_subgroup_id, isg.subgroup_code, isg.item_subgroup_name, ig.group_code "
          + " FROM common_item_sub_groups cisg "
          + " JOIN item_sub_groups isg ON(cisg.item_subgroup_id = isg.item_subgroup_id) "
          + " JOIN item_groups ig ON (ig.item_group_id = isg.item_group_id) "
          + " WHERE cisg.charge_name = ? ";

  public List<BasicDynaBean> getCommonChargesItemSubGroupTaxDetails(String actDescriptionId) {
    return DatabaseHelper.queryToDynaList(
        GET_COMMON_CHARGES_ITEM_SUB_GROUP_TAX_DETAILS, new Object[] {actDescriptionId});
  }

  private static final String GET_CHARGE_TYPE = " Select charge_name, charge_type FROM "
      + "(SELECT ccm.*,chargegroup_name,chargehead_name  FROM common_charges_master ccm "
      + " JOIN chargehead_constants ON (charge_type = chargehead_id) "
      + " JOIN chargegroup_constants USING(chargegroup_id))AS foo";

  /**
   * To get map of charge name and charge type.
   * @return mapping of charge_type by charge_name
   */
  public Map<String, String> getCommonChargeTypeMap() {
    Map<String, String> result = new HashMap<>();
    List<BasicDynaBean> charges = DatabaseHelper.queryToDynaList(GET_CHARGE_TYPE);

    for (BasicDynaBean bean : charges) {
      result.put((String) bean.get("charge_name"), (String) bean.get("charge_type"));
    }
    return result;
  }
}
