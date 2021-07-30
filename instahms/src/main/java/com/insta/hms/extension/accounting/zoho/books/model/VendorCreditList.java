/* $Id$ */

package com.insta.hms.extension.accounting.zoho.books.model;

import java.util.ArrayList;

/**
 * VendorCreditList class is used to create an object for list of vendor credits.
 * 
 * @author ramesh-2099
 *
 */

public class VendorCreditList extends ArrayList<VendorCredit> {

  /** The page context. */
  private PageContext pageContext;

  /**
   * Get the page context.
   * 
   * @return Returns the PageContext object.
   */

  public PageContext getPageContext() {
    return pageContext;
  }

  /**
   * Set the page context.
   * 
   * @param pageContext
   *          PageContext object.
   */

  public void setPageContext(PageContext pageContext) {
    this.pageContext = pageContext;
  }

}
