package com.insta.hms.integration.practo.pojos;

public class Added {
  private String subspecialization_id;

  private String published;

  public String getSubspecialization_id() {
    return subspecialization_id;
  }

  public void setSubspecialization_id(String subspecialization_id) {
    this.subspecialization_id = subspecialization_id;
  }

  public String getPublished() {
    return published;
  }

  public void setPublished(String published) {
    this.published = published;
  }

  @Override
  public String toString() {
    return "ClassPojo [subspecialization_id = " + subspecialization_id + ", published = "
        + published + "]";
  }

}
