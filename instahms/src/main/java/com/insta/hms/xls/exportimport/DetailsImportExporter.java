package com.insta.hms.xls.exportimport;

import com.bob.hms.adminmasters.organization.OrgMasterDao;
import com.bob.hms.common.AutoIncrementId;
import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.insta.hms.adminmasters.bedmaster.BedMasterDAO;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.diagnosticsmasters.addtest.AddTestDAOImpl;
import com.insta.hms.master.ServiceSubGroup.ServiceSubGroupDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.poi.ss.usermodel.DataFormatter;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class DetailsImportExporter {

  static Logger logger = LoggerFactory.getLogger(DetailsImportExporter.class);
  private static final String AUDIT_LOG_HINT = ":XLS";
  private String mainTable;
  private String orgTable;
  private String chargesTable;
  private String tableDbName;
  private String type;
  private String id;
  private String orgId;
  private String bed;
  private String deptName;
  private String practitionerName;
  private String srvSubGrpName;
  private String srvGrpName;
  private String orgTabId;
  private String id4ChgTab;
  private String orgName4ChgTab;
  private String orderType;
  private String dbCodeName;
  private String columnNameForDate;
  private String columnNameForUser;
  private String sequenceName;
  private Object nameAsId;
  private String extraId;
  private String centerName;

  private Map aliasUnmsToDBnmsMap;
  private Map deptMap;
  private Map practitionerMap;
  private Map centerMap;

  private boolean codeAliasReq;
  private boolean deptNotExist;
  private boolean usingHospIdPatterns;
  private boolean usingSequencePattern;
  private boolean isDateRequired;
  private boolean isUserNameRequired;
  private boolean usingUniqueNumber;
  private boolean nameAsIdPattern;

  private List<String> exemptFromNullCheck;
  private List<String> oddFields;
  private List<String> charges;
  private List<String> mandatoryFields;
  private List<String> extraFields;

  private StringBuilder errors;

  private boolean useAuditLogHint;
  private String extraFieldName;
  private String refTableForDupChk;// if duplicate check is not with mainTable, use this.
  private boolean flagForRefDupChk;// set it true if duplicate check should happen with
  // tableForDupChk.
  private int colNumForRefDupChk;
  private String colForRefDupChk;// column for tableForDupChk table
  private DataFormatter formatter = new DataFormatter();

  public String getColForRefDupChk() {
    return colForRefDupChk;
  }

  public void setColForRefDupChk(String colForRefDupChk) {
    this.colForRefDupChk = colForRefDupChk;
  }

  public int getColNumForRefDupChk() {
    return colNumForRefDupChk;
  }

  public void setColNumForRefDupChk(int colNumForRefDupChk) {
    this.colNumForRefDupChk = colNumForRefDupChk;
  }

  public boolean isFlagForRefDupChk() {
    return flagForRefDupChk;
  }

  public void setFlagForRefDupChk(boolean flagForRefDupChk) {
    this.flagForRefDupChk = flagForRefDupChk;
  }

  public String getRefTableForDupChk() {
    return refTableForDupChk;
  }

  public void setRefTableForDupChk(String refTableForDupChk) {
    this.refTableForDupChk = refTableForDupChk;
  }

  public DetailsImportExporter() {
    // TODO Auto-generated constructor stub
  }

  /**
   * Instantiates a new details import exporter.
   *
   * @param mainTable
   *          the main table
   * @param orgTable
   *          the org table
   * @param chargesTable
   *          the charges table
   */
  public DetailsImportExporter(String mainTable, String orgTable, String chargesTable) {

    this.mainTable = mainTable;
    this.orgTable = orgTable;
    this.chargesTable = chargesTable;
    this.useAuditLogHint = false;

  }

  public void setTableDbName(String tableDbName) {
    this.tableDbName = tableDbName;
  }

  public void setType(String type) {
    this.type = type;
  }

  public void setId(String id) {
    this.id = id;
  }

  public void setOrgId(String orgId) {
    this.orgId = orgId;
  }

  public String getExtraId() {
    return extraId;
  }

  public void setExtraId(String extraId) {
    this.extraId = extraId;
  }

  public void setBed(String bed) {
    this.bed = bed;
  }

  public void setDeptName(String deptName) {
    this.deptName = deptName;
  }

  public void setPractitionerName(String practitionerName) {
    this.practitionerName = practitionerName;
  }

  public void setIdForOrgTab(String orgTabId) {
    this.orgTabId = orgTabId;
  }

  public void setSerSubGrpName(String subGrp) {
    this.srvSubGrpName = subGrp;
  }

  public void setSerGrpName(String grp) {
    this.srvGrpName = grp;
  }

  public void setIdForChgTab(String id4ChgTab) {
    this.id4ChgTab = id4ChgTab;
  }

  public void setOrgNameForChgTab(String orgName4ChgTab) {
    this.orgName4ChgTab = orgName4ChgTab;
  }

  public void setSequenceName(String sequenceName) {
    this.sequenceName = sequenceName;
  }

  public void setOrderType(String orderType) {
    this.orderType = orderType;
  }

  public void setDbCodeName(String dbCodeName) {
    this.dbCodeName = dbCodeName;
  }

  public void setColumnNameForDate(String columnNameForDate) {
    this.columnNameForDate = columnNameForDate;
  }

  public void setColumnNameForUser(String columnNameForUser) {
    this.columnNameForUser = columnNameForUser;
  }

  public void setAliasUnmsToDBnmsMap(Map aliasUnmsToDBnmsMap) {
    this.aliasUnmsToDBnmsMap = aliasUnmsToDBnmsMap;
  }

  public void setDeptMap(Map deptMap) {
    this.deptMap = deptMap;
  }

  public void setPractitionerMap(Map practitionerMap) {
    this.practitionerMap = practitionerMap;
  }

  public void setCharges(List<String> charges) {
    this.charges = charges;
  }

  public void setMandatoryFields(List<String> mandatoryFields) {
    this.mandatoryFields = mandatoryFields;
  }

  public void setOddFields(List<String> oddFields) {
    this.oddFields = oddFields;
  }

  public void setExemptFromNullCheck(List<String> exemptFromNullCheck) {
    this.exemptFromNullCheck = exemptFromNullCheck;
  }

  public void setCodeAliasRequired(boolean codeAliasReq) {
    this.codeAliasReq = codeAliasReq;
  }

  public void setDeptNotExist(boolean deptNotExist) {
    this.deptNotExist = deptNotExist;
  }

  public void setUsingHospIdPatterns(boolean usingHospIdPatterns) {
    this.usingHospIdPatterns = usingHospIdPatterns;
  }

  public void setUsingSequencePattern(boolean usingSequencePattern) {
    this.usingSequencePattern = usingSequencePattern;
  }

  public void setIsDateRequired(boolean isDateRequired) {
    this.isDateRequired = isDateRequired;
  }

  public void setIsUserNameRequired(boolean isUserNameRequired) {
    this.isUserNameRequired = isUserNameRequired;
  }

  public void setUsingUniqueNumber(boolean usingUniqueNumber) {
    this.usingUniqueNumber = usingUniqueNumber;
  }

  public void setUseAuditLogHint(boolean useAuditLogHint) {
    this.useAuditLogHint = useAuditLogHint;
  }

  public void seTextraFields(List<String> extraFields) {
    this.extraFields = extraFields;
  }

  private static String INIT_ITEM_ORG_DETAILS = "INSERT INTO doctor_org_details "
      + "(doctor_id, org_id, applicable, base_rate_sheet_id, is_override)"
      + " ( SELECT ?, od.org_id, true, prspv.base_rate_sheet_id, 'N' FROM organization_details od "
      + " LEFT OUTER JOIN priority_rate_sheet_parameters_view prspv ON od.org_id = prspv.org_id )";

  /**
   * Import details to xls.
   *
   * @param sheet
   *          the sheet
   * @param orgid
   *          the orgid
   * @param errors
   *          the errors
   * @param userName
   *          the user name
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public void importDetailsToXls(XSSFSheet sheet, String orgid, StringBuilder errors,
      String userName) throws SQLException, IOException {

    Iterator rowIterator = sheet.rowIterator();
    XSSFRow row1 = (XSSFRow) rowIterator.next();
    String userNameWithHint = ((null == userName) ? "" : userName)
        + ((useAuditLogHint) ? AUDIT_LOG_HINT : "");
    this.errors = errors;

    GenericDAO mainTableDao = new GenericDAO(mainTable);
    BasicDynaBean mainBean = mainTableDao.getBean();
    List<BasicDynaBean> orgList = new OrgMasterDao().getAllOrgIdNames();
    List<String> bedTypes = BedMasterDAO.getUnionOfBedTypes();
    GenericDAO doccendao = new GenericDAO("doctor_center_master");
    GenericDAO docConsultTokendao = new GenericDAO("doctor_consultation_tokens");
    Integer docSubGrpId = null;
    if (mainTable.equals("doctors")) {
      docSubGrpId = (Integer) new ServiceSubGroupDAO().getServiceSubGroupBean("Doctor", "Doctor")
          .get("service_sub_group_id");
    }

    row1.getLastCellNum();
    String[] headers = new String[row1.getLastCellNum()];
    String[] xlHeaders = new String[row1.getLastCellNum()];

    for (int i = 0; i < headers.length; i++) {

      XSSFCell cell = row1.getCell(i);
      if (cell == null) {
        headers[i] = null; /* putting null values, if found */
      } else {

        String header = cell.getStringCellValue().toLowerCase();
        String dbName = (String) (aliasUnmsToDBnmsMap.get(header) == null ? header
            : aliasUnmsToDBnmsMap.get(header));
        headers[i] = dbName;
        xlHeaders[i] = header;

        if (mainBean.getDynaClass().getDynaProperty(dbName) == null
            && (!Arrays.asList(srvGrpName, srvSubGrpName).contains(dbName))
            && !((tableDbName + "_dup_id").equals(dbName))) {
          if (extraFields == null || !extraFields.contains(dbName)) {
            addError(0, "Unknown header found in header " + dbName);
          }
          extraFieldName = headers[i];
          headers[i] = null;
          xlHeaders[i] = null;
        }

      }

    }

    for (String mfield : mandatoryFields) {
      if (!Arrays.asList(headers).contains(mfield)) {
        addError(0, "Mandatory field " + mfield + " is missing cannot process further");
        return;
      }
    }

    Map<String, Object> itemMap = null;
    GenericDAO dao = new GenericDAO(mainTable);
    GenericDAO orgDao = new GenericDAO(orgTable);
    GenericDAO chargeDao = new GenericDAO(chargesTable);
    BasicDynaBean tableBean = dao.getBean();
    BasicDynaBean chgBean = chargeDao.getBean();
    BasicDynaBean orgBean = orgDao.getBean();
    BasicDynaBean itemBean = null;
    BasicDynaBean docCenterBean = null;
    BasicDynaBean docConsultTokenBean = null;
    List<String> inactiveBedList = getInactiveBeds();

    nxtLine: while (rowIterator.hasNext()) {
      XSSFRow row = (XSSFRow) rowIterator.next();
      int lineNumber = row.getRowNum() + 1;
      itemMap = new HashMap<String, Object>();
      // BasicDynaBean bean = null;
      itemBean = dao.getBean();
      // for doctors
      docCenterBean = doccendao.getBean();
      docConsultTokenBean = docConsultTokendao.getBean();
      // String tableId = null;
      Map<String, Object> keys = null;
      String operation = "update";
      Object newId = null;
      Object itemId = null;
      Object extraItemId = null;
      String itemName = null;
      Object itemDept = null;
      Object beanId = null;
      Object grpId = null;
      String subGrpName = null;
      BasicDynaBean existOrNot = null;
      BasicDynaBean subGrpBean = null;
      boolean lineHasErrors = false;
      boolean specialization = false;

      nxtCell: for (int j = 0; j < headers.length; j++) {

        Object cellVal = null;
        DynaProperty property = null;
        XSSFCell rowcell = row.getCell(j);
        String rowcellString = formatter.formatCellValue(rowcell);

        if (headers[j] == null) {
          if (rowcell == null) {
            continue nxtCell;
          }
          extraFieldName = rowcellString;
          continue nxtCell;
        }

        if (!mandatoryFields.contains(headers[j]) && !exemptFromNullCheck.contains(headers[j])) {
          if (rowcell == null || rowcell.toString().equals("")) {
            continue;
          }
        }

        property = tableBean.getDynaClass().getDynaProperty(headers[j]);
        try {
          if (rowcell != null && !rowcell.equals("")
              && !Arrays.asList(srvGrpName, srvSubGrpName).contains(headers[j])) {

            /* check the id */
            if (headers[j].equals(id)) {

              switch (rowcell.getCellType()) {
                case XSSFCell.CELL_TYPE_NUMERIC: {
                  itemId = new Double(rowcell.getNumericCellValue()).intValue();
                  break;
                }
                case XSSFCell.CELL_TYPE_STRING: {
                  itemId = rowcellString;
                  break;
                }
                default:
                  break;
              }
              if (itemId == null) {
                operation = "insert";
              }
              continue nxtCell;

            } else if (headers[j].equals(deptName)) {
              String exlDbName = rowcellString;
              cellVal = deptMap.get(exlDbName);
              itemDept = cellVal;
              if (cellVal == null) {
                addError(lineNumber, "Department " + exlDbName + " not exist");
                lineHasErrors = true;
                /* through error that dept not exist */
              }

            } else if (headers[j].equals(practitionerName)) {
              String exlDbName = rowcellString;
              cellVal = practitionerMap.get(exlDbName);
              itemDept = cellVal;
              if (cellVal == null) {
                addError(lineNumber, "Practitioner " + exlDbName + " not exist");
                lineHasErrors = true;
                /* throw error that practitioner not exist */
              }

            } else if (mainTable.equals("doctors") && headers[j].equals(centerName)) {
              String exlDbName = rowcellString;
              cellVal = centerMap.get(exlDbName);
              if (cellVal == null) {
                addError(lineNumber, "Center " + exlDbName + " not exist");
                lineHasErrors = true;
                /* through error that dept not exist */
              }
            } else if (headers[j].equals(extraId)) {
              switch (rowcell.getCellType()) {
                case XSSFCell.CELL_TYPE_NUMERIC: {
                  extraItemId = new Double(rowcell.getNumericCellValue()).intValue();
                  break;
                }
                case XSSFCell.CELL_TYPE_STRING: {
                  extraItemId = rowcellString;
                  break;
                }
                default:
                  break;
              }
              cellVal = extraItemId;// set extra id value here itself otherwise null will be set
            } else if (mainTable.equals("services") && headers[j].equals("specialization")) {
              specialization = true;
            } else if (oddFields.contains(headers[j])) {
              switch (rowcell.getCellType()) {

                case XSSFCell.CELL_TYPE_NUMERIC: {
                  cellVal = rowcell.getNumericCellValue();
                  break;
                }

                case XSSFCell.CELL_TYPE_STRING: {
                  cellVal = rowcellString;
                  break;
                }

                case XSSFCell.CELL_TYPE_BOOLEAN: {
                  cellVal = rowcell.getBooleanCellValue();
                  break;
                }
                default:
                  break;

              }
            } else if (mainTable.equals("drg_codes_master")
                && headers[j].equals("hcpcs_portion_per")) {
              if (rowcell.getNumericCellValue() > 100) {
                addError(lineNumber, "HCPCS Portion % must be less than or equal to 100");
                lineHasErrors = true;
                break;
              } else if (rowcell.getNumericCellValue() < 0) {
                addError(lineNumber, "HCPCS Portion % must be a valid number");
                lineHasErrors = true;
                break;
              } else {
                cellVal = rowcell.getNumericCellValue();
              }

            } else {
              Class type = property.getType();
              if (type == java.lang.String.class) {
                rowcell.setCellType(XSSFCell.CELL_TYPE_STRING);
                cellVal = rowcellString;
              } else if (type == java.lang.Boolean.class) {
                cellVal = rowcell.getBooleanCellValue();
              } else {
                cellVal = rowcell.getNumericCellValue();
              }
            }

          }
          if (headers[j].equals(id) && cellVal == null) {
            operation = "insert";
          } else if (headers[j].equals(tableDbName)) {

            itemName = (String) cellVal;
            itemBean.set(headers[j], cellVal);

          } else if (headers[j].equals(srvGrpName)) {
            if (rowcell == null || rowcell.equals("")) {
              addError(lineNumber, srvGrpName + " should not be null");
              lineHasErrors = true;
              continue nxtCell;
            }
            cellVal = rowcellString;
            grpId = getGrpId(cellVal.toString());
            if (grpId == null) {
              addError(lineNumber, "Service Group Id not exist");
              lineHasErrors = true;
            }
            continue nxtCell;
          } else if (headers[j].equals(srvSubGrpName)) {
            if (rowcell == null || rowcell.equals("")) {
              addError(lineNumber, srvSubGrpName + " should not be null");
              lineHasErrors = true;
              continue nxtCell;
            }
            subGrpName = rowcellString;
            continue nxtCell;
          } else if (mainTable.equals("doctors") && (headers[j].equals("schedule")
              || headers[j].equals("doctor_type") || headers[j].equals("send_feedback_sms"))) {
            if (headers[j].equals("schedule")) {
              if (rowcell == null) {
                itemBean.set("schedule", false);
              } else {
                itemBean.set("schedule", rowcell.getBooleanCellValue());
              }
            } else if (headers[j].equals("send_feedback_sms")) {
              if (rowcell == null) {
                itemBean.set("send_feedback_sms", false);
              } else {
                itemBean.set("send_feedback_sms", rowcell.getBooleanCellValue());
              }
            } else if (headers[j].equals("doctor_type") && rowcell != null) {
              itemBean.set("doctor_type",
                  ConvertUtils.convert(cellVal, java.lang.StringBuilder.class));
              if (cellVal.equals("CONSULTANT")) {
                itemBean.set("consulting_doctor_flag", "Y");
              } else {
                itemBean.set("consulting_doctor_flag", "N");
              }
            }

          } else if (mainTable.equals("doctors") && headers[j].equals(centerName)) {
            String exlDbName = rowcellString;
            cellVal = centerMap.get(exlDbName);
            // docCenterBean.set("center_id", (cellVal == null || cellVal.equals("")) ? null :
            // Integer.parseInt(cellVal.toString()));
          } else if (mainTable.equals("doctors") && rowcell == null
              && (headers[j].equals("specialization") || headers[j].equals("doctor_mail_id"))) {

            itemBean.set(headers[j], "");
          } else {
            itemBean.set(headers[j], ConvertUtils.convert(cellVal, property.getType()));
          }
          if (mandatoryFields.contains(headers[j]) && cellVal == null) {
            addError(lineNumber, headers[j] + " should not be null");
            lineHasErrors = true;
            continue nxtCell;
          }
        } catch (Exception ex) {

          if (property != null) {
            addError(lineNumber, "Conversion error: Cell value" + " could not be converted to "
                + property.getType() + " below headers of " + headers[j]);
            lineHasErrors = true;
          } else {
            addError(lineNumber,
                "Conversion error: Cell value"
                    + " could not be converted to class java.lang.String below headers of "
                    + headers[j]);
            lineHasErrors = true;
          }
          continue; /* next cell */
        }
      }
      if (itemName != null && itemDept != null) {
        existOrNot = getBean(itemName, itemDept, "A");
      } else if (mainTable.equals("service_sub_groups")) {
        existOrNot = getServicesubgroupBean(itemName, grpId);

      } else if (deptNotExist && tableDbName != null) {
        existOrNot = dao.findByKey(tableDbName, itemName);
      } else if (deptNotExist && flagForRefDupChk) {

        if ((row.getCell(colNumForRefDupChk) == null
            || row.getCell(colNumForRefDupChk).equals(""))) {
          addError(lineNumber, "Item name should not be null");
          lineHasErrors = true;
          continue;
        }

        BasicDynaBean dupRefBean = new GenericDAO(refTableForDupChk).findByKey(colForRefDupChk,
            row.getCell(colNumForRefDupChk).getStringCellValue());
        if (dupRefBean != null) { // item exists in reference table
          existOrNot = dao.findByKey(id, dupRefBean.get(id));
        }
      }
      if (existOrNot != null) {
        beanId = existOrNot.get(nameAsIdPattern ? tableDbName : id);
      }
      if (operation.equals("update") && existOrNot != null) {
        if (!itemId.equals(beanId)) {
          addError(lineNumber, "Duplicate entry cannot updated");
          lineHasErrors = true;
        }
      } else {
        if (beanId != null) {
          addError(lineNumber, "Duplicate entry cannot inserted");
          lineHasErrors = true;
        }
      }
      if (grpId != null && subGrpName != null) {

        subGrpBean = getSubGrpInf(Integer.parseInt(grpId.toString()), subGrpName);
        if (subGrpBean == null) {
          addError(lineNumber, "Group and Subgroup is not matching");
          lineHasErrors = true;
        } else {
          itemBean.set("service_sub_group_id", subGrpBean.get("service_sub_group_id"));
        }
      }
      if (mainTable.equals("service_sub_groups") && grpId != null) {
        itemBean.set("service_group_id", grpId);
      }

      if (lineHasErrors) {
        continue nxtLine;
      }

      if (mainTable.equals("services") && specialization) {
        itemBean.set("specialization", "D");
      } else if (mainTable.equals("doctors")) {
        if (operation.equals("insert")) {
          itemBean.set("service_sub_group_id", docSubGrpId);

        }

      }

      /* updating or inserting part */
      Connection con = null;
      boolean success = false;
      String orderAlias = null;

      try {
        if (codeAliasReq) {
          orderAlias = AddTestDAOImpl.getOrderAlias(orderType, itemDept.toString(),
              grpId.toString(), subGrpBean.get("service_sub_group_id").toString());
        }

        con = DataBaseUtil.getReadOnlyConnection();
        con.setAutoCommit(false);

        if (operation.equals("update")) {
          keys = new HashMap<String, Object>();
          if (nameAsIdPattern) {
            keys.put(tableDbName, itemId);
          } else {
            keys.put(id, itemId);
          }

          if (extraId != null) {
            keys.put(extraId, extraItemId);// extra id for update
          }

          /* update the username and date&time if required */
          if (isDateRequired) {
            itemBean.set(columnNameForDate, DateUtil.getCurrentTimestamp());
          }
          if (isUserNameRequired) {
            itemBean.set(columnNameForUser, userNameWithHint);
          }
          success = dao.update(con, itemBean.getMap(), keys) > 0;

          if (success) {
            con.commit();
            /* insert */
          }
        } else {
          if (usingUniqueNumber) {
            newId = AutoIncrementId.getNewIncrId(id, mainTable.toUpperCase(), type);

          } else if (usingHospIdPatterns) {
            newId = DataBaseUtil.getNextPatternId(mainTable);

          } else if (usingSequencePattern) {
            newId = DataBaseUtil.getNextSequence(sequenceName);
            // itemBean.set("mod_time", DateUtil.getCurrentTimestamp());
          } else if (mainTable.equals("store_item_issue_rates")) {

            BasicDynaBean bean = new GenericDAO("store_item_details").findByKey("medicine_name",
                row.getCell(1).getStringCellValue());
            newId = bean != null ? bean.get("medicine_id") : null;
            if (newId == null) {
              addError(lineNumber, "No item " + extraFieldName + "exists");
              continue nxtLine;
            }
          } else if (nameAsIdPattern) {
            // some masters doesn't have id only item name will be the id
            newId = itemName;
          } else {
            newId = dao.getNextSequence();
          }
          if (nameAsIdPattern) {
            itemBean.set(tableDbName, newId);
          } else {
            itemBean.set(id, newId);
          }
          if (isDateRequired) {
            itemBean.set(columnNameForDate, DateUtil.getCurrentTimestamp());
          }
          if (isUserNameRequired) {
            itemBean.set(columnNameForUser, userName);
          }
          if (codeAliasReq) {
            if (itemBean.get(dbCodeName) == null || itemBean.get(dbCodeName).equals("")) {
              itemBean.set(dbCodeName, orderAlias);
            }
          }
          success = dao.insert(con, itemBean);

          // insert a row into doctor_center_master with a value of cell value or 0;
          if (mainTable.equals("doctors") && success) {
            Integer centerId = 0;
            String doctorId = (String) itemBean.get("doctor_id");
            String status = (String) itemBean.get("status");
            Integer docCenterId = doccendao.getNextSequence();
            docCenterBean.set("doc_center_id", docCenterId);
            docCenterBean.set("center_id", centerId);
            docCenterBean.set("doctor_id", doctorId);
            docCenterBean.set("status", status);
            success = doccendao.insert(con, docCenterBean);
            if (success) {
              docConsultTokenBean.set("doctor_id", doctorId);
              docConsultTokenBean.set("consultation_token", 0);
              success = docConsultTokendao.insert(con, docConsultTokenBean);
            }

          }

          /* insert org details if table is there */
          if (!orgTable.equals("")) {
            for (BasicDynaBean org : orgList) {

              orgBean.set(orgTabId, newId);
              orgBean.set(orgId, org.get("org_id"));
              orgBean.set("applicable", true);
              success &= orgDao.insert(con, orgBean);

            }
          }

          if (!chargesTable.equals("")) {
            for (BasicDynaBean org : orgList) {
              for (String bedName : bedTypes) {

                chgBean.set(id4ChgTab, newId);
                chgBean.set(orgName4ChgTab, org.get("org_id"));
                chgBean.set(bed, bedName);
                for (String chg : charges) {
                  chgBean.set(chg, new BigDecimal(0));
                }
                success &= chargeDao.insert(con, chgBean);

              }
            }

            if (!inactiveBedList.isEmpty()) {
              for (BasicDynaBean org : orgList) {
                for (String bedName : inactiveBedList) {
                  chgBean.set(id4ChgTab, newId);
                  chgBean.set(orgName4ChgTab, org.get("org_id"));
                  chgBean.set(bed, bedName);
                  for (String chg : charges) {
                    chgBean.set(chg, new BigDecimal(0));
                  }
                  success &= chargeDao.insert(con, chgBean);
                }
              }
            }
          }

          if (mainTable.equals("doctors")) {
            List<String> opConsCharges = Arrays.asList("op_charge", "op_revisit_charge",
                "private_cons_charge", "private_cons_revisit_charge");
            GenericDAO opConsChgDao = new GenericDAO("doctor_op_consultation_charge");
            BasicDynaBean opConsChgBean = opConsChgDao.getBean();
            for (BasicDynaBean org : orgList) {
              opConsChgBean.set(id, newId);
              opConsChgBean.set(orgId, org.get("org_id"));
              for (String chg : opConsCharges) {
                opConsChgBean.set(chg, new BigDecimal(0));
              }
              success &= opConsChgDao.insert(con, opConsChgBean);
            }

            PreparedStatement pstmt = con.prepareStatement(INIT_ITEM_ORG_DETAILS);
            pstmt.setObject(1, newId);
            success &= pstmt.executeUpdate() > 0;
          }

        }
      } finally {
        DataBaseUtil.commitClose(con, success);
      }
    }

  }

  private String query = "SELECT * FROM tab WHERE col1 = ? AND col2 = ?";

  /**
   * Gets the bean.
   *
   * @param name
   *          the name
   * @param dept
   *          the dept
   * @param status
   *          the status
   * @return the bean
   * @throws SQLException
   *           the SQL exception
   */
  public BasicDynaBean getBean(String name, Object dept, String status) throws SQLException {

    Connection con = null;
    PreparedStatement pstmt = null;
    StringBuilder builder = null;
    try {
      query = query.replace("tab", mainTable);
      query = query.replace("col1", tableDbName);
      query = query.replace("col2", deptName);

      builder = new StringBuilder(query);
      if (status != null) {
        builder.append(" AND status='A'");
      }
      con = DataBaseUtil.getReadOnlyConnection();
      pstmt = con.prepareStatement(builder.toString());
      pstmt.setString(1, name);
      if (mainTable.equals("services")) {
        pstmt.setInt(2, Integer.parseInt(dept.toString()));
      } else {
        pstmt.setString(2, dept.toString());
      }
      return DataBaseUtil.queryToDynaBean(pstmt);
    } finally {
      DataBaseUtil.closeConnections(con, pstmt);
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

  private static String group = "SELECT service_group_id "
      + "FROM service_groups WHERE service_group_name = ?";

  /**
   * Gets the grp id.
   *
   * @param grpName
   *          the grp name
   * @return the grp id
   * @throws SQLException
   *           the SQL exception
   */
  public Object getGrpId(String grpName) throws SQLException {
    Connection con = null;
    PreparedStatement pstmt = null;
    ResultSet rs = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      pstmt = con.prepareStatement(group);
      pstmt.setString(1, grpName);
      rs = pstmt.executeQuery();
      if (rs.next()) {
        return rs.getInt(1);
      }
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
    return null;
  }

  private static String sub_group = "SELECT service_sub_group_id, service_group_id "
      + "FROM service_sub_groups WHERE " + "service_group_id = ? AND service_sub_group_name = ?";

  /**
   * Gets the sub grp inf.
   *
   * @param grpId
   *          the grp id
   * @param grpName
   *          the grp name
   * @return the sub grp inf
   * @throws SQLException
   *           the SQL exception
   */
  public BasicDynaBean getSubGrpInf(int grpId, String grpName) throws SQLException {
    Connection con = null;
    PreparedStatement pstmt = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      pstmt = con.prepareStatement(sub_group);
      pstmt.setInt(1, grpId);
      pstmt.setString(2, grpName);
      return DataBaseUtil.queryToDynaBean(pstmt);
    } finally {
      DataBaseUtil.closeConnections(con, pstmt);
    }
  }

  private static String inactive_beds = "SELECT DISTINCT intensive_bed_type "
      + "FROM icu_bed_charges WHERE bed_status = 'I'"
      + " UNION ALL SELECT DISTINCT bed_type FROM bed_details WHERE bed_status = 'I'";

  /**
   * Gets the inactive beds.
   *
   * @return the inactive beds
   * @throws SQLException
   *           the SQL exception
   */
  public List<String> getInactiveBeds() throws SQLException {
    Connection con = null;
    PreparedStatement pstmt = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      pstmt = con.prepareStatement(inactive_beds);
      return DataBaseUtil.queryToStringList(pstmt);
    } finally {
      DataBaseUtil.closeConnections(con, pstmt);
    }
  }

  private static String SERVICE_SUB_GROUP = "SELECT * FROM service_sub_groups "
      + "WHERE service_sub_group_name = ? AND service_group_id = ?";

  private BasicDynaBean getServicesubgroupBean(String subGrpName, Object serviceGrpId)
      throws SQLException {
    Connection con = null;
    PreparedStatement pstmt = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      pstmt = con.prepareStatement(SERVICE_SUB_GROUP);
      pstmt.setString(1, subGrpName);
      pstmt.setInt(2, (Integer) serviceGrpId);
      return DataBaseUtil.queryToDynaBean(pstmt);
    } finally {
      DataBaseUtil.closeConnections(con, pstmt);
    }
  }

  private void addError(int line, String msg) {

    if (line > 0) {
      errors.append("Line ").append(line).append(": ");
    } else {
      errors.append("Error in header: ");
    }
    errors.append(msg).append("<br>");
    // logger.error("Line " + line + ": " + msg);
  }

  public boolean isNameAsIdPattern() {
    return nameAsIdPattern;
  }

  public void setNameAsIdPattern(boolean nameAsIdPattern) {
    this.nameAsIdPattern = nameAsIdPattern;
  }

  public Map getCenterMap() {
    return centerMap;
  }

  public void setCenterMap(Map centerMap) {
    this.centerMap = centerMap;
  }

  public String getCenterName() {
    return centerName;
  }

  public void setCenterName(String centerName) {
    this.centerName = centerName;
  }
}