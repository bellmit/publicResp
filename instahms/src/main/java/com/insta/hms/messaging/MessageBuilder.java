package com.insta.hms.messaging;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.Registration.PatientDetailsDAO;
import com.insta.hms.billing.BillPrintHelper;
import com.insta.hms.common.AppInit;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.messaging.providers.DiagReportMessageBuilder;
import com.insta.hms.messaging.providers.MessageDataProvider;
import com.insta.hms.stores.POReportGenrator;

import freemarker.template.Template;
import freemarker.template.TemplateException;
import jlibs.core.util.regex.TemplateMatcher;
import jlibs.core.util.regex.TemplateMatcher.VariableResolver;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The Class MessageBuilder.
 */
public class MessageBuilder {

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(MessageBuilder.class);

  /** The data built. */
  private boolean dataBuilt = false;

  /** The provider data map. */
  // private List<MessageContext> contexts = new ArrayList<MessageContext>();
  private Map<String, List<Map>> providerDataMap = new HashMap<String, List<Map>>();

  /** The data providers. */
  private List<MessageDataProvider> dataProviders = new ArrayList<MessageDataProvider>();

  /** The recipient filter. */
  private Map<String, Set<String>> recipientFilter = null;

  /** The context. */
  private MessageContext context = null;

  /**
   * Builds the.
   *
   * @param ctx
   *          the ctx
   * @throws ParseException
   *           the parse exception
   * @throws SQLException
   *           the SQL exception
   */
  public void build(MessageContext ctx) throws ParseException, SQLException {
    // Check if the data is already built.
    if (dataBuilt) {
      return;
    }

    if (!preProcess(ctx)) {
      dataBuilt = true;
      context = ctx;
      return;
    }

    // Build the data from each provider
    List<MessageDataProvider> providers = getMessageDataProviders();
    for (MessageDataProvider provider : providers) {

      MessageContext copyCtx = null;
      if (null != ctx) {
        copyCtx = (MessageContext) ctx.clone();
      }

      if (null != recipientFilter && recipientFilter.size() > 0) {
        // Do not process the provider if the filter is set but
        // this provider is not specified in the filter.
        if (null == recipientFilter.get(provider.getName())
            || recipientFilter.get(provider.getName()).size() == 0) {
          continue;
        } else {
          // Set the filter on the provider if it has a filter specified.
          provider
              .addRecipientFilter(new ArrayList<String>(recipientFilter.get(provider.getName())));
        }
      }

      // Now process the provider
      List<Map> msgDataList = provider.getMessageDataList(copyCtx);
      if (null != msgDataList) {
        providerDataMap.put(provider.getName(), msgDataList);
      }
    }
    dataBuilt = true;
    context = ctx;
  }

  /**
   * Gets the message data providers.
   *
   * @return the message data providers
   */
  protected List<MessageDataProvider> getMessageDataProviders() {
    return dataProviders;
  }

  /**
   * Gets the message list.
   *
   * @return the message list
   */
  public List<Message> getMessageList() {
    List<Message> msgList = new ArrayList<Message>();
    for (MessageDataProvider provider : dataProviders) {
      List<Map> dataList = providerDataMap.get(provider.getName());
      if (null != dataList) {
        msgList.addAll(processDataList(provider, dataList, context));
      }
    }
    return msgList;
  }

  /** The Constant DEFAULT_SUBJECT. */
  private static final String DEFAULT_SUBJECT = "(No Subject)";

  /** The Constant MAX_BATCH_SIZE. */
  private static final int MAX_BATCH_SIZE = 1000;

  /**
   * Process data list.
   *
   * @param provider
   *          the provider
   * @param dataList
   *          the data list
   * @param ctx
   *          the ctx
   * @return the list
   */
  protected List<Message> processDataList(MessageDataProvider provider, List<Map> dataList,
      MessageContext ctx) {

    // TODO: move this code to a helper class

    List<Message> msgList = new ArrayList<Message>();
    Map messageType = ctx.getMessageType();
    Message message = Message.fromTemplate(messageType);
    Map dataMap = null;

    if (null != dataList && dataList.size() > 0) {
      dataMap = dataList.get(0);
    }

    Map template = new HashMap();
    template.putAll(messageType);

    String sender = getMessageField(message.getSender(), dataMap, "message_sender", "");
    template.put("message_sender", sender);

    // initialize all message fields with appropriate defaults and tokens
    String body = getMessageField(message.getBody(), dataMap, "message_body", "");
    template.put("message_body", body);

    String messageMode = (String) messageType.get("message_mode");

    // Put the default subject if it is not already provided and the message is an email
    String subject = getMessageField(message.getSubject(), dataMap, "message_subject",
        "EMAIL".equalsIgnoreCase(messageMode) ? DEFAULT_SUBJECT : "");
    template.put("message_subject", subject);

    // Put the recipients depending on the mode
    String recipientsTo = "EMAIL".equalsIgnoreCase(messageMode)
        ? getMessageRecipientString(message, dataMap, "recipient_email", Message.RECIPIENT_TO)
        : getMessageRecipientString(message, dataMap, "recipient_mobile", Message.RECIPIENT_TO);
    template.put("message_to", recipientsTo);

    String recipientsCc = "";
    if ("EMAIL".equalsIgnoreCase(messageMode)) {
      getMessageRecipientString(message, dataMap, "recipient_email_cc", Message.RECIPIENT_CC);
    }
    template.put("message_cc", recipientsCc);

    String recipientsBcc = "";
    if ("EMAIL".equalsIgnoreCase(messageMode)) {
      getMessageRecipientString(message, dataMap, "recipient_email_bcc", Message.RECIPIENT_BCC);
    }

    if ("NOTIFICATION".equalsIgnoreCase(messageMode)) {
      recipientsCc = getMessageRecipientString(message, dataMap, "recipient_email_cc",
          Message.RECIPIENT_CC);
      recipientsBcc = getMessageRecipientString(message, dataMap, "recipient_email_bcc",
          Message.RECIPIENT_BCC);
    }

    if ("HL7_SOCKET_ADT".equalsIgnoreCase(messageMode)) {
      template.put("message_body", (String) dataList.get(0).get("_hl7_message"));
    }
    template.put("message_bcc", recipientsBcc);

    template.put("message_attachments", messageType.get("message_attachments"));
    if (dataMap != null && dataMap.containsKey("entity_id")) {
      template.put("entity_id", dataMap.get("entity_id"));
    }

    int batchSize = (dataList.size() > MAX_BATCH_SIZE ? MAX_BATCH_SIZE : dataList.size());
    for (int i = 0; i < batchSize; i++) {
      Map data = dataList.get(i);
      Message msg = Message.fromTemplate(template);
      msg = resolveMessageTokens(msg, data);
      if ("EMAIL".equalsIgnoreCase(messageMode)) {
        msg = attachContent(msg, data);
      }
      msgList.add(msg);
    }
    return msgList;
  }

  /**
   * Attach content.
   *
   * @param msg
   *          the msg
   * @param data
   *          the data
   * @return the message
   * 
   *         This method only take the byte content for sending attachment.
   */
  // This enhancement is for #42454
  protected Message attachContent(Message msg, Map data) {
    if (data.containsKey("_message_attachment")) {
      String category = (String) data.get("category");
      String attachment = null;
      if (category != null && !category.equalsIgnoreCase("phr_doc_upload")
          && !category.equalsIgnoreCase("DEP_RAD") && !category.equalsIgnoreCase("DEP_LAB")) {
        attachment = (String) data.get("_message_attachment");
      }
      String attachmentName = (String) data.get("message_attachment_name");
      int reportId = -1;
      int printerId = -1;
      if (data.containsKey("printtype")) {
        printerId = data.get("printtype") == null ? 0
            : Integer.parseInt((String) data.get("printtype"));
      }
      byte[] reportContent = null;
      try {
        if (category != null && (category.equalsIgnoreCase("Pharmacy")
            || category.equalsIgnoreCase("Bill") || category.equalsIgnoreCase("phr_doc_upload")
            || category.equalsIgnoreCase("Prescription") || category.equalsIgnoreCase("DEP_RAD")
            || category.equalsIgnoreCase("DEP_LAB"))) {
          if (category != null && category.equals("Pharmacy")) {
            // TODO : PDF generation should not be here. It should be moved in provider and set as a
            // byte content.
            reportContent = POReportGenrator.generatePOReportPDF(attachment, printerId);
          }
          if (category != null && category.equalsIgnoreCase("Bill")) {
            // TODO : PDF generation should not be here. It should be moved in provider and set as a
            // byte content.
            reportContent = BillPrintHelper.generateBillReportPDF(attachment, printerId);
          }
          if (category != null && category.equalsIgnoreCase("phr_doc_upload")) {
            reportContent = (byte[]) data.get("_message_attachment");
          }
          if (category != null && category.equalsIgnoreCase("Prescription")) {
            reportContent = (byte[]) data.get("_report_content_byte");
          }
          if (category != null && category.equalsIgnoreCase("DEP_RAD")
              || category.equalsIgnoreCase("DEP_LAB")) {
            reportContent = (byte[]) data.get("_message_attachment");
          }
        }
        if (attachmentName == null || attachmentName.equals("")) {
          attachmentName = "MessageAttachment";
        }
        if (attachment != null || !"".equals(attachment)) {
          if (category != null && category.equalsIgnoreCase("phr_doc_upload")) {
            msg.addAttachment(attachmentName, (String) data.get("content_type"), reportContent);
          } else {
            msg.addAttachment(attachmentName + ".pdf", "application/octet-stream", reportContent);
          }
        }
      } catch (Exception ex) {
        logger.error("Exception while converting the report to pdf" + reportId, ex);
        if (category.equals("Pharmacy")) {
          msg.clearRecipients();
        }
      }
    }
    return msg;
  }

  /**
   * Resolve message tokens.
   *
   * @param msg
   *          the msg
   * @param data
   *          the data
   * @return the message
   */
  private Message resolveMessageTokens(Message msg, Map data) {

    msg.setReceipient_id__((String) data.get("receipient_id__"));
    msg.setReceipient_type__((String) data.get("receipient_type__"));

    if (data.containsKey("batch_id")) {
      msg.setBatchId(Integer.parseInt((String) data.get("batch_id")));
    }

    if (data.containsKey("entity_id")) {
      msg.setEntityId(String.valueOf(data.get("entity_id")));
    }

    TemplateMatcher matcher = new MessageTemplateMatcher("${", "}");
    if (msg.getSender() != null) {
      msg.setSender(matcher.replace(msg.getSender(), data));
    }

    if (null != msg.getSubject()) {
      msg.setSubject(convertFtlBody(msg.getSubject(), data));
    }

    if (null != msg.getBody()) {
      msg.setBody(convertFtlBody(msg.getBody(), data));
    }

    String[] recipientArray = msg.getRecipientArray(Message.RECIPIENT_TO);
    List<String> recipientList = Collections.EMPTY_LIST;
    // logger.debug("Recipient Array Length : " + recipientArray.length);
    for (int i = 0; i < recipientArray.length; i++) {
      recipientArray[i] = matcher.replace(recipientArray[i], data);
    }

    msg.clearRecipients(Message.RECIPIENT_TO);
    recipientList = findMultipleEmailIDsFromRecipientArray(recipientArray);
    msg.addRecipients(recipientList.toArray(new String[recipientList.size()]));

    String[] ccRecipientArray = msg.getRecipientArray(Message.RECIPIENT_CC);
    for (int i = 0; i < ccRecipientArray.length; i++) {
      ccRecipientArray[i] = matcher.replace(ccRecipientArray[i], data);
    }

    msg.clearRecipients(Message.RECIPIENT_CC);
    recipientList = findMultipleEmailIDsFromRecipientArray(ccRecipientArray);
    msg.addReceipients(recipientList.toArray(new String[recipientList.size()]),
        Message.RECIPIENT_CC);

    String[] bccRecipientArray = msg.getRecipientArray(Message.RECIPIENT_BCC);
    for (int i = 0; i < bccRecipientArray.length; i++) {
      bccRecipientArray[i] = matcher.replace(bccRecipientArray[i], data);
    }

    msg.clearRecipients(Message.RECIPIENT_BCC);
    recipientList = findMultipleEmailIDsFromRecipientArray(bccRecipientArray);
    msg.addReceipients(recipientList.toArray(new String[recipientList.size()]),
        Message.RECIPIENT_BCC);

    return msg;
  }

  /**
   * Convert ftl body.
   *
   * @param msg
   *          the msg
   * @param data
   *          the data
   * @return the string
   */
  // Converting from FTL message body to string
  private String convertFtlBody(String msg, Map<String, Object> data) {
    Template template;
    try {
      StringReader reader = new StringReader(msg);
      template = new Template("MessageBody.ftl", reader, AppInit.getFmConfig());
      StringWriter writer = new StringWriter();
      // Replaceing null with empty string
      Map<String, Object> map = new HashMap<String, Object>();
      for (String key : data.keySet()) {
        if (null != data.get(key)) {
          map.put(key, data.get(key));
        } else {
          map.put(key, "");
        }
      }
      template.process(map, writer);
      return writer.toString();
    } catch (IOException ioe) {
      logger.error("FTL syntax error in sms body: " + ioe.getMessage());
    } catch (TemplateException te) {
      logger.error("FTL syntax error in sms body: " + te.getMessage());
    }
    return "";
  }

  /**
   * Gets the message recipient string.
   *
   * @param message
   *          the message
   * @param dataMap
   *          the data map
   * @param dataKey
   *          the data key
   * @param recipientType
   *          the recipient type
   * @return the message recipient string
   */
  private String getMessageRecipientString(Message message, Map dataMap, String dataKey,
      String recipientType) {
    String recipients = message.getRecipientString(recipientType);
    String[] defaultRecipients = new String[] { "${" + dataKey + "}" };

    if (null != dataMap && dataMap.containsKey(dataKey)) {
      if (null == recipients || recipients.trim().length() == 0) {
        recipients = "${" + dataKey + "}";
      } else {
        message.addReceipients(defaultRecipients, recipientType);
        recipients = message.getRecipientString(recipientType);
      }
    }
    return recipients;
  }

  /**
   * Gets the message field.
   *
   * @param templateValue
   *          the template value
   * @param dataMap
   *          the data map
   * @param dataKey
   *          the data key
   * @param defaultValue
   *          the default value
   * @return the message field
   */
  private String getMessageField(String templateValue, Map dataMap, String dataKey,
      String defaultValue) {
    String value = templateValue;
    if (null == value || value.trim().length() == 0) {

      if (null != dataMap && dataMap.containsKey(dataKey)) {
        value = "${" + dataKey + "}";
      } else if (null != defaultValue) {
        value = defaultValue;
      }

    }
    return value;
  }

  /**
   * Adds the data provider.
   *
   * @param provider
   *          the provider
   */
  public void addDataProvider(MessageDataProvider provider) {
    dataProviders.add(provider);
  }

  /**
   * Gets the message tokens.
   *
   * @return the message tokens
   * @throws SQLException
   *           the SQL exception
   */
  public Map<String, List> getMessageTokens() throws SQLException {
    Map<String, List> tokenMap = new LinkedHashMap<String, List>();
    for (MessageDataProvider provider : getMessageDataProviders()) {
      tokenMap.put(provider.getName(), provider.getTokens());
    }
    return tokenMap;
  }

  /**
   * Gets the message tokens.
   *
   * @param providerName
   *          the provider name
   * @return the message tokens
   * @throws SQLException
   *           the SQL exception
   */
  public Map<String, List> getMessageTokens(String providerName) throws SQLException {
    Map<String, List> tokenMap = new LinkedHashMap<String, List>();
    for (MessageDataProvider provider : getMessageDataProviders()) {
      if (provider.getName().equalsIgnoreCase(providerName)) {
        tokenMap.put(provider.getName(), provider.getTokens());
      }
    }
    return tokenMap;
  }

  /**
   * Sets the recipient filter.
   *
   * @param recipientFilter
   *          the recipient filter
   */
  public void setRecipientFilter(Map<String, Set<String>> recipientFilter) {
    this.recipientFilter = recipientFilter;
  }

  /**
   * The Class MessageTemplateMatcher.
   */
  // A custom Template matcher that can handle empty values in the data map
  public class MessageTemplateMatcher extends TemplateMatcher {

    /**
     * Instantiates a new message template matcher.
     *
     * @param leftBrace
     *          the left brace
     * @param rightBrace
     *          the right brace
     */
    public MessageTemplateMatcher(String leftBrace, String rightBrace) {
      super(leftBrace, rightBrace);
    }

    /**
     * Instantiates a new message template matcher.
     *
     * @param prefix
     *          the prefix
     */
    public MessageTemplateMatcher(String prefix) {
      super(prefix);
    }

    /*
     * (non-Javadoc)
     * 
     * @see jlibs.core.util.regex.TemplateMatcher#replace(java.lang.String, java.util.Map)
     */
    public String replace(String input, final Map<String, String> variables) {
      return replace(input, new MessageTokenResolver(variables));
    }
  }

  /**
   * The Class MessageTokenResolver.
   */
  // A custom token resolver that can handle empty values in the data map
  public static class MessageTokenResolver implements VariableResolver {

    /** The variables. */
    private Map<String, String> variables;

    /** The ignore nulls. */
    private boolean ignoreNulls = false;

    /**
     * Instantiates a new message token resolver.
     *
     * @param variables
     *          the variables
     */
    public MessageTokenResolver(Map<String, String> variables) {
      this(variables, false);
    }

    /**
     * Instantiates a new message token resolver.
     *
     * @param variables
     *          the variables
     * @param ignoreNulls
     *          the ignore nulls
     */
    public MessageTokenResolver(Map<String, String> variables, boolean ignoreNulls) {
      this.variables = variables;
      this.ignoreNulls = ignoreNulls;
    }

    /*
     * (non-Javadoc)
     * 
     * @see jlibs.core.util.regex.TemplateMatcher.VariableResolver#resolve(java.lang.String)
     */
    @Override
    public String resolve(String variable) {
      String value = null;
      if (null != variables) {
        if (variables.containsKey(variable)) {
          value = variables.get(variable);
          if (null == value && !ignoreNulls) {
            return "";
          }
        }
      }
      return value;
    }
  }

  // Email Id field may contain multiple email ids by giving comma separated.
  // The below function will separate email ids by comma and gives us the email id list. Refer Bug#
  // 45929

  /**
   * Find multiple email I ds from recipient array.
   *
   * @param recipientArray
   *          the recipient array
   * @return the list
   */
  private List<String> findMultipleEmailIDsFromRecipientArray(String[] recipientArray) {
    List<String> recipientList = Collections.EMPTY_LIST;
    if (null != recipientArray && recipientArray.length > 0) {
      recipientList = new ArrayList<String>();
      for (int i = 0; i < recipientArray.length; i++) {
        if (null != recipientArray[i] && !recipientArray[i].equals("")) {
          recipientList.addAll(Arrays.asList(recipientArray[i].split(",")));
        }
      }
    }
    return recipientList;
  }

  /**
   * Controls for which recipient keys(Ex: reportIds, PatientIds, AppointmentIds), messages are to
   * be built.
   *
   * @param context
   *          the Message context
   * @return true, if Messages can be built
   * @throws SQLException
   *           the SQL exception
   * @see DiagReportMessageBuilder#preProcess(MessageContext) for the usage
   */
  public boolean preProcess(MessageContext context) throws SQLException {
    if (context == null || context.getEventData() == null ) {
      return true;
    }
    String messageMode = (String) context.getMessageType().get("message_mode");
    if (messageMode.equalsIgnoreCase("HL7_SOCKET_ADT") 
          || messageMode.equalsIgnoreCase("NOTIFICATION")) {
      return true;
    }
    if (context.getMessageType().get("recipient_category") != null
        && !"Patient".equals((String) context.getMessageType().get("recipient_category"))) {
      return true;
    }
    String mrNo = null;
    if (context.getEventData().containsKey("mr_no") 
        && context.getEventData().get("mr_no") != null
        && !"".equals((String) context.getEventData().get("mr_no"))) {
      mrNo = (String) context.getEventData().get("mr_no");
    } else if (context.getEventData().containsKey("patient_id")
        || context.getEventData().containsKey("visit_id")) {
      String patientId = context.getEventData().containsKey("patient_id")
          ? (String) context.getEventData().get("patient_id")
          : (String) context.getEventData().get("visit_id");
      mrNo = PatientDetailsDAO.getMrForVisit(patientId);
    } else if (context.getEventData().containsKey("appointment_id")
        && null != context.getEventData().get("appointment_id")) {
      Object appointmentIdObj = context.getEventData().get("appointment_id");
      if (appointmentIdObj instanceof String) {
        mrNo = PatientDetailsDAO
            .getMrForAppointmentId(Integer.parseInt((String) context.getEventData()
                .get("appointment_id")));
      } else {
        mrNo = PatientDetailsDAO
            .getMrForAppointmentId((Integer) context.getEventData().get("appointment_id"));
      }
    }
    if (mrNo != null && !mrNo.equals("")) {
      GenericDAO contactPreferenceDao = new GenericDAO("contact_preferences");
      BasicDynaBean contactPreferenceBean = contactPreferenceDao.findByKey("mr_no", mrNo);

      String messageGroupName = (String) context.getMessageType().get("message_group_name");
      GenericDAO messageCategoryDao = new GenericDAO("message_category");
      BasicDynaBean messageCategoryBean = messageCategoryDao.findByKey("message_category_id",
          (Integer) context.getMessageType().get("category_id"));
      String messageCategory = (String) messageCategoryBean.get("message_category_name");
      if (messageCategory != null 
          && (messageCategory.equals("Custom Promotional")
          || messageCategory.equals("Promotional"))) {
        String promotionalConsent = contactPreferenceBean != null 
            && contactPreferenceBean.get("promotional_consent") != null 
            ?  (String) contactPreferenceBean.get("promotional_consent") : "N";
        if (promotionalConsent.equals("N")) {
          return false;
        }
      }
      String contactPreference = contactPreferenceBean != null
          && contactPreferenceBean.get("receive_communication") != null
          ? (String) contactPreferenceBean.get("receive_communication") : "B";
      String patientCommPreference = getPatientCommPreference(mrNo, messageGroupName);
      String preferenceDecision = (patientCommPreference != null
          && !patientCommPreference.equals("")) ? patientCommPreference
              : ((contactPreference != null && !contactPreference.equals("")) ? contactPreference
                  : "B");
      if (preferenceDecision.equals("N")) {
        return false;
      }
      if ((messageMode.equals("SMS") && preferenceDecision.equals("E"))
          || (messageMode.equals("EMAIL") && preferenceDecision.equals("S"))) {
        return false;
      }
    }
    return true;
  }

  private static final String GET_PATIENT_COMM_PREF = "SELECT communication_type FROM "
      + " patient_communication_preferences WHERE mr_no = ? AND message_group_name = ?";

  /**
   * Gets the patient comm preference.
   *
   * @param mrNo the mr no
   * @param messageGroupName the message group name
   * @return the patient comm preference
   * @throws SQLException the SQL exception
   */
  public static String getPatientCommPreference(String mrNo, String messageGroupName)
      throws SQLException {
    PreparedStatement ps = null;
    Connection con = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_PATIENT_COMM_PREF);
      ps.setString(1, mrNo);
      ps.setString(2, messageGroupName);
      return DataBaseUtil.getStringValueFromDb(ps);
    } finally {
      if (ps != null) {
        ps.close();
      }
      if (con != null) {
        con.close();
      }
    }
  }

  /** The Constant GET_MESSAGE_LOG_DETAILS. */
  private static final String GET_MESSAGE_LOG_DETAILS = "SELECT * FROM message_log "
      + " WHERE last_status != 'F' AND entity_id = ? AND message_type_id = ?";

  /**
   * Checks if is message sent.
   *
   * @param entityId
   *          the entity id
   * @param messageTypeId
   *          the message type id
   * @return true, if is message sent
   * @throws SQLException
   *           the SQL exception
   */
  public static boolean isMessageSent(String entityId, String messageTypeId) throws SQLException {
    PreparedStatement ps = null;
    Connection con = null;
    List<BasicDynaBean> messageloglist = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_MESSAGE_LOG_DETAILS);
      ps.setString(1, entityId);
      ps.setString(2, messageTypeId);
      messageloglist = DataBaseUtil.queryToDynaList(ps);
    } finally {
      if (ps != null) {
        ps.close();
      }
      if (con != null) {
        con.close();
      }
    }

    return (messageloglist != null && messageloglist.size() > 0);
  }

  public List<?> getMessageDetailsList(String messageType, String userId, Integer pageNum,
      Integer pageSize) {
    return Collections.emptyList();
  }

}
