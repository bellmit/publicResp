package com.bob.hms.otmasters.opemaster;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import javax.servlet.http.HttpServletRequest;

/**
 * The Class OpeMasterForm.
 */
public class OpeMasterForm extends ActionForm {


  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;
  
  /** The operation name. */
  private String operationName;
  
  /** The deptid. */
  private String deptid;
  
  /** The org name. */
  private String orgName;
  
  /** The org id. */
  private String orgId;
  
  /** The description. */
  private String description;
  
  /** The active status. */
  private String activeStatus;
  
  /** The bed types. */
  private String[] bedTypes;
  
  /** The surgeon charge. */
  private Double[] surgeonCharge;
  
  /** The anesthetist charge. */
  private Double[] anesthetistCharge;
  
  /** The surgica asst charge. */
  private Double[] surgicaAsstCharge;

  /** The dept filter. */
  private String[] deptFilter;
  
  /** The all operations. */
  private String allOperations;
  
  /** The active operations. */
  private String activeOperations;
  
  /** The inactive operations. */
  private String inactiveOperations;
  
  /** The charge type. */
  private String chargeType;
  
  /** The page num. */
  private String pageNum;
  
  /** The operation id. */
  private String operationId;

  /** The group beds. */
  private String[] groupBeds;
  
  /** The variance type. */
  private String varianceType;
  
  /** The variance by. */
  private Double varianceBy;
  
  /** The variance value. */
  private Double varianceValue;
  
  /** The group operations. */
  private String[] groupOperations;
  
  /** The group updat component. */
  private String groupUpdatComponent;

  /** The operation filter. */
  private String operationFilter;
  
  /** The operation filter id. */
  private String operationFilterId;
  
  /** The start page. */
  private String startPage;
  
  /** The end page. */
  private String endPage;

  /** The org item code. */
  private String orgItemCode;
  
  /** The applicable. */
  private boolean applicable;

  /** The all app services. */
  private String allAppServices;
  
  /** The app services. */
  private String appServices;
  
  /** The inapp services. */
  private String inappServices;



  /**
   * Gets the all app services.
   *
   * @return the all app services
   */
  public String getAllAppServices() {
    return allAppServices;
  }

  /**
   * Sets the all app services.
   *
   * @param allAppServices the new all app services
   */
  public void setAllAppServices(String allAppServices) {
    this.allAppServices = allAppServices;
  }

  /**
   * Gets the app services.
   *
   * @return the app services
   */
  public String getAppServices() {
    return appServices;
  }

  /**
   * Sets the app services.
   *
   * @param appServices the new app services
   */
  public void setAppServices(String appServices) {
    this.appServices = appServices;
  }

  /**
   * Gets the inapp services.
   *
   * @return the inapp services
   */
  public String getInappServices() {
    return inappServices;
  }

  /**
   * Sets the inapp services.
   *
   * @param inappServices the new inapp services
   */
  public void setInappServices(String inappServices) {
    this.inappServices = inappServices;
  }

  /**
   * Gets the applicable.
   *
   * @return the applicable
   */
  public boolean getApplicable() {
    return applicable;
  }

  /**
   * Sets the applicable.
   *
   * @param applicable the new applicable
   */
  public void setApplicable(boolean applicable) {
    this.applicable = applicable;
  }

  /**
   * Gets the org item code.
   *
   * @return the org item code
   */
  public String getOrgItemCode() {
    return orgItemCode;
  }

  /**
   * Sets the org item code.
   *
   * @param orgItemCode the new org item code
   */
  public void setOrgItemCode(String orgItemCode) {
    this.orgItemCode = orgItemCode;
  }

  /**
   * Gets the operation filter.
   *
   * @return the operation filter
   */
  public String getOperationFilter() {
    return operationFilter;
  }

  /**
   * Sets the operation filter.
   *
   * @param operationFilter the new operation filter
   */
  public void setOperationFilter(String operationFilter) {
    this.operationFilter = operationFilter;
  }

  /**
   * Gets the operation filter id.
   *
   * @return the operation filter id
   */
  public String getOperationFilterId() {
    return operationFilterId;
  }

  /**
   * Sets the operation filter id.
   *
   * @param operationFilterId the new operation filter id
   */
  public void setOperationFilterId(String operationFilterId) {
    this.operationFilterId = operationFilterId;
  }

  /**
   * Gets the group updat component.
   *
   * @return the group updat component
   */
  public String getGroupUpdatComponent() {
    return groupUpdatComponent;
  }

  /**
   * Sets the group updat component.
   *
   * @param groupUpdatComponent the new group updat component
   */
  public void setGroupUpdatComponent(String groupUpdatComponent) {
    this.groupUpdatComponent = groupUpdatComponent;
  }

  /**
   * Gets the group operations.
   *
   * @return the group operations
   */
  public String[] getGroupOperations() {
    return groupOperations;
  }

  /**
   * Sets the group operations.
   *
   * @param groupOperations the new group operations
   */
  public void setGroupOperations(String[] groupOperations) {
    this.groupOperations = groupOperations;
  }

  /**
   * Gets the group beds.
   *
   * @return the group beds
   */
  public String[] getGroupBeds() {
    return groupBeds;
  }

  /**
   * Sets the group beds.
   *
   * @param groupBeds the new group beds
   */
  public void setGroupBeds(String[] groupBeds) {
    this.groupBeds = groupBeds;
  }

  /**
   * Gets the variance by.
   *
   * @return the variance by
   */
  public Double getVarianceBy() {
    return varianceBy;
  }

  /**
   * Sets the variance by.
   *
   * @param varianceBy the new variance by
   */
  public void setVarianceBy(Double varianceBy) {
    this.varianceBy = varianceBy;
  }

  /**
   * Gets the variance type.
   *
   * @return the variance type
   */
  public String getVarianceType() {
    return varianceType;
  }

  /**
   * Sets the variance type.
   *
   * @param varianceType the new variance type
   */
  public void setVarianceType(String varianceType) {
    this.varianceType = varianceType;
  }

  /**
   * Gets the variance value.
   *
   * @return the variance value
   */
  public Double getVarianceValue() {
    return varianceValue;
  }

  /**
   * Sets the variance value.
   *
   * @param varianceValue the new variance value
   */
  public void setVarianceValue(Double varianceValue) {
    this.varianceValue = varianceValue;
  }

  /**
   * Gets the operation id.
   *
   * @return the operation id
   */
  public String getOperationId() {
    return operationId;
  }

  /**
   * Sets the operation id.
   *
   * @param operationId the new operation id
   */
  public void setOperationId(String operationId) {
    this.operationId = operationId;
  }

  /**
   * Gets the page num.
   *
   * @return the page num
   */
  public String getPageNum() {
    return pageNum;
  }

  /**
   * Sets the page num.
   *
   * @param pageNum the new page num
   */
  public void setPageNum(String pageNum) {
    this.pageNum = pageNum;
  }

  /**
   * Gets the charge type.
   *
   * @return the charge type
   */
  public String getChargeType() {
    return chargeType;
  }

  /**
   * Sets the charge type.
   *
   * @param chargeType the new charge type
   */
  public void setChargeType(String chargeType) {
    this.chargeType = chargeType;
  }

  /**
   * Gets the active operations.
   *
   * @return the active operations
   */
  public String getActiveOperations() {
    return activeOperations;
  }

  /**
   * Sets the active operations.
   *
   * @param activeOperations the new active operations
   */
  public void setActiveOperations(String activeOperations) {
    this.activeOperations = activeOperations;
  }

  /**
   * Gets the all operations.
   *
   * @return the all operations
   */
  public String getAllOperations() {
    return allOperations;
  }

  /**
   * Sets the all operations.
   *
   * @param allOperations the new all operations
   */
  public void setAllOperations(String allOperations) {
    this.allOperations = allOperations;
  }

  /**
   * Gets the inactive operations.
   *
   * @return the inactive operations
   */
  public String getInactiveOperations() {
    return inactiveOperations;
  }

  /**
   * Sets the inactive operations.
   *
   * @param inactiveOperations the new inactive operations
   */
  public void setInactiveOperations(String inactiveOperations) {
    this.inactiveOperations = inactiveOperations;
  }

  /**
   * Gets the dept filter.
   *
   * @return the dept filter
   */
  public String[] getDeptFilter() {
    return deptFilter;
  }

  /**
   * Sets the dept filter.
   *
   * @param deptFilter the new dept filter
   */
  public void setDeptFilter(String[] deptFilter) {
    this.deptFilter = deptFilter;
  }

  /**
   * Gets the anesthetist charge.
   *
   * @return the anesthetist charge
   */
  public Double[] getAnesthetistCharge() {
    return anesthetistCharge;
  }

  /**
   * Sets the anesthetist charge.
   *
   * @param anesthetistCharge the new anesthetist charge
   */
  public void setAnesthetistCharge(Double[] anesthetistCharge) {
    this.anesthetistCharge = anesthetistCharge;
  }

  /**
   * Gets the bed types.
   *
   * @return the bed types
   */
  public String[] getBedTypes() {
    return bedTypes;
  }

  /**
   * Sets the bed types.
   *
   * @param bedTypes the new bed types
   */
  public void setBedTypes(String[] bedTypes) {
    this.bedTypes = bedTypes;
  }

  /**
   * Gets the surgeon charge.
   *
   * @return the surgeon charge
   */
  public Double[] getSurgeonCharge() {
    return surgeonCharge;
  }

  /**
   * Sets the surgeon charge.
   *
   * @param surgeonCharge the new surgeon charge
   */
  public void setSurgeonCharge(Double[] surgeonCharge) {
    this.surgeonCharge = surgeonCharge;
  }

  /**
   * Gets the surgica asst charge.
   *
   * @return the surgica asst charge
   */
  public Double[] getSurgicaAsstCharge() {
    return surgicaAsstCharge;
  }

  /**
   * Sets the surgica asst charge.
   *
   * @param surgicaAsstCharge the new surgica asst charge
   */
  public void setSurgicaAsstCharge(Double[] surgicaAsstCharge) {
    this.surgicaAsstCharge = surgicaAsstCharge;
  }

  /**
   * Gets the active status.
   *
   * @return the active status
   */
  public String getActiveStatus() {
    return activeStatus;
  }

  /**
   * Sets the active status.
   *
   * @param activeStatus the new active status
   */
  public void setActiveStatus(String activeStatus) {
    this.activeStatus = activeStatus;
  }

  /**
   * Gets the description.
   *
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  /**
   * Sets the description.
   *
   * @param description the new description
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Gets the org id.
   *
   * @return the org id
   */
  public String getOrgId() {
    return orgId;
  }

  /**
   * Sets the org id.
   *
   * @param orgId the new org id
   */
  public void setOrgId(String orgId) {
    this.orgId = orgId;
  }

  /**
   * Gets the org name.
   *
   * @return the org name
   */
  public String getOrgName() {
    return orgName;
  }

  /**
   * Sets the org name.
   *
   * @param orgName the new org name
   */
  public void setOrgName(String orgName) {
    this.orgName = orgName;
  }

  /**
   * Gets the operation name.
   *
   * @return the operation name
   */
  public String getOperationName() {
    return operationName;
  }

  /**
   * Sets the operation name.
   *
   * @param operationName the new operation name
   */
  public void setOperationName(String operationName) {
    this.operationName = operationName;
  }

  /**
   * Gets the serial version UID.
   *
   * @return the serial version UID
   */
  public static long getSerialVersionUID() {
    return serialVersionUID;
  }

  /**
   * Gets the deptid.
   *
   * @return the deptid
   */
  public String getDeptid() {
    return deptid;
  }

  /**
   * Sets the deptid.
   *
   * @param deptid the new deptid
   */
  public void setDeptid(String deptid) {
    this.deptid = deptid;
  }

  /* (non-Javadoc)
   * @see org.apache.struts.action.ActionForm#reset(org.apache.struts.action.ActionMapping, javax.servlet.http.HttpServletRequest)
   */
  @Override
  public void reset(ActionMapping arg0, HttpServletRequest arg1) {
    super.reset(arg0, arg1);
    this.activeStatus = null;
    this.anesthetistCharge = null;
    this.bedTypes = null;
    this.deptid = null;
    this.description = null;
    this.operationName = null;
    this.orgId = null;
    this.orgName = null;
    this.surgeonCharge = null;
    this.surgicaAsstCharge = null;
    this.anesthetistCharge = null;
  }

  /**
   * Gets the end page.
   *
   * @return the end page
   */
  public String getEndPage() {
    return endPage;
  }

  /**
   * Sets the end page.
   *
   * @param endPage the new end page
   */
  public void setEndPage(String endPage) {
    this.endPage = endPage;
  }

  /**
   * Gets the start page.
   *
   * @return the start page
   */
  public String getStartPage() {
    return startPage;
  }

  /**
   * Sets the start page.
   *
   * @param startPage the new start page
   */
  public void setStartPage(String startPage) {
    this.startPage = startPage;
  }



}
