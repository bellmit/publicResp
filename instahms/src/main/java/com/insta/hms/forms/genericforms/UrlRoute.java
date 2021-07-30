package com.insta.hms.forms.genericforms;

public class UrlRoute {
  
  private UrlRoute() {}
  
  static final String SHOW_DETAILS = "/show";
  static final String ADD_FORM = "visit/{visitId}/add";
  static final String DISCARD_FORM = "/{id}/discard";
  static final String EDIT_FORM = "/{id}/edit";
}
