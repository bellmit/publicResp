package com.insta.hms.addtohomepagemaster;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.usermanager.PageStats;
import com.insta.hms.usermanager.RoleDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * The Class HomeFavTabRedirectAction.
 */
public class HomeFavTabRedirectAction extends Action {

  /**
   * @see org.apache.struts.action.Action#execute(org.apache.struts.action.ActionMapping,
   * org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest,
   * javax.servlet.http.HttpServletResponse)
   */
  @IgnoreConfidentialFilters
  public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws ServletException, IOException, SQLException,
      java.text.ParseException {
    Connection con = null;
    con = DataBaseUtil.getConnection();
    HttpSession session = request.getSession();
    try {
      PreparedStatement ps = null;
      RoleDAO roleDAO = new RoleDAO(con);
      Map statsList = roleDAO.getPageStatVals(((Integer) request.getSession()
          .getAttribute("roleId")) <= 1 ? Integer.toString(2) : Integer.toString((Integer) request
          .getSession().getAttribute("roleId")));
      Map pgStsUrlMap = new HashMap();
      Map pgStsDisplayNameMap = new HashMap();
      BasicDynaBean bean = new GenericDAO("generic_preferences").getRecord();// HMS-9103
      int count = 0;
      for (int i = 0; i < statsList.size(); i++) {
        count = i + 1;
        String statsItem = (String) statsList.get("stats_item" + count);
        if (statsItem != null && !statsItem.equals("")) {
          PageStats pagestats = PageStats.valueOf(statsItem);

          pgStsUrlMap.put("stats" + count, pagestats.getURLString(bean));
          pgStsDisplayNameMap.put("stats" + count, pagestats.getDisplayName());

        }
      }
      request.setAttribute("displayNameMap", pgStsDisplayNameMap);
      request.setAttribute("urlMap", pgStsUrlMap);

      request.setAttribute("homePageTabList",
          com.insta.hms.addtohomepagemaster.AddToHomePageMasterDAO
              .getAllHomePageTabs((String) session.getAttribute("userid")));

    } finally {
      DataBaseUtil.closeConnections(con, null);
    }

    return mapping.findForward("success");
  }
}
