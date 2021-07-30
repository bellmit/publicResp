package com.insta.hms.diagnosticsmasters;

import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.diagnosticmodule.prescribetest.DiagnoDAOImpl;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import flexjson.JSONSerializer;

public class TestsCacheAction extends Action {

	public static final DiagnoDAOImpl diagnoDao = new DiagnoDAOImpl();

	@IgnoreConfidentialFilters
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws IOException, ServletException, SQLException {

		if (null != request.getHeader("If-Modified-Since")) {
			// the browser is just requesting the same thing, only if modified.
			// Since we encode the timestamp in the request, if we get a request,
			// it CANNOT have been modified. So, just say 304 not modified without checking.
			response.setStatus(304);
			return null;
		}

		// Last-Modified is required, cache-control is good to have to enable caching
		response.setHeader("Last-modified", "Thu, 1 Jan 2009 00:00:00 GMT");
		response.setHeader(HttpHeaders.CACHE_CONTROL, "public; max-age=360000");
		response.setContentType("text/javascript");

		String orgId = (String)request.getParameter("orgidforitem");
		String sampleNeeded =(String)request.getParameter("sampleNeeded");
		String module =(String)request.getParameter("module");
		String packages = (String)request.getParameter("packages");
		String ageText = (String)request.getParameter("ageText");
		if("".equals(sampleNeeded))
		   sampleNeeded=null;
		if (orgId == null || orgId.equals(""))
			orgId = "ORG0001";
		if ( (module == null) || module.equals("") )
			module = "all";
		if (StringUtils.isNotEmpty(ageText)){
			ageText = "P" + ageText;
		}

		// now write out the JavaScript that encodes the medicine stock data
		JSONSerializer js = new JSONSerializer().exclude("class");
		response.getWriter().write("var deptWiseTestsjson = ");

		BasicDynaBean genericPrefs = GenericPreferencesDAO.getAllPrefs();
		String centerIdStr = (String) request.getParameter("center_id");
		Integer centerId = 0; // all centers
		if ((Integer) genericPrefs.get("max_centers_inc_default") > 1 && centerIdStr != null && !centerIdStr.equals(""))
			centerId = Integer.parseInt(centerIdStr);

		if(request.getParameter("packages") != null && packages.equals("Y") && module.equals("DEP_LAB")){
			response.getWriter().write(js.serialize(diagnoDao.getTestAndPackageNames(module,
					orgId,sampleNeeded, centerId, ageText)));
		}else{
			response.getWriter().write(js.serialize(
					diagnoDao.getTestNames(module, orgId,sampleNeeded,centerId)));
		}
		response.getWriter().write(";");

		return null;
	}
}

