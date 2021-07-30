package com.insta.hms.mdm.storeitemrates;

import com.insta.hms.mdm.MasterService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * The Class StoreItemRatesService.
 */
@Service
public class StoreItemRatesService extends MasterService {

  /**
   * Instantiates a new store item rates service.
   *
   * @param repository the repository
   * @param validator the validator
   */
  public StoreItemRatesService(StoreItemRatesRepository repository,
      StoreItemRatesValidator validator) {
    super(repository, validator);
  }
  
  public BasicDynaBean getBean() {
    return getRepository().getBean();
  }
  
  public List<BasicDynaBean> listAll() {
    return getRepository().listAll();
  }

}
