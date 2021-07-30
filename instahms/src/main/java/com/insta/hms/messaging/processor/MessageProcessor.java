package com.insta.hms.messaging.processor;

import com.insta.hms.messaging.Message;

import java.util.Map;

public interface MessageProcessor {
  public boolean process(Map dataMap);

  public Map parse(Message msg);
}


