package com.insta.hms.patientfeedback;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
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
 * The Class SurveyRatingMasterAction.
 *
 * @author mithun.saha
 */

public class SurveyRatingMasterAction extends DispatchAction {
  
  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(SurveyRatingMasterAction.class);
  
  /** The js. */
  JSONSerializer js = new JSONSerializer().exclude("class");
  
  /** The dao. */
  SurveyRatingMasterDAO dao = new SurveyRatingMasterDAO();
  
  /** The rd dao. */
  GenericDAO rdDao = new GenericDAO("survey_rating_details_master");

  /**
   * Gets the rating list.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the rating list
   * @throws Exception the exception
   */
  public ActionForward getRatingList(ActionMapping mapping, 
      ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws Exception {

    Map map = request.getParameterMap();
    PagedList pagedList = dao.getRatings(map,
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

    List ratingList = dao.getAllRatings();
    request.setAttribute("ratingListJson", js.serialize(ratingList));
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
    String[] ratingId = request.getParameterValues("rating_id");
    String[] ratingText = request.getParameterValues("rating_text");
    Connection con = DataBaseUtil.getConnection();
    con.setAutoCommit(false);

    BasicDynaBean ratingBean = dao.getBean();
    BasicDynaBean ratingDetBean = rdDao.getBean();

    ConversionUtils.copyToDynaBean(params, ratingBean, errors);
    String error = null;
    boolean success = false;
    try {
      if (errors.isEmpty()) {
        boolean exists = dao
            .exist("rating_type", ((String) (ratingBean.get("rating_type"))).trim());
        if (exists) {
          error = "rating name already exists.....";
        } else {
          int ratingTypeId = dao.getNextSequence();
          ratingBean.set("rating_type_id", ratingTypeId);
          success = dao.insert(con, ratingBean);

          if (success) {
            for (int i = 0; i < ratingId.length; i++) {
              ConversionUtils.copyIndexToDynaBean(params, i, ratingDetBean, errors);
              if (errors.isEmpty()) {
                if (ratingText != null && !ratingText[i].isEmpty()) {
                  ratingDetBean.set("rating_type_id", ratingTypeId);
                  ratingDetBean.set("rating_id", rdDao.getNextSequence());
                  rdDao.insert(con, ratingDetBean);
                }
              } else {
                success = false;
              }
            }
          }
          if (!success) {
            error = "Failed to add rating details to the master....";
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
      redirect.addParameter("rating_type_id", ratingBean.get("rating_type_id"));
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

    Map filterMap = new HashMap();
    filterMap.put("rating_type_id", Integer.parseInt(req.getParameter("rating_type_id")));
    BasicDynaBean bean = dao.findByKey("rating_type_id",
        Integer.parseInt(req.getParameter("rating_type_id")));
    List<BasicDynaBean> ratingDetailsList = rdDao.listAll(null, filterMap, "rating_value");
    List ratingList = dao.getAllRatings();
    req.setAttribute("bean", bean);
    req.setAttribute("ratingDetailsList", ratingDetailsList);
    req.setAttribute("ratingListJson", js.serialize(ratingList));
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

    String[] ratingId = req.getParameterValues("rating_id");
    String[] ratingText = req.getParameterValues("rating_text");
    String ratingTypeId = req.getParameter("rating_type_id");
    String[] deleted = req.getParameterValues("r_deleted");
    boolean success = false;

    Connection con = null;
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      Map params = req.getParameterMap();
      List errors = new ArrayList();

      BasicDynaBean bean = dao.getBean();
      BasicDynaBean ratingDetBean = rdDao.getBean();
      ConversionUtils.copyToDynaBean(params, bean, errors);

      Integer key = Integer.parseInt(req.getParameter("rating_type_id"));
      Map<String, Integer> keys = new HashMap<String, Integer>();
      keys.put("rating_type_id", key);
      FlashScope flash = FlashScope.getScope(req);

      if (errors.isEmpty()) {
        success = dao.update(con, bean.getMap(), keys) > 0;
        for (int i = 0; i < ratingId.length; i++) {
          ConversionUtils.copyIndexToDynaBean(params, i, ratingDetBean, errors);
          if (errors.isEmpty()) {
            if (deleted != null && deleted[i].equals("N")) {
              if (ratingId != null && !ratingId[i].isEmpty()) {
                ratingDetBean.set("rating_type_id", Integer.parseInt(ratingTypeId));
                updateRatingDetails(con, Integer.parseInt(ratingId[i]), ratingDetBean.getMap(),
                    Integer.parseInt(ratingTypeId));
              } else {
                if (ratingText != null && !ratingText[i].isEmpty()) {
                  ratingDetBean.set("rating_id", rdDao.getNextSequence());
                  ratingDetBean.set("rating_type_id", Integer.parseInt(ratingTypeId));
                  rdDao.insert(con, ratingDetBean);
                }
              }
            } else if (deleted[i].equals("Y")) {
              rdDao.delete(con, "rating_id", (Integer) ratingDetBean.get("rating_id"));
            }
            success = true;
          } else {
            success = false;
          }
        }
        if (success) {
          con.commit();
          flash.success("survey rating master details updated successfully..");
        } else {
          con.rollback();
          flash.error("Failed to update survey rating master details..");
        }
      } else {
        flash.error("Incorrectly formatted values supplied");
      }
      ActionRedirect redirect = new ActionRedirect(mapping.findForward("showRedirect"));
      redirect.addParameter("rating_type_id", key.toString());
      redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
      return redirect;
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
  }

  /**
   * Update rating details.
   *
   * @param con the con
   * @param ratingId the rating id
   * @param ratingDetMap the rating det map
   * @param ratingTypeId the rating type id
   * @return the int
   * @throws Exception the exception
   */
  private int updateRatingDetails(Connection con, int ratingId, Map ratingDetMap, int ratingTypeId)
      throws Exception {
    Map<String, Integer> keys = new HashMap<String, Integer>();
    keys.put("rating_id", ratingId);
    keys.put("rating_type_id", ratingTypeId);
    return rdDao.update(con, ratingDetMap, keys);
  }
}
