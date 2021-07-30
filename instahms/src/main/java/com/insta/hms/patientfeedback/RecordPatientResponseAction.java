package com.insta.hms.patientfeedback;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// TODO: Auto-generated Javadoc
/**
 * The Class RecordPatientResponseAction.
 *
 * @author mithun.saha
 */
public class RecordPatientResponseAction extends DispatchAction {
  
  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(RecordPatientResponseAction.class);
  
  /** The dao. */
  RecordPatientResponseDAO dao = new RecordPatientResponseDAO();
  
  /** The fdao. */
  GenericDAO fdao = new GenericDAO("survey_form");
  
  /** The svfdao. */
  GenericDAO svfdao = new GenericDAO("survey_visit_feedback");
  
  /** The svfddao. */
  GenericDAO svfddao = new GenericDAO("survey_visit_feedback_details");
  
  /** The regdao. */
  GenericDAO regdao = new GenericDAO("patient_registration");

  /**
   * Gets the all active survey forms.
   *
   * @param map the map
   * @param form the form
   * @param req the req
   * @param res the res
   * @return the all active survey forms
   * @throws ServletException the servlet exception
   * @throws Exception the exception
   */
  @IgnoreConfidentialFilters
  public ActionForward getAllActiveSurveyForms(ActionMapping map, ActionForm form,
      HttpServletRequest req, HttpServletResponse res) throws ServletException, Exception {
    String patientId = req.getParameter("visit_id");
    String formId = req.getParameter("form_id");
    ActionRedirect redirect = null;
    List<BasicDynaBean> allActiveSurveyFormDetails = null;
    allActiveSurveyFormDetails = fdao.findAllByKey("form_status", "A");
    // String surveyResponseId = req.getParameter("survey_response_id");

    FlashScope flash = FlashScope.getScope(req);
    if (patientId != null && !patientId.equals("")) {
      VisitDetailsDAO regDao = new VisitDetailsDAO();
      BasicDynaBean bean = regDao.findByKey("patient_id", patientId);
      if (bean == null) {
        // user entered visit id is not a valid patient id.
        flash.put("error", "No Patient with Id:" + patientId);
        redirect = new ActionRedirect(map.findForward("listRedirect"));
        redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
        return redirect;
      } else {
        if (formId != null && !formId.isEmpty()) {
          redirect = new ActionRedirect(map.findForward("recordSurveyFormRedirect"));
          redirect.addParameter("form_id", formId);
          redirect.addParameter("visit_id", patientId);
          redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
        } else {
          if (allActiveSurveyFormDetails != null && allActiveSurveyFormDetails.size() > 0) {
            int surFormId = (Integer) allActiveSurveyFormDetails.get(0).get("form_id");
            if (allActiveSurveyFormDetails.size() == 1) {
              redirect = new ActionRedirect(map.findForward("recordSurveyFormRedirect"));
              redirect.addParameter("form_id", surFormId);
              redirect.addParameter("visit_id", patientId);
              redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
              return redirect;
            } else {
              req.setAttribute("allActiveSurveyFormDetails", allActiveSurveyFormDetails);
              return map.findForward("showActiveForms");
            }
          }
        }
      }
    }
    req.setAttribute("allActiveSurveyFormDetails", allActiveSurveyFormDetails);
    return map.findForward("showActiveForms");
  }

  /**
   * Record survey form.
   *
   * @param map the map
   * @param form the form
   * @param req the req
   * @param res the res
   * @return the action forward
   * @throws ServletException the servlet exception
   * @throws Exception the exception
   */
  @IgnoreConfidentialFilters
  public ActionForward recordSurveyForm(ActionMapping map, ActionForm form, HttpServletRequest req,
      HttpServletResponse res) throws ServletException, Exception {

    // String surveyResponseId = req.getParameter("survey_response_id");
    Map<Object, List<BasicDynaBean>> ratingDetailsMap = new HashMap<Object, List<BasicDynaBean>>();
    List<BasicDynaBean> allActiveSurveyFormDetails = null;
    List<BasicDynaBean> ratingList = new SurveyRatingMasterDAO().listAll();
    BasicDynaBean formDetails = null;
    List ratingDetailsList = null;
    String formId = req.getParameter("form_id");

    for (int i = 0; i < ratingList.size(); i++) {
      int ratingTypeId = (Integer) ratingList.get(i).get("rating_type_id");
      ratingDetailsList = SurveyRatingMasterDAO.getAllRatingDetails(ratingTypeId);
      ratingDetailsMap.put(ratingTypeId, ratingDetailsList);
    }

    formDetails = fdao.findByKey("form_id", Integer.parseInt(formId));
    allActiveSurveyFormDetails = RecordPatientResponseDAO.getAllActiveSurveyQuestionDetails(Integer
        .parseInt(formId));

    req.setAttribute("surveyFormDetails", allActiveSurveyFormDetails);
    req.setAttribute("surveyFormDetailsLength",
        allActiveSurveyFormDetails != null ? allActiveSurveyFormDetails.size() : 0);
    req.setAttribute("allActiveSurveyFormDetails", allActiveSurveyFormDetails);
    List<BasicDynaBean> allSurveyActiveSectionDetails = null;
    req.setAttribute("allSurveyActiveSectionDetails", allSurveyActiveSectionDetails);
    req.setAttribute("formDetails", formDetails);
    BasicDynaBean surveyDetailsBean = null;
    req.setAttribute("surveyDetailsBean", surveyDetailsBean);
    req.setAttribute("ratingDetailsMap", ratingDetailsMap);
    return map.findForward("recordSurveyForm");
  }

  /**
   * Cancel patient survey response.
   *
   * @param map the map
   * @param form the form
   * @param req the req
   * @param res the res
   * @return the action forward
   * @throws ServletException the servlet exception
   * @throws Exception the exception
   */
  @IgnoreConfidentialFilters
  public ActionForward cancelPatientSurveyResponse(ActionMapping map, ActionForm form,
      HttpServletRequest req, HttpServletResponse res) throws ServletException, Exception {

    ActionRedirect redirect = new ActionRedirect(map.findForward("responsesListRedirect"));
    return redirect;
  }

  /**
   * Record patient survey response.
   *
   * @param map the map
   * @param form the form
   * @param req the req
   * @param res the res
   * @return the action forward
   * @throws ServletException the servlet exception
   * @throws Exception the exception
   */
  public ActionForward recordPatientSurveyResponse(ActionMapping map, ActionForm form,
      HttpServletRequest req, HttpServletResponse res) throws ServletException, Exception {

    Connection con = null;
    String patientId = req.getParameter("visit_id");
    BasicDynaBean patBean = regdao.findByKey("patient_id", patientId);
    String[] questionId = req.getParameterValues("question_id");
    String[] responseValue = req.getParameterValues("db_response_value");
    String[] responseType = req.getParameterValues("response_type");
    String[] responseText = req.getParameterValues("response_text");
    BasicDynaBean surveyVisitFeedbackBean = null;
    BasicDynaBean surveyVisitFeedbackDetailsBean = null;
    String mrNo = null;
    boolean success = false;
    mrNo = (String) patBean.get("mr_no");
    Timestamp surveyDateTime = DateUtil.getCurrentTimestamp();
    String formId = req.getParameter("formId");
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      surveyVisitFeedbackBean = svfdao.getBean();
      surveyVisitFeedbackBean.set("survey_response_id", svfdao.getNextSequence());
      surveyVisitFeedbackBean.set("mr_no", mrNo);
      surveyVisitFeedbackBean.set("visit_id", patientId);
      surveyVisitFeedbackBean.set("form_id", Integer.parseInt(formId));
      surveyVisitFeedbackBean.set("survey_date", surveyDateTime);
      success = svfdao.insert(con, surveyVisitFeedbackBean);

      if (success) {
        for (int i = 0; i < questionId.length; i++) {
          surveyVisitFeedbackDetailsBean = svfddao.getBean();
          surveyVisitFeedbackDetailsBean.set("survey_response_id",
              surveyVisitFeedbackBean.get("survey_response_id"));
          surveyVisitFeedbackDetailsBean.set("question_id", Integer.parseInt(questionId[i]));
          surveyVisitFeedbackDetailsBean.set("response_type", responseType[i]);
          int responseDetailId = 0;
          if (responseType[i].equals("Y")) {
            if (responseValue[i] != null && !responseValue[i].isEmpty()) {
              responseDetailId = svfddao.getNextSequence();
              surveyVisitFeedbackDetailsBean.set("response_detail_id", responseDetailId);
              surveyVisitFeedbackDetailsBean.set("response_value", responseValue[i].equals("Y") ? 1
                  : 0);
              surveyVisitFeedbackDetailsBean.set("response_text", responseText[i]);
            } else if (responseText[i] != null && !responseText[i].isEmpty()) {
              responseDetailId = svfddao.getNextSequence();
              surveyVisitFeedbackDetailsBean.set("response_detail_id", responseDetailId);
              surveyVisitFeedbackDetailsBean.set("response_value", -1);
              surveyVisitFeedbackDetailsBean.set("response_text", responseText[i]);
            } else {
              surveyVisitFeedbackDetailsBean = null;
            }
          } else if (responseType[i].equals("R")) {
            if (responseValue[i] != null && !responseValue[i].isEmpty()) {
              responseDetailId = svfddao.getNextSequence();
              surveyVisitFeedbackDetailsBean.set("response_detail_id", responseDetailId);
              surveyVisitFeedbackDetailsBean.set("response_value", null);
              surveyVisitFeedbackDetailsBean.set("rating_id", Integer.parseInt(responseValue[i]));
              surveyVisitFeedbackDetailsBean.set("response_text", responseText[i]);
            } else if (responseText[i] != null && !responseText[i].isEmpty()) {
              responseDetailId = svfddao.getNextSequence();
              surveyVisitFeedbackDetailsBean.set("response_detail_id", responseDetailId);
              surveyVisitFeedbackDetailsBean.set("response_value", null);
              surveyVisitFeedbackDetailsBean.set("rating_id", null);
              surveyVisitFeedbackDetailsBean.set("response_text", responseText[i]);
            } else {
              surveyVisitFeedbackDetailsBean = null;
            }
          } else if (responseType[i].equals("T")) {
            if (responseText[i] != null && !responseText[i].isEmpty()) {
              responseDetailId = svfddao.getNextSequence();
              surveyVisitFeedbackDetailsBean.set("response_detail_id", responseDetailId);
              surveyVisitFeedbackDetailsBean.set("response_value", null);
              surveyVisitFeedbackDetailsBean.set("rating_id", null);
              surveyVisitFeedbackDetailsBean.set("response_text", responseText[i]);
            } else {
              surveyVisitFeedbackDetailsBean = null;
            }
          }

          if (surveyVisitFeedbackDetailsBean != null) {
            success = svfddao.insert(con, surveyVisitFeedbackDetailsBean);
          }
        }
      }
    } finally {
      DataBaseUtil.commitClose(con, success);
    }
    ActionRedirect redirect = new ActionRedirect(map.findForward("responsesListRedirect"));
    return redirect;
  }
}
