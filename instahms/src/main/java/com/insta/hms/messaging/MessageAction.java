package com.insta.hms.messaging;

import com.bob.hms.adminmasters.organization.OrgMasterDao;
import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.Registration.PatientDetailsDAO;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.master.AreaMaster.AreaMasterDAO;
import com.insta.hms.master.CityMaster.CityMasterDAO;
import com.insta.hms.master.CountryMaster.CountryMasterDAO;
import com.insta.hms.master.DoctorMaster.DoctorMasterDAO;
import com.insta.hms.master.PatientCategory.PatientCategoryDAO;
import com.insta.hms.master.RegistrationPreferences.RegistrationPreferencesDAO;
import com.insta.hms.master.StateMaster.StateMasterDAO;
import com.insta.hms.master.TpaMaster.TpaMasterDAO;
import com.insta.hms.messaging.providers.MessageDataProvider;
import com.insta.hms.modules.ModulesDAO;
import com.insta.hms.usermanager.UserDashBoardDAO;
import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;
import org.apache.struts.upload.FormFile;
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
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The Class MessageAction.
 */
public class MessageAction extends DispatchAction {

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(MessageAction.class);
  
  private static final GenericDAO messageLogAttachmentsDAO =
      new GenericDAO("message_log_attachments");
  private static final GenericDAO messageTypesDAO = new GenericDAO("message_types");
  private static final GenericDAO messageLogDAO = new GenericDAO("message_log");
  private static final GenericDAO customList1Master = new GenericDAO("custom_list1_master");
  private static final GenericDAO customList2Master = new GenericDAO("custom_list2_master");
  private static final GenericDAO customList3Master = new GenericDAO("custom_list3_master");
  private static final GenericDAO customList4Master = new GenericDAO("custom_list4_master");
  private static final GenericDAO customList5Master = new GenericDAO("custom_list5_master");
  private static final GenericDAO customList6Master = new GenericDAO("custom_list6_master");
  private static final GenericDAO customList7Master = new GenericDAO("custom_list7_master");
  private static final GenericDAO customList8Master = new GenericDAO("custom_list8_master");
  private static final GenericDAO customList9Master = new GenericDAO("custom_list9_master");

  /** The Constant DEFAULT_PROVIDER. */
  private static final String DEFAULT_PROVIDER = "Patient_Visits";

  /**
   * Select message type.
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
  public ActionForward selectMessageType(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws Exception {

    logger.debug("selectMessageType....");

    List<String> columns = new ArrayList<String>();
    columns.addAll(messageTypesDAO.getBean().getMap().keySet());

    List<BasicDynaBean> messageTypes = messageTypesDAO.listAll(columns, "status", "A");

    String messageTypeListJson = "";

    if (null != messageTypes && messageTypes.size() > 0) {
      messageTypeListJson = new JSONSerializer().exclude("*.class")
          .serialize(ConversionUtils.listBeanToListMap(messageTypes));
    }

    request.setAttribute("messageTypeList", messageTypeListJson);

    Map providerMap = new HashMap();
    for (BasicDynaBean bean : messageTypes) {
      String messageTypeId = (String) bean.get("message_type_id");
      List<MessageDataProvider> providerList = getProviderList(messageTypeId);
      providerMap.put(messageTypeId, providerList);
    }
    String[] excludeList = new String[] { "*.class", "*.tokens" };
    String providerMapJson = new JSONSerializer().exclude(excludeList).deepSerialize(providerMap);
    request.setAttribute("providerMap", providerMapJson);
    if (null != form) {
      // We are starting a fresh messaging session. Reset the form.
      ((MessageForm) form).resetAll();
    }
    return mapping.findForward("showMessageTypes");
  }

  /**
   * Save message type.
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
  public ActionForward saveMessageType(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws Exception {
    Connection con = null;
    String messageTypeId = request.getParameter("message_type_id");
    String messageMode = request.getParameter("message_mode");
    String providerName = request.getParameter("provider_name");
    logger
        .debug("saveMessageType...." + messageTypeId + " : " + messageMode + " : " + providerName);

    BasicDynaBean typeBean = messageTypesDAO.findByKey("message_type_id", messageTypeId);

    BasicDynaBean bean = messageLogDAO.getBean();

    int logId = messageLogDAO.getNextSequence();
    bean.set("message_type_id", messageTypeId);
    bean.set("message_mode", (null != messageMode) ? messageMode : typeBean.get("message_mode"));
    bean.set("last_status", "D"); // draft message
    bean.set("message_log_id", logId);

    bean.set("message_subject", typeBean.get("message_subject"));
    bean.set("message_sender", typeBean.get("message_sender"));
    bean.set("message_to", typeBean.get("message_to"));
    bean.set("message_cc", typeBean.get("message_cc"));
    bean.set("message_bcc", typeBean.get("message_bcc"));
    bean.set("message_body", typeBean.get("message_body"));

    try {
      con = DataBaseUtil.getConnection();
      boolean success = messageLogDAO.insert(con, bean);

      if (success) {
        List<BasicDynaBean> templateAttachments =
            getAttachments("message_attachments", "message_type_id", messageTypeId, true);
        // copy the attachments over to the log
        for (BasicDynaBean templateBean : templateAttachments) {
          addLogAttachment(con, logId, templateBean.getMap());
        }
      }
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }

    ActionRedirect redirect = new ActionRedirect(mapping.findForward("showRecipientList"));
    redirect.addParameter("_currentProvider", providerName);
    redirect.addParameter("message_log_id", logId);
    return redirect;
  }

  /** The Constant FORWARD_SUFFIX. */
  private static final String FORWARD_SUFFIX = "RecipientList";

  /** The Constant DEFAULT_FORWARD. */
  private static final String DEFAULT_FORWARD = "DefaultRecipientList";

  /**
   * Show recipients.
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
  public ActionForward showRecipients(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws Exception {

    MessageForm msgForm = (MessageForm) form;
    String messageTypeId = request.getParameter("message_type_id");
    String providerName = request.getParameter("_currentProvider");
    String messageLogId = request.getParameter("message_log_id");
    Map searchFilter = null;
    BasicDynaBean bean = null;

    logger.debug("showRecipients...." + messageTypeId + " : " + providerName);

    if (null != msgForm) {
      if (null != msgForm.getMessage_type_id()) {
        messageTypeId = msgForm.getMessage_type_id();
      }
      if (null != providerName && providerName.trim().length() > 0) {
        msgForm.saveRecipients(providerName);
      }
      if (null != msgForm.getMessage_log_id()) {
        messageLogId = msgForm.getMessage_log_id();
      }

      if (null != messageLogId && !"".equals(messageLogId)) {
        Integer logId = new Integer(messageLogId);
        bean = messageLogDAO.findByKey("message_log_id", logId);
      }
      searchFilter = msgForm.getSearchFilter();
    }

    if (null == providerName || providerName.trim().length() == 0) {
      providerName = DEFAULT_PROVIDER;
    }

    List<MessageDataProvider> providerList = getProviderList(messageTypeId);
    if (null == providerList || providerList.size() == 0) {
      return mapping.findForward("modifyTemplate");
    }

    List<String> providerNames = new ArrayList<String>();
    // TODO : we dont need a list here.
    providerNames.add(providerName);

    MessageDataProvider currentProvider = getProvider(providerList, providerName);

    if (null != currentProvider) {
      Map listingParams = ConversionUtils.getListingParameter(request.getParameterMap());
      MessageContext ctx = MessageContext.fromType(messageTypeId);
      ctx.setDisplayParams(listingParams);

      if (null != searchFilter && searchFilter.size() > 0) {
        ctx.setEventData(searchFilter);
      }

      request.setAttribute("_currentProvider", currentProvider.getName());
      request.setAttribute("messageDataList", currentProvider.getMessageDataList(ctx));
      request.setAttribute("providerList", providerNames);
      request.setAttribute("tokenList", currentProvider.getTokens());
      // Set the paging information. Since we are not using the PagedList, we need to
      // do this from scratch.
      Map pageInfo = new HashMap();
      pageInfo.put("totalRecords", ctx.get("totalRecords"));
      pageInfo.put("numPages", ctx.get("numPages"));
      pageInfo.put("pageNumber", ctx.get("pageNumber"));

      Set<String> currentSelections = msgForm.getRecipients(providerName);
      request.setAttribute("pagingInfo", pageInfo);
      request.setAttribute("currentSelections", currentSelections);
      request.setAttribute("_select_all", msgForm.getAllSelected(providerName));
      request.setAttribute("messageLog", bean);
    }

    setupLookupData(currentProvider.getName(), request);

    // Forward to recipient listing page, if a provider specific page is mapped in
    // struts-config.xml. Else Forward to the default listing page.

    String providerListPage = null;
    if ((currentProvider != null && currentProvider.getName() != null
        && currentProvider.getName().trim().length() > 0)) {
      providerListPage = currentProvider.getName() + FORWARD_SUFFIX;
    }

    if (null != providerListPage && null != mapping.findForward(providerListPage)) {
      return mapping.findForward(providerListPage);
    } else {
      return mapping.findForward(DEFAULT_FORWARD);
    }
  }

  /**
   * Search recipients.
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
  public ActionForward searchRecipients(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws Exception {

    MessageForm msgForm = (MessageForm) form;
    String messageTypeId = request.getParameter("message_type_id");
    String providerName = request.getParameter("_currentProvider");
    String messageLogId = request.getParameter("message_log_id");
    logger.debug("search filter :" + request.getParameterMap());
    if (null != msgForm) {
      if (null != msgForm.getMessage_type_id()) {
        messageTypeId = msgForm.getMessage_type_id();
      }
      if (null != msgForm.getMessage_log_id()) {
        messageLogId = msgForm.getMessage_log_id();
      }
    }
    logger.debug("searchRecipients...." + messageTypeId + " : " + providerName);

    if (null != msgForm) {
      msgForm.saveSearchFilter(request.getParameterMap());
    }

    return mapping.findForward("showRecipientList");
  }

  /**
   * Save recipients.
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
  public ActionForward saveRecipients(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws Exception {

    MessageForm msgForm = (MessageForm) form;
    String provider = request.getParameter("_currentProvider");
    String nextProvider = request.getParameter("_nextProvider");
    logger.debug("saveRecipients...." + provider + " : " + nextProvider);

    if (null != msgForm) {
      if (null != msgForm.getNextProvider()) {
        nextProvider = msgForm.getNextProvider();
      }

      if (null != provider && provider.trim().length() > 0) {
        msgForm.saveRecipients(provider);
      }

    }

    String target = "showTemplate";
    String param = null;
    if (null != nextProvider && nextProvider.trim().length() > 0) {
      target = "showRecipientList";
      param = nextProvider;
    }

    ActionRedirect redirect = new ActionRedirect(mapping.findForward(target));
    if (null != param) {
      redirect.addParameter("_currentProvider", param);
    }

    return redirect;

  }

  /**
   * Show template.
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
  public ActionForward showTemplate(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws Exception {

    MessageForm msgForm = (MessageForm) form;

    String providerName =
        (null != msgForm) ? msgForm.getProvider_name() : request.getParameter("provider_name");

    String messageLogId =
        (null != msgForm) ? msgForm.getMessage_log_id() : request.getParameter("message_log_id");

    logger.debug("showTemplate...." + providerName + " : " + messageLogId);
    if (null != messageLogId && !messageLogId.isEmpty()) {
      Integer logId = new Integer(messageLogId);

      BasicDynaBean bean = messageLogDAO.findByKey("message_log_id", logId);
      String messageTypeId = (String) bean.get("message_type_id");

      BasicDynaBean typeBean = messageTypesDAO.findByKey("message_type_id", messageTypeId);

      request.setAttribute("bean", bean);
      request.setAttribute("messageTypeName", typeBean.get("message_type_name"));
      request.setAttribute("practoSmsModule",
          new ModulesDAO().findByKey("module_id", "mod_practo_sms"));
      List<BasicDynaBean> attachments =
          getAttachments("message_log_attachments", "message_log_id", logId, false);
      request.setAttribute("attachmentList", attachments);

      // Get the tokens
      MessageManager mgr = new MessageManager();
      Map<String, List> tokenMap = mgr.getMessageTokens(messageTypeId, providerName);
      request.setAttribute("tokenMap", tokenMap);

      // Get the dispatcher config
      List<BasicDynaBean> dispatcherList = getDispatcherList((String) bean.get("message_mode"));

      if (null != dispatcherList && dispatcherList.size() > 0) {
        request.setAttribute("dispatcher", dispatcherList.get(0));
      }

    }
    // Get the list of recipient list for validation
    Map<String, Set<String>> recipientFilter = msgForm.getRecipients();
    Set<String> recipientSet = recipientFilter.get(providerName);
    request.setAttribute("_select_all", msgForm.getAllSelected(providerName));
    request.setAttribute("recipientCount", (null != recipientSet) ? recipientSet.size() : 0);

    return mapping.findForward("modifyTemplate");
  }

  /**
   * Send message.
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
  public ActionForward sendMessage(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws Exception {

    Connection con = null;
    MessageForm msgForm = (MessageForm) form;
    String messageTypeId = null;
    String messageMode = null;
    boolean success = false;

    if (null != msgForm) {
      Map<String, Set<String>> recipientFilter = msgForm.getRecipients();
      logger.debug("sendMessage ....." + "RecipientFilter :" + recipientFilter);

      String messageLogId = msgForm.getMessage_log_id();
      Integer logId = new Integer(messageLogId);
      BasicDynaBean bean = null;

      try {
        con = DataBaseUtil.getConnection();
        bean = messageLogDAO.findByKey(con, "message_log_id", logId);
        messageTypeId = (String) bean.get("message_type_id");
        messageMode = (String) bean.get("message_mode");

        // overwrite the defaults with the parameters that were passed
        List errors = new ArrayList();
        ConversionUtils.copyToDynaBean(request.getParameterMap(), bean, errors);
        // TODO : handle conversion errors if any

        // process attachments
        String[] selectedAttachments = null;
        List<BasicDynaBean> dispatcherList = getDispatcherList(messageMode);
        boolean attachmentAllowed = false;
        if (null != dispatcherList && dispatcherList.size() > 0) {
          attachmentAllowed = "Y".equals(dispatcherList.get(0).get("attachment_allowed"));
        }
        if (attachmentAllowed) {
          selectedAttachments = request.getParameterValues("attached_files");
        }
        removeUnselectedAttachments(con, logId, selectedAttachments);

        // create a new map so that we can add attachments to it

        Map template = new HashMap();
        template.putAll(bean.getMap());

        List<BasicDynaBean> attachments =
            getAttachments("message_log_attachments", "message_log_id", logId, true);
        if (null != attachments && attachments.size() > 0) {
          // TODO : validate attachment size
          for (BasicDynaBean attachment : attachments) {
            Map attachmentMap = new HashMap();
            attachmentMap.put("attachment_name", attachment.get("attachment_name"));
            attachmentMap.put("attachment_type", attachment.get("attachment_type"));
            byte[] bytes =
                DataBaseUtil.readInputStream((InputStream) attachment.get("attachment_bytes"));
            attachmentMap.put("attachment_bytes", bytes);
            List<Map> attachmentList = (List<Map>) template.get("message_attachments");
            if (null == attachmentList) {
              attachmentList = new ArrayList<Map>();
              template.put("message_attachments", attachmentList);
            }
            attachmentList.add(attachmentMap);
          }
        }

        if (null != messageTypeId) {
          MessageBuilder builder = MessageBuilderFactory.getBuilder(messageTypeId);
          MessageContext ctx = MessageContext.fromType(template);
          if (null != builder) {
            String selectedProvider = msgForm.getProvider_name();
            boolean recipientsOk = false;
            if (null != recipientFilter && null != recipientFilter.get(selectedProvider)
                && 0 != recipientFilter.get(selectedProvider).size()) {
              builder.setRecipientFilter(recipientFilter);
              recipientsOk = true;
            }
            if ("true".equalsIgnoreCase(msgForm.get_select_all())) {
              recipientsOk = true;
            }
            if (recipientsOk) {
              builder.build(ctx);
              List<Message> msgs = builder.getMessageList();
              logger.debug("sendMessage.....message list size :" + msgs.size());
              MessageManager mgr = new MessageManager();
              int sentMessages = mgr.sendMessages(messageTypeId, messageMode, msgs);
              success = (msgs.size() == sentMessages);
            } else {
              logger.info("No recipients specified for the message. No message was sent");
            }
          }
        }
      } finally {
        DataBaseUtil.closeConnections(con, null);
      }

      // cleanup the form and the draft messages
      msgForm.resetAll();
      try {
        con = DataBaseUtil.getConnection();
        BasicDynaBean log = messageLogDAO.findByKey(con, "message_log_id", logId);
        // Delete the draft copy that was created.
        if (null != log && "D".equals(log.get("last_status"))) {
          messageLogAttachmentsDAO.delete(con, "message_log_id", logId);
          messageLogDAO.delete(con, "message_log_id", logId);
        }
      } finally {
        DataBaseUtil.closeConnections(con, null);
      }

    }

    ActionRedirect redirect = new ActionRedirect(mapping.findForward("sendRedirect"));
    FlashScope flash = FlashScope.getScope(request);
    if (success) {
      flash.success("Messages delivered successfully");
    } else {
      flash.error("One or more messages could not be delivered. Check the message log for details");
    }
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
    return redirect;
  }

  /**
   * Adds the attachment.
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

  public ActionForward addAttachment(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws Exception {

    logger.debug("addAttachment ...");
    Connection con = null;
    MessageForm msgForm = (MessageForm) form;
    try {
      con = DataBaseUtil.getConnection();
      if (null != msgForm) {
        FormFile attachment = msgForm.getAttachment();
        if (null != attachment) {
          Map attachmentMap = new HashMap();
          attachmentMap.put("attachment_name", attachment.getFileName());
          attachmentMap.put("attachment_type", attachment.getContentType());
          attachmentMap.put("attachment_bytes", attachment.getInputStream());
          String messageLogId = msgForm.getMessage_log_id();
          if (null != messageLogId) {
            int logId = new Integer(messageLogId);
            addLogAttachment(con, logId, attachmentMap);
          }
        }
      }
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }

    // TODO : save the other parameters into the draft message
    return mapping.findForward("showTemplate");
  }

  /**
   * Adds the log attachment.
   *
   * @param con
   *          the con
   * @param logId
   *          the log id
   * @param attachmentMap
   *          the attachment map
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  private boolean addLogAttachment(Connection con, Integer logId, Map attachmentMap)
      throws SQLException, IOException {

    logger.debug("addLogAttachment...." + logId + " : " + attachmentMap);

    if (null == con || null == logId || null == attachmentMap) {
      return false;
    }

    BasicDynaBean logAttachBean = messageLogAttachmentsDAO.getBean();
    int attachmentId = messageLogAttachmentsDAO.getNextSequence();
    logAttachBean.set("attachment_id", attachmentId);
    logAttachBean.set("message_log_id", logId);
    logAttachBean.set("attachment_name", attachmentMap.get("attachment_name"));
    logAttachBean.set("attachment_type", attachmentMap.get("attachment_type"));
    logAttachBean.set("attachment_bytes", attachmentMap.get("attachment_bytes"));
    return messageLogAttachmentsDAO.insert(con, logAttachBean);
  }

  /**
   * Gets the provider list.
   *
   * @param messageTypeId
   *          the message type id
   * @return the provider list
   */
  private List<MessageDataProvider> getProviderList(String messageTypeId) {
    List<MessageDataProvider> providers = null;
    if (null != messageTypeId) {
      MessageBuilder builder = MessageBuilderFactory.getBuilder(messageTypeId);
      if (null != builder) {
        providers = builder.getMessageDataProviders();
      }
    }
    return providers;
  }

  /**
   * Gets the provider.
   *
   * @param providers
   *          the providers
   * @param providerName
   *          the provider name
   * @return the provider
   */
  private MessageDataProvider getProvider(List<MessageDataProvider> providers,
      String providerName) {

    if (null == providers || null == providerName || providerName.trim().length() == 0) {
      return null;
    }

    for (int i = 0; i < providers.size(); i++) {
      if (providers.get(i).getName().equalsIgnoreCase(providerName)) {
        return providers.get(i);
      }
    }

    return null;
  }

  /**
   * Gets the attachments.
   *
   * @param tableName
   *          the table name
   * @param keycolumn
   *          the keycolumn
   * @param identifier
   *          the identifier
   * @param includeByteaData
   *          the include bytea data
   * @return the attachments
   * @throws SQLException
   *           the SQL exception
   */
  private List<BasicDynaBean> getAttachments(String tableName, String keycolumn, Object identifier,
      boolean includeByteaData) throws SQLException {
    logger.debug("getAttachments...." + tableName + " : " + keycolumn + " : " + identifier);

    List<BasicDynaBean> attachList = new ArrayList<BasicDynaBean>();

    if (null == tableName || null == keycolumn || null == identifier
        || tableName.trim().length() == 0 || keycolumn.trim().length() == 0) {
      return attachList; // empty list
    }

    List<String> columns = new ArrayList<String>();
    columns.add("attachment_id");
    columns.add("attachment_name");
    columns.add("attachment_type");

    Map<String, Object> filter = new HashMap<String, Object>();
    filter.put(keycolumn, identifier);

    GenericDAO attachDao = new GenericDAO(tableName);
    List<BasicDynaBean> attachments = attachDao.listAll(columns, filter, null);

    if (!includeByteaData) {
      return attachments;
    }

    List<BasicDynaBean> attachmentDataList = new ArrayList<BasicDynaBean>();
    for (BasicDynaBean attachment : attachments) {
      BasicDynaBean attachBean = attachDao.getBean();
      attachDao.loadByteaRecords(attachBean, "attachment_id", attachment.get("attachment_id"));
      attachmentDataList.add(attachBean);
    }

    return attachmentDataList;
  }

  /**
   * Removes the unselected attachments.
   *
   * @param con
   *          the con
   * @param logId
   *          the log id
   * @param selectedIds
   *          the selected ids
   * @throws SQLException
   *           the SQL exception
   */
  private void removeUnselectedAttachments(Connection con, Integer logId, String[] selectedIds)
      throws SQLException {
    logger.debug("removeUnselectedAttachments...." + logId + " : " + selectedIds);
    List<BasicDynaBean> attachmentList =
        getAttachments("message_log_attachments", "message_log_id", logId, false);
    List<Integer> deleteAttachmentList = new ArrayList<Integer>();
    for (BasicDynaBean logAttachment : attachmentList) {
      Integer attachmentId = (Integer) logAttachment.get("attachment_id");
      boolean found = false;
      if (null != selectedIds) { // case where all template attachments were removed
        for (String selectedId : selectedIds) {
          if (attachmentId.equals(new Integer(selectedId))) {
            found = true;
            break;
          }
        }
      }
      if (!found) {
        deleteAttachmentList.add(attachmentId);
      }
    }
    for (Integer deleteId : deleteAttachmentList) {
      messageLogAttachmentsDAO.delete(con, "attachment_id", deleteId);
    }

  }

  /**
   * Gets the dispatcher list.
   *
   * @param messageMode
   *          the message mode
   * @return the dispatcher list
   * @throws SQLException
   *           the SQL exception
   */
  private List<BasicDynaBean> getDispatcherList(String messageMode) throws SQLException {

    List<String> dispatcherColumns = new ArrayList<String>();
    dispatcherColumns.add("message_mode");
    dispatcherColumns.add("display_name");
    dispatcherColumns.add("attachment_allowed");
    dispatcherColumns.add("max_attachment_kb");

    Map filterMap = new HashMap();

    if (null != messageMode && messageMode.trim().length() > 0) {
      filterMap.put("message_mode", messageMode);
    }
    // filterMap.put("status", "A");

    GenericDAO dispatcherDao = new GenericDAO("message_dispatcher_config");
    List<BasicDynaBean> dispatcherList =
        dispatcherDao.listAll(dispatcherColumns, filterMap, "display_name");
    logger.debug("getDispatcherList....dispatcher" + dispatcherList.size());

    return dispatcherList;
  }

  /**
   * Setup lookup data.
   *
   * @param providerName
   *          the provider name
   * @param request
   *          the request
   * @throws ServletException
   *           the servlet exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws SQLException
   *           the SQL exception
   * @throws ParseException
   *           the parse exception
   */
  private void setupLookupData(String providerName, HttpServletRequest request)
      throws ServletException, IOException, SQLException, ParseException {

    if ("Patient_Visits".equalsIgnoreCase(providerName)) {
      setPatientAttributes(request);
    }
    if ("Patients".equalsIgnoreCase(providerName)) {
      setPatientAttributes(request);
    }
    if (("Users").equalsIgnoreCase(providerName)) {
      setUserAttributes(request);
    }
    if (("Doctors").equalsIgnoreCase(providerName)) {
      setDoctorAttributes(request);
    }
  }

  /**
   * Sets the patient attributes.
   *
   * @param request
   *          the new patient attributes
   * @throws ServletException
   *           the servlet exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws SQLException
   *           the SQL exception
   * @throws ParseException
   *           the parse exception
   */
  private void setPatientAttributes(HttpServletRequest request)
      throws ServletException, IOException, SQLException, ParseException {

    JSONSerializer js = new JSONSerializer().exclude("class");
    AreaMasterDAO areaDao = new AreaMasterDAO();
    CityMasterDAO cityDao = new CityMasterDAO();
    StateMasterDAO stateDao = new StateMasterDAO();

    request.setAttribute("countryList", js.serialize(CountryMasterDAO.getCountryList(true)));
    request.setAttribute("areaList", js.serialize(areaDao.getAreaList(true)));
    request.setAttribute("cityList", js.serialize(CityMasterDAO.getPatientCityList(true)));
    request.setAttribute("stateList", js.serialize(stateDao.getStateList(true)));
    Map customRegFieldsMap = PatientDetailsDAO.getCustomRegFieldsMap();
    request.setAttribute("customRegFieldsMap", customRegFieldsMap);
    request.setAttribute("regPref", RegistrationPreferencesDAO.getRegistrationPreferences());
    request.setAttribute("tpasponsorList",
        js.serialize(ConversionUtils.listBeanToListMap(new TpaMasterDAO().listAll())));
    request.setAttribute("orgNameJSONList",
        js.serialize(ConversionUtils.listBeanToListMap(OrgMasterDao.getOrganizations())));
    request.setAttribute("categoryList",
        js.serialize(ConversionUtils.listBeanToListMap(new PatientCategoryDAO().listAll())));
    request.setAttribute("customList1", js.serialize(
        ConversionUtils.listBeanToListMap(customList1Master.listAll())));
    request.setAttribute("customList2", js.serialize(
        ConversionUtils.listBeanToListMap(customList2Master.listAll())));
    request.setAttribute("customList3", js.serialize(
        ConversionUtils.listBeanToListMap(customList3Master.listAll())));
    request.setAttribute("customList4", js.serialize(
        ConversionUtils.listBeanToListMap(customList4Master.listAll())));
    request.setAttribute("customList5", js.serialize(
        ConversionUtils.listBeanToListMap(customList5Master.listAll())));
    request.setAttribute("customList6", js.serialize(
        ConversionUtils.listBeanToListMap(customList6Master.listAll())));
    request.setAttribute("customList7", js.serialize(
        ConversionUtils.listBeanToListMap(customList7Master.listAll())));
    request.setAttribute("customList8", js.serialize(
        ConversionUtils.listBeanToListMap(customList8Master.listAll())));
    request.setAttribute("customList9", js.serialize(
        ConversionUtils.listBeanToListMap(customList9Master.listAll())));
  }

  /**
   * Sets the user attributes.
   *
   * @param request
   *          the new user attributes
   * @throws ServletException
   *           the servlet exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws SQLException
   *           the SQL exception
   * @throws ParseException
   *           the parse exception
   */
  private void setUserAttributes(HttpServletRequest request)
      throws ServletException, IOException, SQLException, ParseException {

    JSONSerializer js = new JSONSerializer().exclude("class");
    List allRoleNames = UserDashBoardDAO.getAllRoleNames();
    request.setAttribute("allRoleNames", js.serialize(allRoleNames));

    List allUserNames = UserDashBoardDAO.getAllUserNames();
    request.setAttribute("allUserNames", js.serialize(allUserNames));

    return;
  }

  /**
   * Sets the doctor attributes.
   *
   * @param request
   *          the new doctor attributes
   * @throws ServletException
   *           the servlet exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws SQLException
   *           the SQL exception
   * @throws ParseException
   *           the parse exception
   */
  private void setDoctorAttributes(HttpServletRequest request)
      throws ServletException, IOException, SQLException, ParseException {

    DoctorMasterDAO dao = new DoctorMasterDAO();
    JSONSerializer js = new JSONSerializer();
    request.setAttribute("doctorNames", js.serialize(dao.getDoctorName()));

    return;
  }

}
