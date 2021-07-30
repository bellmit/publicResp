package com.insta.hms.patient;

import com.bob.hms.common.DateUtil;
import com.bob.hms.diag.ohsampleregistration.IncomingPatientDAO;
import com.insta.hms.Registration.PatientDetailsDAO;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;

import flexjson.JSONSerializer;
import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.http.HttpHeaders;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;



/**
 * Action class providing various utility Ajax methods for searching for
 * patients based on various criteria.
 */
public class PatientSearchAction extends DispatchAction {

  static Logger logger = LoggerFactory.getLogger(PatientSearchAction.class);

  private static JSONSerializer js = new JSONSerializer().exclude("class");

  /**
   * find method, which returns a JSON based on a query that takes a single string and searches
   * among various attributes of the patient
   * Whitelisted since it uses allmrno view.
   *
   * @param actionMapping the m
   * @param form the f
   * @param req the req
   * @param res the res
   * @return the action forward
   * @throws SQLException the SQL exception
   * @throws ParseException the parse exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @IgnoreConfidentialFilters
  public ActionForward findPatientsJson(ActionMapping actionMapping, ActionForm form,
      HttpServletRequest req, HttpServletResponse res)
      throws SQLException, ParseException, IOException {

    res.setContentType("text/plain");
    res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    String query = req.getParameter("query");
    String searchType = req.getParameter("searchType");
    String visitType = req.getParameter("visitType");
    String isReg = req.getParameter("isRegScreen");
    String smartCard = req.getParameter("smartCard");
    String firstName = req.getParameter("patientName");
    String middleName = req.getParameter("middleName");
    String lastName = req.getParameter("lastName");
    String patientGender = req.getParameter("patientGender");
    String patientIdNumber = req.getParameter("nid");
    String sqlDOB = req.getParameter("sqlDOB");
    Boolean scheduler = Boolean.parseBoolean(req.getParameter("scheduler"));
    HttpSession session = req.getSession(false);
    int centerId = (Integer) session.getAttribute("centerId");
    int roleId = (Integer) session.getAttribute("roleId");
    int sampleCollectionCenterId = (Integer) req.getSession(false)
        .getAttribute("sampleCollectionCenterId");
    Boolean showDuplicateMrNos = new Boolean(req.getParameter("showDuplicateMrNos"));
    Boolean showOtherCenterPatients = false;
    if (new Boolean(isReg)) {
      showOtherCenterPatients = roleId == 1 || roleId == 2
          || ((HashMap) session.getAttribute("actionRightsMap")).get("show_other_center_patients")
              .equals("A");
    }

    BasicDynaBean genprefs = GenericPreferencesDAO.getAllPrefs();

    if (visitType == null) {
      visitType = "all";
    }

    if (searchType == null) {
      searchType = "";
    }
    String status = req.getParameter("status");
    if (status == null) {
      status = "";
    }

    List<BasicDynaBean> patientList = null;
    try {
      if (null != smartCard && smartCard.equals("yes")) {
        BasicDynaBean patDetailsBean = new PatientDetailsDAO().getBean();
        patDetailsBean.set("patient_name", firstName);
        patDetailsBean.set("middle_name", middleName);
        patDetailsBean.set("last_name", lastName);
        patDetailsBean.set("patient_gender", patientGender);

        patDetailsBean.set("remarks", sqlDOB);
        patDetailsBean.set("patient_care_oftext", "");
        patDetailsBean.set("patient_phone", "");
        patDetailsBean.set("government_identifier", patientIdNumber);
        patientList = new PatientDetailsDAO().checkDetailsExist(patDetailsBean);

      } else if (searchType.equals("mrNo")) {
        if (status.equals("active")) {
          patientList = PatientDetailsDAO.findPatientsActive(query, 0, visitType,
              showDuplicateMrNos, centerId, sampleCollectionCenterId,scheduler);
        } else if (status.equals("inactive")) {
          patientList = PatientDetailsDAO.findPatientsInactive(query, 0, visitType,
              showDuplicateMrNos, centerId, sampleCollectionCenterId, showOtherCenterPatients,
              genprefs);
        } else if (status.equals("all")) {
          patientList = PatientDetailsDAO.findPatients(query, 0, visitType, showDuplicateMrNos,
              centerId, sampleCollectionCenterId, showOtherCenterPatients, genprefs,scheduler);
        }
      } else if (searchType.equals("blockInactiveIp")) {
        if (status.equals("active")) {
          patientList = PatientDetailsDAO.findPatientsWithVisitsWithStatus(query, 0, visitType,
              centerId, sampleCollectionCenterId,"A");
        } else if (status.equals("inactive")) {
          patientList = PatientDetailsDAO.findPatientsWithVisitsWithStatus(query, 0, visitType,
              centerId, sampleCollectionCenterId,"I");
        } else if (status.equals("all")) {
          patientList = PatientDetailsDAO.findAllPatientsWithVisitsForSales(query, 0, visitType,
              centerId, sampleCollectionCenterId);
        }
      } else if (searchType.equals("blockInactiveOp")) {
        if (status.equals("active")) {
          patientList = PatientDetailsDAO.findPatientsWithVisitsWithStatus(query, 0, visitType,
              centerId, sampleCollectionCenterId,"A");
        } else if (status.equals("inactive")) {
          patientList = PatientDetailsDAO.findPatientsWithVisitsWithStatus(query, 0, visitType,
              centerId, sampleCollectionCenterId,"I");
        } else if (status.equals("all")) {
          patientList = PatientDetailsDAO.findOpPatientsWithVisitsForSales(query, 0, visitType,
              centerId, sampleCollectionCenterId);
        }
      } else if (searchType.equals("blockBoth")) {
        if (status.equals("active")) {
          patientList = PatientDetailsDAO.findPatientsWithVisitsWithStatus(query, 0, visitType,
              centerId, sampleCollectionCenterId,"A");
        } else if (status.equals("inactive")) {
          patientList = PatientDetailsDAO.findPatientsWithVisitsWithStatus(query, 0, visitType,
              centerId, sampleCollectionCenterId,"I");
        } else if (status.equals("all")) {
          patientList = PatientDetailsDAO.findBothPatientsWithVisitsForSales(query, 0, visitType,
              centerId, sampleCollectionCenterId);
        }
      } else {
        if (status.equals("active")) {
          patientList = PatientDetailsDAO.findPatientsWithVisitsWithStatus(query, 0, visitType,
              centerId, sampleCollectionCenterId,"A");
        } else if (status.equals("inactive")) {
          patientList = PatientDetailsDAO.findPatientsWithVisitsWithStatus(query, 0, visitType,
              centerId, sampleCollectionCenterId,"I");
        } else if (status.equals("all")) {
          patientList = PatientDetailsDAO.findAllPatientsWithVisits(query, 0, visitType, centerId,
              sampleCollectionCenterId);
        }
      }
      for (BasicDynaBean bean : patientList) {
        boolean precise = (bean.get("dateofbirth") != null);
        if (bean.get("expected_dob") != null) {
          String ageText = DateUtil.getAgeText((java.sql.Date) bean.get("expected_dob"), precise);
          if (!precise) {
            bean.set("age", Integer.parseInt(ageText.substring(0, ageText.length() - 1)));
            bean.set("age_in", ageText.substring(ageText.length() - 1).toUpperCase());
          }
        }
      }
    } catch (SQLException se) {
      logger.error("SQLException", se);
    }
    HashMap retVal = new HashMap();
    retVal.put("result", ConversionUtils.copyListDynaBeansToMap(patientList));

    js.deepSerialize(retVal, res.getWriter());
    res.flushBuffer();

    return null;
  }

  /**
   * Gets the mr no.
   *
   * @param actionMapping the m
   * @param form the f
   * @param req the req
   * @param res the res
   * @return the mr no
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @IgnoreConfidentialFilters
  public ActionForward getMrNo(ActionMapping actionMapping, ActionForm form, HttpServletRequest req,
      HttpServletResponse res) throws SQLException, IOException {
    String query = req.getParameter("query");
    String visitType = req.getParameter("visitType");
    String status = req.getParameter("status");
    Boolean showDuplicateMrNos = new Boolean(req.getParameter("showDuplicateMrNos"));
    HttpSession session = req.getSession(false);
    int centerId = (Integer) session.getAttribute("centerId");
    int roleId = (Integer) session.getAttribute("roleId");
    String isReg = req.getParameter("isRegScreen");

    BasicDynaBean genprefs = GenericPreferencesDAO.getAllPrefs();
    Boolean showOtherCenterPatients = false;
    if (new Boolean(isReg)) {
      showOtherCenterPatients = roleId == 1 || roleId == 2
          || ((HashMap) session.getAttribute("actionRightsMap")).get("show_other_center_patients")
              .equals("A");
    }

    if (status == null) {
      status = "";
    }
    if (visitType == null) {
      visitType = "all";
    }
    res.setContentType("text/plain");
    res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    String mrNo = null;
    String error = "";
    try {
      mrNo = PatientDetailsDAO.getMrNo(query, visitType, status, showDuplicateMrNos, centerId,
          showOtherCenterPatients, genprefs);
    } catch (SQLException exception) {
      error = exception.getLocalizedMessage();
      logger.error("", exception);
      return null; // dont throw the exception because it is a ajax request
    } finally {
      Map results = new HashMap();
      results.put("mrNo", mrNo);
      results.put("msg", error != null ? error : "not found");
      js.deepSerialize(results, res.getWriter());
    }
    return null;
  }

  /**
   * Which will returns the latest active visit id. Whitelisted since it uses visit_search_view.
   *
   * @param actionMapping the m
   * @param form the f
   * @param req the req
   * @param res the res
   * @return the visit id
   */
  @IgnoreConfidentialFilters
  public ActionForward getVisitId(ActionMapping actionMapping, ActionForm form,
      HttpServletRequest req, HttpServletResponse res) throws IOException {
    String query = req.getParameter("query");
    String visitType = req.getParameter("visitType");
    String status = req.getParameter("status");
    if (status == null) {
      status = "";
    }
    if (visitType == null) {
      visitType = "all";
    }      
    res.setContentType("text/plain");
    res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    String visitId = null;
    String error = "";
    try {
      visitId = PatientDetailsDAO.getVisitId(query, visitType, status);
    } catch (SQLException ex) {
      error = ex.getLocalizedMessage();
      logger.error("", ex);
      return null; // dont throw the exception because it is a ajax request
    } finally {
      Map results = new HashMap();
      results.put("visitId", visitId);
      results.put("msg", error != null ? error : "not found");
      js.deepSerialize(results, res.getWriter());
    }
    return null;
  }

  /**
   * Find incoming patients json.
   *
   * @param actionMapping the m
   * @param actionForm the f
   * @param req the req
   * @param res the res
   * @return the action forward
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @IgnoreConfidentialFilters
  public ActionForward findIncomingPatientsJson(ActionMapping actionMapping, ActionForm actionForm,
      HttpServletRequest req, HttpServletResponse res) throws SQLException, IOException {

    res.setContentType("text/plain");
    res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");

    String query = req.getParameter("query");

    List patientList = IncomingPatientDAO.findPatients(query, 20);

    HashMap retVal = new HashMap();
    retVal.put("result", ConversionUtils.copyListDynaBeansToMap(patientList));

    js.deepSerialize(retVal, res.getWriter());
    res.flushBuffer();

    return null;
  }

}
