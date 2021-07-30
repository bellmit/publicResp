package com.insta.hms.mdm.ordersets;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * @author manika.singh
 * @since 21/06/19
 */
@Entity
@Table(name = "package_plan_master")
public class PackagePlanMasterModel implements java.io.Serializable {

  private Integer packagePlanId;
  private Integer packId;
  private Integer planId;
  private Character status;

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator =
      "sequence_generator_for_package_plan")
  @SequenceGenerator(name = "sequence_generator_for_package_plan",
      sequenceName = "package_plan_master_seq",
      allocationSize = 1)
  @Column(name = "package_plan_id", unique = true, nullable = false)
  public Integer getPackagePlanId() {
    return packagePlanId;
  }

  public void setPackagePlanId(int packagePlanId) {
    this.packagePlanId = packagePlanId;
  }

  @Column(name = "pack_id")
  public Integer getPackId() {
    return packId;
  }

  public void setPackId(Integer packId) {
    this.packId = packId;
  }

  @Column(name = "plan_id")
  public Integer getPlanId() {
    return planId;
  }

  public void setPlanId(Integer planId) {
    this.planId = planId;
  }

  @Column(name = "status")
  public Character getStatus() {
    return status;
  }

  public void setStatus(Character status) {
    this.status = status;
  }
}
