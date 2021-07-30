package com.insta.hms.mdm.packageitemcharges;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * TODO: Query can be clubbed.
 *
 * @author ritolia.
 */
@Repository
public class PackageItemChargesRepository extends GenericRepository {

  private static final String GET_MULTI_VISIT_PACKAGE_ITEM_CAHRGE =
      " SELECT charge FROM package_contents pc"
      + " JOIN package_content_charges pcc"
      + " ON (package_contents.package_content_id = package_content_charges.package_content_id)"
        + " where pcc.org_id = ? "
          + " AND pcc.bed_type = ? AND pc.package_id = ? AND pc.package_content_id = ?";

  private static final String ITEM_TOTAL_QTY =
      "SELECT activity_qty FROM package_contents WHERE package_id = ?  AND activity_id= ? ";
  private static final String PACKAGE_OBJ_ID =
      "SELECT pack_ob_id FROM package_contents WHERE package_id = ?  AND activity_id= ? ";
  private static final String PACKAGE_OBJ_ID_CONSULTATION =
      "SELECT pack_ob_id FROM package_contents WHERE package_id = ?  AND activity_id= ? "
          + " AND consultation_type_id = ?";
  private static final String ITEM_TOTAL_QTY_CONSULTATION =
      "SELECT activity_qty FROM package_contents WHERE package_id = ?  AND activity_id= ? "
          + " AND consultation_type_id = ?";

  private static final String GET_MULTI_VISIT_PACKAGE =
      "SELECT pcc.*, pm.billing_group_id FROM package_contents pc "
          + " JOIN packages pm ON (pm.package_id = pc.package_id ) "
          + " JOIN package_content_charges pcc ON (pcc.package_content_id = pc.package_content_id)"
          + " where pcc.org_id = ? "
          + " AND pcc.bed_type = ? AND pc.package_id = ? AND pcc.package_content_id = ?";

  public PackageItemChargesRepository() {
    super("package_content_charges");
  }

  /**
   * @param packageId.
   * @param packObId.
   * @param bedType.
   * @param orgId.
   */
  public BasicDynaBean getMultiVisitPackage(Integer packageId, Integer packObId, String bedType,
      String orgId) {
    Object[] params = {orgId, bedType, packageId, packObId};
    return DatabaseHelper.queryToDynaBean(GET_MULTI_VISIT_PACKAGE, params);
  }

  /**
   * @param packageId.
   * @param itemBean.
   * @param itemId.
   * @param bedType.
   * @param orgId.
   * @param itemType.
   * @param qty.
   * @return BigDecimal
   */
  public BigDecimal getMultiVisitPackageItemCharge(
      String packageId,
      BasicDynaBean itemBean,
      String itemId,
      String bedType,
      String orgId,
      String itemType,
      BigDecimal qty) {

    String activityId;
    BasicDynaBean resultBean;
    BigDecimal charge = BigDecimal.ZERO;
    Object[] params = {Integer.parseInt(packageId), itemId};

    Integer itemTotalQtyInt = DatabaseHelper.getInteger(ITEM_TOTAL_QTY, params);
    String packObId = DatabaseHelper.getString(PACKAGE_OBJ_ID, params);
    if (itemType.equals("doctor")) {
      activityId = (String) itemBean.get("doctor_name");
      Object[] consultationparams = {
        Integer.parseInt(packageId), activityId, Integer.parseInt(itemId)
      };
      packObId = DatabaseHelper.getString(PACKAGE_OBJ_ID_CONSULTATION, consultationparams);

      itemTotalQtyInt = DatabaseHelper.getInteger(ITEM_TOTAL_QTY_CONSULTATION, consultationparams);
      if (packObId == null) {
        activityId = "Doctor";
        Object[] consultationparams1 = {
          Integer.parseInt(packageId), activityId, Integer.parseInt(itemId)
        };
        packObId = DatabaseHelper.getString(PACKAGE_OBJ_ID_CONSULTATION, consultationparams1);
        itemTotalQtyInt =
            DatabaseHelper.getInteger(PACKAGE_OBJ_ID_CONSULTATION, consultationparams1);
      }
    }

    BigDecimal itemTotalQtyNumeric = BigDecimal.ZERO;
    itemTotalQtyNumeric =
        itemTotalQtyInt == null ? BigDecimal.ZERO : new BigDecimal(itemTotalQtyInt);
    Object[] params1 = {orgId, bedType, Integer.parseInt(packageId), packObId};
    resultBean = DatabaseHelper.queryToDynaBean(GET_MULTI_VISIT_PACKAGE_ITEM_CAHRGE, params1);

    if (resultBean != null) {
      charge = (BigDecimal) resultBean.get("charge");
    }

    if (resultBean == null) {
      Object[] params2 = {"ORG0001", "GENERAL", Integer.parseInt(packageId), packObId};
      charge = DatabaseHelper.getBigDecimal(GET_MULTI_VISIT_PACKAGE_ITEM_CAHRGE, params2);
    }
    charge =
        charge.compareTo(BigDecimal.ZERO) != 0
            ? charge.divide(itemTotalQtyNumeric, RoundingMode.CEILING).multiply(qty)
            : BigDecimal.ZERO;

    return charge;
  }
}
