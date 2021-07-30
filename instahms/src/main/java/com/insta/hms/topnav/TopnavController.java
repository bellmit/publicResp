package com.insta.hms.topnav;

import com.insta.hms.common.BaseRestController;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.mdm.centers.CenterService;
import com.insta.hms.mdm.role.RoleMasterService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import java.sql.SQLException;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping(URLRoute.TOPNAV_URL)
public class TopnavController extends BaseRestController {
  
  @LazyAutowired
  CenterService centerService;
  @LazyAutowired
  RoleMasterService rolemasterService;
  
  @IgnoreConfidentialFilters
  @GetMapping(URLRoute.VIEW_INDEX_URL)
  public ModelAndView getTopnavIndex() {
    return new ModelAndView(URLRoute.TOPNAV_INDEX_PATH);
  }
  
  /**
   * Centers inc default.
   *
   * @param request the request
   * @param response the response
   * @return the map
   */
  @GetMapping(value = "/incDefault")
  public Map centersIncDefault(HttpServletRequest request, 
      HttpServletResponse response) {
    return centerService.centersIncludeDefault();
  }
  
  /**
   * Roles.
   *
   * @return the map
   * @throws SQLException the SQL exception
   */
  @GetMapping(value = "/admin")
  public Map<String, Object> roles() throws SQLException {
    return rolemasterService.roles();
  }
}
