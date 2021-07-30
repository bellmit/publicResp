package com.insta.hms.integration.paymentgateway;

import com.insta.hms.common.annotations.LazyAutowired;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class PaymentTransactionService {

  @LazyAutowired
  private PaymentTransactionRepository paymentTransactionRepository;

  public Integer insert(BasicDynaBean bean) {
    return paymentTransactionRepository.insert(bean);
  }

  public Integer update(BasicDynaBean bean, Map<String, Object> keys) {
    return paymentTransactionRepository.update(bean, keys);
  }

  public BasicDynaBean getBean() {
    return paymentTransactionRepository.getBean();
  }

  public Integer getNextSequence() {
    return paymentTransactionRepository.getNextSequence();
  }

  public BasicDynaBean findByKey(Map<String, Object> filterMap) {
    return paymentTransactionRepository.findByKey(filterMap);
  }

}
