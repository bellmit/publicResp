package com.insta.hms.mdm.bedtypes;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.MasterRepository;
import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * The Class BedTypeRepository.
 */
@Repository
public class BedTypeRepository extends MasterRepository<String> {

  /**
   * Instantiates a new bed type repository.
   */
  public BedTypeRepository() {
    super("bed_types", "bed_type_name");
  }

  /** The Constant GET_ALL_ACTIVE_BEDTYPES. */
  private static final String GET_ALL_ACTIVE_BEDTYPES = " SELECT bed_type_name as bed_type "
      + "from "
      + "bed_types WHERE billing_bed_type='Y' AND status = 'A' ORDER BY display_order ";

  /**
   * Gets the all active bed types.
   *
   * @return the all active bed types
   */
  public List<BasicDynaBean> getAllActiveBedTypes() {
    return DatabaseHelper.queryToDynaList(GET_ALL_ACTIVE_BEDTYPES);
  }

  /** The Constant INACTIVE_BEDS. */
  private static final String INACTIVE_BEDS = "SELECT DISTINCT intensive_bed_type FROM "
      + "icu_bed_charges WHERE bed_status = 'I'"
      + " UNION ALL SELECT DISTINCT bed_type FROM bed_details WHERE bed_status = 'I'";

  /**
   * Gets the inactive beds.
   *
   * @return the inactive beds
   */
  public List<BasicDynaBean> getInactiveBeds() {
    return DatabaseHelper.queryToDynaList(INACTIVE_BEDS);
  }

  /** The Constant GET_BED_ITEM_SUB_GROUP_TAX_DETAILS. */
  private static final String GET_BED_ITEM_SUB_GROUP_TAX_DETAILS = " SELECT isg.item_subgroup_id,"
      + " isg.subgroup_code, isg.item_subgroup_name, ig.group_code "
      + " FROM bed_item_sub_groups bisg "
      + " JOIN item_sub_groups isg ON(isg.item_subgroup_id = bisg.item_subgroup_id) "
      + " JOIN item_groups ig ON (ig.item_group_id = isg.item_group_id) "
      + " JOIN bed_names bn ON (bn.bed_type = bisg.bed_type_name) " + " WHERE bn.bed_id = ? ";

  /**
   * Gets the bed item sub group tax details.
   *
   * @param actDescriptionId the act description id
   * @return the bed item sub group tax details
   */
  public List<BasicDynaBean> getBedItemSubGroupTaxDetails(String actDescriptionId) {
    return DatabaseHelper.queryToDynaList(GET_BED_ITEM_SUB_GROUP_TAX_DETAILS,
        new Object[] { Integer.parseInt(actDescriptionId) });
  }

  /** The Constant GET_BED_TYPE_SUB_GROUP_TAX_DETAILS. */
  private static final String GET_BED_TYPE_SUB_GROUP_TAX_DETAILS = " SELECT isg.item_subgroup_id,"
      + " isg.subgroup_code, isg.item_subgroup_name, ig.group_code "
      + " FROM bed_item_sub_groups bisg "
      + " JOIN item_sub_groups isg ON(isg.item_subgroup_id = bisg.item_subgroup_id) "
      + " JOIN item_groups ig ON (ig.item_group_id = isg.item_group_id) "
      + " WHERE bisg.bed_type_name = ? ";

  /**
   * Gets the bed type sub group tax details.
   *
   * @param actDescriptionId the act description id
   * @return the bed type sub group tax details
   */
  public List<BasicDynaBean> getBedTypeSubGroupTaxDetails(String actDescriptionId) {
    return DatabaseHelper.queryToDynaList(GET_BED_TYPE_SUB_GROUP_TAX_DETAILS,
        new Object[] { actDescriptionId });
  }

  private static final String GET_BED_TYPE =
      "SELECT bed_type FROM bed_names bn WHERE bn.bed_id = ? ";

  /**
   * Method to get bed type list based on bed ids.
   * 
   * @param listItemIds the list of bed ids
   * @return the list of bed types
   */
  public List<String> getBedTypeList(List<String> listItemIds) {
    List<String> bedTypeIds = new ArrayList<>();
    for (String bedId : listItemIds) {
      if (!bedId.isEmpty()) {
        String bedType =
            DatabaseHelper.getString(GET_BED_TYPE, new Object[] {Integer.parseInt(bedId)});
        bedTypeIds.add(bedType);
      }
    }
    return bedTypeIds;
  }

  /**
   * Gets the all active bed types for packages.
   *
   * @return the all active bed types
   */
  public List<BasicDynaBean> getAllSortedBedTypes() {
    return DatabaseHelper.queryToDynaList(GET_ALL_ACTIVE_BEDTYPES_FOR_PACKAGES);
  }

  /** The Constant GET_ALL_ACTIVE_BEDTYPES. */
  private static final String GET_ALL_ACTIVE_BEDTYPES_FOR_PACKAGES = "SELECT bed_type_name as"
      + " bed_type from bed_types WHERE billing_bed_type='Y' AND status = 'A' ORDER BY case when "
      + " bed_type_name = 'GENERAL' then 0 else 1 end,  lower(bed_type_name)";
}
