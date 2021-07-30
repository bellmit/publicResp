/*
 * Copyright (c) 2008-2009 Insta Health Solutions Pvt Ltd All rights reserved.
 */

package com.insta.hms.usermanager;

import java.util.HashMap;

public class Rights {
  HashMap screenRightsMap;
  HashMap actionRightsMap;
  HashMap urlActionRightsMap;

  public HashMap getUrlActionRightsMap() {
    return urlActionRightsMap;
  }

  public void setUrlActionRightsMap(HashMap urlActionRightsMap) {
    this.urlActionRightsMap = urlActionRightsMap;
  }

  public HashMap getScreenRightsMap() {
    return screenRightsMap;
  }

  public void setScreenRightsMap(HashMap map) {
    screenRightsMap = map;
  }

  public HashMap getActionRightsMap() {
    return actionRightsMap;
  }

  public void setActionRightsMap(HashMap map) {
    actionRightsMap = map;
  }

}
