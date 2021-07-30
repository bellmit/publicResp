package com.insta.hms.mdm.dynapackage;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;

import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

// TODO: Auto-generated Javadoc

/**
 * The Class DynaPackageChargesRepository.
 *
 * @author eshwar-chandraDynaPackageService.java
 */
@Repository
public class DynaPackageChargesRepository extends GenericRepository {

  /**
   * Copy the GENERAL rate plan charges to all other rate plans. Assumes that the other
   * rate-plan charges are non-existent, ie, no updates only inserts new charges.
   */
  private static final String COPY_GENERAL_CHARGES_TO_ALL_ORGS =
      " INSERT INTO dyna_package_charges (org_id, bed_type, dyna_package_id, charge, username) "
          + " SELECT abo.org_id, abo.bed_type, dpc.dyna_package_id, dpc.charge, dpc.username "
          + " FROM all_beds_orgs_view abo "
          + " JOIN dyna_package_charges dpc ON (dpc.dyna_package_id=?"
          + " AND dpc.bed_type = abo.bed_type "
          + " AND dpc.org_id = 'ORG0001') "
          + " WHERE abo.org_id != 'ORG0001' ";
  /**
   * The Constant UPDATE_RATEPLAN_DYNAPKG_CHARGES_BY.
   */
  private static final String UPDATE_RATEPLAN_DYNAPKG_CHARGES_BY =
      "UPDATE dyna_package_charges totab SET "
          + " charge = doroundvarying(fromtab.charge,?,?) "
          + " FROM dyna_package_charges fromtab"
          + " WHERE totab.org_id = ? AND fromtab.org_id = ?"
          + " AND totab.dyna_package_id = fromtab.dyna_package_id "
          + " AND totab.bed_type = fromtab.bed_type AND totab.is_override='N'";
  /**
   * The Constant UPDATE_DYNAPKG_CHARGES_BY.
   */
  private static final String UPDATE_DYNAPKG_CHARGES_BY = "UPDATE dyna_package_charges totab SET "
      + " charge = doroundvarying(fromtab.charge,?,?) "
      + " FROM dyna_package_charges fromtab"
      + " WHERE totab.org_id = ? AND fromtab.org_id = ?"
      + " AND totab.dyna_package_id = fromtab.dyna_package_id AND totab.bed_type = fromtab.bed_type"
      + " AND totab.is_override='N'";

  /*
   * Instantiates a new dyna package charges repository.
   */
  public DynaPackageChargesRepository() {
    super("dyna_package_charges");
  }

  /**
   * Copy general charges to all orgs.
   *
   * @param dynaPackageId the dyna package id
   * @return the integer
   */
  public Integer copyGeneralChargesToAllOrgs(int dynaPackageId) {
    return DatabaseHelper.update(COPY_GENERAL_CHARGES_TO_ALL_ORGS, new Object[] {dynaPackageId});
  }

  /**
   * Update charges based on new rate sheet.
   *
   * @param orgId              the org id
   * @param varianceBy         the variance by
   * @param baseOrgId          the base org id
   * @param nearstRoundOfValue the nearst round of value
   * @param dynaPackageId      the dyna package id
   * @return true, if successful
   */
  public boolean updateChargesBasedOnNewRateSheet(String orgId, Double varianceBy,
      String baseOrgId, Double nearstRoundOfValue, String dynaPackageId) {
    boolean success = false;
    success = DatabaseHelper
        .update(UPDATE_RATEPLAN_DYNAPKG_CHARGES_BY + " AND totab.dyna_package_id = ? ",
            new BigDecimal(varianceBy), new BigDecimal(nearstRoundOfValue), orgId,
            baseOrgId, Integer.parseInt(dynaPackageId)) > 0;
    return success;
  }

  /**
   * Update dyna package charges.
   *
   * @param values the values
   * @return the integer
   */
  public Integer updateDynaPackageCharges(Object[] values) {
    return DatabaseHelper
        .update(UPDATE_DYNAPKG_CHARGES_BY + " AND totab.dyna_package_id = ? ", values);
  }
}
