package com.insta.hms.core.fa;

import com.insta.hms.billing.accounting.AccountingConstants;
import com.insta.hms.core.billing.BillModel;
import com.insta.hms.core.billing.BillReceiptsModel;
import com.insta.hms.core.billing.ReceiptModel;
import com.insta.hms.mdm.tpas.TpaMasterModel;
import com.insta.hms.model.HmsAccountingInfoModel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Table;

/**
 * The Class SponsorWriteOffRefundAccountingProcessor.
 */
@Component
public class SponsorWriteOffRefundAccountingProcessor extends AbstractReceiptsAccounting {

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
    BillReceiptsModel billReceiptModel = receiptsAccountingRepository
        .getBillReceiptModelForWriteOffRefundReceipt(receipt.getReceiptId());
    BillModel billModel = billReceiptModel != null ? billReceiptModel.getBillNo() : null;

    HmsAccountingInfoModel receiptAccountingData = new HmsAccountingInfoModel();
    List<HmsAccountingInfoModel> receiptAccountingDataList = new ArrayList<>();
    receiptAccountingData.setMrNo(receipt.getMrNo());
    receiptAccountingData
        .setOldMrNo(billModel != null && billModel.getVisitId() != null ? billModel.getVisitId()
            .getPatientDetails().getOldmrno() : null);
    receiptAccountingData.setVisitId(billModel != null ? receiptsAccountingCommon
        .getVisitId(billModel) : null);
    receiptAccountingData
        .setVisitType(billModel != null && billModel.getVisitId() != null ? billModel.getVisitId()
            .getVisitType() : null);
    receiptAccountingData.setAdmittingDoctor(null);
    receiptAccountingData.setAdmittingDepartment(null);
    receiptAccountingData.setReferralDoctor(null);

    receiptAccountingData.setVoucherNo(receipt.getReceiptId());
    receiptAccountingData.setVoucherDate(receipt.getDisplayDate());
    receiptAccountingData.setVoucherType(accountingDataInsertRepo.getVoucherDefinition("VOUCHER_TYPE_PAYMENT"));
    receiptAccountingData.setVoucherRef(null);
    receiptAccountingData.setBillNo(billModel != null ? billModel.getBillNo() : null);
    receiptAccountingData.setBillOpenDate(billModel != null ? billModel.getOpenDate() : null);
    receiptAccountingData.setBillFinalizedDate(billModel != null ? billModel.getFinalizedDate()
        : null);
    receiptAccountingData.setBillLastFinalizedDate(
        billModel != null ? billModel.getLastFinalizedAt() : null);
    receiptAccountingData.setAuditControlNumber(billModel != null ? billModel
        .getAuditControlNumber() : null);
    receiptAccountingData.setAccountGroup(billModel != null ? billModel.getAccountGroup() : null);
    receiptAccountingData.setPointsRedeemed(Integer.valueOf(0));
    receiptAccountingData.setPointsRedeemedRate(BigDecimal.ZERO);
    receiptAccountingData.setPointsRedeemedAmount(BigDecimal.ZERO);
    receiptAccountingData
        .setIsTpa(billModel != null && billModel.getIsTpa() ? AccountingConstants.IS_TPA_Y
            : AccountingConstants.IS_TPA_N);
    receiptAccountingData.setRemarks(receipt.getRemarks());

    receiptAccountingData.setCurrencyConversionRate(receipt.getExchangeRate());
    receiptAccountingData.setUnitRate(BigDecimal.ZERO);
    receiptAccountingData.setRoundOffAmount(BigDecimal.ZERO);
    receiptAccountingData.setInvoiceNo(null);
    receiptAccountingData.setInvoiceDate(null);
    receiptAccountingData.setPoNumber(null);
    receiptAccountingData.setPoDate(null);
    receiptAccountingData.setSupplierName(null);
    receiptAccountingData.setCustSupplierCode(null);
    receiptAccountingData.setGrnDate(null);
    receiptAccountingData.setPurchaseVatAmount(BigDecimal.ZERO);
    receiptAccountingData.setPurchaseVatPercent(BigDecimal.ZERO);
    receiptAccountingData.setSalesVatAmount(BigDecimal.ZERO);
    receiptAccountingData.setSalesVatPercent(BigDecimal.ZERO);

    receiptAccountingData.setItemCode(null);
    receiptAccountingData.setItemName(null);
    receiptAccountingData.setChargeGroup(null);
    receiptAccountingData.setChargeHead(null);
    receiptAccountingData.setServiceGroup(null);
    receiptAccountingData.setServiceSubGroup(null);
    receiptAccountingData.setQuantity(BigDecimal.ZERO);
    receiptAccountingData.setUnit(null);
    receiptAccountingData.setDiscountAmount(BigDecimal.ZERO);
    receiptAccountingData.setPrescribingDoctor(null);
    receiptAccountingData.setPrescribingDoctorDeptName(null);
    receiptAccountingData.setConductiongDoctor(null);
    receiptAccountingData.setConductingDepartment(null);
    receiptAccountingData.setPayeeDoctor(null);
    receiptAccountingData.setModTime(receipt.getModifiedAt());
    receiptAccountingData.setIssueStore(null);
    receiptAccountingData.setIssueStoreCenter(null);
    receiptAccountingData.setCounterNo(receipt.getCountersModel() != null ? receipt
        .getCountersModel().getCounterNo() : null);
    receiptAccountingData.setItemCategoryId(0);
    receiptAccountingData.setReceiptStore(null);
    receiptAccountingData.setReceiptStoreCenter(null);
    receiptAccountingData.setCurrency(receipt.getForeignCurrencyModel() != null ? receipt
        .getForeignCurrencyModel().getCurrency() : null);
    receiptAccountingData.setOuthouseName(null);
    receiptAccountingData.setIncoimngHospital(null);
    receiptAccountingData.setCustItemCode(null);
    receiptAccountingData.setTaxAmount(receipt.getTdsAmount());
    receiptAccountingData.setCostAmount(BigDecimal.ZERO);
    receiptAccountingData.setUpdateStatus(0);

    receiptAccountingData
        .setCenterId(billModel != null && billModel.getVisitId() != null ? billModel.getVisitId()
            .getCenterId().getCenterId() : Integer.valueOf(0));
    receiptAccountingData
        .setCenterName(billModel != null && billModel.getVisitId() != null ? billModel.getVisitId()
            .getCenterId().getCenterName() : null);
    receiptAccountingData.setCounterNo(null);
    receiptAccountingData.setGrossAmount(receipt.getAmount().abs());
    receiptAccountingData.setNetAmount(receipt.getAmount().subtract(receipt.getTdsAmount()).abs());
    receiptAccountingData.setTransactionType(AccountingConstants.TRANSACTION_TYPE_N);
    TpaMasterModel tpaModel = receiptsAccountingCommon.getReceiptTpaModel(billReceiptModel);
    receiptAccountingData.setDebitAccount(tpaModel != null ? tpaModel.getTpaName()
        : accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_COUNTER_RECEIPTS"));
    receiptAccountingData.setCreditAccount(accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_WRITEOFF_ACC"));

    receiptAccountingData.setCustom1(receipt.getAmount().toString());
    receiptAccountingData.setCustom2(tpaModel != null ? tpaModel.getTpaName() : null);
    receiptAccountingData.setCustom3(tpaModel != null ? String.valueOf(tpaModel.getSponsorType())
        : null);
    receiptAccountingData.setCustom4(null);

    receiptAccountingData.setChargeReferenceId(receipt.getReceiptId());
    receiptAccountingData.setPrimaryId(receipt.getReceiptId());
    receiptAccountingData.setSecondaryId(null);
    receiptAccountingData.setPrimaryIdReferenceTable(ReceiptModel.class.getAnnotation(Table.class)
        .name());
    receiptAccountingData.setVoucherSubType(accountingDataInsertRepo.getVoucherDefinition("VOUCHER_SUB_TYPE_SPONSOR_WRITEOFF_REFUND"));

    receiptAccountingDataList.add(receiptAccountingData);

    return receiptAccountingDataList;
  }

}
