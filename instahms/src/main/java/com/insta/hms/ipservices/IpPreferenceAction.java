package com.insta.hms.ipservices;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.master.IPPreferences.IPPreferencesDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// TODO: Auto-generated Javadoc
/**
 * The Class IpPreferenceAction.
 */
public class IpPreferenceAction extends DispatchAction {

  /** The prefs dao. */
  private static IPPreferencesDAO prefsDao = new IPPreferencesDAO();

  /**
   * Gets the ip preference screen.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the ip preference screen
   * @throws Exception the exception
   */
  @IgnoreConfidentialFilters
  public ActionForward getIpPreferenceScreen(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws Exception {
    request.setAttribute("ip_preferences", prefsDao.getPreferences().getMap());
    request.setAttribute("success", request.getParameter("success"));
    return mapping.findForward("getipscreen");
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

    ActionRedirect redirect = new ActionRedirect("/pages/ipservices/ippreference.do");
    FlashScope flash = FlashScope.getScope(request);
    redirect.addParameter("method", "getIpPreferenceScreen");
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

    BasicDynaBean bean = prefsDao.getBean();
    List errors = new ArrayList();
    ConversionUtils.copyToDynaBean(request.getParameterMap(), bean, errors);

    if (!errors.isEmpty()) {
      flash.error("Parameter conversion error");
      return redirect;
    }

    Connection con = null;
    boolean success = false;
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      prefsDao.update(con, bean.getMap(), null);
      success = true;

      // rescheduling the bed charges update job
      // BedChargeUpdateJobScheduler.scheduleBedChargeUpdateJob(); TODO :: Needs to be removed

    } finally {
      DataBaseUtil.commitClose(con, success);
      flash.success("Preferences saved successfully");
    }
    return redirect;
  }

}
