package com.insta.hms.messagingframework;

import com.insta.hms.common.BaseController;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.mdm.systemmessage.SystemMessageService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The Class MessagingController.
 *
 * @author pranaysahota
 */
@RestController
@RequestMapping("/messages")
public class MessagingController extends BaseController {

  /** The messaging service. */
  @LazyAutowired
  private MessagingService messagingService;

  /** The session service. */
  @LazyAutowired
  private SessionService sessionService;

  /** The System message service. */
  @LazyAutowired
  private SystemMessageService systemMessageService;

  /**
   * Gets the notification metadata.
   *
   * @return the notification metadata
   * @throws SQLException the SQL exception
   */
  @IgnoreConfidentialFilters
  @GetMapping(value = "/user/notifications/metadata")
  public NotificationMetadata getNotificationMetadata() throws SQLException {
    String userId = (String) sessionService.getSessionAttributes().get("userId");
    return messagingService.getNotificationMetadata(userId);
  }

  /**
   * Gets the message details.
   *
   * @param messageType
   *          the message type
   * @return the message details
   */
  @IgnoreConfidentialFilters
  @GetMapping(value = "/user/messagedetailslist")
  public Map<String, Object> getMessageDetails(
      @RequestParam(value = "message_type_id", required = true) String messageType,
      @RequestParam(value = "page_num", required = false) Integer pageNum,
      @RequestParam(value = "page_size", required = false) Integer pageSize) {
    String userId = (String) sessionService.getSessionAttributes().get("userId");
    return messagingService.getMessageDetailsList(messageType, userId, pageNum, pageSize);
  }

  /**
   * Process message action.
   *
   * @param requestBody the request body
   * @return the response entity
   * @throws Exception the exception
   */
  @PostMapping(value = "/processAction")
  public ResponseEntity<Map<String, Object>> processMessageAction(@RequestBody ModelMap requestBody)
      throws Exception {
    return new ResponseEntity<>(messagingService.processAction(requestBody), HttpStatus.OK);
  }

  /**
   * Details.
   *
   * @param request the request
   * @param response the response
   * @return the list
   */
  @IgnoreConfidentialFilters
  @GetMapping(value = "/user/systemmessages")
  public Map<String, Object> details(HttpServletRequest request, HttpServletResponse response) {
    return systemMessageService.systemMessages("search");
  }
  
}
