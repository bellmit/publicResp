package com.insta.hms.mdm.countries;

import com.insta.hms.common.PhoneNumberUtil;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.exception.EntityNotFoundException;
import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterController;
import com.insta.hms.mdm.MasterResponseRouter;
import com.insta.hms.mdm.states.StateService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** The Class CountryController. */
@Controller
@RequestMapping(URLRoute.COUNTRY_MASTER_PATH)
public class CountryController extends MasterController {

  /** The country service. */
  @LazyAutowired CountryService countryService;

  /** The state service. */
  @LazyAutowired StateService stateService;

  /**
   * Instantiates a new country controller.
   *
   * @param service the service
   */
  public CountryController(CountryService service) {
    super(service, MasterResponseRouter.COUNTRY_MASTER_ROUTER);
    this.countryService = service;
  }

  @RequestMapping(value = "/add", method = RequestMethod.GET)
  @Override
  public ModelAndView add(HttpServletRequest req, HttpServletResponse resp) {
    ModelAndView mav = new ModelAndView();
    mav.addObject("countryList", PhoneNumberUtil.getAllCountriesWithName());
    mav.setViewName(router.route("add"));
    return mav;
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  @Override
  @RequestMapping(value = "/create", method = RequestMethod.POST)
  public ModelAndView create(
      HttpServletRequest request, HttpServletResponse response, RedirectAttributes redirect) {
    Map<String, String[]> parameters = request.getParameterMap();

    BasicDynaBean countryBean = countryService.insertCountry(parameters);

    Map countryMap = countryBean.getMap();
    redirect.mergeAttributes(countryMap);
    response.setStatus(HttpStatus.CREATED.value());
    return new ModelAndView(URLRoute.Country_MASTER_REDIRECT_TO_SHOW);
  }

  @RequestMapping(value = "/show", method = RequestMethod.GET)
  @Override
  public ModelAndView show(HttpServletRequest req, HttpServletResponse resp) {

    Map params = req.getParameterMap();
    ModelAndView modelView = new ModelAndView();
    BasicDynaBean bean = countryService.findByPk(params);
    modelView.addObject("bean", bean.getMap());
    String countryId = req.getParameter("country_id");
    modelView.addObject("statesList", stateService.getStateList(countryId));
    List<List<String>> countryList = PhoneNumberUtil.getAllCountriesWithName();
    boolean isCountryValid = false;
    for (List<String> country : countryList) {
      if (((String) bean.get("country_name")).equalsIgnoreCase(country.get(1))) {
        isCountryValid = true;
        break;
      }
    }
    modelView.addObject("isCountryValid", isCountryValid);
    modelView.addObject("countryList", countryList);
    modelView.setViewName(router.route("show"));

    return modelView;
  }

  @RequestMapping(value = "/update", method = RequestMethod.POST)
  @Override
  public ModelAndView update(
      HttpServletRequest req, HttpServletResponse resp, RedirectAttributes redirect) {

    Map<String, String[]> parameters = req.getParameterMap();

    int success = countryService.updateCountry(parameters);
    String countryId = parameters.get("country_id")[0];
    if (success < 1) {
      throw new EntityNotFoundException(new String[] {"Country", "id", countryId});
    }

    BasicDynaBean countryBean = null; // updated countryBean
    countryBean = countryService.getCountry("country_id", countryId);

    Map countryMap = countryBean.getMap();
    redirect.mergeAttributes(countryMap);
    return new ModelAndView(URLRoute.Country_MASTER_REDIRECT_TO_SHOW);
  }

  /**
   * Returns the country code corresponding to ISO-2 letter country code EX: returns 91 for IN.
   *
   * @param request the request
   * @param response the response
   * @return the country code from region
   */
  @RequestMapping(value = "/getCountryCodeFromRegion", method = RequestMethod.GET)
  public ModelAndView getCountryCodeFromRegion(
      HttpServletRequest request, HttpServletResponse response) {

    ModelAndView mav = new ModelAndView();
    String regionCode = request.getParameter("region_code");
    mav.addObject("result", PhoneNumberUtil.getCountryCodeForRegion(regionCode));
    return mav;
  }

  /**
   * Gets the nationality.
   *
   * @param request the request
   * @param response the response
   * @return the nationality
   */
  @GetMapping(value = "/getNationality")
  public ModelAndView getNationality(HttpServletRequest request, HttpServletResponse response) {

    ModelAndView mav = new ModelAndView();
    String nationalityCode = request.getParameter("nationality");
    BasicDynaBean nationalityBean = countryService.getNationality(nationalityCode);
    if (nationalityBean != null) {
      mav.addObject("nationality", countryService.getNationality(nationalityCode).getMap());
    }
    return mav;
  }
  
  /**
   * Gets the nationality.
   *
   * @param request the request
   * @return the nationality
   */
  
  @RequestMapping(value = "/lookup", method = RequestMethod.GET)
  @Override
  public ModelMap lookup(HttpServletRequest request) {
    String nationalityCode = request.getParameter("nationality");
    BasicDynaBean nationalityBean = countryService.getNationality(nationalityCode);
    ModelMap modelMap = new ModelMap();
    if (nationalityBean != null) {
      modelMap.addAttribute("nationality", countryService.getNationality(nationalityCode).getMap());
    }
    return modelMap;
  }
}
