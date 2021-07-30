package com.insta.hms.messaging;

import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.utils.EnvironmentUtil;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class GenericDispatcher {

  static Logger logger = LoggerFactory
      .getLogger(GenericDispatcher.class);
  private String type = null;

  public GenericDispatcher(String type) {
    this.type = type;
  }

  protected boolean send(Message message) throws SQLException, MessagingException {
    return send(getConnectionParams(), message);
  }

  protected boolean send(Properties connParameters, Message message) throws MessagingException {
    
    Map config = null;
    try {
      config = getDispatcherConfig();
    } catch (SQLException ex) {
      config = null;
    }

    if (null == config || ((String) config.get("status")).equals("I")) {
      // Dispatcher is inactive
      return false;
    }

    if (connParameters != null) {
      logger.debug("connection parameters :" + connParameters);
      Session session = null;
      if (null != connParameters.get("mail.smtp.auth")) {
        session = Session.getInstance(connParameters, new GenericPasswordAuthenticator(
            (String) config.get("username"), (String) config.get("password")));
      } else {
        session = Session.getInstance(connParameters);
      }

      logger.debug("Session created :" + session);
      if (logger.isDebugEnabled()) {
        session.setDebug(true);
      }
      MimeMessage msg = new MimeMessage(session);

      String sender = message.getSender();
      if (null == sender || sender.trim().length() == 0) {
        sender = (String) config.get("username");
      }

      if (null != sender && sender.trim().length() > 0) {
        // set the from and to address
        InternetAddress addressFrom = new InternetAddress(sender);
        msg.setFrom(addressFrom);
      }

      List<String> recipients = message.getReceipients();
      if (null != recipients && recipients.size() > 0) {
        List<InternetAddress> addressTo = new ArrayList<InternetAddress>();
        for (String recipient : recipients) {
          if (null != recipient && recipient.trim().length() > 0) {
            addressTo.add(new InternetAddress(recipient));
          }
        }
        msg.setRecipients(javax.mail.Message.RecipientType.TO,
            addressTo.toArray(new InternetAddress[addressTo.size()]));
        logger.debug("recipient set : " + addressTo);
      }

      List<String> recipientsCc = message.getReceipients(Message.RECIPIENT_CC);
      if (null != recipientsCc && recipientsCc.size() > 0) {
        List<InternetAddress> addressCc = new ArrayList<InternetAddress>();
        for (String recipientCc : recipientsCc) {
          if (null != recipientCc && recipientCc.trim().length() > 0) {
            addressCc.add(new InternetAddress(recipientCc));
          }
        }
        msg.setRecipients(javax.mail.Message.RecipientType.CC,
            addressCc.toArray(new InternetAddress[addressCc.size()]));
        logger.debug("recipient(cc) set : " + addressCc);
      }

      List<String> recipientsBcc = message.getReceipients(Message.RECIPIENT_BCC);
      if (null != recipientsBcc && recipientsBcc.size() > 0) {
        logger.debug("recipient(bcc) size : " + recipientsBcc.size() + " : " + recipientsBcc);
        List<InternetAddress> addressBcc = new ArrayList<InternetAddress>();
        for (String recipientBcc : recipientsBcc) {
          if (null != recipientBcc && recipientBcc.trim().length() > 0) {
            addressBcc.add(new InternetAddress(recipientBcc));
          }
        }
        msg.setRecipients(javax.mail.Message.RecipientType.BCC,
            addressBcc.toArray(new InternetAddress[addressBcc.size()]));
        logger.debug("recipient(bcc) set : " + addressBcc);
      }

      Address[] recipientsAll = msg.getAllRecipients();
      if (null == recipientsAll || recipientsAll.length == 0) {
        throw new MessagingException("No recipient address");
      }
      msg.setSubject(message.getSubject());
      logger.debug("subject set : " + msg.getSubject());

      List<EmailAttachment> attachments = message.getAllAttachments();
      if (null != attachments && attachments.size() > 0) {
        MimeBodyPart body = new MimeBodyPart();
        body.setContent(message.getBody(), "text/html; charset=UTF-8");
        Multipart mp = new MimeMultipart();
        mp.addBodyPart(body);
        int unnamedAttachments = 0;
        for (EmailAttachment mailAttachment : attachments) {
          MimeBodyPart att = new MimeBodyPart();
          att.setDataHandler(new DataHandler(mailAttachment));
          String attachName = mailAttachment.getName();
          if (null == attachName || attachName.trim().length() == 0) {
            attachName = "attachment_" + (unnamedAttachments++);
          }
          att.setFileName(attachName);
          mp.addBodyPart(att);
        }
        msg.setContent(mp);
      } else if (config.get("message_mode").equals("SMS")) {
        msg.setText(message.getBody(),"UTF-8");
      } else {
        msg.setContent(message.getBody(), "text/html; charset=UTF-8");
      }

      Transport.send(msg);
      return true;
    }

    return false;
  }

  protected Map getDispatcherConfig() throws SQLException {

    GenericDAO dao = new GenericDAO("message_dispatcher_config");
    BasicDynaBean record = dao.findByKey("message_mode", type);
    return record.getMap();
  }

  protected Properties getConnectionParams() throws SQLException {
    Map dispatcherConfig = getDispatcherConfig();
    Properties props = new Properties();
    props.putAll(System.getProperties());
    props.put("mail.smtp.connectiontimeout", 
        EnvironmentUtil.getMessageDispatcherTimeout() * 1000);
    props.put("mail.smtp.timeout", EnvironmentUtil.getMessageDispatcherTimeout() * 1000);
    props.put("mail.smtp.host", dispatcherConfig.get("host_name"));
    props.put("mail.smtp.localhost", dispatcherConfig.get("host_name"));
    if (null != dispatcherConfig.get("port_no")) {
      props.put("mail.smtp.port", dispatcherConfig.get("port_no").toString());
    }
    Boolean authRequired = Boolean.parseBoolean(
        dispatcherConfig.get("auth_required").toString());
    if (authRequired) {
      props.put("mail.smtp.auth", "true");
    } else {
      props.put("mail.smtp.auth", "false");
    }
    Boolean useTls = Boolean.parseBoolean(dispatcherConfig.get("use_tls").toString());
    if (useTls) {
      props.put("mail.smtp.starttls.enable", "true");
    } else {
      props.put("mail.smtp.starttls.enable", "false");
    }
    Boolean useSsl = Boolean.parseBoolean(dispatcherConfig.get("use_ssl").toString());
    if (useSsl) {
      props.put("mail.smtp.ssl.enable", "true");
    } else {
      props.put("mail.smtp.ssl.enable", "false");
    }
    return props;
  }

  private class GenericPasswordAuthenticator extends Authenticator {
    private String username = null;
    private String password = null;

    public GenericPasswordAuthenticator(String username, String password) {
      this.username = username;
      this.password = password;
    }

    public PasswordAuthentication getPasswordAuthentication() {
      return new PasswordAuthentication(username, password);
    }

  }
}
