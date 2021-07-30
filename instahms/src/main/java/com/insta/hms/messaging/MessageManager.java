package com.insta.hms.messaging;

import static com.insta.hms.jobs.common.QuartzJobHelper.buildJob;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.JobSchedulingService;
import com.insta.hms.jobs.JobService;
import com.insta.hms.messagingframework.MessagingService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.mail.MessagingException;

/**
 * The Class MessageManager.
 */
public class MessageManager {

  /** The message log attachments dao. */
  private static GenericDAO messageLogAttachmentsDao = new GenericDAO("message_log_attachments");
  
  /** The message types dao. */
  private static GenericDAO messageTypesDao = new GenericDAO("message_types");
  
  /** The message log dao. */
  private static GenericDAO messageLogDao = new GenericDAO("message_log");
  
  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(MessageManager.class);
  
  /** The Constant SUCCESS_MESSAGE. */
  private static final String SUCCESS_MESSAGE = "Message sent successfully";
  
  /** The Constant DISPATCHER_ERROR. */
  private static final String DISPATCHER_ERROR = "Dispatcher Not Configured / Inactive";

  /**
   * Process event.
   *
   * @param eventId the event id
   * @param eventData the event data
   * @return true, if successful
   * @throws SQLException the SQL exception
   * @throws ParseException the parse exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public boolean processEvent(String eventId, Map eventData)
      throws SQLException, ParseException, IOException {
    return processEvent(eventId, eventData, true);
  }

  /**
   * Process event.
   *
   * @param eventId the event id
   * @param eventData the event data
   * @param asynchronous the asynchronous
   * @return true, if successful
   * @throws SQLException the SQL exception
   * @throws ParseException the parse exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public boolean processEvent(String eventId, Map eventData, boolean asynchronous)
      throws SQLException, ParseException, IOException {
    if (logger.isDebugEnabled()) {
      logger.debug("Entering processEvent ...event :" + eventId);
    }

    if (null == eventId) {
      logger.info("Invalid parameter eventName : null");
      return false;
    }

    int result = 0;

    List<BasicDynaBean> messageTypes = messageTypesDao.findAllByKey("event_id", eventId);

    for (BasicDynaBean messageType : messageTypes) {

      // Check if the message type is active.
      if ("I".equalsIgnoreCase((String) messageType.get("status"))) {
        logger.info("Skipping Inactive message type : " + messageType.get("message_type_name")
            + " for event " + eventId);
        continue;
      }

      // Create a message context based on the input parameters
      String messageTypeId = (String) messageType.get("message_type_id");
      logger.info("Message type :" + messageTypeId);
      String messageMode = (String) messageType.get("message_mode");

      MessageDispatcher dispatcher = MessageDispatcherFactory.getDispatcher(messageMode);

      if (null == dispatcher) {
        logger.info("No dispatcher found for message mode " + messageMode
            + ", Skipping processing.");
        return false;
      }

      // Dispatch the messages

      if (asynchronous) {
        String userName = RequestContext.getUserName();
        Map<String, Object> jobData = new HashMap<String, Object>();
        jobData.put("schema", RequestContext.getSchema());
        jobData.put("userName", userName);
        jobData.put("centerId", RequestContext.getCenterId());
        jobData.put("messageTypeId", messageTypeId);
        jobData.put("messageMode", messageMode);
        jobData.put("eventData", eventData);
        jobData.put("userLocale", RequestContext.getLocale());
        JobService jobService = JobSchedulingService.getJobService();
        jobService.scheduleImmediate(buildJob(
            "MessageManagerJob_" + messageTypeId + "_" + userName 
            + "_" + DateUtil.getCurrentISO8601TimestampMillis(),
            MessageManagerJob.class, jobData));

        result = 1;
      } else {
        MessageContext ctx = MessageContext.fromType(messageTypeId);
        ctx.setEventData(eventData);

        // Initialize the builder with the input context
        MessageBuilder builder = MessageBuilderFactory.getBuilder(messageTypeId);

        // Build the messages
        builder.build(ctx);
        List<Message> msgs = builder.getMessageList();
        if (null != msgs && msgs.size() > 0) {
          result += sendMessages(messageTypeId, messageMode, msgs);
          if (logger.isDebugEnabled()) {
            logger.info("Exiting processEvent... total messages dispatched " + result);
          }
        }
      }
    }
    return (result > 0);
  }

  /**
   * Send messages.
   *
   * @param messageTypeId the message type id
   * @param mode the mode
   * @param messageList the message list
   * @return the int
   * @throws SQLException the SQL exception
   * @throws ParseException the parse exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public int sendMessages(String messageTypeId, String mode, List<Message> messageList)
      throws SQLException, ParseException, IOException {

    BasicDynaBean messageTypeBean = messageTypesDao.findByKey("message_type_id", messageTypeId);
    boolean isMessageConfidential = false;
    if (messageTypeBean != null 
        && messageTypeBean.get("confidential") != null 
        && "Y".equals( (String)messageTypeBean.get("confidential"))) {
      isMessageConfidential = true;
    }

    int result = 0;

    if (logger.isDebugEnabled()) {
      logger.debug("Entering..." + "mode :" + mode + " messageList :" + messageList);
    }

    MessageDispatcher dispatcher = MessageDispatcherFactory.getDispatcher(mode);

    if (null == dispatcher) {
      logger.info("No dispatcher found for message mode " + mode + ", Skipping processing.");
      return result;
    }

    for (Message msg : messageList) {
      msg.setMessageLogId(messageLogDao.getNextSequence());

      String statusMessage = SUCCESS_MESSAGE;
      boolean success = false;

      try {
        success = dispatcher.dispatch(msg);
        statusMessage = success ? SUCCESS_MESSAGE : DISPATCHER_ERROR;
      } catch (SQLException se) {
        logger.error("1 Message dispatch failed : " + se);
        statusMessage = se.getMessage();
      } catch (MessagingException me) {
        statusMessage = me.getMessage();
        logger.error("2 Message dispatch failed : " + me);
      }
      if (isMessageConfidential) {
        msg.setBody((String)messageTypeBean.get("message_body"));
      }
      insertMessageLog(msg, mode, success, statusMessage);

      if (success) {
        result++;
      }
    }
    return result;
  }

  /**
   * Resend message.
   *
   * @param messageLogId the message log id
   * @param overrides the overrides
   * @return true, if successful
   * @throws SQLException the SQL exception
   * @throws ParseException the parse exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public boolean resendMessage(int messageLogId, Map overrides)
      throws SQLException, ParseException, IOException {

    boolean result = false;

    if (logger.isDebugEnabled()) {
      logger.debug("Entering..." + "messageLogId :" + messageLogId);
    }

    BasicDynaBean messageBean = messageLogDao.findByKey("message_log_id", messageLogId);
    String mode = (String) messageBean.get("message_mode");

    MessageDispatcher dispatcher = MessageDispatcherFactory.getDispatcher(mode);

    if (null == dispatcher) {
      logger.info("No dispatcher found for message mode " + mode + ", Skipping processing.");
      return result;
    }

    Message message = new Message();

    String recipients = (null != overrides && overrides.containsKey("message_to"))
        ? (String) overrides.get("message_to") : ((String) messageBean.get("message_to"));

    String[] recipientsList = (null != recipients) ? recipients.split(",") : new String[0];

    String recipientsCc = (null != overrides && overrides.containsKey("message_cc"))
        ? (String) overrides.get("message_cc") : (String) messageBean.get("message_cc");
    String[] recipientsCcList = (null != recipientsCc && !(recipientsCc.equals("")))
        ? recipientsCc.split(",") : new String[0];

    String recipientsBcc = (null != overrides && overrides.containsKey("message_bcc"))
        ? (String) overrides.get("message_bcc") : (String) messageBean.get("message_bcc");
    String[] recipientsBccList = (null != recipientsBcc && !(recipientsBcc.equals("")))
        ? recipientsBcc.split(",") : new String[0];

    message.addRecipients(recipientsList);
    message.addReceipients(recipientsBccList, Message.RECIPIENT_BCC);
    message.addReceipients(recipientsCcList, Message.RECIPIENT_CC);

    if (null != messageBean.get("message_body")) {
      message.setBody((String) messageBean.get("message_body"));
    }

    String sender = (null != overrides && overrides.containsKey("message_sender"))
        ? (String) overrides.get("message_sender") : (String) messageBean.get("message_sender");

    message.setSender(sender);

    if (null != messageBean.get("message_subject")) {
      message.setSubject((String) messageBean.get("message_subject"));
    }
    if (null != messageBean.get("message_type_id")) {
      message.setMessageType((String) messageBean.get("message_type_id"));
    }
    message.setMessageLogId(messageLogId);

    GenericDAO attachmentDao = messageLogAttachmentsDao;
    Map filtermap = new HashMap();
    filtermap.put("message_log_id", messageLogId);
    List<String> columns = new ArrayList();
    columns.add("attachment_id");
    List<BasicDynaBean> attachBeans = attachmentDao.listAll(columns, filtermap, null);
    if (null != attachBeans) {
      for (int i = 0; i < attachBeans.size(); i++) {
        BasicDynaBean attachBean = attachmentDao.getBean();
        attachmentDao.loadByteaRecords(attachBean, "attachment_id",
            attachBeans.get(i).get("attachment_id"));
        String attachName = (String) attachBean.get("attachment_name");
        String contentType = (String) attachBean.get("attachment_type");
        InputStream attachStream = (InputStream) attachBean.get("attachment_bytes");

        byte[] bytes = DataBaseUtil.readInputStream(attachStream);
        message.addAttachment(attachName, contentType, bytes);
      }
    }

    String statusMessage = SUCCESS_MESSAGE;
    try {
      result = dispatcher.dispatch(message);
      statusMessage = result ? SUCCESS_MESSAGE : DISPATCHER_ERROR;
    } catch (SQLException se) {
      statusMessage = se.getMessage();
    } catch (MessagingException me) {
      statusMessage = me.getMessage();
    }
    updateMessageLog(messageBean, result, statusMessage);
    return result;
  }

  /**
   * Insert message log.
   *
   * @param msg the msg
   * @param mode the mode
   * @param status the status
   * @param statusMessage the status message
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private void insertMessageLog(Message msg, String mode, boolean status, String statusMessage)
      throws SQLException, IOException {

    if (logger.isDebugEnabled()) {
      logger.debug("Entering..." + "message :" + msg + "status : " + status);
    }
    Connection con = null;
    boolean success = false;

    try {
      GenericDAO dao = messageLogDao;
      BasicDynaBean bean = dao.getBean();
      bean.set("message_log_id", msg.getMessageLogId());
      bean.set("message_type_id", msg.getMessageType());
      bean.set("message_mode", mode);
      bean.set("message_body", msg.getBody());
      bean.set("message_subject", msg.getSubject());
      bean.set("message_sender", msg.getSender());
      bean.set("message_sender_id", RequestContext.getUserName());
      bean.set("message_center_id", RequestContext.getCenterId());
      bean.set("message_sender_type", "SENDER_USR");
      bean.set("message_to", msg.getRecipientString(Message.RECIPIENT_TO));
      bean.set("message_cc", msg.getRecipientString(Message.RECIPIENT_CC));
      bean.set("message_bcc", msg.getRecipientString(Message.RECIPIENT_BCC));
      bean.set("last_status", (status ? "S" : "F"));
      bean.set("last_status_message", statusMessage);
      bean.set("last_sent_date", DateUtil.getCurrentTimestamp());
      bean.set("retry_count", 0);
      bean.set("entity_id", msg.getEntityId());
      bean.set("batch_id", msg.getBatchId());

      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      success = dao.insert(con, bean);

      GenericDAO recipientDao = new GenericDAO("message_recipient");
      BasicDynaBean recipientBean = recipientDao.getBean();
      recipientBean.set("message_log_id", bean.get("message_log_id"));
      recipientBean.set("message_recipient_id", msg.getReceipient_id__());
      recipientBean.set("message_recipient_type", msg.getReceipient_type__());
      recipientBean.set("message_status", (status ? "S" : "F"));
      success = recipientDao.insert(con, recipientBean);

      if (success) {
        GenericDAO attachDao = messageLogAttachmentsDao;
        BasicDynaBean attachBean = attachDao.getBean();
        List<BasicDynaBean> attBeans = new ArrayList<BasicDynaBean>();
        for (EmailAttachment att : msg.getAllAttachments()) {
          attachBean.set("attachment_id", attachDao.getNextSequence());
          attachBean.set("message_log_id", bean.get("message_log_id"));
          attachBean.set("attachment_name", att.getName());
          attachBean.set("attachment_type", att.getContentType());
          attachBean.set("attachment_bytes", att.getInputStream());
          success &= attachDao.insert(con, attachBean);
        }
      }

    } finally {

      if (null != con) {
        if (success) {
          con.commit();
        } else {
          con.rollback();
        }
        con.close();
      }
    }
    // deleting redisKey for notificationCount
    if (success && mode.equals("NOTIFICATION")) {
      MessagingService messagingService =
          (MessagingService) ApplicationContextProvider.getBean(MessagingService.class);
      messagingService.deleteNotificationFromRedisCache(msg.getReceipient_id__());
    }
  }

  /**
   * Update message log.
   *
   * @param bean the bean
   * @param status the status
   * @param statusMessage the status message
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private void updateMessageLog(BasicDynaBean bean, boolean status, String statusMessage)
      throws SQLException, IOException {

    if (logger.isDebugEnabled()) {
      logger.debug("Entering..." + "status :" + status + " err : " + statusMessage);
    }
    Connection con = null;
    int success = 0;
    int sendCount = -1;

    if (bean.get("retry_count") != null) {
      sendCount = (Integer) bean.get("retry_count");
    }

    try {
      if (status) {
        bean.set("last_status", (status ? "S" : "F"));
        bean.set("last_status_message", statusMessage);
        bean.set("last_sent_date", DateUtil.getCurrentTimestamp());
        bean.set("retry_count", sendCount + 1);
      }

      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      success = messageLogDao.updateWithName(con, bean.getMap(), "message_log_id");

    } finally {

      if (null != con) {
        if (success > 0) {
          con.commit();
        } else {
          con.rollback();
        }
        con.close();
      }
    }
  }

  /**
   * Gets the message tokens.
   *
   * @param messageTypeId the message type id
   * @return the message tokens
   * @throws SQLException the SQL exception
   * @throws ParseException the parse exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public Map<String, List> getMessageTokens(String messageTypeId)
      throws SQLException, ParseException, IOException {
    MessageBuilder builder = MessageBuilderFactory.getBuilder(messageTypeId);
    return builder.getMessageTokens();
  }

  /**
   * Gets the message tokens.
   *
   * @param messageTypeId the message type id
   * @param providerName the provider name
   * @return the message tokens
   * @throws SQLException the SQL exception
   * @throws ParseException the parse exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public Map<String, List> getMessageTokens(String messageTypeId, String providerName)
      throws SQLException, ParseException, IOException {
    MessageBuilder builder = MessageBuilderFactory.getBuilder(messageTypeId);
    return builder.getMessageTokens(providerName);
  }
}
