package com.insta.hms.mdm.dynapackagerules;

import com.insta.hms.mdm.MasterRepository;

import org.springframework.stereotype.Repository;

// TODO: Auto-generated Javadoc
/** The Class DynaPackageRulesRepository. */
@Repository
public class DynaPackageRulesRepository extends MasterRepository<Integer> {

  /** Instantiates a new dyna package rules repository. */
  public DynaPackageRulesRepository() {
    super("dyna_package_rules", "pkg_rule_id");
  }
}
