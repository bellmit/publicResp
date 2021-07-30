package com.insta.hms.insurance;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.master.ReferalDoctor.ReferalDoctorDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The Class InsuranceDashboardAction.
 *
 * @author pragna.p
 */
public class InsuranceDashboardAction extends DispatchAction {

  /**
   * List.
   *
   * @param mapping    the mapping
   * @param actionForm the f
   * @param req        the req
   * @param response   the response
   * @return the action forward
   * @throws Exception the exception
   */
  @IgnoreConfidentialFilters
  public ActionForward list(ActionMapping mapping, ActionForm actionForm, HttpServletRequest req,
      HttpServletResponse response) throws Exception {

    req.setAttribute("referalDetails", ReferalDoctorDAO.getReferencedoctors());

    HashMap filter = new HashMap();
    filter.put("mr_no", req.getParameter("mr_no"));
    filter.put("name", req.getParameter("name"));
    filter.put("tpa_id", ConversionUtils.getParamAsList(req.getParameterMap(), "tpa_id"));
    filter.put("insurance_id", req.getParameter("insurance_id"));
    // filter.put("tpa_no", req.getParameter("tpa_no"));
    filter.put("status", ConversionUtils.getParamAsList(req.getParameterMap(), "status"));
    filter.put("visit_type", ConversionUtils.getParamAsList(req.getParameterMap(), "visit_type"));
    filter.put("gen_reg_date0", DataBaseUtil.parseDate(req.getParameter("gen_reg_date0")));
    filter.put("gen_reg_date1", DataBaseUtil.parseDate(req.getParameter("gen_reg_date1")));

    PagedList list = InsuranceDAO.getAllInsuranceCases(filter,
        ConversionUtils.getListingParameter(req.getParameterMap()));

    ArrayList<BasicDynaBean> dtolist = (ArrayList<BasicDynaBean>) list.getDtoList();
    for (BasicDynaBean dto : dtolist) {
      String visitId = (String) dto.get("patient_id");
      if (visitId != null && !visitId.equals("")) {
        String billNo = InsuranceDAO.getCaseBillsForMainVisit(visitId);
        dto.set("bill_no", billNo);
      }
    }
    List pgdList = ConversionUtils.copyListDynaBeansToMap(dtolist);
    list.setDtoList(pgdList);

    req.setAttribute("pagedList", list);
    return mapping.findForward("list");

  }
}
