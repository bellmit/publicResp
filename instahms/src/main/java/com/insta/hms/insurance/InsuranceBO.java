package com.insta.hms.insurance;

import com.bob.hms.common.DateUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.emr.EMRInterface;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDTO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
 * The Class InsuranceBO.
 */
public class InsuranceBO {

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(InsuranceBO.class);

  /** The dao. */
  GenericDAO dao = new GenericDAO("insurance_transaction");

  /**
   * Send to TPA.
   *
   * @param con    the con
   * @param bean   the bean
   * @param strIds the str ids
   * @return true, if successful
   * @throws Exception the exception
   */
  public boolean sendToTPA(Connection con, BasicDynaBean bean, String[] strIds) throws Exception {
    boolean flag = true;
    try {
      GenericPreferencesDTO dto = GenericPreferencesDAO.getGenericPreferences();
      Properties props = System.getProperties();
      props.put("mail.smtp.host", dto.getHostName());
      props.put("mail.smtp.port", dto.getPortNo());
      if (dto.getAuthRequired().equalsIgnoreCase("t")) {
        props.put("mail.smtp.auth", "true");
      } else {
        props.put("mail.smtp.auth", "false");
      }
      if (logger.isDebugEnabled()) {
        props.put("mail.debug", "true");
      }
      Session session = Session.getInstance(props);

      String to = (String) bean.get("email_to");

      final String subject = (String) bean.get("email_subject");
      final String mailMessage = (String) bean.get("email_body");

      String[] strToAddresses = to.split(",");

      Transport bus = session.getTransport("smtp");
      if (dto.getAuthRequired().equalsIgnoreCase("t")) {
        bus.connect(dto.getHostName(), dto.getMailUserName(), dto.getMailPassword());
      } else {
        bus.connect();
      }

      Message msg = new MimeMessage(session);
      msg.setFrom(new InternetAddress(dto.getHospMailID()));

      List toAddresses = new ArrayList();

      InternetAddress[] addressTo = new InternetAddress[strToAddresses.length];
      for (int i = 0; i < strToAddresses.length; i++) {
        if (strToAddresses[i] != null) {
          addressTo[i] = new InternetAddress(strToAddresses[i]);
          toAddresses.add(strToAddresses[i]);
        }
      }

      msg.setRecipients(Message.RecipientType.TO, addressTo);
      InternetAddress[] addressCC = null;
      String cc = (String) bean.get("email_cc");
      if (cc != null && !(cc.equals(""))) {
        String[] strCCAddresses = cc.split(",");
        addressCC = new InternetAddress[strCCAddresses.length];
        for (int i = 0; i < strCCAddresses.length; i++) {
          if (strCCAddresses[i] != null) {
            addressCC[i] = new InternetAddress(strCCAddresses[i]);
            toAddresses.add(strCCAddresses[i]);
          }
        }
        msg.setRecipients(Message.RecipientType.CC, addressCC);
      }

      InternetAddress[] addresses = new InternetAddress[toAddresses.size()];
      for (int index = 0; index < toAddresses.size(); index++) {
        addresses[index] = new InternetAddress((String) toAddresses.get(index));

      }

      msg.setSubject(subject);
      msg.setSentDate(new Date());

      MimeBodyPart p1 = new MimeBodyPart();
      p1.setContent(mailMessage, "text/html");
      // p1.setText(mailMessage);

      Multipart mp = new MimeMultipart();
      mp.addBodyPart(p1);

      int transactionID = dao.getNextSequence();
      // insert into transactions table

      bean.set("transaction_id", transactionID);
      bean.set("datetime", DateUtil.getCurrentTimestamp());
      boolean result = dao.insert(con, bean);

      if (strIds != null && result) {
        for (int i = 0; i < strIds.length; i++) {
          String[] paramValue = strIds[i].split(",");
          if ("InsuranceProvider".equalsIgnoreCase(paramValue[1])) {
            ArrayList al = new ArrayList();
            al = (ArrayList) new InsuranceDocumentsProvider().getPDFBytes(paramValue[0], true,
                bean.get("insurance_id").toString());

            if (al.get(0) != null) {
              // insert into attachements table
              boolean result1 = insertTransactions(con, transactionID, bean, al);

              if (result1) {
                EmailAttachmentDataSource datasource = new EmailAttachmentDataSource();
                datasource.setData((byte[]) al.get(0));
                datasource.setName((String) al.get(1));
                datasource.setContentType((String) al.get(2));

                MimeBodyPart p2 = new MimeBodyPart();
                p2.setDataHandler(new DataHandler(datasource));
                p2.setFileName(datasource.getName());
                mp.addBodyPart(p2);
              } else {
                flag = false;
                break;
              }
            }
          } else {
            for (EMRInterface.Provider provider : EMRInterface.Provider.values()) {
              if (provider.getProviderName().equals(paramValue[1])) {
                byte[] pdfBytes = provider.getProviderImpl().getPDFBytes(paramValue[0],
                    Integer.parseInt(paramValue[3]));

                if (pdfBytes != null) {
                  ArrayList emral = new ArrayList();
                  emral.add(pdfBytes);
                  emral.add(paramValue[2]);
                  if (RequestContext.getSession().getAttribute("FileType") != null) {
                    if (RequestContext.getSession().getAttribute("FileType").toString()
                        .equals("RTF")) {
                      emral.add("application/rtf");
                    } else {
                      emral.add("application/pdf");
                    }
                  } else {
                    emral.add("application/pdf");
                  }

                  boolean result1 = insertTransactions(con, transactionID, bean, emral);

                  if (result1) {
                    EmailAttachmentDataSource datasource = new EmailAttachmentDataSource();

                    datasource.setData(pdfBytes);
                    datasource.setName(paramValue[2]);
                    if (RequestContext.getSession().getAttribute("FileType") != null) {
                      if (RequestContext.getSession().getAttribute("FileType").toString()
                          .equals("RTF")) {
                        datasource.setContentType("application/rtf");
                      } else {
                        datasource.setContentType("application/pdf");
                      }
                    } else {
                      datasource.setContentType("application/pdf");
                    }

                    MimeBodyPart p2 = new MimeBodyPart();
                    p2.setDataHandler(new DataHandler(datasource));
                    p2.setFileName(datasource.getName());
                    mp.addBodyPart(p2);
                  } else {
                    flag = false;
                    break;
                  }
                  RequestContext.getSession().removeAttribute("FileType");
                }

              }
            }
          }
        }
      }

      msg.setContent(mp);
      bus.sendMessage(msg, addresses);
    } catch (SendFailedException sf) {
      flag = false;
      logger.error("Exception when sending mail from insurance", sf);
    } catch (MessagingException me) {
      flag = false;
      logger.error("Exception when sending mail from insurance", me);
    }
    return flag;
  }

  /**
   * Insert transactions.
   *
   * @param con           the con
   * @param transactionID the transaction ID
   * @param bean          the bean
   * @param al            the al
   * @return true, if successful
   * @throws SQLException the SQL exception
   * @throws IOException  Signals that an I/O exception has occurred.
   */
  public boolean insertTransactions(Connection con, int transactionID, BasicDynaBean bean,
      ArrayList al) throws SQLException, IOException {

    // insert into attachments table
    GenericDAO attachmentsDao = new GenericDAO("insurance_transaction_attachments");
    BasicDynaBean attachmentsbean = attachmentsDao.getBean();
    attachmentsbean.set("attachment_id", attachmentsDao.getNextSequence());
    attachmentsbean.set("transaction_id", transactionID);
    ByteArrayInputStream bis = new ByteArrayInputStream((byte[]) al.get(0));
    attachmentsbean.set("attachment_data", bis);
    attachmentsbean.set("doc_title", (String) al.get(1));
    attachmentsbean.set("content_type", (String) al.get(2));
    boolean result = attachmentsDao.insert(con, attachmentsbean);
    return result;
  }
}
