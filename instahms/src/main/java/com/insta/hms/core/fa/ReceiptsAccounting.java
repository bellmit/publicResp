package com.insta.hms.core.fa;

import com.insta.hms.core.billing.ReceiptModel;
import com.insta.hms.model.HmsAccountingInfoModel;

import java.util.List;

/**
 * The Interface ReceiptsAccounting.
 */
public interface ReceiptsAccounting {

  /**
   * Process accounting for receipt.
   *
   * @param receipt
   *          the receipt
   * @return the hms accounting info model
   */
  public List<HmsAccountingInfoModel> processAccountingForReceipt(ReceiptModel receipt);

  /**
   * Process accounting for receipt taxes.
   *
   * @param receipt
   *          the receipt
   * @return the list
   */
  public List<HmsAccountingInfoModel> processAccountingForReceiptTaxes(ReceiptModel receipt);
}
