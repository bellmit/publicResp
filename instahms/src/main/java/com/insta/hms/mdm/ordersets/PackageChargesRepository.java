package com.insta.hms.mdm.ordersets;

import com.insta.hms.common.GenericRepository;

import org.springframework.stereotype.Repository;

@Repository
public class PackageChargesRepository extends GenericRepository {

  public PackageChargesRepository() {
    super("package_charges");
  }

}
