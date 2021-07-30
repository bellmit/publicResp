package com.insta.hms.stores;

import java.math.BigDecimal;

public class StockIssueBean {
  private String item_id;
  private String item_identifier;
  private String store_id;
  private BigDecimal issue_qty;
  private String issue_type;
  public BigDecimal getIssue_qty() {
    return issue_qty;
  }
  public void setIssue_qty(BigDecimal issue_qty) {
    this.issue_qty = issue_qty;
  }
  public String getItem_id() {
    return item_id;
  }
  public void setItem_id(String item_id) {
    this.item_id = item_id;
  }
  public String getItem_identifier() {
    return item_identifier;
  }
  public void setItem_identifier(String item_identifier) {
    this.item_identifier = item_identifier;
  }
  public String getStore_id() {
    return store_id;
  }
  public void setStore_id(String store_id) {
    this.store_id = store_id;
  }
  public String getIssue_type() {
    return issue_type;
  }
  public void setIssue_type(String issue_type) {
    this.issue_type = issue_type;
  }
}
