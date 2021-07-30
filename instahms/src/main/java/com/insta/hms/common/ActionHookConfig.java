package com.insta.hms.common;

import java.util.ArrayList;
import java.util.List;

public class ActionHookConfig {
  private String id; // action_id
  private String primaryKey; // Primary key value of main table used by action class.
  private String method; // Method that will be called in the action class

  // classes to be called when specified action_id is processed
  private List plugins = new ArrayList();

  public void setId(String id) {
    this.id = id;
  }

  public void setMethod(String method) {
    this.method = method;
  }

  public void setPrimaryKey(String primaryKey) {
    this.primaryKey = primaryKey;
  }

  public void setPlugins(List plugins) {
    this.plugins = plugins;
  }

  public void addPlugins(ActionPluginConfig plugin) {
    plugins.add(plugin);
  }

  public int getPluginsCount() {
    return plugins.size();
  }

  public String getId() {
    return id;
  }

  public String getMethod() {
    return method;
  }

  public String getPrimaryKey() {
    return primaryKey;
  }

  public List getPlugins() {
    return plugins;
  }

}