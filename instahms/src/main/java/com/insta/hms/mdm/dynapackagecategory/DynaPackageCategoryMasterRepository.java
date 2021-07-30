package com.insta.hms.mdm.dynapackagecategory;

import com.insta.hms.mdm.MasterRepository;

import org.springframework.stereotype.Repository;

/*
 * @author eshwar-chandra
 */
@Repository
public class DynaPackageCategoryMasterRepository extends MasterRepository<Integer> {

  public DynaPackageCategoryMasterRepository() {
    super("dyna_package_category", "dyna_pkg_cat_id", "dyna_pkg_cat_name");
  }

}
