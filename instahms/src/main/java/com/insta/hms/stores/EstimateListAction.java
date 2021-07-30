/**
 *
 */
package com.insta.hms.stores;

import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class EstimateListAction extends BaseAction {

  /*
   * Search for a list of estimates
   */
  @IgnoreConfidentialFilters
  public ActionForward search(ActionMapping mapping, ActionForm f, HttpServletRequest req,
      HttpServletResponse res) throws Exception {

    PagedList list = MedicineSalesDAO.searchEstimates(req.getParameterMap(),
        ConversionUtils.getListingParameter(req.getParameterMap()));
    req.setAttribute("pagedList", list);
    return mapping.findForward("showlist");
  }

  /*
   * Retrieve the basic search screen only: no search executed.
   */
  @IgnoreConfidentialFilters
  public ActionForward getScreen(ActionMapping mapping, ActionForm f, HttpServletRequest request,
      HttpServletResponse response) throws Exception {
    return mapping.findForward("showlist");
  }

}
