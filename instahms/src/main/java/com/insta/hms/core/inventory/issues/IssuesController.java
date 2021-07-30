package com.insta.hms.core.inventory.issues;

import com.insta.hms.common.BusinessController;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.inventory.URLRoute;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Used for all issues related operations.
 *
 * @author irshadmohammed
 */
@Controller
@RequestMapping(URLRoute.ISSUES)
public class IssuesController extends BusinessController {
  
  /** The issues service. */
  @LazyAutowired
  private IssuesService issuesService;

  /**
   * Gets the selling price.
   *
   * @param request the request
   * @param response the response
   * @return the selling price
   * @throws Exception the exception
   */
  @IgnoreConfidentialFilters
  @RequestMapping(value = URLRoute.ISSUES_GET_MARKUP_RATE, method = RequestMethod.GET)
  public ModelAndView getSellingPrice(HttpServletRequest request, HttpServletResponse response)
      throws Exception {
    Map<String, Object> reqMap = ConversionUtils.flatten(request.getParameterMap());
    reqMap.put("center_id", request.getSession(false).getAttribute("centerId"));
    BasicDynaBean bean = issuesService.resolveIssuePrice(reqMap);

    ModelAndView mav = new ModelAndView();
    mav.addObject("sellingPriceBean", bean.getMap());
    return mav;
  }
}
