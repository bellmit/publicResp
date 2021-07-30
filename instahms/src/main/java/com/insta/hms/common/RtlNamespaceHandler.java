package com.insta.hms.common;

import org.w3c.dom.Element;
import org.xhtmlrenderer.simple.extend.XhtmlNamespaceHandler;

public class RtlNamespaceHandler extends XhtmlNamespaceHandler {

  private static String RIGHT = "right";
  private static String LEFT = "left";

  @Override
  protected String getAttribute(Element element, String attrName) {
    if (null != attrName) {
      // We wont differentiate the attributes. We are only concerned with values
      // being flipped whatever be the attribute.
      String value = element.getAttribute(attrName);
      if (null != value) {
        if (value.equalsIgnoreCase(LEFT)) {
          return RIGHT;
        }
        if (value.equalsIgnoreCase(RIGHT)) {
          return LEFT;
        }
      }
    }
    return super.getAttribute(element, attrName);
  }

}
