package com.insta.hms.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

import javax.mail.MessagingException;

public class EmailDispatcher extends GenericDispatcher implements MessageDispatcher {
  static Logger logger = LoggerFactory.getLogger(EmailDispatcher.class);

  public EmailDispatcher() {
    super("EMAIL");
  }

  public boolean dispatch(Message msg) throws SQLException, MessagingException {
    return send(msg);
  }
}
