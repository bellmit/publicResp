package com.insta.hms.common;

import java.util.Map;

public interface ActionPlugin {
  public boolean execute(Map<String, String[]> params);
}