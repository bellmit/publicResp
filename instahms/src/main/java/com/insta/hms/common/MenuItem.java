/*
 * type denotes whether it is WorkList or Reports 
 * if value not empty need to be shown in addToHomePage dialog box.  
 */

package com.insta.hms.common;

public class MenuItem {

  private String name = null;
  private String actionId = null;
  private String urlParams = null;
  private String type = null;
  private String hashFragment = null;
  private String module = null;
  private boolean isSeparator = false;
  private boolean isNew = false;
  private boolean isBeta = false;

  public String getName() {
    return name;
  }

  public String getActionId() {
    return actionId;
  }

  public String getUrlParams() {
    return urlParams;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setActionId(String actionId) {
    this.actionId = actionId;
  }

  public void setUrlParams(String urlParams) {
    this.urlParams = urlParams;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public boolean getIsSeparator() {
    return isSeparator;
  }

  public void setIsSeparator(boolean val) {
    isSeparator = val;
  }

  public String getHashFragment() {
    return hashFragment;
  }

  public void setHashFragment(String hashFragment) {
    this.hashFragment = hashFragment;
  }

  public String getModule() {
    return module;
  }

  public void setModule(String module) {
    this.module = module;
  }
  
  public boolean getIsNew() {
    return isNew;
  }

  public void setIsNew(boolean val) {
    isNew = val;
  }

  public boolean getIsBeta() {
    return isBeta;
  }

  public void setIsBeta(boolean beta) {
    isBeta = beta;
  }
}
