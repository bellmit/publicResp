package com.insta.hms.core.inventory.stocks;

import com.insta.hms.common.annotations.LazyAutowired;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class StoreGrnTaxDetailsService {
  @LazyAutowired
  StoreGrnTaxDetailsRepository storeGrnTaxDetailsRepository;

  public BasicDynaBean getBean() {
    return storeGrnTaxDetailsRepository.getBean();
  }

  public BasicDynaBean findByKey(Map<String, Object> filterMap){
    return storeGrnTaxDetailsRepository.findByKey(filterMap);
  }
  
  public Integer update(BasicDynaBean bean, Map<String, Object> keys){
    return storeGrnTaxDetailsRepository.update(bean, keys);
  }

  public Integer insert(BasicDynaBean bean) {
     return storeGrnTaxDetailsRepository.insert(bean);
  }
  
  public Integer delete(Map<String, Object> keys){
    return storeGrnTaxDetailsRepository.delete(keys);
  }
}
