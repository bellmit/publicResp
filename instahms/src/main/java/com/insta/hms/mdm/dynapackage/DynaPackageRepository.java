package com.insta.hms.mdm.dynapackage;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.MasterRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.List;

// TODO: Auto-generated Javadoc
/** The Class DynaPackageRepository. */
@Repository
public class DynaPackageRepository extends MasterRepository<Integer> {

  /** Instantiates a new dyna package repository. */
  public DynaPackageRepository() {
    super("dyna_packages", "dyna_package_id");
  }

  /** The Constant PACKAGE_DETAILS. */
  public static final String PACKAGE_DETAILS =
      "SELECT dp.*, dod.org_id, dod.applicable, dod.item_code,"
          + " o.org_name, dod.code_type "
          + " FROM dyna_packages dp "
          + " JOIN dyna_package_org_details dod ON (dod.dyna_package_id = dp.dyna_package_id) "
          + " JOIN organization_details o ON (o.org_id = dod.org_id) "
          + " WHERE dp.dyna_package_id=? AND dod.org_id=? ";

  /**
   * Gets the dyna package details.
   *
   * @param dynaPkgId the dyna pkg id
   * @param ratePlan the rate plan
   * @return the dyna package details
   */
  public BasicDynaBean getDynaPackageDetails(int dynaPkgId, String ratePlan) {
    return DatabaseHelper.queryToDynaBean(PACKAGE_DETAILS, new Object[] {dynaPkgId, ratePlan});
  }

  /** The Constant PACKAGE_CHARGES. */
  public static final String PACKAGE_CHARGES =
      " SELECT dp.dyna_package_id, dp.status, dp.dyna_package_name, dpc.charge, "
          + " dcl.dyna_pkg_cat_id, dcat.dyna_pkg_cat_name, dcl.pkg_included, dcl.amount_limit, "
          + " dcl.qty_limit, dpo.item_code, dcat.limit_type "
          + " FROM dyna_packages dp "
          + " JOIN dyna_package_charges dpc USING (dyna_package_id) "
          + " JOIN dyna_package_category_limits dcl ON (dcl.dyna_package_id = dp.dyna_package_id "
          + " AND dcl.org_id = dpc.org_id AND dcl.bed_type = dpc.bed_type) "
          + " JOIN dyna_package_category dcat ON (dcat.dyna_pkg_cat_id = dcl.dyna_pkg_cat_id) "
          + " JOIN dyna_package_org_details dpo ON (dp.dyna_package_id = dpo.dyna_package_id  "
          + " AND dpo.org_id=?  AND dpo.applicable = true)"
          + " WHERE dp.dyna_package_id = ? AND dcl.pkg_included = 'Y' ";

  /** The Constant BED_PACKAGE_CHARGES. */
  public static final String BED_PACKAGE_CHARGES =
      PACKAGE_CHARGES + " AND dpc.bed_type=? AND dpc.org_id=? ";

  /**
   * Gets the dyna package charges.
   *
   * @param ratePlan the rate plan
   * @param bedType the bed type
   * @param dynaPkgId the dyna pkg id
   * @return the dyna package charges
   */
  public List<BasicDynaBean> getDynaPackageCharges(String ratePlan, String bedType, int dynaPkgId) {
    return DatabaseHelper.queryToDynaList(
        BED_PACKAGE_CHARGES, new Object[] {ratePlan,dynaPkgId, bedType, ratePlan});
  }
}
