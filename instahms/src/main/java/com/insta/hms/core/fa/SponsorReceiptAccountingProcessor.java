package com.insta.hms.core.fa;

import com.insta.hms.billing.accounting.AccountingConstants;
import com.insta.hms.core.billing.BillReceiptsModel;
import com.insta.hms.core.billing.ReceiptModel;
import com.insta.hms.mdm.tpas.TpaMasterModel;
import com.insta.hms.model.HmsAccountingInfoModel;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The Class SponsorReceiptAccountingProcessor.
 */
@Component
public class SponsorReceiptAccountingProcessor extends AbstractReceiptsAccounting {

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
    List<HmsAccountingInfoModel> receiptAccountingDataList = new ArrayList<>();
    HmsAccountingInfoModel receiptAccountingData = new HmsAccountingInfoModel();
    HmsAccountingInfoModel receiptCommonData = receiptsAccountingCommon
        .setReceiptCommonData(receipt);
    BeanUtils.copyProperties(receiptCommonData, receiptAccountingData);

    receiptAccountingData.setGrossAmount(receipt.getAmount());
    receiptAccountingData.setNetAmount(receipt.getAmount());
    receiptAccountingData.setTransactionType(AccountingConstants.TRANSACTION_TYPE_N);
    receiptAccountingData.setDebitAccount(receipt.getPaymentModeId() != null ? receipt
        .getPaymentModeId().getPaymentMode() : null);
    TpaMasterModel tpaModel = receiptsAccountingCommon.getReceiptTpaModel(billReceiptModel);
    receiptAccountingData.setCreditAccount(tpaModel != null ? tpaModel.getTpaName()
        : accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_COUNTER_RECEIPTS"));

    receiptAccountingData.setCustom1(receipt.getAmount().toString());
    receiptAccountingData.setCustom2(tpaModel != null ? tpaModel.getTpaName() : null);
    receiptAccountingData.setCustom3(tpaModel != null ? String.valueOf(tpaModel.getSponsorType())
        : null);
    receiptAccountingData.setCustom4(null);
    receiptAccountingData.setVoucherSubType(receipt.getIsSettlement() ? accountingDataInsertRepo.getVoucherDefinition("VOUCHER_SUB_TYPE_SPONSOR_SETTLEMENT") :
      accountingDataInsertRepo.getVoucherDefinition("VOUCHER_SUB_TYPE_SPONSOR_ADVANCE"));

    receiptAccountingDataList.add(receiptAccountingData);
    return receiptAccountingDataList;
  }

  @Override
  public List<HmsAccountingInfoModel> processAccountingForReceiptTaxes(ReceiptModel receipt) {  
    BillReceiptsModel billReceiptModel = receipt.getBillReceipts() != null ? receipt
        .getBillReceipts().iterator().next() : null;
    if (receipt.getTdsAmount().compareTo(BigDecimal.ZERO) <= 0) {
      return new ArrayList<>();
    }
    
    HmsAccountingInfoModel receiptAccountingData = new HmsAccountingInfoModel();
    HmsAccountingInfoModel receiptCommonData = receiptsAccountingCommon
        .setReceiptCommonData(receipt);
    BeanUtils.copyProperties(receiptCommonData, receiptAccountingData);
    
    receiptAccountingData.setGrossAmount(receipt.getTdsAmount());
    receiptAccountingData.setNetAmount(receipt.getTdsAmount());
    receiptAccountingData.setTransactionType(AccountingConstants.TRANSACTION_TYPE_N);
    receiptAccountingData.setDebitAccount(accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_TDS_ACC"));
    TpaMasterModel tpaModel = receiptsAccountingCommon.getReceiptTpaModel(billReceiptModel);
    receiptAccountingData.setCreditAccount(tpaModel != null ? tpaModel.getTpaName()
        : accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_COUNTER_RECEIPTS"));
    receiptAccountingData.setTaxAmount(BigDecimal.ZERO);
    receiptAccountingData.setCustom1(receipt.getTdsAmount().toString());
    receiptAccountingData.setCustom2(tpaModel != null ? tpaModel.getTpaName() : null);
    receiptAccountingData.setCustom3(tpaModel != null ? String.valueOf(tpaModel.getSponsorType())
        : null);
    receiptAccountingData.setCustom4(null);
    receiptAccountingData.setVoucherSubType(receipt.getIsSettlement() ? accountingDataInsertRepo.getVoucherDefinition("VOUCHER_SUB_TYPE_SPONSOR_SETTLEMENT") :
      accountingDataInsertRepo.getVoucherDefinition("VOUCHER_SUB_TYPE_SPONSOR_ADVANCE"));
    return  (Arrays.asList(receiptAccountingData));
  }
}
