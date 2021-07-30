package com.insta.hms.integration.practo.doctorprofile;

import com.insta.hms.common.BaseAction;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.DoctorMaster.DoctorMasterDAO;
import flexjson.JSONSerializer;
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
 * The Class DoctorProfileMappingAction.
 *
 * @author insta
 */
public class DoctorProfileMappingAction extends BaseAction {

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(DoctorProfileMappingAction.class);

  /** The mapping service. */
  private DoctorProfileMappingService mappingService = null;
  
  /** The center dao. */
  CenterMasterDAO centerDao = null;
  
  /** The doctor master DAO. */
  DoctorMasterDAO doctorMasterDAO = null;

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
      mappingService = new DoctorProfileMappingService();
    }

    if (centerDao == null) {
      centerDao = new CenterMasterDAO();
    }

    if (doctorMasterDAO == null) {
      doctorMasterDAO = new DoctorMasterDAO();
    }

    Map requestParams = request.getParameterMap();

    PagedList list = mappingService.getDocListing(requestParams);

    request.setAttribute("centerPublishedStatus", mappingService.getCenterPublishStatus(list));
    request.setAttribute("pagedList", list);
    request.setAttribute("centers", centerDao.getAllCentersAndSuperCenterAsFirst());
    JSONSerializer js = new JSONSerializer();
    request.setAttribute("doctorNames", js.serialize(doctorMasterDAO.getAllNames()));
    request.setAttribute("specializations", mappingService.getDoctorSpecializations());

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
  public ActionForward publish(ActionMapping actionMapping, ActionForm actionForm,
      HttpServletRequest request, HttpServletResponse response) throws Exception {

    if (mappingService == null) {
      mappingService = new DoctorProfileMappingService();
    }

    if (centerDao == null) {
      centerDao = new CenterMasterDAO();
    }

    if (doctorMasterDAO == null) {
      doctorMasterDAO = new DoctorMasterDAO();
    }

    Map requestParams = request.getParameterMap();

    PagedList list = mappingService.getDocListing(requestParams);
    mappingService.publishDoctors(requestParams, list);

    FlashScope flash = FlashScope.getScope(request);
    ActionRedirect redirect = new ActionRedirect(
        request.getHeader("Referer").replaceAll("&" + FlashScope.FLASH_KEY + "=[0-9A-Fa-f]+", ""));
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

    return redirect;
  }
}
