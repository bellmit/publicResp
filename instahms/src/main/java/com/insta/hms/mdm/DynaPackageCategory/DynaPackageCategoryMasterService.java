package com.insta.hms.mdm.dynapackagecategory;

import com.insta.hms.mdm.MasterService;

import org.springframework.stereotype.Service;

/*
 * @author eshwar-chandra
 */
@Service
public class DynaPackageCategoryMasterService extends MasterService {

  public DynaPackageCategoryMasterService(DynaPackageCategoryMasterRepository repo,
      DynaPackageCategoryMasterValidator validator) {
    super(repo, validator);
  }

}
