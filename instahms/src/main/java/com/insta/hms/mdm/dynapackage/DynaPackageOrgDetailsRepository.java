package com.insta.hms.mdm.dynapackage;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;

import org.springframework.stereotype.Repository;

/**
 * The Class DynaPackageOrgDetailsRepository.
 *
 * @author eshwar-chandra
 */
@Repository
public class DynaPackageOrgDetailsRepository extends GenericRepository {

  private static final String COPY_GENERAL_DETAILS_TO_ALL_ORGS =
      " INSERT INTO dyna_package_org_details (dyna_package_id, org_id,"
          + " applicable, item_code, code_type) "
          + " SELECT dp.dyna_package_id, o.org_id, dp.applicable, dp.item_code, dp.code_type  "
          + " FROM organization_details o "
          + " JOIN dyna_package_org_details dp ON (dp.dyna_package_id=? AND dp.org_id='ORG0001') "
          + " WHERE o.org_id != 'ORG0001'";

  public DynaPackageOrgDetailsRepository() {
    super("dyna_package_org_details");
  }

  public Integer copyDynaPackageDetailsToAllOrgs(int dynaPackageID) {
    return DatabaseHelper.update(COPY_GENERAL_DETAILS_TO_ALL_ORGS, new Object[] {dynaPackageID});
  }

}
