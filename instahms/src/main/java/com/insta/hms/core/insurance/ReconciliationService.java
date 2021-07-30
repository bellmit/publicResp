package com.insta.hms.core.insurance;

import com.bob.hms.common.Preferences;
import com.bob.hms.common.RequestContext;
import com.insta.hms.billing.BillConstants;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.security.SecurityService;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.core.billing.AllocationRepository;
import com.insta.hms.core.billing.AllocationService;
import com.insta.hms.core.billing.BillChargeClaimModel;
import com.insta.hms.core.billing.BillChargeModel;
import com.insta.hms.core.billing.BillChargeReceiptAllocationModel;
import com.insta.hms.core.billing.BillModel;
import com.insta.hms.core.billing.BillReceiptsModel;
import com.insta.hms.core.billing.BillService;
import com.insta.hms.core.billing.ReceiptModel;
import com.insta.hms.core.billing.ReceiptUsageModel;
import com.insta.hms.exception.HMSException;
import com.insta.hms.exception.ValidationException;
import com.insta.hms.integration.insurance.remittance.ReconciliationActivityDetailsModel;
import com.insta.hms.integration.insurance.remittance.ReconciliationModel;
import com.insta.hms.mdm.paymentmode.PaymentModeMasterModel;
import com.insta.hms.mdm.tpas.TpaMasterModel;
import com.insta.hms.mdm.usercentercounters.UserBillingCenterCounterMappingService;
import com.insta.hms.security.usermanager.UUserModel;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ReconciliationService {

  /**
   * The logger.
   */
  static Logger logger = LoggerFactory.getLogger(ReconciliationService.class);

  /**
   * The session service.
   */
  @LazyAutowired
  private SessionService sessionService;

  @LazyAutowired
  private SecurityService securityService;

  @LazyAutowired
  private UserBillingCenterCounterMappingService counterMappingService;

  /**
   * Hbm test.
   */
  @LazyAutowired
  private ReconciliationRepository sponsorReceiptRepository;

  @LazyAutowired
  private AllocationService allocationService;

  @LazyAutowired
  private AllocationRepository allocationRepository;

  public static final String DEFAULT_PREFIX = "A-";

  public static final String PHARMACY_PREFIX = "P-";

  @LazyAutowired
  private BillService billService;


  private static final String CSV_MAPPING_BILL_NO = "bill_no_mapping";
  private static final String CSV_MAPPING_ACTIVITY_ID = "activity_id_mapping";
  private static final String CSV_MAPPING_REMITTANCE_AMOUNT = "remittance_amount_mapping";
  private static final String CSV_MAPPING_REMARKS = "remarks_mapping";

  private static final String CSV_TYPE_ACTIVITY_LEVEL = "activity_level";
  private static final String CSV_TYPE_BILL_LEVEL = "bill_level";

  /**
   * Adds the sponsor receipt.
   *
   * @param sponsorReceipt the sponsor receipt
   * @return the remittanceId
   */
  @Transactional
  public int addSponsorReceipt(ReceiptModel sponsorReceipt) {
    String userId = (String) sessionService.getSessionAttributes().get("userId");
    UUserModel user = new UUserModel();
    user.setEmpUsername(userId);
    Date now = new Date();

    BasicDynaBean mappedCounterBean =
        counterMappingService.getMappedCounterForCenter(userId, sponsorReceipt.getCenterId());
    sponsorReceipt.setCounter(
        mappedCounterBean != null ? (String) mappedCounterBean.get("counter_id") : null);
    String sponsorReceiptId = sponsorReceiptRepository.generateNextId();
    sponsorReceipt.setReceiptId(sponsorReceiptId);
    sponsorReceipt.setCreatedBy(user);
    sponsorReceipt.setCreatedAt(now);
    sponsorReceipt.setModifiedAt(now);
    sponsorReceipt.setModifiedBy(user);

    // Can be skipped if we change the cascade to cascade.persist in the model.
    ReconciliationModel reconciliationModel = new ReconciliationModel();

    sponsorReceiptRepository.persist(sponsorReceipt);
    reconciliationModel.setReceiptId(sponsorReceipt);
    reconciliationModel.setAllocatedAmount(BigDecimal.ZERO);
    reconciliationModel.setCreatedBy(user);
    reconciliationModel.setModifiedBy(user);
    reconciliationModel.setCreatedAt(now);
    reconciliationModel.setModifiedAt(now);
    int reconciliationId = (int) sponsorReceiptRepository.save(reconciliationModel);
    return reconciliationId;
  }

  /**
   * Gets the sponsor receipt details.
   *
   * @param reconciliationId the reconciliation id
   * @return the sponsor receipt details
   */
  @Transactional(readOnly = true)
  public ReconciliationModel getSponsorReceiptDetails(int reconciliationId) {
    ReconciliationModel reconciliation = null;
    if (reconciliationId > 0) {
      reconciliation = sponsorReceiptRepository.getReconciliationDetails(reconciliationId);
      ReceiptModel receipt = reconciliation.getReceiptId();
      reconciliation.getReconciliationId();
      Hibernate.initialize(receipt.getTpaId());
      return reconciliation;
    }
    return null;
  }


  /**
   * Gets the sponsor receipt amount details.
   *
   * @param reconciliationId the reconciliation id
   * @return the sponsor receipt amount details
   */
  @Transactional(readOnly = true)
  public Object getSponsorReceiptAmountDetails(int reconciliationId) {
    Map<String, Object> resultMap = new HashMap<>();
    if (reconciliationId == 0) {
      return resultMap;
    }
    Object[] amount = sponsorReceiptRepository.getSponsorReceiptAmountDetails(reconciliationId);
    BigDecimal draftedAmount = sponsorReceiptRepository.getDraftedAmount(reconciliationId);
    if (null == draftedAmount) {
      draftedAmount = new BigDecimal(0);
    }
    BigDecimal receiptAmount = (BigDecimal) amount[0];
    BigDecimal tdsAmount = (BigDecimal) amount[1];
    BigDecimal otherDeductions = (BigDecimal) amount[2];
    BigDecimal allocatedAmount = (BigDecimal) amount[3];
    BigDecimal totalAmount = receiptAmount.add(tdsAmount).add(otherDeductions);
    BigDecimal unallocatedAmount = totalAmount.subtract(allocatedAmount);
    resultMap.put("total_amount", totalAmount);
    resultMap.put("receipt_amount", receiptAmount);
    resultMap.put("allocated_amount", allocatedAmount);
    resultMap.put("tds_amount", tdsAmount);
    resultMap.put("drafted_amount", draftedAmount);
    resultMap.put("other_deductions", otherDeductions);
    resultMap.put("unallocated_amount", unallocatedAmount);
    return resultMap;
  }

  /**
   * Gets the bills of the submission batch with pending due amount.
   *
   * @param submissionBatchId the submission batch id
   * @return the bills from batch
   */
  @Transactional
  public Object getBillsFromBatch(List<String> submissionBatchId) {
    List<Map<String, Object>> billList = sponsorReceiptRepository
        .getBillsFromBatch(submissionBatchId);
    List<Map<String, Object>> filteredBillList = new ArrayList<>();
    if (!billList.isEmpty()) {
      // Filter the bills with due amount less than zero.
      for (Map<String, Object> bill : billList) {
        BigDecimal dueAmount = (BigDecimal) bill.get("due");
        if (BigDecimal.ZERO.compareTo(dueAmount) < 0) {
          filteredBillList.add(bill);
        }
      }
    }
    return filteredBillList;
  }

  /**
   * Gets the batches by tpa.
   *
   * @param tpaId the tpa id
   * @return the batches by tpa
   */
  @Transactional
  public List<Map<String, Object>> getBatchesByTpa(String tpaId, Integer centerId) {
    List batchList = sponsorReceiptRepository.getBatchesByTpa(tpaId, centerId);
    List<Map<String, Object>> formattedList = new LinkedList<Map<String, Object>>();
    for (Object batch : batchList) {
      Object[] contents = (Object[]) batch;
      Map<String, Object> batchMap = new HashMap<String, Object>();
      batchMap.put("submission_batch_id", contents[0]);
      batchMap.put("sponsor_due", contents[1]);
      batchMap.put("submission_date", contents[2]);
      batchMap.put("status", contents[3]);
      formattedList.add(batchMap);
    }
    return formattedList;
  }

  /**
   * Parses the bill json and saves.
   *
   * @param reconciliationId the reconciliation id
   * @param bills            the bills
   * @param isDraft          the is draft
   * @return the map
   */
  @Transactional
  public Map<String, Object> parseAndSave(int reconciliationId,
      Map<String, Map<String, Object>> bills,
      boolean isDraft) {
    Set<String> billKeys = bills.keySet();
    Set<String> billNos = bills.values().stream().map(bill -> (String) bill.get("bill_no"))
        .collect(Collectors.toSet());
    Map<String, Object> resMap = new HashMap<>();
    if (null != billNos && !billNos.isEmpty()) {
      sponsorReceiptRepository.deleteRemovedBillRemittances(reconciliationId, billNos.toArray());
    }
    Map<String, Object> allottedAmountAndActivityIds = new HashMap<>();
    Set<String> allocatedActivityIds = new HashSet<>();
    for (String billKey : billKeys) {
      Map remittanceDetails = (Map<String, Object>) bills.get(billKey);
      String billNo = (String) remittanceDetails.get("bill_no");
      String claimId = (String) remittanceDetails.get("claim_id");
      String mrNo = (String) remittanceDetails.get("mr_no");
      BigDecimal amount = new BigDecimal(
          String.valueOf(remittanceDetails.get("remittance_amount")));
      boolean autoAllocate = (boolean) remittanceDetails.get("auto_allocate");
      String remarks = (String) remittanceDetails.get("remarks");
      boolean writeOffFlag = ("Y".equals((String) remittanceDetails.get("writeOffRemaining")));
      Map<String, Object> allocations = (Map<String, Object>) remittanceDetails.get("allocation");

      if (isDraft) {
        allottedAmountAndActivityIds =
            saveAsDraft(reconciliationId, claimId, billNo, amount, autoAllocate, allocations);
      } else {
        allottedAmountAndActivityIds = 
            saveRemittance(reconciliationId, claimId, billNo, amount, autoAllocate, allocations);
        if (writeOffFlag) {
          createSponsorWriteOffReceipt(billNo, claimId, reconciliationId);
          Map modulesActivatedMap =
              ((Preferences) RequestContext.getSession().getAttribute("preferences"))
                  .getModulesActivatedMap();
          String ipModAct = "N";
          if ((modulesActivatedMap != null)
              && (modulesActivatedMap.get("mod_pharmacy") != null)) {
            ipModAct = (String) modulesActivatedMap.get("mod_pharmacy");
            if (ipModAct == null || ipModAct.equals("")) {
              ipModAct = "N";
            }
          }
          billService.updateSpnsrWriteOffStatus(billNo, remarks);
          billService.closeBillAutoOnWriteOffApproval(billNo, ipModAct, mrNo);

        }
      }
      updateBillRemittance(billNo, reconciliationId, remarks, writeOffFlag, isDraft);
      resMap.put(billNo,allottedAmountAndActivityIds.get("totalAllottedAmount"));
      allocatedActivityIds.addAll(
          (Set<String>) allottedAmountAndActivityIds.get("allocatedActivityIds"));
    }
    sponsorReceiptRepository.deleteRemovedDraft(reconciliationId, allocatedActivityIds);
    return resMap;
  }

  private boolean checkUserRights(String screenId) {
    Map<String, String> urlRightsMap = (Map<String, String>) securityService.getSecurityAttributes()
        .get("urlRightsMap");
    return "A".equals(urlRightsMap.get(screenId));
  }

  // TODO: Better move this to receiptService
  private void createSponsorWriteOffReceipt(String billNo, String claimId, int remittanceId) {
    String userId = (String) sessionService.getSessionAttributes().get("userId");
    if (!checkUserRights("sponsor_writeoff_approval")) {
      String errorMessage = "User not authorized to create a sponsor writeoff";
      throw new HMSException(HttpStatus.FORBIDDEN, errorMessage, new String[] {userId});
    }
    BillModel bill = (BillModel) sponsorReceiptRepository.get(BillModel.class, billNo);
    // Check if the bill is closed or finalised first.
    if (bill.getStatus() != 'C' && bill.getStatus() != 'F') {
      String errorMessage = "Bill " + billNo + " not closed and thus cannot be written off";
      throw new HMSException(HttpStatus.BAD_REQUEST, errorMessage, new String[] {billNo});
    }
    ReconciliationModel reconciliationDetails = sponsorReceiptRepository
        .getReconciliationDetails(remittanceId);
    TpaMasterModel tpa = reconciliationDetails.getReceiptId().getTpaId();

    // Create and insert write off receipt.
    ReceiptModel writeOffReceipt = new ReceiptModel();
    String receiptId = sponsorReceiptRepository
        .getNextWriteOffReceiptId("sponsor_writeoff_receipt_sequence", "write_off_sponsor");

    writeOffReceipt.setReceiptId(receiptId);
    writeOffReceipt.setReceiptType("W");

    UUserModel user = new UUserModel(userId);
    Date now = new Date();
    writeOffReceipt.setCreatedBy(user);
    writeOffReceipt.setCreatedAt(now);
    writeOffReceipt.setModifiedAt(now);
    writeOffReceipt.setModifiedBy(user);

    BigDecimal balanceAmount = sponsorReceiptRepository.getSponsorWriteOffAmount(billNo, claimId);
    writeOffReceipt.setAmount(balanceAmount);
    writeOffReceipt.setUnallocatedAmount(balanceAmount);
    writeOffReceipt.setIsSettlement(true);
    writeOffReceipt.setIsDeposit(false);
    writeOffReceipt.setDisplayDate(now);

    sponsorReceiptRepository.persist(writeOffReceipt);

    // Create writeoff receipt_usage entry.
    ReceiptUsageModel receiptUsage = new ReceiptUsageModel(receiptId,
        BillConstants.Restrictions.BILL_NO, billNo);
    sponsorReceiptRepository.persist(receiptUsage);

    // Create billReceipt entry.
    BillReceiptsModel billReceipt = allocationRepository.getBillReceipt(receiptId, billNo);
    if (null == billReceipt) {
      String tpaId = tpa.getTpaId();
      billReceipt = new BillReceiptsModel(writeOffReceipt, bill, now);
      billReceipt.setUsername(userId);
      if (tpaId.equals(bill.getVisitId().getPrimarySponsorId())) {
        billReceipt.setSponsorIndex('P');
      } else {
        billReceipt.setSponsorIndex('S');
      }
      sponsorReceiptRepository.persist(billReceipt);
    }

    // Call the allocation job or call the method to allocate sponsor amounts
    allocationService.scheduleBillChargeAllocation("bill_charge_allocation", userId, billNo);

  }

  private void updateBillRemittance(String billNo, int remittanceId, String remarks,
      boolean writeOffFlag, boolean isDraft) {
    BillRemittanceIdSequence billremittanceId = new BillRemittanceIdSequence(billNo, remittanceId);
    BillRemittanceModel billRemittance = (BillRemittanceModel) sponsorReceiptRepository
        .get(BillRemittanceModel.class, billremittanceId);

    String status = "D";
    if (!isDraft) {
      status = "F";
    }

    boolean shouldCreate = (null != remarks || writeOffFlag);

    if (!shouldCreate && null == billRemittance) {
      return;
    }

    if (shouldCreate && null == billRemittance) {
      billRemittance = new BillRemittanceModel(billremittanceId);
      sponsorReceiptRepository.persist(billRemittance);
    }
    billRemittance.setRemarks(remarks);
    billRemittance.setWriteOff(writeOffFlag);
    billRemittance.setStatus(status);
  }

  /**
   * Save as draft. Auto allocate the amount to the bill charges in FIFO order and save only in
   * insurance_remittance_activity_details as a Draft.
   *
   * @param reconciliationId the reconciliation id
   * @param claimId          the claim id
   * @param billNo           the bill no
   * @param amount           the amount
   * @return the int
   */
  private Map<String, Object> saveAsDraft(int reconciliationId, String claimId, String billNo,
      BigDecimal amount, boolean autoAllocate, Map<String, Object> allocation) {
    Map sponsorReceiptAmounts = (Map) getSponsorReceiptAmountDetails(reconciliationId);
    BigDecimal remainingSponsorAmount = (BigDecimal) sponsorReceiptAmounts.get("total_amount");
    BigDecimal totalAllottedAmount = BigDecimal.ZERO;
    List<String> billChargeIds;

    if (autoAllocate) {
      billChargeIds = sponsorReceiptRepository.getChargesOfBill(billNo);
    } else {
      // Just consider the charges given in the allocation.
      billChargeIds = new ArrayList<>(allocation.keySet());
    }
    // In case allocation is not given then fetch the charges.
    List<String> validCharges = sponsorReceiptRepository.getChargesOfBill(billNo);

    Set<String> allocatedActivityIds = new HashSet<>();

    for (String chargeId : billChargeIds) {
      // do not allow if the chargeIds are not of the bill
      if (!validCharges.contains(chargeId)) {
        continue;
      }

      // Remarks
      String remark = null;

      // Calculate the amount to be settled for that charge id
      BigDecimal balanceAmount = sponsorReceiptRepository.getBalanceAmount(claimId, chargeId);
      BigDecimal settlingAmount = balanceAmount.min(amount);
      BigDecimal minAmount = remainingSponsorAmount.min(settlingAmount);

      // If allocation is provided do another check.
      if (!autoAllocate) {
        Map<String, Object> allocationData = (Map<String, Object>) allocation.get(chargeId);
        BigDecimal inputAmount = new BigDecimal(String.valueOf(allocationData.get("amount")));
        minAmount = minAmount.min(inputAmount);
        remark = String.valueOf(allocationData.get("remark"));
      }

      // If amount is zero don't add an entry into the table.
      if ((minAmount.compareTo(BigDecimal.ZERO)) == 0) {
        continue;
      }

      // Subtract the settled amount from remaining amount
      remainingSponsorAmount = remainingSponsorAmount.subtract(minAmount);
      amount = amount.subtract(minAmount);
      totalAllottedAmount = totalAllottedAmount.add(minAmount);

      // One of A-ACT- / P- / P-ACT- / A-chId-prevBatchId
      String prefix = getPrefixForCharge(chargeId);
      String activityId = prefix.concat(chargeId);


      ReconciliationActivityDetailsModel activityDetails = sponsorReceiptRepository
          .getDraftReconciliationDetails(reconciliationId, activityId, claimId);

      if (null == activityDetails) {
        // Create the insurance_remittance_activity_details entity
        activityDetails = new ReconciliationActivityDetailsModel();

        ReconciliationModel reconciliation = (ReconciliationModel) sponsorReceiptRepository
            .get(ReconciliationModel.class, reconciliationId);
        activityDetails.setReconciliationId(reconciliation);
        activityDetails.setActivityId(activityId);
        activityDetails.setClaimId(new InsuranceClaimModel(claimId));
        activityDetails.setCreatedAt(new Date());
        activityDetails.setCreatedBy((String) sessionService.getSessionAttributes().get("userId"));
        activityDetails.setStatus("D");// Set status as draft
        sponsorReceiptRepository.save(activityDetails);// Save the entity
      }

      activityDetails.setAllocatedAmount(minAmount);
      activityDetails.setDenialRemarks(remark);
      allocatedActivityIds.add(activityId);
    }
    // Should return proper map of the updated claims or bills
    sponsorReceiptRepository.flush();
    
    Map<String, Object> responseMap = new HashMap<>();
    responseMap.put("allocatedActivityIds",allocatedActivityIds);
    responseMap.put("totalAllottedAmount",totalAllottedAmount);
    return responseMap;
  }

  private Map<String, Object> saveRemittance(int reconciliationId, String claimId, String billNo,
      BigDecimal amount, boolean autoAllocate, Map<String, Object> allocation) {
    Map<String, Object> responseMap = new HashMap<>();
    // Enter all the entries as draft first
    responseMap = saveAsDraft(reconciliationId, claimId, billNo, amount, autoAllocate, allocation);
    // And then save by doing stuff in other tables and marking them as finalized.
    // Get all bill charge claims for the claim
    String userId = (String) sessionService.getSessionAttributes().get("userId");
    Date now = new Date();
    List<BillChargeClaimModel> billChargeClaimList = sponsorReceiptRepository
        .getBillChargeClaimsOfVisit(claimId, billNo);
    ReceiptModel sponsorReceipt = sponsorReceiptRepository
        .getReceiptByReconciliationId(reconciliationId);
    // Create a Bill receipt entry if it there is no entry.
    BillReceiptsModel billReceipt = allocationRepository
        .getBillReceipt(sponsorReceipt.getReceiptId(), billNo);
    if (null == billReceipt) {
      billReceipt = new BillReceiptsModel(sponsorReceipt, new BillModel(billNo), now);
      billReceipt.setUsername(userId);
      billReceipt.setAllocatedAmount(BigDecimal.ZERO);
      sponsorReceiptRepository.persist(billReceipt);
    }
    // Update all charges
    List<BillChargeReceiptAllocationModel> billChargeReceipts = new ArrayList<>();
    BigDecimal totalAllotedBillAmount = billReceipt.getAllocatedAmount();
    InsuranceClaimModel claim = new InsuranceClaimModel(claimId);
    for (BillChargeClaimModel billChargeClaim : billChargeClaimList) {
      BigDecimal allocatedAmount = updateBillChargeClaim(billChargeClaim, reconciliationId);
      totalAllotedBillAmount = totalAllotedBillAmount.add(allocatedAmount);
      if (allocatedAmount.compareTo(BigDecimal.ZERO) > 0) {
        BillChargeReceiptAllocationModel chargeReceipt = new BillChargeReceiptAllocationModel();
        chargeReceipt.setBillCharge(billChargeClaim.getChargeId());
        chargeReceipt.setClaimId(claim);
        chargeReceipt.setAllocatedAmount(allocatedAmount);
        billChargeReceipts.add(chargeReceipt);
      }
    }

    // Bill_receipt entries // do this first
    billReceipt.setModTime(now);
    billReceipt.setUsername(userId);
    billReceipt.setAllocatedAmount(totalAllotedBillAmount);
    // BillChargeReceipt entries
    for (BillChargeReceiptAllocationModel chargeReceipt : billChargeReceipts) {
      chargeReceipt.setBillReceipt(billReceipt);
      chargeReceipt.setModifiedAt(now);
      chargeReceipt.setModifiedBy(userId);
      sponsorReceiptRepository.persist(chargeReceipt);
    }
    sponsorReceiptRepository.flush();
    // update the unallocated amount of sponsorReceipt.
    allocationService.updateUnallocatedAmount(sponsorReceipt, billNo);

    // Return new sponsor due.
    responseMap.put("totalAllottedAmount",totalAllotedBillAmount);
    return responseMap;
  }

  private String getPrefixForCharge(String chargeId) {
    BillChargeModel charge = (BillChargeModel) sponsorReceiptRepository.get(BillChargeModel.class,
        chargeId);
    // One of A-ACT- / P- / P-ACT- / A-chId-prevBatchId
    if (BillConstants.pharmacyChargeHeads.contains(charge.getChargeHead().getChargeheadId())) {
      return PHARMACY_PREFIX;
    }
    return DEFAULT_PREFIX;
  }

  /**
   * Update bill charge claim received amount.
   *
   * @param billChargeClaim  the bill charge claim
   * @param reconciliationId the reconciliation id
   * @return the big decimal
   */
  private BigDecimal updateBillChargeClaim(BillChargeClaimModel billChargeClaim,
      int reconciliationId) {
    BigDecimal previouslyReceivedAmount = billChargeClaim.getClaimRecdTotal();
    BigDecimal totalInsuranceAmount =
        billChargeClaim.getInsuranceClaimAmt().add(billChargeClaim.getTaxAmount());
    if (null == previouslyReceivedAmount) {
      previouslyReceivedAmount = new BigDecimal(0);
    }


    BigDecimal remainingAmount = totalInsuranceAmount.subtract(previouslyReceivedAmount);
    InsuranceClaimModel claim = billChargeClaim.getClaimId();
    String chargeId = billChargeClaim.getId().getChargeId();
    String prefix = getPrefixForCharge(chargeId);
    String activityId = prefix.concat(chargeId);
    // Get insurance_remittance_activity_details row for the same
    ReconciliationActivityDetailsModel irad = sponsorReceiptRepository
        .getDraftReconciliationDetails(reconciliationId, activityId, claim.getClaimId());
    // If there is no entry in irad the amount is 0 so just return.
    if (null == irad) {
      return BigDecimal.ZERO;
    }
    BigDecimal toAdd = remainingAmount.min(irad.getAllocatedAmount());
    BigDecimal receivedAmount = previouslyReceivedAmount.add(toAdd);
    billChargeClaim.setClaimRecdTotal(receivedAmount);
    // Finalize the irda also
    irad.setStatus("F");
    // add the amount to the sponsor receipt allocatedAmount
    String userId = (String) sessionService.getSessionAttributes().get("userId");
    UUserModel user = new UUserModel();
    user.setEmpUsername(userId);
    ReconciliationModel reconciliation = (ReconciliationModel) sponsorReceiptRepository
        .get(ReconciliationModel.class, reconciliationId);
    
    BigDecimal allocatedAmount = reconciliation.getAllocatedAmount();
    reconciliation.setAllocatedAmount(allocatedAmount.add(toAdd));
    reconciliation.setModifiedBy(user);
    // Update remarks
    String remark = irad.getDenialRemarks();
    if (null != remark) {
      billChargeClaim.setDenialRemarks(remark);
    }
    return toAdd;
  }

  @Transactional(readOnly = true)
  public List<Map<String, Object>> getBillCharges(String billNo, String claimId) {
    return sponsorReceiptRepository.getBillChargeClaims(Collections.singletonList(billNo), claimId);
  }

  /**
   * Gets bill charges.
   *
   * @param billNos the bill nos
   * @return the bill charges
   */
  @Transactional(readOnly = true)
  public Map<String, Object> getBillCharges(List<String> billNos) {
    Map<String, Object> result = new HashMap<>();
    if (CollectionUtils.isEmpty(billNos)) {
      return Collections.EMPTY_MAP;
    }
    sponsorReceiptRepository.getBillChargeClaims(billNos).forEach(chargeClaim -> {
      List<String> charges =
          result.get(chargeClaim.get("bill_no") + "_" + chargeClaim.get("claim_id")) != null
              ? (List) ((Map) result
              .get(chargeClaim.get("bill_no") + "_" + chargeClaim.get("claim_id")))
              .get(chargeClaim.get("activity_id")) : null;
      if (charges == null) {
        charges = new ArrayList();
        Map<String, List<String>> activityChargeMap =
            (Map<String, List<String>>) result
                .get(chargeClaim.get("bill_no") + "_" + chargeClaim.get("claim_id"));
        if (activityChargeMap == null) {
          activityChargeMap = new HashMap<>();
        }

        activityChargeMap.put((String) chargeClaim.get("activity_id"), charges);
        result
            .put(chargeClaim.get("bill_no") + "_" + chargeClaim.get("claim_id"), activityChargeMap);
      }
      charges.add((String) chargeClaim.get("charge_id"));

    });

    return result;
  }

  /**
   * Gets the list of mrNo or Bills or Claims.
   *
   * @param tpaId   the tpa id
   * @param mrNo    the mr no
   * @param billNo  the bill no
   * @param claimId the claim id
   * @return the list
   */
  @Transactional(readOnly = true)
  public List<Map<String, Object>> getList(String tpaId, String mrNo, String billNo,
      String claimId, Integer centerId) {
    if (null != mrNo) {
      return sponsorReceiptRepository.getMrNoList(mrNo, tpaId, centerId);
    }

    if (null != billNo) {
      return sponsorReceiptRepository.getBillList(billNo, tpaId, centerId);
    }

    if (null != claimId) {
      return sponsorReceiptRepository.getClaimList(claimId, tpaId, centerId);
    }
    return Collections.emptyList();
  }

  /**
   * Gets bills of bill no.
   *
   * @param billNoList the bill no list
   * @param tpaId      the tpa id
   * @return the bills of bill no
   */
  @Transactional(readOnly = true)
  public Object getBillsOfBillNo(List<String> billNoList, String tpaId, Integer centerId) {
    if (StringUtils.isEmpty(tpaId)) {
      return sponsorReceiptRepository.getBillsByBillNo(billNoList, centerId);
    }
    return sponsorReceiptRepository.getBillsByBillNoTpaId(billNoList, tpaId, centerId);
  }

  @Transactional(readOnly = true)
  public Object getBillsOfClaim(List<String> claimIdList) {
    return sponsorReceiptRepository.getBillsByClaimId(claimIdList);
  }

  @Transactional(readOnly = true)
  public Object getBillsOfMrNo(List<String> mrNoList, String tpaId) {
    return sponsorReceiptRepository.getBillsByMrNo(mrNoList, tpaId);
  }

  @Transactional(readOnly = true)
  public Object getBillsByFinalizedDate(String tpaId, Integer centerId, Date fromDate,
      Date toDate) {
    return sponsorReceiptRepository.getBillsFinalizedBetween(tpaId, centerId, fromDate, toDate);
  }

  /**
   * Gets the drafted allocated amounts for bills which were saved as draft.
   *
   * @param reconciliationId the reconciliation id
   * @return the drafted allocated amounts
   */
  private Map<String, BigDecimal> getBillAllocatedAmounts(
      int reconciliationId, boolean drafted) {
    List remittanceAmountsList;
    if (drafted) {
      remittanceAmountsList = sponsorReceiptRepository
          .getDraftedRemittanceAmounts(reconciliationId);
    } else {
      remittanceAmountsList = sponsorReceiptRepository
          .getFinalizedRemittanceAmounts(reconciliationId);
    }
    if (CollectionUtils.isEmpty(remittanceAmountsList)) {
      return Collections.EMPTY_MAP;
    }

    //TODO Make this elegant, so that we don't need to deal with indexes
    Map<String, BigDecimal> remittanceAmounts = new HashMap<>();
    List<Object> chargeIds = ((List<Object[]>) remittanceAmountsList).stream()
        .map(remittanceAmountOb -> remittanceAmountOb[1]).collect(Collectors.toList());
    List<String> claimIds = ((List<Object[]>) remittanceAmountsList).stream()
        .map(remittanceAmountOb -> (String) remittanceAmountOb[0]).collect(Collectors.toList());

    List<Object[]> billChargeArray =
        sponsorReceiptRepository
            .getBillsOfCharges(chargeIds.toArray(), claimIds.toArray(new String[0]));

    Map<String, String> chargeBillMap = new HashMap<>();
    for (Object[] entry : billChargeArray) {
      chargeBillMap.put((String) entry[1], (String) entry[0]);
    }

    for (Object[] remittanceAmount : ((List<Object[]>) remittanceAmountsList)) {
      String key = chargeBillMap.get(remittanceAmount[1]) + "_" + (String) remittanceAmount[0];
      BigDecimal amount =
          remittanceAmounts.get(key) != null ? remittanceAmounts.get(key) : BigDecimal.ZERO;
      remittanceAmounts
          .put(key, amount.add((BigDecimal) remittanceAmount[2]));
    }

    return remittanceAmounts;
  }

  private Map<String, Map<String, Object>> formatAllocations(List<Object[]> allocationList) {
    if (allocationList.isEmpty()) {
      return Collections.emptyMap();
    }
    Map<String, Map<String, Object>> allocationMap = new HashMap<>();
    for (Object[] allocation : allocationList) {
      Map<String, Object> allocationData = new HashMap<>();
      // Amount
      if (allocationMap.get(allocation[0] + "_" + allocation[3]) != null) {
        BigDecimal previousAmount =
            (BigDecimal) allocationMap.get(allocation[0] + "_" + allocation[3]).get("amount");
        allocationData.put("amount", ((BigDecimal) allocation[1]).add(previousAmount));
      } else {
        allocationData.put("amount", allocation[1]);
      }
      // Remarks
      String remark = (String) allocation[2];
      if (!"null".equals(remark)) {
        allocationData.put("remark", remark);
      }
      // Charge id [0]
      allocationMap.put((String) allocation[0] + "_" + (String) allocation[3], allocationData);
    }
    return allocationMap;
  }

  /**
   * Gets the allocations of drafted bills.
   *
   * @param reconciliationId the reconciliation id
   * @return the drafted allocations
   */
  private Map<String, Map<String, Object>> getDraftedAllocations(int reconciliationId) {
    List<Object[]> allocationList =
        sponsorReceiptRepository.getDraftedAllocations(reconciliationId);
    return formatAllocations(allocationList);
  }

  private Map<String, Map<String, Object>> getBillChargesMap(
      Map<String, Map<String, Object>> allocationMap) {
    Object[] charges =
        allocationMap.keySet().stream().map(key -> key.split("_")[0]).distinct().toArray();
    String[] claimIds =
        allocationMap.keySet().stream().map(key -> key.split("_")[1]).distinct()
            .toArray(String[]::new);
    if (charges.length == 0) {
      return Collections.emptyMap();
    }
    List<Object[]> billCharges = sponsorReceiptRepository.getBillsOfCharges(charges, claimIds);
    Map<String, Map<String, Object>> billChargesMap = new HashMap<>();
    for (Object[] entry : billCharges) {
      String billNo = (String) entry[0];
      String chargeId = (String) entry[1];
      String claimId = (String) entry[2];
      Map<String, Object> chargeMap = billChargesMap.get(billNo + "_" + claimId);
      if (null == chargeMap) {
        chargeMap = new HashMap<>();
        billChargesMap.put(billNo + "_" + claimId, chargeMap);
      }
      chargeMap.put(chargeId, allocationMap.get(chargeId + "_" + claimId));
    }
    return billChargesMap;
  }

  private Map<String, Object> formatBillRemittance(List<BillRemittanceModel> billRemittanceList) {

    Map<String, Object> billMap = new HashMap<>();
    for (BillRemittanceModel billRemittance : billRemittanceList) {
      String billNo = billRemittance.getId().getBillNo();
      Map<String, String> dataMap = new HashMap<>();
      dataMap.put("remarks", billRemittance.getRemarks());
      String writeOffFlag = (billRemittance.getWriteOff()) ? "Y" : "N";
      dataMap.put("writeOffRemaining", writeOffFlag);
      billMap.put(billNo, dataMap);
    }
    return billMap;
  }

  private Map<String, Object> getDraftedBillRemittance(int remittanceId, List<String> billList) {
    if (null != billList && !billList.isEmpty()) {
      List<BillRemittanceModel> billRemittanceList = sponsorReceiptRepository
          .getDraftedBillRemittances(remittanceId, billList.toArray());
      return formatBillRemittance(billRemittanceList);
    }
    return Collections.emptyMap();
  }

  /**
   * Gets the drafted details.
   *
   * @param reconciliationId the reconciliation id
   * @return the drafted details
   */
  @Transactional(readOnly = true)
  public Map<String, Object> getDraftedDetails(int reconciliationId) {
    Map<String, Object> finalMap = new HashMap<>();
    // Get the drafted allocations and format it as per need.
    Map<String, Map<String, Object>> allocationMap = getDraftedAllocations(reconciliationId);
    Map<String, Map<String, Object>> receiptAllocationMap = getBillChargesMap(allocationMap);
    finalMap.put("allocations", receiptAllocationMap);
    List<String> billList = new ArrayList<String>(
        receiptAllocationMap.keySet().stream().map(key -> key.split("_")[0]).collect(
            Collectors.toSet()));
    List<String> claimIds = new ArrayList<String>(
        receiptAllocationMap.keySet().stream().map(key -> key.split("_")[1]).collect(
            Collectors.toSet()));
    // Get the bill details of bills which were saved as draft.
    if (billList.isEmpty()) {
      finalMap.put("bills", Collections.emptyList());
    } else {
      finalMap.put("bills", sponsorReceiptRepository.getDraftedBillsByBillNo(billList, claimIds));
    }
    finalMap.put("draftedAmounts", getBillAllocatedAmounts(reconciliationId, true));
    finalMap.put("additionalInformation", getDraftedBillRemittance(reconciliationId, billList));
    return finalMap;
  }

  /**
   * Gets the remitted bill details.
   *
   * @param reconciliationId the reconciliation id
   * @return the remitted bill details
   */
  @Transactional(readOnly = true)
  public Map<String, Object> getRemittedBillDetails(int reconciliationId) {
    Map<String, Object> finalMap = new HashMap<>();
    // Get the remitted allocations and format it as per need.
    Map<String, Map<String, Object>> allocationMap = getRemittedAllocations(reconciliationId);
    Map<String, Map<String, Object>> receiptAllocationMap = getBillChargesMap(allocationMap);
    finalMap.put("allocations", receiptAllocationMap);
    List<String> billList = receiptAllocationMap.keySet().stream().map(key -> key.split("_")[0])
        .collect(Collectors.toList());
    List<String> claimIds = receiptAllocationMap.keySet().stream().map(key -> key.split("_")[1])
        .collect(Collectors.toList());
    // Get the bill details of bills which were saved as draft.
    if (billList.isEmpty()) {
      finalMap.put("bills", Collections.emptyList());
    } else {
      finalMap.put("bills",
          ((List<Map>) sponsorReceiptRepository.getBillsByBillNo(billList, claimIds)).stream()
              .filter((bill ->
                  ((BigDecimal) bill.get("past_paid_amount")).compareTo(BigDecimal.ZERO) != 0
              )).collect(Collectors.toList()));
    }
    //TODO refactor this
    finalMap.put("remittanceAmounts", getBillAllocatedAmounts(reconciliationId, false));
    finalMap.put("additionalInformation", getSavedBillRemittance(reconciliationId, billList));
    return finalMap;
  }

  private Map<String, Map<String, Object>> getRemittedAllocations(int reconciliationId) {
    List<Object[]> allocationList = sponsorReceiptRepository
        .getRemittedAllocations(reconciliationId);
    return formatAllocations(allocationList);
  }

  private Map<String, Object> getSavedBillRemittance(int remittanceId, List<String> billList) {
    if (null != billList && !billList.isEmpty()) {
      List<BillRemittanceModel> billRemittanceList = sponsorReceiptRepository
          .getSavedBillRemittances(remittanceId, billList.toArray());
      return formatBillRemittance(billRemittanceList);
    }
    return Collections.emptyMap();
  }

  @Transactional(readOnly = true)
  public List<Map<String, String>> getTpaList(String tpaName, Date fromDate, Date toDate) {
    return sponsorReceiptRepository.getTpaList(tpaName, fromDate, toDate);
  }

  /**
   * Ge payment references list.
   *
   * @param referenceNo the reference no
   * @param tpaId       the tpa id
   * @param fromDate    the from date
   * @param toDate      the to date
   * @return the list
   */
  @Transactional(readOnly = true)
  public List<String> gePaymentReferences(String referenceNo, String tpaId, Date fromDate,
      Date toDate) {
    Integer centerId = (Integer) sessionService.getSessionAttributes().get("centerId");
    return sponsorReceiptRepository
        .getPaymentReferenceList(referenceNo, tpaId, fromDate, toDate, centerId);
  }

  /**
   * Gets the sponsor receipt list.
   *
   * @param receiptId   the receipt id
   * @param referenceNo the reference no
   * @param tpaId       the tpa id
   * @param fromDate    the from date
   * @param toDate      the to date
   * @return the sponsor receipt list
   */
  @Transactional(readOnly = true)
  public List<Map<String, Object>> getSponsorReceiptList(String receiptId, String referenceNo,
      String tpaId, Date fromDate, Date toDate) {
    Integer centerId = (Integer) sessionService.getSessionAttributes().get("centerId");
    List<Map<String, Object>> sponsorReceiptList = sponsorReceiptRepository
        .getSponsorReceiptList(receiptId, referenceNo, tpaId, fromDate, toDate, centerId);
    for (Map<String, Object> receipt : sponsorReceiptList) {
      int reconciliationId = (int) receipt.get("reconciliation_id");
      receipt.put("drafted_amount", sponsorReceiptRepository.getDraftedAmount(reconciliationId));
    }
    return sponsorReceiptList;
  }

  /**
   * Update sponsor receipt.
   *
   * @param receiptId the receipt id
   * @param json      the json
   */
  @Transactional
  public void updateSponsorReceipt(String receiptId, Map<String, String> json) {
    String userId = (String) sessionService.getSessionAttributes().get("userId");
    if (!checkUserRights("edit_receipt")) {
      throw new HMSException(HttpStatus.FORBIDDEN, "exception.user.not.authorized.to.edit.receipt",
          new String[] {userId});
    }

    ReceiptModel sponsorReceipt = (ReceiptModel) sponsorReceiptRepository.get(ReceiptModel.class,
        receiptId);

    BigDecimal updatedAmount = new BigDecimal(json.get("payment_amount"));
    BigDecimal updatedOtherDeductions = BigDecimal.ZERO;
    BigDecimal updatedTdsAmount = BigDecimal.ZERO;

    String tdsAmountString = json.get("tds_amount");
    if (null != tdsAmountString) {
      updatedTdsAmount = new BigDecimal(tdsAmountString);
    }

    String otherAmountString = json.get("other_deductions");
    if (null != otherAmountString) {
      updatedOtherDeductions = new BigDecimal(otherAmountString);
    }
    BigDecimal currentAmount = sponsorReceipt.getAmount();
    BigDecimal currentTdsAmount = sponsorReceipt.getTdsAmount();
    BigDecimal currentOtherDeductions = sponsorReceipt.getOtherDeductions();

    BigDecimal currentTotal = currentAmount.add(currentTdsAmount).add(currentOtherDeductions);
    BigDecimal updatedTotal = updatedAmount.add(updatedTdsAmount).add(updatedOtherDeductions);

    BigDecimal unallocatedAmount = sponsorReceipt.getUnallocatedAmount();

    BigDecimal diff = currentTotal.subtract(updatedTotal);

    boolean validReductionAmount = false;

    // If Difference is less than unallocated amount then the amount can be reduced.
    validReductionAmount = (BigDecimal.ZERO.compareTo(diff) > 0
        || (diff.compareTo(unallocatedAmount) <= 0));

    // If the amount is not a valid reduction amount throw an error.
    if (!validReductionAmount) {
      throw new HMSException(HttpStatus.BAD_REQUEST,
          "exception.receipt.amount.less.than.allocated.amount", null);
    }

    sponsorReceipt.setAmount(new BigDecimal(json.get("payment_amount")));

    sponsorReceipt.setPaymentModeId(
        new PaymentModeMasterModel(Integer.parseInt(json.get("payment_mode_id"))));

    SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
    Date paymentReceivedDate;

    try {
      paymentReceivedDate = format.parse(json.get("received_date"));
    } catch (ParseException parseException) {
      throw new HMSException("Invalid date format");
    }

    sponsorReceipt.setPaymentReceivedDate(paymentReceivedDate);

    sponsorReceipt.setRemarks(json.get("narration"));

    sponsorReceipt.setAmount(updatedAmount);

    sponsorReceipt.setTdsAmount(updatedTdsAmount);

    sponsorReceipt.setOtherDeductions(updatedOtherDeductions);

    sponsorReceipt.setBankName(json.get("bank"));

    sponsorReceipt.setBankBatchNo(json.get("bank_batch_number"));

    sponsorReceipt.setReferenceNo(json.get("payment_reference_number"));

    sponsorReceipt.setUnallocatedAmount(unallocatedAmount.subtract(diff));
    
    UUserModel user = (UUserModel) sponsorReceiptRepository.get(UUserModel.class,userId);
    Date now = new Date();
    
    sponsorReceipt.setModifiedAt(now);
    sponsorReceipt.setModifiedBy(user);

  }

  @Transactional
  private Set<String> getValidRemittanceBills(List<String> billNos, Integer centerId,
      String tpaId) {
    return new HashSet<>(
        sponsorReceiptRepository.getValidRemittanceBills(billNos, tpaId, centerId));
  }


  @Transactional
  private Set<String> getValidActivityIds(List<String> activityIds, Integer centerId,
      String tpaId) {
    return new HashSet<>(
        sponsorReceiptRepository.getValidActivityIds(activityIds, tpaId, centerId));
  }


  /**
   * Validate csv data list.
   *
   * @param csvData  the csv data
   * @param keyMap   the key map
   * @param centerId the center id
   * @param tpaId    the tpa id
   * @param type     the type
   * @return the list
   */
  @Transactional
  public List<Map<String, Object>> validateCsvData(List<Map<String, Object>> csvData,
      Map<String, String> keyMap, Integer centerId, String tpaId, String type) {

    switch (type) {
      case CSV_TYPE_BILL_LEVEL:
        return validateBillLevelCsv(csvData, keyMap, centerId, tpaId);
      case CSV_TYPE_ACTIVITY_LEVEL:
        return validateActivityLevelCsv(csvData, keyMap, centerId, tpaId);
      default:
        throw new UnsupportedOperationException("Invalid csv type " + type);
    }
  }

  private List<Map<String, Object>> validateActivityLevelCsv(List<Map<String, Object>> csvData,
      Map<String, String> keyMap, Integer centerId, String tpaId) {
    final String[] requiredFields =
        new String[] {CSV_MAPPING_BILL_NO, CSV_MAPPING_ACTIVITY_ID, CSV_MAPPING_REMITTANCE_AMOUNT};
    final String[] fields =
        new String[] {CSV_MAPPING_BILL_NO, CSV_MAPPING_REMITTANCE_AMOUNT, CSV_MAPPING_REMARKS};
    if (org.springframework.util.CollectionUtils.isEmpty(csvData)) {
      return Collections.EMPTY_LIST;
    }

    Arrays.stream(fields).forEach(field -> {
      if (keyMap == null || StringUtils.isBlank(keyMap.get(field))) {
        throw new ValidationException("Invalid mapping");
      }
    });

    List<String> csvBillNumbers =
        csvData.stream().map(row -> (String) row.get(keyMap.get(CSV_MAPPING_BILL_NO))).collect(
            Collectors.toList());
    Set<String> validBillNumbers = getValidRemittanceBills(csvBillNumbers, centerId, tpaId);

    List<String> csvActivityIds = csvData.stream().map(row -> {
      if (row.get(keyMap.get(CSV_MAPPING_ACTIVITY_ID)) != null) {
        return ((String) row.get(keyMap.get(CSV_MAPPING_ACTIVITY_ID))).substring(2);
      } else {
        return null;
      }
    }).collect(Collectors.toList());
    Set<String> validActivityIds = getValidActivityIds(csvActivityIds, centerId, tpaId);


    csvData.forEach(row -> {
      List<String> errors = new ArrayList<>();
      Arrays.stream(requiredFields).forEach(field -> {
        if (row.get(keyMap.get(field)) == null || StringUtils
            .isBlank(row.get(keyMap.get(field)).toString())) {
          errors.add(keyMap.get(field) + " is blank");
        }
      });
      if (!StringUtils.isBlank((String) row.get(keyMap.get(CSV_MAPPING_BILL_NO)))
          && !validBillNumbers.contains((String) row.get(keyMap.get(CSV_MAPPING_BILL_NO)))) {
        errors.add("Invalid bill number");
      }
      if (!StringUtils.isBlank((String) row.get(keyMap.get(CSV_MAPPING_ACTIVITY_ID)))
          && !validActivityIds.contains(
          (String) ((String) row.get(keyMap.get(CSV_MAPPING_ACTIVITY_ID))).substring(2))) {
        errors.add("Invalid activity id");
      }

      row.put("error", errors);

    });
    return csvData;

  }

  private List validateBillLevelCsv(List<Map<String, Object>> csvData, Map<String, String> keyMap,
      Integer centerId, String tpaId) {
    final String[] requiredFields =
        new String[] {CSV_MAPPING_BILL_NO, CSV_MAPPING_REMITTANCE_AMOUNT};
    final String[] fields =
        new String[] {CSV_MAPPING_BILL_NO, CSV_MAPPING_REMITTANCE_AMOUNT, CSV_MAPPING_REMARKS};

    if (org.springframework.util.CollectionUtils.isEmpty(csvData)) {
      return Collections.EMPTY_LIST;
    }

    Arrays.stream(fields).forEach(field -> {
      if (keyMap == null || StringUtils.isBlank(keyMap.get(field))) {
        throw new ValidationException("Invalid mapping");
      }
    });

    List<String> csvBillNumbers =
        csvData.stream().map(row -> (String) row.get(keyMap.get(CSV_MAPPING_BILL_NO))).collect(
            Collectors.toList());
    Set<String> validBillNumbers = getValidRemittanceBills(csvBillNumbers, centerId, tpaId);


    csvData.forEach(row -> {
      List<String> errors = new ArrayList<>();
      Arrays.stream(requiredFields).forEach(field -> {
        if (row.get(keyMap.get(field)) == null || StringUtils
            .isBlank(row.get(keyMap.get(field)).toString())) {
          errors.add(keyMap.get(field) + " is blank");
        }
      });
      if (!StringUtils.isBlank((String) row.get(keyMap.get(CSV_MAPPING_BILL_NO)))
          && !validBillNumbers.contains((String) row.get(keyMap.get(CSV_MAPPING_BILL_NO)))) {
        errors.add("Invalid bill number");
      }
      row.put("error", errors);

    });
    return csvData;
  }

  private static List<String> getChargeIds(List<String> activityIds) {
    return activityIds.stream().map(activityId -> {
      String[] activityIdParts = activityId.split("-");
      if ((activityId.startsWith("A") || activityId.startsWith("P"))
          && activityIdParts.length == 2) {
        return activityIdParts[1];
      }
      return null;
    }).collect(Collectors.toList());
  }


  /**
   * Gets bills of activity ids.
   *
   * @param activityIds the activity ids
   * @param centerId    the center id
   * @param tpaId       the tpa id
   * @return the bills of activity ids
   */
  @Transactional(readOnly = true)
  public List<Map<String, Object>> getBillsOfActivityIds(List<String> activityIds, Integer centerId,
      String tpaId) {
    List<String> chargeIds = getChargeIds(activityIds);
    return sponsorReceiptRepository.getBillsByActivityId(chargeIds, centerId, tpaId);
  }

}
