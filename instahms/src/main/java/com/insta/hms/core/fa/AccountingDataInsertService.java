package com.insta.hms.core.fa;

import com.insta.hms.billing.accounting.AccountingConstants;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.preferences.genericpreferences.GenericPreferencesService;
import com.insta.hms.core.billing.BillChargeClaimModel;
import com.insta.hms.core.billing.BillChargeClaimTaxModel;
import com.insta.hms.core.billing.BillChargeModel;
import com.insta.hms.core.billing.BillChargeTaxModel;
import com.insta.hms.core.billing.BillModel;
import com.insta.hms.core.diagnostics.incomingsampleregistration.IncomingSampleRegistrationModel;
import com.insta.hms.core.patient.PatientDetailsModel;
import com.insta.hms.core.patient.registration.PatientRegistrationModel;
import com.insta.hms.mdm.insurancecompanies.InsuranceCompanyMasterModel;
import com.insta.hms.model.AccountingFailedExportsModel;
import com.insta.hms.model.HmsAccountingInfoModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Table;

@Service
/**
 * The Class AccountingDataInsertService.
 */
public class AccountingDataInsertService {

  /** The accounting data insert repo. */
  @Autowired
  private AccountingDataInsertRepository accountingDataInsertRepo;

  /** The logger. */
  private static Logger logger = LoggerFactory.getLogger(AccountingDataInsertService.class);

  @Autowired
  private AccountingFailedExportsRepository failedExportsRepo;

  @Autowired
  private InventoryAccountingDataInsertService invAccountingDataInsertService;

  @Autowired
  private InventoryReturnsAccountingDataInsertService invReturnsAccountingDataInsertService;

  @LazyAutowired
  private GenericPreferencesService genPrefService;


  /**
   * Process accounting for bill.
   *
   * @param billNo          bill number for which accounting data is to be processed
   * @param jobTransaction  job transction identifier
   * @param reversalsOnly   perform only reversals
   * @param createdAt       creation timestamp for voucher
   */
  @Transactional
  public void processAccountingForBill(String billNo, Integer jobTransaction,
      boolean reversalsOnly, Date createdAt) {
    BillModel billModel = accountingDataInsertRepo.getBillModelByBillNo(billNo);
    if (billModel == null) {
      logger.info("No bill model for bill : " + billNo);
      return;
    }
    if (billModel != null && billModel.getRestrictionType() != null) {
      String restrictionType = String.valueOf(billModel.getRestrictionType());
      if (restrictionType.equals("P")) {
        return;
      }
    }
    String billStatus = String.valueOf(billModel.getStatus());
    if (!AccountingConstants.VALID_BILL_STATUSES.contains(billStatus)) {
      logger.info("Skipping. Status changed to " + billStatus + " for bill : " + billNo);
      return;
    }
    if (billStatus.equals(AccountingConstants.BILL_STATUS_CANCELLED)) {
      reversalsOnly = true;
    }
    postReversalsForHospitalItems(billModel.getBillNo(), jobTransaction, createdAt);
    if (!reversalsOnly) {
      insertAccountingDataForHospitalItems(billNo, billModel, jobTransaction, createdAt);
    }
  }

  /**
   * Insert accounting data for hospital items.
   *
   * @param billNo          bill number for which accounting data is to be processed
   * @param billModel       Bill Model
   * @param jobTransaction  job transction identifier
   * @param createdAt       creation timestamp for voucher
   */
  public void insertAccountingDataForHospitalItems(String billNo, BillModel billModel,
      Integer jobTransaction, Date createdAt) {
    List<HmsAccountingInfoModel> accountingDataToInsert = getAccountingDataToInsert(billNo,
        billModel, jobTransaction, createdAt);
    logger.info("Accounting data insert started for bill : " + billNo);
    accountingDataInsertRepo.insertAccountingDataFromList(accountingDataToInsert);
    logger.info("Accounting data insert successful for bill : " + billNo);
  }

  /**
   * Gets the accounting data to insert.
   *
   * @param billModel       Bill Model
   * @param jobTransaction  job transction identifier
   * @param createdAt       creation timestamp for voucher
   * @return the accounting data to insert
   */
  public List<HmsAccountingInfoModel> getAccountingDataToInsert(String billNo, BillModel billModel,
      Integer jobTransaction, Date createdAt) {
    List<HmsAccountingInfoModel> accountingDataList = new ArrayList<HmsAccountingInfoModel>();

    HmsAccountingInfoModel accountingBillDetails = setAccountingBillDetails(billModel);
    accountingBillDetails.setJobTransaction(jobTransaction);
    accountingBillDetails.setCreatedAt(createdAt);
    List<BillChargeModel> charges = accountingDataInsertRepo.getBillChargeModelsByBillNo(billNo);

    for (BillChargeModel charge : charges) {
      if (String.valueOf(charge.getStatus()).equals("X")) {
        continue;
      }
      List<String> ignoreChargeHeadsList = Arrays.asList("PHMED", "PHRET", "PHCMED", "PHCRET");
      if (ignoreChargeHeadsList.contains(charge.getChargeHead().getChargeheadId())) {
        continue;
      }
      HmsAccountingInfoModel accountingChargeDetails = setAccountingChargeDetails(charge,
          accountingBillDetails);

      if (charge.getChargeHead().getChargeheadId().equals("INVITE")) {
        List<HmsAccountingInfoModel> inventoryItemAccountingData = invAccountingDataInsertService
            .getAccountingDataForInventoryItem(charge, billModel, accountingChargeDetails);
        if (inventoryItemAccountingData != null && !inventoryItemAccountingData.isEmpty()) {
          accountingDataList.addAll(inventoryItemAccountingData);
        }

        // post item level discounts into accounting for inventory items.
        if (accountingDataInsertRepo.getfaConfiguration("post_discount_vouchers")
            .equals("B")) {
          HmsAccountingInfoModel itemLevelDiscountEntry = null;
          if (accountingDataInsertRepo.getfaConfiguration("use_credit_ac_for_discount")
              .equals("Y")) {
            itemLevelDiscountEntry = getItemLevelDiscountAmountAccountingEntry(charge,
                accountingChargeDetails);
          } else {
            itemLevelDiscountEntry = getDiscountAmountAccountingEntry(charge,
                accountingChargeDetails);
          }
          if (itemLevelDiscountEntry != null) {
            accountingDataList.add(itemLevelDiscountEntry);
          }
        }

        continue;
      }

      if (charge.getChargeHead().getChargeheadId().equals("INVRET")) {
        List<HmsAccountingInfoModel> inventoryItemAccountingData = 
            invReturnsAccountingDataInsertService
                .getAccountingDataForInventoryItem(charge, billModel, accountingChargeDetails);
        if (inventoryItemAccountingData != null && !inventoryItemAccountingData.isEmpty()) {
          accountingDataList.addAll(inventoryItemAccountingData);
        }

        // post item level discounts into accounting for inventory item returns.
        if (accountingDataInsertRepo.getfaConfiguration("post_discount_vouchers")
            .equals("B")) {
          HmsAccountingInfoModel itemLevelDiscountEntry = null;
          if (accountingDataInsertRepo.getfaConfiguration("use_credit_ac_for_discount")
              .equals("Y")) {
            itemLevelDiscountEntry = getItemLevelDiscountAmountAccountingEntry(charge,
                accountingChargeDetails);
          } else {
            itemLevelDiscountEntry = getDiscountAmountAccountingEntry(charge,
                accountingChargeDetails);
          }
          if (itemLevelDiscountEntry != null) {
            accountingDataList.add(itemLevelDiscountEntry);
          }
        }
        continue;
      }

      // If bill is Non-TPA bill, amount itself is patient amount and its tax is patient tax
      // We no need to go through bill charge claims and taxes.
      if (!billModel.getIsTpa()) {
        HmsAccountingInfoModel patientAmountEntry = getPatientAmountAccountingEntry(charge,
            accountingChargeDetails);
        if (patientAmountEntry != null) {
          accountingDataList.add(patientAmountEntry);
        }
        Set<BillChargeTaxModel> billChargeTaxes = charge.getBillChargeTaxes();
        List<HmsAccountingInfoModel> patientTaxEntries = getPatientTaxAmountAccountingEntries(
            billChargeTaxes, accountingChargeDetails);
        if (patientTaxEntries != null && patientTaxEntries.size() > 0) {
          accountingDataList.addAll(patientTaxEntries);
        }

        // post item level discounts into accounting.
        if (accountingDataInsertRepo.getfaConfiguration("post_discount_vouchers").equals("B")) {
          HmsAccountingInfoModel itemLevelDiscountEntry = null;
          if (accountingDataInsertRepo.getfaConfiguration("use_credit_ac_for_discount")
              .equals("Y")) {
            itemLevelDiscountEntry = getItemLevelDiscountAmountAccountingEntry(charge,
                accountingChargeDetails);
          } else {
            itemLevelDiscountEntry = getDiscountAmountAccountingEntry(charge,
                accountingChargeDetails);
          }
          if (itemLevelDiscountEntry != null) {
            accountingDataList.add(itemLevelDiscountEntry);
          }
        }

        continue;
      }

      // If bill is TPA bill, Then we need to post pri,sec sponsor amounts and its taxes
      // bill charge claims setters
      Set<BillChargeClaimModel> billChargeClaims = charge.getBillChargeClaims();
      Map<String, BigDecimal> chargeTaxIdClaimTaxMap = new HashMap<String, BigDecimal>();
      for (BillChargeClaimModel chargeClaim : billChargeClaims) {
        if (chargeClaim.getInsuranceClaimAmt().compareTo(BigDecimal.ZERO) == 0) {
          continue;
        }
        HmsAccountingInfoModel claimEntry = getClaimAmountAccountingEntries(chargeClaim,
            accountingChargeDetails, charge);
        if (claimEntry != null) {
          accountingDataList.add(claimEntry);
        }
        Set<BillChargeClaimTaxModel> billChargeClaimTaxes = chargeClaim.getBillChargeClaimTaxes();
        List<HmsAccountingInfoModel> claimTaxEntries = getClaimTaxAmountAccountingEntries(
            billChargeClaimTaxes, chargeClaim, accountingChargeDetails, chargeTaxIdClaimTaxMap);
        if (claimTaxEntries != null && claimTaxEntries.size() > 0) {
          accountingDataList.addAll(claimTaxEntries);
        }
      }

      // posting Patient amount and patient tax for TPA bills
      HmsAccountingInfoModel patientAmountEntry = getInsurancePatientAmountAccountingEntry(charge,
          accountingChargeDetails);
      if (patientAmountEntry != null) {
        accountingDataList.add(patientAmountEntry);
      }
      Set<BillChargeTaxModel> billChargeTaxes = charge.getBillChargeTaxes();
      List<HmsAccountingInfoModel> patientTaxEntries = getInsurancePatientTaxAmountAccountingEntry(
          billChargeTaxes, charge, accountingChargeDetails, chargeTaxIdClaimTaxMap);
      if (patientTaxEntries != null && patientTaxEntries.size() > 0) {
        accountingDataList.addAll(patientTaxEntries);
      }

      // post item level discounts into accounting.
      if (accountingDataInsertRepo.getfaConfiguration("post_discount_vouchers").equals("B")) {
        HmsAccountingInfoModel itemLevelDiscountEntry = null;
        if (accountingDataInsertRepo.getfaConfiguration("use_credit_ac_for_discount").equals("Y")) {
          itemLevelDiscountEntry = getItemLevelDiscountAmountAccountingEntry(charge,
              accountingChargeDetails);
        } else {
          itemLevelDiscountEntry = getDiscountAmountAccountingEntry(charge,
              accountingChargeDetails);
        }
        if (itemLevelDiscountEntry != null) {
          accountingDataList.add(itemLevelDiscountEntry);
        }
      }
    }
    return accountingDataList;
  }

  /**
   * Gets the patient amount accounting entry.
   *
   * @param charge
   *          the charge
   * @param accountingChargeDetails
   *          the accounting charge details
   * @return the patient amount accounting entry
   */
  private HmsAccountingInfoModel getPatientAmountAccountingEntry(BillChargeModel charge,
      HmsAccountingInfoModel accountingChargeDetails) {
    HmsAccountingInfoModel patientAmountEntry = new HmsAccountingInfoModel();
    org.springframework.beans.BeanUtils.copyProperties(accountingChargeDetails, patientAmountEntry);
    BigDecimal grossAmount = charge.getAmount().add(charge.getDiscount());
    if (charge.getChargeHead().getChargeheadId().equals("BIDIS") || grossAmount.compareTo(BigDecimal.ZERO) != 0) {
      patientAmountEntry.setGrossAmount(
          grossAmount.compareTo(BigDecimal.ZERO) >= 0 ? grossAmount
          : (charge.getAmount().add(charge.getDiscount())).negate());
      patientAmountEntry.setNetAmount(charge.getAmount().compareTo(BigDecimal.ZERO) >= 0 ? charge
          .getAmount() : charge.getAmount().negate());
      patientAmountEntry.setTransactionType(AccountingConstants.TRANSACTION_TYPE_N);
      patientAmountEntry
          .setDebitAccount(charge.getAmount().compareTo(BigDecimal.ZERO) >= 0
            ? accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_COUNTER_RECEIPTS")
              : getChargeAccountHeadName(charge));
      patientAmountEntry
          .setCreditAccount(charge.getAmount().compareTo(BigDecimal.ZERO) >= 0
            ? getChargeAccountHeadName(charge)
              : accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_COUNTER_RECEIPTS"));

      patientAmountEntry.setCustom1(accountingDataInsertRepo.getfaConfiguration("use_credit_ac_for_discount").equals("Y")
          ? charge.getAmount().toString() : grossAmount.toString());
      patientAmountEntry.setCustom2(null);
      patientAmountEntry.setCustom3(null);
      patientAmountEntry.setCustom4(getChargeAccountHeadName(charge));
      patientAmountEntry.setGuid(accountingDataInsertRepo.generateAccountingNextId());
      return patientAmountEntry;
    }  
    return null;
  }

  /**
   * Gets the patient tax amount accounting entries.
   *
   * @param billChargeTaxes
   *          the bill charge taxes
   * @param accountingChargeDetails
   *          the accounting charge details
   * @return the patient tax amount accounting entries
   */
  private List<HmsAccountingInfoModel> getPatientTaxAmountAccountingEntries(
      Set<BillChargeTaxModel> billChargeTaxes, HmsAccountingInfoModel accountingChargeDetails) {
    List<HmsAccountingInfoModel> patientTaxAmountEntryList = new ArrayList<>();
    for (BillChargeTaxModel bcTax : billChargeTaxes) {
      if (!accountingDataInsertRepo.getfaConfiguration("post_zero_tax").equals("Y")
          && bcTax.getTaxAmount().compareTo(BigDecimal.ZERO) == 0) {
        continue;
      }
      HmsAccountingInfoModel patientTaxEntry = new HmsAccountingInfoModel();
      org.springframework.beans.BeanUtils.copyProperties(accountingChargeDetails, patientTaxEntry);
      patientTaxEntry.setGrossAmount(bcTax.getTaxAmount());
      patientTaxEntry.setNetAmount(bcTax.getTaxAmount());
      patientTaxEntry
          .setDebitAccount(bcTax.getTaxAmount().compareTo(BigDecimal.ZERO) >= 0
            ? accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_COUNTER_RECEIPTS")
              : accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_TAX_LIABILITY_ACC"));
      patientTaxEntry
          .setCreditAccount(bcTax.getTaxAmount().compareTo(BigDecimal.ZERO) >= 0
            ? accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_TAX_LIABILITY_ACC")
              : accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_COUNTER_RECEIPTS"));
      patientTaxEntry.setCustom1(bcTax.getTaxAmount().toString());
      patientTaxEntry.setDiscountAmount(BigDecimal.ZERO);
      patientTaxEntry.setCustom2(null);
      patientTaxEntry.setCustom3(null);
      patientTaxEntry.setCustom4(bcTax.getTaxSubGroupId().getItemSubgroupName());
      patientTaxEntry.setPrimaryId(String.valueOf(bcTax.getChargeTaxId()));
      patientTaxEntry.setSecondaryId(String.valueOf(bcTax.getTaxSubGroupId().getItemSubgroupId()));
      patientTaxEntry.setPrimaryIdReferenceTable(BillChargeTaxModel.class
          .getAnnotation(Table.class).name());
      patientTaxEntry.setGuid(accountingDataInsertRepo.generateAccountingNextId());
      patientTaxAmountEntryList.add(patientTaxEntry);
    }
    return patientTaxAmountEntryList;
  }

  /**
   * Gets the claim amount accounting entries.
   *
   * @param chargeClaim
   *          the charge claim
   * @param accountingChargeDetails
   *          the accounting charge details
   * @param charge
   *          the charge
   * @return the claim amount accounting entries
   */
  private HmsAccountingInfoModel getClaimAmountAccountingEntries(BillChargeClaimModel chargeClaim,
      HmsAccountingInfoModel accountingChargeDetails, BillChargeModel charge) {
    HmsAccountingInfoModel claimEntry = new HmsAccountingInfoModel();
    org.springframework.beans.BeanUtils.copyProperties(accountingChargeDetails, claimEntry);
    claimEntry
        .setGrossAmount(chargeClaim.getInsuranceClaimAmt().compareTo(BigDecimal.ZERO) >= 0
          ? chargeClaim.getInsuranceClaimAmt() : chargeClaim.getInsuranceClaimAmt().negate());
    claimEntry
        .setNetAmount(chargeClaim.getInsuranceClaimAmt().compareTo(BigDecimal.ZERO) >= 0
          ? chargeClaim.getInsuranceClaimAmt() : chargeClaim.getInsuranceClaimAmt().negate());
    claimEntry
        .setDebitAccount(chargeClaim.getInsuranceClaimAmt().compareTo(BigDecimal.ZERO) >= 0
          ? chargeClaim.getSponsorId().getTpaName() : getChargeAccountHeadName(charge));
    claimEntry
        .setCreditAccount(chargeClaim.getInsuranceClaimAmt().compareTo(BigDecimal.ZERO) >= 0
          ? getChargeAccountHeadName(charge)
            : chargeClaim.getSponsorId().getTpaName());
    if (chargeClaim.getClaimId() != null) {
      InsuranceCompanyMasterModel insCoModel = accountingDataInsertRepo
          .getInsuranceCoModelByClaimId(chargeClaim.getClaimId().getClaimId());
      if (insCoModel != null) {
        claimEntry.setInsuranceCo(insCoModel.getInsuranceCoName());
      } else {
        claimEntry.setInsuranceCo(null);
      }
      claimEntry.setSecondaryId(chargeClaim.getClaimId().getClaimId());      
    } else {
      claimEntry.setInsuranceCo(null);
      claimEntry.setSecondaryId(null);
    }
    claimEntry.setCustom1(chargeClaim.getInsuranceClaimAmt().toString());

    claimEntry.setCustom2(chargeClaim.getSponsorId().getTpaName());
    claimEntry.setCustom3(String.valueOf(chargeClaim.getSponsorId().getSponsorType()));
    claimEntry.setCustom4(getChargeAccountHeadName(charge));
    claimEntry.setPrimaryId(charge.getChargeId());
    claimEntry.setPrimaryIdReferenceTable(BillChargeClaimModel.class.getAnnotation(Table.class)
        .name());
    claimEntry.setGuid(accountingDataInsertRepo.generateAccountingNextId());
    claimEntry.setDiscountAmount(BigDecimal.ZERO);
    return claimEntry;
  }

  /**
   * Gets the claim tax amount accounting entries.
   *
   * @param billChargeClaimTaxes
   *          the bill charge claim taxes
   * @param chargeClaim
   *          the charge claim
   * @param accountingChargeDetails
   *          the accounting charge details
   * @param chargeTaxIdClaimTaxMap
   *          the charge tax id claim tax map
   * @return the claim tax amount accounting entries
   */
  private List<HmsAccountingInfoModel> getClaimTaxAmountAccountingEntries(
      Set<BillChargeClaimTaxModel> billChargeClaimTaxes, BillChargeClaimModel chargeClaim,
      HmsAccountingInfoModel accountingChargeDetails,
      Map<String, BigDecimal> chargeTaxIdClaimTaxMap) {
    List<HmsAccountingInfoModel> accountingClaimTaxEntries = new ArrayList<>();
    for (BillChargeClaimTaxModel bcclTax : billChargeClaimTaxes) {
      if (!accountingDataInsertRepo.getfaConfiguration("post_zero_tax").equals("Y")
          && bcclTax.getSponsorTaxAmount().compareTo(BigDecimal.ZERO) == 0) {
        continue;
      }
      HmsAccountingInfoModel claimTaxEntry = new HmsAccountingInfoModel();
      org.springframework.beans.BeanUtils.copyProperties(accountingChargeDetails, claimTaxEntry);
      claimTaxEntry
          .setGrossAmount(bcclTax.getSponsorTaxAmount().compareTo(BigDecimal.ZERO) >= 0 ? bcclTax
              .getSponsorTaxAmount() : bcclTax.getSponsorTaxAmount().negate());
      claimTaxEntry
          .setNetAmount(bcclTax.getSponsorTaxAmount().compareTo(BigDecimal.ZERO) >= 0 ? bcclTax
              .getSponsorTaxAmount() : bcclTax.getSponsorTaxAmount().negate());
      claimTaxEntry
          .setDebitAccount(bcclTax.getSponsorTaxAmount().compareTo(BigDecimal.ZERO) >= 0 ? (bcclTax
              .getSponsorId() != null ? bcclTax.getSponsorId().getTpaName() : null)
              : accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_TAX_LIABILITY_ACC"));
      if (bcclTax.getClaimId() != null) {
        InsuranceCompanyMasterModel insCoModel = accountingDataInsertRepo
            .getInsuranceCoModelByClaimId(bcclTax.getClaimId());
        if (insCoModel != null) {
          claimTaxEntry.setInsuranceCo(insCoModel.getInsuranceCoName());
        } else {
          claimTaxEntry.setInsuranceCo(null);
        }
      } else {
        claimTaxEntry.setInsuranceCo(null);
      }
      claimTaxEntry
          .setCreditAccount(bcclTax.getSponsorTaxAmount().compareTo(BigDecimal.ZERO) >= 0
            ? accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_TAX_LIABILITY_ACC")
              : (bcclTax.getSponsorId() != null ? bcclTax.getSponsorId().getTpaName() : null));
      claimTaxEntry.setCustom1(bcclTax.getSponsorTaxAmount().toString());
      claimTaxEntry.setCustom2(bcclTax.getSponsorId() != null ? bcclTax.getSponsorId().getTpaName()
          : null);
      claimTaxEntry.setCustom3(String.valueOf(bcclTax.getSponsorId() != null ? bcclTax
          .getSponsorId().getSponsorType() : null));
      claimTaxEntry.setCustom4(bcclTax.getTaxSubGroupId().getItemSubgroupName());
      claimTaxEntry.setDiscountAmount(BigDecimal.ZERO);
      claimTaxEntry.setPrimaryId(String.valueOf(bcclTax.getChargeClaimTaxId()));
      claimTaxEntry.setSecondaryId(String.valueOf(bcclTax.getTaxSubGroupId().getItemSubgroupId()));
      claimTaxEntry.setPrimaryIdReferenceTable(BillChargeClaimTaxModel.class.getAnnotation(
          Table.class).name());
      claimTaxEntry.setGuid(accountingDataInsertRepo.generateAccountingNextId());
      accountingClaimTaxEntries.add(claimTaxEntry);
      if (chargeTaxIdClaimTaxMap.containsKey("CTI" + bcclTax.getChargeTaxId())) {
        BigDecimal claimTax = (BigDecimal) chargeTaxIdClaimTaxMap.get("CTI"
            + bcclTax.getChargeTaxId());
        claimTax = claimTax.add(bcclTax.getSponsorTaxAmount());
        chargeTaxIdClaimTaxMap.put("CTI" + bcclTax.getChargeTaxId(), claimTax);
      } else {
        chargeTaxIdClaimTaxMap.put("CTI" + bcclTax.getChargeTaxId(), bcclTax.getSponsorTaxAmount());
      }
    }
    return accountingClaimTaxEntries;
  }

  /**
   * Gets the insurance patient amount accounting entry.
   *
   * @param charge
   *          the charge
   * @param accountingChargeDetails
   *          the accounting charge details
   * @return the insurance patient amount accounting entry
   */
  private HmsAccountingInfoModel getInsurancePatientAmountAccountingEntry(BillChargeModel charge,
      HmsAccountingInfoModel accountingChargeDetails) {
    HmsAccountingInfoModel patientAmountEntry = new HmsAccountingInfoModel();
    BigDecimal grossPatAmount = charge.getAmount().add(charge.getDiscount())
        .subtract(charge.getInsuranceClaimAmount());
    BigDecimal netPatAmount = charge.getAmount().subtract(charge.getInsuranceClaimAmount());
    if (grossPatAmount.compareTo(BigDecimal.ZERO) != 0) {
      org.springframework.beans.BeanUtils.copyProperties(accountingChargeDetails,
          patientAmountEntry);
      patientAmountEntry
          .setGrossAmount(grossPatAmount.compareTo(BigDecimal.ZERO) >= 0 ? grossPatAmount
              : grossPatAmount.negate());
      patientAmountEntry.setNetAmount(netPatAmount.compareTo(BigDecimal.ZERO) >= 0 ? netPatAmount
          : netPatAmount.negate());
      patientAmountEntry.setTransactionType(AccountingConstants.TRANSACTION_TYPE_N);
      patientAmountEntry
          .setDebitAccount(netPatAmount.compareTo(BigDecimal.ZERO) >= 0
            ? accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_COUNTER_RECEIPTS")
              : getChargeAccountHeadName(charge));
      patientAmountEntry
          .setCreditAccount(netPatAmount.compareTo(BigDecimal.ZERO) >= 0
            ? getChargeAccountHeadName(charge)
              : accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_COUNTER_RECEIPTS"));
      patientAmountEntry.setCustom1(accountingDataInsertRepo.getfaConfiguration("use_credit_ac_for_discount").equals("Y")
          ? netPatAmount.toString() : grossPatAmount.toString());
      patientAmountEntry.setCustom2(null);
      patientAmountEntry.setCustom3(null);
      patientAmountEntry.setCustom4(getChargeAccountHeadName(charge));
      patientAmountEntry.setGuid(accountingDataInsertRepo.generateAccountingNextId());
      return patientAmountEntry;
    }
    return null;
  }

  /**
   * Gets the insurance patient tax amount accounting entry.
   *
   * @param billChargeTaxes
   *          the bill charge taxes
   * @param charge
   *          the charge
   * @param accountingChargeDetails
   *          the accounting charge details
   * @param chargeTaxIdClaimTaxMap
   *          the charge tax id claim tax map
   * @return the insurance patient tax amount accounting entry
   */
  private List<HmsAccountingInfoModel> getInsurancePatientTaxAmountAccountingEntry(
      Set<BillChargeTaxModel> billChargeTaxes, BillChargeModel charge,
      HmsAccountingInfoModel accountingChargeDetails,
      Map<String, BigDecimal> chargeTaxIdClaimTaxMap) {
    List<HmsAccountingInfoModel> patientTaxAmountEntries = new ArrayList<>();
    for (BillChargeTaxModel bcTax : billChargeTaxes) {
      HmsAccountingInfoModel patientTaxEntry = new HmsAccountingInfoModel();
      org.springframework.beans.BeanUtils.copyProperties(accountingChargeDetails, patientTaxEntry);
      BigDecimal patTaxAmount = bcTax
          .getTaxAmount()
          .subtract(
              chargeTaxIdClaimTaxMap.get("CTI" + bcTax.getChargeTaxId()) != null
                  && !"".equals(chargeTaxIdClaimTaxMap.get("CTI" + bcTax.getChargeTaxId()))
                    ? (BigDecimal) chargeTaxIdClaimTaxMap
                        .get("CTI" + bcTax.getChargeTaxId()) : BigDecimal.ZERO);
      if (!accountingDataInsertRepo.getfaConfiguration("post_zero_tax").equals("Y") && patTaxAmount.compareTo(BigDecimal.ZERO) == 0) {
        continue;
      }
      patientTaxEntry.setGrossAmount(patTaxAmount.compareTo(BigDecimal.ZERO) >= 0 ? patTaxAmount
          : patTaxAmount.negate());
      patientTaxEntry.setNetAmount(patTaxAmount.compareTo(BigDecimal.ZERO) >= 0 ? patTaxAmount
          : patTaxAmount.negate());
      patientTaxEntry
          .setDebitAccount(patTaxAmount.compareTo(BigDecimal.ZERO) >= 0
            ?  accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_COUNTER_RECEIPTS")
              : accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_TAX_LIABILITY_ACC"));
      patientTaxEntry
          .setCreditAccount(patTaxAmount.compareTo(BigDecimal.ZERO) >= 0
            ? accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_TAX_LIABILITY_ACC")
              :  accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_COUNTER_RECEIPTS"));
      patientTaxEntry.setCustom1(patTaxAmount.toString());
      patientTaxEntry.setDiscountAmount(BigDecimal.ZERO);
      patientTaxEntry.setCustom2(null);
      patientTaxEntry.setCustom3(null);
      patientTaxEntry.setCustom4(bcTax.getTaxSubGroupId().getItemSubgroupName());
      patientTaxEntry.setPrimaryId(String.valueOf(bcTax.getChargeTaxId()));
      patientTaxEntry.setSecondaryId(String.valueOf(bcTax.getTaxSubGroupId().getItemSubgroupId()));
      patientTaxEntry.setPrimaryIdReferenceTable(BillChargeTaxModel.class
          .getAnnotation(Table.class).name());
      patientTaxEntry.setGuid(accountingDataInsertRepo.generateAccountingNextId());
      patientTaxAmountEntries.add(patientTaxEntry);
    }
    return patientTaxAmountEntries;
  }

  /**
   * Get item level discount amount accounting entry.
   * @param charge Bill Charge Model
   * @param accountingChargeDetails Accounting Charge Details Model
   */
  private HmsAccountingInfoModel getItemLevelDiscountAmountAccountingEntry(BillChargeModel charge,
      HmsAccountingInfoModel accountingChargeDetails) {
    if (charge.getDiscount() == null || charge.getDiscount().compareTo(BigDecimal.ZERO) == 0
        || charge.getChargeHead().getChargeheadId().equals("BIDIS")) {
      return null;
    }
    HmsAccountingInfoModel itemLevelDiscountEntry = new HmsAccountingInfoModel();
    org.springframework.beans.BeanUtils.copyProperties(accountingChargeDetails,
        itemLevelDiscountEntry);
    BigDecimal discountAmount = charge.getDiscount() != null ? charge.getDiscount()
        : BigDecimal.ZERO;
    itemLevelDiscountEntry.setGrossAmount(BigDecimal.ZERO);
    itemLevelDiscountEntry.setNetAmount(discountAmount);
    itemLevelDiscountEntry
        .setDebitAccount(discountAmount.compareTo(BigDecimal.ZERO) >= 0
            ? getChargeAccountHeadName(charge)
            : accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_DISCOUNTS_ACC"));
    itemLevelDiscountEntry
        .setCreditAccount(discountAmount.compareTo(BigDecimal.ZERO) >= 0
            ? accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_DISCOUNTS_ACC")
            : getChargeAccountHeadName(charge));
    itemLevelDiscountEntry.setTransactionType(AccountingConstants.TRANSACTION_TYPE_N);
    itemLevelDiscountEntry.setCustom1(discountAmount.toString());
    itemLevelDiscountEntry.setCustom2(null);
    itemLevelDiscountEntry.setCustom3(null);
    itemLevelDiscountEntry.setCustom4(getChargeAccountHeadName(charge));
    itemLevelDiscountEntry.setDiscountAmount(BigDecimal.ZERO);
    itemLevelDiscountEntry.setRoundOffAmount(BigDecimal.ZERO);
    itemLevelDiscountEntry.setGuid(accountingDataInsertRepo.generateAccountingNextId());

    return itemLevelDiscountEntry;
  }
  
  /**
   * Gets the discount amount accounting entry.
   *
   * @param charge the charge
   * @param accountingChargeDetails the accounting charge details
   * @return the discount amount accounting entry
   */
  private HmsAccountingInfoModel getDiscountAmountAccountingEntry(BillChargeModel charge,
      HmsAccountingInfoModel accountingChargeDetails) {
    if (charge.getDiscount() == null || charge.getDiscount().compareTo(BigDecimal.ZERO) == 0
        || charge.getChargeHead().getChargeheadId().equals("BIDIS")) {
      return null;
    }

    HmsAccountingInfoModel discountAccountData = new HmsAccountingInfoModel();
    org.springframework.beans.BeanUtils.copyProperties(accountingChargeDetails,
        discountAccountData);
    discountAccountData
        .setGrossAmount(charge.getDiscount() != null ? charge.getDiscount() : BigDecimal.ZERO);
    discountAccountData.setDebitAccount(
        accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_DISCOUNTS_ACC"));
    discountAccountData.setCreditAccount(
        accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_COUNTER_RECEIPTS"));
    discountAccountData.setTransactionType(AccountingConstants.TRANSACTION_TYPE_N);
    discountAccountData
        .setCustom1(charge.getDiscount() != null ? charge.getDiscount().toString() : "0");
    discountAccountData.setCustom2(null);
    discountAccountData.setCustom3(null);
    discountAccountData.setCustom4(getChargeAccountHeadName(charge));
    discountAccountData.setDiscountAmount(BigDecimal.ZERO);
    discountAccountData.setRoundOffAmount(BigDecimal.ZERO);
    discountAccountData.setGuid(accountingDataInsertRepo.generateAccountingNextId());

    return discountAccountData;
  } 

  /**
   * Sets the accounting bill details.
   *
   * @param billModel
   *          the bill model
   * @return the hms accounting info model
   */
  private HmsAccountingInfoModel setAccountingBillDetails(BillModel billModel) {
    HmsAccountingInfoModel accountingBillDetails = new HmsAccountingInfoModel();
    if (billModel.getVisitId() != null) {
      PatientRegistrationModel visit = billModel.getVisitId();
      accountingBillDetails.setMrNo(visit.getPatientDetails().getMrNo());
      accountingBillDetails.setOldMrNo(visit.getPatientDetails().getOldmrno());
      accountingBillDetails.setVisitId(visit.getPatientId());
      accountingBillDetails.setVisitType(billModel.getVisitType());
      accountingBillDetails.setAdmittingDoctor(visit.getDoctor() != null ? billModel
          .getVisitId().getDoctor().getDoctorName() : null);
      accountingBillDetails
          .setAdmittingDepartment(visit.getDeptName() != null ? billModel
              .getVisitId().getDeptName().getDeptName() : null);
      accountingBillDetails.setReferralDoctor(getReferraldoctor(billModel));
      accountingBillDetails.setCenterId(visit.getCenterId().getCenterId());
      accountingBillDetails.setCenterName(visit.getCenterId().getCenterName());
      accountingBillDetails.setPatientFullName(getPatientFullName(visit.getPatientDetails()));
      if (String.valueOf(visit.getOpType()).equals("O")) {
        accountingBillDetails
            .setVoucherSubType(String.valueOf(billModel.getBillType()).equalsIgnoreCase("p")
                ? accountingDataInsertRepo.getVoucherDefinition("VOUCHER_SUB_TYPE_OSP_BILLNOW")
                : accountingDataInsertRepo.getVoucherDefinition("VOUCHER_SUB_TYPE_OSP_BILLLATER"));
      } else {
      accountingBillDetails
          .setVoucherSubType(String.valueOf(billModel.getBillType()).equalsIgnoreCase("p")
              ? accountingDataInsertRepo.getVoucherDefinition("VOUCHER_SUB_TYPE_BILLNOW")
              : accountingDataInsertRepo.getVoucherDefinition("VOUCHER_SUB_TYPE_BILLLATER"));
      }
    } else if (billModel.getIncomingSampleRegistration() != null) {
      IncomingSampleRegistrationModel isr = billModel.getIncomingSampleRegistration();
      if (isr.getMrNo() != null) {
        accountingBillDetails.setMrNo(isr.getMrNo().getMrNo());
        accountingBillDetails.setOldMrNo(isr.getMrNo().getOldmrno());
      }
      accountingBillDetails.setVisitId(isr.getIncomingVisitId());     
      accountingBillDetails.setVisitType(billModel.getVisitType());
      accountingBillDetails.setAdmittingDoctor(null);
      accountingBillDetails.setAdmittingDepartment(null);
      accountingBillDetails.setReferralDoctor(getReferraldoctor(billModel));
      accountingBillDetails.setCenterId(isr.getCenterId().getCenterId());
      accountingBillDetails.setCenterName(isr.getCenterId().getCenterName());
      accountingBillDetails.setPatientFullName(isr.getPatientName());
      accountingBillDetails
      .setVoucherSubType(String.valueOf(billModel.getBillType()).equalsIgnoreCase("p")
          ? accountingDataInsertRepo.getVoucherDefinition("VOUCHER_SUB_TYPE_ISR_BILLNOW")
          : accountingDataInsertRepo.getVoucherDefinition("VOUCHER_SUB_TYPE_ISR_BILLLATER"));
      
    }
    
    List  creditBill = accountingDataInsertRepo.getBillCreditNotesModelByBillNo(billModel.getBillNo());
    
    accountingBillDetails.setVoucherNo(billModel.getBillNo());
    accountingBillDetails.setVoucherType(creditBill !=null  && !creditBill.isEmpty() ? accountingDataInsertRepo.getVoucherDefinition("VOUCHER_TYPE_CREDITNOTE") :
      accountingDataInsertRepo.getVoucherDefinition("VOUCHER_TYPE_HOSPBILLS"));
    accountingBillDetails.setVoucherRef(null); 
    accountingBillDetails.setBillNo( creditBill !=null  && !creditBill.isEmpty() ? (String)creditBill.get(0) : billModel.getBillNo());
    accountingBillDetails.setBillOpenDate(billModel.getOpenDate());
    accountingBillDetails.setBillFinalizedDate(billModel.getFinalizedDate());
    accountingBillDetails.setBillLastFinalizedDate(billModel.getLastFinalizedAt());
    accountingBillDetails.setAuditControlNumber(billModel.getAuditControlNumber());
    accountingBillDetails.setAccountGroup(billModel.getAccountGroup());
    accountingBillDetails.setPointsRedeemed(billModel.getPointsRedeemed());
    accountingBillDetails.setPointsRedeemedRate(BigDecimal.ZERO);
    accountingBillDetails.setPointsRedeemedAmount(billModel.getPointsRedeemedAmt());
    accountingBillDetails.setIsTpa(billModel.getIsTpa() ? AccountingConstants.IS_TPA_Y
        : AccountingConstants.IS_TPA_N);
    accountingBillDetails.setRemarks(billModel.getRemarks());

    accountingBillDetails.setCurrencyConversionRate(BigDecimal.ZERO);
    accountingBillDetails.setUnitRate(BigDecimal.ZERO);
    accountingBillDetails.setRoundOffAmount(BigDecimal.ZERO);
    accountingBillDetails.setInvoiceNo(null);
    accountingBillDetails.setInvoiceDate(null);
    accountingBillDetails.setPoNumber(null);
    accountingBillDetails.setPoDate(null);
    accountingBillDetails.setSupplierName(null);
    accountingBillDetails.setCustSupplierCode(null);
    accountingBillDetails.setGrnDate(null);
    accountingBillDetails.setPurchaseVatAmount(BigDecimal.ZERO);
    accountingBillDetails.setPurchaseVatPercent(BigDecimal.ZERO);
    accountingBillDetails.setSalesVatAmount(BigDecimal.ZERO);
    accountingBillDetails.setSalesVatPercent(BigDecimal.ZERO);
    return accountingBillDetails;
  }

  /**
   * Sets the accounting charge details.
   *
   * @param charge
   *          the charge
   * @param accountingBillDetails
   *          the accounting bill details
   * @return the hms accounting info model
   */
  private HmsAccountingInfoModel setAccountingChargeDetails(BillChargeModel charge,
      HmsAccountingInfoModel accountingBillDetails) {
    HmsAccountingInfoModel accountingChargeDetails = new HmsAccountingInfoModel();
    org.springframework.beans.BeanUtils.copyProperties(accountingBillDetails,
        accountingChargeDetails);
    accountingChargeDetails.setChargeReferenceId(charge.getChargeId());
    accountingChargeDetails.setPrimaryId(charge.getChargeId());
    accountingChargeDetails.setPrimaryIdReferenceTable(BillChargeModel.class.getAnnotation(
        Table.class).name());
    accountingChargeDetails.setItemCode(charge.getActDescriptionId());
    accountingChargeDetails.setItemName(charge.getActDescription());
    accountingChargeDetails.setChargeGroup(charge.getChargeGroup().getChargegroupId());
    accountingChargeDetails.setChargeHead(charge.getChargeHead().getChargeheadId());
    accountingChargeDetails.setServiceGroup(charge.getServiceSubGroupId() != null ? charge
        .getServiceSubGroupId().getServiceGroupId().getServiceGroupName() : null);
    accountingChargeDetails.setServiceSubGroup(charge.getServiceSubGroupId() != null ? charge
        .getServiceSubGroupId().getServiceSubGroupName() : null);
    accountingChargeDetails.setQuantity(charge.getActQuantity());
    accountingChargeDetails.setUnit(null);
    accountingChargeDetails.setDiscountAmount(charge.getDiscount());
    accountingChargeDetails.setPrescribingDoctor(charge.getPrescribingDocotor() != null ? charge
        .getPrescribingDocotor().getDoctorName() : null);
    accountingChargeDetails.setPrescribingDoctorDeptName(charge.getPrescribingDocotor() != null
        && charge.getPrescribingDocotor().getDeptId() != null ? charge.getPrescribingDocotor()
        .getDeptId().getDeptName() : null);
    accountingChargeDetails.setConductiongDoctor(charge.getPayeeDoctorId() != null ? charge
        .getPayeeDoctorId().getDoctorName() : null);
    accountingChargeDetails
        .setConductingDepartment(charge.getConductingDepartment() != null ? charge
            .getConductingDepartment().getDeptName() : null);
    accountingChargeDetails.setPayeeDoctor(charge.getPayeeDoctorId() != null ? charge
        .getPayeeDoctorId().getDoctorName() : null);
    accountingChargeDetails.setModTime(charge.getModTime());
    accountingChargeDetails.setIssueStore(null);
    accountingChargeDetails.setIssueStoreCenter(null);
    accountingChargeDetails.setCounterNo(null);
    accountingChargeDetails.setItemCategoryId(0);
    accountingChargeDetails.setReceiptStore(null);
    accountingChargeDetails.setReceiptStoreCenter(null);
    accountingChargeDetails.setCurrency(null);
    accountingChargeDetails.setOuthouseName(null);
    accountingChargeDetails.setIncoimngHospital(null);
    accountingChargeDetails.setCustItemCode(null);
    accountingChargeDetails.setTaxAmount(null);
    accountingChargeDetails.setCostAmount(BigDecimal.ZERO);
    accountingChargeDetails.setUpdateStatus(0);

    accountingChargeDetails.setVoucherDate(charge.getPostedDate());
    accountingChargeDetails.setGrossAmount(BigDecimal.ZERO);
    accountingChargeDetails.setNetAmount(BigDecimal.ZERO);
    accountingChargeDetails.setTransactionType(AccountingConstants.TRANSACTION_TYPE_N);
    accountingChargeDetails.setDebitAccount(null);
    accountingChargeDetails.setCreditAccount(null);

    accountingChargeDetails.setCustom1(null);
    accountingChargeDetails.setCustom2(null);
    accountingChargeDetails.setCustom3(null);
    accountingChargeDetails.setCustom4(null);
    accountingChargeDetails.setHaItemCode(charge.getActRatePlanItemCode());
    accountingChargeDetails.setHaCodeType(charge.getCodeType());
    return accountingChargeDetails;
  }

  /**
   * Gets the charge account head name.
   *
   * @param charge
   *          the charge
   * @return the charge account head name
   */
  public String getChargeAccountHeadName(BillChargeModel charge) {
    if (charge.getServiceSubGroupId() != null
        && charge.getServiceSubGroupId().getAccountHeadId() != null
        && charge.getServiceSubGroupId().getAccountHeadId().getAccountHeadName() != null) {
      return charge.getServiceSubGroupId().getAccountHeadId().getAccountHeadName();
    } else if (charge.getChargeHead().getAccountHeadId() != null
        && charge.getChargeHead().getAccountHeadId().getAccountHeadName() != null) {
      return charge.getChargeHead().getAccountHeadId().getAccountHeadName();
    }
    return null;
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
   * Post reversals for hospital items.
   *
   * @param billNo
   *          the bill no
   * @param jobTransaction
   *          the current job transaction
   */
  public void postReversalsForHospitalItems(String billNo, Integer jobTransaction,
      Date createdAt) {
    // posting reversals has following steps
    // 1. swap debit and credit accounts
    // 2. update created_at
    // 3. Update Transaction type to 'R'
    // 4. Update current jobTransaction
    logger.info("Accounting reversals process started for bill : " + billNo);
    BillModel billModel = accountingDataInsertRepo.getBillModelByBillNo(billNo);
    Integer lastJobTransaction = accountingDataInsertRepo
        .getLastJobTransactionIdForReversalPosts(billNo);
    String billStatus = String.valueOf(billModel.getStatus());
    Date lastFinalizedAt = billModel.getLastFinalizedAt();
    if (lastJobTransaction != null && billNo != null) {
      List<HmsAccountingInfoModel> reversalsDataList = new ArrayList<>();
      List<HmsAccountingInfoModel> lastDataForBill = accountingDataInsertRepo
          .getLastAccountingDataForBill(billNo, lastJobTransaction);
      for (HmsAccountingInfoModel accountingData : lastDataForBill) {
        HmsAccountingInfoModel reversalEntry = new HmsAccountingInfoModel();
        org.springframework.beans.BeanUtils.copyProperties(accountingData, reversalEntry);
        reversalEntry.setDebitAccount(accountingData.getCreditAccount());
        reversalEntry.setCreditAccount(accountingData.getDebitAccount());
        reversalEntry.setGuid(accountingDataInsertRepo.generateAccountingNextId());
        reversalEntry.setCreatedAt(createdAt);
        reversalEntry.setTransactionType('R');
        reversalEntry.setBillLastFinalizedDate(lastFinalizedAt);
        reversalEntry.setUpdateStatus(0);
        reversalEntry.setJobTransaction(jobTransaction);
        reversalEntry.setPatientFullName(accountingData.getPatientFullName());
        reversalEntry.setHaItemCode(accountingData.getHaItemCode());
        reversalEntry.setHaCodeType(accountingData.getHaCodeType());
        reversalsDataList.add(reversalEntry);
      }
      // insert the reversals
      accountingDataInsertRepo.batchInsert(reversalsDataList);
    }
    logger.info("Accounting reversals process completed for bill : " + billNo);

    // Post the reversals for cron job data if data exist from cron job
    accountingDataInsertRepo.postReversalsForMigratedDataOfBill(billNo);
  }

  /**
   * Generate next job transaction.
   *
   * @return the integer
   */
  @Transactional
  public Integer generateNextJobTransaction() {
    return accountingDataInsertRepo.generateNextJobTransaction();
  }

  public void logFailedExport(String billNo, String visitId) {
    failedExportsRepo.logFailedExport(billNo, visitId);
  }

  public void logFailedExport(String receiptId) {
    failedExportsRepo.logFailedExport(receiptId);
  }

  public void removeLogForFailedExport(String billNo, String visitId) {
    failedExportsRepo.remove(billNo, visitId);
  }

  public void removeLogForFailedExport(String receiptId) {
    failedExportsRepo.remove(receiptId);
  }

  public List<AccountingFailedExportsModel> getFailedExports() {
    return failedExportsRepo.getAll();
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
