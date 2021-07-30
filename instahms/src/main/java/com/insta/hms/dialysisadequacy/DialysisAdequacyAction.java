package com.insta.hms.dialysisadequacy;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.insta.hms.Registration.PatientDetailsDAO;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.http.HttpHeaders;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


// TODO: Auto-generated Javadoc
/**
 * The Class DialysisAdequacyAction.
 *
 * @author mithun.saha
 */

public class DialysisAdequacyAction extends DispatchAction {

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(DialysisAdequacyAction.class);
  
  /** The json. */
  JSONSerializer json = new JSONSerializer().exclude("class");
  
  /** The dao. */
  DialysisAdequacyDAO dao = new DialysisAdequacyDAO();

  /**
   * List.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the action forward
   * @throws SQLException the SQL exception
   * @throws Exception the exception
   */
  @IgnoreConfidentialFilters
  public ActionForward list(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws SQLException, Exception {
    // PagedList pl = null;
    // Map requestParams = request.getParameterMap();
    // pl = dao.getAdequacy(requestParams,
    // ConversionUtils.getListingParameter(request.getParameterMap()));
    // request.setAttribute("pagedList", pl);
    // return mapping.findForward("list");

    String mrno = (String) request.getParameter("mr_no");
    if (mrno != null && !mrno.equals("")) {
      Map patmap = PatientDetailsDAO.getPatientGeneralDetailsMap(mrno);
      if (patmap == null) {
        FlashScope flash = FlashScope.getScope(request);
        flash.put("error", mrno + " doesn't exists.");
        ActionRedirect redirect = new ActionRedirect(mapping.findForward("showRedirect"));
        redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
        return redirect;
      }
    }
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
   * @throws SQLException the SQL exception
   * @throws Exception the exception
   */
  @IgnoreConfidentialFilters
  public ActionForward add(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws SQLException, Exception {
    return mapping.findForward("addshow");
  }

  /**
   * Show.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the action forward
   * @throws SQLException the SQL exception
   * @throws Exception the exception
   */
  @IgnoreConfidentialFilters
  public ActionForward show(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws SQLException, Exception {
    String mrno = (String) request.getParameter("mr_no");
    Map requestParams = request.getParameterMap();
    String username = (String) request.getSession(false).getAttribute("userid");
    BasicDynaBean bean = null;
    if (mrno != null && !mrno.equals("")) {
      Map patmap = PatientDetailsDAO.getPatientGeneralDetailsMap(mrno);
      if (patmap == null) {
        FlashScope flash = FlashScope.getScope(request);
        flash.put("error", mrno + " doesn't exists.");
        ActionRedirect redirect = new ActionRedirect(mapping.findForward("showRedirect"));
        redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
        return redirect;
      }
    }

    List<BasicDynaBean> adequacyInformationList = null;
    adequacyInformationList = dao.getAdequacyBean(mrno);
    if ((mrno != null && !mrno.equals(""))) {
      PagedList dateList = dao.getClinicalDialysisDates(requestParams,
          ConversionUtils.getListingParameter(requestParams), mrno);
      request.setAttribute("dateList", dateList);

      bean = dao.findByKey("mr_no", mrno);
      if (bean != null) {
        request.setAttribute("mod_time", bean.get("mod_time"));
        request.setAttribute("userName", bean.get("user_name"));
      }
    }

    return mapping.findForward("addshow");
  }

  /**
   * Save adequacy details.
   *
   * @param mapping the mapping
   * @param form the form
   * @param req the req
   * @param res the res
   * @return the action forward
   * @throws Exception the exception
   */
  public ActionForward saveAdequacyDetails(ActionMapping mapping, ActionForm form,
      HttpServletRequest req, HttpServletResponse res) throws Exception {
    Connection con = null;
    boolean success = false;
    ActionRedirect redirect = null;
    String mrno = (String) req.getParameter("mr_no");
    String[] adequacyDate = req.getParameterValues("values_as_of_date");
    String userName = (String) req.getSession(false).getAttribute("userid");
    Timestamp modTime = DataBaseUtil.getDateandTime();
    String[] deleteItem = req.getParameterValues("hdeleted");
    BasicDynaBean dialysisAdequacyBean = null;
    List errors = new ArrayList();
    BasicDynaBean bean = null;
    Map keys = new HashMap();
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);

      if (deleteItem != null) {
        for (int i = 0; i < deleteItem.length; i++) {
          if (deleteItem[i].equals("false")) {
            if (adequacyDate != null && !adequacyDate[i].equals("")) {
              bean = dao.getAdequacyBean(mrno, adequacyDate[i]);
              if (bean != null) {
                dialysisAdequacyBean = dao.getBean();
                ConversionUtils.copyIndexToDynaBean(req.getParameterMap(), i, dialysisAdequacyBean,
                    errors);
                if (errors.isEmpty()) {
                  dialysisAdequacyBean.set("mr_no", bean.get("mr_no"));
                  dialysisAdequacyBean.set("user_name", userName);
                  dialysisAdequacyBean.set("mod_time", modTime);
                  keys.put("mr_no", bean.get("mr_no"));
                  keys.put("values_as_of_date", bean.get("values_as_of_date"));
                  dao.update(con, dialysisAdequacyBean.getMap(), keys);
                }
              } else {
                dialysisAdequacyBean = dao.getBean();
                ConversionUtils.copyIndexToDynaBean(req.getParameterMap(), i, dialysisAdequacyBean,
                    errors);
                if (errors.isEmpty()) {
                  dialysisAdequacyBean.set("mr_no", mrno);
                  dialysisAdequacyBean.set("user_name", userName);
                  dialysisAdequacyBean.set("mod_time", modTime);
                  dao.insert(con, dialysisAdequacyBean);
                }
              }
            }
          } else if (deleteItem[i].equals("true") && adequacyDate != null
              && !adequacyDate[i].equals("")) {
            dao.delete(con, "mr_no", mrno, "values_as_of_date",
                new java.sql.Date(DateUtil.parseDate(adequacyDate[i]).getTime()));
          }
        }
        success = true;
      }
    } finally {
      DataBaseUtil.commitClose(con, success);
    }

    redirect = new ActionRedirect(mapping.findForward("showRedirect"));
    redirect.addParameter("mr_no", dialysisAdequacyBean.get("mr_no"));
    redirect.addParameter("values_as_of_date", dialysisAdequacyBean.get("values_as_of_date"));
    FlashScope flash = FlashScope.getScope(req);
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
    return redirect;
  }

  /**
   * Gets the dupliacte adequacy details.
   *
   * @param mapping the mapping
   * @param form the form
   * @param req the req
   * @param res the res
   * @return the dupliacte adequacy details
   * @throws Exception the exception
   */
  public ActionForward getDupliacteAdequacyDetails(ActionMapping mapping, ActionForm form,
      HttpServletRequest req, HttpServletResponse res) throws Exception {

    String adequecyDate = req.getParameter("values_as_of_date");
    String mrNo = req.getParameter("mr_no");
    String responseContent = "false";
    BasicDynaBean bean = null;

    bean = dao.getAdequacyBean(mrNo, adequecyDate);
    if (bean != null) {
      responseContent = "true";
    }
    res.setContentType("application/json");
    res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    res.getWriter().write(responseContent);
    res.flushBuffer();
    return null;

  }

}
