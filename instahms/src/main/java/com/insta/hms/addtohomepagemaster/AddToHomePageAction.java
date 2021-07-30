package com.insta.hms.addtohomepagemaster;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.ImpersonationAction;
import com.insta.hms.common.FavouriteReportDAO;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.search.SearchDAO;

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
import java.sql.SQLException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * The Class AddToHomePageAction.
 */
public class AddToHomePageAction extends DispatchAction {

  /** The logger. */
  private static Logger logger = LoggerFactory.getLogger(AddToHomePageAction.class);

  /**
   * Save search.
   *
   * @param mapping
   *          the mapping
   * @param form
   *          the form
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the action forward
   * @throws ServletException
   *           the servlet exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws SQLException
   *           the SQL exception
   * @throws ParseException
   *           the parse exception
   */
  // To save a user saved search for homePage Header.
  @IgnoreConfidentialFilters
  public ActionForward saveSearch(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws ServletException,
      IOException, SQLException, java.text.ParseException {

    HttpSession session = request.getSession(false);
    String userName = (String) session.getAttribute("userId");
    String screenName = request.getParameter("screen_name");
    Integer searchId = Integer.parseInt(request.getParameter("search_id"));

    if (userName != null && screenName != null) {
      try {
        AddToHomePageMasterDAO.saveHomePageTab(screenName, userName, searchId);
        logger.info("Sucessfully saved the favourite tab:" + searchId + "for user " + userName);
      } catch (Exception exception) {
        logger.error("Error while saving the saved search", exception);
      }
    } else {
      logger.error("Either of userName , screenName or search_id is not defined");
    }

    ActionRedirect redirect = null;
    redirect = new ActionRedirect(mapping.findForward("success"));
    return redirect;
  }

  /**
   * Save fav reports.
   *
   * @param mapping
   *          the mapping
   * @param form
   *          the form
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the action forward
   * @throws ServletException
   *           the servlet exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws SQLException
   *           the SQL exception
   * @throws ParseException
   *           the parse exception
   */
  // To save a user Fav Reports for homePage Header.
  @IgnoreConfidentialFilters
  public ActionForward saveFavReports(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws ServletException,
      IOException, SQLException, java.text.ParseException {

    HttpSession session = request.getSession(false);
    String userName = (String) session.getAttribute("userId");
    String screenName = request.getParameter("screen_name");
    Integer reportId = Integer.parseInt(request.getParameter("report_id"));
    if (userName != null && screenName != null) {
      try {
        AddToHomePageMasterDAO.saveFavReportsHomePageTab(screenName, userName, reportId);
        logger.info("Sucessfully saved the favourite tab:" + reportId + "for user " + userName);
      } catch (Exception exception) {
        logger.error("Error while saving the fav Reports", exception);
      }
    } else {
      logger.error("Either of userName , screenName or report_id is not defined");
    }

    ActionRedirect redirect = null;
    redirect = new ActionRedirect(mapping.findForward("success"));
    return redirect;
  }

  /**
   * Save worklist report.
   *
   * @param mapping
   *          the mapping
   * @param form
   *          the form
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the action forward
   * @throws ServletException
   *           the servlet exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws SQLException
   *           the SQL exception
   * @throws ParseException
   *           the parse exception
   */
  // To save the workList and Report for homePage Header
  @IgnoreConfidentialFilters
  public ActionForward saveWorklistReport(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws ServletException,
      IOException, SQLException, java.text.ParseException {

    HttpSession session = request.getSession(false);
    String userName = (String) session.getAttribute("userId");
    String screenName = request.getParameter("screen_name");
    String actionId = request.getParameter("action_id");
    String query = request.getParameter("query");
    Connection con = DataBaseUtil.getConnection();

    if (actionId != null && query != null && screenName != null && userName != null) {
      try {
        AddToHomePageMasterDAO.saveHomePageTab(screenName, userName, actionId, query);
        logger.info("Sucessfully saved the worklist " + screenName + " for " + userName);
      } catch (Exception exception) {
        logger.error("Error while saving the Worklist", exception);
      } finally {
        DataBaseUtil.closeConnections(con, null);
      }
    } else {
      logger.error("Either of actionId , query_params, userName , screenName is empty");
    }

    ActionRedirect redirect = null;
    redirect = new ActionRedirect(mapping.findForward("success"));
    return redirect;
  }

  /**
   * Delete tab.
   *
   * @param mapping
   *          the mapping
   * @param form
   *          the form
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the action forward
   * @throws ServletException
   *           the servlet exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws SQLException
   *           the SQL exception
   * @throws ParseException
   *           the parse exception
   */
  // To delete the homePage Header.
  @IgnoreConfidentialFilters
  public ActionForward deleteTab(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws ServletException,
      IOException, SQLException, ParseException {

    HttpSession session = request.getSession(false);
    Integer homeScreenId = Integer.parseInt(request.getParameter("home_screen_id"));
    Connection con = DataBaseUtil.getConnection();

    try {
      AddToHomePageMasterDAO.deleteTab(homeScreenId);
      logger.info("Sucessfully deleted the tab home_screen_id:" + homeScreenId);
    } catch (Exception exception) {
      logger.error("Error while deleting the tab " + homeScreenId, exception);
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }

    ActionRedirect redirect = null;
    redirect = new ActionRedirect(mapping.findForward("success"));
    return redirect;
  }

  /**
   * Delete using action id.
   *
   * @param actionId
   *          the action id
   * @param screenId
   *          the screen id
   * @param homeScreenId
   *          the home screen id
   * @param urlRightsMap
   *          the url rights map
   */
  @IgnoreConfidentialFilters
  private void deleteUsingActionId(String actionId, String screenId, Integer homeScreenId,
      HashMap urlRightsMap) {
    if (null != screenId && !("passthru").equals(screenId)
        && !("A").equals(urlRightsMap.get(screenId))) {
      AddToHomePageMasterDAO.deleteTab(homeScreenId);
    } else {
      if (!("A").equals(urlRightsMap.get(actionId))) {
        AddToHomePageMasterDAO.deleteTab(homeScreenId);
      }
    }
  }

  /**
   * Delete home page shortcuts.
   *
   * @param request
   *          the request
   * @throws SQLException
   *           the SQL exception
   */
  @IgnoreConfidentialFilters
  public void deleteHomePageShortcuts(HttpServletRequest request) throws SQLException {

    HttpSession session = request.getSession();
    String userId = (String) session.getAttribute("userid");

    List<BasicDynaBean> headerList = AddToHomePageMasterDAO.getAllHomePageTabs(userId);
    List<BasicDynaBean> savedSearchList = SearchDAO.getUserSavedSearch((String) session
        .getAttribute("userid"));
    List<BasicDynaBean> favReportList = FavouriteReportDAO.getUserFavReports((String) session
        .getAttribute("userid"));
    HashMap actionScreenMap = (HashMap) session
        .getServletContext().getAttribute("actionScreenMap");
    HashMap urlRightsMap = (HashMap) session.getAttribute("urlRightsMap");

    if (headerList.size() != 0) {
      for (int i = 0; i < headerList.size(); i++) {
        if (headerList.get(i).get("search_id") != null) {
          for (int j = 0; j < savedSearchList.size(); j++) {
            if (savedSearchList.get(j).get("search_id")
                .equals(headerList.get(i).get("search_id"))) {
              String actionId = (String) savedSearchList.get(j).get("action_id");
              String screenId = (String) actionScreenMap.get(actionId);
              Integer homeScreenId = (Integer) headerList.get(i).get("home_screen_id");
              deleteUsingActionId(actionId, screenId, homeScreenId, urlRightsMap);
              break;
            }
          }
        } else if (headerList.get(i).get("report_id") != null) {
          for (int j = 0; j < favReportList.size(); j++) {
            if (favReportList.get(j).get("report_id").equals(headerList.get(i).get("report_id"))) {
              String actionId = (String) favReportList.get(j).get("action_id");
              String screenId = (String) actionScreenMap.get(actionId);
              Integer homeScreenId = (Integer) headerList.get(i).get("home_screen_id");
              deleteUsingActionId(actionId, screenId, homeScreenId, urlRightsMap);
              break;
            }
          }
        } else {
          String actionId = (String) headerList.get(i).get("action_id");
          String screenId = (String) actionScreenMap.get(actionId);
          Integer homeScreenId = (Integer) headerList.get(i).get("home_screen_id");
          deleteUsingActionId(actionId, screenId, homeScreenId, urlRightsMap);
        }
      }
    }
  }
}
