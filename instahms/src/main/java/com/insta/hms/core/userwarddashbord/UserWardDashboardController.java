package com.insta.hms.core.userwarddashbord;

import com.insta.hms.common.BaseController;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.patient.URLRoute;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import java.text.ParseException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping(value = URLRoute.USER_WARD)
public class UserWardDashboardController extends BaseController {
  @LazyAutowired
  UserWardDashboardService userWardDashboardService;
  static Logger logger = LoggerFactory.getLogger(UserWardDashboardController.class);

  /**
   * List.
   *
   * @param req the req
   * @param resp the resp
   * @return the model and view
   * @throws ParseException the parse exception
   */
  @IgnoreConfidentialFilters
  @RequestMapping(value = { "/list", "" }, method = RequestMethod.GET)
  public ModelAndView list(HttpServletRequest req, HttpServletResponse resp) throws ParseException {
    ModelAndView modelView = new ModelAndView();
    modelView.setViewName(URLRoute.USER_WARD_LIST);
    List allUserList = userWardDashboardService.getAllUsers();
    List allRoleList = userWardDashboardService.getAllRoles();
    modelView.addObject("pagedList",
        userWardDashboardService.getUserDeatilList(req.getParameterMap()));
    modelView.addObject("allUserList", ConversionUtils.listBeanToListMap(allUserList));
    modelView.addObject("allRoleList", ConversionUtils.listBeanToListMap(allRoleList));
    return modelView;
  }
}
