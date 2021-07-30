package com.bob.hms.common;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFCell;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;
import java.util.Map;

/**
 * The Class HSSFWorkbookUtils.
 */
public class HsSfWorkbookUtils {

  /**
   * Creates the physical cells with values.
   *
   * @param list            the list
   * @param columnsNamesMap the columns names map
   * @param worksheet       the worksheet
   * @param fstColHidden    the fst col hidden
   */
  /* This below method will create cells and populate values in corresponding cells. */
  public static void createPhysicalCellsWithValues(List list, Map columnsNamesMap,
      XSSFSheet worksheet, boolean fstColHidden) {
    createPhysicalCellsWithValues(list, columnsNamesMap, worksheet, fstColHidden, new int[] { 0 });
  }

  /** TODO: THIS IS AN OVERLOADED METHOD NEED TO REMOVE THIS AND MODIFY ALL EXPORTS TO SXSSFSheet
   * Creates the physical cells with values.
   *
   * @param list            the list
   * @param columnsNamesMap the columns names map
   * @param worksheet       the worksheet
   * @param fstColHidden    the fst col hidden
   */
  public static void createPhysicalCellsWithValues(List list, Map columnsNamesMap,
      SXSSFSheet worksheet, boolean fstColHidden) {
    createPhysicalCellsWithValues(list, columnsNamesMap, worksheet, fstColHidden, new int[] { 0 });
  }

  /**
   * Creates the physical cells with values.
   *
   * @param list            the list
   * @param columnsNamesMap the columns names map
   * @param worksheet       the worksheet
   * @param fstColHidden    the fst col hidden
   * @param hiddenCols      the hidden cols
   */
  public static void createPhysicalCellsWithValues(List list, Map columnsNamesMap,
      XSSFSheet worksheet, boolean fstColHidden, int[] hiddenCols) {

    XSSFRow row = worksheet.createRow(0);
    XSSFWorkbook workbook = worksheet.getWorkbook();

    int bint = 0;

    List<String> mainItems = (List<String>) columnsNamesMap.get("mainItems");
    List<String> charges = (List<String>) columnsNamesMap.get("charges");

    if (mainItems != null) {
      XSSFCell cell = null;
      for (int i = 0; i < mainItems.size(); i++) {
        cell = row.createCell(bint);
        cell.setCellValue(new XSSFRichTextString(mainItems.get(i)));
        bint++;
      }
    }
    int chargeCell = bint;

    XSSFCellStyle leftRightBorderStyle = workbook.createCellStyle();
    leftRightBorderStyle.setAlignment(XSSFCellStyle.ALIGN_CENTER);
    leftRightBorderStyle.setBorderLeft(XSSFCellStyle.BORDER_THIN);
    leftRightBorderStyle.setBorderRight(XSSFCellStyle.BORDER_THIN);

    XSSFCellStyle rightBorderStyle = workbook.createCellStyle();
    rightBorderStyle.setBorderRight(XSSFCellStyle.BORDER_THIN);

    for (int i : hiddenCols) {
      worksheet.setColumnHidden(i, fstColHidden);
    }
    List<String> bedTypes = (List<String>) columnsNamesMap.get("bedTypes");
    if (bedTypes != null) {

      int cellsToMerge = charges.size();
      for (String bed : bedTypes) {
        // int cellNo = b;
        int from = bint;
        int to = bint + (cellsToMerge - 1);
        worksheet.addMergedRegion(new CellRangeAddress(0, 0, from, to));
        XSSFCell cell = row.createCell(from);
        cell.setCellValue(bed);
        bint = to + 1;
      }

      row = worksheet.createRow(1);
      if (mainItems != null) {
        row.createCell(chargeCell - 1).setCellStyle(rightBorderStyle);
      }

      for (String bed : bedTypes) {
        XSSFCell cell = null;
        for (String charge : charges) {
          cell = row.createCell(chargeCell);
          cell.setCellValue(charge);
          chargeCell++;
        }
      }

    }

    int startingBorderNum = 0;
    int endingBorderNum = 0;
    if (mainItems != null) {
      if (charges == null) {
        startingBorderNum = mainItems.size() + 1;
      } else {
        startingBorderNum = mainItems.size();
      }
    }
    if (charges != null) {
      endingBorderNum = charges.size();
      if (mainItems == null) {
        startingBorderNum = charges.size();
      }
    }

    XSSFCellStyle st = row.getSheet().getWorkbook().createCellStyle();
    rightBorderStyle.setBorderRight(XSSFCellStyle.BORDER_THIN);
    rightBorderStyle.setLocked(false);
    st.setLocked(false);

    for (int k = 1; k <= list.size(); k++) {
      row = worksheet.createRow(k + 1);
      BasicDynaBean basicBean = (BasicDynaBean) list.get(k - 1);

      insertHsSfRow(row, basicBean, startingBorderNum, endingBorderNum, fstColHidden,
          rightBorderStyle, st);

    }
    // for (int k = 0; k < list.size(); k++) {
    // worksheet.autoSizeColumn(k, false);
    // }
    // worksheet.protectSheet("");
  }

  /** TODO: THIS IS A OVERLOADED METHOD NEED TO REMOVE THIS AND MODIFY EVERTHING TO SXSSFSheet
   * Creates the physical cells with values.
   *
   * @param list            the list
   * @param columnsNamesMap the columns names map
   * @param worksheet       the worksheet
   * @param fstColHidden    the fst col hidden
   * @param hiddenCols      the hidden cols
   */
  public static void createPhysicalCellsWithValues(List list, Map columnsNamesMap,
      SXSSFSheet worksheet, boolean fstColHidden, int[] hiddenCols) {

    SXSSFRow row = worksheet.createRow(0);
    SXSSFWorkbook workbook = worksheet.getWorkbook();

    int bint = 0;

    List<String> mainItems = (List<String>) columnsNamesMap.get("mainItems");
    List<String> charges = (List<String>) columnsNamesMap.get("charges");

    if (mainItems != null) {
      SXSSFCell cell = null;
      for (int i = 0; i < mainItems.size(); i++) {
        cell = row.createCell(bint);
        cell.setCellValue(new XSSFRichTextString(mainItems.get(i)));
        bint++;
      }
    }
    int chargeCell = bint;

    CellStyle leftRightBorderStyle = workbook.createCellStyle();
    leftRightBorderStyle.setAlignment(XSSFCellStyle.ALIGN_CENTER);
    leftRightBorderStyle.setBorderLeft(XSSFCellStyle.BORDER_THIN);
    leftRightBorderStyle.setBorderRight(XSSFCellStyle.BORDER_THIN);

    CellStyle rightBorderStyle = workbook.createCellStyle();
    rightBorderStyle.setBorderRight(XSSFCellStyle.BORDER_THIN);

    for (int i : hiddenCols) {
      worksheet.setColumnHidden(i, fstColHidden);
    }
    List<String> bedTypes = (List<String>) columnsNamesMap.get("bedTypes");
    if (bedTypes != null) {

      int cellsToMerge = charges.size();
      for (String bed : bedTypes) {
        // int cellNo = b;
        int from = bint;
        int to = bint + (cellsToMerge - 1);
        worksheet.addMergedRegion(new CellRangeAddress(0, 0, from, to));
        SXSSFCell cell = row.createCell(from);
        cell.setCellValue(bed);
        cell.setCellStyle(leftRightBorderStyle);
        bint = to + 1;
      }

      row = worksheet.createRow(1);
      if (mainItems != null) {
        row.createCell(chargeCell - 1).setCellStyle(rightBorderStyle);
      }

      for (String bed : bedTypes) {
        SXSSFCell cell = null;
        for (String charge : charges) {
          cell = row.createCell(chargeCell);
          cell.setCellValue(charge);
          chargeCell++;
        }
        if (cell != null) {
          cell.setCellStyle(rightBorderStyle);
        }
      }

    }

    int startingBorderNum = 0;
    int endingBorderNum = 0;
    if (mainItems != null) {
      if (charges == null) {
        startingBorderNum = mainItems.size() + 1;
      } else {
        startingBorderNum = mainItems.size();
      }
    }
    if (charges != null) {
      endingBorderNum = charges.size();
      if (mainItems == null) {
        startingBorderNum = charges.size();
      }
    }

    CellStyle st = row.getSheet().getWorkbook().createCellStyle();
    rightBorderStyle.setBorderRight(XSSFCellStyle.BORDER_THIN);
    rightBorderStyle.setLocked(false);
    st.setLocked(false);

    for (int k = 1; k <= list.size(); k++) {
      row = worksheet.createRow(k + 1);
      BasicDynaBean basicBean = (BasicDynaBean) list.get(k - 1);

      insertHsSfRow(row, basicBean, startingBorderNum, endingBorderNum, fstColHidden,
          rightBorderStyle, st);

    }
    // for (int k = 0; k < list.size(); k++) {
    // worksheet.autoSizeColumn(k, false);
    // }
    // worksheet.protectSheet("");
  }

  /**
   * Insert HSSF row.
   *
   * @param row               the row
   * @param basicBean         the basic bean
   * @param startingBorderNum the starting border num
   * @param endingBorderNum   the ending border num
   * @param fstColHidden      the fst col hidden
   * @param borderStyle       the border style
   * @param st                the st
   */
  private static void insertHsSfRow(XSSFRow row, BasicDynaBean basicBean, int startingBorderNum,
      int endingBorderNum, boolean fstColHidden, XSSFCellStyle borderStyle, XSSFCellStyle st) {

    int index = 0;
    DynaProperty[] dynaProperties = basicBean.getDynaClass().getDynaProperties();
    boolean first = true;
    for (DynaProperty property : dynaProperties) {
      XSSFCell cellRunway = null;

      if (basicBean.get(property.getName()) == null) {
        cellRunway = row.createCell(index);
        cellRunway.setCellValue(new XSSFRichTextString());

      } else if (property.getType().equals(java.lang.String.class)) {

        cellRunway = row.createCell(index);
        cellRunway.setCellType(XSSFCell.CELL_TYPE_STRING);
        cellRunway.setCellValue(
            new XSSFRichTextString(basicBean.get(property.getName().toLowerCase()).toString()));

      } else if (property.getType().equals(java.sql.Time.class)) {
        // cellRunway.getCellStyle()
        DataFormat df = row.getSheet().getWorkbook().createDataFormat();
        cellRunway = row.createCell(index);
        cellRunway.getCellStyle().setDataFormat(df.getFormat("[h]:mm:ss;@"));
        cellRunway.setCellType(XSSFCell.CELL_TYPE_FORMULA);
        String[] dateString = basicBean.get(property.getName().toLowerCase()).toString().split(":");
        cellRunway.setCellFormula(
            "TIME(" + dateString[0] + "," + dateString[1] + "," + dateString[2] + ")");
        // cellRunway.setCellType(HSSFCell.CELL_TYPE_STRING);
        cellRunway.setCellValue(
            new XSSFRichTextString(basicBean.get(property.getName().toLowerCase()).toString()));

      } else if (property.getType().equals(java.lang.Boolean.class)) {

        cellRunway = row.createCell(index);
        cellRunway.setCellType(XSSFCell.CELL_TYPE_BOOLEAN);
        cellRunway.setCellValue((Boolean) basicBean.get(property.getName()));

      } else if (property.getType().equals(java.lang.Integer.class)) {

        cellRunway = row.createCell(index);
        cellRunway.setCellType(XSSFCell.CELL_TYPE_NUMERIC);
        cellRunway.setCellValue((Integer) basicBean.get(property.getName()));

      } else if (property.getType().equals(java.math.BigDecimal.class)) {

        cellRunway = row.createCell(index);
        cellRunway.setCellType(XSSFCell.CELL_TYPE_NUMERIC);
        BigDecimal value = BigDecimal.ZERO;
        value = (BigDecimal) basicBean.get(property.getName());
        cellRunway.setCellValue(value.doubleValue());

      } else if (property.getType().equals(java.sql.Date.class)) {

        cellRunway = row.createCell(index);
        cellRunway.setCellType(XSSFCell.CELL_TYPE_NUMERIC);
        java.sql.Date date = (Date) basicBean.get(property.getName());
        cellRunway.setCellValue(date);

      }
      index++;
      if (index == (startingBorderNum)) {
        if (cellRunway != null) {
          cellRunway.setCellStyle(borderStyle);
        }
        startingBorderNum = startingBorderNum + endingBorderNum;
      } else {
        if (cellRunway != null) {
          cellRunway.setCellStyle(st);
        }
      }

      first = false;

    }
  }

  /** TODO: THIS IS A OVERLOADED METHOD NEED TO REMOVE THIS AND MODIFY EVERTHING TO SXSSFSheet
   * Insert HSSF row.
   *
   * @param row               the row
   * @param basicBean         the basic bean
   * @param startingBorderNum the starting border num
   * @param endingBorderNum   the ending border num
   * @param fstColHidden      the fst col hidden
   * @param borderStyle       the border style
   * @param st                the st
   */
  private static void insertHsSfRow(SXSSFRow row, BasicDynaBean basicBean, int startingBorderNum,
      int endingBorderNum, boolean fstColHidden, CellStyle borderStyle, CellStyle st) {

    int index = 0;
    DynaProperty[] dynaProperties = basicBean.getDynaClass().getDynaProperties();
    boolean first = true;
    for (DynaProperty property : dynaProperties) {
      SXSSFCell cellRunway = null;

      if (basicBean.get(property.getName()) == null) {
        cellRunway = row.createCell(index);
        cellRunway.setCellValue(new XSSFRichTextString());

      } else if (property.getType().equals(java.lang.String.class)) {

        cellRunway = row.createCell(index);
        cellRunway.setCellType(XSSFCell.CELL_TYPE_STRING);
        cellRunway.setCellValue(
            new XSSFRichTextString(basicBean.get(property.getName().toLowerCase()).toString()));

      } else if (property.getType().equals(java.sql.Time.class)) {
        // cellRunway.getCellStyle()
        DataFormat df = row.getSheet().getWorkbook().createDataFormat();
        cellRunway = row.createCell(index);
        cellRunway.getCellStyle().setDataFormat(df.getFormat("[h]:mm:ss;@"));
        cellRunway.setCellType(XSSFCell.CELL_TYPE_FORMULA);
        String[] dateString = basicBean.get(property.getName().toLowerCase()).toString().split(":");
        cellRunway.setCellFormula(
            "TIME(" + dateString[0] + "," + dateString[1] + "," + dateString[2] + ")");
        // cellRunway.setCellType(HSSFCell.CELL_TYPE_STRING);
        cellRunway.setCellValue(
            new XSSFRichTextString(basicBean.get(property.getName().toLowerCase()).toString()));

      } else if (property.getType().equals(java.lang.Boolean.class)) {

        cellRunway = row.createCell(index);
        cellRunway.setCellType(XSSFCell.CELL_TYPE_BOOLEAN);
        cellRunway.setCellValue((Boolean) basicBean.get(property.getName()));

      } else if (property.getType().equals(java.lang.Integer.class)) {

        cellRunway = row.createCell(index);
        cellRunway.setCellType(XSSFCell.CELL_TYPE_NUMERIC);
        cellRunway.setCellValue((Integer) basicBean.get(property.getName()));

      } else if (property.getType().equals(java.math.BigDecimal.class)) {

        cellRunway = row.createCell(index);
        cellRunway.setCellType(XSSFCell.CELL_TYPE_NUMERIC);
        BigDecimal value = BigDecimal.ZERO;
        value = (BigDecimal) basicBean.get(property.getName());
        cellRunway.setCellValue(value.doubleValue());

      } else if (property.getType().equals(java.sql.Date.class)) {

        cellRunway = row.createCell(index);
        cellRunway.setCellType(XSSFCell.CELL_TYPE_NUMERIC);
        java.sql.Date date = (Date) basicBean.get(property.getName());
        cellRunway.setCellValue(date);

      }
      index++;
      if (index == (startingBorderNum)) {
        if (!first && !fstColHidden && cellRunway != null) {
          cellRunway.setCellStyle(borderStyle);
        }
        startingBorderNum = startingBorderNum + endingBorderNum;
      } else if (first && fstColHidden && cellRunway != null) {
        cellRunway.setCellStyle(st);
      }
      first = false;
    }
  }

}
