package com.insta.hms.mdm.equipment;

import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryAssembler;
import com.insta.hms.mdm.SearchQuery;
import com.insta.hms.mdm.bulk.BulkDataRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class EquipmentRepository extends BulkDataRepository<Integer> {
  public EquipmentRepository() {
    super("equipment_master", "eq_id", "equipment_name");
  }

  public SearchQuery getSearchQuery() {
    return new SearchQuery(EQUIPMENT_FROM_TABLES);
  }

  private static final String EQUIPMENT_FIELDS = "SELECT *";
  private static final String EQUIPMENT_COUNT = "SELECT count(*)";
  private static final String EQUIPMENT_FROM_TABLES =
      " FROM (SELECT em.equipment_name, em.eq_id,"
          + " em.status, dept.dept_name, em.dept_id, "
          + " em.service_sub_group_id,'equipment'::text as chargeCategory, "
          + " eod.org_id, eod.is_override, od.org_name "
          + " FROM equipment_master em "
          + " JOIN equip_org_details eod on (eod.equip_id = em.eq_id) "
          + " JOIN organization_details od on (od.org_id = eod.org_id) "
          + " JOIN department dept ON (dept.dept_id = em.dept_id) ) AS foo";

  /**
   * get equipment details.
   *
   * @param parameters Map
   * @param listingParameters Map
   * @return PagedList
   */
  public static PagedList getEquipmentDetails(
      Map<String, String[]> parameters, Map<LISTING, Object> listingParameters) {

    SearchQueryAssembler qb = null;
    qb =
        new SearchQueryAssembler(
            EQUIPMENT_FIELDS, EQUIPMENT_COUNT, EQUIPMENT_FROM_TABLES, listingParameters);
    qb.addFilterFromParamMap(parameters);
    qb.addSecondarySort("eq_id");
    qb.build();
    return qb.getMappedPagedList();
  }

  private static final String EQUIPMENT_DETAILS =
      " select em.*, ec.org_id, bed_type"
          + " FROM equipment_master  em "
          + " JOIN equipement_charges  ec on ec.equip_id = em.eq_id"
          + " WHERE em.eq_id=? and ec.org_id=? ";

  /**
   * get equipment details.
   *
   * @param equipmentId String
   * @param orgId String
   * @return BasicDynaBean
   */
  public BasicDynaBean getEquipmentDetails(String equipmentId, String orgId) {
    List<BasicDynaBean> list =
        DatabaseHelper.queryToDynaList(EQUIPMENT_DETAILS, equipmentId, orgId);
    if (list.size() > 0) {
      return list.get(0);
    } else {
      return null;
    }
  }

  /**
   * Gets list of active insurance category id's for
   * Equipment Id.
   *
   * @param equipmentId Equipment Id
   * @return active insurance categories
   */
  public List<BasicDynaBean> getActiveInsuranceCategories(String equipmentId) {
    return DatabaseHelper.queryToDynaList(SELECT_INSURANCE_CATEGORY_IDS, equipmentId);
  }

  private static final String SELECT_INSURANCE_CATEGORY_IDS = "SELECT insurance_category_id "
      + "FROM equipment_insurance_category_mapping "
      + "WHERE equipment_id =?";

  private static final String GET_INSURANCE_CATEGORY_LIST =
      "SELECT insurance_category_id, insurance_category_name FROM item_insurance_categories"
          + " WHERE system_category = 'N'";

  public List<BasicDynaBean> getInsuranceCategories() {
    return DatabaseHelper.queryToDynaList(GET_INSURANCE_CATEGORY_LIST);
  }

  private static final String GET_EQUIPMENT_ITEM_SUB_GROUP_TAX_DETAILS =
      "SELECT isg.item_subgroup_id, isg.subgroup_code, isg.item_subgroup_name, ig.group_code "
          + " FROM equipment_item_sub_groups eisg "
          + " JOIN item_sub_groups isg ON(eisg.item_subgroup_id = isg.item_subgroup_id) "
          + " JOIN item_groups ig ON (ig.item_group_id = isg.item_group_id) "
          + " WHERE eisg.eq_id = ? ";

  public List<BasicDynaBean> getEquipmentItemSubGroupTaxDetails(String actDescriptionId) {
    return DatabaseHelper.queryToDynaList(
        GET_EQUIPMENT_ITEM_SUB_GROUP_TAX_DETAILS, new Object[] {actDescriptionId});
  }

  private static final String GET_TEST_ITEM_SUBGROUP_DETAILS =
      "select eisg.item_subgroup_id,isg.item_subgroup_name,ig.item_group_id, "
          + " item_group_name,igt.item_group_type_id,igt.item_group_type_name "
          + " from equipment_item_sub_groups eisg "
          + " left join item_sub_groups isg on (isg.item_subgroup_id = eisg.item_subgroup_id) "
          + " left join equipment_master em on (em.eq_id = eisg.eq_id) "
          + " left join item_groups ig on (ig.item_group_id = isg.item_group_id)"
          + " left join item_group_type igt on (igt.item_group_type_id = ig.item_group_type_id)"
          + " where eisg.eq_id = ? ";

  public List<BasicDynaBean> getEquipItemSubGroupDetails(String eqId) {
    return DatabaseHelper.queryToDynaList(GET_TEST_ITEM_SUBGROUP_DETAILS, new Object[] {eqId});
  }

  private static final String CHARGE_QUERY = "SELECT "
      + "  ec.equip_id, em.equipment_name, em.equipment_code, em.dept_id, "
      + "  em.duration_unit_minutes, em.min_duration, em.slab_1_threshold, em.incr_duration, "
      + "  em.service_sub_group_id, em.insurance_category_id, "
      + "  ec.daily_charge as charge, ec.daily_charge_discount, ec.min_charge, "
      + "  ec.min_charge_discount, ec.slab_1_charge, ec.slab_1_charge_discount, ec.incr_charge, "
      + "  ec.incr_charge_discount, ec.tax, em.duration_unit_minutes,em.slab_1_threshold,"
      + "  allow_rate_increase,allow_rate_decrease,billing_group_id "
      + "  FROM equipement_charges ec " + "  JOIN equipment_master em on(ec.equip_id = em.eq_id) "
      + "  WHERE ec.equip_id=? AND ec.bed_type =? AND ec.org_id =?";

  public BasicDynaBean getEquipmentChargesBean(String equipmentId, String bedType, String orgId) {
    return DatabaseHelper.queryToDynaBean(CHARGE_QUERY,
        new Object[] { equipmentId, bedType, orgId });
  }

  private static final String ALL_CHARGE_QUERY = "SELECT "
      + "  equip_id, daily_charge as charge, daily_charge_discount, min_charge, "
      + "  min_charge_discount, slab_1_charge, slab_1_charge_discount, incr_charge, "
      + "  incr_charge_discount, tax, bed_type"
      + "  FROM equipement_charges "
      + "  WHERE equip_id=? AND org_id =?";

  public List<BasicDynaBean> getAllEquipmentChargesBean(String equipmentId, String orgId) {
    return DatabaseHelper.queryToDynaList(ALL_CHARGE_QUERY,
        equipmentId, orgId);
  }

  /**
   * Gets the equipment charge.
   *
   * @param equipmentId the equipment id
   * @param bedType the bed type
   * @param ratePlanId the rate plan id
   * @return the equipment charge
   */
  public BasicDynaBean getEquipmentCharge(String equipmentId, String bedType, String ratePlanId) {
    BasicDynaBean equipment = getEquipmentChargesBean((String) equipmentId, bedType, ratePlanId);
    if (equipment == null) {
      equipment = getEquipmentChargesBean((String) equipmentId, "GENERAL", "ORG0001");
    }
    return equipment;
  }
}
