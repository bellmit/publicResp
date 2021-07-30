package com.bob.hms.report;

import au.com.bytecode.opencsv.CSVWriter;
import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.CommonReportAction;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * The Class IpStatsReportAction.
 */
public class IpStatsReportAction extends CommonReportAction {

  
  @Override
  public ActionForward getCsv(ActionMapping mapping, ActionForm form, HttpServletRequest req,
      HttpServletResponse res) throws IOException, SQLException, java.text.ParseException {

    String query = "SELECT d.dept_name, admits, noofdischarge, noofdeath, mlc, ipdays, beds, "
        + "abo, bo_rate  FROM ( " + "SELECT admitted_dept,  COALESCE(beds, 0) AS beds, "
        + "COALESCE(admits, 0) AS admits, " + "COALESCE(noofdeath, 0) AS noofdeath, COALESCE"
        + "(noofdischarge, 0) AS noofdischarge, " + "COALESCE(ipdays, 0) AS ipdays, COALESCE"
        + "(mlc, 0) AS mlc, " + "(COALESCE(ipdays, 0) /365)::numeric(10, 2) as abo, " + "(("
        + "(COALESCE(ipdays, 0) /365)/COALESCE(beds, 0))*100)::numeric(10, 2) as bo_rate "
        + "FROM ( " + "SELECT admitted_dept, COUNT(distinct bed_id) AS beds " + "FROM "
        + "patient_registration " + "JOIN ip_bed_details using (patient_id) " + "JOIN "
        + "bed_names using (bed_id) " + "WHERE reg_date BETWEEN ? AND ? " + " AND ( 0 = ? "
        + " OR  center_id = ?) " + "GROUP BY admitted_dept) AS beds " + "LEFT JOIN " + ""
        + "(SELECT admitted_dept,COUNT(*) as admits " + "FROM patient_registration WHERE "
        + "visit_type = 'i' AND coalesce(discharge_type, '')!='Admission Cancelled' AND "
        + "reg_date BETWEEN ? and ? " + " AND ( 0 = ?  OR  center_id = ?) " + "GROUP BY "
        + "admitted_dept) AS admits USING (admitted_dept) " + "LEFT JOIN " + "(SELECT "
        + "admitted_dept,COUNT(*) AS noofdischarge " + "FROM patient_registration " + "WHERE"
        + " visit_type = 'i' AND status = 'I' AND coalesce(discharge_type, '')!='Admission "
        + "Cancelled' AND discharge_date BETWEEN ? and ? " + " AND ( 0 = ?  OR  center_id = "
        + "?) " + "GROUP BY admitted_dept) AS disc USING (admitted_dept) " + "LEFT JOIN "
        + "(SELECT admitted_dept, COUNT(*) AS noofdeath " + "FROM patient_registration "
        + "WHERE visit_type = 'i' AND discharge_type = 'Expiry' AND discharge_date BETWEEN ?"
        + " and ? " + " AND ( 0 = ?  OR  center_id = ?) " + "GROUP BY admitted_dept) AS "
        + "death USING (admitted_dept) " + "LEFT JOIN (SELECT admitted_dept, (COALESCE"
        + "(EXTRACT(DAY FROM SUM((coalesce(end_date, LOCALTIMESTAMP(0))-start_date))), 0) " 
        + "+ " + "(COALESCE(EXTRACT(HOUR FROM SUM((coalesce(end_date, LOCALTIMESTAMP(0))"
        + "-start_date))), 0))/24)::NUMERIC(10, 2)  AS ipdays " + "FROM patient_registration"
        + " JOIN ip_bed_details using (patient_id) " + "JOIN bed_names using (bed_id) "
        + "WHERE reg_date BETWEEN ? AND ? " + " AND ( 0 = ?  OR  center_id = ?) " + "GROUP "
        + "BY admitted_dept) AS ip USING (admitted_dept) " + "LEFT JOIN (SELECT "
        + "admitted_dept, COUNT(mlc_status) AS mlc " + "FROM patient_registration WHERE "
        + "visit_type = 'i' AND mlc_status = 'Y' AND coalesce(discharge_type, '')"
        + "!='Admission Cancelled' AND reg_date BETWEEN ? AND ? " + " AND ( 0 = ?  OR  "
        + "center_id = ?) " + "GROUP BY admitted_dept) AS mlc USING (admitted_dept)) foo "
        + "JOIN department d ON dept_id = admitted_dept";

    String filename = mapping.getPath();
    filename = filename.substring(filename.lastIndexOf("/") + 1);
    res.setHeader("Content-type", "application/csv");
    res.setHeader("Content-disposition", "attachment; filename=" + filename + ".csv");
    Connection con = null;
    PreparedStatement ps = null;
    String centerFilter = req.getParameter("center") == null
        || req.getParameter("center").equals("")
            ? req.getAttribute("center") == null ? "0" : (String) req.getAttribute("center")
            : req.getParameter("center");
    int centerId = (Integer) req.getSession(false).getAttribute("centerId");
    int viewCenter = Integer.parseInt(centerFilter);

    int center = centerId != 0 ? centerId : viewCenter;

    try {
      con = DataBaseUtil.getConnection(60);
      ps = con.prepareStatement(query);

      ps.setDate(1, DataBaseUtil.parseDate(req.getParameter("fromDate")));
      ps.setDate(2, DataBaseUtil.parseDate(req.getParameter("toDate")));

      ps.setInt(3, center);
      ps.setInt(4, center);

      ps.setDate(5, DataBaseUtil.parseDate(req.getParameter("fromDate")));
      ps.setDate(6, DataBaseUtil.parseDate(req.getParameter("toDate")));

      ps.setInt(7, center);
      ps.setInt(8, center);

      ps.setDate(9, DataBaseUtil.parseDate(req.getParameter("fromDate")));
      ps.setDate(10, DataBaseUtil.parseDate(req.getParameter("toDate")));

      ps.setInt(11, center);
      ps.setInt(12, center);

      ps.setDate(13, DataBaseUtil.parseDate(req.getParameter("fromDate")));
      ps.setDate(14, DataBaseUtil.parseDate(req.getParameter("toDate")));

      ps.setInt(15, center);
      ps.setInt(16, center);

      ps.setDate(17, DataBaseUtil.parseDate(req.getParameter("fromDate")));
      ps.setDate(18, DataBaseUtil.parseDate(req.getParameter("toDate")));

      ps.setInt(19, center);
      ps.setInt(20, center);

      ps.setDate(21, DataBaseUtil.parseDate(req.getParameter("fromDate")));
      ps.setDate(22, DataBaseUtil.parseDate(req.getParameter("toDate")));

      ps.setInt(23, center);
      ps.setInt(24, center);

      ResultSet rs = ps.executeQuery();

      CSVWriter writer = new CSVWriter(res.getWriter(), CSVWriter.DEFAULT_SEPARATOR);
      writer.writeAll(rs, true);
      writer.flush();
    } finally {
      ps.close();
      con.close();
    }
    return null;
  }
}
