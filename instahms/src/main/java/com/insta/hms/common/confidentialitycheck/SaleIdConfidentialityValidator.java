package com.insta.hms.common.confidentialitycheck;

import com.insta.hms.common.annotations.ConfidentialityValidator;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.inventory.sales.SalesService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@ConfidentialityValidator(queryParamNames = { "saleid", }, urlEntityName = { "sales" })
public class SaleIdConfidentialityValidator extends BillingConfidentialityValidator
    implements ConfidentialityInterface {
  private static Logger logger = LoggerFactory.getLogger(SaleIdConfidentialityValidator.class);

  @LazyAutowired
  private SalesService salesService;

  @Override
  public List<String> getAssociatedMrNo(List<String> salesId) {
    logger.debug("Checking confidentiality for saleId:" + ArrayUtils.toString(salesId));
    List<String> billNumbers = new ArrayList<>();
    for (String saleId : salesId) {
      BasicDynaBean saleBean = salesService.findByKey("sale_id", saleId);
      if (saleBean != null && saleBean.get("bill_no") != null) {
        billNumbers.add((String) saleBean.get("bill_no"));
      }
    }

    return super.getAssociatedMrNo(billNumbers);
  }
  
  @Override
  public Boolean isValidParameter(String parameter) {
    return salesService.isSaleIdValid(parameter);
  }
}
