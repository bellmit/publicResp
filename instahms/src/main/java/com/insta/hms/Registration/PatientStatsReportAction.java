/**
 *
 */
package com.insta.hms.Registration;

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


/**
 * @author lakshmi.p
 *
 */
public class PatientStatsReportAction extends DispatchAction {

	static Logger log = LoggerFactory.getLogger(PatientStatsReportAction.class);

	public ActionForward getReportBuilder(ActionMapping m,ActionForm f,
			HttpServletRequest req,HttpServletResponse res) throws Exception {
		String reportName = m.getProperty("report-name");
		if ("summaryReport".equalsIgnoreCase(reportName)) {
			return tableBuilder(m, f, req, res);
		} else if ("trendReport".equalsIgnoreCase(reportName)) {
			return trendBuilder(m, f, req, res);
		} else if ("visitTrendReport".equalsIgnoreCase(reportName)) {
			return visitTrendBuilder(m, f, req, res);
		} else if ("admitDischargeTrendReport".equalsIgnoreCase(reportName)) {
			return admitDischargeTrendBuilder(m, f, req, res);
		}
		return tableBuilder(m, f, req, res);
	}

	public ActionForward getReport(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res)
		throws SQLException,java.text.ParseException, IOException, TemplateException, DocumentException {
		String reportName = m.getProperty("report-name");
		if ("summaryReport".equalsIgnoreCase(reportName)) {
			return summaryReport(m, f, req, res);
		} else if ("trendReport".equalsIgnoreCase(reportName)) {
			return trendReport(m, f, req, res);
		} else if ("visitTrendReport".equalsIgnoreCase(reportName)) {
			return visitTrendReport(m, f, req, res);
		} else if ("admitDischargeTrendReport".equalsIgnoreCase(reportName)) {
			return admitDischargeTrendReport(m, f, req, res);
		}
		return summaryReport(m, f, req, res);
	}

	public ActionForward tableBuilder(ActionMapping m,ActionForm f,
			HttpServletRequest req,HttpServletResponse res) throws Exception {
		return m.findForward("PatientStatsTableBuilder");
	}

	public ActionForward trendBuilder(ActionMapping m,ActionForm f,
			HttpServletRequest req, HttpServletResponse res) throws Exception{
		return m.findForward("PatientStatsTrendBuilder");
	}

	public ActionForward visitTrendBuilder(ActionMapping m,ActionForm f,
			HttpServletRequest req, HttpServletResponse res) throws Exception{
		return m.findForward("PatientVisitStatsTrendBuilder");
	}

	public ActionForward admitDischargeTrendBuilder(ActionMapping m,ActionForm f,
			HttpServletRequest req, HttpServletResponse res) throws Exception{
		return m.findForward("PatientAdmitDischargeTrendBuilder");
	}

	/*
	 * Get the summary report based on given parameters:
	 *  fromDate, toDate
	 *  groupBy
	 */
	public ActionForward summaryReport(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res)
		throws SQLException,java.text.ParseException, IOException, TemplateException, DocumentException {

		String format = getParamDefault(req, "format", "pdf");
		String groupBy = getParamDefault(req, "groupBy", "dept_name");
		String groupByName = getParamDefault(req, "groupByName", "Department");

		String fromDateStr = req.getParameter("fromDate");
		String toDateStr = req.getParameter("toDate");
		java.sql.Date fromDate = DataBaseUtil.parseDate(fromDateStr);
		java.sql.Date toDate = DataBaseUtil.parseDate(toDateStr);
		String centerClause = req.getParameter("centerClause");
		String centerName = req.getParameter("centerName");

		Map params = new HashMap();
		params.put("fromDate", fromDate);
		params.put("toDate", toDate);
		params.put("format", format);
		params.put("groupBy", groupBy);
		params.put("groupByName", groupByName);
		params.put("centerClause", centerClause);
		params.put("centerName", centerName);

		Object out;
		if (format.equals("pdf")) {
			out = res.getOutputStream();
			res.setContentType("application/pdf");
		} else {
			out = res.getWriter();
		}

		PatientStatsReportFtlHelper p = new PatientStatsReportFtlHelper(AppInit.getFmConfig());
		Connection con = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			p.getSummaryReport(con, params, out);
		} finally {
			con.close();
		}

		return null;
	}


	/*
	 * Get the trend report based on given parameters:
	 *  fromDate, toDate
	 *  trendPeriod, groupBy
	 */
	public ActionForward trendReport(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res)
		throws SQLException,java.text.ParseException, IOException, TemplateException, DocumentException {

		String format = getParamDefault(req, "format", "pdf");
		String groupBy = getParamDefault(req, "groupBy", "dept_name");
		String groupByName = getParamDefault(req, "groupByName", "Department");
		String trendPeriod = getParamDefault(req, "trendPeriod", "month");

		String fromDateStr = req.getParameter("fromDate");
		String toDateStr = req.getParameter("toDate");
		java.sql.Date fromDate = DataBaseUtil.parseDate(fromDateStr);
		java.sql.Date toDate = DataBaseUtil.parseDate(toDateStr);
		String centerClause = req.getParameter("centerClause");
		String centerName = req.getParameter("centerName");

		Map params = new HashMap();
		params.put("fromDate", fromDate);
		params.put("toDate", toDate);
		params.put("format", format);
		params.put("groupBy", groupBy);
		params.put("groupByName", groupByName);
		params.put("trendPeriod", trendPeriod);
		params.put("centerClause", centerClause);
		params.put("centerName", centerName);

		Object out;
		if (format.equals("pdf")) {
			out = res.getOutputStream();
			res.setContentType("application/pdf");
		} else {
			out = res.getWriter();
		}

		PatientStatsReportFtlHelper p = new PatientStatsReportFtlHelper(AppInit.getFmConfig());
		Connection con = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			p.getTrendReport(con, params, out);
		} finally {
			con.close();
		}

		return null;
	}


	/*
	 * Get the visit trend report based on given parameters:
	 *  fromDate, toDate
	 *  trendPeriod, groupBy
	 */
	public ActionForward visitTrendReport(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res)
		throws SQLException,java.text.ParseException, IOException, TemplateException, DocumentException {

		String format = getParamDefault(req, "format", "pdf");
		String groupBy = getParamDefault(req, "groupBy", "dept_name");
		String groupByName = getParamDefault(req, "groupByName", "Department");
		String trendPeriod = getParamDefault(req, "trendPeriod", "month");

		String fromDateStr = req.getParameter("fromDate");
		String toDateStr = req.getParameter("toDate");
		java.sql.Date fromDate = DataBaseUtil.parseDate(fromDateStr);
		java.sql.Date toDate = DataBaseUtil.parseDate(toDateStr);
		String centerClause = req.getParameter("centerClause");
		String centerName = req.getParameter("centerName");

		Map params = new HashMap();
		params.put("fromDate", fromDate);
		params.put("toDate", toDate);
		params.put("format", format);
		params.put("groupBy", groupBy);
		params.put("groupByName", groupByName);
		params.put("trendPeriod", trendPeriod);
		params.put("centerClause", centerClause);
		params.put("centerName", centerName);

		Object out;
		if (format.equals("pdf")) {
			out = res.getOutputStream();
			res.setContentType("application/pdf");
		} else {
			out = res.getWriter();
		}

		PatientStatsReportFtlHelper p = new PatientStatsReportFtlHelper(AppInit.getFmConfig());
		Connection con = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			p.getVisitTrendReport(con, params, out);
		} finally {
			con.close();
		}

		return null;
	}

	/*
	 * Get the admit discharge trend report based on given parameters:
	 *  fromDate, toDate
	 *  trendPeriod, groupBy
	 */
	public ActionForward admitDischargeTrendReport(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res)
		throws SQLException,java.text.ParseException, IOException, TemplateException, DocumentException {

		String format = getParamDefault(req, "format", "pdf");
		String groupBy = getParamDefault(req, "groupBy", "dept_name");
		String groupByName = getParamDefault(req, "groupByName", "Department");
		String trendPeriod = getParamDefault(req, "trendPeriod", "month");

		String fromDateStr = req.getParameter("fromDate");
		String toDateStr = req.getParameter("toDate");
		java.sql.Date fromDate = DataBaseUtil.parseDate(fromDateStr);
		java.sql.Date toDate = DataBaseUtil.parseDate(toDateStr);
		String centerClause = req.getParameter("centerClause");
		String centerName = req.getParameter("centerName");

		Map params = new HashMap();
		params.put("fromDate", fromDate);
		params.put("toDate", toDate);
		params.put("format", format);
		params.put("groupBy", groupBy);
		params.put("groupByName", groupByName);
		params.put("trendPeriod", trendPeriod);
		params.put("centerClause", centerClause);
		params.put("centerName", centerName);

		Object out;
		if (format.equals("pdf")) {
			out = res.getOutputStream();
			res.setContentType("application/pdf");
		} else {
			out = res.getWriter();
		}

		PatientStatsReportFtlHelper p = new PatientStatsReportFtlHelper(AppInit.getFmConfig());
		Connection con = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			p.getAdmitDischargeTrendReport(con, params, out);
		} finally {
			con.close();
		}

		return null;
	}


	private String getParamDefault(HttpServletRequest req, String paramName, String defaultValue) {
		String value = req.getParameter(paramName);
		if ((value == null) || value.equals(""))
			value = defaultValue;
		return value;
	}



}
