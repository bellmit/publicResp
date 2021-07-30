package com.bob.hms.common;

/**
 * The Class Screen.
 */
public class Screen {
  private String id;
  private String name;
  private String module;
  private String group;
  private String url;
  private Boolean hidden = false;
  private Boolean instaAdminOnly = false;

  /**
   * Gets the id.
   *
   * @return the id
   */
  /*
   * Accessors
   */
  public String getId() {
    return id;
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
   * Gets the module.
   *
   * @return the module
   */
  public String getModule() {
    return module;
  }

  /**
   * Gets the group.
   *
   * @return the group
   */
  public String getGroup() {
    return group;
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
   * Sets the id.
   *
   * @param id the new id
   */
  public void setId(String id) {
    this.id = id;
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
   * Sets the module.
   *
   * @param module the new module
   */
  public void setModule(String module) {
    this.module = module;
  }

  /**
   * Sets the group.
   *
   * @param group the new group
   */
  public void setGroup(String group) {
    this.group = group;
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
   * Gets the hidden.
   *
   * @return the hidden
   */
  public Boolean getHidden() {
    return hidden;
  }

  /**
   * Sets the hidden.
   *
   * @param isHidden the new hidden
   */
  public void setHidden(Boolean isHidden) {
    hidden = isHidden;
  }

  /**
   * Gets the insta admin only.
   *
   * @return the insta admin only
   */
  public Boolean getInstaAdminOnly() {
    return instaAdminOnly;
  }

  /**
   * Sets the insta admin only.
   *
   * @param isInstaAdmin the new insta admin only
   */
  public void setInstaAdminOnly(Boolean isInstaAdmin) {
    instaAdminOnly = isInstaAdmin;
  }
}
