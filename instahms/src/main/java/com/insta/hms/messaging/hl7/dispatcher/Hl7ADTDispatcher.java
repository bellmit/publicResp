package com.insta.hms.messaging.hl7.dispatcher;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.app.Connection;
import ca.uhn.hl7v2.app.Initiator;
import ca.uhn.hl7v2.model.v23.message.ACK;
import ca.uhn.hl7v2.parser.Parser;

import com.insta.hms.messaging.GenericDispatcher;
import com.insta.hms.messaging.Message;
import com.insta.hms.messaging.MessageDispatcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.sql.SQLException;
import java.util.List;

import javax.mail.MessagingException;

public class Hl7ADTDispatcher extends GenericDispatcher implements MessageDispatcher {

  private final Logger logger = LoggerFactory.getLogger(Hl7ADTDispatcher.class);

  public Hl7ADTDispatcher() {
    super("HL7_SOCKET_ADT");
  }

  @SuppressWarnings("resource")
  @Override
  public boolean dispatch(Message msg) throws SQLException, MessagingException {

    boolean status = true;
    List<String> receipients = msg.getReceipients();
    if (receipients == null || receipients.isEmpty()) {
      String info = "No host and port is configure";
      logger.error(info);
      throw new MessagingException(info);
    }
    for (String receipint : receipients) {
      String url = "http://%s";
      Connection connection = null;
      try {
        URI uri = new URI(String.format(url, receipint));
        String host = uri.getHost();
        int port = uri.getPort();
        HapiContext context = new DefaultHapiContext();
        Boolean useTls = false;
        logger.info("SENDING MESSAGE TO HOST : " + host + " Port :" + port);
        connection = context.newClient(host, port, useTls);
        Initiator initiator = connection.getInitiator();
        Parser parser = context.getPipeParser();
        ca.uhn.hl7v2.model.Message adt = parser.parse(msg.getBody());
        ca.uhn.hl7v2.model.Message hl7Message = initiator.sendAndReceive(adt);
        logger.info("ACK RECEIVED FROM HOST : " + host + " AND PORT :" + port);
        logger.info("Updating status for the same.");
        String responseString = parser.encode(hl7Message);
        logger.debug("ACKNOWLEDGEMENT " + responseString);
        msg.setSubject(responseString);
        if (hl7Message instanceof ACK) {
          ACK ackResponseMessage = (ACK) hl7Message;
          if (ackResponseMessage.getMSA().getAcknowledgementCode().encode().equals("AE")) {
            status = false;
            throw new MessagingException("Application acknowledgment: Error");
          } else if (ackResponseMessage.getMSA().getAcknowledgementCode().encode().equals("AR")) {
            status = false;
            throw new MessagingException("Application acknowledgment: Reject");
          } else if (ackResponseMessage.getMSA().getAcknowledgementCode().encode().equals("CE")) {
            status = false;
            throw new MessagingException("Application acknowledgment: Commit Error");
          } else if (ackResponseMessage.getMSA().getAcknowledgementCode().encode().equals("CR")) {
            status = false;
            throw new MessagingException("Application acknowledgment: Commit Reject");
          }
        }
      } catch (Exception ex) {
        status = false;
        logger.error("HL7 sending fail :" + ex.getMessage());
        throw new MessagingException(ex.getMessage());
      } finally {
        logger.debug("HL7_COMMUNICATION_DONE");
        if (connection != null) {
          connection.close();
          connection = null;
        }
      }
    }
    return status;
  }

}
