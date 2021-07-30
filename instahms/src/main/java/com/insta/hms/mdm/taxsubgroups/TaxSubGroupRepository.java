package com.insta.hms.mdm.taxsubgroups;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.StringUtil;
import com.insta.hms.mdm.MasterRepository;
import com.insta.hms.mdm.SearchQuery;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * The Class TaxSubGroupRepository.
 */
@Repository
public class TaxSubGroupRepository extends MasterRepository<Integer> {
  
  /**
   * Instantiates a new tax sub group repository.
   */
  public TaxSubGroupRepository() {
    super(new String[] { "item_subgroup_name", "item_group_id" }, null, "item_sub_groups",
      "item_subgroup_id");
  }

  /** The item subgroup search tables. */
  private static String ITEM_SUBGROUP_SEARCH_TABLES = " FROM(SELECT isg.item_subgroup_id,"
      + "isg.item_subgroup_name,isg.item_group_id, "
      + " isg.item_subgroup_display_order,isg.item_subgroup_id, " 
      + "isgtd.tax_rate,it.item_group_name,isg.status,isgtd.validity_start,isgtd.validity_end  "
      + " From item_sub_groups isg "
      + " LEFT JOIN item_sub_groups_tax_details isgtd ON (isg.item_subgroup_id = "
      + " isgtd.item_subgroup_id)"
      + " LEFT JOIN item_groups it ON(it.item_group_id = isg.item_group_id) ) AS foo ";

  /* (non-Javadoc)
   * @see com.insta.hms.mdm.MasterRepository#getSearchQuery()
   */
  @Override
  public SearchQuery getSearchQuery() {
    return new SearchQuery(ITEM_SUBGROUP_SEARCH_TABLES);
  }

  /** The get sub group item code. */
  private static String GET_SUB_GROUP_ITEM_CODE = " SELECT * From item_sub_groups  "
      + " LEFT JOIN item_sub_groups_tax_details  USING(item_subgroup_id)"
      + " where item_subgroup_id = ? ";

  /**
   * Gets the item sub group code.
   *
   * @param itemSubGroupId the item sub group id
   * @return the item sub group code
   */
  public BasicDynaBean getItemSubGroupCode(int itemSubGroupId) {

    return DatabaseHelper.queryToDynaBean(GET_SUB_GROUP_ITEM_CODE, new Object[] { itemSubGroupId });

  }

  // private static final String GET_ALL_CODES = "Select item_subgroup_id,item_subgroup_name from
  // item_sub_groups where status='A'";
  //
  // public List<BasicDynaBean> getAllCodeList() {
  // return DatabaseHelper.queryToDynaList(GET_ALL_CODES);
  // }

  /** The Constant GET_ALL_ITEM_SUB_Groups. */
  private static final String GET_ALL_ITEM_SUB_Groups = "Select * from "
      + "item_sub_groups where status='A'";

  /**
   * Gets the item sub group list.
   *
   * @return the item sub group list
   */
  public List<BasicDynaBean> getItemSubGroupList() {
    return DatabaseHelper.queryToDynaList(GET_ALL_ITEM_SUB_Groups);
  }

  // In Edit Tax Sub Group screen while changing the Status to inactive if the tax group is mapped
  /** The tax subgroup. */
  // to an item should throw an alert.
  private static String TAX_SUBGROUP = " SELECT sisg.item_subgroup_id as store_item_subgroup_id, "
      + " oisg.item_subgroup_id as operation_item_sub_group_id , "
      + " seisg.item_subgroup_id as service_item_sub_group_id ,"
      + " disg.item_subgroup_id as diagnostics_item_sub_group_id, "
      + " cisg.item_subgroup_id as consultation_item_sub_group_id,"
      + " bisg.item_subgroup_id as bed_item_sub_group_id,dcisg.item_subgroup_id " 
      + " as drg_code_item_sub_group_id,tisg.item_subgroup_id as theatre_item_sub_group_id,"
      + " aisg.item_subgroup_id as anesthesia_item_sub_group_id,coisg.item_subgroup_id as "
      + " common_item_sub_group_id,eisg.item_subgroup_id as equipment_item_sub_group_id ,"
      + " dtisg.item_subgroup_id as dietary_item_sub_group_id,pisg.item_subgroup_id as " 
      + " perdiem_code_item_sub_group_id"
      + " From item_sub_groups isg"
      + " LEFT JOIN store_item_sub_groups sisg ON(sisg.item_subgroup_id = isg.item_subgroup_id ) "
      + " LEFT JOIN operation_item_sub_groups oisg ON "
      + " (oisg.item_subgroup_id = isg.item_subgroup_id )"
      + " LEFT JOIN service_item_sub_groups seisg ON "
      + " (seisg.item_subgroup_id = isg.item_subgroup_id )"
      + " LEFT JOIN diagnostics_item_sub_groups disg ON "
      + " (disg.item_subgroup_id = isg.item_subgroup_id )"
      + " LEFT JOIN consultation_item_sub_groups cisg ON "
      + " (cisg.item_subgroup_id = isg.item_subgroup_id ) "
      + " LEFT JOIN bed_item_sub_groups bisg ON(bisg.item_subgroup_id = isg.item_subgroup_id )"
      + " LEFT JOIN drg_code_item_sub_groups dcisg ON"
      + " (dcisg.item_subgroup_id = isg.item_subgroup_id )"
      + " LEFT JOIN theatre_item_sub_groups tisg ON(tisg.item_subgroup_id = isg.item_subgroup_id )"
      + " LEFT JOIN anesthesia_item_sub_groups aisg ON"
      + " (aisg.item_subgroup_id = isg.item_subgroup_id )"
      + " LEFT JOIN common_item_sub_groups coisg ON "
      + " (coisg.item_subgroup_id = isg.item_subgroup_id )"
      + " LEFT JOIN equipment_item_sub_groups eisg ON "
      + " (eisg.item_subgroup_id = isg.item_subgroup_id )"
      + " LEFT JOIN dietary_item_sub_groups dtisg ON "
      + " (dtisg.item_subgroup_id = isg.item_subgroup_id )"
      + " LEFT JOIN perdiem_code_item_sub_groups pisg ON "
      + " (pisg.item_subgroup_id = isg.item_subgroup_id ) "
      + " WHERE isg.item_subgroup_id = ? and isg.status='A' ";

  /**
   * Gets the sub group.
   *
   * @param itemSubGroupId the item sub group id
   * @return the sub group
   */
  public List<BasicDynaBean> getSubGroup(int itemSubGroupId) {

    return DatabaseHelper.queryToDynaList(TAX_SUBGROUP, new Object[] { itemSubGroupId });

  }

  /** The Constant GET_All_ITEM_SUBGROUP. */
  private static final String GET_All_ITEM_SUBGROUP = "Select * from item_sub_groups "
      + " Left Join item_sub_groups_tax_details  USING(item_subgroup_id) "
      + " where validity_start <= ? And status= 'A' ";

  /**
   * Gets the item sub group.
   *
   * @param date the date
   * @return the item sub group
   */
  public static List<BasicDynaBean> getItemSubGroup(Date date) {
    return DatabaseHelper.queryToDynaList(GET_All_ITEM_SUBGROUP, new Object[] { date });
  }

  /**
   * Gets the item sub groups.
   *
   * @return the item sub groups
   */
  public List<BasicDynaBean> getItemSubGroups() {
    return DatabaseHelper.queryToDynaList("select item_subgroup_id, item_subgroup_name  from"
        + " item_sub_groups where status='A' order by item_subgroup_name");
  }

  /** The Constant GET_ITEM_SUBGROUP_ID. */
  private static final String GET_ITEM_SUBGROUP_ID = " SELECT item_group_id ," 
      + " item_subgroup_id FROM item_sub_groups "
      + " WHERE item_group_id = ? AND item_subgroup_id = ? ";

  /**
   * Gets the sub group id.
   *
   * @param itemgrpId the itemgrp id
   * @param itemSubgrpId the item subgrp id
   * @return the sub group id
   */
  public BasicDynaBean getSubGroupId(Integer itemgrpId, Integer itemSubgrpId) {
    return DatabaseHelper.queryToDynaBean(GET_ITEM_SUBGROUP_ID,
        new Object[] { itemgrpId, itemSubgrpId });
  }

  /* (non-Javadoc)
   * @see com.insta.hms.common.GenericRepository#findByCriteria(java.util.Map)
   */
  public List<BasicDynaBean> findByCriteria(Map map) {
    return super.findByCriteria(map);
  }

  /** The item subgroup details. */
  private static String ITEM_SUBGROUP_DETAILS = "SELECT foo.*, sum(foo.tax_rate) " 
      + " over(partition by foo.group_code) as agg_tax_rate "
      + " FROM (SELECT isg.item_subgroup_id, isg.item_subgroup_name, "
      + " isg.item_group_id, "
      + " isg.item_subgroup_display_order, it.item_group_name, it.group_code, isg.status,"
      + " isgtd.tax_rate, isgtd.validity_start,isgtd.validity_end  " + " FROM item_sub_groups isg "
      + " LEFT JOIN item_sub_groups_tax_details isgtd ON "
      + " (isg.item_subgroup_id = isgtd.item_subgroup_id)"
      + " LEFT JOIN item_groups it ON(it.item_group_id = isg.item_group_id) "
      + " WHERE isg.item_subgroup_id IN (##)) AS foo";

  /**
   * Gets the subgroups.
   *
   * @param filter the filter
   * @return the subgroups
   */
  public List<BasicDynaBean> getSubgroups(Map<String, Object> filter) {

    if (null != filter && filter.containsKey("item_subgroup_id")) {
      Object subgroups = filter.get("item_subgroup_id");
      if (null != subgroups && subgroups.getClass().isArray()) {
        int len = ((Object[]) subgroups).length;
        if (len > 0) {
          String[] placeholder = new String[len];
          Arrays.fill(placeholder, "?");
          String replacement = StringUtil.join(placeholder, ",");
          String qry = ITEM_SUBGROUP_DETAILS.replace("##", replacement);
          return DatabaseHelper.queryToDynaList(qry, ((Object[]) filter.get("item_subgroup_id")));
        }
      }
    }

    return DatabaseHelper.queryToDynaList(ITEM_SUBGROUP_DETAILS);
  }

  /** The get details. */
  private static String GET_DETAILS = " select isg.*, ig.group_code from item_sub_groups isg "
      + " join item_groups ig ON (isg.item_group_id = ig.item_group_id) " 
      + " where isg.item_subgroup_id = ? ";

  /**
   * Gets the details.
   *
   * @param itemSubGrpId the item sub grp id
   * @return the details
   */
  public BasicDynaBean getDetails(int itemSubGrpId) {
    return DatabaseHelper.queryToDynaBean(GET_DETAILS, new Object[] { itemSubGrpId });
  }

  //TODO : Add validity check
  private static final String GET_All_VALID_ITEM_SUBGROUP = "Select * from item_sub_groups "
      + " Left Join item_sub_groups_tax_details  USING(item_subgroup_id) "
      + " where item_group_id = ? AND status= 'A' ";
  
  public static List<BasicDynaBean> getValidItemSubGroup(int itemGroupId) {
    return DatabaseHelper.queryToDynaList(GET_All_VALID_ITEM_SUBGROUP, new Object[]{itemGroupId});
  }

  private static final String TAX_SUBGROUP_HAS_REFERENCES = "SELECT COUNT(a.has_data) > 0 dt FROM ("
      + "(SELECT 1 as has_data FROM store_item_sub_groups"
      + "   WHERE item_subgroup_id = :subgroupid LIMIT 1) UNION ALL "
      + "(SELECT 1 as has_data FROM operation_item_sub_groups"
      + "   WHERE item_subgroup_id = :subgroupid LIMIT 1) UNION ALL "
      + "(SELECT 1 as has_data FROM service_item_sub_groups"
      + "   WHERE item_subgroup_id = :subgroupid LIMIT 1) UNION ALL "
      + "(SELECT 1 as has_data FROM diagnostics_item_sub_groups"
      + "   WHERE item_subgroup_id = :subgroupid LIMIT 1) UNION ALL "
      + "(SELECT 1 as has_data FROM consultation_item_sub_groups"
      + "   WHERE item_subgroup_id = :subgroupid LIMIT 1) UNION ALL "
      + "(SELECT 1 as has_data FROM bed_item_sub_groups"
      + "   WHERE item_subgroup_id = :subgroupid LIMIT 1) UNION ALL "
      + "(SELECT 1 as has_data FROM drg_code_item_sub_groups"
      + "   WHERE item_subgroup_id = :subgroupid LIMIT 1) UNION ALL "
      + "(SELECT 1 as has_data FROM theatre_item_sub_groups"
      + "   WHERE item_subgroup_id = :subgroupid LIMIT 1) UNION ALL "
      + "(SELECT 1 as has_data FROM anesthesia_item_sub_groups"
      + "   WHERE item_subgroup_id = :subgroupid LIMIT 1) UNION ALL "
      + "(SELECT 1 as has_data FROM common_item_sub_groups"
      + "   WHERE item_subgroup_id = :subgroupid LIMIT 1) UNION ALL "
      + "(SELECT 1 as has_data FROM equipment_item_sub_groups"
      + "   WHERE item_subgroup_id = :subgroupid LIMIT 1) UNION ALL "
      + "(SELECT 1 as has_data FROM dietary_item_sub_groups"
      + "   WHERE item_subgroup_id = :subgroupid LIMIT 1) UNION ALL "
      + "(SELECT 1 as has_data FROM perdiem_code_item_sub_groups"
      + "   WHERE item_subgroup_id = :subgroupid LIMIT 1)) a;";
  
  /**
   * Check if the tax sub group is being referenced by any of the other masters for taxation.
   * @param itemSubGroupId item sub group identifier to be checked
   * @return true if tax subgroup is being referenced else returns false
   */
  public boolean taxSubGroupHasMasterReferences(int itemSubGroupId) {
    MapSqlParameterSource params = new MapSqlParameterSource();
    params.addValue("subgroupid", itemSubGroupId);
    BasicDynaBean result = DatabaseHelper.queryToDynaBean(TAX_SUBGROUP_HAS_REFERENCES, params);
    return result != null && ((Boolean) result.get("dt"));
  }
}
