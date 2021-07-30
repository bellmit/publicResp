/*
 * Copyright (c) 2008-2009 Insta Health Solutions Pvt Ltd All rights reserved.
 */

package com.insta.hms.usermanager;

/*
 * Role: Simple DTO to store Role Details information, which contains the role
 * information + the access permissions, all in one.
 */
public class RoleDetails {
  private Role role;
  private Rights rights;

  public Role getRole() {
    return role;
  }

  public void setRole(Role role) {
    this.role = role;
  }

  public Rights getRights() {
    return rights;
  }

  public void setRights(Rights rights) {
    this.rights = rights;
  }

}
