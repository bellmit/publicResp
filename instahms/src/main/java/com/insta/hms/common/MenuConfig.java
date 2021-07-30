package com.insta.hms.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MenuConfig {

  static Logger logger = LoggerFactory.getLogger(MenuConfig.class);

  private Map groups = new HashMap();
  private Map menu = new HashMap();

  /*
   * Accessors
   */
  public List getHomeMenu() {
    return (List) menu.get("home_menu");
  }

  public List getTopMenu() {
    return (List) menu.get("top_menu");
  }

  public Map getMenuGroups() {
    return groups;
  }

  /*
   * Other methods: used by digester
   */
  public void addMenuGroup(MenuGroup group) {
    logger.debug("adding group " + group.getId() + " : " + group.getName());
    groups.put(group.getId(), group);
  }

  public void addHomeMenu(List menuList) {
    menu.put("home_menu", menuList);
  }

  public void addTopMenu(ArrayList menuList) {
    menu.put("top_menu", menuList);
  }

}
