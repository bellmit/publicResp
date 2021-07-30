package com.insta.hms.mdm.item;

import com.insta.hms.mdm.MasterRepository;

import org.springframework.stereotype.Repository;

/**
 * The Class StoreItemSubgroupRepository.
 */
@Repository
public class StoreItemSubgroupRepository extends MasterRepository<Integer> {

  /**
   * Instantiates a new store item subgroup repository.
   */
  public StoreItemSubgroupRepository() {
    super(new String[] {"item_subgroup_id", "medicine_id" }, 
        new String[] {"item_subgroup_id", "medicine_id" }, "store_item_sub_groups", "medicine_id");
  }
}
