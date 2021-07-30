package com.insta.hms.common;

import org.apache.struts.chain.contexts.ServletActionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;

/**
 * The Class ResetToken.
 */
public class ResetToken extends TokenCommandBase {

  /** The log. */
  static Logger log = LoggerFactory.getLogger(ResetToken.class);

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.common.TokenCommandBase#execute(
   * org.apache.struts.chain.contexts.ServletActionContext)
   */
  @Override
  public boolean execute(ServletActionContext ctx) throws Exception {

    HttpServletRequest req = ctx.getRequest();

    // If it is a post and contains a token, reset the token
    if (req.getMethod().equalsIgnoreCase("POST")) {
      RequestToken token = (RequestToken) ctx.get(TRANSACTION_TOKEN_KEY);
      if (null != token && token.isValid()) {
        token.setLastResult(ctx.getForwardConfig());
        releaseToken(ctx, token);
        ctx.remove(TRANSACTION_TOKEN_KEY);
        log.debug("Token reset : " + token.getTokenKey());
      }
    }

    // continue the chain
    return false;
  }
}
