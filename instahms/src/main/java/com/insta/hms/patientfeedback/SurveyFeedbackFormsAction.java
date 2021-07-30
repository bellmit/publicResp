package com.insta.hms.patientfeedback;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;

import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// TODO: Auto-generated Javadoc
/**
 * The Class SurveyFeedbackFormsAction.
 *
 * @author mithun.saha
 */
public class SurveyFeedbackFormsAction extends DispatchAction {
  
  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(SurveyFeedbackFormsAction.class);
  
  /** The dao. */
  SurveyFeedbackFormsDAO dao = new SurveyFeedbackFormsDAO();
  
  /** The sdao. */
  GenericDAO sdao = new GenericDAO("survey_form_section");
  
  /** The qdao. */
  GenericDAO qdao = new GenericDAO("survey_section_question");
  
  private static final GenericDAO surveyQuestionCategoryMasterDAO =
      new GenericDAO("survey_question_category_master");
  private static final GenericDAO surveyRatingTypeMasterDAO =
      new GenericDAO("survey_rating_type_master");

  /**
   * Gets the feedback forms list.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the feedback forms list
   * @throws Exception the exception
   */
  @IgnoreConfidentialFilters
  public ActionForward getFeedbackFormsList(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws Exception {

    Map map = request.getParameterMap();
    PagedList pagedList = dao.getFeedbackForms(map,
        ConversionUtils.getListingParameter(request.getParameterMap()));
    request.setAttribute("pagedList", pagedList);

    return mapping.findForward("list");
  }

  /**
   * Adds the.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the action forward
   * @throws Exception the exception
   */
  @IgnoreConfidentialFilters
  public ActionForward add(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws Exception {

    JSONSerializer js = new JSONSerializer().exclude("class");
    List formList = dao.getAllFeedbackFormDetails();
    request.setAttribute("feedbackFormsList", js.serialize(formList));
    return mapping.findForward("show");
  }

  /**
   * Creates the.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the action forward
   * @throws Exception the exception
   */
  public ActionForward create(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws Exception {

    Map params = request.getParameterMap();
    List errors = new ArrayList();
    Connection con = DataBaseUtil.getConnection();
    con.setAutoCommit(false);

    BasicDynaBean bean = dao.getBean();
    ConversionUtils.copyToDynaBean(params, bean, errors);
    String error = null;
    boolean success = false;
    try {
      if (errors.isEmpty()) {
        boolean exists = dao.exist("form_name", ((String) (bean.get("form_name"))).trim());
        if (exists) {
          error = "form name already exists.....";
        } else {
          bean.set("form_id", dao.getNextSequence());
          success = dao.insert(con, bean);
          if (!success) {
            error = "Failed to add the form to the master....";
          }
        }
      } else {
        error = "Incorrectly formatted values supplied..";
      }
    } finally {
      DataBaseUtil.commitClose(con, success);
    }
    ActionRedirect redirect = null;
    FlashScope flash = FlashScope.getScope(request);
    if (error != null) {
      redirect = new ActionRedirect(mapping.findForward("addRedirect"));
      flash.error(error);

    } else {
      redirect = new ActionRedirect(mapping.findForward("showRedirect"));
      redirect.addParameter("form_id", bean.get("form_id"));
    }
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

    return redirect;

  }

  /**
   * Edits the form.
   *
   * @param mapping the mapping
   * @param form the form
   * @param req the req
   * @param resp the resp
   * @return the action forward
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ServletException the servlet exception
   * @throws Exception the exception
   */
  @IgnoreConfidentialFilters
  public ActionForward editForm(ActionMapping mapping, ActionForm form, HttpServletRequest req,
      HttpServletResponse resp) throws IOException, ServletException, Exception {

    Map sectionQuestionDetailsMap = new HashMap();
    BasicDynaBean bean = dao.findByKey("form_id", Integer.parseInt(req.getParameter("form_id")));
    req.setAttribute("bean", bean);
    List<BasicDynaBean> sectionDetailsList = dao.getAllFormSectionDetails(Integer.parseInt(req
        .getParameter("form_id")));
    List<BasicDynaBean> questionDetailsList = null;
    for (int i = 0; i < sectionDetailsList.size(); i++) {
      questionDetailsList = dao.getAllQuestionDetailsBySection((Integer) sectionDetailsList.get(i)
          .get("section_id"));
      sectionQuestionDetailsMap.put(sectionDetailsList.get(i).get("section_id"),
          questionDetailsList);
    }

    JSONSerializer js = new JSONSerializer().exclude("class");
    List formList = dao.getAllFeedbackFormDetails();
    req.setAttribute("feedbackFormsList", js.serialize(formList));
    req.setAttribute("formSectionsList", sectionDetailsList);
    req.setAttribute("sectionQuestionDetailsMap", sectionQuestionDetailsMap);
    return mapping.findForward("editForm");
  }

  /**
   * Show.
   *
   * @param mapping the mapping
   * @param form the form
   * @param req the req
   * @param resp the resp
   * @return the action forward
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ServletException the servlet exception
   * @throws Exception the exception
   */
  @IgnoreConfidentialFilters
  public ActionForward show(ActionMapping mapping, ActionForm form, HttpServletRequest req,
      HttpServletResponse resp) throws IOException, ServletException, Exception {

    BasicDynaBean bean = dao.findByKey("form_id", Integer.parseInt(req.getParameter("form_id")));
    req.setAttribute("bean", bean);

    JSONSerializer js = new JSONSerializer().exclude("class");
    List formList = dao.getAllFeedbackFormDetails();
    req.setAttribute("feedbackFormsList", js.serialize(formList));
    return mapping.findForward("show");
  }

  /**
   * Update.
   *
   * @param mapping the mapping
   * @param form the form
   * @param req the req
   * @param resp the resp
   * @return the action forward
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ServletException the servlet exception
   * @throws Exception the exception
   */
  public ActionForward update(ActionMapping mapping, ActionForm form, HttpServletRequest req,
      HttpServletResponse resp) throws IOException, ServletException, Exception {

    Connection con = null;
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      Map params = req.getParameterMap();
      List errors = new ArrayList();

      BasicDynaBean bean = dao.getBean();
      ConversionUtils.copyToDynaBean(params, bean, errors);

      Integer key = Integer.parseInt(req.getParameter("form_id"));
      Map<String, Integer> keys = new HashMap<String, Integer>();
      keys.put("form_id", key);
      FlashScope flash = FlashScope.getScope(req);

      if (errors.isEmpty()) {
        int success = dao.update(con, bean.getMap(), keys);
        if (success > 0) {
          con.commit();
          flash.success("Feedback Form master details updated successfully..");
        } else {
          con.rollback();
          flash.error("Failed to update Feedback Form master details..");
        }
      } else {
        flash.error("Incorrectly formatted values supplied");
      }
      ActionRedirect redirect = new ActionRedirect(mapping.findForward("showRedirect"));
      redirect.addParameter("form_id", key.toString());
      redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
      return redirect;
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
  }

  /**
   * Adds the section.
   *
   * @param mapping the mapping
   * @param form the form
   * @param req the req
   * @param resp the resp
   * @return the action forward
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ServletException the servlet exception
   * @throws Exception the exception
   */
  @IgnoreConfidentialFilters
  public ActionForward addSection(ActionMapping mapping, ActionForm form, HttpServletRequest req,
      HttpServletResponse resp) throws IOException, ServletException, Exception {

    JSONSerializer js = new JSONSerializer().exclude("class");
    List sectionList = dao.getFeedbackSectionDetails(Integer.parseInt(req.getParameter("form_id")));
    req.setAttribute(
        "questionCategoryMaterList",
        surveyQuestionCategoryMasterDAO.listAll(
            Arrays.asList(new String[] { "category", "category_id" }), "status", "A", "category"));
    req.setAttribute("ratingTypeMasterList", surveyRatingTypeMasterDAO.listAll(
        Arrays.asList(new String[] { "rating_type", "rating_type_id" }), "status", "A",
        "rating_type"));
    req.setAttribute("feedbackFormSectionsList", js.serialize(sectionList));
    req.setAttribute("formId", req.getParameter("form_id"));
    return mapping.findForward("showSection");
  }

  /**
   * Show section.
   *
   * @param mapping the mapping
   * @param form the form
   * @param req the req
   * @param resp the resp
   * @return the action forward
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ServletException the servlet exception
   * @throws Exception the exception
   */
  @IgnoreConfidentialFilters
  public ActionForward showSection(ActionMapping mapping, ActionForm form, HttpServletRequest req,
      HttpServletResponse resp) throws IOException, ServletException, Exception {

    String formId = req.getParameter("form_id");
    String sectionId = req.getParameter("section_id");
    BasicDynaBean bean = sdao.findByKey("section_id", Integer.parseInt(sectionId));
    req.setAttribute("bean", bean);
    List<BasicDynaBean> questionDetailsList = dao.getAllSectionQuestionDetails(Integer
        .parseInt(sectionId));

    JSONSerializer js = new JSONSerializer().exclude("class");
    List sectionList = dao.getFeedbackSectionDetails(Integer.parseInt(formId));
    req.setAttribute(
        "questionCategoryMaterList",
        surveyQuestionCategoryMasterDAO.listAll(
            Arrays.asList(new String[] { "category", "category_id" }), "status", "A", "category"));
    req.setAttribute("ratingTypeMasterList", surveyRatingTypeMasterDAO.listAll(
        Arrays.asList(new String[] { "rating_type", "rating_type_id" }), "status", "A",
        "rating_type"));
    req.setAttribute("feedbackFormSectionsList", js.serialize(sectionList));
    req.setAttribute("questionDetailsList", questionDetailsList);
    req.setAttribute("formId", req.getParameter("form_id"));
    return mapping.findForward("showSection");
  }

  /**
   * Creates the section.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the action forward
   * @throws Exception the exception
   */
  public ActionForward createSection(ActionMapping mapping, 
      ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws Exception {

    Map params = request.getParameterMap();
    List errors = new ArrayList();
    Connection con = null;
    int formId = Integer.parseInt(request.getParameter("form_id"));
    int sectionOrder = Integer.parseInt(request.getParameter("section_order"));
    String sectionTitle = request.getParameter("section_title");
    sectionTitle = (sectionTitle != null || !sectionTitle.isEmpty()) ? sectionTitle.trim() : null;

    BasicDynaBean bean = sdao.getBean();
    ConversionUtils.copyToDynaBean(params, bean, errors);
    String error = null;
    boolean success = false;
    BasicDynaBean existBean = sdao.findByKey("form_id", formId);
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      if (errors.isEmpty()) {
        if (existBean != null && ((String) existBean.get("section_title")).equals(sectionTitle)) {
          error = "section name already exists.....";
        } else if (existBean != null && (Integer) existBean.get("section_order") == sectionOrder) {
          error = "duplicate value for section order.....";
        } else {
          bean.set("section_id", sdao.getNextSequence());
          sdao.insert(con, bean);
          insertQuestionDetails(con, request, (Integer) bean.get("section_id"));
          success = true;
          if (!success) {
            error = "Failed to add the section to the form....";
          }
        }
      } else {
        error = "Incorrectly formatted values supplied..";
      }
    } finally {
      DataBaseUtil.commitClose(con, success);
    }
    ActionRedirect redirect = null;
    FlashScope flash = FlashScope.getScope(request);
    if (error != null) {
      redirect = new ActionRedirect(mapping.findForward("addSectionRedirect"));
      redirect.addParameter("form_id", formId);
      flash.error(error);

    } else {
      redirect = new ActionRedirect(mapping.findForward("showSectionRedirect"));
      redirect.addParameter("section_id", bean.get("section_id"));
      redirect.addParameter("form_id", bean.get("form_id"));
    }
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

    return redirect;
  }

  /**
   * Update section.
   *
   * @param mapping the mapping
   * @param form the form
   * @param req the req
   * @param resp the resp
   * @return the action forward
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ServletException the servlet exception
   * @throws Exception the exception
   */
  public ActionForward updateSection(ActionMapping mapping, ActionForm form, HttpServletRequest req,
      HttpServletResponse resp) throws IOException, ServletException, Exception {

    Connection con = null;
    boolean success = false;
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      Map params = req.getParameterMap();
      List errors = new ArrayList();

      BasicDynaBean bean = sdao.getBean();
      ConversionUtils.copyToDynaBean(params, bean, errors);

      Integer key = Integer.parseInt(req.getParameter("section_id"));
      Map<String, Integer> keys = new HashMap<String, Integer>();
      keys.put("section_id", key);
      FlashScope flash = FlashScope.getScope(req);

      if (errors.isEmpty()) {
        sdao.update(con, bean.getMap(), keys);
        updateQuestionDetails(con, req, Integer.parseInt(req.getParameter("section_id")));
        success = true;
        flash.success("Feedback Form Section  details updated successfully..");
      } else {
        flash.error("Incorrectly formatted values supplied");
      }
      ActionRedirect redirect = new ActionRedirect(mapping.findForward("showSectionRedirect"));
      redirect.addParameter("section_id", key.toString());
      redirect.addParameter("form_id", bean.get("form_id"));
      redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
      return redirect;
    } finally {
      DataBaseUtil.commitClose(con, success);
    }
  }

  /**
   * Insert question details.
   *
   * @param con the con
   * @param req the req
   * @param sectionId the section id
   * @throws Exception the exception
   */
  private void insertQuestionDetails(Connection con, HttpServletRequest req, int sectionId)
      throws Exception {
    Map params = req.getParameterMap();
    String[] questionIds = req.getParameterValues("question_id");
    String[] questionDetails = req.getParameterValues("question_detail");
    String[] questionStatus = req.getParameterValues("q_status");
    String error = null;
    FlashScope flash = FlashScope.getScope(req);
    List errors = new ArrayList();
    try {
      for (int i = 0; i < questionIds.length; i++) {
        BasicDynaBean quesBean = qdao.getBean();
        ConversionUtils.copyIndexToDynaBean(params, i, quesBean, errors);
        if (errors.isEmpty()) {
          if (questionDetails != null && !questionDetails[i].isEmpty()) {
            quesBean.set("question_id", qdao.getNextSequence());
            quesBean.set("section_id", sectionId);
            quesBean.set("status", questionStatus[i]);
            qdao.insert(con, quesBean);
          }
        } else {
          error = "Incorrectly formatted values supplied";
          flash.error(error);
        }
      }
    } finally {
      DataBaseUtil.closeConnections(null, null);
    }
  }

  /**
   * Update question details.
   *
   * @param con the con
   * @param req the req
   * @param sectionId the section id
   * @throws Exception the exception
   */
  private void updateQuestionDetails(Connection con, HttpServletRequest req, int sectionId)
      throws Exception {
    Map params = req.getParameterMap();
    String[] questionIds = req.getParameterValues("question_id");
    String[] questionDetails = req.getParameterValues("question_detail");
    String[] questionStatus = req.getParameterValues("q_status");
    String error = null;
    FlashScope flash = FlashScope.getScope(req);
    List errors = new ArrayList();
    Map<String, Integer> keys = new HashMap<String, Integer>();
    keys.put("section_id", sectionId);

    try {
      for (int i = 0; i < questionIds.length; i++) {
        BasicDynaBean quesBean = qdao.getBean();
        ConversionUtils.copyIndexToDynaBean(params, i, quesBean, errors);
        if (errors.isEmpty()) {
          if (questionIds != null && !questionIds[i].isEmpty()) {
            keys.put("question_id", Integer.parseInt(questionIds[i]));
            if (questionDetails != null && !questionDetails[i].isEmpty()) {
              quesBean.set("status", questionStatus[i]);
              qdao.update(con, quesBean.getMap(), keys);
            }
          } else if (questionIds != null && questionIds[i].isEmpty()) {
            if (questionDetails != null && !questionDetails[i].isEmpty()) {
              quesBean.set("question_id", qdao.getNextSequence());
              quesBean.set("section_id", sectionId);
              quesBean.set("status", questionStatus[i]);
              qdao.insert(con, quesBean);
            }
          }
        } else {
          error = "Incorrectly formatted values supplied";
          flash.error(error);
        }
      }
    } finally {
      DataBaseUtil.closeConnections(null, null);
    }
  }
}
