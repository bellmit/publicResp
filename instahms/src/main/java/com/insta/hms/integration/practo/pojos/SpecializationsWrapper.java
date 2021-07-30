package com.insta.hms.integration.practo.pojos;

public class SpecializationsWrapper {
  private Specializations[] specializations;

  public Specializations[] getSpecializations() {
    return specializations;
  }

  public void setSpecializations(Specializations[] specializations) {
    this.specializations = specializations;
  }

  @Override
  public String toString() {
    return "ClassPojo [specializations = " + specializations + "]";
  }

}
