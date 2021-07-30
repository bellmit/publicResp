package com.bob.hms.common;

import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.preferences.genericpreferences.GenericPreferencesService;

import freemarker.template.TemplateException;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.action.ExceptionHandler;
import org.apache.struts.config.ExceptionConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The Class HMSExceptionHandler.
 */
public class StrutsExceptionHandler extends ExceptionHandler {

  static Logger logger = LoggerFactory.getLogger(StrutsExceptionHandler.class);

  GenericPreferencesService genPrefservice = ApplicationContextProvider
      .getBean(GenericPreferencesService.class);

  @Override
  public ActionForward execute(Exception exception, ExceptionConfig exceptionConfig,
      ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws ServletException {

    // make sure we capture this in the logs, the first thing.
    logger.error("Global exception handler caught exception:", exception);

    // handling invalid file upload this way
    if (exception.getMessage() != null) {
      if (exception.getMessage().contains("Invalid header signature") || // for image files
          exception.getMessage().contains("Is it really an excel file")
          || exception.getMessage().contains("appears to be in the Office")) {
        ActionRedirect redirect = new ActionRedirect(request.getHeader("Referer"));
        FlashScope flash = FlashScope.getScope(request);
        flash.put("error", "only xls files are allowed to Upload");
        redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
        return redirect;
      }
    }

    // now, try and make more sense out of this.
    if ((exception instanceof java.io.IOException)
        || (exception instanceof java.net.SocketException)) {
      request.setAttribute("Emsg", "There was a connection problem. Please retry later.");
    } else if (exception instanceof java.io.FileNotFoundException) {
      request.setAttribute("Emsg", "Resource was not found. Please retry again.");
    } else if (exception instanceof java.lang.InstantiationException) {
      request.setAttribute("Emsg", "Service is not available.");
    } else if (exception instanceof java.lang.NullPointerException) {
      request.setAttribute("Emsg", " NullPointerException");
    } else if (exception instanceof ArrayIndexOutOfBoundsException) {
      request.setAttribute("Emsg", " ArrayIndexOutOfBoundsException");
    } else if (exception instanceof ArithmeticException) {
      request.setAttribute("Emsg", " ArithmeticException: / by zero");
    } else if (exception instanceof ClassNotFoundException) {
      request.setAttribute("Emsg", " ClassNotFoundException");
    } else if (exception instanceof TemplateException) {
      request.setAttribute("Emsg", "TemplateException: Bad Custom Template");

    } else if (exception instanceof java.sql.SQLException) {
      if (DataBaseUtil.isTimeout((java.sql.SQLException) exception)) {
        // show a page saying the operation timed out, use a different filter etc.
        return mapping.findForward("sqlTimeout");
      }

      Throwable throwable = exception.getCause();
      String cause = (throwable != null) ? throwable.toString() : exception.toString();
      String[] causeArray = cause.split(":", 2); // Splitting cause Exception Name and message
      String raisedException1 = causeArray[1];
      request.setAttribute("Emsg", "SQLException:" + raisedException1);

    } else {
      request.setAttribute("Emsg", "An unknown error has occured, please contact support");
    }

    logger.error(" ^^^ on the user's screen, the above was was reported as: "
        + request.getAttribute("Emsg") + " ^^^ ");

    request.setAttribute("Exception", exception);
    request.setAttribute("StackTrace", exception.getStackTrace());
    request.setAttribute("currentDate", new java.util.Date());
    request.setAttribute("showStackTrace",
        (Boolean) genPrefservice.getAllPreferences().get("show_stacktrace"));

    if (exception instanceof java.sql.SQLException) {
      Exception nextException = ((java.sql.SQLException) exception).getNextException();
      if (nextException != null) {
        request.setAttribute("NextStackTrace", nextException.getStackTrace());
        request.setAttribute("NextExceptionMessage", nextException.getMessage());
      }
    }
    return super.execute(exception, exceptionConfig, mapping, form, request, response);
  }
}
