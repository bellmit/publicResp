package com.insta.hms.core.fa;

import com.insta.hms.billing.accounting.AccountingConstants;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.preferences.genericpreferences.GenericPreferencesService;
import com.insta.hms.core.billing.BillModel;
import com.insta.hms.core.inventory.sales.SalesClaimDetailsModel;
import com.insta.hms.core.inventory.sales.SalesClaimTaxDetailsModel;
import com.insta.hms.core.patient.PatientDetailsModel;
import com.insta.hms.mdm.chargeheads.ChargeheadConstantsModel;
import com.insta.hms.mdm.insurancecompanies.InsuranceCompanyMasterModel;
import com.insta.hms.model.HmsAccountingInfoModel;
import com.insta.hms.model.StoreSalesDetailsModel;
import com.insta.hms.model.StoreSalesMainModel;
import com.insta.hms.model.StoreSalesTaxDetailsModel;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Table;

/**
 * The Class PharmacyOnlyAccountingDataInsertService. This class is useful for processing accounting
 * for pharmacy bills which are having restrictionType is P
 */
@Service
public class PharmacyOnlyAccountingDataInsertService {

  /** The pharmacy accounting data insert repo. */
  @Autowired
  private PharmacyAccountingDataInsertRepository pharmacyAccountingDataInsertRepo;

  /** The accounting data insert repo. */
  @Autowired
  private AccountingDataInsertRepository accountingDataInsertRepo;

  /** The gen pref service. */
  @LazyAutowired
  private GenericPreferencesService genPrefService;

  /**
   * Gets the accounting data to insert.
   *
   * @param storeSalesMainModel the store sales main model
   * @param jobTransaction      the job log id
   * @return the accounting data to insert
   */
  public List<HmsAccountingInfoModel> getAccountingDataToInsert(
      StoreSalesMainModel storeSalesMainModel, Integer jobTransaction, Date createdAt) {
    List<HmsAccountingInfoModel> accountingDataList = new ArrayList<>();
    BillModel billModel = storeSalesMainModel.getBillNo();

    HmsAccountingInfoModel accountingPharmacyBillDetails = setAccountingPharmacyItemDefaults(
        storeSalesMainModel);
    accountingPharmacyBillDetails.setJobTransaction(jobTransaction);
    accountingPharmacyBillDetails.setCreatedAt(createdAt);
    Set<StoreSalesDetailsModel> storeSalesDetailsSet = storeSalesMainModel.getStoreSalesDetails();

    for (StoreSalesDetailsModel saleItem : storeSalesDetailsSet) {
      HmsAccountingInfoModel accountingSaleItemDetails = setAccountingSaleItemDetails(saleItem,
          accountingPharmacyBillDetails);

      // If sale bill is Non-TPA bill, amount itself is patient amount and its tax is patient tax
      // We no need to go through sales claim details and taxes.
      if (!billModel.getIsTpa()) {
        HmsAccountingInfoModel patientAmountEntry = getPatientAmountAccountingEntry(saleItem,
            accountingSaleItemDetails, storeSalesMainModel);
        if (patientAmountEntry != null) {
          accountingDataList.add(patientAmountEntry);
        }

        // sale item patient tax entries
        Set<StoreSalesTaxDetailsModel> saleItemTaxes = saleItem.getStoreSalesTaxDetails();
        List<HmsAccountingInfoModel> patientTaxEntries = getPatientTaxAmountAccountingEntries(
            saleItemTaxes, accountingSaleItemDetails, storeSalesMainModel);
        if (patientTaxEntries != null && !patientTaxEntries.isEmpty()) {
          accountingDataList.addAll(patientTaxEntries);
        }

        // post item level discounts for pharmacy items
        if (accountingDataInsertRepo.getfaConfiguration("post_discount_vouchers").equals("B")) {
          HmsAccountingInfoModel itemLevelDiscountEntry = null;
          if (accountingDataInsertRepo.getfaConfiguration("use_credit_ac_for_discount")
              .equals("Y")) {
            itemLevelDiscountEntry = getItemLevelDiscountAmountAccountingEntry(saleItem,
                accountingSaleItemDetails, storeSalesMainModel);
          } else {
            itemLevelDiscountEntry = getDiscountAmountAccountingEntry(saleItem,
                accountingSaleItemDetails, storeSalesMainModel);
          }
          if (itemLevelDiscountEntry != null) {
            accountingDataList.add(itemLevelDiscountEntry);
          }
        }

        // post COGS A/C (INVTRANS) for sales
        HmsAccountingInfoModel itemCostValueEntry = getCostAccountingForSales(saleItem,
            accountingSaleItemDetails);
        if (itemCostValueEntry != null) {
          accountingDataList.add(itemCostValueEntry);
        }

        continue;
      }

      // If bill is TPA bill, Then we need to post pri,sec sponsor amounts and its taxes
      // bill charge claims setters
      Map<Integer, BigDecimal> claimAmountSumMap = new HashMap<>();
      Set<SalesClaimDetailsModel> salesClaims = saleItem.getSalesClaimDetails();
      // Map<String, BigDecimal> chargeTaxIdClaimTaxMap = new HashMap<String, BigDecimal>();
      for (SalesClaimDetailsModel saleClaim : salesClaims) {
        if (saleClaim.getInsuranceClaimAmt().compareTo(BigDecimal.ZERO) == 0) {
          continue;
        }
        HmsAccountingInfoModel claimEntry = getClaimAmountAccountingEntries(saleClaim,
            accountingSaleItemDetails, saleItem, storeSalesMainModel);
        if (claimEntry != null) {
          accountingDataList.add(claimEntry);
        }
        Set<SalesClaimTaxDetailsModel> saleClaimTaxes = saleClaim.getSalesClaimTaxDetails();
        List<HmsAccountingInfoModel> claimTaxEntries = getClaimTaxAmountAccountingEntries(
            saleClaimTaxes, saleClaim, accountingSaleItemDetails, storeSalesMainModel);
        if (claimTaxEntries != null && !claimTaxEntries.isEmpty()) {
          accountingDataList.addAll(claimTaxEntries);
        }

        if (claimAmountSumMap.containsKey(saleClaim.getSaleItemId().getSaleItemId())) {
          BigDecimal claimSum = claimAmountSumMap.get(saleClaim.getSaleItemId().getSaleItemId());
          claimSum = claimSum != null ? claimSum : BigDecimal.ZERO;
          claimSum = claimSum
              .add(saleClaim.getInsuranceClaimAmt() != null ? saleClaim.getInsuranceClaimAmt()
                  : BigDecimal.ZERO);
          claimAmountSumMap.put(saleClaim.getSaleItemId().getSaleItemId(), claimSum);
        } else {
          claimAmountSumMap.put(saleClaim.getSaleItemId().getSaleItemId(),
              saleClaim.getInsuranceClaimAmt() != null ? saleClaim.getInsuranceClaimAmt()
                  : BigDecimal.ZERO);
        }
      }

      // posting Patient amount and patient tax for TPA bills
      HmsAccountingInfoModel patientAmountEntry = getInsurancePatientAmountAccountingEntry(saleItem,
          accountingSaleItemDetails, storeSalesMainModel, claimAmountSumMap);
      if (patientAmountEntry != null) {
        accountingDataList.add(patientAmountEntry);
      }
      Map<String, BigDecimal> subGroupLevelClaimTaxSumMap = getSubGroupLevelClaimTaxSumMap(
          salesClaims);
      Set<StoreSalesTaxDetailsModel> saleItemTaxes = saleItem.getStoreSalesTaxDetails();
      List<HmsAccountingInfoModel> patientTaxEntries = getInsurancePatientTaxAmountAccountingEntry(
          saleItemTaxes, accountingSaleItemDetails, storeSalesMainModel,
          subGroupLevelClaimTaxSumMap);
      if (patientTaxEntries != null && !patientTaxEntries.isEmpty()) {
        accountingDataList.addAll(patientTaxEntries);
      }

      // post item level discounts for pharmacy items
      if (accountingDataInsertRepo.getfaConfiguration("post_discount_vouchers").equals("B")) {
        HmsAccountingInfoModel itemLevelDiscountEntry = null;
        if (accountingDataInsertRepo.getfaConfiguration("use_credit_ac_for_discount").equals("Y")) {
          itemLevelDiscountEntry = getItemLevelDiscountAmountAccountingEntry(saleItem,
              accountingSaleItemDetails, storeSalesMainModel);
        } else {
          itemLevelDiscountEntry = getDiscountAmountAccountingEntry(saleItem,
              accountingSaleItemDetails, storeSalesMainModel);
        }
        if (itemLevelDiscountEntry != null) {
          accountingDataList.add(itemLevelDiscountEntry);
        }
      }

      // post COGS A/C (INVTRANS) for sales
      HmsAccountingInfoModel itemCostValueEntry = getCostAccountingForSales(saleItem,
          accountingSaleItemDetails);
      if (itemCostValueEntry != null) {
        accountingDataList.add(itemCostValueEntry);
      }
    }

    // post the accounting entries for pharmacy bill discounts
    HmsAccountingInfoModel discountAmountEntry = getDiscountsAmountAccountingEntry(
        accountingPharmacyBillDetails, storeSalesMainModel);
    if (discountAmountEntry != null) {
      accountingDataList.add(discountAmountEntry);
    }

    // post the accounting entries for pharmacy bill roundoffs
    HmsAccountingInfoModel roundOffAmountEntry = getRoundOffAmountAccountingEntry(
        accountingPharmacyBillDetails, storeSalesMainModel);
    if (roundOffAmountEntry != null) {
      accountingDataList.add(roundOffAmountEntry);
    }

    return accountingDataList;
  }

  /**
   * Gets the patient amount accounting entry.
   *
   * @param saleItem                  the sale item
   * @param accountingSaleItemDetails the accounting sale item details
   * @param storeSalesMainModel       the store sales main model
   * @return the patient amount accounting entry
   */
  private HmsAccountingInfoModel getPatientAmountAccountingEntry(StoreSalesDetailsModel saleItem,
      HmsAccountingInfoModel accountingSaleItemDetails, StoreSalesMainModel storeSalesMainModel) {
    HmsAccountingInfoModel patientAmountEntry = new HmsAccountingInfoModel();
    BeanUtils.copyProperties(accountingSaleItemDetails, patientAmountEntry);
    BigDecimal grossAmount = getSaleItemAmount(saleItem)
        .add(saleItem.getDisc() != null ? saleItem.getDisc() : BigDecimal.ZERO);
    //Use gross amount for non zero value compare instead of (net amount - tax)
    if (grossAmount.compareTo(BigDecimal.ZERO) != 0) {
      patientAmountEntry.setGrossAmount(
          grossAmount.compareTo(BigDecimal.ZERO) >= 0 ? grossAmount : grossAmount.negate());
      patientAmountEntry.setNetAmount(
          getSaleItemAmount(saleItem).compareTo(BigDecimal.ZERO) >= 0 ? getSaleItemAmount(saleItem)
              : getSaleItemAmount(saleItem).negate());
      patientAmountEntry.setTransactionType(AccountingConstants.TRANSACTION_TYPE_N);
      patientAmountEntry.setDebitAccount(grossAmount.compareTo(BigDecimal.ZERO) >= 0
          ? accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_COUNTER_RECEIPTS")
          : getSaleItemAccountHeadName(saleItem, storeSalesMainModel));
      patientAmountEntry.setCreditAccount(grossAmount.compareTo(BigDecimal.ZERO) >= 0
          ? getSaleItemAccountHeadName(saleItem, storeSalesMainModel)
          : accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_COUNTER_RECEIPTS"));

      patientAmountEntry.setCustom1(accountingDataInsertRepo.getfaConfiguration("use_credit_ac_for_discount").equals("Y")
          ? getSaleItemAmount(saleItem).toString() : grossAmount.toString());
      patientAmountEntry.setCustom2(null);
      patientAmountEntry.setCustom3(null);
      patientAmountEntry.setCustom4(getSaleItemAccountHeadName(saleItem, storeSalesMainModel));
      patientAmountEntry.setGuid(accountingDataInsertRepo.generateAccountingNextId());
      return patientAmountEntry;
    }
    return null;
  }

  /**
   * Gets the patient tax amount accounting entries.
   *
   * @param saleItemTaxes             the sale item taxes
   * @param accountingSaleItemDetails the accounting sale item details
   * @param storeSalesMainModel       the store sales main model
   * @return the patient tax amount accounting entries
   */
  private List<HmsAccountingInfoModel> getPatientTaxAmountAccountingEntries(
      Set<StoreSalesTaxDetailsModel> saleItemTaxes,
      HmsAccountingInfoModel accountingSaleItemDetails, StoreSalesMainModel storeSalesMainModel) {
    List<HmsAccountingInfoModel> patientTaxAmountEntryList = new ArrayList<>();
    for (StoreSalesTaxDetailsModel saleItemTax : saleItemTaxes) {
      if (!accountingDataInsertRepo.getfaConfiguration("post_zero_tax").equals("Y")
          && saleItemTax.getTaxAmt().compareTo(BigDecimal.ZERO) == 0) {
        continue;
      }
      HmsAccountingInfoModel patientTaxEntry = new HmsAccountingInfoModel();
      BeanUtils.copyProperties(accountingSaleItemDetails, patientTaxEntry);
      BigDecimal taxAmount = saleItemTax.getTaxAmt() != null ? saleItemTax.getTaxAmt()
          : BigDecimal.ZERO;
      BigDecimal grossAmount = taxAmount.compareTo(BigDecimal.ZERO) >= 0 ? taxAmount
          : taxAmount.negate();
      patientTaxEntry.setGrossAmount(grossAmount);
      patientTaxEntry.setNetAmount(grossAmount);
      String debitAccount = (taxAmount.compareTo(BigDecimal.ZERO) >= 0
          && !String.valueOf(storeSalesMainModel.getType()).equals(AccountingConstants.SALE_TYPE_R))
              ? accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_COUNTER_RECEIPTS")
              : accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_TAX_LIABILITY_ACC");
      String creditAccount = (taxAmount.compareTo(BigDecimal.ZERO) >= 0
          && !String.valueOf(storeSalesMainModel.getType()).equals(AccountingConstants.SALE_TYPE_R))
              ? accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_TAX_LIABILITY_ACC")
              : accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_COUNTER_RECEIPTS");
      patientTaxEntry.setDebitAccount(debitAccount);
      patientTaxEntry.setCreditAccount(creditAccount);
      patientTaxEntry.setCustom1(taxAmount.toString());
      patientTaxEntry.setCustom2(null);
      patientTaxEntry.setCustom3(null);
      patientTaxEntry.setCustom4(saleItemTax.getItemSubGroupsModel() != null
          ? saleItemTax.getItemSubGroupsModel().getItemSubgroupName()
          : null);
      patientTaxEntry.setGuid(accountingDataInsertRepo.generateAccountingNextId());
      patientTaxEntry.setSalesVatPercent(saleItemTax.getTaxRate());
      patientTaxEntry.setDiscountAmount(BigDecimal.ZERO);
      patientTaxEntry.setPrimaryId(String.valueOf(saleItemTax.getId().getSaleItemId()));
      patientTaxEntry.setSecondaryId(String.valueOf(saleItemTax.getId().getItemSubgroupId()));
      patientTaxEntry.setPrimaryIdReferenceTable(StoreSalesTaxDetailsModel.class.getAnnotation(
          Table.class).name());
      patientTaxAmountEntryList.add(patientTaxEntry);
    }
    return patientTaxAmountEntryList;
  }

  /**
   * Gets the claim amount accounting entries.
   *
   * @param saleClaim                 the sale claim
   * @param accountingSaleItemDetails the accounting sale item details
   * @param saleItem                  the sale item
   * @param storeSalesMainModel       the store sales main model
   * @return the claim amount accounting entries
   */
  private HmsAccountingInfoModel getClaimAmountAccountingEntries(SalesClaimDetailsModel saleClaim,
      HmsAccountingInfoModel accountingSaleItemDetails, StoreSalesDetailsModel saleItem,
      StoreSalesMainModel storeSalesMainModel) {
    HmsAccountingInfoModel claimEntry = new HmsAccountingInfoModel();
    BeanUtils.copyProperties(accountingSaleItemDetails, claimEntry);
    claimEntry.setGrossAmount(saleClaim.getInsuranceClaimAmt().compareTo(BigDecimal.ZERO) >= 0
        ? saleClaim.getInsuranceClaimAmt()
        : saleClaim.getInsuranceClaimAmt().negate());
    claimEntry.setNetAmount(saleClaim.getInsuranceClaimAmt().compareTo(BigDecimal.ZERO) >= 0
        ? saleClaim.getInsuranceClaimAmt()
        : saleClaim.getInsuranceClaimAmt().negate());
    claimEntry.setDebitAccount(saleClaim.getInsuranceClaimAmt().compareTo(BigDecimal.ZERO) >= 0
        ? saleClaim.getSponsorId().getTpaName()
        : getSaleItemAccountHeadName(saleItem, storeSalesMainModel));
    claimEntry.setCreditAccount(saleClaim.getInsuranceClaimAmt().compareTo(BigDecimal.ZERO) >= 0
        ? getSaleItemAccountHeadName(saleItem, storeSalesMainModel)
        : saleClaim.getSponsorId().getTpaName());
    if (saleClaim.getClaimId() != null) {
      InsuranceCompanyMasterModel insCoModel = accountingDataInsertRepo
          .getInsuranceCoModelByClaimId(saleClaim.getClaimId().getClaimId());
      if (insCoModel != null) {
        claimEntry.setInsuranceCo(insCoModel.getInsuranceCoName());
      } else {
        claimEntry.setInsuranceCo(null);
      }
      claimEntry.setSecondaryId(saleClaim.getClaimId().getClaimId());      
    } else {
      claimEntry.setInsuranceCo(null);
      claimEntry.setSecondaryId(null);
    }
    claimEntry.setCustom1(saleClaim.getInsuranceClaimAmt().toString());

    claimEntry.setCustom2(saleClaim.getSponsorId().getTpaName());
    claimEntry.setCustom3(String.valueOf(saleClaim.getSponsorId().getSponsorType()));
    claimEntry.setCustom4(getSaleItemAccountHeadName(saleItem, storeSalesMainModel));
    claimEntry.setPrimaryId(String.valueOf(saleClaim.getSaleItemId() != null ? saleClaim
        .getSaleItemId().getSaleItemId() : null));
    claimEntry.setPrimaryIdReferenceTable(SalesClaimDetailsModel.class.getAnnotation(Table.class)
        .name());
    claimEntry.setGuid(accountingDataInsertRepo.generateAccountingNextId());
    return claimEntry;
  }

  /**
   * Gets the claim tax amount accounting entries.
   *
   * @param saleClaimTaxes            the sale claim taxes
   * @param saleClaim                 the sale claim
   * @param accountingSaleItemDetails the accounting sale item details
   * @param storeSalesMainModel       the store sales main model
   * @return the claim tax amount accounting entries
   */
  private List<HmsAccountingInfoModel> getClaimTaxAmountAccountingEntries(
      Set<SalesClaimTaxDetailsModel> saleClaimTaxes, SalesClaimDetailsModel saleClaim,
      HmsAccountingInfoModel accountingSaleItemDetails, StoreSalesMainModel storeSalesMainModel) {
    List<HmsAccountingInfoModel> accountingClaimTaxEntries = new ArrayList<>();
    for (SalesClaimTaxDetailsModel saleClaimTax : saleClaimTaxes) {
      if (!accountingDataInsertRepo.getfaConfiguration("post_zero_tax").equals("Y")
          && saleClaimTax.getTaxAmt().compareTo(BigDecimal.ZERO) == 0) {
        continue;
      }
      HmsAccountingInfoModel claimTaxEntry = new HmsAccountingInfoModel();
      BeanUtils.copyProperties(accountingSaleItemDetails, claimTaxEntry);
      BigDecimal taxAmount = saleClaimTax.getTaxAmt() != null ? saleClaimTax.getTaxAmt()
          : BigDecimal.ZERO;
      BigDecimal grossAmount = taxAmount.compareTo(BigDecimal.ZERO) >= 0 ? taxAmount
          : taxAmount.negate();
      claimTaxEntry.setGrossAmount(grossAmount);
      claimTaxEntry.setNetAmount(grossAmount);

      String debitAccount = accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_TAX_LIABILITY_ACC");
      if (taxAmount.compareTo(BigDecimal.ZERO) >= 0 && !String
          .valueOf(storeSalesMainModel.getType()).equals(AccountingConstants.SALE_TYPE_R)) {
        debitAccount = saleClaim.getSponsorId() != null ? saleClaim.getSponsorId().getTpaName()
            : null;
      }
      String creditAccount = saleClaim.getSponsorId() != null
          ? saleClaim.getSponsorId().getTpaName()
          : null;
      if (taxAmount.compareTo(BigDecimal.ZERO) >= 0 && !String
          .valueOf(storeSalesMainModel.getType()).equals(AccountingConstants.SALE_TYPE_R)) {
        creditAccount = accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_TAX_LIABILITY_ACC");
      }

      claimTaxEntry.setDebitAccount(debitAccount);
      claimTaxEntry.setCreditAccount(creditAccount);
      claimTaxEntry.setCustom1(taxAmount.toString());
      claimTaxEntry.setCustom2(
          saleClaim.getSponsorId() != null ? saleClaim.getSponsorId().getTpaName() : null);
      claimTaxEntry.setCustom3(String.valueOf(
          saleClaim.getSponsorId() != null ? saleClaim.getSponsorId().getSponsorType() : null));
      if (saleClaim.getClaimId() != null) {
        InsuranceCompanyMasterModel insCoModel = accountingDataInsertRepo
            .getInsuranceCoModelByClaimId(saleClaim.getClaimId().getClaimId());
        if (insCoModel != null) {
          claimTaxEntry.setInsuranceCo(insCoModel.getInsuranceCoName());
        } else {
          claimTaxEntry.setInsuranceCo(null);
        }
      } else {
        claimTaxEntry.setInsuranceCo(null);
      }
      claimTaxEntry.setCustom4(saleClaimTax.getItemSubGroupsModel().getItemSubgroupName());
      claimTaxEntry.setSalesVatPercent(saleClaimTax.getTaxRate());
      claimTaxEntry.setGuid(accountingDataInsertRepo.generateAccountingNextId());
      claimTaxEntry.setDiscountAmount(BigDecimal.ZERO);
      claimTaxEntry.setPrimaryId(String.valueOf(saleClaimTax.getId().getSaleItemId()));
      claimTaxEntry.setSecondaryId(String.valueOf(saleClaimTax.getId().getItemSubgroupId()));
      claimTaxEntry.setPrimaryIdReferenceTable(SalesClaimTaxDetailsModel.class.getAnnotation(
          Table.class).name());
      accountingClaimTaxEntries.add(claimTaxEntry);
    }
    return accountingClaimTaxEntries;
  }

  /**
   * Gets the insurance patient amount accounting entry.
   *
   * @param saleItem                  the sale item
   * @param accountingSaleItemDetails the accounting sale item details
   * @param storeSalesMainModel       the store sales main model
   * @return the insurance patient amount accounting entry
   */
  private HmsAccountingInfoModel getInsurancePatientAmountAccountingEntry(
      StoreSalesDetailsModel saleItem, HmsAccountingInfoModel accountingSaleItemDetails,
      StoreSalesMainModel storeSalesMainModel, Map<Integer, BigDecimal> claimAmountSumMap) {
    HmsAccountingInfoModel patientAmountEntry = new HmsAccountingInfoModel();
    BigDecimal amount = getSaleItemAmount(saleItem);
    BigDecimal discount = saleItem.getDisc() != null ? saleItem.getDisc() : BigDecimal.ZERO;

    // get the insurance claim amount from sales_claim_details
    BigDecimal insuranceClaimAmount = BigDecimal.ZERO;
    if (claimAmountSumMap != null && claimAmountSumMap.containsKey(saleItem.getSaleItemId())) {
      insuranceClaimAmount = claimAmountSumMap.get(saleItem.getSaleItemId()) != null
          ? claimAmountSumMap.get(saleItem.getSaleItemId())
          : BigDecimal.ZERO;
    }

    BigDecimal grossPatAmount = amount.add(discount).subtract(insuranceClaimAmount);
    BigDecimal netPatAmount = amount.subtract(insuranceClaimAmount);
    if (grossPatAmount.compareTo(BigDecimal.ZERO) != 0) {
      BeanUtils.copyProperties(accountingSaleItemDetails, patientAmountEntry);
      patientAmountEntry
          .setGrossAmount(grossPatAmount.compareTo(BigDecimal.ZERO) >= 0 ? grossPatAmount
              : grossPatAmount.negate());
      patientAmountEntry.setNetAmount(
          netPatAmount.compareTo(BigDecimal.ZERO) >= 0 ? netPatAmount : netPatAmount.negate());
      patientAmountEntry.setTransactionType(AccountingConstants.TRANSACTION_TYPE_N);
      patientAmountEntry.setDebitAccount(netPatAmount.compareTo(BigDecimal.ZERO) >= 0
          ? accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_COUNTER_RECEIPTS")
          : getSaleItemAccountHeadName(saleItem, storeSalesMainModel));
      patientAmountEntry.setCreditAccount(netPatAmount.compareTo(BigDecimal.ZERO) >= 0
          ? getSaleItemAccountHeadName(saleItem, storeSalesMainModel)
          : accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_COUNTER_RECEIPTS"));
      patientAmountEntry.setCustom1(netPatAmount.toString());
      patientAmountEntry.setCustom2(null);
      patientAmountEntry.setCustom3(null);
      patientAmountEntry.setCustom4(getSaleItemAccountHeadName(saleItem, storeSalesMainModel));
      patientAmountEntry.setGuid(accountingDataInsertRepo.generateAccountingNextId());
      return patientAmountEntry;
    }
    return null;
  }

  /**
   * Gets the insurance patient tax amount accounting entry.
   *
   * @param saleItemTaxes               the sale item taxes
   * @param accountingSaleItemDetails   the accounting sale item details
   * @param storeSalesMainModel         the store sales main model
   * @param subGroupLevelClaimTaxSumMap the sub group level claim tax sum map
   * @return the insurance patient tax amount accounting entry
   */
  private List<HmsAccountingInfoModel> getInsurancePatientTaxAmountAccountingEntry(
      Set<StoreSalesTaxDetailsModel> saleItemTaxes,
      HmsAccountingInfoModel accountingSaleItemDetails, StoreSalesMainModel storeSalesMainModel,
      Map<String, BigDecimal> subGroupLevelClaimTaxSumMap) {
    List<HmsAccountingInfoModel> patientTaxAmountEntries = new ArrayList<>();
    for (StoreSalesTaxDetailsModel saleItemTax : saleItemTaxes) {
      HmsAccountingInfoModel patientTaxEntry = new HmsAccountingInfoModel();
      BeanUtils.copyProperties(accountingSaleItemDetails, patientTaxEntry);
      String saleItemId = String.valueOf(saleItemTax.getId().getSaleItemId());
      String itemSubGroupId = String.valueOf(saleItemTax.getItemSubGroupsModel() != null
          ? saleItemTax.getItemSubGroupsModel().getItemSubgroupId()
          : null);
      String key = saleItemId + "#" + itemSubGroupId;
      BigDecimal claimTaxSumPerSubgroup = BigDecimal.ZERO;
      if (subGroupLevelClaimTaxSumMap.containsKey(key)) {
        BigDecimal claimTaxSum = subGroupLevelClaimTaxSumMap.get(key);
        claimTaxSumPerSubgroup = claimTaxSum != null ? claimTaxSum : BigDecimal.ZERO;
      }
      BigDecimal patTaxAmount = (saleItemTax.getTaxAmt() != null ? saleItemTax.getTaxAmt()
          : BigDecimal.ZERO).subtract(claimTaxSumPerSubgroup);
      if (!accountingDataInsertRepo.getfaConfiguration("post_zero_tax").equals("Y") && patTaxAmount.compareTo(BigDecimal.ZERO) == 0) {
        continue;
      }
      patientTaxEntry.setGrossAmount(
          patTaxAmount.compareTo(BigDecimal.ZERO) >= 0 ? patTaxAmount : patTaxAmount.negate());
      patientTaxEntry.setNetAmount(
          patTaxAmount.compareTo(BigDecimal.ZERO) >= 0 ? patTaxAmount : patTaxAmount.negate());

      String debitAccount = (patTaxAmount.compareTo(BigDecimal.ZERO) >= 0
          && !String.valueOf(storeSalesMainModel.getType()).equals(AccountingConstants.SALE_TYPE_R))
              ? accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_COUNTER_RECEIPTS")
              : accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_TAX_LIABILITY_ACC");
      String creditAccount = (patTaxAmount.compareTo(BigDecimal.ZERO) >= 0
          && !String.valueOf(storeSalesMainModel.getType()).equals(AccountingConstants.SALE_TYPE_R))
              ? accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_TAX_LIABILITY_ACC")
              : accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_COUNTER_RECEIPTS");

      patientTaxEntry.setDebitAccount(debitAccount);
      patientTaxEntry.setCreditAccount(creditAccount);
      patientTaxEntry.setCustom1(patTaxAmount.toString());
      patientTaxEntry.setCustom2(null);
      patientTaxEntry.setCustom3(null);
      patientTaxEntry.setCustom4(saleItemTax.getItemSubGroupsModel() != null
          ? saleItemTax.getItemSubGroupsModel().getItemSubgroupName()
          : null);
      patientTaxEntry.setGuid(accountingDataInsertRepo.generateAccountingNextId());
      patientTaxEntry.setSalesVatPercent(saleItemTax.getTaxRate());
      patientTaxEntry.setDiscountAmount(BigDecimal.ZERO);
      patientTaxEntry.setPrimaryId(String.valueOf(saleItemTax.getId().getSaleItemId()));
      patientTaxEntry.setSecondaryId(String.valueOf(saleItemTax.getId().getItemSubgroupId()));
      patientTaxEntry.setPrimaryIdReferenceTable(StoreSalesTaxDetailsModel.class.getAnnotation(
          Table.class).name());
      patientTaxAmountEntries.add(patientTaxEntry);
    }
    return patientTaxAmountEntries;
  }

  /**
   * Gets the item level discount amount accounting entry.
   *
   * @param saleItem the sale item
   * @param accountingSaleItemDetails the accounting sale item details
   * @param storeSalesMainModel the store sales main model
   * @return the item level discount amount accounting entry
   */
  private HmsAccountingInfoModel getItemLevelDiscountAmountAccountingEntry(
      StoreSalesDetailsModel saleItem, HmsAccountingInfoModel accountingSaleItemDetails,
      StoreSalesMainModel storeSalesMainModel) {
    BigDecimal discountAmount = saleItem.getDisc();
    if (discountAmount == null || discountAmount.compareTo(BigDecimal.ZERO) == 0) {
      return null;
    }

    HmsAccountingInfoModel itemLevelDiscountEntry = new HmsAccountingInfoModel();
    BeanUtils.copyProperties(accountingSaleItemDetails, itemLevelDiscountEntry);

    itemLevelDiscountEntry.setGrossAmount(BigDecimal.ZERO);
    itemLevelDiscountEntry
        .setNetAmount(discountAmount.compareTo(BigDecimal.ZERO) >= 0 ? discountAmount
            : discountAmount.negate());
    itemLevelDiscountEntry.setTransactionType(AccountingConstants.TRANSACTION_TYPE_N);
    itemLevelDiscountEntry
        .setDebitAccount(discountAmount.compareTo(BigDecimal.ZERO) >= 0 
            ? getSaleItemAccountHeadName(saleItem, storeSalesMainModel)
            : accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_DISCOUNTS_ACC"));
    itemLevelDiscountEntry
        .setCreditAccount(discountAmount.compareTo(BigDecimal.ZERO) >= 0 
            ? accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_DISCOUNTS_ACC")
            : getSaleItemAccountHeadName(saleItem, storeSalesMainModel));
    itemLevelDiscountEntry.setCustom1(discountAmount.toString());
    itemLevelDiscountEntry.setCustom2(null);
    itemLevelDiscountEntry.setCustom3(null);
    itemLevelDiscountEntry.setCustom4(getSaleItemAccountHeadName(saleItem, storeSalesMainModel));
    itemLevelDiscountEntry.setGuid(accountingDataInsertRepo.generateAccountingNextId());
    itemLevelDiscountEntry.setDiscountAmount(BigDecimal.ZERO);

    return itemLevelDiscountEntry;
  }
  
  /**
   * Gets the discount amount accounting entry.
   *
   * @param saleItem the sale item
   * @param accountingSaleItemDetails the accounting sale item details
   * @param storeSalesMainModel the store sales main model
   * @return the discount amount accounting entry
   */
  private HmsAccountingInfoModel getDiscountAmountAccountingEntry( StoreSalesDetailsModel saleItem, HmsAccountingInfoModel accountingSaleItemDetails,
      StoreSalesMainModel storeSalesMainModel) {
    BigDecimal discountAmount = saleItem.getDisc();
    if (discountAmount == null || discountAmount.compareTo(BigDecimal.ZERO) == 0) {
      return null;
    }

    HmsAccountingInfoModel discountAccountData = new HmsAccountingInfoModel();
    org.springframework.beans.BeanUtils.copyProperties(accountingSaleItemDetails,
        discountAccountData);
    discountAccountData.setGrossAmount(discountAmount != null ? discountAmount.abs() : BigDecimal.ZERO);
    discountAccountData
    .setDebitAccount(discountAmount.compareTo(BigDecimal.ZERO) >= 0 
        && !String.valueOf(storeSalesMainModel.getType()).equals(AccountingConstants.SALE_TYPE_R)
        ? accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_DISCOUNTS_ACC")
        : accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_COUNTER_RECEIPTS"));
discountAccountData
    .setCreditAccount(discountAmount.compareTo(BigDecimal.ZERO) >= 0 
        && !String.valueOf(storeSalesMainModel.getType()).equals(AccountingConstants.SALE_TYPE_R)
        ? accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_COUNTER_RECEIPTS")
        : accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_DISCOUNTS_ACC"));
    discountAccountData.setTransactionType(AccountingConstants.TRANSACTION_TYPE_N);
    discountAccountData.setCustom1(discountAmount != null ? discountAmount.toString() : "0");
    discountAccountData.setCustom2(null);
    discountAccountData.setCustom3(null);
    discountAccountData.setCustom4(getSaleItemAccountHeadName(saleItem, storeSalesMainModel));
    discountAccountData.setDiscountAmount(BigDecimal.ZERO);
    discountAccountData.setRoundOffAmount(BigDecimal.ZERO);
    discountAccountData.setGuid(accountingDataInsertRepo.generateAccountingNextId());

    return discountAccountData;
  }

  /**
   * Gets the cost accounting for sales.
   *
   * @param saleItem
   *          the sale item
   * @param accountingSaleItemDetails
   *          the accounting sale item details
   * @return the cost accounting for sales
   */
  private HmsAccountingInfoModel getCostAccountingForSales(StoreSalesDetailsModel saleItem,
      HmsAccountingInfoModel accountingSaleItemDetails) {
    BigDecimal costValue = saleItem.getCostValue() != null ? saleItem.getCostValue()
        : BigDecimal.ZERO;
    if (costValue.compareTo(BigDecimal.ZERO) == 0) {
      return null;
    }

    HmsAccountingInfoModel costValueEntry = new HmsAccountingInfoModel();
    BeanUtils.copyProperties(accountingSaleItemDetails, costValueEntry);
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
    costValueEntry.setDiscountAmount(BigDecimal.ZERO);
    costValueEntry.setRoundOffAmount(BigDecimal.ZERO);
    costValueEntry.setReferralDoctor(null);
    costValueEntry.setPrescribingDoctor(null);
    costValueEntry.setPrescribingDoctorDeptName(null);
    costValueEntry.setConductiongDoctor(null);
    costValueEntry.setConductingDepartment(null);
    costValueEntry.setPayeeDoctor(null);
    costValueEntry.setGuid(accountingDataInsertRepo.generateAccountingNextId());

    return costValueEntry;
  }

  /**
   * Gets the discounts amount accounting entry.
   *
   * @param accountingPharmacyBillDetails the accounting pharmacy bill details
   * @param storeSalesMainModel           the store sales main model
   * @return the discounts amount accounting entry
   */
  private HmsAccountingInfoModel getDiscountsAmountAccountingEntry(
      HmsAccountingInfoModel accountingPharmacyBillDetails,
      StoreSalesMainModel storeSalesMainModel) {

    if (storeSalesMainModel.getDiscount().compareTo(BigDecimal.ZERO) == 0) {
      return null;
    }

    HmsAccountingInfoModel discountAmountEntry = new HmsAccountingInfoModel();
    BeanUtils.copyProperties(accountingPharmacyBillDetails, discountAmountEntry);

    discountAmountEntry.setItemCode(null);
    discountAmountEntry.setItemName(null);
    /** discountAmountEntry.setChargeGroup(charge.getChargeGroup().getChargegroupId()); */
    /** discountAmountEntry.setChargeHead(getChargeHead); */
    discountAmountEntry.setServiceGroup(null);
    discountAmountEntry.setServiceSubGroup(null);
    discountAmountEntry.setQuantity(BigDecimal.ZERO);
    discountAmountEntry.setUnit(null);
    discountAmountEntry.setDiscountAmount(BigDecimal.ZERO);
    discountAmountEntry.setPrescribingDoctor(null);
    discountAmountEntry.setPrescribingDoctorDeptName(null);
    discountAmountEntry.setConductiongDoctor(null);
    discountAmountEntry.setConductingDepartment(null);
    discountAmountEntry.setPayeeDoctor(null);
    /** discountAmountEntry.setModTime(charge.getModTime()); */
    /** discountAmountEntry.setIssueStore(); */
    /** discountAmountEntry.setIssueStoreCenter(null); */
    discountAmountEntry.setCounterNo(null);
    discountAmountEntry.setItemCategoryId(0);
    discountAmountEntry.setReceiptStore(null);
    discountAmountEntry.setReceiptStoreCenter(null);
    discountAmountEntry.setCurrency(null);
    discountAmountEntry.setOuthouseName(null);
    discountAmountEntry.setIncoimngHospital(null);
    discountAmountEntry.setCustItemCode(null);
    discountAmountEntry.setTaxAmount(BigDecimal.ZERO);
    discountAmountEntry.setCostAmount(BigDecimal.ZERO);
    discountAmountEntry.setUpdateStatus(0);

    /** discountAmountEntry.setVoucherDate(charge.getPostedDate()); */
    discountAmountEntry
        .setGrossAmount(storeSalesMainModel.getDiscount().compareTo(BigDecimal.ZERO) >= 0
            ? storeSalesMainModel.getDiscount()
            : storeSalesMainModel.getDiscount().negate());
    discountAmountEntry
        .setNetAmount(storeSalesMainModel.getDiscount().compareTo(BigDecimal.ZERO) >= 0
            ? storeSalesMainModel.getDiscount()
            : storeSalesMainModel.getDiscount().negate());
    discountAmountEntry.setTransactionType(AccountingConstants.TRANSACTION_TYPE_N);
    discountAmountEntry
        .setDebitAccount(storeSalesMainModel.getDiscount().compareTo(BigDecimal.ZERO) >= 0
            ? accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_DISCOUNTS_ACC")
            : accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_COUNTER_RECEIPTS"));
    discountAmountEntry
        .setCreditAccount(storeSalesMainModel.getDiscount().compareTo(BigDecimal.ZERO) >= 0
            ? accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_COUNTER_RECEIPTS")
            : accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_DISCOUNTS_ACC"));
    System.out.println(storeSalesMainModel.getDiscount());
    discountAmountEntry.setCustom1(storeSalesMainModel.getDiscount().toString());
    discountAmountEntry.setCustom2(null);
    discountAmountEntry.setCustom3(null);
    discountAmountEntry.setCustom4(null);
    discountAmountEntry.setChargeReferenceId(storeSalesMainModel.getSaleId());
    discountAmountEntry.setPrimaryId(storeSalesMainModel.getSaleId());
    discountAmountEntry.setPrimaryIdReferenceTable(StoreSalesMainModel.class.getAnnotation(
        Table.class).name());
    discountAmountEntry.setGuid(accountingDataInsertRepo.generateAccountingNextId());

    return discountAmountEntry;
  }

  /**
   * Gets the round off amount accounting entry.
   *
   * @param accountingPharmacyBillDetails the accounting pharmacy bill details
   * @param storeSalesMainModel           the store sales main model
   * @return the round off amount accounting entry
   */
  private HmsAccountingInfoModel getRoundOffAmountAccountingEntry(
      HmsAccountingInfoModel accountingPharmacyBillDetails,
      StoreSalesMainModel storeSalesMainModel) {
    BigDecimal roundOffAmount = storeSalesMainModel.getRoundOff() != null
        ? storeSalesMainModel.getRoundOff()
        : BigDecimal.ZERO;
    if (roundOffAmount.compareTo(BigDecimal.ZERO) == 0) {
      return null;
    }

    HmsAccountingInfoModel roundOffAmountEntry = new HmsAccountingInfoModel();
    BeanUtils.copyProperties(accountingPharmacyBillDetails, roundOffAmountEntry);

    roundOffAmountEntry.setItemCode(null);
    roundOffAmountEntry.setItemName(null);
    /** discountAmountEntry.setChargeGroup(charge.getChargeGroup().getChargegroupId()); */
    /** discountAmountEntry.setChargeHead(getChargeHead); */
    roundOffAmountEntry.setServiceGroup(null);
    roundOffAmountEntry.setServiceSubGroup(null);
    roundOffAmountEntry.setQuantity(BigDecimal.ZERO);
    roundOffAmountEntry.setUnit(null);
    roundOffAmountEntry.setDiscountAmount(BigDecimal.ZERO);
    roundOffAmountEntry.setPrescribingDoctor(null);
    roundOffAmountEntry.setPrescribingDoctorDeptName(null);
    roundOffAmountEntry.setConductiongDoctor(null);
    roundOffAmountEntry.setConductingDepartment(null);
    roundOffAmountEntry.setPayeeDoctor(null);
    /** discountAmountEntry.setModTime(charge.getModTime()); */
    /** discountAmountEntry.setIssueStore(); */
    /** discountAmountEntry.setIssueStoreCenter(null); */
    roundOffAmountEntry.setCounterNo(null);
    roundOffAmountEntry.setItemCategoryId(0);
    roundOffAmountEntry.setReceiptStore(null);
    roundOffAmountEntry.setReceiptStoreCenter(null);
    roundOffAmountEntry.setCurrency(null);
    roundOffAmountEntry.setOuthouseName(null);
    roundOffAmountEntry.setIncoimngHospital(null);
    roundOffAmountEntry.setCustItemCode(null);
    roundOffAmountEntry.setTaxAmount(BigDecimal.ZERO);
    roundOffAmountEntry.setCostAmount(BigDecimal.ZERO);
    roundOffAmountEntry.setUpdateStatus(0);

    /** discountAmountEntry.setVoucherDate(charge.getPostedDate()); */

    roundOffAmountEntry.setGrossAmount(
        roundOffAmount.compareTo(BigDecimal.ZERO) >= 0 ? roundOffAmount : roundOffAmount.negate());
    roundOffAmountEntry.setNetAmount(
        roundOffAmount.compareTo(BigDecimal.ZERO) >= 0 ? roundOffAmount : roundOffAmount.negate());
    roundOffAmountEntry.setTransactionType(AccountingConstants.TRANSACTION_TYPE_N);
    roundOffAmountEntry.setDebitAccount(roundOffAmount.compareTo(BigDecimal.ZERO) >= 0
        ? accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_COUNTER_RECEIPTS")
        : accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_ROUNDOFF_ACC"));
    roundOffAmountEntry.setCreditAccount(roundOffAmount.compareTo(BigDecimal.ZERO) >= 0
        ? accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_ROUNDOFF_ACC")
        : accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_COUNTER_RECEIPTS"));

    roundOffAmountEntry.setCustom1(roundOffAmount.toString());
    roundOffAmountEntry.setCustom2(null);
    roundOffAmountEntry.setCustom3(null);
    roundOffAmountEntry.setCustom4(null);
    roundOffAmountEntry.setChargeReferenceId(storeSalesMainModel.getSaleId());
    roundOffAmountEntry.setPrimaryId(storeSalesMainModel.getSaleId());
    roundOffAmountEntry.setPrimaryIdReferenceTable(StoreSalesMainModel.class.getAnnotation(
        Table.class).name());
    roundOffAmountEntry.setGuid(accountingDataInsertRepo.generateAccountingNextId());

    return roundOffAmountEntry;
  }

  /**
   * Sets the accounting pharmacy item defaults.
   *
   * @param storeSalesMainModel the store sales main model
   * @return the hms accounting info model
   */
  private HmsAccountingInfoModel setAccountingPharmacyItemDefaults(
      StoreSalesMainModel storeSalesMainModel) {
    HmsAccountingInfoModel accountingBillDetails = new HmsAccountingInfoModel();
    accountingBillDetails.setMrNo(storeSalesMainModel.getBillNo().getVisitId() != null
        ? storeSalesMainModel.getBillNo().getVisitId().getPatientDetails().getMrNo()
        : null);
    accountingBillDetails.setOldMrNo(storeSalesMainModel.getBillNo().getVisitId() != null
        ? storeSalesMainModel.getBillNo().getVisitId().getPatientDetails().getOldmrno()
        : null);
    accountingBillDetails.setVisitId(storeSalesMainModel.getBillNo().getVisitId() != null
        ? storeSalesMainModel.getBillNo().getVisitId().getPatientId()
        : null);
    accountingBillDetails.setVisitType(storeSalesMainModel.getBillNo().getVisitId() != null
        ? storeSalesMainModel.getBillNo().getVisitId().getVisitType()
        : null);
    if (storeSalesMainModel.getBillNo().getVisitId() != null) {
      accountingBillDetails.setAdmittingDoctor(storeSalesMainModel.getBillNo().getVisitId().getDoctor() != null ?
          storeSalesMainModel.getBillNo().getVisitId().getDoctor().getDoctorName(): null);
      accountingBillDetails.setAdmittingDepartment(storeSalesMainModel.getBillNo().getVisitId().getDeptName() != null ?
          storeSalesMainModel.getBillNo().getVisitId().getDeptName().getDeptName():null);
    } else {
      accountingBillDetails.setAdmittingDoctor(null);
      accountingBillDetails.setAdmittingDepartment(null);
    }
    accountingBillDetails.setReferralDoctor(null);
    accountingBillDetails
        .setCenterId(storeSalesMainModel.getStoreIdModel().getCenterId().getCenterId());
    accountingBillDetails
        .setCenterName(storeSalesMainModel.getStoreIdModel().getCenterId().getCenterName());
    accountingBillDetails.setVoucherNo(storeSalesMainModel.getBillNo().getBillNo());
    // patientEntry.setVoucherDate(null); //bc posted date
    accountingBillDetails.setVoucherType(accountingDataInsertRepo.getVoucherDefinition("VOUCHER_TYPE_PHBILLS"));
    accountingBillDetails.setVoucherRef(null);
    accountingBillDetails.setBillNo(storeSalesMainModel.getBillNo().getBillNo());
    accountingBillDetails.setSaleBillNo(storeSalesMainModel.getSaleId());
    accountingBillDetails.setBillOpenDate(storeSalesMainModel.getBillNo().getOpenDate());
    accountingBillDetails.setBillFinalizedDate(storeSalesMainModel.getBillNo().getFinalizedDate());
    accountingBillDetails.setBillLastFinalizedDate(
        storeSalesMainModel.getBillNo().getLastFinalizedAt());
    accountingBillDetails
        .setAuditControlNumber(storeSalesMainModel.getBillNo().getAuditControlNumber());
    accountingBillDetails.setAccountGroup(storeSalesMainModel.getBillNo().getAccountGroup());
    accountingBillDetails.setPointsRedeemed(storeSalesMainModel.getBillNo().getPointsRedeemed());
    accountingBillDetails.setPointsRedeemedRate(BigDecimal.ZERO);
    accountingBillDetails
        .setPointsRedeemedAmount(storeSalesMainModel.getBillNo().getPointsRedeemedAmt() != null
            ? storeSalesMainModel.getBillNo().getPointsRedeemedAmt()
            : BigDecimal.ZERO);
    accountingBillDetails
        .setIsTpa(storeSalesMainModel.getBillNo().getIsTpa() ? AccountingConstants.IS_TPA_Y
            : AccountingConstants.IS_TPA_N);
    accountingBillDetails.setRemarks(null);

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

    ChargeheadConstantsModel chargeHeadModel = getChargeHeadConstantsModel(storeSalesMainModel);
    accountingBillDetails
        .setChargeGroup((chargeHeadModel != null && chargeHeadModel.getChargeGroupModel() != null)
            ? chargeHeadModel.getChargeGroupModel().getChargegroupName()
            : null);
    accountingBillDetails
        .setChargeHead(chargeHeadModel != null ? chargeHeadModel.getChargeheadId() : null);
    accountingBillDetails.setModTime(storeSalesMainModel.getDateTime());
    accountingBillDetails.setIssueStore(storeSalesMainModel.getStoreIdModel() != null
        ? storeSalesMainModel.getStoreIdModel().getDeptName()
        : null);
    accountingBillDetails.setIssueStoreCenter(storeSalesMainModel.getStoreIdModel() != null
        ? storeSalesMainModel.getStoreIdModel().getCenterId().getCenterName()
        : null);
    accountingBillDetails.setVoucherDate(storeSalesMainModel.getSaleDate());
    accountingBillDetails.setPatientFullName(storeSalesMainModel.getBillNo().getVisitId() !=null ?
        getPatientFullName(storeSalesMainModel.getBillNo().getVisitId().getPatientDetails()) :
          storeSalesMainModel.getBillNo().getStoreRetailCustomers().getCustomerName() );
    if (storeSalesMainModel.getBillNo().getVisitId() != null) {
      accountingBillDetails.setVoucherSubType(accountingDataInsertRepo.getVoucherDefinition("VOUCHER_SUB_TYPE_PHR_HOSBILL"));
    } else {
      accountingBillDetails.setVoucherSubType(
          storeSalesMainModel.getBillNo().getStoreRetailCustomers().getIsCredit().equals('N')
              ? accountingDataInsertRepo.getVoucherDefinition("VOUCHER_SUB_TYPE_PHR_CREDITBILL")
              : accountingDataInsertRepo.getVoucherDefinition("VOUCHER_SUB_TYPE_PHR_RETAIL"));
    }
    return accountingBillDetails;
  }

  /**
   * Sets the accounting sale item details.
   *
   * @param saleItem                      the sale item
   * @param accountingPharmacyBillDetails the accounting pharmacy bill details
   * @return the hms accounting info model
   */
  private HmsAccountingInfoModel setAccountingSaleItemDetails(StoreSalesDetailsModel saleItem,
      HmsAccountingInfoModel accountingPharmacyBillDetails) {
    HmsAccountingInfoModel accountingSaleItemDetails = new HmsAccountingInfoModel();
    BeanUtils.copyProperties(accountingPharmacyBillDetails, accountingSaleItemDetails);
    accountingSaleItemDetails.setChargeReferenceId(String.valueOf(saleItem.getSaleItemId()));
    accountingSaleItemDetails.setPrimaryId(String.valueOf(saleItem.getSaleItemId()));
    accountingSaleItemDetails.setPrimaryIdReferenceTable(StoreSalesDetailsModel.class
        .getAnnotation(Table.class).name());
    accountingSaleItemDetails.setItemCode(String.valueOf(saleItem.getMedicineId()));
    accountingSaleItemDetails.setItemName(
        saleItem.getStoreItemDetails() != null ? saleItem.getStoreItemDetails().getMedicineName()
            : null);
    accountingSaleItemDetails.setHaItemCode(saleItem.getItemCode());
    accountingSaleItemDetails.setHaCodeType(saleItem.getCodeType());
    /** accountingSaleItemDetails.setChargeGroup(charge.getChargeGroup().getChargegroupId()); */
    /** accountingSaleItemDetails.setChargeHead(getChargeHead); */
    accountingSaleItemDetails.setServiceGroup(getServiceGroupName(saleItem));
    accountingSaleItemDetails.setServiceSubGroup((saleItem.getStoreItemDetails() != null
        && saleItem.getStoreItemDetails().getServiceSubGroupId() != null)
            ? saleItem.getStoreItemDetails().getServiceSubGroupId().getServiceSubGroupName()
            : null);
    accountingSaleItemDetails.setQuantity(saleItem.getQuantity());
    accountingSaleItemDetails.setUnit(null);
    accountingSaleItemDetails.setDiscountAmount(saleItem.getDisc());
    accountingSaleItemDetails.setPrescribingDoctor(null);
    accountingSaleItemDetails.setPrescribingDoctorDeptName(null);
    accountingSaleItemDetails.setConductiongDoctor(null);
    accountingSaleItemDetails.setConductingDepartment(null);
    accountingSaleItemDetails.setPayeeDoctor(null);
    /** accountingSaleItemDetails.setModTime(charge.getModTime()); */
    /** accountingSaleItemDetails.setIssueStore(); */
    /** accountingSaleItemDetails.setIssueStoreCenter(null); */
    accountingSaleItemDetails.setCounterNo(null);
    accountingSaleItemDetails.setItemCategoryId((saleItem.getStoreItemDetails() != null
        && saleItem.getStoreItemDetails().getStoreCategoryMaster() != null)
            ? saleItem.getStoreItemDetails().getStoreCategoryMaster().getCategoryId()
            : null);
    accountingSaleItemDetails.setReceiptStore(null);
    accountingSaleItemDetails.setReceiptStoreCenter(null);
    accountingSaleItemDetails.setCurrency(null);
    accountingSaleItemDetails.setOuthouseName(null);
    accountingSaleItemDetails.setIncoimngHospital(null);
    accountingSaleItemDetails.setCustItemCode(
        saleItem.getStoreItemDetails() != null ? saleItem.getStoreItemDetails().getCustItemCode()
            : null);
    accountingSaleItemDetails.setTaxAmount(BigDecimal.ZERO);
    accountingSaleItemDetails.setCostAmount(saleItem.getCostValue());
    accountingSaleItemDetails.setUpdateStatus(0);

    /** accountingSaleItemDetails.setVoucherDate(charge.getPostedDate()); */
    accountingSaleItemDetails.setGrossAmount(BigDecimal.ZERO);
    accountingSaleItemDetails.setNetAmount(BigDecimal.ZERO);
    accountingSaleItemDetails.setTransactionType(AccountingConstants.TRANSACTION_TYPE_N);
    accountingSaleItemDetails.setDebitAccount(null);
    accountingSaleItemDetails.setCreditAccount(null);

    accountingSaleItemDetails.setCustom1(null);
    accountingSaleItemDetails.setCustom2(null);
    accountingSaleItemDetails.setCustom3(null);
    accountingSaleItemDetails.setCustom4(null);

    return accountingSaleItemDetails;
  }

  /**
   * Gets the sub group level claim tax sum map.
   *
   * @param salesClaims the sales claims
   * @return the sub group level claim tax sum map
   */
  private Map<String, BigDecimal> getSubGroupLevelClaimTaxSumMap(
      Set<SalesClaimDetailsModel> salesClaims) {
    Map<String, BigDecimal> subGroupLevelClaimTaxSumMap = new HashMap<>();
    for (SalesClaimDetailsModel saleClaim : salesClaims) {
      Set<SalesClaimTaxDetailsModel> saleClaimTaxes = saleClaim.getSalesClaimTaxDetails();
      for (SalesClaimTaxDetailsModel saleClaimTax : saleClaimTaxes) {
        String saleItemId = String.valueOf(saleClaimTax.getId().getSaleItemId());
        String itemSubGroupId = String.valueOf(saleClaimTax.getItemSubGroupsModel() != null
            ? saleClaimTax.getItemSubGroupsModel().getItemSubgroupId()
            : null);
        if (saleItemId != null && itemSubGroupId != null) {
          String key = saleItemId + "#" + itemSubGroupId;
          if (subGroupLevelClaimTaxSumMap.containsKey(key)) {
            BigDecimal claimTaxSum = subGroupLevelClaimTaxSumMap.get(key);
            claimTaxSum = claimTaxSum.add(saleClaimTax.getTaxAmt());
            subGroupLevelClaimTaxSumMap.put(key, claimTaxSum);
          } else {
            subGroupLevelClaimTaxSumMap.put(key, saleClaimTax.getTaxAmt());
          }
        }
      }
    }
    return subGroupLevelClaimTaxSumMap;
  }

  /**
   * Gets the sale item amount.
   *
   * @param saleItem the sale item
   * @return the sale item amount
   */
  private BigDecimal getSaleItemAmount(StoreSalesDetailsModel saleItem) {
    BigDecimal amount = saleItem.getAmount() != null ? saleItem.getAmount() : BigDecimal.ZERO;
    BigDecimal taxAmount = saleItem.getTax() != null ? saleItem.getTax() : BigDecimal.ZERO;
    return amount.subtract(taxAmount);
  }

  /**
   * Gets the sale item account head name.
   *
   * @param saleItem            the sale item
   * @param storeSalesMainModel the store sales main model
   * @return the sale item account head name
   */
  private String getSaleItemAccountHeadName(StoreSalesDetailsModel saleItem,
      StoreSalesMainModel storeSalesMainModel) {
    if (saleItem.getStoreItemDetails() != null
        && saleItem.getStoreItemDetails().getServiceSubGroupId() != null
        && saleItem.getStoreItemDetails().getServiceSubGroupId().getAccountHeadId() != null
        && saleItem.getStoreItemDetails().getServiceSubGroupId().getAccountHeadId()
            .getAccountHeadName() != null) {
      return saleItem.getStoreItemDetails().getServiceSubGroupId().getAccountHeadId()
          .getAccountHeadName();
    } else if (getChargeHeadConstantsModel(storeSalesMainModel) != null
        && getChargeHeadConstantsModel(storeSalesMainModel).getAccountHeadId() != null) {
      return getChargeHeadConstantsModel(storeSalesMainModel).getAccountHeadId()
          .getAccountHeadName();
    }
    return null;
  }

  /**
   * Gets the service group name.
   *
   * @param saleItem the sale item
   * @return the service group name
   */
  private String getServiceGroupName(StoreSalesDetailsModel saleItem) {
    if (saleItem.getStoreItemDetails() != null
        && saleItem.getStoreItemDetails().getServiceSubGroupId() != null
        && saleItem.getStoreItemDetails().getServiceSubGroupId().getServiceGroupId() != null) {
      return saleItem.getStoreItemDetails().getServiceSubGroupId().getServiceGroupId()
          .getServiceGroupName();
    }
    return null;
  }

  /**
   * Gets the charge head constants model.
   *
   * @param storeSalesMainModel the store sales main model
   * @return the charge head constants model
   */
  private ChargeheadConstantsModel getChargeHeadConstantsModel(
      StoreSalesMainModel storeSalesMainModel) {
    String billType = String.valueOf(
        storeSalesMainModel.getBillNo() != null ? storeSalesMainModel.getBillNo().getBillType()
            : null);
    String saleType = String.valueOf(storeSalesMainModel.getType());

    if (billType.equals("P") && saleType.equals(AccountingConstants.SALE_TYPE_S)) {
      return pharmacyAccountingDataInsertRepo
          .getChargeHeadConstantsModel(AccountingConstants.PHARMACY_CHARGEHEAD_PHMED);
    } else if (billType.equals("P") && saleType.equals(AccountingConstants.SALE_TYPE_R)) {
      return pharmacyAccountingDataInsertRepo
          .getChargeHeadConstantsModel(AccountingConstants.PHARMACY_CHARGEHEAD_PHRET);
    } else if (billType.equals("C") && saleType.equals(AccountingConstants.SALE_TYPE_S)) {
      return pharmacyAccountingDataInsertRepo
          .getChargeHeadConstantsModel(AccountingConstants.PHARMACY_CHARGEHEAD_PHCMED);
    } else if (billType.equals("C") && saleType.equals(AccountingConstants.SALE_TYPE_R)) {
      return pharmacyAccountingDataInsertRepo
          .getChargeHeadConstantsModel(AccountingConstants.PHARMACY_CHARGEHEAD_PHCRET);
    } else {
      return pharmacyAccountingDataInsertRepo.getChargeHeadConstantsModel(
          AccountingConstants.PHARMACY_CHARGEHEAD_HOSPITAL_OR_ISSUE_ITEM);
    }
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
