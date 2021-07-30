package com.insta.hms.common;

import java.util.ArrayList;
import java.util.List;

public class ActionConfigMain {

  public List<ActionHookConfig> actionHookConfigs = new ArrayList<ActionHookConfig>();

  public List<ActionHookConfig> getActionHookConfigs() {
    return actionHookConfigs;
  }

  public void addActionHookConfig(ActionHookConfig actionHookConfig) {
    actionHookConfigs.add(actionHookConfig);
  }

}
