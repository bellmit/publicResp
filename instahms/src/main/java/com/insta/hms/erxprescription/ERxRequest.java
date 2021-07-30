package com.insta.hms.erxprescription;

import com.insta.hms.eservice.ERequest;

public class ERxRequest extends ERequest {

  /**
   * Note: This method should optionally return a map object containing any special data required by
   * the FTL Header template. Whatever is returned by this method will be available to the FTL
   * Header template as "header" However this is not mandatory, since the entire request object is
   * anyway available to all of the templates
   */
  @Override
  public Object getFooter() {
    return super.getFooter();
  }

  /**
   * Note: This method should optionally return a map object containing any special data required by
   * the FTL Footer template. Whatever is returned by this method will be available to the FTL
   * Footer template as "footer" However this is not mandatory, since the entire request object is
   * anyway available to all of the templates
   */
  @Override
  public Object getHeader() {
    return super.getHeader();
  }

}
