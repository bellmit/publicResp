package com.insta.hms.mdm;

import static com.insta.hms.jobs.common.QuartzJobHelper.buildJob;

import com.bob.hms.common.RequestContext;
import com.insta.hms.common.FileOperationJob;
import com.insta.hms.common.MessageUtil;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.exception.HMSException;
import com.insta.hms.jobs.JobService;

import org.apache.commons.io.FilenameUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class FileOperationService {

  /** redisKeyAliveTime. */
  private static final int REDIS_KEY_ALIVE_TIME = 72;

  /** The job service. */
  @LazyAutowired
  private JobService jobService;

  /** The redis template. */
  @LazyAutowired
  private RedisTemplate<String, Object> redisTemplate;

  /** Redis key template. */
  private String redisKeyTemplate = "schema:%s;user:%s;file:%s;uid:%s";

  /** Redis value template. */
  private String redisValueTemplate = 
      "status:%s;action:%s;master:%s;startedAt:%s;completedAt:%s;file:%s;message:%s";

  /** The message util. */
  @LazyAutowired
  private MessageUtil messageUtil;

  /**
   * validates excel file.
   * 
   * @param file the file
   * @return boolean value
   */
  private boolean validateFile(File file) {
    String fileExtension = FilenameUtils.getExtension(file.getName());
    boolean isExcelFile = false;
    if (fileExtension.equalsIgnoreCase("xls") || fileExtension.equalsIgnoreCase("xlsx")
        || fileExtension.equalsIgnoreCase("csv")) {
      isExcelFile = true;
    }
    return isExcelFile;
  }

  /**
   * common method for bulk upload and download file operation.
   * 
   * @param map the map
   * @throws IOException exception
   */
  public void bulkDataOperation(Map<String, Object> map) throws IOException {
    map.put("schema", RequestContext.getSchema());
    map.put("userName", RequestContext.getUserName());
    map.put("centerId", RequestContext.getCenterId());

    String action = map.get("action").toString();
    String master = map.get("master").toString();

    String redisKey = null;
    String redisValue = null;
    redisKey = String.format(redisKeyTemplate, map.get("schema").toString(),
        map.get("userName").toString(), action, System.currentTimeMillis());
    map.put("redisKey", redisKey);

    if (action.equalsIgnoreCase("upload")) {
      if (validateFile((File) map.get("file"))) {
        redisValue = String.format(redisValueTemplate, "queued", action, master, "", "",
            map.get("file"), "");
        redisTemplate.opsForValue().set(redisKey, redisValue);
        redisTemplate.expire(redisKey, REDIS_KEY_ALIVE_TIME, TimeUnit.HOURS);

        jobService.scheduleImmediate(
            buildJob("FileUploadJob_" + System.currentTimeMillis(), FileOperationJob.class, map));
      } else {
        redisValue = String.format(redisValueTemplate, "fail", action, master, "", "",
            map.get("file"), "");
        redisTemplate.opsForValue().set(redisKey, redisValue);
        redisTemplate.expire(redisKey, REDIS_KEY_ALIVE_TIME, TimeUnit.HOURS);
      }
    } else {
      redisValue = String.format(redisValueTemplate, "queued", action, master, "", "", "", "");
      redisTemplate.opsForValue().set(redisKey, redisValue);
      redisTemplate.expire(redisKey, REDIS_KEY_ALIVE_TIME, TimeUnit.HOURS);

      jobService.scheduleImmediate(
          buildJob("FileDownloadJob_" + System.currentTimeMillis(), FileOperationJob.class, map));
    }
  }

  /**
   * validates excel file and converts to map.
   * 
   * @param file                      the file
   * @param mandateColumnInExcelSheet mandatory columns in sheet
   * @return map or error
   * @throws FileNotFoundException exception
   * @throws IOException           exception
   */
  public Map<String, List<Map<String, Object>>> validateExcelFileAndConvertToMapList(File file,
      Map<String, String[]> mandateColumnInExcelSheet) throws FileNotFoundException, IOException {
    String[] mandatoryColumns = null;
    if (!validateFile(file)) {
      throw new HMSException(messageUtil.getMessage("exception.file.not.excel"));
    }
    Workbook workBook = null;
    if (FilenameUtils.getExtension(file.getName()).equalsIgnoreCase("xlsx")) {
      workBook = new XSSFWorkbook(new FileInputStream(file));
    } else {
      workBook = new HSSFWorkbook(new FileInputStream(file));
    }
    Sheet sheet = null;
    Row headerRow = null;
    Row row = null;
    Cell cell = null;
    Iterator<Row> rowIterator = null;
    String[] header = null;
    List<String> errors = new ArrayList<String>();
    boolean isValidFile = true;

    Map<String, List<Map<String, Object>>> sheetMap = null;
    // workbook start
    if (workBook.getNumberOfSheets() != mandateColumnInExcelSheet.size()) {
      throw new HMSException(
          messageUtil.getMessage("exception.file.number.of.sheet.incorrect") + file.getName());
    }

    // sheet start
    sheetMap = new HashMap<String, List<Map<String, Object>>>();

    for (int sheetNumber = 0; sheetNumber < workBook.getNumberOfSheets(); sheetNumber++) {
      sheet = workBook.getSheetAt(sheetNumber);

      // copies mandatory columns of sheet to String[]
      for (String s : mandateColumnInExcelSheet.keySet()) {
        if (s.equals(workBook.getSheetName(sheetNumber))) {
          mandatoryColumns = mandateColumnInExcelSheet.get(s);
          break;
        }
      }

      rowIterator = sheet.rowIterator();

      // checks if header available in sheet and copies header values to String[]
      if (rowIterator.hasNext()) {
        headerRow = (Row) rowIterator.next();
        header = new String[headerRow.getLastCellNum()];
        if (headerRow != null) {
          for (int column = 0; column < header.length; column++) {
            cell = headerRow.getCell(column);
            if (cell != null) {
              header[column] = cell.toString().trim().toLowerCase();
            } else {
              header[column] = null;
            }
          }
        }
      } else {
        errors.add(messageUtil.getMessage("exception.file.header.is.not.available")
            + sheet.getSheetName());
        isValidFile = false;
        continue;
      }

      // checks mandatory columns passed are available in header
      if (mandatoryColumns != null) {
        for (String s : mandatoryColumns) {
          if (!Arrays.asList(mandatoryColumns).contains(s)) {
            errors.add(messageUtil.getMessage("exception.file.missing.mandatory.column")
                + s.toUpperCase() + sheet.getSheetName().toUpperCase());
            isValidFile = false;
          }
        }
      }

      String cellValue = null;
      Map<String, Object> rowData = null;
      List<Map<String, Object>> rowDataList = null;
      // copy cell values and convert to map
      rowDataList = new ArrayList<Map<String, Object>>();
      int lineNumber = 2;
      while (rowIterator.hasNext()) {
        row = (Row) rowIterator.next();
        if (row.getLastCellNum() <= 0) {
          continue;
        }
        rowData = new HashMap<String, Object>();
        for (int column = 0; column < header.length; column++) {
          cell = row.getCell(column);
          if ((mandatoryColumns != null)
              && (Arrays.asList(mandatoryColumns).contains(header[column]) && (cell == null))) {
            errors.add(messageUtil.getMessage("exception.file.missing.value.in.column")
                + header[column].toUpperCase() + messageUtil.getMessage("exception.file.in.sheet")
                + sheet.getSheetName().toUpperCase());
            isValidFile = false;
          } else {
            if (cell != null) {
              cell.setCellType(Cell.CELL_TYPE_STRING);
              cellValue = cell.getStringCellValue();
              if ((mandatoryColumns != null)
                  && (Arrays.asList(mandatoryColumns).contains(header[column])
                      && (cellValue.equals("")))) {
                errors.add(messageUtil.getMessage("exception.file.missing.value.in.column")
                    + header[column].toUpperCase()
                    + messageUtil.getMessage("exception.file.in.sheet")
                    + sheet.getSheetName().toUpperCase());
                isValidFile = false;
              } else {
                rowData.put(header[column], cellValue);
              }
            }
          }
        }

        boolean exists = false;
        for (Map<String, Object> existingRowData : rowDataList) {
          if (rowData.equals(existingRowData)) {
            exists = true;
            errors.add("Duplicate row in line " + lineNumber + " sheet :" + sheet.getSheetName());
            isValidFile = false;
          }
        }
        if (!exists && !rowData.isEmpty()) {
          rowDataList.add(rowData);
        }
        lineNumber++;
      }
      sheetMap.put(sheet.getSheetName(), rowDataList);
    }
    if (!isValidFile) {
      throw new HMSException(errors);
    } else {
      return sheetMap;
    }
  }

  /**
   * Creates a excel file.
   * 
   * @param fileInfo  the fileinfo
   * @param sheetData the sheetdata
   * @return file the excel file
   * @throws IOException the exception
   */
  @SuppressWarnings("unchecked")
  public File createExcelFile(Map<String, Object> fileInfo,
      Map<String, Map<String, Object>> sheetData) throws IOException {
    Workbook workbook = new XSSFWorkbook();

    // Create Sheets
    List<String> sheets = (List<String>) fileInfo.get("SheetNames");
    for (String sh : sheets) {
      Sheet sheet = workbook.createSheet(sh);

      Row row = null;
      Cell cell = null;
      // Create a Header Row
      String[] header = (String[]) sheetData.get(sh).get("header");
      int noOfColumns = header.length;
      row = sheet.createRow(0);
      for (int i = 0; i < noOfColumns; i++) {
        cell = row.createCell(i);
        cell.setCellType(Cell.CELL_TYPE_STRING);
        cell.setCellValue(header[i]);
      }
    }
    
    // Write the output to a file
    File receivedFile = new File(fileInfo.get("file").toString());
    FileOutputStream fileOut = new FileOutputStream(receivedFile);
    workbook.write(fileOut);
    fileOut.close();

    return receivedFile;
  }
  
  /**
   * Append data to file.
   * 
   * @param fileInfo  the fileinfo
   * @param sheetData the sheetdata
   * @return file the excel file
   * @throws IOException the exception
   */
  @SuppressWarnings("unchecked")
  public File appendDataToExcelFile(Map<String, Object> fileInfo,
      Map<String, Map<String, Object>> sheetData) throws IOException {
    File receivedFile = (File) fileInfo.get("ExcelFile");
    FileInputStream fileIn = new FileInputStream(receivedFile);
    Workbook workbook = new XSSFWorkbook(fileIn);

    // Get Sheets
    List<String> sheets = (List<String>) fileInfo.get("SheetNames");
    Row row = null;
    Cell cell = null;
    Sheet sheet;
    List<Map<String,Object>> mapList;
    String[] header;
    for (String sh : sheets) {
      sheet = workbook.getSheet(sh);
      mapList = (List<Map<String,Object>>) sheetData.get(sh).get("data");
      header = (String[]) sheetData.get(sh).get("header");
      int noOfColumns = header.length;
      int rowNumber = sheetData.get(sh).get("row_number") == null ? 1
          : (int) sheetData.get(sh).get("row_number");
      for (Map<String,Object> bean : mapList) {
        row = sheet.createRow(rowNumber);
        for (int i = 0; i < noOfColumns; i++) {
          cell = row.createCell(i);
          cell.setCellType(Cell.CELL_TYPE_STRING);
          if (!StringUtils.isEmpty(bean.get(header[i]))) {
            cell.setCellValue(bean.get(header[i]).toString());
          } else {
            cell.setCellValue("");
          }
        }
        rowNumber++;
      }
      sheetData.get(sh).put("row_number", rowNumber);
    }
    fileIn.close();
    FileOutputStream fileOut = new FileOutputStream(receivedFile);
    workbook.write(fileOut);
    fileOut.close();
    return receivedFile;
  }

  public enum OperationScreenType {
    BulkPatientData, DoctorDefinitionDetails, DoctorCharges, InsurancePlan, InsuranceCompany,
    TpaMaster, InsurancePlanType, CodeSets
  }
}
