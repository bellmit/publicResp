package com.insta.hms.common.confidentialitycheck;

import com.insta.hms.common.annotations.ConfidentialityValidator;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.billing.ReceiptService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/** The Class DepositConfidentialityValidator. */
@ConfidentialityValidator(queryParamNames = { "depositno" }, urlEntityName = { "deposits" })
public class DepositConfidentialityValidator implements ConfidentialityInterface {

  /** The logger. */
  private static Logger logger = LoggerFactory.getLogger(DepositConfidentialityValidator.class);

  /** The patient deposit service. */
  @LazyAutowired
  private ReceiptService receiptService;

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.common.confidentialitycheck.
   * ConfidentialityInterface#getAssociatedMrNo(java.util.List)
   */
  @Override
  public List<String> getAssociatedMrNo(List<String> depositNos) {
    logger.debug("Checking confidentiality for depositno:" + ArrayUtils.toString(depositNos));
    List<String> mrNoList = new ArrayList<>();
    for (String depositNo : depositNos) {
      BasicDynaBean depositBean = receiptService.findByKey(depositNo);
      mrNoList.add((String) depositBean.get("mr_no"));
    }
    return mrNoList;
  }
  
  @Override
  public Boolean isValidParameter(String parameter) {
    return receiptService.isReceiptNoValid(parameter);
  }
}
