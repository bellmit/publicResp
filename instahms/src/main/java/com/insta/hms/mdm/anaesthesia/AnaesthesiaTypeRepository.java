package com.insta.hms.mdm.anaesthesia;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.MasterRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.List;

/** The Class AnaesthesiaTypeRepository. */
@Repository
public class AnaesthesiaTypeRepository extends MasterRepository<String> {

  /** Instantiates a new anaesthesia type repository. */
  public AnaesthesiaTypeRepository() {
    super("anesthesia_type_master", "anesthesia_type_id");
  }

  private static final String GET_ANAESTHESIA_TYPE_ITEM_SUB_GROUP_TAX_DETAILS =
      "SELECT isg.item_subgroup_id, isg.subgroup_code, isg.item_subgroup_name, ig.group_code "
          + " FROM anesthesia_item_sub_groups aisg "
          + " JOIN item_sub_groups isg ON(aisg.item_subgroup_id = isg.item_subgroup_id) "
          + " JOIN item_groups ig ON (ig.item_group_id = isg.item_group_id) "
          + " WHERE aisg.anesthesia_type_id = ? ";

  /**
   * Gets the anaesthesia type sub group tax details.
   *
   * @param actDescriptionId the act description id
   * @return the anaesthesia type sub group tax details
   */
  public List<BasicDynaBean> getAnaesthesiaTypeSubGroupTaxDetails(String actDescriptionId) {
    return DatabaseHelper.queryToDynaList(
        GET_ANAESTHESIA_TYPE_ITEM_SUB_GROUP_TAX_DETAILS, new Object[] {actDescriptionId});
  }
}
