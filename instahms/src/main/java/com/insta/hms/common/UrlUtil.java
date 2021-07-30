package com.insta.hms.common;

import com.bob.hms.common.RequestContext;
import com.bob.hms.common.ScreenRightsHelper;

import org.springframework.web.util.UriComponentsBuilder;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

// TODO: Auto-generated Javadoc
/**
 * The Class UrlUtil.
 *
 * @author aditya
 */
public class UrlUtil {

  /** The Constant SHOW_URL_VALUE. */
  public static final String SHOW_URL_VALUE = "show";

  /** The Constant ADD_URL_VALUE. */
  public static final String ADD_URL_VALUE = "add";

  /** The Constant LIST_URL_VALUE. */
  public static final String LIST_URL_VALUE = "list";

  /** The Constant CREATE_URL_VALUE. */
  public static final String CREATE_URL_VALUE = "create";

  /** The Constant UPDATE_URL_VALUE. */
  public static final String UPDATE_URL_VALUE = "update";

  /** The Constant DELETE_URL_VALUE. */
  public static final String DELETE_URL_VALUE = "delete";

  /** The Constant LOOKUP_URL_VALUE. */
  public static final String LOOKUP_URL_VALUE = "lookup";

  /** The Constant DEFAULT_ENCODING. */
  private static final String DEFAULT_ENCODING = "UTF-8";

  /**
   * Params to map to add to redirectAttributes.
   *
   * @param queryParams the query params
   * @return the map of queryParams
   * @throws URISyntaxException the URI syntax exception
   */
  public static Map<String, String> paramsToMap(String queryParams) throws URISyntaxException {

    String query = queryParams;

    // ignore the initial questionMark in the queryParams
    if (null != query && !query.isEmpty() && query.charAt(0) == '?') {
      query = query.substring(1);
    }
    Map<String, String> map = new LinkedHashMap<String, String>();

    // putting query parameters as a key value map and sending them as
    // redirectAttributes
    if (query != null && !query.isEmpty()) {
      String[] params = query.split("&");
      for (int i = 0; i < params.length; i++) {
        String[] keyValue = params[i].split("=");
        if (keyValue.length > 1) {
          map.put(keyValue[0], keyValue[1]);
        }
      }
    }
    return map;
  }

  /**
   * Get the URL of the referrer.
   *
   * @param request the request
   * @return the URL as a string of the referrer of the Request
   */
  public static String redirectToReferer(HttpServletRequest request) {
    if (request.getHeader("referer") == null) {
      return null;
    }
    String url = request.getHeader("referer");
    int endOfUrl = url.indexOf("?");// removing query String
    if (endOfUrl != -1) {
      url = url.substring(0, endOfUrl);
    }
    if (checkValidRedirectUrl(url, request)) {
      return "redirect:" + url;
    }
    return null;
  }

  /**
   * Builds the URL. Takes into consideration the three different use cases i.e. React pages,
   * Spring + JSP pages and Struts + JSP pages.
   * If a hash fragment is present, then the page is considered
   * to be a react migrated page. To check if a page is migrated from Struts, the list of migrated
   * action IDs is retrieved from the Session.
   *
   * @param actionID     the action ID
   * @param methodName   the method name
   * @param queryString  the query string
   * @param hashFragment the hash fragment used in React Pages e.g. #/regions/list
   * @param qualifier    the qualifier such as used on show pages #/regions/show/7 with 7 being the
   *                     qualifier.
   * @return the URL string
   */
  public static String buildURL(String actionID, String methodName, String queryString,
      String hashFragment, String qualifier) {
    return buildURL(actionID, methodName, queryString, hashFragment, qualifier, true);
  }

  /**
   * Builds the URL. Takes into consideration the three different use cases i.e. React pages, Spring
   * + JSP pages and Struts + JSP pages. If a hash fragment is present, then the page is considered
   * to be a react migrated page. To check if a page is migrated from Struts, the list of migrated
   * action IDs is retrieved from the Session.
   *
   * @param actionID      the action ID
   * @param methodName    the method name
   * @param queryString   the query string
   * @param hashFragment  the hash fragment used in React Pages e.g. #/regions/list
   * @param qualifier     the qualifier such as used on show pages #/regions/show/7 with 7 being the
   *                      qualifier.
   * @param appendContext appends context path if true. Defaults to true.
   * @return the URL string
   */
  public static String buildURL(String actionID, String methodName, String queryString,
      String hashFragment, String qualifier, Boolean appendContext) {

    if (null == actionID) {
      return null;
    }

    ServletContext context = RequestContext.getSession().getServletContext();
    List<String> migratedActionIDs = (List<String>) context.getAttribute("migratedActions");
    Map<String, String> actionURLMap = (Map<String, String>) context.getAttribute("actionUrlMap");

    StringBuilder actionURL = new StringBuilder(actionURLMap.get(actionID));
    UriComponentsBuilder uriBuilder = appendContext
        ? UriComponentsBuilder.fromPath(RequestContext.getHttpRequest().getContextPath())
        : UriComponentsBuilder.newInstance();

    MenuItem menuEntry = ScreenRightsHelper.getMenuItem(actionID);

    String queryStringWithoutQuestionMark = null;
    if (null != queryString) {
      Integer lastIndexOfQuestionMark = queryString.lastIndexOf("?");
      if (lastIndexOfQuestionMark == 0) { // for saved search we are saving with ? at first
        queryStringWithoutQuestionMark = queryString.substring(1);
      } else { 
        queryStringWithoutQuestionMark = -1 != lastIndexOfQuestionMark
            ? queryString.substring(lastIndexOfQuestionMark)
            : queryString;
      }
    }

    if (migratedActionIDs.contains(actionID)) {
      // Check for react migrated pages
      if (null != hashFragment) {
        if (Arrays.asList("new_op_registration", "new_discharge_summary",
            "doc_scheduler_calender_view", "doc_scheduler_available_slots", "new_op_order",
            "new_ip_order", "new_op_bill", "insurance_aggregator", "mas_order_sets",
            "mas_edc_machine", "fp_registration", "fp_verification", "new_cons", "new_triage",
            "new_ipemr", "new_ip_bill", "mas_clinical_preferences", "new_op_appointment",
            "mas_note_types","mas_data_backload",
            "new_mas_vitalparameters", "mas_practitioner_types",
            "new_initial_assessment", "mas_case_rate", "mas_hosp_roles", "mas_code_type",
            "mas_billing_group", "mas_confidentiality_groups", "mas_reason_for_referral",
            "mas_pending_presc_declined_reasons", "mas_indication_for_caesarean_section",
            "remittance_reconciliation","patient_pending_prescription","mas_review_types",
            "mas_review_category","store_stock_take_list", "mas_packages", "mas_salucro_role", 
            "mas_salucro_location", "salucro_transactions", "salucro_reports")
            .contains(actionID)) {
          uriBuilder.pathSegment(actionURL.toString() + "/index.htm");
        } else {

          uriBuilder.pathSegment("index/" + menuEntry.getModule() + ".htm");
        }
        StringBuilder hash = new StringBuilder(hashFragment);

        if (null != qualifier && !(qualifier.isEmpty())) {
          hash.append("/").append(qualifier);

        }

        uriBuilder.fragment(hash.toString());
      } else {
        // Spring + JSP
        if (null != methodName) {
          actionURL.append("/").append(methodName);
        }
        uriBuilder.pathSegment(actionURL.append(".htm").toString());
        if (null != queryStringWithoutQuestionMark && !queryStringWithoutQuestionMark.isEmpty()) {
          uriBuilder.query(queryStringWithoutQuestionMark);
        }
      }
    } else {
      // Struts + JSP
      uriBuilder.pathSegment(actionURL.toString());
      if (null != queryStringWithoutQuestionMark) {
        uriBuilder.query(queryStringWithoutQuestionMark);
      }

      /*
       * Commented due to _method and method if (null != methodName) {
       * uriBuilder.replaceQueryParam("_method", methodName); }
       */
    }

    return uriBuilder.build().toString();
  }

  /**
   * Ensure that the url is valid for redirection: 1. Host of the request should be same as the url
   * host 2. url ends with .htm or .json or .do
   *
   * @param url     the url
   * @param request the request
   * @return the boolean
   */
  public static Boolean checkValidRedirectUrl(String url, HttpServletRequest request) {
    return checkHostOfRedirectUrl(url, request)
        && (url.endsWith(".htm") || url.endsWith(".json") || url.endsWith(".do"));
  }

  /**
   * Check host of redirect url.
   *
   * @param url     the url
   * @param request the request
   * @return the boolean
   */
  private static Boolean checkHostOfRedirectUrl(String url, HttpServletRequest request) {
    URI uri;
    try {
      uri = new URI(url);
    } catch (URISyntaxException exception) {
      return false;
    }
    String requestHost = request.getServerName();
    String host = uri.getHost();
    if (null == requestHost || null == host) {
      return false;
    }
    return host.equals(requestHost);
  }

  /**
   * Builds the query string.
   *
   * @param map the map
   * @return the string
   */
  public static String buildQueryString(final LinkedHashMap<String, Object> map) {
    try {
      final Iterator<Map.Entry<String, Object>> it = map.entrySet().iterator();
      final StringBuilder sb = new StringBuilder(map.size() * 8);
      while (it.hasNext()) {
        final Map.Entry<String, Object> entry = it.next();
        final String key = entry.getKey();
        if (key != null) {
          sb.append(URLEncoder.encode(key, DEFAULT_ENCODING));
          sb.append('=');
          final Object value = entry.getValue();
          final String valueAsString = value != null
              ? URLEncoder.encode(value.toString(), DEFAULT_ENCODING)
              : "";
          sb.append(valueAsString);
          if (it.hasNext()) {
            sb.append('&');
          }
        }
      }
      return sb.toString();
    } catch (final UnsupportedEncodingException ex) {
      throw new UnsupportedOperationException(ex);
    }
  }
}
