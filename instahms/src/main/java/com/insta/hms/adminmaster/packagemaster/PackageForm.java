package com.insta.hms.adminmaster.packagemaster;

import org.apache.struts.action.ActionForm;

import java.math.BigDecimal;

// TODO: Auto-generated Javadoc
/**
 * The Class PackageForm.
 */
public class PackageForm extends ActionForm {

  /** The package name. */
  // variables for packageList screen
  private String packageName;
  
  /** The package type. */
  private String packageType;
  
  /** The department. */
  private String department;
  
  /** The btype. */
  private String[] btype;
  
  /** The organization. */
  private String[] organization;
  
  /** The pkg name. */
  private String pkgName;
  
  /** The status all. */
  private String statusAll;
  
  /** The status ip. */
  private String statusIp;
  
  /** The status op. */
  private String statusOp;
  
  /** The status diag. */
  private String statusDiag;
  
  /** The sp all. */
  private String spAll;
  
  /** The spackage. */
  private String spackage;
  
  /** The stemplate. */
  private String stemplate;
  
  /** The package active all. */
  private String packageActiveAll;
  
  /** The package active A. */
  private String packageActiveA;
  
  /** The package active I. */
  private String packageActiveI;
  
  /** The page num. */
  private String pageNum;
  
  /** The package ob id. */
  private String[] packageObId;
  
  /** The applicable all. */
  private String applicableAll;
  
  /** The applicable org. */
  private String applicableOrg;
  
  /** The applicable not. */
  private String applicableNot;
  
  /** The activity qty. */
  private int[] activity_qty;

  /** The pack name. */
  // variables for package screen
  private String packName;
  
  /** The pack type. */
  private String packType;
  
  /** The dept name. */
  private String deptName;
  
  /** The bed type. */
  private String bedType;
  
  /** The org. */
  private String org;
  
  /** The org id. */
  private String orgId;
  
  /** The org id. */
  private String org_id;
  
  /** The pack id. */
  private String packId;
  
  /** The pack OB id. */
  private String packOBId;
  
  /** The descrip. */
  private String descrip;
  
  /** The operations. */
  private String operations;

  /** The status. */
  private String status;
  
  /** The template. */
  private String template;
  
  /** The allow discount. */
  private boolean allowDiscount;

  /** The charge id. */
  private String[] chargeId;
  
  /** The description. */
  private String[] description;
  
  /** The remarks. */
  private String[] remarks;
  
  /** The rate. */
  private float[] rate;
  
  /** The qty. */
  private float[] qty;
  
  /** The disc. */
  private float[] disc;
  
  /** The amt. */
  private float[] amt;
  
  /** The units. */
  private String[] units;
  
  /** The header. */
  private String[] header;
  
  /** The charge head id. */
  private String[] chargeHeadId;
  
  /** The charge group id. */
  private String[] chargeGroupId;
  
  /** The department id. */
  private String[] departmentId;
  
  /** The charge ref. */
  private String[] chargeRef;
  
  /** The del charge. */
  private boolean[] delCharge;
  
  /** The actid. */
  private String[] actid;
  
  /** The tot amt. */
  private BigDecimal totAmt;

  /**
   * Gets the tot amt.
   *
   * @return the tot amt
   */
  public BigDecimal getTotAmt() {
    return totAmt;
  }

  /**
   * Sets the tot amt.
   *
   * @param totAmt the new tot amt
   */
  public void setTotAmt(BigDecimal totAmt) {
    this.totAmt = totAmt;
  }

  /**
   * Gets the amt.
   *
   * @return the amt
   */
  public float[] getAmt() {
    return amt;
  }

  /**
   * Sets the amt.
   *
   * @param amt          the amt to set
   */
  public void setAmt(float[] amt) {
    this.amt = amt;
  }

  /**
   * Gets the bed type.
   *
   * @return the bedType
   */
  public String getBedType() {
    return bedType;
  }

  /**
   * Sets the bed type.
   *
   * @param bedType          the bedType to set
   */
  public void setBedType(String bedType) {
    this.bedType = bedType;
  }

  /**
   * Gets the charge group id.
   *
   * @return the chargeGroupId
   */
  public String[] getChargeGroupId() {
    return chargeGroupId;
  }

  /**
   * Sets the charge group id.
   *
   * @param chargeGroupId          the chargeGroupId to set
   */
  public void setChargeGroupId(String[] chargeGroupId) {
    this.chargeGroupId = chargeGroupId;
  }

  /**
   * Gets the charge head id.
   *
   * @return the chargeHeadId
   */
  public String[] getChargeHeadId() {
    return chargeHeadId;
  }

  /**
   * Sets the charge head id.
   *
   * @param chargeHeadId          the chargeHeadId to set
   */
  public void setChargeHeadId(String[] chargeHeadId) {
    this.chargeHeadId = chargeHeadId;
  }

  /**
   * Gets the charge id.
   *
   * @return the chargeId
   */
  public String[] getChargeId() {
    return chargeId;
  }

  /**
   * Sets the charge id.
   *
   * @param chargeId          the chargeId to set
   */
  public void setChargeId(String[] chargeId) {
    this.chargeId = chargeId;
  }

  /**
   * Gets the charge ref.
   *
   * @return the chargeRef
   */
  public String[] getChargeRef() {
    return chargeRef;
  }

  /**
   * Sets the charge ref.
   *
   * @param chargeRef          the chargeRef to set
   */
  public void setChargeRef(String[] chargeRef) {
    this.chargeRef = chargeRef;
  }

  /**
   * Gets the del charge.
   *
   * @return the delCharge
   */
  public boolean[] getDelCharge() {
    return delCharge;
  }

  /**
   * Sets the del charge.
   *
   * @param delCharge          the delCharge to set
   */
  public void setDelCharge(boolean[] delCharge) {
    this.delCharge = delCharge;
  }

  /**
   * Gets the department.
   *
   * @return the department
   */
  public String getDepartment() {
    return department;
  }

  /**
   * Sets the department.
   *
   * @param department          the department to set
   */
  public void setDepartment(String department) {
    this.department = department;
  }

  /**
   * Gets the department id.
   *
   * @return the departmentId
   */
  public String[] getDepartmentId() {
    return departmentId;
  }

  /**
   * Sets the department id.
   *
   * @param departmentId          the departmentId to set
   */
  public void setDepartmentId(String[] departmentId) {
    this.departmentId = departmentId;
  }

  /**
   * Gets the dept name.
   *
   * @return the deptName
   */
  public String getDeptName() {
    return deptName;
  }

  /**
   * Sets the dept name.
   *
   * @param deptName          the deptName to set
   */
  public void setDeptName(String deptName) {
    this.deptName = deptName;
  }

  /**
   * Gets the description.
   *
   * @return the description
   */
  public String[] getDescription() {
    return description;
  }

  /**
   * Sets the description.
   *
   * @param description          the description to set
   */
  public void setDescription(String[] description) {
    this.description = description;
  }

  /**
   * Gets the disc.
   *
   * @return the disc
   */
  public float[] getDisc() {
    return disc;
  }

  /**
   * Sets the disc.
   *
   * @param disc          the disc to set
   */
  public void setDisc(float[] disc) {
    this.disc = disc;
  }

  /**
   * Gets the header.
   *
   * @return the header
   */
  public String[] getHeader() {
    return header;
  }

  /**
   * Sets the header.
   *
   * @param header          the header to set
   */
  public void setHeader(String[] header) {
    this.header = header;
  }

  /**
   * Gets the org.
   *
   * @return the org
   */
  public String getOrg() {
    return org;
  }

  /**
   * Sets the org.
   *
   * @param org          the org to set
   */
  public void setOrg(String org) {
    this.org = org;
  }

  /**
   * Gets the package name.
   *
   * @return the packageName
   */
  public String getPackageName() {
    return packageName;
  }

  /**
   * Sets the package name.
   *
   * @param packageName          the packageName to set
   */
  public void setPackageName(String packageName) {
    this.packageName = packageName;
  }

  /**
   * Gets the package type.
   *
   * @return the packageType
   */
  public String getPackageType() {
    return packageType;
  }

  /**
   * Sets the package type.
   *
   * @param packageType          the packageType to set
   */
  public void setPackageType(String packageType) {
    this.packageType = packageType;
  }

  /**
   * Gets the pack name.
   *
   * @return the packName
   */
  public String getPackName() {
    return packName;
  }

  /**
   * Sets the pack name.
   *
   * @param packName          the packName to set
   */
  public void setPackName(String packName) {
    this.packName = packName;
  }

  /**
   * Gets the pack type.
   *
   * @return the packType
   */
  public String getPackType() {
    return packType;
  }

  /**
   * Sets the pack type.
   *
   * @param packType          the packType to set
   */
  public void setPackType(String packType) {
    this.packType = packType;
  }

  /**
   * Gets the qty.
   *
   * @return the qty
   */
  public float[] getQty() {
    return qty;
  }

  /**
   * Sets the qty.
   *
   * @param qty          the qty to set
   */
  public void setQty(float[] qty) {
    this.qty = qty;
  }

  /**
   * Gets the rate.
   *
   * @return the rate
   */
  public float[] getRate() {
    return rate;
  }

  /**
   * Sets the rate.
   *
   * @param rate          the rate to set
   */
  public void setRate(float[] rate) {
    this.rate = rate;
  }

  /**
   * Gets the remarks.
   *
   * @return the remarks
   */
  public String[] getRemarks() {
    return remarks;
  }

  /**
   * Sets the remarks.
   *
   * @param remarks          the remarks to set
   */
  public void setRemarks(String[] remarks) {
    this.remarks = remarks;
  }

  /**
   * Gets the units.
   *
   * @return the units
   */
  public String[] getUnits() {
    return units;
  }

  /**
   * Sets the units.
   *
   * @param units          the units to set
   */
  public void setUnits(String[] units) {
    this.units = units;
  }

  /**
   * Gets the pack id.
   *
   * @return the packId
   */
  public String getPackId() {
    return packId;
  }

  /**
   * Sets the pack id.
   *
   * @param packId          the packId to set
   */
  public void setPackId(String packId) {
    this.packId = packId;
  }

  /**
   * Gets the actid.
   *
   * @return the actid
   */
  public String[] getActid() {
    return actid;
  }

  /**
   * Sets the actid.
   *
   * @param actid          the actid to set
   */
  public void setActid(String[] actid) {
    this.actid = actid;
  }

  /**
   * Gets the pack OB id.
   *
   * @return the packOBId
   */
  public String getPackOBId() {
    return packOBId;
  }

  /**
   * Sets the pack OB id.
   *
   * @param packOBId          the packOBId to set
   */
  public void setPackOBId(String packOBId) {
    this.packOBId = packOBId;
  }

  /**
   * Gets the org id.
   *
   * @return the orgId
   */
  public String getOrgId() {
    return orgId;
  }

  /**
   * Sets the org id.
   *
   * @param orgId          the orgId to set
   */
  public void setOrgId(String orgId) {
    this.orgId = orgId;
  }

  /**
   * Gets the descrip.
   *
   * @return the descrip
   */
  public String getDescrip() {
    return descrip;
  }

  /**
   * Sets the descrip.
   *
   * @param descrip          the descrip to set
   */
  public void setDescrip(String descrip) {
    this.descrip = descrip;
  }

  /**
   * Gets the status.
   *
   * @return the status
   */
  public String getStatus() {
    return status;
  }

  /**
   * Sets the status.
   *
   * @param v the new status
   */
  public void setStatus(String v) {
    status = v;
  }

  /**
   * Gets the template.
   *
   * @return the template
   */
  public String getTemplate() {
    return template;
  }

  /**
   * Sets the template.
   *
   * @param v the new template
   */
  public void setTemplate(String v) {
    template = v;
  }

  /**
   * Gets the sp all.
   *
   * @return the sp all
   */
  public String getSpAll() {
    return spAll;
  }

  /**
   * Sets the sp all.
   *
   * @param spAll the new sp all
   */
  public void setSpAll(String spAll) {
    this.spAll = spAll;
  }

  /**
   * Gets the btype.
   *
   * @return the btype
   */
  public String[] getBtype() {
    return btype;
  }

  /**
   * Sets the btype.
   *
   * @param btype the new btype
   */
  public void setBtype(String[] btype) {
    this.btype = btype;
  }

  /**
   * Gets the organization.
   *
   * @return the organization
   */
  public String[] getOrganization() {
    return organization;
  }

  /**
   * Sets the organization.
   *
   * @param organization the new organization
   */
  public void setOrganization(String[] organization) {
    this.organization = organization;
  }

  /**
   * Gets the pkg name.
   *
   * @return the pkg name
   */
  public String getPkgName() {
    return pkgName;
  }

  /**
   * Sets the pkg name.
   *
   * @param pkgName the new pkg name
   */
  public void setPkgName(String pkgName) {
    this.pkgName = pkgName;
  }

  /**
   * Gets the status all.
   *
   * @return the status all
   */
  public String getStatusAll() {
    return statusAll;
  }

  /**
   * Sets the status all.
   *
   * @param statusAll the new status all
   */
  public void setStatusAll(String statusAll) {
    this.statusAll = statusAll;
  }

  /**
   * Gets the status diag.
   *
   * @return the status diag
   */
  public String getStatusDiag() {
    return statusDiag;
  }

  /**
   * Sets the status diag.
   *
   * @param statusDiag the new status diag
   */
  public void setStatusDiag(String statusDiag) {
    this.statusDiag = statusDiag;
  }

  /**
   * Gets the status ip.
   *
   * @return the status ip
   */
  public String getStatusIp() {
    return statusIp;
  }

  /**
   * Sets the status ip.
   *
   * @param statusIp the new status ip
   */
  public void setStatusIp(String statusIp) {
    this.statusIp = statusIp;
  }

  /**
   * Gets the status op.
   *
   * @return the status op
   */
  public String getStatusOp() {
    return statusOp;
  }

  /**
   * Sets the status op.
   *
   * @param statusOp the new status op
   */
  public void setStatusOp(String statusOp) {
    this.statusOp = statusOp;
  }

  /**
   * Gets the spackage.
   *
   * @return the spackage
   */
  public String getSpackage() {
    return spackage;
  }

  /**
   * Sets the spackage.
   *
   * @param spackage the new spackage
   */
  public void setSpackage(String spackage) {
    this.spackage = spackage;
  }

  /**
   * Gets the stemplate.
   *
   * @return the stemplate
   */
  public String getStemplate() {
    return stemplate;
  }

  /**
   * Sets the stemplate.
   *
   * @param stemplate the new stemplate
   */
  public void setStemplate(String stemplate) {
    this.stemplate = stemplate;
  }

  /**
   * Gets the package active A.
   *
   * @return the package active A
   */
  public String getPackageActiveA() {
    return packageActiveA;
  }

  /**
   * Sets the package active A.
   *
   * @param packageActiveA the new package active A
   */
  public void setPackageActiveA(String packageActiveA) {
    this.packageActiveA = packageActiveA;
  }

  /**
   * Gets the package active all.
   *
   * @return the package active all
   */
  public String getPackageActiveAll() {
    return packageActiveAll;
  }

  /**
   * Sets the package active all.
   *
   * @param packageActiveAll the new package active all
   */
  public void setPackageActiveAll(String packageActiveAll) {
    this.packageActiveAll = packageActiveAll;
  }

  /**
   * Gets the package active I.
   *
   * @return the package active I
   */
  public String getPackageActiveI() {
    return packageActiveI;
  }

  /**
   * Sets the package active I.
   *
   * @param packageActiveI the new package active I
   */
  public void setPackageActiveI(String packageActiveI) {
    this.packageActiveI = packageActiveI;
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
   * Checks if is allow discount.
   *
   * @return true, if is allow discount
   */
  public boolean isAllowDiscount() {
    return allowDiscount;
  }

  /**
   * Sets the allow discount.
   *
   * @param allowDiscount the new allow discount
   */
  public void setAllowDiscount(boolean allowDiscount) {
    this.allowDiscount = allowDiscount;
  }

  /**
   * Gets the package ob id.
   *
   * @return the package ob id
   */
  public String[] getPackageObId() {
    return packageObId;
  }

  /**
   * Sets the package ob id.
   *
   * @param packageObId the new package ob id
   */
  public void setPackageObId(String[] packageObId) {
    this.packageObId = packageObId;
  }

  /**
   * Gets the applicable all.
   *
   * @return the applicable all
   */
  public String getApplicableAll() {
    return applicableAll;
  }

  /**
   * Sets the applicable all.
   *
   * @param applicableAll the new applicable all
   */
  public void setApplicableAll(String applicableAll) {
    this.applicableAll = applicableAll;
  }

  /**
   * Gets the applicable not.
   *
   * @return the applicable not
   */
  public String getApplicableNot() {
    return applicableNot;
  }

  /**
   * Sets the applicable not.
   *
   * @param applicableNot the new applicable not
   */
  public void setApplicableNot(String applicableNot) {
    this.applicableNot = applicableNot;
  }

  /**
   * Gets the applicable org.
   *
   * @return the applicable org
   */
  public String getApplicableOrg() {
    return applicableOrg;
  }

  /**
   * Sets the applicable org.
   *
   * @param applicableOrg the new applicable org
   */
  public void setApplicableOrg(String applicableOrg) {
    this.applicableOrg = applicableOrg;
  }

  /**
   * Gets the org id.
   *
   * @return the org id
   */
  public String getOrg_id() {
    return org_id;
  }

  /**
   * Sets the org id.
   *
   * @param org_id the new org id
   */
  public void setOrg_id(String org_id) {
    this.org_id = org_id;
  }

  /**
   * Gets the operations.
   *
   * @return the operations
   */
  public String getOperations() {
    return operations;
  }

  /**
   * Sets the operations.
   *
   * @param operations the new operations
   */
  public void setOperations(String operations) {
    this.operations = operations;
  }

  /**
   * Gets the activity qty.
   *
   * @return the activity qty
   */
  public int[] getActivity_qty() {
    return activity_qty;
  }

  /**
   * Sets the activity qty.
   *
   * @param activity_qty the new activity qty
   */
  public void setActivity_qty(int[] activity_qty) {
    this.activity_qty = activity_qty;
  }
}
