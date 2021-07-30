package com.insta.hms.api.controllers;

import com.insta.hms.common.BaseRestController;
import com.insta.hms.messagingframework.MessagingService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Map;

@Controller
@RequestMapping("/api/message")
public class ExternalMessageController extends BaseRestController {

  /**
   * Process inbound message.
   *
   * @param requestHandlerKey
   *          the request handler key
   * @param inboundMessage
   *          the inbound message
   * @param parserType
   *          the parser type
   * @return the map
   */
  @RequestMapping(value = "/inbound", method = RequestMethod.GET)
  public Map<String, Object> processInboundMessage(
      @RequestParam(value = "request_handler_key") String requestHandlerKey,
      @RequestParam String inboundMessage, @RequestParam String parserType) {
    return MessagingService.processInboundMessage(inboundMessage, parserType);
  }
  
  /**
   * Trigger report notification.
   *
   * @param requestHandlerKey the request handler key
   * @param reportId the report id
   */
  @RequestMapping(value = "/notification", method = RequestMethod.GET)
  public void triggerReportNotification(
      @RequestParam(value = "request_handler_key") String requestHandlerKey,
      @RequestParam String reportId, @RequestParam String isTestDoc)
      throws SQLException, ParseException, IOException {
    MessagingService.sendReportNotification(reportId, isTestDoc);
  }
}
