package com.bob.hms.adminmasters.wardandbed;

public class BedNames {

  private String wardNo;
  private String bedType;
  private String bedName;
  private String occupancy;
  private String status;
  private int bedId;

  public int getBedId() {
    return bedId;
  }

  public void setBedId(int bedId) {
    this.bedId = bedId;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getBedName() {
    return bedName;
  }

  public void setBedName(String bedName) {
    this.bedName = bedName;
  }

  public String getBedType() {
    return bedType;
  }

  public void setBedType(String bedType) {
    this.bedType = bedType;
  }

  public String getOccupancy() {
    return occupancy;
  }

  public void setOccupancy(String occupancy) {
    this.occupancy = occupancy;
  }

  public String getWardNo() {
    return wardNo;
  }

  public void setWardNo(String wardNo) {
    this.wardNo = wardNo;
  }

}
