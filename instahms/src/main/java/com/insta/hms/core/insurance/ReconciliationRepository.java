package com.insta.hms.core.insurance;

import com.insta.hms.common.GenericHibernateRepository;
import com.insta.hms.common.HibernateHelper;
import com.insta.hms.core.billing.BillChargeClaimModel;
import com.insta.hms.core.billing.ReceiptModel;
import com.insta.hms.integration.insurance.remittance.InsuranceRemittanceModel;
import com.insta.hms.integration.insurance.remittance.ReconciliationActivityDetailsModel;
import com.insta.hms.integration.insurance.remittance.ReconciliationModel;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Repository
public class ReconciliationRepository extends GenericHibernateRepository {

  private static final int MAX_RESULTS = 20;

  public int addSponsorReceipt(InsuranceRemittanceModel sponsorReceipt) {
    Session session = getSession();
    return (int) session.save(sponsorReceipt);
  }

  /**
   * List sponsor receipts.
   *
   * @return the list
   */
  public List<ReceiptModel> listSponsorReceipts() {
    Session session = getSession();
    Criteria ctr = session.createCriteria(ReceiptModel.class);
    ctr.add(Restrictions.eq("receiptType", "R"));
    ctr.add(Restrictions.eq("isSettlement", false));
    ctr.add(Restrictions.isNotNull("tpaId"));
    ctr.add(Restrictions.isNotNull("remittanceId"));

    List<ReceiptModel> list = ctr.list();
    return list;
  }

  public String generateNextId() {
    Session session = getSession();
    return HibernateHelper.generateNextId(session, "SPONSOR_RECEIPT_DEFAULT");
  }

  public ReconciliationModel getReconciliationDetails(int reconciliationId) {
    Session session = getSession();
    return (ReconciliationModel) session.get(ReconciliationModel.class, reconciliationId);
  }

  private static final String GET_RECONCILIATION_ACTIVITY_DETAILS_BY_FILTER = "SELECT "
      + "rad FROM ReconciliationActivityDetailsModel rad "
      + "WHERE rad.reconciliationId = :reconciliationId AND rad.activityId = :activityId "
      + "AND rad.claimId = :claimI"
      + "d AND rad.status = 'D' ";
  
  /**
   * Method to get the last draft 
   * {@link com.insta.hms.integration.insurance.remittance.ReconciliationActivityDetailsModel}
   * entry with below filters
   *
   * @param reconciliationId the reconciliation_id
   * @param activityId the activity_id
   * @param claimId the claim_id
   * @return the last entry with the above filters
   */
  public ReconciliationActivityDetailsModel getDraftReconciliationDetails(int reconciliationId,
      String activityId, String claimId) {
    Session session = getSession();
    Query query = session.createQuery(GET_RECONCILIATION_ACTIVITY_DETAILS_BY_FILTER);
    query.setInteger("reconciliationId", reconciliationId);
    query.setString("activityId", activityId);
    query.setString("claimId", claimId);
    query.setMaxResults(1);
    return (ReconciliationActivityDetailsModel) query.uniqueResult();
  }

  private static final String GET_SR_AMOUNT_DETAILS = "SELECT"
      + " SUM(r.amount), SUM(r.tdsAmount), SUM(r.otherDeductions), rm.allocatedAmount"
      + " FROM ReconciliationModel rm join  rm.receiptId r "
      + " WHERE rm.reconciliationId=:reconciliationId"
      + " GROUP BY rm.reconciliationId, rm.allocatedAmount";

  /**
   * Gets the sponsor receipt amount details.
   *
   * @param reconciliationId the reconciliation id
   * @return the sponsor receipt amount details
   */
  public Object[] getSponsorReceiptAmountDetails(int reconciliationId) {
    Session session = getSession();
    Query query = session.createQuery(GET_SR_AMOUNT_DETAILS);
    query.setInteger("reconciliationId", reconciliationId);
    return (Object[]) query.uniqueResult();
  }

  private static final String GET_SR_DRAFTED_AMOUNT = "SELECT"
      + " COALESCE(SUM(allocatedAmount), 0) FROM ReconciliationActivityDetailsModel"
      + " WHERE id.reconciliationId=? AND status='D'";

  /**
   * Gets the drafted amount.
   *
   * @param reconciliationId the reconciliation id
   * @return the drafted amount
   */
  public BigDecimal getDraftedAmount(int reconciliationId) {
    Session session = getSession();
    Query query = session.createQuery(GET_SR_DRAFTED_AMOUNT);
    query.setInteger(0, reconciliationId);
    return (BigDecimal) query.uniqueResult();
  }

  private static final String GET_BILLS_FROM_BATCH = "SELECT"
      + " new map(b.billNo as bill_no, c.claimId as claim_id,"
      + " pr.patientId as visit_id, concat(pd.patientName, ' ', "
      + " pd.middleName, ' ', pd.lastName) AS patient_name, pd.mrNo as mr_no,"
      + " SUM(COALESCE(bcc.insuranceClaimAmt, 0) + COALESCE(bcc.taxAmount, 0))"
      + " as insurance_claim_amount, "
      + " SUM(COALESCE(bcc.claimRecdTotal,0)) as past_paid_amount, "
      + " SUM(COALESCE(bcc.insuranceClaimAmt) + COALESCE(bcc.taxAmount))"
      + " - SUM(COALESCE(bcc.claimRecdTotal,0)) AS due)"
      + " FROM ClaimSubmissionsModel cs JOIN cs.claim c "
      + " JOIN c.bills b JOIN b.billCharges bc JOIN bc.billChargeClaims bcc "
      + " JOIN b.visitId pr JOIN pr.patientDetails pd"
      + " WHERE cs.submissionBatch.submissionBatchId IN (:submissionBatchIds)"
      + " GROUP BY b.billNo, c.claimId, pr.patientId, pd.patientName, pd.mrNo";

  /**
   * Gets the bills from batch.
   *
   * @param submissionBatchIds the submission batch ids
   * @return the bills from batch
   */
  public List getBillsFromBatch(List<String> submissionBatchIds) {
    Session session = getSession();
    Query query = session.createQuery(GET_BILLS_FROM_BATCH);
    query.setParameterList("submissionBatchIds", submissionBatchIds);
    return query.list();
  }

  private static final String GET_BILLS_BY_MRNO = "SELECT "
      + "new map(b.billNo AS bill_no, bcc.id.claimId AS claim_id, "
      + "pr.patientId AS visit_id, concat(pd.patientName, ' ', "
      + "pd.middleName, ' ', pd.lastName) AS patient_name, pd.mrNo AS mr_no, "
      + "SUM(bcc.insuranceClaimAmt) + SUM(bcc.taxAmount) AS insurance_claim_amount, "
      + "SUM(COALESCE(bcc.claimRecdTotal,0)) AS past_paid_amount,"
      + " CASE WHEN (COALESCE(b.primaryTotalSponsorReceipts,0) + "
      + " COALESCE(b.secondaryTotalSponsorReceipts,0)) = 0 THEN "
      + " (b.totalClaim + b.totalClaimTax - COALESCE(b.claimRecdAmount,0)) "
      + " ELSE (b.totalClaim + b.totalClaimTax - COALESCE("
      + " b.primaryTotalSponsorReceipts,0) - COALESCE(b.secondaryTotalSponsorReceipts,0)) "
      + " END AS total_sponsor_due) "
      + " FROM BillChargeClaimModel bcc JOIN bcc.billNo b"
      + " JOIN b.visitId pr JOIN pr.patientDetails pd"
      + " WHERE pd.mrNo IN (:mrNoList) AND bcc.sponsorId = :tpaId AND b.status = 'F'"
      + " AND b.sponsorWriteoff != 'A' AND bcc.insuranceClaimAmt + bcc.taxAmount > 0"
      + " AND (((COALESCE(b.primaryTotalSponsorReceipts,0)"
      + " + COALESCE(b.secondaryTotalSponsorReceipts,0)) = 0"
      + " AND (b.totalClaim + b.totalClaimTax - COALESCE(b.claimRecdAmount,0)) > 0 )"
      + " OR ((COALESCE(b.primaryTotalSponsorReceipts,0)"
      + " + COALESCE(b.secondaryTotalSponsorReceipts,0)) != 0"
      + " AND (b.totalClaim + b.totalClaimTax - COALESCE(b.primaryTotalSponsorReceipts,0)"
      + " - COALESCE(b.secondaryTotalSponsorReceipts,0)) > 0 ))"
      + " GROUP BY b.billNo, bcc.id.claimId, pr.patientId, pd.patientName, pd.mrNo,"
      + " b.primaryTotalSponsorReceipts,b.secondaryTotalSponsorReceipts,"
      + " b.totalClaim,b.totalClaimTax,b.claimRecdAmount";

  /**
   * Gets the bills by mr no.
   *
   * @param mrNoList the mr no list
   * @return the bills by mr no
   */
  public List getBillsByMrNo(List<String> mrNoList, String tpaId) {
    Session session = getSession();
    Query query = session.createQuery(GET_BILLS_BY_MRNO);
    query.setParameterList("mrNoList", mrNoList);
    query.setString("tpaId", tpaId);
    return query.list();
  }

  private static final String GET_BILLS_BY_CLAIM_ID = "SELECT"
      + " new map(b.billNo AS bill_no, bcc.id.claimId AS claim_id, "
      + "pr.patientId AS visit_id, concat(pd.patientName, ' ', "
      + "pd.middleName, ' ', pd.lastName) AS patient_name, pd.mrNo AS mr_no, "
      + "SUM(bcc.insuranceClaimAmt) + SUM(bcc.taxAmount) AS insurance_claim_amount, "
      + "SUM(COALESCE(bcc.claimRecdTotal,0)) AS past_paid_amount,"
      + " CASE WHEN (COALESCE(b.primaryTotalSponsorReceipts,0) +"
      + " COALESCE(b.secondaryTotalSponsorReceipts,0)) = 0 THEN "
      + " (b.totalClaim + b.totalClaimTax - COALESCE(b.claimRecdAmount,0)) "
      + " ELSE (b.totalClaim + b.totalClaimTax - COALESCE( "
      + " b.primaryTotalSponsorReceipts,0) - COALESCE(b.secondaryTotalSponsorReceipts,0)) "
      + " END AS total_sponsor_due) "
      + " FROM BillChargeClaimModel bcc JOIN bcc.billNo b"
      + " JOIN b.visitId pr JOIN pr.patientDetails pd"
      + " WHERE bcc.id.claimId IN (:claimIdList) AND b.status = 'F'"
      + " AND b.sponsorWriteoff != 'A' AND bcc.insuranceClaimAmt + bcc.taxAmount> 0"
      + " AND (((COALESCE(b.primaryTotalSponsorReceipts,0)"
      + " + COALESCE(b.secondaryTotalSponsorReceipts,0)) = 0"
      + " AND (b.totalClaim + b.totalClaimTax - COALESCE(b.claimRecdAmount,0)) > 0 )"
      + " OR ((COALESCE(b.primaryTotalSponsorReceipts,0)"
      + " + COALESCE(b.secondaryTotalSponsorReceipts,0)) != 0"
      + " AND (b.totalClaim + b.totalClaimTax - COALESCE(b.primaryTotalSponsorReceipts,0)"
      + " - COALESCE(b.secondaryTotalSponsorReceipts,0)) > 0 ))"
      + " GROUP BY b.billNo, bcc.claimId, pr.patientId, pd.patientName, pd.mrNo,"
      + " b.primaryTotalSponsorReceipts,b.secondaryTotalSponsorReceipts,"
      + " b.totalClaim,b.totalClaimTax,b.claimRecdAmount";

  /**
   * Gets the bills by claim id.
   *
   * @param claimIdList the claim id list
   * @return the bills by claim id
   */
  public List getBillsByClaimId(List<String> claimIdList) {
    Session session = getSession();
    Query query = session.createQuery(GET_BILLS_BY_CLAIM_ID);
    query.setParameterList("claimIdList", claimIdList);
    return query.list();
  }

  private static final String GET_BILLS_BY_BILL_NO_WITH_DRAFTED_CLAIMS = "SELECT "
      + "new map(b.billNo AS bill_no, bcc.id.claimId AS claim_id, "
      + "pr.patientId AS visit_id, concat(pd.patientName, ' ', "
      + "pd.middleName, ' ', pd.lastName) AS patient_name, pd.mrNo AS mr_no, "
      + "SUM(bcc.insuranceClaimAmt) + SUM(bcc.taxAmount) AS insurance_claim_amount, "
      + "SUM(COALESCE(bcc.claimRecdTotal,0)) AS past_paid_amount)"
      + " FROM BillChargeClaimModel bcc JOIN bcc.billNo b"
      + " JOIN b.visitId pr JOIN pr.patientDetails pd"
      + " JOIN bcc.claimId ic "
      + " WHERE b.billNo IN   (:billNoList) AND bcc.insuranceClaimAmt + bcc.taxAmount > 0"
      + " AND bcc.id.claimId in "
      + " (:claimIds)"
      + " GROUP BY b.billNo, bcc.claimId, pr.patientId, pd.patientName, pd.mrNo";


  private static final String GET_BILLS_BY_BILL_NO_TPA_ID = "SELECT "
      + "new map(b.billNo AS bill_no, bcc.id.claimId AS claim_id, "
      + "pr.patientId AS visit_id, concat(pd.patientName, ' ', "
      + "pd.middleName, ' ', pd.lastName) AS patient_name, pd.mrNo AS mr_no, "
      + "SUM(bcc.insuranceClaimAmt) + SUM(bcc.taxAmount) AS insurance_claim_amount, "
      + "SUM(COALESCE(bcc.claimRecdTotal,0)) AS past_paid_amount,"
      + " CASE WHEN (COALESCE(b.primaryTotalSponsorReceipts,0) +"
      + " COALESCE(b.secondaryTotalSponsorReceipts,0)) = 0 THEN "
      + " (b.totalClaim + b.totalClaimTax - COALESCE(b.claimRecdAmount,0)) "
      + " ELSE (b.totalClaim + b.totalClaimTax - COALESCE( "
      + " b.primaryTotalSponsorReceipts,0) - COALESCE(b.secondaryTotalSponsorReceipts,0)) "
      + " END AS total_sponsor_due) "
      + " FROM BillChargeClaimModel bcc JOIN bcc.billNo b"
      + " JOIN b.visitId pr JOIN pr.patientDetails pd"
      + " WHERE b.billNo IN (:billNoList) AND bcc.sponsorId.tpaId = :tpaId"
      + " AND b.visitId.centerId.centerId = :centerId"
      + " AND bcc.insuranceClaimAmt + bcc.taxAmount > 0"
      + " GROUP BY b.billNo, bcc.claimId, pr.patientId, pd.patientName, pd.mrNo,"
      + " b.primaryTotalSponsorReceipts,b.secondaryTotalSponsorReceipts,"
      + " b.totalClaim,b.totalClaimTax,b.claimRecdAmount";

  /**
   * Gets the bills by bill no.
   *
   * @param billNoList the bill no list
   * @return the bills by bill no
   */
  public List getBillsByBillNoTpaId(List<String> billNoList, String tpaId, Integer centerId) {
    Session session = getSession();
    Query query = session.createQuery(GET_BILLS_BY_BILL_NO_TPA_ID);
    query.setParameterList("billNoList", billNoList);
    query.setString("tpaId", tpaId);
    query.setInteger("centerId", centerId);

    return query.list();
  }

  private static final String GET_BILLS_BY_BILL_NO = "SELECT "
      + "new map(b.billNo AS bill_no, bcc.id.claimId AS claim_id, "
      + "pr.patientId AS visit_id, concat(pd.patientName, ' ', "
      + "pd.middleName, ' ', pd.lastName) AS patient_name, pd.mrNo AS mr_no, "
      + "SUM(bcc.insuranceClaimAmt + bcc.taxAmount) AS insurance_claim_amount, "
      + "SUM(COALESCE(bcc.claimRecdTotal,0)) AS past_paid_amount,"
      + " CASE WHEN (COALESCE(b.primaryTotalSponsorReceipts,0) +"
      + " COALESCE(b.secondaryTotalSponsorReceipts,0)) = 0 THEN "
      + " (b.totalClaim + b.totalClaimTax - COALESCE(b.claimRecdAmount,0)) "
      + " ELSE (b.totalClaim + b.totalClaimTax - COALESCE( "
      + " b.primaryTotalSponsorReceipts,0) - COALESCE(b.secondaryTotalSponsorReceipts,0)) "
      + " END AS total_sponsor_due) "
      + " FROM BillChargeClaimModel bcc JOIN bcc.billNo b"
      + " JOIN b.visitId pr JOIN pr.patientDetails pd"
      + " WHERE b.billNo IN (:billNoList) AND bcc.insuranceClaimAmt + bcc.taxAmount > 0 "
      + " AND b.visitId.centerId.centerId = :centerId"
      + " GROUP BY b.billNo, bcc.claimId, pr.patientId, pd.patientName, pd.mrNo,"
      + " b.primaryTotalSponsorReceipts,b.secondaryTotalSponsorReceipts,"
      + " b.totalClaim,b.totalClaimTax,b.claimRecdAmount ";

  /**
   * Gets the bills by bill no.
   *
   * @param billNoList the bill no list
   * @return the bills by bill no
   */
  public List getBillsByBillNo(List<String> billNoList, Integer centerId) {
    Session session = getSession();
    Query query = session.createQuery(GET_BILLS_BY_BILL_NO);
    query.setParameterList("billNoList", billNoList);
    query.setInteger("centerId", centerId);
    return query.list();
  }


  private static final String GET_BILLS_BY_BILL_NO_CLAIM_ID = "SELECT "
      + "new map(b.billNo AS bill_no, bcc.id.claimId AS claim_id, "
      + "pr.patientId AS visit_id, concat(pd.patientName, ' ', "
      + "pd.middleName, ' ', pd.lastName) AS patient_name, pd.mrNo AS mr_no, "
      + "SUM(bcc.insuranceClaimAmt) + SUM(bcc.taxAmount) AS insurance_claim_amount, "
      + "SUM(COALESCE(bcc.claimRecdTotal,0)) AS past_paid_amount)"
      + " FROM BillChargeClaimModel bcc JOIN bcc.billNo b"
      + " JOIN b.visitId pr JOIN pr.patientDetails pd"
      + " WHERE b.billNo IN (:billNoList) AND bcc.insuranceClaimAmt + bcc.taxAmount > 0"
      + " AND bcc.claimId.claimId IN (:claimIds)"
      + " GROUP BY b.billNo, bcc.claimId, pr.patientId, pd.patientName, pd.mrNo";

  /**
   * Gets the bills by bill no.
   *
   * @param billNoList the bill no list
   * @return the bills by bill no
   */
  public List getBillsByBillNo(List<String> billNoList, List<String> claimIds) {
    Session session = getSession();
    Query query = session.createQuery(GET_BILLS_BY_BILL_NO_CLAIM_ID);
    query.setParameterList("billNoList", billNoList);
    query.setParameterList("claimIds", claimIds);
    return query.list();
  }

  /**
   * Gets the bills by bill no.
   *
   * @param billNoList the bill no list
   * @return the bills by bill no
   */
  public List getDraftedBillsByBillNo(List<String> billNoList, List<String> claimIds) {
    Session session = getSession();
    Query query = session.createQuery(GET_BILLS_BY_BILL_NO_WITH_DRAFTED_CLAIMS);
    query.setParameterList("billNoList", billNoList);
    query.setParameterList("claimIds", claimIds);
    return query.list();
  }

  private static final String GET_SPONSOR_DUE_AND_BATCHES_BY_TPA = "SELECT isb.submissionBatchId,"
      + " SUM(COALESCE(bcc.insuranceClaimAmt, 0) + COALESCE(bcc.taxAmount, 0)"
      + " - COALESCE(bcc.claimRecdTotal, 0)) AS due,"
      + " isb.submissionDate, isb.status"
      + " FROM ClaimSubmissionsModel cs JOIN cs.submissionBatch isb JOIN"
      + " cs.claim ic JOIN ic.billChargeClaims bcc"
      + " WHERE isb.tpaId = :tpaId AND COALESCE(bcc.insuranceClaimAmt, 0)"
      + " + COALESCE(bcc.taxAmount, 0) > 0"
      + " AND bcc.claimStatus != 'C' AND cs.submissionBatch.centerId=:centerId"
      + " AND isb.status = 'S' "
      + " AND COALESCE(bcc.claimRecdTotal, 0) < COALESCE(bcc.insuranceClaimAmt, 0)"
      + " +  COALESCE(bcc.taxAmount, 0)"
      + " GROUP BY isb.submissionBatchId ORDER BY isb.submissionDate DESC  NULLS LAST";

  /**
   * Gets the batches by tpa.
   *
   * @param tpaId the tpa id
   * @return the batches by tpa
   */
  public List getBatchesByTpa(String tpaId, Integer centerId) {
    Session session = getSession();
    Query query = session.createQuery(GET_SPONSOR_DUE_AND_BATCHES_BY_TPA);
    query.setString("tpaId", tpaId);
    query.setInteger("centerId", centerId);
    return query.list();
  }

  private static final String GET_BILL_CHARGES = "SELECT chargeId"
      + " FROM BillChargeModel WHERE billNo=:billNo ORDER BY chargeId";

  /**
   * Gets the charges of bill.
   *
   * @param billNo the bill no
   * @return the charges of bill
   */
  public List<String> getChargesOfBill(String billNo) {
    Session session = getSession();
    Query query = session.createQuery(GET_BILL_CHARGES).setString("billNo", billNo);
    return query.list();
  }

  private static final String GET_BALANCE_AMOUNT = "SELECT"
      + " (insuranceClaimAmt + taxAmount - COALESCE(claimRecdTotal, 0)) FROM BillChargeClaimModel"
      + " WHERE id.claimId=:claimId AND id.chargeId=:chargeId";

  /**
   * Gets the balance amount.
   *
   * @param claimId  the claim id
   * @param chargeId the charge id
   * @return the balance amount
   */
  public BigDecimal getBalanceAmount(String claimId, String chargeId) {
    Session session = getSession();
    Query query = session.createQuery(GET_BALANCE_AMOUNT);
    query.setString("claimId", claimId);
    query.setString("chargeId", chargeId);
    return (BigDecimal) query.uniqueResult();
  }

  private static final String GET_BILL_CHARGE_CLAIMS_BY_CLAIM_AND_BILL = "FROM BillChargeClaimModel"
      + " WHERE id.claimId = :claimId AND billNo.billNo = :billNo";

  /**
   * Gets the bill charge claims of visit.
   *
   * @param claimId the claim id
   * @return the bill charge claims of visit
   */
  public List<BillChargeClaimModel> getBillChargeClaimsOfVisit(String claimId, String billNo) {
    Session session = getSession();
    Query query = session.createQuery(GET_BILL_CHARGE_CLAIMS_BY_CLAIM_AND_BILL);
    query.setString("claimId", claimId);
    query.setString("billNo", billNo);
    return query.list();
  }

  private static final String GET_RECEIPT_BY_REMITTANCE =
      "SELECT r FROM ReconciliationModel rm JOIN rm.receiptId r"
          + " WHERE rm.reconciliationId=:reconciliationId";

  /**
   * Gets the receipt by reconciliation id.
   *
   * @param reconciliationId the reconciliation id
   * @return the receipt by reconciliation id
   */
  public ReceiptModel getReceiptByReconciliationId(int reconciliationId) {
    Session session = getSession();
    Query query = session.createQuery(GET_RECEIPT_BY_REMITTANCE);
    query.setInteger("reconciliationId", reconciliationId);
    return (ReceiptModel) query.uniqueResult();
  }

  private static final String GET_BILL_CHARGE_CLAIMS = "SELECT "
      + "new map(bc.chargeId AS charge_id, bc.actDescription AS item_description, "
      + "(bcc.insuranceClaimAmt + bcc.taxAmount) AS claim_amount, "
      + "bcc.claimRecdTotal AS sponsor_paid, bc.billNo.billNo AS bill_no, bcc.claimId.claimId AS "
      + "claim_id, bcc.chargeId.chargeId as activity_id ) "
      + "FROM BillChargeClaimModel bcc JOIN bcc.chargeId bc "
      + "WHERE bc.billNo.billNo in (:billNoList) AND bc.status!='X'"
      + "ORDER BY bc.chargeId";

  /**
   * Gets the bill charge claims.
   *
   * @param billNos  the bill no
   * @return the bill charge claims
   */
  public List<Map<String, Object>> getBillChargeClaims(List<String> billNos) {
    Session session = getSession();
    Query query = session.createQuery(GET_BILL_CHARGE_CLAIMS);
    query.setParameterList("billNoList", billNos);
    return query.list();
  }


  private static final String GET_BILL_CHARGE_CLAIMS_CLAIM_ID = "SELECT "
      + "new map(bc.chargeId AS charge_id, bc.actDescription AS item_description, "
      + "(bcc.insuranceClaimAmt + bcc.taxAmount) AS claim_amount, "
      + "bcc.claimRecdTotal AS sponsor_paid) "
      + "FROM BillChargeClaimModel bcc JOIN bcc.chargeId bc "
      + "WHERE bc.billNo.billNo in (:billNoList) AND bcc.claimId = :claimId AND bc.status!='X'"
      + "ORDER BY bc.chargeId";

  /**
   * Gets bill charge claims.
   *
   * @param billNos the bill nos
   * @param claimId the claim id
   * @return the bill charge claims
   */
  public List<Map<String, Object>> getBillChargeClaims(List<String> billNos, String claimId) {
    Session session = getSession();
    Query query = session.createQuery(GET_BILL_CHARGE_CLAIMS_CLAIM_ID);
    query.setParameterList("billNoList", billNos);
    query.setString("claimId", claimId);
    return query.list();
  }

  private static final String GET_MR_NO_LIST = "SELECT "
      + "new map(pr.mrNo AS mr_no, pr.regDate AS visit_date, pr.regTime AS visit_time, "
      + "pr.patientId AS visit_id, "
      + "SUM(COALESCE(bcc.insuranceClaimAmt, 0) + COALESCE(bcc.taxAmount, 0)"
      + " - COALESCE(bcc.claimRecdTotal, 0)) AS sponsor_due, "
      + "pd.patientName AS name) "
      + "FROM BillChargeClaimModel bcc JOIN bcc.billNo b "
      + "JOIN b.visitId pr JOIN pr.patientDetails pd "
      + "WHERE bcc.sponsorId = :tpaId AND pr.mrNo LIKE :mrNo AND b.status = 'F' "
      + "AND b.sponsorWriteoff != 'A' AND pr.centerId.centerId = :centerId  "
      + "GROUP BY pr.mrNo, pr.regDate, pr.regTime, pr.patientId, pd.patientName "
      + "HAVING SUM(COALESCE(bcc.insuranceClaimAmt, 0) + COALESCE(bcc.taxAmount, 0)"
      + " - COALESCE(bcc.claimRecdTotal, 0)) > 0";

  /**
   * Gets the mr no list.
   *
   * @param mrNo  the mr no
   * @param tpaId the tpa id
   * @return the mr no list
   */
  public List<Map<String, Object>> getMrNoList(String mrNo, String tpaId, Integer centerId) {
    Session session = getSession();
    Query query = session.createQuery(GET_MR_NO_LIST);
    query.setString("mrNo", "%" + mrNo + "%");
    query.setString("tpaId", tpaId);
    query.setInteger("centerId", centerId);
    query.setMaxResults(15);
    return query.list();
  }

  private static final String GET_BILL_LIST = "SELECT "
      + "new map(b.billNo AS bill_no, pr.regDate AS visit_date, pr.regTime AS visit_time, "
      + "pr.patientId AS visit_id, "
      + "SUM(COALESCE(bcc.insuranceClaimAmt, 0) + COALESCE(bcc.taxAmount, 0)"
      + " - COALESCE(bcc.claimRecdTotal, 0)) AS sponsor_due, "
      + "pd.patientName AS name) "
      + "FROM BillChargeClaimModel bcc JOIN bcc.billNo b "
      + "JOIN b.visitId pr JOIN pr.patientDetails pd "
      + "WHERE bcc.sponsorId = :tpaId AND bcc.billNo LIKE :billNo AND b.status = 'F' "
      + "AND b.sponsorWriteoff != 'A' AND pr.centerId.centerId = :centerId "
      + "GROUP BY b.billNo, pr.regDate, pr.regTime, pr.patientId, pd.patientName "
      + "HAVING SUM(COALESCE(bcc.insuranceClaimAmt, 0) + COALESCE(bcc.taxAmount, 0)"
      + " - COALESCE(bcc.claimRecdTotal, 0)) > 0";

  /**
   * Gets the bill list.
   *
   * @param billNo the bill no
   * @param tpaId  the tpa id
   * @return the bill list
   */
  public List<Map<String, Object>> getBillList(String billNo, String tpaId, Integer centerId) {
    Session session = getSession();
    Query query = session.createQuery(GET_BILL_LIST);
    query.setString("billNo", "%" + billNo + "%");
    query.setString("tpaId", tpaId);
    query.setInteger("centerId", centerId);
    query.setMaxResults(15);
    return query.list();
  }

  private static final String GET_CLAIM_LIST = "SELECT "
      + "new map(bcc.id.claimId as claim_id, pr.regDate AS visit_date, pr.regTime AS visit_time, "
      + "pr.patientId AS visit_id, "
      + "SUM(COALESCE(bcc.insuranceClaimAmt, 0) + COALESCE(bcc.taxAmount, 0)"
      + " - COALESCE(bcc.claimRecdTotal, 0)) AS sponsor_due, "
      + "pd.patientName AS name) "
      + "FROM BillChargeClaimModel bcc JOIN bcc.billNo b "
      + "JOIN b.visitId pr JOIN pr.patientDetails pd "
      + "WHERE bcc.sponsorId = :tpaId AND bcc.claimId LIKE :claimId AND b.status = 'F' "
      + "AND b.sponsorWriteoff != 'A' AND pr.centerId.centerId = :centerId "
      + "GROUP BY bcc.claimId, pr.regDate, pr.regTime, pr.patientId, pd.patientName "
      + "HAVING SUM(COALESCE(bcc.insuranceClaimAmt, 0) + COALESCE(bcc.taxAmount, 0)"
      + " - COALESCE(bcc.claimRecdTotal, 0)) > 0";

  /**
   * Gets the claim list.
   *
   * @param claimId the claim id
   * @param tpaId   the tpa id
   * @return the claim list
   */
  public List<Map<String, Object>> getClaimList(String claimId, String tpaId, Integer centerId) {
    Session session = getSession();
    Query query = session.createQuery(GET_CLAIM_LIST);
    query.setString("claimId", "%" + claimId + "%");
    query.setString("tpaId", tpaId);
    query.setInteger("centerId", centerId);
    query.setMaxResults(15);
    return query.list();
  }

  private static final String GET_DRAFTED_ALLOCATIONS = "SELECT "
      + "SUBSTRING(id.activityId,3), allocatedAmount, denialRemarks,claimId.claimId "
      + "FROM ReconciliationActivityDetailsModel "
      + "WHERE reconciliationId = :reconciliationId AND status = 'D'";

  /**
   * Gets the drafted allocations.
   *
   * @param reconciliationId the reconciliation id
   * @return the drafted allocations
   */
  public List getDraftedAllocations(int reconciliationId) {
    Session session = getSession();
    Query query = session.createQuery(GET_DRAFTED_ALLOCATIONS);
    query.setInteger("reconciliationId", reconciliationId);
    return query.list();
  }

  private static final String GET_BILLS_OF_CHARGES = "SELECT "
      + "bc.billNo.billNo, bc.chargeId, bcc.claimId.claimId FROM BillChargeModel bc"
      + " LEFT JOIN bc.billChargeClaims bcc"
      + " WHERE bc.chargeId IN (:chargeIds)"
      + " and bcc.claimId.claimId in (:claimIds)";

  /**
   * Gets the bills of charges.
   *
   * @param charges the charges
   * @return the bills of charges
   */
  public List getBillsOfCharges(Object[] charges, String[] claimIds) {
    Session session = getSession();
    Query query = session.createQuery(GET_BILLS_OF_CHARGES);
    query.setParameterList("chargeIds", charges);
    query.setParameterList("claimIds", claimIds);
    return query.list();
  }

  private static final String GET_BILLS_BY_FINALIZED_DATE = "SELECT "
      + "new map(b.billNo AS bill_no, bcc.id.claimId AS claim_id, "
      + "pr.patientId AS visit_id, concat(pd.patientName, ' ', "
      + "pd.middleName, ' ', pd.lastName) AS  patient_name, pd.mrNo AS mr_no, "
      + "SUM(bcc.insuranceClaimAmt + bcc.taxAmount) AS insurance_claim_amount, "
      + "SUM(COALESCE(bcc.claimRecdTotal,0)) AS past_paid_amount)"
      + " FROM BillChargeClaimModel bcc JOIN bcc.billNo b "
      + "JOIN b.visitId pr JOIN pr.patientDetails pd"
      + " WHERE bcc.sponsorId = :tpaId AND b.finalizedDate >= :fromDate" 
      + " AND b.finalizedDate < :toDate AND pr.centerId.centerId = :centerId"
      + " GROUP BY b.billNo, bcc.claimId, pr.patientId, pd.patientName, pd.mrNo HAVING " 
      + " SUM(bcc.insuranceClaimAmt + bcc.taxAmount) - SUM(COALESCE(bcc.claimRecdTotal,0)) > 0";

  /**
   * Gets the bills finalized between.
   *
   * @param tpaId    the tpa id
   * @param fromDate the from date
   * @param toDate   the to date
   * @return the bills finalized between
   */
  public List getBillsFinalizedBetween(String tpaId, Integer centerId, Date fromDate, Date toDate) {
    Session session = getSession();
    Query query = session.createQuery(GET_BILLS_BY_FINALIZED_DATE);
    query.setString("tpaId", tpaId);
    query.setInteger("centerId", centerId);

    // This is done to consider the to date as well.
    Calendar cal = Calendar.getInstance();
    cal.setTime(toDate);
    cal.add(Calendar.DATE,1);
    Timestamp fromDateTimestamp = new Timestamp(fromDate.getTime());
    Timestamp toDateTimestamp = new Timestamp(cal.getTime().getTime());
    query.setTimestamp("fromDate", fromDateTimestamp);
    query.setTimestamp("toDate", toDateTimestamp);
    return query.list();
  }

  private static final String DELETE_REMOVED_DRAFT_ACTIVITIES = "DELETE "
      + "FROM ReconciliationActivityDetailsModel WHERE reconciliationId = :reconciliationId "
      + "AND status = 'D' AND activityId NOT IN (:activityIds)";

  /**
   * Delete saved draft.
   *
   * @param reconciliationId the reconciliation id
   */
  public void deleteRemovedDraft(int reconciliationId, Set<String> activityIds) {
    Session session = getSession();
    Query query = session.createQuery(DELETE_REMOVED_DRAFT_ACTIVITIES);
    query.setInteger("reconciliationId", reconciliationId);
    query.setParameterList("activityIds", activityIds);
    query.executeUpdate();
  }

  private static final String DELETE_REMOVED_BILLS_FROM_BILL_REMITTANCE = "DELETE "
      + "FROM BillRemittanceModel WHERE remittance=:remittanceId AND status = 'D' "
      + "AND id.billNo NOT IN (:billList)";

  /**
   * Delete removed bill remittances.
   *
   * @param reconciliationId the reconciliation id
   * @param billList         the bill list
   */
  public void deleteRemovedBillRemittances(int reconciliationId, Object[] billList) {
    Session session = getSession();
    // TODO need to rethink this
    Query query = session.createQuery(DELETE_REMOVED_BILLS_FROM_BILL_REMITTANCE);
    query.setInteger("remittanceId", reconciliationId);
    query.setParameterList("billList", billList);
    query.executeUpdate();
  }

  private static final String GET_DRAFTED_BILL_REMITTANCES = "FROM BillRemittanceModel "
      + "WHERE id.remittanceId=:remittanceId AND id.billNo IN (:billList) " + "AND status = 'D'";

  /**
   * Gets the drafted bill remittances.
   *
   * @param remittanceId the remittance id
   * @param billList     the bill list
   * @return the drafted bill remittances
   */
  public List<BillRemittanceModel> getDraftedBillRemittances(int remittanceId, Object[] billList) {
    Session session = getSession();
    Query query = session.createQuery(GET_DRAFTED_BILL_REMITTANCES);
    query.setInteger("remittanceId", remittanceId);
    query.setParameterList("billList", billList);
    return query.list();
  }

  private static final String GET_SAVED_BILL_REMITTANCES = "FROM BillRemittanceModel "
      + "WHERE id.remittanceId=:remittanceId AND id.billNo IN (:billList) " + "AND status = 'F'";

  /**
   * Gets the saved bill remittances.
   *
   * @param remittanceId the remittance id
   * @param billList     the bill list
   * @return the saved bill remittances
   */
  public List<BillRemittanceModel> getSavedBillRemittances(int remittanceId, Object[] billList) {
    Session session = getSession();
    Query query = session.createQuery(GET_SAVED_BILL_REMITTANCES);
    query.setInteger("remittanceId", remittanceId);
    query.setParameterList("billList", billList);
    return query.list();
  }

  public String getNextWriteOffReceiptId(String sequenceName, String typeNumber) {
    Session session = getSession();
    return HibernateHelper.generateNextSequenceId(session, sequenceName, typeNumber);
  }

  private static final String GET_WRITEOFF_AMOUNT = "SELECT "
      + "sum(insuranceClaimAmt + taxAmount - COALESCE(claimRecdTotal, 0)) "
      + "FROM BillChargeClaimModel WHERE billNo = :billNo AND claimId = :claimId "
      + "GROUP BY billNo, claimId";

  /**
   * Gets the sponsor write off amount.
   *
   * @param billNo  the bill no
   * @param claimId the claim id
   * @return the sponsor write off amount
   */
  public BigDecimal getSponsorWriteOffAmount(String billNo, String claimId) {
    Session session = getSession();
    Query query = session.createQuery(GET_WRITEOFF_AMOUNT);
    query.setString("billNo", billNo);
    query.setString("claimId", claimId);
    return (BigDecimal) query.uniqueResult();
  }

  private static final String CREATED_DATE_FILTER =
      " AND r.displayDate >= :fromDate AND r.displayDate < :toDate ";
  private static final String TPA_FILTER = " AND r.tpaId = :tpaId ";
  private static final String PAYMENT_REFERENCE_FILTER = " AND r.referenceNo = :referenceNo ";
  private static final String CENTER_ID_FILTER = " AND (r.centerId = :centerId or r.centerId = 0) ";

  private static final String GET_TPA_LIST = "SELECT "
      + "DISTINCT new map(tpa.tpaId AS tpa_id, tpa.tpaName AS tpa_name) "
      + "FROM ReceiptModel r JOIN r.tpaId tpa " + "WHERE LOWER(tpa.tpaName) LIKE :tpaName ";

  /**
   * Gets the tpa list.
   *
   * @param tpaName  the tpa name
   * @param fromDate the from date
   * @param toDate   the to date
   * @return the tpa list
   */
  public List<Map<String, String>> getTpaList(String tpaName, Date fromDate, Date toDate) {
    Session session = getSession();
    StringBuilder queryString = new StringBuilder(GET_TPA_LIST);
    if (null != toDate && null != fromDate) {
      queryString.append(CREATED_DATE_FILTER);
    }

    Query query = session.createQuery(queryString.toString());
    if (null != toDate && null != fromDate) {
      // This is done to consider the to date as well.
      Calendar cal = Calendar.getInstance();
      cal.setTime(toDate);
      cal.add(Calendar.DATE,1);
      Timestamp fromDateTimestamp = new Timestamp(fromDate.getTime());
      Timestamp toDateTimestamp = new Timestamp(cal.getTime().getTime());
      query.setTimestamp("fromDate", fromDateTimestamp);
      query.setTimestamp("toDate", toDateTimestamp);
    }
    query.setString("tpaName", "%" + tpaName.toLowerCase() + "%");
    query.setMaxResults(MAX_RESULTS);
    return query.list();
  }

  private static final String GET_PAYMENT_REFERENCE_NUMBER = "SELECT "
      + " DISTINCT r.referenceNo FROM ReconciliationModel rc JOIN rc.receiptId r WHERE "
      + " r.referenceNo LIKE :referenceNo";

  /**
   * Gets the payment reference list.
   *
   * @param referenceNo the reference no
   * @param tpaId       the tpa id
   * @param fromDate    the from date
   * @param toDate      the to date
   * @return the payment reference list
   */
  public List<String> getPaymentReferenceList(String referenceNo, String tpaId, Date fromDate,
      Date toDate, Integer centerId) {
    StringBuilder queryString = new StringBuilder(GET_PAYMENT_REFERENCE_NUMBER);
    if (null != tpaId) {
      queryString.append(TPA_FILTER);
    }

    if (null != toDate && null != fromDate) {
      queryString.append(CREATED_DATE_FILTER);
    }

    if (null != centerId && centerId != 0) {
      queryString.append(CENTER_ID_FILTER);
    }

    Session session = getSession();
    Query query = session.createQuery(queryString.toString());
    query.setString("referenceNo", "%" + referenceNo + "%");

    if (null != centerId && centerId != 0) {
      query.setInteger("centerId", centerId);
    }
    if (null != toDate && null != fromDate) {
      Calendar cal = Calendar.getInstance();
      cal.setTime(toDate);
      cal.add(Calendar.DATE,1);
      Timestamp fromDateTimestamp = new Timestamp(fromDate.getTime());
      Timestamp toDateTimestamp = new Timestamp(cal.getTime().getTime());
      query.setTimestamp("fromDate", fromDateTimestamp);
      query.setTimestamp("toDate", toDateTimestamp);
    }
    if (null != tpaId) {
      query.setString("tpaId", tpaId);
    }
    query.setMaxResults(MAX_RESULTS);
    return query.list();
  }

  private static final String GET_SPONSOR_RECEIPT_LIST = "SELECT "
      + "new map(r.receiptId AS receipt_id, rc.reconciliationId as reconciliation_id, "
      + "r.unallocatedAmount AS unallocated_amount ) "
      + "FROM ReconciliationModel rc JOIN rc.receiptId r WHERE " + " r.receiptId LIKE :receiptId";

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
  public List<Map<String, Object>> getSponsorReceiptList(String receiptId, String referenceNo,
      String tpaId, Date fromDate, Date toDate, Integer centerId) {
    StringBuilder queryString = new StringBuilder(GET_SPONSOR_RECEIPT_LIST);
    if (null != tpaId) {
      queryString.append(TPA_FILTER);
    }

    if (null != toDate && null != fromDate) {
      queryString.append(CREATED_DATE_FILTER);
    }


    if (null != centerId && centerId != 0) {
      queryString.append(CENTER_ID_FILTER);
    }

    if (null != referenceNo) {
      queryString.append(PAYMENT_REFERENCE_FILTER);
    }
    queryString.append(" ORDER BY r.modifiedAt DESC");
    Session session = getSession();
    Query query = session.createQuery(queryString.toString());
    query.setString("receiptId", "%" + receiptId + "%");

    if (centerId != null && centerId != 0) {
      query.setInteger("centerId", centerId);
    }

    if (null != referenceNo) {
      query.setString("referenceNo", referenceNo);
    }
    if (null != toDate && null != fromDate) {
      // This is done to consider the to date as well.
      Calendar cal = Calendar.getInstance();
      cal.setTime(toDate);
      cal.add(Calendar.DATE,1);
      Timestamp fromDateTimestamp = new Timestamp(fromDate.getTime());
      Timestamp toDateTimestamp = new Timestamp(cal.getTime().getTime());
      query.setTimestamp("fromDate", fromDateTimestamp);
      query.setTimestamp("toDate", toDateTimestamp);
    }
    if (null != tpaId) {
      query.setString("tpaId", tpaId);
    }
    query.setMaxResults(MAX_RESULTS);
    return query.list();
  }

  private static final String GET_REMITTED_ALLOCATIONS = "SELECT "
      + "SUBSTRING(id.activityId,3), allocatedAmount as paymentAmount, denialRemarks, "
      + "claimId.claimId FROM ReconciliationActivityDetailsModel "
      + "WHERE reconciliationId = :reconciliationId AND status = 'F'";

  /**
   * Gets the remitted allocations.
   *
   * @param reconciliationId the reconciliation id
   * @return the remitted allocations
   */
  public List getRemittedAllocations(int reconciliationId) {
    Session session = getSession();
    Query query = session.createQuery(GET_REMITTED_ALLOCATIONS);
    query.setInteger("reconciliationId", reconciliationId);
    return query.list();
  }

  private static final String GET_DRAFTED_REMITTANCE_AMOUNTS =
      "SELECT irad.claimId.claimId, SUBSTRING(irad.id.activityId, 3), SUM(COALESCE(irad"
          + ".allocatedAmount, 0)) from  "
          + "ReconciliationActivityDetailsModel irad WHERE  "
          + "irad.status = 'D' AND "
          + "irad.reconciliationId=:reconciliationId GROUP BY irad.claimId.claimId, "
          + "irad.id.activityId";

  /**
   * Gets remittance amounts by reconciliation id grouped by claim id.
   *
   * @param reconciliationId the reconciliation id
   * @return Map of claim id -> remittance amount
   */
  public List<Object> getDraftedRemittanceAmounts(int reconciliationId) {
    Session session = getSession();
    Query query = session.createQuery(GET_DRAFTED_REMITTANCE_AMOUNTS);
    query.setInteger("reconciliationId", reconciliationId);
    return query.list();
  }

  private static final String GET_FINALIZED_REMITTANCE_AMOUNTS =
      "SELECT irad.claimId.claimId, SUBSTRING(irad.id.activityId, 3), SUM(COALESCE(irad"
          + ".allocatedAmount, 0)) from  "
          + "ReconciliationActivityDetailsModel irad WHERE "
          + "irad.status = 'F' AND "
          + "irad.reconciliationId=:reconciliationId GROUP BY irad.claimId.claimId, "
          + "irad.id.activityId";


  /**
   * Gets remittance amounts by reconciliation id grouped by claim id.
   *
   * @param reconciliationId the reconciliation id
   * @return Map of claim id -> remittance amount
   */
  public List<Object> getFinalizedRemittanceAmounts(int reconciliationId) {
    Session session = getSession();
    Query query = session.createQuery(GET_FINALIZED_REMITTANCE_AMOUNTS);
    query.setInteger("reconciliationId", reconciliationId);
    return query.list();
  }


  private static final String GET_VALID_REMITTANCE_BILLS = "SELECT "
      + "b.billNo AS bill_no "
      + "FROM BillChargeClaimModel bcc JOIN bcc.billNo b "
      + "JOIN b.visitId pr JOIN pr.patientDetails pd "
      + "WHERE bcc.sponsorId = :tpaId AND bcc.billNo.billNo IN (:billNos) AND b.status = 'F' "
      + "AND pr.centerId.centerId = :centerId "
      + "GROUP BY b.billNo, pr.regDate, pr.regTime, pr.patientId, pd.patientName ";

  /**
   * Gets valid remittance bills.
   *
   * @param billNos  the bill nos
   * @param tpaId    the tpa id
   * @param centerId the center id
   * @return the valid remittance bills
   */
  public List<String> getValidRemittanceBills(List<String> billNos, String tpaId,
      Integer centerId) {
    Session session = getSession();
    Query query = session.createQuery(GET_VALID_REMITTANCE_BILLS);
    query.setParameterList("billNos", billNos);
    query.setInteger("centerId", centerId);
    query.setString("tpaId", tpaId);
    return query.list();
  }


  private static final String GET_VALID_ACTIVITY_IDS = "SELECT "
      + "bcc.chargeId.chargeId AS activity_id "
      + "FROM BillChargeClaimModel bcc JOIN bcc.billNo b "
      + "JOIN b.visitId pr JOIN pr.patientDetails pd "
      + "WHERE bcc.sponsorId = :tpaId AND bcc.chargeId.chargeId IN (:activityIds) "
      + "AND b.status = 'F' AND pr.centerId.centerId = :centerId "
      ;

  /**
   * Gets valid activity ids.
   *
   * @param activityIds the activity ids
   * @param tpaId       the tpa id
   * @param centerId    the center id
   * @return the valid activity ids
   */
  public List<String> getValidActivityIds(List<String> activityIds, String tpaId,
      Integer centerId) {
    Session session = getSession();
    Query query = session.createQuery(GET_VALID_ACTIVITY_IDS);
    query.setParameterList("activityIds", activityIds);
    query.setInteger("centerId", centerId);
    query.setString("tpaId", tpaId);
    return query.list();
  }

  private static final String GET_BILLS_BY_ACTIVITY_ID = "SELECT "
      + "new map(b.billNo AS bill_no, bcc.id.claimId AS claim_id, "
      + "pr.patientId AS visit_id, concat(pd.patientName, ' ', "
      + "pd.middleName, ' ', pd.lastName) AS patient_name, pd.mrNo AS mr_no, "
      + "SUM(bcc.insuranceClaimAmt + bcc.taxAmount) AS insurance_claim_amount, "
      + "SUM(COALESCE(bcc.claimRecdTotal,0)) AS past_paid_amount)"
      + " FROM BillChargeClaimModel bcc JOIN bcc.billNo b"
      + " JOIN b.visitId pr JOIN pr.patientDetails pd"
      + " WHERE bcc.chargeId.chargeId IN (:activityList) "
      + " AND bcc.insuranceClaimAmt + bcc.taxAmount > 0 "
      + " AND bcc.sponsorId.tpaId = :tpaId"
      + " AND b.visitId.centerId.centerId = :centerId"
      + " GROUP BY b.billNo, bcc.claimId, pr.patientId, pd.patientName, pd.mrNo";

  /**
   * Gets the bills by activity id.
   *
   * @param activityList the activity list
   * @return the bills by activity list
   */

  public List<Map<String, Object>> getBillsByActivityId(List<String> activityList, Integer centerId,
      String tpaId) {
    Session session = getSession();

    Query query = session.createQuery(GET_BILLS_BY_ACTIVITY_ID);
    query.setParameterList("activityList", activityList);
    query.setInteger("centerId", centerId);
    query.setString("tpaId", tpaId);
    return query.list();
  }

}
