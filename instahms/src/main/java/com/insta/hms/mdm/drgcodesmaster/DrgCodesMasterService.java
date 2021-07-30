package com.insta.hms.mdm.drgcodesmaster;

import com.insta.hms.mdm.MasterService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * The Class DrgCodesMasterService.
 */
@Service
public class DrgCodesMasterService extends MasterService {

  /**
   * Instantiates a new drg codes master service.
   *
   * @param repository DrgCodesMasterRepository
   * @param validator DrgCodesMasterValidator
   */
  public DrgCodesMasterService(DrgCodesMasterRepository repository,
      DrgCodesMasterValidator validator) {
    super(repository, validator);
  }

  /**
   * Find by key.
   *
   * @param keys Map
   * @return the basic dyna bean
   */
  public BasicDynaBean findByKey(Map keys) {
    return getRepository().findByKey(keys);
  }
  
  /**
   * Gets the drg item sub group tax details.
   *
   * @param itemId the item id
   * @return the drg item sub group tax details
   */
  public List<BasicDynaBean> getDrgItemSubGroupTaxDetails(String itemId) {
    return ((DrgCodesMasterRepository) getRepository()).getDrgItemSubGroupTaxDetails(itemId);
  }

}
