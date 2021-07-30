package com.insta.hms.integration.practo.centerprofile;

import com.insta.hms.common.BaseAction;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.master.CityMaster.CityMasterDAO;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * The Class CenterProfileMappingAction.
 */
public class CenterProfileMappingAction extends BaseAction {
  
  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(CenterProfileMappingAction.class);

  /** The mapping service. */
  CenterProfileMappingService mappingService = null;

  /**
   * Show dashboard.
   *
   * @param actionMapping the action mapping
   * @param actionForm the action form
   * @param request the request
   * @param response the response
   * @return the action forward
   * @throws Exception the exception
   */
  @IgnoreConfidentialFilters
  public ActionForward showDashboard(ActionMapping actionMapping, ActionForm actionForm,
      HttpServletRequest request, HttpServletResponse response) throws Exception {

    if (mappingService == null) {
      mappingService = new CenterProfileMappingService();
    }

    request.setAttribute("pagedList", mappingService.getCentersPagedList(request));
    request.setAttribute("cityStateCountryList", mappingService.getCitiesMasterData());
    request.setAttribute("cityMap", CityMasterDAO.getCityMap());

    return actionMapping.findForward("list");

  }

  /**
   * Publish.
   *
   * @param actionMapping the action mapping
   * @param actionForm the action form
   * @param request the request
   * @param response the response
   * @return the action forward
   * @throws Exception the exception
   */
  @IgnoreConfidentialFilters
  public ActionForward publish(ActionMapping actionMapping, ActionForm actionForm,
      HttpServletRequest request, HttpServletResponse response) throws Exception {

    if (mappingService == null) {
      mappingService = new CenterProfileMappingService();
    }
    PagedList pagedList = mappingService.getCentersPagedList(request);
    Map paramsMap = getParameterMap(request);
    mappingService.publishCenters(paramsMap, pagedList);

    FlashScope flash = FlashScope.getScope(request);
    ActionRedirect redirect = new ActionRedirect(
        request.getHeader("Referer").replaceAll("&" + FlashScope.FLASH_KEY + "=[0-9A-Fa-f]+", ""));
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

    return redirect;

  }

}
