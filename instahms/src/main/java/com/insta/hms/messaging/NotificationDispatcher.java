package com.insta.hms.messaging;

import java.sql.SQLException;

import javax.mail.MessagingException;

public class NotificationDispatcher extends GenericDispatcher implements MessageDispatcher {

  public NotificationDispatcher() {
    super("NOTIFICATION");
  }

  public boolean dispatch(Message msg) throws SQLException, MessagingException {
    return true;
  }

}
