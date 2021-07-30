package com.insta.hms.common;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.usermanager.PageStats;
import com.insta.hms.usermanager.RoleDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The Class StatisticsAction.
 */
public class StatisticsAction extends DispatchAction {

  /**
   * Gets the statistics.
   *
   * @param mapping  the mapping
   * @param form     the form
   * @param request  the request
   * @param response the response
   * @return the statistics
   * @throws ServletException the servlet exception
   * @throws IOException      Signals that an I/O exception has occurred.
   * @throws SQLException     the SQL exception
   * @throws ParseException   the parse exception
   */
  public ActionForward getStatistics(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException, SQLException, ParseException {
    Connection con = null;
    con = DataBaseUtil.getConnection();
    try {
      PreparedStatement ps = null;
      RoleDAO roleDAO = new RoleDAO(con);
      Map statsList = roleDAO.getPageStatVals(
          ((Integer) request.getSession().getAttribute("roleId")) <= 1 ? Integer.toString(2)
              : Integer.toString((Integer) request.getSession().getAttribute("roleId")));
      Map pgStsUrlMap = new HashMap();
      Map pgStsDisplayNameMap = new HashMap();
      BasicDynaBean bean = new GenericDAO("generic_preferences").getRecord(); // HMS-9103
      // int finyear = (Integer)GenericPreferencesDAO.getAllPrefs().get("fin_year_start_month");
      int count = 0;
      for (int i = 0; i < statsList.size(); i++) {
        count = i + 1;
        String stat = (String) statsList.get("stats_item" + count);
        if (stat != null && !stat.equals("")) {
          PageStats pageStats = PageStats.valueOf(stat);

          pgStsUrlMap.put("stats" + count, pageStats.getURLString(bean));
          pgStsDisplayNameMap.put("stats" + count, pageStats.getDisplayName());

        }
      }
      request.setAttribute("displayNameMap", pgStsDisplayNameMap);
      request.setAttribute("urlMap", pgStsUrlMap);
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
    return mapping.findForward("success");
  }
}
