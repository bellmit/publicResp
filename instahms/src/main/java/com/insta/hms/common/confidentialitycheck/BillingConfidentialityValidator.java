package com.insta.hms.common.confidentialitycheck;

import com.insta.hms.common.annotations.ConfidentialityValidator;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.billing.BillService;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@ConfidentialityValidator(queryParamNames = { "billno", "billnumber" },
    urlEntityName = { "billing" })
public class BillingConfidentialityValidator extends VisitConfidentialityValidator
    implements ConfidentialityInterface {

  private static Logger logger = LoggerFactory.getLogger(BillingConfidentialityValidator.class);
  @LazyAutowired
  BillService billService;

  @Override
  public List<String> getAssociatedMrNo(List<String> billNumbers) {
    logger.debug("Checking confidentiality for billno:" + ArrayUtils.toString(billNumbers));
    List<String> visitIds = new ArrayList<String>();
    for (String billNumber : billNumbers) {
      visitIds.add(billService.getVisitId(billNumber));
    }
    return super.getAssociatedMrNo(visitIds);
  }

  @Override
  public Boolean isValidParameter(String parameter) {
    return billService.isBillNumberExist(parameter);
  }
}
