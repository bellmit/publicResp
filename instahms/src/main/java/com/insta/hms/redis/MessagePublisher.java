package com.insta.hms.redis;

/**
 * The Interface MessagePublisher which needs to be implemented for message distribution.
 * 
 * @author tanmay.k
 */
public interface MessagePublisher {

  /**
   * Publish.
   *
   * @param message
   *          the message
   */
  void publish(String message);
}