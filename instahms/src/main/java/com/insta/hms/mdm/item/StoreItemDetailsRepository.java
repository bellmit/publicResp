package com.insta.hms.mdm.item;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.MasterRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The Class StoreItemDetailsRepository.
 *
 * @author irshadmohammed
 */
@Repository
public class StoreItemDetailsRepository extends MasterRepository<Integer> {

  /**
   * Instantiates a new store item details repository.
   */
  public StoreItemDetailsRepository() {
    super("store_item_details", "medicine_id", "medicine_name");
  }

  /**
   * The get item group details.
   */
  private static final String GET_ITEM_GROUP_DETAILS = " SELECT foo.*, sum(foo.tax_rate) over "
      + "(partition by foo.group_code) as agg_tax_rate " + " FROM ( SELECT isg"
      + ".item_subgroup_name, isg.item_subgroup_id, ig.group_code, " + " ig.item_group_id, "
      + "isg.item_subgroup_display_order, ig.item_group_name, isg.status, " + " isgtd"
      + ".tax_rate, isgtd.validity_start,isgtd.validity_end  " + " FROM store_item_sub_groups"
      + " sisg " + " JOIN item_sub_groups isg ON (isg.item_subgroup_id = sisg"
      + ".item_subgroup_id) " + " JOIN item_groups ig ON (isg.item_group_id = ig"
      + ".item_group_id) " + " LEFT JOIN item_sub_groups_tax_details isgtd " + "  ON (isgtd"
      + ".item_subgroup_id = isg.item_subgroup_id) " + " WHERE medicine_id = ? AND ig"
      + ".status='A' AND isg.status='A') AS foo";

  /**
   * Gets the subgroups.
   *
   * @param medicineId
   *          the medicine id
   * @return the subgroups
   */
  public List<BasicDynaBean> getSubgroups(int medicineId) {
    return DatabaseHelper.queryToDynaList(GET_ITEM_GROUP_DETAILS, new Object[] { medicineId });
  }
  
  
  private static final String GET_ITEM_GROUP_DETAILS_FROM_STORE_TARIFF = " SELECT foo.*, "
      + " sum(foo.tax_rate) over (partition by foo.group_code) as agg_tax_rate FROM ( SELECT isg "
      + " .item_subgroup_name, isg.item_subgroup_id, ig.group_code, "
      + " ig.item_group_id, isg.item_subgroup_display_order, ig.item_group_name, isg.status, "
      + " isgtd.tax_rate, isgtd.validity_start,isgtd.validity_end "
      + " FROM store_tariff_item_sub_groups stisg "
      + " JOIN item_sub_groups isg ON (isg.item_subgroup_id = "
      + " stisg.item_subgroup_id  AND store_rate_plan_id = ?) "
      + " JOIN item_groups ig ON (isg.item_group_id = ig.item_group_id) "
      + " LEFT JOIN item_sub_groups_tax_details isgtd "
      + " ON (isgtd.item_subgroup_id = isg.item_subgroup_id)  WHERE item_id = ? AND "
      + " ig.status='A' AND isg.status='A' ) AS foo";


  
  /**
   * Gets the subgroups from store tariff.
   *
   * @param medicineId
   *          the medicine id
   * @param storeRatePlanId
   *          the storeRatePlanId
   * @return the subgroups
   */
  public List<BasicDynaBean> getSubgroups(int medicineId, int storeRatePlanId) {
    return DatabaseHelper.queryToDynaList(GET_ITEM_GROUP_DETAILS_FROM_STORE_TARIFF,
        new Object[] {storeRatePlanId, medicineId});
  }
  
  /**
   * The get item sub group details from store tariff level.
   */
  private static final String GET_ITEM_GROUP_DETAILS_FOR_STORE_TARIFF = 
      " SELECT foo.*, sum(foo.tax_rate) over "
      + "(partition by foo.group_code) as agg_tax_rate " + " FROM ( SELECT isg"
      + ".item_subgroup_name, isg.item_subgroup_id, ig.group_code, " + " ig.item_group_id, "
      + "isg.item_subgroup_display_order, ig.item_group_name, isg.status, " + " isgtd"
      + ".tax_rate, isgtd.validity_start,isgtd.validity_end  "
      + " FROM store_tariff_item_sub_groups" + " stisg "
      + " JOIN item_sub_groups isg ON (isg.item_subgroup_id = stisg"
      + ".item_subgroup_id and stisg.store_rate_plan_id = ? ) "
      + " JOIN item_groups ig ON (isg.item_group_id = ig" + ".item_group_id) "
      + " LEFT JOIN item_sub_groups_tax_details isgtd " + "  ON (isgtd"
      + ".item_subgroup_id = isg.item_subgroup_id) " + " WHERE item_id = ? AND ig"
      + ".status='A' AND isg.status='A') AS foo";

  /**
   * Gets the subgroups.
   *
   * @param itemId
   *          the item id
   * @param storeRatePlanId
   *          the store rate plan id
   * @return the subgroups
   */
  public List<BasicDynaBean> getStoreTariffSubgroups(int itemId, int storeRatePlanId) {
    return DatabaseHelper.queryToDynaList(GET_ITEM_GROUP_DETAILS_FOR_STORE_TARIFF,
        new Object[] { storeRatePlanId, itemId });
  }
  
  private static final String GET_ITEM_SUBGROUPS = "SELECT * "
      + " FROM item_sub_groups isg "
      + " JOIN store_item_sub_groups sisg ON(isg.item_subgroup_id = sisg.item_subgroup_id)"
      + " JOIN item_groups ig ON (ig.item_group_id = isg.item_group_id) "
      + " where medicine_id = ? AND isg.status = 'A'";
  
  public List<BasicDynaBean> getMedicineSubgroups(int medicineId) {
    return DatabaseHelper.queryToDynaList(GET_ITEM_SUBGROUPS, new Object[] { medicineId });
  }

  /**
   * The get item category discount details.
   */
  private static final String GET_ITEM_CATEGORY_DISCOUNT_DETAILS = "SELECT medicine_name, sid"
      + ".medicine_id, issue_base_unit, package_type, med_category_id, " + " category, "
      + "discount, claimable, billable, retailable, insurance_category_id ," + " issue_units,"
      + " package_uom " + " FROM store_item_details sid " + " JOIN store_category_master scm "
      + "ON (sid.med_category_id = scm.category_id) " + " WHERE sid.medicine_id =?";

  /**
   * Gets the item category discount details.
   *
   * @param medicineId
   *          the medicine id
   * @return the item category discount details
   */
  public BasicDynaBean getItemCategoryDiscountDetails(int medicineId) {
    return DatabaseHelper.queryToDynaBean(GET_ITEM_CATEGORY_DISCOUNT_DETAILS,
        new Object[] { medicineId });
  }

  private static final String GET_REMAINING_QUANTITY = "select (total_qty-ing.issue_qty) as "
      + "qty,grn_no,invoice_no,supplier_id,billable,supplier_invoice_id " + " from "
      + "store_grn_details ing join store_grn_main using (grn_no)" + " join store_invoice "
      + "using(supplier_invoice_id)" + " join store_item_details using(medicine_id) join "
      + "store_category_master on (category_id=med_category_id)" + " where medicine_id=? and "
      + "batch_no=? and (total_qty-ing.issue_qty) > 0 order by grn_date asc";

  /**
   * Gets the remaining quantity.
   *
   * @param medicineId the medicine id
   * @param batchNo the batch no
   * @return the remaining quantity
   */
  public List<BasicDynaBean> getRemainingQuantity(Integer medicineId, String batchNo) {
    return DatabaseHelper.queryToDynaList(GET_REMAINING_QUANTITY, 
        new Object[]{ medicineId, batchNo });
  }

  private static final String GET_ITEM_LOT_DETAILS = "SELECT il.*, sid.issue_base_unit " + " "
      + "FROM store_item_lot_details il " + " JOIN store_item_batch_details ibd USING "
      + "(item_batch_id) " + " JOIN store_item_details sid USING (medicine_id) " + " WHERE "
      + "item_lot_id=?";

  public BasicDynaBean getItemLotDetails(int itemLotId) {
    return DatabaseHelper.queryToDynaBean(GET_ITEM_LOT_DETAILS, new Object[] { itemLotId });
  }

  public static String GET_MEDICINE_DETAILS = " SELECT pmd.medicine_name, pmd.medicine_id,pmd"
      + ".cust_item_code, coalesce(gm.generic_name,'') as generic_name, " + "   pmd"
      + ".composition, pmd.therapatic_use, pmd.status, pmd.issue_base_unit, sic"
      + ".control_type_name, value, " + " mm.manf_name, mm.manf_mnemonic, pmd.package_type, "
      + "pmd.issue_units, pmd.medicine_short_name,pmd.cons_uom_id,cum.consumption_uom," + " pmd"
      + ".consumption_capacity, icm.category AS category_name, icm.claimable, pmd"
      + ".med_category_id, mm.manf_code, max_cost_price, " + " supplier_name, "
      + "obsolete_preferred_supplier, invoice_details, " + " pmd.service_sub_group_id, ssg"
      + ".service_group_id,item_barcode_id,pmd.control_type_id," + " sic.control_type_name, "
      + "pmd.insurance_category_id, route_of_admin, prior_auth_required, pmd.package_uom, "
      + " item_form_id, item_strength, item_strength_units, identification,tax_type,tax_rate,"
      + "bin,batch_no_applicable,item_selling_price, high_cost_consumable " + " FROM "
      + "store_item_details pmd " + "   LEFT OUTER JOIN generic_name gm on gm"
      + ".generic_code=pmd.generic_name " + "   LEFT OUTER JOIN manf_master mm on mm"
      + ".manf_code=pmd.manf_name " + "   LEFT OUTER JOIN store_category_master icm on pmd"
      + ".med_category_id=icm.category_id " + "   LEFT JOIN service_sub_groups ssg ON (ssg"
      + ".service_sub_group_id = pmd.service_sub_group_id) " + "   LEFT JOIN "
      + "store_item_controltype sic ON (sic.control_type_id = pmd.control_type_id) "
      + "LEFT JOIN consumption_uom_master cum ON (pmd.cons_uom_id = cum.cons_uom_id) "
      + "WHERE pmd.medicine_id=?";

  public BasicDynaBean getMedicineDetails(Integer medicineId) {
    return DatabaseHelper.queryToDynaBean(GET_MEDICINE_DETAILS, new Object[]{ medicineId });
  }

  private static final String GET_MEDICINE_BY_IDS =
      "Select * from store_item_details where medicine_id in (:ids)";

  /**
   * Gets the medicinesRecords by ids.
   *
   * @param medicineIds the medicine ids
   * @return the medicines
   */
  public List<BasicDynaBean> getMedicinesByIds(List<Integer> medicineIds) {
    MapSqlParameterSource parameters = new MapSqlParameterSource();
    parameters.addValue("ids", medicineIds);
    return DatabaseHelper.queryToDynaList(GET_MEDICINE_BY_IDS, parameters);
  }

  private static final String GET_ITEMS_BY_QUERY = "SELECT sid.medicine_name, "
      + "sid.medicine_id, mm.manf_name, sid.cons_uom_id "
      + "FROM store_item_details sid "
      + "LEFT JOIN manf_master mm ON (sid.manf_name = mm.manf_code) "
      + "JOIN store_category_master scm ON (sid.med_category_id = scm.category_id) "
      + "WHERE  scm.is_drug='Y' AND (medicine_name ILIKE ? OR medicine_name ILIKE ? "
      + "OR medicine_name ILIKE ? ) "
      + "LIMIT 50";
  
  /**
   * Fetch medicine items for given search query.
   * 
   * @param stringInput the string to be searched for
   * @return the list of medicine items
   */
  public List<BasicDynaBean> getItemsBySearchString(String stringInput) {
    return DatabaseHelper.queryToDynaList(GET_ITEMS_BY_QUERY, new Object[] {stringInput + "%",
        "%" + stringInput + "%", "%" + stringInput});
  }
}
