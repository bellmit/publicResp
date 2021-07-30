package com.bob.hms.report;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.adminmasters.bedmaster.BedMasterDAO;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.ReportPrinter;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import flexjson.JSONSerializer;
import net.sf.jasperreports.engine.JRException;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The Class BedOccupancyReportAction.
 */
public class BedOccupancyReportAction extends DispatchAction {

  /** The logger. */
  static Logger logger = LoggerFactory
      .getLogger(BedOccupancyReportAction.class);

  /**
   * Gets the bed occupancy screen.
   *
   * @param mapping the mapping
   * @param af the af
   * @param request the request
   * @param response the response
   * @return the bed occupancy screen
   */
  public ActionForward getBedOccupancyScreen(ActionMapping mapping, ActionForm af,
      HttpServletRequest request, HttpServletResponse response) {

    String wardNames = null;
    ArrayList wardNameList = null;
    wardNames = "select ward_no,ward_name from ward_names where status='A' ";
    wardNameList = DataBaseUtil.queryToArrayList(wardNames);
    request.setAttribute("wardNameList", wardNameList);

    return mapping.findForward("getoccupancyscreen");
  }

  /**
   * Gets the bed occupancy report.
   *
   * @param mapping the mapping
   * @param af the af
   * @param request the request
   * @param response the response
   * @return the bed occupancy report
   * @throws JRException the JR exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws SQLException the SQL exception
   */
  public ActionForward getBedOccupancyReport(ActionMapping mapping, ActionForm af,
      HttpServletRequest request, HttpServletResponse response)
      throws JRException, IOException, SQLException {

    String[] wards = request.getParameterValues("allWardNames");
    String wardId = "  where  bsr.ward_no in (";
    String occupancy = "";
    if (wards.length > 0) {
      wardId += wards[0];
    }
    for (int i = 1; i < wards.length; i++) {

      wardId = wardId + "," + (wards[i]);
    }
    wardId = wardId + " ) ";

    HashMap map = new HashMap();
    String occupied = request.getParameter("occupied");
    String vacant = request.getParameter("vacant");
    String whereCodition = "";

    if (occupied != null && vacant != null) {
      occupancy = " and bsr.occupancy in (" + occupied + "," + vacant + ")";
    } else if (vacant == null) {
      occupancy = " and bsr.occupancy in (" + occupied + ")";
    } else {
      occupancy = " and bsr.occupancy in (" + vacant + ")";
    }

    whereCodition = wardId + occupancy;
    map.put("whereCodition", whereCodition);
    if (request.getParameter("orderby").equals("patient_name")) {
      map.put("orderby", ",bsr.patient_name");
    } else if (request.getParameter("orderby").equals("bed_id")) {
      map.put("orderby", ",bed_id");
    }

    ReportPrinter.printPdfStream(request, response, "BedOccupancyReport", map);
    return mapping.findForward("getoccupancyscreen");
  }

  /**
   * Gets the bed utilization screen.
   *
   * @param mapping the mapping
   * @param af the af
   * @param request the request
   * @param response the response
   * @return the bed utilization screen
   * @throws SQLException the SQL exception
   */
  public ActionForward getBedUtilizationScreen(ActionMapping mapping, ActionForm af,
      HttpServletRequest request, HttpServletResponse response) throws SQLException {
    int centerId = (Integer) request.getSession().getAttribute("centerId");
    boolean multiCentered = GenericPreferencesDAO.getGenericPreferences()
        .getMax_centers_inc_default() > 1;
    JSONSerializer js = new JSONSerializer().exclude("class");

    List wardNames = BedMasterDAO.getAllWardNames(centerId, multiCentered);
    request.setAttribute("wardNameList", wardNames);
    request.setAttribute("wardNameListJSON",
        js.serialize(ConversionUtils.copyListDynaBeansToMap(wardNames)));
    request.setAttribute("centers", CenterMasterDAO.getAllCentersExceptSuper());
    request.setAttribute("multiCenters",
        GenericPreferencesDAO.getGenericPreferences().getMax_centers_inc_default() > 1);
    return mapping.findForward("getBedUtilizationReport");
  }

  /**
   * Gets the bed utilization report.
   *
   * @param mapping the mapping
   * @param af the af
   * @param request the request
   * @param response the response
   * @return the bed utilization report
   * @throws JRException the JR exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws SQLException the SQL exception
   */
  public ActionForward getBedUtilizationReport(ActionMapping mapping, ActionForm af,
      HttpServletRequest request, HttpServletResponse response)
      throws JRException, IOException, SQLException {
    String[] wards = request.getParameterValues("allWardNames");
    String[] departments = request.getParameterValues("dept_name");
    String[] centers = request.getParameterValues("centerId");
    String wardId = "#";
    String wardsCondition = "#";
    String deptIn = "#";
    String centerCond = "#";
    String left = "  ";
    for (int i = 0; i < wards.length; i++) {

      if (wards[i].equals("")) {
        continue;
      }
      wardId = wardId.replace("#", "  wn.ward_no in ( '");
      wardId = wardId + (wards[i]) + (i + 1 == wards.length ? "')" : "','");
      wardsCondition = wardsCondition.replace("#", " AND wn.ward_no in ( '");
      wardsCondition = wardsCondition + (wards[i]) + (i + 1 == wards.length ? "')" : "','");
    }
    if (departments != null) {
      left = " LEFT ";
      for (int i = 0; i < departments.length; i++) {
        if (departments[i].equals("")) {
          continue;
        }
        deptIn = deptIn.replace("#", " and pr.dept_name in ( '");
        deptIn = deptIn + (departments[i]) + (i + 1 == departments.length ? "')" : "','");
      }
    } else {
      deptIn = "";
    }

    if (centers != null) {
      for (int i = 0; i < centers.length; i++) {
        if (centers[i].equals("")) {
          continue;
        }
        centerCond = centerCond.replace("#", " wn.center_id IN ( ");
        centerCond = centerCond + centers[i] + (i + 1 == centers.length ? ")" : ",");
      }
    }
    wardId = wardId.replace("#", "");
    wardsCondition = wardsCondition.replace("#", "");
    deptIn = deptIn.replace("#", "");
    centerCond = centerCond.replace("#", "");

    HashMap map = new HashMap();
    String whereWardsCondition = "";
    String andWardsCondition = "";
    String whereCondition = "";
    
    whereCondition = wardId;
    map.put("whereWardsCondition",
        whereWardsCondition = wardId.isEmpty() ? "" : (" WHERE " + wardId));
    map.put("andWardsCondition", andWardsCondition = wardId.isEmpty() ? "" : (" AND " + wardId));
    map.put("orderby", ",bed_id");
    map.put("departments", deptIn);
    map.put("centerCond", (wardId.isEmpty() ? (centerCond.isEmpty() ? "" : " WHERE ")
        : (centerCond.isEmpty() ? "" : " AND ")) + centerCond);

    ReportPrinter.printPdfStream(request, response, "BedUtilizationReport", map);
    return mapping.findForward("getBedUtilizationReport");
  }

}
