package com.insta.hms.stores;

import au.com.bytecode.opencsv.CSVWriter;
import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class StoresReportAction extends DispatchAction {

  /*
   * Purchase Details Report
   */
  @IgnoreConfidentialFilters
  public ActionForward purchaseDetailsExportCSV(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws java.sql.SQLException, IOException, ServletException, java.text.ParseException {

    response.setHeader("Content-type", "application/csv");
    response.setHeader("Content-disposition", "attachment; filename=PurchaseDetails.csv");
    response.setHeader("Readonly", "true");

    java.sql.Date fromDate = DataBaseUtil.parseDate(request.getParameter("fromDate"));
    java.sql.Date toDate = DataBaseUtil.parseDate(request.getParameter("toDate"));
    String supplier = request.getParameter("supplier_id");
    String storeId = request.getParameter("store_id");
    String storeTypeId = request.getParameter("store_type_id");
    String centerClause = request.getParameter("centerClause");

    CSVWriter writer = new CSVWriter(response.getWriter(), CSVWriter.DEFAULT_SEPARATOR);

    StoresReportDAO.purchaseDetailsExportCSV(writer, fromDate, toDate, supplier, storeId,
        storeTypeId, centerClause);
    return null;
  }

  /*
   * Purchase Summary Report
   */
  @IgnoreConfidentialFilters
  public ActionForward purchaseSummaryExportCSV(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws java.sql.SQLException, IOException, ServletException, java.text.ParseException {

    response.setHeader("Content-type", "application/csv");
    response.setHeader("Content-disposition", "attachment; filename=PurchaseSummaryDetails.csv");
    response.setHeader("Readonly", "true");

    java.sql.Date fromDate = DataBaseUtil.parseDate(request.getParameter("fromDate"));
    java.sql.Date toDate = DataBaseUtil.parseDate(request.getParameter("toDate"));
    String supplier = request.getParameter("supplier_id");
    String storeId = request.getParameter("store_id");
    String storeTypeId = request.getParameter("store_type_id");
    String centerClause = request.getParameter("centerClause");

    CSVWriter writer = new CSVWriter(response.getWriter(), CSVWriter.DEFAULT_SEPARATOR);

    StoresReportDAO.purchaseSummaryExportCSV(writer, fromDate, toDate, supplier, storeId,
        storeTypeId, centerClause);
    return null;
  }

  /*
   * Pharmacy Sales Details ReportPharmacySalesSummaryExportCSV
   */
  @IgnoreConfidentialFilters
  public ActionForward PharmacySalesDetailExportCSV(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws java.sql.SQLException, IOException, ServletException, java.text.ParseException {

    response.setHeader("Content-type", "application/csv");
    response.setHeader("Content-disposition", "attachment; filename=PharmacySalesDetails.csv");
    response.setHeader("Readonly", "true");

    java.sql.Date fromDate = DataBaseUtil.parseDate(request.getParameter("fromDate"));
    java.sql.Date toDate = DataBaseUtil.parseDate(request.getParameter("toDate"));
    String type = request.getParameter("type");
    String medicineName = request.getParameter("medicineName");

    CSVWriter writer = new CSVWriter(response.getWriter(), CSVWriter.DEFAULT_SEPARATOR);

    StoresReportDAO.PharmacySalesDetailExportCSV(writer, fromDate, toDate, type, medicineName);
    return null;
  }

  /*
   * Pharmacy Sales Summary Report
   */
  @IgnoreConfidentialFilters
  public ActionForward PharmacySalesSummaryExportCSV(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws java.sql.SQLException, IOException, ServletException, java.text.ParseException {

    response.setHeader("Content-type", "application/csv");
    response.setHeader("Content-disposition", "attachment; filename=PharmacySalesSummary.csv");
    response.setHeader("Readonly", "true");

    java.sql.Date fromDate = DataBaseUtil.parseDate(request.getParameter("fromDate"));
    java.sql.Date toDate = DataBaseUtil.parseDate(request.getParameter("toDate"));
    String type = request.getParameter("type");

    CSVWriter writer = new CSVWriter(response.getWriter(), CSVWriter.DEFAULT_SEPARATOR);

    StoresReportDAO.PharmacySalesSummaryExportCSV(writer, fromDate, toDate, type);
    return null;
  }

  /*
   * Doctor Wise Sales Report
   */
  @IgnoreConfidentialFilters
  public ActionForward doctorSalesExportCSV(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws java.sql.SQLException, IOException, ServletException, java.text.ParseException {

    response.setHeader("Content-type", "application/csv");
    response.setHeader("Content-disposition", "attachment; filename=DoctorSales.csv");
    response.setHeader("Readonly", "true");

    java.sql.Date fromDate = DataBaseUtil.parseDate(request.getParameter("fromDate"));
    java.sql.Date toDate = DataBaseUtil.parseDate(request.getParameter("toDate"));

    CSVWriter writer = new CSVWriter(response.getWriter(), CSVWriter.DEFAULT_SEPARATOR);

    StoresReportDAO.doctorSalesExportCSV(writer, fromDate, toDate);
    return null;
  }

  /*
   * Supplier Returns Report
   */
  @IgnoreConfidentialFilters
  public ActionForward supplierReturnsExportCSV(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws java.sql.SQLException, IOException, ServletException, java.text.ParseException {

    response.setHeader("Content-type", "application/csv");
    response.setHeader("Content-disposition", "attachment; filename=SupplierReturns.csv");
    response.setHeader("Readonly", "true");

    java.sql.Date fromDate = DataBaseUtil.parseDate(request.getParameter("fromDate"));
    java.sql.Date toDate = DataBaseUtil.parseDate(request.getParameter("toDate"));
    String supplier = request.getParameter("supplier_id");

    CSVWriter writer = new CSVWriter(response.getWriter(), CSVWriter.DEFAULT_SEPARATOR);

    StoresReportDAO.supplierReturnsExportCSV(writer, fromDate, toDate, supplier);
    return null;
  }

  /*
   * Stock Issue Report
   */
  @IgnoreConfidentialFilters
  public ActionForward stockIssueExportCSV(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws java.sql.SQLException, IOException, ServletException, java.text.ParseException {

    response.setHeader("Content-type", "application/csv");
    response.setHeader("Content-disposition", "attachment; filename=StockIssue.csv");
    response.setHeader("Readonly", "true");

    java.sql.Date fromDate = DataBaseUtil.parseDate(request.getParameter("fromDate"));
    java.sql.Date toDate = DataBaseUtil.parseDate(request.getParameter("toDate"));

    CSVWriter writer = new CSVWriter(response.getWriter(), CSVWriter.DEFAULT_SEPARATOR);

    StoresReportDAO.stockIssueExportCSV(writer, fromDate, toDate);
    return null;
  }

  /*
   * Stock Adjustment Report
   */
  @IgnoreConfidentialFilters
  public ActionForward stockAdjustmentExportCSV(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws java.sql.SQLException, IOException, ServletException, java.text.ParseException {

    response.setHeader("Content-type", "application/csv");
    response.setHeader("Content-disposition", "attachment; filename=StockAdjustment.csv");
    response.setHeader("Readonly", "true");

    java.sql.Date fromDate = DataBaseUtil.parseDate(request.getParameter("fromDate"));
    java.sql.Date toDate = DataBaseUtil.parseDate(request.getParameter("toDate"));

    CSVWriter writer = new CSVWriter(response.getWriter(), CSVWriter.DEFAULT_SEPARATOR);

    StoresReportDAO.stockAdjustmentExportCSV(writer, fromDate, toDate);
    return null;
  }

}
