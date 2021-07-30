package com.insta.hms.dialysis;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.master.Dialysis.DialysisMachineMasterDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// TODO: Auto-generated Javadoc
/**
 * The Class DialysisCurrentSessionsAction.
 */
public class DialysisCurrentSessionsAction extends DispatchAction {

  /**
   * List.
   *
   * @param mapping the mapping
   * @param form the form
   * @param req the req
   * @param res the res
   * @return the action forward
   * @throws ParseException the parse exception
   * @throws SQLException the SQL exception
   */
  @IgnoreConfidentialFilters
  public ActionForward list(ActionMapping mapping, ActionForm form, HttpServletRequest req,
      HttpServletResponse res) throws ParseException, SQLException {

    HashMap filter = new HashMap();
    filter.put("machine_id", req.getParameter("machine_id"));
    filter.put("start_attendant", req.getParameter("start_attendant"));
    filter.put("start_time1", DataBaseUtil.parseDate(req.getParameter("start_time1")));
    filter.put("start_time0", DataBaseUtil.parseDate(req.getParameter("start_time0")));
    filter.put("order_time1", DataBaseUtil.parseDate(req.getParameter("order_time1")));
    filter.put("status", ConversionUtils.getParamAsList(req.getParameterMap(), "status"));

    String dateRange = req.getParameter("date_range");
    String weekStartDate = null;

    if (dateRange != null && dateRange.equals("week")) {
      SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
      Calendar cal = Calendar.getInstance();
      cal.add(Calendar.DATE, -7);
      Date openDt = cal.getTime();
      weekStartDate = dateFormat.format(openDt);
      filter.put("order_time0", new java.sql.Date(openDt.getTime()));

    } else {
      filter.put("order_time0", DataBaseUtil.parseDate(req.getParameter("order_time0")));
    }

    PagedList list = DialysisSessionsDao.getAllCurrentSessions(filter,
        ConversionUtils.getListingParameter(req.getParameterMap()));

    req.setAttribute("machines",
        DialysisMachineMasterDAO.getMachines(RequestContext.getCenterId()));
    req.setAttribute("pagedList", list);
    ActionForward forward = new ActionForward(mapping.findForward("list").getPath());
    if (dateRange != null && dateRange.equals("week")
        && req.getParameter("order_time0") == null) {
      addParameter("order_time0", String.valueOf(weekStartDate), forward);
    }
    return forward;
  }

  /**
   * Gets the sessions screen.
   *
   * @param mapping the mapping
   * @param form the form
   * @param req the req
   * @param res the res
   * @return the sessions screen
   * @throws SQLException the SQL exception
   */
  public ActionForward getSessionsScreen(ActionMapping mapping, ActionForm form,
      HttpServletRequest req, HttpServletResponse res) throws SQLException {

    String mrNo = req.getParameter("mr_no");
    int orderId = 0;
    int servicePrescribeId = 0;
    ActionRedirect redirect = null;

    if (req.getParameter("order_id") != null && !(req.getParameter("order_id").equals(""))) {
      orderId = Integer.parseInt(req.getParameter("order_id"));
      BasicDynaBean bean = DialysisSessionsDao.getCurrentSessionDetails(orderId);

      String status = bean.get("status").toString();

      if (status.equals("O") || status.equals("P")) {
        redirect = new ActionRedirect(mapping.findForwardConfig("preDialysis"));
        redirect.addParameter("dialysisprescId", bean.get("prescription_id"));
      } else if (status.equals("I")) {
        redirect = new ActionRedirect(mapping.findForwardConfig("intraDialysis"));
      } else if (status.equals("F") || status.equals("C")) {
        redirect = new ActionRedirect(mapping.findForwardConfig("postDialysis"));
      }
      redirect.addParameter("order_id", bean.get("order_id"));
    } else {
      servicePrescribeId = Integer.parseInt(req.getParameter("prescriptionId"));
      redirect = new ActionRedirect(mapping.findForwardConfig("notInitiated"));
      redirect.addParameter("prescriptionId", servicePrescribeId);

    }
    redirect.addParameter("visit_center", req.getParameter("visit_center"));
    redirect.addParameter("mr_no", mrNo);
    return redirect;
  }

  /**
   * Adds the parameter.
   *
   * @param key the key
   * @param value the value
   * @param forward the forward
   */
  public void addParameter(String key, String value, ActionForward forward) {
    StringBuffer sb = new StringBuffer(forward.getPath());
    if (key == null || key.length() < 1) {
      return;
    }
    if (forward.getPath().indexOf('?') == -1) {
      sb.append('?');
    } else {
      sb.append('&');
    }
    sb.append(key + "=" + value);
    forward.setPath(sb.toString());
  }
}
