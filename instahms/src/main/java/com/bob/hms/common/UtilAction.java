package com.bob.hms.common;

import com.insta.hms.common.annotations.IgnoreConfidentialFilters;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The Class UtilAction.
 */
public class UtilAction extends DispatchAction {

  /**
   * The time of the Db server is what we use everywhere as the "current" time, but it could be
   * displayed in different timezones. What we need to return is only the epoch time, that the UI
   * can convert to a display time based on the current timezone (typically, that of the browser),
   * the timezone of the server is quite irrelevant.
   */
  @IgnoreConfidentialFilters
  public ActionForward getEpochTime(ActionMapping map, ActionForm form, HttpServletRequest req,
      HttpServletResponse res) {
    try (Connection con = DataBaseUtil.getConnection()) {
      if (con != null) {
        int time = 0;
        try (Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT EXTRACT(EPOCH FROM CURRENT_TIMESTAMP)")) {
          while (rs.next()) {
            time = rs.getInt(1);
          }
          res.setContentType("text/plain");
          res.setHeader("Cache-Control", "no-cache");
          res.getWriter().write("" + time);
        }
      }
    } catch (SQLException exception) {
      Logger.logException("Exception Raised in getTime method of UtilAction", exception);
    } catch (IOException exception) {
      Logger.logException("Exception Raised in getTime method of UtilAction", exception);
    }
    return null;
  }

  /**
   * Gets the time.
   *
   * @param map  the map
   * @param form the form
   * @param req  the req
   * @param res  the res
   * @return the time
   */
  public ActionForward getTime(ActionMapping map, ActionForm form, HttpServletRequest req,
      HttpServletResponse res) {

    try (Connection con = DataBaseUtil.getConnection()) {
      if (con != null) {
        String time = null;
        try (Statement st = con.createStatement();
            ResultSet rs = st
                .executeQuery("SELECT to_char(CURRENT_TIMESTAMP,'HH24:MI:SS DD-MON-YYYY')")) {
          while (rs.next()) {
            time = rs.getString(1);
          }
          res.setContentType("text/plain");
          res.setHeader("Cache-Control", "no-cache");
          res.getWriter().write(time);

        }
      }
    } catch (SQLException exception) {
      Logger.logException("Exception Raised in getTime method of UtilAction", exception);

    } catch (IOException exception) {
      Logger.logException("Exception Raised in getTime method of UtilAction", exception);

    }
    return null;
  }

  /**
   * Gets the mrno.
   *
   * @param map  the map
   * @param form the form
   * @param req  the req
   * @param res  the res
   * @return the mrno
   */
  public ActionForward getMrno(ActionMapping map, ActionForm form, HttpServletRequest req,
      HttpServletResponse res) {
    Logger.log("Enter getMrno method of UtilAction");
    Connection con = DataBaseUtil.getConnection();
    Statement st = null;
    ResultSet rs = null;
    String xml = null;
    StringBuffer buf = new StringBuffer();
    try {
      String reg = req.getParameter("list");
      st = con.createStatement();
      if (reg != null) {
        if (reg.equals("opreg")) {
          rs = st.executeQuery(
              "select mr_no, patient_name from patient_details " + " where cflag is null and "
                  + " mr_no not in(select mr_no from PATIENT_REGISTRATION where cflag=0) "
                  + " order by mr_no");
        } else if (reg.equals("ipreg")) {
          Logger.log("in ipreg");
          // rs= st.executeQuery("select mr_no, patient_name from patient_details where cflag is
          // null and mr_no not in(select mr_no from PATIENT_REGISTRATION where cflag=0) order by
          // mr_no");
          rs = st.executeQuery("select distinct mr_no,patient_name from patient_details "
              + " where mr_no not in(select distinct mr_no from patient_registration) and "
              + " cflag is null union "
              + " select distinct mr_no,patient_name from patient_details "
              + " where mr_no not in((select distinct mr_no from patient_registration "
              + " where patient_id like 'IP%' and status='A') "
              + " union( select  distinct mr_no from patient_registration "
              + " where patient_id like 'OP%')"
              + " union( select  distinct mr_no from patient_registration "
              + " where patient_id like 'DIA%')) " + "and cflag is null");
          Logger.log("ip-------rs------------>" + rs);
        } else if (reg.equals("opipreg")) {
          rs = st.executeQuery("select distinct reg.mr_no, det.patient_name from "
              + " patient_registration reg, patient_details det "
              + " where reg.mr_no=det.mr_no and(reg.patient_id like 'OP%') "
              + " and reg.cflag=0 order by mr_no");
        } else if (reg.equals("diareg")) {
          rs = st
              .executeQuery("select distinct det.mr_no,det.patient_name from patient_details det "
                  + " where (det.cflag is null) and (det.mr_no not in "
                  + " (select reg.mr_no from patient_registration reg "
                  + " where reg.mr_no= det.mr_no )) " + " order by det.mr_no");
        }
      } else {

        rs = st.executeQuery("select distinct det.mr_no,det.patient_name from patient_details det "
            + " where (det.cflag is null) and "
            + " (det.mr_no not in (select reg.mr_no from patient_registration reg "
            + " where reg.mr_no= det.mr_no )) order by det.mr_no");
      }
      if (rs != null) {
        while (rs.next()) {
          buf.append(rs.getString(1) + " nm56 ");
          buf.append(rs.getString(2) + " nm57 ");
        }
      }

    } catch (SQLException exception) {
      Logger.logException("Exception Raised in getMrno method of UtilAction", exception);
    } finally {
      xml = buf.toString();
      Logger.log(xml);
      res.setContentType("text/xml");
      res.setHeader("Cache-Control", "no-cache");
      try {
        PrintWriter writer = res.getWriter();
        writer.write(xml);
        if (!st.isClosed()) {
          st.close();
        }
        if (!rs.isClosed()) {
          rs.close();
        }
        if (!con.isClosed()) {
          con.close();
        }

      } catch (Exception exception) {
        exception.printStackTrace();
      }
    }

    return null;

  }

}
