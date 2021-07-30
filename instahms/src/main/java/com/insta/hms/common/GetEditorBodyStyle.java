package com.insta.hms.common;

import com.insta.hms.common.annotations.IgnoreConfidentialFilters;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/*
 * Returns a css suitable for inclusion in the TinyMCE editor. We need this so 
 * that the "default" font can be set. Note that both the default font and size are
 * sent as request parameters, so that the results can be cached by the browser.
 * We don't go to the DB to get these.
 */

/**
 * The Class GetEditorBodyStyle.
 */
public class GetEditorBodyStyle extends Action {

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
      HttpServletResponse res) throws IOException {

    String fontFamily = req.getParameter("font");
    String fontSize = req.getParameter("fontSize");

    res.setContentType("text/css");
    Writer writer = res.getWriter();

    writer.write(
        "body, td, pre { font-family: " + fontFamily + "; font-size: " + fontSize + "pt; }\n");

    writer.flush();
    return null;
  }
}
