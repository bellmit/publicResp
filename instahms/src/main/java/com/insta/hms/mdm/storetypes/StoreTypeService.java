package com.insta.hms.mdm.storetypes;

import com.insta.hms.mdm.MasterService;

import org.springframework.stereotype.Service;

@Service
public class StoreTypeService extends MasterService {

  public StoreTypeService(StoreTypeRepository repository, StoreTypeValidator validator) {
    super(repository, validator);
  }

}
