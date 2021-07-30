package com.insta.hms.common;

import com.bob.hms.common.AutoIncrementId;
import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;


import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * This class is used to upload excel sheet to db.
 * 
 * @author irshad
 *
 */

public class ExcelImporter {

  /** The audit log hint. */
  private static final String AUDIT_LOG_HINT = ":XLS";

  /** The main table name. */
  private String mainTableName;// Main table of insert

  /** The seq name. */
  // Set the sequence name for insert, if no seqName then pickup the seqName based on main table
  // name.
  private String seqName;

  /** The errors. */
  private StringBuilder errors;

  /** The column name for date. */
  private String columnNameForDate;// For audit logs update of date

  /** The column name for user. */
  private String columnNameForUser;// For audit log update of user

  /** The is date required. */
  private boolean isDateRequired;// boolean for audit logs update of date

  /** The is user name required. */
  private boolean isUserNameRequired;// boolean for audit log update of user

  /** The use audit log hint. */
  private boolean useAuditLogHint;// boolean for audit log hint to add or not.

  /** The id. */
  private String id;// primary key of main table

  /** The is composite primary key. */
  private boolean isCompositePrimaryKey = false;// If Excel sheet contains Composite key to update.

  /** The composite primary keys. */
  // If Excel sheet contains Composite key to update and set the keys.
  private List<String> compositePrimaryKeys = new ArrayList<String>();

  /** The no of rows skip. */
  private int noOfRowsSkip = 0;// used to skip the rows based on excel sheet.

  /** The sheet name. */
  private String sheetName;

  /** The is insert enable. */
  private boolean isInsertEnable = false;

  /** The column type. */
  private String columnType = "";// Used for custom auto increment generation.

  /** The column name. */
  private String columnName = "";// Used for custom auto increment generation.

  /**
   * Instantiates a new excel importer.
   *
   * @param mainTableName This contains the table name in which we are going to manipulate the data.
   * @param seqName       This contains the sequence name used to insert the data into the table.
   */
  public ExcelImporter(String mainTableName, String seqName) {

    this.mainTableName = mainTableName;
    if (seqName != null) {
      if (seqName.contains("custom_unique_key")) {
        String[] seqInfo = seqName.split("\\@");
        if (seqInfo.length > 0) {
          this.seqName = seqInfo[0];
        }
        if (seqInfo.length > 1) {
          columnType = seqInfo[1];
        }
        if (seqInfo.length > 2) {
          columnName = seqInfo[2];
        }

      } else {
        this.seqName = seqName;
      }
    }
  }

  /**
   * Gets the column name for date.
   *
   * @return the column name for date
   */
  public String getColumnNameForDate() {
    return columnNameForDate;
  }

  /**
   * Sets the column name for date.
   *
   * @param columnNameForDate the new column name for date
   */
  public void setColumnNameForDate(String columnNameForDate) {
    this.columnNameForDate = columnNameForDate;
  }

  /**
   * Gets the column name for user.
   *
   * @return the column name for user
   */
  public String getColumnNameForUser() {
    return columnNameForUser;
  }

  /**
   * Sets the column name for user.
   *
   * @param columnNameForUser the new column name for user
   */
  public void setColumnNameForUser(String columnNameForUser) {
    this.columnNameForUser = columnNameForUser;
  }

  /**
   * Checks if is date required.
   *
   * @return true, if is date required
   */
  public boolean isDateRequired() {
    return isDateRequired;
  }

  /**
   * Sets the date required.
   *
   * @param isDateRequired the new date required
   */
  public void setDateRequired(boolean isDateRequired) {
    this.isDateRequired = isDateRequired;
  }

  /**
   * Checks if is user name required.
   *
   * @return true, if is user name required
   */
  public boolean isUserNameRequired() {
    return isUserNameRequired;
  }

  /**
   * Sets the user name required.
   *
   * @param isUserNameRequired the new user name required
   */
  public void setUserNameRequired(boolean isUserNameRequired) {
    this.isUserNameRequired = isUserNameRequired;
  }

  /**
   * Gets the id.
   *
   * @return the id
   */
  public String getId() {
    return id;
  }

  /**
   * Sets the id.
   *
   * @param id the new id
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Checks if is use audit log hint.
   *
   * @return true, if is use audit log hint
   */
  public boolean isUseAuditLogHint() {
    return useAuditLogHint;
  }

  /**
   * Sets the use audit log hint.
   *
   * @param useAuditLogHint the new use audit log hint
   */
  public void setUseAuditLogHint(boolean useAuditLogHint) {
    this.useAuditLogHint = useAuditLogHint;
  }

  /**
   * Gets the no of rows skip.
   *
   * @return the no of rows skip
   */
  public int getNoOfRowsSkip() {
    return noOfRowsSkip;
  }

  /**
   * Sets the no of rows skip.
   *
   * @param noOfRowsSkip the new no of rows skip
   */
  public void setNoOfRowsSkip(int noOfRowsSkip) {
    this.noOfRowsSkip = noOfRowsSkip;
  }

  /**
   * Checks if is composite primary key.
   *
   * @return true, if is composite primary key
   */
  public boolean isCompositePrimaryKey() {
    return isCompositePrimaryKey;
  }

  /**
   * Sets the composite primary key.
   *
   * @param compositePrimaryKey the new composite primary key
   */
  public void setCompositePrimaryKey(boolean compositePrimaryKey) {
    this.isCompositePrimaryKey = compositePrimaryKey;
  }

  /**
   * Gets the composite primary keys.
   *
   * @return the composite primary keys
   */
  public List<String> getCompositePrimaryKeys() {
    return compositePrimaryKeys;
  }

  /**
   * Sets the composite primary keys.
   *
   * @param compositePrimaryKeys the new composite primary keys
   */
  public void setCompositePrimaryKeys(List<String> compositePrimaryKeys) {
    this.compositePrimaryKeys = compositePrimaryKeys;
  }

  /**
   * Checks if is insert enable.
   *
   * @return true, if is insert enable
   */
  public boolean isInsertEnable() {
    return isInsertEnable;
  }

  /**
   * Sets the insert enable.
   *
   * @param isInsertEnable the new insert enable
   */
  public void setInsertEnable(boolean isInsertEnable) {
    this.isInsertEnable = isInsertEnable;
  }

  /**
   * This method upload the excel file to db.
   *
   * @param sheet                  the sheet
   * @param aliasMap               This map Contains key as Excel column header name and value as
   *                               table column name header name
   * @param columnValueVerifierMap This map contains key as Excel column header name and value as db
   *                               column name@table name
   * @param mandatoryFieldsList    - This list holds the mandatory fields list.
   * @param errors                 - already existing errors
   * @param userName               - action user
   * @param skipFieldsList         - List contains fields to skip from reading.
   * @throws SQLException the SQL exception
   * @throws IOException  Signals that an I/O exception has occurred.
   */
  public void uploadExcelToDB(XSSFSheet sheet, LinkedHashMap<String, String> aliasMap,
      LinkedHashMap<String, String[]> columnValueVerifierMap, List<String> mandatoryFieldsList,
      StringBuilder errors, String userName, List<String> skipFieldsList)
      throws SQLException, IOException {

    Iterator<Row> rowIterator = sheet.rowIterator();
    int rowCount = 0;
    this.errors = errors;
    this.sheetName = sheet.getSheetName();

    while (rowIterator.hasNext()) {
      rowCount++;
      XSSFRow headerRow = (XSSFRow) rowIterator.next();

      // If noOfRowsSkip is > 0 then skip the rows accordingly.
      /*
       * if(noOfRowsSkip > 0) { for(int i=0;i<noOfRowsSkip; i++) { rowIterator.next(); } }
       */

      LinkedHashMap<String, Map<String, List<BasicDynaBean>>> refColumnValueMaps =
          getcolumnValueVerifierMapData(columnValueVerifierMap);
      // Hint for audit logs.
      String userNameWithHint = ((null == userName) ? "" : userName)
          + ((useAuditLogHint) ? AUDIT_LOG_HINT : "");

      // Form Excel header list based on the aliasMap and skipFieldsList.
      Map<String, Boolean> excelHeadersList = new LinkedHashMap<String, Boolean>();
      for (int i = 0; i < headerRow.getLastCellNum(); i++) {

        XSSFCell cell = headerRow.getCell(i);
        String header = cell.getStringCellValue().toLowerCase();

        if (!aliasMap.containsKey(header)) {

          if (!skipFieldsList.contains(header)) {
            addError(0, "Unknown header found in headers: " + header);

          } else {
            excelHeadersList.put(header, true);
          }

        } else {
          excelHeadersList.put(aliasMap.get(header), false);
        }

      }
      aliasMap = null;

      // Check the excel is there any mandatory parameter is missing.
      Iterator<String> mandatoryFieldsListIterator = mandatoryFieldsList.iterator();
      while (mandatoryFieldsListIterator.hasNext()) {
        String mandatoryFieldName = mandatoryFieldsListIterator.next();
        // If mandatory parameter is missing then stop processing the excel.
        if (!excelHeadersList.containsKey(mandatoryFieldName)) {
          addError(0,
              "Mandatory field " + mandatoryFieldName + " is missing cannot process further");
          return;
        }
      }

      GenericDAO dao = new GenericDAO(mainTableName);
      BasicDynaBean tableBean = dao.getBean();
      BasicDynaBean itemBean = null;
      Connection con = null;
      int lineNumber = 0;
      String operation = "update";
      try {

        con = DataBaseUtil.getConnection();
        con.setAutoCommit(false);
        // Iterate through each line of excel sheet.
        nxtLine: while (rowIterator.hasNext()) {
          XSSFRow dataRow = (XSSFRow) rowIterator.next();
          if (noOfRowsSkip < dataRow.getRowNum() - 1) {
            lineNumber = dataRow.getRowNum() + 1;
            itemBean = dao.getBean();
            Object cellId = null;
            Map<String, Object> compositeKeys = new HashMap<String, Object>();
            Map<String, Object> keys = null;
            Object newId = null;
            boolean lineHasErrors = false;
            DynaProperty property = null;
            boolean success = false;
            int index = 0;

            Iterator<Entry<String, Boolean>> excelHeadersListIterator = excelHeadersList.entrySet()
                .iterator();
            // Iterate through each cell of corresponding line of excel.
            nxtCell: while (excelHeadersListIterator.hasNext()) {
              Entry<String, Boolean> excelHeadersEntry = excelHeadersListIterator.next();

              if (!excelHeadersEntry.getValue()) {

                XSSFCell rowcell = dataRow.getCell(index);
                Object cellVal = null;
                try {
                  if (rowcell != null && !StringUtil.isNullOrEmpty(rowcell.toString())) {

                    property = tableBean.getDynaClass().getDynaProperty(excelHeadersEntry.getKey());

                    // If cell is not empty then get the value and assign to cellVal.
                    switch (rowcell.getCellType()) {

                      case XSSFCell.CELL_TYPE_NUMERIC: {
                        if (property.getType().equals(String.class)) {
                          rowcell.setCellType(1);
                          cellVal = rowcell.getStringCellValue();
                        } else {
                          cellVal = rowcell.getNumericCellValue();
                        }
                        break;
                      }

                      case XSSFCell.CELL_TYPE_STRING: {
                        cellVal = rowcell.getStringCellValue();
                        break;
                      }

                      case XSSFCell.CELL_TYPE_BOOLEAN: {
                        cellVal = rowcell.getBooleanCellValue();
                        break;
                      }

                      default: {
                        cellVal = null;
                        break;
                      }

                    }
                    // If mandatory field value is empty add error and read next line.
                    if (mandatoryFieldsList.contains(excelHeadersEntry.getKey().trim())
                        && cellVal == null) {
                      lineHasErrors = true;
                      addError(lineNumber, excelHeadersEntry.getKey() + " should not be null");
                      continue nxtLine;
                    }

                    // Set cell value from Excel cell.
                    // Check depended table for cell value is exist or not.
                    if (columnValueVerifierMap.containsKey(excelHeadersEntry.getKey())) {
                      if (cellVal != null) {
                        String[] columnValueCheckerColumn = columnValueVerifierMap
                            .get(excelHeadersEntry.getKey());
                        if (columnValueCheckerColumn.length == 4) {
                          Map<String, List<BasicDynaBean>> refColumnValueMap = refColumnValueMaps
                              .get(excelHeadersEntry.getKey());
                          String[] setDobuleInsertValues = null;
                          if (columnValueCheckerColumn[0].contains("@")) {
                            setDobuleInsertValues = columnValueCheckerColumn[0].split("@");
                            if (setDobuleInsertValues.length > 1
                                && setDobuleInsertValues[1].equals(excelHeadersEntry.getKey())) {
                              itemBean.set(setDobuleInsertValues[1], cellVal);
                            }
                          }
                          if (refColumnValueMap.containsKey(cellVal)) {
                            if (columnValueCheckerColumn[0].contains("@")) {
                              itemBean.set(setDobuleInsertValues[0], refColumnValueMap.get(cellVal)
                                  .get(0).get(columnValueCheckerColumn[1]));
                            } else {
                              itemBean.set(columnValueCheckerColumn[0], refColumnValueMap
                                  .get(cellVal).get(0).get(columnValueCheckerColumn[1]));
                            }
                          } else {
                            if (columnValueCheckerColumn[0].contains("@")) {
                              if (setDobuleInsertValues.length > 2) {
                                if ("insensitive".equalsIgnoreCase(setDobuleInsertValues[2])) {
                                  String makeIgloreCase = ((String) cellVal).toUpperCase();
                                  if (refColumnValueMap.containsKey(makeIgloreCase)) {
                                    itemBean.set(setDobuleInsertValues[0],
                                        refColumnValueMap.get(makeIgloreCase).get(0)
                                            .get(columnValueCheckerColumn[1]));
                                  } else {
                                    itemBean.set(setDobuleInsertValues[0], null);
                                  }
                                } else {
                                  itemBean.set(setDobuleInsertValues[0], null);
                                }

                              } else {
                                itemBean.set(setDobuleInsertValues[0], null);
                              }
                            } else {
                              addError(lineNumber, excelHeadersEntry.getKey() + " cell value "
                                  + cellVal + " is not exist in reference table");
                              index++;
                              continue nxtCell;
                            }

                          }

                        }
                      }
                    } else {
                      // For Composite Primary key set values from excel sheet accordingly.
                      if (isCompositePrimaryKey
                          && compositePrimaryKeys.contains(excelHeadersEntry.getKey())) {
                        switch (rowcell.getCellType()) {
                          case XSSFCell.CELL_TYPE_NUMERIC: {
                            cellId = new Double(rowcell.getNumericCellValue()).intValue();
                            compositeKeys.put(excelHeadersEntry.getKey(),
                                ConvertUtils.convert(cellId, property.getType()));
                            break;
                          }
                          case XSSFCell.CELL_TYPE_STRING: {
                            cellId = rowcell.getStringCellValue();
                            compositeKeys.put(excelHeadersEntry.getKey(),
                                ConvertUtils.convert(cellId, property.getType()));
                            break;
                          }
                          default: {
                            break;
                          }
                        }
                        // If Cell holds the id value then operation should be DB insertion OR
                        // Update operation
                      } else if (excelHeadersEntry.getKey().equals(id)) {
                        switch (rowcell.getCellType()) {
                          case XSSFCell.CELL_TYPE_NUMERIC: {
                            cellId = new Double(rowcell.getNumericCellValue()).intValue();
                            break;
                          }
                          case XSSFCell.CELL_TYPE_STRING: {
                            cellId = rowcell.getStringCellValue();
                            break;
                          }
                          default:
                            cellId = null;
                        }
                      } else {
                        // If conversion has any problem with data types ,
                        // add conversion classes and type in register method of ConvertUtils
                        // Ex: ConvertUtils.register(new DateConverter(), Date.class);
                        Object convertedValue = ConvertUtils.convert(cellVal, property.getType());
                        if (convertedValue != null) {
                          itemBean.set(excelHeadersEntry.getKey(), convertedValue);
                        } else if (convertedValue == null && cellVal != null) {
                          addError(lineNumber, excelHeadersEntry.getKey() + " cell value " + cellVal
                              + " is not a valid value.");
                          index++;
                          continue nxtCell;
                        }

                      }
                    }

                  } else if (excelHeadersEntry.getKey().equals(id) && !isCompositePrimaryKey) {
                    if (cellId == null) {
                      operation = "insert";
                      index++;
                      continue nxtCell;
                    }
                  } else {
                    if (mandatoryFieldsList.contains(excelHeadersEntry.getKey().trim())
                        && cellVal == null) {
                      lineHasErrors = true;
                      addError(lineNumber, excelHeadersEntry.getKey() + " should not be null");
                      continue nxtLine;
                    }
                  }
                } catch (Exception exception) {
                  if (property != null) {
                    addError(lineNumber,
                        "Conversion error in Header:" + excelHeadersEntry.getKey() + " Cell value :"
                            + cellVal + " could not be converted to " + property.getType()
                            + " below headers of " + excelHeadersEntry.getKey());
                    lineHasErrors = true;
                  } else {
                    addError(lineNumber,
                        "Conversion error in Header:" + excelHeadersEntry.getKey()
                            + " Cell value : " + cellVal
                            + " could not be converted to class java.lang.String below headers of "
                            + excelHeadersEntry.getKey());
                    lineHasErrors = true;
                  }
                  index++;
                  continue nxtCell;
                }

              }
              index++;
            }

            if (lineHasErrors) {
              continue nxtLine;
            }

            /* updating or inserting part */
            try {
              if (itemBean.getMap().size() > 0) {
                if (operation.equals("update") && !isCompositePrimaryKey) {
                  keys = new HashMap<String, Object>();
                  keys.put(id, cellId);

                  /* update the user name and date&time if required */
                  if (isDateRequired) {
                    itemBean.set(columnNameForDate, DateUtil.getCurrentTimestamp());
                  }
                  if (isUserNameRequired) {
                    itemBean.set(columnNameForUser, userNameWithHint);
                  }

                  if (updateRecord(dao, itemBean, con, keys) > 0) {
                    success = true;
                  } else {
                    addError(lineNumber, "DB error: Unable to " + operation + " the record. ");
                  }

                } else if (isCompositePrimaryKey) {

                  keys = new HashMap<String, Object>();
                  Iterator<Entry<String, Object>> compositeKeysIterator = compositeKeys.entrySet()
                      .iterator();
                  while (compositeKeysIterator.hasNext()) {
                    Entry<String, Object> compositeKeysEntry = compositeKeysIterator.next();
                    keys.put(compositeKeysEntry.getKey(), compositeKeysEntry.getValue());
                  }

                  /* update the user name and date&time if required */
                  if (isDateRequired) {
                    itemBean.set(columnNameForDate, DateUtil.getCurrentTimestamp());
                  }
                  if (isUserNameRequired) {
                    itemBean.set(columnNameForUser, userNameWithHint);
                  }
                  if (keys.size() > 0) {
                    success = updateRecord(dao, itemBean, con, keys) > 0;
                  }

                  if (!success && isInsertEnable) {
                    operation = "insert";
                    if (insertRecord(dao, itemBean, con)) {
                      success = true;
                    } else {
                      addError(lineNumber, "DB error: Unable to " + operation + " the record. ");
                    }
                  }

                } else if (isInsertEnable) {
                  if (seqName == null) {
                    newId = dao.getNextSequence();
                  } else if (seqName.contains("custom_unique_key")) {
                    newId = AutoIncrementId.getNewIncrUniqueId(columnName, mainTableName,
                        columnType);
                  } else {
                    newId = DataBaseUtil.getNextSequence(seqName);
                  }
                  itemBean.set(id, newId);
                  if (isDateRequired) {
                    itemBean.set(columnNameForDate, DateUtil.getCurrentTimestamp());
                  }
                  if (isUserNameRequired) {
                    itemBean.set(columnNameForUser, userName);
                  }

                  if (insertRecord(dao, itemBean, con)) {
                    success = true;
                  } else {
                    addError(lineNumber, "DB error: Unable to " + operation + " the record. ");
                  }

                } else {
                  addError(lineNumber, "Insert is not allowed. ");
                }
                itemBean.getMap().clear();
              }
            } catch (SQLException sqle) {
              con.rollback();
              if (DataBaseUtil.isDuplicateViolation(sqle)) {
                addError(lineNumber, "Duplicate record found. " + sqle.getMessage());
                continue nxtLine;
              } else {
                addError(lineNumber, "Unknown error: " + sqle.getMessage());
                continue nxtLine;
              }

            } catch (Exception exception) {
              addError(lineNumber,
                  "DB error: Unable to " + operation + " the record. " + exception.getMessage());
              continue nxtLine;
            }
            if (success) {
              con.commit();
            }
          } else {
            continue nxtLine;
          }

        }

      } catch (Exception exception) {
        addError(lineNumber,
            "DB error: Unable to " + operation + " the record. " + exception.getMessage());

      } finally {
        DataBaseUtil.closeConnections(con, null);
      }

      excelHeadersList = null;
    }
    if (rowCount == 0) {
      addError(1, "Excel file doesn't have any headers.Please upload a valid excel file");
    }

  }

  /**
   * Insert record.
   *
   * @param dao      the dao
   * @param itemBean the item bean
   * @param con      the con
   * @return true, if successful
   * @throws SQLException the SQL exception
   * @throws IOException  Signals that an I/O exception has occurred.
   */
  protected boolean insertRecord(GenericDAO dao, BasicDynaBean itemBean, Connection con)
      throws SQLException, IOException {
    return dao.insert(con, itemBean);
  }

  /**
   * Update record.
   *
   * @param dao      the dao
   * @param itemBean the item bean
   * @param con      the con
   * @param keys     the keys
   * @return the int
   * @throws SQLException the SQL exception
   * @throws IOException  Signals that an I/O exception has occurred.
   */
  protected int updateRecord(GenericDAO dao, BasicDynaBean itemBean, Connection con,
      Map<String, Object> keys) throws SQLException, IOException {
    return dao.update(con, itemBean.getMap(), keys);
  }

  /**
   * Adds the error.
   *
   * @param line the line
   * @param msg  the msg
   */
  private void addError(int line, String msg) {

    errors.append("Sheet ").append(sheetName).append(": ");
    if (line > 0) {
      errors.append("Line ").append(line).append(": ");
    } else {
      errors.append("Error in header: ");
    }
    errors.append(msg).append("<br>");
  }

  /**
   * Gets the column value verifier map data.
   *
   * @param columnValueVerifierMap the column value verifier map
   * @return the column value verifier map data
   * @throws SQLException the SQL exception
   */
  private LinkedHashMap<String, Map<String, List<BasicDynaBean>>> getcolumnValueVerifierMapData(
      LinkedHashMap<String, String[]> columnValueVerifierMap) throws SQLException {
    LinkedHashMap<String, Map<String, List<BasicDynaBean>>> columnVerifierDataMap =
        new LinkedHashMap<String, Map<String, List<BasicDynaBean>>>();

    Iterator<Entry<String, String[]>> columnValueVerifierMapIterator = columnValueVerifierMap
        .entrySet().iterator();
    while (columnValueVerifierMapIterator.hasNext()) {
      Entry<String, String[]> columnValueVerifierMapEntry = columnValueVerifierMapIterator.next();
      String excelHeaderName = columnValueVerifierMapEntry.getKey();
      String[] refTableColumnArray = columnValueVerifierMapEntry.getValue();
      if (refTableColumnArray.length == 4) {

        GenericDAO checkerDao = new GenericDAO(refTableColumnArray[3]);
        List<String> columns = new ArrayList<String>();
        columns.add(refTableColumnArray[1]);
        columns.add(refTableColumnArray[2]);
        List<BasicDynaBean> existDbValues = checkerDao.listAll(columns);
        if (refTableColumnArray[0].contains("@")) {
          String[] getSplitLength = refTableColumnArray[0].split("@");
          if (getSplitLength.length > 2) {
            if ("insensitive".equalsIgnoreCase(getSplitLength[2])) {
              columnVerifierDataMap.put(excelHeaderName,
                  listBeanToMapListMap(existDbValues, refTableColumnArray[2]));
            } else {
              columnVerifierDataMap.put(excelHeaderName,
                  ConversionUtils.listBeanToMapListBean(existDbValues, refTableColumnArray[2]));
            }
          } else {
            columnVerifierDataMap.put(excelHeaderName,
                ConversionUtils.listBeanToMapListBean(existDbValues, refTableColumnArray[2]));
          }
        } else {
          columnVerifierDataMap.put(excelHeaderName,
              ConversionUtils.listBeanToMapListBean(existDbValues, refTableColumnArray[2]));
        }
      }
    }
    return columnVerifierDataMap;
  }

  /**
   * List bean to map list map.
   *
   * @param beans      the beans
   * @param columnName the column name
   * @return the hash map
   */
  // If required insensitive column values.
  private HashMap listBeanToMapListMap(List beans, String columnName) {
    HashMap rowMap = new LinkedHashMap();
    Iterator it = beans.iterator();
    while (it.hasNext()) {
      BasicDynaBean row = (BasicDynaBean) it.next();
      Object colName = row.get(columnName);
      List list = (List) rowMap.get(colName);
      if (list == null) {
        list = new ArrayList();
        rowMap.put(((String) colName).toUpperCase(), list);
      }
      list.add(row);
    }
    return rowMap;
  }
}
