package com.insta.hms.master.DeliveryResultsApplicability;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.http.HttpHeaders;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ReportsCenterOverride extends DispatchAction {

	ReportsCenterOverrideDao dao = new ReportsCenterOverrideDao();
	CenterMasterDAO centerdao = new CenterMasterDAO();
	
    private static final GenericDAO centerReportDelivOverrideDetailsDAO =
        new GenericDAO("center_report_deliv_override_details");
    private static final GenericDAO centeReportDelivDaysOverridesDAO =
        new GenericDAO("center_report_deliv_days_overrides");
	
	public ActionForward list(ActionMapping m,ActionForm f,
			HttpServletRequest request,HttpServletResponse response) throws Exception {

		PagedList pagedList = null;
		Map map= request.getParameterMap();
		int centerId = Integer.parseInt(request.getParameter("center_id"));
		pagedList = dao.getReportsAvailabilityList(map,ConversionUtils.getListingParameter(map), centerId);
		BasicDynaBean bean = centerdao.findByKey("center_id", centerId);
		request.setAttribute("pagedList", pagedList);
		request.setAttribute("bean", bean);
		request.setAttribute("centerId", centerId);
		return m.findForward("list");
	}
	
	public ActionForward getScreen(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		int centerId = Integer.parseInt(request.getParameter("center_id"));
		
		int repDelivId=0;
		if (request.getParameter("rep_deliv_override_id")!=null) {
			repDelivId = Integer.parseInt(request.getParameter("rep_deliv_override_id"));
		}
		BasicDynaBean bean = centerdao.findByKey("center_id", centerId);
		Date r_report_startDate = null;
		Date r_report_endDate = null;
		BasicDynaBean rBean = dao.findByKey("rep_deliv_override_id", repDelivId);
		List<BasicDynaBean> reportsExistList = new ArrayList<BasicDynaBean>();
		if (centerId != 0) {
			reportsExistList = dao.getReportsList(centerId);
		}

		if (reportsExistList != null && reportsExistList.size() > 0) {
			for (int i = 0; i < reportsExistList.size(); i++) {
				r_report_startDate = new Date(((java.sql.Date) reportsExistList
						.get(i).get("day")).getTime());
				r_report_endDate = new  Date(((java.sql.Date)reportsExistList.get(reportsExistList.size()-1).get("day")).getTime());
				break;
			}
		}
		List<BasicDynaBean> reportsAvailtimingList = null;
			reportsAvailtimingList = dao.getOverrideReportsList(repDelivId);
			request.setAttribute("reportStartDate", r_report_startDate);
			request.setAttribute("reporEndDate", r_report_endDate);
			request.setAttribute("bean", bean);
			request.setAttribute("rBean", rBean);
			request.setAttribute("reportsAvailtimingList", reportsAvailtimingList);
		return mapping.findForward("show");
	}

	public ActionForward update(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res)
			throws IOException, ServletException, Exception {

		Map params = req.getParameterMap();
		List errors = new ArrayList();
		Connection con = null;
		int centerId = Integer.parseInt(req.getParameter("center_id"));
		FlashScope fScope = FlashScope.getScope(req);
		ActionRedirect redirect = new ActionRedirect(
				m.findForwardConfig("showRedirect"));
		SimpleDateFormat sm = new SimpleDateFormat("dd-MM-yyyy");
		int repAvailId = 0;
		boolean success = true;
		BasicDynaBean bean = dao.getBean();
		Date availabilityDate = null;
		boolean recordExistsWithinGivenDateRange = true;
		
		BasicDynaBean reportbean = null;
		if (req.getParameter("rep_deliv_override_id")!=null && !req.getParameter("rep_deliv_override_id").equals("")) {
			repAvailId = Integer.parseInt(req.getParameter("rep_deliv_override_id"));
			reportbean = dao.findByKey("rep_deliv_override_id", repAvailId);
		}
		
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			if (errors.isEmpty()) {
				if (req.getParameter("rep_deliv_override_id").equals("")) {
				bean = dao.getBean();
				ConversionUtils.copyToDynaBean(params, bean, errors);
				List<BasicDynaBean> existingAvailabiltyRecords = new ArrayList<BasicDynaBean>();
				existingAvailabiltyRecords = dao.getReportsList((Integer) bean
						.get("center_id"));
				Date fromDate = new Date(DateUtil.parseDate(req.getParameter("b_report_start_date")).getTime());
				Date toDate = new Date(DateUtil.parseDate(req.getParameter("b_report_end_date")).getTime());
				Calendar cal1 = Calendar.getInstance();
				Calendar cal2 = Calendar.getInstance();
				cal1.setTime(fromDate);
				cal2.setTime(toDate);
		
				if (existingAvailabiltyRecords != null && existingAvailabiltyRecords.size() > 0) {
					for (int i=0; i<existingAvailabiltyRecords.size();i++) {
						availabilityDate = (java.util.Date)existingAvailabiltyRecords.get(i).get("day");
						if (availabilityDate.equals(fromDate) || availabilityDate.equals(toDate)) {
							recordExistsWithinGivenDateRange = false;
							break;
						}
					}
				}
	
				if(recordExistsWithinGivenDateRange) {
					if (cal1.compareTo(cal2) == 0) {
						bean = dao.getBean();
						ConversionUtils.copyToDynaBean(params, bean, errors);
						repAvailId = dao.getNextSequence();
						bean.set("rep_deliv_override_id", repAvailId);
						bean.set("day", new java.sql.Date(cal1.getTime().getTime()));
						success = dao.insert(con, bean);
						if(saveReportsAvailability(con, req, repAvailId) == null)
							success = true;
						else
							success = false;

					} else {
						if (success) {
							while(cal1.before(cal2)) {
								bean = dao.getBean();
								ConversionUtils.copyToDynaBean(params, bean, errors);
								repAvailId = dao.getNextSequence();
								bean.set("rep_deliv_override_id", repAvailId);
								bean.set("day", new java.sql.Date(cal1.getTime().getTime()));
								success = dao.insert(con, bean);
								if(saveReportsAvailability(con, req, repAvailId) == null)
									success = true;
								else {
									success = false;
									break;
								}
								cal1.add(Calendar.DATE, 1);
							}
					}
					if (success) {
						bean = dao.getBean();
						ConversionUtils.copyToDynaBean(params, bean, errors);
						repAvailId = dao.getNextSequence();
						bean.set("rep_deliv_override_id", repAvailId);
						bean.set("day", new java.sql.Date(cal2.getTime().getTime()));
						success = dao.insert(con, bean);
						if(saveReportsAvailability(con, req, repAvailId) == null)
							success = true;
						else
							success = false;
					}
				}
				if(success){
					fScope.success("Reports Availability details inserted successfully..");
					redirect.addParameter(FlashScope.FLASH_KEY, fScope.key());
					redirect.addParameter("rep_deliv_override_id", bean.get("rep_deliv_override_id"));
					redirect.addParameter("center_id", req.getParameter("center_id"));
					if (success) {
						success = true;
					}
					return redirect;
				}

		} else {
			fScope.put("error","Reports Availability already exists within given date range...");
			redirect = new ActionRedirect(m.findForwardConfig("showRedirect"));
			redirect.addParameter("center_id", req.getParameter("center_id"));
			redirect.addParameter(FlashScope.FLASH_KEY, fScope.key());
			return redirect;
		}
				} else {
					if (saveReportsAvailability(con, req, repAvailId) == null) {
						if (dao.getOverrideReportsListCount(con, repAvailId) == 0) {
							if (success) {
								success = centeReportDelivDaysOverridesDAO.delete(con, "rep_deliv_override_id",repAvailId);
								success = true;
							}
						} else {
							success = true;
						}
					} else {
						success = false;
					}
				}
			}
		} finally {
			DataBaseUtil.commitClose(con, success);
		}
		FlashScope flash = FlashScope.getScope(req);

		flash.put("error", errors);
		req.setAttribute("reportbean", reportbean);
		redirect.addParameter("center_id", req.getParameter("center_id"));
		redirect.addParameter("rep_deliv_override_id", req.getParameter("rep_deliv_override_id"));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;
	}

	public String saveReportsAvailability(Connection con,
			HttpServletRequest req, int resAvailId) throws Exception {
		Map params = req.getParameterMap();
		List errors = new ArrayList();
		String[] rDelete = req.getParameterValues("r_delete");
		String[] reportTime = req.getParameterValues("delivery_time");
		String[] repAvailDetId = req.getParameterValues("rep_delivery_override_time_id");
		BasicDynaBean reportAvailBean = null;
		boolean success = true;
		List availDetIds = new ArrayList();
		
		if (repAvailDetId != null) {
			for (int j=0;j<repAvailDetId.length;j++) {
				if (!repAvailDetId[j].equals("_") && !repAvailDetId[j].equals("")) {
					if (rDelete[j].equals("false") && !reportTime[j].equals("")) {
						reportAvailBean = centerReportDelivOverrideDetailsDAO.getBean();
						ConversionUtils.copyIndexToDynaBean(params,j, reportAvailBean, errors);
						Map<String, Integer> keys = new HashMap<String, Integer>();
						keys.put("rep_delivery_override_time_id", Integer.parseInt(repAvailDetId[j]));
						reportAvailBean.set("rep_deliv_override_id", resAvailId);
						success = centerReportDelivOverrideDetailsDAO.update(con, reportAvailBean.getMap(), keys) > 0;
						availDetIds.add(reportAvailBean.get("rep_delivery_override_time_id"));

						if (!success)
							break;
					} else {
						
						success = centerReportDelivOverrideDetailsDAO.delete(con, "rep_delivery_override_time_id", Integer.parseInt(repAvailDetId[j]));
						if (!success)
							break;
					}
				} else {
                  if (reportTime[j] != null && !repAvailDetId[j].equals("")) {
                    reportAvailBean = centerReportDelivOverrideDetailsDAO.getBean();
                    ConversionUtils.copyIndexToDynaBean(params, j, reportAvailBean, errors);
                    reportAvailBean.set("rep_delivery_override_time_id",
                        centerReportDelivOverrideDetailsDAO.getNextSequence());
                    reportAvailBean.set("rep_deliv_override_id", resAvailId);
                    success = centerReportDelivOverrideDetailsDAO.insert(con, reportAvailBean);
                    availDetIds.add(reportAvailBean.get("rep_delivery_override_time_id"));
                  }
                }
				}
			}
		if (success) {
			return null;
		} else {
			return "error";
		}
	}
	
	public ActionForward deleteSelectedRows(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse res) throws IOException, SQLException, ParseException {
			 String resAvaillId = request.getParameter("rep_deliv_override_id");
			 boolean success = true;
			 String responseContent = "Deleted";
			 String[] resAvailIds = null;
			 Connection con = null;
			 if (resAvaillId.contains(",")) {
				 String [] arrayElements = resAvaillId.split(",");
				 resAvailIds = new String[arrayElements.length];
				 resAvailIds = arrayElements;
			 } else {
				 resAvailIds = new String[1];
				 resAvailIds[0] = resAvaillId;
			 }

			 try {
				con = DataBaseUtil.getConnection();
				con.setAutoCommit(false);
				for (int i=0;i<resAvailIds.length;i++) {
					success = dao.delete(con, "rep_deliv_override_id", Integer.parseInt(resAvailIds[i]));
					if (success) {
						success = centerReportDelivOverrideDetailsDAO.delete(con, "rep_deliv_override_id", Integer.parseInt(resAvailIds[i]));
					}
				}
			} finally {
				DataBaseUtil.commitClose(con, success);
			}
			if (!success) {
				responseContent = "";
			}
			res.setContentType("application/json");
			res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
			res.getWriter().write(responseContent);
			res.flushBuffer();
			return null;
		}
	
	public ActionForward deleteReportsAvailability(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse res) throws Exception {
		int centerId = Integer.parseInt(request.getParameter("center_id"));
		ActionRedirect redirect = new ActionRedirect(mapping.findForwardConfig("listRedirect"));
		String repAvailId = request.getParameter("rep_deliv_override_id");
		boolean success = true;
		Connection con = null;
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			success = dao.delete(con, "rep_deliv_override_id", Integer.parseInt(repAvailId));
			if (success) {
				success = centerReportDelivOverrideDetailsDAO.delete(con, "rep_deliv_override_id", Integer.parseInt(repAvailId));
			}
		} finally {
			DataBaseUtil.commitClose(con, success);
		}
		redirect.addParameter("center_id", centerId);
		return redirect;
	}
	
	public ActionForward showDefaultTimings(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse res) throws Exception {

		int centerId = Integer.parseInt(request.getParameter("center_id"));
		ActionRedirect redirect = new ActionRedirect(mapping.findForwardConfig("defaultTimingsRedirect"));
		BasicDynaBean bean = null;

		if (bean == null) {
			bean = dao.getReportsListSize(centerId);
		}

		if (bean!= null) {
			redirect.addParameter("center_id", bean.get("center_id"));
		}
		return redirect;
	}
}
