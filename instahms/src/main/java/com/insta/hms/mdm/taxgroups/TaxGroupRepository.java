package com.insta.hms.mdm.taxgroups;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.MasterRepository;
import com.insta.hms.mdm.SearchQuery;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.List;

// TODO: Auto-generated Javadoc
/** The Class TaxGroupRepository. */
@Repository
public class TaxGroupRepository extends MasterRepository<Integer> {

  /** Instantiates a new tax group repository. */
  public TaxGroupRepository() {
    super("item_groups", "item_group_id", "item_group_name");
  }

  /** The Constant ITEM_GROUP_SEARCH_TABLES. */
  private static final String ITEM_GROUP_SEARCH_TABLES =
      " FROM(SELECT ig.item_group_id, "
          + "ig.item_group_name, ig.group_code, ig.item_group_display_order,"
          + " ig.status, "
          + "igt.item_group_type_id, igt.item_group_type_name From item_groups ig "
          + " JOIN "
          + "item_group_type igt ON (ig.item_group_type_id = igt.item_group_type_id)) AS "
          + "foo ";

  /* (non-Javadoc)
   * @see com.insta.hms.mdm.MasterRepository#getSearchQuery()
   */
  @Override
  public SearchQuery getSearchQuery() {
    // we want item_group_type_name from item_group_type table and will showing dashboard screen
    // i.e list screen, that's y overridden the method.
    return new SearchQuery(ITEM_GROUP_SEARCH_TABLES);
  }

  /** The Constant GET_ALL_ITEM_GROUPS. */
  private static final String GET_ALL_ITEM_GROUPS =
      "Select * from item_groups where status='A' and item_group_type_id ='TAX'";

  /**
   * Gets the item group list.
   *
   * @return the item group list
   */
  public List<BasicDynaBean> getItemGroupList() {
    return DatabaseHelper.queryToDynaList(GET_ALL_ITEM_GROUPS);
  }

  /** The Constant GET_ALL_ITEM_GROUP_TYPE. */
  private static final String GET_ALL_ITEM_GROUP_TYPE =
      "Select * from item_group_type" + " " + "where status='A' and item_group_type_id ='TAX' ";

  /**
   * Gets the item group type.
   *
   * @return the item group type
   */
  public List<BasicDynaBean> getItemGroupType() {
    return DatabaseHelper.queryToDynaList(GET_ALL_ITEM_GROUP_TYPE);
  }

  /** The Constant GET_ALL_GROUP_NAMES. */
  private static final String GET_ALL_GROUP_NAMES =
      "Select item_group_id,item_group_name "
          + " from item_groups where item_group_type_id ='TAX' ";

  /**
   * Gets the name list.
   *
   * @return the name list
   */
  public List<BasicDynaBean> getNameList() {
    return DatabaseHelper.queryToDynaList(GET_ALL_GROUP_NAMES);
  }

  /** The Constant GET_ALL_SUB_GROUPS. */
  private static final String GET_ALL_SUB_GROUPS =
      "Select * from item_sub_groups " + " where " + "item_group_id = ? and status='A' ";

  /**
   * Gets the sub group list.
   *
   * @param groupId the group id
   * @return the sub group list
   */
  public List<BasicDynaBean> getSubGroupList(int groupId) {
    return DatabaseHelper.queryToDynaList(GET_ALL_SUB_GROUPS, new Object[] {groupId});
  }

  /**
   * Gets the item groups.
   *
   * @return the item groups
   */
  public List<BasicDynaBean> getItemGroups() {
    return DatabaseHelper.queryToDynaList(
        "select item_group_id, item_group_name  from"
            + " item_groups where status='A' "
            + " AND item_group_type_id ='TAX' "
            + " order by item_group_name");
  }

  /** The Constant GET_ALL_TAX_ITEM_GROUPS. */
  private static final String GET_ALL_TAX_ITEM_GROUPS =
      "SELECT item_group_name, item_group_id "
          + " FROM item_groups ig "
          + " JOIN item_group_type igt ON (ig.item_group_type_id = "
          + " igt.item_group_type_id)"
          + " WHERE ig.status = 'A' AND igt.item_group_type_name= 'TAX'"
          + " ORDER BY item_group_display_order";

  /**
   * Gets the tax item groups.
   *
   * @return the tax item groups
   */
  public List<BasicDynaBean> getTaxItemGroups() {
    return DatabaseHelper.queryToDynaList(GET_ALL_TAX_ITEM_GROUPS);
  }
}
