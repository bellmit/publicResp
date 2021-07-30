package com.insta.hms.common;

import com.insta.hms.common.confidentialitycheck.ConfidentialityCheckUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class ConfidentialityPatientFilter implements Filter {

  private static Logger logger = LoggerFactory.getLogger(ConfidentialityPatientFilter.class);

  @SuppressWarnings("unchecked")
  private Set<String> getWhiteList(HttpServletRequest request) {
    HttpSession session = request.getSession(false);
    return (Set<String>) session.getServletContext()
        .getAttribute("confidentialityWhiteListedUrlList");
  }

  @Override
  public void destroy() {

  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
      throws IOException, ServletException {
    HttpServletRequest httpRequest = ((HttpServletRequest) request);
    if (!httpRequest.getMethod().equals("GET")) {
      logger.debug("Not a get request;passing it through:" + httpRequest.getServletPath());
      filterChain.doFilter(request, response);
      return;
    }
    ConfidentialityCheckUtil confidentialityCheckUtil = ApplicationContextProvider
        .getBean(ConfidentialityCheckUtil.class);
    String url = httpRequest.getRequestURL().toString().trim();
    url = url.replace(" ", "%20");
    String servletPath = httpRequest.getServletPath();
    if (url.endsWith(".do")) {
      if (url.endsWith("auditlog/AuditLogSearch.do")
          && confidentialityCheckUtil.validateAuditLogUrls(httpRequest)) {
        filterChain.doFilter(request, response);
        return;
      }
      if (isUrlInStrutsWhiteList(httpRequest.getServletPath(), httpRequest)) {
        filterChain.doFilter(request, response);
        return;
      }
    } else if (url.endsWith(".htm") || url.endsWith(".json")) {
      if (isUrlInSpringWhiteList(servletPath, httpRequest)) {
        filterChain.doFilter(request, response);
        return;
      }
    }
    if (url.endsWith(".do") || url.endsWith(".htm") || url.endsWith(".json")) {
      if (isReportOrSettingsUrl(httpRequest)) {
        logger.warn("Allowing URL to pass thru since it's a report/settings url" + url);
        filterChain.doFilter(request, response);
        return;
      }
      if (isUrlPartOfMenu(httpRequest)) {
        logger.debug("Allowing URL to pass thru since it's a menu url" + url);
        filterChain.doFilter(request, response);
        return;
      }
      if ((url.endsWith(".htm") || url.endsWith(".json"))
          && confidentialityCheckUtil.validateRestUrls(httpRequest)) {
        filterChain.doFilter(request, response);
        return;
      }
      Map<String, String[]> queryParams = request.getParameterMap();
      if (queryParams.isEmpty()) {
        logger.warn("Blocked access for url:" + url);
        sendAccessControlForward(request, response);
        return;
      }
      if (confidentialityCheckUtil.validateQueryParams(queryParams, httpRequest)) {
        filterChain.doFilter(request, response);
        return;
      } else {
        logger.warn("Blocked access for url:" + url);
        sendAccessControlForward(request, response);
        return;
      }
    }
    filterChain.doFilter(request, response);
  }

  @SuppressWarnings("unchecked")
  private boolean isUrlPartOfMenu(HttpServletRequest httpRequest) {
    HttpSession session = httpRequest.getSession(false);
    String incomingUrl = httpRequest.getContextPath() + httpRequest.getServletPath();
    if (!StringUtil.isNullOrEmpty(httpRequest.getQueryString())) {
      incomingUrl = incomingUrl + "?" + httpRequest.getQueryString();

    }
    List<String> menuUrlsList = (List<String>) session.getAttribute("menuUrlsList");
    for (String menuUrl : menuUrlsList) {
      if (urlsMatch(incomingUrl, menuUrl, true)) {
        return true;
      }
    }
    return false;
  }

  private boolean urlsMatch(String incomingUrl, String linkUrl, Boolean compareQueryParams) {
    if (incomingUrl.equals(linkUrl)) {
      logger.debug("incomingUrl is same as menuUrl");
      return true;
    }
    try {
      URI incomingURI = new URI(incomingUrl.replace(" ", "%20"));
      URI linkURI = new URI(linkUrl.replace(" ", "%20"));
      if (!(incomingURI.getPath().equals(linkURI.getPath()))) {
        return false;
      } else {
        if (!compareQueryParams) {
          return true;
        }
      }
      MultiValueMap<String, String> incomingParameters = UriComponentsBuilder.fromUri(incomingURI)
          .build().getQueryParams();
      MultiValueMap<String, String> linkParameters = UriComponentsBuilder.fromUri(linkURI).build()
          .getQueryParams();
      linkParameters.isEmpty();
      if (!incomingParameters.isEmpty()) {
        for (String paramName : incomingParameters.keySet()) {
          if (paramName.equals("prgkey") || paramName.equals("prgKey")
              || paramName.equals("pageNum") || paramName.equals("pageSize")) {
            continue;
          }
          List<String> incomingParamValues = new ArrayList<String>(
              incomingParameters.get(paramName));
          if (linkParameters.get(paramName) == null) {
            return false;
          }
          List<String> linkParamValues = new ArrayList<String>(linkParameters.get(paramName));
          Collections.sort(incomingParamValues);
          Collections.sort(linkParamValues);
          if (!(incomingParamValues.equals(linkParamValues))) {
            return false;
          }
        }
      }
      return true;
    } catch (URISyntaxException exception) {
      logger.warn("Error while comparing urls", exception);
      return false;
    }
  }

  @SuppressWarnings("unchecked")
  private boolean isReportOrSettingsUrl(HttpServletRequest httpRequest) {
    HttpSession session = httpRequest.getSession(false);
    String incomingUrl = httpRequest.getContextPath() + httpRequest.getServletPath();
    List<String> reportsUrlList = (List<String>) session.getAttribute("reportsUrlList");
    List<String> settingsUrlList = (List<String>) session.getAttribute("settingsUrlList");
    for (String reportUrl : reportsUrlList) {
      if (urlsMatch(incomingUrl, reportUrl, false)) {
        return true;
      }
    }
    for (String settingUrl : settingsUrlList) {
      if (urlsMatch(incomingUrl, settingUrl, false)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void init(FilterConfig arg0) throws ServletException {

  }

  private void sendAccessControlForward(ServletRequest request, ServletResponse response)
      throws IOException {
    HttpServletRequest httpRequest = (HttpServletRequest) request;
    ((HttpServletResponse) response)
        .sendRedirect(httpRequest.getContextPath() + "/AccessControlForward.do");
  }

  private Boolean isUrlInStrutsWhiteList(String url, HttpServletRequest request) {
    String method = null;
    for (String pattern : getStrutsRegexPatterns()) {
      if (Pattern.compile(pattern).matcher(url.toString()).matches()) {
        logger.debug(
            "Struts Whitelisted servletPath allowed to pass without confidentiality checks:" + url);
        return true;
      }
    }
    StringBuilder urlWithMethodParameter = new StringBuilder(url);
    if (!url.startsWith("/")) {
      urlWithMethodParameter = urlWithMethodParameter.insert(0, "/");
    }

    Map<String, String[]> requestParameters = request.getParameterMap();
    StringBuilder tempUrl = new StringBuilder(urlWithMethodParameter);
    Set<String> whiteListUrlSet = getWhiteList(request);
    if (requestParameters.containsKey("_method")) {
      method = requestParameters.get("_method")[0];
      tempUrl.append("?" + "_method=" + method);
      if (checkInStrutsWhitelist(tempUrl.toString(), whiteListUrlSet)) {
        return true;
      }
      tempUrl = urlWithMethodParameter;
    }
    if (requestParameters.containsKey("method")) {
      method = requestParameters.get("method")[0];
      tempUrl.append("?" + "method=" + method);
      if (checkInStrutsWhitelist(tempUrl.toString(), whiteListUrlSet)) {
        return true;
      }
      tempUrl = urlWithMethodParameter;
    }
    if (checkInStrutsWhitelist(tempUrl.toString(), whiteListUrlSet)) {
      return true;
    }
    return false;
  }

  private Boolean isUrlInSpringWhiteList(String servletPath, HttpServletRequest request) {
    if (!servletPath.contains("scripts/tiny_mce")) {
      servletPath = servletPath.substring(0, servletPath.indexOf("."));
      for (String pattern : getSpringRegexPatterns()) {
        if (Pattern.compile(pattern).matcher(servletPath).matches()) {
          logger.debug(
              "Spring regexPattern servletPath allowed to pass without confidentiality checks:"
                  + servletPath);
          return true;
        }
      }
      if (getWhiteList(request).contains(servletPath)) {
        logger
            .debug("Spring Whitelisted servletPath allowed to pass without confidentiality checks:"
                + servletPath);
        return true;
      }
      return false;
    }
    return true;
  }

  private List<String> getSpringRegexPatterns() {
    List<String> regexPatterns = new ArrayList<>();
    regexPatterns.add("\\/master\\/.*");
    regexPatterns.add(".*[sS]equence[s]*.*");
    regexPatterns.add(".*[rR]eport[s]*.*");
    return regexPatterns;
  }

  private List<String> getStrutsRegexPatterns() {
    List<String> regexPatterns = new ArrayList<>();
    regexPatterns.add(".*[mM]aster[s]*.*");
    regexPatterns.add(".*[sS]equence[s]*.*");
    regexPatterns.add(".*\\/ErrorPageForward.do");
    regexPatterns.add(".*\\/fileUploadSizeError.do");
    regexPatterns.add(".*\\/AccessControlForward.do");
    regexPatterns.add(".*\\/module.do");
    regexPatterns.add(".*\\/loginForm.do");
    regexPatterns.add(".*\\/echoParams.do");
    regexPatterns.add(".*\\/PatientSearchPopup.do");
    regexPatterns.add(".*\\/patientReportAccessFailure.do");
    regexPatterns.add(".*\\/logout.do");
    regexPatterns.add(".*[rR]eport[s]*.*");
    regexPatterns.add(".*Builder[s]*\\.do");
    regexPatterns.add(".*\\/PatientSponsorsApproval.do");
    regexPatterns.add(".*\\/consolidatedbill.do");
    return regexPatterns;
  }

  private boolean checkInStrutsWhitelist(String url, Set<String> whiteList) {
    if (whiteList.contains(url.toString())) {
      logger.debug("Struts Whitelisted servletPath allowed to pass without confidentiality checks:"
          + url.toString());
      return true;
    }
    return false;
  }
}
