package com.insta.hms.tokendisplay;

import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.master.DoctorMaster.DoctorMasterDAO;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// TODO: Auto-generated Javadoc
/**
 * The Class TokensDisplayPrefsAction.
 *
 * @author krishna
 */
public class TokensDisplayPrefsAction extends DispatchAction {

  /**
   * Prefs.
   *
   * @param mapping
   *          the mapping
   * @param from
   *          the from
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the action forward
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws ServletException
   *           the servlet exception
   */

  @IgnoreConfidentialFilters
  public ActionForward prefs(ActionMapping mapping, ActionForm from, HttpServletRequest request,
      HttpServletResponse response) throws SQLException, IOException, ServletException {
    request.setAttribute("doctors", DoctorMasterDAO.getAllActiveDoctors());
    return mapping.findForward("prefs");
  }

}
