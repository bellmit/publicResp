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
 * The Class PatientReceiptAccountingProcessor.
 */
@Component
public class PatientReceiptAccountingProcessor extends AbstractReceiptsAccounting {

  /** The receipts accounting common. */
  @Autowired
  private ReceiptsAccountingCommon receiptsAccountingCommon;
  
  @Autowired
  private AccountingDataInsertRepository accountingDataInsertRepo;

  /**
   * @see com.insta.hms.core.fa.ReceiptsAccounting#processAccountingForReceipt(com.insta.hms.core.billing.ReceiptModel)
   */
  @Override
  public List<HmsAccountingInfoModel> processAccountingForReceipt(ReceiptModel receipt) {
    BillReceiptsModel billReceiptModel = receipt.getBillReceipts().iterator().next();
    if (billReceiptModel == null) {
      return null;
    }
    List<HmsAccountingInfoModel> receiptAccountingDataList = new ArrayList<>();
    HmsAccountingInfoModel receiptCommonData = receiptsAccountingCommon
        .setReceiptCommonData(receipt);
    HmsAccountingInfoModel receiptAccountingData = new HmsAccountingInfoModel();
    BeanUtils.copyProperties(receiptCommonData, receiptAccountingData);
    receiptAccountingData.setGrossAmount(receipt.getAmount());
    receiptAccountingData.setNetAmount(receipt.getAmount().subtract(receipt.getTdsAmount()));
    receiptAccountingData.setTransactionType(AccountingConstants.TRANSACTION_TYPE_N);
    receiptAccountingData.setDebitAccount(receipt.getPaymentModeId() != null ? receipt
        .getPaymentModeId().getPaymentMode() : null);
    receiptAccountingData.setCreditAccount(accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_COUNTER_RECEIPTS"));
    receiptAccountingData.setCustom1(receipt.getAmount().toString());
      receiptAccountingData.setVoucherSubType(receipt.getIsSettlement() ? accountingDataInsertRepo.getVoucherDefinition("VOUCHER_SUB_TYPE_BILL_SETTLEMENT") :
        accountingDataInsertRepo.getVoucherDefinition("VOUCHER_SUB_TYPE_BILL_ADVANCE"));

      receiptAccountingDataList.add(receiptAccountingData);
      return receiptAccountingDataList;
  }

}
