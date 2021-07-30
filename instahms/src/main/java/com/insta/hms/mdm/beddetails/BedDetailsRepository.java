package com.insta.hms.mdm.beddetails;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

/**
 * The Class BedDetailsRepository.
 */
@Repository
public class BedDetailsRepository extends GenericRepository {

  /**
   * Instantiates a new bed details repository.
   */
  public BedDetailsRepository() {
    super("bed_details");
  }

  /** The Constant BED_NAMES_QUERY. */
  private static final String BED_NAMES_QUERY = "SELECT b.ward_no, b.bed_type, "
      + " b.bed_name, w.ward_name, b.bed_id, b.bed_ref_id, b.avilable_date, "
      + " b.remarks, b.occupancy, b.status, b.bed_status, "
      + " bt.is_icu, bt.is_child_bed, bt.billing_bed_type, bt.insurance_category_id "
      + " FROM bed_names b " + "  JOIN ward_names w USING(ward_no) "
      + "  JOIN bed_types bt ON (bt.bed_type_name = b.bed_type) ";

  /**
   * Gets the bed details bean.
   *
   * @param actDescriptionId
   *          the act description id
   * @return the bed details bean
   */
  public BasicDynaBean getBedDetailsBean(String actDescriptionId) {
    return DatabaseHelper.queryToDynaBean(
        BED_NAMES_QUERY + " WHERE b.bed_id::varchar =? OR bt.bed_type_name=? ",
        new Object[] {actDescriptionId,actDescriptionId});
  }

  /** The Constant GET_ICU_WARD_QUERY. */
  private static final String GET_ICU_WARD_QUERY = "SELECT bn.bed_type,bn.bed_id, "
      + " bn.bed_name, bn.ward_no, ic.intensive_bed_type, ic.bed_charge, ic.hourly_charge, "
      + " ic.nursing_charge, ic.duty_charge, ic.maintainance_charge,"
      + " ic.luxary_tax,ic.initial_payment,ic.bed_charge_discount, "
      + " ic.nursing_charge_discount, ic.duty_charge_discount, "
      + " ic.maintainance_charge_discount, ic.hourly_charge_discount, "
      + " ic.daycare_slab_1_charge, ic.daycare_slab_2_charge, "
      + " ic.daycare_slab_3_charge, ic.daycare_slab_1_charge_discount, "
      + " ic.daycare_slab_2_charge_discount, ic.daycare_slab_3_charge_discount "
      + " FROM bed_names bn "
      + "  JOIN icu_bed_charges ic ON (bn.bed_type = ic.intensive_bed_type) "
      + "  JOIN ward_names w ON (bn.ward_no=w.ward_no) "
      + " WHERE ic.bed_status='A' AND w.status='A'  and bn.status='A'";

  /** The Constant GET_ICU_CHARGES_QUERY. */
  private static final String GET_ICU_CHARGES_QUERY = GET_ICU_WARD_QUERY
      + " AND bn.bed_id=? AND organization=? AND ic.bed_type=? ";

  /**
   * Gets the ICU bed charges.
   *
   * @param bedId
   *          the bed id
   * @param orgId
   *          the org id
   * @param bedType
   *          the bed type
   * @return the ICU bed charges
   */
  public BasicDynaBean getIcuBedCharges(int bedId, String orgId, String bedType) {
    if (isIcuBedType(bedType)) {
      return DatabaseHelper.queryToDynaBean(GET_ICU_CHARGES_QUERY, bedId, orgId, "GENERAL");
    } else {
      return DatabaseHelper.queryToDynaBean(GET_ICU_CHARGES_QUERY, bedId, orgId, bedType);
    }
  }

  /** The Constant IS_ICU_BED_TYPE. */

  private static final String IS_ICU_BED_TYPE = "SELECT is_icu "
      + " FROM  bed_types WHERE  bed_type_name = ?";

  /**
   * Checks if is icu bed type.
   *
   * @param bedType
   *          the bed type
   * @return true, if is icu bed type
   */
  public boolean isIcuBedType(String bedType) {
    boolean icubedType = false;
    String icuStatus = DatabaseHelper.getString(IS_ICU_BED_TYPE, bedType);
    if (icuStatus.equals("Y")) {
      icubedType = true;
    }
    return icubedType;
  }

  /** The Constant NORMAL_BED_CHARGE. */

  private static final String NORMAL_BED_CHARGE = " SELECT bed_type, organization, bed_status, "
      + " intensive_bed_status, child_bed_status, "
      + "  bed_charge, bed_charge_discount, nursing_charge, nursing_charge_discount, "
      + "  duty_charge, duty_charge_discount, maintainance_charge, maintainance_charge_discount, "
      + "  hourly_charge, hourly_charge_discount, "
      + "  daycare_slab_1_charge, daycare_slab_2_charge, daycare_slab_3_charge, "
      + "  daycare_slab_1_charge_discount, daycare_slab_2_charge_discount, "
      + " daycare_slab_3_charge_discount, " + "  initial_payment, luxary_tax, code_type, item_code "
      + " FROM bed_details where bed_type=? and organization=?";

  /**
   * Gets the normal bed charges bean.
   *
   * @param bedType
   *          the bed type
   * @param orgId
   *          the org id
   * @return the normal bed charges bean
   */
  public BasicDynaBean getNormalBedChargesBean(String bedType, String orgId) {
    return DatabaseHelper.queryToDynaBean(NORMAL_BED_CHARGE, bedType, orgId);
  }
}
