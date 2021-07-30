package com.insta.hms.usermanager;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.Encoder;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;

import flexjson.JSONSerializer;
import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.http.HttpHeaders;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The Class PageStatsAction.
 *
 * @author deepasri.prasad
 */
public class PageStatsAction extends DispatchAction {

  /**
   * Gets the page stats screen.
   *
   * @param mapping the mapping
   * @param af the af
   * @param request the request
   * @param response the response
   * @return the page stats screen
   * @throws SQLException the SQL exception
   */
  @IgnoreConfidentialFilters
  public ActionForward getPageStatsScreen(ActionMapping mapping, ActionForm af,
      HttpServletRequest request, HttpServletResponse response) throws SQLException {
    Connection con = null;
    con = DataBaseUtil.getConnection();
    List<Map> jsonReportsList = new ArrayList<Map>();
    for (PageStats report : PageStats.values()) {
      jsonReportsList.add(report.getDisplayMap());
    }
    request.setAttribute("jsonReportsList", new JSONSerializer().deepSerialize(jsonReportsList));
    request.setAttribute("roleId", request.getParameter("roleId"));
    request.setAttribute("roleName", request.getParameter("roleName"));
    RoleDAO roleDAO = new RoleDAO(con);
    Map statsList = roleDAO.getPageStatVals(request.getParameter("roleId"));
    if (con != null) {
      DataBaseUtil.closeConnections(con, null);
    }
    request.setAttribute("statsList", statsList);
    return mapping.findForward("getPageStats");
  }

  /**
   * Update page stats.
   *
   * @param mapping the mapping
   * @param af the af
   * @param request the request
   * @param response the response
   * @return the action forward
   * @throws SQLException the SQL exception
   */
  @IgnoreConfidentialFilters
  public ActionForward updatePageStats(ActionMapping mapping, ActionForm af,
      HttpServletRequest request, HttpServletResponse response) throws SQLException {
    String roleName = request.getParameter("roleName").trim();
    String[] statsItem = new String[13];
    statsItem[0] = "";
    for (int i = 1; i <= 12; i++) {
      statsItem[i] = request.getParameter("stats_item" + i) == null ? ""
          : request.getParameter("stats_item" + i);
    }
    Connection con = null;
    con = DataBaseUtil.getConnection();
    RoleDAO roleDAO = new RoleDAO(con);
    String roleId = request.getParameter("roleId").trim();
    Boolean success = roleDAO.updatePageStatDetails(roleId, statsItem);
    FlashScope flash = FlashScope.getScope(request);
    if (success) {
      flash.put("success",
          "The Page Stats for the role, " + roleName + " was Successfully updated...");
    } else {
      flash.put("error", "The Page Stats for the role could not be updated...");
    }
    ActionRedirect redirect = redirect = new ActionRedirect(mapping.findForward("listRedirect"));
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
    if (con != null) {
      con.close();
    }
    return redirect;
  }

  /**
   * Gets the page stats JSON.
   *
   * @param mapping
   *          the mapping
   * @param form
   *          the form
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the page stats JSON
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws ParseException
   *           the parse exception
   */
  @IgnoreConfidentialFilters
  public ActionForward getPageStatsJSON(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws SQLException, IOException, ParseException {

    response.setContentType("text/javascript");
    response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");

    int roleId = (Integer) request.getSession().getAttribute("roleId");
    if (roleId <= 1) {
      roleId = 2;
    }
    String statNo = Encoder.cleanSQL(request.getParameter("statsNo"));
    BasicDynaBean bean = new GenericDAO("generic_preferences").getRecord();

    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      RoleDAO roleDAO = new RoleDAO(con);

      String res = roleDAO.getIndividualPageStatValue(roleId, statNo);

      Map pgStsCountMap = new HashMap();

      if (res == null || res.equals("")) {
        pgStsCountMap.put("stats" + statNo, "");

      } else {
        PageStats pageStat = PageStats.valueOf(res);

        ps = con.prepareStatement(pageStat.getDisplayCountQuery(bean));
        List list = DataBaseUtil.queryToOnlyArrayList(ps);
        pgStsCountMap.put("stats" + statNo, (list.get(0)).toString());

      }
      JSONSerializer js = new JSONSerializer().exclude("class");
      response.getWriter().write(js.serialize(pgStsCountMap));
      response.flushBuffer();
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return null;
  }

}
