package com.insta.hms.mdm.sequences.posequences;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.mdm.sequences.SequenceMasterService;
import com.insta.hms.mdm.stores.StoreService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class PurchaseOrderSequenceService.
 */
@Service
public class PurchaseOrderSequenceService extends SequenceMasterService {
  
  /** The store service. */
  @LazyAutowired public StoreService storeService;

  /**
   * Instantiates a new purchase order sequence service.
   *
   * @param repository the r
   * @param validator the v
   */
  public PurchaseOrderSequenceService(
      PurchaseOrderSequenceRepository repository, PurchaseOrderSequenceValidator validator) {
    super(repository, validator);
  }

  /**
   * @see com.insta.hms.mdm.sequences.SequenceMasterService#getListPageData(java.util.Map)
   */
  @Override
  public Map<String, List<BasicDynaBean>> getListPageData(Map<String, String[]> requestParams) {
    Map<String, List<BasicDynaBean>> map = new HashMap<String, List<BasicDynaBean>>();
    List<BasicDynaBean> stores = storeService.lookup(true);
    map.put("stores", stores);
    return map;
  }
}
