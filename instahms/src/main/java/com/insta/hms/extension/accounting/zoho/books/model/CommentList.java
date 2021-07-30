/* $Id$ */

package com.insta.hms.extension.accounting.zoho.books.model;

import java.util.ArrayList;

/**
 * This class is used to create an object for comments list.
 */

public class CommentList extends ArrayList<Comment> {

  /** The page context. */
  private PageContext pageContext = new PageContext();

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

}
