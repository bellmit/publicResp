package com.insta.hms.common;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Class representing a queue-like map to hold the tokens. Simply extends a LinkedHashMap limiting
 * the size to a pre-defined value Removes the older tokens when the specified size is reached. This
 * class is not thread safe.
 */

public class TokenQueue extends LinkedHashMap<String, RequestToken> implements Serializable {
  static final long serialVersionUID = 1;
  private static final int TRANSACTION_TOKEN_QUEUE_LENGTH = 50;

  protected boolean removeEldestEntry(Map.Entry eldest) {
    return this.size() > TRANSACTION_TOKEN_QUEUE_LENGTH;
  }
}
