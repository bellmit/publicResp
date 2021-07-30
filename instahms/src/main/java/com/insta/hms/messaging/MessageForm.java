package com.insta.hms.messaging;

import org.apache.struts.action.ActionForm;
import org.apache.struts.upload.FormFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The Class MessageForm.
 */
public class MessageForm extends ActionForm {

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(MessageForm.class);

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /** The message type id. */
  private String messageTypeId;

  /** The message mode. */
  private String messageMode;

  /** The message sender. */
  private String messageSender;

  /** The message to. */
  private String messageTo;

  /** The message cc. */
  private String messageCc;

  /** The message bcc. */
  private String messageBcc;

  /** The message body. */
  private String messageBody;

  /** The message subject. */
  private String messageSubject;

  /** The provider name. */
  private String providerName;

  /** The attachment. */
  private FormFile attachment;

  /** The next provider. */
  private String nextProvider;

  /** The current provider. */
  private String currentProvider;

  /** The attachment name. */
  private String attachmentName;

  /** The message log id. */
  private String messageLogId;

  /** The select all. */
  private String selectAll;

  /** The selected recipients. */
  private String[] selectedRecipients;

  /** The removed selections. */
  private String removedSelections;

  /** The search filter. */
  private Map searchFilter = new HashMap();

  /** The attachments. */
  private Map<String, FormFile> attachments = new Hashtable<String, FormFile>();

  /** The recipients. */
  private Map<String, Set<String>> recipients = new Hashtable<String, Set<String>>();

  /** The providers. */
  private List<String> providers = new ArrayList<String>();

  /**
   * Clear form.
   */
  private void clearForm() {
    selectedRecipients = null;
    removedSelections = null;
    selectAll = null;
    // _search_filter.clear();
  }

  /**
   * Reset all.
   */
  public void resetAll() {

    messageTypeId = "";
    messageMode = "";
    messageSender = "";
    messageTo = "";
    messageCc = "";
    messageBcc = "";
    messageBody = "";
    messageSubject = "";
    providerName = "";
    nextProvider = "";
    attachmentName = "";
    messageLogId = "";

    currentProvider = "";
    selectAll = "";
    removedSelections = "";

    selectedRecipients = null;
    attachment = null;

    searchFilter.clear();
    recipients.clear();
    providers.clear();
  }

  /** The all selected. */
  private Map<String, Boolean> allSelected = new Hashtable<String, Boolean>();

  /**
   * Gets the select all.
   *
   * @return the select all
   */
  public String get_select_all() {
    return selectAll;
  }

  /**
   * Sets the select all.
   *
   * @param selectAll
   *          the new select all
   */
  public void set_select_all(String selectAll) {
    this.selectAll = selectAll;
  }

  /**
   * Gets the selected recipients.
   *
   * @return the selected recipients
   */
  public String[] get_selected_recipients() {
    return selectedRecipients;
  }

  /**
   * Sets the selected recipients.
   *
   * @param selectedRecipients
   *          the new selected recipients
   */
  public void set_selected_recipients(String[] selectedRecipients) {
    this.selectedRecipients = selectedRecipients;
  }

  /**
   * Gets the message type id.
   *
   * @return the message type id
   */
  /*
   * public String getMessage_mode() { return message_mode; }
   * 
   * public void setMessage_mode(String message_mode) { this.message_mode = message_mode; }
   * 
   */
  public String getMessage_type_id() {
    return messageTypeId;
  }

  /**
   * Sets the message type id.
   *
   * @param messageTypeId
   *          the new message type id
   */
  public void setMessage_type_id(String messageTypeId) {
    this.messageTypeId = messageTypeId;
  }

  /**
   * Gets the current provider.
   *
   * @return the current provider
   */
  public String get_currentProvider() {
    return currentProvider;
  }

  /**
   * Sets the current provider.
   *
   * @param provider
   *          the new current provider
   */
  public void set_currentProvider(String provider) {
    this.currentProvider = provider;

  }

  /**
   * Save recipients.
   *
   * @param provider
   *          the provider
   */
  public void saveRecipients(String provider) {
    logger.info("Saving recipients for " + provider + selectAll);
    if (null == provider || provider.trim().length() == 0) {
      return;
    }

    if (!providers.contains(provider)) {
      providers.add(provider);
    }

    if (null != selectAll) {
      allSelected.put(provider, Boolean.valueOf(selectAll));
    }

    if (Boolean.valueOf(selectAll)) {
      Set recSet = recipients.get(provider);

      if (null != recSet) {
        recSet.clear();
      }
    } else {

      if (null != selectedRecipients && selectedRecipients.length > 0) {
        Set recSet = recipients.get(provider);

        if (null == recSet) {
          recSet = new HashSet<String>();
          recipients.put(provider, recSet);
        }
        if (null != removedSelections) {
          String[] removed = removedSelections.split(",");
          recSet.removeAll(Arrays.asList(removed));
        }
        recSet.addAll(Arrays.asList(selectedRecipients));
      }
    }
    clearForm();
  }

  /**
   * Sets the next provider.
   *
   * @param prevProvider
   *          the new next provider
   */
  public void setNextProvider(String prevProvider) {
    this.nextProvider = prevProvider;
  }

  /**
   * Gets the next provider.
   *
   * @return the next provider
   */
  public String getNextProvider() {
    return nextProvider;
  }

  /**
   * Gets the message bcc.
   *
   * @return the message bcc
   */
  public String getMessage_bcc() {
    return messageBcc;
  }

  /**
   * Sets the message bcc.
   *
   * @param messageBcc
   *          the new message bcc
   */
  public void setMessage_bcc(String messageBcc) {
    this.messageBcc = messageBcc;
  }

  /**
   * Gets the message body.
   *
   * @return the message body
   */
  public String getMessage_body() {
    return messageBody;
  }

  /**
   * Sets the message body.
   *
   * @param messageBody
   *          the new message body
   */
  public void setMessage_body(String messageBody) {
    this.messageBody = messageBody;
  }

  /**
   * Gets the message cc.
   *
   * @return the message cc
   */
  public String getMessage_cc() {
    return messageCc;
  }

  /**
   * Sets the message cc.
   *
   * @param messageCc
   *          the new message cc
   */
  public void setMessage_cc(String messageCc) {
    this.messageCc = messageCc;
  }

  /**
   * Gets the message sender.
   *
   * @return the message sender
   */
  public String getMessage_sender() {
    return messageSender;
  }

  /**
   * Sets the message sender.
   *
   * @param messageSender
   *          the new message sender
   */
  public void setMessage_sender(String messageSender) {
    this.messageSender = messageSender;
  }

  /**
   * Gets the message to.
   *
   * @return the message to
   */
  public String getMessage_to() {
    return messageTo;
  }

  /**
   * Sets the message to.
   *
   * @param messageTo
   *          the new message to
   */
  public void setMessage_to(String messageTo) {
    this.messageTo = messageTo;
  }

  /**
   * Gets the removed selections.
   *
   * @return the removed selections
   */
  public String get_removed_selections() {
    return removedSelections;
  }

  /**
   * Sets the removed selections.
   *
   * @param removedSelections
   *          the new removed selections
   */
  public void set_removed_selections(String removedSelections) {
    this.removedSelections = removedSelections;
  }

  /**
   * Gets the recipients.
   *
   * @return the recipients
   */
  public Map<String, Set<String>> getRecipients() {
    return Collections.unmodifiableMap(recipients);
  }

  /**
   * Gets the recipients.
   *
   * @param providerName
   *          the provider name
   * @return the recipients
   */
  public Set<String> getRecipients(String providerName) {
    if (null != providerName && providerName.trim().length() > 0) {
      return getRecipients().get(providerName);
    }
    return null;
  }

  /**
   * Gets the all selected.
   *
   * @param provider
   *          the provider
   * @return the all selected
   */
  public Boolean getAllSelected(String provider) {
    return allSelected.get(provider);
  }

  /**
   * Gets the message subject.
   *
   * @return the message subject
   */
  public String getMessage_subject() {
    return messageSubject;
  }

  /**
   * Sets the message subject.
   *
   * @param subject
   *          the new message subject
   */
  public void setMessage_subject(String subject) {
    messageSubject = subject;
  }

  /**
   * Gets the attachment.
   *
   * @return the attachment
   */
  public FormFile getAttachment() {
    return attachment;
  }

  /**
   * Sets the attachment.
   *
   * @param attachment
   *          the new attachment
   */
  public void setAttachment(FormFile attachment) {
    this.attachment = attachment;
  }

  /**
   * Gets the provider name.
   *
   * @return the provider name
   */
  public String getProvider_name() {
    return providerName;
  }

  /**
   * Sets the provider name.
   *
   * @param providerName
   *          the new provider name
   */
  public void setProvider_name(String providerName) {
    this.providerName = providerName;
  }

  /**
   * Gets the message log id.
   *
   * @return the message log id
   */
  public String getMessage_log_id() {
    return messageLogId;
  }

  /**
   * Sets the message log id.
   *
   * @param messageLogId
   *          the new message log id
   */
  public void setMessage_log_id(String messageLogId) {
    this.messageLogId = messageLogId;
  }

  /**
   * Save search filter.
   *
   * @param parameterMap
   *          the parameter map
   */
  public void saveSearchFilter(Map parameterMap) {
    searchFilter.clear();
    searchFilter.putAll(parameterMap);
    logger.debug("saved search filter : " + searchFilter.size());
  }

  /**
   * Gets the search filter.
   *
   * @return the search filter
   */
  public Map getSearchFilter() {
    // TODO Auto-generated method stub
    logger.debug("saved search filter : " + searchFilter.size());
    return searchFilter;

  }
}
