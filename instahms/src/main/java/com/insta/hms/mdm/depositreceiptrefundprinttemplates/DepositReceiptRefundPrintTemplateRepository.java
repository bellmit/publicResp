package com.insta.hms.mdm.depositreceiptrefundprinttemplates;

import com.insta.hms.mdm.MasterRepository;

import org.springframework.stereotype.Repository;

/**
 * The Class DepositReceiptRefundPrintTemplateRepository.
 */
@Repository
public class DepositReceiptRefundPrintTemplateRepository extends MasterRepository<String> {

  /**
   * Instantiates a new deposit receipt refund print template repository.
   */
  public DepositReceiptRefundPrintTemplateRepository() {
    super("deposit_receipt_refund_template", "template_name");
  }

}
