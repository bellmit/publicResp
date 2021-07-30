package com.insta.hms.messaging;

import java.sql.SQLException;

import javax.mail.MessagingException;

public interface MessageDispatcher {

  boolean dispatch(Message msg) throws SQLException, MessagingException;

}
