package com.insta.hms.stores;

import au.com.bytecode.opencsv.CSVWriter;
import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.CommonReportAction;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SurplusStockReportAction extends CommonReportAction {

	static Logger log = LoggerFactory.getLogger(SurplusStockReportAction.class);

	@IgnoreConfidentialFilters
	public ActionForward getScreen(ActionMapping m, ActionForm f, HttpServletRequest req,
			HttpServletResponse res) {
		return m.findForward("getScreen");
	}

	@IgnoreConfidentialFilters
	public ActionForward getCsv (ActionMapping m, ActionForm f, HttpServletRequest req,
			HttpServletResponse res)
		throws IOException, SQLException, java.text.ParseException {
		String filename = m.getPath();
		filename = filename.substring(filename.lastIndexOf("/")+1);

		String fromDateStr = req.getParameter("fromDate");
		String toDateStr = req.getParameter("toDate");
		java.sql.Date fromDate = DataBaseUtil.parseDate(fromDateStr);
		java.sql.Date toDate = DataBaseUtil.parseDate(toDateStr);

		String store_id = req.getParameter("store_id");
		String fsn = req.getParameter("fsn");
		String qty = req.getParameter("qty");
		String diffDays = req.getParameter("diffdays");

		res.setHeader("Content-type","application/csv");
		res.setHeader("Content-disposition","attachment; filename=" + filename + ".csv");
		CSVWriter writer = new CSVWriter(res.getWriter(), CSVWriter.DEFAULT_SEPARATOR);
		SurplusStockReportDAO.getNSFmedicines(writer,fromDate, toDate, store_id, fsn, qty,diffDays);
		return null;

	}
}