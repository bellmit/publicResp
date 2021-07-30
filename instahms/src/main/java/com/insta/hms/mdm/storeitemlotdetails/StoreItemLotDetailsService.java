package com.insta.hms.mdm.storeitemlotdetails;

import com.insta.hms.mdm.MasterRepository;
import com.insta.hms.mdm.MasterService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

/**
 * The Class StoreItemLotDetailsService.
 *
 * @author Amol
 */
@Service
public class StoreItemLotDetailsService extends MasterService {

  /**
   * Instantiates a new store item lot details service.
   *
   * @param repository
   *          the repository
   * @param validator
   *          the validator
   */
  public StoreItemLotDetailsService(StoreItemLotDetailsRepository repository,
      StoreItemLotDetailsValidator validator) {
    super(repository, validator);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.mdm.MasterService#getRepository()
   */
  @Override
  public MasterRepository getRepository() {
    return super.getRepository();
  }

  public BasicDynaBean getBean() {
    return getRepository().getBean();
  }

  public Integer getNextSequence() {
    return getRepository().getNextSequence();
  }

}
