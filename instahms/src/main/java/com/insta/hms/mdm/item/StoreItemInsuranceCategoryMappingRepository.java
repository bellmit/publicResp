package com.insta.hms.mdm.item;

import com.insta.hms.common.GenericRepository;

import org.springframework.stereotype.Repository;

@Repository
public class StoreItemInsuranceCategoryMappingRepository extends GenericRepository {

  public StoreItemInsuranceCategoryMappingRepository() {
    super("store_items_insurance_category_mapping");
  }

}
