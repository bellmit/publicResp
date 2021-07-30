package com.insta.hms.core.inventory.supplierreturn.debit;

import com.insta.hms.common.BusinessService;
import com.insta.hms.common.annotations.LazyAutowired;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

/**
 * Store Debit Note Service.
 * @author anandpatel
 *
 */
@Service
public class StoreDebitNoteService extends BusinessService {

  @LazyAutowired
  private StoreDebitNoteRepository storeDebitNoteRepository;

  public BasicDynaBean getBean() {
    return storeDebitNoteRepository.getBean();
  }

  public BasicDynaBean findByKey(String keyColumn, Object identifier) {
    return storeDebitNoteRepository.findByKey(keyColumn, identifier);
  }
  
  public Integer insert(BasicDynaBean bean) {
    return storeDebitNoteRepository.insert(bean);
  }
}
