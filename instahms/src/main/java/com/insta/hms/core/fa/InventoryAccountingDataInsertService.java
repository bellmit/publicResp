package com.insta.hms.core.fa;

import com.insta.hms.billing.accounting.AccountingConstants;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.preferences.genericpreferences.GenericPreferencesService;
import com.insta.hms.core.billing.BillActivityChargeModel;
import com.insta.hms.core.billing.BillChargeClaimModel;
import com.insta.hms.core.billing.BillChargeClaimTaxModel;
import com.insta.hms.core.billing.BillChargeModel;
import com.insta.hms.core.billing.BillChargeTaxModel;
import com.insta.hms.core.billing.BillModel;
import com.insta.hms.mdm.insurancecompanies.InsuranceCompanyMasterModel;
import com.insta.hms.mdm.item.StoreItemDetailsModel;
import com.insta.hms.model.HmsAccountingInfoModel;
import com.insta.hms.model.StockIssueDetailsModel;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Table;

/**
 * The Class InventoryAccountingDataInsertService.
 */
@Service
public class InventoryAccountingDataInsertService {

  /** The accounting data insert repo. */
  @Autowired
  private AccountingDataInsertRepository accountingDataInsertRepo;

  @LazyAutowired
  private GenericPreferencesService genPrefService;

  /**
   * Gets the accounting data for inventory item.
   *
   * @param charge the charge
   * @param billModel the bill model
   * @param accountingChargeDetails the accounting charge details
   * @return the accounting data for inventory item
   */
  public List<HmsAccountingInfoModel> getAccountingDataForInventoryItem(BillChargeModel charge,
      BillModel billModel, HmsAccountingInfoModel accountingChargeDetails) {
    List<HmsAccountingInfoModel> accountingDataList = new ArrayList<>();

    List<BillActivityChargeModel> billActivityChargeList = accountingDataInsertRepo
        .getInventoryItemActivityChargeModel(charge.getChargeId());
    BillActivityChargeModel billActivityChargeModel = null;
    Integer itemIssueId = null;
    if (billActivityChargeList != null && !billActivityChargeList.isEmpty()) {
      billActivityChargeModel = billActivityChargeList.get(0);
      itemIssueId = Integer.valueOf(billActivityChargeModel.getId().getActivityId());
    }

    StockIssueDetailsModel stockIssueDetailsModel = 
        (StockIssueDetailsModel) accountingDataInsertRepo
          .load(StockIssueDetailsModel.class, itemIssueId);
    if (stockIssueDetailsModel == null) {
      return new ArrayList<>();
    }
    StoreItemDetailsModel storeItemDetails = stockIssueDetailsModel.getStoreItemDetails();
    HmsAccountingInfoModel inventoryItemDetails = setInventoryItemCommonData(
        stockIssueDetailsModel, storeItemDetails, accountingChargeDetails);

    // If bill is Non-TPA bill, amount itself is patient amount and its tax is patient tax
    // We no need to go through bill charge claims and taxes.

    if (!billModel.getIsTpa()) {
      HmsAccountingInfoModel patientAmountEntry = getPatientAmountAccountingEntry(charge,
          storeItemDetails, inventoryItemDetails);
      if (patientAmountEntry != null) {
        accountingDataList.add(patientAmountEntry);
      }
      Set<BillChargeTaxModel> billChargeTaxes = charge.getBillChargeTaxes();
      List<HmsAccountingInfoModel> patientTaxEntries = getPatientTaxAmountAccountingEntries(
          billChargeTaxes, inventoryItemDetails);
      if (patientTaxEntries != null && !patientTaxEntries.isEmpty()) {
        accountingDataList.addAll(patientTaxEntries);
      }

      // post COGS A/C (INVTRANS) for inventory item
      HmsAccountingInfoModel itemCostValueEntry = getCostAccountingForInventoryItem(
          stockIssueDetailsModel, inventoryItemDetails);
      if (itemCostValueEntry != null) {
        accountingDataList.add(itemCostValueEntry);
      }

      return accountingDataList;
    }

    // If bill is TPA bill, Then we need to post pri,sec sponsor amounts and its taxes
    // bill charge claims setters
    Set<BillChargeClaimModel> billChargeClaims = charge.getBillChargeClaims();
    Map<String, BigDecimal> chargeTaxIdClaimTaxMap = new HashMap<>();
    for (BillChargeClaimModel chargeClaim : billChargeClaims) {
      if (chargeClaim.getInsuranceClaimAmt().compareTo(BigDecimal.ZERO) == 0) {
        continue;
      }
      HmsAccountingInfoModel claimEntry = getClaimAmountAccountingEntries(chargeClaim,
          inventoryItemDetails, charge, storeItemDetails);
      if (claimEntry != null) {
        accountingDataList.add(claimEntry);
      }
      Set<BillChargeClaimTaxModel> billChargeClaimTaxes = chargeClaim.getBillChargeClaimTaxes();
      List<HmsAccountingInfoModel> claimTaxEntries = getClaimTaxAmountAccountingEntries(
          billChargeClaimTaxes, inventoryItemDetails, chargeTaxIdClaimTaxMap);
      if (claimTaxEntries != null && !claimTaxEntries.isEmpty()) {
        accountingDataList.addAll(claimTaxEntries);
      }
    }

    // posting Patient amount and patient tax for TPA bills
    HmsAccountingInfoModel patientAmountEntry = getInsurancePatientAmountAccountingEntry(charge,
        storeItemDetails, inventoryItemDetails);
    if (patientAmountEntry != null) {
      accountingDataList.add(patientAmountEntry);
    }
    Set<BillChargeTaxModel> billChargeTaxes = charge.getBillChargeTaxes();
    List<HmsAccountingInfoModel> patientTaxEntries = getInsurancePatientTaxAmountAccountingEntry(
        billChargeTaxes, inventoryItemDetails, chargeTaxIdClaimTaxMap);
    if (patientTaxEntries != null && !patientTaxEntries.isEmpty()) {
      accountingDataList.addAll(patientTaxEntries);
    }

    // post COGS A/C (INVTRANS) for inventory item
    HmsAccountingInfoModel itemCostValueEntry = getCostAccountingForInventoryItem(
        stockIssueDetailsModel, inventoryItemDetails);
    if (itemCostValueEntry != null) {
      accountingDataList.add(itemCostValueEntry);
    }

    return accountingDataList;
  }

  /**
   * Gets the patient amount accounting entry.
   *
   * @param charge the charge
   * @param storeItemDetails the store item details
   * @param inventoryItemDetails the inventory item details
   * @return the patient amount accounting entry
   */
  private HmsAccountingInfoModel getPatientAmountAccountingEntry(BillChargeModel charge,
      StoreItemDetailsModel storeItemDetails, HmsAccountingInfoModel inventoryItemDetails) {
    HmsAccountingInfoModel patientAmountEntry = new HmsAccountingInfoModel();
    BeanUtils.copyProperties(inventoryItemDetails, patientAmountEntry);
    if (charge.getAmount().compareTo(BigDecimal.ZERO) != 0) {
      patientAmountEntry.setGrossAmount((charge.getAmount().add(charge.getDiscount()))
          .compareTo(BigDecimal.ZERO) >= 0 ? (charge.getAmount().add(charge.getDiscount()))
          : (charge.getAmount().add(charge.getDiscount())).negate());
      patientAmountEntry.setNetAmount(charge.getAmount().compareTo(BigDecimal.ZERO) >= 0 ? charge
          .getAmount() : charge.getAmount().negate());
      patientAmountEntry.setTransactionType(AccountingConstants.TRANSACTION_TYPE_N);
      patientAmountEntry
          .setDebitAccount(charge.getAmount().compareTo(BigDecimal.ZERO) >= 0 
            ? accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_COUNTER_RECEIPTS")
                : getInventoryChargeAccountHeadName(charge, storeItemDetails));
      patientAmountEntry
          .setCreditAccount(charge.getAmount().compareTo(BigDecimal.ZERO) >= 0 
            ? getInventoryChargeAccountHeadName(
                charge, storeItemDetails) : accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_COUNTER_RECEIPTS"));

      patientAmountEntry.setCustom1(charge.getAmount().toString());
      patientAmountEntry.setCustom2(null);
      patientAmountEntry.setCustom3(null);
      patientAmountEntry.setCustom4(getInventoryChargeAccountHeadName(charge, storeItemDetails));
      patientAmountEntry.setGuid(accountingDataInsertRepo.generateAccountingNextId());
      patientAmountEntry.setVoucherSubType(accountingDataInsertRepo.getVoucherDefinition("VOUCHER_SUB_TYPE_PATIENT_ISSUE"));
      return patientAmountEntry;
    }
    return null;
  }

  /**
   * Gets the patient tax amount accounting entries.
   *
   * @param billChargeTaxes the bill charge taxes
   * @param inventoryItemDetails the inventory item details
   * @return the patient tax amount accounting entries
   */
  private List<HmsAccountingInfoModel> getPatientTaxAmountAccountingEntries(
      Set<BillChargeTaxModel> billChargeTaxes, HmsAccountingInfoModel inventoryItemDetails) {
    List<HmsAccountingInfoModel> patientTaxAmountEntryList = new ArrayList<>();
    for (BillChargeTaxModel bcTax : billChargeTaxes) {
      if (!accountingDataInsertRepo.getfaConfiguration("post_zero_tax").equals("Y")
          && bcTax.getTaxAmount().compareTo(BigDecimal.ZERO) == 0) {
        continue;
      }
      HmsAccountingInfoModel patientTaxEntry = new HmsAccountingInfoModel();
      BeanUtils.copyProperties(inventoryItemDetails, patientTaxEntry);
      patientTaxEntry.setGrossAmount((bcTax.getTaxAmount()).compareTo(BigDecimal.ZERO) >= 0 ? bcTax
          .getTaxAmount() : bcTax.getTaxAmount().negate());
      patientTaxEntry.setNetAmount((bcTax.getTaxAmount()).compareTo(BigDecimal.ZERO) >= 0 ? bcTax
          .getTaxAmount() : bcTax.getTaxAmount().negate());
      patientTaxEntry
          .setDebitAccount(bcTax.getTaxAmount().compareTo(BigDecimal.ZERO) >= 0 
            ? accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_COUNTER_RECEIPTS")
                : accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_TAX_LIABILITY_ACC"));
      patientTaxEntry
          .setCreditAccount(bcTax.getTaxAmount().compareTo(BigDecimal.ZERO) >= 0 
            ? accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_TAX_LIABILITY_ACC")
                : accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_COUNTER_RECEIPTS"));
      patientTaxEntry.setCustom1(bcTax.getTaxAmount().toString());
      patientTaxEntry.setCustom2(null);
      patientTaxEntry.setCustom3(null);
      patientTaxEntry.setCustom4(bcTax.getTaxSubGroupId().getItemSubgroupName());
      patientTaxEntry.setTaxAmount(BigDecimal.ZERO);
      patientTaxEntry.setDiscountAmount(BigDecimal.ZERO);
      patientTaxEntry.setRoundOffAmount(BigDecimal.ZERO);
      patientTaxEntry.setPrimaryId(String.valueOf(bcTax.getChargeTaxId()));
      patientTaxEntry.setSecondaryId(String.valueOf(bcTax.getTaxSubGroupId().getItemSubgroupId()));
      patientTaxEntry.setPrimaryIdReferenceTable(BillChargeTaxModel.class
          .getAnnotation(Table.class).name());
      patientTaxEntry.setGuid(accountingDataInsertRepo.generateAccountingNextId());
      patientTaxEntry.setVoucherSubType(accountingDataInsertRepo.getVoucherDefinition("VOUCHER_SUB_TYPE_PATIENT_ISSUE"));

      patientTaxAmountEntryList.add(patientTaxEntry);
    }
    return patientTaxAmountEntryList;
  }

  /**
   * Gets the claim amount accounting entries.
   * This method will create the claim amount accounting entry for an inventory item
   *
   * @param chargeClaim the charge claim
   * @param inventoryItemDetails the inventory item details
   * @param charge the charge
   * @param storeItemDetails the store item details
   * @return the claim amount accounting entries
   */
  private HmsAccountingInfoModel getClaimAmountAccountingEntries(BillChargeClaimModel chargeClaim,
      HmsAccountingInfoModel inventoryItemDetails, BillChargeModel charge,
      StoreItemDetailsModel storeItemDetails) {
    HmsAccountingInfoModel claimEntry = new HmsAccountingInfoModel();
    BeanUtils.copyProperties(inventoryItemDetails, claimEntry);
    claimEntry
        .setGrossAmount(chargeClaim.getInsuranceClaimAmt().compareTo(BigDecimal.ZERO) >= 0 
          ? chargeClaim.getInsuranceClaimAmt() : chargeClaim.getInsuranceClaimAmt().negate());
    claimEntry
        .setNetAmount(chargeClaim.getInsuranceClaimAmt().compareTo(BigDecimal.ZERO) >= 0 
          ? chargeClaim.getInsuranceClaimAmt() : chargeClaim.getInsuranceClaimAmt().negate());
    claimEntry
        .setDebitAccount(chargeClaim.getInsuranceClaimAmt().compareTo(BigDecimal.ZERO) >= 0 
          ? chargeClaim.getSponsorId().getTpaName() : getInventoryChargeAccountHeadName(charge,
            storeItemDetails));
    claimEntry
        .setCreditAccount(chargeClaim.getInsuranceClaimAmt().compareTo(BigDecimal.ZERO) >= 0 
          ? getInventoryChargeAccountHeadName(
              charge, storeItemDetails) : chargeClaim.getSponsorId().getTpaName());
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
    claimEntry.setCustom4(getInventoryChargeAccountHeadName(charge, storeItemDetails));
    claimEntry.setPrimaryId(charge.getChargeId());
    claimEntry.setPrimaryIdReferenceTable(BillChargeClaimModel.class.getAnnotation(Table.class)
        .name());
    claimEntry.setGuid(accountingDataInsertRepo.generateAccountingNextId());
    return claimEntry;
  }

  /**
   * Gets the claim tax amount accounting entries.
   *
   * @param billChargeClaimTaxes the bill charge claim taxes
   * @param inventoryItemDetails the inventory item details
   * @param chargeTaxIdClaimTaxMap the charge tax id claim tax map
   * @return the claim tax amount accounting entries
   */
  private List<HmsAccountingInfoModel> getClaimTaxAmountAccountingEntries(
      Set<BillChargeClaimTaxModel> billChargeClaimTaxes,
      HmsAccountingInfoModel inventoryItemDetails, Map<String, BigDecimal> chargeTaxIdClaimTaxMap) {
    List<HmsAccountingInfoModel> accountingClaimTaxEntries = new ArrayList<>();
    for (BillChargeClaimTaxModel bcclTax : billChargeClaimTaxes) {
      if (!accountingDataInsertRepo.getfaConfiguration("post_zero_tax").equals("Y")
          && bcclTax.getSponsorTaxAmount().compareTo(BigDecimal.ZERO) == 0) {
        continue;
      }
      HmsAccountingInfoModel claimTaxEntry = new HmsAccountingInfoModel();
      BeanUtils.copyProperties(inventoryItemDetails, claimTaxEntry);
      claimTaxEntry
          .setGrossAmount(bcclTax.getSponsorTaxAmount().compareTo(BigDecimal.ZERO) >= 0 ? bcclTax
              .getSponsorTaxAmount() : bcclTax.getSponsorTaxAmount().negate());
      claimTaxEntry
          .setNetAmount(bcclTax.getSponsorTaxAmount().compareTo(BigDecimal.ZERO) >= 0 ? bcclTax
              .getSponsorTaxAmount() : bcclTax.getSponsorTaxAmount().negate());
      String tpaName = bcclTax.getSponsorId() != null ? bcclTax.getSponsorId().getTpaName() : null;
      claimTaxEntry
          .setDebitAccount(bcclTax.getSponsorTaxAmount().compareTo(BigDecimal.ZERO) >= 0 ? tpaName
              : accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_TAX_LIABILITY_ACC"));
      claimTaxEntry
          .setCreditAccount(bcclTax.getSponsorTaxAmount().compareTo(BigDecimal.ZERO) >= 0 
            ? accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_TAX_LIABILITY_ACC") : tpaName);
      claimTaxEntry.setCustom1(bcclTax.getSponsorTaxAmount().toString());
      claimTaxEntry.setCustom2(tpaName);
      claimTaxEntry.setCustom3(String.valueOf(bcclTax.getSponsorId() != null ? bcclTax
          .getSponsorId().getSponsorType() : null));
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
      claimTaxEntry.setCustom4(bcclTax.getTaxSubGroupId().getItemSubgroupName());
      claimTaxEntry.setTaxAmount(BigDecimal.ZERO);
      claimTaxEntry.setDiscountAmount(BigDecimal.ZERO);
      claimTaxEntry.setRoundOffAmount(BigDecimal.ZERO);
      claimTaxEntry.setPrimaryId(String.valueOf(bcclTax.getChargeClaimTaxId()));
      claimTaxEntry.setSecondaryId(String.valueOf(bcclTax.getTaxSubGroupId().getItemSubgroupId()));
      claimTaxEntry.setPrimaryIdReferenceTable(BillChargeClaimTaxModel.class.getAnnotation(
          Table.class).name());
      claimTaxEntry.setGuid(accountingDataInsertRepo.generateAccountingNextId());
      accountingClaimTaxEntries.add(claimTaxEntry);

      if (chargeTaxIdClaimTaxMap.containsKey("CTI" + bcclTax.getChargeTaxId())) {
        BigDecimal claimTax = chargeTaxIdClaimTaxMap.get("CTI" + bcclTax.getChargeTaxId());
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
   * @param charge the charge
   * @param storeItemDetails the store item details
   * @param inventoryItemDetails the inventory item details
   * @return the insurance patient amount accounting entry
   */
  private HmsAccountingInfoModel getInsurancePatientAmountAccountingEntry(BillChargeModel charge,
      StoreItemDetailsModel storeItemDetails, HmsAccountingInfoModel inventoryItemDetails) {
    HmsAccountingInfoModel patientAmountEntry = new HmsAccountingInfoModel();
    BigDecimal grossPatAmount = charge.getAmount().add(charge.getDiscount())
        .subtract(charge.getInsuranceClaimAmount());
    BigDecimal netPatAmount = charge.getAmount().subtract(charge.getInsuranceClaimAmount());
    if (netPatAmount.compareTo(BigDecimal.ZERO) != 0) {
      BeanUtils.copyProperties(inventoryItemDetails, patientAmountEntry);
      patientAmountEntry
          .setGrossAmount(grossPatAmount.compareTo(BigDecimal.ZERO) >= 0 ? grossPatAmount
              : grossPatAmount.negate());
      patientAmountEntry.setNetAmount(netPatAmount.compareTo(BigDecimal.ZERO) >= 0 ? netPatAmount
          : netPatAmount.negate());
      patientAmountEntry.setTransactionType(AccountingConstants.TRANSACTION_TYPE_N);
      patientAmountEntry
          .setDebitAccount(netPatAmount.compareTo(BigDecimal.ZERO) >= 0 
            ? accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_COUNTER_RECEIPTS")
              : getInventoryChargeAccountHeadName(charge, storeItemDetails));
      patientAmountEntry
          .setCreditAccount(netPatAmount.compareTo(BigDecimal.ZERO) >= 0 
            ? getInventoryChargeAccountHeadName(
              charge, storeItemDetails) : accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_COUNTER_RECEIPTS"));
      patientAmountEntry.setCustom1(netPatAmount.toString());
      patientAmountEntry.setCustom2(null);
      patientAmountEntry.setCustom3(null);
      patientAmountEntry.setCustom4(getInventoryChargeAccountHeadName(charge, storeItemDetails));
      patientAmountEntry.setGuid(accountingDataInsertRepo.generateAccountingNextId());
      patientAmountEntry.setVoucherSubType(accountingDataInsertRepo.getVoucherDefinition("VOUCHER_SUB_TYPE_PATIENT_ISSUE"));

      return patientAmountEntry;
    }
    return null;
  }

  /**
   * Gets the insurance patient tax amount accounting entry.
   *
   * @param billChargeTaxes the bill charge taxes
   * @param inventoryItemDetails the inventory item details
   * @param chargeTaxIdClaimTaxMap the charge tax id claim tax map
   * @return the insurance patient tax amount accounting entry
   */
  private List<HmsAccountingInfoModel> getInsurancePatientTaxAmountAccountingEntry(
      Set<BillChargeTaxModel> billChargeTaxes, HmsAccountingInfoModel inventoryItemDetails,
      Map<String, BigDecimal> chargeTaxIdClaimTaxMap) {
    List<HmsAccountingInfoModel> patientTaxAmountEntries = new ArrayList<>();
    for (BillChargeTaxModel bcTax : billChargeTaxes) {
      HmsAccountingInfoModel patientTaxEntry = new HmsAccountingInfoModel();
      BeanUtils.copyProperties(inventoryItemDetails, patientTaxEntry);
      String key = "CTI" + bcTax.getChargeTaxId();
      BigDecimal claimTax = chargeTaxIdClaimTaxMap.get(key) != null ? chargeTaxIdClaimTaxMap
          .get(key) : BigDecimal.ZERO;

      BigDecimal patTaxAmount = bcTax.getTaxAmount().subtract(claimTax);
      if (!accountingDataInsertRepo.getfaConfiguration("post_zero_tax").equals("Y") && patTaxAmount.compareTo(BigDecimal.ZERO) == 0) {
        continue;
      }
      patientTaxEntry.setGrossAmount(patTaxAmount.compareTo(BigDecimal.ZERO) >= 0 ? patTaxAmount
          : patTaxAmount.negate());
      patientTaxEntry.setNetAmount(patTaxAmount.compareTo(BigDecimal.ZERO) >= 0 ? patTaxAmount
          : patTaxAmount.negate());
      patientTaxEntry
          .setDebitAccount(patTaxAmount.compareTo(BigDecimal.ZERO) >= 0 
            ? accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_COUNTER_RECEIPTS")
              : accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_TAX_LIABILITY_ACC"));
      patientTaxEntry
          .setCreditAccount(patTaxAmount.compareTo(BigDecimal.ZERO) >= 0 
            ? accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_TAX_LIABILITY_ACC")
                : accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_COUNTER_RECEIPTS"));
      patientTaxEntry.setCustom1(patTaxAmount.toString());
      patientTaxEntry.setCustom2(null);
      patientTaxEntry.setCustom3(null);
      patientTaxEntry.setCustom4(bcTax.getTaxSubGroupId().getItemSubgroupName());
      patientTaxEntry.setTaxAmount(BigDecimal.ZERO);
      patientTaxEntry.setDiscountAmount(BigDecimal.ZERO);
      patientTaxEntry.setRoundOffAmount(BigDecimal.ZERO);
      patientTaxEntry.setPrimaryId(String.valueOf(bcTax.getChargeTaxId()));
      patientTaxEntry.setSecondaryId(String.valueOf(bcTax.getTaxSubGroupId().getItemSubgroupId()));
      patientTaxEntry.setPrimaryIdReferenceTable(BillChargeTaxModel.class
          .getAnnotation(Table.class).name());
      patientTaxEntry.setGuid(accountingDataInsertRepo.generateAccountingNextId());
      patientTaxEntry.setVoucherSubType(accountingDataInsertRepo.getVoucherDefinition("VOUCHER_SUB_TYPE_PATIENT_ISSUE"));

      patientTaxAmountEntries.add(patientTaxEntry);
    }
    return patientTaxAmountEntries;
  }

  /**
   * Gets the cost accounting for inventory item.
   *
   * @param stockIssueDetailsModel
   *          the stock issue details model
   * @param inventoryItemDetails
   *          the inventory item details
   * @return the cost accounting for inventory item
   */
  private HmsAccountingInfoModel getCostAccountingForInventoryItem(
      StockIssueDetailsModel stockIssueDetailsModel, HmsAccountingInfoModel inventoryItemDetails) {
    BigDecimal costValue = stockIssueDetailsModel.getCostValue() != null ? stockIssueDetailsModel
        .getCostValue() : BigDecimal.ZERO;
    if (costValue.compareTo(BigDecimal.ZERO) == 0) {
      return null;
    }

    HmsAccountingInfoModel costValueEntry = new HmsAccountingInfoModel();
    BeanUtils.copyProperties(inventoryItemDetails, costValueEntry);
    costValueEntry.setVoucherType(accountingDataInsertRepo.getVoucherDefinition("VOUCHER_TYPE_INVTRANS"));
    costValueEntry.setGrossAmount(costValue.abs());
    costValueEntry.setNetAmount(costValue.abs());
    costValueEntry.setTransactionType(AccountingConstants.TRANSACTION_TYPE_N);
    costValueEntry
        .setDebitAccount(costValue.compareTo(BigDecimal.ZERO) >= 0
          ? accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_COGS_ACC")
            : accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_INVENTORY_ACC"));
    costValueEntry
        .setCreditAccount(costValue.compareTo(BigDecimal.ZERO) >= 0
          ? accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_INVENTORY_ACC")
            : accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_COGS_ACC"));
    costValueEntry.setCostAmount(costValue);
    costValueEntry.setCustom1(costValue.toString());
    costValueEntry.setCustom2(null);
    costValueEntry.setCustom3(null);
    costValueEntry.setCustom4(null);
    costValueEntry.setRoundOffAmount(BigDecimal.ZERO);
    costValueEntry.setReferralDoctor(null);
    costValueEntry.setPrescribingDoctor(null);
    costValueEntry.setPrescribingDoctorDeptName(null);
    costValueEntry.setConductiongDoctor(null);
    costValueEntry.setConductingDepartment(null);
    costValueEntry.setPayeeDoctor(null);
    costValueEntry.setGuid(accountingDataInsertRepo.generateAccountingNextId());
    costValueEntry.setVoucherSubType(accountingDataInsertRepo.getVoucherDefinition("VOUCHER_SUB_TYPE_PATIENT_ISSUE"));

    return costValueEntry;
  }

  /**
   * Gets the inventory charge account head name.
   *
   * @param charge the charge
   * @param storeItemDetails the store item details
   * @return the inventory charge account head name
   */
  public String getInventoryChargeAccountHeadName(BillChargeModel charge,
      StoreItemDetailsModel storeItemDetails) {
    if (storeItemDetails != null && storeItemDetails.getServiceSubGroupId() != null
        && storeItemDetails.getServiceSubGroupId().getAccountHeadId() != null) {
      return storeItemDetails.getServiceSubGroupId().getAccountHeadId().getAccountHeadName();
    } else if (charge != null && charge.getChargeHead() != null
        && charge.getChargeHead().getAccountHeadId() != null) {
      return charge.getChargeHead().getAccountHeadId().getAccountHeadName();
    }
    return null;
  }

  /**
   * Sets the inventory item common data.
   *
   * @param stockIssueDetailsModel the stock issue details model
   * @param storeItemDetails the store item details
   * @param accountingChargeDetails the accounting charge details
   * @return the hms accounting info model
   */
  private HmsAccountingInfoModel setInventoryItemCommonData(
      StockIssueDetailsModel stockIssueDetailsModel, StoreItemDetailsModel storeItemDetails,
      HmsAccountingInfoModel accountingChargeDetails) {
    HmsAccountingInfoModel inventoryItemDetails = new HmsAccountingInfoModel();
    BeanUtils.copyProperties(accountingChargeDetails, inventoryItemDetails);
    inventoryItemDetails.setPrimaryId(String.valueOf(stockIssueDetailsModel.getItemIssueNo()));
    inventoryItemDetails.setPrimaryIdReferenceTable(StockIssueDetailsModel.class.getAnnotation(
        Table.class).name());
    inventoryItemDetails
        .setSalesVatAmount(BigDecimal.valueOf(stockIssueDetailsModel.getFinalTax() != null 
          ? stockIssueDetailsModel.getFinalTax() : 0.00));
    inventoryItemDetails.setSalesVatPercent(stockIssueDetailsModel.getVat());
    inventoryItemDetails.setServiceSubGroup(getServiceSubGroupName(storeItemDetails));
    inventoryItemDetails.setServiceGroup(getServiceGroupName(storeItemDetails));
    inventoryItemDetails.setQuantity(stockIssueDetailsModel.getQty());
    inventoryItemDetails.setIssueStore(getIssueStore(stockIssueDetailsModel));
    inventoryItemDetails.setIssueStoreCenter(getIssueStoreCenterName(stockIssueDetailsModel));
    inventoryItemDetails.setItemCategoryId(getItemCategoryId(storeItemDetails));
    inventoryItemDetails.setCustItemCode(storeItemDetails != null ? storeItemDetails
        .getCustItemCode() : null);
    inventoryItemDetails
        .setTaxAmount(BigDecimal.valueOf(stockIssueDetailsModel.getFinalTax() != null 
          ? stockIssueDetailsModel.getFinalTax() : 0.00));
    inventoryItemDetails
        .setCostAmount(stockIssueDetailsModel.getCostValue() == null ? BigDecimal.ZERO
            : stockIssueDetailsModel.getCostValue());
    return inventoryItemDetails;
  }

  /**
   * Gets the item category id.
   *
   * @param storeItemDetails the store item details
   * @return the item category id
   */
  private Integer getItemCategoryId(StoreItemDetailsModel storeItemDetails) {
    if (storeItemDetails != null && storeItemDetails.getStoreCategoryMaster() != null) {
      return storeItemDetails.getStoreCategoryMaster().getCategoryId();
    }
    return null;
  }

  /**
   * Gets the issue store center name.
   *
   * @param stockIssueDetailsModel the stock issue details model
   * @return the issue store center name
   */
  private String getIssueStoreCenterName(StockIssueDetailsModel stockIssueDetailsModel) {
    if (stockIssueDetailsModel != null && stockIssueDetailsModel.getStockIssueMain() != null
        && stockIssueDetailsModel.getStockIssueMain().getStoresModel() != null
        && stockIssueDetailsModel.getStockIssueMain().getStoresModel().getCenterId() != null) {
      return stockIssueDetailsModel.getStockIssueMain().getStoresModel().getCenterId()
          .getCenterName();
    }
    return null;
  }

  /**
   * Gets the issue store.
   *
   * @param stockIssueDetailsModel the stock issue details model
   * @return the issue store
   */
  private String getIssueStore(StockIssueDetailsModel stockIssueDetailsModel) {
    if (stockIssueDetailsModel != null && stockIssueDetailsModel.getStockIssueMain() != null
        && stockIssueDetailsModel.getStockIssueMain().getStoresModel() != null) {
      return stockIssueDetailsModel.getStockIssueMain().getStoresModel().getDeptName();
    }
    return null;

  }

  /**
   * Gets the service sub group name.
   *
   * @param storeItemDetails the store item details
   * @return the service sub group name
   */
  private String getServiceSubGroupName(StoreItemDetailsModel storeItemDetails) {
    if (storeItemDetails != null && storeItemDetails.getServiceSubGroupId() != null) {
      return storeItemDetails.getServiceSubGroupId().getServiceSubGroupName();
    }
    return null;
  }

  /**
   * Gets the service group name.
   *
   * @param storeItemDetails the store item details
   * @return the service group name
   */
  private String getServiceGroupName(StoreItemDetailsModel storeItemDetails) {
    if (storeItemDetails != null && storeItemDetails.getServiceSubGroupId() != null
        && storeItemDetails.getServiceSubGroupId().getServiceGroupId() != null) {
      return storeItemDetails.getServiceSubGroupId().getServiceGroupId().getServiceGroupName();
    }
    return null;
  }

}
