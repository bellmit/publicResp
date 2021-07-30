package com.insta.hms.wardactivities.patientsummary;

import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.vitalForm.genericVitalFormDAO;
import com.insta.hms.vitalparameter.VitalMasterDAO;
import com.insta.hms.wardactivities.PatientActivitiesDAO;
import com.insta.hms.wardactivities.doctorsnotes.DoctorsNotesDAO;
import com.insta.hms.wardactivities.nursenotes.NurseNotesDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * The Class PatientSummaryAction.
 */
public class PatientSummaryAction extends DispatchAction {

  /** The activity DAO. */
  PatientActivitiesDAO activityDAO = new PatientActivitiesDAO();
  
  /** The doctorsnote DAO. */
  DoctorsNotesDAO doctorsnoteDAO = new DoctorsNotesDAO();
  
  /** The vm DAO. */
  VitalMasterDAO vmDAO = new VitalMasterDAO();

  /**
   * List.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the action forward
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @IgnoreConfidentialFilters
  public ActionForward list(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws SQLException, IOException {

    HttpSession session = request.getSession(false);
    java.util.HashMap urlRightsMap = (java.util.HashMap) session.getAttribute("urlRightsMap");
    java.util.HashMap actionUrlMap = (java.util.HashMap) session.getServletContext()
        .getAttribute("actionUrlMap");
    String patientId = request.getParameter("visit_id");
    if (patientId != null && !patientId.equals("")) {
      VisitDetailsDAO regDao = new VisitDetailsDAO();
      BasicDynaBean bean = regDao.getVisitDetailsWithConfCheck(patientId);
      if (bean == null) {
        // user entered visit id is not a valid patient id.
        FlashScope flash = FlashScope.getScope(request);
        flash.put("error", "No Patient with Id:" + patientId);
        ActionRedirect redirect = new ActionRedirect(mapping.findForward("listRedirect"));
        redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
        return redirect;
      }

      String pageNum = request.getParameter("activitiesPageNum");
      if (urlRightsMap != null && actionUrlMap != null
          && urlRightsMap.get("activities_list").equals("A")) {
        request.setAttribute("activityrights", "yes");
      }
      request.setAttribute("activities", activityDAO.getActivities(patientId, pageNum, "7"));

      String vitalPageNum = request.getParameter("vitalsPageNum");
      vitalPageNum = (vitalPageNum == null || vitalPageNum.isEmpty()) ? null : vitalPageNum;
      List vitalFields = genericVitalFormDAO.getVitalParameterMaster(patientId, "V", "A");
      PagedList pagedList = genericVitalFormDAO.getVisitFormReadings(patientId, vitalPageNum, "7",
          "V");
      List readings = genericVitalFormDAO.getVitalFormDAOValues(pagedList, "V");
      request.setAttribute("vitalsFields", vitalFields);
      request.setAttribute("vitalsDetails", pagedList);
      request.setAttribute("readingsMap",
          ConversionUtils.listBeanToMapListBean(readings, "vital_reading_id"));

      java.util.Map patient = com.insta.hms.Registration.VisitDetailsDAO
          .getPatientVisitDetailsMap(patientId);
      request.setAttribute("referenceRanges", genericVitalFormDAO.getReferenceRange(patient));
      request.setAttribute("prefColorCodes", GenericPreferencesDAO.getAllPrefs());

      String notePageNum = request.getParameter("doctorsNotePageNum");
      notePageNum = (notePageNum == null || notePageNum.isEmpty()) ? null : notePageNum;
      if (urlRightsMap != null && actionUrlMap != null
          && urlRightsMap.get("doctors_note").equals("A")) {
        request.setAttribute("docrights", "yes");
      }
      request.setAttribute("doctorsnote",
          DoctorsNotesDAO.getDoctorsNotes(patientId, notePageNum, "3"));

      String nursePageNum = request.getParameter("nurseNotePageNum");
      nursePageNum = (nursePageNum == null || nursePageNum.isEmpty()) ? null : nursePageNum;
      if (urlRightsMap != null && actionUrlMap != null
          && urlRightsMap.get("nurse_note").equals("A")) {
        request.setAttribute("nursrights", "yes");
      }
      request.setAttribute("nursenote", NurseNotesDAO.getNurseNotes(patientId, nursePageNum, "3"));
    }
    return mapping.findForward("list");
  }

}
