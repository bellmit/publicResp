package com.insta.mhms.patient.emrview;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.diagnosticmodule.common.DiagnosticsDAO;
import com.insta.hms.emr.DocHolder;
import com.insta.hms.emr.EMRDoc;
import com.insta.hms.emr.EMRDocFilter;
import com.insta.hms.emr.EMRInterface;
import com.insta.hms.emr.Filter;
import com.insta.hms.emr.FilterFactory;
import com.insta.hms.vitalForm.genericVitalFormDAO;
import com.insta.instaapi.common.JsonProcessor;
import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @author mithun.saha
 */

public class EmrViewAction extends DispatchAction {
  static Logger logger = LoggerFactory.getLogger(EmrViewAction.class);

  /**
   * Get patient EMR visit Details.
   *
   * @param mapping  mapping paramter
   * @param form     form paramter
   * @param request  request object
   * @param response response object
   * @return returns action forward
   * @throws IOException              Signals that an I/O exception has occurred
   * @throws ServletException         may throw Servlet Exception
   * @throws SQLException             may throw SQL Exception
   * @throws NoSuchAlgorithmException may throw NoSuchAlgorithmException
   * @throws ParseException           may throw Parsing exception
   * @throws Exception                may throw Generic Exception.
   */
  public ActionForward getPatientEMRVisitDetails(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException, SQLException, NoSuchAlgorithmException, ParseException, Exception {

    logger.info("getting all patient realted emr data...");
    JSONSerializer js = JsonProcessor.getJSONParser();
    String successMsg = "";
    String returnCode = "";
    HttpSession session = (HttpSession) request.getSession(false);
    Map<String, Object> responseData = new HashMap<String, Object>();
    List<EMRDoc> allDocs = new ArrayList<EMRDoc>();
    String userName = (String) session.getAttribute("mobile_user_id");
    if (userName == null || userName.equals("")) {
      successMsg = "Session is expired, please login again";
      logger.info("Session is expired, please login again");
      responseData.put("return_code", "1001");
      responseData.put("return_message", successMsg);
      logger.info("sending the response back to the requesting server");
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.getWriter().write(js.deepSerialize(responseData));
      response.flushBuffer();
      return null;
    }
    EMRDocFilter docFilter = new EMRDocFilter();
    List<String> columns = new ArrayList<String>();
    columns.add("patient_id");
    Map<String, Object> identifiers = new HashMap<String, Object>();
    identifiers.put("emp_username", "APIPatient");
    BasicDynaBean userBean = new GenericDAO("u_user").findByKey(identifiers);
    session.setAttribute("userid", (String) userBean.get("emp_username"));
    session.setAttribute("centerId", (Integer) userBean.get("center_id"));
    session.setAttribute("roleId", ((BigDecimal) userBean.get("role_id")).intValue());
    List<BasicDynaBean> patientVisitIds = new GenericDAO("patient_registration").listAll(columns,
        "mr_no", userName, "reg_date");
    for (BasicDynaBean bean : patientVisitIds) {
      String visitId = (String) bean.get("patient_id");
      if (visitId != null && !visitId.isEmpty()) {
        for (EMRInterface.Provider provider : EMRInterface.Provider.values()) {
          List<EMRDoc> list = provider.getProviderImpl().listDocumentsByVisit(visitId);
          if (list != null && !list.isEmpty()) {
            allDocs = docFilter.applyFilter(allDocs, list, request, false);
          }
        }
      }
    }
    String filterType = "visits";
    Filter filter = FilterFactory.getFilter(filterType);
    List filteredDocs = Collections.EMPTY_LIST;
    if (!allDocs.isEmpty()) {
      filteredDocs = filter.applyFilter(allDocs, "");
    }
    if (!filteredDocs.isEmpty()) {
      Iterator<ArrayList> it = filteredDocs.iterator();
      while (it.hasNext()) {
        List docHolderList = (ArrayList) it.next();
        for (int i = 0; i < docHolderList.size(); i++) {
          DocHolder docHolder = (DocHolder) docHolderList.get(i);
          String label = docHolder.getLabel();
          if (label != null && !label.equals("")) {
            Date date = DateUtil.parseDate(label.substring(0, label.indexOf(" ")));
            String str = DateUtil.formatIso8601Date(date) + " "
                + label.substring(label.indexOf(" "));
            docHolder.setLabel(str);
          }
          List<EMRDoc> list = docHolder.getViewDocs();
          Iterator<EMRDoc> iter = list.iterator();
          if (list != null && list.size() > 0) {
            List newList = new ArrayList();
            while (iter.hasNext()) {
              EMRDoc emrDoc = iter.next();
              Map temp = new HashMap();
              temp.put("annotation", emrDoc.getAnotation());
              temp.put("contentType", emrDoc.getContentType());
              temp.put("description", emrDoc.getDescription());
              if (emrDoc.getDisplayUrl().contains("print/")
                  && emrDoc.getDisplayUrl().contains(".json")) {
                temp.put("displayUrl", "/api" + emrDoc.getDisplayUrl());
              } else {
                temp.put("displayUrl", emrDoc.getDisplayUrl());
              }
              temp.put("docid", emrDoc.getDocid());
              temp.put("doctor", emrDoc.getDoctor());
              temp.put("pdfSupported", emrDoc.isPdfSupported());
              if (emrDoc.getDate() != null) {
                temp.put("date", DateUtil
                    .formatIso8601Timestamp((new java.sql.Date(emrDoc.getDate().getTime()))));
              }
              temp.put("title", emrDoc.getTitle());
              temp.put("type", emrDoc.getType());
              if (emrDoc.getVisitDate() != null) {
                temp.put("visitDate", DateUtil
                    .formatIso8601Timestamp(new java.sql.Date(emrDoc.getVisitDate().getTime())));
              }
              temp.put("visitid", emrDoc.getVisitid());
              newList.add(temp);
            }
            docHolder.setViewDocs(newList);
          }
        }
      }
    }

    boolean success = false;
    logger.info("got all patient realted emr data...");
    success = true;
    if (success) {
      // responseData.put("success", "success");
      responseData.put("return_message", "Success");
      returnCode = "2001";
    } else {
      // responseData.put("success", "failure");
      responseData.put("return_message", "fail to get emr data.");
      returnCode = "1021";
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
    responseData.put("return_code", returnCode);
    responseData.put("patient_visit_emr_documents", filteredDocs);
    response.setContentType("application/json");
    response.setHeader("Cache-Control", "no-cache");
    // response.setHeader("Access-Control-Allow-Origin", "*");
    response.getWriter().write(js.deepSerialize(responseData));
    response.flushBuffer();

    return null;
  }

  /**
   * Apply Filters to the docs.
   * 
   * @param allDocs paramter alldocs
   * @param list    paramter list
   * @param req     request object
   * @return returns list of EMR doc
   * @throws ParseException may throw parsing exception
   */
  public List<EMRDoc> applyFilterToTheDocs(List<EMRDoc> allDocs, List list, HttpServletRequest req)
      throws ParseException {

    String indocType = null;
    String exdocType = null;

    String filterType = req.getParameter("filterType") != null
        ? (String) req.getParameter("filterType")
        : "visits";

    if (req.getParameter("indocType") != null) {
      indocType = req.getParameter("indocType");
      if (indocType.equals("*")) {
        indocType = null;
      }
    }
    if (req.getParameter("exdocType") != null) {
      exdocType = req.getParameter("exdocType");
      if (exdocType.equals("*")) {
        indocType = null;
      }
    }
    String fromDate = null;
    String toDate = null;
    if (req.getParameter("fromDate") != null) {
      fromDate = req.getParameter("fromDate");
    }
    if (req.getParameter("toDate") != null) {
      toDate = req.getParameter("toDate");
    }

    if (list != null && !list.isEmpty()) {
      Iterator<EMRDoc> it = list.iterator();
      while (it.hasNext()) {
        EMRDoc emrDoc = it.next();
        if (filterType.equals("visits")) {
          if ((indocType != null && exdocType != null) || (indocType != null && exdocType == null)
              || (indocType == null && exdocType != null)) {
            if (emrDoc.getType().equals(indocType) && exdocType == null) {
              allDocs.add(emrDoc);
            }
            if (!emrDoc.getType().equals(exdocType) && indocType == null) {
              allDocs.add(emrDoc);
            }
          } else {
            allDocs.add(emrDoc);
          }
        } else {
          SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd");
          if ((!fromDate.equals("") && !toDate.equals(""))
              || (fromDate.equals("") && !toDate.equals(""))
              || (!fromDate.equals("") && toDate.equals(""))) {
            if (emrDoc.getDate() == null) {
              // skip the document. when the document date is null, but searching within a date
              // range.
              continue;
            }
            if (!toDate.equals("") && fromDate.equals("")) {
              java.util.Date valtoDate = DataBaseUtil.parseDate(toDate);
              int value = new Date(dateformat.parse(emrDoc.getDate().toString()).getTime())
                  .compareTo(valtoDate);
              if (value == -1 || value == 0) {
                allDocs.add(emrDoc);
              }
            }
            if (!fromDate.equals("") && toDate.equals("")) {
              java.util.Date valfromDate = DataBaseUtil.parseDate(fromDate);
              int value = emrDoc.getDate().compareTo(valfromDate);
              if (value == 1 || value == 0) {
                allDocs.add(emrDoc);
              }
            }
            if (!fromDate.equals("") && !toDate.equals("")) {
              java.util.Date valtoDate = DataBaseUtil.parseDate(toDate);
              java.util.Date valfromDate = DataBaseUtil.parseDate(fromDate);
              if (emrDoc.getDate() == null) {
                allDocs.add(emrDoc);
              } else {
                int valueToDate = new Date(dateformat.parse(emrDoc.getDate().toString()).getTime())
                    .compareTo(valtoDate);
                int valFromDate = new Date(dateformat.parse(emrDoc.getDate().toString()).getTime())
                    .compareTo(valfromDate);
                if ((valueToDate == -1 || valueToDate == 0)
                    && (valFromDate == 1 || valFromDate == 0)) {
                  allDocs.add(emrDoc);
                }
              }
            }
          } else {
            allDocs.add(emrDoc);
          }
        }
      }
    }

    return allDocs;
  }

  /**
   * Get Results View.
   *
   * @param mapping mapping paramter
   * @param form    form parameter
   * @param req     request object
   * @param res     response object
   * @return returns String
   * @throws IOException    Signals that an I/O exception has occurred
   * @throws SQLException   may throw SQL Exception
   * @throws ParseException may throw parsing exception
   */
  public String getTestResultsView(ActionMapping mapping, ActionForm form, HttpServletRequest req,
      HttpServletResponse res) throws IOException, SQLException, ParseException {
    String mrNo = (String) req.getSession(false).getAttribute("mobile_user_id");
    String[] testVlaues = null;
    Date fromDate = null;
    Date toDate = null;
    List testValues = DiagnosticsDAO.getTestValues(mrNo, testVlaues, fromDate, toDate, null);
    Map map = ConversionUtils.listBeanToMapMapBean(testValues, "resultname", "pres_date");
    Map datesMap = ConversionUtils.listBeanToMapListBean(testValues, "pres_date");
    Map<String, List<Timestamp>> columnNamesMap = new HashMap<String, List<Timestamp>>();
    List<Timestamp> presDateList = new ArrayList<Timestamp>();
    presDateList.addAll(datesMap.keySet());
    columnNamesMap.put("headerList", presDateList);
    HSSFWorkbook workbook = new HSSFWorkbook();
    HSSFSheet workSheet = workbook.createSheet("Patient Test Trend Report ");

    createPhysicalCellsWithValues(map, columnNamesMap, workSheet, "test_trend");
    res.setHeader("Content-type", "application/vnd.ms-excel");
    // res.setHeader("Access-Control-Allow-Origin", "*");
    // res.setHeader("Content-disposition","attachment;
    // filename="+"\"patient_test_trend_report.xls\"");
    res.setHeader("Readonly", "true");

    java.io.OutputStream outputStream = res.getOutputStream();
    workbook.write(outputStream);
    outputStream.flush();
    outputStream.close();

    return null;
  }

  /**
   * Get Vitals View.
   *
   * @param mapping mapping paramter
   * @param form    form parameter
   * @param req     request object
   * @param res     response object
   * @return returns String
   * @throws IOException    Signals that an I/O exception has occurred
   * @throws SQLException   may throw SQL Exception
   * @throws ParseException may throw parsing exception
   */
  public String getVitalsView(ActionMapping mapping, ActionForm form, HttpServletRequest req,
      HttpServletResponse res) throws IOException, SQLException, ParseException {
    String mrNo = (String) req.getSession(false).getAttribute("mobile_user_id");
    String[] testVlaues = null;
    Date fromDate = null;
    Date toDate = null;
    List vitalValues = genericVitalFormDAO.getVitalValues(mrNo, testVlaues, fromDate, toDate, null);
    Map map = ConversionUtils.listBeanToMapMapBean(vitalValues, "param_label", "date_time");
    Map datesMap = ConversionUtils.listBeanToMapListBean(vitalValues, "date_time");
    Map<String, List<Timestamp>> columnNamesMap = new HashMap<String, List<Timestamp>>();
    List<Timestamp> vitalDateList = new ArrayList<Timestamp>();
    vitalDateList.addAll(datesMap.keySet());
    columnNamesMap.put("headerList", vitalDateList);
    HSSFWorkbook workbook = new HSSFWorkbook();
    HSSFSheet workSheet = workbook.createSheet("Patient Vital Trend Report ");

    createPhysicalCellsWithValues(map, columnNamesMap, workSheet, "vital_trend");
    res.setHeader("Content-type", "application/vnd.ms-excel");
    // res.setHeader("Access-Control-Allow-Origin", "*");
    // res.setHeader("Content-disposition","attachment;
    // filename="+"\"patient_vital_trend_report.xls\"");
    res.setHeader("Readonly", "true");

    java.io.OutputStream outputStream = res.getOutputStream();
    workbook.write(outputStream);
    outputStream.flush();
    outputStream.close();

    return null;
  }

  /**
   * create PhysicalCells With Values.
   *
   * @param map             map paramter
   * @param columnsNamesMap columns name map
   * @param worksheet       worksheet
   * @param reportType      report type
   */
  public static void createPhysicalCellsWithValues(Map map, Map columnsNamesMap,
      HSSFSheet worksheet, String reportType) {

    HSSFRow row = worksheet.createRow(0);
    HSSFWorkbook workbook = worksheet.getWorkbook();
    int rowIndex = 1;
    int count = 1;

    List<Timestamp> mainItems = (List<Timestamp>) columnsNamesMap.get("headerList");

    if (mainItems != null) {
      HSSFCell cell = null;
      for (int i = 0; i < mainItems.size(); i++) {
        cell = row.createCell(count);
        cell.setCellValue(new HSSFRichTextString(
            new DateUtil().getTimeStampFormatter().format(mainItems.get(i))));
        count++;
      }
    }

    HSSFCellStyle leftRightBorderStyle = workbook.createCellStyle();
    leftRightBorderStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
    leftRightBorderStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);
    leftRightBorderStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);

    HSSFCellStyle rightBorderStyle = workbook.createCellStyle();
    rightBorderStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);

    if (map != null) {
      for (Object key : map.keySet()) {
        row = worksheet.createRow(rowIndex);
        rowIndex++;
        int from = 0;
        HSSFCell cell = row.createCell(from);
        cell.setCellValue(key.toString());
        cell.setCellStyle(rightBorderStyle);
        from = from + 1;
        for (Timestamp time : mainItems) {
          BasicDynaBean bean = (BasicDynaBean) ((Map) map.get(key.toString())).get(time);
          Timestamp rowTime = null;
          String reportValue = null;
          String units = "";
          if (bean != null) {
            if (reportType.equals("test_trend")) {
              rowTime = (Timestamp) bean.get("pres_date");
              reportValue = (String) bean.get("report_value");
              units = units + " " + (String) bean.get("units");
            } else {
              rowTime = (Timestamp) bean.get("date_time");
              reportValue = (String) bean.get("param_value");
            }

            HSSFCell celll = row.createCell(from);
            celll.setCellValue(reportValue);
            celll.setCellStyle(rightBorderStyle);
          }
          from++;
        }
      }
    }
    worksheet.createFreezePane(11, 1, 11, 1);
  }

}
