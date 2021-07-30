package com.insta.hms.patientfeedback;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.PagedList;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// TODO: Auto-generated Javadoc
/**
 * The Class SurveyQuestionCategoryMasterAction.
 *
 * @author mithun.saha
 */

public class SurveyQuestionCategoryMasterAction extends DispatchAction {
  
  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(SurveyQuestionCategoryMasterAction.class);
  
  /** The js. */
  JSONSerializer js = new JSONSerializer().exclude("class");
  
  /** The dao. */
  SurveyQuestionCategoryMasterDAO dao = new SurveyQuestionCategoryMasterDAO();

  /**
   * Gets the category list.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the category list
   * @throws Exception the exception
   */
  public ActionForward getCategoryList(ActionMapping mapping, 
      ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws Exception {

    Map map = request.getParameterMap();
    PagedList pagedList = dao.getAllQuestionCategories(map,
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
  public ActionForward add(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws Exception {

    List categoryList = dao.getAllQuestionCategory();
    request.setAttribute("questionCategoryList", js.serialize(categoryList));
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
        boolean exists = dao.exist("category", ((String) (bean.get("category"))).trim());
        if (exists) {
          error = "category name already exists.....";
        } else {
          bean.set("category_id", dao.getNextSequence());
          success = dao.insert(con, bean);
          if (!success) {
            error = "Failed to add category to the master....";
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
      redirect.addParameter("category_id", bean.get("category_id"));
    }
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

    return redirect;

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
  public ActionForward show(ActionMapping mapping, ActionForm form, HttpServletRequest req,
      HttpServletResponse resp) throws IOException, ServletException, Exception {

    BasicDynaBean bean = dao.findByKey("category_id",
        Integer.parseInt(req.getParameter("category_id")));
    req.setAttribute("bean", bean);

    List categoryList = dao.getAllQuestionCategory();
    req.setAttribute("questionCategoryList", js.serialize(categoryList));
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

      Integer key = Integer.parseInt(req.getParameter("category_id"));
      Map<String, Integer> keys = new HashMap<String, Integer>();
      keys.put("category_id", key);
      FlashScope flash = FlashScope.getScope(req);

      if (errors.isEmpty()) {
        int success = dao.update(con, bean.getMap(), keys);
        if (success > 0) {
          con.commit();
          flash.success("Question Category master details updated successfully..");
        } else {
          con.rollback();
          flash.error("Failed to update Question Category master details..");
        }
      } else {
        flash.error("Incorrectly formatted values supplied");
      }
      ActionRedirect redirect = new ActionRedirect(mapping.findForward("showRedirect"));
      redirect.addParameter("category_id", key.toString());
      redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
      return redirect;
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
  }
}
