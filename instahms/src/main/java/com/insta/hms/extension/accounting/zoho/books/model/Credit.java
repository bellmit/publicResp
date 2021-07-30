/* $Id$ */

package com.insta.hms.extension.accounting.zoho.books.model;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to make an object for credit.
 */

public class Credit {

  /** The creditnote id. */
  private String creditnoteId = "";

  /** The creditnotes invoice id. */
  private String creditnotesInvoiceId = "";

  /** The creditnotes number. */
  private String creditnotesNumber = "";

  /** The credited date. */
  private String creditedDate = "";

  /** The amount applied. */
  private double amountApplied = 0.00;

  /** The invoice payments. */
  private List<InvoicePayment> invoicePayments = new ArrayList<InvoicePayment>();

  /** The apply creditnote. */
  private List<ApplyCreditnote> applyCreditnote = new ArrayList<ApplyCreditnote>();

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
   * @return Returns the creditnote id.
   */

  public String getCreditnoteId() {
    return creditnoteId;
  }

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
   * @return Returns the creditnotes invoice id.
   */

  public String getCreditnotesInvoiceId() {
    return creditnotesInvoiceId;
  }

  /**
   * set the creditnotes number.
   * 
   * @param creditnotesNumber
   *          Number of the creditnote.
   */

  public void setCreditnotesNumber(String creditnotesNumber) {
    this.creditnotesNumber = creditnotesNumber;
  }

  /**
   * get the creditnotes number.
   * 
   * @return Returns the creditnotes number.
   */

  public String getCreditnotesNumber() {
    return creditnotesNumber;
  }

  /**
   * set the credited date.
   * 
   * @param creditedDate
   *          Credited date of the invoice.
   */

  public void setCreditedDate(String creditedDate) {
    this.creditedDate = creditedDate;
  }

  /**
   * get the credited date.
   * 
   * @return Returns the credited date.
   */

  public String getCreditedDate() {
    return creditedDate;
  }

  /**
   * set the amount applied.
   * 
   * @param amountApplied
   *          Amount applied to the invoice.
   */

  public void setAmountApplied(double amountApplied) {
    this.amountApplied = amountApplied;
  }

  /**
   * get the amount applied.
   * 
   * @return Returns the amount applied to the invoice.
   */

  public double getAmountApplied() {
    return amountApplied;
  }

  /**
   * set the invoice payments.
   *
   * @param invoicePayments
   *          Payments applied to an invoice.
   * @throws Exception
   *           the exception
   */

  public void setInvoicePayments(List<InvoicePayment> invoicePayments) throws Exception {
    this.invoicePayments = invoicePayments;
  }

  /**
   * get the invoice payments.
   * 
   * @return Returns list of InvoicePayment object.
   */

  public List<InvoicePayment> getInvoicePayments() {
    return invoicePayments;
  }

  /**
   * set the apply creditnotes.
   *
   * @param applyCreditnote
   *          Credits applied to invoice.
   * @throws Exception
   *           the exception
   */

  public void setApplyCreditnotes(List<ApplyCreditnote> applyCreditnote) throws Exception {
    this.applyCreditnote = applyCreditnote;
  }

  /**
   * get the apply creditnotes.
   * 
   * @return Returns list of ApplyCreditnote object.
   */

  public List<ApplyCreditnote> getApplyCreditnotes() {
    return applyCreditnote;
  }
}
