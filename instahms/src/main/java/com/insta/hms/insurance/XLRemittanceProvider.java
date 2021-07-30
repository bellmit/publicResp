package com.insta.hms.insurance;

import com.bob.hms.common.DataBaseUtil;
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
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The Class XLRemittanceProvider.
 */
public abstract class XLRemittanceProvider {

  /** The Constant MAX_PROCESSED_COLUMNS. */
  private static final int MAX_PROCESSED_COLUMNS = 50;

  /** The payer id cell index. */
  protected int payerIdCellIndex = 0;

  /** The payment ref cell index. */
  protected int paymentRefCellIndex = 0;

  /** The payment reference. */
  protected String paymentReference = null;

  /** The bill no cell index. */
  protected int billNoCellIndex = 0;

  /** The claim id index. */
  protected int claimIdIndex = 0;

  /** The denial remarks cell index. */
  protected int denialRemarksCellIndex = 0;

  /** The amount cell index. */
  protected int amountCellIndex = 0;

  /** The sheet. */
  protected HSSFSheet sheet = null;

  /** The claimdao. */
  protected ClaimDAO claimdao = new ClaimDAO();

  /** The submitdao. */
  protected ClaimSubmissionDAO submitdao = new ClaimSubmissionDAO();

  /** The visitdao. */
  protected GenericDAO visitdao = new GenericDAO("patient_registration");

  /** The billdao. */
  protected GenericDAO billdao = new GenericDAO("bill");

  /** The center name. */
  protected String centerName = null;

  /** The center id. */
  protected int centerId = 0;

  /** The account group. */
  protected int accountGroup = 0;

  /** The account group name. */
  protected String accountGroupName = null;

  /** The tpa id. */
  protected String tpaId = null;

  /** The tpa name. */
  protected String tpaName = null;

  /** The header row index. */
  protected int headerRowIndex = 0;

  /** The item indentification. */
  protected String itemIndentification = null;

  /** The detail level. */
  protected String detailLevel = null;

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(XLRemittanceProvider.class);

  /**
   * Gets the remittance advice.
   *
   * @param selectedValues  the selected values
   * @param workBook        the work book
   * @param selectedSheetNo the selected sheet no
   * @param errorMap        the error map
   * @return the remittance advice
   * @throws Exception the exception
   */
  // Strategy method
  public RemittanceAdvice getRemittanceAdvice(Map selectedValues, HSSFWorkbook workBook,
      int selectedSheetNo, Map errorMap) throws Exception {

    boolean dataValid = validateSheet(selectedValues, workBook, selectedSheetNo, errorMap);

    if (!dataValid) {
      return null;
    }

    Iterator rowIterator = sheet.rowIterator();
    RemittanceAdvice remAdvice = new RemittanceAdvice(); // Initializes header and claims list
    List<String> claimIds = new ArrayList<String>();
    List<String> billNos = new ArrayList<String>();

    HSSFRow row = null;
    while (rowIterator.hasNext()) {
      row = (HSSFRow) rowIterator.next();
      logger.debug("Reading Values from line number............" + getLineNumber(row));
      if (skipRow(row, claimIds, billNos)) {
        continue;
      }
      remAdvice = processRow(remAdvice, row, errorMap, claimIds, billNos);
      if (null == remAdvice) {
        return null;
      }
    }

    return remAdvice;
  }

  /**
   * Skip row.
   *
   * @param row      the row
   * @param claimIds the claim ids
   * @param billNos  the bill nos
   * @return true, if successful
   */
  // Hook for the subclasses, in case they want to skip processing some rows
  public boolean skipRow(HSSFRow row, List<String> claimIds, List<String> billNos) {
    return false;
  }

  /**
   * Process row.
   *
   * @param remAdvice the rem advice
   * @param row       the row
   * @param errorMap  the error map
   * @param claimIds  the claim ids
   * @param billNos   the bill nos
   * @return the remittance advice
   * @throws Exception the exception
   */
  public abstract RemittanceAdvice processRow(RemittanceAdvice remAdvice, HSSFRow row, Map errorMap,
      List<String> claimIds, List<String> billNos) throws Exception;

  /**
   * Gets the row id payer.
   *
   * @param row the row
   * @return the row id payer
   */
  protected String getRowIdPayer(HSSFRow row) {
    return extractString(row, payerIdCellIndex);
  }

  /**
   * Gets the payment reference.
   *
   * @param row the row
   * @return the payment reference
   */
  protected String getPaymentReference(HSSFRow row) {
    String itemPaymentReference = paymentReference;
    if (paymentReference == null || paymentReference.equals("")) {
      itemPaymentReference = extractString(row, paymentRefCellIndex);
    }
    return itemPaymentReference;
  }

  /**
   * Gets the row bill no.
   *
   * @param row the row
   * @return the row bill no
   */
  protected String getRowBillNo(HSSFRow row) {
    return extractString(row, billNoCellIndex, false);
  }

  /**
   * Gets the row claim id.
   *
   * @param row the row
   * @return the row claim id
   */
  protected String getRowClaimId(HSSFRow row) {
    return extractString(row, claimIdIndex, false);
  }

  /**
   * Gets the column index map.
   *
   * @param headerRow      the header row
   * @param selectedValues the selected values
   * @return the column index map
   */
  protected Map<String, Integer> getColumnIndexMap(HSSFRow headerRow, Map selectedValues) {
    Map<String, Integer> columnIndexMap = new HashMap<String, Integer>();
    Set<String> columnkeys = selectedValues.keySet();

    // Assume that there are 50 columns in the spreadsheet.
    for (int i = 0; i < MAX_PROCESSED_COLUMNS; i++) {
      HSSFCell cell = headerRow.getCell(i);

      if (cell != null && cell.getStringCellValue() != null) {
        String header = cell.getStringCellValue().trim().toLowerCase();

        for (String key : columnkeys) {

          if (null != selectedValues.get(key)
              && ((String) selectedValues.get(key)).trim().toLowerCase().equals(header)) {
            columnIndexMap.put(key, i);
          }
        }
      }
    }

    return columnIndexMap;
  }

  /**
   * Gets the required columns.
   *
   * @return the required columns
   */
  public String[] getRequiredColumns() {
    return new String[] { "denial_remarks_heading", "amount_heading", "claim_id_heading" };
  }

  /**
   * Gets the id payer.
   *
   * @param row      the row
   * @param errorMap the error map
   * @return the id payer
   * @throws SQLException the SQL exception
   */
  public String getIdPayer(HSSFRow row, Map errorMap) throws SQLException {
    String idPayer = getRowIdPayer(row);
    if (payerIdCellIndex != 0) {
      if (idPayer == null || idPayer.equals("")) {
        errorMap.put("error",
            "Invalid ID Payer :" + idPayer + " found at Line number...." + getLineNumber(row));
        return null;
      }
    } else {
      idPayer = new String(
          DataBaseUtil.getNextSequence("insurance_claim_payers_reference_no_seq") + "");
    }
    return idPayer;
  }

  /**
   * Gets the payment amount.
   *
   * @param row      the row
   * @param errorMap the error map
   * @return the payment amount
   */
  public BigDecimal getPaymentAmount(HSSFRow row, Map errorMap) {
    return extractBigDecimal(row, amountCellIndex, "amount", errorMap);
  }

  /**
   * Gets the row denial remarks.
   *
   * @param row the row
   * @return the row denial remarks
   */
  protected String getRowDenialRemarks(HSSFRow row) {
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
   * Initialize column indices.
   *
   * @param columnIndexMap the column index map
   */
  protected void initializeColumnIndices(Map<String, Integer> columnIndexMap) {
    this.amountCellIndex = columnIndexMap.get("amount_heading");
    this.payerIdCellIndex = (columnIndexMap.get("payer_id_heading") != null)
        ? columnIndexMap.get("payer_id_heading")
        : 0;
    this.denialRemarksCellIndex = columnIndexMap.get("denial_remarks_heading");
    if (paymentReference == null || paymentReference.equals("")) {
      if (null != columnIndexMap.get("payment_reference_heading")) {
        this.paymentRefCellIndex = columnIndexMap.get("payment_reference_heading");
      }
    }
    if (null != columnIndexMap.get("claim_id_heading")) {
      this.claimIdIndex = columnIndexMap.get("claim_id_heading");
    }
  }

  /**
   * Validate required columns.
   *
   * @param columnIndexMap the column index map
   * @param errorMap       the error map
   * @return true, if successful
   */
  protected boolean validateRequiredColumns(Map<String, Integer> columnIndexMap, Map errorMap) {
    boolean valid = false;
    String[] requiredColumns = getRequiredColumns();
    for (String requiredColumn : requiredColumns) {
      valid = validateRequiredColumn(columnIndexMap, requiredColumn, errorMap);
      if (!valid) {
        return false;
      }
    }

    if (paymentReference == null || paymentReference.equals("")) {
      valid = validateRequiredColumn(columnIndexMap, "payment_reference_heading", errorMap);
      if (!valid) {
        return false;
      }
    }
    return true;
  }

  /**
   * Validate required column.
   *
   * @param columnIndexMap the column index map
   * @param requiredColumn the required column
   * @param errorMap       the error map
   * @return true, if successful
   */
  private boolean validateRequiredColumn(Map<String, Integer> columnIndexMap, String requiredColumn,
      Map errorMap) {
    if (columnIndexMap.get(requiredColumn) == null) {
      errorMap.put("error", "Column Not Found: " + requiredColumn);
      return false;
    }
    return true;
  }

  /**
   * Validate payment reference.
   *
   * @param row                  the row
   * @param itemPaymentReference the item payment reference
   * @param errorMap             the error map
   * @return true, if successful
   */
  public boolean validatePaymentReference(HSSFRow row, String itemPaymentReference, Map errorMap) {
    if (itemPaymentReference == null || itemPaymentReference.equals("")) {
      errorMap.put("error", "Invalid Payment Reference :" + itemPaymentReference
          + " found at Line number...." + getLineNumber(row));
      return false;
    }

    if (itemPaymentReference.length() > 50) {
      errorMap.put("error", "Payment Reference is more than 50 characters :" + itemPaymentReference
          + " found at Line number...." + getLineNumber(row));
      return false;
    }
    return true;
  }

  /**
   * Validate claim parameters.
   *
   * @param row      the row
   * @param claimID  the claim ID
   * @param errorMap the error map
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public boolean validateClaimParameters(HSSFRow row, String claimID, Map errorMap)
      throws SQLException {

    if (claimID == null || claimID.equals("")) {
      errorMap.put("error", "Invalid Bill. Bill Claim ID is Invalid :" + claimID
          + " found at Line number...." + getLineNumber(row));
      return false;
    }
    int resubmissionCount = 0;
    BasicDynaBean claimBean = claimdao.findClaimById(claimID);
    if (null != claimBean) {
      resubmissionCount = claimBean.get("resubmission_count") != null
          ? (Integer) claimBean.get("resubmission_count")
          : 0;
    } else {
      errorMap.put("error",
          "Invalid Claim ID " + claimID
              + ". No Submission Batch ID found for the Claim found at Line number...."
              + getLineNumber(row));
      return false;
    }
    String isResubmission = resubmissionCount > 0 ? "Y" : "N";
    BasicDynaBean batchBean = submitdao.getLatestSubmissionBatch(claimID, isResubmission);
    String batchStatus = (batchBean != null && batchBean.get("status") != null)
        ? (String) batchBean.get("status")
        : null;
    String batchId = (batchBean != null && batchBean.get("submission_batch_id") != null)
        ? (String) batchBean.get("submission_batch_id")
        : null;

    if (batchBean == null || batchId == null) {
      errorMap.put("error",
          "Invalid Claim ID " + claimID
              + ". No Submission Batch ID found for the Claim found at Line number...."
              + getLineNumber(row));
      return false;
    }

    if (batchStatus == null || !batchStatus.equals("S")) {
      errorMap.put("error", "Submission Batch " + batchId
          + " is not marked as Sent. Please mark it as Sent and Upload again.");
      return false;
    }
    return true;

  }

  /**
   * Validate remittance parameters.
   *
   * @param row      the row
   * @param billNo   the bill no
   * @param errorMap the error map
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public boolean validateRemittanceParameters(HSSFRow row, String billNo, Map errorMap)
      throws SQLException {

    BasicDynaBean billBean = billdao.findByKey("bill_no", billNo);
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
      errorMap.put("error",
          "Center selected (" + this.centerName + ") is not same as the visit center (" + centerName
              + ") at Line number " + getLineNumber(row));
      return false;
    }

    if (!this.tpaId.equals(tpaId)) {
      String tpaName = "";
      if (null != tpaId && !tpaId.equals("")) {
        tpaName = (String) ((BasicDynaBean) new TpaMasterDAO().findByKey("tpa_id", tpaId))
            .get("tpa_name");
      }

      errorMap.put("error", "TPA selected (" + this.tpaName + ") is not same as the bill TPA ("
          + tpaName + ") at Line number " + getLineNumber(row));
      return false;
    }

    int accountGroup = (billBean != null && billBean.get("account_group") != null)
        ? (Integer) billBean.get("account_group")
        : 0;
    if (this.accountGroupName != null && this.accountGroup != accountGroup) {
      String accountGroupName = (String) ((BasicDynaBean) new AccountingGroupMasterDAO()
          .findByKey("account_group_id", accountGroup)).get("account_group_name");
      errorMap.put("error",
          "Account Group selected (" + this.accountGroupName
              + ") is not same as the bill account group (" + accountGroupName + ") at Line number "
              + getLineNumber(row));
      return false;
    }
    return true;
  }

  /**
   * Validate sheet.
   *
   * @param selectedValues  the selected values
   * @param workBook        the work book
   * @param selectedSheetNo the selected sheet no
   * @param errorMap        the error map
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public boolean validateSheet(Map selectedValues, HSSFWorkbook workBook, int selectedSheetNo,
      Map errorMap) throws SQLException {

    // Map descMap = new HashMap();

    HSSFSheet sheet = getWorkBookSheet(workBook, selectedSheetNo, errorMap);
    if (null == sheet) {
      return false;
    }
    this.sheet = sheet;

    HSSFRow row1 = getHeaderRow(sheet, errorMap);
    if (null == row1) {
      errorMap.put("error", "Selected sheet number :" + (selectedSheetNo + 1)
          + " has first column empty. Please delete the column and upload again");
      return false;
    }

    this.headerRowIndex = row1.getRowNum();

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

    Map<String, Integer> columnIndexMap = getColumnIndexMap(row1, selectedValues);
    boolean validColumns = validateRequiredColumns(columnIndexMap, errorMap);
    if (validColumns) {
      initializeColumnIndices(columnIndexMap);
    }
    return validColumns;
  }

  /**
   * Extract date.
   *
   * @param row       the row
   * @param cellIndex the cell index
   * @param fmt       the fmt
   * @param colName   the col name
   * @param errorMap  the error map
   * @return the date
   */
  // XL Utility methods
  protected Date extractDate(HSSFRow row, int cellIndex, DateFormat fmt, String colName,
      Map errorMap) {
    java.sql.Date extractedDt = null;
    HSSFCell cell = row.getCell(cellIndex);
    if (cell != null) {
      Object cellVal = getCellValue(cell);

      if (cellVal != null) {
        try {
          if (cellVal.getClass() == java.lang.String.class) {
            java.util.Date formattedDt = fmt.parse((String) cellVal);
            extractedDt = new java.sql.Date(formattedDt.getTime());
          } else {
            Double cellValue = (Double) cellVal;
            java.util.Date date = (java.util.Date) HSSFDateUtil.getJavaDate(cellValue);
            extractedDt = new java.sql.Date(date.getTime());
          }
        } catch (Exception exception) {
          logger.debug("Error while Converting " + colName + " at line number " + getLineNumber(row)
              + "... " + exception.getMessage()); //
          errorMap.put("error", "Error while Converting" + colName + " at line number."
              + " Date format needed : dd/MM/yyyy hh24:mi " + getLineNumber(row));
          return null;
        }
      }
    }
    return extractedDt;
  }

  /**
   * Extract big decimal.
   *
   * @param row       the row
   * @param cellIndex the cell index
   * @param colName   the col name
   * @param errorMap  the error map
   * @return the big decimal
   */
  protected BigDecimal extractBigDecimal(HSSFRow row, int cellIndex, String colName, Map errorMap) {
    BigDecimal bigDecimal = BigDecimal.ZERO;
    HSSFCell cell = row.getCell(cellIndex);
    if (cell != null) {
      Object cellVal = getCellValue(cell);

      if (cellVal != null) {
        try {
          bigDecimal = new BigDecimal(cellVal.toString());
        } catch (Exception exception) {
          logger.debug("Error while Converting" + colName + "at line number " + getLineNumber(row)
              + "... " + exception.getMessage());
          errorMap.put("error",
              "Error while Converting amount at line number " + getLineNumber(row));
          return null;
        }
      } else {
        bigDecimal = BigDecimal.ZERO;
      }
    }
    return bigDecimal;
  }

  /**
   * Extract string.
   *
   * @param row            the row
   * @param cellIndex      the cell index
   * @param convertNumeric the convert numeric
   * @return the string
   */
  protected String extractString(HSSFRow row, int cellIndex, boolean convertNumeric) {
    String str = null;
    HSSFCell cell = row.getCell(cellIndex);
    if (cell != null) {
      Object cellVal = getCellValue(cell);
      if (cellVal != null) {
        if (convertNumeric) {
          if (cellVal.getClass() == java.lang.String.class) {
            str = new String(cellVal.toString());
          } else if (cellVal.getClass() == java.lang.Double.class) {
            str = new String(((Double) cellVal).intValue() + "");
          }
        } else {
          str = new String(cellVal.toString());
        }
      }
    }
    return str;
  }

  /**
   * Extract string.
   *
   * @param row       the row
   * @param cellIndex the cell index
   * @return the string
   */
  protected String extractString(HSSFRow row, int cellIndex) {
    return extractString(row, cellIndex, true);
  }

  /**
   * Gets the line number.
   *
   * @param row the row
   * @return the line number
   */
  protected int getLineNumber(HSSFRow row) {
    return (null != row) ? row.getRowNum() + 1 : 1;
  }

  /**
   * Gets the header row.
   *
   * @param sheet    the sheet
   * @param errorMap the error map
   * @return the header row
   */
  protected HSSFRow getHeaderRow(HSSFSheet sheet, Map errorMap) {
    Iterator rowIterator = sheet.rowIterator();
    HSSFRow row = null;
    while (rowIterator.hasNext()) {
      HSSFRow hssfRow = (HSSFRow) rowIterator.next();
      if (hssfRow != null) {
        HSSFCell cell = hssfRow.getCell(0);
        if (cell != null && cell.getStringCellValue() != null) {
          row = hssfRow;
          break;
        }
      }
    }
    return row;
  }

  /**
   * Gets the cell value.
   *
   * @param cell the cell
   * @return the cell value
   */
  protected Object getCellValue(HSSFCell cell) {

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
   * Gets the work book sheet.
   *
   * @param workBook        the work book
   * @param selectedSheetNo the selected sheet no
   * @param errorMap        the error map
   * @return the work book sheet
   */
  protected HSSFSheet getWorkBookSheet(HSSFWorkbook workBook, int selectedSheetNo, Map errorMap) {
    int noOfSheets = workBook.getNumberOfSheets();

    logger.debug("Number of sheets in the Excel file...." + noOfSheets);

    HSSFSheet sheet = null;
    try {
      sheet = workBook.getSheetAt(selectedSheetNo);

    } catch (IllegalArgumentException ie) {
      errorMap.put("error", ie.getMessage());
      return null;
    }

    if (sheet == null) {
      errorMap.put("error",
          "Selected sheet number :" + (selectedSheetNo + 1) + " is not found in the Excel file");
      return null;
    }
    return sheet;

  }

}
