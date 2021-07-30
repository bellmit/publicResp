package com.insta.hms.mdm.storeitemcontroltypes;

import com.insta.hms.mdm.MasterService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class StoreItemControlTypeService.
 */
@Service
public class StoreItemControlTypeService extends MasterService {

  /**
   * Instantiates a new store item control type service.
   *
   * @param repository the repository
   * @param validator the validator
   */
  public StoreItemControlTypeService(StoreItemControlTypeRepository repository,
      StoreItemControlTypeValidator validator) {
    super(repository, validator);
  }

  /**
   * Gets the adds the edit page data.
   *
   * @return the adds the edit page data
   */
  public Map<String, List<BasicDynaBean>> getAddEditPageData() {
    Map<String, List<BasicDynaBean>> refData = new HashMap<>();
    refData.put("storeitemcontroltypedetails", lookup(false));
    return refData;
  }
}
