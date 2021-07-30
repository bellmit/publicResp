package com.insta.hms.messaging.action;

import com.insta.hms.common.annotations.IgnoreConfidentialFilters;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The Class StdNotificationAction.
 */
public class StdNotificationAction extends DispatchAction {

  /**
   * Reply.
   *
   * @return the action forward
   */
  @IgnoreConfidentialFilters
  public ActionForward reply() {
    return null;
  }

  /**
   * Do action.
   *
   * @param mapping
   *          the mapping
   * @param form
   *          the form
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the action forward
   * @throws Exception
   *           the exception
   */
  @IgnoreConfidentialFilters
  public ActionForward doAction(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws Exception {
    boolean success = false;
    Map<String, Object> actionContext = new HashMap<String, Object>();
    Integer messageId = null;
    String screen = null;
    String messageActionType = (String) request.getParameter("message_action_type");
    screen = (String) request.getParameter("screen");
    messageId = Integer.parseInt(request.getParameter("message_log_id"));
    MessageActionHandler handler = MessageActionHandler.get(messageActionType);
    actionContext.put("username", request.getSession(false).getAttribute("userid"));
    String option = (String) request.getParameter("option");
    handler.doHandle(messageId, option, actionContext);

    if (screen.equals("sent_msg")) {
      return mapping.findForward("myMsglist");
    } else if (screen.equals("archive_msg")) {
      return mapping.findForward("archivelist");
    }

    return mapping.findForward("myNotificationMsglist");
  }
}
