package com.insta.hms.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class MenuGroup {
  static Logger logger = LoggerFactory.getLogger(MenuGroup.class);
  private String name;
  private String id;
  private String defaultModule;
  private List subGroups = new ArrayList();
  private List menuItems = new ArrayList();

  public void setName(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setDefaultModule(String module) {
    this.defaultModule = module;
  }

  public String getDefaultModule() {
    return defaultModule;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getId() {
    return id;
  }

  public void addSubGroups(MenuGroup group) {
    subGroups.add(group);
  }

  public List getSubGroups() {
    return subGroups;
  }

  public void addMenuItem(MenuItem item) {
    menuItems.add(item);
  }

  public List getMenuItems() {
    return menuItems;
  }

  public int getSubGroupCount() {
    return subGroups.size();
  }

  public int getMenuItemCount() {
    return menuItems.size();
  }

}
