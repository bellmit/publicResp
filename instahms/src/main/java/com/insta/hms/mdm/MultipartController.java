package com.insta.hms.mdm;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The Interface MultipartController for implementing view end-point and multipart specific
 * functionalities.
 * 
 * @author - tanmay.k
 */
public interface MultipartController {

  /**
   * End-point for viewing the image.
   *
   * @param request
   *          the request
   * @param response
   *          the response
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public void view(HttpServletRequest request, HttpServletResponse response) throws IOException;
}
