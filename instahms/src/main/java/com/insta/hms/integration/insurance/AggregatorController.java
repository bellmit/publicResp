package com.insta.hms.integration.insurance;

import com.insta.hms.common.BaseRestController;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.integration.URLRoute;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The Class AggregatorController.
 */
@RestController()
@RequestMapping(URLRoute.AGGREGATORS)
public class AggregatorController extends BaseRestController {

  /** The agg factory. */
  @LazyAutowired
  private InsuranceAggregatorFactory aggFactory;

  /**
   * Gets the aggregator index page.
   *
   * @return the aggregator index page
   */
  @IgnoreConfidentialFilters
  @GetMapping(URLRoute.VIEW_INDEX_URL)
  public ModelAndView getAggregatorIndexPage() {
    return renderMasterUi("Insurance Aggregator Master", "hospitalAdminMasters");
  }

  /**
   * Gets the aggregator details.
   *
   * @param request the request
   * @param response the response
   * @return the aggregator details
   */
  @IgnoreConfidentialFilters
  @RequestMapping(value = "/aggregator", method = RequestMethod.GET)
  public Map<String, Object> getAggregatorDetails(HttpServletRequest request,
      HttpServletResponse response) {
    Map<String, Object> map = new HashMap<String, Object>();
    map.put("aggregatorsList", aggFactory.getAggregators());
    return map;
  }

  /**
   * Gets the services supported.
   *
   * @param request the request
   * @param response the response
   * @return the services supported
   * @throws InstantiationException the instantiation exception
   * @throws IllegalAccessException the illegal access exception
   */
  @RequestMapping(value = "/getServicesSupported", method = RequestMethod.GET)
  public Map<String, Object> getServicesSupported(HttpServletRequest request,
      HttpServletResponse response) throws InstantiationException, IllegalAccessException {
    Map<String, Object> map = new HashMap<String, Object>();
    String aggId = request.getParameter("ia_id");
    List<String> servicesSupported =
        aggFactory.getInsuranceAggregatorInstance(aggId).getSupportedServices();
    map.put("services", servicesSupported);
    return map;

  }

}
