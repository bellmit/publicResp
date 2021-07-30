package com.bob.hms.common;

import java.util.ArrayList;
import java.util.List;

/**
 * The Class MenuGroup.
 */
public class MenuGroup {
  private String name;
  private String id;
  private List subGroups = new ArrayList();

  /**
   * Sets the name.
   *
   * @param name the new name
   */
  public void setName(String name) {
    this.name = name;
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
   * Sets the id.
   *
   * @param id the new id
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Gets the id.
   *
   * @return the id
   */
  public String getId() {
    return id;
  }

  /**
   * Adds the sub groups.
   *
   * @param group the group
   */
  public void addSubGroups(MenuGroup group) {
    subGroups.add(group);
  }

  /**
   * Gets the sub groups.
   *
   * @return the sub groups
   */
  public List getSubGroups() {
    return subGroups;
  }

}
