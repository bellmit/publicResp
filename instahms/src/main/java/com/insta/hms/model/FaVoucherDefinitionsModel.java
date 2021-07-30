package com.insta.hms.model;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "fa_voucher_definitions")
public class FaVoucherDefinitionsModel implements java.io.Serializable {
  private int id;
  private String voucherKey;
  private String voucherDefinition;
  private String isSubType;
  private String parentKey;
  public FaVoucherDefinitionsModel() {
  }

  public FaVoucherDefinitionsModel(int id) {
    this.id = id;
  }
  
  public FaVoucherDefinitionsModel(int id, String voucherKey, String voucherDefinition, String isSubType,String parentKey  ) {
    this.id = id;
    this.voucherKey = voucherKey;
    this.voucherDefinition = voucherDefinition;
    this.isSubType = isSubType;
    this.parentKey = parentKey;
  }
@Id
@Column(name = "id", unique = true, nullable = false)
  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  @Column(name = "is_sub_type")
  public String getIsSubType() {
    return isSubType;
  }

  public void setIsSubType(String isSubType) {
    this.isSubType = isSubType;
  }
  
  @Column(name = "parent_key")
  public String getParentKey() {
    return parentKey;
  }

  public void setParentKey(String parentKey) {
    this.parentKey = parentKey;
  }
  @Column(name = "voucher_key", length = 100)
  public String getVoucherKey() {
    return voucherKey;
  }

  public void setVoucherKey(String voucherKey) {
    this.voucherKey = voucherKey;
  }
  
  @Column(name = "voucher_definition", length = 15)
  public String getVoucherDefinition() {
    return voucherDefinition;
  }

  public void setVoucherDefinition(String voucherDefinition) {
    this.voucherDefinition = voucherDefinition;
  }

}
