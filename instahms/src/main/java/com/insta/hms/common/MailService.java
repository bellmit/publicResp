package com.insta.hms.common;

import org.apache.commons.beanutils.DynaBean;

import java.sql.SQLException;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * The Class MailService.
 */
public class MailService {

  /** The auth required. */
  private boolean authRequired = true;

  /** The username. */
  private String username;

  /** The password. */
  private String password;

  /** The mail properties. */
  private Properties mailProperties;

  /**
   * Instantiates a new mail service.
   *
   * @throws SQLException the SQL exception
   */
  public MailService() throws SQLException {
    GenericDAO dao = new GenericDAO("generic_preferences");
    DynaBean record = dao.getRecord();

    mailProperties = new Properties();
    mailProperties.put("mail.smtp.host", record.get("host_name"));
    mailProperties.put("mail.smtp.port", record.get("port_no"));

    /*
     * TODO: expose via prefs props.put("mail.smtp.starttls.enable", "true");
     */

    authRequired = Boolean.parseBoolean(record.get("auth_required").toString());

    username = (String) record.get("username");
    password = (String) record.get("password");

  }

  /**
   * Send a mail to the specified recipients.
   *
   * @param from       The address from which the mail is sent
   * @param recipients List of recipients.
   * @param subject    The subject of the mail
   * @param message    The mail body
   * @throws AddressException   Thrown if the specified address in from or recipients are invalid
   * @throws MessagingException the messaging exception
   */
  public void sendMail(String from, String[] recipients, String subject, String message)
      throws AddressException, MessagingException {

    Session session = Session.getInstance(mailProperties);
    MimeMessage msg = new MimeMessage(session);

    // set the from and to address
    InternetAddress addressFrom = new InternetAddress(from);
    msg.setFrom(addressFrom);

    InternetAddress[] addressTo = new InternetAddress[recipients.length];
    for (int i = 0; i < recipients.length; i++) {
      addressTo[i] = new InternetAddress(recipients[i]);
    }
    msg.setRecipients(Message.RecipientType.TO, addressTo);

    msg.setSubject(subject);
    msg.setContent(message, "text/html");

    Transport transport = session.getTransport("smtp");
    try {
      if (authRequired) {
        transport.connect(username, password);
      } else {
        transport.connect();
      }
      transport.sendMessage(msg, msg.getAllRecipients());
    } finally {
      transport.close();
    }

  }

}
