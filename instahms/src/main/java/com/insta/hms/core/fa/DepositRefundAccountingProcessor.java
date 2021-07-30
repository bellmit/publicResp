package com.insta.hms.core.fa;

import com.insta.hms.billing.accounting.AccountingConstants;
import com.insta.hms.common.AccountingConfig;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.billing.ReceiptModel;
import com.insta.hms.core.billing.ReceiptRefundReferenceModel;
import com.insta.hms.core.billing.ReceiptTaxModel;
import com.insta.hms.model.DepositsReceiptsViewModel;
import com.insta.hms.model.HmsAccountingInfoModel;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.Table;

/**
 * The Class DepositRefundAccountingProcessor.
 */
@Component
public class DepositRefundAccountingProcessor extends AbstractReceiptsAccounting {

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
   * @see com.insta.hms.core.fa.ReceiptsAccounting#
   *      processAccountingForReceipt(com.insta.hms.core.billing.ReceiptModel)
   */
  @Override
  public List<HmsAccountingInfoModel> processAccountingForReceipt(ReceiptModel refundReceipt) {
    List<HmsAccountingInfoModel> receiptAccountingDataList = new ArrayList<>();
    List<Map<String, Object>> refundReceiptAllocation = receiptsAccountingRepository
        .getRefundReceiptAllocationDetails(refundReceipt.getReceiptId());

    if(accountingDataInsertRepo.getfaConfiguration("separate_deposit_refund").equals("Y")) {
    for (Map<String, Object> taxDetails : refundReceiptAllocation) {
      ReceiptRefundReferenceModel refundReference = (ReceiptRefundReferenceModel) taxDetails
          .get("rrm");
      ReceiptModel fromReceiptTaxDetails = (ReceiptModel) taxDetails.get("fromReceipt");
      if (fromReceiptTaxDetails == null) {
        continue;
      }

    HmsAccountingInfoModel receiptAccountingData = new HmsAccountingInfoModel();
    HmsAccountingInfoModel receiptCommonData = receiptsAccountingCommon
        .setDepositReceiptCommonData(refundReceipt);
    BeanUtils.copyProperties(receiptCommonData, receiptAccountingData);

    BigDecimal receiptTotalTax = receiptsAccountingRepository
        .getRefundReceiptTotalTax(refundReceipt.getReceiptId());

    receiptAccountingData.setVoucherType(accountingDataInsertRepo.getVoucherDefinition("VOUCHER_TYPE_PAYMENT"));
    receiptAccountingData.setGrossAmount(refundReference.getAmount().abs().subtract(refundReference.getTaxAmount()));
    receiptAccountingData.setNetAmount(refundReference.getAmount().abs().subtract(refundReference.getTaxAmount()));
    receiptAccountingData.setTransactionType(AccountingConstants.TRANSACTION_TYPE_N);
    DepositsReceiptsViewModel depoReceiptsViewModel =
        (DepositsReceiptsViewModel) receiptsAccountingRepository
        .load(DepositsReceiptsViewModel.class, refundReceipt.getReceiptId());
    receiptAccountingData.setDebitAccount(receiptsAccountingCommon
        .getDepositAccountName(depoReceiptsViewModel));
    receiptAccountingData.setCreditAccount(refundReceipt.getPaymentModeId() != null ? refundReceipt
        .getPaymentModeId().getPaymentMode() : null);

    receiptAccountingData.setCustom1(refundReference.getAmount().abs().subtract(refundReference.getTaxAmount()).toString());
    receiptAccountingData.setCustom2(null);
    receiptAccountingData.setCustom3(null);
    receiptAccountingData.setCustom4(null);
    receiptAccountingData.setChargeReferenceId(refundReceipt.getReceiptId());
    receiptAccountingData.setPrimaryId(refundReceipt.getReceiptId());
    receiptAccountingData.setSecondaryId(null);
    receiptAccountingData.setPrimaryIdReferenceTable(ReceiptModel.class.getAnnotation(Table.class)
        .name());


    receiptAccountingData.setVoucherSubType(accountingDataInsertRepo.getVoucherDefinition("VOUCHER_SUB_TYPE_DEPOSIT_REFUND"));
 
    receiptAccountingData.setVoucherRef(refundReference.getReceipt().getReceiptId());
    receiptAccountingDataList.add(receiptAccountingData);
    }
   } else {
     HmsAccountingInfoModel receiptAccountingData = new HmsAccountingInfoModel();
     HmsAccountingInfoModel receiptCommonData = receiptsAccountingCommon
         .setDepositReceiptCommonData(refundReceipt);
     BeanUtils.copyProperties(receiptCommonData, receiptAccountingData);
     BigDecimal receiptTotalTax = receiptsAccountingRepository
         .getRefundReceiptTotalTax(refundReceipt.getReceiptId());
     receiptAccountingData.setVoucherType(accountingDataInsertRepo.getVoucherDefinition("VOUCHER_TYPE_PAYMENT"));
     receiptAccountingData.setGrossAmount(((refundReceipt.getAmount().negate())
         .subtract(receiptTotalTax)).abs());
     receiptAccountingData.setNetAmount(((refundReceipt.getAmount().negate())
         .subtract(receiptTotalTax)).abs());
     receiptAccountingData.setTransactionType(AccountingConstants.TRANSACTION_TYPE_N);
     DepositsReceiptsViewModel depoReceiptsViewModel =
         (DepositsReceiptsViewModel) receiptsAccountingRepository
         .load(DepositsReceiptsViewModel.class, refundReceipt.getReceiptId());
     receiptAccountingData.setDebitAccount(receiptsAccountingCommon
         .getDepositAccountName(depoReceiptsViewModel));
     receiptAccountingData.setCreditAccount(refundReceipt.getPaymentModeId() != null ? refundReceipt
         .getPaymentModeId().getPaymentMode() : null);
     receiptAccountingData.setCustom1((refundReceipt.getAmount().negate()).subtract(receiptTotalTax)
         .toString());
     receiptAccountingData.setCustom2(null);
     receiptAccountingData.setCustom3(null);
     receiptAccountingData.setCustom4(null);
     receiptAccountingData.setChargeReferenceId(refundReceipt.getReceiptId());
     receiptAccountingData.setPrimaryId(refundReceipt.getReceiptId());
     receiptAccountingData.setSecondaryId(null);
     receiptAccountingData.setPrimaryIdReferenceTable(ReceiptModel.class.getAnnotation(Table.class)
         .name());
     receiptAccountingDataList.add(receiptAccountingData);
   }
    return receiptAccountingDataList;
  }

  /**
   * @see com.insta.hms.core.fa.AbstractReceiptsAccounting#
   *      processAccountingForReceiptTaxes(com.insta.hms.core.billing.ReceiptModel)
   */
  @Override
  public List<HmsAccountingInfoModel> processAccountingForReceiptTaxes(ReceiptModel refundReceipt) {
    List<HmsAccountingInfoModel> receiptsTaxAccountingDataList = new ArrayList<>();

    List<Map<String, Object>> refundReceiptTaxDetails = receiptsAccountingRepository
        .getRefundReceiptTaxDetails(refundReceipt.getReceiptId());

    for (Map<String, Object> taxDetails : refundReceiptTaxDetails) {
      ReceiptRefundReferenceModel refundReference = (ReceiptRefundReferenceModel) taxDetails
          .get("rrm");
      ReceiptTaxModel fromReceiptTaxDetails = (ReceiptTaxModel) taxDetails.get("fromReceiptTaxes");
      if (fromReceiptTaxDetails == null) {
        continue;
      }

      HmsAccountingInfoModel receiptTaxAccountingData = new HmsAccountingInfoModel();
      HmsAccountingInfoModel receiptCommonData = receiptsAccountingCommon
          .setDepositReceiptCommonData(refundReceipt);
      BeanUtils.copyProperties(receiptCommonData, receiptTaxAccountingData);

      // calculate the tax amount for refund amount at subgroup level

      BigDecimal fromReceiptTaxRate = fromReceiptTaxDetails.getTaxRate() != null
          ? fromReceiptTaxDetails.getTaxRate() : BigDecimal.ZERO;
      BigDecimal refundTotalTaxRate = refundReference.getTaxRate() != null ? refundReference
          .getTaxRate() : BigDecimal.ZERO;
      BigDecimal refundTotalTaxAmount = refundReference.getTaxAmount() != null ? refundReference
          .getTaxAmount() : BigDecimal.ZERO;

      BigDecimal refundSubgroupTaxAmount = BigDecimal.ZERO;
      if (refundTotalTaxRate.compareTo(BigDecimal.ZERO) != 0) {
        refundSubgroupTaxAmount = (fromReceiptTaxRate.divide(refundTotalTaxRate,
            BigDecimal.ROUND_HALF_UP)).multiply(refundTotalTaxAmount);
      }

      receiptTaxAccountingData.setVoucherType(accountingDataInsertRepo.getVoucherDefinition("VOUCHER_TYPE_PAYMENT"));
      receiptTaxAccountingData.setGrossAmount(refundSubgroupTaxAmount.abs());
      receiptTaxAccountingData.setNetAmount(refundSubgroupTaxAmount.abs());
      receiptTaxAccountingData.setTransactionType(AccountingConstants.TRANSACTION_TYPE_N);
      receiptTaxAccountingData.setDebitAccount(accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_TAX_LIABILITY_ACC"));
      receiptTaxAccountingData
          .setCreditAccount(refundReceipt.getPaymentModeId() != null ? refundReceipt
              .getPaymentModeId().getPaymentMode() : null);

      receiptTaxAccountingData.setCustom1(refundSubgroupTaxAmount.toString());
      receiptTaxAccountingData.setCustom2(null);
      receiptTaxAccountingData.setCustom3(null);
      receiptTaxAccountingData
          .setCustom4(fromReceiptTaxDetails.getTaxSubGroupId() != null ? fromReceiptTaxDetails
              .getTaxSubGroupId().getItemSubgroupName() : null);
      receiptTaxAccountingData.setChargeReferenceId(refundReference.getRefundReceipt()
          .getReceiptId());
      receiptTaxAccountingData.setPrimaryId(refundReference.getRefundReceipt().getReceiptId());
      ReceiptModel fromReceipt = (ReceiptModel) taxDetails.get("fromReceipt");
      receiptTaxAccountingData.setSecondaryId(fromReceipt.getReceiptId());
      receiptTaxAccountingData.setPrimaryIdReferenceTable(ReceiptRefundReferenceModel.class
          .getAnnotation(Table.class).name());
      receiptTaxAccountingData.setVoucherSubType(accountingDataInsertRepo.getVoucherDefinition("VOUCHER_SUB_TYPE_DEPOSIT_REFUND"));
      receiptTaxAccountingData.setVoucherRef(fromReceipt.getReceiptId());
      receiptsTaxAccountingDataList.add(receiptTaxAccountingData);
    }
    return receiptsTaxAccountingDataList;
  }

}
