package com.insta.hms.integration;

import com.bob.hms.common.RequestContext;
import com.insta.hms.common.BaseRestController;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

public abstract class AbstractRedirectionController extends BaseRestController {

  protected void redirect(HttpServletResponse resp, String url) throws IOException {
    resp.sendRedirect(RequestContext.getHttpRequest().getContextPath() + url);
  }

}
