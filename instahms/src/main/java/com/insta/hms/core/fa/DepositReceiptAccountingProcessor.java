package com.insta.hms.core.fa;

import com.insta.hms.billing.accounting.AccountingConstants;
import com.insta.hms.core.billing.ReceiptModel;
import com.insta.hms.core.billing.ReceiptTaxModel;
import com.insta.hms.model.DepositsReceiptsViewModel;
import com.insta.hms.model.HmsAccountingInfoModel;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.persistence.Table;

/**
 * The Class DepositReceiptAccountingProcessor.
 */
@Component
public class DepositReceiptAccountingProcessor extends AbstractReceiptsAccounting {

  /** The receipts accounting repository. */
  @Autowired
  private ReceiptsAccountingRepository receiptsAccountingRepository;

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
    HmsAccountingInfoModel receiptAccountingData = new HmsAccountingInfoModel();
    List<HmsAccountingInfoModel> receiptAccountingDataList = new ArrayList<>();
    HmsAccountingInfoModel receiptCommonData = receiptsAccountingCommon
        .setDepositReceiptCommonData(receipt);
    BeanUtils.copyProperties(receiptCommonData, receiptAccountingData);
    BigDecimal receiptTotalTax = receiptsAccountingRepository.getReceiptTotalTax(receipt
        .getReceiptId());
    receiptAccountingData.setGrossAmount(receipt.getAmount().abs().subtract(receiptTotalTax));
    receiptAccountingData.setNetAmount(receipt.getAmount().abs().subtract(receiptTotalTax));
    receiptAccountingData.setTransactionType(AccountingConstants.TRANSACTION_TYPE_N);
    receiptAccountingData.setDebitAccount(receipt.getPaymentModeId() != null ? receipt
        .getPaymentModeId().getPaymentMode() : null);
    DepositsReceiptsViewModel depoReceiptsViewModel =
        (DepositsReceiptsViewModel) receiptsAccountingRepository
        .load(DepositsReceiptsViewModel.class, receipt.getReceiptId());
    receiptAccountingData.setCreditAccount(receiptsAccountingCommon
        .getDepositAccountName(depoReceiptsViewModel));

    receiptAccountingData.setCustom1((receipt.getAmount().subtract(receiptTotalTax)).toString());
    receiptAccountingData.setCustom2(null);
    receiptAccountingData.setCustom3(null);
    receiptAccountingData.setCustom4(null);
    receiptAccountingData.setChargeReferenceId(receipt.getReceiptId());
    receiptAccountingData.setPrimaryId(receipt.getReceiptId());
    receiptAccountingData.setSecondaryId(null);
    receiptAccountingData.setPrimaryIdReferenceTable(ReceiptModel.class.getAnnotation(Table.class)
        .name());
    receiptAccountingData.setVoucherSubType(accountingDataInsertRepo.getVoucherDefinition("VOUCHER_SUB_TYPE_DEPOSIT_COLLECTION"));

    receiptAccountingDataList.add(receiptAccountingData);
    return receiptAccountingDataList;
  }

  /**
   * @see com.insta.hms.core.fa.AbstractReceiptsAccounting#
   *      processAccountingForReceiptTaxes(com.insta.hms.core.billing.ReceiptModel)
   */
  @Override
  public List<HmsAccountingInfoModel> processAccountingForReceiptTaxes(ReceiptModel receipt) {
    List<HmsAccountingInfoModel> receiptsTaxAccountingDataList = new ArrayList<>();
    Set<ReceiptTaxModel> receiptTaxes = receipt.getReceiptTaxes();
    for (ReceiptTaxModel receiptTax : receiptTaxes) {
      HmsAccountingInfoModel receiptTaxAccountingData = new HmsAccountingInfoModel();
      HmsAccountingInfoModel receiptCommonData = receiptsAccountingCommon
          .setDepositReceiptCommonData(receipt);
      BeanUtils.copyProperties(receiptCommonData, receiptTaxAccountingData);

      receiptTaxAccountingData.setGrossAmount(receiptTax.getTaxAmount().abs());
      receiptTaxAccountingData.setNetAmount(receiptTax.getTaxAmount().abs());
      receiptTaxAccountingData.setTransactionType(AccountingConstants.TRANSACTION_TYPE_N);
      receiptTaxAccountingData.setDebitAccount(receipt.getPaymentModeId() != null ? receipt
          .getPaymentModeId().getPaymentMode() : null);
      receiptTaxAccountingData.setCreditAccount(accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_TAX_LIABILITY_ACC"));

      receiptTaxAccountingData.setCustom1(receiptTax.getTaxAmount().toString());
      receiptTaxAccountingData.setCustom2(null);
      receiptTaxAccountingData.setCustom3(null);
      receiptTaxAccountingData.setCustom4(receiptTax.getTaxSubGroupId() != null ? receiptTax
          .getTaxSubGroupId().getItemSubgroupName() : null);
      receiptTaxAccountingData.setChargeReferenceId(receipt.getReceiptId());
      receiptTaxAccountingData.setPrimaryId(receiptTax.getReceiptTaxId().toString());
      receiptTaxAccountingData.setSecondaryId(null);
      receiptTaxAccountingData.setPrimaryIdReferenceTable(ReceiptTaxModel.class.getAnnotation(
          Table.class).name());
      receiptTaxAccountingData.setVoucherSubType(accountingDataInsertRepo.getVoucherDefinition("VOUCHER_SUB_TYPE_DEPOSIT_COLLECTION"));
      receiptsTaxAccountingDataList.add(receiptTaxAccountingData);
    }
    return receiptsTaxAccountingDataList;
  }

}
