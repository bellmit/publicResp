package com.insta.hms.billing.accounting;

import java.math.BigDecimal;

/**
 * The Class LedgerEntry.
 */
public class LedgerEntry {

  /** The amount. */
  private BigDecimal amount;

  /** The account name. */
  private String accountName;

  /** The type. */
  private String type;

  /** The ledger type. */
  private String ledgerType;

  /** The reference name. */
  private String referenceName;

  /** The is new ref. */
  private Boolean isNewRef;

  /**
   * Gets the account name.
   *
   * @return the account name
   */
  public String getAccountName() {
    return accountName;
  }

  /**
   * Sets the account name.
   *
   * @param accountName
   *          the new account name
   */
  public void setAccountName(String accountName) {
    this.accountName = accountName;
  }

  /**
   * Gets the amount.
   *
   * @return the amount
   */
  public BigDecimal getAmount() {
    return amount;
  }

  /**
   * Sets the amount.
   *
   * @param amount
   *          the new amount
   */
  public void setAmount(BigDecimal amount) {
    this.amount = amount;
  }

  /**
   * Gets the type.
   *
   * @return the type
   */
  public String getType() {
    return type;
  }

  /**
   * Sets the type.
   *
   * @param type
   *          the new type
   */
  public void setType(String type) {
    this.type = type;
  }

  /**
   * Instantiates a new ledger entry.
   *
   * @param type
   *          the type
   * @param ledgerType
   *          the ledger type
   * @param accountName
   *          the account name
   */
  public LedgerEntry(String type, String ledgerType, String accountName) {
    this(type, ledgerType, accountName, BigDecimal.ZERO);
  }

  /**
   * Instantiates a new ledger entry.
   *
   * @param type
   *          the type
   * @param ledgerType
   *          the ledger type
   * @param accountName
   *          the account name
   * @param amt
   *          the amt
   */
  public LedgerEntry(String type, String ledgerType, String accountName, BigDecimal amt) {
    this.amount = amt;
    this.accountName = accountName;
    this.type = type;
    this.ledgerType = ledgerType;
  }

  /**
   * Adds the amount.
   *
   * @param amt
   *          the amt
   */
  public void addAmount(BigDecimal amt) {
    if (null != amt) {
      BigDecimal newAmt = amt;
      BigDecimal total = getAmount();
      if (null != total) {
        newAmt = total.add(amt);
      }
      setAmount(newAmt);
    }
  }

  /**
   * Gets the ledger type.
   *
   * @return the ledger type
   */
  public String getLedgerType() {
    return ledgerType;
  }

  /**
   * Sets the ledger type.
   *
   * @param ledgerType
   *          the new ledger type
   */
  public void setLedgerType(String ledgerType) {
    this.ledgerType = ledgerType;
  }

  /**
   * Gets the reference name.
   *
   * @return the reference name
   */
  public String getReferenceName() {
    return referenceName;
  }

  /**
   * Sets the reference name.
   *
   * @param referenceName
   *          the new reference name
   */
  public void setReferenceName(String referenceName) {
    this.referenceName = referenceName;
  }

  /**
   * Gets the checks if is new ref.
   *
   * @return the checks if is new ref
   */
  public Boolean getIsNewRef() {
    return isNewRef;
  }

  /**
   * Sets the checks if is new ref.
   *
   * @param isNewRef
   *          the new checks if is new ref
   */
  public void setIsNewRef(Boolean isNewRef) {
    this.isNewRef = isNewRef;
  }

}