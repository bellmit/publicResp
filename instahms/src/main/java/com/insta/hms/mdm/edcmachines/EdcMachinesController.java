package com.insta.hms.mdm.edcmachines;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterRestController;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping(URLRoute.EDC_MACHINES_PATH)
public class EdcMachinesController extends MasterRestController {

  @LazyAutowired private EdcMachinesService service;

  public EdcMachinesController(EdcMachinesService service) {
    super(service);
    // TODO Auto-generated constructor stub
  }

  /**
   * List of edc machine by center.
   *
   * @param request HttpServletRequest
   * @param response HttpServletResponse
   * @return ResponseEntity
   */
  @RequestMapping(
      value = {"/listbycenter", ""},
      method = RequestMethod.GET
  )
  public ResponseEntity<Map<String, Object>> listByCenter(
      HttpServletRequest request, HttpServletResponse response) {
    Map<String, Object> responseMap = new HashMap<String, Object>();
    Map paramMap = request.getParameterMap();
    PagedList pagedList =
        service.search(paramMap, ConversionUtils.getListingParameter(paramMap), true);
    responseMap.put("paged_list", pagedList);
    return new ResponseEntity<Map<String, Object>>(responseMap, HttpStatus.OK);
  }

  @GetMapping(URLRoute.MASTER_INDEX_URL)
  public ModelAndView getEdcMachineIndexPage() {
    return renderMasterUi("Master", "billingPreferences");
  }
}
