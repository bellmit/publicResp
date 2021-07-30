package com.insta.hms.mdm.storeitembatchdetails;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.mdm.MasterService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class StoreItemBatchDetailsService.
 *
 * @author Amol
 */
@Service
public class StoreItemBatchDetailsService extends MasterService {
  
  @LazyAutowired
  private StoreItemBatchDetailsRepository repo ;

  /**
   * Instantiates a new store item batch details service.
   *
   * @param repo
   *          the repo
   * @param validator
   *          the validator
   */
  public StoreItemBatchDetailsService(StoreItemBatchDetailsRepository repo,
      StoreItemBatchDetailsValidator validator) {
    super(repo, validator);
    this.repo = repo;
  }

  /**
   * Find by key.
   *
   * @param filterMap
   *          the filter map
   * @return the basic dyna bean
   */
  public BasicDynaBean findByKey(Map<String, Object> filterMap) {
    return super.getRepository().findByKey(filterMap);
  }

  /**
   * Gets the bean.
   *
   * @return the bean
   */
  public BasicDynaBean getBean() {
    return super.getRepository().getBean();
  }

  public Integer getNextSequence() {
    return getRepository().getNextSequence();
  }
  
  public boolean isItemExpired(String batchNo, int medicineId) {
    return repo.isItemExpired(batchNo, medicineId);
  }
}
