package com.insta.hms.tokendisplay;

import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.master.DepartmentMaster.DepartmentMasterDAO;
import com.insta.hms.master.DoctorMaster.DoctorMasterDAO;
import com.insta.hms.outpatient.DoctorConsultationDAO;
import com.insta.hms.resourcescheduler.ResourceDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

// TODO: Auto-generated Javadoc
/**
 * The Class ConsultationTokenDisplayAction.
 *
 * @author krishna
 */
public class ConsultationTokenDisplayAction extends DispatchAction {

  /**
   * Sets the prefs.
   *
   * @param mapping
   *          the mapping
   * @param from
   *          the from
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the action forward
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws ServletException
   *           the servlet exception
   */
  @IgnoreConfidentialFilters
  public ActionForward setPrefs(ActionMapping mapping, ActionForm from, HttpServletRequest request,
      HttpServletResponse response) throws SQLException, IOException, ServletException {
    String[] doctors = request.getParameterValues("selected_doctors");
    HttpSession session = request.getSession(false);
    session.setAttribute("consultation_doctors", doctors);
    session.setAttribute("interval", request.getParameter("interval"));
    session.setAttribute("no_of_records", request.getParameter("no_of_records"));
    session.setAttribute("display_patient_name", request.getParameter("display_patient_name"));
    return mapping.findForward("tokenDisplayRedirect");
  }

  /**
   * Gets the display system.
   *
   * @param mapping
   *          the mapping
   * @param from
   *          the from
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the display system
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws ServletException
   *           the servlet exception
   * @throws Exception
   *           the exception
   */
  @IgnoreConfidentialFilters
  public ActionForward getDisplaySystem(ActionMapping mapping, ActionForm from,
      HttpServletRequest request, HttpServletResponse response)
          throws SQLException, IOException, ServletException, Exception {
    Date today = new Date();
    Calendar cal = Calendar.getInstance();
    cal.setTime(today);
    int weekDayNo = (cal.get(Calendar.DAY_OF_WEEK) - 1);
    List<BasicDynaBean> doctorAvailabilityTimings = null;
    List<BasicDynaBean> doctorAvailabilityTimingsList = null;

    int doctorNo = 0;
    int pageNumber = 1;
    if (request.getParameter("doctorNo") != null && !request.getParameter("doctorNo").equals("")) {
      doctorNo = Integer.parseInt(request.getParameter("doctorNo"));
    }
    if (request.getParameter("pageNumber") != null
        && !request.getParameter("pageNumber").equals("")) {
      pageNumber = Integer.parseInt(request.getParameter("pageNumber"));
    }
    
    HttpSession session = request.getSession(false);
    String[] doctors = (String[]) session.getAttribute("consultation_doctors");
    doctorAvailabilityTimings = new ResourceDAO().getResourceAvailabilities("DOC",
        new java.sql.Date(today.getTime()), doctors[doctorNo], null, null);

    if (doctorAvailabilityTimings == null || doctorAvailabilityTimings.size() < 1) {
      doctorAvailabilityTimings = new ResourceDAO()
          .getResourceDefaultAvailabilities(doctors[doctorNo], weekDayNo, "DOC", null, null);
    }

    if (doctorAvailabilityTimings == null || doctorAvailabilityTimings.size() < 1) {
      doctorAvailabilityTimings = new ResourceDAO().getResourceDefaultAvailabilities("*", weekDayNo,
          "DOC", null, null);
    }

    if (doctorAvailabilityTimings != null || doctorAvailabilityTimings.size() > 0) {
      doctorAvailabilityTimingsList = new ArrayList<BasicDynaBean>();
      for (int i = 0; i < doctorAvailabilityTimings.size(); i++) {
        if (doctorAvailabilityTimings.get(i).get("availability_status").equals("A")) {
          doctorAvailabilityTimingsList.add(doctorAvailabilityTimings.get(i));
        }
      }
    }

    int recordsPerPage = Integer.parseInt((String) session.getAttribute("no_of_records"));
    PagedList list = DoctorConsultationDAO.getNewConsultations(doctors[doctorNo], pageNumber,
        recordsPerPage);
    request.setAttribute("pagedList", list);
    BasicDynaBean doctordetails = new DoctorMasterDAO().findByKey("doctor_id", doctors[doctorNo]);
    request.setAttribute("doctordetails", doctordetails);
    BasicDynaBean doctordepartment = new DepartmentMasterDAO().findByKey("dept_id",
        doctordetails.get("dept_id"));
    request.setAttribute("doctordepartment", doctordepartment);
    request.setAttribute("doctor_timings", doctorAvailabilityTimingsList);
    return mapping.findForward("tokenDisplay");
  }

}
