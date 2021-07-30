package com.insta.hms.common;

import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;

import org.apache.http.HttpHeaders;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * The Class ShowScreenLogoAction. Shows the screen logo image: depending on the method that is
 * called, we return a cacheable image (for regular fetches for showing on the screen) or a
 * non-cacheable image (for showing the image from the print master)
 */
public class ShowScreenLogoAction extends Action {

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

    /*
     * todo: need to actually check if the file is modified, if it needs to come from the db instead
     * of the file system.
     */
    if (null != req.getHeader("If-Modified-Since")) {
      res.setStatus(304);
      return null;
    }

    // todo: this is a wrong assumption, but seems to work.
    res.setContentType("image/gif");

    String cache = req.getParameter("cache");
    if (cache != null && cache.equals("false")) {
      res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    } else {
      // Last-Modified is required, cache-control is good to have to enable caching
      res.setHeader("Last-modified", "Thu, 1 Jan 2009 00:00:00 GMT");
      res.setHeader(HttpHeaders.CACHE_CONTROL, "public; max-age=360000");
    }

    OutputStream os = res.getOutputStream();
    InputStream logo = GenericPreferencesDAO.getScreenLogo();
    if (logo != null) {
      sendStream(logo, os);
      logo.close();
    } else {
      /*
       * res.setContentType("image/jpeg"); res.sendRedirect(req.getContextPath() +
       * "/images/InstaLogo107x40.jpg");
       */
      HttpSession session = req.getSession(false);
      ServletContext sc = session.getServletContext();
      FileInputStream file = new FileInputStream(sc.getRealPath("/images/InstaLogo107x40.jpg"));
      sendStream(file, os);
      file.close();
    }
    return null;
  }

  /**
   * Send stream.
   *
   * @param is the is
   * @param os the os
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private void sendStream(InputStream is, OutputStream os) throws IOException {
    byte[] bytes = new byte[4096];
    int len = 0;
    while ((len = is.read(bytes)) > 0) {
      os.write(bytes, 0, len);
    }
    os.flush();
  }
}
