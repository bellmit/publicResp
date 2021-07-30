package com.insta.hms.core.clinical.order.beditems;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.core.clinical.order.master.OrderItemRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

@Repository
public class BedOrderItemRepository extends OrderItemRepository {

  public BedOrderItemRepository() {
    super("bed_types");
  }

  private static final String BED_TYPES_QUERY = " SELECT CASE WHEN (is_icu = 'N') THEN 'Bed' "
      + "  ELSE 'ICU' END AS type, "
      + "  bed_type_name AS id, bed_type_name AS name, null AS code, '' AS department, "
      + "  s.service_sub_group_id as subGrpId, s.service_group_id as groupid, "
      + "  'N' as prior_auth_required, bt.insurance_category_id,false as conduction_applicable, "
      + "  false as conducting_doc_mandatory, "
      + "  false as results_entry_applicable,'N' as tooth_num_required, "
      + "  false as multi_visit_package, -1 as center_id, '-1' as tpa_id,  "
      + "  'N' as mandate_additional_info, '' as additional_info_reqts " + " FROM bed_types bt "
      + "  JOIN chargehead_constants on ( chargehead_id = 'BBED' ) "
      + "  JOIN service_sub_groups s using(service_sub_group_id)";

  public List<BasicDynaBean> getItemDetails(List<Object> entityIdList) {
    return super.getItemDetails(BED_TYPES_QUERY, entityIdList, "bt.bed_type_name", null);
  }

  private static final String NORMAL_BED_CHARGE = " SELECT bed_type, organization, bed_status, "
      + "  intensive_bed_status, child_bed_status, "
      + "  bed_charge as charge, bed_charge, bed_charge_discount, nursing_charge, "
      + "  nursing_charge_discount, duty_charge, duty_charge_discount, maintainance_charge, "
      + "  maintainance_charge_discount, hourly_charge, hourly_charge_discount, "
      + "  daycare_slab_1_charge, daycare_slab_2_charge, daycare_slab_3_charge, "
      + "  daycare_slab_1_charge_discount, daycare_slab_2_charge_discount, "
      + "  daycare_slab_3_charge_discount, "
      + "  initial_payment, luxary_tax, code_type, item_code, billing_group_id "
      + " FROM bed_details bd JOIN bed_types bt on (bt.bed_type_name = bd.bed_type) "
      + " where bed_type=? and organization=?";

  public BasicDynaBean getNormalBedChargesBean(String bedType, String orgId) {
    return DatabaseHelper.queryToDynaBean(NORMAL_BED_CHARGE, new Object[] { bedType, orgId });
  }

  private static final String ALL_NORMAL_BED_CHARGE = " SELECT bed_type, organization, bed_status, "
      + "  bed_charge as charge, nursing_charge, initial_payment, duty_charge, maintainance_charge,"
      + "  luxary_tax, hourly_charge"
      + "  daycare_slab_1_charge, daycare_slab_2_charge, daycare_slab_3_charge, "
      + "  daycare_slab_1_charge_discount, daycare_slab_2_charge_discount, "
      + "  daycare_slab_3_charge_discount "
      + " FROM bed_details "
      + " where organization=?";

  public List<BasicDynaBean> getAllNormalBedChargesBean(String orgId) {
    return DatabaseHelper.queryToDynaList(ALL_NORMAL_BED_CHARGE, orgId);
  }

  private static final String ICU_BED_CHARGE = " SELECT bed_type, organization, "
      + "  bed_status, intensive_bed_type, "
      + "  bed_charge as charge, bed_charge, bed_charge_discount, nursing_charge, "
      + "  nursing_charge_discount, duty_charge, duty_charge_discount, maintainance_charge, "
      + "  maintainance_charge_discount, hourly_charge, hourly_charge_discount, "
      + "  daycare_slab_1_charge, daycare_slab_2_charge, daycare_slab_3_charge, "
      + "  daycare_slab_1_charge_discount, daycare_slab_2_charge_discount, "
      + "  daycare_slab_3_charge_discount, "
      + "  luxary_tax, initial_payment, code_type, item_code, billing_group_id "
      + " FROM icu_bed_charges bd JOIN bed_types bt on (bt.bed_type_name = bd.bed_type)"
      + " where bed_type=? and organization=? and intensive_bed_type=?";

  public BasicDynaBean getIcuBedChargesBean(String previousBedType, String orgId, String bedType) {
    return DatabaseHelper.queryToDynaBean(ICU_BED_CHARGE,
        new Object[] { previousBedType, orgId, bedType });
  }

  private static final String ALL_ICU_BED_CHARGE = " SELECT bed_type, organization, "
      + "  bed_status, intensive_bed_type, "
      + "  bed_charge as charge, bed_charge, bed_charge_discount, nursing_charge, "
      + "  nursing_charge_discount, duty_charge, duty_charge_discount, maintainance_charge, "
      + "  maintainance_charge_discount, hourly_charge, hourly_charge_discount, "
      + "  daycare_slab_1_charge, daycare_slab_2_charge, daycare_slab_3_charge, "
      + "  daycare_slab_1_charge_discount, daycare_slab_2_charge_discount, "
      + "  daycare_slab_3_charge_discount, "
      + "  luxary_tax, initial_payment, code_type, item_code, billing_group_id "
      + " FROM icu_bed_charges bd JOIN bed_types bt on (bt.bed_type_name = bd.bed_type)"
      + " where intensive_bed_type=? and organization=?";

  public List<BasicDynaBean> getAllIcuBedChargesBean(String bedType, String orgId) {
    return DatabaseHelper.queryToDynaList(ALL_ICU_BED_CHARGE,
        bedType, orgId);
  }

  @Override
  public String getPackageRefQuery() {
    return null;
  }

  @Override
  public String getOperationRefQuery() {
    return null;
  }

  @Override
  public List<BasicDynaBean> getPackageRefOrders(String visitId, List<Integer> prescriptionIdList) {
    return Collections.emptyList();
  }

}
