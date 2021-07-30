package com.bob.hms.common;

/*
 * This class is instantiated and populated by the DigesterPlugIn. See screens-digester.xml
 * in WEB-INF for how the XML is parsed. When a screen_group element is encountered, a
 * ScreenGroup object is created, and all attributes of that element are stuffed in
 * as properties of this object.
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * The Class ScreenGroup.
 */
public class ScreenGroup {
  static Logger logger = LoggerFactory.getLogger(ScreenConfig.class);
  private String id = null;
  private String name = null;
  private String type = null;
  private String defaultModule = null; /* name of module for screens in this group */
  private String url = null;
  private String defaultScreenId = null;
  private String hasRights = "Y";

  private Screen defaultScreen = null;
  private ArrayList screenList = new ArrayList();
  private HashMap screenMap = new HashMap();

  /**
   * Gets the id.
   *
   * @return the id
   */
  public String getId() {
    return id;
  }

  /**
   * Sets the id.
   *
   * @param id the new id
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Gets the name.
   *
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the name.
   *
   * @param name the new name
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Gets the type.
   *
   * @return the type
   */
  public String getType() {
    return type;
  }

  /**
   * Sets the type.
   *
   * @param type the new type
   */
  public void setType(String type) {
    this.type = type;
  }

  /**
   * Gets the url.
   *
   * @return the url
   */
  public String getUrl() {
    return url;
  }

  /**
   * Sets the url.
   *
   * @param url the new url
   */
  public void setUrl(String url) {
    this.url = url;
  }

  /**
   * Gets the default screen id.
   *
   * @return the default screen id
   */
  public String getDefaultScreenId() {
    return defaultScreenId;
  }

  /**
   * Sets the default screen id.
   *
   * @param defaultScreenId the new default screen id
   */
  public void setDefaultScreenId(String defaultScreenId) {
    this.defaultScreenId = defaultScreenId;
  }

  /**
   * Gets the checks for rights.
   *
   * @return the checks for rights
   */
  public String getHasRights() {
    return hasRights;
  }

  /**
   * Sets the checks for rights.
   *
   * @param hasRightsString the new checks for rights
   */
  public void setHasRights(String hasRightsString) {
    hasRights = hasRightsString;
  }

  /**
   * Gets the default screen.
   *
   * @return the default screen
   */
  public Screen getDefaultScreen() {
    if ((defaultScreen == null) && (defaultScreenId != null)) {
      defaultScreen = (Screen) screenMap.get(defaultScreenId);
    }
    return defaultScreen;
  }

  /**
   * Sets the default screen.
   *
   * @param defaultScreen the new default screen
   */
  public void setDefaultScreen(Screen defaultScreen) {
    this.defaultScreen = defaultScreen;
  }

  /**
   * Gets the default module.
   *
   * @return the default module
   */
  public String getDefaultModule() {
    return defaultModule;
  }

  /**
   * Sets the default module.
   *
   * @param defaultModule the new default module
   */
  public void setDefaultModule(String defaultModule) {
    this.defaultModule = defaultModule;
  }

  /**
   * Gets the screen list.
   *
   * @return the screen list
   */
  public ArrayList getScreenList() {
    return screenList;
  }

  /**
   * Gets the screen map.
   *
   * @return the screen map
   */
  public HashMap getScreenMap() {
    return screenMap;
  }

  /**
   * Gets the screen.
   *
   * @param screenId the screen id
   * @return the screen
   */
  /*
   * Other methods
   */
  public Screen getScreen(String screenId) {
    return (Screen) screenMap.get(screenId);
  }

  /**
   * Adds the screen.
   *
   * @param screen the screen
   */
  public void addScreen(Screen screen) {
    logger.debug("Adding screen: " + screen.getId() + "; Name: " + screen.getName());
    screenList.add(screen);
    screenMap.put(screen.getId(), screen);
    // in addition, supply our group name to the screen, and our default module
    // as the screen's module if there is no module set in the screen itself.
    screen.setGroup(name);
    if (screen.getModule() == null) {
      screen.setModule(defaultModule);
    }
  }
}
