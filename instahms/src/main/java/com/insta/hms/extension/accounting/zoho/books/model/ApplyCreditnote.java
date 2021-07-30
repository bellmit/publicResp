/* $Id$ */

package com.insta.hms.extension.accounting.zoho.books.model;

/**
 * This class is used to make an object for apply creditnotes.
 */

public class ApplyCreditnote {

  /** The creditnotes invoice id. */
  private String creditnotesInvoiceId = "";

  /** The creditnote id. */
  private String creditnoteId = "";

  /** The invoice id. */
  private String invoiceId = "";

  /** The amount applied. */
  private double amountApplied = 0.00;

  /**
   * set the creditnotes invoice id.
   * 
   * @param creditnotesInvoiceId
   *          ID of the creditnotes invoice.
   */

  public void setCreditnotesInvoiceId(String creditnotesInvoiceId) {
    this.creditnotesInvoiceId = creditnotesInvoiceId;
  }

  /**
   * get the creditnotes invoice id.
   * 
   * @return Returns the ID of the creditnotes invoice.
   */

  public String getCreditnotesInvoiceId() {
    return creditnotesInvoiceId;
  }

  /**
   * set the creditnote id.
   * 
   * @param creditnoteId
   *          ID of the creditnote.
   */

  public void setCreditnoteId(String creditnoteId) {
    this.creditnoteId = creditnoteId;
  }

  /**
   * get the creditnote id.
   * 
   * @return Returns the ID of the creditnote.
   */

  public String getCreditnoteId() {
    return creditnoteId;
  }

  /**
   * set the invoice id.
   * 
   * @param invoiceId
   *          ID of the invoice.
   */

  public void setInvoiceId(String invoiceId) {
    this.invoiceId = invoiceId;
  }

  /**
   * get the invoice id.
   * 
   * @return Returns the ID of the invoice.
   */

  public String getInvoiceId() {
    return invoiceId;
  }

  /**
   * set the amount applied.
   * 
   * @param amountApplied
   *          Amount applied for the payment.
   */

  public void setAmountApplied(double amountApplied) {
    this.amountApplied = amountApplied;
  }

  /**
   * get the amount applied.
   * 
   * @return Returns the amount applied for the payment.
   */

  public double getAmountApplied() {
    return amountApplied;
  }
}
