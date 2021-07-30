/* $Id$ */

package com.insta.hms.extension.accounting.zoho.books.model;

import java.util.ArrayList;

/**
 * The Class SalesOrderList.
 */
public class SalesOrderList extends ArrayList<SalesOrder> {

  /** The page context. */
  private PageContext pageContext;

  /**
   * Get the page context of the sales order list.
   * 
   * @return Returns the PageContext object.
   */

  public PageContext getPageContext() {
    return pageContext;
  }

  /**
   * Set the page context for the sales order list.
   * 
   * @param pageContext
   *          PageContext object.
   */

  public void setPageContext(PageContext pageContext) {
    this.pageContext = pageContext;
  }

}
