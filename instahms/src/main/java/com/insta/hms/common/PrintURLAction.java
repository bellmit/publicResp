package com.insta.hms.common;

import flexjson.JSONSerializer;

import org.apache.http.HttpHeaders;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The Class PrintURLAction. This action is intended for returning a set of print URLs set in the
 * session, as well as removing the URLs once it has been queried. The objective is that once a
 * print has been invoked, it should not be invoked again if the user navigates away from the page
 * and uses the back button to come back to the page.
 * Usage: 1. Action class: construct the URL(s) for print and set an attribute for printURLs it in
 * the session. 2. JSP: call ajaxForPrintUrls in body's onload event. This function is available in
 * common.js, which will popup the prints.
 */
public class PrintURLAction extends Action {

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.struts.action.Action#execute(org.apache.struts.action.ActionMapping,
   * org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest,
   * javax.servlet.http.HttpServletResponse)
   */
  @Override
  public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws Exception {
    List<String> printurlsList = null;
    printurlsList = (ArrayList<String>) request.getSession(false).getAttribute("printURLs");
    request.getSession(false).removeAttribute("printURLs");
    response.setContentType("text/plain");
    response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    JSONSerializer js = new JSONSerializer().exclude("class");
    response.getWriter().write(js.serialize(printurlsList));
    response.flushBuffer();
    return null;
  }
}
