package com.insta.hms.billing.accounting;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * The Class Voucher.
 *
 * @author krishna.t
 */
public class Voucher {

  /** The date. */
  private Date date;

  /** The formatted date. */
  private String formattedDate;

  /** The voucher type. */
  private String voucherType;

  /** The remote id. */
  private String remoteId;

  /** The action. */
  private String action;

  /** The guid. */
  private String guid;

  /** The narration. */
  private String narration;

  /** The voucher type name. */
  private String voucherTypeName;

  /** The voucher number. */
  private String voucherNumber;

  /** The effective date. */
  private String effectiveDate;

  /** The ledger list. */
  private List<Map> ledgerList = new ArrayList<Map>();

  /**
   * Gets the date.
   *
   * @return the date
   */
  public Date getDate() {
    return date;
  }

  /**
   * Sets the date.
   *
   * @param date
   *          the new date
   */
  public void setDate(Date date) {
    this.date = date;
  }

  /**
   * Gets the effective date.
   *
   * @return the effective date
   */
  public String getEffectiveDate() {
    return effectiveDate;
  }

  /**
   * Sets the effective date.
   *
   * @param effectiveDate
   *          the new effective date
   */
  public void setEffectiveDate(String effectiveDate) {
    this.effectiveDate = effectiveDate;
  }

  /**
   * Gets the formatted date.
   *
   * @return the formatted date
   */
  public String getFormattedDate() {
    return formattedDate;
  }

  /**
   * Sets the formatted date.
   *
   * @param formattedDate
   *          the new formatted date
   */
  public void setFormattedDate(String formattedDate) {
    this.formattedDate = formattedDate;
  }

  /**
   * Gets the guid.
   *
   * @return the guid
   */
  public String getGuid() {
    return guid;
  }

  /**
   * Sets the guid.
   *
   * @param guid
   *          the new guid
   */
  public void setGuid(String guid) {
    this.guid = guid;
  }

  /**
   * Gets the ledger list.
   *
   * @return the ledger list
   */
  public List<Map> getLedgerList() {
    return ledgerList;
  }

  /**
   * Sets the ledger list.
   *
   * @param ledgerList
   *          the new ledger list
   */
  public void setLedgerList(List<Map> ledgerList) {
    this.ledgerList = ledgerList;
  }

  /**
   * Gets the narration.
   *
   * @return the narration
   */
  public String getNarration() {
    return narration;
  }

  /**
   * Sets the narration.
   *
   * @param narration
   *          the new narration
   */
  public void setNarration(String narration) {
    this.narration = narration;
  }

  /**
   * Gets the voucher type.
   *
   * @return the voucher type
   */
  public String getVoucherType() {
    return voucherType;
  }

  /**
   * Sets the voucher type.
   *
   * @param voucherType
   *          the new voucher type
   */
  public void setVoucherType(String voucherType) {
    this.voucherType = voucherType;
  }

  /**
   * Gets the voucher type name.
   *
   * @return the voucher type name
   */
  public String getVoucherTypeName() {
    return voucherTypeName;
  }

  /**
   * Sets the voucher type name.
   *
   * @param voucherTypeName
   *          the new voucher type name
   */
  public void setVoucherTypeName(String voucherTypeName) {
    this.voucherTypeName = voucherTypeName;
  }

  /**
   * Gets the action.
   *
   * @return the action
   */
  public String getAction() {
    return action;
  }

  /**
   * Sets the action.
   *
   * @param action
   *          the new action
   */
  public void setAction(String action) {
    this.action = action;
  }

  /**
   * Gets the remote id.
   *
   * @return the remote id
   */
  public String getRemoteId() {
    return remoteId;
  }

  /**
   * Sets the remote id.
   *
   * @param remoteId
   *          the new remote id
   */
  public void setRemoteId(String remoteId) {
    this.remoteId = remoteId;
  }

  /**
   * Gets the voucher number.
   *
   * @return the voucher number
   */
  public String getVoucherNumber() {
    return voucherNumber;
  }

  /**
   * Sets the voucher number.
   *
   * @param voucherNumber
   *          the new voucher number
   */
  public void setVoucherNumber(String voucherNumber) {
    this.voucherNumber = voucherNumber;
  }

}
