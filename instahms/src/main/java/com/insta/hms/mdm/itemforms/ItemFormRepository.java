package com.insta.hms.mdm.itemforms;

import com.insta.hms.mdm.MasterRepository;

import org.springframework.stereotype.Repository;

/**
 * The Class ItemFormRepository.
 *
 * @author irshadmohammed
 */
@Repository
public class ItemFormRepository extends MasterRepository<Integer> {

  /**
   * Instantiates a new item form repository.
   */
  public ItemFormRepository() {
    super("item_form_master", "item_form_id", "item_form_name");
  }
}
