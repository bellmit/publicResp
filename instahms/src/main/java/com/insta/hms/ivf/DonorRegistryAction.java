package com.insta.hms.ivf;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// TODO: Auto-generated Javadoc
/**
 * The Class DonorRegistryAction.
 */
public class DonorRegistryAction extends DispatchAction {
  
  private static final GenericDAO ivfDonorHeaderDAO = new GenericDAO("ivf_donor_header");

  /**
   * List.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the action forward
   * @throws SQLException the SQL exception
   * @throws ParseException the parse exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public ActionForward list(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws SQLException, ParseException, IOException {
    Map map = request.getParameterMap();
    PagedList list = DonorRegistryDAO.getDonorregistryDetails(map,
        ConversionUtils.getListingParameter(map));
    request.setAttribute("PagedList", list);
    return mapping.findForward("list");
  }

  /**
   * Adds the.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the action forward
   * @throws SQLException the SQL exception
   * @throws ParseException the parse exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public ActionForward add(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws SQLException, ParseException, IOException {
    return mapping.findForward("addshow");
  }

  /**
   * Show.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the action forward
   * @throws SQLException the SQL exception
   * @throws ParseException the parse exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public ActionForward show(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws SQLException, ParseException, IOException {
    String mrNo = request.getParameter("mr_no");
    BasicDynaBean bean = ivfDonorHeaderDAO.findByKey("mr_no", mrNo);
    request.setAttribute("bean", bean);
    return mapping.findForward("addshow");
  }

  /**
   * Gets the patient visit details.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the patient visit details
   * @throws SQLException the SQL exception
   * @throws ParseException the parse exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public ActionForward getPatientVisitDetails(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws SQLException,
      ParseException, IOException {
    ActionRedirect redirect = null;
    String visitId = request.getParameter("patient_id");
    Map visitbean = VisitDetailsDAO.getPatientVisitDetailsMap(visitId);

    if (visitbean == null) {
      FlashScope flash = FlashScope.getScope(request);
      flash.put("error", visitId + " doesn't exists.");
      redirect = new ActionRedirect(mapping.findForward("addRedirect"));
      redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
      return redirect;
    }

    redirect = new ActionRedirect(mapping.findForward("addRedirect"));
    redirect.addParameter("_method", "add");
    redirect.addParameter("mr_no", visitbean.get("mr_no"));
    return redirect;
  }

  /**
   * Creates the.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the action forward
   * @throws SQLException the SQL exception
   * @throws ParseException the parse exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public ActionForward create(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws SQLException, ParseException, IOException {
    BasicDynaBean donorBean = ivfDonorHeaderDAO.getBean();
    Map requestingparams = request.getParameterMap();
    List errors = new ArrayList();
    ConversionUtils.copyToDynaBean(requestingparams, donorBean, errors);
    Connection con = null;
    Boolean success = false;
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      success = ivfDonorHeaderDAO.insert(con, donorBean);
    } finally {
      DataBaseUtil.commitClose(con, success);
    }
    ActionRedirect redirect = null;
    redirect = new ActionRedirect(mapping.findForward("showRedirect"));
    String mrNo = request.getParameter("mr_no");
    redirect.addParameter("mr_no", mrNo);
    return redirect;
  }

  /**
   * Update.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the action forward
   * @throws SQLException the SQL exception
   * @throws ParseException the parse exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public ActionForward update(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws SQLException, ParseException, IOException {
    Map requestingparams = request.getParameterMap();
    List errors = new ArrayList();
    String mrNo = request.getParameter("mr_no");
    BasicDynaBean donorBean = ivfDonorHeaderDAO.getBean();
    Connection con = null;
    Boolean success = false;
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      ConversionUtils.copyToDynaBean(requestingparams, donorBean, errors);
      Map<String, Object> keys = new HashMap<String, Object>();
      keys.put("mr_no", mrNo);
      int updatedCnt = ivfDonorHeaderDAO.update(con, donorBean.getMap(), keys);
      if (updatedCnt > 0) {
        success = true;
      }
    } finally {
      DataBaseUtil.commitClose(con, success);
    }
    ActionRedirect redirect = null;
    redirect = new ActionRedirect(mapping.findForward("showRedirect"));
    redirect.addParameter("mr_no", mrNo);
    return redirect;
  }
}
