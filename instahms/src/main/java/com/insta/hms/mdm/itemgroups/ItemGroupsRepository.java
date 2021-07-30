package com.insta.hms.mdm.itemgroups;

import com.insta.hms.mdm.MasterRepository;

import org.springframework.stereotype.Repository;

/** The Class ItemGroupsRepository. */
@Repository
public class ItemGroupsRepository extends MasterRepository<Integer> {

  /** Instantiates a new item groups repository. */
  public ItemGroupsRepository() {
    super("item_groups", "item_group_id","item_group_name");
  }
}
