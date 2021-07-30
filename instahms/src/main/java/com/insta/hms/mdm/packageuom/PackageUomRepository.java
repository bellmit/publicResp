package com.insta.hms.mdm.packageuom;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The Class PackageUomRepository.
 * TODO: Not extending MasterRepository because of composite primary key.
 */
@Repository
public class PackageUomRepository extends GenericRepository {

  /**
   * Instantiates a new package uom repository.
   */
  public PackageUomRepository() {
    super("package_issue_uom");
  }

  /** The Constant GET_DISTINCT_PACK_ISSUES_UOM. */
  private static final String GET_DISTINCT_PACK_ISSUES_UOM = "SELECT distinct issue_uom, "
      + "package_uom, package_size FROM package_issue_uom";

  /**
   * Gets the distinct package isse.
   *
   * @return the distinct package isse
   */
  public List<BasicDynaBean> getDistinctPackageIsse() {
    return DatabaseHelper.queryToDynaList(GET_DISTINCT_PACK_ISSUES_UOM);
  }

  /** The Constant GET_DISTINCT_ISSUE_UOM. */
  private static final String GET_DISTINCT_ISSUE_UOM = "SELECT DISTINCT issue_uom FROM "
      + "package_issue_uom ORDER BY issue_uom";

  /**
   * Gets the distinct issue uom.
   *
   * @return the distinct issue uom
   */
  public List<BasicDynaBean> getDistinctIssueUom() {
    return DatabaseHelper.queryToDynaList(GET_DISTINCT_ISSUE_UOM);
  }
  
  private static final String GET_DISTINCT_PACKAGE_UOM = "SELECT DISTINCT package_uom FROM "
      + "package_issue_uom ORDER BY package_uom";
  
  public List<BasicDynaBean> listDistinctPackageUom() {
    return DatabaseHelper.queryToDynaList(GET_DISTINCT_PACKAGE_UOM);
  }
}
