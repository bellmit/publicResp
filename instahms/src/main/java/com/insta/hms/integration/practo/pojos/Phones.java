package com.insta.hms.integration.practo.pojos;

public class Phones {
  private String[] added;

  private String[] updated;

  private String[] soft_deleted;

  public String[] getAdded() {
    return added;
  }

  public void setAdded(String[] added) {
    this.added = added;
  }

  public String[] getUpdated() {
    return updated;
  }

  public void setUpdated(String[] updated) {
    this.updated = updated;
  }

  public String[] getSoft_deleted() {
    return soft_deleted;
  }

  public void setSoft_deleted(String[] soft_deleted) {
    this.soft_deleted = soft_deleted;
  }

  @Override
  public String toString() {
    return "ClassPojo [added = " + added + ", updated = " + updated + ", soft_deleted = "
        + soft_deleted + "]";
  }

}
