package com.insta.hms.xls.exportimport;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.HsSfWorkbookUtils;
import com.insta.hms.adminmasters.bedmaster.BedMasterDAO;
import com.insta.hms.common.GenericDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ChargesImportExporter {

  static Logger logger = LoggerFactory.getLogger(ChargesImportExporter.class);

  private static final String AUDIT_LOG_HINT = ":XLS";
  private String itemTable;
  private String itemOrgDetailsTable;
  private String itemChargesTable;
  private String itemDeptTable;
  private String itemKey;
  private String orgKey;
  private String chargeKey;
  private String itemName;
  private String chgTabOrgColName;
  private String bedColumnName;
  private String userColumnName;
  private String itemDeptKey;
  private String deptKey;
  private String deptNameKey;

  private String[] itemColumns;
  private String[] orgColumns;
  private String[] chargeColumns;
  private String[] mandatoryFields;

  private String[] itemWhereFieldKeys;
  private String[] orgWhereFieldKeys;
  private String[] chargesWhereFieldKeys;

  private Map<String, String> userNamesToDbNames;
  private Map<String, String> dbNamesToUserNames;
  private Map<String, List<String>> columnNamesMap;

  private boolean useAuditLogHint;

  public ChargesImportExporter() {

  }

  /**
   * Instantiates a new charges import exporter.
   *
   * @param itemTable
   *          the item table
   * @param itemOrgDetailsTable
   *          the item org details table
   * @param itemChargesTable
   *          the item charges table
   * @param itemDeptTable
   *          the item dept table
   * @param itemKey
   *          the item key
   * @param itemDeptKey
   *          the item dept key
   * @param deptNameKey
   *          the dept name key
   * @param itemColumns
   *          the item columns
   * @param itemColNames
   *          the item col names
   * @param orgColumns
   *          the org columns
   * @param orgColumnNames
   *          the org column names
   * @param chargeColumns
   *          the charge columns
   * @param chargeColNames
   *          the charge col names
   */
  public ChargesImportExporter(String itemTable, String itemOrgDetailsTable,
      String itemChargesTable, String itemDeptTable, String itemKey, String itemDeptKey,
      String deptNameKey, String[] itemColumns, String[] itemColNames, String[] orgColumns,
      String[] orgColumnNames, String[] chargeColumns, String[] chargeColNames) {

    this.itemTable = itemTable;
    this.itemOrgDetailsTable = itemOrgDetailsTable;
    this.itemChargesTable = itemChargesTable;
    this.itemDeptTable = itemDeptTable;
    this.itemKey = itemKey;
    this.chargeKey = itemKey;
    this.orgKey = itemKey;
    this.itemColumns = itemColumns;
    this.orgColumns = orgColumns;
    this.chargeColumns = chargeColumns;
    this.chgTabOrgColName = "org_id";
    this.bedColumnName = "bed_type";
    this.userColumnName = "username";
    this.useAuditLogHint = false;
    this.itemDeptKey = itemDeptKey;
    this.deptKey = itemDeptKey;
    this.deptNameKey = deptNameKey;

    userNamesToDbNames = new HashMap<String, String>();
    dbNamesToUserNames = new HashMap<String, String>();
    columnNamesMap = new HashMap<String, List<String>>();
    List<String> itemList = new ArrayList<String>();
    List<String> chargeList = new ArrayList<String>();

    for (int i = 0; i < itemColumns.length; i++) {
      userNamesToDbNames.put(itemColNames[i].toLowerCase(), itemColumns[i]);
      dbNamesToUserNames.put(itemColumns[i], itemColNames[i]);
      itemList.add(itemColNames[i]);

    }
    if (itemDeptTable != null) {
      userNamesToDbNames.put("department", "department");
      dbNamesToUserNames.put("department", "department");
      itemList.add("Department");
    }
    for (int k = 0; k < orgColumns.length; k++) {
      userNamesToDbNames.put(orgColumnNames[k].toLowerCase(), orgColumns[k]);
      dbNamesToUserNames.put(orgColumns[k], orgColumnNames[k]);
      itemList.add(orgColumnNames[k]);
    }
    for (int j = 0; j < chargeColumns.length; j++) {
      userNamesToDbNames.put(chargeColNames[j].toLowerCase(), chargeColumns[j]);
      dbNamesToUserNames.put(chargeColumns[j], chargeColNames[j]);
      chargeList.add(chargeColNames[j]);

    }
    columnNamesMap.put("mainItems", itemList);
    columnNamesMap.put("charges", chargeList);
  }

  public void setItemName(String itemName) {
    this.itemName = itemName;
  }

  public void setOrgKey(String orgKey) {
    this.orgKey = orgKey;
  }

  public void setBedColumnName(String bedColumnName) {
    this.bedColumnName = bedColumnName;
  }

  public void setUserColumnName(String userColumnName) {
    this.userColumnName = userColumnName;
  }

  public void setDeptKey(String deptKey) {
    this.deptKey = deptKey;
  }

  public void setMandatoryFields(String[] mandatoryFields) {
    this.mandatoryFields = mandatoryFields;
  }

  public void setChargeKey(String chargeKey) {
    this.chargeKey = chargeKey;
  }

  public void setChgTabOrgColName(String chgTabOrgColName) {
    this.chgTabOrgColName = chgTabOrgColName;
  }

  public void setItemWhereFieldKeys(String[] itemKeys) {
    this.itemWhereFieldKeys = itemKeys;
  }

  public void setOrgWhereFieldKeys(String[] orgKeys) {
    this.orgWhereFieldKeys = orgKeys;
  }

  public void setChargeWhereFieldKeys(String[] chargeKeys) {
    this.chargesWhereFieldKeys = chargeKeys;
  }

  public void setUseAuditLogHint(boolean useAuditLogHint) {
    this.useAuditLogHint = useAuditLogHint;
  }

  /**
   * Export charges.
   *
   * @param orgId
   *          the org id
   * @param workSheet
   *          the work sheet
   * @param deptFilter
   *          the dept filter
   * @param statusFilter
   *          the status filter
   * @throws SQLException
   *           the SQL exception
   */
  public void exportCharges(String orgId, XSSFSheet workSheet, String deptFilter,
      String statusFilter) throws SQLException {

    List<String> allBedTypes = new BedMasterDAO().getUnionOfBedTypes();
    columnNamesMap.put("bedTypes", allBedTypes);
    StringBuilder query = new StringBuilder();
    Connection con = null;
    PreparedStatement pstmt = null;
    int bed = 0;

    /* Construting the query */
    query.append("SELECT ");

    for (String itemColumn : itemColumns) {

      query.append("itemTable." + itemColumn + ",");
    }
    if (itemDeptTable != null) {
      query.append("deptTable." + deptNameKey + ",");
    }
    if (itemOrgDetailsTable != null) {
      for (String orgColumn : orgColumns) {

        query.append("orgTable." + orgColumn + ",");
      }
    }
    for (String bedType : allBedTypes) {
      int charge = 0;
      ++bed;
      for (String chargeColumn : chargeColumns) {
        ++charge;
        query.append("(SELECT " + chargeColumn + " FROM " + itemChargesTable + " WHERE " + chargeKey
            + " = itemTable." + itemKey + " AND bed_type = ? AND " + chgTabOrgColName + " = ?) AS "
            + DataBaseUtil.quoteIdent(bedType + "" + chargeColumn, true));
        if (bed < allBedTypes.size() || charge < chargeColumns.length) {
          query.append(",");
        }
      }

    }
    query.append(" FROM " + itemTable + " itemTable ");
    if (itemOrgDetailsTable != null) {
      query.append(" JOIN " + itemOrgDetailsTable + " orgTable ON (itemTable." + itemKey + "= "
          + "orgTable." + orgKey + " AND org_id = ?)");
    }
    if (itemDeptTable != null) {
      query.append(" JOIN " + itemDeptTable + " deptTable ON (itemTable." + itemDeptKey + "="
          + "deptTable." + deptKey + ")");
    }
    if (deptFilter != null || statusFilter != null) {

      query.append(" WHERE");
      if (deptFilter != null) {

        query.append("dept_id = ?");

      }

      if (statusFilter != null) {
        if (deptFilter != null) {
          query.append(" AND ");
        }
        query.append(" itemTable.status = ?");
      }
    }

    query.append(" ORDER BY itemTable." + itemKey);

    try {
      con = DataBaseUtil.getReadOnlyConnection();
      pstmt = con.prepareStatement(query.toString());
      int index = 1;

      for (String bedType : allBedTypes) {
        for (String chargecolumn : chargeColumns) {
          pstmt.setString(index++, bedType);
          pstmt.setString(index++, orgId);
        }
      }
      if (itemOrgDetailsTable != null) {
        pstmt.setString(index++, orgId);
      }

      if (deptFilter != null) {
        pstmt.setString(index++, deptFilter);
      }
      if (statusFilter != null) {
        pstmt.setString(index++, statusFilter);
      }

      List list = DataBaseUtil.queryToDynaList(pstmt);
      HsSfWorkbookUtils.createPhysicalCellsWithValues(list, columnNamesMap, workSheet, false);

    } finally {
      DataBaseUtil.closeConnections(con, pstmt);

    }

  }

  public StringBuilder errors;

  /**
   * Import charges.
   * 
   * <p>
   * While importing charges, we are OK if the user deletes or moves some columns. We get back the
   * actual field name based on the column header. The user can delete any of the item's column nams
   * and bed types, but within a bed type, all charges must exist. That is, user cannot delete a
   * merged column. Also, the charges must come at the end only, ie, user cannot insert an item
   * column between bed-types/charges.
   * </p>
   * 
   * @param bedTypeDependent
   *          the bed type dependent
   * @param orgId
   *          the org id
   * @param sheet
   *          the sheet
   * @param userName
   *          the user name
   * @param errorss
   *          the errorss
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public void importCharges(boolean bedTypeDependent, String orgId, XSSFSheet sheet,
      String userName, StringBuilder errorss) throws SQLException, IOException {

    List<String> bedTypes = getUnionOfBedTypes();
    Map<String, String> dbBedNamesMap = getMapofOriginalBedtypes();
    String userNameWithHint = ((null == userName) ? "" : userName)
        + ((useAuditLogHint) ? AUDIT_LOG_HINT : "");

    errors = errorss;
    int noOfCharges = chargeColumns.length; // charges per bed has to be constant.
    int maxColumns = itemColumns.length + orgColumns.length + (bedTypes.size() * noOfCharges);
    if (itemDeptTable != null) {
      maxColumns += 1;
    }
    String[] headers = new String[maxColumns];

    Iterator rowIterator = sheet.rowIterator();
    XSSFRow row1 = (XSSFRow) rowIterator.next();
    int bedsCount = 0;
    int chargesColNo = 0;

    for (int i = 0; i < headers.length; i++) {
      XSSFCell cell = row1.getCell(i);
      if (cell == null) {
        headers[i] = null; /* putting null values, if found */
        continue;
      }

      String header = cell.getStringCellValue().toLowerCase();
      String dbName = (String) (userNamesToDbNames.get(header) == null ? header
          : userNamesToDbNames.get(header));
      headers[i] = dbName;

      int headerColNo = cell.getColumnIndex();
      if (!(Arrays.asList(itemColumns).contains(dbName) || "department".equals(dbName)
          || Arrays.asList(orgColumns).contains(dbName))) {
        if (bedTypes.contains(dbName)) {
          bedsCount++;
          if (chargesColNo == 0) {
            chargesColNo = i;
          }

        } else {
          if (header != null && !"".equals(header)) {
            addError(0, "Unknown property found in header " + header + "(" + dbName + ")"
                + " at column: " + i);
          }
          headers[i] = null; /* putting null values, if found unknown properties */
        }
      }
    }

    /* Checking for mandatory fields */
    List headersList = Arrays.asList(headers);
    for (String mdtField : mandatoryFields) {
      if (!headersList.contains(mdtField)) {
        addError(0,
            "Mandatory field is missing " + (dbNamesToUserNames.get(mdtField) == null ? mdtField
                : dbNamesToUserNames.get(mdtField)).toString().toUpperCase());
        return;
      }

    }

    XSSFRow row2 = sheet.getRow(1);
    int noOfCellsToRead = headers.length;

    String subHeader = null;
    String dbCharge = null;
    String[] subHeaders = new String[noOfCellsToRead];

    if (row2 != null) {
      rowIterator.next();
      for (int i = chargesColNo; i < noOfCellsToRead; i++) {
        XSSFCell cell = row2.getCell(i);
        if (cell != null && !cell.equals("")) {
          subHeader = cell.toString().toLowerCase();
          dbCharge = (String) (userNamesToDbNames.get(subHeader) == null ? subHeader
              : userNamesToDbNames.get(subHeader));
          if (Arrays.asList(chargeColumns).contains(dbCharge)) {
            subHeaders[i] = dbCharge;

          } else {
            subHeaders[i] = null;
            addError(1, "Unknown charge type found in subHeader " + subHeader);
          }
        } else {
          subHeaders[i] = null;
        }
      }
    }

    /* Mapping .... */
    Map<Integer, String[]> columnMap = new HashMap<Integer, String[]>();
    String[] bedAndCharge = null;
    int subLen = chargesColNo;

    for (int i = 0; i < headers.length; i++) {

      if (headers[i] == null || headers[i].equals("")) {
        if (i >= chargesColNo) {
          columnMap.put(i, new String[] { null });
          subLen++;
        } else {
          columnMap.put(i, new String[] { null });
        }

      } else if (bedTypes.contains(headers[i])) {
        String bedType = dbBedNamesMap.get(headers[i]);
        int subI = i;

        for (int charges = 0; charges < noOfCharges; charges++) {
          bedAndCharge = new String[2];
          bedAndCharge[0] = bedType;
          bedAndCharge[1] = subHeaders[subLen];
          columnMap.put(subI, bedAndCharge);
          subLen++;
          subI++; /* incrementing header bcoz of spliting the cells remaining cells gives nulls */
        }
        i = i + (noOfCharges - 1);

      } else {
        columnMap.put(i, new String[] { headers[i] });
      }
    }

    GenericDAO itemDao = null;
    BasicDynaBean itemTabBean = null;
    if (itemTable != null) {
      itemDao = new GenericDAO(itemTable);
      itemTabBean = itemDao.getBean();
    }

    GenericDAO itemChargesDao = new GenericDAO(itemChargesTable);
    BasicDynaBean itemChargesBean = itemChargesDao.getBean();

    GenericDAO itemOrgDao = null;
    BasicDynaBean itemOrgBean = null;
    if (itemOrgDetailsTable != null) {
      itemOrgDao = new GenericDAO(itemOrgDetailsTable);
      itemOrgBean = itemOrgDao.getBean();
    }

    Map itemMap = null;
    Map itemOrgMap = null;
    Map<String, Map<String, BigDecimal>> eachBedMap = null;

    // List listOfChargeMaps = null;
    BigDecimal chargeVal;

    Map itemKeys = null;
    Map orgKeys = null;
    Map chargeKeys = null;

    nextLine: while (rowIterator.hasNext()) {
      XSSFRow row = (XSSFRow) rowIterator.next();
      int lineNumber = row.getRowNum() + 1;
      if (row != null) {

        itemMap = new HashMap();
        itemOrgMap = new HashMap();
        eachBedMap = new HashMap<String, Map<String, BigDecimal>>();

        itemKeys = new HashMap();
        orgKeys = new HashMap();
        chargeKeys = new HashMap();

        Object idVal = null;
        BasicDynaBean itemBean = null;
        Object itemNameValue = null;
        Object deptNameValue = null;
        int index = 0;
        Iterator colMapItr = columnMap.entrySet().iterator();

        nextCell: while (colMapItr.hasNext()) {

          Map.Entry entry = (Map.Entry) colMapItr.next();

          String dbName = null;
          Object cellVal = null;
          DynaProperty property = null;

          String colName = columnMap.get(index)[0];
          if (colName == null) {
            index++;
            continue nextCell;
          }

          XSSFCell rowcell = row.getCell(index);

          dbName = (String) (userNamesToDbNames.get(colName) == null ? colName
              : userNamesToDbNames.get(colName));

          try {

            /* set item values to ItemMap */

            if ("department".equals(dbName)) {
              deptNameValue = rowcell.getStringCellValue();
            } else if (Arrays.asList(itemColumns).contains(dbName)) {

              property = itemTabBean.getDynaClass().getDynaProperty(dbName);
              Class type = property.getType();
              if (rowcell != null && !rowcell.equals("")) {

                if (type == java.lang.String.class) {
                  cellVal = rowcell.getStringCellValue();
                } else if (type == java.lang.Boolean.class) {
                  cellVal = rowcell.getBooleanCellValue();
                }
              }

              if (itemName.equals(dbName)) {
                itemNameValue = cellVal;
              }

              itemMap.put(dbName, ConvertUtils.convert(cellVal, property.getType()));
            } else if (Arrays.asList(orgColumns).contains(dbName)) { 
              /* set itemOrg values to the ItemOrgMap */
              property = itemOrgBean.getDynaClass().getDynaProperty(dbName);
              Class type = property.getType();
              if (rowcell != null && !rowcell.equals("")) {
                if (type == java.lang.String.class) {
                  cellVal = rowcell.getStringCellValue();
                } else if (type == java.lang.Boolean.class) {
                  cellVal = rowcell.getBooleanCellValue();
                }
              }
              itemOrgMap.put(dbName, ConvertUtils.convert(cellVal, property.getType()));
            } else if (actualBedNames.contains(dbName)) {
              String chargeName = columnMap.get(index)[1];
              if (chargeName != null) {
                property = itemChargesBean.getDynaClass().getDynaProperty(chargeName);
                if (rowcell != null && !rowcell.equals("")) {
                  chargeVal = new BigDecimal(rowcell.getNumericCellValue());
                } else {
                  chargeVal = new BigDecimal(0);
                }

                Map<String, BigDecimal> bedMap = eachBedMap.get(dbName);
                if (bedMap != null) {
                  bedMap.put(chargeName, chargeVal);
                } else {
                  bedMap = new HashMap<String, BigDecimal>();
                  bedMap.put(chargeName, chargeVal);
                  eachBedMap.put(dbName, bedMap);
                }
              }
            }

          } catch (Exception convertionException) {

            if (property != null) {
              addError(lineNumber, "Conversion error: Cell value" + " could not be converted to "
                  + property.getType() + " below headers of " + colName.toUpperCase());
            } else {
              addError(lineNumber,
                  "Conversion error: Cell value" + " could not be converted to java.lang.String"
                      + " below headers of " + colName.toUpperCase());
            }
          }

          index++;
        }
        Object id = null;
        if (itemDeptTable != null) {
          id = getDeprartmentId(deptNameValue);
          if (id == null) {
            addError(lineNumber, "Master value not found for the department " + deptNameValue);
          }
        }
        if (itemTable != null) {
          itemBean = getBean(itemNameValue, id);
          if (itemBean != null) {
            idVal = itemBean.get(itemKey);
          } else {
            addError(lineNumber, "Master value not found for " + itemNameValue);
            continue nextLine;
          }
        }

        for (String key : itemWhereFieldKeys) {
          if (key.contains("org")) {
            itemKeys.put(key, orgId);
          }
          if (key.contains(itemKey)) {
            itemKeys.put(key, idVal);
          }
        }

        if (itemOrgDetailsTable != null) {
          for (String key : orgWhereFieldKeys) {
            if (key.contains("org")) {
              orgKeys.put(key, orgId);
            }
            if (key.contains(orgKey)) {
              orgKeys.put(key, idVal);
            }
          }
        }

        for (String key : chargesWhereFieldKeys) {
          if (key.contains("org")) {
            chargeKeys.put(key, orgId);
          }
          if (key.contains(chargeKey)) {
            chargeKeys.put(key, idVal);
          }

        }

        Connection con = null;
        boolean success = false;

        try {
          con = DataBaseUtil.getReadOnlyConnection();
          con.setAutoCommit(false);
          if (itemTable != null && !itemTable.equals("")) {
            success = itemDao.update(con, itemMap, itemKeys) > 0;
          }

          if (itemOrgDetailsTable != null) {
            success &= itemOrgDao.update(con, itemOrgMap, orgKeys) > 0;
          }

          Iterator eachBedMapItr = eachBedMap.entrySet().iterator();
          while (eachBedMapItr.hasNext()) {
            Map.Entry entry = (Map.Entry) eachBedMapItr.next();
            String bedType = (String) entry.getKey();
            chargeKeys.put(bedColumnName, bedType);
            if (userColumnName != null) {
              ((Map) entry.getValue()).put(userColumnName, userNameWithHint);
            }
            if (itemTable == null && itemOrgDetailsTable == null) {
              success = itemChargesDao.update(con, (Map) entry.getValue(), chargeKeys) > 0;
            } else {
              success &= itemChargesDao.update(con, (Map) entry.getValue(), chargeKeys) > 0;
            }

          }
          if (success) {
            con.commit();
          }

        } finally {
          con.close();

        }
      }

    }
  }

  /*
   * private BasicDynaBean getId(String keyColumn, Object identifier, GenericDAO dao) throws
   * SQLException { Connection con = null; BasicDynaBean bean = null; try { con =
   * DataBaseUtil.getReadOnlyConnection(); bean = dao.findByKey(con, keyColumn, identifier);
   * 
   * return bean; } finally { DataBaseUtil.closeConnections(con, null); }
   * 
   * }
   */
  private BasicDynaBean getBean(Object name, Object deptId) throws SQLException {
    StringBuilder builder = new StringBuilder();
    builder.append("SELECT * FROM " + itemTable);
    builder.append(" WHERE " + itemName + "= ?");
    if (itemDeptTable != null) {
      builder.append(" AND " + itemDeptKey + "= ?");
    }
    Connection con = null;
    PreparedStatement pstmt = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      pstmt = con.prepareStatement(builder.toString());
      pstmt.setObject(1, name);
      if (itemDeptTable != null) {
        pstmt.setObject(2, deptId);
      }
      return DataBaseUtil.queryToDynaBean(pstmt);
    } finally {
      DataBaseUtil.closeConnections(con, pstmt);
    }
  }

  private String getdeptIdQuery = "SELECT deptKey FROM itemDeptTable WHERE deptName =?";

  private Object getDeprartmentId(Object departementName) throws SQLException {
    Connection con = null;
    PreparedStatement pstmt = null;
    ResultSet rs = null;
    try {
      getdeptIdQuery = getdeptIdQuery.replace("deptKey", deptKey);
      getdeptIdQuery = getdeptIdQuery.replace("itemDeptTable", itemDeptTable);
      getdeptIdQuery = getdeptIdQuery.replace("deptName", deptNameKey);

      con = DataBaseUtil.getReadOnlyConnection();
      pstmt = con.prepareStatement(getdeptIdQuery);
      pstmt.setString(1, (String) departementName);
      rs = pstmt.executeQuery();
      if (rs.next()) {
        return rs.getObject(1);
      } else {
        return null;
      }
    } finally {
      DataBaseUtil.closeConnections(con, pstmt);
    }
  }

  private static final String BED_TYPES = "SELECT lower(bed_type_name) as bed_type "
      + "from bed_types WHERE " + "billing_bed_type='Y' AND status = 'A'";

  private static final String ALL_BED_TYPES = "SELECT lower(bed_type_name) as bed_type, "
      + "bed_type_name as actual_name from bed_types WHERE "
      + "billing_bed_type='Y' AND status = 'A'";

  HashMap<String, String> originalBedNamesMap = null;

  /**
   * Gets the union of bed types.
   *
   * @return the union of bed types
   * @throws SQLException
   *           the SQL exception
   */
  public static ArrayList<String> getUnionOfBedTypes() throws SQLException {
    ArrayList<String> al = null;
    Connection con = DataBaseUtil.getReadOnlyConnection();
    PreparedStatement ps = con.prepareStatement(BED_TYPES);
    al = DataBaseUtil.queryToOnlyArrayList(ps);
    ArrayList<String> duplicate = new ArrayList<String>();

    Iterator<String> it = al.iterator();
    while (it.hasNext()) {
      String bed = it.next();
      duplicate.add(bed);
    }
    ps.close();
    con.close();
    return duplicate;
  }

  ArrayList<String> actualBedNames = null;

  /**
   * Gets the mapof original bedtypes.
   *
   * @return the mapof original bedtypes
   * @throws SQLException
   *           the SQL exception
   */
  public Map<String, String> getMapofOriginalBedtypes() throws SQLException {
    Connection con = null;
    PreparedStatement pstmt = null;
    ResultSet resultSet = null;
    HashMap<String, String> map = new HashMap<String, String>();
    actualBedNames = new ArrayList<String>();
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      pstmt = con.prepareStatement(ALL_BED_TYPES);
      resultSet = pstmt.executeQuery();
      while (resultSet.next()) {
        map.put(resultSet.getString("bed_type"), resultSet.getString("actual_name"));
        actualBedNames.add(resultSet.getString("actual_name"));
      }
    } finally {
      DataBaseUtil.closeConnections(con, pstmt, resultSet);
    }
    return map;
  }

  private void addError(int line, String msg) {

    if (line > 0) {
      errors.append("Line ").append(line).append(": ");
    } else {
      errors.append("Error in header: ");
    }
    errors.append(msg).append("<br>");
    logger.error("Line " + line + ": " + msg);
  }

}
