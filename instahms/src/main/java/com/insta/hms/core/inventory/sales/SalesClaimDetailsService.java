package com.insta.hms.core.inventory.sales;

import com.insta.hms.common.BusinessService;
import com.insta.hms.common.annotations.LazyAutowired;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class SalesClaimDetailsService extends BusinessService {

  @LazyAutowired
  SalesClaimDetailsRepository scdRepository;

  public BasicDynaBean findByKey(Map<String, Object> filterMap) {
    return scdRepository.findByKey(filterMap);
  }

  public int update(BasicDynaBean bean, Map<String, Object> keys) {
    return scdRepository.update(bean, keys);
  }

public BasicDynaBean getTaxAmtInsuranceAmt(Integer saleItemId) {
	return scdRepository.getTaxAmtInsuranceAmt(saleItemId);
}

public List<BasicDynaBean> getCombinedActivities(String claimId, String claimActivityId, Boolean isInternalComplaint) {
  return scdRepository.getCombinedActivities(claimId, claimActivityId, isInternalComplaint);
}
}
