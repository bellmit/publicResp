package com.insta.hms.billing;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.AppInit;
import com.lowagie.text.DocumentException;
import freemarker.template.TemplateException;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CFDashboardReportAction extends DispatchAction {


    static Logger logger = LoggerFactory.getLogger(CFDashboardReportAction.class);
	/*
	 * The dashboard is significantly different from other reports. We have a
	 * separate action method for this.
	 */
	public ActionForward dashboardBuilder(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res) {
		return m.findForward("RevenueDashboardBuilder");
	}

	/*
	 * Consolidated financial dashboard: counts, collection, postedDate based revenue all in one
	 */
	public ActionForward dashboardReport(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res)
			throws SQLException, java.text.ParseException, IOException,
			TemplateException, DocumentException {

		String format = getParamDefault(req, "format", "pdf");
		String dateField = getParamDefault(req, "dateField", "posted_date");
		String accountGroup = getParamDefault(req, "accountGroup", "0");
		String centerFilter = getParamDefault(req, "centerFilter", "0");

		int viewCenter = Integer.parseInt(centerFilter);

		String fromDateStr = req.getParameter("fromDate");
		String toDateStr = req.getParameter("toDate");
		java.sql.Date fromDate = DataBaseUtil.parseDate(fromDateStr);
		java.sql.Date toDate = DataBaseUtil.parseDate(toDateStr);

		Map params = new HashMap();
		params.put("fromDate", fromDate);
		params.put("toDate", toDate);
		params.put("format", format);
		params.put("dateField", dateField);
		params.put("accountGroup", accountGroup);
		params.put("centerFilter", centerFilter);
		params.put("cpath", req.getContextPath());
		params.put("baseFontSize", getParamDefault(req, "baseFontSize", "10pt"));
		

		int centerId = (Integer) req.getSession(false).getAttribute("centerId");

		if (centerId != 0 ) {
			params.put("center_id", centerId);
		} else {
			params.put("center_id", viewCenter);
		}

		Object out;
		if (format.equals("pdf")) {
			out = res.getOutputStream();
			res.setContentType("application/pdf");
		} else {
			out = res.getWriter();
		}

		RevenueReportFtlHelper r = new RevenueReportFtlHelper(AppInit.getFmConfig());
		Connection con = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			r.getDashboardReport(con, params, out);
		} finally {
			con.close();
		}

		return null;
	}

	public ActionForward getHelpPage(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res) {

		return m.findForward("Help");

	}

	private String getParamDefault(HttpServletRequest req, String paramName,
			String defaultValue) {
		String value = req.getParameter(paramName);
		if ((value == null) || value.equals(""))
			value = defaultValue;
		return value;
	}
}
