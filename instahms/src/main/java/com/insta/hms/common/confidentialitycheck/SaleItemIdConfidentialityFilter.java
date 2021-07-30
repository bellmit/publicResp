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

@ConfidentialityValidator(queryParamNames = { "saleitemid", }, urlEntityName = { "sales" })
public class SaleItemIdConfidentialityFilter extends SaleIdConfidentialityValidator 
    implements ConfidentialityInterface {

  private static Logger logger = LoggerFactory.getLogger(SaleItemIdConfidentialityFilter.class);
  
  @LazyAutowired
  private SalesService salesService;

  @Override
  public List<String> getAssociatedMrNo(List<String> salesItemId) {
    logger.debug("Checking confidentiality for saleItemIds:" + ArrayUtils.toString(salesItemId));
    List<String> saleIds = new ArrayList<>();
    for (String saleItemId : salesItemId) {
      BasicDynaBean saleItemBean = salesService.getSaleItemDetail(saleItemId);
      if (saleItemBean != null && saleItemBean.get("sale_id") != null) {
        saleIds.add((String)saleItemBean.get("sale_id"));
      }
    }

    return super.getAssociatedMrNo(saleIds);
  }

  @Override
  public Boolean isValidParameter(String parameter) {
    return salesService.isSaleItemIdValid(parameter);
  }

}
