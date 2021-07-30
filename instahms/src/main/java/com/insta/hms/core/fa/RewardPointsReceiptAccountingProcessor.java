package com.insta.hms.core.fa;

import com.insta.hms.billing.accounting.AccountingConstants;
import com.insta.hms.core.billing.BillReceiptsModel;
import com.insta.hms.core.billing.ReceiptModel;
import com.insta.hms.model.HmsAccountingInfoModel;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The Class RewardPointsReceiptAccountingProcessor.
 */
@Component
public class RewardPointsReceiptAccountingProcessor extends AbstractReceiptsAccounting {

  /** The receipts accounting common. */
  @Autowired
  private ReceiptsAccountingCommon receiptsAccountingCommon;
  
  /** The accounting data insert repo. */
  @Autowired
  private AccountingDataInsertRepository accountingDataInsertRepo;

  /**
   * @see com.insta.hms.core.fa.ReceiptsAccounting#processAccountingForReceipt(com.insta.hms.core.billing.ReceiptModel)
   */
  @Override
  public List<HmsAccountingInfoModel> processAccountingForReceipt(ReceiptModel receipt) {
    BillReceiptsModel billReceiptModel = receipt.getBillReceipts() != null ? receipt
        .getBillReceipts().iterator().next() : null;
    if (billReceiptModel == null) {
      return null;
    }

    HmsAccountingInfoModel receiptAccountingData = new HmsAccountingInfoModel();
    List<HmsAccountingInfoModel> receiptAccountingDataList = new ArrayList<>();
    HmsAccountingInfoModel receiptCommonData = receiptsAccountingCommon
        .setReceiptCommonData(receipt);
    BeanUtils.copyProperties(receiptCommonData, receiptAccountingData);

    receiptAccountingData.setPointsRedeemed(receipt.getPointsRedeemed());
    receiptAccountingData.setGrossAmount(receipt.getAmount());
    receiptAccountingData.setNetAmount(receipt.getAmount().subtract(receipt.getTdsAmount()));
    receiptAccountingData.setTransactionType(AccountingConstants.TRANSACTION_TYPE_N);
    receiptAccountingData.setDebitAccount(accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_REWARDPOINTS_ACC"));
    receiptAccountingData.setCreditAccount(accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_COUNTER_RECEIPTS"));
    receiptAccountingData.setCustom1(receipt.getAmount().toString());
    receiptAccountingData.setVoucherSubType(accountingDataInsertRepo.getVoucherDefinition("VOUCHER_SUB_TYPE_REWARD_POINTS"));
    receiptAccountingDataList.add(receiptAccountingData);
    return receiptAccountingDataList;
  }

}
