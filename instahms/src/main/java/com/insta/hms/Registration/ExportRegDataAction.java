package com.insta.hms.Registration;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.bob.hms.common.DateUtil;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;

public class ExportRegDataAction extends DispatchAction{

	ExportRegDataDAO dao = new ExportRegDataDAO();
	
	@IgnoreConfidentialFilters
	public ActionForward show(ActionMapping mapping, ActionForm form,
			HttpServletRequest req, HttpServletResponse res)
			throws IOException, ServletException, SQLException, ParseException {
		return mapping.findForward("show");
	}

	@IgnoreConfidentialFilters
	public ActionForward getReport(ActionMapping mapping, ActionForm form,
			HttpServletRequest req, HttpServletResponse res)
			throws Exception {
		HttpSession s = req.getSession(false);

		String fromDateStr = req.getParameter("fromDate");
		String toDateStr = req.getParameter("toDate");
		java.sql.Date fromDate = DateUtil.parseDate(fromDateStr);
		java.sql.Date toDate = DateUtil.parseDate(toDateStr);
        String user = (String)s.getAttribute("userid");

		String fileName = "EncounterData_" + fromDateStr + "_to_" + toDateStr;

		OutputStream stream = res.getOutputStream();
		//res.setContentType("text/xml");
		res.setContentType("text/plain");
		res.setHeader("Content-disposition",
				"attachment; filename=" + fileName + ".xml");
		generateXml(stream, fromDate, toDate, user);
		stream.flush();
		return null;
	}

	public static void generateXml(OutputStream stream, java.sql.Date fromDate, java.sql.Date toDate,
			String user) throws Exception {

		Map headerMap = new HashMap();//ExportRegDataDAO.getXmlHeaderFields();
		headerMap.put("todays_date", DateUtil.getCurrentTimestamp());
		headerMap.put("user_name", user);

		PatDataExportUtility.addClaimHeader(stream, headerMap);
		ExportRegDataDAO.getXmlFieldsArray(fromDate, toDate, stream);
		PatDataExportUtility.addClaimFooter(stream, new HashMap());
	}


}
