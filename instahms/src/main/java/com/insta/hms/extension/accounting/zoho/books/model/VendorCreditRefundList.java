/* $Id$ */

package com.insta.hms.extension.accounting.zoho.books.model;

import java.util.ArrayList;

/**
 * The Class VendorCreditRefundList.
 */
public class VendorCreditRefundList extends ArrayList<VendorCreditRefund> {

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
