package com.insta.hms.common;

import org.apache.commons.chain.Context;
import org.apache.commons.chain.Filter;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.chain.contexts.ServletActionContext;
import org.apache.struts.config.ForwardConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;

/**
 * The Class CheckToken.
 */
public class CheckToken extends TokenCommandBase implements Filter {

  /** The log. */
  static Logger log = LoggerFactory.getLogger(CheckToken.class);

  @Override
  public boolean execute(ServletActionContext ctx) throws Exception {

    HttpServletRequest req = ctx.getRequest();

    // If it is a post and contains a token, check for valid token
    if (isCheckRequired(req)) {
      String tokenKey = req.getParameter(TRANSACTION_TOKEN_KEY);
      log.debug("Acquiring token : " + tokenKey);

      // This is a blocking call - blocks on the token lock.
      // If the token is found in the token Q, this call returns only when it has
      // acquired a lock on the token
      RequestToken token = getToken(ctx, tokenKey);

      if (null != token) {
        if (!token.isValid()) {

          ForwardConfig lastResult = null;
          String referer = req.getHeader("Referer");

          // skip the action execution
          log.warn("Stale Token, Skipping the action ...");
          ctx.setAction(null);

          // set the result to the last known good result
          if (null != token.getLastResult() && token.getLastResult().getRedirect()) {
            lastResult = token.getLastResult();
          } else {
            // If the lastResult is null or if the lastResult was not a redirect
            // then we redirect to the referer page.
            log.warn("PRG not used, redirecting to referrer..."
                + ((null != token.getLastResult()) ? token.getLastResult().getName() : "(null)"));
            lastResult = new ActionRedirect(referer);
          }

          ctx.setForwardConfig(lastResult);
          releaseToken(ctx, token);
        } else {
          log.debug("Valid Token, Executing the action ...");
          ctx.put(TRANSACTION_TOKEN_KEY, token);
        }
      }
    }
    // continue the command chain
    return false;
  }

  /**
   * Checks if is check required.
   *
   * @param req the req
   * @return true, if is check required
   */
  private boolean isCheckRequired(HttpServletRequest req) {
    // we dont need to check if it is not a post or the token key is not
    // submitted as part of the transaction
    if (!req.getMethod().equalsIgnoreCase("POST")
        || null == req.getParameter(TRANSACTION_TOKEN_KEY)) {
      log.debug("isCheckRequired exiting (false) : " + req.getMethod());
      return false;
    }

    return true;
  }

  @Override
  public boolean postprocess(Context ctx, Exception excep) {

    // This method will be called when an exception occurs in the
    // command chain or the chain was terminated (normal & premature)
    // We need to check if we hold the token at this point and if so release it.

    // Under normal conditions, the token will be null, by the time we reach here.
    // If it is not, in all likelyhood, we did not reach the ResetToken command.
    // So we release the token using releaseToken which is a safe method - it releases
    // the token if and only if it is held by the current thread.

    RequestToken token = (RequestToken) ctx.get(TRANSACTION_TOKEN_KEY);
    if (null != token) {
      log.debug("Cleaning up token ...");
      // TODO: Process the exception
      releaseToken((ServletActionContext) ctx, token);
    }
    // continue the command chain
    return false;
  }
}
