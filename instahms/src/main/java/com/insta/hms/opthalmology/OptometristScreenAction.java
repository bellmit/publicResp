package com.insta.hms.opthalmology;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The Class OptometristScreenAction.
 */
public class OptometristScreenAction extends BaseAction {
  
  private static final GenericDAO opthalTestMainDAO = new GenericDAO("opthal_test_main");
  private static final GenericDAO patientHistoryDAO = new GenericDAO("patient_history");
  private static final GenericDAO patientEyeHistoryDAO = new GenericDAO("patient_eye_history");

  /**
   * List.
   *
   * @param mapping the m
   * @param form the f
   * @param req the req
   * @param res the res
   * @return the action forward
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ServletException the servlet exception
   * @throws Exception the exception
   */
  @SuppressWarnings("unchecked")
  public ActionForward list(ActionMapping mapping, ActionForm form, HttpServletRequest req,
      HttpServletResponse res) throws IOException, ServletException, Exception {

    Map map = new HashMap();
    map.put("doctor_id", req.getParameter("doctor_id"));
    map.put("status", req.getParameter("status"));
    map.put("mr_no", req.getParameter("mr_no"));

    ArrayList<String> doctorList = OptometristScreenDAO.getOpthaDoctors();
    PagedList list = OptometristScreenDAO.pendingPatientsList(map,
        ConversionUtils.getListingParameter(req.getParameterMap()));
    req.setAttribute("doctorList", doctorList);
    req.setAttribute("pagedList", list);

    return mapping.findForward("list");
  }

  /**
   * Show.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the action forward
   * @throws ServletException the servlet exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws SQLException the SQL exception
   */
  public ActionForward show(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws ServletException, IOException, SQLException {

    String patientId = request.getParameter("patient_id");
    String doctorId = (String) request.getParameter("doctor_id");
    String status = request.getParameter("status");
    String mrNo = (String) new VisitDetailsDAO().findByKey("patient_id", patientId).get("mr_no");

    ActionRedirect redirect = null;

    if ((null != doctorId) && (!(doctorId.equals("")))) {
      String doctorName = OptometristScreenDAO.getDoctorFromDoctorId(doctorId);
      request.setAttribute("doctor_name", doctorName);
    }

    BasicDynaBean complaintbean = VisitDetailsDAO.getComplaint(patientId);
    String complaintName = complaintbean == null ? "" : (String) complaintbean.get("complaint");
    request.setAttribute("complaint", complaintName);

    if (status != null && !status.equals("")) {
      if (status.equals("D")) {
        redirect = new ActionRedirect(mapping.findForward("doctorPageRedirect"));
        redirect.addParameter("_method", "showDoctorEyeExamScreen");
        redirect.addParameter("patient_id", patientId);
        redirect.addParameter("mr_no", mrNo);
        redirect.addParameter("doctor_id", doctorId);

        return redirect;
      }
    }

    String valueList = OptometristScreenDAO.valuesToPick();
    request.setAttribute("mr_no", mrNo);
    request.setAttribute("valuelistpicker", valueList);
    request.setAttribute("eyeTestList", OptometristScreenDAO.getEyeTestList());
    request.setAttribute("parametersList", OptometristScreenDAO.parametersForEyeTest());
    request.setAttribute("testValues", OptometristScreenDAO.getTestValues(patientId));
    request.setAttribute("opthal_id", OptometristScreenDAO.getOpthalId(patientId));
    request.setAttribute("opthalTestmain",
        opthalTestMainDAO.findByKey("patient_id", patientId));
    request.setAttribute("patientHistory",
        patientHistoryDAO.findByKey("patient_id", patientId));
    request.setAttribute("patientEyeHistory",
        patientEyeHistoryDAO.findByKey("patient_id", patientId));

    return mapping.findForward("show");
  }

  /**
   * Save test values.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the action forward
   * @throws ServletException the servlet exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws Exception the exception
   */
  public ActionForward saveTestValues(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException, Exception {

    String[] testValues = request.getParameterValues("attribValues");
    String[] testIds = request.getParameterValues("testIds");
    String[] attributeIds = request.getParameterValues("attributeIds");
    String[] ids = request.getParameterValues("key");

    int opthalId = Integer.parseInt(request.getParameter("opthalId"));

    ActionRedirect redirect = new ActionRedirect(mapping.findForward("showRedirect"));
    FlashScope flash = FlashScope.getScope(request);
    boolean allSuccess = false;
    boolean success = false;

    GenericDAO opthalTestDetais = new GenericDAO("opthal_test_details");
    Connection con = null;

    try {
      con = DataBaseUtil.getReadOnlyConnection();
      con.setAutoCommit(false);

      success = savePatientHistoryAndEyeHistory(request, con);
      if (!success) {
        flash.put("error", "Transaction Failure While Saving Patient History");
        return redirect;
      }

      BasicDynaBean otMainBean = opthalTestMainDAO.getBean();

      if (opthalId == 0) {
        opthalId = opthalTestMainDAO.getNextSequence();
        otMainBean.set("opthal_id", opthalId);
        otMainBean.set("patient_id", request.getParameter("patient_id"));
        otMainBean.set("consult_id", OptometristScreenDAO
            .getConsultationId(request.getParameter("doctor_id"), request.getParameter("mr_no")));
        success = opthalTestMainDAO.insert(con, otMainBean);
        if (!success) {
          flash.put("error", "Transaction Failure");
          return redirect;
        }
      }
      otMainBean.set("test_notes", request.getParameter("notes"));
      otMainBean.set("status", request.getParameter("status"));
      // otMainBean.set("completion_status", "");
      success = opthalTestMainDAO.update(con, otMainBean.getMap(), "opthal_id", opthalId) > 0;
      if (!success) {
        flash.put("error", "Transaction Failure");
        return redirect;
      }

      for (int i = 0; i < testIds.length; i++) {

        if (ids[i].startsWith("_")) {
          BasicDynaBean bean = opthalTestDetais.getBean();
          bean.set("value_id", opthalTestDetais.getNextSequence());
          bean.set("test_id", Integer.parseInt(testIds[i]));
          bean.set("attribute_id", Integer.parseInt(attributeIds[i]));
          bean.set("test_values", testValues[i]);
          bean.set("opthal_id", opthalId);
          success = opthalTestDetais.insert(con, bean);
          if (!success) {
            flash.put("error", "Transaction Failure");
            return redirect;
          }
        } else {
          BasicDynaBean bean = opthalTestDetais.getBean();
          bean.set("test_id", Integer.parseInt(testIds[i]));
          bean.set("attribute_id", Integer.parseInt(attributeIds[i]));
          bean.set("test_values", testValues[i]);
          bean.set("opthal_id", opthalId);
          success = opthalTestDetais.update(con, bean.getMap(), "value_id",
              Integer.parseInt(ids[i])) > 0;
          if (!success) {
            flash.put("error", "Transaction Failure");
            return redirect;
          }
        }
      }
      allSuccess = true;
      redirect.addParameter("success", "Transaction Successfully Done");

    } finally {
      DataBaseUtil.commitClose(con, allSuccess);
      redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
      redirect.addParameter("mr_no", request.getParameter("mr_no"));
      redirect.addParameter("patient_id", request.getParameter("patient_id"));
      redirect.addParameter("doctor_id", request.getParameter("doctor_id"));
      redirect.addParameter("complaint", request.getParameter("complaint"));
    }
    return redirect;
  }

  /**
   * Save patient history and eye history.
   *
   * @param request the request
   * @param con the con
   * @return true, if successful
   * @throws SQLException the SQL exception
   * @throws Exception the exception
   */
  private boolean savePatientHistoryAndEyeHistory(HttpServletRequest request, Connection con)
      throws SQLException, Exception {

    String patientId = request.getParameter("patient_id");
    String pastOcularHistory = request.getParameter("pastOcularHistory");
    String familyHistory = request.getParameter("familyHistory");
    String medicalHistory = request.getParameter("medicalHistory");

    BasicDynaBean patientHistoryBean = patientHistoryDAO.findByKey("patient_id", patientId);
    BasicDynaBean patientEyeHistoryBean = patientEyeHistoryDAO.findByKey("patient_id", patientId);
    BasicDynaBean bean = null;
    boolean success = true;

    if (null == patientHistoryBean) {
      if ((familyHistory != null && !familyHistory.equals(""))
          || (medicalHistory != null && !medicalHistory.equals(""))) {
        bean = patientHistoryDAO.getBean();
        bean.set("patient_id", patientId);
        bean.set("patient_history", "");
        bean.set("family_history", familyHistory);
        bean.set("past_surgery", medicalHistory);
        success = patientHistoryDAO.insert(con, bean);
        if (!success) {
          return success;
        }
      }

    } else {
      bean = patientHistoryDAO.getBean();
      bean.set("patient_id", patientId);
      bean.set("patient_history", "");
      bean.set("family_history", familyHistory);
      bean.set("past_surgery", medicalHistory);
      success = patientHistoryDAO.update(con, bean.getMap(), "patient_id", patientId) > 0;
      if (!success) {
        return success;
      }
    }

    if (null == patientEyeHistoryBean) {
      if ((pastOcularHistory != null && !pastOcularHistory.equals(""))) {
        bean = patientEyeHistoryDAO.getBean();
        bean.set("patient_id", patientId);
        bean.set("patient_eye_history", pastOcularHistory);
        bean.set("past_eye_surgery", "");
        success = patientEyeHistoryDAO.insert(con, bean);
        if (!success) {
          return success;
        }
      }
    } else {
      bean = patientEyeHistoryDAO.getBean();
      bean.set("patient_id", patientId);
      bean.set("patient_eye_history", pastOcularHistory);
      bean.set("past_eye_surgery", "");
      success = patientEyeHistoryDAO.update(con, bean.getMap(), "patient_id", patientId) > 0;
      if (!success) {
        return success;
      }
    }

    return success;
  }
}