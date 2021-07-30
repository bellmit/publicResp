package com.insta.hms.common;

import org.apache.commons.chain.Context;
import org.apache.commons.chain.Filter;
import org.apache.struts.chain.contexts.ServletActionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;

/**
 * The Class CreateToken.
 */
public class CreateToken extends TokenCommandBase implements Filter {

  static Logger log = LoggerFactory.getLogger(CreateToken.class);

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.common.TokenCommandBase#execute(
   * org.apache.struts.chain.contexts.ServletActionContext)
   */
  @Override
  public boolean execute(ServletActionContext ctx) throws Exception {
    HttpServletRequest req = ctx.getRequest();
    // If it is a get request and not an ajax call save a token
    // Note: Most browsers send the X-Requested-With header for an ajax call, but this is not a
    // standard and therefore cannot be relied upon. However this header is supported by FireFox
    // which
    // the bowser of choice for the application. We assume that if the header does not
    // exist or if its value is not as expected, it is a normal call and we generate the token
    // we are failing on the safer side.

    Boolean createToken = new Boolean(req.getParameter("createToken"));
    if (req.getMethod().equals("GET") && ((null == req.getHeader("X-Requested-With")) || createToken
        || (!req.getHeader("X-Requested-With").equalsIgnoreCase("XMLHttpRequest")))) {
      String tokenValue = createToken(ctx);
      req.setAttribute(TRANSACTION_TOKEN_KEY, tokenValue);
      log.debug("path : " + ctx.getActionConfig().getPath() + " token : " + tokenValue);

      // This call will block if the maximum no of latches are already consumed.
      // Will return only when one of the requests that holds the latch completes and releases
      // the latch.
      // Changes for 57686 => call to acquireRequestLatch moved to ThrottleRequest
      // acquireRequestLatch(ctx);

    }
    // continue the command chain.
    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.commons.chain.Filter#postprocess(org.apache.commons.chain.Context,
   * java.lang.Exception)
   */
  @Override
  public boolean postprocess(Context ctx, Exception exception) {

    // This releases the latch, as the request is processed and we are on the way back.
    // This handles the case where the command chain reaches the end or was broken due to an
    // exception.
    // The latch will be released if and only if this command was invoked during the
    // forward execution of the chain.

    /*
     * Changes for 57686 => acquire and release RequestLatch now in ThrottleRequest
     */
    /*
     * ServletActionContext sc = (ServletActionContext)ctx; HttpServletRequest req =
     * sc.getRequest(); Boolean createToken = new Boolean(req.getParameter("createToken")); if
     * (req.getMethod().equals("GET") && ((null == req.getHeader("X-Requested-With")) || createToken
     * || (!req.getHeader("X-Requested-With").equalsIgnoreCase("XMLHttpRequest")))) {
     * releaseRequestLatch(sc); }
     */
    return false;
  }
}
