package com.insta.hms.common;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ReportErrorsAction extends Action {

  static Logger log = LoggerFactory.getLogger(ReportErrorsAction.class);

  public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws ServletException, IOException, Exception {
    log.info("error is >>>" + request.getParameter("error"));
    return mapping.findForward("reportErrorPage");
  }
}
