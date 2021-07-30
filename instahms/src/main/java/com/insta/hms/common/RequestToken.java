package com.insta.hms.common;

import org.apache.struts.config.ForwardConfig;

import java.io.Serializable;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Class representing a RequestToken which maintains the state of the token and the last result that
 * was returned from the call to the action method.
 */
public class RequestToken implements Serializable {

  /** The Constant serialVersionUID. */
  static final long serialVersionUID = 1;

  /** The Constant TOKEN_STATE_NEW. */
  private static final int TOKEN_STATE_NEW = 0;

  /** The Constant TOKEN_STATE_USED. */
  private static final int TOKEN_STATE_USED = 1;

  /** The token key. */
  private String tokenKey = null;

  /** The state. */
  private int state = TOKEN_STATE_NEW;

  /** The last result. */
  private ForwardConfig lastResult = null;

  /** The token lock. */
  private ReentrantLock tokenLock = new ReentrantLock();

  /**
   * Instantiates a new request token.
   */
  public RequestToken() {
  }

  /**
   * Constructs a token from a given key.
   *
   * @param tokenKey - String that specifies the token key
   */
  public RequestToken(String tokenKey) {
    this.tokenKey = tokenKey;
  }

  /**
   * Method to check if the token is valid. A token is valid if it is NEW.
   * 
   * @return boolean - true if the token is new, false otherwise.
   */
  public boolean isValid() {
    return state == TOKEN_STATE_NEW;
  }

  /**
   * Gets the last ForwardConfig that was used by the request that succeeded.
   *
   * @return the last result
   */
  public ForwardConfig getLastResult() {
    return lastResult;
  }

  /**
   * Sets the specified ForwardConfig as the last result associated with the token.
   *
   * @param forward the new last result
   */
  public void setLastResult(ForwardConfig forward) {
    this.state = TOKEN_STATE_USED;
    this.lastResult = forward;
  }

  /**
   * Method to lock the token.
   *
   */
  public void lock() {
    tokenLock.lock();
  }

  /**
   * Method to unlock the token. Unlocks the token only if the current thread holds the token.
   * 
   * @return boolean - true if the lock was released, false otherwise.
   */
  public boolean unlock() {
    if (tokenLock.isHeldByCurrentThread()) {
      tokenLock.unlock();
      return true;
    }
    return false;
  }

  /**
   * Method to get the auto-generated key associated with the token.
   * 
   * @return String value of the token key.
   */
  public String getTokenKey() {
    return tokenKey;
  }

}
