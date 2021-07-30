package com.insta.hms.common;

import org.apache.commons.chain.Context;
import org.apache.commons.chain.Filter;
import org.apache.struts.chain.commands.ActionCommandBase;
import org.apache.struts.chain.contexts.ActionContext;
import org.apache.struts.chain.contexts.ServletActionContext;
import org.apache.struts.util.MessageResources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Semaphore;

import javax.servlet.http.HttpSession;

/**
 * The Class ThrottleRequest.
 */
public class ThrottleRequest extends ActionCommandBase implements Filter {

  /** The log. */
  static Logger log = LoggerFactory.getLogger(ThrottleRequest.class);

  /** The Constant REQUEST_LATCH_KEY. */
  private static final String REQUEST_LATCH_KEY = "request_latch";

  /** The Constant MAX_CONCURRENT_REQUESTS_KEY. */
  private static final String MAX_CONCURRENT_REQUESTS_KEY = "max.concurrent.requests";

  /** The Constant REQUEST_LATCH_WAIT_QUEUE. */
  private static final String REQUEST_LATCH_WAIT_QUEUE = "request.latch.wait.queue";

  /** The Constant TOO_MANY_REQUESTS. */
  private static final String TOO_MANY_REQUESTS = "too.many.requests.msg";

  /** The Constant REQUEST_LATCH_ARCQUIRED. */
  protected static final String REQUEST_LATCH_ARCQUIRED = "request_latch_acquired";

  /**
   * Execute.
   *
   * @param ctx the ctx
   * @return true, if successful
   * @throws Exception the exception
   */
  public boolean execute(ServletActionContext ctx) throws Exception {
    // Acquire the latch, continue / stop command chain depending on return value
    boolean retVal = acquireRequestLatch(ctx);
    if (retVal) {
      MessageResources resource = ctx.getMessageResources();
      String message = getMessage(resource, TOO_MANY_REQUESTS);
      message = (message == null) ? "" : message;

      ctx.getRequest().setAttribute("error", message);
      ctx.getRequest().getRequestDispatcher("/pages/Common/ErrorPage.jsp").forward(ctx.getRequest(),
          ctx.getResponse());
    }
    return retVal;
  }

  /**
   * Command chain entry point into the command. Casts the ctx to a ServletActionContext and
   * delegates to the subclasses
   *
   * @param ctx - action context for the current command
   * @return true, if successful
   * @throws Exception the exception
   */
  public boolean execute(ActionContext ctx) throws Exception {
    return execute((ServletActionContext) ctx);
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

    ServletActionContext sc = (ServletActionContext) ctx;
    boolean latchAcquired = false;
    latchAcquired = sc.get(REQUEST_LATCH_ARCQUIRED) == null ? latchAcquired
        : (Boolean) sc.get(REQUEST_LATCH_ARCQUIRED);
    log.debug("REQUEST_LATCH_ARCQUIRED = " + latchAcquired);
    // Release request latch only if acquired
    if (latchAcquired) {
      log.debug("releasing request latch");
      releaseRequestLatch(sc);
    }
    return false;

  }

  /**
   * Acquire request latch.
   *
   * @param ctx the ctx
   * @return true, if successful
   */
  protected boolean acquireRequestLatch(ServletActionContext ctx) {
    MessageResources resource = ctx.getMessageResources();
    int maxConcurrentRequests = getIntValue(resource, MAX_CONCURRENT_REQUESTS_KEY);
    int requestLatchWaitQueue = getIntValue(resource, REQUEST_LATCH_WAIT_QUEUE);
    Semaphore requestLatch = null;
    if (maxConcurrentRequests > 0) {

      HttpSession session = ctx.getRequest().getSession();

      synchronized (session) {
        requestLatch = (Semaphore) session.getAttribute(REQUEST_LATCH_KEY);
        log.debug("-> @acquire " + ((requestLatch == null) ? ""
            : requestLatch.toString().replace("java.util.concurrent.", "")));
        if (null == requestLatch) {
          requestLatch = new Semaphore(maxConcurrentRequests);
          session.setAttribute(REQUEST_LATCH_KEY, requestLatch);
          log.debug("--> %set " + requestLatch.toString().replace("java.util.concurrent.", ""));
        }
      }

      if (null != requestLatch) {
        log.debug("requestLatch queue length = " + requestLatch.getQueueLength());
        if (requestLatch.getQueueLength() >= requestLatchWaitQueue) {
          log.debug("queue greater than maxRequests. Returning true");
          return true;
        }
        requestLatch.acquireUninterruptibly();
        ctx.put(REQUEST_LATCH_ARCQUIRED, Boolean.TRUE);
      }
    }

    log.debug("<- @acquire " + ((requestLatch == null) ? ""
        : requestLatch.toString().replace("java.util.concurrent.", "")));
    return false;
  }

  /**
   * Gets the int value.
   *
   * @param resource the resource
   * @param key      the key
   * @return the int value
   */
  private int getIntValue(MessageResources resource, String key) {
    Integer intValue = 0;
    log.debug("Entering getIntValue... " + key);
    String configValue = getMessage(resource, key);
    if (null != configValue) {
      try {
        intValue = Integer.valueOf(configValue);
      } catch (NumberFormatException nfe) {
        log.error("Invalid setting in application.properties for " + key + ", ignoring value");
        intValue = 0;
      }
    }
    log.debug("Exiting getIntValue... " + key);
    return intValue;
  }

  /**
   * Gets the message.
   *
   * @param resource the resource
   * @param key      the key
   * @return the message
   */
  private String getMessage(MessageResources resource, String key) {
    log.debug("Entering getMessage... key = " + key);
    // MessageResources resource = ctx.getMessageResources();
    String message = null;
    if (null != resource) {
      message = resource.getMessage(key);
    }
    log.debug("Exiting getMessage... key = " + key);
    return message;
  }

  /**
   * Release request latch.
   *
   * @param ctx the ctx
   */
  protected void releaseRequestLatch(ServletActionContext ctx) {
    HttpSession session = ctx.getRequest().getSession();
    Semaphore requestLatch = (Semaphore) session.getAttribute(REQUEST_LATCH_KEY);
    log.debug("-> #release " + ((requestLatch == null) ? ""
        : requestLatch.toString().replace("java.util.concurrent.", "")));
    if (null != requestLatch) {
      requestLatch.release();
      log.debug("latch queue length post release = " + requestLatch.getQueueLength());
    }
    log.debug("<- #release " + ((requestLatch == null) ? ""
        : requestLatch.toString().replace("java.util.concurrent.", "")));
    return;
  }

}
