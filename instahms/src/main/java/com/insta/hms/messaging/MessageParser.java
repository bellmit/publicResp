package com.insta.hms.messaging;


public interface MessageParser {
  
  /**
   * Gets the insta message.
   *
   * @param msg the msg
   * @return the insta message
   */
  Message getInstaMessage(String msg);
}
