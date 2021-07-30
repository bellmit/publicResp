package com.insta.hms.common;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.security.usermanager.UserService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class PushService to provide support to push changes to connected clients. Currently, only
 * pushes using web sockets. TODO - Bring redis pushes also into PushService.
 * 
 * @author tanmay.k
 * 
 */
@Service("pushService")
public class PushService {

  /** The push template. */
  @LazyAutowired
  private SimpMessagingTemplate pushTemplate;

  /** The user service. */
  @LazyAutowired
  private UserService userService;

  /**
   * Push to all schemas.
   *
   * @param destination the destination
   * @param payload     the payload
   */
  public void pushToAllSchemas(String destination, Object payload) {
    this.pushToAllSchemas(destination, payload, null);
  }

  /**
   * Push to all schemas.
   *
   * @param destination the destination
   * @param payload     the payload
   * @param headers     the headers
   */
  public void pushToAllSchemas(String destination, Object payload, Map<String, Object> headers) {
    this.pushTemplate.convertAndSend(destination, payload, headers);
  }

  /**
   * Push.
   *
   * @param destination the destination
   * @param payload     the payload
   */
  public void push(String destination, Object payload) {
    List<BasicDynaBean> users = userService.listAll();
    for (BasicDynaBean user : users) {
      Object loginHandle = user.get("login_handle");
      if (null != loginHandle && !"".equals(loginHandle)) {
        pushToSession((String) loginHandle, destination, payload);
      }
    }
  }

  /**
   * Push to user.
   *
   * @param userName    the user name
   * @param destination the destination
   * @param payload     the payload
   */
  public void pushToUser(String userName, String destination, Object payload) {
    BasicDynaBean userBean = userService.findByKey("emp_username", userName);
    Object loginHandle = userBean.get("login_handle");
    if (null != loginHandle && !"".equals(loginHandle)) {
      pushToSession((String) loginHandle, destination, payload);
    }
  }

  /**
   * Push to session.
   *
   * @param sessionId   the session id
   * @param destination the destination
   * @param payload     the payload
   */
  public void pushToSession(String sessionId, String destination, Object payload) {
    SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor
        .create(SimpMessageType.MESSAGE);
    headerAccessor.setSessionId(sessionId);
    headerAccessor.setLeaveMutable(true);
    this.pushTemplate.convertAndSendToUser(sessionId, destination, payload,
        headerAccessor.getMessageHeaders());
  }

}
