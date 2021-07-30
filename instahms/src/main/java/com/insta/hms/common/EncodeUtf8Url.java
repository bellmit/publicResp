package com.insta.hms.common;

import org.apache.struts.action.ActionRedirect;
import org.apache.struts.chain.commands.ActionCommandBase;
import org.apache.struts.chain.contexts.ActionContext;
import org.apache.struts.config.ForwardConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class EncodeUtf8Url.
 */
public class EncodeUtf8Url extends ActionCommandBase {

  static Logger logger = LoggerFactory.getLogger(EncodeUtf8Url.class);

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.struts.chain.commands.ActionCommandBase#execute(
   * org.apache.struts.chain.contexts.ActionContext)
   */
  @Override
  public boolean execute(ActionContext ctx) throws Exception {
    ForwardConfig originalForward = ctx.getForwardConfig();
    logger.debug("originalForward : " + originalForward);
    if (null != originalForward && originalForward.getRedirect()) {
      String uri = originalForward.getPath();
      logger.debug("original uri : " + uri);
      if (null != uri && uri.length() > 0) {
        String encodedUri = new String(uri.getBytes("UTF-8"), "ISO-8859-1");
        ActionRedirect newRedirect = new ActionRedirect(originalForward);
        newRedirect.setPath(encodedUri);
        logger.debug("encoded uri : " + encodedUri);
        ctx.setForwardConfig(newRedirect);
      }
    }
    return false;
  }
}
