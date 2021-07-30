package com.insta.hms.core.fa;

import com.insta.hms.billing.accounting.AccountingConstants;
import com.insta.hms.core.billing.BillModel;
import com.insta.hms.core.billing.BillReceiptsModel;
import com.insta.hms.core.billing.ReceiptModel;
import com.insta.hms.mdm.tpas.TpaMasterModel;
import com.insta.hms.model.HmsAccountingInfoModel;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The Class SponsorWriteOffAccountingProcessor.
 */
@Component
public class SponsorWriteOffAccountingProcessor extends AbstractReceiptsAccounting {

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
    BillModel billModel = billReceiptModel.getBillNo();
    List<HmsAccountingInfoModel> receiptAccountingDataList = new ArrayList<>();
    HmsAccountingInfoModel receiptAccountingData = new HmsAccountingInfoModel();
    HmsAccountingInfoModel receiptCommonData = receiptsAccountingCommon
        .setReceiptCommonData(receipt);
    BeanUtils.copyProperties(receiptCommonData, receiptAccountingData);

    receiptAccountingData.setCenterId(billModel.getVisitId() != null ? billModel.getVisitId()
        .getCenterId().getCenterId() : null);
    receiptAccountingData.setCenterName(billModel.getVisitId() != null ? billModel.getVisitId()
        .getCenterId().getCenterName() : null);
    receiptAccountingData.setCounterNo(null);
    receiptAccountingData.setGrossAmount(receipt.getAmount().abs());
    receiptAccountingData.setNetAmount(receipt.getAmount().subtract(receipt.getTdsAmount()).abs());
    receiptAccountingData.setTransactionType(AccountingConstants.TRANSACTION_TYPE_N);
    receiptAccountingData.setDebitAccount(accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_WRITEOFF_ACC"));
    TpaMasterModel tpaModel = receiptsAccountingCommon.getReceiptTpaModel(billReceiptModel);
    receiptAccountingData.setCreditAccount(tpaModel != null ? tpaModel.getTpaName()
        : accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_COUNTER_RECEIPTS"));

    receiptAccountingData.setCustom1(receipt.getAmount().toString());
    receiptAccountingData.setCustom2(tpaModel != null ? tpaModel.getTpaName() : null);
    receiptAccountingData.setCustom3(tpaModel != null ? String.valueOf(tpaModel.getSponsorType())
        : null);
    receiptAccountingData.setCustom4(null);
    receiptAccountingData.setVoucherSubType(accountingDataInsertRepo.getVoucherDefinition("VOUCHER_SUB_TYPE_SPONSOR_WRITEOFF"));

    receiptAccountingDataList.add(receiptAccountingData);
    return receiptAccountingDataList;
  }

}
