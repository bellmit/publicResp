package com.insta.hms.common;

import org.apache.commons.chain.Context;
import org.apache.commons.chain.Filter;
import org.apache.struts.chain.commands.ActionCommandBase;
import org.apache.struts.chain.contexts.ActionContext;
import org.apache.struts.chain.contexts.ServletActionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * The Class RequestLog.
 */
public class RequestLog extends ActionCommandBase implements Filter {

  /** The log. */
  static Logger log = LoggerFactory.getLogger(RequestLog.class);

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.commons.chain.Filter#postprocess(org.apache.commons.chain.Context,
   * java.lang.Exception)
   */
  @Override
  public boolean postprocess(Context arg0, Exception arg1) {
    return false;
  }

  /**
   * Execute.
   *
   * @param ctx the ctx
   * @return true, if successful
   * @throws Exception the exception
   */
  public boolean execute(ServletActionContext ctx) throws Exception {
    HttpServletRequest req = ctx.getRequest();
    Map<String, String[]> par = req.getParameterMap();
    String requestParamsInfo = null;
    for (Map.Entry<String, String[]> entry : par.entrySet()) {
      for (String value : entry.getValue()) {
        requestParamsInfo = requestParamsInfo + entry.getKey() + " : " + value + "\n";
      }
    }
    // log.info(requestParamsInfo);
    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.struts.chain.commands.ActionCommandBase#execute(
   * org.apache.struts.chain.contexts.ActionContext)
   */
  public boolean execute(ActionContext ctx) throws Exception {
    return execute((ServletActionContext) ctx);
  }

}