package com.insta.hms.common;

import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * The Class ShowLogoAction. Shows the logo image.
 */
public class ShowLogoAction extends Action {

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.struts.action.Action#execute(org.apache.struts.action.ActionMapping,
   * org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest,
   * javax.servlet.http.HttpServletResponse)
   */
  @IgnoreConfidentialFilters
  @Override
  public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest req,
      HttpServletResponse res) throws IOException, SQLException {

    res.setContentType("image/gif");
    OutputStream os = res.getOutputStream();
    int centerId = 0;
    HttpSession session = req.getSession();
    if (req.getParameter("center_id") != null && !req.getParameter("center_id").equals("")) {
      centerId = Integer.parseInt(req.getParameter("center_id"));
    } else {
      centerId = (Integer) session.getAttribute("centerId");
    }
    InputStream logo = PrintConfigurationsDAO.getLogo(centerId);
    byte[] bytes = new byte[4096];
    int len = 0;
    while ((len = logo.read(bytes)) > 0) {
      os.write(bytes, 0, len);
    }

    os.flush();
    logo.close();
    return null;
  }
}
