package com.insta.hms.mdm.equipmentcharges;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.bulk.BulkDataRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class EquipmentChargesRepository extends BulkDataRepository<String> {

  public EquipmentChargesRepository() {
    super("equipement_charges", "equip_id");
    // TODO Auto-generated constructor stub
  }

  private static final String GET_ALL_CHARGES_FOR_ORG_BEDTYPES =
      " SELECT equip_id, bed_type, org_id, daily_charge, min_charge, "
          + " incr_charge ,slab_1_charge, tax, "
          + " daily_charge_discount, min_charge_discount, "
          + " incr_charge_discount, slab_1_charge_discount  "
          + " FROM equipement_charges ";

  /**
   * get all charges for bed types.
   *
   * @param orgId String
   * @param ids List
   * @return List
   */
  public List<BasicDynaBean> getAllChargesForBedtypes(String orgId, List<String> ids) {

    boolean first;
    StringBuilder queryBuilder = new StringBuilder();
    queryBuilder.append(GET_ALL_CHARGES_FOR_ORG_BEDTYPES);
    queryBuilder.append(" WHERE org_id = ?");
    if (ids != null) {
      queryBuilder.append(" AND equip_id IN ('");
      first = true;
      for (String id : ids) {
        if (first) {
          queryBuilder.append(id);
          first = false;
        } else {
          queryBuilder.append("','" + id);
        }
      }
      queryBuilder.append("')");
    }
    return DatabaseHelper.queryToDynaListWithCase(queryBuilder.toString(), orgId);
  }

  private static String INIT_ITEM_ORG_DETAILS =
      "INSERT INTO equip_org_details "
          + " (equip_id, org_id, applicable, base_rate_sheet_id, is_override) "
          + " ( SELECT ?, od.org_id, true, prspv.base_rate_sheet_id, "
          + " 'N' FROM organization_details od "
          + " LEFT OUTER JOIN priority_rate_sheet_parameters_view prspv "
          + " ON od.org_id = prspv.org_id ) ";

  private static String INIT_ITEM_CHARGES =
      "INSERT INTO equipement_charges(equip_id,org_id,bed_type,"
          + "daily_charge, min_charge, incr_charge)"
          + "(SELECT ?, abov.org_id, abov.bed_type, 0.0, 0.0, 0.0 FROM all_beds_orgs_view abov) ";

  /**
   * initialize item charges.
   *
   * @param equipmentId String
   * @return boolean
   */
  public boolean initItemCharges(String equipmentId) {
    boolean status = false;
    status =
        initItemCharges(
            INIT_ITEM_ORG_DETAILS, INIT_ITEM_CHARGES, equipmentId); // no username field here
    return status;
  }

  private boolean initItemCharges(
      String initExclusionsQuery, String initChargesQuery, String equipmentId) {

    boolean status = false;
    if (null != initExclusionsQuery) {
      if (DatabaseHelper.insert(initExclusionsQuery, equipmentId) > 0) {
        status = true;
      }
    }
    if (null != initChargesQuery) {
      if (DatabaseHelper.insert(initChargesQuery, equipmentId) > 0) {
        status = true;
      }
    }
    return status;
  }

  private static final String GET_DERIVED_RATE_PALN_DETAILS =
      "select rp.org_id,od.org_name, "
          + " case when rate_variation_percent<0 "
          + " then 'Decrease By' else 'Increase By' end as discormarkup, "
          + " rate_variation_percent,round_off_amount,eod.applicable, "
          + " eod.equip_id,rp.base_rate_sheet_id,eod.is_override "
          + " from rate_plan_parameters rp "
          + " join organization_details od on(od.org_id=rp.org_id) "
          + " join equip_org_details eod on (eod.org_id = rp.org_id) "
          + " where rp.base_rate_sheet_id =?  and equip_id=? and eod.base_rate_sheet_id=? ";

  public static List<BasicDynaBean> getDerivedRatePlanDetails(String baseSheetId, String opId) {
    return getDerivedRatePlanDetails(baseSheetId, "equipment", opId, GET_DERIVED_RATE_PALN_DETAILS);
  }

  protected static List<BasicDynaBean> getDerivedRatePlanDetails(
      String baseRateSheetId, String category, String categoryIdValue, String query) {

    if (category.equals("consultation")
        || category.equals("packages")
        || category.equals("dynapackages")) {
      return DatabaseHelper.queryToDynaList(
          query, baseRateSheetId, Integer.parseInt(categoryIdValue), baseRateSheetId);
    } else {
      return DatabaseHelper.queryToDynaList(
          query, baseRateSheetId, categoryIdValue, baseRateSheetId);
    }
  }

  /**
   * update equipment charges.
   *
   * @param ecbean BasicDynaBean
   * @param keys Map
   * @return boolean
   */
  public boolean updateEquipCharge(BasicDynaBean ecbean, Map keys) {
    int result = update(ecbean, keys);
    if (result > 0) {
      return true;
    } else {
      return false;
    }
  }

  private static final String GET_RATE_PLAN_PARAMETERS =
      "SELECT * FROM rate_plan_parameters WHERE base_rate_sheet_id = ? AND org_id = ? ";

  /**
   * Update charges for derived rate plane.
   *
   * @param baseRateSheetId String
   * @param ratePlanIds String[]
   * @param bedType String[]
   * @param dailyChg String[]
   * @param minChg String[]
   * @param incrChg String[]
   * @param slabChg String[]
   * @param equipId String
   * @param dailyDisc String[]
   * @param minDisc String[]
   * @param incrDisc String[]
   * @param slabDisc String[]
   * @param tax String[]
   * @return boolean
   * @throws Exception Exception
   */
  public boolean updateChargesForDerivedRatePlans(
      String baseRateSheetId,
      String[] ratePlanIds,
      String[] bedType,
      Double[] dailyChg,
      Double[] minChg,
      Double[] incrChg,
      Double[] slabChg,
      String equipId,
      Double[] dailyDisc,
      Double[] minDisc,
      Double[] incrDisc,
      Double[] slabDisc,
      Double[] tax)
      throws Exception {

    boolean success = true;
    for (int i = 0; i < ratePlanIds.length; i++) {
      BasicDynaBean bean =
          DatabaseHelper.queryToDynaBean(GET_RATE_PLAN_PARAMETERS, baseRateSheetId, ratePlanIds[i]);
      int variation = (Integer) bean.get("rate_variation_percent");
      int roundoff = (Integer) bean.get("round_off_amount");

      List<BasicDynaBean> chargeList = new ArrayList();
      boolean overrided = isChargeOverrided(ratePlanIds[i], equipId);
      if (!overrided) {
        for (int k = 0; k < bedType.length; k++) {

          BasicDynaBean charge = getBean();
          charge.set("equip_id", equipId);
          charge.set("org_id", ratePlanIds[i]);
          charge.set("bed_type", bedType[k]);

          Double dcharge = calculateCharge(dailyChg[k], new Double(variation), roundoff);
          Double mcharge = calculateCharge(minChg[k], new Double(variation), roundoff);
          Double icharge = calculateCharge(incrChg[k], new Double(variation), roundoff);
          Double scharge = calculateCharge(slabChg[k], new Double(variation), roundoff);

          Double ddisc = calculateCharge(dailyDisc[k], new Double(variation), roundoff);
          Double mdisc = calculateCharge(minDisc[k], new Double(variation), roundoff);
          Double idisc = calculateCharge(incrDisc[k], new Double(variation), roundoff);
          Double sdisc = calculateCharge(slabDisc[k], new Double(variation), roundoff);

          charge.set("daily_charge", new BigDecimal(dcharge));
          charge.set("min_charge", new BigDecimal(mcharge));
          charge.set("incr_charge", new BigDecimal(icharge));
          charge.set("slab_1_charge", new BigDecimal(scharge));

          charge.set("daily_charge_discount", new BigDecimal(ddisc));
          charge.set("min_charge_discount", new BigDecimal(mdisc));
          charge.set("incr_charge_discount", new BigDecimal(idisc));
          charge.set("slab_1_charge_discount", new BigDecimal(sdisc));
          charge.set("tax", new BigDecimal(tax[k]));

          chargeList.add(charge);
        }
      }
      for (BasicDynaBean ecbean : chargeList) {
        Map keys = new HashMap();
        keys.put("equip_id", ecbean.get("equip_id"));
        keys.put("org_id", ecbean.get("org_id"));
        keys.put("bed_type", ecbean.get("bed_type"));
        success = updateEquipCharge(ecbean, keys);
      }
    }
    return success;
  }

  private static final String GET_ORG_DETAILS =
      "SELECT * FROM equip_org_details WHERE org_id = ? AND equip_id = ? ";

  /**
   * charge Overrided.
   *
   * @param ratePlanId String
   * @param categoryIdValue String
   * @return boolean
   */
  public boolean isChargeOverrided(String ratePlanId, String categoryIdValue) {

    BasicDynaBean bean =
        DatabaseHelper.queryToDynaBean(GET_ORG_DETAILS, ratePlanId, categoryIdValue);
    String override = (String) bean.get("is_override");

    if (override.equals("Y")) {
      return true;
    } else {
      return false;
    }
  }

  protected Double calculateCharge(Double rsCharge, Double variance, int roundOff)
      throws Exception {
    Double charge = 0.00;
    charge = rsCharge + (rsCharge * variance) / 100;
    if (roundOff != 0) {
      Double xx = new Double(roundOff) / 2;
      xx = charge + xx;
      xx = xx / roundOff;
      int kk = xx.intValue();
      charge = roundOff * new Double(kk);
    }
    return charge;
  }
}
