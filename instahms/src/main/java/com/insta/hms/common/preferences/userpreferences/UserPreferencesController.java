package com.insta.hms.common.preferences.userpreferences;

import com.insta.hms.common.BaseRestController;
import com.insta.hms.common.URLRoute;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.common.annotations.LazyAutowired;

import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping(URLRoute.USER_PREFERENCES)
@SuppressWarnings({ "rawtypes", "unchecked", "unused" })
public class UserPreferencesController extends BaseRestController {

  @LazyAutowired
  UserPreferencesService userPreferencesService;

  @RequestMapping(value = "/update", method = RequestMethod.POST, consumes = "application/json")
  public Map<String, Object> update(HttpServletRequest request, HttpServletResponse response,
      @RequestBody ModelMap requestBody) throws IOException {
    return userPreferencesService.insertPref(requestBody);
  }

  @IgnoreConfidentialFilters
  @RequestMapping(value = "/get", method = RequestMethod.GET)
  public Map<String, Object> get(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    return userPreferencesService.getPref(null);
  }
}
