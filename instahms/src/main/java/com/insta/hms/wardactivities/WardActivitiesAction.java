package com.insta.hms.wardactivities;

import com.insta.hms.adminmasters.bedmaster.BedMasterDAO;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// TODO: Auto-generated Javadoc
/**
 * The Class WardActivitiesAction.
 *
 * @author mithun.saha
 */

public class WardActivitiesAction extends DispatchAction {
  
  /** The log. */
  static Logger log = LoggerFactory.getLogger(WardActivitiesAction.class);

  /**
   * List.
   *
   * @param mapping the mapping
   * @param form the form
   * @param req the req
   * @param res the res
   * @return the action forward
   * @throws ServletException the servlet exception
   * @throws Exception the exception
   * @throws ParseException the parse exception
   */
  @IgnoreConfidentialFilters
  public ActionForward list(ActionMapping mapping, ActionForm form, HttpServletRequest req,
      HttpServletResponse res) throws ServletException, Exception, ParseException {
    Map params = req.getParameterMap();
    WardActivitiesDAO dao = new WardActivitiesDAO();
    PagedList pagedList = dao.getPendingActivities(params,
        ConversionUtils.getListingParameter(params));
    int centerId = (Integer) req.getSession().getAttribute("centerId");
    boolean multiCentered = GenericPreferencesDAO.getGenericPreferences()
        .getMax_centers_inc_default() > 1;

    req.setAttribute("wards", BedMasterDAO.getAllWardNames(centerId, multiCentered));
    req.setAttribute("pagedList", pagedList);

    return mapping.findForward("list");
  }

}
