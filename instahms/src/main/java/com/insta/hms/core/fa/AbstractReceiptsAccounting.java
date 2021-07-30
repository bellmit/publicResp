package com.insta.hms.core.fa;

import com.insta.hms.core.billing.ReceiptModel;
import com.insta.hms.model.HmsAccountingInfoModel;

import java.util.ArrayList;
import java.util.List;

/**
 * The Class AbstractReceiptsAccounting.
 */
public abstract class AbstractReceiptsAccounting implements ReceiptsAccounting {

  /**
   * Process accounting for receipt taxes.
   *
   * @param receipt
   *          the receipt
   * @return the list
   * @see com.insta.hms.core.fa.ReceiptsAccounting
   *      #processAccountingForReceiptTaxes(com.insta.hms.core.billing.ReceiptModel)
   */
  @Override
  public List<HmsAccountingInfoModel> processAccountingForReceiptTaxes(ReceiptModel receipt) {
    return new ArrayList<>();
  }
}
