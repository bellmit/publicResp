package com.insta.hms.core.fa;

import com.insta.hms.billing.accounting.AccountingConstants;
import com.insta.hms.core.billing.BillModel;
import com.insta.hms.core.billing.BillReceiptsModel;
import com.insta.hms.core.billing.ReceiptModel;
import com.insta.hms.core.patient.PatientDetailsModel;
import com.insta.hms.core.diagnostics.incomingsampleregistration.IncomingSampleRegistrationModel;
import com.insta.hms.core.patient.registration.PatientRegistrationModel;
import com.insta.hms.mdm.tpas.TpaMasterModel;
import com.insta.hms.model.CounterAssociatedAccountgroupViewModel;
import com.insta.hms.model.DepositsReceiptsViewModel;
import com.insta.hms.model.HmsAccountingInfoModel;

import org.codehaus.plexus.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

import javax.persistence.Table;

/**
 * The Class ReceiptsAccountingCommon.
 */
@Component
public class ReceiptsAccountingCommon {

  /** The receipts accounting repository. */
  @Autowired
  private ReceiptsAccountingRepository receiptsAccountingRepository;

  
  /** The accounting data insert repo. */
  @Autowired
  private AccountingDataInsertRepository accountingDataInsertRepo;

  /**
   * Gets the receipt tpa model.
   *
   * @param billReceiptModel
   *          the bill receipt model
   * @return the receipt tpa model
   */
  public TpaMasterModel getReceiptTpaModel(BillReceiptsModel billReceiptModel) {
    if (billReceiptModel != null && billReceiptModel.getSponsorIndex() != null
        && billReceiptModel.getSponsorIndex().equals(Character.valueOf('P'))) {
      return billReceiptModel.getBillNo().getVisitId().getPrimarySponsorId();
    } else if (billReceiptModel != null && billReceiptModel.getSponsorIndex() != null
        && billReceiptModel.getSponsorIndex().equals(Character.valueOf('S'))) {
      return billReceiptModel.getBillNo().getVisitId().getSecondarySponsorId();
    }
    return null;
  }

  /**
   * Gets the visit id.
   *
   * @param billModel
   *          the bill model
   * @return the visit id
   */
  public String getVisitId(BillModel billModel) {
    if (billModel.getVisitId() != null) {
      return billModel.getVisitId().getPatientId();
    } else if (billModel.getStoreRetailCustomers() != null) {
      return billModel.getStoreRetailCustomers().getCustomerId();
    } else {
      return null;
    }
  }

  /**
   * Gets the referraldoctor.
   *
   * @param billModel
   *          the bill model
   * @return the referraldoctor
   */
  private String getReferraldoctor(BillModel billModel) {
    PatientRegistrationModel visit = billModel.getVisitId();
    IncomingSampleRegistrationModel isr = billModel.getIncomingSampleRegistration();
    if (visit != null && visit.getReferralDoctorDoctors() != null
        && visit.getReferralDoctorDoctors().getDoctorName() != null) {
      return visit.getReferralDoctorDoctors().getDoctorName();
    } else if (visit != null && visit.getReferralDoctorReferral() != null
        && visit.getReferralDoctorReferral().getReferalName() != null) {
      return visit.getReferralDoctorReferral().getReferalName();
    } else if (isr != null && isr.getReferringDoctor() != null
        && isr.getReferringDoctor().getReferalName() != null) {
      return isr.getReferringDoctor().getReferalName();
    } else if (isr != null && isr.getReferringDoctorDoctors() != null
        && isr.getReferringDoctorDoctors().getDoctorName() != null) {
      return isr.getReferringDoctorDoctors().getDoctorName();
    }
    return null;
  }

  /**
   * Sets the receipt common data.
   *
   * @param receipt
   *          the receipt
   * @return the hms accounting info model
   */
  public HmsAccountingInfoModel setReceiptCommonData(ReceiptModel receipt) {
    BillReceiptsModel billReceiptModel = receipt.getBillReceipts().iterator().next();
    if (billReceiptModel == null) {
      return null;
    }
    BillModel billModel = billReceiptModel.getBillNo();
    PatientRegistrationModel visit = billModel.getVisitId();
    
    HmsAccountingInfoModel receiptAccountingData = new HmsAccountingInfoModel();
    receiptAccountingData.setMrNo(receipt.getMrNo());
    receiptAccountingData.setOldMrNo(visit != null ? visit
        .getPatientDetails().getOldmrno() : null);
    receiptAccountingData.setVisitId(getVisitId(billModel));
    receiptAccountingData.setVisitType(visit != null ? visit
        .getVisitType() : null);
    if (visit != null) {
      receiptAccountingData.setAdmittingDoctor(visit.getDoctor() != null ? visit
          .getDoctor().getDoctorName() : null);
      receiptAccountingData
              .setAdmittingDepartment(visit.getDeptName() != null ? visit
                  .getDeptName().getDeptName() : null);
    }
    receiptAccountingData.setReferralDoctor(getReferraldoctor(billModel));
    receiptAccountingData.setCenterId(receipt.getCountersModel() != null ? receipt
        .getCountersModel().getCenterId().getCenterId() : Integer.valueOf(0));
    receiptAccountingData.setCenterName(receipt.getCountersModel() != null ? receipt
        .getCountersModel().getCenterId().getCenterName() : null);
    receiptAccountingData.setVoucherNo(receipt.getReceiptId());
    receiptAccountingData.setVoucherDate(receipt.getDisplayDate());
    receiptAccountingData.setVoucherType(accountingDataInsertRepo.getVoucherDefinition("VOUCHER_TYPE_RECEIPT"));
    receiptAccountingData.setVoucherRef(null);
    receiptAccountingData.setBillNo(billModel.getBillNo());
    receiptAccountingData.setBillOpenDate(billModel.getOpenDate());
    receiptAccountingData.setBillFinalizedDate(billModel.getFinalizedDate());
    receiptAccountingData.setBillLastFinalizedDate(billModel.getLastFinalizedAt());
    receiptAccountingData.setAuditControlNumber(billModel.getAuditControlNumber());
    receiptAccountingData.setAccountGroup(billModel.getAccountGroup());
    receiptAccountingData.setPointsRedeemed(Integer.valueOf(0));
    receiptAccountingData.setPointsRedeemedRate(BigDecimal.ZERO);
    receiptAccountingData.setPointsRedeemedAmount(BigDecimal.ZERO);
    receiptAccountingData.setIsTpa(billModel.getIsTpa() ? AccountingConstants.IS_TPA_Y
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

    receiptAccountingData.setGrossAmount(BigDecimal.ZERO);
    receiptAccountingData.setNetAmount(BigDecimal.ZERO);
    receiptAccountingData.setTransactionType(null);
    receiptAccountingData.setDebitAccount(null);
    receiptAccountingData.setCreditAccount(null);

    receiptAccountingData.setCustom1(null);
    receiptAccountingData.setCustom2(null);
    receiptAccountingData.setCustom3(null);
    receiptAccountingData.setCustom4(null);
    receiptAccountingData.setChargeReferenceId(receipt.getReceiptId());
    receiptAccountingData.setPrimaryId(receipt.getReceiptId());
    receiptAccountingData.setSecondaryId(null);
    receiptAccountingData.setPrimaryIdReferenceTable(ReceiptModel.class.getAnnotation(Table.class)
        .name());
    if (billModel.getIncomingSampleRegistration() != null) {
      receiptAccountingData
          .setPatientFullName(billModel.getIncomingSampleRegistration().getPatientName());
    } else {
      receiptAccountingData.setPatientFullName(
          receipt.getPatientDetails() != null ? getPatientFullName(receipt.getPatientDetails())
              : billModel.getStoreRetailCustomers().getCustomerName());
    }

    return receiptAccountingData;
  }

  /**
   * Sets the deposit receipt common data.
   *
   * @param receipt
   *          the receipt
   * @return the hms accounting info model
   */
  public HmsAccountingInfoModel setDepositReceiptCommonData(ReceiptModel receipt) {
    DepositsReceiptsViewModel depoReceiptsViewModel =
        (DepositsReceiptsViewModel) receiptsAccountingRepository
        .load(DepositsReceiptsViewModel.class, receipt.getReceiptId());
    CounterAssociatedAccountgroupViewModel caavModel = null;
    if (depoReceiptsViewModel != null) {
      caavModel = depoReceiptsViewModel.getCounter();
    }
    HmsAccountingInfoModel receiptAccountingData = new HmsAccountingInfoModel();

    receiptAccountingData.setMrNo(receipt.getMrNo());
    receiptAccountingData.setOldMrNo(receipt.getPatientDetails() != null ? receipt
        .getPatientDetails().getOldmrno() : null);
    receiptAccountingData.setVisitId(null);
    receiptAccountingData.setVisitType(null);
    receiptAccountingData.setAdmittingDoctor(null);
    receiptAccountingData.setAdmittingDepartment(null);
    receiptAccountingData.setReferralDoctor(null);
    receiptAccountingData.setPatientFullName( getPatientFullName(receipt.getPatientDetails()));
    receiptAccountingData.setVoucherNo(receipt.getReceiptId());
    receiptAccountingData.setVoucherDate(receipt.getDisplayDate());
    receiptAccountingData.setVoucherType(accountingDataInsertRepo.getVoucherDefinition("VOUCHER_TYPE_RECEIPT"));
    receiptAccountingData.setVoucherRef(null);
    receiptAccountingData.setBillNo(null);
    receiptAccountingData.setBillOpenDate(null);
    receiptAccountingData.setBillFinalizedDate(null);
    receiptAccountingData.setAuditControlNumber(null);
    receiptAccountingData.setAccountGroup(Integer.valueOf(1));
    receiptAccountingData.setPointsRedeemed(Integer.valueOf(0));
    receiptAccountingData.setPointsRedeemedRate(BigDecimal.ZERO);
    receiptAccountingData.setPointsRedeemedAmount(BigDecimal.ZERO);
    receiptAccountingData.setIsTpa(null);
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

    receiptAccountingData.setCenterId(receipt.getCountersModel() != null ? receipt
        .getCountersModel().getCenterId().getCenterId() : Integer.valueOf(0));
    receiptAccountingData.setCenterName(receipt.getCountersModel() != null ? receipt
        .getCountersModel().getCenterId().getCenterName() : null);

    receiptAccountingData.setGrossAmount(BigDecimal.ZERO);
    receiptAccountingData.setNetAmount(BigDecimal.ZERO);
    receiptAccountingData.setTransactionType(null);
    receiptAccountingData.setDebitAccount(null);
    receiptAccountingData.setCreditAccount(null);

    receiptAccountingData.setCustom1(null);
    receiptAccountingData.setCustom2(null);
    receiptAccountingData.setCustom3(null);
    receiptAccountingData.setCustom4(null);
    receiptAccountingData.setChargeReferenceId(receipt.getReceiptId());
    receiptAccountingData.setPrimaryId(receipt.getReceiptId());
    receiptAccountingData.setSecondaryId(null);
    receiptAccountingData.setPrimaryIdReferenceTable(ReceiptModel.class.getAnnotation(Table.class)
        .name());

    return receiptAccountingData;
  }

  /**
   * Sets the set off receipt common data.
   *
   * @param billReceipt
   *          the bill receipt
   * @return the hms accounting info model
   */
  public HmsAccountingInfoModel setSetOffReceiptCommonData(BillReceiptsModel billReceipt) {

    HmsAccountingInfoModel setOffAccountingData = new HmsAccountingInfoModel();

    setOffAccountingData.setMrNo(billReceipt.getReceiptNo().getMrNo());
    setOffAccountingData.setOldMrNo(billReceipt.getBillNo().getVisitId() != null ? billReceipt
        .getBillNo().getVisitId().getPatientDetails().getOldmrno() : null);
    setOffAccountingData.setVisitId(billReceipt.getBillNo().getVisitId() != null ? billReceipt
        .getBillNo().getVisitId().getPatientId() : null);
    setOffAccountingData.setVisitType(billReceipt.getBillNo().getVisitId() != null ? billReceipt
        .getBillNo().getVisitId().getVisitType() : null);
    setOffAccountingData.setAdmittingDoctor(null);
    setOffAccountingData.setAdmittingDepartment(null);
    setOffAccountingData.setReferralDoctor(null);

    setOffAccountingData.setVoucherNo(billReceipt.getReceiptNo().getReceiptId());
    setOffAccountingData.setVoucherDate(billReceipt.getModTime());
    setOffAccountingData.setVoucherType(accountingDataInsertRepo.getVoucherDefinition("VOUCHER_TYPE_RECEIPT"));
    setOffAccountingData.setVoucherRef(null);
    setOffAccountingData.setBillNo(billReceipt.getBillNo().getBillNo());
    setOffAccountingData.setBillOpenDate(billReceipt.getBillNo().getOpenDate());
    setOffAccountingData.setBillFinalizedDate(billReceipt.getBillNo().getFinalizedDate());
    setOffAccountingData.setBillLastFinalizedDate(billReceipt.getBillNo().getLastFinalizedAt());
    setOffAccountingData.setAuditControlNumber(null);
    setOffAccountingData.setAccountGroup(billReceipt.getBillNo().getAccountGroup());
    setOffAccountingData.setPointsRedeemed(Integer.valueOf(0));
    setOffAccountingData.setPointsRedeemedRate(BigDecimal.ZERO);
    setOffAccountingData.setPointsRedeemedAmount(BigDecimal.ZERO);
    setOffAccountingData.setIsTpa(null);
    setOffAccountingData.setRemarks(billReceipt.getReceiptNo().getRemarks());

    setOffAccountingData.setCurrencyConversionRate(billReceipt.getReceiptNo().getExchangeRate());
    setOffAccountingData.setUnitRate(BigDecimal.ZERO);
    setOffAccountingData.setRoundOffAmount(BigDecimal.ZERO);
    setOffAccountingData.setInvoiceNo(null);
    setOffAccountingData.setInvoiceDate(null);
    setOffAccountingData.setPoNumber(null);
    setOffAccountingData.setPoDate(null);
    setOffAccountingData.setSupplierName(null);
    setOffAccountingData.setCustSupplierCode(null);
    setOffAccountingData.setGrnDate(null);
    setOffAccountingData.setPurchaseVatAmount(BigDecimal.ZERO);
    setOffAccountingData.setPurchaseVatPercent(BigDecimal.ZERO);
    setOffAccountingData.setSalesVatAmount(BigDecimal.ZERO);
    setOffAccountingData.setSalesVatPercent(BigDecimal.ZERO);

    setOffAccountingData.setItemCode(null);
    setOffAccountingData.setItemName(null);
    setOffAccountingData.setChargeGroup(null);
    setOffAccountingData.setChargeHead(null);
    setOffAccountingData.setServiceGroup(null);
    setOffAccountingData.setServiceSubGroup(null);
    setOffAccountingData.setQuantity(BigDecimal.ZERO);
    setOffAccountingData.setUnit(null);
    setOffAccountingData.setDiscountAmount(BigDecimal.ZERO);
    setOffAccountingData.setPrescribingDoctor(null);
    setOffAccountingData.setPrescribingDoctorDeptName(null);
    setOffAccountingData.setConductiongDoctor(null);
    setOffAccountingData.setConductingDepartment(null);
    setOffAccountingData.setPayeeDoctor(null);
    setOffAccountingData.setModTime(billReceipt.getReceiptNo().getModifiedAt());
    setOffAccountingData.setIssueStore(null);
    setOffAccountingData.setIssueStoreCenter(null);
    setOffAccountingData.setCounterNo(null);
    setOffAccountingData.setItemCategoryId(0);
    setOffAccountingData.setReceiptStore(null);
    setOffAccountingData.setReceiptStoreCenter(null);
    setOffAccountingData.setCurrency(null);
    setOffAccountingData.setOuthouseName(null);
    setOffAccountingData.setIncoimngHospital(null);
    setOffAccountingData.setCustItemCode(null);
    setOffAccountingData.setTaxAmount(billReceipt.getReceiptNo().getTdsAmount());
    setOffAccountingData.setCostAmount(BigDecimal.ZERO);
    setOffAccountingData.setUpdateStatus(0);

    setOffAccountingData.setCenterId(billReceipt.getBillNo().getVisitId() != null ? billReceipt
        .getBillNo().getVisitId().getCenterId().getCenterId() : null);
    setOffAccountingData.setCenterName(billReceipt.getBillNo().getVisitId() != null ? billReceipt
        .getBillNo().getVisitId().getCenterId().getCenterName() : null);

    setOffAccountingData.setGrossAmount(BigDecimal.ZERO);
    setOffAccountingData.setNetAmount(BigDecimal.ZERO);
    setOffAccountingData.setTransactionType(null);
    setOffAccountingData.setDebitAccount(null);
    setOffAccountingData.setCreditAccount(null);

    setOffAccountingData.setCustom1(null);
    setOffAccountingData.setCustom2(null);
    setOffAccountingData.setCustom3(null);
    setOffAccountingData.setCustom4(null);
    setOffAccountingData.setChargeReferenceId(null);
    setOffAccountingData.setPrimaryId(null);
    setOffAccountingData.setSecondaryId(null);
    setOffAccountingData.setPrimaryIdReferenceTable(null);
    return setOffAccountingData;
  }

  /**
   * Gets the deposit account name.
   *
   * @param depoReceiptsViewModel
   *          the depo receipts view model
   * @return the deposit account name
   */
  public String getDepositAccountName(DepositsReceiptsViewModel depoReceiptsViewModel) {
    String depositAvailableFor = depoReceiptsViewModel.getDepositAvailableFor();
    if (depositAvailableFor.equals("I")) {
      return "IP Deposit Liability Account";
    } else if (depoReceiptsViewModel.getPackageId() != null) {
      return "Package Deposit Liability Account";
    } else {
      return "General Deposit Liability Account";
    }
  }

  /**
   * Gets the deposit account name.
   *
   * @param depositReceipt
   *          the deposit receipt
   * @return the deposit account name
   */
  public String getDepositAccountName(ReceiptModel depositReceipt) {
    DepositsReceiptsViewModel depoReceiptsViewModel =
        (DepositsReceiptsViewModel) receiptsAccountingRepository
        .load(DepositsReceiptsViewModel.class, depositReceipt.getReceiptId());
    return depositReceipt.getIsDeposit() ? getDepositAccountName(depoReceiptsViewModel) : null;
  }
  
  private String getPatientFullName(PatientDetailsModel pdm) {
    String salutation = "";
    if (!StringUtils.isEmpty(pdm.getSalutation())) {
      salutation = !StringUtils.isEmpty(pdm.getSalutationMaster().getSalutation())
          ? (pdm.getSalutationMaster().getSalutation() + " ") : ""; 
    }
  String firstName =  !StringUtils.isEmpty(pdm.getPatientName()) ? (pdm.getPatientName() + " ") : "";
  String middleName =  !StringUtils.isEmpty(pdm.getMiddleName()) ? (pdm.getMiddleName() + " ") : "";
  String lastName = !StringUtils.isEmpty(pdm.getLastName())  ? pdm.getLastName() : "";
  return ( salutation + firstName + middleName + lastName); 
  }

}
