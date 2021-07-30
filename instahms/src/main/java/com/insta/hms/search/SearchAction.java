package com.insta.hms.search;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.ScreenRightsHelper;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * The Class SearchAction.
 *
 * @author krishna.t
 */
public class SearchAction extends DispatchAction {

  static SearchDAO dao = new SearchDAO();

  /**
   * Save search.
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
  @IgnoreConfidentialFilters
  public ActionForward saveSearch(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws IOException, SQLException {
    HttpSession session = request.getSession(false);
    String userid = (String) session.getAttribute("userid");
    Connection con = DataBaseUtil.getConnection();
    BasicDynaBean bean = dao.getBean();
    String actionId = request.getParameter("_actionId");
    String searchName = request.getParameter("_search_name");
    String screenName = ScreenRightsHelper.getMenuItemName(actionId);
    String error = null;

    String queryParams = SearchHelper.getSearchCriteria(request.getParameterMap());
    try {
      bean.set("search_id", dao.getNextSequence());
      bean.set("search_name", searchName);
      bean.set("user_name", userid);
      bean.set("action_id", actionId);
      bean.set("query_params", queryParams);
      bean.set("islast", true);
      bean.set("screen_name", screenName == null ? "" : screenName);
      if (dao.searchExsits(con, userid, actionId, searchName, dao.getNextSequence())) {
        error = "Search Name already exists";
      } else {
        if (!dao.insert(con, bean)) {
          error = "Failed to save search..";
        }
      }
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
    FlashScope flash = FlashScope.getScope(request);
    flash.put("error", error);
    String url = ScreenRightsHelper.getUrl(actionId) + queryParams;
    ActionRedirect redirect = new ActionRedirect(url);
    redirect.addParameter("_savedsearch", error == null ? searchName : null);
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
    return redirect;
  }

  /**
   * Gets the my search.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the my search
   * @throws ServletException the servlet exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws SQLException the SQL exception
   */
  @IgnoreConfidentialFilters
  public ActionForward getMySearch(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws SQLException {
    String mysearch = request.getParameter("_mysearch");
    String actionId = request.getParameter("_actionId");
    String error = null;
    String queryParams = "";
    String searchName = "";
    if (mysearch == null || mysearch.equals("")) {
      error = "Search Id cannot be empty";
      queryParams = SearchHelper.getSearchCriteria(request.getParameterMap());
    } else {
      int searchId = Integer.parseInt(mysearch);
      Connection con = DataBaseUtil.getConnection();
      try {
        BasicDynaBean bean = dao.findByKey(con, "search_id", searchId);
        queryParams = (String) bean.get("query_params");
        searchName = (String) bean.get("search_name");
      } finally {
        DataBaseUtil.closeConnections(con, null);
      }
    }
    FlashScope flash = FlashScope.getScope(request);
    flash.put("error", error);
    String url = ScreenRightsHelper.getUrl(actionId) + queryParams;
    ActionRedirect redirect = new ActionRedirect(url);
    redirect.addParameter("_savedsearch", searchName);
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
    return redirect;
  }

  /**
   * List.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the action forward
   * @throws ServletException the servlet exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws SQLException the SQL exception
   * @throws ParseException the parse exception
   */
  @IgnoreConfidentialFilters
  public ActionForward list(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response)
      throws SQLException, ParseException {
    SearchDAO dao = new SearchDAO();
    Map map = request.getParameterMap();

    PagedList pagedList = dao.getSavedSearches(map,
        ConversionUtils.getListingParameter(request.getParameterMap()));
    request.setAttribute("pagedList", pagedList);

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
  @IgnoreConfidentialFilters
  public ActionForward show(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws ServletException, IOException, SQLException {
    BasicDynaBean bean = dao.findByKey("search_id",
        Integer.parseInt(request.getParameter("search_id")));
    request.setAttribute("bean", bean);
    return mapping.findForward("addshow");
  }

  /**
   * Update.
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
  @IgnoreConfidentialFilters
  public ActionForward update(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws IOException, SQLException {
    String success = null;
    String error = null;
    List errors = new ArrayList();
    ActionRedirect redirect = null;
    Connection con = DataBaseUtil.getConnection();
    con.setAutoCommit(false);
    Map params = request.getParameterMap();
    BasicDynaBean bean = dao.getBean();
    ConversionUtils.copyToDynaBean(params, bean, errors);
    String key = request.getParameter("search_id");
    Map<String, Object> keys = new HashMap<>();
    keys.put("search_id", Integer.parseInt(key));
    try {
      if (errors.isEmpty()) {
        boolean exsist = dao.searchExsits(con, (String) bean.get("user_name"),
            (String) bean.get("action_id"), (String) bean.get("search_name"),
            (Integer) bean.get("search_id"));
        if (exsist) {
          error = "Search name already exist";
        } else {
          if (dao.update(con, bean.getMap(), keys) > 0) {
            success = "Your Data is updated successfully";
          } else {
            error = "Failed to Update the search name..";
          }
        }
      } else {
        error = "Incorrectly formatted details supplied..";
      }
    } finally {
      DataBaseUtil.commitClose(con, (success != null));
    }

    FlashScope flash = FlashScope.getScope(request);
    flash.put("success", success);
    flash.put("error", error);

    if (error != null) {
      redirect = new ActionRedirect(mapping.findForward("showRedirect"));
      redirect.addParameter("search_id", request.getParameter("search_id"));
    }
    if (success != null) {
      redirect = new ActionRedirect(mapping.findForward("showRedirect"));
      redirect.addParameter("search_id", request.getParameter("search_id"));
    }

    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

    return redirect;
  }

  /**
   * Action to batch-close a set of consultations, given all the patient IDs that need to be closed.
   *
   * @param actionMapping the action mapping
   * @param actionForm the action form
   * @param req the req
   * @param res the res
   * @return the action forward
   * @throws SQLException the SQL exception
   */
  public ActionForward delete(ActionMapping actionMapping, ActionForm actionForm,
      HttpServletRequest req, HttpServletResponse res) throws SQLException {

    String[] deleteSearches = req.getParameterValues("_deleteSearch");
    int[] searchIds = new int[deleteSearches.length];

    Connection con = DataBaseUtil.getConnection();
    con.setAutoCommit(false);
    boolean flag = true;
    try {
      for (int i = 0; i < deleteSearches.length; i++) {
        searchIds[i] = Integer.parseInt(deleteSearches[i]);
        if (!(dao.delete(con, "search_id", searchIds[i]))) {
          flag = false;
          break;
        }
      }
    } catch (SQLException se) {
      flag = false;
    } finally {
      DataBaseUtil.commitClose(con, flag);
    }

    FlashScope flash = FlashScope.getScope(req);
    ActionRedirect redirect = new ActionRedirect(
        req.getHeader("Referer").replaceAll("&" + FlashScope.FLASH_KEY + "=[0-9A-Fa-f]+", ""));
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

    if (flag) {
      flash.put("success", "Deleted successfully..");
    } else {
      flash.put("error", "Failed to delete the searches.");
    }

    return redirect;
  }

}
