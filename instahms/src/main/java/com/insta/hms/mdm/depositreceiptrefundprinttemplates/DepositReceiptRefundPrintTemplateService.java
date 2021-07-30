package com.insta.hms.mdm.depositreceiptrefundprinttemplates;

import com.insta.hms.mdm.MasterService;

import org.springframework.stereotype.Service;

/**
 * The Class DepositReceiptRefundPrintTemplateService.
 */
@Service
public class DepositReceiptRefundPrintTemplateService extends MasterService {

  /**
   * Instantiates a new deposit receipt refund print template service.
   *
   * @param repository
   *          DepositReceiptRefundPrintTemplateRepository
   * @param validator
   *          DepositReceiptRefundPrintTemplateValidator
   */
  public DepositReceiptRefundPrintTemplateService(
      DepositReceiptRefundPrintTemplateRepository repository,
      DepositReceiptRefundPrintTemplateValidator validator) {
    super(repository, validator);
  }

}
