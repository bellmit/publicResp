/* $Id$ */

package com.insta.hms.extension.accounting.zoho.books.model;

import java.util.ArrayList;

/**
 * This class is used to create an object for bank transaction list.
 */

public class TransactionList extends ArrayList<Transaction> {

  /** The page context. */
  private PageContext pageContext = new PageContext();

  /** The instrumentation. */
  private Instrumentation instrumentation = new Instrumentation();

  /**
   * set the page context.
   *
   * @param pageContext
   *          PageContext object.
   * @throws Exception
   *           the exception
   */

  public void setPageContext(PageContext pageContext) throws Exception {
    this.pageContext = pageContext;
  }

  /**
   * get the page context.
   * 
   * @return Returns the PageContext object.
   */

  public PageContext getPageContext() {
    return pageContext;
  }

  /**
   * set the instrumentation.
   *
   * @param instrumentation
   *          Instrumentation object.
   * @throws Exception
   *           the exception
   */

  public void setInstrumentation(Instrumentation instrumentation) throws Exception {
    this.instrumentation = instrumentation;
  }

  /**
   * get the instrumentation.
   * 
   * @return Returns the Instrumentation object.
   */

  public Instrumentation getInstrumentation() {
    return instrumentation;
  }

}
