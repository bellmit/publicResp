package com.insta.hms.mdm.centergroup;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.exception.EntityNotFoundException;
import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterController;
import com.insta.hms.mdm.MasterResponseRouter;
import com.insta.hms.mdm.cities.CityService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The Class CenterGroupController.
 */
@Controller
@RequestMapping(URLRoute.CENTER_GROUP)
public class CenterGroupController extends MasterController {
  
  /** The center group service. */
  @LazyAutowired
  private CenterGroupService centerGroupService;
  
  /** The city service. */
  @LazyAutowired
  private CityService cityService;

  /**
   * Instantiates a new center group controller.
   *
   * @param service the service
   */
  public CenterGroupController(CenterGroupService service) {
    super(service, MasterResponseRouter.CENTER_GROUP_ROUTER);
    // TODO Auto-generated constructor stub
  }

  /* (non-Javadoc)
   * @see com.insta.hms.mdm.MasterController#getReferenceLists(java.util.Map)
   */
  @SuppressWarnings("rawtypes")
  @Override
  protected Map<String, List<BasicDynaBean>> getReferenceLists(Map params) {
    return ((CenterGroupService) getService()).getAddEditPageData(params);
  }

  /* (non-Javadoc)
   * @see com.insta.hms.mdm.MasterController#create(
   * javax.servlet.http.HttpServletRequest,
   *  javax.servlet.http.HttpServletResponse, 
   *  org.springframework.web.servlet.mvc.support.RedirectAttributes)
   */
  @RequestMapping(value = "/create", method = RequestMethod.POST)
  protected ModelAndView create(HttpServletRequest request, HttpServletResponse response,
      RedirectAttributes attribs) {
    String appForCenters = request.getParameter("applicable_for_centers");
    Map<String, String[]> parameters = request.getParameterMap();
    HashMap<String, Object> requestValues = new HashMap<String, Object>();
    String[] centerIds = request.getParameterValues("center_id");
    String[] assocIds = request.getParameterValues("center_group_assoc_id");
    String[] assocDeleted = request.getParameterValues("cntr_delete");
    String[] assocEdited = request.getParameterValues("cntr_edited");
    String[] assocStatus = request.getParameterValues("center_status");
    requestValues.put("app_for_centers", appForCenters);
    requestValues.put("centerIds", centerIds);
    requestValues.put("assocIds", assocIds);
    requestValues.put("assocDeleted", assocDeleted);
    requestValues.put("assocEdited", assocEdited);
    requestValues.put("assocStatus", assocStatus);
    BasicDynaBean centerGroupBean = centerGroupService.insertCenterGroup(parameters, requestValues);

    Map centerGroupMap = centerGroupBean.getMap();

    attribs.mergeAttributes(centerGroupMap);
    response.setStatus(HttpStatus.CREATED.value());
    return new ModelAndView(URLRoute.CENTER_GROUP_REDIRECT_TO_SHOW);
  }

  /* (non-Javadoc)
   * @see com.insta.hms.mdm.MasterController#update(
   * javax.servlet.http.HttpServletRequest, 
   * javax.servlet.http.HttpServletResponse, 
   * org.springframework.web.servlet.mvc.support.RedirectAttributes)
   */
  @RequestMapping(value = "/update", method = RequestMethod.POST)
  protected ModelAndView update(HttpServletRequest request, HttpServletResponse response,
      RedirectAttributes attribs) {
    String appForCenters = request.getParameter("applicable_for_centers");
    Map<String, String[]> parameters = request.getParameterMap();
    HashMap<String, Object> requestValues = new HashMap<String, Object>();
    String[] centerIds = request.getParameterValues("center_id");
    String[] assocIds = request.getParameterValues("center_group_assoc_id");
    String[] assocDeleted = request.getParameterValues("cntr_delete");
    String[] assocEdited = request.getParameterValues("cntr_edited");
    String[] assocStatus = request.getParameterValues("center_status");
    requestValues.put("app_for_centers", appForCenters);
    requestValues.put("centerIds", centerIds);
    requestValues.put("assocIds", assocIds);
    requestValues.put("assocDeleted", assocDeleted);
    requestValues.put("assocEdited", assocEdited);
    requestValues.put("assocStatus", assocStatus);
    int success = centerGroupService.updateCenterGroup(parameters, requestValues);
    Integer centerGroupId = Integer.parseInt(request.getParameter("center_group_id"));
    if (success < 1) {
      throw new EntityNotFoundException(
          new String[] { "Center group", "id", centerGroupId.toString() });
    }
    BasicDynaBean centerGroupBean = centerGroupService.getCenterGroup("center_group_id",
        centerGroupId);
    Map centerGroupMap = centerGroupBean.getMap();
    attribs.mergeAttributes(centerGroupMap);
    return new ModelAndView(URLRoute.CENTER_GROUP_REDIRECT_TO_SHOW);
  }

}
