package com.insta.hms.api;

import com.bob.hms.common.RequestContext;
import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.MessageUtil;
import com.insta.instaapi.common.ApiUtil;
import com.insta.instaapi.common.JsonProcessor;

import flexjson.JSONSerializer;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class RequestHandlerKeyCheckFilter implements javax.servlet.Filter {
  ServletContext servletContext;
  String excludePatterns;

  public void init(FilterConfig filterConfig) throws ServletException {
    servletContext = filterConfig.getServletContext();
    this.excludePatterns = filterConfig.getInitParameter("excludePatterns");
  }

  @Override
  public void destroy() {
    // added unimplemented method
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse,
   * javax.servlet.FilterChain)
   */
  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    Set<String> excludePatternSet = new HashSet<String>();
    String url = ((HttpServletRequest) request).getRequestURI();
    if (StringUtils.isNotBlank(excludePatterns)) {
      excludePatternSet.addAll((Arrays.asList(excludePatterns.split(","))));
    }
    for (String excludePattern : excludePatternSet) {
      if (url.contains(excludePattern)) {
        chain.doFilter(request, response);
        return;
      }
    }
    String requestHandalerKey = ApiUtil.getRequestHandlerKey((HttpServletRequest) request);
    boolean isAValidRequest = false;
    Map<String, Object> sessionMap = (Map<String, Object>) servletContext
        .getAttribute("sessionMap");
    Map<String, Object> sessionParameters = null;
    java.sql.Timestamp loginTime = null;
    MessageUtil messageutil = ApplicationContextProvider.getBean(MessageUtil.class);
    String tokenValidation = messageutil.getMessage("token.validation.duration", null);
    int validDuration = Integer.parseInt(tokenValidation);
    // getting current time
    Calendar calendar = Calendar.getInstance();
    java.util.Date now = calendar.getTime();
    java.sql.Timestamp currentTime = new java.sql.Timestamp(now.getTime());
    String userName = null;
    String hospitalName = null;
    if (sessionMap != null && !sessionMap.isEmpty() && !requestHandalerKey.equals("")) {
      sessionParameters = (Map) sessionMap.get(requestHandalerKey);
      if (sessionParameters != null && !sessionParameters.isEmpty()) {
        loginTime = (java.sql.Timestamp) sessionParameters.get("login_time");
        userName = (String) sessionParameters.get("customer_user_id");
        hospitalName = (String) sessionParameters.get("hospital_name");
        if ((currentTime.getTime() - loginTime.getTime()) / 60000 <= validDuration) {
          isAValidRequest = true;
        }
      }
    } else {
      // check for patient centric api's print url's which doesn't have request handler key
      HttpSession session = ((HttpServletRequest) request).getSession(false);
      if (session != null && session.getAttribute("mobile_user_id") != null) {
        RequestContext.setRequest(request);
        chain.doFilter(request, response);
        return;
      }

    }
    if (isAValidRequest) {
      RequestContext.setConnectionDetails(new String[] { "", "", hospitalName, userName, "0", null,
          null, request.getLocale().toString() });
      chain.doFilter(request, response);
    } else {
      Map<String, Object> schedulerMasterDataMap = new HashMap<>();
      String successMsg = messageutil.getMessage("api.invalid.key", null);
      schedulerMasterDataMap.put("return_code", "1001");
      schedulerMasterDataMap.put("return_message", successMsg);
      ((HttpServletResponse) response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
      JSONSerializer js = JsonProcessor.getJSONParser();
      response.getWriter().write(js.deepSerialize(schedulerMasterDataMap));
      response.flushBuffer();
    }

  }

}
