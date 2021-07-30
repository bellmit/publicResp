package com.insta.hms.diagnosticsmasters.outhousemaster;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.upload.FormFile;

import javax.servlet.http.HttpServletRequest;

public class OutHouseMasterForm extends ActionForm {

  private static final long serialVersionUID = 1L;

  String[] ohName;
  String[] ohTestName;
  String[] ohCharge;
  String outHouseName;

  String name;
  String testname;
  String charge;
  String ohId;
  String checkcharge;
  String templateName;

  private FormFile uploadOutHouseDetailsFile;
  private FormFile uploadChargeFile;

  // for out house
  private String[] outhouseFilter;
  private boolean all;
  private boolean inActive;
  private boolean active;
  private String[] groupUpdate;
  private String status;

  private String outHouseId;
  private String deptId;
  private String testId;
  private Double newCharge;

  private String pageNum;
  private String cliaNo;
  private String ohAddress;

  // for sorting
  private String sortOrder;
  private boolean sortReverse;

  public String getSortOrder() {
    return sortOrder;
  }

  public void setSortOrder(String sortOrder) {
    this.sortOrder = sortOrder;
  }

  public boolean getSortReverse() {
    return sortReverse;
  }

  public void setSortReverse(boolean sortReverse) {
    this.sortReverse = sortReverse;
  }

  @Override
  public void reset(ActionMapping arg0, HttpServletRequest arg1) {

    super.reset(arg0, arg1);
    this.newCharge = null;
    this.all = false;
    this.active = false;
    this.inActive = false;
    this.outhouseFilter = null;
  }

  public Double getNewCharge() {
    return newCharge;
  }

  public void setNewCharge(Double newCharge) {
    this.newCharge = newCharge;
  }

  public String[] getGroupUpdate() {
    return groupUpdate;
  }

  public void setGroupUpdate(String[] groupUpdate) {
    this.groupUpdate = groupUpdate;
  }

  public boolean isAll() {
    return all;
  }

  public void setAll(boolean all) {
    this.all = all;
  }

  public String[] getOuthouseFilter() {
    return outhouseFilter;
  }

  public void setOuthouseFilter(String[] outhouseFilter) {
    this.outhouseFilter = outhouseFilter;
  }

  public String[] getOhName() {
    return ohName;
  }

  public void setOhName(String[] ohName) {
    this.ohName = ohName;
  }

  public String[] getOhCharge() {
    return ohCharge;
  }

  public void setOhCharge(String[] ohCharge) {
    this.ohCharge = ohCharge;
  }

  public String[] getOhTestName() {
    return ohTestName;
  }

  public void setOhTestName(String[] ohTestName) {
    this.ohTestName = ohTestName;
  }

  public String getCheckcharge() {
    return checkcharge;
  }

  public void setCheckcharge(String checkcharge) {
    this.checkcharge = checkcharge;
  }

  public String getOhId() {
    return ohId;
  }

  public void setOhId(String ohId) {
    this.ohId = ohId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getTestName() {
    return testname;
  }

  public void setTestName(String testname) {
    this.testname = testname;
  }

  public String getCharge() {
    return charge;
  }

  public void setCharge(String charge) {
    this.charge = charge;
  }

  public String getDeptId() {
    return deptId;
  }

  public void setDeptId(String deptId) {
    this.deptId = deptId;
  }

  public String getOutHouseId() {
    return outHouseId;
  }

  public void setOutHouseId(String outHouseId) {
    this.outHouseId = outHouseId;
  }

  public String getTestId() {
    return testId;
  }

  public void setTestId(String testId) {
    this.testId = testId;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public boolean isInActive() {
    return inActive;
  }

  public void setInActive(boolean inActive) {
    this.inActive = inActive;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  public String getPageNum() {
    return pageNum;
  }

  public void setPageNum(String pageNum) {
    this.pageNum = pageNum;
  }

  public String getOutHouseName() {
    return outHouseName;
  }

  public void setOutHouseName(String outHouseName) {
    this.outHouseName = outHouseName;
  }

  public String getTemplate_name() {
    return templateName;
  }

  public void setTemplate_name(String templateName) {
    this.templateName = templateName;
  }

  public String getCliaNo() {
    return cliaNo;
  }

  public void setCliaNo(String cliaNo) {
    this.cliaNo = cliaNo;
  }

  public String getOhAddress() {
    return ohAddress;
  }

  public void setOhAddress(String ohAddress) {
    this.ohAddress = ohAddress;
  }

  public FormFile getUploadChargeFile() {
    return uploadChargeFile;
  }

  public void setUploadChargeFile(FormFile uploadChargeFile) {
    this.uploadChargeFile = uploadChargeFile;
  }

  public FormFile getUploadOutHouseDetailsFile() {
    return uploadOutHouseDetailsFile;
  }

  public void setUploadOutHouseDetailsFile(FormFile uploadOutHouseDetailsFile) {
    this.uploadOutHouseDetailsFile = uploadOutHouseDetailsFile;
  }

}
