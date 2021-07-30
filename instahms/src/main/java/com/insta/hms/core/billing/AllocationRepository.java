package com.insta.hms.core.billing;

import com.insta.hms.common.GenericHibernateRepository;
import com.insta.hms.common.HibernateHelper;
import com.insta.hms.model.StoreSalesDetailsModel;

import java.util.ArrayList;
import java.util.Date;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;

import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class AllocationRepository.
 */
@Repository
public class AllocationRepository extends GenericHibernateRepository {


  /** The Constant GET_RECEIPTS_OF_BILL. */
  private static final String GET_RECEIPTS_OF_BILL = "SELECT r "
      + "FROM BillReceiptsModel br JOIN br.receiptNo r "
      + "WHERE br.billNo = :billNo AND r.receiptType IN (:receiptTypes) "
      + "AND r.isDeposit = :isDeposit "
      + "ORDER BY r.receiptId";

  /**
   * Gets the receipt list.
   *
   * @param billNo
   *          the bill no
   * @return the receipt list
   */
  public List getReceiptList(String billNo) {
    return getReceiptList(billNo, false, false);
  }

  
  /** The Constant GET_STORE_SALE_TYPE. */
  private static final String GET_STORE_SALE_TYPE = "SELECT cast(ssm.type as string) AS type "
      + "FROM StoreSalesMainModel ssm "
      + "WHERE ssm.billNo = :billNo AND ssm.type = 'R' LIMIT 1";
  
  /** The Constant GET_BILL_RECEIPT_SALE_TYPE. */
  private static final String GET_BILL_RECEIPT_SALE_TYPE = "SELECT DISTINCT cast(ssm.type as string) AS type "
      + "FROM StoreSalesMainModel ssm JOIN ssm.billNo b JOIN b.receipts r "
      + "WHERE ssm.billNo = :billNo AND r.receiptId = :receiptId AND ssm.type = 'R' "
      + " AND b.restrictionType = 'P' ";
  
  
  /**
   * Gets the store sale type.
   *
   * @param billNo the bill no
   * @return the store sale type
   */
  public String getStoreSaleType(String billNo) {
    Session session = getSession();
    Query query = session.createQuery(GET_STORE_SALE_TYPE);
    query.setString("billNo", billNo);
    return (String) query.uniqueResult();
  }
  
  /**
   * Gets the store sale type.
   *
   * @param billNo the bill no
   * @param receiptId the receipt id
   * @return the store sale type
   */
  public String getStoreSaleType(String billNo, String receiptId) {
    Session session = getSession();
    Query query = session.createQuery(GET_BILL_RECEIPT_SALE_TYPE);
    query.setString("billNo", billNo);
    query.setString("receiptId", receiptId);
    return (String) query.uniqueResult();
  }
  
  /**
   * Gets the receipt list.
   *
   * @param billNo
   *          the bill no
   * @param isDeposit
   *          the is deposit
   * @param lifo
   *          the lifo
   * @return the receipt list
   */
  public List getReceiptList(String billNo, boolean isDeposit, boolean lifo) {
    Session session = getSession();
    String order = "";
    if (lifo) {
      order = " DESC";
    }
    Query query = session.createQuery(GET_RECEIPTS_OF_BILL + order);
    query.setString("billNo", billNo);
    query.setBoolean("isDeposit", isDeposit);
    String[] receiptTypes = new String[] {"R","W","F"};
    query.setParameterList("receiptTypes", receiptTypes);
    return query.list();
    
  }

  /** The Constant GET_CHARGES_OF_BILL. */
  private static final String GET_CHARGES_OF_BILL = "FROM BillChargeModel "
      + "WHERE billNo = :billNo ORDER BY chargeId";
  
  /** The Constant GET_RETURN_CHARGES_OF_BILL. */
  private static final String GET_RETURN_CHARGES_OF_BILL = "FROM BillChargeModel "
      + "WHERE billNo = :billNo AND (amount < 0 OR insuranceClaimAmount < 0) ORDER BY chargeId";

  /**
   * Gets the charges.
   *
   * @param billNo the bill no
   * @param returnCharges the return charges
   * @return the charges
   */
  public List getCharges(String billNo, Boolean returnCharges) {
    Session session = getSession();
    Query query;
    if (Boolean.TRUE.equals(returnCharges)) {
      query = session.createQuery(GET_RETURN_CHARGES_OF_BILL);
    } else {
      query = session.createQuery(GET_CHARGES_OF_BILL);      
    }
    query.setString("billNo", billNo);
    return query.list();
  }
  
  /** The Constant GET_CHARGES_OF_BILL_BY_TPA. */
  private static final String GET_CHARGES_OF_BILL_BY_TPA = "SELECT bc "
      + "FROM BillChargeModel bc JOIN bc.billChargeClaims bcc "
      + "WHERE bc.billNo = :billNo AND bcc.sponsorId = :tpaId " + "ORDER BY bc.chargeId";
  
  /**
   * Gets the charges.
   *
   * @param billNo
   *          the bill no
   * @param tpaId
   *          the tpa id
   * @return the charges
   */
  public List<BillChargeModel> getCharges(String billNo, String tpaId) {
    Session session = getSession();
    Query query = session.createQuery(GET_CHARGES_OF_BILL_BY_TPA);
    query.setString("billNo", billNo);
    query.setString("tpaId", tpaId);
    return query.list();
  }

  /** The Constant GET_DISCOUNT_CHARGES_OF_BILL. */
  private static final String GET_DISCOUNT_CHARGES_OF_BILL = "FROM BillChargeModel "
      + "WHERE billNo = :billNo AND amount < 0 ORDER BY chargeId";

  /**
   * Gets the discount charges.
   *
   * @param billNo
   *          the bill no
   * @return the discount charges
   */
  public List getDiscountCharges(String billNo) {
    Session session = getSession();
    Query query = session.createQuery(GET_DISCOUNT_CHARGES_OF_BILL);
    query.setString("billNo", billNo);
    return query.list();
  }

  /** The Constant GET_BILL_CHARGE_CLAIM. */
  private static final String GET_BILL_CHARGE_CLAIM = "FROM BillChargeClaimModel "
      + "WHERE billNo = :billNo AND sponsorId = :tpaId AND chargeId = :chargeId";

  /**
   * Gets the bill charge claim.
   *
   * @param chargeId
   *          the charge id
   * @param billNo
   *          the bill no
   * @param tpaId
   *          the tpa id
   * @return the bill charge claim
   */
  public BillChargeClaimModel getBillChargeClaim(String chargeId, String billNo, String tpaId) {
    Session session = getSession();
    Query query = session.createQuery(GET_BILL_CHARGE_CLAIM);
    query.setString("chargeId", chargeId);
    query.setString("billNo", billNo);
    query.setString("tpaId", tpaId);
    return (BillChargeClaimModel) query.uniqueResult();
  }
  
  /** The Constant GET_PATIENT_PAID_RECEIPTS_OF_BILL. */
  private static final String GET_PATIENT_PAID_RECEIPTS_OF_BILL = "SELECT r "
      + "FROM BillReceiptsModel br JOIN br.receiptNo r "
      + "WHERE br.billNo = :billNo AND r.receiptType in (:receiptTypes) "
      + "AND r.tpaId IS NULL AND r.isDeposit = false AND r.paymentModeId NOT IN (-6,-7,-8,-9) "
      + "ORDER BY r.receiptId";

  /**
   * Gets the patient receipt list.
   *
   * @param billNo
   *          the bill no
   * @return the patient receipt list
   */
  public List<ReceiptModel> getPatientReceiptList(String billNo) {
    return getPatientReceiptList(billNo, false, false);
  }

  /**
   * Gets the patient receipt list.
   *
   * @param billNo the bill no
   * @param lifo the lifo
   * @param onlyReceipts the refund receipts
   * @return the patient receipt list
   */
  public List<ReceiptModel> getPatientReceiptList(String billNo, boolean lifo, 
      boolean onlyReceipts) {
    Session session = getSession();
    String order = "";
    if (lifo) {
      order = " DESC";
    }
    Query query = session.createQuery(GET_PATIENT_PAID_RECEIPTS_OF_BILL + order);
    query.setString("billNo", billNo);
    query.setParameterList("receiptTypes", new String[] {"R","F"});
    if (onlyReceipts) {
      query.setParameterList("receiptTypes", new String[] {"R"});            
    }
    return query.list();
  }
  
  /** The Constant GET_PATIENT_REFUND_RECEIPTS. */
  private static final String GET_PATIENT_REFUND_RECEIPTS = "SELECT r "
      + "FROM BillReceiptsModel br JOIN br.receiptNo r  LEFT JOIN r.refundReceipts rf "
      + "WHERE br.billNo = :billNo AND r.receiptType = 'F' AND r.isDeposit = false "
      + "AND rf.id IS NULL "
      + "ORDER BY r.receiptId";

  
  /**
   * Gets the un referenced refund receipts.
   *
   * @param billNo the bill no
   * @return the un referenced refund receipts
   */
  public List<ReceiptModel> getUnReferencedRefundReceipts(String billNo) {
    Session session = getSession();
    Query query = session.createQuery(GET_PATIENT_REFUND_RECEIPTS);
    query.setString("billNo", billNo);
    return query.list();
  }
  
  /** The Constant GET_REFUNDED_RECEIPTS_WITH_BILL_RECEIPTS. */
  private static final String GET_REFUNDED_RECEIPTS_WITH_BILL_RECEIPTS = "SELECT "
      + "new map(rrr.id as refundReferenceId, "
      + "rrr.amount + COALESCE(rrr.taxAmount,0) as refundedAmount, "
      + "br.billReceiptId as billReceiptId, br.allocatedAmount as allocatedAmount) "
      + "FROM ReceiptRefundReferenceModel rrr JOIN rrr.receipt r "
      + " JOIN r.billReceipts br "
      + "WHERE br.billNo = :billNo AND rrr.amount>0 ORDER BY rrr.id";
  

  /**
   * Gets the refund receipts and bill receipts.
   *
   * @param billNo the bill no
   * @return the refund receipts and bill receipts
   */
  public List<Map<String, Object>> getRefundReceiptsAndBillReceipts(String billNo) {
    Session session = getSession();
    Query query = session.createQuery(GET_REFUNDED_RECEIPTS_WITH_BILL_RECEIPTS);
    query.setString("billNo", billNo);
    return query.list();
  }
  
  /** The Constant GET_REFUND_REFERENCE_ALLOCATIONS. */
  private static final String GET_REFUND_REFERENCE_ALLOCATIONS = " SELECT rrav "
      + "FROM RefundReferenceAllocationViewModel AS rrav "
      + "WHERE rrav.billNo = :billNo "
      + " AND rrav.amount != rrav.allocatedAmount "
      + " AND rrav.billReceipt = :billReceiptId ";
    
  
  /** The Constant GET_UNUTILIZED_REFUND_REFERENCE_ALLOCATIONS. */
  private static final String GET_UNUTILIZED_REFUND_REFERENCE_ALLOCATIONS = " SELECT rrav "
      + "FROM RefundReferenceAllocationViewModel AS rrav "
      + "WHERE rrav.billNo = :billNo "
      + " AND rrav.amount != rrav.allocatedAmount ";
  
  /**
   * Gets the refund reference allocations.
   *
   * @param billNo the bill no
   * @param billReceiptId the bill receipt id
   * @return the refund reference allocations
   */
  public List<RefundReferenceAllocationViewModel> getRefundReferenceAllocations(String billNo, 
      Long billReceiptId) {
    Session session = getSession();
    Query query;
    if (billReceiptId > 0) {
      query = session.createQuery(GET_REFUND_REFERENCE_ALLOCATIONS);
      query.setLong("billReceiptId", billReceiptId);
    } else {
      query = session.createQuery(GET_UNUTILIZED_REFUND_REFERENCE_ALLOCATIONS);
    }
    query.setString("billNo", billNo);
    return query.list();
  }

  /** The Constant GET_SPONSOR_RECEIPTS_OF_BILL. */
  private static final String GET_SPONSOR_RECEIPTS_OF_BILL = "SELECT r "
      + "FROM BillReceiptsModel br JOIN br.receiptNo r "
      + "WHERE br.billNo = :billNo AND r.receiptType = 'R' "
      + "AND r.tpaId IS NOT NULL AND r.receiptUsages IS NOT EMPTY ORDER BY r.receiptId";

  /**
   * Gets the sponsor receipt list.
   *
   * @param billNo
   *          the bill no
   * @return the sponsor receipt list
   */
  public List<ReceiptModel> getSponsorReceiptList(String billNo) {
    Session session = getSession();
    Query query = session.createQuery(GET_SPONSOR_RECEIPTS_OF_BILL);
    query.setString("billNo", billNo);
    return query.list();
  }

  /** The Constant GET_BILL_RECEIPT. */
  private static final String GET_BILL_RECEIPT = "FROM BillReceiptsModel "
      + "WHERE billNo = :billNo AND receiptNo = :receiptNo";

  /**
   * Gets the bill receipt.
   *
   * @param receiptNo
   *          the receipt no
   * @param billNo
   *          the bill no
   * @return the bill receipt
   */
  public BillReceiptsModel getBillReceipt(String receiptNo, String billNo) {
    Session session = getSession();
    Query query = session.createQuery(GET_BILL_RECEIPT);
    query.setString("billNo", billNo);
    query.setString("receiptNo", receiptNo);
    return (BillReceiptsModel) query.uniqueResult();
  }
  
  /** The Constant GET_CANCELLED_BILL_CHARGE_RECEIPT_ALLOCATION. */
  private static final String GET_CANCELLED_BILL_CHARGE_RECEIPT_ALLOCATION = "SELECT bcra FROM "
      + "BillChargeReceiptAllocationModel AS bcra WHERE bcra.billReceipt = :billReceiptId "
      + "AND bcra.allocatedAmount < 0 "
      + "AND bcra.activity = 'c' AND bcra.refundReferenceId IS NULL ";
  
  /** The Constant GET_CANCELLED_RETURN_CHARGE_RECEIPT_ALLOCATION. */
  private static final String GET_CANCELLED_RETURN_CHARGE_RECEIPT_ALLOCATION = "SELECT bcra FROM "
      + "BillChargeReceiptAllocationModel AS bcra JOIN bcra.billCharge AS bc WHERE bcra.billReceipt = :billReceiptId "
      + "AND bcra.allocatedAmount < 0 AND bc.chargeHead.chargeheadId = :saleReturnName "
      + "AND bcra.activity = 'c' AND bcra.refundReferenceId IS NULL ";
  
  
  /**
   * Gets the cancelled bill charge receipt allocation.
   *
   * @param billReceiptId the bill receipt id
   * @param orderByAmount the order by amount
   * @return the cancelled bill charge receipt allocation
   */
  public List<BillChargeReceiptAllocationModel> getCancelledBillChargeReceiptAllocation(Object 
      billReceiptId, Boolean orderByAmount) {
    Session session = getSession();
    String orderByQuery = (Boolean.TRUE.equals(orderByAmount)) 
        ? " ORDER BY bcra.allocatedAmount DESC" : "";
    Query query = session.createQuery(GET_CANCELLED_BILL_CHARGE_RECEIPT_ALLOCATION 
          + orderByQuery);
    query.setLong("billReceiptId", Long.valueOf(billReceiptId.toString()));
    return query.list();
  }
  

  /**
   * Gets the cancelled bill charge receipt allocation.
   *
   * @param billReceiptId the bill receipt id
   * @param orderByAmount the order by amount
   * @param saleReturnName the sale return name
   * @return the cancelled bill charge receipt allocation
   */
  public List<BillChargeReceiptAllocationModel> getCancelledBillChargeReceiptAllocation(Object 
      billReceiptId, Boolean orderByAmount, String saleReturnName, Boolean isClaim) {
    Session session = getSession();
    String orderByQuery = (Boolean.TRUE.equals(orderByAmount)) 
        ? " ORDER BY bcra.allocatedAmount DESC" : "";
    String claimQuery = (Boolean.TRUE.equals(isClaim)) 
        ? " AND bcra.claimId IS NOT NULL " : " AND bcra.claimId IS NULL ";
    Query query = session.createQuery(GET_CANCELLED_RETURN_CHARGE_RECEIPT_ALLOCATION + claimQuery
        + orderByQuery);
    query.setLong("billReceiptId", Long.valueOf(billReceiptId.toString()));
    query.setString("saleReturnName", saleReturnName);
    return query.list();
  }

  /**
   * Gets the cancelled bill charge receipt allocation.
   *
   * @param billReceiptId the bill receipt id
   * @param orderByAmount the order by amount
   * @return the cancelled bill charge receipt allocation
   */
  public List<BillChargeReceiptAllocationModel> getCancelledBillChargeReceiptAllocation(
      Long billReceiptId, Boolean orderByAmount, Boolean isClaim) {
    Session session = getSession();
    String orderByQuery = (Boolean.TRUE.equals(orderByAmount)) 
        ? " ORDER BY bcra.allocatedAmount DESC" : "";
    String claimQuery = (Boolean.TRUE.equals(isClaim)) 
        ? " AND bcra.claimId IS NOT NULL " : " AND bcra.claimId IS NULL ";
    Query query;
      query = session.createQuery(GET_CANCELLED_BILL_CHARGE_RECEIPT_ALLOCATION + claimQuery
          + orderByQuery);            
    
    query.setLong("billReceiptId", billReceiptId);
    return query.list();
  }
  
  /** The Constant GET_BILL_RECEIPT_ID. */
  private static final String GET_BILL_RECEIPT_ID = "SELECT billReceiptId "
      + "FROM BillReceiptsModel WHERE billNo = :billNo AND receiptNo = :receiptNo";

  /**
   * Gets the bill receipt id.
   *
   * @param receiptNo
   *          the receipt no
   * @param billNo
   *          the bill no
   * @return the bill receipt id
   */
  public long getBillReceiptId(String receiptNo, String billNo) {
    Session session = getSession();
    Query query = session.createQuery(GET_BILL_RECEIPT_ID);
    query.setString("billNo", billNo);
    query.setString("receiptNo", receiptNo);
    return (long) query.uniqueResult();
  }

  /** The Constant DELETE_ALLOCATIONS_OF_BILL. */
  private static final String DELETE_ALLOCATIONS_OF_BILL = "DELETE "
      + "FROM BillChargeReceiptAllocationModel "
      + "WHERE id.chargeId IN (SELECT chargeId FROM BillChargeModel WHERE billNo = :billNo)";

  /**
   * Delete allocations of bill.
   *
   * @param billNo
   *          the bill no
   * @return the int
   */
  public int deleteAllocationsOfBill(String billNo) {
    Session session = getSession();
    Query query = session.createQuery(DELETE_ALLOCATIONS_OF_BILL);
    query.setString("billNo", billNo);
    return query.executeUpdate();

  }

  /** The Constant DELETE_ALLOCATIONS_USING_CHARGE. */
  private static final String DELETE_ALLOCATIONS_USING_CHARGE = "DELETE "
      + "FROM BillChargeReceiptAllocationModel bcra WHERE bcra.billCharge.chargeId = :chargeId";
  
  /**
   * Delete allocations of charge id.
   *
   * @param chargeId the charge id
   * @return the int
   */
  public void deleteAllocationsOfChargeId(String chargeId) {
    Session session = getSession();
    Query query = session.createQuery(DELETE_ALLOCATIONS_USING_CHARGE);
   
    query.setString("chargeId", chargeId);
    query.executeUpdate();
  }

  /** The Constant GET_ALLOCATED_AMOUNT. */
  private static final String GET_ALLOCATED_AMOUNT = "SELECT COALESCE(SUM(allocatedAmount), 0) "
      + "FROM BillReceiptsModel WHERE receiptNo = :receiptId";

  /**
   * Gets the allocated amount.
   *
   * @param receiptId
   *          the receipt id
   * @return the allocated amount
   */
  public BigDecimal getAllocatedAmount(String receiptId) {
    Session session = getSession();
    Query query = session.createQuery(GET_ALLOCATED_AMOUNT);
    query.setString("receiptId", receiptId);
    return (BigDecimal) query.uniqueResult();
  }

  /** The Constant GET_ALLOCATED_INSURANCE_AMOUNT. */
  private static final String GET_ALLOCATED_INSURANCE_AMOUNT = "SELECT "
      + "COALESCE(SUM(allocatedAmount), 0) " + "FROM BillChargeReceiptAllocationModel "
      + "WHERE billCharge = :chargeId AND claimId IS NOT NULL";

  /**
   * Gets the allocated insurance amount.
   *
   * @param chargeId
   *          the charge id
   * @return the allocated insurance amount
   */
  public BigDecimal getAllocatedInsuranceAmount(String chargeId) {
    Session session = getSession();
    Query query = session.createQuery(GET_ALLOCATED_INSURANCE_AMOUNT);
    query.setString("chargeId", chargeId);
    return (BigDecimal) query.uniqueResult();
  }

  /** The Constant GET_ALLOCATED_PATIENT_AMOUNT. */
  private static final String GET_ALLOCATED_PATIENT_AMOUNT = "SELECT "
      + "COALESCE(SUM(allocatedAmount), 0) " + "FROM BillChargeReceiptAllocationModel "
      + "WHERE billCharge = :chargeId AND claimId IS NULL";

  /**
   * Gets the allocated patient amount.
   *
   * @param chargeId
   *          the charge id
   * @return the allocated patient amount
   */
  public BigDecimal getAllocatedPatientAmount(String chargeId) {
    Session session = getSession();
    Query query = session.createQuery(GET_ALLOCATED_PATIENT_AMOUNT);
    query.setString("chargeId", chargeId);
    return (BigDecimal) query.uniqueResult();
  }
  
  /** The Constant GET_ALLOCATED_PATIENT_AMOUNT. */
  private static final String GET_ALLOCATED_DEPOSIT_PATIENT_AMOUNT = "SELECT "
      + "COALESCE(SUM(bcra.allocatedAmount), 0) " 
      + "FROM BillChargeReceiptAllocationModel bcra "
      + " JOIN bcra.billReceipt br JOIN br.receiptNo r WITH r.isDeposit = true "
      + "WHERE bcra.billCharge = :chargeId AND bcra.claimId IS NULL ";
  

  /**
   * Gets the allocated deposit patient amount.
   *
   * @param chargeId the charge id
   * @return the allocated deposit patient amount
   */
  public BigDecimal getAllocatedDepositPatientAmount(String chargeId) {
    Session session = getSession();
    Query query = session.createQuery(GET_ALLOCATED_DEPOSIT_PATIENT_AMOUNT);
    query.setString("chargeId", chargeId);
    return (BigDecimal) query.uniqueResult();
  }

  /** The Constant GET_PATIENT_AMOUNT_ALLOCATIONS. */
  private static final String GET_PATIENT_AMOUNT_ALLOCATIONS = "SELECT "
      + " new  map(COALESCE(SUM(bcra.allocatedAmount), 0) AS allocatedAmount, "
      + " bcra.billReceipt.billReceiptId AS billReceiptId, bcra.billCharge.chargeId AS chargeId )"
      + " FROM BillChargeReceiptAllocationModel bcra "
      + " WHERE bcra.billCharge = :chargeId AND bcra.claimId IS NULL "
      + " GROUP BY bcra.billReceipt.billReceiptId, bcra.billCharge.chargeId "
      + " ORDER BY bcra.billReceipt DESC";

  /**
   * Gets the patient amount allocations lifo.
   *
   * @param chargeId
   *          the charge id
   * @return the patient amount allocations lifo
   */
  public List<Map<String, Object>> getPatientAmountAllocationsLifo(String chargeId) {
    Session session = getSession();
    Query query = session.createQuery(GET_PATIENT_AMOUNT_ALLOCATIONS);
    query.setString("chargeId", chargeId);
    return (List<Map<String, Object>>) query.list();
  }
  
  /** The Constant GET_PATIENT_DEPOSIT_AMOUNT_ALLOCATIONS. */
  private static final String GET_PATIENT_DEPOSIT_AMOUNT_ALLOCATIONS = "SELECT "
      + " new  map(COALESCE(SUM(bcra.allocatedAmount), 0) AS allocatedAmount, "
      + " bcra.billReceipt.billReceiptId AS billReceiptId, bcra.billCharge.chargeId AS chargeId )"
      + " FROM BillChargeReceiptAllocationModel bcra "
      + " JOIN bcra.billReceipt br JOIN br.receiptNo r WITH r.isDeposit = true "
      + " WHERE bcra.billCharge = :chargeId AND bcra.claimId IS NULL "
      + " GROUP BY bcra.billReceipt.billReceiptId, bcra.billCharge.chargeId "
      + " ORDER BY bcra.billReceipt DESC";


  /**
   * Gets the patient deposit amount allocations lifo.
   *
   * @param chargeId the charge id
   * @return the patient deposit amount allocations lifo
   */
  public List<Map<String, Object>> getPatientDepositAmountAllocationsLifo(
      String chargeId) {
    Session session = getSession();
    Query query = session.createQuery(GET_PATIENT_DEPOSIT_AMOUNT_ALLOCATIONS);
    query.setString("chargeId", chargeId);
    return (List<Map<String, Object>>) query.list();
  }
  
  /** The Constant GET_BILL_CHARGE_ALLOCATIONS. */
  private static final String GET_BILL_CHARGE_ALLOCATIONS = "FROM "
      + "BillChargeReceiptAllocationModel "
      + "WHERE billCharge = :chargeId AND activity = 'c' AND refundReferenceId IS NULL "
      + "ORDER BY allocationId";
    
  /** The Constant GET_BILL_CHARGE_ALLOCATIONS_FOR_CHARGEID. */
  private static final String GET_BILL_CHARGE_ALLOCATIONS_FOR_CHARGEID = "FROM "
      + "BillChargeReceiptAllocationModel WHERE billCharge = :chargeId "
      + "ORDER BY allocationId";

  /**
   * Gets the bill charge allocations.
   *
   * @param chargeId the charge id
   * @return the bill charge allocations
   */
  public List<BillChargeReceiptAllocationModel> getBillChargeAllocations(String chargeId) {
    Session session = getSession();
    Query query = session.createQuery(GET_BILL_CHARGE_ALLOCATIONS_FOR_CHARGEID);
    query.setString("chargeId", chargeId);
    return query.list();
  }

  /** The Constant GET_SPONSOR_AMOUNT_ALLOCATIONS. */
  private static final String GET_SPONSOR_AMOUNT_ALLOCATIONS = "SELECT "
      + " new  map(COALESCE(SUM(bcra.allocatedAmount), 0) AS allocatedAmount, "
      + " bcra.billReceipt.billReceiptId AS billReceiptId, bcra.billCharge.chargeId AS chargeId,"
      + " bcra.claimId.claimId AS claimId )"
      + " FROM BillChargeReceiptAllocationModel bcra "
      + " WHERE bcra.billCharge = :chargeId AND bcra.claimId IS NOT NULL "
      + " GROUP BY bcra.billReceipt.billReceiptId, bcra.billCharge.chargeId, "
      + " bcra.claimId.claimId "
      + " ORDER BY bcra.billReceipt DESC";

  /**
   * Gets the sponsor amount allocations lifo.
   *
   * @param chargeId
   *          the charge id
   * @return the sponsor amount allocations lifo
   */
  public List<Map<String, Object>> getSponsorAmountAllocationsLifo(String chargeId) {
    Session session = getSession();
    Query query = session.createQuery(GET_SPONSOR_AMOUNT_ALLOCATIONS);
    query.setString("chargeId", chargeId);
    return (List<Map<String, Object>>) query.list();
  }

  /** The Constant GET_NON_DEPOSIT_BILL_RECEIPTS. */
  private static final String GET_NON_DEPOSIT_BILL_RECEIPTS = "SELECT br "
      + "FROM BillReceiptsModel br JOIN br.receiptNo r "
      + "WHERE br.billNo = :billNo AND r.isDeposit = false";
  
  /** The Constant GET_NON_DEPOSIT_BILL_RECEIPTS_ONLY_RECEIPTS. */
  private static final String GET_NON_DEPOSIT_BILL_RECEIPTS_ONLY_RECEIPTS = "SELECT br "
      + "FROM BillReceiptsModel br JOIN br.receiptNo r "
      + "WHERE br.billNo = :billNo AND r.isDeposit = false AND r.receiptType = 'R' "
      + "AND br.allocatedAmount != 0 ";

  
  /**
   * Gets the non deposit bill receipts.
   *
   * @param billNo the bill no
   * @return the non deposit bill receipts
   */
  public List<BillReceiptsModel> getNonDepositBillReceipts(String billNo) {
    return getNonDepositBillReceipts(billNo, false);
  }
  
  /** The Constant GET_BILL_RECEIPTS_FOR_DISCOUNT_CHARGES_INSURANCE. */
  private static final String GET_BILL_RECEIPTS_FOR_DISCOUNT_CHARGES_INSURANCE = "SELECT br "
      + "FROM BillReceiptsModel br JOIN br.receiptNo r "
      + "WHERE br.billNo = :billNo AND r.receiptType = 'R' "
      + "AND br.allocatedAmount != 0 AND r.tpaId IS NOT NULL ORDER BY r.receiptId DESC";
  
  /** The Constant GET_BILL_RECEIPTS_FOR_DISCOUNT_CHARGES. */
  private static final String GET_BILL_RECEIPTS_FOR_DISCOUNT_CHARGES = "SELECT br "
      + "FROM BillReceiptsModel br JOIN br.receiptNo r "
      + "WHERE br.billNo = :billNo AND r.receiptType = 'R' AND br.allocatedAmount != 0 "
      + "AND r.tpaId IS NULL ORDER BY r.receiptId DESC";
  

  /**
   * Gets the bill receipts for discount type charges.
   *
   * @param billNo the bill no
   * @param isTpa the tpa id
   * @return the bill receipts for discount type charges
   */
  public List<BillReceiptsModel> getBillReceiptsForDiscountTypeCharges(String billNo, 
      Boolean isTpa) {
    Session session = getSession();
    Query query;
    query = session.createQuery(GET_BILL_RECEIPTS_FOR_DISCOUNT_CHARGES);
    if (Boolean.TRUE.equals(isTpa)) {
      query = session.createQuery(GET_BILL_RECEIPTS_FOR_DISCOUNT_CHARGES_INSURANCE);
    }
    
    query.setString("billNo", billNo);
    return query.list();
  }
  
  

  /** The Constant GET_SALE_CHARGE_ALLOCATIONS. */
  private static final String GET_SALE_CHARGE_ALLOCATIONS = "SELECT "
      + "new map(COALESCE(SUM(bcra.allocatedAmount), 0) AS allocatedAmount, "
      + " bcra.billCharge.chargeId AS chargeId, bc.chargeHead.chargeheadId AS chargeHead ) FROM "
      + "BillChargeReceiptAllocationModel bcra JOIN bcra.billCharge bc "
      + "WHERE bcra.claimId IS NULL AND bc.chargeHead.chargeheadId IN (:saleType, :saleReturnType) "
      + " AND bcra.billReceipt = :billReceiptId  "
      + "GROUP BY bcra.billCharge.chargeId, bc.chargeHead.chargeheadId, bcra.allocationId "
      + "ORDER BY bcra.allocationId ";
  
  
  /**
   * Gets the bill receipts allocation with sale returns.
   *
   * @param billReceiptId the bill receipt id
   * @param saleType the sale type
   * @param saleReturnType the sale return type
   * @return the bill receipts allocation with sale returns
   */
  public List<Map<String, Object>> getBillReceiptsAllocationWithSaleReturns(Long billReceiptId, 
      String saleType, String saleReturnType) {
    Session session = getSession();
    Query query;
    query = session.createQuery(GET_SALE_CHARGE_ALLOCATIONS);
    query.setLong("billReceiptId", billReceiptId);
    query.setString("saleType", saleType);
    query.setString("saleReturnType", saleReturnType);
    return query.list();
  }
  
  /** The Constant GET_BILL_ACTIVITY_CHARGES. */
  private static final String GET_BILL_ACTIVITY_CHARGES = "SELECT bacm FROM BillChargeModel bcm "
      + " JOIN bcm.billActivityChargeModel bacm with bacm.activityCode='PHI' "
      + " WHERE bcm.chargeId = :chargeId ";

  

  /**
   * Gets the bill activity charges.
   *
   * @param chargeId the charge id
   * @return the bill activity charges
   */
  public BillActivityChargeModel getBillActivityCharges(String chargeId) {
    Session session = getSession();
    Query query;
    query = session.createQuery(GET_BILL_ACTIVITY_CHARGES);
    query.setString("chargeId", chargeId);
    return (BillActivityChargeModel) query.uniqueResult();
  }
  
  /**
   * Gets the non deposit bill receipts.
   *
   * @param billNo the bill no
   * @param onlyReceipts the only receipts
   * @return the non deposit bill receipts
   */
  public List<BillReceiptsModel> getNonDepositBillReceipts(String billNo, Boolean onlyReceipts) {
    Session session = getSession();
    Query query;
    
    query = session.createQuery(GET_NON_DEPOSIT_BILL_RECEIPTS);      
    
    if (onlyReceipts) {
      query = session.createQuery(GET_NON_DEPOSIT_BILL_RECEIPTS_ONLY_RECEIPTS);
    }
    query.setString("billNo", billNo);
    return query.list();
  }
  
  /** The Constant GET_DEPOSIT_BILL_RECEIPTS. */
  private static final String GET_DEPOSIT_BILL_RECEIPTS = "SELECT br "
      + "FROM BillReceiptsModel br JOIN br.receiptNo r "
      + "WHERE br.billNo = :billNo AND r.isDeposit = true";
  
  /**
   * Gets the non deposit bill receipts.
   *
   * @param billNo
   *          the bill no
   * @return the non deposit bill receipts
   */
  public List<BillReceiptsModel> getDepositBillReceipts(String billNo) {
    Session session = getSession();
    Query query = session.createQuery(GET_DEPOSIT_BILL_RECEIPTS);
    query.setString("billNo", billNo);
    return query.list();
  }

  /** The Constant GET_GENERAL_DEPOSIT_BILL_RECEIPTS. */
  private static final String GET_GENERAL_DEPOSIT_BILL_RECEIPTS = "SELECT br "
      + "FROM BillReceiptsModel br JOIN br.receiptNo r "
      + "LEFT JOIN r.receiptUsages ru "
      + "WHERE br.billNo = :billNo AND r.isDeposit = true "
      + "AND (r.receiptUsages  IS EMPTY OR ru.id.entityType = 'bill_type') "
      + "ORDER BY r.receiptId DESC";

  /**
   * Gets the general deposit bill receipts.
   *
   * @param billNo
   *          the bill no
   * @return the general deposit bill receipts
   */
  public List<BillReceiptsModel> getGeneralDepositBillReceipts(String billNo) {
    Session session = getSession();
    Query query = session.createQuery(GET_GENERAL_DEPOSIT_BILL_RECEIPTS);
    query.setString("billNo", billNo);
    return query.list();
  }

  /** The Constant GET_IP_DEPOSIT_BILL_RECEIPTS. */
  private static final String GET_IP_DEPOSIT_BILL_RECEIPTS = "SELECT br "
      + "FROM BillReceiptsModel br JOIN br.receiptNo r JOIN r.receiptUsages ru "
      + "WHERE br.billNo = :billNo AND r.isDeposit = true AND ru.id.entityType='visit_type' "
      + "AND ru.id.entityId='i' ORDER BY r.receiptId DESC";

  /**
   * Gets the ip deposit bill receipts.
   *
   * @param billNo
   *          the bill no
   * @return the ip deposit bill receipts
   */
  public List<BillReceiptsModel> getIpDepositBillReceipts(String billNo) {
    Session session = getSession();
    Query query = session.createQuery(GET_IP_DEPOSIT_BILL_RECEIPTS);
    query.setString("billNo", billNo);
    return query.list();
  }

  /** The Constant GET_PACKAGE_DEPOSIT_BILL_RECEIPTS. */
  private static final String GET_PACKAGE_DEPOSIT_BILL_RECEIPTS = "SELECT br "
      + "FROM BillReceiptsModel br JOIN br.receiptNo r JOIN r.receiptUsages ru "
      + "WHERE br.billNo = :billNo AND r.isDeposit = true AND ru.id.entityType='pat_package_id' "
      + "AND ru.id.entityId IS NOT NULL ORDER BY r.receiptId DESC";

  /**
   * Gets the package deposit bill receipts.
   *
   * @param billNo
   *          the bill no
   * @return the package deposit bill receipts
   */
  public List<BillReceiptsModel> getPackageDepositBillReceipts(String billNo) {
    Session session = getSession();
    Query query = session.createQuery(GET_PACKAGE_DEPOSIT_BILL_RECEIPTS);
    query.setString("billNo", billNo);
    return query.list();
  }

  /** The Constant SUM_OF_ALLOCATION_AMOUNT. */
  private static final String SUM_OF_ALLOCATION_AMOUNT = "SELECT "
      + "COALESCE(SUM(allocatedAmount), 0) " 
      + "FROM BillChargeReceiptAllocationModel "
      + "WHERE billReceipt = :billReceiptId ";

  /** The Constant SUM_OF_BILL_RECEIPT_ALLOCATED_AMOUNT. */
  private static final String SUM_OF_BILL_RECEIPT_ALLOCATED_AMOUNT = "SELECT "
      + " new  map(billReceipt.billReceiptId AS billReceiptId, "
      + "COALESCE(SUM(allocatedAmount), 0) AS allocatedAmount) " 
      + "FROM BillChargeReceiptAllocationModel "
      + "WHERE billReceipt.billNo.billNo = :billNo GROUP BY billReceipt.billReceiptId";

  /**
   * Calculate allocated amount.
   *
   * @param billReceiptId
   *          the bill receipt id
   * @return the big decimal
   */
  public BigDecimal calculateAllocatedAmount(long billReceiptId) {
    Session session = getSession();
    Query query = session.createQuery(SUM_OF_ALLOCATION_AMOUNT);
    query.setLong("billReceiptId", billReceiptId);
    return (BigDecimal) query.uniqueResult();
  }


  /**
   * Calculate bill receipt allocated amount.
   *
   * @param billNo the bill no
   * @return the list
   */
  public List<Map<String, Object>> calculateBillReceiptAllocatedAmount(String billNo) {
    Session session = getSession();
    Query query = session.createQuery(SUM_OF_BILL_RECEIPT_ALLOCATED_AMOUNT);
    query.setString("billNo", billNo);
    return (List<Map<String, Object>>) query.list();
  }

  /** The Constant SUM_OF_ALLOCATION_AMOUNT_OF_RECEIPT. */
  private static final String SUM_OF_ALLOCATION_AMOUNT_OF_RECEIPT = "SELECT "
      + "COALESCE(SUM(allocatedAmount), 0) "
      + "FROM BillReceiptsModel WHERE receiptNo = :receiptId";

  /**
   * Calculate allocated amount.
   *
   * @param receiptId
   *          the receipt id
   * @return the big decimal
   */
  public BigDecimal calculateAllocatedAmount(String receiptId) {
    Session session = getSession();
    Query query = session.createQuery(SUM_OF_ALLOCATION_AMOUNT_OF_RECEIPT);
    query.setString("receiptId", receiptId);
    return (BigDecimal) query.uniqueResult();
  }

  /** The Constant GET_REFUNDED_AMOUNT. */
  private static final String GET_REFUNDED_AMOUNT = "SELECT " + "COALESCE(SUM(amount), 0) "
      + "FROM ReceiptRefundReferenceModel " + "WHERE receipt= :receiptId";

  /**
   * Gets the refunded amount.
   *
   * @param receiptId
   *          the receipt id
   * @return the refunded amount
   */
  public BigDecimal getRefundedAmount(String receiptId) {
    Session session = getSession();
    Query query = session.createQuery(GET_REFUNDED_AMOUNT);
    query.setString("receiptId", receiptId);
    return (BigDecimal) query.uniqueResult();
  }

  /** The Constant GET_ALLOCATIONS_OF_RECEIPT. */
  private static final String GET_ALLOCATIONS_OF_RECEIPT = "SELECT "
      + "new map(COALESCE(SUM(bcra.allocatedAmount), 0) AS allocatedAmount,"
      + " bcra.billReceipt.billReceiptId AS billReceiptId, bcra.billCharge.chargeId AS chargeId, "
      + " bcra.claimId.claimId AS claimId) "
      + "FROM BillChargeReceiptAllocationModel bcra "
      + " JOIN bcra.billReceipt br WHERE br.receiptNo = :receiptId AND bcra.billCharge.amount > 0 " 
      + "GROUP BY br.receiptNo, bcra.billReceipt.billReceiptId, bcra.billCharge.chargeId, "
      + " bcra.claimId.claimId "
      + " ORDER BY bcra.billCharge.chargeId DESC";

  /**
   * Gets the allocations of receipt.
   *
   * @param receiptId the receipt id
   * @return the allocations of receipt
   */
  public List<Map<String, Object>> getAllocationsOfReceipt(String receiptId) {
    Session session = getSession();
    Query query = session.createQuery(GET_ALLOCATIONS_OF_RECEIPT);
    query.setString("receiptId", receiptId);
    return (List<Map<String, Object>>) query.list();
  }

  /** The Constant GET_ALLOCATIONS_OF_DEPOSIT. */
  private static final String GET_ALLOCATIONS_OF_DEPOSIT = "SELECT "
      + "new map(COALESCE(SUM(bcra.allocatedAmount), 0) AS allocatedAmount,"
      + " bcra.billReceipt.billReceiptId AS billReceiptId, bcra.billCharge.chargeId AS chargeId) "
      + "FROM BillChargeReceiptAllocationModel bcra "
      + " JOIN bcra.billReceipt br JOIN br.receiptNo r WITH r.isDeposit = true "
      + " WHERE r.receiptId = :depositId AND br.billNo = :billNo " 
      + "GROUP BY r.receiptId, bcra.billReceipt.billReceiptId, bcra.billCharge.chargeId, br.billNo"
      + " ORDER BY bcra.billReceipt DESC";

  
  /**
   * Gets the allocations of deposit.
   *
   * @param depositId the deposit id
   * @param billNo the bill no
   * @return the allocations of deposit
   */
  public List<Map<String, Object>> getAllocationsOfDeposit(String depositId,
      String billNo) {
    Session session = getSession();
    Query query = session.createQuery(GET_ALLOCATIONS_OF_DEPOSIT);
    query.setString("billNo", billNo);
    query.setString("depositId", depositId);
    return (List<Map<String, Object>>) query.list();
  }

  /** The Constant GET_BILL_LEVEL_RESTRICTIONS. */
  private static final String GET_BILL_LEVEL_RESTRICTIONS = "SELECT "
      + "new map(id.entityType as entityType, id.entityId as entityId) "
      + "FROM ReceiptUsageModel WHERE receipt = :receiptId";

  /**
   * Gets the bill level restrictions.
   *
   * @param receiptId
   *          the receipt id
   * @return the bill level restrictions
   */
  public List<Map<String, String>> getBillLevelRestrictions(String receiptId) {
    Session session = getSession();
    Query query = session.createQuery(GET_BILL_LEVEL_RESTRICTIONS);
    query.setString("receiptId", receiptId);
    return query.list();
  }
    
    
  private static final String GET_IP_BILL_LEVEL_RESTRICTIONS = "SELECT "
      + "new map(id.entityType as entityType, id.entityId as entityId) "
      + "FROM ReceiptUsageModel WHERE receipt.receiptId = :receiptId AND "
      + " id.entityType = 'visit_type' AND id.entityId = 'i' ";
    
  /**
   * Gets the bill level restrictions.
   *
   * @param receiptId
   *          the receipt id
   * @return the bill level restrictions
   */
  public List<Map<String, String>> getIpBillLevelRestrictions(String receiptId) {
      Session session = getSession();
      Query query = session.createQuery(GET_IP_BILL_LEVEL_RESTRICTIONS);
      query.setString("receiptId", receiptId);
      return (List<Map<String, String>>) query.list();
  }

  /** The Constant GET_DEPOSITS. */
  private static final String GET_DEPOSITS = "FROM ReceiptModel "
      + "WHERE mrNo = :mrNo AND receiptType = 'R' AND isDeposit = true ORDER BY receiptId";

  /**
   * Gets the deposit list.
   *
   * @param mrNo
   *          the mr no
   * @param lifo
   *          the lifo
   * @return the deposit list
   */
  public List<ReceiptModel> getDepositList(String mrNo, boolean lifo) {
    Session session = getSession();
    String order = "";
    if (lifo) {
      order = " DESC";
    }
    Query query = session.createQuery(GET_DEPOSITS + order);
    query.setString("mrNo", mrNo);
    return query.list();
  }

  /** The Constant GET_GENERAL_DEPOSITS. */
  private static final String GET_GENERAL_DEPOSITS = "SELECT r FROM ReceiptModel r "
      + "LEFT JOIN r.receiptUsages ru " + "WHERE r.mrNo = :mrNo AND r.isDeposit = true "
      + "AND (r.receiptUsages IS EMPTY OR ru.id.entityType = 'bill_type') AND r.receiptType = 'R' "
      + "ORDER BY r.receiptId";

  /**
   * Gets the general deposit list.
   *
   * @param mrNo
   *          the mr no
   * @param lifo
   *          the lifo
   * @return the general deposit list
   */
  public List<ReceiptModel> getGeneralDepositList(String mrNo, boolean lifo) {
    Session session = getSession();
    String order = "";
    if (lifo) {
      order = " DESC";
    }
    Query query = session.createQuery(GET_GENERAL_DEPOSITS + order);
    query.setString("mrNo", mrNo);
    return query.list();
  }


  /** The Constant GET_CENTER_WISE_GENERAL_DEPOSITS. */
  private static final String GET_CENTER_WISE_GENERAL_DEPOSITS = "SELECT r FROM ReceiptModel r "
	      + "LEFT JOIN r.receiptUsages ru " + "WHERE r.mrNo = :mrNo AND r.isDeposit = true "
	      + "AND (r.receiptUsages IS EMPTY OR ru.id.entityType = 'bill_type') AND r.receiptType = 'R' "
	      + "AND (r.centerId IS NULL OR r.centerId=:centerId)"
	      + "ORDER BY r.receiptId";

	  /**
	   * Gets the general deposit list.
	   *
	   * @param mrNo
	   *          the mr no
	   * @param centerId
	   *          the center no
	   * @param lifo
	   *          the lifo
	   * @return the general deposit list
	   */
	  public List<ReceiptModel> getGeneralDepositList(String mrNo,int centerId, boolean lifo) {
	    Session session = getSession();
	    String order = "";
	    if (lifo) {
	      order = " DESC";
	    }
	    Query query = session.createQuery(GET_CENTER_WISE_GENERAL_DEPOSITS + order);
	    query.setString("mrNo", mrNo);
	    query.setInteger("centerId", centerId);
	    return query.list();
	  }


  /** The Constant GET_IP_DEPOSITS. */
  private static final String GET_IP_DEPOSITS = "SELECT r "
      + "FROM ReceiptUsageModel ru JOIN ru.receipt r WITH r.mrNo = :mrNo "
      + "WHERE ru.id.entityType = 'visit_type' AND r.receiptType = 'R' AND ru.id.entityId = 'i' "
      + "ORDER BY r.receiptId";

  /**
   * Gets the ip deposit list.
   *
   * @param mrNo
   *          the mr no
   * @param lifo
   *          the lifo
   * @return the ip deposit list
   */
  public List<ReceiptModel> getIpDepositList(String mrNo, boolean lifo) {
    Session session = getSession();
    String order = "";
    if (lifo) {
      order = " DESC";
    }
    Query query = session.createQuery(GET_IP_DEPOSITS + order);
    query.setString("mrNo", mrNo);
    return query.list();
  }


  /** The Constant GET_CENTER_WISE_IP_DEPOSITS. */
  private static final String GET_CENTER_WISE_IP_DEPOSITS= "SELECT r "
	      + "FROM ReceiptUsageModel ru JOIN ru.receipt r WITH r.mrNo = :mrNo "
	      + "WHERE ru.id.entityType = 'visit_type' AND r.receiptType = 'R' AND ru.id.entityId = 'i' "
	      + "AND (r.centerId IS NULL OR r.centerId=:centerId)"
	      + "ORDER BY r.receiptId";

	  /**
	   * Gets the center wise ip deposit list .
	   *
	   * @param mrNo
	   *          the mr no
	   *  @param centerId
	   *          the center Id
	   * @param lifo
	   *          the lifo
	   * @return the ip deposit list
	   */
	  public List<ReceiptModel> getIpDepositList(String mrNo,int centerId, boolean lifo) {
	    Session session = getSession();
	    String order = "";
	    if (lifo) {
	      order = " DESC";
	    }
	    Query query = session.createQuery(GET_CENTER_WISE_IP_DEPOSITS + order);
	    query.setString("mrNo", mrNo);
	    query.setInteger("centerId", centerId);
	    return query.list();
	  }

  /** The Constant GET_PACKAGE_DEPOSITS. */
  private static final String GET_PACKAGE_DEPOSITS = "SELECT r "
      + "FROM ReceiptUsageModel ru JOIN ru.receipt r WITH r.mrNo = :mrNo "
      + "WHERE ru.id.entityType = 'pat_package_id' AND r.receiptType = 'R' ORDER BY r.receiptId";

  /**
   * Gets the package deposit list.
   *
   * @param mrNo
   *          the mr no
   * @param lifo
   *          the lifo
   * @return the package deposit list
   */
  public List<ReceiptModel> getPackageDepositList(String mrNo, boolean lifo) {
    Session session = getSession();
    String order = "";
    if (lifo) {
      order = " DESC";
    }
    Query query = session.createQuery(GET_PACKAGE_DEPOSITS + order);
    query.setString("mrNo", mrNo);
    return query.list();
  }


  /** The Constant GET_CENTER_WISE_PACKAGE_DEPOSITS. */
  private static final String GET_CENTER_WISE_PACKAGE_DEPOSITS = "SELECT r "
	      + "FROM ReceiptUsageModel ru JOIN ru.receipt r WITH r.mrNo = :mrNo "
	      + "WHERE ru.id.entityType = 'pat_package_id' AND r.receiptType = 'R' "
	      + "AND (r.centerId IS NULL OR r.centerId=:centerId)"
	      + "ORDER BY r.receiptId";

	  /**
  	 * Gets the package deposit list.
  	 *
  	 * @param mrNo          the mr no
  	 * @param centerId the center id
  	 * @param lifo          the lifo
  	 * @return the package deposit list
  	 */
	  public List<ReceiptModel> getCenterWisePackageDepositList(String mrNo, int centerId,boolean lifo) {
	    Session session = getSession();
	    String order = "";
	    if (lifo) {
	      order = " DESC";
	    }
	    Query query = session.createQuery(GET_CENTER_WISE_PACKAGE_DEPOSITS + order);
	    query.setString("mrNo", mrNo);
	    query.setInteger("centerId", centerId);
	    return query.list();
	  }


  /** The Constant GET_PACKAGE_DEPOSITS_BY_PACKAGE_ID. */
  private static final String GET_PACKAGE_DEPOSITS_BY_PACKAGE_ID = "SELECT r "
      + "FROM ReceiptUsageModel ru JOIN ru.receipt r WITH r.mrNo = :mrNo "
      + "WHERE ru.id.entityType = 'package_id' AND r.receiptType = 'R' "
      + "AND ru.id.entityId = :packageId ORDER BY r.receiptId";

  /**
   * Gets the package deposit list.
   *
   * @param mrNo
   *          the mr no
   * @param packageId
   *          the package id
   * @param lifo
   *          the lifo
   * @return the package deposit list
   */
  public List<ReceiptModel> getPackageDepositList(String mrNo, int packageId, boolean lifo) {
    Session session = getSession();
    String order = "";
    if (lifo) {
      order = " DESC";
    }
    Query query = session.createQuery(GET_PACKAGE_DEPOSITS_BY_PACKAGE_ID + order);
    query.setString("mrNo", mrNo);
    query.setString("packageId", String.valueOf(packageId));
    return query.list();
  }


  /** The Constant GET_CENTER_WISE_PACKAGE_DEPOSITS_BY_PACKAGE_ID. */
  private static final String GET_CENTER_WISE_PACKAGE_DEPOSITS_BY_PACKAGE_ID = "SELECT r "
	      + "FROM ReceiptUsageModel ru JOIN ru.receipt r WITH r.mrNo = :mrNo "
	      + "WHERE ru.id.entityType = 'package_id' AND r.receiptType = 'R' "
	      + "AND ru.id.entityId = :packageId "
	      + "AND (r.centerId IS NULL OR r.centerId=:centerId)"
	      + "ORDER BY r.receiptId ";
	  /**
	   * Gets the center wise package deposit list.
	   *
	   * @param mrNo
	   *          the mr no
	   * @param packageId
	   *          the package id
	   * @param centerId
	   *          the center id
	   * @param lifo
	   *          the lifo
	   * @return the package deposit list
	   */
	  public List<ReceiptModel> getPackageDepositList(String mrNo, int packageId,int centerId, boolean lifo) {
	    Session session = getSession();
	    String order = "";
	    if (lifo) {
	      order = " DESC";
	    }
	    Query query = session.createQuery(GET_CENTER_WISE_PACKAGE_DEPOSITS_BY_PACKAGE_ID + order);
	    query.setString("mrNo", mrNo);
	    query.setString("packageId", String.valueOf(packageId));
	    query.setInteger("centerId", centerId);
	    return query.list();
	  }

  /** The Constant GET_TOTAL_RECEIPT_AMOUNT. */
  private static final String GET_TOTAL_RECEIPT_AMOUNT = "SELECT (COALESCE(SUM(r.amount),0) + "
      + "COALESCE(SUM(r.tdsAmount),0) + COALESCE(SUM(r.otherDeductions),0))"
      + " FROM ReceiptUsageModel ru JOIN ru.receipt r "
      + "WHERE ru.id.entityType = 'bill_no' AND ru.id.entityId = :billNo "
      + "AND r.isDeposit = false AND r.tpaId IS NULL AND r.receiptType != 'W'"
      + "AND r.paymentModeId != -9 ";

  private static final String GET_TOTAL_ALLOCATED_AMOUNT_FOR_INS_EXT_ENABLED = "SELECT "
      + "COALESCE(SUM(br.allocatedAmount), 0) " + "FROM BillReceiptsModel br JOIN br.receiptNo r "
      + "WHERE br.billNo = :billNo AND r.isDeposit = false AND r.tpaId IS NULL "
      + "AND r.receiptType != 'W' AND r.paymentModeId != -9";

  /**
   * Calculate total receipt amount.
   *
   * @param billNo
   *          the bill no
   * @param isModInsExtEnabled if the module `mod_ins_ext` is enabled then set to true else false
   * @return the big decimal
   */
  public BigDecimal calculateTotalReceiptAmount(String billNo, boolean isModInsExtEnabled) {
    Session session = getSession();
    Query query;
    if (isModInsExtEnabled) {
      query = session.createQuery(GET_TOTAL_ALLOCATED_AMOUNT_FOR_INS_EXT_ENABLED);
    } else {
      query = session.createQuery(GET_TOTAL_RECEIPT_AMOUNT);
    }
    query.setString("billNo", billNo);
    return (BigDecimal) query.uniqueResult();
  }

  /** The Constant GET_TOTAL_ALLOCATED_DEPOSIT_AMOUNT. */
  private static final String GET_TOTAL_ALLOCATED_DEPOSIT_AMOUNT = "SELECT "
      + "COALESCE(SUM(br.allocatedAmount), 0) " + "FROM BillReceiptsModel br JOIN br.receiptNo r "
      + "WHERE br.billNo = :billNo AND r.isDeposit = true";

  /**
   * Calculate deposit amount.
   *
   * @param billNo
   *          the bill no
   * @return the big decimal
   */
  public BigDecimal calculateDepositAmount(String billNo) {
    Session session = getSession();
    Query query = session.createQuery(GET_TOTAL_ALLOCATED_DEPOSIT_AMOUNT);
    query.setString("billNo", billNo);
    return (BigDecimal) query.uniqueResult();
  }

  /** The Constant GET_PACKAGE_RESTRICTION. */
  private static final String GET_PACKAGE_RESTRICTION = "SELECT id.entityId "
      + "FROM ReceiptUsageModel "
      + "WHERE receipt = :receiptId AND id.entityType = 'pat_package_id'";

  /**
   * Gets the package restriction.
   *
   * @param receiptId
   *          the receipt id
   * @return the package restriction
   */
  public String getPackageRestriction(String receiptId) {
    Session session = getSession();
    Query query = session.createQuery(GET_PACKAGE_RESTRICTION);
    query.setString("receiptId", receiptId);
    return (String) query.uniqueResult();
  }

  /** The Constant GET_ALLOCATED_AMOUNT_FOR_CHARGES. */
  private static final String GET_ALLOCATED_AMOUNT_FOR_CHARGES = "SELECT "
      + "new map(COALESCE(SUM(bcra.allocatedAmount), 0) AS allocatedAmount,"
      + " br.billReceiptId AS billReceiptId ) FROM BillChargeReceiptAllocationModel bcra "
      + "JOIN bcra.billReceipt br JOIN br.receiptNo r WITH r.isDeposit = true "
      + "AND r.mrNo IS NOT NULL WHERE bcra.billCharge = :chargeId " + "GROUP BY br.billReceiptId";

  /**
   * Gets the allocations for charge.
   *
   * @param chargeId
   *          the charge id
   * @return the allocations for charge
   */
  public List<Map<String, Object>> getAllocationsForCharge(String chargeId) {
    Session session = getSession();
    Query query = session.createQuery(GET_ALLOCATED_AMOUNT_FOR_CHARGES);
    query.setString("chargeId", chargeId);
    return (List<Map<String, Object>>) query.list();
  }
  
  /** The Constant GET_REVERSED_ALLOCATED_AMOUNT_FOR_BILL. */
  private static final String GET_REVERSED_ALLOCATED_AMOUNT_FOR_BILL = "SELECT bcra FROM "
      + "BillChargeReceiptAllocationModel AS bcra "
      + "JOIN bcra.billReceipt AS br JOIN br.receiptNo AS r WITH r.isDeposit = false "
      + "WHERE bcra.claimId IS NULL AND bcra.refundReferenceId IS NULL "
      + " AND br.billNo = :billNo AND bcra.allocatedAmount < 0 "
      + "ORDER BY bcra.activity DESC, bcra.allocationId DESC";
  
  /** The Constant GET_REVERSED_ALLOCATED_AMOUNT_FOR_BILLRECEIPTID. */
  private static final String GET_REVERSED_ALLOCATED_AMOUNT_FOR_BILLRECEIPTID = "SELECT bcra FROM "
      + "BillChargeReceiptAllocationModel AS bcra "
      + "JOIN bcra.billReceipt AS br JOIN br.receiptNo AS r WITH r.isDeposit = false "
      + "WHERE bcra.claimId IS NULL AND bcra.refundReferenceId IS NULL "
      + " AND br.billNo = :billNo AND bcra.allocatedAmount < 0 "
      + " AND bcra.billReceipt = :billReceiptId "
      + "ORDER BY bcra.activity DESC, bcra.allocationId DESC";
  


  /**
   * Gets the reversed allocated amount for bill.
   *
   * @param billNo the bill no
   * @param billReceiptId the bill receipt id
   * @return the reversed allocated amount for bill
   */
  public List<BillChargeReceiptAllocationModel> getReversedAllocatedAmountForBill(String billNo, 
      Long billReceiptId) {
    Session session = getSession();
    Query query;

    if (billReceiptId > 0) {
      query = session.createQuery(GET_REVERSED_ALLOCATED_AMOUNT_FOR_BILLRECEIPTID);
      query.setLong("billReceiptId", billReceiptId);
    } else {
      query = session.createQuery(GET_REVERSED_ALLOCATED_AMOUNT_FOR_BILL);
    }
    query.setString("billNo", billNo);
    return (List<BillChargeReceiptAllocationModel>) query.list();
  }
  
  /** The Constant GET_REVERSED_ALLOCATED_SUM_FOR_BILL. */
  private static final String GET_REVERSED_ALLOCATED_SUM_FOR_BILL = "SELECT "
      + "new map(COALESCE(SUM(bcra.allocatedAmount), 0) AS allocatedAmount,"
      + " br.billReceiptId AS billReceiptId, r.receiptId AS receiptId ) FROM BillChargeReceiptAllocationModel bcra "
      + "JOIN bcra.billReceipt AS br JOIN br.receiptNo AS r WITH r.isDeposit = false "
      + "WHERE bcra.claimId IS NULL AND bcra.refundReferenceId IS NULL "
      + " AND br.billNo = :billNo AND bcra.allocatedAmount < 0 AND bcra.activity = 'c' "
      + "GROUP BY br.billReceiptId,r.receiptId ORDER BY br.billReceiptId";
  
  
  /**
   * Gets the reversed allocated sum for bill.
   *
   * @param billNo the bill no
   * @return the reversed allocated sum for bill
   */
  public List<Map<String, Object>> getReversedAllocatedSumForBill(String billNo) {
    Session session = getSession();
    Query query = session.createQuery(GET_REVERSED_ALLOCATED_SUM_FOR_BILL);
    query.setString("billNo", billNo);
    return (List<Map<String, Object>>) query.list();
  }
  
  /** The Constant GET_BILL_CHARGES_WITH_RP. */
  private static final String GET_BILL_CHARGES_WITH_RP = "FROM BillChargeModel "
      + "WHERE billNo = :billNo AND redeemedPoints > 0";

  /**
   * Gets the bill charges with redeemed points.
   *
   * @param billNo
   *          the bill no
   * @return the bill charges with redeemed points
   */
  public List<BillChargeModel> getBillChargesWithRedeemedPoints(String billNo) {
    Session session = getSession();
    Query query = session.createQuery(GET_BILL_CHARGES_WITH_RP);
    query.setString("billNo", billNo);
    return query.list();
  }

  /** The Constant GET_REWARD_POINTS_RECEIPT. */
  private static final String GET_REWARD_POINTS_RECEIPT = "SELECT r "
      + "FROM BillReceiptsModel br JOIN br.receiptNo r "
      + "WHERE r.paymentModeId = -9 AND br.billNo = :billNo";

  /**
   * Gets the reward point receipts.
   *
   * @param billNo
   *          the bill no
   * @return the reward point receipts
   */
  public List<ReceiptModel> getRewardPointReceipts(String billNo) {
    Session session = getSession();
    Query query = session.createQuery(GET_REWARD_POINTS_RECEIPT);
    query.setString("billNo", billNo);
    return query.list();
  }

  /** The Constant CALCULATE_PRIMARY_TOTAL_SR. */
  private static final String CALCULATE_PRIMARY_TOTAL_SR = "SELECT COALESCE("
      + "(SELECT COALESCE( SUM(COALESCE(r.amount, 0) + COALESCE(r.tdsAmount,0) "
      + " + COALESCE(r.otherDeductions,0)), 0 ) "
      + "FROM BillReceiptsModel br JOIN br.receiptNo r "
      + "WHERE br.billNo = :billNo AND r.tpaId IS NOT NULL AND br.sponsorIndex = 'P'), 0)";

  /**
   * Calculate primary total sponsor receipts.
   *
   * @param billNo
   *          the bill no
   * @return the big decimal
   */
  public BigDecimal calculatePrimaryTotalSponsorReceipts(String billNo) {
    Session session = getSession();
    Query query = session.createQuery(CALCULATE_PRIMARY_TOTAL_SR);
    query.setString("billNo", billNo);
    return (BigDecimal) query.uniqueResult();
  }

  /** The Constant CALCULATE_TOTAL_SR. */
  private static final String CALCULATE_TOTAL_SR = "SELECT "
      + "COALESCE( SUM(COALESCE(r.amount, 0) + COALESCE(r.tdsAmount,0) "
      + " + COALESCE(r.otherDeductions,0)), 0 ) "
      + "FROM BillReceiptsModel br JOIN br.receiptNo r "
      + "WHERE br.billNo = :billNo AND r.tpaId IS NOT NULL AND br.sponsorIndex = :sponsorIndex";

  /**
   * Calculate total sponsor receipts.
   *
   * @param billNo
   *          the bill no
   * @param isPrimarySponsor
   *          the is primary sponsor
   * @return the big decimal
   */
  public BigDecimal calculateTotalSponsorReceipts(String billNo, boolean isPrimarySponsor) {
    Session session = getSession();
    Query query = session.createQuery(CALCULATE_TOTAL_SR);
    query.setString("billNo", billNo);
    if (isPrimarySponsor) {
      query.setString("sponsorIndex", "P");
    } else {
      query.setString("sponsorIndex", "S");
    }
    return (BigDecimal) query.uniqueResult();
  }

  /** The Constant GET_SR_COUNT. */
  private static final String GET_SR_COUNT = "SELECT COUNT(1) "
      + "FROM BillReceiptsModel br JOIN br.receiptNo r "
      + "WHERE br.billNo = :billNo AND r.tpaId IS NOT NULL AND br.sponsorIndex = :sponsorIndex "
      + "AND r.receiptType != 'W'";

  /**
   * Gets the sponsor receipt count.
   *
   * @param billNo
   *          the bill no
   * @param isPrimarySponsor
   *          the is primary sponsor
   * @return the sponsor receipt count
   */
  public long getSponsorReceiptCount(String billNo, boolean isPrimarySponsor) {
    Session session = getSession();
    Query query = session.createQuery(GET_SR_COUNT);
    query.setString("billNo", billNo);
    if (isPrimarySponsor) {
      query.setString("sponsorIndex", "P");
    } else {
      query.setString("sponsorIndex", "S");
    }
    return (long) query.uniqueResult();
  }
  
  /** The Constant GET_PATIENT_RECEIPT_COUNT. */
  private static final String GET_PATIENT_RECEIPT_COUNT = "SELECT COUNT(1) "
      + "FROM BillReceiptsModel br JOIN br.receiptNo r "
      + "WHERE br.billNo = :billNo AND r.tpaId IS NULL AND r.isDeposit = false "
      + "AND r.receiptType != 'W'";

  /**
   * Gets the Patient receipt count.
   *
   * @param billNo
   *          the bill no
   * @return the patient receipt count
   */
  public long getPatientReceiptCount(String billNo) {
    Session session = getSession();
    Query query = session.createQuery(GET_PATIENT_RECEIPT_COUNT);
    query.setString("billNo", billNo);
    return (long) query.uniqueResult();
  }

  /** The Constant GET_LATEST_MODIFIED_RECEIPT_ID. */
  private static final String GET_LATEST_MODIFIED_RECEIPT_ID = "SELECT r.receiptId "
      + "FROM BillReceiptsModel br JOIN br.receiptNo r "
      + "WHERE br.billNo = :billNo AND r.isDeposit = false AND r.receiptType != 'W' "
      + "ORDER BY br.modTime DESC";

  /**
   * Gets the latest receipt id.
   *
   * @param billNo
   *          the bill no
   * @return the latest receipt id
   */
  public String getLatestReceiptId(String billNo) {
    Session session = getSession();
    Query query = session.createQuery(GET_LATEST_MODIFIED_RECEIPT_ID);
    query.setString("billNo", billNo);
    query.setMaxResults(1);
    return (String) query.uniqueResult();
  }

  private static final String GET_PACKAGE_ID_OF_BILL = "SELECT pack_id as package_id "
      + "FROM multivisit_bills_view WHERE bill_no = :billNo AND pat_package_id = :patPackId";

  /**
   * Gets the package id of bill.
   *
   * @param billNo
   *          the bill no
   * @param patPackId
   *          the pat_package_id
   * @return the package id of bill
   */
  public String getPackageIdOfBill(String billNo, Integer patPackId) {
    Session session = getSession();
    SQLQuery query = session.createSQLQuery(GET_PACKAGE_ID_OF_BILL);
    query.setString("billNo", billNo);
    query.setInteger("patPackId", patPackId);
    query.setMaxResults(1);
    return (query.uniqueResult() != null) ? Integer.toString((int) query.uniqueResult()) : "";
  }

  /** The Constant GET_WRITEOFF_RECEIPT. */
  private static final String GET_WRITEOFF_RECEIPT = "SELECT r "
      + "FROM BillReceiptsModel br JOIN br.receiptNo r ON r.receiptType = 'W' "
      + "WHERE br.billNo = :billNo AND r.unallocatedAmount != 0";

  /**
   * Gets the write off receipt.
   *
   * @param billNo the bill no
   * @return the write off receipt
   */
  public List<ReceiptModel> getWriteOffReceipt(String billNo) {
    Session session = getSession();
    Query query = session.createQuery(GET_WRITEOFF_RECEIPT);
    query.setString("billNo", billNo);
    return (List<ReceiptModel>) query.list();
  }

  /**
   * Gets the next writeoff refund receipt id.
   *
   * @return the next writeoff refund receipt id
   */
  public String getNextWriteoffRefundReceiptId() {
    Session session = getSession();
    return HibernateHelper.generateNextId(session, "WRITEOFF_REFUND");
  }
  
  /** The Constant GET_SPONSOR_AMOUNT. */
  private static final String GET_SPONSOR_AMOUNT = "SELECT "
      + "COALESCE(SUM(COALESCE(r.amount, 0) + COALESCE(r.tdsAmount, 0) "
      + "+ COALESCE(r.otherDeductions,0)),0) "
      + "FROM ReceiptUsageModel ru JOIN ru.receipt r JOIN r.billReceipts br "
      + "WHERE ru.id.entityType = 'bill_no' AND ru.id.entityId = :billNo "
      + "AND r.receiptType != 'W' AND r.isDeposit = false AND r.tpaId IS NOT NULL "
      + "AND br.sponsorIndex=:sponsorIndex";

  /**
   * Gets the unallocated sponsor amount.
   *
   * @param billNo
   *          the bill no
   * @param isPrimarySponsor
   *          the is primary sponsor
   * @return the unallocated sponsor amount
   */
  public BigDecimal getSponsorAmount(String billNo, boolean isPrimarySponsor) {
    Session session = getSession();
    Query query = session.createQuery(GET_SPONSOR_AMOUNT);
    query.setString("billNo", billNo);
    if (isPrimarySponsor) {
      query.setString("sponsorIndex", "P");
    } else {
      query.setString("sponsorIndex", "S");
    }
    return (BigDecimal) query.uniqueResult();
  }
  
  /** The Constant GET_ALL_OPEN_BILLS_WITH_CANCELLED_CHARGES. */
  private static final String GET_ALL_OPEN_BILLS_WITH_CANCELLED_CHARGES = "SELECT "
      + "DISTINCT(b.billNo) "
      + "FROM BillModel b JOIN b.billCharges bc "
      + "WHERE b.status='A' AND bc.status='X' "
      + "AND bc.amount != 0";

  /**
   * Gets the all open bills.
   *
   * @return the all open bills
   */
  public List<String> getAllOpenBills() {
    Session session = getSession();
    Query query = session.createQuery(GET_ALL_OPEN_BILLS_WITH_CANCELLED_CHARGES);
    return query.list();
  }

  private static final String GET_ALL_OPEN_BILLS_FOR_DAVITA = "SELECT "
      + "DISTINCT(b.billNo) "
      + "FROM BillModel b JOIN b.billCharges bc "
      + "WHERE open_date >= :fromDate AND open_date <= :toDate "
      + "AND bc.amount != 0";

  public List<String> getAllOpenBillsForDavita(Date startDate, Date endDate) {
    Session session = getSession();
    List<Object> params = new ArrayList<>();
    Query query = session.createQuery(GET_ALL_OPEN_BILLS_FOR_DAVITA);
    query.setDate("fromDate", new java.sql.Date(startDate.getTime()));
    query.setDate("toDate", new java.sql.Date(endDate.getTime()));
    return query.list();
  }

  /** The Constant GET_UNALLOCATED_DISCOUNTED_CHARGES. */
  private static final String GET_UNALLOCATED_DISCOUNTED_CHARGES = "SELECT bc "
      + "FROM BillChargeModel bc LEFT JOIN bc.billChargeReceiptAllocation bcra "
      + "WHERE bc.billNo = :billNo AND bc.status = 'A' "
      + "AND bcra.allocationId IS NULL AND bcra.claimId IS NULL ORDER BY bc.chargeId";
  
  /** The Constant GET_UNALLOCATED_DISCOUNTED_CHARGES_AND_CLM. */
  private static final String GET_UNALLOCATED_DISCOUNTED_CHARGES_AND_CLM = "SELECT bc "
      + "FROM BillChargeModel bc LEFT JOIN bc.billChargeReceiptAllocation bcra "
      + "WHERE bc.billNo = :billNo AND bc.status = 'A' "
      + "AND bcra.allocationId IS NULL AND bcra.claimId IS NOT NULL ORDER BY bc.chargeId";

  
  /**
   * Gets the unallocated discount charges.
   *
   * @param billNo the bill no
   * @return the unallocated discount charges
   */
  public List<BillChargeModel> getUnallocatedDiscountCharges(String billNo, boolean isPatient) {
    Session session = getSession();
    Query query;
    if (isPatient) {
      query = session.createQuery(GET_UNALLOCATED_DISCOUNTED_CHARGES);      
    } else {
      query = session.createQuery(GET_UNALLOCATED_DISCOUNTED_CHARGES_AND_CLM);
    }
    query.setString("billNo", billNo);
    return query.list();    
  }

  /** The Constant GET_SALE_STORE_DETAILS_WITH_CHARGE. */
  private static final String GET_SALE_STORE_DETAILS_WITH_CHARGE = "SELECT ssd FROM StoreSalesMainModel ssm "
      + " JOIN ssm.storeSalesDetails AS ssd "
      + " WHERE ssm.chargeId = :chargeId";
  
  /**
   * Gets the sale store details for charge.
   *
   * @param chargeId the charge id
   * @return the sale store details for charge
   */
  public StoreSalesDetailsModel getSaleStoreDetailsForCharge(String chargeId) {
    Session session = getSession();
    Query query;
    query = session.createQuery(GET_SALE_STORE_DETAILS_WITH_CHARGE);
    query.setString("chargeId", chargeId);
    return (StoreSalesDetailsModel) query.uniqueResult();
  }


  /** The Constant GET_SALE_STORE_DETAILS_WITH_CHARGE. */
  private static final String GET_SALE_STORE_DETAILS_WITH_CHARGE_MEDICINE = "SELECT ssd FROM "
      + " StoreSalesMainModel ssm JOIN ssm.storeSalesDetails AS ssd "
      + " WHERE ssm.chargeId = :chargeId AND ssd.medicineId = :medicineId "
      + " AND ssd.itemBatchId = :batch";
  
  /**
   * Gets the sale store details for charge.
   *
   * @param chargeId the charge id
   * @param medicineId the medicine id
   * @param batchNo the batch no
   * @return the sale store details for charge
   */
  public List<StoreSalesDetailsModel> getSaleStoreDetailsForCharge(String chargeId, 
      Integer medicineId, Integer batchNo) {
    Session session = getSession();
    Query query;
    if (medicineId == 0) {
      query = session.createQuery(GET_SALE_STORE_DETAILS_WITH_CHARGE);
    } else {
      query = session.createQuery(GET_SALE_STORE_DETAILS_WITH_CHARGE_MEDICINE);
      query.setInteger("medicineId", medicineId);
      query.setInteger("batch", batchNo);
    }
    query.setString("chargeId", chargeId);
    return query.list();
  }
}
