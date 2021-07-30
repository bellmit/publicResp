package com.insta.hms.mdm.itemgroups;

import com.insta.hms.mdm.MasterService;

import org.springframework.stereotype.Service;

/** The Class ItemGroupsService. */
@Service
public class ItemGroupsService extends MasterService {

  /**
   * Instantiates a new item groups service.
   *
   * @param repository the repository
   * @param validator the validator
   */
  public ItemGroupsService(ItemGroupsRepository repository, ItemGroupsValidator validator) {
    super(repository, validator);
  }
}
