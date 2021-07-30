package com.insta.hms.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * The Class Message.
 */
public class Message implements Serializable {

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(Message.class);

  /** The sender. */
  private String sender = null;

  /** The subject. */
  private String subject = null;

  /** The message body. */
  private String messageBody = null;

  /** The message type. */
  private String messageType = null;

  /** The message log id. */
  private int messageLogId;

  /** The recipient list. */
  private List<String> recipientList = new ArrayList<String>();

  /** The cc recipient list. */
  private List<String> ccRecipientList = new ArrayList<String>();

  /** The bcc recipient list. */
  private List<String> bccRecipientList = new ArrayList<String>();

  /** The attachment list. */
  private Map<String, EmailAttachment> attachmentList = new Hashtable<String, EmailAttachment>();

  /** The Constant RECIPIENT_TO. */
  public static final String RECIPIENT_TO = "to";

  /** The Constant RECIPIENT_CC. */
  public static final String RECIPIENT_CC = "cc";

  /** The Constant RECIPIENT_BCC. */
  public static final String RECIPIENT_BCC = "bcc";

  /** The recipient id. */
  private String recipientId = null;

  /** The recipient type. */
  private String recipientType = null;

  /** The entity id. */
  private String entityId = null;

  /** The batch id. */
  private int batchId;

  private String errorMsg;

  public String getErrorMsg() {
    return errorMsg;
  }

  public void setErrorMsg(String errorMsg) {
    this.errorMsg = errorMsg;
  }

  private ca.uhn.hl7v2.model.Message hl7ModelMessage;

  public ca.uhn.hl7v2.model.Message getHl7ModelMessage() {
    return hl7ModelMessage;
  }

  public void setHl7ModelMessage(ca.uhn.hl7v2.model.Message hl7ModelMessage) {
    this.hl7ModelMessage = hl7ModelMessage;
  }

  /**
   * Sets the sender.
   *
   * @param sender the new sender
   */
  public void setSender(String sender) {
    this.sender = sender;
  }

  /**
   * Sets the message type.
   *
   * @param messageType the new message type
   */
  public void setMessageType(String messageType) {
    this.messageType = messageType;
  }

  /**
   * Gets the message type.
   *
   * @return the message type
   */
  public String getMessageType() {
    return messageType;
  }

  /**
   * Sets the subject.
   *
   * @param subject the new subject
   */
  public void setSubject(String subject) {
    this.subject = subject;
  }

  /**
   * Sets the body.
   *
   * @param messageBody the new body
   */
  public void setBody(String messageBody) {
    this.messageBody = messageBody;
  }

  /**
   * Gets the body.
   *
   * @return the body
   */
  public String getBody() {
    return messageBody;
  }

  /**
   * Gets the sender.
   *
   * @return the sender
   */
  public String getSender() {
    return sender;
  }

  /**
   * Gets the subject.
   *
   * @return the subject
   */
  public String getSubject() {
    return subject;
  }

  /**
   * Adds the recipients.
   *
   * @param recipients the recipients
   */
  public void addRecipients(String[] recipients) {
    addReceipients(recipients, RECIPIENT_TO);
  }

  /**
   * Adds the receipients.
   *
   * @param recipients the recipients
   * @param recipientType the recipient type
   */
  public void addReceipients(String[] recipients, String recipientType) {
    if (null != recipients) {
      if (RECIPIENT_CC.equalsIgnoreCase(recipientType)) {
        for (String recipient : recipients) {
          ccRecipientList.add(recipient);
        }
      } else if (RECIPIENT_BCC.equalsIgnoreCase(recipientType)) {
        for (String recipient : recipients) {
          bccRecipientList.add(recipient);
        }
      } else {
        for (String recipient : recipients) {
          recipientList.add(recipient);
        }
      }
    }
  }

  /**
   * Gets the receipients.
   *
   * @return the receipients
   */
  public List<String> getReceipients() {
    return getReceipients(RECIPIENT_TO);
  }

  /**
   * Gets the receipients.
   *
   * @param recipientType the recipient type
   * @return the receipients
   */
  public List<String> getReceipients(String recipientType) {
    if (RECIPIENT_CC.equalsIgnoreCase(recipientType)) {
      return ccRecipientList;
    } else if (RECIPIENT_BCC.equalsIgnoreCase(recipientType)) {
      return bccRecipientList;
    } else {
      return recipientList;
    }
  }

  /**
   * Clear recipients.
   */
  public void clearRecipients() {
    clearRecipients(RECIPIENT_TO);
  }

  /**
   * Clear recipients.
   *
   * @param recipientType the recipient type
   */
  public void clearRecipients(String recipientType) {
    if (RECIPIENT_CC.equalsIgnoreCase(recipientType)) {
      ccRecipientList.clear();
    } else if (RECIPIENT_BCC.equalsIgnoreCase(recipientType)) {
      bccRecipientList.clear();
    } else {
      recipientList.clear();
    }
  }

  /**
   * Clear recipients.
   *
   * @param recipientType the recipient type
   * @param recipient the recipient
   */
  public void clearRecipients(String recipientType, String recipient) {
    if (RECIPIENT_CC.equalsIgnoreCase(recipientType)) {
      ccRecipientList.remove(recipient);
    } else if (RECIPIENT_BCC.equalsIgnoreCase(recipientType)) {
      bccRecipientList.remove(recipient);
    } else {
      recipientList.remove(recipient);
    }
  }

  /**
   * Adds the attachment.
   *
   * @param attachmentName the attachment name
   * @param attachmentType the attachment type
   * @param attachmentBytes the attachment bytes
   */
  public void addAttachment(String attachmentName, String attachmentType, byte[] attachmentBytes) {
    if (null != attachmentName && null != attachmentBytes) {
      EmailAttachment attachment =
          new EmailAttachment(attachmentName, attachmentType, attachmentBytes);
      attachmentList.put(attachmentName, attachment);
    }
  }

  /**
   * Adds the attachment.
   *
   * @param attachmentName the attachment name
   * @param attachmentBytes the attachment bytes
   */
  public void addAttachment(String attachmentName, byte[] attachmentBytes) {
    addAttachment(attachmentName, "", attachmentBytes);
  }

  /**
   * Gets the attachment.
   *
   * @param attachmentName the attachment name
   * @return the attachment
   */
  public EmailAttachment getAttachment(String attachmentName) {
    return attachmentList.get(attachmentName);
  }

  /**
   * Gets the all attachments.
   *
   * @return the all attachments
   */
  public List<EmailAttachment> getAllAttachments() {
    return new ArrayList<EmailAttachment>(attachmentList.values());
  }

  /**
   * Removes the attachment.
   *
   * @param attachmentName the attachment name
   */
  public void removeAttachment(String attachmentName) {
    attachmentList.remove(attachmentName);
  }

  /**
   * Gets the recipient array.
   *
   * @param recipientType the recipient type
   * @return the recipient array
   */
  public String[] getRecipientArray(String recipientType) {
    List<String> recipients = getReceipients(recipientType);
    if (null != recipients) {
      return recipients.toArray(new String[recipients.size()]);
    }
    return null;
  }

  /**
   * Gets the recipient string.
   *
   * @param recipientType the recipient type
   * @return the recipient string
   */
  public String getRecipientString(String recipientType) {
    List<String> recipients = getReceipients(recipientType);
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < recipients.size(); i++) {
      if (i != 0) {
        builder.append(",");
      }
      builder.append(recipients.get(i));
    }
    return builder.toString();
  }

  /**
   * From template.
   *
   * @param template the template
   * @return the message
   */
  public static Message fromTemplate(Map template) {

    Message msg = new Message();

    msg.setBody((String) template.get("message_body"));
    msg.setSender((String) template.get("message_sender"));
    msg.setSubject((String) template.get("message_subject"));

    String recipients = (String) template.get("message_to");
    if (null != recipients) {
      msg.addRecipients(recipients.split(","));
    }

    String ccRecipients = (String) template.get("message_cc");
    if (null != ccRecipients) {
      msg.addReceipients(ccRecipients.split(","), Message.RECIPIENT_CC);
    }

    String bccRecipients = (String) template.get("message_bcc");
    if (null != bccRecipients) {
      msg.addReceipients(bccRecipients.split(","), Message.RECIPIENT_BCC);
    }

    List<Map> attachmentList = (List<Map>) template.get("message_attachments");
    if (null != attachmentList) {
      for (Map attachment : attachmentList) {
        msg.addAttachment((String) attachment.get("attachment_name"),
            (String) attachment.get("attachment_type"), 
            (byte[]) attachment.get("attachment_bytes"));
      }
    }
    msg.setMessageType((String) template.get("message_type_id"));
    msg.setEntityId(String.valueOf(template.get("entity_id")));
    return msg;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "Sender : " + this.getSender() + " Subject : " + this.getSubject()
        + " Recipients (to) : " + this.getRecipientString(RECIPIENT_TO) + " Recipients (cc) : "
        + this.getRecipientString(RECIPIENT_CC) + " Recipients (bcc) : "
        + this.getRecipientString(RECIPIENT_BCC) + " Body : " + this.getBody();
  }

  /**
   * Gets the receipient id.
   *
   * @return the receipient id
   */
  public String getReceipient_id__() {
    return recipientId;
  }

  /**
   * Sets the receipient id.
   *
   * @param recipientId the new receipient id
   */
  public void setReceipient_id__(String recipientId) {
    this.recipientId = recipientId;
  }

  /**
   * Gets the receipient type.
   *
   * @return the receipient type
   */
  public String getReceipient_type__() {
    return recipientType;
  }

  /**
   * Sets the receipient type.
   *
   * @param recipientType the new receipient type
   */
  public void setReceipient_type__(String recipientType) {
    this.recipientType = recipientType;
  }

  /**
   * Gets the entity id.
   *
   * @return the entity id
   */
  public String getEntityId() {
    return entityId;
  }

  /**
   * Sets the entity id.
   *
   * @param entityId the new entity id
   */
  public void setEntityId(String entityId) {
    this.entityId = entityId;
  }

  /**
   * Gets the batch id.
   *
   * @return the batch id
   */
  public int getBatchId() {
    return batchId;
  }

  /**
   * Sets the batch id.
   *
   * @param batchId the new batch id
   */
  public void setBatchId(int batchId) {
    this.batchId = batchId;
  }

  /**
   * Gets the message log id.
   *
   * @return the message log id
   */
  public int getMessageLogId() {
    return messageLogId;
  }

  /**
   * Sets the message log id.
   *
   * @param messageLogId the new message log id
   */
  public void setMessageLogId(int messageLogId) {
    this.messageLogId = messageLogId;
  }
}
