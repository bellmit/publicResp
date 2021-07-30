package com.insta.hms.dialysis;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.master.Dialysis.DialLocationMasterDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// TODO: Auto-generated Javadoc
/**
 * The Class DialysisMachinesStatusAction.
 */
public class DialysisMachinesStatusAction extends DispatchAction {
  
  /**
   * List.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the action forward
   * @throws SQLException the SQL exception
   */
  @IgnoreConfidentialFilters
  public ActionForward list(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws SQLException {

    Map filters = new HashMap();

    if (request.getParameter("locationId") != null
        && !request.getParameter("locationId").equals("")) {
      filters.put("locationId", Integer.parseInt(request.getParameter("locationId")));
    }
    filters.put("allocStatus",
        ConversionUtils.getParamAsList(request.getParameterMap(), "allocStatus"));
    filters.put("machineStatus",
        ConversionUtils.getParamAsList(request.getParameterMap(), "machStatus"));

    DialysisMachinesStatusDAO dao = new DialysisMachinesStatusDAO();
    Map listingParams = ConversionUtils.getListingParameter(request.getParameterMap());
    PagedList pagedList = dao.getAllMachinesStatus(filters, listingParams);

    request.setAttribute("locations", DialLocationMasterDAO.getAvalDialLocations());
    request.setAttribute("pagedList", pagedList);

    return mapping.findForward("list");
  }

  /**
   * Mach status screen.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the action forward
   * @throws SQLException the SQL exception
   */
  @IgnoreConfidentialFilters
  public ActionForward machStatusScreen(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws SQLException {

    int machineID = Integer.parseInt(request.getParameter("machineID"));

    BasicDynaBean bean = DialysisMachinesStatusDAO.getStatusDetails(machineID);
    request.setAttribute("bean", bean.getMap());

    return mapping.findForward("statusScreen");
  }

}
