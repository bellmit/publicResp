/**
 *
 */

package com.bob.hms.common;

import com.insta.hms.addtohomepagemaster.AddToHomePageAction;
import com.insta.hms.addtohomepagemaster.AddToHomePageMasterDAO;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.UrlUtil;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.search.SearchDAO;
import com.insta.hms.usermanager.PageStats;
import com.insta.hms.usermanager.RoleDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * The Class HomeAction.
 *
 * @author krishna.t
 */
public class HomeAction extends Action {

  @Override
  @IgnoreConfidentialFilters
  public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response)
      throws ServletException, IOException, SQLException, java.text.ParseException {
    Connection con = null;
    con = DataBaseUtil.getConnection();
    HttpSession session = request.getSession();
    try {
      PreparedStatement ps = null;
      RoleDAO roleDao = new RoleDAO(con);
      Map statsList = roleDao.getPageStatVals(
          ((Integer) request.getSession().getAttribute("roleId")) <= 1 ? Integer.toString(2)
              : Integer.toString((Integer) request.getSession().getAttribute("roleId")));
      Map pgStsUrlMap = new HashMap();
      Map pgStsDisplayNameMap = new HashMap();
      BasicDynaBean bean = new GenericDAO("generic_preferences").getRecord();// HMS-9103
      int count = 0;
      for (int i = 0; i < statsList.size(); i++) {
        count = i + 1;
        String st = (String) statsList.get("stats_item" + count);
        if (st != null && !st.equals("")) {
          PageStats pageStats = PageStats.valueOf(st);

          pgStsUrlMap.put("stats" + count, pageStats.getURLString(bean));
          pgStsDisplayNameMap.put("stats" + count, pageStats.getDisplayName());

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

    // deleting shortcuts which user don't have access
    AddToHomePageAction addToHomePageAction = new AddToHomePageAction();
    addToHomePageAction.deleteHomePageShortcuts(request);

    java.util.HashMap actionUrlMap = (java.util.HashMap) session.getServletContext()
        .getAttribute("actionUrlMap");
    List<BasicDynaBean> headerList = AddToHomePageMasterDAO
        .getAllHomePageTabs((String) session.getAttribute("userid"));
    List<BasicDynaBean> savedSearchList = SearchDAO
        .getUserSavedSearch((String) session.getAttribute("userid"));

    String url = null;

    if (headerList.size() != 0) {
      if (headerList.get(0).get("search_id") != null) {
        for (int i = 0; i < savedSearchList.size(); i++) {
          if (savedSearchList.get(i).get("search_id").equals(headerList.get(0).get("search_id"))) {
            url = UrlUtil.buildURL((String) savedSearchList.get(i).get("action_id"), null,
                (String) savedSearchList.get(i).get("query_params") + "&_homePagetab=1", null, null,
                false);
            break;
          }
        }

        ActionRedirect redirect = new ActionRedirect(url);
        return redirect;
      } else if (headerList.get(0).get("report_id") != null) {
        return mapping.findForward("success");
      } else {
        url = UrlUtil.buildURL((String) headerList.get(0).get("action_id"), null,
            (String) headerList.get(0).get("query_params") + "&_homePagetab=1", null, null, false);
        ActionRedirect redirect = new ActionRedirect(url);
        return redirect;
      }
    } else {
      return mapping.findForward("success");
    }
  }

}
