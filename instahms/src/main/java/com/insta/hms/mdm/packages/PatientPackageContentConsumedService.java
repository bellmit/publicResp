package com.insta.hms.mdm.packages;

import com.insta.hms.common.annotations.LazyAutowired;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

/**
 * The Class PatientPackageContentConsumedService.
 */
@Service
public class PatientPackageContentConsumedService {

  /** The pat pkg content consumed repo. */
  @LazyAutowired
  private PatientPackageContentConsumedRepository patPkgContentConsumedRepo;

  /**
   * Gets the bean.
   *
   * @return the bean
   */
  public BasicDynaBean getBean() {
    return patPkgContentConsumedRepo.getBean();
  }

  /**
   * Save.
   *
   * @param patPkgContentConsumedBean the pat pkg content consumed bean
   * @return the integer
   */
  public Integer save(BasicDynaBean patPkgContentConsumedBean) {
    return patPkgContentConsumedRepo.insert(patPkgContentConsumedBean);
  }

  /**
   * Find by key.
   *
   * @param key the key
   * @param value the value
   * @return the basic dyna bean
   */
  public BasicDynaBean findByKey(String key, String value) {
    return patPkgContentConsumedRepo.findByKey(key, value);
  }
  
  
  /**
   * Update package content consumed quantity.
   *
   * @param patPkgContentId the pat package content id
   * @param prescId the presc id
   * @param itemType the item type
   * @param contentId the content id
   */
  public void updatePkgContentConsumedQuantity(Integer patPkgContentId, Integer prescId, 
      String itemType, Integer contentId) {
    patPkgContentConsumedRepo.updatePkgContentConsumedQuantity(patPkgContentId, prescId, 
        itemType, contentId);
  }

}
