package com.insta.hms.core.inventory.stocks;

import com.insta.hms.common.GenericRepository;

import org.springframework.stereotype.Repository;

@Repository
public class StoreCategoryRepository extends GenericRepository{

  public StoreCategoryRepository() {
    super("store_category_master");
  }

}
