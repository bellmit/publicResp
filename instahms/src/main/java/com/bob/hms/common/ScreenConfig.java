package com.bob.hms.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;

// TODO: Auto-generated Javadoc
/**
 * The Class ScreenConfig.
 */
public class ScreenConfig {
  static Logger logger = LoggerFactory.getLogger(ScreenConfig.class);

  private ArrayList screenGroupList = new ArrayList();
  private HashMap screenGroupMap = new HashMap();
  private HashMap screenMap = new HashMap();
  // private ArrayList homeMenu = new ArrayList();
  // private ArrayList topMenu = new ArrayList();

  /**
   * Gets the screen group list.
   *
   * @return the screen group list
   */
  /*
   * Accessors
   */
  public ArrayList getScreenGroupList() {
    return screenGroupList;
  }

  /**
   * Gets the screen group map.
   *
   * @return the screen group map
   */
  public HashMap getScreenGroupMap() {
    return screenGroupMap;
  }

  /**
   * Gets the screen map.
   *
   * @return the screen map
   */
  public HashMap getScreenMap() {
    return screenMap;
  }

  /*
   * public ArrayList getHomeMenu() { return homeMenu; }
   */
  /*
   * public ArrayList getTopMenu() { return topMenu; }
   */
  /**
   * Other methods: used by digester.
   *
   * @param grp the grp
   */
  public void addScreenGroup(ScreenGroup grp) {
    logger.debug("Adding screen group: " + grp.getId() + "; Name: " + grp.getName());
    screenGroupList.add(grp);
    screenGroupMap.put(grp.getId(), grp);

    // add all the screens in this group to the screens HashMap for direct access convenience
    screenMap.putAll(grp.getScreenMap());
  }

  /*
   * public void addMenuGroup(ArrayList menuGroup) { logger.debug("Adding menu: " +
   * menuGroup.size()); homeMenu.add(menuGroup); }
   */
  /*
   * public void addTopMenu(MenuGroup group) { topMenu.add(group); }
   */
  /**
   * Gets the screen group.
   *
   * @param groupId the group id
   * @return the screen group
   */
  /*
   * Other methods
   */
  public ScreenGroup getScreenGroup(String groupId) {
    return (ScreenGroup) screenGroupMap.get(groupId);
  }

  /**
   * Gets the screen.
   *
   * @param screenId the screen id
   * @return the screen
   */
  public Screen getScreen(String screenId) {
    return (Screen) screenMap.get(screenId);
  }
}
