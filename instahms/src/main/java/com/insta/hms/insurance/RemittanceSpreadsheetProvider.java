package com.insta.hms.insurance;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.billing.BillClaimDAO;
import com.insta.hms.billing.ClaimDAO;
import com.insta.hms.billing.ClaimSubmissionDAO;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.master.Accounting.AccountingGroupMasterDAO;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.TpaMaster.TpaMasterDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The Class RemittanceSpreadsheetProvider.
 */
public class RemittanceSpreadsheetProvider {

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(RemittanceSpreadsheetProvider.class);

  /** The claimdao. */
  ClaimDAO claimdao = new ClaimDAO();

  /** The submitdao. */
  ClaimSubmissionDAO submitdao = new ClaimSubmissionDAO();

  /** The storedao. */
  GenericDAO storedao = new GenericDAO("store_sales_details");

  /** The chargedao. */
  GenericDAO chargedao = new GenericDAO("bill_charge");

  /** The billdao. */
  GenericDAO billdao = new GenericDAO("bill");

  /** The visitdao. */
  GenericDAO visitdao = new GenericDAO("patient_registration");

  /** The date formatter. */
  private SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");

  /** The sheet date formatter. */
  private SimpleDateFormat sheetDateFormatter = new SimpleDateFormat("dd-MM-yyyy");

  /** The sheet. */
  private HSSFSheet sheet = null;

  /** The header row index. */
  private int headerRowIndex = 0;

  /** The bill no cell index. */
  private int billNoCellIndex = 0;

  /** The service name cell index. */
  private int serviceNameCellIndex = 0;

  /** The charge insurance claim amount cell index. */
  private int chargeInsuranceClaimAmountCellIndex = 0;

  /** The service posted date cell index. */
  private int servicePostedDateCellIndex = 0;

  /** The item id cell index. */
  private int itemIdCellIndex = 0;

  /** The payment ref cell index. */
  private int paymentRefCellIndex = 0;

  /** The payer id cell index. */
  private int payerIdCellIndex = 0;

  /** The denial remarks cell index. */
  private int denialRemarksCellIndex = 0;

  /** The amount cell index. */
  private int amountCellIndex = 0;

  /** The detail level. */
  private String detailLevel = null;

  /** The item indentification. */
  private String itemIndentification = null;

  /** The payment reference. */
  private String paymentReference = null;

  /** The center id. */
  private int centerId = 0;

  /** The center name. */
  private String centerName = null;

  /** The account group. */
  private int accountGroup = 0;

  /** The account group name. */
  private String accountGroupName = null;

  /** The tpa id. */
  private String tpaId = null;

  /** The tpa name. */
  private String tpaName = null;

  /**
   * Validate required columns in work sheet.
   *
   * @param selectedValues
   *          the selected values
   * @param workBook
   *          the work book
   * @param selectedSheetNo
   *          the selected sheet no
   * @return the map
   * @throws Exception
   *           the exception
   */
  // Validate the required columns in the Excel Sheet.
  public Map validateRequiredColumnsInWorkSheet(Map selectedValues, HSSFWorkbook workBook,
      int selectedSheetNo) throws Exception {

    Map descMap = new HashMap();

    int noOfSheets = workBook.getNumberOfSheets();

    logger.debug("Number of sheets in the Excel file...." + noOfSheets);

    HSSFSheet sheet = null;
    try {
      sheet = workBook.getSheetAt(selectedSheetNo);

    } catch (IllegalArgumentException ie) {
      descMap.put("error", ie.getMessage());
      return descMap;
    }

    if (sheet == null) {
      descMap.put("error",
          "Selected sheet number :" + (selectedSheetNo + 1) + " is not found in the Excel file");
      return descMap;
    }

    this.sheet = sheet;

    Iterator rowIterator = sheet.rowIterator();
    HSSFRow row1 = getHeaderRow(rowIterator);

    if (row1 == null) {
      descMap.put("error", "Selected sheet number :" + (selectedSheetNo + 1)
          + " has first column empty. Please delete the column and upload again");
      return descMap;
    }

    int headerRowIndex = row1.getRowNum();
    this.headerRowIndex = headerRowIndex;

    if (selectedValues.get("account_group") != null) {
      this.accountGroup = new Integer((String) selectedValues.get("account_group"));
      this.accountGroupName = (String) selectedValues.get("account_group_name");

    } else if (selectedValues.get("center_id") != null) {
      this.centerId = new Integer((String) selectedValues.get("center_id"));
      this.centerName = (String) selectedValues.get("center_name");
    }

    this.tpaId = (String) selectedValues.get("tpa_id");
    this.tpaName = (String) ((BasicDynaBean) new TpaMasterDAO().findByKey("tpa_id", this.tpaId))
        .get("tpa_name");

    this.detailLevel = (String) selectedValues.get("detail_level");
    this.itemIndentification = (String) selectedValues.get("item_identification");
    this.paymentReference = null;

    if (!selectedValues.get("payment_ref_type").equals("PerItem")) {
      paymentReference = (String) selectedValues.get("payment_reference");
    }
    selectedValues.remove("payment_reference");
    selectedValues.remove("item_identification");
    selectedValues.remove("worksheet_index");
    selectedValues.remove("detail_level");

    Map<String, Integer> requiredColumnIndexMap = new HashMap<String, Integer>();
    Set<String> columnkeys = selectedValues.keySet();

    // Assume that there are 50 columns in the spreadsheet.
    for (int i = 0; i < 50; i++) {
      HSSFCell cell = row1.getCell(i);

      if (cell != null && cell.getStringCellValue() != null) {
        String header = cell.getStringCellValue().trim().toLowerCase();

        for (String key : columnkeys) {

          if (((String) selectedValues.get(key)).trim().toLowerCase().equals(header)) {
            requiredColumnIndexMap.put(key, i);
          }
        }
      }
    }

    if (detailLevel.equals("I")) {
      if (itemIndentification.equals("ActivityId")) {
        if (requiredColumnIndexMap.get("item_id_heading") == null) {
          descMap.put("error", "Column Not Found: item_id_heading");
          return descMap;
        }
        this.itemIdCellIndex = requiredColumnIndexMap.get("item_id_heading");

      } else {
        if (requiredColumnIndexMap.get("bill_no_heading") == null) {
          descMap.put("error", "Column Not Found: bill_no_heading");
          return descMap;
        }
        this.billNoCellIndex = requiredColumnIndexMap.get("bill_no_heading");

        if (requiredColumnIndexMap.get("service_name_heading") == null) {
          descMap.put("error", "Column Not Found: service_name_heading");
          return descMap;
        }
        this.serviceNameCellIndex = requiredColumnIndexMap.get("service_name_heading");

        if (requiredColumnIndexMap.get("charge_insurance_claim_amount_heading") == null) {
          descMap.put("error", "Column Not Found: charge_insurance_claim_amount_heading");
          return descMap;
        }
        this.chargeInsuranceClaimAmountCellIndex = requiredColumnIndexMap
            .get("charge_insurance_claim_amount_heading");

        if (requiredColumnIndexMap.get("service_posted_date_heading") == null) {
          descMap.put("error", "Column Not Found: service_posted_date_heading");
          return descMap;
        }
        this.servicePostedDateCellIndex = requiredColumnIndexMap.get("service_posted_date_heading");
      }
    } else {
      if (requiredColumnIndexMap.get("bill_no_heading") == null) {
        descMap.put("error", "Column Not Found: bill_no_heading");
        return descMap;
      }
      this.billNoCellIndex = requiredColumnIndexMap.get("bill_no_heading");
    }

    if (paymentReference == null || paymentReference.equals("")) {
      if (requiredColumnIndexMap.get("payment_reference_heading") == null) {
        descMap.put("error", "Column Not Found: payment_reference_heading");
        return descMap;
      }
      this.paymentRefCellIndex = requiredColumnIndexMap.get("payment_reference_heading");
    }

    /*
     * if (requiredColumnIndexMap.get("payer_id_heading") == null) { descMap.put("error",
     * "Column Not Found: payer_id_heading"); return descMap; }
     */
    this.payerIdCellIndex = requiredColumnIndexMap.get("payer_id_heading") != null
        ? requiredColumnIndexMap.get("payer_id_heading")
        : 0;

    if (requiredColumnIndexMap.get("denial_remarks_heading") == null) {
      descMap.put("error", "Column Not Found: denial_remarks_heading");
      return descMap;
    }
    this.denialRemarksCellIndex = requiredColumnIndexMap.get("denial_remarks_heading");

    if (requiredColumnIndexMap.get("amount_heading") == null) {
      descMap.put("error", "Column Not Found: amount_heading");
      return descMap;
    }
    this.amountCellIndex = requiredColumnIndexMap.get("amount_heading");

    return descMap;
  }

  /**
   * Gets the report desc from sheet item level.
   *
   * @param selectedValues
   *          the selected values
   * @param workBook
   *          the work book
   * @param selectedSheetNo
   *          the selected sheet no
   * @return the report desc from sheet item level
   * @throws Exception
   *           the exception
   */
  public Map getReportDescFromSheetItemLevel(Map selectedValues, HSSFWorkbook workBook,
      int selectedSheetNo) throws Exception {

    Map descMap = validateRequiredColumnsInWorkSheet(selectedValues, workBook, selectedSheetNo);

    if (descMap != null && descMap.get("error") != null && !descMap.get("error").equals("")) {
      return descMap;
    }

    Iterator rowIterator = sheet.rowIterator();

    RemittanceAdvice remAdvice = new RemittanceAdvice(); // Initializes header and claims list

    ArrayList<String> claimIds = new ArrayList<String>();
    ArrayList<String> billNos = new ArrayList<String>();

    // Iterate rows to fetch the activities and create RemittanceAdviceClaim

    HSSFRow row = null;
    while (rowIterator.hasNext()) {
      row = (HSSFRow) rowIterator.next();
      int lineNumber = row.getRowNum() + 1;

      logger.debug("Reading Values from line number............" + lineNumber);

      if (row.getRowNum() <= headerRowIndex) {
        continue;
      }

      if (row == null) {
        continue;
      }

      String activityID = getRowActivityId(row);
      if (activityID == null || activityID.equals("")) {
        continue;
      }

      String idPayer = null;
      String itemPaymentReference = null;

      if (itemIndentification.equals("ActivityId")) {

        idPayer = getRowIdPayer(row);
        if (payerIdCellIndex != 0) {
          if (idPayer == null || idPayer.equals("")) {
            descMap.put("error",
                "Invalid ID Payer :" + idPayer + " found at Line number...." + lineNumber);
            return descMap;
          }
        } else {
          idPayer = new String(
              DataBaseUtil.getNextSequence("insurance_claim_payers_reference_no_seq") + "");
        }

        itemPaymentReference = getPaymentReference(row);
        if (itemPaymentReference == null || itemPaymentReference.equals("")) {
          descMap.put("error", "Invalid Payment Reference :" + itemPaymentReference
              + " found at Line number...." + lineNumber);
          return descMap;
        }

        if (itemPaymentReference.length() > 50) {
          descMap.put("error", "Payment Reference is more than 50 characters :"
              + itemPaymentReference + " found at Line number...." + lineNumber);
          return descMap;
        }

        String claimID = getChargeClaimId(activityID);

        if (claimID == null || claimID.equals("")) {
          descMap.put("error", "Invalid Activity ID. Activity Bill Claim ID is Invalid :"
              + activityID + " found at Line number...." + lineNumber);
          return descMap;
        } else if (claimID.startsWith("Invalid")) {
          descMap.put("error",
              claimID + " : " + activityID + " found at Line number...." + lineNumber);
          return descMap;
        }

        if (claimID != null && !claimID.equals("") && !claimIds.contains(claimID)) {

          BasicDynaBean claimBean = claimdao.findClaimById(claimID);
          int resubmissionCount = claimBean.get("resubmission_count") != null
              ? (Integer) claimBean.get("resubmission_count")
              : 0;
          String isResubmission = resubmissionCount > 0 ? "Y" : "N";
          BasicDynaBean batchBean = submitdao.getLatestSubmissionBatch(claimID, isResubmission);
          String batchStatus = (batchBean != null && batchBean.get("status") != null)
              ? (String) batchBean.get("status")
              : null;
          String batchId = (batchBean != null && batchBean.get("submission_batch_id") != null)
              ? (String) batchBean.get("submission_batch_id")
              : null;

          if (batchBean == null || batchId == null) {
            descMap.put("error",
                "Invalid Claim ID " + claimID
                    + ". No Submission Batch ID found for the Claim found at Line number...."
                    + lineNumber);
            return descMap;
          }

          if (batchStatus == null || !batchStatus.equals("S")) {
            descMap.put("error", "Submission Batch " + batchId
                + " is not marked as Sent. Please mark it as Sent and Upload again.");
            return descMap;
          }

          RemittanceAdviceClaim claim = new RemittanceAdviceClaim();
          claim.setClaimID(claimID);
          claim.setIdPayer(idPayer);
          claim.setPaymentReference(itemPaymentReference);

          claimIds.add(claimID);

          Map activityMap = getClaimActivities(claimID);
          if (activityMap.get("error") != null && !activityMap.get("error").equals("")) {
            descMap.put("error", activityMap.get("error"));
            return descMap;
          } else {
            ArrayList<RemittanceAdviceActivity> activities = 
                (ArrayList<RemittanceAdviceActivity>) activityMap
                .get("RemAdviceActivities");

            claim.setActivities(activities);

            remAdvice.addClaim(claim);
          }
        }

      } else {

        String billNo = getRowBillNo(row);
        if (billNo == null || billNo.equals("") || billNos.contains(billNo)) {
          continue;
        }

        if (billNo != null && !billNo.equals("") && !billNos.contains(billNo)) {

          BasicDynaBean billClaimBean = new BillClaimDAO().getPrimaryBillClaim(billNo);
          String claimID = billClaimBean.get("claim_id") != null
              ? (String) billClaimBean.get("claim_id")
              : null;

          if (claimID == null || claimID.equals("")) {
            descMap.put("error", "Invalid Bill. Bill Claim ID is Invalid :" + billNo
                + " found at Line number...." + lineNumber);
            return descMap;
          }

          BasicDynaBean claimBean = claimdao.findClaimById(claimID);
          int resubmissionCount = claimBean.get("resubmission_count") != null
              ? (Integer) claimBean.get("resubmission_count")
              : 0;
          String isResubmission = resubmissionCount > 0 ? "Y" : "N";
          BasicDynaBean batchBean = submitdao.getLatestSubmissionBatch(claimID, isResubmission);
          String batchStatus = (batchBean != null && batchBean.get("status") != null)
              ? (String) batchBean.get("status")
              : null;
          String batchId = (batchBean != null && batchBean.get("submission_batch_id") != null)
              ? (String) batchBean.get("submission_batch_id")
              : null;

          if (batchBean == null || batchId == null) {
            descMap.put("error",
                "Invalid Claim ID " + claimID
                    + ". No Submission Batch ID found for the Claim found at Line number...."
                    + lineNumber);
            return descMap;
          }

          if (batchStatus == null || !batchStatus.equals("S")) {
            descMap.put("error", "Submission Batch " + batchId
                + " is not marked as Sent. Please mark it as Sent and Upload again.");
            return descMap;
          }
          BasicDynaBean billBean = billdao.findByKey("bill_no", billNo);

          BasicDynaBean visitBean = visitdao.findByKey("patient_id", billBean.get("visit_id"));
          String tpaId = (visitBean != null && visitBean.get("primary_sponsor_id") != null)
              ? (String) visitBean.get("primary_sponsor_id")
              : "";
          int centerId = (visitBean != null && visitBean.get("center_id") != null)
              ? (Integer) visitBean.get("center_id")
              : 0;

          if (this.centerName != null && this.centerId != centerId) {
            String centerName = (String) ((BasicDynaBean) new CenterMasterDAO()
                .findByKey("center_id", centerId)).get("center_name");
            descMap.put("error",
                "Center selected (" + this.centerName + ") is not same as the visit center ("
                    + centerName + ") at Line number " + lineNumber);
            return descMap;
          }

          if (!this.tpaId.equals(tpaId)) {
            String tpaName = (String) ((BasicDynaBean) new TpaMasterDAO().findByKey("tpa_id",
                tpaId)).get("tpa_name");
            descMap.put("error", "TPA selected (" + this.tpaName + ") is not same as the bill TPA ("
                + tpaName + ") at Line number " + lineNumber);
            return descMap;
          }

          int accountGroup = (billBean != null && billBean.get("account_group") != null)
              ? (Integer) billBean.get("account_group")
              : 0;
          if (this.accountGroupName != null && this.accountGroup != accountGroup) {
            String accountGroupName = (String) ((BasicDynaBean) new AccountingGroupMasterDAO()
                .findByKey("account_group_id", accountGroup)).get("account_group_name");
            descMap.put("error",
                "Account Group selected (" + this.accountGroupName
                    + ") is not same as the bill account group (" + accountGroupName
                    + ") at Line number " + lineNumber);
            return descMap;
          }

          Map chargesMap = getBillActivities(billNo);

          if (chargesMap.get("error") != null && !chargesMap.get("error").equals("")) {
            descMap.put("error", chargesMap.get("error"));
            return descMap;
          } else {
            setBillActivities(remAdvice, billNo, idPayer, itemPaymentReference, claimIds,
                chargesMap);
            billNos.add(billNo);
          }
        }
      }
    }

    descMap.put("RemittanceDesc", remAdvice);
    return descMap;
  }

  /**
   * Gets the payment reference.
   *
   * @param row
   *          the row
   * @return the payment reference
   */
  private String getPaymentReference(HSSFRow row) {
    String itemPaymentReference = null;
    if (paymentReference == null || paymentReference.equals("")) {
      HSSFCell pmntRefCell = row.getCell(paymentRefCellIndex);
      if (pmntRefCell != null) {
        Object cellVal = getCellValue(pmntRefCell);
        if (cellVal != null) {
          if (cellVal.getClass() == java.lang.String.class) {
            itemPaymentReference = new String(cellVal.toString());
          } else if (cellVal.getClass() == java.lang.Double.class) {
            itemPaymentReference = new String(((Double) cellVal).intValue() + "");
          }
        }
      }
    } else {
      itemPaymentReference = paymentReference;
    }
    return itemPaymentReference;
  }

  /**
   * Gets the row id payer.
   *
   * @param row
   *          the row
   * @return the row id payer
   */
  private String getRowIdPayer(HSSFRow row) {
    String idPayer = null;
    HSSFCell idPayerCell = row.getCell(payerIdCellIndex);
    if (idPayerCell != null) {
      Object cellVal = getCellValue(idPayerCell);
      if (cellVal != null) {
        if (cellVal.getClass() == java.lang.String.class) {
          idPayer = new String(cellVal.toString());
        } else if (cellVal.getClass() == java.lang.Double.class) {
          idPayer = new String(((Double) cellVal).intValue() + "");
        }
      }
    }
    return idPayer;
  }

  /**
   * Gets the row bill no.
   *
   * @param row
   *          the row
   * @return the row bill no
   */
  private String getRowBillNo(HSSFRow row) {
    String billNo = null;
    HSSFCell billNoCell = row.getCell(billNoCellIndex);
    if (billNoCell != null) {
      Object cellVal = getCellValue(billNoCell);
      if (cellVal != null) {
        billNo = new String(cellVal.toString());
      }
    }
    return billNo;
  }

  /**
   * Gets the row activity id.
   *
   * @param row
   *          the row
   * @return the row activity id
   */
  private String getRowActivityId(HSSFRow row) {
    String activityID = null;
    HSSFCell activityIdCell = row.getCell(itemIdCellIndex);
    if (activityIdCell != null) {
      Object cellVal = getCellValue(activityIdCell);
      if (cellVal != null) {
        activityID = new String(cellVal.toString());
      }
    }
    return activityID;
  }

  /** The Constant CHARGE_CLAIM_ID. */
  public static final String CHARGE_CLAIM_ID = "SELECT claim_id FROM bill WHERE bill_no = "
      + "(SELECT bill_no FROM bill_charge WHERE charge_id = ?)";

  /**
   * Gets the charge claim id.
   *
   * @param activityID
   *          the activity ID
   * @return the charge claim id
   * @throws Exception
   *           the exception
   */
  private String getChargeClaimId(String activityID) throws Exception {
    try {
      String chargeId = activityID.split("-")[1];
      if (chargeId != null && !chargeId.equals("") && chargeId.startsWith("CH")) {
        return DataBaseUtil.getStringValueFromDb(CHARGE_CLAIM_ID, chargeId);
      }
    } catch (Exception exception) {
      logger.debug("Invalid Activity ID :" + activityID);
      return "Invalid Activity ID";
    }
    return null;
  }

  /**
   * Gets the bill activities.
   *
   * @param billNo
   *          the bill no
   * @return the bill activities
   * @throws Exception
   *           the exception
   */
  private Map getBillActivities(String billNo) throws Exception {

    ArrayList<String> billActivityIDs = new ArrayList<String>();
    Map chargesMap = new HashMap();
    ArrayList<RemittanceAdviceActivity> billActivities = new ArrayList<RemittanceAdviceActivity>();

    Iterator rowIterator = sheet.rowIterator();
    HSSFRow row = null;
    while (rowIterator.hasNext()) {
      row = (HSSFRow) rowIterator.next();

      int lineNumber = row.getRowNum() + 1;

      logger.debug("Reading charge values from line number............" + lineNumber);

      if (row.getRowNum() <= headerRowIndex) {
        continue;
      }

      if (row == null) {
        continue;
      }

      String rowBillNo = getRowBillNo(row);

      if (rowBillNo == null || rowBillNo.equals("") || !rowBillNo.equals(billNo)) {
        continue;
      }

      String serviceName = getRowServiceName(row);
      if (serviceName == null || serviceName.equals("")) {
        chargesMap.put("error",
            "Invalid/No Service Name :" + serviceName + " found at Line number...." + lineNumber);
        return chargesMap;
      }

      String denialRemarks = getRowDenialRemarks(row);

      BigDecimal paymentAmount = BigDecimal.ZERO;
      BigDecimal insuranceClaimAmount = BigDecimal.ZERO;
      java.sql.Date postedDate = null;

      HSSFCell insuranceClaimAmountCell = row.getCell(chargeInsuranceClaimAmountCellIndex);
      if (insuranceClaimAmountCell != null) {
        Object cellVal = getCellValue(insuranceClaimAmountCell);

        if (cellVal != null) {
          try {
            insuranceClaimAmount = new BigDecimal(cellVal.toString());
          } catch (Exception exception) {
            logger.debug("Error while Converting insurance claim amount at line number "
                + lineNumber + "... " + exception.getMessage());
            chargesMap.put("error",
                "Error while Converting insurance claim amount at line number " + lineNumber);
            return chargesMap;
          }
        } else {
          insuranceClaimAmount = BigDecimal.ZERO;
        }
      }

      HSSFCell amountCell = row.getCell(amountCellIndex);
      if (amountCell != null) {
        Object cellVal = getCellValue(amountCell);

        if (cellVal != null) {
          try {
            paymentAmount = new BigDecimal(cellVal.toString());
          } catch (Exception exception) {
            logger.debug("Error while Converting amount at line number " + lineNumber + "... "
                + exception.getMessage());
            chargesMap.put("error", "Error while Converting amount at line number " + lineNumber);
            return chargesMap;
          }
        } else {
          paymentAmount = BigDecimal.ZERO;
        }
      }

      HSSFCell postedDateCell = row.getCell(servicePostedDateCellIndex);
      if (postedDateCell != null) {
        Object cellVal = getCellValue(postedDateCell);

        if (cellVal != null) {
          try {
            if (cellVal.getClass() == java.lang.String.class) {
              java.util.Date date = sheetDateFormatter.parse((String) cellVal);
              postedDate = new java.sql.Date(date.getTime());
            } else {
              Double cellValue = (Double) cellVal;
              java.util.Date date = (java.util.Date) HSSFDateUtil.getJavaDate(cellValue);
              postedDate = new java.sql.Date(date.getTime());
            }
          } catch (Exception exception) {
            logger.debug("Error while Converting posted/service date at line number " + lineNumber
                + "... " + exception.getMessage());
            chargesMap.put("error", "Error while Converting posted/service date at line number."
                + " Date format needed : dd/MM/yyyy hh24:mi " + lineNumber);
            return chargesMap;
          }
        }
      }

      List<BasicDynaBean> billactivities = claimdao.findChargesByDescription(billNo, serviceName);

      if (billactivities == null || billactivities.size() == 0) {
        chargesMap.put("error", "Invalid/No Service Name :" + serviceName + " found. Bill No: "
            + billNo + " at Line number...." + lineNumber);
        return chargesMap;
      }

      if (billactivities != null && billactivities.size() > 0) {

        BasicDynaBean chrg = null;
        String activityID = null;

        // Duplicate activities
        if (billactivities.size() > 1) {

          for (BasicDynaBean charge : billactivities) {

            String actDescription = (String) charge.get("act_description");
            BigDecimal insuranceclaimAmount = (BigDecimal) charge.get("insurance_claim_amt");
            java.util.Date dt = dateFormatter.parse((String) charge.get("posted_date"));
            Date posteddate = new java.sql.Date(dt.getTime());
            String activityid = activityID;

            if (actDescription.equals(serviceName) && !billActivityIDs.contains(activityid)
                && (posteddate.compareTo(postedDate) == 0
                    || insuranceclaimAmount.compareTo(insuranceclaimAmount) == 0)) {
              chrg = charge;
              activityid = (String) chrg.get("activity_charge_id");
              billActivityIDs.add(activityid);
              break;
            }
          }

        } else {
          chrg = billactivities.get(0);
        }

        if (chrg == null) {
          chargesMap.put("error",
              "Invalid/No Service Name :" + serviceName + " found (Empty item bean). Bill No: "
                  + billNo + " at Line number...." + lineNumber);
          return chargesMap;
        }

        activityID = (String) chrg.get("activity_charge_id");

        if (activityID != null && !activityID.equals("") && !billActivityIDs.contains(activityID)) {

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

          billActivities.add(activity);
        }
      }
    } // while

    chargesMap.put("RemAdviceBillActivities", billActivities);
    return chargesMap;
  }

  /**
   * Gets the row service name.
   *
   * @param row
   *          the row
   * @return the row service name
   */
  private String getRowServiceName(HSSFRow row) {
    String serviceName = null;
    HSSFCell serviceNameCell = row.getCell(serviceNameCellIndex);
    if (serviceNameCell != null) {
      Object cellVal = getCellValue(serviceNameCell);
      if (cellVal != null) {
        serviceName = new String(cellVal.toString());
      }
    }
    return serviceName;
  }

  /**
   * Gets the row denial remarks.
   *
   * @param row
   *          the row
   * @return the row denial remarks
   */
  private String getRowDenialRemarks(HSSFRow row) {
    String denialRemarks = null;
    HSSFCell denialRemarksCell = row.getCell(denialRemarksCellIndex);
    if (denialRemarksCell != null) {
      if (denialRemarksCell.getStringCellValue() != null) {
        denialRemarksCell.setCellType(HSSFCell.CELL_TYPE_STRING);
        denialRemarks = denialRemarksCell.getStringCellValue();
      }
    }
    return denialRemarks;
  }

  /**
   * Sets the bill activities.
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
   * @param chargesMap
   *          the charges map
   * @throws Exception
   *           the exception
   */
  private void setBillActivities(RemittanceAdvice remAdvice, String billNo, String idPayer,
      String paymentReference, ArrayList<String> claimIds, Map chargesMap) throws Exception {

    ArrayList<RemittanceAdviceActivity> billActivities = 
        (ArrayList<RemittanceAdviceActivity>) chargesMap
        .get("RemAdviceBillActivities");

    BasicDynaBean bill = billdao.findByKey("bill_no", billNo);
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
        throw new Exception("Remittance Advice claim is not found");
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
   * Gets the cell value.
   *
   * @param cell
   *          the cell
   * @return the cell value
   */
  private Object getCellValue(HSSFCell cell) {

    Object cellVal = null;
    switch (cell.getCellType()) {

      case HSSFCell.CELL_TYPE_BLANK: {
        cellVal = null;
        break;
      }
      case HSSFCell.CELL_TYPE_STRING: {
        cellVal = cell.getStringCellValue().trim();
        break;
      }
      case HSSFCell.CELL_TYPE_NUMERIC: {
        cellVal = cell.getNumericCellValue();
        break;
      }
      default: {
        break;
      }
    }
    return cellVal;
  }

  /**
   * Gets the header row.
   *
   * @param rowIterator
   *          the row iterator
   * @return the header row
   */
  private HSSFRow getHeaderRow(Iterator rowIterator) {
    HSSFRow row1 = null;
    while (rowIterator.hasNext()) {
      HSSFRow row = (HSSFRow) rowIterator.next();
      if (row != null) {
        HSSFCell claimCell = row.getCell(0);
        if (claimCell != null && claimCell.getStringCellValue() != null) {
          row1 = row;
          break;
        }
      }
    }
    return row1;
  }

  /**
   * Gets the claim activities.
   *
   * @param claimID
   *          the claim ID
   * @return the claim activities
   * @throws Exception
   *           the exception
   */
  private Map getClaimActivities(String claimID) throws Exception {

    Map activityMap = new HashMap();
    ArrayList<RemittanceAdviceActivity> remAdviceActivities = 
        new ArrayList<RemittanceAdviceActivity>();

    Iterator rowIterator = sheet.rowIterator();
    HSSFRow row = null;
    while (rowIterator.hasNext()) {

      row = (HSSFRow) rowIterator.next();

      int lineNumber = row.getRowNum() + 1;

      logger.debug("Reading claim activity values from line number............" + lineNumber);

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

      String rowClaimID = getChargeClaimId(activityID);

      if (claimID == null || claimID.equals("")) {
        activityMap.put("error", "Invalid Activity ID. Activity Bill Claim ID is Invalid :"
            + activityID + " found at Line number...." + lineNumber);
        return activityMap;
      } else if (claimID.startsWith("Invalid")) {
        activityMap.put("error",
            claimID + " : " + activityID + " found at Line number...." + lineNumber);
        return activityMap;
      }

      if (rowClaimID != null && rowClaimID.equals(claimID)) {

        BigDecimal paymentAmount = BigDecimal.ZERO;
        HSSFCell amountCell = row.getCell(amountCellIndex);
        if (amountCell != null) {
          Object cellVal = getCellValue(amountCell);

          if (cellVal != null) {
            try {
              paymentAmount = new BigDecimal(cellVal.toString());
            } catch (Exception exception) {
              logger.debug("Error while Converting amount at line number " + lineNumber + "... "
                  + exception.getMessage());
              activityMap.put("error",
                  "Error while Converting amount at line number " + lineNumber);
              return activityMap;
            }
          } else {
            paymentAmount = BigDecimal.ZERO;
          }
        }

        String denialRemarks = getRowDenialRemarks(row);

        if (activityID != null && !activityID.equals("")) {
          BigDecimal insuranceClaimAmount = BigDecimal.ZERO;
          final String chargeType = activityID.split("-")[0];
          String chargeId = activityID.split("-")[1];

          BasicDynaBean chargeBean = chargedao.findByKey("charge_id", chargeId);
          BasicDynaBean billBean = billdao.findByKey("bill_no", chargeBean.get("bill_no"));
          BasicDynaBean visitBean = visitdao.findByKey("patient_id", billBean.get("visit_id"));
          String tpaId = (visitBean != null && visitBean.get("primary_sponsor_id") != null)
              ? (String) visitBean.get("primary_sponsor_id")
              : "";
          int centerId = (visitBean != null && visitBean.get("center_id") != null)
              ? (Integer) visitBean.get("center_id")
              : 0;

          if (this.centerName != null && this.centerId != centerId) {
            String centerName = (String) ((BasicDynaBean) new CenterMasterDAO()
                .findByKey("center_id", centerId)).get("center_name");
            activityMap.put("error",
                "Center selected (" + this.centerName + ") is not same as the visit center ("
                    + centerName + ") at Line number " + lineNumber);
            return activityMap;
          }

          if (!this.tpaId.equals(tpaId)) {
            String tpaName = (String) ((BasicDynaBean) new TpaMasterDAO().findByKey("tpa_id",
                tpaId)).get("tpa_name");
            activityMap.put("error", "TPA selected (" + this.tpaName
                + ") is not same as the bill TPA (" + tpaName + ") at Line number " + lineNumber);
            return activityMap;
          }

          int accountGroup = (billBean != null && billBean.get("account_group") != null)
              ? (Integer) billBean.get("account_group")
              : 0;
          if (this.accountGroupName != null && this.accountGroup != accountGroup) {
            String accountGroupName = (String) ((BasicDynaBean) new AccountingGroupMasterDAO()
                .findByKey("account_group_id", accountGroup)).get("account_group_name");
            activityMap.put("error",
                "Account Group selected (" + this.accountGroupName
                    + ") is not same as the bill account group (" + accountGroupName
                    + ") at Line number " + lineNumber);
            return activityMap;
          }

          if (chargeType != null && !chargeType.equals("") && chargeType.startsWith("P")) {
            String saleItemId = activityID.split("-")[2];
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

    activityMap.put("RemAdviceActivities", remAdviceActivities);
    return activityMap;
  }

  /**
   * Gets the report desc from sheet bill level.
   *
   * @param selectedValues
   *          the selected values
   * @param workBook
   *          the work book
   * @param selectedSheetNo
   *          the selected sheet no
   * @return the report desc from sheet bill level
   * @throws Exception
   *           the exception
   */
  public Map getReportDescFromSheetBillLevel(Map selectedValues, HSSFWorkbook workBook,
      int selectedSheetNo) throws Exception {

    Map descMap = validateRequiredColumnsInWorkSheet(selectedValues, workBook, selectedSheetNo);

    if (descMap != null && descMap.get("error") != null && !descMap.get("error").equals("")) {
      return descMap;
    }

    Iterator rowIterator = sheet.rowIterator();

    RemittanceAdvice remAdvice = new RemittanceAdvice(); // Initializes header and claims list

    ArrayList<String> claimIds = new ArrayList<String>();
    ArrayList<String> billNos = new ArrayList<String>();

    // Iterate rows to fetch the activities and create RemittanceAdviceClaim

    HSSFRow row = null;
    while (rowIterator.hasNext()) {
      row = (HSSFRow) rowIterator.next();
      int lineNumber = row.getRowNum() + 1;

      logger.debug("Reading Values from line number............" + lineNumber);

      if (row.getRowNum() <= headerRowIndex) {
        continue;
      }

      if (row == null) {
        continue;
      }

      String billNo = getRowBillNo(row);

      if (billNo == null || billNo.equals("") || billNos.contains(billNo)) {
        continue;
      }

      String idPayer = getRowIdPayer(row);

      idPayer = getRowIdPayer(row);
      if (payerIdCellIndex != 0) {
        if (idPayer == null || idPayer.equals("")) {
          descMap.put("error",
              "Invalid ID Payer :" + idPayer + " found at Line number...." + lineNumber);
          return descMap;
        }
      } else {
        idPayer = new String(
            DataBaseUtil.getNextSequence("insurance_claim_payers_reference_no_seq") + "");
      }

      String itemPaymentReference = getPaymentReference(row);
      if (itemPaymentReference == null || itemPaymentReference.equals("")) {
        descMap.put("error", "Invalid Payment Reference :" + itemPaymentReference
            + " found at Line number...." + lineNumber);
        return descMap;
      }

      if (itemPaymentReference.length() > 50) {
        descMap.put("error", "Payment Reference is more than 50 characters :" + itemPaymentReference
            + " found at Line number...." + lineNumber);
        return descMap;
      }

      if (billNo != null && !billNo.equals("") && !billNos.contains(billNo)) {

        BasicDynaBean billBean = billdao.findByKey("bill_no", billNo);
        BasicDynaBean billClaimBean = new BillClaimDAO().getPrimaryBillClaim(billNo);
        final String claimID = billClaimBean.get("claim_id") != null
            ? (String) billClaimBean.get("claim_id")
            : null;

        BasicDynaBean visitBean = visitdao.findByKey("patient_id", billBean.get("visit_id"));
        String tpaId = (visitBean != null && visitBean.get("primary_sponsor_id") != null)
            ? (String) visitBean.get("primary_sponsor_id")
            : "";
        int centerId = (visitBean != null && visitBean.get("center_id") != null)
            ? (Integer) visitBean.get("center_id")
            : 0;

        if (this.centerName != null && this.centerId != centerId) {
          String centerName = (String) ((BasicDynaBean) new CenterMasterDAO().findByKey("center_id",
              centerId)).get("center_name");
          descMap.put("error",
              "Center selected (" + this.centerName + ") is not same as the visit center ("
                  + centerName + ") at Line number " + lineNumber);
          return descMap;
        }

        if (!this.tpaId.equals(tpaId)) {
          String tpaName = (String) ((BasicDynaBean) new TpaMasterDAO().findByKey("tpa_id", tpaId))
              .get("tpa_name");
          descMap.put("error", "TPA selected (" + this.tpaName + ") is not same as the bill TPA ("
              + tpaName + ") at Line number " + lineNumber);
          return descMap;
        }

        int accountGroup = (billBean != null && billBean.get("account_group") != null)
            ? (Integer) billBean.get("account_group")
            : 0;
        if (this.accountGroupName != null && this.accountGroup != accountGroup) {
          String accountGroupName = (String) ((BasicDynaBean) new AccountingGroupMasterDAO()
              .findByKey("account_group_id", accountGroup)).get("account_group_name");
          descMap.put("error",
              "Account Group selected (" + this.accountGroupName
                  + ") is not same as the bill account group (" + accountGroupName
                  + ") at Line number " + lineNumber);
          return descMap;
        }

        if (claimID != null && !claimID.equals("") && !claimIds.contains(claimID)) {

          if (claimID == null || claimID.equals("")) {
            descMap.put("error", "Invalid Bill. Bill Claim ID is Invalid :" + billNo
                + " found at Line number...." + lineNumber);
            return descMap;
          }

          BasicDynaBean claimBean = claimdao.findClaimById(claimID);
          int resubmissionCount = claimBean.get("resubmission_count") != null
              ? (Integer) claimBean.get("resubmission_count")
              : 0;
          String isResubmission = resubmissionCount > 0 ? "Y" : "N";
          BasicDynaBean batchBean = submitdao.getLatestSubmissionBatch(claimID, isResubmission);
          String batchStatus = (batchBean != null && batchBean.get("status") != null)
              ? (String) batchBean.get("status")
              : null;
          String batchId = (batchBean != null && batchBean.get("submission_batch_id") != null)
              ? (String) batchBean.get("submission_batch_id")
              : null;

          if (batchBean == null || batchId == null) {
            descMap.put("error",
                "Invalid Claim ID " + claimID
                    + ". No Submission Batch ID found for the Claim found at Line number...."
                    + lineNumber);
            return descMap;
          }

          if (batchStatus == null || !batchStatus.equals("S")) {
            descMap.put("error", "Submission Batch " + batchId
                + " is not marked as Sent. Please mark it as Sent and Upload again.");
            return descMap;
          }

          RemittanceAdviceClaim claim = new RemittanceAdviceClaim();
          claim.setClaimID(claimID);
          claim.setIdPayer(idPayer);
          claim.setPaymentReference(itemPaymentReference);

          claimIds.add(claimID);

          Map claimMap = getClaimBills(claimID);

          if (claimMap.get("error") != null && !claimMap.get("error").equals("")) {
            descMap.put("error", claimMap.get("error"));
            return descMap;
          } else {
            ArrayList<RemittanceAdviceBill> remBills = (ArrayList<RemittanceAdviceBill>) claimMap
                .get("RemAdviceBills");

            claim.setBills(remBills);

            remAdvice.addClaim(claim);
          }
        }
      }
    }

    descMap.put("RemittanceDesc", remAdvice);
    return descMap;
  }

  /**
   * Gets the claim bills.
   *
   * @param claimID
   *          the claim ID
   * @return the claim bills
   * @throws Exception
   *           the exception
   */
  private Map getClaimBills(String claimID) throws Exception {

    ArrayList<String> bills = new ArrayList<String>();
    Map claimMap = new HashMap();
    ArrayList<RemittanceAdviceBill> remBills = new ArrayList<RemittanceAdviceBill>();

    Iterator rowIterator = sheet.rowIterator();
    HSSFRow row = null;
    while (rowIterator.hasNext()) {
      row = (HSSFRow) rowIterator.next();

      int lineNumber = row.getRowNum() + 1;

      logger.debug("Reading bill from line number............" + lineNumber);

      if (row.getRowNum() <= headerRowIndex) {
        continue;
      }

      if (row == null) {
        continue;
      }

      String rowBillNo = getRowBillNo(row);

      if (rowBillNo == null || rowBillNo.equals("")) {
        continue;
      }

      String rowClaimID = null;
      BigDecimal billInsuranceClaimAmount = BigDecimal.ZERO;
      BasicDynaBean bill = null;
      if (rowBillNo != null && !rowBillNo.equals("")) {

        bill = billdao.findByKey("bill_no", rowBillNo);
        BasicDynaBean billClaimBean = new BillClaimDAO().getPrimaryBillClaim(rowBillNo);
        rowClaimID = billClaimBean.get("claim_id") != null ? (String) billClaimBean.get("claim_id")
            : null;

        if (rowClaimID == null || rowClaimID.equals("")) {
          claimMap.put("error", "Invalid Bill. Bill Claim ID is Invalid :" + rowBillNo
              + " found at Line number...." + lineNumber);
          return claimMap;
        }

        if (!rowClaimID.equals(claimID)) {
          continue;
        }
      }

      String itemPaymentReference = getPaymentReference(row);
      if (itemPaymentReference == null || itemPaymentReference.equals("")) {
        claimMap.put("error", "Invalid Payment Reference :" + itemPaymentReference
            + " found at Line number...." + lineNumber);
        return claimMap;
      }

      if (itemPaymentReference.length() > 50) {
        claimMap.put("error", "Payment Reference is more than 50 characters :"
            + itemPaymentReference + " found at Line number...." + lineNumber);
        return claimMap;
      }

      String denialRemarks = getRowDenialRemarks(row);

      BigDecimal paymentAmount = BigDecimal.ZERO;

      HSSFCell amountCell = row.getCell(amountCellIndex);
      if (amountCell != null) {
        Object cellVal = getCellValue(amountCell);

        if (cellVal != null) {
          try {
            paymentAmount = new BigDecimal(cellVal.toString());
          } catch (Exception exception) {
            logger.debug("Error while Converting amount at line number " + lineNumber + "... "
                + exception.getMessage());
            claimMap.put("error", "Error while Converting amount at line number " + lineNumber);
            return claimMap;
          }
        } else {
          paymentAmount = BigDecimal.ZERO;
        }
      }

      if (rowBillNo != null && !rowBillNo.equals("") && !bills.contains(rowBillNo)) {
        if (bill == null) {
          throw new Exception("Bill is not found for bill number " + rowBillNo);
        }

        billInsuranceClaimAmount = (BigDecimal) bill.get("total_claim");
        RemittanceAdviceBill remBill = new RemittanceAdviceBill();

        remBill.setBillNo(rowBillNo);
        remBill.setPaymentAmount(paymentAmount);
        remBill.setPaymentReference(itemPaymentReference);

        if (billInsuranceClaimAmount.compareTo(paymentAmount) != 0) {
          remBill.setDenialRemarks((denialRemarks == null)
              ? "Denied as the claim total amount and payment amount is not equal."
              : denialRemarks);
        } else {
          remBill.setDenialRemarks(null);
        }
        remBills.add(remBill);
      }
    } // while

    claimMap.put("RemAdviceBills", remBills);
    return claimMap;
  }
}
