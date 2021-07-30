package com.insta.hms.integration.practo.pojos;

public class LocalitiesWrapper {
  private Localities[] localities;

  public Localities[] getLocalities() {
    return localities;
  }

  public void setLocalities(Localities[] localities) {
    this.localities = localities;
  }

  @Override
  public String toString() {
    return "ClassPojo [localities = " + localities + "]";
  }

}
