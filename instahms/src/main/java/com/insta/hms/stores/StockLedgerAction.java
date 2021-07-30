package com.insta.hms.stores;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.DynaBeanMapDecorator;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class StockLedgerAction extends DispatchAction {
  
  private static final GenericDAO storesDAO = new GenericDAO("stores");

  public static final String CHECK_POINT_NOW = "-1";
  @IgnoreConfidentialFilters
  public ActionForward list(ActionMapping mapping, ActionForm form, HttpServletRequest req,
      HttpServletResponse response) throws Exception {

    JSONSerializer js = new JSONSerializer().exclude("class");
    req.setAttribute("checkpointJSON", js.serialize(
        ConversionUtils.listBeanToListMap(StoresStockCheckPointDAO.getCheckPointNames())));
    req.setAttribute("checkPoints",
        ConversionUtils.listBeanToListMap(StoresStockCheckPointDAO.getCheckPointNames()));

    HashMap filter = new HashMap();
    req.setAttribute("medicine_timestamp", MedicineStockDAO.getMedicineTimestamp());
    req.setAttribute("medicineId",
        MedicineStockDAO.medicineNameToId(req.getParameter("medicineName")));
    filter.put("medicineName", req.getParameter("medicineName"));
    filter.put("store_id", req.getParameter("store_id"));
    filter.put("fromDate", DataBaseUtil.parseDate(req.getParameter("fromDate")));
    filter.put("toDate", DataBaseUtil.parseDate(req.getParameter("toDate")));
    filter.put("fromCheckPt", req.getParameter("fromCheckPt"));
    filter.put("toCheckPt", req.getParameter("toCheckPt"));
    filter.put("batchno", req.getParameter("batchno"));

    String dept_id = req.getParameter("store_id");
    String exportType = req.getParameter("export_type");
    boolean exportRequest = false;
    if (null != exportType && !exportType.equals("")) {
      exportRequest = true;
    }
    if (dept_id != null) {
      BasicDynaBean dept = storesDAO.findByKey("dept_id", Integer.parseInt(dept_id));
      String dept_name = dept.get("dept_name").toString();
      req.setAttribute("dept_id", dept_id);
      req.setAttribute("dept_name", dept_name);
      req.setAttribute("storeUnit", dept.get("sale_unit"));

    }

    List checkPontList = StoresStockCheckPointDAO.getAllCheckpoints();
    Iterator it = checkPontList.iterator();
    if (req.getParameter("fromCheckPt") != null && !req.getParameter("fromCheckPt").equals("")) {
      while (it.hasNext()) {
        Hashtable tab = (Hashtable) it.next();
        if (req.getParameter("fromCheckPt").equals(tab.get("CHECKPOINT_ID")))
          filter.put("fromChkDate", DataBaseUtil.parseTimestamp((String) tab.get("CHECKPOINT_DATE")));
        if (req.getParameter("toCheckPt").equals(tab.get("CHECKPOINT_ID")))
          filter.put("toChkDate", DataBaseUtil.parseTimestamp((String) tab.get("CHECKPOINT_DATE")));
      }
      if (req.getParameter("toCheckPt").equals(CHECK_POINT_NOW)) {
        filter.put("toChkDate", DataBaseUtil.getDateandTime());
      }
    }

    List openStockList = null;
    List closingStock = null;
    List transitStock = null;
    List rejectedStock = null;
    if (req.getParameter("fromCheckPt") != null && !req.getParameter("fromCheckPt").equals("")) {
      openStockList = StockLedgerDAO.getOpenAndClose(filter, "O");
      req.setAttribute("openStockList", ConversionUtils.copyListDynaBeansToMap(openStockList));
      if (!req.getParameter("toCheckPt").equals(CHECK_POINT_NOW)) {
        closingStock = StockLedgerDAO.getOpenAndClose(filter, "C");
        req.setAttribute("closingStock", ConversionUtils.copyListDynaBeansToMap(closingStock));

        transitStock = StockLedgerDAO.getOpenAndClose(filter, "T");
        req.setAttribute("transitStock", ConversionUtils.copyListDynaBeansToMap(transitStock));

        rejectedStock = StockLedgerDAO.getOpenAndClose(filter, "RJ");
        req.setAttribute("rejectedStock", ConversionUtils.copyListDynaBeansToMap(rejectedStock));
      }
    }

    if (req.getParameter("period") != null && !req.getParameter("period").equals("")) {
      if (req.getParameter("period").equalsIgnoreCase("history")) {
        closingStock = StockLedgerDAO.getOpenAndClose(filter, "CM");
        req.setAttribute("closingStock", ConversionUtils.copyListDynaBeansToMap(closingStock));

        transitStock = StockLedgerDAO.getOpenAndClose(filter, "TM");
        req.setAttribute("transitStock", ConversionUtils.copyListDynaBeansToMap(transitStock));

        rejectedStock = StockLedgerDAO.getOpenAndClose(filter, "RJM");
        req.setAttribute("rejectedStock", ConversionUtils.copyListDynaBeansToMap(rejectedStock));
      }
    }

    if (req.getParameter("toCheckPt") != null && !req.getParameter("toCheckPt").equals("")) {
      if (req.getParameter("toCheckPt").equals(CHECK_POINT_NOW)) {
        closingStock = StockLedgerDAO.getOpenAndClose(filter, "CM");
        req.setAttribute("closingStock", ConversionUtils.copyListDynaBeansToMap(closingStock));

        transitStock = StockLedgerDAO.getOpenAndClose(filter, "TM");
        req.setAttribute("transitStock", ConversionUtils.copyListDynaBeansToMap(transitStock));

        rejectedStock = StockLedgerDAO.getOpenAndClose(filter, "RJM");
        req.setAttribute("rejectedStock", ConversionUtils.copyListDynaBeansToMap(rejectedStock));
      }
    }

    BasicDynaBean hosp = new GenericDAO("generic_preferences").getRecord();
    if (hosp.get("hospital_tin") != null)
      req.setAttribute("hosp_tin", hosp.get("hospital_tin").toString());
    if (hosp.get("hospital_pan") != null)
      req.setAttribute("hosp_pan", hosp.get("hospital_pan").toString());
    if (hosp.get("hospital_service_regn_no") != null)
      req.setAttribute("hosp_ser_reg_no", hosp.get("hospital_service_regn_no").toString());
    Map<LISTING, Object> listing = ConversionUtils.getListingParameter(req.getParameterMap());
    if (exportRequest) {
      listing.put(LISTING.PAGENUM, 0);
      listing.put(LISTING.PAGESIZE, 0);
    }

    PagedList list = StockLedgerDAO.getPurchases(filter, listing);
    if (exportRequest && exportType.equals("EXCEL")) {
      exportToExcel(list, response, closingStock, transitStock, rejectedStock);
      return null;
    } else if (exportRequest) {
      exportToCsv(list, response, closingStock, transitStock, rejectedStock);
      return null;
    }
    req.setAttribute("pagedList", list);
    exportType = null;
    return mapping.findForward("list");
  }

  private void exportToCsv(PagedList list, HttpServletResponse response,
      List<BasicDynaBean> closingStock, List<BasicDynaBean> transitStock,
      List<BasicDynaBean> rejectedStock) throws IOException {

    List<String> excelColumnNames = Arrays.asList(new String[] { "Txn Date", "Batch", "User",
        "Txn Type", "Txn Ref#", "Details", "MRP(Pkg)", "Bonus Qty", "Total Qty" });
    File tempcsvfile = new File("tempcsvfile.csv");
    FileWriter fileWriter = null;
    try {
      fileWriter = new FileWriter(tempcsvfile);
      for (String string : excelColumnNames) {
        fileWriter.append(string);
        fileWriter.append(',');
      }
      fileWriter.append("\n");
      fileWriter.append("\n");
      List<BasicDynaBean> basicDynaBean = new ArrayList<BasicDynaBean>();
      BasicDynaBean dynaBean = null;
      List<DynaBeanMapDecorator> dynabeanMapDecoratorList = list.getDtoList();
      for (DynaBeanMapDecorator basicDynaBean2 : dynabeanMapDecoratorList) {
        fileWriter.append(basicDynaBean2.get("txn_date").toString()).append(',');
        fileWriter.append(basicDynaBean2.get("batch_no").toString()).append(',');
        fileWriter.append(basicDynaBean2.get("user_name").toString()).append(',');
        fileWriter.append(basicDynaBean2.get("txn_type").toString()).append(',');
        fileWriter.append(basicDynaBean2.get("txn_ref").toString()).append(',');
        fileWriter.append(basicDynaBean2.get("details").toString().replace(',', ' ')).append(',');
        fileWriter.append(basicDynaBean2.get("mrp").toString()).append(',');
        fileWriter.append(basicDynaBean2.get("bonus_qty").toString()).append(',');
        fileWriter.append(basicDynaBean2.get("total_qty").toString()).append(',');
        fileWriter.append("\n");
      }
      if (null != closingStock && null != transitStock && null != rejectedStock) {
        for (BasicDynaBean closingBean : closingStock) {
          fileWriter.append("  ").append(',');
          fileWriter.append(closingBean.get("batch_no").toString()).append(',');
          fileWriter.append("  ").append(',');
          fileWriter.append("Closing Stock").append(',');
          fileWriter.append("  ").append(',');
          fileWriter.append("  ").append(',');
          fileWriter.append(closingBean.get("mrp").toString()).append(',');
          fileWriter.append("  ").append(',');
          fileWriter.append(closingBean.get("qty").toString()).append(',');
          fileWriter.append("\n");
        }
        for (BasicDynaBean transitQty : transitStock) {
          fileWriter.append("  ").append(',');
          fileWriter.append(transitQty.get("batch_no").toString()).append(',');
          fileWriter.append("  ").append(',');
          fileWriter.append("Transit Qty").append(',');
          fileWriter.append("  ").append(',');
          fileWriter.append("  ").append(',');
          fileWriter.append(transitQty.get("mrp").toString()).append(',');
          fileWriter.append("  ").append(',');
          fileWriter.append(transitQty.get("transit_qty").toString()).append(',');
          fileWriter.append("\n");
        }
        for (BasicDynaBean rejectedQty : rejectedStock) {
          fileWriter.append("  ").append(',');
          fileWriter.append(rejectedQty.get("batch_no").toString()).append(',');
          fileWriter.append("  ").append(',');
          fileWriter.append("Rejected Qty").append(',');
          fileWriter.append("  ").append(',');
          fileWriter.append("  ").append(',');
          fileWriter.append(rejectedQty.get("mrp").toString()).append(',');
          fileWriter.append("  ").append(',');
          fileWriter.append(rejectedQty.get("qty_in_rejection").toString()).append(',');
          fileWriter.append("\n");
        }
      }
    } finally {
      if (fileWriter != null) {
        fileWriter.close(); 
      }
    }

    ServletOutputStream outputStream = response.getOutputStream();
    response.setHeader("Content-type", "text/csv");
    response.setHeader("Content-disposition",
        "attachment; filename=store_stock_ledger_details.csv");
    response.setHeader("Readonly", "true");
    InputStream inputStream = null;
    try {
      inputStream = new FileInputStream(tempcsvfile);
      byte[] buf = new byte[8192];
      int c = 0;
      while ((c = inputStream.read(buf, 0, buf.length)) > 0) {
        outputStream.write(buf, 0, c);
      }
      outputStream.flush();
    } finally {
      if (inputStream != null) {
        inputStream.close();
      }
    }

  }

  private void exportToExcel(PagedList list, HttpServletResponse response,
      List<BasicDynaBean> closingStock, List<BasicDynaBean> transitStock,
      List<BasicDynaBean> rejectedStock) throws IOException {
    List<String> excelColumnNames = Arrays.asList(new String[] { "Txn Date", "Batch", "User",
        "Txn Type", "Txn Ref#", "Details", "MRP(Pkg)", "Bonus Qty", "Total Qty" });
    List<BasicDynaBean> basicDynaBean = new LinkedList<BasicDynaBean>();
    BasicDynaBean dynaBean = null;
    List<DynaBeanMapDecorator> dynabeanMapDecoratorList = list.getDtoList();

    for (DynaBeanMapDecorator basicDynaBean2 : dynabeanMapDecoratorList) {
      dynaBean = (BasicDynaBean) basicDynaBean2.getDynaBean();
      basicDynaBean.add(dynaBean);
    }
    if (null != closingStock && null != transitStock && null != rejectedStock) {
      for (BasicDynaBean closingStockBean : closingStock) {
        closingStockBean.set("package_uom", "closing_stock");
        basicDynaBean.add(closingStockBean);
      }
      for (BasicDynaBean transitQty : transitStock) {
        transitQty.set("package_uom", "transit_qty");
        basicDynaBean.add(transitQty);
      }
      for (BasicDynaBean rejectedQty : rejectedStock) {
        rejectedQty.set("package_uom", "qty_in_rejection");
        basicDynaBean.add(rejectedQty);
      }
    }
    HSSFWorkbook workbook = new HSSFWorkbook();
    HSSFSheet diagWorkSheet = workbook.createSheet("STOCK_LEDGER");
    Map<String, List> columnNamesMap = new HashMap<String, List>();
    columnNamesMap.put("mainItems", excelColumnNames);
    createCellsWithValues(basicDynaBean, columnNamesMap, diagWorkSheet);
    ServletOutputStream outputStream = response.getOutputStream();
    response.setHeader("Content-type", "application/vnd.ms-excel");
    response.setHeader("Content-disposition",
        "attachment; filename=store_stock_ledger_details.xls");
    response.setHeader("Readonly", "true");
    workbook.write(outputStream);
    outputStream.flush();
    outputStream.close();
  }

  private void createCellsWithValues(List<BasicDynaBean> basicDynaBean,
      Map<String, List> columnNamesMap, HSSFSheet diagWorkSheet) {
    HSSFRow row = diagWorkSheet.createRow(0);
    HSSFWorkbook workbook = diagWorkSheet.getWorkbook();
    int b = 0;
    List<String> mainItems = (List<String>) columnNamesMap.get("mainItems");
    if (mainItems != null) {
      HSSFCell cell = null;
      for (int i = 0; i < mainItems.size(); i++) {
        cell = row.createCell(b);
        cell.setCellValue(new HSSFRichTextString(mainItems.get(i)));
        HSSFCellStyle styleHeader = workbook.createCellStyle();
        styleHeader.setFillBackgroundColor(HSSFColor.DARK_YELLOW.index);
        HSSFFont font = workbook.createFont();
        font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        styleHeader.setFont(font);
        cell.setCellStyle(styleHeader);

        b++;
      }
    }

    HSSFCellStyle leftRightBorderStyle = workbook.createCellStyle();
    leftRightBorderStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
    leftRightBorderStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);
    leftRightBorderStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);

    HSSFCellStyle rightBorderStyle = workbook.createCellStyle();
    rightBorderStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);

    HSSFCellStyle st = row.getSheet().getWorkbook().createCellStyle();
    rightBorderStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);
    rightBorderStyle.setLocked(false);
    st.setLocked(false);

    HSSFCellStyle styleBody = workbook.createCellStyle();
    styleBody.setFillBackgroundColor(HSSFColor.AQUA.index);
    int k = 0;
    for (int i = 0; i < basicDynaBean.size(); i++) {
      int l = 0;
      if (i == 0) {
        k += 2;
      } else {
        k += 1;
      }

      row = diagWorkSheet.createRow(k);
      BasicDynaBean bean = basicDynaBean.get(i);
      String type = (String) bean.get("package_uom");
      HSSFCell cellRunway = null;
      if (bean.getMap().containsKey("txn_date")) {
        cellRunway = row.createCell(l);
        cellRunway.setCellType(HSSFCell.CELL_TYPE_STRING);
        java.sql.Timestamp data = (java.sql.Timestamp) bean.getMap().get("txn_date");
        cellRunway.setCellValue(new HSSFRichTextString(bean.getMap().get("txn_date").toString()));
        cellRunway.setCellStyle(styleBody);
      }

      if (bean.getMap().containsKey("batch_no")) {
        cellRunway = row.createCell(++l);
        cellRunway.setCellType(HSSFCell.CELL_TYPE_STRING);
        cellRunway.setCellValue(
            new HSSFRichTextString(bean.getMap().get("batch_no").toString().toUpperCase()));
        cellRunway.setCellStyle(styleBody);
      } else {
        ++l;
      }

      if (bean.getMap().containsKey("user_name")) {
        cellRunway = row.createCell(++l);
        cellRunway.setCellType(HSSFCell.CELL_TYPE_STRING);
        cellRunway.setCellValue(new HSSFRichTextString(bean.getMap().get("user_name").toString()));
        cellRunway.setCellStyle(styleBody);
      } else {
        ++l;
      }

      if (bean.getMap().containsKey("txn_type")) {
        cellRunway = row.createCell(++l);
        cellRunway.setCellType(HSSFCell.CELL_TYPE_STRING);
        cellRunway.setCellValue(new HSSFRichTextString(bean.getMap().get("txn_type").toString()));
        cellRunway.setCellStyle(styleBody);
      } else if (type.equals("closing_stock")) {
        cellRunway = row.createCell(++l);
        cellRunway.setCellType(HSSFCell.CELL_TYPE_STRING);
        cellRunway.setCellValue(new HSSFRichTextString("Closing Stock"));
        cellRunway.setCellStyle(styleBody);
      } else if (type.equals("transit_qty")) {
        cellRunway = row.createCell(++l);
        cellRunway.setCellType(HSSFCell.CELL_TYPE_STRING);
        cellRunway.setCellValue(new HSSFRichTextString("Transit Qty"));
        cellRunway.setCellStyle(styleBody);
      } else {
        ++l;
      }

      if (bean.getMap().containsKey("txn_ref")) {
        cellRunway = row.createCell(++l);
        cellRunway.setCellType(HSSFCell.CELL_TYPE_STRING);
        cellRunway.setCellValue(new HSSFRichTextString(bean.getMap().get("txn_ref").toString()));
      } else {
        ++l;
      }

      if (bean.getMap().containsKey("details")) {
        cellRunway = row.createCell(++l);
        cellRunway.setCellType(HSSFCell.CELL_TYPE_STRING);
        cellRunway.setCellValue(new HSSFRichTextString(bean.getMap().get("details").toString()));
        cellRunway.setCellStyle(styleBody);
      } else {
        ++l;
      }
      if (bean.getMap().containsKey("mrp")) {
        cellRunway = row.createCell(++l);
        cellRunway.setCellType(HSSFCell.CELL_TYPE_STRING);
        cellRunway.setCellValue(new HSSFRichTextString(bean.getMap().get("mrp").toString()));
        cellRunway.setCellStyle(styleBody);

      }

      if (bean.getMap().containsKey("bonus_qty")) {
        cellRunway = row.createCell(++l);
        cellRunway.setCellType(HSSFCell.CELL_TYPE_STRING);
        cellRunway.setCellValue(new HSSFRichTextString(bean.getMap().get("bonus_qty").toString()));
        cellRunway.setCellStyle(styleBody);
      } else {
        ++l;
      }
      if (bean.getMap().containsKey("total_qty")) {
        cellRunway = row.createCell(++l);
        cellRunway.setCellType(HSSFCell.CELL_TYPE_STRING);
        cellRunway.setCellValue(new HSSFRichTextString(bean.getMap().get("total_qty").toString()));
        cellRunway.setCellStyle(styleBody);
      }
      if (type.equals("closing_stock")) {
        if (bean.getMap().containsKey("qty")) {
          cellRunway = row.createCell(++l);
          cellRunway.setCellType(HSSFCell.CELL_TYPE_STRING);
          cellRunway.setCellValue(new HSSFRichTextString(bean.getMap().get("qty").toString()));
          cellRunway.setCellStyle(styleBody);
        }
      }
      if (bean.getMap().containsKey("transit_qty")) {
        cellRunway = row.createCell(++l);
        cellRunway.setCellType(HSSFCell.CELL_TYPE_STRING);
        cellRunway
            .setCellValue(new HSSFRichTextString(bean.getMap().get("transit_qty").toString()));
        cellRunway.setCellStyle(styleBody);
      }

    }

  }

  @IgnoreConfidentialFilters
  public ActionForward getScreen(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws Exception {
    JSONSerializer js = new JSONSerializer().exclude("class");

    HttpSession session = request.getSession(false);
    String dept_id = (String) session.getAttribute("pharmacyStoreId");
    int roleId = (Integer) session.getAttribute("roleId");
    if (dept_id != null && !dept_id.equals("")) {
      BasicDynaBean dept = storesDAO.findByKey("dept_id", Integer.parseInt(dept_id));
      String dept_name = dept.get("dept_name").toString();
      request.setAttribute("dept_id", dept_id);
      request.setAttribute("dept_name", dept_name);
    }
    if (dept_id != null && dept_id.equals("")) {
      request.setAttribute("dept_id", dept_id);
    }
    if (roleId == 1 || roleId == 2) {
      if (dept_id != null && !dept_id.equals(""))
        request.setAttribute("dept_id", dept_id);
      else
        request.setAttribute("dept_id", 0);
    }

    request.setAttribute("checkpointJSON", js.serialize(
        ConversionUtils.listBeanToListMap(StoresStockCheckPointDAO.getCheckPointNames())));
    request.setAttribute("checkPoints", StoresStockCheckPointDAO.getAllCheckpoints());
    request.setAttribute("medicine_timestamp", MedicineStockDAO.getMedicineTimestamp());
    return mapping.findForward("list");
  }

}
