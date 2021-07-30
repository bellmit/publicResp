package com.insta.hms.master.ComplaintsLog;

import au.com.bytecode.opencsv.CSVWriter;
import com.bob.hms.common.DataBaseUtil;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ComplaintsLogReportAction extends DispatchAction{


	public ActionForward complaintsLogExportCSV(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
		throws java.sql.SQLException,IOException,ServletException, java.text.ParseException {

		response.setHeader("Content-type","application/csv");
		response.setHeader("Content-disposition","attachment; filename=ComplaintsLog.csv");
		response.setHeader("Readonly","true");

		java.sql.Date fromDate = DataBaseUtil.parseDate(request.getParameter("fromDate"));
		java.sql.Date toDate = DataBaseUtil.parseDate(request.getParameter("toDate"));
		String status = request.getParameter("complaint_status");

		CSVWriter writer = new CSVWriter(response.getWriter(),CSVWriter.DEFAULT_SEPARATOR);

		ComplaintsLogReportDAO.complaintsLogExportCSV(writer, fromDate, toDate, status);
		return null;
	}


}
