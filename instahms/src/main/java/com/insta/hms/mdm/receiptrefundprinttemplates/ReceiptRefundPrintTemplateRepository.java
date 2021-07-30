package com.insta.hms.mdm.receiptrefundprinttemplates;

import com.insta.hms.mdm.MasterRepository;

import org.springframework.stereotype.Repository;

@Repository
public class ReceiptRefundPrintTemplateRepository extends MasterRepository<String> {

  public ReceiptRefundPrintTemplateRepository() {
    super("receipt_refund_print_template", "template_name");
  }

}
