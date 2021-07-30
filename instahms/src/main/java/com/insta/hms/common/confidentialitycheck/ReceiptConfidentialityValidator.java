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

@ConfidentialityValidator(queryParamNames = { "receiptno", "receiptid",
    "receiptnumber" }, urlEntityName = { "receipts" })
public class ReceiptConfidentialityValidator extends BillingConfidentialityValidator
    implements ConfidentialityInterface {

  private static Logger logger = LoggerFactory.getLogger(ReceiptConfidentialityValidator.class);

  @LazyAutowired
  ReceiptService receiptService;

  @Override
  public List<String> getAssociatedMrNo(List<String> receiptNumbers) {
    logger.debug("Checking confidentiality for receiptno:" + ArrayUtils.toString(receiptNumbers));
    List<String> mrNoList = new ArrayList<>();
    List<String> billNumbers = new ArrayList<>();
    for (String receiptno : receiptNumbers) {
      BasicDynaBean receiptBean = receiptService.findByKey(receiptno);
      String mrNo = (String) receiptBean.get("mr_no");
      if (null != mrNo) {
        mrNoList.add(mrNo);
        continue;
      } else {
        mrNoList.add("retail");
      }
      String tpaId = (String) receiptBean.get("tpa_id");

      if (null != tpaId) {
        List<BasicDynaBean> billReceipts = receiptService.findAllBillReceipts(receiptno);
        for (BasicDynaBean billReceipt : billReceipts) {
          billNumbers.add((String) billReceipt.get("bill_no"));
        }
      }
    }
    if (!billNumbers.isEmpty()) {
      mrNoList.addAll(super.getAssociatedMrNo(billNumbers));
    }
    return mrNoList;

  }

  @Override
  public Boolean isValidParameter(String parameter) {
    return receiptService.isReceiptNoValid(parameter);
  }

}
