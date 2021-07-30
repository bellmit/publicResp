package com.insta.hms.core.inventory.issues;

import com.insta.hms.common.annotations.LazyAutowired;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class StockIssueDetailsService {
  @LazyAutowired
  StockIssueDetailsRepository stockIssueDetailsRepository;

  public BasicDynaBean findByKey(Integer itemIssueNo) {
    return stockIssueDetailsRepository.findByKey("item_issue_no", itemIssueNo);
  }

  public boolean resetReturnQuantity(String chargeId) {
    return stockIssueDetailsRepository.resetReturnQuantity(chargeId) > 0;
  }

  public boolean update(BasicDynaBean bean, Map<String, Object> keys) {
    return stockIssueDetailsRepository.update(bean, keys) > 0;
  }

  public BasicDynaBean getIssueItemCharge(String itemIssueNo) {
    return stockIssueDetailsRepository.getIssueItemCharge(itemIssueNo);
  }
}
