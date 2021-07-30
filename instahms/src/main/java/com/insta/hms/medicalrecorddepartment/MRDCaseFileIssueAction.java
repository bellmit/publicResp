package com.insta.hms.medicalrecorddepartment;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.insta.hms.Registration.PatientDetailsDAO;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.MRDCaseFileUsers.MRDCaseFileUsersDAO;
import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.http.HttpHeaders;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * The Class MRDCaseFileIssueAction.
 *
 * @author lakshmi.p
 */
public class MRDCaseFileIssueAction extends BaseAction {

  /** The log. */
  static Logger log = LoggerFactory.getLogger(MRDCaseFileIssueAction.class);
  
  private static final GenericDAO mrdCaseFileAttributesDAO =
      new GenericDAO("mrd_casefile_attributes");
  
  private static final GenericDAO patientDetailsDAO = new GenericDAO("patient_details");

  /**
   * Issue MRD casefile screen.
   *
   * @param am
   *          the am
   * @param af
   *          the af
   * @param req
   *          the req
   * @param res
   *          the res
   * @return the action forward
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws SQLException
   *           the SQL exception
   * @throws ParseException
   *           the parse exception
   */
  @IgnoreConfidentialFilters
  public ActionForward issueMRDCasefileScreen(ActionMapping am, ActionForm af,
      HttpServletRequest req, HttpServletResponse res)
      throws IOException, SQLException, ParseException {

    List depUnit = MRDCasefileIndentDAO.getDepartmentUnits();
    req.setAttribute("depUnitList", new JSONSerializer().serialize(depUnit));

    req.setAttribute("mrdscreen", req.getParameter("_mrdscreen"));
    req.setAttribute("GMRDDetails", GenericPreferencesDAO.getGenericPreferences());
    return am.findForward("issuescreen");
  }

  /**
   * Return MRD casefile screen.
   *
   * @param am
   *          the am
   * @param af
   *          the af
   * @param req
   *          the req
   * @param res
   *          the res
   * @return the action forward
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws SQLException
   *           the SQL exception
   * @throws ParseException
   *           the parse exception
   */
  @IgnoreConfidentialFilters
  public ActionForward returnMRDCasefileScreen(ActionMapping am, ActionForm af,
      HttpServletRequest req, HttpServletResponse res)
      throws IOException, SQLException, ParseException {
    HttpSession session = req.getSession();
    String username = (String) session.getAttribute("userid");
    req.setAttribute("userName", username);

    return am.findForward("returnscreen");
  }

  /**
   * Issue casefile.
   *
   * @param mapping
   *          the m
   * @param form
   *          the f
   * @param req
   *          the req
   * @param res
   *          the res
   * @return the action forward
   * @throws SQLException
   *           the SQL exception
   * @throws ParseException
   *           the parse exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public ActionForward issueCasefile(ActionMapping mapping, ActionForm form, HttpServletRequest req,
      HttpServletResponse res) throws SQLException, ParseException, IOException {
    ActionRedirect redirect;
    MRDCaseFileIssueDAO issuedao = new MRDCaseFileIssueDAO();
    BasicDynaBean issuebean = issuedao.getBean();
    String[] mrnos = req.getParameterValues("mrNo");
    String issueDate = req.getParameter("issuedOnDate");
    String issueTime = req.getParameter("issuedOnTime");
    String[] issueType = req.getParameterValues("issueType");
    String[] issuedTo = req.getParameterValues("issuedToId");
    FlashScope flash = FlashScope.getScope(req);
    boolean success = false;
    Connection con = null;
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      for (int i = 0; i < mrnos.length; i++) {
        if (mrnos[i] != null && !mrnos[i].equals("")) {
          issuebean.set("mr_no", mrnos[i].toString());
          issuebean.set("issued_on", DateUtil.parseTimestamp(issueDate, issueTime));
          if (issueType[i].equals("D")) {
            issuebean.set("issued_to_dept", issuedTo[i]);
          } else {
            issuebean.set("issued_to_user", Integer.parseInt(issuedTo[i]));
          }
          issuebean.set("purpose", req.getParameter("purpose"));
          issuebean.set("issue_user", req.getSession().getAttribute("userid").toString());
          int issueseq =
              DataBaseUtil.getIntValueFromDb("SELECT nextval('mrd_casefile_issue_id_seq')");
          issuebean.set("issue_id", issueseq);

          success = issuedao.insert(con, issuebean);
          if (success) {
            Map fields = new HashMap();

            fields.put("issued_id", issueseq);
            if (issueType[i].equals("D")) {
              fields.put("issued_to_dept", issuedTo[i]);
            } else {
              fields.put("issued_to_user", Integer.parseInt(issuedTo[i]));
            }
            fields.put("file_status", "U"); // Issued To User
            fields.put("indented", "N");
            fields.put("request_date", null);
            fields.put("returned_on", null);
            fields.put("issued_on", DateUtil.parseTimestamp(issueDate, issueTime));

            Map keys = new HashMap();
            keys.put("mr_no", mrnos[i].toString());
            int update = mrdCaseFileAttributesDAO.update(con, fields, keys);
            if (update > 0) {
              success = true;
            } else {
              success = false;
            }
          }
        }
      }
      if (success) {
        con.commit();
        flash.success("MRD Issue details inserted successfully..");
        redirect = new ActionRedirect(mapping.findForward("issuedcasefile"));
        redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
        return redirect;

      } else {
        con.rollback();
        flash.error("Failed to add issue details..");
        return mapping.findForward("issuescreen");
      }
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
  }

  /**
   * Return casefile.
   *
   * @param mapping
   *          the m
   * @param form
   *          the f
   * @param req
   *          the req
   * @param resp
   *          the resp
   * @return the action forward
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws ServletException
   *           the servlet exception
   * @throws SQLException
   *           the SQL exception
   * @throws ParseException
   *           the parse exception
   */
  public ActionForward returnCasefile(ActionMapping mapping, ActionForm form,
      HttpServletRequest req, HttpServletResponse resp)
      throws IOException, ServletException, SQLException, ParseException {

    FlashScope flash = FlashScope.getScope(req);

    MRDCaseFileIssueDAO issuedao = new MRDCaseFileIssueDAO();

    String[] mrnos = req.getParameterValues("mrNo");
    String[] selected = req.getParameterValues("selected");
    String returnDate = req.getParameter("return_date");
    String returnTime = req.getParameter("return_time");

    Connection con = null;
    boolean success = false;
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);

      Map fields = new HashMap();
      Map keys = new HashMap();

      Map fieldsAttr = new HashMap();
      Map keysAttr = new HashMap();

      for (int i = 0; i < mrnos.length; i++) {
        if (mrnos[i] != null && !mrnos[i].equals("") && selected[i].equals("true")) {
          fields.put("returned_on", DateUtil.parseTimestamp(returnDate, returnTime));
          fields.put("return_user", req.getSession().getAttribute("userid").toString());

          fieldsAttr.put("returned_on", DateUtil.parseTimestamp(returnDate, returnTime));
          fieldsAttr.put("remarks", req.getParameter("remarks"));
          fieldsAttr.put("issued_id", null);
          fieldsAttr.put("issued_to_dept", null);
          fieldsAttr.put("issued_to_user", null);
          fieldsAttr.put("file_status", "A"); // Available with MRD
          fieldsAttr.put("issued_on", null);

          String indented = (String) mrdCaseFileAttributesDAO
              .findByKey("mr_no", mrnos[i]).get("indented");
          if (indented.equals("N")) {
            fieldsAttr.put("requested_by", "");
            fieldsAttr.put("request_date", null);
            fieldsAttr.put("requesting_dept", null);
          }

          keysAttr.put("mr_no", mrnos[i]);

          int returnupdate =
              mrdCaseFileAttributesDAO.update(con, fieldsAttr, keysAttr);
          if (returnupdate > 0) {
            success = true;
          } else {
            success = false;
          }

          keys.put("issue_id",
              (mrdCaseFileAttributesDAO.findByKey("mr_no", mrnos[i]))
                  .get("issued_id"));

          int update = issuedao.update(con, fields, keys);
          if (update > 0) {
            success = true;
          } else {
            success = false;
          }
        }
      }
      if (success) {
        con.commit();
        flash.success("MRD return details updated successfully..");
        ActionRedirect redirect = new ActionRedirect(mapping.findForward("returncasefile"));
        redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
        return redirect;
      } else {
        con.rollback();
        flash.error("Failed to update return details..");
        return mapping.findForward("returnscreen");
      }
    } finally {
      DataBaseUtil.commitClose(con, success);
    }

  }

  /**
   * View.
   *
   * @param mapping
   *          the m
   * @param form
   *          the f
   * @param req
   *          the req
   * @param resp
   *          the resp
   * @return the action forward
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws ServletException
   *           the servlet exception
   * @throws Exception
   *           the exception
   */
  public ActionForward view(ActionMapping mapping, ActionForm form, HttpServletRequest req,
      HttpServletResponse resp) throws IOException, ServletException, Exception {

    String mrNo = req.getParameter("mr_no");

    // TODO: check this correctly... confusion...
    Map patient = PatientDetailsDAO.getPatientGeneralDetailsMap(mrNo);
    String activeVisitId = null;

    if (patient.get("visit_id") != null) {
      activeVisitId = patient.get("visit_id").toString();
    }
    if (activeVisitId != null && !activeVisitId.equals("")) {
      BasicDynaBean activePatient = VisitDetailsDAO.getPatientVisitDetailsBean(activeVisitId);
      req.setAttribute("activeVisitId", activeVisitId);
      req.setAttribute("activePatient", activePatient);
    } else if (patient.get("previous_visit_id") != null
        && !patient.get("previous_visit_id").equals("")) {
      String inActiveVisitId = patient.get("previous_visit_id").toString();
      BasicDynaBean inactivePatient = VisitDetailsDAO.getPatientVisitDetailsBean(inActiveVisitId);
      req.setAttribute("inActiveVisitId", inActiveVisitId);
      req.setAttribute("inactivePatient", inactivePatient);
    }

    BasicDynaBean mlc = patientDetailsDAO.getBean();
    patientDetailsDAO.loadByteaRecords(mlc, "mr_no", mrNo);
    String mlcStatus;
    if (mlc.get("first_mlc_visitid") != null) {
      mlcStatus = "Y";
    } else {
      mlcStatus = "N";
    }
    req.setAttribute("mlc_status", mlcStatus);

    BasicDynaBean bean = mrdCaseFileAttributesDAO.findByKey("mr_no", mrNo);
    req.setAttribute("mrdfile", bean);

    List<BasicDynaBean> issueLoglist = MRDCaseFileIssueDAO.getAllIssueDetails(mrNo);

    req.setAttribute("issueLoglist", issueLoglist);
    return mapping.findForward("casefilehistory");
  }

  /**
   * Edits the casefile.
   *
   * @param mapping
   *          the m
   * @param form
   *          the f
   * @param req
   *          the req
   * @param res
   *          the res
   * @return the action forward
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws SQLException
   *           the SQL exception
   * @throws ParseException
   *           the parse exception
   */
  public ActionForward editCasefile(ActionMapping mapping, ActionForm form, HttpServletRequest req,
      HttpServletResponse res) throws IOException, SQLException, ParseException {
    String mrNo = req.getParameter("mr_no");
    String patientStatus = req.getParameter("status");

    Map patient = PatientDetailsDAO.getPatientGeneralDetailsMap(mrNo);
    String activeVisitId = null;
    if (patient.get("visit_id") != null) {
      activeVisitId = patient.get("visit_id").toString();
    }
    if (activeVisitId != null && !activeVisitId.equals("")) {
      BasicDynaBean activePatient = VisitDetailsDAO.getPatientVisitDetailsBean(activeVisitId);
      req.setAttribute("activeVisitId", activeVisitId);
      req.setAttribute("activePatient", activePatient);
    } else if (patient.get("previous_visit_id") != null
        && !patient.get("previous_visit_id").equals("")) {
      String inActiveVisitId = patient.get("previous_visit_id").toString();
      BasicDynaBean inactivePatient = VisitDetailsDAO.getPatientVisitDetailsBean(inActiveVisitId);
      req.setAttribute("inActiveVisitId", inActiveVisitId);
      req.setAttribute("inactivePatient", inactivePatient);
    }

    BasicDynaBean mlc = patientDetailsDAO.getBean();
    patientDetailsDAO.loadByteaRecords(mlc, "mr_no", mrNo);
    String mlcStatus;
    if (mlc.get("first_mlc_visitid") != null) {
      mlcStatus = "Y";
    } else {
      mlcStatus = "N";
    }
    req.setAttribute("mlc_status", mlcStatus);

    BasicDynaBean bean = mrdCaseFileAttributesDAO.findByKey("mr_no", mrNo);
    req.setAttribute("mrdfile", bean);

    List mrdUserNames = MRDCaseFileUsersDAO.getAllUserNames();
    req.setAttribute("mrdUserNameList", new JSONSerializer().serialize(mrdUserNames));

    BasicDynaBean mrdissueBean = MRDCaseFileIssueDAO.getIssueDetails(mrNo);
    req.setAttribute("mrdissueBean", mrdissueBean);

    return mapping.findForward("editcasefile");
  }

  /**
   * Update casefile status.
   *
   * @param mapping
   *          the m
   * @param form
   *          the f
   * @param req
   *          the req
   * @param res
   *          the res
   * @return the action forward
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public ActionForward updateCasefileStatus(ActionMapping mapping, ActionForm form,
      HttpServletRequest req, HttpServletResponse res) throws SQLException, IOException {
    String mrNo = req.getParameter("mr_no");
    String caseStatus = req.getParameter("case_status");
    String fileStatus = req.getParameter("file_status");
    String recreated = req.getParameter("recreated");

    String filestatus = null;
    MRDCaseFileIssueDAO issueDAO = new MRDCaseFileIssueDAO();
    FlashScope flash = FlashScope.getScope(req);
    ActionRedirect redirect = null;

    Map editMap = new HashMap();
    Map key = new HashMap();

    int update = 0;
    boolean success = false;
    Connection con = null;

    try {
      con = DataBaseUtil.getReadOnlyConnection();
      con.setAutoCommit(false);

      if (caseStatus.equals("I")) {
        editMap.put("case_status", "I");
      } else {
        editMap.put("case_status", "A");
      }

      if (recreated != null) {
        if (recreated.equals("Y")) {
          editMap.put("recreated", true);
          editMap.put("file_status", "A");
        } else {
          editMap.put("recreated", false);
          editMap.put("file_status", "A");
        }
      }
      if (fileStatus != null) {
        editMap.put("file_status", fileStatus);
      }
      key.put("mr_no", mrNo);

      update = mrdCaseFileAttributesDAO.update(con, editMap, key);

      if (update > 0) {
        success = true;
      } else {
        success = false;
      }

      if (success) {
        con.commit();
        flash.success("Case file of " + mrNo + " is updated successfully..");
        redirect = new ActionRedirect(mapping.findForward("success"));
        redirect.addParameter("_visit_type", "o");
        redirect.addParameter("_visit_status", "A");
        redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
        return redirect;
      } else {
        con.rollback();
        flash.error("Failed to update case file ..");
        return mapping.findForward("editcasefile");
      }
    } finally {
      DataBaseUtil.commitClose(con, success);
    }
  }

  /**
   * Gets the indented casefile list.
   *
   * @param mapping
   *          the m
   * @param form
   *          the f
   * @param req
   *          the req
   * @param res
   *          the res
   * @return the indented casefile list
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws ParseException
   *           the parse exception
   * @throws ServletException
   *           the servlet exception
   * @throws FileUploadException
   *           the file upload exception
   */
  @IgnoreConfidentialFilters
  public ActionForward getIndentedCasefileList(ActionMapping mapping, ActionForm form,
      HttpServletRequest req, HttpServletResponse res)
      throws SQLException, IOException, ParseException, ServletException, FileUploadException {
    Map map = getParameterMap(req);
    List indentList =
        MRDCaseFileIssueDAO.getIndentCasefiles(map, ConversionUtils.getListingParameter(map));
    List listMap = ConversionUtils.listBeanToListMap(indentList);
    JSONSerializer js = new JSONSerializer();
    res.setContentType("application/json");
    res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    res.getWriter().write(js.serialize(listMap));

    return null;
  }

  /**
   * Gets the department list.
   *
   * @param mapping
   *          the m
   * @param form
   *          the f
   * @param req
   *          the req
   * @param res
   *          the res
   * @return the department list
   * @throws Exception
   *           the exception
   */
  @IgnoreConfidentialFilters
  public ActionForward getDepartmentList(ActionMapping mapping, ActionForm form,
      HttpServletRequest req, HttpServletResponse res) throws Exception {
    res.setContentType("application/json");
    res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");

    JSONSerializer js = new JSONSerializer().exclude("class");
    String query = req.getParameter("query");

    List deptList = MRDCaseFileIssueDAO.getIssuedtoDepUserList(query, 0);
    Map map = new HashMap();
    map.put("result", ConversionUtils.copyListDynaBeansToMap(deptList));
    res.getWriter().write(js.deepSerialize(map));
    res.flushBuffer();
    return null;
  }

}
