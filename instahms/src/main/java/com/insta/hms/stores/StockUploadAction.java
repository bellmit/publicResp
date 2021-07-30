package com.insta.hms.stores;

import com.bob.hms.common.AutoIncrementId;
import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.master.StoreMaster.StoreMasterDAO;
import com.insta.hms.master.StoresMaster.CategoryMasterDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The Class StockUploadAction.
 */
public class StockUploadAction extends DispatchAction {

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(StockUploadAction.class);

  /** The errors. */
  private static StringBuilder errors;

  /**
   * Show.
   *
   * @param mapping the mapping
   * @param form the form
   * @param req the req
   * @param res the res
   * @return the action forward
   * @throws Exception the exception
   */
  public ActionForward show(ActionMapping mapping, ActionForm form, HttpServletRequest req,
      HttpServletResponse res) throws Exception {

    return mapping.findForward("addshow");
  }
  
  /**
   * Gets the stock sample xls sheets.
   *
   * @param mapping the mapping
   * @param form the form
   * @param req the req
   * @param res the res
   * @return the stock sample xls sheets
   * @throws Exception the exception
   */
  public ActionForward getStockSampleXlsSheets(ActionMapping mapping, ActionForm form,
      HttpServletRequest req, HttpServletResponse res) throws Exception {

    String xlsPath = getServlet().getServletContext()
        .getRealPath("/WEB-INF/stockSampleXlsSheets/stockSampleTemplate.xls");
    InputStream inp = null;
    java.io.OutputStream os = null;
    try {
      inp = new FileInputStream(xlsPath);
      res.setHeader("Content-type", "application/vnd.ms-excel");
      res.setHeader("Content-disposition", "attachment; filename=StockSampleTemplate.xls");
      res.setHeader("Readonly", "true");
      os = res.getOutputStream();
      HSSFWorkbook wb = new HSSFWorkbook(inp);
      wb.write(os);
      os.flush();
    } finally {
      if (inp != null) {
        inp.close();
      }
      if (os != null) {
        os.close();
      }
    }

    return null;

  }
  
  /**
   * Import stock details from xls.
   *
   * @param mapping the mapping
   * @param form the form
   * @param req the req
   * @param res the res
   * @return the action forward
   * @throws Exception the exception
   */
  public ActionForward importStockDetailsFromXls(ActionMapping mapping, ActionForm form,
      HttpServletRequest req, HttpServletResponse res) throws Exception {

    List<BasicDynaBean> insertStock = new ArrayList<BasicDynaBean>();

    List<String> supplireMasterMandatoryFields = Arrays
        .asList(new String[] { "supplier_name", "cust_supplier_code" });
    List<String> manfMasterMandatoryFields = Arrays
        .asList(new String[] { "manf_name", "manf_mnemonic" });
    List<String> genericNameMandatoryFields = Arrays.asList(new String[] { "generic_code" });
    List<String> storeItemDetailsMandatoryFields = Arrays
        .asList(new String[] { "medicine_name", "manf_name", "issue_base_unit", "med_category_id",
            "medicine_short_name", "cust_item_code" });
    List<String> storeStockDetailsMandatoryFields = Arrays
        .asList(new String[] { "medicine_id", "batch_no", "qty", "mrp", "package_sp", "package_cp",
            "username", "change_source", "dept_id" });
    List<String> sheetNames = Arrays.asList(new String[] { "supplier_master", "manf_master",
        "generic_name", "store_item_details", "store_stock_details" });
    HashMap mandatoryFieldsHashMap = new HashMap();
    mandatoryFieldsHashMap.put("supplier_master", supplireMasterMandatoryFields);
    mandatoryFieldsHashMap.put("manf_master", manfMasterMandatoryFields);
    mandatoryFieldsHashMap.put("generic_name", genericNameMandatoryFields);
    mandatoryFieldsHashMap.put("store_item_details", storeItemDetailsMandatoryFields);
    mandatoryFieldsHashMap.put("store_stock_details", storeStockDetailsMandatoryFields);

    Connection con = null;
    int lineNo = 0;
    boolean batchSuccess = true;
    boolean gotSqlException = false;
    String sheetName = null;
    HSSFCell cellvalue = null;
    HSSFCell cellVal = null;
    FlashScope flash = FlashScope.getScope(req);
    ActionRedirect redirect = new ActionRedirect(mapping.findForward("addRedirect"));
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

    try {
      int noOfCells = 0;
      int startNum = 1;
      ArrayList columnNames = new ArrayList();
      StockUploadForm af = (StockUploadForm) form;
      StoreMasterDAO dao = new StoreMasterDAO();
      CategoryMasterDAO catDao = new CategoryMasterDAO();
      ManufacturermasterDAO mdao = new ManufacturermasterDAO();
      HashMap storeHashMap = new HashMap();
      HashMap manufacturerHashMap = new HashMap();
      HashMap storeCategoryHashMap = new HashMap();
      java.sql.Date expDate = null;
      storeHashMap = dao.getStoreDetails();
      ByteArrayInputStream input = new ByteArrayInputStream(af.getStockXlsFile().getFileData());
      HSSFWorkbook wb = new HSSFWorkbook(input);
      int numOfSheets = wb.getNumberOfSheets();

      outer : {
        HSSFSheet sheet = wb.getSheetAt(0);
        if (sheet == null) {
          batchSuccess = false;
          break outer;
        }
        Iterator rows = sheet.rowIterator();
        this.errors = new StringBuilder();
        for (int sheetnum = 0; sheetnum < numOfSheets; sheetnum++) {
          if (wb.getSheetName(sheetnum).equalsIgnoreCase("store_item_details")) {
            manufacturerHashMap = mdao.getManufacturerDetails();
            storeCategoryHashMap = catDao.getStoreCategoryDetails();
          }
          sheet = wb.getSheetAt(sheetnum);
          sheetName = wb.getSheetName(sheetnum);
          int noRows = sheet.getPhysicalNumberOfRows();
          columnNames.clear();
          insertStock.clear();
          rows = sheet.rowIterator();

          if (wb.getSheetName(sheetnum).equalsIgnoreCase(sheetNames.get(sheetnum))) {

            while (rows.hasNext()) {
              Map rowWiseCellDetails = new HashMap();
              HSSFRow row = (HSSFRow) rows.next();

              if (row.getRowNum() == 0) { // ignore header row
                noOfCells = row.getPhysicalNumberOfCells(); // number of cells
                // add column names to arraylist
                for (int cellNo = 0; cellNo < noOfCells; cellNo++) {
                  columnNames.add(row.getCell(cellNo).toString().toLowerCase());
                }
                if (wb.getSheetName(sheetnum).equalsIgnoreCase("manf_master")) {
                  batchSuccess = StockUploadDAO.resetSequence("manufacturer_id_seq", startNum);
                }

                if (wb.getSheetName(sheetnum).equalsIgnoreCase("generic_name")) {
                  batchSuccess = StockUploadDAO.resetSequence("generic_sequence", startNum);
                }

                if (wb.getSheetName(sheetnum).equalsIgnoreCase("store_item_details")) {
                  batchSuccess = StockUploadDAO.resetSequence("item_id_seq", startNum);
                }
                // no need to add the values to bean skip the row.
                continue;
              }
              // add to map row wise values.
              for (int cellNo = 0; cellNo < noOfCells; cellNo++) {
                HSSFCell cell = row.getCell(cellNo);
                String[] value = new String[1];
                if (cell == null) {
                  value[0] = null;
                  rowWiseCellDetails.put(columnNames.get(cellNo).toString(), value);
                  continue;
                }
                switch (cell.getCellType()) {

                  case HSSFCell.CELL_TYPE_NUMERIC : {

                    if (HSSFDateUtil.isCellDateFormatted(cell)) {
                      SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
                      expDate = DataBaseUtil.parseDate(sdf.format(cell.getDateCellValue()));
                    } else {
                      value[0] = (BigDecimal.valueOf(cell.getNumericCellValue())).toString();
                      rowWiseCellDetails.put(columnNames.get(cellNo).toString(), value);
                    }
                    break;
                  }
                  case HSSFCell.CELL_TYPE_STRING : {

                    HSSFRichTextString richTextString = cell.getRichStringCellValue();
                    value[0] = richTextString.getString();
                    rowWiseCellDetails.put(columnNames.get(cellNo).toString(), value);

                    break;
                  }
                  case HSSFCell.CELL_TYPE_BOOLEAN : {

                    Boolean[] booleanValue = new Boolean[1];
                    booleanValue[0] = cell.getBooleanCellValue();
                    rowWiseCellDetails.put(columnNames.get(cellNo).toString(), booleanValue);

                    break;
                  }
                  default : {
                    break;
                  }
                }
              }
              if (wb.getSheetName(sheetnum).equalsIgnoreCase("supplier_master")) {
                String[] value = new String[1];
                value[0] = AutoIncrementId.getSequenceId("manufacturer_id_seq", "Supplier Code");
                rowWiseCellDetails.put("supplier_code", value);

                if (noRows == row.getRowNum()) {
                  batchSuccess = StockUploadDAO.resetSequence("manufacturer_id_seq", startNum);
                }
              }
              if (wb.getSheetName(sheetnum).equalsIgnoreCase("manf_master")) {
                String[] value = new String[1];
                value[0] = AutoIncrementId.getSequenceId("manufacturer_id_seq", "Manufacturer");
                rowWiseCellDetails.put("manf_code", value);
              }
              if (wb.getSheetName(sheetnum).equalsIgnoreCase("generic_name")) {
                String[] value = new String[1];
                value[0] = AutoIncrementId.getSequenceId("generic_sequence", "GENERICNAME");
                rowWiseCellDetails.put("generic_code", value);
              }
              if (wb.getSheetName(sheetnum).equalsIgnoreCase("store_item_details")) {
                Integer[] value = new Integer[1];
                value[0] = DataBaseUtil.getNextSequence("item_id_seq");
                rowWiseCellDetails.put("medicine_id", value);
                String manufCode = (String) manufacturerHashMap
                    .get(((String[]) rowWiseCellDetails.get("manf_name"))[0].trim());

                if (manufCode != null) {
                  String[] manfCode = new String[1];
                  manfCode[0] = manufCode;
                  rowWiseCellDetails.put("manf_name", manfCode);
                } else {
                  batchSuccess = false;
                  addError(row.getRowNum(), "Manufacturer name does't have a relation with master."
                      + "Manufacturer name:" + ((String[]) rowWiseCellDetails.get("manf_name"))[0]);
                }
                String[] categoryId = new String[1];
                String catId = (String) storeCategoryHashMap
                    .get(((String[]) rowWiseCellDetails.get("med_category_id"))[0].trim());

                if (catId != null) {
                  categoryId[0] = catId;
                  rowWiseCellDetails.put("med_category_id", categoryId);
                } else {
                  batchSuccess = false;
                  addError(row.getRowNum(),
                      "Store category master name does't have a "
                          + "relation with master.Store category name:" + ""
                          + ((String[]) rowWiseCellDetails.get("med_category_id"))[0]);
                }
              }

              if (wb.getSheetName(sheetnum).equalsIgnoreCase("store_stock_details")) {
                String storeId = (String) storeHashMap
                    .get(((String[]) rowWiseCellDetails.get("dept_id"))[0]);
                logger.debug(storeId);
                if (storeId != null) {
                  String[] storeID = new String[1];
                  storeID[0] = storeId;
                  rowWiseCellDetails.put("dept_id", storeID);
                } else {
                  batchSuccess = false;
                  addError(row.getRowNum(), "Store name does't have a relation with master."
                      + "Store name:" + ((String[]) rowWiseCellDetails.get("dept_id"))[0]);
                }
                BasicDynaBean medicineBean = new GenericDAO("store_item_details").findByKey(
                    "medicine_name", ((String[]) rowWiseCellDetails.get("medicine_id"))[0].trim());
                if (medicineBean != null) {
                  String[] medicineId = new String[1];
                  medicineId[0] = Integer.toString((Integer) medicineBean.get("medicine_id"));
                  rowWiseCellDetails.put("medicine_id", medicineId);
                } else {
                  batchSuccess = false;
                  addError(row.getRowNum(), "Medicine name does't have a relation with master."
                      + "Medicine name:" + ((String[]) rowWiseCellDetails.get("medicine_id"))[0]);
                }
              }
              // check for mandatory fields if they are null then add to addErrors.
              List mandatoryFieldList = (List) mandatoryFieldsHashMap.get(sheetNames.get(sheetnum));
              for (int l = 0; l < mandatoryFieldList.size(); l++) {
                String checkCellVal = ((String[]) rowWiseCellDetails
                    .get(mandatoryFieldList.get(l)))[0];
                if (checkCellVal == null) {
                  batchSuccess = false;
                  addError(row.getRowNum() + 1,
                      "Null Value found  at headers:" + mandatoryFieldList.get(l));
                }
              }
              BasicDynaBean insertBean = new GenericDAO(sheetNames.get(sheetnum)).getBean();
              ConversionUtils.copyToDynaBean(rowWiseCellDetails, insertBean);
              if (wb.getSheetName(sheetnum).equalsIgnoreCase("store_stock_details")) {
                insertBean.set("exp_dt", expDate);
              }
              insertStock.add(insertBean);
            }
            if (insertStock.size() > 0 && batchSuccess) {
              int batch = 0;
              con = DataBaseUtil.getConnection();
              con.setAutoCommit(false);

              GenericDAO insertDao = new GenericDAO(sheetNames.get(sheetnum));
              GenericDAO deleteDao = new GenericDAO(sheetNames.get(sheetnum));

              batchSuccess = deleteDao.deleteAll(con);

              if (batchSuccess) {
                for (BasicDynaBean stockBean : insertStock) {
                  batchSuccess = insertDao.insert(con, stockBean);
                  if (!batchSuccess) {
                    break outer;
                  }
                  batch++;
                  lineNo++;
                  if (batch > 1000) {
                    DataBaseUtil.commitClose(con, true);
                    con = DataBaseUtil.getConnection();
                    con.setAutoCommit(false);
                    batch = 0;
                    logger.debug("Stock Details, Committing batch ...");
                  }
                } // end of inner for loop
              } // end of inner if loop
            } // end of outer if loop
            DataBaseUtil.commitClose(con, batchSuccess);
          } else { // end of outer if loop
            batchSuccess = false;
            addError(1, " Excel Sheets are not in prorper format."
                + "Please check stock sample template and follow the format. ");
          }
        } // end of for loop
      }
    } catch (SQLException ex) {

      batchSuccess = false;
      gotSqlException = true;

      if (DataBaseUtil.isDuplicateViolation(ex)) {

        flash.put("error", " At sheet " + sheetName + " got duplicate: at line " + lineNo);

      } else if (DataBaseUtil.isForeignKeyViolation(ex)) {

        flash.put("error", " Unable to delete data from table " + sheetName + " please check"
            + " table constraints and try again.");
      } else {
        throw (ex);
      }
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
    if (!batchSuccess && !gotSqlException) {
      if (this.errors == null || this.errors.length() == 0) {
        flash.put("error", "Failed to upload Stock details, some codes may be non-existent.");
      } else {
        flash.put("error", this.errors);
      }
    } else if (batchSuccess) {
      flash.put("info", "Stock Details successfully uploaded.");
    }
    return redirect;
  }
  
  /**
   * Adds the error.
   *
   * @param line the line
   * @param msg the msg
   */
  private void addError(int line, String msg) {
    if (line > 0) {
      this.errors.append("Line ").append(line).append(": ");
    } else {
      this.errors.append("Error in header: ");
    }
    this.errors.append(msg).append("<br>");
    logger.error("Line " + line + ": " + msg);
  }

}