package com.insta.hms.scheduledreport;

import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.utils.EnvironmentUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

/**
 * @author krishna.t
 *
 */
public class SendMail {

  private static final Logger logger = LoggerFactory.getLogger(SendMail.class);

  /**
   * Send mail.
   *
   * @param datasource
   *          the datasource
   * @param from
   *          the from
   * @param templateSubject
   *          the template subject
   * @param templateMessage
   *          the template message
   * @return the string
   */
  public static String sendMail(EmailAttachmentDataSource datasource, String from,
      String templateSubject, String templateMessage) {

    String result = null;
    try {

      logger.info(new java.sql.Timestamp(new java.util.Date().getTime()) + " : "
          + datasource.getName() + " : sending mail..");
      GenericDAO dao = new GenericDAO("message_dispatcher_config");
      Map dispatcherConfig = dao.findByKey("message_mode", "EMAIL").getMap();

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
      Session session = Session.getInstance(props);
      Transport bus = session.getTransport("smtp");
      if (authRequired) {
        bus.connect((String) dispatcherConfig.get("host_name"),
            (String) dispatcherConfig.get("username"), (String) dispatcherConfig.get("password"));
      } else {
        bus.connect();
      }

      Message msg = new MimeMessage(session);

      msg.setFrom(new InternetAddress(from));
      msg.setSubject(templateSubject);
      msg.setSentDate(new Date());
      MimeBodyPart p1 = new MimeBodyPart();

      String[] strToAddresses = datasource.getMailIds();
      InternetAddress[] address = new InternetAddress[strToAddresses.length];
      for (int i = 0; i < strToAddresses.length; i++) {
        if (strToAddresses[i] != null) {
          address[i] = new InternetAddress(strToAddresses[i]);
        }
      }
      msg.setRecipients(Message.RecipientType.TO, address);
      p1.setContent(templateMessage, "text/html");
      Multipart mp = new MimeMultipart();
      mp.addBodyPart(p1);
      MimeBodyPart p2 = new MimeBodyPart();
      p2.setDataHandler(new DataHandler(datasource));
      p2.setFileName(datasource.getName());
      mp.addBodyPart(p2);
      msg.setContent(mp);
      bus.sendMessage(msg, address);

      result = "success";

    } catch (SendFailedException sfe) {
      result = "mail send failed";
      logger.error(new java.sql.Timestamp(new java.util.Date().getTime())
          + " : mail send failed :"
          + sfe.getMessage());
      sfe.printStackTrace();
    } catch (MessagingException me) {
      result = "mail send failed";
      logger.error(new java.sql.Timestamp(new java.util.Date().getTime())
          + " : mail send failed :"
          + me.getMessage());
      me.printStackTrace();
    } catch (SQLException sqlException) {
      logger.error("Error getting email message dispatcher config", sqlException);
    }
    return result;
  }
}
