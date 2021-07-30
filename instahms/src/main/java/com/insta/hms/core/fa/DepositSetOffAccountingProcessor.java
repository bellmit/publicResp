package com.insta.hms.core.fa;

import com.insta.hms.billing.accounting.AccountingConstants;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.core.billing.AllocationRepository;
import com.insta.hms.core.billing.BillReceiptsModel;
import com.insta.hms.core.billing.ReceiptTaxModel;
import com.insta.hms.model.HmsAccountingInfoModel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.persistence.Table;

/**
 * The Class DepositSetOffAccountingProcessor.
 */
@Component
public class DepositSetOffAccountingProcessor {

  /** The receipts accounting repository. */
  @Autowired
  private ReceiptsAccountingRepository receiptsAccountingRepository;

  /** The receipts accounting common. */
  @Autowired
  private ReceiptsAccountingCommon receiptsAccountingCommon;

  /** The accounting data insert repo. */
  @Autowired
  private AccountingDataInsertRepository accountingDataInsertRepo;

  /** The allocation repository. */
  @Autowired
  AllocationRepository allocationRepository;

  /**
   * 1. Set Off amount is inclusive of tax. So to get the actual amount of set off
   * <p>
   * formula(1.a) :- setOffAmount(exclusive of tax) =setOffAmount(inclusive of
   * tax)/(1+(totalTaxRate/100))
   * 
   * (OR)
   * 
   * formula(1.b) :- setOffAmount(exclusive of tax) =setOffAmount(inclusive of tax)-(sum of
   * subgroups tax amount)
   * </p>
   * 2. Get the total tax of the setOff
   * <p>
   * formula(2) :- totalTaxAmount = setOffAmount(inclusive of tax) - setOffAmount(exclusive of tax)
   * </p>
   * 3. Proportionate the totalTaxAmount among all tax subgroups
   * <p>
   * formula(3) :- subgroupTaxAmount = (sbugroupTaxRate/totalTaxRate)*totalTaxAmount
   * </p>
   * Process accounting for deposit set off.
   *
   * @param receiptId
   *          the receipt id
   * @param setOffBillNo
   *          the set off bill no
   * @return the list
   */
  public List<HmsAccountingInfoModel> processAccountingForDepositSetOff(String receiptId,
      String setOffBillNo) {
    List<HmsAccountingInfoModel> setOffAccountingDataList = new ArrayList<>();
    HmsAccountingInfoModel setOffAccountingData = processSetOff(receiptId, setOffBillNo);
    if (setOffAccountingData != null) {
      setOffAccountingDataList.add(setOffAccountingData);
    }

    List<HmsAccountingInfoModel> setOffTaxAccountingData = processSetOffTaxAccounting(receiptId,
        setOffBillNo);
    if (!setOffTaxAccountingData.isEmpty()) {
      setOffAccountingDataList.addAll(setOffTaxAccountingData);
    }
    return setOffAccountingDataList;
  }

  /**
   * Process set off.
   *
   * @param receiptId
   *          the receipt id
   * @param setOffBillNo
   *          the set off bill no
   * @return the hms accounting info model
   */
  private HmsAccountingInfoModel processSetOff(String receiptId, String setOffBillNo) {
    BillReceiptsModel billReceipt = allocationRepository.getBillReceipt(receiptId, setOffBillNo);
    if (billReceipt == null) {
      return null;
    }

    BigDecimal setOffAmountWithTax = billReceipt.getAllocatedAmount();
    BigDecimal totalTaxRate = billReceipt.getReceiptNo().getTotalTaxRate();

    // get setoff amount without tax
    BigDecimal setOffAmountWithoutTax = getSetOffAmountWithoutTax(setOffAmountWithTax,
        totalTaxRate, billReceipt);

    HmsAccountingInfoModel setOffAccountingData = receiptsAccountingCommon
        .setSetOffReceiptCommonData(billReceipt);
    setOffAccountingData.setGrossAmount(setOffAmountWithoutTax.abs());
    setOffAccountingData.setNetAmount(setOffAmountWithoutTax.abs());
    setOffAccountingData.setTransactionType(AccountingConstants.TRANSACTION_TYPE_N);
    setOffAccountingData.setDebitAccount(receiptsAccountingCommon.getDepositAccountName(billReceipt
        .getReceiptNo()));
    setOffAccountingData.setCreditAccount(accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_COUNTER_RECEIPTS"));

    setOffAccountingData.setCustom1(setOffAmountWithoutTax.toString());
    setOffAccountingData.setCustom2(null);
    setOffAccountingData.setCustom3(null);
    setOffAccountingData.setCustom4(null);

    setOffAccountingData.setChargeReferenceId(billReceipt.getReceiptNo().getReceiptId());
    setOffAccountingData.setPrimaryId(billReceipt.getReceiptNo().getReceiptId());
    setOffAccountingData.setSecondaryId(billReceipt.getBillNo().getBillNo());
    setOffAccountingData.setPrimaryIdReferenceTable(BillReceiptsModel.class.getAnnotation(
        Table.class).name());
    setOffAccountingData.setVoucherSubType(accountingDataInsertRepo.getVoucherDefinition("VOUCHER_SUB_TYPE_DEPOSIT_SETTLEMENT"));
    return setOffAccountingData;
  }

  /**
   * Gets the sets the off amount without tax.
   *
   * @param setOffAmountWithTax
   *          the set off amount with tax
   * @param totalTaxRate
   *          the total tax rate
   * @param billReceipt
   *          the bill receipt
   * @return the sets the off amount without tax
   */
  private BigDecimal getSetOffAmountWithoutTax(BigDecimal setOffAmountWithTax,
      BigDecimal totalTaxRate, BillReceiptsModel billReceipt) {
    // formula(1.a)
    BigDecimal denominator = ConversionUtils.divideHighPrecision(totalTaxRate, new BigDecimal(100));
    denominator = denominator.add(BigDecimal.ONE);
    BigDecimal setOffAmountWithoutTax = ConversionUtils.divideHighPrecision(setOffAmountWithTax,
        denominator);
    setOffAmountWithoutTax = ConversionUtils.setScale(setOffAmountWithoutTax, true);
    // formula(2)
    BigDecimal totalTaxAmount = setOffAmountWithTax.subtract(setOffAmountWithoutTax);

    Set<ReceiptTaxModel> receiptTaxes = billReceipt.getReceiptNo().getReceiptTaxes();
    BigDecimal totalSubGroupLevelTaxAmount = BigDecimal.ZERO;
    for (ReceiptTaxModel receiptTax : receiptTaxes) {
      BigDecimal subgroupTaxAmount = ConversionUtils.divideHighPrecision(receiptTax.getTaxRate(),
          totalTaxRate);
      subgroupTaxAmount = ConversionUtils
          .setScale(subgroupTaxAmount.multiply(totalTaxAmount), true);
      subgroupTaxAmount = ConversionUtils.setScale(subgroupTaxAmount, false);
      totalSubGroupLevelTaxAmount = totalSubGroupLevelTaxAmount.add(subgroupTaxAmount);
    }
    // formula(1.b)
    return setOffAmountWithTax.subtract(totalSubGroupLevelTaxAmount);
  }

  /**
   * Process set off tax accounting.
   *
   * @param receiptId
   *          the receipt id
   * @param setOffBillNo
   *          the set off bill no
   * @return the list
   */
  private List<HmsAccountingInfoModel> processSetOffTaxAccounting(String receiptId,
      String setOffBillNo) {
    List<HmsAccountingInfoModel> setOffAccountingDataList = new ArrayList<>();
    BillReceiptsModel billReceipt = allocationRepository.getBillReceipt(receiptId, setOffBillNo);
    if (billReceipt == null) {
      return setOffAccountingDataList;
    }

    BigDecimal setOffAmountWithTax = billReceipt.getAllocatedAmount();
    BigDecimal totalTaxRate = billReceipt.getReceiptNo().getTotalTaxRate();
    // formula(1.a)
    BigDecimal denominator = ConversionUtils.divideHighPrecision(totalTaxRate, new BigDecimal(100));
    denominator = denominator.add(BigDecimal.ONE);
    BigDecimal setOffAmountWithoutTax = ConversionUtils.divideHighPrecision(setOffAmountWithTax,
        denominator);
    setOffAmountWithoutTax = ConversionUtils.setScale(setOffAmountWithoutTax, true);
    BigDecimal totalTaxAmount = setOffAmountWithTax.subtract(setOffAmountWithoutTax);

    Set<ReceiptTaxModel> receiptTaxes = billReceipt.getReceiptNo().getReceiptTaxes();
    for (ReceiptTaxModel receiptTax : receiptTaxes) {
      HmsAccountingInfoModel setOffTaxAccountingData = receiptsAccountingCommon
          .setSetOffReceiptCommonData(billReceipt);
      // formula(3)
      BigDecimal subgroupTaxAmount = ConversionUtils.divideHighPrecision(receiptTax.getTaxRate(),
          totalTaxRate);
      subgroupTaxAmount = ConversionUtils
          .setScale(subgroupTaxAmount.multiply(totalTaxAmount), true);

      setOffTaxAccountingData.setGrossAmount(subgroupTaxAmount.abs());
      setOffTaxAccountingData.setNetAmount(subgroupTaxAmount.abs());
      setOffTaxAccountingData.setTransactionType(AccountingConstants.TRANSACTION_TYPE_N);
      setOffTaxAccountingData.setDebitAccount(accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_TAX_LIABILITY_ACC"));
      setOffTaxAccountingData.setCreditAccount(accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_COUNTER_RECEIPTS"));

      setOffTaxAccountingData.setCustom1(subgroupTaxAmount.toString());
      setOffTaxAccountingData.setCustom2(null);
      setOffTaxAccountingData.setCustom3(null);
      setOffTaxAccountingData.setCustom4(receiptTax.getTaxSubGroupId() != null ? receiptTax
          .getTaxSubGroupId().getItemSubgroupName() : null);

      setOffTaxAccountingData.setChargeReferenceId(billReceipt.getReceiptNo().getReceiptId());
      setOffTaxAccountingData.setPrimaryId(receiptTax.getReceiptTaxId().toString());
      setOffTaxAccountingData.setSecondaryId(null);
      setOffTaxAccountingData.setPrimaryIdReferenceTable(ReceiptTaxModel.class.getAnnotation(
          Table.class).name());
      setOffTaxAccountingData.setVoucherSubType(accountingDataInsertRepo.getVoucherDefinition("VOUCHER_SUB_TYPE_DEPOSIT_SETTLEMENT"));
      setOffAccountingDataList.add(setOffTaxAccountingData);
    }

    return setOffAccountingDataList;
  }

}
