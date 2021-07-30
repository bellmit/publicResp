package com.insta.hms.insurance;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.billing.Bill;
import com.insta.hms.billing.BillBO;
import com.insta.hms.billing.BillChargeClaimDAO;
import com.insta.hms.billing.BillClaimDAO;
import com.insta.hms.billing.BillDAO;
import com.insta.hms.billing.ChargeDAO;
import com.insta.hms.billing.ClaimDAO;
import com.insta.hms.billing.ClaimSubmissionDAO;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDTO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.RowSetDynaClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The Class RemittanceAdviceDAO.
 *
 * @author lakshmi.p
 */
public class RemittanceAdviceDAO extends GenericDAO {

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(RemittanceAdviceDAO.class);

  /** The submitdao. */
  ClaimSubmissionDAO submitdao = new ClaimSubmissionDAO();

  /** The claimdao. */
  ClaimDAO claimdao = new ClaimDAO();

  /** The billdao. */
  GenericDAO billdao = new GenericDAO("bill");

  /** The charge. */
  GenericDAO charge = new GenericDAO("bill_charge");

  /** The chg claim DAO. */
  GenericDAO chgClaimDAO = new GenericDAO("bill_charge_claim");

  /** The item. */
  GenericDAO item = new GenericDAO("store_sales_details");

  /** The allocdao. */
  GenericDAO allocdao = new GenericDAO("insurance_payment_allocation");
  
  private static final GenericDAO insurancePaymentUnallocAmountDAO =
      new GenericDAO("insurance_payment_unalloc_amount");
  
  private static final GenericDAO billCalimDAO = new GenericDAO("bill_claim");
  
  private static final GenericDAO salesClaimDetailsDAO = new GenericDAO("sales_claim_details");

  /**
   * Instantiates a new remittance advice DAO.
   */
  public RemittanceAdviceDAO() {
    super("insurance_remittance");
  }

  // MP TODO : We need to show the plans / claims as well, since that is the field that
  // distingushes remittances for different claims

  /** The Constant SEARCH_FIELDS. */
  private static final String SEARCH_FIELDS = " SELECT ic.insurance_co_id, ic.insurance_co_name, "
      + " ir.tpa_id, tp.tpa_name, ir.remittance_id, received_date::date,"
      + " file_name, ir.detail_level ";

  /** The Constant SEARCH_COUNT. */
  private static final String SEARCH_COUNT = " SELECT count(*) ";

  /** The Constant SEARCH_TABLE. */
  private static final String SEARCH_TABLE = " FROM insurance_remittance ir "
      + " LEFT JOIN insurance_company_master ic USING (insurance_co_id) "
      + " JOIN tpa_master tp ON (tp.tpa_id=ir.tpa_id) ";

  /**
   * Search.
   *
   * @param requestParams
   *          the request params
   * @param listing
   *          the listing
   * @return the paged list
   * @throws SQLException
   *           the SQL exception
   * @throws ParseException
   *           the parse exception
   */
  public PagedList search(Map requestParams, Map listing) throws SQLException, ParseException {

    Connection con = DataBaseUtil.getReadOnlyConnection();

    SearchQueryBuilder qb = new SearchQueryBuilder(con, SEARCH_FIELDS, SEARCH_COUNT, SEARCH_TABLE,
        listing);

    qb.addFilterFromParamMap(requestParams);
    qb.addSecondarySort("remittance_id", true);

    qb.build();
    PagedList pagedList = qb.getMappedPagedList();
    qb.close();
    DataBaseUtil.closeConnections(con, null);
    return pagedList;
  }

  /**
   * Update bills for remittance advice.Process bills received in remittance.
   *
   * @param con
   *          the con
   * @param claims
   *          the claims
   * @param bean
   *          the bean
   * @return the map
   * @throws Exception
   *           the exception
   */
  public Map updateBillsForRemittanceAdvice(Connection con, ArrayList<RemittanceAdviceClaim> claims,
      BasicDynaBean bean) throws Exception {

    String path = RequestContext.getHttpRequest().getContextPath();
    int remittanceid = (Integer) bean.get("remittance_id");
    String isRecovery = (String) bean.get("is_recovery");
    BasicDynaBean remittancebean = new RemittanceAdviceDAO().findByKey(con, "remittance_id",
        remittanceid);

    Map map = new HashMap();

    ArrayList<String> bills = new ArrayList<String>();

    ArrayList<String> pharmacyCharges = new ArrayList<String>();
    Map<String, String> phChgAndClaimIds = new HashMap<String, String>();

    ArrayList<BasicDynaBean> chargeBeansList = new ArrayList<BasicDynaBean>();
    ArrayList<BasicDynaBean> chargeClaimBeanList = new ArrayList<BasicDynaBean>();
    ArrayList<BasicDynaBean> parmacyItemBeansList = new ArrayList<BasicDynaBean>();

    ArrayList<BasicDynaBean> insuranceUnAllocPaymentList = new ArrayList<BasicDynaBean>();
    ArrayList<BasicDynaBean> billUnAllocPaymentList = new ArrayList<BasicDynaBean>();
    ArrayList<BasicDynaBean> insurancePaymentList = new ArrayList<BasicDynaBean>();
    ArrayList<BasicDynaBean> insuranceClaimsList = new ArrayList<BasicDynaBean>();

    /*
     * Create insurance_payment_allocation beans i.e insert activity details into insurance payment
     * allocation table.
     */
    for (RemittanceAdviceClaim claim : claims) {

      String claimID = claim.getClaimID();
      BasicDynaBean insuClaimBean = claimdao.getClaimById(claimID);
      int resubmissionCount = insuClaimBean.get("resubmission_count") != null
          ? (Integer) insuClaimBean.get("resubmission_count")
          : 0;
      String isResubmission = resubmissionCount > 0 ? "Y" : "N";
      String resubmissionType = insuClaimBean.get("resubmission_type") != null
          ? ((String) insuClaimBean.get("resubmission_type")).toString()
          : null;
      BasicDynaBean batchBean = submitdao.getLatestSubmissionBatch(claimID, isResubmission);
      String batchStatus = (batchBean != null && batchBean.get("status") != null)
          ? (String) batchBean.get("status")
          : null;
      String batchId = (batchBean != null && batchBean.get("submission_batch_id") != null)
          ? (String) batchBean.get("submission_batch_id")
          : null;

      if (batchBean == null || batchId == null) {
        map.put("error",
            "Invalid Claim ID " + claimID + ". No Submission Batch ID found for the Claim");
        return map;
      }

      if (batchStatus == null || !batchStatus.equals("S")) {
        map.put("error", "Submission Batch " + batchId
            + " is not marked as Sent. Please mark it as Sent and Upload again.");
        return map;
      }

      // insuClaimBean.set("payers_reference_no", claim.getIdPayer());

      BasicDynaBean insClaimBean = claimdao.findByKey("claim_id", claimID);
      insClaimBean.set("payers_reference_no", claim.getIdPayer());
      insuranceClaimsList.add(insClaimBean);

      ArrayList<RemittanceAdviceBill> remBills = claim.getBills();

      for (RemittanceAdviceBill remittancebill : remBills) {

        String billNo = remittancebill.getBillNo();
        String denialRemarks = remittancebill.getDenialRemarks();
        BigDecimal paymentAmount = remittancebill.getPaymentAmount();
        String paymentReference = remittancebill.getPaymentReference();
        Date paymentDate = (Date) remittancebean.get("received_date");

        BasicDynaBean billbean = billdao.findByKey("bill_no", billNo);

        BigDecimal totalClaim = (BigDecimal) billbean.get("total_claim");
        BigDecimal claimRecdAmount = (BigDecimal) billbean.get("claim_recd_amount");
        BigDecimal claimRcdUnallocAmount = (BigDecimal) billbean.get("claim_recd_unalloc_amount");

        boolean received = (totalClaim.compareTo(paymentAmount) == 0);

        // If bill receives total claim amount, allocate claim amount to all charges in bill.
        if (received) {

          BasicDynaBean insuUnAllocPaymentBean = insurancePaymentUnallocAmountDAO.getBean();
          insuUnAllocPaymentBean.set("remittance_id", remittanceid);
          insuUnAllocPaymentBean.set("bill_no", billNo);
          insuUnAllocPaymentBean.set("amount_recd", paymentAmount);
          insuUnAllocPaymentBean.set("unalloc_amount", BigDecimal.ZERO);
          insuUnAllocPaymentBean.set("payment_reference", paymentReference);
          insuUnAllocPaymentBean.set("denial_remarks", denialRemarks);
          insuUnAllocPaymentBean.set("payment_recd_date", paymentDate);
          insuUnAllocPaymentBean.set("is_recovery", isRecovery);

          insuranceUnAllocPaymentList.add(insuUnAllocPaymentBean);

          List<BasicDynaBean> billPaymentRefList = getBillPaymentReferenceList(billNo,
              paymentReference, isRecovery);

          if (billPaymentRefList != null && billPaymentRefList.size() > 0) {

            BasicDynaBean billPaymentRefBean = billPaymentRefList.get(0);
            if (billPaymentRefBean.get("remittance_id") == null
                || (Integer) billPaymentRefBean.get("remittance_id") == 0) {
              String err = "Duplicate payment reference. " + " </br> Bill No. : " + billNo
                  + " has payment amount <b> " + paymentAmount
                  + " </b> with payment reference : <b>" + paymentReference + " </b>"
                  + " </br> Please check the bill remittance for payments. "
                  + submitdao.urlString(path, "bill-remittance", billNo, billNo);
              map.put("error", err);
              return map;
            }
            BasicDynaBean remittanceBean = findByKey("remittance_id",
                (Integer) billPaymentRefBean.get("remittance_id"));
            String remFileName = (String) remittanceBean.get("file_name");
            String remRecdDate = DateUtil.formatDate((Date) remittanceBean.get("received_date"));
            String remittanceId = ((Integer) remittanceBean.get("remittance_id")).toString();

            String err = "Duplicate payment reference. " + " </br> Bill No. : " + billNo
                + " has payment amount <b> " + paymentAmount + " </b> with payment reference : <b>"
                + paymentReference + " </b>"
                + " </br> Correct this payment reference and Upload again <b> (or) </b> "
                + " </br> Please check the remittance file received on " + remRecdDate + " </br> "
                + submitdao.urlString(path, "ins-remittance", remittanceId, remFileName);
            map.put("error", err);
            return map;
          }

          List<BasicDynaBean> billCharges = new ArrayList<BasicDynaBean>();
          if (isResubmission.equals("Y")
              && (resubmissionType.equalsIgnoreCase("internal complaint")
                || resubmissionType.equalsIgnoreCase("reconciliation"))) {
            billCharges = claimdao.findAllChargesForResub(billNo, claimID);
          } else {
            billCharges = claimdao.findAllCharges(billNo, claimID);
          }

          for (BasicDynaBean activity : billCharges) {
            BasicDynaBean insuPaymentBean = allocdao.getBean();
            String activityId = (String) activity.get("activity_charge_id");

            if ((activityId.split("-")).length < 2 || (!(activityId.split("-")[0]).equals("A")
                && !(activityId.split("-")[0]).equals("P"))) {
              String err = "Invalid activity found : Activity Id : " + activityId
                  + " is not prefixed with A or P or not a valid activity id.";
              map.put("error", err);
              return map;
            }

            BigDecimal activityPaymentAmount = (BigDecimal) activity.get("insurance_claim_amt");

            insuPaymentBean.set("charge_type", activityId.split("-")[0]);
            insuPaymentBean.set("charge_id", activityId.split("-")[1]);
            insuPaymentBean.set("denial_code", null);
            insuPaymentBean.set("payment_reference", claim.getPaymentReference());
            insuPaymentBean.set("amount", activityPaymentAmount);
            insuPaymentBean.set("remittance_id", remittanceid);
            insuPaymentBean.set("denial_remarks", denialRemarks);
            insuPaymentBean.set("is_recovery", isRecovery);
            insuPaymentBean.set("sale_item_id", 0);
            insuPaymentBean.set("claim_id", claimID);

            if (activityId.startsWith("P-")) {
              if ((activityId.split("-")).length < 3) {
                String err = "Invalid activity found : Activity Id : " + activityId
                    + " prefixed with P does not have sale item id.";
                map.put("error", err);
                return map;
              }
              insuPaymentBean.set("sale_item_id", new Integer(activityId.split("-")[2]));
            }

            insurancePaymentList.add(insuPaymentBean);
          }

          billbean.set("claim_recd_amount", paymentAmount);
          billbean.set("claim_recd_unalloc_amount", BigDecimal.ZERO);
          billUnAllocPaymentList.add(billbean);

        } else {
          BasicDynaBean insuUnAllocPaymentBean = insurancePaymentUnallocAmountDAO.getBean();
          insuUnAllocPaymentBean.set("remittance_id", remittanceid);
          insuUnAllocPaymentBean.set("bill_no", billNo);
          insuUnAllocPaymentBean.set("amount_recd", paymentAmount);
          insuUnAllocPaymentBean.set("unalloc_amount", paymentAmount);
          insuUnAllocPaymentBean.set("payment_reference", paymentReference);
          insuUnAllocPaymentBean.set("denial_remarks", denialRemarks);
          insuUnAllocPaymentBean.set("payment_recd_date", paymentDate);
          insuUnAllocPaymentBean.set("is_recovery", isRecovery);

          insuranceUnAllocPaymentList.add(insuUnAllocPaymentBean);

          billbean.set("claim_recd_unalloc_amount", claimRcdUnallocAmount.add(paymentAmount));
          billUnAllocPaymentList.add(billbean);
        }

        bills.add(billNo);
      }
    }

    /*
     * Iterate payment list to filter hospital & pharmacy activities.
     */
    for (BasicDynaBean actBean : insurancePaymentList) {

      String chargeType = (String) actBean.get("charge_type");
      String chargeId = (String) actBean.get("charge_id");
      BigDecimal amount = (BigDecimal) actBean.get("amount");
      String claimId = (String) actBean.get("claim_id");

      if (chargeType.equals("A")) {

        BasicDynaBean billChrgPaymentBean = getCharge(con, chargeId);

        billChrgPaymentBean.set("claim_recd_total", amount);
        billChrgPaymentBean.set("claim_status", "C");

        chargeBeansList.add(billChrgPaymentBean);

        BasicDynaBean billchgClaimBean = getChargeClaim(con, chargeId, claimId);

        billchgClaimBean.set("claim_recd_total", amount);
        billchgClaimBean.set("claim_status", "C");

        chargeClaimBeanList.add(billchgClaimBean);

      } else if (chargeType.equals("P")) {
        Integer saleItemId = (Integer) actBean.get("sale_item_id");
        BasicDynaBean saleItemPaymentBean = getSaleItem(con, saleItemId);

        saleItemPaymentBean.set("claim_recd_total", amount);
        saleItemPaymentBean.set("claim_status", "C");

        parmacyItemBeansList.add(saleItemPaymentBean);

        // Fetch item related charges to be updated.
        if (!pharmacyCharges.contains(chargeId)) {
          pharmacyCharges.add(chargeId);
          phChgAndClaimIds.put(chargeId, claimId);
        }
      }
    }

    // Update all pharmacy sale items.
    for (BasicDynaBean pb : parmacyItemBeansList) {
      item.updateWithName(con, pb.getMap(), "sale_item_id");
    }

    // Update sale related charge in bill_charge by fetching sale id from bill activity charge.
    for (String phCharge : pharmacyCharges) {

      BasicDynaBean sale = findSaleCharge(con, phCharge);
      String saleId = (String) sale.get("sale_id");

      List<BasicDynaBean> items = findItemsBySaleId(con, saleId);

      BigDecimal totalPhInsAmt = BigDecimal.ZERO;
      for (BasicDynaBean item : items) {
        totalPhInsAmt = totalPhInsAmt.add((BigDecimal) item.get("claim_recd_total"));
      }

      BasicDynaBean phchargebean = getCharge(con, phCharge);

      phchargebean.set("claim_recd_total", totalPhInsAmt);
      phchargebean.set("claim_status", "C");

      chargeBeansList.add(phchargebean);

      BasicDynaBean phChgClaimBean = getChargeClaim(con, phCharge, phChgAndClaimIds.get(phCharge));
      phChgClaimBean.set("claim_recd_total", totalPhInsAmt);
      phChgClaimBean.set("claim_status", "C");
      chargeClaimBeanList.add(phChgClaimBean);
    }

    // Update all payers reference ids (ID payer) for the claims.
    for (BasicDynaBean insClaim : insuranceClaimsList) {
      claimdao.updateWithName(con, insClaim.getMap(), "claim_id");
    }

    // Update all charges i.e activities in the received remittance
    for (BasicDynaBean chargeBean : chargeBeansList) {
      charge.updateWithName(con, chargeBean.getMap(), "charge_id");
    }

    for (BasicDynaBean clargeClaimBean : chargeClaimBeanList) {
      Map<String, String> keys = new HashMap<String, String>();
      String chargeId = (String) clargeClaimBean.get("charge_id");
      String claimId = (String) clargeClaimBean.get("claim_id");
      keys.put("charge_id", chargeId);
      keys.put("claim_id", claimId);
      chgClaimDAO.update(con, clargeClaimBean.getMap(), keys);
    }

    // Insert activity details into insurance payment allocation table.
    allocdao.insertAll(con, insurancePaymentList);

    // Insert unallocated amount for the bills.
    insurancePaymentUnallocAmountDAO.insertAll(con, insuranceUnAllocPaymentList);

    // Update claim unallocated amount for the bills.
    for (BasicDynaBean bl : billUnAllocPaymentList) {
      billdao.updateWithName(con, bl.getMap(), "bill_no");
    }

    map.put("error", null);
    map.put("bills", bills);

    return map;
  }

  /**
   * Update charges for remittance advice.
   *
   * @param con
   *          the con
   * @param claims
   *          the claims
   * @param bean
   *          the bean
   * @return the map
   * @throws Exception
   *           the exception
   */
  public Map updateChargesForRemittanceAdvice(Connection con, List<RemittanceAdviceClaim> claims,
      BasicDynaBean bean) throws Exception {

    int remittanceId = (Integer) bean.get("remittance_id");
    String isRecovery = (String) bean.get("is_recovery");
    Map map = new HashMap();

    Map<String, String> splDenialcodesMap = new HashMap<String, String>();
    List<BasicDynaBean> denialCodesList = getSpecialDenialCodesOnCorrection();

    for (BasicDynaBean codeBean : denialCodesList) {
      splDenialcodesMap.put((String) codeBean.get("denial_code"), "");
    }

    ArrayList<String> activityBills = new ArrayList<String>();
    ArrayList<String> activityClaims = new ArrayList<String>();

    HashMap<String, BasicDynaBean> pharmacyCharges = new HashMap<String, BasicDynaBean>();

    ArrayList<BasicDynaBean> chargeBeansList = new ArrayList<BasicDynaBean>();
    ArrayList<BasicDynaBean> parmacyItemBeansList = new ArrayList<BasicDynaBean>();

    ArrayList<BasicDynaBean> insurancePaymentList = new ArrayList<BasicDynaBean>();
    ArrayList<BasicDynaBean> insuranceClaimsList = new ArrayList<BasicDynaBean>();

    /*
     * Create insurance_payment_allocation beans i.e insert activity details into insurance payment
     * allocation table.
     */

    setInsuranceAmtPaymentAllocationList(claims, map, insuranceClaimsList, insurancePaymentList,
        remittanceId, isRecovery);

    // Update all payers reference ids (ID payer) for the claims.
    if (!errorExists(map)) {
      updateAllPayerRefIdsForClaims(con, insuranceClaimsList);
    }

    // Iterate payment list to filter hospital & pharmacy activities.
    if (!errorExists(map)) {
      processPaymentList(con, insurancePaymentList, map, chargeBeansList, activityBills,
          activityClaims, bean, insuranceClaimsList, parmacyItemBeansList, pharmacyCharges,
          splDenialcodesMap);
    }

    if (!errorExists(map)) {
      updateAllPharmacySaleItems(con, parmacyItemBeansList);
    }

    // Update sale related charge in bill_charge by fetching sale id from bill activity charge.

    if (!errorExists(map)) {
      getChargeBeanListForpharmacyItems(con, pharmacyCharges, activityBills, activityClaims,
          chargeBeansList);
    }

    if (!errorExists(map)) {
      updateAllActivities(con, chargeBeansList);
    }

    // Insert activity details into insurance payment allocation table.
    if (!errorExists(map)) {
      allocdao.insertAll(con, insurancePaymentList);
    }

    // Update claim recd amount for the bills.
    if (!errorExists(map)) {
      updateAllActivityBills(con, activityBills);
    }

    if (errorExists(map)) {
      return map;
    }

    map.put("error", null);
    map.put("bills", activityBills);
    map.put("claims", activityClaims);

    return map;
  }

  /** The Constant GET_SPL_DENIAL_CODES_ON_CORRECTION. */
  public static final String GET_SPL_DENIAL_CODES_ON_CORRECTION = 
      " SELECT * FROM insurance_denial_codes WHERE special_denial_code_on_correction ='Y' "
      + " AND status='A' ";

  /**
   * Gets the special denial codes on correction.
   *
   * @return the special denial codes on correction
   * @throws SQLException
   *           the SQL exception
   */
  private List<BasicDynaBean> getSpecialDenialCodesOnCorrection() throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(GET_SPL_DENIAL_CODES_ON_CORRECTION);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      if (ps != null) {
        ps.close();
      }
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Error exists.
   *
   * @param map
   *          the map
   * @return true, if successful
   */
  private boolean errorExists(Map map) {
    if (null != map.get("error") && !map.get("error").equals("")) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * Update all activity bills.
   *
   * @param con
   *          the con
   * @param activityBills
   *          the activity bills
   * @throws SQLException
   *           the SQL exception
   * @throws Exception
   *           the exception
   */
  public void updateAllActivityBills(Connection con, ArrayList<String> activityBills)
      throws SQLException, Exception {

    for (String billNo : activityBills) {
      BasicDynaBean billbean = billdao.findByKey("bill_no", billNo);
      billbean.set("claim_recd_amount", getBillClaimRecdTotal(con, billNo));
      billdao.updateWithName(con, billbean.getMap(), "bill_no");
    }
  }

  /**
   * Update all activities.
   *
   * @param con
   *          the con
   * @param chargeBeansList
   *          the charge beans list
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public void updateAllActivities(Connection con, ArrayList<BasicDynaBean> chargeBeansList)
      throws SQLException, IOException {
    HashMap keyMap = new HashMap();
    // Update all charges i.e activities in the received remittance
    for (BasicDynaBean chargeBean : chargeBeansList) {
      // charge.updateWithName(con, b.getMap(), "charge_id");
      keyMap.put("charge_id", chargeBean.get("charge_id"));
      keyMap.put("claim_id", chargeBean.get("claim_id"));
      billCalimDAO.update(con, chargeBean.getMap(), keyMap);
    }
  }

  /**
   * Sets the insurance amt payment allocation list.
   *
   * @param claims
   *          the claims
   * @param map
   *          the map
   * @param insuranceClaimsList
   *          the insurance claims list
   * @param insurancePaymentList
   *          the insurance payment list
   * @param remittanceId
   *          the remittance id
   * @param isRecovery
   *          the is recovery
   * @throws SQLException
   *           the SQL exception
   */
  public void setInsuranceAmtPaymentAllocationList(List<RemittanceAdviceClaim> claims, Map map,
      ArrayList<BasicDynaBean> insuranceClaimsList, ArrayList<BasicDynaBean> insurancePaymentList,
      int remittanceId, String isRecovery) throws SQLException {

    for (RemittanceAdviceClaim claim : claims) {

      String claimID = claim.getClaimID();
      BasicDynaBean insuClaimBean = claimdao.getClaimById(claimID);
      int resubmissionCount = insuClaimBean.get("resubmission_count") != null
          ? (Integer) insuClaimBean.get("resubmission_count")
          : 0;
      String isResubmission = resubmissionCount > 0 ? "Y" : "N";
      boolean submissionIdWithCorrectionExists = insuClaimBean
          .get("last_submission_batch_id") != null
          && !insuClaimBean.get("last_submission_batch_id").equals("");
      BasicDynaBean batchBean = submitdao.getLatestSubmissionBatch(claimID, isResubmission);
      String batchStatus = (batchBean != null && batchBean.get("status") != null)
          ? (String) batchBean.get("status")
          : null;
      String batchId = (batchBean != null && batchBean.get("submission_batch_id") != null)
          ? (String) batchBean.get("submission_batch_id")
          : null;

      if (batchBean == null || batchId == null) {
        map.put("error",
            "Invalid Claim ID " + claimID + ". No Submission Batch ID found for the Claim");
        return;
      }

      if (batchStatus == null || !batchStatus.equals("S")) {
        map.put("error", "Submission Batch " + batchId
            + " is not marked as Sent. Please mark it as Sent and Upload again.");
        return;
      }

      BasicDynaBean insClaimBean = claimdao.findByKey("claim_id", claimID);

      insClaimBean.set("payers_reference_no", claim.getIdPayer());
      insuranceClaimsList.add(insClaimBean);

      ArrayList<RemittanceAdviceActivity> activities = claim.getActivities();

      for (RemittanceAdviceActivity activity : activities) {

        String activityId = activity.getActivityID();
        String denialCode = activity.getActivityDenialCode();
        String denialRemarks = activity.getDenialRemarks();

        if ((activityId.split("-")).length < 2 || (!(activityId.split("-")[0]).equals("A")
            && !(activityId.split("-")[0]).equals("P"))) {
          String err = "Invalid activity found : Activity Id : " + activityId
              + " is not prefixed with A or P or not a valid activity id.";
          map.put("error", err);
          return;
        }

        if (activityId.startsWith("P-")) {

          if ((activityId.split("-")).length < 3) {
            String err = "Invalid activity found : Activity Id : " + activityId
                + " prefixed with P does not have sale item id.";
            map.put("error", err);
            return;
          }

        }

        // pass activity id and get all charges of that activity id (select charge id,claim amt
        // and total amount)
        List<BasicDynaBean> claimActivityCharges = new ArrayList<BasicDynaBean>();

        boolean isRepeatedItem = activityId.split("-")[1].equals("ACT");

        String claimActivityID = isRepeatedItem ? activityId.substring(2)
            : (activityId.startsWith("P-")
                ? activityId.split("-")[1].concat("-").concat(activityId.split("-")[2])
                : activityId.split("-")[1]);

        String resubmissionType = getClaimResubmissionType(insuranceClaimsList, claimID);

        if (resubmissionType != null && (resubmissionType.equals("correction")
            || resubmissionType.equals("legacy") || submissionIdWithCorrectionExists)) {
          if (isRepeatedItem) {
            if (activityId.startsWith("A-")) {
              claimActivityID = claimActivityID.split("-")[0].concat("-")
                  .concat(claimActivityID.split("-")[1]);
            } else {
              claimActivityID = claimActivityID.split("-")[0].concat("-")
                  .concat(claimActivityID.split("-")[1].concat("-")
                      .concat((claimActivityID.split("-")[2])));
            }
          } else {
            if (activityId.startsWith("A-")) {
              claimActivityID = claimActivityID.split("-")[0];
            } else {
              claimActivityID = claimActivityID.split("-")[0].concat("-")
                  .concat(claimActivityID.split("-")[1]);
            }
          }
        }

        claimActivityCharges = getClaimActivityChagres(activityId, claimActivityID,
            resubmissionType, claimID);

        for (BasicDynaBean bean : claimActivityCharges) {
          BasicDynaBean insuPaymentBean = allocdao.getBean();

          insuPaymentBean.set("sale_item_id", 0);
          if (activityId.startsWith("P-")) {
            insuPaymentBean.set("sale_item_id", (Integer) bean.get("sale_item_id"));
          }

          insuPaymentBean.set("charge_id", (String) bean.get("charge_id"));
          insuPaymentBean.set("charge_type", activityId.split("-")[0]);
          insuPaymentBean.set("denial_code",
              claim.getDenialCode() == null ? denialCode : claim.getDenialCode());
          insuPaymentBean.set("payment_reference", claim.getPaymentReference());
          // (claim amount/total amount) * received amount

          BigDecimal insClaimAmt = (BigDecimal) bean.get("insurance_claim_amt");
          BigDecimal totalClaimAmt = (BigDecimal) bean.get("total_claim_amt");

          BigDecimal totalReceivedAmt = activity.getPaymentAmount();

          BigDecimal receivedAmt = BigDecimal.ZERO;

          if (totalClaimAmt.compareTo(BigDecimal.ZERO) > 0) {
            receivedAmt = ConversionUtils.setScale((insClaimAmt.multiply(totalReceivedAmt))
                .divide(totalClaimAmt, BigDecimal.ROUND_HALF_UP));
          } else {
            receivedAmt = totalReceivedAmt;
          }

          insuPaymentBean.set("amount", receivedAmt);
          insuPaymentBean.set("remittance_id", remittanceId);
          insuPaymentBean.set("denial_remarks", denialRemarks);
          insuPaymentBean.set("is_recovery", isRecovery);

          insuPaymentBean.set("claim_id", claimID);

          insurancePaymentList.add(insuPaymentBean);

        }

      }
    }

  }

  /** The Constant GET_CLAIM_ACTIVITY_CHARGES. */
  private static final String GET_CLAIM_ACTIVITY_CHARGES = "SELECT bcc.charge_id, "
      + " (bcc.insurance_claim_amt + bc.return_insurance_claim_amt) as insurance_claim_amt, "
      + " sum(bcc.insurance_claim_amt + bc.return_insurance_claim_amt) "
      + " OVER (partition BY claim_activity_id) AS total_claim_amt "
      + " FROM bill_charge_claim bcc " + " JOIN bill_charge bc ON(bcc.charge_id = bc.charge_id) "
      + " WHERE claim_activity_id = ?  AND bcc.claim_id = ? ";

  /** The Constant GET_SALES_CLAIM_ACTIVITY_CHARGES. */
  private static final String GET_SALES_CLAIM_ACTIVITY_CHARGES = 
      " SELECT ssm.charge_id,scd.sale_item_id,scd.insurance_claim_amt, "
      + " sum(scd.insurance_claim_amt) OVER (partition BY scd.claim_activity_id) "
      + " AS total_claim_amt " + " FROM sales_claim_details scd "
      + " JOIN store_sales_details ssd ON(scd.sale_item_id = ssd.sale_item_id) "
      + " JOIN store_sales_main ssm ON(ssd.sale_id = ssm.sale_id) "
      + " WHERE scd.claim_activity_id = ?  AND scd.claim_id = ? ";

  /** The Constant GET_CLAIM_ACTIVITY_CHARGES_RESUBMISSION. */
  private static final String GET_CLAIM_ACTIVITY_CHARGES_RESUBMISSION = "SELECT bcc.charge_id,"
      + " (bcc.insurance_claim_amt + bc.return_insurance_claim_amt - bcc.claim_recd_total) "
      + " as insurance_claim_amt, "
      + " sum(bcc.insurance_claim_amt + bc.return_insurance_claim_amt - bcc.claim_recd_total) "
      + " OVER (partition BY claim_activity_id) AS total_claim_amt "
      + " FROM bill_charge_claim bcc " + " JOIN bill_charge bc ON(bc.charge_id = bcc.charge_id) "
      + " WHERE claim_activity_id = ?  AND bcc.claim_id = ? ";

  /** The Constant GET_SALES_CLAIM_ACTIVITY_CHARGES_RESUBMISSION. */
  private static final String GET_SALES_CLAIM_ACTIVITY_CHARGES_RESUBMISSION = 
      "SELECT ssm.charge_id,scd.sale_item_id,"
      + " (scd.insurance_claim_amt-claim_recd) as insurance_claim_amt, "
      + " sum(scd.insurance_claim_amt-claim_recd) OVER "
      + " (partition BY scd.claim_activity_id) AS total_claim_amt "
      + " FROM sales_claim_details scd "
      + " JOIN store_sales_details ssd ON(scd.sale_item_id = ssd.sale_item_id) "
      + " JOIN store_sales_main ssm ON(ssd.sale_id = ssm.sale_id) "
      + " WHERE scd.claim_activity_id = ?  and scd.claim_id = ? ";

  /**
   * Gets the claim activity chagres.
   *
   * @param activityId
   *          the activity id
   * @param claimActivityId
   *          the claim activity id
   * @param resubmissionType
   *          the resubmission type
   * @param claimId
   *          the claim id
   * @return the claim activity chagres
   * @throws SQLException
   *           the SQL exception
   */
  private List<BasicDynaBean> getClaimActivityChagres(String activityId, String claimActivityId,
      String resubmissionType, String claimId) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    String query = activityId.startsWith("A-") ? GET_CLAIM_ACTIVITY_CHARGES
        : GET_SALES_CLAIM_ACTIVITY_CHARGES;

    if (resubmissionType != null 
        && (resubmissionType.equals("internal complaint")
          || resubmissionType.equalsIgnoreCase("reconciliation"))) {
      query = activityId.startsWith("A") ? GET_CLAIM_ACTIVITY_CHARGES_RESUBMISSION
          : GET_SALES_CLAIM_ACTIVITY_CHARGES_RESUBMISSION;
    }
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(query);
      ps.setString(1, claimActivityId);
      ps.setString(2, claimId);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Process payment list.
   *
   * @param con
   *          the con
   * @param insurancePaymentList
   *          the insurance payment list
   * @param map
   *          the map
   * @param chargeBeansList
   *          the charge beans list
   * @param activityBills
   *          the activity bills
   * @param activityClaims
   *          the activity claims
   * @param bean
   *          the bean
   * @param insuranceClaimsList
   *          the insurance claims list
   * @param parmacyItemBeansList
   *          the parmacy item beans list
   * @param pharmacyCharges
   *          the pharmacy charges
   * @param splDenialcodesMap
   *          the spl denialcodes map
   * @throws SQLException
   *           the SQL exception
   */
  public void processPaymentList(Connection con, List<BasicDynaBean> insurancePaymentList, Map map,
      ArrayList<BasicDynaBean> chargeBeansList, ArrayList<String> activityBills,
      ArrayList<String> activityClaims, BasicDynaBean bean,
      ArrayList<BasicDynaBean> insuranceClaimsList, ArrayList<BasicDynaBean> parmacyItemBeansList,
      HashMap<String, BasicDynaBean> pharmacyCharges, Map<String, String> splDenialcodesMap)
      throws SQLException {

    StringBuilder allErrors = new StringBuilder();

    for (BasicDynaBean actBean : insurancePaymentList) {

      String chargeType = actBean.get("charge_type") == null ? ""
          : (String) actBean.get("charge_type");
      if (chargeType.equals("")) {
        allErrors.append(
            "Invalid activity(s) found : claims have some activity(s) not prefixed with A or P ");
        map.put("error", allErrors.toString());
        return;
      }

      if (chargeType.equals("A")) {

        setHospitalActivitiesFrmPaymentList(con, actBean, map, chargeBeansList, activityBills,
            activityClaims, bean, insuranceClaimsList, splDenialcodesMap);
      } else if (chargeType.equals("P")) {

        setPharmacyActivitiesFrmPaymentList(con, actBean, map, bean, insuranceClaimsList,
            parmacyItemBeansList, pharmacyCharges, splDenialcodesMap);
      }
    }
  }

  /**
   * Sets the hospital activities frm payment list.
   *
   * @param con
   *          the con
   * @param actBean
   *          the act bean
   * @param map
   *          the map
   * @param chargeBeansList
   *          the charge beans list
   * @param activityBills
   *          the activity bills
   * @param activityClaims
   *          the activity claims
   * @param bean
   *          the bean
   * @param insuranceClaimsList
   *          the insurance claims list
   * @param splDenialcodesMap
   *          the spl denialcodes map
   * @throws SQLException
   *           the SQL exception
   */
  public void setHospitalActivitiesFrmPaymentList(Connection con, BasicDynaBean actBean, Map map,
      ArrayList<BasicDynaBean> chargeBeansList, ArrayList<String> activityBills,
      ArrayList<String> activityClaims, BasicDynaBean bean,
      ArrayList<BasicDynaBean> insuranceClaimsList, Map<String, String> splDenialcodesMap)
      throws SQLException {

    String path = RequestContext.getHttpRequest().getContextPath();
    final String isRecovery = (String) bean.get("is_recovery");

    final String paymentReference = actBean.get("payment_reference") == null ? ""
        : (String) actBean.get("payment_reference");
    String chargeId = actBean.get("charge_id") == null ? "" : (String) actBean.get("charge_id");
    final String denialCode = actBean.get("denial_code") == null ? ""
        : (String) actBean.get("denial_code");
    final String denialRemarks = actBean.get("denial_remarks") == null ? ""
        : (String) actBean.get("denial_remarks");

    BigDecimal existingAllocatedAmt = BigDecimal.ZERO;

    BasicDynaBean billChrgPaymentBean = getChargeClaim(con, chargeId,
        (String) actBean.get("claim_id"));

    checkActivityExists(billChrgPaymentBean, map, chargeId);

    if (errorExists(map) || billChrgPaymentBean == null) {
      return;
    }

    String billNo = (String) billChrgPaymentBean.get("bill_no");
    // String chargeDesc = getChargeDescription(billChrgPaymentBean);
    BigDecimal chargeClaimRecdAmt = billChrgPaymentBean.get("claim_recd_total") == null
        ? BigDecimal.ZERO
        : (BigDecimal) billChrgPaymentBean.get("claim_recd_total");

    List<BasicDynaBean> existAllocBeans = findAllocations((String) actBean.get("claim_id"),
        chargeId, 0);

    checkExistanceOfAllocBeans(existAllocBeans, existingAllocatedAmt, map, path, billNo);

    if (errorExists(map)) {
      return;
    }
    BigDecimal amount = actBean.get("amount") == null ? BigDecimal.ZERO
        : (BigDecimal) actBean.get("amount");

    amount = getclaimReceivedAmount(insuranceClaimsList, actBean, amount, chargeClaimRecdAmt,
        existingAllocatedAmt, isRecovery);

    checkDuplicatePaymentReference(actBean, chargeId, paymentReference, isRecovery, map, path);

    BasicDynaBean billbean = billdao.findByKey("bill_no", billNo);
    if (errorExists(map)) {
      return;
    }

    BigDecimal insuClaimAmt = getInsuranceClaimAmount(billChrgPaymentBean, billbean);

    /*
     * Getting a return insurance claim amount from bill charge table. It will not work in case of
     * multi payer. Need to fix it.
     */

    BigDecimal retInsuClaimAmt = getReturnInsuranceClaimAmt(con, chargeId);

    BigDecimal claimRecdTotal = billChrgPaymentBean.get("claim_recd_total") != null
        ? (BigDecimal) (billChrgPaymentBean.get("claim_recd_total"))
        : BigDecimal.ZERO;

    // checkExcessPayments(amount, insuClaimAmt, retInsuClaimAmt, map, existAllocBeans,
    // claimRecdTotal, path, bill_no);

    if (errorExists(map)) {
      return;
    }

    setbillChargePaymentBean(denialCode, denialRemarks, amount, insuClaimAmt, retInsuClaimAmt,
        billChrgPaymentBean, splDenialcodesMap);

    chargeBeansList.add(billChrgPaymentBean);

    // Fetch charge related bills to be updated.
    if (!activityBills.contains((String) billChrgPaymentBean.get("bill_no"))) {
      activityBills.add((String) billChrgPaymentBean.get("bill_no"));
    }
    if (!activityClaims.contains((String) billChrgPaymentBean.get("claim_id"))) {
      activityClaims.add((String) billChrgPaymentBean.get("claim_id"));
    }

    return;

  }

  /**
   * Setbill charge payment bean.
   *
   * @param denialCode
   *          the denial code
   * @param denialRemarks
   *          the denial remarks
   * @param amount
   *          the amount
   * @param insuClaimAmt
   *          the insu claim amt
   * @param retInsuClaimAmt
   *          the ret insu claim amt
   * @param billChrgPaymentBean
   *          the bill chrg payment bean
   * @param splDenialcodesMap
   *          the spl denialcodes map
   */
  private void setbillChargePaymentBean(String denialCode, String denialRemarks, BigDecimal amount,
      BigDecimal insuClaimAmt, BigDecimal retInsuClaimAmt, BasicDynaBean billChrgPaymentBean,
      Map<String, String> splDenialcodesMap) {
    billChrgPaymentBean.set("claim_recd_total", amount);
    if ((denialCode == null || denialCode.equals(""))
        && (insuClaimAmt.add(retInsuClaimAmt).compareTo(amount) <= 0)) {
      billChrgPaymentBean.set("claim_status", "C");
    } else {
      if (!(splDenialcodesMap.containsKey(denialCode))
          || !((String) billChrgPaymentBean.get("claim_status")).equals("C")) {
        billChrgPaymentBean.set("denial_code", denialCode);
        billChrgPaymentBean.set("denial_remarks", denialRemarks);
        billChrgPaymentBean.set("claim_status", "D");
      }
    }
  }

  /**
   * Check excess payments.
   *
   * @param amount
   *          the amount
   * @param insuClaimAmt
   *          the insu claim amt
   * @param retInsuClaimAmt
   *          the ret insu claim amt
   * @param map
   *          the map
   * @param existAllocBeans
   *          the exist alloc beans
   * @param claimRecdTotal
   *          the claim recd total
   * @param path
   *          the path
   * @param billNo
   *          the bill no
   * @throws SQLException
   *           the SQL exception
   */
  private void checkExcessPayments(BigDecimal amount, BigDecimal insuClaimAmt,
      BigDecimal retInsuClaimAmt, Map map, List<BasicDynaBean> existAllocBeans,
      BigDecimal claimRecdTotal, String path, String billNo) throws SQLException {

    StringBuilder allErrors = new StringBuilder();
    if (amount.compareTo(insuClaimAmt.add(retInsuClaimAmt)) > 0) {

      if (existAllocBeans != null && existAllocBeans.size() > 0) {

        allErrors.append("Excess payment amount " + " </br> "/* +chargeDesc */
            + " has claim received total amount <b> " + claimRecdTotal + " </b>"
            + " </br> Please check the bill remittance for payments. "
            + submitdao.urlString(path, "bill-remittance", billNo, billNo));
        map.put("error", allErrors.toString());
        return;
      }
    }

  }

  /**
   * Gets the return insurance claim amt.
   *
   * @param con
   *          the con
   * @param chargeId
   *          the charge id
   * @return the return insurance claim amt
   * @throws SQLException
   *           the SQL exception
   */
  private BigDecimal getReturnInsuranceClaimAmt(Connection con, String chargeId)
      throws SQLException {
    BasicDynaBean chgBean = charge.findByKey(con, "charge_id", chargeId);
    BigDecimal retInsuClaimAmt = chgBean.get("return_insurance_claim_amt") != null
        ? (BigDecimal) (chgBean.get("return_insurance_claim_amt"))
        : BigDecimal.ZERO;
    return retInsuClaimAmt;

  }

  /**
   * Gets the insurance claim amount.
   *
   * @param billChrgPaymentBean
   *          the bill chrg payment bean
   * @param billbean
   *          the billbean
   * @return the insurance claim amount
   */
  private BigDecimal getInsuranceClaimAmount(BasicDynaBean billChrgPaymentBean,
      BasicDynaBean billbean) {
    BigDecimal insuClaimAmt = billChrgPaymentBean.get("insurance_claim_amt") != null
        ? (BigDecimal) (billChrgPaymentBean.get("insurance_claim_amt"))
        : BigDecimal.ZERO;

    if (((String) billChrgPaymentBean.get("charge_head")).equals("MARDRG")) {
      BigDecimal insuranceDeduction = (BigDecimal) billbean.get("insurance_deduction");
      insuClaimAmt = insuClaimAmt.subtract(insuranceDeduction);
    }
    return insuClaimAmt;
  }

  /**
   * Check activity exists.
   *
   * @param billChrgPaymentBean
   *          the bill chrg payment bean
   * @param map
   *          the map
   * @param chargeId
   *          the charge id
   */
  private void checkActivityExists(BasicDynaBean billChrgPaymentBean, Map map, String chargeId) {
    StringBuilder allErrors = new StringBuilder();
    if (billChrgPaymentBean == null) {
      allErrors.append("No activity found with charge Id :" + chargeId);
      map.put("error", allErrors.toString());
      return;
    }

  }

  /**
   * Check duplicate payment reference.
   *
   * @param actBean
   *          the act bean
   * @param chargeId
   *          the charge id
   * @param paymentReference
   *          the payment reference
   * @param isRecovery
   *          the is recovery
   * @param map
   *          the map
   * @param path
   *          the path
   * @throws SQLException
   *           the SQL exception
   */
  private void checkDuplicatePaymentReference(BasicDynaBean actBean, String chargeId,
      String paymentReference, String isRecovery, Map map, String path) throws SQLException {

    StringBuilder allErrors = new StringBuilder();
    List<BasicDynaBean> chPaymentRefList = getChargePaymentReferenceList(
        (String) actBean.get("claim_id"), chargeId, paymentReference, new Integer(0), isRecovery);

    if (chPaymentRefList != null && chPaymentRefList.size() > 0) {

      BasicDynaBean chPaymentRefBean = chPaymentRefList.get(0);
      BigDecimal chPaymentAmount = (BigDecimal) chPaymentRefBean.get("amount");
      BasicDynaBean remittanceBean = findByKey("remittance_id",
          (Integer) chPaymentRefBean.get("remittance_id"));

      String remFileName = (String) remittanceBean.get("file_name");
      String remRecdDate = DateUtil.formatDate((Date) remittanceBean.get("received_date"));
      String remittanceId = ((Integer) remittanceBean.get("remittance_id")).toString();

      allErrors.append("Duplicate payment reference. " + " </br> "/* +chargeDesc */
          + " has payment amount <b> " + chPaymentAmount + " </b> with payment reference : <b>"
          + paymentReference + " </b>"
          + " </br> Correct this payment reference and Upload again <b> (or) </b> "
          + " </br> Please check the remittance file received on " + remRecdDate + " </br> "
          + submitdao.urlString(path, "ins-remittance", remittanceId, remFileName));
      map.put("error", allErrors.toString());
      return;
    }

  }

  /**
   * Gets the claim received amount.
   *
   * @param insuranceClaimsList
   *          the insurance claims list
   * @param actBean
   *          the act bean
   * @param amount
   *          the amount
   * @param chargeClaimRecdAmt
   *          the charge claim recd amt
   * @param existingAllocatedAmt
   *          the existing allocated amt
   * @param isRecovery
   *          the is recovery
   * @return the claim received amount
   * @throws SQLException
   *           the SQL exception
   */
  private BigDecimal getclaimReceivedAmount(ArrayList<BasicDynaBean> insuranceClaimsList,
      BasicDynaBean actBean, BigDecimal amount, BigDecimal chargeClaimRecdAmt,
      BigDecimal existingAllocatedAmt, String isRecovery) throws SQLException {
    String resubmissionType = getClaimResubmissionType(insuranceClaimsList,
        (String) actBean.get("claim_id"));
    if (resubmissionType != null
        && (resubmissionType.equals("correction") || resubmissionType.equals("legacy"))) {
      // overwrite previous sponsor remittance if claim's last resubmission type was
      // correction.
      // based on generic pref.
      // In case of recovery option selected, irrespective of resubmission type and generic
      // preference
      // we are always adding to the existing received amount.
      GenericPreferencesDTO genPrefs = GenericPreferencesDAO.getGenericPreferences();
      String aggregateAmtOnRemittance = genPrefs.getAggregate_amt_on_remittance();
      if ((aggregateAmtOnRemittance != null && aggregateAmtOnRemittance.equalsIgnoreCase("Y"))
          || isRecovery.equals("Y")) {
        amount = amount.add(chargeClaimRecdAmt);
      }
    } else if ((resubmissionType != null 
        && (resubmissionType.equals("internal complaint")
          || resubmissionType.equalsIgnoreCase("reconciliation")))
        || isRecovery.equals("Y")) {
      amount = amount.add(chargeClaimRecdAmt);
    } else {
      amount = amount.add(existingAllocatedAmt);
    }

    return amount;
  }

  /**
   * Check existance of alloc beans.
   *
   * @param existAllocBeans
   *          the exist alloc beans
   * @param existingAllocatedAmt
   *          the existing allocated amt
   * @param map
   *          the map
   * @param path
   *          the path
   * @param billNo
   *          the bill no
   * @throws SQLException
   *           the SQL exception
   */
  private void checkExistanceOfAllocBeans(List<BasicDynaBean> existAllocBeans,
      BigDecimal existingAllocatedAmt, Map map, String path, String billNo) throws SQLException {

    StringBuilder allErrors = new StringBuilder();

    if (existAllocBeans != null && existAllocBeans.size() > 0) {
      for (BasicDynaBean abean : existAllocBeans) {

        if (abean.get("remittance_id") == null || (Integer) abean.get("remittance_id") == 0) {
          // +chargeDesc
          allErrors
              .append("Cannot allocate payment amount." + " </br> Bill level payment allocated for "
                  + " </br> Please check the bill remittance for payments. "
                  + submitdao.urlString(path, "bill-remittance", billNo, billNo));
          map.put("error", allErrors.toString());
          return;
        }

        existingAllocatedAmt = existingAllocatedAmt.add((BigDecimal) abean.get("amount"));
      }
    }

  }

  /**
   * Sets the pharmacy activities frm payment list.
   *
   * @param con
   *          the con
   * @param actBean
   *          the act bean
   * @param map
   *          the map
   * @param bean
   *          the bean
   * @param insuranceClaimsList
   *          the insurance claims list
   * @param parmacyItemBeansList
   *          the parmacy item beans list
   * @param pharmacyCharges
   *          the pharmacy charges
   * @param splDenialcodesMap
   *          the spl denialcodes map
   * @throws SQLException
   *           the SQL exception
   */
  public void setPharmacyActivitiesFrmPaymentList(Connection con, BasicDynaBean actBean, Map map,
      BasicDynaBean bean, ArrayList<BasicDynaBean> insuranceClaimsList,
      ArrayList<BasicDynaBean> parmacyItemBeansList, HashMap<String, BasicDynaBean> pharmacyCharges,
      Map<String, String> splDenialcodesMap) throws SQLException {

    Integer saleItemId = (Integer) actBean.get("sale_item_id");

    String path = RequestContext.getHttpRequest().getContextPath();
    final String isRecovery = (String) bean.get("is_recovery");

    String chargeId = actBean.get("charge_id") == null ? "" : (String) actBean.get("charge_id");

    BigDecimal existingAllocatedAmt = BigDecimal.ZERO;
    final String claimID = actBean.get("claim_id") == null ? "" : (String) actBean.get("claim_id");

    BasicDynaBean saleItemPaymentBean = getSaleClaimItem(con, saleItemId,
        (String) actBean.get("claim_id"));

    checkSaleItemExists(saleItemPaymentBean, map, saleItemId);

    if (errorExists(map)) {
      return;
    }

    BasicDynaBean billChrgPaymentBean = getCharge(con, chargeId);
    String billNo = (String) billChrgPaymentBean.get("bill_no");
    // getMedicineDescription(billChrgPaymentBean,
    // saleItemPaymentBean);
    String medicineDesc = "";
    BigDecimal chargeClaimRecdAmt = saleItemPaymentBean.get("claim_recd") == null ? BigDecimal.ZERO
        : (BigDecimal) saleItemPaymentBean.get("claim_recd");

    List<BasicDynaBean> existAllocBeans = findAllocations((String) actBean.get("claim_id"),
        chargeId, saleItemId);

    checkExistanceOfAllocBeans(existAllocBeans, existingAllocatedAmt, map, path, billNo);

    if (errorExists(map)) {
      return;
    }

    BasicDynaBean billbean = billdao.findByKey("bill_no", billNo);
    BigDecimal amount = actBean.get("amount") == null ? BigDecimal.ZERO
        : (BigDecimal) actBean.get("amount");
    amount = getclaimReceivedAmount(insuranceClaimsList, actBean, amount, chargeClaimRecdAmt,
        existingAllocatedAmt, isRecovery);
    String paymentReference = actBean.get("payment_reference") == null ? ""
        : (String) actBean.get("payment_reference");
    checkDuplicatePaymentReference(actBean, chargeId, paymentReference, isRecovery, map, path);

    if (errorExists(map)) {
      return;
    }

    BigDecimal insuClaimAmt = saleItemPaymentBean.get("insurance_claim_amt") != null
        ? (BigDecimal) (saleItemPaymentBean.get("insurance_claim_amt"))
        : BigDecimal.ZERO;

    BigDecimal retInsuClaimAmt = saleItemPaymentBean.get("return_insurance_claim_amt") != null
        ? (BigDecimal) (saleItemPaymentBean.get("return_insurance_claim_amt"))
        : BigDecimal.ZERO;

    BigDecimal claimRecdTotal = saleItemPaymentBean.get("claim_recd") != null
        ? (BigDecimal) (saleItemPaymentBean.get("claim_recd"))
        : BigDecimal.ZERO;

    // checkExcessPaymentForSaleItem(amount, insuClaimAmt, retInsuClaimAmt, claimRecdTotal,
    // existAllocBeans, medicineDesc,
    // map, path, bill_no);

    if (errorExists(map)) {
      return;
    }
    String denialRemarks = actBean.get("denial_remarks") == null ? ""
        : (String) actBean.get("denial_remarks");
    String denialCode = actBean.get("denial_code") == null ? ""
        : (String) actBean.get("denial_code");
    setSaleItemPaymentBean(denialCode, insuClaimAmt, retInsuClaimAmt, amount, saleItemPaymentBean,
        denialRemarks, splDenialcodesMap);

    parmacyItemBeansList.add(saleItemPaymentBean);

    // Fetch item related charges to be updated.
    if (!pharmacyCharges.containsKey(chargeId + ":" + claimID)) {
      pharmacyCharges.put(chargeId + ":" + claimID, saleItemPaymentBean);
    }

    return;

  }

  /**
   * Sets the sale item payment bean.
   *
   * @param denialCode
   *          the denial code
   * @param insuClaimAmt
   *          the insu claim amt
   * @param retInsuClaimAmt
   *          the ret insu claim amt
   * @param amount
   *          the amount
   * @param saleItemPaymentBean
   *          the sale item payment bean
   * @param denialRemarks
   *          the denial remarks
   * @param splDenialcodesMap
   *          the spl denialcodes map
   */
  private void setSaleItemPaymentBean(String denialCode, BigDecimal insuClaimAmt,
      BigDecimal retInsuClaimAmt, BigDecimal amount, BasicDynaBean saleItemPaymentBean,
      String denialRemarks, Map<String, String> splDenialcodesMap) {
    saleItemPaymentBean.set("claim_recd", amount);
    if ((denialCode == null || denialCode.equals(""))
        && ((insuClaimAmt.add(retInsuClaimAmt)).compareTo(amount) <= 0)) {
      saleItemPaymentBean.set("claim_status", "C");
    } else {
      if (!(splDenialcodesMap.containsKey(denialCode))
          || !((String) saleItemPaymentBean.get("claim_status")).equals("C")) {
        saleItemPaymentBean.set("denial_code", denialCode);
        saleItemPaymentBean.set("denial_remarks", denialRemarks);
        saleItemPaymentBean.set("claim_status", "D");
      }
    }
  }

  /**
   * Check excess payment for sale item.
   *
   * @param amount
   *          the amount
   * @param insuClaimAmt
   *          the insu claim amt
   * @param retInsuClaimAmt
   *          the ret insu claim amt
   * @param claimRecdTotal
   *          the claim recd total
   * @param existAllocBeans
   *          the exist alloc beans
   * @param medicineDesc
   *          the medicine desc
   * @param map
   *          the map
   * @param path
   *          the path
   * @param billNo
   *          the bill no
   * @throws SQLException
   *           the SQL exception
   */
  private void checkExcessPaymentForSaleItem(BigDecimal amount, BigDecimal insuClaimAmt,
      BigDecimal retInsuClaimAmt, BigDecimal claimRecdTotal, List<BasicDynaBean> existAllocBeans,
      String medicineDesc, Map map, String path, String billNo) throws SQLException {

    StringBuilder allErrors = new StringBuilder();
    if (amount.compareTo(insuClaimAmt.add(retInsuClaimAmt)) > 0) {

      if (existAllocBeans != null && existAllocBeans.size() > 0) {

        allErrors.append("Excess payment amount " + " </br> " + medicineDesc
            + " has claim received total amount <b> " + claimRecdTotal + " </b>"
            + " </br> Please check the bill remittance for payments. "
            + submitdao.urlString(path, "bill-remittance", billNo, billNo));
        map.put("error", allErrors.toString());
        return;
      }
    }

  }

  /**
   * Check sale item exists.
   *
   * @param saleItemPaymentBean
   *          the sale item payment bean
   * @param map
   *          the map
   * @param saleItemId
   *          the sale item id
   */
  private void checkSaleItemExists(BasicDynaBean saleItemPaymentBean, Map map, Integer saleItemId) {
    StringBuilder allErrors = new StringBuilder();
    if (saleItemPaymentBean == null) {
      allErrors.append("No pharmacy sale bill found with sale Id :" + saleItemId);
      map.put("error", allErrors.toString());
      return;
    }

  }

  /**
   * Gets the charge bean list forpharmacy items.
   *
   * @param con
   *          the con
   * @param pharmacyCharges
   *          the pharmacy charges
   * @param activityBills
   *          the activity bills
   * @param activityClaims
   *          the activity claims
   * @param chargeBeansList
   *          the charge beans list
   * @throws SQLException
   *           the SQL exception
   */
  public void getChargeBeanListForpharmacyItems(Connection con,
      HashMap<String, BasicDynaBean> pharmacyCharges, ArrayList<String> activityBills,
      ArrayList<String> activityClaims, ArrayList<BasicDynaBean> chargeBeansList)
      throws SQLException {

    for (Map.Entry<String, BasicDynaBean> pharmacyCharge : pharmacyCharges.entrySet()) {

      String entryKey = pharmacyCharge.getKey();
      if (null == entryKey || entryKey.trim().equals("")) {
        // we dont expect this nor can handle this
        continue;
      }
      String[] keyParts = entryKey.split(":");
      if (keyParts == null || keyParts.length <= 0) {
        // we dont expect this nor can handle this
        continue;
      }

      String phCharge = keyParts[0]; // part 1 has charge_id
      BasicDynaBean saleClaimBean = pharmacyCharge.getValue();
      if (null == saleClaimBean) {
        // we dont expect this nor can handle this
        continue;
      }

      String claimID = (String) saleClaimBean.get("claim_id");

      BasicDynaBean sale = findSaleCharge(con, phCharge);
      String saleId = (String) sale.get("sale_id");

      List<BasicDynaBean> items = findItemClaimsBySaleId(con, saleId, claimID);

      String denialCode = null;
      for (BasicDynaBean sb : items) {
        if (((String) sb.get("claim_status")).equals("D")) {
          denialCode = (String) sb.get("denial_code");
          break;
        }
      }

      String denialRemarks = null;
      for (BasicDynaBean sb : items) {
        if (((String) sb.get("claim_status")).equals("D")) {
          denialRemarks = (String) sb.get("denial_remarks");
          break;
        }
      }

      BigDecimal totalPhInsAmt = BigDecimal.ZERO;
      boolean phClaimClosed = true;
      for (BasicDynaBean sb : items) {
        totalPhInsAmt = totalPhInsAmt.add((BigDecimal) sb.get("claim_recd"));
        phClaimClosed = phClaimClosed && "C".equals(sb.get("claim_status"));
      }

      BasicDynaBean phchargebean = getChargeClaim(con, phCharge, claimID);
      BigDecimal insuClaimAmt = phchargebean.get("insurance_claim_amt") != null
          ? (BigDecimal) (phchargebean.get("insurance_claim_amt"))
          : BigDecimal.ZERO;

      /*
       * Getting a return insurance claim amount from bill charge table. It will not work in case of
       * multi payer. Need to fix it.
       */

      BasicDynaBean chgBean = charge.findByKey(con, "charge_id", phCharge);
      BigDecimal retInsuClaimAmt = chgBean.get("return_insurance_claim_amt") != null
          ? (BigDecimal) (chgBean.get("return_insurance_claim_amt"))
          : BigDecimal.ZERO;

      if ((denialCode == null || denialCode.equals("")) && (phClaimClosed)) {
        phchargebean.set("claim_recd_total", totalPhInsAmt);
        phchargebean.set("claim_status", "C");
      } else {
        phchargebean.set("claim_recd_total", totalPhInsAmt);
        phchargebean.set("denial_code", denialCode);
        phchargebean.set("claim_status", "D");
        phchargebean.set("denial_remarks", denialRemarks);
      }

      // Handle zero claim bills for pharmacy returns
      List<BasicDynaBean> salesReturns = getSalesReturnChargesByClaim(con, claimID);
      for (BasicDynaBean salesReturn : salesReturns) {
        BigDecimal claimAmt = (BigDecimal) salesReturn.get("insurance_claim_amt");
        if (null != claimAmt && BigDecimal.ZERO.compareTo(claimAmt) == 0) {
          updateBillChargeClaims(salesReturn, denialCode, denialRemarks, BigDecimal.ZERO);
          chargeBeansList.add(salesReturn);
        }
      }

      // Fetch charge related bills to be updated.
      if (!activityBills.contains((String) phchargebean.get("bill_no"))) {
        activityBills.add((String) phchargebean.get("bill_no"));
      }
      if (!activityClaims.contains((String) phchargebean.get("claim_id"))) {
        activityClaims.add((String) phchargebean.get("claim_id"));
      }

      chargeBeansList.add(phchargebean);
    }

  }

  /**
   * Update all payer ref ids for claims.
   *
   * @param con
   *          the con
   * @param insuranceClaimsList
   *          the insurance claims list
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public void updateAllPayerRefIdsForClaims(Connection con,
      ArrayList<BasicDynaBean> insuranceClaimsList) throws SQLException, IOException {
    for (BasicDynaBean cl : insuranceClaimsList) {
      claimdao.updateWithName(con, cl.getMap(), "claim_id");
    }
  }

  /**
   * Update all pharmacy sale items.
   *
   * @param con
   *          the con
   * @param parmacyItemBeansList
   *          the parmacy item beans list
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public void updateAllPharmacySaleItems(Connection con,
      ArrayList<BasicDynaBean> parmacyItemBeansList) throws SQLException, IOException {
    HashMap scKeyMap = new HashMap();
    // Update all pharmacy sale items.
    for (BasicDynaBean pb : parmacyItemBeansList) {
      // item.updateWithName(con, pb.getMap(), "sale_item_id");
      scKeyMap.put("sale_item_id", pb.get("sale_item_id"));
      scKeyMap.put("claim_id", pb.get("claim_id"));
      salesClaimDetailsDAO.update(con, pb.getMap(), scKeyMap);
    }
  }

  /**
   * Gets the claim resubmission type.
   *
   * @param insuranceClaimsList
   *          the insurance claims list
   * @param claimId
   *          the claim id
   * @return the claim resubmission type
   */
  private String getClaimResubmissionType(ArrayList<BasicDynaBean> insuranceClaimsList,
      String claimId) {
    for (BasicDynaBean insclaim : insuranceClaimsList) {
      if (insclaim.get("claim_id").equals(claimId)) {
        return insclaim.get("resubmission_type") != null
            ? (String) insclaim.get("resubmission_type")
            : null;
      }
    }
    return null;
  }

  /**
   * Gets the medicine description.
   *
   * @param billChrgPaymentBean
   *          the bill chrg payment bean
   * @param saleItemPaymentBean
   *          the sale item payment bean
   * @return the medicine description
   * @throws SQLException
   *           the SQL exception
   */
  private String getMedicineDescription(BasicDynaBean billChrgPaymentBean,
      BasicDynaBean saleItemPaymentBean) throws SQLException {
    Integer saleItemId = (Integer) saleItemPaymentBean.get("sale_item_id");
    Integer medicineId = (Integer) saleItemPaymentBean.get("medicine_id");
    BasicDynaBean phMedBean = getMedicine(medicineId);
    String medicineName = (String) phMedBean.get("medicine_name");
    StringBuilder sb = new StringBuilder();
    sb.append(getChargeDescription(billChrgPaymentBean));
    sb.append(" and Sale Item Id : " + saleItemId + ": ");
    sb.append("( " + medicineName + ")");
    return sb.toString();
  }

  /**
   * Gets the charge description.
   *
   * @param billChrgPaymentBean
   *          the bill chrg payment bean
   * @return the charge description
   */
  private String getChargeDescription(BasicDynaBean billChrgPaymentBean) {
    StringBuilder sb = new StringBuilder();
    String chargeId = (String) billChrgPaymentBean.get("charge_id");
    String actDescription = (String) billChrgPaymentBean.get("act_description");
    String postedDate = DateUtil
        .formatTimestamp(new Date(((Timestamp) billChrgPaymentBean.get("posted_date")).getTime()));
    sb.append("Activity Charge Id : " + chargeId + ": ");
    sb.append("( " + actDescription);
    sb.append(" Dated : " + postedDate + " )");
    return sb.toString();
  }

  /**
   * Update all bills status.
   *
   * @param con
   *          the con
   * @param billsToUpdtList
   *          the bills to updt list
   * @param userid
   *          the userid
   * @return the string
   * @throws Exception
   *           the exception
   */
  public String updateAllBillsStatus(Connection con, List<String> billsToUpdtList, String userid)
      throws Exception {
    String path = RequestContext.getHttpRequest().getContextPath();
    StringBuilder allErrors = new StringBuilder();
    StringBuilder sbDiscountProblems = new StringBuilder(
        " bills are not closed. Please check the discount amounts and close the bills. <br/>");
    StringBuilder sbPaymentProblems = new StringBuilder(" bills are not closed as the "
        + "bill amount is not equal to patient payment total amount for the bill. <br/>");
    StringBuilder sbClaimProblems = new StringBuilder(" bills are not closed as the "
        + " bill claim amount is not equal to sponsor payment total amount for bill. <br/>");

    Set<String> discountProblemBills = new HashSet<String>();
    Set<String> patientPayProblemBills = new HashSet<String>();
    Set<String> sponsorPayProblemBills = new HashSet<String>();

    String dischargeStatus = "N";
    java.sql.Timestamp closedDate = com.bob.hms.common.DateUtil.getCurrentTimestamp();
    String closedBy = userid;
    boolean success = false;
    String billsStr = "";

    update: {
      BillDAO billDAO = new BillDAO(con);
      BillBO billBO = new BillBO();

      /*
       * Bills which don't have any denied charges are closed; Else we mark their claim status as
       * recieved and leave them in finalized status. In both the cases, payment amounts are checked
       * before changing the bill status.
       */
      if (billsToUpdtList == null || billsToUpdtList.size() == 0) {
        return null;
      }
      for (String billNo : billsToUpdtList) {
        boolean billTotalsOK = true;
        String error = null;
        String clerror = null;
        StringBuilder sb = new StringBuilder();
        StringBuilder sbc = new StringBuilder();

        final Boolean markBillClaimAsClosed = updateChargeAndMarkBillAsClosed(con, billNo);
        BigDecimal claimRecdTotal = getBillClaimRecdTotal(con, billNo);
        boolean hasDRGCode = false;
        Map drgCodeMap = ChargeDAO.getBillDRGCode(billNo);

        if (drgCodeMap != null && drgCodeMap.get("drg_charge_id") != null) {
          hasDRGCode = true;
        }

        // Original bill status
        Bill bill = billBO.getBill(billNo);
        String status = bill.getStatus();
        String finalizedBy = bill.getFinalizedBy();

        String paymentStatus = bill.getPaymentStatus();
        String claimStatus = bill.getPrimaryClaimStatus();
        String secClaimStatus = bill.getSecondaryClaimStatus();
        bill.setClaimRecdAmount(claimRecdTotal);

        // If bill is closed then bill status updation is skipped.
        if (status.equals("C") && claimStatus.equals("R") && "R".equals(secClaimStatus)) {
          continue;
        }

        String billRemarks = "";

        // Change bill status and bill claim status if all bill charges have claim amount
        // received (or) not denied.
        if (markBillClaimAsClosed) {
          bill.setPrimaryClaimStatus("R");
          billRemarks = "Claim amount received. Closing the bill.";
          status = Bill.BILL_STATUS_CLOSED;
          paymentStatus = Bill.BILL_PAYMENT_PAID;
        }

        billDAO.updateBill(bill);
        boolean isTpa = bill.getIs_tpa();
        BasicDynaBean billAmtBean = billDAO.getBillAmounts(bill.getBillNo());

        if (billAmtBean != null) {

          if (status.equals(Bill.BILL_STATUS_CLOSED)) {

            // Additional check since return_insurance_claim_amt is made zero.
            // Change done for Pharmacy returns due to bug # 31033 fix.

            if (!(isPatientTotalOK(billAmtBean) || bill.getPatientWriteOff().equals("A"))) {
              // &&
              // !BillDAO.isClaimAllBillsTotalsOK(con,
              // b.getClaim_id()))
              // {
              // further check for a pharmacy bill
              if ((!BillDAO.isAllReturnBillsTotalsOK(con, billNo))) {
                // check all dues from
                // all claims against the
                // reference bill
                sb.append("Payment Problem");
                billRemarks = 
                    "Patient payment amount not equal to total amount, bill is not closed.";
                billTotalsOK = false;
              }
            }

            if (!isClaimTotalsOK(billAmtBean, isTpa, hasDRGCode)) {
              sbc.append("Claim Problem");
              billRemarks = "Claim received amount not equal to total claim, bill is not closed.";
              billTotalsOK = false;
            }
          }

          if (!billTotalsOK) {
            billRemarks = "Claim amount received." + billRemarks;
          }
          bill.setBillRemarks(billRemarks);
          billDAO.updateBill(bill);

          error = sb.toString();
          if (error.equals("")) {
            error = null;
          } else if (error.startsWith("Payment Problem")) {
            patientPayProblemBills.add(billNo);
          } else {
            discountProblemBills.add(billNo);
          }

          clerror = sbc.toString();
          if (clerror.equals("")) {
            clerror = null;
          } else {
            sponsorPayProblemBills.add(billNo);
          }

          // If no patient payment and claim payment errors update bill status
          if (error == null && clerror == null && billTotalsOK) {
            billDAO.updateBillStatus(userid, billNo, status, paymentStatus, dischargeStatus,
                new Timestamp(bill.getFinalizedDate().getTime()), closedDate, closedBy,
                finalizedBy, new Timestamp(bill.getLastFinalizedAt().getTime()));
          }
        }
      }
      success = true;

      if (discountProblemBills.size() > 0) {
        billsStr = "";
        for (String billNo : discountProblemBills) {
          billsStr = submitdao.urlString(path, "bill", billNo, null) + ", " + billsStr;
        }
        allErrors.append(discountProblemBills.size() + sbDiscountProblems.toString()
            + " Please check bills : " + billsStr);
      }

      if (patientPayProblemBills.size() > 0) {
        billsStr = "";
        for (String billNo : patientPayProblemBills) {
          billsStr = submitdao.urlString(path, "bill", billNo, null) + ", " + billsStr;
        }
        allErrors.append(patientPayProblemBills.size() + sbPaymentProblems.toString()
            + " Please check bills : " + billsStr);
      }

      if (sponsorPayProblemBills.size() > 0) {
        billsStr = "";
        for (String billNo : sponsorPayProblemBills) {
          billsStr = submitdao.urlString(path, "bill", billNo, null) + ", " + billsStr;
        }
        allErrors.append(sponsorPayProblemBills.size() + sbClaimProblems.toString()
            + " Please check bills : " + billsStr);
      }
    }

    if (success && allErrors.toString().equals("")) {
      return null;
    } else {
      return allErrors.toString();
    }
  }

  /**
   * Update charge and mark bill as closed.
   *
   * @param con
   *          the con
   * @param billNo
   *          the bill no
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  private boolean updateChargeAndMarkBillAsClosed(Connection con, String billNo)
      throws SQLException, IOException {
    ArrayList<BasicDynaBean> chrgBeanForBill = (ArrayList<BasicDynaBean>) getAllCharges(con,
        billNo);
    Boolean markBillAsClosed = true;

    if (chrgBeanForBill != null && chrgBeanForBill.size() > 0) {
      for (BasicDynaBean chrg : chrgBeanForBill) {
        BigDecimal insuClaimAmt = chrg.get("insurance_claim_amount") != null
            ? (BigDecimal) (chrg.get("insurance_claim_amount"))
            : BigDecimal.ZERO;

        if (((String) chrg.get("charge_head")).equals("MARDRG")) {
          BasicDynaBean billbean = billdao.findByKey("bill_no", (String) chrg.get("bill_no"));
          BigDecimal insuranceDeduction = (BigDecimal) billbean.get("insurance_deduction");
          insuClaimAmt = insuClaimAmt.subtract(insuranceDeduction);
        }
        BigDecimal insuRetClaimAmt = chrg.get("return_insurance_claim_amt") != null
            ? (BigDecimal) (chrg.get("return_insurance_claim_amt"))
            : BigDecimal.ZERO;

        BigDecimal insClaimRecdAmount = chrg.get("claim_recd_total") != null
            ? (BigDecimal) chrg.get("claim_recd_total")
            : BigDecimal.ZERO;
        if (!chrg.get("status").equals("X")) {
          // Bill is not closed when item claim status is denied or the claim amount is not
          // completely reccived.
          if ((chrg.get("claim_status") != null && chrg.get("claim_status").equals("D"))
              || (insuClaimAmt.add(insuRetClaimAmt).compareTo(BigDecimal.ZERO) > 0
                  && (insuClaimAmt.add(insuRetClaimAmt)).compareTo(insClaimRecdAmount) != 0)) {
            markBillAsClosed = false;

            // Mark item claim status as closed when it has zero claim amount.
          } else if ((insuClaimAmt.add(insuRetClaimAmt)).compareTo(BigDecimal.ZERO) <= 0) {
            chrg.set("claim_status", "C");
          }
        }
      }

      // Update all charges claim status as Closed which have zero claim amount.
      for (BasicDynaBean chargeBeanforBill : chrgBeanForBill) {
        charge.updateWithName(con, chargeBeanforBill.getMap(), "charge_id");
      }

      for (BasicDynaBean chargeBeanForBill : chrgBeanForBill) {
        String chargeId = (String) chargeBeanForBill.get("charge_id");
        List<BasicDynaBean> billChgClaimList = new BillChargeClaimDAO().findAllByKey(con,
            "charge_id", chargeId);
        for (BasicDynaBean bcl : billChgClaimList) {
          BigDecimal insuClaimAmt = bcl.get("insurance_claim_amt") != null
              ? (BigDecimal) (bcl.get("insurance_claim_amt"))
              : BigDecimal.ZERO;
          if (insuClaimAmt.compareTo(BigDecimal.ZERO) <= 0) {
            bcl.set("claim_status", "C");
            Map<String, Object> keys = new HashMap<String, Object>();
            keys.put("claim_id", (String) bcl.get("claim_id"));
            keys.put("charge_id", chargeId);
            new BillChargeClaimDAO().update(con, bcl.getMap(), keys);
          }
        }
      }
    }
    return markBillAsClosed;
  }

  /**
   * Checks if is patient total OK.
   *
   * @param billAmtBean
   *          the bill amt bean
   * @return true, if is patient total OK
   * @throws SQLException
   *           the SQL exception
   */
  public boolean isPatientTotalOK(BasicDynaBean billAmtBean) throws SQLException {

    BigDecimal billAmt = (BigDecimal) billAmtBean.get("total_amount");
    BigDecimal claimReturnAmt = (BigDecimal) billAmtBean.get("total_claim_return");
    BigDecimal insAmt = ((BigDecimal) billAmtBean.get("total_claim"));
    BigDecimal priClaimAmt = ((BigDecimal) billAmtBean.get("primary_total_claim"));
    BigDecimal secClaimAmt = ((BigDecimal) billAmtBean.get("secondary_total_claim"));
    BigDecimal dedn = (BigDecimal) billAmtBean.get("insurance_deduction");

    BigDecimal totalReceipts = (BigDecimal) billAmtBean.get("total_receipts");
    BigDecimal depositSetOff = (BigDecimal) billAmtBean.get("deposit_set_off");
    BigDecimal pointsRedeemedAmt = (BigDecimal) billAmtBean.get("points_redeemed_amt");

    BigDecimal patientAmt = billAmt.subtract(priClaimAmt).subtract(secClaimAmt);
    BigDecimal patientCredits = totalReceipts.add(depositSetOff).add(pointsRedeemedAmt);

    BasicDynaBean creditNoteBean = new BillDAO()
        .getCreditNoteDetails((String) billAmtBean.get("bill_no"));

    BigDecimal patientCreditNoteAmt = BigDecimal.ZERO;

    if (null != creditNoteBean) {
      patientCreditNoteAmt = (BigDecimal) creditNoteBean.get("total_pat_amt");
    }

    patientCredits = patientCredits.subtract(patientCreditNoteAmt);

    // Payment status check
    if (patientAmt.compareTo(patientCredits) != 0) {
      return false;
    }
    return true;
  }

  /**
   * Checks if is claim totals OK.
   *
   * @param billAmtBean
   *          the bill amt bean
   * @param isTpa
   *          the is tpa
   * @param hasDRG
   *          the has DRG
   * @return true, if is claim totals OK
   */
  public boolean isClaimTotalsOK(BasicDynaBean billAmtBean, boolean isTpa, boolean hasDRG) {

    BigDecimal totalSponsorReceipts = billAmtBean.get("primary_total_sponsor_receipts") == null
        ? BigDecimal.ZERO
        : (BigDecimal) billAmtBean.get("primary_total_sponsor_receipts");
    BigDecimal totalClaimRecd = billAmtBean.get("claim_recd_amount") == null ? BigDecimal.ZERO
        : (BigDecimal) billAmtBean.get("claim_recd_amount");
    // BigDecimal totalClaimReturns = billAmtBean.get("total_claim_return") == null ?
    // BigDecimal.ZERO : (BigDecimal) billAmtBean.get("total_claim_return");
    BigDecimal totalClaimReturns = BigDecimal.ZERO;

    BigDecimal dedn = (BigDecimal) billAmtBean.get("insurance_deduction");
    BigDecimal insAmt = ((BigDecimal) billAmtBean.get("total_claim"));

    // For remittance, deduction is not considered.
    // Hence, bill is not closed if deduction exists but can be force closed from billing.
    // But for DRG bills we need to consider deduction.

    if (hasDRG) {
      insAmt = insAmt.subtract(dedn);
    }

    // Claim status check
    if (isTpa) {

      // insAmt = insAmt.add(totalClaimReturns);

      if ((insAmt.add(totalClaimReturns).compareTo(BigDecimal.ZERO) > 0)
          && (insAmt.compareTo(totalSponsorReceipts) <= 0 && insAmt.compareTo(totalClaimRecd) <= 0)
          && (insAmt.add(totalClaimReturns).compareTo(totalSponsorReceipts) <= 0
              && insAmt.add(totalClaimReturns).compareTo(totalClaimRecd) <= 0)) {
        return false;
      }

      /*
       * if ((insAmt.add(totalClaimReturns).compareTo(BigDecimal.ZERO) > 0) &&
       * (insAmt.add(totalClaimReturns).compareTo(totalClaimRecd.add(totalSponsorReceipts)) < 0)) {
       * return false; }
       */
    }
    return true;
  }

  /**
   * Update all claims status.
   *
   * @param con
   *          the con
   * @param claims
   *          the claims
   * @param userid
   *          the userid
   * @return the string
   * @throws Exception
   *           the exception
   */
  public String updateAllClaimsStatus(Connection con, List<String> claims, String userid)
      throws Exception {
    /*
     * Finally, for all claims received in the remittance, if all bills are closed for the claim -
     * we mark the claim as closed else we mark the claim as denied, to indicate that it requires
     * further reconciliation processing.
     */
    BillBO billBO = new BillBO();
    BillDAO billDAO = new BillDAO(con);
    GenericDAO sale = new GenericDAO("store_sales_main");
    if (claims == null || claims.size() == 0) {
      return null;
    }

    BasicDynaBean prefbean = GenericPreferencesDAO.getAllPrefs();
    BigDecimal amountDiff = (prefbean != null
        && prefbean.get("auto_close_claims_with_difference") != null)
            ? (BigDecimal) prefbean.get("auto_close_claims_with_difference")
            : BigDecimal.ZERO;

    String dischargeStatus = "N";
    java.sql.Timestamp closedDate = com.bob.hms.common.DateUtil.getCurrentTimestamp();
    String closedBy = userid;

    for (String claimId : claims) {
      BasicDynaBean claimbean = new ClaimDAO().findClaimById(claimId);
      List<BasicDynaBean> claimBillList = getClaimBills(con, claimId);
      Boolean isClaimClosed = true;
      Boolean isDenialAccepted = false;

      if (claimBillList != null && claimBillList.size() > 0) {

        BigDecimal claimAllBillsRecdTotal = BigDecimal.ZERO;
        BigDecimal claimAllBillsTotal = BigDecimal.ZERO;
        for (BasicDynaBean cbill : claimBillList) {
          claimAllBillsRecdTotal = claimAllBillsRecdTotal
              .add(getClaimRecdTotal(con, (String) cbill.get("bill_no"), claimId));
          claimAllBillsTotal = claimAllBillsTotal
              .add(claimdao.getBillClaimTotal(con, (String) cbill.get("bill_no"), claimId));
        }

        if (claimAllBillsTotal.compareTo(claimAllBillsRecdTotal) != 0) {
          if (amountDiff.compareTo(BigDecimal.ZERO) != 0) {
            BigDecimal claimDiff = claimAllBillsTotal.subtract(claimAllBillsRecdTotal);
            isDenialAccepted = claimDiff.abs().compareTo(amountDiff) <= 0;
          }
        }

        if (!isDenialAccepted) {
          // No denial
          for (BasicDynaBean cbill : claimBillList) {
            if (cbill.get("restriction_type") != null
                && ((String) cbill.get("restriction_type")).equals("P")) {
              BasicDynaBean salebill = sale.findByKey("bill_no", (String) cbill.get("bill_no"));
              // If Pharmacy return bill claim can be closed
              if (salebill == null || !((String) salebill.get("type")).equals("R")) {
                if (!cbill.get("claim_status").equals("C")) {
                  isClaimClosed = false;
                  break;
                }
              }
            } else if (!cbill.get("claim_status").equals("C")) {
              isClaimClosed = false;
              break;
            }
          }

          boolean allBillChgClaimClosed = true;
          for (BasicDynaBean cbill : claimBillList) {
            Map<String, Object> keys = new HashMap<String, Object>();
            keys.put("claim_id", claimId);
            keys.put("bill_no", (String) cbill.get("bill_no"));
            List<BasicDynaBean> billChgClaimList = new BillChargeClaimDAO().listAll(con, null, keys,
                null);

            if (null != billChgClaimList && billChgClaimList.size() > 0) {

              for (BasicDynaBean billChgbean : billChgClaimList) {
                String chgClmStatus = (String) billChgbean.get("claim_status");
                allBillChgClaimClosed = allBillChgClaimClosed && "C".equals(chgClmStatus);
              }
            }
          }

          isClaimClosed = allBillChgClaimClosed;

        } else {
          // Denial accepted, mark the bills as closed.

          for (BasicDynaBean cbill : claimBillList) {
            BigDecimal sponsorDue = BillDAO.getSponsorDue((String) cbill.get("bill_no"));
            if (allChargesClosed(con, (String) cbill.get("bill_no"), claimId)
                || (sponsorDue.compareTo(amountDiff) <= 0)) {
              BillClaimDAO bcdao = new BillClaimDAO();
              bcdao.closeBillClaim(con, (String) cbill.get("bill_no"), claimId);
            }
          }

          for (BasicDynaBean cbill : claimBillList) {

            String billNo = (String) cbill.get("bill_no");
            final Boolean markBillClaimAsClosed = updateChargeAndMarkBillAsClosed(con, billNo);

            // Original bill status
            Bill bill = billBO.getBill(billNo);
            String status = bill.getStatus();
            String finalizedBy = bill.getFinalizedBy();

            String pclaimStatus = bill.getPrimaryClaimStatus();
            String sclaimStatus = bill.getSecondaryClaimStatus();

            // If bill is closed then bill status updation is skipped.
            if (status.equals("C") && pclaimStatus.equals("R") && sclaimStatus.equals("R")) {
              continue;
            }

            // Change bill status and bill claim status if all bill charges have claim amount
            // received (or) not denied.
            if (null != cbill.get("priority")) {
              if ((Integer) cbill.get("priority") == 1) {
                bill.setPrimaryClaimStatus("R");
              } else {
                bill.setSecondaryClaimStatus("R");
              }
            }
            status = Bill.BILL_STATUS_CLOSED;
            String paymentStatus = bill.getPaymentStatus();
            paymentStatus = Bill.BILL_PAYMENT_PAID;

            String billRemarks = "";
            String writeOffRemarks = "";

            // Mark bill as closed even though some round off amount is claim due.
            if (markBillClaimAsClosed) {
              billRemarks = "Claim amount received. Closing the bill.";
            } else {
              writeOffRemarks = "Denial accepted with amount difference. Closing the bill.";
              bill.setSpnrWriteOffRemarks(writeOffRemarks);
            }

            billDAO.updateBill(bill);
            BasicDynaBean billAmtBean = billDAO.getBillAmounts(bill.getBillNo());
            boolean billTotalsOK = true;

            if (billAmtBean != null) {

              if (status.equals(Bill.BILL_STATUS_CLOSED)) {

                if (!(isPatientTotalOK(billAmtBean) || bill.getPatientWriteOff().equals("A"))
                    && !BillDAO.isClaimAllBillsTotalsOK(con, claimId)) {
                  billRemarks = 
                      "Claim amount received. Patient payment amount not equal to total amount,"
                      + " bill is not closed.";
                  billTotalsOK = false;
                }
              }

              bill.setBillRemarks(billRemarks);

              billDAO.updateBill(bill);

              // If no patient payment and claim payment errors update bill status
              if (billTotalsOK) {
                Timestamp finalizedDate = null != bill.getFinalizedDate()
                    ? new Timestamp(bill.getFinalizedDate().getTime())
                    : DateUtil.getCurrentTimestamp();
                billDAO.updateBillStatus(
                    userid, billNo, status, paymentStatus, dischargeStatus, finalizedDate, 
                    closedDate, closedBy, finalizedBy, DateUtil.getCurrentTimestamp());
              }
            }
          }
        }

        HashMap insuStatusMap = new HashMap();
        if (isClaimClosed) {
          insuStatusMap.put("status", "C");
          if (isDenialAccepted) {
            insuStatusMap.put("closure_type", "D"); // Denial accepted with amount difference.
            insuStatusMap.put("action_remarks",
                "Denial accepted with amount difference. Closing the claim.");
          } else {
            insuStatusMap.put("closure_type", "F"); // Fully Received
            insuStatusMap.put("action_remarks", "Claim amount received. Closing the claim.");
          }
        } else {
          if (claimbean != null && claimbean.get("status") != null
              && claimbean.get("status").equals("S")) {
            insuStatusMap.put("status", "D");
          }
        }
        new GenericDAO("insurance_claim").update(con, insuStatusMap, "claim_id", claimId);
      }
    }
    return null;
  }

  /** The Constant GET_ALL_CHARGES. */
  private static final String GET_ALL_CHARGES = "SELECT * from bill_charge where bill_no = ?";

  /**
   * Gets the all charges.
   *
   * @param con
   *          the con
   * @param billNo
   *          the bill no
   * @return the all charges
   * @throws SQLException
   *           the SQL exception
   */
  private List<BasicDynaBean> getAllCharges(Connection con, String billNo) throws SQLException {
    try (PreparedStatement ps = con.prepareStatement(GET_ALL_CHARGES);) {
      ps.setString(1, billNo);
      return DataBaseUtil.queryToDynaList(ps);
    }
  }

  /** The Constant GET_CHARGE. */
  private static final String GET_CHARGE = "SELECT * from bill_charge where charge_id = ?";

  /**
   * Gets the charge.
   *
   * @param con
   *          the con
   * @param chargeId
   *          the charge id
   * @return the charge
   * @throws SQLException
   *           the SQL exception
   */
  public BasicDynaBean getCharge(Connection con, String chargeId) throws SQLException {
    try (PreparedStatement ps = con.prepareStatement(GET_CHARGE);) {
      ps.setString(1, chargeId);
      return DataBaseUtil.queryToDynaBean(ps);
    }
  }

  /** The Constant GET_CHARGE_CLAIM. */
  private static final String GET_CHARGE_CLAIM = 
      "SELECT * from bill_charge_claim where charge_id = ? AND claim_id = ?";

  /**
   * Gets the charge claim.
   *
   * @param con
   *          the con
   * @param chargeId
   *          the charge id
   * @param claimId
   *          the claim id
   * @return the charge claim
   * @throws SQLException
   *           the SQL exception
   */
  public BasicDynaBean getChargeClaim(Connection con, String chargeId, String claimId)
      throws SQLException {
    try (PreparedStatement ps = con.prepareStatement(GET_CHARGE_CLAIM);) {
      ps.setString(1, chargeId);
      ps.setString(2, claimId);
      return DataBaseUtil.queryToDynaBean(ps);
    }
  }

  /** The Constant GET_CHARGES_BYCLAIM. */
  private static final String GET_CHARGES_BYCLAIM = 
      "SELECT bcc.* from bill_charge_claim bcc, store_sales_main ssm "
      + " where ssm.charge_id = bcc.charge_id and ssm.type = 'R' and bcc.claim_id = ?";

  /**
   * Gets the sales return charges by claim.
   *
   * @param con
   *          the con
   * @param claimId
   *          the claim id
   * @return the sales return charges by claim
   * @throws SQLException
   *           the SQL exception
   */
  private List<BasicDynaBean> getSalesReturnChargesByClaim(Connection con, String claimId)
      throws SQLException {
    try (PreparedStatement ps = con.prepareStatement(GET_CHARGES_BYCLAIM);) {
      ps.setString(1, claimId);
      return DataBaseUtil.queryToDynaList(ps);
    }
  }

  /** The Constant FIND_CHARGE. */
  private static final String FIND_CHARGE = "SELECT * from store_sales_main where charge_id = ?";

  /**
   * Find sale charge.
   *
   * @param con
   *          the con
   * @param chargeId
   *          the charge id
   * @return the basic dyna bean
   * @throws SQLException
   *           the SQL exception
   */
  public BasicDynaBean findSaleCharge(Connection con, String chargeId) throws SQLException {
    PreparedStatement ps = con.prepareStatement(FIND_CHARGE);
    ps.setString(1, chargeId);
    return DataBaseUtil.queryToDynaBean(ps);
  }

  /** The Constant GET_SALE_ITEM. */
  private static final String GET_SALE_ITEM = 
      "SELECT * from store_sales_details where sale_item_id = ?";

  /**
   * Gets the sale item.
   *
   * @param con
   *          the con
   * @param saleItemId
   *          the sale item id
   * @return the sale item
   * @throws SQLException
   *           the SQL exception
   */
  public BasicDynaBean getSaleItem(Connection con, Integer saleItemId) throws SQLException {
    try (PreparedStatement ps = con.prepareStatement(GET_SALE_ITEM);) {
      ps.setInt(1, saleItemId);
      return DataBaseUtil.queryToDynaBean(ps);
    }
  }

  /** The Constant GET_SALE_CLAIM_ITEM. */
  private static final String GET_SALE_CLAIM_ITEM = 
      "SELECT * from sales_claim_details where sale_item_id = ? AND claim_id = ?";

  /**
   * Gets the sale claim item.
   *
   * @param con
   *          the con
   * @param saleItemId
   *          the sale item id
   * @param claimId
   *          the claim id
   * @return the sale claim item
   * @throws SQLException
   *           the SQL exception
   */
  public BasicDynaBean getSaleClaimItem(Connection con, Integer saleItemId, String claimId)
      throws SQLException {
    try (PreparedStatement ps = con.prepareStatement(GET_SALE_CLAIM_ITEM);) {
      ps.setInt(1, saleItemId);
      ps.setString(2, claimId);
      return DataBaseUtil.queryToDynaBean(ps);
    }
  }

  /** The Constant FIND_SALE_ITEMS. */
  private static final String FIND_SALE_ITEMS = 
      "SELECT * from store_sales_details where sale_id = ?";

  /**
   * Find items by sale id.
   *
   * @param con
   *          the con
   * @param saleId
   *          the sale id
   * @return the list
   * @throws SQLException
   *           the SQL exception
   */
  public List<BasicDynaBean> findItemsBySaleId(Connection con, String saleId) throws SQLException {
    try (PreparedStatement ps = con.prepareStatement(FIND_SALE_ITEMS);) {
      ps.setString(1, saleId);
      return DataBaseUtil.queryToDynaList(ps);
    }
  }

  /** The Constant FIND_SALE_CLAIM_ITEMS. */
  private static final String FIND_SALE_CLAIM_ITEMS = 
      "SELECT scm.* from sales_claim_details scm, store_sales_details ssd "
      + "where scm.sale_item_id = ssd.sale_item_id and ssd.sale_id = ? AND scm.claim_id =?";

  /**
   * Find item claims by sale id.
   *
   * @param con
   *          the con
   * @param saleId
   *          the sale id
   * @param claimId
   *          the claim id
   * @return the list
   * @throws SQLException
   *           the SQL exception
   */
  public List<BasicDynaBean> findItemClaimsBySaleId(Connection con, String saleId, String claimId)
      throws SQLException {
    try (PreparedStatement ps = con.prepareStatement(FIND_SALE_CLAIM_ITEMS);) {
      ps.setString(1, saleId);
      ps.setString(2, claimId);
      return DataBaseUtil.queryToDynaList(ps);
    }
  }

  /** The Constant GET_MEDICINE. */
  private static final String GET_MEDICINE = 
      "SELECT * from store_item_details where medicine_id = ?";

  /**
   * Gets the medicine.
   *
   * @param medicineId
   *          the medicine id
   * @return the medicine
   * @throws SQLException
   *           the SQL exception
   */
  private BasicDynaBean getMedicine(Integer medicineId) throws SQLException {
    return DataBaseUtil.queryToDynaBean(GET_MEDICINE, new Object[] { medicineId });
  }

  /** The Constant GET_REMITTANCE_CHARGES. */
  private static final String GET_REMITTANCE_CHARGES = 
      "SELECT charge_id from insurance_payment_allocation where remittance_id = ?";

  /**
   * Gets the all charge ids.
   *
   * @param con
   *          the con
   * @param remittanceId
   *          the remittance id
   * @return the all charge ids
   * @throws SQLException
   *           the SQL exception
   */
  public List<BasicDynaBean> getAllChargeIds(Connection con, Integer remittanceId)
      throws SQLException {
    return DataBaseUtil.queryToDynaList(con, GET_REMITTANCE_CHARGES, remittanceId);
  }

  /** The Constant GET_ALLOCATED_CHARGES. */
  private static final String GET_ALLOCATED_CHARGES = 
      "SELECT * from insurance_payment_allocation where charge_id = ? AND sale_item_id = ? "
      + " AND claim_id = ?";

  /**
   * Find allocations.
   *
   * @param claimId
   *          the claim id
   * @param chargeId
   *          the charge id
   * @param saleItemId
   *          the sale item id
   * @return the list
   * @throws SQLException
   *           the SQL exception
   */
  public List<BasicDynaBean> findAllocations(String claimId, String chargeId, int saleItemId)
      throws SQLException {
    return DataBaseUtil.queryToDynaList(GET_ALLOCATED_CHARGES,
        new Object[] { chargeId, saleItemId, claimId });
  }

  /** The Constant GET_ALL_BILLS. */
  private static final String GET_ALL_BILLS = "SELECT bill_no FROM bill_charge ";

  /**
   * Gets the all bills for charges.
   *
   * @param con
   *          the con
   * @param charges
   *          the charges
   * @return the all bills for charges
   * @throws SQLException
   *           the SQL exception
   */
  public List<String> getAllBillsForCharges(Connection con, List charges) throws SQLException {

    Set set = new HashSet();
    StringBuilder where = new StringBuilder();
    DataBaseUtil.addWhereFieldInList(where, "charge_id", charges);

    StringBuilder query = new StringBuilder(GET_ALL_BILLS);
    query.append(where);

    logger.debug("{}", query);
    List<String> billsForCharges = new ArrayList<String>();
    try (PreparedStatement ps = con.prepareStatement(query.toString());) {
      int index = 1;
      for (int chargeIndex = 0; chargeIndex < charges.size(); chargeIndex++, index++) {
        ps.setString(index, (String) ((BasicDynaBean) charges.get(chargeIndex)).get("charge_id"));
      }
      try (ResultSet rs = ps.executeQuery();) {
        while (rs.next()) {
          set.add(rs.getString("bill_no"));
        }
        billsForCharges.addAll(set);
      }
    }
    return billsForCharges;
  }

  /** The Constant GET_ALL_CLAIMS. */
  private static final String GET_ALL_CLAIMS = "SELECT claim_id FROM bill_claim ";

  /**
   * Gets the all claims for bills.
   *
   * @param bills
   *          the bills
   * @return the all claims for bills
   * @throws SQLException
   *           the SQL exception
   */
  public List<String> getAllClaimsForBills(List bills) throws SQLException {
    Set set = new HashSet();
    StringBuilder where = new StringBuilder();
    DataBaseUtil.addWhereFieldInList(where, "bill_no", bills);

    StringBuilder query = new StringBuilder(GET_ALL_CLAIMS);
    query.append(where);

    logger.debug("{}", query);
    List<String> claimsForBills = new ArrayList<String>();
    try (Connection con = DataBaseUtil.getConnection();
        PreparedStatement ps = con.prepareStatement(query.toString());) {
      int index = 1;
      for (int billIndex = 0; billIndex < bills.size(); billIndex++, index++) {
        ps.setString(index, (String) (bills.get(billIndex)));
      }
      try (ResultSet rs = ps.executeQuery();) {
        while (rs.next()) {
          set.add(rs.getString("claim_id"));
        }
        claimsForBills.addAll(set);
      }
    }
    return claimsForBills;
  }

  /** The get charge payment reference. */
  private String getChargePaymentReferenceQuery = " SELECT * FROM insurance_payment_allocation "
      + " WHERE charge_id = ? AND payment_reference = ? AND sale_item_id = ?"
      + " AND is_recovery = ? AND claim_id = ?";

  /**
   * Gets the charge payment reference list.
   *
   * @param claimId
   *          the claim id
   * @param chargeId
   *          the charge id
   * @param paymentReference
   *          the payment reference
   * @param saleItemId
   *          the sale item id
   * @param isRecovery
   *          the is recovery
   * @return the charge payment reference list
   * @throws SQLException
   *           the SQL exception
   */
  private List<BasicDynaBean> getChargePaymentReferenceList(String claimId, String chargeId,
      String paymentReference, Integer saleItemId, String isRecovery) throws SQLException {
    return DataBaseUtil.queryToDynaList(getChargePaymentReferenceQuery,
        new Object[] { chargeId, paymentReference, saleItemId, isRecovery, claimId });
  }

  /** The get bill payment reference. */
  private String getBillPaymentReferenceQuery = 
      " SELECT * FROM all_insurance_remittance_details_view "
      + " WHERE bill_no = ? AND payment_reference = ? AND is_recovery = ? ";

  /**
   * Gets the bill payment reference list.
   *
   * @param billNo
   *          the bill no
   * @param paymentReference
   *          the payment reference
   * @param isRecovery
   *          the is recovery
   * @return the bill payment reference list
   * @throws SQLException
   *           the SQL exception
   */
  public List<BasicDynaBean> getBillPaymentReferenceList(String billNo, String paymentReference,
      String isRecovery) throws SQLException {
    return DataBaseUtil.queryToDynaList(getBillPaymentReferenceQuery,
        new Object[] { billNo, paymentReference, isRecovery });
  }

  /**
   * Delete remittance.
   *
   * @param con
   *          the con
   * @param remittanceId
   *          the remittance id
   * @param userid
   *          the userid
   * @return the map
   * @throws Exception
   *           the exception
   */
  public Map deleteRemittance(Connection con, int remittanceId, String userid) throws Exception {

    Map map = new HashMap();
    StringBuilder allErrors = new StringBuilder();
    String path = RequestContext.getHttpRequest().getContextPath();

    ArrayList<String> activityBills = new ArrayList<String>();
    HashMap<String, BasicDynaBean> pharmacyCharges = new HashMap<String, BasicDynaBean>();
    ArrayList<String> activityClaims = new ArrayList<String>();
    ArrayList<BasicDynaBean> chargeBeansList = new ArrayList<BasicDynaBean>();
    ArrayList<BasicDynaBean> parmacyItemBeansList = new ArrayList<BasicDynaBean>();

    List<BasicDynaBean> insurancePaymentList = allocdao.findAllByKey("remittance_id", remittanceId);
    ArrayList<BasicDynaBean> insuranceClaimsList = new ArrayList<BasicDynaBean>();
    ArrayList<BasicDynaBean> billBeansList = new ArrayList<BasicDynaBean>();

    List<String> billsToUpdtList = new ArrayList<String>();
    List<String> claimsToUpdList = new ArrayList<String>();

    /*
     * Iterate payment list to filter hospital & pharmacy activities.
     */
    for (BasicDynaBean actBean : insurancePaymentList) {

      String paymentReference = (String) actBean.get("payment_reference");
      String chargeType = (String) actBean.get("charge_type");
      String chargeId = (String) actBean.get("charge_id");
      BigDecimal amount = (BigDecimal) actBean.get("amount");
      String claimId = (String) actBean.get("claim_id");

      BigDecimal existingAllocatedAmt = BigDecimal.ZERO;

      if (chargeType.equals("A")) {

        BasicDynaBean billChrgPaymentBean = getChargeClaim(con, chargeId, claimId);
        if (billChrgPaymentBean == null) {
          allErrors.append("No activity found with charge Id :" + chargeId);
          map.put("error", allErrors.toString());
          return map;
        }

        String billNo = (String) billChrgPaymentBean.get("bill_no");
        // String chargeDesc = getChargeDescription(billChrgPaymentBean);

        List<BasicDynaBean> existAllocBeans = findAllocations((String) actBean.get("claim_id"),
            chargeId, 0);

        if (existAllocBeans != null && existAllocBeans.size() > 1) {
          allErrors.append("Cannot delete this remittance file. "
              + " </br> More than one payment exists for this charge. " + chargeId
              + " </br> Please check the bill remittance for payments. "
              + submitdao.urlString(path, "bill-remittance", billNo, billNo));
          map.put("error", allErrors.toString());
          return map;
        }

        if (existAllocBeans != null && existAllocBeans.size() > 0) {
          for (BasicDynaBean abean : existAllocBeans) {
            existingAllocatedAmt = existingAllocatedAmt.add((BigDecimal) abean.get("amount"));
          }
        }

        existingAllocatedAmt = existingAllocatedAmt.subtract(amount);

        BigDecimal insuClaimAmt = billChrgPaymentBean.get("insurance_claim_amt") != null
            ? (BigDecimal) (billChrgPaymentBean.get("insurance_claim_amt"))
            : BigDecimal.ZERO;

        BigDecimal retInsuClaimAmt = billChrgPaymentBean.get("return_insurance_claim_amt") != null
            ? (BigDecimal) (billChrgPaymentBean.get("return_insurance_claim_amt"))
            : BigDecimal.ZERO;

        if (insuClaimAmt.add(retInsuClaimAmt).compareTo(existingAllocatedAmt) == 0) {
          billChrgPaymentBean.set("claim_recd_total", existingAllocatedAmt);
          billChrgPaymentBean.set("claim_status", "C");
        } else {
          billChrgPaymentBean.set("claim_recd_total", existingAllocatedAmt);
          billChrgPaymentBean.set("claim_status", "D");
        }

        chargeBeansList.add(billChrgPaymentBean);

        // Fetch charge related bills to be updated.
        if (!activityBills.contains((String) billChrgPaymentBean.get("bill_no"))) {
          activityBills.add((String) billChrgPaymentBean.get("bill_no"));
        }
        if (!activityClaims.contains((String) billChrgPaymentBean.get("claim_id"))) {
          activityClaims.add((String) billChrgPaymentBean.get("claim_id"));
        }

      } else if (chargeType.equals("P")) {
        Integer saleItemId = (Integer) actBean.get("sale_item_id");
        BasicDynaBean saleItemPaymentBean = getSaleClaimItem(con, saleItemId, claimId);
        if (saleItemPaymentBean == null) {
          allErrors.append("No pharmacy sale bill found with sale Id :" + saleItemId);
          map.put("error", allErrors.toString());
          return map;
        }

        BasicDynaBean billChrgPaymentBean = getCharge(con, chargeId);
        String billNo = (String) billChrgPaymentBean.get("bill_no");
        // String medicineDesc = getMedicineDescription(billChrgPaymentBean,
        // saleItemPaymentBean);
        List<BasicDynaBean> existAllocBeans = findAllocations((String) actBean.get("claim_id"),
            chargeId, saleItemId);

        if (existAllocBeans != null && existAllocBeans.size() > 1) {
          allErrors.append("Cannot delete this remittance file. "
              + " </br> More than one payment exists for this charge. " + chargeId
              + " </br> Please check the bill remittance for payments. "
              + submitdao.urlString(path, "bill-remittance", billNo, billNo));
          map.put("error", allErrors.toString());
          return map;
        }

        if (existAllocBeans != null && existAllocBeans.size() > 0) {
          for (BasicDynaBean abean : existAllocBeans) {
            existingAllocatedAmt = existingAllocatedAmt.add((BigDecimal) abean.get("amount"));
          }
        }

        existingAllocatedAmt = existingAllocatedAmt.subtract(amount);

        BigDecimal insuClaimAmt = saleItemPaymentBean.get("insurance_claim_amt") != null
            ? (BigDecimal) (saleItemPaymentBean.get("insurance_claim_amt"))
            : BigDecimal.ZERO;

        BigDecimal retInsuClaimAmt = saleItemPaymentBean.get("return_insurance_claim_amt") != null
            ? (BigDecimal) (saleItemPaymentBean.get("return_insurance_claim_amt"))
            : BigDecimal.ZERO;

        if ((insuClaimAmt.add(retInsuClaimAmt)).compareTo(existingAllocatedAmt) == 0) {
          saleItemPaymentBean.set("claim_recd", existingAllocatedAmt);
          saleItemPaymentBean.set("claim_status", "C");
        } else {
          saleItemPaymentBean.set("claim_recd", existingAllocatedAmt);
          saleItemPaymentBean.set("claim_status", "D");
        }

        parmacyItemBeansList.add(saleItemPaymentBean);

        // Fetch item related charges to be updated.
        /*
         * if (!pharmacyCharges.contains(charge_id)) pharmacyCharges.add(charge_id);
         */
        if (!pharmacyCharges.containsKey(chargeId + ":" + claimId)) {
          pharmacyCharges.put(chargeId + ":" + claimId, saleItemPaymentBean);
        }

      }
    }

    // Update all pharmacy sale items.
    HashMap scKeyMap = new HashMap();
    // Update all pharmacy sale items.
    for (BasicDynaBean pb : parmacyItemBeansList) {
      // item.updateWithName(con, pb.getMap(), "sale_item_id");
      scKeyMap.put("sale_item_id", pb.get("sale_item_id"));
      scKeyMap.put("claim_id", pb.get("claim_id"));
      salesClaimDetailsDAO.update(con, pb.getMap(), scKeyMap);
    }
    /*
     * for (BasicDynaBean pb : parmacyItemBeansList) { item.updateWithName(con, pb.getMap(),
     * "sale_item_id"); }
     */
    // Update sale related charge in bill_charge by fetching sale id from bill activity charge.
    // for (String phCharge : pharmacyCharges) {
    for (Map.Entry<String, BasicDynaBean> pharmacyCharge : pharmacyCharges.entrySet()) {
      String entryKey = pharmacyCharge.getKey();
      if (null == entryKey || entryKey.trim().equals("")) {
        // we dont expect this nor can handle this
        continue;
      }
      String[] keyParts = entryKey.split(":");
      if (keyParts == null || keyParts.length <= 0) {
        // we dont expect this nor can handle this
        continue;
      }
      // part 1 has charge_id
      String phCharge = keyParts[0];
      BasicDynaBean saleClaimBean = pharmacyCharge.getValue();
      if (null == saleClaimBean) {
        // we dont expect this nor can handle this
        continue;
      }

      String claimID = (String) saleClaimBean.get("claim_id");

      BasicDynaBean sale = findSaleCharge(con, phCharge);
      String saleId = (String) sale.get("sale_id");

      List<BasicDynaBean> items = findItemClaimsBySaleId(con, saleId, claimID);

      BigDecimal totalPhInsAmt = BigDecimal.ZERO;
      for (BasicDynaBean sb : items) {
        totalPhInsAmt = totalPhInsAmt.add((BigDecimal) sb.get("claim_recd"));
      }

      BasicDynaBean phchargebean = getChargeClaim(con, phCharge, claimID);
      BigDecimal insuClaimAmt = phchargebean.get("insurance_claim_amt") != null
          ? (BigDecimal) (phchargebean.get("insurance_claim_amt"))
          : BigDecimal.ZERO;

      BigDecimal retInsuClaimAmt = phchargebean.get("return_insurance_claim_amt") != null
          ? (BigDecimal) (phchargebean.get("return_insurance_claim_amt"))
          : BigDecimal.ZERO;

      if ((insuClaimAmt.add(retInsuClaimAmt)).compareTo(totalPhInsAmt) == 0) {
        phchargebean.set("claim_recd_total", totalPhInsAmt);
        phchargebean.set("claim_status", "C");
      } else {
        phchargebean.set("claim_recd_total", totalPhInsAmt);
        phchargebean.set("claim_status", "D");
      }

      // Fetch charge related bills to be updated.
      if (!activityBills.contains((String) phchargebean.get("bill_no"))) {
        activityBills.add((String) phchargebean.get("bill_no"));
      }
      if (!activityClaims.contains((String) phchargebean.get("claim_id"))) {
        activityClaims.add((String) phchargebean.get("claim_id"));
      }

      chargeBeansList.add(phchargebean);
    }

    // TODO : Need to rework this loop
    for (BasicDynaBean chargeBean : chargeBeansList) {
      String billNo = (String) chargeBean.get("bill_no");
      final String claim_id = (String) chargeBean.get("claim_id");
      BasicDynaBean billbean = billdao.findByKey("bill_no", billNo);
      // String claim_status = (String)billbean.get("primary_claim_status");
      String status = (String) billbean.get("status");

      if (!billsToUpdtList.contains(billNo)) {
        // billbean.set("primary_claim_status", (!status.equals("A") && !status.equals("X")) ?
        // "S" : claim_status);
        billbean.set("status", (!status.equals("A") && !status.equals("X")) ? "F" : status);
        billbean.set("claim_recd_amount", getBillClaimRecdTotal(con, billNo));
        billbean.set("username", userid);
        billBeansList.add(billbean);
        billsToUpdtList.add(billNo);
      }

      // String claim_id = (billbean != null && billbean.get("claim_id") != null) ?
      // (String)billbean.get("claim_id") : null;

      if (claim_id == null) {
        allErrors.append("No Claim Id found in Bill No :" + billNo);
        map.put("error", allErrors.toString());
        return map;
      }

      // Fetch bill related claim to be updated.
      if (claim_id != null) {
        BasicDynaBean insuClaimBean = claimdao.getClaimById(claim_id);
        String insClaimStatus = (String) billbean.get("status");
        if (!claimsToUpdList.contains(claim_id)) {
          insuClaimBean.set("payers_reference_no", null);
          insuClaimBean.set("status",
              (!insClaimStatus.equals("O") && !insClaimStatus.equals("R")) ? "S" : insClaimStatus);
          insuranceClaimsList.add(insuClaimBean);
          claimsToUpdList.add(claim_id);
        }
      }
    }

    HashMap keyMap = new HashMap();
    // Update all charges i.e activities in the deleted remittance
    for (BasicDynaBean chargeBean : chargeBeansList) {
      // charge.updateWithName(con, b.getMap(), "charge_id");
      keyMap.put("charge_id", chargeBean.get("charge_id"));
      keyMap.put("claim_id", chargeBean.get("claim_id"));
      chgClaimDAO.update(con, chargeBean.getMap(), keyMap);
    }

    // Update all bills claim status in the deleted remittance
    for (BasicDynaBean bl : billBeansList) {
      billdao.updateWithName(con, bl.getMap(), "bill_no");
    }

    // Update all payers reference ids (ID payer) for the claims.
    for (BasicDynaBean cl : insuranceClaimsList) {
      claimdao.updateWithName(con, cl.getMap(), "claim_id");
    }

    // Delete remittance activity details from insurance payment allocation table.
    allocdao.delete(con, "remittance_id", remittanceId);

    map.put("error", null);
    map.put("bills", activityBills);
    map.put("claims", activityClaims);
    return map;
  }

  /** The Constant BILL_CLAIM_RECD_TOTAL. */
  private static final String BILL_CLAIM_RECD_TOTAL = 
      "SELECT sum(claim_recd_total) FROM bill_charge_claim WHERE bill_no = ? ";

  /**
   * Gets the bill claim recd total.
   *
   * @param con
   *          the con
   * @param billNo
   *          the bill no
   * @return the bill claim recd total
   * @throws SQLException
   *           the SQL exception
   */
  private BigDecimal getBillClaimRecdTotal(Connection con, String billNo) throws SQLException {
    PreparedStatement ps = con.prepareStatement(BILL_CLAIM_RECD_TOTAL);
    ps.setString(1, billNo);
    BigDecimal claimRecdAmount = DataBaseUtil.getBigDecimalValueFromDb(ps);
    return (claimRecdAmount) == null ? BigDecimal.ZERO : claimRecdAmount;
  }

  /** The Constant CLAIM_RECD_TOTAL. */
  private static final String CLAIM_RECD_TOTAL = 
      "SELECT sum(claim_recd_total) FROM bill_charge_claim WHERE bill_no = ? AND claim_id = ?";

  /**
   * Gets the claim recd total.
   *
   * @param con
   *          the con
   * @param billNo
   *          the bill no
   * @param claimId
   *          the claim id
   * @return the claim recd total
   * @throws SQLException
   *           the SQL exception
   */
  private BigDecimal getClaimRecdTotal(Connection con, String billNo, String claimId)
      throws SQLException {
    try (PreparedStatement ps = con.prepareStatement(CLAIM_RECD_TOTAL);) {
      ps.setString(1, billNo);
      ps.setString(2, claimId);
      BigDecimal claimRecdAmount = DataBaseUtil.getBigDecimalValueFromDb(ps);
      return (claimRecdAmount) == null ? BigDecimal.ZERO : claimRecdAmount;
    }
  }

  /** The Constant FIND_CLAIM_BILLS. */
  private static final String FIND_CLAIM_BILLS = 
      " SELECT b.bill_no,b.status,b.bill_type,b.restriction_type,bclm.claim_status,bclm.priority "
      + " FROM bill b " + " JOIN bill_claim bclm ON(b.bill_no = bclm.bill_no)"
      + " WHERE bclm.claim_id = ? AND b.status != 'X' ORDER BY b.bill_no ";

  /**
   * Gets the claim bills.
   *
   * @param con
   *          the con
   * @param claimID
   *          the claim ID
   * @return the claim bills
   * @throws SQLException
   *           the SQL exception
   */
  public List<BasicDynaBean> getClaimBills(Connection con, String claimID) throws SQLException {
    List<BasicDynaBean> claimBills = null;
    try (PreparedStatement ps = con.prepareStatement(FIND_CLAIM_BILLS);) {
      ps.setString(1, claimID);
      try (ResultSet rs = ps.executeQuery();) {
        RowSetDynaClass rsd = new RowSetDynaClass(rs);
        claimBills = rsd.getRows();
      }
    }
    return claimBills;
  }

  /** The Constant OPEN_CLAIM_CHARGES. */
  private static final String OPEN_CLAIM_CHARGES = "SELECT * FROM bill_charge_claim "
      + " WHERE bill_no = ? AND claim_id = ? AND claim_status != 'C'";

  /**
   * All charges closed.
   *
   * @param con
   *          the con
   * @param billNo
   *          the bill no
   * @param claimId
   *          the claim id
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  private boolean allChargesClosed(Connection con, String billNo, String claimId)
      throws SQLException, IOException {
    List<BasicDynaBean> chargesClosed = null;
    try (PreparedStatement ps = con.prepareStatement(OPEN_CLAIM_CHARGES);) {
      ps.setString(1, billNo);
      ps.setString(2, claimId);
      try (ResultSet rs = ps.executeQuery();) {
        RowSetDynaClass rsd = new RowSetDynaClass(rs);
        chargesClosed = rsd.getRows();
      }
    }

    return (null == chargesClosed || chargesClosed.isEmpty());
  }

  /** The Constant GET_REMITTANCE_CLAIMS. */
  private static final String GET_REMITTANCE_CLAIMS = 
      "SELECT claim_id from insurance_payment_allocation where remittance_id = ?";

  /**
   * Gets the all claims ids.
   *
   * @param con
   *          the con
   * @param remittanceId
   *          the remittance id
   * @return the all claims ids
   * @throws SQLException
   *           the SQL exception
   */
  public List<BasicDynaBean> getAllClaimsIds(Connection con, Integer remittanceId)
      throws SQLException {
    return DataBaseUtil.queryToDynaList(con, GET_REMITTANCE_CLAIMS, remittanceId);
  }

  /**
   * Update bill charge claims.
   *
   * @param phchargebean
   *          the phchargebean
   * @param denialCode
   *          the denial code
   * @param denialRemarks
   *          the denial remarks
   * @param totalPhInsAmt
   *          the total ph ins amt
   * @return the basic dyna bean
   */
  private BasicDynaBean updateBillChargeClaims(BasicDynaBean phchargebean, String denialCode,
      String denialRemarks, BigDecimal totalPhInsAmt) {
    BigDecimal insuClaimAmt = phchargebean.get("insurance_claim_amt") != null
        ? (BigDecimal) (phchargebean.get("insurance_claim_amt"))
        : BigDecimal.ZERO;

    BigDecimal retInsuClaimAmt = phchargebean.get("return_insurance_claim_amt") != null
        ? (BigDecimal) (phchargebean.get("return_insurance_claim_amt"))
        : BigDecimal.ZERO;

    if ((denialCode == null || denialCode.equals(""))
        && ((insuClaimAmt.add(retInsuClaimAmt)).compareTo(totalPhInsAmt) == 0)) {
      phchargebean.set("claim_recd_total", totalPhInsAmt);
      phchargebean.set("claim_status", "C");
    } else {
      phchargebean.set("claim_recd_total", totalPhInsAmt);
      phchargebean.set("denial_code", denialCode);
      phchargebean.set("claim_status", "D");
      phchargebean.set("denial_remarks", denialRemarks);
    }
    return phchargebean;
  }

  /** The Constant UPDATE_CLAIM_ACTIVITY_ID_FOR_HOSPITAL. */
  private static final String UPDATE_CLAIM_ACTIVITY_ID_FOR_HOSPITAL = 
      "UPDATE bill_charge_claim "
      + " SET claim_activity_id = charge_id WHERE bill_no=?";

  /** The Constant UPDATE_CLAIM_ACTIVITY_ID_FOR_PHARMACY. */
  private static final String UPDATE_CLAIM_ACTIVITY_ID_FOR_PHARMACY = 
      " UPDATE sales_claim_details scd "
      + " SET claim_activity_id = ssm.charge_id || '-' || scd.sale_item_id"
      + " FROM store_sales_main ssm "
      + " JOIN store_sales_details ssd ON(ssd.sale_id = ssm.sale_id) "
      + " WHERE scd.sale_item_id = ssd.sale_item_id AND ssm.bill_no=? ";

  /**
   * Update claim activity I ds for XL upload.
   *
   * @param claims
   *          the claims
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   */
  public boolean updateClaimActivityIDsForXLUpload(ArrayList claims) throws SQLException {

    ArrayList<RemittanceAdviceClaim> claimList = new ArrayList<RemittanceAdviceClaim>();
    claimList.addAll(claims);
    if (claimList != null && claimList.size() > 0) {
      for (RemittanceAdviceClaim claim : claimList) {
        List<BasicDynaBean> billList = billCalimDAO.findAllByKey("claim_id", claim.getClaimID());
        if (billList != null && billList.size() > 0) {
          for (BasicDynaBean bill : billList) {
            String billNo = (String) bill.get("bill_no");
            updateClaimActivityIds(billNo, UPDATE_CLAIM_ACTIVITY_ID_FOR_HOSPITAL);
            updateClaimActivityIds(billNo, UPDATE_CLAIM_ACTIVITY_ID_FOR_PHARMACY);
          }
        }
      }
    }
    return true;
  }

  /**
   * Update claim activity ids.
   *
   * @param billNo
   *          the bill no
   * @param query
   *          the query
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   */
  public boolean updateClaimActivityIds(String billNo, String query) throws SQLException {
    Connection con = null;
    boolean success = false;
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      try (PreparedStatement ps = con.prepareStatement(query);) {
        ps.setString(1, billNo);
        int rowsUpdated = ps.executeUpdate();
        if (rowsUpdated > 0) {
          success = true;
        }
      }
    } finally {
      DataBaseUtil.commitClose(con, success);
    }

    return success;
  }
}
