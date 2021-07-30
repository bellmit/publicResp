package com.insta.hms.insurance;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.billing.BillClaimDAO;
import com.insta.hms.common.GenericDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * The Class XLItemRemittanceProvider.
 */
public class XLItemRemittanceProvider extends XLRemittanceProvider {

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(XLItemRemittanceProvider.class);

  /** The storedao. */
  GenericDAO storedao = new GenericDAO("store_sales_details");

  /** The chargedao. */
  GenericDAO chargedao = new GenericDAO("bill_charge");

  /** The date formatter. */
  private SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");

  /** The sheet date formatter. */
  private SimpleDateFormat sheetDateFormatter = new SimpleDateFormat("dd-MM-yyyy");

  /** The service name cell index. */
  private int serviceNameCellIndex = 0;

  /** The charge insurance claim amount cell index. */
  private int chargeInsuranceClaimAmountCellIndex = 0;

  /** The service posted date cell index. */
  private int servicePostedDateCellIndex = 0;

  /** The item id cell index. */
  private int itemIdCellIndex = 0;

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.insurance.XLRemittanceProvider#initializeColumnIndices(java.util.Map)
   */
  @Override
  public void initializeColumnIndices(Map<String, Integer> requiredColumnIndexMap) {
    super.initializeColumnIndices(requiredColumnIndexMap);
    if (itemIndentification.equals("ActivityId")) {
      this.itemIdCellIndex = requiredColumnIndexMap.get("item_id_heading");

    } else {
      this.billNoCellIndex = requiredColumnIndexMap.get("bill_no_heading");
      this.serviceNameCellIndex = requiredColumnIndexMap.get("service_name_heading");
      this.chargeInsuranceClaimAmountCellIndex = requiredColumnIndexMap
          .get("charge_insurance_claim_amount_heading");
      this.servicePostedDateCellIndex = requiredColumnIndexMap.get("service_posted_date_heading");
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.insurance.XLRemittanceProvider#getRequiredColumns()
   */
  @Override
  public String[] getRequiredColumns() {
    if (itemIndentification.equals("ActivityId")) {
      return new String[] { "item_id_heading", "denial_remarks_heading", "amount_heading",
          "claim_id_heading" };
    } else {
      return new String[] { "bill_no_heading", "service_name_heading",
          "charge_insurance_claim_amount_heading", "service_posted_date_heading",
          "denial_remarks_heading", "amount_heading", "claim_id_heading" };
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.insurance.XLRemittanceProvider#skipRow(org.apache.poi.hssf.usermodel.
   * HSSFRow, java.util.List, java.util.List)
   */
  @Override
  public boolean skipRow(HSSFRow row, List<String> claimIds, List<String> billNos) {
    if (row == null) {
      return true;
    }

    if (row.getRowNum() <= headerRowIndex) {
      return true;
    }

    String activityID = getRowActivityId(row);
    if (activityID == null || activityID.equals("")) {
      return true;
    }
    if (!itemIndentification.equals("ActivityId")) {
      String billNo = getRowBillNo(row);
      String claimId = getRowClaimId(row);
      if (billNo == null || billNo.equals("")
          || (billNos.contains(billNo) && claimIds.contains(claimId))) {
        return true;
      }
    }
    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.insurance.XLRemittanceProvider#processRow(com.insta.hms.insurance.
   * RemittanceAdvice, org.apache.poi.hssf.usermodel.HSSFRow, java.util.Map, java.util.List,
   * java.util.List)
   */
  @Override
  public RemittanceAdvice processRow(RemittanceAdvice remAdvice, HSSFRow row, Map errorMap,
      List<String> claimIds, List<String> billNos) throws Exception {
    if (itemIndentification.equals("ActivityId")) {
      remAdvice = processActivityRow(remAdvice, row, errorMap, claimIds, billNos);
    } else {
      remAdvice = processBillChargeRow(remAdvice, row, errorMap, claimIds, billNos);
    }
    return remAdvice;
  }

  /**
   * Process bill charge row.
   *
   * @param remAdvice
   *          the rem advice
   * @param row
   *          the row
   * @param errorMap
   *          the error map
   * @param claimIds
   *          the claim ids
   * @param billNos
   *          the bill nos
   * @return the remittance advice
   * @throws Exception
   *           the exception
   */
  private RemittanceAdvice processBillChargeRow(RemittanceAdvice remAdvice, HSSFRow row,
      Map errorMap, List<String> claimIds, List<String> billNos) throws Exception {

    String billNo = getRowBillNo(row);
    String claimID = getRowClaimId(row);
    if (claimID != null && !claimID.equals("") && !claimIds.contains(claimID)) {

      if (claimID == null || claimID.equals("")) {
        errorMap.put("error", "Invalid Bill. Bill Claim ID is Invalid :" + billNo
            + " found at Line number...." + getLineNumber(row));
        return null;
      }

      if (!validateClaimParameters(row, claimID, errorMap)) {
        return null;
      }

      if (!validateRemittanceParameters(row, billNo, errorMap)) {
        return null;
      }

      ArrayList<RemittanceAdviceActivity> billActivities = getBillActivities(billNo, claimID,
          errorMap);
      // chargesMap.get("error") != null &&
      // !chargesMap.get("error").equals("")) {
      if (null != billActivities) {
        // TODO : IdPayer and itemPaymentReference seems to be null always in the original code
        setRemittanceAdviceClaims(remAdvice, billNo, null, null, claimIds, billActivities);
      } else {
        return null;
      }
    }
    return remAdvice;
  }

  /**
   * Process activity row.
   *
   * @param remAdvice
   *          the rem advice
   * @param row
   *          the row
   * @param errorMap
   *          the error map
   * @param claimIds
   *          the claim ids
   * @param billNos
   *          the bill nos
   * @return the remittance advice
   * @throws SQLException
   *           the SQL exception
   */
  private RemittanceAdvice processActivityRow(RemittanceAdvice remAdvice, HSSFRow row, Map errorMap,
      List<String> claimIds, List<String> billNos) throws SQLException {

    String activityID = getRowActivityId(row);
    RemittanceAdviceClaim claim = null;
    String idPayer = getIdPayer(row, errorMap);
    String itemPaymentReference = getPaymentReference(row);
    if (!validatePaymentReference(row, itemPaymentReference, errorMap)) {
      return null;
    }
    // TODO : this is incorrect. we cant get a claim from charge alone. we need plan or the
    // claim id itself directly
    // right now this fetches the primary plan which works.
    String claimID = getRowClaimId(row);

    if (claimID == null || claimID.equals("")) {
      errorMap.put("error", "Invalid Activity ID. Activity Bill Claim ID is Invalid :" + activityID
          + " found at Line number...." + getLineNumber(row));
      return null;
    }

    if (claimID != null && !claimID.equals("") && !claimIds.contains(claimID)) {
      if (!validateClaimParameters(row, claimID, errorMap)) {
        return null;
      }

      claim = new RemittanceAdviceClaim();
      claim.setClaimID(claimID);
      claim.setIdPayer(idPayer);
      claim.setPaymentReference(itemPaymentReference);

      claimIds.add(claimID);

      ArrayList<RemittanceAdviceActivity> activities = getClaimActivities(claimID, errorMap);
      if (null != activities) {
        // activityMap.get("error") != null &&
        // !activityMap.get("error").equals("")) {
        claim.setActivities(activities);
        remAdvice.addClaim(claim);
      } else {
        return null;
      }
    }
    return remAdvice;
  }

  /** The Constant CHARGE_CLAIM_ID. */
  private static final String CHARGE_CLAIM_ID = 
      " SELECT bc.claim_id FROM bill_claim bc, bill_charge_claim bcc"
      + " WHERE bc.bill_no = bcc.bill_no and bc.claim_id = bcc.claim_id"
      + " and bc.priority = 1 and " + " bcc.charge_id = ?";

  /**
   * Gets the charge claim id.
   *
   * @param activityID
   *          the activity ID
   * @param errorMap
   *          the error map
   * @return the charge claim id
   * @throws SQLException
   *           the SQL exception
   */
  private String getChargeClaimId(String activityID, Map errorMap) throws SQLException {
    String chargeId = activityID.split("-")[1];
    if (chargeId != null && !chargeId.equals("") && chargeId.startsWith("CH")) {
      return DataBaseUtil.getStringValueFromDb(CHARGE_CLAIM_ID, chargeId);
    }
    return null;
  }

  /**
   * Gets the claim activities.
   *
   * @param claimID
   *          the claim ID
   * @param errorMap
   *          the error map
   * @return the claim activities
   * @throws SQLException
   *           the SQL exception
   */
  private ArrayList<RemittanceAdviceActivity> getClaimActivities(String claimID, Map errorMap)
      throws SQLException {

    Map activityMap = new HashMap();
    ArrayList<RemittanceAdviceActivity> remAdviceActivities = 
        new ArrayList<RemittanceAdviceActivity>();

    Iterator rowIterator = sheet.rowIterator();
    HSSFRow row = null;
    while (rowIterator.hasNext()) {

      row = (HSSFRow) rowIterator.next();

      logger
          .debug("Reading claim activity values from line number............" + getLineNumber(row));

      if (row.getRowNum() <= headerRowIndex) {
        continue;
      }

      if (row == null) {
        continue;
      }

      RemittanceAdviceActivity activity = new RemittanceAdviceActivity();

      String activityID = getRowActivityId(row);
      if (activityID == null || activityID.equals("")) {
        continue;
      }

      String rowClaimID = getRowClaimId(row);

      if (claimID == null || claimID.equals("")) {
        errorMap.put("error", "Invalid Activity ID. Activity Bill Claim ID is Invalid :"
            + activityID + " found at Line number...." + getLineNumber(row));
        return null;
      }

      if (rowClaimID != null && rowClaimID.equals(claimID)) {

        BigDecimal paymentAmount = getPaymentAmount(row, errorMap);
        if (null == paymentAmount) {
          return null;
        }

        String denialRemarks = getRowDenialRemarks(row);

        if (activityID != null && !activityID.equals("")) {
          BigDecimal insuranceClaimAmount = BigDecimal.ZERO;
          String chargeType = activityID.split("-")[0];
          String chargeId = activityID.split("-")[1];

          // TODO : This should come from bill charge claim, instead of bill_charge
          BasicDynaBean chargeBean = chargedao.findByKey("charge_id", chargeId);
          if (null != chargeBean) {
            String billNo = (String) chargeBean.get("bill_no");
            if (!validateRemittanceParameters(row, billNo, errorMap)) {
              return null;
            }
          } else {
            errorMap.put("error", "Invalid Activity ID. Activity Charge ID is Invalid :"
                + activityID + " found at Line number...." + getLineNumber(row));
            return null;
          }

          if (chargeType != null && !chargeType.equals("") && chargeType.startsWith("P")) {
            String saleItemId = activityID.split("-")[2];
            // TODO : this should come from sale_claim_details instead of store_sale_details
            BasicDynaBean saleItemPaymentBean = storedao.findByKey("sale_item_id",
                new Integer(saleItemId));
            if (saleItemPaymentBean != null) {
              insuranceClaimAmount = ((BigDecimal) saleItemPaymentBean.get("insurance_claim_amt"))
                  .add((BigDecimal) saleItemPaymentBean.get("return_insurance_claim_amt"));
            }
          } else {
            if (chargeBean != null) {
              insuranceClaimAmount = ((BigDecimal) chargeBean.get("insurance_claim_amount"))
                  .add((BigDecimal) chargeBean.get("return_insurance_claim_amt"));
            }
          }

          activity.setActivityID(activityID);
          activity.setPaymentAmount(paymentAmount);

          if (insuranceClaimAmount.compareTo(paymentAmount) != 0) {
            activity.setDenialRemarks((denialRemarks == null)
                ? "Denied as the insurance claim amount and payment amount is not equal."
                : denialRemarks);
          } else {
            activity.setDenialRemarks(null);
          }

          remAdviceActivities.add(activity);
        }
      }
    }

    // activityMap.put("RemAdviceActivities", remAdviceActivities);
    return remAdviceActivities;
  }

  /**
   * Gets the bill activities.
   *
   * @param billNo
   *          the bill no
   * @param claimId
   *          the claim id
   * @param errorMap
   *          the error map
   * @return the bill activities
   * @throws Exception
   *           the exception
   */
  private ArrayList<RemittanceAdviceActivity> getBillActivities(String billNo, String claimId,
      Map errorMap) throws Exception {

    ArrayList<String> billActivityIDs = new ArrayList<String>();
    // Map chargesMap = new HashMap();
    ArrayList<RemittanceAdviceActivity> billActivities = new ArrayList<RemittanceAdviceActivity>();

    Iterator rowIterator = sheet.rowIterator();
    HSSFRow row = null;
    while (rowIterator.hasNext()) {
      row = (HSSFRow) rowIterator.next();

      logger.debug("Reading charge values from line number............" + getLineNumber(row));

      if (row.getRowNum() <= headerRowIndex) {
        continue;
      }

      if (row == null) {
        continue;
      }

      String rowBillNo = getRowBillNo(row);
      String rowClaimId = getRowClaimId(row);

      if (rowBillNo == null || rowBillNo.equals("") || !rowBillNo.equals(billNo)) {
        continue;
      }

      if (rowClaimId == null || rowClaimId.equals("") || !rowClaimId.equals(claimId)) {
        continue;
      }

      String serviceName = getRowServiceName(row);

      if ((serviceName == null) || (serviceName.equals(""))) {
        errorMap.put("error", "Invalid/No Service Name :" + serviceName
            + " found at Line number...." + getLineNumber(row));
        return null;
      }

      List<BasicDynaBean> activities = claimdao.findChargesByDescription(billNo, claimId,
          serviceName);

      if (activities == null || activities.size() == 0) {
        errorMap.put("error", "Invalid/No Service Name :" + serviceName + " found. Bill No: "
            + billNo + " at Line number...." + getLineNumber(row));
        return null;
      }

      if (activities != null && activities.size() > 0) {

        BasicDynaBean chrg = null;
        String activityID = null;

        // Duplicate activities
        chrg = getMatchingChargeItem(row, activities, errorMap, billActivityIDs);

        if (chrg == null) {
          errorMap.put("error",
              "Invalid/No Service Name :" + serviceName + " found (Empty item bean). Bill No: "
                  + billNo + " at Line number...." + getLineNumber(row));
          return null;
        }

        activityID = (String) chrg.get("activity_charge_id");

        if (activityID != null && !activityID.equals("") && !billActivityIDs.contains(activityID)) {
          RemittanceAdviceActivity activity = getRemittanceActivity(row, activityID, errorMap);
          billActivities.add(activity);
        }
      }
    } // while

    // chargesMap.put("RemAdviceBillActivities", billActivities);
    return billActivities;
  }

  /**
   * Sets the remittance advice claims.
   *
   * @param remAdvice
   *          the rem advice
   * @param billNo
   *          the bill no
   * @param idPayer
   *          the id payer
   * @param paymentReference
   *          the payment reference
   * @param claimIds
   *          the claim ids
   * @param billActivities
   *          the bill activities
   * @throws Exception
   *           the exception
   */
  private void setRemittanceAdviceClaims(RemittanceAdvice remAdvice, String billNo, String idPayer,
      String paymentReference, List<String> claimIds,
      ArrayList<RemittanceAdviceActivity> billActivities) throws Exception {

    BasicDynaBean billClaimBean = new BillClaimDAO().getPrimaryBillClaim(billNo);
    String claimID = (String) billClaimBean.get("claim_id");

    if (claimID != null && !claimID.equals("") && !claimIds.contains(claimID)) {

      RemittanceAdviceClaim claim = new RemittanceAdviceClaim();

      claim.setClaimID(claimID);
      claim.setIdPayer(idPayer);
      claim.setPaymentReference(paymentReference);

      claimIds.add(claimID);
      claim.setActivities(billActivities);
      remAdvice.addClaim(claim);

    } else {

      ArrayList<RemittanceAdviceClaim> claims = remAdvice.getClaim();
      RemittanceAdviceClaim claim = null;

      for (RemittanceAdviceClaim remclaim : claims) {
        if (remclaim.getClaimID().equals(claimID)) {
          claim = remclaim;
          break;
        }
      }
      if (claim == null) {
        throw new RuntimeException("Remittance Advice Claim is null");
      } else {
        // Get existing claim activities
        ArrayList<RemittanceAdviceActivity> activities = claim.getActivities();

        // Add the bill activities
        activities.addAll(billActivities);

        // Set the claim activities
        claim.setActivities(activities);
      }
    }
  }

  /**
   * Gets the row claim id.
   *
   * @param row
   *          the row
   * @param errorMap
   *          the error map
   * @return the row claim id
   * @throws SQLException
   *           the SQL exception
   */
  private String getRowClaimId(HSSFRow row, Map errorMap) throws SQLException {
    String billNo = getRowBillNo(row);
    BasicDynaBean billClaimBean = new BillClaimDAO().getPrimaryBillClaim(billNo);
    String claimID = billClaimBean.get("claim_id") != null ? (String) billClaimBean.get("claim_id")
        : null;
    return claimID;
  }

  /**
   * Gets the matching charge item.
   *
   * @param row
   *          the row
   * @param billActivities
   *          the bill activities
   * @param errorMap
   *          the error map
   * @param billActivityIDs
   *          the bill activity I ds
   * @return the matching charge item
   * @throws ParseException
   *           the parse exception
   */
  private BasicDynaBean getMatchingChargeItem(HSSFRow row, List<BasicDynaBean> billActivities,
      Map errorMap, List<String> billActivityIDs) throws ParseException {

    BasicDynaBean chrg = null;
    String activityId = null;

    // There is only one entry that matches the description, just return that
    if (billActivities.size() == 0) {
      return billActivities.get(0);
    }

    // There is more than one entry that matches the description, so we compare the claim
    // amount and posted date
    BigDecimal insuranceClaimAmount = getInsuranceClaimAmount(row, errorMap);
    if (null == insuranceClaimAmount) {
      return null;
    }

    java.sql.Date postedDate = getPostedDate(row, errorMap);
    if (null == postedDate) {
      return null;
    }

    for (BasicDynaBean charge : billActivities) {
      BigDecimal insuranceclaimAmount = (BigDecimal) charge.get("insurance_claim_amt");
      java.util.Date dt = dateFormatter.parse((String) charge.get("posted_date"));
      Date posteddate = new java.sql.Date(dt.getTime());
      // String activity_id = activityID;

      if (!billActivityIDs.contains(activityId) && (posteddate.compareTo(postedDate) == 0
          || insuranceclaimAmount.compareTo(insuranceClaimAmount) == 0)) {
        chrg = charge;
        activityId = (String) chrg.get("activity_charge_id");
        billActivityIDs.add(activityId);
        break;
      }
    }

    return chrg;
  }

  /**
   * Gets the remittance activity.
   *
   * @param row
   *          the row
   * @param activityID
   *          the activity ID
   * @param errorMap
   *          the error map
   * @return the remittance activity
   */
  private RemittanceAdviceActivity getRemittanceActivity(HSSFRow row, String activityID,
      Map errorMap) {

    String denialRemarks = getRowDenialRemarks(row);
    BigDecimal paymentAmount = getPaymentAmount(row, errorMap);
    if (null == paymentAmount) {
      return null;
    }

    BigDecimal insuranceClaimAmount = getInsuranceClaimAmount(row, errorMap);
    if (null == insuranceClaimAmount) {
      return null;
    }

    RemittanceAdviceActivity activity = new RemittanceAdviceActivity();

    activity.setActivityID(activityID);
    activity.setPaymentAmount(paymentAmount);

    if (insuranceClaimAmount.compareTo(paymentAmount) != 0) {
      activity.setDenialRemarks((denialRemarks == null)
          ? "Denied as the insurance claim amount and payment amount is not equal."
          : denialRemarks);
    } else {
      activity.setDenialRemarks(null);
    }
    return activity;
  }

  /**
   * Gets the row activity id.
   *
   * @param row
   *          the row
   * @return the row activity id
   */
  private String getRowActivityId(HSSFRow row) {
    return extractString(row, itemIdCellIndex, false);
  }

  /**
   * Gets the posted date.
   *
   * @param row
   *          the row
   * @param errorMap
   *          the error map
   * @return the posted date
   */
  private Date getPostedDate(HSSFRow row, Map errorMap) {
    return extractDate(row, servicePostedDateCellIndex, sheetDateFormatter, "posted/service date",
        errorMap);
  }

  /**
   * Gets the insurance claim amount.
   *
   * @param row
   *          the row
   * @param errorMap
   *          the error map
   * @return the insurance claim amount
   */
  private BigDecimal getInsuranceClaimAmount(HSSFRow row, Map errorMap) {
    return extractBigDecimal(row, chargeInsuranceClaimAmountCellIndex, "insurance claim amount",
        errorMap);
  }

  /**
   * Gets the row service name.
   *
   * @param row
   *          the row
   * @return the row service name
   */
  private String getRowServiceName(HSSFRow row) {
    return extractString(row, serviceNameCellIndex, false);
  }
}
