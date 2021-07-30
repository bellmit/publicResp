package com.insta.hms.core.wardassignment;

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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Controller
@RequestMapping(value = URLRoute.EDIT_WARD)
public class UserWardAssignmentController extends BaseController {
  @LazyAutowired
  UserWardAssignmentService userWardAssignmentService;
  static Logger logger = LoggerFactory.getLogger(UserWardAssignmentController.class);

  /**
   * Show.
   *
   * @param request the request
   * @param response the response
   * @return the model and view
   */
  @IgnoreConfidentialFilters
  @RequestMapping(value = { "/show", "" }, method = RequestMethod.GET)
  public ModelAndView show(HttpServletRequest request, HttpServletResponse response) {
    ModelAndView modelView = new ModelAndView();
    modelView.setViewName(URLRoute.USER_WARD_SHOW);
    Map params = request.getParameterMap();
    int centerId = (Integer) request.getSession().getAttribute("centerId");
    String empUserName = request.getParameter("empusername");
    final String roleId = request.getParameter("roleId");
    final String roleName = request.getParameter("roleName");
    List wards = Collections.EMPTY_LIST;
    if (empUserName != null && !empUserName.equals("")) {
      modelView.addObject("userWardlist",
          (userWardAssignmentService.getUserWardDetails(empUserName, centerId)));
    }
    wards = userWardAssignmentService.getAllWardNames(centerId);
    modelView.addObject("wards", wards);
    modelView.addObject("wardsJSON", ConversionUtils.listBeanToListMap(wards));
    modelView.addObject("empusername", empUserName);
    modelView.addObject("empRoleId", roleId);
    modelView.addObject("empRoleName", roleName);
    return modelView;
  }

  @RequestMapping(value = "/update", method = RequestMethod.POST)
  protected ModelAndView update(HttpServletRequest request, HttpServletResponse response,
      RedirectAttributes redirectAttributes) throws SQLException, IOException {
    final String empRoleId = request.getParameter("roleId");
    final String empUserName = request.getParameter("empusername");
    final String empRoleName = request.getParameter("roleName");
    userWardAssignmentService.updateUserwards(request.getParameterMap());
    redirectAttributes.addAttribute("empusername", empUserName);
    redirectAttributes.addAttribute("roleId", empRoleId);
    redirectAttributes.addAttribute("roleName", empRoleName);
    ModelAndView modelView = new ModelAndView();
    modelView.setViewName("redirect:show");

    return modelView;
  }

}
