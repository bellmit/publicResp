package com.insta.hms.model;

import org.hibernate.annotations.Immutable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Immutable
@Table(name = "counter_associated_accountgroup_view")
public class CounterAssociatedAccountgroupViewModel implements java.io.Serializable {
  private Integer accountGroupId;
  private String accountGroupName;
  private String counterId;
  private Integer centerId;

  public CounterAssociatedAccountgroupViewModel() {

  }

  @Column(name = "account_group_id")
  public Integer getAccountGroupId() {
    return accountGroupId;
  }

  public void setAccountGroupId(Integer accountGroupId) {
    this.accountGroupId = accountGroupId;
  }

  @Column(name = "account_group_name", length = 200)
  public String getAccountGroupName() {
    return accountGroupName;
  }

  public void setAccountGroupName(String accountGroupName) {
    this.accountGroupName = accountGroupName;
  }

  @Id
  @Column(name = "counter_id", length = 20)
  public String getCounterId() {
    return counterId;
  }

  public void setCounterId(String counterId) {
    this.counterId = counterId;
  }

  @Column(name = "center_id")
  public Integer getCenterId() {
    return centerId;
  }

  public void setCenterId(Integer centerId) {
    this.centerId = centerId;
  }

}
