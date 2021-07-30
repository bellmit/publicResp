package com.insta.hms.core.billing;

import static com.insta.hms.jobs.common.QuartzJobHelper.buildJob;

import com.bob.hms.common.RequestContext;
import com.insta.hms.billing.BillConstants;
import com.insta.hms.billing.Receipt;
import com.insta.hms.common.DateHelper;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.modulesactivated.ModulesActivatedService;
import com.insta.hms.common.preferences.genericpreferences.GenericPreferencesService;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.core.fa.AccountingJobScheduler;
import com.insta.hms.core.insurance.InsuranceClaimModel;
import com.insta.hms.jobs.JobService;
import com.insta.hms.mdm.packages.PatientPackagesModel;
import com.insta.hms.mdm.paymentmode.PaymentModeMasterModel;
import com.insta.hms.mdm.tpas.TpaMasterModel;
import com.insta.hms.model.PatientDepositsSetoffAdjustmentsModel;
import com.insta.hms.model.StockIssueDetailsModel;
import com.insta.hms.model.StoreIssueReturnsDetailsModel;
import com.insta.hms.model.StoreSalesDetailsModel;
import com.insta.hms.security.usermanager.UUserModel;

import java.sql.SQLException;
import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.transaction.Transactional;

// TODO: Auto-generated Javadoc
/**
 * The Class AllocationService.
 */
@Service
public class AllocationService {

  /** The logger. */
  private static Logger logger = LoggerFactory.getLogger(AllocationService.class);
  
  /** The allocation repository. */
  @LazyAutowired
  AllocationRepository allocationRepository;

  /** The job service. */
  @LazyAutowired
  JobService jobService;

  /** The session service. */
  @LazyAutowired
  SessionService sessionService;

  /** The receipt service. */
  @LazyAutowired
  ReceiptService receiptService;

  /** The generic preferences service. */
  @LazyAutowired
  GenericPreferencesService genericPreferencesService;

  @Autowired
  ModulesActivatedService modulesActivatedService;

  /** The Constant SYSTEM_USER. */
  private static final String SYSTEM_USER = "_system";

  /** The accounting job scheduler. */
  @Autowired
  private AccountingJobScheduler accountingJobScheduler;
  
  /** The Constant ZERO. */
  private static final BigDecimal ZERO = BigDecimal.ZERO;
  
  /** The return sale types. */
  private List<String> returnSaleTypes = Arrays.asList("INVRET", "PHCRET", "PHRET");

  /** The sale types. */
  private List<String> saleTypes = Arrays.asList("INVITE", "PHCMED", "PHMED");

  /**
   * Allocate all open bills.
   *
   * @return the list
   */
  @Transactional
  public List<String> allocateAllOpenBills() {
    List<String> billList = allocationRepository.getAllOpenBills();
    for(String bill : billList) {
      allocate(bill);
    }
    return billList;
  }

  /**
   * Allocate.
   *
   * @param billNo the bill no
   * @return the list
   */
  @Transactional
  public List<String> allocateAllOpenBillsForDavita(String startDateStr, String endDateStr) {
    String pattern = "dd-MM-yyyy";
    Date startDate = DateHelper.getTimeStamp(startDateStr, pattern);
    Date endDate = DateHelper.getTimeStamp(endDateStr, pattern);
    List<String> billList = allocationRepository.getAllOpenBillsForDavita(startDate,endDate);
    for (String bill : billList) {
      allocate(bill);
    }
    return billList;
  }
  
  @Transactional
  public List<BillChargeReceiptAllocationModel> allocate(String billNo) {
    synchronized (billNo.intern()){
      firstPass(billNo);
      return secondPass(billNo);
    }
  }

  /**
   * Allocate.
   *
   * @param billNo the bill no
   * @param centerId the center id
   * @return the list
   */
  @Transactional
  public List<BillChargeReceiptAllocationModel> allocate(String billNo, Integer centerId) {
	  //Center ID has taken from the Job
	  synchronized (billNo.intern()){
      firstPass(billNo);
      return secondPass(billNo, centerId);
    }
  }

  /**
   * Check refund refund is sale return.
   *
   * @param billNo the bill no
   * @param receipt the receipt
   * @return the boolean
   */
  private Boolean checkRefundRefundIsSaleReturn(String billNo, ReceiptModel receipt) {
    return ("F".equals(receipt.getReceiptType()) 
        && StringUtils.isEmpty(receipt.getStoreRetailCustomerId())
        && "R".equals(allocationRepository.getStoreSaleType(billNo, receipt.getReceiptId())));
  }
  
  /**
   * Update receipts unallocated amount.
   *
   * @param billNo the bill no
   */
  private void updateReceiptsUnallocatedAmount(String billNo) {
    allocationRepository.flush();
    BillModel bill = (BillModel) allocationRepository.get(BillModel.class, billNo);
    List<ReceiptModel> receipts = bill.getReceipts();
    for (ReceiptModel receipt : receipts) {
      if (Boolean.TRUE.equals(checkRefundRefundIsSaleReturn(billNo, receipt))) {
        updateUnallocatedAmountForReturns(receipt);                
      } else {
        updateUnallocatedAmount(receipt, billNo);        
      }
    }
  }

  /**
   * Update unallocated amount.
   *
   * @param receipt the receipt
   * @param billNo the bill no
   */
  public void updateUnallocatedAmount(ReceiptModel receipt, String billNo) {
    BigDecimal allocatedAmount = allocationRepository.getAllocatedAmount(receipt.getReceiptId());
    BigDecimal receiptAmount = calculateTotalReceiptAmount(receipt);
    BigDecimal unallocatedAmount = receiptAmount.subtract(allocatedAmount);
    BigDecimal refundedAmount = allocationRepository.getRefundedAmount(receipt.getReceiptId());
    unallocatedAmount = unallocatedAmount.subtract(refundedAmount);
    if (billNo != null) {
      updateCancelledActivityAgaintReceiptUnallocated(billNo, receipt.getReceiptId(), 
          unallocatedAmount);      
    }
    receipt.setUnallocatedAmount(unallocatedAmount);
  }

  /**
   * Update cancelled activity against receipt unallocated.
   *
   * @param billNo the bill no
   * @param receiptId the receipt id
   * @param unallocatedAmount the unallocated amount
   */
  private void updateCancelledActivityAgaintReceiptUnallocated(String billNo, String receiptId,
      BigDecimal unallocatedAmount) {
    BillReceiptsModel billReceipt = allocationRepository.getBillReceipt(receiptId, billNo);
    if (billReceipt == null) {
      return;
    }
    List<BillChargeReceiptAllocationModel> cancelledAllocations = allocationRepository
        .getCancelledBillChargeReceiptAllocation(billReceipt.getBillReceiptId(), true);
    if (cancelledAllocations.isEmpty()) {
      return;
    }
    BigDecimal billAllocatedAmount = allocationRepository.calculateAllocatedAmount(receiptId);
    if (unallocatedAmount.compareTo(ZERO) == 0) {
      // if in case, there are cancelled activity allocations. but there is no negative due. 
      // then update the activity status to 'a'
      for(BillChargeReceiptAllocationModel allocation : cancelledAllocations) {
        allocation.setActivity("a");
        allocationRepository.persist(allocation);
      }
    } 
      
    Iterator<BillChargeReceiptAllocationModel> cancelledAllocationsItr = 
        cancelledAllocations.iterator();
    while (cancelledAllocationsItr.hasNext() && unallocatedAmount.compareTo(ZERO) > 0) {
      BillChargeReceiptAllocationModel allocation = cancelledAllocationsItr.next();
      BigDecimal cancelledAllocatedAmt = allocation.getAllocatedAmount().multiply(BigDecimal.ONE.negate());
      if (unallocatedAmount.compareTo(cancelledAllocatedAmt) >= 0) {
        unallocatedAmount = unallocatedAmount.subtract(cancelledAllocatedAmt);
        continue;
      }
      allocation.setActivity("a");
      allocation.setModifiedAt(new Date());
      BigDecimal newAllocatedAmt = cancelledAllocatedAmt.subtract(unallocatedAmount);
      allocation.setAllocatedAmount(newAllocatedAmt.negate());
      createBillChargeReceiptAllocations(allocation.getBillCharge(), 
          allocation.getBillReceipt(), unallocatedAmount.negate(), "c", allocation.getClaimId());
      unallocatedAmount = ZERO;
    }
  }

  /**
   * Update unallocated amount for returns.
   *
   * @param receipt the receipt
   */
  private void updateUnallocatedAmountForReturns(ReceiptModel receipt) {
    BigDecimal allocatedAmount = allocationRepository.getAllocatedAmount(receipt.getReceiptId());
    BigDecimal receiptAmount = calculateTotalReceiptAmount(receipt);
    BigDecimal unallocatedAmount = receiptAmount.subtract(allocatedAmount);
    BigDecimal refundedAmount = allocationRepository.getRefundedAmount(receipt.getReceiptId());
    unallocatedAmount = unallocatedAmount.add(refundedAmount);
    receipt.setUnallocatedAmount(unallocatedAmount);
  }
  
  
  /**
   * Patient amount allocation.
   *
   * @param billNo the bill no
   * @return the list
   */
  private List<BillChargeReceiptAllocationModel> patientAmountAllocation(String billNo) {
    //This method is to fix, below use case
    // 1. Charge1 amount 200/-
    // 2. Bill Discount amount -100/-
    // 3. Patient Due is 100/-   
    // so, we would have collected 100/- as cash receipt. 
    // As per the old logic, Only collected amount is going to allocated against the charges.
    // i.e we allocate only 100/- against 200/-
    // solution to Allocated all the charges.
    // consider, Collected amount + Discount/Return amount i.e 100+(-100*-1)= 200/- (NewReceipt amount)
    // Allocate the NewReceipt amount against the charge amount. 

    // Get receipt List
    List<ReceiptModel> receipts = allocationRepository.getPatientReceiptList(billNo);      
    if (receipts.isEmpty()) {
      return new LinkedList<>();
    }
    Boolean hasDiscountedPatAmt = false;
    ReceiptModel lastReceipt = null;
    for (ReceiptModel receipt : receipts) {
      if ("R".equals(receipt.getReceiptType())) {
        lastReceipt = receipt;
      }
    }
    
    // Get the unallocated billCharges
    List<BillChargeModel> newUnallocatedCharges = allocationRepository
        .getUnallocatedDiscountCharges(billNo, Boolean.TRUE); 

    BigDecimal discountedPatientAmount = getDiscountedUnallocatedAmt(newUnallocatedCharges, true);
    List<BillChargeReceiptAllocationModel> chargeReceiptAllocationList = new LinkedList<>();
    Date now = new Date();
    // initialize first receipt
    Iterator<ReceiptModel> receiptIterator = receipts.iterator();
    ReceiptModel currentReceipt = getNextReceipt(receiptIterator, billNo);
    // There are no unallocated 
    if (null == currentReceipt) {
      if (discountedPatientAmount.compareTo(ZERO) > 0 && lastReceipt != null) {
        currentReceipt = lastReceipt;
        hasDiscountedPatAmt = true;        
      } else {
        return chargeReceiptAllocationList;
      }
    }
    BigDecimal unallocatedAmount = (Boolean.FALSE.equals(hasDiscountedPatAmt)) 
        ? currentReceipt.getUnallocatedAmount() : discountedPatientAmount;
    BillReceiptsModel billReceipt = allocationRepository
        .getBillReceipt(currentReceipt.getReceiptId(), billNo);
    BigDecimal allocatedReceiptAmount = billReceipt.getAllocatedAmount();
    // Get Charges
    Boolean isSaleReturn = currentReceipt.getIsStoreReturn();
    List<BillChargeModel> billCharges = getBillChargeList(billNo, isSaleReturn);
    ChargesLoop:
    for (BillChargeModel billCharge : billCharges) {
      
      String chargeHead = billCharge.getChargeHead().getChargeheadId();
      //Continue, if the return sale type, This is handled along with ROF 
      if (returnSaleTypes.contains(chargeHead) && Boolean.FALSE.equals(isSaleReturn)) {
        continue;
      }
      
      if (unallocatedAmount.compareTo(ZERO) == 0 
          && discountedPatientAmount.compareTo(ZERO) == 0) {
        break;
      }
      
      if (unallocatedAmount.compareTo(ZERO) == 0 
          && discountedPatientAmount.compareTo(ZERO) > 0) {
          hasDiscountedPatAmt = true;
          unallocatedAmount = discountedPatientAmount;
          discountedPatientAmount = ZERO;
      } 
      BigDecimal diff = compareChargeAmountToAllocatedAmount(billCharge, true);
      if (diff.compareTo(ZERO) == 0) {
        continue;
      }
      // This should be only bill charges with charge amounts greater than allocated amounts only.
      // Because the first pass handles all cases with charge amounts less than allocated amounts.

      BigDecimal patientAmount = diff;
      while (unallocatedAmount.compareTo(patientAmount) < 0) {
        BigDecimal minAmount = unallocatedAmount;
        BillChargeReceiptAllocationModel allocation = new BillChargeReceiptAllocationModel(
            billCharge, billReceipt, minAmount, SYSTEM_USER, now, "a");
        allocatedReceiptAmount = allocatedReceiptAmount.add(minAmount);

        // Save the allocation.
        allocationRepository.persist(allocation);
        chargeReceiptAllocationList.add(allocation);

        patientAmount = patientAmount.subtract(minAmount);

        // set the allocated amount and then change receipt
        billReceipt.setAllocatedAmount(allocatedReceiptAmount);
        billReceipt.setModTime(new Date());
        currentReceipt = getNextReceipt(receiptIterator, billNo);
        if (null == currentReceipt) {
          //If there are no receipts and discountedPatientAmount > 0
          //Then allocated discounted amount
          if (patientAmount.compareTo(ZERO) > 0 && discountedPatientAmount.compareTo(ZERO) > 0) {
            minAmount = patientAmount.min(discountedPatientAmount);
            discountedPatientAmount = discountedPatientAmount.subtract(minAmount);
            allocation = new BillChargeReceiptAllocationModel(
                billCharge, billReceipt, minAmount, SYSTEM_USER, now, "a");
            allocatedReceiptAmount = allocatedReceiptAmount.add(minAmount);
            
            billReceipt.setAllocatedAmount(allocatedReceiptAmount);
            billReceipt.setModTime(new Date());
            
            // Save the allocation.
            allocationRepository.persist(allocation);
            chargeReceiptAllocationList.add(allocation);
          }
          // If There are newly added charges, 
          // then discounted amount will allocate with the last receipt.
          if (discountedPatientAmount.compareTo(ZERO) > 0 && lastReceipt != null) {
            currentReceipt = lastReceipt;
            hasDiscountedPatAmt = true;
            unallocatedAmount = discountedPatientAmount;
            continue ChargesLoop;
          } else { 
            return chargeReceiptAllocationList;
          }
        }

        billReceipt = allocationRepository.getBillReceipt(currentReceipt.getReceiptId(), billNo);
        allocatedReceiptAmount = billReceipt.getAllocatedAmount();

        unallocatedAmount = (Boolean.FALSE.equals(hasDiscountedPatAmt)) 
            ? currentReceipt.getUnallocatedAmount() : discountedPatientAmount;
      }
      // Create new entry in bill_charge_receipt_allocation table with minAmount as amount and
      // currentReceipt as receipt.

      BigDecimal minAmount = patientAmount;

      BillChargeReceiptAllocationModel allocation = new BillChargeReceiptAllocationModel(billCharge,
          billReceipt, minAmount, SYSTEM_USER, now, (minAmount.compareTo(ZERO) < 0) ? "c" : "a" );

      // Save the allocation.
      allocationRepository.persist(allocation);
      chargeReceiptAllocationList.add(allocation);

      // Add to allocatedReceiptAmount
      allocatedReceiptAmount = allocatedReceiptAmount.add(minAmount);

      // Subtract amount from receipt
      unallocatedAmount = unallocatedAmount.subtract(minAmount);

    }
    
    billReceipt.setAllocatedAmount(allocatedReceiptAmount);
    billReceipt.setModTime(new Date());
        
    return chargeReceiptAllocationList;
  }
  
  

  
  /**
   * Gets the discounted unallocated amount.
   *
   * @param newUnallocatedCharges the new unallocated charges
   * @param isPatient the is patient
   * @return the discounted unallocated amount
   */
  private BigDecimal getDiscountedUnallocatedAmt(List<BillChargeModel> newUnallocatedCharges, 
      boolean isPatient) {
    
    BigDecimal discountedPatientAmount = ZERO;
    BigDecimal discountedInsuranceAmount = ZERO;
    for (BillChargeModel newUnallocatedCharge : newUnallocatedCharges) {
      // Discounted/Returned amount will be negative.
      if ((newUnallocatedCharge.getAmount().compareTo(ZERO) < 0 && isPatient) ||
          (newUnallocatedCharge.getInsuranceClaimAmount().compareTo(ZERO) < 0 && !isPatient)) {
        BigDecimal chargeAmount = newUnallocatedCharge.getAmount()
            .add(newUnallocatedCharge.getTaxAmt()).negate();
        BigDecimal insuranceAmount = newUnallocatedCharge.getInsuranceClaimAmount()
            .add(newUnallocatedCharge.getSponsorTaxAmt()).negate();
        discountedInsuranceAmount = discountedInsuranceAmount.add(insuranceAmount);
        discountedPatientAmount = discountedPatientAmount
              .add(chargeAmount.subtract(insuranceAmount));
      }
    }
    if (isPatient) {
      return discountedPatientAmount;
    } else {
      return discountedInsuranceAmount;
    }
  }

  /**
   * Convert cancelled charges to adjustment.
   *
   * @param newlyAddedCharges the newly added charges
   * @return the boolean
   */
  private Boolean convertCancelledChargesToAdjustment(
      List<BillChargeReceiptAllocationModel> newlyAddedCharges) {
    // get all cancelled charges(Unmapped) for the billReceipt    
    Iterator billChargeAmountItr = newlyAddedCharges.iterator();
    BillChargeReceiptAllocationModel allocatedCharge = null;
    BigDecimal allocatedChargeAmount;
    BillChargeReceiptAllocationModel cancelledCharge;
    AllocatedCharges:
    while (billChargeAmountItr.hasNext()) {
      allocatedCharge = (BillChargeReceiptAllocationModel) billChargeAmountItr.next();
      String chargeHead = allocatedCharge.getBillCharge().getChargeHead().getChargeheadId();
      InsuranceClaimModel claimId = allocatedCharge.getClaimId();
      List<BillChargeReceiptAllocationModel> cancelledBillChargeRecAllocs;
      if (saleTypes.contains(chargeHead)) {
        String saleReturnName = getSaleReturnTypeName(chargeHead);
        cancelledBillChargeRecAllocs = allocationRepository
            .getCancelledBillChargeReceiptAllocation(
                allocatedCharge.getBillReceipt().getBillReceiptId(), false, saleReturnName, 
                (claimId == null) ? Boolean.FALSE : Boolean.TRUE);
      } else {
        cancelledBillChargeRecAllocs = allocationRepository
            .getCancelledBillChargeReceiptAllocation(allocatedCharge.getBillReceipt()
                .getBillReceiptId(), false, (claimId == null) ? Boolean.FALSE : Boolean.TRUE);
      }
      // If no cancelled charges, for billReceipt, then continue.
      if (cancelledBillChargeRecAllocs.isEmpty()) {
        continue;
      }
      allocatedChargeAmount = allocatedCharge.getAllocatedAmount();
      Iterator cancelledBillCharges = cancelledBillChargeRecAllocs.iterator();
      if (allocatedChargeAmount.compareTo(BigDecimal.ZERO) == 0) {
        break;
      }
      // Iterate all cancelled charges and adjust with the newly added charges. 
      // if any cancelled charge amount is not adjusted, it remains as cancelled activity.
      CancelledCharges:
      while (cancelledBillCharges.hasNext()) {
        if (allocatedCharge == null || allocatedChargeAmount.compareTo(BigDecimal.ZERO) == 0) {
          break;
        }
        cancelledCharge = (BillChargeReceiptAllocationModel) cancelledBillCharges.next();
        String cancelChargeHead = cancelledCharge.getBillCharge().getChargeHead().getChargeheadId();
        String saleType = getSaleTypeName(cancelChargeHead);
        // continue, if the canceled charge is return type and allocated charge is not saleType
        if (returnSaleTypes.contains(cancelChargeHead) && (saleType == null 
            || !saleType.equals(chargeHead))) {
          continue;
        }
        
        BigDecimal cancelledAmount = cancelledCharge.getAllocatedAmount().multiply(BigDecimal.ONE.negate());
        if (cancelledCharge.getBillCharge().getChargeId()
            .equals(allocatedCharge.getBillCharge().getChargeId()) 
            || allocatedChargeAmount.compareTo(cancelledAmount) == 0) {
          
          if (!cancelledCharge.getBillCharge().getChargeId()
              .equals(allocatedCharge.getBillCharge().getChargeId())) {
            cancelledCharge.setActivity("a");
            cancelledCharge.setModifiedAt(new Date());
            allocationRepository.persist(cancelledCharge);
          }
          continue AllocatedCharges;
        } 
        cancelledCharge.setActivity("a");
        cancelledCharge.setModifiedAt(new Date());
        InsuranceClaimModel cancelledClaimId = null;
        if (cancelledCharge.getBillCharge().getInsuranceClaimAmount().compareTo(ZERO) > 0) {
          cancelledClaimId = cancelledCharge.getClaimId();
        }
        //Reverse the canceled charge allocation entry for adjustment
        createBillChargeReceiptAllocations(cancelledCharge.getBillCharge(),
            allocatedCharge.getBillReceipt(), cancelledAmount, "a", cancelledClaimId);
        allocationRepository.persist(cancelledCharge);
        
        if (cancelledAmount.compareTo(allocatedChargeAmount) <= 0) {
          createBillChargeReceiptAllocations(cancelledCharge.getBillCharge(),
              allocatedCharge.getBillReceipt(), cancelledAmount.negate(), "a", cancelledClaimId);
          allocatedChargeAmount = allocatedChargeAmount.subtract(cancelledAmount);
          continue;
        } 
          
        while (cancelledAmount.compareTo(BigDecimal.ZERO) > 0) {
          cancelledAmount = cancelledAmount.subtract(allocatedChargeAmount);
          createBillChargeReceiptAllocations(cancelledCharge.getBillCharge(),
              allocatedCharge.getBillReceipt(), allocatedChargeAmount.negate(), "a", cancelledClaimId);   
          allocatedCharge = getNextAllocatedCharge(billChargeAmountItr, cancelledCharge, saleType);
          if (allocatedCharge == null) {
            createBillChargeReceiptAllocations(cancelledCharge.getBillCharge(),
                cancelledCharge.getBillReceipt(), cancelledAmount.negate(), 
                (cancelledAmount.compareTo(ZERO) < 0) ? "a" : "c", cancelledClaimId);              
            cancelledAmount = BigDecimal.ZERO;
            allocatedChargeAmount = allocatedChargeAmount.subtract(cancelledAmount);
            break;
          }
          
          allocatedChargeAmount = allocatedCharge.getAllocatedAmount();    
          if (cancelledAmount.compareTo(allocatedChargeAmount) <= 0) {
            createBillChargeReceiptAllocations(cancelledCharge.getBillCharge(),
                allocatedCharge.getBillReceipt(), cancelledAmount.negate(), "a", cancelledClaimId);
            allocatedChargeAmount = allocatedChargeAmount.subtract(cancelledAmount);
            continue CancelledCharges;
          }
        }          
      }
    }
    return true;
  }
  

  /**
   * Gets the sale allocated amount.
   *
   * @param billReceipt the bill receipt
   * @param saleReturnType the sale return type
   * @param unAlloctdRetBillCharge the unallocated return bill charge
   * @return the sale allocated amount
   */
  private BigDecimal getSaleAllocatedAmount(BillReceiptsModel billReceipt, String saleReturnType, 
      BillChargeModel unAlloctdRetBillCharge) {

    // get relative sale type for return sale type
    String saleType;
    saleType = getSaleTypeName(saleReturnType);
    if (saleType == null) {
      return ZERO;
    }

    List<Map<String, Object>> billChargesAllocs = allocationRepository
        .getBillReceiptsAllocationWithSaleReturns(billReceipt.getBillReceiptId(), saleType, 
            saleReturnType);

    List<Map<String, Object>> saleItems = new LinkedList<>();
    List<Map<String, Object>> returnItems = new LinkedList<>();
    for (Map<String, Object> billChargeAlloc : billChargesAllocs) {
      String chargeHead = billChargeAlloc.get("chargeHead").toString();
      if (saleType.equals(chargeHead)) {
        saleItems.add(billChargeAlloc);
      }
      
      if (saleReturnType.equals(chargeHead)) {
        returnItems.add(billChargeAlloc);        
      } 
      
    }

    if (CollectionUtils.size(saleItems) == 0) {
      return ZERO;
    }     
    
    if (CollectionUtils.size(saleItems) == 1 && CollectionUtils.size(returnItems) == 0) {
      return new BigDecimal(saleItems.get(0).get("allocatedAmount").toString());
    } 
    
    Iterator<Map<String, Object>> saleItemsItr = saleItems.iterator();
    Iterator<Map<String, Object>> returnItemItr = returnItems.iterator();
    
    List<Map<String, Object>> alloctdSaleReturnDetailsList = getSaleReturnChargeItemDetails(
        unAlloctdRetBillCharge.getChargeId(), saleReturnType);
    for (Map<String, Object> unAlloctdSaleReturnDetails : alloctdSaleReturnDetailsList) {
      Integer unAlloctdRetMedicineId = Integer.parseInt(unAlloctdSaleReturnDetails
          .get("retMedicineId").toString());
      Integer unAlloctdRetBatchNo = Integer.parseInt(unAlloctdSaleReturnDetails
          .get("retBatchNo").toString());
      //compare the sale medicine_id, issueBatchNo with return item
      BigDecimal saleAmount = BigDecimal.ZERO;
      while(saleItemsItr.hasNext()) {
        Map<String, Object> saleBillChargeRow = saleItemsItr.next();
        saleAmount = saleAmount.add(
            new BigDecimal(saleBillChargeRow.get("allocatedAmount").toString()));

        //For sale item
        List<Map<String, Object>> saleItemDetailsList = getSaleCharegeItemDetails(
            saleBillChargeRow.get("chargeId").toString(), saleType, unAlloctdRetMedicineId, 
            unAlloctdRetBatchNo);
        if (saleItemDetailsList.isEmpty()) {
          continue;
        }
        for (Map<String, Object> saleItemDetails : saleItemDetailsList) {
          Integer medicineId = Integer.parseInt(saleItemDetails.get("medicineId").toString());
          Integer itemBatchNo = Integer.parseInt(saleItemDetails.get("itemBatchNo").toString());  
          BigDecimal saleReturnQty = (BigDecimal) saleItemDetails.get("returnQuantity");
          while (returnItemItr.hasNext()) {
            Map<String, Object> retBillChargeRow = returnItemItr.next();
            //For return item      
            List<Map<String, Object>> saleReturnDetailsList = getSaleReturnChargeItemDetails(retBillChargeRow
                .get("chargeId").toString(), saleReturnType, medicineId, itemBatchNo);
            if (saleReturnDetailsList.isEmpty()) {
              continue;
            }
            
            Map<String, Object> saleReturnDetails = saleReturnDetailsList.get(0);
            
            Integer retMedicineId = Integer.parseInt(saleReturnDetails.get("retMedicineId").toString());
            Integer retBatchNo = Integer.parseInt(saleReturnDetails.get("retBatchNo").toString());
            BigDecimal retSaleQuantity = (BigDecimal) saleReturnDetails.get("quantity");
            BigDecimal returnAmount = new BigDecimal(retBillChargeRow.get("allocatedAmount")
                .toString());
            returnAmount = (returnAmount.compareTo(ZERO) < 0) 
                ? returnAmount.multiply(BigDecimal.ONE.negate()) : returnAmount;
            
            if (medicineId.equals(retMedicineId) && itemBatchNo.equals(retBatchNo) 
                && saleReturnQty.compareTo(retSaleQuantity) <= 0 
                && medicineId.equals(unAlloctdRetMedicineId) && itemBatchNo.equals(unAlloctdRetBatchNo)
                ) {
              saleAmount = saleAmount.subtract(returnAmount);
              while (returnItemItr.hasNext() && saleAmount.compareTo(ZERO) > 0) {
                retBillChargeRow = (Map<String, Object>) returnItemItr.next();
                saleReturnDetailsList = getSaleReturnChargeItemDetails(retBillChargeRow
                    .get("chargeId").toString(), saleReturnType, medicineId, itemBatchNo);
                if (saleReturnDetailsList.isEmpty()) {
                  continue;
                }
                //For return item      
                saleReturnDetails = saleReturnDetailsList.get(0);
                retMedicineId = Integer.parseInt(saleReturnDetails.get("retMedicineId").toString());
                retBatchNo = Integer.parseInt(saleReturnDetails.get("retBatchNo").toString());
                retSaleQuantity = (BigDecimal) saleReturnDetails.get("quantity");
                returnAmount = new BigDecimal(retBillChargeRow.get("allocatedAmount").toString());
                returnAmount = (returnAmount.compareTo(ZERO) < 0) 
                    ? returnAmount.multiply(BigDecimal.ONE.negate()) : returnAmount;
                if (medicineId.equals(retMedicineId) && itemBatchNo.equals(retBatchNo) 
                    && saleReturnQty.compareTo(retSaleQuantity) >= 0 
                    && medicineId.equals(unAlloctdRetMedicineId) 
                    && itemBatchNo.equals(unAlloctdRetBatchNo)) {
                  saleAmount = saleAmount.subtract(returnAmount);
                }
              }
            }
          }
        }
      }
      return saleAmount; 
      
    }
    return ZERO;
  }
  
  
  /**
   * Gets the sale type name.
   *
   * @param saleReturnType the sale return type
   * @return the sale type name
   */
  private String getSaleTypeName(String saleReturnType) {
    String saleType;
    switch (saleReturnType) {
      case "INVRET":
        saleType = "INVITE";
        break;
      case "PHCRET":
        saleType = "PHCMED";
        break;
      case "PHRET":
        saleType = "PHMED";
        break;
      default:
        saleType = null;
    }
    return saleType;
  }
  
  /**
   * Gets the sale return type name.
   *
   * @param saleType the sale type
   * @return the sale return type name
   */
  private String getSaleReturnTypeName(String saleType) {
    String saleReturnType;
    switch (saleType) {
    case "INVITE":
      saleReturnType = "INVRET";
      break;
    case "PHCMED":
      saleReturnType = "PHCRET";
      break;
    case "PHMED":
      saleReturnType = "PHRET";
      break;
    default:
      saleReturnType = null;
    }
    return saleReturnType;
  }

  /**
   * Gets the sale charege item details.
   *
   * @param chargeId the charge id
   * @param saleType the sale type
   * @param medicineId the medicine id
   * @param batchNo the batch no
   * @return the sale charege item details
   */
  private List<Map<String, Object>> getSaleCharegeItemDetails(String chargeId, String saleType, 
      Integer medicineId, Integer batchNo) {
    Map<String, Object> returnMap = new HashMap<>();
    List<Map<String, Object>> returnList = new ArrayList<Map<String, Object>>();
    if ("INVITE".equals(saleType)) {
      BillActivityChargeModel billChargeActivity = allocationRepository
          .getBillActivityCharges(chargeId);
      Integer itemIssueId = Integer.valueOf(billChargeActivity.getId().getActivityId());

      StockIssueDetailsModel storeIssueDetails = (StockIssueDetailsModel) 
          allocationRepository.get(StockIssueDetailsModel.class, itemIssueId);
      returnMap.put("medicineId", storeIssueDetails.getStoreItemDetails().getMedicineId());
      returnMap.put("itemBatchNo", storeIssueDetails.getItemBatchId());
      returnMap.put("returnQuantity", storeIssueDetails.getReturnQty());
      returnMap.put("quantity", storeIssueDetails.getQty());
      returnList.add(returnMap);
      return returnList;
    }
    // For medicine, pharmacy sale
    // PHMED,PHCMED
    List<StoreSalesDetailsModel> saleDetailsList = allocationRepository.getSaleStoreDetailsForCharge(chargeId, 
        medicineId, batchNo);
    for (StoreSalesDetailsModel saleDetail : saleDetailsList) {
      returnMap.put("medicineId", saleDetail.getMedicineId());
      returnMap.put("itemBatchNo", saleDetail.getItemBatchId());
      returnMap.put("returnQuantity", saleDetail.getReturnQty());
      returnMap.put("quantity", saleDetail.getQuantity());
      returnList.add(returnMap);
    }
    return returnList;

  } 

  /**
   * Gets the sale return charge item details.
   *
   * @param chargeId the charge id
   * @param saleReturnType the sale return type
   * @return the sale return charge item details
   */
  private List<Map<String, Object>> getSaleReturnChargeItemDetails(String chargeId, String saleReturnType) {
    
    return getSaleReturnChargeItemDetails(chargeId, saleReturnType, 0, 0);
  
  } 
  
  
  
  
  /**
   * Gets the sale return charge item details.
   *
   * @param chargeId the charge id
   * @param saleReturnType the sale return type
   * @param medicineId the medicine id
   * @param batchNo the batch no
   * @return the sale return charge item details
   */
  private List<Map<String, Object>> getSaleReturnChargeItemDetails(String chargeId, 
      String saleReturnType, Integer medicineId, Integer batchNo) {
    Map<String, Object> returnMap = new HashMap<>();
    List<Map<String, Object>> returnList = new ArrayList<Map<String, Object>>();
    if ("INVRET".equals(saleReturnType)) {
      BillActivityChargeModel billChargeActivity = allocationRepository
          .getBillActivityCharges(chargeId);
      Integer itemIssueId = Integer.valueOf(billChargeActivity.getId().getActivityId());
      
      StoreIssueReturnsDetailsModel storeIssueReturn = (StoreIssueReturnsDetailsModel) 
          allocationRepository.get(StoreIssueReturnsDetailsModel.class, itemIssueId);
      returnMap.put("retMedicineId", storeIssueReturn.getStoreItemDetails().getMedicineId());
      returnMap.put("retBatchNo", storeIssueReturn.getItemBatchId());
      returnMap.put("returnQuantity", 0);
      returnMap.put("quantity", storeIssueReturn.getQty());
      returnList.add(returnMap);
      
      return returnList;
    } 
    // For medicine, pharmacy return
    // PHRET, PHCRET
    List<StoreSalesDetailsModel> saleDetailsList = allocationRepository.getSaleStoreDetailsForCharge(
        chargeId, medicineId, batchNo);
    
    for (StoreSalesDetailsModel saleDetail : saleDetailsList) {
      
      returnMap.put("retMedicineId", saleDetail.getMedicineId());
      returnMap.put("retBatchNo", saleDetail.getItemBatchId());
      returnMap.put("returnQuantity", saleDetail.getReturnQty());
      returnMap.put("quantity", saleDetail.getQuantity());
      
      returnList.add(returnMap);
    }
    return returnList;
    
  } 

  /**
   * Gets the next allocated charge.
   *
   * @param billChargeAmountItr the bill charge amount itr
   * @param cancelledCharge the cancelled charge
   * @param saleType the sale type
   * @return the next allocated charge
   */
  private BillChargeReceiptAllocationModel getNextAllocatedCharge(Iterator billChargeAmountItr, 
      BillChargeReceiptAllocationModel cancelledCharge, String saleType) {
    BillChargeReceiptAllocationModel allocatedCharge = null;
    while (billChargeAmountItr.hasNext()) {
      allocatedCharge = (BillChargeReceiptAllocationModel) billChargeAmountItr.next();
      // if Current cancelled billReceiptId should be same to next billReceiptId 
      //  else cancelled claimID should same as next row claimId
      if ((allocatedCharge.getBillReceipt().getBillReceiptId() == cancelledCharge.getBillReceipt()
          .getBillReceiptId() || allocatedCharge.getClaimId() == null) 
          || (allocatedCharge.getClaimId() == cancelledCharge.getClaimId())) {
        //continue, if saleType is not null and saleType is not matching with nextCharge.headId, 
        // This mean, this is not same sale type.
        if (saleType != null && !saleType.equals(allocatedCharge.getBillCharge().getChargeHead()
            .getChargeheadId())) {
          continue;
        }
        break;
      } else {
        allocatedCharge = null;
      }
    }

    return allocatedCharge;
  }

  /**
   * Sponsor amount allocation.
   *
   * @param billNo the bill no
   * @return the list
   */
  private List<BillChargeReceiptAllocationModel> sponsorAmountAllocation(String billNo) {
    // Get receipt List
    List<ReceiptModel> receipts = allocationRepository.getSponsorReceiptList(billNo);
  
    List<BillChargeReceiptAllocationModel> chargeReceiptAllocationList = new LinkedList<>();

    Date now = new Date();
    
    Iterator<ReceiptModel> receiptsItr = receipts.iterator();
    while (receiptsItr.hasNext()) {
      ReceiptModel receipt = receiptsItr.next();
      BigDecimal receiptAmount = receipt.getUnallocatedAmount();
      if (!receiptsItr.hasNext()) {
        // Get the unallocated billCharges
        List<BillChargeModel> newUnallocatedCharges = allocationRepository
            .getUnallocatedDiscountCharges(billNo, Boolean.FALSE); 
        BigDecimal discountedPatientAmount = getDiscountedUnallocatedAmt(newUnallocatedCharges, 
            Boolean.FALSE);
        receiptAmount = receiptAmount.add(discountedPatientAmount);
      }
      BillReceiptsModel billReceipt = allocationRepository.getBillReceipt(receipt.getReceiptId(),
          billNo);
      BigDecimal allocatedReceiptAmount = billReceipt.getAllocatedAmount();

      TpaMasterModel tpa = receipt.getTpaId();
      // get charges List with entry in bill_charge_claim with specified TPA id
      List<BillChargeModel> billCharges = allocationRepository.getCharges(billNo, tpa.getTpaId());
      if (null == billCharges) {
        continue;
      }
      for (BillChargeModel charge : billCharges) {
        String chargeId = charge.getChargeId();
        BillChargeClaimModel bcc = allocationRepository.getBillChargeClaim(chargeId, billNo,
            tpa.getTpaId());
        BigDecimal claimAmount = bcc.getInsuranceClaimAmt();
        BigDecimal claimTaxAmount = (bcc.getTaxAmount() == null) ? ZERO : bcc.getTaxAmount();
        BigDecimal totalClaimAmount = claimAmount.add(claimTaxAmount);
        BigDecimal claimRecdTotal = ZERO;
        if (null != bcc.getClaimRecdTotal()) {
          claimRecdTotal = bcc.getClaimRecdTotal();
        }
        BigDecimal remainingAmount = totalClaimAmount.subtract(claimRecdTotal);
        BigDecimal minAmount = receiptAmount.min(remainingAmount);
        if (minAmount.compareTo(BigDecimal.ZERO) == 0) {
          continue;
        }
        // Create new entry in bill_charge_receipt_allocation table with minAmount as amount and
        // currentReceipt as receipt.
        BillChargeReceiptAllocationModel allocation = new BillChargeReceiptAllocationModel(charge,
            billReceipt, bcc.getClaimId(), minAmount, SYSTEM_USER, now, "a");
        // Update claimRecdTotal in bill charge claim.
        bcc.setClaimRecdTotal(claimRecdTotal.add(minAmount));
        // Add to allocated amount
        allocatedReceiptAmount = allocatedReceiptAmount.add(minAmount);

        // Save the allocation.
        allocationRepository.persist(allocation);
        chargeReceiptAllocationList.add(allocation);

        receiptAmount = receiptAmount.subtract(minAmount);
      }
      billReceipt.setAllocatedAmount(allocatedReceiptAmount);
      billReceipt.setModTime(new Date());
    }
    return chargeReceiptAllocationList;
  }

  /**
   * Calculate total receipt amount.
   *
   * @param receipt the receipt
   * @return the big decimal
   */
  private BigDecimal calculateTotalReceiptAmount(ReceiptModel receipt) {
    return receipt.getAmount().add(receipt.getTdsAmount()).add(receipt.getOtherDeductions());
  }

  /**
   * Compare charge amount to allocated amount.
   *
   * @param billCharge the bill charge
   * @param isPatientAmount the is patient amount
   * @return the big decimal
   */
  private BigDecimal compareChargeAmountToAllocatedAmount(BillChargeModel billCharge,
      boolean isPatientAmount) {
    return compareChargeAmountToAllocatedAmount(billCharge, isPatientAmount, false);
  }
  
  
  /**
   * Compare charge amount to allocated amount.
   *
   * @param billCharge the bill charge
   * @param isPatientAmount the is patient amount
   * @param isDeposit the is deposit
   * @return the big decimal
   */
  private BigDecimal compareChargeAmountToAllocatedAmount(BillChargeModel billCharge,
      boolean isPatientAmount, boolean isDeposit) {
    BigDecimal chargeAmount;
    if (isPatientAmount) {
      chargeAmount = getPatientAmount(billCharge);
    } else {
      BigDecimal sponsorAmount = billCharge.getInsuranceClaimAmount();
      BigDecimal sponsorTax = billCharge.getSponsorTaxAmt();
      chargeAmount = sponsorAmount.add(sponsorTax);
    }

    BigDecimal allocatedAmount;
    String chargeId = billCharge.getChargeId();
    if (isPatientAmount) {
      if (isDeposit) {
        allocatedAmount = allocationRepository.getAllocatedDepositPatientAmount(chargeId);
      } else {
        allocatedAmount = allocationRepository.getAllocatedPatientAmount(chargeId);
      }
    } else {
      allocatedAmount = allocationRepository.getAllocatedInsuranceAmount(chargeId);
    }
    BigDecimal finalAmount = ZERO;
    if (chargeAmount.compareTo(allocatedAmount) != 0) {
      finalAmount = chargeAmount.subtract(allocatedAmount);
    }
    return finalAmount;
  }

  /**
   * Check charge level restrictions.
   *
   * @param billCharge the bill charge
   * @param receipt the receipt
   * @return true, if successful
   */
  private boolean checkChargeLevelRestrictions(BillChargeModel billCharge, ReceiptModel receipt) {
    // Check if the given chargeId can be paid with the given receipt (if any).
    return true;
  }

  /**
   * Check bill level restrictions.
   *
   * @param billNo the bill no
   * @param receipt the receipt
   * @return true, if successful
   */
  private boolean checkBillLevelRestrictions(String billNo, ReceiptModel receipt) {
    // Check the given receipt with billNo, MrNo, visitType restrictions.
    BillModel bill = (BillModel) allocationRepository.get(BillModel.class, billNo);
    List<Map<String, String>> restrictions = allocationRepository
        .getBillLevelRestrictions(receipt.getReceiptId());
    if (CollectionUtils.isEmpty(restrictions)) {
      return true;
    }
    for (Map<String, String> restriction : restrictions) {
      String entityType = restriction.get("entityType");
      String entityId = restriction.get("entityId");
      String billValue = "";
      switch (entityType) {
        case BillConstants.Restrictions.BILL_NO:
          billValue = bill.getBillNo();
          break;
        case BillConstants.Restrictions.VISIT_TYPE:
          billValue = bill.getVisitType().toString();
          break;
        case BillConstants.Restrictions.BILL_TYPE:
          return true; // For now we will be NOT be considering this restriction.
          /** 
           * Refer to HMS-30940 for more information.
           * This is changed so that deposits with bill_type
           * restriction are considered as general deposits.
           * The below lines are to be uncommented when the bill_type restriction 
           * of the deposits have to be considered 
           **/

        case BillConstants.Restrictions.PAT_PACKAGE_ID:
          Integer patPackId = Integer.parseInt(entityId);
          String packageId = allocationRepository.getPackageIdOfBill(billNo, patPackId);
          String mrNo = bill.getVisitId().getMrNo();
          PatientPackagesModel patientPackages = (PatientPackagesModel) allocationRepository
              .get(PatientPackagesModel.class, patPackId);
          // Check if that patient package exists
          if (null == patientPackages) {
            continue;
          }
          // Check if mrNo and the packageId match
          if (mrNo.equals(patientPackages.getMrNo())
              && packageId.equals(Integer.toString(patientPackages.getPackageId()))) {
            return true;
          }
          break;
        default:
      }
      if (entityId.equals(billValue)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Reduce allocation amount.
   *
   * @param billCharge the bill charge
   * @param reductionAmount the reduction amount
   * @param isPatientAmount the is patient amount
   */
  private void reduceAllocationAmount(BillChargeModel billCharge, BigDecimal reductionAmount,
      boolean isPatientAmount) {
    // Inserts new allocation with negative amount to account for the reduction.
    // IMPORTANT !!!!! The reduction amount is the positive amount to be reduced so negate the
    // values before insert.
    BillChargeReceiptAllocationModel newAllocation;
    List<Map<String, Object>> allocations = null;
    if (isPatientAmount) {
      allocations = allocationRepository.getPatientAmountAllocationsLifo(billCharge.getChargeId());
    } else {
      allocations = allocationRepository.getSponsorAmountAllocationsLifo(billCharge.getChargeId());
    }
    Date now = new Date();
    for (Map<String, Object> allocation : allocations) {

      if (reductionAmount.compareTo(BigDecimal.ZERO) == 0) {
        return;
      }

      BigDecimal allocatedAmount = new BigDecimal(allocation.get("allocatedAmount").toString());
      if (allocatedAmount.compareTo(ZERO) == 0) {
        continue;
      }
      BigDecimal minAmount = allocatedAmount.min(reductionAmount);
      BillReceiptsModel billReceipt = (BillReceiptsModel) allocationRepository.get(
        BillReceiptsModel.class, Long.parseLong(allocation.get("billReceiptId").toString()));
      String activity = "c";
      // if the reverse allocation is of Deposit receipt or Insurance amount. mark as "a" activity.
      if (billReceipt.getReceiptNo().getIsDeposit() || !isPatientAmount) {
        activity = "a";
      }
      
      InsuranceClaimModel claim = null;
      if (allocation.containsKey("claimId") && null != allocation.get("claimId")) {
        claim = (InsuranceClaimModel) allocationRepository.get(
            InsuranceClaimModel.class, allocation.get("claimId").toString());   
        String tpaId = billReceipt.getReceiptNo().getTpaId().getTpaId();
        updateBillChargeClaim(billCharge, tpaId, minAmount.negate());
      }
      // negate the amount as the entries should be have negative amounts.
      newAllocation = new BillChargeReceiptAllocationModel(billCharge, billReceipt,
          claim, minAmount.negate(), SYSTEM_USER, now, activity);
      allocationRepository.persist(newAllocation);

      reductionAmount = reductionAmount.subtract(minAmount);
    }
    allocationRepository.flush();
  }
  
  
  /**
   * Reduce deposit allocated amount.
   *
   * @param billCharge the bill charge
   * @param reductionAmount the reduction amount
   * @return the list
   */
  private List<BillChargeReceiptAllocationModel> reduceDepositAllocatedAmount(
      BillChargeModel billCharge, BigDecimal reductionAmount) {
    
    List<Map<String, Object>> allocations = null;
    allocations = allocationRepository.getPatientDepositAmountAllocationsLifo(
        billCharge.getChargeId());
    
    Date now = new Date();
    
    List<Map<String, Object>> bcraAmtList = allocationRepository
        .calculateBillReceiptAllocatedAmount(billCharge.getBillNo().getBillNo());
    Map<Long, BigDecimal> bcraMap = new HashMap<>();
    //converting List to Map
    for(Map<String, Object> bcra : bcraAmtList) {
      bcraMap.put((Long) bcra.get("billReceiptId"), (BigDecimal) bcra.get("allocatedAmount"));
    }
    List<BillChargeReceiptAllocationModel> returnAllocations = new ArrayList<>();
    for (Map<String, Object> allocation : allocations) {
      BillChargeReceiptAllocationModel newAllocation;
      if (reductionAmount.compareTo(BigDecimal.ZERO) == 0) {
        return returnAllocations;
      }
      Long billReceiptId = (Long) allocation.get("billReceiptId");
      BigDecimal allocatedAmount = new BigDecimal(allocation.get("allocatedAmount").toString());
      BigDecimal minAmount;
      if ((allocatedAmount.compareTo(ZERO) > 0 && allocatedAmount.compareTo(reductionAmount) >= 0) ||
          (allocatedAmount.compareTo(ZERO) < 0 && allocatedAmount.compareTo(reductionAmount) <= 0)) {
          minAmount = reductionAmount;
          reductionAmount = ZERO;
      } else {
        minAmount = allocatedAmount;
        reductionAmount = reductionAmount.subtract(minAmount);
      }
      
      //Reducing amount shouldn't be less than to billReceipt Allocated amount.
      BigDecimal bcraAmt = bcraMap.get(billReceiptId);
      if (bcraAmt.compareTo(minAmount) <= 0) {
        minAmount = bcraAmt;
        reductionAmount = (reductionAmount.compareTo(ZERO) > 0) ? 
            reductionAmount.subtract(bcraAmt) : reductionAmount;
        bcraMap.put(billReceiptId, ZERO);
      } else {
        bcraMap.put(billReceiptId, bcraAmt.subtract(minAmount));
      }
      BillReceiptsModel billReceipt = (BillReceiptsModel) allocationRepository.get(
          BillReceiptsModel.class, billReceiptId);
      // negate the amount as the entries should have negative amounts.
      newAllocation = new BillChargeReceiptAllocationModel(billCharge, billReceipt,
          minAmount.negate(), SYSTEM_USER, now, "a");
      allocationRepository.persist(newAllocation);
      returnAllocations.add(newAllocation);
      
    }
    allocationRepository.flush();
    return returnAllocations;
  }

  /**
   * Update bill receipts allocated amounts.
   *
   * @param billNo the bill no
   */
  private void updateBillReceiptsAllocatedAmounts(String billNo) {
    allocationRepository.flush();
    List<BillReceiptsModel> billReceipts = allocationRepository.getNonDepositBillReceipts(billNo);
    if (null == billReceipts) {
      return;
    }

    for (BillReceiptsModel billReceipt : billReceipts) {
      BigDecimal updatedAllocatedAmount = allocationRepository
          .calculateAllocatedAmount(billReceipt.getBillReceiptId());
      billReceipt.setAllocatedAmount(updatedAllocatedAmount);
      billReceipt.setModTime(new Date());
      allocationRepository.persist(billReceipt);
    }
    allocationRepository.flush();
  }

  /**
   * Check receipts.
   *
   * @param billNo the bill no
   */
  private void checkReceipts(String billNo) {
    List<ReceiptModel> receipts = allocationRepository.getReceiptList(billNo);
    for (ReceiptModel receipt : receipts) {
      String receiptId = receipt.getReceiptId();
      // if Receipt type is Refund and Receipt for Sales return then allocate negative amount 
      if (Boolean.TRUE.equals(checkRefundRefundIsSaleReturn(billNo, receipt))) {
        updateUnallocatedAmountForReturns(receipt);
      } else {
        if ("F".equals(receipt.getReceiptType())) {
          continue;
        }
        BigDecimal refundedAmount = allocationRepository.getRefundedAmount(receiptId);
        BigDecimal allocatedAmount = allocationRepository.calculateAllocatedAmount(receiptId);
        BigDecimal totalReceiptAmount = calculateTotalReceiptAmount(receipt);
        BigDecimal usedAmount = allocatedAmount.add(refundedAmount);
        BigDecimal reductionAmount = usedAmount.subtract(totalReceiptAmount);
        if (reductionAmount.compareTo(BigDecimal.ZERO) > 0) {
          reduceReceiptAllocations(receipt, reductionAmount);
        } else {
          updateUnallocatedAmount(receipt, null);
        }
      }
    }
    updateBillReceiptsAllocatedAmounts(billNo);
  }

  /**
   * Reduce receipt allocations.
   *
   * @param receipt the receipt
   * @param reductionAmount the reduction amount
   */
  private void reduceReceiptAllocations(ReceiptModel receipt, BigDecimal reductionAmount) {
    String receiptId = receipt.getReceiptId();
    List<Map<String, Object>> allocations = allocationRepository
        .getAllocationsOfReceipt(receiptId);
    for (Map<String, Object> allocation : allocations) {
      BigDecimal allocationAmount = new BigDecimal(allocation.get("allocatedAmount").toString());
      BillChargeReceiptAllocationModel newAllocation;
      BillChargeModel billCharge = (BillChargeModel) allocationRepository.get(BillChargeModel.class, 
          allocation.get("chargeId").toString());      
      BigDecimal minAmount = allocationAmount.min(reductionAmount);

      BillReceiptsModel billReceipt = (BillReceiptsModel) allocationRepository.get(
          BillReceiptsModel.class, Long.parseLong(allocation.get("billReceiptId").toString()));
      InsuranceClaimModel claim = null;
      if (null != allocation.get("claimId")) {
        claim = (InsuranceClaimModel) allocationRepository.get(
            InsuranceClaimModel.class, allocation.get("claimId").toString());   
        String tpaId = receipt.getTpaId().getTpaId();
        updateBillChargeClaim(billCharge, tpaId, minAmount.negate());
      }
      newAllocation = new BillChargeReceiptAllocationModel(billCharge, billReceipt, claim, 
          minAmount.negate(), SYSTEM_USER, new Date(), "c");
      allocationRepository.persist(newAllocation);


      reductionAmount = reductionAmount.subtract(minAmount);
      if (reductionAmount.compareTo(BigDecimal.ZERO) == 0) {
        return;
      }
    }
  }

  /**
   * Update bill charge claim.
   *
   * @param billCharge the bill charge
   * @param tpaId the tpa id
   * @param changeAmount the change amount
   */
  private void updateBillChargeClaim(BillChargeModel billCharge, String tpaId,
      BigDecimal changeAmount) {
    // the change amount will be added to claimRecdTotal. So provide negative amount to subtract.
    String chargeId = billCharge.getChargeId();
    String billNo = billCharge.getBillNo().getBillNo();
    BillChargeClaimModel bcc = allocationRepository.getBillChargeClaim(chargeId, billNo, tpaId);
    if (null != bcc) {
      bcc.setClaimRecdTotal(bcc.getClaimRecdTotal().add(changeAmount));
    }
  }

  /**
   * First pass.
   *
   * @param billNo the bill no
   */
  private void firstPass(String billNo) {
    // This method is going to find the charges which were either cancelled or had the amount
    // reduced and update the allocated amount of bill_receipts and unallocated amounts of receipts.
    // Also check the receipts which were refunded or had their amount reduced.
 
    List<BillChargeModel> billCharges = getBillChargeList(billNo);
    
    if (billCharges.isEmpty()) {
      return;
    }
   
    //reverse the cancelled/returned deposit set off amount. 
    depositsFirstPass(billNo, billCharges);
    
    boolean isUpdated = false;
    //Check for charge adjustments, if adjusted, then reverse the canceled entries 
    //and update the cancel(c) activity with Adjusted(a)
    for (BillChargeModel billCharge : billCharges) {
      
      if (billCharge.getAmount().compareTo(ZERO) < 0) {
        continue;
      }
      
      BigDecimal patientDiff = compareChargeAmountToAllocatedAmount(billCharge, true);
      BigDecimal sponsorDiff = compareChargeAmountToAllocatedAmount(billCharge, false);
      if (patientDiff.compareTo(BigDecimal.ZERO) < 0 || 
          (billCharge.getAmount().compareTo(ZERO) == 0 && patientDiff.compareTo(ZERO) > 0)) {
        // Negate the diff before calling the function as diff is supposed to be negative.
        reduceAllocationAmount(billCharge, patientDiff.negate(), true);
        isUpdated = true;
      }
      if (sponsorDiff.compareTo(BigDecimal.ZERO) < 0 || 
          (billCharge.getAmount().compareTo(ZERO) == 0 && sponsorDiff.compareTo(ZERO) > 0)) {
        // Negate the diff before calling the function as diff is supposed to be negative.
        reduceAllocationAmount(billCharge, sponsorDiff.negate(), false);
        isUpdated = true;
      }
    }
 
    if (isUpdated) {
      updateBillReceiptsAllocatedAmounts(billNo);
    }
    checkReceipts(billNo);
  }
  
  /**
   * Check discount charge or insert.
   *
   * @param billCharge the bill charge
   * @return the boolean
   */
  private Boolean checkDiscountChargeOrInsert(BillChargeModel billCharge) {
    // discount charges are RET, ROF, DISCOUNT, Check the allocations are present or not, 
    // if there is a difference, insert of 
    List<BillChargeReceiptAllocationModel> allocations;
    String chargeHead = billCharge.getChargeHead().getChargeheadId();    
    
    // Get the all allocations against the negative charge_id.
    allocations = allocationRepository.getBillChargeAllocations(billCharge.getChargeId());
    
    // Discount will have only patient Amount
    // also include tax amount and convert the negative amount to positive
    BigDecimal insuranceAmount = billCharge.getInsuranceClaimAmount()
        .add(billCharge.getSponsorTaxAmt()).multiply(BigDecimal.ONE.negate());
    BigDecimal amount = billCharge.getAmount().add(
        billCharge.getTaxAmt()).multiply(BigDecimal.ONE.negate());
    amount = amount.subtract(insuranceAmount);
    // Allocate negative BillCharges.
    if (allocations.isEmpty()) {
      createNewBillChargeReceiptForDiscount(billCharge, amount, insuranceAmount, chargeHead);
      return false;
    }

    String activity = "c";
    if ("ROF".equals(chargeHead)) {
      //delete all previously allocated ROF charges.
      allocationRepository.deleteAllocationsOfChargeId(billCharge.getChargeId());
      allocationRepository.flush();
      createNewBillChargeReceiptForDiscount(billCharge, amount, insuranceAmount, chargeHead);
      return false;
    }
    BigDecimal allocationedAmount = ZERO;
    BigDecimal insuranceAllocations = ZERO;
    BillChargeReceiptAllocationModel lastAllocation = null; 
    BillChargeReceiptAllocationModel lastInsAllocation = null;
    for (BillChargeReceiptAllocationModel allocation : allocations) {
      //convert the negative amount to positive
      BigDecimal amountTmp = allocation.getAllocatedAmount();
      if (allocation.getClaimId() == null) {
        lastAllocation = allocation;
        allocationedAmount = allocationedAmount.add(amountTmp);        
      } else {
        lastInsAllocation = allocation;
        insuranceAllocations = insuranceAllocations.add(amountTmp);        
      }
    }
    
    allocationedAmount = (allocationedAmount.compareTo(ZERO) < 0) 
        ? allocationedAmount.negate() : allocationedAmount;
    insuranceAllocations = (insuranceAllocations.compareTo(ZERO) < 0) 
        ? insuranceAllocations.negate() : insuranceAllocations;
    if (allocationedAmount.compareTo(amount) != 0 && lastAllocation != null) {
      BigDecimal diff = amount.subtract(allocationedAmount);
      List<BillReceiptsModel> billReceipts = allocationRepository
          .getBillReceiptsForDiscountTypeCharges(billCharge.getBillNo().getBillNo(), false);
      Iterator<BillReceiptsModel> billReceiptsItr = billReceipts.iterator();
      while (billReceiptsItr.hasNext()) {
        BillReceiptsModel billReceipt = billReceiptsItr.next();
        if (diff.compareTo(ZERO) == 0) {
          break;
        }
        if (diff.compareTo(billReceipt.getAllocatedAmount()) <= 0) {
          createBillChargeReceiptAllocations(billCharge, billReceipt, diff.negate(), activity);
          break;
        }
        diff = diff.subtract(billReceipt.getAllocatedAmount());
        createBillChargeReceiptAllocations(billCharge, billReceipt, 
            billReceipt.getAllocatedAmount().negate(), activity);
      }
    } 
    
    if (insuranceAllocations.compareTo(insuranceAmount) != 0 && lastInsAllocation != null) {
      List<BillReceiptsModel> billReceiptsIns = allocationRepository
          .getBillReceiptsForDiscountTypeCharges(billCharge.getBillNo().getBillNo(), true);
      BigDecimal diff = insuranceAmount.subtract(insuranceAllocations);
      createBillChargeReceiptAllocations(lastInsAllocation.getBillCharge(), 
          lastInsAllocation.getBillReceipt(), (diff.compareTo(ZERO) > 0) ? diff.negate() : diff, 
              activity, 
              lastInsAllocation.getClaimId());
      Iterator<BillReceiptsModel> billReceiptsInsItr = billReceiptsIns.iterator();
      while (billReceiptsInsItr.hasNext()) {
        BillReceiptsModel billReceipt = billReceiptsInsItr.next();
        if (diff.compareTo(ZERO) == 0) {
          break;
        }
        TpaMasterModel tpa = billReceipt.getReceiptNo().getTpaId();
        BillChargeClaimModel bcc = allocationRepository.getBillChargeClaim(billCharge.getChargeId(), 
            billReceipt.getBillNo().getBillNo(), tpa.getTpaId());
        if (diff.compareTo(billReceipt.getAllocatedAmount()) <= 0) {
          createBillChargeReceiptAllocations(billCharge, billReceipt, diff.negate(), activity, 
              bcc.getClaimId());
          break;
        }
        diff = diff.subtract(billReceipt.getAllocatedAmount());
        createBillChargeReceiptAllocations(billCharge, billReceipt, 
            billReceipt.getAllocatedAmount().negate(), activity, bcc.getClaimId());
      }
    } 
    
    
    return true;    
  }

  /**
   * Deposits first pass.
   *
   * @param billNo the bill no
   * @param billCharges the bill charges
   */
  private void depositsFirstPass(String billNo, List<BillChargeModel> billCharges) {
    
    List<BillChargeReceiptAllocationModel> reducedBillCharges = new ArrayList<>();
    for (BillChargeModel billCharge : billCharges) {
      String chargeGroup = billCharge.getChargeGroup().getChargegroupId();
      String chargeHead = billCharge.getChargeHead().getChargeheadId();
      if (billCharge.getAmount().compareTo(ZERO) < 0 && chargeGroup.equals("RET")) {
        continue;
      }
      
      BigDecimal patientDiff = compareChargeAmountToAllocatedAmount(billCharge, true, true);
      if (patientDiff.compareTo(ZERO) < 0 || 
          (billCharge.getAmount().compareTo(ZERO) <= 0 && patientDiff.compareTo(ZERO) > 0)){
        reducedBillCharges.addAll(reduceDepositAllocatedAmount(billCharge, patientDiff.negate()));
      }
    }
    Map<Long, BigDecimal> cancelledDepositAmount = new HashMap<>();
    cancelledDepositAmount = getCancelledChargesIterate(reducedBillCharges);
    
    if (!cancelledDepositAmount.isEmpty()) {
      adjustDepositCancelledAmount(billNo, cancelledDepositAmount);      
    }
     
    allocationRepository.flush();
  }

  /**
   * Gets the cancelled charges iterate.
   *
   * @param reducedBillCharges the reduced bill charges
   * @return the cancelled charges iterate
   */
  private Map<Long, BigDecimal> getCancelledChargesIterate(List<BillChargeReceiptAllocationModel> reducedBillCharges) {
    Map<Long, BigDecimal> cancelledDepositAmount = new HashMap<>();
    for(BillChargeReceiptAllocationModel bcra : reducedBillCharges) {
      Long billReceiptId = bcra.getBillReceipt().getBillReceiptId();
      BigDecimal reducedAmount = bcra.getAllocatedAmount().negate();
      if (cancelledDepositAmount.containsKey(billReceiptId)) {
        BigDecimal billReceiptAmount = cancelledDepositAmount.get(billReceiptId);
        cancelledDepositAmount.put(billReceiptId, billReceiptAmount.add(reducedAmount));
      } else {
        cancelledDepositAmount.put(billReceiptId, reducedAmount);
      }
    }
    return cancelledDepositAmount;
  }

  /**
   * Adjust deposit cancelled amount.
   *
   * @param billNo the bill no
   * @param cancelledDepositAmount the cancelled deposit amount
   */
  private void adjustDepositCancelledAmount(String billNo, 
      Map<Long, BigDecimal> cancelledDepositAmount) {
    
    
    Iterator<Map.Entry<Long, BigDecimal>> cancelledAmountItr = cancelledDepositAmount.entrySet().iterator();
    
    BigDecimal totalAllocatedAmount = BigDecimal.ZERO;
    List<BillChargeModel> billCharges = getBillChargeList(billNo);
    Date now = new Date();
    while( cancelledAmountItr.hasNext() ) {
      Map.Entry<Long, BigDecimal> billReceiptMap = cancelledAmountItr.next();
      Long billReceiptId = billReceiptMap.getKey();
      BillReceiptsModel billReceipt = (BillReceiptsModel) allocationRepository
          .get(BillReceiptsModel.class, billReceiptId);
      BigDecimal amountToBeAllocated = billReceiptMap.getValue();
      
      Iterator<BillChargeModel> billChargeItr = billCharges.iterator();
      while (billChargeItr.hasNext()) {
        BillChargeModel charge = billChargeItr.next();
        if (amountToBeAllocated.compareTo(BigDecimal.ZERO) == 0) {
          
          if (!cancelledAmountItr.hasNext()) {
            break;
          }
          
          billReceiptMap = cancelledAmountItr.next();
          billReceiptId = billReceiptMap.getKey();
          billReceipt = (BillReceiptsModel) allocationRepository
              .get(BillReceiptsModel.class, billReceiptId); 
          amountToBeAllocated = billReceiptMap.getValue();
          
        }
  
        BigDecimal diff = compareChargeAmountToAllocatedAmount(charge, true);
        if (diff.compareTo(BigDecimal.ZERO) <= 0) {
          continue;
        }
  
        BigDecimal chargeAmount = diff;
        BigDecimal minAmount = chargeAmount.min(amountToBeAllocated);
        // Allocating
        BillChargeReceiptAllocationModel allocation = new BillChargeReceiptAllocationModel(charge,
            billReceipt, minAmount, SYSTEM_USER, now, "a");
        allocationRepository.persist(allocation);
        totalAllocatedAmount = totalAllocatedAmount.add(minAmount);
        amountToBeAllocated = amountToBeAllocated.subtract(minAmount);
        
      }

      // Update allocated Amount if the entire amount could not be allocated. And give it back to
      // deposit receipt.
      if (amountToBeAllocated.compareTo(BigDecimal.ZERO) != 0) {
        String depositType = getDepositType(billReceipt.getReceiptNo().getReceiptId());
        reduceDepositSetoffAmount(billReceipt, amountToBeAllocated, depositType);
        
      }
    }
  }
  
  private String getDepositType(String receiptId) {
    List<Map<String, String>> receiptUsage = allocationRepository.getIpBillLevelRestrictions(
        receiptId);
    return (receiptUsage != null && !receiptUsage.isEmpty()) ? 
        DepositType.IP : DepositType.GENERAL;
  }
  
  /**
   * Second pass.
   *
   * @param billNo the bill no
   * @return the list
   */
  private List<BillChargeReceiptAllocationModel> secondPass(String billNo) {
	int centerId = 0;
    if(null != RequestContext.getCenterId()) {
      centerId = RequestContext.getCenterId();
    }
    return secondPass(billNo, centerId);
  }

  /**
   * Second pass.
   *
   * @param billNo the bill no
   * @param centerId the center id
   * @return the list
   */
  private List<BillChargeReceiptAllocationModel> secondPass(String billNo, Integer centerId) {
	  // This method is going to start allocating amounts to charges using the unallocated amounts in
	  // the receipts.
	  List<BillChargeReceiptAllocationModel> allocations = new LinkedList<>();
	  if (isBillWithMrNo(billNo)) {
		  allocations.addAll(rewardPointsAmountAllocation(billNo));
		  allocationRepository.flush();
		  allocations.addAll(depositAmountAllocation(billNo, DepositType.PACKAGE, centerId));
		  allocationRepository.flush();
		  allocations.addAll(depositAmountAllocation(billNo, DepositType.IP, centerId));
		  allocationRepository.flush();
		  allocations.addAll(depositAmountAllocation(billNo, DepositType.GENERAL, centerId));
		  allocationRepository.flush();
	  }
   
	  List<BillChargeReceiptAllocationModel> patientAndInsAllocatedCharges = 
	      patientAmountAllocation(billNo);

	  patientAndInsAllocatedCharges.addAll(sponsorAmountAllocation(billNo));	  

	  //insert/update discount charges
	  createUpdateDiscountedCharges(billNo);
    
	  if (!patientAndInsAllocatedCharges.isEmpty()) {
	    convertCancelledChargesToAdjustment(patientAndInsAllocatedCharges);
	  }
	  
	  allocations.addAll(patientAndInsAllocatedCharges);
	  
	  updateBillReceiptsAllocatedAmounts(billNo);
	  
	  //Call refund reference method
	  createRefundReceiptReferenceForBill(billNo);
	  

    //CheckReceipts for reverse amount
    checkReceipts(billNo);

    //Charge is cancelled, but still not mapped with refund receipt.
    updateUnmappedReverseCharges(billNo);

    //Update receipts unallocated amount
	  updateReceiptsUnallocatedAmount(billNo);
	  
	  //Update bill total 
	  updateBillTotal(billNo);	 
	  
	  //Unlock bill 
	  unlockBill(billNo);

	  return allocations;
  }

  /**
   * Creates the update discounted charges.
   *
   * @param billNo the bill no
   */
  private void createUpdateDiscountedCharges(String billNo) {
    // charges with negative amount.
    allocationRepository.flush();
    List<BillChargeModel> billCharges = getBillChargeList(billNo, true);
    for (BillChargeModel billCharge : billCharges) {
      checkDiscountChargeOrInsert(billCharge);      
    }
  }

 
  /**
   * Creates the new bill charge receipt for discount.
   *
   * @param billCharge the bill charge
   * @param amount the amount
   * @param insuranceAmount the insurance amount
   * @param chargeHead the charge head
   */
  private void createNewBillChargeReceiptForDiscount(BillChargeModel billCharge, BigDecimal amount, 
      BigDecimal insuranceAmount, String chargeHead) {
    String activity = "c";
    String billNo = billCharge.getBillNo().getBillNo();
    if ("ROF".equals(chargeHead)) {
      //ROF will not have any tax amounts
      activity = "a";
      amount = billCharge.getAmount().subtract(billCharge.getInsuranceClaimAmount());
      insuranceAmount = billCharge.getInsuranceClaimAmount();
    }
    //get the billReceiptId of previously inserted receipt.
    List<BillReceiptsModel> billReceipts = null;
    if (amount.compareTo(ZERO) != 0) {
      billReceipts = allocationRepository
          .getBillReceiptsForDiscountTypeCharges(billNo, false);        
    }
    
    if (billReceipts != null && !billReceipts.isEmpty()) {
      
      // For ROF, if Round off Amount is Negative, Then allocate as negative, else positive
      if ("ROF".equals(chargeHead)) {
        BillReceiptsModel billReceiptRof = billReceipts.get(billReceipts.size()-1);
        createBillChargeReceiptAllocations(billCharge, billReceiptRof, amount, activity);
      } else {
        Iterator<BillReceiptsModel> billReceiptItr = billReceipts.iterator();
        BillReceiptsModel billReceipt;
        while (billReceiptItr.hasNext() && amount.compareTo(ZERO) > 0) {
          billReceipt = billReceiptItr.next();
          if (billReceipt.getAllocatedAmount().compareTo(BigDecimal.ZERO) <= 0) {
            break;
          }
          // if sale return or patient issue return
          // then, get the sale or issue allocated bill_receipt_id.
          if (returnSaleTypes.contains(chargeHead)) {
            //Get the respective available saleType allocated amount
            BigDecimal saleAmount = getSaleAllocatedAmount(billReceipt, chargeHead, billCharge);
            if (saleAmount.compareTo(ZERO) == 0) {
              continue;
            }
            if (amount.compareTo(saleAmount) <= 0) {
              createBillChargeReceiptAllocations(billCharge, billReceipt, amount.negate(), activity);
              break;
            }
            createBillChargeReceiptAllocations(billCharge, billReceipt, saleAmount.negate(), 
                activity);
            amount = amount.subtract(saleAmount);
            continue;
          }        
          //discounted amount is less or equal to BillReceipt's AllocatedAmount
          if (amount.compareTo(billReceipt.getAllocatedAmount()) <= 0) {
            createBillChargeReceiptAllocations(billCharge, billReceipt, amount.negate(), activity);
            break;
          }
          BigDecimal minDiscount = billReceipt.getAllocatedAmount();
          createBillChargeReceiptAllocations(billCharge, billReceipt, minDiscount.negate(), activity);
          amount = amount.subtract(minDiscount);
        
        }
      }
    }
    
    List<BillReceiptsModel> insuranceBillReceipts = null;
    if (insuranceAmount.compareTo(ZERO) != 0) {
      insuranceBillReceipts = allocationRepository
          .getBillReceiptsForDiscountTypeCharges(billNo, true);
    }
    
    if (insuranceBillReceipts != null && !insuranceBillReceipts.isEmpty()) {
      
      // For ROF, if Round off Amount is Negative, Then allocate as negative, else positive
      if ("ROF".equals(chargeHead)) {
        BillReceiptsModel insBillReceiptRof = insuranceBillReceipts.get(insuranceBillReceipts.size()-1);
        TpaMasterModel tpa = insBillReceiptRof.getReceiptNo().getTpaId();
        BillChargeClaimModel bcc = allocationRepository.getBillChargeClaim(billCharge.getChargeId(), 
            billNo, tpa.getTpaId());
        createBillChargeReceiptAllocations(billCharge, insBillReceiptRof, insuranceAmount, 
            activity, bcc.getClaimId());
      } else {
      
        Iterator insuranceBillReceiptsItr = insuranceBillReceipts.iterator();
        BillReceiptsModel insBillReceipt;
        while (insuranceBillReceiptsItr.hasNext() && insuranceAmount.compareTo(ZERO) > 0) {
          insBillReceipt = (BillReceiptsModel) insuranceBillReceiptsItr.next();
          TpaMasterModel tpa = insBillReceipt.getReceiptNo().getTpaId();
          BillChargeClaimModel bcc = allocationRepository.getBillChargeClaim(billCharge.getChargeId(), 
              billNo, tpa.getTpaId());
          if (insBillReceipt.getAllocatedAmount().compareTo(BigDecimal.ZERO) <= 0) {
            break;
          }
          //discounted amount is less or equal to BillReceipt's AllocatedAmount
          if (insuranceAmount.compareTo(insBillReceipt.getAllocatedAmount()) <= 0) {
            createBillChargeReceiptAllocations(billCharge, insBillReceipt, insuranceAmount.negate(), 
                activity, bcc.getClaimId());
            break;
          }
          BigDecimal minDiscount = insBillReceipt.getAllocatedAmount();
          createBillChargeReceiptAllocations(billCharge, insBillReceipt, minDiscount.negate(), 
              activity, bcc.getClaimId());
          insuranceAmount = insuranceAmount.subtract(minDiscount);
        }
      }
    }
  }
  
  /**
   * Checks if is bill with mr no.
   *
   * @param billNo the bill no
   * @return true, if is bill with mr no
   */
  private boolean isBillWithMrNo(String billNo) {
    BillModel bill = (BillModel) allocationRepository.get(BillModel.class, billNo);
    return (bill.getVisitType() != 'r');
  }

  /**
   * Reward points amount allocation.
   *
   * @param billNo the bill no
   * @return the list
   */
  private List<BillChargeReceiptAllocationModel> rewardPointsAmountAllocation(String billNo) {
    List<BillChargeReceiptAllocationModel> allocationList = new LinkedList<>();
    BillModel bill = (BillModel) allocationRepository.get(BillModel.class, billNo);
    if (bill.getIsTpa()) {
      return allocationList;
    }
    List<ReceiptModel> receipts = allocationRepository.getRewardPointReceipts(billNo);
    if (null == receipts) {
      return allocationList;
    }
    Iterator receiptsIterator = receipts.iterator();
    ReceiptModel receipt = getNextReceipt(receiptsIterator, billNo);
    if (null == receipt) {
      return allocationList;
    }
    BillReceiptsModel billReceipt = allocationRepository.getBillReceipt(receipt.getReceiptId(),
        billNo);
    BigDecimal unallocatedAmount = receipt.getUnallocatedAmount();
    BigDecimal allocatedAmount = billReceipt.getAllocatedAmount();

    Date now = new Date();
    int totalRedeemedPoints = bill.getPointsRedeemed();
    BigDecimal totalRedeemedAmount = bill.getPointsRedeemedAmt();
    BigDecimal rate = totalRedeemedAmount.divide(new BigDecimal(totalRedeemedPoints));
    List<BillChargeModel> billCharges = allocationRepository
        .getBillChargesWithRedeemedPoints(billNo);
    for (BillChargeModel charge : billCharges) {
      BigDecimal diff = compareChargeAmountToAllocatedAmount(charge, true);
      int pointsRedeemed = charge.getRedeemedPoints();
      BigDecimal redeemAmount = rate.multiply(new BigDecimal(pointsRedeemed));
      if (redeemAmount.compareTo(diff) > 0) {
        BigDecimal reductionAmount = redeemAmount.subtract(diff);
        reduceAllocationAmount(charge, reductionAmount, true);
      }
      while (redeemAmount.compareTo(unallocatedAmount) > 0) {

        receipt.setUnallocatedAmount(BigDecimal.ZERO);
        billReceipt.setAllocatedAmount(allocatedAmount.add(unallocatedAmount));
        billReceipt.setModTime(new Date());
        redeemAmount = redeemAmount.subtract(unallocatedAmount);
        BillChargeReceiptAllocationModel allocation = new BillChargeReceiptAllocationModel(charge,
            billReceipt, unallocatedAmount, SYSTEM_USER, now);
        allocationRepository.persist(allocation);
        allocationList.add(allocation);
        receipt = getNextReceipt(receiptsIterator, billNo);
        if (null == receipt) {
          return allocationList;
        }
        billReceipt = allocationRepository.getBillReceipt(receipt.getReceiptId(), billNo);
        unallocatedAmount = receipt.getUnallocatedAmount();
        allocatedAmount = billReceipt.getAllocatedAmount();
      }
      BillChargeReceiptAllocationModel allocation = new BillChargeReceiptAllocationModel(charge,
          billReceipt, redeemAmount, SYSTEM_USER, now);
      unallocatedAmount = unallocatedAmount.subtract(redeemAmount);
      allocatedAmount = allocatedAmount.add(redeemAmount);
      allocationRepository.persist(allocation);
      allocationList.add(allocation);
    }
    receipt.setUnallocatedAmount(unallocatedAmount);
    billReceipt.setAllocatedAmount(allocatedAmount);
    billReceipt.setModTime(new Date());
    return allocationList;
  }

  /**
   * Gets the next receipt.
   *
   * @param receiptIterator the receipt iterator
   * @param billNo the bill no
   * @return the next receipt
   */
  private ReceiptModel getNextReceipt(Iterator<ReceiptModel> receiptIterator, String billNo) {
    while (receiptIterator.hasNext()) {
      ReceiptModel currentReceipt = receiptIterator.next();
      BigDecimal unallocatedAmount = currentReceipt.getUnallocatedAmount();
      Boolean saleReturnReceipt = false;
      if (Boolean.TRUE.equals(checkRefundRefundIsSaleReturn(billNo, currentReceipt))) {
        saleReturnReceipt = true;
        currentReceipt.setIsStoreReturn(true);
      }
      if (((saleReturnReceipt && unallocatedAmount.compareTo(BigDecimal.ZERO) < 0) 
          || ("R".equals(currentReceipt.getReceiptType()) && unallocatedAmount.compareTo(BigDecimal.ZERO) > 0))
          && checkBillLevelRestrictions(billNo, currentReceipt)) {
        return currentReceipt;
      }
    }
    return null;
  }

  /**
   * Gets the patient amount.
   *
   * @param billCharge the bill charge
   * @return the patient amount
   */
  private BigDecimal getPatientAmount(BillChargeModel billCharge) {
    BigDecimal amount = billCharge.getAmount();
    BigDecimal tax = billCharge.getTaxAmt();
    BigDecimal sponsorAmount = billCharge.getInsuranceClaimAmount();
    BigDecimal sponsorTax = billCharge.getSponsorTaxAmt();
    BigDecimal insuranceAmount = sponsorAmount.add(sponsorTax);
    return amount.add(tax).subtract(insuranceAmount);
  }

  /**
   * Gets the receipt list.
   *
   * @param billNo the bill no
   * @return the receipt list
   */
  private List<ReceiptModel> getReceiptList(String billNo) {
    // List based on priority group the receipts and return a linked list.
    BillModel bill = (BillModel) allocationRepository.get(BillModel.class, billNo);
    List receipts = bill.getReceipts();
    return new LinkedList<>(receipts);
  }

  /**
   * Gets the bill charge list.
   *
   * @param billNo the bill no
   * @return the bill charge list
   */
  private List<BillChargeModel> getBillChargeList(String billNo) {
    return getBillChargeList(billNo, false);
  }
  
  /**
   * Gets the bill charge list.
   *
   * @param billNo the bill no
   * @param returnCharges the return charges
   * @return the bill charge list
   */
  private List<BillChargeModel> getBillChargeList(String billNo, Boolean returnCharges) {
    // List based on priority group the bill_charges and return a linked list.
    List<BillChargeModel> billCharges = 
        allocationRepository.getCharges(billNo, returnCharges);
    return billCharges;
  }

  /**
   * Gets the bill data.
   *
   * @param billNo
   *          the bill no
   * @return the bill data
   */
  @Transactional
  public BillModel getBillData(String billNo) {
    BillModel bill = (BillModel) allocationRepository.get(BillModel.class, billNo);
    Hibernate.initialize(bill.getClaims());
    Hibernate.initialize(bill.getBillCharges());
    Hibernate.initialize(bill.getReceipts());
    return bill;
  }

  /**
   * Update bill charge receipt.
   *
   * @param billChargeReceiptAllocation the bill charge receipt allocation
   * @param refundReferenceId the refund reference id
   * @param unallocatedAmount the unallocated amount
   */
  public void updateBillChargeReceipt(BillChargeReceiptAllocationModel billChargeReceiptAllocation, 
      Long refundReferenceId, BigDecimal unallocatedAmount) {
    if (refundReferenceId > 0) {
      ReceiptRefundReferenceModel receiptRefundReference = 
          (ReceiptRefundReferenceModel) allocationRepository.get(
              ReceiptRefundReferenceModel.class, refundReferenceId);
      billChargeReceiptAllocation.setRefundReferenceId(receiptRefundReference);
    }
    billChargeReceiptAllocation.setModifiedAt(new Date());
    
    if (unallocatedAmount.compareTo(BigDecimal.ZERO) > 0) {
      billChargeReceiptAllocation.setAllocatedAmount(unallocatedAmount.negate());
    }
    allocationRepository.persist(billChargeReceiptAllocation);
    allocationRepository.flush();
  }
  
  /**
   * Creates the refund receipt reference for bill.
   *
   * @param billNo the bill no
   */
  private void createRefundReceiptReferenceForBill(String billNo) {
    List<ReceiptModel> refundReceipts = allocationRepository.getUnReferencedRefundReceipts(billNo); 
    for (ReceiptModel receipt : refundReceipts) {
      createReceiptRefundReference(receipt, billNo);
    }  
    
  }
  
  /**
   * Creates the receipt refund reference.
   *
   * @param receiptDtoObject the receipt dto object
   * @param billNo the bill no
   * @return true, if successful
   */
  @Transactional
  public boolean createReceiptRefundReference(ReceiptModel receiptDtoObject, String billNo) {
    BigDecimal totalRefund = receiptDtoObject.getAmount().add(receiptDtoObject.getTdsAmount())
        .negate();
    
    List<ReceiptModel> receipts = allocationRepository
        .getPatientReceiptList(billNo, true, true);
    ReceiptModel refundReceipt = receiptDtoObject;
    if (Boolean.TRUE.equals(checkRefundRefundIsSaleReturn(billNo, refundReceipt))) {
      refundReceipt.setIsStoreReturn(true);
      return true;
    }
    //First create the receiptRefundReferences for the cancelled receipts.
    totalRefund = createRefundReferencesForPendingAllocations(billNo, refundReceipt, totalRefund);
    if (totalRefund.compareTo(BigDecimal.ZERO) <= 0) {
      return true;
    }
    for (ReceiptModel receipt : receipts) {
      BigDecimal receiptAmount = receipt.getAmount().add(receipt.getTdsAmount())
          .add(receipt.getOtherDeductions());
      BigDecimal previouslyRefundedAmount = allocationRepository
          .getRefundedAmount(receipt.getReceiptId());

      receiptAmount = receiptAmount.subtract(previouslyRefundedAmount);
      if (receiptAmount.compareTo(BigDecimal.ZERO) == 0) {
        continue;
      }

      if (totalRefund.compareTo(receiptAmount) <= 0) {
        ReceiptRefundReferenceModel refundReference = new ReceiptRefundReferenceModel(refundReceipt,
            receipt, totalRefund);
        allocationRepository.persist(refundReference);
        allocationRepository.flush();
        return true;
      }
      ReceiptRefundReferenceModel refundReference = new ReceiptRefundReferenceModel(refundReceipt,
          receipt, receiptAmount);
      allocationRepository.persist(refundReference);

      totalRefund = totalRefund.subtract(receiptAmount);
    }
    allocationRepository.flush();
        
    return false;
  }
  

  /**
   * Creates the refund references for pending allocations.
   *
   * @param billNo the bill no
   * @param refundReceipt the refund receipt
   * @param totalAmount the total amount
   * @return the big decimal
   */
  private BigDecimal createRefundReferencesForPendingAllocations(String billNo, 
      ReceiptModel refundReceipt, BigDecimal totalAmount) {
    BigDecimal zero = BigDecimal.ZERO;
    //Get the list of reversed charges with unmapped refund reference id.
    List<Map<String, Object>> reversedUnmappedCharges = allocationRepository
        .getReversedAllocatedSumForBill(billNo);
    BigDecimal remainingAmount = totalAmount;
    BigDecimal receiptRefundAmount = zero;
    for (Map<String, Object> reversedUnmappedCharge : reversedUnmappedCharges) {
      BigDecimal availableReceiptAmount;
      BigDecimal reversedSum = new BigDecimal(reversedUnmappedCharge.get("allocatedAmount").toString());
      reversedSum = reversedSum.multiply(BigDecimal.ONE.negate());
      ReceiptModel receipt = (ReceiptModel) allocationRepository.get(ReceiptModel.class,
          reversedUnmappedCharge.get("receiptId").toString());
      BigDecimal receiptAmount = receipt.getAmount().add(receipt.getTdsAmount())
          .add(receipt.getOtherDeductions());
      BigDecimal previouslyRefundedAmount = allocationRepository
          .getRefundedAmount(receipt.getReceiptId());
      receiptAmount = receiptAmount.subtract(previouslyRefundedAmount);
      if (receiptAmount.compareTo(BigDecimal.ZERO) == 0) {
        continue;
      }
      
      if (remainingAmount.compareTo(receiptAmount) > 0) {
        availableReceiptAmount = receiptAmount;
      } else {
        availableReceiptAmount = remainingAmount;
      }

      if (availableReceiptAmount.compareTo(reversedSum) >= 0) {
        receiptRefundAmount = reversedSum;
      } else {
        receiptRefundAmount = availableReceiptAmount;
      }
      createReceiptRefundReference(refundReceipt, receipt, receiptRefundAmount); 
      remainingAmount = remainingAmount.subtract(receiptRefundAmount);
      if (remainingAmount.compareTo(zero) <= 0) {
        return zero;
      }
    }
    return remainingAmount;
  }

  /**
   * Update unmapped reverse charges.
   *
   * @param billNo the bill no
   */
  private void updateUnmappedReverseCharges(String billNo) {
    
    BigDecimal zero = BigDecimal.ZERO;
    
    //Get the list of pending unmapped refund references    
    List<RefundReferenceAllocationViewModel> refundRefenceAllocations = 
        allocationRepository.getRefundReferenceAllocations(billNo, new Long(0));
    
    Iterator<RefundReferenceAllocationViewModel> refundReferenceAllocation = 
        refundRefenceAllocations.iterator();
    
    RefundReferenceAllocationViewModel rrav;
    while (refundReferenceAllocation.hasNext()) {
      rrav = refundReferenceAllocation.next();   
      //Get the list of reversed charges with unmapped refund reference id. cancelled first
      List<BillChargeReceiptAllocationModel> reversedUnmappedCharges = allocationRepository
          .getReversedAllocatedAmountForBill(billNo, rrav.getBillReceipt().getBillReceiptId());
      
      BigDecimal refundedAmount = rrav.getAmount().subtract(rrav.getAllocatedAmount());
    
      for (BillChargeReceiptAllocationModel reversedUnmappedCharge : reversedUnmappedCharges) {
        BigDecimal reversedAmount = reversedUnmappedCharge.getAllocatedAmount()
          .multiply(BigDecimal.ONE.negate());
        
        if (refundedAmount.compareTo(zero) <= 0) {
          break;
        }
        //check the difference between refundedAmount and reversedAmount, 
        //map refund_reference_id with billChargeReceiptAllocation row.
        if (refundedAmount.compareTo(reversedAmount) >= 0) {
          refundedAmount = refundedAmount.subtract(reversedAmount);
          updateBillChargeReceipt(reversedUnmappedCharge, rrav.getId(), zero);
          continue;
        }
        //means, reveredAmount < refundedAmount
        reversedAmount = reversedAmount.subtract(refundedAmount);
        
        //Update the left over amount
        updateBillChargeReceipt(reversedUnmappedCharge, new Long(0), 
            reversedAmount);
        
        //Insert a new BillChargeReceiptAllocation row with available refund amount
        createBillChargeReceiptAllocations(reversedUnmappedCharge.getBillCharge(), 
            reversedUnmappedCharge.getBillReceipt(), refundedAmount.negate(), 
            rrav.getId(), reversedUnmappedCharge.getActivity());
        
        refundedAmount = zero;
        
      }    
    }
  }
  
 
  /**
   * Creates the receipt refund reference.
   *
   * @param refundReceipt the refund receipt
   * @param receipt the receipt
   * @param refundedAmount the refunded amount
   */
  private void createReceiptRefundReference(ReceiptModel refundReceipt, 
      String receipt, BigDecimal refundedAmount) {
    ReceiptModel receiptModel = (ReceiptModel) allocationRepository.get(ReceiptModel.class, 
        receipt);
    createReceiptRefundReference(refundReceipt, receiptModel, refundedAmount);
  }
  
  /**
   * Creates the receipt refund reference.
   *
   * @param refundReceipt the refund receipt
   * @param receipt the receipt
   * @param refundedAmount the refunded amount
   * @return the receipt refund reference model
   */
  private ReceiptRefundReferenceModel createReceiptRefundReference(ReceiptModel refundReceipt, 
      ReceiptModel receipt, BigDecimal refundedAmount) {
    ReceiptRefundReferenceModel newReceiptRef = new ReceiptRefundReferenceModel(
        refundReceipt, receipt, 
        refundedAmount);
    allocationRepository.persist(newReceiptRef);
    allocationRepository.flush();    
    return newReceiptRef;
  }
  

  /**
   * Creates the bill charge receipt allocations.
   *
   * @param billChargeId the bill charge id
   * @param billReceiptId the bill receipt id
   * @param refundedAmount the refunded amount
   * @param refundReferenceId the refund reference id
   * @param activity the activity
   */
  private void createBillChargeReceiptAllocations(BillChargeModel billChargeId, 
      BillReceiptsModel billReceiptId, BigDecimal refundedAmount, 
      Long refundReferenceId, String activity) {
    ReceiptRefundReferenceModel refundReference = (ReceiptRefundReferenceModel) allocationRepository.get(
        ReceiptRefundReferenceModel.class, refundReferenceId);
    createBillChargeReceiptAllocations(billChargeId, billReceiptId, refundedAmount, refundReference, activity);
  }
  

  /**
   * Creates the bill charge receipt allocations.
   *
   * @param billChargeId the bill charge id
   * @param billReceipt the bill receipt
   * @param refundedAmount the refunded amount
   * @param refundReferenceId the refund reference id
   * @param activity the activity
   */
  private void createBillChargeReceiptAllocations(BillChargeModel billChargeId, 
      BillReceiptsModel billReceipt, BigDecimal refundedAmount, 
      ReceiptRefundReferenceModel refundReferenceId, String activity) {
    Date now = new Date();
    BillChargeReceiptAllocationModel billChargeRecipt = 
        new BillChargeReceiptAllocationModel(billChargeId, 
            billReceipt, refundedAmount, SYSTEM_USER,
            now, refundReferenceId, activity);
    allocationRepository.persist(billChargeRecipt);
    allocationRepository.flush();    
  }
  
  /**
   * Creates the bill charge receipt allocations.
   *
   * @param billChargeId the bill charge id
   * @param billReceipt the bill receipt
   * @param refundedAmount the refunded amount
   * @param activity the activity
   */
  private void createBillChargeReceiptAllocations(BillChargeModel billChargeId, 
      BillReceiptsModel billReceipt, BigDecimal refundedAmount, String activity) {
    Date now = new Date();    
    BillChargeReceiptAllocationModel billChargeRecipt = 
        new BillChargeReceiptAllocationModel(billChargeId, 
            billReceipt, refundedAmount, SYSTEM_USER, now, activity);
    allocationRepository.persist(billChargeRecipt);
    allocationRepository.flush();
    //If the Receipt is deposit type 
    if (!"RET".equals(billChargeId.getChargeGroup().getChargegroupId()) && 
        "R".equals(billReceipt.getReceiptNo().getReceiptType()) &&
        Boolean.TRUE.equals(billReceipt.getReceiptNo().getIsDeposit())) {
      updateDepositTotalForDiscountAllocations(billReceipt);
    }
  }
  
  
  /**
   * Update deposit total for discount allocations.
   *
   * @param billReceipt the bill receipt
   */
  private void updateDepositTotalForDiscountAllocations(BillReceiptsModel billReceipt) {
    
    String depositType = getDepositType(billReceipt.getReceiptNo().getReceiptId());
    //After billChargeReceipt entry, If amountToBeAllocated is not zero,
    //Then below code will take care.
    BigDecimal amountToBeAllocated = getAmountToBeAllocatedFromDeposit(
        billReceipt.getReceiptNo().getReceiptId(), billReceipt.getBillNo().getBillNo());

    if (amountToBeAllocated.compareTo(BigDecimal.ZERO) != 0) {
      if (amountToBeAllocated.compareTo(BigDecimal.ZERO) < 0) {
        unallocateDepositAmount(billReceipt.getBillNo(), billReceipt.getReceiptNo(), 
            amountToBeAllocated.negate());
      } else {
        allocateDepositAmount(billReceipt.getBillNo(), billReceipt.getReceiptNo(), 
            amountToBeAllocated, depositType);
      }
    }
  }

  /**
   * Creates the bill charge receipt allocations.
   *
   * @param billChargeId the bill charge id
   * @param billReceipt the bill receipt
   * @param refundedAmount the refunded amount
   * @param activity the activity
   * @param claim the claim
   */
  private void createBillChargeReceiptAllocations(BillChargeModel billChargeId, 
      BillReceiptsModel billReceipt, BigDecimal refundedAmount, String activity, InsuranceClaimModel claim) {
    Date now = new Date();
    BillChargeReceiptAllocationModel billChargeRecipt = 
        new BillChargeReceiptAllocationModel(billChargeId, 
            billReceipt, claim, refundedAmount, SYSTEM_USER,
            now, activity);
    allocationRepository.persist(billChargeRecipt);
    allocationRepository.flush();    
  }

  /**
   * Creates refund reference for deposit receipts.
   *
   * @param receiptDtoObject
   *          the receipt dto object
   * @return true, if successful
   */
  @Transactional
  public boolean createDepositRefundReference(Receipt receiptDtoObject) {
    List<ReceiptModel> depositList;
    String mrNo = receiptDtoObject.getMrno();
    BasicDynaBean gprefs = genericPreferencesService.getAllPreferences();
    String enablePatientDepositAvailability = (String) gprefs.get("enable_patient_deposit_availability");
    //If the Deposit Availability needs to show at each center level
    if ("E".equals(enablePatientDepositAvailability)) {
       int centerId = RequestContext.getCenterId();
       if("I".equals(receiptDtoObject.getApplicableToIp())) {
        // If applicable for IP
          depositList = allocationRepository.getIpDepositList(mrNo,centerId, true);
        } else if (receiptDtoObject.getPackageId() != null && receiptDtoObject.getPackageId() > 0) {
        // If refund for package
          int packageId = receiptDtoObject.getPackageId();
          depositList = allocationRepository.getPackageDepositList(mrNo, packageId,centerId, true);
        } else {
        // Consider general deposits
          depositList = allocationRepository.getGeneralDepositList(mrNo,centerId,true);
        }
    } else {
       if("I".equals(receiptDtoObject.getApplicableToIp())) {
        // If applicable for IP
          depositList = allocationRepository.getIpDepositList(mrNo, true);
       } else if (receiptDtoObject.getPackageId() != null && receiptDtoObject.getPackageId() > 0) {
        // If refund for package
          int packageId = receiptDtoObject.getPackageId();
          depositList = allocationRepository.getPackageDepositList(mrNo, packageId, true);
       } else {
        // Consider general deposits
          depositList = allocationRepository.getGeneralDepositList(mrNo,true);
       }
    }
    ReceiptModel refundReceipt = (ReceiptModel) allocationRepository.get(ReceiptModel.class,
        receiptDtoObject.getReceiptNo());
    BigDecimal totalRefund = receiptDtoObject.getAmount().add(receiptDtoObject.getTdsAmt())
        .negate();
    for (ReceiptModel deposit : depositList) {
      BigDecimal receiptAmount = deposit.getAmount().add(deposit.getTdsAmount())
          .add(deposit.getOtherDeductions());
      BigDecimal previouslyRefundedAmount = allocationRepository
          .getRefundedAmount(deposit.getReceiptId());
      receiptAmount = receiptAmount.subtract(previouslyRefundedAmount);
      if (receiptAmount.compareTo(BigDecimal.ZERO) == 0) {
        continue;
      }
      if (totalRefund.compareTo(receiptAmount) <= 0) {
        ReceiptRefundReferenceModel refundReference = new ReceiptRefundReferenceModel(refundReceipt,
            deposit, totalRefund);
        deposit.setUnallocatedAmount(deposit.getUnallocatedAmount().subtract(totalRefund));
        allocationRepository.persist(refundReference);
        return true;
      }
      ReceiptRefundReferenceModel refundReference = new ReceiptRefundReferenceModel(refundReceipt,
          deposit, receiptAmount);
      deposit.setUnallocatedAmount(deposit.getUnallocatedAmount().subtract(receiptAmount));
      allocationRepository.persist(refundReference);

      totalRefund = totalRefund.subtract(receiptAmount);
    }
    return false;
  }

  /**
   * Deposit amount allocation.
   *
   * @param billNo the bill no
   * @param depositType the deposit type
   * @param centerId the center id
   * @return the list
   */
  @Transactional
  private List<BillChargeReceiptAllocationModel> depositAmountAllocation(String billNo,
      String depositType, Integer centerId) {
    BillModel bill = (BillModel) allocationRepository.get(BillModel.class, billNo);
    List<BillChargeReceiptAllocationModel> allocationList = new LinkedList<>();
    if( bill.getVisitId() == null) {
      return allocationList;
    }
    BasicDynaBean gprefs = genericPreferencesService.getAllPreferences();
    String enablePatientDepositAvailability = (String) gprefs.get("enable_patient_deposit_availability");
    String mrNo = bill.getVisitId().getMrNo();
    List<ReceiptModel> depositList;

  //If the Deposit Availability needs to show at each center level
    if ("E".equals(enablePatientDepositAvailability)) {
     switch (depositType) {
        case DepositType.PACKAGE:
          depositList = allocationRepository.getCenterWisePackageDepositList(mrNo,centerId, false);
          break;
        case DepositType.IP:
          depositList = allocationRepository.getIpDepositList(mrNo,centerId, false);
          break;
        default:
          depositList = allocationRepository.getGeneralDepositList(mrNo,centerId, false);
      }
    } else {
       switch (depositType) {
         case DepositType.PACKAGE:
           depositList = allocationRepository.getPackageDepositList(mrNo, false);
           break;
         case DepositType.IP:
           depositList = allocationRepository.getIpDepositList(mrNo, false);
           break;
         default:
           depositList = allocationRepository.getGeneralDepositList(mrNo, false);
      }
    }
    Iterator<ReceiptModel> depositReceiptsItr = depositList.iterator();
    while (depositReceiptsItr.hasNext()) {
      ReceiptModel deposit = depositReceiptsItr.next();    
      BigDecimal amountToBeAllocated = getAmountToBeAllocatedFromDeposit(deposit.getReceiptId(),
          billNo);

      if (amountToBeAllocated.compareTo(BigDecimal.ZERO) == 0) {
        continue;
      }
      if (amountToBeAllocated.compareTo(BigDecimal.ZERO) < 0) {
        // Unallocated the additional amount.
        // !!!!Careful the amount to be allocated is negative so negate it before calling
        // an unallocated method.
        unallocateDepositAmount(bill, deposit, amountToBeAllocated.negate());
      } else {
        allocationList
            .addAll(allocateDepositAmount(bill, deposit, amountToBeAllocated, depositType, 
            		(!depositReceiptsItr.hasNext()) ? true : false));
      }
    }
    return allocationList;
  }

  /**
   * Gets the amount to be allocated from deposit.
   *
   * @param depositId the deposit id
   * @param billNo the bill no
   * @return the amount to be allocated from deposit
   */
  private BigDecimal getAmountToBeAllocatedFromDeposit(String depositId, String billNo) {
    BillReceiptsModel billReceipt = allocationRepository.getBillReceipt(depositId, billNo);
    if (null == billReceipt) {
      return BigDecimal.ZERO;
    }
    BigDecimal allocatedAmount = billReceipt.getAllocatedAmount();
    BigDecimal actuallyAllocatedAmount = allocationRepository
        .calculateAllocatedAmount(billReceipt.getBillReceiptId());
    return allocatedAmount.subtract(actuallyAllocatedAmount);
  }

  /**
   * Unallocate deposit amount.
   *
   * @param bill the bill
   * @param deposit the deposit
   * @param reductionAmount the reduction amount
   */
  private void unallocateDepositAmount(BillModel bill, ReceiptModel deposit,
      BigDecimal reductionAmount) {
    String depositId = deposit.getReceiptId();
    String billNo = bill.getBillNo();
    BigDecimal amountUnallocated = BigDecimal.ZERO;
    List<Map<String, Object>> allocations = allocationRepository
        .getAllocationsOfDeposit(depositId, billNo);
    for (Map<String, Object> allocation : allocations) {
      BigDecimal allocationAmount = new BigDecimal(allocation.get("allocatedAmount").toString());
      BillChargeReceiptAllocationModel newAllocation;
      BillChargeModel billCharge = (BillChargeModel) allocationRepository.get(BillChargeModel.class, 
          allocation.get("chargeId").toString());
      BillReceiptsModel billReceipt = (BillReceiptsModel) allocationRepository.get(
          BillReceiptsModel.class, Long.parseLong(allocation.get("billReceiptId").toString()));
      BigDecimal minAmount = allocationAmount.min(reductionAmount);

      newAllocation = new BillChargeReceiptAllocationModel(billCharge, billReceipt, 
          minAmount.negate(), SYSTEM_USER, new Date(), "a");
      allocationRepository.persist(newAllocation);

      reductionAmount = reductionAmount.subtract(minAmount);
      amountUnallocated = amountUnallocated.add(minAmount);
      if (reductionAmount.compareTo(BigDecimal.ZERO) == 0) {
        break;
      }
    }
  }

  /**
   * Allocate deposit amount.
   *
   * @param bill the bill
   * @param deposit the deposit
   * @param amountToBeAllocated the amount to be allocated
   * @param depositType the deposit type
   * @return the list
   */
  private List<BillChargeReceiptAllocationModel> allocateDepositAmount(BillModel bill,
      ReceiptModel deposit, BigDecimal amountToBeAllocated, String depositType) {
    return allocateDepositAmount(bill, deposit, amountToBeAllocated, depositType, false);
  }

  /**
   * Allocate deposit amount.
   *
   * @param bill the bill
   * @param deposit the deposit
   * @param amountToBeAllocated the amount to be allocated
   * @param depositType the deposit type
   * @param isLastDepositReceipt is last deposit receipt
   * @return the list
   */
  private List<BillChargeReceiptAllocationModel> allocateDepositAmount(BillModel bill,
      ReceiptModel deposit, BigDecimal amountToBeAllocated, String depositType, Boolean isLastDepositReceipt) {

    List<BillChargeReceiptAllocationModel> allocationList = new LinkedList<>();
    String billNo = bill.getBillNo();
    BillReceiptsModel billReceipt = allocationRepository.getBillReceipt(deposit.getReceiptId(),
        billNo);
    if (!checkBillLevelRestrictions(billNo, deposit)) {
      // Undo the set off in case the bill level restriction fails.
      reduceDepositSetoffAmount(billReceipt, amountToBeAllocated, depositType);
      return allocationList;
    }
    BigDecimal chargeAmount = ZERO; 
    BigDecimal lessPkgChargeAmount = ZERO;
    // Get the unallocated billCharges
    List<BillChargeModel> newUnallocatedCharges = allocationRepository
        .getUnallocatedDiscountCharges(billNo, Boolean.TRUE);
    
    Date now = new Date();
    BigDecimal discountedPatientAmount = ZERO;
    if (!newUnallocatedCharges.isEmpty() && isLastDepositReceipt) {
      BigDecimal insuranceAmount = ZERO; 
      for (BillChargeModel newUnallocatedCharge : newUnallocatedCharges) {
        String chargeGroup = newUnallocatedCharge.getChargeGroup().getChargegroupId();

        if (newUnallocatedCharge.getAmount().compareTo(ZERO) < 0) {
          chargeAmount = newUnallocatedCharge.getAmount()
              .add(newUnallocatedCharge.getTaxAmt()).negate();
          insuranceAmount = newUnallocatedCharge.getInsuranceClaimAmount()
              .add(newUnallocatedCharge.getSponsorTaxAmt()).negate();
          discountedPatientAmount = discountedPatientAmount
                .add(chargeAmount.subtract(insuranceAmount));
          //This is hack to solve HMS-38010, 
          //here, package margin to be allocated with negative amount
          if ("PKG".equals(chargeGroup)) {
            lessPkgChargeAmount = chargeAmount.subtract(insuranceAmount).negate();
            BillChargeReceiptAllocationModel allocation = new BillChargeReceiptAllocationModel(
                newUnallocatedCharge, billReceipt, lessPkgChargeAmount, SYSTEM_USER, now, "a");
            allocationRepository.persist(allocation);            
          }
        }
      }
      amountToBeAllocated = amountToBeAllocated.add(discountedPatientAmount);
    }
    
    BigDecimal totalAllocatedAmount = BigDecimal.ZERO;
    List<BillChargeModel> billCharges = getBillChargeList(billNo);

    for (BillChargeModel charge : billCharges) {

      if (amountToBeAllocated.compareTo(BigDecimal.ZERO) == 0) {
        break;
      }

      BigDecimal diff = compareChargeAmountToAllocatedAmount(charge, true);
      if (diff.compareTo(BigDecimal.ZERO) == 0) {
        continue;
      }

      if (checkChargeLevelRestrictions(charge, deposit)) {
        chargeAmount = diff;
        BigDecimal minAmount = chargeAmount.min(amountToBeAllocated);
        // Allocating the deposit amount
        BillChargeReceiptAllocationModel allocation = new BillChargeReceiptAllocationModel(charge,
            billReceipt, minAmount, SYSTEM_USER, now, "a");
        allocationRepository.persist(allocation);
        allocationList.add(allocation);
        totalAllocatedAmount = totalAllocatedAmount.add(minAmount);
        amountToBeAllocated = amountToBeAllocated.subtract(minAmount);
      }
    }
    // Update allocated Amount if the entire amount could not be allocated. And give it back to
    // deposit receipt.
    //Substract the discountedAmount from amountToBeAllocated.
    amountToBeAllocated = (amountToBeAllocated.compareTo(ZERO) > 0) ? 
        amountToBeAllocated.subtract(discountedPatientAmount) : amountToBeAllocated;
    if (amountToBeAllocated.compareTo(BigDecimal.ZERO) != 0) {
      reduceDepositSetoffAmount(billReceipt, amountToBeAllocated, depositType);
    }
    updateBillTotal(billNo);
    allocationRepository.flush();
    return allocationList;
  }

  /**
   * Reduce deposit setoff amount.
   *
   * @param billReceipt the bill receipt
   * @param reductionAmount the reduction amount
   * @param depositType the deposit type
   */
  private void reduceDepositSetoffAmount(BillReceiptsModel billReceipt, BigDecimal reductionAmount,
      String depositType) {
    BigDecimal billReceiptAllocatedAmount = billReceipt.getAllocatedAmount();
    //Here deposit amount is less than the reductionAmount.
    if (billReceiptAllocatedAmount.compareTo(reductionAmount) < 0) {
      return;
    }
    billReceiptAllocatedAmount = billReceiptAllocatedAmount.subtract(reductionAmount);
    billReceipt.setAllocatedAmount(billReceiptAllocatedAmount);
    billReceipt.setModTime(new Date());
    BillModel bill = billReceipt.getBillNo();
    ReceiptModel deposit = billReceipt.getReceiptNo();
    // update deposits unallocated amount
    BigDecimal unallocatedAmount = deposit.getUnallocatedAmount();
    deposit.setUnallocatedAmount(unallocatedAmount.add(reductionAmount));

    // Update bill set_off_amount
    BigDecimal depositSetOff = bill.getDepositSetOff();    
    
    depositSetOff = (depositSetOff.compareTo(reductionAmount) >= 0) ? 
        depositSetOff.subtract(reductionAmount) : reductionAmount;
    bill.setDepositSetOff(depositSetOff);
    // if deposit type is ip then reduce from ip_deposit_setoff also
    BigDecimal ipDepositSetOff = bill.getIpDepositSetOff();
    if(depositType.equals(DepositType.IP)) {
      ipDepositSetOff = (ipDepositSetOff.compareTo(reductionAmount) >= 0) ? 
          ipDepositSetOff.subtract(reductionAmount) : reductionAmount;    
      bill.setIpDepositSetOff(ipDepositSetOff);
    }

    String mrNo = bill.getVisitId().getMrNo();
    String depositFor = depositType.equals(DepositType.IP) ? "I" : "B";
    boolean isMultiPackage = depositType.equals(DepositType.PACKAGE);

    // Setoff refund - so should negate
    allocationRepository.persist(new PatientDepositsSetoffAdjustmentsModel(mrNo, bill.getBillNo(),
        reductionAmount.negate(), depositFor, isMultiPackage));
    updateBillTotal(bill.getBillNo());

  }
  
  /**
   * Split the reduction in the deposit setoff amount across the deposits.
   *
   * @param billNo
   *          the bill no
   * @param depositType
   *          the deposit type
   * @param amount
   *          the positive reduction amount
   */
  @Transactional
  public void reduceDepositSetoffAmount(String billNo, String depositType, BigDecimal amount) {
    BillModel bill = (BillModel) allocationRepository.get(BillModel.class, billNo);
    
    BigDecimal previouslySetOffAmount = bill.getDepositSetOff();
    BigDecimal previouslySetOffIpAmount = bill.getIpDepositSetOff();
    
    List<BillReceiptsModel> billReceiptList;
    String depositFor = "B";
    boolean isMultiVisitPackage = false;

    switch (depositType) {
      case DepositType.PACKAGE:
        billReceiptList = allocationRepository.getPackageDepositBillReceipts(billNo);
        isMultiVisitPackage = true;
        break;
      case DepositType.IP:
        billReceiptList = allocationRepository.getIpDepositBillReceipts(billNo);
        depositFor = "I";
        break;
      default:
        billReceiptList = allocationRepository.getGeneralDepositBillReceipts(billNo);
    }
    //data list for accounting
    List<Map<String, Object>> receiptsDataList = new ArrayList<>();
    BigDecimal totalDepositAmountReduced = BigDecimal.ZERO;
    for (BillReceiptsModel billReceipt : billReceiptList) {
      ReceiptModel deposit = billReceipt.getReceiptNo();
      BigDecimal unallocatedAmount = deposit.getUnallocatedAmount();
      BigDecimal availableToRefund = calculateTotalReceiptAmount(deposit).subtract(unallocatedAmount);

      BigDecimal minAmount = amount.min(availableToRefund);
      // Should negate the minAmount as we are reducing the allocated amount.
      setOffDepositAmount(deposit, bill, minAmount.negate());

      // get data related to setoff refund for accounting schedule
      Map<String, Object> receiptData = new HashMap<>();
      receiptData.put("receiptId", deposit.getReceiptId());
      receiptData.put("reversalsOnly", Boolean.FALSE);
      receiptData.put("setOffBillNo", billNo);
      receiptData.put("setOffType", "F");
      receiptsDataList.add(receiptData);

      amount = amount.subtract(minAmount);
      totalDepositAmountReduced = totalDepositAmountReduced.add(minAmount);
      if (amount.compareTo(BigDecimal.ZERO) == 0) {
        break;
      }
    }
    // Schedule the accounting for deposit setoff refund receipt
    accountingJobScheduler.scheduleAccountingForReceiptsList(receiptsDataList);
    bill.setDepositSetOff(previouslySetOffAmount.subtract(totalDepositAmountReduced));
    if (depositType.equals(DepositType.IP)) {
      bill.setIpDepositSetOff(previouslySetOffIpAmount.subtract(totalDepositAmountReduced));
    }
    
    String mrNo = bill.getVisitId().getMrNo();
    // Setoff refund - so should negate
    allocationRepository.persist(new PatientDepositsSetoffAdjustmentsModel(mrNo, billNo,
        totalDepositAmountReduced.negate(), depositFor, isMultiVisitPackage));
  }

  /**
   * Unallocate package charges.
   *
   * @param bill the bill
   * @param packageId the package id
   */
  private void unallocatePackageCharges(BillModel bill, String packageId) {
    Set<BillChargeModel> billCharges = bill.getBillCharges();
    Date now = new Date();
    for (BillChargeModel charge : billCharges) {
      List<Map<String, Object>> allocations = allocationRepository
          .getAllocationsForCharge(charge.getChargeId());
      for (Map<String, Object> allocation : allocations) {
        long billReceiptId = (long) allocation.get("billReceiptId");
        BigDecimal amount = (BigDecimal) allocation.get("allocatedAmount");
        if (amount.compareTo(BigDecimal.ZERO) == 0) {
          continue;
        }
        allocationRepository.persist(new BillChargeReceiptAllocationModel(charge,
            new BillReceiptsModel(billReceiptId), amount.negate(), SYSTEM_USER, now, "a"));
      }
    }
  }

  /**
   * Calculate bill total.
   *
   * @param billNo the bill no
   * @return the big decimal
   */
  @Transactional
  public BigDecimal calculateBillTotal(String billNo) {
    boolean isModInsExtEnabled = modulesActivatedService.isModuleActivated("mod_ins_ext");
    return allocationRepository.calculateTotalReceiptAmount(billNo, isModInsExtEnabled);
  }

  /**
   * Update bill total. Method to replace bill_all_receipts_totals_trigger trigger on bill_receipts
   * table.
   *
   * @param billNo
   *          the bill no
   */
  @Transactional
  public void updateBillTotal(String billNo) {
    allocationRepository.flush();
    BillModel bill = (BillModel) allocationRepository.get(BillModel.class, billNo);
    bill.setPrimaryNoOfSponsorReceipts(
        (int) allocationRepository.getSponsorReceiptCount(billNo, true));
    bill.setSecondaryNoOfSponsor((int) allocationRepository.getSponsorReceiptCount(billNo, false));
    // I have no clue why we do this.
    bill.setLastSponsorReceiptNo(allocationRepository.getLatestReceiptId(billNo));
    bill.setLastReceiptNo(allocationRepository.getLatestReceiptId(billNo));
    bill.setNoOfReceipts((int) allocationRepository.getPatientReceiptCount(billNo));
    bill.setTotalReceipts(calculateBillTotal(billNo));
    bill.setPrimaryTotalSponsorReceipts(allocationRepository.getSponsorAmount(billNo, true));
    bill.setSecondaryTotalSponsorReceipts(allocationRepository.getSponsorAmount(billNo, false));
  }
  

  /**
   * Unlock bill.
   *
   * @param billNo the bill no
   */
  @Transactional
  public void unlockBill(String billNo) {
    allocationRepository.flush();
    BillModel bill = (BillModel) allocationRepository.get(BillModel.class, billNo);
    bill.setLocked(Boolean.FALSE);
  }

  /**
   * Split deposit amount to set off. This Function splits the amount sent in the parameter across
   * multiple deposit receipts.
   *
   * @param billNo
   *          the bill no
   * @param depositType
   *          the deposit type
   * @param amount
   *          the amount to be split across deposit receipts
   */
  @Transactional
  public void splitDepositAmountToSetOff(String billNo, String depositType, BigDecimal amount) {
    BillModel bill = (BillModel) allocationRepository.get(BillModel.class, billNo);
    BigDecimal avblCashLimit = BigDecimal.ZERO;
    String mrNo = bill.getVisitId().getMrNo();
    BigDecimal previouslySetOffAmount = bill.getDepositSetOff();
    BigDecimal previouslySetOffIpAmount = bill.getIpDepositSetOff();
    String visitId = bill.getVisitId().getPatientId();
    BasicDynaBean gprefs = genericPreferencesService.getAllPreferences();
    String incomeTaxApplicability = (String) gprefs.get("income_tax_cash_limit_applicability");
    if ("Y".equals(incomeTaxApplicability)) {
      BasicDynaBean avblLimitBean = receiptService.getCashLimit(mrNo,visitId);
      if (null != avblLimitBean) {
         avblCashLimit = (BigDecimal) avblLimitBean.get("avbl_cash_limit");
      }
    }
    String enablePatientDepositAvailability = (String) gprefs.get("enable_patient_deposit_availability");
    List<ReceiptModel> depositList;
    String depositFor = "B";
    boolean isMultiVisitPackage = false;
  //If the Deposit Availability needs to show at each center level
    if ("E".equals(enablePatientDepositAvailability)) {
      int centerId=0;
      if(null != RequestContext.getCenterId()) {
       centerId = RequestContext.getCenterId();
      }
       switch (depositType) {
        case DepositType.PACKAGE:
          depositList = allocationRepository.getCenterWisePackageDepositList(mrNo,centerId, false);
          isMultiVisitPackage = true;
          break;
        case DepositType.IP:
          depositList = allocationRepository.getIpDepositList(mrNo,centerId, false);
          depositFor = "I";
          break;
        default:
          depositList = allocationRepository.getGeneralDepositList(mrNo,centerId, false);
       }
     } else {
       switch (depositType) {
        case DepositType.PACKAGE:
          depositList = allocationRepository.getPackageDepositList(mrNo, false);
          isMultiVisitPackage = true;
          break;
        case DepositType.IP:
          depositList = allocationRepository.getIpDepositList(mrNo, false);
          depositFor = "I";
          break;
        default:
          depositList = allocationRepository.getGeneralDepositList(mrNo, false);
       }
    }
    //data list for accounting
    List<Map<String, Object>> receiptsDataList = new ArrayList<>();
    BigDecimal totalDepositSetoff = BigDecimal.ZERO;
    for (ReceiptModel deposit : depositList) {
      BigDecimal unallocatedAmount = deposit.getUnallocatedAmount();
      if (unallocatedAmount.compareTo(BigDecimal.ZERO) == 0 || (!checkBillLevelRestrictions(billNo, deposit))) {
        continue;
      }
			PaymentModeMasterModel payModeModel = deposit.getPaymentModeId();
			int id = payModeModel.getModeId();
			BigDecimal minAmount = BigDecimal.ZERO;
			minAmount = amount.min(unallocatedAmount);
			if ("Y".equals(incomeTaxApplicability) && id == -1 && avblCashLimit != BigDecimal.ZERO) {
				if (avblCashLimit.compareTo(BigDecimal.ZERO) > 0) {
					minAmount = minAmount.min(avblCashLimit);
				} else {
					continue;
				}
			}
			setOffDepositAmount(deposit, bill, minAmount);

	 // get data related to deposit setoff for accounting schedule
      Map<String, Object> receiptData = new HashMap<>();
      receiptData.put("receiptId", deposit.getReceiptId());
      receiptData.put("reversalsOnly", Boolean.FALSE);
      receiptData.put("setOffBillNo", billNo);
      receiptData.put("setOffType", "R");
      receiptsDataList.add(receiptData);

      amount = amount.subtract(minAmount);
      totalDepositSetoff = totalDepositSetoff.add(minAmount);
      if (amount.compareTo(BigDecimal.ZERO) == 0) {
        break;
      }
    }
    // Schedule the accounting for deposit setoff receipt
    accountingJobScheduler.scheduleAccountingForReceiptsList(receiptsDataList);

    bill.setDepositSetOff(previouslySetOffAmount.add(totalDepositSetoff));
    if (depositType.equals(DepositType.IP)) {
      bill.setIpDepositSetOff(previouslySetOffIpAmount.add(totalDepositSetoff));
    }
    allocationRepository.persist(new PatientDepositsSetoffAdjustmentsModel(mrNo, billNo,
        totalDepositSetoff, depositFor, isMultiVisitPackage));
  }

  /**
   * Sets the off deposit amount.
   *
   * @param deposit the deposit
   * @param bill the bill
   * @param allocatedAmount the allocated amount
   */
  private void setOffDepositAmount(ReceiptModel deposit, BillModel bill,
      BigDecimal allocatedAmount) {
    Date now = new Date();
    BigDecimal unallocatedAmount = deposit.getUnallocatedAmount();
    BillReceiptsModel billReceipt = allocationRepository.getBillReceipt(deposit.getReceiptId(),
        bill.getBillNo());
    
    // Update unallocated amount of the receipt.
    unallocatedAmount = unallocatedAmount.subtract(allocatedAmount);
    deposit.setUnallocatedAmount(unallocatedAmount);
    
    // Then update allocated amount in bill_receipts
    if (null == billReceipt) {
      billReceipt = new BillReceiptsModel(deposit, bill, now);
      billReceipt.setUsername(SYSTEM_USER);
      billReceipt.setAllocatedAmount(allocatedAmount);
      billReceipt.setModTime(new Date());
      allocationRepository.persist(billReceipt);
      allocationRepository.flush();
      return;
    }
    
    allocatedAmount = billReceipt.getAllocatedAmount().add(allocatedAmount);
    billReceipt.setAllocatedAmount(allocatedAmount);
    billReceipt.setModTime(new Date());
    allocationRepository.flush();
  }

  /**
   * Schedule bill charge allocation job for the bill.
   *
   * @param eventId
   *          the event id
   * @param userName
   *          the user name
   * @param billNo
   *          the bill no
   */
  public void scheduleBillChargeAllocation(String eventId, String userName, String billNo) {

    Map<String, Object> jobData = new HashMap<>();
    jobData.put("bill_no", billNo);
    jobData.put("userName", userName);
    jobData.put("schema", RequestContext.getSchema());
    jobData.put("eventId", eventId);
    jobData.put("centerId",RequestContext.getCenterId());

    String jobMessage = "BillChargeAllocation_" + billNo;

    jobService.scheduleImmediate(buildJob(jobMessage, BillChargeAllocationJob.class, jobData));
  }

  /**
   * Reset all writeoff receipts amount to zero. This is called when a written off bill is reopened.
   *
   * @param billNo
   *          the bill no
   */
  @Transactional
  public void resetWriteoffAmount(String billNo) {
    List<ReceiptModel> writeOffReceipts = allocationRepository
        .getWriteOffReceipt(billNo);
    Date now = new Date();
    String userId = (String) sessionService.getSessionAttributes().get("userId");
    List<Map<String, Object>> receiptsDataList = new ArrayList<>();
    for (ReceiptModel receipt : writeOffReceipts) {
      // Create the refund Receipt
      UUserModel user = new UUserModel(userId);
      ReceiptModel refundReceipt = new ReceiptModel(
          allocationRepository.getNextWriteoffRefundReceiptId(), "F", receipt.getAmount().negate(),
          user);
      refundReceipt.setTpaId(receipt.getTpaId());
      refundReceipt.setMrNo(receipt.getMrNo());
      refundReceipt.setPaymentModeId(receipt.getPaymentModeId());
      refundReceipt.setIsSettlement(true);
      refundReceipt.setIsDeposit(false);
      refundReceipt.setDisplayDate(now);
      refundReceipt.setCreatedAt(now);
      refundReceipt.setModifiedAt(now);
      refundReceipt.setModifiedBy(new UUserModel(userId));
      allocationRepository.persist(refundReceipt);

      // Create the refund reference for the writeoff
      ReceiptRefundReferenceModel refundReference = new ReceiptRefundReferenceModel(refundReceipt,
          receipt, receipt.getAmount());
      allocationRepository.persist(refundReference);

      // Setting the unallocated amount of the writeoff receipt to 0
      receipt.setUnallocatedAmount(BigDecimal.ZERO);
      // send the receipt for accounting
      Map<String, Object> receiptData = new HashMap<>();
      receiptData.put("receiptId", refundReceipt.getReceiptId());
      receiptData.put("reversalsOnly", Boolean.FALSE);
      receiptsDataList.add(receiptData);
    }
    // schedule accounting for writtenoff refund receipts
    accountingJobScheduler.scheduleAccountingForReceiptsList(receiptsDataList);
  }

}
